import java.lang.reflect.Field;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class GameRegionFlagTest {
    private Player savedPlayer;
    private Game game;

    @Before public void setUp() throws Exception {
        savedPlayer = Game.myPlayer;
        Game.myPlayer = new Player();
        Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        game = (Game) ((sun.misc.Unsafe) field.get(null)).allocateInstance(Game.class);
    }

    @After public void tearDown() {
        Game.myPlayer = savedPlayer;
    }

    private void check(int x, int y, int expected) {
        game.baseX = 3000;
        game.baseY = 3000;
        Game.myPlayer.x = (x - 3000) * 128;
        Game.myPlayer.y = (y - 3000) * 128;
        game.chatSuppressionZone = 9;
        game.updateChatSuppressionZone();
        assertEquals("world tile " + x + "," + y, expected, game.chatSuppressionZone);
    }

    @Test public void method70UsesInclusiveRectanglesAndExclusion() {
        check(3053, 3056, 1);
        check(3052, 3056, 0);
        check(3053, 3055, 0);
        check(3156, 3136, 1);
        check(3157, 3136, 0);
        check(3156, 3137, 0);
        check(3072, 9492, 1);
        check(3071, 9492, 0);
        check(3072, 9491, 0);
        check(3118, 9535, 1);
        check(3119, 9535, 0);
        check(3118, 9536, 0);
        check(3138, 3056, 1);
        check(3139, 3056, 0);
        check(3156, 3062, 0);
        check(3156, 3063, 1);
        check(3199, 3008, 0);
        check(3200, 3008, 0);
    }

    @Test public void method70CombinesMapBaseWithLocalPositionIn128UnitTiles() {
        game.baseX = 3052;
        game.baseY = 3055;
        Game.myPlayer.x = 127;
        Game.myPlayer.y = 128;
        game.updateChatSuppressionZone();
        assertEquals(0, game.chatSuppressionZone);
        Game.myPlayer.x = 128;
        game.updateChatSuppressionZone();
        assertEquals(1, game.chatSuppressionZone);
        Game.myPlayer.y = 127;
        game.updateChatSuppressionZone();
        assertEquals(0, game.chatSuppressionZone);
        game.baseY = 3056;
        game.updateChatSuppressionZone();
        assertEquals(1, game.chatSuppressionZone);
        game.baseX = 3051;
        game.updateChatSuppressionZone();
        assertEquals(0, game.chatSuppressionZone);
    }
}
