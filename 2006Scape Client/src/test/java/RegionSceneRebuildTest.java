import java.io.ByteArrayOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Whole-scene rebuild routine of Game (rebuildScene): reset loops, terrain/object region loops, the constructed-scene
 * (aBoolean1159) chunk loops, the ground item pass, the low-memory model unload and the map-file prefetch ring.
 * Original member names only. Locals are observed through the data each one routes.
 */
public class RegionSceneRebuildTest {
    private static final int RX = 50, RY = 100;          // region 50,100
    private ObjectDef[] savedCache;
    private Animation[] savedAnims;
    private boolean savedGameLowMem, savedObjLowMem, savedDefLowMem, savedHideRoofs;
    private Object savedHeaders, savedRequester;
    private java.io.RandomAccessFile savedCacheDat;

    private static Field field(Class<?> c, String n) throws Exception {
        Field f = c.getDeclaredField(n); f.setAccessible(true); return f;
    }
    private static sun.misc.Unsafe unsafe() throws Exception {
        return (sun.misc.Unsafe) field(sun.misc.Unsafe.class, "theUnsafe").get(null);
    }

    @Before public void setUp() throws Exception {
        savedCache = (ObjectDef[]) field(ObjectDef.class, "cache").get(null);
        savedAnims = Animation.anims;
        savedGameLowMem = Game.lowMem;
        savedObjLowMem = ObjectManager.lowMem;
        savedDefLowMem = ObjectDef.lowMem;
        savedHideRoofs = ObjectManager.hideRoofs;
        savedHeaders = field(Model.class, "modelHeaders").get(null);
        savedRequester = field(Model.class, "modelRequester").get(null);
        savedCacheDat = Signlink.cache_dat;
        Game.lowMem = false; ObjectManager.lowMem = false; ObjectDef.lowMem = false; ObjectManager.hideRoofs = false;
        Animation[] anims = new Animation[6];
        Animation a = (Animation) unsafe().allocateInstance(Animation.class);
        a.loopBackFrames = -1; a.frameCount = 1; anims[3] = a;
        Animation.anims = anims;
        install(animated(604), animated(605));
    }
    @After public void tearDown() throws Exception {
        field(ObjectDef.class, "cache").set(null, savedCache);
        Animation.anims = savedAnims;
        Game.lowMem = savedGameLowMem; ObjectManager.lowMem = savedObjLowMem;
        ObjectDef.lowMem = savedDefLowMem; ObjectManager.hideRoofs = savedHideRoofs;
        field(Model.class, "modelHeaders").set(null, savedHeaders);
        field(Model.class, "modelRequester").set(null, savedRequester);
        Signlink.cache_dat = savedCacheDat;
    }

    // ------------------------------------------------ fixtures

    private static ObjectDef newDef(int type) throws Exception {
        Constructor<ObjectDef> c = ObjectDef.class.getDeclaredConstructor();
        c.setAccessible(true);
        ObjectDef d = c.newInstance();
        Method m = ObjectDef.class.getDeclaredMethod("setDefaults");
        m.setAccessible(true); m.invoke(d);
        d.type = type; d.blocksMovement = false;
        return d;
    }
    private static ObjectDef animated(int type) throws Exception {
        ObjectDef d = newDef(type); d.animationId = 3; return d;
    }
    private static void install(ObjectDef... defs) throws Exception {
        ObjectDef[] cache = new ObjectDef[20];
        for (int n = 0; n < 20; n++) cache[n] = n < defs.length ? defs[n] : newDef(-1);
        field(ObjectDef.class, "cache").set(null, cache);
    }

    private static class Probe extends Game {
        final List<int[]> spawned = new ArrayList<int[]>();
        @Override public void spawnGroundItem(int i, int j) { spawned.add(new int[] {i, j}); }
    }

    /** Scene with `slots` region slots (all keys RX,RY, no data), baseX/baseY putting region 50,100 at (20,30). */
    private Probe game(int slots) {
        Probe g = new Probe();
        g.byteGroundArray = new byte[4][104][104];
        g.intGroundArray = new int[4][105][105];
        g.groundArray = new NodeList[4][104][104];
        for (int p = 0; p < 4; p++) g.aClass11Array1230[p] = new CollisionMap();
        g.worldController = new WorldController(g.intGroundArray);
        g.baseX = RX * 64 - 20; g.baseY = RY * 64 - 30;
        g.anInt1069 = 402; g.anInt1070 = 700;
        g.regionTerrainData = new byte[slots][]; g.regionObjectData = new byte[slots][];
        g.regionKeys = new int[slots];
        Arrays.fill(g.regionKeys, (RX << 8) | RY);
        g.stream = new Stream(new byte[64]);
        g.stream.encryption = new ISAACRandomGen(new int[4]);
        try { fetcher(g, 1, new byte[] {0}, new int[0], new int[0], new int[0]); }
        catch (Exception e) { throw new AssertionError(e); }
        return g;
    }

    private static byte[] terrain(Map<String, int[]> tiles, int[] standard) {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        for (int p = 0; p < 4; p++)
            for (int x = 0; x < 64; x++)
                for (int y = 0; y < 64; y++) {
                    int[] bytes = tiles.get(p + "," + x + "," + y);
                    if (bytes == null) bytes = standard;
                    for (int b : bytes) o.write(b);
                }
        return o.toByteArray();
    }
    private static final int[] EXPLICIT3 = {1, 3};      // explicit height 3 -> -24 on plane 0
    private static final int[] END = {0};

    private static void smart(ByteArrayOutputStream out, int v) {
        if (v < 128) out.write(v); else { out.write(0x80 | v >> 8); out.write(v & 0xff); }
    }
    /** Object stream: one id with positions (plane, x, y). */
    private static byte[] objects(int id, int[]... positions) {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        smart(o, id + 1);
        int last = 0;
        for (int[] p : positions) {
            int packed = p[0] << 12 | p[1] << 6 | p[2];
            smart(o, packed - last + 1); last = packed;
            o.write(10 << 2);
        }
        smart(o, 0); smart(o, 0);
        return o.toByteArray();
    }
    private static Ground[][][] grounds(WorldController w) throws Exception {
        return (Ground[][][]) field(WorldController.class, "groundArray").get(w);
    }
    private static boolean placed(WorldController w, int plane, int x, int y) throws Exception {
        Ground g = grounds(w)[plane][x][y];
        return g != null && g.obj5Array[0] != null;
    }
    private static int placedCount(WorldController w) throws Exception {
        int n = 0;
        for (int p = 0; p < 4; p++) for (int x = 0; x < 104; x++) for (int y = 0; y < 104; y++)
            if (placed(w, p, x, y)) n++;
        return n;
    }
    private static int flagCount(Game g) {
        int n = 0;
        for (int p = 0; p < 4; p++) for (int x = 0; x < 104; x++) for (int y = 0; y < 104; y++)
            if (g.byteGroundArray[p][x][y] != 0) n++;
        return n;
    }
    private static int template(int plane, int chunkX, int chunkY, int rotation) {
        return plane << 24 | chunkX << 14 | chunkY << 3 | rotation << 1;
    }
    private static int key(int rx, int ry) { return (rx << 8) | ry; }
    private static void fillTemplates(Game g, int value) {
        for (int p = 0; p < 4; p++) for (int x = 0; x < 13; x++) Arrays.fill(g.anIntArrayArrayArray1129[p][x], value);
    }

    // ------------------------------------------------ reset loops (i, l, k1, j2)

    @Test public void resetLoopsClearAllFourFlagPlanesAndRebuildAllFourCollisionMaps() {
        Probe g = game(0);
        for (int p = 0; p < 4; p++) {
            for (byte[] column : g.byteGroundArray[p]) Arrays.fill(column, (byte) 5);
            for (int[] column : g.aClass11Array1230[p].tileFlags) Arrays.fill(column, 7);
        }
        int before = g.anInt1051;
        g.rebuildScene();
        assertEquals("routine completed", before + 1, g.anInt1051);
        assertEquals(0, flagCount(g));
        for (int p = 0; p < 4; p++) {
            int[][] f = g.aClass11Array1230[p].tileFlags;
            assertEquals("plane " + p + " edge", 0xffffff, f[0][50]);
            assertEquals("plane " + p + " edge", 0xffffff, f[50][103]);
            assertEquals("plane " + p + " interior", 0x1000000, f[50][50]);
        }
    }

    // ------------------------------------------------ terrain region loop (k2, i3, i4, k5, abyte0)

    @Test public void terrainLoopPlacesEachSlotsDataAtItsOwnKeyOffsetsOverRegionTerrainDataLength() {
        Probe g = game(3);
        // slot keys: only slot 1 has data; it sits at region 50,100 => scene offset (20, 30)
        g.regionKeys[0] = key(RX + 1, RY); g.regionKeys[2] = key(RX, RY + 1);
        Map<String, int[]> t = new HashMap<String, int[]>();
        t.put("1,5,9", new int[] {50, 0});   // flag 1 on plane 1, region tile x=5, y=9
        g.regionTerrainData[1] = terrain(t, END);
        g.rebuildScene();
        assertEquals(1, g.byteGroundArray[1][20 + 5][30 + 9]);
        assertEquals("only one tile flagged", 1, flagCount(g));
        assertEquals("x offset uses the key high byte", 0, g.byteGroundArray[1][30 + 5][20 + 9]);
    }

    @Test public void terrainLoopOnlyVisitsAsManySlotsAsRegionTerrainDataHas() {
        Probe g = game(3);
        Map<String, int[]> t = new HashMap<String, int[]>();
        t.put("1,5,9", new int[] {50, 0});
        g.regionTerrainData = new byte[1][];      // loop bound is this array's length
        g.regionTerrainData[0] = terrain(t, END);
        g.rebuildScene();
        assertEquals(1, flagCount(g));
        g.regionTerrainData = new byte[2][];
        g.regionObjectData = new byte[3][];
        g.regionTerrainData[1] = terrain(t, END);  // second slot only is non-null
        g.byteGroundArray = new byte[4][104][104];
        g.rebuildScene();
        assertEquals(1, g.byteGroundArray[1][25][39]);
    }

    // ------------------------------------------------ blank loop (j4, l5, k7, abyte2)

    @Test public void blankLoopSmoothsTheEdgesOfEverySlotWithoutTerrainDataAtItsOwnOffsets() {
        Probe g = game(2);
        g.regionKeys[1] = key(RX, RY + 1);   // scene offset (20, 94) -> mostly outside the 104 scene
        g.regionKeys[0] = key(RX, RY);       // slot 0 has no data: blanked at x 20..84, y 30..94
        // slot 1 carries data: it is not blanked
        g.regionTerrainData[1] = terrain(new HashMap<String, int[]>(), EXPLICIT3);
        for (int x = 0; x < 105; x++) for (int y = 0; y < 105; y++) g.intGroundArray[0][x][y] = 1000 * x + y + 1;
        g.rebuildScene();
        int[][] h = g.intGroundArray[0];
        assertEquals("start x edge copies x-1", h[19][50], h[20][50]);
        assertEquals("start y edge copies y-1", h[40][29], h[40][30]);
        assertEquals("end x edge copies x+1", h[85][50], h[84][50]);
        assertEquals("interior untouched", 1000 * 40 + 50 + 1, h[40][50]);
        assertEquals("outside untouched", 1000 * 10 + 50 + 1, h[10][50]);
        // data slot (offset 20,94): explicit height rows
        assertEquals(-24, h[40][100]);
    }

    @Test public void blankLoopIsSkippedFromRowNumber800() {
        for (int limit : new int[] {799, 800}) {
            Probe g = game(1);
            g.anInt1070 = limit;
            for (int x = 0; x < 105; x++) for (int y = 0; y < 105; y++) g.intGroundArray[0][x][y] = 1000 * x + y + 1;
            g.rebuildScene();
            boolean blanked = g.intGroundArray[0][20][50] == g.intGroundArray[0][19][50];
            assertEquals("anInt1070=" + limit, limit < 800, blanked);
        }
    }

    // ------------------------------------------------ object region loop (i6, abyte1, l8, k9)

    @Test public void objectLoopPlacesEachSlotsObjectsAtItsKeyOffsets() throws Exception {
        Probe g = game(3);
        g.regionKeys[0] = key(RX + 1, RY); g.regionKeys[1] = key(RX, RY + 1);
        g.regionObjectData[2] = objects(604, new int[] {1, 3, 5});   // slot 2 -> region 50,100
        g.rebuildScene();
        assertTrue(placed(g.worldController, 1, 20 + 3, 30 + 5));
        assertEquals(1, placedCount(g.worldController));
    }

    // ------------------------------------------------ constructed scene (j3, k4, j6, l7, i9, l9, j10, l10, j11, l11)

    private Probe instanceGame() {
        Probe g = game(3);
        g.aBoolean1159 = true;
        fillTemplates(g, -1);
        g.regionKeys[0] = key(RX + 1, RY); g.regionKeys[1] = key(RX, RY); g.regionKeys[2] = key(RX, RY + 1);
        return g;
    }

    @Test public void terrainChunkLoopCopiesTheTemplateSourceTileToTheDestinationChunk() {
        Probe g = instanceGame();
        fillTemplates(g, template(0, 7 * 8, 9 * 8, 0));   // every other chunk points at an unloaded region
        g.anIntArrayArrayArray1129[2][4][7] = template(1, RX * 8 + 3, RY * 8 + 5, 0);
        Map<String, int[]> a = new HashMap<String, int[]>(), b = new HashMap<String, int[]>();
        a.put("1,26,43", new int[] {50, 0});  // chunk-local tile (2,3) inside source chunk (3,5)
        a.put("1,2,3", new int[] {50, 0});    // wrong chunk, must not be copied
        a.put("0,26,43", new int[] {50, 0});  // wrong source plane
        b.put("1,26,43", new int[] {50, 0});  // right data but belongs to slot 0 (another key)
        g.regionTerrainData[1] = terrain(a, END);
        g.regionTerrainData[0] = terrain(b, END);
        g.rebuildScene();
        assertEquals(1, g.byteGroundArray[2][32 + 2][56 + 3]);
        assertEquals(1, flagCount(g));
    }

    @Test public void terrainChunkRotationBitsSelectTheRotation() {
        int[][] expected = {{34, 59}, {-1, -1}, {37, 60}, {-1, -1}};
        for (int rotation : new int[] {0, 2}) {
            Probe g = instanceGame();
            g.anIntArrayArrayArray1129[2][4][7] = template(1, RX * 8 + 3, RY * 8 + 5, rotation);
            Map<String, int[]> a = new HashMap<String, int[]>();
            a.put("1,26,43", new int[] {50, 0});
            g.regionTerrainData[1] = terrain(a, END);
            g.rebuildScene();
            assertEquals("rotation " + rotation, 1, g.byteGroundArray[2][expected[rotation][0]][expected[rotation][1]]);
            assertEquals(1, flagCount(g));
        }
    }

    @Test public void chunkTemplateMinusOneBlanksOnlyThePlaneZeroChunkAtItsOwnCoordinates() {
        Probe g = instanceGame();
        fillTemplates(g, template(0, 7 * 8, 9 * 8, 0));
        g.anIntArrayArrayArray1129[0][4][7] = -1;       // chunkX 4, chunkY 7 -> start x 32, start y 56
        g.anIntArrayArrayArray1129[1][2][3] = -1;       // other planes never blank
        for (int x = 0; x < 105; x++) for (int y = 0; y < 105; y++) g.intGroundArray[0][x][y] = 1000 * x + y + 1;
        g.rebuildScene();
        int[][] h = g.intGroundArray[0];
        assertEquals("start x copies x-1", h[31][60], h[32][60]);
        assertEquals("start y copies y-1", h[35][55], h[35][56]);
        assertEquals("interior untouched", 1000 * 35 + 60 + 1, h[35][60]);
        assertEquals("plane 1 template not blanked", 1000 * 16 + 24 + 1, h[16][24]);
        assertEquals("swapped chunk not blanked", 1000 * 56 + 33 + 1, h[56][33]);
    }

    @Test public void objectChunkLoopPlacesTheTemplateSourceObjectAtTheDestinationChunk() throws Exception {
        Probe g = instanceGame();
        fillTemplates(g, template(0, 7 * 8, 9 * 8, 0));
        g.anIntArrayArrayArray1129[3][6][2] = template(1, RX * 8 + 3, RY * 8 + 5, 0);
        g.regionObjectData[1] = objects(604, new int[] {1, 26, 43}, new int[] {1, 2, 3}, new int[] {0, 26, 43});
        g.regionObjectData[0] = objects(604, new int[] {1, 26, 43});
        g.rebuildScene();
        assertTrue(placed(g.worldController, 3, 48 + 2, 16 + 3));
        assertEquals(1, placedCount(g.worldController));
    }

    @Test public void objectChunkRotationBitsSelectTheRotation() throws Exception {
        Probe g = instanceGame();
        fillTemplates(g, template(0, 7 * 8, 9 * 8, 0));
        g.anIntArrayArrayArray1129[3][6][2] = template(1, RX * 8 + 3, RY * 8 + 5, 2);
        g.regionObjectData[1] = objects(604, new int[] {1, 26, 43});
        g.rebuildScene();
        assertTrue(placed(g.worldController, 3, 48 + 5, 16 + 4));
        assertEquals(1, placedCount(g.worldController));
    }

    // ------------------------------------------------ ground item pass (i5, i7)

    @Test public void groundItemPassVisitsEveryTileColumnMajor() {
        Probe g = game(0);
        g.rebuildScene();
        assertEquals(104 * 104, g.spawned.size());
        for (int n = 0; n < g.spawned.size(); n++) {
            assertEquals(n / 104, g.spawned.get(n)[0]);
            assertEquals(n % 104, g.spawned.get(n)[1]);
        }
    }

    // ------------------------------------------------ low memory model unload (j, i1, l1)

    private void fetcher(Probe g, int modelCount, byte[] modelIndices, int[] regionKeys, int[] terrainIds, int[] objectIds)
            throws Exception {
        OnDemandFetcher f = (OnDemandFetcher) unsafe().allocateInstance(OnDemandFetcher.class);
        field(Game.class, "cacheStores").set(g, new CacheStore[] {(CacheStore) unsafe().allocateInstance(CacheStore.class)});
        field(OnDemandFetcher.class, "clientInstance").set(f, g);
        int[][] versions = new int[4][];
        byte[][] status = new byte[4][];
        versions[0] = new int[modelCount]; status[0] = new byte[modelCount];
        versions[3] = new int[70000]; status[3] = new byte[70000];
        Arrays.fill(versions[3], 1); Arrays.fill(status[3], (byte) 1);
        field(OnDemandFetcher.class, "versions").set(f, versions);
        field(OnDemandFetcher.class, "extraFilePriorities").set(f, status);
        field(OnDemandFetcher.class, "highestPendingPriority").setInt(f, 1);
        field(OnDemandFetcher.class, "extraFileQueue").set(f, new NodeList());
        field(OnDemandFetcher.class, "modelIndices").set(f, modelIndices);
        field(OnDemandFetcher.class, "mapRegionKeys").set(f, regionKeys);
        field(OnDemandFetcher.class, "mapTerrainFileIds").set(f, terrainIds);
        field(OnDemandFetcher.class, "mapObjectFileIds").set(f, objectIds);
        g.onDemandFetcher = f;
    }
    private static List<String> queued(OnDemandFetcher f) throws Exception {
        NodeList queue = (NodeList) field(OnDemandFetcher.class, "extraFileQueue").get(f);
        List<String> ids = new ArrayList<String>();
        for (OnDemandData d = (OnDemandData) queue.firstFromTail(); d != null; d = (OnDemandData) queue.nextTowardHead())
            ids.add(d.dataType + ":" + d.ID);
        Collections.sort(ids);
        return ids;
    }

    private boolean[] unloadedHeaders(boolean lowMem, boolean cacheDat) throws Exception {
        Model.initModelHeaders(64, new OnDemandFetcherParent() { @Override public void requestModel(int id) { } });
        ModelHeader[] headers = (ModelHeader[]) field(Model.class, "modelHeaders").get(null);
        for (int i = 0; i < headers.length; i++) headers[i] = new ModelHeader();
        Probe g = game(0);
        fetcher(g, 6, new byte[] {0, 1, 0x79, 0x78, (byte) 0x86, 0x02, 0, 0}, new int[0], new int[0], new int[0]);
        Game.lowMem = lowMem;
        Signlink.cache_dat = cacheDat ? (java.io.RandomAccessFile) unsafe().allocateInstance(java.io.RandomAccessFile.class) : null;
        g.rebuildScene();
        boolean[] gone = new boolean[8];
        for (int i = 0; i < gone.length; i++) gone[i] = headers[i] == null;
        return gone;
    }

    @Test public void lowMemoryUnloadDropsHeadersWhoseIndexByteHasNoneOfTheMask0x79BitsForEveryVersionedModel() throws Exception {
        boolean[] gone = unloadedHeaders(true, true);
        assertArrayEquals(new boolean[] {true, false, false, false, true, true, false, false}, gone);
    }

    @Test public void lowMemoryUnloadNeedsBothLowMemoryAndAnOpenCacheFile() throws Exception {
        for (boolean gone : unloadedHeaders(false, true)) assertFalse(gone);
        for (boolean gone : unloadedHeaders(true, false)) assertFalse(gone);
    }

    // ------------------------------------------------ prefetch ring (k, j1, i2, l2, l3, j5, j7, k8)

    private Probe prefetchGame(int chunkX, int chunkY, boolean small) throws Exception {
        Probe g = game(0);
        g.anInt1069 = chunkX; g.anInt1070 = chunkY; g.aBoolean1141 = small;
        List<Integer> keys = new ArrayList<Integer>();
        List<Integer> terrain = new ArrayList<Integer>(), obj = new ArrayList<Integer>();
        for (int x = 40; x <= 62; x++) for (int y = 40; y <= 105; y++) {
            if ((x + y) % 7 == 0) continue;     // no files for some regions
            keys.add(key(x, y)); terrain.add(10000 + x * 200 + y); obj.add(40000 + x * 200 + y);
        }
        int[] k = new int[keys.size()], t = new int[keys.size()], o = new int[keys.size()];
        for (int i = 0; i < k.length; i++) { k[i] = keys.get(i); t[i] = terrain.get(i); o[i] = obj.get(i); }
        fetcher(g, 1, new byte[] {0}, k, t, o);
        // a stale request that clearExtraFileQueue must drop
        g.onDemandFetcher.requestExtraFile(12345, 3);
        return g;
    }
    private static List<String> ring(int minX, int maxX, int minY, int maxY) {
        List<String> ids = new ArrayList<String>();
        for (int x = minX; x <= maxX; x++) for (int y = minY; y <= maxY; y++) {
            if (!(x == minX || x == maxX || y == minY || y == maxY)) continue;
            if ((x + y) % 7 == 0) continue;
            ids.add("3:" + (10000 + x * 200 + y)); ids.add("3:" + (40000 + x * 200 + y));
        }
        Collections.sort(ids);
        return ids;
    }

    @Test public void prefetchRequestsTheTerrainAndObjectFilesOfTheRingAroundTheSceneOnlyInTypeThree() throws Exception {
        // anInt1069=402 -> x 48..52, anInt1070=806 -> y 99..102 (chunk -6 over 8, -1) and (+6 over 8, +1)
        Probe g = prefetchGame(402, 806, false);
        g.rebuildScene();
        assertEquals(ring(48, 52, 99, 102), queued(g.onDemandFetcher));
    }

    @Test public void prefetchWindowMovesWithEachChunkCoordinateIndependently() throws Exception {
        Probe g = prefetchGame(450, 806, false);        // (444)/8-1=54, (456)/8+1=58
        g.rebuildScene();
        assertEquals(ring(54, 58, 99, 102), queued(g.onDemandFetcher));
        g = prefetchGame(402, 770, false);              // (764)/8-1=94, (776)/8+1=98
        g.rebuildScene();
        assertEquals(ring(48, 52, 94, 98), queued(g.onDemandFetcher));
    }

    @Test public void smallWorldOverrideForcesTheFixedTwoByTwoWindow() throws Exception {
        Probe g = prefetchGame(402, 806, true);
        g.rebuildScene();
        assertEquals(ring(49, 50, 49, 50), queued(g.onDemandFetcher));
    }
}
