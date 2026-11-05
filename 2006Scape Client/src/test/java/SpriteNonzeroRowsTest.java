import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SpriteNonzeroRowsTest {
    private int[] savedPixels;
    private int[] savedBounds;

    @Before
    public void saveDrawingArea() {
        savedPixels = DrawingArea.pixels;
        savedBounds = new int[] {DrawingArea.width, DrawingArea.height, DrawingArea.topX,
                DrawingArea.topY, DrawingArea.bottomX, DrawingArea.bottomY, DrawingArea.scanlineRightLimit,
                DrawingArea.clipRightHalf, DrawingArea.clipBottomHalf};
    }

    @After
    public void restoreDrawingArea() {
        DrawingArea.pixels = savedPixels;
        DrawingArea.width = savedBounds[0]; DrawingArea.height = savedBounds[1];
        DrawingArea.topX = savedBounds[2]; DrawingArea.topY = savedBounds[3];
        DrawingArea.bottomX = savedBounds[4]; DrawingArea.bottomY = savedBounds[5];
        DrawingArea.scanlineRightLimit = savedBounds[6]; DrawingArea.clipRightHalf = savedBounds[7];
        DrawingArea.clipBottomHalf = savedBounds[8];
    }


    private static void copy(int[] destination, int[] source, int sourceIndex,
            int destinationIndex, int width, int rows, int destinationSkip,
            int sourceSkip) throws Exception {
        Method method = Sprite.class.getDeclaredMethod("copyNonzeroRows", int[].class,
                int[].class, int.class, int.class, int.class, int.class, int.class, int.class);
        method.setAccessible(true);
        // Internal sprite storage cannot supply the pixels in these tests.
        method.invoke(new Sprite(0, 0), destination, source, sourceIndex,
                destinationIndex, width, rows, destinationSkip, sourceSkip);
    }

    private static int[] filled(int size) {
        int[] result = new int[size];
        Arrays.fill(result, 9);
        return result;
    }

    @Test
    public void copiesOnlyNonzeroIntegersWithoutBlendingOrMasking() throws Exception {
        int[] source = {0, 1, 0x804020, 0x7f123456, -1, 0x01000000, 0};
        int[] destination = filled(9);
        copy(destination, source, 0, 1, 7, 1, 0, 0);
        assertArrayEquals(new int[] {9,9,1,0x804020,0x7f123456,-1,0x01000000,9,9}, destination);
        assertArrayEquals(new int[] {0,1,0x804020,0x7f123456,-1,0x01000000,0}, source);
        int[] differentBackground = new int[9];
        Arrays.fill(differentBackground, 0x123456);
        copy(differentBackground, source, 0, 1, 7, 1, 0, 0);
        assertArrayEquals(new int[] {0x123456,0x123456,1,0x804020,0x7f123456,-1,
                0x01000000,0x123456,0x123456}, differentBackground);
    }

    @Test
    public void zeroAdvancesBothIndicesInEveryUnrolledSlotAndRemainder() throws Exception {
        for (int zeroIndex = 0; zeroIndex < 9; zeroIndex++) {
            int[] source = {10,11,12,13,14,15,16,17,18};
            source[zeroIndex] = 0;
            int[] destination = filled(11);
            copy(destination, source, 0, 1, 9, 1, 0, 0);
            int[] expected = {9,10,11,12,13,14,15,16,17,18,9};
            expected[zeroIndex + 1] = 9;
            assertArrayEquals("zero index=" + zeroIndex, expected, destination);
        }
        int[] destination = filled(4);
        copy(destination, new int[4], 0, 0, 2, 2, 0, 0);
        assertArrayEquals(new int[] {9,9,9,9}, destination);
    }

    @Test
    public void startIndicesMoveIndependentlyInIntegerElements() throws Exception {
        int[] source = {10,0,12,13,14};
        int[] destination = filled(6);
        copy(destination, source, 0, 1, 2, 1, 0, 0);
        assertArrayEquals(new int[] {9,10,9,9,9,9}, destination);
        destination = filled(6);
        copy(destination, source, 1, 1, 2, 1, 0, 0);
        assertArrayEquals(new int[] {9,9,12,9,9,9}, destination);
        destination = filled(6);
        copy(destination, source, 1, 2, 2, 1, 0, 0);
        assertArrayEquals(new int[] {9,9,9,12,9,9}, destination);
    }

    @Test
    public void skipsAddElementsAfterEachRowAndVaryIndependently() throws Exception {
        int[] source = {10,11,0,13,14,0,16,17,18,19};
        int[] destination = filled(10);
        copy(destination, source, 1, 1, 2, 3, 0, 0);
        assertArrayEquals(new int[] {9,11,9,13,14,9,16,9,9,9}, destination);
        destination = filled(10);
        copy(destination, source, 1, 1, 2, 3, 0, 1);
        assertArrayEquals(new int[] {9,11,9,14,9,17,18,9,9,9}, destination);
        destination = filled(10);
        copy(destination, source, 1, 1, 2, 3, 1, 1);
        assertArrayEquals(new int[] {9,11,9,9,14,9,9,17,18,9}, destination);
        assertArrayEquals(new int[] {10,11,0,13,14,0,16,17,18,19}, source);
    }

    @Test
    public void widthsAndRowCountsBoundWritesIncludingExactArrayEnds() throws Exception {
        int[] source = {10,0,12,13,14,0,16,17,18};
        int[] expected = {9,10,9,12,13,14,9,16,17,18,9};
        for (int width = 0; width <= 9; width++) {
            int[] destination = filled(11);
            copy(destination, source, 0, 1, width, 1, 0, 0);
            int[] bounded = expected.clone();
            Arrays.fill(bounded, 1 + width, bounded.length, 9);
            assertArrayEquals("width=" + width, bounded, destination);
        }
        int[] destination = filled(5);
        copy(destination, source, 0, 0, 2, 0, 0, 0);
        assertArrayEquals(new int[] {9,9,9,9,9}, destination);
        copy(destination, source, 0, 0, 2, 1, 0, 0);
        assertArrayEquals(new int[] {10,9,9,9,9}, destination);
        destination = filled(5);
        copy(destination, source, 0, 0, 2, 2, 0, 0);
        assertArrayEquals(new int[] {10,9,12,13,9}, destination);
        destination = filled(4);
        copy(destination, new int[] {10,0,12,13}, 0, 0, 2, 2, 0, 0);
        assertArrayEquals(new int[] {10,9,12,13}, destination);
        copy(new int[0], new int[0], 0, 0, 0, 3, 0, 0);
    }

    @Test
    public void everyFourPixelBlockAndTailResetsAcrossStridedRows() throws Exception {
        int[] source = new int[48]; Arrays.fill(source, -7);
        for (int row = 0; row < 3; row++) for (int col = 0; col < 12; col++)
            source[2 + row * 15 + col] = (row + col) % 3 == 0 ? 0 : (row + 1) * 100 + col + 1;
        int[] unchanged = source.clone();
        for (int width = 0; width <= 12; width++) for (int rows = 0; rows <= 3; rows++) {
            int[] actual = filled(60), expected = filled(60);
            copy(actual, source, 2, 3, width, rows, 17 - width, 15 - width);
            for (int row = 0; row < rows; row++) for (int col = 0; col < width; col++)
                if ((row + col) % 3 != 0) expected[3 + row * 17 + col] = (row + 1) * 100 + col + 1;
            assertArrayEquals("width=" + width + " rows=" + rows, expected, actual);
            assertArrayEquals(unchanged, source);
        }
        // Exact ends: no extra element is available to hide an overrun in any tail.
        for (int width = 8; width <= 12; width++) {
            int[] exactSource = new int[width * 3], expected = filled(width * 3);
            for (int i = 0; i < exactSource.length; i++) {
                exactSource[i] = i % 4 == 0 ? 0 : i + 100;
                if (i % 4 != 0) expected[i] = i + 100;
            }
            int[] actual = filled(width * 3);
            copy(actual, exactSource, 0, 0, width, 3, 0, 0);
            assertArrayEquals(expected, actual);
        }
    }
    @Test
    public void independentClipDistancesSelectSourceRowsAndColumnsWithDistinctPitches() {
        Sprite sprite = new Sprite(6, 4);
        sprite.pixels = new int[] {101,0,103,104,0,106, 0,202,203,0,205,206,
                301,302,0,304,305,0, 401,0,403,404,0,406};
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
            int[] actual = raster(10, 7);
            DrawingArea.setClip(6, 2, 8, 2);
            sprite.drawSprite(c[0], c[1]);
            int[] expected = new int[70]; Arrays.fill(expected, 9);
            for (int row = 0; row < c[5]; row++) for (int column = 0; column < c[4]; column++) {
                // A labelled grid oracle, independent of the clipping/copy implementation.
                int label = 100 * (c[3] + row + 1) + c[2] + column + 1;
                if (label != 102 && label != 105 && label != 201 && label != 204
                        && label != 303 && label != 306 && label != 402 && label != 405)
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
            int[] actual = raster(10, 7);
            DrawingArea.setClip(6, 2, 8, 2);
            sprite.drawSprite(point[0], point[1]);
            int[] expected = new int[70]; Arrays.fill(expected, 9);
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
                int[] expected = raster(10, 7);
                DrawingArea.setClip(6, 1, 9, 1);
                sprite.drawSprite(point[0] + offset[0], point[1] + offset[1]);
                sprite.offsetX = offset[0]; sprite.offsetY = offset[1];
                int[] actual = raster(10, 7);
                DrawingArea.setClip(6, 1, 9, 1);
                sprite.drawSprite(point[0], point[1]);
                assertArrayEquals(expected, actual);
                assertEquals(offset[0], sprite.offsetX); assertEquals(offset[1], sprite.offsetY);
            }
        }
    }

    private static int[] raster(int width, int height) {
        int[] raster = new int[width * height]; Arrays.fill(raster, 9);
        DrawingArea.bindRaster(height, width, raster);
        return raster;
    }
}
