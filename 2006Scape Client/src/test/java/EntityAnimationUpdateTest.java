import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class EntityAnimationUpdateTest {
    private Animation[] oldAnimations;
    private SpotAnim[] oldSpots;

    @Before public void saveTables() { oldAnimations = Animation.anims; oldSpots = SpotAnim.cache; }
    @After public void restoreTables() { Animation.anims = oldAnimations; SpotAnim.cache = oldSpots; }

    private static Game game() throws Exception {
        Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (Game) ((sun.misc.Unsafe) field.get(null)).allocateInstance(Game.class);
    }
    private static Animation sequence(int... delays) throws Exception {
        Constructor<Animation> constructor = Animation.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Animation animation = constructor.newInstance();
        animation.frameCount = delays.length;
        Field field = Animation.class.getDeclaredField("frameDelays");
        field.setAccessible(true);
        field.set(animation, delays);
        return animation;
    }
    private static SpotAnim spot(Animation sequence) throws Exception {
        Constructor<SpotAnim> constructor = SpotAnim.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        SpotAnim result = constructor.newInstance();
        result.animation = sequence;
        return result;
    }

    @Test public void independentSequenceSourcesAdvanceTheirOwnCounters() throws Exception {
        Animation.anims = new Animation[]{sequence(2, 3), sequence(5, 4)};
        SpotAnim.cache = new SpotAnim[]{spot(sequence(1, 1))};
        Game game = game();
        Entity entity = new Entity();
        entity.movementAnimationId = 0;
        entity.anInt1520 = 0;
        entity.anim = 1;
        game.updateEntityAnimations(entity);
        assertEquals(0, entity.anInt1518); assertEquals(1, entity.anInt1519);
        assertEquals(0, entity.anInt1521); assertEquals(1, entity.anInt1522);
        assertEquals(0, entity.anInt1527); assertEquals(1, entity.anInt1528);
        game.updateEntityAnimations(entity);
        assertEquals(0, entity.anInt1518); assertEquals(2, entity.anInt1519);
        assertEquals(1, entity.anInt1521); assertEquals(1, entity.anInt1522);
        assertEquals(0, entity.anInt1527); assertEquals(2, entity.anInt1528);
        game.updateEntityAnimations(entity);
        assertEquals(1, entity.anInt1518); assertEquals(0, entity.anInt1519);
        assertEquals(-1, entity.anInt1520);
        assertEquals(0, entity.anInt1527); assertEquals(3, entity.anInt1528);
    }

    @Test public void spotWaitsForStartCycleAndMissingSlotsStayInactive() throws Exception {
        Animation.anims = new Animation[]{sequence(1)};
        SpotAnim.cache = new SpotAnim[]{spot(sequence(1))};
        Game game = game(); game.loopCycle = 9;
        Entity entity = new Entity(); entity.anInt1520 = 0; entity.anInt1523 = 10;
        entity.anInt1521 = -1;
        game.updateEntityAnimations(entity);
        assertEquals(-1, entity.anInt1521); assertEquals(0, entity.anInt1522);
        game.loopCycle = 10; game.updateEntityAnimations(entity);
        assertEquals(0, entity.anInt1521); assertEquals(1, entity.anInt1522);
        assertEquals(-1, entity.movementAnimationId); assertEquals(-1, entity.anim);
    }

    @Test public void actionPrecedenceGatesProgressOnlyDuringMovementWindow() throws Exception {
        Animation action = sequence(1, 3); action.movingPrecedence = 1;
        Animation.anims = new Animation[]{action}; SpotAnim.cache = new SpotAnim[0];
        Game game = game(); game.loopCycle = 100;
        Entity entity = new Entity(); entity.anim = 0; entity.anInt1542 = 1;
        entity.anInt1547 = 100; entity.anInt1548 = 99;
        game.updateEntityAnimations(entity);
        assertEquals(1, entity.anInt1529); assertEquals(0, entity.anInt1528);
        entity.anInt1547 = 101; entity.anInt1529 = 0;
        game.updateEntityAnimations(entity);
        assertEquals(0, entity.anInt1529); assertEquals(1, entity.anInt1528);
        game.updateEntityAnimations(entity);
        assertEquals(1, entity.anInt1527); assertEquals(1, entity.anInt1528);
        entity.anInt1529 = 2;
        game.updateEntityAnimations(entity);
        assertEquals(1, entity.anInt1529); assertEquals(1, entity.anInt1528);
    }

    @Test public void actionSequenceSuppliesLoopLimitAndRenderFlag() throws Exception {
        Animation action = sequence(1, 1); action.loopBackFrames = 1; action.maxLoops = 2;
        action.extendByYaw = true;
        Animation.anims = new Animation[]{action}; SpotAnim.cache = new SpotAnim[0];
        Game game = game(); Entity entity = new Entity(); entity.anim = 0;
        game.updateEntityAnimations(entity);
        assertTrue(entity.aBoolean1541); assertEquals(0, entity.anInt1527);
        game.updateEntityAnimations(entity); game.updateEntityAnimations(entity);
        assertEquals(1, entity.anInt1530); assertEquals(1, entity.anInt1527);
        game.updateEntityAnimations(entity); game.updateEntityAnimations(entity);
        assertEquals(-1, entity.anim); assertEquals(2, entity.anInt1530);
        game.updateEntityAnimations(entity);
        assertFalse(entity.aBoolean1541);
    }
}
