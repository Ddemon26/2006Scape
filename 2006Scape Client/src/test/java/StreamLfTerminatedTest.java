import java.nio.charset.Charset;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

public class StreamLfTerminatedTest {
    private Stream at(byte[] bytes, int offset) {
        Stream stream = new Stream(bytes);
        stream.currentOffset = offset;
        stream.bitOffset = 37;
        return stream;
    }

    @Test public void readersSelectFirstLfAcrossLengthsOffsetsAndAllOtherBytes() {
        for (int length : new int[] {0, 1, 2, 127, 255, 256}) {
            for (int offset : new int[] {0, 1, 7}) {
                byte[] payload = new byte[length];
                for (int i = 0; i < length; i++) payload[i] = (byte) (i % 255 < 10 ? i % 255 : i % 255 + 1);
                byte[] input = new byte[offset + length + 4];
                Arrays.fill(input, (byte) 91);
                System.arraycopy(payload, 0, input, offset, length);
                input[offset + length] = 10;
                input[offset + length + 1] = 0;
                input[offset + length + 2] = 10;
                byte[] saved = input.clone();
                Stream bytes = at(input, offset), text = at(input, offset);
                byte[] result = bytes.readLfTerminatedBytes();
                assertArrayEquals(payload, result);
                assertNotSame(input, result);
                assertEquals(new String(payload, Charset.defaultCharset()), text.readLfTerminatedString());
                for (Stream stream : new Stream[] {bytes, text}) {
                    assertEquals(offset + length + 1, stream.currentOffset);
                    assertEquals(37, stream.bitOffset);
                    assertSame(input, stream.buffer);
                }
                assertArrayEquals(saved, input);
                if (result.length > 0) result[0] ^= 1;
                assertArrayEquals(saved, input);
            }
        }
    }

    @Test public void nulAndCarriageReturnArePayloadAndConsecutiveRecordsRemainSeparate() {
        byte[] input = {91, 65, 0, 13, 66, 10, 10, 67, 10, 99};
        Stream bytes = at(input, 1), text = at(input, 1);
        assertArrayEquals(new byte[] {65, 0, 13, 66}, bytes.readLfTerminatedBytes());
        assertEquals("A\u0000\rB", text.readLfTerminatedString());
        assertArrayEquals(new byte[0], bytes.readLfTerminatedBytes());
        assertEquals("", text.readLfTerminatedString());
        assertArrayEquals(new byte[] {67}, bytes.readLfTerminatedBytes());
        assertEquals("C", text.readLfTerminatedString());
        assertEquals(9, bytes.currentOffset);
        assertEquals(9, text.currentOffset);
    }

    @Test public void writerEmitsCharacterCountPrefixOfDefaultEncodedBytesThenLf() {
        String[] inputs = {"", "A", "Hello09!", "A\u0000\rB", "A\nB", "\u00e9", "\u20ac", "\ud83d\ude00"};
        for (String input : inputs) {
            for (int offset : new int[] {0, 3}) {
                byte[] buffer = new byte[64];
                Arrays.fill(buffer, (byte) 91);
                byte[] expected = buffer.clone();
                byte[] encoded = input.getBytes(Charset.defaultCharset());
                Stream stream = at(buffer, offset);
                if (encoded.length < input.length()) {
                    // Some default charsets replace a surrogate pair with one byte.
                    try { stream.writeLfTerminatedString(input); fail("short encoded input"); }
                    catch (IndexOutOfBoundsException expectedFailure) { }
                    assertArrayEquals(expected, buffer);
                    assertEquals(offset, stream.currentOffset);
                } else {
                    System.arraycopy(encoded, 0, expected, offset, input.length());
                    expected[offset + input.length()] = 10;
                    stream.writeLfTerminatedString(input);
                    assertArrayEquals(expected, buffer);
                    assertEquals(offset + input.length() + 1, stream.currentOffset);
                }
                assertSame(buffer, stream.buffer);
                assertEquals(37, stream.bitOffset);
            }
        }
    }

    @Test public void embeddedLfIsNotEscapedAndWriterDoesNotAddLengthPrefix() {
        Stream stream = at(new byte[5], 0);
        stream.writeLfTerminatedString("A\nB");
        assertArrayEquals(new byte[] {65, 10, 66, 10, 0}, stream.buffer);
        stream.currentOffset = 0;
        assertEquals("A", stream.readLfTerminatedString());
        assertArrayEquals(new byte[] {66}, stream.readLfTerminatedBytes());
        assertEquals(4, stream.currentOffset);
    }

    @Test public void unterminatedReadersFailAfterPostincrementWithoutChangingInput() {
        for (boolean text : new boolean[] {false, true}) {
            for (int offset : new int[] {0, 2, 3}) {
                byte[] input = {65, 0, 13};
                Stream stream = at(input, offset);
                try {
                    if (text) stream.readLfTerminatedString(); else stream.readLfTerminatedBytes();
                    fail("missing LF");
                } catch (ArrayIndexOutOfBoundsException expected) { }
                assertEquals(4, stream.currentOffset);
                assertEquals(37, stream.bitOffset);
                assertArrayEquals(new byte[] {65, 0, 13}, input);
            }
        }
    }

    @Test public void writerFailureOrderAndExactEndRemainObservable() {
        Stream exact = at(new byte[2], 0);
        exact.writeLfTerminatedString("A");
        assertArrayEquals(new byte[] {65, 10}, exact.buffer);
        assertEquals(2, exact.currentOffset);
        Stream noTerminatorRoom = at(new byte[1], 0);
        try { noTerminatorRoom.writeLfTerminatedString("A"); fail("no LF room"); }
        catch (ArrayIndexOutOfBoundsException expected) { }
        assertArrayEquals(new byte[] {65}, noTerminatorRoom.buffer);
        assertEquals(2, noTerminatorRoom.currentOffset);
        Stream noPayloadRoom = at(new byte[] {91}, 0);
        try { noPayloadRoom.writeLfTerminatedString("AB"); fail("no payload room"); }
        catch (IndexOutOfBoundsException expected) { }
        assertArrayEquals(new byte[] {91}, noPayloadRoom.buffer);
        assertEquals(0, noPayloadRoom.currentOffset);
        Stream nullText = at(new byte[] {91}, 0);
        try { nullText.writeLfTerminatedString(null); fail("null text"); }
        catch (NullPointerException expected) { }
        assertEquals(0, nullText.currentOffset);
        assertArrayEquals(new byte[] {91}, nullText.buffer);
    }
}
