import static org.junit.Assert.*;

import java.lang.reflect.Field;
import org.junit.Test;

public class WorldControllerTileObjectsTest {
    private static final int OBJ_KIND = 2 << 29;

    private static WorldController scene() throws Exception {
        Field f = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        sun.misc.Unsafe unsafe = (sun.misc.Unsafe) f.get(null);
        WorldController wc = (WorldController) unsafe.allocateInstance(WorldController.class);
        Field g = WorldController.class.getDeclaredField("groundArray");
        g.setAccessible(true);
        g.set(wc, new Ground[4][104][104]);
        return wc;
    }

    private static Ground[][][] tiles(WorldController wc) throws Exception {
        Field g = WorldController.class.getDeclaredField("groundArray");
        g.setAccessible(true);
        return (Ground[][][]) g.get(wc);
    }

    private static Ground filled(Ground[][][] t, int plane, int x, int y, int base) {
        Ground g = new Ground(plane, x, y);
        g.obj1 = new Object1();
        g.obj1.uid = base + 1;
        g.obj1.info = (byte) 0x81;
        g.obj2 = new Object2();
        g.obj2.uid = base + 2;
        g.obj2.info = (byte) 0x82;
        g.obj3 = new Object3();
        g.obj3.uid = base + 3;
        g.obj3.info = (byte) 0x83;
        g.obj4 = new Object4();
        t[plane][x][y] = g;
        return g;
    }

    private static Object5 object5(int plane, int x0, int x1, int y0, int y1, int uid, int sz) {
        Object5 o = new Object5();
        o.plane = plane;
        o.minTileX = x0;
        o.maxTileX = x1;
        o.minTileY = y0;
        o.maxTileY = y1;
        o.uid = uid;
        o.info = (byte) sz;
        return o;
    }

    private static void attach(Ground[][][] t, Object5 o, int flags) {
        for (int x = o.minTileX; x <= o.maxTileX; x++)
            for (int y = o.minTileY; y <= o.maxTileY; y++) {
                Ground g = t[o.plane][x][y];
                if (g == null) g = t[o.plane][x][y] = new Ground(o.plane, x, y);
                g.obj5Array[g.objectCount] = o;
                g.objectEdgeMasks[g.objectCount++] = flags;
                g.objectEdgeMask |= flags;
            }
    }

    /** Fully populated tiles with swapped axes / other plane / neighbouring row, to catch argument-order mistakes. */
    private static Ground[] decoys(Ground[][][] t) {
        return new Ground[] { filled(t, 2, 11, 10, 100), filled(t, 1, 10, 11, 200), filled(t, 2, 10, 12, 300), filled(t, 3, 11, 10, 400) };
    }

    private static void assertSlots(Ground g, boolean o1, boolean o2, boolean o3, boolean o4) {
        assertEquals(o1, g.obj1 != null);
        assertEquals(o2, g.obj2 != null);
        assertEquals(o3, g.obj3 != null);
        assertEquals(o4, g.obj4 != null);
    }

    private interface Remover { void run(WorldController wc); }

    private void checkRemover(Remover r, boolean o1, boolean o2, boolean o3, boolean o4) throws Exception {
        WorldController wc = scene();
        Ground[][][] t = tiles(wc);
        Ground target = filled(t, 2, 10, 11, 0);
        Ground[] others = decoys(t);
        r.run(wc);
        assertSlots(target, o1, o2, o3, o4);
        for (Ground g : others) assertSlots(g, true, true, true, true);
        r.run(scene()); // missing tile must not throw
    }

    @Test public void slotRemoversClearOnlyTheirSlotOnTheAddressedTile() throws Exception {
        checkRemover(new Remover() { public void run(WorldController wc) { wc.removeWall(10, 2, 11, (byte) -119); } }, false, true, true, true);
        checkRemover(new Remover() { public void run(WorldController wc) { wc.removeWallDecoration(11, 2, 10); } }, true, false, true, true);
        checkRemover(new Remover() { public void run(WorldController wc) { wc.removeGroundDecoration(2, 11, 10); } }, true, true, false, true);
        checkRemover(new Remover() { public void run(WorldController wc) { wc.removeGroundItemStack(2, 10, 11); } }, true, true, true, false);
    }

    @Test public void method291GuardByteOnlyTogglesDummyFlag() throws Exception {
        WorldController wc = scene();
        Ground[][][] t = tiles(wc);
        Ground target = filled(t, 2, 10, 11, 0);
        Field flag = WorldController.class.getDeclaredField("aBoolean434");
        flag.setAccessible(true);
        flag.setBoolean(wc, false);
        wc.removeWall(10, 2, 11, (byte) -119);
        assertFalse(flag.getBoolean(wc));
        wc.removeWall(10, 2, 11, (byte) 5);
        assertTrue(flag.getBoolean(wc));
        assertNull(target.obj1);
    }

    @Test public void slotGettersReturnTheAddressedTilesObjects() throws Exception {
        WorldController wc = scene();
        Ground[][][] t = tiles(wc);
        Ground target = filled(t, 2, 10, 11, 0);
        decoys(t);
        assertSame(target.obj1, wc.getWall(2, 10, 11));
        assertSame(target.obj2, wc.getWallDecoration(10, 11, 2));
        assertSame(target.obj3, wc.getGroundDecoration(11, 10, 2));
        assertEquals(1, wc.getWallUid(2, 10, 11));
        assertEquals(2, wc.getWallDecorationUid(2, 10, 11));
        assertEquals(3, wc.getGroundDecorationUid(2, 10, 11));
        assertEquals(101, wc.getWallUid(2, 11, 10));
        assertEquals(203, wc.getGroundDecorationUid(1, 10, 11));
        assertNull(wc.getWall(0, 5, 5));
        assertNull(wc.getWallDecoration(5, 5, 0));
        assertNull(wc.getSceneObject(5, 5, 0));
        assertNull(wc.getGroundDecoration(5, 5, 0));
        assertEquals(0, wc.getWallUid(0, 5, 5));
        assertEquals(0, wc.getWallDecorationUid(0, 5, 5));
        assertEquals(0, wc.getSceneObjectUid(0, 5, 5));
        assertEquals(0, wc.getGroundDecorationUid(0, 5, 5));
        target.obj1 = null;
        target.obj2 = null;
        target.obj3 = null;
        assertNull(wc.getWall(2, 10, 11));
        assertNull(wc.getWallDecoration(10, 11, 2));
        assertNull(wc.getGroundDecoration(11, 10, 2));
        assertEquals(0, wc.getWallUid(2, 10, 11));
        assertEquals(0, wc.getWallDecorationUid(2, 10, 11));
        assertEquals(0, wc.getGroundDecorationUid(2, 10, 11));
    }

    @Test public void sceneObjectLookupNeedsObjectKindAndAnchorTile() throws Exception {
        WorldController wc = scene();
        Ground[][][] t = tiles(wc);
        Object5 big = object5(2, 10, 11, 11, 12, OBJ_KIND | 7, 0x44);
        Object5 npcKind = object5(2, 20, 20, 20, 20, (1 << 29) | 8, 0x45);
        Object5 itemKind = object5(2, 21, 21, 21, 21, (3 << 29) | 9, 0x46);
        attach(t, big, 4);
        attach(t, npcKind, 8);
        attach(t, itemKind, 16);
        assertSame(big, wc.getSceneObject(10, 11, 2));
        assertEquals(OBJ_KIND | 7, wc.getSceneObjectUid(2, 10, 11));
        assertNull(wc.getSceneObject(11, 11, 2));
        assertNull(wc.getSceneObject(10, 12, 2));
        assertEquals(0, wc.getSceneObjectUid(2, 11, 12));
        assertNull(wc.getSceneObject(10, 11, 1));
        assertNull(wc.getSceneObject(20, 20, 2));
        assertEquals(0, wc.getSceneObjectUid(2, 20, 20));
        assertNull(wc.getSceneObject(21, 21, 2));
        Object5 hi = object5(2, 30, 30, 30, 30, 0x80000000 | OBJ_KIND | 0x12345, 0);
        attach(t, hi, 1);
        assertSame(hi, wc.getSceneObject(30, 30, 2));
    }

    @Test public void method293DetachesAnchoredSceneObjectFromEveryCoveredTile() throws Exception {
        WorldController wc = scene();
        Ground[][][] t = tiles(wc);
        Object5 a = object5(2, 10, 10, 11, 11, (1 << 29) | 1, 1);
        Object5 big = object5(2, 10, 11, 11, 12, OBJ_KIND | 7, 2);
        Object5 b = object5(2, 10, 10, 11, 11, (3 << 29) | 3, 3);
        Object5 other = object5(2, 11, 11, 12, 12, (3 << 29) | 4, 4);
        Object5 near = object5(2, 11, 11, 11, 11, OBJ_KIND | 9, 5);
        attach(t, a, 1);
        attach(t, big, 2);
        attach(t, b, 4);
        attach(t, other, 8);
        attach(t, near, 16);
        assertEquals(1 | 2 | 4, t[2][10][11].objectEdgeMask);
        wc.removeSceneObject(2, 10, 12); // covered by big but not its anchor: nothing happens
        assertEquals(3, t[2][10][11].objectCount);
        assertEquals(1, t[2][10][12].objectCount);
        wc.removeSceneObject(2, 10, 11);
        Ground g = t[2][10][11];
        assertEquals(2, g.objectCount);
        assertSame(a, g.obj5Array[0]);
        assertSame(b, g.obj5Array[1]);
        assertNull(g.obj5Array[2]);
        assertEquals(1, g.objectEdgeMasks[0]);
        assertEquals(4, g.objectEdgeMasks[1]);
        assertEquals(1 | 4, g.objectEdgeMask);
        assertEquals(0, t[2][10][12].objectCount);
        assertEquals(0, t[2][10][12].objectEdgeMask);
        assertEquals(1, t[2][11][11].objectCount);
        assertSame(near, t[2][11][11].obj5Array[0]);
        assertEquals(16, t[2][11][11].objectEdgeMask);
        assertEquals(1, t[2][11][12].objectCount);
        assertSame(other, t[2][11][12].obj5Array[0]);
        assertEquals(8, t[2][11][12].objectEdgeMask);
    }

    @Test public void method304ReturnsMaskedInfoByUidWithSlotPriority() throws Exception {
        WorldController wc = scene();
        Ground[][][] t = tiles(wc);
        Ground g = filled(t, 2, 10, 11, 0);
        decoys(t);
        Object5 o = object5(2, 10, 10, 11, 11, OBJ_KIND | 55, 0xC5);
        attach(t, o, 1);
        assertEquals(0x81, wc.getObjectInfo(2, 10, 11, 1));
        assertEquals(0x82, wc.getObjectInfo(2, 10, 11, 2));
        assertEquals(0x83, wc.getObjectInfo(2, 10, 11, 3));
        assertEquals(0xC5, wc.getObjectInfo(2, 10, 11, OBJ_KIND | 55));
        assertEquals(-1, wc.getObjectInfo(2, 10, 11, 99));
        assertEquals(-1, wc.getObjectInfo(2, 5, 5, 1));
        assertEquals(0x81, wc.getObjectInfo(2, 11, 10, 101));
        assertEquals(-1, wc.getObjectInfo(2, 11, 10, 1));
        g.obj2.uid = 1;
        assertEquals(0x81, wc.getObjectInfo(2, 10, 11, 1));
        g.obj1 = null;
        assertEquals(0x82, wc.getObjectInfo(2, 10, 11, 1));
    }

    @Test public void method290ScalesWallDecorationOffsetAboutTileCentreInSixteenths() throws Exception {
        for (int[] c : new int[][] { { 16, 1408, 1440 }, { 8, 1376, 1456 }, { 0, 1344, 1472 }, { 32, 1472, 1408 }, { 4, 1360, 1464 } }) {
            WorldController wc = scene();
            Ground[][][] t = tiles(wc);
            Ground g = filled(t, 2, 10, 11, 0);
            Ground other = filled(t, 2, 11, 10, 100);
            g.obj2.worldX = 1344 + 64;
            g.obj2.worldY = 1472 - 32;
            other.obj2.worldX = 1344 + 64;
            other.obj2.worldY = 1472 - 32;
            wc.scaleWallDecorationOffset(11, c[0], 10, 2);
            assertEquals(c[1], g.obj2.worldX);
            assertEquals(c[2], g.obj2.worldY);
            assertEquals(1408, other.obj2.worldX);
            assertEquals(1440, other.obj2.worldY);
        }
        WorldController wc = scene();
        Ground[][][] t = tiles(wc);
        Ground g = filled(t, 0, 3, 4, 0);
        g.obj2.worldX = 3 * 128 + 64 - 33;
        g.obj2.worldY = 4 * 128 + 64 + 33;
        wc.scaleWallDecorationOffset(4, 8, 3, 0);
        assertEquals(3 * 128 + 64 - 16, g.obj2.worldX);
        assertEquals(4 * 128 + 64 + 16, g.obj2.worldY);
        wc.scaleWallDecorationOffset(9, 8, 9, 0);
        g.obj2 = null;
        wc.scaleWallDecorationOffset(4, 8, 3, 0);
    }
}
