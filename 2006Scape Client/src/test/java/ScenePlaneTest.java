import static org.junit.Assert.*;
import java.util.*;
import org.junit.*;

public class ScenePlaneTest {
    public static final class Trace {
        static boolean on;
        static final Map<Integer,List<Integer>> values = new HashMap<Integer,List<Integer>>();
        public static void value(int a,int b) {
            if(on) {
                if(!values.containsKey(a)) values.put(a,new ArrayList<Integer>());
                values.get(a).add(b);
            }
        }
        static void reset(){values.clear();on=true;}
        static void expect(int a,int... b) {
            if(Boolean.getBoolean("pass093.trace")) {
                List<Integer> c=values.get(a);assertNotNull("slot "+a,c);
                assertEquals("slot "+a, b.length,c.size());
                for(int n=0;n<b.length;n++)assertEquals("slot "+a+" store "+n,b[n],(int)c.get(n));
            }
        }
    }
    private Game g; private Player old;
    @Before public void before(){old=Game.myPlayer;Game.myPlayer=new Player();g=new Game();g.byteGroundArray=new byte[4][104][104];g.plane=1;g.yCameraCurve=309;}
    @After public void after(){Trace.on=false;Game.myPlayer=old;}
    private void place(int a,int b,int c,int d) {
        g.xCameraPos=a*128;g.yCameraPos=b*128;Game.myPlayer.x=c*128;Game.myPlayer.y=d*128;
    }
    private void ray(int a,int b,int c,int d,int[][] e) {
        place(a,b,c,d);
        Set<String> f=new HashSet<String>();
        for(int[] q:e)f.add(q[0]+","+q[1]);
        Trace.on=false;
        for(int x=0;x<104;x++)for(int y=0;y<104;y++) {
            g.byteGroundArray[1][x][y]=4;
            assertEquals("single marked cell "+x+","+y,f.contains(x+","+y)?1:3,g.selectRoofVisibilityPlane());
            g.byteGroundArray[1][x][y]=0;
        }
    }
    @Test public void explicitCellMembershipCoversBothBranchesAllOctantsAndTie() {
        int[][][] r={{{0,0},{1,0},{1,1},{2,1},{3,1},{3,2},{4,2}},
                     {{0,0},{0,1},{1,1},{1,2},{1,3},{2,3},{2,4}},
                     {{0,0},{0,1},{1,1},{1,2},{2,2}},
                     {{0,0},{1,0},{2,0},{3,0}},
                     {{0,0},{0,1},{0,2},{0,3}}};
        for(int s:new int[]{-1,1})for(int t:new int[]{-1,1})for(int[][] u:r) {
            int[][] v=new int[u.length][2];
            for(int n=0;n<u.length;n++){v[n][0]=50+s*u[n][0];v[n][1]=50+t*u[n][1];}
            int[] w=v[v.length-1];ray(50,50,w[0],w[1],v);
        }
    }
    @Test public void bitFourCurrentLayerPitchBoundaryAndPlayerOverride() {
        place(10,10,14,12);
        for(int a=0;a<4;a++)for(int b:new int[]{0,1,2,3,4,5,8,16,128,255}) {
            g.plane=a;g.byteGroundArray[a][11][10]=(byte)b;
            for(int c:new int[]{128,309,310,383}) {
                g.yCameraCurve=c;assertEquals(c<310&&(b&4)!=0?a:3,g.selectRoofVisibilityPlane());
            }
            g.byteGroundArray[a][11][10]=0;
            g.byteGroundArray[(a+1)%4][11][10]=4;g.yCameraCurve=309;assertEquals(3,g.selectRoofVisibilityPlane());
            g.byteGroundArray[(a+1)%4][11][10]=0;
            g.byteGroundArray[a][14][12]=(byte)b;g.yCameraCurve=310;
            assertEquals((b&4)!=0?a:3,g.selectRoofVisibilityPlane());g.byteGroundArray[a][14][12]=0;
        }
        g.plane=1;g.yCameraCurve=309;g.byteGroundArray[1][11][10]=4;
        g.xCameraPos+=127;g.yCameraPos+=127;Game.myPlayer.x+=127;Game.myPlayer.y+=127;
        assertEquals(1,g.selectRoofVisibilityPlane());
        g.byteGroundArray[1][11][10]=0;g.byteGroundArray[1][11][11]=4;assertEquals(1,g.selectRoofVisibilityPlane());
    }
    @Test public void actualIntegerSlotsEstablishCoordinatesDistancesAndHalfCellSteps() {
        for(int s:new int[]{-1,1})for(int t:new int[]{-1,1}) {
            place(50,50,50+s*4,50+t*2);Trace.reset();assertEquals(3,g.selectRoofVisibilityPlane());
            Trace.expect(101,3);Trace.expect(102,50,50+s,50+2*s,50+3*s,50+4*s);
            Trace.expect(103,50,50+t,50+2*t);Trace.expect(104,50+s*4);Trace.expect(105,50+t*2);
            Trace.expect(106,4);Trace.expect(107,2);Trace.expect(108,32768);
            Trace.expect(109,32768,65536,0,32768,65536,0,32768);
            place(50,50,50+s*2,50+t*4);Trace.reset();assertEquals(3,g.selectRoofVisibilityPlane());
            Trace.expect(102,50,50+s,50+2*s);Trace.expect(103,50,50+t,50+2*t,50+3*t,50+4*t);
            Trace.expect(104,50+s*2);Trace.expect(105,50+t*4);Trace.expect(106,2);Trace.expect(107,4);
            Trace.expect(108,32768);Trace.expect(109,32768,65536,0,32768,65536,0,32768);
        }
        place(10,10,12,12);Trace.reset();g.selectRoofVisibilityPlane();
        Trace.expect(102,10,11,12);Trace.expect(103,10,11,12);
        Trace.expect(108,65536);Trace.expect(109,32768,98304,32768,98304,32768);
    }
    @Test public void fractionalTruncationAndAccumulatorCrossingHaveIndependentCellOracle() {
        ray(10,10,13,11,new int[][]{{10,10},{11,10},{12,10},{12,11},{13,11}});
        Trace.reset();g.selectRoofVisibilityPlane();Trace.expect(108,21845);Trace.expect(109,32768,54613,76458,10922,32767);
        ray(10,10,11,13,new int[][]{{10,10},{10,11},{10,12},{11,12},{11,13}});
        Trace.reset();g.selectRoofVisibilityPlane();Trace.expect(108,21845);Trace.expect(109,32768,54613,76458,10922,32767);
    }
    @Test public void cameraClampAndSubtileFloorUseSceneEdges() {
        int[][] r={{-129,10*128,0,10,2,10},{104*128+127,10*128,103,10,101,10},
                   {10*128,-1,10,0,10,2},{10*128,104*128,10,103,10,101}};
        for(int[] a:r) {
            g.xCameraPos=a[0];g.yCameraPos=a[1];Game.myPlayer.x=a[4]*128+127;Game.myPlayer.y=a[5]*128+127;
            g.byteGroundArray[1][a[2]][a[3]]=4;Trace.reset();assertEquals(1,g.selectRoofVisibilityPlane());
            Trace.expect(101,3,1);Trace.expect(104,a[4]);Trace.expect(105,a[5]);
            assertEquals(a[2],(int)Trace.values.getOrDefault(102,Arrays.asList(a[2])).get(0));
            assertEquals(a[3],(int)Trace.values.getOrDefault(103,Arrays.asList(a[3])).get(0));
            g.byteGroundArray[1][a[2]][a[3]]=0;assertEquals(3,g.selectRoofVisibilityPlane());
        }
    }
    @Test public void coincidentTilesPreserveDivisionFailureOnlyBelowThreshold() {
        place(10,10,10,10);g.xCameraPos+=1;Game.myPlayer.x+=127;
        Trace.reset();
        try {g.selectRoofVisibilityPlane();fail("original zero-distance division must throw");}catch(ArithmeticException expected){}
        Trace.expect(106,0);Trace.expect(107,0);
        g.yCameraCurve=310;assertEquals(3,g.selectRoofVisibilityPlane());g.byteGroundArray[1][10][10]=4;assertEquals(1,g.selectRoofVisibilityPlane());
    }
    @Test public void fixedCameraReturnsCurrentPlaneWithoutReadingTerrainOrCamera() {
        g.byteGroundArray=null;g.xCameraPos=Integer.MIN_VALUE;g.yCameraPos=Integer.MAX_VALUE;
        for(int a=0;a<4;a++)for(int b:new int[]{0,309,310,2047}) {
            g.plane=a;g.yCameraCurve=b;assertEquals(a,g.selectFixedCameraPlane());
        }
    }
}
