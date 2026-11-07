import static org.junit.Assert.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** Pass 159: semantic tests (original identifiers) for the Entity movement fields not already covered by WalkingPathTest/EntityTurningTest. */
public class EntityMovementFieldsTest {
    private Animation[] oldAnimations;
    @Before public void save() { oldAnimations = Animation.anims; }
    @After public void restore() { Animation.anims = oldAnimations; }

    private static Game game() throws Exception {
        Field f = sun.misc.Unsafe.class.getDeclaredField("theUnsafe"); f.setAccessible(true);
        return (Game) ((sun.misc.Unsafe) f.get(null)).allocateInstance(Game.class);
    }
    private static Animation anim(int a363, int a364) throws Exception {
        Constructor<Animation> c = Animation.class.getDeclaredConstructor(); c.setAccessible(true);
        Animation a = c.newInstance(); a.movingPrecedence = a363; a.idlePrecedence = a364; return a;
    }
    private static Entity walker(int queueDepth) {
        Entity e = new Entity();
        e.x = 1344; e.y = 1344; e.smallXYIndex = queueDepth;
        for (int i = 0; i < queueDepth; i++) { e.smallX[i] = 11; e.smallY[i] = 10; }
        e.idleAnimationId = 10; e.walkAnimationId = 20; e.reverseWalkAnimationId = 21; e.sidestepNegativeAnimationId = 22; e.sidestepPositiveAnimationId = 23; e.runAnimationId = 24;
        e.facingDirection = 1536;
        return e;
    }

    @Test public void anInt1503CountsTicksHeldBackByAnimationsOnly() throws Exception {
        Game g = game();
        Animation.anims = new Animation[]{anim(0, 0), anim(1, 1)};
        // walking queue pending (anInt1542 > 0), animation 0 forbids movement then (movingPrecedence == 0)
        Entity a = walker(1); a.anim = 0; a.animationDelay = 0; a.anInt1542 = 2;
        g.advanceEntityAlongWalkingPath(a);
        assertEquals(1, a.deferredMovementTicks); assertEquals(1344, a.x);
        g.advanceEntityAlongWalkingPath(a);
        assertEquals(2, a.deferredMovementTicks); assertEquals(1344, a.x);
        // no pending queue (anInt1542 <= 0): governed by idlePrecedence instead
        Entity b = walker(1); b.anim = 0; b.anInt1542 = 0;
        g.advanceEntityAlongWalkingPath(b);
        assertEquals(1, b.deferredMovementTicks); assertEquals(1344, b.x);
        // counterexamples: animation that allows movement, animation delay running, no animation
        Entity c = walker(1); c.anim = 1; c.anInt1542 = 2;
        g.advanceEntityAlongWalkingPath(c);
        assertEquals(0, c.deferredMovementTicks); assertEquals(1348, c.x);
        Entity d = walker(1); d.anim = 0; d.animationDelay = 1; d.anInt1542 = 2;
        g.advanceEntityAlongWalkingPath(d);
        assertEquals(0, d.deferredMovementTicks); assertEquals(1348, d.x);
        Entity e = walker(1); e.anim = -1; e.anInt1542 = 2;
        g.advanceEntityAlongWalkingPath(e);
        assertEquals(0, e.deferredMovementTicks); assertEquals(1348, e.x);
    }

    @Test public void anInt1503IsRepaidAsFastStepsOnlyWhileTheQueueIsDeeperThanOne() throws Exception {
        Game g = game();
        Entity deep = walker(2); deep.deferredMovementTicks = 3;
        g.advanceEntityAlongWalkingPath(deep);
        assertEquals(1352, deep.x); assertEquals(2, deep.deferredMovementTicks);
        Entity none = walker(2); none.deferredMovementTicks = 0;
        g.advanceEntityAlongWalkingPath(none);
        assertEquals(1348, none.x); assertEquals(0, none.deferredMovementTicks);
        Entity single = walker(1); single.deferredMovementTicks = 3;
        g.advanceEntityAlongWalkingPath(single);
        assertEquals(1348, single.x); assertEquals(3, single.deferredMovementTicks);
    }

    @Test public void anInt1505ReplacesOnlyTheForwardWalkAnimationAtFastStepSpeed() throws Exception {
        Game g = game();
        // run flag doubles the step to 8; heading 1536 vs facing 1536: forward walk animation -> replaced by runAnimationId
        Entity run = walker(1); run.aBooleanArray1553[0] = true;
        g.advanceEntityAlongWalkingPath(run);
        assertEquals(1352, run.x); assertEquals(24, run.movementAnimationId);
        // normal speed (4): not replaced
        Entity slow = walker(1);
        g.advanceEntityAlongWalkingPath(slow);
        assertEquals(1348, slow.x); assertEquals(20, slow.movementAnimationId);
        // fast but sidestepping (facing 1024, heading 1536): stays the sidestep animation
        Entity side = walker(1); side.aBooleanArray1553[0] = true; side.facingDirection = 1024;
        g.advanceEntityAlongWalkingPath(side);
        assertEquals(23, side.movementAnimationId);
        // fast but backwards (facing 512): stays the reverse animation
        Entity back = walker(1); back.aBooleanArray1553[0] = true; back.facingDirection = 512;
        g.advanceEntityAlongWalkingPath(back);
        assertEquals(21, back.movementAnimationId);
        // -1 means none: keep the walk animation
        Entity none = walker(1); none.aBooleanArray1553[0] = true; none.runAnimationId = -1;
        g.advanceEntityAlongWalkingPath(none);
        assertEquals(1352, none.x); assertEquals(20, none.movementAnimationId);
    }

    @Test public void anInt1512ReplacesTheIdleAnimationOnlyWhileTurningInPlace() throws Exception {
        Game g = game();
        Entity turning = new Entity();
        turning.idleAnimationId = 10; turning.turnInPlaceAnimationId = 11; turning.walkAnimationId = 20;
        turning.turnSpeed = 32; turning.facingDirection = 0; turning.turnDirection = 1024; turning.movementAnimationId = 10;
        g.updateEntityTurning(turning);
        assertEquals(32, turning.facingDirection); assertEquals(11, turning.movementAnimationId);
        // already facing the target: stays idle
        Entity still = new Entity();
        still.idleAnimationId = 10; still.turnInPlaceAnimationId = 11; still.walkAnimationId = 20; still.facingDirection = 1024; still.turnDirection = 1024; still.movementAnimationId = 10;
        g.updateEntityTurning(still);
        assertEquals(10, still.movementAnimationId);
        // not idle (already walking): untouched
        Entity walking = new Entity();
        walking.idleAnimationId = 10; walking.turnInPlaceAnimationId = 11; walking.walkAnimationId = 20; walking.facingDirection = 0; walking.turnDirection = 1024; walking.movementAnimationId = 20;
        g.updateEntityTurning(walking);
        assertEquals(20, walking.movementAnimationId);
        // no turning animation: falls back to the walk animation
        Entity fallback = new Entity();
        fallback.idleAnimationId = 10; fallback.turnInPlaceAnimationId = -1; fallback.walkAnimationId = 20; fallback.facingDirection = 0; fallback.turnDirection = 1024; fallback.movementAnimationId = 10;
        g.updateEntityTurning(fallback);
        assertEquals(20, fallback.movementAnimationId);
    }
}
