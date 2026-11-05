import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Base64;
import java.util.Locale;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class InterfaceSpriteCacheTest {
    private Field cacheField;
    private Method loader;
    private Object previousCache;
    private Locale previousLocale;

    @Before
    public void isolateSpriteCacheAndLocale() throws Exception {
        previousLocale = Locale.getDefault();
        Locale.setDefault(Locale.ENGLISH);
        cacheField = RSInterface.class.getDeclaredField("spriteCache");
        cacheField.setAccessible(true);
        previousCache = cacheField.get(null);
        cacheField.set(null, new MRUNodes(20));
        loader = RSInterface.class.getDeclaredMethod("loadCachedSprite", int.class, StreamLoader.class, String.class);
        loader.setAccessible(true);
    }

    @After
    public void restoreSpriteCacheAndLocale() throws Exception {
        cacheField.set(null, previousCache);
        Locale.setDefault(previousLocale);
    }

    @Test
    public void hashUsesOrderedUppercaseNameCharactersRatherThanBase37Names() {
        assertEquals(0L, TextClass.hashSpriteName(""));
        assertEquals(33L, TextClass.hashSpriteName("A"));
        assertEquals(33L, TextClass.hashSpriteName("a"));
        assertEquals(2047L, TextClass.hashSpriteName("ab")); // 33 * 61 + 34
        assertEquals(2107L, TextClass.hashSpriteName("ba")); // 34 * 61 + 33
        assertEquals(16L, TextClass.hashSpriteName("0"));
        assertEquals(63L, TextClass.hashSpriteName("_"));
        assertEquals(460226508L, TextClass.hashSpriteName("a.dat"));
        // This is a hash, not a unique encoding: a leading space contributes zero.
        assertEquals(TextClass.hashSpriteName("a"), TextClass.hashSpriteName(" a"));
        assertEquals(0L, TextClass.hashSpriteName(" "));
    }

    @Test
    public void hashFoldsHighBitsRatherThanJustTruncatingAtFiftySixBits() {
        // Independently evaluated weighted polynomials, reduced modulo 2^56 - 1.
        assertEquals(105492274179892L, TextClass.hashSpriteName("ABCDEFGH"));
        assertEquals(6435028724973453L, TextClass.hashSpriteName("ABCDEFGHI"));
        assertEquals(32248782033741000L, TextClass.hashSpriteName("ABCDEFGHIJ"));
        // Simply masking the ten-character polynomial would instead give ...0995.
        assertEquals(43866651795376896L, TextClass.hashSpriteName("ZZZZZZZZZZZZ"));
        assertEquals(38436242612567615L, TextClass.hashSpriteName(repeat('Z', 80)));
    }

    @Test
    public void hashRetainsDefaultLocaleAndUppercaseExpansion() {
        assertEquals(41L, TextClass.hashSpriteName("i"));
        assertEquals(41L, TextClass.hashSpriteName("I"));
        assertEquals(3162L, TextClass.hashSpriteName("\u00df")); // Uppercase SS, not one character.
        Locale.setDefault(new Locale("tr", "TR"));
        assertEquals(272L, TextClass.hashSpriteName("i")); // U+0130 minus 32.
        assertEquals(41L, TextClass.hashSpriteName("I"));
        assertEquals(41L, TextClass.hashSpriteName("\u0131"));
    }

    @Test
    public void loaderSelectsArchiveNameAndSpriteIndexAndReusesEachEntry() throws Exception {
        StreamLoader archive = archive();
        Sprite a0 = load(0, archive, "a");
        Sprite a1 = load(1, archive, "a");
        Sprite b0 = load(0, archive, "b");
        assertEquals(1, a0.width);
        assertEquals(1, a0.height);
        assertArrayEquals(new int[] {0xff0000}, a0.pixels);
        assertArrayEquals(new int[] {0x0000ff}, a1.pixels);
        assertArrayEquals(new int[] {0x0000ff}, b0.pixels);
        assertNotSame(a0, a1);
        assertNotSame(a1, b0);
        assertSame(a0, load(0, archive, "a"));
        assertSame(a1, load(1, archive, "a"));
        assertSame(b0, load(0, archive, "b"));
    }

    @Test
    public void cacheFieldHoldsSpritesUnderTheShiftedNameHashPlusIndex() throws Exception {
        MRUNodes cache = (MRUNodes) cacheField.get(null);
        assertNull(cache.insertFromCache(8448L));
        Sprite a0 = load(0, archive(), "a");
        Sprite a1 = load(1, archive(), "a");
        assertSame(a0, cache.insertFromCache(8448L)); // 33 * 256 + 0
        assertSame(a1, cache.insertFromCache(8449L)); // 33 * 256 + 1
        cacheField.set(null, new MRUNodes(20));
        Sprite fresh = load(0, archive(), "a");
        assertNotSame(a0, fresh);
        assertArrayEquals(a0.pixels, fresh.pixels);
        assertSame(a0, cache.insertFromCache(8448L));
    }

    @Test
    public void cacheHitUsesCaseFoldedNameWithoutReadingTheSuppliedArchive() throws Exception {
        Sprite a0 = load(0, archive(), "a");
        assertSame(a0, load(0, null, "A"));
        assertSame(a0, load(0, null, " a")); // Known hash collision also hits.
        assertNull(load(1, null, "a")); // A different index misses and attempts to load.
        assertNull(load(0, null, "b")); // A different name misses.
    }

    @Test
    public void failedLoadsReturnNullWithoutCachingAFailure() throws Exception {
        assertNull(load(0, archive(), "missing"));
        assertNull(load(0, null, "a"));
        Sprite a0 = load(0, archive(), "a");
        assertArrayEquals(new int[] {0xff0000}, a0.pixels);
        assertSame(a0, load(0, null, "a"));
    }

    @Test
    public void indexOutsideEightBitsCanAliasAnotherNameInsteadOfBeingValidated() throws Exception {
        Sprite b0 = load(0, archive(), "b");
        // (33 << 8) + 256 == (34 << 8) + 0. No claim of collision freedom.
        assertSame(b0, load(256, null, "a"));
    }


    @Test
    public void everyPrintablePrefixMatchesAnIndependentWeightedPolynomial() {
        java.math.BigInteger modulus = java.math.BigInteger.ONE.shiftLeft(56).subtract(java.math.BigInteger.ONE);
        java.math.BigInteger base = java.math.BigInteger.valueOf(61);
        for (String input : new String[] {"", " aB9_.dat", "\u00dfi\u0131", repeat('Z', 100),
                "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789 !?.dat"}) {
            for (int length = 0; length <= input.length(); length++) {
                String prefix = input.substring(0, length);
                String uppercase = prefix.toUpperCase(Locale.ENGLISH);
                java.math.BigInteger polynomial = java.math.BigInteger.ZERO;
                // Sum positional weights at arbitrary precision; no production folding loop.
                for (int position = 0; position < uppercase.length(); position++) {
                    polynomial = polynomial.add(base.pow(uppercase.length() - position - 1)
                            .multiply(java.math.BigInteger.valueOf(uppercase.charAt(position) - 32)));
                }
                assertEquals("prefix=" + prefix, polynomial.mod(modulus).longValue(),
                        TextClass.hashSpriteName(prefix));
            }
        }
    }

    @Test
    public void signedIndexIsAddedToTheKeyWithoutMaskingOrRangeValidation() throws Exception {
        int[] indices = {Integer.MIN_VALUE, -257, -256, -1, 0, 1, 255, 256, Integer.MAX_VALUE};
        long[] keys = {-2147475200L, 8191L, 8192L, 8447L, 8448L, 8449L, 8703L, 8704L, 2147492095L};
        MRUNodes cache = (MRUNodes) cacheField.get(null);
        for (int n = 0; n < indices.length; n++) {
            Sprite marker = new Sprite(1, 1);
            marker.pixels[0] = n + 1;
            cache.removeFromCache(marker, keys[n]);
            assertSame("signed index=" + indices[n], marker, load(indices[n], null, "a"));
        }
        // Negative-index counterpart to the existing a,256 / b,0 alias.
        assertSame(load(0, null, "a"), load(-256, null, "b"));
    }

    @Test
    public void suppliedArchiveControlsMissesButIsIgnoredOnAHit() throws Exception {
        StreamLoader redFirst = archive();
        StreamLoader blueFirst = new StreamLoader(Base64.getDecoder().decode("AABBAABWMUFZJlNZlEXzAAAADeXU/IAALiAAAAEAAIACAQACBAAAoABUQjTTIyYQSnqUPU0Gh6jciywvN7mONqE1ZcJHrEsGiKHyVVxFSlWRfF3JFOFCQlEXzAA="));
        Sprite first = load(0, redFirst, "a");
        assertArrayEquals(new int[] {0xff0000}, first.pixels);
        assertSame(first, load(0, blueFirst, "a"));
        cacheField.set(null, new MRUNodes(20));
        Sprite second = load(0, blueFirst, "a");
        assertNotSame(first, second);
        assertArrayEquals(new int[] {0x0000ff}, second.pixels);
        assertSame(second, load(0, redFirst, "a"));
    }

    @Test
    public void outOfArchiveIndexFailureDoesNotPoisonLaterValidLoads() throws Exception {
        assertNull(load(2, archive(), "a"));
        assertNull(((MRUNodes) cacheField.get(null)).insertFromCache(8450L));
        Sprite valid = load(1, archive(), "a");
        assertArrayEquals(new int[] {0x0000ff}, valid.pixels);
        assertSame(valid, load(1, null, "a"));
        assertNull(load(2, archive(), "a"));
    }

    private Sprite load(int index, StreamLoader archive, String name) throws Exception {
        return (Sprite) loader.invoke(null, index, archive, name);
    }

    private static StreamLoader archive() {
        // Whole-archive bzip2 fixture: index.dat plus a.dat and b.dat, each with
        // two 1x1 sprites. a: red, blue; b: blue, red. No external game cache.
        return new StreamLoader(Base64.getDecoder().decode(
                "AABBAABWMUFZJlNZHYI1FQAADeXU/IAALiAAAAEAAIACAQACBAAAoABUQjTTIyYQSp+pI9EGh6jUCwhfmsGeJwJswqW9S2Uuih8kzURUpVkXxdyRThQkB2CNRUA="));
    }

    private static String repeat(char character, int count) {
        char[] characters = new char[count];
        java.util.Arrays.fill(characters, character);
        return new String(characters);
    }
}
