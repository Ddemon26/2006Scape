import java.awt.Canvas;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/** Semantic tests for RSApplet.handleKeyPressed and keyReleased (key code / key char translation). */
public class RSAppletKeyInputTest {
    private static final Canvas SOURCE = new Canvas();
    private boolean savedShift;
    private boolean savedCtrl;

    @Before public void saveStatics() { savedShift = RSApplet.shiftDown; savedCtrl = RSApplet.ctrlDown; }
    @After public void restoreStatics() { RSApplet.shiftDown = savedShift; RSApplet.ctrlDown = savedCtrl; }

    private static Field f(String name) throws Exception {
        Field field = RSApplet.class.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    private static RSApplet applet() throws Exception {
        Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        RSApplet a = (RSApplet) ((sun.misc.Unsafe) unsafeField.get(null)).allocateInstance(RSApplet.class);
        f("keyArray").set(a, new int[128]);
        f("charQueue").set(a, new int[128]);
        return a;
    }

    private static int[] keys(RSApplet a) throws Exception { return (int[]) f("keyArray").get(a); }

    /** The queued values since construction, in order. */
    private static int[] queue(RSApplet a) throws Exception {
        int n = f("writeIndex").getInt(a);
        int[] q = (int[]) f("charQueue").get(a);
        int[] out = new int[n];
        System.arraycopy(q, 0, out, 0, n);
        return out;
    }

    private static int marked(RSApplet a) throws Exception {
        int n = 0;
        for (int v : keys(a)) n += v;
        return n;
    }

    @Test public void printableCharacterIsMarkedAndQueuedUnchanged() throws Exception {
        RSApplet a = applet();
        a.idleTime = 50;
        a.handleKeyPressed(KeyEvent.VK_A, 'a', false, false);
        assertEquals(0, a.idleTime);
        assertEquals(1, keys(a)['a']);
        assertEquals(0, keys(a)[KeyEvent.VK_A]);                 // 65 is the key code, not the stored value
        assertArrayEquals(new int[] {'a'}, queue(a));
    }

    @Test public void firstParameterSelectsTranslationSecondIsTheCharacter() throws Exception {
        RSApplet a = applet();
        a.handleKeyPressed(37, 'a', false, false);               // VK_LEFT with any char -> slot 1, not queued
        assertEquals(1, keys(a)[1]);
        assertEquals(0, keys(a)['a']);
        assertEquals(0, queue(a).length);
        RSApplet b = applet();
        b.handleKeyPressed(KeyEvent.VK_A, 37, false, false);     // swapped: char 37 '%' is kept as is
        assertEquals(1, keys(b)[37]);
        assertEquals(0, keys(b)[1]);
        assertArrayEquals(new int[] {37}, queue(b));
    }

    @Test public void controlCharactersBelowThirtyAreDroppedUnlessKeyCodeMapsThem() throws Exception {
        RSApplet a = applet();
        a.handleKeyPressed(0, 29, false, false);
        assertEquals(0, marked(a)); assertEquals(0, queue(a).length);
        a.handleKeyPressed(0, 30, false, false);                 // boundary: 30 survives
        assertEquals(1, keys(a)[30]);
        assertArrayEquals(new int[] {30}, queue(a));
        RSApplet b = applet();
        b.handleKeyPressed(KeyEvent.VK_ENTER, '\n', false, false);   // '\n' (10) < 30 but keyCode 10 restores it
        b.handleKeyPressed(KeyEvent.VK_BACK_SPACE, '\b', false, false);
        b.handleKeyPressed(KeyEvent.VK_DELETE, 127, false, false);   // delete maps to backspace (8)
        b.handleKeyPressed(KeyEvent.VK_TAB, '\t', false, false);
        assertArrayEquals(new int[] {10, 8, 8, 9}, queue(b));
    }

    @Test public void navigationAndFunctionKeyCodesBecomeClientKeyValues() throws Exception {
        RSApplet a = applet();
        int undefined = KeyEvent.CHAR_UNDEFINED;                 // 65535, as AWT reports for these keys
        a.handleKeyPressed(KeyEvent.VK_RIGHT, undefined, false, false);
        a.handleKeyPressed(KeyEvent.VK_UP, undefined, false, false);
        a.handleKeyPressed(KeyEvent.VK_DOWN, undefined, false, false);
        assertEquals(1, keys(a)[2]); assertEquals(1, keys(a)[3]); assertEquals(1, keys(a)[4]);
        assertEquals(0, queue(a).length);                        // values 1..4 are held keys, not typed
        a.handleKeyPressed(KeyEvent.VK_CONTROL, undefined, false, false);   // 5: held and queued
        a.handleKeyPressed(KeyEvent.VK_F1, undefined, false, false);
        a.handleKeyPressed(KeyEvent.VK_F12, undefined, false, false);
        a.handleKeyPressed(KeyEvent.VK_HOME, undefined, false, false);
        a.handleKeyPressed(KeyEvent.VK_END, undefined, false, false);
        a.handleKeyPressed(KeyEvent.VK_PAGE_UP, undefined, false, false);
        a.handleKeyPressed(KeyEvent.VK_PAGE_DOWN, undefined, false, false);
        assertEquals(1, keys(a)[5]);
        assertArrayEquals(new int[] {5, 1008, 1019, 1000, 1001, 1002, 1003}, queue(a));
        assertEquals(4, marked(a));                              // >= 128 values are queued only
        RSApplet b = applet();
        b.handleKeyPressed(KeyEvent.VK_A, undefined, false, false);   // unmapped code, char out of range
        assertEquals(0, marked(b));
        assertArrayEquals(new int[] {65535}, queue(b));
    }

    @Test public void modifierFlagsAreOnlySetByPress() throws Exception {
        RSApplet a = applet();
        RSApplet.shiftDown = false; RSApplet.ctrlDown = false;
        a.handleKeyPressed(KeyEvent.VK_A, 'A', false, true);
        assertTrue(RSApplet.shiftDown); assertFalse(RSApplet.ctrlDown);
        a.handleKeyPressed(KeyEvent.VK_A, 'a', true, false);
        assertTrue(RSApplet.shiftDown); assertTrue(RSApplet.ctrlDown);   // not cleared by a later press
    }

    private static KeyEvent release(int keyCode, char keyChar) {
        return new KeyEvent(SOURCE, KeyEvent.KEY_RELEASED, 0L, 0, keyCode, keyChar);
    }

    @Test public void releaseClearsTheSlotTheSameTranslationChose() throws Exception {
        RSApplet a = applet();
        a.handleKeyPressed(KeyEvent.VK_A, 'a', false, false);
        a.handleKeyPressed(KeyEvent.VK_LEFT, KeyEvent.CHAR_UNDEFINED, false, false);
        a.handleKeyPressed(KeyEvent.VK_A, '%', false, false);
        assertEquals(3, marked(a));
        a.keyReleased(release(KeyEvent.VK_A, '%'));              // char picks the slot: 37 cleared, 'a' kept
        assertEquals(0, keys(a)['%']); assertEquals(1, keys(a)['a']); assertEquals(1, keys(a)[1]);
        a.keyReleased(release(KeyEvent.VK_LEFT, 'a'));           // key code 37 overrides the char -> slot 1
        assertEquals(0, keys(a)[1]); assertEquals(1, keys(a)['a']);
        a.keyReleased(release(KeyEvent.VK_A, (char) 29));        // control char below 30 clears nothing
        assertEquals(1, keys(a)['a']);
        a.keyReleased(release(KeyEvent.VK_A, 'a'));
        assertEquals(0, marked(a));
        assertArrayEquals(new int[] {'a', '%'}, queue(a));      // left (1) is held only; release never queues
    }

    @Test public void releaseOfModifierClearsItsFlag() throws Exception {
        RSApplet a = applet();
        RSApplet.shiftDown = true; RSApplet.ctrlDown = true;
        a.keyReleased(release(KeyEvent.VK_SHIFT, KeyEvent.CHAR_UNDEFINED));
        assertFalse(RSApplet.shiftDown); assertTrue(RSApplet.ctrlDown);
        a.handleKeyPressed(KeyEvent.VK_CONTROL, KeyEvent.CHAR_UNDEFINED, true, false);
        assertEquals(1, keys(a)[5]);
        a.keyReleased(release(KeyEvent.VK_CONTROL, KeyEvent.CHAR_UNDEFINED));
        assertFalse(RSApplet.ctrlDown); assertEquals(0, keys(a)[5]);
    }
}
