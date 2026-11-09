import java.awt.Canvas;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.Test;
import static org.junit.Assert.*;

/** Semantic tests for RSApplet frame setup, frame pacing, loading bar, focus loss and the typed-character queue. */
public class RSAppletCoreTest {
    private static final int RED = 0x8c1111;

    /** RSApplet whose game component is a plain canvas and whose run loop is scripted by processDrawing. */
    public static final class Probe extends RSApplet {
        final Canvas canvas = new Canvas();
        int iteration;
        boolean scriptedRun;
        @Override Component getGameComponent() { return canvas; }
        @Override void drawLoadingText(int a, String b) { if (!scriptedRun) super.drawLoadingText(a, b); }
        @Override public void startRunnable(Runnable runnable, int priority) { }

        @Override void processDrawing() {
            iteration++;
            shouldDebug = true;
            try {
                long[] times = (long[]) get(this, "frameTimes");
                long now = System.currentTimeMillis();
                if (iteration == 1) Arrays.fill(times, now - 1000L);
                if (iteration == 2) Arrays.fill(times, 0L);
                if (iteration == 3) Arrays.fill(times, now - 100L);
                if (iteration == 4) Arrays.fill(times, 0L);
                if (iteration == 5) set(this, "runState", -1);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static Probe probe() throws Exception {
        Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        Probe p = (Probe) ((sun.misc.Unsafe) unsafeField.get(null)).allocateInstance(Probe.class);
        set(p, "canvas", new Canvas());
        set(p, "delayTime", 20);
        set(p, "minDelay", 1);
        set(p, "frameTimes", new long[10]);
        set(p, "keyArray", new int[128]);
        set(p, "charQueue", new int[128]);
        return p;
    }

    private static Field field(Object target, String name) throws Exception {
        for (Class<?> c = target.getClass(); c != null; c = c.getSuperclass()) {
            try {
                Field f = c.getDeclaredField(name);
                f.setAccessible(true);
                Field mods = Field.class.getDeclaredField("modifiers");
                mods.setAccessible(true);
                mods.setInt(f, f.getModifiers() & ~Modifier.FINAL);
                return f;
            } catch (NoSuchFieldException ignored) {
            }
        }
        throw new NoSuchFieldException(name);
    }

    static Object get(Object target, String name) throws Exception { return field(target, name).get(target); }
    static void set(Object target, String name, Object value) throws Exception { field(target, name).set(target, value); }

    // ---- setFrameRate(i): delayTime = 1000 / i milliseconds per frame ----

    @Test public void setFrameRateArgumentIsFramesPerSecond() throws Exception {
        Probe p = probe();
        int[][] cases = {{50, 20}, {1, 1000}, {3, 333}, {7, 142}};
        for (int[] c : cases) {
            p.setFrameRate(c[0]);
            assertEquals("delay for " + c[0] + " fps", c[1], get(p, "delayTime"));
        }
    }

    // ---- initClientFrame(i, j): i is the height, j the width ----

    @Test public void initClientFrameTakesHeightThenWidth() throws Exception {
        Map<Field, Object> saved = new HashMap<Field, Object>();
        for (Field f : DrawingArea.class.getDeclaredFields()) {
            if (Modifier.isStatic(f.getModifiers()) && !Modifier.isFinal(f.getModifiers())) {
                f.setAccessible(true);
                saved.put(f, f.get(null));
            }
        }
        try {
            Probe p = probe();
            p.initClientFrame(300, 400);
            assertEquals(400, p.myWidth);
            assertEquals(300, p.myHeight);
            assertEquals(400, get(p.fullGameScreen, "width"));
            assertEquals(300, get(p.fullGameScreen, "height"));
            assertEquals(400 * 300, p.fullGameScreen.pixels.length);
        } finally {
            for (Map.Entry<Field, Object> e : saved.entrySet()) {
                e.getKey().set(null, e.getValue());
            }
        }
    }

    // ---- run(): i2 / j2 keep the previous iteration's speed ratio and sleep time ----

    @Test(timeout = 20000) public void runRestoresPreviousRatioAndSleepWhenFrameSlotIsEmpty() throws Exception {
        Probe p = probe();
        p.scriptedRun = true;
        PrintStream original = System.out;
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        System.setOut(new PrintStream(bytes, true));
        try {
            p.run();
        } finally {
            System.setOut(original);
        }
        String out = bytes.toString();
        List<Integer> ratios = new ArrayList<Integer>();
        List<Integer> sleeps = new ArrayList<Integer>();
        Matcher r = Pattern.compile("ratio:(-?\\d+)").matcher(out);
        while (r.find()) ratios.add(Integer.parseInt(r.group(1)));
        Matcher d = Pattern.compile("del:(-?\\d+) ").matcher(out);
        while (d.find()) sleeps.add(Integer.parseInt(d.group(1)));
        assertEquals(out, 5, ratios.size());
        assertEquals(out, 5, sleeps.size());
        // iteration 2: slot one second old -> ratio well below the 256 cap, sleep 1
        assertTrue(out, ratios.get(1) >= 25 && ratios.get(1) < 200);
        // iteration 3: slot empty -> ratio and sleep restored from iteration 2 (default would be 300 -> 256)
        assertEquals(out, ratios.get(1), ratios.get(2));
        assertEquals(out, sleeps.get(1), sleeps.get(2));
        // iteration 4: slot 100 ms old -> ratio capped at 256 and sleep about 10 ms (not the default 1)
        assertEquals(out, 256, (int) ratios.get(3));
        assertTrue(out, sleeps.get(3) > 1 && sleeps.get(3) <= 10);
        // iteration 5: slot empty -> previous sleep restored (default path would compute sleep 1 via minDelay)
        assertEquals(out, ratios.get(3), ratios.get(4));
        assertEquals(out, sleeps.get(3), sleeps.get(4));
    }

    // ---- readChar: j spins forever for a non-negative argument; k is the dequeued char or -1 ----

    @SuppressWarnings("deprecation")
    @Test public void readCharSpinsForeverForNonNegativeArgument() throws Exception {
        final Probe p = probe();
        Thread t = new Thread(new Runnable() {
            @Override public void run() { p.readChar(0); }
        });
        t.setDaemon(true);
        t.start();
        t.join(400);
        try {
            assertTrue("readChar(0) must keep spinning", t.isAlive());
        } finally {
            t.stop();
        }
        assertEquals(-1, p.readChar(-796));
        assertEquals(-1, p.readChar(-1));
    }

    @Test public void readCharReturnsQueuedCharsInOrderThenMinusOne() throws Exception {
        Probe p = probe();
        p.handleKeyPressed(65, 'a', false, false);
        p.handleKeyPressed(66, 'b', false, false);
        p.handleKeyPressed(10, 10, false, false);
        p.handleKeyPressed(38, 0, false, false); // up arrow -> char 3, pressed but not queued
        assertEquals('a', p.readChar(-796));
        assertEquals('b', p.readChar(-796));
        assertEquals(10, p.readChar(-796));
        assertEquals(-1, p.readChar(-796));
        assertEquals(-1, p.readChar(-796));
    }

    @Test public void readCharWrapsQueueIndexAt128() throws Exception {
        Probe p = probe();
        int[] queue = (int[]) get(p, "charQueue");
        queue[127] = 1005;
        queue[0] = 1006;
        set(p, "readIndex", 127);
        set(p, "writeIndex", 1);
        assertEquals(1005, p.readChar(-796));
        assertEquals(0, get(p, "readIndex"));
        assertEquals(1006, p.readChar(-796));
        assertEquals(-1, p.readChar(-796));
        assertEquals(1, get(p, "readIndex"));
    }

    // ---- focusLost: loop index walks every key slot ----

    @Test public void focusLostClearsEveryKeySlot() throws Exception {
        Probe p = probe();
        Arrays.fill(p.keyArray, 1);
        p.awtFocus = true;
        p.focusLost(null);
        assertFalse(p.awtFocus);
        for (int keyChar = 0; keyChar < 128; keyChar++) {
            assertEquals("slot " + keyChar, 0, p.keyArray[keyChar]);
        }
    }

    // ---- drawLoadingText(i, s): i percent of a 300 px bar, s centred text, j the bar's top y ----

    private static BufferedImage draw(int width, int height, int percent, String text) throws Exception {
        Probe p = probe();
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        p.graphics = g;
        p.myWidth = width;
        p.myHeight = height;
        set(p, "shouldClearScreen", true);
        p.drawLoadingText(percent, text);
        g.dispose();
        return img;
    }

    private static int rgb(BufferedImage img, int x, int y) { return img.getRGB(x, y) & 0xffffff; }

    @Test public void loadingBarFillIsThreePixelsPerPercent() throws Exception {
        int[][] cases = {{50, 150}, {10, 30}, {100, 300}, {1, 3}};
        for (int[] c : cases) {
            BufferedImage img = draw(765, 503, c[0], "");
            int left = 765 / 2 - 150;
            int y = 503 / 2 - 18 + 3;
            assertEquals("last filled px at " + c[0] + "%", RED, rgb(img, left + c[1] - 1, y));
            if (c[1] < 300) {
                assertEquals("first empty px at " + c[0] + "%", 0, rgb(img, left + c[1], y));
            }
        }
    }

    @Test public void loadingBarTopFollowsHalfHeightMinus18() throws Exception {
        int[] heights = {503, 403, 300};
        for (int h : heights) {
            BufferedImage img = draw(765, h, 0, "");
            int barY = h / 2 - 18;
            int x = 765 / 2 - 152;
            assertEquals("outline at " + barY, RED, rgb(img, x + 10, barY));
            assertEquals("above outline", 0, rgb(img, x + 10, barY - 1));
            assertEquals("bottom outline", RED, rgb(img, x + 10, barY + 34));
            assertEquals("below outline", 0, rgb(img, x + 10, barY + 35));
        }
    }

    private static int[] whiteSpan(BufferedImage img, int barY) {
        int min = Integer.MAX_VALUE, max = -1;
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = barY; y <= barY + 34; y++) {
                int c = rgb(img, x, y);
                if (((c >> 16) & 0xff) > 200 && ((c >> 8) & 0xff) > 200 && (c & 0xff) > 200) {
                    min = Math.min(min, x);
                    max = Math.max(max, x);
                }
            }
        }
        return new int[] {min, max};
    }

    @Test public void loadingMessageIsDrawnCentredInTheBar() throws Exception {
        int barY = 503 / 2 - 18;
        assertEquals(-1, whiteSpan(draw(765, 503, 20, ""), barY)[1]);
        String[] messages = {"Loading...", "Unpacking media - 42%", "W"};
        for (String m : messages) {
            int[] span = whiteSpan(draw(765, 503, 20, m), barY);
            assertTrue("text drawn for " + m, span[1] >= 0);
            assertTrue("centred " + m + " " + span[0] + ".." + span[1], Math.abs((span[0] + span[1]) / 2 - 765 / 2) <= 3);
        }
        int[] wide = whiteSpan(draw(765, 503, 20, "Unpacking media - 42%"), barY);
        int[] narrow = whiteSpan(draw(765, 503, 20, "W"), barY);
        assertTrue(wide[1] - wide[0] > narrow[1] - narrow[0]);
    }
}
