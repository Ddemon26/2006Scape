import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Opcode 60 of Game.parsePacket: a two byte header (updateBlockY first, then updateBlockX as a negated byte) followed by
 * sub-opcodes dispatched through method137 until pktSize bytes are consumed. Original member names only. Sub-opcode
 * 105 (positional sound) compares the sub-record's tile, updateBlockX/updateBlockY plus the nibbles, to the player's tile,
 * so it observes the header values and the sub-opcode local without touching the scene.
 */
public class Packet60AnchoredUpdatesTest {
    private final List<Socket> sockets = new ArrayList<Socket>();
    private ServerSocket listener;
    private Player savedPlayer;
    private boolean savedLowMem;

    @After public void tearDown() throws Exception {
        for (Socket s : sockets) try { s.close(); } catch (Exception e) { }
        if (listener != null) listener.close();
        Game.myPlayer = savedPlayer; Game.lowMem = savedLowMem;
    }

    private Game game(int playerX, int playerY) {
        savedPlayer = Game.myPlayer; savedLowMem = Game.lowMem;
        Game g = new Game();
        g.inStream = new Stream(new byte[6000]);
        g.soundEffectsEnabled = true; Game.lowMem = false;
        Game.myPlayer = new Player();
        Game.myPlayer.smallX[0] = playerX; Game.myPlayer.smallY[0] = playerY;
        return g;
    }
    private void deliver(Game g, int opcode, byte[] payload) throws Exception {
        if (listener == null) listener = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"));
        Socket client = new Socket(InetAddress.getByName("127.0.0.1"), listener.getLocalPort());
        Socket server = listener.accept();
        sockets.add(client); sockets.add(server);
        RSSocket rs = new RSSocket(null, client);
        server.getOutputStream().write(payload); server.getOutputStream().flush();
        long end = System.currentTimeMillis() + 5000;
        while (rs.available() < payload.length && System.currentTimeMillis() < end) Thread.sleep(5);
        g.socketStream = rs;
        g.pktType = opcode; g.pktSize = payload.length;
        assertTrue(g.parsePacket());
        assertEquals(-1, g.pktType);
    }
    /** header (y, negated x) + one positional sound record with radius 0 at nibble offsets (dx, dy). */
    private static byte[] sound(int y, int x, int dx, int dy, int id, byte[]... more) {
        java.io.ByteArrayOutputStream o = new java.io.ByteArrayOutputStream();
        o.write(y); o.write(-x);
        o.write(105); o.write(dx << 4 | dy); o.write(id >> 8); o.write(id); o.write(0x02);
        for (byte[] m : more) o.write(m, 0, m.length);
        return o.toByteArray();
    }

    @Test public void headerSetsXFromSecondByteNegatedAndYFromFirst() throws Exception {
        Game g = game(40 + 3, 17 + 5);
        deliver(g, 60, sound(17, 40, 3, 5, 321));
        assertEquals(40, g.updateBlockX);
        assertEquals(17, g.updateBlockY);
        assertEquals(1, g.currentSound);
        assertEquals(321, g.sound[0]);
        assertEquals(2, g.soundType[0]);
    }
    @Test public void swappedAxesDoNotMatch() throws Exception {
        Game g = game(17 + 3, 40 + 5);
        deliver(g, 60, sound(17, 40, 3, 5, 321));
        assertEquals(0, g.currentSound);
    }
    @Test public void offByOneEitherAxisDoesNotMatch() throws Exception {
        int[][] off = {{44, 22}, {42, 22}, {43, 23}, {43, 21}};
        for (int[] p : off) {
            Game g = game(p[0], p[1]);
            deliver(g, 60, sound(17, 40, 3, 5, 321));
            assertEquals(0, g.currentSound);
        }
    }
    @Test public void negatedByteWrapsForLargeX() throws Exception {
        Game g = game(100 + 1, 2 + 1);
        deliver(g, 60, sound(2, 100, 1, 1, 7));
        assertEquals(100, g.updateBlockX);
        assertEquals(1, g.currentSound);
    }
    @Test public void everySubRecordIsDispatchedUntilPacketEnds() throws Exception {
        Game g = game(10 + 2, 20 + 4);
        byte[] second = {105, (byte) (2 << 4 | 4), 0, 9, 0x01};
        byte[] third = {105, (byte) (6 << 4 | 6), 0, 11, 0x01};
        deliver(g, 60, sound(20, 10, 2, 4, 5, second, third));
        assertEquals(2, g.currentSound);
        assertEquals(5, g.sound[0]);
        assertEquals(9, g.sound[1]);
    }
    @Test public void subOpcodeIsTheFirstByteAfterHeader() throws Exception {
        Game g = game(10 + 2, 20 + 4);
        // header followed by an unknown sub-opcode: nothing dispatched, header still stored
        deliver(g, 60, new byte[] {20, (byte) -10, 0});
        assertEquals(0, g.currentSound);
        assertEquals(10, g.updateBlockX);
        assertEquals(20, g.updateBlockY);
    }
}
