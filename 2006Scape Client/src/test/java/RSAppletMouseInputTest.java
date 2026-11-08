import java.awt.Canvas;
import java.awt.event.MouseEvent;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class RSAppletMouseInputTest {
    private static final Canvas SOURCE = new Canvas();

    /** Records every middle-button drag delta the listener hands on. */
    public static final class DragSpy extends RSApplet {
        List<int[]> calls;
        @Override void mouseWheelDragged(int a, int b) { calls.add(new int[] {a, b}); }
    }

    private static <T> T allocate(Class<T> type) throws Exception {
        Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        return type.cast(((sun.misc.Unsafe) unsafeField.get(null)).allocateInstance(type));
    }

    private static int field(RSApplet applet, String name) throws Exception {
        Field f = RSApplet.class.getDeclaredField(name);
        f.setAccessible(true);
        return f.getInt(applet);
    }

    private static DragSpy spy() throws Exception {
        DragSpy spy = allocate(DragSpy.class);
        spy.calls = new ArrayList<int[]>();
        return spy;
    }

    private static MouseEvent event(int id, int x, int y, int button) {
        return new MouseEvent(SOURCE, id, 0L, 0, x, y, 1, false, button);
    }

    private static MouseEvent press(int x, int y, int button) { return event(MouseEvent.MOUSE_PRESSED, x, y, button); }
    private static MouseEvent drag(int x, int y) { return event(MouseEvent.MOUSE_DRAGGED, x, y, MouseEvent.NOBUTTON); }

    @Test public void pressStoresEventXAndYAsClickPosition() throws Exception {
        DragSpy s = spy();
        s.idleTime = 99;
        s.mousePressed(press(30, 410, MouseEvent.BUTTON1));
        assertEquals(30, field(s, "clickX")); assertEquals(410, field(s, "clickY"));
        assertEquals(0, s.idleTime);
        assertEquals(1, field(s, "clickMode1")); assertEquals(1, s.clickMode2);
        assertFalse(s.mouseWheelDown);
        s.mousePressed(press(412, 7, MouseEvent.BUTTON3));   // swapped magnitudes: x and y are not interchangeable
        assertEquals(412, field(s, "clickX")); assertEquals(7, field(s, "clickY"));
        assertEquals(2, field(s, "clickMode1")); assertEquals(2, s.clickMode2);
    }

    @Test public void middlePressRecordsClickButStartsWheelDragInsteadOfClickMode() throws Exception {
        DragSpy s = spy();
        s.mousePressed(press(100, 200, MouseEvent.BUTTON2));
        assertTrue(s.mouseWheelDown);
        assertEquals(100, field(s, "clickX")); assertEquals(200, field(s, "clickY"));
        assertEquals(0, field(s, "clickMode1")); assertEquals(0, s.clickMode2);
        s.mouseReleased(event(MouseEvent.MOUSE_RELEASED, 100, 200, MouseEvent.BUTTON2));
        assertFalse(s.mouseWheelDown);
    }

    @Test public void moveStoresEventXAndYAsMousePosition() throws Exception {
        DragSpy s = spy();
        s.idleTime = 5;
        s.mouseMoved(event(MouseEvent.MOUSE_MOVED, 11, 322, MouseEvent.NOBUTTON));
        assertEquals(11, s.mouseX); assertEquals(322, s.mouseY);
        assertEquals(0, s.idleTime);
        s.mouseMoved(event(MouseEvent.MOUSE_MOVED, 322, 11, MouseEvent.NOBUTTON));
        assertEquals(322, s.mouseX); assertEquals(11, s.mouseY);
    }

    @Test public void middleDragPassesLeftwardXThenDownwardYDeltaFromPreviousPosition() throws Exception {
        DragSpy s = spy();
        s.mousePressed(press(100, 200, MouseEvent.BUTTON2));
        s.mouseDragged(drag(90, 200));     // 10 px left, no vertical motion
        s.mouseDragged(drag(90, 215));     // 15 px down from the new position
        s.mouseDragged(drag(97, 208));     // 7 right, 7 up
        s.mouseDragged(drag(97, 208));     // no motion
        assertEquals(4, s.calls.size());
        assertArrayEquals(new int[] {10, 0}, s.calls.get(0));
        assertArrayEquals(new int[] {0, 15}, s.calls.get(1));
        assertArrayEquals(new int[] {-7, -7}, s.calls.get(2));
        assertArrayEquals(new int[] {0, 0}, s.calls.get(3));
        assertEquals(0, s.mouseX); assertEquals(0, s.mouseY);   // a wheel drag does not move the pointer fields
    }

    @Test public void ordinaryDragMovesPointerWithoutWheelCallback() throws Exception {
        DragSpy s = spy();
        s.mousePressed(press(100, 200, MouseEvent.BUTTON1));
        s.idleTime = 4;
        s.mouseDragged(drag(60, 250));
        assertTrue(s.calls.isEmpty());
        assertEquals(60, s.mouseX); assertEquals(250, s.mouseY); assertEquals(0, s.idleTime);
        s.mousePressed(press(60, 250, MouseEvent.BUTTON2));
        s.mouseReleased(event(MouseEvent.MOUSE_RELEASED, 60, 250, MouseEvent.BUTTON2));
        s.mouseDragged(drag(10, 20));      // released: back to pointer movement
        assertTrue(s.calls.isEmpty());
        assertEquals(10, s.mouseX); assertEquals(20, s.mouseY);
    }

    @Test public void gameTurnsLeftwardDragIntoYawAndDownwardDragIntoPitchVelocity() throws Exception {
        Game game = allocate(Game.class);
        game.mousePressed(press(300, 150, MouseEvent.BUTTON2));
        game.mouseDragged(drag(296, 150));     // 4 left: yaw += 3 * 4
        assertEquals(12, game.cameraYawVelocity); assertEquals(0, game.cameraPitchVelocity);
        game.mouseDragged(drag(296, 155));     // 5 down: pitch += 2 * 5
        assertEquals(12, game.cameraYawVelocity); assertEquals(10, game.cameraPitchVelocity);
        game.mouseDragged(drag(302, 152));     // 6 right, 3 up
        assertEquals(12 - 18, game.cameraYawVelocity); assertEquals(10 - 6, game.cameraPitchVelocity);
        game.mouseReleased(event(MouseEvent.MOUSE_RELEASED, 302, 152, MouseEvent.BUTTON2));
        game.mouseDragged(drag(0, 0));
        assertEquals(-6, game.cameraYawVelocity); assertEquals(4, game.cameraPitchVelocity);
    }
}
