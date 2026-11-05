import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

public class StreamLongByteOrderTest {
    private Stream at(byte[] bytes, int cursor) {
        Stream stream = new Stream(bytes);
        stream.currentOffset = cursor;
        stream.bitOffset = 37;
        return stream;
    }

    private void checkValue(long value) {
        byte[] wire = ByteBuffer.allocate(8).putLong(value).array();
        for (int cursor : new int[] {0,1,7}) {
            byte[] bytes = new byte[cursor + 9];
            Arrays.fill(bytes, (byte) 91);
            byte[] expected = bytes.clone();
            System.arraycopy(wire, 0, expected, cursor, 8);
            Stream stream = at(bytes, cursor);
            stream.writeLongBigEndian(value);
            assertArrayEquals(expected, bytes);
            assertSame(bytes, stream.buffer);
            assertEquals(cursor + 8, stream.currentOffset);
            assertEquals(37, stream.bitOffset);
            stream.currentOffset = cursor;
            assertEquals(value, stream.readLongBigEndian());
            assertArrayEquals(expected, bytes);
            assertSame(bytes, stream.buffer);
            assertEquals(cursor + 8, stream.currentOffset);
            assertEquals(37, stream.bitOffset);
        }
    }

    @Test public void everyOctetVariesIndependentlyAcrossAllEightLanes() {
        for (int lane = 0; lane < 8; lane++) {
            for (int octet = 0; octet < 256; octet++) {
                byte[] wire = {0x12,0x34,0x56,0x78,(byte)0x9a,(byte)0xbc,(byte)0xde,(byte)0xf0};
                wire[lane] = (byte) octet;
                checkValue(ByteBuffer.wrap(wire).getLong());
            }
        }
    }

    @Test public void signedAndUnsignedHalfBoundariesAndFullWidthSamples() {
        for (long value : new long[] {0,1,-1,127,128,255,256,65535,65536,
                0x7fffffffL,0x80000000L,0xffffffffL,0x100000000L,
                0x7fffffff00000000L,0x8000000000000000L,0xffffffff00000000L,
                0x123456789abcdef0L,Long.MIN_VALUE,Long.MIN_VALUE+1,Long.MAX_VALUE}) checkValue(value);
        Random random = new Random(600064L);
        for (int i = 0; i < 1024; i++) checkValue(random.nextLong());
    }

    @Test public void halvesOccupyDistinctUnsigned32BitPositions() {
        assertEquals(4294967295L, at(new byte[] {0,0,0,0,-1,-1,-1,-1},0).readLongBigEndian());
        assertEquals(-4294967296L, at(new byte[] {-1,-1,-1,-1,0,0,0,0},0).readLongBigEndian());
        assertEquals(Long.MIN_VALUE, at(new byte[] {-128,0,0,0,0,0,0,0},0).readLongBigEndian());
        assertEquals(2147483648L, at(new byte[] {0,0,0,0,-128,0,0,0},0).readLongBigEndian());
        Stream stream = at(new byte[8],0);
        stream.writeLongBigEndian(0x123456789abcdef0L);
        assertArrayEquals(new byte[] {0x12,0x34,0x56,0x78,-102,-68,-34,-16},stream.buffer);
    }

    @Test public void consecutiveValuesReachExactEnd() {
        byte[] bytes = new byte[17]; bytes[0] = 91;
        Stream stream = at(bytes,1);
        stream.writeLongBigEndian(Long.MIN_VALUE); stream.writeLongBigEndian(Long.MAX_VALUE);
        assertEquals(17,stream.currentOffset);
        stream.currentOffset = 1;
        assertEquals(Long.MIN_VALUE,stream.readLongBigEndian());
        assertEquals(Long.MAX_VALUE,stream.readLongBigEndian());
        assertEquals(17,stream.currentOffset); assertEquals(91,bytes[0]);
        assertEquals(37,stream.bitOffset);
    }

    @Test public void shortWritesReportCaughtFailureAndKeepCompletedPrefix() throws Exception {
        PrintStream original = System.out;
        ByteArrayOutputStream report = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(report,true,"UTF-8"));
            for (int available = 0; available < 8; available++) {
                byte[] bytes = new byte[available+1]; bytes[0] = 91;
                Stream stream = at(bytes,1);
                report.reset();
                try {stream.writeLongBigEndian(0x123456789abcdef0L); fail("short write");}
                catch (RuntimeException failure) {
                    assertEquals(RuntimeException.class,failure.getClass());
                    assertNull(failure.getMessage()); assertNull(failure.getCause());
                }
                byte[] expected = new byte[available+1]; expected[0] = 91;
                System.arraycopy(ByteBuffer.allocate(8).putLong(0x123456789abcdef0L).array(),0,expected,1,available);
                assertArrayEquals(expected,bytes);
                assertEquals(available+2,stream.currentOffset); assertEquals(37,stream.bitOffset);
                assertTrue(report.toString("UTF-8").startsWith("Error: 14395, 5, 1311768467463790320, java.lang.ArrayIndexOutOfBoundsException"));
            }
            Stream stream = at(null,3); report.reset();
            try {stream.writeLongBigEndian(-1); fail("null write");}
            catch (RuntimeException failure) {assertEquals(RuntimeException.class,failure.getClass());}
            assertEquals(4,stream.currentOffset); assertNull(stream.buffer);
            assertEquals(37,stream.bitOffset);
            assertTrue(report.toString("UTF-8").startsWith("Error: 14395, 5, -1, java.lang.NullPointerException"));
        } finally {System.setOut(original);}
    }

    @Test public void failedReadsAdvanceByAttemptedFourByteHalf() {
        for (int available = 0; available < 8; available++) {
            byte[] bytes = new byte[available+1]; Arrays.fill(bytes,(byte)91);
            byte[] before = bytes.clone(); Stream stream = at(bytes,1);
            try {stream.readLongBigEndian(); fail("short read");}
            catch (ArrayIndexOutOfBoundsException expected) { }
            assertEquals(available<4?5:9,stream.currentOffset);
            assertArrayEquals(before,bytes); assertEquals(37,stream.bitOffset);
        }
        for (int cursor : new int[] {-1,9,Integer.MAX_VALUE}) {
            Stream stream = at(new byte[8],cursor);
            try {stream.readLongBigEndian(); fail("invalid cursor");}
            catch (ArrayIndexOutOfBoundsException expected) { }
            assertEquals(cursor+4,stream.currentOffset);
        }
        Stream stream = at(null,3);
        try {stream.readLongBigEndian(); fail("null read");}
        catch (NullPointerException expected) { }
        assertEquals(7,stream.currentOffset); assertEquals(37,stream.bitOffset);
    }
}
