import static org.junit.Assert.*;
import java.lang.reflect.Field;
import java.util.*;
import org.junit.*;

public class ScrollbarTest {
    private Game g;
    private final DrawingPrimitivesTest state = new DrawingPrimitivesTest();
    public static final class Trace {
        public static boolean on;
        public static final Map<Integer,List<Integer>> values = new HashMap<Integer,List<Integer>>();
        public static void value(int a,int b) {
            if(on) { if(!values.containsKey(a)) values.put(a,new ArrayList<Integer>()); values.get(a).add(b); }
        }
        public static void reset() { values.clear(); on=true; }
        static void expect(int a,Integer... b) {
            if(Boolean.getBoolean("pass091.trace")) assertEquals("slot "+a,Arrays.asList(b),values.get(a));
        }
    }
    @Before public void before() { state.saveState(); ClientSettings.FULL_512PX_VIEWPORT=false; g=fixture(); Trace.reset(); }
    @After public void after() { Trace.on=false; state.restoreState(); }
    public static Background image(int a) {
        Background b=new Background(new StreamLoader(Base64.getDecoder().decode(
            "AABgAABtMUFZJlNZKILEYgAADWtV/6QQICAAAAJAABAKAQAAQAAAgAGgAFCgAxGmmjQaaII2kD0j1FlFbrbt36ul5XDigcNmqcEMRoi5Aby3NEoQNwoUk2ZqFPSM/KkamMVmYitWIIP4u5IpwoSBRBYjEA==")),"small",0);
        b.imageWidth=16; b.imageHeight=16; b.canvasWidth=16; b.offsetX=0; b.offsetY=0;
        b.paletteRgb[1]=a; b.paletteIndices=new byte[256]; Arrays.fill(b.paletteIndices,(byte)1); return b;
    }
    public static Game fixture() { Game a=new Game(); a.scrollUpArrow=image(0x123456); a.scrollDownArrow=image(0xabcdef); return a; }
    private static void field(Game a,String b,int c)throws Exception { Field d=Game.class.getDeclaredField(b); d.setAccessible(true); d.setInt(a,c); }
    private static int[] raster() { int[] a=new int[180*160]; Arrays.fill(a,0x070809); DrawingArea.bindRaster(160,180,a); return a; }

    // Rows specify independently calculated geometry: height, offset, content, thumb size, travel from top.
    @Test public void rasterGeometryAndIntegerBoundaries() {
        int[][] a={{100,0,200,34,0},{100,50,200,34,17},{100,100,200,34,34},
            {100,0,1000,8,0},{100,450,1000,8,30},{100,900,1000,8,60},
            {77,61,200,17,13},{101,99,201,34,34},{100,-1,200,34,0}};
        for(int[] b:a) for(int c:new int[]{7,29}) {
            int[] d=raster(); Trace.reset(); g.drawScrollbar(b[0],b[1],c,41,b[2]);
            check(d,41,c,b[0],b[3],b[4],new int[]{g.scrollTrackColor,g.scrollThumbColor,g.scrollHighlightColor,g.scrollShadowColor},0,0,180,160);
            Trace.expect(106,b[3]==8?new Integer[]{6,8}:new Integer[]{b[3]}); Trace.expect(107,b[4]);
        }
    }
    @Test public void substitutedSourcesAndColorsRemainIndependent()throws Exception {
        String[] a={"scrollTrackColor","scrollThumbColor","scrollHighlightColor","scrollShadowColor"};
        int[] b={g.scrollTrackColor,g.scrollThumbColor,g.scrollHighlightColor,g.scrollShadowColor};
        for(int c=0;c<4;c++) {
            int[] d=b.clone(); d[c]=0x808010+c; field(g,a[c],d[c]); int[] e=raster();
            g.drawScrollbar(100,50,13,27,200); check(e,27,13,100,34,17,d,0,0,180,160); field(g,a[c],b[c]);
        }
        g.scrollUpArrow=image(0x445566); g.scrollDownArrow=image(0x778899); int[] c=raster(); g.drawScrollbar(100,0,13,27,200);
        assertEquals(0x445566,c[27+13*180]); assertEquals(0x778899,c[27+97*180]);
    }
    @Test public void clippingOnlyRestrictsWrittenPixels() {
        int[] a=raster(); DrawingArea.setClip(94,34,39,27); g.drawScrollbar(100,50,13,27,200);
        check(a,27,13,100,34,17,new int[]{g.scrollTrackColor,g.scrollThumbColor,g.scrollHighlightColor,g.scrollShadowColor},34,27,39,94);
    }
    private static void check(int[] a,int b,int c,int d,int e,int f,int[] h,int l,int t,int r,int u) {
        for(int y=0;y<160;y++) for(int x=0;x<180;x++) {
            int v=0x070809,dx=x-b,dy=y-(c+16+f);
            if(x>=l&&x<r&&y>=t&&y<u&&dx>=0&&dx<16&&y>=c&&y<c+d) {
                if(y<c+16) v=0x123456;
                else if(y>=c+d-16) v=0xabcdef;
                else if(dy>=0&&dy<e) {
                    if(dy==e-1||dx==15||(dy>=1&&dx==14)||(dy==e-2&&dx>=1)) v=h[3];
                    else if(dx<=1||dy<=1) v=h[2]; else v=h[1];
                } else v=h[0];
            }
            assertEquals("pixel "+x+","+y,v,a[x+y*180]);
        }
    }
    @Test public void arrowsUseHalfOpenBoxesWeightedMovementAndRedrawFlag() {
        for(int a:new int[]{0,1,3}) for(boolean b:new boolean[]{false,true}) {
            int[][] c={{39,20,100,0},{40,19,100,0},{40,20,100-4*a,1},{55,35,100-4*a,1},
                {56,20,100,0},{40,104,100+4*a,1},{55,119,100+4*a,1},{40,120,100,0}};
            for(int[] d:c) {
                RSInterface e=new RSInterface(),f=new RSInterface(); e.scrollPosition=100; f.scrollPosition=73;
                g.anInt1213=a;g.needDrawTabArea=false;g.scrollbarDragging=false;
                g.handleScrollbarInput(40,100,d[0],d[1],e,20,b,200);
                assertEquals(Arrays.toString(d),d[2],e.scrollPosition);assertEquals(73,f.scrollPosition);
                assertEquals(b&&d[3]==1,g.needDrawTabArea);assertFalse(g.scrollbarDragging); Trace.expect(209,0);
                Trace.reset();
            }
        }
    }
    @Test public void trackCentersTheThumbAndDoesNotClampAtArrowEdges() {
        int[][] a={{200,36,-50,34,-17,34},{200,53,0,34,0,34},{200,70,50,34,17,34},
            {200,87,100,34,34,34},{200,103,147,34,50,34},
            {1000,39,-15,8,-1,60},{1000,40,0,8,0,60},{1000,70,450,8,30,60},{1000,100,900,8,60,60},{1000,103,945,8,63,60}};
        for(int[] b:a) {
            RSInterface c=new RSInterface();c.scrollPosition=999; g.anInt1213=1;g.scrollbarDragging=false;g.needDrawTabArea=false;Trace.reset();
            g.handleScrollbarInput(40,100,48,b[1],c,20,true,b[0]);
            assertEquals(Arrays.toString(b),b[2],c.scrollPosition);assertTrue(g.scrollbarDragging);assertTrue(g.needDrawTabArea);
            Trace.expect(209,0);Trace.expect(210,b[3]==8?new Integer[]{6,8}:new Integer[]{b[3]});Trace.expect(211,b[4]);Trace.expect(212,b[5]);
        }
    }
    @Test public void priorDragExpandsOnlyHorizontalTrackBoxAndIsClearedByMissesAndArrows() {
        for(boolean a:new boolean[]{false,true}) for(int b:new int[]{7,8,39,40,55,56,87,88}) {
            RSInterface c=new RSInterface();c.scrollPosition=999;g.anInt1213=1;g.scrollbarDragging=a;g.needDrawTabArea=true;Trace.reset();
            g.handleScrollbarInput(40,100,b,70,c,20,false,200);
            boolean d=b>=(a?8:40)&&b<(a?88:56);
            assertEquals(d?50:999,c.scrollPosition);assertEquals(d,g.scrollbarDragging);assertTrue(g.needDrawTabArea);Trace.expect(209,a?32:0);
        }
        RSInterface a=new RSInterface();a.scrollPosition=100;g.anInt1213=0;g.scrollbarDragging=true;g.needDrawTabArea=false;
        g.handleScrollbarInput(40,100,40,70,a,20,true,200);assertEquals(100,a.scrollPosition);assertFalse(g.scrollbarDragging);assertFalse(g.needDrawTabArea);
        g.anInt1213=1;g.scrollbarDragging=true;g.handleScrollbarInput(40,100,40,20,a,20,false,200);assertEquals(96,a.scrollPosition);assertFalse(g.scrollbarDragging);
        g.anInt1213=3;g.handleScrollbarInput(40,100,40,70,a,20,false,200);assertEquals(50,a.scrollPosition);assertTrue(g.scrollbarDragging);
    }
}
