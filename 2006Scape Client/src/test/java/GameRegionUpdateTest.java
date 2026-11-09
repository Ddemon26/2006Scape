import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/** Original-name semantic tests for Game.method137 (region-local tile update packets). */
public class GameRegionUpdateTest {
    private static final sun.misc.Unsafe UNSAFE;
    static {
        try {
            Field f = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            UNSAFE = (sun.misc.Unsafe) f.get(null);
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    private ItemDef[] oldItems;
    private ObjectDef[] oldObjects;
    private Animation[] oldAnims;
    private SpotAnim[] oldSpots;
    private Player oldPlayer;
    private int oldCycle;
    private boolean oldLowMem;
    private int oldUnitsNine;

    private static Object get(Object o, String name) throws Exception {
        Field f = o.getClass().getDeclaredField(name); f.setAccessible(true); return f.get(o);
    }
    private static Object sget(Class<?> c, String name) throws Exception {
        Field f = c.getDeclaredField(name); f.setAccessible(true); return f.get(null);
    }
    private static void sset(Class<?> c, String name, Object v) throws Exception {
        Field f = c.getDeclaredField(name); f.setAccessible(true); f.set(null, v);
    }

    @Before public void setUp() throws Exception {
        oldItems = (ItemDef[]) sget(ItemDef.class, "cache");
        ItemDef[] items = new ItemDef[10];
        for (int i = 0; i < 10; i++) { items[i] = (ItemDef) UNSAFE.allocateInstance(ItemDef.class); items[i].id = i + 1; }
        sset(ItemDef.class, "cache", items);
        oldObjects = (ObjectDef[]) sget(ObjectDef.class, "cache");
        ObjectDef[] objs = new ObjectDef[20];
        for (int i = 0; i < 20; i++) {
            objs[i] = (ObjectDef) UNSAFE.allocateInstance(ObjectDef.class);
            objs[i].type = -1; objs[i].childrenIDs = new int[0]; objs[i].varBitId = -1; objs[i].settingId = -1;
            objs[i].sizeX = 1; objs[i].sizeY = 1;
        }
        objs[0].type = 401; objs[0].sizeX = 2; objs[0].sizeY = 3;
        sset(ObjectDef.class, "cache", objs);
        oldAnims = Animation.anims;
        Animation.anims = new Animation[3];
        Animation.anims[1] = (Animation) UNSAFE.allocateInstance(Animation.class);
        oldSpots = SpotAnim.cache;
        SpotAnim.cache = new SpotAnim[6];
        for (int i = 0; i < 6; i++) {
            java.lang.reflect.Constructor<SpotAnim> c = SpotAnim.class.getDeclaredConstructor();
            c.setAccessible(true); SpotAnim.cache[i] = c.newInstance();
        }
        oldPlayer = Game.myPlayer; oldCycle = Game.loopCycle; oldLowMem = Game.lowMem;
        oldUnitsNine = Sounds.leadingSilence20msUnits[9];
        Game.lowMem = false;
        ((LruCache) sget(ObjectDef.class, "mruNodes2")).clear();
    }

    @After public void tearDown() throws Exception {
        sset(ItemDef.class, "cache", oldItems);
        sset(ObjectDef.class, "cache", oldObjects);
        Animation.anims = oldAnims; SpotAnim.cache = oldSpots;
        Game.myPlayer = oldPlayer; Game.loopCycle = oldCycle; Game.lowMem = oldLowMem;
        Sounds.leadingSilence20msUnits[9] = oldUnitsNine;
        ((LruCache) sget(ObjectDef.class, "mruNodes2")).clear();
    }

    private Game game() throws Exception {
        Game g = (Game) UNSAFE.allocateInstance(Game.class);
        g.groundArray = new NodeList[4][104][104];
        g.worldController = new WorldController(new int[4][105][105]);
        g.intGroundArray = new int[4][105][105];
        g.byteGroundArray = new byte[4][104][104];
        g.playerArray = new Player[8];
        g.pendingSceneChanges = new NodeList();
        g.transientGraphics = new NodeList();
        g.projectiles = new NodeList();
        Field cat = Game.class.getDeclaredField("anIntArray1177"); cat.setAccessible(true);
        cat.set(g, new int[] {0, 0, 0, 0, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3});
        g.plane = 1;
        g.updateBlockX = 16;
        g.updateBlockY = 32;
        g.unknownInt10 = 2;
        Game.loopCycle = 1000;
        return g;
    }

    private static Stream s(int... bytes) {
        byte[] b = new byte[bytes.length];
        for (int i = 0; i < b.length; i++) b[i] = (byte) bytes[i];
        return new Stream(b);
    }
    private static int[] cat(int[]... parts) {
        int n = 0; for (int[] p : parts) n += p.length;
        int[] r = new int[n]; int k = 0;
        for (int[] p : parts) for (int v : p) r[k++] = v;
        return r;
    }
    private static int[] i(int... v) { return v; }
    private static int[] usBE(int v) { return i(v >> 8, v); }
    private static int[] usBE_m128(int v) { return i(v >> 8, v + 128); }
    private static int[] usLE(int v) { return i(v, v >> 8); }
    private static int[] usLE_p128(int v) { return i(v + 128, v >> 8); }
    private static int pos(int x, int y) { return (x << 4) | y; }

    private void run(Game g, int opcode, int[] data) {
        Stream st = s(data);
        g.applyRegionUpdate(st, opcode);
        assertEquals("packet fully consumed", data.length, st.currentOffset);
    }

    private static void put(Game g, int x, int y, int id, int amount) {
        if (g.groundArray[g.plane][x][y] == null) g.groundArray[g.plane][x][y] = new NodeList();
        Item it = new Item(); it.ID = id; it.amount = amount;
        g.groundArray[g.plane][x][y].insertHead(it);
    }
    private static List<String> items(Game g, int x, int y) {
        List<String> r = new ArrayList<String>();
        NodeList l = g.groundArray[g.plane][x][y];
        if (l == null) return r;
        for (Item it = (Item) l.firstFromTail(); it != null; it = (Item) l.nextTowardHead()) r.add(it.ID + "x" + it.amount);
        Collections.sort(r);
        return r;
    }
    private static List<Class30_Sub1> pending(Game g) {
        List<Class30_Sub1> r = new ArrayList<Class30_Sub1>();
        for (Class30_Sub1 c = (Class30_Sub1) g.pendingSceneChanges.firstFromTail(); c != null; c = (Class30_Sub1) g.pendingSceneChanges.nextTowardHead()) r.add(c);
        return r;
    }

    @Test public void tilePositionByteUsesMaskedNibblesWithBaseOffsets() throws Exception {
        Game g = game();
        // 0x8B: bit 7 and bit 3 are ignored -> x nibble 0, y 3 ; base (16,32) -> tile (16,35)
        put(g, 16, 35, 5, 10); put(g, 19, 37, 5, 10);
        run(g, 84, cat(i(0x8B), usBE(5), usBE(10), usBE(77)));
        assertEquals(Arrays.asList("5x77"), items(g, 16, 35));
        assertEquals(Arrays.asList("5x10"), items(g, 19, 37));
        put(g, 23, 39, 5, 10);
        run(g, 84, cat(i(0x7F), usBE(5), usBE(10), usBE(78)));
        assertEquals(Arrays.asList("5x78"), items(g, 23, 39));
        // tile outside 0..103 is ignored without effect
        g.updateBlockX = 100;
        run(g, 84, cat(i(0x70), usBE(5), usBE(10), usBE(79)));
        assertEquals(Arrays.asList("5x78"), items(g, 23, 39));
    }

    @Test public void method137Op84ChangesOnlyMatchingIdAndOldAmount() throws Exception {
        Game g = game();
        put(g, 19, 37, 5, 10); put(g, 19, 37, 5, 20); put(g, 19, 37, 6, 10);
        run(g, 84, cat(i(pos(3, 5)), usBE(0x8005), usBE(11), usBE(99)));   // wrong old amount
        assertEquals(Arrays.asList("5x10", "5x20", "6x10"), items(g, 19, 37));
        run(g, 84, cat(i(pos(3, 5)), usBE(0x8005), usBE(10), usBE(99)));   // masked id, right old amount
        assertEquals(Arrays.asList("5x20", "5x99", "6x10"), items(g, 19, 37));
        run(g, 84, cat(i(pos(3, 5)), usBE(7), usBE(10), usBE(55)));        // absent id
        assertEquals(Arrays.asList("5x20", "5x99", "6x10"), items(g, 19, 37));
    }

    @Test public void method137Op156RemovesOneItemByMaskedIdAndClearsEmptyTile() throws Exception {
        Game g = game();
        put(g, 20, 34, 5, 1); put(g, 20, 34, 6, 2);
        run(g, 156, cat(i(pos(4, 2) + 128), usBE(9)));
        assertEquals(Arrays.asList("5x1", "6x2"), items(g, 20, 34));
        run(g, 156, cat(i(pos(4, 2) + 128), usBE(0x8005)));
        assertEquals(Arrays.asList("6x2"), items(g, 20, 34));
        assertNotNull(g.groundArray[1][20][34]);
        run(g, 156, cat(i(pos(4, 2) + 128), usBE(6)));
        assertNull(g.groundArray[1][20][34]);
    }

    @Test public void method137Op44AddsItemWithIdAndAmount() throws Exception {
        Game g = game();
        run(g, 44, cat(usLE_p128(7), usBE(1234), i(pos(2, 6))));
        assertEquals(Arrays.asList("7x1234"), items(g, 18, 38));
        run(g, 44, cat(usLE_p128(8), usBE(5), i(pos(2, 6))));
        assertEquals(Arrays.asList("7x1234", "8x5"), items(g, 18, 38));
        g.updateBlockY = 101;
        run(g, 44, cat(usLE_p128(9), usBE(5), i(pos(2, 6))));
        assertEquals(2, items(g, 18, 38).size());
    }

    @Test public void method137Op215SkipsItemsOwnedByLocalPlayerIndex() throws Exception {
        Game g = game();
        run(g, 215, cat(usBE_m128(7), i(128 - pos(1, 4)), usBE_m128(3), usBE(1234)));
        assertEquals(Arrays.asList("7x1234"), items(g, 17, 36));
        run(g, 215, cat(usBE_m128(8), i(128 - pos(1, 4)), usBE_m128(2), usBE(9)));   // owner == unknownInt10
        assertEquals(Arrays.asList("7x1234"), items(g, 17, 36));
    }

    @Test public void method137Op105QueuesSoundByRadiusAndPlayCount() throws Exception {
        Game g = game();
        for (String n : new String[] {"sound", "soundType", "soundDelay", "soundVolume"}) {
            Field f = Game.class.getDeclaredField(n); f.setAccessible(true); f.set(g, new int[50]);
        }
        g.soundEffectsEnabled = true;
        Player me = new Player(); Game.myPlayer = me;
        me.smallX[0] = 19; me.smallY[0] = 37;
        Sounds.leadingSilence20msUnits[9] = 12;
        run(g, 105, cat(i(pos(3, 5)), usBE(9), i(0xFD)));      // radius nibble 15; low 3 bits 5 (bit 3 ignored)
        assertEquals(1, g.currentSound);
        assertEquals(9, g.sound[0]); assertEquals(5, g.soundType[0]); assertEquals(12, g.soundDelay[0]);
        me.smallX[0] = 22;                                       // radius 3: dx 3 inside
        run(g, 105, cat(i(pos(3, 5)), usBE(9), i(0x31)));
        assertEquals(2, g.currentSound); assertEquals(1, g.soundType[1]);
        me.smallX[0] = 23;                                       // dx 4 outside
        run(g, 105, cat(i(pos(3, 5)), usBE(9), i(0x31)));
        assertEquals(2, g.currentSound);
        me.smallX[0] = 19; me.smallY[0] = 40;                    // dy 3 inside
        run(g, 105, cat(i(pos(3, 5)), usBE(9), i(0x31)));
        assertEquals(3, g.currentSound);
        me.smallY[0] = 41;                                       // dy 4 outside
        run(g, 105, cat(i(pos(3, 5)), usBE(9), i(0x31)));
        assertEquals(3, g.currentSound);
        g.soundEffectsEnabled = false; me.smallY[0] = 37;
        run(g, 105, cat(i(pos(3, 5)), usBE(9), i(0x31)));
        assertEquals(3, g.currentSound);
        g.soundEffectsEnabled = true; g.currentSound = 50;
        run(g, 105, cat(i(pos(3, 5)), usBE(9), i(0x31)));
        assertEquals(50, g.currentSound);
    }

    @Test public void method137Op160AnimatesObjectByShapeCategoryWithCornerHeights() throws Exception {
        Game g = game();
        for (int x = 0; x < 105; x++) for (int y = 0; y < 105; y++) {
            g.intGroundArray[1][x][y] = x == 17 ? (y == 36 ? 200 : 300) : (y == 36 ? 100 : 400);
        }
        Field gf = WorldController.class.getDeclaredField("groundArray"); gf.setAccessible(true);
        Ground[][][] grounds = (Ground[][][]) gf.get(g.worldController);
        Ground tile = new Ground(1, 16, 36);
        grounds[1][16][36] = tile;
        tile.obj1 = new Object1(); tile.obj1.uid = 401 << 14;
        tile.obj2 = new Object2(); tile.obj2.uid = 401 << 14;
        tile.obj3 = new Object3(); tile.obj3.uid = 401 << 14;
        Object5 so = new Object5(); so.uid = (2 << 29) | (401 << 14); so.minTileX = 16; so.minTileY = 36;
        tile.objectCount = 1; tile.obj5Array[0] = so;
        int p = 128 - pos(0, 4);
        run(g, 160, cat(i(p), i(128 - ((2 << 2) | 3)), usBE_m128(1)));   // shape 2 orientation 3: two wall models
        AnimatedObject m = (AnimatedObject) tile.obj1.model, m2 = (AnimatedObject) tile.obj1.secondaryModel;
        assertEquals(401, get(m, "objectId")); assertEquals(2, get(m, "type")); assertEquals(7, get(m, "orientation"));
        assertEquals(2, get(m2, "type")); assertEquals(0, get(m2, "orientation"));
        assertEquals(100, get(m, "heightX0Y0")); assertEquals(200, get(m, "heightX1Y0"));
        assertEquals(300, get(m, "heightX1Y1")); assertEquals(400, get(m, "heightX0Y1"));
        assertNotNull(get(m, "animation"));
        tile.obj1.secondaryModel = null;
        run(g, 160, cat(i(p), i(128 - ((0 << 2) | 1)), usBE_m128(1)));   // shape 0 orientation 1
        m = (AnimatedObject) tile.obj1.model;
        assertEquals(0, get(m, "type")); assertEquals(1, get(m, "orientation")); assertNull(tile.obj1.secondaryModel);
        run(g, 160, cat(i(p), i(128 - ((5 << 2) | 2)), usBE_m128(1)));   // shape 5: wall decoration, type 4, orientation 0
        m = (AnimatedObject) tile.obj2.model;
        assertEquals(4, get(m, "type")); assertEquals(0, get(m, "orientation"));
        run(g, 160, cat(i(p), i(128 - ((11 << 2) | 2)), usBE_m128(1)));  // shape 11 animated as shape 10
        m = (AnimatedObject) so.model;
        assertEquals(10, get(m, "type")); assertEquals(2, get(m, "orientation"));
        run(g, 160, cat(i(p), i(128 - ((22 << 2) | 1)), usBE_m128(1)));  // shape 22: ground decoration, type 22
        m = (AnimatedObject) tile.obj3.model;
        assertEquals(22, get(m, "type")); assertEquals(1, get(m, "orientation"));
        // tile x 103 is outside the handled 0..102 range
        Object before = tile.obj1.model;
        g.updateBlockX = 96;
        run(g, 160, cat(i(128 - pos(7, 4)), i(128 - ((2 << 2) | 3)), usBE_m128(1)));
        assertSame(before, tile.obj1.model);
    }

    @Test public void method137Op147AttachesTemporaryModelToPlayerWithOrderedBounds() throws Exception {
        for (int pass = 0; pass < 2; pass++) {
            Game g = game();
            Model model = (Model) UNSAFE.allocateInstance(Model.class);
            int orientation = pass == 0 ? 2 : 1;                // odd orientation swaps footprint
            ((LruCache) sget(ObjectDef.class, "mruNodes2")).removeFromCache(model, (401 << 6) + orientation);
            Player target = new Player();
            g.playerArray[5] = target;
            Game.myPlayer = new Player();
            for (int x = 0; x < 105; x++) for (int y = 0; y < 105; y++) g.intGroundArray[1][x][y] = 60;
            int xFirst = pass == 0 ? 3 : -2, xSecond = pass == 0 ? -2 : 3;   // pass 1 sends them reversed
            int yFirst = pass == 0 ? 4 : -1, ySecond = pass == 0 ? -1 : 4;
            run(g, 147, cat(i(128 - pos(1, 2)), usBE(5), i(128 - xFirst), usLE(11), i(-yFirst), usBE(22),
                    i(128 - ((10 << 2) | orientation)), i(xSecond), usBE(401), i(-ySecond)));
            assertSame(model, target.attachedModel);
            assertEquals(1011, target.attachedModelStartCycle); assertEquals(1022, target.attachedModelEndCycle);
            int tx = 17, ty = 34;
            int sx = pass == 0 ? 2 : 3, sy = pass == 0 ? 3 : 2;
            assertEquals(tx * 128 + sx * 64, target.attachedModelX);
            assertEquals(ty * 128 + sy * 64, target.attachedModelY);
            assertEquals(60, target.attachedModelHeight);
            assertEquals(tx - 2, target.attachedMinTileX); assertEquals(tx + 3, target.attachedMaxTileX);
            assertEquals(ty - 1, target.attachedMinTileY); assertEquals(ty + 4, target.attachedMaxTileY);
            List<Class30_Sub1> ps = pending(g);
            assertEquals(1, ps.size());
            Class30_Sub1 c = ps.get(0);
            assertEquals(23, c.restoreDelayTicks); assertEquals(12, c.placementDelayTicks);
            assertEquals(-1, c.requestedObjectId); assertEquals(2, c.objectCategory);
            assertEquals(tx, c.tileX); assertEquals(ty, c.tileY); assertEquals(1, c.scenePlane);
        }
        // the local player's own index selects myPlayer; a missing player does nothing
        Game g = game();
        Model model = (Model) UNSAFE.allocateInstance(Model.class);
        ((LruCache) sget(ObjectDef.class, "mruNodes2")).removeFromCache(model, (401 << 6));
        Player me = new Player(); Game.myPlayer = me;
        run(g, 147, cat(i(128 - pos(1, 2)), usBE(2), i(128), usLE(0), i(0), usBE(4), i(128 - (10 << 2)), i(0), usBE(401), i(0)));
        assertSame(model, me.attachedModel);
        g = game(); me = new Player(); Game.myPlayer = me;
        run(g, 147, cat(i(128 - pos(1, 2)), usBE(6), i(128), usLE(0), i(0), usBE(4), i(128 - (10 << 2)), i(0), usBE(401), i(0)));
        assertNull(me.attachedModel);
        assertEquals(0, pending(g).size());
    }

    /** Pass 217: op 147 locals i22/j22/k22/l22 are the four ground corner heights (x0y0, x1y0, x1y1, x0y1) fed to a terrain-adjusting model. */
    @Test public void method137Op147CornerLocalsAreTileCornerHeightsForTerrainAdjustingModel() throws Exception {
        java.lang.reflect.Constructor<Model> mc = Model.class.getDeclaredConstructor();
        mc.setAccessible(true);
        int[][] corners = {{100, 220, 340, 500}, {100, 220, 700, 500}};   // second run changes only ground[x+1][y+1]
        int[][] want = {{-190, -70, 50, 210}, {-280, -160, 320, 120}};
        for (int run = 0; run < 2; run++) {
            Game g = game();
            ObjectDef def = ObjectDef.forID(401);
            def.adjustsToTerrain = true;
            Model base = mc.newInstance();
            base.vertexCount = 4;
            base.vertexX = new int[] {-64, 64, 64, -64};
            base.vertexZ = new int[] {-64, -64, 64, 64};
            base.vertexY = new int[4];
            ((LruCache) sget(ObjectDef.class, "mruNodes2")).clear();
            ((LruCache) sget(ObjectDef.class, "mruNodes2")).removeFromCache(base, (401 << 6));
            Player target = new Player();
            g.playerArray[5] = target;
            Game.myPlayer = new Player();
            for (int x = 0; x < 105; x++) for (int y = 0; y < 105; y++) g.intGroundArray[1][x][y] = 999;   // wrong neighbours stand out
            int tx = 17, ty = 34;
            g.intGroundArray[1][tx][ty] = corners[run][0];
            g.intGroundArray[1][tx + 1][ty] = corners[run][1];
            g.intGroundArray[1][tx + 1][ty + 1] = corners[run][2];
            g.intGroundArray[1][tx][ty + 1] = corners[run][3];
            run(g, 147, cat(i(128 - pos(1, 2)), usBE(5), i(128), usLE(11), i(0), usBE(22),
                    i(128 - (10 << 2)), i(0), usBE(401), i(0)));
            Model m = target.attachedModel;
            assertNotNull(m);
            assertNotSame("terrain adjustment copies the model", base, m);
            assertArrayEquals("vertex at (-64,-64),(64,-64),(64,64),(-64,64)", want[run], Arrays.copyOf(m.vertexY, 4));
            assertArrayEquals("source model untouched", new int[4], base.vertexY);
        }
    }

    @Test public void method137Op101And151QueueRemovalAndSpawnWithShapeCategory() throws Exception {
        Game g = game();
        run(g, 101, cat(i(-((5 << 2) | 2)), i(pos(2, 3))));
        Class30_Sub1 c = pending(g).get(0);
        assertEquals(-1, c.requestedObjectId); assertEquals(5, c.requestedShape); assertEquals(2, c.requestedOrientation);
        assertEquals(1, c.objectCategory); assertEquals(18, c.tileX); assertEquals(35, c.tileY);
        assertEquals(0, c.placementDelayTicks); assertEquals(-1, c.restoreDelayTicks);
        g = game();
        run(g, 151, cat(i(pos(4, 1) + 128), usLE(321), i(128 - ((22 << 2) | 3))));
        c = pending(g).get(0);
        assertEquals(321, c.requestedObjectId); assertEquals(22, c.requestedShape); assertEquals(3, c.requestedOrientation);
        assertEquals(3, c.objectCategory); assertEquals(20, c.tileX); assertEquals(33, c.tileY);
        assertEquals(0, c.placementDelayTicks); assertEquals(-1, c.restoreDelayTicks);
        g = game(); g.updateBlockX = 100;
        run(g, 151, cat(i(pos(7, 1) + 128), usLE(321), i(128 - (22 << 2))));
        assertEquals(0, pending(g).size());
    }

    @Test public void method137Op4SpawnsTileGraphicWithHeightOffsetAndDelay() throws Exception {
        Game g = game();
        for (int x = 0; x < 105; x++) for (int y = 0; y < 105; y++) g.intGroundArray[1][x][y] = 90;
        run(g, 4, cat(i(pos(2, 3)), usBE(4), i(25), usBE(7)));
        StationaryGraphic gr = (StationaryGraphic) g.transientGraphics.firstFromTail();
        assertNotNull(gr);
        assertSame(SpotAnim.cache[4], get(gr, "spotAnimation"));
        assertEquals(90 - 25, gr.height);
        assertEquals(1007, get(gr, "startCycle"));
        assertEquals(35 * 128 + 64, gr.worldY);   // world Y (field was named worldX before pass 217)
        assertEquals(18 * 128 + 64, gr.worldX);
        assertEquals(1, gr.plane);
    }

    @Test public void method137Op117CreatesProjectileFromTilesAndScaledHeights() throws Exception {
        Game g = game();
        for (int x = 0; x < 105; x++) for (int y = 0; y < 105; y++) g.intGroundArray[1][x][y] = 500;
        run(g, 117, cat(i(pos(1, 2), 2, -1), usBE(0xFFFD), usBE(3), i(5, 6), usBE(10), usBE(30), i(40, 9)));
        Projectile p = (Projectile) g.projectiles.firstFromTail();
        assertNotNull(p);
        assertEquals(-3, p.target);
        assertEquals(1, p.plane);
        assertEquals(1010, p.startCycle); assertEquals(1030, p.endCycle);
        assertEquals(6 * 4, p.targetHeightOffset);
        assertEquals(17 * 128 + 64, ((Number) get(p, "originX")).intValue());
        assertEquals(34 * 128 + 64, ((Number) get(p, "originY")).intValue());
        assertEquals(500 - 20, ((Number) get(p, "originHeight")).intValue());
        assertEquals(40, ((Number) get(p, "launchAngle")).intValue());
        assertEquals(9, ((Number) get(p, "startOffset")).intValue());
        assertSame(SpotAnim.cache[3], get(p, "spotAnim"));
        g = game();
        run(g, 117, cat(i(pos(1, 2), 2, -1), usBE(0xFFFD), usBE(0xFFFF), i(5, 6), usBE(10), usBE(30), i(40, 9)));
        assertNull(g.projectiles.firstFromTail());
        run(g, 117, cat(i(pos(1, 2), 100, -1), usBE(0xFFFD), usBE(3), i(5, 6), usBE(10), usBE(30), i(40, 9)));
        assertNull(g.projectiles.firstFromTail());
    }
}
