import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** Controlled original-name observations of Model.drawFacesByDepthAndPriority (face bucketing and draw order). */
public class ModelFaceDrawOrderTest {
    private static final int W = 48, H = 40, BG = 0x7f7f7f;
    private static final int N = 4;
    private static final String[] MODEL_STATIC = {"faceNeedsHorizontalClip", "faceNearPlaneClipped", "screenX",
            "screenY", "vertexDepth", "vertexCameraX", "vertexCameraY", "vertexCameraZ"};
    private static final String[] TEXTURE_STATIC = {"clipScanlines", "transparency", "lowMem", "blockShading"};
    private final Object[] savedModel = new Object[MODEL_STATIC.length];
    private final Object[] savedTexture = new Object[TEXTURE_STATIC.length];
    private int[] savedPixels, drawing, palette;
    private boolean full;
    private int savedPickX, savedPickY, savedPickCount;
    private Model model;

    @Before public void setup() throws Exception {
        for (int n = 0; n < MODEL_STATIC.length; n++) savedModel[n] = get(Model.class, MODEL_STATIC[n], null);
        for (int n = 0; n < TEXTURE_STATIC.length; n++) savedTexture[n] = get(Texture.class, TEXTURE_STATIC[n], null);
        savedPixels = DrawingArea.pixels;
        drawing = new int[] {DrawingArea.width, DrawingArea.height, DrawingArea.topX, DrawingArea.topY,
                DrawingArea.bottomX, DrawingArea.bottomY, DrawingArea.scanlineRightLimit,
                DrawingArea.clipRightHalf, DrawingArea.clipBottomHalf};
        palette = Texture.hslToRgbPalette.clone();
        savedPickX = Model.pickMouseX; savedPickY = Model.pickMouseY; savedPickCount = Model.pickedCount;
        full = ClientSettings.FULL_512PX_VIEWPORT;
        ClientSettings.FULL_512PX_VIEWPORT = true;
        set(Texture.class, "lowMem", null, true);
        set(Texture.class, "blockShading", null, false);
        for (int n = 0; n < 65536; n++) Texture.hslToRgbPalette[n] = n;
        for (String name : MODEL_STATIC) set(Model.class, name, null,
                savedModel[Arrays.asList(MODEL_STATIC).indexOf(name)] instanceof boolean[] ? new boolean[3 * N] : new int[3 * N]);
        Constructor<Model> ctor = Model.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        model = ctor.newInstance();
        int[] a = new int[N], b = new int[N], c = new int[N], col = new int[N];
        for (int f = 0; f < N; f++) { a[f] = 3 * f; b[f] = 3 * f + 1; c[f] = 3 * f + 2; col[f] = color(f); }
        set(Model.class, "faceVertexA", model, a);
        set(Model.class, "faceVertexB", model, b);
        set(Model.class, "faceVertexC", model, c);
        set(Model.class, "faceShadeA", model, col);
        set(Model.class, "faceCount", model, N);
        set(Model.class, "depthBucketCount", model, 40);
        set(Model.class, "depthBucketOffset", model, 5);
        int[] x = (int[]) get(Model.class, "screenX", null);
        int[] y = (int[]) get(Model.class, "screenY", null);
        for (int f = 0; f < N; f++) {
            x[3 * f] = 36; x[3 * f + 1] = 4; x[3 * f + 2] = 20;
            y[3 * f] = 3; y[3 * f + 1] = 3; y[3 * f + 2] = 27;
        }
        int[] flags = new int[N];
        Arrays.fill(flags, 1);
        set(Model.class, "faceRenderFlags", model, flags);
        depths(0, 0, 0, 0);
    }

    @After public void restore() throws Exception {
        for (int n = 0; n < MODEL_STATIC.length; n++) set(Model.class, MODEL_STATIC[n], null, savedModel[n]);
        for (int n = 0; n < TEXTURE_STATIC.length; n++) set(Texture.class, TEXTURE_STATIC[n], null, savedTexture[n]);
        System.arraycopy(palette, 0, Texture.hslToRgbPalette, 0, palette.length);
        DrawingArea.pixels = savedPixels;
        DrawingArea.width = drawing[0]; DrawingArea.height = drawing[1];
        DrawingArea.topX = drawing[2]; DrawingArea.topY = drawing[3];
        DrawingArea.bottomX = drawing[4]; DrawingArea.bottomY = drawing[5];
        DrawingArea.scanlineRightLimit = drawing[6]; DrawingArea.clipRightHalf = drawing[7];
        DrawingArea.clipBottomHalf = drawing[8];
        Model.pickMouseX = savedPickX; Model.pickMouseY = savedPickY; Model.pickedCount = savedPickCount;
        ClientSettings.FULL_512PX_VIEWPORT = full;
    }

    private static int color(int face) { return 0x1000 * (face + 1); }

    /** Sets the three vertex depths of each listed face to one value. */
    private void depths(int... d) throws Exception {
        int[] z = (int[]) get(Model.class, "vertexDepth", null);
        for (int f = 0; f < d.length; f++) { z[3 * f] = d[f]; z[3 * f + 1] = d[f]; z[3 * f + 2] = d[f]; }
    }

    private void hide(int... faces) throws Exception {
        int[] t = (int[]) get(Model.class, "faceRenderFlags", model);
        for (int f : faces) t[f] = -1;
    }

    private void priorities(int... p) throws Exception {
        int[] pr = new int[N];
        System.arraycopy(p, 0, pr, 0, p.length);
        set(Model.class, "facePriorities", model, pr);
    }

    /** Colour of the face drawn last at a pixel inside every face. */
    private int top(boolean nearFlag, boolean pick, int pickId) throws Exception {
        int[] pixels = new int[W * H];
        Arrays.fill(pixels, BG);
        DrawingArea.bindRaster(H, W, pixels);
        Texture.setScreenSize(W, H);
        Method m = Model.class.getDeclaredMethod("drawFacesByDepthAndPriority", boolean.class, boolean.class, int.class);
        m.setAccessible(true);
        m.invoke(model, nearFlag, pick, pickId);
        return pixels[10 * W + 20];
    }

    private int top() throws Exception { return top(false, false, 0); }

    @Test public void fartherFacesAreDrawnFirstSoNearerFacesCoverThem() throws Exception {
        hide(2, 3);
        depths(10, 5);
        assertEquals(color(1), top());
        depths(5, 10);
        assertEquals(color(0), top());
        depths(10, 10);
        assertEquals(color(1), top());
    }

    @Test public void faceDepthIsTheTruncatedMeanOfItsThreeVertexDepths() throws Exception {
        hide(2, 3);
        depths(5, 5);
        int[] z = (int[]) get(Model.class, "vertexDepth", null);
        z[0] = 0; z[1] = 0; z[2] = 12; // face 0 mean 4: nearer than face 1 (5) although its largest depth is 12
        assertEquals(color(0), top());
        z[0] = 3; z[1] = 3; z[2] = 3; z[3] = 0; z[4] = 0; z[5] = 17; // face 1 mean 5 (truncated), face 0 mean 3
        assertEquals(color(0), top());
        z[3] = 0; z[4] = 0; z[5] = 8; // face 1 mean 2 (truncated): nearer than face 0
        assertEquals(color(1), top());
    }

    @Test public void bucketIndexIsMeanDepthPlusOffsetSoNegativeDepthsWork() throws Exception {
        hide(2, 3);
        depths(-5, -2);
        assertEquals(color(0), top());
        set(Model.class, "depthBucketOffset", model, 2);
        try {
            top();
            fail("mean depth -5 plus offset 2 must index below 0");
        } catch (java.lang.reflect.InvocationTargetException e) {
            assertTrue(e.getCause() instanceof ArrayIndexOutOfBoundsException);
        }
    }

    @Test public void backFacingFacesAreSkippedAndMinusOneFlagHidesAFace() throws Exception {
        hide(2, 3);
        depths(10, 5);
        int[] x = (int[]) get(Model.class, "screenX", null);
        x[3] = 4; x[4] = 36; // reverse the winding of face 1
        assertEquals(color(0), top());
        x[3] = 36; x[4] = 4;
        assertEquals(color(1), top());
        hide(1);
        assertEquals(color(0), top());
    }

    @Test public void nearFlagMarksOnlyFacesWithAMinus5000CornerAndOnlyWhenSet() throws Exception {
        hide(2, 3);
        int[] x = (int[]) get(Model.class, "screenX", null);
        boolean[] near = (boolean[]) get(Model.class, "faceNearPlaneClipped", null);
        x[0] = -5000;
        try { top(true, false, 0); } catch (Exception ignored) { }
        assertTrue(near[0]);
        assertFalse(near[1]);
        near[0] = false;
        try { top(false, false, 0); } catch (Exception ignored) { }
        assertFalse(near[0]);
    }

    @Test public void pickRecordsTheIdOnceWhenTheCursorIsInsideAFace() throws Exception {
        hide(2, 3);
        depths(10, 5);
        Model.pickMouseX = 20; Model.pickMouseY = 10; Model.pickedCount = 0;
        top(false, true, 77);
        assertEquals(1, Model.pickedCount);
        assertEquals(77, Model.pickedIds[0]);
        Model.pickedCount = 0;
        top(false, false, 78);
        assertEquals(0, Model.pickedCount);
        Model.pickMouseX = 45; Model.pickMouseY = 35;
        top(false, true, 79);
        assertEquals(0, Model.pickedCount);
    }

    @Test public void lowerPriorityNumbersDrawFirstRegardlessOfDepth() throws Exception {
        hide(2, 3);
        depths(5, 10);
        priorities(0, 1);
        assertEquals(color(1), top());
        priorities(1, 0);
        assertEquals(color(0), top());
        priorities(3, 3);
        assertEquals(color(0), top());
    }

    @Test public void priorityTenAndElevenFacesAreMergedByDepthAgainstPriorityOneTwoMean() throws Exception {
        for (int p : new int[] {10, 11}) {
            hide(2, 3);
            depths(12, 8);
            priorities(p, 1);
            assertEquals("p" + p + " far", color(1), top());
            depths(4, 8);
            assertEquals("p" + p + " near", color(0), top());
        }
    }

    @Test public void priorityTenFaceIsGatedByPriorityThreeFourMeanAtStageThree() throws Exception {
        // face 0 priority 10 depth 6; face 1 priority 1 depth 8; face 2 priority 3 depth 3
        depths(6, 8, 3);
        priorities(10, 1, 3);
        hide(2, 3);
        assertEquals(color(0), top());      // 6 not > mean(1,2)=8, so drawn after face 1
        set(Model.class, "faceRenderFlags", model, new int[] {1, 1, 1, 1});
        hide(3);
        assertEquals(color(2), top());      // 6 not > mean(1,2)=8 but > mean(3,4)=3: drawn before face 2, which ends on top
        depths(2, 8, 3);
        set(Model.class, "faceRenderFlags", model, new int[] {1, 1, 1, 1});
        hide(3);
        assertEquals(color(0), top());      // 2 not > 8, not > mean(3,4)=3, > 6/8 mean 0: drawn after face 2
    }

    @Test public void priorityElevenFacesFollowPriorityTenFacesInDepthOrder() throws Exception {
        hide(3);
        depths(9, 7, 3);
        priorities(10, 11, 1);
        hide(2);
        assertEquals(color(1), top());
        depths(7, 9);
        assertEquals(color(1), top());
    }

    @Test public void workTablesHoldBucketAndPriorityLists() throws Exception {
        depths(8, 9, 10, 6);
        priorities(0, 5, 10, 11);
        top();
        int[] bucketCounts = (int[]) get(Model.class, "depthBucketFaceCounts", null);
        int[][] bucketFaces = (int[][]) get(Model.class, "depthBucketFaces", null);
        assertEquals(1, bucketCounts[13]); assertEquals(0, bucketFaces[13][0]);
        assertEquals(1, bucketCounts[14]); assertEquals(1, bucketFaces[14][0]);
        assertEquals(1, bucketCounts[15]); assertEquals(2, bucketFaces[15][0]);
        assertEquals(1, bucketCounts[11]); assertEquals(3, bucketFaces[11][0]);
        assertEquals(0, bucketCounts[12]);
        int[] pc = (int[]) get(Model.class, "priorityFaceCounts", null);
        int[][] pf = (int[][]) get(Model.class, "priorityFaces", null);
        assertEquals(1, pc[0]); assertEquals(1, pc[5]); assertEquals(1, pc[10]); assertEquals(1, pc[11]);
        assertEquals(0, pc[1]);
        assertEquals(0, pf[0][0]); assertEquals(1, pf[5][0]); assertEquals(2, pf[10][0]); assertEquals(3, pf[11][0]);
        assertEquals(15, ((int[]) get(Model.class, "priority10Depths", null))[0]);
        assertEquals(11, ((int[]) get(Model.class, "priority11Depths", null))[0]);
        int[] sums = (int[]) get(Model.class, "priorityDepthSums", null);
        assertEquals(13, sums[0]); assertEquals(14, sums[5]); assertEquals(0, sums[10]);
        priorities(5, 5, 5, 5);
        top();
        assertEquals(4, pc[5]);
        assertArrayEquals(new int[] {2, 1, 0, 3}, Arrays.copyOf(pf[5], 4)); // farthest bucket first
        assertEquals(15 + 14 + 13 + 11, sums[5]);
    }

    @Test public void horizontalClipFlagMarksFacesWithACornerOutsideTheRasterWidth() throws Exception {
        hide(2, 3);
        int[] x = (int[]) get(Model.class, "screenX", null);
        boolean[] clip = (boolean[]) get(Model.class, "faceNeedsHorizontalClip", null);
        top();
        assertFalse(clip[0]); assertFalse(clip[1]);
        int limit = DrawingArea.scanlineRightLimit;
        x[1] = -1;
        top();
        assertTrue(clip[0]); assertFalse(clip[1]);
        x[1] = 4; x[0] = limit; x[3] = limit + 1;
        top();
        assertFalse(clip[0]); assertTrue(clip[1]);
        x[3] = limit;
        top();
        assertFalse(clip[1]);
    }

    private static Object get(Class<?> cls, String name, Object target) throws Exception {
        Field f = cls.getDeclaredField(name); f.setAccessible(true); return f.get(target);
    }

    private static void set(Class<?> cls, String name, Object target, Object value) throws Exception {
        Field f = cls.getDeclaredField(name); f.setAccessible(true);
        if (value instanceof Integer) f.setInt(target, (Integer) value); else f.set(target, value);
    }
}
