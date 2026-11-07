import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/** Behaviour of the music player family (MusicPlayer, MidiMusicPlayer, JavaMidiMusicPlayer, BufferedMidiMusicPlayer) and the volume state in Game. */
public class MusicPlayerTest {
    // Member names are reached by reflection where they are private; kept together so a rename touches only these lines.
    private static final String M_CHANNEL_GAIN = "channelOutputVolume";
    private static final String M_QUEUE = "queueEvent";
    private static final String M_FLUSH = "flushEvents";
    private static final String M_DISPATCH = "dispatchNextEvent";
    private static final String F_RECEIVER = "receiver";
    private static final String F_SEQUENCER = "sequencer";
    private static final String F_SINK = "eventSink";
    private static final String F_SHUTDOWN = "shutdownRequested";
    private static final String F_LOOP = "looping";
    private static final String F_LAST_TIME = "lastEventTime";
    private static final String F_BUF_LEN = "eventBufferLength";
    private static final String F_BUF = "eventBuffer";
    private static final String F_READER = "sequenceReader";

    private int savedMaster;
    private int[] savedChannels;
    private BufferedMidiMusicPlayer live;

    @Before public void setUp() {
        savedMaster = Game.musicOutputVolume;
        savedChannels = Game.channelVolumes.clone();
        resetVolumeState();
    }

    @After public void tearDown() {
        if (live != null) { live.shutdown(); live = null; }
        Game.musicOutputVolume = savedMaster;
        System.arraycopy(savedChannels, 0, Game.channelVolumes, 0, 16);
    }

    private static void resetVolumeState() {
        Game.musicOutputVolume = 256;
        Arrays.fill(Game.channelVolumes, 12800);
    }

    // ---------------------------------------------------------------- reflection helpers
    private static Object invokeStatic(Class<?> c, String name, Object... args) {
        for (Method m : c.getDeclaredMethods()) {
            if (m.getName().equals(name) && m.getParameterTypes().length == args.length) {
                m.setAccessible(true);
                try { return m.invoke(null, args); }
                catch (InvocationTargetException e) { throw new RuntimeException(e.getCause()); }
                catch (IllegalAccessException e) { throw new AssertionError(e); }
            }
        }
        throw new AssertionError("no method " + name);
    }

    private static Object invoke(Object target, String name, Object... args) {
        for (Method m : target.getClass().getDeclaredMethods()) {
            if (m.getName().equals(name) && m.getParameterTypes().length == args.length) {
                m.setAccessible(true);
                try { return m.invoke(target, args); }
                catch (InvocationTargetException e) {
                    if (e.getCause() instanceof RuntimeException) throw (RuntimeException) e.getCause();
                    throw new RuntimeException(e.getCause());
                } catch (IllegalAccessException e) { throw new AssertionError(e); }
            }
        }
        throw new AssertionError("no method " + name);
    }

    private static Field field(Class<?> c, String name) {
        try { Field f = c.getDeclaredField(name); f.setAccessible(true); return f; }
        catch (NoSuchFieldException e) { throw new AssertionError(e); }
    }

    private static Object get(Class<?> c, Object target, String name) {
        try { return field(c, name).get(target); } catch (IllegalAccessException e) { throw new AssertionError(e); }
    }

    private static void set(Class<?> c, Object target, String name, Object value) {
        try { field(c, name).set(target, value); } catch (IllegalAccessException e) { throw new AssertionError(e); }
    }

    private static Object allocate(Class<?> c) {
        try {
            Class<?> u = Class.forName("sun.misc.Unsafe");
            Field f = u.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            return u.getMethod("allocateInstance", Class.class).invoke(f.get(null), c);
        } catch (Exception e) { throw new AssertionError(e); }
    }

    // ---------------------------------------------------------------- MidiMusicPlayer fixture
    /** Records every message the shared Sub1 logic sends; the abstract player methods are inert. */
    static final class Recorder extends MidiMusicPlayer {
        final List<long[]> sent = new ArrayList<long[]>();
        void sendMessage(int i, int a, int b, long l) { sent.add(new long[] { i, a, b, l }); }
        void play(int i, byte[] is, int j, boolean b) { }
        void shutdown() { }
        void fade(int i, int j) { }
        void setVolume(int i) { }
        void tick(int i) { }
        void stop() { }
    }

    private static int channelGain(int channel) { return (Integer) invokeStatic(MidiMusicPlayer.class, M_CHANNEL_GAIN, channel); }

    private static void assertVolumePairs(List<long[]> sent, int from, long time, int coarse, int fine) {
        for (int ch = 0; ch < 16; ch++) {
            assertArrayEquals("coarse ch" + ch, new long[] { 176 + ch, 7, coarse, time }, sent.get(from + ch * 2));
            assertArrayEquals("fine ch" + ch, new long[] { 176 + ch, 39, fine, time }, sent.get(from + ch * 2 + 1));
        }
    }

    // ---------------------------------------------------------------- channelOutputVolume: output volume of a channel
    @Test public void channelOutputVolumeScalesTheChannelVolumeByTheSquareRootOfTheMasterFraction() {
        assertEquals(12800, channelGain(0));          // master 256 = unchanged
        Game.musicOutputVolume = 64;
        assertEquals(6400, channelGain(0));           // quarter master -> half (amplitude)
        Game.musicOutputVolume = 0;
        assertEquals(0, channelGain(0));
        Game.musicOutputVolume = 100;
        Game.channelVolumes[1] = 8000;
        assertEquals(5000, channelGain(1));           // sqrt((8000*100>>8)*8000)
        Game.musicOutputVolume = 255;
        assertEquals(12775, channelGain(0));          // 12774.78 is rounded, not truncated
        Game.musicOutputVolume = 256;
        Game.channelVolumes[3] = 4096;
        assertEquals(4096, channelGain(3));           // the index selects the channel
        assertEquals(12800, channelGain(2));
        assertEquals(12800, channelGain(15));
    }

    // ---------------------------------------------------------------- setMasterVolume: set output volume, reset channel volumes, resend
    @Test public void setVolumeStoresTheOutputVolumeResetsEveryChannelAndResendsBothControllers() {
        Recorder r = new Recorder();
        for (int i = 0; i < 16; i++) Game.channelVolumes[i] = 1000 + i;
        r.setMasterVolume(128, 77L);
        assertEquals(128, Game.musicOutputVolume);
        for (int i = 0; i < 16; i++) assertEquals(12800, Game.channelVolumes[i]);
        assertEquals(32, r.sent.size());
        assertVolumePairs(r.sent, 0, 77L, 70, 91);    // 9051 = 70 * 128 + 91
        r.sent.clear();
        r.setMasterVolume(256, 5L);
        assertVolumePairs(r.sent, 0, 5L, 100, 0);     // 12800 = 100 * 128 + 0
    }

    // ---------------------------------------------------------------- applyFade: fade
    @Test public void fadeAttenuatesTheVolumeByTenToTheMinusFiveTenThousandthsPerUnitAndOnlyResendsOnChange() {
        Recorder r = new Recorder();
        r.applyFade(0, 256, 9L);
        assertTrue("no change, nothing sent", r.sent.isEmpty());
        assertEquals(256, Game.musicOutputVolume);
        Game.channelVolumes[5] = 9999;
        r.applyFade(2000, 256, 9L);                   // 256 * 0.1 + 0.5 -> 26
        assertEquals(26, Game.musicOutputVolume);
        assertEquals(32, r.sent.size());
        assertEquals(31, r.sent.get(0)[2]);           // 4079 = 31 * 128 + 111, built from channel 0 = 12800
        assertEquals(111, r.sent.get(1)[2]);
        assertEquals(9999, Game.channelVolumes[5]);    // channel volumes are kept, unlike setVolume
        assertEquals(channelGain(5) >> 7, r.sent.get(10)[2]);
        for (long[] m : r.sent) assertEquals(9L, m[3]);
        r.sent.clear();
        r.applyFade(2000, 256, 9L);
        assertTrue("same resulting volume sends nothing", r.sent.isEmpty());
        r.applyFade(1000, 256, 9L);                   // 256 * 10^-0.5 = 80.96 -> 81
        assertEquals(81, Game.musicOutputVolume);
        r.sent.clear();
        r.applyFade(0, 100, 3L);                      // second int is the volume, first is the fade
        assertEquals(100, Game.musicOutputVolume);
        assertEquals(32, r.sent.size());
        r.applyFade(256, 2000, 3L);                   // swapped arguments would give ~1489
        assertEquals(1489, Game.musicOutputVolume);
    }

    // ---------------------------------------------------------------- filterVolumeController: controller filter
    @Test public void controllerFilterOnlyConsumesVolumeAndResetControllers() {
        Recorder r = new Recorder();
        assertFalse(r.filterVolumeController(0x90, 7, 50, 1L));    // note on, not a controller
        assertFalse(r.filterVolumeController(0xC0, 7, 50, 1L));
        assertFalse(r.filterVolumeController(0xB2, 10, 50, 1L));   // pan
        assertTrue(r.sent.isEmpty());
        assertEquals(12800, Game.channelVolumes[2]);

        assertTrue(r.filterVolumeController(0xB2, 7, 50, 11L));    // coarse volume on channel 2
        assertEquals(6400, Game.channelVolumes[2]);    // 50 << 7, the fine half was 0
        assertEquals(2, r.sent.size());               // the raw message is not forwarded
        assertArrayEquals(new long[] { 0xB2, 7, 50, 11L }, r.sent.get(0));
        assertArrayEquals(new long[] { 0xB2, 39, 0, 11L }, r.sent.get(1));
        r.sent.clear();

        assertTrue(r.filterVolumeController(0xB2, 39, 5, 12L));    // fine volume keeps the coarse half
        assertEquals(6405, Game.channelVolumes[2]);
        assertEquals(50, r.sent.get(0)[2]);
        assertEquals(5, r.sent.get(1)[2]);
        r.sent.clear();

        Game.channelVolumes[3] = 1234;
        assertTrue(r.filterVolumeController(0xB3, 121, 0, 13L));   // reset all controllers: forwarded, then volume restored
        assertEquals(12800, Game.channelVolumes[3]);
        assertEquals(3, r.sent.size());
        assertArrayEquals(new long[] { 0xB3, 121, 0, 13L }, r.sent.get(0));
        assertArrayEquals(new long[] { 0xB3, 7, 100, 13L }, r.sent.get(1));
        assertArrayEquals(new long[] { 0xB3, 39, 0, 13L }, r.sent.get(2));
        r.sent.clear();

        Game.musicOutputVolume = 64;                          // the master volume scales what is sent
        assertTrue(r.filterVolumeController(0xBF, 7, 100, 14L));   // channel 15
        assertEquals(12800, Game.channelVolumes[15]);
        assertEquals(50, r.sent.get(0)[2]);
        assertEquals(0xBF, r.sent.get(0)[0]);
    }

    // ---------------------------------------------------------------- resetAllChannels: reset everything
    @Test public void resetAllChannelsSendsSixControllerSweepsInOrder() {
        Recorder r = new Recorder();
        Game.channelVolumes[4] = 555;
        r.resetAllChannels(42L);
        assertEquals(96, r.sent.size());
        int[][] sweeps = { { 176, 123 }, { 176, 120 }, { 176, 121 }, { 176, 0 }, { 176, 32 }, { 192, 0 } };
        for (int s = 0; s < 6; s++)
            for (int ch = 0; ch < 16; ch++)
                assertArrayEquals("sweep " + s + " ch " + ch, new long[] { sweeps[s][0] + ch, sweeps[s][1], 0, 42L }, r.sent.get(s * 16 + ch));
        assertEquals("sent directly, volumes are not touched", 555, Game.channelVolumes[4]);
    }

    // ---------------------------------------------------------------- JavaMidiMusicPlayer (javax.sound.midi)
    private static final class Log {
        final List<String> events = new ArrayList<String>();
    }

    private static Receiver fakeReceiver(final Log log) {
        return new Receiver() {
            public void send(MidiMessage m, long t) {
                ShortMessage s = (ShortMessage) m;
                log.events.add("msg:" + s.getStatus() + "," + s.getData1() + "," + s.getData2() + "," + t);
            }
            public void close() { log.events.add("receiver.close"); }
        };
    }

    private static Sequencer fakeSequencer(final Log log) {
        return (Sequencer) Proxy.newProxyInstance(Sequencer.class.getClassLoader(), new Class<?>[] { Sequencer.class }, new InvocationHandler() {
            public Object invoke(Object p, Method m, Object[] a) {
                String n = m.getName();
                if (n.equals("setLoopCount")) log.events.add("loop:" + a[0]);
                else if (n.equals("setSequence")) log.events.add("sequence");
                else if (n.equals("start") || n.equals("stop") || n.equals("close")) log.events.add(n);
                Class<?> t = m.getReturnType();
                if (t == boolean.class) return false;
                if (t == int.class) return 0;
                if (t == long.class) return 0L;
                if (t == float.class) return 0f;
                return null;
            }
        });
    }

    private static JavaMidiMusicPlayer javaPlayer(Log log, boolean withSequencer) {
        JavaMidiMusicPlayer p = (JavaMidiMusicPlayer) allocate(JavaMidiMusicPlayer.class);
        set(JavaMidiMusicPlayer.class, null, F_RECEIVER, fakeReceiver(log));
        set(JavaMidiMusicPlayer.class, null, F_SEQUENCER, withSequencer ? fakeSequencer(log) : null);
        return p;
    }

    private static byte[] standardMidi() {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        byte[] head = { 'M', 'T', 'h', 'd', 0, 0, 0, 6, 0, 0, 0, 1, 1, (byte) 0xE0, 'M', 'T', 'r', 'k', 0, 0, 0, 4, 0, (byte) 0xFF, 0x2F, 0 };
        o.write(head, 0, head.length);
        return o.toByteArray();
    }

    @After public void clearJavaStatics() {
        set(JavaMidiMusicPlayer.class, null, F_RECEIVER, null);
        set(JavaMidiMusicPlayer.class, null, F_SEQUENCER, null);
    }

    @Test public void javaPlayerSendsEncodedShortMessagesWithTheTimestampAndIgnoresInvalidOnes() {
        Log log = new Log();
        JavaMidiMusicPlayer p = javaPlayer(log, true);
        p.sendMessage(0x90, 60, 127, 5L);
        p.sendMessage(0xB4, 7, 0, 6L);
        p.sendMessage(0x90, 200, 1, 7L);                // data byte above 127 is invalid MIDI
        assertEquals(Arrays.asList("msg:144,60,127,5", "msg:180,7,0,6"), log.events);
    }

    @Test public void javaPlayerReceivesRoutesVolumeControllersThroughTheFilterAndForwardsEverythingElse() throws Exception {
        Log log = new Log();
        JavaMidiMusicPlayer p = javaPlayer(log, true);
        p.send(new ShortMessage(0x90, 0, 60, 64), 8L);
        p.send(new ShortMessage(0xB0, 1, 7, 100), 9L);
        assertEquals(Arrays.asList("msg:144,60,64,8", "msg:177,7,100,9", "msg:177,39,0,9"), log.events);
        assertEquals(12800, Game.channelVolumes[1]);
    }

    @Test public void javaPlayerPlayLoadsTheSongSetsTheLoopCountAppliesVolumeThenStarts() {
        Log log = new Log();
        JavaMidiMusicPlayer p = javaPlayer(log, true);
        p.play(128, standardMidi(), 12345, true);
        assertEquals("sequence", log.events.get(0));
        assertEquals("loop:-1", log.events.get(1));
        assertEquals("msg:176,7,70,-1", log.events.get(2));
        assertEquals(34, log.events.size() - 1);
        assertEquals("start", log.events.get(log.events.size() - 1));
        assertEquals(128, Game.musicOutputVolume);
        log.events.clear();
        p.play(128, standardMidi(), 0, false);   // same volume: nothing to resend; third int is ignored
        assertEquals(Arrays.asList("sequence", "loop:0", "start"), log.events);
        log.events.clear();
        p.play(64, new byte[] { 1, 2, 3 }, 0, true);   // not a MIDI file: swallowed, nothing started
        assertTrue(log.events.isEmpty());
        assertEquals(128, Game.musicOutputVolume);
    }

    @Test public void javaPlayerStopStopsTheSequencerAndResetsAllChannels() {
        Log log = new Log();
        JavaMidiMusicPlayer p = javaPlayer(log, true);
        p.stop();
        assertEquals("stop", log.events.get(0));
        assertEquals(97, log.events.size());
        assertEquals("msg:176,123,0,-1", log.events.get(1));
        assertEquals("msg:207,0,0,-1", log.events.get(96));
    }

    @Test public void javaPlayerVolumeAndFadeOnlyActWhenTheSequencerExists() {
        Log log = new Log();
        JavaMidiMusicPlayer p = javaPlayer(log, true);
        p.setVolume(64);
        assertEquals(64, Game.musicOutputVolume);
        assertEquals(32, log.events.size());
        assertEquals("msg:176,7,50,-1", log.events.get(0));
        log.events.clear();
        p.fade(256, 2000);                       // volume 256 faded by 2000 -> 26
        assertEquals(26, Game.musicOutputVolume);
        assertEquals(32, log.events.size());

        Log none = new Log();
        JavaMidiMusicPlayer q = javaPlayer(none, false);
        resetVolumeState();
        q.setVolume(64);
        q.fade(256, 2000);
        q.stop();
        q.play(64, standardMidi(), 0, true);
        assertTrue(none.events.isEmpty());
        assertEquals(256, Game.musicOutputVolume);
    }

    @Test public void javaPlayerTickDoesNothingUnlessTheGuardArgumentExceedsMinusNinety() {
        Log log = new Log();
        JavaMidiMusicPlayer p = javaPlayer(log, true);
        p.tick(-122);
        p.tick(-126);
        p.tick(-90);
        assertTrue(log.events.isEmpty());
        p.tick(-89);                             // the guard value that falls into stop
        assertEquals("stop", log.events.get(0));
        assertEquals(97, log.events.size());
    }

    @Test public void javaPlayerShutdownClosesBothDevicesAndIsSafeTwice() {
        Log log = new Log();
        JavaMidiMusicPlayer p = javaPlayer(log, true);
        p.shutdown();
        assertEquals(Arrays.asList("close", "receiver.close"), log.events);
        assertNull(get(JavaMidiMusicPlayer.class, null, F_SEQUENCER));
        assertNull(get(JavaMidiMusicPlayer.class, null, F_RECEIVER));
        p.shutdown();
        assertEquals(2, log.events.size());
    }

    // ---------------------------------------------------------------- BufferedMidiMusicPlayer (event buffer to a sink)
    static final class Sink implements MidiEventSink {
        final List<int[]> batches = new ArrayList<int[]>();
        int resets, closes, lastInitArg;
        volatile int reading;
        boolean closedWith;
        public synchronized void writeEvents(int[] is, int n) {
            batches.add(Arrays.copyOf(is, n));
        }
        public void method11(boolean b) { closes++; closedWith = b; }
        public void method12(boolean b) { resets++; }
        public void close() { }
        public int getPlaybackPosition(int i) { return reading; }
        public void method15(byte b) { lastInitArg = b; }
        public void run() { }
    }

    private static void resetSub2Statics(Sink sink) {
        Class<?> c = BufferedMidiMusicPlayer.class;
        set(c, null, F_SINK, sink);
        set(c, null, F_SHUTDOWN, false);
        set(c, null, F_LOOP, false);
        set(c, null, F_LAST_TIME, 0);
        set(c, null, F_BUF_LEN, 0);
        set(c, null, F_BUF, new int[256]);
    }

    /** A player without its pump thread, so ticks are driven only by the test. */
    private static BufferedMidiMusicPlayer sub2(Sink sink) {
        resetSub2Statics(sink);
        BufferedMidiMusicPlayer p = (BufferedMidiMusicPlayer) allocate(BufferedMidiMusicPlayer.class);
        set(BufferedMidiMusicPlayer.class, p, F_READER, new Class11());
        return p;
    }

    private static List<long[]> decode(List<int[]> batches) {
        List<long[]> out = new ArrayList<long[]>();
        for (int[] b : batches)
            for (int i = 0; i < b.length; i += 2)
                out.add(new long[] { b[i], b[i + 1] & 0xff, (b[i + 1] >> 8) & 0xff, (b[i + 1] >> 16) & 0xff });
        return out;
    }

    private static long[] ev(long delta, int status, int d1, int d2) { return new long[] { delta, status, d1, d2 }; }

    /** Song in the cache layout: lengths and deltas are stored as 128 - value. Division 500, so one tick is one millisecond. */
    private static byte[] cacheSong(int[][] events) {
        ByteArrayOutputStream t = new ByteArrayOutputStream();
        for (int[] e : events) {
            t.write(128 - e[0]);
            if (e[1] == 0xFF) { t.write(0xFF); t.write(0x2F); t.write(0x80); continue; }
            t.write(e[1]);
            t.write(e[2]);
            if (e[1] < 0xC0 || e[1] >= 0xE0) t.write(e[3]);
        }
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        byte[] head = { 'M', 'T', 'h', 'd', 0, 0, 0, 6, 0, 1, 0, 1, 1, (byte) 0xF4, 'M', 'T', 'r', 'k', 0, 0, 0, (byte) t.size() };
        o.write(head, 0, head.length);
        o.write(t.toByteArray(), 0, t.size());
        return o.toByteArray();
    }

    private static final int[][] SONG = {
        { 0, 0xC0, 5, 0 }, { 0, 0xB0, 7, 100 },
        { 100, 0x90, 60, 64 }, { 150, 0x80, 60, 0 }, { 50, 0x90, 62, 64 }, { 200, 0x80, 62, 0 }, { 0, 0xFF, 0, 0 } };

    @Test public void queueEventStoresDeltaAndPackedMessageAndFlushSendsThemOnce() {
        Sink sink = new Sink();
        BufferedMidiMusicPlayer p = sub2(sink);
        invokeStatic(BufferedMidiMusicPlayer.class, M_FLUSH);
        assertTrue("empty buffer is not flushed", sink.batches.isEmpty());
        invokeStatic(BufferedMidiMusicPlayer.class, M_QUEUE, 0x90, 150, 60, 64);
        invokeStatic(BufferedMidiMusicPlayer.class, M_QUEUE, 0x80, 400, 61, 3);
        assertTrue("queued only", sink.batches.isEmpty());
        assertEquals(400, get(BufferedMidiMusicPlayer.class, null, F_LAST_TIME));
        assertEquals(4, get(BufferedMidiMusicPlayer.class, null, F_BUF_LEN));
        invokeStatic(BufferedMidiMusicPlayer.class, M_FLUSH);
        assertEquals(1, sink.batches.size());
        assertArrayEquals(new int[] { 150, 0x90 | 60 << 8 | 64 << 16, 250, 0x80 | 61 << 8 | 3 << 16 }, sink.batches.get(0));
        assertEquals(0, get(BufferedMidiMusicPlayer.class, null, F_BUF_LEN));
        invokeStatic(BufferedMidiMusicPlayer.class, M_FLUSH);
        assertEquals(1, sink.batches.size());
        p.sendMessage(0x91, 10, 20, 500L);               // sendMessage queues (status, data1, data2, time)
        invokeStatic(BufferedMidiMusicPlayer.class, M_FLUSH);
        assertArrayEquals(new int[] { 100, 0x91 | 10 << 8 | 20 << 16 }, sink.batches.get(1));
    }

    @Test public void theEventBufferIsFlushedBeforeItOverflowsAt256Integers() {
        Sink sink = new Sink();
        sub2(sink);
        for (int i = 0; i < 128; i++) invokeStatic(BufferedMidiMusicPlayer.class, M_QUEUE, 0x90, i, 1, 2);
        assertTrue(sink.batches.isEmpty());
        invokeStatic(BufferedMidiMusicPlayer.class, M_QUEUE, 0x90, 128, 1, 2);
        assertEquals(1, sink.batches.size());
        assertEquals(256, sink.batches.get(0).length);
        assertEquals(2, get(BufferedMidiMusicPlayer.class, null, F_BUF_LEN));
    }

    @Test public void constructorInitialisesTheSinkResetsAllChannelsAndStartsAPumpThread() throws Exception {
        Sink sink = new Sink();
        resetSub2Statics(sink);
        sink.reading = 100000;
        live = new BufferedMidiMusicPlayer(sink);
        assertEquals(96, sink.lastInitArg);
        assertEquals(1, sink.resets);
        assertEquals(1, sink.batches.size());
        assertEquals(96, decode(sink.batches).size());
        assertArrayEquals(ev(0, 176, 123, 0), decode(sink.batches).get(0));
        assertTrue(pumpThreadAlive());
        BufferedMidiMusicPlayer p = live;
        live = null;
        p.shutdown();                                // shutdown: waits for the thread, then closes the sink
        assertEquals(1, sink.closes);
        assertTrue(sink.closedWith);
        assertEquals(false, get(BufferedMidiMusicPlayer.class, null, F_SHUTDOWN));
        assertFalse(pumpThreadAlive());
    }

    private static boolean pumpThreadAlive() throws Exception {
        for (int attempt = 0; attempt < 20; attempt++) {
            for (Thread t : Thread.getAllStackTraces().keySet())
                for (StackTraceElement e : t.getStackTrace())
                    if (e.getClassName().equals("BufferedMidiMusicPlayer") && e.getMethodName().equals("run")) return true;
            Thread.sleep(25);
        }
        return false;
    }

    @Test public void playQueuesTheStartOfTheSongAtTimeZeroAndStopsAtTheFirstDelay() {
        Sink sink = new Sink();
        BufferedMidiMusicPlayer p = sub2(sink);
        sink.reading = 100000;
        p.play(256, cacheSong(SONG), 0, false);
        assertEquals(1, sink.resets);
        assertEquals(1, sink.batches.size());
        List<long[]> got = decode(sink.batches);
        assertEquals(3, got.size());
        assertArrayEquals(ev(0, 0xC0, 5, 0), got.get(0));
        assertArrayEquals(ev(0, 0xB0, 7, 100), got.get(1));   // the volume controller went through the filter
        assertArrayEquals(ev(0, 0xB0, 39, 0), got.get(2));
        assertEquals(256, Game.musicOutputVolume);
    }

    @Test public void playAppliesVolumeAndTheThirdArgumentAsFadeBeforeTheSong() {
        Sink sink = new Sink();
        BufferedMidiMusicPlayer p = sub2(sink);
        p.play(128, cacheSong(SONG), 0, false);
        List<long[]> got = decode(sink.batches);
        assertEquals(35, got.size());
        assertArrayEquals(ev(0, 176, 7, 70), got.get(0));
        assertArrayEquals(ev(0, 177, 39, 91), got.get(3));
        assertArrayEquals(ev(0, 0xC0, 5, 0), got.get(32));
        assertEquals(128, Game.musicOutputVolume);

        resetVolumeState();
        Sink sink2 = new Sink();
        BufferedMidiMusicPlayer q = sub2(sink2);
        q.play(256, cacheSong(SONG), 2000, false);
        got = decode(sink2.batches);
        assertEquals(35, got.size());
        assertArrayEquals(ev(0, 176, 7, 31), got.get(0));
        assertArrayEquals(ev(0, 176, 39, 111), got.get(1));
        assertEquals(26, Game.musicOutputVolume);
    }

    @Test public void aSongWithNoPlayableEventsLoopsForeverIsRefusedElseItEndsWithAReset() {
        Sink sink = new Sink();
        BufferedMidiMusicPlayer p = sub2(sink);
        byte[] empty = cacheSong(new int[][] { { 0, 0xFF, 0, 0 } });
        try { p.play(256, empty, 0, true); fail("looping an empty song must throw"); }
        catch (RuntimeException expected) { /* guard against an endless loop */ }
        sink = new Sink();
        p = sub2(sink);
        p.play(256, empty, 0, false);
        assertEquals(96, decode(sink.batches).size());
        assertArrayEquals(ev(0, 176, 123, 0), decode(sink.batches).get(0));
        assertFalse("song released", ((Class11) get(BufferedMidiMusicPlayer.class, p, F_READER)).isLoaded());
        sink.reading = -1000000;
        int before = sink.batches.size();
        p.tick(-126);                            // nothing loaded: nothing happens
        assertEquals(before, sink.batches.size());
    }

    private static List<String> strs(List<long[]> events) {
        List<String> out = new ArrayList<String>();
        for (long[] e : events) out.add(Arrays.toString(e));
        return out;
    }

    private static List<String> strs(long[]... events) { return strs(Arrays.asList(events)); }

    private static void tick(BufferedMidiMusicPlayer p, Sink sink, int reading) {
        sink.reading = reading;
        p.tick(-126);
    }

    @Test public void tickQueuesEventsDueWithinTheWindowOfTwoHundredMillisecondsPastTheLastQueuedEventMinusTheSinkReading() {
        Sink sink = new Sink();
        BufferedMidiMusicPlayer p = sub2(sink);
        sink.reading = 100000;
        p.play(256, cacheSong(SONG), 0, false);
        sink.batches.clear();
        tick(p, sink, 101);                           // window ends at 0 + 200 - 101 = 99; the first note is at 100
        assertTrue(sink.batches.isEmpty());
        tick(p, sink, 100);                           // window ends exactly at 100
        assertEquals(1, sink.batches.size());
        assertEquals(strs(ev(100, 0x90, 60, 64)), strs(decode(sink.batches)));
        tick(p, sink, 100);                           // window is now 100 + 200 - 100 = 200; the next event is at 250
        assertEquals(1, sink.batches.size());
        tick(p, sink, 51);                            // 100 + 149 = 249 < 250
        assertEquals(1, sink.batches.size());
        tick(p, sink, 50);                            // window 250: the note off at 250 only, the window is not re-evaluated mid tick
        assertEquals(2, sink.batches.size());
        assertEquals(strs(ev(150, 0x80, 60, 0)), strs(decode(sink.batches.subList(1, 2))));
        tick(p, sink, 50);                            // window 250 + 200 - 50 = 400: the note on at 300
        assertEquals(3, sink.batches.size());
        assertEquals(strs(ev(50, 0x90, 62, 64)), strs(decode(sink.batches.subList(2, 3))));
        tick(p, sink, 1);                             // 300 + 199 = 499 < 500
        assertEquals(3, sink.batches.size());
        tick(p, sink, 0);                             // 500: last note off, end of song, channel reset
        assertEquals(4, sink.batches.size());
        List<long[]> end = decode(sink.batches.subList(3, 4));
        assertEquals(97, end.size());
        assertArrayEquals(ev(200, 0x80, 62, 0), end.get(0));
        assertArrayEquals(ev(0, 176, 123, 0), end.get(1));
        assertFalse("song released", ((Class11) get(BufferedMidiMusicPlayer.class, p, F_READER)).isLoaded());
        tick(p, sink, -100000);
        assertEquals(4, sink.batches.size());
    }

    @Test public void aLoopingSongRestartsAtTheEndInsteadOfResetting() {
        Sink sink = new Sink();
        BufferedMidiMusicPlayer p = sub2(sink);
        sink.reading = 100000;
        p.play(256, cacheSong(SONG), 0, true);
        sink.batches.clear();
        tick(p, sink, 100);                           // note on at 100
        tick(p, sink, 50);                            // note off at 250
        tick(p, sink, 50);                            // note on at 300
        tick(p, sink, 0);                             // note off at 500 and end of the song: restart, replaying the start at 500
        List<long[]> got = decode(sink.batches.subList(3, 4));
        assertEquals(strs(ev(200, 0x80, 62, 0), ev(0, 0xC0, 5, 0), ev(0, 0xB0, 7, 100), ev(0, 0xB0, 39, 0)), strs(got));
        assertTrue("song stays loaded", ((Class11) get(BufferedMidiMusicPlayer.class, p, F_READER)).isLoaded());
        tick(p, sink, 100);                           // the restarted song continues on the same timeline: note on at 500 + 100
        assertEquals(strs(ev(100, 0x90, 60, 64)), strs(decode(sink.batches.subList(4, 5))));
        assertEquals(true, get(BufferedMidiMusicPlayer.class, null, F_LOOP));
    }
    @Test public void setVolumeFlushesAtOnceButFadeOnlyQueues() {
        Sink sink = new Sink();
        BufferedMidiMusicPlayer p = sub2(sink);
        set(BufferedMidiMusicPlayer.class, null, F_LAST_TIME, 70);
        p.fade(128, 0);
        assertTrue("fade is queued, not flushed", sink.batches.isEmpty());
        assertEquals(128, Game.musicOutputVolume);
        p.setVolume(256);
        assertEquals(1, sink.batches.size());
        assertEquals(64, decode(sink.batches).size());     // the 32 queued by fade plus the 32 from the volume change
        assertArrayEquals(ev(0, 176, 7, 70), decode(sink.batches).get(0));
        assertArrayEquals(ev(0, 176, 7, 100), decode(sink.batches).get(32));
        assertEquals(256, Game.musicOutputVolume);
        assertEquals(0, get(BufferedMidiMusicPlayer.class, null, F_BUF_LEN));
    }

    @Test public void stopResetsTheSinkQueuesAResetAndReleasesTheSong() {
        Sink sink = new Sink();
        BufferedMidiMusicPlayer p = sub2(sink);
        sink.reading = 100000;
        p.play(256, cacheSong(SONG), 0, false);
        int resetsBefore = sink.resets, batchesBefore = sink.batches.size();
        p.stop();
        assertEquals(resetsBefore + 1, sink.resets);
        assertEquals(batchesBefore + 1, sink.batches.size());
        assertEquals(96, decode(sink.batches.subList(batchesBefore, batchesBefore + 1)).size());
        assertFalse(((Class11) get(BufferedMidiMusicPlayer.class, p, F_READER)).isLoaded());
        sink.reading = -1000000;
        p.tick(-126);
        assertEquals("a stopped song queues nothing more", batchesBefore + 1, sink.batches.size());
    }

    @Test public void dispatchNextEventQueuesChannelMessagesScaledToMillisecondsAndEndsTracksOnEndOfTrack() {
        Sink sink = new Sink();
        BufferedMidiMusicPlayer p = sub2(sink);
        sink.reading = 100000;
        p.play(256, cacheSong(SONG), 0, false);
        sink.batches.clear();
        Class11 reader = (Class11) get(BufferedMidiMusicPlayer.class, p, F_READER);
        reader.restoreTrackPosition(0);                          // rewind track 0 to the saved position (before the note on)
        invoke(p, M_DISPATCH, 0, 123456789L, 0);      // 123456789 / (500 * 1000) = 246 ms
        invokeStatic(BufferedMidiMusicPlayer.class, M_FLUSH);
        assertArrayEquals(ev(246, 0x90, 60, 64), decode(sink.batches).get(0));
    }

    @Test public void sub2SleepsThroughTickGuardValuesBelowMinusNinety() {
        Sink sink = new Sink();
        BufferedMidiMusicPlayer p = sub2(sink);
        sink.reading = 100000;
        p.play(256, cacheSong(SONG), 0, false);
        p.tick(-90);
        assertSame(sink, get(BufferedMidiMusicPlayer.class, null, F_SINK));
        assertTrue("song still loaded", ((Class11) get(BufferedMidiMusicPlayer.class, p, F_READER)).isLoaded());
        p.tick(-89);                             // the guard value that nulls the sink
        assertNull(get(BufferedMidiMusicPlayer.class, null, F_SINK));
    }
}
