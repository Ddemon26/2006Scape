package util;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import util.collections.Node;
import util.collections.NodeList;

public class NodeListTest {

  private NodeList list;
  private Node node1;
  private Node node2;

  @Before
  public void setUp() {
    list = new NodeList();
    node1 = new Node();
    node2 = new Node();
  }

  @Test
  public void testInsertHeadAndPop() {
    list.insertHead(node1);
    Node result = list.popHead();
    assertSame("Should pop the inserted node", node1, result);
    assertNull("List should be empty after pop", list.popHead());
  }

  @Test
  public void testInsertTailOrder() {
    list.insertHead(node1);
    list.insertTail(node2);

    Node first = list.getFirst();
    Node second = list.getNext();

    assertSame("First node should be node1", node1, first);
    assertSame("Second node should be node2", node2, second);
    assertNull("No more nodes expected", list.getNext());
  }

  @Test
  public void testRemoveAllClearsList() {
    list.insertHead(node1);
    list.insertHead(node2);
    list.removeAll();
    assertNull("List should be empty after removeAll", list.popHead());
  }
}
