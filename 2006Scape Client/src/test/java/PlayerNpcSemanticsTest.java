import java.io.ByteArrayOutputStream;
import java.lang.reflect.*;
import java.util.*;
import org.junit.*;
import static org.junit.Assert.*;

/** Pass 172: semantic tests for Player / NPC members, written against the original identifiers only. */
public class PlayerNpcSemanticsTest {
    private static Field f(Class<?> c, String n) throws Exception { Field x = c.getDeclaredField(n); x.setAccessible(true); return x; }
    private static void seti(Object o, String n, int v) throws Exception { f(o.getClass(), n).setInt(o, v); }
    private static Object get(Object o, String n) throws Exception { return f(o.getClass(), n).get(o); }
    private static void set(Object o, String n, Object v) throws Exception { f(o.getClass(), n).set(o, v); }
    private static void sset(Class<?> c, String n, Object v) throws Exception { f(c, n).set(null, v); }
    private static Object sget(Class<?> c, String n) throws Exception { return f(c, n).get(null); }
    private static <T> T make(Class<T> c) throws Exception { Constructor<T> k = c.getDeclaredConstructor(); k.setAccessible(true); return k.newInstance(); }
    private static int[] xs(Model m) { return Arrays.copyOf(m.vertexX, m.vertexCount); }
    private static int[] ys(Model m) { return Arrays.copyOf(m.vertexY, m.vertexCount); }
    private static int[] zs(Model m) { return Arrays.copyOf(m.vertexZ, m.vertexCount); }

    /** Triangle (X,0,0) (X+10,0,0) (X,10,10) with X=20+id; one face. */
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

    private Object savedHeaders, savedFetcher, savedFrames, savedSpot, savedIdk; private Animation[] savedAnims; private int savedLoop;
    private boolean savedLowMem; private Player savedMe;
    @Before public void setUp() throws Exception {
        savedHeaders = sget(Model.class, "modelHeaders"); savedFetcher = sget(Model.class, "modelRequester");
        savedFrames = sget(AnimationFrame.class, "loadedFrames"); savedSpot = SpotAnim.cache; savedIdk = IDK.cache;
        savedAnims = Animation.anims; savedLoop = Game.loopCycle; savedMe = Game.myPlayer; savedLowMem = Game.lowMem;
        Model.initModelHeaders(64, new Spy());
        for (int id : new int[]{5, 6, 7}) Model.loadModelHeader(triangle(id, 40000), id);
        EntityDef.mruNodes.clear(); Player.mruNodes.clear(); SpotAnim.modelCache.clear();
    }
    @After public void tearDown() throws Exception {
        sset(Model.class, "modelHeaders", savedHeaders); sset(Model.class, "modelRequester", savedFetcher);
        sset(AnimationFrame.class, "loadedFrames", savedFrames); SpotAnim.cache = (SpotAnim[]) savedSpot; IDK.cache = (IDK[]) savedIdk;
        Animation.anims = savedAnims; Game.loopCycle = savedLoop; Game.myPlayer = savedMe; Game.lowMem = savedLowMem;
        EntityDef.mruNodes.clear(); Player.mruNodes.clear(); SpotAnim.modelCache.clear();
    }

    /** a face-less model with the given vertices; groupIds (if any) are the per-vertex animation group ids, groups the built groups */
    private static Model bare(int[] x, int[] y, int[] z, int[] groupIds, int[][] groups) throws Exception {
        Model m = make(Model.class);
        m.vertexCount = x.length; m.faceCount = 0; m.vertexX = x; m.vertexY = y; m.vertexZ = z;
        set(m, "vertexGroupIds", groupIds); m.vertexGroups = groups; return m;
    }
    private static Model attached() throws Exception { return bare(new int[]{1, 2}, new int[]{3, 4}, new int[]{5, 6}, null, null); }
    /** a descriptor whose animated body model is already cached under its type: vertices x {10,20}, one vertex per animation group */
    private static EntityDef desc(long type, int ignored) throws Exception {
        EntityDef d = make(EntityDef.class); d.type = type;
        EntityDef.mruNodes.removeFromCache(bare(new int[]{10, 20}, new int[]{0, 0}, new int[]{0, 0}, null, new int[][]{{0}, {1}}), type);
        return d;
    }
    private static AnimationTransformTable table() {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        for (int v : new int[]{2, 1, 1, 1, 0, 1, 1}) o.write(v);
        return new AnimationTransformTable(new Stream(o.toByteArray()));
    }
    private static AnimationFrame frame(AnimationTransformTable t, int dx) throws Exception {
        AnimationFrame fr = make(AnimationFrame.class);
        fr.transformTable = t; fr.transformCount = 2; fr.transformIndices = new int[]{0, 1};
        fr.transformX = new int[]{dx, dx}; fr.transformY = new int[]{0, 0}; fr.transformZ = new int[]{0, 0}; return fr;
    }
    private static Animation anim(int[] frameIds, int[] mask) throws Exception {
        Animation a = make(Animation.class); a.frameIds = frameIds; a.interleavedTransformIndices = mask;
        a.slot5EquipmentOverride = -1; a.slot3EquipmentOverride = -1; return a;
    }
    /** a spot animation (cache slot 0) whose model is model 7 and whose frame list is {5, 6}; only frame 6 (dx 9) exists */
    private static void installSpot() throws Exception {
        AnimationFrame[] fr = new AnimationFrame[8]; fr[6] = frame(table(), 9);
        sset(AnimationFrame.class, "loadedFrames", fr);
        SpotAnim s = make(SpotAnim.class); seti(s, "modelId", 7); seti(s, "recordIndex", 0);
        SpotAnim.modelCache.removeFromCache(bare(new int[]{100, 200}, new int[]{7, 8}, new int[]{0, 0}, new int[]{0, 1}, null), 0);
        s.animation = anim(new int[]{5, 6}, null);
        SpotAnim.cache = new SpotAnim[]{s};
    }

    // ---------- Player: attached (temporary object) model ----------
    private Player attachedPlayer() throws Exception {
        Player p = new Player(); p.visible = true; p.desc = desc(21L, 5);
        p.x = 1000; p.y = 2000; p.groundHeight = 120;
        p.attachedModelStartCycle = 100; p.attachedModelEndCycle = 200;
        p.attachedModelX = 1300; p.attachedModelHeight = 500; p.attachedModelY = 2400;
        p.attachedModel = attached();
        return p;
    }
    @Test public void attachedModelIsMergedOnlyInsideTheCycleWindowAtTheOffsetFromThePlayer() throws Exception {
        Player p = attachedPlayer();
        int[] ax = xs(p.attachedModel), ay = ys(p.attachedModel), az = zs(p.attachedModel);
        Game.loopCycle = 99;                                      // before the start cycle: not merged
        assertEquals(2, p.getRotatedModel().vertexCount);
        assertNotNull(p.attachedModel);                             // and not dropped either
        Game.loopCycle = 100;                                     // start cycle is inclusive
        Model m = p.getRotatedModel();
        assertEquals(4, m.vertexCount);
        assertArrayEquals(new int[]{10, 20}, Arrays.copyOf(xs(m), 2));
        int[] wantX = new int[2], wantY = new int[2], wantZ = new int[2];
        for (int i = 0; i < 2; i++) { wantX[i] = ax[i] + (1300 - 1000); wantY[i] = ay[i] + (500 - 120); wantZ[i] = az[i] + (2400 - 2000); }
        assertArrayEquals(wantX, Arrays.copyOfRange(xs(m), 2, 4));
        assertArrayEquals(wantY, Arrays.copyOfRange(ys(m), 2, 4));
        assertArrayEquals(wantZ, Arrays.copyOfRange(zs(m), 2, 4));
        assertArrayEquals(ax, xs(p.attachedModel));                 // the stored model is moved back afterwards
        Game.loopCycle = 199;
        assertEquals(4, p.getRotatedModel().vertexCount);
        assertNotNull(p.attachedModel);
        Game.loopCycle = 200;                                     // end cycle is exclusive and drops the model
        assertEquals(2, p.getRotatedModel().vertexCount);
        assertNull(p.attachedModel);
    }
    @Test public void staticModelOnlyReturnsThePlainBodyModelWithoutAttachedOrSpotModels() throws Exception {
        installSpot();
        Player p = attachedPlayer(); p.anInt1520 = 0; p.anInt1521 = 1; p.anInt1524 = 50;
        Game.loopCycle = 150;
        assertEquals(6, p.getRotatedModel().vertexCount);         // body + spot + attached
        p.staticModelOnly = true;
        assertEquals(2, p.getRotatedModel().vertexCount);
        assertNotNull(p.attachedModel);                             // the flag leaves the attached model alone
        p.staticModelOnly = false;
        assertEquals(6, p.getRotatedModel().vertexCount);
    }
    @Test public void playerSpotModelIsLiftedByAnInt1524AndAnimatedByTheFrameIndexedByAnInt1521() throws Exception {
        installSpot();
        Player p = new Player(); p.visible = true; p.desc = desc(21L, 5);
        p.anInt1520 = 0; p.anInt1521 = 1; p.anInt1524 = 50;
        Model m = p.getRotatedModel();
        assertEquals(4, m.vertexCount);
        assertArrayEquals(new int[]{7 - 50, 8 - 50}, Arrays.copyOfRange(ys(m), 2, 4));
        assertArrayEquals(new int[]{109, 209}, Arrays.copyOfRange(xs(m), 2, 4));   // frame id = frameIds[anInt1521] = 6 (dx 9)
    }

    // ---------- Player: appearance colours and the last built model key ----------
    private static byte[] appearance(int[] colours) {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        o.write(0); o.write(0); o.write(0);
        for (int j = 0; j < 12; j++) o.write(0);
        for (int c : colours) o.write(c);
        for (int k = 0; k < 7; k++) { o.write(255); o.write(255); }
        for (int k = 0; k < 8; k++) o.write(0);
        o.write(3); o.write(0); o.write(0);
        return o.toByteArray();
    }
    @Test public void appearanceColoursHoldsOneValidatedPaletteIndexPerColourSlot() throws Exception {
        int[][] pal = Game.anIntArrayArray1003;
        int[] in = new int[5];
        for (int i = 0; i < 5; i++) in[i] = pal[i].length - 1;     // largest valid index of each slot's palette
        Player p = new Player();
        p.updatePlayer(new Stream(appearance(in)));
        assertArrayEquals(in, p.appearanceColours);
        int[] bad = in.clone(); bad[2] = pal[2].length; bad[4] = 255;   // one past the palette, and a wild value
        p.updatePlayer(new Stream(appearance(bad)));
        assertArrayEquals(new int[]{in[0], in[1], 0, in[3], 0}, p.appearanceColours);
    }
    private static IDK kit(int... models) throws Exception { IDK k = make(IDK.class); set(k, "bodyModelIds", models); return k; }
    @Test public void lastBuiltModelKeyIsTheKeyOfTheLastModelBuiltAndIsTheFallbackWhileKitModelsLoad() throws Exception {
        IDK.cache = new IDK[]{kit(5), kit(40)};                    // kit 0 loaded; kit 1's model 40 has no header
        Player p = new Player(); p.visible = true; seti(p, "gender", 0);
        f(Player.class, "appearanceKey").setLong(p, 4242L);
        p.equipment[0] = 256;
        assertEquals(-1L, f(Player.class, "lastBuiltModelKey").getLong(p));
        Method body = Player.class.getDeclaredMethod("buildBodyModel"); body.setAccessible(true);
        assertNotNull(body.invoke(p));
        assertEquals(4242L, f(Player.class, "lastBuiltModelKey").getLong(p));   // set when a model is built
        f(Player.class, "appearanceKey").setLong(p, 9L);                // new look with a kit part that is not loaded yet
        p.equipment[0] = 256 + 1;
        assertNotNull(body.invoke(p));                                  // falls back to the model cached under lastBuiltModelKey
        assertEquals(4242L, f(Player.class, "lastBuiltModelKey").getLong(p));   // the fallback does not change it
        f(Player.class, "lastBuiltModelKey").setLong(p, -1L);
        assertNull(body.invoke(p));                                     // no previous model: nothing to draw
        f(Player.class, "lastBuiltModelKey").setLong(p, 31337L);                // a key with nothing cached under it
        assertNull(body.invoke(p));
    }

    // ---------- Player: tile bounds and ground height passed to the scene ----------
    private static sun.misc.Unsafe unsafe() throws Exception { Field u = sun.misc.Unsafe.class.getDeclaredField("theUnsafe"); u.setAccessible(true); return (sun.misc.Unsafe) u.get(null); }
    @Test public void tileBoundFieldsAreTheMinMaxTilesOfTheAttachedModelFootprint() throws Exception {
        Game g = (Game) unsafe().allocateInstance(Game.class);
        int[][][] ground = new int[4][105][105];
        for (int[][] plane : ground) for (int[] row : plane) Arrays.fill(row, 77);
        set(g, "intGroundArray", ground); set(g, "byteGroundArray", new byte[4][104][104]);
        WorldController wc = new WorldController(ground);
        set(g, "worldController", wc);
        Player me = new Player(); me.visible = true; me.desc = desc(21L, 5);
        me.x = 5 * 128 + 64; me.y = 9 * 128 + 64;
        me.attachedModelStartCycle = 0; me.attachedModelEndCycle = 100; me.attachedModel = attached();
        me.attachedMinTileX = 3; me.attachedMaxTileX = 4; me.attachedMinTileY = 7; me.attachedMaxTileY = 10;
        Game.myPlayer = me; Game.loopCycle = 50; Game.lowMem = false;
        g.plane = 0;
        g.addPlayersToScene(true);
        Ground[][][] grounds = (Ground[][][]) get(wc, "groundArray");
        for (int tx = 0; tx < 8; tx++) for (int ty = 0; ty < 14; ty++) {
            boolean covered = tx >= 3 && tx <= 4 && ty >= 7 && ty <= 10;
            Ground gr = grounds[0][tx][ty];
            assertEquals("tile " + tx + "," + ty, covered, gr != null && gr.objectCount > 0);
        }
        Object5 o = grounds[0][3][7].obj5Array[0];
        assertEquals(3, o.minTileX); assertEquals(4, o.maxTileX); assertEquals(7, o.minTileY); assertEquals(10, o.maxTileY);
        assertEquals(me.x, o.worldX); assertEquals(me.y, o.worldY);
        assertEquals(77, me.groundHeight); assertEquals(77, o.height);   // the player's own ground height is what the scene gets
    }

    // ---------- NPC ----------
    private static NPC npc(int anim, int hold, int animEntry, int mov, int idle, int movEntry) throws Exception {
        NPC n = new NPC(); n.desc = desc(31L, 5);
        n.anim = anim; n.anInt1529 = hold; n.anInt1527 = animEntry; n.movementAnimationId = mov; n.idleAnimationId = idle; n.anInt1518 = movEntry;
        return n;
    }
    private static int[] npcXs(NPC n) throws Exception {
        Method m = NPC.class.getDeclaredMethod("buildAnimatedModel"); m.setAccessible(true);
        return xs((Model) m.invoke(n));
    }
    private static int[] oracle(int second, int main, int[] mask) throws Exception {
        EntityDef.mruNodes.clear();
        return xs(desc(31L, 5).getAnimatedModel(second, main, mask));
    }
    @Test public void method450CombinesTheMainAnimationFrameWithTheMovementFrameThroughTheMask() throws Exception {
        AnimationTransformTable t = table();
        AnimationFrame[] fr = new AnimationFrame[8]; fr[3] = frame(t, 3000); fr[4] = frame(t, 100); fr[5] = frame(t, 7); fr[6] = frame(t, 9);
        sset(AnimationFrame.class, "loadedFrames", fr);
        int[] mask = {1, 1000};
        Animation.anims = new Animation[]{anim(new int[]{3, 4}, mask), anim(new int[]{5, 6}, null)};
        // main animation 0 entry 1 = frame 4; movement animation 1 entry 1 = frame 6
        assertArrayEquals(oracle(6, 4, mask), npcXs(npc(0, 0, 1, 1, 0, 1)));          // both: movement frame is the second argument
        assertArrayEquals(oracle(-1, 4, mask), npcXs(npc(0, 0, 1, 1, 1, 1)));         // movement == idle: movement frame dropped
        assertArrayEquals(oracle(5, 3, mask), npcXs(npc(0, 0, 0, 1, 0, 0)));          // entries 0: frames 3 and 5
        assertArrayEquals(oracle(-1, 6, null), npcXs(npc(-1, 0, 1, 1, 0, 1)));        // no main animation: movement frame alone, unmasked
        assertArrayEquals(oracle(-1, 6, null), npcXs(npc(0, 1, 1, 1, 0, 1)));         // hold counter anInt1529 set: main animation ignored
        assertArrayEquals(oracle(-1, -1, null), npcXs(npc(-1, 0, 1, -1, 0, 1)));      // nothing playing
        assertFalse(Arrays.equals(oracle(6, 4, mask), oracle(-1, 4, mask)));         // guards: the cases above really differ
        assertFalse(Arrays.equals(oracle(6, 4, mask), oracle(5, 3, mask)));
        assertFalse(Arrays.equals(oracle(-1, 6, null), oracle(-1, 4, mask)));
    }
    @Test public void npcSpotModelIsLiftedByAnInt1524AndMergedAfterTheBodyModel() throws Exception {
        installSpot();
        NPC n = new NPC(); n.desc = desc(31L, 5); n.anInt1520 = 0; n.anInt1521 = 1; n.anInt1524 = 50;
        Model m = n.getRotatedModel();
        assertEquals(4, m.vertexCount);
        assertArrayEquals(new int[]{10, 20}, Arrays.copyOf(xs(m), 2));
        assertArrayEquals(new int[]{7 - 50, 8 - 50}, Arrays.copyOfRange(ys(m), 2, 4));
        assertArrayEquals(new int[]{109, 209}, Arrays.copyOfRange(xs(m), 2, 4));   // frame id = frameIds[anInt1521] = 6 (dx 9)
        n.anInt1521 = -1;
        assertEquals(2, n.getRotatedModel().vertexCount);
    }
}
