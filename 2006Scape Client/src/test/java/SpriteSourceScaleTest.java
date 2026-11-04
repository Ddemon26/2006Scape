import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SpriteSourceScaleTest {
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

    @Test public void scaleUnitsAreOne256thSourcePixelPerHorizontalTargetPixel() {
        Sprite sprite = numbered(1200, 5);
        int[] source = sprite.pixels.clone();
        for (boolean filtering : new boolean[] {false, true}) {
            ClientSettings.BILINEAR_MINIMAP_FILTERING = filtering;
            for (int scale : scales()) {
                int[] raster = target(260, 3), expected = filled(260, 3);
                // A 256-pixel displacement amplifies one input unit to one source pixel.
                sprite.method352(1, 0, new int[] {1}, scale, new int[] {256},
                        2, 1, 0, 0, 600);
                expected[256 + 260] = source[600 + scale + 2 * 1200];
                assertArrayEquals(expected, raster);
            }
        }
        assertArrayEquals(source, sprite.pixels);
    }

    @Test public void sameScaleUnitsApplyToVerticalTargetSteps() {
        Sprite sprite = numbered(5, 1200);
        int[] source = sprite.pixels.clone();
        int[] counts = new int[513], offsets = new int[513]; counts[512] = 1;
        int[] originalCounts = counts.clone();
        for (boolean filtering : new boolean[] {false, true}) {
            ClientSettings.BILINEAR_MINIMAP_FILTERING = filtering;
            for (int scale : scales()) {
                int[] raster = target(3, 513), expected = filled(3, 513);
                // With 513 rows the final row is 256 steps below the source center.
                sprite.method352(513, 0, counts, scale, offsets, 600, 0, 1, 0, 2);
                expected[1 + 512 * 3] = source[2 + (600 + scale) * 5];
                assertArrayEquals(expected, raster);
                assertArrayEquals(originalCounts, counts);
                assertArrayEquals(new int[513], offsets);
            }
        }
        assertArrayEquals(source, sprite.pixels);
    }

    @Test public void scaleChangesSampleSpacingAndDirectionWithoutChangingMaskCoverage() {
        Sprite sprite = new Sprite(40, 40);
        // Independent affine color field: half-pixel samples have exact channel means.
        for (int y = 0; y < 40; y++) for (int x = 0; x < 40; x++)
            sprite.pixels[x + y * 40] = (4 * x << 16) | (4 * y << 8);
        int[] source = sprite.pixels.clone();
        for (boolean filtering : new boolean[] {false, true}) {
            ClientSettings.BILINEAR_MINIMAP_FILTERING = filtering;
            for (int scale : new int[] {-512,-256,-128,0,128,256,384,512}) {
                int[] raster = target(8, 7), expected = filled(8, 7);
                sprite.method352(3, 0, new int[] {3,0,2}, scale, new int[] {0,1,1},
                        16, 2, 3, 3, 16);
                for (int row = 0; row < 3; row++) for (int col = 0; col < 3; col++) {
                    if (row == 1 || (row == 2 && col == 0)) continue;
                    double x = 16 + (col - 1) * (scale / 256.0);
                    double y = 16 + (row - 1) * (scale / 256.0);
                    if (!filtering) { x = Math.floor(x); y = Math.floor(y); }
                    expected[3 + col + (2 + row) * 8] = ((int) (4 * x) << 16) | ((int) (4 * y) << 8);
                }
                assertArrayEquals(expected, raster);
            }
        }
        assertArrayEquals(source, sprite.pixels);
    }

    @Test public void zeroScaleCollapsesEveryMaskedSampleToCenterAtDifferentAngles() {
        Sprite sprite = numbered(40, 40);
        for (boolean filtering : new boolean[] {false, true}) {
            ClientSettings.BILINEAR_MINIMAP_FILTERING = filtering;
            for (int angle : new int[] {-512,0,512,1024,2047}) {
                int[] raster = target(8, 7), expected = filled(8, 7);
                sprite.method352(3, angle, new int[] {3,0,2}, 0, new int[] {0,1,1},
                        16, 2, 3, 3, 16);
                for (int col = 0; col < 3; col++) expected[3 + col + 2 * 8] = sprite.pixels[16 + 16 * 40];
                for (int col = 1; col < 3; col++) expected[3 + col + 4 * 8] = sprite.pixels[16 + 16 * 40];
                assertArrayEquals(expected, raster);
            }
        }
    }

    private static int[] scales() { return new int[] {-512,-256,-1,0,1,128,255,256,257,512}; }
    private static Sprite numbered(int width, int height) {
        Sprite sprite = new Sprite(width, height);
        for (int i = 0; i < sprite.pixels.length; i++) sprite.pixels[i] = i + 1;
        return sprite;
    }
    private static int[] target(int width, int height) {
        int[] raster = filled(width, height); DrawingArea.initDrawingArea(height, width, raster); return raster;
    }
    private static int[] filled(int width, int height) {
        int[] raster = new int[width * height]; Arrays.fill(raster, FILL); return raster;
    }
}
