package game;

import game.animation.AnimFrame;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.*;

public class AnimFrameTest {

    @Test
    public void testInitSetsFrameArray() throws Exception {
        AnimFrame.init(2);
        Field framesField = AnimFrame.class.getDeclaredField("frames");
        framesField.setAccessible(true);
        AnimFrame[] frames = (AnimFrame[]) framesField.get(null);
        assertEquals("Length should be capacity + 1", 3, frames.length);
        AnimFrame.clear();
    }

    @Test
    public void testIsNullFrame() {
        assertTrue(AnimFrame.isNullFrame(-1));
        assertFalse(AnimFrame.isNullFrame(0));
    }
}
