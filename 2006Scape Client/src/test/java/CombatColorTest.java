import static org.junit.Assert.*;
import java.util.Arrays;
import org.junit.Test;

public class CombatColorTest {
    public static final int[] DELTAS = {-10,-9,-7,-6,-4,-3,-1,0,1,3,4,6,7,9,10};
    public static final String[] TAGS = {"@red@","@or3@","@or3@","@or2@","@or2@","@or1@","@or1@","@yel@","@gr1@","@gr1@","@gr2@","@gr2@","@gr3@","@gr3@","@gre@"};
    public static final int[] RGB = {0xff0000,0xff3000,0xff3000,0xff7000,0xff7000,0xffb000,0xffb000,0xffff00,0xc0ff00,0xc0ff00,0x80ff00,0x80ff00,0x40ff00,0x40ff00,0x00ff00};

    @Test public void everyTransitionIncludesBothAdjacentIntegerValues() {
        for (int n=0;n<DELTAS.length;n++)
            assertEquals("offset "+DELTAS[n],TAGS[n],Game.combatLevelColorTag(60+DELTAS[n],60));
    }

    @Test public void argumentsHaveOppositeRolesAndCommonTranslationIsIrrelevant() {
        assertEquals("@red@",Game.combatLevelColorTag(30,50));
        assertEquals("@gre@",Game.combatLevelColorTag(50,30));
        assertEquals("@yel@",Game.combatLevelColorTag(30,30));
        assertEquals("@or1@",Game.combatLevelColorTag(30,31));
        assertEquals("@gr1@",Game.combatLevelColorTag(31,30));
        for (int shift : new int[]{-1000,-60,0,66,1000000})
            for (int n=0;n<DELTAS.length;n++)
                assertEquals(TAGS[n],Game.combatLevelColorTag(60+shift+DELTAS[n],60+shift));
    }

    @Test public void allNormalLevelPairsAgreeWithIndependentBandTable() {
        String[] table = {"@red@","@or3@","@or3@","@or3@","@or2@","@or2@","@or2@","@or1@","@or1@","@or1@","@yel@","@gr1@","@gr1@","@gr1@","@gr2@","@gr2@","@gr2@","@gr3@","@gr3@","@gr3@","@gre@"};
        for (int a=3;a<=126;a++) for (int b=3;b<=126;b++) {
            long d=(long)a-b;
            int cell=(int)Math.max(0,Math.min(20,10+d));
            assertEquals(a+","+b,table[cell],Game.combatLevelColorTag(a,b));
        }
    }

    @Test public void subtractionUsesSignedIntWraparoundRatherThanDistanceOrLongArithmetic() {
        assertEquals("@yel@",Game.combatLevelColorTag(Integer.MIN_VALUE,Integer.MIN_VALUE));
        assertEquals("@red@",Game.combatLevelColorTag(Integer.MIN_VALUE,0));
        assertEquals("@gre@",Game.combatLevelColorTag(Integer.MAX_VALUE,0));
        assertEquals("@gr1@",Game.combatLevelColorTag(Integer.MIN_VALUE,Integer.MAX_VALUE));
        assertEquals("@or1@",Game.combatLevelColorTag(Integer.MAX_VALUE,Integer.MIN_VALUE));
        assertEquals("@gre@",Game.combatLevelColorTag(Integer.MIN_VALUE,1));
        assertEquals("@red@",Game.combatLevelColorTag(Integer.MAX_VALUE,-1));
    }

    @Test public void returnedTokensColorFollowingGlyphWithoutAddingHorizontalAdvance() {
        TextDrawingArea font=TextWidthTest.font(false);
        int[] saved=DrawingArea.pixels;
        int[] state={DrawingArea.width,DrawingArea.height,DrawingArea.topX,DrawingArea.topY,DrawingArea.bottomX,DrawingArea.bottomY,DrawingArea.scanlineRightLimit,DrawingArea.clipRightHalf,DrawingArea.clipBottomHalf};
        try {
            int[] raster=new int[40*20]; DrawingArea.bindRaster(20,40,raster);
            for (int n=0;n<DELTAS.length;n++) {
                Arrays.fill(raster,0x123456);
                String token=Game.combatLevelColorTag(60+DELTAS[n],60);
                assertEquals(5,token.length());
                assertEquals(5,font.measureTaggedTextWidth(token+"A"));
                font.drawTaggedText(false,2,0xffffff,token+"A",10);
                for (int y=0;y<20;y++) for (int x=0;x<40;x++)
                    assertEquals("case "+n+" pixel "+x+","+y,
                        x>=3&&x<=5&&y>=3&&y<10?RGB[n]:0x123456,raster[y*40+x]);
            }
        } finally {
            DrawingArea.pixels=saved;DrawingArea.width=state[0];DrawingArea.height=state[1];
            DrawingArea.topX=state[2];DrawingArea.topY=state[3];DrawingArea.bottomX=state[4];DrawingArea.bottomY=state[5];
            DrawingArea.scanlineRightLimit=state[6];DrawingArea.clipRightHalf=state[7];DrawingArea.clipBottomHalf=state[8];
        }
    }
}
