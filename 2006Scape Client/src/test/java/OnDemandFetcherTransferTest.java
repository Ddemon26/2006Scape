import static org.junit.Assert.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.zip.GZIPOutputStream;
import org.junit.Before;
import org.junit.Test;

/** OnDemandFetcher.readData, sendRequest (was closeRequest), method558, getNextNode and the three queues, original names only. */
public class OnDemandFetcherTransferTest {
    private OnDemandFetcher fetcher;
    private Game game;

    private static Field field(Class<?> c, String n) throws Exception {
        Field f = c.getDeclaredField(n);
        f.setAccessible(true);
        return f;
    }
    private static sun.misc.Unsafe unsafe() throws Exception {
        return (sun.misc.Unsafe) field(sun.misc.Unsafe.class, "theUnsafe").get(null);
    }
    private Object get(String n) throws Exception { return field(OnDemandFetcher.class, n).get(fetcher); }
    private void set(String n, Object v) throws Exception { field(OnDemandFetcher.class, n).set(fetcher, v); }
    private int geti(String n) throws Exception { return field(OnDemandFetcher.class, n).getInt(fetcher); }
    private void call(String n) throws Exception {
        Method m = OnDemandFetcher.class.getDeclaredMethod(n);
        m.setAccessible(true);
        m.invoke(fetcher);
    }

    @Before public void setUp() throws Exception {
        fetcher = new OnDemandFetcher();
        game = (Game) unsafe().allocateInstance(Game.class);
        CacheStore[] stores = new CacheStore[5];
        for (int i = 0; i < 5; i++) {
            java.io.File d = java.io.File.createTempFile("odf", ".dat");
            java.io.File x = java.io.File.createTempFile("odf", ".idx");
            d.deleteOnExit(); x.deleteOnExit();
            stores[i] = new CacheStore(new java.io.RandomAccessFile(d, "rw"), new java.io.RandomAccessFile(x, "rw"), i);
        }
        field(Game.class, "cacheStores").set(game, stores);
        set("clientInstance", game);
        for (int t = 0; t < 4; t++) {
            ((int[][]) get("versions"))[t] = new int[600];
            ((int[][]) get("crcs"))[t] = new int[600];
            ((byte[][]) get("fileStatus"))[t] = new byte[600];
        }
        set("openSocketTime", System.currentTimeMillis() + 3000000L);
    }

    private NodeList list(String n) throws Exception { return (NodeList) get(n); }

    private OnDemandData request(int type, int id, boolean incomplete) throws Exception {
        OnDemandData d = new OnDemandData();
        d.dataType = type; d.ID = id; d.urgent = incomplete;
        list("requested").insertHead(d);
        return d;
    }

    private static byte[] header(int type, int id, int size, int chunk) {
        return new byte[] {(byte) type, (byte) (id >> 8), (byte) id, (byte) (size >> 8), (byte) size, (byte) chunk};
    }

    private void feed(byte[]... parts) throws Exception {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        for (byte[] p : parts) b.write(p);
        set("inputStream", new ByteArrayInputStream(b.toByteArray()));
    }

    private static byte[] pattern(int n, int seed) {
        byte[] r = new byte[n];
        for (int i = 0; i < n; i++) r[i] = (byte) (i * 7 + seed);
        return r;
    }

    private static OutputStream failing() {
        return new OutputStream() {
            public void write(int b) throws IOException { throw new IOException("x"); }
        };
    }

    @Test public void headerSelectsRequestByTypeAndIdAndSizesBufferFromThirdField() throws Exception {
        OnDemandData other = request(1, 0x0102, true);
        OnDemandData other2 = request(2, 0x0103, true);
        OnDemandData want = request(2, 0x0102, true);
        feed(header(2, 0x0102, 1234, 0));
        call("readData");
        assertSame(want, get("current"));
        assertEquals(1234, want.buffer.length);
        assertNull(other.buffer);
        assertNull(other2.buffer);
        assertEquals(0, geti("completedSize"));
        assertEquals(500, geti("expectedSize"));
    }

    @Test public void chunkIndexScalesStartOffsetBy500AndLastChunkIsShort() throws Exception {
        OnDemandData d = request(0, 9, true);
        d.buffer = new byte[1234];
        feed(header(0, 9, 1234, 2));
        call("readData");
        assertEquals(1000, geti("completedSize"));
        assertEquals(234, geti("expectedSize"));
        feed(header(0, 9, 1234, 1));
        set("expectedSize", 0);
        call("readData");
        assertEquals(500, geti("completedSize"));
        assertEquals(500, geti("expectedSize"));
    }

    @Test public void continuationChunkWithoutStartDropsConnection() throws Exception {
        request(0, 9, true);
        feed(header(0, 9, 1234, 1));
        call("readData");
        assertNull(get("inputStream"));
        assertEquals(0, geti("expectedSize"));
    }

    @Test public void zeroSizeIsRejection() throws Exception {
        OnDemandData d = request(1, 5, true);
        d.buffer = new byte[10];
        feed(header(1, 5, 0, 0));
        call("readData");
        assertNull(d.buffer);
        assertNull(get("current"));
        assertSame(d, list("completedRequests").popTail());
        OnDemandData e = request(1, 6, false);
        feed(header(1, 6, 0, 0));
        call("readData");
        assertNull(list("completedRequests").popTail());
        assertNull(list("requested").firstFromTail());
        assertNull(e.buffer);
    }

    @Test public void bodyIsCopiedAtChunkOffsetAndCompletionQueuesNode() throws Exception {
        OnDemandData d = request(1, 4, true);
        byte[] data = pattern(700, 3);
        feed(header(1, 4, 700, 0), java.util.Arrays.copyOfRange(data, 0, 500));
        call("readData");
        call("readData");
        assertNull(list("completedRequests").popTail());
        assertEquals(0, geti("expectedSize"));
        feed(header(1, 4, 700, 1), java.util.Arrays.copyOfRange(data, 500, 700));
        call("readData");
        call("readData");
        assertArrayEquals(data, d.buffer);
        assertSame(d, list("completedRequests").popTail());
    }

    @Test public void completedNonIncompleteModelIsRequeuedAsType93() throws Exception {
        OnDemandData d = request(3, 4, false);
        feed(header(3, 4, 10, 0), pattern(10, 1));
        call("readData");
        call("readData");
        assertTrue(d.urgent);
        assertEquals(93, d.dataType);
        assertSame(d, list("completedRequests").popTail());
        request(1, 4, false);
        feed(header(1, 4, 10, 0), pattern(10, 1));
        call("readData");
        call("readData");
        assertNull(list("completedRequests").popTail());
    }

    @Test public void unmatchedHeaderDiscardsBodyIntoIoBuffer() throws Exception {
        feed(header(2, 77, 20, 0), pattern(20, 5));
        call("readData");
        assertNull(get("current"));
        call("readData");
        assertEquals(0, geti("expectedSize"));
        byte[] io = (byte[]) get("ioBuffer");
        assertEquals(pattern(20, 5)[19], io[19]);
    }

    @Test public void handleFailedMovesAClass19_1368ToRequested() throws Exception {
        OnDemandData d = new OnDemandData();
        d.dataType = 2; d.ID = 3;
        ((byte[][]) get("fileStatus"))[2][3] = 1;
        list("cacheMissQueue").insertHead(d);
        call("handleFailed");
        assertNull(list("cacheMissQueue").popTail());
        assertSame(d, list("requested").firstFromTail());
        assertEquals(1, geti("urgentInFlightCount"));
        assertEquals(0, ((byte[][]) get("fileStatus"))[2][3]);
    }

    @Test public void checkReceivedSplitsClass19_1370ByCacheHit() throws Exception {
        OnDemandData miss = new OnDemandData();
        miss.dataType = 0; miss.ID = 1;
        list("cacheLookupQueue").insertHead(miss);
        call("checkReceived");
        assertSame(miss, list("cacheMissQueue").popTail());
        assertNull(list("completedRequests").popTail());
        assertNull(list("cacheLookupQueue").popTail());
        byte[] payload = pattern(30, 9);
        byte[] stored = new byte[32];
        System.arraycopy(payload, 0, stored, 0, 30);
        stored[30] = 0; stored[31] = 5;
        java.util.zip.CRC32 c = new java.util.zip.CRC32();
        c.update(payload, 0, 30);
        ((int[][]) get("versions"))[0][2] = 5;
        ((int[][]) get("crcs"))[0][2] = (int) c.getValue();
        game.cacheStores[1].writeFile(stored.length, stored, 2);
        OnDemandData hit = new OnDemandData();
        hit.dataType = 0; hit.ID = 2;
        list("cacheLookupQueue").insertHead(hit);
        call("checkReceived");
        assertSame(hit, list("completedRequests").popTail());
        assertArrayEquals(stored, hit.buffer);
        assertNull(list("cacheMissQueue").popTail());
    }

    @Test public void method558QueuesIncompleteRequestOnceInClass19_1370() throws Exception {
        ((int[][]) get("versions"))[1][7] = 4;
        fetcher.requestFile(1, 7);
        fetcher.requestFile(1, 7);
        fetcher.requestFile(1, 8);
        OnDemandData d = (OnDemandData) list("cacheLookupQueue").popTail();
        assertEquals(1, d.dataType);
        assertEquals(7, d.ID);
        assertTrue(d.urgent);
        assertNull(list("cacheLookupQueue").popTail());
        assertEquals(1, fetcher.getNodeCount());
    }

    @Test public void closeRequestWriteFailureIncrementsAnInt1349AndSuccessResets() throws Exception {
        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        set("socket", new Socket());
        set("outputStream", sink);
        set("inputStream", new ByteArrayInputStream(new byte[0]));
        OnDemandData d = new OnDemandData();
        d.dataType = 2; d.ID = 0x0304; d.urgent = true;
        Method m = OnDemandFetcher.class.getDeclaredMethod("sendRequest", OnDemandData.class);
        m.setAccessible(true);
        m.invoke(fetcher, d);
        assertEquals(-10000, geti("connectionFailureCount"));
        assertArrayEquals(new byte[] {2, 3, 4, 2}, sink.toByteArray());
        set("outputStream", failing());
        set("socket", new Socket());
        set("inputStream", new ByteArrayInputStream(new byte[0]));
        m.invoke(fetcher, d);
        assertEquals(-9999, geti("connectionFailureCount"));
        assertNull(get("outputStream"));
        set("socket", new Socket());
        set("outputStream", failing());
        m.invoke(fetcher, d);
        assertEquals(-9998, geti("connectionFailureCount"));
    }

    @Test public void getNextNodeInflatesBufferToExactLength() throws Exception {
        byte[] data = pattern(300, 2);
        ByteArrayOutputStream z = new ByteArrayOutputStream();
        GZIPOutputStream g = new GZIPOutputStream(z);
        g.write(data);
        g.close();
        OnDemandData d = new OnDemandData();
        d.buffer = z.toByteArray();
        list("completedRequests").insertHead(d);
        OnDemandData r = fetcher.getNextNode();
        assertSame(d, r);
        assertArrayEquals(data, r.buffer);
        assertNull(fetcher.getNextNode());
    }

    @Test public void getVersionCountIndexesByDataType() throws Exception {
        ((int[][]) get("versions"))[2] = new int[13];
        assertEquals(13, fetcher.getVersionCount(2));
        assertEquals(600, fetcher.getVersionCount(0));
    }

    // Pass 197: request-node locals and in-flight counters, original names only.
    private OnDemandData node(int type, int id) {
        OnDemandData d = new OnDemandData();
        d.dataType = type; d.ID = id;
        return d;
    }

    @Test public void requestFileDuplicateScanNeedsSameTypeAndIdAndNewNodeGoesToBothQueues() throws Exception {
        ((int[][]) get("versions"))[1][7] = 4;
        ((int[][]) get("versions"))[2][7] = 4;
        fetcher.requestFile(1, 7);
        fetcher.requestFile(2, 7);   // same ID, other type: not a duplicate
        fetcher.requestFile(1, 7);   // same type and ID: duplicate
        assertEquals(2, fetcher.getNodeCount());
        OnDemandData first = (OnDemandData) list("cacheLookupQueue").popTail();
        OnDemandData second = (OnDemandData) list("cacheLookupQueue").popTail();
        assertNull(list("cacheLookupQueue").popTail());
        assertEquals(1, first.dataType); assertEquals(7, first.ID); assertTrue(first.urgent);
        assertEquals(2, second.dataType); assertEquals(7, second.ID); assertTrue(second.urgent);
    }

    @Test public void handleFailedCountsRequestedNodesByIncompleteFlag() throws Exception {
        request(0, 1, true);
        request(0, 2, true);
        request(0, 3, false);
        request(1, 4, false);
        request(2, 5, false);
        call("handleFailed");
        assertEquals(2, geti("urgentInFlightCount"));
        assertEquals(3, geti("extraInFlightCount"));
        set("urgentInFlightCount", 77);
        set("extraInFlightCount", 77);
        list("requested").removeAll();
        call("handleFailed");
        assertEquals(0, geti("urgentInFlightCount"));
        assertEquals(0, geti("extraInFlightCount"));
    }

    @Test public void handleFailedMovesOldestMissesUntilTenUrgentInFlight() throws Exception {
        for (int i = 0; i < 8; i++) request(0, 100 + i, true);
        request(0, 200, false);
        OnDemandData[] miss = new OnDemandData[5];
        for (int i = 0; i < 5; i++) {
            miss[i] = node(1, 10 + i);
            list("cacheMissQueue").insertHead(miss[i]);
        }
        ((byte[][]) get("fileStatus"))[1][10] = 3;   // was also a pending extra: counted as loaded
        set("filesLoaded", 0);
        call("handleFailed");
        assertEquals(10, geti("urgentInFlightCount"));
        assertEquals(1, geti("extraInFlightCount"));
        assertEquals(1, geti("filesLoaded"));
        assertEquals(0, ((byte[][]) get("fileStatus"))[1][10]);
        assertSame(miss[2], list("cacheMissQueue").popTail());   // oldest two were taken
        assertSame(miss[1], lastSent());                       // newest entry in requested
        assertSame(miss[3], list("cacheMissQueue").popTail());
        assertSame(miss[4], list("cacheMissQueue").popTail());
        assertNull(list("cacheMissQueue").popTail());
    }

    private OnDemandData lastSent() throws Exception {
        OnDemandData last = null;
        for (OnDemandData d = (OnDemandData) list("requested").firstFromTail(); d != null; d = (OnDemandData) list("requested").nextTowardHead()) {
            last = d;
        }
        return last;
    }

    @Test public void requestExtraFileQueuesNonIncompleteNodeForTypeAndId() throws Exception {
        ((int[][]) get("versions"))[3][9] = 1;
        ((byte[][]) get("fileStatus"))[3][9] = 2;
        set("highestPendingPriority", 2);
        fetcher.requestExtraFile(9, 3);
        OnDemandData d = (OnDemandData) list("extraFileQueue").popTail();
        assertEquals(3, d.dataType);
        assertEquals(9, d.ID);
        assertFalse(d.urgent);
        assertEquals(0, fetcher.getNodeCount());
        assertNull(list("cacheLookupQueue").popTail());
    }

    @Test public void checkReceivedDrainsEveryLookupOldestFirst() throws Exception {
        OnDemandData a = node(0, 11), b = node(1, 12), c = node(2, 13);
        list("cacheLookupQueue").insertHead(a);
        list("cacheLookupQueue").insertHead(b);
        list("cacheLookupQueue").insertHead(c);
        call("checkReceived");
        assertNull(list("cacheLookupQueue").popTail());
        assertSame(a, list("cacheMissQueue").popTail());
        assertSame(b, list("cacheMissQueue").popTail());
        assertSame(c, list("cacheMissQueue").popTail());
    }

    @Test public void getNextNodeReturnsOldestCompletedAndUnlinksItFromNodeSubList() throws Exception {
        ((int[][]) get("versions"))[0][5] = 1;
        ((int[][]) get("versions"))[0][6] = 1;
        fetcher.requestFile(0, 5);
        fetcher.requestFile(0, 6);
        OnDemandData a = (OnDemandData) list("cacheLookupQueue").popTail();
        OnDemandData b = (OnDemandData) list("cacheLookupQueue").popTail();
        list("completedRequests").insertHead(a);
        list("completedRequests").insertHead(b);
        assertEquals(2, fetcher.getNodeCount());
        assertSame(a, fetcher.getNextNode());
        assertNull(a.buffer);
        assertEquals(1, fetcher.getNodeCount());
        assertSame(b, fetcher.getNextNode());
        assertEquals(0, fetcher.getNodeCount());
        assertNull(fetcher.getNextNode());
    }

    // Pass 203: incomplete, waiting, readData's lookup local and the idle counters, original names only.

    @Test public void pass203IncompleteMarksClientRequestsNotDownloadState() throws Exception {
        ((int[][]) get("versions"))[1][7] = 5;
        fetcher.requestFile(1, 7);
        assertTrue("client request", ((OnDemandData) list("cacheLookupQueue").popTail()).urgent);
        ((byte[][]) get("fileStatus"))[1][8] = 2;
        ((int[][]) get("versions"))[1][8] = 5;
        set("highestPendingPriority", 2);
        fetcher.requestExtraFile(8, 1);
        assertFalse("prefetch request", ((OnDemandData) list("extraFileQueue").popTail()).urgent);
        OnDemandData done = request(1, 4, true);
        feed(header(1, 4, 10, 0), pattern(10, 1));
        call("readData");
        call("readData");
        assertArrayEquals(pattern(10, 1), done.buffer);
        assertTrue("still true after the whole file arrived", done.urgent);
        assertSame(done, list("completedRequests").popTail());
        OnDemandData partial = request(1, 6, false);
        feed(header(1, 6, 1000, 0));
        call("readData");
        assertSame(partial, get("current"));
        assertFalse("false while still downloading", partial.urgent);
    }

    @Test public void pass203ReadDataSetsWaitingOnlyWhenItConsumesInput() throws Exception {
        set("madeProgress", false);
        feed(new byte[5]);
        call("readData");
        assertFalse(((Boolean) get("madeProgress")).booleanValue());
        feed(header(2, 77, 20, 0));
        call("readData");
        assertTrue(((Boolean) get("madeProgress")).booleanValue());
        set("madeProgress", false);
        feed(new byte[19]);
        call("readData");
        assertFalse("body not yet available", ((Boolean) get("madeProgress")).booleanValue());
        feed(new byte[20]);
        call("readData");
        assertTrue(((Boolean) get("madeProgress")).booleanValue());
    }

    @Test public void pass203HeaderMatchesOnlyRequestsInTheRequestedList() throws Exception {
        OnDemandData lookupOnly = new OnDemandData();
        lookupOnly.dataType = 1; lookupOnly.ID = 5; lookupOnly.urgent = true;
        list("cacheLookupQueue").insertHead(lookupOnly);
        feed(header(1, 5, 10, 0));
        call("readData");
        assertNull(get("current"));
        assertNull(lookupOnly.buffer);
        set("expectedSize", 0);
        OnDemandData inFlight = request(1, 5, true);
        feed(header(1, 5, 10, 0));
        call("readData");
        assertSame(inFlight, get("current"));
    }

    @Test public void pass203HeaderResetsIdleCountersFromTheMatchOnward() throws Exception {
        OnDemandData a = request(0, 1, true);
        OnDemandData b = request(0, 2, true);
        OnDemandData c = request(0, 3, true);
        a.idleCycles = 30; b.idleCycles = 30; c.idleCycles = 30;
        set("connectionIdleCycles", 600);
        feed(header(2, 77, 20, 0));
        call("readData");
        assertEquals("unmatched header leaves connection counter", 600, geti("connectionIdleCycles"));
        assertEquals(30, a.idleCycles);
        assertEquals(30, b.idleCycles);
        set("expectedSize", 0);
        feed(header(0, 2, 10, 0));
        call("readData");
        assertEquals(0, geti("connectionIdleCycles"));
        assertEquals("entry before the match keeps its count", 30, a.idleCycles);
        assertEquals(0, b.idleCycles);
        assertEquals(0, c.idleCycles);
    }

    // BUG-002 fix: the body check uses the bytes left after the header, not the count from before it.
    @Test public void headerWithPartialBodyWaitsForTheRestInsteadOfOverReading() throws Exception {
        OnDemandData d = request(1, 4, true);
        feed(header(1, 4, 10, 0), pattern(5, 1));   // 11 bytes available before the header, only 5 after it
        call("readData");
        assertSame(d, get("current"));
        assertEquals("waiting for the 10-byte body", 10, geti("expectedSize"));
        assertTrue(((Boolean) get("madeProgress")).booleanValue());
        assertArrayEquals(new byte[10], d.buffer);
        assertNull(list("completedRequests").popTail());
        feed(pattern(10, 1));
        call("readData");
        assertArrayEquals(pattern(10, 1), d.buffer);
        assertEquals(0, geti("expectedSize"));
        assertSame(d, list("completedRequests").popTail());
    }

    @Test public void headerAndWholeBodyInOneCallStillCompleteInThatCall() throws Exception {
        OnDemandData d = request(1, 4, true);
        feed(header(1, 4, 10, 0), pattern(10, 3));
        call("readData");
        assertArrayEquals(pattern(10, 3), d.buffer);
        assertSame(d, list("completedRequests").popTail());
    }
}
