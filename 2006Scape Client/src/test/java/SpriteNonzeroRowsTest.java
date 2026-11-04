import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.Arrays;
import org.junit.Test;

public class SpriteNonzeroRowsTest {
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
}
