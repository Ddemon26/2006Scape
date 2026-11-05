import static org.junit.Assert.*;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GlyphMaskTest {
    private static final int W=19,H=15,BG=0x7395b7,INK=0xd34691;
    private int[] savedPixels,savedState;
    private TextDrawingArea font;
    @Before public void bindFixture() {
        savedPixels=DrawingArea.pixels;
        savedState=new int[]{DrawingArea.height,DrawingArea.width,DrawingArea.topX,DrawingArea.topY,
            DrawingArea.bottomX,DrawingArea.bottomY,DrawingArea.scanlineRightLimit,
            DrawingArea.clipRightHalf,DrawingArea.clipBottomHalf};
        font=TextWidthTest.font(false);
        DrawingArea.bindRaster(H,W,new int[W*H]);
    }
    @After public void restoreRaster() {
        DrawingArea.pixels=savedPixels;DrawingArea.height=savedState[0];DrawingArea.width=savedState[1];
        DrawingArea.topX=savedState[2];DrawingArea.topY=savedState[3];DrawingArea.bottomX=savedState[4];
        DrawingArea.bottomY=savedState[5];DrawingArea.scanlineRightLimit=savedState[6];
        DrawingArea.clipRightHalf=savedState[7];DrawingArea.clipBottomHalf=savedState[8];
    }
    private void invoke(String name,Class<?>[] types,Object... args) throws Exception {
        Method m=TextDrawingArea.class.getDeclaredMethod(name,types);m.setAccessible(true);m.invoke(font,args);
    }
    private void solid(byte[] mask,int x,int y,int width,int height,int color) throws Exception {
        invoke("drawGlyphMask",new Class<?>[]{byte[].class,int.class,int.class,int.class,int.class,int.class},
            mask,x,y,width,height,color);
    }
    private void blended(byte[] mask,int x,int y,int width,int height,int color,int opacity) throws Exception {
        invoke("drawBlendedGlyphMask",new Class<?>[]{int.class,int.class,byte[].class,int.class,int.class,int.class,int.class},
            opacity,x,mask,width,y,height,color);
    }
    private void solidRows(int[] dest,byte[] mask,int color,int source,int target,int width,int height,int destSkip,int sourceSkip) throws Exception {
        invoke("copyGlyphMaskRows",new Class<?>[]{int[].class,byte[].class,int.class,int.class,int.class,int.class,int.class,int.class,int.class},
            dest,mask,color,source,target,width,height,destSkip,sourceSkip);
    }
    private void blendRows(byte[] mask,int height,int target,int[] dest,int source,int width,int sourceSkip,int destSkip,int color,int opacity) throws Exception {
        invoke("blendGlyphMaskRows",new Class<?>[]{byte[].class,int.class,int.class,int[].class,int.class,int.class,int.class,int.class,int.class,int.class},
            mask,height,target,dest,source,width,sourceSkip,destSkip,color,opacity);
    }
    // Independent channel oracle: truncate each contribution, sign-extend its packed
    // 24-bit value, then add. The legacy signed shift can set the unused high byte.
    private static int mix(int color,int background,int opacity) {
        int source=0,destination=0;
        for(int shift:new int[]{0,8,16}) {
            source|=((((color>>>shift)&255)*opacity)/256)<<shift;
            destination|=((((background>>>shift)&255)*(256-opacity))/256)<<shift;
        }
        if((source&0x800000)!=0)source|=0xff000000;
        if((destination&0x800000)!=0)destination|=0xff000000;
        return source+destination;
    }
    private static byte[] mask(int width,int height) {
        byte[] m=new byte[width*height];
        byte[] values={0,1,2,127,-128,-1,0};
        for(int i=0;i<m.length;i++)m[i]=values[(i*3+i/width)%values.length];
        return m;
    }
    // Coordinate oracle walks the original source rectangle, never clipped widths/skip arithmetic.
    private int[] expected(byte[] mask,int x,int y,int width,int height,int color,int opacity) {
        int[] result=new int[W*H];Arrays.fill(result,BG);
        for(int row=0;row<height;row++)for(int col=0;col<width;col++) {
            int dx=x+col,dy=y+row;
            if(mask[row*width+col]!=0 && dx>=DrawingArea.topX && dx<DrawingArea.bottomX-1
                && dy>=DrawingArea.topY && dy<DrawingArea.bottomY-1)
                result[dy*W+dx]=opacity<0?color:mix(color,BG,opacity);
        }
        return result;
    }
    @Test public void solidMaskUsesAnyNonzeroByteAsCoverageAndWritesTheEntireColor() throws Exception {
        byte[] values={0,1,2,127,-128,-1};byte[] before=values.clone();
        for(int color:new int[]{0,INK,0xff123456}) {
            Arrays.fill(DrawingArea.pixels,BG);solid(values,4,3,6,1,color);
            assertArrayEquals(expected(values,4,3,6,1,color,-1),DrawingArea.pixels);
        }
        assertArrayEquals(before,values);
    }
    @Test public void solidClippingRetainsSourceRowsAndExistingRightBottomExclusion() throws Exception {
        DrawingArea.setClip(12,3,15,2);
        for(int width:new int[]{1,2,3,4,5,7,8,9}) {
            byte[] m=mask(width,5);byte[] before=m.clone();
            for(int[] xy:new int[][]{{5,4},{1,4},{5,0},{1,0},{12,9},{14,11},{15,12},{-8,4},{5,-8}}) {
                Arrays.fill(DrawingArea.pixels,BG);solid(m,xy[0],xy[1],width,5,INK);
                assertArrayEquals("width="+width+" xy="+Arrays.toString(xy),expected(m,xy[0],xy[1],width,5,INK,-1),DrawingArea.pixels);
            }
            assertArrayEquals(before,m);
        }
        byte[] full=new byte[12];Arrays.fill(full,(byte)1);
        Arrays.fill(DrawingArea.pixels,BG);solid(full,12,9,4,3,INK);
        assertEquals(INK,DrawingArea.pixels[10*W+13]);
        assertEquals(BG,DrawingArea.pixels[10*W+14]);assertEquals(BG,DrawingArea.pixels[11*W+13]);
    }
    @Test public void solidRowsSeparateOffsetsDimensionsAndIndependentRowSkips() throws Exception {
        for(int width=0;width<=11;width++)for(int height:new int[]{0,1,3})
            for(int[] layout:new int[][]{{0,0,0,0},{5,7,0,0},{5,7,3,0},{5,7,0,4},{5,7,3,4}}) {
                int source=layout[0],target=layout[1],ss=layout[2],ds=layout[3];
                byte[] m=mask(17,8);byte[] before=m.clone();
                int[] dest=new int[180];Arrays.fill(dest,BG);int[] expected=dest.clone();
                for(int row=0;row<height;row++)for(int col=0;col<width;col++)
                    if(m[source+row*(width+ss)+col]!=0)expected[target+row*(width+ds)+col]=INK;
                solidRows(dest,m,INK,source,target,width,height,ds,ss);
                assertArrayEquals(expected,dest);assertArrayEquals(before,m);
            }
    }
    @Test public void blendedMasksUseOpacityOver256RatherThanCoverageMagnitudeOrOver255() throws Exception {
        byte[] m={0,1,2,127,-128,-1};
        for(int opacity:new int[]{0,1,64,127,128,192,255,256}) {
            Arrays.fill(DrawingArea.pixels,BG);blended(m,4,3,6,1,INK,opacity);
            assertArrayEquals(expected(m,4,3,6,1,INK,opacity),DrawingArea.pixels);
        }
        assertEquals(0x692348,mix(INK,0,128));
        assertEquals(0xa26da3,mix(INK,BG,128));
        // At 255 the destination still contributes; 256 is the exact full-opacity endpoint.
        assertNotEquals(INK,mix(INK,BG,255)&0xffffff);assertEquals(INK,mix(INK,BG,256)&0xffffff);
        assertEquals(0xffd34691,mix(INK,BG,256));
    }
    @Test public void blendRowsSeparateStridesAndPreservePackedSignedShiftBehavior() throws Exception {
        for(int width=0;width<=9;width++)for(int height:new int[]{0,1,3})
            for(int[] layout:new int[][]{{0,0,0,0},{4,9,0,0},{4,9,5,0},{4,9,0,3},{4,9,5,3}})
                for(int opacity:new int[]{0,1,128,255,256}) {
                    int source=layout[0],target=layout[1],ss=layout[2],ds=layout[3];
                    byte[] m=mask(17,8);byte[] before=m.clone();int[] dest=new int[180];
                    for(int i=0;i<dest.length;i++)dest[i]=0xff000000|((i*0x132537)&0xffffff);
                    int[] expected=dest.clone();
                    for(int row=0;row<height;row++)for(int col=0;col<width;col++) {
                        int di=target+row*(width+ds)+col;
                        if(m[source+row*(width+ss)+col]!=0)expected[di]=mix(0xabd34691,dest[di],opacity);
                    }
                    blendRows(m,height,target,dest,source,width,ss,ds,0xabd34691,opacity);
                    assertArrayEquals(expected,dest);assertArrayEquals(before,m);
                }
        // The two contributions round separately: floor(1/2)+floor(1/2)=0.
        int[] dest={0x010101};blendRows(new byte[]{1},1,0,dest,0,1,0,0,0x010101,128);
        assertEquals(0,dest[0]);
    }
    @Test public void blendedClippingUsesSameMaskAndBoundsAtEveryOpacityEndpoint() throws Exception {
        DrawingArea.setClip(12,3,15,2);
        for(int width:new int[]{1,4,5,8})for(int[] xy:new int[][]{{5,4},{1,4},{5,0},{1,0},{12,9},{14,11},{15,12},{-8,4},{5,-8}})
            for(int opacity:new int[]{0,1,128,255,256}) {
                byte[] m=mask(width,5);Arrays.fill(DrawingArea.pixels,BG);
                blended(m,xy[0],xy[1],width,5,INK,opacity);
                assertArrayEquals(expected(m,xy[0],xy[1],width,5,INK,opacity),DrawingArea.pixels);
            }
    }
    @Test public void emptyOrFullyClippedRectanglesDoNotReadNullMasks() throws Exception {
        for(int[] rectangle:new int[][]{{4,3,0,2},{4,3,2,0},{W+1,3,2,2},{4,H+1,2,2},{-4,3,2,2},{4,-4,2,2}}) {
            Arrays.fill(DrawingArea.pixels,BG);
            solid(null,rectangle[0],rectangle[1],rectangle[2],rectangle[3],INK);
            blended(null,rectangle[0],rectangle[1],rectangle[2],rectangle[3],INK,128);
            for(int p:DrawingArea.pixels)assertEquals(BG,p);
        }
    }
}
