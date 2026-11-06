import java.lang.reflect.Field;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class CameraFollowTest {
    private Player priorPlayer;
    private int priorCounter;

    @Before public void saveStatics() {
        priorPlayer = Game.myPlayer;
        priorCounter = Game.anInt1005;
        Game.anInt1005 = 0;
    }

    @After public void restoreStatics() {
        Game.myPlayer = priorPlayer;
        Game.anInt1005 = priorCounter;
    }

    private static Game game() throws Exception {
        Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        Game game = (Game) ((sun.misc.Unsafe) unsafeField.get(null)).allocateInstance(Game.class);
        Field keys = RSApplet.class.getDeclaredField("keyArray");
        keys.setAccessible(true);
        keys.set(game, new int[128]);
        game.stream = new Stream(new byte[128]);
        game.intGroundArray = new int[4][105][105];
        game.byteGroundArray = new byte[4][105][105];
        game.anInt984 = 32768;
        Game.myPlayer = new Player();
        Game.myPlayer.x = 50 * 128 + 64;
        Game.myPlayer.y = 50 * 128 + 64;
        return game;
    }

    @Test public void followsEachWorldAxisAndSnapsBeyondFiveHundredUnits() throws Exception {
        Game game = game();
        game.updateCameraFollow();
        assertEquals(Game.myPlayer.x, game.anInt1014);
        assertEquals(Game.myPlayer.y, game.anInt1015);
        Game.myPlayer.x += 160;
        game.updateCameraFollow();
        assertEquals(Game.myPlayer.x - 150, game.anInt1014);
        assertEquals(Game.myPlayer.y, game.anInt1015);
        Game.myPlayer.y += 160;
        game.updateCameraFollow();
        assertEquals(Game.myPlayer.y - 150, game.anInt1015);
        game.anInt1278 = 32;
        game.anInt1131 = -32;
        game.updateCameraFollow();
        assertEquals(Game.myPlayer.x - 150 + (150 / 16) + (173 / 16), game.anInt1014);
        assertEquals(Game.myPlayer.y - 150 + (118 / 16), game.anInt1015);
        Game.myPlayer.x += 501;
        game.updateCameraFollow();
        assertEquals(Game.myPlayer.x + 32, game.anInt1014);
    }

    @Test public void keyboardVelocityChangesRotationAndClampsPitch() throws Exception {
        Game game = game(); game.updateCameraFollow();
        game.keyArray[2] = 1;
        game.keyArray[3] = 1;
        game.anInt1184 = 380;
        game.updateCameraFollow();
        assertEquals(12, game.anInt1186);
        assertEquals(6, game.minimapInt1);
        assertEquals(6, game.anInt1187);
        assertEquals(383, game.anInt1184);
        game.keyArray[2] = 0; game.keyArray[3] = 0;
        game.keyArray[1] = 1; game.keyArray[4] = 1;
        game.anInt1184 = 128;
        game.updateCameraFollow();
        assertEquals(-6, game.anInt1186);
        assertEquals(3, game.minimapInt1);
        assertEquals(-3, game.anInt1187);
        assertEquals(128, game.anInt1184);
        game.keyArray[1] = 0; game.keyArray[4] = 0;
        game.updateCameraFollow();
        assertEquals(-3, game.anInt1186);
        assertEquals(-1, game.anInt1187);
    }

    @Test public void nearbyGroundClearanceSetsAndSmoothsCameraDistance() throws Exception {
        Game game = game(); game.updateCameraFollow();
        assertEquals(32768, game.anInt984);
        for (int x = 50; x <= 51; x++) for (int y = 50; y <= 51; y++) game.intGroundArray[0][x][y] = 1000;
        game.updateCameraFollow();
        assertEquals(32768 + (0x17f00 - 32768) / 24, game.anInt984);
        game.anInt984 = 0x17f00;
        for (int x = 50; x <= 51; x++) for (int y = 50; y <= 51; y++) game.intGroundArray[0][x][y] = 0;
        game.updateCameraFollow();
        assertEquals(0x17f00 - (0x17f00 - 32768) / 80, game.anInt984);
        game.anInt984 = 32768;
        for (int x = 46; x <= 54; x++) for (int y = 46; y <= 54; y++) game.intGroundArray[0][x][y] = 1000;
        game.byteGroundArray[1][49][50] = 2;
        game.intGroundArray[1][49][50] = 0;
        game.updateCameraFollow();
        assertEquals(32768 + (0x17f00 - 32768) / 24, game.anInt984);
        game.byteGroundArray[1][49][50] = 0;
        game.anInt984 = 32768;
        game.updateCameraFollow();
        assertEquals(32768, game.anInt984);
    }
}
