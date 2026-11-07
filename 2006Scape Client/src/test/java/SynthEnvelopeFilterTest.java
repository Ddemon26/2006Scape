import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import org.junit.Test;

/** Original-name semantic tests for SynthEnvelope (breakpoint envelope) and SynthFilter (two-direction pole filter). */
public class SynthEnvelopeFilterTest {

    private static void i32(ByteArrayOutputStream o, int v) { o.write(v >>> 24); o.write(v >>> 16); o.write(v >>> 8); o.write(v); }
    private static void u16(ByteArrayOutputStream o, int v) { o.write(v >>> 8); o.write(v); }
    private static Object get(Object o, String n) throws Exception { Field f = o.getClass().getDeclaredField(n); f.setAccessible(true); return f.get(o); }
    private static int geti(Object o, String n) throws Exception { return (Integer) get(o, n); }
    private static int[] arr(Object o, String n) throws Exception { return (int[]) get(o, n); }

    private static byte[] env(int type, int a, int b, int... pts) {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        o.write(type); i32(o, a); i32(o, b); o.write(pts.length / 2);
        for (int p : pts) u16(o, p);
        return o.toByteArray();
    }

    private static SynthEnvelope envelope(int type, int a, int b, int... pts) {
        SynthEnvelope c = new SynthEnvelope(); c.decode(new Stream(env(type, a, b, pts))); return c;
    }

    // ---- SynthEnvelope ---------------------------------------------------------
    @Test public void envelopeHeaderIsByteThenTwoBigEndianInts() {
        SynthEnvelope c = envelope(3, 70000, 123456, 0, 5, 65535, 6);
        assertEquals(3, c.waveform); assertEquals(70000, c.rangeStart); assertEquals(123456, c.rangeEnd);
        SynthEnvelope d = envelope(1, 2, 1, 0, 5, 65535, 6);   // end below start is stored as read
        assertEquals(1, d.waveform); assertEquals(2, d.rangeStart); assertEquals(1, d.rangeEnd);
    }

    @Test public void envelopePointsAreCountThenPositionValuePairs() throws Exception {
        SynthEnvelope c = envelope(0, 0, 0, 0, 111, 20000, 222, 65535, 333);
        assertEquals(3, geti(c, "pointCount"));
        assertArrayEquals(new int[] {0, 20000, 65535}, arr(c, "pointPositions"));
        assertArrayEquals(new int[] {111, 222, 333}, arr(c, "pointValues"));
        // decodePoints alone (used by the filter) replaces the points but keeps the header
        c.decodePoints(new Stream(new byte[] {1, 0, 7, 0, 9}));
        assertEquals(1, geti(c, "pointCount")); assertEquals(7, arr(c, "pointPositions")[0]); assertEquals(9, arr(c, "pointValues")[0]);
        assertEquals(0, c.rangeStart);
    }

    @Test public void method328InterpolatesLinearlyOverTheSampleCount() {
        SynthEnvelope c = envelope(0, 0, 0, 0, 0, 65535, 32000);
        c.resetValues();
        int n = 1000, prev = -1;
        for (int k = 0; k < n; k++) {
            int v = c.nextValue(n);
            assertEquals("sample " + k, 32000.0 * k / (n * 65535.0 / 65536.0), v, 2.0 + 0.002 * v);
            assertTrue(v >= prev); prev = v;
        }
        // a different sample count rescales the same shape: halfway through 400 samples is half the value
        SynthEnvelope d = envelope(0, 0, 0, 0, 0, 65535, 32000); d.resetValues();
        int mid = 0; for (int k = 0; k <= 200; k++) mid = d.nextValue(400);
        assertEquals(16000, mid, 100);
    }

    @Test public void method328FollowsPiecewiseSegmentsAndHoldsLastValue() {
        // position is a fraction of 65536 of the whole duration; values rise to 1000 at 50% then fall to 0.
        SynthEnvelope c = envelope(0, 0, 0, 0, 100, 32768, 1000, 65535, 0);
        c.resetValues();
        int n = 1000; int[] out = new int[n];
        for (int k = 0; k < n; k++) out[k] = c.nextValue(n);
        assertEquals(100, out[0]);
        assertEquals(1000, out[500], 5);
        assertEquals(550, out[250], 5);
        assertEquals(500, out[750], 5);
        for (int k = 0; k < 3000; k++) assertEquals(0, c.nextValue(n), 2); // beyond the end it holds the final value
    }

    @Test public void resetValuesRestartsTheSweep() {
        SynthEnvelope c = envelope(0, 0, 0, 0, 0, 65535, 32000);
        c.resetValues();
        for (int k = 0; k < 600; k++) c.nextValue(1000);
        int late = c.nextValue(1000);
        c.resetValues();
        assertEquals(0, c.nextValue(1000));
        assertTrue(late > 15000);
    }

    @Test public void method328CountsOneStepPerCall() throws Exception {
        SynthEnvelope c = envelope(0, 0, 0, 0, 0, 65535, 32000);
        c.resetValues();
        assertEquals(0, geti(c, "sampleIndex"));
        for (int k = 1; k <= 7; k++) { c.nextValue(1000); assertEquals(k, geti(c, "sampleIndex")); }
        // the current value is kept <<15 and stepPerSample is the per-sample step in the same scale
        int ret = c.nextValue(1000);
        assertEquals(ret, geti(c, "currentValue") - geti(c, "stepPerSample") >> 15);
        assertEquals(1, geti(c, "targetPoint"));          // index of the point being approached
        assertEquals((int) (65535 / 65536D * 1000), geti(c, "segmentEndSample")); // sample at which that point is reached
    }

    // ---- SynthFilter ---------------------------------------------------------
    private static byte[] filterBytes(int counts, int g0, int g1, int mask, int[] startsFreqMag, int[] endsFreqMag, int[] envPts) {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        o.write(counts); u16(o, g0); u16(o, g1); o.write(mask);
        for (int p : startsFreqMag) u16(o, p);
        for (int p : endsFreqMag) u16(o, p);
        if (envPts != null) { o.write(envPts.length / 2); for (int p : envPts) u16(o, p); }
        return o.toByteArray();
    }

    private static SynthFilter filter(byte[] b, SynthEnvelope env) { SynthFilter f = new SynthFilter(); f.decode(new Stream(b), env); return f; }

    @Test public void filterHeaderNibblesAreTheTwoPoleCounts() throws Exception {
        SynthFilter f = filter(filterBytes(0x21, 0, 0, 0, new int[] {1, 2, 3, 4, 5, 6}, new int[0], null), new SynthEnvelope());
        assertArrayEquals(new int[] {2, 1}, f.pairCounts);
        SynthFilter z = filter(new byte[] {0}, new SynthEnvelope());
        assertArrayEquals(new int[] {0, 0}, z.pairCounts);
        assertArrayEquals(new int[] {0, 0}, arr(z, "gainStartEnd"));
        // first value of each pair goes to ...666 (frequency), second to ...667 (magnitude); direction 0 pairs first
        int[][][] fr = (int[][][]) get(f, "pairFrequencies"), mg = (int[][][]) get(f, "pairMagnitudes");
        assertEquals(1, fr[0][0][0]); assertEquals(2, mg[0][0][0]);
        assertEquals(3, fr[0][0][1]); assertEquals(4, mg[0][0][1]);
        assertEquals(5, fr[1][0][0]); assertEquals(6, mg[1][0][0]);
        // end values default to the start values when no mask bit is set
        assertEquals(1, fr[0][1][0]); assertEquals(4, mg[0][1][1]); assertEquals(5, fr[1][1][0]);
    }

    @Test public void maskBitSelectsWhichPolesCarryEndValuesAndTriggersEnvelope() throws Exception {
        // direction 0 has 2 poles, direction 1 has 1: bits 0,1 -> direction 0 poles, bit 4 -> direction 1 pole 0
        byte[] b = filterBytes(0x21, 0, 0, 0x12, new int[] {1, 2, 3, 4, 5, 6}, new int[] {30, 40, 50, 60}, new int[] {777, 5});
        Stream s = new Stream(b);
        SynthFilter f = new SynthFilter(); SynthEnvelope env = new SynthEnvelope();
        f.decode(s, env);
        int[][][] fr = (int[][][]) get(f, "pairFrequencies"), mg = (int[][][]) get(f, "pairMagnitudes");
        assertEquals(1, fr[0][1][0]); assertEquals(2, mg[0][1][0]);    // bit 0 clear
        assertEquals(30, fr[0][1][1]); assertEquals(40, mg[0][1][1]);   // bit 1 set
        assertEquals(50, fr[1][1][0]); assertEquals(60, mg[1][1][0]);   // bit 4 set
        assertEquals(1, geti(env, "pointCount"));                          // envelope points were read after the poles
        assertEquals(777, arr(env, "pointPositions")[0]);
        assertEquals(b.length, s.currentOffset);
    }

    @Test public void noEnvelopeIsReadWhenMaskIsZeroAndGainsEqual() {
        byte[] b = filterBytes(0x10, 500, 500, 0, new int[] {1, 2}, new int[0], null);
        Stream s = new Stream(b); new SynthFilter().decode(s, new SynthEnvelope());
        assertEquals(b.length, s.currentOffset);
        byte[] c = filterBytes(0x10, 500, 501, 0, new int[] {1, 2}, new int[0], new int[] {7, 8});
        Stream t = new Stream(c); new SynthFilter().decode(t, new SynthEnvelope());   // differing gains force an envelope
        assertEquals(c.length, t.currentOffset);
    }

    private static double angle(double storedFreq) { return 32.7032 * Math.pow(2, storedFreq / 8192.0) * Math.PI / 11025.0; }
    private static double radius(double storedMag) { return 1 - Math.pow(10, -(storedMag / 655.36) / 20); }

    @Test public void method544OnePoleGivesRadiusAndAngleCoefficients() {
        int fr = 20000, mg = 30000;
        SynthFilter f = filter(filterBytes(0x10, 0, 0, 0, new int[] {fr, mg}, new int[0], null), new SynthEnvelope());
        assertEquals(2, f.computeCoefficients(0, 0.0F));
        double r = radius(mg), w = angle(fr);
        assertEquals((int) (-2 * r * Math.cos(w) * 65536), SynthFilter.coefficients[0][0], 4);
        assertEquals((int) (r * r * 65536), SynthFilter.coefficients[0][1], 4);
        assertEquals(65536, SynthFilter.gainFactorFixed);          // zero gain value -> unity
        assertEquals(0, f.computeCoefficients(1, 0.0F));          // direction 1 has no poles
        // counterexample for the angle: a pole at a quarter of the 22050 Hz rate has cosine ~0
        double storedQuarter = 8192.0 * Math.log(5512.5 / 32.7032) / Math.log(2);
        SynthFilter q = filter(filterBytes(0x10, 0, 0, 0, new int[] {(int) Math.round(storedQuarter), 30000}, new int[0], null), new SynthEnvelope());
        q.computeCoefficients(0, 0.0F);
        assertEquals(0, SynthFilter.coefficients[0][0], 80);
        // zero magnitude -> radius 0 -> zero coefficients; more magnitude -> larger radius
        SynthFilter m0 = filter(filterBytes(0x10, 0, 0, 0, new int[] {fr, 0}, new int[0], null), new SynthEnvelope());
        m0.computeCoefficients(0, 0.0F);
        assertEquals(0, SynthFilter.coefficients[0][1]);
        f.computeCoefficients(0, 0.0F);
        int small = SynthFilter.coefficients[0][1];
        SynthFilter big = filter(filterBytes(0x10, 0, 0, 0, new int[] {fr, 60000}, new int[0], null), new SynthEnvelope());
        big.computeCoefficients(0, 0.0F);
        assertTrue(SynthFilter.coefficients[0][1] > small);
    }

    @Test public void method544InterpolatesBetweenStartAndEndAndGainOnlyScalesDirectionZero() {
        // pole 0 of direction 0: start (freq 8192, mag 20000) -> end (freq 16384, mag 40000); gain 0 -> 13107 (~20.0)
        byte[] b = filterBytes(0x10, 0, 13107, 1, new int[] {8192, 20000}, new int[] {16384, 40000}, new int[] {0, 0});
        SynthFilter f = filter(b, new SynthEnvelope());
        f.computeCoefficients(0, 0.5F);
        double r = radius(30000), w = angle(12288);
        double gain = Math.pow(0.1, 13107 * 0.5 * 0.003051758 / 20);
        assertEquals((int) (gain * 65536), SynthFilter.gainFactorFixed, 8);
        assertEquals((int) (-2 * r * Math.cos(w) * gain * 65536), SynthFilter.coefficients[0][0], 8);
        assertEquals((int) (r * r * gain * 65536), SynthFilter.coefficients[0][1], 8);
        assertTrue(SynthFilter.gainFactorFixed < 60000);
    }

    @Test public void method544TwoPolesMultiplyTheQuadratics() {
        int f1 = 15000, m1 = 25000, f2 = 30000, m2 = 35000;
        SynthFilter f = filter(filterBytes(0x20, 0, 0, 0, new int[] {f1, m1, f2, m2}, new int[0], null), new SynthEnvelope());
        assertEquals(4, f.computeCoefficients(0, 0.0F));
        double r1 = radius(m1), r2 = radius(m2);
        double a1 = -2 * r1 * Math.cos(angle(f1)), b1 = r1 * r1, a2 = -2 * r2 * Math.cos(angle(f2)), b2 = r2 * r2;
        int[] c = SynthFilter.coefficients[0];
        assertEquals((int) ((a1 + a2) * 65536), c[0], 6);
        assertEquals((int) ((b1 + b2 + a1 * a2) * 65536), c[1], 6);
        assertEquals((int) ((a1 * b2 + a2 * b1) * 65536), c[2], 6);
        assertEquals((int) ((b1 * b2) * 65536), c[3], 6);
    }
}
