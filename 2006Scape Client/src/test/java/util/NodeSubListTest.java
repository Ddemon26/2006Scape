package util;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import util.collections.NodeSub;
import util.collections.NodeSubList;

public class NodeSubListTest {

  private NodeSubList list;
  private NodeSub node1;
  private NodeSub node2;

  @Before
  public void setUp() {
    list = new NodeSubList();
    node1 = new NodeSub();
    node2 = new NodeSub();
  }

  @Test
  public void testInsertHeadAndPopTail() {
    list.insertHead(node1);
    list.insertHead(node2);
    assertSame("Tail should be node1", node1, list.popTail());
    assertSame("Next tail should be node2", node2, list.popTail());
    assertNull("List should be empty after pops", list.popTail());
  }

  @Test
  public void testGetNodeCount() {
    assertEquals(0, list.getNodeCount());
    list.insertHead(node1);
    list.insertHead(node2);
    assertEquals(2, list.getNodeCount());
  }

  @Test
  public void testReverseIteration() {
    list.insertHead(node1);
    list.insertHead(node2);
    NodeSub first = list.reverseGetFirst();
    NodeSub second = list.reverseGetNext();
    assertSame("First returned should be node1", node1, first);
    assertSame("Second returned should be node2", node2, second);
    assertNull("No more elements expected", list.reverseGetNext());
  }
}
