import java.io.ByteArrayOutputStream;
import java.lang.reflect.*;
import java.util.*;
import org.junit.*;
import static org.junit.Assert.*;

/** Pass 167: semantic tests for Flo, IDK and Sounds, written against the original identifiers only. */
public class FloIdkSoundsTest {
    private static Field f(Class<?> c, String n) throws Exception { Field x = c.getDeclaredField(n); x.setAccessible(true); return x; }
    private static int geti(Object o, String n) throws Exception { return f(o.getClass(), n).getInt(o); }
    private static void seti(Object o, String n, int v) throws Exception { f(o.getClass(), n).setInt(o, v); }
    private static Object call(Object o, Class<?> c, String n, Class<?>[] t, Object... a) throws Exception {
        Method m = c.getDeclaredMethod(n, t); m.setAccessible(true);
        try { return m.invoke(o, a); } catch (InvocationTargetException e) { throw (Exception) e.getCause(); }
    }
    private static <T> T make(Class<T> c) throws Exception { Constructor<T> k = c.getDeclaredConstructor(); k.setAccessible(true); return k.newInstance(); }
    private static Stream stream(int... b) { byte[] r = new byte[b.length]; for (int i = 0; i < b.length; i++) r[i] = (byte) b[i]; return new Stream(r); }

    private boolean snowFloor, snowForce;

    @Before public void setUp() throws Exception {
        snowFloor = ClientSettings.SNOW_FLOOR_ENABLED; snowForce = ClientSettings.SNOW_FLOOR_FORCE_ENABLED;
        ClientSettings.SNOW_FLOOR_ENABLED = false; ClientSettings.SNOW_FLOOR_FORCE_ENABLED = false;
    }
    @After public void tearDown() {
        ClientSettings.SNOW_FLOOR_ENABLED = snowFloor; ClientSettings.SNOW_FLOOR_FORCE_ENABLED = snowForce;
    }

    // ================= Flo =================
    private static Flo flo(int... bytes) throws Exception {
        Flo x = make(Flo.class);
        call(x, Flo.class, "readValues", new Class<?>[] {Stream.class}, stream(bytes));
        return x;
    }
    private static Flo colour(int rgb) throws Exception { return flo(1, rgb >> 16 & 255, rgb >> 8 & 255, rgb & 255, 0); }

    @Test public void floOpcodesAndDefaults() throws Exception {
        Flo d = flo(0);
        assertEquals(-1, d.textureId); assertTrue(d.occludes); assertEquals(0, d.rgb);
        Flo x = flo(1, 0x12, 0x34, 0x56, 2, 77, 5, 0);
        assertEquals(0x123456, x.rgb); assertEquals(77, x.textureId); assertFalse(x.occludes);
        Flo ignored = flo(3, 6, 'a', 'b', 10, 0); // opcode 3 and the LF-terminated opcode 6 string change nothing
        assertEquals(-1, ignored.textureId); assertTrue(ignored.occludes); assertEquals(0, ignored.rgb);
        ClientSettings.SNOW_FLOOR_FORCE_ENABLED = true;
        assertEquals(0xffffff, flo(1, 1, 2, 3, 0).rgb); // the snow override replaces exactly this colour field
    }

    @Test public void floHslFromRgbHandComputedValues() throws Exception {
        Flo red = colour(0xff0000);
        assertEquals(0, red.hue); assertEquals(255, red.saturation); assertEquals(127, red.lightness);
        Flo green = colour(0x00ff00);
        assertEquals(85, green.hue); assertEquals(255, green.saturation);
        Flo blue = colour(0x0000ff);
        assertEquals(170, blue.hue);
        Flo grey = colour(0x808080);
        assertEquals(0, grey.hue); assertEquals(0, grey.saturation); assertEquals(128, grey.lightness);
        // max=blue, min=red, lightness 0.25 (below 0.5), saturation 0.5, hue 3.5/6
        Flo dark = colour(0x204060);
        assertEquals(149, dark.hue); assertEquals(128, dark.saturation); assertEquals(64, dark.lightness);
        assertEquals(64, dark.hueWeight); assertEquals(37, dark.weightedHue); // weight 0.25*0.5*512; hue 0.58333*64 truncated
        // max=red, min=green: hue is negative and is not wrapped
        Flo neg = colour(0x602040);
        assertEquals(-21, neg.hue); assertEquals(128, neg.saturation); assertEquals(64, neg.lightness);
        assertEquals(64, neg.hueWeight); assertEquals(-5, neg.weightedHue);
        // lightness above 0.5 uses the (1 - lightness) branch for the weight
        Flo light = colour(0xa0c0e0);
        assertEquals(149, light.hue); assertEquals(128, light.saturation); assertEquals(192, light.lightness);
        assertEquals(64, light.hueWeight);
        // grey: no chroma, weight floors at 1 and weighted hue is 0
        assertEquals(1, grey.hueWeight); assertEquals(0, grey.weightedHue);
    }

    @Test public void floMinimapHslIsJitteredPackedColourOfSameColourAndOpcode7OnlyChangesIt() throws Exception {
        Set<Integer> seen = new HashSet<Integer>();
        for (int i = 0; i < 400; i++) {
            int v = colour(0x00ff00).minimapHsl;
            seen.add(v);
            int hue = v >> 10, sat = v >> 7 & 7, light = v & 127;
            assertTrue(hue >= 19 && hue <= 23);          // base hue 85 +/-8, divided by 4
            assertTrue(sat >= 6 && sat <= 7);            // base saturation 255 -24..0 after clamping, divided by 32
            assertTrue(light >= 51 && light <= 75);      // base lightness 127 +/-24, divided by 2
        }
        assertTrue(seen.size() > 5);                      // random jitter
        // opcode 7 after opcode 1: hue/saturation/lightness/weighted hue keep the first colour; only minimapHsl follows the second
        Flo both = flo(1, 0x20, 0x40, 0x60, 7, 0, 255, 0, 0);
        Flo first = colour(0x204060);
        assertEquals(first.rgb, both.rgb); assertEquals(149, both.hue); assertEquals(128, both.saturation);
        assertEquals(64, both.lightness); assertEquals(37, both.weightedHue);
        assertTrue((both.minimapHsl >> 10) >= 19 && (both.minimapHsl >> 10) <= 23); // green-hued packed value; the first colour's hue field is about 37
        assertTrue((first.minimapHsl >> 10) >= 33 && (first.minimapHsl >> 10) <= 41);
        assertEquals(37, both.hueWeight); // quirk preserved: opcode 7 stores the previous weighted hue in hueWeight
        assertEquals(64, first.hueWeight);
    }

    @Test public void floMethod263PacksHueSaturationLightnessAndDampsBrightSaturation() throws Exception {
        Flo x = make(Flo.class);
        Class<?>[] t = {int.class, int.class, int.class};
        assertEquals((63 << 10) + (7 << 7) + 50, call(x, Flo.class, "packHsl", t, 255, 255, 100));
        assertEquals((10 << 10) + (3 << 7) + 50, call(x, Flo.class, "packHsl", t, 40, 100, 100));
        // saturation is halved once each above lightness 179, 192, 217, 243
        assertEquals(224 / 32 << 7, 0x380 & (Integer) call(x, Flo.class, "packHsl", t, 0, 224, 179));
        assertEquals(112 / 32 << 7, 0x380 & (Integer) call(x, Flo.class, "packHsl", t, 0, 224, 180));
        assertEquals(56 / 32 << 7, 0x380 & (Integer) call(x, Flo.class, "packHsl", t, 0, 224, 193));
        assertEquals(28 / 32 << 7, 0x380 & (Integer) call(x, Flo.class, "packHsl", t, 0, 224, 218));
        assertEquals(0, 0x380 & (Integer) call(x, Flo.class, "packHsl", t, 0, 224, 244));
        assertEquals(1 << 10, 0x7c00 & (Integer) call(x, Flo.class, "packHsl", t, 4, 0, 0)); // first argument is the hue
        assertEquals(1 << 7, 0x380 & (Integer) call(x, Flo.class, "packHsl", t, 0, 32, 0)); // second is the saturation
        assertEquals(1, 127 & (Integer) call(x, Flo.class, "packHsl", t, 0, 0, 2));          // third is the lightness
    }

    // ================= IDK =================
    static final class Spy extends OnDemandFetcherParent {
        final List<Integer> requested = new ArrayList<Integer>();
        @Override public void requestModel(int i) { requested.add(i); }
    }
    private static byte[] triangle(int id, int color) {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        int x = 20 + id;
        for (int i = 0; i < 3; i++) o.write(7);
        o.write(1);
        o.write(0 + 64); o.write(1 + 64); o.write(1 + 64);
        o.write(color >> 8); o.write(color);
        o.write(x + 64); o.write(10 + 64); o.write(-10 + 64);
        o.write(64); o.write(64); o.write(10 + 64);
        o.write(64); o.write(64); o.write(10 + 64);
        o.write(0); o.write(3); o.write(0); o.write(1);
        o.write(0); o.write(0); o.write(0); o.write(0); o.write(0); o.write(0);
        o.write(0); o.write(3); o.write(0); o.write(3); o.write(0); o.write(3);
        o.write(0); o.write(3);
        return o.toByteArray();
    }
    private Object savedArr, savedFetcher;
    private Spy spy;
    private boolean modelsSaved;
    private void withModels() throws Exception {
        modelsSaved = true;
        savedArr = f(Model.class, "modelHeaders").get(null); savedFetcher = f(Model.class, "modelRequester").get(null);
        spy = new Spy(); Model.initModelHeaders(64, spy);
    }
    @After public void restoreModels() throws Exception {
        if (modelsSaved) { f(Model.class, "modelHeaders").set(null, savedArr); f(Model.class, "modelRequester").set(null, savedFetcher); modelsSaved = false; }
    }
    private static IDK idk(int... bytes) throws Exception {
        IDK x = make(IDK.class);
        call(x, IDK.class, "readValues", new Class<?>[] {Stream.class}, stream(bytes));
        return x;
    }
    private static int[] colours(Model m) throws Exception { return Arrays.copyOf((int[]) f(Model.class, "faceColors").get(m), m.faceCount); }

    @Test public void idkOpcodesAndDefaults() throws Exception {
        IDK d = idk(0);
        assertEquals(-1, d.bodyPartIndex); assertFalse(d.notSelectable);
        assertArrayEquals(new int[] {-1, -1, -1, -1, -1}, (int[]) f(IDK.class, "headModelIds").get(d));
        assertNull(f(IDK.class, "bodyModelIds").get(d));
        IDK x = idk(1, 5, 2, 2, 0, 9, 1, 4, 3, 40, 1, 2, 41, 0, 7, 50, 3, 4, 51, 0, 8, 60, 0, 11, 64, 0, 12, 99, 0);
        assertEquals(5, x.bodyPartIndex); assertTrue(x.notSelectable);
        assertArrayEquals(new int[] {9, 260}, (int[]) f(IDK.class, "bodyModelIds").get(x));
        assertArrayEquals(new int[] {258, 7, 0, 0, 0, 0}, (int[]) f(IDK.class, "recolourSources").get(x));
        assertArrayEquals(new int[] {772, 8, 0, 0, 0, 0}, (int[]) f(IDK.class, "recolourReplacements").get(x));
        assertArrayEquals(new int[] {11, -1, -1, -1, 12}, (int[]) f(IDK.class, "headModelIds").get(x));
    }

    @Test public void idkBodyModelsReadinessAndBuild() throws Exception {
        withModels();
        assertTrue(idk(0).areBodyModelsLoaded());               // no body models: ready
        IDK k = idk(2, 2, 0, 5, 0, 6, 40, 0xc3, 0x50, 50, 0x04, 0xd2, 0);  // models 5,6; recolour 50000 -> 1234
        assertFalse(k.areBodyModelsLoaded());
        assertEquals(Arrays.asList(5, 6), spy.requested); // every missing model is requested, not just the first
        Model.loadModelHeader(triangle(5, 50000), 5);
        assertFalse(k.areBodyModelsLoaded());                    // one of two loaded is not ready
        Model.loadModelHeader(triangle(6, 50000), 6);
        assertTrue(k.areBodyModelsLoaded());
        Model built = k.buildBodyModel();
        assertEquals(2, built.faceCount);
        assertArrayEquals(new int[] {1234, 1234}, colours(built));
        assertNull(idk(0).buildBodyModel());               // no body models: nothing to build
        IDK single = idk(2, 1, 0, 5, 40, 0x11, 0x11, 50, 0x22, 0x22, 0); // recolour that matches nothing
        assertEquals(1, single.buildBodyModel().faceCount);
        assertArrayEquals(new int[] {50000}, colours(single.buildBodyModel()));
        // recolouring stops at the first zero source colour
        IDK stop = idk(2, 1, 0, 5, 40, 0, 0, 41, 0xc3, 0x50, 50, 0, 1, 51, 0, 2, 0);
        assertArrayEquals(new int[] {50000}, colours(stop.buildBodyModel()));
    }

    @Test public void idkHeadModelsReadinessAndBuild() throws Exception {
        withModels();
        assertTrue(idk(0).areHeadModelsLoaded());                // all -1: ready
        IDK k = idk(60, 0, 5, 62, 0, 6, 40, 0xc3, 0x50, 50, 0x04, 0xd2, 0);
        assertFalse(k.areHeadModelsLoaded());
        assertEquals(Arrays.asList(5, 6), spy.requested);
        Model.loadModelHeader(triangle(5, 50000), 5);
        assertFalse(k.areHeadModelsLoaded());
        Model.loadModelHeader(triangle(6, 50000), 6);
        assertTrue(k.areHeadModelsLoaded());
        Model built = k.buildHeadModel();
        assertEquals(2, built.faceCount);               // gaps (-1) are skipped, present slots are combined
        assertArrayEquals(new int[] {1234, 1234}, colours(built));
        // body models do not count for the head readiness
        IDK bodyOnly = idk(2, 1, 0, 9, 0);
        assertTrue(bodyOnly.areHeadModelsLoaded());
    }

    @Test public void idkBodyPartAndHiddenFlagSelectGameDesignerKits() throws Exception {
        Object savedCache = f(IDK.class, "cache").get(null); int savedLen = IDK.length;
        try {
            IDK[] kits = new IDK[] {idk(1, 0, 0), idk(1, 7, 0), idk(1, 2, 3, 0), idk(1, 2, 0), idk(1, 9, 0)};
            IDK.cache = kits; IDK.length = kits.length;
            Field uf = sun.misc.Unsafe.class.getDeclaredField("theUnsafe"); uf.setAccessible(true);
            sun.misc.Unsafe u = (sun.misc.Unsafe) uf.get(null);
            Game g = (Game) u.allocateInstance(Game.class);
            Field arr = f(Game.class, "designerKitIds"); arr.set(g, new int[7]);
            g.designerMale = true;
            g.selectDefaultDesignerKits();
            int[] male = (int[]) arr.get(g);
            assertEquals(0, male[0]); assertEquals(-1, male[1]); assertEquals(3, male[2]); // kit 2 is hidden, kit 3 is the part-2 kit
            g.designerMale = false;
            g.selectDefaultDesignerKits();
            int[] female = (int[]) arr.get(g);
            assertEquals(1, female[0]); assertEquals(4, female[2]); assertEquals(-1, female[1]); // part index + 7
        } finally { IDK.cache = (IDK[]) savedCache; IDK.length = savedLen; }
    }

    // ================= Sounds =================
    private static Sounds sound(int loopBegin, int loopEnd, int[]... voices) throws Exception {
        Sounds s = make(Sounds.class);
        SynthVoice[] arr = (SynthVoice[]) f(Sounds.class, "voices").get(s);
        for (int i = 0; i < voices.length; i++) { SynthVoice c = new SynthVoice(); c.durationMs = voices[i][0]; c.startOffsetMs = voices[i][1]; arr[i] = c; }
        seti(s, "loopStartMillis", loopBegin); seti(s, "loopEndMillis", loopEnd);
        return s;
    }
    private static final Class<?>[] I = {int.class};

    @Test public void soundsMethod245LengthAndLoopRepeat() throws Exception {
        Sounds.unpack(stream(255, 255));
        // 9ms voice: 198 samples (voices of 10ms or more need real envelopes); loop 2ms..6ms is samples 44..132 (88 samples), repeated playCount-1 more times
        Sounds s = sound(2, 6, new int[] {9, 0});
        assertEquals(198, call(s, Sounds.class, "renderSamples", I, 1));
        assertEquals(198 + 88 * 2, call(s, Sounds.class, "renderSamples", I, 3));
        // start offset counts towards the total; the longest voice wins
        Sounds t = sound(0, 0, new int[] {9, 5}, new int[] {3, 0});
        assertEquals(22050 * 14 / 1000, call(t, Sounds.class, "renderSamples", I, 1));
        // invalid loop ranges ignore the repeat count (length formula then uses a count of 0)
        assertEquals(198, call(sound(6, 6, new int[] {9, 0}), Sounds.class, "renderSamples", I, 4));
        assertEquals(198 + 88, call(sound(6, 2, new int[] {9, 0}), Sounds.class, "renderSamples", I, 4));
        assertEquals(198 - (220 - 44), call(sound(2, 10, new int[] {9, 0}), Sounds.class, "renderSamples", I, 4));
        // no voices: zero length
        assertEquals(0, call(sound(2, 6), Sounds.class, "renderSamples", I, 3));
    }

    @Test public void soundsMethod244WritesWavHeaderAroundTheSamples() throws Exception {
        Sounds.unpack(stream(255, 255));
        Sounds s = sound(2, 6, new int[] {9, 0});
        Stream out = (Stream) call(s, Sounds.class, "writeWav", I, 3);
        int n = 198 + 88 * 2;
        assertEquals(44 + n, out.currentOffset);
        byte[] b = out.buffer;
        assertEquals("RIFF", new String(b, 0, 4)); assertEquals("WAVE", new String(b, 8, 4)); assertEquals("fmt ", new String(b, 12, 4));
        assertEquals("data", new String(b, 36, 4));
        assertEquals(36 + n, (b[4] & 255) | (b[5] & 255) << 8 | (b[6] & 255) << 16);
        assertEquals(22050, (b[24] & 255) | (b[25] & 255) << 8 | (b[26] & 255) << 16);
        assertEquals(n, (b[40] & 255) | (b[41] & 255) << 8 | (b[42] & 255) << 16);
        assertEquals(8, b[34]);                           // 8 bits per sample
        for (int i = 44; i < 44 + n; i++) assertEquals(-128, b[i]); // silent voice is mid-scale
    }

    @Test public void soundsMethod243TrimsLeadingSilenceInTwentyMillisecondUnits() throws Exception {
        Sounds s = sound(40, 80, new int[] {10, 100}, new int[] {10, 60});
        assertEquals(2, call(s, Sounds.class, "trimLeadingSilence", new Class<?>[0])); // min(100/20, 60/20, 40/20)
        SynthVoice[] v = (SynthVoice[]) f(Sounds.class, "voices").get(s);
        assertEquals(60, v[0].startOffsetMs); assertEquals(20, v[1].startOffsetMs);
        assertEquals(0, geti(s, "loopStartMillis")); assertEquals(40, geti(s, "loopEndMillis"));
        // an invalid loop does not take part in the minimum or the shift
        Sounds t = sound(40, 40, new int[] {10, 100}, new int[] {10, 60});
        assertEquals(3, call(t, Sounds.class, "trimLeadingSilence", new Class<?>[0]));
        assertEquals(40, geti(t, "loopStartMillis")); assertEquals(40, geti(t, "loopEndMillis"));
        // less than one unit of silence, or nothing at all: nothing is trimmed
        Sounds u = sound(0, 0, new int[] {10, 19});
        assertEquals(0, call(u, Sounds.class, "trimLeadingSilence", new Class<?>[0]));
        assertEquals(19, ((SynthVoice[]) f(Sounds.class, "voices").get(u))[0].startOffsetMs);
        assertEquals(0, call(sound(0, 0), Sounds.class, "trimLeadingSilence", new Class<?>[0]));
    }

    @Test public void soundsUnpackReadsLoopAndRecordsLeadingSilence() throws Exception {
        Object[] byId = (Object[]) f(Sounds.class, "soundsById").get(null);
        Object old = byId[4998]; int oldDelay = Sounds.leadingSilence20msUnits[4998];
        try {
            // id 4998 (0x1386): ten empty voice slots, loop 40..80, then terminator
            int[] rec = new int[18];
            rec[0] = 0x13; rec[1] = 0x86; rec[13] = 40; rec[15] = 80; rec[16] = 255; rec[17] = 255;
            Sounds.unpack(stream(rec));
            Sounds s = (Sounds) ((Object[]) f(Sounds.class, "soundsById").get(null))[4998];
            assertNotNull(s);
            assertEquals(2, Sounds.leadingSilence20msUnits[4998]);       // 40ms / 20
            assertEquals(0, geti(s, "loopStartMillis")); assertEquals(40, geti(s, "loopEndMillis")); // shifted by the trim
            assertNull(Sounds.buildWave(1, 4997));              // unknown id has no wave
            assertNotNull(Sounds.buildWave(1, 4998));
        } finally { ((Object[]) f(Sounds.class, "soundsById").get(null))[4998] = old; Sounds.leadingSilence20msUnits[4998] = oldDelay; }
    }
}
