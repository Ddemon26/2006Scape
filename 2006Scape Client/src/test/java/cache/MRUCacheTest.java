package cache;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import util.collections.NodeSub;

public class MRUCacheTest {

    private MRUCache cache;
    private NodeSub testNode1;
    private NodeSub testNode2;
    private NodeSub testNode3;

    @Before
    public void setUp() {
        cache = new MRUCache(2);
        testNode1 = new NodeSub();
        testNode2 = new NodeSub();
        testNode3 = new NodeSub();
    }

    @Test
    public void testConstructorInitializesCache() {
        assertNotNull("Cache should be created successfully", cache);
    }

    @Test
    public void testPutAndGetSingleNode() {
        long testKey = 100L;
        
        cache.put(testNode1, testKey);
        NodeSub retrievedNode = cache.get(testKey);
        
        assertSame("Should retrieve the same node that was put", testNode1, retrievedNode);
    }

    @Test
    public void testGetNonExistentNode() {
        long nonExistentKey = 999L;
        
        NodeSub retrievedNode = cache.get(nonExistentKey);
        
        assertNull("Should return null for non-existent key", retrievedNode);
    }

    @Test
    public void testCacheCapacityEviction() {
        long key1 = 1L;
        long key2 = 2L;
        long key3 = 3L;
        
        cache.put(testNode1, key1);
        cache.put(testNode2, key2);
        cache.put(testNode3, key3);
        
        NodeSub node1Retrieved = cache.get(key1);
        NodeSub node2Retrieved = cache.get(key2);
        NodeSub node3Retrieved = cache.get(key3);
        
        assertNull("First node should be evicted", node1Retrieved);
        assertNotNull("Second node should still be in cache", node2Retrieved);
        assertNotNull("Third node should be in cache", node3Retrieved);
    }

    @Test
    public void testMRUBehaviorOnGet() {
        long key1 = 1L;
        long key2 = 2L;
        long key3 = 3L;
        
        cache.put(testNode1, key1);
        cache.put(testNode2, key2);
        
        cache.get(key1);
        
        cache.put(testNode3, key3);
        
        NodeSub node1Retrieved = cache.get(key1);
        NodeSub node2Retrieved = cache.get(key2);
        NodeSub node3Retrieved = cache.get(key3);
        
        assertNotNull("Node1 should still be in cache after being accessed", node1Retrieved);
        assertNull("Node2 should be evicted as it wasn't recently used", node2Retrieved);
        assertNotNull("Node3 should be in cache", node3Retrieved);
    }

    @Test
    public void testReplaceNodeWithSameKey() {
        long testKey = 50L;
        
        cache.put(testNode1, testKey);
        cache.put(testNode2, testKey);
        
        NodeSub retrievedNode = cache.get(testKey);
        
        assertNotNull("Should retrieve a node", retrievedNode);
    }

    @Test
    public void testUnlinkAll() {
        long key1 = 1L;
        long key2 = 2L;
        
        cache.put(testNode1, key1);
        cache.put(testNode2, key2);
        
        cache.unlinkAll();
        
        NodeSub node1Retrieved = cache.get(key1);
        NodeSub node2Retrieved = cache.get(key2);
        
        assertNull("All nodes should be removed after unlinkAll", node1Retrieved);
        assertNull("All nodes should be removed after unlinkAll", node2Retrieved);
    }

    @Test
    public void testCacheAfterUnlinkAll() {
        cache.put(testNode1, 1L);
        cache.put(testNode2, 2L);
        cache.unlinkAll();
        
        cache.put(testNode3, 3L);
        NodeSub retrievedNode = cache.get(3L);
        
        assertSame("Cache should work normally after unlinkAll", testNode3, retrievedNode);
    }

    @Test
    public void testZeroCapacityCache() {
        try {
            MRUCache zeroCache = new MRUCache(0);
            zeroCache.put(testNode1, 1L);
            NodeSub retrievedNode = zeroCache.get(1L);
            assertNull("Zero capacity cache should not store any nodes", retrievedNode);
        } catch (RuntimeException e) {
            assertTrue("Zero capacity cache may throw runtime exception", true);
        }
    }

    @Test
    public void testSingleCapacityCache() {
        MRUCache singleCache = new MRUCache(1);
        
        singleCache.put(testNode1, 1L);
        singleCache.put(testNode2, 2L);
        
        NodeSub node1Retrieved = singleCache.get(1L);
        NodeSub node2Retrieved = singleCache.get(2L);
        
        assertNull("First node should be evicted in single capacity cache", node1Retrieved);
        assertSame("Second node should be in single capacity cache", testNode2, node2Retrieved);
    }

    @Test
    public void testLargeCapacityCache() {
        MRUCache largeCache = new MRUCache(1000);
        
        for (int i = 0; i < 100; i++) {
            NodeSub node = new NodeSub();
            largeCache.put(node, i);
        }
        
        for (int i = 0; i < 100; i++) {
            NodeSub retrievedNode = largeCache.get(i);
            assertNotNull("Large cache should store many nodes", retrievedNode);
        }
    }
}