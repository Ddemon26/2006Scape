import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SpriteDestinationTest {
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

    @Test public void destinationXMovesMaskedRowsByWholeTargetPixels() {
        for (boolean filtering : new boolean[] {false, true}) {
            ClientSettings.BILINEAR_MINIMAP_FILTERING = filtering;
            for (int width : new int[] {16,19})
                for (int x : new int[] {0,1,7,width-3}) assertWindow(width, x, 5);
        }
    }

    @Test public void destinationYMovesMaskedRowsByWholeTargetRows() {
        for (boolean filtering : new boolean[] {false, true}) {
            ClientSettings.BILINEAR_MINIMAP_FILTERING = filtering;
            for (int width : new int[] {16,19})
                for (int y : new int[] {0,1,7,13}) assertWindow(width, 5, y);
        }
    }

    @Test public void destinationTranslationKeepsRotatedScaledSourceSamples() {
        Sprite sprite = numberedSprite();
        int[] source = sprite.pixels.clone();
        for (boolean filtering : new boolean[] {false, true}) {
            ClientSettings.BILINEAR_MINIMAP_FILTERING = filtering;
            for (int angle : new int[] {0,512,1024,2047})
                for (int scale : new int[] {0,128,256,384}) {
                    int[] reference = target(16);
                    sprite.drawRotatedScanlines(3, angle, new int[] {3,3,3}, scale,
                            new int[] {0,0,0}, 16, 0, 0, 3, 16);
                    // Translate each axis independently, keeping every source input fixed.
                    for (int[] point : new int[][] {{1,0},{13,0},{0,1},{0,13}}) {
                        int[] raster = target(16), expected = filled(16);
                        sprite.drawRotatedScanlines(3, angle, new int[] {3,3,3}, scale,
                                new int[] {0,0,0}, 16, point[1], point[0], 3, 16);
                        for (int row = 0; row < 3; row++) for (int col = 0; col < 3; col++)
                            expected[point[0]+col+(point[1]+row)*16] = reference[col+row*16];
                        assertArrayEquals(expected, raster);
                    }
                }
        }
        assertArrayEquals(source, sprite.pixels);
    }

    @Test public void destinationIsMaskOriginDespiteClipAndSpriteCanvasOffsets() {
        Sprite sprite = numberedSprite();
        sprite.offsetX = 3; sprite.offsetY = 4;
        sprite.trimWidth = 80; sprite.trimHeight = 90;
        for (boolean filtering : new boolean[] {false, true}) {
            ClientSettings.BILINEAR_MINIMAP_FILTERING = filtering;
            int[] raster = target(19), expected = filled(19);
            DrawingArea.setClip(1, 0, 1, 0);
            // A nonzero mask offset places the sample after the destination anchor.
            // The final physical pixel is outside the configured clip rectangle.
            sprite.drawRotatedScanlines(1, 0, new int[] {1}, 256,
                    new int[] {3}, 16, 15, 15, 0, 16);
            expected[18+15*19] = sprite.pixels[19+16*40];
            assertArrayEquals(expected, raster);
        }
    }

    private static void assertWindow(int width, int x, int y) {
        Sprite sprite = numberedSprite();
        int[] source = sprite.pixels.clone();
        int[] raster = target(width), expected = filled(width);
        sprite.drawRotatedScanlines(3, 0, new int[] {3,0,2}, 256,
                new int[] {0,3,1}, 16, y, x, 3, 16);
        for (int col = 0; col < 3; col++) expected[x+col+y*width] = source[15+col+15*40];
        for (int col = 1; col < 3; col++) expected[x+col+(y+2)*width] = source[15+col+17*40];
        assertArrayEquals(expected, raster);
        assertArrayEquals(source, sprite.pixels);
    }

    private static Sprite numberedSprite() {
        Sprite sprite = new Sprite(40, 40);
        for (int i = 0; i < sprite.pixels.length; i++) sprite.pixels[i] = i + 1;
        return sprite;
    }
    private static int[] target(int width) {
        int[] raster = filled(width); DrawingArea.bindRaster(16, width, raster); return raster;
    }
    private static int[] filled(int width) {
        int[] raster = new int[width * 16]; Arrays.fill(raster, FILL); return raster;
    }
}
