import static org.junit.Assert.*;
import org.junit.Test;

public class WalkingPathTest {
    private Entity at(int x, int y, int tileX, int tileY) {
        Entity e = new Entity();
        e.x = x; e.y = y; e.smallXYIndex = 1;
        e.smallX[0] = tileX; e.smallY[0] = tileY;
        e.idleAnimationId = 10; e.walkAnimationId = 20; e.reverseWalkAnimationId = 21;
        e.sidestepNegativeAnimationId = 22; e.sidestepPositiveAnimationId = 23; e.runAnimationId = 24;
        e.facingDirection = 1536;
        return e;
    }

    @Test public void emptyQueueAndFarStepHaveDistinctOutcomes() {
        Game g = new Game();
        Entity empty = at(1344, 1344, 11, 10);
        empty.smallXYIndex = 0; empty.deferredMovementTicks = 7;
        g.advanceEntityAlongWalkingPath(empty);
        assertEquals(1344, empty.x); assertEquals(0, empty.deferredMovementTicks);
        assertEquals(10, empty.movementAnimationId);
        Entity far = at(1344, 1344, 13, 10);
        g.advanceEntityAlongWalkingPath(far);
        assertEquals(1728, far.x); assertEquals(1344, far.y);
        assertEquals(1, far.smallXYIndex);
        assertEquals(10, far.movementAnimationId);
    }

    @Test public void lastQueuedTileAndSizeSetIndependentTargets() {
        Game g = new Game();
        Entity e = at(1380, 1410, 11, 11);
        e.tileSize = 2; e.smallXYIndex = 2;
        e.smallX[0] = 99; e.smallY[0] = 99;
        e.smallX[1] = 10; e.smallY[1] = 10;
        e.facingDirection = 1792;
        g.advanceEntityAlongWalkingPath(e);
        assertEquals(1384, e.x); assertEquals(1408, e.y);
        assertEquals(2, e.smallXYIndex);
    }

    @Test public void relativeFacingSelectsDirectionalAnimationAndFallback() {
        Game g = new Game();
        Entity forward = at(1344, 1344, 11, 10);
        g.advanceEntityAlongWalkingPath(forward);
        assertEquals(1536, forward.turnDirection); assertEquals(20, forward.movementAnimationId);
        assertEquals(1348, forward.x); assertEquals(1344, forward.y);
        Entity right = at(1344, 1344, 11, 10);
        right.facingDirection = 1024;
        g.advanceEntityAlongWalkingPath(right);
        assertEquals(23, right.movementAnimationId);
        Entity left = at(1344, 1344, 11, 10);
        left.facingDirection = 0;
        g.advanceEntityAlongWalkingPath(left);
        assertEquals(22, left.movementAnimationId);
        Entity back = at(1344, 1344, 11, 10);
        back.facingDirection = 512; back.reverseWalkAnimationId = -1;
        g.advanceEntityAlongWalkingPath(back);
        assertEquals(20, back.movementAnimationId);
    }

    @Test public void queueDepthAndRunFlagSetSpeedAndArrivalCounters() {
        Game g = new Game();
        Entity deep = at(1344, 1344, 11, 10);
        deep.smallXYIndex = 4; deep.smallX[3] = 11; deep.smallY[3] = 10;
        deep.deferredMovementTicks = 2; deep.stepsQueuedBeforeAnimation = 3;
        g.advanceEntityAlongWalkingPath(deep);
        assertEquals(1352, deep.x); assertEquals(1, deep.deferredMovementTicks);
        assertEquals(24, deep.movementAnimationId);
        Entity running = at(1468, 1344, 11, 10);
        running.pathRunFlags[0] = true; running.stepsQueuedBeforeAnimation = 2;
        g.advanceEntityAlongWalkingPath(running);
        assertEquals(1472, running.x); assertEquals(0, running.smallXYIndex);
        assertEquals(1, running.stepsQueuedBeforeAnimation);
        assertEquals(24, running.movementAnimationId);
        Entity walking = at(1468, 1344, 11, 10);
        g.advanceEntityAlongWalkingPath(walking);
        assertEquals(1472, walking.x); assertEquals(0, walking.smallXYIndex);
        assertEquals(20, walking.movementAnimationId);
    }
}
