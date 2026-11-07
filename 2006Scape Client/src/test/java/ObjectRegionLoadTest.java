import java.io.ByteArrayOutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Region loading helpers of ObjectManager (method173/174/178/179/180/181/183), the footprint/shadow locals of
 * placeObject and the remaining ObjectDef.buildModel locals. Original member names only.
 */
public class ObjectRegionLoadTest {
    private ObjectDef[] savedCache;
    private Animation[] savedAnims;
    private boolean savedHideRoofs, savedManagerLowMem, savedDefLowMem;

    private static Field field(Class<?> type, String name) throws Exception {
        Field f = type.getDeclaredField(name);
        f.setAccessible(true);
        return f;
    }

    private static sun.misc.Unsafe unsafe() throws Exception {
        return (sun.misc.Unsafe) field(sun.misc.Unsafe.class, "theUnsafe").get(null);
    }

    @Before public void setUp() throws Exception {
        savedCache = (ObjectDef[]) field(ObjectDef.class, "cache").get(null);
        savedAnims = Animation.anims;
        savedHideRoofs = ObjectManager.hideRoofs;
        savedManagerLowMem = ObjectManager.lowMem;
        savedDefLowMem = ObjectDef.lowMem;
        ObjectManager.lowMem = false;
        ObjectDef.lowMem = false;
        ObjectManager.hideRoofs = false;
        Animation[] anims = new Animation[6];
        Animation a = (Animation) unsafe().allocateInstance(Animation.class);
        a.anInt356 = -1;
        a.anInt352 = 1;
        anims[3] = a;
        Animation.anims = anims;
    }

    @After public void tearDown() throws Exception {
        field(ObjectDef.class, "cache").set(null, savedCache);
        Animation.anims = savedAnims;
        ObjectManager.hideRoofs = savedHideRoofs;
        ObjectManager.lowMem = savedManagerLowMem;
        ObjectDef.lowMem = savedDefLowMem;
    }

    // ------------------------------------------------ fixtures

    private static ObjectDef newDef(int type) throws Exception {
        Constructor<ObjectDef> constructor = ObjectDef.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        ObjectDef def = constructor.newInstance();
        Method defaults = ObjectDef.class.getDeclaredMethod("setDefaults");
        defaults.setAccessible(true);
        defaults.invoke(def);
        def.type = type;
        def.blocksMovement = false;
        return def;
    }

    private static void install(ObjectDef... defs) throws Exception {
        ObjectDef[] cache = new ObjectDef[20];
        for (int n = 0; n < 20; n++) {
            cache[n] = n < defs.length ? defs[n] : newDef(-1);
        }
        field(ObjectDef.class, "cache").set(null, cache);
    }

    private static void smart(ByteArrayOutputStream out, int v) {
        if (v < 128) {
            out.write(v);
        } else {
            out.write(0x80 | v >> 8);
            out.write(v & 0xff);
        }
    }

    private static Object call(String name, Object target, Class<?>[] types, Object... args) throws Exception {
        Method m = ObjectManager.class.getDeclaredMethod(name, types);
        m.setAccessible(true);
        return m.invoke(target, args);
    }

    private static int m172(int x, int y) throws Exception {
        return (Integer) call("proceduralHeight", null, new Class<?>[] {int.class, int.class}, x, y);
    }

    private static final Class<?>[] M181 = {int.class, int.class, Stream.class, int.class, int.class, int.class, int.class};

    private static void m181(ObjectManager m, int i, int j, Stream s, int k, int l, int i1, int k1) throws Exception {
        call("readTerrainTile", m, M181, i, j, s, k, l, i1, k1);
    }

    private static byte[][][] f(ObjectManager m, String name) throws Exception {
        return (byte[][][]) field(ObjectManager.class, name).get(m);
    }

    private static int[][][] heights(ObjectManager m) throws Exception {
        return (int[][][]) field(ObjectManager.class, "anIntArrayArrayArray129").get(m);
    }

    private static CollisionMap[] maps() {
        CollisionMap[] maps = new CollisionMap[4];
        for (int p = 0; p < 4; p++) {
            maps[p] = new CollisionMap();
        }
        return maps;
    }

    // ------------------------------------------------ method173 preloads the models of every listed object

    private static OnDemandFetcher fetcher() throws Exception {
        OnDemandFetcher fetcher = (OnDemandFetcher) unsafe().allocateInstance(OnDemandFetcher.class);
        Game game = (Game) unsafe().allocateInstance(Game.class);
        field(Game.class, "cacheStores").set(game, new CacheStore[] {(CacheStore) unsafe().allocateInstance(CacheStore.class)});
        field(OnDemandFetcher.class, "clientInstance").set(fetcher, game);
        int[][] versions = new int[1][70000];
        byte[][] status = new byte[1][70000];
        Arrays.fill(versions[0], 1);
        Arrays.fill(status[0], (byte) 1);
        field(OnDemandFetcher.class, "versions").set(fetcher, versions);
        field(OnDemandFetcher.class, "fileStatus").set(fetcher, status);
        field(OnDemandFetcher.class, "anInt1332").setInt(fetcher, 1);
        field(OnDemandFetcher.class, "aClass19_1344").set(fetcher, new NodeList());
        return fetcher;
    }

    private static List<Integer> requested(OnDemandFetcher fetcher) throws Exception {
        NodeList queue = (NodeList) field(OnDemandFetcher.class, "aClass19_1344").get(fetcher);
        List<Integer> ids = new ArrayList<Integer>();
        for (OnDemandData d = (OnDemandData) queue.firstFromTail(); d != null; d = (OnDemandData) queue.nextTowardHead()) {
            assertEquals(0, d.dataType);
            ids.add(d.ID);
        }
        Collections.sort(ids);
        return ids;
    }

    @Test public void method173RequestsTheModelsOfEachObjectIdAccumulatedFromDeltasAndSkipsItsPositionLists() throws Exception {
        ObjectDef a = newDef(204);
        field(ObjectDef.class, "modelIds").set(a, new int[] {100, 101 + 0x10000});
        ObjectDef b = newDef(206);
        field(ObjectDef.class, "modelIds").set(b, new int[] {300});
        ObjectDef skipped = newDef(205);
        field(ObjectDef.class, "modelIds").set(skipped, new int[] {999});
        install(a, b, skipped);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        smart(out, 205);          // id -1 + 205 = 204
        smart(out, 3); out.write(0x47);   // a position list whose attribute byte must not be read as a delta
        smart(out, 200); out.write(0x09);
        smart(out, 0);
        smart(out, 2);            // id 204 + 2 = 206
        smart(out, 0);
        smart(out, 0);            // end
        byte[] data = out.toByteArray();
        Stream stream = new Stream(data);
        OnDemandFetcher fetcher = fetcher();
        ObjectManager.requestObjectModels(stream, fetcher);
        assertEquals(Arrays.asList(100, 101, 300), requested(fetcher));
        assertEquals("consumed the whole stream", data.length, stream.currentOffset);
    }

    // ------------------------------------------------ method174 blanks a rectangle

    @Test public void method174ShadesTheInclusiveRectangleFullyAndCopiesEachOuterEdgeHeightFromTheNeighbour() throws Exception {
        int[][][] ground = new int[4][105][105];
        for (int x = 0; x < 105; x++) for (int y = 0; y < 105; y++) ground[0][x][y] = x * 1000 + y;
        ObjectManager m = new ObjectManager(new byte[4][105][105], ground);
        // i = lowest y, j = y span, l = x span, i1 = lowest x
        m.blankRegionArea(40, 3, 5, 20);
        byte[][][] shadow = f(m, "objectShadow");
        for (int x = 0; x < 105; x++) {
            for (int y = 0; y < 105; y++) {
                boolean inside = x >= 20 && x <= 25 && y >= 40 && y <= 43;
                assertEquals("shadow " + x + "," + y, inside ? 127 : 0, shadow[0][x][y]);
            }
        }
        assertEquals("low x edge copies x-1", 19 * 1000 + 41, ground[0][20][41]);
        assertEquals("high x edge copies x+1", 26 * 1000 + 41, ground[0][25][41]);
        assertEquals("low y edge copies y-1", 22 * 1000 + 39, ground[0][22][40]);
        assertEquals("high y edge copies y+1", 22 * 1000 + 44, ground[0][22][43]);
        assertEquals("interior untouched", 22 * 1000 + 41, ground[0][22][41]);
        assertEquals("outside untouched", 30 * 1000 + 41, ground[0][30][41]);
        // right at the map border: nothing is written outside 0..103
        m.blankRegionArea(0, 200, 200, 0);
        assertEquals(127, shadow[0][103][103]);
    }

    // ------------------------------------------------ method178 shape normalisation

    @Test public void method178MapsShape11To10AndShapes5To8To4BeforeAskingTheDefinitionIfItsModelsAreLoaded() throws Exception {
        ObjectDef plain = newDef(310);
        field(ObjectDef.class, "modelIds").set(plain, new int[] {5});
        ObjectDef shaped = newDef(311);
        field(ObjectDef.class, "modelIds").set(shaped, new int[] {5});
        field(ObjectDef.class, "modelShapes").set(shaped, new int[] {4});
        install(plain, shaped);
        // with no model headers loaded a model of the asked-for shape is "not loaded"; other shapes are not asked for
        assertFalse("shape 10 asks", ObjectManager.areObjectModelsLoaded(310, 10));
        assertFalse("shape 11 becomes 10", ObjectManager.areObjectModelsLoaded(310, 11));
        assertTrue("shape 9 is not a model shape", ObjectManager.areObjectModelsLoaded(310, 9));
        assertTrue("shape 5 stays 5 here", ObjectManager.areObjectModelsLoaded(310, 5));
        assertFalse("shape 4 matches modelShapes", ObjectManager.areObjectModelsLoaded(311, 4));
        for (int shape = 5; shape <= 8; shape++) {
            assertFalse("shape " + shape + " becomes 4", ObjectManager.areObjectModelsLoaded(311, shape));
        }
        assertTrue("shape 9 matches nothing", ObjectManager.areObjectModelsLoaded(311, 9));
        assertTrue("shape 3 matches nothing", ObjectManager.areObjectModelsLoaded(311, 3));
    }

    // ------------------------------------------------ method181 one terrain tile

    private static Stream bytes(int... values) {
        byte[] b = new byte[values.length];
        for (int n = 0; n < b.length; n++) b[n] = (byte) values[n];
        return new Stream(b);
    }

    @Test public void method181OpcodeZeroOnPlaneZeroUsesNoiseHeightAtTheTileOffsetByKAndJ() throws Exception {
        byte[][][] flags = new byte[4][105][105];
        flags[0][5][7] = 9;
        ObjectManager m = new ObjectManager(flags, new int[4][105][105]);
        // i = y, k = x, j = y noise offset, k1 = x noise offset
        m181(m, 7, 13, bytes(0), 5, 0, 0, 11);
        int expected = -m172(0xe3b7b + 5 + 11, 0x87cce + 7 + 13) * 8;
        int swapped = -m172(0xe3b7b + 5 + 13, 0x87cce + 7 + 11) * 8;
        assertNotEquals("test is able to tell the offsets apart", expected, swapped);
        assertEquals(expected, heights(m)[0][5][7]);
        assertEquals("flags reset", 0, flags[0][5][7]);
    }

    @Test public void method181UpperPlanesSitBelowThePlaneUnderThemAndOpcodeOneIsAnExplicitHeight() throws Exception {
        int[][][] ground = new int[4][105][105];
        ObjectManager m = new ObjectManager(new byte[4][105][105], ground);
        ground[1][5][7] = -100;
        m181(m, 7, 0, bytes(0), 5, 2, 0, 0);
        assertEquals("plane 2 = plane 1 - 240", -340, ground[2][5][7]);
        m181(m, 7, 0, bytes(1, 9), 5, 0, 0, 0);
        assertEquals(-72, ground[0][5][7]);
        m181(m, 7, 0, bytes(1, 1), 6, 0, 0, 0);
        assertEquals("explicit height 1 means 0", 0, ground[0][6][7]);
        ground[0][5][8] = -50;
        m181(m, 8, 0, bytes(1, 9), 5, 1, 0, 0);
        assertEquals("plane 1 = plane 0 - 8*value", -50 - 72, ground[1][5][8]);
    }

    @Test public void method181OpcodesTwoTo49StoreAShapeByteAndOrientationRotatedByI1AndLaterOpcodesStoreFlags() throws Exception {
        byte[][][] flags = new byte[4][105][105];
        ObjectManager m = new ObjectManager(flags, new int[4][105][105]);
        // opcode 15: signed byte -5, (15-2)/4 = 3, orientation (15-2+3)&3 = 0; opcode 60 -> flag 11; opcode 90 -> 9
        m181(m, 7, 0, bytes(15, 0xfb, 60, 90, 0), 5, 0, 3, 0);
        assertEquals(-5, f(m, "aByteArrayArrayArray130")[0][5][7]);
        assertEquals(3, f(m, "aByteArrayArrayArray136")[0][5][7]);
        assertEquals(0, f(m, "aByteArrayArrayArray148")[0][5][7]);
        assertEquals(11, flags[0][5][7]);
        assertEquals(9, f(m, "aByteArrayArrayArray142")[0][5][7]);
        m181(m, 8, 0, bytes(15, 0xfb, 0), 5, 0, 0, 0);
        assertEquals("orientation without rotation", 1, f(m, "aByteArrayArrayArray148")[0][5][8]);
    }

    @Test public void method181OutsideTheMapOnlyConsumesTheTileBytes() throws Exception {
        ObjectManager m = new ObjectManager(new byte[4][105][105], new int[4][105][105]);
        Stream s = bytes(5, 77, 1, 33, 99);
        m181(m, 7, 0, s, 104, 0, 0, 0);
        assertEquals("opcode 5 and 1 take a byte each, stop at 1", 4, s.currentOffset);
        Stream t = bytes(0, 8);
        m181(m, 104, 0, t, 5, 0, 0, 0);
        assertEquals("opcode 0 ends the tile", 1, t.currentOffset);
        Stream u = bytes(60, 0, 9);
        m181(m, 7, 0, u, -1, 0, 0, 0);
        assertEquals("opcodes above 49 take no byte", 2, u.currentOffset);
        assertEquals(0, heights(m)[0][104][7]);
        m181(m, 103, 0, bytes(1, 9), 103, 0, 0, 0);
        assertEquals("103 is inside", -72, heights(m)[0][103][103]);
    }

    // ------------------------------------------------ method180 a whole 64x64x4 region

    @Test public void method180DecodesFourPlanesOf64By64TilesAtXPlusJAndYPlusIWithNoiseOffsetsKAndL() throws Exception {
        int[][][] ground = new int[4][105][105];
        for (int[][] plane : ground) for (int[] column : plane) Arrays.fill(column, 12345);
        ObjectManager m = new ObjectManager(new byte[4][105][105], ground);
        CollisionMap[] maps = maps();
        // i = y base, j = x base, k = x noise offset, l = y noise offset
        m.loadTerrainRegion(new byte[4 * 64 * 64], 3, 2, 800, 640, maps);
        for (int[] tile : new int[][] {{2, 3}, {10, 20}, {65, 66}, {40, 5}}) {
            int x = tile[0], y = tile[1];
            int h0 = -m172(0xe3b7b + x + 800, 0x87cce + y + 640) * 8;
            assertEquals("plane 0 " + x + "," + y, h0, ground[0][x][y]);
            assertEquals(h0 - 240, ground[1][x][y]);
            assertEquals(h0 - 720, ground[3][x][y]);
        }
        assertEquals("below the base untouched", 12345, ground[0][1][3]);
        assertEquals("beyond the region untouched", 12345, ground[0][66][66]);
        assertEquals("region shorter in y", 12345, ground[0][20][2]);
        assertEquals("cleared collision bit inside", 0, maps[2].tileFlags[10][20] & 0x1000000);
        assertEquals("kept outside", 0x1000000, maps[2].tileFlags[1][1] & 0x1000000);
        assertEquals("kept beyond region", 0x1000000, maps[0].tileFlags[70][20] & 0x1000000);
    }

    // ------------------------------------------------ method179 one rotated 8x8 chunk

    private static byte[] chunkRegion(int plane, int srcX, int srcY) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int p = 0; p < 4; p++) {
            for (int x = 0; x < 64; x++) {
                for (int y = 0; y < 64; y++) {
                    if (x >= srcX && x < srcX + 8 && y >= srcY && y < srcY + 8) {
                        out.write(1);
                        out.write(p == plane ? 10 + (x - srcX) * 8 + (y - srcY) : 99);
                    } else {
                        out.write(0);
                    }
                }
            }
        }
        return out.toByteArray();
    }

    @Test public void method179CopiesTheChunkAtI1J1OfPlaneIToPlaneK1RotatedByJAtLAndL1() throws Exception {
        for (int rot = 0; rot < 4; rot++) {
            int[][][] ground = new int[4][105][105];
            for (int[][] plane : ground) for (int[] column : plane) Arrays.fill(column, 12345);
            ObjectManager m = new ObjectManager(new byte[4][105][105], ground);
            CollisionMap[] maps = maps();
            // i = source plane, j = rotation, l = dest x, i1 = source x, j1 = source y, k1 = dest plane, l1 = dest y
            m.loadTerrainChunk(2, rot, maps, 40, 16, chunkRegion(2, 16, 24), 24, 1, 50);
            for (int a = 0; a < 8; a++) {
                for (int b = 0; b < 8; b++) {
                    int fx, fy;
                    if (rot == 0) { fx = a; fy = b; }
                    else if (rot == 1) { fx = b; fy = 7 - a; }
                    else if (rot == 2) { fx = 7 - a; fy = 7 - b; }
                    else { fx = 7 - b; fy = a; }
                    assertEquals("rot " + rot + " a=" + a + " b=" + b, 12345 - (10 + a * 8 + b) * 8, ground[1][40 + fx][50 + fy]);
                    assertEquals("collision bit cleared", 0, maps[1].tileFlags[40 + a][50 + b] & 0x1000000);
                }
            }
            assertEquals("only the chunk is written", 12345, ground[1][48][50]);
            assertEquals("source plane differs from dest plane", 12345, ground[2][40][50]);
            assertEquals("other planes kept", 12345, ground[3][40][50]);
            assertEquals("collision kept outside the chunk", 0x1000000, maps[1].tileFlags[48][50] & 0x1000000);
        }
    }

    // ------------------------------------------------ method183 one rotated 8x8 chunk of objects

    private static boolean[][] occupied(WorldController w, int plane) throws Exception {
        Field g = null;
        for (Field candidate : WorldController.class.getDeclaredFields()) {
            if (candidate.getType().isArray() && candidate.getType().getComponentType().isArray()
                    && candidate.getType().getComponentType().getComponentType().isArray()
                    && !candidate.getType().getComponentType().getComponentType().getComponentType().isPrimitive()
                    && candidate.getType().getComponentType().getComponentType().getComponentType().getSimpleName().equals("Ground")) {
                g = candidate;
            }
        }
        g.setAccessible(true);
        Object[][][] grounds = (Object[][][]) g.get(w);
        boolean[][] out = new boolean[105][105];
        for (int x = 0; x < grounds[plane].length; x++) for (int y = 0; y < grounds[plane][x].length; y++) out[x][y] = grounds[plane][x][y] != null;
        return out;
    }

    @Test public void method183PlacesTheObjectsOfThePlaneIChunkAtI1K1RotatedByJ1OntoPlaneLAtJAndK1() throws Exception {
        ObjectDef def = newDef(604);
        def.animationId = 3;
        install(def);
        int a = 3, b = 5;
        for (int rot = 0; rot < 4; rot++) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            smart(out, 605);
            int q1 = 2 << 12 | (16 + a) << 6 | (24 + b);
            int q2 = 2 << 12 | 30 << 6 | (24 + b);
            int q3 = 3 << 12 | (16 + a) << 6 | (24 + b);
            smart(out, q1 + 1);
            out.write(22 << 2 | 0);
            smart(out, q2 - q1 + 1);
            out.write(22 << 2);
            smart(out, q3 - q2 + 1);
            out.write(22 << 2);
            smart(out, 0);
            smart(out, 0);
            ManagerHolder h = new ManagerHolder();
            WorldController world = new WorldController(new int[4][105][105]);
            // i = source plane, j = dest x, k = source y, l = dest plane, i1 = source x, j1 = rotation, k1 = dest y
            h.manager.loadObjectChunk(maps(), world, 2, 60, 24, 0, out.toByteArray(), 16, rot, 50);
            int fx, fy;
            if (rot == 0) { fx = a; fy = b; }
            else if (rot == 1) { fx = b; fy = 7 - a; }
            else if (rot == 2) { fx = 7 - a; fy = 7 - b; }
            else { fx = 7 - b; fy = a; }
            boolean[][] on = occupied(world, 0);
            int count = 0;
            for (int x = 0; x < 105; x++) for (int y = 0; y < 105; y++) if (on[x][y]) count++;
            assertTrue("rot " + rot, on[60 + fx][50 + fy]);
            assertEquals("exactly one object placed", 1, count);
        }
    }

    private static final class ManagerHolder {
        final ObjectManager manager = new ObjectManager(new byte[4][105][105], new int[4][105][105]);
    }

    // ------------------------------------------------ placeObject shape 10/11 footprint and shadow locals

    private static Model shadowModel(int radius) throws Exception {
        Constructor<Model> c = Model.class.getDeclaredConstructor();
        c.setAccessible(true);
        Model m = c.newInstance();
        m.anInt1626 = 1;
        m.anIntArray1627 = new int[] {0};
        m.anIntArray1628 = new int[] {0};
        m.anIntArray1629 = new int[] {0};
        m.anInt1630 = 0;
        m.radiusXZ = radius;
        return m;
    }

    private static int[] shadowExtent(int orientation, int radius) throws Exception {
        ObjectDef def = newDef(700 + orientation + 10 * radius);
        def.sizeX = 2;
        def.sizeY = 3;
        field(ObjectDef.class, "modelIds").set(def, new int[] {1});
        install(def);
        ObjectDef.mruNodes2.removeFromCache(shadowModel(radius), (def.type << 6) + orientation + ((long) 0 << 32));
        ObjectManager m = new ObjectManager(new byte[4][105][105], new int[4][105][105]);
        Method method = ObjectManager.class.getDeclaredMethod("placeObject", int.class, WorldController.class,
                CollisionMap.class, int.class, int.class, int.class, int.class, int.class);
        method.setAccessible(true);
        method.invoke(m, 30, new WorldController(new int[4][105][105]), null, 10, 0, 20, def.type, orientation);
        byte[][][] shadow = f(m, "objectShadow");
        int minX = 999, maxX = -1, minY = 999, maxY = -1, value = 0;
        for (int x = 0; x < 105; x++) {
            for (int y = 0; y < 105; y++) {
                if (shadow[0][x][y] != 0) {
                    minX = Math.min(minX, x); maxX = Math.max(maxX, x);
                    minY = Math.min(minY, y); maxY = Math.max(maxY, y);
                    value = shadow[0][x][y];
                }
            }
        }
        return new int[] {minX, maxX, minY, maxY, value};
    }

    @Test public void placeObjectShadowCoversSizeXPlusOneByYPlusOneCellsSwappedForSidewaysOrientationsWithRadiusOverFourCappedAt30() throws Exception {
        assertArrayEquals(new int[] {20, 22, 30, 33, 20}, shadowExtent(0, 80));
        assertArrayEquals(new int[] {20, 23, 30, 32, 20}, shadowExtent(1, 80));
        assertArrayEquals(new int[] {20, 22, 30, 33, 20}, shadowExtent(2, 80));
        assertArrayEquals(new int[] {20, 23, 30, 32, 11}, shadowExtent(3, 45));
        assertArrayEquals(new int[] {20, 22, 30, 33, 30}, shadowExtent(0, 400));
    }

    // ------------------------------------------------ ObjectDef.buildModel locals

    private static Model triangle(int xOffset) throws Exception {
        Constructor<Model> c = Model.class.getDeclaredConstructor();
        c.setAccessible(true);
        Model m = c.newInstance();
        m.anInt1626 = 3;
        m.anIntArray1627 = new int[] {xOffset, xOffset + 64, xOffset};
        m.anIntArray1628 = new int[] {0, 0, 40};
        m.anIntArray1629 = new int[] {0, 0, 0};
        m.anInt1630 = 1;
        m.anIntArray1631 = new int[] {0};
        m.anIntArray1632 = new int[] {1};
        m.anIntArray1633 = new int[] {2};
        m.anIntArray1640 = new int[] {1234};
        return m;
    }

    private static Model build(ObjectDef d, int shape, int frame, int orientation) throws Exception {
        Method m = ObjectDef.class.getDeclaredMethod("buildModel", int.class, int.class, int.class);
        m.setAccessible(true);
        return (Model) m.invoke(d, shape, frame, orientation);
    }

    private static int maxX(Model m) {
        int max = Integer.MIN_VALUE;
        for (int v = 0; v < m.anInt1626; v++) max = Math.max(max, m.anIntArray1627[v]);
        return max;
    }

    @Test public void buildModelWithModelShapesTakesTheModelAtTheIndexOfTheMatchingShapeAndKeysTheCachePerIndex() throws Exception {
        ObjectDef d = newDef(9601);
        field(ObjectDef.class, "modelShapes").set(d, new int[] {5, 10, 22});
        field(ObjectDef.class, "modelIds").set(d, new int[] {8101, 8102, 8103});
        for (int n = 0; n < 3; n++) {
            ObjectDef.mruNodes1.removeFromCache(triangle(1000 * (n + 1)), 8101 + n);
        }
        Model first = build(d, 10, -1, 0);
        assertTrue("index 1 -> model 8102", maxX(first) >= 2000 && maxX(first) < 3000);
        Model third = build(d, 22, -1, 0);
        assertTrue("index 2 -> model 8103", maxX(third) >= 3000);
        Model zeroth = build(d, 5, -1, 0);
        assertTrue("index 0 -> model 8101", maxX(zeroth) >= 1000 && maxX(zeroth) < 2000);
        assertNull("no matching shape", build(d, 0, -1, 0));
        assertSame("cached per index and orientation", first, build(d, 10, -1, 0));
        assertNotSame(first, build(d, 10, -1, 1));
    }

    @Test public void buildModelWithModelShapesMirrorsWhenMirroredXorOrientationAbove3() throws Exception {
        for (boolean flag : new boolean[] {false, true}) {
            for (int orientation = 0; orientation < 8; orientation++) {
                int id = 8200 + (flag ? 50 : 0) + orientation;
                ObjectDef d = newDef(9700 + id);
                field(ObjectDef.class, "modelShapes").set(d, new int[] {7, 10});
                field(ObjectDef.class, "modelIds").set(d, new int[] {1, id});
                field(ObjectDef.class, "mirrored").setBoolean(d, flag);
                ObjectDef.mruNodes1.removeFromCache(triangle(0), id);
                ObjectDef.mruNodes1.removeFromCache(triangle(500), id + 0x10000);
                Model out = build(d, 10, -1, orientation);
                assertNotNull(out);
                boolean expectMirrored = flag ^ orientation > 3;
                assertEquals("flag=" + flag + " orientation=" + orientation, expectMirrored, maxX(out) >= 500 || minAbs(out) >= 500);
            }
        }
    }

    private static int minAbs(Model m) {
        int min = Integer.MAX_VALUE;
        for (int v = 0; v < m.anInt1626; v++) min = Math.min(min, Math.abs(m.anIntArray1629[v]) + Math.abs(m.anIntArray1627[v]));
        return min;
    }

    @Test public void buildModelRecolorsEachModifiedColourToItsOriginalColourPartner() throws Exception {
        ObjectDef d = newDef(9801);
        field(ObjectDef.class, "keepsVertexNormals").setBoolean(d, true);
        field(ObjectDef.class, "modelIds").set(d, new int[] {8301});
        field(ObjectDef.class, "modifiedModelColors").set(d, new int[] {999, 1234});
        field(ObjectDef.class, "originalModelColors").set(d, new int[] {5, 777});
        ObjectDef.mruNodes1.removeFromCache(triangle(0), 8301);
        Model out = build(d, 10, -1, 0);
        assertEquals("the second pair applies (index 1)", 777, out.anIntArray1640[0]);
        ObjectDef e = newDef(9802);
        field(ObjectDef.class, "keepsVertexNormals").setBoolean(e, true);
        field(ObjectDef.class, "modelIds").set(e, new int[] {8301});
        field(ObjectDef.class, "modifiedModelColors").set(e, new int[] {1234, 999});
        field(ObjectDef.class, "originalModelColors").set(e, new int[] {5, 777});
        assertEquals("the first pair applies (index 0)", 5, build(e, 10, -1, 0).anIntArray1640[0]);
    }

    @Test public void buildModelJoinsAllModelIdsIntoOneModelWhenThereAreSeveral() throws Exception {
        ObjectDef d = newDef(9901);
        field(ObjectDef.class, "modelIds").set(d, new int[] {8401, 8402, 8403});
        ObjectDef.mruNodes1.removeFromCache(triangle(0), 8401);
        ObjectDef.mruNodes1.removeFromCache(triangle(1000), 8402);
        ObjectDef.mruNodes1.removeFromCache(triangle(2000), 8403);
        Model out = build(d, 10, -1, 0);
        assertEquals("three triangles", 9, out.anInt1626);
        assertEquals(3, out.anInt1630);
        assertTrue(maxX(out) >= 2064);
        ObjectDef single = newDef(9902);
        field(ObjectDef.class, "modelIds").set(single, new int[] {8402});
        assertEquals(3, build(single, 10, -1, 0).anInt1626);
    }
}
