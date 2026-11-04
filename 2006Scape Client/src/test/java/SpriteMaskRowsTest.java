import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.Arrays;
import org.junit.Test;

public class SpriteMaskRowsTest {
    private static void copy(int[] source, int width, byte[] mask, int rows,
            int[] destination, int destinationSkip, int destinationIndex,
            int sourceSkip, int sourceIndex) throws Exception {
        Method method = Sprite.class.getDeclaredMethod("copyNonzeroRowsThroughZeroMask", int[].class,
                int.class, byte[].class, int.class, int[].class, int.class,
                int.class, int.class, int.class, int.class);
        method.setAccessible(true);
        // No sprite storage or global drawing target can supply these arrays.
        method.invoke(new Sprite(0, 0), source, width, mask, rows, destination,
                0x76543210, destinationSkip, destinationIndex, sourceSkip, sourceIndex);
    }

    private static int[] filled(int size) {
        int[] result = new int[size];
        Arrays.fill(result, 9);
        return result;
    }

    @Test
    public void copiesNonzeroSourceExactlyOnlyThroughZeroMaskBytes() throws Exception {
        int[] source = {0,1,0x804020,0x7f123456,-1,0x01000000,0};
        byte[] mask = new byte[9];
        int[] destination = filled(9);
        copy(source, 7, mask, 1, destination, 0, 1, 0, 0);
        assertArrayEquals(new int[] {9,9,1,0x804020,0x7f123456,-1,0x01000000,9,9}, destination);
        mask = new byte[] {0,0,1,-1,127,-128,0,0,0};
        destination = filled(9);
        copy(source, 7, mask, 1, destination, 0, 1, 0, 0);
        assertArrayEquals(new int[] {9,9,9,9,9,9,0x01000000,9,9}, destination);
        assertArrayEquals(new int[] {0,1,0x804020,0x7f123456,-1,0x01000000,0}, source);
        assertArrayEquals(new byte[] {0,0,1,-1,127,-128,0,0,0}, mask);
        destination = new int[] {20,21,22,23,24,25,26,27,28};
        copy(source, 7, mask, 1, destination, 0, 1, 0, 0);
        assertArrayEquals(new int[] {20,21,22,23,24,25,0x01000000,27,28}, destination);
    }

    @Test
    public void sourceAndDestinationStartsVaryIndependentlyAndMaskUsesDestinationIndex() throws Exception {
        int[] source = {10,11,12,13};
        byte[] mask = {1,0,1,0,0,0};
        int[] destination = filled(6);
        copy(source, 2, mask, 1, destination, 0, 1, 0, 0);
        assertArrayEquals(new int[] {9,10,9,9,9,9}, destination);
        destination = filled(6);
        copy(source, 2, mask, 1, destination, 0, 1, 0, 1);
        assertArrayEquals(new int[] {9,11,9,9,9,9}, destination);
        destination = filled(6);
        copy(source, 2, mask, 1, destination, 0, 2, 0, 1);
        assertArrayEquals(new int[] {9,9,9,12,9,9}, destination);
    }

    @Test
    public void rowSkipsAreAdditionalElementsAndVaryIndependently() throws Exception {
        int[] source = {10,11,12,13,14,15,16,17,18,19};
        byte[] mask = {1,0,1,0,0,1,0,0,0,0};
        int[] destination = filled(10);
        copy(source, 2, mask, 3, destination, 0, 1, 0, 1);
        assertArrayEquals(new int[] {9,11,9,13,14,9,16,9,9,9}, destination);
        destination = filled(10);
        copy(source, 2, mask, 3, destination, 0, 1, 1, 1);
        assertArrayEquals(new int[] {9,11,9,14,15,9,18,9,9,9}, destination);
        destination = filled(10);
        copy(source, 2, mask, 3, destination, 1, 1, 1, 1);
        assertArrayEquals(new int[] {9,11,9,9,14,9,9,17,18,9}, destination);
        assertArrayEquals(new int[] {10,11,12,13,14,15,16,17,18,19}, source);
        assertArrayEquals(new byte[] {1,0,1,0,0,1,0,0,0,0}, mask);
    }

    @Test
    public void zerosAndBlockedPixelsAdvanceBothIndicesInEverySlot() throws Exception {
        for (int slot = 0; slot < 9; slot++) {
            int[] source = {10,11,12,13,14,15,16,17,18};
            byte[] mask = new byte[11];
            int[] expected = {9,10,11,12,13,14,15,16,17,18,9};
            expected[slot + 1] = 9;
            int[] destination = filled(11);
            mask[slot + 1] = -1;
            copy(source, 9, mask, 1, destination, 0, 1, 0, 0);
            assertArrayEquals("blocked slot=" + slot, expected, destination);
            destination = filled(11);
            mask[slot + 1] = 0;
            source[slot] = 0;
            copy(source, 9, mask, 1, destination, 0, 1, 0, 0);
            assertArrayEquals("zero slot=" + slot, expected, destination);
        }
    }

    @Test
    public void widthsRowsAndExactEndsBoundWrites() throws Exception {
        int[] source = {10,11,12,13,14,15,16,17,18};
        int[] expected = {9,10,11,12,13,14,15,16,17,18,9};
        for (int width = 0; width <= 9; width++) {
            int[] destination = filled(11);
            int[] bounded = expected.clone();
            Arrays.fill(bounded, 1 + width, bounded.length, 9);
            copy(source, width, new byte[11], 1, destination, 0, 1, 0, 0);
            assertArrayEquals("width=" + width, bounded, destination);
        }
        int[] destination = filled(5);
        copy(source, 2, new byte[5], 0, destination, 0, 0, 0, 0);
        assertArrayEquals(new int[] {9,9,9,9,9}, destination);
        copy(source, 2, new byte[5], 1, destination, 0, 0, 0, 0);
        assertArrayEquals(new int[] {10,11,9,9,9}, destination);
        destination = filled(4);
        copy(source, 2, new byte[] {0,1,0,0}, 2, destination, 0, 0, 0, 0);
        assertArrayEquals(new int[] {10,9,12,13}, destination);
        copy(new int[0], 0, new byte[0], 3, new int[0], 0, 0, 0, 0);
        copy(new int[0], 9, new byte[0], 0, new int[0], 0, 0, 0, 0);
    }

    @Test public void multipleBlocksAndEveryTailResetAcrossMaskedStridedRows() throws Exception {
        int[] source = new int[48]; Arrays.fill(source, -7);
        for (int row = 0; row < 3; row++) for (int col = 0; col < 12; col++)
            source[2 + row * 15 + col] = (row + col) % 3 == 0 ? 0 : (row + 1) * 100 + col + 1;
        int[] unchanged = source.clone();
        byte[] mask = new byte[60];
        for (int i = 0; i < mask.length; i++) mask[i] = (byte) (i % 5 == 0 ? -128 : 0);
        byte[] unchangedMask = mask.clone();
        for (int width = 0; width <= 12; width++) for (int rows = 0; rows <= 3; rows++) {
            int[] actual = filled(60), expected = filled(60);
            for (int row = 0; row < rows; row++) for (int col = 0; col < width; col++) {
                int index = 3 + row * 17 + col;
                if ((row + col) % 3 != 0 && index % 5 != 0)
                    expected[index] = (row + 1) * 100 + col + 1;
            }
            copy(source, width, mask, rows, actual, 17 - width, 3, 15 - width, 2);
            assertArrayEquals("width=" + width + " rows=" + rows, expected, actual);
            assertArrayEquals(unchanged, source); assertArrayEquals(unchangedMask, mask);
        }
        for (int width = 8; width <= 12; width++) {
            int[] input = new int[width * 3], expected = filled(width * 3);
            byte[] gates = new byte[input.length];
            for (int i = 0; i < input.length; i++) {
                input[i] = i % 4 == 0 ? 0 : i + 100;
                gates[i] = (byte) (i % 5 == 0 ? 1 : 0);
                if (i % 4 != 0 && i % 5 != 0) expected[i] = i + 100;
            }
            int[] actual = filled(input.length);
            copy(input, width, gates, 3, actual, 0, 0, 0, 0);
            assertArrayEquals(expected, actual);
        }
    }

    @Test public void everySignedByteGateBlocksExceptZeroAndIncomingScratchIsIgnored() throws Exception {
        Method method = Sprite.class.getDeclaredMethod("copyNonzeroRowsThroughZeroMask", int[].class,
                int.class, byte[].class, int.class, int[].class, int.class,
                int.class, int.class, int.class, int.class);
        method.setAccessible(true);
        for (int incoming : new int[] {0, -1, 0x76543210}) for (int gate = -128; gate <= 127; gate++) {
            int[] input = {0, 0x01000000, -1, 0x7f123456, 0, 13, 0, 15, 16, 0, 18, 19};
            byte[] mask = new byte[12]; Arrays.fill(mask, (byte) gate);
            int[] actual = filled(12), expected = filled(12);
            if (gate == 0) for (int i = 0; i < input.length; i++) if (input[i] != 0) expected[i] = input[i];
            method.invoke(new Sprite(0, 0), input, 12, mask, 1, actual, incoming, 0, 0, 0, 0);
            assertArrayEquals("incoming=" + incoming + " gate=" + gate, expected, actual);
        }
    }
}
