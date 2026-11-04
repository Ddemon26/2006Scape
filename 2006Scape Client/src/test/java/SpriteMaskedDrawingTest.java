import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Base64;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SpriteMaskedDrawingTest {
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

    @Test public void firstCoordinateIsVerticalAndSecondIsHorizontalInTargetPixels() {
        Sprite sprite = sprite(1, 1, 0x7f123456);
        Background mask = mask(12);
        for (int[] placement : new int[][] {{0,0,0}, {1,0,4}, {0,1,1}, {2,3,11}}) {
            int[] raster = target(4, 3);
            sprite.drawNonzeroThroughZeroMask(mask, placement[0], placement[1]);
            int[] expected = filled(12);
            expected[placement[2]] = 0x7f123456;
            assertArrayEquals(expected, raster);
        }
    }

    @Test public void spriteOffsetsIndependentlyShiftPlacementBeforeClipping() {
        Sprite sprite = sprite(1, 1, 9);
        for (int[] offset : new int[][] {{0,0,6}, {1,0,7}, {0,1,11}, {1,1,12}}) {
            sprite.offsetX = offset[0]; sprite.offsetY = offset[1];
            int[] raster = target(5, 4);
            sprite.drawNonzeroThroughZeroMask(mask(20), 1, 1);
            int[] expected = filled(20); expected[offset[2]] = 9;
            assertArrayEquals(expected, raster);
        }
        sprite.offsetX = 2; sprite.offsetY = 1;
        int[] raster = target(5, 4);
        sprite.drawNonzeroThroughZeroMask(mask(20), -1, -2);
        int[] expected = filled(20); expected[0] = 9;
        assertArrayEquals(expected, raster);
    }

    @Test public void copiesExactNonzeroIntegersOnlyAtZeroDestinationMaskBytes() {
        Sprite sprite = sprite(7, 1, 0, 0x01000000, -1, 0x7f123456, 5, 6, 7);
        Background mask = mask(7);
        mask.aByteArray1450 = new byte[] {0, 0, 0, 0, 1, -1, -128};
        byte[] originalMask = mask.aByteArray1450.clone();
        int[] originalSource = sprite.pixels.clone();
        int[] raster = target(7, 1);
        sprite.drawNonzeroThroughZeroMask(mask, 0, 0);
        assertArrayEquals(new int[] {FILL, 0x01000000, -1, 0x7f123456, FILL, FILL, FILL}, raster);
        assertArrayEquals(originalSource, sprite.pixels);
        assertArrayEquals(originalMask, mask.aByteArray1450);
        // Change only the mask: previously allowed pixels block and blocked pixels copy.
        mask.aByteArray1450 = new byte[] {1, 1, -1, 127, 0, 0, 0};
        raster = target(7, 1);
        sprite.drawNonzeroThroughZeroMask(mask, 0, 0);
        assertArrayEquals(new int[] {FILL, FILL, FILL, FILL, 5, 6, 7}, raster);
    }

    @Test public void maskUsesTargetIndicesAndIgnoresBackgroundPlacementAndPalette() {
        Sprite sprite = sprite(2, 1, 11, 22);
        Background mask = mask(12);
        mask.aByteArray1450[0] = 1; // Source-local index would block the wrong pixel.
        mask.aByteArray1450[6] = -1;
        int[] raster = target(4, 3);
        sprite.drawNonzeroThroughZeroMask(mask, 1, 1);
        int[] expected = filled(12); expected[5] = 11;
        assertArrayEquals(expected, raster);
        mask.offsetX = 99; mask.offsetY = 88;
        mask.anInt1452 = 1; mask.anInt1453 = 1;
        mask.anInt1456 = 2;
        Arrays.fill(mask.anIntArray1451, 0xffffff);
        raster = target(4, 3);
        sprite.drawNonzeroThroughZeroMask(mask, 1, 1);
        assertArrayEquals(expected, raster);
        assertArrayEquals(new int[] {11, 22}, sprite.pixels);
    }

    @Test public void clipsEachEdgeWithCorrectSourceRowsAndTargetMaskAlignment() {
        Sprite sprite = sprite(3, 3, 1,2,3, 4,5,6, 7,8,9);
        sprite.offsetX = 1; sprite.offsetY = 1;
        Background mask = mask(25);
        mask.aByteArray1450[13] = 1;
        int[] raster = target(5, 5);
        DrawingArea.setDrawingArea(4, 2, 4, 2);
        sprite.drawNonzeroThroughZeroMask(mask, 0, 0);
        int[] expected = filled(25);
        expected[12] = 5; expected[17] = 8; expected[18] = 9;
        assertArrayEquals(expected, raster);
        raster = target(5, 5);
        DrawingArea.setDrawingArea(3, 0, 3, 0);
        sprite.drawNonzeroThroughZeroMask(mask, 0, 0);
        expected = filled(25);
        expected[6] = 1; expected[7] = 2; expected[11] = 4; expected[12] = 5;
        assertArrayEquals(expected, raster);
        assertArrayEquals(new int[] {1,2,3,4,5,6,7,8,9}, sprite.pixels);
    }

    @Test public void exactEdgesOffscreenEmptySpritesAndEmptyClipBoundWrites() {
        Sprite sprite = sprite(1, 1, 9);
        Background mask = mask(12);
        for (int[] placement : new int[][] {{0,0,0}, {2,3,11}, {0,-1,-1}, {0,4,-1}, {-1,0,-1}, {3,0,-1}}) {
            int[] raster = target(4, 3);
            sprite.drawNonzeroThroughZeroMask(mask, placement[0], placement[1]);
            int[] expected = filled(12);
            if (placement[2] >= 0) expected[placement[2]] = 9;
            assertArrayEquals(expected, raster);
        }
        int[] raster = target(4, 3);
        new Sprite(0, 2).drawNonzeroThroughZeroMask(mask, 0, 0);
        new Sprite(2, 0).drawNonzeroThroughZeroMask(mask, 0, 0);
        DrawingArea.setDrawingArea(1, 1, 1, 1);
        sprite.drawNonzeroThroughZeroMask(mask, 1, 1);
        assertArrayEquals(filled(12), raster);
    }

    private static Sprite sprite(int width, int height, int... pixels) {
        Sprite sprite = new Sprite(width, height);
        sprite.pixels = pixels;
        return sprite;
    }

    private static Background mask(int size) {
        // Existing independent archive fixture; replace only its byte raster for mask tests.
        StreamLoader archive = new StreamLoader(Base64.getDecoder().decode(
                "AABgAABtMUFZJlNZKILEYgAADWtV/6QQICAAAAJAABAKAQAAQAAAgAGgAFCgAxGmmjQaaII2kD0j1FlFbrbt36ul5XDigcNmqcEMRoi5Aby3NEoQNwoUk2ZqFPSM/KkamMVmYitWIIP4u5IpwoSBRBYjEA=="));
        Background background = new Background(archive, "small", 0);
        background.aByteArray1450 = new byte[size];
        return background;
    }

    private static int[] target(int width, int height) {
        int[] raster = filled(width * height);
        DrawingArea.initDrawingArea(height, width, raster);
        return raster;
    }

    private static int[] filled(int size) {
        int[] pixels = new int[size]; Arrays.fill(pixels, FILL); return pixels;
    }
    @Test
    public void independentClipDistancesSelectSourceRowsAndColumnsWithDistinctPitches() {
        Sprite sprite = new Sprite(6, 4);
        sprite.pixels = new int[] {101,0,103,104,0,106, 0,202,203,0,205,206,
                301,302,0,304,305,0, 401,0,403,404,0,406};
        int[] source = sprite.pixels.clone();
        Background gate = mask(70);
        for (int i = 0; i < 70; i++) gate.aByteArray1450[i] = (byte) (i % 5 == 0 ? -128 : 0);
        byte[] unchangedMask = gate.aByteArray1450.clone();
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
            int[] actual = target(10, 7);
            DrawingArea.setDrawingArea(6, 2, 8, 2);
            sprite.drawNonzeroThroughZeroMask(gate, c[1], c[0]);
            int[] expected = new int[70]; Arrays.fill(expected, FILL);
            for (int row = 0; row < c[5]; row++) for (int column = 0; column < c[4]; column++) {
                // A labelled grid oracle, independent of the clipping/copy implementation.
                int label = 100 * (c[3] + row + 1) + c[2] + column + 1;
                if (label != 102 && label != 105 && label != 201 && label != 204
                        && label != 303 && label != 306 && label != 402 && label != 405
                        && ((c[7] + row) * 10 + c[6] + column) % 5 != 0)
                    expected[(c[7] + row) * 10 + c[6] + column] = label;
            }
            assertArrayEquals("placement=" + c[0] + "," + c[1], expected, actual);
            assertArrayEquals(source, sprite.pixels);
            assertArrayEquals(unchangedMask, gate.aByteArray1450);
            assertArrayEquals(new int[] {6,4,6,4,0,0}, new int[] {sprite.width,
                    sprite.height,sprite.trimWidth,sprite.trimHeight,sprite.offsetX,sprite.offsetY});
            assertEquals(2, DrawingArea.topX); assertEquals(2, DrawingArea.topY);
            assertEquals(8, DrawingArea.bottomX); assertEquals(6, DrawingArea.bottomY);
        }
        // Exactly touching each clip edge has no intersection; one-pixel intersections above do.
        for (int[] point : new int[][] {{-4,2}, {8,2}, {2,-2}, {2,6}}) {
            int[] actual = target(10, 7);
            DrawingArea.setDrawingArea(6, 2, 8, 2);
            sprite.drawNonzeroThroughZeroMask(gate, point[1], point[0]);
            int[] expected = new int[70]; Arrays.fill(expected, FILL);
            assertArrayEquals(expected, actual);
        }
    }

    @Test
    public void offsetPlacementEqualsTranslatedCoordinatesEvenAtNegativeOrigins() {
        Sprite sprite = new Sprite(6, 4);
        for (int i = 0; i < sprite.pixels.length; i++) sprite.pixels[i] = i % 3 == 0 ? 0 : i + 1;
        Background gate = mask(70);
        for (int i = 0; i < 70; i++) gate.aByteArray1450[i] = (byte) (i % 4 == 1 ? 127 : 0);
        for (int[] offset : new int[][] {{0,0}, {2,0}, {0,1}, {2,1}, {-2,-1}}) {
            for (int[] point : new int[][] {{-2,-1}, {0,0}, {3,3}, {7,5}}) {
                sprite.offsetX = 0; sprite.offsetY = 0;
                int[] expected = target(10, 7);
                DrawingArea.setDrawingArea(6, 1, 9, 1);
                sprite.drawNonzeroThroughZeroMask(gate, point[1] + offset[1], point[0] + offset[0]);
                sprite.offsetX = offset[0]; sprite.offsetY = offset[1];
                int[] actual = target(10, 7);
                DrawingArea.setDrawingArea(6, 1, 9, 1);
                sprite.drawNonzeroThroughZeroMask(gate, point[1], point[0]);
                assertArrayEquals(expected, actual);
                assertEquals(offset[0], sprite.offsetX); assertEquals(offset[1], sprite.offsetY);
            }
        }
    }

}
