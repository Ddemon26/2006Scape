import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** Original-name semantic tests for WorldController createPlaneTiles-placeSceneObject (scene add/place routines). */
public class WorldControllerPlacementTest {
    private static final int PLANES = 4;
    private static final int SIZE = 104;
    private static final int OBJ_KIND = 2 << 29;

    private int savedDrawDistance;
    private Object savedTable, savedCounts, savedLists;

    @Before public void save() throws Exception {
        savedDrawDistance = WorldController.drawDistance;
        savedTable = sget("tileVisibilityTable");
        savedCounts = sget("occluderCounts");
        savedLists = sget("occluders");
        sset("occluderCounts", new int[4]);
        sset("occluders", new Class47[4][500]);
        sset("aClass28Array462", new Object5[100]);
    }

    @After public void restore() throws Exception {
        WorldController.drawDistance = savedDrawDistance;
        sset("tileVisibilityTable", savedTable);
        sset("occluderCounts", savedCounts);
        sset("occluders", savedLists);
        sset("cameraTileVisibility", null);
        sset("aBoolean467", false);
    }

    // ---- reflection helpers (original names only) ----
    private static Field field(Class<?> c, String n) throws Exception { Field f = c.getDeclaredField(n); f.setAccessible(true); return f; }
    private static Object get(Object o, String n) throws Exception { return field(o.getClass(), n).get(o); }
    private static int geti(Object o, String n) throws Exception { return (Integer) get(o, n); }
    private static void set(Object o, String n, Object v) throws Exception { field(o.getClass(), n).set(o, v); }
    private static Object sget(String n) throws Exception { return field(WorldController.class, n).get(null); }
    private static void sset(String n, Object v) throws Exception { field(WorldController.class, n).set(null, v); }
    private static int sint(String n) throws Exception { return (Integer) sget(n); }
    private static Object call(Object target, String name, Class<?>[] types, Object... args) throws Exception {
        Method m = WorldController.class.getDeclaredMethod(name, types);
        m.setAccessible(true);
        try { return m.invoke(target, args); } catch (InvocationTargetException e) { if (e.getCause() instanceof Exception) throw (Exception) e.getCause(); throw e; }
    }

    private static WorldController scene() throws Exception { return new WorldController(new int[PLANES][SIZE + 1][SIZE + 1]); }
    private static Ground[][][] tiles(WorldController wc) throws Exception { return (Ground[][][]) get(wc, "groundArray"); }
    private static Ground at(WorldController wc, int plane, int x, int y) throws Exception { return tiles(wc)[plane][x][y]; }
    private static Object5[] cache(WorldController wc) throws Exception { return (Object5[]) get(wc, "obj5Cache"); }

    private static Model model() throws Exception {
        java.lang.reflect.Constructor<Model> c = Model.class.getDeclaredConstructor();
        c.setAccessible(true);
        return c.newInstance();
    }

    private static Model modelWithHeight(int h) throws Exception { Model m = model(); m.anInt1654 = h; return m; }

    // ================= addOccluder: occluder list =================

    @Test public void method277StoresOccluderPerPlaneWithTileBoundsAndWorldBounds() throws Exception {
        WorldController.addOccluder(2, 1300, -500, 1700, 2900, -900, 2200, 1);
        WorldController.addOccluder(2, 128, 7, 256, 384, 8, 512, 4);
        WorldController.addOccluder(0, 640, 1, 640, 768, 2, 700, 2);
        int[] counts = (int[]) sget("occluderCounts");
        assertArrayEquals(new int[] {1, 0, 2, 0}, counts);
        Class47[][] lists = (Class47[][]) sget("occluders");
        Class47 o = lists[2][0];
        // tile bounds are j/128, l/128, l1/128, i1/128
        assertEquals(1300 / 128, o.anInt787); assertEquals(1700 / 128, o.anInt788);
        assertEquals(2200 / 128, o.anInt789); assertEquals(2900 / 128, o.anInt790);
        assertEquals(1, o.anInt791);
        assertEquals(1300, o.anInt792); assertEquals(1700, o.anInt793);
        assertEquals(2200, o.anInt794); assertEquals(2900, o.anInt795);
        assertEquals(-900, o.anInt796); assertEquals(-500, o.anInt797);
        assertEquals(4, lists[2][1].anInt791);
        assertEquals(2, lists[0][0].anInt791); assertEquals(640, lists[0][0].anInt792); assertEquals(700, lists[0][0].anInt794);
        assertNull(lists[1][0]); assertNull(lists[3][0]);
    }

    private static final Class<?>[] M319 = {};
    private static final Class<?>[] M324 = {int.class, int.class, int.class};

    private static void camera(WorldController wc, int plane, int camX, int camHeight, int camY) throws Exception {
        sset("anInt447", plane);
        sset("anInt453", camX / 128); sset("anInt454", camY / 128);
        sset("anInt455", camX); sset("anInt456", camHeight); sset("anInt457", camY);
        boolean[][] vis = new boolean[51][51];
        for (boolean[] r : vis) java.util.Arrays.fill(r, true);
        sset("cameraTileVisibility", vis);
        WorldController.drawDistance = 25;
        call(wc, "method319", M319);
    }

    private static boolean blocked(WorldController wc, int x, int height, int y) throws Exception { return (Boolean) call(wc, "method324", M324, x, height, y); }

    @Test public void method277Type1IsAnXFixedWallHiddenBehindItsRangeInYAndHeight() throws Exception {
        WorldController wc = scene();
        // fixed x = 1280; y range 2560..2944 (arguments l1 and i1); height range -300..-100 (arguments j1 and k)
        WorldController.addOccluder(0, 1280, -100, 1280, 2944, -300, 2560, 1);
        camera(wc, 0, 1280 + 200, -200, 2752);
        assertTrue(blocked(wc, 1280 - 100, -200, 2752));
        assertFalse("in front of the wall (camera side)", blocked(wc, 1280 + 100, -200, 2752));
        assertFalse("y far outside", blocked(wc, 1280 - 100, -200, 2752 + 2000));
        assertFalse("height above range", blocked(wc, 1280 - 100, -500, 2752));
        assertFalse("height below range", blocked(wc, 1280 - 100, 100, 2752));
        // a camera on the other side of the wall hides what is on the camera side's far side instead
        camera(wc, 0, 1280 - 200, -200, 2752);
        assertTrue(blocked(wc, 1280 + 100, -200, 2752));
        assertFalse(blocked(wc, 1280 - 300, -200, 2752));
        // a camera within 32 units of the wall plane makes no occluder
        camera(wc, 0, 1280 + 20, -200, 2752);
        assertFalse(blocked(wc, 1280 - 100, -200, 2752));
        // occluder entries of other planes are ignored
        WorldController wc2 = scene();
        sset("occluderCounts", new int[4]); sset("occluders", new Class47[4][500]);
        WorldController.addOccluder(1, 1280, -100, 1280, 2944, -300, 2560, 1);
        camera(wc2, 0, 1280 + 200, -200, 2752);
        assertFalse(blocked(wc2, 1280 - 100, -200, 2752));
    }

    @Test public void method277Type2IsAYFixedWallHiddenBehindItsRangeInXAndHeight() throws Exception {
        WorldController wc = scene();
        // fixed y = 2560 (l1 and i1 equal); x range 1280..1664 (arguments j and l); height -300..-100
        WorldController.addOccluder(0, 1280, -100, 1664, 2560, -300, 2560, 2);
        camera(wc, 0, 1472, -200, 2560 + 200);
        assertTrue(blocked(wc, 1472, -200, 2560 - 100));
        assertFalse(blocked(wc, 1472, -200, 2560 + 100));
        assertFalse("x far outside", blocked(wc, 1472 + 3000, -200, 2560 - 100));
        assertFalse("height outside", blocked(wc, 1472, -500, 2560 - 100));
    }

    // ================= createPlaneTiles / lowerColumnPlanes: plane preparation and column shift =================

    @Test public void method275CreatesAGroundForEveryTileOfThePlaneKeepsExistingOnesAndRecordsThePlane() throws Exception {
        WorldController wc = scene();
        Ground kept = new Ground(2, 5, 6);
        tiles(wc)[2][5][6] = kept;
        wc.createPlaneTiles(2);
        assertSame(kept, at(wc, 2, 5, 6));
        for (int x = 0; x < SIZE; x += 13) for (int y = 0; y < SIZE; y += 17) {
            Ground g = at(wc, 2, x, y);
            assertNotNull(g);
            if (g != kept) { assertEquals(x, g.anInt1308); assertEquals(y, g.anInt1309); assertEquals(2, g.anInt1307); }
        }
        assertNotNull(at(wc, 2, SIZE - 1, SIZE - 1));
        assertNull(at(wc, 1, 0, 0)); assertNull(at(wc, 3, 0, 0));
        assertEquals(2, geti(wc, "lowestPlane"));
        wc.createPlaneTiles(0);
        assertEquals(0, geti(wc, "lowestPlane"));
        assertNotNull(at(wc, 0, 3, 4));
    }

    @Test public void method276ShiftsOneColumnDownAKeepsOldGroundLinkedAndLowersAnchoredSceneObjects() throws Exception {
        WorldController wc = scene();
        Ground g0 = new Ground(0, 7, 9), g1 = new Ground(1, 7, 9), g2 = new Ground(2, 7, 9), g3 = new Ground(3, 7, 9);
        Ground[][][] t = tiles(wc);
        t[0][7][9] = g0; t[1][7][9] = g1; t[2][7][9] = g2; t[3][7][9] = g3;
        Ground neighbour = new Ground(1, 8, 9);
        t[1][8][9] = neighbour;
        Object5 anchored = obj5(1, 7, 7, 9, 9, OBJ_KIND | 5);     // kind 2, anchored here
        Object5 notAnchoredX = obj5(1, 6, 7, 9, 9, OBJ_KIND | 6);
        Object5 notAnchoredY = obj5(1, 7, 7, 8, 9, OBJ_KIND | 7);
        Object5 wrongKind = obj5(1, 7, 7, 9, 9, 1 << 29 | 8);
        g1.obj5Array[0] = anchored; g1.obj5Array[1] = notAnchoredX; g1.obj5Array[2] = notAnchoredY; g1.obj5Array[3] = wrongKind; g1.anInt1317 = 4;
        wc.lowerColumnPlanes(9, 7);   // (y, x)
        assertSame(g1, at(wc, 0, 7, 9)); assertSame(g2, at(wc, 1, 7, 9)); assertSame(g3, at(wc, 2, 7, 9));
        assertNull(at(wc, 3, 7, 9));
        assertEquals(0, g1.anInt1307); assertEquals(1, g2.anInt1307); assertEquals(2, g3.anInt1307);
        assertEquals(0, anchored.anInt517); assertEquals(1, notAnchoredX.anInt517);
        assertEquals(1, notAnchoredY.anInt517); assertEquals(1, wrongKind.anInt517);
        assertSame(g0, g1.aClass30_Sub3_1329);
        assertSame(neighbour, at(wc, 1, 8, 9));
        // empty column: a fresh plane-0 ground is created and still links the old plane-0 ground
        WorldController wc2 = scene();
        Ground only0 = new Ground(0, 3, 4);
        tiles(wc2)[0][3][4] = only0;
        wc2.lowerColumnPlanes(4, 3);
        Ground fresh = at(wc2, 0, 3, 4);
        assertNotNull(fresh); assertNotSame(only0, fresh);
        assertSame(only0, fresh.aClass30_Sub3_1329);
        assertEquals(3, fresh.anInt1308); assertEquals(4, fresh.anInt1309);
    }

    private static Object5 obj5(int plane, int x0, int x1, int y0, int y1, int uid) {
        Object5 o = new Object5();
        o.anInt517 = plane; o.anInt523 = x0; o.anInt524 = x1; o.anInt525 = y0; o.anInt526 = y1; o.uid = uid;
        return o;
    }

    // ================= setMinCameraPlane: lowest camera plane for a tile =================

    @Test public void method278SetsTheTilePlaneLimitWithArgumentOrderPlaneXYAndIgnoresMissingTiles() throws Exception {
        WorldController wc = scene();
        Ground g = new Ground(1, 12, 20);
        Ground decoyXY = new Ground(1, 20, 12), decoyPlane = new Ground(2, 12, 20);
        tiles(wc)[1][12][20] = g; tiles(wc)[1][20][12] = decoyXY; tiles(wc)[2][12][20] = decoyPlane;
        wc.setMinCameraPlane(1, 12, 20, 3);
        assertEquals(3, g.anInt1321); assertEquals(0, decoyXY.anInt1321); assertEquals(0, decoyPlane.anInt1321);
        wc.setMinCameraPlane(0, 50, 50, 2);    // no ground there: nothing is created
        assertNull(at(wc, 0, 50, 50));
    }

    @Test public void method278LimitHidesTheTileFromCamerasOnLowerPlanesButNotFromEqualOrHigherOnes() throws Exception {
        WorldController wc = scene();
        WorldController.drawDistance = 5;
        boolean[][][][] table = new boolean[8][32][256][256];
        for (boolean[][][] a : table) for (boolean[][] b : a) for (boolean[] c : b) java.util.Arrays.fill(c, true);
        sset("tileVisibilityTable", table);
        Ground g = new Ground(0, 42, 61);
        tiles(wc)[0][42][61] = g;
        wc.setMinCameraPlane(0, 42, 61, 2);
        g.aBoolean1324 = true;     // a drawn (visible) tile gets this flag recomputed to false; a hidden tile keeps it
        wc.drawScene(40 * 128 + 10, 60 * 128 + 10, 640, 0, 1, 160);
        assertTrue("camera plane 1 < limit 2: hidden", g.aBoolean1324);
        wc.drawScene(40 * 128 + 10, 60 * 128 + 10, 640, 0, 2, 160);
        assertFalse("camera plane 2 == limit: drawn", g.aBoolean1324);
        g.aBoolean1324 = true;
        wc.drawScene(40 * 128 + 10, 60 * 128 + 10, 640, 0, 3, 160);
        assertFalse("camera plane 3 > limit: drawn", g.aBoolean1324);
    }

    // ================= addTile: tile shape =================

    private static Object tileGround(WorldController wc, String slot, int plane, int x, int y) throws Exception { return get(at(wc, plane, x, y), slot); }

    @Test public void method279Type0MakesAPlainTileFromTheFirstFourColoursAndFillsLowerPlanes() throws Exception {
        WorldController wc = scene();
        wc.addTile(2, 30, 40, 0, 0, 99, 1, 1, 1, 1, 101, 102, 103, 104, 201, 202, 203, 204, 55, 66);
        Class43 c = (Class43) tileGround(wc, "aClass43_1311", 2, 30, 40);
        assertEquals(101, c.anInt716); assertEquals(102, c.anInt717); assertEquals(103, c.anInt718); assertEquals(104, c.anInt719);
        assertEquals(-1, c.anInt720);
        assertEquals(55, c.anInt722);
        assertFalse(c.aBoolean721);
        assertNull(tileGround(wc, "aClass40_1312", 2, 30, 40));
        assertNotNull(at(wc, 1, 30, 40)); assertNotNull(at(wc, 0, 30, 40));
        assertNull(at(wc, 3, 30, 40)); assertNull(at(wc, 2, 40, 30)); assertNull(at(wc, 2, 31, 40));
        assertNull(tileGround(wc, "aClass43_1311", 1, 30, 40));
    }

    @Test public void method279Type1MakesAPlainTileFromTheSecondFourColoursWithTextureAndFlatFlagFromTheHeights() throws Exception {
        WorldController wc = scene();
        wc.addTile(1, 31, 41, 1, 0, 7, 100, 100, 100, 100, 101, 102, 103, 104, 201, 202, 203, 204, 55, 66);
        Class43 c = (Class43) tileGround(wc, "aClass43_1311", 1, 31, 41);
        assertEquals(201, c.anInt716); assertEquals(202, c.anInt717); assertEquals(203, c.anInt718); assertEquals(204, c.anInt719);
        assertEquals(7, c.anInt720);
        assertEquals(66, c.anInt722);
        assertTrue("all four heights equal", c.aBoolean721);
        for (int which = 0; which < 4; which++) {
            int[] h = {100, 100, 100, 100};
            h[which] = 101;
            WorldController w = scene();
            w.addTile(0, 3, 4, 1, 0, 7, h[0], h[1], h[2], h[3], 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
            assertFalse("height " + which + " differs", ((Class43) tileGround(w, "aClass43_1311", 0, 3, 4)).aBoolean721);
        }
    }

    private static int[] vertex(Class40 c, int index) throws Exception {
        return new int[] {c.anIntArray673[index], c.anIntArray675[index], c.anIntArray674[index]};
    }

    private static int vertexAt(Class40 c, int x, int y) {
        for (int i = 0; i < c.anIntArray673.length; i++) if (c.anIntArray673[i] == x && c.anIntArray675[i] == y) return i;
        return -1;
    }

    @Test public void method279ShapedTileHasCornersAtTileOriginPlusXPlusYWithHeightsInOrderAndColourGroups() throws Exception {
        WorldController wc = scene();
        // shape 2 (argument l), rotation 0 (i1), texture 9 (j1)
        wc.addTile(1, 5, 6, 2, 0, 9, 1001, 1002, 1003, 1004, 11, 12, 13, 14, 21, 22, 23, 24, 77, 88);
        Class40 c = (Class40) tileGround(wc, "aClass40_1312", 1, 5, 6);
        assertNull(tileGround(wc, "aClass43_1311", 1, 5, 6));
        assertEquals(4, c.anIntArray673.length);
        int x0 = 5 * 128, y0 = 6 * 128;
        int[] expectHeight = {1001, 1002, 1003, 1004};
        int[][] corners = {{x0, y0}, {x0 + 128, y0}, {x0 + 128, y0 + 128}, {x0, y0 + 128}};
        for (int i = 0; i < 4; i++) {
            int v = vertexAt(c, corners[i][0], corners[i][1]);
            assertTrue("corner " + i, v >= 0);
            assertEquals(expectHeight[i], c.anIntArray674[v]);
            assertEquals(i, v);
        }
        // faces for shape 2: face 0 underlay on corners 1,2,3 with the first colour group; face 1 overlay on corners 0,1,3 with the second group and texture
        assertEquals(2, c.anInt684); assertEquals(0, c.anInt685); assertEquals(77, c.anInt686); assertEquals(88, c.anInt687);
        assertArrayEquals(new int[] {1, 0}, new int[] {c.anIntArray679[0], c.anIntArray679[1]});
        assertArrayEquals(new int[] {2, 1}, new int[] {c.anIntArray680[0], c.anIntArray680[1]});
        assertArrayEquals(new int[] {3, 3}, new int[] {c.anIntArray681[0], c.anIntArray681[1]});
        assertEquals(12, c.anIntArray676[0]); assertEquals(13, c.anIntArray677[0]); assertEquals(14, c.anIntArray678[0]);
        assertEquals(21, c.anIntArray676[1]); assertEquals(22, c.anIntArray677[1]); assertEquals(24, c.anIntArray678[1]);
        assertEquals(-1, c.anIntArray682[0]); assertEquals(9, c.anIntArray682[1]);
        assertNotNull(at(wc, 0, 5, 6));
    }

    @Test public void method279ShapedTileRotationTurnsFaceCornersByQuarterTurnsAndTextureMinusOneHasNoTextureArray() throws Exception {
        for (int rot = 0; rot < 4; rot++) {
            WorldController wc = scene();
            wc.addTile(0, 5, 6, 2, rot, -1, 1001, 1002, 1003, 1004, 11, 12, 13, 14, 21, 22, 23, 24, 77, 88);
            Class40 c = (Class40) tileGround(wc, "aClass40_1312", 0, 5, 6);
            assertEquals(rot, c.anInt685);
            assertEquals((1 - rot) & 3, c.anIntArray679[0]); assertEquals((2 - rot) & 3, c.anIntArray680[0]); assertEquals((3 - rot) & 3, c.anIntArray681[0]);
            assertNull(c.anIntArray682);
        }
    }

    // ================= addGroundDecoration-addWallDecoration: object slots =================

    @Test public void method280PlacesGroundDecorationAtTileCentreWithHeightUidAndInfoOnlyOnThatPlane() throws Exception {
        WorldController wc = scene();
        Model m = model();
        wc.addGroundDecoration(2, -250, 21, m, (byte) 0x85, 4444, 33);   // plane, height, y, model, info, uid, x
        Object3 d = at(wc, 2, 33, 21).obj3;
        assertSame(m, d.aClass30_Sub2_Sub4_814);
        assertEquals(33 * 128 + 64, d.anInt812); assertEquals(21 * 128 + 64, d.anInt813);
        assertEquals(-250, d.anInt811); assertEquals(4444, d.uid); assertEquals((byte) 0x85, d.aByte816);
        assertNull(at(wc, 1, 33, 21));          // lower planes are not created
        assertEquals(4444, wc.getGroundDecorationUid(2, 33, 21));
        assertEquals(0x85, wc.getObjectInfo(2, 33, 21, 4444));
        // a null model changes nothing
        wc.addGroundDecoration(2, 5, 22, null, (byte) 1, 1, 33);
        assertNull(at(wc, 2, 33, 22));
        // a second call replaces the decoration
        Model m2 = model();
        wc.addGroundDecoration(2, 7, 21, m2, (byte) 2, 9, 33);
        assertSame(m2, at(wc, 2, 33, 21).obj3.aClass30_Sub2_Sub4_814);
    }

    @Test public void method281BuildsItemStackAtTileCentreWithHeightAndTallestObjectModelOnTheTile() throws Exception {
        WorldController wc = scene();
        Model top = model(), a = model(), b = model();
        // arguments: x, uid, model stored in 49, height, model stored in 50, model stored in 48, plane, y
        wc.addItemStack(14, 9090, a, -64, b, top, 1, 27);
        Object4 s = at(wc, 1, 14, 27).obj4;
        assertSame(top, s.aClass30_Sub2_Sub4_48); assertSame(a, s.aClass30_Sub2_Sub4_49); assertSame(b, s.aClass30_Sub2_Sub4_50);
        assertEquals(14 * 128 + 64, s.anInt46); assertEquals(27 * 128 + 64, s.anInt47);
        assertEquals(-64, s.anInt45); assertEquals(9090, s.uid);
        assertEquals(0, s.anInt52);
        // tallest scene-object model on the tile; non-model objects and other tiles/planes are ignored
        Ground tile = at(wc, 1, 15, 27);
        tile = new Ground(1, 15, 27); tiles(wc)[1][15][27] = tile;
        Object5 o1 = obj5(1, 15, 15, 27, 27, 1), o2 = obj5(1, 15, 15, 27, 27, 2), o3 = obj5(1, 15, 15, 27, 27, 3);
        o1.aClass30_Sub2_Sub4_521 = modelWithHeight(5);
        o2.aClass30_Sub2_Sub4_521 = modelWithHeight(12);
        o3.aClass30_Sub2_Sub4_521 = new Animable();
        tile.obj5Array[0] = o1; tile.obj5Array[1] = o3; tile.obj5Array[2] = o2; tile.anInt1317 = 3;
        Ground otherTile = new Ground(1, 16, 27); tiles(wc)[1][16][27] = otherTile;
        Object5 big = obj5(1, 16, 16, 27, 27, 4); big.aClass30_Sub2_Sub4_521 = modelWithHeight(99);
        otherTile.obj5Array[0] = big; otherTile.anInt1317 = 1;
        wc.addItemStack(15, 1, null, 0, null, top, 1, 27);
        assertEquals(12, at(wc, 1, 15, 27).obj4.anInt52);
        assertNull(at(wc, 1, 15, 27).obj4.aClass30_Sub2_Sub4_49);
        wc.addItemStack(15, 1, null, 0, null, top, 0, 27);
        assertEquals(0, at(wc, 0, 15, 27).obj4.anInt52);
    }

    @Test public void method282BuildsWallAtTileCentreWithTwoModelsOrientationsAndFillsLowerPlanes() throws Exception {
        WorldController wc = scene();
        Model a = model(), b = model();
        // arguments: orientation, model, uid, y, info, x, secondModel, height, secondOrientation, plane
        wc.addWall(4, a, 777, 19, (byte) 0x83, 8, b, -120, 16, 2);
        Object1 w = at(wc, 2, 8, 19).obj1;
        assertSame(a, w.aClass30_Sub2_Sub4_278); assertSame(b, w.aClass30_Sub2_Sub4_279);
        assertEquals(8 * 128 + 64, w.anInt274); assertEquals(19 * 128 + 64, w.anInt275);
        assertEquals(-120, w.anInt273);
        assertEquals(4, w.orientation); assertEquals(16, w.orientation1);
        assertEquals(777, w.uid); assertEquals((byte) 0x83, w.aByte281);
        assertNotNull(at(wc, 1, 8, 19)); assertNotNull(at(wc, 0, 8, 19)); assertNull(at(wc, 3, 8, 19));
        assertEquals(777, wc.getWallUid(2, 8, 19));
        assertEquals(0x83, wc.getObjectInfo(2, 8, 19, 777));
        // only one model: still placed; none: nothing happens
        wc.addWall(1, null, 5, 20, (byte) 1, 8, b, 0, 2, 0);
        assertSame(b, at(wc, 0, 8, 20).obj1.aClass30_Sub2_Sub4_279); assertNull(at(wc, 0, 8, 20).obj1.aClass30_Sub2_Sub4_278);
        wc.addWall(1, null, 5, 21, (byte) 1, 8, null, 0, 2, 0);
        assertNull(at(wc, 0, 8, 21));
    }

    @Test public void method283BuildsWallDecorationWithOffsetsHeightRotationAndMaskAndFillsLowerPlanes() throws Exception {
        WorldController wc = scene();
        Model m = model();
        // arguments: uid, y, rotation, plane, xOffset, height, model, x, info, yOffset, mask
        wc.addWallDecoration(6060, 23, 3, 2, 17, -90, m, 10, (byte) 0x84, -29, 0x300);
        Object2 d = at(wc, 2, 10, 23).obj2;
        assertSame(m, d.aClass30_Sub2_Sub4_504);
        assertEquals(10 * 128 + 64 + 17, d.anInt500); assertEquals(23 * 128 + 64 - 29, d.anInt501);
        assertEquals(-90, d.anInt499); assertEquals(3, d.anInt503); assertEquals(0x300, d.anInt502);
        assertEquals(6060, d.uid); assertEquals((byte) 0x84, d.aByte506);
        assertNotNull(at(wc, 0, 10, 23)); assertNull(at(wc, 3, 10, 23));
        assertEquals(6060, wc.getWallDecorationUid(2, 10, 23));
        assertEquals(0x84, wc.getObjectInfo(2, 10, 23, 6060));
        wc.addWallDecoration(1, 24, 0, 2, 0, 0, null, 10, (byte) 0, 0, 0);
        assertNull(at(wc, 2, 10, 24));
    }

    // ================= addSceneObject-placeSceneObject: scene objects =================

    private static final Class<?>[] M287 = {int.class, int.class, int.class, int.class, int.class, int.class, int.class, int.class, Animable.class, int.class, boolean.class, int.class, byte.class};

    @Test public void method284PlacesSceneObjectOverItsFootprintCentredOnTheFootprintAndIsNotCached() throws Exception {
        WorldController wc = scene();
        Model m = model();
        // arguments: uid, info, height, sizeY, model, sizeX, plane, rotation, y, x
        assertTrue(wc.addSceneObject(31337, (byte) 0x82, -77, 3, m, 2, 1, 1024, 40, 50));
        Object5 o = at(wc, 1, 50, 40).obj5Array[0];
        assertSame(m, o.aClass30_Sub2_Sub4_521);
        assertEquals(50 * 128 + 64 * 2, o.anInt519); assertEquals(40 * 128 + 64 * 3, o.anInt520);
        assertEquals(-77, o.anInt518); assertEquals(1024, o.anInt522); assertEquals(1, o.anInt517);
        assertEquals(31337, o.uid); assertEquals((byte) 0x82, o.aByte530);
        assertEquals(50, o.anInt523); assertEquals(51, o.anInt524); assertEquals(40, o.anInt525); assertEquals(42, o.anInt526);
        for (int x = 50; x <= 51; x++) for (int y = 40; y <= 42; y++) assertSame(o, at(wc, 1, x, y).obj5Array[0]);
        assertNull(at(wc, 1, 52, 40)); assertNull(at(wc, 1, 50, 43));
        assertEquals(0, geti(wc, "obj5CacheCurrPos"));
        assertEquals(0, wc.getSceneObjectUid(1, 50, 40));   // not kind 2
        assertTrue(wc.addSceneObject(1, (byte) 0, 0, 1, null, 1, 1, 0, 0, 0));
        assertNull(at(wc, 1, 0, 0));
    }

    @Test public void method287RejectsFootprintsOutsideTheMapOrOverFiveObjectsWithoutChangingAnything() throws Exception {
        WorldController wc = scene();
        Model m = model();
        Object[] ok = {1, 100, 100, 2, 2, 0, 0, 0, m, 0, false, 5, (byte) 0};
        assertTrue((Boolean) call(wc, "placeSceneObject", M287, ok));
        // x/y past the end: 103 fits as a single tile, 104 does not; a 2-wide footprint at 103 does not
        assertTrue((Boolean) call(wc, "placeSceneObject", M287, 0, 103, 103, 1, 1, 0, 0, 0, m, 0, false, 5, (byte) 0));
        assertFalse((Boolean) call(wc, "placeSceneObject", M287, 0, 104, 10, 1, 1, 0, 0, 0, m, 0, false, 5, (byte) 0));
        assertFalse((Boolean) call(wc, "placeSceneObject", M287, 0, 10, 104, 1, 1, 0, 0, 0, m, 0, false, 5, (byte) 0));
        assertFalse((Boolean) call(wc, "placeSceneObject", M287, 0, 103, 10, 2, 1, 0, 0, 0, m, 0, false, 5, (byte) 0));
        assertFalse((Boolean) call(wc, "placeSceneObject", M287, 0, 10, 103, 1, 2, 0, 0, 0, m, 0, false, 5, (byte) 0));
        assertFalse((Boolean) call(wc, "placeSceneObject", M287, 0, -1, 10, 1, 1, 0, 0, 0, m, 0, false, 5, (byte) 0));
        assertFalse((Boolean) call(wc, "placeSceneObject", M287, 0, 10, -1, 1, 1, 0, 0, 0, m, 0, false, 5, (byte) 0));
        assertNull(at(wc, 0, 103, 10)); assertNull(at(wc, 0, 10, 103)); assertNull(at(wc, 0, 0, 10));
        // five objects on a tile is the limit: the sixth object is refused even when its other tiles are free
        for (int i = 0; i < 5; i++) assertTrue((Boolean) call(wc, "placeSceneObject", M287, 0, 60, 60, 1, 1, 0, 0, 0, m, 0, false, 10 + i, (byte) 0));
        assertEquals(5, at(wc, 0, 60, 60).anInt1317);
        assertFalse((Boolean) call(wc, "placeSceneObject", M287, 0, 59, 60, 2, 1, 0, 0, 0, m, 0, false, 99, (byte) 0));
        assertNull(at(wc, 0, 59, 60));
        assertEquals(5, at(wc, 0, 60, 60).anInt1317);
        // the count is per plane
        assertTrue((Boolean) call(wc, "placeSceneObject", M287, 1, 60, 60, 1, 1, 0, 0, 0, m, 0, false, 98, (byte) 0));
    }

    @Test public void method287StoresObjectFieldsEdgeMasksAndRegistersInCacheOnlyWhenFlagged() throws Exception {
        WorldController wc = scene();
        Model m = model();
        // arguments: plane, tileX, tileY, sizeX, sizeY, x, y, height, model, rotation, cache flag, uid, info
        assertTrue((Boolean) call(wc, "placeSceneObject", M287, 2, 20, 30, 3, 2, 3333, 4444, -555, m, 1536, true, 66, (byte) 9));
        Object5 o = at(wc, 2, 20, 30).obj5Array[0];
        assertEquals(2, o.anInt517); assertEquals(3333, o.anInt519); assertEquals(4444, o.anInt520); assertEquals(-555, o.anInt518);
        assertSame(m, o.aClass30_Sub2_Sub4_521); assertEquals(1536, o.anInt522); assertEquals(66, o.uid); assertEquals((byte) 9, o.aByte530);
        assertEquals(20, o.anInt523); assertEquals(22, o.anInt524); assertEquals(30, o.anInt525); assertEquals(31, o.anInt526);
        // per-tile edge masks: +1 has a tile before in x, +4 has a tile after in x, +8 before in y, +2 after in y
        int[][] expect = {{2 + 4, 2 + 1 + 4, 2 + 1}, {8 + 4, 8 + 1 + 4, 8 + 1}};
        for (int y = 0; y < 2; y++) for (int x = 0; x < 3; x++) {
            Ground g = at(wc, 2, 20 + x, 30 + y);
            assertEquals(1, g.anInt1317);
            assertEquals("mask x" + x + " y" + y, expect[y][x], g.anIntArray1319[0]);
            assertEquals(expect[y][x], g.anInt1320);
            assertNotNull(at(wc, 1, 20 + x, 30 + y)); assertNotNull(at(wc, 0, 20 + x, 30 + y)); assertNull(at(wc, 3, 20 + x, 30 + y));
        }
        assertEquals(1, geti(wc, "obj5CacheCurrPos"));
        assertSame(o, cache(wc)[0]);
        // single tile object has no edge bits; masks of several objects are OR-ed
        assertTrue((Boolean) call(wc, "placeSceneObject", M287, 2, 21, 30, 1, 1, 0, 0, 0, m, 0, false, 67, (byte) 0));
        Ground g = at(wc, 2, 21, 30);
        assertEquals(2, g.anInt1317); assertEquals(0, g.anIntArray1319[1]); assertEquals(4 + 1 + 2, g.anInt1320);
        assertEquals(1, geti(wc, "obj5CacheCurrPos"));
    }

    @Test public void method285PlacesEntityWithSquareRadiusFootprintAndYawDependentExtensionOnlyWhenFlagged() throws Exception {
        // arguments: plane, yaw, height, uid, y, radius, x, model, extend flag
        WorldController wc = scene();
        Model m = model();
        assertTrue(wc.addEntity(0, 0, -33, 808, 2000, 60, 1000, m, false));
        Object5 o = at(wc, 0, 7, 15).obj5Array[0];
        assertEquals(1000, o.anInt519); assertEquals(2000, o.anInt520); assertEquals(-33, o.anInt518);
        assertEquals(0, o.anInt522); assertEquals(808, o.uid); assertEquals((byte) 0, o.aByte530);
        assertEquals(7, o.anInt523); assertEquals(8, o.anInt524); assertEquals(15, o.anInt525); assertEquals(16, o.anInt526);
        assertEquals(1, geti(wc, "obj5CacheCurrPos"));
        assertSame(o, cache(wc)[0]);
        // extension: yaw bands add one tile on the maximum y (641..1407), maximum x (1153..1919), minimum y (>1664 or <384), minimum x (129..895)
        int[][] cases = {
            // yaw, minX, maxX, minY, maxY (tile coordinates), radius 60 at x=1000 (7..8), y=2000 (15..16)
            {0, 7, 8, 14, 16}, {128, 7, 8, 14, 16}, {129, 6, 8, 14, 16}, {383, 6, 8, 14, 16}, {384, 6, 8, 15, 16},
            {640, 6, 8, 15, 16}, {641, 6, 8, 15, 17}, {895, 6, 8, 15, 17}, {896, 7, 8, 15, 17}, {1152, 7, 8, 15, 17},
            {1153, 7, 9, 15, 17}, {1407, 7, 9, 15, 17}, {1408, 7, 9, 15, 16}, {1664, 7, 9, 15, 16}, {1665, 7, 9, 14, 16},
            {1919, 7, 9, 14, 16}, {1920, 7, 8, 14, 16}};
        for (int[] c : cases) {
            WorldController w = scene();
            assertTrue(w.addEntity(1, c[0], 0, 1, 2000, 60, 1000, m, true));
            Object5 e = at(w, 1, 8, 15) != null ? at(w, 1, 8, 15).obj5Array[0] : null;
            if (e == null) e = at(w, 1, 7, 15).obj5Array[0];
            assertEquals("yaw " + c[0] + " minX", c[1], e.anInt523); assertEquals("yaw " + c[0] + " maxX", c[2], e.anInt524);
            assertEquals("yaw " + c[0] + " minY", c[3], e.anInt525); assertEquals("yaw " + c[0] + " maxY", c[4], e.anInt526);
            assertEquals(c[0], e.anInt522);
            WorldController w2 = scene();
            assertTrue(w2.addEntity(1, c[0], 0, 1, 2000, 60, 1000, m, false));
            Object5 f = at(w2, 1, 7, 15).obj5Array[0];
            assertEquals(7, f.anInt523); assertEquals(8, f.anInt524); assertEquals(15, f.anInt525); assertEquals(16, f.anInt526);
        }
        assertTrue(wc.addEntity(0, 0, 0, 1, 0, 60, 0, null, false));   // null model: true, nothing placed
        // false when the footprint leaves the map
        assertFalse(wc.addEntity(0, 0, 0, 1, 2000, 60, -200, m, false));
    }

    @Test public void method286PlacesEntityOverGivenTileBoundsAtItsOwnPosition() throws Exception {
        WorldController wc = scene();
        Model m = model();
        // arguments: plane, y, model, yaw, maxTileY, x, height, minTileX, maxTileX, uid, minTileY
        assertTrue(wc.addEntityInTileBounds(1, 4444, m, 512, 33, 3333, -44, 24, 26, 909, 31));
        Object5 o = at(wc, 1, 24, 31).obj5Array[0];
        assertEquals(24, o.anInt523); assertEquals(26, o.anInt524); assertEquals(31, o.anInt525); assertEquals(33, o.anInt526);
        assertEquals(3333, o.anInt519); assertEquals(4444, o.anInt520); assertEquals(-44, o.anInt518);
        assertEquals(512, o.anInt522); assertEquals(909, o.uid); assertEquals(1, o.anInt517);
        assertSame(m, o.aClass30_Sub2_Sub4_521);
        assertSame(o, at(wc, 1, 26, 33).obj5Array[0]);
        assertEquals(1, geti(wc, "obj5CacheCurrPos"));
        assertTrue(wc.addEntityInTileBounds(1, 0, null, 0, 0, 0, 0, 0, 0, 0, 0));
        // out of range
        assertFalse(wc.addEntityInTileBounds(1, 0, m, 0, 40, 0, 0, 100, 104, 1, 38));
    }

    // ================= shared scene dimension fields =================

    @Test public void sceneDimensionFieldsAreThePlaneCountTileCountsLowestPlaneAndTileHeightsArray() throws Exception {
        int[][][] heights = new int[PLANES][SIZE + 1][SIZE + 1];
        WorldController wc = new WorldController(heights);
        assertEquals(4, geti(wc, "planeCount"));
        assertEquals(tiles(wc).length, geti(wc, "planeCount"));
        assertEquals(tiles(wc)[0].length, geti(wc, "tileCountX"));
        assertEquals(tiles(wc)[0][0].length, geti(wc, "tileCountY"));
        assertSame(heights, get(wc, "tileHeights"));
        // initToNull clears every plane (437) and every tile of both axes (438 = x extent, 439 = y extent)
        for (int p = 0; p < PLANES; p++) tiles(wc)[p][SIZE - 1][0] = new Ground(p, SIZE - 1, 0);
        for (int p = 0; p < PLANES; p++) tiles(wc)[p][0][SIZE - 1] = new Ground(p, 0, SIZE - 1);
        wc.initToNull();
        for (int p = 0; p < PLANES; p++) { assertNull(at(wc, p, SIZE - 1, 0)); assertNull(at(wc, p, 0, SIZE - 1)); }
        // the tile-height array is read at [plane][x][y] (x = second index) by the visibility test in drawScene
        WorldController.drawDistance = 5;
        boolean[][][][] table = new boolean[8][32][256][256];
        sset("tileVisibilityTable", table);
        Ground g = new Ground(0, 43, 60); tiles(wc)[0][43][60] = g; g.aBoolean1324 = true;
        Ground swapped = new Ground(0, 60, 43); tiles(wc)[0][60][43] = swapped; swapped.aBoolean1324 = true;
        heights[0][43][60] = 500;
        wc.drawScene(40 * 128 + 10, 60 * 128 + 10, 640, 0, 2, 160);
        assertFalse("500 above the camera: visible although outside the table", g.aBoolean1324);
        assertTrue("terrain height of the swapped axes is 0: hidden", swapped.aBoolean1324);
        // the lowest plane limits which planes drawScene considers
        table[1][10][4 + 5][0 + 5] = true;
        Ground low = new Ground(1, 44, 60); tiles(wc)[1][44][60] = low; low.aBoolean1324 = true;
        Ground high = new Ground(2, 44, 60); tiles(wc)[2][44][60] = high; high.aBoolean1324 = true;
        wc.createPlaneTiles(2);
        tiles(wc)[1][44][60] = low; tiles(wc)[2][44][60] = high;
        wc.drawScene(40 * 128 + 10, 60 * 128 + 10, 640, 0, 3, 160);
        assertTrue("plane below the lowest plane is not processed", low.aBoolean1324);
        assertFalse("lowest plane is processed", high.aBoolean1324);
    }
}
