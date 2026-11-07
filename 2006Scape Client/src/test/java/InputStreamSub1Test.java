import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.junit.Test;

/** Original-name semantic tests for MuLawInputStream (24-bit sample ints -> 8-bit mu-law bytes). */
public class InputStreamSub1Test {

    private static MuLawInputStream make(int fill) throws Exception {
        MuLawInputStream s = new MuLawInputStream();
        Field f = MuLawInputStream.class.getDeclaredField("sampleBuffer"); f.setAccessible(true);
        java.util.Arrays.fill((int[]) f.get(s), fill);
        return s;
    }

    private static void set(MuLawInputStream s, int idx, int v) throws Exception {
        Field f = MuLawInputStream.class.getDeclaredField("sampleBuffer"); f.setAccessible(true);
        ((int[]) f.get(s))[idx] = v;
    }

    private static int enc(int sample24) throws Exception {
        MuLawInputStream s = make(sample24);
        byte[] out = new byte[1];
        assertEquals(1, s.read(out, 0, 1));
        return out[0] & 0xff;
    }

    /** Independent standard G.711 mu-law decoder. */
    private static int decode(int b) {
        b = ~b & 0xff;
        int sign = b & 0x80, exp = b >> 4 & 7, mant = b & 15;
        int v = (((mant << 3) + 0x84) << exp) - 0x84;
        return sign != 0 ? -v : v;
    }

    @Test public void standardMuLawCodewords() throws Exception {
        assertEquals(0xFF, enc(0));
        assertEquals(0x7F, enc(-1 << 8));            // smallest negative 16-bit step
        assertEquals(0x80, enc(32124 << 8) & 0xff);  // loudest positive code
        assertEquals(0x00, enc(-32124 << 8));        // loudest negative code
        assertEquals(0x80, enc(32767 << 8));         // clipped at the top
        assertEquals(0x00, enc(-32768 << 8));
    }

    @Test public void roundTripsThroughStandardDecoderWithinQuantisationStep() throws Exception {
        for (int s16 = -32768; s16 <= 32767; s16 += 97) {
            int d = decode(enc(s16 << 8));
            int step = Math.max(8, Math.abs(Math.min(Math.max(s16, -32124), 32124)) >> 4);
            assertEquals("sample " + s16, Math.min(Math.max(s16, -32124), 32124), d, step);
        }
        // a-law style or linear 8-bit truncation would fail: loud and quiet samples both keep ~6 bit precision
        assertEquals(1000, decode(enc(1000 << 8)), 40);
        assertEquals(10, decode(enc(10 << 8)), 8);
    }

    @Test public void lowEightBitsOfTheSampleAreIgnoredAndSignIsPreserved() throws Exception {
        assertEquals(enc(5000 << 8), enc((5000 << 8) | 0xFF));
        assertTrue(decode(enc(5000 << 8)) > 0);
        assertTrue(decode(enc(-5000 << 8)) < 0);
        assertEquals(decode(enc(5000 << 8)), -decode(enc(-5000 << 8)), 40);
    }

    @Test public void samplesOutside24BitRangeAreClampedNotWrapped() throws Exception {
        assertEquals(enc(0x7FFFFF), enc(0x1000000));      // would wrap to a tiny positive/zero value without the clamp
        assertEquals(enc(0x7FFFFF), enc(0x7FFFFFFF));
        assertEquals(enc(-0x800000), enc(-0x1000001));
        assertEquals(enc(-0x800000), enc(0x80000000));
    }

    @Test public void readHandlesRequestsOverTheInternalBlockSizeAndOffsets() throws Exception {
        MuLawInputStream s = make(1000 << 8);
        byte[] out = new byte[310];
        assertEquals(300, s.read(out, 5, 300));
        byte expect = (byte) enc(1000 << 8);
        for (int i = 0; i < 5; i++) assertEquals(0, out[i]);
        for (int i = 5; i < 305; i++) assertEquals("byte " + i, expect, out[i]);
        for (int i = 305; i < 310; i++) assertEquals(0, out[i]);
        // each sample slot is encoded independently
        set(s, 0, 0); set(s, 1, 32124 << 8);
        byte[] two = new byte[2];
        assertEquals(2, s.read(two, 0, 2));
        assertEquals((byte) 0xFF, two[0]); assertEquals((byte) 0x80, two[1]);
    }

    @Test public void failureSetsTheFlagAndEveryLaterReadReturnsMinusOne() throws Exception {
        MuLawInputStream s = make(1000 << 8);
        assertFalse(s.failed);
        assertEquals(-1, s.read(new byte[2], 1, 2));     // does not fit: exception inside read
        assertTrue(s.failed);
        assertEquals(-1, s.read(new byte[8], 0, 4));     // valid request still refused afterwards
        MuLawInputStream t = make(0);
        assertEquals(4, t.read(new byte[8], 0, 4));      // a fresh stream is unaffected
    }

    @Test public void singleByteReadReturnsTheSignedCodeByte() throws Exception {
        assertEquals(-1, make(0).read());                 // 0xFF as a signed byte
        assertEquals(-128, make(32124 << 8).read());      // 0x80
        assertEquals(0, make(-32124 << 8).read());
    }

    @Test public void tableMapsEverySixteenBitSampleToItsCode() throws Exception {
        Field f = MuLawInputStream.class.getDeclaredField("muLawTable"); f.setAccessible(true);
        byte[] t = (byte[]) f.get(new MuLawInputStream());
        assertEquals(65536, t.length);
        assertEquals((byte) 0xFF, t[32768]);              // sample 0
        assertEquals((byte) 0x00, t[0]);                  // sample -32768
        assertEquals((byte) 0x80, t[65535]);              // sample 32767
        Method m = MuLawInputStream.class.getDeclaredMethod("encodeSample", int.class); m.setAccessible(true);
        MuLawInputStream s = new MuLawInputStream();
        for (int v = -32768; v < 32768; v += 251) assertEquals(t[v + 32768], ((Byte) m.invoke(s, v)).byteValue());
    }
}
