import java.io.ByteArrayOutputStream;
import java.lang.reflect.*;
import java.util.*;
import org.junit.*;
import static org.junit.Assert.*;

/** Pass 128: semantic tests for ItemDef, written against the original identifiers only. */
public class ItemDefTest {
    // ---- reflection helpers ----
    private static Field f(Class<?> c, String n) throws Exception { Field x = c.getDeclaredField(n); x.setAccessible(true); return x; }
    private static Object get(Object o, String n) throws Exception { return f(o.getClass(), n).get(o); }
    private static int geti(Object o, String n) throws Exception { return f(o.getClass(), n).getInt(o); }
    private static void set(Object o, String n, Object v) throws Exception { f(o.getClass(), n).set(o, v); }
    private static void seti(Object o, String n, int v) throws Exception { f(o.getClass(), n).setInt(o, v); }
    private static void sset(Class<?> c, String n, Object v) throws Exception { f(c, n).set(null, v); }
    private static Object sget(Class<?> c, String n) throws Exception { return f(c, n).get(null); }
    private static Object call(Object o, String n, Class<?>[] t, Object... a) throws Exception {
        Method m = ItemDef.class.getDeclaredMethod(n, t); m.setAccessible(true);
        try { return m.invoke(o, a); } catch (InvocationTargetException e) { throw (Exception) e.getCause(); }
    }
    private static ItemDef newDef() throws Exception { Constructor<ItemDef> c = ItemDef.class.getDeclaredConstructor(); c.setAccessible(true); return c.newInstance(); }

    // ---- record builder (item definition stream) ----
    static final class Rec {
        final ByteArrayOutputStream o = new ByteArrayOutputStream();
        Rec b(int v) { o.write(v); return this; }
        Rec s(int v) { o.write(v >> 8); o.write(v); return this; }
        Rec i(int v) { s(v >>> 16); s(v & 0xffff); return this; }
        Rec str(String t) { for (byte x : t.getBytes()) o.write(x); o.write(10); return this; }
        byte[] done() { o.write(0); return o.toByteArray(); }
    }

    // ---- synthetic model data in the cache format read by Model.method460 ----
    /** Triangle (X,0,0) (X+10,0,0) (X,10,10) with X=20+id; one face of colour `color`. */
    static byte[] triangle(int id, int color, boolean reversed) {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        int x = 20 + id;
        for (int i = 0; i < 3; i++) o.write(7);                       // vertex flags
        o.write(1);                                                   // face type 1
        int a = 0, b = reversed ? 2 : 1, c = reversed ? 1 : 2, prev = 0;  // face index deltas (smart)
        o.write(a - prev + 64); prev = a; o.write(b - prev + 64); prev = b; o.write(c - prev + 64);
        o.write(color >> 8); o.write(color);                          // face colour
        o.write(x + 64); o.write(10 + 64); o.write(-10 + 64);             // x deltas
        o.write(64); o.write(64); o.write(10 + 64);                   // y deltas
        o.write(64); o.write(64); o.write(10 + 64);                   // z deltas
        o.write(0); o.write(3); o.write(0); o.write(1);               // vertices=3, faces=1
        o.write(0); o.write(0); o.write(0); o.write(0); o.write(0); o.write(0); // textured, renderTypes, priority, alpha, faceGroups, vertexGroups
        o.write(0); o.write(3); o.write(0); o.write(3); o.write(0); o.write(3); // x/y/z data lengths
        o.write(0); o.write(3);                                       // face index data length
        return o.toByteArray();
    }

    static final class Spy extends OnDemandFetcherParent {
        final List<Integer> requested = new ArrayList<Integer>();
        @Override public void requestModel(int i) { requested.add(i); }
    }

    private Object savedArr, savedFetcher;
    private Object[] sd = new Object[8];
    private Spy spy;

    @Before public void setUp() throws Exception {
        savedArr = sget(Model.class, "modelHeaders"); savedFetcher = sget(Model.class, "modelRequester");
        sd[0] = sget(ItemDef.class, "cache"); sd[1] = sget(ItemDef.class, "cacheIndex"); sd[2] = sget(ItemDef.class, "stream");
        sd[3] = sget(ItemDef.class, "streamIndices"); sd[4] = ItemDef.totalItems;
        spy = new Spy(); Model.method459(64, spy);
        ItemDef.mruNodes1.clear(); ItemDef.mruNodes2.clear();
    }
    @After public void tearDown() throws Exception {
        sset(Model.class, "modelHeaders", savedArr); sset(Model.class, "modelRequester", savedFetcher);
        sset(ItemDef.class, "cache", sd[0]); sset(ItemDef.class, "cacheIndex", sd[1]); sset(ItemDef.class, "stream", sd[2]);
        sset(ItemDef.class, "streamIndices", sd[3]); ItemDef.totalItems = (Integer) sd[4];
        ItemDef.mruNodes1.clear(); ItemDef.mruNodes2.clear();
    }

    private static void load(int id) { Model.method460(triangle(id, 40000, false), id); }
    private static void loadAll(int... ids) { for (int id : ids) load(id); }
    private static int[] sh(Model m, int n) throws Exception { return (int[]) f(Model.class, "anIntArray163" + n).get(m); }
    private static int[] xs(Model m) { return Arrays.copyOf(m.anIntArray1627, m.anInt1626); }
    private static int[] ys(Model m) { return Arrays.copyOf(m.anIntArray1628, m.anInt1626); }
    private static int[] zs(Model m) { return Arrays.copyOf(m.anIntArray1629, m.anInt1626); }
    private static int[] x3(int... ids) { int[] r = new int[ids.length * 3]; for (int i = 0; i < ids.length; i++) { r[i * 3] = 20 + ids[i]; r[i * 3 + 1] = 30 + ids[i]; r[i * 3 + 2] = 20 + ids[i]; } return r; }

    /** Installs records as the item database. Record i is item id i. */
    private static void install(byte[]... records) throws Exception {
        ByteArrayOutputStream data = new ByteArrayOutputStream(); data.write(0); data.write(0);
        int[] idx = new int[records.length];
        for (int i = 0; i < records.length; i++) { idx[i] = data.size(); data.write(records[i]); }
        sset(ItemDef.class, "stream", new Stream(data.toByteArray()));
        sset(ItemDef.class, "streamIndices", idx);
        ItemDef.totalItems = records.length;
        ItemDef[] cache = new ItemDef[10]; for (int i = 0; i < 10; i++) cache[i] = newDef();
        sset(ItemDef.class, "cache", cache); sset(ItemDef.class, "cacheIndex", 0);
    }
    private static byte[] blank() { return new Rec().done(); }
    private static final Class<?>[] I = {int.class};

    /** An item whose male/female/head fields are set directly (bypassing the decoder). */
    private static ItemDef withModels(int m1, int m2, int m3, int f1, int f2, int f3, int mh1, int mh2, int fh1, int fh2) throws Exception {
        ItemDef d = newDef(); call(d, "setDefaults", new Class<?>[0]);
        seti(d, "maleWornModelID1", m1); seti(d, "maleWornModelID2", m2); seti(d, "maleWornModelID3", m3);
        seti(d, "femaleWornModelID1", f1); seti(d, "femaleWornModelID2", f2); seti(d, "femaleWornModelID3", f3);
        seti(d, "maleHeadModelID1", mh1); seti(d, "maleHeadModelID2", mh2); seti(d, "femaleHeadModelID1", fh1); seti(d, "femaleHeadModelID2", fh2);
        return d;
    }

    // ================= decoding (readValues) =================
    @Test public void opcodesFillTheWornHeadScaleLightAndRotationFields() throws Exception {
        ItemDef d = newDef(); call(d, "setDefaults", new Class<?>[0]);
        byte[] rec = new Rec().b(23).s(301).b(0xfb).b(24).s(302).b(25).s(303).b(0x05).b(26).s(304).b(78).s(305).b(79).s(306)
            .b(90).s(307).b(91).s(308).b(92).s(309).b(93).s(310).b(95).s(311)
            .b(110).s(312).b(111).s(313).b(112).s(314).b(113).b(0xfd).b(114).b(0xfd).done();
        call(d, "readValues", new Class<?>[]{Stream.class}, new Stream(rec));
        assertEquals(301, geti(d, "maleWornModelID1")); assertEquals(-5, f(ItemDef.class, "maleWornOffsetY").getByte(d));
        assertEquals(302, geti(d, "maleWornModelID2")); assertEquals(303, geti(d, "femaleWornModelID1")); assertEquals(5, f(ItemDef.class, "femaleWornOffsetY").getByte(d));
        assertEquals(304, geti(d, "femaleWornModelID2")); assertEquals(305, geti(d, "maleWornModelID3")); assertEquals(306, geti(d, "femaleWornModelID3"));
        assertEquals(307, geti(d, "maleHeadModelID1")); assertEquals(308, geti(d, "femaleHeadModelID1")); assertEquals(309, geti(d, "maleHeadModelID2"));
        assertEquals(310, geti(d, "femaleHeadModelID2")); assertEquals(311, geti(d, "modelRotation3"));
        assertEquals(312, geti(d, "modelScaleX")); assertEquals(313, geti(d, "modelScaleY")); assertEquals(314, geti(d, "modelScaleZ"));
        assertEquals(-3, geti(d, "ambientLightOffset")); assertEquals(-15, geti(d, "lightDivisorOffset"));  // signed byte; second one is multiplied by 5
        assertEquals(-5, geti(d, "ambientLightOffset") - 2);
    }

    @Test public void defaultsAreNoModelUnitScaleAndZeroLightOffsets() throws Exception {
        ItemDef d = newDef();
        for (String n : new String[]{"maleWornModelID1", "maleWornModelID2", "maleWornModelID3", "femaleWornModelID1", "femaleWornModelID2", "femaleWornModelID3", "maleHeadModelID1", "maleHeadModelID2", "femaleHeadModelID1", "femaleHeadModelID2"}) seti(d, n, 7);
        seti(d, "modelScaleX", 5); seti(d, "modelScaleY", 5); seti(d, "modelScaleZ", 5); seti(d, "ambientLightOffset", 5); seti(d, "lightDivisorOffset", 5); seti(d, "modelRotation3", 5);
        f(ItemDef.class, "maleWornOffsetY").setByte(d, (byte) 4); f(ItemDef.class, "femaleWornOffsetY").setByte(d, (byte) 4);
        call(d, "setDefaults", new Class<?>[0]);
        for (String n : new String[]{"maleWornModelID1", "maleWornModelID2", "maleWornModelID3", "femaleWornModelID1", "femaleWornModelID2", "femaleWornModelID3", "maleHeadModelID1", "maleHeadModelID2", "femaleHeadModelID1", "femaleHeadModelID2"}) assertEquals(n, -1, geti(d, n));
        assertEquals(128, geti(d, "modelScaleX")); assertEquals(128, geti(d, "modelScaleY")); assertEquals(128, geti(d, "modelScaleZ"));
        assertEquals(0, geti(d, "ambientLightOffset")); assertEquals(0, geti(d, "lightDivisorOffset")); assertEquals(0, geti(d, "modelRotation3"));
        assertEquals(0, f(ItemDef.class, "maleWornOffsetY").getByte(d)); assertEquals(0, f(ItemDef.class, "femaleWornOffsetY").getByte(d));
    }

    // ================= readiness (isWornModelReady worn, isHeadModelReady head) =================
    @Test public void wornReadinessRequestsMaleThenFemaleIdsAndReportsReady() throws Exception {
        ItemDef d = withModels(11, 12, 13, 21, 22, 23, 31, 32, 41, 42);
        assertFalse((Boolean) call(d, "isWornModelReady", I, 0)); assertEquals(Arrays.asList(11, 12, 13), spy.requested); spy.requested.clear();
        assertFalse((Boolean) call(d, "isWornModelReady", I, 1)); assertEquals(Arrays.asList(21, 22, 23), spy.requested); spy.requested.clear();
        loadAll(11, 12, 13);
        assertTrue((Boolean) call(d, "isWornModelReady", I, 0)); assertFalse((Boolean) call(d, "isWornModelReady", I, 1));
        loadAll(21, 22, 23);
        assertTrue((Boolean) call(d, "isWornModelReady", I, 1));
        // any gender other than exactly 1 is the first set
        assertTrue((Boolean) call(d, "isWornModelReady", I, 2)); assertTrue((Boolean) call(d, "isWornModelReady", I, -1));
    }
    @Test public void wornReadinessSkipsUnusedParts() throws Exception {
        ItemDef d = withModels(11, -1, 13, 21, 22, -1, -1, -1, -1, -1);
        assertFalse((Boolean) call(d, "isWornModelReady", I, 0)); assertEquals(Arrays.asList(11, 13), spy.requested); spy.requested.clear();
        assertFalse((Boolean) call(d, "isWornModelReady", I, 1)); assertEquals(Arrays.asList(21, 22), spy.requested); spy.requested.clear();
        d = withModels(-1, 12, 13, -1, 22, 23, -1, -1, -1, -1);
        assertTrue((Boolean) call(d, "isWornModelReady", I, 0)); assertTrue((Boolean) call(d, "isWornModelReady", I, 1)); assertTrue(spy.requested.isEmpty());
    }
    @Test public void headReadinessUsesTwoIdsPerGenderAndIgnoresWornIds() throws Exception {
        ItemDef d = withModels(11, 12, 13, 21, 22, 23, 31, 32, 41, 42);
        assertFalse((Boolean) call(d, "isHeadModelReady", I, 0)); assertEquals(Arrays.asList(31, 32), spy.requested); spy.requested.clear();
        assertFalse((Boolean) call(d, "isHeadModelReady", I, 1)); assertEquals(Arrays.asList(41, 42), spy.requested); spy.requested.clear();
        loadAll(31, 32);
        assertTrue((Boolean) call(d, "isHeadModelReady", I, 0)); assertFalse((Boolean) call(d, "isHeadModelReady", I, 1));
        loadAll(41, 42); assertTrue((Boolean) call(d, "isHeadModelReady", I, 1)); assertTrue((Boolean) call(d, "isHeadModelReady", I, 3));
        d = withModels(11, 12, 13, 21, 22, 23, -1, 32, -1, 42);
        assertTrue((Boolean) call(d, "isHeadModelReady", I, 0)); assertTrue((Boolean) call(d, "isHeadModelReady", I, 1));
        d = withModels(11, 12, 13, 21, 22, 23, 31, -1, 41, -1); spy.requested.clear(); Model.method461(31);
        assertFalse((Boolean) call(d, "isHeadModelReady", I, 0)); assertEquals(Arrays.asList(31), spy.requested);
    }

    // ================= model assembly (getWornModel worn, getHeadModel head) =================
    @Test public void wornModelConcatenatesPartsInOrderForTheChosenGender() throws Exception {
        loadAll(11, 12, 13, 21, 22, 23);
        ItemDef d = withModels(11, 12, 13, 21, 22, 23, -1, -1, -1, -1);
        assertArrayEquals(x3(11, 12, 13), xs((Model) call(d, "getWornModel", I, 0)));
        assertArrayEquals(x3(21, 22, 23), xs((Model) call(d, "getWornModel", I, 1)));
        assertArrayEquals(x3(11, 12, 13), xs((Model) call(d, "getWornModel", I, 5)));
        d = withModels(11, 12, -1, 21, -1, 23, -1, -1, -1, -1);
        assertArrayEquals(x3(11, 12), xs((Model) call(d, "getWornModel", I, 0)));
        assertArrayEquals(x3(21), xs((Model) call(d, "getWornModel", I, 1)));   // tertiary ignored when secondary is absent
        d = withModels(-1, 12, 13, -1, 22, 23, -1, -1, -1, -1);
        assertNull(call(d, "getWornModel", I, 0)); assertNull(call(d, "getWornModel", I, 1));
    }
    @Test public void wornModelAppliesOnlyTheSelectedGendersVerticalOffset() throws Exception {
        loadAll(11, 21);
        ItemDef d = withModels(11, -1, -1, 21, -1, -1, -1, -1, -1, -1);
        f(ItemDef.class, "maleWornOffsetY").setByte(d, (byte) 7); f(ItemDef.class, "femaleWornOffsetY").setByte(d, (byte) -9);
        Model male = (Model) call(d, "getWornModel", I, 0), female = (Model) call(d, "getWornModel", I, 1);
        assertArrayEquals(new int[]{7, 7, 17}, ys(male)); assertArrayEquals(new int[]{-9, -9, 1}, ys(female));
        assertArrayEquals(new int[]{0, 0, 10}, zs(male)); assertArrayEquals(x3(11), xs(male));
        f(ItemDef.class, "maleWornOffsetY").setByte(d, (byte) 0);
        assertArrayEquals(new int[]{0, 0, 10}, ys((Model) call(d, "getWornModel", I, 0)));
        f(ItemDef.class, "femaleWornOffsetY").setByte(d, (byte) 0); f(ItemDef.class, "maleWornOffsetY").setByte(d, (byte) 5);
        assertArrayEquals(new int[]{0, 0, 10}, ys((Model) call(d, "getWornModel", I, 1)));
    }
    @Test public void headModelUsesTwoPartsPerGenderWithoutOffsetOrScale() throws Exception {
        loadAll(31, 32, 41, 42);
        ItemDef d = withModels(-1, -1, -1, -1, -1, -1, 31, 32, 41, 42);
        f(ItemDef.class, "maleWornOffsetY").setByte(d, (byte) 7); seti(d, "modelScaleX", 64);
        assertArrayEquals(x3(31, 32), xs((Model) call(d, "getHeadModel", I, 0)));
        assertArrayEquals(x3(41, 42), xs((Model) call(d, "getHeadModel", I, 1)));
        assertArrayEquals(new int[]{0, 0, 10, 0, 0, 10}, ys((Model) call(d, "getHeadModel", I, 0)));
        d = withModels(-1, -1, -1, -1, -1, -1, 31, -1, -1, 42);
        assertArrayEquals(x3(31), xs((Model) call(d, "getHeadModel", I, 0)));
        assertNull(call(d, "getHeadModel", I, 1));
        d = withModels(11, 12, 13, 21, 22, 23, -1, 32, -1, 42);
        assertNull(call(d, "getHeadModel", I, 0));
    }
    @Test public void headWornAndInventoryBuildersRecolorEveryFace() throws Exception {
        loadAll(11, 12, 21);
        ItemDef d = withModels(11, 12, -1, -1, -1, -1, 11, -1, -1, -1);
        seti(d, "modelID", 21);
        set(d, "modifiedModelColors", new int[]{40000, 5}); set(d, "originalModelColors", new int[]{123, 6});
        assertArrayEquals(new int[]{123, 123}, Arrays.copyOf(((Model) call(d, "getWornModel", I, 0)).anIntArray1640, 2));
        assertEquals(123, ((Model) call(d, "getHeadModel", I, 0)).anIntArray1640[0]);
        Model ref = Model.getModel(21); ref.recolor(40000, 123); ref.applyLighting(64, 768, -50, -10, -50, true);
        Model lit = (Model) call(d, "getLitModel", I, 1);
        assertArrayEquals(sh(ref, 4), sh(lit, 4));
        Model plainRef = Model.getModel(21); plainRef.applyLighting(64, 768, -50, -10, -50, true);
        assertFalse(Arrays.equals(sh(plainRef, 4), sh(lit, 4)));
        assertEquals(123, ((Model) call(d, "getUnlitModel", I, 1)).anIntArray1640[0]);
    }

    // ================= getLitModel / getUnlitModel =================
    private static byte[] stackItem(int model) {
        return new Rec().b(1).s(model).b(11).b(100).s(12).s(2).b(101).s(13).s(5).b(102).s(14).s(0).done();
    }
    private static byte[] plain(int model) { return new Rec().b(1).s(model).done(); }
    private void stackDb() throws Exception {
        byte[][] r = new byte[16][]; for (int i = 0; i < 16; i++) r[i] = blank();
        r[10] = stackItem(11); r[12] = plain(12); r[13] = plain(13); r[14] = plain(14); r[11] = plain(11);
        install(r); loadAll(11, 12, 13, 14);
    }
    @Test public void stackAmountSelectsTheLastQualifyingVariantForBothBuilders() throws Exception {
        stackDb();
        int[][] cases = {{1, 11}, {2, 12}, {4, 12}, {5, 13}, {100000, 13}, {0, 11}, {-5, 11}};
        for (int[] c : cases) {
            assertArrayEquals("201 amount " + c[0], x3(c[1]), xs(ItemDef.forID(10).getLitModel(c[0])));
            assertArrayEquals("202 amount " + c[0], x3(c[1]), xs(ItemDef.forID(10).getUnlitModel(c[0])));
        }
    }
    @Test public void inventoryModelIsCachedScaledPerAxisAndLitWhileTheOtherIsNone() throws Exception {
        byte[][] r = new byte[8][]; for (int i = 0; i < 8; i++) r[i] = blank();
        r[3] = new Rec().b(1).s(21).b(110).s(64).b(111).s(256).b(112).s(32).done();
        install(r); loadAll(21);
        Model a = ItemDef.forID(3).getLitModel(1), b = ItemDef.forID(3).getLitModel(1);
        assertSame(a, b);
        assertArrayEquals(new int[]{20, 25, 20}, xs(a));            // x 41,51,41 * 64 / 128
        assertArrayEquals(new int[]{0, 0, 20}, ys(a));              // y 10 * 256 / 128
        assertArrayEquals(new int[]{0, 0, 2}, zs(a));               // z 10 * 32 / 128
        assertTrue(a.aBoolean1659); assertNotNull(sh(a, 4));
        Model u1 = ItemDef.forID(3).getUnlitModel(1), u2 = ItemDef.forID(3).getUnlitModel(1);
        assertNotSame(u1, u2); assertNotSame(a, u1);
        assertArrayEquals(x3(21), xs(u1)); assertArrayEquals(new int[]{0, 0, 10}, ys(u1)); assertArrayEquals(new int[]{0, 0, 10}, zs(u1));
        assertFalse(u1.aBoolean1659); assertNull(sh(u1, 4));
        // cache is keyed by item id: another item gets its own model
        r[4] = plain(22); install(r); loadAll(21, 22);
        assertNotSame(ItemDef.forID(3).getLitModel(1), ItemDef.forID(4).getLitModel(1));
        // a missing model gives null and caches nothing
        assertNull(ItemDef.forID(6).getLitModel(1)); assertNull(ItemDef.forID(6).getUnlitModel(1));
    }
    @Test public void scaleFieldsMapToOneAxisEachAndOnlyWhenNotAllDefault() throws Exception {
        for (int axis = 0; axis < 3; axis++) {
            byte[][] r = new byte[6][]; for (int i = 0; i < 6; i++) r[i] = blank();
            r[3] = new Rec().b(1).s(21).b(110 + axis).s(64).done();
            install(r); ItemDef.mruNodes2.clear(); loadAll(21);
            Model m = ItemDef.forID(3).getLitModel(1);
            int[] ex = {41, 10, 10}, got = {xs(m)[0], ys(m)[2], zs(m)[2]};
            // opcode 110 -> x, 111 -> y, 112 -> z
            ex[axis] = ex[axis] / 2; assertArrayEquals(ex, got);
        }
    }
    @Test public void lightFieldsAreForwardedAsFirstAndSecondLightingArguments() throws Exception {
        for (int[] ab : new int[][]{{0, 0}, {-5, 15}, {20, 0}, {0, -30}, {-10, 45}}) {
            byte[][] r = new byte[6][]; for (int i = 0; i < 6; i++) r[i] = blank();
            r[3] = new Rec().b(1).s(21).b(113).b(ab[0] & 0xff).b(114).b(ab[1] / 5 & 0xff).done();
            install(r); ItemDef.mruNodes2.clear(); Model.method460(triangle(21, 0x2A7F, false), 21);
            Model m = ItemDef.forID(3).getLitModel(1);
            Model ref = Model.getModel(21); ref.applyLighting(64 + ab[0], 768 + ab[1], -50, -10, -50, true);
            assertArrayEquals(Arrays.toString(ab), sh(ref, 4), sh(m, 4));
            assertArrayEquals(sh(ref, 5), sh(m, 5)); assertArrayEquals(sh(ref, 6), sh(m, 6));
        }
        Model base = Model.getModel(21); base.applyLighting(64, 768, -50, -10, -50, true);
        Model amb = Model.getModel(21); amb.applyLighting(84, 768, -50, -10, -50, true);
        Model div = Model.getModel(21); div.applyLighting(64, 1536, -50, -10, -50, true);
        assertFalse(Arrays.equals(sh(base, 4), sh(amb, 4))); assertFalse(Arrays.equals(sh(base, 4), sh(div, 4)));
    }
    @Test public void firstLightingArgumentIsAdditiveAmbientAndSecondDividesTheDirectionalTerm() throws Exception {
        Model.method460(triangle(21, 0x2A7F, false), 21); Model.method460(triangle(22, 0x2A7F, true), 22);
        int[] amb = new int[2], dim = new int[2], bright = new int[2];
        for (int k = 0; k < 2; k++) {
            Model m0 = Model.getModel(21 + k); m0.applyLighting(64, 768, -50, -10, -50, true);
            Model m1 = Model.getModel(21 + k); m1.applyLighting(66, 768, -50, -10, -50, true);
            Model m2 = Model.getModel(21 + k); m2.applyLighting(64, 1536, -50, -10, -50, true);
            amb[k] = sh(m1, 4)[0] - sh(m0, 4)[0];
            dim[k] = sh(m2, 4)[0] - sh(m0, 4)[0];
            bright[k] = sh(m0, 4)[0];
        }
        // ambient raises both faces by the same amount, whichever way they face (regardless of orientation)
        assertTrue(Math.abs(amb[0] - 2) <= 1); assertTrue(Math.abs(amb[1] - 2) <= 1);
        // dividing the directional term moves each face toward the ambient level: the lit side darkens, the unlit side brightens
        assertTrue(bright[0] != bright[1]);
        int lit = bright[0] > bright[1] ? 0 : 1;
        assertTrue(dim[lit] < 0); assertTrue(dim[1 - lit] > 0);
    }

    // ================= users of 201/202 and the head/worn builders =================
    @Test public void groundItemModelIsTheCachedInventoryModelForItsAmount() throws Exception {
        stackDb();
        Item item = new Item(); item.ID = 10; item.amount = 5;
        assertSame(ItemDef.forID(10).getLitModel(5), item.getRotatedModel());
        assertArrayEquals(x3(13), xs(item.getRotatedModel()));
        item.amount = 1; assertArrayEquals(x3(11), xs(item.getRotatedModel()));
    }
    @Test public void playerHeadModelUsesTheHeadSetOfTheAppearanceGender() throws Exception {
        byte[][] r = new byte[8][]; for (int i = 0; i < 8; i++) r[i] = blank();
        install(r); loadAll(11, 12, 13, 21, 22, 23, 31, 32, 41, 42);
        ItemDef d = withModels(11, 12, 13, 21, 22, 23, 31, 32, 41, 42);
        ItemDef[] cache = (ItemDef[]) sget(ItemDef.class, "cache"); cache[0] = d; seti(d, "id", 3);
        sset(ItemDef.class, "cacheIndex", 5);
        for (int gender = 0; gender < 2; gender++) {
            Player p = new Player(); p.visible = true; seti(p, "gender", gender); p.equipment[0] = 512 + 3;
            assertArrayEquals(gender == 0 ? x3(31, 32) : x3(41, 42), xs(p.buildHeadModel()));
        }
        Player p = new Player(); p.visible = true; seti(p, "gender", 1); p.equipment[0] = 512 + 3;
        Model.method461(41); assertNull(p.buildHeadModel());
    }
    @Test public void playerBodyModelUsesTheWornSetOfTheAppearanceGender() throws Exception {
        byte[][] r = new byte[8][]; for (int i = 0; i < 8; i++) r[i] = blank();
        install(r); loadAll(11, 12, 13, 21, 22, 23);
        ItemDef d = withModels(11, 12, 13, 21, 22, 23, -1, -1, -1, -1);
        ItemDef[] cache = (ItemDef[]) sget(ItemDef.class, "cache"); cache[0] = d; seti(d, "id", 3);
        for (int gender = 0; gender < 2; gender++) {
            Player.mruNodes.clear();
            Player p = new Player(); p.visible = true; seti(p, "gender", gender); p.equipment[4] = 512 + 3;
            f(Player.class, "appearanceKey").setLong(p, 5000L + gender);
            Method m = Player.class.getDeclaredMethod("buildBodyModel"); m.setAccessible(true);
            Model out = (Model) m.invoke(p);
            assertArrayEquals(gender == 0 ? x3(11, 12, 13) : x3(21, 22, 23), xs(out));
        }
        Player.mruNodes.clear();
    }

    // ================= misc routines =================
    @Test public void forIdReloadsFromTheRecordAndNoteBorrowsLooksFromTheTemplate() throws Exception {
        byte[][] r = new byte[8][]; for (int i = 0; i < 8; i++) r[i] = blank();
        r[1] = new Rec().b(1).s(77).b(2).str("Apple").b(12).i(50).done();
        r[2] = new Rec().b(1).s(78).b(2).str("Note").b(97).s(1).b(98).s(5).done();
        r[5] = new Rec().b(1).s(79).b(4).s(900).b(5).s(11).b(6).s(12).b(7).s(0xfff0).b(8).s(3).b(95).s(13).done();
        install(r);
        ItemDef note = ItemDef.forID(2);
        assertEquals("Apple", note.name); assertEquals(50, note.value); assertTrue(note.stackable);
        assertEquals("Swap this note at any bank for an Apple.", new String(note.description));
        assertEquals(79, geti(note, "modelID")); assertEquals(900, note.modelZoom); assertEquals(11, note.modelRotation1); assertEquals(12, note.modelRotation2);
        assertEquals(13, geti(note, "modelRotation3")); assertEquals(-16, geti(note, "modelOffset1")); assertEquals(3, geti(note, "modelOffset2"));
        ItemDef apple = ItemDef.forID(1);
        assertEquals("Apple", apple.name); assertEquals(77, geti(apple, "modelID")); assertNull(apple.description); assertFalse(apple.stackable);
    }
    @Test public void noteDescriptionUsesAnForVowelInitialsAndCaseSensitive() throws Exception {
        byte[][] r = new byte[8][]; for (int i = 0; i < 8; i++) r[i] = blank();
        String[] names = {"Egg", "Onion", "Umbrella", "Item", "apple", "Boot"};
        String[] exp = {"an", "an", "an", "an", "a", "a"};
        for (int k = 0; k < names.length; k++) {
            r[1] = new Rec().b(2).str(names[k]).done();
            r[2] = new Rec().b(97).s(1).b(98).s(5).done(); r[5] = blank();
            install(r);
            assertEquals("Swap this note at any bank for " + exp[k] + " " + names[k] + ".", new String(ItemDef.forID(2).description));
        }
    }
    @Test public void forIdCyclesTenCacheSlotsAndMemoises() throws Exception {
        byte[][] r = new byte[14][]; for (int i = 0; i < 14; i++) r[i] = new Rec().b(2).str("n" + i).done();
        install(r);
        ItemDef a = ItemDef.forID(1); assertSame(a, ItemDef.forID(1)); assertEquals("n1", a.name); assertEquals(1, a.id);
        for (int i = 2; i < 11; i++) ItemDef.forID(i);
        assertSame(a, ItemDef.forID(1));
        ItemDef.forID(11);                                   // evicts the oldest slot, which is item 1
        assertNotSame(a, ItemDef.forID(1)); assertEquals(1, ItemDef.forID(1).id);
    }

    // ================= getSprite =================
    static final int PAL = 0x00aa55;
    private int[] savedPalette;
    private void paletteOn() { savedPalette = Texture.hslToRgbPalette.clone(); Arrays.fill(Texture.hslToRgbPalette, PAL); }
    private void paletteOff() { if (savedPalette != null) System.arraycopy(savedPalette, 0, Texture.hslToRgbPalette, 0, savedPalette.length); }
    private static void loadRev(int... ids) { for (int id : ids) Model.method460(triangle(id, 0x2A7F, true), id); }
    private static Rec spriteRec(int model) { return spriteRec(model, 0); }
    private static Rec spriteRec(int model, int shift) {
        int off = 4 * (20 + model) + 20 + shift;
        return new Rec().b(1).s(model).b(4).s(1000).b(5).s(0).b(6).s(0).b(110).s(512).b(111).s(512).b(112).s(512).b(7).s(0x10000 - off).b(8).s(0x10000 - 20);
    }
    private static Rec unscaled(int model) { return new Rec().b(1).s(model).b(4).s(300).b(7).s(0x10000 - (20 + model + 5)).b(8).s(0x10000 - 5); }
    private static int count(int[] px, int v) { int n = 0; for (int p : px) if (p == v) n++; return n; }
    private static int at(int[] px, int x, int y) { return (x < 0 || y < 0 || x > 31 || y > 31) ? 0 : px[x + y * 32]; }
    private static boolean near(int[] px, int x, int y, int v) { return at(px, x - 1, y) == v || at(px, x + 1, y) == v || at(px, x, y - 1) == v || at(px, x, y + 1) == v; }
    private static byte[][] blanks(int n) { byte[][] r = new byte[n][]; for (int i = 0; i < n; i++) r[i] = blank(); return r; }

    @Test public void spriteRestoresTheRasterAndTextureStateItBorrows() throws Exception {
        paletteOn();
        try {
            byte[][] r = blanks(4); r[1] = spriteRec(21).done(); install(r); loadRev(21);
            int[] pix = new int[40 * 50]; int[] lines = {9, 8, 7};
            DrawingArea.bindRaster(50, 40, pix); DrawingArea.setClip(44, 3, 33, 5);
            int a1 = Texture.textureInt1, a2 = Texture.textureInt2; Texture.textureInt1 = 111; Texture.textureInt2 = 222; Texture.lineOffsets = lines;
            Sprite s = ItemDef.getSprite(1, 1, 0);
            assertNotNull(s); assertTrue(count(s.pixels, PAL) > 20);
            assertSame(pix, DrawingArea.pixels); assertEquals(40, DrawingArea.width); assertEquals(50, DrawingArea.height);
            assertEquals(3, DrawingArea.topX); assertEquals(33, DrawingArea.bottomX); assertEquals(5, DrawingArea.topY); assertEquals(44, DrawingArea.bottomY);
            assertEquals(111, Texture.textureInt1); assertEquals(222, Texture.textureInt2); assertSame(lines, Texture.lineOffsets);
            assertEquals(0, count(pix, PAL)); assertTrue(Texture.blockShading);
            Texture.textureInt1 = a1; Texture.textureInt2 = a2;
        } finally { paletteOff(); }
    }
    @Test public void spriteIsThirtyTwoSquareWithQuantityAndStackableMarkers() throws Exception {
        paletteOn();
        try {
            byte[][] r = blanks(16);
            r[10] = spriteRec(21).b(100).s(12).s(2).b(101).s(13).s(5).done();
            r[3] = spriteRec(21).b(11).done();
            r[4] = spriteRec(22).done();
            r[12] = spriteRec(12, 6).done(); r[13] = spriteRec(13, -6).done();
            install(r); loadRev(21, 22, 12, 13);
            Sprite plain = ItemDef.getSprite(4, 7, 5);
            assertEquals(1024, plain.pixels.length); assertEquals(32, plain.trimWidth); assertEquals(-1, plain.trimHeight);   // no stack ids: quantity forgotten
            Sprite stackable = ItemDef.getSprite(3, 7, 5);
            assertEquals(33, stackable.trimWidth); assertEquals(-1, stackable.trimHeight);
            Sprite viaStack = ItemDef.getSprite(10, 7, 5);
            assertEquals(32, viaStack.trimWidth); assertEquals(7, viaStack.trimHeight);
            // quantity picks the same variant as the model builders do
            assertArrayEquals(ItemDef.getSprite(13, 1, 5).pixels, viaStack.pixels);
            assertArrayEquals(ItemDef.getSprite(12, 1, 5).pixels, ItemDef.getSprite(10, 3, 5).pixels);
            assertArrayEquals(ItemDef.getSprite(3, 1, 5).pixels, ItemDef.getSprite(10, 1, 5).pixels);
            assertFalse(Arrays.equals(ItemDef.getSprite(10, 1, 5).pixels, viaStack.pixels));
            // quantity of one or less never selects a variant
            assertEquals(1, ItemDef.getSprite(10, 1, 5).trimHeight); assertEquals(0, ItemDef.getSprite(10, 0, 5).trimHeight);
        } finally { paletteOff(); }
    }
    @Test public void spriteCacheOnlyHoldsOutlineZeroAndRefreshesWhenTheQuantityChanges() throws Exception {
        paletteOn();
        try {
            byte[][] r = blanks(16);
            r[10] = new Rec().b(1).s(21).b(100).s(12).s(2).b(101).s(13).s(5).done();
            r[4] = spriteRec(22).done(); r[12] = spriteRec(12, 6).done(); r[13] = spriteRec(13, -6).done();
            install(r); loadRev(21, 22, 12, 13);
            Sprite a = ItemDef.getSprite(4, 1, 0); assertSame(a, ItemDef.getSprite(4, 1, 0)); assertSame(a, ItemDef.getSprite(4, 99, 0));
            assertNotSame(ItemDef.getSprite(4, 1, 7), ItemDef.getSprite(4, 1, 7)); assertNotSame(ItemDef.getSprite(4, 1, -1), ItemDef.getSprite(4, 1, -1));
            assertSame(a, ItemDef.getSprite(4, 1, 0));
            Sprite one = ItemDef.getSprite(10, 1, 0); assertSame(one, ItemDef.getSprite(10, 1, 0));
            Sprite five = ItemDef.getSprite(10, 5, 0); assertNotSame(one, five); assertEquals(5, five.trimHeight);
            assertSame(five, ItemDef.getSprite(10, 5, 0)); assertNotSame(five, ItemDef.getSprite(10, 1, 0));
            // only the item id keys the cache
            assertNotSame(ItemDef.getSprite(4, 1, 0), ItemDef.getSprite(12, 1, 0));
            // model not loaded: nothing drawn, nothing cached
            Model.method461(22); ItemDef.mruNodes1.clear(); ItemDef.mruNodes2.clear(); assertNull(ItemDef.getSprite(4, 1, 0));
        } finally { paletteOff(); }
    }
    @Test public void spriteOutlineParameterSelectsColouredRingShadowOrNothingAndTheZoomRules() throws Exception {
        paletteOn();
        try {
            byte[][] r = blanks(8); r[4] = spriteRec(22).done();
            install(r); loadRev(22);
            int[] none = ItemDef.getSprite(4, 1, -1).pixels, shadow = ItemDef.getSprite(4, 1, 0).pixels, ring = ItemDef.getSprite(4, 1, 0x123456).pixels;
            // every sprite is a body (palette colour) with a one pixel marker ring (value 1) at its four-neighbour border
            for (int[] px : new int[][]{none, shadow, ring}) {
                assertTrue(count(px, PAL) > 8);
                for (int y = 0; y < 32; y++) for (int x = 0; x < 32; x++) {
                    int v = at(px, x, y);
                    if (v == 1) assertTrue(near(px, x, y, PAL));
                    if (v != PAL && v != 1 && v != 0x302020 && v != 0x123456) assertEquals(0, v);
                    if (near(px, x, y, PAL) && v != PAL) assertEquals(1, v);
                }
            }
            assertEquals(0, count(none, 0x302020)); assertEquals(0, count(none, 0x123456));
            assertEquals(0, count(shadow, 0x123456)); assertEquals(0, count(ring, 0x302020));
            assertTrue(count(ring, 0x123456) > 0); assertTrue(count(shadow, 0x302020) > 0);
            // k > 0: a second ring of colour k around the marker ring, nothing else
            for (int y = 0; y < 32; y++) for (int x = 0; x < 32; x++) {
                int v = at(ring, x, y);
                assertEquals("(" + x + "," + y + ")", v != PAL && v != 1 && near(ring, x, y, 1), v == 0x123456);
            }
            // k == 0: dark shadow where the pixel was empty and its top-left neighbour was drawn (marker or body)
            for (int y = 0; y < 32; y++) for (int x = 0; x < 32; x++) {
                int pre = at(shadow, x, y) == 0x302020 ? 0 : at(shadow, x, y);
                boolean expect = false;
                if (x > 0 && y > 0 && pre == 0) { int nb = at(shadow, x - 1, y - 1); expect = (nb == 0x302020 ? 0 : nb) > 0; }
                assertEquals("(" + x + "," + y + ")", expect, at(shadow, x, y) == 0x302020);
            }
            // a larger zoom value draws the model smaller: x1.5 for -1, x1.04 for k > 0
            assertTrue(count(none, PAL) < count(shadow, PAL)); assertTrue(count(ring, PAL) <= count(shadow, PAL));
        } finally { paletteOff(); }
    }
    @Test public void spriteForANoteDrawsTheBaseItemIconOverTheTemplateIcon() throws Exception {
        paletteOn();
        try {
            byte[][] r = blanks(16);
            r[1] = spriteRec(21).b(2).str("Gem").done();             // the real item
            r[5] = unscaled(22).done();                               // the note template (a note copies its look but not the scale)
            r[6] = unscaled(22).done();                               // identical stand-alone copy of the template
            r[2] = new Rec().b(2).str("Gem note").b(97).s(1).b(98).s(5).done();
            install(r); loadRev(21, 22);
            Sprite note = ItemDef.getSprite(2, 1, 0);
            Sprite base = ItemDef.getSprite(1, 10, -1), templ = ItemDef.getSprite(6, 1, 0);
            assertNotNull(note);
            for (int i = 0; i < 1024; i++) assertEquals("pixel " + i, base.pixels[i] != 0 ? base.pixels[i] : templ.pixels[i], note.pixels[i]);
            assertTrue(count(base.pixels, PAL) > 0); assertFalse(Arrays.equals(base.pixels, templ.pixels));
            assertEquals(33, note.trimWidth); assertEquals(-1, note.trimHeight);
            assertEquals(-1, base.trimHeight); assertEquals(32, base.trimWidth);   // the base sprite keeps its own trim fields
            Model.method461(21); ItemDef.mruNodes1.clear(); ItemDef.mruNodes2.clear(); assertNull(ItemDef.getSprite(2, 1, 0));
        } finally { paletteOff(); }
    }

    // ================= third rotation and unpackConfig =================
    private static int[] arr(String n) throws Exception { Field x = Model.class.getDeclaredField(n); x.setAccessible(true); return (int[]) x.get(null); }
    /** Projected x, y and depth of the three vertices of the last model drawn by getSprite. */
    private int[][] project(int rot1, int rot2, int roll) throws Exception {
        byte[][] r = blanks(4); r[1] = new Rec().b(1).s(21).b(4).s(300).b(5).s(rot1).b(6).s(rot2).b(95).s(roll).done();
        install(r); ItemDef.mruNodes1.clear(); ItemDef.mruNodes2.clear(); loadRev(21);
        assertNotNull(ItemDef.getSprite(1, 1, 5));
        return new int[][]{Arrays.copyOf(arr("anIntArray1665"), 3), Arrays.copyOf(arr("anIntArray1666"), 3), Arrays.copyOf(arr("anIntArray1667"), 3)};
    }
    @Test public void thirdRotationTurnsTheIconInThePlaneOfTheScreenWithoutChangingDepth() throws Exception {
        int[][] base = project(0, 0, 0);
        int[][] half = project(0, 0, 1024), quarter = project(0, 0, 512);
        assertTrue(base[0][1] - base[0][0] > 3);                       // the icon has real extent
        for (int v = 0; v < 3; v++) {
            assertEquals(32 - base[0][v], half[0][v], 3); assertEquals(32 - base[1][v], half[1][v], 3);
            int dx = base[0][v] - 16, dy = base[1][v] - 16;
            assertEquals(Math.abs(dy), Math.abs(quarter[0][v] - 16), 3); assertEquals(Math.abs(dx), Math.abs(quarter[1][v] - 16), 3);
            assertEquals(base[2][v], half[2][v]); assertEquals(base[2][v], quarter[2][v]);
        }
        assertFalse(Arrays.equals(base[2], project(256, 0, 0)[2]));   // the first and second rotations do change depth
        assertFalse(Arrays.equals(base[2], project(0, 256, 0)[2]));
        int[][] again = project(0, 0, 0); assertArrayEquals(base[0], again[0]);
    }
    @Test public void unpackConfigIndexesRecordsByRunningOffsetAndPreparesTenCacheSlots() throws Exception {
        String archive = "AABxAABxAAKcmiw2AAATAAA2nJp1ngAACAAAJTFBWSZTWbACUvQAAAFHgHCQAACAEAIBkAAgACGhoGggGmmguJcxDLbCS6vF3JFOFCQsAJS9ADFBWSZTWbc9AokAAADAAGigIAAhg0GaAuBLi7kinChIW56BRIA=";
        ItemDef.unpackConfig(new StreamLoader(Base64.getDecoder().decode(archive)));
        assertEquals(3, ItemDef.totalItems);
        assertArrayEquals(new int[]{2, 9, 18}, (int[]) sget(ItemDef.class, "streamIndices"));    // 2, then +7, then +9
        ItemDef[] slots = (ItemDef[]) sget(ItemDef.class, "cache");
        assertEquals(10, slots.length);
        for (int i = 0; i < 10; i++) { assertNotNull(slots[i]); assertEquals(-1, slots[i].id); for (int j = 0; j < i; j++) assertNotSame(slots[i], slots[j]); }
        assertEquals("One", ItemDef.forID(1).name); assertEquals(7, geti(ItemDef.forID(1), "modelID")); assertEquals("Zero", ItemDef.forID(0).name);
    }

    // ================= pass 134: appearance hash, gender, and the interface media model loader =================
    private static byte[] appearanceStream(int gender, int[] equip, int[] colours) {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        o.write(gender); o.write(0); o.write(0);
        for (int j = 0; j < 12; j++) { if (equip[j] == 0) o.write(0); else { o.write(equip[j] >> 8); o.write(equip[j] & 255); } }
        for (int c : colours) o.write(c);
        for (int k = 0; k < 7; k++) { o.write(255); o.write(255); }
        for (int k = 0; k < 8; k++) o.write(0);
        o.write(3); o.write(0); o.write(0);
        return o.toByteArray();
    }
    @Test public void updatePlayerStoresGenderAndPacksEquipmentColoursAndGenderIntoTheLongKey() throws Exception {
        int[] colours = {1, 2, 3, 1, 2};
        long packed = 0; for (int c : colours) packed = (packed << 3) + c;
        for (int gender = 0; gender < 2; gender++) {
            Player p = new Player(); int[] eq = new int[12];
            p.updatePlayer(new Stream(appearanceStream(gender, eq, colours)));
            assertEquals(gender, geti(p, "gender"));
            assertEquals((packed << 1) + gender, f(Player.class, "appearanceKey").getLong(p));
            eq[11] = 256 + 3;   // a kit part in the last slot lands just above the colour bits
            p.updatePlayer(new Stream(appearanceStream(gender, eq, colours)));
            assertEquals((((3L << 15) + packed) << 1) + gender, f(Player.class, "appearanceKey").getLong(p));
        }
    }
    @Test public void bodyModelIsLookedUpInTheCacheUnderTheLongKey() throws Exception {
        install(blanks(8)); loadAll(11);
        Model cached = Model.getModel(11);
        Player.mruNodes.clear();
        Player p = new Player(); p.visible = true;
        f(Player.class, "appearanceKey").setLong(p, 777L);
        Player.mruNodes.removeFromCache(cached, 777L);
        Method m = Player.class.getDeclaredMethod("buildBodyModel"); m.setAccessible(true);
        assertArrayEquals(x3(11), xs((Model) m.invoke(p)));   // the cached model, copied into the animation scratch model
        f(Player.class, "appearanceKey").setLong(p, 778L);   // a different key must not hit the cached model
        assertFalse(Arrays.equals(x3(11), xs((Model) m.invoke(p))));
        Player.mruNodes.clear();
    }
    private static Model media(RSInterface ri, int type, int id) throws Exception {
        Method m = RSInterface.class.getDeclaredMethod("loadMediaModel", int.class, int.class); m.setAccessible(true);
        return (Model) m.invoke(ri, type, id);
    }
    @Test public void interfaceMediaLoaderPicksTheSourceByTypeAndCachesUnderTypeAndId() throws Exception {
        install(blanks(8)); loadAll(11, 12, 31, 32, 41, 42);
        LruCache cache = (LruCache) sget(RSInterface.class, "aMRUNodes_264"); cache.clear();
        RSInterface ri = new RSInterface();
        Model one = media(ri, 1, 11);                    // type 1: model file by id
        assertArrayEquals(x3(11), xs(one));
        assertSame(one, media(ri, 1, 11));               // cached under (type << 16) + id
        assertArrayEquals(x3(12), xs(media(ri, 1, 12)));
        assertNull(media(ri, 5, 11));                    // type 5: nothing
        Model sentinel = Model.getModel(12);
        cache.removeFromCache(sentinel, (2 << 16) + 7);  // type 2 id 7 would need an NPC definition; the cache answers first
        assertSame(sentinel, media(ri, 2, 7));
        assertNotSame(sentinel, media(ri, 1, 7));        // same id, other type: different key
        assertNotSame(sentinel, media(ri, 7, 2));        // swapped type/id: different key
        ItemDef[] c = (ItemDef[]) sget(ItemDef.class, "cache");
        ItemDef d = withModels(11, 12, 13, 21, 22, 23, 31, 32, 41, 42);
        c[0] = d; seti(d, "id", 3); sset(ItemDef.class, "cacheIndex", 5);
        Player me = new Player(); me.visible = true; seti(me, "gender", 1); me.equipment[0] = 512 + 3;
        Player saved = Game.myPlayer;
        try {
            Game.myPlayer = me;                          // type 3: the local player's head model
            assertArrayEquals(x3(41, 42), xs(media(ri, 3, 0)));
        } finally { Game.myPlayer = saved; cache.clear(); }
    }
}
