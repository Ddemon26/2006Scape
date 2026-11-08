import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Packets 75 (interface media model) and 99 (minimap state) of Game.parsePacket. Original member names only; packet
 * bytes travel through a real loopback RSSocket.
 */
public class MediaAndMinimapPacketTest {
    private final List<Socket> sockets = new ArrayList<Socket>();
    private ServerSocket listener;

    @After public void tearDown() throws Exception {
        for (Socket s : sockets) try { s.close(); } catch (Exception e) { }
        if (listener != null) listener.close();
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
        assertEquals(payload.length, g.inStream.currentOffset);
    }

    private Game game() {
        Game g = new Game();
        g.inStream = new Stream(new byte[6000]);
        return g;
    }

    /** Packet 75 body: two little-endian shorts whose low byte carries +128. */
    private static byte[] packet75(int first, int second) {
        return new byte[] {(byte) ((first & 0xff) + 128), (byte) (first >> 8), (byte) ((second & 0xff) + 128), (byte) (second >> 8)};
    }

    private static RSInterface[] interfaces(int n) {
        RSInterface[] a = new RSInterface[n];
        for (int i = 0; i < n; i++) { a[i] = new RSInterface(); a[i].mediaType = 9; a[i].mediaID = -5; }
        return a;
    }

    private static Model vertexModel(int vx) throws Exception {
        Constructor<Model> k = Model.class.getDeclaredConstructor(); k.setAccessible(true);
        Model m = k.newInstance(); m.vertexCount = 1; m.vertexX = new int[]{vx}; m.vertexY = new int[]{0}; m.vertexZ = new int[]{0};
        m.faceCount = 0; m.faceVertexA = new int[0]; m.faceVertexB = new int[0]; m.faceVertexC = new int[0];
        return m;
    }

    @Test public void packet75FirstShortIsTheMediaIdAndSecondSelectsTheInterface() throws Exception {
        RSInterface[] saved = RSInterface.interfaceCache;
        try {
            int[][] cases = {{0, 0}, {1, 2}, {2, 1}, {255, 256}, {256, 255}, {1000, 3}, {3, 1000}, {65535, 65534}, {128, 127}};
            for (int[] c : cases) {
                RSInterface[] a = interfaces(66000);
                RSInterface.interfaceCache = a;
                deliver(game(), 75, packet75(c[0], c[1]));
                for (int i = 0; i < a.length; i++) {
                    if (i == c[1]) { assertEquals(2, a[i].mediaType); assertEquals(c[0], a[i].mediaID); }
                    else { assertEquals(9, a[i].mediaType); assertEquals(-5, a[i].mediaID); }
                }
            }
        } finally { RSInterface.interfaceCache = saved; }
    }

    @Test public void packet75MediaIdIsTheKeyOfTheTypeTwoModelCache() throws Exception {
        RSInterface[] saved = RSInterface.interfaceCache;
        try {
            RSInterface[] a = interfaces(10);
            RSInterface.interfaceCache = a;
            Model m3 = vertexModel(3);
            RSInterface.replaceCachedModel(m3, 300, 2);       // type 2 (NPC head), id 300; the cache holds one entry
            deliver(game(), 75, packet75(300, 4));
            assertSame(m3, a[4].getMediaModel(-1, -1, false));
        } finally { RSInterface.interfaceCache = saved; }
    }

    @Test public void packet99StoresOneUnsignedByteAndOnlyStateZeroAllowsMinimapClicks() throws Exception {
        for (int v : new int[] {0, 1, 2, 3, 127, 128, 255}) {
            Game g = game();
            g.minimapState = 77;
            deliver(g, 99, new byte[] {(byte) v});
            assertEquals(v, g.minimapState);
            g.clickMode3 = 1; g.saveClickX = 0; g.saveClickY = 0; g.minimapPrimaryClickCount = 5;
            g.processMainScreenClick();
            assertEquals("state " + v, v == 0 ? 6 : 5, g.minimapPrimaryClickCount);
        }
    }
}
