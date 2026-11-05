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
        assertEquals(2, first.imageWidth);
        assertEquals(3, first.imageHeight);
        assertEquals(260, first.canvasWidth);
        assertArrayEquals(new byte[] {1, 0, 2, 1, 0, 2}, first.paletteIndices);
        int[] actual = raster(6, 7, FILL);
        first.drawNonzeroIndexed(0, 0);
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
        second.drawNonzeroIndexed(0, 0);
        int[] largeExpected = new int[260 * 258];
        largeExpected[66295] = GREEN; // Pixel (255,254): unsigned bytes, x before y.
        assertArrayEquals(largeExpected, large);
    }

    @Test
    public void eachOffsetMovesOnlyItsOwnAxisWhenDrawing() {
        for (int[] offset : new int[][] {{0, 0}, {1, 0}, {0, 2}, {1, 2}}) {
            Background background = single(offset[0], offset[1]);
            int[] actual = raster(7, 7, FILL);
            background.drawNonzeroIndexed(2, 1);
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
            background.drawNonzeroIndexed(position[0] - 2, position[1] - 3);
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
        background.expandToCanvas();
        assertArrayEquals(new byte[] {0,0,0,0,0, 0,0,0,0,0, 0,0,0,0,0,
                0,0,1,2,0, 0,0,3,4,0, 0,0,0,0,0}, background.paletteIndices);
        assertEquals(5, background.imageWidth);
        assertEquals(6, background.imageHeight);
        assertEquals(0, background.offsetX);
        assertEquals(0, background.offsetY);

        Background fullSize = new Background(archive(), "small", 0);
        fullSize.imageWidth = 5;
        fullSize.imageHeight = 6;
        fullSize.expandToCanvas(); // Already canvas-sized: returns before touching offsets.
        assertEquals(2, fullSize.offsetX);
        assertEquals(3, fullSize.offsetY);
    }

    @Test
    public void halfScaleExpansionHalvesOffsetPositionsAndResetsThem() {
        Background background = new Background(archive(), "small", 0);
        background.reduceToHalfCanvas();
        // Source (k,j) lands at ((k + 2) >> 1, (j + 3) >> 1) in a 2x3 canvas; later writes win.
        assertArrayEquals(new byte[] {0,0, 0,2, 0,4}, background.paletteIndices);
        assertEquals(2, background.imageWidth);
        assertEquals(3, background.imageHeight);
        assertEquals(2, background.canvasWidth);
        assertEquals(0, background.offsetX);
        assertEquals(0, background.offsetY);
    }

    @Test
    public void horizontalMirrorReflectsOnlyTheHorizontalOffsetWithinCanvas() {
        Background original = new Background(archive(), "small", 0);
        original.expandToCanvas();
        Background mirrored = new Background(archive(), "small", 0);
        mirrored.flipHorizontally();
        assertEquals(1, mirrored.offsetX); // canvas 5 - width 2 - offset 2
        assertEquals(3, mirrored.offsetY);
        assertArrayEquals(new byte[] {2, 1, 4, 3}, mirrored.paletteIndices);
        mirrored.expandToCanvas();
        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 5; x++) {
                assertEquals(original.paletteIndices[y * 5 + 4 - x], mirrored.paletteIndices[y * 5 + x]);
            }
        }
    }

    @Test
    public void verticalMirrorReflectsOnlyTheVerticalOffsetWithinCanvas() {
        Background original = new Background(archive(), "small", 0);
        original.expandToCanvas();
        Background mirrored = new Background(archive(), "small", 0);
        mirrored.flipVertically();
        assertEquals(2, mirrored.offsetX);
        assertEquals(1, mirrored.offsetY); // canvas 6 - height 2 - offset 3
        assertArrayEquals(new byte[] {3, 4, 1, 2}, mirrored.paletteIndices);
        mirrored.expandToCanvas();
        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 5; x++) {
                assertEquals(original.paletteIndices[(5 - y) * 5 + x], mirrored.paletteIndices[y * 5 + x]);
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

    @Test
    public void fullAndHalfCanvasRemappingPreserveIndicesAndChooseLastRowMajorWriter() throws Exception {
        for (int width = 0; width <= 4; width++) for (int height = 0; height <= 4; height++)
            for (int x = 0; x <= 3; x++) for (int y = 0; y <= 3; y++) {
                Background full = canvasFixture(width, height, x, y, 8, 8);
                Background half = canvasFixture(width, height, x, y, 8, 8);
                byte[] source = full.paletteIndices, halfSource = half.paletteIndices;
                byte[] unchanged = source.clone();
                int[] palette = full.paletteRgb, halfPalette = half.paletteRgb;
                byte[] expanded = new byte[64], reduced = new byte[16];
                for (int dy = 0; dy < 8; dy++) for (int dx = 0; dx < 8; dx++)
                    if (dx >= x && dx < x + width && dy >= y && dy < y + height)
                        expanded[dy * 8 + dx] = source[(dy - y) * width + dx - x];
                // Inverse block oracle: the bottommost, then rightmost intersecting source pixel wins.
                // Zero indices also overwrite; this is neither averaging nor "last nonzero" sampling.
                for (int dy = 0; dy < 4; dy++) for (int dx = 0; dx < 4; dx++) {
                    int right = Math.min(dx * 2 + 1, x + width - 1);
                    int bottom = Math.min(dy * 2 + 1, y + height - 1);
                    if (width > 0 && height > 0 && right >= Math.max(dx * 2, x)
                            && bottom >= Math.max(dy * 2, y))
                        reduced[dy * 4 + dx] = source[(bottom - y) * width + right - x];
                }
                full.expandToCanvas(); half.reduceToHalfCanvas();
                assertArrayEquals(expanded, full.paletteIndices);
                assertArrayEquals(reduced, half.paletteIndices);
                assertArrayEquals(unchanged, source); assertArrayEquals(unchanged, halfSource);
                org.junit.Assert.assertNotSame(source, full.paletteIndices);
                org.junit.Assert.assertNotSame(halfSource, half.paletteIndices);
                org.junit.Assert.assertSame(palette, full.paletteRgb);
                org.junit.Assert.assertSame(halfPalette, half.paletteRgb);
                assertEquals(8, full.imageWidth); assertEquals(8, full.imageHeight);
                assertEquals(8, full.canvasWidth); assertEquals(8, canvasHeight(full));
                assertEquals(4, half.imageWidth); assertEquals(4, half.imageHeight);
                assertEquals(4, half.canvasWidth); assertEquals(4, canvasHeight(half));
                assertEquals(0, full.offsetX); assertEquals(0, full.offsetY);
                assertEquals(0, half.offsetX); assertEquals(0, half.offsetY);
            }
    }

    @Test
    public void fullCanvasEarlyReturnRequiresBothDimensionsAndKeepsArrayAndOffsets() throws Exception {
        Background full = canvasFixture(8, 8, 2, 3, 8, 8);
        byte[] source = full.paletteIndices;
        full.expandToCanvas();
        org.junit.Assert.assertSame(source, full.paletteIndices);
        assertEquals(2, full.offsetX); assertEquals(3, full.offsetY);
        for (int[] size : new int[][] {{8, 3}, {3, 8}, {0, 0}}) {
            Background partial = canvasFixture(size[0], size[1], 0, 0, 8, 8);
            byte[] before = partial.paletteIndices;
            partial.expandToCanvas();
            org.junit.Assert.assertNotSame(before, partial.paletteIndices);
            assertEquals(64, partial.paletteIndices.length);
            assertEquals(8, partial.imageWidth); assertEquals(8, partial.imageHeight);
        }
        Background empty = canvasFixture(0, 0, 0, 0, 0, 0);
        byte[] before = empty.paletteIndices;
        empty.expandToCanvas();
        org.junit.Assert.assertSame(before, empty.paletteIndices);
        empty.reduceToHalfCanvas();
        org.junit.Assert.assertNotSame(before, empty.paletteIndices);
        assertEquals(0, empty.paletteIndices.length);
    }

    @Test
    public void halfCanvasTruncatesOddDimensionsAndPreservesExistingEdgeFailure() throws Exception {
        Background valid = canvasFixture(2, 2, 2, 2, 5, 7);
        valid.paletteIndices = new byte[] {1, -128, -1, 0};
        valid.reduceToHalfCanvas();
        assertArrayEquals(new byte[6], valid.paletteIndices); // last zero overwrites all three nonzero indices
        assertEquals(2, valid.canvasWidth); assertEquals(3, canvasHeight(valid));
        Background edge = canvasFixture(1, 1, 4, 6, 5, 7);
        byte[] before = edge.paletteIndices;
        try { edge.reduceToHalfCanvas(); org.junit.Assert.fail("odd outer edge exceeds truncated half canvas"); }
        catch (ArrayIndexOutOfBoundsException expected) {
            org.junit.Assert.assertSame(before, edge.paletteIndices);
            assertEquals(2, edge.canvasWidth); assertEquals(3, canvasHeight(edge));
            assertEquals(1, edge.imageWidth); assertEquals(1, edge.imageHeight);
            assertEquals(4, edge.offsetX); assertEquals(6, edge.offsetY);
        }
    }


    @Test
    public void indexedDrawClipsEveryEdgeAndCornerBeforePaletteLookup() throws Exception {
        Background background = drawingFixture(9, 7);
        int[][] clips = {{0,0,18,16}, {3,2,14,12}, {5,4,5,10}, {5,4,12,4}};
        for (int[] clip : clips) for (int y = -8; y <= 17; y++) for (int x = -10; x <= 19; x++) {
            int[] actual = raster(18, 16, FILL), expected = actual.clone();
            DrawingArea.setDrawingArea(clip[3], clip[0], clip[2], clip[1]);
            indexedDrawOracle(expected, 18, background, x, y, clip);
            background.drawNonzeroIndexed(x, y);
            assertArrayEquals("clip=" + Arrays.toString(clip) + " x=" + x + " y=" + y, expected, actual);
        }
    }

    @Test
    public void indexedDrawOffsetsAreSignedPixelTranslationsAndInputsAreReadOnly() throws Exception {
        Background background = drawingFixture(9, 7);
        byte[] indices = background.paletteIndices, savedIndices = indices.clone();
        int[] palette = background.paletteRgb, savedPalette = palette.clone();
        int[] clip = {3,2,14,12};
        for (int x : new int[] {-5,-1,0,2,7}) for (int y : new int[] {-4,0,3,8}) {
            background.offsetX = x; background.offsetY = y;
            int[] actual = raster(18, 16, FILL), expected = actual.clone();
            DrawingArea.setDrawingArea(12,3,14,2);
            indexedDrawOracle(expected, 18, background, 1, 1, clip);
            background.drawNonzeroIndexed(1, 1);
            assertArrayEquals(expected, actual);
            org.junit.Assert.assertSame(indices, background.paletteIndices);
            org.junit.Assert.assertSame(palette, background.paletteRgb);
            assertArrayEquals(savedIndices, indices); assertArrayEquals(savedPalette, palette);
            assertEquals(x, background.offsetX); assertEquals(y, background.offsetY);
            assertEquals(9, background.imageWidth); assertEquals(7, background.imageHeight);
        }
    }

    @Test
    public void indexedDrawUsesUnsignedIndicesAndOnlyIndexZeroIsTransparent() throws Exception {
        Background background = drawingFixture(16, 16);
        for (int i = 0; i < 256; i++) background.paletteIndices[i] = (byte) i;
        int[] actual = raster(18, 18, FILL), expected = actual.clone();
        indexedDrawOracle(expected,18,background,1,1,new int[] {0,0,18,18});
        background.drawNonzeroIndexed(1,1);
        assertArrayEquals(expected, actual);
        assertEquals(FILL, actual[19]); // index zero, despite its nonzero palette value
        assertEquals(0, actual[20]); // nonzero index one copies a zero color
        assertEquals(background.paletteRgb[128], actual[9 * 18 + 1]);
        assertEquals(background.paletteRgb[255], actual[16 * 18 + 16]);
    }

    @Test
    public void indexedRowCopyUsesIndependentStartsSkipsBlocksAndTails() throws Exception {
        java.lang.reflect.Method copy = Background.class.getDeclaredMethod("copyNonzeroIndexedRows", int.class,
                int[].class, byte[].class, int.class, int.class, int.class, int.class, int[].class, int.class);
        copy.setAccessible(true);
        Background background = drawingFixture(1,1);
        for (int width = 0; width <= 12; width++) for (int rows = 0; rows <= 4; rows++)
            for (int sourceSkip : new int[] {0,3}) for (int destinationSkip : new int[] {0,5}) {
                int sourceStart=2, destinationStart=7;
                byte[] source = new byte[sourceStart + rows * (width + sourceSkip) + 3];
                for (int i=0;i<source.length;i++) source[i] = i%5==0 ? 0 : (byte)(i*43);
                byte[] unchanged=source.clone(); int[] palette=background.paletteRgb.clone();
                int[] actual=filled(destinationStart + rows*(width+destinationSkip)+3,FILL), expected=actual.clone();
                for(int y=0;y<rows;y++) for(int x=0;x<width;x++) {
                    int index=source[sourceStart+y*(width+sourceSkip)+x]&255;
                    if(index!=0) expected[destinationStart+y*(width+destinationSkip)+x]=palette[index];
                }
                copy.invoke(background, rows, actual, source, destinationSkip, destinationStart,
                        width, sourceStart, palette, sourceSkip);
                assertArrayEquals(expected,actual); assertArrayEquals(unchanged,source);
                assertArrayEquals(background.paletteRgb,palette);
            }
        for(int width=0;width<=12;width++) {
            byte[] source=new byte[width]; Arrays.fill(source,(byte)255);
            int[] actual=new int[width], expected=filled(width,background.paletteRgb[255]);
            copy.invoke(background,1,actual,source,0,0,width,0,background.paletteRgb,0);
            assertArrayEquals(expected,actual); // exact array ends, including zero width
        }
    }


    @Test
    public void archiveMemberSelectionAndBothStorageOrdersProduceRowMajorIndices() throws Exception {
        int fixtureIndex = 0;
        for (int width = 0; width <= 4; width++) for (int height = 0; height <= 3; height++)
            for (int layout = 0; layout <= 1; layout++) {
                StreamLoader archive = constructorArchive(CONSTRUCTOR_FIXTURES[fixtureIndex++]);
                Background first = new Background(archive, "sheet", 0);
                assertArrayEquals(new byte[] {1,2,3}, first.paletteIndices);
                assertEquals(1, first.imageWidth); assertEquals(3, first.imageHeight);
                Background selected = new Background(archive, "sheet", 1);
                assertEquals(width, selected.imageWidth); assertEquals(height, selected.imageHeight);
                assertEquals(513, selected.canvasWidth); assertEquals(770, canvasHeight(selected));
                assertEquals(255, selected.offsetX); assertEquals(254, selected.offsetY);
                byte[] expected = new byte[width * height];
                for (int y = 0; y < height; y++) for (int x = 0; x < width; x++)
                    expected[y * width + x] = (byte) ((x + 3 * y) % 5);
                assertArrayEquals(expected, selected.paletteIndices);
                assertArrayEquals(new int[] {0,0x491f13,0x923e26,0xdb5d39,0x247c4c}, selected.paletteRgb);
                Background last = new Background(archive, "sheet", 2);
                assertArrayEquals(new byte[] {3,1}, last.paletteIndices);
                assertEquals(2, last.imageWidth); assertEquals(1, last.imageHeight);
                assertEquals(4, last.offsetX); assertEquals(5, last.offsetY);
                // A negative member selector runs no skip iterations, selecting member zero.
                assertArrayEquals(first.paletteIndices, new Background(archive, "sheet", -1).paletteIndices);
            }
    }

    @Test
    public void archivePaletteUsesRgb24AndRawByteIndicesWithUnsignedWordDimensions() throws Exception {
        for (String encoded : BYTE_INDEX_FIXTURES) {
            Background selected = new Background(constructorArchive(encoded), "sheet", 1);
            assertEquals(65535, selected.canvasWidth); assertEquals(65534, canvasHeight(selected));
            assertEquals(16, selected.imageWidth); assertEquals(16, selected.imageHeight);
            assertEquals(255, selected.paletteRgb.length); assertEquals(0, selected.paletteRgb[0]);
            for (int i = 1; i < 255; i++) assertEquals(((i * 73 & 255) << 16)
                    | ((i * 31 & 255) << 8) | (i * 19 & 255), selected.paletteRgb[i]);
            for (int i = 0; i < 256; i++) assertEquals((byte) i, selected.paletteIndices[i]);
            // Decoder stores 255 even with only 255 palette slots; it does not validate palette membership.
            selected.paletteIndices[255] = 0;
            int[] actual = raster(18,18,FILL), expected = actual.clone();
            for (int i = 1; i < 255; i++) expected[(i / 16 + 1) * 18 + i % 16 + 1]
                    = selected.paletteRgb[i];
            selected.drawNonzeroIndexed(1 - selected.offsetX,1 - selected.offsetY);
            assertArrayEquals(expected, actual);
        }
    }

    @Test
    public void archiveUnknownLayoutLeavesAllocatedZeroIndicesAndSingleSlotPalette() throws Exception {
        Background selected = new Background(constructorArchive(UNKNOWN_LAYOUT_FIXTURE), "sheet", 1);
        assertEquals(3, selected.imageWidth); assertEquals(2, selected.imageHeight);
        assertArrayEquals(new byte[6], selected.paletteIndices);
        assertArrayEquals(new int[] {0}, selected.paletteRgb);
    }

    private static StreamLoader constructorArchive(String encoded) {
        return new StreamLoader(Base64.getDecoder().decode(encoded));
    }

    private static final String[] CONSTRUCTOR_FIXTURES = {
        "AABeAACEMUFZJlNZoCnjcwAAB//y/pIIAOVCACEDJFBCA6WQhAAKEABBQAAIAAGgAFRgAANA0GQaeoY0Qap4obUAEwAJo0ME3RA5V/vnOoZApjEmhsQuViRAmeCQIbZnIickBvQZHOxmSuIHvoqBwLiBGpBSVLFgD6Ak1Fi25fi7kinChIUBTxuY",
        "AABeAACCMUFZJlNZ+4g/uQAACH/y/pIIAOVCACEDJFBCA6WQhAAKEABBQAAIAAGgAFRgAANA0GQaeoY0QinihtQAAAAAB6KxQp82UaApxCC4Vg5EK0UbwCqQTAyXIoDPwQDqUyOdjMlcQPfRUDgXECNSCkqWLAH0BJqLFty/F3JFOFCQ+4g/uQ==",
        "AABeAACBMUFZJlNZUilJFQAAB3/y/pIIAOVCACEDJFBCA6WQhAAKEABBQAAIAAGgAFRgAANA0AHqeoY0QiZEbUAAAAAAeiUJ/NlGYLcMhG4UByKKzW8ClyCkEkoggIvBAOpbQ501XG0Bd9HArj3iZiBShKiWJeiRsyWpmH4u5IpwoSCkUpIq",
        "AABeAACCMUFZJlNZCYiV3wAAB3/y/pIIAOVCACEDJFBCA6WQhAAKEABBQAAIAAGgAFRgAANA0AHqeoY0QiYoPUAAAAAAeiUKfNlGgMuIQXCoORVWjLDwLlIJgZLEUBn4IB1LaHOmq42gLvo4Fce8TMQKUJUSxL0SNmS1Mw/F3JFOFCQCYiV3wA==",
        "AABeAACDMUFZJlNZQOmqCAAAB3/y/pIIAOVCACEDJFBCA6WQhAAKEABBQAAIAAGgAFRgAANA0GQaeoY0Qaqfkib1AAAAAAHhHAnrRxIG0QlFaX4cEKkXdCzBRmoKNYw+Z9kBzIZHOxmSuIHvoqBwLiBGpBSVLFgD6Ak1Fi25fi7kinChIIHTVBA=",
        "AABeAACDMUFZJlNZG0h2wgAAB3/y/pIIAOVCACEDJFBCA6WQhAAKEABBQAAIAAGgAFRgAANA0GQaeoY0Qaqfkib1AAAAAAHhHAnrRxIG0QhaX4cEKkYDoWYjM1AzWM31K7IDmQyOdjMlcQPfRUDgXECNSCkqWLAH0BJqLFty/F3JFOFCQG0h2wg=",
        "AABeAACDMUFZJlNZsukAbgAAB3/y/pIIAOVCACEDJFBCA6WQhAAKEABBQAAIAAGgAFRgAANA0GQaeoY0Qin6UD1AAAAAAHoOFPmzjQnEQNi4KFyCraRglmZAqqAaxlAZ+Ai6lMjnYzJXED30VA4FxAjUgpKliwB9ASaixbcvxdyRThQkLLpAG4A=",
        "AABeAACBMUFZJlNZ6UjcpAAAB3/y/pIIAOVCACEDJFBCA6WQhAAKEABBQAAIAAGgAFRgAANA0AHqeoY0QiZEbUAAAAAAeiUJ/NlGYOGYhcKA5EKzW8hS5BSkRJRFAlPggHUtoc6arjaAu+jgVx7xMxApQlRLEvRI2ZLUzD8XckU4UJDpSNyk",
        "AABeAACDMUFZJlNZWYVknQAAB3/y/pIIAOVCACEDJFBCA6WQhAAKEABBQAAIAAGgAFRgAANA0DQaaGT9RCJkRpoGgAAAAaCArSxRRscB5SjXiYOhNYpMAITKJwOSCBZF6IB3NdpxpquNo465KcXFvQRiUKSr4L7HqATMLDMPxdyRThQkFmFZJ0A=",
        "AABeAACEMUFZJlNZAiS4VwAAB3/y/pIIAOVCACEDJFBCA6WQhAAKEABBQAAIAAGgAFRgAANA0DQaaGT9RCKeKGmgaAAAABoIKQtJRrcBx5JBeLAdCxYlBwAKJRMDJkiUZ+iAdzXH401XG0cdclOLi3oIxKFJV8F9j1AJmFhmH4u5IpwoSAESXCuA",
        "AABfAACDMUFZJlNZ3aoD/gAAB3/y/tIIAOVCACEDJFBCA6WQhAAKEABBQAAIAAGgAFRgAANA0ADTQyfqIRNCMmQaAAAAACxPylRJ+FLqyrG4oE5EArUlAHRIJjnAwgw3hiB1LbnxnoqFgw65KYWFtORaDpQn4KOPEAmIUF3fi7kinChIbtUB/wA=",
        "AABfAACDMUFZJlNZhgvfNAAAB3/y/tIIAOVCACEDJFBCA6WQhAAKEABBQAAIAAGgAFRgAANA0ADTQyfqIRNCGmQaAAAABoK0MSmNDBQ8oqrxAnQgFiUNuA0WUTHOBhFZvTkDua7TjPRULBh1yUwsLaci0HShPwUceIBMQoLu/F3JFOFCQhgvfNA=",
        "AABgAACEMUFZJlNZRHFzMAAAB3/y/rIIAOVCACEDJFBCA6WQhAAKEABBQAAIAAGgAFRgAANA0ADTQyfqIU0ahpkGgAAAAaAtDEpjQwHnSKUN5iF6AtiWmyC4EpjiAnAqx9OUnc12nGeioWDDrkphYW05FoOlCfgo48QCYhQXd+LuSKcKEgiOLmYA",
        "AABgAACEMUFZJlNZH9Cv+gAAB3/y/rIIAOVCACEDJFBCA6WQhAAKEABBQAAIAAGgAFRgAANA0ADTQyfqIRNCGmQaAAAABoK0MSmNDBedBSrxAnQksSgGwO5KUpjiYwgKz+nIHc1xuM9FQsGHXJTCwtpyLQdKE/BRx4gExCgu78XckU4UJAf0K/6A",
        "AABhAACEMUFZJlNZMGf2kAAAB3/y/pIIAOVCACEDJFBCA6WQhAAKEABBQAAIAAGgAFRUAAANNBkGmhk8kInqmRpkAAAAAAPoVEqJPwcbWFsXMEbkGrWy2CQmQHQgjgAoD+HMXUp08anGQiQ65MkMDFVa1amLZ8A9h6QLMwEWl/F3JFOFCQMGf2kA",
        "AABhAACDMUFZJlNZa8YqWgAAB3/y/pIIAOVCACEDJFBCA6WQhAAKEABBQAAIAAGgAFRUAAANNAB6mhk8kInqmRpkAAAAAAPoVEqJPwcWol3Ajcg1a2mwZCZAdCCOABAfxBi6ltDjPQojAPXKAG5B01sUlal/CXY+mGrESJh/xdyRThQkGvGKloA=",
        "AABeAACDMUFZJlNZV7HxGAAAB3/y/pIIAOVCACEDJFBCA6WQhAAKEABBQAAIAAGgAFRgAANA0DQaaGT9RBqm0RpoGgAAAABES0/KKJI+dUpi4zA5MK1NvhATIY4OSCCyLwwHUtp+NNVxtHHXJTi4t6CMShSVfBfY9QCZhYZh+LuSKcKEgr2PiMA=",
        "AABeAACEMUFZJlNZDBAt0gAAB3/y/pIIAOVCACEDJFBCA6WQhAAKEABBQAAIAAGgAFRgAANA0GgaaGT9RBqnihpoAAAExMTJhnErxZyNhXjy0GBMDkhatxIAQVMSBDZM4kTsgP05j8aGXG0cdclOLi3oIxKFJV8F9j1AJmFhmH4u5IpwoSAYIFuk",
        "AABgAACCMUFZJlNZykVEngAAB3/y/rIIAOVCACEDJFBCA6WQhAAKEABBQAAIAAGgAFRgAANAA0DTQyfqIUyIYQaAAAAAAxHylRIKlfylhcJC8gWtQpSQXAkExxOAEYbwxSdS258aFVCwYdc4GFl9pyLQdKE/BRx4gEyCgu78XckU4UJDKRUSeA==",
        "AABgAACCMUFZJlNZkeSYVAAAB3/y/rIIAOVCACEDJFBCA6WQhAAKEABBQAAIAAGgAFRgAANAA0DTQyfqIVkIYQaAAAAACxHylRIBK/lLuEgciFaipbSQxRkKY5TiIsN4cCdS258aFVCwYdc4GFl9pyLQdKE/BRx4gEyCgu78XckU4UJCR5JhUA==",
        "AABiAACFMUFZJlNZpqVBcwAAB3/y/poIAOVCACEDJFBCA6WQhAAKEABBQAAIAAGgAFRgAANDQGQNNDJ5IRNNEMgAAAAAA/NQrXE0Cm/iW2e44G5EK1pJQCyISHJECUAKBJHhGBZTo4McY2kB1zigLcCpIxSVqX8ApBXoEzARZf+LuSKcKEhTUqC5gA==",
        "AABiAACGMUFZJlNZP0nmVAAAB3/y/poIAOVCACEDJFBCA6WQhAAKEABBQAAIAAGgAFRgAANDQGQNNDJ5IRPUIyZTAQYAAE0egcuVjJlk1x2sbcCNyCrYpQBJUPIOhKJAkAjo/JMSynRwY4xtIDrnFAXYFSRikrUv4BSCvQJmAiy/8XckU4UJA/SeZUA=",
        "AABkAACHMUFZJlNZSbrLMgAAB3/y/pIIAOVCACEDJFBCA6WQhAAKEABBQAAIAAGgAFRgAANDQGgaaG1PJCKemiNMgAAAABoLExKVGkHVsJS0XmBroQsVCk7gCkZTARziJAJCg/pNtBchPGiCjaSPXOKRtwOozFqBYfCXYgsTZvETD/i7kinChIJN1lmQ",
        "AABkAACGMUFZJlNZbJgSxwAAB3/y/pIIAOVCACEDJFBCA6WQhAAKEABBQAAIAAGgAFRUAADRkBoGmhtT0QiaNIMg0AAAAAPzjFKicBfy0KXcIG5EE1qbbIyTjIdhJSAiQiBI8IxAMhPGiGSESPXOKRNgdRWrUCw+FnYgsS5vsY0/4u5IpwoSDZMCWOA=",
        "AABeAACCMUFZJlNZrh129gAAB3/y/pIIAOVCACEDJFBCA6WQhAAKEABBQAAIAAGgAFRgAANAABp6hk/UQiZEMgaAAAABociW4xRRpB1ilGvEwdCFikwAhyUTnESQQLIvRAO5rj8Z6KhYOOucDiy+0jLQSUJ+Cux6QNkFBd34u5IpwoSFcOu3sA==",
        "AABeAACDMUFZJlNZ9byqPAAAB3/y/pIIAOVCACEDJFBCA6WQhAAKEABBQAAIAAGgAFRgADQ0AGgaaGT9RCKeKDIGgAAAAaGSnFpKNAIdWkwvFgOhCxKDgAXJRMxEmSJRn6IB3MY/GhlxtHHXOJxbhcgrEoUlXwX2PUAuYWGX/i7kinChIet5VHg=",
        "AABhAACFMUFZJlNZEEH63QAAB3/y/pIIAOVCACEDJFBCA6WQhAAKEABBQAAIAAGgAFRUAAANAaB6mhk8kGppqGnpJgmBDACYRhdSIElYHw3pGlasK3JXtq0QLZMQM3ARkIz/GocToDjRBRGAeucQG7A6a2KStS/hLsfTDVkJEw/4u5IpwoSAgg/W6A==",
        "AABhAACDMUFZJlNZS+AmFwAAB3/y/pIIAOVCACEDJFBCA6WQhAAKEABBQAAIAAGgAFRUAAANAaB6mhk8kImmkHpAAAAAAD9CYHE0DYL+NdwgjkUVrSbJgSZBAiUAkIwF4pAdSoDjRBRGAeucQG7A6a2KStS/hLsfTDVkJEw/4u5IpwoSCXwEwuA=",
        "AABkAACFMUFZJlNZhqg2JAAAB//y/pIIAOVCACEDJFBCA6WQhAAKEABBQAAIAAGgAFRgAANDQGgaaG1PJCJigaZAAAAAAPzkLrsTgJUPy1LauIEciFaiZSBNjIQGwwgyIxSfhoaC5CeNEFG0keucUjbgdRmLUCw+EuxBYmzeImH/F3JFOFCQhqg2JA==",
        "AABkAACIMUFZJlNZDFiKsQAAB//y/pIIAOVCACEDJFBCA6WQhAAKEABBQAAIAAGgAFRUAADRkBoGmhtT0QieqekZPUARkMIwACDtsqJM0oE7BpBaCVsCqR5gBLZJ4M1uOhRgE6fK1EAyE8aIZIRI9c4pE2B1FatQLD4WdiCxLm+xjT/i7kinChIBixFWIA==",
        "AABnAACJMUFZJlNZomaXvwAAB//y/pJIAOVCACEDJFBCA6WQhAAKEABBQAAIAAGgAFRUAADQ0A0DTQyeSETEo2m1JgCDAAAIPbIkbGwGtTskeKsgVsIUIQN8gTkoqNawITMCMUMHWltkMdHGhxhIgOucEBZeZJWKStS/gFIK9AuQCLLvxdyRThQkKJml78A=",
        "AABnAACJMUFZJlNZ48cQewAAB//y/pJIAOVCACEDJFBCA6WQhAAKEABBQAAIAAGgAFRUAADQ0A0DTQyeSETU2kaZAAAAAAD8mFaokgv5YKJdwgbkQRWplojIYPJAdBIMwJxAQIvHmltkMdHGhxhIgOucEBbeZJWKStS/gFIK9AuQCLLvxdyRThQkOPHEHsA=",
        "AABeAACDMUFZJlNZS9jaEgAABv/y/pIIAOVCACEDJFBCA6WQhAAKEABBQAAIAAGgAFRgAANAABp6hk/UQiZTQ9QGgAAAAaJIlNrcKMwdM1iF4oDoQscOEEnKLAMEkkKIvRAO5rj8Z6KhYOOucDiy+0jLQSUJ+Cux6QNkFBd34u5IpwoSCXsbQkA=",
        "AABeAACDMUFZJlNZEHkG2AAABv/y/pIIAOVCACEDJFBCA6WQhAAKEABBQAAIAAGgAFRgADQ0AGgaaGT9RCKeJD1AAAAAACigZ+aKJARtC+G4Vg5EK2kN8BSQTAyUQFAZ+CAdSmPxoZcbRx1zicW4XIKxKFJV8F9j1ALmFhl/4u5IpwoSAg8g2wA=",
        "AABiAACDMUFZJlNZJIJsagAABv/y/poIAOVCACEDJFBCA6WQhAAKEABBQAAIAAGgAFRgAANAA0D1NDJ5IRGUB6QAAAAACfmoTOJoDNRq+G4gRylFahNsBZkSBLJWIhQofCQHUpw40OULAD1zgA2X2oMtKFJV8JIFPomyEiXd+LuSKcKEgSQTY1A=",
        "AABiAACFMUFZJlNZfyOwoAAABv/y/poIAOVCACEDJFBCA6WQhAAKEABBQAAIAAGgAFRgAANDQGQNNDJ5IRGSjaekAAAJgAmJ8XCZxLAzYbPLhBHCUVsE2mwIZEgSkqIhAWF2pAclOjgxxjaQHXOKAuwKkjFJWpfwCkFegTMBFl/4u5IpwoSD+R2FAA==",
        "AABmAACLMUFZJlNZPYkJMQAAB3/y/pKIAOVCACEDJFBCA6WQhAAKEABBQAAIAAGgAFRUAADQ0A0DTQyeSEU8SJ6n6RMTAAJhGCegtXGxPaw8owUY168wc3ELGEDqASxlIDUShIzEQQwdaW2Qx0caHGEiA65wQFl5klYpK1L+AUgr0C5AIsu/F3JFOFCQPYkJMQ==",
        "AABmAACLMUFZJlNZ/+QjKQAAB3/y/pKIAOVCACEDJFBCA6WQhAAKEABBQAAIAAGgAFRUAADQ0A0DTQyeSEU/VPUGj1CYAATEwmAPFRI7GokRdHU8vXJb+CsRtQAJhTkpmwyZGACBMmDzS2yGOjjQ4wkQHXOCAtvMkrFJWpfwCkFegXIBFl34u5IpwoSH/yEZSA==",
        "AABqAACKMUFZJlNZTS7WVgAAB3/y/pIIAOVCACEDJFBCA6WQhAAKEABBQAAIAAGgAHQqGgAAaA0DTQ2p5QU00jJ6QAAAAAA+XCwolmImIJnzVwmPyB62IUSjCMgHFKSBIpAUAAOnE7QNmtSrfNJQGUPPGChsvQur5iwYHol0IMNTiJF8j8XckU4UJBNLtZWA",
        "AABqAACLMUFZJlNZxxqlrgAAB3/y/pIIAOVCACEDJFBCA6WQhAAKEABBQAAIAAGgAFRUGgABkBoGmhtT0QiZNJk8ogGACDQaZoMYclTnAFRUNY4EDPwg+1XwMAcTeqNcbiExMWCwiiVwHSGQnjRDIiJHrnFImwOqxq1AsPhZ2ILJXkLGNP+LuSKcKEhjjVLXAA=="
    };
    private static final String[] BYTE_INDEX_FIXTURES = {
        "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"
    };
    private static final String UNKNOWN_LAYOUT_FIXTURE = "AABYAABvMUFZJlNZQueeBgAACFfQ/pJgQAMkUEADpZCAAApBQAABoABUUJoGmTQYjajDINTamo0wgAAANQp9RKEJPEYfRiMUDO0DleXKpe1qggoUq9LB4CZIII+NlsAT+pOlbD7au70iX9xdyRThQkELnngY";

    private static Background drawingFixture(int width,int height) throws Exception {
        Background background=canvasFixture(width,height,0,0,width,height);
        int[] palette=new int[256];
        for(int i=0;i<256;i++) palette[i]=0x80000000 | i*0x010305;
        palette[0]=0x123456; palette[1]=0;
        Field field=Background.class.getDeclaredField("paletteRgb"); field.setAccessible(true);
        field.set(background,palette);
        for(int i=0;i<background.paletteIndices.length;i++)
            background.paletteIndices[i]=i%5==0?0:(byte)(i*43);
        return background;
    }

    private static void indexedDrawOracle(int[] raster,int width,Background background,int x,int y,int[] clip) {
        for(int sourceY=0;sourceY<background.imageHeight;sourceY++)
            for(int sourceX=0;sourceX<background.imageWidth;sourceX++) {
                int dx=x+background.offsetX+sourceX, dy=y+background.offsetY+sourceY;
                int index=background.paletteIndices[sourceY*background.imageWidth+sourceX]&255;
                if(index!=0 && dx>=clip[0] && dx<clip[2] && dy>=clip[1] && dy<clip[3])
                    raster[dy*width+dx]=background.paletteRgb[index];
            }
    }

    private static int canvasHeight(Background background) throws Exception {
        Field field = Background.class.getDeclaredField("canvasHeight"); field.setAccessible(true);
        return field.getInt(background);
    }

    private static Background canvasFixture(int width, int height, int x, int y,
            int canvasWidth, int canvasHeight) throws Exception {
        Background background = new Background(archive(), "small", 0);
        background.imageWidth = width; background.imageHeight = height;
        background.canvasWidth = canvasWidth;
        Field field = Background.class.getDeclaredField("canvasHeight"); field.setAccessible(true);
        field.setInt(background, canvasHeight);
        background.offsetX = x; background.offsetY = y;
        background.paletteIndices = new byte[width * height];
        for (int i = 0; i < background.paletteIndices.length; i++)
            background.paletteIndices[i] = i % 4 == 3 ? 0 : (byte) (i % 2 == 0 ? i + 1 : 128 + i);
        return background;
    }

    private static Background single(int x, int y) {
        Background background = new Background(archive(), "small", 0);
        background.imageWidth = 1;
        background.imageHeight = 1;
        background.paletteIndices = new byte[] {1};
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
