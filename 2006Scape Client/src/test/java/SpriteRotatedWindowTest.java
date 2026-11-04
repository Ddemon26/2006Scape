import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SpriteRotatedWindowTest {
    private static final int FILL = 0x204060;
    private int[] savedPixels;
    private int[] saved;

    @Before public void saveDrawingArea() {
        savedPixels = DrawingArea.pixels;
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
