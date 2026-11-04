import static org.junit.Assert.*;

import org.junit.Test;

public class SpriteCanvasExpansionTest {
    @Test
    public void placesStoredPixelsAtOffsetsWithinOriginalCanvas() {
        Sprite sprite = new Sprite(3, 2);
        sprite.pixels = new int[] {11, 0, 13, 21, 22, 23};
        sprite.trimWidth = 6;
        sprite.trimHeight = 5;
        sprite.offsetX = 2;
        sprite.offsetY = 1;
        int[] original = sprite.pixels;

        sprite.expandToCanvas();

        int[] expected = new int[30];
        expected[8] = 11;
        expected[10] = 13;
        expected[14] = 21;
        expected[15] = 22;
        expected[16] = 23;
        assertArrayEquals(expected, sprite.pixels);
        assertNotSame(original, sprite.pixels);
        assertArrayEquals(new int[] {11, 0, 13, 21, 22, 23}, original);
        assertEquals(6, sprite.width);
        assertEquals(5, sprite.height);
        assertEquals(0, sprite.offsetX);
        assertEquals(0, sprite.offsetY);
    }

    @Test
    public void zeroOffsetsStillAllocateAndEmptyCanvasRemainsEmpty() {
        Sprite sprite = new Sprite(1, 1);
        sprite.pixels[0] = 7;
        int[] original = sprite.pixels;
        sprite.expandToCanvas();
        assertNotSame(original, sprite.pixels);
        assertArrayEquals(new int[] {7}, sprite.pixels);

        Sprite empty = new Sprite(0, 0);
        empty.expandToCanvas();
        assertEquals(0, empty.pixels.length);
        assertEquals(0, empty.width);
        assertEquals(0, empty.height);
    }
}
