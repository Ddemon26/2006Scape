import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

public class StreamByteTransformsTest {
    private void write(int kind, Stream stream, int value) {
        if (kind == 0) stream.writeNegatedByte(value);
        else stream.writeByte128MinusValue(value);
    }

    private int read(int kind, Stream stream) {
        switch (kind) {
            case 0: return stream.readUnsignedNegatedByte();
            case 1: return stream.readUnsignedByte128MinusValue();
            case 2: return stream.readSignedNegatedByte();
            default: return stream.readSignedByte128MinusValue();
        }
    }

    private Stream at(byte[] bytes, int cursor) {
        Stream stream = new Stream(bytes);
        stream.currentOffset = cursor;
        stream.bitOffset = 37;
        return stream;
    }

    // Residue arithmetic uses long to avoid Java int overflow; it specifies
    // the byte-domain mapping independently of the implementation's casts/masks.
    private int residue(long value) { return (int) Math.floorMod(value, 256L); }
    private int signed(int value) { return value < 128 ? value : value - 256; }

    @Test public void everyWireByteHasIndependentSignedAndUnsignedMeaning() {
        for (int wire = 0; wire < 256; wire++) {
            for (int cursor : new int[] {0, 1, 7}) {
                for (int kind = 0; kind < 4; kind++) {
                    byte[] bytes = new byte[9];
                    Arrays.fill(bytes, (byte) 91);
                    bytes[cursor] = (byte) wire;
                    byte[] before = bytes.clone();
                    Stream stream = at(bytes, cursor);
                    int expected = residue((kind % 2 == 0 ? 0L : 128L) - wire);
                    if (kind >= 2) expected = signed(expected);
                    assertEquals("kind=" + kind + " wire=" + wire, expected, read(kind, stream));
                    assertEquals(cursor + 1, stream.currentOffset);
                    assertEquals(37, stream.bitOffset);
                    assertSame(bytes, stream.buffer);
                    assertArrayEquals(before, bytes);
                }
            }
        }
    }

    @Test public void writersTransformLowByteOfFullIntAndTouchOneCell() {
        int[] extremes = {Integer.MIN_VALUE, Integer.MIN_VALUE + 1, -65537, -257,
            -256, -255, -129, -128, -1, 256, 257, 65535, Integer.MAX_VALUE};
        for (int kind = 0; kind < 2; kind++) {
            for (int index = 0; index < 256 + extremes.length; index++) {
                int value = index < 256 ? index : extremes[index - 256];
                for (int cursor : new int[] {0, 1, 7}) {
                    byte[] bytes = new byte[9];
                    Arrays.fill(bytes, (byte) 91);
                    byte[] expected = bytes.clone();
                    expected[cursor] = (byte) residue((kind == 0 ? 0L : 128L) - value);
                    Stream stream = at(bytes, cursor);
                    write(kind, stream, value);
                    assertArrayEquals(expected, bytes);
                    assertSame(bytes, stream.buffer);
                    assertEquals(cursor + 1, stream.currentOffset);
                    assertEquals(37, stream.bitOffset);
                }
            }
        }
    }

    @Test public void fixedVectorsDistinguishNegationComplementAndSignedness() {
        int[] wire = {0, 1, 127, 128, 129, 255};
        int[][] expected = {
            {0, 255, 129, 128, 127, 1}, {128, 127, 1, 0, 255, 129},
            {0, -1, -127, -128, 127, 1}, {-128, 127, 1, 0, -1, -127}
        };
        for (int kind = 0; kind < 4; kind++) {
            for (int index = 0; index < wire.length; index++) {
                assertEquals(expected[kind][index], read(kind, at(new byte[] {(byte) wire[index]}, 0)));
                if (kind < 2) {
                    Stream stream = at(new byte[1], 0);
                    write(kind, stream, wire[index]);
                    assertEquals(expected[kind][index], stream.buffer[0] & 255);
                }
            }
        }
    }

    @Test public void matchingTransformsAreInvolutionsButOtherTransformDiffersBy128() {
        for (int value = 0; value < 256; value++) {
            for (int writer = 0; writer < 2; writer++) {
                Stream stream = at(new byte[1], 0);
                write(writer, stream, value);
                stream.currentOffset = 0;
                assertEquals(value, read(writer, stream));
                stream.currentOffset = 0;
                assertEquals((value + 128) % 256, read(1 - writer, stream));
                stream.currentOffset = 0;
                assertEquals(signed(value), read(writer + 2, stream));
            }
        }
    }

    @Test public void consecutiveCallsConsumeExactlyOneByteIncludingExactEnd() {
        for (int kind = 0; kind < 6; kind++) {
            Stream stream = at(new byte[] {91, 0, 1}, 1);
            if (kind < 2) { write(kind, stream, 0); write(kind, stream, 1); }
            else { read(kind - 2, stream); read(kind - 2, stream); }
            assertEquals(3, stream.currentOffset);
            assertEquals(91, stream.buffer[0]);
            assertEquals(37, stream.bitOffset);
        }
    }

    @Test public void invalidAccessRetainsPostincrementAndDoesNotMutateBytes() {
        for (int kind = 0; kind < 6; kind++) {
            for (int cursor : new int[] {-1, 2, Integer.MAX_VALUE}) {
                byte[] bytes = {11, 22};
                Stream stream = at(bytes, cursor);
                try {
                    if (kind < 2) write(kind, stream, 127); else read(kind - 2, stream);
                    fail("out of bounds");
                } catch (ArrayIndexOutOfBoundsException expected) { }
                assertEquals(cursor + 1, stream.currentOffset);
                assertEquals(37, stream.bitOffset);
                assertArrayEquals(new byte[] {11, 22}, bytes);
            }
            Stream stream = at(null, 3);
            try {
                if (kind < 2) write(kind, stream, 127); else read(kind - 2, stream);
                fail("null buffer");
            } catch (NullPointerException expected) { }
            assertEquals(4, stream.currentOffset);
            assertEquals(37, stream.bitOffset);
            assertNull(stream.buffer);
        }
    }
}
