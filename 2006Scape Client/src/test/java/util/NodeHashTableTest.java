package util;

import org.junit.Before;
import org.junit.Test;
import util.collections.Node;
import util.collections.NodeHashTable;

import static org.junit.Assert.*;

public class NodeHashTableTest {

    private NodeHashTable hashTable;
    private Node testNode;

    @Before
    public void setUp() {
        hashTable = new NodeHashTable();
        testNode = new Node();
    }

    @Test
    public void testConstructorInitializesCorrectly() {
        assertNotNull("Hash table should be created successfully", hashTable);
    }

    @Test
    public void testInsertAndFindNode() {
        long testId = 12345L;
        
        hashTable.insertNode(testNode, testId);
        Node foundNode = hashTable.findNodeById(testId);
        
        assertNotNull("Should find inserted node", foundNode);
        assertEquals("Found node should have correct ID", testId, foundNode.id);
        assertSame("Should return the same node instance", testNode, foundNode);
    }

    @Test
    public void testFindNonExistentNode() {
        long nonExistentId = 99999L;
        
        Node foundNode = hashTable.findNodeById(nonExistentId);
        
        assertNull("Should return null for non-existent node", foundNode);
    }

    @Test
    public void testInsertMultipleNodes() {
        Node node1 = new Node();
        Node node2 = new Node();
        Node node3 = new Node();
        
        long id1 = 100L;
        long id2 = 200L;
        long id3 = 300L;
        
        hashTable.insertNode(node1, id1);
        hashTable.insertNode(node2, id2);
        hashTable.insertNode(node3, id3);
        
        assertEquals("Should find first node", node1, hashTable.findNodeById(id1));
        assertEquals("Should find second node", node2, hashTable.findNodeById(id2));
        assertEquals("Should find third node", node3, hashTable.findNodeById(id3));
    }

    @Test
    public void testReplaceNode() {
        Node originalNode = new Node();
        Node replacementNode = new Node();
        long testId = 500L;
        
        hashTable.insertNode(originalNode, testId);
        hashTable.insertNode(replacementNode, testId);
        
        Node foundNode = hashTable.findNodeById(testId);
        assertNotNull("Should find a node with the given ID", foundNode);
        assertEquals("Node should have correct ID", testId, foundNode.id);
    }

    @Test
    public void testHashCollisionHandling() {
        Node node1 = new Node();
        Node node2 = new Node();
        
        long id1 = 1024L;
        long id2 = 2048L;
        
        hashTable.insertNode(node1, id1);
        hashTable.insertNode(node2, id2);
        
        assertSame("Should find first node despite potential collision", node1, hashTable.findNodeById(id1));
        assertSame("Should find second node despite potential collision", node2, hashTable.findNodeById(id2));
    }

    @Test
    public void testNegativeIds() {
        Node node = new Node();
        long negativeId = -123L;
        
        hashTable.insertNode(node, negativeId);
        Node foundNode = hashTable.findNodeById(negativeId);
        
        assertNotNull("Should handle negative IDs", foundNode);
        assertEquals("Node should have correct negative ID", negativeId, foundNode.id);
    }

    @Test
    public void testZeroId() {
        Node node = new Node();
        long zeroId = 0L;
        
        hashTable.insertNode(node, zeroId);
        Node foundNode = hashTable.findNodeById(zeroId);
        
        assertNotNull("Should handle zero ID", foundNode);
        assertEquals("Node should have zero ID", zeroId, foundNode.id);
    }

    @Test
    public void testLargeIds() {
        Node node = new Node();
        long largeId = Long.MAX_VALUE;
        
        hashTable.insertNode(node, largeId);
        Node foundNode = hashTable.findNodeById(largeId);
        
        assertNotNull("Should handle large IDs", foundNode);
        assertEquals("Node should have correct large ID", largeId, foundNode.id);
    }

    @Test
    public void testNodeUnlinkingBehavior() {
        Node node1 = new Node();
        Node node2 = new Node();
        long testId = 777L;
        
        hashTable.insertNode(node1, testId);
        hashTable.insertNode(node2, testId);
        
        Node foundNode = hashTable.findNodeById(testId);
        assertNotNull("Should find a node", foundNode);
        assertEquals("Found node should have correct ID", testId, foundNode.id);
        assertNotNull("Found node should be linked", foundNode.next);
        assertNotNull("Found node should be linked", foundNode.prev);
    }
}