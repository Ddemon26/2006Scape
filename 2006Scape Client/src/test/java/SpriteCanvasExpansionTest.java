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
    @Test
    public void expansionTraversesAsymmetricRectanglesAndPreservesRawPixels() {
        for (int width = 0; width <= 4; width++) for (int height = 0; height <= 3; height++)
            for (int x = 0; x <= 2; x++) for (int y = 0; y <= 2; y++) {
                Sprite sprite = new Sprite(width, height);
                sprite.trimWidth = 7; sprite.trimHeight = 5;
                sprite.offsetX = x; sprite.offsetY = y;
                for (int i = 0; i < sprite.pixels.length; i++)
                    sprite.pixels[i] = i % 3 == 0 ? 0 : (i % 3 == 1 ? 0x80000000 + i : 0x7f102030 + i);
                int[] source = sprite.pixels, unchanged = source.clone();
                int[] expected = new int[35];
                // Destination-coordinate oracle: only the translated source rectangle is populated.
                for (int dy = 0; dy < 5; dy++) for (int dx = 0; dx < 7; dx++)
                    if (dx >= x && dx < x + width && dy >= y && dy < y + height)
                        expected[dy * 7 + dx] = source[(dy - y) * width + dx - x];
                sprite.expandToCanvas();
                assertArrayEquals(expected, sprite.pixels);
                assertArrayEquals(unchanged, source);
                assertNotSame(source, sprite.pixels);
                assertEquals(7, sprite.width); assertEquals(5, sprite.height);
                assertEquals(7, sprite.trimWidth); assertEquals(5, sprite.trimHeight);
                assertEquals(0, sprite.offsetX); assertEquals(0, sprite.offsetY);
            }
    }
}
