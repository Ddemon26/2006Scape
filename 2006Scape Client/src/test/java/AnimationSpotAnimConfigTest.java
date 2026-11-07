import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** Pass 164: observations of the seq.dat / spotanim.dat decoders and their consumers (original identifiers only). */
public class AnimationSpotAnimConfigTest {
    private Animation[] savedAnims;
    private SpotAnim[] savedSpots;
    private Object savedHeaders, savedRequester;
    private LruCache savedModelCache;
    private final java.util.List<Integer> requests = new java.util.ArrayList<Integer>();

    @Before public void save() throws Exception {
        savedAnims = Animation.anims; savedSpots = SpotAnim.cache;
        savedHeaders = sget(Model.class, "modelHeaders"); savedRequester = sget(Model.class, "modelRequester");
        savedModelCache = SpotAnim.modelCache;
        SpotAnim.modelCache = new LruCache(30);
    }
    @After public void restore() throws Exception {
        Animation.anims = savedAnims; SpotAnim.cache = savedSpots;
        sset(Model.class, "modelHeaders", savedHeaders); sset(Model.class, "modelRequester", savedRequester);
        SpotAnim.modelCache = savedModelCache;
    }

    private static Object sget(Class<?> c, String n) throws Exception { Field f = c.getDeclaredField(n); f.setAccessible(true); return f.get(null); }
    private static void sset(Class<?> c, String n, Object v) throws Exception { Field f = c.getDeclaredField(n); f.setAccessible(true); f.set(null, v); }
    private static Object get(Object o, String n) throws Exception { Field f = o.getClass().getDeclaredField(n); f.setAccessible(true); return f.get(o); }
    private static void set(Object o, String n, Object v) throws Exception { Field f = o.getClass().getDeclaredField(n); f.setAccessible(true); f.set(o, v); }
    private static sun.misc.Unsafe unsafe() throws Exception { Field f = sun.misc.Unsafe.class.getDeclaredField("theUnsafe"); f.setAccessible(true); return (sun.misc.Unsafe) f.get(null); }

    /** A StreamLoader whose single entry is stored raw (archiveDecompressed path). */
    private static StreamLoader loader(String name, byte[] data) throws Exception {
        StreamLoader l = (StreamLoader) unsafe().allocateInstance(StreamLoader.class);
        int hash = 0; String up = name.toUpperCase();
        for (int i = 0; i < up.length(); i++) hash = hash * 61 + up.charAt(i) - 32;
        set(l, "archiveData", data); set(l, "entryCount", 1); set(l, "entryNameHashes", new int[]{hash});
        set(l, "entryUnpackedSizes", new int[]{data.length}); set(l, "entryPackedSizes", new int[]{data.length});
        set(l, "entryOffsets", new int[]{0}); set(l, "archiveDecompressed", true);
        return l;
    }
    private static void u8(ByteArrayOutputStream o, int v) { o.write(v); }
    private static void u16(ByteArrayOutputStream o, int v) { o.write(v >> 8); o.write(v); }
    private static void unpackSeq(byte[] records) throws Exception { Animation.anims = null; Animation.unpackConfig(loader("seq.dat", records)); }
    private static void unpackSpot(byte[] records) throws Exception { SpotAnim.cache = null; SpotAnim.unpackConfig(loader("spotanim.dat", records)); }
    private static Animation newSeq() throws Exception { Constructor<Animation> c = Animation.class.getDeclaredConstructor(); c.setAccessible(true); return c.newInstance(); }

    // ---------------- Animation decoder ----------------
    @Test public void seqRecordFillsEveryFieldFromItsOpcode() throws Exception {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        u16(o, 2);
        u8(o, 1); u8(o, 3);                                   // 3 frame pairs
        u16(o, 100); u16(o, 200); u16(o, 7);
        u16(o, 101); u16(o, 65535); u16(o, 8);                // second value 65535 -> -1
        u16(o, 65535); u16(o, 202); u16(o, 0);                // first value is NOT remapped
        u8(o, 2); u16(o, 1);
        u8(o, 3); u8(o, 2); u8(o, 11); u8(o, 22);
        u8(o, 4);
        u8(o, 5); u8(o, 9);
        u8(o, 6); u16(o, 1234);
        u8(o, 7); u16(o, 4321);
        u8(o, 8); u8(o, 4);
        u8(o, 9); u8(o, 1);
        u8(o, 10); u8(o, 0);
        u8(o, 11); u8(o, 6);
        u8(o, 12); u16(o, 0); u16(o, 0);                      // skipped int
        u8(o, 0);
        u8(o, 0);                                             // second record: empty
        unpackSeq(o.toByteArray());
        Animation a = Animation.anims[0];
        assertEquals(3, a.frameCount);
        assertArrayEquals(new int[]{100, 101, 65535}, a.frameIds);
        assertArrayEquals(new int[]{200, -1, 202}, a.secondFrameIds);
        assertArrayEquals(new int[]{7, 8, 0}, (int[]) get(a, "frameDelays"));
        assertEquals(1, a.loopBackFrames);
        assertArrayEquals(new int[]{11, 22, 0x98967f}, a.interleavedTransformIndices);
        assertTrue(a.extendByYaw);
        assertEquals(9, a.priority);
        assertEquals(1234, a.slot5EquipmentOverride); assertEquals(4321, a.slot3EquipmentOverride);
        assertEquals(4, a.maxLoops); assertEquals(1, a.movingPrecedence); assertEquals(0, a.idlePrecedence); assertEquals(6, a.replayMode);
    }

    @Test public void seqDefaultsAndMaskDependentPrecedenceDefaults() throws Exception {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        u16(o, 2);
        u8(o, 0);                                             // empty record
        u8(o, 3); u8(o, 0); u8(o, 0);                         // mask (only sentinel), nothing else
        unpackSeq(o.toByteArray());
        Animation a = Animation.anims[0], m = Animation.anims[1];
        assertEquals(1, a.frameCount); assertArrayEquals(new int[]{-1}, a.frameIds); assertArrayEquals(new int[]{-1}, a.secondFrameIds);
        assertArrayEquals(new int[]{-1}, (int[]) get(a, "frameDelays"));
        assertEquals(-1, a.loopBackFrames); assertNull(a.interleavedTransformIndices); assertFalse(a.extendByYaw);
        assertEquals(5, a.priority); assertEquals(99, a.maxLoops); assertEquals(2, a.replayMode);
        assertEquals(0, a.movingPrecedence); assertEquals(0, a.idlePrecedence);          // no mask -> 0
        assertArrayEquals(new int[]{0x98967f}, m.interleavedTransformIndices);
        assertEquals(2, m.movingPrecedence); assertEquals(2, m.idlePrecedence);          // mask -> 2
    }

    @Test public void seqExplicitPrecedenceIsKeptEvenWithMask() throws Exception {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        u16(o, 1); u8(o, 3); u8(o, 0); u8(o, 9); u8(o, 1); u8(o, 10); u8(o, 0); u8(o, 0);
        unpackSeq(o.toByteArray());
        assertEquals(1, Animation.anims[0].movingPrecedence); assertEquals(0, Animation.anims[0].idlePrecedence);
    }

    // ---------------- walking precedence consumer (movingPrecedence vs idlePrecedence) ----------------
    private static boolean blocked(int moving, int precedenceWhileMoving, int precedenceWhenIdle) throws Exception {
        Animation a = newSeq(); a.movingPrecedence = precedenceWhileMoving; a.idlePrecedence = precedenceWhenIdle;
        Animation.anims = new Animation[]{a};
        Game game = (Game) unsafe().allocateInstance(Game.class);
        Entity e = new Entity(); e.anim = 0; e.animationDelay = 0; e.smallXYIndex = 1; e.anInt1542 = moving; e.x = 0; e.y = 0;
        e.tileSize = 1; e.smallX[0] = 100; e.smallY[0] = 100;
        game.advanceEntityAlongWalkingPath(e);
        boolean teleported = e.x != 0;
        assertEquals(!teleported, e.deferredMovementTicks == 1);
        return !teleported;
    }
    @Test public void walkingPausesOnlyWhenTheMatchingPrecedenceIsZero() throws Exception {
        assertTrue(blocked(1, 0, 2));    // still has queued movement, 363 == 0 -> paused
        assertFalse(blocked(1, 2, 0));   // 364 irrelevant while moving
        assertFalse(blocked(1, 1, 2));   // 363 == 1 does not pause
        assertTrue(blocked(0, 2, 0));    // no queued movement, 364 == 0 -> paused
        assertFalse(blocked(0, 0, 2));   // 363 irrelevant when idle
    }

    // ---------------- SpotAnim decoder ----------------
    @Test public void spotAnimRecordFillsEveryFieldFromItsOpcode() throws Exception {
        Animation.anims = new Animation[]{null, null, null, newSeq()};
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        u16(o, 3);
        u8(o, 1); u16(o, 777);
        u8(o, 2); u16(o, 3);
        u8(o, 4); u16(o, 64);
        u8(o, 5); u16(o, 96);
        u8(o, 6); u16(o, 180);
        u8(o, 7); u8(o, 5);
        u8(o, 8); u8(o, 9);
        u8(o, 40); u16(o, 1000); u8(o, 45); u16(o, 1005);
        u8(o, 50); u16(o, 2000); u8(o, 55); u16(o, 2005);
        u8(o, 0);
        u8(o, 0);
        u8(o, 0);
        unpackSpot(o.toByteArray());
        SpotAnim s = SpotAnim.cache[0], d = SpotAnim.cache[2];
        assertEquals(777, get(s, "modelId")); assertEquals(3, get(s, "animationId")); assertSame(Animation.anims[3], s.animation);
        assertEquals(64, s.horizontalScale128); assertEquals(96, s.verticalScale128); assertEquals(180, s.rotationDegrees);
        assertEquals(5, s.ambientLightOffset); assertEquals(9, s.lightDivisorOffset);
        assertArrayEquals(new int[]{1000, 0, 0, 0, 0, 1005}, (int[]) get(s, "recolorSources"));
        assertArrayEquals(new int[]{2000, 0, 0, 0, 0, 2005}, (int[]) get(s, "recolorReplacements"));
        assertEquals(0, get(SpotAnim.cache[0], "recordIndex")); assertEquals(1, get(SpotAnim.cache[1], "recordIndex")); assertEquals(2, get(d, "recordIndex"));
        assertEquals(128, d.horizontalScale128); assertEquals(128, d.verticalScale128); assertEquals(0, d.rotationDegrees);
        assertEquals(-1, get(d, "animationId")); assertNull(d.animation);
    }

    // ---------------- SpotAnim.getModel ----------------
    private void loadModelWithFaceColor123() throws Exception {
        int[] v = {255, 128, 0, 127, 255}, f = {0, 255, 128, 127, 0};
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(); java.io.DataOutputStream out = new java.io.DataOutputStream(bytes);
        for (int i = 0; i < 10; i++) out.writeByte(0);
        for (int id : f) out.writeByte(id); for (int id : v) out.writeByte(id);
        for (int i = 0; i < 5; i++) out.writeShort(123);
        out.writeShort(5); out.writeShort(5); out.writeByte(0); out.writeByte(0); out.writeByte(0);
        out.writeByte(0); out.writeByte(1); out.writeByte(1);
        for (int i = 0; i < 4; i++) out.writeShort(0);
        Model.initModelHeaders(4, new OnDemandFetcherParent() { @Override public void requestModel(int i) { requests.add(i); } });
        Model.loadModelHeader(bytes.toByteArray(), 1);
    }
    private static SpotAnim spotWith(int id, int modelId, int[] from, int[] to) throws Exception {
        Constructor<SpotAnim> c = SpotAnim.class.getDeclaredConstructor(); c.setAccessible(true);
        SpotAnim s = c.newInstance();
        set(s, "recordIndex", id); set(s, "modelId", modelId);
        System.arraycopy(from, 0, (int[]) get(s, "recolorSources"), 0, from.length);
        System.arraycopy(to, 0, (int[]) get(s, "recolorReplacements"), 0, to.length);
        return s;
    }
    @Test public void getModelLoadsTheModelIdRecolorsSourceToReplacementAndCachesByRecordIndex() throws Exception {
        loadModelWithFaceColor123();
        SpotAnim s = spotWith(7, 1, new int[]{123, 5}, new int[]{456, 6});
        Model m = s.getModel();
        assertNotNull(m);
        assertArrayEquals(new int[]{456, 456, 456, 456, 456}, m.faceColors);
        assertSame(m, s.getModel());                                  // cached under recordIndex
        assertSame(m, spotWith(7, 2, new int[0], new int[0]).getModel()); // same index -> same entry, model id ignored on hit
        Model other = spotWith(8, 1, new int[]{999}, new int[]{1}).getModel();
        assertNotSame(m, other);
        assertArrayEquals(new int[]{123, 123, 123, 123, 123}, other.faceColors);   // 999 does not match: untouched
        Model swapped = spotWith(9, 1, new int[]{456}, new int[]{123}).getModel();
        assertArrayEquals(new int[]{123, 123, 123, 123, 123}, swapped.faceColors);  // 408 is matched, 409 written
        assertNull(spotWith(10, 3, new int[0], new int[0]).getModel());
        assertEquals(java.util.Collections.singletonList(3), requests);             // modelId is the model id requested
    }

    // ---------------- SpotAnim display fields through StationaryGraphic.getRotatedModel ----------------
    private Model triangleModel() throws Exception {
        loadModelWithFaceColor123();
        Model m = Model.getModel(1);
        m.vertexX = new int[]{0, 100, 0, 0, 0}; m.vertexY = new int[]{0, 0, 50, 0, 0}; m.vertexZ = new int[]{0, 20, 0, 0, 0};
        m.faceVertexA = new int[]{0, 0, 0, 0, 0}; m.faceVertexB = new int[]{1, 0, 0, 0, 0}; m.faceVertexC = new int[]{2, 0, 0, 0, 0};
        return m;
    }
    private Model shown(Model base, int id, int s410, int s411, int s412, int s413, int s414) throws Exception {
        SpotAnim s = spotWith(id, 1, new int[0], new int[0]);
        s.horizontalScale128 = s410; s.verticalScale128 = s411; s.rotationDegrees = s412; s.ambientLightOffset = s413; s.lightDivisorOffset = s414;
        Animation a = newSeq(); a.frameIds = new int[]{-1}; a.frameCount = 1; s.animation = a;
        SpotAnim.modelCache.removeFromCache(base, id);
        SpotAnim.cache = new SpotAnim[id + 1]; SpotAnim.cache[id] = s;
        StationaryGraphic g = new StationaryGraphic(0, 0, 0, id, 0, 0, 0);
        set(g, "finished", Boolean.TRUE);
        return g.getRotatedModel();
    }
    private static int[] shade(Model m) throws Exception { return (int[]) get(m, "faceShadeA"); }
    private static Model reference(Model base, int ambient, int divisor) {
        Model r = new Model(true, false, false, base);
        r.applyLighting(ambient, divisor, -30, -50, -30, true);
        return r;
    }
    @Test public void spotAnimScaleRotationAndLightingFieldsDriveTheDisplayedModel() throws Exception {
        Model base = triangleModel();
        Model plain = shown(base, 1, 128, 128, 0, 0, 0);
        assertArrayEquals(new int[]{0, 100, 0, 0, 0}, plain.vertexX);
        // 410 scales X and Z, 411 scales Y
        Model scaled = shown(base, 2, 64, 256, 0, 0, 0);
        assertArrayEquals(new int[]{0, 50, 0, 0, 0}, scaled.vertexX);
        assertArrayEquals(new int[]{0, 10, 0, 0, 0}, scaled.vertexZ);
        assertArrayEquals(new int[]{0, 0, 100, 0, 0}, scaled.vertexY);
        // 412 rotates by that many degrees about Y; other values do nothing
        assertArrayEquals(new int[]{0, 20, 0, 0, 0}, shown(base, 3, 128, 128, 90, 0, 0).vertexX);
        assertArrayEquals(new int[]{0, -100, 0, 0, 0}, shown(base, 4, 128, 128, 180, 0, 0).vertexX);
        assertArrayEquals(new int[]{0, -20, 0, 0, 0}, shown(base, 5, 128, 128, 270, 0, 0).vertexX);
        assertArrayEquals(new int[]{0, 100, 0, 0, 0}, shown(base, 6, 128, 128, 45, 0, 0).vertexX);
        // 413 is added to ambient light 64, 414 to the light divisor scale 850
        Model ambient = shown(base, 7, 128, 128, 0, 40, 0), divisor = shown(base, 8, 128, 128, 0, 0, 200);
        assertArrayEquals(shade(reference(base, 104, 850)), shade(ambient));
        assertArrayEquals(shade(reference(base, 64, 1050)), shade(divisor));
        assertFalse(java.util.Arrays.equals(shade(reference(base, 104, 850)), shade(reference(base, 64, 850))));
        assertFalse(java.util.Arrays.equals(shade(reference(base, 64, 1050)), shade(reference(base, 64, 850))));
        assertFalse(java.util.Arrays.equals(shade(reference(base, 104, 850)), shade(reference(base, 64, 1050))));
    }
}
