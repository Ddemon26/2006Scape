import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** Controlled original-name observations of Model.method484. */
public class ModelFaceRasterTest {
    private static final int W = 48, H = 40, BG = 0x7f7f7f;
    private static final String[] MODEL_STATIC = {"aBooleanArray1663", "aBooleanArray1664", "anIntArray1665",
            "anIntArray1666", "anIntArray1668", "anIntArray1669", "anIntArray1670"};
    private static final String[] TEXTURE_STATIC = {"clipScanlines", "transparency", "lineOffsets", "lowMem",
            "blockShading", "textureInt1", "textureInt2"};
    private final Object[] savedModel = new Object[MODEL_STATIC.length];
    private final Object[] savedTexture = new Object[TEXTURE_STATIC.length];
    private int[] savedPixels, drawing, palette;
    private int[] savedTextureBuffer;
    private boolean savedTransparent, full;
    private Model model;

    @Before public void setup() throws Exception {
        for (int n = 0; n < MODEL_STATIC.length; n++) savedModel[n] = get(Model.class, MODEL_STATIC[n], null);
        for (int n = 0; n < TEXTURE_STATIC.length; n++) savedTexture[n] = get(Texture.class, TEXTURE_STATIC[n], null);
        savedPixels = DrawingArea.pixels;
        drawing = new int[] {DrawingArea.width, DrawingArea.height, DrawingArea.topX, DrawingArea.topY,
                DrawingArea.bottomX, DrawingArea.bottomY, DrawingArea.scanlineRightLimit,
                DrawingArea.clipRightHalf, DrawingArea.clipBottomHalf};
        palette = Texture.hslToRgbPalette.clone();
        savedTextureBuffer = ((int[][]) get(Texture.class, "textureBuffers", null))[4];
        savedTransparent = ((boolean[]) get(Texture.class, "hasTransparentPixels", null))[4];
        full = ClientSettings.FULL_512PX_VIEWPORT;
        ClientSettings.FULL_512PX_VIEWPORT = true;
        set(Texture.class, "lowMem", null, true);
        set(Texture.class, "blockShading", null, false);
        for (int n = 0; n < 65536; n++) Texture.hslToRgbPalette[n] = n;
        for (String name : MODEL_STATIC) set(Model.class, name, null,
                name.startsWith("aBoolean") ? new boolean[6] : new int[6]);
        set(Model.class, "anIntArray1665", null, new int[] {4, 36, 20, 4, 36, 20});
        set(Model.class, "anIntArray1666", null, new int[] {3, 3, 27, 3, 3, 27});
        set(Model.class, "anIntArray1668", null, new int[] {-24, 40, -24, -24, 40, -24});
        set(Model.class, "anIntArray1669", null, new int[] {-20, -20, 44, -20, -20, 44});
        set(Model.class, "anIntArray1670", null, new int[] {512, 512, 512, 512, 512, 512});
        Constructor<Model> ctor = Model.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        model = ctor.newInstance();
        set(Model.class, "anIntArray1631", model, new int[] {0, 3});
        set(Model.class, "anIntArray1632", model, new int[] {1, 4});
        set(Model.class, "anIntArray1633", model, new int[] {2, 5});
        set(Model.class, "anIntArray1634", model, new int[] {0x1234, 0x3456});
        set(Model.class, "anIntArray1635", model, new int[] {0x2345, 0x4567});
        set(Model.class, "anIntArray1636", model, new int[] {0x3456, 0x5678});
        set(Model.class, "anIntArray1643", model, new int[] {0, 2});
        set(Model.class, "anIntArray1644", model, new int[] {1, 1});
        set(Model.class, "anIntArray1645", model, new int[] {2, 0});
        set(Model.class, "anIntArray1640", model, new int[] {4, 4});
        int[] texture = new int[16384];
        for (int level = 0; level < 4; level++) for (int row = 0; row < 64; row++) for (int col = 0; col < 64; col++)
            texture[level * 4096 + row * 64 + col] = ((level + 1) << 20) | (row << 10) | col;
        ((int[][]) get(Texture.class, "textureBuffers", null))[4] = texture;
        ((boolean[]) get(Texture.class, "hasTransparentPixels", null))[4] = false;
    }

    @After public void restore() throws Exception {
        for (int n = 0; n < MODEL_STATIC.length; n++) set(Model.class, MODEL_STATIC[n], null, savedModel[n]);
        for (int n = 0; n < TEXTURE_STATIC.length; n++) set(Texture.class, TEXTURE_STATIC[n], null, savedTexture[n]);
        System.arraycopy(palette, 0, Texture.hslToRgbPalette, 0, palette.length);
        ((int[][]) get(Texture.class, "textureBuffers", null))[4] = savedTextureBuffer;
        ((boolean[]) get(Texture.class, "hasTransparentPixels", null))[4] = savedTransparent;
        DrawingArea.pixels = savedPixels;
        DrawingArea.width = drawing[0]; DrawingArea.height = drawing[1];
        DrawingArea.topX = drawing[2]; DrawingArea.topY = drawing[3];
        DrawingArea.bottomX = drawing[4]; DrawingArea.bottomY = drawing[5];
        DrawingArea.scanlineRightLimit = drawing[6]; DrawingArea.clipRightHalf = drawing[7];
        DrawingArea.clipBottomHalf = drawing[8];
        ClientSettings.FULL_512PX_VIEWPORT = full;
    }

    @Test public void faceIndexAndCornerIndicesSelectTheVisibleTriangle() throws Exception {
        int[] first = render(0);
        assertNotEquals(BG, first[10 * W + 20]);
        assertEquals(BG, first[30 * W + 20]);
        int[] x = (int[]) get(Model.class, "anIntArray1665", null);
        x[3] = 12; x[4] = 44; x[5] = 28;
        int[] second = render(1);
        assertEquals(BG, second[10 * W + 8]);
        assertNotEquals(BG, second[10 * W + 28]);
        assertFalse(Arrays.equals(first, second));
        x[3] = 4; x[4] = 36; x[5] = 20;
        assertArrayEquals(first, render(0));
    }

    @Test public void eachFaceCornerChangesTheTriangleOutline() throws Exception {
        int[] original = render(0);
        int[] x = (int[]) get(Model.class, "anIntArray1665", null);
        for (int corner = 0; corner < 3; corner++) {
            x[corner] += 8;
            assertFalse("corner " + corner, Arrays.equals(original, render(0)));
            x[corner] -= 8;
            assertArrayEquals(original, render(0));
        }
    }

    @Test public void lowBitsSelectShadedFlatAndTwoTexturedVariants() throws Exception {
        int[] implicit = render(0);
        set(Model.class, "anIntArray1637", model, new int[] {0, 0});
        assertArrayEquals(implicit, render(0));
        int[] flat = renderWithType(1);
        assertEquals(0x1234, flat[10 * W + 20]);
        assertFalse(Arrays.equals(implicit, flat));
        int[] graded = renderWithType(2);
        int[] constant = renderWithType(3);
        assertNotEquals(BG, graded[10 * W + 20]);
        assertNotEquals(BG, constant[10 * W + 20]);
        assertFalse(Arrays.equals(graded, constant));
        assertFalse(Arrays.equals(flat, graded));
        int[] swapped = renderWithType(6); // same low bits, different mapping triangle
        assertFalse(Arrays.equals(graded, swapped));
    }

    @Test public void clippingAndAlphaFlagsReachTheRasteriser() throws Exception {
        int[] base = renderWithType(1);
        ((boolean[]) get(Model.class, "aBooleanArray1663", null))[0] = true;
        renderWithType(1);
        assertTrue((boolean) get(Texture.class, "clipScanlines", null));
        ((boolean[]) get(Model.class, "aBooleanArray1663", null))[0] = false;
        set(Model.class, "anIntArray1639", model, new int[] {128, 0});
        int[] translucent = renderWithType(1);
        assertEquals(128, get(Texture.class, "transparency", null));
        assertFalse(Arrays.equals(base, translucent));
        set(Model.class, "anIntArray1639", model, null);
        assertArrayEquals(base, renderWithType(1));
        assertEquals(0, get(Texture.class, "transparency", null));
    }

    @Test public void eachMappingCornerControlsTheTexturedImage() throws Exception {
        int[] first = renderWithType(2);
        int[] mappingA = (int[]) get(Model.class, "anIntArray1643", model);
        int[] mappingB = (int[]) get(Model.class, "anIntArray1644", model);
        int[] mappingC = (int[]) get(Model.class, "anIntArray1645", model);
        mappingA[0] = 1; mappingB[0] = 0;
        assertFalse(Arrays.equals(first, renderWithType(2)));
        mappingA[0] = 0; mappingB[0] = 1;
        mappingB[0] = 2; mappingC[0] = 1;
        assertFalse(Arrays.equals(first, renderWithType(2)));
        mappingB[0] = 1; mappingC[0] = 2;
        assertArrayEquals(first, renderWithType(2));
        int[] flatShade = renderWithType(3);
        mappingA[0] = 2; mappingC[0] = 0;
        assertFalse(Arrays.equals(flatShade, renderWithType(3)));
    }

    // ---- Model.method485 (near-plane clipping), original names only ----
    private static final int[] SHADES = {100, 200, 300};
    private static final int[] CAM_X = {-2, -2, 3, 0, 0, 0};
    private static final int[] CAM_Y = {-1, 3, -1, 0, 0, 0};
    private static final int SENTINEL = -77777;
    /** Camera coordinates are truncated to whole units before the 512/50 scale, so a cut can be 10.24 px off the exact line. */
    private static final double CUT_TOLERANCE = 11.0;

    private void clipSetup(int zA, int zB, int zC) throws Exception {
        Texture.setScreenSize(W, H);
        int[] z = {zA, zB, zC, 512, 512, 512};
        set(Model.class, "anIntArray1668", null, CAM_X.clone());
        set(Model.class, "anIntArray1669", null, CAM_Y.clone());
        set(Model.class, "anIntArray1670", null, z);
        int[] px = new int[6], py = new int[6];
        for (int v = 0; v < 3; v++) {
            if (z[v] >= 50) {
                px[v] = Texture.textureInt1 + CAM_X[v] * 512 / z[v];
                py[v] = Texture.textureInt2 + CAM_Y[v] * 512 / z[v];
            }
        }
        set(Model.class, "anIntArray1665", null, px);
        set(Model.class, "anIntArray1666", null, py);
        set(Model.class, "anIntArray1634", model, new int[] {SHADES[0], 7});
        set(Model.class, "anIntArray1635", model, new int[] {SHADES[1], 7});
        set(Model.class, "anIntArray1636", model, new int[] {SHADES[2], 7});
        set(Model.class, "anIntArray1637", model, null);
        for (String name : new String[] {"clippedX", "clippedY", "clippedShade"})
            Arrays.fill((int[]) get(Model.class, name, null), SENTINEL);
    }

    private int[] clipRender(int face) throws Exception {
        int[] pixels = new int[W * H];
        Arrays.fill(pixels, BG);
        DrawingArea.bindRaster(H, W, pixels);
        Method m = Model.class.getDeclaredMethod("drawFaceClippedToNearPlane", int.class);
        m.setAccessible(true);
        m.invoke(model, face);
        return pixels;
    }

    private int[] blank() {
        int[] blank = new int[W * H];
        Arrays.fill(blank, BG);
        return blank;
    }

    private int[] depths() throws Exception {
        return (int[]) get(Model.class, "anIntArray1670", null);
    }

    private double[] front(int v) throws Exception {
        return new double[] {Texture.textureInt1 + CAM_X[v] * 512 / depths()[v],
                Texture.textureInt2 + CAM_Y[v] * 512 / depths()[v], SHADES[v]};
    }

    /** Where edge p-to-q crosses the z=50 plane, projected with the 512/50 scale. */
    private double[] cut(int p, int q) throws Exception {
        int[] z = depths();
        double t = (50.0 - z[p]) / (z[q] - z[p]);
        double x = CAM_X[p] + (CAM_X[q] - CAM_X[p]) * t;
        double y = CAM_Y[p] + (CAM_Y[q] - CAM_Y[p]) * t;
        return new double[] {Texture.textureInt1 + x * 512 / 50.0, Texture.textureInt2 + y * 512 / 50.0,
                SHADES[p] + (SHADES[q] - SHADES[p]) * t};
    }

    private double[][] expected(int behind) throws Exception {
        switch (behind) {
            case 0: return new double[][] {front(0), front(1), front(2)};
            case 1: return new double[][] {cut(0, 2), cut(0, 1), front(1), front(2)};
            case 2: return new double[][] {front(0), cut(1, 0), cut(1, 2), front(2)};
            case 4: return new double[][] {front(0), front(1), cut(2, 1), cut(2, 0)};
            case 3: return new double[][] {cut(0, 2), cut(1, 2), front(2)};
            case 6: return new double[][] {front(0), cut(1, 0), cut(2, 0)};
            case 5: return new double[][] {cut(0, 1), front(1), cut(2, 1)};
            default: return new double[0][];
        }
    }

    private void assertPolygon(String label, int behind) throws Exception {
        double[][] want = expected(behind);
        int[] xs = (int[]) get(Model.class, "clippedX", null);
        int[] ys = (int[]) get(Model.class, "clippedY", null);
        int[] shade = (int[]) get(Model.class, "clippedShade", null);
        for (int n = 0; n < want.length; n++) {
            assertEquals(label + " x" + n, want[n][0], xs[n], CUT_TOLERANCE);
            assertEquals(label + " y" + n, want[n][1], ys[n], CUT_TOLERANCE);
            assertEquals(label + " shade" + n, want[n][2], shade[n], 3.0);
        }
        assertEquals(label + " no extra x", SENTINEL, xs[want.length]);
        assertEquals(label + " no extra y", SENTINEL, ys[want.length]);
        assertEquals(label + " no extra shade", SENTINEL, shade[want.length]);
    }

    @Test public void nearPlaneCutsFollowTheTriangleEdgesForEveryVertexSubset() throws Exception {
        int[][][] depthSets = {{{114, 150, 90}, {-14, 10, 30}}, {{114, 114, 114}, {-14, -14, -14}}};
        for (int[][] set : depthSets) {
            for (int behind = 0; behind <= 6; behind++) {
                int[] z = new int[3];
                for (int v = 0; v < 3; v++) z[v] = set[((behind >> v) & 1)][v];
                clipSetup(z[0], z[1], z[2]);
                clipRender(0);
                assertPolygon("subset " + behind + " z" + Arrays.toString(z), behind);
            }
        }
    }

    @Test public void faceWithAllVerticesBehindDrawsNothing() throws Exception {
        clipSetup(-14, 10, 30);
        set(Model.class, "anIntArray1637", model, new int[] {1, 0});
        assertArrayEquals(blank(), clipRender(0));
        assertEquals(SENTINEL, ((int[]) get(Model.class, "clippedX", null))[0]);
    }

    @Test public void screenCentreShiftsOnlyTheCutPoints() throws Exception {
        clipSetup(-14, 114, 114);
        clipRender(0);
        int[] xs = ((int[]) get(Model.class, "clippedX", null)).clone();
        int[] ys = ((int[]) get(Model.class, "clippedY", null)).clone();
        clipSetup(-14, 114, 114);
        Texture.textureInt1 += 5;
        Texture.textureInt2 -= 3;
        clipRender(0);
        int[] xs2 = (int[]) get(Model.class, "clippedX", null);
        int[] ys2 = (int[]) get(Model.class, "clippedY", null);
        assertEquals(xs[0] + 5, xs2[0]);
        assertEquals(ys[0] - 3, ys2[0]);
        assertEquals(xs[1] + 5, xs2[1]);
        assertEquals(ys[1] - 3, ys2[1]);
        assertEquals(xs[2], xs2[2]); // front vertices keep their projected coordinates
        assertEquals(ys[3], ys2[3]);
    }

    @Test public void reciprocalTableScalesTheInterpolationFraction() throws Exception {
        clipSetup(-14, 114, 114);
        clipRender(0);
        assertEquals(200, ((int[]) get(Model.class, "clippedShade", null))[0], 1); // edge A->C halfway
        int[] table = (int[]) get(Model.class, "reciprocalTable16", null);
        assertEquals(512, table[128]);
        try {
            int[] doubled = table.clone();
            doubled[128] = 1024;
            set(Model.class, "reciprocalTable16", null, doubled);
            clipSetup(-14, 114, 114);
            clipRender(0);
            assertEquals(300, ((int[]) get(Model.class, "clippedShade", null))[0]);
        } finally {
            set(Model.class, "reciprocalTable16", null, table);
        }
    }

    @Test public void flatColourComesFromThePaletteEntryOfTheFirstShade() throws Exception {
        clipSetup(-14, 114, 114);
        set(Model.class, "anIntArray1637", model, new int[] {1, 0});
        int[] savedPalette = Texture.hslToRgbPalette.clone();
        try {
            Texture.hslToRgbPalette[100] = 0xABCDEF;
            Texture.hslToRgbPalette[200] = 0x123456;
            int[] px = clipRender(0);
            int[] xs = (int[]) get(Model.class, "clippedX", null);
            int[] ys = (int[]) get(Model.class, "clippedY", null);
            assertEquals(0xABCDEF, px[((ys[0] + ys[1] + ys[2]) / 3) * W + (xs[0] + xs[1] + xs[2]) / 3]);
            assertEquals(0xABCDEF, px[((ys[0] + ys[2] + ys[3]) / 3) * W + (xs[0] + xs[2] + xs[3]) / 3]);
        } finally {
            System.arraycopy(savedPalette, 0, Texture.hslToRgbPalette, 0, savedPalette.length);
        }
    }

    @Test public void backFacingClippedPolygonIsCulled() throws Exception {
        clipSetup(-14, 114, 114);
        set(Model.class, "anIntArray1637", model, new int[] {1, 0});
        set(Model.class, "anIntArray1632", model, new int[] {2, 4});
        set(Model.class, "anIntArray1633", model, new int[] {1, 5});
        assertArrayEquals(blank(), clipRender(0));
    }

    @Test public void shadedClippedQuadDrawsBothTriangles() throws Exception {
        clipSetup(-14, 114, 114);
        set(Model.class, "anIntArray1637", model, new int[] {0, 0});
        int[] px = clipRender(0);
        int[] xs = (int[]) get(Model.class, "clippedX", null);
        int[] ys = (int[]) get(Model.class, "clippedY", null);
        assertNotEquals(BG, px[((ys[0] + ys[1] + ys[2]) / 3) * W + (xs[0] + xs[1] + xs[2]) / 3]);
        assertNotEquals(BG, px[((ys[0] + ys[2] + ys[3]) / 3) * W + (xs[0] + xs[2] + xs[3]) / 3]);
    }

    @Test public void texturedClippedFacesUseMappingCornersAndTextureFaceIndex() throws Exception {
        for (int behind : new int[] {0, 1}) {
            for (int base : new int[] {2, 3}) {
                String label = behind + "/" + base;
                clipSetup(behind == 1 ? -14 : 114, 114, 114);
                set(Model.class, "anIntArray1637", model, new int[] {base, 0});
                int[] first = clipRender(0);
                assertFalse(label + " drawn", Arrays.equals(blank(), first));
                set(Model.class, "anIntArray1637", model, new int[] {base + 4, 0});
                assertFalse("face index " + label, Arrays.equals(first, clipRender(0)));
                set(Model.class, "anIntArray1637", model, new int[] {base, 0});
                int[] a = (int[]) get(Model.class, "anIntArray1643", model);
                int[] b = (int[]) get(Model.class, "anIntArray1644", model);
                int[] c = (int[]) get(Model.class, "anIntArray1645", model);
                a[0] = 1; b[0] = 0;
                assertFalse("corner A/B " + label, Arrays.equals(first, clipRender(0)));
                a[0] = 0; b[0] = 2; c[0] = 1;
                assertFalse("corner B/C " + label, Arrays.equals(first, clipRender(0)));
                b[0] = 1; c[0] = 2;
                assertArrayEquals(first, clipRender(0));
            }
        }
        clipSetup(-14, 114, 114);
        set(Model.class, "anIntArray1637", model, new int[] {2, 0});
        int[] graded = clipRender(0);
        set(Model.class, "anIntArray1637", model, new int[] {3, 0});
        assertFalse(Arrays.equals(graded, clipRender(0)));
    }

    private int[] renderWithType(int type) throws Exception {
        set(Model.class, "anIntArray1637", model, new int[] {type, 0});
        return render(0);
    }

    private int[] render(int face) throws Exception {
        int[] pixels = new int[W * H];
        Arrays.fill(pixels, BG);
        DrawingArea.bindRaster(H, W, pixels);
        Texture.setScreenSize(W, H);
        Method m = Model.class.getDeclaredMethod("drawFace", int.class);
        m.setAccessible(true);
        m.invoke(model, face);
        return pixels;
    }

    private static Object get(Class<?> cls, String name, Object target) throws Exception {
        Field f = cls.getDeclaredField(name); f.setAccessible(true); return f.get(target);
    }
    private static void set(Class<?> cls, String name, Object target, Object value) throws Exception {
        Field f = cls.getDeclaredField(name); f.setAccessible(true); f.set(target, value);
    }
}
