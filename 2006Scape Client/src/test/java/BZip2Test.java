import static org.junit.Assert.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Base64;
import org.junit.Test;

/**
 * Pass 162: semantic tests for the bzip2 stream decoder (headerless "BZh1" stream) and its decoder state.
 * Fixtures are real bzip2 streams made by Python's bz2 module with the 4-byte "BZh1" header removed; the
 * expected header facts (origin pointer, selectors, code lengths, symbol map) come from an independent
 * bit-level parse of the stream and a brute-force Burrows-Wheeler sort (gen_fixtures.py).
 */
public class BZip2Test {
    // Single place that names the decoder members under test.
    private static final String DECODER = "BZip2Decompressor";
    private static final String STATE = "BZip2State";
    private static final String DECODE = "decompress";
    private static final String DECODE_BLOCKS = "decodeBlocks";
    private static final String EMIT_BLOCK = "emitBlockOutput";
    private static final String READ_BYTE = "readByte";
    private static final String READ_BIT = "readBit";
    private static final String READ_BITS = "readBits";
    private static final String BUILD_SYMBOL_MAP = "buildSymbolMap";
    private static final String BUILD_TABLES = "buildDecodeTables";
    private static final String F_INPUT = "input", F_IN_POS = "inputPos", F_IN_LEFT = "inputRemaining",
        F_IN_TOTAL_LO = "inputTotalLow", F_IN_TOTAL_HI = "inputTotalHigh", F_OUTPUT = "output", F_OUT_POS = "outputPos",
        F_OUT_LEFT = "outputRemaining", F_OUT_TOTAL_LO = "outputTotalLow", F_OUT_TOTAL_HI = "outputTotalHigh", F_PENDING_CH = "pendingByte",
        F_PENDING_LEN = "pendingRepeat", F_RANDOMISED = "randomised", F_BIT_BUF = "bitBuffer", F_BIT_COUNT = "bitCount",
        F_BLOCK_SIZE = "blockSize100k", F_BLOCKS = "blockCount", F_ORIG_PTR = "originPointer", F_TPOS = "nextIndex", F_K0 = "lookaheadByte",
        F_UNZFTAB = "byteCounts", F_USED = "blockPosition", F_CFTAB = "cumulativeCounts", F_TT = "inverseBwtTable",
        F_N_IN_USE = "symbolsInUse", F_IN_USE = "byteInUse", F_IN_USE16 = "rangeInUse", F_SEQ = "symbolToByte",
        F_MTF = "aByteArray592", F_MTF_BASE = "anIntArray593", F_SELECTOR = "selectors", F_SELECTOR_MTF = "selectorMtf",
        F_LEN = "codeLengths", F_LIMIT = "limit", F_BASE = "base",
        F_PERM = "perm", F_MIN_LEN = "minLengths", F_NBLOCK = "blockLength";

    // BEGIN FIXTURES
    private static final String TINY_B64 = "MUFZJlNZdmaMQgAAA5GAQAAGRJCAIAAgqoaegQwI7ERX7WhjEaLuSKcKEg7M0YhA";
    private static final int[] TINY_FACTS = {23, 23, 7, 2, 1, 10};
    private static final int[] TINY_SELECTORS = {0};
    private static final int[] TINY_SELECTOR_MTF = {0};
    private static final int[] TINY_SEQ_TO_UNSEQ = {32,100,101,104,108,111,114,119};
    private static final int[][] TINY_LENS = {{1,5,5,5,4,5,3,4,4,4},{4,4,3,3,3,3,3,3,4,4}};
    private static final int[] TINY_IN_USE16 = {0,0,1,0,0,0,1,1,0,0,0,0,0,0,0,0};
    private static final String RUNS_B64 = "MUFZJlNZpcSoqwAAAlEA4AAAID8AAAggADEAMCv1T1No0lZeScZ2i2bEQ9qHS+LuSKcKEhS4lRVg";
    private static final int[] RUNS_FACTS = {572, 34, 4, 2, 1, 12};
    private static final int[] RUNS_SELECTORS = {1};
    private static final int[] RUNS_SELECTOR_MTF = {1};
    private static final int[] RUNS_SEQ_TO_UNSEQ = {0,1,41,97,98,99,100,101,102,251};
    private static final int[][] RUNS_LENS = {{4,4,4,4,4,4,4,4,3,3,3,3},{5,2,5,3,5,4,3,4,4,3,4,5}};
    private static final int[] RUNS_IN_USE16 = {1,0,1,0,0,0,1,0,0,0,0,0,0,0,0,1};
    private static final String SPARSE_B64 = "MUFZJlNZ6jw98QAABUBAggBAAEAAIAAwzAUppk6mTxdyRThQkOo8PfE=";
    private static final int[] SPARSE_FACTS = {31, 31, 10, 2, 1, 5};
    private static final int[] SPARSE_SELECTORS = {1};
    private static final int[] SPARSE_SELECTOR_MTF = {1};
    private static final int[] SPARSE_SEQ_TO_UNSEQ = {5,128,240};
    private static final int[][] SPARSE_LENS = {{3,3,2,2,2},{1,2,4,3,4}};
    private static final int[] SPARSE_IN_USE16 = {1,0,0,0,0,0,0,0,1,0,0,0,0,0,0,1};
    private static final String WIDE_B64 = "MUFZJlNZscb56QAAAP////////////////////////////////////////////+wAWABR+pv/9VVQAAAATAAAAATJgAAAAAAAAAmAAAAAAAAAAAAAAAAAAAJkaYJMABMAATAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABJgAJgACYAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAQIEGgQUEQdCEJQpC0MQ1DkPRBEUSRNFEVRZF0YRlGkbRxHUeR9IEhSJ/CRpIkqTJOlCUpUlaWJalyXpgmKZJmmiapsm6cJynSdp4nqfJ+oCgv5UJQ39KIoqjKOpCkqUpamKapynqgqKpKmqiqqsq6sKyrStq4rqvK+sCwrEsayLKsyzrQtK1LWti2rct64LiuS5rouq7LuvC8r0va+L6vz+1/YBgWCYNhGFYZh2IYlimLYxjWOY9kGRZJk2UZVlmXZhmWaZtnGdZ5n2gaFomjaRpWmadqGpapq2sa1rmvbBsWybNtG1bZt24blum7bxvW+b9wHBcJw3EcVxnHchyXKctzHNc5z3QdF0nTdR1XWdd2HZdp23cd13nfeB4XieN5HleZ53oel6nrex7Xue98HxfJ83+Po+r/X/Ps/9934flAgv0LuSKcKEhY43z0g";
    private static final int[] WIDE_FACTS = {512, 512, 1, 3, 11, 258};
    private static final int[] WIDE_SELECTORS = {0,0,0,0,0,0,0,0,0,0,0};
    private static final int[] WIDE_SELECTOR_MTF = {0,0,0,0,0,0,0,0,0,0,0};
    private static final int[] WIDE_SEQ_TO_UNSEQ = {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,69,70,71,72,73,74,75,76,77,78,79,80,81,82,83,84,85,86,87,88,89,90,91,92,93,94,95,96,97,98,99,100,101,102,103,104,105,106,107,108,109,110,111,112,113,114,115,116,117,118,119,120,121,122,123,124,125,126,127,128,129,130,131,132,133,134,135,136,137,138,139,140,141,142,143,144,145,146,147,148,149,150,151,152,153,154,155,156,157,158,159,160,161,162,163,164,165,166,167,168,169,170,171,172,173,174,175,176,177,178,179,180,181,182,183,184,185,186,187,188,189,190,191,192,193,194,195,196,197,198,199,200,201,202,203,204,205,206,207,208,209,210,211,212,213,214,215,216,217,218,219,220,221,222,223,224,225,226,227,228,229,230,231,232,233,234,235,236,237,238,239,240,241,242,243,244,245,246,247,248,249,250,251,252,253,254,255};
    private static final int[][] WIDE_LENS = {{10,10,7,9,8,1,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,10,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,10,9,9,10,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,10,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,9,10,9,9,10,10,9,10,9,9,9,9},{9,9,8,8,8,8,8,8,8,8,8,8,8,8,8,9,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,9,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8},{9,9,8,8,8,8,8,8,8,8,8,8,8,8,8,9,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,9,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8,8}};
    private static final int[] WIDE_IN_USE16 = {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1};
    private static final String MULTI_B64 = "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";
    private static final int[] MULTI_FACTS = {6000, 6001, 4295, 6, 120, 43};
    private static final int[] MULTI_SELECTORS = {4,2,0,0,1,5,0,1,0,5,3,1,1,0,1,4,1,1,0,4,0,5,1,1,3,2,4,4,0,1,3,3,4,0,3,1,2,4,5,3,4,2,1,5,0,3,4,3,5,2,5,2,5,1,0,2,3,1,0,0,4,3,5,3,0,4,0,5,2,1,0,1,1,1,2,3,0,4,4,3,4,4,1,5,2,1,2,0,5,5,3,5,1,0,5,1,5,0,5,3,3,0,3,1,3,1,1,1,4,0,1,3,3,3,3,0,4,4,2,4};
    private static final int[] MULTI_SELECTOR_MTF = {4,3,2,0,3,5,2,2,1,2,5,3,0,3,1,5,1,0,2,2,1,4,3,0,4,5,5,0,5,4,4,0,3,3,2,3,4,4,5,4,2,3,4,4,5,5,5,1,3,5,1,1,1,5,5,3,4,3,3,0,5,3,5,1,3,3,1,3,5,5,3,1,0,0,2,5,3,5,0,2,1,0,4,5,5,2,1,5,3,0,5,1,4,3,2,2,1,2,1,3,0,2,1,3,1,1,0,0,5,3,2,3,0,0,0,2,3,0,5,1};
    private static final int[] MULTI_SEQ_TO_UNSEQ = {0,97,98,99,100,101,102,103,104,105,106,107,108,109,110,111,112,113,114,115,116,117,118,119,120,121,122,123,124,125,126,127,128,129,130,131,132,133,134,135,136};
    private static final int[][] MULTI_LENS = {{6,9,7,5,6,5,5,5,6,6,5,6,5,5,6,5,6,6,6,5,5,6,6,5,5,5,6,5,5,5,6,5,5,6,6,5,5,5,5,5,5,9,8},{6,8,6,5,5,7,5,6,5,6,6,5,5,6,5,5,5,6,6,5,5,6,5,6,5,6,6,5,5,5,5,5,6,5,5,5,5,5,6,5,6,9,9},{5,9,6,7,6,5,5,6,6,7,5,5,5,5,5,6,6,6,6,6,5,5,5,5,5,4,5,6,5,5,6,7,6,5,5,6,6,5,6,4,6,9,8},{5,9,5,6,6,5,5,5,5,5,6,6,5,6,5,5,5,5,5,5,6,5,6,5,5,7,5,5,6,6,6,5,5,6,6,6,6,6,5,5,5,8,9},{6,9,5,6,5,5,7,5,5,5,5,5,5,4,5,6,6,5,5,6,5,5,5,5,6,6,5,6,6,6,6,6,6,5,6,6,5,5,5,6,6,9,8},{5,10,4,5,5,5,5,5,6,5,5,5,7,5,5,6,5,6,5,6,5,6,6,6,8,5,5,6,6,6,6,6,6,5,5,6,5,5,5,6,5,10,9}};
    private static final int[] MULTI_IN_USE16 = {1,0,0,0,0,0,1,1,1,0,0,0,0,0,0,0};
    private static final String BIG_B64 = "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";
    // END FIXTURES

    private static Class<?> cls(String n) throws Exception { return Class.forName(n); }
    private static Field fld(String n) throws Exception {
        Field f = cls(STATE).getDeclaredField(n);
        f.setAccessible(true);
        return f;
    }
    private static Object get(Object s, String n) throws Exception { return fld(n).get(s); }
    private static int geti(Object s, String n) throws Exception { return ((Number) get(s, n)).intValue(); }
    private static void set(Object s, String n, Object v) throws Exception { fld(n).set(s, v); }
    private static Object invoke(String m, Class<?>[] types, Object... args) throws Exception {
        Method me = cls(DECODER).getDeclaredMethod(m, types);
        me.setAccessible(true);
        return me.invoke(null, args);
    }
    private static Object newState() throws Exception {
        java.lang.reflect.Constructor<?> c = cls(STATE).getDeclaredConstructor();
        c.setAccessible(true);
        return c.newInstance();
    }
    private static Class<?>[] stateOnly() throws Exception { return new Class<?>[] {cls(STATE)}; }
    private static int decode(byte[] dest, int destLen, byte[] src, int srcLen, int off) throws Exception {
        return (Integer) cls(DECODER).getMethod(DECODE, byte[].class, int.class, byte[].class, int.class, int.class)
            .invoke(null, dest, destLen, src, srcLen, off);
    }

    private static byte[] plain(String name) {
        switch (name) {
        case "TINY": return "hello world hello world".getBytes();
        case "RUNS": {
            byte[] b = new byte[572]; int p = 0;
            p = fill(b, p, 'a', 3); p = fill(b, p, 'b', 4); p = fill(b, p, 'c', 5); p = fill(b, p, 'd', 300);
            p = fill(b, p, 'e', 259); fill(b, p, 'f', 1); return b;
        }
        case "SPARSE": {
            byte[] b = new byte[31];
            for (int i = 0; i < 30; i++) b[i] = (byte) new int[] {5, 0x80, 0xf0}[i % 3];
            b[30] = 5; return b;
        }
        case "WIDE": {
            byte[] b = new byte[512];
            for (int i = 0; i < 256; i++) { b[i] = (byte) i; b[511 - i] = (byte) i; }
            return b;
        }
        case "MULTI": {
            byte[] a = new byte[40]; for (int i = 0; i < 40; i++) a[i] = (byte) (97 + i);
            return lcg(6000, a);
        }
        case "BIG": {
            byte[] a = new byte[16]; for (int i = 0; i < 16; i++) a[i] = (byte) (65 + i);
            byte[] unit = lcg(1000, a), b = new byte[150000];
            for (int i = 0; i < 150; i++) System.arraycopy(unit, 0, b, i * 1000, 1000);
            return b;
        }
        default: throw new IllegalArgumentException(name);
        }
    }
    private static int fill(byte[] b, int p, int v, int n) { for (int i = 0; i < n; i++) b[p++] = (byte) v; return p; }
    private static byte[] lcg(int n, byte[] alpha) {
        long x = 12345; byte[] out = new byte[n];
        for (int i = 0; i < n; i++) { x = (x * 1103515245L + 12345L) & 0x7fffffffL; out[i] = alpha[(int) ((x >> 16) % alpha.length)]; }
        return out;
    }
    private static Object fixtureField(String n) throws Exception { return BZip2Test.class.getDeclaredField(n).get(null); }
    private static byte[] b64(String name) throws Exception { return Base64.getDecoder().decode((String) fixtureField(name + "_B64")); }
    private static int[] facts(String name) throws Exception { return (int[]) fixtureField(name + "_FACTS"); }
    private static int[] ints(String name, String suffix) throws Exception { return (int[]) fixtureField(name + suffix); }
    private static final String[] SINGLE_BLOCK = {"TINY", "RUNS", "SPARSE", "WIDE", "MULTI"};

    /** Builds a source buffer: offset junk bytes, the stream, then trailing junk. */
    private static byte[] framed(byte[] stream, int offset, int trailing) {
        byte[] src = new byte[offset + stream.length + trailing];
        Arrays.fill(src, (byte) 0x5a);
        System.arraycopy(stream, 0, src, offset, stream.length);
        return src;
    }
    private static Object state(byte[] src, int offset, int srcLen, byte[] dest, int destLen) throws Exception {
        Object s = newState();
        set(s, F_INPUT, src); set(s, F_IN_POS, offset); set(s, F_IN_LEFT, srcLen);
        set(s, F_OUTPUT, dest); set(s, F_OUT_POS, 0); set(s, F_OUT_LEFT, destLen);
        return s;
    }
    private static Object decodeFresh(String name, int destExtra) throws Exception {
        byte[] stream = b64(name), want = plain(name);
        byte[] src = framed(stream, 3, 20), dest = new byte[want.length + destExtra];
        Object s = state(src, 3, stream.length + 20, dest, dest.length);
        invoke(DECODE_BLOCKS, stateOnly(), s);
        return s;
    }

    // --- whole-routine behaviour: decode(dest, destLength, src, srcLength, srcOffset) returns bytes written ---
    @Test public void decodeReturnsWrittenLengthAndData() throws Exception {
        for (String name : new String[] {"TINY", "RUNS", "SPARSE", "WIDE", "MULTI", "BIG"}) {
            byte[] stream = b64(name), want = plain(name);
            for (int extra : new int[] {0, 7}) {
                byte[] src = framed(stream, 3, 20), dest = new byte[want.length + extra];
                int n = decode(dest, dest.length, src, stream.length + 20, 3);
                assertEquals(name + " extra=" + extra, want.length, n);
                assertArrayEquals(name, want, Arrays.copyOf(dest, want.length));
                for (int i = want.length; i < dest.length; i++) assertEquals("untouched tail", 0, dest[i]);
            }
        }
    }
    @Test public void decodeWithSmallDestinationReturnsOnlyCapacity() throws Exception {
        byte[] stream = b64("MULTI"), want = plain("MULTI");
        byte[] src = framed(stream, 3, 0), dest = new byte[100];
        assertEquals(100, decode(dest, 100, src, stream.length, 3));
        assertArrayEquals(Arrays.copyOf(want, 100), dest);
    }
    @Test public void decodeUsesSourceOffset() throws Exception {
        byte[] stream = b64("TINY");
        for (int off : new int[] {0, 1, 6, 40}) {
            byte[] src = framed(stream, off, 0), dest = new byte[23];
            assertEquals(23, decode(dest, 23, src, stream.length, off));
            assertArrayEquals(plain("TINY"), dest);
        }
    }

    // --- input accounting ---
    @Test public void inputPositionRemainingAndTotals() throws Exception {
        for (String name : SINGLE_BLOCK) {
            byte[] stream = b64(name);
            for (int trailing : new int[] {0, 50}) {
                byte[] src = framed(stream, 3, trailing), dest = new byte[plain(name).length];
                int srcLen = stream.length + trailing;
                Object s = state(src, 3, srcLen, dest, dest.length);
                invoke(DECODE_BLOCKS, stateOnly(), s);
                int consumed = geti(s, F_IN_TOTAL_LO);
                assertSame(src, get(s, F_INPUT));
                assertEquals(3 + consumed, geti(s, F_IN_POS));
                assertEquals(srcLen - consumed, geti(s, F_IN_LEFT));
                assertEquals(0, geti(s, F_IN_TOTAL_HI));
                // End-of-stream magic (6 bytes) and combined CRC (4 bytes) are only read up to the first magic byte.
                assertEquals(name, stream.length - 9, consumed);
            }
        }
    }
    @Test public void inputTotalCarriesIntoHighWord() throws Exception {
        byte[] stream = b64("TINY"), want = plain("TINY");
        byte[] src = framed(stream, 0, 0), dest = new byte[want.length];
        Object s = state(src, 0, stream.length, dest, dest.length);
        set(s, F_IN_TOTAL_LO, -3);
        invoke(DECODE_BLOCKS, stateOnly(), s);
        int consumed = geti(s, F_IN_POS);
        assertEquals(1, geti(s, F_IN_TOTAL_HI));
        assertEquals(consumed - 3, geti(s, F_IN_TOTAL_LO));
    }

    // --- output accounting ---
    @Test public void outputPositionRemainingAndTotals() throws Exception {
        for (String name : SINGLE_BLOCK) {
            int len = plain(name).length;
            for (int extra : new int[] {0, 9}) {
                Object s = decodeFresh(name, extra);
                assertEquals(name, len, geti(s, F_OUT_POS));
                assertEquals(name, extra, geti(s, F_OUT_LEFT));
                assertEquals(name, len, geti(s, F_OUT_TOTAL_LO));
                assertEquals(0, geti(s, F_OUT_TOTAL_HI));
                assertArrayEquals(plain(name), Arrays.copyOf((byte[]) get(s, F_OUTPUT), len));
            }
        }
    }
    @Test public void outputTotalCarriesIntoHighWord() throws Exception {
        byte[] stream = b64("MULTI"), want = plain("MULTI");
        byte[] src = framed(stream, 0, 0), dest = new byte[want.length];
        Object s = state(src, 0, stream.length, dest, dest.length);
        set(s, F_OUT_TOTAL_LO, Integer.MAX_VALUE - 100); // the carry test is a signed comparison
        invoke(DECODE_BLOCKS, stateOnly(), s);
        assertEquals(1, geti(s, F_OUT_TOTAL_HI));
        assertEquals(Integer.MAX_VALUE - 100 + want.length, geti(s, F_OUT_TOTAL_LO));
    }

    // --- bit reader ---
    @Test public void readBitsTakesBitsMostSignificantFirstAndTracksBuffer() throws Exception {
        Object s = newState();
        set(s, F_INPUT, new byte[] {(byte) 0xB6, (byte) 0x5C, (byte) 0xFF});
        set(s, F_IN_POS, 0); set(s, F_IN_LEFT, 3); set(s, F_BIT_COUNT, 0); set(s, F_BIT_BUF, 0);
        Class<?>[] t = {int.class, cls(STATE)};
        // 0xB6 = 1011 0110, 0x5C = 0101 1100
        assertEquals(0b101, invoke(READ_BITS, t, 3, s));
        assertEquals(5, geti(s, F_BIT_COUNT));
        assertEquals(1, geti(s, F_IN_POS)); assertEquals(2, geti(s, F_IN_LEFT)); assertEquals(1, geti(s, F_IN_TOTAL_LO));
        assertEquals(0b10110, invoke(READ_BITS, t, 5, s));
        assertEquals(0, geti(s, F_BIT_COUNT));
        assertEquals(0b01011, invoke(READ_BITS, t, 5, s)); // takes 5 of the second byte
        assertEquals(3, geti(s, F_BIT_COUNT));
        assertEquals(2, geti(s, F_IN_POS));
        assertEquals(0b100, invoke(READ_BITS, t, 3, s)); // remaining buffered bits, no new byte
        assertEquals(2, geti(s, F_IN_POS));
        assertEquals(0, geti(s, F_BIT_COUNT));
        assertEquals(0xFF, invoke(READ_BITS, t, 8, s));
        assertEquals(3, geti(s, F_IN_POS));
    }
    @Test public void readBitsTotalCarry() throws Exception {
        Object s = newState();
        set(s, F_INPUT, new byte[] {1, 2}); set(s, F_IN_POS, 0); set(s, F_IN_LEFT, 2); set(s, F_BIT_COUNT, 0);
        set(s, F_IN_TOTAL_LO, -1);
        invoke(READ_BITS, new Class<?>[] {int.class, cls(STATE)}, 8, s);
        assertEquals(0, geti(s, F_IN_TOTAL_LO));
        assertEquals(1, geti(s, F_IN_TOTAL_HI));
    }
    @Test public void readByteAndReadBitWrapReadBits() throws Exception {
        Object s = newState();
        set(s, F_INPUT, new byte[] {(byte) 0xA5, (byte) 0x3C}); set(s, F_IN_POS, 0); set(s, F_IN_LEFT, 2); set(s, F_BIT_COUNT, 0);
        assertEquals((byte) 1, invoke(READ_BIT, stateOnly(), s));
        assertEquals((byte) 0, invoke(READ_BIT, stateOnly(), s));
        assertEquals((byte) 0x94, invoke(READ_BYTE, stateOnly(), s)); // 100101 then 00 of the next byte
        assertEquals((byte) 1, invoke(READ_BIT, stateOnly(), s));
        assertEquals(2, geti(s, F_IN_POS));
    }

    // --- symbol map ---
    @Test public void symbolMapListsUsedByteValuesInOrder() throws Exception {
        Object s = newState();
        boolean[] used = (boolean[]) get(s, F_IN_USE);
        for (int v : new int[] {200, 3, 77, 255, 0}) used[v] = true;
        invoke(BUILD_SYMBOL_MAP, stateOnly(), s);
        assertEquals(5, geti(s, F_N_IN_USE));
        assertArrayEquals(new byte[] {0, 3, 77, (byte) 200, (byte) 255}, Arrays.copyOf((byte[]) get(s, F_SEQ), 5));
        used[77] = false;
        invoke(BUILD_SYMBOL_MAP, stateOnly(), s);
        assertEquals(4, geti(s, F_N_IN_USE));
        assertArrayEquals(new byte[] {0, 3, (byte) 200, (byte) 255}, Arrays.copyOf((byte[]) get(s, F_SEQ), 4));
    }

    // --- decode tables (canonical Huffman) ---
    private static final Class<?>[] TABLE_TYPES = {int[].class, int[].class, int[].class, byte[].class, int.class, int.class, int.class};
    @Test public void decodeTablesForKnownCodeLengths() throws Exception {
        // lengths 1,2,3,3 -> codes 0,10,110,111
        int[] limit = new int[23], base = new int[23], perm = new int[258];
        invoke(BUILD_TABLES, TABLE_TYPES, limit, base, perm, new byte[] {1, 2, 3, 3}, 1, 3, 4);
        assertEquals(0, limit[1]); assertEquals(2, limit[2]); assertEquals(7, limit[3]);
        assertEquals(0, base[1]); assertEquals(1, base[2]); assertEquals(4, base[3]);
        assertArrayEquals(new int[] {0, 1, 2, 3}, Arrays.copyOf(perm, 4));
        // symbols out of order, lengths 2,1,2 -> symbols sorted by length then value: 1,0,2
        limit = new int[23]; base = new int[23]; perm = new int[258];
        invoke(BUILD_TABLES, TABLE_TYPES, limit, base, perm, new byte[] {2, 1, 2}, 1, 2, 3);
        assertArrayEquals(new int[] {1, 0, 2}, Arrays.copyOf(perm, 3));
        assertEquals(0, limit[1]); assertEquals(3, limit[2]); // codes: 0 (sym1), 10 (sym0), 11 (sym2)
        assertEquals(0, base[1]); assertEquals(1, base[2]);
        // the last parameter bounds how many length entries are used
        limit = new int[23]; base = new int[23]; perm = new int[258];
        invoke(BUILD_TABLES, TABLE_TYPES, limit, base, perm, new byte[] {2, 1, 2, 1, 1}, 1, 2, 3);
        assertArrayEquals(new int[] {1, 0, 2}, Arrays.copyOf(perm, 3));
        assertEquals(3, limit[2]);
    }

    // --- block header facts against the independent parse ---
    @Test public void blockHeaderFields() throws Exception {
        for (String name : SINGLE_BLOCK) {
            Object s = decodeFresh(name, 0);
            int[] f = facts(name);
            assertEquals(name, f[1], geti(s, F_NBLOCK));
            assertEquals(name, f[1] + 1, geti(s, F_USED));
            assertEquals(name, f[2], geti(s, F_ORIG_PTR));
            assertEquals(name, 1, geti(s, F_BLOCKS));
            assertEquals(name, 1, geti(s, F_BLOCK_SIZE));
            assertFalse((Boolean) get(s, F_RANDOMISED));
            assertEquals(0, geti(s, F_PENDING_LEN));
            assertEquals(f[5] - 2, geti(s, F_N_IN_USE));
            boolean[] i16 = (boolean[]) get(s, F_IN_USE16);
            int[] want16 = ints(name, "_IN_USE16");
            for (int i = 0; i < 16; i++) assertEquals(name + " group " + i, want16[i] == 1, i16[i]);
            int[] seq = ints(name, "_SEQ_TO_UNSEQ");
            byte[] gotSeq = (byte[]) get(s, F_SEQ);
            boolean[] inUse = (boolean[]) get(s, F_IN_USE);
            for (int i = 0; i < seq.length; i++) { assertEquals(seq[i], gotSeq[i] & 0xff); assertTrue(inUse[seq[i]]); }
            int count = 0; for (boolean b : inUse) if (b) count++;
            assertEquals(seq.length, count);
        }
    }
    @Test public void selectorsAndCodeLengthTables() throws Exception {
        for (String name : SINGLE_BLOCK) {
            Object s = decodeFresh(name, 0);
            int[] f = facts(name);
            int groups = f[3], selectors = f[4], alpha = f[5];
            byte[] sel = (byte[]) get(s, F_SELECTOR), mtf = (byte[]) get(s, F_SELECTOR_MTF);
            int[] wantSel = ints(name, "_SELECTORS"), wantMtf = ints(name, "_SELECTOR_MTF");
            assertEquals(selectors, wantSel.length);
            for (int i = 0; i < selectors; i++) { assertEquals(name + " sel " + i, wantSel[i], sel[i]); assertEquals(wantMtf[i], mtf[i]); }
            assertEquals("nothing beyond the selector count", 0, sel[selectors]);
            assertEquals(0, mtf[selectors]);
            byte[][] lens = (byte[][]) get(s, F_LEN);
            int[][] wantLens = (int[][]) fixtureField(name + "_LENS");
            int[] minLen = (int[]) get(s, F_MIN_LEN);
            for (int t = 0; t < groups; t++) {
                int min = 99;
                for (int i = 0; i < alpha; i++) { assertEquals(name + " len " + t + "," + i, wantLens[t][i], lens[t][i]); min = Math.min(min, wantLens[t][i]); }
                if (alpha < lens[t].length) assertEquals("alphabet size bounds table fill", 0, lens[t][alpha]);
                assertEquals(min, minLen[t]);
            }
            for (int t = groups; t < 6; t++) assertEquals("unused table", 0, lens[t][0]);
        }
    }
    @Test public void blockCountForTwoBlockStream() throws Exception {
        byte[] stream = b64("BIG"), want = plain("BIG");
        byte[] src = framed(stream, 0, 0), dest = new byte[want.length];
        Object s = state(src, 0, stream.length, dest, dest.length);
        invoke(DECODE_BLOCKS, stateOnly(), s);
        assertEquals(2, geti(s, F_BLOCKS));
        assertEquals(want.length, geti(s, F_OUT_TOTAL_LO));
        assertArrayEquals(want, dest);
    }
    @Test public void randomisedFlagIsReadFromBitAfterCrc() throws Exception {
        byte[] stream = b64("TINY").clone();
        stream[10] |= (byte) 0x80; // magic (48) + crc (32) = 80 bits, then the flag
        byte[] dest = new byte[23];
        Object s = state(framed(stream, 0, 0), 0, stream.length, dest, 23);
        invoke(DECODE_BLOCKS, stateOnly(), s);
        assertTrue((Boolean) get(s, F_RANDOMISED));
        assertFalse((Boolean) get(decodeFresh("TINY", 0), F_RANDOMISED));
    }
    @Test public void originPointerSelectsRotation() throws Exception {
        byte[] stream = b64("TINY").clone();
        int[] f = facts("TINY");
        assertEquals(7, f[2]);
        long bit = 81 + 23; // origin pointer occupies bits 81..104
        stream[(int) (bit / 8)] ^= (byte) (0x80 >> (bit % 8));
        byte[] dest = new byte[23];
        Object s = state(framed(stream, 0, 0), 0, stream.length, dest, 23);
        invoke(DECODE_BLOCKS, stateOnly(), s);
        assertEquals(f[2] ^ 1, geti(s, F_ORIG_PTR));
        assertFalse(Arrays.equals(plain("TINY"), dest));
    }
    @Test public void blockSizeAllocatesTranslationTable() throws Exception {
        set(null, F_TT, null);
        Object s = decodeFresh("TINY", 0);
        int[] tt = (int[]) get(null, F_TT);
        assertEquals(1, geti(s, F_BLOCK_SIZE));
        assertEquals(100000 * geti(s, F_BLOCK_SIZE), tt.length);
    }

    // --- pending-run state when the destination fills mid-run ---
    @Test public void pendingRunStateWhenDestinationFills() throws Exception {
        byte[] stream = b64("RUNS");
        int[] pending = new int[2]; byte[] ch = new byte[2]; int k = 0;
        for (int cap : new int[] {20, 30}) {
            byte[] dest = new byte[cap];
            Object s = state(framed(stream, 0, 0), 0, stream.length, dest, cap);
            invoke(DECODE_BLOCKS, stateOnly(), s);
            assertEquals(cap, geti(s, F_OUT_POS));
            assertEquals(0, geti(s, F_OUT_LEFT));
            pending[k] = geti(s, F_PENDING_LEN); ch[k] = (Byte) get(s, F_PENDING_CH); k++;
        }
        // the 300-long run of 'd' starts at output index 12, so both cut points fall inside it.
        assertEquals('d', ch[0]); assertEquals('d', ch[1]);
        assertTrue(pending[0] > 0);
        assertEquals("ten more output bytes consume ten pending repeats", pending[0] - 10, pending[1]);
        // counterexample: a cut inside the earlier 'c' run leaves 'c' pending
        byte[] dest = new byte[10];
        Object s = state(framed(stream, 0, 0), 0, stream.length, dest, 10);
        invoke(DECODE_BLOCKS, stateOnly(), s);
        assertEquals('c', (byte) (Byte) get(s, F_PENDING_CH));
    }

    // --- inverse BWT / run-length stage with a hand-built translation table ---
    private static Object emitState(int[] tt, int tPos, int k0, int used, int nblock, byte[] out) throws Exception {
        Object s = newState();
        set(null, F_TT, tt);
        set(s, F_TPOS, tPos); set(s, F_K0, k0); set(s, F_USED, used); set(s, F_NBLOCK, nblock);
        set(s, F_OUTPUT, out); set(s, F_OUT_POS, 0); set(s, F_OUT_LEFT, out.length);
        set(s, F_PENDING_LEN, 0);
        return s;
    }
    @Test public void emitBlockFollowsTranslationChain() throws Exception {
        // entries hold (next index << 8) | byte ; k0 is the byte already read from tt[0]
        int[] tt = new int[100000];
        tt[0] = (1 << 8) | 'a'; tt[1] = (2 << 8) | 'b'; tt[2] = (0 << 8) | 'c';
        byte[] out = new byte[3];
        Object s = emitState(tt, 1, 'a', 1, 3, out);
        invoke(EMIT_BLOCK, stateOnly(), s);
        assertArrayEquals(new byte[] {'a', 'b', 'c'}, out);
        assertEquals(4, geti(s, F_USED));
        assertEquals(3, geti(s, F_OUT_POS));
        assertEquals(0, geti(s, F_PENDING_LEN));
        tt[0] = (2 << 8) | 'x'; tt[2] = (1 << 8) | 'y'; tt[1] = (0 << 8) | 'z';
        out = new byte[3];
        s = emitState(tt, 2, 'x', 1, 3, out);
        invoke(EMIT_BLOCK, stateOnly(), s);
        assertArrayEquals(new byte[] {'x', 'y', 'z'}, out);
    }
    @Test public void emitBlockExpandsFourEqualBytesPlusCount() throws Exception {
        int[] tt = new int[100000];
        tt[0] = (1 << 8) | 'a'; tt[1] = (2 << 8) | 'a'; tt[2] = (3 << 8) | 'a'; tt[3] = (4 << 8) | 'a'; tt[4] = (0 << 8) | 3;
        byte[] out = new byte[7];
        Object s = emitState(tt, 1, 'a', 1, 5, out);
        invoke(EMIT_BLOCK, stateOnly(), s);
        assertArrayEquals(new byte[] {'a', 'a', 'a', 'a', 'a', 'a', 'a'}, out);
        assertEquals(6, geti(s, F_USED));
        // only three equal bytes: the following byte is data, not a count
        tt[3] = (4 << 8) | 'b';
        out = new byte[5];
        s = emitState(tt, 1, 'a', 1, 5, out);
        invoke(EMIT_BLOCK, stateOnly(), s);
        assertArrayEquals(new byte[] {'a', 'a', 'a', 'b', 3}, out);
    }
    @Test public void emitBlockCarriesOutputTotal() throws Exception {
        int[] tt = new int[100000];
        tt[0] = (1 << 8) | 'a'; tt[1] = (2 << 8) | 'b'; tt[2] = (0 << 8) | 'c';
        byte[] out = new byte[3];
        Object s = emitState(tt, 1, 'a', 1, 3, out);
        set(s, F_OUT_TOTAL_LO, Integer.MAX_VALUE - 1);
        invoke(EMIT_BLOCK, stateOnly(), s);
        assertEquals(Integer.MIN_VALUE + 1, geti(s, F_OUT_TOTAL_LO));
        assertEquals(1, geti(s, F_OUT_TOTAL_HI));
    }

    // --- tables built while decoding: counts, cumulative counts, translation table, Huffman tables ---
    private static byte[] rle1(byte[] d) {
        java.io.ByteArrayOutputStream o = new java.io.ByteArrayOutputStream();
        int i = 0;
        while (i < d.length) {
            int j = i;
            while (j < d.length && d[j] == d[i] && j - i < 255) j++;
            int run = j - i;
            if (run >= 4) { for (int q = 0; q < 4; q++) o.write(d[i]); o.write(run - 4); }
            else for (int q = 0; q < run; q++) o.write(d[i]);
            i = j;
        }
        return o.toByteArray();
    }
    @Test public void byteCountsCumulativeCountsAndTranslationTable() throws Exception {
        for (String name : SINGLE_BLOCK) {
            Object s = decodeFresh(name, 0);
            byte[] block = rle1(plain(name));
            int[] count = new int[256];
            for (byte b : block) count[b & 0xff]++;
            int[] unz = (int[]) get(s, F_UNZFTAB), cf = (int[]) get(s, F_CFTAB), tt = (int[]) get(null, F_TT);
            int running = 0;
            for (int v = 0; v < 256; v++) {
                assertEquals(name + " count " + v, count[v], unz[v]);
                running += count[v];
                assertEquals(name + " cumulative (inclusive after the pass) " + v, running, cf[v]);
            }
            assertEquals(block.length, cf[256]);
            int[] lowCount = new int[256];
            boolean[] seen = new boolean[block.length];
            for (int i = 0; i < block.length; i++) {
                lowCount[tt[i] & 0xff]++;
                int next = tt[i] >>> 8;
                assertTrue(name + " next index in range", next < block.length);
                assertFalse(name + " next indices form a permutation " + i, seen[next]);
                seen[next] = true;
            }
            assertArrayEquals(name + " low bytes hold the block's bytes", count, lowCount);
            assertEquals(name + " last column entry of the origin row is the block's last byte",
                block[block.length - 1] & 0xff, tt[geti(s, F_ORIG_PTR)] & 0xff);
        }
    }
    @Test public void storedHuffmanTablesMatchTableBuilderForEachCodeLengthRow() throws Exception {
        Object s = decodeFresh("MULTI", 0);
        int[] f = facts("MULTI");
        byte[][] lens = (byte[][]) get(s, F_LEN);
        int[][] limit = (int[][]) get(s, F_LIMIT), base = (int[][]) get(s, F_BASE), perm = (int[][]) get(s, F_PERM);
        int[] minLen = (int[]) get(s, F_MIN_LEN);
        for (int t = 0; t < f[3]; t++) {
            int min = 99, max = 0;
            for (int i = 0; i < f[5]; i++) { min = Math.min(min, lens[t][i]); max = Math.max(max, lens[t][i]); }
            int[] l = new int[258], b = new int[258], p = new int[258];
            invoke(BUILD_TABLES, TABLE_TYPES, l, b, p, lens[t], min, max, f[5]);
            assertEquals(min, minLen[t]);
            assertArrayEquals("limit " + t, l, limit[t]);
            for (int i = min; i <= max; i++) assertEquals("base " + t + "," + i, b[i], base[t][i]);
            assertArrayEquals("perm " + t, p, perm[t]);
        }
    }
}
