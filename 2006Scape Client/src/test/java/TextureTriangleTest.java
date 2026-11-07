import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** Pass 125 semantic tests for the Texture triangle and scanline rasterisers (original names only, via reflection strings). */
public class TextureTriangleTest {
    private static final int W = 48, H = 40, BG = 0x7f7f7f;
    private static final String[] STATE = {"clipScanlines", "opaqueTexture", "blockShading", "transparency", "lineOffsets", "textureInt1",
            "textureInt2", "lowMem", "useCounter"};
    private static final int[][] TRIANGLES = {
            {16, 2, 4, 18, 36, 10}, // x1,y1,x2,y2,x3,y3: all three y distinct, exact binary slopes
            {4, 2, 36, 2, 20, 18}, // flat top
            {20, 2, 4, 18, 36, 18}, // flat bottom
            {24, 2, 40, 18, 8, 10}};
    private static final int[][] PERMS = {{0, 1, 2}, {0, 2, 1}, {1, 0, 2}, {1, 2, 0}, {2, 0, 1}, {2, 1, 0}};

    private Object[] saved = new Object[STATE.length];
    private int[] palette, stamps, savedPixels;
    private int[][] savedBuffers;
    private boolean[] savedTransparent;
    private int[] drawing;
    private boolean fix, full;

    @Before public void save() throws Exception {
        for (int i = 0; i < STATE.length; i++) saved[i] = get(STATE[i]);
        palette = Texture.hslToRgbPalette.clone();
        stamps = Texture.lastUsedStamps.clone();
        savedBuffers = ((int[][]) get("textureBuffers")).clone();
        savedTransparent = ((boolean[]) get("hasTransparentPixels")).clone();
        savedPixels = DrawingArea.pixels;
        drawing = new int[] {DrawingArea.width, DrawingArea.height, DrawingArea.topX, DrawingArea.topY, DrawingArea.bottomX,
                DrawingArea.bottomY, DrawingArea.scanlineRightLimit, DrawingArea.clipRightHalf, DrawingArea.clipBottomHalf};
        fix = ClientSettings.FIX_TRANSPARENCY_OVERFLOW;
        full = ClientSettings.FULL_512PX_VIEWPORT;
        ClientSettings.FIX_TRANSPARENCY_OVERFLOW = true;
        ClientSettings.FULL_512PX_VIEWPORT = true;
        set("clipScanlines", false);
        set("opaqueTexture", true);
        set("blockShading", true);
        set("transparency", 0);
        set("lowMem", true);
        for (int i = 0; i < 65536; i++) Texture.hslToRgbPalette[i] = i;
    }

    @After public void restore() throws Exception {
        for (int i = 0; i < STATE.length; i++) set(STATE[i], saved[i]);
        System.arraycopy(palette, 0, Texture.hslToRgbPalette, 0, 65536);
        System.arraycopy(stamps, 0, Texture.lastUsedStamps, 0, 50);
        System.arraycopy(savedBuffers, 0, (int[][]) get("textureBuffers"), 0, 50);
        System.arraycopy(savedTransparent, 0, (boolean[]) get("hasTransparentPixels"), 0, 50);
        DrawingArea.pixels = savedPixels;
        DrawingArea.width = drawing[0];
        DrawingArea.height = drawing[1];
        DrawingArea.topX = drawing[2];
        DrawingArea.topY = drawing[3];
        DrawingArea.bottomX = drawing[4];
        DrawingArea.bottomY = drawing[5];
        DrawingArea.scanlineRightLimit = drawing[6];
        DrawingArea.clipRightHalf = drawing[7];
        DrawingArea.clipBottomHalf = drawing[8];
        ClientSettings.FIX_TRANSPARENCY_OVERFLOW = fix;
        ClientSettings.FULL_512PX_VIEWPORT = full;
    }

    // ---------------------------------------------------------------- static tables

    @Test public void reciprocalTablesHoldFixedPointInverses() throws Exception {
        int[] t15 = (int[]) get("reciprocalTable15");
        int[] t16 = Texture.reciprocalTable16;
        assertEquals(512, t15.length);
        assertEquals(2048, t16.length);
        assertEquals(0, t15[0]);
        assertEquals(0, t16[0]);
        assertEquals(32768, t15[1]);
        assertEquals(16384, t15[2]);
        assertEquals(10922, t15[3]);
        assertEquals(64, t15[511]);
        assertEquals(65536, t16[1]);
        assertEquals(21845, t16[3]);
        assertEquals(32, t16[2047]);
        for (int i = 1; i < 512; i++) assertTrue("15-bit " + i, t15[i] * i <= 32768 && t15[i] * i > 32768 - i);
        for (int i = 1; i < 2048; i++) assertTrue("16-bit " + i, t16[i] * i <= 65536 && t16[i] * i > 65536 - i);
    }

    @Test public void sineAndCosineTablesAreFullTurnIn2048StepsAt16Dot16() {
        int[] sin = Texture.sineTable, cos = Texture.cosineTable;
        assertEquals(2048, sin.length);
        assertEquals(2048, cos.length);
        assertEquals(0, sin[0]);
        assertEquals(65536, cos[0]);
        assertEquals(65536, sin[512], 1);
        assertEquals(0, cos[512], 1);
        assertEquals(-65536, cos[1024], 1);
        assertEquals(-65536, sin[1536], 1);
        assertEquals(46340, sin[256], 1);
        assertEquals(-46340, cos[768], 1);
        assertTrue(sin[100] > 0 && sin[600] > 0 && sin[1100] < 0 && cos[100] > 0 && cos[600] < 0 && cos[1700] > 0);
        for (int k = 0; k < 2048; k++) {
            double norm = Math.hypot(sin[k], cos[k]);
            assertTrue("unit circle " + k, Math.abs(norm - 65536) < 1.5);
            assertTrue("quarter turn " + k, Math.abs(sin[(k + 512) & 2047] - cos[k]) <= 1);
        }
    }

    // ---------------------------------------------------------------- flat scanline (method377)

    @Test public void flatScanlineFillsLeftInclusiveRightExclusive() throws Exception {
        for (int length = 0; length <= 10; length++) {
            int[] px = raster(W, H);
            call("drawFlatScanline", px, W * 2, 0xabcdef, 3, 3 + length);
            for (int i = 0; i < px.length; i++) assertEquals("len " + length + " idx " + i, i / W == 2 && i % W >= 3 && i % W < 3 + length ? 0xabcdef : BG, px[i]);
        }
        int[] px = raster(W, H);
        call("drawFlatScanline", px, W * 2, 0xabcdef, 9, 4);
        assertArrayEquals(raster(W, H), px);
    }

    @Test public void flatScanlineBlendsByTransparencyWeightOfDestination() throws Exception {
        int src = 0x80c0e0, dst = 0x204060;
        for (int alpha : new int[] {0, 64, 128, 255}) {
            set("transparency", alpha);
            int[] px = raster(W, H);
            Arrays.fill(px, dst);
            call("drawFlatScanline", px, W, src, 2, 11);
            for (int x = 2; x < 11; x++) for (int shift = 0; shift <= 16; shift += 8) {
                double s = (src >> shift) & 255, d = (dst >> shift) & 255;
                double expected = s * (256 - alpha) / 256 + d * alpha / 256;
                assertEquals("alpha " + alpha + " x " + x, expected, (px[W + x] >> shift) & 255, 2.01);
            }
            assertEquals(dst, px[W + 1]);
            assertEquals(dst, px[W + 11]);
        }
    }

    @Test public void flatScanlineClipFlagClampsToTheScanlineLimitsAndNoClipSpillsOver() throws Exception {
        for (boolean clip : new boolean[] {true, false}) {
            int[] px = raster(W, H);
            DrawingArea.setClip(H, 0, 40, 0);
            set("clipScanlines", clip);
            call("drawFlatScanline", px, W * 3, 0x123456, -2, 45);
            for (int i = 0; i < px.length; i++) {
                boolean row3 = i / W == 3, previousRowTail = i >= W * 3 - 2 && i < W * 3;
                boolean expected = clip ? row3 && i % W < 40 : previousRowTail || (i >= W * 3 && i < W * 3 + 45);
                assertEquals("clip " + clip + " idx " + i, expected ? 0x123456 : BG, px[i]);
            }
        }
    }

    // ---------------------------------------------------------------- Gouraud scanline (method375)

    @Test public void shadedScanlineBlockModeStepsColourEveryFourPixelsByReciprocal() throws Exception {
        set("blockShading", true);
        int[] px = raster(W, H);
        call("drawShadedScanline", px, W, 2, 10, 1000 << 8, 1096 << 8);
        for (int x = 0; x < W; x++) {
            int expected = x < 2 || x >= 10 ? BG : x < 6 ? 1000 : 1048;
            assertEquals("x " + x, expected, px[W + x]);
        }
        px = raster(W, H);
        call("drawShadedScanline", px, W, 2, 12, 1000 << 8, 1096 << 8); // 10 pixels: two blocks, then the 2-pixel remainder
        int[] expected = {1000, 1000, 1000, 1000, 1048, 1048, 1048, 1048, 1096, 1096};
        for (int n = 0; n < 10; n++) assertEquals("x " + (2 + n), expected[n], px[W + 2 + n]);
        assertEquals(BG, px[W + 12]);
    }

    @Test public void shadedScanlineExactModeStepsColourEveryPixel() throws Exception {
        set("blockShading", false);
        int[] px = raster(W, H);
        call("drawShadedScanline", px, W, 2, 10, 1000 << 8, 1800 << 8);
        for (int n = 0; n < 8; n++) assertEquals("pixel " + n, 1000 + 100 * n, px[W + 2 + n]);
        assertEquals(BG, px[W + 10]);
        assertEquals(BG, px[W + 1]);
        px = raster(W, H);
        call("drawShadedScanline", px, W, 5, 5, 1000 << 8, 1800 << 8);
        assertArrayEquals(raster(W, H), px);
    }

    @Test public void shadedScanlineBlendsPaletteColourByTransparency() throws Exception {
        for (boolean block : new boolean[] {true, false}) {
            set("blockShading", block);
            for (int i = 0; i < 65536; i++) Texture.hslToRgbPalette[i] = 0x80c0e0;
            set("transparency", 192);
            int[] px = raster(W, H);
            Arrays.fill(px, 0x204060);
            call("drawShadedScanline", px, W, 3, 14, 7 << 8, 7 << 8);
            for (int x = 3; x < 14; x++) for (int shift = 0; shift <= 16; shift += 8) {
                double expected = ((0x80c0e0 >> shift) & 255) * 64 / 256.0 + ((0x204060 >> shift) & 255) * 192 / 256.0;
                assertEquals("block " + block + " x " + x, expected, (px[W + x] >> shift) & 255, 1.01);
            }
        }
    }

    @Test public void shadedScanlineClipFlagStartsColourAtTheUnclippedLeftEdge() throws Exception {
        set("blockShading", false);
        set("clipScanlines", true);
        DrawingArea.setClip(H, 0, 30, 0);
        int[] px = raster(W, H);
        call("drawShadedScanline", px, W * 2, -8, 24, 1000 << 8, 1032 << 8); // one colour index per pixel across the unclipped span
        for (int x = 0; x < 30; x++) assertEquals("x " + x, x < 24 ? 1008 + x : BG, px[W * 2 + x]);
        assertEquals(BG, px[W * 2 - 1]);
    }

    // ---------------------------------------------------------------- flat triangle (method376)

    @Test public void flatTriangleFillsExactlyTheGeometricSpansForEveryVertexOrder() throws Exception {
        for (int[] t : TRIANGLES) for (int[] p : PERMS) {
            int[] px = raster(W, H);
            call("drawFlatTriangle", t[p[0] * 2 + 1], t[p[1] * 2 + 1], t[p[2] * 2 + 1], t[p[0] * 2], t[p[1] * 2], t[p[2] * 2], 0x445566);
            int[] want = raster(W, H);
            for (int y = 0; y < H; y++) {
                double[] span = span(t, y);
                if (span == null) continue;
                for (int x = (int) Math.floor(span[0]); x < (int) Math.floor(span[1]); x++) want[y * W + x] = 0x445566;
            }
            assertArrayEquals(Arrays.toString(t) + Arrays.toString(p), want, px);
        }
    }

    @Test public void flatTriangleIsClippedAtBottomYAndRowZeroAndBlendsByTransparency() throws Exception {
        int[] t = {16, -6, 4, 10, 36, 2}; // top vertex above the raster: x1,y1,x2,y2,x3,y3
        int[] px = raster(W, H);
        DrawingArea.setClip(8, 0, W, 0);
        call("drawFlatTriangle", t[1], t[3], t[5], t[0], t[2], t[4], 0x445566);
        int drawn = 0;
        for (int y = 0; y < H; y++) {
            double[] span = span(t, y);
            for (int x = 0; x < W; x++) {
                boolean in = y < 8 && span != null && x >= Math.floor(span[0]) && x < Math.floor(span[1]);
                assertEquals("y " + y + " x " + x, in ? 0x445566 : BG, px[y * W + x]);
                if (in) drawn++;
            }
        }
        assertTrue(drawn > 20);
        set("transparency", 128);
        px = raster(W, H);
        Arrays.fill(px, 0x000000);
        call("drawFlatTriangle", 5, 5, 20, 10, 30, 20, 0xfefefe);
        assertEquals(0x7f7f7f, px[12 * W + 20], 1.01);
    }

    // ---------------------------------------------------------------- Gouraud triangle (method374)

    @Test public void shadedTriangleInterpolatesVertexColoursAlongEdgesForEveryVertexOrder() throws Exception {
        set("blockShading", false);
        int[] colours = {2000, 6000, 10000};
        for (int[] t : TRIANGLES) for (int[] p : PERMS) {
            int[] px = raster(W, H);
            call("drawShadedTriangle", t[p[0] * 2 + 1], t[p[1] * 2 + 1], t[p[2] * 2 + 1], t[p[0] * 2], t[p[1] * 2], t[p[2] * 2],
                    colours[p[0]], colours[p[1]], colours[p[2]]);
            int covered = 0;
            for (int y = 0; y < H; y++) {
                double[] span = span(t, y, colours);
                for (int x = 0; x < W; x++) {
                    int idx = y * W + x;
                    if (span == null || x < Math.floor(span[0]) || x >= Math.floor(span[1])) {
                        assertEquals(BG, px[idx]);
                        continue;
                    }
                    covered++;
                    int len = (int) Math.floor(span[1]) - (int) Math.floor(span[0]);
                    double expected = span[2] + (x - Math.floor(span[0])) * (span[3] - span[2]) / len;
                    assertEquals(Arrays.toString(t) + Arrays.toString(p) + " y " + y + " x " + x, expected, px[idx], 4.0);
                }
            }
            assertTrue(covered > 40);
        }
    }

    @Test public void shadedTriangleBlockModeColoursStayWithinVertexRangeAndVertexOwnsItsColour() throws Exception {
        set("blockShading", true);
        int[] t = TRIANGLES[0];
        int[] colours = {3072 + 10, 3072 + 60, 3072 + 110};
        int[] px = raster(W, H);
        call("drawShadedTriangle", t[1], t[3], t[5], t[0], t[2], t[4], colours[0], colours[1], colours[2]);
        for (int v = 0; v < 3; v++) {
            int best = 0, bestDist = Integer.MAX_VALUE;
            for (int i = 0; i < px.length; i++) {
                if (px[i] == BG) continue;
                int d = Math.abs(i % W - t[v * 2]) + Math.abs(i / W - t[v * 2 + 1]);
                if (d < bestDist) { bestDist = d; best = px[i]; }
            }
            assertEquals("vertex " + v, colours[v], best, 20);
        }
        for (int i = 0; i < px.length; i++) if (px[i] != BG) assertTrue(px[i] >= colours[0] - 5 && px[i] <= colours[2] + 5);
    }

    @Test public void clippedScanlinesEqualTheVisibleWindowOfTheUnclippedDrawing() throws Exception {
        // wide triangle (x -20..70, y 1..39) crossing both horizontal limits; the unclipped copy is shifted right by 30 on a 128-wide raster
        for (String routine : new String[] {"drawFlatTriangle", "drawShadedTriangle"}) {
            set("blockShading", false);
            set("clipScanlines", false);
            int[] big = raster(128, H);
            call(routine, args(routine, 30));
            set("clipScanlines", true);
            int[] small = raster(W, H);
            call(routine, args(routine, 0));
            int hits = 0;
            for (int y = 0; y < H; y++) for (int x = 0; x < W; x++) {
                assertEquals(routine + " y " + y + " x " + x, big[y * 128 + x + 30], small[y * W + x]);
                if (small[y * W + x] != BG) hits++;
            }
            assertTrue(hits > 300);
        }
    }

    private Object[] args(String routine, int dx) {
        return routine.equals("drawFlatTriangle") ? new Object[] {1, 39, 3, -20 + dx, 70 + dx, 60 + dx, 0x445566}
                : new Object[] {1, 39, 3, -20 + dx, 70 + dx, 60 + dx, 2000, 9000, 14000};
    }

    // ---------------------------------------------------------------- textured scanline (method379)

    private int[] makeTexture(int id, boolean low) throws Exception {
        int dim = low ? 64 : 128;
        int[] buf = new int[dim * dim * 4];
        for (int level = 0; level < 4; level++) for (int row = 0; row < dim; row++) for (int col = 0; col < dim; col++)
            buf[level * dim * dim + row * dim + col] = ((level + 1) << 20) | (row << 10) | col;
        ((int[][]) get("textureBuffers"))[id] = buf;
        ((boolean[]) get("hasTransparentPixels"))[id] = false;
        return buf;
    }

    private static int level(int p) { return (p >> 20) - 1; }
    private static int row(int p) { return (p >> 10) & 1023; }
    private static int col(int p) { return p & 1023; }

    private int[] scanline(boolean low, int rowIndex, int left, int right, int shadeL, int shadeR, int uNum, int vNum, int dNum, int uStep, int vStep, int dStep) throws Exception {
        set("lowMem", low);
        int[] px = raster(W, H);
        int[] buf = makeTexture(4, low);
        call("drawTexturedScanline", px, buf, W * rowIndex, left, right, shadeL, shadeR, uNum, vNum, dNum, uStep, vStep, dStep);
        return px;
    }

    @Test public void texturedScanlineStepsTextureColumnAndRowFromNumeratorsAndGradients() throws Exception {
        for (boolean low : new boolean[] {true, false}) {
            int unit = low ? 64 : 128, one = low ? 1 << 12 : 1 << 14;
            int[] px = scanline(low, 3, 8, 24, 0, 0, unit * 18, unit * 5, one, unit * 8, 0, 0);
            for (int x = 0; x < W; x++) {
                int p = px[3 * W + x];
                if (x < 8 || x >= 24) { assertEquals(BG, p); continue; }
                assertEquals("low " + low + " col x" + x, x - 6, col(p));
                assertEquals(5, row(p));
                assertEquals(0, level(p));
            }
            px = scanline(low, 3, 8, 24, 0, 0, unit * 18, unit * 5, one, unit * 8, unit * 8, 0); // v advances one texel per pixel too
            for (int x = 8; x < 24; x++) {
                assertEquals(x - 6, col(px[3 * W + x]));
                assertEquals(Math.floorMod(x - 19, low ? 64 : 128), row(px[3 * W + x]));
            }
        }
    }

    @Test public void texturedScanlineDividesNumeratorsByTheDepthNumerator() throws Exception {
        int[] px = scanline(true, 3, 8, 24, 0, 0, 64 * 18, 64 * 5, 2 << 12, 512, 0, 0);
        for (int x = 8; x < 24; x++) assertEquals("x " + x, (64 * 18 + 64 * (x - 24)) / 2 >> 6, col(px[3 * W + x]));
        px = scanline(true, 3, 8, 24, 0, 0, 64 * 60, 64 * 5, 4 << 12, 512, 0, 512 * 8);
        for (int x : new int[] {8, 16}) {
            int divisor = (4 * 4096 + 512 * (x - 24)) >> 12;
            assertEquals("x " + x, (64 * 60 + 64 * (x - 24)) / divisor >> 6, col(px[3 * W + x]));
        }
        int start = (64 * 60 - 1024) / 2, end = (64 * 60 - 512) / 3;
        assertEquals((start + 4 * (end - start) / 8) >> 6, col(px[3 * W + 12]), 1.01);
    }

    @Test public void texturedScanlineClampsColumnAtSpanEndsAndWrapsRows() throws Exception {
        int[] px = scanline(true, 3, 8, 24, 0, 0, 64 * 24 - 64 * 10, 64 * 5, 1 << 12, 512, 0, 0);
        for (int n = 0; n < 8; n++) assertEquals("clamped low " + n, 48 * n >> 6, col(px[3 * W + 8 + n]));
        px = scanline(true, 3, 8, 24, 0, 0, 4672 + 64 * 16, 64 * 5, 1 << 12, 512, 0, 0);
        for (int x = 8; x < 24; x++) assertEquals(63, col(px[3 * W + x]));
        px = scanline(true, 3, 8, 24, 0, 0, 64 * 18, 64 * 62 + 64 * 16, 1 << 12, 0, 512, 0);
        for (int n = 0; n < 8; n++) assertEquals("wrap " + n, (62 + n) % 64, row(px[3 * W + 8 + n]));
    }

    @Test public void texturedScanlineShadeSelectsBrightnessCopyAndShiftAndRampsPerEightPixelBlock() throws Exception {
        for (int s : new int[] {0, 15, 16, 32, 48, 63, 64, 80, 128, 200, 255}) {
            int[] px = scanline(true, 3, 8, 24, s << 8, s << 8, 64 * 18, 64 * 5, 1 << 12, 512, 0, 0);
            for (int x = 8; x < 24; x++)
                assertEquals("shade " + s, (((s >> 4 & 3) + 1) << 20 | 5 << 10 | (x - 6)) >>> (s >> 6), px[3 * W + x]);
        }
        int[] px = scanline(true, 3, 0, 35, 0, 48 << 8, 64 * 18 + 64 * 24, 64 * 5, 1 << 12, 512, 0, 0);
        int[] want = {0, 0, 1, 2};
        for (int x = 0; x < 32; x++) assertEquals("ramp block of x " + x, want[x / 8], level(px[3 * W + x]));
        for (int x = 32; x < 35; x++) assertEquals("remainder " + x, 3, level(px[3 * W + x]));
        assertEquals(BG, px[3 * W + 35]);
    }

    @Test public void texturedScanlineLargerShadeIsDarkerWithTheRealBrightnessCopies() throws Exception {
        set("lowMem", true);
        uniformTexture(4, 0xf0e0d8);
        int previous = Integer.MAX_VALUE;
        for (int s = 0; s < 256; s += 8) {
            int[] px = raster(W, H);
            call("drawTexturedScanline", px, ((int[][]) get("textureBuffers"))[4], W * 3, 8, 24, s << 8, s << 8, 64 * 18, 64 * 5, 1 << 12, 512, 0, 0);
            int b = brightness(px[3 * W + 12]);
            assertTrue("shade " + s + " brightness " + b + " <= " + previous, b <= previous);
            if (s == 0) assertEquals(0xf0e0d8 & 0xf8f8ff, px[3 * W + 12]);
            if (s == 64) assertEquals(brightness(0xf0e0d8) / 2.0, b, brightness(0xf0e0d8) * 0.04);
            previous = b;
        }
        assertTrue(previous < brightness(0xf0e0d8) / 4);
    }

    @Test public void texturedScanlineHighMemoryUses128TexturesWithFourCopiesAndLargerClamp() throws Exception {
        for (int s : new int[] {0, 16, 32, 48}) {
            int[] px = scanline(false, 3, 8, 24, s << 8, s << 8, 128 * 18, 128 * 5, 1 << 14, 1024, 0, 0);
            for (int x = 8; x < 24; x++) assertEquals("shade " + s, ((s >> 4) + 1) << 20 | 5 << 10 | (x - 6), px[3 * W + x]);
        }
        int[] px = scanline(false, 3, 8, 24, 0, 0, 16256 + 128 * 4 + 128 * 16, 128 * 5, 1 << 14, 1024, 0, 0);
        for (int x = 8; x < 24; x++) assertEquals(127, col(px[3 * W + x]));
        px = scanline(false, 3, 8, 24, 0, 0, 128 * 24 - 128 * 10, 128 * 5, 1 << 14, 1024, 0, 0);
        for (int n = 0; n < 8; n++) assertEquals("clamped low " + n, 96 * n >> 7, col(px[3 * W + 8 + n]));
    }

    @Test public void texturedScanlineOpaqueFlagControlsWhetherTransparentTexelsOverwrite() throws Exception {
        for (boolean opaque : new boolean[] {true, false}) {
            set("lowMem", true);
            int[] px = raster(W, H);
            int[] buf = makeTexture(4, true);
            buf[5 * 64 + 10] = 0; // level 0, row 5, column 10 is a transparent texel
            set("opaqueTexture", opaque);
            call("drawTexturedScanline", px, buf, W * 3, 8, 24, 0, 0, 64 * 18, 64 * 5, 1 << 12, 512, 0, 0);
            assertEquals(opaque ? 0 : BG, px[3 * W + 16]);
            assertEquals(1 << 20 | 5 << 10 | 11, px[3 * W + 17]);
        }
    }

    @Test public void texturedScanlineClipFlagKeepsTheWindowAndTheColumnFormula() throws Exception {
        for (boolean clip : new boolean[] {true, false}) {
            set("lowMem", true);
            set("clipScanlines", clip);
            int[] px = raster(W, H);
            DrawingArea.setClip(H, 0, 40, 0);
            int[] buf = makeTexture(4, true);
            call("drawTexturedScanline", px, buf, W * 3, -6, 45, 0, 0, 64 * 18, 64 * 5, 1 << 12, 512, 0, 0);
            for (int i = 0; i < px.length; i++) {
                boolean in = clip ? i / W == 3 && i % W < 40 : i >= W * 3 - 6 && i < W * 3 + 45;
                assertEquals("clip " + clip + " idx " + i, in, px[i] != BG);
            }
            if (clip) for (int x = 1; x < 40; x++) assertEquals("x " + x, Math.min(63, Math.max(0, x - 6)), col(px[3 * W + x]), 1.01);
        }
    }

    // ---------------------------------------------------------------- textured triangle (method378)

    private int[] textured(int width, int height, int[] t, int[] p, int[] shades, int[] pointX, int[] pointY, int[] pointZ, int id) throws Exception {
        int[] px = raster(width, height);
        call("drawTexturedTriangle", t[p[0] * 2 + 1], t[p[1] * 2 + 1], t[p[2] * 2 + 1], t[p[0] * 2], t[p[1] * 2], t[p[2] * 2],
                shades[p[0]], shades[p[1]], shades[p[2]], pointX[0], pointX[1], pointX[2], pointY[0], pointY[1], pointY[2],
                pointZ[0], pointZ[1], pointZ[2], id);
        return px;
    }

    private void assertMapping(int[] px, int width, int[] t, int colX, int colY, int colK, int rowX, int rowY, int rowK, int colOffset, int rowOffset) {
        int hits = 0;
        for (int y = 0; y < px.length / width; y++) for (int x = 0; x < width; x++) {
            int p = px[y * width + x];
            if (p == BG) continue;
            hits++;
            int wantCol = Math.floorDiv(colX * x + colY * y + colK, 2) + colOffset;
            int wantRow = Math.floorMod(Math.floorDiv(rowX * x + rowY * y + rowK, 2) + rowOffset, 64);
            assertEquals("col at " + x + "," + y, wantCol, col(p), 1.01);
            assertEquals("row at " + x + "," + y, wantRow, row(p), 1.01);
        }
        assertTrue("hits " + hits, hits > 60);
    }

    @Test public void texturedTriangleMapsScreenAlignedPlanesOneTexelPerWorldUnitOver64() throws Exception {
        set("lowMem", true);
        makeTexture(4, true);
        int[] t = TRIANGLES[0], flat = {0, 0, 0};
        // origin point P0 = (-24,-20,512) is the screen top-left; P1 = P0 + (64,0,0) and P2 = P0 + (0,64,0)
        int[] px = textured(W, H, t, PERMS[0], flat, new int[] {-24, 40, -24}, new int[] {-20, -20, 44}, new int[] {512, 512, 512}, 4);
        assertMapping(px, W, t, 2, 0, 0, 0, 2, 0, 0, 0); // column = x, row = y
        for (int[] p : PERMS) assertArrayEquals(px, textured(W, H, t, p, flat, new int[] {-24, 40, -24}, new int[] {-20, -20, 44}, new int[] {512, 512, 512}, 4));
        // swap which point defines which axis: column = y, row = x
        px = textured(W, H, t, PERMS[0], flat, new int[] {-24, -24, 40}, new int[] {-20, 44, -20}, new int[] {512, 512, 512}, 4);
        assertMapping(px, W, t, 0, 2, 0, 2, 0, 0, 0, 0);
        // longer u axis halves the column rate
        px = textured(W, H, t, PERMS[0], flat, new int[] {-24, 104, -24}, new int[] {-20, -20, 44}, new int[] {512, 512, 512}, 4);
        assertMapping(px, W, t, 1, 0, 0, 0, 2, 0, 0, 0);
        // the same plane at depth 1024 with axes twice as long gives the same picture (focal length 512)
        px = textured(W, H, t, PERMS[0], flat, new int[] {-48, 80, -48}, new int[] {-40, -40, 88}, new int[] {1024, 1024, 1024}, 4);
        assertMapping(px, W, t, 2, 0, 0, 0, 2, 0, 0, 0);
    }

    @Test public void texturedTriangleMapsATiltedPlaneThroughBothScreenGradients() throws Exception {
        set("lowMem", true);
        makeTexture(4, true);
        int[] t = TRIANGLES[0], flat = {0, 0, 0};
        // u runs along (1,1), v along (1,-1): column = (x+y)/2, row = (x-y)/2
        int[] px = textured(W, H, t, PERMS[0], flat, new int[] {-24, 40, 40}, new int[] {-20, 44, -84}, new int[] {512, 512, 512}, 4);
        assertMapping(px, W, t, 1, 1, 0, 1, -1, 0, 0, 0);
    }

    @Test public void texturedTriangleMeasuresFromTheScreenCentreSetByScreenSize() throws Exception {
        set("lowMem", true);
        makeTexture(4, true);
        int[] t = TRIANGLES[0], flat = {0, 0, 0};
        int[] px = textured(W + 10, H + 6, t, PERMS[0], flat, new int[] {-24, 40, -24}, new int[] {-20, -20, 44}, new int[] {512, 512, 512}, 4);
        assertMapping(px, W + 10, t, 2, 0, -10, 0, 2, -6, 0, 0); // column = x - 5, row = y - 3
    }

    @Test public void texturedTriangleVertexShadesAndTextureIdAndTransparencyFlag() throws Exception {
        set("lowMem", true);
        int[] t = TRIANGLES[0];
        int[] pointX = {-24, 40, -24}, pointY = {-20, -20, 44}, pointZ = {512, 512, 512};
        uniformTexture(4, 0xf0e0d8);
        uniformTexture(9, 0x604020);
        int[] px = textured(W, H, t, PERMS[0], new int[] {0, 0, 0}, pointX, pointY, pointZ, 9);
        for (int p : px) if (p != BG) assertEquals(0x604020 & 0xf8f8ff, p);
        px = textured(W, H, t, PERMS[0], new int[] {0, 0, 0}, pointX, pointY, pointZ, 4);
        for (int p : px) if (p != BG) assertEquals(0xf0e0d8 & 0xf8f8ff, p);
        // the darkest pixel lies near the vertex that carries shade 255, whatever the vertex order
        for (int[] p : PERMS) for (int v = 0; v < 3; v++) {
            int[] shades = {0, 0, 0};
            shades[v] = 255;
            px = textured(W, H, t, p, shades, pointX, pointY, pointZ, 4);
            int bestIndex = -1, bestBrightness = Integer.MAX_VALUE;
            for (int i = 0; i < px.length; i++) if (px[i] != BG && brightness(px[i]) < bestBrightness) { bestBrightness = brightness(px[i]); bestIndex = i; }
            int dist = Math.abs(bestIndex % W - t[v * 2]) + Math.abs(bestIndex / W - t[v * 2 + 1]);
            assertTrue("vertex " + v + " dist " + dist, dist <= 12);
        }
        // a 5x5 block of texel value 0 around texel (row 9, column 20), shown near pixel (20, 9)
        for (boolean transparent : new boolean[] {true, false}) {
            int[] buf = makeTexture(4, true);
            for (int row = 7; row <= 11; row++) for (int col = 18; col <= 22; col++) buf[row * 64 + col] = 0;
            ((boolean[]) get("hasTransparentPixels"))[4] = transparent;
            px = textured(W, H, t, PERMS[0], new int[] {0, 0, 0}, pointX, pointY, pointZ, 4);
            assertEquals(transparent ? BG : 0, px[9 * W + 20]);
        }
    }

    private void uniformTexture(int id, int color) throws Exception {
        int[] buf = new int[16384];
        for (int i = 0; i < 4096; i++) {
            int c = color & 0xf8f8ff;
            buf[i] = c;
            buf[4096 + i] = c - (c >>> 3) & 0xf8f8ff;
            buf[8192 + i] = c - (c >>> 2) & 0xf8f8ff;
            buf[12288 + i] = c - (c >>> 2) - (c >>> 3) & 0xf8f8ff;
        }
        ((int[][]) get("textureBuffers"))[id] = buf;
        ((boolean[]) get("hasTransparentPixels"))[id] = false;
    }

    private static int brightness(int rgb) {
        return (rgb >> 16 & 255) + (rgb >> 8 & 255) + (rgb & 255);
    }

    // ---------------------------------------------------------------- helpers

    /** Geometric span of triangle t = {x1,y1,x2,y2,x3,y3} on row y (half-open edge rule): {left, right}. */
    private static double[] span(int[] t, int y) {
        return span(t, y, new double[] {0, 0, 0}, false);
    }

    private static double[] span(int[] t, int y, int[] colours) {
        return span(t, y, new double[] {colours[0], colours[1], colours[2]}, true);
    }

    private static double[] span(int[] t, int y, double[] colours, boolean unused) {
        double[][] hits = new double[2][];
        int n = 0;
        for (int e = 0; e < 3; e++) {
            int a = e, b = (e + 1) % 3, ya = t[a * 2 + 1], yb = t[b * 2 + 1];
            if (ya == yb || y < Math.min(ya, yb) || y >= Math.max(ya, yb)) continue;
            double f = (y - ya) / (double) (yb - ya);
            hits[n++] = new double[] {t[a * 2] + f * (t[b * 2] - t[a * 2]), colours[a] + f * (colours[b] - colours[a])};
        }
        if (n != 2) return null;
        double[] l = hits[0][0] <= hits[1][0] ? hits[0] : hits[1], r = l == hits[0] ? hits[1] : hits[0];
        return new double[] {l[0], r[0], l[1], r[1]};
    }

    private static int[] raster(int width, int height) throws Exception {
        int[] px = new int[width * height];
        Arrays.fill(px, BG);
        DrawingArea.bindRaster(height, width, px);
        Texture.setScreenSize(width, height);
        return px;
    }

    private static Object get(String name) throws Exception {
        Field f = Texture.class.getDeclaredField(name);
        f.setAccessible(true);
        return f.get(null);
    }

    private static void set(String name, Object value) throws Exception {
        Field f = Texture.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(null, value);
    }

    private static Object call(String name, Object... args) throws Exception {
        for (Method m : Texture.class.getDeclaredMethods()) {
            if (!m.getName().equals(name)) continue;
            m.setAccessible(true);
            try {
                return m.invoke(null, args);
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof Exception) throw (Exception) e.getCause();
                throw e;
            }
        }
        throw new NoSuchMethodException(name);
    }
}
