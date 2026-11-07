import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/** Game.parsePacket opcode 70: [x signed BE short][y signed LE short][interface id unsigned LE short]. Original names only. */
public class InterfaceOffsetPacketTest {
    private Socket client, server;
    private ServerSocket listener;
    private RSInterface[] saved;

    @Before public void before() { saved = RSInterface.interfaceCache; }
    @After public void after() throws Exception {
        RSInterface.interfaceCache = saved;
        if (client != null) client.close();
        if (server != null) server.close();
        if (listener != null) listener.close();
    }

    private void deliver(Game g, byte[] payload) throws Exception {
        listener = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"));
        client = new Socket(InetAddress.getByName("127.0.0.1"), listener.getLocalPort());
        server = listener.accept();
        RSSocket rs = new RSSocket(null, client);
        server.getOutputStream().write(payload); server.getOutputStream().flush();
        long end = System.currentTimeMillis() + 5000;
        while (rs.available() < payload.length && System.currentTimeMillis() < end) Thread.sleep(5);
        g.inStream = new Stream(new byte[200]);
        g.socketStream = rs;
        g.pktType = 70; g.pktSize = payload.length;
        assertTrue(g.parsePacket());
        assertEquals(-1, g.pktType);
    }
    private static byte[] pkt(int x, int y, int id) {
        return new byte[] {(byte) (x >> 8), (byte) x, (byte) y, (byte) (y >> 8), (byte) id, (byte) (id >> 8)};
    }
    private RSInterface[] cache(int n) {
        RSInterface[] c = new RSInterface[n];
        for (int i = 0; i < n; i++) { c[i] = new RSInterface(); c[i].offsetX = 9000 + i; c[i].offsetY = 8000 + i; }
        RSInterface.interfaceCache = c;
        return c;
    }

    @Test public void firstShortIsXSecondIsYAndTheIdPicksTheInterface() throws Exception {
        RSInterface[] c = cache(10);
        deliver(new Game(), pkt(123, 45, 7));
        assertEquals(123, c[7].offsetX); assertEquals(45, c[7].offsetY);
        for (int i = 0; i < 10; i++) if (i != 7) { assertEquals(9000 + i, c[i].offsetX); assertEquals(8000 + i, c[i].offsetY); }
    }
    @Test public void offsetsAreSignedAndByteOrdersDiffer() throws Exception {
        RSInterface[] c = cache(10);
        deliver(new Game(), pkt(-300, -2, 3));
        assertEquals(-300, c[3].offsetX); assertEquals(-2, c[3].offsetY);
        deliver(new Game(), pkt(0x0102, 0x0304, 4));
        assertEquals(0x0102, c[4].offsetX); assertEquals(0x0304, c[4].offsetY);
    }
    @Test public void interfaceIdIsUnsignedAndSixteenBit() throws Exception {
        RSInterface[] c = cache(40000);
        deliver(new Game(), pkt(5, 6, 33000));
        assertEquals(5, c[33000].offsetX); assertEquals(6, c[33000].offsetY);
        assertEquals(9000 + 32999, c[32999].offsetX);
    }
}
