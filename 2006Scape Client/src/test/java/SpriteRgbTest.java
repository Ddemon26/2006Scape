import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

public class SpriteRgbTest {
    @Test
    public void firstArgumentAddsRedChannelLevels() {
        Sprite s = image(0x123456, 0xfeabcd, 0xffffff, 0);
        s.adjustRgb(1, 0, 0);
        assertArrayEquals(new int[] {0x133456, 0xffabcd, 0xffffff, 0}, s.pixels);
    }

    @Test
    public void secondArgumentAddsGreenChannelLevels() {
        Sprite s = image(0x123456, 0xabfecd, 0xffffff, 0);
        s.adjustRgb(0, 1, 0);
        assertArrayEquals(new int[] {0x123556, 0xabffcd, 0xffffff, 0}, s.pixels);
    }

    @Test
    public void thirdArgumentAddsBlueChannelLevels() {
        Sprite s = image(0x123456, 0xabcdfe, 0xffffff, 0);
        s.adjustRgb(0, 0, 1);
        assertArrayEquals(new int[] {0x123457, 0xabcdff, 0xffffff, 0}, s.pixels);
    }

    @Test
    public void zeroPixelsRemainTransparentButZeroChannelsBecomeOne() {
        Sprite s = image(0, 0x000001, 0x010000, 0x000100);
        s.adjustRgb(0, 0, 0);
        assertArrayEquals(new int[] {0, 0x010101, 0x010101, 0x010101}, s.pixels);
        s.adjustRgb(255, 255, 255);
        assertArrayEquals(new int[] {0, 0xffffff, 0xffffff, 0xffffff}, s.pixels);
    }

    @Test
    public void signedDeltasClampIndependentlyWithoutCarry() {
        Sprite s = image(0x010203, 0xfefdfc, 0xffffff, 0x804020);
        s.adjustRgb(-2, 2, -4);
        assertArrayEquals(new int[] {0x010401, 0xfcfff8, 0xfdfffb, 0x7e421c}, s.pixels);
        s.adjustRgb(256, -256, 255);
        assertArrayEquals(new int[] {0xff01ff, 0xff01ff, 0xff01ff, 0xff01ff}, s.pixels);
    }

    @Test
    public void exactEndpointsAndAdjacentLevels() {
        Sprite s = image(0x0a0a0a, 0x0b0b0b, 0x0c0c0c, 0xf4f4f4, 0xf5f5f5, 0xf6f6f6);
        s.adjustRgb(-10, 10, 0);
        assertArrayEquals(new int[] {0x01140a, 0x01150b, 0x02160c, 0xeafef4, 0xebfff5, 0xecfff6}, s.pixels);
    }

    @Test
    public void mutatesSamePixelArrayWithoutChangingGeometryOrDrawingTarget() {
        Sprite s = image(0, 0x123456, 0xabcdef, 0xffffff);
        s.offsetX = 2; s.offsetY = 3; s.trimWidth = 9; s.trimHeight = 8;
        int[] pixels = s.pixels;
        int[] target = {0x556677};
        DrawingArea.initDrawingArea(1, 1, target);
        s.adjustRgb(3, -2, 5);
        assertSame(pixels, s.pixels);
        assertArrayEquals(new int[] {0, 0x15325b, 0xaecbf4, 0xfffdff}, pixels);
        assertSame(target, DrawingArea.pixels);
        assertArrayEquals(new int[] {0x556677}, target);
        assertEquals(4, s.width); assertEquals(1, s.height);
        assertEquals(9, s.trimWidth); assertEquals(8, s.trimHeight);
        assertEquals(2, s.offsetX); assertEquals(3, s.offsetY);
    }

    @Test
    public void adjustmentsAccumulateAndClippingLosesInformation() {
        Sprite s = image(0, 0x102030, 0xffffff, 0x010101);
        s.adjustRgb(10, 10, 10);
        s.adjustRgb(-10, -10, -10);
        assertArrayEquals(new int[] {0, 0x102030, 0xf5f5f5, 0x010101}, s.pixels);
        s.adjustRgb(-255, -255, -255);
        assertArrayEquals(new int[] {0, 0x010101, 0x010101, 0x010101}, s.pixels);
    }

    @Test
    public void extremeIntegerAndHighByteCounterexamplesRemainUnchanged() {
        // Preservation limits: Java int overflow occurs before clamping; this is not an alpha operation.
        Sprite s = image(0, 1, 0x010101, 0xffffff, 0xff000000);
        s.adjustRgb(Integer.MAX_VALUE, Integer.MIN_VALUE, 0);
        assertArrayEquals(new int[] {0, 0xff0101, 0x010101, 0x0101ff, 0xff0101}, s.pixels);
        s = image(0xff123456, 0xff000000);
        s.adjustRgb(0, 0, 0);
        assertArrayEquals(new int[] {0x123456, 0x010101}, s.pixels);
    }

    private static Sprite image(int... colors) {
        Sprite s = new Sprite(colors.length, 1);
        System.arraycopy(colors, 0, s.pixels, 0, colors.length);
        return s;
    }
}
