import static org.junit.Assert.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.junit.Test;

/**
 * Pass 175: semantic tests (written against the original names) for the applet main loop (RSApplet.run and its
 * state), the varp config record (Varp) and the varbit config record (VarBit).
 */
public class AppletVarpVarBitTest {
    // ---- names under test, in one place ----
    private static final String RUN_STATE = "runState", TIMESTAMPS = "frameTimes", SAVED_CLICK_TIME = "saveClickTime",
        SET_RATE = "setFrameRate", DELAY_TIME = "delayTime", CLICK_TIME = "clickTime";
    private static final String VARP_ACTION = "settingAction", VARBIT_VARP = "varpIndex", VARBIT_LOW = "lowBit", VARBIT_HIGH = "highBit";

    private static Field field(Class<?> c, String n) throws Exception { Field f = c.getDeclaredField(n); f.setAccessible(true); return f; }
    private static int geti(Object o, String n) throws Exception { return field(RSApplet.class, n).getInt(o); }
    private static long[] timestamps(RSApplet a) throws Exception { return ((long[]) field(RSApplet.class, TIMESTAMPS).get(a)).clone(); }
    private static void setRate(RSApplet a, int v) throws Exception {
        Method m = RSApplet.class.getDeclaredMethod(SET_RATE, int.class); m.setAccessible(true); m.invoke(a, v);
    }

    /** Applet whose game loop is driven by a script; run() is called on the test thread. */
    static class LoopApplet extends RSApplet {
        interface Tick { void tick(LoopApplet a, int tickNumber) throws Exception; }
        interface Frame { void frame(LoopApplet a, int frameNumber) throws Exception; }
        Tick tick; Frame frame; int ticks, frames, cleanups; long frameSleep;
        Throwable failure;
        @Override void startUp() {}
        @Override void drawLoadingText(int i, String s) {}
        @Override void cleanUpForQuit() { cleanups++; }
        @Override void processGameLoop() {
            ticks++;
            try { if (tick != null) tick.tick(this, ticks); } catch (Throwable t) { failure = t; destroy(); }
        }
        @Override void processDrawing() {
            frames++;
            try {
                if (frame != null) frame.frame(this, frames);
                if (frameSleep > 0) Thread.sleep(frameSleep);
            } catch (Throwable t) { failure = t; destroy(); }
        }
    }
    private static void runApplet(LoopApplet a) throws Exception {
        a.run();
        if (a.failure != null) throw new AssertionError(a.failure);
    }

    @Test public void setRateStoresMillisecondsPerFrame() throws Exception {
        LoopApplet a = new LoopApplet();
        assertEquals(20, geti(a, DELAY_TIME));
        setRate(a, 50); assertEquals(20, geti(a, DELAY_TIME));
        setRate(a, 10); assertEquals(100, geti(a, DELAY_TIME));
        setRate(a, 3); assertEquals("integer division", 333, geti(a, DELAY_TIME));
        setRate(a, 1); assertEquals(1000, geti(a, DELAY_TIME));
    }

    @Test public void runStateIsShutdownGraceCountdown() throws Exception {
        LoopApplet a = new LoopApplet();
        final int[] seen = new int[8];
        a.tick = new LoopApplet.Tick() { public void tick(LoopApplet ap, int n) throws Exception {
            Field f = field(RSApplet.class, RUN_STATE);
            if (n == 1) { seen[0] = f.getInt(ap); ap.stop(); seen[1] = f.getInt(ap); }
            if (n == 2) { seen[2] = f.getInt(ap); }
            if (n == 3) { ap.start(); seen[3] = f.getInt(ap); }
            if (n == 5) { seen[4] = f.getInt(ap); ap.destroy(); seen[5] = f.getInt(ap); }
        } };
        runApplet(a);
        assertEquals("running", 0, seen[0]);
        assertEquals("stop() starts a 4000 ms grace period counted in frames", 4000 / 20, seen[1]);
        assertEquals("counts down once per frame", 4000 / 20 - 1, seen[2]);
        assertEquals("start() cancels the countdown", 0, seen[3]);
        assertEquals("a cancelled countdown stays at zero", 0, seen[4]);
        assertEquals(1, a.cleanups);
        assertEquals("exited", -2, geti(a, RUN_STATE));
        assertEquals("destroy() ended the loop in its frame", 5, a.frames);
    }

    @Test public void graceCountdownExpiryShutsDown() throws Exception {
        LoopApplet a = new LoopApplet();
        setRate(a, 2); // 500 ms per frame: stop() grace = 8 frames
        a.tick = new LoopApplet.Tick() { public void tick(LoopApplet ap, int n) { if (n == 1) ap.stop(); } };
        runApplet(a);
        assertEquals(1, a.cleanups);
        assertEquals(-2, geti(a, RUN_STATE));
        assertEquals("exit happens when the 8 counted frames have elapsed", 8, a.frames);
    }

    @Test public void timestampRingKeepsTenLatestFrameTimesAndDebugDumpShowsLoopLocals() throws Exception {
        LoopApplet a = new LoopApplet();
        final long start = System.currentTimeMillis();
        final long[][] ring = new long[3][];
        final String[] dump = new String[1];
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        final PrintStream oldOut = System.out;
        a.tick = new LoopApplet.Tick() { public void tick(LoopApplet ap, int n) throws Exception {
            if (n == 1) ring[0] = timestamps(ap);
            if (n == 3) Thread.currentThread().interrupt(); // next frame's sleep is interrupted
            if (n == 4) { ap.shouldDebug = true; System.setOut(new PrintStream(bytes, true)); }
            if (n == 5) { System.setOut(oldOut); dump[0] = bytes.toString(); }
        } };
        a.frame = new LoopApplet.Frame() { public void frame(LoopApplet ap, int n) throws Exception {
            if (n == 4) ring[2] = timestamps(ap);
            if (n == 14) ring[1] = timestamps(ap);
            if (n == 15) ap.destroy();
        } };
        try { runApplet(a); } finally { System.setOut(oldOut); }
        long[] first = ring[0];
        assertEquals(10, first.length);
        for (int i = 2; i < 10; i++) assertEquals("untouched slots still hold the start-up time", first[1], first[i]);
        assertTrue("slot 0 already holds the first frame's time", first[0] >= first[1]);
        assertTrue(first[1] > 0);
        long[] later = ring[1];
        // after 14 completed frames the next slot to overwrite is 14 % 10 = 4: the oldest entry; slot 3 is the newest
        for (int k = 0; k < 9; k++) assertTrue("oldest to newest is non-decreasing at " + k, later[(4 + k) % 10] <= later[(4 + k + 1) % 10]);
        assertTrue("entries are real clock times", later[3] >= start && later[3] <= System.currentTimeMillis() + 10000);
        assertTrue("the newest frame is later than the oldest", later[3] > later[4]);
        assertNotNull(dump[0]);
        String[] lines = dump[0].split("\\r?\\n");
        assertTrue(dump[0], lines[0].startsWith("ntime:"));
        long now = Long.parseLong(lines[0].substring(6).trim());
        assertTrue(now >= start && now <= System.currentTimeMillis());
        for (int age = 0; age < 10; age++) {
            int slot = (4 - age - 1 + 20) % 10;
            assertEquals("old timestamps listed newest first by ring slot", "otim" + slot + ":" + ring[2][slot], lines[1 + age].trim());
        }
        assertTrue(lines[11], lines[11].startsWith("fps:"));
        java.util.regex.Matcher fm = java.util.regex.Pattern.compile("fps:(\\d+) ratio:(\\d+) count:(\\d+)").matcher(lines[11]);
        assertTrue(lines[11], fm.matches());
        int ratio = Integer.parseInt(fm.group(2)), count = Integer.parseInt(fm.group(3));
        assertTrue("speed ratio is clamped to 25..256 and is full speed for an on-time loop: " + ratio, ratio >= 200 && ratio <= 256);
        assertTrue("tick accumulator stays below 256: " + count, count >= 0 && count < 256);
        int del = Integer.parseInt(lines[12].trim().substring(4, lines[12].trim().indexOf(' ')));
        assertTrue("sleep time between minDelay and delayTime: " + del, del >= 1 && del <= 20);
        assertTrue(lines[12], lines[12].startsWith("del:") && lines[12].contains("deltime:20 mindel:1"));
        assertEquals("one interrupted sleep so far, ring cursor at frame 3 % 10 + 1 = 4", "intex:1 opos:4", lines[13].trim());
    }

    private static double[] runWithFrameCost(long extraMillis, int frames) throws Exception {
        LoopApplet a = new LoopApplet();
        a.frameSleep = extraMillis;
        final int[] tickAtFrame = new int[frames + 1];
        final int[] fpsAtEnd = new int[1];
        a.frame = new LoopApplet.Frame() { public void frame(LoopApplet ap, int n) {
            tickAtFrame[n] = ap.ticks;
            if (n == tickAtFrame.length - 1) { fpsAtEnd[0] = ap.fps; ap.destroy(); }
        } };
        runApplet(a);
        int from = frames / 2;
        double ticksPerFrame = (tickAtFrame[frames] - tickAtFrame[from]) / (double) (frames - from);
        return new double[] {fpsAtEnd[0], ticksPerFrame};
    }

    @Test public void fastLoopRunsAtTargetRateWithOneTickPerFrame() throws Exception {
        double[] r = runWithFrameCost(0, 40);
        assertTrue("fps " + r[0], r[0] >= 42 && r[0] <= 50);
        assertEquals(1.0, r[1], 0.2);
    }

    @Test public void slowLoopLowersReportedRateAndCatchesUpWithExtraTicks() throws Exception {
        double[] r = runWithFrameCost(60, 30); // each frame costs ~80 ms instead of 20 ms
        assertTrue("fps " + r[0], r[0] >= 9 && r[0] <= 15);
        assertTrue("ticks per frame " + r[1], r[1] >= 3.0 && r[1] <= 7.0);
    }

    @Test public void savedClickTimeIsLatestMousePressTimeCopiedWithTheClickSnapshot() throws Exception {
        LoopApplet a = new LoopApplet();
        field(RSApplet.class, CLICK_TIME).setLong(a, 12345L);
        field(RSApplet.class, "clickX").setInt(a, 77);
        field(RSApplet.class, "clickMode1").setInt(a, 1);
        final long[] times = new long[4]; final int[] modes = new int[4], xs = new int[4];
        a.tick = new LoopApplet.Tick() { public void tick(LoopApplet ap, int n) throws Exception {
            if (n <= 3) { times[n] = field(RSApplet.class, SAVED_CLICK_TIME).getLong(ap); modes[n] = ap.clickMode3; xs[n] = ap.saveClickX; }
            if (n == 1) field(RSApplet.class, CLICK_TIME).setLong(ap, 999L);
            if (n == 3) ap.destroy();
        } };
        runApplet(a);
        assertEquals(12345L, times[1]); assertEquals(1, modes[1]); assertEquals(77, xs[1]);
        assertEquals("time follows the press time even when no new click", 999L, times[2]);
        assertEquals("no click this tick", 0, modes[2]);
    }

    // ---- Varp / VarBit ----
    private static Object newRecord(Class<?> c) throws Exception {
        Constructor<?> k = c.getDeclaredConstructor(); k.setAccessible(true); return k.newInstance();
    }
    private static void readValues(Object rec, byte[] data, Integer index) throws Exception {
        Stream s = new Stream(data);
        Method m = index == null ? rec.getClass().getDeclaredMethod("readValues", Stream.class)
                                 : rec.getClass().getDeclaredMethod("readValues", Stream.class, int.class);
        m.setAccessible(true);
        if (index == null) m.invoke(rec, s); else m.invoke(rec, s, index);
        assertEquals("all bytes consumed", data.length, s.currentOffset);
    }

    @Test public void varpOpcodeFiveReadsBigEndianShortIntoActionField() throws Exception {
        Object v = newRecord(Varp.class);
        assertEquals(0, field(Varp.class, VARP_ACTION).getInt(v));
        readValues(v, new byte[] {5, 0x01, 0x02, 0}, 0);
        assertEquals(0x0102, field(Varp.class, VARP_ACTION).getInt(v));
        Object w = newRecord(Varp.class); // other opcodes (1,2: byte; 4,6,13: none; 7,12: int) leave it unset
        readValues(w, new byte[] {1, 9, 2, 8, 4, 6, 13, 7, 1, 2, 3, 4, 12, 1, 2, 3, 4, 0}, 0);
        assertEquals(0, field(Varp.class, VARP_ACTION).getInt(w));
    }

    @Test public void varpActionFieldSelectsClientReactionToSettingChange() throws Exception {
        Varp[] saved = Varp.cache;
        try {
            Game g = new Game(); g.variousSettings = new int[4];
            Varp[] cache = new Varp[4];
            for (int i = 0; i < 4; i++) cache[i] = (Varp) newRecord(Varp.class);
            field(Varp.class, VARP_ACTION).setInt(cache[1], 1); // action 1 = brightness: raises the welcome screen
            field(Varp.class, VARP_ACTION).setInt(cache[2], 99); // unknown action: nothing happens
            Varp.cache = cache;
            g.variousSettings[0] = 1; g.variousSettings[1] = 1; g.variousSettings[2] = 1;
            g.welcomeScreenRaised = false; g.applyVarpSetting(0); assertFalse("action 0 does nothing", g.welcomeScreenRaised);
            g.applyVarpSetting(2); assertFalse("unknown action does nothing", g.welcomeScreenRaised);
            g.applyVarpSetting(1); assertTrue("action 1 reacts", g.welcomeScreenRaised);
        } finally { Varp.cache = saved; }
    }

    @Test public void varbitOpcodeOneReadsVarpIndexThenLowBitThenHighBit() throws Exception {
        Object b = newRecord(VarBit.class);
        readValues(b, new byte[] {1, 0x01, 0x07, 3, 9, 0}, null);
        assertEquals(0x0107, field(VarBit.class, VARBIT_VARP).getInt(b));
        assertEquals(3, field(VarBit.class, VARBIT_LOW).getInt(b));
        assertEquals(9, field(VarBit.class, VARBIT_HIGH).getInt(b));
        Object c = newRecord(VarBit.class);
        readValues(c, new byte[] {1, 0, 2, (byte) 200, (byte) 255, 0}, null);
        assertEquals(200, field(VarBit.class, VARBIT_LOW).getInt(c));
        assertEquals("unsigned bytes", 255, field(VarBit.class, VARBIT_HIGH).getInt(c));
    }
}
