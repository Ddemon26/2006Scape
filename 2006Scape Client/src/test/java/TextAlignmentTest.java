import static org.junit.Assert.*;
import java.lang.reflect.Field;
import java.util.Arrays;
import org.junit.*;

public class TextAlignmentTest {
    private static final int W=160,H=40,BG=0x345678,COLOR=0xff123456;
    private TextDrawingArea font;
    private int[] saved,state;
    @Before public void setup() {
        saved=DrawingArea.pixels;
        state=new int[]{DrawingArea.width,DrawingArea.height,DrawingArea.topX,DrawingArea.topY,
            DrawingArea.bottomX,DrawingArea.bottomY,DrawingArea.scanlineRightLimit,DrawingArea.clipRightHalf,DrawingArea.clipBottomHalf};
        font=TextWidthTest.font(false);DrawingArea.bindRaster(H,W,blank());
    }
    @After public void restore() {
        DrawingArea.pixels=saved;DrawingArea.width=state[0];DrawingArea.height=state[1];
        DrawingArea.topX=state[2];DrawingArea.topY=state[3];DrawingArea.bottomX=state[4];DrawingArea.bottomY=state[5];
        DrawingArea.scanlineRightLimit=state[6];DrawingArea.clipRightHalf=state[7];DrawingArea.clipBottomHalf=state[8];
    }
    private int[] blank(){int[] p=new int[W*H];Arrays.fill(p,BG);return p;}
    private void clear(){Arrays.fill(DrawingArea.pixels,BG);}
    private Field field(String name) throws Exception {Field f=TextDrawingArea.class.getDeclaredField(name);f.setAccessible(true);return f;}
    // Independent rectangular geometry from the shared genuine constructor archive.
    private void ink(int[] p,int origin,int baseline,int width,int color) {
        for(int y=baseline-7;y<baseline;y++)for(int x=origin+1;x<origin+1+width;x++)
            if(x>=DrawingArea.topX && x<DrawingArea.bottomX-1 && y>=DrawingArea.topY && y<DrawingArea.bottomY-1)
                p[y*W+x]=color;
    }
    private void ab(int[] p,int x,int y,boolean shadow,int secondColor) {
        if(shadow)ink(p,x+1,y+1,3,0);ink(p,x,y,3,COLOR);
        if(shadow)ink(p,x+6,y+1,6,0);ink(p,x+5,y,6,secondColor);
    }
    private void draw(int kind,String text,int anchor,int baseline,boolean shadow) {
        switch(kind) {
            case 0:font.drawLiteralText(COLOR,text,baseline,anchor);break;
            case 1:font.drawRightAlignedLiteralText(COLOR,text,baseline,anchor);break;
            case 2:font.drawCenteredLiteralText(COLOR,text,baseline,anchor);break;
            case 3:font.drawCenteredTaggedText(COLOR,anchor,text,baseline,shadow);break;
            case 4:font.drawRightAlignedTaggedText(shadow,anchor,COLOR,text,baseline);break;
            default:throw new AssertionError(kind);
        }
    }
    @Test public void literalPenBaselineColorAndSpaceAdvanceVaryIndependently() {
        for(int x:new int[]{-2,4,151})for(int y:new int[]{3,18,40}) {
            clear();font.drawLiteralText(COLOR,"AB A",y,x);
            int[] p=blank();ab(p,x,y,false,COLOR);ink(p,x+16,y,3,COLOR);
            assertArrayEquals("x="+x+" baseline="+y,p,DrawingArea.pixels);
        }
        clear();font.drawLiteralText(0xabcdef,"A",18,4);
        int[] p=blank();ink(p,4,18,3,0xabcdef);assertArrayEquals(p,DrawingArea.pixels);
        clear();font.drawLiteralText(COLOR,"   ",18,4);assertArrayEquals(blank(),DrawingArea.pixels);
    }
    @Test public void literalAnchorsUseAdvanceWidthsIncludingTrailingSpacesAndTruncateOddHalves() {
        String[] text={"A","B","AB","A B","AB "};
        int[] width={5,8,13,16,16};int[] half={2,4,6,8,8};
        for(int kind:new int[]{1,2})for(int n=0;n<text.length;n++)for(int anchor:new int[]{35,36,90}) {
            clear();draw(kind,text[n],anchor,18,false);int[] p=blank();
            int start=anchor-(kind==1?width[n]:half[n]);
            if(n==1)ink(p,start,18,6,COLOR);
            else {ink(p,start,18,3,COLOR);if(n>=2)ink(p,start+(n==3?8:5),18,6,COLOR);}
            assertArrayEquals("kind="+kind+" text="+text[n]+" anchor="+anchor,p,DrawingArea.pixels);
        }
    }
    @Test public void taggedAnchorsSkipUnknownAndUnicodeTagsAndForwardShadowAndColor() {
        for(int kind:new int[]{3,4})for(boolean shadow:new boolean[]{false,true})
            for(String tag:new String[]{"@red@","@xyz@","@RED@","@\ud83d\ude00x@"}) {
                clear();draw(kind,"A"+tag+"B",80,18,shadow);int[] p=blank();
                ab(p,kind==3?74:67,18,shadow,tag.equals("@red@")?0xff0000:COLOR);
                assertArrayEquals("kind="+kind+" tag="+tag+" shadow="+shadow,p,DrawingArea.pixels);
            }
    }
    @Test public void literalTagCharactersAreInkAndMalformedTaggedSequencesRemainLiteral() {
        for(int kind=0;kind<5;kind++) {
            String text=kind<3?"A@red@B":"A@re@B";
            // Five ordinary 2px glyphs have 4px advances; malformed @re@ has four.
            int count=kind<3?5:4,total=kind<3?33:29;
            int start=80-(kind==0?0:(kind==1||kind==4)?total:total/2);
            clear();draw(kind,text,80,18,false);int[] p=blank();ink(p,start,18,3,COLOR);
            for(int i=0;i<count;i++)ink(p,start+5+i*4,18,2,COLOR);
            ink(p,start+5+count*4,18,6,COLOR);assertArrayEquals("kind="+kind,p,DrawingArea.pixels);
        }
    }
    @Test public void allAnchorsRetainClipEdgesAndBaselineHeightAdjustment() {
        DrawingArea.setClip(29,12,145,6);
        for(int kind=0;kind<5;kind++)for(int start:new int[]{8,136})for(int y:new int[]{10,28,34}) {
            int anchor=start+(kind==1||kind==4?13:kind==2||kind==3?6:0);
            clear();draw(kind,"AB",anchor,y,true);int[] p=blank();ab(p,start,y,kind>=3,COLOR);
            assertArrayEquals("kind="+kind+" start="+start+" baseline="+y,p,DrawingArea.pixels);
        }
        DrawingArea.resetClip();font.anInt1497+=3;
        for(int kind=0;kind<5;kind++) {
            clear();draw(kind,"AB",80,18,false);int[] p=blank();
            ab(p,kind==1||kind==4?67:kind==2||kind==3?74:80,15,false,COLOR);
            assertArrayEquals("font height kind="+kind,p,DrawingArea.pixels);
        }
    }
    @Test public void nullAndEmptyLeavePixelsButOnlyTaggedWrappersResetStrike() throws Exception {
        Field strike=field("strikeThroughEnabled");
        for(int kind=0;kind<5;kind++)for(String text:new String[]{null,""}) {
            clear();strike.setBoolean(font,true);draw(kind,text,80,18,true);
            assertArrayEquals(blank(),DrawingArea.pixels);assertEquals(kind<3,strike.getBoolean(font));
        }
        for(int kind:new int[]{3,4}) {
            clear();draw(kind,"A@str@B",80,18,false);int[] p=blank();int start=kind==3?74:67;
            ab(p,start,18,false,COLOR);for(int x=start;x<start+13;x++)p[15*W+x]=0x800000;
            assertArrayEquals(p,DrawingArea.pixels);assertTrue(strike.getBoolean(font));
        }
    }
    @Test public void liveAdvanceTableSignedHalvesAndOverflowKeepJavaIntegerRules() throws Exception {
        int[] advances=(int[])field("glyphAdvancePixels").get(font);
        int[] widths={5,6,-5,-6};int[] half={2,3,-2,-3};
        for(int kind=1;kind<5;kind++)for(int n=0;n<widths.length;n++) {
            advances['A']=widths[n];clear();draw(kind,"A",80,18,false);int[] p=blank();
            ink(p,80-(kind==1||kind==4?widths[n]:half[n]),18,3,COLOR);assertArrayEquals(p,DrawingArea.pixels);
        }
        advances['A']=Integer.MIN_VALUE;
        for(int kind=1;kind<5;kind++) {
            clear();draw(kind,"A",kind==1||kind==4?Integer.MIN_VALUE:-1073741824,18,false);
            int[] p=blank();ink(p,0,18,3,COLOR);assertArrayEquals(p,DrawingArea.pixels);
        }
        advances['A']=Integer.MAX_VALUE;advances['B']=1;
        for(int kind=1;kind<5;kind++) {
            clear();draw(kind,"AB",kind==1||kind==4?Integer.MIN_VALUE:-1073741824,18,false);
            int[] p=blank();ink(p,0,18,3,COLOR);assertArrayEquals(p,DrawingArea.pixels);
        }
    }
    @Test public void unsupportedUnitsShowMeasurementBeforeDrawingAndLiteralPartialFailure() {
        for(int kind=0;kind<5;kind++) {
            clear();try{draw(kind,"A\u0100",80,18,false);fail("kind="+kind);}
            catch(ArrayIndexOutOfBoundsException expected){}
            int[] p=blank();if(kind==0)ink(p,80,18,3,COLOR);assertArrayEquals(p,DrawingArea.pixels);
        }
    }
}
