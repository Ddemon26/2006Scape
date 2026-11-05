import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

public class StreamAdjustedShortTest {
    private static void write(Stream s, int kind, int value) {
        if (kind == 0) s.writeShortBigEndianAdd128(value);
        else s.writeShortLittleEndianAdd128(value);
    }

    private static int read(Stream s, int kind) {
        if (kind == 0) return s.readUnsignedShortBigEndianSubtract128();
        if (kind == 1) return s.readUnsignedShortLittleEndianSubtract128();
        return s.readSignedShortLittleEndianSubtract128();
    }

    private static ByteOrder order(int kind) {
        return kind == 0 ? ByteOrder.BIG_ENDIAN : ByteOrder.LITTLE_ENDIAN;
    }

    // Adding/subtracting 128 modulo 256 toggles precisely the low byte's top bit.
    private static byte[] encoded(int value, int kind) {
        byte[] bytes = ByteBuffer.allocate(2).order(order(kind)).putShort((short) value).array();
        bytes[kind == 0 ? 1 : 0] ^= (byte) 0x80;
        return bytes;
    }

    @Test public void everyValueWritesTwoBytesWithOnlyLowByteTopBitToggled() {
        for (int value = 0; value <= 65535; value++) for (int kind = 0; kind < 2; kind++) {
            byte[] bytes = new byte[7]; Arrays.fill(bytes, (byte) 0x59);
            byte[] expected = bytes.clone(); System.arraycopy(encoded(value, kind), 0, expected, 2, 2);
            Stream s = new Stream(bytes); s.currentOffset = 2; s.bitOffset = 37;
            write(s, kind, value);
            assertArrayEquals(expected, bytes); assertSame(bytes, s.buffer);
            assertEquals(4, s.currentOffset); assertEquals(37, s.bitOffset);
        }
    }

    @Test public void everyWirePatternReadsWithIndependentOrderAndSignedness() {
        for (int pattern = 0; pattern <= 65535; pattern++) {
            byte[] bytes = ByteBuffer.allocate(7).putShort(2, (short) pattern).array();
            byte[] unchanged = bytes.clone();
            for (int kind = 0; kind < 3; kind++) {
                byte[] decoded = bytes.clone(); decoded[kind == 0 ? 3 : 2] ^= (byte) 0x80;
                short signed = ByteBuffer.wrap(decoded).order(order(kind)).getShort(2);
                Stream s = new Stream(bytes); s.currentOffset = 2; s.bitOffset = 37;
                assertEquals(kind == 2 ? signed : Short.toUnsignedInt(signed), read(s, kind));
                assertArrayEquals(unchanged, bytes); assertSame(bytes, s.buffer);
                assertEquals(4, s.currentOffset); assertEquals(37, s.bitOffset);
            }
        }
    }

    @Test public void asymmetricCounterexamplesSeparateLowByteAdjustmentFromWholeShortBias() {
        int[] values = {0, 127, 128, 255, 256, 0x1280, 32767, 32768, 65535};
        int[][] big = {{0,128},{0,255},{0,0},{0,127},{1,128},{18,0},{127,127},{128,128},{255,127}};
        for (int i = 0; i < values.length; i++) for (int kind = 0; kind < 2; kind++) {
            byte[] expected = {(byte) big[i][kind], (byte) big[i][1-kind]};
            Stream s = new Stream(new byte[2]); write(s, kind, values[i]);
            assertArrayEquals(expected, s.buffer);
            assertEquals(values[i], read(new Stream(expected), kind));
            if (kind == 1) assertEquals((short) values[i], read(new Stream(expected), 2));
        }
        assertEquals(0x12b4, read(new Stream(new byte[]{0x12,0x34}), 0));
        assertEquals(0x3492, read(new Stream(new byte[]{0x12,0x34}), 1));
        assertEquals(128, read(new Stream(new byte[]{0,0}), 0));
        assertEquals(65535, read(new Stream(new byte[]{0x7f,-1}), 1));
        assertEquals(-1, read(new Stream(new byte[]{0x7f,-1}), 2));
    }

    @Test public void writersTruncateIntInputsWithoutSaturationOrRangeChecks() {
        int[] values = {Integer.MIN_VALUE,Integer.MAX_VALUE,-65537,-65536,-32769,-32768,-1,0,65535,65536,65537,0x12345678};
        for (int value : values) for (int kind = 0; kind < 2; kind++) {
            Stream s = new Stream(new byte[2]); write(s, kind, value);
            assertArrayEquals(encoded(value, kind), s.buffer); assertEquals(2, s.currentOffset);
        }
    }

    @Test public void adjacentValuesAndAllReadersPreserveBitCursorAndGeneratorState() {
        for (int start : new int[]{0,1,3,7}) for (int kind = 0; kind < 2; kind++) {
            Stream s = new Stream(new byte[start+6]); s.currentOffset = start; s.bitOffset = -17;
            ISAACRandomGen keys = new ISAACRandomGen(new int[]{17,8,3,29});
            ISAACRandomGen reference = new ISAACRandomGen(new int[]{17,8,3,29}); s.encryption = keys;
            for (int value : new int[]{0x1280,-1,0x7fff}) write(s, kind, value);
            assertEquals(start+6, s.currentOffset);
            for (int reader = kind; reader < (kind == 0 ? 1 : 3); reader++) {
                s.currentOffset = start;
                for (int value : new int[]{0x1280,reader == 2 ? -1 : 65535,0x7fff}) assertEquals(value, read(s, reader));
                assertEquals(start+6, s.currentOffset);
            }
            assertEquals(-17, s.bitOffset); assertSame(keys, s.encryption);
            assertEquals(reference.getNextKey(), keys.getNextKey());
        }
    }

    @Test public void uncheckedWriterFailuresRetainPartialWriteAndPostincrement() {
        for (int kind = 0; kind < 2; kind++) for (int start : new int[]{-1,0,1,Integer.MAX_VALUE}) {
            byte[] bytes = {0x55}; Stream s = new Stream(bytes); s.currentOffset = start; s.bitOffset = 11;
            try { write(s, kind, 0x1234); fail("short buffer"); } catch (ArrayIndexOutOfBoundsException expected) { }
            assertEquals(start+(start == 0 ? 2 : 1), s.currentOffset);
            assertEquals(start == 0 ? (kind == 0 ? 0x12 : 0xb4) : 0x55, bytes[0]&255); assertEquals(11, s.bitOffset);
        }
        for (int kind = 0; kind < 2; kind++) {
            Stream s = new Stream(null); s.currentOffset = 4;
            try { write(s, kind, 1); fail("null buffer"); } catch (NullPointerException expected) { }
            assertEquals(5, s.currentOffset);
        }
    }

    @Test public void uncheckedReaderFailuresAdvanceByTwoWithoutChangingInput() {
        for (int kind = 0; kind < 3; kind++) for (int start : new int[]{-1,0,1,Integer.MAX_VALUE}) {
            byte[] bytes = {0x55}; Stream s = new Stream(bytes); s.currentOffset = start; s.bitOffset = 11;
            try { read(s, kind); fail("short buffer"); } catch (ArrayIndexOutOfBoundsException expected) { }
            assertEquals(start+2, s.currentOffset); assertEquals(0x55, bytes[0]); assertEquals(11, s.bitOffset);
        }
        for (int kind = 0; kind < 3; kind++) {
            Stream s = new Stream(null); s.currentOffset = 4;
            try { read(s, kind); fail("null buffer"); } catch (NullPointerException expected) { }
            assertEquals(6, s.currentOffset);
        }
    }
}
