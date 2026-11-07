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
 * Object placement in ObjectManager (placeObject/placeObjectWithoutShading/loadObjects), the shadow and occlusion
 * flags of ObjectDef (castsShadow/occludes) and the terrain-contour transform of
 * ObjectDef.getModel/buildModel. Original member names only.
 */
public class ObjectPlacementTest {
    private ObjectDef[] savedCache;
    private Animation[] savedAnims;
    private boolean savedLowMem, savedHideRoofs, savedManagerLowMem;

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
        savedLowMem = ObjectDef.lowMem;
        savedHideRoofs = ObjectManager.hideRoofs;
        savedManagerLowMem = ObjectManager.lowMem;
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
        ObjectDef.lowMem = savedLowMem;
        ObjectManager.hideRoofs = savedHideRoofs;
        ObjectManager.lowMem = savedManagerLowMem;
    }

    private static ObjectDef newDef(int type) throws Exception {
        Constructor<ObjectDef> constructor = ObjectDef.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        ObjectDef def = constructor.newInstance();
        Method defaults = ObjectDef.class.getDeclaredMethod("setDefaults");
        defaults.setAccessible(true);
        defaults.invoke(def);
        def.type = type;
        return def;
    }

    /** An animated (so placeable without models) definition. */
    private static ObjectDef animated(int type) throws Exception {
        ObjectDef d = newDef(type);
        d.animationId = 3;
        d.blocksMovement = false;
        return d;
    }

    private static void install(ObjectDef... defs) throws Exception {
        ObjectDef[] cache = new ObjectDef[20];
        for (int n = 0; n < 20; n++) {
            cache[n] = n < defs.length ? defs[n] : newDef(-1);
        }
        field(ObjectDef.class, "cache").set(null, cache);
    }

    private static ObjectManager manager() {
        return new ObjectManager(new byte[4][105][105], new int[4][105][105]);
    }

    private static WorldController world() {
        return new WorldController(new int[4][105][105]);
    }

    private static Ground[][][] grounds(WorldController w) throws Exception {
        return (Ground[][][]) field(WorldController.class, "groundArray").get(w);
    }

    private static void place175(ObjectManager m, WorldController world, CollisionMap map, int tileY, int shape,
            int plane, int tileX, int id, int orientation) throws Exception {
        Method method = ObjectManager.class.getDeclaredMethod("placeObject", int.class, WorldController.class,
                CollisionMap.class, int.class, int.class, int.class, int.class, int.class);
        method.setAccessible(true);
        method.invoke(m, tileY, world, map, shape, plane, tileX, id, orientation);
    }

    private static List<String> cells(ObjectManager m, String name) throws Exception {
        Object array = field(ObjectManager.class, name).get(m);
        List<String> out = new ArrayList<String>();
        for (int p = 0; p < Array.getLength(array); p++) {
            Object plane = Array.get(array, p);
            for (int x = 0; x < Array.getLength(plane); x++) {
                Object column = Array.get(plane, x);
                for (int y = 0; y < Array.getLength(column); y++) {
                    long v = ((Number) Array.get(column, y)).longValue();
                    if (v != 0) out.add(p + "," + x + "," + y + "=" + v);
                }
            }
        }
        Collections.sort(out);
        return out;
    }

    private static List<String> list(String... s) {
        List<String> l = new ArrayList<String>(Arrays.asList(s));
        Collections.sort(l);
        return l;
    }

    private static final int X = 20, Y = 30;

    // ------------------------------------------------ flags castsShadow / occludes decoding

    @Test public void aBoolean779DefaultsOnAndOpcode64ClearsIt() throws Exception {
        assertTrue(newDef(1).castsShadow);
        assertFalse(newDef(1).occludes);
        Method read = ObjectDef.class.getDeclaredMethod("readValues", Stream.class);
        read.setAccessible(true);
        ObjectDef d = newDef(1);
        read.invoke(d, new Stream(new byte[] {64, 0}));
        assertFalse(d.castsShadow);
        assertFalse("opcode 64 does not touch occludes", d.occludes);
        d = newDef(1);
        read.invoke(d, new Stream(new byte[] {23, 0}));
        assertTrue(d.occludes);
        assertTrue("opcode 23 does not touch castsShadow", d.castsShadow);
    }

    // ------------------------------------------------ castsShadow and array 134

    @Test public void aBoolean779WritesFixedStrengthDarkeningOnTheWallsCorners() throws Exception {
        String[][] shape0 = {{"0,20,30=50", "0,20,31=50"}, {"0,20,31=50", "0,21,31=50"},
            {"0,21,30=50", "0,21,31=50"}, {"0,20,30=50", "0,21,30=50"}};
        String[][] shape13 = {{"0,20,31=50"}, {"0,21,31=50"}, {"0,21,30=50"}, {"0,20,30=50"}};
        ObjectDef d = animated(604);
        install(d);
        for (int orientation = 0; orientation < 4; orientation++) {
            ObjectManager m = manager();
            place175(m, world(), null, Y, 0, 0, X, 604, orientation);
            assertEquals("shape 0 orientation " + orientation, list(shape0[orientation]), cells(m, "objectShadow"));
            for (int shape : new int[] {1, 3}) {
                m = manager();
                place175(m, world(), null, Y, shape, 0, X, 604, orientation);
                assertEquals("shape " + shape + " orientation " + orientation, list(shape13[orientation]), cells(m, "objectShadow"));
            }
        }
        d.castsShadow = false;
        for (int shape : new int[] {0, 1, 2, 3}) {
            ObjectManager m = manager();
            place175(m, world(), null, Y, shape, 0, X, 604, 1);
            assertEquals("flag off shape " + shape, list(), cells(m, "objectShadow"));
        }
        d.castsShadow = true;
        for (int shape : new int[] {2, 4, 22}) {
            ObjectManager m = manager();
            place175(m, world(), null, Y, shape, 0, X, 604, 1);
            assertEquals("flag on but shape " + shape, list(), cells(m, "objectShadow"));
        }
    }

    // ------------------------------------------------ occludes and array 135

    @Test public void aBoolean764SetsAxisBitsInArray135ForWallsAndCornerWalls() throws Exception {
        ObjectDef d = animated(604);
        d.occludes = true;
        install(d);
        String[][] shape0 = {{"0,20,30=585"}, {"0,20,31=1170"}, {"0,21,30=585"}, {"0,20,30=1170"}};
        String[][] shape2 = {{"0,20,30=585", "0,20,31=1170"}, {"0,20,31=1170", "0,21,30=585"},
            {"0,20,30=1170", "0,21,30=585"}, {"0,20,30=1755"}};
        for (int orientation = 0; orientation < 4; orientation++) {
            ObjectManager m = manager();
            place175(m, world(), null, Y, 0, 0, X, 604, orientation);
            assertEquals("shape 0 orientation " + orientation, list(shape0[orientation]), cells(m, "occlusionFlags"));
            m = manager();
            place175(m, world(), null, Y, 2, 0, X, 604, orientation);
            assertEquals("shape 2 orientation " + orientation, list(shape2[orientation]), cells(m, "occlusionFlags"));
            for (int shape : new int[] {1, 3, 4, 10, 22}) {
                m = manager();
                place175(m, world(), null, Y, shape, 0, X, 604, orientation);
                assertEquals("shape " + shape, list(), cells(m, "occlusionFlags"));
            }
        }
        d.occludes = false;
        for (int shape : new int[] {0, 2}) {
            ObjectManager m = manager();
            place175(m, world(), null, Y, shape, 0, X, 604, 0);
            assertEquals("flag off shape " + shape, list(), cells(m, "occlusionFlags"));
        }
    }

    @Test public void roofShapesMarkTheWholeOcclusionFlagRegardlessOfEitherDefinitionFlag() throws Exception {
        ObjectDef d = animated(604);
        d.occludes = false;
        d.castsShadow = false;
        install(d);
        for (int shape = 12; shape <= 18; shape++) {
            ObjectManager m = manager();
            place175(m, world(), null, Y, shape, 1, X, 604, 0);
            boolean expect = shape != 13 && shape <= 17;
            assertEquals("shape " + shape, expect ? list("1,20,30=2340") : list(), cells(m, "occlusionFlags"));
            m = manager();
            place175(m, world(), null, Y, shape, 0, X, 604, 0);
            assertEquals("ground plane never flagged, shape " + shape, list(), cells(m, "occlusionFlags"));
        }
    }

    // ------------------------------------------------ placeObject vs placeObjectWithoutShading

    @Test public void method188PlacesTheSameWorldObjectAsMethod175AndMethod175AlsoWritesShading() throws Exception {
        ObjectDef d = animated(604);
        d.occludes = true;
        d.sizeX = 3;
        d.sizeY = 2;
        install(d);
        for (int shape : new int[] {0, 10}) {
            for (int orientation = 0; orientation < 4; orientation++) {
                WorldController a = world();
                WorldController b = world();
                ObjectManager m = manager();
                place175(m, a, null, Y, shape, 0, X, 604, orientation);
                ObjectManager.placeObjectWithoutShading(b, orientation, Y, shape, 0, new CollisionMap(), new int[4][105][105], X, 604, 0);
                Ground ga = grounds(a)[0][X][Y];
                Ground gb = grounds(b)[0][X][Y];
                assertNotNull(ga);
                assertNotNull(gb);
                if (shape == 10) {
                    Object5 oa = ga.obj5Array[0];
                    Object5 ob = gb.obj5Array[0];
                    assertEquals(oa.uid, ob.uid);
                    assertEquals("uid = x + y<<7 + id<<14 + 0x40000000, plus 0x80000000 without actions", X + (Y << 7) + (604 << 14) + 0x40000000 + 0x80000000, oa.uid);
                    assertEquals("orientation in the top two bits, shape below", (byte) ((orientation << 6) + shape), oa.aByte530);
                    assertEquals(oa.aByte530, ob.aByte530);
                    assertEquals(oa.anInt524, ob.anInt524);
                    assertEquals(oa.anInt526, ob.anInt526);
                } else {
                    assertEquals(ga.obj1.uid, gb.obj1.uid);
                    assertEquals(ga.obj1.aByte281, gb.obj1.aByte281);
                    assertFalse("placeObject shades walls", cells(m, "occlusionFlags").isEmpty());
                }
            }
        }
    }

    @Test public void method188HeightComesFromItsHeightPlaneAndTheWorldPlaneFromTheLastArgument() throws Exception {
        install(animated(604));
        int[][][] ground = new int[4][105][105];
        ground[1][X][Y] = 40;
        ground[1][X + 1][Y] = 80;
        ground[1][X + 1][Y + 1] = 120;
        ground[1][X][Y + 1] = 160;
        WorldController w = world();
        ObjectManager.placeObjectWithoutShading(w, 0, Y, 10, 1, new CollisionMap(), ground, X, 604, 0);
        assertNotNull("placed on the plane named by the last argument", grounds(w)[0][X][Y]);
        assertEquals(0, grounds(w)[0][X][Y].obj5Array[0].anInt517);
        assertEquals("average of the four corners on the height plane", 100, grounds(w)[0][X][Y].obj5Array[0].anInt518);
        w = world();
        ObjectManager.placeObjectWithoutShading(w, 0, Y, 10, 0, new CollisionMap(), ground, X, 604, 1);
        assertNotNull(grounds(w)[1][X][Y]);
        assertEquals(1, grounds(w)[1][X][Y].obj5Array[0].anInt517);
        assertEquals("height plane 0 is flat", 0, grounds(w)[1][X][Y].obj5Array[0].anInt518);
    }

    @Test public void method175HeightIsTheAverageOfTheFourCornerHeights() throws Exception {
        install(animated(604));
        int[][][] ground = new int[4][105][105];
        ground[0][X][Y] = 40;
        ground[0][X + 1][Y] = 80;
        ground[0][X + 1][Y + 1] = 120;
        ground[0][X][Y + 1] = 160;
        WorldController w = world();
        place175(new ObjectManager(new byte[4][105][105], ground), w, null, Y, 10, 0, X, 604, 0);
        assertEquals(100, grounds(w)[0][X][Y].obj5Array[0].anInt518);
        ground[0][X + 1][Y + 1] = 520;
        w = world();
        place175(new ObjectManager(new byte[4][105][105], ground), w, null, Y, 10, 0, X, 604, 0);
        assertEquals(200, grounds(w)[0][X][Y].obj5Array[0].anInt518);
    }

    // ------------------------------------------------ loadObjects region object stream

    private static void smart(java.io.ByteArrayOutputStream out, int v) {
        if (v < 128) {
            out.write(v);
        } else {
            out.write(0x80 | v >> 8);
            out.write(v & 0xff);
        }
    }

    /** One object group: id, then a single position (plane, localX, localY) with type/orientation. */
    private static byte[] stream(int id, int plane, int localX, int localY, int shape, int orientation) {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        smart(out, id + 1);
        smart(out, (plane << 12 | localX << 6 | localY) + 1);
        out.write(shape << 2 | orientation);
        smart(out, 0);
        smart(out, 0);
        return out.toByteArray();
    }

    private static CollisionMap[] maps() {
        CollisionMap[] maps = new CollisionMap[4];
        for (int p = 0; p < 4; p++) {
            maps[p] = new CollisionMap();
            for (int[] column : maps[p].tileFlags) Arrays.fill(column, 0);
        }
        return maps;
    }

    private static int blockedTiles(CollisionMap map) {
        int n = 0;
        for (int[] column : map.tileFlags) for (int v : column) if (v != 0) n++;
        return n;
    }

    @Test public void method190AddsItsFirstIntegerToXAndItsThirdToYAndDecodesShapeAndOrientation() throws Exception {
        ObjectDef d = animated(604);
        d.sizeX = 3;
        d.sizeY = 2;
        install(d);
        for (int orientation = 0; orientation < 4; orientation++) {
            WorldController w = world();
            manager().loadObjects(10, maps(), 20, w, stream(604, 0, 3, 5, 10, orientation));
            assertNull("local x is not added to y", grounds(w)[0][25][13]);
            Object5 o = grounds(w)[0][13][25].obj5Array[0];
            int wide = orientation % 2 == 0 ? 3 : 2;
            int tall = orientation % 2 == 0 ? 2 : 3;
            assertEquals(13, o.anInt523);
            assertEquals(25, o.anInt525);
            assertEquals(13 + wide - 1, o.anInt524);
            assertEquals(25 + tall - 1, o.anInt526);
        }
    }

    @Test public void method190IgnoresObjectsOutsideTheInnerTiles() throws Exception {
        install(animated(604));
        // {x base, y base, local x, local y, placed}: placed only when 0 < x < 103 and 0 < y < 103
        int[][] probes = {{0, 0, 0, 0, 0}, {0, 0, 1, 1, 1}, {0, 0, 0, 5, 0}, {0, 0, 5, 0, 0},
            {100, 0, 2, 5, 1}, {100, 0, 3, 5, 0}, {0, 100, 5, 2, 1}, {0, 100, 5, 3, 0}};
        for (int[] p : probes) {
            WorldController w = world();
            manager().loadObjects(p[0], maps(), p[1], w, stream(604, 0, p[2], p[3], 10, 0));
            int x = p[0] + p[2], y = p[1] + p[3];
            boolean placed = x < 105 && y < 105 && grounds(w)[0][x][y] != null;
            assertEquals(Arrays.toString(p), p[4] == 1, placed);
        }
    }

    @Test public void method190StepsOneCollisionPlaneDownWhenThePlaneAboveHasTheBridgeBit() throws Exception {
        ObjectDef d = animated(604);
        d.blocksMovement = true;
        install(d);
        byte[][][] flags = new byte[4][105][105];
        flags[1][13][25] = 2;
        ObjectManager m = new ObjectManager(flags, new int[4][105][105]);
        CollisionMap[] maps = maps();
        WorldController w = world();
        m.loadObjects(10, maps, 20, w, stream(604, 1, 3, 5, 10, 0));
        assertNotNull("still placed on plane 1", grounds(w)[1][13][25]);
        assertTrue(blockedTiles(maps[0]) > 0);
        assertEquals(0, blockedTiles(maps[1]));

        maps = maps();
        manager().loadObjects(10, maps, 20, world(), stream(604, 1, 3, 5, 10, 0));
        assertEquals(0, blockedTiles(maps[0]));
        assertTrue(blockedTiles(maps[1]) > 0);
    }

    // ------------------------------------------------ ObjectDef.getModel / buildModel

    private static Model flatModel() throws Exception {
        Constructor<Model> c = Model.class.getDeclaredConstructor();
        c.setAccessible(true);
        Model m = c.newInstance();
        m.anInt1626 = 5;
        m.anIntArray1627 = new int[] {-64, 64, 64, -64, 0};
        m.anIntArray1628 = new int[] {0, 0, 0, 0, 7};
        m.anIntArray1629 = new int[] {-64, -64, 64, 64, 0};
        m.anInt1630 = 0;
        return m;
    }

    private static void seed(ObjectDef d, int orientation, int frame, Model model) {
        long key = (d.type << 6) + orientation + ((long) (frame + 1) << 32);
        ObjectDef.mruNodes2.removeFromCache(model, key);
    }

    @Test public void method578BlendsFourCornerHeightsBilinearlyWhenAboolean762() throws Exception {
        ObjectDef d = newDef(10);
        d.adjustsToTerrain = true;
        Model source = flatModel();
        seed(d, 0, -1, source);
        // heights k=0 at (-x,-z), l=100 at (+x,-z), i1=300 at (+x,+z), j1=200 at (-x,+z); mean 150
        Model out = d.getModel(10, 0, 0, 100, 300, 200, -1);
        assertNotSame("a private copy is returned", source, out);
        assertArrayEquals(new int[] {-150, -50, 150, 50, 7}, out.anIntArray1628.length == 5 ? out.anIntArray1628 : null);
        assertArrayEquals("source vertices are not mutated", new int[] {0, 0, 0, 0, 7}, source.anIntArray1628);
    }

    @Test public void method578LeavesHeightsAloneWithoutAboolean762() throws Exception {
        ObjectDef d = newDef(11);
        Model source = flatModel();
        seed(d, 0, -1, source);
        assertSame(source, d.getModel(10, 0, 0, 100, 300, 200, -1));
    }

    @Test public void method578SelectsTheCachedModelByShapeOrientationAndFrame() throws Exception {
        ObjectDef d = newDef(12);
        Model a = flatModel();
        seed(d, 2, -1, a);
        assertSame(a, d.getModel(10, 2, 0, 0, 0, 0, -1));
        assertNull("orientation is the second argument", d.getModel(10, 1, 0, 0, 0, 0, -1));
        assertNull("frame is the last argument", d.getModel(10, 2, 0, 0, 0, 0, 5));
        assertNull("only shape 10 is available without per-shape models", d.getModel(4, 2, 0, 0, 0, 0, -1));
        Model b = flatModel();
        seed(d, 2, 5, b);
        assertSame(b, d.getModel(10, 2, 0, 0, 0, 0, 5));
        assertSame(a, d.getModel(10, 2, 0, 0, 0, 0, -1));
    }
}
