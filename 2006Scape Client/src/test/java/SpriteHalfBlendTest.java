import static org.junit.Assert.*;

import java.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SpriteHalfBlendTest {
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

    private static int[] target(int width, int height, int color) {
        int[] result = new int[width * height];
        Arrays.fill(result, color);
        DrawingArea.initDrawingArea(height, width, result);
        return result;
    }

    @Test public void combinesEqualSourceAndDestinationContributionsWithChannelTruncation() {
        Sprite sprite = new Sprite(1, 1);
        sprite.pixels[0] = 0x804020;
        int[] raster = target(1, 1, 0x204080);
        sprite.drawHalfBlended(0, 0);
        assertEquals(0x504050, raster[0]);
        sprite.pixels[0] = 0x204080;
        raster[0] = 0x804020;
        sprite.drawHalfBlended(0, 0);
        assertEquals(0x504050, raster[0]);
        sprite.pixels[0] = 0x010305;
        raster[0] = 0x020406;
        sprite.drawHalfBlended(0, 0);
        assertEquals(0x010305, raster[0]);
        sprite.pixels[0] = 0xffffff;
        raster[0] = 0xffffff;
        sprite.drawHalfBlended(0, 0);
        assertEquals(0xffffffff, raster[0]);
        assertEquals(0xffffff, sprite.pixels[0]);
    }

    @Test public void skipsOnlyIntegerZeroRatherThanAnAlphaOrLowRgbTest() {
        Sprite sprite = new Sprite(4, 1);
        sprite.pixels = new int[] {0, 0x01000000, 0xff000000, 0x00000001};
        int[] raster = target(4, 1, 0x7f204060);
        sprite.drawHalfBlended(0, 0);
        assertArrayEquals(new int[] {0x7f204060, 0x102030, 0x102030, 0x102030}, raster);
        assertArrayEquals(new int[] {0, 0x01000000, 0xff000000, 1}, sprite.pixels);
    }

    @Test public void firstCoordinateMovesHorizontallyAndSecondMovesVerticallyInPixels() {
        Sprite sprite = new Sprite(1, 1);
        sprite.pixels[0] = 0x204060;
        for (int[] placement : new int[][] {{0, 0, 0}, {1, 0, 1}, {0, 1, 4}, {3, 2, 11}}) {
            int[] raster = target(4, 3, 0);
            sprite.drawHalfBlended(placement[0], placement[1]);
            int[] expected = new int[12];
            expected[placement[2]] = 0x102030;
            assertArrayEquals(expected, raster);
        }
    }

    @Test public void addsSpriteOffsetsBeforeClippingAndKeepsSourceRowsAligned() {
        Sprite sprite = new Sprite(3, 3);
        sprite.offsetX = 1; sprite.offsetY = 1;
        sprite.pixels = new int[] {2, 4, 6, 8, 10, 12, 14, 16, 18};
        int[] raster = target(5, 5, 0);
        DrawingArea.setDrawingArea(4, 2, 4, 2);
        sprite.drawHalfBlended(0, 0);
        int[] expected = new int[25];
        expected[12] = 5; expected[13] = 6;
        expected[17] = 8; expected[18] = 9;
        assertArrayEquals(expected, raster);
        raster = target(5, 5, 0);
        DrawingArea.setDrawingArea(3, 0, 3, 0);
        sprite.drawHalfBlended(0, 0);
        expected = new int[25];
        expected[6] = 1; expected[7] = 2;
        expected[11] = 4; expected[12] = 5;
        assertArrayEquals(expected, raster);
    }

    @Test public void offscreenAndEmptyExtentsLeaveDestinationUntouched() {
        Sprite sprite = new Sprite(1, 1);
        sprite.pixels[0] = 0xffffff;
        for (int[] placement : new int[][] {{-1, 0}, {4, 0}, {0, -1}, {0, 3}}) {
            int[] raster = target(4, 3, 7);
            sprite.drawHalfBlended(placement[0], placement[1]);
            int[] expected = new int[12]; Arrays.fill(expected, 7);
            assertArrayEquals(expected, raster);
        }
        int[] raster = target(4, 3, 7);
        new Sprite(0, 2).drawHalfBlended(0, 0);
        new Sprite(2, 0).drawHalfBlended(0, 0);
        int[] expected = new int[12]; Arrays.fill(expected, 7);
        assertArrayEquals(expected, raster);
    }
}
