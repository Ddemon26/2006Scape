package game;

import org.junit.Test;
import static org.junit.Assert.*;

public class AnimableTest {
    @Test
    public void testDefaultModelHeight() {
        Animable animable = new Animable();
        assertEquals(1000, animable.modelHeight);
    }
}
