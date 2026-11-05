import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Random;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DrawingPrimitivesTest {
    private int[] savedPixels;
    private int[] savedState;
    private boolean savedViewport;
    private static final int W = 13, H = 11;
    private static final int LEFT = 2, TOP = 3, RIGHT = 10, BOTTOM = 9;

    @Before public void saveState() {
        savedPixels = DrawingArea.pixels;
        savedState = new int[] {DrawingArea.width, DrawingArea.height, DrawingArea.topX,
                DrawingArea.topY, DrawingArea.bottomX, DrawingArea.bottomY,
                DrawingArea.centerX, DrawingArea.centerY, DrawingArea.anInt1387};
        savedViewport = ClientSettings.FULL_512PX_VIEWPORT;
    }

    @After public void restoreState() {
        DrawingArea.pixels = savedPixels;
        DrawingArea.width = savedState[0]; DrawingArea.height = savedState[1];
        DrawingArea.topX = savedState[2]; DrawingArea.topY = savedState[3];
        DrawingArea.bottomX = savedState[4]; DrawingArea.bottomY = savedState[5];
        DrawingArea.centerX = savedState[6]; DrawingArea.centerY = savedState[7];
        DrawingArea.anInt1387 = savedState[8];
        ClientSettings.FULL_512PX_VIEWPORT = savedViewport;
    }

    private int[] target() {
        int[] raster = new int[W * H + 7];
        for (int i = 0; i < raster.length; i++) raster[i] = 0xa5000000 | (i * 7919 & 0xffffff);
        DrawingArea.initDrawingArea(H, W, raster);
        DrawingArea.setDrawingArea(BOTTOM, LEFT, RIGHT, TOP);
        return raster.clone();
    }

    // Independent coordinate-membership oracle: never walks a clipped scanline.
    public static int blend(int source, int destination, int sourceWeight) {
        int result = 0;
        for (int shift : new int[] {0, 8, 16}) {
            int channel = (((source >>> shift) & 255) * sourceWeight
                    + ((destination >>> shift) & 255) * (256 - sourceWeight)) / 256;
            result |= channel << shift;
        }
        return result;
    }

    public static void oracle(int[] raster, int rasterWidth, int rasterHeight,
            int left, int top, int right, int bottom, int kind,
            int x, int y, int width, int height, int color, int alpha) {
        for (int py = 0; py < rasterHeight; py++) for (int px = 0; px < rasterWidth; px++) {
            if (px < left || px >= right || py < top || py >= bottom) continue;
            int coverage = 0;
            if (kind == 0) coverage = px >= x && px < x + width && py >= y && py < y + height ? 1 : 0;
            if (kind == 1) coverage = py == y && px >= x && px < x + width ? 1 : 0;
            if (kind == 2) coverage = px == x && py >= y && py < y + height ? 1 : 0;
            if (kind == 3 || kind == 4) {
                if (px >= x && px < x + width) {
                    if (py == y) coverage++;
                    if (py == y + height - 1) coverage++;
                }
                if (kind == 3) {
                    if (py >= y && py < y + height) {
                        if (px == x) coverage++;
                        if (px == x + width - 1) coverage++;
                    }
                } else if (height >= 3 && py > y && py < y + height - 1) {
                    if (px == x) coverage++;
                    if (px == x + width - 1) coverage++;
                }
            }
            int index = py * rasterWidth + px;
            for (int hit = 0; hit < coverage; hit++)
                raster[index] = alpha < 0 ? color : blend(color, raster[index], alpha);
        }
    }

    private void expect(int[] expected, int kind, int x, int y, int width, int height, int color, int alpha) {
        oracle(expected, W, H, LEFT, TOP, RIGHT, BOTTOM, kind, x, y, width, height, color, alpha);
        assertArrayEquals(expected, DrawingArea.pixels);
        assertEquals(W, DrawingArea.width); assertEquals(H, DrawingArea.height);
        assertEquals(LEFT, DrawingArea.topX); assertEquals(TOP, DrawingArea.topY);
        assertEquals(RIGHT, DrawingArea.bottomX); assertEquals(BOTTOM, DrawingArea.bottomY);
    }

    @Test public void opaqueLinesUsePixelCoordinatesAndExclusiveClipEdges() {
        for (int x : new int[] {-3, 1, 2, 6, 9, 10, 14})
            for (int y : new int[] {-2, 2, 3, 6, 8, 9, 12})
                for (int length : new int[] {-2, 0, 1, 3, 8, 17}) {
                    int[] expected = target();
                    DrawingArea.drawHorizontalLine(y, 0xfe123456, length, x);
                    expect(expected, 1, x, y, length, 1, 0xfe123456, -1);
                    expected = target();
                    DrawingArea.drawVerticalLine(y, 0xfe987654, length, x);
                    expect(expected, 2, x, y, 1, length, 0xfe987654, -1);
                }
    }

    @Test public void opaqueRectanglesWriteZeroAndFullIntColorsOnlyWithinIntersection() {
        for (int color : new int[] {0, 0x2468ac, 0xfe123456})
            for (int[] r : rectangles()) {
                int[] expected = target();
                DrawingArea.fillRectangle(r[3], r[1], color, r[2], r[0]);
                expect(expected, 0, r[0], r[1], r[2], r[3], color, -1);
            }
    }

    @Test public void rectangleBlendHas256DenominatorAndDiscardsHighBytes() {
        for (int alpha : new int[] {0, 1, 63, 127, 128, 193, 255, 256})
            for (int color : new int[] {0, 0xffffff, 0xa57319c7})
                for (int[] r : rectangles()) {
                    int[] expected = target();
                    DrawingArea.fillBlendedRectangle(color, r[1], r[2], r[3], alpha, r[0]);
                    expect(expected, 0, r[0], r[1], r[2], r[3], color, alpha);
                }
        DrawingArea.initDrawingArea(1, 3, new int[] {0, 0x010101, 0xa5123456});
        DrawingArea.fillBlendedRectangle(0xffffff, 0, 1, 1, 127, 0);
        DrawingArea.fillBlendedRectangle(0xffffff, 0, 1, 1, 128, 1);
        DrawingArea.fillBlendedRectangle(0xffffff, 0, 1, 1, 0, 2);
        assertArrayEquals(new int[] {0x7e7e7e, 0x808080, 0x123456}, DrawingArea.pixels);
    }

    @Test public void opaqueOutlineIsFourStraightEdgesIncludingDegenerateAxes() {
        for (int width : new int[] {0, 1, 2, 5, 15})
            for (int height : new int[] {0, 1, 2, 3, 6, 13})
                for (int[] xy : new int[][] {{3,4}, {0,1}, {9,8}, {10,9}}) {
                    int[] expected = target();
                    DrawingArea.drawRectangleOutline(xy[1], height, 0xfe145fa3, xy[0], width);
                    expect(expected, 3, xy[0], xy[1], width, height, 0xfe145fa3, -1);
                }
    }

    @Test public void blendedOutlineHasSquareCornersAndPreservesRepeatedEdgeCoverage() {
        for (int alpha : new int[] {0, 1, 128, 255, 256})
            for (int width : new int[] {0, 1, 2, 6, 15})
                for (int height : new int[] {0, 1, 2, 3, 6, 13})
                    for (int[] xy : new int[][] {{3,4}, {0,1}, {9,8}, {10,9}}) {
                        int[] expected = target();
                        DrawingArea.drawBlendedRectangleOutline(xy[1], height, alpha, 0x93c721, width, xy[0]);
                        expect(expected, 4, xy[0], xy[1], width, height, 0x93c721, alpha);
                    }
        int[] expected = target();
        DrawingArea.drawBlendedRectangleOutline(4, 5, 128, 0xffffff, 5, 3);
        expect(expected, 4, 3, 4, 5, 5, 0xffffff, 128);
    }

    @Test public void privateBlendedLinesShareAxisClippingWeightsAndRasterStride() throws Exception {
        Method horizontal = DrawingArea.class.getDeclaredMethod("drawBlendedHorizontalLine", int.class, int.class, int.class, int.class, int.class);
        Method vertical = DrawingArea.class.getDeclaredMethod("drawBlendedVerticalLine", int.class, int.class, int.class, int.class, int.class);
        horizontal.setAccessible(true); vertical.setAccessible(true);
        for (int alpha : new int[] {0, 1, 128, 255, 256})
            for (int[] xy : new int[][] {{-1,3}, {2,-1}, {3,4}, {9,8}, {10,9}})
                for (int length : new int[] {-1, 0, 1, 4, 20}) {
                    int[] expected = target();
                    horizontal.invoke(null, 0xfe1234ef, length, xy[1], alpha, xy[0]);
                    expect(expected, 1, xy[0], xy[1], length, 1, 0xfe1234ef, alpha);
                    expected = target();
                    vertical.invoke(null, 0xfe1234ef, xy[0], alpha, xy[1], length);
                    expect(expected, 2, xy[0], xy[1], 1, length, 0xfe1234ef, alpha);
                }
    }

    @Test public void randomizedCompositionsAgreeWithCoordinateCoverageModel() {
        Random random = new Random(420042);
        int[] expected = target();
        for (int step = 0; step < 600; step++) {
            int x = random.nextInt(22) - 5, y = random.nextInt(19) - 4;
            int width = random.nextInt(18), height = random.nextInt(15);
            int color = random.nextInt(), alpha = random.nextInt(257), kind = random.nextInt(6);
            if (kind == 0) DrawingArea.fillRectangle(height, y, color, width, x);
            if (kind == 1) DrawingArea.drawHorizontalLine(y, color, width, x);
            if (kind == 2) DrawingArea.drawVerticalLine(y, color, height, x);
            if (kind == 3) DrawingArea.drawRectangleOutline(y, height, color, x, width);
            if (kind == 4) DrawingArea.drawBlendedRectangleOutline(y, height, alpha, color, width, x);
            if (kind == 5) DrawingArea.fillBlendedRectangle(color, y, width, height, alpha, x);
            oracle(expected, W, H, LEFT, TOP, RIGHT, BOTTOM, kind == 5 ? 0 : kind,
                    x, y, width, height, color, kind >= 4 ? alpha : -1);
            assertArrayEquals("composition " + step, expected, DrawingArea.pixels);
        }
    }

    @Test public void emptyClipAndViewportFlagDoNotChangePrimitiveCoverage() {
        for (boolean viewport : new boolean[] {false, true}) {
            ClientSettings.FULL_512PX_VIEWPORT = viewport;
            int[] expected = target();
            DrawingArea.fillBlendedRectangle(0x123456, 1, 14, 12, 128, 0);
            expect(expected, 0, 0, 1, 14, 12, 0x123456, 128);
            for (int[] clip : new int[][] {{4,3,4,8}, {2,5,10,5}}) {
                expected = target();
                DrawingArea.setDrawingArea(clip[3], clip[0], clip[2], clip[1]);
                DrawingArea.fillRectangle(12, 0, 0xff00ff, 13, 0);
                DrawingArea.fillBlendedRectangle(0xffffff, 0, 13, 12, 128, 0);
                DrawingArea.drawRectangleOutline(1, 9, 0x123456, 1, 10);
                DrawingArea.drawBlendedRectangleOutline(1, 9, 128, 0xffffff, 10, 1);
                assertArrayEquals(expected, DrawingArea.pixels);
            }
        }
    }

    private int[][] rectangles() {
        return new int[][] {{3,4,4,3}, {-2,-1,9,7}, {8,7,9,8}, {0,0,20,20},
                {LEFT,TOP,RIGHT-LEFT,BOTTOM-TOP}, {RIGHT,TOP,3,2}, {LEFT,BOTTOM,3,2},
                {0,0,2,3}, {3,4,0,4}, {3,4,5,0}, {3,4,-2,4}, {3,4,5,-2}, {9,8,1,1}};
    }
}
