import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;

public class ScreenProjectionTest {
    private int cx,cy;
    @Before public void save(){cx=Texture.textureInt1;cy=Texture.textureInt2;Texture.textureInt1=253;Texture.textureInt2=167;}
    @After public void restore(){Texture.textureInt1=cx;Texture.textureInt2=cy;}
    public static Game fixture(){
        Game g=TerrainHeightTest.fixture();
        for(int p=0;p<4;p++)for(int a=0;a<105;a++)java.util.Arrays.fill(g.intGroundArray[p][a],p*200-300);
        g.xCameraPos=3000;g.yCameraPos=2000;g.zCameraPos=-500;
        return g;
    }
    // Independent row-matrix evaluation, long products and explicit floor division.
    public static int[] stages(Game g,int a,int b,int c){
        int h=TerrainHeightTest.expected(g,g.plane,c,a)-b;
        int dx=a-g.xCameraPos,dy=c-g.yCameraPos,dz=h-g.zCameraPos;
        int sp=Model.modelIntArray1[g.yCameraCurve],cp=Model.modelIntArray2[g.yCameraCurve];
        int sy=Model.modelIntArray1[g.xCameraCurve],cc=Model.modelIntArray2[g.xCameraCurve];
        int[] v={dx,dy,dz};
        int[] r={dot(new int[]{cc,sy,0},v),dot(new int[]{-sy,cc,0},v),dz};
        int z=dot(new int[]{0,-sp,cp},r),d=dot(new int[]{0,cp,sp},r);
        return new int[]{h,dx,dz,dy,sp,cp,sy,cc,r[0],r[1],r[0],z,d,z};
    }
    private static int dot(int[] row,int[] v){long n=0;for(int k=0;k<3;k++)n+=(long)row[k]*v[k];return (int)Math.floorDiv(n,65536);}
    public static int[] expected(Game g,int a,int b,int c){
        if(a<128||c<128||a>13056||c>13056)return new int[]{-1,-1};
        int[] s=stages(g,a,b,c);if(s[12]<50)return new int[]{-1,-1};
        return new int[]{Texture.textureInt1+(int)((long)s[10]*512/s[12]),Texture.textureInt2+(int)((long)s[13]*512/s[12])};
    }
    public static void check(Game g,int a,int b,int c){int[] e=expected(g,a,b,c);g.projectWorldToScreen(a,b,c);assertArrayEquals(e,new int[]{g.spriteDrawX,g.spriteDrawY});}

    @Test public void zeroAnglesSeparateCoordinatesHeightAndPerspectiveScale(){
        Game g=fixture();
        for(int p=0;p<4;p++){g.plane=p;for(int a:new int[]{2744,3000,3256})for(int b:new int[]{-40,0,37})for(int c:new int[]{2050,2512,3024})check(g,a,b,c);}
        g.plane=0;g.projectWorldToScreen(3256,0,3024);assertEquals(381,g.spriteDrawX);assertEquals(267,g.spriteDrawY);
        g.projectWorldToScreen(3256,100,3024);assertEquals(217,g.spriteDrawY);
        g.xCameraPos+=128;g.projectWorldToScreen(3256,100,3024);assertEquals(317,g.spriteDrawX);
        g.yCameraPos+=512;g.projectWorldToScreen(3256,100,3024);assertEquals(381,g.spriteDrawX);
        g.zCameraPos+=50;g.projectWorldToScreen(3256,100,3024);assertEquals(217,g.spriteDrawY);
    }
    @Test public void allAngleIndicesAndMixedRotationsMatchIndependentMatrix(){
        Game g=fixture();
        for(int n=0;n<2048;n++){
            assertEquals(Math.sin(n*Math.PI/1024)*65536,Model.modelIntArray1[n],1.01);
            assertEquals(Math.cos(n*Math.PI/1024)*65536,Model.modelIntArray2[n],1.01);
            g.xCameraCurve=n;g.yCameraCurve=0;check(g,3257,37,3025);
            g.xCameraCurve=0;g.yCameraCurve=n;check(g,2743,-19,3025);
        }
        for(int n:new int[]{0,1,255,512,769,1024,1536,2047})for(int m:new int[]{0,1,255,512,769,1024,1536,2047}){
            g.xCameraCurve=n;g.yCameraCurve=m;
            for(int a:new int[]{2743,3257})for(int b:new int[]{-19,37})check(g,a,b,3025);
        }
    }
    @Test public void nearPlaneAndSceneBoundsAreInclusiveWithMinusOneSentinel(){
        Game g=fixture();
        for(int d:new int[]{-100,0,1,49,50,51,512}){g.yCameraPos=3000-d;check(g,3001,37,3000);if(d<50)assertEquals(-1,g.spriteDrawX);else assertTrue(g.spriteDrawX>=253);}
        g.yCameraPos=-1000;
        for(int a:new int[]{128,129,13055,13056})for(int c:new int[]{128,129,13055,13056}){check(g,a,37,c);assertTrue(g.spriteDrawY!=-1);}
        g.intGroundArray=null;g.byteGroundArray=null;
        for(int bad:new int[]{Integer.MIN_VALUE,-1,0,127,13057,Integer.MAX_VALUE}){check(g,bad,37,3000);check(g,3000,37,bad);}
    }
    @Test public void entityWrapperForwardsBothPlayerAndNpcCoordinatesAndOffset(){
        Game g=fixture();
        for(Entity e:new Entity[]{new Player(),new NPC()})for(int a:new int[]{2743,3257})for(int c:new int[]{2050,3025})for(int b:new int[]{-19,37}){
            e.x=a;e.y=c;e.height=999;int[] v=expected(g,a,b,c);g.projectEntityToScreen(e,b);assertArrayEquals(v,new int[]{g.spriteDrawX,g.spriteDrawY});
        }
    }
}
