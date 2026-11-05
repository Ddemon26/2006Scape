import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

public class StreamPlainByteAndMediumTest {
    private static void write(Stream s, int width, int value) {
        if (width == 1) s.writeByte(value);
        else s.writeMediumBigEndian(value);
    }

    private static byte[] encoded(int value, int width) {
        byte[] integer = ByteBuffer.allocate(4).putInt(value).array();
        return Arrays.copyOfRange(integer, 4 - width, 4);
    }

    @Test public void everyBytePatternSeparatesSignedUnsignedAndTruncatingWrite() {
        for (int pattern = 0; pattern < 256; pattern++) {
            Stream s = new Stream(new byte[]{91, 0, 93}); s.currentOffset = 1; s.bitOffset = 37;
            s.writeByte(pattern);
            assertArrayEquals(new byte[]{91, (byte) pattern, 93}, s.buffer);
            assertEquals(2, s.currentOffset); assertEquals(37, s.bitOffset);
            s.currentOffset = 1; assertEquals(pattern, s.readUnsignedByte());
            s.currentOffset = 1; assertEquals((byte) pattern, s.readSignedByte());
        }
    }

    @Test public void independentlyVariedMediumBytesProveOrderWidthAndUnsignedRange() {
        // Every value of each byte, with asymmetric fixed neighbours; no round-trip oracle.
        for (int position = 0; position < 3; position++) for (int value = 0; value < 256; value++) {
            byte[] wire = {0x12, 0x34, 0x56}; wire[position] = (byte) value;
            int expected = new BigInteger(1, wire).intValue();
            byte[] source = {91, wire[0], wire[1], wire[2], 93};
            Stream s = new Stream(source); s.currentOffset = 1; s.bitOffset = 37;
            assertEquals(expected, s.readUnsignedMediumBigEndian()); assertEquals(4, s.currentOffset);
            assertEquals(37, s.bitOffset); assertSame(source, s.buffer);
            assertArrayEquals(new byte[]{91, wire[0], wire[1], wire[2], 93}, source);
            byte[] output = {91, 92, 92, 92, 93}; s = new Stream(output); s.currentOffset = 1; s.bitOffset = 37;
            s.writeMediumBigEndian(expected);
            assertArrayEquals(source, output); assertSame(output, s.buffer);
            assertEquals(4, s.currentOffset); assertEquals(37, s.bitOffset);
        }
    }

    @Test public void boundariesAndCounterexamplesExcludeShortIntLittleEndianAndSignExtension() {
        int[] values = {0,1,127,128,255,256,65535,65536,0x123456,0x7fffff,0x800000,0xffffff};
        for (int value : values) {
            Stream s = new Stream(new byte[3]); s.writeMediumBigEndian(value);
            assertArrayEquals(encoded(value, 3), s.buffer);
            assertEquals(value, new Stream(encoded(value, 3)).readUnsignedMediumBigEndian());
        }
        assertEquals(0x123456, new Stream(new byte[]{0x12,0x34,0x56,0x78}).readUnsignedMediumBigEndian());
        assertEquals(0x800000, new Stream(new byte[]{(byte) 0x80,0,0}).readUnsignedMediumBigEndian());
        assertEquals(0xffffff, new Stream(new byte[]{-1,-1,-1}).readUnsignedMediumBigEndian());
    }

    @Test public void writerInputsTruncateModuloWidthWithoutSaturation() {
        int[] values = {Integer.MIN_VALUE,Integer.MAX_VALUE,-16777217,-16777216,-257,-256,-1,0,255,256,257,0xffffff,0x1000000,0x1000001,0x12345678};
        for (int width : new int[]{1,3}) for (int value : values) {
            Stream s = new Stream(new byte[width]); write(s, width, value);
            assertArrayEquals(encoded(value, width), s.buffer); assertEquals(width, s.currentOffset);
        }
    }

    @Test public void adjacentFieldsPreserveBitCursorAndIsaacState() {
        for (int start : new int[]{0,1,3,7}) {
            Stream s = new Stream(new byte[start+7]); s.currentOffset = start; s.bitOffset = -17;
            ISAACRandomGen keys = new ISAACRandomGen(new int[]{17,8,3,29});
            ISAACRandomGen reference = new ISAACRandomGen(new int[]{17,8,3,29}); s.encryption = keys;
            s.writeByte(0x180); s.writeMediumBigEndian(0x123456); s.writeMediumBigEndian(-1);
            s.currentOffset = start;
            assertEquals(128, s.readUnsignedByte()); assertEquals(0x123456, s.readUnsignedMediumBigEndian()); assertEquals(0xffffff, s.readUnsignedMediumBigEndian());
            assertEquals(start+7, s.currentOffset); assertEquals(-17, s.bitOffset);
            assertSame(keys, s.encryption); assertEquals(reference.getNextKey(), keys.getNextKey());
        }
    }

    @Test public void uncheckedWritesRetainPartialBytesAndPostincrement() {
        for (int width : new int[]{1,3}) for (int capacity = 0; capacity < width; capacity++) {
            byte[] bytes = new byte[capacity]; Stream s = new Stream(bytes); s.bitOffset = 11;
            try { write(s, width, 0x123456); fail("short buffer"); } catch (ArrayIndexOutOfBoundsException expected) { }
            assertEquals(capacity+1, s.currentOffset); assertEquals(11, s.bitOffset);
            assertArrayEquals(Arrays.copyOf(encoded(0x123456, width), capacity), bytes);
        }
        for (int width : new int[]{1,3}) for (int start : new int[]{-1,1,Integer.MAX_VALUE}) {
            Stream s = new Stream(new byte[]{91}); s.currentOffset = start;
            try { write(s, width, 1); fail("invalid cursor"); } catch (ArrayIndexOutOfBoundsException expected) { }
            assertEquals(start+1, s.currentOffset); assertEquals(91, s.buffer[0]);
        }
        for (int width : new int[]{1,3}) {
            Stream s = new Stream(null); s.currentOffset = 4;
            try { write(s, width, 1); fail("null buffer"); } catch (NullPointerException expected) { }
            assertEquals(5, s.currentOffset);
        }
    }

    @Test public void uncheckedMediumReadsAdvanceThreeBeforeFailure() {
        for (int length : new int[]{0,1,2}) for (int start : new int[]{-1,0,1,Integer.MAX_VALUE}) {
            byte[] bytes = new byte[length]; Arrays.fill(bytes, (byte) 91);
            Stream s = new Stream(bytes); s.currentOffset = start; s.bitOffset = 11;
            try { s.readUnsignedMediumBigEndian(); fail("invalid input"); } catch (ArrayIndexOutOfBoundsException expected) { }
            assertEquals(start+3, s.currentOffset); assertEquals(11, s.bitOffset);
            for (byte b : bytes) assertEquals(91, b);
        }
        Stream s = new Stream(null); s.currentOffset = 4;
        try { s.readUnsignedMediumBigEndian(); fail("null buffer"); } catch (NullPointerException expected) { }
        assertEquals(7, s.currentOffset);
    }
}
