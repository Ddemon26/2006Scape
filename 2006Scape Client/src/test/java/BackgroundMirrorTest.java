import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Base64;
import org.junit.Test;

public class BackgroundMirrorTest {
    @Test
    public void horizontalMirrorReversesColumnsAndPreservesRowsAndPalette() {
        Background image = asymmetric();
        int[] palette = image.anIntArray1451;
        int[] colors = palette.clone();
        image.flipHorizontally();
        assertArrayEquals(new byte[] {2, 1, 0, -1, 4, 3}, image.aByteArray1450);
        assertEquals(1, image.offsetX); // 5-wide canvas, 3-wide image, one pixel on each side.
        assertEquals(3, image.offsetY);
        assertDimensions(image, 3, 2, 5);
        assertSame(palette, image.anIntArray1451);
        assertArrayEquals(colors, image.anIntArray1451);
    }

    @Test
    public void verticalMirrorReversesRowsAndPreservesColumnsAndPalette() {
        Background image = asymmetric();
        int[] palette = image.anIntArray1451;
        int[] colors = palette.clone();
        image.flipVertically();
        assertArrayEquals(new byte[] {3, 4, -1, 0, 1, 2}, image.aByteArray1450);
        assertEquals(1, image.offsetX);
        assertEquals(1, image.offsetY); // 6-high canvas, 2-high image, original top margin 3.
        assertDimensions(image, 3, 2, 5);
        assertSame(palette, image.anIntArray1451);
        assertArrayEquals(colors, image.anIntArray1451);
    }

    @Test
    public void expandedImagesReflectAcrossTheFullCanvasIncludingTransparentMargins() {
        Background horizontal = asymmetric();
        horizontal.offsetX = 0;
        horizontal.flipHorizontally();
        assertEquals(2, horizontal.offsetX);
        horizontal.method357();
        assertArrayEquals(new byte[] {
                0,0,0,0,0, 0,0,0,0,0, 0,0,0,0,0,
                0,0,2,1,0, 0,0,-1,4,3, 0,0,0,0,0}, horizontal.aByteArray1450);

        Background vertical = asymmetric();
        vertical.flipVertically();
        vertical.method357();
        assertArrayEquals(new byte[] {
                0,0,0,0,0, 0,3,4,-1,0, 0,0,1,2,0,
                0,0,0,0,0, 0,0,0,0,0, 0,0,0,0,0}, vertical.aByteArray1450);
        assertDimensions(horizontal, 5, 6, 5);
        assertDimensions(vertical, 5, 6, 5);
    }

    @Test
    public void repeatingEitherMirrorRestoresPixelsAndOffsets() {
        Background horizontal = asymmetric();
        horizontal.offsetX = 0;
        horizontal.flipHorizontally();
        horizontal.flipHorizontally();
        assertArrayEquals(new byte[] {0, 1, 2, 3, 4, -1}, horizontal.aByteArray1450);
        assertEquals(0, horizontal.offsetX);
        assertEquals(3, horizontal.offsetY);

        Background vertical = asymmetric();
        vertical.flipVertically();
        vertical.flipVertically();
        assertArrayEquals(new byte[] {0, 1, 2, 3, 4, -1}, vertical.aByteArray1450);
        assertEquals(1, vertical.offsetX);
        assertEquals(3, vertical.offsetY);
    }

    @Test
    public void mirrorsComposeInEitherOrderWithoutTransposingTheImage() {
        Background hv = asymmetric();
        hv.flipHorizontally();
        hv.flipVertically();
        Background vh = asymmetric();
        vh.flipVertically();
        vh.flipHorizontally();
        assertArrayEquals(new byte[] {-1, 4, 3, 2, 1, 0}, hv.aByteArray1450);
        assertArrayEquals(hv.aByteArray1450, vh.aByteArray1450);
        assertDimensions(hv, 3, 2, 5);
        assertDimensions(vh, 3, 2, 5);
        assertEquals(1, hv.offsetX);
        assertEquals(1, hv.offsetY);
        assertEquals(hv.offsetX, vh.offsetX);
        assertEquals(hv.offsetY, vh.offsetY);
    }

    @Test
    public void onePixelAxesStillReflectPlacementBetweenCanvasEdges() {
        Background column = asymmetric();
        column.anInt1452 = 1;
        column.aByteArray1450 = new byte[] {1, 2};
        column.offsetX = 0;
        column.flipHorizontally();
        assertArrayEquals(new byte[] {1, 2}, column.aByteArray1450);
        assertEquals(4, column.offsetX);
        column.flipVertically();
        assertArrayEquals(new byte[] {2, 1}, column.aByteArray1450);
        assertEquals(1, column.offsetY);

        Background row = asymmetric();
        row.anInt1453 = 1;
        row.aByteArray1450 = new byte[] {1, 0, 2};
        row.offsetY = 0;
        row.flipVertically();
        assertArrayEquals(new byte[] {1, 0, 2}, row.aByteArray1450);
        assertEquals(5, row.offsetY);
        row.flipHorizontally();
        assertArrayEquals(new byte[] {2, 0, 1}, row.aByteArray1450);
    }

    @Test
    public void fullCanvasMirrorsKeepZeroOffsetsAndByteValues() {
        Background image = asymmetric();
        image.method357();
        image.flipHorizontally();
        image.flipVertically();
        assertDimensions(image, 5, 6, 5);
        assertEquals(0, image.offsetX);
        assertEquals(0, image.offsetY);
        assertArrayEquals(new byte[] {
                0,0,0,0,0, 0,-1,4,3,0, 0,2,1,0,0,
                0,0,0,0,0, 0,0,0,0,0, 0,0,0,0,0}, image.aByteArray1450);
    }

    private static Background asymmetric() {
        // Same independently generated archive as BackgroundOffsetTest: "small" canvas 5x6.
        Background image = new Background(new StreamLoader(Base64.getDecoder().decode(
                "AABgAABtMUFZJlNZKILEYgAADWtV/6QQICAAAAJAABAKAQAAQAAAgAGgAFCgAxGmmjQaaII2kD0j1FlFbrbt36ul5XDigcNmqcEMRoi5Aby3NEoQNwoUk2ZqFPSM/KkamMVmYitWIIP4u5IpwoSBRBYjEA==")), "small", 0);
        image.anInt1452 = 3;
        image.anInt1453 = 2;
        image.offsetX = 1;
        image.offsetY = 3;
        // Asymmetric rows distinguish mirrors from rotation/transposition; zero and 255 are bytes,
        // not colors to be remapped. Index 255 is used only in byte operations, never palette drawing.
        image.aByteArray1450 = new byte[] {0, 1, 2, 3, 4, -1};
        return image;
    }

    private static void assertDimensions(Background image, int width, int height, int canvasWidth) {
        assertEquals(width, image.anInt1452);
        assertEquals(height, image.anInt1453);
        assertEquals(canvasWidth, image.anInt1456);
    }
}
