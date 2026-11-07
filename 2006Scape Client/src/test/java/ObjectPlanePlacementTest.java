import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Plane filter and lowest-built-plane statics of ObjectManager (anInt131 / anInt145), their Game consumers, and the
 * shape tables and locals of placeObject / placeObjectWithoutShading. Original member names only.
 */
public class ObjectPlanePlacementTest {
    private ObjectDef[] savedCache;
    private Animation[] savedAnims;
    private Flo[] savedFlo;
    private int[] savedPalette;
    private boolean savedHideRoofs, savedLowMem, savedSnow, savedDefLowMem, savedGameLowMem;
    private int savedFilter, savedLowest;

    private static Field field(Class<?> type, String name) throws Exception {
        Field f = type.getDeclaredField(name);
        f.setAccessible(true);
        return f;
    }

    private static sun.misc.Unsafe unsafe() throws Exception {
        return (sun.misc.Unsafe) field(sun.misc.Unsafe.class, "theUnsafe").get(null);
    }

    private static Object get(Object o, String name) throws Exception {
        return field(o.getClass(), name).get(o);
    }

    @Before public void setUp() throws Exception {
        savedCache = (ObjectDef[]) field(ObjectDef.class, "cache").get(null);
        savedAnims = Animation.anims;
        savedFlo = Flo.cache;
        savedPalette = Texture.hslToRgbPalette;
        savedHideRoofs = ObjectManager.hideRoofs;
        savedLowMem = ObjectManager.lowMem;
        savedSnow = ObjectManager.hideBuggyVarrockSwordShopSnow;
        savedDefLowMem = ObjectDef.lowMem;
        savedGameLowMem = Game.lowMem;
        savedFilter = ObjectManager.scenePlaneFilter;
        savedLowest = ObjectManager.lowestBuiltPlane;
        ObjectManager.lowMem = false;
        ObjectManager.hideRoofs = false;
        ObjectManager.hideBuggyVarrockSwordShopSnow = false;
        ObjectDef.lowMem = false;
        Animation[] anims = new Animation[6];
        Animation a = (Animation) unsafe().allocateInstance(Animation.class);
        a.loopBackFrames = -1;
        a.frameCount = 1;
        anims[3] = a;
        Animation.anims = anims;
        Flo.cache = new Flo[] {flo()};
        int[] palette = new int[0x10000];
        for (int i = 0; i < palette.length; i++) {
            palette[i] = i + 1;
        }
        Texture.hslToRgbPalette = palette;
    }

    @After public void tearDown() throws Exception {
        field(ObjectDef.class, "cache").set(null, savedCache);
        Animation.anims = savedAnims;
        Flo.cache = savedFlo;
        Texture.hslToRgbPalette = savedPalette;
        ObjectManager.hideRoofs = savedHideRoofs;
        ObjectManager.lowMem = savedLowMem;
        ObjectManager.hideBuggyVarrockSwordShopSnow = savedSnow;
        ObjectDef.lowMem = savedDefLowMem;
        Game.lowMem = savedGameLowMem;
        ObjectManager.scenePlaneFilter = savedFilter;
        ObjectManager.lowestBuiltPlane = savedLowest;
    }

    private static Flo flo() throws Exception {
        Flo f = (Flo) unsafe().allocateInstance(Flo.class);
        f.textureId = -1;
        f.occludes = true;
        f.saturation = 100;
        f.lightness = 80;
        f.weightedHue = 100;
        f.hueWeight = 200;
        return f;
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
        def.castsShadow = false;
        def.occludes = false;
        def.animationId = 3;
        return def;
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

    private static byte[][][] flags(ObjectManager m) throws Exception {
        return (byte[][][]) get(m, "tileFlags");
    }

    private static int[][][] heights(ObjectManager m) throws Exception {
        return (int[][][]) get(m, "tileHeights");
    }

    private static Ground ground(WorldController w, int plane, int x, int y) throws Exception {
        return ((Ground[][][]) get(w, "groundArray"))[plane][x][y];
    }

    private static void place(ObjectManager m, WorldController w, int shape, int plane, int x, int y, int id, int orientation) throws Exception {
        Method method = ObjectManager.class.getDeclaredMethod("placeObject", int.class, WorldController.class,
                CollisionMap.class, int.class, int.class, int.class, int.class, int.class);
        method.setAccessible(true);
        method.invoke(m, y, w, null, shape, plane, x, id, orientation);
    }

    private static WorldController world() {
        return new WorldController(new int[4][105][105]);
    }

    /** Places through either placeObject (twin false) or placeObjectWithoutShading (twin true); returns the controller. */
    private static WorldController placeEither(boolean twin, ObjectManager m, WorldController w, int shape, int x, int y, int id, int orientation) throws Exception {
        if (twin) {
            ObjectManager.placeObjectWithoutShading(w, orientation, y, shape, 0, null, heights(m), x, id, 0);
        } else {
            place(m, w, shape, 0, x, y, id, orientation);
        }
        return w;
    }

    // ------------------------------------------------ anInt131 / anInt145 in placeObject

    private static boolean wallPlaced(ObjectManager m, int plane, int filter, boolean lowMemOnly) throws Exception {
        ObjectManager.hideRoofs = !lowMemOnly;
        ObjectManager.lowMem = lowMemOnly;
        ObjectManager.scenePlaneFilter = filter;
        WorldController w = world();
        place(m, w, 0, plane, 10, 10, 1, 0);
        return w.getWallUid(plane, 10, 10) != 0;
    }

    @Test public void anInt131SelectsTheOnlyPlaneWhoseObjectsAreBuiltWhenRoofsAreHiddenOrLowMem() throws Exception {
        install(newDef(1));
        for (boolean lowMemOnly : new boolean[] {false, true}) {
            for (int plane = 0; plane < 4; plane++) {
                for (int filter = 0; filter < 4; filter++) {
                    assertEquals("plane " + plane + " filter " + filter + " lowMemOnly " + lowMemOnly,
                            plane == filter, wallPlaced(manager(), plane, filter, lowMemOnly));
                }
            }
        }
    }

    @Test public void anInt131IsIgnoredWhenNeitherHideRoofsNorLowMemIsSet() throws Exception {
        install(newDef(1));
        ObjectManager.hideRoofs = false;
        ObjectManager.lowMem = false;
        for (int filter = 0; filter < 4; filter++) {
            ObjectManager.scenePlaneFilter = filter;
            WorldController w = world();
            place(manager(), w, 0, 2, 10, 10, 1, 0);
            assertTrue("filter " + filter, w.getWallUid(2, 10, 10) != 0);
        }
    }

    @Test public void anInt131IsComparedWithTheEffectivePlaneOfTheTile() throws Exception {
        install(newDef(1));
        // bridge bit (2) on plane 1 lowers the effective plane of planes above 0 by one
        ObjectManager m = manager();
        flags(m)[1][10][10] = 2;
        assertTrue(wallPlaced(m, 1, 0, false));
        assertFalse(wallPlaced(m, 1, 1, false));
        // bit 8 on a tile sends its effective plane to 0
        m = manager();
        flags(m)[2][10][10] = 8;
        assertTrue(wallPlaced(m, 2, 0, false));
        assertFalse(wallPlaced(m, 2, 2, false));
        // bit 0x10 (roof) on the tile keeps it out of every filtered scene
        m = manager();
        flags(m)[1][10][10] = 0x10;
        assertFalse(wallPlaced(m, 1, 1, false));
        assertFalse(wallPlaced(m, 1, 0, false));
        // bit 2 on plane 0 of the same tile exempts the tile from the filter
        m = manager();
        flags(m)[0][10][10] = 2;
        flags(m)[3][10][10] = 0x10;
        assertTrue(wallPlaced(m, 3, 0, false));
        assertTrue(wallPlaced(m, 3, 3, false));
    }

    @Test public void anInt145IsResetToNinetyNineByTheConstructorAndOnlyDropsToTheLowestPlanePlaced() throws Exception {
        install(newDef(1));
        ObjectManager.lowestBuiltPlane = 2;
        ObjectManager m = manager();
        assertEquals(99, ObjectManager.lowestBuiltPlane);
        ObjectManager.hideRoofs = true;
        // a filtered-out placement leaves it alone
        ObjectManager.scenePlaneFilter = 0;
        place(m, world(), 0, 3, 10, 10, 1, 0);
        assertEquals(99, ObjectManager.lowestBuiltPlane);
        ObjectManager.scenePlaneFilter = 2;
        place(m, world(), 0, 2, 10, 10, 1, 0);
        assertEquals(2, ObjectManager.lowestBuiltPlane);
        ObjectManager.scenePlaneFilter = 1;
        place(m, world(), 0, 1, 10, 10, 1, 0);
        assertEquals(1, ObjectManager.lowestBuiltPlane);
        // a higher plane afterwards does not raise it
        ObjectManager.scenePlaneFilter = 3;
        place(m, world(), 0, 3, 10, 10, 1, 0);
        assertEquals(1, ObjectManager.lowestBuiltPlane);
        // unfiltered: the lowest of the planes placed, not the last
        ObjectManager.hideRoofs = false;
        m = manager();
        place(m, world(), 0, 3, 10, 10, 1, 0);
        place(m, world(), 0, 2, 10, 10, 1, 0);
        place(m, world(), 0, 3, 11, 10, 1, 0);
        assertEquals(2, ObjectManager.lowestBuiltPlane);
    }

    // ------------------------------------------------ anInt131 / anInt145 in buildTerrainScene

    private static boolean tileBuilt(WorldController w, int plane, int x, int y) throws Exception {
        Ground g = ground(w, plane, x, y);
        return g != null && g.plainTile != null;
    }

    private static WorldController terrain(ObjectManager m) throws Exception {
        byte[][][] underlay = (byte[][][]) get(m, "underlayIds");
        for (int plane = 0; plane < 4; plane++) {
            for (int x = 0; x < 104; x++) {
                for (int y = 0; y < 104; y++) {
                    underlay[plane][x][y] = 1;
                }
            }
        }
        CollisionMap[] maps = new CollisionMap[4];
        for (int i = 0; i < 4; i++) {
            maps[i] = new CollisionMap();
        }
        WorldController w = world();
        m.buildTerrainScene(maps, w);
        return w;
    }

    @Test public void anInt131SelectsTheOnlyPlaneWhoseTerrainTilesAreBuiltWhenFiltering() throws Exception {
        for (int mode = 0; mode < 3; mode++) {
            ObjectManager.hideRoofs = mode == 0;
            ObjectManager.lowMem = mode == 1;
            ObjectManager.hideBuggyVarrockSwordShopSnow = mode == 2;
            for (int filter = 0; filter < 4; filter++) {
                ObjectManager.scenePlaneFilter = filter;
                WorldController w = terrain(manager());
                for (int plane = 0; plane < 4; plane++) {
                    assertEquals("mode " + mode + " plane " + plane + " filter " + filter, plane == filter, tileBuilt(w, plane, 50, 50));
                }
            }
        }
        ObjectManager.hideBuggyVarrockSwordShopSnow = false;
        ObjectManager.hideRoofs = false;
        ObjectManager.lowMem = false;
        ObjectManager.scenePlaneFilter = 1;
        WorldController all = terrain(manager());
        for (int plane = 0; plane < 4; plane++) {
            assertTrue("unfiltered plane " + plane, tileBuilt(all, plane, 50, 50));
        }
    }

    @Test public void anInt131IsComparedWithTheEffectivePlaneInTheTerrainLoopToo() throws Exception {
        ObjectManager.hideRoofs = true;
        ObjectManager.scenePlaneFilter = 0;
        ObjectManager m = manager();
        flags(m)[2][50][50] = 8;      // effective plane 0
        flags(m)[3][52][50] = 0x10;   // roof bit on plane 3 (never matches 0 anyway) ...
        flags(m)[0][54][50] = 2;      // exempt column
        flags(m)[1][54][50] = 0x10;
        WorldController w = terrain(m);
        assertTrue(tileBuilt(w, 0, 50, 50));
        assertFalse(tileBuilt(w, 1, 50, 50));
        assertTrue(tileBuilt(w, 2, 50, 50));
        assertFalse(tileBuilt(w, 3, 50, 50));
        for (int plane = 0; plane < 4; plane++) {
            assertTrue("exempt column plane " + plane, tileBuilt(w, plane, 54, 50));
        }
        ObjectManager.scenePlaneFilter = 2;
        m = manager();
        flags(m)[2][50][50] = 0x10;   // roof bit keeps plane 2 out even though the filter matches
        w = terrain(m);
        assertFalse(tileBuilt(w, 2, 50, 50));
        assertTrue(tileBuilt(w, 2, 51, 50));
    }

    private static void run(ObjectManager m) throws Exception {
        CollisionMap[] maps = new CollisionMap[4];
        for (int i = 0; i < 4; i++) {
            maps[i] = new CollisionMap();
        }
        m.buildTerrainScene(maps, world());
    }

    @Test public void anInt145IsTheLowestPlaneThatPassedTheFilterInTheTerrainLoop() throws Exception {
        ObjectManager.hideRoofs = true;
        for (int filter = 0; filter < 4; filter++) {
            ObjectManager.scenePlaneFilter = filter;
            ObjectManager m = manager();     // no underlay at all: planes still count
            run(m);
            assertEquals("filter " + filter, filter, ObjectManager.lowestBuiltPlane);
        }
        ObjectManager.scenePlaneFilter = 5;
        ObjectManager m = manager();
        run(m);
        assertEquals("no plane matched", 99, ObjectManager.lowestBuiltPlane);
        ObjectManager.scenePlaneFilter = 3;
        m = manager();
        ObjectManager.lowestBuiltPlane = 1;       // set after the constructor: a higher plane never raises it
        run(m);
        assertEquals(1, ObjectManager.lowestBuiltPlane);
        ObjectManager.hideRoofs = false;
        ObjectManager.scenePlaneFilter = 3;
        m = manager();
        run(m);
        assertEquals("unfiltered: lowest of 0..3, not the last or the filter", 0, ObjectManager.lowestBuiltPlane);
    }

    // ------------------------------------------------ Game consumers

    private static class Probe extends Game {
        int texts, refreshes;
        @Override public void drawTextOnScreen(String s, String s1) { texts++; }
        @Override public void rebuildScene() { refreshes++; }
    }

    private Probe probe(int plane, int filter) throws Exception {
        Probe g = new Probe();
        g.plane = plane;
        g.minimapBuiltPlane = plane;
        g.loadingStage = 2;
        g.regionTerrainData = new byte[0][];
        g.regionObjectData = new byte[0][];
        g.regionTerrainFileIds = new int[0];
        g.regionObjectFileIds = new int[0];
        g.regionKeys = new int[0];
        g.stream = new Stream(new byte[16]);
        g.stream.encryption = new ISAACRandomGen(new int[4]);
        ObjectManager.scenePlaneFilter = filter;
        return g;
    }

    @Test public void loadingStagesReloadsTheSceneInLowMemOnlyWhenAnInt131DiffersFromThePlane() throws Exception {
        Game.lowMem = true;
        Probe g = probe(2, 0);
        g.loadingStages();
        assertEquals("reload message", 1, g.texts);
        assertEquals("anInt131 follows the plane once loaded", 2, ObjectManager.scenePlaneFilter);
        assertEquals(2, g.loadingStage);
        assertEquals(1, g.refreshes);
        g = probe(2, 2);
        g.loadingStages();
        assertEquals(0, g.texts);
        assertEquals(0, g.refreshes);
        assertEquals(2, ObjectManager.scenePlaneFilter);
        Game.lowMem = false;
        g = probe(2, 0);
        g.loadingStages();
        assertEquals("high mem never reloads for a plane change", 0, g.texts);
        assertEquals("and does not touch anInt131", 0, ObjectManager.scenePlaneFilter);
    }

    // ------------------------------------------------ shape tables and locals of placeObject / placeObjectWithoutShading

    private static final int[] EDGE = {1, 2, 4, 8};
    private static final int[] CORNER = {16, 32, 64, 128};
    private static final int[] DIR_X = {1, 0, -1, 0};
    private static final int[] DIR_Y = {0, -1, 0, 1};

    private static int sub5(Animable a, String name) throws Exception {
        assertTrue(a instanceof AnimatedObject);
        return field(AnimatedObject.class, name).getInt(a);
    }

    @Test public void wallShapesUseTheEdgeMaskTableAndTheCornerMaskTableByOrientation() throws Exception {
        for (boolean twin : new boolean[] {false, true}) {
            for (int o = 0; o < 4; o++) {
                install(newDef(1));
                ObjectManager m = manager();
                Object1 wall = ground(placeEither(twin, m, world(), 0, 10, 10, 1, o), 0, 10, 10).obj1;
                assertEquals("shape 0 twin " + twin + " o " + o, EDGE[o], wall.orientation);
                assertEquals(0, wall.orientation1);
                assertNull(wall.secondaryModel);
                for (int shape : new int[] {1, 3}) {
                    wall = ground(placeEither(twin, manager(), world(), shape, 10, 10, 1, o), 0, 10, 10).obj1;
                    assertEquals("shape " + shape + " twin " + twin + " o " + o, CORNER[o], wall.orientation);
                    assertEquals(0, wall.orientation1);
                }
                wall = ground(placeEither(twin, manager(), world(), 2, 10, 10, 1, o), 0, 10, 10).obj1;
                assertEquals("shape 2 first piece", EDGE[o], wall.orientation);
                assertEquals("shape 2 second piece uses the next orientation", EDGE[(o + 1) & 3], wall.orientation1);
                assertEquals(4 + o, sub5(wall.model, "orientation"));
                assertEquals((o + 1) & 3, sub5(wall.secondaryModel, "orientation"));
                assertEquals(2, sub5(wall.model, "type"));
                assertEquals(2, sub5(wall.secondaryModel, "type"));
            }
        }
    }

    @Test public void wallDecorationsDrawMaskIsTheEdgeMaskAndShape5ShiftsByDirectionTablesTimesTheWallsOffset() throws Exception {
        for (boolean twin : new boolean[] {false, true}) {
            for (int o = 0; o < 4; o++) {
                ObjectDef wallDef = newDef(7);
                wallDef.wallDecorationOffset = 40;
                wallDef.hasActions = true;      // a wall without actions has a negative uid and is ignored (checked below)
                ObjectDef silentWall = newDef(6);
                silentWall.wallDecorationOffset = 40;
                silentWall.hasActions = false;
                ObjectDef plainWall = newDef(8);
                plainWall.hasActions = true;
                install(newDef(9), wallDef, plainWall, silentWall);
                ObjectManager m = manager();
                Object2 d4 = ground(placeEither(twin, m, world(), 4, 10, 10, 9, o), 0, 10, 10).obj2;
                assertEquals(EDGE[o], d4.drawMask);
                assertEquals("shape 4 is not shifted", 10 * 128 + 64, d4.worldX);
                assertEquals(10 * 128 + 64, d4.worldY);
                // shape 5 without a wall: default offset 16
                Object2 d5 = ground(placeEither(twin, manager(), world(), 5, 10, 10, 9, o), 0, 10, 10).obj2;
                assertEquals(EDGE[o], d5.drawMask);
                assertEquals("x o=" + o, 10 * 128 + 64 + DIR_X[o] * 16, d5.worldX);
                assertEquals("y o=" + o, 10 * 128 + 64 + DIR_Y[o] * 16, d5.worldY);
                // with a wall (object 7, offset 40) already on the tile: that wall's offset
                m = manager();
                WorldController w = world();
                placeEither(twin, m, w, 0, 10, 10, 7, 0);
                d5 = ground(placeEither(twin, m, w, 5, 10, 10, 9, o), 0, 10, 10).obj2;
                assertEquals(10 * 128 + 64 + DIR_X[o] * 40, d5.worldX);
                assertEquals(10 * 128 + 64 + DIR_Y[o] * 40, d5.worldY);
                // a wall on another tile does not count
                m = manager();
                w = world();
                placeEither(twin, m, w, 0, 11, 10, 7, 0);
                d5 = ground(placeEither(twin, m, w, 5, 10, 10, 9, o), 0, 10, 10).obj2;
                assertEquals(10 * 128 + 64 + DIR_X[o] * 16, d5.worldX);
                // a wall without actions is ignored (negative uid) even with offset 40
                m = manager();
                w = world();
                placeEither(twin, m, w, 0, 10, 10, 6, 0);
                d5 = ground(placeEither(twin, m, w, 5, 10, 10, 9, o), 0, 10, 10).obj2;
                assertEquals(10 * 128 + 64 + DIR_X[o] * 16, d5.worldX);
                // a wall of the default offset (object 8 = 16) gives 16 even though object 7 exists
                m = manager();
                w = world();
                placeEither(twin, m, w, 0, 10, 10, 8, 0);
                d5 = ground(placeEither(twin, m, w, 5, 10, 10, 9, o), 0, 10, 10).obj2;
                assertEquals(10 * 128 + 64 + DIR_Y[o] * 16, d5.worldY);
            }
        }
    }

    @Test public void shape10And11SceneObjectsGetRotationZeroOr256AndSwappedFootprintForSidewaysOrientations() throws Exception {
        for (boolean twin : new boolean[] {false, true}) {
            for (int o = 0; o < 4; o++) {
                ObjectDef def = newDef(1);
                def.sizeX = 2;
                def.sizeY = 3;
                install(def);
                for (int shape : new int[] {10, 11}) {
                    Ground g = ground(placeEither(twin, manager(), world(), shape, 20, 20, 1, o), 0, 20, 20);
                    Object5 so = g.obj5Array[0];
                    assertEquals(shape == 11 ? 256 : 0, so.rotation);
                    boolean sideways = o == 1 || o == 3;
                    assertEquals("x span", sideways ? 3 : 2, so.maxTileX - so.minTileX + 1);
                    assertEquals("y span", sideways ? 2 : 3, so.maxTileY - so.minTileY + 1);
                }
            }
        }
    }

    private static int[] corners(Animable a) throws Exception {
        return new int[] {sub5(a, "heightX0Y0"), sub5(a, "heightX1Y0"), sub5(a, "heightX1Y1"), sub5(a, "heightX0Y1")}; // x0y0 x1y0 x1y1 x0y1
    }

    @Test public void adjustsToTerrainRotatesTheFourCornerHeightsOneStepPerOrientation() throws Exception {
        int[][] expected = {{10, 20, 30, 40}, {40, 10, 20, 30}, {30, 40, 10, 20}, {20, 30, 40, 10}};
        for (boolean twin : new boolean[] {false, true}) {
            for (int o = 0; o < 4; o++) {
                for (boolean adjusts : new boolean[] {true, false}) {
                    ObjectDef def = newDef(1);
                    def.adjustsToTerrain = adjusts;
                    install(def);
                    ObjectManager m = manager();
                    int[][][] h = heights(m);
                    h[0][30][30] = 10;
                    h[0][31][30] = 20;
                    h[0][31][31] = 30;
                    h[0][30][31] = 40;
                    Object2 d = ground(placeEither(twin, m, world(), 4, 30, 30, 1, o), 0, 30, 30).obj2;
                    assertArrayEquals("twin " + twin + " o " + o + " adjusts " + adjusts, adjusts ? expected[o] : expected[0], corners(d.model));
                }
            }
        }
    }

    @Test public void eachShapeBuildsItsModelWithTheMatchingTypeAndOrientationAndPassesItToTheMatchingAddCall() throws Exception {
        // shape -> {model type, model orientation for o=2}
        int[][] cases = {{22, 22, 2}, {10, 10, 2}, {11, 10, 2}, {12, 12, 2}, {13, 13, 2}, {0, 0, 2}, {1, 1, 2}, {3, 3, 2},
                {4, 4, 0}, {5, 4, 0}, {6, 4, 0}, {7, 4, 0}, {8, 4, 0}, {9, 9, 2}};
        for (boolean twin : new boolean[] {false, true}) {
            for (int[] c : cases) {
                install(newDef(1));
                WorldController w = placeEither(twin, manager(), world(), c[0], 40, 40, 1, 2);
                Ground g = ground(w, 0, 40, 40);
                Animable model;
                switch (c[0]) {
                    case 22: model = g.obj3.model; break;
                    case 0: case 1: case 3: model = g.obj1.model; break;
                    case 4: case 5: case 6: case 7: case 8: model = g.obj2.model; break;
                    default: model = g.obj5Array[0].model; break;
                }
                String label = "shape " + c[0] + " twin " + twin;
                assertEquals(label, c[1], sub5(model, "type"));
                assertEquals(label, c[2], sub5(model, "orientation"));
            }
        }
    }
}
