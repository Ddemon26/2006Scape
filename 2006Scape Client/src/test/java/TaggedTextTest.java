import static org.junit.Assert.*;
import java.lang.reflect.*;
import java.util.*;
import org.junit.*;

public class TaggedTextTest {
    private static final int W=160,H=40,BG=0x345678;
    private TextDrawingArea font;
    private int[] saved, state;
    @Before public void setup() {
        saved=DrawingArea.pixels;
        state=new int[]{DrawingArea.width,DrawingArea.height,DrawingArea.topX,DrawingArea.topY,
            DrawingArea.bottomX,DrawingArea.bottomY,DrawingArea.scanlineRightLimit,DrawingArea.clipRightHalf,DrawingArea.clipBottomHalf};
        font=TextWidthTest.font(false); DrawingArea.bindRaster(H,W,new int[W*H]); clear();
    }
    @After public void restore() {
        DrawingArea.pixels=saved; DrawingArea.width=state[0];DrawingArea.height=state[1];
        DrawingArea.topX=state[2];DrawingArea.topY=state[3];DrawingArea.bottomX=state[4];DrawingArea.bottomY=state[5];
        DrawingArea.scanlineRightLimit=state[6];DrawingArea.clipRightHalf=state[7];DrawingArea.clipBottomHalf=state[8];
    }
    private void clear(){Arrays.fill(DrawingArea.pixels,BG);}
    private Field field(String name) throws Exception {Field f=TextDrawingArea.class.getDeclaredField(name);f.setAccessible(true);return f;}
    private boolean strike() throws Exception{return field("strikeThroughEnabled").getBoolean(font);}
    private int tag(String tag) throws Exception {
        Method m=TextDrawingArea.class.getDeclaredMethod("applyTextTag",String.class);m.setAccessible(true);
        try{return (Integer)m.invoke(font,tag);}catch(InvocationTargetException e){throw (Exception)e.getCause();}
    }
    private int[] blank(){int[] p=new int[W*H];Arrays.fill(p,BG);return p;}
    // Independent fixture geometry: A is 3x7 with advance 5, B is 6x7 with advance 8.
    private void glyph(int[] p,int origin,int baseline,int width,int color,int opacity) {
        for(int y=baseline-7;y<baseline;y++)for(int x=origin+1;x<origin+1+width;x++)
            if(x>=DrawingArea.topX && x<DrawingArea.bottomX-1 && y>=DrawingArea.topY && y<DrawingArea.bottomY-1)
                p[y*W+x]=opacity==256?color:blend(color,p[y*W+x],opacity);
    }
    private static int blend(int src,int dst,int opacity) {
        int value=0;
        for(int shift:new int[]{0,8,16}) value+=((((src>>>shift)&255)*opacity/256)+(((dst>>>shift)&255)*(256-opacity)/256))<<shift;
        // The legacy packed routine sign-extends each separately premultiplied red channel.
        if(((src>>>16)&255)*opacity/256>=128)value-=0x1000000;
        if(((dst>>>16)&255)*(256-opacity)/256>=128)value-=0x1000000;
        return value;
    }
    @Test public void paletteIsExactCaseSensitiveAndOnlyStrikeTagsMutateState() throws Exception {
        String[] tags={"red","gre","blu","yel","cya","mag","whi","bla","lre","dre","dbl","or1","or2","or3","gr1","gr2","gr3"};
        int[] rgb={0xff0000,0x00ff00,0x0000ff,0xffff00,0x00ffff,0xff00ff,0xffffff,0,0xff9040,0x800000,0x80,0xffb000,0xff7000,0xff3000,0xc0ff00,0x80ff00,0x40ff00};
        for(int n=0;n<tags.length;n++) {
            assertEquals(tags[n],rgb[n],tag(tags[n]));assertFalse(strike());
            clear();font.drawTaggedText(false,4,0x123456,"@"+tags[n]+"@A",18);
            int[] p=blank();glyph(p,4,18,3,rgb[n],256);assertArrayEquals(tags[n],p,DrawingArea.pixels);
        }
        assertEquals(-1,tag("str"));assertTrue(strike());
        for(String unknown:new String[]{"RED","xyz","@red@","red ","","\u0100xy"}){assertEquals(-1,tag(unknown));assertTrue(strike());}
        assertEquals(-1,tag("end"));assertFalse(strike());
        try{tag(null);fail();}catch(NullPointerException expected){}
    }
    @Test public void inlineTagsChangeFollowingColorWithoutAdvanceAndUnknownTagsAreConsumed() {
        font.drawTaggedText(false,4,0x123456,"A@red@B@xyz@@end@A",18);
        int[] p=blank();glyph(p,4,18,3,0x123456,256);glyph(p,9,18,6,0xff0000,256);glyph(p,17,18,3,0xff0000,256);
        assertArrayEquals(p,DrawingArea.pixels);
        for(String text:new String[]{"@","@red","@re@","@reds@","A@@red","red@"}) {
            clear();font.drawLiteralText(0x123456,text,18,4);int[] literal=DrawingArea.pixels.clone();
            clear();font.drawTaggedText(false,4,0x123456,text,18);assertArrayEquals(text,literal,DrawingArea.pixels);
        }
    }
    @Test public void optionalBlackShadowIsOnePixelRightAndDownAndBaselineIsIndependent() {
        for(boolean shadow:new boolean[]{false,true})for(int x:new int[]{-2,4,154})for(int y:new int[]{3,18,40}) {
            clear();font.drawTaggedText(shadow,x,0xff123456,"A B",y);int[] p=blank();
            if(shadow)glyph(p,x+1,y+1,3,0,256);glyph(p,x,y,3,0xff123456,256);
            if(shadow)glyph(p,x+9,y+1,6,0,256);glyph(p,x+8,y,6,0xff123456,256);
            assertArrayEquals("shadow="+shadow+" x="+x+" y="+y,p,DrawingArea.pixels);
        }
    }
    @Test public void finalStrikeStateDrawsAcrossWholeRunAndResetsEvenForNull() throws Exception {
        font.drawTaggedText(false,4,0x123456,"A@str@ B",18);
        int[] p=blank();glyph(p,4,18,3,0x123456,256);glyph(p,12,18,6,0x123456,256);
        for(int x=4;x<20;x++)p[15*W+x]=0x800000;assertArrayEquals(p,DrawingArea.pixels);assertTrue(strike());
        clear();font.drawTaggedText(false,4,0x123456,"@str@A@end@",18);
        p=blank();glyph(p,4,18,3,0x123456,256);assertArrayEquals(p,DrawingArea.pixels);assertFalse(strike());
        tag("str");font.drawTaggedText(false,4,0,null,18);assertFalse(strike());assertArrayEquals(p,DrawingArea.pixels);
    }
    private static final class ControlledRandom extends Random {
        long observedSeed;int calls;private int[] values;
        ControlledRandom(int... values){this.values=values;}
        @Override public void setSeed(long seed){observedSeed=seed;calls=0;}
        @Override public int nextInt(){return values[calls++];}
    }
    private ControlledRandom random(int... values) throws Exception {
        ControlledRandom r=new ControlledRandom(values);field("jitterRandom").set(font,r);return r;
    }
    @Test public void seededOpacityAndSpacingUseSeparateDrawsIncludingSpacesButNotTags() throws Exception {
        for(int sample:new int[]{0,31,32,-1}) {
            ControlledRandom r=random(sample,0,3,4);
            clear();font.drawSeededJitterText(4,0x123456,"A @red@B",-123,18);
            assertEquals(-123L,r.observedSeed);assertEquals(4,r.calls);
            int opacity=(sample==31 || sample==-1)?223:192;
            int[] p=blank();glyph(p,5,19,3,0,192);glyph(p,4,18,3,0x123456,opacity);
            glyph(p,14,19,6,0,192);glyph(p,13,18,6,0xff0000,opacity);
            assertArrayEquals("sample="+sample,p,DrawingArea.pixels);
        }
    }
    @Test public void jitterReseedsActualGeneratorAndDoesNotResetOrDrawStrike() throws Exception {
        font.drawSeededJitterText(4,0x123456,"AB A",0,18);int[] zero=DrawingArea.pixels.clone();
        clear();font.drawSeededJitterText(4,0x123456,"AB A",1,18);assertFalse(Arrays.equals(zero,DrawingArea.pixels));
        clear();font.drawSeededJitterText(4,0x123456,"AB A",0,18);assertArrayEquals(zero,DrawingArea.pixels);
        clear();font.drawSeededJitterText(4,0x123456,"@str@AB A",0,18);assertTrue(strike());assertArrayEquals(zero,DrawingArea.pixels);
        ControlledRandom r=random(31);font.drawSeededJitterText(4,0,null,Integer.MIN_VALUE,18);assertEquals(0,r.calls);assertTrue(strike());
        font.drawSeededJitterText(4,0,"",Integer.MIN_VALUE,18);assertEquals((long)Integer.MIN_VALUE,r.observedSeed);assertEquals(1,r.calls);assertTrue(strike());
        random(0);font.drawSeededJitterText(4,0,"@end@@xyz@",Integer.MAX_VALUE,18);assertFalse(strike());
    }
    @Test public void skippedUnicodeTagsAndUnsupportedLiteralUnitsKeepTheirBoundaries() throws Exception {
        for(boolean jitter:new boolean[]{false,true}) {
            clear();if(jitter)font.drawSeededJitterText(4,0x123456,"A@\ud83d\ude00x@B",1,18);else font.drawTaggedText(false,4,0x123456,"A@\ud83d\ude00x@B",18);
            int[] actual=DrawingArea.pixels.clone();clear();
            if(jitter)font.drawSeededJitterText(4,0x123456,"AB",1,18);else font.drawTaggedText(false,4,0x123456,"AB",18);
            assertArrayEquals(actual,DrawingArea.pixels);
            try {if(jitter)font.drawSeededJitterText(4,0,"\u0100",0,18);else font.drawTaggedText(false,4,0,"\u0100",18);fail();}
            catch(ArrayIndexOutOfBoundsException expected){}
        }
    }
    @Test public void foregroundFailureIsCaughtOnlyInSolidTaggedRenderer() throws Exception {
        byte[][] masks=(byte[][])field("aByteArrayArray1491").get(font);masks['A']=null;
        font.drawTaggedText(false,4,0x123456,"AB",18);
        int[] p=blank();glyph(p,9,18,6,0x123456,256);assertArrayEquals(p,DrawingArea.pixels);
        try{font.drawTaggedText(true,4,0,"A",18);fail();}catch(NullPointerException expected){}
        try{font.drawSeededJitterText(4,0,"A",0,18);fail();}catch(NullPointerException expected){}
    }
}
