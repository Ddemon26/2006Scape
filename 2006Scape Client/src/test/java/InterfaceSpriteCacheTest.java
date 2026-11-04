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
