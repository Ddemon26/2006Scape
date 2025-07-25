package render;

import org.junit.Test;
import render.geometry.VertexNormal;

import static org.junit.Assert.*;

public class VertexNormalTest {
    @Test
    public void testDefaultValues() {
        VertexNormal v = new VertexNormal();
        assertEquals(0, v.x);
        assertEquals(0, v.y);
        assertEquals(0, v.z);
        assertEquals(0, v.magnitude);
    }
}
