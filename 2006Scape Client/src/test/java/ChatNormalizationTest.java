import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Locale;

import org.junit.Test;

public class ChatNormalizationTest {

    @Test
    public void normalizationProducesTheChatDisplayFormWithoutCensoring() {
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(Locale.ENGLISH);
            assertEquals("Hello ", TextInput.normalizeChatText("HELLO"));
            assertEquals("Em ", TextInput.normalizeChatText("em"));
            assertEquals("Me ", TextInput.normalizeChatText("me"));
            assertEquals("M!", TextInput.normalizeChatText("m!"));
            assertEquals("Ee!Ee?Ee.Ee", TextInput.normalizeChatText("ee!ee?ee.ee"));
            assertEquals("Ee,ee", TextInput.normalizeChatText("ee,ee"));
            assertEquals("E e ", TextInput.normalizeChatText("e^e"));
            assertEquals("[]09!", TextInput.normalizeChatText("[]09!"));
            // Codec normalization does not perform the separate chat censorship step.
            assertEquals("Fuck ", TextInput.normalizeChatText("fuck"));
        } finally {
            Locale.setDefault(previous);
        }
    }

    @Test
    public void normalizationPreservesPaddingAndCapsInputAtEightyCharacters() {
        assertEquals("", TextInput.normalizeChatText(""));
        assertEquals("  ", TextInput.normalizeChatText(" "));
        assertEquals("  ", TextInput.normalizeChatText("  "));
        assertEquals("E" + repeat('e', 78) + " ", TextInput.normalizeChatText(repeat('e', 79)));
        assertEquals("E" + repeat('e', 79), TextInput.normalizeChatText(repeat('e', 80)));
        assertEquals("E" + repeat('e', 79), TextInput.normalizeChatText(repeat('e', 80) + "!"));
        assertEquals("M" + repeat('m', 79), TextInput.normalizeChatText(repeat('m', 80)));
    }

    @Test
    public void normalizationRetainsDefaultLocaleDependence() {
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(Locale.ENGLISH);
            assertEquals("I ", TextInput.normalizeChatText("I"));
            Locale.setDefault(new Locale("tr", "TR"));
            assertEquals("  ", TextInput.normalizeChatText("I"));
        } finally {
            Locale.setDefault(previous);
        }
    }

    @Test
    public void normalizationReusesAPackedByteStreamAndResetsItsCursor() throws Exception {
        Stream scratch = field("normalizationStream", Stream.class);
        assertEquals(100, scratch.buffer.length);
        byte[] saved = scratch.buffer.clone();
        int savedOffset = scratch.currentOffset;
        try {
            Arrays.fill(scratch.buffer, (byte) 0x55);
            scratch.currentOffset = 99;
            assertEquals("Hello ", TextInput.normalizeChatText("hello"));
            assertSame(scratch, field("normalizationStream", Stream.class));
            assertEquals(3, scratch.currentOffset);
            assertArrayEquals(new byte[] {0x61, (byte) 0xbb, 0x40},
                    Arrays.copyOf(scratch.buffer, 3));
            assertEquals(0x55, scratch.buffer[3]);
            assertEquals("Ee", TextInput.normalizeChatText("ee"));
            assertEquals(1, scratch.currentOffset);
            assertEquals(0x11, scratch.buffer[0]);
            assertEquals((byte) 0xbb, scratch.buffer[1]);
            assertEquals("", TextInput.normalizeChatText(""));
            assertEquals(0, scratch.currentOffset);
        } finally {
            System.arraycopy(saved, 0, scratch.buffer, 0, saved.length);
            scratch.currentOffset = savedOffset;
        }
    }

    @Test
    public void explicitCodecStreamsDoNotUseTheNormalizationByteStream() throws Exception {
        Stream scratch = field("normalizationStream", Stream.class);
        byte[] saved = scratch.buffer.clone();
        int savedOffset = scratch.currentOffset;
        try {
            Arrays.fill(scratch.buffer, (byte) 0x55);
            scratch.currentOffset = 7;
            Stream separate = new Stream(new byte[8]);
            TextInput.encodeChatText("hello", separate);
            separate.currentOffset = 0;
            assertEquals("Hello ", TextInput.decodeChatText(3, separate));
            assertEquals(7, scratch.currentOffset);
            byte[] expected = new byte[100];
            Arrays.fill(expected, (byte) 0x55);
            assertArrayEquals(expected, scratch.buffer);
        } finally {
            System.arraycopy(saved, 0, scratch.buffer, 0, saved.length);
            scratch.currentOffset = savedOffset;
        }
    }

    @Test
    public void decodedCharacterScratchContainsDisplayCharactersAndRetainsItsUnusedTail() throws Exception {
        char[] scratch = field("decodedCharacters", char[].class);
        assertEquals(100, scratch.length);
        char[] saved = scratch.clone();
        try {
            Arrays.fill(scratch, '#');
            String first = TextInput.decodeChatText(3,
                    new Stream(new byte[] {0x61, (byte) 0xbb, 0x40}));
            assertEquals("Hello ", first);
            assertArrayEquals("Hello ".toCharArray(), Arrays.copyOf(scratch, 6));
            assertEquals('#', scratch[6]);
            assertEquals("Ee", TextInput.decodeChatText(1, new Stream(new byte[] {0x11})));
            assertSame(scratch, field("decodedCharacters", char[].class));
            assertArrayEquals("Eello ".toCharArray(), Arrays.copyOf(scratch, 6));
            assertEquals("Hello ", first);
            TextInput.encodeChatText("m!", new Stream(new byte[8]));
            assertArrayEquals("Eello ".toCharArray(), Arrays.copyOf(scratch, 6));
            assertEquals("M!", TextInput.normalizeChatText("m!"));
            assertArrayEquals("M!llo ".toCharArray(), Arrays.copyOf(scratch, 6));
        } finally {
            System.arraycopy(saved, 0, scratch, 0, saved.length);
        }
    }

    private static <T> T field(String name, Class<T> type) throws Exception {
        Field field = TextInput.class.getDeclaredField(name);
        field.setAccessible(true);
        return type.cast(field.get(null));
    }

    private static String repeat(char character, int count) {
        char[] characters = new char[count];
        Arrays.fill(characters, character);
        return new String(characters);
    }
}
