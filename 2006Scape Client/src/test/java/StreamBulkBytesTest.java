import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

public class StreamBulkBytesTest {
    private void copy(int kind, Stream stream, byte[] array, int offset, int length) {
        if (kind == 0) stream.writeBytes(array, length, offset);
        if (kind == 1) stream.readBytes(length, offset, array);
        if (kind == 2) stream.writeReversedBytesAdd128(offset, array, length);
        if (kind == 3) stream.readReversedBytes(length, offset, array);
    }

    private byte[] filled(int size) {
        byte[] bytes = new byte[size];
        Arrays.fill(bytes, (byte) 91);
        return bytes;
    }

    @Test public void independentOffsetsLengthsAndAllByteValues() {
        for (int kind = 0; kind < 4; kind++) {
            for (int length = 0; length <= 256; length++) {
                for (int offset : new int[] {0, 1, 7}) {
                    for (int cursor : new int[] {0, 3}) {
                        byte[] array = filled(270), buffer = filled(270);
                        boolean write = kind == 0 || kind == 2;
                        for (int index = 0; index < length; index++) {
                            (write ? array : buffer)[(write ? offset : cursor) + index] = (byte) index;
                        }
                        byte[] input = (write ? array : buffer).clone();
                        byte[] expected = (write ? buffer : array).clone();
                        // A destination-oriented specification: select the input position
                        // for each output cell; no Stream loop or cursor expression copied.
                        for (int index = 0; index < length; index++) {
                            int inputIndex = (kind >= 2 ? length - 1 - index : index);
                            int value = input[(write ? offset : cursor) + inputIndex] & 255;
                            if (kind == 2) value = (value + 128) % 256;
                            expected[(write ? cursor : offset) + index] = (byte) value;
                        }
                        Stream stream = new Stream(buffer);
                        stream.currentOffset = cursor;
                        stream.bitOffset = 37;
                        copy(kind, stream, array, offset, length);
                        assertArrayEquals(expected, write ? buffer : array);
                        assertArrayEquals(input, write ? array : buffer);
                        assertSame(buffer, stream.buffer);
                        assertEquals(cursor + length, stream.currentOffset);
                        assertEquals(37, stream.bitOffset);
                    }
                }
            }
        }
    }

    @Test public void fixedVectorsDistinguishReverseBiasAndPlainCopy() {
        byte[] input = {(byte) 0, (byte) 1, (byte) 127, (byte) 128, (byte) 255};
        byte[][] expected = {
            {0, 1, 127, (byte)128, (byte)255},
            {0, 1, 127, (byte)128, (byte)255},
            {127, 0, (byte)255, (byte)129, (byte)128},
            {(byte)255, (byte)128, 127, 1, 0}
        };
        for (int kind = 0; kind < 4; kind++) {
            boolean write = kind == 0 || kind == 2;
            Stream stream = new Stream(write ? new byte[5] : input.clone());
            byte[] array = write ? input.clone() : new byte[5];
            copy(kind, stream, array, 0, 5);
            assertArrayEquals(expected[kind], write ? stream.buffer : array);
        }
    }

    @Test public void emptyAndNegativeLengthsDoNotDereferenceArrays() {
        for (int kind = 0; kind < 4; kind++) {
            for (int length : new int[] {0, -1, -7}) {
                Stream stream = new Stream(null);
                stream.currentOffset = 17;
                stream.bitOffset = 29;
                copy(kind, stream, null, 4, length);
                assertEquals(17, stream.currentOffset);
                assertEquals(29, stream.bitOffset);
                assertNull(stream.buffer);
            }
        }
    }

    @Test public void exactEndAndConsecutiveCopiesConsumeOnlyLength() {
        for (int kind = 0; kind < 4; kind++) {
            Stream stream = new Stream(new byte[] {10, 20, 30, 40});
            byte[] array = {1, 2, 3, 4};
            copy(kind, stream, array, 1, 1);
            assertEquals(1, stream.currentOffset);
            copy(kind, stream, array, 1, 3);
            assertEquals(4, stream.currentOffset);
        }
    }

    @Test public void aliasingUsesSequentialAssignmentsRatherThanSnapshotCopy() {
        byte[][] expected = {
            {1,1,1,1,5}, {1,1,1,1,5},
            {1,(byte)131,(byte)3,(byte)129,5}, {1,2,2,1,5}
        };
        for (int kind = 0; kind < 4; kind++) {
            byte[] bytes = {1,2,3,4,5};
            Stream stream = new Stream(bytes);
            boolean write = kind == 0 || kind == 2;
            stream.currentOffset = write ? 1 : 0;
            copy(kind, stream, bytes, write ? 0 : 1, 3);
            assertArrayEquals(expected[kind], bytes);
            assertEquals(write ? 4 : 3, stream.currentOffset);
        }
    }

    @Test public void failuresRetainPartialWritesAndPostincrementOrder() {
        for (int kind = 0; kind < 4; kind++) {
            Stream stream = new Stream(new byte[] {10,20});
            byte[] array = {1,2};
            try { copy(kind, stream, array, 0, 3); fail("out of bounds"); }
            catch (ArrayIndexOutOfBoundsException expected) { }
            if (kind == 0) { assertEquals(3, stream.currentOffset); assertArrayEquals(new byte[]{1,2},stream.buffer); }
            if (kind == 1) { assertEquals(3, stream.currentOffset); assertArrayEquals(new byte[]{10,20},array); }
            if (kind == 2) { assertEquals(1, stream.currentOffset); assertArrayEquals(new byte[]{10,20},stream.buffer); }
            if (kind == 3) { assertEquals(1, stream.currentOffset); assertArrayEquals(new byte[]{1,2},array); }
        }
    }
}
