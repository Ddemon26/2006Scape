import static org.junit.Assert.*;
import java.util.*;
import org.junit.*;

public class MinimapMarkerTest {
    public static final class Trace {
        static boolean on;
        static final Map<Integer,List<Double>> values=new HashMap<Integer,List<Double>>();
        public static void value(int a,int b){decimal(a,b);}
        public static void decimal(int a,double b){if(on){if(!values.containsKey(a))values.put(a,new ArrayList<Double>());values.get(a).add(b);}}
        static void reset(){values.clear();on=true;}
        static void expect(int a,double... b){if(Boolean.getBoolean("pass092.trace")){List<Double> c=values.get(a);assertNotNull("slot "+a,c);assertEquals(b.length,c.size());for(int i=0;i<b.length;i++)assertEquals("slot "+a,b[i],c.get(i),1e-12);}}
    }
    private final DrawingPrimitivesTest state=new DrawingPrimitivesTest();
    private Game g;private Sprite a,b;private int[] c,d,raster;
    @Before public void before(){state.saveState();ClientSettings.FULL_512PX_VIEWPORT=false;g=new Game();a=new Sprite(3,5);Arrays.fill(a.pixels,0x123456);a.trimWidth=7;a.trimHeight=9;b=new Sprite(50,50);Arrays.fill(b.pixels,0xabcdef);g.minimapEdgeArrow=b;g.mapBack=ScrollbarTest.image(1);g.mapBack.paletteIndices=new byte[400*300];for(int n=0;n<400*300;n++)g.mapBack.paletteIndices[n]=(byte)(n%2);raster=new int[400*300];DrawingArea.bindRaster(300,400,raster);c=Model.modelIntArray1;d=Model.modelIntArray2;Model.modelIntArray1=new int[2048];Model.modelIntArray2=new int[2048];Model.modelIntArray2[0]=65536;Model.modelIntArray1[512]=65536;Model.modelIntArray1[257]=39321;Model.modelIntArray2[257]=52428;Trace.reset();}
    @After public void after(){Trace.on=false;Model.modelIntArray1=c;Model.modelIntArray2=d;state.restoreState();}
    private void check(int h,int x,int y){for(int yy=0;yy<300;yy++)for(int xx=0;xx<400;xx++){boolean inside=xx>=x&&xx<x+(h==3?20:3)&&yy>=y&&yy<y+(h==3?20:5);int expected=inside&&(h!=2||g.mapBack.paletteIndices[xx+yy*400]==0)?(h==3?0xabcdef:0x123456):0;assertEquals("pixel "+xx+","+yy,expected,raster[xx+yy*400]);}Arrays.fill(raster,0);}
    private void blank(){for(int v:raster)assertEquals(0,v);}
    @Test public void axesDimensionsAndRadiusBoundaries(){
        int[][] r={{0,0,1,95,75,0},{10,0,1,105,75,100},{0,10,1,95,65,100},{-10,0,1,85,75,100},{0,-10,1,95,85,100},{30,40,1,125,35,2500},{30,41,2,125,34,2581},{80,0,2,175,75,6400}};
        for(int[] t:r){Trace.reset();g.drawMinimapMarker(a,t[0],t[1]);check(t[2],t[3],t[4]);Trace.expect(104,0);Trace.expect(105,t[5]);Trace.expect(106,0,0);Trace.expect(107,65536,65536);Trace.expect(108,t[0]);Trace.expect(109,t[1]);}
        for(int[] t:new int[][]{{80,1},{81,0},{0,-81}}){Trace.reset();g.drawMinimapMarker(a,t[0],t[1]);blank();assertFalse(Trace.values.containsKey(106));}
        a.trimWidth=2;a.trimHeight=4;g.drawMinimapMarker(a,0,0);check(1,97,77);
    }
    @Test public void tableSelectionZoomAndSignedFixedPoint(){
        // index inputs, zoom, X/Y, transformed X/Y; independent cardinal and 3-4-5 vectors.
        int[][] r={{256,256,0,10,20,20,-10,512,65536,0}, {2048,0,0,10,20,10,20,0,0,65536},
            {-1,513,256,10,20,10,-5,512,32768,0},{0,0,256,-1,1,-1,0,0,0,32768},
            {0,0,-128,10,-20,20,-40,0,0,131072},{257,0,0,10,20,19,9,257,39321,52428}};
        for(int[] t:r){g.minimapInt1=t[0];g.minimapInt2=t[1];g.minimapInt3=t[2];Trace.reset();g.drawMinimapMarker(a,t[3],t[4]);check(1,95+t[5],75-t[6]);Trace.expect(104,t[7]);Trace.expect(106,t[7]==512?65536:t[8],t[8]);Trace.expect(107,t[7]==0?65536:t[9],t[9]);Trace.expect(108,t[5]);Trace.expect(109,t[6]);}
    }
    @Test public void hintStrictThresholdsAndFallbackOrder(){
        for(int n:new int[]{0,50,65,300,301}){Trace.reset();g.drawMinimapHint(a,0,n);if(n<=65)check(n>50?2:1,95+n,75);else blank();Trace.expect(204,n*n);}
        for(int n:new int[]{66,80,81,299}){g.drawMinimapHint(a,0,n);check(3,151,63);}
    }
    @Test public void hintCardinalsObliqueAnglesAndLocalEllipseValues(){
        double[][] r={{0,100,0,100,0,0,57},{100,0,100,0,Math.PI/2,63,0},{0,-100,0,-100,Math.PI,0,-57},{-100,0,-100,0,-Math.PI/2,-63,0},{60,80,60,80,0.6435011087932844,37,45}};
        for(double[] t:r){Trace.reset();g.drawMinimapHint(a,(int)t[1],(int)t[0]);check(3,88+(int)t[5],63-(int)t[6]);Trace.expect(204,t[0]*t[0]+t[1]*t[1]);Trace.expect(205,0);Trace.expect(206,0,0);Trace.expect(207,65536,65536);Trace.expect(208,t[2]);Trace.expect(209,t[3]);Trace.expect(210,t[4]);Trace.expect(212,t[5]);Trace.expect(213,t[6]);}
        g.minimapInt1=256;g.minimapInt2=256;g.minimapInt3=256;Trace.reset();g.drawMinimapHint(a,0,100);check(3,88,120);Trace.expect(205,512);Trace.expect(206,65536,32768);Trace.expect(207,0,0);Trace.expect(208,0);Trace.expect(209,-50);Trace.expect(210,Math.PI);
    }
    @Test public void sourceIdentityAndInputRadiusAreIndependentOfZoom(){
        Sprite e=new Sprite(50,50);Arrays.fill(e.pixels,0x123456);g.minimapEdgeArrow=e;g.minimapInt3=256;g.drawMinimapHint(a,0,66);assertEquals(0x123456,raster[151+63*400]);Arrays.fill(raster,0);
        g.drawMinimapMarker(a,81,0);blank();g.minimapInt3=-128;g.drawMinimapMarker(a,50,0);check(1,195,75);
        g.drawMinimapHint(a,0,65);check(2,225,75);
    }
}
