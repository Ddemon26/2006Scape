import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.Arrays;
import org.junit.Test;

public class SpriteBlendRowsTest {
    private static void blend(int sourceIndex, int width, int[] destination,
            int[] source, int sourceSkip, int rows, int destinationSkip,
            int weight, int destinationIndex) throws Exception {
        Method method = Sprite.class.getDeclaredMethod("blendNonzeroRows", int.class,
                int.class, int[].class, int[].class, int.class, int.class,
                int.class, int.class, int.class);
        method.setAccessible(true);
        method.invoke(new Sprite(0, 0), sourceIndex, width, destination, source,
                sourceSkip, rows, destinationSkip, weight, destinationIndex);
    }

    private static int[] filled(int size, int color) {
        int[] result = new int[size]; Arrays.fill(result, color); return result;
    }

    @Test public void sourceWeightUses256ScaleAndDestinationComplement() throws Exception {
        int[] weights = {0, 1, 64, 128, 192, 255, 256};
        int[] expected = {0x000000, 0x000000, 0x201008, 0x402010,
                0x603018, 0x7f3f1f, 0xff804020};
        for (int n = 0; n < weights.length; n++) {
            int[] destination = {0};
            blend(0, 1, destination, new int[] {0x804020}, 0, 1, 0, weights[n], 0);
            assertEquals("source weight=" + weights[n], expected[n], destination[0]);
        }
        int[] destination = {0x804020};
        blend(0, 1, destination, new int[] {0x204080}, 0, 1, 0, 64, 0);
        assertEquals(0x684038, destination[0]);
        destination[0] = 0x804020;
        blend(0, 1, destination, new int[] {0x204080}, 0, 1, 0, 192, 0);
        assertEquals(0x384068, destination[0]);
    }

    @Test public void zeroPreservesDestinationButNonzeroLowRgbZeroStillBlends() throws Exception {
        int[] source = {0, 0x01000000, 0xff000000, 1};
        int[] destination = filled(4, 0x204060);
        blend(0, 4, destination, source, 0, 1, 0, 128, 0);
        assertArrayEquals(new int[] {0x204060, 0x102030, 0x102030, 0x102030}, destination);
        assertArrayEquals(new int[] {0,0x01000000,0xff000000,1}, source);
        destination = filled(4, 0x123456);
        blend(0, 4, destination, source, 0, 1, 0, 256, 0);
        assertArrayEquals(new int[] {0x123456,0,0,1}, destination);
    }

    @Test public void channelsRoundDownAndSignedPackedOutputIsPreserved() throws Exception {
        int[] destination = {0x020406, 0, 0xabcdef};
        blend(0, 3, destination, new int[] {0x010305,0xff0000,0x123456}, 0, 1, 0, 128, 0);
        assertArrayEquals(new int[] {0x010305,0x7f0000,0x5e80a2}, destination);
        destination = new int[] {0,0};
        blend(0, 2, destination, new int[] {0xff0000,0xffffffff}, 0, 1, 0, 256, 0);
        assertArrayEquals(new int[] {0xffff0000,0xffffffff}, destination);
    }

    @Test public void sourceAndDestinationStartsVaryIndependently() throws Exception {
        int[] source = {0x204060,0,0x406080,0x6080a0};
        int[] destination = filled(5, 0);
        blend(0, 2, destination, source, 0, 1, 0, 128, 1);
        assertArrayEquals(new int[] {0,0x102030,0,0,0}, destination);
        destination = filled(5, 0);
        blend(1, 2, destination, source, 0, 1, 0, 128, 1);
        assertArrayEquals(new int[] {0,0,0x203040,0,0}, destination);
        destination = filled(5, 0);
        blend(1, 2, destination, source, 0, 1, 0, 128, 2);
        assertArrayEquals(new int[] {0,0,0,0x203040,0}, destination);
    }

    @Test public void rowSkipsAddArrayElementsAfterWidthIndependently() throws Exception {
        int[] source = {2,4,6,8,10,12,14,16,18};
        int[] destination = filled(9, 0);
        blend(0, 2, destination, source, 0, 3, 0, 128, 1);
        assertArrayEquals(new int[] {0,1,2,3,4,5,6,0,0}, destination);
        destination = filled(9, 0);
        blend(0, 2, destination, source, 1, 3, 0, 128, 1);
        assertArrayEquals(new int[] {0,1,2,4,5,7,8,0,0}, destination);
        destination = filled(9, 0);
        blend(0, 2, destination, source, 1, 3, 1, 128, 1);
        assertArrayEquals(new int[] {0,1,2,0,4,5,0,7,8}, destination);
    }

    @Test public void widthAndRowsBoundWritesAndEmptyShapesDoNothing() throws Exception {
        for (int width = 0; width <= 9; width++) {
            for (int rows = 0; rows <= 2; rows++) {
                int[] destination = filled(width * rows, 0x204060);
                int[] source = filled(width * rows, 0x6080a0);
                blend(0, width, destination, source, 0, rows, 0, 128, 0);
                assertArrayEquals(filled(width * rows, 0x406080), destination);
            }
        }
        blend(0, 0, new int[0], new int[0], 0, 3, 0, 128, 0);
        int[] destination = {7,7,7,7,7,7};
        blend(0, 2, destination, new int[] {2,4,6,8}, 0, 2, 1, 128, 0);
        assertArrayEquals(new int[] {4,5,7,6,7,7}, destination);
    }
}
