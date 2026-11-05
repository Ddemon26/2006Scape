import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Locale;

import org.junit.Test;

public class TextInputTest {

    @Test
    public void commonLettersUseSingleNibblesWithZeroPadding() {
        assertEncoded("", new int[0]);
        assertEncoded("e", 0x10);
        assertEncoded("ee", 0x11);
        assertEncoded("et", 0x12);
        assertEncoded("u", 0xc0);
        assertEncoded("hello", 0x61, 0xbb, 0x40);
    }

    @Test
    public void extendedLettersDigitsAndPunctuationUseTwoNibbles() {
        assertEncoded("m", 0xd0);
        assertEncoded("w", 0xd1);
        assertEncoded("z", 0xdd);
        assertEncoded("0", 0xde);
        assertEncoded("9", 0xe7);
        assertEncoded("!", 0xe9);
        assertEncoded("[", 0xfe);
        assertEncoded("]", 0xff);
    }

    @Test
    public void extendedCodesCanCrossByteBoundaries() {
        assertEncoded("em", 0x1d, 0x00);
        assertEncoded("me", 0xd0, 0x10);
        assertEncoded("m!", 0xd0, 0xe9);
        assertDecoded("Em ", 0x1d, 0x00);
        assertDecoded("Me ", 0xd0, 0x10);
        assertDecoded("M!", 0xd0, 0xe9);
    }

    @Test
    public void caseFoldingUsesTheExistingDefaultLocale() {
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(Locale.ENGLISH);
            assertEncoded("HELLO", 0x61, 0xbb, 0x40);
            assertEncoded("I", 0x50);
            Locale.setDefault(new Locale("tr", "TR"));
            // Turkish dotless i is absent from the codec alphabet.
            assertEncoded("I", 0x00);
        } finally {
            Locale.setDefault(previous);
        }
    }

    @Test
    public void unsupportedCharactersBecomeSpaces() {
        assertEncoded(" ", 0x00);
        assertEncoded("~", 0x00);
        assertEncoded("e~e", 0x10, 0x10);
        assertDecoded("E e ", 0x10, 0x10);
    }

    @Test
    public void encodingStopsAtEightyInputCharacters() {
        assertEncoded(repeat('e', 79), repeatedBytes(39, 0x11, 0x10));
        assertEncoded(repeat('e', 80), repeatedBytes(40, 0x11));
        assertEncoded(repeat('e', 80) + "!", repeatedBytes(40, 0x11));
        assertEncoded(repeat('m', 80), repeatedBytes(80, 0xd0));
    }

    @Test
    public void decoderReadsPackedBytesIncludingPaddingAndUnsignedValues() {
        assertDecoded("", new int[0]);
        assertDecoded("E ", 0x10);
        assertDecoded("Ee", 0x11);
        assertDecoded("Hello ", 0x61, 0xbb, 0x40);
        assertDecoded("U ", 0xc0);
        assertDecoded("M", 0xd0);
        assertDecoded("Z", 0xdd);
        assertDecoded("0", 0xde);
        assertDecoded("9", 0xe7);
        assertDecoded("!", 0xe9);
        assertDecoded("]", 0xff);
        // The decoder also accepts the second space entry in the alphabet.
        assertDecoded(" ", 0xe8);
    }

    @Test
    public void decoderCapitalizesAtTheStartAndAfterSentencePunctuation() {
        assertDecoded("Ee!Ee?Ee.Ee", 0x11, 0xe9, 0x11, 0xea, 0x11, 0xeb, 0x11);
        assertDecoded("Ee,ee", 0x11, 0xec, 0x11);
        assertDecoded("  Ee", 0x00, 0x11);
    }

    @Test
    public void decoderLengthIsBytesAndRespectsTheCurrentCursor() {
        Stream input = new Stream(new byte[] {0x55, 0x11, (byte) 0xd0, (byte) 0xff});
        input.currentOffset = 1;
        assertEquals("Eem", TextInput.decodeChatText(2, input));
        assertEquals(3, input.currentOffset);
        assertEquals(0xff, input.readUnsignedByte());
        assertEquals("", TextInput.decodeChatText(0, input));
        assertEquals(4, input.currentOffset);
    }

    @Test
    public void encoderAppendsWithoutChangingSurroundingBytes() {
        Stream output = new Stream(new byte[] {0x55, 0x66, 0x77, 0x22});
        output.currentOffset = 1;
        TextInput.encodeChatText("em", output);
        assertEquals(3, output.currentOffset);
        assertArrayEquals(new byte[] {0x55, 0x1d, 0x00, 0x22}, output.buffer);
    }

    @Test
    public void incompleteExtendedCodeDoesNotCarryIntoTheNextDecode() {
        assertDecoded("E", 0x1d);
        assertDecoded("  ", 0x00);
    }

    @Test
    public void normalizeChatTextAppliesTheCodecNormalizationAndPadding() {
        assertEquals("Hello ", TextInput.normalizeChatText("HELLO"));
        assertEquals("E e ", TextInput.normalizeChatText("e~e"));
        assertEquals("E" + repeat('e', 79), TextInput.normalizeChatText(repeat('e', 81)));
        assertEquals("M", TextInput.normalizeChatText("m"));
        assertEquals("", TextInput.normalizeChatText(""));
    }


    @Test
    public void entireWireAlphabetHasFixedCodesAtBothNibbleAlignments() {
        String alphabet = " etaoihnsrdlumwcyfgpbvkxjqz0123456789 !?.,:;()-&*\\'@#+=\u00a3$%\"[]";
        String[] codes = ("0 1 2 3 4 5 6 7 8 9 a b c "
                + "d0 d1 d2 d3 d4 d5 d6 d7 d8 d9 da db dc dd de df "
                + "e0 e1 e2 e3 e4 e5 e6 e7 e8 e9 ea eb ec ed ee ef "
                + "f0 f1 f2 f3 f4 f5 f6 f7 f8 f9 fa fb fc fd fe ff").split(" ");
        assertEquals(61, alphabet.length());
        assertEquals(alphabet.length(), codes.length);
        for (int index = 0; index < alphabet.length(); index++) {
            char symbol = alphabet.charAt(index);
            // Encoding chooses the first space entry; decoding accepts both.
            String encodedCode = index == 37 ? "0" : codes[index];
            assertEncoded(String.valueOf(symbol), packedNibbles(encodedCode));
            assertEncoded("e" + symbol + "e", packedNibbles("1" + encodedCode + "1"));
            assertEncoded("ee" + symbol + "e", packedNibbles("11" + encodedCode + "1"));
            assertEncoded("m" + symbol + "e", packedNibbles("d0" + encodedCode + "1"));
            String displaySymbol = symbol >= 'a' && symbol <= 'z'
                    ? String.valueOf((char) (symbol - 'a' + 'A')) : String.valueOf(symbol);
            assertDecoded(displaySymbol + (codes[index].length() == 1 ? " " : ""),
                    packedNibbles(codes[index]));
            assertDecoded("E" + symbol + (codes[index].length() == 1 ? "  " : " "), packedNibbles("1" + codes[index] + "0"));
            assertDecoded("Ee" + symbol + (codes[index].length() == 1 ? " " : ""),
                    packedNibbles("11" + codes[index]));
        }
    }

    @Test
    public void pendingExtendedPrefixSpansBytesButNotDecodeCalls() {
        assertDecoded("M", 0xd0);
        assertDecoded("E", 0x1d);
        assertDecoded("M", 0xd0);
        assertDecoded("Em ", 0x1d, 0x00);
        assertDecoded("Eme", 0x1d, 0x01);
        assertDecoded("Emm ", 0x1d, 0x0d, 0x00);
        assertDecoded("Emm", 0x1d, 0x0d, 0x0d);
        assertDecoded("  ", 0x00);
        assertDecoded("Ee", 0x11);
    }

    @Test
    public void sentenceCapitalizationWaitsForALetterAcrossDigitsAndSeparators() {
        assertDecoded("0  Eee", 0xde, 0x00, 0x11, 0x1d);
        assertDecoded("Ee!0  Eee", 0x11, 0xe9, 0xde, 0x00, 0x11, 0x1d);
        assertDecoded("Ee,0  eee", 0x11, 0xec, 0xde, 0x00, 0x11, 0x1d);
        assertDecoded("Ee?Ee.Ee!Ee", 0x11, 0xea, 0x11, 0xeb, 0x11, 0xe9, 0x11);
    }

    @Test
    public void inputLimitCountsUtf16UnitsBeforeLocaleLowercaseExpansion() {
        Locale previous = Locale.getDefault();
        try {
            Locale.setDefault(Locale.ENGLISH);
            assertEncoded(repeat('e', 79) + "\u0130", repeatedBytes(39, 0x11, 0x15, 0x00));
            assertEquals("E" + repeat('e', 78) + "i  ",
                    TextInput.normalizeChatText(repeat('e', 79) + "\u0130"));
            assertEncoded(repeat('e', 79) + "\ud83d\ude00", repeatedBytes(39, 0x11, 0x10));
            assertEncoded(repeat('e', 78) + "\ud83d\ude00", repeatedBytes(39, 0x11, 0x00));
            assertEquals("E" + repeat('e', 78) + " ",
                    TextInput.normalizeChatText(repeat('e', 79) + "\ud83d\ude00"));
            assertEncoded("\u0130", 0x50);
            Locale.setDefault(new Locale("tr", "TR"));
            assertEncoded("\u0130", 0x50);
            assertEncoded("I", 0x00);
        } finally {
            Locale.setDefault(previous);
        }
    }

    // Independent wire fixtures are concatenated hex digits, then padded to bytes.
    private static int[] packedNibbles(String hexadecimal) {
        if ((hexadecimal.length() & 1) != 0) hexadecimal += "0";
        int[] result = new int[hexadecimal.length() / 2];
        for (int index = 0; index < result.length; index++) {
            result[index] = Integer.parseInt(hexadecimal.substring(index * 2, index * 2 + 2), 16);
        }
        return result;
    }

    private static void assertEncoded(String text, int... expected) {
        Stream output = new Stream(new byte[100]);
        TextInput.encodeChatText(text, output);
        assertEquals("encoded byte count for " + text, expected.length, output.currentOffset);
        assertArrayEquals(bytes(expected), Arrays.copyOf(output.buffer, output.currentOffset));
    }

    private static void assertDecoded(String expected, int... encoded) {
        Stream input = new Stream(bytes(encoded));
        assertEquals(expected, TextInput.decodeChatText(encoded.length, input));
        assertEquals(encoded.length, input.currentOffset);
    }

    private static byte[] bytes(int... values) {
        byte[] result = new byte[values.length];
        for (int index = 0; index < values.length; index++) {
            result[index] = (byte) values[index];
        }
        return result;
    }

    private static String repeat(char value, int count) {
        char[] result = new char[count];
        Arrays.fill(result, value);
        return new String(result);
    }

    private static int[] repeatedBytes(int count, int value, int... suffix) {
        int[] result = new int[count + suffix.length];
        Arrays.fill(result, 0, count, value);
        System.arraycopy(suffix, 0, result, count, suffix.length);
        return result;
    }
}
