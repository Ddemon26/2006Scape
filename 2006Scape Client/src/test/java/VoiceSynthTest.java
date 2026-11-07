import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.junit.BeforeClass;
import org.junit.Test;

/** Semantic tests for the synthesiser voice class (original identifiers only). */
public class VoiceSynthTest {

    @BeforeClass public static void tables() { SynthVoice.initWaveTables(); }

    // ---- byte builders -------------------------------------------------
    private static void i32(ByteArrayOutputStream o, int v) { o.write(v >>> 24); o.write(v >>> 16); o.write(v >>> 8); o.write(v); }
    private static void u16(ByteArrayOutputStream o, int v) { o.write(v >>> 8); o.write(v); }
    /** Envelope: waveform byte, start, end, then (time,value) points. */
    private static byte[] env(int type, int start, int end, int... pts) {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        o.write(type); i32(o, start); i32(o, end); o.write(pts.length / 2);
        for (int p : pts) u16(o, p);
        return o.toByteArray();
    }
    private static byte[] constEnv(int type, int start, int end, int value) { return env(type, start, end, 0, value, 65535, value); }
    private static void put(ByteArrayOutputStream o, byte[] b) { o.write(b, 0, b.length); }

    private static class V {
        byte[] pitch = constEnv(2, 441, 441, 0), amp = constEnv(2, 0, 0, 20000);
        byte[] vibRate, vibDepth, tremRate, tremDepth, gateA, gateB;
        int[][] harm = {{100, 64, 0}}; // {volume, signed-smart pitch (+64 stored), delay}
        int echoDelay, echoFeedback = 100, duration = 100, offset = 7;
        byte[] filter = {0};
        byte[] filterEnv;
        byte[] bytes() {
            ByteArrayOutputStream o = new ByteArrayOutputStream();
            put(o, pitch); put(o, amp);
            if (vibRate == null) o.write(0); else { put(o, vibRate); put(o, vibDepth); }
            if (tremRate == null) o.write(0); else { put(o, tremRate); put(o, tremDepth); }
            if (gateA == null) o.write(0); else { put(o, gateA); put(o, gateB); }
            for (int[] h : harm) { o.write(h[0]); if (h[1] < 128) o.write(h[1]); else u16(o, h[1] - 64 + 49152); o.write(h[2]); }
            if (harm.length < 10) o.write(0);
            o.write(echoDelay); o.write(echoFeedback); u16(o, duration); u16(o, offset);
            put(o, filter);
            if (filterEnv != null) put(o, filterEnv);
            return o.toByteArray();
        }
        SynthVoice build() { SynthVoice c = new SynthVoice(); c.decode(new Stream(bytes())); return c; }
        int[] render(int ms) { int n = 22050 * ms / 1000; return java.util.Arrays.copyOf(build().synthesize(n, ms), n); }
    }

    private static Object get(Object o, String name) throws Exception { Field f = SynthVoice.class.getDeclaredField(name); f.setAccessible(true); return f.get(o); }
    private static int[] arr(Object o, String name) throws Exception { return (int[]) get(o, name); }
    private static int osc(int a, int p, int w) throws Exception {
        Method m = SynthVoice.class.getDeclaredMethod("oscillate", int.class, int.class, int.class); m.setAccessible(true);
        return (Integer) m.invoke(new SynthVoice(), a, p, w);
    }
    private static int firstNonZero(int[] a) { for (int i = 0; i < a.length; i++) if (a[i] != 0) return i; return -1; }
    private static int absMax(int[] a) { int m = 0; for (int x : a) m = Math.max(m, Math.abs(x)); return m; }

    // ---- tables / oscillator -------------------------------------------
    @Test public void tablesHaveExpectedSizesAndValues() throws Exception {
        int[] noise = arr(null, "noiseTable"), sine = arr(null, "sineTable"), buf = arr(null, "sampleBuffer");
        assertEquals(32768, noise.length); assertEquals(32768, sine.length); assertEquals(22050 * 10, buf.length);
        boolean pos = false, neg = false;
        for (int n : noise) { assertTrue(n == 1 || n == -1); pos |= n == 1; neg |= n == -1; }
        assertTrue(pos && neg);
        assertEquals(0, sine[0]); assertEquals(16384, sine[8192], 2); assertEquals(-16384, sine[24576], 2);
    }

    @Test public void oscillatorWaveforms() throws Exception {
        assertEquals(1000, osc(1000, 0, 1)); assertEquals(1000, osc(1000, 16383, 1));
        assertEquals(-1000, osc(1000, 16384, 1)); assertEquals(1000, osc(1000, 32768, 1)); // phase wraps at 32768
        assertEquals(0, osc(1000, 0, 2)); assertEquals(1000, osc(1000, 8192, 2), 1); assertEquals(-1000, osc(1000, 24576, 2), 1);
        assertEquals(-1000, osc(1000, 0, 3)); assertEquals(0, osc(1000, 16384, 3)); assertEquals(500, osc(1000, 24576, 3));
        int a = osc(1000, 0, 4); assertTrue(a == 1000 || a == -1000);
        assertEquals(0, osc(1000, 5, 0)); assertEquals(0, osc(1000, 5, 5));
    }

    // ---- decode ---------------------------------------------------------
    @Test public void decodeReadsFieldsInOrder() throws Exception {
        V v = new V();
        v.vibRate = constEnv(1, 11, 12, 100); v.vibDepth = constEnv(1, 13, 14, 200);
        v.harm = new int[][] {{30, 70, 5}, {40, 60, 6}};
        v.echoDelay = 9; v.echoFeedback = 33; v.duration = 321; v.offset = 45;
        SynthVoice c = v.build();
        assertEquals(30, arr(c, "harmonicVolumes")[0]); assertEquals(40, arr(c, "harmonicVolumes")[1]);
        assertEquals(6, arr(c, "harmonicPitchOffsets")[0]); assertEquals(-4, arr(c, "harmonicPitchOffsets")[1]); // stored - 64
        assertEquals(5, arr(c, "harmonicDelays")[0]); assertEquals(6, arr(c, "harmonicDelays")[1]);
        assertEquals(9, get(c, "echoDelay")); assertEquals(33, get(c, "echoFeedback"));
        assertEquals(321, c.durationMs); assertEquals(45, c.startOffsetMs);
        Object pitch = get(c, "pitchEnvelope"), amp = get(c, "amplitudeEnvelope"), vr = get(c, "vibratoRateEnvelope"), vd = get(c, "vibratoDepthEnvelope");
        assertEquals(441, ((SynthEnvelope) pitch).rangeStart); assertEquals(0, ((SynthEnvelope) amp).rangeStart);
        assertEquals(11, ((SynthEnvelope) vr).rangeStart); assertEquals(13, ((SynthEnvelope) vd).rangeStart);
        assertNull(get(c, "tremoloRateEnvelope")); assertNull(get(c, "gateSilenceEnvelope"));
        assertEquals(500, new SynthVoice().durationMs); assertEquals(100, get(new SynthVoice(), "echoFeedback"));
    }

    // ---- synthesis ------------------------------------------------------
    @Test public void pitchStartIsHzAndAmplitudeIsHalfEnvelopeAtFullHarmonic() {
        int[] out = new V().render(100);
        assertEquals(0, out[0]);
        assertEquals(10000, out[12], 400);     // quarter of a 50-sample period (441 Hz at 22050 Hz)
        assertEquals(-10000, out[37], 400);
        assertEquals(0, out[25], 300);
        assertEquals(10000, out[62], 400);     // next period
        assertEquals(10000, absMax(out), 200);
        V v = new V(); v.pitch = constEnv(2, 882, 882, 0);
        assertEquals(10000, v.render(100)[6], 400); // double frequency, quarter period at 25/4
    }

    @Test public void harmonicVolumeIsPercentOfEnvelope() {
        V v = new V(); v.harm = new int[][] {{50, 64, 0}};
        assertEquals(5000, absMax(v.render(100)), 150);
        v.harm = new int[][] {{100, 64, 0}, {100, 64, 0}};
        assertEquals(20000, absMax(v.render(100)), 400);
    }

    @Test public void harmonicDelayIsMilliseconds() {
        V v = new V(); v.pitch = constEnv(1, 441, 441, 0); v.harm = new int[][] {{100, 64, 2}};
        int[] out = v.render(100);
        assertEquals(44, firstNonZero(out));  // 2 ms * 22.05 samples/ms
        v.harm = new int[][] {{100, 64, 4}};
        assertEquals(88, firstNonZero(v.render(100)));
    }

    @Test public void harmonicPitchOffsetScalesPitchSweepNotBase() {
        V base = new V(); base.pitch = constEnv(2, 100, 100, 0); // no sweep: offset irrelevant
        V off = new V(); off.pitch = base.pitch; off.harm = new int[][] {{100, 64 + 60, 0}};
        assertArrayEquals(base.render(100), off.render(100));
        V s0 = new V(); s0.pitch = env(2, 100, 300, 0, 0, 65535, 65535); // sweep 200 Hz
        V s1 = new V(); s1.pitch = s0.pitch; s1.harm = new int[][] {{100, 64 + 120, 0}}; // +120 units doubles it
        V s2 = new V(); s2.pitch = env(2, 100, 500, 0, 0, 65535, 65535);
        int[] a = s0.render(100), b = s1.render(100), c = s2.render(100);
        assertFalse(java.util.Arrays.equals(a, b));
        assertArrayEquals(b, c);
    }

    @Test public void echoRepeatsOutputAfterDelayWithFeedbackPercent() {
        V base = new V(); base.pitch = constEnv(1, 441, 441, 0);
        int[] dry = base.render(100);
        V e = new V(); e.pitch = base.pitch; e.echoDelay = 2; e.echoFeedback = 50;
        int[] wet = e.render(100);
        int d = 44; int[] ref = dry.clone();
        for (int k = d; k < ref.length; k++) ref[k] += ref[k - d] * 50 / 100;
        assertArrayEquals(ref, wet);
        assertFalse(java.util.Arrays.equals(dry, wet));
        e.echoFeedback = 0; assertArrayEquals(dry, e.render(100));
        e.echoDelay = 0; e.echoFeedback = 80; assertArrayEquals(dry, e.render(100));
    }

    @Test public void gateAlternatesSilentThenSoundingSpans() {
        V v = new V(); v.pitch = constEnv(1, 441, 441, 0);
        // span length = first envelope's start..end range, positioned by the value of the envelope owning the current span
        v.gateA = constEnv(1, 0, 256 * 40, 64);        // silent span: 256*40*64/256 = 10 samples
        v.gateB = constEnv(1, 99999, 99999, 128);      // sounding span: same range, value 128 -> 20 samples (own start/end unused)
        int[] out = v.render(100);
        for (int k = 0; k < 9; k++) assertEquals(0, out[k]);
        for (int k = 9; k < 29; k++) assertNotEquals(0, out[k]);
        for (int k = 29; k < 39; k++) assertEquals(0, out[k]);
        assertNotEquals(0, out[39]);
        V w = new V(); w.pitch = v.pitch; w.gateA = constEnv(1, 0, 256 * 40, 128); w.gateB = constEnv(1, 0, 0, 64); // swapped values
        int[] sw = w.render(100);
        for (int k = 0; k < 19; k++) assertEquals(0, sw[k]);
        for (int k = 19; k < 28; k++) assertNotEquals(0, sw[k]);
        assertEquals(0, sw[29]);
    }

    @Test public void vibratoRateDrivesPhaseAndDepthScalesIt() {
        V base = new V(); base.pitch = env(1, 100, 300, 0, 0, 65535, 65535);
        int[] ref = base.render(50);
        V v = new V(); v.pitch = base.pitch; v.vibRate = constEnv(1, 0, 0, 0); v.vibDepth = constEnv(2, 0, 0, 0);
        assertArrayEquals(ref, v.render(50));          // depth 0 -> no change
        v.vibDepth = constEnv(2, 0, 0, 30000);          // depth > 0 but rate 0, phase frozen at 0 -> constant +depth bias
        int[] biased = v.render(50);
        assertFalse(java.util.Arrays.equals(ref, biased));
        V z = new V(); z.pitch = base.pitch; z.vibRate = constEnv(5, 0, 0, 0); z.vibDepth = constEnv(2, 0, 0, 30000);
        assertArrayEquals(ref, z.render(50));           // rate envelope waveform 5 (unknown) -> oscillator gives 0
    }

    /** LFO phase advances at the rate envelope's Hz: a square LFO at 441 Hz flips after 26 samples at 22050 Hz. */
    @Test public void lfoPhaseAdvancesAtRateHzAndSweepScalesRateEnvelopeValue() {
        for (int which = 0; which < 2; which++) {
            V frozen = new V(), fast = new V(), sweep = new V(), faster = new V();
            frozen.pitch = fast.pitch = sweep.pitch = faster.pitch = which == 0 ? env(2, 100, 300, 0, 0, 65535, 65535) : constEnv(1, 441, 441, 0);
            byte[] depth = constEnv(2, 0, 0, 30000);
            byte[] r0 = constEnv(1, 0, 0, 0), r441 = constEnv(1, 441, 441, 0), r882 = constEnv(1, 882, 882, 0);
            byte[] rSweep = env(1, 0, 441, 0, 65535, 65535, 65535); // 0 + (441-0) * (65535/65536) ~ 441 Hz
            if (which == 0) { frozen.vibRate = r0; fast.vibRate = r441; faster.vibRate = r882; sweep.vibRate = rSweep; frozen.vibDepth = fast.vibDepth = faster.vibDepth = sweep.vibDepth = depth; }
            else { frozen.tremRate = r0; fast.tremRate = r441; faster.tremRate = r882; sweep.tremRate = rSweep; frozen.tremDepth = fast.tremDepth = faster.tremDepth = sweep.tremDepth = depth; }
            int[] a = frozen.render(50), b = fast.render(50), c = faster.render(50), d = sweep.render(50);
            int lag = which == 0 ? 1 : 0; // vibrato changes the pitch step, so the output differs one sample later
            for (int k = 0; k <= 25 + lag; k++) assertEquals("mode " + which + " k=" + k, a[k], b[k]);
            assertNotEquals(a[26 + lag], b[26 + lag]);
            for (int k = 0; k <= 12 + lag; k++) assertEquals("mode " + which + " k=" + k, a[k], c[k]);
            assertNotEquals(a[13 + lag], c[13 + lag]);   // twice the rate flips twice as early
            for (int k = 0; k < 400; k++) assertEquals(b[k], d[k]); // sweep-scale path matches the plain 441 Hz rate (truncation drift only much later)
        }
    }

    @Test public void tremoloDepthScalesAmplitude() {
        V base = new V(); base.pitch = constEnv(1, 441, 441, 0);
        int[] ref = base.render(50);
        V t = new V(); t.pitch = base.pitch; t.tremRate = constEnv(1, 0, 0, 0); t.tremDepth = constEnv(2, 0, 0, 0);
        assertArrayEquals(ref, t.render(50));
        t.tremDepth = constEnv(2, 0, 0, 30000);         // rate 0 -> phase 0 -> +depth: gain 1 + 30000/2/32768
        assertTrue(absMax(t.render(50)) > absMax(ref) + 3000);
        V z = new V(); z.pitch = base.pitch; z.tremRate = constEnv(5, 0, 0, 0); z.tremDepth = constEnv(2, 0, 0, 30000);
        assertArrayEquals(ref, z.render(50));
    }

    @Test public void outputClampedAndShortDurationIsSilent() {
        V v = new V(); v.pitch = constEnv(1, 441, 441, 0); v.amp = constEnv(2, 0, 0, 65535);
        v.harm = new int[][] {{100, 64, 0}, {100, 64, 0}, {100, 64, 0}};
        int[] out = v.render(50);
        for (int x : out) assertTrue(x >= -32768 && x <= 32767);
        assertEquals(32767, out[0]);
        SynthVoice c = new V().build();
        int[] zero = c.synthesize(100, 9);               // duration < 10 ms -> zeroed buffer
        for (int k = 0; k < 100; k++) assertEquals(0, zero[k]);
    }

    @Test public void filterChangesOutputAndFilterEnvelopeSelectsSweepPosition() {
        V base = new V(); base.pitch = constEnv(1, 441, 441, 0);
        int[] dry = base.render(100);
        ByteArrayOutputStream f = new ByteArrayOutputStream();
        f.write(0x10); u16(f, 0); u16(f, 0); f.write(1);        // one pole in filter 0, endpoint differs
        u16(f, 10000); u16(f, 20000);                            // pole 0 start frequency/gain
        u16(f, 30000); u16(f, 20000);                            // pole 0 end frequency/gain
        V a = new V(); a.pitch = base.pitch; a.filter = f.toByteArray();
        ByteArrayOutputStream e0 = new ByteArrayOutputStream(); e0.write(2); u16(e0, 0); u16(e0, 0); u16(e0, 65535); u16(e0, 0);
        ByteArrayOutputStream e1 = new ByteArrayOutputStream(); e1.write(2); u16(e1, 0); u16(e1, 60000); u16(e1, 65535); u16(e1, 60000);
        a.filterEnv = e0.toByteArray(); int[] lo = a.render(100);
        a.filterEnv = e1.toByteArray(); int[] hi = a.render(100);
        assertFalse(java.util.Arrays.equals(dry, lo));
        assertFalse(java.util.Arrays.equals(lo, hi));
    }
}
