import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Base64;
import org.junit.Test;

public class BackgroundPaletteTest {
    @Test
    public void firstArgumentAddsRedLevelsWithoutChangingGreenOrBlue() {
        Background image = image(0x123456, 0x001122, 0xfeabcd, 0xffffff, 0);
        image.adjustPaletteRgb(1, 0, 0);
        assertArrayEquals(new int[] {0x133456, 0x011122, 0xffabcd, 0xffffff, 0x010000}, image.anIntArray1451);
    }

    @Test
    public void secondArgumentAddsGreenLevelsWithoutChangingRedOrBlue() {
        Background image = image(0x123456, 0x001122, 0xabfecd, 0xffffff, 0);
        image.adjustPaletteRgb(0, 1, 0);
        assertArrayEquals(new int[] {0x123556, 0x001222, 0xabffcd, 0xffffff, 0x000100}, image.anIntArray1451);
    }

    @Test
    public void thirdArgumentAddsBlueLevelsWithoutChangingRedOrGreen() {
        Background image = image(0x123456, 0x001122, 0xabcdfe, 0xffffff, 0);
        image.adjustPaletteRgb(0, 0, 1);
        assertArrayEquals(new int[] {0x123457, 0x001123, 0xabcdff, 0xffffff, 1}, image.anIntArray1451);
    }

    @Test
    public void signedDeltasSaturateEachChannelRatherThanWrappingOrCarrying() {
        Background image = image(0x010203, 0xfefdfc, 0x000000, 0xffffff, 0x804020);
        image.adjustPaletteRgb(-2, 2, -4);
        assertArrayEquals(new int[] {0x000400, 0xfcfff8, 0x000200, 0xfdfffb, 0x7e421c}, image.anIntArray1451);
        image = image(0x010203, 0xfefdfc, 0x000000, 0xffffff, 0x804020);
        image.adjustPaletteRgb(256, -256, 255);
        assertArrayEquals(new int[] {0xff00ff, 0xff00ff, 0xff00ff, 0xff00ff, 0xff00ff}, image.anIntArray1451);
    }

    @Test
    public void exactEndpointsAndAdjacentValuesClampIndependently() {
        Background image = image(0x0a0a0a, 0x090909, 0x0b0b0b, 0xf5f5f5, 0xf6f6f6);
        image.adjustPaletteRgb(-10, 10, 0);
        assertArrayEquals(new int[] {0x00140a, 0x001309, 0x01150b, 0xebfff5, 0xecfff6}, image.anIntArray1451);
    }

    @Test
    public void mutatesEveryPaletteEntryInPlaceAndPreservesIndexedImageStorage() {
        Background image = image(0, 0x123456, 0xabcdef, 0x804020, 0xffffff);
        int[] palette = image.anIntArray1451;
        byte[] pixels = image.aByteArray1450;
        byte[] before = pixels.clone();
        image.adjustPaletteRgb(3, -2, 5);
        assertSame(palette, image.anIntArray1451);
        assertArrayEquals(new int[] {0x030005, 0x15325b, 0xaecbf4, 0x833e25, 0xfffdff}, palette);
        assertSame(pixels, image.aByteArray1450);
        assertArrayEquals(before, pixels);
        assertEquals(2, image.anInt1452);
        assertEquals(2, image.anInt1453);
        assertEquals(5, image.anInt1456);
        assertEquals(2, image.offsetX);
        assertEquals(3, image.offsetY);
    }

    @Test
    public void deltasAccumulateAndClippingMakesTheOperationLossy() {
        Background image = image(0, 0x102030, 0xffffff, 0xfafafa, 0x050505);
        image.adjustPaletteRgb(0, 0, 0);
        assertArrayEquals(new int[] {0, 0x102030, 0xffffff, 0xfafafa, 0x050505}, image.anIntArray1451);
        image.adjustPaletteRgb(10, 10, 10);
        image.adjustPaletteRgb(-10, -10, -10);
        assertArrayEquals(new int[] {0, 0x102030, 0xf5f5f5, 0xf5f5f5, 0x050505}, image.anIntArray1451);
    }

    @Test
    public void extremeDeltasRetainExistingIntegerOverflowBehavior() {
        // Preservation counterexample: do not claim mathematically saturating addition for all ints.
        Background image = image(0, 0x010101, 0xffffff, 0x804020, 0x102030);
        image.adjustPaletteRgb(Integer.MAX_VALUE, Integer.MIN_VALUE, 0);
        assertArrayEquals(new int[] {0xff0000, 0x000001, 0x0000ff, 0x000020, 0x000030}, image.anIntArray1451);
    }


    @Test
    public void paletteTraversalIncludesUnusedZeroAndRepeatedEntries() {
        int[][] deltas = {{1,0,0}, {0,1,0}, {0,0,1}};
        int[][] expected = {
            {0x010000,0x133456,0x020101,0x133456,0x010000},
            {0x000100,0x123556,0x010201,0x123556,0x000100},
            {0x000001,0x123457,0x010102,0x123457,0x000001}};
        for (int channel = 0; channel < deltas.length; channel++) {
            Background b = image();
            System.arraycopy(new int[] {0,0x123456,0x010101,0x123456,0xff000000}, 0, b.anIntArray1451, 0, 5);
            b.aByteArray1450 = new byte[] {1,1,1,1};
            int[] storage = b.anIntArray1451;
            b.adjustPaletteRgb(deltas[channel][0], deltas[channel][1], deltas[channel][2]);
            assertSame(storage, b.anIntArray1451);
            assertArrayEquals(expected[channel], b.anIntArray1451);
            assertArrayEquals(new byte[] {1,1,1,1}, b.aByteArray1450);
        }
    }

    private static Background image(int... colors) {
        // Independently generated pass-005 archive: 5x6 canvas, 2x2 at (2,3), five-entry RGB palette.
        Background image = new Background(new StreamLoader(Base64.getDecoder().decode(
                "AABgAABtMUFZJlNZKILEYgAADWtV/6QQICAAAAJAABAKAQAAQAAAgAGgAFCgAxGmmjQaaII2kD0j1FlFbrbt36ul5XDigcNmqcEMRoi5Aby3NEoQNwoUk2ZqFPSM/KkamMVmYitWIIP4u5IpwoSBRBYjEA==")), "small", 0);
        System.arraycopy(colors, 0, image.anIntArray1451, 0, colors.length);
        return image;
    }
}
