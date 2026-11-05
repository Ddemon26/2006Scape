import org.junit.Test;
import static org.junit.Assert.*;
import java.lang.reflect.Field;
import java.util.*;

public class IntrusiveListTest {
    private static Object field(Object list, String oldName, String newName) throws Exception {
        Field f;
        try { f = list.getClass().getDeclaredField(oldName); }
        catch (NoSuchFieldException renamed) { f = list.getClass().getDeclaredField(newName); }
        f.setAccessible(true); return f.get(list);
    }
    private static List<Node> forward(NodeList list) {
        List<Node> nodes = new ArrayList<Node>();
        for (Node n = list.firstFromHead(); n != null; n = list.nextTowardTail()) nodes.add(n);
        return nodes;
    }
    private static List<Node> backward(NodeList list) {
        List<Node> nodes = new ArrayList<Node>();
        for (Node n = list.firstFromTail(); n != null; n = list.nextTowardHead()) nodes.add(n);
        return nodes;
    }
    private static List<NodeSub> secondary(NodeSubList list) {
        List<NodeSub> nodes = new ArrayList<NodeSub>();
        for (NodeSub n = list.firstFromTail(); n != null; n = list.nextTowardHead()) nodes.add(n);
        return nodes;
    }
    private static void detached(Node n) { assertNull(n.next); assertNull(n.prev); }

    @Test public void emptySentinelsAndSingletonBoundaries() throws Exception {
        NodeList primary = new NodeList(); NodeSubList secondary = new NodeSubList();
        Node p = (Node) field(primary, "head", "sentinel");
        NodeSub s = (NodeSub) field(secondary, "head", "sentinel");
        assertSame(p, p.next); assertSame(p, p.prev);
        assertSame(s, s.nextNodeSub); assertSame(s, s.prevNodeSub);
        assertNull(primary.popTail()); assertNull(primary.firstFromHead()); assertNull(primary.firstFromTail());
        assertNull(secondary.popTail()); assertNull(secondary.firstFromTail()); assertEquals(0, secondary.getNodeCount());
        NodeSub one = new NodeSub(); primary.insertTail(one); secondary.insertHead(one);
        assertEquals(Arrays.asList(one), forward(primary)); assertEquals(Arrays.asList(one), backward(primary));
        assertEquals(Arrays.asList(one), secondary(secondary)); assertEquals(1, secondary.getNodeCount());
        assertSame(one, primary.popTail()); detached(one); assertEquals(1, secondary.getNodeCount());
        assertSame(one, secondary.popTail()); assertNull(one.nextNodeSub); assertNull(one.prevNodeSub);
        assertNull(primary.popTail()); assertNull(secondary.popTail());
    }
    @Test public void insertEndsTraversalAndMisnamedPopHaveObservableOrder() {
        NodeList list = new NodeList(); Node a=new Node(), b=new Node(), c=new Node(), d=new Node();
        list.insertHead(a); list.insertHead(b); list.insertTail(c); list.insertHead(d);
        assertEquals(Arrays.asList(d,b,a,c), forward(list));
        assertEquals(Arrays.asList(c,a,b,d), backward(list));
        assertSame(c,list.popTail()); detached(c);
        assertSame(a,list.popTail()); assertSame(b,list.popTail()); assertSame(d,list.popTail()); assertNull(list.popTail());
    }
    @Test public void primaryMovesAndClearDetachWithoutDuplicatingMembership() {
        NodeList a=new NodeList(), b=new NodeList(); Node x=new Node(), y=new Node(), z=new Node();
        a.insertHead(x); a.insertHead(y); a.insertTail(z); a.insertHead(z);
        assertEquals(Arrays.asList(z,y,x),forward(a));
        b.insertTail(y); assertEquals(Arrays.asList(z,x),forward(a)); assertEquals(Arrays.asList(y),forward(b));
        x.unlink(); x.unlink(); assertEquals(Arrays.asList(z),forward(a));
        a.removeAll(); a.removeAll(); detached(x); detached(z); assertSame(y,b.popTail()); detached(y);
        assertTrue(forward(a).isEmpty()); a.insertTail(x); assertSame(x,a.popTail());
    }
    @Test public void cursorsHoldNextNodeAndAreSharedAcrossDirections() throws Exception {
        NodeList list=new NodeList(); Node a=new Node(), b=new Node(), c=new Node();
        list.insertTail(a);list.insertTail(b);list.insertTail(c);
        assertSame(a,list.firstFromHead()); assertSame(b,field(list,"current","traversalNext"));
        a.unlink(); assertSame(b,list.nextTowardTail()); assertSame(c,list.nextTowardTail()); assertNull(list.nextTowardTail());
        assertNull(field(list,"current","traversalNext"));
        assertSame(c,list.firstFromTail()); assertSame(b,field(list,"current","traversalNext"));
        assertSame(b,list.nextTowardTail()); assertSame(c,list.nextTowardTail()); assertNull(list.nextTowardTail());
        assertSame(b,list.firstFromHead()); assertSame(c,list.firstFromTail()); assertSame(b,list.nextTowardHead()); assertNull(list.nextTowardHead());
        // Existing contract: another next after exhaustion dereferences null.
        try { list.nextTowardHead(); fail(); } catch (NullPointerException expected) { }
    }
    @Test public void secondaryCountOrderPromotionTransferAndTraversalRemoval() throws Exception {
        NodeSubList list=new NodeSubList(), other=new NodeSubList(); NodeSub a=new NodeSub(),b=new NodeSub(),c=new NodeSub();
        list.insertHead(a);list.insertHead(b);list.insertHead(c);assertEquals(3,list.getNodeCount());
        assertEquals(Arrays.asList(a,b,c),secondary(list));list.insertHead(a);
        assertEquals(Arrays.asList(b,c,a),secondary(list));assertEquals(3,list.getNodeCount());
        other.insertHead(c);assertEquals(2,list.getNodeCount());assertEquals(1,other.getNodeCount());
        assertSame(b,list.firstFromTail());assertSame(a,field(list,"current","traversalNext"));
        b.unlinkSub();assertSame(a,list.nextTowardHead());assertNull(list.nextTowardHead());
        assertNull(field(list,"current","traversalNext"));assertSame(a,list.popTail());assertNull(list.popTail());
        assertSame(c,other.popTail());assertEquals(0,other.getNodeCount());
        try { list.nextTowardHead(); fail(); } catch (NullPointerException expected) { }
    }
    @Test public void primaryAndSecondaryMembershipStayIndependent() {
        NodeList primary=new NodeList();NodeSubList secondary=new NodeSubList();NodeSub a=new NodeSub(),b=new NodeSub();
        primary.insertHead(a);primary.insertHead(b);secondary.insertHead(b);secondary.insertHead(a);
        assertEquals(Arrays.asList(b,a),forward(primary));assertEquals(Arrays.asList(b,a),secondary(secondary));
        primary.removeAll();detached(a);detached(b);assertEquals(2,secondary.getNodeCount());
        primary.insertTail(a);assertSame(b,secondary.popTail());assertSame(a,secondary.popTail());
        assertEquals(Arrays.asList(a),forward(primary));assertNull(a.nextNodeSub);assertNull(a.prevNodeSub);
    }
    @Test public void primaryMatchesIndependentSequenceModel() {
        Random random=new Random(4101);Node[] nodes=new Node[17];for(int i=0;i<nodes.length;i++)nodes[i]=new Node();
        NodeList list=new NodeList();List<Node> model=new ArrayList<Node>();
        for(int step=0;step<700;step++) {
            Node n=nodes[random.nextInt(nodes.length)];int op=random.nextInt(5);
            if(op==0){model.remove(n);model.add(0,n);list.insertHead(n);}
            if(op==1){model.remove(n);model.add(n);list.insertTail(n);}
            if(op==2){assertSame(model.isEmpty()?null:model.remove(model.size()-1),list.popTail());}
            if(op==3){model.remove(n);n.unlink();}
            if(op==4 && step%19==0){model.clear();list.removeAll();}
            assertEquals(model,forward(list));List<Node> reversed=new ArrayList<Node>(model);Collections.reverse(reversed);assertEquals(reversed,backward(list));
        }
    }
    @Test public void secondaryMatchesIndependentSequenceModel() {
        Random random=new Random(4102);NodeSub[] nodes=new NodeSub[17];for(int i=0;i<nodes.length;i++)nodes[i]=new NodeSub();
        NodeSubList list=new NodeSubList();List<NodeSub> model=new ArrayList<NodeSub>();
        for(int step=0;step<700;step++) {
            NodeSub n=nodes[random.nextInt(nodes.length)];int op=random.nextInt(3);
            if(op==0){model.remove(n);model.add(n);list.insertHead(n);}
            if(op==1){assertSame(model.isEmpty()?null:model.remove(0),list.popTail());}
            if(op==2){model.remove(n);n.unlinkSub();}
            assertEquals(model,secondary(list));assertEquals(model.size(),list.getNodeCount());
        }
    }
}
