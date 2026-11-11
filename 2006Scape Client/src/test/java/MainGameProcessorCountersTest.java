import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import static org.junit.Assert.*;

/**
 * Original-name observations for the counters and flags of Game.mainGameProcessor (mouseSampleRepeatCount, lastMouseSampleX, lastMouseSampleY, lastReportedClickTime,
 * focusReportedToServer, cyclesSinceLastSend) and the bytecode-slot values of its obfuscated locals. Game is loaded through a private class loader that
 * (1) replaces every call from mainGameProcessor to another Game method by a recording hook and (2) records every store to a local slot.
 * Slots (javac, original bytecode): 1 = loop index j / monitor / click time l (long) / pickedTileX k / loop index i1; 2 = j2, k1;
 * 3 = j3, k2, flag; 4 = j4, k3; 5 = l4, k4; 6 = k5, j5; 7 = i6, l5; 8 = j6; 9 = k6.
 */
public class MainGameProcessorCountersTest {
    static final List<String> events = new ArrayList<String>();
    static int parseTrue;
    static boolean walkResult;
    static final List<Integer> walkArgs = new ArrayList<Integer>();
    static Runnable beforeProcessRightClick;
    static int dropCalls;

    public static boolean call(Object game, String name) {
        events.add("M:" + name);
        if (name.equals("parsePacket")) return parseTrue-- > 0;
        if (name.equals("dropClient")) dropCalls++;
        return false;
    }
    public static boolean walk(Object game, int a, int b, int c, int d, int e, int f, int g, int h, int i, boolean j, int k) {
        events.add("M:doWalkTo");
        walkArgs.clear();
        walkArgs.addAll(Arrays.asList(a, b, c, d, e, f, g, h, i, j ? 1 : 0, k));
        return walkResult;
    }
    public static void sI(long v, int slot) { events.add("I" + slot + "=" + v); }
    public static void sL(long v, int slot) { events.add("L" + slot + "=" + v); }
    public static void sA(int slot) { events.add("A" + slot); }
    public static void sC(long v, int slot) { events.add("C" + slot + "=" + v); }

    private static byte[] bytes(String name) throws Exception {
        try (InputStream in = MainGameProcessorCountersTest.class.getResourceAsStream("/" + name + ".class")) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192]; int count;
            while ((count = in.read(buffer)) >= 0) out.write(buffer, 0, count);
            return out.toByteArray();
        }
    }

    static final String ME = "MainGameProcessorCountersTest";
    static final ClassLoader LOADER = new ClassLoader(MainGameProcessorCountersTest.class.getClassLoader()) {
        @Override protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (name.contains(".") || name.startsWith(ME)) return super.loadClass(name, resolve);
            synchronized (this) {
                Class<?> c = findLoadedClass(name);
                if (c == null) {
                    try {
                        byte[] b = bytes(name);
                        if (name.equals("Game")) b = instrument(b);
                        c = defineClass(name, b, 0, b.length);
                    } catch (Exception ex) { throw new ClassNotFoundException(name, ex); }
                }
                if (resolve) resolveClass(c);
                return c;
            }
        }
    };

    private static byte[] instrument(byte[] b) {
        ClassWriter w = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        new ClassReader(b).accept(new ClassVisitor(Opcodes.ASM9, w) {
            @Override public MethodVisitor visitMethod(int a, String n, String d, String s, String[] e) {
                MethodVisitor mv = super.visitMethod(a, n, d, s, e);
                if (!n.equals("mainGameProcessor") || !d.equals("()V")) return mv;
                return new MethodVisitor(Opcodes.ASM9, mv) {
                    @Override public void visitMethodInsn(int op, String owner, String name, String desc, boolean itf) {
                        if (op != Opcodes.INVOKEVIRTUAL || !owner.equals("Game")) { super.visitMethodInsn(op, owner, name, desc, itf); return; }
                        if (name.equals("doWalkTo")) {
                            super.visitMethodInsn(Opcodes.INVOKESTATIC, ME, "walk", "(Ljava/lang/Object;" + desc.substring(1), false);
                            return;
                        }
                        Type[] args = Type.getArgumentTypes(desc);
                        for (int i = args.length - 1; i >= 0; i--) super.visitInsn(args[i].getSize() == 2 ? Opcodes.POP2 : Opcodes.POP);
                        super.visitLdcInsn(name);
                        super.visitMethodInsn(Opcodes.INVOKESTATIC, ME, "call", "(Ljava/lang/Object;Ljava/lang/String;)Z", false);
                        Type r = Type.getReturnType(desc);
                        if (r.getSort() == Type.VOID) super.visitInsn(Opcodes.POP);
                        else if (r.getSort() != Type.BOOLEAN) throw new IllegalStateException("unexpected return type from " + name + desc);
                    }
                    @Override public void visitVarInsn(int op, int slot) {
                        if (op == Opcodes.ISTORE) {
                            super.visitInsn(Opcodes.DUP); super.visitInsn(Opcodes.I2L); super.visitLdcInsn(slot);
                            super.visitMethodInsn(Opcodes.INVOKESTATIC, ME, "sI", "(JI)V", false);
                        } else if (op == Opcodes.LSTORE) {
                            super.visitInsn(Opcodes.DUP2); super.visitLdcInsn(slot);
                            super.visitMethodInsn(Opcodes.INVOKESTATIC, ME, "sL", "(JI)V", false);
                        } else if (op == Opcodes.ASTORE) {
                            super.visitLdcInsn(slot);
                            super.visitMethodInsn(Opcodes.INVOKESTATIC, ME, "sA", "(I)V", false);
                        }
                        super.visitVarInsn(op, slot);
                    }
                    @Override public void visitIincInsn(int slot, int amount) {
                        super.visitIincInsn(slot, amount);
                        super.visitVarInsn(Opcodes.ILOAD, slot); super.visitInsn(Opcodes.I2L); super.visitLdcInsn(slot);
                        super.visitMethodInsn(Opcodes.INVOKESTATIC, ME, "sC", "(JI)V", false);
                    }
                };
            }
        }, 0);
        return w.toByteArray();
    }

    // ---- reflection helpers -------------------------------------------------------------------------------------------------
    static Class<?> cls(String n) throws Exception { return LOADER.loadClass(n); }
    static Field fld(Class<?> c, String n) throws Exception {
        for (; c != null; c = c.getSuperclass()) {
            try { Field f = c.getDeclaredField(n); f.setAccessible(true); return f; } catch (NoSuchFieldException ex) { }
        }
        throw new NoSuchFieldException(n);
    }
    static int gi(Object o, String n) throws Exception { return fld(o.getClass(), n).getInt(o); }
    static long gl(Object o, String n) throws Exception { return fld(o.getClass(), n).getLong(o); }
    static boolean gb(Object o, String n) throws Exception { return fld(o.getClass(), n).getBoolean(o); }
    static void si(Object o, String n, int v) throws Exception { fld(o.getClass(), n).setInt(o, v); }
    static void sl(Object o, String n, long v) throws Exception { fld(o.getClass(), n).setLong(o, v); }
    static void sb(Object o, String n, boolean v) throws Exception { fld(o.getClass(), n).setBoolean(o, v); }
    static Object go(Object o, String n) throws Exception { return fld(o.getClass(), n).get(o); }
    static Object make(String cn, Class<?>[] sig, Object... args) throws Exception {
        Constructor<?> c = cls(cn).getDeclaredConstructor(sig); c.setAccessible(true); return c.newInstance(args);
    }

    // ---- fixture ------------------------------------------------------------------------------------------------------------
    Object game, stream, mouse;
    ISAACRandomGen decoder;
    ServerSocket listener; Socket client, server;

    @Before public void before() throws Exception {
        events.clear(); parseTrue = 0; walkResult = false; walkArgs.clear(); dropCalls = 0;
        Object g = cls("Game").newInstance();
        game = g;
        stream = make("Stream", new Class<?>[]{byte[].class}, new byte[5000]);
        fld(stream.getClass(), "encryption").set(stream, make("ISAACRandomGen", new Class<?>[]{int[].class}, new int[]{1, 2, 3, 4}));
        decoder = new ISAACRandomGen(new int[]{1, 2, 3, 4});
        mouse = make("MouseDetection", new Class<?>[]{cls("Game")}, g);
        fld(g.getClass(), "stream").set(g, stream);
        fld(g.getClass(), "mouseDetection").set(g, mouse);
        sb(g, "loggedIn", true);
        sb(g, "awtFocus", true);
        sb(g, "focusReportedToServer", true);
        sb(g, "flagged", true);
        si(g, "cyclesSinceLastSend", 0);
        si(g, "mouseX", 0); si(g, "mouseY", 0);
        si(cls("WorldController"), "pickedTileX", -1);
    }
    @After public void after() throws Exception {
        if (client != null) client.close();
        if (server != null) server.close();
        if (listener != null) listener.close();
    }
    static void si(Class<?> c, String n, int v) throws Exception { fld(c, n).setInt(null, v); }

    private void cycle() throws Exception {
        events.clear();
        try { game.getClass().getMethod("mainGameProcessor").invoke(game); }
        catch (InvocationTargetException e) { throw new AssertionError("mainGameProcessor threw " + e.getCause(), e.getCause()); }
    }
    private byte[] pending() throws Exception {
        int n = gi(stream, "currentOffset");
        byte[] b = (byte[]) go(stream, "buffer");
        return Arrays.copyOf(b, n);
    }
    private void clearPending() throws Exception { si(stream, "currentOffset", 0); }

    /** One decoded client packet. */
    static class P {
        final int opcode; final byte[] payload;
        P(int o, byte[] p) { opcode = o; payload = p; }
        long value() { long v = 0; for (byte x : payload) v = v << 8 | (x & 255); return v; }
        @Override public String toString() { return opcode + ":" + hex(payload); }
    }
    static String hex(byte[] b) { StringBuilder s = new StringBuilder(); for (byte x : b) s.append(String.format("%02x", x & 255)); return s.toString(); }
    List<P> decode(byte[] b) {
        List<P> out = new ArrayList<P>();
        int i = 0;
        while (i < b.length) {
            int opcode = (b[i++] - decoder.getNextKey()) & 255;
            int len;
            switch (opcode) {
                case 0: len = 0; break;
                case 3: len = 1; break;
                case 241: len = 4; break;
                case 45: len = b[i++] & 255; break;
                default: throw new AssertionError("unexpected opcode " + opcode);
            }
            out.add(new P(opcode, Arrays.copyOfRange(b, i, i + len)));
            i += len;
        }
        return out;
    }
    /** Runs one cycle with no socket and returns the packets it left in the stream. */
    List<P> run() throws Exception { cycle(); List<P> p = decode(pending()); clearPending(); return p; }

    static P only(List<P> ps, int opcode) {
        P found = null;
        for (P p : ps) if (p.opcode == opcode) { assertNull("duplicate opcode " + opcode + " in " + ps, found); found = p; }
        return found;
    }

    /** Puts the given samples into the sampler and requests the send with a left click (the click packet is ignored here). */
    List<P> sendSamples(int[]... pts) throws Exception {
        int[] xs = (int[]) go(mouse, "sampledMouseXs"), ys = (int[]) go(mouse, "sampledMouseYs");
        for (int i = 0; i < pts.length; i++) { xs[i] = pts[i][0]; ys[i] = pts[i][1]; }
        si(mouse, "sampleCount", pts.length);
        si(game, "clickMode3", 1);
        si(game, "saveClickX", 0); si(game, "saveClickY", 0);
        sl(game, "saveClickTime", gl(game, "lastReportedClickTime"));
        return run();
    }
    private void setLast(int count, int x, int y) throws Exception { si(game, "mouseSampleRepeatCount", count); si(game, "lastMouseSampleX", x); si(game, "lastMouseSampleY", y); }
    private void assertLast(int count, int x, int y) throws Exception {
        assertEquals("mouseSampleRepeatCount", count, gi(game, "mouseSampleRepeatCount"));
        assertEquals("lastMouseSampleX", x, gi(game, "lastMouseSampleX"));
        assertEquals("lastMouseSampleY", y, gi(game, "lastMouseSampleY"));
    }
    static long shortForm(int count, int dx, int dy) { return ((long) count << 12) + ((dx + 32) << 6) + (dy + 32); }

    // ---- mouse sample packet (opcode 45) -------------------------------------------------------------------------------------
    @Test public void unflaggedSessionNeverSendsSamplesAndDropsThem() throws Exception {
        sb(game, "flagged", false);
        int[] xs = (int[]) go(mouse, "sampledMouseXs"); xs[0] = 5;
        si(mouse, "sampleCount", 60);
        setLast(4, 7, 8);
        List<P> ps = run();
        assertNull(only(ps, 45));
        assertEquals(0, gi(mouse, "sampleCount"));
        assertLast(4, 7, 8);
    }

    @Test public void sendsOnlyWithFortySamplesOrAClick() throws Exception {
        si(mouse, "sampleCount", 39);
        assertNull(only(run(), 45));
        assertEquals(39, gi(mouse, "sampleCount"));
        si(mouse, "sampleCount", 40);
        P p = only(run(), 45);
        assertNotNull(p);
        assertEquals(0, gi(mouse, "sampleCount"));
    }

    @Test public void identicalSamplesOnlyCountUpAndWriteNoPayload() throws Exception {
        setLast(0, 100, 50);
        P p = only(sendSamples(new int[]{100, 50}, new int[]{100, 50}, new int[]{100, 50}), 45);
        assertEquals(0, p.payload.length);
        assertLast(3, 100, 50);
        assertEquals(0, gi(mouse, "sampleCount"));
    }

    @Test public void repeatCountSaturatesAt2047() throws Exception {
        setLast(2046, 100, 50);
        sendSamples(new int[]{100, 50}, new int[]{100, 50}, new int[]{100, 50});
        assertLast(2047, 100, 50);
    }

    @Test public void shortFormCarriesRepeatCountThenXDeltaThenYDelta() throws Exception {
        setLast(3, 100, 50);
        P p = only(sendSamples(new int[]{110, 60}), 45);
        assertEquals(2, p.payload.length);
        assertEquals(shortForm(3, 10, 10), p.value());
        assertLast(0, 110, 60);
        setLast(0, 100, 50);
        p = only(sendSamples(new int[]{105, 80}), 45);   // dx = 5, dy = 30 differ, so the field order is established
        assertEquals(shortForm(0, 5, 30), p.value());
        assertEquals((0 << 12) + (37 << 6) + 62, p.value());
        assertLast(0, 105, 80);
        setLast(0, 100, 50);
        p = only(sendSamples(new int[]{70, 40}), 45);    // negative deltas: dx = -30, dy = -10
        assertEquals(shortForm(0, -30, -10), p.value());
    }

    @Test public void shortFormDeltaBoundaries() throws Exception {
        setLast(7, 100, 100);
        assertEquals(shortForm(7, 31, -32), only(sendSamples(new int[]{131, 68}), 45).value());      // largest count 7, dx = 31, dy = -32
        assertEquals(2, only(sendSamples(new int[]{99, 68 + 31}), 45).payload.length);                 // dx = -32, dy = 31
        setLast(0, 100, 100);
        P p = only(sendSamples(new int[]{132, 100}), 45);                                              // dx = 32 no longer fits
        assertEquals(3, p.payload.length);
        setLast(0, 100, 100);
        assertEquals(3, only(sendSamples(new int[]{67, 100}), 45).payload.length);                     // dx = -33
        setLast(0, 100, 100);
        assertEquals(3, only(sendSamples(new int[]{100, 132}), 45).payload.length);                    // dy = 32
        setLast(0, 100, 100);
        assertEquals(3, only(sendSamples(new int[]{100, 67}), 45).payload.length);                     // dy = -33
    }

    @Test public void mediumFormCarriesRepeatCountAndPackedPosition() throws Exception {
        setLast(5, 100, 100);
        P p = only(sendSamples(new int[]{300, 200}), 45);
        assertEquals(3, p.payload.length);
        assertEquals(0x800000L + (5L << 19) + (200 * 765 + 300), p.value());
        assertLast(0, 300, 200);
    }

    @Test public void longFormWhenRepeatCountIsEightOrMore() throws Exception {
        setLast(8, 100, 100);
        P p = only(sendSamples(new int[]{101, 101}), 45);   // tiny delta, but the count is 8
        assertEquals(4, p.payload.length);
        assertEquals(0xc0000000L + (8L << 19) + (101 * 765 + 101), p.value());
        assertLast(0, 101, 101);
        setLast(7, 100, 100);
        assertEquals(2, only(sendSamples(new int[]{101, 101}), 45).payload.length);    // count 7 still uses the short form
        setLast(2047, 3, 4);
        p = only(sendSamples(new int[]{10, 20}), 45);
        assertEquals(0xc0000000L + (2047L << 19) + (20 * 765 + 10), p.value());
    }

    @Test public void samplesAreClampedToTheCanvasBeforeComparingAndPacking() throws Exception {
        setLast(0, 0, 0);
        P p = only(sendSamples(new int[]{900, 700}), 45);
        assertEquals(0x800000L + (502 * 765 + 764), p.value());
        assertLast(0, 764, 502);
        setLast(0, 0, 0);
        p = only(sendSamples(new int[]{-5, -9}), 45);   // clamps to (0,0): identical to the last sample, so nothing is written
        assertEquals(0, p.payload.length);
        assertLast(1, 0, 0);
        setLast(0, 0, 0);
        p = only(sendSamples(new int[]{764, 502}), 45);
        assertEquals(0x800000L + (502 * 765 + 764), p.value());
        setLast(0, 764, 502);
        p = only(sendSamples(new int[]{765, 503}), 45);   // one past the edge clamps back onto the last sample
        assertEquals(0, p.payload.length);
        assertLast(1, 764, 502);
    }

    @Test public void minusOneMinusOneIsTheMouseLeftSentinel() throws Exception {
        setLast(2, 30, 40);
        P p = only(sendSamples(new int[]{-1, -1}), 45);
        assertEquals(0x800000L + (2L << 19) + 0x7ffff, p.value());
        assertLast(0, -1, -1);
        p = only(sendSamples(new int[]{-1, -1}), 45);     // same sentinel again: counts up, nothing written
        assertEquals(0, p.payload.length);
        assertLast(1, -1, -1);
        setLast(0, -1, -1);
        p = only(sendSamples(new int[]{-1, 50}), 45);     // only both -1 make the sentinel: x clamps to 0
        assertEquals(0x800000L + (0L << 19) + (50 * 765 + 0), p.value());
        assertLast(0, 0, 50);
    }

    @Test public void severalSamplesShareOnePacketAndTheLengthByteCountsOnlyThePayload() throws Exception {
        setLast(0, 100, 50);
        si(stream, "currentOffset", 5);                    // earlier packet bytes already queued this cycle
        byte[] b = (byte[]) go(stream, "buffer"); Arrays.fill(b, 0, 5, (byte) 0x55);
        int[] xs = (int[]) go(mouse, "sampledMouseXs"), ys = (int[]) go(mouse, "sampledMouseYs");
        int[][] pts = {{100, 50}, {100, 50}, {103, 52}, {500, 400}};
        for (int i = 0; i < pts.length; i++) { xs[i] = pts[i][0]; ys[i] = pts[i][1]; }
        si(mouse, "sampleCount", 4);
        si(game, "clickMode3", 1); si(game, "saveClickX", 0); si(game, "saveClickY", 0); sl(game, "saveClickTime", gl(game, "lastReportedClickTime"));   // the click makes the send happen
        events.clear();
        game.getClass().getMethod("mainGameProcessor").invoke(game);
        byte[] out = pending();
        for (int i = 0; i < 5; i++) assertEquals((byte) 0x55, out[i]);
        List<P> ps = decode(Arrays.copyOfRange(out, 5, out.length));   // the filler is not a packet; the first encrypted opcode is at offset 5
        P p = only(ps, 45);
        assertEquals(5, out[6] & 255);                     // length byte: short (2) + medium (3) payload bytes, not counting the 5 filler bytes
        assertEquals(5, p.payload.length);
        assertEquals(shortForm(2, 3, 2), (p.payload[0] & 255) << 8 | (p.payload[1] & 255));
        assertEquals(0x800000L + (0L << 19) + (400 * 765 + 500), (p.payload[2] & 255) << 16 | (p.payload[3] & 255) << 8 | (p.payload[4] & 255));
        assertEquals(0, gi(mouse, "sampleCount"));
        assertLast(0, 500, 400);
    }

    // ---- click packet (opcode 241) -------------------------------------------------------------------------------------------
    private P click(int mode, int x, int y, long time) throws Exception {
        si(game, "clickMode3", mode); si(game, "saveClickX", x); si(game, "saveClickY", y); sl(game, "saveClickTime", time);
        return only(run(), 241);
    }
    @Test public void clickDelayIsMeasuredInFiftyMillisecondUnitsFromThePreviousClick() throws Exception {
        sl(game, "lastReportedClickTime", 1000);
        assertEquals(7L << 20, click(1, 0, 0, 1000 + 50 * 7 + 49).value());
        assertEquals(1000 + 50 * 7 + 49, gl(game, "lastReportedClickTime"));
        assertEquals(8L << 20, click(1, 0, 0, 1000 + 50 * 7 + 49 + 400).value());
        sl(game, "lastReportedClickTime", 1000);
        assertEquals(0L, click(1, 0, 0, 1049).value());
        sl(game, "lastReportedClickTime", 1000);
        assertEquals(1L << 20, click(1, 0, 0, 1050).value());
    }
    @Test public void clickDelayIsCappedAt4095AndFirstClickAfterResetIsCapped() throws Exception {
        sl(game, "lastReportedClickTime", 1000);
        assertEquals(4095L << 20, click(1, 0, 0, 1000 + 50 * 4095 + 49).value());
        sl(game, "lastReportedClickTime", 1000);
        assertEquals(4095L << 20, click(1, 0, 0, 1000 + 50 * 4096).value());
        sl(game, "lastReportedClickTime", 0);
        assertEquals(4095L << 20, click(1, 0, 0, 1700000000000L).value());
        assertEquals(1700000000000L, gl(game, "lastReportedClickTime"));
    }
    @Test public void rightClickSetsBit19AndPositionIsClampedAndPacked() throws Exception {
        sl(game, "lastReportedClickTime", 5000);
        assertEquals((2L << 20) + (0L << 19) + (30 * 765 + 20), click(1, 20, 30, 5100).value());
        sl(game, "lastReportedClickTime", 5000);
        assertEquals((2L << 20) + (1L << 19) + (30 * 765 + 20), click(2, 20, 30, 5100).value());
        sl(game, "lastReportedClickTime", 5000);
        assertEquals((0L << 20) + (1L << 19) + (502 * 765 + 764), click(2, 900, 700, 5000).value());
        sl(game, "lastReportedClickTime", 5000);
        assertEquals((0L << 20) + (0L << 19) + 0, click(1, -4, -6, 5000).value());
        sl(game, "lastReportedClickTime", 5000);
        assertEquals((502 * 765 + 764) + 0L, click(1, 764, 502, 5000).value());
        sl(game, "lastReportedClickTime", 5000);
        assertEquals(502 * 765 + 764L, click(1, 765, 503, 5000).value());   // one past the edge is clamped
    }
    @Test public void noClickMeansNoPacketAndTheClickTimeIsUntouched() throws Exception {
        sl(game, "lastReportedClickTime", 4242);
        assertNull(click(0, 10, 10, 99999));
        assertEquals(4242, gl(game, "lastReportedClickTime"));
    }

    // ---- focus packet (opcode 3) -------------------------------------------------------------------------------------------
    @Test public void focusChangeSendsOnePacketAndRemembersIt() throws Exception {
        sb(game, "awtFocus", false);
        P p = only(run(), 3);
        assertArrayEquals(new byte[]{0}, p.payload);
        assertFalse(gb(game, "focusReportedToServer"));
        assertNull(only(run(), 3));                       // still unfocused: nothing more is sent
        assertNull(only(run(), 3));
        assertFalse(gb(game, "focusReportedToServer"));
        sb(game, "awtFocus", true);
        p = only(run(), 3);
        assertArrayEquals(new byte[]{1}, p.payload);
        assertTrue(gb(game, "focusReportedToServer"));
        assertNull(only(run(), 3));
    }
    @Test public void focusFlagIsComparedWithTheFlagNotWithAPacketHistory() throws Exception {
        sb(game, "awtFocus", true); sb(game, "focusReportedToServer", false);   // the flag alone decides: regain is reported
        assertArrayEquals(new byte[]{1}, only(run(), 3).payload);
        sb(game, "awtFocus", false); sb(game, "focusReportedToServer", false);   // equal: silent
        assertNull(only(run(), 3));
        sb(game, "awtFocus", true); sb(game, "focusReportedToServer", true);
        assertNull(only(run(), 3));
    }

    // ---- idle / flush counter (cyclesSinceLastSend, opcode 0) ------------------------------------------------------------------------------
    @Test public void quietCyclesCountUpAndOpcodeZeroIsWrittenOnlyAboveFifty() throws Exception {
        si(game, "cyclesSinceLastSend", 0);
        for (int i = 1; i <= 50; i++) {
            List<P> ps = run();
            assertEquals(i, gi(game, "cyclesSinceLastSend"));      // no socket: nothing is flushed, so the counter keeps counting
            assertTrue("no packet at count " + i + ": " + ps, ps.isEmpty());
        }
        List<P> ps = run();                               // 51st quiet cycle
        assertEquals(51, gi(game, "cyclesSinceLastSend"));
        assertEquals(1, ps.size());
        assertEquals(0, ps.get(0).opcode);
        assertEquals(1, run().size());                    // keeps being written while it stays above 50
        assertEquals(52, gi(game, "cyclesSinceLastSend"));
    }

    private void connect() throws Exception {
        listener = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"));
        client = new Socket(InetAddress.getByName("127.0.0.1"), listener.getLocalPort());
        server = listener.accept();
        server.setSoTimeout(5000);
        fld(game.getClass(), "socketStream").set(game, make("RSSocket", new Class<?>[]{cls("RSApplet"), Socket.class}, game, client));
    }
    private byte[] received(int n) throws Exception {
        byte[] b = new byte[n]; int got = 0;
        while (got < n) { int r = server.getInputStream().read(b, got, n - got); if (r < 0) fail("closed"); got += r; }
        return b;
    }

    @Test public void anyFlushedPacketResetsTheCounterAndKeepAliveIsASmallPacket() throws Exception {
        connect();
        si(game, "cyclesSinceLastSend", 10);
        cycle();                                          // quiet: nothing queued, nothing sent, counter just counts
        assertEquals(11, gi(game, "cyclesSinceLastSend"));
        assertEquals(0, gi(stream, "currentOffset"));
        sb(game, "awtFocus", false);                       // any packet resets the counter to 0 when flushed
        cycle();
        assertEquals(0, gi(game, "cyclesSinceLastSend"));
        assertEquals(0, gi(stream, "currentOffset"));
        List<P> ps = decode(received(2));
        assertEquals(1, ps.size()); assertEquals(3, ps.get(0).opcode);
        si(game, "cyclesSinceLastSend", 49);
        cycle();                                          // 50: not above 50
        assertEquals(50, gi(game, "cyclesSinceLastSend"));
        cycle();                                          // 51: opcode 0 written, flushed, counter back to 0
        assertEquals(0, gi(game, "cyclesSinceLastSend"));
        ps = decode(received(1));
        assertEquals(1, ps.size()); assertEquals(0, ps.get(0).opcode);
        cycle();
        assertEquals(1, gi(game, "cyclesSinceLastSend"));
    }

    // ---- locals: values recorded from the original bytecode slots ----------------------------------------------------------
    private List<Long> window(String startEvent, String endEvent, String... prefixes) {
        int s = startEvent == null ? 0 : events.indexOf(startEvent);
        int e = endEvent == null ? events.size() : events.indexOf(endEvent);
        if (e < 0) e = events.size();
        assertTrue(startEvent + " missing in " + events, s >= 0);
        List<Long> out = new ArrayList<Long>();
        for (int i = s; i < e; i++) for (String p : prefixes) if (events.get(i).startsWith(p)) out.add(Long.parseLong(events.get(i).substring(p.length())));
        return out;
    }
    private int firstIndex(String prefix) { for (int i = 0; i < events.size(); i++) if (events.get(i).startsWith(prefix)) return i; return -1; }

    @Test public void parseLoopSlot1RunsHundredTimesAtMostAndStopsWhenNoPacketRemains() throws Exception {
        parseTrue = 1000;
        cycle();
        assertEquals(100, count("M:parsePacket"));
        assertEquals(longs(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50,
                51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100),
                window(null, "A1", "I1=", "C1="));
        parseTrue = 3;
        cycle();
        assertEquals(4, count("M:parsePacket"));
        assertEquals(longs(0, 1, 2, 3), window(null, "A1", "I1=", "C1="));
    }
    private int count(String ev) { int n = 0; for (String s : events) if (s.equals(ev)) n++; return n; }
    private static List<Long> longs(long... v) { List<Long> l = new ArrayList<Long>(); for (long x : v) l.add(x); return l; }

    @Test public void sampleLocalsHoldPayloadStartCountIndexClampedPositionPackedAndDeltas() throws Exception {
        setLast(2, 100, 50);
        si(stream, "currentOffset", 6);                    // so the payload start (slot 2) is not just 1
        int[] xs = (int[]) go(mouse, "sampledMouseXs"), ys = (int[]) go(mouse, "sampledMouseYs");
        int[][] pts = {{100, 50}, {110, 60}, {900, -3}, {-1, -1}};
        for (int i = 0; i < pts.length; i++) { xs[i] = pts[i][0]; ys[i] = pts[i][1]; }
        si(mouse, "sampleCount", 4); si(game, "clickMode3", 1); si(game, "saveClickX", 0); si(game, "saveClickY", 0); sl(game, "saveClickTime", gl(game, "lastReportedClickTime"));
        cycle();
        List<String> sync = events.subList(events.indexOf("A1"), firstIndex("L1="));
        assertEquals("[I2=8]", filter(sync, "I2=").toString());                                   // j2: opcode at 6, length byte at 7, payload from 8
        assertEquals("[C3=1, C3=2, C3=3, C3=4]", filter(sync, "C3=").toString());                  // j3: samples encoded so far
        assertEquals("[I4=0, C4=1, C4=2, C4=3, C4=4]", filter(sync, "I4=", "C4=").toString());     // j4: sample index
        assertEquals("[I5=50, I5=60, I5=-3, I5=0, I5=-1, I5=0, I5=-1]", filter(sync, "I5=").toString());   // l4: y, clamped y, (-1) sentinel
        assertEquals("[I6=100, I6=110, I6=900, I6=764, I6=-1, I6=0, I6=-1]", filter(sync, "I6=").toString()); // k5: x, clamped x
        assertEquals("[I7=" + (50 * 765 + 100) + ", I7=" + (60 * 765 + 110) + ", I7=764, I7=0, I7=524287]", filter(sync, "I7=").toString());   // i6
        assertEquals("[I8=10, I8=654, I8=-765]", filter(sync, "I8=").toString());                  // j6: x - last x
        assertEquals("[C8=42]", filter(sync, "C8=").toString());                                   // +32 bias only for the short form
        assertEquals("[I9=10, I9=-60, I9=-1]", filter(sync, "I9=").toString());                    // k6: y - last y
        assertEquals("[C9=42]", filter(sync, "C9=").toString());
    }

    @Test public void clickLocalsHoldDelayTicksClampedPositionPackedAndRightClickFlag() throws Exception {
        sl(game, "lastReportedClickTime", 1000);
        si(game, "clickMode3", 2); si(game, "saveClickX", 900); si(game, "saveClickY", -5); sl(game, "saveClickTime", 1000 + 50 * 5000);
        cycle();
        List<String> click = events.subList(firstIndex("L1="), firstIndex("M:loadingStages"));
        assertEquals("[L1=5000, L1=4095]", filter(click, "L1=").toString());     // l: ticks, then capped
        assertEquals("[I3=-5, I3=0]", filter(click, "I3=").toString());          // k2: y then clamped
        assertEquals("[I4=900, I4=764]", filter(click, "I4=").toString());       // k3: x then clamped
        assertEquals("[I5=764]", filter(click, "I5=").toString());               // k4: y * 765 + x
        assertEquals("[I6=0, I6=1]", filter(click, "I6=").toString());           // j5: right-click flag
        assertEquals("[I7=4095]", filter(click, "I7=").toString());              // l5: (int) l
        sl(game, "lastReportedClickTime", 1000);
        si(game, "clickMode3", 1); si(game, "saveClickX", 20); si(game, "saveClickY", 30); sl(game, "saveClickTime", 1000 + 50 * 3 + 10);
        cycle();
        click = events.subList(firstIndex("L1="), firstIndex("M:loadingStages"));
        assertEquals("[L1=3]", filter(click, "L1=").toString());
        assertEquals("[I3=30]", filter(click, "I3=").toString());
        assertEquals("[I4=20]", filter(click, "I4=").toString());
        assertEquals("[I5=" + (30 * 765 + 20) + "]", filter(click, "I5=").toString());
        assertEquals("[I6=0]", filter(click, "I6=").toString());
        assertEquals("[I7=3]", filter(click, "I7=").toString());
    }

    private void setPlayerAt(int smallX, int smallY) throws Exception {
        Object player = make("Player", new Class<?>[0]);
        ((int[]) go(player, "smallX"))[0] = smallX; ((int[]) go(player, "smallY"))[0] = smallY;
        fld(game.getClass(), "myPlayer").set(game, player);
    }
    private static int pickedX() throws Exception { return fld(cls("WorldController"), "pickedTileX").getInt(null); }

    @Test public void pickedTileIsWalkedToAndTheCrossMarkerFollowsTheWalkResult() throws Exception {
        setPlayerAt(33, 44);
        si(cls("WorldController"), "pickedTileX", 11);
        si(cls("WorldController"), "pickedTileY", 22);
        si(game, "saveClickX", 321); si(game, "saveClickY", 123);
        si(game, "crossType", 0); si(game, "crossIndex", 77);
        walkResult = true;
        cycle();
        int walk = events.indexOf("M:doWalkTo");
        List<String> before = events.subList(events.indexOf("M:loadingStages"), walk);
        assertEquals("[I1=11]", filter(before, "I1=").toString());      // k
        assertEquals("[I2=22]", filter(before, "I2=").toString());      // k1
        assertEquals("I3=1", filter(events.subList(walk, events.size()), "I3=").get(0));   // flag
        assertEquals(Arrays.asList(0, 0, 0, 0, 44, 0, 0, 22, 33, 1, 11), walkArgs);          // targetY = k1, targetX = k
        assertEquals(-1, pickedX());
        assertEquals(1, gi(game, "crossType")); assertEquals(321, gi(game, "crossX")); assertEquals(123, gi(game, "crossY"));
        assertEquals(0, gi(game, "crossIndex"));

        si(cls("WorldController"), "pickedTileX", 5);
        si(cls("WorldController"), "pickedTileY", 6);
        si(game, "crossType", 0); si(game, "crossX", 9); si(game, "saveClickX", 400);
        walkResult = false;
        cycle();
        walk = events.indexOf("M:doWalkTo");
        assertEquals("I3=0", filter(events.subList(walk, events.size()), "I3=").get(0));
        assertEquals(0, gi(game, "crossType")); assertEquals(9, gi(game, "crossX"));
        assertEquals(-1, pickedX());
        assertEquals(Arrays.asList(0, 0, 0, 0, 44, 0, 0, 6, 33, 1, 5), walkArgs);
    }

    @Test public void shakePhaseLoopIndexCoversExactlyTheFiveChannelsOncePerCycle() throws Exception {
        int[] phase = (int[]) go(game, "shakePhase");
        for (int i = 0; i < 5; i++) phase[i] = 10 * (i + 1);
        cycle();
        assertArrayEquals(new int[]{11, 21, 31, 41, 51}, phase);
        int end = events.indexOf("M:processKeyboardInput"), start = end;
        while (!events.get(--start).startsWith("M:")) { }
        assertEquals("[I1=0, C1=1, C1=2, C1=3, C1=4, C1=5]", filter(events.subList(start, end), "I1=", "C1=").toString());
    }

    @Test public void partialConsumeBranchIsNeverReachedAndLongPayloadsOverflowTheLengthByte() throws Exception {
        setLast(0, 0, 0);
        int[] xs = (int[]) go(mouse, "sampledMouseXs"), ys = (int[]) go(mouse, "sampledMouseYs");
        for (int i = 0; i < 100; i++) { xs[i] = i % 2 == 0 ? 600 : 100; ys[i] = i % 2 == 0 ? 400 : 100; }   // 100 medium-form samples = 300 payload bytes
        si(mouse, "sampleCount", 100);
        si(game, "clickMode3", 1); si(game, "saveClickX", 0); si(game, "saveClickY", 0); sl(game, "saveClickTime", gl(game, "lastReportedClickTime"));
        cycle();
        byte[] out = pending();
        assertEquals(300 & 255, out[1] & 255);                                  // length byte wrapped: the 240-byte stop never fired
        assertEquals(1 + 1 + 300 + 1 + 4, out.length);                          // opcode, length byte, all 300 payload bytes, then the click packet
        assertEquals(0, gi(mouse, "sampleCount"));
        List<String> sync = events.subList(events.indexOf("A1"), firstIndex("L1="));
        assertEquals(100, filter(sync, "C3=").size());                          // all 100 samples were encoded in one pass
        assertEquals(1, filter(sync, "I4=").size());                            // slot 4 is only ever the sample index: the i5 copy loop (also slot 4) never ran
    }

    private static List<String> filter(List<String> l, String... prefixes) {
        List<String> out = new ArrayList<String>();
        for (String s : l) for (String p : prefixes) if (s.startsWith(p)) out.add(s);
        return out;
    }
}
