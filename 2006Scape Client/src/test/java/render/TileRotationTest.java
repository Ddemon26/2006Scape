package render;

import org.junit.Test;
import static org.junit.Assert.*;

public class TileRotationTest {
    @Test
    public void testRotateX() {
        assertEquals(5, TileRotation.rotateX(0, 0, 5));
        assertEquals(3, TileRotation.rotateX(1, 3, 5));
        assertEquals(2, TileRotation.rotateX(2, 3, 5));
        assertEquals(4, TileRotation.rotateX(3, 3, 5));
    }

    @Test
    public void testRotateY() {
        assertEquals(2, TileRotation.rotateY(2, 0, 2));
        assertEquals(6, TileRotation.rotateY(2, 1, 1));
        assertEquals(5, TileRotation.rotateY(5, 2, 1));
        assertEquals(1, TileRotation.rotateY(1, 3, 2));
    }

    @Test
    public void testRotateWidthAndHeight() {
        int j = 2;
        int k = 0;
        int l = 1;
        int i1 = 2;
        assertEquals(0, TileRotation.rotateWidth(0, j, k, l, i1));
        assertEquals(1, TileRotation.rotateWidth(1, j, k, l, i1));
        assertEquals(6, TileRotation.rotateWidth(2, j, k, l, i1));
        assertEquals(5, TileRotation.rotateWidth(3, j, k, l, i1));

        assertEquals(0, TileRotation.rotateHeight(k, j, 0, i1, l));
        assertEquals(4, TileRotation.rotateHeight(k, j, 1, i1, l));
        assertEquals(6, TileRotation.rotateHeight(k, j, 2, i1, l));
        assertEquals(1, TileRotation.rotateHeight(k, j, 3, i1, l));
    }
}
