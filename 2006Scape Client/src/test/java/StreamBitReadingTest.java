import java.lang.reflect.Field;
import java.math.BigInteger;
import org.junit.Test;
import static org.junit.Assert.*;

public class StreamBitReadingTest {
    // Written as a bit string so the oracle does not repeat the byte-mask reader.
    private static final String BITS =
            "1011011001101001111000010100101110010110010110101100001101110101";

    private static Stream input() {
        byte[] bytes = new byte[BITS.length() / 8];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(BITS.substring(i * 8, i * 8 + 8), 2);
        }
        return new Stream(bytes);
    }

    private static int expected(int offset, int width) {
        return width == 0 ? 0 : new BigInteger(BITS.substring(offset, offset + width), 2).intValue();
    }

    @Test public void asymmetricBitsProveMsbFirstOrderAcrossEveryAlignmentAndWidth() {
        for (int start = 0; start < 16; start++) {
            for (int width = 0; width <= 32; width++) {
                Stream stream = input();
                stream.currentOffset = start / 8;
                stream.beginBitReading();
                stream.readBitsMsbFirst(start % 8);
                assertEquals("start=" + start + " width=" + width,
                        expected(start, width), stream.readBitsMsbFirst(width));
                assertEquals(start + width, stream.bitOffset);
                assertEquals(start / 8, stream.currentOffset);
            }
        }
    }

    @Test public void singleBitChangesAndAllOnesProveMasksAndSignedIntResult() {
        for (int width = 1; width <= 32; width++) {
            for (int position = 0; position < width; position++) {
                byte[] bytes = new byte[5];
                bytes[position / 8] = (byte) (128 >>> (position % 8));
                Stream stream = new Stream(bytes);
                stream.beginBitReading();
                assertEquals(1 << (width - position - 1), stream.readBitsMsbFirst(width));
            }
            Stream ones = new Stream(new byte[] {-1, -1, -1, -1, -1});
            ones.beginBitReading();
            assertEquals(BigInteger.ONE.shiftLeft(width).subtract(BigInteger.ONE).intValue(), ones.readBitsMsbFirst(width));
        }
        Stream negative = new Stream(new byte[] {(byte) 0x80, 0, 0, 0});
        negative.beginBitReading();
        assertEquals(Integer.MIN_VALUE, negative.readBitsMsbFirst(32));
    }

    @Test public void splitReadsConsumeConsecutiveBitsAndLeaveByteCursorUntilFinish() {
        Stream stream = input();
        stream.currentOffset = 1;
        stream.beginBitReading();
        int offset = 8;
        for (int width : new int[] {1, 2, 3, 5, 7, 0, 8, 9, 10}) {
            assertEquals(expected(offset, width), stream.readBitsMsbFirst(width));
            offset += width;
            assertEquals(offset, stream.bitOffset);
            assertEquals(1, stream.currentOffset);
        }
        stream.finishBitReading();
        assertEquals(7, stream.currentOffset);
        assertEquals(53, stream.bitOffset);
        assertEquals(0x75, stream.readUnsignedByte());
    }

    @Test public void beginUsesByteCursorAndFinishRoundsUpWithoutChangingBitCursor() {
        for (int start = 0; start < 4; start++) {
            for (int count = 0; count <= 24; count++) {
                Stream stream = input();
                stream.bitOffset = 57;
                stream.currentOffset = start;
                stream.beginBitReading();
                assertEquals(start * 8, stream.bitOffset);
                stream.readBitsMsbFirst(count);
                stream.finishBitReading();
                assertEquals(start + (count + 7) / 8, stream.currentOffset);
                assertEquals(start * 8 + count, stream.bitOffset);
                stream.finishBitReading();
                assertEquals(start + (count + 7) / 8, stream.currentOffset);
                stream.beginBitReading();
                assertEquals(stream.currentOffset * 8, stream.bitOffset);
            }
        }
    }

    @Test public void maskTableContainsLowBitMasksIncludingZeroAndAllThirtyTwoBits() throws Exception {
        Field field = Stream.class.getDeclaredField("lowBitMasks");
        field.setAccessible(true);
        int[] masks = (int[]) field.get(null);
        assertEquals(33, masks.length);
        for (int width = 0; width <= 32; width++) {
            assertEquals(BigInteger.ONE.shiftLeft(width).subtract(BigInteger.ONE).intValue(), masks[width]);
        }
    }

    @Test public void exactEndReadWorksButZeroWidthStillRequiresAnAddressableByte() {
        Stream stream = new Stream(new byte[] {(byte) 0xa5});
        stream.beginBitReading();
        assertEquals(0, stream.readBitsMsbFirst(0));
        assertEquals(0xa5, stream.readBitsMsbFirst(8));
        expectBounds(stream, 0, 8);
        stream.finishBitReading();
        assertEquals(1, stream.currentOffset);
        expectBounds(new Stream(new byte[0]), 0, 0);
    }

    @Test public void truncatedInputAdvancesBitCursorBeforeFailureAndLeavesBytesUnchanged() {
        Stream stream = new Stream(new byte[] {(byte) 0xa5});
        stream.beginBitReading();
        assertEquals(5, stream.readBitsMsbFirst(3));
        expectBounds(stream, 6, 9);
        assertEquals(0, stream.currentOffset);
        assertArrayEquals(new byte[] {(byte) 0xa5}, stream.buffer);
        stream.finishBitReading();
        assertEquals(2, stream.currentOffset);
    }

    private static void expectBounds(Stream stream, int width, int cursorAfter) {
        try {
            stream.readBitsMsbFirst(width);
            fail("Expected bounds failure");
        } catch (ArrayIndexOutOfBoundsException expected) {
            assertEquals(cursorAfter, stream.bitOffset);
        }
    }
}
