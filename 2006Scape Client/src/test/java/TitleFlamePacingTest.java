import static org.junit.Assert.*;

import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.objectweb.asm.*;

/** Observe original drawFlames local slots with controlled clock/sleep, without editing production. */
public class TitleFlamePacingTest {
    private static Object instance;
    private static long[] times;
    private static int clockReads, stopAfter, updates, draws;
    private static boolean interruptSleep, failDraw;
    private static final List<Long> sleeps = new ArrayList<>();
    private static final Map<Integer, List<Long>> stores = new HashMap<>();

    public static long now() { return times[clockReads++]; }
    public static void store(long value, int slot) {
        if (!stores.containsKey(slot)) stores.put(slot, new ArrayList<Long>());
        stores.get(slot).add(value);
    }
    public static void pause(long value) throws Exception {
        assertTrue(instance.getClass().getField("drawingFlames").getBoolean(instance));
        sleeps.add(value);
        if (sleeps.size() == stopAfter) instance.getClass().getField("flameThreadActive").setBoolean(instance, false);
        if (interruptSleep) throw new InterruptedException("controlled interruption");
    }
    public static void update() { updates++; }
    public static void draw() {
        draws++;
        assertEquals(draws * 2, updates);
        if (failDraw) throw new IllegalStateException("controlled draw failure");
    }

    private static byte[] bytes(String name) throws Exception {
        try (InputStream in = TitleFlamePacingTest.class.getResourceAsStream("/" + name + ".class")) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192]; int count;
            while ((count = in.read(buffer)) >= 0) out.write(buffer, 0, count);
            return out.toByteArray();
        }
    }

    private static Object fixture() throws Exception {
        ClassLoader loader = new ClassLoader(TitleFlamePacingTest.class.getClassLoader()) {
            @Override protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (name.contains(".") || name.equals("TitleFlamePacingTest")) return super.loadClass(name, resolve);
                Class<?> c = findLoadedClass(name);
                if (c == null) {
                    try {
                        byte[] b = bytes(name);
                        if (name.equals("Game")) {
                            ClassWriter w = new ClassWriter(ClassWriter.COMPUTE_MAXS);
                            new ClassReader(b).accept(new ClassVisitor(Opcodes.ASM9, w) {
                                @Override public MethodVisitor visitMethod(int a, String n, String d, String s, String[] e) {
                                    MethodVisitor mv = super.visitMethod(a, n, d, s, e);
                                    if (!n.equals("drawFlames") || !d.equals("()V")) return mv;
                                    return new MethodVisitor(Opcodes.ASM9, mv) {
                                        @Override public void visitMethodInsn(int op, String o, String n, String d, boolean itf) {
                                            if (o.equals("java/lang/System") && n.equals("currentTimeMillis")) {
                                                super.visitMethodInsn(Opcodes.INVOKESTATIC, "TitleFlamePacingTest", "now", d, false);
                                            } else if (o.equals("java/lang/Thread") && n.equals("sleep")) {
                                                super.visitMethodInsn(Opcodes.INVOKESTATIC, "TitleFlamePacingTest", "pause", d, false);
                                            } else if (o.equals("Game") && (n.equals("calcFlamesPosition") || n.equals("doFlamesDrawing"))) {
                                                super.visitInsn(Opcodes.POP);
                                                super.visitMethodInsn(Opcodes.INVOKESTATIC, "TitleFlamePacingTest", n.equals("calcFlamesPosition") ? "update" : "draw", "()V", false);
                                            } else super.visitMethodInsn(op, o, n, d, itf);
                                        }
                                        @Override public void visitVarInsn(int op, int slot) {
                                            if (op == Opcodes.ISTORE || op == Opcodes.LSTORE) {
                                                super.visitInsn(op == Opcodes.ISTORE ? Opcodes.DUP : Opcodes.DUP2);
                                                if (op == Opcodes.ISTORE) super.visitInsn(Opcodes.I2L);
                                                super.visitLdcInsn(slot);
                                                super.visitMethodInsn(Opcodes.INVOKESTATIC, "TitleFlamePacingTest", "store", "(JI)V", false);
                                            }
                                            super.visitVarInsn(op, slot);
                                        }
                                        @Override public void visitIincInsn(int slot, int amount) {
                                            super.visitIincInsn(slot, amount);
                                            super.visitVarInsn(Opcodes.ILOAD, slot);
                                            super.visitInsn(Opcodes.I2L);
                                            super.visitLdcInsn(slot);
                                            super.visitMethodInsn(Opcodes.INVOKESTATIC, "TitleFlamePacingTest", "store", "(JI)V", false);
                                        }
                                    };
                                }
                            }, 0);
                            b = w.toByteArray();
                        }
                        c = defineClass(name, b, 0, b.length);
                    } catch (Exception ex) { throw new ClassNotFoundException(name, ex); }
                }
                if (resolve) resolveClass(c);
                return c;
            }
        };
        return loader.loadClass("Game").newInstance();
    }

    private void run(int count, boolean interrupted, boolean brokenDraw, long... clock) throws Exception {
        sleeps.clear(); stores.clear(); updates = draws = clockReads = 0;
        times = clock; stopAfter = count; interruptSleep = interrupted; failDraw = brokenDraw;
        instance = fixture();
        instance.getClass().getField("flameThreadActive").setBoolean(instance, count != 0);
        instance.getClass().getMethod("drawFlames").invoke(instance);
        assertFalse(instance.getClass().getField("drawingFlames").getBoolean(instance));
        assertEquals(draws, instance.getClass().getField("flameFrameCount").getInt(instance));
    }

    @Test public void tenFramesUseTwentyMillisecondSleepWithoutAnotherClockRead() throws Exception {
        run(10, false, false, 1000);
        assertEquals(1, clockReads);
        assertEquals(Arrays.asList(1000L), stores.get(1)); // original l
        assertEquals(Arrays.asList(20L), stores.get(4)); // original j
        assertEquals(Arrays.asList(0L,1L,2L,3L,4L,5L,6L,7L,8L,9L,10L), stores.get(3)); // original i
        assertEquals(10, sleeps.size());
        for (long v : sleeps) assertEquals(20, v);
        assertEquals(20, updates);
    }

    @Test public void eleventhFrameSamplesElapsedMillisecondsResetsCountAndClampsSleep() throws Exception {
        run(23, false, false, 1000, 1239, 2009);
        assertEquals(3, clockReads);
        assertEquals(Arrays.asList(1000L,1239L,2009L), stores.get(1)); // l
        assertEquals(Arrays.asList(1239L,2009L), stores.get(5)); // l1
        assertEquals(Arrays.asList(3L,40L), stores.get(7)); // k: truncated elapsed/10 minus old j
        assertEquals(Arrays.asList(20L,37L,0L,5L), stores.get(4)); // j including clamp
        assertEquals(11L, (long) stores.get(3).get(11));
        assertEquals(0L, (long) stores.get(3).get(12));
        assertEquals(20L, (long) sleeps.get(9));
        assertEquals(37L, (long) sleeps.get(10));
        assertEquals(37L, (long) sleeps.get(20));
        assertEquals(5L, (long) sleeps.get(21));
        assertEquals(5L, (long) sleeps.get(22));
    }

    @Test public void fiveMillisecondBoundaryIsNotClampedAndFasterWindowRaisesSleep() throws Exception {
        run(22, false, false, 1000, 1550, 1550);
        assertEquals(Arrays.asList(35L,-5L), stores.get(7));
        assertEquals(Arrays.asList(20L,5L,45L), stores.get(4));
        assertEquals(45L, (long) sleeps.get(21));
    }

    @Test public void stoppedLoopAndExceptionsKeepExistingLifecycle() throws Exception {
        run(0, false, false, 42);
        assertEquals(0, draws); assertTrue(sleeps.isEmpty());
        run(2, true, false, 42);
        assertEquals(2, draws); assertEquals(2, sleeps.size());
        run(2, false, true, 42);
        assertEquals(1, draws); assertTrue(sleeps.isEmpty());
    }
}
