import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SpriteRotatedWindowTest {
    private static final int FILL = 0x204060;
    private int[] savedPixels;
    private int[] saved;
    private boolean savedFiltering;

    @Before public void saveDrawingArea() {
        savedPixels = DrawingArea.pixels;
        savedFiltering = ClientSettings.BILINEAR_MINIMAP_FILTERING;
        saved = new int[] {DrawingArea.width, DrawingArea.height, DrawingArea.topX,
                DrawingArea.topY, DrawingArea.bottomX, DrawingArea.bottomY,
                DrawingArea.centerX, DrawingArea.centerY, DrawingArea.anInt1387};
    }

    @After public void restoreDrawingArea() {
        DrawingArea.pixels = savedPixels;
        DrawingArea.width = saved[0]; DrawingArea.height = saved[1];
        DrawingArea.topX = saved[2]; DrawingArea.topY = saved[3];
        DrawingArea.bottomX = saved[4]; DrawingArea.bottomY = saved[5];
        DrawingArea.centerX = saved[6]; DrawingArea.centerY = saved[7];
        DrawingArea.anInt1387 = saved[8];
        ClientSettings.BILINEAR_MINIMAP_FILTERING = savedFiltering;
    }

    @Test public void coordinatesPlaceFixedTwentyPixelWindowInDestinationPixels() {
        Sprite sprite = numberedSprite();
        for (int[] position : new int[][] {{0,0}, {1,0}, {0,1}, {3,7}, {12,12}}) {
            int[] raster = target();
            sprite.drawRotatedNonzero20x20(position[0], 0, position[1]);
            assertArrayEquals(cardinalExpected(sprite, position[0], position[1], 0), raster);
        }
    }

    @Test public void radiansRotateSamplesAroundSourceFifteenFifteen() {
        Sprite sprite = numberedSprite();
        double[] angles = {0, Math.PI / 2, Math.PI, -Math.PI / 2, 2 * Math.PI};
        int[] turns = {0, 1, 2, 3, 0};
        for (int i = 0; i < angles.length; i++) {
            int[] raster = target();
            sprite.drawRotatedNonzero20x20(2, angles[i], 3);
            assertArrayEquals(cardinalExpected(sprite, 2, 3, turns[i]), raster);
        }
        // Ninety radians is not a quarter turn (ninety degrees).
        int[] raster = target();
        sprite.drawRotatedNonzero20x20(2, 90, 3);
        org.junit.Assert.assertFalse(Arrays.equals(cardinalExpected(sprite, 2, 3, 1), raster));
    }

    @Test public void onlyIntegerZeroIsTransparentAndOtherValuesCopyExactly() {
        Sprite sprite = new Sprite(40, 40);
        sprite.pixels[5 + 5 * 40] = 0x01000000;
        sprite.pixels[6 + 5 * 40] = -1;
        sprite.pixels[7 + 5 * 40] = 0x7f123456;
        sprite.pixels[24 + 24 * 40] = 9;
        // Immediately outside all four sampled edges: none may appear.
        sprite.pixels[4 + 5 * 40] = 11;
        sprite.pixels[25 + 5 * 40] = 12;
        sprite.pixels[5 + 4 * 40] = 13;
        sprite.pixels[5 + 25 * 40] = 14;
        int[] source = sprite.pixels.clone();
        int[] raster = target();
        sprite.drawRotatedNonzero20x20(1, 0, 2);
        int[] expected = filled();
        expected[2 + 32] = 0x01000000;
        expected[3 + 32] = -1;
        expected[4 + 32] = 0x7f123456;
        expected[21 + 20 * 32] = 9;
        assertArrayEquals(expected, raster);
        assertArrayEquals(source, sprite.pixels);
    }

    @Test public void spriteOffsetsCanvasSizeAndActiveClipDoNotMoveOrClipWindow() {
        Sprite sprite = numberedSprite();
        int[] expected = cardinalExpected(sprite, 2, 3, 0);
        for (int[] offset : new int[][] {{0,0}, {4,0}, {0,7}, {4,7}}) {
            sprite.offsetX = offset[0]; sprite.offsetY = offset[1];
            sprite.trimWidth = 70; sprite.trimHeight = 90;
            int[] raster = target();
            DrawingArea.setDrawingArea(1, 0, 1, 0);
            sprite.drawRotatedNonzero20x20(2, 0, 3);
            assertArrayEquals(expected, raster);
        }
    }

    @Test public void encodedAnglesRotateHorizontalAndVerticalSamplingDirections() {
        Sprite sprite = numberedSprite();
        ClientSettings.BILINEAR_MINIMAP_FILTERING = false;
        // Independent lattice landmarks four pixels right/below the center.
        // 256 is just below 45 degrees; 512 is just below 90 degrees.
        int[][] cases = {{0,20,16,16,20}, {256,18,13,18,18},
                {-256,18,18,13,18}, {512,16,12,19,16},
                {-512,16,19,12,16}, {1024,12,15,16,12}, {2048,19,16,15,19}};
        for (int[] c : cases) for (int probe = 0; probe < 3; probe++) {
            int[] counts = new int[9], offsets = new int[9];
            int row = probe == 2 ? 8 : 4;
            counts[row] = 1; offsets[row] = probe == 1 ? 4 : 0;
            int sx = probe == 0 ? 16 : c[probe == 1 ? 1 : 3];
            int sy = probe == 0 ? 16 : c[probe == 1 ? 2 : 4];
            int[] expected = filled(), raster = target();
            expected[3 + offsets[row] + (2 + row) * 32] = sprite.pixels[sx + sy * 40];
            sprite.drawRotatedScanlines(9, c[0], counts, 256, offsets, 16, 2, 3, 0, 16);
            assertArrayEquals("angle=" + c[0] + " probe=" + probe, expected, raster);
        }
    }

    @Test public void encodedAngleCalibrationMatchesRadiansBut2048IsNotAnExactTurn() {
        Sprite sprite = numberedSprite();
        int[] counts = new int[20], offsets = new int[20]; Arrays.fill(counts, 20);
        ClientSettings.BILINEAR_MINIMAP_FILTERING = false;
        for (int angle : new int[] {-2048,-512,-1,0,1,137,256,512,1024,2047,2048}) {
            int[] radians = target();
            sprite.drawRotatedNonzero20x20(2, angle / 326.11D, 3);
            int[] encoded = target();
            sprite.drawRotatedScanlines(20, angle, counts, 256, offsets, 15, 2, 3, 20, 15);
            assertArrayEquals("angle=" + angle, radians, encoded);
        }
        int[] zero = target();
        sprite.drawRotatedScanlines(20, 0, counts, 256, offsets, 15, 2, 3, 20, 15);
        int[] turn = target();
        sprite.drawRotatedScanlines(20, 2048, counts, 256, offsets, 15, 2, 3, 20, 15);
        org.junit.Assert.assertFalse(Arrays.equals(zero, turn));
        int[] ninety = target();
        sprite.drawRotatedScanlines(20, 90, counts, 256, offsets, 15, 2, 3, 20, 15);
        int[] quarter = target();
        sprite.drawRotatedNonzero20x20(2, Math.PI / 2, 3);
        org.junit.Assert.assertFalse(Arrays.equals(ninety, quarter));
    }

    @Test public void bilinearCornerContributionsUseFractionalSamplePositionWithoutAlpha() {
        // Gray 128 at one corner at a time: observable area contributions,
        // explicitly tabulated independently of the renderer's integer arithmetic.
        int[][] cases = {{0,128,0,0,0}, {1,127,0,0,0}, {64,72,24,24,8},
                {128,32,32,32,32}, {192,8,24,24,72}, {255,0,0,0,127}};
        for (int[] c : cases) for (int corner = 0; corner < 4; corner++) {
            Sprite sprite = new Sprite(40, 40);
            sprite.pixels[16 + corner % 2 + (16 + corner / 2) * 40] = 0x808080;
            int[] source = sprite.pixels.clone();
            for (boolean filtering : new boolean[] {false,true}) {
                ClientSettings.BILINEAR_MINIMAP_FILTERING = filtering;
                int value = filtering ? c[corner + 1] : corner == 0 ? 128 : 0;
                int[] expected = filled(), raster = target();
                expected[4 + 4 * 32] = value * 0x010101;
                sprite.drawRotatedScanlines(3, 0, new int[] {0,0,1}, c[0], new int[] {0,0,1},
                        16, 2, 3, 0, 16);
                assertArrayEquals(expected, raster);
            }
            assertArrayEquals(source, sprite.pixels);
        }
    }

    @Test public void fractionalSamplesUseEightBitsOfFractionAndSixteenBitCoordinateSteps() {
        Sprite sprite = new Sprite(40, 40);
        for (int y = 0; y < 40; y++) for (int x = 0; x < 40; x++)
            sprite.pixels[x + y * 40] = x == 17 ? 0x800000 : 0;
        // At zero angle scale=1 moves 1/256 pixel: one target step gives zero
        // channel units, two give one, and 256 select the next source pixel.
        ClientSettings.BILINEAR_MINIMAP_FILTERING = true;
        for (int[] c : new int[][] {{0,0}, {1,0}, {2,1}, {64,32}, {128,64}, {255,127}, {256,128}}) {
            int[] raster = new int[260 * 3], expected = new int[raster.length];
            Arrays.fill(raster, FILL); Arrays.fill(expected, FILL);
            DrawingArea.initDrawingArea(3, 260, raster);
            sprite.drawRotatedScanlines(1, 0, new int[] {1}, 1, new int[] {c[0]}, 16, 1, 0, 0, 16);
            expected[c[0] + 260] = c[1] << 16;
            assertArrayEquals(expected, raster);
        }
        // A sub-1/256 coordinate fraction is discarded before color weighting.
        // sin(1 / 326.11) is between .0030 and .0031: at unit scale a single
        // vertical step moves X less than 1/256; two steps cross that boundary.
        for (int[] c : new int[][] {{1,0}, {2,0}, {3,1}}) {
            int rows = c[0] * 2 + 1;
            int[] counts = new int[rows], offsets = new int[rows]; counts[rows - 1] = 1;
            int[] expected = filled(), raster = target();
            expected[3 + (2 + rows - 1) * 32] = c[1] << 16;
            sprite.drawRotatedScanlines(rows, 1, counts, 256, offsets, 16, 2, 3, 0, 16);
            assertArrayEquals(expected, raster);
        }
    }

    private static Sprite numberedSprite() {
        Sprite sprite = new Sprite(40, 40);
        for (int i = 0; i < sprite.pixels.length; i++) sprite.pixels[i] = i + 1;
        return sprite;
    }

    // Independent exact lattice geometry for quarter turns, no fixed-point renderer copied.
    private static int[] cardinalExpected(Sprite sprite, int y, int x, int turn) {
        int[] expected = filled();
        for (int row = 0; row < 20; row++) for (int col = 0; col < 20; col++) {
            int sx, sy;
            switch (turn) {
                case 1: sx = 5 + row; sy = 25 - col; break;
                case 2: sx = 25 - col; sy = 25 - row; break;
                case 3: sx = 25 - row; sy = 5 + col; break;
                default: sx = 5 + col; sy = 5 + row;
            }
            expected[x + col + (y + row) * 32] = sprite.pixels[sx + sy * 40];
        }
        return expected;
    }

    private static int[] target() {
        int[] raster = filled();
        DrawingArea.initDrawingArea(32, 32, raster);
        return raster;
    }

    private static int[] filled() {
        int[] raster = new int[32 * 32]; Arrays.fill(raster, FILL); return raster;
    }
}
