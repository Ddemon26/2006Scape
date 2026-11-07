import static org.junit.Assert.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.Socket;
import org.junit.Before;
import org.junit.Test;

/** OnDemandFetcher.run, sendRequest (was closeRequest), getModelIndex, requestModel, original names only. */
public class OnDemandFetcherRunLoopTest {
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

    @Before public void setUp() throws Exception {
        fetcher = new OnDemandFetcher();
        game = (Game) unsafe().allocateInstance(Game.class);
        field(Game.class, "cacheStores").set(game, new CacheStore[5]);
        set("clientInstance", game);
        set("openSocketTime", System.currentTimeMillis() + 3000000L);
    }

    private void markStore(boolean present) throws Exception {
        CacheStore[] s = new CacheStore[5];
        if (present) {
            s[0] = (CacheStore) unsafe().allocateInstance(CacheStore.class);
        }
        field(Game.class, "cacheStores").set(game, s);
    }

    private OnDemandData request(int type, int id, boolean incomplete, int loopCycle) throws Exception {
        OnDemandData d = new OnDemandData();
        d.dataType = type; d.ID = id; d.urgent = incomplete; d.idleCycles = loopCycle;
        ((NodeList) get("requested")).insertHead(d);
        return d;
    }

    private void sendRequest(OnDemandData d) throws Exception {
        Method m = OnDemandFetcher.class.getDeclaredMethod("sendRequest", OnDemandData.class);
        m.setAccessible(true);
        m.invoke(fetcher, d);
    }

    /** available() counts readData calls (one per inner pass), keeps waiting true and stops the loop on the first call. */
    private class Counting extends InputStream {
        int calls;
        long firstCallAt = -1;
        public int read() { return -1; }
        public int available() {
            calls++;
            if (firstCallAt < 0) firstCallAt = System.currentTimeMillis();
            try {
                set("madeProgress", true);
                set("running", false);
            } catch (Exception e) { throw new RuntimeException(e); }
            return 0;
        }
    }

    private Counting runOneCycle() throws Exception {
        Counting c = new Counting();
        set("inputStream", c);
        set("running", true);
        long start = System.currentTimeMillis();
        fetcher.run();
        c.firstCallAt -= start;
        return c;
    }

    @Test public void successfulWriteSendsFourByteRequestAndKeepsSocketOpen() throws Exception {
        final boolean[] closed = {false};
        ByteArrayOutputStream sink = new ByteArrayOutputStream() {
            public void close() throws IOException { closed[0] = true; }
        };
        Socket s = new Socket();
        set("socket", s);
        set("outputStream", sink);
        OnDemandData d = new OnDemandData();
        d.dataType = 3; d.ID = 0x1234; d.urgent = false;
        sendRequest(d);
        assertArrayEquals(new byte[] {3, 0x12, 0x34, 1}, sink.toByteArray());
        game.loggedIn = true;
        sendRequest(d);
        assertEquals(0, sink.toByteArray()[7]);
        d.urgent = true;
        sendRequest(d);
        assertEquals(2, sink.toByteArray()[11]);
        assertEquals(12, sink.size());
        assertSame(s, get("socket"));
        assertSame(sink, get("outputStream"));
        assertFalse(closed[0]);
    }

    @Test public void innerPassesEndAtFiveWhenNothingUncompleted() throws Exception {
        markStore(true);
        assertEquals(5, runOneCycle().calls);
    }

    @Test public void innerPassesRunToOneHundredWhileAnIncompleteRequestIsPending() throws Exception {
        markStore(true);
        request(0, 5, true, 0);
        assertEquals(100, runOneCycle().calls);
    }

    @Test public void cycleSleepsFiftyWithStoreAndNoPriorityElseTwenty() throws Exception {
        markStore(true);
        long t50 = runOneCycle().firstCallAt;
        assertTrue("t50=" + t50, t50 >= 45);
        long best20 = Long.MAX_VALUE;
        for (int n = 0; n < 5; n++) {
            fetcher = new OnDemandFetcher();
            set("clientInstance", game);
            set("clientInstance", game);
            byte[][] status = (byte[][]) get("fileStatus");
            for (int t = 0; t < 4; t++) status[t] = new byte[4];
            set("highestPendingPriority", 1);
            best20 = Math.min(best20, runOneCycle().firstCallAt);
        }
        assertTrue("best20=" + best20, best20 >= 18 && best20 < 45);
        fetcher = new OnDemandFetcher();
        set("clientInstance", game);
        markStore(false);
        long none = runOneCycle().firstCallAt;
        assertTrue("none=" + none, none >= 18 && none < 45);
    }

    @Test public void incompleteRequestAtLoopCycle50IsResentAndResetAndOnlyIncompleteTicks() throws Exception {
        markStore(true);
        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        set("socket", new Socket());
        set("outputStream", sink);
        OnDemandData done = request(1, 7, false, 10);
        OnDemandData due = request(2, 0x0102, true, 50);
        OnDemandData fresh = request(0, 4, true, 3);
        runOneCycle();
        assertEquals(0, due.idleCycles);
        assertEquals(4, fresh.idleCycles);
        assertEquals("complete entries do not tick while an incomplete one exists", 10, done.idleCycles);
        assertArrayEquals(new byte[] {2, 1, 2, 2}, sink.toByteArray());
        assertEquals(1, geti("connectionIdleCycles"));
    }

    @Test public void atFortyNineAnIncompleteRequestIsNotYetResent() throws Exception {
        markStore(true);
        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        set("socket", new Socket());
        set("outputStream", sink);
        OnDemandData d = request(2, 9, true, 49);
        runOneCycle();
        assertEquals(50, d.idleCycles);
        assertEquals(0, sink.size());
    }

    @Test public void withNoIncompleteRequestCompletedOnesTickAndResendAndLoopCycleCounts() throws Exception {
        markStore(true);
        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        set("socket", new Socket());
        set("outputStream", sink);
        OnDemandData done = request(1, 7, false, 50);
        runOneCycle();
        assertEquals(0, done.idleCycles);
        assertArrayEquals(new byte[] {1, 0, 7, 1}, sink.toByteArray());
        assertEquals(1, geti("connectionIdleCycles"));
    }

    @Test public void noRequestsResetsLoopCycleAndClearsStatus() throws Exception {
        markStore(true);
        set("connectionIdleCycles", 40);
        fetcher.statusString = "busy";
        runOneCycle();
        assertEquals(0, geti("connectionIdleCycles"));
        assertEquals("", fetcher.statusString);
    }

    @Test public void modelIndexIsUnsignedByteAtModelId() throws Exception {
        set("modelIndices", new byte[] {(byte) 200, 7, (byte) 0x80});
        assertEquals(200, fetcher.getModelIndex(0));
        assertEquals(7, fetcher.getModelIndex(1));
        assertEquals(128, fetcher.getModelIndex(2));
    }

    @Test public void requestModelQueuesFileTypeZeroWithTheGivenId() throws Exception {
        fetcher.versions[0] = new int[] {0, 5, 5, 5};
        fetcher.requestModel(3);
        OnDemandData d = (OnDemandData) ((NodeList) get("cacheLookupQueue")).popTail();
        assertEquals(0, d.dataType);
        assertEquals(3, d.ID);
        assertTrue(d.urgent);
    }

    // Pass 203: waiting, the connection and write counters, original names only.

    /** available() stops the outer loop but, unlike Counting, never sets waiting itself. */
    private class Quiet extends InputStream {
        int calls;
        public int read() { return -1; }
        public int available() {
            calls++;
            try { set("running", false); } catch (Exception e) { throw new RuntimeException(e); }
            return 0;
        }
    }

    private Quiet runQuietCycle() throws Exception {
        Quiet q = new Quiet();
        set("inputStream", q);
        set("running", true);
        fetcher.run();
        return q;
    }

    @Test public void pass203InnerPollRepeatsOnlyAfterAPassThatDidWork() throws Exception {
        markStore(false);
        assertEquals("idle pass ends the poll loop", 1, runQuietCycle().calls);
        fetcher = new OnDemandFetcher();
        set("clientInstance", game);
        set("openSocketTime", System.currentTimeMillis() + 3000000L);
        fetcher.versions[0] = new int[] {5, 5};
        fetcher.crcs[0] = new int[2];
        ((byte[][]) get("fileStatus"))[0] = new byte[2];
        OnDemandData lookup = new OnDemandData();
        lookup.dataType = 0; lookup.ID = 1;
        ((NodeList) get("cacheLookupQueue")).insertHead(lookup);
        assertEquals("cache-lookup work buys exactly one more pass", 2, runQuietCycle().calls);
        assertFalse(((Boolean) get("madeProgress")).booleanValue());
    }

    @Test public void pass203ConnectionIsDroppedAboveSevenFiftyIdleCycles() throws Exception {
        markStore(true);
        Socket s = new Socket();
        set("socket", s);
        set("outputStream", new ByteArrayOutputStream());
        request(0, 5, true, 0);
        set("connectionIdleCycles", 749);
        runOneCycle();
        assertEquals(750, geti("connectionIdleCycles"));
        assertSame("750 is not yet over the limit", s, get("socket"));
        runOneCycle();
        assertEquals(751, geti("connectionIdleCycles"));
        assertNull(get("socket"));
        assertNull(get("inputStream"));
        assertNull(get("outputStream"));
    }

    @Test public void pass203WriteCounterSendsKeepAliveAfterFiveHundredAndResetsOnRequestWrite() throws Exception {
        markStore(false);
        game.loggedIn = true;
        ByteArrayOutputStream sink = new ByteArrayOutputStream();
        set("socket", new Socket());
        set("outputStream", sink);
        set("cyclesSinceLastWrite", 499);
        runOneCycle();
        assertEquals(500, geti("cyclesSinceLastWrite"));
        assertEquals(0, sink.size());
        runOneCycle();
        assertEquals(0, geti("cyclesSinceLastWrite"));
        assertArrayEquals(new byte[] {0, 0, 0, 10}, sink.toByteArray());
        game.loggedIn = false;
        set("cyclesSinceLastWrite", 7);
        runOneCycle();
        assertEquals("not counted while logged out", 7, geti("cyclesSinceLastWrite"));
        set("cyclesSinceLastWrite", 300);
        OnDemandData d = new OnDemandData();
        d.dataType = 1; d.ID = 2;
        sendRequest(d);
        assertEquals(0, geti("cyclesSinceLastWrite"));
        set("socket", null);
        set("cyclesSinceLastWrite", 300);
        sendRequest(d);
        assertEquals("no write, no reset", 300, geti("cyclesSinceLastWrite"));
    }

    @Test public void pass203FailedKeepAliveForcesConnectionCounterPastTheLimit() throws Exception {
        markStore(false);
        game.loggedIn = true;
        set("socket", new Socket());
        set("outputStream", new java.io.OutputStream() {
            public void write(int b) throws IOException { throw new IOException("x"); }
        });
        set("cyclesSinceLastWrite", 500);
        runOneCycle();
        assertEquals(5000, geti("connectionIdleCycles"));
    }
}
