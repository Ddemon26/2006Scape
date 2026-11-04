import static org.junit.Assert.assertArrayEquals;

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
}
