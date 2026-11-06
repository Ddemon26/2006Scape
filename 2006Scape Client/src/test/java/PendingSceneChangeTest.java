import java.lang.reflect.Field;
import org.junit.Test;
import static org.junit.Assert.*;

public class PendingSceneChangeTest {
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
        game.aClass19_1179 = new NodeList();
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
        Class30_Sub1 entry = (Class30_Sub1) game.aClass19_1179.firstFromTail();
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
        assertSame(entry, game.aClass19_1179.firstFromTail());
        assertEquals(88, entry.requestedObjectId);
        assertEquals(5, entry.restoreDelayTicks);
        assertEquals(2, entry.placementDelayTicks);
        game.method130(5, -1, 1, 1, 11, 8, 1, 10, 2);
        Class30_Sub1 other = (Class30_Sub1) game.aClass19_1179.firstFromHead();
        assertNotSame(entry, other);
        assertEquals(1, other.objectCategory);
        assertEquals(-1, other.originalObjectId);
    }

    @Test public void method115CountsDownOnlyInLoadedSceneAndUnlinksAfterRestoration() throws Exception {
        Game game = game();
        game.method130(3, -2, 0, 0, 11, 0, 1, 10, 2);
        Class30_Sub1 entry = (Class30_Sub1) game.aClass19_1179.firstFromTail();
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
        assertSame(entry, game.aClass19_1179.firstFromTail());
        game.advancePendingSceneChanges();
        assertNull(game.aClass19_1179.firstFromTail());
    }

    @Test public void method115RejectsPlacementAtSceneBoundary() throws Exception {
        Game game = game();
        game.method130(2, -1, 0, 0, 11, 0, 1, 0, 1);
        Class30_Sub1 entry = (Class30_Sub1) game.aClass19_1179.firstFromTail();
        game.loadingStage = 2;
        game.advancePendingSceneChanges();
        assertEquals(1, entry.restoreDelayTicks);
        assertEquals(0, entry.placementDelayTicks);
        game.advancePendingSceneChanges();
        assertNull(game.aClass19_1179.firstFromTail());
    }
}
