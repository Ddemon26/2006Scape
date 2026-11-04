import static org.junit.Assert.*;

import java.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SpriteDrawingAreaTest {
    private int[] previousPixels;
    private int[] previousState;
    private boolean previousViewport;

    @Before
    public void saveDrawingState() {
        previousPixels = DrawingArea.pixels;
        previousState = new int[] {DrawingArea.width, DrawingArea.height, DrawingArea.topX,
                DrawingArea.topY, DrawingArea.bottomX, DrawingArea.bottomY,
                DrawingArea.centerX, DrawingArea.centerY, DrawingArea.anInt1387};
        previousViewport = ClientSettings.FULL_512PX_VIEWPORT;
        ClientSettings.FULL_512PX_VIEWPORT = false;
    }

    @After
    public void restoreDrawingState() {
        DrawingArea.pixels = previousPixels;
        DrawingArea.width = previousState[0];
        DrawingArea.height = previousState[1];
        DrawingArea.topX = previousState[2];
        DrawingArea.topY = previousState[3];
        DrawingArea.bottomX = previousState[4];
        DrawingArea.bottomY = previousState[5];
        DrawingArea.centerX = previousState[6];
        DrawingArea.centerY = previousState[7];
        DrawingArea.anInt1387 = previousState[8];
        ClientSettings.FULL_512PX_VIEWPORT = previousViewport;
    }

    @Test
    public void selectsStoredPixelsAndDimensionsWithoutDrawingOrExpanding() {
        int[] oldTarget = new int[42];
        Arrays.fill(oldTarget, 0x654321);
        DrawingArea.initDrawingArea(6, 7, oldTarget);
        DrawingArea.setDrawingArea(4, 2, 5, 1);
        Sprite sprite = new Sprite(3, 2);
        sprite.pixels = new int[] {0, 1, 0x123456, 4, 5, 6};
        int[] storage = sprite.pixels;
        sprite.trimWidth = 9;
        sprite.trimHeight = 8;
        sprite.offsetX = 4;
        sprite.offsetY = 5;
        sprite.setAsDrawingArea();
        assertSame(storage, DrawingArea.pixels);
        assertArrayEquals(new int[] {0, 1, 0x123456, 4, 5, 6}, sprite.pixels);
        for (int pixel : oldTarget) assertEquals(0x654321, pixel);
        assertBounds(3, 2);
        assertEquals(2, DrawingArea.centerX);
        assertEquals(1, DrawingArea.centerY);
        assertEquals(1, DrawingArea.anInt1387);
        assertEquals(3, sprite.width);
        assertEquals(2, sprite.height);
        assertEquals(9, sprite.trimWidth);
        assertEquals(8, sprite.trimHeight);
        assertEquals(4, sprite.offsetX);
        assertEquals(5, sprite.offsetY);
    }

    @Test
    public void subsequentRasterWritesReachSpriteAtUnshiftedCoordinates() {
        Sprite sprite = new Sprite(3, 2);
        sprite.offsetX = 20;
        sprite.offsetY = 30;
        sprite.setAsDrawingArea();
        DrawingArea.fillArea(1, 1, 0xabcdef, 1, 2);
        assertArrayEquals(new int[] {0, 0, 0, 0, 0, 0xabcdef}, sprite.pixels);
        sprite.pixels[0] = 0x123456;
        assertEquals(0x123456, DrawingArea.pixels[0]);
        DrawingArea.fillArea(1, 2, 0xff0000, 1, 0); // one row beyond bottom
        DrawingArea.fillArea(1, 0, 0xff0000, 1, 3); // one column beyond right
        assertArrayEquals(new int[] {0x123456, 0, 0, 0, 0, 0xabcdef}, sprite.pixels);
    }

    @Test
    public void reselectionResetsClipAndReplacesThePreviousTarget() {
        Sprite first = new Sprite(3, 2);
        Sprite second = new Sprite(2, 3);
        first.setAsDrawingArea();
        DrawingArea.fillArea(1, 0, 7, 1, 0);
        second.setAsDrawingArea();
        DrawingArea.fillArea(1, 2, 9, 1, 1);
        assertArrayEquals(new int[] {7, 0, 0, 0, 0, 0}, first.pixels);
        assertArrayEquals(new int[] {0, 0, 0, 0, 0, 9}, second.pixels);
        DrawingArea.setDrawingArea(2, 1, 2, 1);
        first.setAsDrawingArea();
        assertBounds(3, 2);
        DrawingArea.fillArea(1, 0, 11, 1, 0);
        assertEquals(11, first.pixels[0]);
        assertEquals(0, second.pixels[0]);
    }

    @Test
    public void viewportSettingOnlyChangesTheExistingHorizontalCenterConvention() {
        Sprite sprite = new Sprite(5, 3);
        ClientSettings.FULL_512PX_VIEWPORT = true;
        sprite.setAsDrawingArea();
        assertBounds(5, 3);
        assertEquals(5, DrawingArea.centerX);
        assertEquals(2, DrawingArea.centerY);
        assertEquals(1, DrawingArea.anInt1387);
        ClientSettings.FULL_512PX_VIEWPORT = false;
        sprite.setAsDrawingArea();
        assertEquals(4, DrawingArea.centerX);
    }

    @Test
    public void zeroSizedAxesProduceEmptyClipsWithoutAllocatingOrDrawing() {
        for (int[] size : new int[][] {{0, 0}, {0, 3}, {3, 0}, {1, 1}}) {
            Sprite sprite = new Sprite(size[0], size[1]);
            int[] storage = sprite.pixels;
            sprite.setAsDrawingArea();
            assertSame(storage, DrawingArea.pixels);
            assertBounds(size[0], size[1]);
            assertEquals(size[0] - 1, DrawingArea.centerX);
            assertEquals(size[0] / 2, DrawingArea.centerY);
            assertEquals(size[1] / 2, DrawingArea.anInt1387);
        }
    }

    @Test
    public void usesCurrentMutableStorageAndDoesNotValidateItsLength() {
        Sprite sprite = new Sprite(3, 2);
        sprite.pixels = new int[] {42};
        sprite.width = 7;
        sprite.height = 4;
        sprite.setAsDrawingArea();
        assertSame(sprite.pixels, DrawingArea.pixels);
        assertBounds(7, 4);
        assertArrayEquals(new int[] {42}, sprite.pixels);
        sprite.pixels = null;
        sprite.setAsDrawingArea();
        assertNull(DrawingArea.pixels);
        assertBounds(7, 4);
        // Binding is not validation: subsequent drawing with inconsistent storage is unsafe.
    }

    private static void assertBounds(int width, int height) {
        assertEquals(width, DrawingArea.width);
        assertEquals(height, DrawingArea.height);
        assertEquals(0, DrawingArea.topX);
        assertEquals(0, DrawingArea.topY);
        assertEquals(width, DrawingArea.bottomX);
        assertEquals(height, DrawingArea.bottomY);
    }
}
