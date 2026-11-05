import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

public class StreamSmartReadingTest {
    // Inverse encoders enumerate values, rather than duplicate the reader's branch.
    private static byte[] encoded(int stored, int width) throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream data = new DataOutputStream(bytes);
        if (width == 1) data.writeByte(stored);
        else data.writeShort(stored);
        return bytes.toByteArray();
    }

    private static void check(byte[] bytes, int expected, boolean signed) {
        byte[] buffer = new byte[bytes.length + 2];
        buffer[0] = 0x35;
        System.arraycopy(bytes, 0, buffer, 1, bytes.length);
        buffer[buffer.length - 1] = 0x6b;
        byte[] before = buffer.clone();
        Stream stream = new Stream(buffer);
        stream.currentOffset = 1;
        stream.bitOffset = 37;
        assertEquals(expected, signed ? stream.readSignedSmart() : stream.readUnsignedSmart());
        assertEquals(1 + bytes.length, stream.currentOffset);
        assertEquals(37, stream.bitOffset);
        assertSame(buffer, stream.buffer);
        assertArrayEquals(before, stream.buffer);
        assertEquals(0x6b, stream.readUnsignedByte());
    }

    @Test public void everySignedValueInBothRepresentationsHasBiasedEncoding() throws Exception {
        for (int value = -64; value <= 63; value++) {
            check(encoded(value + 64, 1), value, true);
        }
        for (int value = -16384; value <= 16383; value++) {
            check(encoded(value + 49152, 2), value, true);
        }
    }

    @Test public void everyUnsignedValueInBothRepresentationsHasBiasedEncoding() throws Exception {
        for (int value = 0; value <= 127; value++) {
            check(encoded(value, 1), value, false);
        }
        for (int value = 0; value <= 32767; value++) {
            check(encoded(value + 32768, 2), value, false);
        }
    }

    @Test public void fixedBoundariesDistinguishBiasesByteOrderAndTwosComplement() {
        byte[][] bytes = {{0}, {0x3f}, {0x40}, {0x7f}, {(byte) 0x80, 0},
                {(byte) 0x80, 1}, {(byte) 0x81, 0}, {(byte) 0xbf, (byte) 0xff},
                {(byte) 0xc0, 0}, {(byte) 0xc0, 1}, {(byte) 0xff, (byte) 0xff}};
        int[] signed = {-64, -1, 0, 63, -16384, -16383, -16128, -1, 0, 1, 16383};
        int[] unsigned = {0, 63, 64, 127, 0, 1, 256, 16383, 16384, 16385, 32767};
        for (int i = 0; i < bytes.length; i++) {
            check(bytes[i], signed[i], true);
            check(bytes[i], unsigned[i], false);
        }
    }

    @Test public void lowFirstByteConsumesOneRegardlessOfFollowingByte() {
        for (int second = 0; second <= 255; second++) {
            for (boolean signed : new boolean[] {false, true}) {
                Stream stream = new Stream(new byte[] {0x7f, (byte) second});
                assertEquals(signed ? 63 : 127, signed ? stream.readSignedSmart() : stream.readUnsignedSmart());
                assertEquals(1, stream.currentOffset);
                assertEquals(second, stream.readUnsignedByte());
            }
        }
    }

    @Test public void overlappingEncodingsAreAcceptedWithoutCanonicalization() throws Exception {
        for (int value = -64; value <= 63; value++) {
            check(encoded(value + 64, 1), value, true);
            check(encoded(value + 49152, 2), value, true);
        }
        for (int value = 0; value <= 127; value++) {
            check(encoded(value, 1), value, false);
            check(encoded(value + 32768, 2), value, false);
        }
    }

    @Test public void consecutiveMixedReadsUseByteCursorAndNeedNoTerminator() {
        Stream stream = new Stream(new byte[] {0x40, (byte) 0x80, 0,
                (byte) 0xbf, (byte) 0xff, 0x7f, (byte) 0xc0, 1});
        stream.bitOffset = 29;
        assertEquals(0, stream.readSignedSmart());
        assertEquals(1, stream.currentOffset);
        assertEquals(0, stream.readUnsignedSmart());
        assertEquals(3, stream.currentOffset);
        assertEquals(-1, stream.readSignedSmart());
        assertEquals(5, stream.currentOffset);
        assertEquals(127, stream.readUnsignedSmart());
        assertEquals(6, stream.currentOffset);
        assertEquals(1, stream.readSignedSmart());
        assertEquals(8, stream.currentOffset);
        assertEquals(29, stream.bitOffset);
        assertEquals(63, new Stream(new byte[] {0x7f}).readSignedSmart());
        assertEquals(127, new Stream(new byte[] {0x7f}).readUnsignedSmart());
        assertEquals(32767, new Stream(new byte[] {(byte) 0xff, (byte) 0xff}).readUnsignedSmart());
    }

    @Test public void peekFailuresDoNotAdvanceButTruncatedTwoByteReadAdvancesByTwo() {
        for (boolean signed : new boolean[] {false, true}) {
            expectBounds(new byte[0], 0, 0, signed);
            expectBounds(new byte[] {0x40}, 1, 1, signed);
            expectBounds(new byte[] {0x40}, -1, -1, signed);
            for (int first = 128; first <= 255; first++) {
                expectBounds(new byte[] {0x35, (byte) first}, 1, 3, signed);
            }
        }
    }

    private static void expectBounds(byte[] bytes, int start, int after, boolean signed) {
        Stream stream = new Stream(bytes);
        stream.currentOffset = start;
        stream.bitOffset = 37;
        byte[] before = Arrays.copyOf(bytes, bytes.length);
        try {
            if (signed) stream.readSignedSmart(); else stream.readUnsignedSmart();
            fail("Expected array bounds failure");
        } catch (ArrayIndexOutOfBoundsException expected) {
            assertEquals(after, stream.currentOffset);
            assertEquals(37, stream.bitOffset);
            assertArrayEquals(before, stream.buffer);
        }
    }
}
