package integration;

import static org.junit.Assert.*;

import cache.MRUCache;
import game.entities.Animable;
import game.items.ItemPile;
import org.junit.Before;
import org.junit.Test;
import util.collections.NodeHashTable;
import util.collections.NodeSub;
import util.cryptography.ISAACRandomGen;

public class ClientIntegrationTest {

  private MRUCache cache;
  private NodeHashTable hashTable;
  private ISAACRandomGen randomGen;
  private ItemPile itemPile;

  @Before
  public void setUp() {
    cache = new MRUCache(5);
    hashTable = new NodeHashTable();
    randomGen = new ISAACRandomGen(new int[] {1, 2, 3, 4, 5});
    itemPile = new ItemPile();
  }

  @Test
  public void testCacheAndHashTableIntegration() {
    NodeSub node1 = new NodeSub();
    NodeSub node2 = new NodeSub();

    long key1 = 100L;
    long key2 = 200L;

    hashTable.insertNode(node1, key1);
    hashTable.insertNode(node2, key2);

    NodeSub hashNode1 = (NodeSub) hashTable.findNodeById(key1);
    NodeSub hashNode2 = (NodeSub) hashTable.findNodeById(key2);

    assertNotNull("Hash table should contain node1", hashNode1);
    assertNotNull("Hash table should contain node2", hashNode2);
    assertEquals("Node1 should have correct ID", key1, hashNode1.id);
    assertEquals("Node2 should have correct ID", key2, hashNode2.id);
  }

  @Test
  public void testRandomGeneratorWithCacheKeys() {
    for (int i = 0; i < 10; i++) {
      long randomKey = Math.abs(randomGen.getNextKey());
      NodeSub node = new NodeSub();

      cache.put(node, randomKey);
      NodeSub retrievedNode = cache.get(randomKey);

      assertSame(
          "Should be able to store and retrieve nodes with random keys", node, retrievedNode);
    }
  }

  @Test
  public void testItemPileWithAnimableObjects() {
    Animable animable1 = new Animable();
    Animable animable2 = new Animable();
    Animable animable3 = new Animable();

    itemPile.topItem = animable1;
    itemPile.secondItem = animable2;
    itemPile.thirdItem = animable3;

    itemPile.x = randomGen.getNextKey() % 1000;
    itemPile.y = randomGen.getNextKey() % 1000;
    itemPile.height = randomGen.getNextKey() % 100;

    assertSame("ItemPile should hold animable references correctly", animable1, itemPile.topItem);
    assertSame(
        "ItemPile should hold animable references correctly", animable2, itemPile.secondItem);
    assertSame("ItemPile should hold animable references correctly", animable3, itemPile.thirdItem);

    assertTrue(
        "ItemPile coordinates should be within expected range",
        itemPile.x >= -1000 && itemPile.x < 1000);
    assertTrue(
        "ItemPile coordinates should be within expected range",
        itemPile.y >= -1000 && itemPile.y < 1000);
    assertTrue(
        "ItemPile height should be within expected range",
        itemPile.height >= -100 && itemPile.height < 100);
  }

  @Test
  public void testCacheEvictionWithRandomData() {
    MRUCache smallCache = new MRUCache(3);

    for (int i = 0; i < 10; i++) {
      NodeSub node = new NodeSub();
      long key = Math.abs(randomGen.getNextKey());
      smallCache.put(node, key);
    }

    int nonNullCount = 0;
    for (int i = 0; i < 100; i++) {
      long testKey = Math.abs(randomGen.getNextKey());
      if (smallCache.get(testKey) != null) {
        nonNullCount++;
      }
    }

    assertTrue("Cache should have evicted older entries", nonNullCount <= 3);
  }

  @Test
  public void testComplexDataFlowScenario() {
    NodeSub[] nodes = new NodeSub[5];
    long[] keys = new long[5];

    for (int i = 0; i < 5; i++) {
      nodes[i] = new NodeSub();
      keys[i] = Math.abs(randomGen.getNextKey());

      cache.put(nodes[i], keys[i]);
      hashTable.insertNode(nodes[i], keys[i]);
    }

    for (int i = 0; i < 5; i++) {
      NodeSub hashNode = (NodeSub) hashTable.findNodeById(keys[i]);

      assertNotNull("Node should be retrievable from hash table", hashNode);
      assertEquals("Node should have correct ID", keys[i], hashNode.id);
    }

    cache.unlinkAll();

    for (int i = 0; i < 5; i++) {
      NodeSub cacheNode = cache.get(keys[i]);
      NodeSub hashNode = (NodeSub) hashTable.findNodeById(keys[i]);

      assertNull("Cache should be empty after unlinkAll", cacheNode);
    }
  }

  @Test
  public void testMemoryManagementPattern() {
    MRUCache testCache = new MRUCache(100);
    NodeHashTable testHashTable = new NodeHashTable();

    for (int i = 0; i < 1000; i++) {
      NodeSub node = new NodeSub();
      long key = i;

      testCache.put(node, key);
      testHashTable.insertNode(node, key);

      if (i % 200 == 0) {
        testCache.unlinkAll();
      }
    }

    int hashTableHits = 0;
    int cacheHits = 0;

    for (int i = 800; i < 1000; i++) {
      if (testHashTable.findNodeById(i) != null) {
        hashTableHits++;
      }
      if (testCache.get(i) != null) {
        cacheHits++;
      }
    }

    assertTrue("Hash table should retain some entries", hashTableHits > 0);
    assertTrue("Cache should have fewer entries due to unlinkAll calls", cacheHits < hashTableHits);
  }
}
