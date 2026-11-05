import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SpriteScanlineMaskTest {
    private static final int FILL = 0x204060;
    private int[] savedPixels;
    private int[] saved;
    private boolean savedFiltering;

    @Before public void saveDrawingArea() {
        savedPixels = DrawingArea.pixels;
        saved = new int[] {DrawingArea.width, DrawingArea.height, DrawingArea.topX,
                DrawingArea.topY, DrawingArea.bottomX, DrawingArea.bottomY,
                DrawingArea.scanlineRightLimit, DrawingArea.clipRightHalf, DrawingArea.clipBottomHalf};
        savedFiltering = ClientSettings.BILINEAR_MINIMAP_FILTERING;
    }

    @After public void restoreDrawingArea() {
        DrawingArea.pixels = savedPixels;
        DrawingArea.width = saved[0]; DrawingArea.height = saved[1];
        DrawingArea.topX = saved[2]; DrawingArea.topY = saved[3];
        DrawingArea.bottomX = saved[4]; DrawingArea.bottomY = saved[5];
        DrawingArea.scanlineRightLimit = saved[6]; DrawingArea.clipRightHalf = saved[7];
        DrawingArea.clipBottomHalf = saved[8];
        ClientSettings.BILINEAR_MINIMAP_FILTERING = savedFiltering;
    }

    @Test public void rowCountSelectsArrayPrefixAndCentersSourceVertically() {
        Sprite sprite = numberedSprite();
        for (boolean filtering : new boolean[] {false, true}) {
            ClientSettings.BILINEAR_MINIMAP_FILTERING = filtering;
            for (int rows = 0; rows <= 4; rows++) {
                int[] raster = target();
                sprite.drawRotatedScanlines(rows, 0, new int[] {1,1,1,1,1}, 256,
                        new int[] {0,0,0,0,0}, 8, 3, 4, 6, 8);
                int[] expected = filled();
                // Integer, zero-angle geometry: the center lies at source (8,8).
                for (int row = 0; row < rows; row++)
                    expected[4 + (3 + row) * 16] = sprite.pixels[5 + (8 - rows / 2 + row) * 20];
                assertArrayEquals(expected, raster);
            }
        }
    }

    @Test public void eachRowPixelCountControlsAnExclusiveHorizontalRun() {
        Sprite sprite = numberedSprite();
        for (boolean filtering : new boolean[] {false, true}) {
            ClientSettings.BILINEAR_MINIMAP_FILTERING = filtering;
            for (int count : new int[] {0,1,2,3,7}) {
                int[] raster = target();
                sprite.drawRotatedScanlines(1, 0, new int[] {count}, 256,
                        new int[] {0}, 8, 3, 4, 6, 8);
                int[] expected = filled();
                for (int col = 0; col < count; col++) expected[4 + col + 3 * 16] = sprite.pixels[5 + col + 8 * 20];
                assertArrayEquals(expected, raster);
            }
        }
        // Seven pixels are copied even though the centering width argument is six.
    }

    @Test public void rowStartOffsetsShiftDestinationAndSourceInPixelUnits() {
        Sprite sprite = numberedSprite();
        int[] source = sprite.pixels.clone();
        int[] counts = {2};
        for (boolean filtering : new boolean[] {false, true}) {
            ClientSettings.BILINEAR_MINIMAP_FILTERING = filtering;
            for (int offset : new int[] {0,1,3,-1}) {
                int[] offsets = {offset};
                int[] raster = target();
                sprite.drawRotatedScanlines(1, 0, counts, 256, offsets, 8, 3, 4, 6, 8);
                int[] expected = filled();
                expected[4 + offset + 3 * 16] = source[5 + offset + 8 * 20];
                expected[5 + offset + 3 * 16] = source[6 + offset + 8 * 20];
                assertArrayEquals(expected, raster);
                assertArrayEquals(new int[] {offset}, offsets);
                assertArrayEquals(new int[] {2}, counts);
                assertArrayEquals(source, sprite.pixels);
            }
        }
    }

    @Test public void independentRowsHaveGapsAndIgnoreUnusedArrayTails() {
        Sprite sprite = numberedSprite();
        for (boolean filtering : new boolean[] {false, true}) {
            ClientSettings.BILINEAR_MINIMAP_FILTERING = filtering;
            int[] raster = target();
            sprite.drawRotatedScanlines(3, 0, new int[] {2,0,3,999}, 256,
                    new int[] {0,4,1,999}, 8, 3, 4, 6, 8);
            int[] expected = filled();
            expected[4 + 3 * 16] = sprite.pixels[5 + 7 * 20];
            expected[5 + 3 * 16] = sprite.pixels[6 + 7 * 20];
            expected[5 + 5 * 16] = sprite.pixels[6 + 9 * 20];
            expected[6 + 5 * 16] = sprite.pixels[7 + 9 * 20];
            expected[7 + 5 * 16] = sprite.pixels[8 + 9 * 20];
            assertArrayEquals(expected, raster);
        }
    }

    @Test public void scanlineMaskIsNotAZeroTransparencyOrDrawingAreaClipMask() {
        Sprite sprite = numberedSprite();
        sprite.pixels[5 + 8 * 20] = 0;
        sprite.offsetX = 3; sprite.offsetY = 4;
        sprite.trimWidth = 40; sprite.trimHeight = 50;
        for (boolean filtering : new boolean[] {false, true}) {
            ClientSettings.BILINEAR_MINIMAP_FILTERING = filtering;
            int[] raster = target();
            DrawingArea.setClip(1, 0, 1, 0);
            sprite.drawRotatedScanlines(1, 0, new int[] {2}, 256, new int[] {0}, 8, 3, 4, 6, 8);
            int[] expected = filled();
            expected[4 + 3 * 16] = 0;
            expected[5 + 3 * 16] = sprite.pixels[6 + 8 * 20];
            assertArrayEquals(expected, raster);
        }
    }

    private static Sprite numberedSprite() {
        Sprite sprite = new Sprite(20, 20);
        for (int i = 0; i < sprite.pixels.length; i++) sprite.pixels[i] = i + 1;
        return sprite;
    }
    private static int[] target() {
        int[] raster = filled(); DrawingArea.bindRaster(16, 16, raster); return raster;
    }
    private static int[] filled() {
        int[] raster = new int[16 * 16]; Arrays.fill(raster, FILL); return raster;
    }
}
