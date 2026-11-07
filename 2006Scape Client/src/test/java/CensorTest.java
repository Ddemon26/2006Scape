import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** Behaviour tests for the chat filter's helper routines, written against the original identifiers. */
public class CensorTest {
    private static final String[] FIELDS = {"allowedFragmentCodes", "badWords", "badWordExemptionPairs",
            "domains", "topLevelDomains", "topLevelDomainStrictness"};
    // Non-ASCII characters are built from code points so the source stays encoding independent.
    private static final char POUND = (char) 0xA3, EURO = (char) 0x20AC, E_ACUTE = (char) 0xE9, DEL = (char) 0x7F,
            CENT = (char) 0xA2, CURRENCY = (char) 0xA4, C1 = (char) 0x80, SOH = (char) 1, STX = (char) 2, NUL = 0, CR = '\r';
    private final Object[] saved = new Object[FIELDS.length];

    @Before public void saveFields() throws Exception {
        for (int i = 0; i < FIELDS.length; i++) saved[i] = field(FIELDS[i]).get(null);
    }

    @After public void restoreFields() throws Exception {
        for (int i = 0; i < FIELDS.length; i++) field(FIELDS[i]).set(null, saved[i]);
    }

    private static Field field(String name) throws Exception {
        Field f = Censor.class.getDeclaredField(name);
        f.setAccessible(true);
        return f;
    }

    private static Object call(String name, Object... args) throws Exception {
        for (Method m : Censor.class.getDeclaredMethods()) {
            if (!m.getName().equals(name) || m.getParameterTypes().length != args.length) continue;
            m.setAccessible(true);
            try {
                return m.invoke(null, args);
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof Exception) throw (Exception) e.getCause();
                throw e;
            }
        }
        throw new NoSuchMethodException(name);
    }

    private static String rep(int n) { char[] a = new char[n]; Arrays.fill(a, '*'); return new String(a); }
    private static char[] c(String s) { return s.toCharArray(); }
    private static String s(char[] a) { return new String(a); }

    /** Runs a char[]-mutating routine on text and returns the resulting text. */
    private static String mutate(String name, String text) throws Exception {
        char[] a = c(text);
        call(name, (Object) a);
        return s(a);
    }

    private static void setFragments(int... values) throws Exception { field("allowedFragmentCodes").set(null, values); }

    // ---- character set and whitespace sanitising ----

    @Test public void permittedCharactersAreAsciiPrintablePlusWhitespaceAndCurrency() throws Exception {
        char[] yes = {' ', '!', 'a', '~', DEL, '\n', '\t', POUND, EURO};
        char[] no = {NUL, (char) 0x1F, CR, C1, CENT, CURRENCY, (char) 0x20AD, E_ACUTE};
        for (char ch : yes) assertTrue("permitted " + (int) ch, (Boolean) call("isPermittedCharacter", ch));
        for (char ch : no) assertFalse("rejected " + (int) ch, (Boolean) call("isPermittedCharacter", ch));
    }

    @Test public void sanitiseReplacesDisallowedCharactersAndCollapsesSpaceRuns() throws Exception {
        assertEquals("a b c ", mutate("sanitizeCharacters", "a" + SOH + "b  c"));
        assertEquals(" a b  ", mutate("sanitizeCharacters", "  a" + CR + " b"));        // leading blank kept once; CR becomes blank
        assertEquals("ab    ", mutate("sanitizeCharacters", "ab" + SOH + " " + STX + " "));  // run folds to one, tail refilled
        assertEquals("a\nb\tc", mutate("sanitizeCharacters", "a\nb\tc"));              // newline and tab are permitted and kept
        assertEquals("a b", mutate("sanitizeCharacters", "a" + E_ACUTE + "b"));         // e acute is replaced by a blank
    }

    // ---- case restoration and capital lowering ----

    @Test public void restoreUpperCaseCopiesOnlyUppercaseLettersAndNeverUnmasks() throws Exception {
        char[] censored = c("a*c");
        call("restoreUpperCase", c("AbC"), censored);
        assertEquals("A*C", s(censored));                // masked position stays masked
        censored = c("abc");
        call("restoreUpperCase", c("AbC"), censored);
        assertEquals("AbC", s(censored));
        censored = c("Xyz");
        call("restoreUpperCase", c("abc"), censored);
        assertEquals("Xyz", s(censored));                // lowercase originals never overwrite
        censored = c("*");
        call("restoreUpperCase", c("A"), censored);
        assertEquals("*", s(censored));
    }

    @Test public void lowerTrailingCapitalsLowersUppercaseAfterTheFirstLowercaseLetterOfAWord() throws Exception {
        assertEquals("hello world", mutate("lowerTrailingCapitals", "hELLO wORLD"));
        assertEquals("HELLO", mutate("lowerTrailingCapitals", "HELLO"));
        assertEquals("Hello", mutate("lowerTrailingCapitals", "Hello"));
        assertEquals("Hello", mutate("lowerTrailingCapitals", "HeLLo"));
        assertEquals("ABc DE", mutate("lowerTrailingCapitals", "ABc DE"));  // second word resets
        assertEquals("a1B", mutate("lowerTrailingCapitals", "a1B"));         // digit starts a new word
    }

    // ---- separators, filler, digits ----

    @Test public void separatorIsAnyCharacterThatIsNotAnAsciiLetterOrDigit() throws Exception {
        for (char ch : new char[] {' ', '_', '\'', '*', '@', E_ACUTE, '/', '`', '{'}) assertTrue("sep " + (int) ch, (Boolean) call("isSeparator", ch));
        for (char ch : new char[] {'a', 'z', 'A', 'Z', '0', '9'}) assertFalse("alnum " + (int) ch, (Boolean) call("isSeparator", ch));
    }

    @Test public void fillerIsAnythingThatIsNotAnOrdinaryLowercaseLetter() throws Exception {
        for (char ch : new char[] {'v', 'x', 'j', 'q', 'z', 'A', '0', '-', '{', '`'}) assertTrue("filler " + (int) ch, (Boolean) call("isSkippableFiller", ch));
        for (char ch : new char[] {'a', 'b', 'i', 'k', 'y', 'u', 'w'}) assertFalse("not filler " + (int) ch, (Boolean) call("isSkippableFiller", ch));
    }

    @Test public void neighbourCodeIs1To26ForLettersThen28ForApostropheAndTheDigitsAfter() throws Exception {
        assertEquals((byte) 1, call("neighbourCode", 'a'));
        assertEquals((byte) 26, call("neighbourCode", 'z'));
        assertEquals((byte) 28, call("neighbourCode", '\''));
        assertEquals((byte) 29, call("neighbourCode", '0'));
        assertEquals((byte) 38, call("neighbourCode", '9'));
        for (char ch : new char[] {' ', 'A', '-', '`', '{'}) assertEquals((byte) 27, call("neighbourCode", ch));
    }

    @Test public void nextDigitAndEndOfDigitRunScans() throws Exception {
        assertEquals(2, call("findNextDigit", c("ab12c"), 0));
        assertEquals(3, call("findNextDigit", c("ab12c"), 3));       // starts inside the run
        assertEquals(-1, call("findNextDigit", c("ab12c"), 5));      // start at length
        assertEquals(-1, call("findNextDigit", c("ab"), 0));
        assertEquals(-1, call("findNextDigit", c("1"), -1));         // negative start stops the scan
        assertEquals(2, call("findDigitRunEnd", c("12ab"), 0));
        assertEquals(2, call("findDigitRunEnd", c("12"), 0));          // run reaches the end: length
        assertEquals(0, call("findDigitRunEnd", c("a1"), 0));
        assertEquals(2, call("findDigitRunEnd", c("a1b"), 1));
        assertEquals(2, call("findDigitRunEnd", c("ab"), -1));         // negative start: loop skipped, length returned
    }

    @Test public void fourConsecutiveByteSizedNumbersAreMasked() throws Exception {
        assertEquals("ip " + rep(11) + " x", mutate("censorIpAddresses", "ip 192.168.0.1 x"));
        assertEquals("ip 1.2.3", mutate("censorIpAddresses", "ip 1.2.3"));            // only three numbers
        assertEquals("1.2.3.256", mutate("censorIpAddresses", "1.2.3.256"));          // 256 is not a byte value
        assertEquals(rep(7) + ".5", mutate("censorIpAddresses", "1.2.3.4.5"));        // the mask covers exactly the first four
        assertEquals("1.2.a3.4", mutate("censorIpAddresses", "1.2.a3.4"));            // a real letter breaks the run
        assertEquals("123456789.1.2.3", mutate("censorIpAddresses", "123456789.1.2.3")); // more than 8 digits resets
        assertEquals(rep(7), mutate("censorIpAddresses", "1,2,3,4"));                 // any separator works
    }

    // ---- fragment table ----

    @Test public void fragmentCodeIsBase38WithLettersApostropheAndDigitsReadFromTheEnd() throws Exception {
        assertEquals(1, call("encodeFragment", c("a")));
        assertEquals(26, call("encodeFragment", c("z")));
        assertEquals(27, call("encodeFragment", c("'")));
        assertEquals(28, call("encodeFragment", c("0")));
        assertEquals(37, call("encodeFragment", c("9")));
        assertEquals(2 * 38 + 1, call("encodeFragment", c("ab")));   // the last character is the lowest digit
        assertEquals(1 * 38 + 2, call("encodeFragment", c("ba")));
        assertEquals(1, call("encodeFragment", new char[] {'a', NUL}));     // NUL contributes nothing
        assertEquals(0, call("encodeFragment", c("abcdefg")));             // longer than six characters
        assertEquals(0, call("encodeFragment", c("A")));                   // uppercase is not encodable
        assertEquals(0, call("encodeFragment", c("a-")));
        assertTrue((Integer) call("encodeFragment", c("abcdef")) > 0);
    }

    @Test public void allowedFragmentLookup() throws Exception {
        // codes: "a"=1, "b"=2, "ba"=40, "ab"=77, "z"=26
        setFragments(1, 26, 40, 77, 500);
        assertTrue((Boolean) call("isAllowedFragment", c("a")));          // first element
        assertTrue((Boolean) call("isAllowedFragment", c("ba")));        // middle element
        assertTrue((Boolean) call("isAllowedFragment", c("ab")));        // found by the search loop
        assertTrue((Boolean) call("isAllowedFragment", c("z")));
        assertFalse((Boolean) call("isAllowedFragment", c("b")));        // absent, between entries
        assertFalse((Boolean) call("isAllowedFragment", c("aa")));
        assertFalse((Boolean) call("isAllowedFragment", c("A")));        // unencodable gives code 0
        assertFalse((Boolean) call("isAllowedFragment", c("abcdefg")));  // too long gives code 0
        assertTrue((Boolean) call("isAllowedFragment", c("123")));       // digits are always allowed
        assertTrue((Boolean) call("isAllowedFragment", new char[] {'1', NUL}));
        assertTrue((Boolean) call("isAllowedFragment", new char[0]));
        assertTrue((Boolean) call("isAllowedFragment", new char[] {NUL, NUL}));
        assertFalse((Boolean) call("isAllowedFragment", c("12a")));      // digits plus a letter is looked up, absent
        setFragments(0, 1);
        assertTrue((Boolean) call("isAllowedFragment", c("a")));
        assertTrue((Boolean) call("isAllowedFragment", c("A")));         // code 0 is present in this table
    }

    // ---- simple and lookalike substitutions ----

    @Test public void simpleSubstitutionReturnsTheNumberOfTextCharactersConsumed() throws Exception {
        // simpleSubstitutionLength(textChar, patternChar, nextTextChar)
        assertEquals(1, call("simpleSubstitutionLength", 'a', 'a', 'x'));
        assertEquals(1, call("simpleSubstitutionLength", '0', 'o', 'x'));
        assertEquals(2, call("simpleSubstitutionLength", '(', 'o', ')'));
        assertEquals(0, call("simpleSubstitutionLength", '(', 'o', 'x'));
        assertEquals(1, call("simpleSubstitutionLength", '(', 'c', 'x'));
        assertEquals(1, call("simpleSubstitutionLength", '<', 'c', 'x'));
        assertEquals(1, call("simpleSubstitutionLength", '[', 'c', 'x'));
        assertEquals(0, call("simpleSubstitutionLength", '{', 'c', 'x'));
        assertEquals(1, call("simpleSubstitutionLength", EURO, 'e', 'x'));
        assertEquals(1, call("simpleSubstitutionLength", '$', 's', 'x'));
        assertEquals(1, call("simpleSubstitutionLength", 'i', 'l', 'x'));
        assertEquals(0, call("simpleSubstitutionLength", 'l', 'i', 'x'));       // not symmetric
        assertEquals(0, call("simpleSubstitutionLength", '0', 'a', 'x'));
        assertEquals(0, call("simpleSubstitutionLength", 'o', '0', 'x'));       // the pattern char decides
    }

    private static void sub(char next, char current, char target, int expected) throws Exception {
        // lookalikeSubstitutionLength(nextTextChar, currentTextChar, patternChar)
        assertEquals("target " + target + " current " + current + " next " + next, expected,
                call("lookalikeSubstitutionLength", next, current, target));
    }

    private static void subs(String currents, char target) throws Exception {
        for (char ch : currents.toCharArray()) sub('x', ch, target, 1);
    }

    @Test public void lookalikeTableAcceptsDisguisesOfTheTargetLetter() throws Exception {
        for (char t : "abcdefghijklmnopqrstuvwxyz0123456789,.!".toCharArray()) sub('x', t, t, 1); // identity
        subs("4@^", 'a');
        sub('\\', '/', 'a', 2);
        sub('x', '/', 'a', 0);
        subs("68", 'b');
        sub('3', '1', 'b', 2);
        sub('3', 'i', 'b', 2);
        sub('x', '1', 'b', 0);
        subs("({[<", 'c');
        sub(')', '[', 'd', 2);
        sub(')', 'i', 'd', 2);
        sub('x', '[', 'd', 0);
        subs("3" + EURO, 'e');
        sub('h', 'p', 'f', 2);
        subs(String.valueOf(POUND), 'f');
        sub('x', 'p', 'f', 0);
        subs("96q", 'g');
        subs("#", 'h');
        subs("ylj1!:;|", 'i');
        subs("1|i", 'l');
        subs("0*", 'o');
        sub(')', '(', 'o', 2);
        sub(']', '[', 'o', 2);
        sub('}', '{', 'o', 2);
        sub('>', '<', 'o', 2);
        sub('x', '(', 'o', 0);
        subs("5z$2", 's');
        subs("7+", 't');
        subs("v", 'u');
        sub('/', '\\', 'u', 2);
        sub('|', '\\', 'u', 2);
        sub('/', '|', 'u', 2);
        sub('/', '\\', 'v', 2);
        sub('v', 'v', 'w', 2);   // two v characters make a w
        sub('w', 'v', 'w', 0);
        sub('x', 'v', 'w', 0);
        sub('(', ')', 'x', 2);
        sub('{', '}', 'x', 2);
        sub('[', ']', 'x', 2);
        sub('<', '>', 'x', 2);
        subs("oO", '0');
        sub(')', '(', '0', 2);
        subs("l", '1');
        subs(".", ',');
        subs(",", '.');
        subs("i", '!');
    }

    @Test public void lookalikeTableRejectsEverythingButIdentityForUndisguisableTargets() throws Exception {
        for (char t : "jkmnpqryz23456789'".toCharArray()) {
            for (int ch = 0; ch < 256; ch++) {
                if (ch == t) continue;
                for (char next : new char[] {'x', ')', '/', '\\', '3', 'h', ']', '}', '>'}) {
                    assertEquals("target " + t + " current " + ch, 0, call("lookalikeSubstitutionLength", next, (char) ch, t));
                }
            }
        }
    }

    // ---- reading the data tables ----

    private static Stream bytes(int... values) {
        byte[] b = new byte[values.length];
        for (int i = 0; i < b.length; i++) b[i] = (byte) values[i];
        return new Stream(b);
    }

    @Test public void badWordEntriesReadWordsAndOptionalPairLists() throws Exception {
        Stream stream = bytes(2, 'a', 'b', 1, 3, 4,   1, 'c', 0,   3, 'x', 'y', 'z', 2, 5, 6, 7, 8);
        char[][] words = new char[3][];
        byte[][][] pairs = new byte[3][][];
        call("readBadWordEntries", stream, words, pairs);
        assertEquals("ab", s(words[0]));
        assertEquals("c", s(words[1]));
        assertEquals("xyz", s(words[2]));
        assertArrayEquals(new byte[][] {{3, 4}}, pairs[0]);
        assertNull(pairs[1]);                                       // zero pairs stays null
        assertArrayEquals(new byte[][] {{5, 6}, {7, 8}}, pairs[2]);
        assertEquals(18, stream.currentOffset);
    }

    @Test public void domainEntriesReadLengthPrefixedWords() throws Exception {
        Stream stream = bytes(3, 'f', 'o', 'o', 0, 2, 'h', 'i');
        char[][] words = new char[3][];
        call("readDomainEntries", words, stream);
        assertEquals("foo", s(words[0]));
        assertEquals("", s(words[1]));
        assertEquals("hi", s(words[2]));
    }

    @Test public void tablesAreStoredIntoTheMatchingFieldsByTheNamedReaders() throws Exception {
        call("readBadEnc", bytes(0, 0, 0, 1, 1, 'a', 0));
        assertEquals("a", s(((char[][]) field("badWords").get(null))[0]));
        assertNull(((byte[][][]) field("badWordExemptionPairs").get(null))[0]);
        call("readDomainEnc", bytes(0, 0, 0, 1, 2, 'a', 'b'));
        assertEquals("ab", s(((char[][]) field("domains").get(null))[0]));
        call("readFragmentsEnc", bytes(0, 0, 0, 2, 0, 5, 1, 0));
        assertArrayEquals(new int[] {5, 256}, (int[]) field("allowedFragmentCodes").get(null));
        call("readTldList", bytes(0, 0, 0, 1, 2, 3, 'c', 'o', 'm'));
        assertEquals("com", s(((char[][]) field("topLevelDomains").get(null))[0]));
        assertArrayEquals(new int[] {2}, (int[]) field("topLevelDomainStrictness").get(null));
    }

    // ---- boundary classification ----

    @Test public void domainPrefixClassification() throws Exception {
        // classifyDomainPrefix(text, atMaskedText, start): 2 text start, 3 '@' before, 4 three masks before, 1 separator, 0 letter
        assertEquals(2, call("classifyDomainPrefix", c("abc"), c("abc"), 0));
        assertEquals(3, call("classifyDomainPrefix", c("a@b"), c("a@b"), 2));
        assertEquals(3, call("classifyDomainPrefix", c("a@ x"), c("a@ x"), 3));      // '@' reached through separators
        assertEquals(1, call("classifyDomainPrefix", c("@a x"), c("@a x"), 3));      // letter in between hides the '@'
        assertEquals(1, call("classifyDomainPrefix", c("ab x"), c("ab x"), 3));
        assertEquals(0, call("classifyDomainPrefix", c("abx"), c("abx"), 2));
        assertEquals(4, call("classifyDomainPrefix", c("(a)x"), c("***x"), 3));
        assertEquals(1, call("classifyDomainPrefix", c("(a)x"), c("**.x"), 3));      // only two masks plus a dot
        assertEquals(4, call("classifyDomainPrefix", c("ab ***x"), c("ab ***x"), 6)); // masks in the masked copy are counted
        assertEquals(1, call("classifyDomainPrefix", c("a**x"), c("a**x"), 3));      // two masks only
    }

    @Test public void domainSuffixClassification() throws Exception {
        // classifyDomainSuffix(dotMaskedText, lastMatchedIndex, text): 2 text end, 3 '.'/',' after, 4 three masks after
        assertEquals(2, call("classifyDomainSuffix", c("abc"), 2, c("abc")));
        assertEquals(3, call("classifyDomainSuffix", c("ab.c"), 1, c("ab.c")));
        assertEquals(3, call("classifyDomainSuffix", c("ab,c"), 1, c("ab,c")));
        assertEquals(3, call("classifyDomainSuffix", c("ab .c"), 1, c("ab .c")));
        assertEquals(1, call("classifyDomainSuffix", c("ab c"), 1, c("ab c")));
        assertEquals(0, call("classifyDomainSuffix", c("abc"), 0, c("abc")));
        assertEquals(4, call("classifyDomainSuffix", c("a***"), 0, c("adot")));
        assertEquals(0, call("classifyDomainSuffix", c("a**t"), 0, c("adot")));
    }

    @Test public void tldPrefixClassification() throws Exception {
        // classifyTldPrefix(text, start, dotMaskedText): 2 start, 3 ',' or '.' before, 4 three masks before
        assertEquals(2, call("classifyTldPrefix", c("com"), 0, c("com")));
        assertEquals(3, call("classifyTldPrefix", c("a.com"), 2, c("a.com")));
        assertEquals(3, call("classifyTldPrefix", c("a,com"), 2, c("a,com")));
        assertEquals(3, call("classifyTldPrefix", c("a. com"), 3, c("a. com")));
        assertEquals(1, call("classifyTldPrefix", c("a com"), 2, c("a com")));
        assertEquals(1, call("classifyTldPrefix", c("a@com"), 2, c("a@com")));      // '@' is not a dot here
        assertEquals(0, call("classifyTldPrefix", c("acom"), 1, c("acom")));
        assertEquals(4, call("classifyTldPrefix", c("dot com"), 4, c("*** com")));
        assertEquals(1, call("classifyTldPrefix", c("do com"), 3, c("** com")));
    }

    @Test public void tldSuffixClassification() throws Exception {
        // classifyTldSuffix(text, slashMaskedText, lastMatchedIndex): 2 end, 3 '/' or '\' after, 4 five masks after
        assertEquals(2, call("classifyTldSuffix", c("abc"), c("abc"), 2));
        assertEquals(3, call("classifyTldSuffix", c("a/b"), c("a/b"), 0));
        assertEquals(3, call("classifyTldSuffix", c("a\\b"), c("a\\b"), 0));
        assertEquals(3, call("classifyTldSuffix", c("a /b"), c("a /b"), 0));
        assertEquals(1, call("classifyTldSuffix", c("a b"), c("a b"), 0));
        assertEquals(0, call("classifyTldSuffix", c("ab"), c("ab"), 0));
        assertEquals(4, call("classifyTldSuffix", c("aslash"), c("a*****"), 0));
        assertEquals(0, call("classifyTldSuffix", c("aslash"), c("a****h"), 0));    // four masks are not enough
        assertEquals(1, call("classifyTldSuffix", c("a slas"), c("a ****"), 0));
        assertEquals(1, call("classifyTldSuffix", c("a ,"), c("a ,"), 0));         // ',' is not a slash here
    }

    // ---- exemption pair search ----

    @Test public void exemptionSearchFindsPairsInTheSortedList() throws Exception {
        byte[][] pairs = {{1, 1}, {5, 5}, {10, 3}, {24, 26}, {27, 27}};
        // isExemptNeighbourPair(previousCode, pairs, nextCode)
        for (byte[] p : pairs) assertTrue("" + p[0] + "," + p[1], (Boolean) call("isExemptNeighbourPair", p[0], pairs, p[1]));
        assertFalse((Boolean) call("isExemptNeighbourPair", (byte) 10, pairs, (byte) 4));
        assertFalse((Boolean) call("isExemptNeighbourPair", (byte) 24, pairs, (byte) 25));
        assertFalse((Boolean) call("isExemptNeighbourPair", (byte) 26, pairs, (byte) 26));
        assertFalse((Boolean) call("isExemptNeighbourPair", (byte) 3, pairs, (byte) 10));      // order matters
        assertFalse((Boolean) call("isExemptNeighbourPair", (byte) 0, pairs, (byte) 0));
        byte[][] single = {{24, 26}};
        assertTrue((Boolean) call("isExemptNeighbourPair", (byte) 24, single, (byte) 26));
        assertFalse((Boolean) call("isExemptNeighbourPair", (byte) 24, single, (byte) 25));
    }

    // ---- bad word matching ----

    private static String word(String text, String word, byte[][] exemptions) throws Exception {
        char[] a = c(text);
        call("censorWord", exemptions, a, c(word));
        return s(a);
    }

    private static String word(String text, String word) throws Exception { return word(text, word, null); }

    @Test public void wordMatcherMasksExactAndLookalikeMatches() throws Exception {
        setFragments(1);
        assertEquals("a *** b", word("a bad b", "bad"));
        assertEquals("***", word("b4d", "bad"));
        assertEquals("***", word("b@d", "bad"));
        assertEquals("***", word("8@d", "bad"));
        assertEquals("BAD", word("BAD", "bad"));                       // uppercase is not matched here
        assertEquals("ba", word("ba", "bad"));                         // text shorter than the word
        assertEquals("*****", word("bbbad", "bad"));                   // repeated first letter is absorbed
        assertEquals("****", word("baad", "bad"));                     // repeated inner letter is absorbed
        assertEquals("*** ***", word("bad bad", "bad"));              // every occurrence
    }

    @Test public void wordMatcherSkipsFillerWithinAMatch() throws Exception {
        setFragments(1);
        assertEquals("*****", word("b-a-d", "bad"));
        assertEquals("x *****", word("x b-a-d", "bad"));
        assertEquals("**** x", word("b-ad x", "bad"));
        assertEquals("b-a-x", word("b-a-x", "bad"));                   // incomplete match masks nothing
    }

    @Test public void wordMatcherAbandonsMatchesThatAreMostlyFiller() throws Exception {
        setFragments(1);
        String ten = "b----------ad";     // 10 fillers: 10*100/11 = 90 is not above 90
        String eleven = "b-----------ad";                // 11 fillers: 11*100/12 = 91 is above 90
        assertEquals(rep(13), word(ten, "bad"));
        assertEquals(eleven, word(eleven, "bad"));
    }

    @Test public void digitDisguisesAreMaskedOnlyWhenLettersOutnumberLeadingDigits() throws Exception {
        setFragments(1);
        assertEquals("444s", word("444s", "aaas"));      // 3 digits against 1 letter: left alone
        assertEquals("****", word("44as", "aaas"));      // 2 digits against 2 letters: masked
        assertEquals("***", word("a55", "ass"));         // trailing digits are not counted against it
        assertEquals("***", word("a5s", "ass"));
    }

    @Test public void wordMatcherHonoursNeighbourExemptionsOnlyWithoutFiller() throws Exception {
        setFragments(1);
        byte[][] ex = {{24, 26}};        // previous letter 'x' (24), next letter 'z' (26)
        assertEquals("xbadz", word("xbadz", "bad", ex));
        assertEquals("x***y", word("xbady", "bad", ex));
        assertEquals("w***z", word("wbadz", "bad", ex));
        assertEquals("x***z", word("xbadz", "bad", new byte[][] {{1, 2}}));
        assertEquals("x***z", word("xbadz", "bad", null));
        assertEquals(" bad ", word(" bad ", "bad", new byte[][] {{27, 27}}));       // spaces map to code 27
        assertEquals("x'bad'z", word("x'bad'z", "bad", new byte[][] {{28, 28}}));   // apostrophe is code 28
    }

    @Test public void embeddedFillerMatchIsMaskedUnlessEveryNearbyFragmentIsAllowed() throws Exception {
        // text "xb-a-dx": the match "b-a-d" sits inside a larger word. Windows checked: "xb"(100) "a"(1) "dx"(916)
        setFragments(1, 100, 916);
        assertEquals("xb-a-dx", word("xb-a-dx", "bad"));
        setFragments(1, 100);
        assertEquals("x*****x", word("xb-a-dx", "bad"));
        setFragments(100, 916);
        assertEquals("x*****x", word("xb-a-dx", "bad"));
        assertEquals("*****", word("b-a-d", "bad"));                     // both edges are boundaries: no fragment check
        assertEquals("x *****", word("x b-a-d", "bad"));
    }

    // ---- domain and TLD filters ----

    private static String domain(String text, String domain, String dotMasked, String atMasked) throws Exception {
        char[] a = c(text);
        call("censorDomain", a, c(domain), c(dotMasked), c(atMasked));
        return s(a);
    }

    private static String domain(String text, String domain) throws Exception { return domain(text, domain, text, text); }

    @Test public void domainMatchesAreMaskedAtTextEdgesBeforeAtAndBeforeDots() throws Exception {
        assertEquals("visit ***.bar", domain("visit foo.bar", "foo"));
        assertEquals("visit foo bar", domain("visit foo bar", "foo"));
        assertEquals("foo bar", domain("foo bar", "foo"));              // text start alone is not enough
        assertEquals("a@*** b", domain("a@foo b", "foo"));
        assertEquals("visitfoo bar", domain("visitfoo bar", "foo"));
        assertEquals("visit***.bar", domain("visitfoo.bar", "foo"));
        assertEquals("xfoo y", domain("xfoo y", "foo"));
        assertEquals("f00", domain("f00", "foo"));                      // nor is text end
        assertEquals("***.x", domain("f00.x", "foo"));
        assertEquals("*****.x", domain("f.o.o.x", "foo"));
    }

    @Test public void domainRunsAreMaskedWhenAtOrDotAreSpelledOut() throws Exception {
        // "(a)" before the match and "dot" after it, given as masked copies
        assertEquals("joe(a)*** bar", domain("joe(a)foo bar", "foo", "joe(a)foo bar", "joe***foo bar"));
        assertEquals("a *** dot com", domain("a foo dot com", "foo", "a foo *** com", "a foo dot com"));
        assertEquals("a foo dot com", domain("a foo dot com", "foo"));
    }

    @Test public void domainFilterBuildsAtAndDotMasksAndMasksEveryTableDomain() throws Exception {
        field("domains").set(null, new char[][] {c("foo"), c("bar")});
        assertEquals("joe(a)*** x", mutate("censorDomains", "joe(a)foo x"));
        assertEquals("*** dot com", mutate("censorDomains", "foo dot com"));
        assertEquals("a foo b", mutate("censorDomains", "a foo b"));
        assertEquals("joe@***", mutate("censorDomains", "joe@bar"));
        assertEquals("joe(@)*** x", mutate("censorDomains", "joe(@)foo x"));    // lookalike spelling of "(a)"
    }

    private static String tld(String text, String tld, int mode, String dotMasked, String slashMasked) throws Exception {
        char[] a = c(text);
        call("censorTopLevelDomain", c(slashMasked), c(tld), mode, c(dotMasked), a);
        return s(a);
    }

    private static String tld(String text, String tld, int mode) throws Exception { return tld(text, tld, mode, text, text); }

    @Test public void tldStrictnessLevelsDecideWhichSurroundingsCensor() throws Exception {
        assertEquals("foo *** bar", tld("foo com bar", "com", 1));
        assertEquals("foo com bar", tld("foo com bar", "com", 2));
        assertEquals("foo com bar", tld("foo com bar", "com", 3));
        assertEquals("foo " + rep(7), tld("foo com/bar", "com", 3));          // suffix is a slash
        assertEquals("foo.com bar", tld("foo.com bar", "com", 3));            // dot before is not enough in mode 3
        assertEquals(rep(7) + " bar", tld("foo.com bar", "com", 2));          // dot before is enough in mode 2
    }

    @Test public void tldMasksExtendOverTheAdjacentWordsWhenThePrefixOrSuffixIsADelimiter() throws Exception {
        assertEquals(rep(11), tld("foo.com/bar", "com", 1));
        assertEquals(rep(7), tld("foo.com", "com", 1));
        assertEquals(rep(7), tld("foo.com", "com", 2));
        assertEquals(rep(7) + " bar", tld("foo.com bar", "com", 1));
    }

    @Test public void tldFilterBuildsDotAndSlashMasksAndAppliesEveryListedTld() throws Exception {
        field("topLevelDomains").set(null, new char[][] {c("com")});
        field("topLevelDomainStrictness").set(null, new int[] {1});
        assertEquals(rep(21), mutate("censorTopLevelDomains", "foo dot com slash bar"));
        assertEquals("foo *** bar", mutate("censorTopLevelDomains", "foo com bar"));
        assertEquals("foo net bar", mutate("censorTopLevelDomains", "foo net bar"));
    }

    // ---- full pipeline pieces ----

    @Test public void badWordFilterRunsTwiceSoMasksCanEnableFurtherMatches() throws Exception {
        setFragments(1);
        field("badWords").set(null, new char[][] {c("ab"), c("oc")});
        field("badWordExemptionPairs").set(null, new byte[2][][]);
        assertEquals("***", mutate("censorBadWords", "abc"));       // 'o' matches the mask character
        field("badWords").set(null, new char[][] {c("ab")});
        field("badWordExemptionPairs").set(null, new byte[1][][]);
        assertEquals("** c", mutate("censorBadWords", "ab c"));
    }

    @Test public void doCensorRunsTheFilterButReturnsItsInputUnchanged() throws Exception {
        setFragments(1);
        field("badWords").set(null, new char[][] {c("bad")});
        field("badWordExemptionPairs").set(null, new byte[1][][]);
        field("domains").set(null, new char[][] {c("foo")});
        field("topLevelDomains").set(null, new char[][] {c("com")});
        field("topLevelDomainStrictness").set(null, new int[] {1});
        for (String text : new String[] {"bad", "Hello BAD foo.com 1.2.3.4", "", "x" + SOH + "y   z"}) {
            assertEquals(text, Censor.doCensor(text));
        }
    }
}
