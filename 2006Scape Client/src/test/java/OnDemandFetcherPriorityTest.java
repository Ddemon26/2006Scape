import static org.junit.Assert.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.junit.Before;
import org.junit.Test;

/** OnDemandFetcher.method560 / aClass19_1344 / anInt1332 (via method563 and method568), original names only. */
public class OnDemandFetcherPriorityTest {
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
        int[][] versions = (int[][]) get("versions");
        byte[][] status = (byte[][]) get("fileStatus");
        for (int t = 0; t < 4; t++) {
            versions[t] = new int[20];
            ((int[][]) get("crcs"))[t] = new int[20];
            status[t] = new byte[20];
        }
        set("openSocketTime", System.currentTimeMillis() + 3000000L); // sendRequest returns early, no IO
        set("totalFiles", 100);
    }

    private NodeList queue() throws Exception { return (NodeList) get("extraFileQueue"); }
    private NodeList requested() throws Exception { return (NodeList) get("requested"); }
    private int levelField() throws Exception { return field(OnDemandFetcher.class, "highestPendingPriority").getInt(fetcher); }
    private void level(int v) throws Exception { field(OnDemandFetcher.class, "highestPendingPriority").setInt(fetcher, v); }
    private byte[][] status() throws Exception { return (byte[][]) get("fileStatus"); }
    private int[][] versions() throws Exception { return (int[][]) get("versions"); }

    private void arm(int type, int id) throws Exception {
        versions()[type][id] = 7;
        status()[type][id] = 2;
        level(2);
    }

    @Test public void queuesOneNodeWithIdAndTypeWhenAllGuardsHold() throws Exception {
        arm(3, 11);
        fetcher.requestExtraFile(11, 3);
        OnDemandData d = (OnDemandData) queue().popTail();
        assertNotNull(d);
        assertEquals(11, d.ID);
        assertEquals(3, d.dataType);
        assertFalse(d.incomplete);
        assertNull(queue().popTail());
        // argument order: swapped call indexes versions[11], which does not exist
        arm(3, 11);
        try {
            fetcher.requestExtraFile(3, 11);
            fail("expected (id, type) order");
        } catch (ArrayIndexOutOfBoundsException expected) { }
    }

    @Test public void eachGuardBlocksQueuing() throws Exception {
        arm(1, 5);
        fetcher.requestExtraFile(6, 1);               // other id: no version
        assertNull(queue().popTail());
        status()[1][5] = 0;                     // not flagged pending
        fetcher.requestExtraFile(5, 1);
        assertNull(queue().popTail());
        status()[1][5] = 2;
        versions()[1][5] = 0;                   // no version
        fetcher.requestExtraFile(5, 1);
        assertNull(queue().popTail());
        versions()[1][5] = 7;
        level(0);                               // no pending level
        fetcher.requestExtraFile(5, 1);
        assertNull(queue().popTail());
        level(1);                               // any nonzero level is enough (not compared to status)
        fetcher.requestExtraFile(5, 1);
        assertNotNull(queue().popTail());
        level(2);
        ((CacheStore[]) field(Game.class, "cacheStores").get(game))[0] = null; // no cache
        fetcher.requestExtraFile(5, 1);
        assertNull(queue().popTail());
    }

    @Test public void method563RaisesLevelToMaxPriorityOnly() throws Exception {
        versions()[0][4] = 9;
        versions()[0][5] = 9;
        versions()[0][6] = 9;
        fetcher.markExtraFileForDownload((byte) 2, 0, 4);
        assertEquals(2, levelField());
        assertEquals(2, status()[0][4]);
        fetcher.markExtraFileForDownload((byte) 5, 0, 5);
        assertEquals(5, levelField());
        fetcher.markExtraFileForDownload((byte) 1, 0, 6);
        assertEquals(5, levelField());          // lower priority does not lower it
        assertEquals(1, status()[0][6]);
    }

    private void drain() throws Exception {
        Method m = OnDemandFetcher.class.getDeclaredMethod("dispatchExtraFileRequests");
        m.setAccessible(true);
        m.invoke(fetcher);
    }

    private CacheStore[] stores() throws Exception {
        return (CacheStore[]) field(Game.class, "cacheStores").get(game);
    }

    private byte[] cached(int type, int id, int version) throws Exception {
        byte[] bytes = {11, 37, 91, (byte) (version >>> 8), (byte) version};
        java.util.zip.CRC32 crc = new java.util.zip.CRC32();
        crc.update(bytes, 0, bytes.length - 2);
        versions()[type][id] = version;
        ((int[][]) get("crcs"))[type][id] = (int) crc.getValue();
        assertTrue(stores()[type + 1].writeFile(bytes.length, bytes, id));
        return bytes;
    }

    @Test public void method563ChecksExactStoreAndFileAndBothCacheChecksums() throws Exception {
        byte[] bytes = cached(2, 13, 0x1234);
        fetcher.markExtraFileForDownload((byte) 7, 2, 13);
        assertEquals(0, status()[2][13]);
        assertEquals(100, get("totalFiles"));
        // Correct version but wrong payload CRC must mark this exact cell.
        bytes[0] ^= 1;
        assertTrue(stores()[3].writeFile(bytes.length, bytes, 13));
        fetcher.markExtraFileForDownload((byte) 7, 2, 13);
        assertEquals(7, status()[2][13]);
        assertEquals(101, get("totalFiles"));
        // Correct payload CRC but wrong trailing big-endian version also marks.
        bytes = cached(1, 17, 0x1234);
        bytes[4] ^= 1;
        assertTrue(stores()[2].writeFile(bytes.length, bytes, 17));
        fetcher.markExtraFileForDownload((byte) 3, 1, 17);
        assertEquals(3, status()[1][17]);
        assertEquals(7, levelField());
        // Identical bytes in a different type's store do not satisfy this type.
        cached(0, 9, 0x1234);
        versions()[3][9] = 0x1234;
        ((int[][]) get("crcs"))[3][9] = ((int[][]) get("crcs"))[0][9];
        fetcher.markExtraFileForDownload((byte) 2, 3, 9);
        assertEquals(2, status()[3][9]);
        assertEquals(0, status()[0][9]);
        assertEquals(103, get("totalFiles"));
        assertNull(queue().popTail()); // marking alone does not enqueue
    }

    @Test public void method563GuardsAndSignedByteBoundaries() throws Exception {
        fetcher.markExtraFileForDownload((byte) 6, 3, 19); // version zero
        assertEquals(100, get("totalFiles"));
        versions()[3][19] = 1;
        CacheStore original = stores()[0];
        stores()[0] = null;
        fetcher.markExtraFileForDownload((byte) 6, 3, 19);
        assertEquals(0, status()[3][19]);
        assertEquals(0, levelField());
        assertEquals(100, get("totalFiles"));
        stores()[0] = original;
        for (byte b : new byte[] {0, -1, Byte.MIN_VALUE, Byte.MAX_VALUE, 1}) {
            fetcher.markExtraFileForDownload(b, 3, 19);
            assertEquals(b, status()[3][19]);
        }
        assertEquals(127, levelField()); // signed, not unsigned
        assertEquals(105, get("totalFiles")); // includes repeated/zero marks
    }

    @Test public void method566ClearsOnlyExplicitQueueAndIsRepeatable() throws Exception {
        arm(2, 19);
        fetcher.requestExtraFile(19, 2);
        fetcher.requestExtraFile(19, 2);
        OnDemandData pending = new OnDemandData();
        requested().insertHead(pending);
        fetcher.clearExtraFileQueue();
        fetcher.clearExtraFileQueue();
        assertNull(queue().popTail());
        assertSame(pending, requested().popTail());
        assertEquals(2, status()[2][19]);
        assertEquals(2, levelField());
        assertEquals(100, get("totalFiles"));
    }

    @Test public void method568UsesTypeRowsAndLastFileIndexIncludingEmptyRow() throws Exception {
        byte[][] cells = status();
        cells[0] = new byte[] {0, 2};
        cells[1] = new byte[0];
        cells[2] = new byte[] {0, 0, 1};
        cells[3] = new byte[] {0, 0, 0, 2};
        level(2);
        drain();
        int[][] expected = {{0, 1}, {3, 3}, {2, 2}};
        for (int[] pair : expected) {
            OnDemandData node = (OnDemandData) requested().popTail();
            assertNotNull(node);
            assertEquals(pair[0], node.dataType);
            assertEquals(pair[1], node.ID);
            assertFalse(node.incomplete);
            assertEquals(0, cells[pair[0]][pair[1]]);
        }
        assertNull(requested().popTail());
        assertEquals(3, get("completedCount"));
        assertEquals(3, get("filesLoaded"));
        assertEquals("Loading extra files - 3%", fetcher.statusString);
        assertEquals(0, levelField());
    }

    @Test public void method568BlocksOnMandatoryRequestsAndStopsAtTenThenResumes() throws Exception {
        for (int id = 0; id < 12; id++) status()[2][id] = 2;
        level(2);
        set("uncompletedCount", 1);
        drain();
        assertEquals(2, levelField());
        assertNull(requested().popTail());
        set("uncompletedCount", 0);
        drain();
        assertEquals(10, get("completedCount"));
        assertEquals(2, status()[2][10]);
        for (int id = 0; id < 10; id++) {
            OnDemandData node = (OnDemandData) requested().popTail();
            assertEquals(2, node.dataType);
            assertEquals(id, node.ID);
        }
        drain(); // outstanding extra count at ten blocks even with queue empty
        assertNull(requested().popTail());
        set("completedCount", 0);
        drain();
        assertEquals(10, ((OnDemandData) requested().popTail()).ID);
        assertEquals(11, ((OnDemandData) requested().popTail()).ID);
        assertNull(requested().popTail());
        assertEquals(0, levelField());
    }

    @Test public void method568SkipsStaleQueuedNodesAndCapsExplicitQueueAtTen() throws Exception {
        level(1);
        OnDemandData stale = new OnDemandData();
        stale.dataType = 3; stale.ID = 19;
        queue().insertHead(stale); // zero status: must be skipped
        for (int id = 0; id < 11; id++) {
            versions()[1][id] = 1;
            status()[1][id] = 1;
            fetcher.requestExtraFile(id, 1);
        }
        set("filesLoaded", 100); // must not exceed totalFiles
        drain();
        assertEquals(10, get("completedCount"));
        assertEquals(100, get("filesLoaded"));
        assertEquals("Loading extra files - 100%", fetcher.statusString);
        for (int id = 0; id < 10; id++) {
            OnDemandData node = (OnDemandData) requested().popTail();
            assertEquals(1, node.dataType);
            assertEquals(id, node.ID);
        }
        assertNull(requested().popTail());
        assertEquals(10, ((OnDemandData) queue().popTail()).ID);
        assertEquals(1, status()[1][10]);
    }

    @Test public void method568ScansFromLevelDownAndDecrementsIt() throws Exception {
        status()[0][1] = 1; status()[0][2] = 3; status()[0][3] = 2; status()[0][4] = 3;
        level(3);
        drain();
        assertEquals(0, levelField());
        // requested order (popTail = oldest): status-3 files first, then 2, then 1
        int[] order = new int[4];
        for (int i = 0; i < 4; i++) order[i] = ((OnDemandData) requested().popTail()).ID;
        assertArrayEquals(new int[] {2, 4, 3, 1}, order);
        for (int i = 1; i <= 4; i++) assertEquals(0, status()[0][i]);
    }

    @Test public void method568NeverScansAboveLevelAndQueuedFilesGoFirst() throws Exception {
        status()[0][1] = 3; status()[0][2] = 1;
        level(1);
        drain();
        assertEquals(3, status()[0][1]);        // above level: untouched
        assertEquals(0, status()[0][2]);
        assertEquals(0, levelField());
        assertEquals(2, ((OnDemandData) requested().popTail()).ID);
        assertNull(requested().popTail());
        // explicit queue entries are served before the scan
        status()[0][2] = 1; status()[0][8] = 1; status()[0][9] = 1;
        level(1);
        OnDemandData d = new OnDemandData();
        d.dataType = 0; d.ID = 9; d.incomplete = false;
        queue().insertHead(d);
        drain();
        assertEquals(9, ((OnDemandData) requested().popTail()).ID);
    }
}
