import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SpriteHorizontalCenteringTest {
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

    @Test public void spanShiftsHorizontalSamplesWithoutChangingMaskCoverage() {
        Sprite sprite = numbered(256, 16);
        int[] source = sprite.pixels.clone();
        // Explicit observations distinguish truncated half-span from exact half,
        // output width, source width and row count, including signed inputs.
        int[][] cases = {{-5,2},{-4,2},{-3,1},{-2,1},{-1,0},{0,0},{1,0},
                {2,-1},{3,-1},{4,-2},{5,-2},{32,-16},{33,-16},{34,-17},
                {145,-72},{146,-73},{147,-73}};
        int[] counts = {3,0,2}, offsets = {0,1,1};
        for (boolean filtering : new boolean[] {false, true}) {
            ClientSettings.BILINEAR_MINIMAP_FILTERING = filtering;
            for (int[] c : cases) {
                int[] raster = target(8, 7), expected = filled(8, 7);
                sprite.drawRotatedScanlines(3, 0, counts, 256, offsets, 8, 2, 3, c[0], 100);
                for (int col = 0; col < 3; col++)
                    expected[3 + col + 2 * 8] = source[100 + c[1] + col + 7 * 256];
                for (int col = 1; col < 3; col++)
                    expected[3 + col + 4 * 8] = source[100 + c[1] + col + 9 * 256];
                assertArrayEquals(expected, raster);
                assertArrayEquals(new int[] {3,0,2}, counts);
                assertArrayEquals(new int[] {0,1,1}, offsets);
            }
        }
        assertArrayEquals(source, sprite.pixels);
    }

    @Test public void centerColumnUsesTargetColumnUnitsEvenWhenOnlyOnePixelIsDrawn() {
        Sprite sprite = numbered(32, 16);
        int[][] cases = {{0,0},{1,0},{2,1},{3,1},{4,2},{5,2},
                {32,16},{33,16},{34,17},{145,72},{146,73},{147,73}};
        for (boolean filtering : new boolean[] {false, true}) {
            ClientSettings.BILINEAR_MINIMAP_FILTERING = filtering;
            for (int[] c : cases) {
                int[] raster = target(80, 3), expected = filled(80, 3);
                sprite.drawRotatedScanlines(1, 0, new int[] {1}, 256, new int[] {c[1]},
                        8, 1, 2, c[0], 16);
                expected[2 + c[1] + 80] = sprite.pixels[16 + 8 * 32];
                assertArrayEquals(expected, raster);
            }
        }
    }

    @Test public void centeringDisplacementIsScaledBeforeSampling() {
        Sprite sprite = new Sprite(80, 40);
        // Even channel slopes make half-coordinate means exact.
        for (int y = 0; y < 40; y++) for (int x = 0; x < 80; x++)
            sprite.pixels[x + y * 80] = (2 * x << 16) | (4 * y << 8);
        int[][] cases = {{-4,2},{-3,1},{0,0},{1,0},{2,-1},{3,-1},
                {4,-2},{5,-2},{32,-16},{33,-16}};
        for (boolean filtering : new boolean[] {false, true}) {
            ClientSettings.BILINEAR_MINIMAP_FILTERING = filtering;
            for (int scale : new int[] {-512,-256,-128,0,128,256,384,512}) {
                for (int[] c : cases) {
                    int[] raster = target(8, 7), expected = filled(8, 7);
                    sprite.drawRotatedScanlines(3, 0, new int[] {3,0,2}, scale,
                            new int[] {0,1,1}, 16, 2, 3, c[0], 32);
                    for (int row = 0; row < 3; row++) for (int col = 0; col < 3; col++) {
                        if (row == 1 || (row == 2 && col == 0)) continue;
                        double x = 32 + (c[1] + col) * (scale / 256.0);
                        double y = 16 + (row - 1) * (scale / 256.0);
                        if (!filtering) { x = Math.floor(x); y = Math.floor(y); }
                        expected[3 + col + (2 + row) * 8] = ((int) (2 * x) << 16) | ((int) (4 * y) << 8);
                    }
                    assertArrayEquals(expected, raster);
                }
            }
        }
    }

    @Test public void twoMoreSpanUnitsAreBalancedByOneMoreMaskColumnAcrossRotations() {
        Sprite sprite = numbered(80, 80);
        int[] source = sprite.pixels.clone();
        for (boolean filtering : new boolean[] {false, true}) {
            ClientSettings.BILINEAR_MINIMAP_FILTERING = filtering;
            for (int angle : new int[] {-512,0,137,512,1024,2047}) {
                for (int scale : new int[] {-256,0,128,256,384}) {
                    int[] reference = target(8, 7);
                    sprite.drawRotatedScanlines(3, angle, new int[] {3,0,2}, scale,
                            new int[] {0,1,1}, 40, 2, 3, 3, 40);
                    int[] raster = target(8, 7);
                    sprite.drawRotatedScanlines(3, angle, new int[] {3,0,2}, scale,
                            new int[] {1,2,2}, 40, 2, 2, 5, 40);
                    assertArrayEquals(reference, raster);
                }
            }
        }
        assertArrayEquals(source, sprite.pixels);
    }

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
