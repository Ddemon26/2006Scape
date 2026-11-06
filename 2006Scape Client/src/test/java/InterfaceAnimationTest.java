import static org.junit.Assert.*;
import java.lang.reflect.*;
import java.util.*;
import org.junit.*;

public class InterfaceAnimationTest {
    private RSInterface[] a;
    private Animation[] b;
    private Game g;

    public static final class Trace {
        public static boolean on;
        public static final Map<Integer,List<Object>> values=new HashMap<Integer,List<Object>>();
        public static void value(int a,Object b) {
            if(!on)return;
            if(!values.containsKey(a))values.put(a,new ArrayList<Object>());
            values.get(a).add(b);
        }
        public static void value(int a,int b){value(a,Integer.valueOf(b));}
        public static void reset(){values.clear();on=true;}
        static void saw(int a,Object b){if(Boolean.getBoolean("pass088.trace")){assertNotNull("slot "+a,values.get(a));assertTrue("slot "+a+" lacks "+b,values.get(a).contains(b));}}
    }

    @Before public void before() throws Exception {
        a=RSInterface.interfaceCache;b=Animation.anims;g=new Game();
        RSInterface.interfaceCache=new RSInterface[20];Animation.anims=new Animation[4];
        Animation.anims[1]=sequence(new int[]{2,4,1},2);
        Animation.anims[3]=sequence(new int[]{7,3},1);
        RSInterface.interfaceCache[2]=component(2,0);
        RSInterface.interfaceCache[2].children=new int[]{7};
        RSInterface.interfaceCache[7]=component(7,6);
        RSInterface.interfaceCache[7].anInt257=1;RSInterface.interfaceCache[7].anInt258=3;
        Trace.reset();
    }
    @After public void after(){Trace.on=false;RSInterface.interfaceCache=a;Animation.anims=b;}
    public static Animation sequence(int[] a,int b)throws Exception {
        Constructor<Animation> c=Animation.class.getDeclaredConstructor();c.setAccessible(true);
        Animation d=c.newInstance();d.anInt352=a.length;d.anInt356=b;
        Field e=Animation.class.getDeclaredField("frameDelays");e.setAccessible(true);e.set(d,a.clone());
        d.anIntArray353=new int[a.length];d.anIntArray354=new int[a.length];Arrays.fill(d.anIntArray353,-1);Arrays.fill(d.anIntArray354,-1);return d;
    }
    public static RSInterface component(int a,int b){RSInterface c=new RSInterface();c.id=a;c.type=b;c.anInt257=-1;c.anInt258=-1;return c;}
    private RSInterface c(){return RSInterface.interfaceCache[7];}

    @Test public void accumulatedTimeUsesStrictDelayBoundaryAndReportsOnlyFrameAdvance(){
        assertFalse(g.advanceInterfaceModelAnimations(0,2));assertFalse(g.advanceInterfaceModelAnimations(2,2));
        assertEquals(0,c().anInt246);assertEquals(2,c().anInt208);
        assertTrue(g.advanceInterfaceModelAnimations(1,2));assertEquals(1,c().anInt246);assertEquals(0,c().anInt208);
        assertFalse(g.advanceInterfaceModelAnimations(4,2));assertEquals(4,c().anInt208);
        assertTrue(g.advanceInterfaceModelAnimations(1,2));assertEquals(2,c().anInt246);assertEquals(0,c().anInt208);
        Trace.saw(3,0);Trace.saw(3,1);Trace.saw(4,RSInterface.interfaceCache[2]);Trace.saw(8,7);Trace.saw(9,c());Trace.saw(10,0);Trace.saw(11,1);Trace.saw(12,Animation.anims[1]);
    }
    @Test public void realSelectionPredicateChoosesAlternateSequenceAndMissingChoiceIsNotFallback(){
        c().conditionOperators=new int[0];assertTrue(g.interfaceIsSelected(c()));
        assertFalse(g.advanceInterfaceModelAnimations(7,2));assertEquals(7,c().anInt208);assertEquals(0,c().anInt246);
        assertTrue(g.advanceInterfaceModelAnimations(1,2));assertEquals(1,c().anInt246);assertEquals(0,c().anInt208);
        Trace.saw(10,1);Trace.saw(11,3);Trace.saw(12,Animation.anims[3]);
        c().anInt258=-1;assertFalse(g.advanceInterfaceModelAnimations(99,2));assertEquals(0,c().anInt208);assertEquals(1,c().anInt246);Trace.saw(11,-1);
        c().conditionOperators=null;c().anInt257=-1;c().anInt258=3;
        assertFalse(g.interfaceIsSelected(c()));assertFalse(g.advanceInterfaceModelAnimations(99,2));assertEquals(0,c().anInt208);
    }
    @Test public void multipleFramesLoopByTailCountAndInvalidLoopReturnsToZero(){
        assertTrue(g.advanceInterfaceModelAnimations(10,2));assertEquals(1,c().anInt246);assertEquals(0,c().anInt208);
        c().anInt246=2;c().anInt208=0;Animation.anims[1].anInt356=-1;
        assertTrue(g.advanceInterfaceModelAnimations(2,2));assertEquals(0,c().anInt246);assertEquals(0,c().anInt208);
        c().anInt246=2;Animation.anims[1].anInt356=9;
        assertTrue(g.advanceInterfaceModelAnimations(2,2));assertEquals(0,c().anInt246);
        c().anInt246=2;Animation.anims[1].anInt356=0;
        assertTrue(g.advanceInterfaceModelAnimations(2,2));assertEquals(0,c().anInt246);
    }
    @Test public void sparseChildrenSentinelNullParentsAndOnlyTypeOneRecursion(){
        assertFalse(g.advanceInterfaceModelAnimations(99,19));assertFalse(g.advanceInterfaceModelAnimations(99,7));
        RSInterface d=component(11,1);RSInterface.interfaceCache[11]=d;d.children=new int[]{7};
        RSInterface.interfaceCache[2].children=new int[]{11,-1,15};
        assertTrue(g.advanceInterfaceModelAnimations(3,2));assertEquals(1,c().anInt246);Trace.saw(8,11);Trace.saw(8,-1);Trace.saw(4,d);Trace.saw(9,d);
        c().anInt246=0;c().anInt208=0;d.type=0;
        assertFalse(g.advanceInterfaceModelAnimations(3,2));assertEquals(0,c().anInt246);assertEquals(0,c().anInt208);
        d.type=1;c().type=5;assertFalse(g.advanceInterfaceModelAnimations(3,2));assertEquals(0,c().anInt208);
    }
    @Test public void allSiblingsAdvanceEvenAfterRedrawBecameTrue(){
        RSInterface d=component(15,6);d.anInt257=3;RSInterface.interfaceCache[15]=d;
        RSInterface.interfaceCache[2].children=new int[]{7,15};
        assertTrue(g.advanceInterfaceModelAnimations(8,2));assertEquals(2,c().anInt246);assertEquals(1,d.anInt246);assertEquals(0,d.anInt208);
        Trace.saw(8,15);Trace.saw(9,d);Trace.saw(11,3);
    }
    @Test public void negativeDeltaIsAddedAndSingleFrameWrapStillRequestsRedraw()throws Exception{
        assertFalse(g.advanceInterfaceModelAnimations(-1,2));assertEquals(-1,c().anInt208);
        assertFalse(g.advanceInterfaceModelAnimations(3,2));assertEquals(2,c().anInt208);
        Animation.anims[1]=sequence(new int[]{1},1);c().anInt208=0;
        assertTrue(g.advanceInterfaceModelAnimations(2,2));assertEquals(0,c().anInt246);assertEquals(0,c().anInt208);
    }
}
