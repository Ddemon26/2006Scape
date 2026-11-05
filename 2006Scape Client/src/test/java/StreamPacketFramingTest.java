import java.lang.reflect.Field;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

public class StreamPacketFramingTest {
    private Stream at(byte[] bytes, int offset) {
        Stream stream = new Stream(bytes);
        stream.currentOffset = offset;
        stream.bitOffset = 37;
        return stream;
    }

    private Field field(String name) throws Exception {
        Field field = ISAACRandomGen.class.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    private ISAACRandomGen singleKey(int key) throws Exception {
        ISAACRandomGen generator = new ISAACRandomGen(new int[] {1,2,3,4});
        ((int[]) field("results").get(generator))[0] = key;
        field("count").setInt(generator, 1);
        return generator;
    }

    private void checkOpcode(int opcode, int key, int offset) throws Exception {
        byte[] bytes = new byte[offset + 2]; Arrays.fill(bytes, (byte) 91);
        byte[] expected = bytes.clone();
        expected[offset] = (byte) Math.floorMod((long) opcode + key, 256L);
        Stream stream = at(bytes, offset); stream.encryption = singleKey(key);
        ISAACRandomGen generator = stream.encryption;
        stream.writeEncryptedOpcode(opcode);
        assertArrayEquals(expected, bytes); assertSame(bytes, stream.buffer);
        assertSame(generator, stream.encryption);
        assertEquals(offset + 1, stream.currentOffset); assertEquals(37, stream.bitOffset);
        assertEquals(0, field("count").getInt(generator));
    }

    @Test public void opcodeAndKeyOctetsVaryIndependently() throws Exception {
        for (int opcode = 0; opcode < 256; opcode++) {
            for (int key = 0; key < 256; key++) checkOpcode(opcode, key, 1);
        }
    }

    @Test public void moduloAdditionDiffersFromXorAndConsumesOneKeyAcrossRefills() throws Exception {
        checkOpcode(1, 1, 0); // sum is 2, XOR would be 0
        checkOpcode(255, 1, 7); // low byte wraps to zero
        for (int value : new int[] {Integer.MIN_VALUE,-257,-1,0,127,128,255,256,Integer.MAX_VALUE}) {
            for (int key : new int[] {Integer.MIN_VALUE,-1,0,1,255,256,Integer.MAX_VALUE})
                checkOpcode(value, key, 3);
        }
        int[] seed = {0x12345678,-1,Integer.MIN_VALUE,7};
        ISAACRandomGen reference = new ISAACRandomGen(seed);
        Stream stream = at(new byte[601], 1); stream.buffer[0] = 91;
        stream.encryption = new ISAACRandomGen(seed);
        for (int index = 0; index < 600; index++) {
            int opcode = index % 256;
            int expected = (int) Math.floorMod((long) opcode + reference.getNextKey(),256L);
            stream.writeEncryptedOpcode(opcode);
            assertEquals(expected, stream.buffer[index + 1] & 255);
        }
        assertEquals(reference.getNextKey(), stream.encryption.getNextKey());
        assertEquals(601,stream.currentOffset); assertEquals(91,stream.buffer[0]);
        assertEquals(37,stream.bitOffset);
    }

    @Test public void reservedLengthCellIsPatchedWithoutAdvancingOrTouchingPayload() {
        for (int prefix : new int[] {0,1,7}) {
            for (int length = 0; length <= 512; length++) {
                byte[] bytes = new byte[prefix + 1 + length + 1];
                Arrays.fill(bytes,(byte)91);
                for (int index = 0; index < length; index++) bytes[prefix + 1 + index] = (byte)(index * 17);
                byte[] expected = bytes.clone(); expected[prefix] = (byte)(length % 256);
                int end = prefix + 1 + length;
                Stream stream = at(bytes,end);
                stream.backpatchBytePayloadLength(length);
                assertArrayEquals(expected,bytes); assertSame(bytes,stream.buffer);
                assertEquals(end,stream.currentOffset); assertEquals(37,stream.bitOffset);
                assertNull(stream.encryption); // backpatch requires no key generator
            }
        }
    }

    @Test public void variablePacketsKeepOpcodeLengthAndPayloadSeparate() throws Exception {
        Stream stream = at(new byte[400],0);
        stream.encryption = singleKey(1);
        stream.writeEncryptedOpcode(4);
        stream.writeWordBigEndian(0); // reserved cell
        int start = stream.currentOffset;
        stream.writeBytes(new byte[] {12,34,56},3,0);
        stream.backpatchBytePayloadLength(stream.currentOffset - start);
        assertArrayEquals(new byte[] {5,3,12,34,56},Arrays.copyOf(stream.buffer,stream.currentOffset));
        stream.encryption = singleKey(2);
        stream.writeEncryptedOpcode(126);
        stream.writeWordBigEndian(0);
        start = stream.currentOffset;
        stream.backpatchBytePayloadLength(stream.currentOffset - start); // zero payload still patches reserved byte
        assertArrayEquals(new byte[] {5,3,12,34,56,-128,0},Arrays.copyOf(stream.buffer,stream.currentOffset));
        assertEquals(37,stream.bitOffset);
    }

    @Test public void backpatchUsesSuppliedDistanceRatherThanStoredPacketStart() {
        byte[] bytes = {10,20,30,40,50,60}; Stream stream = at(bytes,4);
        stream.backpatchBytePayloadLength(1); assertArrayEquals(new byte[] {10,20,1,40,50,60},bytes);
        stream.backpatchBytePayloadLength(3); assertArrayEquals(new byte[] {3,20,1,40,50,60},bytes);
        stream.backpatchBytePayloadLength(-1); assertArrayEquals(new byte[] {3,20,1,40,-1,60},bytes);
        assertEquals(4,stream.currentOffset); assertEquals(37,stream.bitOffset);
        stream = at(new byte[1],Integer.MIN_VALUE);
        stream.backpatchBytePayloadLength(Integer.MAX_VALUE); // Java int address overflow is retained
        assertEquals(-1,stream.buffer[0]); assertEquals(Integer.MIN_VALUE,stream.currentOffset);
    }

    @Test public void failedOpcodeStillAdvancesCursorAndConsumesKeyBeforeArrayStore() throws Exception {
        for (int offset : new int[] {-1,0,1,Integer.MAX_VALUE}) {
            Stream stream = at(new byte[0],offset); stream.encryption = singleKey(42);
            try { stream.writeEncryptedOpcode(4); fail("short opcode buffer"); }
            catch (ArrayIndexOutOfBoundsException expected) { }
            assertEquals(offset + 1,stream.currentOffset);
            assertEquals(0,field("count").getInt(stream.encryption)); assertEquals(37,stream.bitOffset);
        }
        Stream stream = at(null,3); stream.encryption = singleKey(42);
        try { stream.writeEncryptedOpcode(4); fail("null opcode buffer"); } catch (NullPointerException expected) { }
        assertEquals(4,stream.currentOffset); assertEquals(0,field("count").getInt(stream.encryption));
        byte[] bytes = {91}; stream = at(bytes,0);
        try { stream.writeEncryptedOpcode(4); fail("missing encryption"); } catch (NullPointerException expected) { }
        assertEquals(1,stream.currentOffset); assertArrayEquals(new byte[] {91},bytes);
    }

    @Test public void failedBackpatchLeavesBothCursorsAndBufferUnchanged() {
        for (int length : new int[] {0,2,-2,Integer.MIN_VALUE,Integer.MAX_VALUE}) {
            byte[] bytes = {91}; Stream stream = at(bytes,0);
            try { stream.backpatchBytePayloadLength(length); fail("invalid patch address"); }
            catch (ArrayIndexOutOfBoundsException expected) { }
            assertEquals(0,stream.currentOffset); assertEquals(37,stream.bitOffset);
            assertArrayEquals(new byte[] {91},bytes);
        }
        Stream stream = at(null,3);
        try { stream.backpatchBytePayloadLength(2); fail("null patch buffer"); } catch (NullPointerException expected) { }
        assertEquals(3,stream.currentOffset); assertEquals(37,stream.bitOffset);
    }
}
