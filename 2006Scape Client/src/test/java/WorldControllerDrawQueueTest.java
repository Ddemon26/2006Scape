import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** Original-name semantic tests for WorldController.drawTileQueue (tile draw queue), the Ground/Object1-5 fields it reads and the wall mask tables. */
public class WorldControllerDrawQueueTest {
    private static final int SIZE = 104;
    private static final List<String> LOG = new ArrayList<String>();
    private final WorldControllerDrawTest base = new WorldControllerDrawTest();
    private final Object[] saved = new Object[3];
    private int[] px;

    /** Renderable that logs its draw call: name:rotation,x,height,y,uid (camera-relative position). */
    private static final class Rec extends Animable {
        final String name;
        Rec(String name) { this.name = name; }
        @Override public void method443(int i, int j, int k, int l, int i1, int j1, int k1, int l1, int i2) {
            LOG.add(name + ":" + i + "," + j1 + "," + k1 + "," + l1 + "," + i2);
        }
    }

    @Before public void setUp() throws Exception {
        base.save();
        LOG.clear();
        saved[0] = sget("drawQueue"); saved[1] = sget("wallFirstMasks"); saved[2] = sget("wallLateMasks");
        px = new int[200 * 160];
        Arrays.fill(px, 0x7f7f7f);
        DrawingArea.bindRaster(160, 200, px);
        Texture.setScreenSize(200, 160);
        Texture.transparency = 0;
    }

    @After public void tearDown() throws Exception { base.restore(); }

    private static Field field(Class<?> c, String n) throws Exception { Field f = c.getDeclaredField(n); f.setAccessible(true); return f; }
    private static Object sget(String n) throws Exception { return field(WorldController.class, n).get(null); }
    private static void sset(String n, Object v) throws Exception { field(WorldController.class, n).set(null, v); }
    private static int sint(String n) throws Exception { return (Integer) sget(n); }

    private static WorldController scene() { return new WorldController(new int[4][SIZE + 1][SIZE + 1]); }

    private static void camera(int tileX, int tileY) throws Exception {
        int x = tileX * 128 + 64, y = tileY * 128 + 64;
        sset("cameraWorldX", x); sset("cameraWorldHeight", -300); sset("cameraWorldY", y);
        sset("cameraTileX", tileX); sset("cameraTileY", tileY);
        sset("pitchSine", Model.sineTable16[128]); sset("pitchCosine", Model.cosineTable16[128]);
        sset("yawSine", Model.sineTable16[0]); sset("yawCosine", Model.cosineTable16[0]);
        sset("cameraPlane", 0); sset("drawMinTileX", 0); sset("drawMaxTileX", SIZE); sset("drawMinTileY", 0); sset("drawMaxTileY", SIZE);
        sset("drawCycle", 1); sset("activeOccluderCount", 0); sset("pendingTileCount", 0);
    }

    private static Ground tile(WorldController wc, int plane, int x, int y) throws Exception {
        Ground g = new Ground(plane, x, y);
        ((Ground[][][]) field(WorldController.class, "groundArray").get(wc))[plane][x][y] = g;
        return g;
    }

    private static Ground pending(WorldController wc, int plane, int x, int y) throws Exception {
        Ground g = tile(wc, plane, x, y);
        g.firstPassPending = true; g.drawPending = true;
        sset("pendingTileCount", sint("pendingTileCount") + 1);
        return g;
    }

    private static Object1 wall(String name, int orientation, int orientation1, Animable second) {
        Object1 w = new Object1();
        w.model = new Rec(name); w.secondaryModel = second;
        w.orientation = orientation; w.orientation1 = orientation1;
        w.worldX = 20 * 128 + 64; w.height = -11; w.worldY = 25 * 128 + 64; w.uid = 7;
        return w;
    }

    private static void draw(WorldController wc, Ground start, boolean flag) throws Exception {
        Method m = WorldController.class.getDeclaredMethod("drawTileQueue", Ground.class, boolean.class);
        m.setAccessible(true);
        try { m.invoke(wc, start, flag); } catch (InvocationTargetException e) { if (e.getCause() instanceof Exception) throw (Exception) e.getCause(); throw e; }
    }

    private static int index(String prefix) {
        for (int i = 0; i < LOG.size(); i++) if (LOG.get(i).startsWith(prefix)) return i;
        return -1;
    }

    // ---- the flag parameter, plane/originalPlane, coordinates ----

    @Test public void method314FlagTrueSkipsATileWhoseLowerPlaneTileIsStillPendingAndFlagFalseDoesNot() throws Exception {
        for (boolean flag : new boolean[] {true, false}) {
            WorldController wc = scene(); LOG.clear();
            camera(30, 40);
            Ground low = pending(wc, 0, 20, 25);
            Ground high = pending(wc, 1, 20, 25);
            high.obj1 = wall("high", 1, 0, null);
            draw(wc, high, flag);
            assertEquals("flag=" + flag, !flag, index("high:") >= 0);
            assertEquals(!flag, !high.drawPending);
            assertEquals(!flag, !high.firstPassPending);
            assertTrue("lower plane tile stays pending when the upper tile was skipped", flag ? low.drawPending : true);
        }
        WorldController wc = scene(); LOG.clear();
        camera(30, 40);
        Ground low = tile(wc, 0, 20, 25);
        low.drawPending = false;
        Ground high = pending(wc, 1, 20, 25);
        high.obj1 = wall("high", 1, 0, null);
        draw(wc, high, true);
        assertTrue("lower tile already done: flag=true does not skip", index("high:") >= 0);
    }

    @Test public void neighboursTowardTheCameraAreQueuedFirstInFirstOutUsingTileXTileYAsTheArrayIndices() throws Exception {
        int[][] cameras = {{30, 40}, {10, 10}};
        int[] orientations = {1, 4};
        int[][][] expected = {{{20, 25}, {21, 25}, {20, 26}, {21, 26}}, {{20, 25}, {19, 25}, {20, 24}, {19, 24}}};
        for (int c = 0; c < 2; c++) {
            WorldController wc = scene(); LOG.clear();
            camera(cameras[c][0], cameras[c][1]);
            int sx = c == 0 ? 1 : -1, sy = c == 0 ? 1 : -1;
            Ground a = pending(wc, 0, 20, 25), nx = pending(wc, 0, 20 + sx, 25), ny = pending(wc, 0, 20, 25 + sy), diag = pending(wc, 0, 20 + sx, 25 + sy);
            for (Ground g : new Ground[] {a, nx, ny, diag}) g.obj1 = wall("t" + g.tileX + "_" + g.tileY, orientations[c], 0, null);
            draw(wc, a, false);
            List<String> want = new ArrayList<String>();
            for (int[] t : expected[c]) want.add("t" + t[0] + "_" + t[1]);
            assertEquals("camera " + c + ": start, x neighbour, y neighbour (queue order), then their shared diagonal", want, names(LOG));
            assertEquals(0, sint("pendingTileCount"));
            assertEquals("the draw queue is empty on return", null, ((NodeList) sget("drawQueue")).popTail());
        }
    }

    private static List<String> names(List<String> log) {
        List<String> out = new ArrayList<String>();
        for (String s : log) out.add(s.substring(0, s.indexOf(':')));
        return out;
    }

    @Test public void groundConstructorStoresPlaneInBothPlaneFieldsAndLoweringChangesOnlyTheFirst() throws Exception {
        Ground g = new Ground(2, 7, 9);
        assertEquals(2, g.plane); assertEquals(2, g.originalPlane);
        assertEquals(7, g.tileX); assertEquals(9, g.tileY);
        WorldController wc = scene();
        Ground[][][] tiles = (Ground[][][]) field(WorldController.class, "groundArray").get(wc);
        tiles[1][7][9] = new Ground(1, 7, 9);
        tiles[0][7][9] = new Ground(0, 7, 9);
        wc.lowerColumnPlanes(9, 7);
        Ground lowered = tiles[0][7][9];
        assertEquals("tile that was on plane 1 is now on plane 0", 0, lowered.plane);
        assertEquals("but keeps the plane it was created on", 1, lowered.originalPlane);
    }

    // ---- wall side masks (tables 478/480, lateWallMask) ----

    @Test public void cardinalWallEdgesAwayFromTheCameraDrawBeforeTheTileFinishesAndEdgesFacingItDrawAfter() throws Exception {
        int[] bits = {1, 2, 4, 8};
        for (int dx = -1; dx <= 1; dx++) for (int dy = -1; dy <= 1; dy++) for (int bit : bits) {
            WorldController wc = scene(); LOG.clear();
            camera(20 + dx, 25 + dy);
            Ground g = pending(wc, 0, 20, 25);
            g.obj1 = wall("wall", bit, 0, null);
            Object4 marker = new Object4();
            marker.topModel = new Rec("marker"); marker.stackHeight = 1;
            g.obj4 = marker;
            draw(wc, g, false);
            boolean facing = bit == 1 ? dx < 0 : bit == 4 ? dx > 0 : bit == 2 ? dy > 0 : dy < 0;
            String ctx = "camera offset " + dx + "," + dy + " bit " + bit;
            assertEquals(ctx, 2, LOG.size());
            assertEquals(ctx, facing, index("wall:") > index("marker:"));
            assertEquals(ctx, facing, (g.lateWallMask & bit) != 0);
            assertEquals(ctx, false, g.drawPending);
        }
    }

    // ---- Object1 wall fields ----

    @Test public void wallFieldsAreHeightWorldXWorldYAndTwoModelsDrawnWithCameraRelativePosition() throws Exception {
        WorldController wc = scene();
        camera(20, 25);
        Ground g = pending(wc, 0, 20, 25);
        Object1 w = wall("primary", 1, 4, new Rec("secondary"));
        w.worldX = 2700; w.height = -123; w.worldY = 3300; w.uid = 99;
        g.obj1 = w;
        draw(wc, g, false);
        int cx = sint("cameraWorldX"), cy = sint("cameraWorldY"), ch = sint("cameraWorldHeight");
        assertEquals(Arrays.asList("primary:0," + (2700 - cx) + "," + (-123 - ch) + "," + (3300 - cy) + ",99",
                "secondary:0," + (2700 - cx) + "," + (-123 - ch) + "," + (3300 - cy) + ",99"), LOG);
        // a model is drawn only when its own orientation overlaps the visible mask
        wc = scene(); LOG.clear(); camera(20, 25);
        g = pending(wc, 0, 20, 25);
        w = wall("primary", 0, 4, new Rec("secondary"));
        g.obj1 = w;
        draw(wc, g, false);
        assertEquals(Arrays.asList("secondary"), names(LOG));
    }

    // ---- Object2 wall decoration ----

    @Test public void wallDecorationDrawsItsModelWithRotationWhenItsMaskOverlapsAndPositionIsWorldXHeightWorldY() throws Exception {
        WorldController wc = scene();
        camera(20, 25);
        Ground g = pending(wc, 0, 20, 25);
        Object2 d = new Object2();
        d.model = new Rec("deco"); d.worldX = 2600; d.height = -55; d.worldY = 3250; d.rotation = 3; d.drawMask = 2; d.uid = 55;
        g.obj2 = d;
        draw(wc, g, false);
        int cx = sint("cameraWorldX"), cy = sint("cameraWorldY"), ch = sint("cameraWorldHeight");
        assertEquals(Arrays.asList("deco:3," + (2600 - cx) + "," + (-55 - ch) + "," + (3250 - cy) + ",55"), LOG);
        wc = scene(); LOG.clear(); camera(20, 25);
        g = pending(wc, 0, 20, 25);
        d.drawMask = 0;
        g.obj2 = d;
        draw(wc, g, false);
        assertTrue("mask 0 never matches", LOG.isEmpty());
    }

    // ---- ground contents: plain/shaped tile gate Object3 and Object4 ----

    @Test public void groundDecorationAndFlatItemStackDrawOnlyWhenThePlainOrShapedTileIsDrawn() throws Exception {
        for (int kind = 0; kind < 3; kind++) {
            WorldController wc = scene(); LOG.clear();
            camera(20, 25);
            Ground g = pending(wc, 0, 20, 25);
            if (kind == 1) g.plainTile = new Class43(0x1150, 0x2900, 0x4a80, 0x6fe0, -1, 77, false);
            if (kind == 2) g.shapedTile = new Class40(25, 1, 2, 3, -1, 4, 0, 5, 6, 7, 0, 0, 0, 0, 8, 9, 10, 20, 11);
            Object3 deco = new Object3();
            deco.model = new Rec("ground"); deco.worldX = 2700; deco.height = -9; deco.worldY = 3300; deco.uid = 5;
            g.obj3 = deco;
            Object4 items = new Object4();
            items.topModel = new Rec("top"); items.otherModel1 = new Rec("first"); items.otherModel2 = new Rec("second");
            items.worldX = 2800; items.height = -21; items.worldY = 3400; items.uid = 6; items.stackHeight = 0;
            g.obj4 = items;
            draw(wc, g, false);
            int cx = sint("cameraWorldX"), cy = sint("cameraWorldY"), ch = sint("cameraWorldHeight");
            if (kind == 0) {
                assertTrue("no tile drawn: neither is drawn", LOG.isEmpty());
            } else {
                assertEquals(Arrays.asList("ground:0," + (2700 - cx) + "," + (-9 - ch) + "," + (3300 - cy) + ",5",
                        "first:0," + (2800 - cx) + "," + (-21 - ch) + "," + (3400 - cy) + ",6",
                        "second:0," + (2800 - cx) + "," + (-21 - ch) + "," + (3400 - cy) + ",6",
                        "top:0," + (2800 - cx) + "," + (-21 - ch) + "," + (3400 - cy) + ",6"), LOG);
            }
        }
    }

    @Test public void itemStackWithAStackHeightIsLiftedByItAndDrawnAfterTheTileEvenWithoutAGroundTile() throws Exception {
        WorldController wc = scene();
        camera(20, 25);
        Ground g = pending(wc, 0, 20, 25);
        Object4 items = new Object4();
        items.topModel = new Rec("top"); items.otherModel1 = new Rec("first");
        items.worldX = 2800; items.height = -21; items.worldY = 3400; items.uid = 6; items.stackHeight = 40;
        g.obj4 = items;
        draw(wc, g, false);
        int cx = sint("cameraWorldX"), cy = sint("cameraWorldY"), ch = sint("cameraWorldHeight");
        assertEquals(Arrays.asList("first:0," + (2800 - cx) + "," + (-21 - ch - 40) + "," + (3400 - cy) + ",6",
                "top:0," + (2800 - cx) + "," + (-21 - ch - 40) + "," + (3400 - cy) + ",6"), LOG);
    }

    // ---- lowerGround ----

    @Test public void lowerGroundContentsAreDrawnBeforeTheTileItIsLinkedFrom() throws Exception {
        WorldController wc = scene();
        camera(20, 25);
        Ground g = pending(wc, 0, 20, 25);
        Ground lower = new Ground(0, 20, 25);
        lower.obj1 = wall("lower", 1, 0, null);
        g.lowerGround = lower;
        g.obj1 = wall("upper", 1, 0, null);
        draw(wc, g, false);
        assertEquals(Arrays.asList("lower", "upper"), names(LOG));
    }

    // ---- Object5 scene objects, objectCount, objectEdgeMasks, objectsPending, firstPassPending ----

    private static void place(WorldController wc, String name, int minX, int maxX, int y, int uid) throws Exception {
        assertTrue(wc.addEntityInTileBounds(0, y * 128 + 64, new Rec(name), 33, y, minX * 128 + 64, -77, minX, maxX, uid, y));
    }

    @Test public void sceneObjectsDrawFarthestFirstOnceEachAfterAllCoveredTilesAndUseObjectFields() throws Exception {
        for (int order = 0; order < 2; order++) {
            WorldController wc = scene(); LOG.clear();
            camera(30, 40);
            if (order == 0) { place(wc, "wide", 19, 20, 25, 1); place(wc, "narrow", 20, 20, 25, 2); }
            else { place(wc, "narrow", 20, 20, 25, 2); place(wc, "wide", 19, 20, 25, 1); }
            Ground[][][] tiles = (Ground[][][]) field(WorldController.class, "groundArray").get(wc);
            Ground start = tiles[0][19][25];
            for (int x = 19; x <= 20; x++) { tiles[0][x][25].firstPassPending = true; tiles[0][x][25].drawPending = true; tiles[0][x][25].objectsPending = true; }
            sset("pendingTileCount", 2);
            Object5 wide = tiles[0][19][25].obj5Array[0];
            Object5 narrow = tiles[0][20][25].obj5Array[order == 0 ? 1 : 0];
            assertEquals(2, tiles[0][20][25].objectCount); assertEquals(1, tiles[0][19][25].objectCount);
            draw(wc, start, false);
            assertEquals("each drawn once, farther (wider toward camera-far side) first, order=" + order, Arrays.asList("wide", "narrow"), names(LOG));
            assertEquals(1, wide.lastDrawCycle); assertEquals(1, narrow.lastDrawCycle);
            assertEquals("distance key: far tile offset plus far tile offset", (30 - 19) + (40 - 25), wide.cameraDistance);
            assertEquals((30 - 20) + (40 - 25), narrow.cameraDistance);
            assertEquals(0, sint("pendingTileCount"));
            int cx = sint("cameraWorldX"), cy = sint("cameraWorldY"), ch = sint("cameraWorldHeight");
            assertTrue(LOG.get(0).equals("wide:33," + (19 * 128 + 64 - cx) + "," + (-77 - ch) + "," + (25 * 128 + 64 - cy) + ",1"));
        }
    }

    @Test public void sceneObjectIsNotDrawnTwiceInTheSameCycleAndNotAtAllWhenObjectsPendingIsFalse() throws Exception {
        WorldController wc = scene();
        camera(30, 40);
        place(wc, "obj", 20, 20, 25, 3);
        Ground[][][] tiles = (Ground[][][]) field(WorldController.class, "groundArray").get(wc);
        Ground g = tiles[0][20][25];
        g.firstPassPending = true; g.drawPending = true; g.objectsPending = false;
        sset("pendingTileCount", 1);
        draw(wc, g, false);
        assertTrue("objectsPending false: objects are not drawn by this tile", LOG.isEmpty());
        wc = scene(); LOG.clear(); camera(30, 40);
        place(wc, "obj", 20, 20, 25, 3);
        tiles = (Ground[][][]) field(WorldController.class, "groundArray").get(wc);
        g = tiles[0][20][25];
        g.firstPassPending = true; g.drawPending = true; g.objectsPending = true;
        g.obj5Array[0].lastDrawCycle = 1;
        sset("pendingTileCount", 1);
        draw(wc, g, false);
        assertTrue("object already drawn this cycle (lastDrawCycle == drawCycle)", LOG.isEmpty());
    }

    @Test public void tileKeepsItselfPendingAndDefersItsObjectWhileACoveredTileHasNotHadItsFirstPass() throws Exception {
        WorldController wc = scene();
        camera(30, 40);
        place(wc, "wide", 19, 20, 25, 1);
        Ground[][][] tiles = (Ground[][][]) field(WorldController.class, "groundArray").get(wc);
        Ground a = tiles[0][19][25], b = tiles[0][20][25];
        a.firstPassPending = a.drawPending = a.objectsPending = true;
        b.firstPassPending = true; b.drawPending = false;
        sset("pendingTileCount", 1);
        draw(wc, a, false);
        assertTrue("object waits for the covered tile", LOG.isEmpty());
        assertFalse(a.firstPassPending);
        assertTrue("objects still pending", a.objectsPending);
        assertTrue("tile not finished", a.drawPending);
        assertEquals(1, sint("pendingTileCount"));
        b.firstPassPending = false;
        draw(wc, a, false);
        assertEquals(Arrays.asList("wide"), names(LOG));
        assertFalse(a.objectsPending);
        assertFalse(a.drawPending);
        assertEquals(0, sint("pendingTileCount"));
    }

    @Test public void objectCountAndEdgeMasksOfAPlacedObjectMatchTheTilesItCovers() throws Exception {
        WorldController wc = scene();
        camera(30, 40);
        assertTrue(wc.addEntityInTileBounds(0, 3000, new Rec("big"), 0, 26, 2700, -1, 20, 22, 4, 25));
        Ground[][][] tiles = (Ground[][][]) field(WorldController.class, "groundArray").get(wc);
        Ground corner = tiles[0][20][25], middle = tiles[0][21][26], far = tiles[0][22][26];
        assertEquals(1, corner.objectCount);
        assertEquals(4 + 2, corner.objectEdgeMasks[0]); assertEquals(corner.objectEdgeMasks[0], corner.objectEdgeMask);
        assertEquals(1 + 4 + 8, middle.objectEdgeMasks[0]);
        assertEquals(1 + 8, far.objectEdgeMasks[0]);
        assertEquals(far.objectEdgeMasks[0], far.objectEdgeMask);
    }

    // ---- minCameraPlane via drawScene ----

    @Test public void tileWithMinimumCameraPlaneAboveTheCameraPlaneIsNotDrawnByDrawScene() throws Exception {
        for (int minPlane = 0; minPlane <= 1; minPlane++) {
            WorldController wc = scene(); LOG.clear();
            Ground g = tile(wc, 0, 50, 50);
            g.obj1 = wall("wall", 1, 0, null);
            g.minCameraPlane = minPlane;
            wc.drawScene(50 * 128 + 64, 50 * 128 + 64, 0, -400, 0, 128);
            assertEquals("minimum plane " + minPlane + " camera plane 0", minPlane == 0, index("wall:") >= 0);
            assertEquals(0, sint("pendingTileCount"));
        }
    }

    // ---- first-pass pending flag ----

    @Test public void firstPassPendingIsClearedWhenTheTileFirstDrawsAndKeepsAFinishedTileFromDrawingItsContentsAgain() throws Exception {
        WorldController wc = scene();
        camera(30, 40);
        Ground g = pending(wc, 0, 20, 25);
        g.obj1 = wall("wall", 16, 0, null);
        g.firstPassPending = false;
        draw(wc, g, false);
        assertTrue("firstPass already done: contents are not drawn again", LOG.isEmpty());
        assertFalse(g.drawPending);
    }
}
