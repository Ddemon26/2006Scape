import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class StreamAllocationTest {
    private Field countField;
    private NodeList pool;
    private int savedCount;
    private final List<Node> savedNodes = new ArrayList<Node>();

    @Before public void isolatePool() throws Exception {
        countField = Stream.class.getDeclaredField("pooledStreamCount"); countField.setAccessible(true);
        Field poolField = Stream.class.getDeclaredField("pooledStreams"); poolField.setAccessible(true);
        pool = (NodeList) poolField.get(null);
        synchronized (pool) {
            savedCount = countField.getInt(null);
            Node node; while ((node = pool.popTail()) != null) savedNodes.add(node);
            countField.setInt(null, 0);
        }
    }

    @After public void restorePool() throws Exception {
        synchronized (pool) {
            pool.removeAll();
            for (Node node : savedNodes) pool.insertHead(node);
            countField.setInt(null, savedCount);
        }
    }

    @Test public void constructorBorrowsExactArrayIncludingEmptyAndNull() {
        for (byte[] bytes : new byte[][] {null, new byte[0], new byte[]{17, -1, 93}}) {
            Stream s = new Stream(bytes);
            assertSame(bytes, s.buffer); assertEquals(0, s.currentOffset);
            assertEquals(0, s.bitOffset); assertNull(s.encryption);
            assertNull(s.next); assertNull(s.prev); assertNull(s.nextNodeSub); assertNull(s.prevNodeSub);
            if (bytes != null && bytes.length > 0) {
                bytes[0] = 31; assertEquals(31, s.readUnsignedByte());
                s.currentOffset = 1; s.writeByte(42); assertEquals(42, bytes[1]);
                assertEquals(93, bytes[2]);
            }
        }
    }

    @Test public void emptyPoolAllocatesIndependentZeroFilled5000ByteStreams() throws Exception {
        Stream a = Stream.acquire(), b = Stream.acquire();
        assertNotSame(a, b); assertNotSame(a.buffer, b.buffer);
        assertEquals(5000, a.buffer.length); assertArrayEquals(new byte[5000], a.buffer);
        assertArrayEquals(new byte[5000], b.buffer); assertEquals(0, a.currentOffset); assertEquals(0, b.currentOffset);
        assertEquals(0, a.bitOffset); assertNull(a.encryption); assertEquals(0, countField.getInt(null));
        a.writeByte(91); assertEquals(0, b.buffer[0]); assertNull(pool.popTail());
    }

    @Test public void positiveCountReusesTailIdentityAndResetsOnlyByteCursor() throws Exception {
        Stream first = new Stream(new byte[]{11, 22, 33}), second = new Stream(new byte[0]);
        byte[] bytes = first.buffer; first.currentOffset = 123; first.bitOffset = 37; first.id = 987;
        first.encryption = new ISAACRandomGen(new int[]{17, 8, 3, 29});
        ISAACRandomGen keys = first.encryption, reference = new ISAACRandomGen(new int[]{17, 8, 3, 29});
        NodeSub previous = new NodeSub(), next = new NodeSub(); first.prevNodeSub = previous; first.nextNodeSub = next;
        pool.insertHead(first); pool.insertHead(second); countField.setInt(null, 2);
        assertSame(first, Stream.acquire()); assertEquals(1, countField.getInt(null));
        assertEquals(0, first.currentOffset); assertEquals(37, first.bitOffset); assertEquals(987, first.id);
        assertSame(bytes, first.buffer); assertArrayEquals(new byte[]{11, 22, 33}, bytes);
        assertSame(keys, first.encryption); assertEquals(reference.getNextKey(), keys.getNextKey());
        assertSame(previous, first.prevNodeSub); assertSame(next, first.nextNodeSub);
        assertNull(first.next); assertNull(first.prev);
        assertSame(second, Stream.acquire()); assertEquals(0, countField.getInt(null));
        assertEquals(0, second.buffer.length); assertNull(pool.popTail());
    }

    @Test public void nonpositiveCountLeavesQueuedNodeUntouchedAndAllocates() throws Exception {
        for (int count : new int[]{0, -1, Integer.MIN_VALUE}) {
            Stream queued = new Stream(new byte[]{91}); queued.currentOffset = 7;
            pool.insertHead(queued); countField.setInt(null, count);
            Stream result = Stream.acquire(); assertNotSame(queued, result);
            assertEquals(5000, result.buffer.length); assertEquals(count, countField.getInt(null));
            assertEquals(7, queued.currentOffset); assertSame(queued, pool.popTail());
        }
    }

    @Test public void positiveCountWithEmptyListDecrementsThenFallsBackToAllocation() throws Exception {
        for (int count : new int[]{1, 2, Integer.MAX_VALUE}) {
            countField.setInt(null, count); Stream result = Stream.acquire();
            assertEquals(count - 1, countField.getInt(null)); assertEquals(5000, result.buffer.length);
            assertArrayEquals(new byte[5000], result.buffer); assertEquals(0, result.currentOffset);
        }
    }

    @Test public void reusedStreamCanRetainNullBufferWithoutAllocation() throws Exception {
        Stream queued = new Stream(null); queued.currentOffset = -1; queued.bitOffset = 99;
        pool.insertHead(queued); countField.setInt(null, 1);
        assertSame(queued, Stream.acquire()); assertNull(queued.buffer);
        assertEquals(0, queued.currentOffset); assertEquals(99, queued.bitOffset);
        assertEquals(0, countField.getInt(null));
    }

    @Test public void wrongNodeTypeRetainsDecrementAndTailRemovalBeforeClassCastFailure() throws Exception {
        Node wrong = new Node(); pool.insertHead(wrong); countField.setInt(null, 1);
        try { Stream.acquire(); fail("existing unchecked cast must fail"); }
        catch (ClassCastException expected) { }
        assertEquals(0, countField.getInt(null)); assertNull(wrong.next); assertNull(wrong.prev);
        assertNull(pool.popTail());
    }
}
