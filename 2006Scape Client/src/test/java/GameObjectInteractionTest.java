import java.lang.reflect.Field;
import java.lang.reflect.Constructor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class GameObjectInteractionTest {
    private Player savedPlayer;
    private ObjectDef[] savedDefinitions;

    public static class CapturingGame extends Game {
        int[] walk;
        int calls;

        @Override public boolean doWalkTo(int i, int j, int k, int i1, int j1,
                int k1, int l1, int i2, int j2, boolean flag, int k2) {
            calls++;
            walk = new int[]{i, j, k, i1, j1, k1, l1, i2, j2, flag ? 1 : 0, k2};
            return false;
        }
    }

    private static Field definitionCache() throws Exception {
        Field field = ObjectDef.class.getDeclaredField("cache");
        field.setAccessible(true);
        return field;
    }

    @Before public void setUp() throws Exception {
        savedPlayer = Game.myPlayer;
        savedDefinitions = (ObjectDef[]) definitionCache().get(null);
        ObjectDef[] definitions = new ObjectDef[20];
        Constructor<ObjectDef> constructor = ObjectDef.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        for (int n = 0; n < definitions.length; n++) {
            definitions[n] = constructor.newInstance();
            definitions[n].type = -1;
        }
        definitions[0].type = 123;
        definitions[0].sizeX = 2;
        definitions[0].sizeY = 3;
        definitions[0].blockedSidesMask = 11;
        definitions[1].type = 124;
        definitions[1].sizeX = 4;
        definitions[1].sizeY = 5;
        definitions[1].blockedSidesMask = 3;
        definitionCache().set(null, definitions);
        Game.myPlayer = new Player();
        Game.myPlayer.smallX[0] = 41;
        Game.myPlayer.smallY[0] = 42;
    }

    @After public void tearDown() throws Exception {
        Game.myPlayer = savedPlayer;
        definitionCache().set(null, savedDefinitions);
    }

    private CapturingGame game() throws Exception {
        Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        CapturingGame game = (CapturingGame) ((sun.misc.Unsafe) field.get(null))
                .allocateInstance(CapturingGame.class);
        game.worldController = new WorldController(new int[4][105][105]);
        game.plane = 1;
        game.saveClickX = 310;
        game.saveClickY = 220;
        return game;
    }

    private static void put(CapturingGame game, int plane, int x, int y, int uid, int shape, int rotation)
            throws Exception {
        Field field = WorldController.class.getDeclaredField("groundArray");
        field.setAccessible(true);
        Ground[][][] grounds = (Ground[][][]) field.get(game.worldController);
        Ground ground = new Ground(plane, x, y);
        Object1 object = new Object1();
        object.uid = uid;
        object.info = (byte) (shape | rotation << 6);
        ground.obj1 = object;
        grounds[plane][x][y] = ground;
    }

    @Test public void method66RequiresMatchingSceneObjectAtPlaneAndTile() throws Exception {
        CapturingGame game = game();
        int uid = 123 << 14;
        assertFalse(game.walkToSceneObject(uid, 53, 54));
        assertEquals(0, game.calls);
        assertEquals(0, game.crossType);
        put(game, 0, 54, 53, uid, 9, 2);
        assertFalse(game.walkToSceneObject(uid, 53, 54));
        put(game, 1, 54, 53, uid + 1, 9, 2);
        assertFalse(game.walkToSceneObject(uid, 53, 54));
        put(game, 1, 54, 53, uid, 9, 2);
        assertFalse(game.walkToSceneObject(uid, 54, 53));
        assertTrue(game.walkToSceneObject(uid, 53, 54));
        assertArrayEquals(new int[]{2, 2, 0, 10, 42, 0, 0, 53, 41, 0, 54}, game.walk);
        assertEquals(1, game.calls);
        assertEquals(310, game.crossX);
        assertEquals(220, game.crossY);
        assertEquals(2, game.crossType);
        assertEquals(0, game.crossIndex);
    }

    @Test public void method66UsesShapeOrientationDimensionsAndAccessMask() throws Exception {
        CapturingGame game = game();
        int uid = 123 << 14;
        put(game, 1, 54, 53, uid, 10, 0);
        assertTrue(game.walkToSceneObject(uid, 53, 54));
        assertArrayEquals(new int[]{2, 0, 3, 0, 42, 2, 11, 53, 41, 0, 54}, game.walk);
        put(game, 1, 54, 53, 124 << 14, 10, 0);
        assertTrue(game.walkToSceneObject(124 << 14, 53, 54));
        assertArrayEquals(new int[]{2, 0, 5, 0, 42, 4, 3, 53, 41, 0, 54}, game.walk);
        put(game, 1, 54, 53, uid, 10, 1);
        assertTrue(game.walkToSceneObject(uid, 53, 54));
        assertArrayEquals(new int[]{2, 0, 2, 0, 42, 3, 7, 53, 41, 0, 54}, game.walk);
        put(game, 1, 54, 53, uid, 11, 3);
        assertTrue(game.walkToSceneObject(uid, 53, 54));
        assertArrayEquals(new int[]{2, 0, 2, 0, 42, 3, 13, 53, 41, 0, 54}, game.walk);
        put(game, 1, 54, 53, uid, 22, 1);
        assertTrue(game.walkToSceneObject(uid, 53, 54));
        assertArrayEquals(new int[]{2, 0, 2, 0, 42, 3, 7, 53, 41, 0, 54}, game.walk);
        put(game, 1, 54, 53, uid, 9, 1);
        assertTrue(game.walkToSceneObject(uid, 53, 54));
        assertArrayEquals(new int[]{2, 1, 0, 10, 42, 0, 0, 53, 41, 0, 54}, game.walk);
    }
}

