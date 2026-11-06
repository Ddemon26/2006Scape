import static org.junit.Assert.*;
import org.junit.Test;

public class WalkingPathTest {
    private Entity at(int x, int y, int tileX, int tileY) {
        Entity e = new Entity();
        e.x = x; e.y = y; e.smallXYIndex = 1;
        e.smallX[0] = tileX; e.smallY[0] = tileY;
        e.anInt1511 = 10; e.anInt1554 = 20; e.anInt1555 = 21;
        e.anInt1556 = 22; e.anInt1557 = 23; e.anInt1505 = 24;
        e.anInt1552 = 1536;
        return e;
    }

    @Test public void emptyQueueAndFarStepHaveDistinctOutcomes() {
        Game g = new Game();
        Entity empty = at(1344, 1344, 11, 10);
        empty.smallXYIndex = 0; empty.anInt1503 = 7;
        g.advanceEntityAlongWalkingPath(empty);
        assertEquals(1344, empty.x); assertEquals(0, empty.anInt1503);
        assertEquals(10, empty.anInt1517);
        Entity far = at(1344, 1344, 13, 10);
        g.advanceEntityAlongWalkingPath(far);
        assertEquals(1728, far.x); assertEquals(1344, far.y);
        assertEquals(1, far.smallXYIndex);
        assertEquals(10, far.anInt1517);
    }

    @Test public void lastQueuedTileAndSizeSetIndependentTargets() {
        Game g = new Game();
        Entity e = at(1380, 1410, 11, 11);
        e.anInt1540 = 2; e.smallXYIndex = 2;
        e.smallX[0] = 99; e.smallY[0] = 99;
        e.smallX[1] = 10; e.smallY[1] = 10;
        e.anInt1552 = 1792;
        g.advanceEntityAlongWalkingPath(e);
        assertEquals(1384, e.x); assertEquals(1408, e.y);
        assertEquals(2, e.smallXYIndex);
    }

    @Test public void relativeFacingSelectsDirectionalAnimationAndFallback() {
        Game g = new Game();
        Entity forward = at(1344, 1344, 11, 10);
        g.advanceEntityAlongWalkingPath(forward);
        assertEquals(1536, forward.turnDirection); assertEquals(20, forward.anInt1517);
        assertEquals(1348, forward.x); assertEquals(1344, forward.y);
        Entity right = at(1344, 1344, 11, 10);
        right.anInt1552 = 1024;
        g.advanceEntityAlongWalkingPath(right);
        assertEquals(23, right.anInt1517);
        Entity left = at(1344, 1344, 11, 10);
        left.anInt1552 = 0;
        g.advanceEntityAlongWalkingPath(left);
        assertEquals(22, left.anInt1517);
        Entity back = at(1344, 1344, 11, 10);
        back.anInt1552 = 512; back.anInt1555 = -1;
        g.advanceEntityAlongWalkingPath(back);
        assertEquals(20, back.anInt1517);
    }

    @Test public void queueDepthAndRunFlagSetSpeedAndArrivalCounters() {
        Game g = new Game();
        Entity deep = at(1344, 1344, 11, 10);
        deep.smallXYIndex = 4; deep.smallX[3] = 11; deep.smallY[3] = 10;
        deep.anInt1503 = 2; deep.anInt1542 = 3;
        g.advanceEntityAlongWalkingPath(deep);
        assertEquals(1352, deep.x); assertEquals(1, deep.anInt1503);
        assertEquals(24, deep.anInt1517);
        Entity running = at(1468, 1344, 11, 10);
        running.aBooleanArray1553[0] = true; running.anInt1542 = 2;
        g.advanceEntityAlongWalkingPath(running);
        assertEquals(1472, running.x); assertEquals(0, running.smallXYIndex);
        assertEquals(1, running.anInt1542);
        assertEquals(24, running.anInt1517);
        Entity walking = at(1468, 1344, 11, 10);
        g.advanceEntityAlongWalkingPath(walking);
        assertEquals(1472, walking.x); assertEquals(0, walking.smallXYIndex);
        assertEquals(20, walking.anInt1517);
    }
}
