import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SpriteSourceCenterTest {
    private static final int FILL = 0x204060;
    private int[] savedPixels;
    private int[] saved;
    private boolean savedFiltering;

    @Before public void saveDrawingArea() {
        savedPixels = DrawingArea.pixels;
        saved = new int[] {DrawingArea.width, DrawingArea.height, DrawingArea.topX,
                DrawingArea.topY, DrawingArea.bottomX, DrawingArea.bottomY,
                DrawingArea.centerX, DrawingArea.centerY, DrawingArea.anInt1387};
        savedFiltering = ClientSettings.BILINEAR_MINIMAP_FILTERING;
    }

    @After public void restoreDrawingArea() {
        DrawingArea.pixels = savedPixels;
        DrawingArea.width = saved[0]; DrawingArea.height = saved[1];
        DrawingArea.topX = saved[2]; DrawingArea.topY = saved[3];
        DrawingArea.bottomX = saved[4]; DrawingArea.bottomY = saved[5];
        DrawingArea.centerX = saved[6]; DrawingArea.centerY = saved[7];
        DrawingArea.anInt1387 = saved[8];
        ClientSettings.BILINEAR_MINIMAP_FILTERING = savedFiltering;
    }

    @Test public void horizontalCenterSelectsSourceColumnInPixels() {
        for (boolean filtering : new boolean[] {false, true}) {
            ClientSettings.BILINEAR_MINIMAP_FILTERING = filtering;
            for (int cx : new int[] {0,1,8,18})
                for (int angle : new int[] {0,512,1024,2047})
                    for (int scale : new int[] {0,128,256,384})
                        assertCenter(cx, 9, angle, scale);
        }
    }

    @Test public void verticalCenterSelectsSourceRowInPixels() {
        for (boolean filtering : new boolean[] {false, true}) {
            ClientSettings.BILINEAR_MINIMAP_FILTERING = filtering;
            for (int cy : new int[] {0,1,8,18})
                for (int angle : new int[] {0,512,1024,2047})
                    for (int scale : new int[] {0,128,256,384})
                        assertCenter(9, cy, angle, scale);
        }
    }

    @Test public void movingEitherSourceCenterMovesSampleWindowButNotDestination() {
        Sprite sprite = numberedSprite();
        int[] source = sprite.pixels.clone();
        for (boolean filtering : new boolean[] {false, true}) {
            ClientSettings.BILINEAR_MINIMAP_FILTERING = filtering;
            // Independently move one source axis, holding all destination inputs fixed.
            for (int[] center : new int[][] {{8,8},{7,8},{10,8},{8,7},{8,10}}) {
                int[] raster = target();
                sprite.method352(3, 0, new int[] {3,3,3}, 256,
                        new int[] {0,0,0}, center[1], 3, 4, 3, center[0]);
                int[] expected = filled();
                for (int row = 0; row < 3; row++) for (int col = 0; col < 3; col++)
                    expected[4 + col + (3 + row) * 16] = source[
                            center[0] - 1 + col + (center[1] - 1 + row) * 20];
                assertArrayEquals(expected, raster);
                assertArrayEquals(source, sprite.pixels);
            }
        }
    }

    @Test public void centersReferToStoredPixelsDespiteSpriteOffsetsAndTrimSize() {
        for (boolean filtering : new boolean[] {false, true}) {
            ClientSettings.BILINEAR_MINIMAP_FILTERING = filtering;
            Sprite sprite = numberedSprite();
            sprite.offsetX = 3; sprite.offsetY = 4;
            sprite.trimWidth = 40; sprite.trimHeight = 50;
            int[] raster = target();
            sprite.method352(1, 512, new int[] {1}, 384, new int[] {0}, 9, 3, 4, 0, 8);
            int[] expected = filled(); expected[4 + 3 * 16] = sprite.pixels[8 + 9 * 20];
            assertArrayEquals(expected, raster);
        }
    }

    private static void assertCenter(int cx, int cy, int angle, int scale) {
        Sprite sprite = numberedSprite();
        int[] raster = target();
        // A single sample at the transform origin removes angle/scale from the oracle.
        sprite.method352(1, angle, new int[] {1}, scale, new int[] {0}, cy, 3, 4, 0, cx);
        int[] expected = filled(); expected[4 + 3 * 16] = sprite.pixels[cx + cy * 20];
        assertArrayEquals(expected, raster);
    }

    private static Sprite numberedSprite() {
        Sprite sprite = new Sprite(20, 20);
        for (int i = 0; i < sprite.pixels.length; i++) sprite.pixels[i] = i + 1;
        return sprite;
    }
    private static int[] target() {
        int[] raster = filled(); DrawingArea.initDrawingArea(16, 16, raster); return raster;
    }
    private static int[] filled() {
        int[] raster = new int[16 * 16]; Arrays.fill(raster, FILL); return raster;
    }
}
