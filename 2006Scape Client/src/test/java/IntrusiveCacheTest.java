import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import org.junit.Test;

public class IntrusiveCacheTest {
    // Reflection accepts both spellings so the assertions are unchanged after renaming.
    private static Object field(Object owner, String original, String renamed) throws Exception {
        Field field;
        try { field = owner.getClass().getDeclaredField(original); }
        catch (NoSuchFieldException absent) { field = owner.getClass().getDeclaredField(renamed); }
        field.setAccessible(true);
        return field.get(owner);
    }

    private static void detached(NodeSub node) {
        assertNull(node.prev); assertNull(node.next);
        assertNull(node.prevNodeSub); assertNull(node.nextNodeSub);
    }

    @Test public void tableKeepsFullSignedLongKeysDespiteSharedLowTenBits() {
        NodeHashTable table = new NodeHashTable();
        long[] keys = {0, 1024, 1L << 32, Long.MIN_VALUE, -1024, Long.MAX_VALUE, -1, 1023};
        Node[] nodes = new Node[keys.length];
        for (int i = 0; i < keys.length; i++) {
            assertNull(table.findNodeByID(keys[i]));
            nodes[i] = new Node(); table.removeFromCache(nodes[i], keys[i]);
        }
        for (int i = 0; i < keys.length; i++) {
            assertSame(nodes[i], table.findNodeByID(keys[i])); assertEquals(keys[i], nodes[i].id);
        }
        assertNull(table.findNodeByID(2048)); assertNull(table.findNodeByID(1022));
    }

    @Test public void tableBuilds1024CircularBucketSentinelsAndUsesLowBits() throws Exception {
        NodeHashTable table = new NodeHashTable();
        Node[] buckets = (Node[]) field(table, "cache", "buckets");
        assertEquals(1024, buckets.length);
        assertEquals(1024, field(table, "size", "bucketCount"));
        for (Node bucket : buckets) { assertSame(bucket, bucket.next); assertSame(bucket, bucket.prev); }
        Node first = new Node(), second = new Node(), other = new Node();
        table.removeFromCache(first, 7); table.removeFromCache(second, 1031); table.removeFromCache(other, 8);
        assertSame(buckets[7], second.prev); assertSame(first, second.next);
        assertSame(second, first.prev); assertSame(buckets[7], first.next);
        assertSame(buckets[8], other.prev); assertSame(buckets[8], other.next);
        first.unlink(); second.unlink(); other.unlink();
        for (Node bucket : buckets) { assertSame(bucket, bucket.next); assertSame(bucket, bucket.prev); }
    }

    @Test public void tableRekeysAndTransfersOnlyPrimaryLinks() {
        NodeHashTable first = new NodeHashTable(), second = new NodeHashTable();
        NodeSub node = new NodeSub(); NodeSubList queue = new NodeSubList(); queue.insertHead(node);
        first.removeFromCache(node, 1); first.removeFromCache(node, 2050);
        assertNull(first.findNodeByID(1)); assertSame(node, first.findNodeByID(2050));
        second.removeFromCache(node, -7);
        assertNull(first.findNodeByID(2050)); assertSame(node, second.findNodeByID(-7));
        assertEquals(1, queue.getNodeCount()); assertSame(node, queue.popTail());
        assertSame(node, second.findNodeByID(-7)); node.unlink(); assertNull(second.findNodeByID(-7));
    }

    @Test public void duplicateKeysKeepOldestMatchRatherThanReplacingIt() {
        NodeHashTable table = new NodeHashTable(); Node first = new Node(), second = new Node();
        table.removeFromCache(first, 12); table.removeFromCache(second, 12);
        assertSame(first, table.findNodeByID(12)); first.unlink();
        assertSame(second, table.findNodeByID(12)); second.unlink(); assertNull(table.findNodeByID(12));
        LruCache cache = new LruCache(2); NodeSub a = new NodeSub(), b = new NodeSub(), c = new NodeSub();
        cache.removeFromCache(a, 12); cache.removeFromCache(b, 12);
        assertSame(a, cache.insertFromCache(12)); // Promotes a; next eviction must remove b.
        cache.removeFromCache(c, 13); detached(b);
        assertSame(a, cache.insertFromCache(12)); assertSame(c, cache.insertFromCache(13));
    }

    @Test public void hitsPromoteMissesDoNotInsertAndOldestIsEvicted() throws Exception {
        LruCache cache = new LruCache(2);
        NodeSub a = new NodeSub(), b = new NodeSub(), c = new NodeSub(), d = new NodeSub();
        assertEquals(2, field(cache, "initialCount", "capacity"));
        assertEquals(2, field(cache, "spaceLeft", "remainingSlots"));
        cache.removeFromCache(a, 0); cache.removeFromCache(b, 1024);
        assertEquals(0, field(cache, "spaceLeft", "remainingSlots"));
        assertSame(a, cache.insertFromCache(0)); assertNull(cache.insertFromCache(2048));
        cache.removeFromCache(c, 2048); detached(b); assertNull(cache.insertFromCache(1024));
        cache.removeFromCache(d, 3072); detached(a); assertNull(cache.insertFromCache(0));
        assertSame(c, cache.insertFromCache(2048)); assertSame(d, cache.insertFromCache(3072));
        assertEquals(0, field(cache, "spaceLeft", "remainingSlots"));
    }

    @Test public void positiveCapacitiesMatchIndependentAccessOrderedMapForUniqueInsertions() {
        for (final int capacity : new int[] {1, 2, 3, 7}) {
            LruCache cache = new LruCache(capacity);
            Map<Long, NodeSub> expected = new LinkedHashMap<Long, NodeSub>(16, 0.75f, true) {
                protected boolean removeEldestEntry(Map.Entry<Long, NodeSub> eldest) { return size() > capacity; }
            };
            Random random = new Random(654321 + capacity);
            for (int step = 0; step < 400; step++) {
                long key = (random.nextInt(13) - 6) * 1024L;
                if (step % 61 == 0) { cache.clear(); expected.clear(); }
                NodeSub hit = expected.get(key);
                assertSame("capacity=" + capacity + " step=" + step, hit, cache.insertFromCache(key));
                if (hit == null && random.nextBoolean()) {
                    NodeSub added = new NodeSub(); expected.put(key, added); cache.removeFromCache(added, key);
                }
                // Read oldest to newest: checking membership restores the same access order in both.
                for (Long present : expected.keySet().toArray(new Long[0])) {
                    assertSame(expected.get(present), cache.insertFromCache(present));
                }
            }
        }
    }

    @Test public void clearDetachesBothLinkPlanesAndRestoresFullCapacity() throws Exception {
        LruCache cache = new LruCache(3); NodeSub[] nodes = {new NodeSub(), new NodeSub(), new NodeSub()};
        for (int i = 0; i < 3; i++) cache.removeFromCache(nodes[i], i);
        cache.clear(); cache.clear();
        for (int i = 0; i < 3; i++) { detached(nodes[i]); assertNull(cache.insertFromCache(i)); }
        assertEquals(3, field(cache, "spaceLeft", "remainingSlots"));
        for (int i = 0; i < 3; i++) cache.removeFromCache(nodes[i], i + 10);
        for (int i = 0; i < 3; i++) assertSame(nodes[i], cache.insertFromCache(i + 10));
        assertEquals(0, field(cache, "spaceLeft", "remainingSlots"));
    }

    @Test public void reinsertingSameNodeStillConsumesSlotAccounting() throws Exception {
        LruCache cache = new LruCache(2); NodeSub a = new NodeSub(), b = new NodeSub();
        cache.removeFromCache(a, 1); cache.removeFromCache(a, 2);
        assertNull(cache.insertFromCache(1)); assertSame(a, cache.insertFromCache(2));
        assertEquals(0, field(cache, "spaceLeft", "remainingSlots"));
        cache.removeFromCache(b, 3); detached(a); assertNull(cache.insertFromCache(2));
        assertSame(b, cache.insertFromCache(3));
    }

    @Test public void evictionSkipsSpecialSentinelAndRemovesNextOldest() throws Exception {
        LruCache cache = new LruCache(1);
        NodeSub sentinel = (NodeSub) field(cache, "emptyNodeSub", "evictionSentinel");
        NodeSubList queue = (NodeSubList) field(cache, "nodeSubList", "recencyList");
        NodeHashTable table = (NodeHashTable) field(cache, "nodeCache", "table");
        table.removeFromCache(sentinel, 99); queue.insertHead(sentinel);
        NodeSub a = new NodeSub(), b = new NodeSub();
        cache.removeFromCache(a, 1); cache.removeFromCache(b, 2);
        detached(sentinel); detached(a); assertNull(cache.insertFromCache(99)); assertNull(cache.insertFromCache(1));
        assertSame(b, cache.insertFromCache(2)); assertEquals(1, queue.getNodeCount());
    }

    @Test public void invalidCapacityAndNullInsertionKeepExistingErrorBehavior() throws Exception {
        PrintStream saved = System.out; ByteArrayOutputStream log = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(log));
            try { new LruCache(0).removeFromCache(new NodeSub(), 8); fail("zero capacity"); }
            catch (RuntimeException expected) { assertEquals(RuntimeException.class, expected.getClass()); }
            try { new NodeHashTable().removeFromCache(null, 77); fail("null table entry"); }
            catch (RuntimeException expected) { assertEquals(RuntimeException.class, expected.getClass()); }
            LruCache cache = new LruCache(1);
            try { cache.removeFromCache(null, 88); fail("null cache entry"); }
            catch (RuntimeException expected) { assertEquals(RuntimeException.class, expected.getClass()); }
            assertEquals(0, field(cache, "spaceLeft", "remainingSlots"));
        } finally { System.setOut(saved); }
        String text = log.toString("UTF-8");
        assertTrue(text.contains("47547,")); assertTrue(text.contains("91499, null, 77, 7,"));
        assertTrue(text.contains("91499, null, 88, 7,")); assertTrue(text.contains("47547, null, 88, 2,"));
        LruCache negative = new LruCache(-1);
        for (int i = 0; i < 3; i++) negative.removeFromCache(new NodeSub(), i);
        assertEquals(-4, field(negative, "spaceLeft", "remainingSlots"));
        for (int i = 0; i < 3; i++) assertNotNull(negative.insertFromCache(i));
    }
}
