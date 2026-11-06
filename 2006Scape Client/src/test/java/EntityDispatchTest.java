import static org.junit.Assert.*;
import java.util.*;
import org.junit.*;

public class EntityDispatchTest {
    public static final class Trace {
        public static boolean on;
        public static final Map<Integer,List<Object>> values=new TreeMap<Integer,List<Object>>();
        public static void reset(){values.clear();on=true;}
        public static void value(int a,Object b){if(on){if(!values.containsKey(a))values.put(a,new ArrayList<Object>());values.get(a).add(b);}}
        public static void value(int a,int b){value(a,Integer.valueOf(b));}
    }
    public static class Probe extends Game {
        final List<Entity> calls=new ArrayList<Entity>();
        int failAt=-1;
        @Override public void method96(Entity a){calls.add(a);if(calls.size()==failAt)throw new IllegalStateException("controlled callback");}
    }
    private Probe g;
    private Player saved;
    @Before public void before(){g=new Probe();saved=Game.myPlayer;Trace.reset();}
    @After public void after(){Game.myPlayer=saved;Trace.on=false;}
    private void slots(int a,Object... b){if(Boolean.getBoolean("pass095.trace"))assertEquals(Arrays.asList(b),Trace.values.get(a));}

    @Test public void npcSparseOrderNullDuplicateAndCount(){
        NPC a=new NPC(),b=new NPC(),c=new NPC();
        g.npcArray[19]=a;g.npcArray[3]=b;g.npcArray[90]=c;
        g.npcIndices=new int[]{19,7,3,19,90};g.npcCount=4;
        g.advanceNpcEntities();assertEquals(Arrays.<Entity>asList(a,b,a),g.calls);
        slots(1,0,1,2,3,4);slots(2,19,7,3,19);slots(3,a,null,b,a);
        assertFalse(g.calls.contains(c));
    }
    @Test public void npcEmptyAndNegativeCountsDoNotReadArrays(){
        g.npcArray=null;g.npcIndices=null;
        for(int a:new int[]{0,-1,Integer.MIN_VALUE}){g.npcCount=a;g.advanceNpcEntities();}
        assertTrue(g.calls.isEmpty());slots(1,0,0,0);assertNull(Trace.values.get(2));
    }
    @Test public void playersUseArrayLocalBeforeSparseListAndIgnoreGlobalPointer(){
        Player a=new Player(),b=new Player(),c=new Player(),d=new Player();
        Game.myPlayer=d;g.playerArray[g.myPlayerIndex]=a;g.playerArray[19]=b;g.playerArray[3]=c;
        g.playerIndices=new int[]{19,7,3,19,g.myPlayerIndex,90};g.playerCount=5;
        g.playerArray[90]=d;
        g.advancePlayerEntities();assertEquals(Arrays.<Entity>asList(a,b,c,b,a),g.calls);
        slots(11,-1,0,1,2,3,4,5);slots(12,g.myPlayerIndex,19,7,3,19,g.myPlayerIndex);slots(13,a,b,null,c,b,a);
        assertFalse(g.calls.contains(d));
    }
    @Test public void zeroPlayerCountStillVisitsLocalSlotAndNullLocalIsSkipped(){
        Player a=new Player();g.playerArray[g.myPlayerIndex]=a;g.playerIndices=null;g.playerCount=0;
        g.advancePlayerEntities();assertEquals(Arrays.<Entity>asList(a),g.calls);slots(11,-1,0);slots(12,g.myPlayerIndex);
        g.calls.clear();g.playerArray[g.myPlayerIndex]=null;Trace.reset();g.advancePlayerEntities();
        assertTrue(g.calls.isEmpty());slots(13,(Object)null);
    }
    @Test public void negativePlayerCountsExcludeEvenLocalWithoutReadingArrays(){
        g.playerArray=null;g.playerIndices=null;
        for(int a:new int[]{-1,Integer.MIN_VALUE}){g.playerCount=a;g.advancePlayerEntities();}
        assertTrue(g.calls.isEmpty());slots(11,-1,-1);assertNull(Trace.values.get(12));
    }
    @Test public void badIndicesAndShortListsFailAfterEarlierCalls(){
        NPC a=new NPC();g.npcArray[3]=a;g.npcIndices=new int[]{3,-1};g.npcCount=2;
        try{g.advanceNpcEntities();fail();}catch(ArrayIndexOutOfBoundsException expected){}
        assertEquals(Arrays.<Entity>asList(a),g.calls);
        g.calls.clear();g.npcIndices=new int[]{3};
        try{g.advanceNpcEntities();fail();}catch(ArrayIndexOutOfBoundsException expected){}
        assertEquals(Arrays.<Entity>asList(a),g.calls);
        Player b=new Player();g.playerArray[g.myPlayerIndex]=b;g.playerIndices=new int[]{g.playerArray.length};g.playerCount=1;g.calls.clear();
        try{g.advancePlayerEntities();fail();}catch(ArrayIndexOutOfBoundsException expected){}
        assertEquals(Arrays.<Entity>asList(b),g.calls);
    }
    @Test public void callbackFailureStopsDispatchWithoutSwallowingIt(){
        NPC a=new NPC(),b=new NPC();g.npcArray[3]=a;g.npcArray[19]=b;
        g.npcIndices=new int[]{3,19};g.npcCount=2;g.failAt=1;
        try{g.advanceNpcEntities();fail();}catch(IllegalStateException expected){}
        assertEquals(Arrays.<Entity>asList(a),g.calls);
        Player c=new Player(),d=new Player();g.playerArray[g.myPlayerIndex]=c;g.playerArray[3]=d;g.playerIndices=new int[]{3};g.playerCount=1;g.calls.clear();
        try{g.advancePlayerEntities();fail();}catch(IllegalStateException expected){}
        assertEquals(Arrays.<Entity>asList(c),g.calls);
    }
}
