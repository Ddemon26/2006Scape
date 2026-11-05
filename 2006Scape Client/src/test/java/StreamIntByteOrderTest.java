import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

public class StreamIntByteOrderTest {
    private static final int[][] ORDERS = {{0,1,2,3}, {3,2,1,0}, {2,3,0,1}, {1,0,3,2}};

    private void write(int kind, Stream stream, int value) {
        if (kind == 0) stream.writeIntBigEndian(value); else stream.writeIntLittleEndian(value);
    }

    private int read(int kind, Stream stream) {
        if (kind == 0) return stream.readIntBigEndian();
        if (kind == 2) return stream.readIntOrder3412();
        return stream.readIntOrder2143();
    }

    private Stream at(byte[] bytes, int cursor) {
        Stream stream = new Stream(bytes);
        stream.currentOffset = cursor;
        stream.bitOffset = 37;
        return stream;
    }

    // Digit 1 denotes the most significant octet. Permute a standard library
    // representation rather than reproducing the production shift expressions.
    private byte[] wire(int value, int kind) {
        byte[] canonical = ByteBuffer.allocate(4).putInt(value).array();
        byte[] result = new byte[4];
        for (int i = 0; i < 4; i++) result[i] = canonical[ORDERS[kind][i]];
        return result;
    }

    private void checkValue(int value) {
        for (int cursor : new int[] {0,1,7}) {
            for (int kind = 0; kind < 4; kind++) {
                byte[] bytes = new byte[cursor + 5];
                Arrays.fill(bytes, (byte) 91);
                System.arraycopy(wire(value, kind), 0, bytes, cursor, 4);
                byte[] before = bytes.clone();
                Stream stream = at(bytes, cursor);
                if (kind != 1) {
                    assertEquals(value, read(kind, stream));
                    assertArrayEquals(before, bytes);
                } else {
                    assertEquals(value, ByteBuffer.wrap(bytes, cursor, 4).order(ByteOrder.LITTLE_ENDIAN).getInt());
                }
                if (kind < 2) {
                    Arrays.fill(bytes, (byte) 91);
                    stream.currentOffset = cursor;
                    write(kind, stream, value);
                    assertArrayEquals(before, bytes);
                }
                assertSame(bytes, stream.buffer);
                assertEquals(cursor + 4, stream.currentOffset);
                assertEquals(37, stream.bitOffset);
            }
        }
    }

    @Test public void eachOctetVariesIndependentlyThroughAll256Values() {
        for (int lane = 0; lane < 4; lane++) {
            for (int octet = 0; octet < 256; octet++) {
                byte[] canonical = {0x12,0x34,0x56,0x78};
                canonical[lane] = (byte) octet;
                checkValue(ByteBuffer.wrap(canonical).getInt());
            }
        }
    }

    @Test public void signedBoundariesAndDeterministicFullWidthSamples() {
        for (int value : new int[] {0,1,-1,127,128,255,256,32767,32768,65535,
                65536,0x7fffff,0x800000,0xffffff,0x1000000,Integer.MIN_VALUE,
                Integer.MIN_VALUE+1,Integer.MAX_VALUE}) checkValue(value);
        Random random = new Random(590032L);
        for (int i = 0; i < 1024; i++) checkValue(random.nextInt());
    }

    @Test public void asymmetricWireDistinguishesAllOrdersAndSign() {
        byte[] bytes = {0x12,0x34,0x56,0x78};
        assertEquals(0x12345678, read(0, at(bytes,0)));
        assertEquals(0x56781234, read(2, at(bytes,0)));
        assertEquals(0x34127856, read(3, at(bytes,0)));
        Stream big = at(new byte[4],0), little = at(new byte[4],0);
        write(0,big,0x12345678); write(1,little,0x12345678);
        assertArrayEquals(bytes,big.buffer);
        assertArrayEquals(new byte[] {0x78,0x56,0x34,0x12},little.buffer);
        assertEquals(-2147483648,read(2,at(new byte[] {0,0,(byte)128,0},0)));
        assertEquals(-2147483648,read(3,at(new byte[] {0,(byte)128,0,0},0)));
    }

    @Test public void consecutiveValuesReachExactEnd() {
        for (int kind = 0; kind < 4; kind++) {
            byte[] bytes = new byte[9]; bytes[0] = 91;
            Stream stream = at(bytes,1);
            if (kind < 2) {write(kind,stream,0x12345678); write(kind,stream,-1);}
            else {
                System.arraycopy(wire(0x12345678,kind),0,bytes,1,4);
                System.arraycopy(wire(-1,kind),0,bytes,5,4);
                assertEquals(0x12345678,read(kind,stream)); assertEquals(-1,read(kind,stream));
            }
            assertEquals(9,stream.currentOffset); assertEquals(91,bytes[0]);
            assertEquals(37,stream.bitOffset);
        }
    }

    @Test public void shortWritesRetainOnlyCompletedPrefixAndPostincrement() {
        for (int kind = 0; kind < 2; kind++) {
            for (int available = 0; available < 4; available++) {
                byte[] bytes = new byte[available+1]; bytes[0] = 91;
                Stream stream = at(bytes,1);
                try {write(kind,stream,0x12345678); fail("short write");}
                catch (ArrayIndexOutOfBoundsException expected) { }
                byte[] expected = new byte[available+1]; expected[0] = 91;
                System.arraycopy(wire(0x12345678,kind),0,expected,1,available);
                assertArrayEquals(expected,bytes);
                assertEquals(available+2,stream.currentOffset); assertEquals(37,stream.bitOffset);
            }
        }
    }

    @Test public void failedReadsPreadvanceFourAndNeverMutateInput() {
        for (int kind : new int[] {0,2,3}) {
            for (int cursor : new int[] {-1,0,1,4,Integer.MAX_VALUE}) {
                byte[] bytes = {11,22,33}; Stream stream = at(bytes,cursor);
                try {read(kind,stream); fail("short read");}
                catch (ArrayIndexOutOfBoundsException expected) { }
                assertEquals(cursor+4,stream.currentOffset);
                assertArrayEquals(new byte[] {11,22,33},bytes); assertEquals(37,stream.bitOffset);
            }
        }
        for (int kind = 0; kind < 5; kind++) {
            Stream stream = at(null,3);
            try {if (kind < 2) write(kind,stream,0); else read(kind==2?0:kind-1,stream); fail("null");}
            catch (NullPointerException expected) { }
            assertEquals(kind<2?4:7,stream.currentOffset); assertEquals(37,stream.bitOffset);
            assertNull(stream.buffer);
        }
    }
}
