import static org.junit.Assert.*;

import java.util.Locale;
import org.junit.Test;

public class TextClassNameTest {
    @Test public void passwordMaskCountsUtf16UnitsIncludingNoncharacters() {
        String[][] cases = {{"", ""}, {"a", "*"}, {"Z9!?", "****"},
                {"\u0000", "*"}, {"\ud83d\ude00", "**"}, {"\ud83d", "*"},
                {"\ude00", "*"}, {"e\u0301", "**"}, {"@red@", "*****"},
                {"a\ud83d\ude00b", "****"}};
        for (String[] pair : cases) assertEquals(pair[1], TextClass.passwordAsterisks(pair[0]));
    }

    @Test public void passwordMaskDependsOnWidthAndNeverRetainsContent() {
        String[] inputs = {"abcd", "WXYZ", "1234", "    ", "****", "\ud83d\ude00\ud83d\ude00"};
        for (String value : inputs) assertEquals("****", TextClass.passwordAsterisks(value));
        String value = "abcD";
        TextClass.passwordAsterisks(value);
        assertEquals("abcD", value);
    }

    @Test public void passwordMaskHandlesPrefixesAndBufferGrowthWithoutStateLeakage() {
        StringBuilder value = new StringBuilder();
        StringBuilder expected = new StringBuilder();
        for (int size = 0; size <= 257; size++) {
            assertEquals(expected.toString(), TextClass.passwordAsterisks(value.toString()));
            assertEquals("", TextClass.passwordAsterisks(""));
            value.append((char) (size + 1));
            expected.append('*');
        }
    }

    @Test public void nullPasswordKeepsItsFailureContract() {
        try { TextClass.passwordAsterisks(null); fail("null password"); }
        catch (NullPointerException expected) { }
    }

    @Test public void eachAsciiLetterAndDigitHasItsBase37Value() {
        for (int index = 0; index < 26; index++) {
            String lower = String.valueOf((char) ('a' + index));
            String upper = String.valueOf((char) ('A' + index));
            assertEquals(index + 1L, TextClass.encodeBase37Name(lower));
            assertEquals(index + 1L, TextClass.encodeBase37Name(upper));
            assertEquals(lower, TextClass.decodeBase37Name(index + 1L));
        }
        for (int index = 0; index < 10; index++) {
            String digit = String.valueOf((char) ('0' + index));
            assertEquals(index + 27L, TextClass.encodeBase37Name(digit));
            assertEquals(digit, TextClass.decodeBase37Name(index + 27L));
        }
    }

    @Test public void unequalPlaceValuesProveOrderAndInternalZeroSeparators() {
        String[] names = {"ab", "ba", "a0", "0a", "a_b", "a b", "a-b", "a\u00e9b", "a\ud83d\ude00b"};
        long[] values = {39, 75, 64, 1000, 1371, 1371, 1371, 1371, 50655};
        String[] decoded = {"ab", "ba", "a0", "0a", "a_b", "a_b", "a_b", "a_b", "a__b"};
        for (int index = 0; index < names.length; index++) {
            assertEquals(names[index], values[index], TextClass.encodeBase37Name(names[index]));
            assertEquals(decoded[index], TextClass.decodeBase37Name(values[index]));
        }
    }

    @Test public void firstTwelveUtf16UnitsAreUsedAndTrailingZeroDigitsRemoved() {
        assertEquals(0L, TextClass.encodeBase37Name(""));
        assertEquals(0L, TextClass.encodeBase37Name(" _!?\u00e9\ud83d\ude00"));
        assertEquals(1L, TextClass.encodeBase37Name("___A___"));
        assertEquals(39L, TextClass.encodeBase37Name("Ab !__"));
        assertEquals(6582952005840035280L, TextClass.encodeBase37Name("999999999999"));
        assertEquals(6582952005840035280L, TextClass.encodeBase37Name("999999999999A"));
        assertEquals(39L, TextClass.encodeBase37Name("ab__________9"));
        assertEquals(1L, TextClass.encodeBase37Name("a__________\ud83d\ude00b"));
        assertEquals("999999999999", TextClass.decodeBase37Name(6582952005840035280L));
        assertEquals("a__________a", TextClass.decodeBase37Name(177917621779460414L));
    }

    @Test public void decoderRejectsNonpositiveOutOfRangeAndTrailingZeroEncodings() {
        long[] invalid = {Long.MIN_VALUE, -1, 0, 37, 74, 1369, 6582952005840035281L, Long.MAX_VALUE};
        for (long value : invalid) assertEquals("invalid_name", TextClass.decodeBase37Name(value));
        assertEquals("a", TextClass.decodeBase37Name(1));
        assertEquals("a_a", TextClass.decodeBase37Name(1370));
        assertEquals("a0", TextClass.decodeBase37Name(64));
    }

    @Test public void formatterReplacesUnderscoresAndCapitalizesOnlyAsciiStarts() {
        String[][] cases = {{"", ""}, {"alice", "Alice"}, {"a_b", "A B"},
                {"a__b_", "A  B "}, {"_abc", " Abc"}, {"__a", "  A"},
                {"a b", "A b"}, {"a-b", "A-b"}, {"hELLO_wORLD", "HELLO WORLD"},
                {"9abc_def", "9abc Def"}, {"\u00e9abc_\u00e9def", "\u00e9abc \u00e9def"},
                {"x_1abc", "X 1abc"}, {"a\ud83d\ude00_b", "A\ud83d\ude00 B"}, {"a_!b", "A !b"}};
        for (String[] pair : cases) assertEquals(pair[0], pair[1], TextClass.formatDisplayName(pair[0]));
    }

    @Test public void codecAndFormatterIgnoreDefaultLocale() {
        Locale previous = Locale.getDefault();
        try {
            for (Locale locale : new Locale[] {Locale.US, new Locale("tr", "TR")}) {
                Locale.setDefault(locale);
                assertEquals(342L, TextClass.encodeBase37Name("Ii"));
                assertEquals("ii", TextClass.decodeBase37Name(342));
                assertEquals("I I", TextClass.formatDisplayName("i_i"));
                assertEquals(0L, TextClass.encodeBase37Name("\u0130\u0131"));
            }
        } finally { Locale.setDefault(previous); }
    }

    @Test public void canonicalNamesRoundTripAtDifferentWidthsAndSeparatorPositions() {
        String[] names = {"a", "9", "ab", "0a", "a_b", "a__b", "smokeone", "smoketwo",
                "abcdefghijkl", "999999999999", "a__________a", "0_9_a_z"};
        for (String name : names) {
            long value = TextClass.encodeBase37Name(name);
            assertTrue(value > 0 && value < 6582952005840035281L);
            assertEquals(name, TextClass.decodeBase37Name(value));
            assertEquals(value, TextClass.encodeBase37Name(TextClass.formatDisplayName(name)));
        }
    }

    @Test public void nullStringInputsKeepTheirFailureContract() {
        try { TextClass.encodeBase37Name(null); fail("null name"); } catch (NullPointerException expected) { }
        try { TextClass.formatDisplayName(null); fail("null display input"); } catch (NullPointerException expected) { }
    }

    @Test public void decoderReportsItsRuntimeFailureAndRethrowsWithoutCause() throws Exception {
        java.lang.reflect.Field digits = null;
        for (java.lang.reflect.Field field : TextClass.class.getDeclaredFields()) {
            if (field.getType() == char[].class) digits = field;
        }
        assertNotNull(digits);
        digits.setAccessible(true);
        java.lang.reflect.Field modifiers = java.lang.reflect.Field.class.getDeclaredField("modifiers");
        modifiers.setAccessible(true);
        int originalModifiers = digits.getModifiers();
        modifiers.setInt(digits, originalModifiers & ~java.lang.reflect.Modifier.FINAL);
        Object originalDigits = digits.get(null);
        java.io.PrintStream originalOutput = System.out;
        java.io.ByteArrayOutputStream captured = new java.io.ByteArrayOutputStream();
        try {
            // A shortened private digit table is a controlled fault, never normal input.
            digits.set(null, new char[] {'_'});
            System.setOut(new java.io.PrintStream(captured, true, "UTF-8"));
            assertEquals("invalid_name", TextClass.decodeBase37Name(0));
            assertEquals(0, captured.size());
            try {
                TextClass.decodeBase37Name(39); // ab: divide to 1, then lookup remainder 2.
                fail("corrupt table must fail");
            } catch (RuntimeException failure) {
                assertEquals(RuntimeException.class, failure.getClass());
                assertNull(failure.getCause());
                assertNull(failure.getMessage());
            }
            String diagnostic = captured.toString("UTF-8");
            assertTrue(diagnostic, diagnostic.contains("Error: 81570, 1, -99, java.lang.ArrayIndexOutOfBoundsException"));
        } finally {
            System.setOut(originalOutput);
            digits.set(null, originalDigits);
            modifiers.setInt(digits, originalModifiers);
        }
        assertEquals("ab", TextClass.decodeBase37Name(39));
    }
}
