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
        d.dataType = type; d.ID = id; d.incomplete = incomplete;
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
        assertTrue(d.incomplete);
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
        assertEquals(1, geti("uncompletedCount"));
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
        assertTrue(d.incomplete);
        assertNull(list("cacheLookupQueue").popTail());
        assertEquals(1, fetcher.getNodeCount());
    }

    @Test public void closeRequestWriteFailureIncrementsAnInt1349AndSuccessResets() throws Exception {
        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        set("socket", new Socket());
        set("outputStream", sink);
        set("inputStream", new ByteArrayInputStream(new byte[0]));
        OnDemandData d = new OnDemandData();
        d.dataType = 2; d.ID = 0x0304; d.incomplete = true;
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
}
