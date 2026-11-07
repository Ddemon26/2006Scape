import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class GameProjectileUpdateTest {
    private SpotAnim[] savedSpots;
    private Player savedLocalPlayer;
    private int savedCycle;

    @Before public void setUp() throws Exception {
        savedSpots = SpotAnim.cache;
        savedLocalPlayer = Game.myPlayer;
        savedCycle = Game.loopCycle;
        Constructor<SpotAnim> constructor = SpotAnim.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        SpotAnim.cache = new SpotAnim[]{constructor.newInstance()};
    }

    @After public void tearDown() {
        SpotAnim.cache = savedSpots;
        Game.myPlayer = savedLocalPlayer;
        Game.loopCycle = savedCycle;
    }

    private static Game game() throws Exception {
        Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        Game game = (Game) ((sun.misc.Unsafe) field.get(null)).allocateInstance(Game.class);
        game.projectiles = new NodeList();
        game.worldController = new WorldController(new int[4][105][105]);
        game.intGroundArray = new int[4][105][105];
        game.byteGroundArray = new byte[4][104][104];
        game.npcArray = new NPC[2];
        game.playerArray = new Player[2];
        game.plane = 0;
        game.anInt945 = 1;
        return game;
    }

    private static Projectile projectile(int start, int end, int plane, int target) {
        Projectile result = new Projectile(0, 0, start, end, 0, plane,
                0, 320, 320, target, 0);
        result.retarget(start, 320, 0, 448);
        return result;
    }

    private static Ground[][][] grounds(Game game) throws Exception {
        Field field = WorldController.class.getDeclaredField("groundArray");
        field.setAccessible(true);
        return (Ground[][][]) field.get(game.worldController);
    }

    @Test public void method55ExpiresWrongPlaneAndPastEndButWaitsForStart() throws Exception {
        Game game = game();
        Game.loopCycle = 10;
        Projectile wrongPlane = projectile(1, 20, 1, 0);
        Projectile expired = projectile(1, 9, 0, 0);
        Projectile future = projectile(11, 20, 0, 0);
        game.projectiles.insertHead(wrongPlane);
        game.projectiles.insertHead(expired);
        game.projectiles.insertHead(future);
        game.updateProjectiles();
        assertNull(wrongPlane.next);
        assertNull(expired.next);
        assertSame(future, game.projectiles.firstFromTail());
        assertNull(game.projectiles.nextTowardHead());
        assertEquals(320.0, future.positionX, 0.001);
        Game.loopCycle = 11;
        game.updateProjectiles();
        assertTrue(future.positionX > 320.0);
        assertSame(future, game.projectiles.firstFromTail());
        assertNotNull(grounds(game)[0][2][2]);
    }

    @Test public void method55ProcessesEachQueuedEntryIndependently() throws Exception {
        Game game = game();
        Game.loopCycle = 10;
        Projectile first = projectile(10, 20, 0, 0);
        Projectile second = projectile(10, 20, 0, 0);
        second.retarget(10, 448, 0, 320);
        game.projectiles.insertHead(first);
        game.projectiles.insertHead(second);
        game.updateProjectiles();
        assertTrue(first.positionX > 320);
        assertEquals(320.0, second.positionX, 0.001);
        assertTrue(second.positionY > 320);
        assertSame(first, game.projectiles.firstFromTail());
        assertSame(second, game.projectiles.nextTowardHead());
    }

    @Test public void method55RetargetsNpcAndDecodesNegativePlayerIndex() throws Exception {
        Game game = game();
        Game.loopCycle = 10;
        NPC npc = new NPC(); npc.x = 448; npc.y = 320;
        game.npcArray[0] = npc;
        Player indexed = new Player(); indexed.x = 768; indexed.y = 320;
        game.playerArray[0] = indexed;
        Player local = new Player(); local.x = 640; local.y = 320;
        Game.myPlayer = local; game.unknownInt10 = 1;
        Projectile npcShot = projectile(10, 15, 0, 1);
        Projectile indexedShot = projectile(10, 15, 0, -1);
        Projectile localShot = projectile(10, 15, 0, -2);
        game.projectiles.insertHead(npcShot);
        game.projectiles.insertHead(indexedShot);
        game.projectiles.insertHead(localShot);
        game.updateProjectiles();
        assertEquals(320 + 128.0 / 6, npcShot.positionX, 0.001);
        assertEquals(320 + 448.0 / 6, indexedShot.positionX, 0.001);
        assertEquals(320 + 320.0 / 6, localShot.positionX, 0.001);
        assertTrue(indexedShot.positionX > localShot.positionX);
        assertTrue(localShot.positionX > npcShot.positionX);
        indexed.x = 13312;
        Projectile invalidTarget = projectile(10, 15, 0, -1);
        game.projectiles.insertHead(invalidTarget);
        game.updateProjectiles();
        assertEquals(320 + 128.0 / 6, invalidTarget.positionX, 0.001);
    }
}
