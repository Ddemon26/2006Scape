package render;

import org.junit.Test;
import render.objects.TileDecoration;

import static org.junit.Assert.*;

public class TileDecorationTest {
    @Test
    public void testDefaultsAfterConstruction() {
        TileDecoration d = new TileDecoration();
        assertEquals(0, d.tileHeight);
        assertEquals(0, d.x);
        assertEquals(0, d.y);
        assertNull(d.renderable);
        assertEquals(0, d.uid);
        assertEquals(0, d.config);
    }
}
