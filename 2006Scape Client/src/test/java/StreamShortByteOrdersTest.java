import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

public class StreamShortByteOrdersTest {
    private static void write(Stream s, int kind, int value) {
        if (kind == 0) s.writeShortBigEndian(value);
        else if (kind == 1) s.writeWaveShortLittleEndian(value);
        else s.writeShortLittleEndian(value);
    }

    private static int read(Stream s, int kind) {
        if (kind == 0) return s.readUnsignedShortBigEndian();
        if (kind == 1) return s.readSignedShortBigEndian();
        if (kind == 2) return s.readUnsignedShortLittleEndian();
        return s.readSignedShortLittleEndian();
    }

    private static ByteOrder order(int kind) {
        return kind < 2 ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
    }

    @Test public void everySixteenBitPatternIsWrittenInTheClaimedOrder() {
        for (int value = 0; value <= 65535; value++) {
            for (int kind = 0; kind < 3; kind++) {
                byte[] bytes = new byte[7]; Arrays.fill(bytes, (byte) 0x59);
                byte[] expected = bytes.clone();
                ByteBuffer.wrap(expected).order(kind == 0 ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN).putShort(2, (short) value);
                Stream s = new Stream(bytes); s.currentOffset = 2; s.bitOffset = 37;
                write(s, kind, value);
                assertArrayEquals(expected, bytes); assertSame(bytes, s.buffer);
                assertEquals(4, s.currentOffset); assertEquals(37, s.bitOffset);
            }
        }
    }

    @Test public void everyWirePatternHasIndependentSignedAndUnsignedInterpretations() {
        for (int pattern = 0; pattern <= 65535; pattern++) {
            byte[] bytes = ByteBuffer.allocate(7).putShort(2, (short) pattern).array();
            byte[] unchanged = bytes.clone();
            for (int kind = 0; kind < 4; kind++) {
                short signed = ByteBuffer.wrap(bytes).order(order(kind)).getShort(2);
                int expected = kind % 2 == 0 ? Short.toUnsignedInt(signed) : signed;
                Stream s = new Stream(bytes); s.currentOffset = 2; s.bitOffset = 37;
                assertEquals(expected, read(s, kind));
                assertArrayEquals(unchanged, bytes); assertSame(bytes, s.buffer);
                assertEquals(4, s.currentOffset); assertEquals(37, s.bitOffset);
            }
        }
    }

    @Test public void writersTruncateToLowSixteenBitsRatherThanClampOrReject() {
        int[] values = {Integer.MIN_VALUE, Integer.MAX_VALUE, -65537, -65536, -32769, -32768, -1, 0, 32767, 32768, 65535, 65536, 65537, 0x12345678};
        for (int value : values) for (int kind = 0; kind < 3; kind++) {
            Stream s = new Stream(new byte[2]); write(s, kind, value);
            byte[] expected = ByteBuffer.allocate(2).order(kind == 0 ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN).putShort((short) value).array();
            assertArrayEquals(expected, s.buffer); assertEquals(2, s.currentOffset);
        }
    }

    @Test public void asymmetricBytesDistinguishOrdersAndSignedBoundary() {
        byte[] bytes = {(byte) 0x80, 1};
        int[] expected = {32769, -32767, 384, 384};
        for (int kind = 0; kind < 4; kind++) assertEquals(expected[kind], read(new Stream(bytes), kind));
        byte[] reversed = {1, (byte) 0x80};
        int[] opposite = {384, 384, 32769, -32767};
        for (int kind = 0; kind < 4; kind++) assertEquals(opposite[kind], read(new Stream(reversed), kind));
        for (int kind = 0; kind < 4; kind++) {
            assertEquals(kind % 2 == 0 ? 65535 : -1, read(new Stream(new byte[]{-1, -1}), kind));
            assertEquals(0, read(new Stream(new byte[]{0, 0}), kind));
        }
    }

    @Test public void adjacentValuesUseExactlyTwoBytesAtDifferentOffsetsWithoutKeys() {
        for (int start : new int[]{0, 1, 3, 7}) for (int kind = 0; kind < 3; kind++) {
            Stream s = new Stream(new byte[start + 6]); s.currentOffset = start; s.bitOffset = -17;
            ISAACRandomGen keys = new ISAACRandomGen(new int[]{17, 8, 3, 29});
            ISAACRandomGen reference = new ISAACRandomGen(new int[]{17, 8, 3, 29}); s.encryption = keys;
            for (int value : new int[]{0x1280, -1, 0x7fff}) write(s, kind, value);
            assertEquals(start + 6, s.currentOffset); assertEquals(-17, s.bitOffset);
            s.currentOffset = start;
            int reader = kind == 0 ? 0 : 2;
            for (int value : new int[]{0x1280, 65535, 0x7fff}) assertEquals(value, read(s, reader));
            assertEquals(start + 6, s.currentOffset); assertEquals(-17, s.bitOffset);
            assertSame(keys, s.encryption); assertEquals(reference.getNextKey(), keys.getNextKey());
        }
    }

    @Test public void uncheckedWriterFailuresRetainPartialWritesAndPostincrement() {
        for (int kind = 0; kind < 3; kind++) for (int start : new int[]{-1, 0, 1, Integer.MAX_VALUE}) {
            byte[] bytes = {0x55}; Stream s = new Stream(bytes); s.currentOffset = start; s.bitOffset = 11;
            try { write(s, kind, 0x1234); fail("short buffer"); } catch (ArrayIndexOutOfBoundsException expected) { }
            assertEquals(start + (start == 0 ? 2 : 1), s.currentOffset);
            assertEquals(start == 0 ? (kind == 0 ? 0x12 : 0x34) : 0x55, bytes[0] & 255);
            assertEquals(11, s.bitOffset);
        }
        for (int kind = 0; kind < 3; kind++) {
            Stream s = new Stream(null); s.currentOffset = 4;
            try { write(s, kind, 1); fail("null buffer"); } catch (NullPointerException expected) { }
            assertEquals(5, s.currentOffset);
        }
    }

    @Test public void uncheckedReaderFailuresAdvanceByTwoBeforeAnyAccess() {
        for (int kind = 0; kind < 4; kind++) for (int start : new int[]{-1, 0, 1, Integer.MAX_VALUE}) {
            byte[] bytes = {0x55}; Stream s = new Stream(bytes); s.currentOffset = start; s.bitOffset = 11;
            try { read(s, kind); fail("short buffer"); } catch (ArrayIndexOutOfBoundsException expected) { }
            assertEquals(start + 2, s.currentOffset); assertEquals(0x55, bytes[0]); assertEquals(11, s.bitOffset);
        }
        for (int kind = 0; kind < 4; kind++) {
            Stream s = new Stream(null); s.currentOffset = 4;
            try { read(s, kind); fail("null buffer"); } catch (NullPointerException expected) { }
            assertEquals(6, s.currentOffset);
        }
    }
}
