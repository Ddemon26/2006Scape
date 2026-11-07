import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Region-change packets (opcodes 73 and 241) of Game.parsePacket. Original member names only. The packet bytes are
 * fed through a real loopback RSSocket; every local is observed through the data it routes.
 */
public class RegionPacketParserTest {
    private final List<Socket> sockets = new ArrayList<Socket>();
    private ServerSocket listener;

    @After public void tearDown() throws Exception {
        for (Socket s : sockets) try { s.close(); } catch (Exception e) { }
        if (listener != null) listener.close();
    }

    private static Field field(Class<?> c, String n) throws Exception {
        Field f = c.getDeclaredField(n); f.setAccessible(true); return f;
    }
    private static sun.misc.Unsafe unsafe() throws Exception {
        return (sun.misc.Unsafe) field(sun.misc.Unsafe.class, "theUnsafe").get(null);
    }
    private static int key(int rx, int ry) { return (rx << 8) + ry; }

    // ------------------------------------------------ fixtures

    /** Map index knowledge: {regionX, regionY, terrainId, objectId} rows. */
    private Game game(int[][] map) throws Exception {
        Game g = new Game();
        g.inStream = new Stream(new byte[6000]);
        g.groundArray = new NodeList[4][104][104];
        g.pendingSceneChanges = new NodeList();
        g.anInt1069 = 1; g.anInt1070 = 1;
        OnDemandFetcher f = (OnDemandFetcher) unsafe().allocateInstance(OnDemandFetcher.class);
        int[][] versions = new int[4][]; versions[3] = new int[70000]; Arrays.fill(versions[3], 1);
        for (int k = 0; k < 3; k++) versions[k] = new int[1];
        field(OnDemandFetcher.class, "versions").set(f, versions);
        java.lang.reflect.Constructor<NodeSubList> c = NodeSubList.class.getDeclaredConstructor();
        c.setAccessible(true);
        field(OnDemandFetcher.class, "nodeSubList").set(f, c.newInstance());
        field(OnDemandFetcher.class, "aClass19_1370").set(f, new NodeList());
        int[] keys = new int[map.length], t = new int[map.length], o = new int[map.length];
        for (int i = 0; i < map.length; i++) { keys[i] = key(map[i][0], map[i][1]); t[i] = map[i][2]; o[i] = map[i][3]; }
        field(OnDemandFetcher.class, "mapRegionKeys").set(f, keys);
        field(OnDemandFetcher.class, "mapTerrainFileIds").set(f, t);
        field(OnDemandFetcher.class, "mapObjectFileIds").set(f, o);
        g.onDemandFetcher = f;
        return g;
    }
    private static List<String> requested(Game g) throws Exception {
        NodeSubList q = (NodeSubList) field(OnDemandFetcher.class, "nodeSubList").get(g.onDemandFetcher);
        List<String> ids = new ArrayList<String>();
        for (OnDemandData d = (OnDemandData) q.firstFromTail(); d != null; d = (OnDemandData) q.nextTowardHead())
            ids.add(d.dataType + ":" + d.ID);
        Collections.sort(ids);
        return ids;
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

    private static byte[] packet73(int x, int y) {
        return new byte[] {(byte) (x >> 8), (byte) ((x & 0xff) + 128), (byte) (y >> 8), (byte) y};
    }
    private static int template(int plane, int chunkX, int chunkY, int rotation) {
        return plane << 24 | chunkX << 14 | chunkY << 3 | rotation << 1;
    }
    private static byte[] packet241(int y, int[][][] t, int x) {
        java.io.ByteArrayOutputStream o = new java.io.ByteArrayOutputStream();
        o.write(y >> 8); o.write((y & 0xff) + 128);
        long acc = 0; int bits = 0;
        for (int p = 0; p < 4; p++) for (int cx = 0; cx < 13; cx++) for (int cy = 0; cy < 13; cy++) {
            int v = t[p][cx][cy];
            int n = v == -1 ? 1 : 27;
            long val = v == -1 ? 0 : (1L << 26) | v;
            acc = (acc << n | val) & 0xffffffffffL; bits += n;
            while (bits >= 8) { o.write((int) (acc >> (bits - 8)) & 0xff); bits -= 8; }
        }
        if (bits > 0) o.write((int) (acc << (8 - bits)) & 0xff);
        o.write(x >> 8); o.write(x & 0xff);
        return o.toByteArray();
    }
    private static int[][][] noTemplates() {
        int[][][] t = new int[4][13][13];
        for (int[][] p : t) for (int[] r : p) Arrays.fill(r, -1);
        return t;
    }
    private static final int[][] MAP = {
        {49, 87, 1000, 2000}, {50, 87, 1001, 2001}, {51, 87, 1002, 2002},
        {49, 88, 1003, 2003}, {50, 88, 1004, 2004},          // (51,88) is absent from the map index
        {87, 49, 1900, 2900},                                  // swapped-axis decoy for (49,87)
    };

    // ------------------------------------------------ centre (l2, i11) and window (opcode 73)

    @Test public void opcode73ReadsXThenYAndDerivesTheBaseFromEach() throws Exception {
        Game g = game(MAP);
        deliver(g, 73, packet73(405, 702));
        assertEquals(405, g.anInt1069); assertEquals(702, g.anInt1070);
        assertEquals((405 - 6) * 8, g.baseX); assertEquals((702 - 6) * 8, g.baseY);
        assertEquals(1, g.loadingStage);
        assertFalse(g.aBoolean1159);
    }

    @Test public void sameCentreIsIgnoredOnlyWhenBothCoordinatesMatchAndStageIsTwo() throws Exception {
        // {packet x, packet y, loadingStage, ignored?}
        for (int[] c : new int[][] {{405, 702, 2, 1}, {405, 703, 2, 0}, {406, 702, 2, 0}, {405, 702, 1, 0}, {405, 702, 0, 0}}) {
            Game g = game(MAP);
            g.anInt1069 = 405; g.anInt1070 = 702; g.loadingStage = c[2];
            g.baseX = 7; g.baseY = 9; g.regionKeys = new int[] {-5};
            deliver(g, 73, packet73(c[0], c[1]));
            boolean ignored = c[3] == 1;
            assertEquals(Arrays.toString(c), ignored, g.baseX == 7 && g.baseY == 9);
            assertEquals(Arrays.toString(c), ignored, g.regionKeys.length == 1 && g.regionKeys[0] == -5);
            assertEquals(Arrays.toString(c), ignored ? 2 : 1, g.loadingStage == 2 ? 2 : 1);
        }
    }

    @Test public void opcode73BuildsOneSlotPerRegionOfTheWindowXOuterYInner() throws Exception {
        Game g = game(MAP);
        deliver(g, 73, packet73(405, 702));      // x regions 49..51 (399/8..411/8), y regions 87..88 (696/8..708/8)
        assertArrayEquals(new int[] {key(49, 87), key(49, 88), key(50, 87), key(50, 88), key(51, 87), key(51, 88)}, g.regionKeys);
        assertArrayEquals(new int[] {1000, 1003, 1001, 1004, 1002, -1}, g.regionTerrainFileIds);
        assertArrayEquals(new int[] {2000, 2003, 2001, 2004, 2002, -1}, g.regionObjectFileIds);
        assertEquals(6, g.regionTerrainData.length); assertEquals(6, g.regionObjectData.length);
        assertEquals(Arrays.asList("3:1000", "3:1001", "3:1002", "3:1003", "3:1004", "3:2000", "3:2001", "3:2002", "3:2003", "3:2004"),
                requested(g));
    }

    @Test public void windowCountsAreIndependentPerAxis() throws Exception {
        Game g = game(MAP);
        deliver(g, 73, packet73(402, 702));      // x 396/8=49 .. 408/8=51 (3), y 87..88 (2)
        assertEquals(6, g.regionKeys.length);
        g = game(MAP);
        deliver(g, 73, packet73(405, 700));      // x 49..51 (3), y 694/8=86 .. 706/8=88 (3)
        assertEquals(9, g.regionKeys.length);
        g = game(MAP);
        deliver(g, 73, packet73(409, 702));      // x 403/8=50 .. 415/8=51 (2), y 87..88 (2)
        assertEquals(4, g.regionKeys.length);
        g = game(MAP);
        deliver(g, 73, packet73(409, 700));      // x 50..51 (2), y 86..88 (3)
        assertEquals(6, g.regionKeys.length);
    }

    @Test public void smallWorldFlagSkipsTheBorderRegionsWithoutRequestingThem() throws Exception {
        int[][] map = {{48, 48, 500, 600}, {49, 48, 501, 601}, {48, 49, 502, 602}, {49, 49, 503, 603}};
        Game g = game(map);
        deliver(g, 73, packet73(390, 390));      // chunk 390/8 = 48 on both axes -> small world
        assertTrue(g.aBoolean1141);
        assertArrayEquals(new int[] {key(48, 48), key(48, 49), key(49, 48), key(49, 49)}, g.regionKeys);
        assertArrayEquals(new int[] {500, -1, 501, -1}, g.regionTerrainFileIds);
        assertArrayEquals(new int[] {600, -1, 601, -1}, g.regionObjectFileIds);
        assertEquals(Arrays.asList("3:500", "3:501", "3:600", "3:601"), requested(g));
    }

    // ------------------------------------------------ constructed scene (opcode 241)

    @Test public void opcode241ReadsYThenTemplateBitsThenXAndStoresPlaneChunkXChunkYTemplates() throws Exception {
        int[][][] t = noTemplates();
        t[2][4][7] = template(1, 401, 702, 3);
        t[0][12][0] = template(3, 1023, 2047, 0);
        t[3][0][12] = template(0, 0, 1, 2);
        t[1][5][5] = 0;
        Game g = game(MAP);
        deliver(g, 241, packet241(702, t, 405));
        assertEquals(702, g.anInt1070); assertEquals(405, g.anInt1069);
        assertTrue(g.aBoolean1159);
        for (int p = 0; p < 4; p++) for (int x = 0; x < 13; x++) for (int y = 0; y < 13; y++)
            assertEquals(p + "," + x + "," + y, t[p][x][y], g.anIntArrayArrayArray1129[p][x][y]);
    }

    @Test public void opcode241ListsEachSourceRegionOnceInScanOrder() throws Exception {
        int[][][] t = noTemplates();
        t[0][0][9] = template(3, 409, 700, 2);   // region (51,87)
        t[0][5][0] = template(0, 401, 702, 0);   // region (50,87)
        t[1][0][0] = template(1, 403, 705, 1);   // region (50,88)
        t[2][3][3] = template(0, 400, 703, 0);   // duplicate of (50,87)
        t[3][12][12] = template(2, 404, 705, 0); // duplicate of the LAST listed (50,88)
        Game g = game(MAP);
        deliver(g, 241, packet241(702, t, 405));
        assertArrayEquals(new int[] {key(51, 87), key(50, 87), key(50, 88)}, g.regionKeys);
        assertArrayEquals(new int[] {1002, 1001, 1004}, g.regionTerrainFileIds);
        assertArrayEquals(new int[] {2002, 2001, 2004}, g.regionObjectFileIds);
        assertEquals(3, g.regionTerrainData.length); assertEquals(3, g.regionObjectData.length);
        assertEquals(Arrays.asList("3:1001", "3:1002", "3:1004", "3:2001", "3:2002", "3:2004"), requested(g));
    }

    @Test public void opcode241RegionAxesAreNotSwappedAndMissingFilesAreNotRequested() throws Exception {
        int[][][] t = noTemplates();
        t[0][0][0] = template(0, 49 * 8, 87 * 8, 0);   // (49,87): terrain 1000, decoy (87,49) must not match
        t[0][0][1] = template(0, 51 * 8, 88 * 8, 0);   // (51,88): absent from the index
        Game g = game(MAP);
        deliver(g, 241, packet241(702, t, 405));
        assertArrayEquals(new int[] {key(49, 87), key(51, 88)}, g.regionKeys);
        assertArrayEquals(new int[] {1000, -1}, g.regionTerrainFileIds);
        assertArrayEquals(new int[] {2000, -1}, g.regionObjectFileIds);
        assertEquals(Arrays.asList("3:1000", "3:2000"), requested(g));
    }

    @Test public void opcode241WithNoTemplatesLoadsNoRegions() throws Exception {
        Game g = game(MAP);
        deliver(g, 241, packet241(702, noTemplates(), 405));
        assertEquals(0, g.regionKeys.length);
        assertEquals(0, requested(g).size());
    }

    // ------------------------------------------------ base change shift (i17, j21 and their loops)

    private static final int[] NEW_BASE = {(405 - 6) * 8, (702 - 6) * 8};

    private Game shifted(int dx, int dy) throws Exception {
        Game g = game(MAP);
        g.anInt1036 = NEW_BASE[0] - dx; g.anInt1037 = NEW_BASE[1] - dy;
        return g;
    }

    @Test public void entitiesShiftByWholeTilesAndTimes128ForEveryHistoryEntry() throws Exception {
        for (int op : new int[] {73, 241}) {
            Game g = shifted(16, -8);
            Entity[] all = {new NPC(), new NPC(), new Player(), new Player()};
            for (Entity e : all) {
                for (int i = 0; i < 10; i++) { e.smallX[i] = 100 + i; e.smallY[i] = 200 + 3 * i; }
                e.x = 5000; e.y = 7000;
            }
            g.npcArray[16383] = (NPC) all[0]; g.npcArray[0] = (NPC) all[1];
            g.playerArray[0] = (Player) all[2]; g.playerArray[g.maxPlayers - 1] = (Player) all[3];
            g.destX = 40; g.destY = 50;
            deliver(g, op, op == 73 ? packet73(405, 702) : packet241(702, noTemplates(), 405));
            for (Entity e : all) {
                for (int i = 0; i < 10; i++) {
                    assertEquals(op + " x" + i, 100 + i - 16, e.smallX[i]);
                    assertEquals(op + " y" + i, 200 + 3 * i + 8, e.smallY[i]);
                }
                assertEquals(5000 - 16 * 128, e.x); assertEquals(7000 + 8 * 128, e.y);
            }
            assertEquals(NEW_BASE[0], g.anInt1036); assertEquals(NEW_BASE[1], g.anInt1037);
            assertEquals(24, g.destX); assertEquals(58, g.destY);
            assertTrue(g.aBoolean1080); assertFalse(g.aBoolean1160);
        }
    }

    @Test public void destinationIsOnlyShiftedWhenDestXIsNonZero() throws Exception {
        Game g = shifted(3, 2);
        g.destX = 0; g.destY = 50;
        deliver(g, 73, packet73(405, 702));
        assertEquals(0, g.destX); assertEquals(50, g.destY);
    }

    @Test public void groundItemListsMoveByTheDeltaInAllFourDirections() throws Exception {
        for (int[] d : new int[][] {{3, 2}, {-3, -2}, {3, -2}, {-3, 2}, {0, 5}, {5, 0}, {0, 0}, {104, 1}}) {
            Game g = shifted(d[0], d[1]);
            NodeList[][][] before = new NodeList[4][104][104];
            for (int p = 0; p < 4; p++) for (int x = 0; x < 104; x++) for (int y = 0; y < 104; y++)
                before[p][x][y] = g.groundArray[p][x][y] = new NodeList();
            deliver(g, 73, packet73(405, 702));
            for (int p = 0; p < 4; p++) for (int x = 0; x < 104; x++) for (int y = 0; y < 104; y++) {
                int sx = x + d[0], sy = y + d[1];
                boolean in = sx >= 0 && sy >= 0 && sx < 104 && sy < 104;
                assertSame(Arrays.toString(d) + " " + p + "," + x + "," + y, in ? before[p][sx][sy] : null, g.groundArray[p][x][y]);
            }
        }
    }

    @Test public void pendingSceneChangesAreShiftedAndDroppedOutsideTheScene() throws Exception {
        for (int[] d : new int[][] {{3, 2}, {-3, -2}}) {
            Game g = shifted(d[0], d[1]);
            int[][] tiles = {{3, 2}, {2, 2}, {2, 3}, {100, 100}, {101, 101}, {104 + d[0] - 1, 50}, {50, 104 + d[1] - 1}, {50, 104 + d[1]}, {104 + d[0], 50}, {10, 20}};
            List<Class30_Sub1> nodes = new ArrayList<Class30_Sub1>();
            for (int[] t : tiles) {
                Class30_Sub1 n = new Class30_Sub1(); n.tileX = t[0]; n.tileY = t[1];
                g.pendingSceneChanges.insertHead(n); nodes.add(n);
            }
            deliver(g, 73, packet73(405, 702));
            int kept = 0;
            for (int i = 0; i < tiles.length; i++) {
                int nx = tiles[i][0] - d[0], ny = tiles[i][1] - d[1];
                boolean keep = nx >= 0 && ny >= 0 && nx < 104 && ny < 104;
                assertEquals(Arrays.toString(d) + " " + Arrays.toString(tiles[i]), nx, nodes.get(i).tileX);
                assertEquals(ny, nodes.get(i).tileY);
                assertEquals(Arrays.toString(d) + " " + Arrays.toString(tiles[i]) + " linked", keep, nodes.get(i).next != null);
                if (keep) kept++;
            }
            int count = 0;
            for (Class30_Sub1 n = (Class30_Sub1) g.pendingSceneChanges.firstFromTail(); n != null; n = (Class30_Sub1) g.pendingSceneChanges.nextTowardHead()) count++;
            assertEquals(kept, count);
        }
    }
}
