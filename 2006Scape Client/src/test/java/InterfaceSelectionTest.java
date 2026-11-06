import static org.junit.Assert.*;
import java.util.*;
import java.lang.reflect.Field;
import org.junit.*;

public class InterfaceSelectionTest {
    private Game g;
    public static final class Trace {
        public static boolean on;
        public static final Map<Integer,List<Integer>> values=new HashMap<Integer,List<Integer>>();
        public static void value(int a,int b){if(on){if(!values.containsKey(a))values.put(a,new ArrayList<Integer>());values.get(a).add(b);}}
        public static void value(int a,Object b){}
        public static void reset(){values.clear();on=true;}
        static void saw(int a,int b){if(Boolean.getBoolean("pass089.trace")){assertNotNull("slot "+a,values.get(a));assertTrue(values.get(a).contains(b));}}
    }
    @Before public void before(){g=new Game();Trace.reset();}
    @After public void after(){Trace.on=false;}
    public static RSInterface fixture(int[] a,int[] b,int[] c){
        RSInterface d=new RSInterface();d.id=193;d.conditionOperators=a;d.conditionValues=b;
        if(c!=null){d.valueIndexArray=new int[c.length][];for(int n=0;n<c.length;n++)d.valueIndexArray[n]=new int[]{20,c[n],0};}
        return d;
    }

    @Test public void strictEdgesAndEveryOtherCodeUsesEquality(){
        int[][] a={{2,6,7,1},{2,7,7,0},{2,8,7,0},{3,6,7,0},{3,7,7,0},{3,8,7,1},
            {4,6,7,1},{4,7,7,0},{4,8,7,1},{2,Integer.MIN_VALUE,Integer.MAX_VALUE,1},
            {3,Integer.MAX_VALUE,Integer.MIN_VALUE,1},{4,-1,-2,1},{2,-2,-1,1},{3,-1,-2,1}};
        for(int[] b:a)assertEquals(Arrays.toString(b),b[3]==1,g.interfaceIsSelected(fixture(new int[]{b[0]},new int[]{b[2]},new int[]{b[1]})));
        for(int b:new int[]{-1,0,1,5,17,255,Integer.MAX_VALUE}){
            assertTrue(g.interfaceIsSelected(fixture(new int[]{b},new int[]{-7},new int[]{-7})));
            assertFalse(g.interfaceIsSelected(fixture(new int[]{b},new int[]{-7},new int[]{-8})));
            assertFalse(g.interfaceIsSelected(fixture(new int[]{b},new int[]{-7},new int[]{-6})));
        }
        Trace.saw(3,Integer.MIN_VALUE);Trace.saw(3,-8);Trace.saw(4,Integer.MAX_VALUE);Trace.saw(4,-7);
    }

    @Test public void allRowsArePairedInOrderAndFailureStopsEvaluation(){
        final List<Integer> a=new ArrayList<Integer>();final List<RSInterface> b=new ArrayList<RSInterface>();
        Game c=new Game(){@Override public int extractInterfaceValues(RSInterface d,int e){a.add(e);b.add(d);return super.extractInterfaceValues(d,e);}};
        RSInterface d=fixture(new int[]{2,3,4},new int[]{12,-5,99},new int[]{11,-4,98});
        assertTrue(c.interfaceIsSelected(d));assertEquals(Arrays.asList(0,1,2),a);
        for(RSInterface e:b)assertSame(d,e);
        Trace.saw(2,0);Trace.saw(2,1);Trace.saw(2,2);Trace.saw(2,3);
        Trace.saw(3,11);Trace.saw(3,-4);Trace.saw(3,98);Trace.saw(4,12);Trace.saw(4,-5);Trace.saw(4,99);
        a.clear();d.conditionValues[1]=-4;
        assertFalse(c.interfaceIsSelected(d));assertEquals(Arrays.asList(0,1),a);
        a.clear();d.conditionValues=new int[]{11};
        assertFalse(c.interfaceIsSelected(d));assertEquals(Arrays.asList(0),a);
    }

    @Test public void absentAndEmptyRowsDifferAndScriptErrorsRemainIntegers(){
        assertFalse(g.interfaceIsSelected(fixture(null,null,null)));
        assertTrue(g.interfaceIsSelected(fixture(new int[0],null,null)));
        RSInterface a=fixture(new int[]{1},new int[]{-2},null);
        assertEquals(-2,g.extractInterfaceValues(a,0));assertTrue(g.interfaceIsSelected(a));
        a.conditionValues[0]=0;assertFalse(g.interfaceIsSelected(a));
        a.valueIndexArray=new int[][]{{20}};a.conditionValues[0]=-1;
        assertEquals(-1,g.extractInterfaceValues(a,0));assertTrue(g.interfaceIsSelected(a));
        a.conditionOperators[0]=4;assertFalse(g.interfaceIsSelected(a));Trace.saw(3,-2);Trace.saw(3,-1);
    }

    @Test public void operatorTargetAndScriptChangesHaveIndependentEffects(){
        RSInterface a=fixture(new int[]{2},new int[]{10},new int[]{9});
        assertTrue(g.interfaceIsSelected(a));a.conditionOperators[0]=3;assertFalse(g.interfaceIsSelected(a));
        a.conditionValues[0]=8;assertTrue(g.interfaceIsSelected(a));
        a.valueIndexArray[0][1]=8;assertFalse(g.interfaceIsSelected(a));
        a.conditionOperators[0]=1;assertTrue(g.interfaceIsSelected(a));
    }

    @Test public void realSettingScriptIsReadAgainOnEachCall(){
        g.variousSettings[17]=256;
        RSInterface a=fixture(new int[]{1},new int[]{257},null);a.valueIndexArray=new int[][]{{5,17,0}};
        assertFalse(g.interfaceIsSelected(a));g.variousSettings[17]=257;assertTrue(g.interfaceIsSelected(a));
        g.variousSettings[17]=258;assertFalse(g.interfaceIsSelected(a));
        Trace.saw(3,256);Trace.saw(3,257);Trace.saw(3,258);Trace.saw(4,257);
    }

    @Test public void archiveRetainsPairedUnsignedByteAndBigEndianShortValues()throws Exception{
        RSInterface[] a=RSInterface.interfaceCache;
        Field b=RSInterface.class.getDeclaredField("spriteCache");b.setAccessible(true);Object c=b.get(null);
        try{
            RSInterface.unpack(new StreamLoader(Base64.getDecoder().decode("AABTAABUMUFZJlNZJWOVJwAAC+Xg/TFAAACAEAAAAUAAAAgAAKAAVFGTaJobQZqp5NT1NPUFXM/dU0gCTMiEsEvSnrhWixrsTBw/EydbIBsXckU4UJAlY5Un")),null,null);
            RSInterface d=RSInterface.interfaceCache[2];
            assertArrayEquals(new int[]{2,3,4},d.conditionOperators);assertArrayEquals(new int[]{0,32768,65535},d.conditionValues);
            assertNull(RSInterface.interfaceCache[9].conditionOperators);assertNull(RSInterface.interfaceCache[9].conditionValues);
            RSInterface e=RSInterface.interfaceCache[14];assertArrayEquals(new int[]{255},e.conditionOperators);assertArrayEquals(new int[]{257},e.conditionValues);
            d.valueIndexArray=new int[][]{{20,-1,0},{20,32769,0},{20,65534,0}};assertTrue(g.interfaceIsSelected(d));
            d.valueIndexArray[2][1]=65535;assertFalse(g.interfaceIsSelected(d));
            e.valueIndexArray=new int[][]{{20,257,0}};assertTrue(g.interfaceIsSelected(e));e.valueIndexArray[0][1]=1;assertFalse(g.interfaceIsSelected(e));
        }finally{RSInterface.interfaceCache=a;b.set(null,c);}
    }
}
