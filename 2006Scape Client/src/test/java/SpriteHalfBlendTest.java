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
    @Test
    public void independentClipDistancesSelectSourceRowsAndColumnsWithDistinctPitches() {
        Sprite sprite = new Sprite(6, 4);
        sprite.pixels = new int[] {2,0,6,8,0,12, 0,16,18,0,22,24,
                26,28,0,32,34,0, 38,0,42,44,0,48};
        int[] source = sprite.pixels.clone();
        // x, y, first visible source column/row, visible columns/rows, target x/y.
        // Each pair changes only placement along one axis against [2,8) x [2,6).
        int[][] cases = {
            {2,2, 0,0,6,4, 2,2},
            {2,1, 0,1,6,3, 2,2}, {2,0, 0,2,6,2, 2,2},
            {2,3, 0,0,6,3, 2,3}, {2,4, 0,0,6,2, 2,4},
            {1,2, 1,0,5,4, 2,2}, {0,2, 2,0,4,4, 2,2},
            {3,2, 0,0,5,4, 3,2}, {4,2, 0,0,4,4, 4,2},
            {0,0, 2,2,4,2, 2,2}, {4,4, 0,0,4,2, 4,4},
            {2,-1, 0,3,6,1, 2,2}, {2,5, 0,0,6,1, 2,5},
            {-3,2, 5,0,1,4, 2,2}, {7,2, 0,0,1,4, 7,2}
        };
        for (int[] c : cases) {
            int[] actual = target(10, 7, 0x7f000000);
            DrawingArea.setDrawingArea(6, 2, 8, 2);
            sprite.drawHalfBlended(c[0], c[1]);
            int[] expected = new int[70]; Arrays.fill(expected, 0x7f000000);
            for (int row = 0; row < c[5]; row++) for (int column = 0; column < c[4]; column++) {
                // A labelled grid oracle, independent of the clipping/copy implementation.
                int label = 6 * (c[3] + row) + c[2] + column + 1;
                if (label != 2 && label != 5 && label != 7 && label != 10
                        && label != 15 && label != 18 && label != 20 && label != 23)
                    expected[(c[7] + row) * 10 + c[6] + column] = label;
            }
            assertArrayEquals("placement=" + c[0] + "," + c[1], expected, actual);
            assertArrayEquals(source, sprite.pixels);
            assertArrayEquals(new int[] {6,4,6,4,0,0}, new int[] {sprite.width,
                    sprite.height,sprite.trimWidth,sprite.trimHeight,sprite.offsetX,sprite.offsetY});
            assertEquals(2, DrawingArea.topX); assertEquals(2, DrawingArea.topY);
            assertEquals(8, DrawingArea.bottomX); assertEquals(6, DrawingArea.bottomY);
        }
        // Exactly touching each clip edge has no intersection; one-pixel intersections above do.
        for (int[] point : new int[][] {{-4,2}, {8,2}, {2,-2}, {2,6}}) {
            int[] actual = target(10, 7, 0x7f000000);
            DrawingArea.setDrawingArea(6, 2, 8, 2);
            sprite.drawHalfBlended(point[0], point[1]);
            int[] expected = new int[70]; Arrays.fill(expected, 0x7f000000);
            assertArrayEquals(expected, actual);
        }
    }

    @Test
    public void offsetPlacementEqualsTranslatedCoordinatesEvenAtNegativeOrigins() {
        Sprite sprite = new Sprite(6, 4);
        for (int i = 0; i < sprite.pixels.length; i++) sprite.pixels[i] = i % 3 == 0 ? 0 : i + 1;
        for (int[] offset : new int[][] {{0,0}, {2,0}, {0,1}, {2,1}, {-2,-1}}) {
            for (int[] point : new int[][] {{-2,-1}, {0,0}, {3,3}, {7,5}}) {
                sprite.offsetX = 0; sprite.offsetY = 0;
                int[] expected = target(10, 7, 0x7f000000);
                DrawingArea.setDrawingArea(6, 1, 9, 1);
                sprite.drawHalfBlended(point[0] + offset[0], point[1] + offset[1]);
                sprite.offsetX = offset[0]; sprite.offsetY = offset[1];
                int[] actual = target(10, 7, 0x7f000000);
                DrawingArea.setDrawingArea(6, 1, 9, 1);
                sprite.drawHalfBlended(point[0], point[1]);
                assertArrayEquals(expected, actual);
                assertEquals(offset[0], sprite.offsetX); assertEquals(offset[1], sprite.offsetY);
            }
        }
    }

}
