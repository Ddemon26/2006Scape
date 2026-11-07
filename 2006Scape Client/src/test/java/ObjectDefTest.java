import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/** Decoding, morphing, model-id bookkeeping and placement behaviour of ObjectDef (original member names). */
public class ObjectDefTest {
    private ObjectDef[] savedCache;
    private Game savedClient;
    private boolean savedLowMem;
    private Animation[] savedAnims;
    private VarBit[] savedVarBits;
    private int[] savedMasks;
    private Object savedModelTable, savedModelFetcher, savedStream, savedStreamIndices;
    private int savedCacheIndex;

    private static Field field(Class<?> type, String name) throws Exception {
        Field f = type.getDeclaredField(name);
        f.setAccessible(true);
        return f;
    }

    private static Object get(Object target, String name) throws Exception {
        return field(target.getClass(), name).get(target);
    }

    private static int getInt(Object target, String name) throws Exception {
        return field(target.getClass(), name).getInt(target);
    }

    private static sun.misc.Unsafe unsafe() throws Exception {
        return (sun.misc.Unsafe) field(sun.misc.Unsafe.class, "theUnsafe").get(null);
    }

    private static ObjectDef newDef() throws Exception {
        Constructor<ObjectDef> constructor = ObjectDef.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        ObjectDef def = constructor.newInstance();
        Method defaults = ObjectDef.class.getDeclaredMethod("setDefaults");
        defaults.setAccessible(true);
        defaults.invoke(def);
        return def;
    }

    private static ObjectDef decode(ObjectDef def, int... bytes) throws Exception {
        byte[] data = new byte[bytes.length];
        for (int n = 0; n < bytes.length; n++) data[n] = (byte) bytes[n];
        Stream stream = new Stream(data);
        Method read = ObjectDef.class.getDeclaredMethod("readValues", Stream.class);
        read.setAccessible(true);
        read.invoke(def, stream);
        assertEquals("decoder consumed exactly the bytes", data.length, stream.currentOffset);
        return def;
    }

    @Before public void setUp() throws Exception {
        savedCache = (ObjectDef[]) field(ObjectDef.class, "cache").get(null);
        savedClient = ObjectDef.clientInstance;
        savedLowMem = ObjectDef.lowMem;
        savedAnims = Animation.anims;
        savedVarBits = VarBit.cache;
        savedMasks = Game.anIntArray1232;
        savedModelTable = field(Model.class, "modelHeaders").get(null);
        savedModelFetcher = field(Model.class, "modelRequester").get(null);
        savedStream = field(ObjectDef.class, "stream").get(null);
        savedStreamIndices = field(ObjectDef.class, "streamIndices").get(null);
        savedCacheIndex = field(ObjectDef.class, "cacheIndex").getInt(null);
        ObjectDef.lowMem = false;
    }

    @After public void tearDown() throws Exception {
        field(ObjectDef.class, "cache").set(null, savedCache);
        ObjectDef.clientInstance = savedClient;
        ObjectDef.lowMem = savedLowMem;
        Animation.anims = savedAnims;
        VarBit.cache = savedVarBits;
        Game.anIntArray1232 = savedMasks;
        field(Model.class, "modelHeaders").set(null, savedModelTable);
        field(Model.class, "modelRequester").set(null, savedModelFetcher);
        field(ObjectDef.class, "stream").set(null, savedStream);
        field(ObjectDef.class, "streamIndices").set(null, savedStreamIndices);
        field(ObjectDef.class, "cacheIndex").setInt(null, savedCacheIndex);
    }

    // ---------------------------------------------------------------- decoding

    @Test public void defaultsAndFlagOpcodes() throws Exception {
        ObjectDef d = newDef();
        assertEquals(1, d.sizeX);
        assertEquals(1, d.sizeY);
        assertTrue(d.blocksMovement);
        assertTrue(d.blocksProjectiles);
        assertEquals(0, d.blockedSidesMask);
        assertEquals(-1, d.animationId);
        assertEquals(16, d.wallDecorationOffset);
        assertEquals(-1, d.varBitId);
        assertEquals(-1, d.settingId);

        ObjectDef onlyMovement = decode(newDef(), 17, 0);
        assertFalse(onlyMovement.blocksMovement);
        assertTrue("opcode 17 leaves the projectile flag alone", onlyMovement.blocksProjectiles);
        ObjectDef onlyProjectile = decode(newDef(), 18, 0);
        assertTrue("opcode 18 leaves the movement flag alone", onlyProjectile.blocksMovement);
        assertFalse(onlyProjectile.blocksProjectiles);
        ObjectDef both = decode(newDef(), 74, 0);
        assertFalse(both.blocksMovement);
        assertFalse(both.blocksProjectiles);
    }

    @Test public void sizeMaskAnimationAndOffsetOpcodes() throws Exception {
        ObjectDef d = decode(newDef(), 14, 3, 15, 2, 69, 11, 24, 0, 7, 28, 20, 0);
        assertEquals(3, d.sizeX);
        assertEquals(2, d.sizeY);
        assertEquals(11, d.blockedSidesMask);
        assertEquals(7, d.animationId);
        assertEquals(20, d.wallDecorationOffset);
        ObjectDef none = decode(newDef(), 24, 255, 255, 0);
        assertEquals("65535 means no animation", -1, none.animationId);
        ObjectDef big = decode(newDef(), 24, 1, 2, 0);
        assertEquals(0x102, big.animationId);
        ObjectDef mask = decode(newDef(), 69, 255, 0);
        assertEquals("mask is an unsigned byte", 255, mask.blockedSidesMask);
        ObjectDef sizes = decode(newDef(), 15, 9, 0);
        assertEquals(1, sizes.sizeX);
        assertEquals(9, sizes.sizeY);
    }

    @Test public void modelListsAndTheirCounts() throws Exception {
        ObjectDef shaped = decode(newDef(), 1, 2, 0x01, 0x02, 10, 0x03, 0x04, 22, 0);
        assertArrayEquals(new int[] {0x102, 0x304}, (int[]) get(shaped, "modelIds"));
        assertArrayEquals(new int[] {10, 22}, (int[]) get(shaped, "modelShapes"));
        ObjectDef empty = decode(newDef(), 1, 0, 0);
        assertNull(get(empty, "modelIds"));
        assertNull(get(empty, "modelShapes"));
        ObjectDef plain = decode(newDef(), 5, 3, 0, 5, 0, 6, 1, 0, 0);
        assertArrayEquals(new int[] {5, 6, 256}, (int[]) get(plain, "modelIds"));
        assertNull(get(plain, "modelShapes"));
        ObjectDef replaced = decode(newDef(), 1, 1, 0, 9, 3, 5, 1, 0, 8, 0);
        assertArrayEquals("second list ignored without low memory", new int[] {9}, (int[]) get(replaced, "modelIds"));
        assertArrayEquals(new int[] {3}, (int[]) get(replaced, "modelShapes"));
        ObjectDef.lowMem = true;
        ObjectDef swapped = decode(newDef(), 1, 1, 0, 9, 3, 5, 1, 0, 8, 0);
        assertArrayEquals("low memory replaces", new int[] {8}, (int[]) get(swapped, "modelIds"));
        assertNull(get(swapped, "modelShapes"));
    }

    @Test public void interactionByteAndDerivedActionFlag() throws Exception {
        assertTrue(decode(newDef(), 19, 1, 0).hasActions);
        ObjectDef explicitNo = decode(newDef(), 1, 1, 0, 9, 10, 19, 0, 0);
        assertFalse("explicit 0 beats the model-based default", explicitNo.hasActions);
        assertTrue(decode(newDef(), 1, 1, 0, 9, 10, 0).hasActions);
        assertFalse(decode(newDef(), 1, 1, 0, 9, 0, 0).hasActions);
        assertTrue(decode(newDef(), 5, 1, 0, 9, 0).hasActions);
        assertFalse(decode(newDef(), 0).hasActions);
        assertTrue(decode(newDef(), 30, 'x', 10, 0).hasActions);
        ObjectDef hidden = decode(newDef(), 30, 'h', 'i', 'd', 'd', 'e', 'n', 10, 0);
        assertNull(hidden.actions[0]);
    }

    @Test public void morphOpcodeReadsVarBitSettingAndChildren() throws Exception {
        ObjectDef d = decode(newDef(), 77, 0, 7, 0, 3, 2, 1, 0, 255, 255, 1, 2, 0);
        assertEquals(7, d.varBitId);
        assertEquals(3, d.settingId);
        assertArrayEquals("count byte is the last index", new int[] {256, -1, 258}, d.childrenIDs);
        ObjectDef none = decode(newDef(), 77, 255, 255, 255, 255, 0, 0, 5, 0);
        assertEquals(-1, none.varBitId);
        assertEquals(-1, none.settingId);
        assertArrayEquals(new int[] {5}, none.childrenIDs);
    }

    @Test public void recolourOpcodeReadsPairsByCount() throws Exception {
        ObjectDef d = decode(newDef(), 40, 2, 0, 1, 0, 2, 0, 3, 0, 4, 0);
        assertArrayEquals(new int[] {1, 3}, (int[]) get(d, "modifiedModelColors"));
        assertArrayEquals(new int[] {2, 4}, (int[]) get(d, "originalModelColors"));
    }

    // ---------------------------------------------------------------- morph resolution

    private void installCache(ObjectDef... defs) throws Exception {
        ObjectDef[] cache = new ObjectDef[20];
        for (int n = 0; n < 20; n++) {
            cache[n] = n < defs.length ? defs[n] : newDef();
            if (n >= defs.length) cache[n].type = -1;
        }
        field(ObjectDef.class, "cache").set(null, cache);
    }

    private static ObjectDef typed(int type) throws Exception {
        ObjectDef d = newDef();
        d.type = type;
        return d;
    }

    @Test public void method580PicksChildByVarBitThenSettingAndRejectsBadValues() throws Exception {
        installCache(typed(500), typed(501), typed(502), typed(503));
        Game game = new Game();
        game.variousSettings = new int[50];
        ObjectDef.clientInstance = game;
        Game.anIntArray1232 = new int[32];
        for (int k = 0, i = 2; k < 32; k++, i += i) Game.anIntArray1232[k] = i - 1;
        VarBit[] bits = new VarBit[10];
        for (int n = 0; n < bits.length; n++) bits[n] = (VarBit) unsafe().allocateInstance(VarBit.class);
        bits[7].anInt648 = 3;
        bits[7].anInt649 = 2;
        bits[7].anInt650 = 4;
        VarBit.cache = bits;

        ObjectDef def = newDef();
        def.childrenIDs = new int[] {500, -1, 502, 503};
        def.varBitId = 7;
        game.variousSettings[3] = 12;
        assertEquals("bits 2..4 of setting 3", 3, game.variousSettings[3] >> 2 & 7);
        assertEquals(503, def.resolveMorph().type);
        game.variousSettings[3] = 0b1000;
        assertEquals(502, def.resolveMorph().type);
        game.variousSettings[3] = 0b0011;
        assertEquals("only bits above the low bit count", 500, def.resolveMorph().type);
        game.variousSettings[3] = 0b0100;
        assertNull("child id -1 resolves to nothing", def.resolveMorph());
        game.variousSettings[3] = 0b10000;
        assertNull("index past the children resolves to nothing", def.resolveMorph());
        game.variousSettings[3] = 0b1100000;
        assertEquals("bits above the high bit are masked off", 500, def.resolveMorph().type);

        ObjectDef bySetting = newDef();
        bySetting.childrenIDs = new int[] {500, -1, 502, 503};
        bySetting.settingId = 9;
        game.variousSettings[9] = 2;
        assertEquals(502, bySetting.resolveMorph().type);
        game.variousSettings[9] = 4;
        assertNull(bySetting.resolveMorph());
        game.variousSettings[9] = -1;
        assertNull("negative setting value", bySetting.resolveMorph());

        ObjectDef both = newDef();
        both.childrenIDs = new int[] {500, 501, 502, 503};
        both.varBitId = 7;
        both.settingId = 9;
        game.variousSettings[3] = 0b1000;
        game.variousSettings[9] = 1;
        assertEquals("varbit wins over the plain setting", 502, both.resolveMorph().type);

        ObjectDef neither = newDef();
        neither.childrenIDs = new int[] {500, 501};
        assertNull("no source selects nothing", neither.resolveMorph());
    }

    // ---------------------------------------------------------------- model ids and availability

    private static class RecordingFetcher extends OnDemandFetcherParent {
        final List<Integer> requested = new ArrayList<Integer>();
        @Override public void requestModel(int i) { requested.add(i); }
    }

    private RecordingFetcher modelTable(int size, int... present) throws Exception {
        RecordingFetcher fetcher = new RecordingFetcher();
        Model.initModelHeaders(size, fetcher);
        ModelHeader[] table = (ModelHeader[]) field(Model.class, "modelHeaders").get(null);
        for (int id : present) table[id] = new ModelHeader();
        return fetcher;
    }

    @Test public void method579ChecksEveryModelIdLowSixteenBits() throws Exception {
        ObjectDef def = newDef();
        assertTrue("no models at all", def.areAllModelsLoaded());
        field(ObjectDef.class, "modelIds").set(def, new int[] {10, 11 | 0x10000, 12});
        RecordingFetcher fetcher = modelTable(30, 10, 11, 12);
        assertTrue(def.areAllModelsLoaded());
        assertTrue(fetcher.requested.isEmpty());
        fetcher = modelTable(30, 10, 12);
        assertFalse(def.areAllModelsLoaded());
        assertEquals(Arrays.asList(11), fetcher.requested);
        fetcher = modelTable(30, 11);
        assertFalse(def.areAllModelsLoaded());
        assertEquals("every missing model is requested", Arrays.asList(10, 12), fetcher.requested);
    }

    @Test public void method577OnlyNeedsTheModelsForTheAskedShape() throws Exception {
        ObjectDef noModels = newDef();
        assertTrue(noModels.areModelsLoaded(10));
        ObjectDef unshaped = newDef();
        field(ObjectDef.class, "modelIds").set(unshaped, new int[] {10, 11});
        RecordingFetcher fetcher = modelTable(30, 10);
        assertTrue("shape other than 10 needs nothing", unshaped.areModelsLoaded(0));
        assertTrue(fetcher.requested.isEmpty());
        assertFalse("shape 10 needs every model", unshaped.areModelsLoaded(10));
        assertEquals(Arrays.asList(11), fetcher.requested);
        modelTable(30, 10, 11);
        assertTrue(unshaped.areModelsLoaded(10));

        ObjectDef shaped = newDef();
        field(ObjectDef.class, "modelIds").set(shaped, new int[] {10, 11 | 0x10000, 12});
        field(ObjectDef.class, "modelShapes").set(shaped, new int[] {0, 4, 10});
        fetcher = modelTable(30, 10, 12);
        assertTrue("shape 0 uses model 10 only", shaped.areModelsLoaded(0));
        assertFalse("shape 4 uses model 11 only", shaped.areModelsLoaded(4));
        assertEquals(Arrays.asList(11), fetcher.requested);
        assertTrue("shape 10 uses model 12 only", shaped.areModelsLoaded(10));
        assertTrue("shape without a model entry is fine", shaped.areModelsLoaded(22));
    }

    @Test public void method574QueuesOnDemandRequestsForEveryModelId() throws Exception {
        OnDemandFetcher fetcher = new OnDemandFetcher();
        Game client = new Game();
        field(Game.class, "cacheStores").setAccessible(true);
        ((Object[]) field(Game.class, "cacheStores").get(client))[0] = unsafe().allocateInstance(CacheStore.class);
        field(OnDemandFetcher.class, "clientInstance").set(fetcher, client);
        field(OnDemandFetcher.class, "highestPendingPriority").setInt(fetcher, 1);
        fetcher.versions[0] = new int[40];
        Arrays.fill(fetcher.versions[0], 1);
        byte[][] status = (byte[][]) field(OnDemandFetcher.class, "fileStatus").get(fetcher);
        status[0] = new byte[40];
        Arrays.fill(status[0], (byte) 1);
        status[0][21] = 0;

        ObjectDef def = newDef();
        def.requestModels(fetcher);
        NodeList queue = (NodeList) field(OnDemandFetcher.class, "extraFileQueue").get(fetcher);
        assertNull("no models: nothing queued", queue.popTail());

        field(ObjectDef.class, "modelIds").set(def, new int[] {20, 21, 22 | 0x10000});
        def.requestModels(fetcher);
        List<Integer> ids = new ArrayList<Integer>();
        for (OnDemandData data = (OnDemandData) queue.popTail(); data != null; data = (OnDemandData) queue.popTail()) {
            assertEquals("models are file type 0", 0, data.dataType);
            ids.add(data.ID);
        }
        assertEquals("id 21 has no status so is skipped, high bit stripped", Arrays.asList(20, 22), ids);
    }

    // ---------------------------------------------------------------- lookup and low-memory flag

    @Test public void forIDReturnsCachedDefinitionsAndLoadsMissesIntoRoundRobinSlots() throws Exception {
        ObjectDef[] cache = new ObjectDef[20];
        for (int n = 0; n < 20; n++) {
            cache[n] = newDef();
            cache[n].type = -1;
        }
        cache[4].type = 77;
        field(ObjectDef.class, "cache").set(null, cache);
        field(ObjectDef.class, "cacheIndex").setInt(null, 18);
        Stream data = new Stream(new byte[] {0, 0, 14, 4, 0, 15, 7, 0});
        field(ObjectDef.class, "stream").set(null, data);
        field(ObjectDef.class, "streamIndices").set(null, new int[] {0, 0, 2, 5});
        assertSame("already cached", cache[4], ObjectDef.forID(77));
        assertEquals(18, field(ObjectDef.class, "cacheIndex").getInt(null));
        ObjectDef two = ObjectDef.forID(2);
        assertSame("slot after the previous index", cache[19], two);
        assertEquals(2, two.type);
        assertEquals(4, two.sizeX);
        assertEquals(1, two.sizeY);
        ObjectDef three = ObjectDef.forID(3);
        assertSame("wraps to slot zero", cache[0], three);
        assertEquals(3, three.type);
        assertEquals(1, three.sizeX);
        assertEquals(7, three.sizeY);
        assertSame("hit after load", three, ObjectDef.forID(3));
    }

    @Test public void method189OnlyChecksLowMemoryGroundDecorationsThatAreShownInLowMemory() throws Exception {
        boolean savedObjectManagerLowMem = ObjectManager.lowMem;
        try {
            RecordingFetcher fetcher = modelTable(300, 1);
            // one object, id 100, shape 22, tile (5,5) of the 64x64 block based at (0,0)
            byte[] data = {101, 1, 22 << 2, 0, 0};
            ObjectDef def = typed(100);
            field(ObjectDef.class, "modelIds").set(def, new int[] {2});
            installCache(def);
            ObjectManager.lowMem = true;
            def.hasActions = false;
            def.showInLowMemory = false;
            assertTrue("low memory, no actions, flag clear: not needed", ObjectManager.method189(5, data, 5));
            assertTrue(fetcher.requested.isEmpty());
            def.showInLowMemory = true;
            assertFalse("flag set: its missing model is needed", ObjectManager.method189(5, data, 5));
            assertEquals(Arrays.asList(2), fetcher.requested);
            def.showInLowMemory = false;
            def.hasActions = true;
            assertFalse("actions: needed", ObjectManager.method189(5, data, 5));
            def.hasActions = false;
            ObjectManager.lowMem = false;
            assertFalse("high memory: always needed", ObjectManager.method189(5, data, 5));
            ObjectManager.lowMem = true;
            byte[] other = {101, 1, 10 << 2, 0, 0};
            assertFalse("other shapes are always needed", ObjectManager.method189(5, other, 5));
        } finally {
            ObjectManager.lowMem = savedObjectManagerLowMem;
        }
    }

    // ---------------------------------------------------------------- placement (ObjectManager.placeObjectWithoutShading)

    private static final int[] WALL_SHAPES = {0, 1, 2, 3};
    private static final int[] OBJECT_SHAPES = {9, 10, 11, 12};

    private static CollisionMap blank() {
        CollisionMap map = new CollisionMap();
        for (int[] column : map.tileFlags) Arrays.fill(column, 0);
        return map;
    }

    private static int[][] snapshot(CollisionMap map) {
        int[][] copy = new int[map.tileFlags.length][];
        for (int x = 0; x < copy.length; x++) copy[x] = map.tileFlags[x].clone();
        return copy;
    }

    private static CollisionMap place(ObjectDef def, int shape, int orientation, int x, int y) throws Exception {
        CollisionMap map = blank();
        WorldController world = new WorldController(new int[4][105][105]);
        ObjectManager.placeObjectWithoutShading(world, orientation, y, shape, 0, map, new int[4][105][105], x, def.type, 0);
        return map;
    }

    private ObjectDef flagged(int type, boolean movement, boolean projectiles) throws Exception {
        ObjectDef d = typed(type);
        d.blocksMovement = movement;
        d.blocksProjectiles = projectiles;
        d.sizeX = 2;
        d.sizeY = 2;
        return d;
    }

    @Test public void collisionIsOnlyAddedWhenTheMovementFlagIsSet() throws Exception {
        int[] shapes = {0, 1, 2, 3, 9, 10, 11, 12};
        for (int shape : shapes) {
            for (int orientation = 0; orientation < 4; orientation++) {
                ObjectDef off = flagged(600, false, true);
                installCache(off);
                assertTrue("shape " + shape, Arrays.deepEquals(snapshot(blank()), snapshot(place(off, shape, orientation, 40, 41))));
                ObjectDef on = flagged(600, true, true);
                installCache(on);
                assertFalse("shape " + shape + " orientation " + orientation,
                        Arrays.deepEquals(snapshot(blank()), snapshot(place(on, shape, orientation, 40, 41))));
            }
        }
    }

    @Test public void groundDecorationBlocksItsTileOnlyWithMovementFlagAndActions() throws Exception {
        for (int mask = 0; mask < 4; mask++) {
            ObjectDef d = flagged(601, (mask & 1) != 0, true);
            d.hasActions = (mask & 2) != 0;
            installCache(d);
            CollisionMap map = place(d, 22, 0, 40, 41);
            assertEquals("movement " + (mask & 1) + " actions " + (mask >> 1), mask == 3 ? 0x200000 : 0, map.tileFlags[40][41]);
            assertEquals(0, map.tileFlags[41][41]);
        }
    }

    @Test public void projectileFlagOnlyAddsProjectileBits() throws Exception {
        int[] shapes = {0, 1, 2, 3, 9, 10, 11, 12};
        for (int shape : shapes) {
            for (int orientation = 0; orientation < 4; orientation++) {
                ObjectDef without = flagged(602, true, false);
                installCache(without);
                int[][] low = snapshot(place(without, shape, orientation, 40, 41));
                ObjectDef with = flagged(602, true, true);
                installCache(with);
                int[][] high = snapshot(place(with, shape, orientation, 40, 41));
                boolean differs = false;
                for (int x = 0; x < 104; x++) for (int y = 0; y < 104; y++) {
                    assertEquals("walking bits identical", low[x][y], low[x][y] & high[x][y]);
                    if (low[x][y] != high[x][y]) differs = true;
                }
                assertTrue("shape " + shape + " orientation " + orientation, differs);
            }
        }
        // without the movement flag nothing is added, whatever the projectile flag
        ObjectDef off = flagged(602, false, true);
        installCache(off);
        assertTrue(Arrays.deepEquals(snapshot(blank()), snapshot(place(off, 10, 0, 40, 41))));
    }

    @Test public void footprintIsSizeXByYSwappedForSidewaysOrientations() throws Exception {
        ObjectDef d = flagged(603, true, false);
        d.sizeX = 3;
        d.sizeY = 2;
        installCache(d);
        for (int orientation = 0; orientation < 4; orientation++) {
            CollisionMap map = place(d, 10, orientation, 20, 30);
            int wide = orientation % 2 == 0 ? 3 : 2;
            int tall = orientation % 2 == 0 ? 2 : 3;
            for (int x = 18; x < 26; x++) for (int y = 28; y < 36; y++) {
                boolean inside = x >= 20 && x < 20 + wide && y >= 30 && y < 30 + tall;
                assertEquals("orientation " + orientation + " tile " + x + "," + y, inside, (map.tileFlags[x][y] & 0x100) != 0);
            }
        }
        d.sizeX = 1;
        d.sizeY = 4;
        CollisionMap map = place(d, 12, 1, 20, 30);
        assertTrue("shape 12 uses the same footprint", (map.tileFlags[23][30] & 0x100) != 0 && (map.tileFlags[20][31] & 0x100) == 0);
    }

    private static Animation animation() throws Exception {
        Animation a = (Animation) unsafe().allocateInstance(Animation.class);
        a.anInt356 = -1;
        a.anInt352 = 1;
        return a;
    }

    private static Animable placedObject(WorldController world, int plane, int x, int y) throws Exception {
        Ground[][][] grounds = (Ground[][][]) field(WorldController.class, "groundArray").get(world);
        Ground g = grounds[plane][x][y];
        return g == null || g.objectCount == 0 ? null : g.obj5Array[0].model;
    }

    @Test public void animationIdOrChildrenMakeTheObjectAnAnimatedOne() throws Exception {
        Animation[] anims = new Animation[6];
        anims[3] = animation();
        Animation.anims = anims;
        ObjectDef d = flagged(604, false, false);
        d.animationId = 3;
        installCache(d);
        WorldController world = new WorldController(new int[4][105][105]);
        ObjectManager.placeObjectWithoutShading(world, 0, 41, 10, 0, blank(), new int[4][105][105], 40, 604, 0);
        Animable placed = placedObject(world, 0, 40, 41);
        assertTrue(placed instanceof Animable_Sub5);
        assertSame("animation table entry at the object's animation id", anims[3], get(placed, "aAnimation_1607"));

        d.animationId = -1;
        world = new WorldController(new int[4][105][105]);
        ObjectManager.placeObjectWithoutShading(world, 0, 41, 10, 0, blank(), new int[4][105][105], 40, 604, 0);
        assertNull("no animation, no children, no models: nothing to place", placedObject(world, 0, 40, 41));

        d.childrenIDs = new int[] {604};
        world = new WorldController(new int[4][105][105]);
        ObjectManager.placeObjectWithoutShading(world, 0, 41, 10, 0, blank(), new int[4][105][105], 40, 604, 0);
        placed = placedObject(world, 0, 40, 41);
        assertTrue(placed instanceof Animable_Sub5);
        assertNull(get(placed, "aAnimation_1607"));
        assertSame("children and morph ids are copied", d.childrenIDs, get(placed, "anIntArray1600"));
    }

    @Test public void animatedObjectCopiesMorphSourcesFromItsDefinition() throws Exception {
        Animation[] anims = new Animation[6];
        anims[3] = animation();
        Animation.anims = anims;
        ObjectDef d = flagged(605, false, false);
        d.animationId = 3;
        d.varBitId = 17;
        d.settingId = 29;
        installCache(d);
        Animable_Sub5 animated = new Animable_Sub5(605, 0, 10, 0, 0, 0, 0, 3, false);
        assertEquals(17, getInt(animated, "anInt1601"));
        assertEquals(29, getInt(animated, "anInt1602"));
    }

    @Test public void objectExtentSwapsWithOrientationInTheWorld() throws Exception {
        Animation[] anims = new Animation[6];
        anims[3] = animation();
        Animation.anims = anims;
        ObjectDef d = flagged(606, false, false);
        d.animationId = 3;
        d.sizeX = 3;
        d.sizeY = 2;
        installCache(d);
        for (int orientation = 0; orientation < 4; orientation++) {
            WorldController world = new WorldController(new int[4][105][105]);
            ObjectManager.placeObjectWithoutShading(world, orientation, 30, 10, 0, blank(), new int[4][105][105], 20, 606, 0);
            Ground[][][] grounds = (Ground[][][]) field(WorldController.class, "groundArray").get(world);
            Object5 object = grounds[0][20][30].obj5Array[0];
            int wide = orientation % 2 == 0 ? 3 : 2;
            int tall = orientation % 2 == 0 ? 2 : 3;
            assertEquals(20, object.minTileX);
            assertEquals(30, object.minTileY);
            assertEquals(20 + wide - 1, object.maxTileX);
            assertEquals(30 + tall - 1, object.maxTileY);
        }
    }

    @Test public void wallDecorationIsPushedOutByTheWallsOffset() throws Exception {
        Animation[] anims = new Animation[6];
        anims[3] = animation();
        Animation.anims = anims;
        ObjectDef decoration = flagged(607, false, false);
        decoration.animationId = 3;
        ObjectDef thin = flagged(608, false, false);
        thin.wallDecorationOffset = 8;
        ObjectDef thick = flagged(609, false, false);
        thick.wallDecorationOffset = 40;
        installCache(decoration, thin, thick);
        int[] wallIds = {-1, 608, 609};
        int[] expected = {16, 8, 40};
        for (int n = 0; n < 3; n++) {
            for (int orientation = 0; orientation < 4; orientation++) {
                WorldController world = new WorldController(new int[4][105][105]);
                if (wallIds[n] != -1) {
                    Ground[][][] grounds = (Ground[][][]) field(WorldController.class, "groundArray").get(world);
                    Ground ground = new Ground(0, 20, 30);
                    ground.obj1 = new Object1();
                    ground.obj1.uid = wallIds[n] << 14;
                    grounds[0][20][30] = ground;
                }
                ObjectManager.placeObjectWithoutShading(world, orientation, 30, 5, 0, blank(), new int[4][105][105], 20, 607, 0);
                Ground[][][] grounds = (Ground[][][]) field(WorldController.class, "groundArray").get(world);
                Object2 placed = grounds[0][20][30].obj2;
                int dx = placed.worldX - (20 * 128 + 64);
                int dy = placed.worldY - (30 * 128 + 64);
                int[] ux = {1, 0, -1, 0};
                int[] uy = {0, -1, 0, 1};
                assertEquals("wall " + wallIds[n] + " orientation " + orientation + " x", expected[n] * Math.abs(ux[orientation]), Math.abs(dx));
                assertEquals("wall " + wallIds[n] + " orientation " + orientation + " y", expected[n] * Math.abs(uy[orientation]), Math.abs(dy));
            }
        }
    }

    @Test public void unpackConfigIndexesRecordsByRunningOffsetAndPreparesTwentyCacheSlots() throws Exception {
        Object savedC = field(ObjectDef.class, "cache").get(null), savedS = field(ObjectDef.class, "stream").get(null), savedI = field(ObjectDef.class, "streamIndices").get(null);
        int total = ObjectDef.totalObjects;
        try {
            // loc.dat = 20 bytes; loc.idx = count 3, record lengths 5, 9, 4 (bzip2 entries)
            String archive = "AAByAAByAAIotWvdAAAUAAA3KLW1RQAACAAAJTFBWSZTWV1DcboAAABgAH//8AAgADFMmJkGRhBMhoGTEkFjS+MrrfO+Z/xNC7kinChILqG43QAxQVkmU1ky4qb4AAAAQABOICAAIYNBmgpgS4u5IpwoSBlxU3wA";
            ObjectDef.unpackConfig(new StreamLoader(java.util.Base64.getDecoder().decode(archive)));
            assertEquals(3, ObjectDef.totalObjects);
            assertArrayEquals(new int[] {2, 7, 16}, (int[]) field(ObjectDef.class, "streamIndices").get(null));
            ObjectDef[] slots = (ObjectDef[]) field(ObjectDef.class, "cache").get(null);
            assertEquals(20, slots.length);
            for (int i = 0; i < 20; i++) { assertNotNull(slots[i]); for (int j = 0; j < i; j++) assertNotSame(slots[i], slots[j]); }
            assertEquals(20, ((Stream) field(ObjectDef.class, "stream").get(null)).buffer.length);
        } finally {
            field(ObjectDef.class, "cache").set(null, savedC); field(ObjectDef.class, "stream").set(null, savedS); field(ObjectDef.class, "streamIndices").set(null, savedI);
            ObjectDef.totalObjects = total;
        }
    }
}
