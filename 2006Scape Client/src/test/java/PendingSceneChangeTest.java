import java.lang.reflect.Field;
import org.junit.Test;
import static org.junit.Assert.*;

public class PendingSceneChangeTest {
    @Test public void method142PlacesByTilePlaneShapeOrientationAndId() throws Exception {
        Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        sun.misc.Unsafe unsafe = (sun.misc.Unsafe) unsafeField.get(null);
        Field cacheField = ObjectDef.class.getDeclaredField("cache");
        cacheField.setAccessible(true);
        Object oldCache = cacheField.get(null);
        boolean oldLowMem = Game.lowMem;
        try {
            ObjectDef[] cache = new ObjectDef[20];
            for (int n = 0; n < cache.length; n++) {
                cache[n] = (ObjectDef) unsafe.allocateInstance(ObjectDef.class);
                cache[n].type = -1;
            }
            cache[0].type = 401;
            cache[0].childrenIDs = new int[0];
            cache[0].anInt781 = -1;
            cache[1].type = 402;
            cache[1].aBoolean767 = true;
            cacheField.set(null, cache);
            Game.lowMem = false;
            Game game = game();
            game.plane = 1;
            game.intGroundArray = new int[4][105][105];
            game.byteGroundArray = new byte[4][104][104];
            game.aClass11Array1230 = new CollisionMap[4];
            for (int n = 0; n < 4; n++) game.aClass11Array1230[n] = new CollisionMap();
            for (int x = 0; x < 105; x++) for (int y = 0; y < 105; y++) {
                game.intGroundArray[1][x][y] = 100;
                game.intGroundArray[2][x][y] = 200;
            }
            game.byteGroundArray[1][10][11] = 2;
            game.applySceneObjectChange(11, 1, 2, 22, 10, 3, 401);
            Field ground = WorldController.class.getDeclaredField("groundArray");
            ground.setAccessible(true);
            Ground[][][] tiles = (Ground[][][]) ground.get(game.worldController);
            assertNotNull(tiles[1][10][11].obj3);
            assertEquals(401, tiles[1][10][11].obj3.uid >> 14 & 0x7fff);
            assertEquals((2 << 6) | 22, tiles[1][10][11].obj3.aByte816 & 0xff);
            assertEquals(200, tiles[1][10][11].obj3.anInt811);
            assertNull(tiles[2][10][11]);
            game.applySceneObjectChange(12, 1, 1, 22, 10, 3, 401);
            assertEquals(100, tiles[1][10][12].obj3.anInt811);
            game.applySceneObjectChange(11, 1, 0, 22, 0, 3, 401);
            assertNull(tiles[1][0][11]);

            tiles[1][10][11].obj1 = new Object1();
            tiles[1][10][11].obj1.uid = 402 << 14;
            tiles[1][10][11].obj1.aByte281 = (byte) (2 << 6);
            game.aClass11Array1230[1].method211(11, 2, 10, 0, false);
            assertTrue((game.aClass11Array1230[1].anIntArrayArray294[10][11] & 8) != 0);
            game.applySceneObjectChange(11, 1, 0, 0, 10, 0, -1);
            assertNull(tiles[1][10][11].obj1);
            assertNotNull(tiles[1][10][11].obj3);
            assertEquals(0, game.aClass11Array1230[1].anIntArrayArray294[10][11] & 8);
            game.applySceneObjectChange(11, 1, 0, 0, 10, 3, -1);
            assertNull(tiles[1][10][11].obj3);
        } finally {
            cacheField.set(null, oldCache);
            Game.lowMem = oldLowMem;
        }
    }
    private static Game game() throws Exception {
        Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        sun.misc.Unsafe unsafe = (sun.misc.Unsafe) field.get(null);
        Game game = (Game) unsafe.allocateInstance(Game.class);
        WorldController scene = (WorldController) unsafe.allocateInstance(WorldController.class);
        Field ground = WorldController.class.getDeclaredField("groundArray");
        ground.setAccessible(true);
        ground.set(scene, new Ground[4][104][104]);
        game.worldController = scene;
        game.pendingSceneChanges = new NodeList();
        return game;
    }

    @Test public void method130StoresDistinctInputsAndMethod89ReadsSelectedSceneObject() throws Exception {
        Game game = game();
        Field ground = WorldController.class.getDeclaredField("groundArray");
        ground.setAccessible(true);
        Ground[][][] tiles = (Ground[][][]) ground.get(game.worldController);
        tiles[1][10][11] = new Ground(1, 10, 11);
        Object1 existing = new Object1();
        existing.uid = 123 << 14;
        existing.aByte281 = (byte) ((3 << 6) | 10);
        tiles[1][10][11].obj1 = existing;

        game.method130(3, 77, 2, 0, 11, 9, 1, 10, 4);
        Class30_Sub1 entry = (Class30_Sub1) game.pendingSceneChanges.firstFromTail();
        assertNotNull(entry);
        assertEquals(77, entry.requestedObjectId);
        assertEquals(2, entry.requestedOrientation);
        assertEquals(9, entry.requestedShape);
        assertEquals(3, entry.restoreDelayTicks);
        assertEquals(1, entry.scenePlane);
        assertEquals(0, entry.objectCategory);
        assertEquals(10, entry.tileX);
        assertEquals(11, entry.tileY);
        assertEquals(123, entry.originalObjectId);
        assertEquals(3, entry.originalOrientation);
        assertEquals(10, entry.originalShape);
        assertEquals(4, entry.placementDelayTicks);

        game.method130(5, 88, 1, 0, 11, 8, 1, 10, 2);
        assertSame(entry, game.pendingSceneChanges.firstFromTail());
        assertEquals(88, entry.requestedObjectId);
        assertEquals(5, entry.restoreDelayTicks);
        assertEquals(2, entry.placementDelayTicks);
        game.method130(5, -1, 1, 1, 11, 8, 1, 10, 2);
        Class30_Sub1 other = (Class30_Sub1) game.pendingSceneChanges.firstFromHead();
        assertNotSame(entry, other);
        assertEquals(1, other.objectCategory);
        assertEquals(-1, other.originalObjectId);
    }

    @Test public void method115CountsDownOnlyInLoadedSceneAndUnlinksAfterRestoration() throws Exception {
        Game game = game();
        game.method130(3, -2, 0, 0, 11, 0, 1, 10, 2);
        Class30_Sub1 entry = (Class30_Sub1) game.pendingSceneChanges.firstFromTail();
        game.loadingStage = 1;
        game.advancePendingSceneChanges();
        assertEquals(3, entry.restoreDelayTicks);
        assertEquals(2, entry.placementDelayTicks);
        game.loadingStage = 2;
        game.advancePendingSceneChanges();
        assertEquals(2, entry.restoreDelayTicks);
        assertEquals(1, entry.placementDelayTicks);
        game.advancePendingSceneChanges();
        assertEquals(1, entry.restoreDelayTicks);
        assertEquals(-1, entry.placementDelayTicks);
        assertSame(entry, game.pendingSceneChanges.firstFromTail());
        game.advancePendingSceneChanges();
        assertNull(game.pendingSceneChanges.firstFromTail());
    }

    @Test public void method115RejectsPlacementAtSceneBoundary() throws Exception {
        Game game = game();
        game.method130(2, -1, 0, 0, 11, 0, 1, 0, 1);
        Class30_Sub1 entry = (Class30_Sub1) game.pendingSceneChanges.firstFromTail();
        game.loadingStage = 2;
        game.advancePendingSceneChanges();
        assertEquals(1, entry.restoreDelayTicks);
        assertEquals(0, entry.placementDelayTicks);
        game.advancePendingSceneChanges();
        assertNull(game.pendingSceneChanges.firstFromTail());
    }

    @Test public void method89CopiesOnlySelectedSceneCategoryAndPackedFields() throws Exception {
        Game game = game();
        Field ground = WorldController.class.getDeclaredField("groundArray");
        ground.setAccessible(true);
        Ground[][][] tiles = (Ground[][][]) ground.get(game.worldController);
        tiles[2][17][23] = new Ground(2, 17, 23);
        Ground tile = tiles[2][17][23];
        tile.obj1 = new Object1();
        tile.obj1.uid = 101 << 14;
        tile.obj1.aByte281 = (byte) ((1 << 6) | 3);
        tile.obj2 = new Object2();
        tile.obj2.uid = 202 << 14;
        tile.obj2.aByte506 = (byte) ((2 << 6) | 11);
        tile.obj3 = new Object3();
        tile.obj3.uid = 303 << 14;
        tile.obj3.aByte816 = (byte) ((3 << 6) | 22);
        Object5 scenery = new Object5();
        scenery.uid = (2 << 29) | (404 << 14);
        scenery.aByte530 = (byte) 31;
        scenery.anInt523 = 17;
        scenery.anInt525 = 23;
        tile.obj5Array[0] = scenery;
        tile.anInt1317 = 1;

        int[] ids = {101, 202, 404, 303};
        int[] shapes = {3, 11, 31, 22};
        int[] orientations = {1, 2, 0, 3};
        for (int category = 0; category < 4; category++) {
            Class30_Sub1 entry = new Class30_Sub1();
            entry.scenePlane = 2;
            entry.tileX = 17;
            entry.tileY = 23;
            entry.objectCategory = category;
            entry.requestedObjectId = 999;
            game.snapshotOriginalSceneObject(entry);
            assertEquals(ids[category], entry.originalObjectId);
            assertEquals(shapes[category], entry.originalShape);
            assertEquals(orientations[category], entry.originalOrientation);
            assertEquals(999, entry.requestedObjectId);
        }

        Class30_Sub1 absent = new Class30_Sub1();
        absent.scenePlane = 2;
        absent.tileX = 17;
        absent.tileY = 24;
        absent.objectCategory = 0;
        absent.originalObjectId = 999;
        game.snapshotOriginalSceneObject(absent);
        assertEquals(-1, absent.originalObjectId);
        assertEquals(0, absent.originalShape);
        assertEquals(0, absent.originalOrientation);
        scenery.anInt525 = 24;
        Class30_Sub1 mismatched = new Class30_Sub1();
        mismatched.scenePlane = 2;
        mismatched.tileX = 17;
        mismatched.tileY = 23;
        mismatched.objectCategory = 2;
        game.snapshotOriginalSceneObject(mismatched);
        assertEquals(-1, mismatched.originalObjectId);
    }

    @Test public void method63KeepsIndefiniteEntriesAndResnapshotsScene() throws Exception {
        Game game = game();
        Field ground = WorldController.class.getDeclaredField("groundArray");
        ground.setAccessible(true);
        Ground[][][] tiles = (Ground[][][]) ground.get(game.worldController);
        tiles[1][10][11] = new Ground(1, 10, 11);
        tiles[1][10][11].obj1 = new Object1();
        tiles[1][10][11].obj1.uid = 77 << 14;
        tiles[1][10][11].obj1.aByte281 = (byte) ((2 << 6) | 8);
        Class30_Sub1 retained = new Class30_Sub1();
        retained.scenePlane = 1;
        retained.tileX = 10;
        retained.tileY = 11;
        retained.objectCategory = 0;
        retained.placementDelayTicks = 9;
        retained.originalObjectId = 12;
        Class30_Sub1 removed = new Class30_Sub1();
        removed.restoreDelayTicks = 2;
        game.pendingSceneChanges.insertHead(retained);
        game.pendingSceneChanges.insertHead(removed);
        game.resetPendingSceneChangesAfterRegionLoad();
        assertNull(removed.next);
        assertSame(retained, game.pendingSceneChanges.firstFromTail());
        assertNull(game.pendingSceneChanges.nextTowardHead());
        assertEquals(0, retained.placementDelayTicks);
        assertEquals(77, retained.originalObjectId);
        assertEquals(8, retained.originalShape);
        assertEquals(2, retained.originalOrientation);
        retained.restoreDelayTicks = 0;
        game.resetPendingSceneChangesAfterRegionLoad();
        assertNull(game.pendingSceneChanges.firstFromTail());
    }
}
