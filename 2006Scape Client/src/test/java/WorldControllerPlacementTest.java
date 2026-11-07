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
        sset("tilePickRequested", false);
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
        assertEquals(1300 / 128, o.minTileX); assertEquals(1700 / 128, o.maxTileX);
        assertEquals(2200 / 128, o.minTileY); assertEquals(2900 / 128, o.maxTileY);
        assertEquals(1, o.type);
        assertEquals(1300, o.minX); assertEquals(1700, o.maxX);
        assertEquals(2200, o.minY); assertEquals(2900, o.maxY);
        assertEquals(-900, o.minHeight); assertEquals(-500, o.maxHeight);
        assertEquals(4, lists[2][1].type);
        assertEquals(2, lists[0][0].type); assertEquals(640, lists[0][0].minX); assertEquals(700, lists[0][0].minY);
        assertNull(lists[1][0]); assertNull(lists[3][0]);
    }

    private static final Class<?>[] M319 = {};
    private static final Class<?>[] M324 = {int.class, int.class, int.class};

    private static void camera(WorldController wc, int plane, int camX, int camHeight, int camY) throws Exception {
        sset("cameraPlane", plane);
        sset("cameraTileX", camX / 128); sset("cameraTileY", camY / 128);
        sset("cameraWorldX", camX); sset("cameraWorldHeight", camHeight); sset("cameraWorldY", camY);
        boolean[][] vis = new boolean[51][51];
        for (boolean[] r : vis) java.util.Arrays.fill(r, true);
        sset("cameraTileVisibility", vis);
        WorldController.drawDistance = 25;
        call(wc, "selectActiveOccluders", M319);
    }

    private static boolean blocked(WorldController wc, int x, int height, int y) throws Exception { return (Boolean) call(wc, "isPointOccluded", M324, x, height, y); }

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
            if (g != kept) { assertEquals(x, g.tileX); assertEquals(y, g.tileY); assertEquals(2, g.plane); }
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
        g1.obj5Array[0] = anchored; g1.obj5Array[1] = notAnchoredX; g1.obj5Array[2] = notAnchoredY; g1.obj5Array[3] = wrongKind; g1.objectCount = 4;
        wc.lowerColumnPlanes(9, 7);   // (y, x)
        assertSame(g1, at(wc, 0, 7, 9)); assertSame(g2, at(wc, 1, 7, 9)); assertSame(g3, at(wc, 2, 7, 9));
        assertNull(at(wc, 3, 7, 9));
        assertEquals(0, g1.plane); assertEquals(1, g2.plane); assertEquals(2, g3.plane);
        assertEquals(0, anchored.plane); assertEquals(1, notAnchoredX.plane);
        assertEquals(1, notAnchoredY.plane); assertEquals(1, wrongKind.plane);
        assertSame(g0, g1.lowerGround);
        assertSame(neighbour, at(wc, 1, 8, 9));
        // empty column: a fresh plane-0 ground is created and still links the old plane-0 ground
        WorldController wc2 = scene();
        Ground only0 = new Ground(0, 3, 4);
        tiles(wc2)[0][3][4] = only0;
        wc2.lowerColumnPlanes(4, 3);
        Ground fresh = at(wc2, 0, 3, 4);
        assertNotNull(fresh); assertNotSame(only0, fresh);
        assertSame(only0, fresh.lowerGround);
        assertEquals(3, fresh.tileX); assertEquals(4, fresh.tileY);
    }

    private static Object5 obj5(int plane, int x0, int x1, int y0, int y1, int uid) {
        Object5 o = new Object5();
        o.plane = plane; o.minTileX = x0; o.maxTileX = x1; o.minTileY = y0; o.maxTileY = y1; o.uid = uid;
        return o;
    }

    // ================= setMinCameraPlane: lowest camera plane for a tile =================

    @Test public void method278SetsTheTilePlaneLimitWithArgumentOrderPlaneXYAndIgnoresMissingTiles() throws Exception {
        WorldController wc = scene();
        Ground g = new Ground(1, 12, 20);
        Ground decoyXY = new Ground(1, 20, 12), decoyPlane = new Ground(2, 12, 20);
        tiles(wc)[1][12][20] = g; tiles(wc)[1][20][12] = decoyXY; tiles(wc)[2][12][20] = decoyPlane;
        wc.setMinCameraPlane(1, 12, 20, 3);
        assertEquals(3, g.minCameraPlane); assertEquals(0, decoyXY.minCameraPlane); assertEquals(0, decoyPlane.minCameraPlane);
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
        g.objectsPending = true;     // a drawn (visible) tile gets this flag recomputed to false; a hidden tile keeps it
        wc.drawScene(40 * 128 + 10, 60 * 128 + 10, 640, 0, 1, 160);
        assertTrue("camera plane 1 < limit 2: hidden", g.objectsPending);
        wc.drawScene(40 * 128 + 10, 60 * 128 + 10, 640, 0, 2, 160);
        assertFalse("camera plane 2 == limit: drawn", g.objectsPending);
        g.objectsPending = true;
        wc.drawScene(40 * 128 + 10, 60 * 128 + 10, 640, 0, 3, 160);
        assertFalse("camera plane 3 > limit: drawn", g.objectsPending);
    }

    // ================= addTile: tile shape =================

    private static Object tileGround(WorldController wc, String slot, int plane, int x, int y) throws Exception { return get(at(wc, plane, x, y), slot); }

    @Test public void method279Type0MakesAPlainTileFromTheFirstFourColoursAndFillsLowerPlanes() throws Exception {
        WorldController wc = scene();
        wc.addTile(2, 30, 40, 0, 0, 99, 1, 1, 1, 1, 101, 102, 103, 104, 201, 202, 203, 204, 55, 66);
        Class43 c = (Class43) tileGround(wc, "plainTile", 2, 30, 40);
        assertEquals(101, c.cornerColour0); assertEquals(102, c.cornerColour1); assertEquals(103, c.cornerColour2); assertEquals(104, c.cornerColour3);
        assertEquals(-1, c.textureId);
        assertEquals(55, c.mapColour);
        assertFalse(c.flat);
        assertNull(tileGround(wc, "shapedTile", 2, 30, 40));
        assertNotNull(at(wc, 1, 30, 40)); assertNotNull(at(wc, 0, 30, 40));
        assertNull(at(wc, 3, 30, 40)); assertNull(at(wc, 2, 40, 30)); assertNull(at(wc, 2, 31, 40));
        assertNull(tileGround(wc, "plainTile", 1, 30, 40));
    }

    @Test public void method279Type1MakesAPlainTileFromTheSecondFourColoursWithTextureAndFlatFlagFromTheHeights() throws Exception {
        WorldController wc = scene();
        wc.addTile(1, 31, 41, 1, 0, 7, 100, 100, 100, 100, 101, 102, 103, 104, 201, 202, 203, 204, 55, 66);
        Class43 c = (Class43) tileGround(wc, "plainTile", 1, 31, 41);
        assertEquals(201, c.cornerColour0); assertEquals(202, c.cornerColour1); assertEquals(203, c.cornerColour2); assertEquals(204, c.cornerColour3);
        assertEquals(7, c.textureId);
        assertEquals(66, c.mapColour);
        assertTrue("all four heights equal", c.flat);
        for (int which = 0; which < 4; which++) {
            int[] h = {100, 100, 100, 100};
            h[which] = 101;
            WorldController w = scene();
            w.addTile(0, 3, 4, 1, 0, 7, h[0], h[1], h[2], h[3], 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
            assertFalse("height " + which + " differs", ((Class43) tileGround(w, "plainTile", 0, 3, 4)).flat);
        }
    }

    private static int[] vertex(Class40 c, int index) throws Exception {
        return new int[] {c.vertexX[index], c.vertexY[index], c.vertexHeight[index]};
    }

    private static int vertexAt(Class40 c, int x, int y) {
        for (int i = 0; i < c.vertexX.length; i++) if (c.vertexX[i] == x && c.vertexY[i] == y) return i;
        return -1;
    }

    @Test public void method279ShapedTileHasCornersAtTileOriginPlusXPlusYWithHeightsInOrderAndColourGroups() throws Exception {
        WorldController wc = scene();
        // shape 2 (argument l), rotation 0 (i1), texture 9 (j1)
        wc.addTile(1, 5, 6, 2, 0, 9, 1001, 1002, 1003, 1004, 11, 12, 13, 14, 21, 22, 23, 24, 77, 88);
        Class40 c = (Class40) tileGround(wc, "shapedTile", 1, 5, 6);
        assertNull(tileGround(wc, "plainTile", 1, 5, 6));
        assertEquals(4, c.vertexX.length);
        int x0 = 5 * 128, y0 = 6 * 128;
        int[] expectHeight = {1001, 1002, 1003, 1004};
        int[][] corners = {{x0, y0}, {x0 + 128, y0}, {x0 + 128, y0 + 128}, {x0, y0 + 128}};
        for (int i = 0; i < 4; i++) {
            int v = vertexAt(c, corners[i][0], corners[i][1]);
            assertTrue("corner " + i, v >= 0);
            assertEquals(expectHeight[i], c.vertexHeight[v]);
            assertEquals(i, v);
        }
        // faces for shape 2: face 0 underlay on corners 1,2,3 with the first colour group; face 1 overlay on corners 0,1,3 with the second group and texture
        assertEquals(2, c.shape); assertEquals(0, c.rotation); assertEquals(77, c.underlayMapColour); assertEquals(88, c.overlayMapColour);
        assertArrayEquals(new int[] {1, 0}, new int[] {c.faceVertexA[0], c.faceVertexA[1]});
        assertArrayEquals(new int[] {2, 1}, new int[] {c.faceVertexB[0], c.faceVertexB[1]});
        assertArrayEquals(new int[] {3, 3}, new int[] {c.faceVertexC[0], c.faceVertexC[1]});
        assertEquals(12, c.faceColourA[0]); assertEquals(13, c.faceColourB[0]); assertEquals(14, c.faceColourC[0]);
        assertEquals(21, c.faceColourA[1]); assertEquals(22, c.faceColourB[1]); assertEquals(24, c.faceColourC[1]);
        assertEquals(-1, c.faceTexture[0]); assertEquals(9, c.faceTexture[1]);
        assertNotNull(at(wc, 0, 5, 6));
    }

    @Test public void method279ShapedTileRotationTurnsFaceCornersByQuarterTurnsAndTextureMinusOneHasNoTextureArray() throws Exception {
        for (int rot = 0; rot < 4; rot++) {
            WorldController wc = scene();
            wc.addTile(0, 5, 6, 2, rot, -1, 1001, 1002, 1003, 1004, 11, 12, 13, 14, 21, 22, 23, 24, 77, 88);
            Class40 c = (Class40) tileGround(wc, "shapedTile", 0, 5, 6);
            assertEquals(rot, c.rotation);
            assertEquals((1 - rot) & 3, c.faceVertexA[0]); assertEquals((2 - rot) & 3, c.faceVertexB[0]); assertEquals((3 - rot) & 3, c.faceVertexC[0]);
            assertNull(c.faceTexture);
        }
    }

    // ================= addGroundDecoration-addWallDecoration: object slots =================

    @Test public void method280PlacesGroundDecorationAtTileCentreWithHeightUidAndInfoOnlyOnThatPlane() throws Exception {
        WorldController wc = scene();
        Model m = model();
        wc.addGroundDecoration(2, -250, 21, m, (byte) 0x85, 4444, 33);   // plane, height, y, model, info, uid, x
        Object3 d = at(wc, 2, 33, 21).obj3;
        assertSame(m, d.model);
        assertEquals(33 * 128 + 64, d.worldX); assertEquals(21 * 128 + 64, d.worldY);
        assertEquals(-250, d.height); assertEquals(4444, d.uid); assertEquals((byte) 0x85, d.info);
        assertNull(at(wc, 1, 33, 21));          // lower planes are not created
        assertEquals(4444, wc.getGroundDecorationUid(2, 33, 21));
        assertEquals(0x85, wc.getObjectInfo(2, 33, 21, 4444));
        // a null model changes nothing
        wc.addGroundDecoration(2, 5, 22, null, (byte) 1, 1, 33);
        assertNull(at(wc, 2, 33, 22));
        // a second call replaces the decoration
        Model m2 = model();
        wc.addGroundDecoration(2, 7, 21, m2, (byte) 2, 9, 33);
        assertSame(m2, at(wc, 2, 33, 21).obj3.model);
    }

    @Test public void method281BuildsItemStackAtTileCentreWithHeightAndTallestObjectModelOnTheTile() throws Exception {
        WorldController wc = scene();
        Model top = model(), a = model(), b = model();
        // arguments: x, uid, model stored in 49, height, model stored in 50, model stored in 48, plane, y
        wc.addItemStack(14, 9090, a, -64, b, top, 1, 27);
        Object4 s = at(wc, 1, 14, 27).obj4;
        assertSame(top, s.topModel); assertSame(a, s.otherModel1); assertSame(b, s.otherModel2);
        assertEquals(14 * 128 + 64, s.worldX); assertEquals(27 * 128 + 64, s.worldY);
        assertEquals(-64, s.height); assertEquals(9090, s.uid);
        assertEquals(0, s.stackHeight);
        // tallest scene-object model on the tile; non-model objects and other tiles/planes are ignored
        Ground tile = at(wc, 1, 15, 27);
        tile = new Ground(1, 15, 27); tiles(wc)[1][15][27] = tile;
        Object5 o1 = obj5(1, 15, 15, 27, 27, 1), o2 = obj5(1, 15, 15, 27, 27, 2), o3 = obj5(1, 15, 15, 27, 27, 3);
        o1.model = modelWithHeight(5);
        o2.model = modelWithHeight(12);
        o3.model = new Animable();
        tile.obj5Array[0] = o1; tile.obj5Array[1] = o3; tile.obj5Array[2] = o2; tile.objectCount = 3;
        Ground otherTile = new Ground(1, 16, 27); tiles(wc)[1][16][27] = otherTile;
        Object5 big = obj5(1, 16, 16, 27, 27, 4); big.model = modelWithHeight(99);
        otherTile.obj5Array[0] = big; otherTile.objectCount = 1;
        wc.addItemStack(15, 1, null, 0, null, top, 1, 27);
        assertEquals(12, at(wc, 1, 15, 27).obj4.stackHeight);
        assertNull(at(wc, 1, 15, 27).obj4.otherModel1);
        wc.addItemStack(15, 1, null, 0, null, top, 0, 27);
        assertEquals(0, at(wc, 0, 15, 27).obj4.stackHeight);
    }

    @Test public void method282BuildsWallAtTileCentreWithTwoModelsOrientationsAndFillsLowerPlanes() throws Exception {
        WorldController wc = scene();
        Model a = model(), b = model();
        // arguments: orientation, model, uid, y, info, x, secondModel, height, secondOrientation, plane
        wc.addWall(4, a, 777, 19, (byte) 0x83, 8, b, -120, 16, 2);
        Object1 w = at(wc, 2, 8, 19).obj1;
        assertSame(a, w.model); assertSame(b, w.secondaryModel);
        assertEquals(8 * 128 + 64, w.worldX); assertEquals(19 * 128 + 64, w.worldY);
        assertEquals(-120, w.height);
        assertEquals(4, w.orientation); assertEquals(16, w.orientation1);
        assertEquals(777, w.uid); assertEquals((byte) 0x83, w.info);
        assertNotNull(at(wc, 1, 8, 19)); assertNotNull(at(wc, 0, 8, 19)); assertNull(at(wc, 3, 8, 19));
        assertEquals(777, wc.getWallUid(2, 8, 19));
        assertEquals(0x83, wc.getObjectInfo(2, 8, 19, 777));
        // only one model: still placed; none: nothing happens
        wc.addWall(1, null, 5, 20, (byte) 1, 8, b, 0, 2, 0);
        assertSame(b, at(wc, 0, 8, 20).obj1.secondaryModel); assertNull(at(wc, 0, 8, 20).obj1.model);
        wc.addWall(1, null, 5, 21, (byte) 1, 8, null, 0, 2, 0);
        assertNull(at(wc, 0, 8, 21));
    }

    @Test public void method283BuildsWallDecorationWithOffsetsHeightRotationAndMaskAndFillsLowerPlanes() throws Exception {
        WorldController wc = scene();
        Model m = model();
        // arguments: uid, y, rotation, plane, xOffset, height, model, x, info, yOffset, mask
        wc.addWallDecoration(6060, 23, 3, 2, 17, -90, m, 10, (byte) 0x84, -29, 0x300);
        Object2 d = at(wc, 2, 10, 23).obj2;
        assertSame(m, d.model);
        assertEquals(10 * 128 + 64 + 17, d.worldX); assertEquals(23 * 128 + 64 - 29, d.worldY);
        assertEquals(-90, d.height); assertEquals(3, d.rotation); assertEquals(0x300, d.drawMask);
        assertEquals(6060, d.uid); assertEquals((byte) 0x84, d.info);
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
        assertSame(m, o.model);
        assertEquals(50 * 128 + 64 * 2, o.worldX); assertEquals(40 * 128 + 64 * 3, o.worldY);
        assertEquals(-77, o.height); assertEquals(1024, o.rotation); assertEquals(1, o.plane);
        assertEquals(31337, o.uid); assertEquals((byte) 0x82, o.info);
        assertEquals(50, o.minTileX); assertEquals(51, o.maxTileX); assertEquals(40, o.minTileY); assertEquals(42, o.maxTileY);
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
        assertEquals(5, at(wc, 0, 60, 60).objectCount);
        assertFalse((Boolean) call(wc, "placeSceneObject", M287, 0, 59, 60, 2, 1, 0, 0, 0, m, 0, false, 99, (byte) 0));
        assertNull(at(wc, 0, 59, 60));
        assertEquals(5, at(wc, 0, 60, 60).objectCount);
        // the count is per plane
        assertTrue((Boolean) call(wc, "placeSceneObject", M287, 1, 60, 60, 1, 1, 0, 0, 0, m, 0, false, 98, (byte) 0));
    }

    @Test public void method287StoresObjectFieldsEdgeMasksAndRegistersInCacheOnlyWhenFlagged() throws Exception {
        WorldController wc = scene();
        Model m = model();
        // arguments: plane, tileX, tileY, sizeX, sizeY, x, y, height, model, rotation, cache flag, uid, info
        assertTrue((Boolean) call(wc, "placeSceneObject", M287, 2, 20, 30, 3, 2, 3333, 4444, -555, m, 1536, true, 66, (byte) 9));
        Object5 o = at(wc, 2, 20, 30).obj5Array[0];
        assertEquals(2, o.plane); assertEquals(3333, o.worldX); assertEquals(4444, o.worldY); assertEquals(-555, o.height);
        assertSame(m, o.model); assertEquals(1536, o.rotation); assertEquals(66, o.uid); assertEquals((byte) 9, o.info);
        assertEquals(20, o.minTileX); assertEquals(22, o.maxTileX); assertEquals(30, o.minTileY); assertEquals(31, o.maxTileY);
        // per-tile edge masks: +1 has a tile before in x, +4 has a tile after in x, +8 before in y, +2 after in y
        int[][] expect = {{2 + 4, 2 + 1 + 4, 2 + 1}, {8 + 4, 8 + 1 + 4, 8 + 1}};
        for (int y = 0; y < 2; y++) for (int x = 0; x < 3; x++) {
            Ground g = at(wc, 2, 20 + x, 30 + y);
            assertEquals(1, g.objectCount);
            assertEquals("mask x" + x + " y" + y, expect[y][x], g.objectEdgeMasks[0]);
            assertEquals(expect[y][x], g.objectEdgeMask);
            assertNotNull(at(wc, 1, 20 + x, 30 + y)); assertNotNull(at(wc, 0, 20 + x, 30 + y)); assertNull(at(wc, 3, 20 + x, 30 + y));
        }
        assertEquals(1, geti(wc, "obj5CacheCurrPos"));
        assertSame(o, cache(wc)[0]);
        // single tile object has no edge bits; masks of several objects are OR-ed
        assertTrue((Boolean) call(wc, "placeSceneObject", M287, 2, 21, 30, 1, 1, 0, 0, 0, m, 0, false, 67, (byte) 0));
        Ground g = at(wc, 2, 21, 30);
        assertEquals(2, g.objectCount); assertEquals(0, g.objectEdgeMasks[1]); assertEquals(4 + 1 + 2, g.objectEdgeMask);
        assertEquals(1, geti(wc, "obj5CacheCurrPos"));
    }

    @Test public void method285PlacesEntityWithSquareRadiusFootprintAndYawDependentExtensionOnlyWhenFlagged() throws Exception {
        // arguments: plane, yaw, height, uid, y, radius, x, model, extend flag
        WorldController wc = scene();
        Model m = model();
        assertTrue(wc.addEntity(0, 0, -33, 808, 2000, 60, 1000, m, false));
        Object5 o = at(wc, 0, 7, 15).obj5Array[0];
        assertEquals(1000, o.worldX); assertEquals(2000, o.worldY); assertEquals(-33, o.height);
        assertEquals(0, o.rotation); assertEquals(808, o.uid); assertEquals((byte) 0, o.info);
        assertEquals(7, o.minTileX); assertEquals(8, o.maxTileX); assertEquals(15, o.minTileY); assertEquals(16, o.maxTileY);
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
            assertEquals("yaw " + c[0] + " minX", c[1], e.minTileX); assertEquals("yaw " + c[0] + " maxX", c[2], e.maxTileX);
            assertEquals("yaw " + c[0] + " minY", c[3], e.minTileY); assertEquals("yaw " + c[0] + " maxY", c[4], e.maxTileY);
            assertEquals(c[0], e.rotation);
            WorldController w2 = scene();
            assertTrue(w2.addEntity(1, c[0], 0, 1, 2000, 60, 1000, m, false));
            Object5 f = at(w2, 1, 7, 15).obj5Array[0];
            assertEquals(7, f.minTileX); assertEquals(8, f.maxTileX); assertEquals(15, f.minTileY); assertEquals(16, f.maxTileY);
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
        assertEquals(24, o.minTileX); assertEquals(26, o.maxTileX); assertEquals(31, o.minTileY); assertEquals(33, o.maxTileY);
        assertEquals(3333, o.worldX); assertEquals(4444, o.worldY); assertEquals(-44, o.height);
        assertEquals(512, o.rotation); assertEquals(909, o.uid); assertEquals(1, o.plane);
        assertSame(m, o.model);
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
        Ground g = new Ground(0, 43, 60); tiles(wc)[0][43][60] = g; g.objectsPending = true;
        Ground swapped = new Ground(0, 60, 43); tiles(wc)[0][60][43] = swapped; swapped.objectsPending = true;
        heights[0][43][60] = 500;
        wc.drawScene(40 * 128 + 10, 60 * 128 + 10, 640, 0, 2, 160);
        assertFalse("500 above the camera: visible although outside the table", g.objectsPending);
        assertTrue("terrain height of the swapped axes is 0: hidden", swapped.objectsPending);
        // the lowest plane limits which planes drawScene considers
        table[1][10][4 + 5][0 + 5] = true;
        Ground low = new Ground(1, 44, 60); tiles(wc)[1][44][60] = low; low.objectsPending = true;
        Ground high = new Ground(2, 44, 60); tiles(wc)[2][44][60] = high; high.objectsPending = true;
        wc.createPlaneTiles(2);
        tiles(wc)[1][44][60] = low; tiles(wc)[2][44][60] = high;
        wc.drawScene(40 * 128 + 10, 60 * 128 + 10, 640, 0, 3, 160);
        assertTrue("plane below the lowest plane is not processed", low.objectsPending);
        assertFalse("lowest plane is processed", high.objectsPending);
    }
}
