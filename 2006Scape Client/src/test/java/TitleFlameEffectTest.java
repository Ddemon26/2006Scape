import static org.junit.Assert.*;

import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Semantics of the title-screen flame state, tested through the original identifiers: the 128x256 heat grid
 * (flameIntensity) and its scratch (829), the noise grid (1190/1191) and its scroll (flameNoiseScroll), the row sway
 * (flameRowSway), the palettes (850..853), the flash timers (greenFlameFlashTimer/1041) and the two backdrop sprites
 * (titleLeftFlameBackdrop/1202).
 */
public class TitleFlameEffectTest {
    private static final int W = 128, H = 256, BG_L = 0x112233, BG_R = 0x445566;

    private final DrawingPrimitivesTest state = new DrawingPrimitivesTest();
    private Game g;
    private int savedLoopCycle;

    @Before public void before() {
        state.saveState();
        savedLoopCycle = Game.loopCycle;
        g = new Game();
        g.graphics = new BufferedImage(765, 503, BufferedImage.TYPE_INT_RGB).getGraphics();
        g.resetImageProducers();
        g.titleFlameRuneMasks = new Background[12];
        g.flameIntensity = new int[W * H];
        g.flameNeighbourAverage = new int[W * H];
        g.flameNoise = new int[W * H];
        g.flameNoiseScratch = new int[W * H];
        g.flameNoiseScroll = 0;
        g.greenFlameFlashTimer = 0;
        g.blueFlameFlashTimer = 0;
        Game.loopCycle = 0;
    }

    @After public void after() {
        Game.loopCycle = savedLoopCycle;
        state.restoreState();
    }

    private static int at(int row, int col) { return row * W + col; }

    // ---- calcFlamesPosition: heat grid ----

    @Test public void heatMovesTowardLowerRowsAndIsAveragedWithItsNeighbours() {
        g.flameIntensity[at(60, 64)] = 200;
        g.calcFlamesPosition();
        int[] h = g.flameIntensity;
        assertEquals(50, h[at(58, 64)]);
        assertEquals(50, h[at(60, 64)]);
        assertEquals(50, h[at(59, 63)]);
        assertEquals(50, h[at(59, 65)]);
        assertEquals(0, h[at(59, 64)]);
        assertEquals(0, h[at(61, 64)]);
        int sum = 0;
        for (int i = 0; i < at(121, 0); i++) sum += h[i];
        assertEquals(200, sum);
    }

    @Test public void scratchGridHoldsTheFourNeighbourMeanOfTheSeededHeatGridInTheInterior() {
        int[] before = g.flameIntensity;
        for (int r = 10; r <= 100; r++) for (int c = 0; c < W; c++) before[at(r, c)] = (r * 31 + c * 17) % 256;
        int[] copy = before.clone();
        Arrays.fill(g.flameNeighbourAverage, -7);
        g.calcFlamesPosition();
        for (int r = 5; r <= 110; r++) {
            for (int c = 1; c <= 126; c++) {
                int expect = (copy[at(r, c - 1)] + copy[at(r, c + 1)] + copy[at(r - 1, c)] + copy[at(r + 1, c)]) / 4;
                assertEquals("r" + r + " c" + c, expect, g.flameNeighbourAverage[at(r, c)]);
            }
        }
        for (int r = 0; r < H; r++) {
            assertEquals(-7, g.flameNeighbourAverage[at(r, 0)]);
            assertEquals(-7, g.flameNeighbourAverage[at(r, 127)]);
        }
        for (int c = 0; c < W; c++) {
            assertEquals(-7, g.flameNeighbourAverage[at(0, c)]);
            assertEquals(-7, g.flameNeighbourAverage[at(255, c)]);
        }
    }

    @Test public void edgeColumnsAndFirstRowAreNeverRecomputed() {
        Arrays.fill(g.flameIntensity, 100);
        g.calcFlamesPosition();
        for (int r = 0; r < H; r++) {
            assertEquals(100, g.flameIntensity[at(r, 0)]);
            assertEquals(100, g.flameIntensity[at(r, 127)]);
        }
        for (int c = 0; c < W; c++) assertEquals(100, g.flameIntensity[at(0, c)]);
        for (int r = 1; r <= 120; r++) for (int c = 1; c <= 126; c++) assertEquals(100, g.flameIntensity[at(r, c)]);
    }

    @Test public void fuelRowFeedsTheRowAboveItAndNothingReachesTheTopInOneStep() {
        g.calcFlamesPosition();
        int[] h = g.flameIntensity;
        for (int i = 0; i < at(121, 0); i++) assertEquals(0, h[i]);
        int row253 = 0, row254 = 0;
        for (int c = 0; c < W; c++) { row253 += h[at(253, c)]; row254 += h[at(254, c)]; }
        assertTrue("fuel row heat reaches row 253: " + row253, row253 > 2500);
        assertEquals(0, row254);
    }

    @Test public void randomSparksLandOnlyInTheLastRowAtValue192InColumns2To125() {
        for (int i = 0; i < 3000; i++) g.calcFlamesPosition();
        for (int c = 0; c < W; c++) {
            int v = g.flameIntensity[at(255, c)];
            if (c >= 2 && c <= 125) assertEquals("col " + c, 192, v);
            else assertEquals("col " + c, 0, v);
        }
    }

    // ---- calcFlamesPosition: noise grid and its scroll ----

    private void fillBand(int value) {
        for (int r = 10; r <= 110; r++) for (int c = 0; c < W; c++) g.flameIntensity[at(r, c)] = value;
    }

    @Test public void noiseFifthIsSubtractedFromTheCellAtTheScrolledIndex() {
        fillBand(100);
        g.flameNoiseScroll = 256;
        g.flameNoise[at(50, 40) + 384] = 100;
        g.flameNoise[at(50, 40)] = 250;
        g.calcFlamesPosition();
        assertEquals(384, g.flameNoiseScroll);
        assertEquals(80, g.flameIntensity[at(50, 40)]);
        assertEquals(50, g.flameIntensity[at(47, 40)]);
        assertEquals(100, g.flameIntensity[at(50, 41)]);
        assertEquals(100, g.flameIntensity[at(49, 40)]);
    }

    @Test public void scrollIndexWrapsAroundTheNoiseLengthWithoutRerandomising() {
        fillBand(100);
        g.flameNoiseScroll = 32768 - 128;
        int[] noise = g.flameNoise;
        noise[at(50, 40)] = 100;
        g.calcFlamesPosition();
        assertEquals(32768, g.flameNoiseScroll);
        assertSame(noise, g.flameNoise);
        assertEquals(80, g.flameIntensity[at(50, 40)]);
        assertEquals(100, g.flameIntensity[at(50, 41)]);
    }

    @Test public void passingTheNoiseLengthRewindsTheScrollAndRegeneratesTheNoise() {
        Arrays.fill(g.flameNoise, 77);
        g.flameNoiseScroll = 32768;
        int[] noiseArray = g.flameNoise;
        g.calcFlamesPosition();
        assertEquals(128, g.flameNoiseScroll);
        assertSame(noiseArray, g.flameNoise);
        int max = 0;
        for (int r = 1; r <= 254; r++) for (int c = 1; c <= 126; c++) max = Math.max(max, g.flameNoise[at(r, c)]);
        assertTrue(max < 160 && max > 0);
    }

    @Test public void noiseCannotDriveHeatBelowZero() {
        fillBand(10);
        Arrays.fill(g.flameNoise, 250);
        g.calcFlamesPosition();
        for (int r = 1; r <= 120; r++) for (int c = 1; c <= 126; c++) assertEquals(0, g.flameIntensity[at(r, c)]);
    }

    // ---- randomizeBackground: builds the noise ----

    @Test public void randomizeBackgroundBuildsSmoothedSparseNoiseInTheNoiseArray() {
        Arrays.fill(g.flameNoise, 99);
        int[] noise = g.flameNoise, scratch = g.flameNoiseScratch;
        g.randomizeBackground(null);
        assertSame(noise, g.flameNoise);
        assertSame(scratch, g.flameNoiseScratch);
        long sum = 0;
        int max = 0, min = 1000;
        for (int v : noise) { sum += v; min = Math.min(min, v); }
        for (int r = 1; r <= 254; r++) for (int c = 1; c <= 126; c++) max = Math.max(max, noise[at(r, c)]);
        double mean = sum / (double) noise.length;
        assertTrue("mean " + mean, mean > 8 && mean < 30);
        assertTrue("max " + max, max > 0 && max < 160);
        assertEquals(0, min);
    }

    private static Background mask(int w, int h, int offsetX, int offsetY, int index) throws Exception {
        Field uf = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        uf.setAccessible(true);
        sun.misc.Unsafe u = (sun.misc.Unsafe) uf.get(null);
        Background b = (Background) u.allocateInstance(Background.class);
        b.imageWidth = w;
        b.imageHeight = h;
        b.offsetX = offsetX;
        b.offsetY = offsetY;
        b.paletteIndices = new byte[w * h];
        Arrays.fill(b.paletteIndices, (byte) index);
        return b;
    }

    @Test public void nonzeroMaskPixelsClearTheNoiseAtMaskPositionPlus16AndOffset() throws Exception {
        g.randomizeBackground(mask(30, 30, 5, 7, 1));
        int[] n = g.flameNoise;
        for (int y = 0; y < 30; y++) for (int x = 0; x < 30; x++) assertEquals(0, n[at(16 + 7 + y, 16 + 5 + x)]);
        int ring = 0;
        for (int x = 20; x <= 51; x++) { ring += n[at(22, x)] + n[at(53, x)]; }
        for (int y = 22; y <= 53; y++) { ring += n[at(y, 20)] + n[at(y, 51)]; }
        assertTrue("noise survives just outside the mask", ring > 0);
    }

    @Test public void zeroMaskIndicesLeaveTheNoiseAlone() throws Exception {
        g.randomizeBackground(mask(30, 30, 5, 7, 0));
        int box = 0;
        for (int y = 0; y < 30; y++) for (int x = 0; x < 30; x++) box += g.flameNoise[at(16 + 7 + y, 16 + 5 + x)];
        assertTrue(box > 0);
    }

    // ---- flameRowSway: row sway ----

    @Test public void swayArrayShiftsUpOneSlotAndAppendsABoundedSineSum() {
        for (int i = 0; i < 256; i++) g.flameRowSway[i] = i + 1000;
        Game.loopCycle = 0;
        g.calcFlamesPosition();
        for (int i = 0; i < 255; i++) assertEquals(i + 1001, g.flameRowSway[i]);
        assertEquals(0, g.flameRowSway[255]);
        Set<Integer> seen = new HashSet<>();
        for (int cycle = 0; cycle < 400; cycle += 20) {
            Game.loopCycle = cycle;
            g.calcFlamesPosition();
            int v = g.flameRowSway[255];
            assertTrue("|" + v + "|<=42", Math.abs(v) <= 42);
            seen.add(v);
        }
        assertTrue(seen.size() > 5);
    }

    // ---- doFlamesDrawing ----

    private void drawFixture() {
        g.titleLeftFlameBackdrop = new Sprite(128, 265);
        g.titleRightFlameBackdrop = new Sprite(128, 265);
        Arrays.fill(g.titleLeftFlameBackdrop.pixels, BG_L);
        Arrays.fill(g.titleRightFlameBackdrop.pixels, BG_R);
        g.activeFlamePalette = new int[256];
        g.flamePaletteRed = new int[256];
        g.flamePaletteGreen = new int[256];
        g.flamePaletteBlue = new int[256];
        Arrays.fill(g.flamePaletteRed, 0xff0000);
        Arrays.fill(g.flamePaletteGreen, 0x00ff00);
        Arrays.fill(g.flamePaletteBlue, 0x0000ff);
        Arrays.fill(g.flameRowSway, 0);
    }

    private static boolean lit(int pixel, int bg) { return pixel != bg; }

    @Test public void flamesFillTheLeftPaneColumns0To105AndTheRightPaneColumns24To126ForRows9To262() {
        drawFixture();
        Arrays.fill(g.flameIntensity, 255);
        g.doFlamesDrawing();
        int[] l = g.titleLeftFlamePaneProducer.pixels, r = g.titleRightFlamePaneProducer.pixels;
        for (int y = 0; y < 265; y++) {
            for (int x = 0; x < 128; x++) {
                boolean rows = y >= 9 && y <= 262;
                assertEquals("L " + x + "," + y, rows && x <= 105, lit(l[y * 128 + x], BG_L));
                assertEquals("R " + x + "," + y, rows && x >= 24 && x <= 126, lit(r[y * 128 + x], BG_R));
            }
        }
    }

    @Test public void gridCellMapsToTheLeftPane22ColumnsLeftAndTheRightPane24ColumnsRight() {
        drawFixture();
        g.flameIntensity[at(100, 50)] = 255;
        g.flameIntensity[at(30, 110)] = 255;
        g.doFlamesDrawing();
        int[] l = g.titleLeftFlamePaneProducer.pixels, r = g.titleRightFlamePaneProducer.pixels;
        int litL = 0, litR = 0;
        for (int i = 0; i < l.length; i++) { if (lit(l[i], BG_L)) litL++; if (lit(r[i], BG_R)) litR++; }
        assertEquals(2, litL);
        assertEquals(1, litR);
        assertTrue(lit(l[109 * 128 + 28], BG_L));
        assertTrue(lit(l[39 * 128 + 88], BG_L));
        assertTrue(lit(r[109 * 128 + 74], BG_R));
    }

    @Test public void swayShiftsFlamesHorizontallyMostlyAtTheTopAndNotAtTheBottom() {
        drawFixture();
        Arrays.fill(g.flameIntensity, 255);
        Arrays.fill(g.flameRowSway, 64);
        g.doFlamesDrawing();
        int[] l = g.titleLeftFlamePaneProducer.pixels, r = g.titleRightFlamePaneProducer.pixels;
        assertFalse(lit(l[9 * 128 + 43], BG_L));
        assertTrue(lit(l[9 * 128 + 42], BG_L));
        assertTrue(lit(l[262 * 128 + 105], BG_L));
        assertFalse(lit(l[262 * 128 + 106], BG_L));
        assertFalse(lit(r[9 * 128 + 86], BG_R));
        assertTrue(lit(r[9 * 128 + 87], BG_R));
        assertTrue(lit(r[9 * 128 + 126], BG_R));
        assertFalse(lit(r[9 * 128 + 127], BG_R));
        assertTrue(lit(r[262 * 128 + 24], BG_R));
    }

    @Test public void negativeSwayIsClampedAtTheLeftPaneEdge() {
        drawFixture();
        Arrays.fill(g.flameIntensity, 255);
        Arrays.fill(g.flameRowSway, -64);
        g.doFlamesDrawing();
        int[] l = g.titleLeftFlamePaneProducer.pixels;
        assertTrue(lit(l[9 * 128], BG_L));
        assertTrue(lit(l[9 * 128 + 127], BG_L));
    }

    private int redGreenBlue(int pixel) { return (pixel >> 16 & 0xff) > 200 ? 1 : (pixel >> 8 & 0xff) > 200 ? 2 : (pixel & 0xff) > 200 ? 3 : 0; }

    private int colourWith(int timer40, int timer41) {
        drawFixture();
        Arrays.fill(g.flameIntensity, 255);
        g.greenFlameFlashTimer = timer40;
        g.blueFlameFlashTimer = timer41;
        g.doFlamesDrawing();
        return redGreenBlue(g.titleLeftFlamePaneProducer.pixels[100 * 128 + 80]);
    }

    @Test public void defaultPaletteIs851ThenGreen852ThenBlue853WhileTheFlashTimersRun() {
        assertEquals(1, colourWith(0, 0));
        assertEquals(2, colourWith(512, 0));
        assertEquals(2, colourWith(768, 0));
        assertEquals(2, colourWith(256, 0));
        assertEquals(3, colourWith(0, 512));
        assertEquals(2, colourWith(512, 512));
        assertEquals(1, colourWith(-4, 0));
    }

    @Test public void activePaletteIsRebuiltEveryFrameFromTheThreeBasePalettes() {
        drawFixture();
        for (int i = 0; i < 256; i++) { g.flamePaletteRed[i] = 0x010000 * (i & 127); g.flamePaletteGreen[i] = 0x000100 * i; g.flamePaletteBlue[i] = i; }
        Arrays.fill(g.activeFlamePalette, -1);
        g.doFlamesDrawing();
        assertArrayEquals(g.flamePaletteRed, g.activeFlamePalette);
        g.greenFlameFlashTimer = 1024;
        g.doFlamesDrawing();
        assertArrayEquals(g.flamePaletteRed, g.activeFlamePalette);
        g.greenFlameFlashTimer = 512;
        g.doFlamesDrawing();
        assertArrayEquals(g.flamePaletteGreen, g.activeFlamePalette);
        g.greenFlameFlashTimer = 0;
        g.blueFlameFlashTimer = 512;
        g.doFlamesDrawing();
        assertArrayEquals(g.flamePaletteBlue, g.activeFlamePalette);
        g.blueFlameFlashTimer = 900;
        g.doFlamesDrawing();
        int mid = g.activeFlamePalette[200];
        assertTrue((mid >> 16 & 0xff) > 0 && (mid & 0xff) > 0);
    }

    @Test public void backdropSpritesAreCopiedIntoThePanesEachFrameAndNeverModified() {
        drawFixture();
        Arrays.fill(g.titleLeftFlamePaneProducer.pixels, 7);
        Arrays.fill(g.titleRightFlamePaneProducer.pixels, 7);
        g.doFlamesDrawing();
        for (int v : g.titleLeftFlamePaneProducer.pixels) assertEquals(BG_L, v);
        for (int v : g.titleRightFlamePaneProducer.pixels) assertEquals(BG_R, v);
        Arrays.fill(g.flameIntensity, 255);
        g.doFlamesDrawing();
        for (int v : g.titleLeftFlameBackdrop.pixels) assertEquals(BG_L, v);
        for (int v : g.titleRightFlameBackdrop.pixels) assertEquals(BG_R, v);
        g.flameIntensity = new int[W * H];
        g.doFlamesDrawing();
        for (int v : g.titleLeftFlamePaneProducer.pixels) assertEquals(BG_L, v);
    }

    // ---- flash timers: calcFlamesPosition ----

    @Test public void runningTimerCountsDownByFourAndOtherStaysZero() {
        g.greenFlameFlashTimer = 1024;
        g.calcFlamesPosition();
        assertEquals(1020, g.greenFlameFlashTimer);
        assertEquals(0, g.blueFlameFlashTimer);
        g.greenFlameFlashTimer = 0;
        g.blueFlameFlashTimer = 100;
        g.calcFlamesPosition();
        assertEquals(0, g.greenFlameFlashTimer);
        assertEquals(96, g.blueFlameFlashTimer);
        g.greenFlameFlashTimer = 40;
        g.blueFlameFlashTimer = 8;
        g.calcFlamesPosition();
        assertEquals(36, g.greenFlameFlashTimer);
        assertEquals(4, g.blueFlameFlashTimer);
    }

    @Test public void timersStartAt1024OneAtATimeOnlyWhenBothAreZero() {
        int green = 0, blue = 0;
        for (int n = 0; n < 40000 && (green < 2 || blue < 2); n++) {
            g.calcFlamesPosition();
            int a = g.greenFlameFlashTimer, b = g.blueFlameFlashTimer;
            assertFalse("never both running", a > 0 && b > 0);
            if (a > 0 || b > 0) {
                if (a > 0) { assertEquals(1024, a); green++; } else { assertEquals(1024, b); blue++; }
                int expectLeft = 1024;
                while (g.greenFlameFlashTimer > 0 || g.blueFlameFlashTimer > 0) {
                    g.calcFlamesPosition();
                    expectLeft -= 4;
                    if (expectLeft == 0) break; // a new flash may start in the step the old one ends
                    assertEquals(expectLeft, a > 0 ? g.greenFlameFlashTimer : g.blueFlameFlashTimer);
                    assertEquals(0, a > 0 ? g.blueFlameFlashTimer : g.greenFlameFlashTimer);
                }
            }
        }
        assertTrue("green " + green, green >= 1);
        assertTrue("blue " + blue, blue >= 1);
    }

    // ---- additional proofs: spark rows, rune mask choice, mask pixel order, blend ----

    @Test public void sparksFeedTheScratchGridOnlyBelowRow127() {
        g.calcFlamesPosition();
        for (int i = 0; i < at(127, 0); i++) assertEquals("row " + i / W, 0, g.flameNeighbourAverage[i]);
        int cells = 0;
        for (int r = 130; r <= 250; r++) for (int c = 0; c < W; c++) if (g.flameNeighbourAverage[at(r, c)] != 0) cells++;
        assertTrue("spark neighbourhoods " + cells, cells > 250);
    }

    @Test public void regenerationPicksOneOfTheTwelveRuneMasksAtRandom() throws Exception {
        for (int k = 0; k < 12; k++) g.titleFlameRuneMasks[k] = mask(6, 100, 8 * k, 0, 1);
        Set<Integer> seen = new HashSet<>();
        for (int n = 0; n < 400 && seen.size() < 12; n++) {
            g.flameNoiseScroll = 32768;
            g.calcFlamesPosition();
            int found = -1, count = 0;
            for (int k = 0; k < 12; k++) {
                boolean zero = true;
                for (int r = 20; r <= 100 && zero; r++) zero = g.flameNoise[at(r, 16 + 8 * k + 2)] == 0;
                if (zero) { found = k; count++; }
            }
            assertEquals(1, count);
            seen.add(found);
        }
        assertEquals(12, seen.size());
    }

    @Test public void maskPixelsAreReadRowByRowFromTheMask() throws Exception {
        Background b = mask(40, 40, 3, 2, 0);
        b.paletteIndices[9 * 40 + 5] = 1;
        g.randomizeBackground(b);
        assertEquals(0, g.flameNoise[at(16 + 2 + 9, 16 + 3 + 5)]);
        assertTrue(g.flameNoise[at(16 + 2 + 5, 16 + 3 + 9)] > 0);
    }

    @Test public void heatIsTheOpacityOfThePaletteColourOverThePaneBackdrop() {
        drawFixture();
        Arrays.fill(g.flameIntensity, 128);
        Arrays.fill(g.flameRowSway, 0);
        g.doFlamesDrawing();
        assertEquals(0x881119, g.titleLeftFlamePaneProducer.pixels[50 * 128 + 10] & 0xffffff);
        assertEquals(0xa12a33, g.titleRightFlamePaneProducer.pixels[50 * 128 + 50] & 0xffffff);
    }

    private static final class ConfiguredTitle extends Game {
        String value;
        boolean throwParameter;
        @Override public String getParameter(String key) {
            assertEquals("fl_icon", key);
            if (throwParameter) throw new IllegalStateException("fixture parameter unavailable");
            return value;
        }
        @Override public void drawLoadingText(int percent, String text) { }
    }

    /** Sixteen distinguishable 2x2 archive images: each has its own palette index and offset. */
    private static StreamLoader numberedTitleArchive() throws Exception {
        byte[] index = new byte[5 + 16 * 3 + 16 * 7];
        index[1] = 64; index[3] = 64; index[4] = 17;
        byte[] pixels = new byte[2 + 16 * 4];
        for (int n = 0; n < 16; n++) {
            int at = 5 + 16 * 3 + n * 7;
            index[at] = (byte) n; index[at + 1] = (byte) (15 - n);
            index[at + 3] = 2; index[at + 5] = 2;
            pixels[2 + n * 4] = (byte) (n + 1);
            pixels[2 + n * 4 + 3] = (byte) (n + 1);
        }
        String[] names = {"index.dat", "runes.dat", "titlebox.dat", "titlebutton.dat"};
        byte[][] entries = {index, pixels, pixels, pixels};
        byte[] data = new byte[index.length + 3 * pixels.length];
        int[] hashes = new int[4], sizes = new int[4], offsets = new int[4];
        int cursor = 0;
        for (int n = 0; n < 4; n++) {
            for (char ch : names[n].toUpperCase(java.util.Locale.ROOT).toCharArray())
                hashes[n] = hashes[n] * 61 + ch - 32;
            sizes[n] = entries[n].length; offsets[n] = cursor;
            System.arraycopy(entries[n], 0, data, cursor, sizes[n]); cursor += sizes[n];
        }
        Field f = sun.misc.Unsafe.class.getDeclaredField("theUnsafe"); f.setAccessible(true);
        StreamLoader archive = (StreamLoader) ((sun.misc.Unsafe) f.get(null)).allocateInstance(StreamLoader.class);
        String[] fields = {"archiveData", "archiveDecompressed", "entryCount", "entryNameHashes",
            "entryUnpackedSizes", "entryPackedSizes", "entryOffsets"};
        Object[] values = {data, true, 4, hashes, sizes, sizes, offsets};
        for (int n = 0; n < fields.length; n++) {
            Field field = StreamLoader.class.getDeclaredField(fields[n]); field.setAccessible(true); field.set(archive, values[n]);
        }
        return archive;
    }

    @Test public void titleParameterSelectsAllTwelveArchiveEntriesAndCleanupReleasesThem() throws Exception {
        String[] inputs = {null, "bad", "0", "1", "-7", "2147483647", "2147483648", "throw"};
        int[][] expected = {
            {0,1,2,3,4,5,6,7,8,9,10,11}, {0,1,2,3,4,5,6,7,8,9,10,11},
            {0,1,2,3,4,5,6,7,8,9,10,11}, {12,13,14,15,12,13,14,15,12,13,14,15},
            {12,13,14,15,12,13,14,15,12,13,14,15}, {12,13,14,15,12,13,14,15,12,13,14,15},
            {0,1,2,3,4,5,6,7,8,9,10,11}, {0,1,2,3,4,5,6,7,8,9,10,11}};
        for (int c = 0; c < inputs.length; c++) {
            ConfiguredTitle game = new ConfiguredTitle();
            game.graphics = new BufferedImage(765, 503, BufferedImage.TYPE_INT_RGB).getGraphics();
            game.resetImageProducers(); game.titleStreamLoader = numberedTitleArchive();
            game.flameThreadActive = true; game.value = inputs[c]; game.throwParameter = "throw".equals(inputs[c]);
            game.loadTitleScreen();
            assertEquals(12, game.titleFlameRuneMasks.length);
            for (int slot = 0; slot < 12; slot++) {
                Background b = game.titleFlameRuneMasks[slot];
                int n = expected[c][slot];
                assertEquals("input " + inputs[c] + " slot " + slot, n, b.offsetX);
                assertEquals(15 - n, b.offsetY);
                assertArrayEquals(new byte[] {(byte)(n+1),0,0,(byte)(n+1)}, b.paletteIndices);
                assertEquals(2, b.imageWidth); assertEquals(2, b.imageHeight);
                for (int other = 0; other < slot; other++) assertNotSame(game.titleFlameRuneMasks[other], b);
            }
            game.nullLoader(); assertNull(game.titleFlameRuneMasks);
        }
    }
}
