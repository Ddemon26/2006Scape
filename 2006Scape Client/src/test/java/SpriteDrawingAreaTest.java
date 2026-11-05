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
                DrawingArea.scanlineRightLimit, DrawingArea.clipRightHalf, DrawingArea.clipBottomHalf};
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
        DrawingArea.scanlineRightLimit = previousState[6];
        DrawingArea.clipRightHalf = previousState[7];
        DrawingArea.clipBottomHalf = previousState[8];
        ClientSettings.FULL_512PX_VIEWPORT = previousViewport;
    }

    @Test
    public void selectsStoredPixelsAndDimensionsWithoutDrawingOrExpanding() {
        int[] oldTarget = new int[42];
        Arrays.fill(oldTarget, 0x654321);
        DrawingArea.bindRaster(6, 7, oldTarget);
        DrawingArea.setClip(4, 2, 5, 1);
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
        assertEquals(2, DrawingArea.scanlineRightLimit);
        assertEquals(1, DrawingArea.clipRightHalf);
        assertEquals(1, DrawingArea.clipBottomHalf);
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
        DrawingArea.fillRectangle(1, 1, 0xabcdef, 1, 2);
        assertArrayEquals(new int[] {0, 0, 0, 0, 0, 0xabcdef}, sprite.pixels);
        sprite.pixels[0] = 0x123456;
        assertEquals(0x123456, DrawingArea.pixels[0]);
        DrawingArea.fillRectangle(1, 2, 0xff0000, 1, 0); // one row beyond bottom
        DrawingArea.fillRectangle(1, 0, 0xff0000, 1, 3); // one column beyond right
        assertArrayEquals(new int[] {0x123456, 0, 0, 0, 0, 0xabcdef}, sprite.pixels);
    }

    @Test
    public void reselectionResetsClipAndReplacesThePreviousTarget() {
        Sprite first = new Sprite(3, 2);
        Sprite second = new Sprite(2, 3);
        first.setAsDrawingArea();
        DrawingArea.fillRectangle(1, 0, 7, 1, 0);
        second.setAsDrawingArea();
        DrawingArea.fillRectangle(1, 2, 9, 1, 1);
        assertArrayEquals(new int[] {7, 0, 0, 0, 0, 0}, first.pixels);
        assertArrayEquals(new int[] {0, 0, 0, 0, 0, 9}, second.pixels);
        DrawingArea.setClip(2, 1, 2, 1);
        first.setAsDrawingArea();
        assertBounds(3, 2);
        DrawingArea.fillRectangle(1, 0, 11, 1, 0);
        assertEquals(11, first.pixels[0]);
        assertEquals(0, second.pixels[0]);
    }

    @Test
    public void viewportSettingOnlyChangesTheExistingHorizontalCenterConvention() {
        Sprite sprite = new Sprite(5, 3);
        ClientSettings.FULL_512PX_VIEWPORT = true;
        sprite.setAsDrawingArea();
        assertBounds(5, 3);
        assertEquals(5, DrawingArea.scanlineRightLimit);
        assertEquals(2, DrawingArea.clipRightHalf);
        assertEquals(1, DrawingArea.clipBottomHalf);
        ClientSettings.FULL_512PX_VIEWPORT = false;
        sprite.setAsDrawingArea();
        assertEquals(4, DrawingArea.scanlineRightLimit);
    }

    @Test
    public void zeroSizedAxesProduceEmptyClipsWithoutAllocatingOrDrawing() {
        for (int[] size : new int[][] {{0, 0}, {0, 3}, {3, 0}, {1, 1}}) {
            Sprite sprite = new Sprite(size[0], size[1]);
            int[] storage = sprite.pixels;
            sprite.setAsDrawingArea();
            assertSame(storage, DrawingArea.pixels);
            assertBounds(size[0], size[1]);
            assertEquals(size[0] - 1, DrawingArea.scanlineRightLimit);
            assertEquals(size[0] / 2, DrawingArea.clipRightHalf);
            assertEquals(size[1] / 2, DrawingArea.clipBottomHalf);
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

    @Test
    public void bindingUsesHeightWidthOrderAndAliasesWithoutClearing() {
        int[] storage = new int[13 * 7 + 5];
        for (int i = 0; i < storage.length; i++) storage[i] = 0xa5000000 + i;
        int[] before = storage.clone();
        DrawingArea.bindRaster(7, 13, storage);
        assertSame(storage, DrawingArea.pixels);
        assertBounds(13, 7);
        assertArrayEquals(before, storage);
        DrawingArea.fillRectangle(1, 5, 0x123456, 1, 11);
        before[11 + 5 * 13] = 0x123456;
        assertArrayEquals(before, storage);
        assertEquals(12, DrawingArea.scanlineRightLimit);
        assertEquals(6, DrawingArea.clipRightHalf);
        assertEquals(3, DrawingArea.clipBottomHalf);
    }

    @Test
    public void clipArgumentsAreBottomLeftRightTopWithExclusivePixelEdges() {
        int[][] clips = {{9,2,11,3},{99,-4,99,-7},{3,0,5,0},{9,6,6,3},
                {4,2,10,4},{1,0,1,0},{11,0,13,0}};
        for (int[] clip : clips) {
            int[] pixels = new int[13 * 11 + 5];
            Arrays.fill(pixels, 0x765432);
            DrawingArea.bindRaster(11, 13, pixels);
            DrawingArea.setClip(clip[0],clip[1],clip[2],clip[3]);
            int left=Math.max(0,clip[1]), top=Math.max(0,clip[3]);
            int right=Math.min(13,clip[2]), bottom=Math.min(11,clip[0]);
            DrawingArea.fillRectangle(30,-8,0x12ab34,30,-8);
            for(int i=0;i<pixels.length;i++) {
                int x=i%13,y=i/13;
                assertEquals(i<143 && x>=left && x<right && y>=top && y<bottom
                        ? 0x12ab34 : 0x765432,pixels[i]);
            }
            assertEquals(left,DrawingArea.topX); assertEquals(top,DrawingArea.topY);
            assertEquals(right,DrawingArea.bottomX); assertEquals(bottom,DrawingArea.bottomY);
            assertEquals(13,DrawingArea.width); assertEquals(11,DrawingArea.height);
        }
    }

    @Test
    public void derivedBoundsUseAbsoluteRightAndBottomRatherThanClipSize() {
        DrawingArea.bindRaster(17,23,new int[391]);
        for(boolean viewport:new boolean[]{false,true}) {
            ClientSettings.FULL_512PX_VIEWPORT=viewport;
            DrawingArea.setClip(13,8,19,6);
            assertEquals(viewport?19:18,DrawingArea.scanlineRightLimit);
            assertEquals(9,DrawingArea.clipRightHalf); assertEquals(6,DrawingArea.clipBottomHalf);
            DrawingArea.setClip(13,2,19,1);
            assertEquals(viewport?19:18,DrawingArea.scanlineRightLimit);
            assertEquals(9,DrawingArea.clipRightHalf); assertEquals(6,DrawingArea.clipBottomHalf);
            DrawingArea.setClip(12,2,18,1);
            assertEquals(viewport?18:17,DrawingArea.scanlineRightLimit);
            assertEquals(9,DrawingArea.clipRightHalf); assertEquals(6,DrawingArea.clipBottomHalf);
            DrawingArea.setClip(11,2,17,1);
            assertEquals(8,DrawingArea.clipRightHalf); assertEquals(5,DrawingArea.clipBottomHalf);
        }
    }

    @Test
    public void resetClipKeepsThePreviouslyComputedBottomHalf() {
        int[] pixels=new int[23*17]; Arrays.fill(pixels,42);
        DrawingArea.bindRaster(17,23,pixels);
        for(boolean viewport:new boolean[]{false,true}) {
            ClientSettings.FULL_512PX_VIEWPORT=viewport;
            DrawingArea.setClip(7,4,15,2);
            DrawingArea.resetClip();
            assertBounds(23,17);
            assertEquals(viewport?23:22,DrawingArea.scanlineRightLimit);
            assertEquals(11,DrawingArea.clipRightHalf);
            assertEquals(3,DrawingArea.clipBottomHalf); // reset deliberately leaves this stale
            assertSame(pixels,DrawingArea.pixels);
            for(int pixel:pixels) assertEquals(42,pixel);
        }
    }

    @Test
    public void clearingIgnoresClipAndPreservesUnusedArrayTailAndState() {
        int[] pixels=new int[13*7+5]; Arrays.fill(pixels,0xfa123456);
        DrawingArea.bindRaster(7,13,pixels); DrawingArea.setClip(5,3,8,2);
        int[] state={DrawingArea.topX,DrawingArea.topY,DrawingArea.bottomX,DrawingArea.bottomY,
                DrawingArea.scanlineRightLimit,DrawingArea.clipRightHalf,DrawingArea.clipBottomHalf};
        DrawingArea.clearRaster();
        for(int i=0;i<pixels.length;i++) assertEquals(i<91?0:0xfa123456,pixels[i]);
        assertArrayEquals(state,new int[]{DrawingArea.topX,DrawingArea.topY,DrawingArea.bottomX,DrawingArea.bottomY,
                DrawingArea.scanlineRightLimit,DrawingArea.clipRightHalf,DrawingArea.clipBottomHalf});
        assertSame(pixels,DrawingArea.pixels);
        DrawingArea.clearRaster();
        for(int i=0;i<pixels.length;i++) assertEquals(i<91?0:0xfa123456,pixels[i]);
        for(int[] size:new int[][]{{0,7},{13,0},{0,0}}) {
            DrawingArea.bindRaster(size[1],size[0],pixels);
            int[] before=pixels.clone(); DrawingArea.clearRaster(); assertArrayEquals(before,pixels);
        }
    }

    @Test
    public void bindingAndClippingRetainExistingInvalidInputBehavior() {
        DrawingArea.bindRaster(7,13,null); assertNull(DrawingArea.pixels); assertBounds(13,7);
        DrawingArea.setClip(-3,19,-2,17);
        assertEquals(19,DrawingArea.topX); assertEquals(17,DrawingArea.topY);
        assertEquals(-2,DrawingArea.bottomX); assertEquals(-3,DrawingArea.bottomY);
        assertEquals(-1,DrawingArea.clipRightHalf); assertEquals(-1,DrawingArea.clipBottomHalf);
        try { DrawingArea.clearRaster(); fail("positive dimensions still dereference null"); }
        catch(NullPointerException expected) { }
        int[] shortStorage={9,8,7}; DrawingArea.bindRaster(2,3,shortStorage);
        try { DrawingArea.clearRaster(); fail("short storage is not validated at bind time"); }
        catch(ArrayIndexOutOfBoundsException expected) { }
        assertArrayEquals(new int[]{0,0,0},shortStorage);
    }

    @Test
    public void scanlineConsumerUsesViewportDependentRightLimitExclusively() throws Exception {
        java.lang.reflect.Method line=Texture.class.getDeclaredMethod("method377",int[].class,
                int.class,int.class,int.class,int.class);
        line.setAccessible(true);
        boolean clipping=Texture.aBoolean1462; int alpha=Texture.anInt1465;
        try {
            Texture.aBoolean1462=true; Texture.anInt1465=0;
            for(boolean viewport:new boolean[]{false,true}) {
                ClientSettings.FULL_512PX_VIEWPORT=viewport;
                for(int right:new int[]{1,2,7,12,13}) {
                    int[] pixels=new int[13*7+5]; Arrays.fill(pixels,42);
                    DrawingArea.bindRaster(7,13,pixels);
                    DrawingArea.setClip(6,3,right,2);
                    line.invoke(null,pixels,13*4,0xabcdef,-2,20);
                    int end=viewport?right:right-1;
                    for(int i=0;i<pixels.length;i++) assertEquals(i/13==4 && i%13<end?0xabcdef:42,pixels[i]);
                }
            }
        } finally { Texture.aBoolean1462=clipping; Texture.anInt1465=alpha; }
    }

    @Test
    public void seededClipSequencesMatchCoordinateMembershipWithoutChangingStorage() {
        java.util.Random random=new java.util.Random(430043L);
        for(boolean viewport:new boolean[]{false,true}) {
            ClientSettings.FULL_512PX_VIEWPORT=viewport;
            int[] pixels=new int[17*13+7]; DrawingArea.bindRaster(13,17,pixels);
            int bottomHalf=6;
            for(int step=0;step<300;step++) {
                Arrays.fill(pixels,42);
                int left,top,right,bottom;
                if(step%7==0) { DrawingArea.resetClip(); left=top=0;right=17;bottom=13; }
                else {
                    left=random.nextInt(18);top=random.nextInt(14);
                    right=left+random.nextInt(23-left);bottom=top+random.nextInt(19-top);
                    DrawingArea.setClip(bottom,left,right,top);
                    right=Math.min(17,right);bottom=Math.min(13,bottom);bottomHalf=bottom/2;
                }
                DrawingArea.fillRectangle(30,-2,0x43,30,-2);
                for(int i=0;i<pixels.length;i++) assertEquals(i<221 && i%17>=left && i%17<right
                        && i/17>=top && i/17<bottom?0x43:42,pixels[i]);
                assertEquals(viewport?right:right-1,DrawingArea.scanlineRightLimit);
                assertEquals(right/2,DrawingArea.clipRightHalf);assertEquals(bottomHalf,DrawingArea.clipBottomHalf);
                assertSame(pixels,DrawingArea.pixels);
            }
        }
    }
}
