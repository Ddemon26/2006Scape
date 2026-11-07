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
