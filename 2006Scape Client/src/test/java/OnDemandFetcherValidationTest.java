import static org.junit.Assert.*;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import org.junit.Test;

/** Controlled raw-file fixtures, written before pass 168 production changes. */
public class OnDemandFetcherValidationTest {
    private final OnDemandFetcher fetcher = new OnDemandFetcher();

    private boolean check(int a, int b, byte[] c) throws Exception {
        Method m = OnDemandFetcher.class.getDeclaredMethod("matchesVersionAndCrc32", int.class, int.class, byte[].class);
        m.setAccessible(true);
        return (Boolean) m.invoke(fetcher, a, b, c);
    }

    private static byte[] fixture(int a) {
        byte[] b = "123456789__".getBytes(StandardCharsets.US_ASCII);
        b[9] = (byte) (a >>> 8);
        b[10] = (byte) a;
        return b;
    }

    @Test public void rejectsMissingAndTruncatedData() throws Exception {
        assertFalse(check(0, 0, null));
        assertFalse(check(0, 0, new byte[0]));
        assertFalse(check(0, 0, new byte[1]));
        assertTrue(check(0, 0, new byte[2]));
    }

    @Test public void matchesThePublishedCrc32CheckVector() throws Exception {
        assertTrue(check(0x80fe, 0xcbf43926, fixture(0x80fe)));
        assertFalse(check(0xcbf43926, 0x80fe, fixture(0x80fe)));
    }

    @Test public void requiresBothIndependentComparisons() throws Exception {
        assertFalse(check(0x80ff, 0xcbf43926, fixture(0x80fe)));
        assertFalse(check(0x80fe, 0xcbf43927, fixture(0x80fe)));
        assertFalse(check(0x80ff, 0xcbf43927, fixture(0x80fe)));
    }

    @Test public void readsUnsignedBigEndianFinalTwoBytes() throws Exception {
        assertTrue(check(0x80fe, 0xcbf43926, fixture(0x80fe)));
        assertFalse(check(0xfe80, 0xcbf43926, fixture(0x80fe)));
        assertFalse(check((short) 0x80fe, 0xcbf43926, fixture(0x80fe)));
        assertTrue(check(65535, 0, new byte[] {-1, -1}));
        assertFalse(check(-1, 0, new byte[] {-1, -1}));
        assertFalse(check(65536, 0, new byte[] {0, 0}));
    }

    @Test public void excludesExactlyTwoTrailerBytes() throws Exception {
        assertTrue(check(1, 0, new byte[] {0, 1}));
        // CRC32 of the single zero byte is D202EF8D; it is not the empty CRC.
        assertTrue(check(1, 0xd202ef8d, new byte[] {0, 0, 1}));
        assertFalse(check(1, 0, new byte[] {0, 0, 1}));
        for (int a : new int[] {0, 1, 255, 256, 0x80fe, 65535}) {
            assertTrue(check(a, 0xcbf43926, fixture(a)));
        }
    }

    @Test public void detectsFirstAndLastPayloadByteChanges() throws Exception {
        byte[] a = fixture(0x80fe);
        a[0] ^= 1;
        assertFalse(check(0x80fe, 0xcbf43926, a));
        a = fixture(0x80fe);
        a[8] ^= 1;
        assertFalse(check(0x80fe, 0xcbf43926, a));
    }

    @Test public void resetsAccumulatedStateBetweenCalls() throws Exception {
        assertTrue(check(0x80fe, 0xcbf43926, fixture(0x80fe)));
        assertTrue(check(0, 0, new byte[2]));
        assertTrue(check(1, 0xd202ef8d, new byte[] {0, 0, 1}));
        assertTrue(check(0x80fe, 0xcbf43926, fixture(0x80fe)));
    }
}
