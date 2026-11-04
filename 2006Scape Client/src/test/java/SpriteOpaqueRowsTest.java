import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.Arrays;
import org.junit.Test;

public class SpriteOpaqueRowsTest {
    private static void copy(int destinationIndex, int width, int rows, int sourceSkip,
            int sourceIndex, int destinationSkip, int[] source, int[] destination) throws Exception {
        Method method = Sprite.class.getDeclaredMethod("copyOpaqueRows", int.class, int.class,
                int.class, int.class, int.class, int.class, int[].class, int[].class);
        method.setAccessible(true);
        // Deliberately unrelated sprite storage: this helper uses its array arguments.
        method.invoke(new Sprite(0, 0), destinationIndex, width, rows, sourceSkip,
                sourceIndex, destinationSkip, source, destination);
    }

    private static int[] filled(int size) {
        int[] result = new int[size];
        Arrays.fill(result, 9);
        return result;
    }

    @Test
    public void copiesRawIntegersIncludingZeroAndHighBitsIntoDestinationArgument() throws Exception {
        int[] source = {0, 0x804020, 0x7f123456, -1};
        int[] destination = filled(6);
        copy(1, 4, 1, 0, 0, 0, source, destination);
        assertArrayEquals(new int[] {9, 0, 0x804020, 0x7f123456, -1, 9}, destination);
        assertArrayEquals(new int[] {0, 0x804020, 0x7f123456, -1}, source);
    }

    @Test
    public void sourceAndDestinationStartIndicesMoveIndependentlyInIntegerElements() throws Exception {
        int[] source = {10, 11, 12, 13, 14};
        int[] destination = filled(7);
        copy(1, 2, 1, 0, 0, 0, source, destination);
        assertArrayEquals(new int[] {9,10,11,9,9,9,9}, destination);
        destination = filled(7);
        copy(1, 2, 1, 0, 1, 0, source, destination);
        assertArrayEquals(new int[] {9,11,12,9,9,9,9}, destination);
        destination = filled(7);
        copy(2, 2, 1, 0, 1, 0, source, destination);
        assertArrayEquals(new int[] {9,9,11,12,9,9,9}, destination);
    }

    @Test
    public void rowSkipsAreAdditionalElementsAfterWidthAndVaryIndependently() throws Exception {
        int[] source = {10,11,12,13,14,15,16,17,18,19};
        int[] destination = filled(10);
        copy(1, 2, 3, 0, 1, 0, source, destination);
        assertArrayEquals(new int[] {9,11,12,13,14,15,16,9,9,9}, destination);
        destination = filled(10);
        copy(1, 2, 3, 1, 1, 0, source, destination);
        assertArrayEquals(new int[] {9,11,12,14,15,17,18,9,9,9}, destination);
        destination = filled(10);
        copy(1, 2, 3, 1, 1, 1, source, destination);
        assertArrayEquals(new int[] {9,11,12,9,14,15,9,17,18,9}, destination);
        assertArrayEquals(new int[] {10,11,12,13,14,15,16,17,18,19}, source);
    }

    @Test
    public void widthAndRowCountBoundTheCopyIncludingUnrolledLoopRemainders() throws Exception {
        int[] source = {10,11,12,13,14,15,16,17,18,19,20,21};
        int[][] expected = {
            {9,9,9,9,9,9,9,9,9,9,9,9,9,9},
            {9,10,9,9,9,9,9,16,9,9,9,9,9,9},
            {9,10,11,9,9,9,9,16,17,9,9,9,9,9},
            {9,10,11,12,9,9,9,16,17,18,9,9,9,9},
            {9,10,11,12,13,9,9,16,17,18,19,9,9,9},
            {9,10,11,12,13,14,9,16,17,18,19,20,9,9}
        };
        for (int width = 0; width <= 5; width++) {
            // Change only width; all other arguments stay fixed.
            int[] singleRow = filled(14);
            copy(1, width, 1, 0, 0, 0, source, singleRow);
            int[] expectedSingleRow = expected[width].clone();
            Arrays.fill(expectedSingleRow, 7, 14, 9);
            assertArrayEquals("single row width=" + width, expectedSingleRow, singleRow);

            // Keep row starts six elements apart to expose both remainder and stride errors.
            int[] destination = filled(14);
            copy(1, width, 2, 6 - width, 0, 6 - width, source, destination);
            assertArrayEquals("width=" + width, expected[width], destination);
        }
        int[] destination = filled(5);
        copy(0, 2, 0, 0, 0, 0, source, destination);
        assertArrayEquals(new int[] {9,9,9,9,9}, destination);
        copy(0, 2, 1, 0, 0, 0, source, destination);
        assertArrayEquals(new int[] {10,11,9,9,9}, destination);
        destination = filled(5);
        copy(0, 2, 2, 0, 0, 0, source, destination);
        assertArrayEquals(new int[] {10,11,12,13,9}, destination);
        // Exact end of both arrays: no sentinel capacity is required.
        destination = filled(4);
        copy(0, 2, 2, 0, 0, 0, new int[] {10,11,12,13}, destination);
        assertArrayEquals(new int[] {10,11,12,13}, destination);
    }
}
