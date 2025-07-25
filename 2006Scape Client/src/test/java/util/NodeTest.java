package util;

import org.junit.Test;
import util.collections.Node;

import static org.junit.Assert.*;

public class NodeTest {
    @Test
    public void testUnlinkRemovesFromList() {
        Node a = new Node();
        Node b = new Node();
        Node c = new Node();

        a.next = b;
        b.prev = a;
        b.next = c;
        c.prev = b;

        b.unlink();

        assertNull(b.prev);
        assertNull(b.next);
        assertSame(c.prev, a);
        assertSame(a.next, c);
    }

    @Test
    public void testUnlinkWithoutLinks() {
        Node node = new Node();
        node.unlink();
        assertNull(node.prev);
        assertNull(node.next);
    }
}
