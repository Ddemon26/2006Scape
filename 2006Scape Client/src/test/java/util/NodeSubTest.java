package util;

import org.junit.Test;
import util.collections.NodeSub;

import static org.junit.Assert.*;

public class NodeSubTest {
    @Test
    public void testUnlinkSubRemovesFromList() {
        NodeSub a = new NodeSub();
        NodeSub b = new NodeSub();
        NodeSub c = new NodeSub();

        a.nextNodeSub = b;
        b.prevNodeSub = a;
        b.nextNodeSub = c;
        c.prevNodeSub = b;

        b.unlinkSub();

        assertNull(b.prevNodeSub);
        assertNull(b.nextNodeSub);
        assertSame(c.prevNodeSub, a);
        assertSame(a.nextNodeSub, c);
    }

    @Test
    public void testUnlinkSubWithoutLinks() {
        NodeSub node = new NodeSub();
        node.unlinkSub();
        assertNull(node.prevNodeSub);
        assertNull(node.nextNodeSub);
    }
}
