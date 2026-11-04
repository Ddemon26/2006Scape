import static org.junit.Assert.*;

import java.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SpriteOpaqueDrawingTest {
    private int[] savedPixels;
    private int[] savedBounds;

    @Before
    public void saveDrawingArea() {
        savedPixels = DrawingArea.pixels;
        savedBounds = new int[] {DrawingArea.width, DrawingArea.height, DrawingArea.topX,
                DrawingArea.topY, DrawingArea.bottomX, DrawingArea.bottomY, DrawingArea.centerX,
                DrawingArea.centerY, DrawingArea.anInt1387};
    }

    @After
    public void restoreDrawingArea() {
        DrawingArea.pixels = savedPixels;
        DrawingArea.width = savedBounds[0]; DrawingArea.height = savedBounds[1];
        DrawingArea.topX = savedBounds[2]; DrawingArea.topY = savedBounds[3];
        DrawingArea.bottomX = savedBounds[4]; DrawingArea.bottomY = savedBounds[5];
        DrawingArea.centerX = savedBounds[6]; DrawingArea.centerY = savedBounds[7];
        DrawingArea.anInt1387 = savedBounds[8];
    }

    @Test
    public void copiesRawPixelsIncludingZeroInsteadOfSkippingOrBlending() {
        Sprite sprite = new Sprite(3, 1);
        sprite.pixels = new int[] {0, 0x804020, 0x7f123456};
        int[] raster = raster(5, 2);
        sprite.drawOpaque(1, 1);
        assertArrayEquals(new int[] {9,9,9,9,9, 9,0,0x804020,0x7f123456,9}, raster);
        assertArrayEquals(new int[] {0, 0x804020, 0x7f123456}, sprite.pixels);

        raster = raster(5, 2);
        sprite.drawSprite(1, 1);
        assertEquals("transparent path keeps the destination where source is zero", 9, raster[6]);
    }

    @Test
    public void addsOffsetsToHorizontalThenVerticalDestinationCoordinates() {
        Sprite sprite = new Sprite(1, 1);
        sprite.pixels[0] = 42;
        for (int[] offset : new int[][] {{0,0}, {2,0}, {0,1}, {2,1}}) {
            sprite.offsetX = offset[0]; sprite.offsetY = offset[1];
            int[] actual = raster(6, 4);
            sprite.drawOpaque(1, 1);
            int[] expected = new int[24]; Arrays.fill(expected, 9);
            expected[(1 + offset[1]) * 6 + 1 + offset[0]] = 42;
            assertArrayEquals(expected, actual);
        }
    }

    @Test
    public void clipsAllEdgesToTheActiveRectangleWithoutChangingSource() {
        Sprite sprite = new Sprite(4, 3);
        sprite.pixels = new int[] {1,2,3,4, 5,0,7,8, 9,10,11,12};
        int[] actual = raster(5, 5);
        DrawingArea.setDrawingArea(3, 1, 3, 1); // x in [1,3), y in [1,3).
        sprite.drawOpaque(0, 0);
        assertArrayEquals(new int[] {9,9,9,9,9, 9,0,7,9,9, 9,10,11,9,9,
                9,9,9,9,9, 9,9,9,9,9}, actual);
        assertArrayEquals(new int[] {1,2,3,4, 5,0,7,8, 9,10,11,12}, sprite.pixels);
    }

    @Test
    public void handlesCopyLoopBoundariesAndFullyClippedOrEmptySprites() {
        for (int width = 1; width <= 5; width++) {
            Sprite sprite = new Sprite(width, 2);
            Arrays.fill(sprite.pixels, 42);
            int[] actual = raster(7, 4);
            sprite.drawOpaque(1, 1);
            for (int y = 0; y < 4; y++) for (int x = 0; x < 7; x++) {
                assertEquals(x >= 1 && x <= width && y >= 1 && y <= 2 ? 42 : 9, actual[y * 7 + x]);
            }
        }
        Sprite sprite = new Sprite(1, 1);
        for (int[] position : new int[][] {{-1,0}, {3,0}, {0,-1}, {0,3}}) {
            int[] actual = raster(3, 3);
            sprite.drawOpaque(position[0], position[1]);
            int[] expected = new int[9]; Arrays.fill(expected, 9);
            assertArrayEquals(expected, actual);
        }
        int[] actual = raster(3, 3);
        new Sprite(0, 0).drawOpaque(0, 0);
        assertEquals(9, actual[0]);
    }

    private static int[] raster(int width, int height) {
        int[] raster = new int[width * height]; Arrays.fill(raster, 9);
        DrawingArea.initDrawingArea(height, width, raster);
        return raster;
    }
}
