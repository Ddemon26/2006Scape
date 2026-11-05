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
                0,0,1,2,0, 0,0,3,4,0, 0,0,0,0,0}, background.aByteArray1450);
        assertEquals(5, background.anInt1452);
        assertEquals(6, background.anInt1453);
        assertEquals(0, background.offsetX);
        assertEquals(0, background.offsetY);

        Background fullSize = new Background(archive(), "small", 0);
        fullSize.anInt1452 = 5;
        fullSize.anInt1453 = 6;
        fullSize.expandToCanvas(); // Already canvas-sized: returns before touching offsets.
        assertEquals(2, fullSize.offsetX);
        assertEquals(3, fullSize.offsetY);
    }

    @Test
    public void halfScaleExpansionHalvesOffsetPositionsAndResetsThem() {
        Background background = new Background(archive(), "small", 0);
        background.reduceToHalfCanvas();
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
        original.expandToCanvas();
        Background mirrored = new Background(archive(), "small", 0);
        mirrored.flipHorizontally();
        assertEquals(1, mirrored.offsetX); // canvas 5 - width 2 - offset 2
        assertEquals(3, mirrored.offsetY);
        assertArrayEquals(new byte[] {2, 1, 4, 3}, mirrored.aByteArray1450);
        mirrored.expandToCanvas();
        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 5; x++) {
                assertEquals(original.aByteArray1450[y * 5 + 4 - x], mirrored.aByteArray1450[y * 5 + x]);
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
        assertArrayEquals(new byte[] {3, 4, 1, 2}, mirrored.aByteArray1450);
        mirrored.expandToCanvas();
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

    @Test
    public void fullAndHalfCanvasRemappingPreserveIndicesAndChooseLastRowMajorWriter() throws Exception {
        for (int width = 0; width <= 4; width++) for (int height = 0; height <= 4; height++)
            for (int x = 0; x <= 3; x++) for (int y = 0; y <= 3; y++) {
                Background full = canvasFixture(width, height, x, y, 8, 8);
                Background half = canvasFixture(width, height, x, y, 8, 8);
                byte[] source = full.aByteArray1450, halfSource = half.aByteArray1450;
                byte[] unchanged = source.clone();
                int[] palette = full.anIntArray1451, halfPalette = half.anIntArray1451;
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
                assertArrayEquals(expanded, full.aByteArray1450);
                assertArrayEquals(reduced, half.aByteArray1450);
                assertArrayEquals(unchanged, source); assertArrayEquals(unchanged, halfSource);
                org.junit.Assert.assertNotSame(source, full.aByteArray1450);
                org.junit.Assert.assertNotSame(halfSource, half.aByteArray1450);
                org.junit.Assert.assertSame(palette, full.anIntArray1451);
                org.junit.Assert.assertSame(halfPalette, half.anIntArray1451);
                assertEquals(8, full.anInt1452); assertEquals(8, full.anInt1453);
                assertEquals(8, full.anInt1456); assertEquals(8, canvasHeight(full));
                assertEquals(4, half.anInt1452); assertEquals(4, half.anInt1453);
                assertEquals(4, half.anInt1456); assertEquals(4, canvasHeight(half));
                assertEquals(0, full.offsetX); assertEquals(0, full.offsetY);
                assertEquals(0, half.offsetX); assertEquals(0, half.offsetY);
            }
    }

    @Test
    public void fullCanvasEarlyReturnRequiresBothDimensionsAndKeepsArrayAndOffsets() throws Exception {
        Background full = canvasFixture(8, 8, 2, 3, 8, 8);
        byte[] source = full.aByteArray1450;
        full.expandToCanvas();
        org.junit.Assert.assertSame(source, full.aByteArray1450);
        assertEquals(2, full.offsetX); assertEquals(3, full.offsetY);
        for (int[] size : new int[][] {{8, 3}, {3, 8}, {0, 0}}) {
            Background partial = canvasFixture(size[0], size[1], 0, 0, 8, 8);
            byte[] before = partial.aByteArray1450;
            partial.expandToCanvas();
            org.junit.Assert.assertNotSame(before, partial.aByteArray1450);
            assertEquals(64, partial.aByteArray1450.length);
            assertEquals(8, partial.anInt1452); assertEquals(8, partial.anInt1453);
        }
        Background empty = canvasFixture(0, 0, 0, 0, 0, 0);
        byte[] before = empty.aByteArray1450;
        empty.expandToCanvas();
        org.junit.Assert.assertSame(before, empty.aByteArray1450);
        empty.reduceToHalfCanvas();
        org.junit.Assert.assertNotSame(before, empty.aByteArray1450);
        assertEquals(0, empty.aByteArray1450.length);
    }

    @Test
    public void halfCanvasTruncatesOddDimensionsAndPreservesExistingEdgeFailure() throws Exception {
        Background valid = canvasFixture(2, 2, 2, 2, 5, 7);
        valid.aByteArray1450 = new byte[] {1, -128, -1, 0};
        valid.reduceToHalfCanvas();
        assertArrayEquals(new byte[6], valid.aByteArray1450); // last zero overwrites all three nonzero indices
        assertEquals(2, valid.anInt1456); assertEquals(3, canvasHeight(valid));
        Background edge = canvasFixture(1, 1, 4, 6, 5, 7);
        byte[] before = edge.aByteArray1450;
        try { edge.reduceToHalfCanvas(); org.junit.Assert.fail("odd outer edge exceeds truncated half canvas"); }
        catch (ArrayIndexOutOfBoundsException expected) {
            org.junit.Assert.assertSame(before, edge.aByteArray1450);
            assertEquals(2, edge.anInt1456); assertEquals(3, canvasHeight(edge));
            assertEquals(1, edge.anInt1452); assertEquals(1, edge.anInt1453);
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
        byte[] indices = background.aByteArray1450, savedIndices = indices.clone();
        int[] palette = background.anIntArray1451, savedPalette = palette.clone();
        int[] clip = {3,2,14,12};
        for (int x : new int[] {-5,-1,0,2,7}) for (int y : new int[] {-4,0,3,8}) {
            background.offsetX = x; background.offsetY = y;
            int[] actual = raster(18, 16, FILL), expected = actual.clone();
            DrawingArea.setDrawingArea(12,3,14,2);
            indexedDrawOracle(expected, 18, background, 1, 1, clip);
            background.drawNonzeroIndexed(1, 1);
            assertArrayEquals(expected, actual);
            org.junit.Assert.assertSame(indices, background.aByteArray1450);
            org.junit.Assert.assertSame(palette, background.anIntArray1451);
            assertArrayEquals(savedIndices, indices); assertArrayEquals(savedPalette, palette);
            assertEquals(x, background.offsetX); assertEquals(y, background.offsetY);
            assertEquals(9, background.anInt1452); assertEquals(7, background.anInt1453);
        }
    }

    @Test
    public void indexedDrawUsesUnsignedIndicesAndOnlyIndexZeroIsTransparent() throws Exception {
        Background background = drawingFixture(16, 16);
        for (int i = 0; i < 256; i++) background.aByteArray1450[i] = (byte) i;
        int[] actual = raster(18, 18, FILL), expected = actual.clone();
        indexedDrawOracle(expected,18,background,1,1,new int[] {0,0,18,18});
        background.drawNonzeroIndexed(1,1);
        assertArrayEquals(expected, actual);
        assertEquals(FILL, actual[19]); // index zero, despite its nonzero palette value
        assertEquals(0, actual[20]); // nonzero index one copies a zero color
        assertEquals(background.anIntArray1451[128], actual[9 * 18 + 1]);
        assertEquals(background.anIntArray1451[255], actual[16 * 18 + 16]);
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
                byte[] unchanged=source.clone(); int[] palette=background.anIntArray1451.clone();
                int[] actual=filled(destinationStart + rows*(width+destinationSkip)+3,FILL), expected=actual.clone();
                for(int y=0;y<rows;y++) for(int x=0;x<width;x++) {
                    int index=source[sourceStart+y*(width+sourceSkip)+x]&255;
                    if(index!=0) expected[destinationStart+y*(width+destinationSkip)+x]=palette[index];
                }
                copy.invoke(background, rows, actual, source, destinationSkip, destinationStart,
                        width, sourceStart, palette, sourceSkip);
                assertArrayEquals(expected,actual); assertArrayEquals(unchanged,source);
                assertArrayEquals(background.anIntArray1451,palette);
            }
        for(int width=0;width<=12;width++) {
            byte[] source=new byte[width]; Arrays.fill(source,(byte)255);
            int[] actual=new int[width], expected=filled(width,background.anIntArray1451[255]);
            copy.invoke(background,1,actual,source,0,0,width,0,background.anIntArray1451,0);
            assertArrayEquals(expected,actual); // exact array ends, including zero width
        }
    }

    private static Background drawingFixture(int width,int height) throws Exception {
        Background background=canvasFixture(width,height,0,0,width,height);
        int[] palette=new int[256];
        for(int i=0;i<256;i++) palette[i]=0x80000000 | i*0x010305;
        palette[0]=0x123456; palette[1]=0;
        Field field=Background.class.getDeclaredField("anIntArray1451"); field.setAccessible(true);
        field.set(background,palette);
        for(int i=0;i<background.aByteArray1450.length;i++)
            background.aByteArray1450[i]=i%5==0?0:(byte)(i*43);
        return background;
    }

    private static void indexedDrawOracle(int[] raster,int width,Background background,int x,int y,int[] clip) {
        for(int sourceY=0;sourceY<background.anInt1453;sourceY++)
            for(int sourceX=0;sourceX<background.anInt1452;sourceX++) {
                int dx=x+background.offsetX+sourceX, dy=y+background.offsetY+sourceY;
                int index=background.aByteArray1450[sourceY*background.anInt1452+sourceX]&255;
                if(index!=0 && dx>=clip[0] && dx<clip[2] && dy>=clip[1] && dy<clip[3])
                    raster[dy*width+dx]=background.anIntArray1451[index];
            }
    }

    private static int canvasHeight(Background background) throws Exception {
        Field field = Background.class.getDeclaredField("anInt1457"); field.setAccessible(true);
        return field.getInt(background);
    }

    private static Background canvasFixture(int width, int height, int x, int y,
            int canvasWidth, int canvasHeight) throws Exception {
        Background background = new Background(archive(), "small", 0);
        background.anInt1452 = width; background.anInt1453 = height;
        background.anInt1456 = canvasWidth;
        Field field = Background.class.getDeclaredField("anInt1457"); field.setAccessible(true);
        field.setInt(background, canvasHeight);
        background.offsetX = x; background.offsetY = y;
        background.aByteArray1450 = new byte[width * height];
        for (int i = 0; i < background.aByteArray1450.length; i++)
            background.aByteArray1450[i] = i % 4 == 3 ? 0 : (byte) (i % 2 == 0 ? i + 1 : 128 + i);
        return background;
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
