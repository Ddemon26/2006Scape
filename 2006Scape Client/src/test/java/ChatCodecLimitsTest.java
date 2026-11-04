import static org.junit.Assert.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.junit.Test;

public class ChatCodecLimitsTest {
    private String decode(int count, Stream stream) throws Exception {
        Method method;
        try {
            method = TextInput.class.getDeclaredMethod("decodeChatText", int.class, Stream.class);
        } catch (NoSuchMethodException baseline) {
            method = TextInput.class.getDeclaredMethod("method525", int.class, Stream.class);
        }
        try {
            return (String) method.invoke(null, count, stream);
        } catch (InvocationTargetException failure) {
            throw (Exception) failure.getCause();
        }
    }

    @Test
    public void scratchBufferAcceptsExactlyOneHundredDecodedCharacters() throws Exception {
        byte[] input = new byte[50];
        Arrays.fill(input, (byte) 0x11);
        String result = decode(50, new Stream(input));
        assertEquals(100, result.length());
        assertEquals('E', result.charAt(0));
        for (int i = 1; i < result.length(); i++) assertEquals('e', result.charAt(i));
    }

    @Test
    public void oversizedPackedInputExposesExistingScratchBufferLimit() throws Exception {
        byte[] input = new byte[51];
        Arrays.fill(input, (byte) 0x11);
        try {
            decode(51, new Stream(input));
            fail("Expected the original 100-character buffer limit");
        } catch (ArrayIndexOutOfBoundsException expected) {
            // This characterizes a pre-existing unsupported input, not safety.
        }
        assertEquals("Ee", decode(1, new Stream(new byte[] {0x11})));
    }

    @Test
    public void shortPayloadIsNotSilentlyAccepted() throws Exception {
        try {
            decode(2, new Stream(new byte[] {0x11}));
            fail("Expected bounds failure for an invalid byte count");
        } catch (ArrayIndexOutOfBoundsException expected) {
            // Production caller must supply the complete payload.
        }
    }

    @Test
    public void negativeByteCountCurrentlyProducesEmptyOutput() throws Exception {
        Stream stream = new Stream(new byte[0]);
        assertEquals("", decode(-1, stream));
        assertEquals(0, stream.currentOffset);
    }

    @Test
    public void largestNormalEncodedMessageFitsTheSharedBuffers() {
        char[] input = new char[80];
        Arrays.fill(input, 'm');
        String result = TextInput.processText(new String(input));
        assertEquals(80, result.length());
        assertEquals('M', result.charAt(0));
        for (int i = 1; i < result.length(); i++) assertEquals('m', result.charAt(i));
    }
}
