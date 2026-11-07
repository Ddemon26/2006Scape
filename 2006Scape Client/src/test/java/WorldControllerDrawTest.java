import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** Original-name semantic tests for the WorldController draw statics and tile/occlusion routines (drawPlainTile-isTileOccluded, isTileColumnOccluded-isPointOccluded). */
public class WorldControllerDrawTest {
    private static final int PLANES = 4;
    private static final int SIZE = 104;
    private static final int W = 200, H = 160, BG = 0x7f7f7f;
    private static final String[] STATIC_NAMES = {"pendingTileCount", "cameraPlane", "drawCycle", "drawMinTileX", "drawMaxTileX", "drawMinTileY", "drawMaxTileY", "cameraTileX",
            "cameraTileY", "cameraWorldX", "cameraWorldHeight", "cameraWorldY", "pitchSine", "pitchCosine", "yawSine", "yawCosine", "tilePickRequested", "pickScreenX", "pickScreenY",
            "pickedTileX", "pickedTileY", "activeOccluderCount", "cameraTileVisibility", "occluderCounts", "occluders", "lowMem"};

    private final Object[] savedStatics = new Object[STATIC_NAMES.length];
    private int savedDrawDistance;
    private int[] palette, savedPixels, savedLineOffsets, savedDrawingArea;
    private int savedTex1, savedTex2, savedTransparency;
    private boolean savedClip;

    @Before public void save() throws Exception {
        for (int i = 0; i < STATIC_NAMES.length; i++) savedStatics[i] = sget(STATIC_NAMES[i]);
        savedDrawDistance = WorldController.drawDistance;
        palette = Texture.hslToRgbPalette.clone();
        savedPixels = DrawingArea.pixels;
        savedDrawingArea = new int[] {DrawingArea.width, DrawingArea.height, DrawingArea.topX, DrawingArea.topY, DrawingArea.bottomX,
                DrawingArea.bottomY, DrawingArea.scanlineRightLimit, DrawingArea.clipRightHalf, DrawingArea.clipBottomHalf};
        savedLineOffsets = (int[]) tget("lineOffsets");
        savedTex1 = Texture.textureInt1;
        savedTex2 = Texture.textureInt2;
        savedTransparency = Texture.transparency;
        savedClip = (Boolean) tget("clipScanlines");
        for (int i = 0; i < 65536; i++) Texture.hslToRgbPalette[i] = i;
        sset("occluderCounts", new int[4]);
        sset("occluders", new Class47[4][500]);
        sset("tilePickRequested", false);
        sset("pickedTileX", -1);
        sset("pickedTileY", -1);
        sset("lowMem", true);
        WorldController.drawDistance = 25;
    }

    @After public void restore() throws Exception {
        for (int i = 0; i < STATIC_NAMES.length; i++) sset(STATIC_NAMES[i], savedStatics[i]);
        WorldController.drawDistance = savedDrawDistance;
        System.arraycopy(palette, 0, Texture.hslToRgbPalette, 0, 65536);
        DrawingArea.pixels = savedPixels;
        DrawingArea.width = savedDrawingArea[0]; DrawingArea.height = savedDrawingArea[1];
        DrawingArea.topX = savedDrawingArea[2]; DrawingArea.topY = savedDrawingArea[3];
        DrawingArea.bottomX = savedDrawingArea[4]; DrawingArea.bottomY = savedDrawingArea[5];
        DrawingArea.scanlineRightLimit = savedDrawingArea[6];
        DrawingArea.clipRightHalf = savedDrawingArea[7]; DrawingArea.clipBottomHalf = savedDrawingArea[8];
        tset("lineOffsets", savedLineOffsets);
        Texture.textureInt1 = savedTex1;
        Texture.textureInt2 = savedTex2;
        Texture.transparency = savedTransparency;
        tset("clipScanlines", savedClip);
    }

    @Test public void method321RequiresEveryOrientationSampleAtExactTileCoordinatesAndHeights() throws Exception {
        Class47[] active = (Class47[]) sget("activeOccluders");
        Class47[] original = active.clone();
        try {
            WorldController wc = scene();
            int[][][] heights = (int[][][]) get(wc, "tileHeights");
            int[][][] cache = (int[][][]) get(wc, "tileOcclusionCache");
            sset("drawCycle", 17);
            for (int plane : new int[] {0, 2}) for (int tx : new int[] {7, 11}) for (int ty : new int[] {19, 24}) {
                int h = plane == 0 ? 500 : -600;
                heights[plane][tx][ty] = h;
                for (int dx = -1; dx <= 1; dx++) for (int dy = -1; dy <= 1; dy++) {
                    sset("cameraWorldX", tx * 128 + dx);
                    sset("cameraWorldY", ty * 128 + dy);
                    for (int mask : new int[] {1, 2, 4, 8, 16, 32, 64, 128}) {
                        int[][] points = wallSamples(plane, tx, ty, h, mask, dx, dy);
                        coverWallSamples(points, -1, -1, 0);
                        cache[plane][tx][ty] = 17;
                        assertEquals("all samples covered mask=" + mask, true, call(wc, "isWallOccluded", WALL_QUERY, plane, tx, ty, mask));
                        cache[plane][tx][ty] = -17;
                        assertEquals("tile visibility is a prerequisite", false, call(wc, "isWallOccluded", WALL_QUERY, plane, tx, ty, mask));
                        cache[plane][tx][ty] = 17;
                        for (int missing = 0; missing < points.length; missing++) {
                            coverWallSamples(points, missing, -1, 0);
                            assertEquals("missing sample " + missing + " mask=" + mask, false, call(wc, "isWallOccluded", WALL_QUERY, plane, tx, ty, mask));
                            for (int axis = 0; axis < 3; axis++) for (int delta : new int[] {-1, 1}) {
                                coverWallSamples(points, missing, axis, delta);
                                assertEquals("shifted sample axis=" + axis + " delta=" + delta, false, call(wc, "isWallOccluded", WALL_QUERY, plane, tx, ty, mask));
                            }
                        }
                    }
                }
            }
        } finally { System.arraycopy(original, 0, active, 0, original.length); }
    }

    private static final Class<?>[] WALL_QUERY = {int.class, int.class, int.class, int.class};

    // Table of wall edge/corner coordinates, independent of the routine's branch structure.
    private static int[][] wallSamples(int plane, int tx, int ty, int h, int mask, int dx, int dy) {
        int[][] edges = {{0, 0, 0, 128}, {0, 128, 128, 128}, {128, 0, 128, 128}, {0, 0, 128, 0}};
        int[][] corners = {{0, 128}, {128, 128}, {128, 0}, {0, 0}};
        java.util.List<int[]> points = new java.util.ArrayList<int[]>();
        int bit = Integer.numberOfTrailingZeros(mask);
        if (bit < 4) {
            boolean[] facing = {dx < 0, dy > 0, dx > 0, dy < 0};
            int[] e = edges[bit];
            for (int offset : new int[] {1, 121, 231}) {
                if (offset == 1 && !facing[bit] || offset == 121 && plane == 0) continue;
                points.add(new int[] {tx * 128 + e[0], h - offset, ty * 128 + e[1]});
                points.add(new int[] {tx * 128 + e[2], h - offset, ty * 128 + e[3]});
            }
        } else {
            points.add(new int[] {tx * 128 + 64, h - 239, ty * 128 + 64});
            int[] c = corners[bit - 4];
            points.add(new int[] {tx * 128 + c[0], h - 231, ty * 128 + c[1]});
        }
        return points.toArray(new int[points.size()][]);
    }

    // Zero-width sloping occluders contain only their requested test point among the samples.
    private static void coverWallSamples(int[][] points, int missing, int axis, int delta) throws Exception {
        Class47[] occluders = new Class47[500];
        int count = 0;
        for (int n = 0; n < points.length; n++) {
            if (n == missing && axis == -1) continue;
            int[] p = points[n].clone();
            if (n == missing) p[axis] += delta;
            Class47 o = new Class47();
            o.side = 1; o.minX = p[0] + 1;
            o.minY = o.maxY = p[2];
            o.minHeight = o.maxHeight = p[1] - 1;
            o.minHeightSlope = o.maxHeightSlope = 256;
            occluders[count++] = o;
        }
        System.arraycopy(occluders, 0, (Class47[]) sget("activeOccluders"), 0, occluders.length);
        sset("activeOccluderCount", count);
    }

    // ---- reflection helpers (original names only) ----
    private static Field field(Class<?> c, String n) throws Exception { Field f = c.getDeclaredField(n); f.setAccessible(true); return f; }
    private static Object sget(String n) throws Exception { return field(WorldController.class, n).get(null); }
    private static void sset(String n, Object v) throws Exception { field(WorldController.class, n).set(null, v); }
    private static int sint(String n) throws Exception { return (Integer) sget(n); }
    private static Object tget(String n) throws Exception { return field(Texture.class, n).get(null); }
    private static void tset(String n, Object v) throws Exception { field(Texture.class, n).set(null, v); }
    private static Object get(Object o, String n) throws Exception { return field(o.getClass(), n).get(o); }
    private static Object call(Object target, String name, Class<?>[] types, Object... args) throws Exception {
        Method m = WorldController.class.getDeclaredMethod(name, types);
        m.setAccessible(true);
        try { return m.invoke(target, args); } catch (InvocationTargetException e) { if (e.getCause() instanceof Exception) throw (Exception) e.getCause(); throw e; }
    }

    private static WorldController scene() { return new WorldController(new int[PLANES][SIZE + 1][SIZE + 1]); }

    private static int[] raster() {
        int[] px = new int[W * H];
        Arrays.fill(px, BG);
        DrawingArea.bindRaster(H, W, px);
        Texture.setScreenSize(W, H);
        Texture.transparency = 0;
        return px;
    }

    private static final Class<?>[] M315 = {Class43.class, int.class, int.class, int.class, int.class, int.class, int.class, int.class};
    private static final Class<?>[] M316 = {int.class, int.class, int.class, ShapedTile.class, int.class, int.class, int.class};
    private static final Class<?>[] M317 = {int.class, int.class};
    private static final Class<?>[] M318 = {int.class, int.class, int.class, int.class, int.class, int.class, int.class, int.class};
    private static final Class<?>[] M319 = {};
    private static final Class<?>[] M320 = {int.class, int.class, int.class};
    private static final Class<?>[] M322 = {int.class, int.class, int.class, int.class};
    private static final Class<?>[] M323 = {int.class, int.class, int.class, int.class, int.class, int.class};

    /** Sets the camera statics the way drawScene does (world position, tile position, sines and cosines of pitch and yaw). */
    private static void camera(int x, int height, int y, int pitch, int yaw) throws Exception {
        sset("cameraWorldX", x); sset("cameraWorldHeight", height); sset("cameraWorldY", y);
        sset("cameraTileX", x / 128); sset("cameraTileY", y / 128);
        sset("pitchSine", Model.sineTable16[pitch]); sset("pitchCosine", Model.cosineTable16[pitch]);
        sset("yawSine", Model.sineTable16[yaw]); sset("yawCosine", Model.cosineTable16[yaw]);
    }

    /** Independent projection of a world point: returns {screenX, screenY, depth}. */
    private static int[] project(int wx, int wh, int wy) throws Exception {
        long dx = wx - sint("cameraWorldX"), dh = wh - sint("cameraWorldHeight"), dy = wy - sint("cameraWorldY");
        long ys = sint("yawSine"), yc = sint("yawCosine"), ps = sint("pitchSine"), pc = sint("pitchCosine");
        long x1 = Math.floorDiv(dy * ys + dx * yc, 65536L);
        long d1 = Math.floorDiv(dy * yc - dx * ys, 65536L);
        long h2 = Math.floorDiv(dh * pc - d1 * ps, 65536L);
        long d2 = Math.floorDiv(dh * ps + d1 * pc, 65536L);
        if (d2 < 50) return new int[] {0, 0, (int) d2};
        return new int[] {Texture.textureInt1 + (int) (x1 * 512 / d2), Texture.textureInt2 + (int) (h2 * 512 / d2), (int) d2};
    }

    private static void oracleTriangle(int[] a, int[] b, int[] c, int ca, int cb, int cc) throws Exception {
        if ((a[0] - b[0]) * (c[1] - b[1]) - (a[1] - b[1]) * (c[0] - b[0]) <= 0) return;
        int limit = DrawingArea.scanlineRightLimit;
        tset("clipScanlines", a[0] < 0 || b[0] < 0 || c[0] < 0 || a[0] > limit || b[0] > limit || c[0] > limit);
        Texture.drawShadedTriangle(a[1], b[1], c[1], a[0], b[0], c[0], ca, cb, cc);
    }


    // ================= drawScene: camera and draw-window statics =================

    private static final Class<?>[] DRAW_SCENE = {int.class, int.class, int.class, int.class, int.class, int.class};

    @Test public void drawSceneStoresCameraPlaneFrameStampTrigAndTileWindow() throws Exception {
        WorldController wc = scene();
        sset("drawCycle", 10);
        wc.drawScene(5 * 128 + 70, 9 * 128 + 3, 512, -400, 2, 256);
        assertEquals(11, sint("drawCycle"));
        assertEquals(2, sint("cameraPlane"));
        assertEquals(5 * 128 + 70, sint("cameraWorldX"));
        assertEquals(-400, sint("cameraWorldHeight"));
        assertEquals(9 * 128 + 3, sint("cameraWorldY"));
        assertEquals(5, sint("cameraTileX"));
        assertEquals(9, sint("cameraTileY"));
        assertEquals(Model.sineTable16[256], sint("pitchSine"));
        assertEquals(Model.cosineTable16[256], sint("pitchCosine"));
        assertEquals(Model.sineTable16[512], sint("yawSine"));
        assertEquals(Model.cosineTable16[512], sint("yawCosine"));
        // window = camera tile -/+ draw distance, clamped to the map
        assertEquals(0, sint("drawMinTileX")); assertEquals(30, sint("drawMaxTileX"));
        assertEquals(0, sint("drawMinTileY")); assertEquals(34, sint("drawMaxTileY"));
        wc.drawScene(100 * 128, 90 * 128, 0, -400, 0, 128);
        assertEquals(12, sint("drawCycle"));
        assertEquals(75, sint("drawMinTileX")); assertEquals(SIZE, sint("drawMaxTileX"));
        assertEquals(65, sint("drawMinTileY")); assertEquals(SIZE, sint("drawMaxTileY"));
        WorldController.drawDistance = 7;
        wc.drawScene(50 * 128, 40 * 128, 0, -400, 0, 128);
        assertEquals(43, sint("drawMinTileX")); assertEquals(57, sint("drawMaxTileX"));
        assertEquals(33, sint("drawMinTileY")); assertEquals(47, sint("drawMaxTileY"));
    }

    @Test public void drawSceneClampsTheCameraIntoTheMapAndSelectsVisibilityTableByPitchAndYaw() throws Exception {
        WorldController wc = scene();
        wc.drawScene(-50, -9, 0, 0, 0, 128);
        assertEquals(0, sint("cameraWorldX")); assertEquals(0, sint("cameraWorldY"));
        assertEquals(0, sint("cameraTileX")); assertEquals(0, sint("cameraTileY"));
        wc.drawScene(SIZE * 128, SIZE * 128 + 500, 0, 0, 0, 128);
        assertEquals(SIZE * 128 - 1, sint("cameraWorldX")); assertEquals(SIZE * 128 - 1, sint("cameraWorldY"));
        assertEquals(SIZE - 1, sint("cameraTileX")); assertEquals(SIZE - 1, sint("cameraTileY"));
        boolean[][][][] table = (boolean[][][][]) sget("tileVisibilityTable");
        wc.drawScene(1000, 1000, 192, 0, 0, 160);
        assertSame(table[1][3], sget("cameraTileVisibility"));
        wc.drawScene(1000, 1000, 1984, 0, 0, 128 + 32 * 7);
        assertSame(table[7][31], sget("cameraTileVisibility"));
    }

    private static Ground tile(WorldController wc, int x, int y) {
        Ground g = new Ground(0, x, y);
        return g;
    }

    @Test public void method314DrawingATileDecrementsTheCountOfTilesStillToDrawOnlyWhenTheTileIsPending() throws Exception {
        WorldController wc = scene();
        camera(5 * 128, -300, 5 * 128, 128, 0);
        sset("cameraPlane", 0); sset("drawMinTileX", 0); sset("drawMaxTileX", SIZE); sset("drawMinTileY", 0); sset("drawMaxTileY", SIZE);
        sset("cameraTileVisibility", new boolean[51][51]);
        Ground pending = tile(wc, 20, 20);
        pending.firstPassPending = true; pending.drawPending = true;
        sset("pendingTileCount", 3);
        Method m = WorldController.class.getDeclaredMethod("drawTileQueue", Ground.class, boolean.class);
        m.setAccessible(true);
        Field tiles = field(WorldController.class, "groundArray");
        Ground[][][] g = (Ground[][][]) tiles.get(wc);
        g[0][20][20] = pending;
        m.invoke(wc, pending, false);
        assertEquals(2, sint("pendingTileCount"));
        assertFalse(pending.drawPending);
        assertFalse(pending.firstPassPending);
        Ground notPending = tile(wc, 21, 20);
        notPending.firstPassPending = true; notPending.drawPending = false;
        g[0][21][20] = notPending;
        m.invoke(wc, notPending, false);
        assertEquals("a tile that was already drawn is not counted again", 2, sint("pendingTileCount"));
    }

    // ================= requestTilePick =================

    @Test public void requestTilePickStoresScreenPositionAndClearsThePickedTile() throws Exception {
        WorldController wc = scene();
        sset("pickedTileX", 7); sset("pickedTileY", 8);
        wc.requestTilePick(33, 111);
        assertEquals(true, sget("tilePickRequested"));
        assertEquals(111, sint("pickScreenX"));
        assertEquals(33, sint("pickScreenY"));
        assertEquals(-1, WorldController.pickedTileX);
        assertEquals(-1, WorldController.pickedTileY);
    }

    // ================= shadeTextureHsl: low-memory texture shade =================

    private static int expectedShade(int base, int shade) {
        int lightness = (127 - shade) * (base & 0x7f) / 160;
        lightness = Math.max(2, Math.min(126, lightness));
        return (base & 0xff80) + lightness;
    }

    @Test public void method317KeepsHueAndSaturationOfTheBaseAndScalesItsLightnessByTheInverseShadeClampedTo2To126() throws Exception {
        WorldController wc = scene();
        int[] bases = {0x9950, 0x1234, 0xff7f, 0x0000, 0x8000 | 0x40, 0xa87f};
        for (int base : bases) {
            for (int shade = -60; shade <= 127; shade++) {
                assertEquals("base " + base + " shade " + shade, expectedShade(base, shade), call(wc, "shadeTextureHsl", M317, base, shade));
            }
        }
        // the clamp is reached at both ends
        assertEquals((0xff7f & 0xff80) + 2, call(wc, "shadeTextureHsl", M317, 0xff7f, 127));
        assertEquals(100, ((Integer) call(wc, "shadeTextureHsl", M317, 0x007f, 0)) & 0x7f);
        assertEquals("upper clamp is only reachable with a negative shade", 126, ((Integer) call(wc, "shadeTextureHsl", M317, 0x007f, -100)) & 0x7f);
        // hue/saturation (bits 7..15) of the base never changes and the first argument is the base
        assertEquals(0x1200, ((Integer) call(wc, "shadeTextureHsl", M317, 0x1234, 50)) & 0xff80);
        assertNotEquals(call(wc, "shadeTextureHsl", M317, 0x1234, 50), call(wc, "shadeTextureHsl", M317, 0x1034, 50));
    }

    // ================= isPointInTriangle: point strictly inside a triangle =================

    @Test public void method318IsTrueForPointsStrictlyInsideEitherWindingFalseOnEdgesOutsideAndForDegenerateTriangles() throws Exception {
        WorldController wc = scene();
        // arguments: pointX, pointY, y1, y2, y3, x1, x2, x3
        int[][] tri = {{10, 10, 90, 20, 40, 80}, {10, 10, 90, 20, 80, 40}}; // {y1,y2,y3,x1,x2,x3} windings
        int[][] xs = {{10, 90, 20}, {10, 90, 20}};
        for (int w = 0; w < 2; w++) {
            int y1 = 10, y2 = 10, y3 = 90, x1 = 20, x2 = 90, x3 = 40;
            if (w == 1) { int t = x2; x2 = x3; x3 = t; }
            for (int py = 0; py <= 100; py += 5) {
                for (int px = 0; px <= 100; px += 5) {
                    boolean expected = insideStrict(px, py, x1, y1, x2, y2, x3, y3);
                    assertEquals("w" + w + " " + px + "," + py, expected, call(wc, "isPointInTriangle", M318, px, py, y1, y2, y3, x1, x2, x3));
                }
            }
        }
        assertEquals(true, call(wc, "isPointInTriangle", M318, 45, 30, 10, 10, 90, 20, 90, 40));
        assertEquals("on the top edge", false, call(wc, "isPointInTriangle", M318, 50, 10, 10, 10, 90, 20, 90, 40));
        assertEquals("on a vertex", false, call(wc, "isPointInTriangle", M318, 20, 10, 10, 10, 90, 20, 90, 40));
        assertEquals("above", false, call(wc, "isPointInTriangle", M318, 45, 9, 10, 10, 90, 20, 90, 40));
        assertEquals("left of the bounding box", false, call(wc, "isPointInTriangle", M318, 19, 30, 10, 10, 90, 20, 90, 40));
        assertEquals("degenerate", false, call(wc, "isPointInTriangle", M318, 30, 30, 10, 20, 30, 10, 20, 30));
        // x is the first argument and y the second: swapping them changes the answer for an asymmetric triangle
        assertEquals(true, call(wc, "isPointInTriangle", M318, 50, 20, 10, 10, 90, 20, 90, 40));
        assertEquals(false, call(wc, "isPointInTriangle", M318, 20, 50, 10, 10, 90, 20, 90, 40));
    }

    private static boolean insideStrict(int px, int py, int x1, int y1, int x2, int y2, int x3, int y3) {
        long a = (long) (x2 - x1) * (py - y1) - (long) (y2 - y1) * (px - x1);
        long b = (long) (x3 - x2) * (py - y2) - (long) (y3 - y2) * (px - x2);
        long c = (long) (x1 - x3) * (py - y3) - (long) (y1 - y3) * (px - x3);
        return (a > 0 && b > 0 && c > 0) || (a < 0 && b < 0 && c < 0);
    }

    // ================= drawPlainTile: plain (single-colour-per-corner) tile =================


    private static void setHeights(WorldController wc, int plane, int x, int y, int h0, int h1, int h2, int h3) throws Exception {
        int[][][] heights = (int[][][]) get(wc, "tileHeights");
        heights[plane][x][y] = h0; heights[plane][x + 1][y] = h1; heights[plane][x + 1][y + 1] = h2; heights[plane][x][y + 1] = h3;
    }

    private static final int TX = 10, TY = 12;

    @Test public void method315DrawsTwoGouraudTrianglesBetweenTheProjectedCornersWithTheColoursInCornerOrder() throws Exception {
        WorldController wc = scene();
        setHeights(wc, 1, TX, TY, 0, 12, 25, -9);
        camera(TX * 128 + 64, -400, TY * 128 - 700, 128, 0);
        Class43 t = new Class43(0x1150, 0x2900, 0x4a80, 0x6fe0, -1, 77, false);
        int[] px = raster();
        call(wc, "drawPlainTile", M315, t, 1, sint("pitchSine"), sint("pitchCosine"), sint("yawSine"), sint("yawCosine"), TX, TY);
        int[] actual = px.clone();
        int[] p0 = project(TX * 128, 0, TY * 128), p1 = project(TX * 128 + 128, 12, TY * 128);
        int[] p2 = project(TX * 128 + 128, 25, TY * 128 + 128), p3 = project(TX * 128, -9, TY * 128 + 128);
        int[] expected = raster();
        oracleTriangle(p2, p3, p1, 0x4a80, 0x6fe0, 0x2900);
        oracleTriangle(p0, p1, p3, 0x1150, 0x2900, 0x6fe0);
        assertFalse("something was drawn", Arrays.equals(expected, raster()));
        assertArrayEquals(expected, actual);
    }

    @Test public void method315UsesTheHeightsOfThePlaneArgumentAndTheTileXYArgumentsInThatOrder() throws Exception {
        WorldController wc = scene();
        setHeights(wc, 2, 3, 7, 0, 40, 40, 0);
        setHeights(wc, 0, 3, 7, 5, 5, 5, 5);
        setHeights(wc, 2, 7, 3, 90, 90, 90, 90);
        camera(3 * 128 + 64, -400, 7 * 128 - 700, 128, 0);
        Class43 t = new Class43(0x1150, 0x2900, 0x4a80, 0x6fe0, -1, 77, false);
        int[] px = raster();
        call(wc, "drawPlainTile", M315, t, 2, sint("pitchSine"), sint("pitchCosine"), sint("yawSine"), sint("yawCosine"), 3, 7);
        int[] drawn = px.clone();
        int[] p0 = project(3 * 128, 0, 7 * 128), p1 = project(3 * 128 + 128, 40, 7 * 128);
        int[] p2 = project(3 * 128 + 128, 40, 7 * 128 + 128), p3 = project(3 * 128, 0, 7 * 128 + 128);
        int[] expected = raster();
        oracleTriangle(p2, p3, p1, 0x4a80, 0x6fe0, 0x2900);
        oracleTriangle(p0, p1, p3, 0x1150, 0x2900, 0x6fe0);
        assertArrayEquals(expected, drawn);
    }

    private static void backAlongView(int yaw, int back) throws Exception {
        double a = yaw * Math.PI / 1024;
        camera((int) Math.round(TX * 128 + 64 + Math.sin(a) * back), -10, (int) Math.round(TY * 128 + 64 - Math.cos(a) * back), 0, yaw);
    }

    @Test public void method315SkipsTheWholeTileWhenAnyOneCornerIsNearerThanFiftyUnits() throws Exception {
        WorldController wc = scene();
        int[] yaws = {1792, 256, 768, 1280}; // the yaw for which corner 0, 1, 2, 3 is the single nearest corner
        int[] cx = {TX * 128, TX * 128 + 128, TX * 128 + 128, TX * 128};
        int[] cy = {TY * 128, TY * 128, TY * 128 + 128, TY * 128 + 128};
        int nonEmpty = 0;
        for (int corner = 0; corner < 4; corner++) {
            int back;
            for (back = 0; back < 3000; back++) {
                backAlongView(yaws[corner], back);
                int min = Integer.MAX_VALUE;
                for (int c = 0; c < 4; c++) min = Math.min(min, project(cx[c], 0, cy[c])[2]);
                if (min >= 50) break;
            }
            for (int step = -1; step <= 0; step++) {
                backAlongView(yaws[corner], back + step);
                int below = 0, nearest = -1;
                for (int c = 0; c < 4; c++) if (project(cx[c], 0, cy[c])[2] < 50) { below++; nearest = c; }
                int[] px = raster();
                Class43 t = new Class43(0x1150, 0x2900, 0x4a80, 0x6fe0, -1, 77, false);
                call(wc, "drawPlainTile", M315, t, 1, sint("pitchSine"), sint("pitchCosine"), sint("yawSine"), sint("yawCosine"), TX, TY);
                if (step == -1) {
                    assertEquals("exactly one corner is too near", 1, below);
                    assertEquals(corner, nearest);
                    assertArrayEquals("corner " + corner, raster(), px);
                } else {
                    assertEquals(0, below);
                    int[] p0 = project(cx[0], 0, cy[0]), p1 = project(cx[1], 0, cy[1]), p2 = project(cx[2], 0, cy[2]), p3 = project(cx[3], 0, cy[3]);
                    int[] actual = px.clone();
                    int[] expected = raster();
                    oracleTriangle(p2, p3, p1, 0x4a80, 0x6fe0, 0x2900);
                    oracleTriangle(p0, p1, p3, 0x1150, 0x2900, 0x6fe0);
                    if (!Arrays.equals(expected, raster())) nonEmpty++;
                    assertArrayEquals("corner " + corner, expected, actual);
                }
            }
        }
        assertTrue("the accepted side drew something for the corners", nonEmpty >= 2);
    }

    @Test public void method315DoesNotDrawTilesSeenFromBelowAndSkipsTheNoColourMarker() throws Exception {
        WorldController wc = scene();
        camera(TX * 128 + 64, 500, TY * 128 - 700, 128, 0); // below the ground: back face
        Class43 t = new Class43(0x1150, 0x2900, 0x4a80, 0x6fe0, -1, 77, false);
        int[] px = raster();
        call(wc, "drawPlainTile", M315, t, 1, sint("pitchSine"), sint("pitchCosine"), sint("yawSine"), sint("yawCosine"), TX, TY);
        assertArrayEquals(raster(), px);
        camera(TX * 128 + 64, -400, TY * 128 - 700, 128, 0);
        Class43 hidden = new Class43(0xbc614e, 0xbc614e, 0xbc614e, 0xbc614e, -1, 77, false);
        px = raster();
        call(wc, "drawPlainTile", M315, hidden, 1, sint("pitchSine"), sint("pitchCosine"), sint("yawSine"), sint("yawCosine"), TX, TY);
        assertArrayEquals("0xbc614e untextured colours draw nothing", raster(), px);
        // each triangle is skipped on its own: triangle 1 uses colour 718 and triangle 2 uses colour 716
        Class43 onlyFirst = new Class43(0xbc614e, 0x2900, 0x4a80, 0x6fe0, -1, 77, false);
        int[] a = raster();
        call(wc, "drawPlainTile", M315, onlyFirst, 1, sint("pitchSine"), sint("pitchCosine"), sint("yawSine"), sint("yawCosine"), TX, TY);
        Class43 onlySecond = new Class43(0x1150, 0x2900, 0xbc614e, 0x6fe0, -1, 77, false);
        int[] b = raster();
        call(wc, "drawPlainTile", M315, onlySecond, 1, sint("pitchSine"), sint("pitchCosine"), sint("yawSine"), sint("yawCosine"), TX, TY);
        Class43 full = new Class43(0x1150, 0x2900, 0x4a80, 0x6fe0, -1, 77, false);
        int[] c = raster();
        call(wc, "drawPlainTile", M315, full, 1, sint("pitchSine"), sint("pitchCosine"), sint("yawSine"), sint("yawCosine"), TX, TY);
        int onlyFirstPixels = 0, onlySecondPixels = 0;
        for (int i = 0; i < c.length; i++) {
            if (a[i] != BG) onlyFirstPixels++;
            if (b[i] != BG) onlySecondPixels++;
        }
        assertTrue(onlyFirstPixels > 0 && onlySecondPixels > 0);
        // triangle 1 (corners 2,3,1) is drawn when colour 716 is the marker (718 present); triangle 2 (corners 0,1,3) when 718 is the marker
        int[] p0 = project(TX * 128, 0, TY * 128), p1 = project(TX * 128 + 128, 0, TY * 128);
        int[] p2 = project(TX * 128 + 128, 0, TY * 128 + 128), p3 = project(TX * 128, 0, TY * 128 + 128);
        int[] expFirst = raster();
        oracleTriangle(p2, p3, p1, 0x4a80, 0x6fe0, 0x2900);
        assertArrayEquals(expFirst, a);
        int[] expSecond = raster();
        oracleTriangle(p0, p1, p3, 0x1150, 0x2900, 0x6fe0);
        assertArrayEquals(expSecond, b);
    }

    @Test public void method315LowMemoryTexturedTileIsShadedFromTheTextureTableByTextureId() throws Exception {
        WorldController wc = scene();
        camera(TX * 128 + 64, -400, TY * 128 - 700, 128, 0);
        int[] table = (int[]) sget("lowMemTextureHsl");
        int texture = 3; // table[3] = 4643
        assertEquals(4643, table[texture]);
        Class43 t = new Class43(20, 60, 100, 30, texture, 5, true);
        int[] px = raster();
        call(wc, "drawPlainTile", M315, t, 1, sint("pitchSine"), sint("pitchCosine"), sint("yawSine"), sint("yawCosine"), TX, TY);
        int[] actual = px.clone();
        int[] p0 = project(TX * 128, 0, TY * 128), p1 = project(TX * 128 + 128, 0, TY * 128);
        int[] p2 = project(TX * 128 + 128, 0, TY * 128 + 128), p3 = project(TX * 128, 0, TY * 128 + 128);
        int[] expected = raster();
        int base = table[texture];
        oracleTriangle(p2, p3, p1, expectedShade(base, 100), expectedShade(base, 30), expectedShade(base, 60));
        oracleTriangle(p0, p1, p3, expectedShade(base, 20), expectedShade(base, 60), expectedShade(base, 30));
        assertArrayEquals(expected, actual);
        // and it is not the untextured colour path
        Class43 untextured = new Class43(20, 60, 100, 30, -1, 5, true);
        int[] other = raster();
        call(wc, "drawPlainTile", M315, untextured, 1, sint("pitchSine"), sint("pitchCosine"), sint("yawSine"), sint("yawCosine"), TX, TY);
        assertFalse(Arrays.equals(actual, other));
    }

    @Test public void method315PicksTheTileWhenTheRequestedScreenPointIsInsideEitherTriangleOnlyWhileARequestIsPending() throws Exception {
        WorldController wc = scene();
        setHeights(wc, 1, TX, TY, 0, 12, 25, -9);
        raster();
        camera(TX * 128 + 64, -400, TY * 128 - 700, 128, 0);
        Class43 t = new Class43(0x1150, 0x2900, 0x4a80, 0x6fe0, -1, 77, false);
        int[] p0 = project(TX * 128, 0, TY * 128), p1 = project(TX * 128 + 128, 12, TY * 128);
        int[] p2 = project(TX * 128 + 128, 25, TY * 128 + 128), p3 = project(TX * 128, -9, TY * 128 + 128);
        int[][][] cases = {{p2, p3, p1}, {p0, p1, p3}};
        for (int[][] triangle : cases) {
            int cx = (triangle[0][0] + triangle[1][0] + triangle[2][0]) / 3, cy = (triangle[0][1] + triangle[1][1] + triangle[2][1]) / 3;
            raster();
            wc.requestTilePick(cy, cx);
            call(wc, "drawPlainTile", M315, t, 1, sint("pitchSine"), sint("pitchCosine"), sint("yawSine"), sint("yawCosine"), TX, TY);
            assertEquals(TX, WorldController.pickedTileX);
            assertEquals(TY, WorldController.pickedTileY);
            // no pending request, no pick
            sset("tilePickRequested", false);
            sset("pickedTileX", -1); sset("pickedTileY", -1);
            call(wc, "drawPlainTile", M315, t, 1, sint("pitchSine"), sint("pitchCosine"), sint("yawSine"), sint("yawCosine"), TX, TY);
            assertEquals(-1, WorldController.pickedTileX);
        }
        wc.requestTilePick(2, 2);
        call(wc, "drawPlainTile", M315, t, 1, sint("pitchSine"), sint("pitchCosine"), sint("yawSine"), sint("yawCosine"), TX, TY);
        assertEquals("a point outside the tile picks nothing", -1, WorldController.pickedTileX);
        assertEquals(-1, WorldController.pickedTileY);
    }

    // ================= drawShapedTile: shaped tile (ShapedTile) =================

    private static ShapedTile shapedTile(WorldController wc, int shape, int rotation, int texture, int[] heights) throws Exception {
        wc.addTile(1, TX, TY, shape, rotation, texture, heights[0], heights[1], heights[2], heights[3], 0x1150, 0x2900, 0x4a80, 0x6fe0, 21, 60, 100, 30, 77, 88);
        Ground g = ((Ground[][][]) get(wc, "groundArray"))[1][TX][TY];
        return (ShapedTile) g.shapedTile;
    }

    private static void call316(WorldController wc, ShapedTile t) throws Exception {
        // arguments: tileX, pitchSine, yawSine, tile, pitchCosine, tileY, yawCosine
        call(wc, "drawShapedTile", M316, TX, sint("pitchSine"), sint("yawSine"), t, sint("pitchCosine"), TY, sint("yawCosine"));
    }

    @Test public void method316DrawsEveryFaceBetweenTheProjectedVerticesWithItsThreeColours() throws Exception {
        for (int shape = 2; shape <= 4; shape++) {
            WorldController wc = scene();
            ShapedTile t = shapedTile(wc, shape, shape - 2, -1, new int[] {0, 12, 25, -9});
            camera(TX * 128 + 64, -400, TY * 128 - 700, 128, 0);
            int[] px = raster();
            call316(wc, t);
            int[] actual = px.clone();
            int[] expected = raster();
            int n = t.faceVertexA.length;
            assertTrue(n >= 2);
            for (int f = 0; f < n; f++) {
                int[] a = project(t.vertexX[t.faceVertexA[f]], t.vertexHeight[t.faceVertexA[f]], t.vertexY[t.faceVertexA[f]]);
                int[] b = project(t.vertexX[t.faceVertexB[f]], t.vertexHeight[t.faceVertexB[f]], t.vertexY[t.faceVertexB[f]]);
                int[] c = project(t.vertexX[t.faceVertexC[f]], t.vertexHeight[t.faceVertexC[f]], t.vertexY[t.faceVertexC[f]]);
                oracleTriangle(a, b, c, t.faceColourA[f], t.faceColourB[f], t.faceColourC[f]);
            }
            assertFalse(Arrays.equals(expected, raster()));
            assertArrayEquals("shape " + shape, expected, actual);
        }
    }

    @Test public void method316ShadesTexturedFacesFromTheTextureTableInLowMemoryAndSkipsBackFaces() throws Exception {
        WorldController wc = scene();
        ShapedTile t = shapedTile(wc, 2, 0, 3, new int[] {0, 0, 0, 0});
        assertNotNull(t.faceTexture);
        camera(TX * 128 + 64, -400, TY * 128 - 700, 128, 0);
        int[] px = raster();
        call316(wc, t);
        int[] actual = px.clone();
        int[] expected = raster();
        int[] table = (int[]) sget("lowMemTextureHsl");
        for (int f = 0; f < t.faceVertexA.length; f++) {
            int[] a = project(t.vertexX[t.faceVertexA[f]], t.vertexHeight[t.faceVertexA[f]], t.vertexY[t.faceVertexA[f]]);
            int[] b = project(t.vertexX[t.faceVertexB[f]], t.vertexHeight[t.faceVertexB[f]], t.vertexY[t.faceVertexB[f]]);
            int[] c = project(t.vertexX[t.faceVertexC[f]], t.vertexHeight[t.faceVertexC[f]], t.vertexY[t.faceVertexC[f]]);
            if (t.faceTexture[f] == -1) oracleTriangle(a, b, c, t.faceColourA[f], t.faceColourB[f], t.faceColourC[f]);
            else {
                int base = table[t.faceTexture[f]];
                oracleTriangle(a, b, c, expectedShade(base, t.faceColourA[f]), expectedShade(base, t.faceColourB[f]), expectedShade(base, t.faceColourC[f]));
            }
        }
        assertArrayEquals(expected, actual);
        camera(TX * 128 + 64, 500, TY * 128 - 700, 128, 0);
        px = raster();
        call316(wc, t);
        assertArrayEquals("seen from below nothing is drawn", raster(), px);
    }

    @Test public void method316SkipsEverythingWhenAnyVertexIsNearerThanFiftyUnitsAndKeepsScratchArraysOfTheLastProjection() throws Exception {
        WorldController wc = scene();
        ShapedTile t = shapedTile(wc, 2, 0, -1, new int[] {0, 0, 0, 0});
        camera(TX * 128 + 64, -400, TY * 128 - 700, 128, 0);
        int[] px = raster();
        call316(wc, t);
        for (int v = 0; v < 4; v++) {
            int[] p = project(t.vertexX[v], t.vertexHeight[v], t.vertexY[v]);
            assertEquals(p[0], ShapedTile.projectedX[v]);
            assertEquals(p[1], ShapedTile.projectedY[v]);
        }
        int[] baseline = px.clone();
        assertFalse(Arrays.equals(raster(), baseline));
        // move the camera forward so the near edge (vertices 0 and 1) has depth below 50: nothing drawn at all
        camera(TX * 128 + 64, -400, TY * 128 - 20, 0, 0);
        px = raster();
        call316(wc, t);
        assertArrayEquals(raster(), px);
    }

    @Test public void method316KeepsCameraSpaceCoordinatesOfTexturedTileVerticesInTheScratchArraysAndFlatFlagIsAllHeightsEqual() throws Exception {
        WorldController wc = scene();
        ShapedTile t = shapedTile(wc, 2, 0, 3, new int[] {0, 12, 25, -9});
        assertFalse(t.flat);
        camera(TX * 128 + 64, -400, TY * 128 - 700, 128, 0);
        raster();
        call316(wc, t);
        for (int v = 0; v < 4; v++) {
            long dx = t.vertexX[v] - sint("cameraWorldX"), dh = t.vertexHeight[v] - sint("cameraWorldHeight"), dy = t.vertexY[v] - sint("cameraWorldY");
            long ys = sint("yawSine"), yc = sint("yawCosine"), ps = sint("pitchSine"), pc = sint("pitchCosine");
            long x1 = Math.floorDiv(dy * ys + dx * yc, 65536L), d1 = Math.floorDiv(dy * yc - dx * ys, 65536L);
            long h2 = Math.floorDiv(dh * pc - d1 * ps, 65536L), d2 = Math.floorDiv(dh * ps + d1 * pc, 65536L);
            assertEquals(x1, ShapedTile.cameraSpaceX[v]);
            assertEquals(h2, ShapedTile.cameraSpaceHeight[v]);
            assertEquals(d2, ShapedTile.cameraSpaceDepth[v]);
        }
        for (int mask = 0; mask < 16; mask++) {
            int[] h = new int[4];
            Arrays.fill(h, 100);
            boolean allEqual = true;
            for (int i = 0; i < 4; i++) if ((mask >> i & 1) != 0 && mask != 15) { h[i] = 101; allEqual = false; }
            assertEquals("heights " + Arrays.toString(h), allEqual, shapedTile(scene(), 2, 0, -1, h).flat);
        }
    }

    @Test public void method316PicksTheTileFromTheRequestedPointInsideAFace() throws Exception {
        WorldController wc = scene();
        ShapedTile t = shapedTile(wc, 2, 0, -1, new int[] {0, 0, 0, 0});
        raster();
        camera(TX * 128 + 64, -400, TY * 128 - 700, 128, 0);
        int f = 0;
        int[] a = project(t.vertexX[t.faceVertexA[f]], t.vertexHeight[t.faceVertexA[f]], t.vertexY[t.faceVertexA[f]]);
        int[] b = project(t.vertexX[t.faceVertexB[f]], t.vertexHeight[t.faceVertexB[f]], t.vertexY[t.faceVertexB[f]]);
        int[] c = project(t.vertexX[t.faceVertexC[f]], t.vertexHeight[t.faceVertexC[f]], t.vertexY[t.faceVertexC[f]]);
        raster();
        wc.requestTilePick((a[1] + b[1] + c[1]) / 3, (a[0] + b[0] + c[0]) / 3);
        call316(wc, t);
        assertEquals(TX, WorldController.pickedTileX);
        assertEquals(TY, WorldController.pickedTileY);
        wc.requestTilePick(1, 1);
        call316(wc, t);
        assertEquals(-1, WorldController.pickedTileX);
    }

    // ================= selectActiveOccluders: choose the occluders that are active for the camera =================

    private static Class47 occ(int plane, int index) throws Exception { return ((Class47[][]) sget("occluders"))[plane][index]; }

    private static void visibleEverywhere() throws Exception {
        boolean[][] vis = new boolean[51][51];
        for (boolean[] r : vis) Arrays.fill(r, true);
        sset("cameraTileVisibility", vis);
    }

    @Test public void method319ActivatesAnXFixedOccluderOnTheSideOfTheCameraWithSlopesScaledBy256PerUnitOfDistance() throws Exception {
        WorldController wc = scene();
        WorldController.addOccluder(0, 1280, -100, 1280, 2944, -300, 2560, 1);
        visibleEverywhere();
        sset("cameraPlane", 0);
        camera(1280 + 200, -200, 2752, 128, 0);
        call(wc, "selectActiveOccluders", M319);
        assertEquals(1, sint("activeOccluderCount"));
        Class47 o = occ(0, 0);
        assertSame(o, ((Class47[]) sget("activeOccluders"))[0]);
        assertEquals(1, o.side);
        assertEquals((2560 - 2752 << 8) / 200, o.minYSlope);
        assertEquals((2944 - 2752 << 8) / 200, o.maxYSlope);
        assertEquals((-300 + 200 << 8) / 200, o.minHeightSlope);
        assertEquals((-100 + 200 << 8) / 200, o.maxHeightSlope);
        // the other side of the wall
        camera(1280 - 100, -200, 2752, 128, 0);
        call(wc, "selectActiveOccluders", M319);
        assertEquals(1, sint("activeOccluderCount"));
        assertEquals(2, o.side);
        assertEquals((2560 - 2752 << 8) / 100, o.minYSlope);
        assertEquals((2944 - 2752 << 8) / 100, o.maxYSlope);
        // within 32 units of the wall plane: not active; list is rebuilt from zero each call
        camera(1280 + 32, -200, 2752, 128, 0);
        call(wc, "selectActiveOccluders", M319);
        assertEquals(0, sint("activeOccluderCount"));
        camera(1280 - 32, -200, 2752, 128, 0);
        call(wc, "selectActiveOccluders", M319);
        assertEquals(0, sint("activeOccluderCount"));
        camera(1280 + 33, -200, 2752, 128, 0);
        call(wc, "selectActiveOccluders", M319);
        assertEquals(1, sint("activeOccluderCount"));
        camera(1280 - 33, -200, 2752, 128, 0);
        call(wc, "selectActiveOccluders", M319);
        assertEquals(1, sint("activeOccluderCount"));
    }

    @Test public void method319ActivatesAYFixedOccluderAndAHorizontalOccluderWithTheirOwnSides() throws Exception {
        WorldController wc = scene();
        WorldController.addOccluder(0, 1280, -100, 1664, 2560, -300, 2560, 2);
        visibleEverywhere();
        sset("cameraPlane", 0);
        camera(1472, -200, 2560 + 200, 128, 0);
        call(wc, "selectActiveOccluders", M319);
        Class47 o = occ(0, 0);
        assertEquals(1, sint("activeOccluderCount"));
        assertEquals(3, o.side);
        assertEquals((1280 - 1472 << 8) / 200, o.minXSlope);
        assertEquals((1664 - 1472 << 8) / 200, o.maxXSlope);
        assertEquals((-300 + 200 << 8) / 200, o.minHeightSlope);
        assertEquals((-100 + 200 << 8) / 200, o.maxHeightSlope);
        camera(1472, -200, 2560 - 100, 128, 0);
        call(wc, "selectActiveOccluders", M319);
        assertEquals(4, o.side);
        assertEquals((1280 - 1472 << 8) / 100, o.minXSlope);
        camera(1472, -200, 2560 + 32, 128, 0);
        call(wc, "selectActiveOccluders", M319);
        assertEquals(0, sint("activeOccluderCount"));
        // type 4: a horizontal occluder lying more than 128 units below the camera (minHeight is the "-300" side)
        sset("occluderCounts", new int[4]); sset("occluders", new Class47[4][500]);
        WorldController.addOccluder(0, 1280, -100, 1664, 2944, -50, 2560, 4);
        Class47 h = occ(0, 0);
        camera(1472, -300, 2752, 128, 0);
        call(wc, "selectActiveOccluders", M319);
        assertEquals(1, sint("activeOccluderCount"));
        assertSame(h, ((Class47[]) sget("activeOccluders"))[0]);
        assertEquals(5, h.side);
        int depth = -50 + 300;
        assertEquals((1280 - 1472 << 8) / depth, h.minXSlope);
        assertEquals((1664 - 1472 << 8) / depth, h.maxXSlope);
        assertEquals((2560 - 2752 << 8) / depth, h.minYSlope);
        assertEquals((2944 - 2752 << 8) / depth, h.maxYSlope);
        camera(1472, -178, 2752, 128, 0); // depth exactly 128: not active; 129 is
        call(wc, "selectActiveOccluders", M319);
        assertEquals(0, sint("activeOccluderCount"));
        camera(1472, -179, 2752, 128, 0);
        call(wc, "selectActiveOccluders", M319);
        assertEquals(1, sint("activeOccluderCount"));
    }

    @Test public void method319OnlyUsesTheOccludersOfTheCameraPlaneAndKeepsThemInInsertionOrder() throws Exception {
        WorldController wc = scene();
        WorldController.addOccluder(0, 1280, -100, 1280, 2944, -300, 2560, 1);
        WorldController.addOccluder(1, 1280, -100, 1280, 2944, -300, 2560, 1);
        WorldController.addOccluder(0, 1408, -100, 1408, 2944, -300, 2560, 1);
        visibleEverywhere();
        camera(1700, -200, 2752, 128, 0);
        sset("cameraPlane", 0);
        call(wc, "selectActiveOccluders", M319);
        assertEquals(2, sint("activeOccluderCount"));
        assertSame(occ(0, 0), ((Class47[]) sget("activeOccluders"))[0]);
        assertSame(occ(0, 1), ((Class47[]) sget("activeOccluders"))[1]);
        sset("cameraPlane", 1);
        call(wc, "selectActiveOccluders", M319);
        assertEquals(1, sint("activeOccluderCount"));
        assertSame(occ(1, 0), ((Class47[]) sget("activeOccluders"))[0]);
        sset("cameraPlane", 3);
        call(wc, "selectActiveOccluders", M319);
        assertEquals(0, sint("activeOccluderCount"));
    }

    @Test public void method319NeedsAVisibleTileInTheOccluderSpanWithinTheFiftyByFiftyWindow() throws Exception {
        WorldController wc = scene();
        // occluder over tile column 10, tile rows 20..23; camera tile (13, 21): table column 10 - 13 + 25 = 22, rows 24..27
        WorldController.addOccluder(0, 1280, -100, 1280, 2944, -300, 2560, 1);
        sset("cameraPlane", 0);
        camera(13 * 128 + 5, -200, 21 * 128 + 5, 128, 0);
        for (int row = 20; row <= 30; row++) {
            boolean[][] vis = new boolean[51][51];
            vis[22][row] = true;
            sset("cameraTileVisibility", vis);
            call(wc, "selectActiveOccluders", M319);
            assertEquals("row " + row, row >= 24 && row <= 27 ? 1 : 0, sint("activeOccluderCount"));
        }
        boolean[][] vis = new boolean[51][51];
        vis[21][25] = true; vis[23][25] = true;
        sset("cameraTileVisibility", vis);
        call(wc, "selectActiveOccluders", M319);
        assertEquals("other columns do not count", 0, sint("activeOccluderCount"));
        // the occluder's own column must be inside 0..50 of the table
        sset("occluderCounts", new int[4]); sset("occluders", new Class47[4][500]);
        WorldController.addOccluder(0, 1280, -100, 1280, 2944, -300, 2560, 1);
        for (int camTileX = 10 - 25 + 0; camTileX <= 10 + 26; camTileX++) {
            if (camTileX < 0) continue;
            camera(camTileX * 128 + 5, -200, 21 * 128 + 5, 128, 0);
            visibleEverywhere();
            call(wc, "selectActiveOccluders", M319);
            int column = 10 - camTileX + 25;
            assertEquals("camera tile " + camTileX, column >= 0 && column <= 50 && Math.abs(camTileX * 128 + 5 - 1280) > 32 ? 1 : 0, sint("activeOccluderCount"));
        }
        sset("occluderCounts", new int[4]); sset("occluders", new Class47[4][500]);
        WorldController.addOccluder(0, 12800, -100, 12800, 2944, -300, 2560, 1);
        for (int camTileX = 70; camTileX <= 76; camTileX++) {
            camera(camTileX * 128 + 5, -200, 21 * 128 + 5, 128, 0);
            visibleEverywhere();
            call(wc, "selectActiveOccluders", M319);
            assertEquals("camera tile " + camTileX, 100 - camTileX + 25 <= 50 ? 1 : 0, sint("activeOccluderCount"));
        }
    }

    // ================= isPointOccluded / isTileOccluded / isTileColumnOccluded / isFootprintOccluded: occlusion queries =================

    private static WorldController occludedScene() throws Exception {
        WorldController wc = scene();
        // the wall at x = 1280 hides everything behind it (smaller x) for y 2560..2944 and heights -300..-100
        WorldController.addOccluder(0, 1280, 200, 1280, 3200, -800, 2300, 1);
        visibleEverywhere();
        sset("cameraPlane", 0);
        camera(1280 + 400, -200, 2752, 128, 0);
        call(wc, "selectActiveOccluders", M319);
        assertEquals(1, sint("activeOccluderCount"));
        sset("drawCycle", 5);
        return wc;
    }

    private static boolean activeWithOnlyTileVisible(WorldController wc, int col, int row) throws Exception {
        boolean[][] vis = new boolean[51][51];
        vis[col][row] = true;
        sset("cameraTileVisibility", vis);
        call(wc, "selectActiveOccluders", M319);
        return sint("activeOccluderCount") == 1;
    }

    @Test public void method319ChecksTheVisibleTilesOfTheOccluderSpanForEachOccluderType() throws Exception {
        WorldController wc = scene();
        sset("cameraPlane", 0);
        // type 1: tile column 10 (x 1280), rows 20..23; camera tile (13, 21) -> column 22, rows 24..27
        WorldController.addOccluder(0, 1280, -100, 1280, 2944, -300, 2560, 1);
        camera(13 * 128 + 5, -200, 21 * 128 + 5, 128, 0);
        for (int col = 0; col <= 50; col++) for (int row = 0; row <= 50; row++)
            assertEquals("type 1 " + col + "," + row, col == 22 && row >= 24 && row <= 27, activeWithOnlyTileVisible(wc, col, row));
        // type 2: row 20 (y 2560), columns 10..13; camera tile (12, 22) -> row 23, columns 23..26
        sset("occluderCounts", new int[4]); sset("occluders", new Class47[4][500]);
        WorldController.addOccluder(0, 1280, -100, 1664, 2560, -300, 2560, 2);
        camera(12 * 128 + 5, -200, 22 * 128 + 5, 128, 0);
        for (int col = 0; col <= 50; col++) for (int row = 0; row <= 50; row++)
            assertEquals("type 2 " + col + "," + row, row == 23 && col >= 23 && col <= 26, activeWithOnlyTileVisible(wc, col, row));
        // type 4: columns 10..13, rows 20..23; camera tile (12, 21) -> columns 23..26, rows 24..27
        sset("occluderCounts", new int[4]); sset("occluders", new Class47[4][500]);
        WorldController.addOccluder(0, 1280, -100, 1664, 2944, -50, 2560, 4);
        camera(12 * 128 + 5, -300, 21 * 128 + 5, 128, 0);
        for (int col = 0; col <= 50; col++) for (int row = 0; row <= 50; row++)
            assertEquals("type 4 " + col + "," + row, col >= 23 && col <= 26 && row >= 24 && row <= 27, activeWithOnlyTileVisible(wc, col, row));
        // spans clamp to the 0..50 window of the table
        camera(36 * 128 + 5, -300, 40 * 128 + 5, 128, 0); // columns 10-36+25=-1 -> 0 .. 2, rows 20-40+25=5 .. 8
        assertTrue(activeWithOnlyTileVisible(wc, 0, 5));
        assertTrue(activeWithOnlyTileVisible(wc, 2, 8));
        assertFalse(activeWithOnlyTileVisible(wc, 3, 5));
        assertFalse(activeWithOnlyTileVisible(wc, 0, 9));
        sset("occluderCounts", new int[4]); sset("occluders", new Class47[4][500]);
        WorldController.addOccluder(0, 7680, -100, 8960, 8960, -50, 7680, 4); // tiles 60..70 on both axes
        camera(45 * 128 + 5, -300, 45 * 128 + 5, 128, 0); // 40..50 on both axes
        assertTrue(activeWithOnlyTileVisible(wc, 50, 50));
        assertTrue(activeWithOnlyTileVisible(wc, 40, 40));
        camera(40 * 128 + 5, -300, 40 * 128 + 5, 128, 0); // 45..55 -> clamped to 50
        assertTrue(activeWithOnlyTileVisible(wc, 50, 50));
        assertFalse(activeWithOnlyTileVisible(wc, 44, 50));
        assertTrue(activeWithOnlyTileVisible(wc, 45, 45));
    }

    @Test public void method324BoundariesOfEachOccluderSideComeFromTheOccluderBoundsPlusSlopeTimesDistance() throws Exception {
        WorldController wc = scene();
        sset("cameraPlane", 0);
        visibleEverywhere();
        Class<?>[] t = {int.class, int.class, int.class};
        // sides 1 and 2 (x fixed, camera at larger / smaller x), 3 and 4 (y fixed), 5 (horizontal)
        int[][] setups = {
                {1, 1280, 1280, 2560, 2944, 1480, 2752, -200}, // type, minX, maxX, minY, maxY, camX, camY, camHeight
                {1, 1280, 1280, 2560, 2944, 1100, 2752, -200},
                {2, 1280, 1664, 2560, 2560, 1472, 2760, -200},
                {2, 1280, 1664, 2560, 2560, 1472, 2400, -200},
                {4, 1280, 1664, 2560, 2944, 1472, 2752, -400}};
        boolean[] sidesSeen = new boolean[6];
        for (int[] s : setups) {
            sset("occluderCounts", new int[4]); sset("occluders", new Class47[4][500]);
            WorldController.addOccluder(0, s[1], -100, s[2], s[4], s[0] == 4 ? -50 : -300, s[3], s[0]);
            camera(s[5], s[7], s[6], 128, 0);
            call(wc, "selectActiveOccluders", M319);
            assertEquals(1, sint("activeOccluderCount"));
            Class47 o = occ(0, 0);
            int side = o.side;
            sidesSeen[side] = true;
            int hits = 0, misses = 0;
            for (int px = o.minX - 700; px <= o.maxX + 700; px += 37) {
                for (int py = o.minY - 700; py <= o.maxY + 700; py += 37) {
                    for (int ph = -1500; ph <= 600; ph += 53) {
                        boolean expected;
                        if (side == 1 || side == 2) {
                            int d = side == 1 ? o.minX - px : px - o.minX;
                            expected = d > 0 && py >= o.minY + (o.minYSlope * d >> 8) && py <= o.maxY + (o.maxYSlope * d >> 8)
                                    && ph >= o.minHeight + (o.minHeightSlope * d >> 8) && ph <= o.maxHeight + (o.maxHeightSlope * d >> 8);
                        } else if (side == 3 || side == 4) {
                            int d = side == 3 ? o.minY - py : py - o.minY;
                            expected = d > 0 && px >= o.minX + (o.minXSlope * d >> 8) && px <= o.maxX + (o.maxXSlope * d >> 8)
                                    && ph >= o.minHeight + (o.minHeightSlope * d >> 8) && ph <= o.maxHeight + (o.maxHeightSlope * d >> 8);
                        } else {
                            int d = ph - o.minHeight;
                            expected = d > 0 && px >= o.minX + (o.minXSlope * d >> 8) && px <= o.maxX + (o.maxXSlope * d >> 8)
                                    && py >= o.minY + (o.minYSlope * d >> 8) && py <= o.maxY + (o.maxYSlope * d >> 8);
                        }
                        boolean actual = (Boolean) call(wc, "isPointOccluded", t, px, ph, py);
                        if (actual) hits++; else misses++;
                        assertEquals("side " + side + " at " + px + "," + ph + "," + py, expected, actual);
                    }
                }
            }
            assertTrue("side " + side + " has hidden and visible points: " + hits + "/" + misses, hits > 20 && misses > 20);
        }
        for (int side = 1; side <= 5; side++) assertTrue("side " + side, sidesSeen[side]);
    }

    @Test public void method323MultiTileFootprintIsHiddenOnlyWhenItsFourCornersAreAllHidden() throws Exception {
        WorldController wc = occludedScene();
        Class<?>[] t = {int.class, int.class, int.class};
        int hits = 0, misses = 0, n = 0;
        for (int minX = 4; minX <= 12; minX++) for (int maxX = minX + 1; maxX <= 13; maxX++)
            for (int minY = 15; minY <= 24; minY += 3) for (int maxY = minY + 1; maxY <= 30; maxY += 2)
                for (int height : new int[] {0, 600, 1500}) {
                    sset("drawCycle", 50 + n++);
                    int h = 0 - height;
                    boolean expected = (Boolean) call(wc, "isPointOccluded", t, (minX << 7) + 1, h, (minY << 7) + 2)
                            && (Boolean) call(wc, "isPointOccluded", t, (maxX << 7) - 1, h, (minY << 7) + 2)
                            && (Boolean) call(wc, "isPointOccluded", t, (minX << 7) + 1, h, (maxY << 7) - 1)
                            && (Boolean) call(wc, "isPointOccluded", t, (maxX << 7) - 1, h, (maxY << 7) - 1);
                    boolean actual = (Boolean) call(wc, "isFootprintOccluded", M323, 0, minX, maxX, minY, maxY, height);
                    if (actual) hits++; else misses++;
                    assertEquals(minX + "-" + maxX + " " + minY + "-" + maxY + " h" + height, expected, actual);
                }
        assertTrue(hits + "/" + misses, hits > 20 && misses > 20);
    }

    @Test public void method320IsTrueOnlyWhenAllFourInsetCornersOfTheTileAreHiddenAndCachesTheAnswerPerDrawCycle() throws Exception {
        WorldController wc = occludedScene();
        int tx = 8, ty = 22; // x 1024..1151, behind the wall; y 2816..2943
        assertEquals(true, call(wc, "isTileOccluded", M320, 0, tx, ty));
        int[][][] cache = (int[][][]) get(wc, "tileOcclusionCache");
        assertEquals(5, cache[0][tx][ty]);
        assertEquals(0, cache[0][tx + 1][ty]);
        // a tile in front of the wall is not hidden, and the negative stamp records that
        assertEquals(false, call(wc, "isTileOccluded", M320, 0, 11, ty));
        assertEquals(-5, cache[0][11][ty]);
        // a tile straddling the wall: two corners hidden, two not
        assertEquals(false, call(wc, "isTileOccluded", M320, 0, 10, ty));
        // the cache wins within the same draw cycle even if the occluders disappear ...
        sset("activeOccluderCount", 0);
        assertEquals(true, call(wc, "isTileOccluded", M320, 0, tx, ty));
        assertEquals(false, call(wc, "isTileOccluded", M320, 0, 11, ty));
        // ... and is recomputed once the cycle number changes
        sset("drawCycle", 6);
        assertEquals(false, call(wc, "isTileOccluded", M320, 0, tx, ty));
        assertEquals(-6, cache[0][tx][ty]);
        // the plane argument selects the cache plane
        sset("drawCycle", 7);
        assertEquals(false, call(wc, "isTileOccluded", M320, 1, tx, ty));
        assertEquals(-7, cache[1][tx][ty]);
        assertEquals(-6, cache[0][tx][ty]);
    }

    @Test public void method320UsesTheHeightsOfTheRequestedPlaneAndCornerCoordinatesInsetByOneUnit() throws Exception {
        WorldController wc = occludedScene();
        int[][][] heights = (int[][][]) get(wc, "tileHeights");
        // plane 2 heights are far above the occluded height range (-800..200): hidden on plane 0, visible on plane 2
        for (int x = 0; x <= SIZE; x++) Arrays.fill(heights[2][x], -5000);
        assertEquals(true, call(wc, "isTileOccluded", M320, 0, 8, 22));
        assertEquals(false, call(wc, "isTileOccluded", M320, 2, 8, 22));
        // one corner of the tile (x+1,y) raised out of range: the whole tile is no longer hidden
        sset("drawCycle", 9);
        heights[0][9][22] = -5000;
        assertEquals(false, call(wc, "isTileOccluded", M320, 0, 8, 22));
    }

    @Test public void method324IsTrueOnlyForPointsBehindAnActiveOccluderInsideItsProjectedBounds() throws Exception {
        WorldController wc = occludedScene();
        Class<?>[] t = {int.class, int.class, int.class};
        assertEquals(true, call(wc, "isPointOccluded", t, 1000, -200, 2752));
        assertEquals("in front of the wall", false, call(wc, "isPointOccluded", t, 1281, -200, 2752));
        assertEquals("exactly at the wall", false, call(wc, "isPointOccluded", t, 1280, -200, 2752));
        assertEquals("one unit behind the wall", true, call(wc, "isPointOccluded", t, 1279, -200, 2752));
        assertEquals("height argument is the second argument", false, call(wc, "isPointOccluded", t, 1000, -5000, 2752));
        assertEquals("y argument is the third argument", false, call(wc, "isPointOccluded", t, 1000, -200, 9000));
        // when no occluder is active nothing is hidden
        sset("activeOccluderCount", 0);
        assertEquals(false, call(wc, "isPointOccluded", t, 1000, -200, 2752));
    }

    @Test public void method322IsTheTileOcclusionTestRaisedByTheObjectHeight() throws Exception {
        WorldController wc = occludedScene();
        // wall hides heights -800..200; tile height 0; an object of height 600 reaches -600 (still hidden), 900 reaches -900 (not)
        assertEquals(true, call(wc, "isTileColumnOccluded", M322, 0, 8, 22, 0));
        assertEquals(true, call(wc, "isTileColumnOccluded", M322, 0, 8, 22, 600));
        assertEquals(false, call(wc, "isTileColumnOccluded", M322, 0, 8, 22, 1300));
        assertEquals("the base tile must itself be hidden", false, call(wc, "isTileColumnOccluded", M322, 0, 11, 22, 0));
        // heights equal to the limits of the occluder: hidden up to the first value beyond -800 (the projected bounds widen with distance)
        boolean prev = true;
        int boundary = -1;
        for (int h = 0; h < 1500; h++) {
            sset("drawCycle", 100 + h);
            boolean v = (Boolean) call(wc, "isTileColumnOccluded", M322, 0, 8, 22, h);
            if (prev && !v) { boundary = h; }
            prev = v;
        }
        assertTrue("hidden for small heights, not for large ones", boundary > 900 && boundary < 1100);
    }

    @Test public void method323TestsASingleTileLikeMethod322AndAMultiTileFootprintByItsFourOuterCorners() throws Exception {
        WorldController wc = occludedScene();
        // same tile range on both axes (minTileX == maxTileX and minTileY == maxTileY): the single tile test
        assertEquals(call(wc, "isTileColumnOccluded", M322, 0, 8, 22, 300), call(wc, "isFootprintOccluded", M323, 0, 8, 8, 22, 22, 300));
        assertEquals(true, call(wc, "isFootprintOccluded", M323, 0, 8, 8, 22, 22, 300));
        assertEquals(false, call(wc, "isFootprintOccluded", M323, 0, 11, 11, 22, 22, 300));
        // multi tile: all tiles of the rectangle must be non-negative in the cache, then the four corners are tested
        sset("drawCycle", 20);
        assertEquals(true, call(wc, "isFootprintOccluded", M323, 0, 6, 9, 21, 23, 0));
        // a tile of the rectangle recorded as not hidden for this cycle (negative stamp) rejects the rectangle immediately
        int[][][] cache = (int[][][]) get(wc, "tileOcclusionCache");
        cache[0][7][22] = -20;
        assertEquals(false, call(wc, "isFootprintOccluded", M323, 0, 6, 9, 21, 23, 0));
        cache[0][7][22] = 20;
        assertEquals(true, call(wc, "isFootprintOccluded", M323, 0, 6, 9, 21, 23, 0));
        // the corners are at (minX*128+1, minY*128+2), (maxX*128-1, ...) and heights are tileHeights[minX][minY] - height
        assertEquals(false, call(wc, "isFootprintOccluded", M323, 0, 6, 9, 21, 23, 2500));
        assertEquals(false, call(wc, "isFootprintOccluded", M323, 0, 6, 11, 21, 23, 0));
    }
}
