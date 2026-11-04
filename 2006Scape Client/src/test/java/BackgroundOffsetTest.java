import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Base64;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BackgroundOffsetTest {
    private static final int RED = 0xff0000, GREEN = 0x00ff00, FILL = 0x204060;
    private int[] previousPixels;
    private int previousWidth, previousHeight, previousTopX, previousTopY;
    private int previousBottomX, previousBottomY, previousCenterX, previousCenterY, previous1387;

    @Before
    public void isolateRaster() {
        previousPixels = DrawingArea.pixels;
        previousWidth = DrawingArea.width;
        previousHeight = DrawingArea.height;
        previousTopX = DrawingArea.topX;
        previousTopY = DrawingArea.topY;
        previousBottomX = DrawingArea.bottomX;
        previousBottomY = DrawingArea.bottomY;
        previousCenterX = DrawingArea.centerX;
        previousCenterY = DrawingArea.centerY;
        previous1387 = DrawingArea.anInt1387;
    }

    @After
    public void restoreRaster() {
        DrawingArea.pixels = previousPixels;
        DrawingArea.width = previousWidth;
        DrawingArea.height = previousHeight;
        DrawingArea.topX = previousTopX;
        DrawingArea.topY = previousTopY;
        DrawingArea.bottomX = previousBottomX;
        DrawingArea.bottomY = previousBottomY;
        DrawingArea.centerX = previousCenterX;
        DrawingArea.centerY = previousCenterY;
        DrawingArea.anInt1387 = previous1387;
    }

    @Test
    public void archiveStoresUnsignedHorizontalThenVerticalPixelOffsets() {
        Background first = new Background(archive(), "offset", 0);
        assertEquals(2, first.offsetX);
        assertEquals(3, first.offsetY);
        assertEquals(2, first.anInt1452);
        assertEquals(3, first.anInt1453);
        assertEquals(260, first.anInt1456);
        assertArrayEquals(new byte[] {1, 0, 2, 1, 0, 2}, first.aByteArray1450);
        int[] actual = raster(6, 7, FILL);
        first.method361(0, 0);
        int[] expected = filled(42, FILL);
        expected[3 * 6 + 2] = RED;   // (2,3)
        expected[4 * 6 + 2] = GREEN; // (2,4)
        expected[4 * 6 + 3] = RED;   // (3,4)
        expected[5 * 6 + 3] = GREEN; // (3,5); zero palette indices keep the fill.
        assertArrayEquals(expected, actual);

        Background second = new Background(archive(), "offset", 1);
        assertEquals(255, second.offsetX);
        assertEquals(254, second.offsetY);
        int[] large = raster(260, 258, 0);
        second.method361(0, 0);
        int[] largeExpected = new int[260 * 258];
        largeExpected[66295] = GREEN; // Pixel (255,254): unsigned bytes, x before y.
        assertArrayEquals(largeExpected, large);
    }

    @Test
    public void eachOffsetMovesOnlyItsOwnAxisWhenDrawing() {
        for (int[] offset : new int[][] {{0, 0}, {1, 0}, {0, 2}, {1, 2}}) {
            Background background = single(offset[0], offset[1]);
            int[] actual = raster(7, 7, FILL);
            background.method361(2, 1);
            int[] expected = filled(49, FILL);
            expected[(1 + offset[1]) * 7 + 2 + offset[0]] = RED;
            assertArrayEquals(Arrays.toString(offset), expected, actual);
        }
    }

    @Test
    public void offsetsAreAppliedBeforeClippingAtEachRasterBoundary() {
        Background background = single(2, 3);
        int[][] positions = {{0,0}, {4,0}, {0,4}, {4,4}, {-1,0}, {5,0}, {0,-1}, {0,5}};
        for (int[] position : positions) {
            int[] actual = raster(5, 5, 0);
            background.method361(position[0] - 2, position[1] - 3);
            int[] expected = new int[25];
            if (position[0] >= 0 && position[0] < 5 && position[1] >= 0 && position[1] < 5) {
                expected[position[1] * 5 + position[0]] = RED;
            }
            assertArrayEquals(Arrays.toString(position), expected, actual);
        }
    }

    @Test
    public void expansionPlacesPixelsAtOffsetsWithinCanvasAndResetsThem() {
        Background background = new Background(archive(), "small", 0);
        assertEquals(2, background.offsetX);
        assertEquals(3, background.offsetY);
        background.method357();
        assertArrayEquals(new byte[] {0,0,0,0,0, 0,0,0,0,0, 0,0,0,0,0,
                0,0,1,2,0, 0,0,3,4,0, 0,0,0,0,0}, background.aByteArray1450);
        assertEquals(5, background.anInt1452);
        assertEquals(6, background.anInt1453);
        assertEquals(0, background.offsetX);
        assertEquals(0, background.offsetY);

        Background fullSize = new Background(archive(), "small", 0);
        fullSize.anInt1452 = 5;
        fullSize.anInt1453 = 6;
        fullSize.method357(); // Already canvas-sized: returns before touching offsets.
        assertEquals(2, fullSize.offsetX);
        assertEquals(3, fullSize.offsetY);
    }

    @Test
    public void halfScaleExpansionHalvesOffsetPositionsAndResetsThem() {
        Background background = new Background(archive(), "small", 0);
        background.method356();
        // Source (k,j) lands at ((k + 2) >> 1, (j + 3) >> 1) in a 2x3 canvas; later writes win.
        assertArrayEquals(new byte[] {0,0, 0,2, 0,4}, background.aByteArray1450);
        assertEquals(2, background.anInt1452);
        assertEquals(3, background.anInt1453);
        assertEquals(2, background.anInt1456);
        assertEquals(0, background.offsetX);
        assertEquals(0, background.offsetY);
    }

    @Test
    public void horizontalMirrorReflectsOnlyTheHorizontalOffsetWithinCanvas() {
        Background original = new Background(archive(), "small", 0);
        original.method357();
        Background mirrored = new Background(archive(), "small", 0);
        mirrored.flipHorizontally();
        assertEquals(1, mirrored.offsetX); // canvas 5 - width 2 - offset 2
        assertEquals(3, mirrored.offsetY);
        assertArrayEquals(new byte[] {2, 1, 4, 3}, mirrored.aByteArray1450);
        mirrored.method357();
        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 5; x++) {
                assertEquals(original.aByteArray1450[y * 5 + 4 - x], mirrored.aByteArray1450[y * 5 + x]);
            }
        }
    }

    @Test
    public void verticalMirrorReflectsOnlyTheVerticalOffsetWithinCanvas() {
        Background original = new Background(archive(), "small", 0);
        original.method357();
        Background mirrored = new Background(archive(), "small", 0);
        mirrored.flipVertically();
        assertEquals(2, mirrored.offsetX);
        assertEquals(1, mirrored.offsetY); // canvas 6 - height 2 - offset 3
        assertArrayEquals(new byte[] {3, 4, 1, 2}, mirrored.aByteArray1450);
        mirrored.method357();
        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 5; x++) {
                assertEquals(original.aByteArray1450[(5 - y) * 5 + x], mirrored.aByteArray1450[y * 5 + x]);
            }
        }
    }

    @Test
    public void titleFlameMaskClearsCellsAtOffsetPixelPositions() throws Exception {
        Game game = (Game) unsafe().allocateInstance(Game.class); // Avoids applet/window construction.
        for (int[] offset : new int[][] {{2, 3}, {0, 0}, {5, 0}, {0, 7}}) {
            game.anIntArray1190 = new int[32768];
            game.anIntArray1191 = new int[32768];
            Background background = new Background(archive(), "small", 0);
            background.offsetX = offset[0];
            background.offsetY = offset[1];
            game.randomizeBackground(background);
            for (int row = 0; row < 2; row++) {
                for (int col = 0; col < 2; col++) {
                    int x = col + 16 + offset[0], y = row + 16 + offset[1];
                    assertEquals(Arrays.toString(offset) + " cell " + x + "," + y, 0, game.anIntArray1190[x + (y << 7)]);
                }
            }
        }
        // Counterexample: with offsets (2,3), the no-offset cells are not forced to zero.
        game.anIntArray1190 = new int[32768];
        game.anIntArray1191 = new int[32768];
        game.randomizeBackground(new Background(archive(), "small", 0));
        int nonzero = 0;
        for (int row = 0; row < 2; row++) {
            for (int col = 0; col < 2; col++) {
                if (game.anIntArray1190[col + 16 + ((row + 16) << 7)] != 0) nonzero++;
            }
        }
        assertTrue("smoothed random flame field should be nonzero away from the mask", nonzero > 0);
    }

    private static Background single(int x, int y) {
        Background background = new Background(archive(), "small", 0);
        background.anInt1452 = 1;
        background.anInt1453 = 1;
        background.aByteArray1450 = new byte[] {1};
        background.offsetX = x;
        background.offsetY = y;
        return background;
    }

    private static int[] raster(int width, int height, int fill) {
        int[] pixels = filled(width * height, fill);
        DrawingArea.initDrawingArea(height, width, pixels);
        return pixels;
    }

    private static int[] filled(int length, int fill) {
        int[] pixels = new int[length];
        Arrays.fill(pixels, fill);
        return pixels;
    }

    private static sun.misc.Unsafe unsafe() throws Exception {
        Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (sun.misc.Unsafe) field.get(null);
    }

    private static StreamLoader archive() {
        // Independent archive (Make-Fixture.py): "offset" 260x258 canvas, 2x3 at (2,3) and 1x1 at (255,254);
        // "small" 5x6 canvas, 2x2 palette indices [1,2/3,4] at (2,3).
        return new StreamLoader(Base64.getDecoder().decode(
                "AABgAABtMUFZJlNZKILEYgAADWtV/6QQICAAAAJAABAKAQAAQAAAgAGgAFCgAxGmmjQaaII2kD0j1FlFbrbt36ul5XDigcNmqcEMRoi5Aby3NEoQNwoUk2ZqFPSM/KkamMVmYitWIIP4u5IpwoSBRBYjEA=="));
    }
}
