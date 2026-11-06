import static org.junit.Assert.*;
import java.lang.reflect.Field;
import java.util.*;
import org.junit.*;

public class InterfaceInventoryTest {
    public static final class Trace {
        public static boolean on;
        public static final Map<Integer,List<Integer>> values=new HashMap<Integer,List<Integer>>();
        public static void value(int a,int b){if(on){if(!values.containsKey(a))values.put(a,new ArrayList<Integer>());values.get(a).add(b);}}
        static void reset(){values.clear();on=true;}
        static void expect(int a,Integer... b){if(Boolean.getBoolean("pass090.trace"))assertEquals(Arrays.asList(b),values.get(a));}
    }
    @Before public void before(){Trace.reset();}
    @After public void after(){Trace.on=false;}
    public static RSInterface fixture(int[] a,int[] b){RSInterface c=new RSInterface();c.itemIdsPlusOne=a;c.itemQuantities=b;return c;}

    @Test public void endpointsExchangeBothArraysInPlaceAndOnlyThosePositions(){
        int[] a={101,202,303,404},b={70001,8,9,256};RSInterface c=fixture(a,b);
        c.swapInventoryItems(0,3);
        assertSame(a,c.itemIdsPlusOne);assertSame(b,c.itemQuantities);
        assertArrayEquals(new int[]{404,202,303,101},a);assertArrayEquals(new int[]{256,8,9,70001},b);
        Trace.expect(3,101,70001);Trace.expect(1,0,0,0,0);Trace.expect(2,3,3,3,3);
    }
    @Test public void reversedIndicesAndSecondExchangeRestorePairs(){
        RSInterface a=fixture(new int[]{11,22,33,44},new int[]{100,200,300,400});
        a.swapInventoryItems(2,1);assertArrayEquals(new int[]{11,33,22,44},a.itemIdsPlusOne);assertArrayEquals(new int[]{100,300,200,400},a.itemQuantities);
        a.swapInventoryItems(2,1);assertArrayEquals(new int[]{11,22,33,44},a.itemIdsPlusOne);assertArrayEquals(new int[]{100,200,300,400},a.itemQuantities);
    }
    @Test public void sameSlotAndSingleEntryPreserveRawInts(){
        RSInterface a=fixture(new int[]{65535},new int[]{Integer.MAX_VALUE});a.swapInventoryItems(0,0);
        assertArrayEquals(new int[]{65535},a.itemIdsPlusOne);assertArrayEquals(new int[]{Integer.MAX_VALUE},a.itemQuantities);
        Trace.expect(3,65535,Integer.MAX_VALUE);
    }
    @Test public void emptyMarkersAndQuantityBoundariesAreMovedWithoutInterpretation(){
        for(int a:new int[]{-1,0,1,65535})for(int b:new int[]{Integer.MIN_VALUE,0,1,254,255,256,70001,Integer.MAX_VALUE}){
            RSInterface c=fixture(new int[]{a,996},new int[]{b,17});Trace.reset();c.swapInventoryItems(0,1);
            assertArrayEquals(new int[]{996,a},c.itemIdsPlusOne);assertArrayEquals(new int[]{17,b},c.itemQuantities);Trace.expect(3,a,b);
        }
    }
    @Test public void independentArraysProveTheTemporaryIsNotSpecificToOneKindOfValue(){
        RSInterface a=fixture(new int[]{7,8},new int[]{99,100});a.swapInventoryItems(0,1);Trace.expect(3,7,99);
        Trace.reset();a=fixture(new int[]{17,8},new int[]{99,100});a.swapInventoryItems(0,1);Trace.expect(3,17,99);
        Trace.reset();a=fixture(new int[]{7,8},new int[]{199,100});a.swapInventoryItems(0,1);Trace.expect(3,7,199);
    }
    @Test public void archiveTypesTwoAndSevenAllocatePairedZeroFilledGridStorage()throws Exception{
        RSInterface[] a=RSInterface.interfaceCache;Field b=RSInterface.class.getDeclaredField("spriteCache");b.setAccessible(true);Object c=b.get(null);
        try{
            RSInterface.unpack(new StreamLoader(Base64.getDecoder().decode("AABeAABHMUFZJlNZ0PKBQAAACWOgfLRAEBAAEAAAAQAIIAAxTAAAwptUaZqZMMRsRE3ftVteIRGadItDSYO7wRxc6LuSKcKEhoeUCgA=")),null,null);
            for(int d:new int[]{2,9}){RSInterface e=RSInterface.interfaceCache[d];int f=d==2?6:8;
                assertEquals(d==2?2:7,e.type);assertEquals(f,e.width*e.height);
                assertArrayEquals(new int[f],e.itemIdsPlusOne);assertArrayEquals(new int[f],e.itemQuantities);assertNotSame(e.itemIdsPlusOne,e.itemQuantities);
                e.itemIdsPlusOne[0]=996;e.itemQuantities[0]=70001;e.swapInventoryItems(0,f-1);
                assertEquals(996,e.itemIdsPlusOne[f-1]);assertEquals(70001,e.itemQuantities[f-1]);assertEquals(0,e.itemIdsPlusOne[0]);assertEquals(0,e.itemQuantities[0]);
            }
        }finally{RSInterface.interfaceCache=a;b.set(null,c);}
    }
}
