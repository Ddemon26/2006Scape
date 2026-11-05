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


    @Test
    public void archiveMemberAndLayoutExpandPaletteIntoRowMajorRgbPixels() {
        int fixture = 0;
        int[] palette = {0,1,0x923e26,0xdb5d39,0x247c4c};
        for (int width = 0; width <= 4; width++) for (int height = 0; height <= 3; height++)
            for (int layout = 0; layout <= 1; layout++) {
                StreamLoader archive = constructorArchive(CONSTRUCTOR_FIXTURES[fixture++]);
                Sprite first = new Sprite(archive,"sheet",0);
                assertEquals(1,first.width); assertEquals(3,first.height);
                assertArrayEquals(new int[] {1,0x923e26,0xdb5d39},first.pixels);
                Sprite selected = new Sprite(archive,"sheet",1);
                assertEquals(width,selected.width); assertEquals(height,selected.height);
                assertEquals(513,selected.trimWidth); assertEquals(770,selected.trimHeight);
                assertEquals(255,selected.offsetX); assertEquals(254,selected.offsetY);
                int[] expected = new int[width*height];
                for(int y=0;y<height;y++) for(int x=0;x<width;x++)
                    expected[y*width+x]=palette[(x+3*y)%5];
                assertArrayEquals(expected,selected.pixels);
                int[] actual=raster(width+2,height+2,0x204060);
                int[] drawn=actual.clone();
                for(int y=0;y<height;y++) for(int x=0;x<width;x++)
                    if(expected[y*width+x]!=0)drawn[(y+1)*(width+2)+x+1]=expected[y*width+x];
                selected.drawSprite(1-selected.offsetX,1-selected.offsetY);
                assertArrayEquals(drawn,actual);
                Sprite last=new Sprite(archive,"sheet",2);
                assertEquals(2,last.width); assertEquals(1,last.height);
                assertEquals(4,last.offsetX); assertEquals(5,last.offsetY);
                assertArrayEquals(new int[] {0xdb5d39,1},last.pixels);
                assertArrayEquals(first.pixels,new Sprite(archive,"sheet",-1).pixels);
            }
    }

    @Test
    public void archiveUnsignedIndicesRgb24AndZeroPaletteEntriesHaveDistinctTransparency() {
        for(String encoded:BYTE_INDEX_FIXTURES) {
            Sprite selected=new Sprite(constructorArchive(encoded),"sheet",1);
            assertEquals(65535,selected.trimWidth); assertEquals(65534,selected.trimHeight);
            assertEquals(16,selected.width); assertEquals(16,selected.height);
            for(int i=0;i<256;i++) {
                int entry=i%255;
                int expected=entry==0?0:entry==1?1:((entry*73&255)<<16)|((entry*31&255)<<8)|(entry*19&255);
                assertEquals(expected,selected.pixels[i]);
            }
            int[] actual=raster(18,18,0x204060),expected=actual.clone();
            for(int i=0;i<256;i++)if(selected.pixels[i]!=0)expected[(i/16+1)*18+i%16+1]=selected.pixels[i];
            selected.drawSprite(1-selected.offsetX,1-selected.offsetY);
            assertArrayEquals(expected,actual);
            assertEquals(0x204060,actual[19]); // Palette slot zero is transparent.
            assertEquals(1,actual[20]); // Encoded black in a nonzero slot becomes drawable RGB 1.
        }
    }

    @Test
    public void archiveUnknownStorageOrderLeavesZeroPixelsAndOneEntryPaletteIsValid() {
        Sprite selected=new Sprite(constructorArchive(UNKNOWN_LAYOUT_FIXTURE),"sheet",1);
        assertEquals(3,selected.width); assertEquals(2,selected.height);
        assertArrayEquals(new int[6],selected.pixels);
        Sprite single=new Sprite(constructorArchive(SINGLE_PALETTE_FIXTURE),"sheet",1);
        assertArrayEquals(new int[6],single.pixels);
    }

    @Test(expected=ArrayIndexOutOfBoundsException.class)
    public void archiveIndex255IsNotAValidSlotInA255EntryPalette() {
        new Sprite(constructorArchive(INVALID_INDEX_FIXTURE),"sheet",1);
    }

    private static StreamLoader constructorArchive(String encoded) {
        return new StreamLoader(Base64.getDecoder().decode(encoded));
    }

    private static final String[] CONSTRUCTOR_FIXTURES = {
        "AABeAAB/MUFZJlNZkw9C/AAACN/y/pJlQgAhAyRQQgOlkIQAChAAQUAACAABoABUYAADQGgyaeoYyQin6KNNAAAAAAehQSFfzpxqcILWZXAmOQVa20gINyBCQ2TM4k8BExST7FK8oe09CbLAAcsGLY0S8H4DmgEmuz/F3JFOFCQkw9C/AA==",
        "AABeAACAMUFZJlNZyK6eNgAACV/y/pJlQgAhAyRQQgOlkIQAChAAQUAACAABoABUVAAA0BoGmnqG00Qin6KNNAAAAAAeglBIV/OnGpwgtZlcCY5BVrbSAg3IEJDZMziTwEYVJRq2Qyi7T0R8bwBywFYsJvhfAg75NY7L8XckU4UJDIrp42A=",
        "AABeAAB+MUFZJlNZYQ/omgAACF/y/pJlQgAhAyRQQgOlkIQAChAAQUAACAABoABUYAADQGgyaeoYyQiekTTQAAAAAHqzTCz5041uEGMMC4EjkIrY3ASm5AkkmoyER+Ai6Uk+xSvKHtPQmywAHLBi2NEvB+A5oBJrs/xdyRThQkGEP6Jo",
        "AABeAAB/MUFZJlNZOq40UAAACF/y/pJlQgAhAyRQQgOlkIQAChAAQUAACAABoABUYAADQGgyaeoYyQintKNNAAAAAAeqCQr+dEanCNawVxkxybFa21QE0JDQkDIAiTwyLpST7FK8oe09CbLAAcsGLY0S8H4DmgEmuz/F3JFOFCQOq40UAA==",
        "AABeAAB/MUFZJlNZc88LhwAACF/y/pJlQgAhAyRQQgOlkIQAChAAQUAACAABoABUYAADQGgyaeoYyQap5EPUAAAAAB60aoU+dONpwIaEoriDHJCtGYAtspDUFGswUR+EAxST7FK8oe09CbLAAcsGLY0S8H4DmgEmuz/F3JFOFCQc88LhwA==",
        "AABeAAB+MUFZJlNZKG7XTQAACF/y/pJlQgAhAyRQQgOlkIQAChAAQUAACAABoABUYAADQGgyaeoYyQapvUQ0AAAAAB60Uwr+dMbTgWVoFyAxygVrbEAU3ISkEqJMSfEAMUk+xSvKHtPQmywAHLBi2NEvB+A5oBJrs/xdyRThQkChu100",
        "AABeAAB/MUFZJlNZgc+h4QAACF/y/pJlQgAhAyRQQgOlkIQAChAAQUAACAABoABUYAADQGgyaeoYyQin6RNNNAAAAAB6o0Qr+YcanSWawwXAmuQVbq1ElyQIQAaTIRH4CJikn2KV5Q9p6E2WAA5YMWxol4PwHNAJNdn+LuSKcKEhA59Dwg==",
        "AABeAAB+MUFZJlNZ2m59KwAACF/y/pJlQgAhAyRQQgOlkIQAChAAQUAACAABoABUYAADQGgyaeoYyQiYk09QAAAAAHrCOFPnSjY4CEghcIK5EK0bgUtuQTMRJZEUR+CAdKSfYpXlD2noTZYADlgxbGiXg/Ac0Ak12f4u5IpwoSG03PpW",
        "AABeAACAMUFZJlNZaqPFEgAACF/y/pJlQgAhAyRQQgOlkIQAChAAQUAACAABoABUYAA0NAaBoeoek8kImU0aeoaaAAAAGguiS1NMmOtMDNKLxBHQosNSwSqUXAcJJALC9EA70WrM43OS6XwSb7QBogwWfEfhCCkvEZNw/F3JFOFCQaqPFEg=",
        "AABeAACAMUFZJlNZMQIZ2AAACF/y/pJlQgAhAyRQQgOlkIQAChAAQUAACAABoABUYAADQGgaaeoek8kImSGnqGmgAAABo6QLU0yYppgRmQXiCOhRYakrAVSikEk4RExPogHa1J0ONxZdK5JGFwA0QYLPiPwhBKHiMm4/i7kinChIGIEM7AA=",
        "AABfAACAMUFZJlNZ7oyicQAACF/y/tJlQgAhAyRQQgOlkIQAChAAQUAACAABoABUYAA0NAAND1DankhE0EwgaAAAAGiCxKcxjVLcK2PlQN5SE6EAsYpIHTKJjnAwgVvpiB3orLM4WuS6VwSbawBiwFY/EfZejilwjJl34u5IpwoSHdGUTiA=",
        "AABfAAB/MUFZJlNZtS1+uwAACF/y/tJlQgAhAyRQQgOlkIQAChAAQUAACAABoABUYAA0NAAND1DankhEyEYgaAAAAAkoVfMY1S/hWx4k3CY3IgFbFOOgkpBOhAHESb4gwdZqyzOFrkulcEm2sAYsBWPxH2Xo4pcIyZd+LuSKcKEhalr9dg==",
        "AABgAACAMUFZJlNZd1fSvwAACF/y/rJlQgAhAyRQQgOlkIQAChAAQUAACAABoABUYAA0NAAND1DankhWQRp6gaAAAAGijxLaRMW80SIz6k3nAOhCwlOGS6Mp0JFKBBDPUAbvRWWZwtcl0rgk21gDFgKx+I+y9HFLhGTLvxdyRThQkHdX0r8=",
        "AABgAACBMUFZJlNZLPYOdQAACF/y/rJlQgAhAyRQQgOlkIQAChAAQUAACAABoABUYAADQADTT1DankhE0EaeoGgAAABoo8S2kTFvNEiB9V4mN0JrCWDgJdlOdCRQgQFDPUmDtSc9DhaUXSeSRdYAMWArH4j7L0ckOEZMv/F3JFOFCQLPYOdQ",
        "AABhAACAMUFZJlNZA0FXHwAACF/y/pJlQgAhAyRQQgOlkIQAChAAQUAACAABoABUVAAAGgBpp6htTyQiaCZpAAAAAGijuiICjWxwkTRUe85jdCaxqmCCVyihKQQIiB4/UGAVqz0OJCy6VyVqbgBrAFg/EfhMRlY6Rm1P4u5IpwoSAGgq4+A=",
        "AABhAAB/MUFZJlNZWOCL1QAACF/y/pJlQgAhAyRQQgOlkIQAChAAQUAACAABoABUVAAAGgBpp6h6TyQrakyYQAAAAAJP9CwzIlL+DGtMm4EXIKtrqwSFSBxCDOAAHi8QRulqz0OJCy6VyVqbgBogwWfEfhCjKx0jNqfxdyRThQkFjgi9UA==",
        "AABeAAB+MUFZJlNZZJdQlwAACF/y/pJlQgAhAyRQQgOlkIQAChAAQUAACAABoABUYAADQGgaaeoek8kGpkQyAAAAAAbSUwOfMOJpY+YxSLiCOSFbFwBQckJISajBRH4QHS1J0ONxZdK5JGFwA0QYLPiPwhBKHiMm4/i7kinChIMkuoS4",
        "AABeAAB/MUFZJlNZPzaMXQAACF/y/pJlQgAhAyRQQgOlkIQAChAAQUAACAABoABUYAADQGgaaeoeU9EGqeKGmgAAAAANIkCvlnE4oF8taC4gxyQrW4kAIKkJAhsmZxJ4QBLUnRkbiz6VyaMLgBogQl8bPDEEoeNhtx/F3JFOFCQPzaMXQA==",
        "AABgAAB/MUFZJlNZ+WPlEQAACF/y/rJlQgAhAyRQQgOlkIQAChAAQUAACAABoABUYAA0NAAND1DankhWJNGagAAAAAKKBfxnEtgGfzqhuIA5EKzJjEE4MhJaJoRQUXiQR1mrLM4WuS6VwSbawBiwFY/EfZejilwjJl34u5IpwoSHyx8oiA==",
        "AABgAACAMUFZJlNZosI52wAACF/y/rJlQgAhAyRQQgOlkIQAChAAQUAACAABoABUYAA0NAAND1DankhWKZDagaAAAAAooXfjNjqwM/nlXCAciFZkttaGiGQmkkkREmeJBHWasszha5LpXBJtrAGLAVj8R9l6OKXCMmXfi7kinChIUWEc7YA=",
        "AABiAACCMUFZJlNZlYPg/AAACF/y/pplQgAhAyRQQgOlkIQAChAAQUAACAABoABUVAAAAANNNDynkhFPRpGnqBoAAAACkfLiM2LdAz+S8pFyGmuRCs1JdAXBCRCSYogEhIvEtIFSc9DgQouk8lVdYtbAWID8R9kIZVtkZsv/F3JFOFCQlYPg/A==",
        "AABiAACDMUFZJlNZDG9H2wAACF/y/pplQgAhAyRQQgOlkIQAChAAQUAACAABoABUVAAA0ADTTTQ2p5IVP1BNMQAAAABogrXolrjeURTSsYi8OOdAli0pAIdIlBTHNGDgEMb07hgUnTUojKHpPInvtWtrFgwPhLsQZmd0SJuH4u5IpwoSAY3o+2A=",
        "AABkAACEMUFZJlNZepxqvQAACF/y/pJlQgAhAyRQQgOlkIQAChAAQUAACAABoABUVAAAGhoGmmhtMkIp6aI09QaAAAAGikNXGZsW+Bm0oFKvQ450IWGxaXgFYyoAkpESASElDC8YFJRqUhlD2nobj4LW5iwYDRL0Wgi9+Aicf8XckU4UJB6nGq9A",
        "AABkAACCMUFZJlNZX76zSAAACF/y/pJlQgAhAyRQQgOlkIQAChAAQUAACAABoABUVAAA0aGgGmhtMkImk2pggAAAAGhHcqJjI1LBbmvNK8TG6ECsYlKDEcZTyIIgEUCIHR7E8YFcp0KQyh756E6L1rcxYMBol6LQSd+Aicj8XckU4UJBfvrNIA==",
        "AABeAACAMUFZJlNZnTvXeQAACF/y/pJlQgAhAyRQQgOlkIQAChAAQUAACAABoABUYAADQGgaaeoek8kImmiMhpoAAAAaEkiY+5Z2pWCnLQa8QN0IWLUwEPyiRCJ0HAWn9EA7WpOhxuLLpXJIwuAGiDBZ8R+EIJQ8Rk3H8XckU4UJCdO9d5A=",
        "AABeAACAMUFZJlNZxpoLswAACF/y/pJlQgAhAyRQQgOlkIQAChAAQUAACAABoABUYAADQGgaaeoeU9EIp4oHqGmgAAABo42anmIS30AUYhheINdCFiPmoBelEzESbIiWfogBLUnRkbiz6VyaMLgBogQl8bPDEEoeNhtx/F3JFOFCQxpoLsw=",
        "AABhAACAMUFZJlNZI2dbUgAACF/y/pJlQgAhAyRQQgOlkIQAChAAQUAACAABoABUVAAAGgBpp6htTyQiZJoekGgAAABoomxmBsefAwNq1DeSEdCixZGYE9KIJS4ApECZ6lACtWehxIWXSuStTcANYAsH4j8JiMrHSM2p/F3JFOFCQI2dbUg=",
        "AABhAACAMUFZJlNZeMaHmAAACF/y/pJlQgAhAyRQQgOlkIQAChAAQUAACAABoABUVAAAGgBpp6htTyQiZJoekGgAAABoomxmBsefAwNq1XihHQosWRumCSlEEpcAUiJM9cQArVnocSFl0rkrU3ADWALB+I/CYjKx0jNqfxdyRThQkHjGh5g=",
        "AABkAACDMUFZJlNZtY6XqwAACN/y/pJlQgAhAyRQQgOlkIQAChAAQUAACAABoABUVAAAGhoGmmhtMkIjSZM1NAAAAAaJFy4iKNbAIjua1SrxMbo4WQClgC+eUQQ88ZAig4s9heMCko1KQyh7T0Nx8FrcxYMBol6LQRe/AROP+LuSKcKEhax0vVg=",
        "AABkAACCMUFZJlNZP34rPgAACN/y/pJlQgAhAyRQQgOlkIQAChAAQUAACAABoABUVAAA0aGgGmhtMkImmiMQaAAAAGiTuVGxjVLBbHCUCbxMboQsY8+Igh9spEg6RMQiB0+xPGBXKdCkMoe+ehOi9a3MWDAaJei0EnfgInI/F3JFOFCQP34rPg==",
        "AABnAACIMUFZJlNZkUA2MAAACP/y/pJAAGVCACEDJFBCA6WQhAAKEABBQAAIAAGgAFRU0AAAANNNNDankhEaJpptQAAAABo4LVxEyNbwERNF9Q3iY3RwsIQYoBdlOZCUgJ0IAUHdmF5wDmUnTUoDKHpPI132rW1iwYHwl2IMzsOJE3D8XckU4UJCRQDYwA==",
        "AABnAACHMUFZJlNZ0OGx9AAACP/y/pJAAGVCACEDJFBCA6WQhAAKEABBQAAIAAGgAFRjRoADQADTTQ2p5IRNDUaZBoAAAABH+VExbVPA98wGJK4TG5ECrW46kxHRI1BJIxxQICCDid0DOKTnoUDih6TyKLrFraxYMD4S7EGZIMJE2/8XckU4UJDQ4bH0",
        "AABeAACAMUFZJlNZeP57nQAAB9/y/pJlQgAhAyRQQgOlkIQAChAAQUAACAABoABUYAADQGgaaeoek8kIp6aI9IGgAAABolRFE81aGKWBmTRC8Qc6ELFnGCTlEgJCUOCxHogHa1J0ONxZdK5JGFwA0QYLPiPwhBKHiMm4/i7kinChIPH89zo=",
        "AABeAACAMUFZJlNZI1+nVwAAB9/y/pJlQgAhAyRQQgOlkIQAChAAQUAACAABoABUYAADQGgaaeoeU9EIp4po9IGgAAAAKUcLvyyYiwFaF8Nwg1yIVrQ4gFJBMDJsgImH4IAS1J0ZG4s+lcmjC4AaIEJfGzwxBKHjYbcfxdyRThQkCNfp1cA=",
        "AABiAAB/MUFZJlNZF6TN5QAAB9/y/pplQgAhAyRQQgOlkIQAChAAQUAACAABoABUYAADQADTTQ8p5IVpkgzUAAAAADQ/OwsZE6tBmZtfDcQBykKzFi0C5IlBKSoiEEn4QIFSc9DhaUXSeSRdYtbAWID8R9kIZIcIzZf+LuSKcKEgL0mbyg==",
        "AABiAACBMUFZJlNZTAURLwAAB9/y/pplQgAhAyRQQgOlkIQAChAAQUAACAABoABUVAAA0ADTTTQ2p5IRGhGNQAAAAAKH5cLWxLYBERK+uExuTmrIWusBAyHBCTpQgQES8SYwKTpqURlD0nkT32rW1iwYHwl2IMzO6JE3D8XckU4UJBMBREvA",
        "AABmAACFMUFZJlNZDq+ovgAACF/y/pLlQgAhAyRQQgOlkIQAChAAQUAACAABoABUVNAAAADTTTQ2p5IRTyEabUAAAAACi+XCbIlugwzE1fN3Iaa5EKzIFMAljIQISSRJCBEHZRecA5lJ01KAyh6TyNd9q1tYsGB8JdiDM7DiRNw/F3JFOFCQDq+ovg==",
        "AABmAACFMUFZJlNZzMKCpgAACF/y/pLlQgAhAyRQQgOlkIQAChAAQUAACAABoABUY0NAA0AA000NqeSETQRk9QAAAABoo7VxE9GtgMI5Ept5zG6ELCdWIiCVslQhKkAgREBB44wOgZxSc9CO4oek8ii6xa2sWDA+EuxBmSDCRNv/F3JFOFCQzMKCpg==",
        "AABqAACIMUFZJlNZfgh32QAACH/y/pIIAGVCACEDJFBCA6WQhAAKEABBQAAIAAGgAFRj1AAAAA000NqeSFNNEybUAAAAACkfMhJsTHTERIJXyrhOfkD1khbVmEZAOKUtBIpAUAAOPE62INJRnob7SS6RyVV1ilMWoVj8R9kK8qwIzZf+LuSKcKEg/BDvsg==",
        "AABqAACHMUFZJlNZ9DwEIQAACH/y/pIIAGVCACEDJFBCA6WQhAAKEABBQAAIAAGgAFRRGgANAANNNDJ5IU0nlMEAAAAABI+VC1kSlmWxrRL65Bz8gZNbUqExiQ5IxwhJACAgcCnfA5WmIpOegwEUHSeQRdYtbmLaxvwD2DWZIMAi6/8XckU4UJD0PAQh"
    };
    private static final String[] BYTE_INDEX_FIXTURES = {
        "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"
    };
    private static final String UNKNOWN_LAYOUT_FIXTURE = "AABYAABuMUFZJlNZM8krfQAACFfQ/pJgQAMkUEADpZCAAApBQAABoABUY0yGE0ZGmGk9AhDRJppiAAGgaNCCbovbFxQfOEMwZ+AqWBqqjlYCYEsT4GXvCbjAPguXgAM6DStrytXl2l8D+LuSKcKEgZ5JW+g=";
    private static final String SINGLE_PALETTE_FIXTURE = "AABYAABuMUFZJlNZhIqS6QAACFfQ/pJgQAMkUEADpZCAAApBQAABoABUYxGmhiGRkekxkET0Uep6mIAAAaKDEmi9YsUHyhCfI3O8BJMFRAbGSAqx8HF7uewsEIr+eATlSdKiXo1b2crf3F3JFOFCQhIqS6Q=";
    private static final String INVALID_INDEX_FIXTURE = "AARMAAV2MUFZJlNZlzycGwAAB3/////////////////////////////////////////////AAtzjnLW2sKSPUNqeiNPRPJGhoGmjQ9TR6j2qGj1NPU0PKDTyCBoAADQMQyNAG1Gj0mm1AxPUwnqYmnhRppobUzJA09DRGMTQ1P1R6maaniTxqNNJ4GqIBNpNMBMAJiaaMCYNJkxMBMjCYJmmiYEwCMEwRgmAJmiNkE9DQBomaBiQwaQZMJtAEwDSaY0eomGpkYBEmhoaZGj0TGkyHpPU0MnoJgmNMRDTT1MhmoGZRtqT8o1NPapp6jTRpjUBmk2iep6jyRiNHqY1GTQBkZA8QTHqamRmpjTSaZGI0PU0aaZqb1CGU9qE9KejJpNPSaY0GjUzU00yDTTATJgAmEaaMATR6jExMAExMmAmAEaGjAEYExMjAACANAwAAJgJiYEzeAGZChYQwsO4A0BMQ27AAgbwIwMK/EAZhhN4+VWwCx/GDC5uRGmI4kNEFAMLsvdChCpfAOwtZsLkyM4QI0RAozEhnDOuFYIBVifOuPLVeZaC+PFsOyjlOtXcumRzsHq7DyBESECxsuSJd85LE6Jt2GtoX3VP4AxVPlZRsJSlPshZ3BhDWWo5zbvc00iKcIueLIy27Jncr/DKHtdwG9+GXimoqg3tz0Tc1f+lAjV8LsrZECYeitUqoJCwPmDovyX+SeQpZM5SCCKGCWayg7Nggn3FIOlVCV2QLoc3qAyMQhb+2cp+d7xuQHBzIV6Ke20tw7vQD3F3w33efmGjrLrp7kPKWg9gqX4AQDqsLrM+rimtaT641A/NgrBCrmPsIaSJBQSYx0ul780uwYJrzLX1awmGFGKnkxY/RIOE2C3bYqG5JRKroUMQaDk8+iIKbpSiAWOc6YzVJ86jYwYiZbGKmWNNEkA87wqCiLlmbHX1uC+9hP2QATn+SQmBI8AALV2bCRBEH8Q8FeI1+3tn+ewLJz0rCxpK5MHUWuX30va4unAt0w/i3Q+knJ02qaALcZ4VcP+tvT56cbfE9aRwwwzpEOU2Fb9Wdu04sOdzBmDsZoqhtJWxwqJvedB8vbnlRI5uDfU/zNhLSLZjQViwBq+x8bJZLqZCbiODngvuWibXQq9eUUjICi/fEq8BZ565cJIKKIbtEMmXqcR+YbbPxGoiN4cmAdG8sAPEhZVb3fSWQWyWx9Kdbz1UkZ6qksv+JSM9dW4qiF+hnvaN7UmBkSrC6KDkZTWU70hzN3MPzQnCKbGhmjt8SpdOKsByRRUSC67pLZt/VA4LYdj5eJxuuiFw1I97kgC8YvJbzi+bIvidTTHR3MTiwkNw349iUYl16C4cO74mFQX2f5OAULzQagRHcVaq7XjGC+9ZzwT8agBZgSuAxfHc5ik/8KuLIaoIWhsJkoPwgEjpWY+klYV3wJ/TikgzIjeuTxltL7RqRRYSb2QuPMTYt2YBxb80nCDf0URHNhtFClw7WVXxmOIKHHzavEPk29SkfV54CjG7K/XbpGoypTYinHjZNg42Jway07H9E96OmT5AOS7YhwuPMrimoY+zHU2tGdlA+VeRDsmhQkMHV49g2R1egXGkLU13IB3Wp91DWKG9iohLPVipve2znrP6fmid+Q/Gk1X5ccsW6wJ+uLcJdIXHu/xU7KJoSsJOjkOYPrWZm0BfagBWmzcZNzY+lYm7w4DBoQwvAsuTdJWreuzk2Ducwr1tHNB6kTuYLsTTwNT9h/1q1xAZJA1qaKBFCAfr3BgMOhNLLKtSXSq5mS5+PM/g5AqxpbdpFJMiT8wQz2cpkCj0SpNFft/M1UFKZcvX1yJXj36XWL4uWjxuHLrfrbRcn65h7/4u5IpwoSEueTg2";

    private static void draw(Sprite sprite, int mode, int x, int y, byte[] mask) {
        if (mode == 0) sprite.drawSprite(x, y);
        else if (mode == 1) sprite.drawOpaque(x, y);
        else if (mode == 2) sprite.drawHalfBlended(x, y);
        else {
            Background background = new Background(archive(), "offset", 0);
            background.paletteIndices = mask == null ? new byte[DrawingArea.width * DrawingArea.height] : mask;
            sprite.drawNonzeroThroughZeroMask(background, y, x); // This entry point takes y before x.
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
