import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class Packet64GroundClearTest {
    private static final class ObservedGame extends Game {
        final List<Integer> visits = new ArrayList<Integer>();
        @Override public void spawnGroundItem(int a, int b) {
            assertNull(groundArray[plane][a][b]);
            visits.add(a * 104 + b);
        }
    }

    private static void deliver(Game g, int a, int b) throws Exception {
        InetAddress host = InetAddress.getByName("127.0.0.1");
        try (ServerSocket listener = new ServerSocket(0, 1, host);
             Socket client = new Socket(host, listener.getLocalPort());
             Socket server = listener.accept()) {
            server.getOutputStream().write(new byte[] {64, (byte) -a, (byte) (128 - b)});
            server.getOutputStream().flush();
            g.socketStream = new RSSocket(null, client);
            long end = System.currentTimeMillis() + 5000;
            while (g.socketStream.available() < 3 && System.currentTimeMillis() < end) Thread.sleep(5);
            g.inStream = new Stream(new byte[6000]);
            g.pktType = -1;
            assertTrue(g.parsePacket());
            assertEquals(-1, g.pktType);
            assertEquals(2, g.inStream.currentOffset);
            assertEquals(a, g.updateBlockX);
            assertEquals(b, g.updateBlockY);
        }
    }

    private static Class30_Sub1 entry(Game g, int a, int b, int c, int d) {
        Class30_Sub1 e = new Class30_Sub1();
        e.tileX = a; e.tileY = b; e.scenePlane = c; e.restoreDelayTicks = d;
        g.pendingSceneChanges.insertHead(e);
        return e;
    }

    private static void rectangle(int a, int b) throws Exception {
        ObservedGame g = new ObservedGame();
        g.plane = 2;
        NodeList sentinel = new NodeList();
        for (int p = 0; p < 4; p++) for (int x = 0; x < 104; x++) for (int y = 0; y < 104; y++) {
            g.groundArray[p][x][y] = sentinel;
        }
        List<Class30_Sub1> changed = new ArrayList<Class30_Sub1>();
        for (int x = a; x < a + 8; x++) for (int y = b; y < b + 8; y++) {
            changed.add(entry(g, x, y, 2, 731));
        }
        // Different objects at the same coordinate must each be visited.
        changed.add(entry(g, a + 7, b + 7, 2, -1));
        Class30_Sub1[] untouched = {
            entry(g, a - 1, b, 2, 91), entry(g, a + 8, b, 2, 91),
            entry(g, a, b - 1, 2, 91), entry(g, a, b + 8, 2, 91),
            entry(g, a, b, 1, 91), entry(g, a + 7, b + 7, 3, 91)
        };
        deliver(g, a, b);
        assertEquals(64, g.visits.size());
        for (int n = 0; n < 64; n++) assertEquals((a + n / 8) * 104 + b + n % 8, (int) g.visits.get(n));
        for (int p = 0; p < 4; p++) for (int x = 0; x < 104; x++) for (int y = 0; y < 104; y++) {
            boolean cleared = p == 2 && x >= a && x < a + 8 && y >= b && y < b + 8;
            if (cleared) assertNull(g.groundArray[p][x][y]);
            else assertSame(sentinel, g.groundArray[p][x][y]);
        }
        for (Class30_Sub1 e : changed) assertEquals(0, e.restoreDelayTicks);
        for (Class30_Sub1 e : untouched) assertEquals(91, e.restoreDelayTicks);
        int count = 0;
        for (Node e = g.pendingSceneChanges.firstFromTail(); e != null; e = g.pendingSceneChanges.nextTowardHead()) count++;
        assertEquals(changed.size() + untouched.length, count);
    }

    @Test public void asymmetricOriginAndIndependentAxes() throws Exception { rectangle(17, 39); rectangle(18, 39); rectangle(17, 40); }
    @Test public void firstAndLastValidOrigins() throws Exception { rectangle(0, 0); rectangle(96, 96); }
    @Test public void sparseCellsOnlyNotifyAfterClearingAndEmptyListIsValid() throws Exception {
        ObservedGame g = new ObservedGame(); g.plane = 1;
        g.groundArray[1][23][41] = new NodeList();
        g.groundArray[1][30][48] = new NodeList();
        deliver(g, 23, 41);
        assertEquals(java.util.Arrays.asList(23 * 104 + 41, 30 * 104 + 48), g.visits);
        g.visits.clear(); deliver(g, 23, 41); assertTrue(g.visits.isEmpty());
    }
}
