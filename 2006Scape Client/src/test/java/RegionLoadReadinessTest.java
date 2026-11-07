import java.io.ByteArrayOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.zip.GZIPOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/** Controlled completion, region-coordinate and model-availability observations. */
public class RegionLoadReadinessTest {
    private Object savedCache, savedHeaders, savedRequester;
    private int savedPlane;
    private boolean savedLowMem;
    private final List<Integer> requested = new ArrayList<Integer>();

    private static Field field(Class<?> c, String n) throws Exception {
        Field f = c.getDeclaredField(n); f.setAccessible(true); return f;
    }
    @Before public void setUp() throws Exception {
        savedCache = field(ObjectDef.class, "cache").get(null);
        savedHeaders = field(Model.class, "modelHeaders").get(null);
        savedRequester = field(Model.class, "modelRequester").get(null);
        savedPlane = ObjectManager.scenePlaneFilter;
        savedLowMem = ObjectManager.lowMem;
        ObjectManager.lowMem = false;
        Constructor<ObjectDef> ctor = ObjectDef.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        ObjectDef[] cache = new ObjectDef[20];
        for (int i = 0; i < cache.length; i++) {
            cache[i] = ctor.newInstance(); cache[i].type = i;
            field(ObjectDef.class, "modelIds").set(cache[i], new int[] {7 + i});
        }
        field(ObjectDef.class, "cache").set(null, cache);
        Model.initModelHeaders(64, new OnDemandFetcherParent() {
            @Override public void requestModel(int id) { requested.add(id); }
        });
    }
    @After public void tearDown() throws Exception {
        field(ObjectDef.class, "cache").set(null, savedCache);
        field(Model.class, "modelHeaders").set(null, savedHeaders);
        field(Model.class, "modelRequester").set(null, savedRequester);
        ObjectManager.scenePlaneFilter = savedPlane;
        ObjectManager.lowMem = savedLowMem;
    }
    private static class Probe extends Game {
        int builds;
        @Override public void method22() { builds++; }
    }
    private Probe game(int count) {
        Probe g = new Probe();
        g.loadingStage = 1; g.aBoolean1080 = true;
        g.baseX = 3200; g.baseY = 6400;
        g.regionTerrainData = new byte[count][];
        g.regionObjectData = new byte[count][];
        g.regionKeys = new int[count];
        g.regionTerrainFileIds = new int[count]; g.regionObjectFileIds = new int[count];
        Arrays.fill(g.regionKeys, (50 << 8) | 100);
        Arrays.fill(g.regionTerrainFileIds, -1); Arrays.fill(g.regionObjectFileIds, -1);
        g.stream = new Stream(new byte[32]);
        g.stream.encryption = new ISAACRandomGen(new int[4]);
        return g;
    }
    private static void smart(ByteArrayOutputStream b, int n) {
        if (n < 128) b.write(n);
        else { b.write((n + 32768) >> 8); b.write(n + 32768); }
    }
    private static byte[] data(int id, int x, int y) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        smart(b, id + 1); smart(b, (x << 6) + y + 1);
        b.write(10 << 2); b.write(0); b.write(0); return b.toByteArray();
    }
    @Test public void missingFilesHaveDistinctCodesAndMinusOneMeansNoFile() {
        Probe g = game(3);
        g.regionTerrainFileIds[2] = 41; g.regionObjectFileIds[0] = 42;
        assertEquals(-2, g.tryFinishRegionLoad()); // first slot wins, even with later terrain missing
        g.regionObjectData[0] = new byte[] {0};
        assertEquals(-1, g.tryFinishRegionLoad());
        g.regionTerrainData[2] = new byte[] {99}; // presence only; no terrain decoding here
        assertEquals(-4, g.tryFinishRegionLoad());
        g.regionTerrainFileIds[2] = -1; g.regionTerrainData[2] = null;
        assertEquals(-4, g.tryFinishRegionLoad());
        assertEquals(0, g.builds); assertEquals(1, g.loadingStage);
        assertEquals(0, g.stream.currentOffset);
    }
    @Test public void modelsPrecedeTheGuardAndEveryRegionIsVisited() {
        Probe g = game(3);
        g.regionObjectData[0] = data(0, 1, 2);
        g.regionObjectData[1] = new byte[] {0};
        g.regionObjectData[2] = data(1, 3, 4);
        assertEquals(-3, g.tryFinishRegionLoad());
        assertEquals(Arrays.asList(7, 8), requested);
        assertEquals(0, g.builds); assertEquals(1, g.loadingStage);
        assertEquals(0, g.stream.currentOffset);
        try {
            ModelHeader[] h = (ModelHeader[]) field(Model.class, "modelHeaders").get(null);
            h[7] = new ModelHeader(); h[8] = new ModelHeader();
        } catch (Exception e) { throw new AssertionError(e); }
        assertEquals(-4, g.tryFinishRegionLoad());
    }
    @Test public void packedRegionAxesUse64TilesAndIndependentBaseCoordinates() {
        Probe g = game(2); g.regionObjectData[1] = data(0, 1, 2);
        assertEquals(-3, g.tryFinishRegionLoad());
        g.baseX++; assertEquals(-4, g.tryFinishRegionLoad()); // x=0 is excluded
        g.baseX--; g.baseY += 2; assertEquals(-4, g.tryFinishRegionLoad()); // y=0
        g.baseY -= 2;
        g.regionKeys[1] = (51 << 8) | 100;
        g.baseX += 64; assertEquals(-3, g.tryFinishRegionLoad());
        g.baseX -= 64; g.regionKeys[1] = (50 << 8) | 101;
        g.baseY += 64; assertEquals(-3, g.tryFinishRegionLoad());
        g.regionKeys[1] = (101 << 8) | 50;
        assertEquals(-4, g.tryFinishRegionLoad()); // swapping non-symmetric axes is not equivalent
    }
    @Test public void modelChecksExcludeZeroAnd103ButIncludeOneAnd102() {
        Probe g = game(1); g.regionObjectData[0] = data(0, 0, 1);
        assertEquals(-4, g.tryFinishRegionLoad());
        g.baseX--; assertEquals(-3, g.tryFinishRegionLoad());
        g.baseX = 3200 - 102; assertEquals(-3, g.tryFinishRegionLoad());
        g.baseX--; assertEquals(-4, g.tryFinishRegionLoad());
        g.baseX = 3200; g.regionObjectData[0] = data(0, 1, 0);
        assertEquals(-4, g.tryFinishRegionLoad());
        g.baseY--; assertEquals(-3, g.tryFinishRegionLoad());
        g.baseY = 6400 - 102; assertEquals(-3, g.tryFinishRegionLoad());
        g.baseY--; assertEquals(-4, g.tryFinishRegionLoad());
    }
    @Test public void constructedScenesCheckAtTenInsteadOfThePackedRegionOffset() {
        Probe g = game(1); g.regionKeys[0] = 0;
        g.regionObjectData[0] = data(0, 0, 0);
        assertEquals(-4, g.tryFinishRegionLoad());
        g.aBoolean1159 = true; assertEquals(-3, g.tryFinishRegionLoad());
        g.regionObjectData[0] = new byte[] {0}; assertEquals(-4, g.tryFinishRegionLoad());
    }
    @Test public void successChangesStageAndPlaneBuildsOnceAndWrites121() {
        Probe g = game(0); g.plane = 3;
        assertEquals(-4, g.tryFinishRegionLoad());
        g.aBoolean1080 = false; assertEquals(0, g.tryFinishRegionLoad());
        assertEquals(2, g.loadingStage); assertEquals(3, ObjectManager.scenePlaneFilter);
        assertEquals(1, g.builds); assertEquals(1, g.stream.currentOffset);
        int key = new ISAACRandomGen(new int[4]).getNextKey();
        assertEquals((121 + key) & 255, g.stream.buffer[0] & 255);
    }
    private static class Fetcher {
        final OnDemandFetcher value = new OnDemandFetcher();
        OnDemandData add(int type, int id, byte[] bytes) throws Exception {
            OnDemandData d = new OnDemandData(); d.dataType = type; d.ID = id;
            if (bytes != null) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                GZIPOutputStream zip = new GZIPOutputStream(out);
                zip.write(bytes); zip.close(); d.buffer = out.toByteArray();
            }
            ((NodeList) field(OnDemandFetcher.class, "aClass19_1358").get(value)).insertHead(d);
            return d;
        }
    }
    @Test public void completionsRouteByFileIdNotPositionOrRegionKey() throws Exception {
        Probe g = game(3); Fetcher f = new Fetcher(); g.onDemandFetcher = f.value;
        g.regionTerrainFileIds = new int[] {10, 11, 12};
        g.regionObjectFileIds = new int[] {20, 21, 22};
        byte[] a = {1}, b = {2}, c = {3};
        OnDemandData da = f.add(3, 12, a); f.add(3, 20, b); OnDemandData dc = f.add(3, 21, c);
        f.add(3, g.regionKeys[0], new byte[] {9});
        f.add(4, 10, new byte[] {8});
        g.processOnDemandQueue();
        assertArrayEquals(new byte[][] {null, null, a}, g.regionTerrainData);
        assertArrayEquals(new byte[][] {b, c, null}, g.regionObjectData);
        assertSame(da.buffer, g.regionTerrainData[2]); assertSame(dc.buffer, g.regionObjectData[1]);
        assertArrayEquals(new int[] {10, 11, 12}, g.regionTerrainFileIds);
        assertArrayEquals(new int[] {20, 21, 22}, g.regionObjectFileIds);
    }
    @Test public void nullCompletionDisablesOnlyMatchingIdAndOtherStagesIgnoreMaps() throws Exception {
        Probe g = game(2); Fetcher f = new Fetcher(); g.onDemandFetcher = f.value;
        g.regionTerrainFileIds = new int[] {10, 11}; g.regionObjectFileIds = new int[] {20, 21};
        f.add(3, 11, null); f.add(3, 20, null); g.processOnDemandQueue();
        assertArrayEquals(new int[] {10, -1}, g.regionTerrainFileIds);
        assertArrayEquals(new int[] {-1, 21}, g.regionObjectFileIds);
        g.loadingStage = 2; f.add(3, 10, new byte[] {1}); f.add(3, 21, null);
        g.processOnDemandQueue();
        assertNull(g.regionTerrainData[0]); assertEquals(21, g.regionObjectFileIds[1]);
    }
}
