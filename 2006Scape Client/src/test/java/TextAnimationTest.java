import static org.junit.Assert.*;
import java.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TextAnimationTest {
    private static final int WIDTH=180, HEIGHT=100, BACKGROUND=0x243648;
    private int[] savedPixels, savedState;
    private TextDrawingArea font;

    @Before public void bindFixture() {
        savedPixels=DrawingArea.pixels;
        savedState=new int[]{DrawingArea.width,DrawingArea.height,DrawingArea.topX,DrawingArea.topY,
            DrawingArea.bottomX,DrawingArea.bottomY,DrawingArea.scanlineRightLimit,
            DrawingArea.clipRightHalf,DrawingArea.clipBottomHalf};
        font=TextWidthTest.font(false);
        DrawingArea.bindRaster(HEIGHT,WIDTH,new int[WIDTH*HEIGHT]);
    }
    @After public void restoreRaster() {
        DrawingArea.pixels=savedPixels; DrawingArea.width=savedState[0]; DrawingArea.height=savedState[1];
        DrawingArea.topX=savedState[2]; DrawingArea.topY=savedState[3]; DrawingArea.bottomX=savedState[4];
        DrawingArea.bottomY=savedState[5]; DrawingArea.scanlineRightLimit=savedState[6];
        DrawingArea.clipRightHalf=savedState[7]; DrawingArea.clipBottomHalf=savedState[8];
    }
    private int[] render(int effect,String text,int centerX,int baselineY,int phase,int age,int color) {
        Arrays.fill(DrawingArea.pixels,BACKGROUND);
        if(effect==1) font.drawCenteredVerticalWave(color,text,centerX,phase,baselineY);
        else if(effect==2) font.drawCenteredWave(centerX,text,phase,baselineY,color);
        else font.drawCenteredShake(age,text,phase,baselineY,centerX,color);
        return DrawingArea.pixels.clone();
    }
    // Independent raster oracle: fixture glyph rectangles and fixed observed landmark offsets.
    // No trigonometry or implementation of the animation is used here.
    private int[] expected(String text,int centerX,int baselineY,int color,int[] dx,int[] dy) {
        int advance=0;
        for(char c:text.toCharArray()) advance+=advance(c);
        int x=centerX-advance/2;
        int[] pixels=new int[WIDTH*HEIGHT]; Arrays.fill(pixels,BACKGROUND);
        for(int n=0;n<text.length();n++) {
            char c=text.charAt(n);
            if(c!=' ') for(int y=baselineY-7+dy[n];y<baselineY+dy[n];y++)
                for(int column=x+1+dx[n];column<x+1+dx[n]+advance(c)-2;column++)
                    if(y>=DrawingArea.topY && y<DrawingArea.bottomY-1 && column>=DrawingArea.topX && column<DrawingArea.bottomX-1)
                        pixels[y*WIDTH+column]=color;
            x+=advance(c);
        }
        return pixels;
    }
    private static int advance(char c) {
        return c=='A'?5:c=='B'?8:c=='W'?11:c=='i'||c==' '?3:c=='I'?6:4;
    }
    @Test public void verticalWaveUsesIndexAndSignedPhaseWithoutMovingColumns() {
        int[][] offsets={{0,2,4},{4,4,2},{-4,-4,-2}};
        int[] phases={0,8,-8};
        for(int n=0;n<phases.length;n++)
            assertArrayEquals(expected("ABA",90,40,0x123456,new int[3],offsets[n]),
                render(1,"ABA",90,40,phases[n],0,0x123456));
    }
    @Test public void twoAxisWaveHasDistinctHorizontalAndVerticalIndexPitches() {
        int[][] xs={{0,0,1},{4,4,4},{-4,-4,-4}};
        int[][] ys={{0,1,3},{4,4,3},{-4,-4,-4}};
        int[] phases={0,8,-8};
        for(int n=0;n<phases.length;n++)
            assertArrayEquals(expected("ABA",90,40,0x876543,xs[n],ys[n]),
                render(2,"ABA",90,40,phases[n],0,0x876543));
    }
    @Test public void shakeAgeChangesAmplitudeAndClampsWhilePhaseMovesThePattern() {
        int[] ages={-8,0,8,40,55,56,57,150,Integer.MAX_VALUE};
        int[][] offsets={{0,4,7},{0,4,6},{0,3,5},{0,1,1},{0,0,0},{0,0,0},{0,0,0},{0,0,0},{0,0,0}};
        for(int n=0;n<ages.length;n++)
            assertArrayEquals("age "+ages[n],expected("ABA",90,40,0x456789,new int[3],offsets[n]),
                render(3,"ABA",90,40,0,ages[n],0x456789));
        assertArrayEquals(expected("ABA",90,40,0x456789,new int[3],new int[]{5,6,5}),
            render(3,"ABA",90,40,1,0,0x456789));
        assertArrayEquals(expected("ABA",90,40,0x456789,new int[3],new int[]{-5,-2,2}),
            render(3,"ABA",90,40,-1,0,0x456789));
    }
    @Test public void anchorsAreLiteralAdvanceCenteredAndBaselineRelativeInPixels() {
        for(int effect=1;effect<=3;effect++) for(int color:new int[]{0,0x123456,0xffabcdef})
            for(int[] anchor:new int[][]{{90,40},{103,49}}) {
                int[] dy=effect==1?new int[]{0,2}:effect==2?new int[]{0,1}:new int[]{0,0};
                assertArrayEquals(expected("AB",anchor[0],anchor[1],color,new int[2],dy),
                    render(effect,"AB",anchor[0],anchor[1],0,56,color));
            }
        // AB has odd advance13: the left origin is center-6, not center-7.
        assertEquals(0x123456,render(1,"AB",90,40,0,0,0x123456)[33*WIDTH+85]);
    }
    @Test public void spacesAdvanceAndOccupyAnimationIndicesButAreNotPainted() {
        assertArrayEquals(expected("A B",90,40,0x123456,new int[3],new int[]{0,2,4}),
            render(1,"A B",90,40,0,0,0x123456));
        assertArrayEquals(expected("A B",90,40,0x123456,new int[]{0,0,1},new int[]{0,1,3}),
            render(2,"A B",90,40,0,0,0x123456));
        assertArrayEquals(expected("A B",90,40,0x123456,new int[3],new int[]{0,4,6}),
            render(3,"A B",90,40,0,0,0x123456));
        for(int effect=1;effect<=3;effect++)
            assertArrayEquals(expected("   ",90,40,123,new int[3],new int[3]),render(effect,"   ",90,40,8,0,123));
    }
    @Test public void tagsAreLiteralGlyphsAndClipUsesTheExistingGlyphBoundaryPolicy() {
        String text="A@red@B";
        assertArrayEquals(expected(text,90,40,0x123456,new int[7],new int[]{0,2,4,4,4,2,0}),
            render(1,text,90,40,0,0,0x123456));
        DrawingArea.setClip(44,88,99,35);
        for(int effect=1;effect<=3;effect++)
            assertArrayEquals(expected("AB",90,40,0x654321,new int[2],
                effect==1?new int[]{0,2}:effect==2?new int[]{0,1}:new int[]{0,4}),
                render(effect,"AB",90,40,0,0,0x654321));
    }
    @Test public void nullEmptyAndUnsupportedUtf16KeepExistingContracts() {
        int[] empty=new int[WIDTH*HEIGHT]; Arrays.fill(empty,BACKGROUND);
        for(int effect=1;effect<=3;effect++) {
            assertArrayEquals(empty,render(effect,null,Integer.MIN_VALUE,Integer.MAX_VALUE,7,-1,123));
            assertArrayEquals(empty,render(effect,"",90,40,7,-1,123));
            for(String text:new String[]{"\u0100","\ud83d\ude00","A@\u0100xy@B"}) {
                try {render(effect,text,90,40,0,0,123);fail(text);}
                catch(ArrayIndexOutOfBoundsException expected) {assertArrayEquals(empty,DrawingArea.pixels);}
            }
        }
    }
}
