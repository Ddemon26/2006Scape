import java.io.ByteArrayOutputStream;
import java.lang.reflect.*;
import java.util.*;
import org.junit.*;
import static org.junit.Assert.*;

/** Pass 144: semantic tests for EntityDef.method160 / method164, written against the original identifiers only. */
public class EntityDefTest {
    private static Field f(Class<?> c, String n) throws Exception { Field x = c.getDeclaredField(n); x.setAccessible(true); return x; }
    private static void seti(Object o, String n, int v) throws Exception { f(o.getClass(), n).setInt(o, v); }
    private static void set(Object o, String n, Object v) throws Exception { f(o.getClass(), n).set(o, v); }
    private static void sset(Class<?> c, String n, Object v) throws Exception { f(c, n).set(null, v); }
    private static Object sget(Class<?> c, String n) throws Exception { return f(c, n).get(null); }
    private static EntityDef newDef() throws Exception { Constructor<EntityDef> c = EntityDef.class.getDeclaredConstructor(); c.setAccessible(true); return c.newInstance(); }
    private static int[] xs(Model m) { return Arrays.copyOf(m.vertexX, m.vertexCount); }
    private static int[] ys(Model m) { return Arrays.copyOf(m.vertexY, m.vertexCount); }
    private static int[] zs(Model m) { return Arrays.copyOf(m.vertexZ, m.vertexCount); }
    private static int[] sh(Model m, int n) throws Exception { return (int[]) f(Model.class, "faceShade" + "ABC".charAt(n - 4)).get(m); }

    /** Triangle (X,0,0) (X+10,0,0) (X,10,10) with X=20+id; one face of colour `color`. */
    static byte[] triangle(int id, int color) {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        int x = 20 + id;
        for (int i = 0; i < 3; i++) o.write(7);
        o.write(1);
        o.write(0 + 64); o.write(1 + 64); o.write(1 + 64);
        o.write(color >> 8); o.write(color);
        o.write(x + 64); o.write(10 + 64); o.write(-10 + 64);
        o.write(64); o.write(64); o.write(10 + 64);
        o.write(64); o.write(64); o.write(10 + 64);
        o.write(0); o.write(3); o.write(0); o.write(1);
        o.write(0); o.write(0); o.write(0); o.write(0); o.write(0); o.write(0);
        o.write(0); o.write(3); o.write(0); o.write(3); o.write(0); o.write(3);
        o.write(0); o.write(3);
        return o.toByteArray();
    }
    static final class Spy extends OnDemandFetcherParent {
        final List<Integer> requested = new ArrayList<Integer>();
        @Override public void requestModel(int i) { requested.add(i); }
    }
    private Object savedArr, savedFetcher; private Spy spy;
    @Before public void setUp() throws Exception {
        savedArr = sget(Model.class, "modelHeaders"); savedFetcher = sget(Model.class, "modelRequester");
        spy = new Spy(); Model.initModelHeaders(64, spy); EntityDef.mruNodes.clear();
    }
    @After public void tearDown() throws Exception {
        sset(Model.class, "modelHeaders", savedArr); sset(Model.class, "modelRequester", savedFetcher); EntityDef.mruNodes.clear();
    }

    // ---------------- method160 ----------------
    @Test public void method160IsNullWithoutAModelListAndRequestsEveryMissingModel() throws Exception {
        EntityDef d = newDef();
        assertNull(d.getHeadModel());
        set(d, "headModelIds", new int[]{5, 6, 7});
        Model.loadModelHeader(triangle(6, 40000), 6);
        assertNull(d.getHeadModel());                                  // 5 and 7 absent: nothing built, even though 6 is loaded
        assertEquals(Arrays.asList(5, 7), spy.requested);
    }
    @Test public void method160ReturnsTheSingleModelOrTheListConcatenatedInOrderAndIsNotLit() throws Exception {
        Model.loadModelHeader(triangle(5, 40000), 5); Model.loadModelHeader(triangle(6, 40000), 6);
        EntityDef d = newDef();
        set(d, "headModelIds", new int[]{5});
        Model one = d.getHeadModel();
        assertArrayEquals(xs(Model.getModel(5)), xs(one));
        assertNull(sh(one, 4));                                    // no lighting applied
        set(d, "headModelIds", new int[]{6, 5});
        Model two = d.getHeadModel();
        assertArrayEquals(new int[]{26, 36, 26, 25, 35, 25}, xs(two));
        assertNotSame(two, d.getHeadModel());                         // built fresh each call (not cached)
        EntityDef body = newDef(); set(body, "bodyModelIds", new int[]{5}); set(body, "headModelIds", new int[]{6});
        assertArrayEquals(new int[]{26, 36, 26}, xs(body.getHeadModel()));   // uses the 73 list, not the 94 list
    }
    @Test public void method160RecolorsFacesWhoseColourEqualsTheFirstArrayWithTheSecond() throws Exception {
        Model.loadModelHeader(triangle(5, 40000), 5);
        EntityDef d = newDef(); set(d, "headModelIds", new int[]{5});
        assertEquals(40000, d.getHeadModel().faceColors[0]);
        set(d, "recolorSources", new int[]{123, 40000}); set(d, "recolorReplacements", new int[]{77, 555});
        assertEquals(555, d.getHeadModel().faceColors[0]);        // 40000 -> 555 (76 = source, 70 = replacement)
        set(d, "recolorSources", new int[]{555}); set(d, "recolorReplacements", new int[]{1});
        assertEquals(40000, d.getHeadModel().faceColors[0]);      // reversed roles: no face has colour 555
    }

    // ---------------- method164 ----------------
    @Test public void method164BuildsFromTheNinetyFourListLightsItWithOffsetsAndCachesItUnderType() throws Exception {
        Model.loadModelHeader(triangle(5, 40000), 5); Model.loadModelHeader(triangle(6, 40000), 6);
        for (int[] ab : new int[][]{{0, 0}, {-5, 15}, {20, -30}}) {
            EntityDef.mruNodes.clear();
            EntityDef d = newDef(); d.type = 77L;
            set(d, "bodyModelIds", new int[]{6, 5}); seti(d, "ambientLightOffset", ab[0]); seti(d, "lightDivisorOffset", ab[1]);
            Model m = d.getAnimatedModel(-1, -1, null);
            Model both = new Model(2, new Model[]{Model.getModel(6), Model.getModel(5)});
            both.buildAnimationGroups(); both.applyLighting(64 + ab[0], 850 + ab[1], -30, -50, -30, true);
            assertArrayEquals(Arrays.toString(ab), xs(both), xs(m));
            assertArrayEquals(Arrays.toString(ab), sh(both, 4), sh(m, 4));
            assertSame(Model.animationScratchModel, m);
        }
        Model a0 = Model.getModel(5); a0.applyLighting(64, 850, -30, -50, -30, true);
        Model a1 = Model.getModel(5); a1.applyLighting(84, 850, -30, -50, -30, true);
        Model a2 = Model.getModel(5); a2.applyLighting(64, 1700, -30, -50, -30, true);
        assertFalse(Arrays.equals(sh(a0, 4), sh(a1, 4))); assertFalse(Arrays.equals(sh(a0, 4), sh(a2, 4)));
        // cached under the long id: a second definition with the same id reuses the first build
        EntityDef.mruNodes.clear();
        EntityDef e1 = newDef(); e1.type = 9L; set(e1, "bodyModelIds", new int[]{5});
        EntityDef e2 = newDef(); e2.type = 9L; set(e2, "bodyModelIds", new int[]{6});
        assertArrayEquals(new int[]{25, 35, 25}, xs(e1.getAnimatedModel(-1, -1, null)));
        assertArrayEquals(new int[]{25, 35, 25}, xs(e2.getAnimatedModel(-1, -1, null)));
        EntityDef e3 = newDef(); e3.type = 10L; set(e3, "bodyModelIds", new int[]{6});
        assertArrayEquals(new int[]{26, 36, 26}, xs(e3.getAnimatedModel(-1, -1, null)));
    }
    @Test public void method164ScalesXAndZByAnInt91AndYByAnInt86ThenRecolors() throws Exception {
        Model.loadModelHeader(triangle(5, 40000), 5);
        EntityDef d = newDef(); d.type = 3L; set(d, "bodyModelIds", new int[]{5});
        set(d, "recolorSources", new int[]{40000}); set(d, "recolorReplacements", new int[]{321});
        seti(d, "horizontalScale", 256); seti(d, "verticalScale", 64);
        Model m = d.getAnimatedModel(-1, -1, null);
        assertArrayEquals(new int[]{50, 70, 50}, xs(m));           // x doubled
        assertArrayEquals(new int[]{0, 0, 5}, ys(m));              // y (anInt86) halved
        assertArrayEquals(new int[]{0, 0, 20}, zs(m));             // z doubled (anInt91)
        Model ref = Model.getModel(5); ref.recolor(40000, 321); ref.buildAnimationGroups(); ref.applyLighting(64, 850, -30, -50, -30, true);
        Model unrecoloured = Model.getModel(5); unrecoloured.buildAnimationGroups(); unrecoloured.applyLighting(64, 850, -30, -50, -30, true);
        Model cached = (Model) EntityDef.mruNodes.insertFromCache(3L);
        assertArrayEquals(sh(ref, 6), sh(cached, 6)); assertArrayEquals(sh(ref, 4), sh(cached, 4));
        assertFalse(Arrays.equals(sh(ref, 6), sh(unrecoloured, 6)) && Arrays.equals(sh(ref, 4), sh(unrecoloured, 4)));
        EntityDef.mruNodes.clear();
        EntityDef n = newDef(); n.type = 4L; set(n, "bodyModelIds", new int[]{5}); seti(n, "horizontalScale", 128); seti(n, "verticalScale", 256);
        Model q = n.getAnimatedModel(-1, -1, null);
        assertArrayEquals(new int[]{25, 35, 25}, xs(q)); assertArrayEquals(new int[]{0, 0, 20}, ys(q)); assertArrayEquals(new int[]{0, 0, 10}, zs(q));
    }
    @Test public void method164UnloadedModelIsNullAndRequested() throws Exception {
        EntityDef d = newDef(); d.type = 11L; set(d, "bodyModelIds", new int[]{5, 6});
        Model.loadModelHeader(triangle(6, 40000), 6);
        assertNull(d.getAnimatedModel(-1, -1, null)); assertEquals(Arrays.asList(5), spy.requested);
    }

    // frames: parameter j = second frame (for masked transforms), k = main frame, ai = mask
    private static AnimationFrame frame(AnimationTransformTable t, int dx) throws Exception {
        Constructor<AnimationFrame> c = AnimationFrame.class.getDeclaredConstructor(); c.setAccessible(true); AnimationFrame fr = c.newInstance();
        fr.transformTable = t; fr.transformCount = 2; fr.transformIndices = new int[]{0, 1};
        fr.transformX = new int[]{dx, dx}; fr.transformY = new int[]{0, 0}; fr.transformZ = new int[]{0, 0}; return fr;
    }
    private static int[] animated(EntityDef d, int j, int k, int[] ai) throws Exception {
        Constructor<Model> c = Model.class.getDeclaredConstructor(); c.setAccessible(true); Model m = c.newInstance();
        m.vertexCount = 2; m.faceCount = 0; m.vertexX = new int[]{10, 20}; m.vertexY = new int[]{0, 0}; m.vertexZ = new int[]{0, 0};
        m.vertexGroups = new int[][]{{0}, {1}};
        EntityDef.mruNodes.clear(); EntityDef.mruNodes.removeFromCache(m, d.type);
        return xs(d.getAnimatedModel(j, k, ai));
    }
    @Test public void method164AppliesKAsTheMainFrameAndJOnlyThroughTheMask() throws Exception {
        Object savedFrames = sget(AnimationFrame.class, "loadedFrames");
        try {
            ByteArrayOutputStream o = new ByteArrayOutputStream();
            for (int v : new int[]{2, 1, 1, 1, 0, 1, 1}) o.write(v);
            AnimationTransformTable t = new AnimationTransformTable(new Stream(o.toByteArray()));
            AnimationFrame[] fr = new AnimationFrame[8]; fr[4] = frame(t, 100); fr[6] = frame(t, 9);
            sset(AnimationFrame.class, "loadedFrames", fr);
            EntityDef d = newDef(); d.type = 21L;
            int[] mask = {1, 1000};
            assertArrayEquals(new int[]{110, 120}, animated(d, -1, 4, null));      // k alone: every transform
            assertArrayEquals(new int[]{19, 29}, animated(d, -1, 6, null));
            assertArrayEquals(new int[]{110, 29}, animated(d, 6, 4, mask));        // k main, j supplies the masked group 1
            assertArrayEquals(new int[]{19, 120}, animated(d, 4, 6, mask));        // swapped roles give another result
            assertArrayEquals(new int[]{10, 20}, animated(d, -1, -1, null));      // no frames: untouched
        } finally { sset(AnimationFrame.class, "loadedFrames", savedFrames); EntityDef.mruNodes.clear(); }
    }

    // ---------------- pass 149: morph, decoder counts, cache cursor, unpackConfig (original identifiers only) ----------------
    private static sun.misc.Unsafe unsafe() throws Exception { return (sun.misc.Unsafe) f(sun.misc.Unsafe.class, "theUnsafe").get(null); }

    private static EntityDef decode(int... bytes) throws Exception {
        byte[] data = new byte[bytes.length];
        for (int n = 0; n < bytes.length; n++) data[n] = (byte) bytes[n];
        Stream s = new Stream(data);
        EntityDef d = newDef();
        Method m = EntityDef.class.getDeclaredMethod("readValues", Stream.class);
        m.setAccessible(true);
        m.invoke(d, s);
        assertEquals("decoder consumed exactly the bytes", data.length, s.currentOffset);
        return d;
    }

    private static EntityDef typed(long type) throws Exception { EntityDef d = newDef(); d.type = type; return d; }

    @Test public void method161PicksChildByVarBitThenSettingAndRejectsBadValues() throws Exception {
        Object savedCache = sget(EntityDef.class, "cache"); Game savedClient = EntityDef.clientInstance;
        Object savedMasks = Game.anIntArray1232; VarBit[] savedBits = VarBit.cache;
        try {
            EntityDef[] cache = new EntityDef[20];
            for (int n = 0; n < 20; n++) cache[n] = typed(-1);
            cache[0] = typed(500); cache[1] = typed(501); cache[2] = typed(502); cache[3] = typed(503);
            sset(EntityDef.class, "cache", cache);
            Game game = new Game(); game.variousSettings = new int[50]; EntityDef.clientInstance = game;
            Game.anIntArray1232 = new int[32];
            for (int k = 0, i = 2; k < 32; k++, i += i) Game.anIntArray1232[k] = i - 1;
            VarBit[] bits = new VarBit[10];
            for (int n = 0; n < bits.length; n++) bits[n] = (VarBit) unsafe().allocateInstance(VarBit.class);
            bits[7].varpIndex = 3; bits[7].lowBit = 2; bits[7].highBit = 4;
            VarBit.cache = bits;

            EntityDef def = newDef();
            def.childrenIDs = new int[] {500, -1, 502, 503};
            seti(def, "morphVarBitId", 7);
            game.variousSettings[3] = 12;
            assertEquals(503, def.getMorphedDefinition().type);
            game.variousSettings[3] = 0b1000; assertEquals(502, def.getMorphedDefinition().type);
            game.variousSettings[3] = 0b0011; assertEquals("bits below the low bit are ignored", 500, def.getMorphedDefinition().type);
            game.variousSettings[3] = 0b0100; assertNull("child id -1", def.getMorphedDefinition());
            game.variousSettings[3] = 0b10000; assertNull("index past the children", def.getMorphedDefinition());
            game.variousSettings[3] = 0b1100000; assertEquals("bits above the high bit are masked off", 500, def.getMorphedDefinition().type);

            EntityDef bySetting = newDef();
            bySetting.childrenIDs = new int[] {500, -1, 502, 503};
            seti(bySetting, "morphSettingId", 9);
            game.variousSettings[9] = 2; assertEquals(502, bySetting.getMorphedDefinition().type);
            game.variousSettings[9] = 4; assertNull(bySetting.getMorphedDefinition());
            game.variousSettings[9] = -1; assertNull("negative value", bySetting.getMorphedDefinition());

            EntityDef both = newDef();
            both.childrenIDs = new int[] {500, 501, 502, 503};
            seti(both, "morphVarBitId", 7); seti(both, "morphSettingId", 9);
            game.variousSettings[3] = 0b1000; game.variousSettings[9] = 1;
            assertEquals("varbit wins over the plain setting", 502, both.getMorphedDefinition().type);

            EntityDef neither = newDef(); neither.childrenIDs = new int[] {500, 501};
            assertNull(neither.getMorphedDefinition());
        } finally {
            sset(EntityDef.class, "cache", savedCache); EntityDef.clientInstance = savedClient;
            Game.anIntArray1232 = (int[]) savedMasks; VarBit.cache = savedBits;
        }
    }

    @Test public void readValuesDecodesCountsAndListsOfOpcodes1_40_60_106() throws Exception {
        EntityDef d = decode(1, 3, 0, 5, 0, 6, 0, 7,                    // body models 5,6,7
                60, 2, 1, 0, 2, 0,                                      // head models 256, 512
                40, 2, 0, 10, 0, 20, 0, 11, 0, 21,                      // recolour (10->20), (11->21)
                106, 0xFF, 0xFF, 0x00, 0x09, 2, 0, 1, 0xFF, 0xFF, 0, 3, // varbit none, setting 9, 3 children
                0);
        assertArrayEquals(new int[] {5, 6, 7}, (int[]) f(EntityDef.class, "bodyModelIds").get(d));
        assertArrayEquals(new int[] {256, 512}, (int[]) f(EntityDef.class, "headModelIds").get(d));
        assertArrayEquals(new int[] {10, 11}, (int[]) f(EntityDef.class, "recolorSources").get(d));
        assertArrayEquals(new int[] {20, 21}, (int[]) f(EntityDef.class, "recolorReplacements").get(d));
        assertEquals(-1, f(EntityDef.class, "morphVarBitId").getInt(d));
        assertEquals(9, f(EntityDef.class, "morphSettingId").getInt(d));
        assertArrayEquals(new int[] {1, -1, 3}, d.childrenIDs);
        EntityDef v = decode(106, 0, 12, 0xFF, 0xFF, 0, 0, 7, 0);
        assertEquals(12, f(EntityDef.class, "morphVarBitId").getInt(v));
        assertEquals(-1, f(EntityDef.class, "morphSettingId").getInt(v));
        assertArrayEquals(new int[] {7}, v.childrenIDs);
    }

    @Test public void forIDReturnsCachedDefinitionsAndLoadsMissesIntoRoundRobinSlots() throws Exception {
        Object sc = sget(EntityDef.class, "cache"), ss = sget(EntityDef.class, "stream"), si = sget(EntityDef.class, "streamIndices");
        int cursor = f(EntityDef.class, "cacheIndex").getInt(null);
        try {
            EntityDef[] cache = new EntityDef[20];
            for (int n = 0; n < 20; n++) cache[n] = typed(-1);
            cache[4].type = 77;
            sset(EntityDef.class, "cache", cache);
            f(EntityDef.class, "cacheIndex").setInt(null, 18);
            sset(EntityDef.class, "stream", new Stream(new byte[] {0, 0, 95, 0, 33, 0, 95, 0, 44, 0}));
            sset(EntityDef.class, "streamIndices", new int[] {0, 0, 2, 6});
            assertSame("already cached", cache[4], EntityDef.forID(77));
            assertEquals(18, f(EntityDef.class, "cacheIndex").getInt(null));
            EntityDef two = EntityDef.forID(2);
            assertSame("slot after the previous index", cache[19], two);
            assertEquals(33, two.combatLevel); assertEquals(2, two.type);
            EntityDef three = EntityDef.forID(3);
            assertSame("wraps to slot zero", cache[0], three);
            assertEquals(44, three.combatLevel);
            assertSame("hit after load", three, EntityDef.forID(3));
            assertEquals(0, f(EntityDef.class, "cacheIndex").getInt(null));
        } finally {
            sset(EntityDef.class, "cache", sc); sset(EntityDef.class, "stream", ss); sset(EntityDef.class, "streamIndices", si);
            f(EntityDef.class, "cacheIndex").setInt(null, cursor);
        }
    }

    @Test public void unpackConfigIndexesRecordsByRunningOffsetAndPreparesTwentyCacheSlots() throws Exception {
        Object sc = sget(EntityDef.class, "cache"), ss = sget(EntityDef.class, "stream"), si = sget(EntityDef.class, "streamIndices");
        int total = EntityDef.totalNPCs;
        try {
            // npc.dat = 20 bytes; npc.idx = count 3, record lengths 5, 9, 4 (bzip2 entries, built with the generator in the pass folder)
            String archive = "AAByAAByAAJYwfzcAAAUAAA3WMJGRAAACAAAJTFBWSZTWV1DcboAAABgAH//8AAgADFMmJkGRhBMhoGTEkFjS+MrrfO+Z/xNC7kinChILqG43QAxQVkmU1ky4qb4AAAAQABOICAAIYNBmgpgS4u5IpwoSBlxU3wA";
            EntityDef.unpackConfig(new StreamLoader(Base64.getDecoder().decode(archive)));
            assertEquals(3, EntityDef.totalNPCs);
            assertArrayEquals(new int[] {2, 7, 16}, (int[]) sget(EntityDef.class, "streamIndices")); // 2, then +5, then +9
            EntityDef[] slots = (EntityDef[]) sget(EntityDef.class, "cache");
            assertEquals(20, slots.length);
            for (int i = 0; i < 20; i++) { assertNotNull(slots[i]); assertEquals(-1L, slots[i].type); for (int j = 0; j < i; j++) assertNotSame(slots[i], slots[j]); }
            assertEquals(20, ((Stream) sget(EntityDef.class, "stream")).buffer.length);
        } finally {
            sset(EntityDef.class, "cache", sc); sset(EntityDef.class, "stream", ss); sset(EntityDef.class, "streamIndices", si);
            EntityDef.totalNPCs = total;
        }
    }

    // ---------------- pass 156: remaining EntityDef members, observed through their Game/NPC consumers (original identifiers only) ----------------
    static class TestGame extends Game {
        final List<Object[]> markers = new ArrayList<Object[]>();
        final List<Entity> projected = new ArrayList<Entity>();
        @Override public void drawMinimapMarker(Sprite sprite, int offsetX, int offsetY) { markers.add(new Object[] {sprite, offsetX, offsetY}); }
        @Override public void projectEntityToScreen(Entity entity, int heightOffset) { projected.add(entity); spriteDrawX = 50; spriteDrawY = 60; }
    }
    interface Body { void run() throws Exception; }

    /** Runs body with `def` installed as the cached definition for npc id `id` (EntityDef.forID returns cached slots by type). */
    private static void withDefInCache(EntityDef def, long id, Body body) throws Exception {
        Object savedCache = sget(EntityDef.class, "cache");
        Game savedClient = EntityDef.clientInstance;
        try {
            EntityDef[] cache = new EntityDef[20];
            for (int n = 0; n < 20; n++) cache[n] = typed(-1);
            def.type = id; cache[0] = def;
            sset(EntityDef.class, "cache", cache);
            body.run();
        } finally { sset(EntityDef.class, "cache", savedCache); EntityDef.clientInstance = savedClient; }
    }

    /** Delivers NPC update mask 2 (new definition id) to a fresh NPC in slot 2, which copies the definition fields onto the NPC. */
    private static NPC npcWithDef(Game game, int id) throws Exception {
        NPC npc = new NPC();
        game.npcArray = new NPC[8]; game.maskUpdateIndices = new int[8];
        game.npcArray[2] = npc; game.maskUpdateIndices[0] = 2; game.maskUpdateCount = 1;
        Stream packet = new Stream(new byte[] {2, (byte) (id + 128), (byte) (id >> 8)});
        game.decodeNpcUpdateMasks(packet);
        assertEquals(3, packet.currentOffset);
        return npc;
    }

    @Test public void constructorDefaultsAndOpcodesDecodeSizeAnimationsTurnSpeedHeadIconAndFlags() throws Exception {
        EntityDef fresh = newDef();
        assertEquals(1, fresh.tileSize); assertEquals(32, fresh.turnSpeed);
        assertEquals(-1, fresh.walkAnimationId); assertEquals(-1, fresh.reverseWalkAnimationId); assertEquals(-1, fresh.sidestepNegativeAnimationId);
        assertEquals(-1, fresh.sidestepPositiveAnimationId); assertEquals(-1, fresh.idleAnimationId); assertEquals(-1, fresh.headIconId);
        assertTrue(fresh.interactable); assertTrue(fresh.showsMinimapDot); assertFalse(fresh.placedInFirstPass);
        EntityDef d = decode(12, 0xFD, 13, 0, 50, 14, 0, 51, 17, 0, 60, 0, 61, 0, 62, 0, 63, 93, 99, 102, 0, 9, 103, 0, 17, 107, 0);
        assertEquals(-3, d.tileSize);
        assertEquals(50, d.idleAnimationId);
        assertEquals("opcode 17 rewrites the opcode 14 value, then reads three more", 60, d.walkAnimationId);
        assertEquals(61, d.reverseWalkAnimationId); assertEquals(62, d.sidestepNegativeAnimationId); assertEquals(63, d.sidestepPositiveAnimationId);
        assertEquals(9, d.headIconId); assertEquals(17, d.turnSpeed);
        assertFalse(d.showsMinimapDot); assertTrue(d.placedInFirstPass); assertFalse(d.interactable);
        EntityDef w = decode(14, 0, 51, 0);
        assertEquals(51, w.walkAnimationId); assertEquals(-1, w.reverseWalkAnimationId); assertEquals(-1, w.sidestepNegativeAnimationId); assertEquals(-1, w.sidestepPositiveAnimationId);
        assertEquals(1, decode(0).tileSize);
        assertEquals(3, decode(12, 3, 0).tileSize);
        EntityDef onlyOne = decode(107, 0);
        assertFalse(onlyOne.interactable); assertTrue(onlyOne.showsMinimapDot); assertFalse(onlyOne.placedInFirstPass);
        EntityDef onlyTwo = decode(93, 0);
        assertTrue(onlyTwo.interactable); assertFalse(onlyTwo.showsMinimapDot);
    }

    @Test public void animationFieldsReachTheEntityAndSelectTheMovementAnimationByHeadingAndIdleWhenStill() throws Exception {
        final EntityDef def = typed(0);
        def.idleAnimationId = 105; def.walkAnimationId = 101; def.reverseWalkAnimationId = 102; def.sidestepNegativeAnimationId = 103; def.sidestepPositiveAnimationId = 104; def.turnSpeed = 7; def.tileSize = 1;
        withDefInCache(def, 77, new Body() { @Override public void run() throws Exception {
            Game game = new Game();
            NPC npc = npcWithDef(game, 77);
            assertSame(def, npc.desc);
            assertEquals(105, npc.idleAnimationId); assertEquals(101, npc.walkAnimationId); assertEquals(102, npc.reverseWalkAnimationId);
            assertEquals(103, npc.sidestepNegativeAnimationId); assertEquals(104, npc.sidestepPositiveAnimationId); assertEquals(7, npc.turnSpeed); assertEquals(1, npc.tileSize);
            npc.movementAnimationId = -1; npc.smallXYIndex = 0;
            game.advanceEntityAlongWalkingPath(npc);
            assertEquals("no queued steps: the idle animation", 105, npc.movementAnimationId);
            // facing 0; the walk target is tile (10,10), centre 1344. {startX, startY, expected animation, expected turnDirection}
            int[][] cases = {
                {1284, 1344, 103, 1536},   // target east: relative heading -512
                {1404, 1344, 104, 512},    // target west: relative heading +512
                {1344, 1284, 102, 1024},   // target north: relative heading 1024, away from the facing
                {1344, 1404, 101, 0},      // target south: straight ahead
                {1284, 1404, 101, 1792},   // relative -256: still forward
                {1404, 1404, 101, 256},    // relative +256: still forward
                {1284, 1284, 103, 1280},   // relative -768: the -512 side
                {1404, 1284, 102, 768}};   // relative +768: away from the facing
            for (int[] c : cases) {
                npc.smallXYIndex = 1; npc.smallX[0] = 10; npc.smallY[0] = 10; npc.facingDirection = 0; npc.deferredMovementTicks = 0; npc.movementAnimationId = -1;
                npc.x = c[0]; npc.y = c[1];
                game.advanceEntityAlongWalkingPath(npc);
                assertEquals(Arrays.toString(c) + " turnDirection", c[3], npc.turnDirection);
                assertEquals(Arrays.toString(c) + " animation", c[2], npc.movementAnimationId);
            }
            def.sidestepNegativeAnimationId = -1; def.sidestepPositiveAnimationId = -1; def.reverseWalkAnimationId = -1;
            npcWithDef(game, 77);
            for (int[] c : cases) {
                npc = game.npcArray[2];
                npc.smallXYIndex = 1; npc.smallX[0] = 10; npc.smallY[0] = 10; npc.facingDirection = 0; npc.deferredMovementTicks = 0; npc.movementAnimationId = -1;
                npc.x = c[0]; npc.y = c[1];
                game.advanceEntityAlongWalkingPath(npc);
                assertEquals(Arrays.toString(c) + " with missing side animations falls back to the forward one", 101, npc.movementAnimationId);
            }
        } });
    }

    @Test public void turnSpeedIsTheAngleStepPerTurnUpdateAndZeroDisablesTurning() throws Exception {
        final EntityDef def = typed(0);
        def.idleAnimationId = 105; def.walkAnimationId = 101;
        withDefInCache(def, 77, new Body() { @Override public void run() throws Exception {
            Game game = new Game();
            for (int speed : new int[] {7, 32, 0}) {
                def.turnSpeed = speed;
                NPC npc = npcWithDef(game, 77);
                assertEquals(speed, npc.turnSpeed);
                npc.turnDirection = 1024; npc.facingDirection = 0; npc.interactingEntity = -1; npc.movementAnimationId = npc.idleAnimationId;
                game.updateEntityTurning(npc);
                assertEquals("one update turns by the speed", speed, npc.facingDirection);
                assertEquals(speed == 0 ? 105 : 101, npc.movementAnimationId);   // a turning idle NPC plays the walk animation
                game.updateEntityTurning(npc);
                assertEquals(2 * speed, npc.facingDirection);
            }
            def.turnSpeed = 7;
            NPC npc = npcWithDef(game, 77);
            npc.turnDirection = 5; npc.facingDirection = 0; npc.interactingEntity = -1;
            game.updateEntityTurning(npc);
            assertEquals("less than one step away: snaps to the target", 5, npc.facingDirection);
            npc.turnDirection = 2040; npc.facingDirection = 0;
            game.updateEntityTurning(npc);
            assertEquals("short way round is negative", 2041, npc.facingDirection);
        } });
    }

    @Test public void sizeIsTheFootprintInTilesCentringTheWalkTargetWideningTheSceneFootprintAndEnablingBoundsPicking() throws Exception {
        final EntityDef def = typed(0);
        withDefInCache(def, 77, new Body() { @Override public void run() throws Exception {
            Game game = new Game();
            for (int size = 1; size <= 3; size++) {
                for (int standing = 1; standing <= 3; standing++) {
                    def.tileSize = (byte) size;
                    NPC npc = npcWithDef(game, 77);
                    assertEquals(size, npc.tileSize);
                    npc.smallXYIndex = 1; npc.smallX[0] = 10; npc.smallY[0] = 10; npc.facingDirection = 0; npc.deferredMovementTicks = 0;
                    npc.x = 10 * 128 + standing * 64; npc.y = 10 * 128 + standing * 64;
                    game.advanceEntityAlongWalkingPath(npc);
                    assertEquals("size " + size + " standing as " + standing, size == standing ? 0 : 1, npc.smallXYIndex);
                    if (size == standing) { assertEquals(10 * 128 + size * 64, npc.x); }
                    else { assertTrue(npc.x != 10 * 128 + standing * 64 || npc.y != 10 * 128 + standing * 64); }
                }
            }
            // animated model: bounds picking only for size 1
            Model.loadModelHeader(triangle(5, 40000), 5);
            for (int size = 0; size <= 3; size++) {
                EntityDef.mruNodes.clear();
                EntityDef e = newDef(); e.type = 300L + size; e.tileSize = (byte) size; set(e, "bodyModelIds", new int[] {5});
                Model.animationScratchModel.pickByBounds = false;
                assertEquals("size " + size, size == 1, e.getAnimatedModel(-1, -1, null).pickByBounds);
            }
            // scene footprint: radius (size - 1) * 64 + 60 around the centre of tile (10,10)
            game.npcIndices = new int[] {2}; game.npcCount = 1; game.plane = 0;
            game.byteGroundArray = new byte[4][104][104]; game.intGroundArray = new int[4][105][105];
            for (int[] c : new int[][] {{1, 10, 10}, {3, 9, 11}, {5, 8, 12}}) {
                def.tileSize = (byte) c[0];
                NPC npc = npcWithDef(game, 77);
                npc.x = 1344; npc.y = 1344;
                game.tileCentreEntityCycle = new int[104][104]; game.sceneDrawCycle = 1;
                game.worldController = new WorldController(new int[4][105][105]);
                game.addNpcsToScene(false);
                Ground[][][] tiles = (Ground[][][]) f(WorldController.class, "groundArray").get(game.worldController);
                Object5 o = tiles[0][10][10].obj5Array[0];
                assertEquals("size " + c[0] + " minTileX", c[1], o.minTileX); assertEquals(c[2], o.maxTileX);
                assertEquals(c[1], o.minTileY); assertEquals(c[2], o.maxTileY);
            }
        } });
    }

    @Test public void lateDrawAndClickableFlagsChoosePassAndPickHashWhenPlacingNpcs() throws Exception {
        final EntityDef def = typed(0);
        withDefInCache(def, 77, new Body() { @Override public void run() throws Exception {
            Game game = new Game();
            NPC npc = npcWithDef(game, 77);
            game.npcIndices = new int[] {2}; game.npcCount = 1; game.plane = 0;
            game.byteGroundArray = new byte[4][104][104]; game.intGroundArray = new int[4][105][105];
            npc.x = 1344; npc.y = 1344;
            for (boolean late : new boolean[] {false, true}) {
                for (boolean clickable : new boolean[] {true, false}) {
                    def.placedInFirstPass = late; def.interactable = clickable;
                    Ground[][][] tiles;
                    game.tileCentreEntityCycle = new int[104][104]; game.sceneDrawCycle = 1;
                    game.worldController = new WorldController(new int[4][105][105]);
                    game.addNpcsToScene(late);
                    tiles = (Ground[][][]) f(WorldController.class, "groundArray").get(game.worldController);
                    String label = "late=" + late + " clickable=" + clickable;
                    assertNotNull(label, tiles[0][10][10]);
                    assertEquals(label, 0x20000000 + (2 << 14) + (clickable ? 0 : 0x80000000), tiles[0][10][10].obj5Array[0].uid);
                    game.tileCentreEntityCycle = new int[104][104]; game.sceneDrawCycle = 1;
                    game.worldController = new WorldController(new int[4][105][105]);
                    game.addNpcsToScene(!late);
                    tiles = (Ground[][][]) f(WorldController.class, "groundArray").get(game.worldController);
                    assertNull(label + " other pass", tiles[0][10][10]);
                }
            }
        } });
    }

    @Test public void clickableFlagGatesTheNpcMenuRows() throws Exception {
        Game game = new Game();
        EntityDef d = typed(5); d.name = "Bob"; d.combatLevel = 0; d.actions = new String[] {"Talk-to", null, null, null, "Attack"};
        game.menuActionID = new int[500]; game.menuActionCmd1 = new int[500]; game.menuActionCmd2 = new int[500]; game.menuActionCmd3 = new int[500];
        Player savedMe = Game.myPlayer; Game.myPlayer = new Player();
        try {
        game.menuActionRow = 0; game.itemSelected = 0; game.spellSelected = 0;
        game.buildAtNPCMenu(d, 1, 2, 3);
        assertEquals("Talk-to, Attack, Examine", 3, game.menuActionRow);
        d.interactable = false;
        game.menuActionRow = 0;
        game.buildAtNPCMenu(d, 1, 2, 3);
        assertEquals("not clickable: no rows at all", 0, game.menuActionRow);
        d.showsMinimapDot = false; d.interactable = true;
        game.buildAtNPCMenu(d, 1, 2, 3);
        assertEquals("the minimap flag does not gate the menu", 3, game.menuActionRow);
        } finally { Game.myPlayer = savedMe; }
    }

    private static RSImageProducer producer(int w, int h) throws Exception {
        RSImageProducer p = (RSImageProducer) unsafe().allocateInstance(RSImageProducer.class);
        f(RSImageProducer.class, "pixels").set(p, new int[w * h]);
        f(RSImageProducer.class, "width").setInt(p, w);
        f(RSImageProducer.class, "height").setInt(p, h);
        return p;
    }
    private static int[] saveRaster() {
        return new int[] {DrawingArea.width, DrawingArea.height, DrawingArea.topX, DrawingArea.topY, DrawingArea.bottomX, DrawingArea.bottomY};
    }
    private static void restoreRaster(int[] pixelsHolderIgnored, int[] s, int[] savedPixels) {
        DrawingArea.pixels = savedPixels;
        DrawingArea.width = s[0]; DrawingArea.height = s[1]; DrawingArea.topX = s[2]; DrawingArea.topY = s[3]; DrawingArea.bottomX = s[4]; DrawingArea.bottomY = s[5];
    }

    /** Draws the minimap with NPCs at (10, 20) offset units from the player and returns how many NPC dots were requested. */
    private static int npcDots(TestGame g, NPC... npcs) throws Exception {
        g.npcArray = new NPC[8]; g.npcIndices = new int[npcs.length];
        for (int n = 0; n < npcs.length; n++) { g.npcArray[n + 1] = npcs[n]; g.npcIndices[n] = n + 1; }
        g.npcCount = npcs.length;
        g.markers.clear();
        g.drawMinimap();
        int dots = 0;
        for (Object[] m : g.markers) if (m[0] == g.mapDotNPC) { dots++; assertEquals(10, m[1]); assertEquals(20, m[2]); }
        return dots;
    }

    @Test public void minimapFlagSelectsNpcDotsAndBothFlagsAreReadFromTheMorphedChild() throws Exception {
        int[] savedState = saveRaster(); int[] savedPixels = DrawingArea.pixels; int[] savedOffsets = Texture.lineOffsets; Player savedMe = Game.myPlayer;
        try {
            TestGame g = new TestGame();
            g.minimapIP = producer(172, 156); g.worldViewIP = producer(8, 8);
            g.minimapState = 0; g.compass = new Sprite(1, 1); g.minimapSprite = new Sprite(1, 1); g.mapDotNPC = new Sprite(1, 1);
            g.groundArray = new NodeList[4][104][104]; g.plane = 0; g.playerCount = 0; g.mapFunctionIconCount = 0; g.hintIconType = 0; g.destX = 0;
            g.variousSettings = new int[10];
            Game.myPlayer = new Player(); Game.myPlayer.x = 0; Game.myPlayer.y = 0;
            final EntityDef def = typed(0);
            final TestGame game = g;
            withDefInCache(def, 77, new Body() { @Override public void run() throws Exception {
                NPC npc = new NPC(); npc.desc = def; npc.x = 320; npc.y = 640;
                assertEquals("defaults draw a dot", 1, npcDots(game, npc));
                def.showsMinimapDot = false;
                assertEquals("flag 87 off", 0, npcDots(game, npc));
                def.showsMinimapDot = true; def.interactable = false;
                assertEquals("flag 84 off", 0, npcDots(game, npc));
                def.interactable = true;
                assertEquals(1, npcDots(game, npc));
                assertEquals("two NPCs", 2, npcDots(game, npc, npc));
                // morph: the dot flags come from the child definition the settings select
                EntityDef[] cache = (EntityDef[]) sget(EntityDef.class, "cache");
                cache[1] = typed(500); cache[1].showsMinimapDot = false;
                cache[2] = typed(501);
                def.childrenIDs = new int[] {500, 501};
                seti(def, "morphSettingId", 3);
                EntityDef.clientInstance = game;
                game.variousSettings[3] = 0; assertEquals("child 500 has flag 87 off", 0, npcDots(game, npc));
                game.variousSettings[3] = 1; assertEquals("child 501 default", 1, npcDots(game, npc));
                game.variousSettings[3] = 5; assertEquals("no child: no dot", 0, npcDots(game, npc));
                cache[2].interactable = false;
                game.variousSettings[3] = 1; assertEquals("child 501 flag 84 off", 0, npcDots(game, npc));
            } });
        } finally { restoreRaster(null, savedState, savedPixels); Texture.lineOffsets = savedOffsets; Game.myPlayer = savedMe; }
    }

    @Test public void headIconFieldIndexesTheOverheadIconSpritesWithinTheirBounds() throws Exception {
        int[] savedState = saveRaster(); int[] savedPixels = DrawingArea.pixels; Player savedMe = Game.myPlayer;
        try {
            final TestGame g = new TestGame();
            g.groundArray = new NodeList[4][104][104]; g.plane = 0; g.playerCount = 0; g.hintIconType = 0;
            Player me = new Player(); me.visible = true; me.combatLevel = 3; me.headIcon = -1; me.x = 640; me.y = 640;
            Game.myPlayer = me; g.playerArray = new Player[8]; g.playerIndices = new int[8];
            g.headIcons = new Sprite[4];
            for (int n = 0; n < 4; n++) { g.headIcons[n] = new Sprite(1, 1); g.headIcons[n].pixels[0] = 0xAA0000 + 0x100 * n + 1; }
            final EntityDef def = typed(0);
            withDefInCache(def, 77, new Body() { @Override public void run() throws Exception {
                NPC npc = new NPC(); npc.desc = def; npc.x = 640; npc.y = 640;
                g.npcArray = new NPC[8]; g.npcArray[1] = npc; g.npcIndices = new int[] {1}; g.npcCount = 1;
                for (int icon : new int[] {-1, 0, 1, 3, 4, 19, 20}) {
                    def.headIconId = icon;
                    int[] raster = new int[100 * 100];
                    DrawingArea.bindRaster(100, 100, raster);
                    g.projected.clear();
                    g.updateEntities();
                    boolean drawn = icon >= 0 && icon < 4;
                    assertEquals("icon " + icon, drawn ? 1 : 0, g.projected.size());
                    assertEquals("icon " + icon + " pixel", drawn ? 0xAA0000 + 0x100 * icon + 1 : 0, raster[30 * 100 + 38]);
                }
            } });
        } finally { restoreRaster(null, savedState, savedPixels); Game.myPlayer = savedMe; }
    }
}
