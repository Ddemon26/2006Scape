import static org.junit.Assert.*;
import org.junit.Test;

public class TerrainHeightTest {
    public static Game fixture() {
        Game g = new Game();
        g.intGroundArray = new int[4][105][105];
        g.byteGroundArray = new byte[4][104][104];
        for (int p=0;p<4;p++) for (int x=0;x<105;x++) for (int y=0;y<105;y++)
            g.intGroundArray[p][x][y] = p*1000+x*3-y*5;
        return g;
    }

    // Scalar two-stage grid interpolation; floor division matters for negative heights.
    public static int expected(Game g,int p,int y,int x) {
        int a=Math.floorDiv(x,128),b=Math.floorDiv(y,128);
        if(a<0||a>=104||b<0||b>=104)return 0;
        if(p<3&&(g.byteGroundArray[1][a][b]&2)!=0)p++;
        int dx=Math.floorMod(x,128),dy=Math.floorMod(y,128);
        long lo=g.intGroundArray[p][a][b],hi=g.intGroundArray[p][a][b+1];
        lo+=Math.floorDiv(((long)g.intGroundArray[p][a+1][b]-lo)*dx,128);
        hi+=Math.floorDiv(((long)g.intGroundArray[p][a+1][b+1]-hi)*dx,128);
        return (int)(lo+Math.floorDiv((hi-lo)*dy,128));
    }

    @Test public void asymmetricAxesAndIndependentPlanesSeparateArgumentRoles() {
        Game g=fixture();
        for(int p=0;p<4;p++) {
            assertEquals(p*1000+17*3-23*5,g.interpolatedGroundHeight(p,23*128,17*128));
            assertEquals(p*1000+18*3-23*5,g.interpolatedGroundHeight(p,23*128,18*128));
            assertEquals(p*1000+17*3-24*5,g.interpolatedGroundHeight(p,24*128,17*128));
            assertEquals(expected(g,p,23*128+97,17*128+31),g.interpolatedGroundHeight(p,23*128+97,17*128+31));
        }
    }

    @Test public void everySubtileOffsetUsesBothRowsAndSequentialSignedFlooring() {
        Game g=fixture();int a=17,b=23;
        int[][] corners={{0,1,1,2},{-17,302,-211,57},{123,-321,47,901}};
        for(int[] v:corners) {
            g.intGroundArray[0][a][b]=v[0];g.intGroundArray[0][a+1][b]=v[1];
            g.intGroundArray[0][a][b+1]=v[2];g.intGroundArray[0][a+1][b+1]=v[3];
            for(int x=0;x<128;x++)for(int y=0;y<128;y++)
                assertEquals(expected(g,0,b*128+y,a*128+x),g.interpolatedGroundHeight(0,b*128+y,a*128+x));
        }
        g.intGroundArray[0][a][b]=0;g.intGroundArray[0][a+1][b]=1;
        g.intGroundArray[0][a][b+1]=1;g.intGroundArray[0][a+1][b+1]=2;
        assertEquals(0,g.interpolatedGroundHeight(0,b*128+64,a*128+64)); // one-step bilinear would be 1
        g.intGroundArray[0][a][b]=0;g.intGroundArray[0][a+1][b]=-1;
        assertEquals(-1,g.interpolatedGroundHeight(0,b*128,a*128+1)); // truncation toward zero would be 0
    }

    @Test public void bridgeUsesOnlyBitTwoFromLayerOneAndNeverPromotesPlaneThree() {
        Game g=fixture();int x=17*128+31,y=23*128+97;
        for(int flags:new int[]{0,1,2,3,4,8,16,127,128,255}) {
            g.byteGroundArray[1][17][23]=(byte)flags;
            for(int p=0;p<4;p++)assertEquals(expected(g,p,y,x),g.interpolatedGroundHeight(p,y,x));
        }
        g.byteGroundArray[1][17][23]=0;
        g.byteGroundArray[0][17][23]=2;g.byteGroundArray[2][17][23]=2;
        g.byteGroundArray[1][18][23]=2;g.byteGroundArray[1][17][24]=2;
        for(int p=0;p<4;p++)assertEquals(expected(g,p,y,x),g.interpolatedGroundHeight(p,y,x));
    }

    @Test public void firstAndLastTilesAndOutOfSceneCoordinatesRespect128UnitGrid() {
        Game g=fixture();int[] coordinates={0,1,127,128,129,13183,13184,13310,13311};
        for(int x:coordinates)for(int y:coordinates)for(int p=0;p<4;p++)
            assertEquals(expected(g,p,y,x),g.interpolatedGroundHeight(p,y,x));
        g.intGroundArray=null;g.byteGroundArray=null;
        for(int bad:new int[]{Integer.MIN_VALUE,-129,-128,-1,13312,13313,Integer.MAX_VALUE}) {
            assertEquals(0,g.interpolatedGroundHeight(0,128,bad));assertEquals(0,g.interpolatedGroundHeight(0,bad,128));
        }
    }
}
