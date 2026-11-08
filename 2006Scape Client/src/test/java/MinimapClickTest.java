import static org.junit.Assert.*;
import java.util.*;
import org.junit.*;

/** Controlled inputs and outputs of the minimap-click routine; slot traces are optional agent evidence. */
public class MinimapClickTest {
    public static final class Trace {
        public static final Map<Integer,List<Integer>> values = new HashMap<Integer,List<Integer>>();
        public static void value(int slot,int value) {
            if (!values.containsKey(slot)) values.put(slot,new ArrayList<Integer>());
            values.get(slot).add(value);
        }
        static void expect(int slot,int... expected) {
            if (!Boolean.getBoolean("pass204.trace")) return;
            List<Integer> actual=values.get(slot);
            assertNotNull("slot "+slot,actual); assertEquals("slot "+slot,expected.length,actual.size());
            for(int n=0;n<expected.length;n++) assertEquals("slot "+slot,expected[n],actual.get(n).intValue());
        }
    }
    static final class Capture extends Game {
        int calls; int[] args; boolean answer=true;
        @Override public boolean doWalkTo(int a,int b,int c,int d,int e,int f,int h,int i,int j,boolean k,int l) {
            calls++; args=new int[]{a,b,c,d,e,f,h,i,j,k?1:0,l}; return answer;
        }
    }
    private Player saved; private int counter; private int[] sine,cosine; private Capture g;
    @Before public void before() {
        saved=Game.myPlayer;counter=Game.minimapPrimaryClickCount;sine=Texture.sineTable;cosine=Texture.cosineTable;
        g=new Capture();Game.myPlayer=new Player();Game.myPlayer.x=6464;Game.myPlayer.y=6464;
        Game.myPlayer.smallX[0]=50;Game.myPlayer.smallY[0]=50;
        Texture.sineTable=new int[2048];Texture.cosineTable=new int[2048];
        Texture.cosineTable[0]=65536;Texture.sineTable[512]=65536;
        Texture.cosineTable[1024]=-65536;Texture.sineTable[1536]=-65536;
        Texture.sineTable[257]=39321;Texture.cosineTable[257]=52428;
        g.stream=new Stream(new byte[4096]);g.stream.encryption=new ISAACRandomGen(new int[4]);
        Game.minimapPrimaryClickCount=0;g.clickMode3=1;Trace.values.clear();
    }
    @After public void after(){Game.myPlayer=saved;Game.minimapPrimaryClickCount=counter;Texture.sineTable=sine;Texture.cosineTable=cosine;}
    private void click(int x,int y){g.saveClickX=575+x;g.saveClickY=9+y;Trace.values.clear();g.processMainScreenClick();}
    @Test public void cardinalAxesZoomWrapAndFractionalWorldPositions() {
        // yaw, drift, zoom, centered x/y, expected world x/south, target x/y, table index, scaled sine/cosine
        int[][] cases={{0,0,0,8,4,256,128,52,49,0,0,65536},
            {512,0,0,8,4,128,-256,51,52,512,65536,0},
            {1024,0,0,8,4,-256,-128,48,51,1024,0,-65536},
            {1536,0,0,8,4,-128,256,49,48,1536,-65536,0},
            {2000,560,128,8,4,192,-384,52,53,512,98304,0},
            {0,0,-128,-1,1,-16,16,50,50,0,0,32768},
            {257,0,0,10,20,639,319,55,48,257,39321,52428},
            {257,0,0,-10,-20,-640,-320,45,53,257,39321,52428}};
        for(int[] a:cases){
            g.cameraYawBase=a[0];g.minimapRotationDrift=a[1];g.minimapZoomDrift=a[2];g.stream.currentOffset=0;
            click(a[3]+73,a[4]+75);
            assertArrayEquals(new int[]{1,0,0,0,50,0,0,a[8],50,1,a[7]},g.args);
            assertEquals(14,g.stream.currentOffset);assertEquals((byte)a[3],g.stream.buffer[0]);assertEquals((byte)a[4],g.stream.buffer[1]);
            assertEquals(57,g.stream.buffer[4]&255);assertEquals(89,g.stream.buffer[7]&255);assertEquals(63,g.stream.buffer[13]&255);
            Trace.expect(1,a[3]+73,a[3]);Trace.expect(2,a[4]+75,a[4]);Trace.expect(3,a[9]);
            Trace.expect(4,Texture.sineTable[a[9]],a[10]);Trace.expect(5,Texture.cosineTable[a[9]],a[11]);
            Trace.expect(6,a[5]);Trace.expect(7,a[6]);Trace.expect(8,a[7]);Trace.expect(9,a[8]);Trace.expect(10,1);
        }
        Game.myPlayer.x=-1;Game.myPlayer.y=-1;g.cameraYawBase=0;g.minimapRotationDrift=0;g.minimapZoomDrift=0;
        click(73,75);assertEquals(-1,g.args[7]);assertEquals(-1,g.args[10]);Trace.expect(8,-1);Trace.expect(9,-1);
    }
    @Test public void rectangleAndInputGatesAndFalseReturn() {
        for(int[] a:new int[][]{{0,0},{145,150},{73,75}}){int n=g.calls;click(a[0],a[1]);assertEquals(n+1,g.calls);}
        for(int[] a:new int[][]{{-1,0},{0,-1},{146,0},{0,151}}){int n=g.calls,o=g.stream.currentOffset;click(a[0],a[1]);assertEquals(n,g.calls);assertEquals(o,g.stream.currentOffset);}
        assertEquals(7,Game.minimapPrimaryClickCount);
        g.minimapState=1;click(73,75);assertEquals(7,Game.minimapPrimaryClickCount);
        g.minimapState=0;g.clickMode3=2;click(73,75);assertEquals(7,Game.minimapPrimaryClickCount);
        g.clickMode3=1;g.answer=false;int o=g.stream.currentOffset;click(73,75);assertEquals(o,g.stream.currentOffset);Trace.expect(10,0);
    }
    @Test public void quantizedTableValuesKeepSignedFloorRounding() {
        // Real table's three-quarter sine is -65535: the positive displacement is 127, not 128.
        assertEquals(-65535,sine[1536]);
        Texture.sineTable[1536]=sine[1536];g.cameraYawBase=1536;
        click(77,75);Trace.expect(6,0);Trace.expect(7,127);assertEquals(49,g.args[7]);
        click(69,75);Trace.expect(6,0);Trace.expect(7,-128);assertEquals(51,g.args[7]);
    }
    @Test public void periodicPacketLengthStartsAfterPlaceholderAtDifferentPrefixes() {
        for(int prefix:new int[]{0,7,31}) for(int attempt=0;attempt<20;attempt++) {
            g.stream.currentOffset=prefix;Game.minimapPrimaryClickCount=1150;click(-1,0);
            assertEquals(1151,Game.minimapPrimaryClickCount);assertEquals(prefix,g.stream.currentOffset);
            click(-1,0);assertEquals(0,Game.minimapPrimaryClickCount);
            int length=g.stream.buffer[prefix+1]&255;
            assertEquals(g.stream.currentOffset-prefix-2,length);assertTrue(length>=10&&length<=14);
            Trace.expect(1,-1);Trace.expect(3,prefix+2);
            assertEquals(180,g.stream.buffer[g.stream.currentOffset-1]&255);
        }
    }
}
