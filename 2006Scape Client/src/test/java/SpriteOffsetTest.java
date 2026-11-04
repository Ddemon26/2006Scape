import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.Locale;
import javax.imageio.ImageIO;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SpriteOffsetTest {
    private int[] previousPixels;
    private int previousWidth, previousHeight, previousTopX, previousTopY;
    private int previousBottomX, previousBottomY, previousCenterX, previousCenterY, previous1387;
    private Locale previousLocale;
    private boolean previousAndroid;

    @Before
    public void isolateRasterAndDesktopMode() {
        previousPixels = DrawingArea.pixels;
        previousWidth = DrawingArea.width;
        previousHeight = DrawingArea.height;
        previousTopX = DrawingArea.topX;
        previousTopY = DrawingArea.topY;
        previousBottomX = DrawingArea.bottomX;
        previousBottomY = DrawingArea.bottomY;
        previousCenterX = DrawingArea.centerX;
        previousCenterY = DrawingArea.centerY;
        previous1387 = DrawingArea.anInt1387;
        previousLocale = Locale.getDefault();
        previousAndroid = ClientSettings.ANDROID;
        Locale.setDefault(Locale.ENGLISH);
        ClientSettings.ANDROID = false;
    }

    @After
    public void restoreRasterAndDesktopMode() {
        DrawingArea.pixels = previousPixels;
        DrawingArea.width = previousWidth;
        DrawingArea.height = previousHeight;
        DrawingArea.topX = previousTopX;
        DrawingArea.topY = previousTopY;
        DrawingArea.bottomX = previousBottomX;
        DrawingArea.bottomY = previousBottomY;
        DrawingArea.centerX = previousCenterX;
        DrawingArea.centerY = previousCenterY;
        DrawingArea.anInt1387 = previous1387;
        Locale.setDefault(previousLocale);
        ClientSettings.ANDROID = previousAndroid;
    }

    @Test
    public void blankAndDesktopDecodedImagesStartAtZeroOffset() throws Exception {
        Sprite blank = new Sprite(2, 3);
        assertEquals(0, blank.offsetX);
        assertEquals(0, blank.offsetY);
        BufferedImage image = new BufferedImage(2, 3, BufferedImage.TYPE_INT_RGB);
        image.setRGB(1, 2, 0xff0000);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(image, "png", bytes);
        blank.offsetX = 7;
        blank.offsetY = 9;
        DesktopHooks.decodeImage(blank, bytes.toByteArray(), null);
        assertEquals(0, blank.offsetX);
        assertEquals(0, blank.offsetY);
        assertEquals(2, blank.width);
        assertEquals(3, blank.height);
        assertEquals(0xff0000, blank.pixels[5] & 0xffffff);
    }

    @Test
    public void archiveStoresUnsignedHorizontalThenVerticalPixelOffsets() {
        Sprite first = new Sprite(archive(), "offset", 0);
        assertEquals(2, first.offsetX);
        assertEquals(3, first.offsetY);
        assertEquals(2, first.width);
        assertEquals(2, first.height);
        assertEquals(260, first.trimWidth);
        assertEquals(260, first.trimHeight);
        assertArrayEquals(new int[] {0xff0000, 0, 0x00ff00, 0xff0000}, first.pixels);
        Sprite second = new Sprite(archive(), "offset", 1);
        assertEquals(255, second.offsetX);
        assertEquals(254, second.offsetY);
        int[] raster = raster(260, 260, 0);
        second.drawSprite(0, 0);
        int[] expected = new int[260 * 260];
        expected[66295] = 0x00ff00; // Pixel (255,254), not signed bytes or swapped axes.
        assertArrayEquals(expected, raster);
    }

    @Test
    public void eachOffsetMovesOnlyItsOwnAxisInEveryOffsetAwareDrawPath() {
        for (int mode = 0; mode < 4; mode++) {
            for (int[] offset : new int[][] {{0, 0}, {1, 0}, {0, 2}, {1, 2}}) {
                Sprite sprite = new Sprite(1, 1);
                sprite.pixels[0] = 0x804020;
                sprite.offsetX = offset[0];
                sprite.offsetY = offset[1];
                int[] actual = raster(7, 7, 0x204060);
                draw(sprite, mode, 2, 1, null);
                int[] expected = new int[49];
                Arrays.fill(expected, 0x204060);
                expected[(1 + offset[1]) * 7 + 2 + offset[0]] = mode == 2 ? 0x504040 : 0x804020;
                assertArrayEquals("mode=" + mode + " offsets=" + Arrays.toString(offset), expected, actual);
            }
        }
    }

    @Test
    public void offsetsAreAppliedBeforeClippingAtEachRasterBoundary() {
        for (int mode = 0; mode < 4; mode++) {
            Sprite sprite = new Sprite(1, 1);
            sprite.pixels[0] = 0x804020;
            sprite.offsetX = 2;
            sprite.offsetY = 3;
            // Final pixel positions: four corners, then one pixel beyond each edge.
            int[][] positions = {{0,0}, {4,0}, {0,4}, {4,4}, {-1,0}, {5,0}, {0,-1}, {0,5}};
            for (int[] position : positions) {
                int[] actual = raster(5, 5, 0);
                draw(sprite, mode, position[0] - 2, position[1] - 3, null);
                int[] expected = new int[25];
                if (position[0] >= 0 && position[0] < 5 && position[1] >= 0 && position[1] < 5) {
                    expected[position[1] * 5 + position[0]] = mode == 2 ? 0x402010 : 0x804020;
                }
                assertArrayEquals(expected, actual);
            }
        }
    }

    @Test
    public void placementIsIndependentOfTransparencyAndDestinationMask() {
        for (int mode = 0; mode < 4; mode++) {
            Sprite sprite = new Sprite(archive(), "offset", 0);
            int[] actual = raster(6, 7, 0x204060);
            byte[] mask = new byte[42];
            mask[27] = 1; // Blocks green at destination (3,4) only in masked path.
            draw(sprite, mode, 1, 0, mask);
            int[] expected = new int[42];
            Arrays.fill(expected, 0x204060);
            expected[21] = mode == 2 ? 0xff8f2030 : 0xff0000;
            expected[22] = mode == 1 ? 0 : 0x204060;
            expected[27] = mode == 3 ? 0x204060 : mode == 2 ? 0x109f30 : 0x00ff00;
            expected[28] = mode == 2 ? 0xff8f2030 : 0xff0000;
            assertArrayEquals(expected, actual);
        }
    }

    @Test
    public void expansionPlacesPixelsWithinCanvasAndResetsBothOffsets() {
        Sprite sprite = new Sprite(2, 2);
        sprite.pixels = new int[] {1, 2, 3, 4};
        sprite.trimWidth = 5;
        sprite.trimHeight = 6;
        sprite.offsetX = 2;
        sprite.offsetY = 3;
        sprite.expandToCanvas();
        assertArrayEquals(new int[] {0,0,0,0,0, 0,0,0,0,0, 0,0,0,0,0,
                0,0,1,2,0, 0,0,3,4,0, 0,0,0,0,0}, sprite.pixels);
        assertEquals(5, sprite.width);
        assertEquals(6, sprite.height);
        assertEquals(0, sprite.offsetX);
        assertEquals(0, sprite.offsetY);
    }

    private static void draw(Sprite sprite, int mode, int x, int y, byte[] mask) {
        if (mode == 0) sprite.drawSprite(x, y);
        else if (mode == 1) sprite.method346(x, y);
        else if (mode == 2) sprite.drawSprite1(x, y);
        else {
            Background background = new Background(archive(), "offset", 0);
            background.aByteArray1450 = mask == null ? new byte[DrawingArea.width * DrawingArea.height] : mask;
            sprite.method354(background, y, x); // This entry point takes y before x.
        }
    }

    private static int[] raster(int width, int height, int fill) {
        int[] pixels = new int[width * height];
        Arrays.fill(pixels, fill);
        DrawingArea.initDrawingArea(height, width, pixels);
        return pixels;
    }

    private static StreamLoader archive() {
        // Independent archive: 260x260 canvas; 2x2 at (2,3), 1x1 at (255,254).
        return new StreamLoader(Base64.getDecoder().decode(
                "AAA2AABMMUFZJlNZMu7A6wAAB+JR/IQAIAACEAIBAAAAgAGgAFCmAABqemkNNNNDbTscWbaIeGcCixwYlaIDpBeMkVe1faQ+LuSKcKEgZd2B1g=="));
    }
}
