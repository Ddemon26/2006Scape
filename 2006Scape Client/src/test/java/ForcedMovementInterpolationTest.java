import static org.junit.Assert.*;
import org.junit.Test;

public class ForcedMovementInterpolationTest {
    private Entity entity(int sx, int sy, int ex, int ey, int size) {
        Entity e = new Entity();
        e.forceMoveStartX = sx; e.forceMoveStartY = sy;
        e.forceMoveEndX = ex; e.forceMoveEndY = ey;
        e.tileSize = size; e.anim = -1;
        e.forceMoveStartCycle = 100; e.forceMoveEndCycle = 104;
        e.x = -50; e.y = -60; e.deferredMovementTicks = 8;
        return e;
    }

    @Test public void timeAndTileEndpointsControlBothAxesIndependently() {
        Game g = new Game(); g.loopCycle = 101;
        Entity a = entity(10, 20, 14, 24, 1);
        g.advanceEntityThroughForcedMovement(a);
        assertEquals(1472, a.x); assertEquals(2752, a.y);
        assertEquals(0, a.deferredMovementTicks);
        Entity b = entity(11, 20, 14, 24, 1);
        g.advanceEntityThroughForcedMovement(b); assertEquals(1568, b.x); assertEquals(2752, b.y);
        Entity c = entity(10, 21, 14, 24, 1);
        g.advanceEntityThroughForcedMovement(c); assertEquals(1472, c.x); assertEquals(2848, c.y);
        Entity d = entity(10, 20, 15, 25, 1);
        g.advanceEntityThroughForcedMovement(d); assertEquals(1504, d.x); assertEquals(2784, d.y);
        g.loopCycle = 102;
        Entity e = entity(10, 20, 14, 24, 1);
        g.advanceEntityThroughForcedMovement(e); assertEquals(1600, e.x); assertEquals(2880, e.y);
        Entity f = entity(10, 20, 14, 24, 2);
        g.advanceEntityThroughForcedMovement(f); assertEquals(1664, f.x); assertEquals(2944, f.y);
    }

    @Test public void durationBoundaryAndFacingMapping() {
        Game g = new Game(); g.loopCycle = 101;
        Entity a = entity(10, 20, 14, 24, 1);
        a.forceMoveEndCycle = 102; a.forceMoveDirection = 3; a.turnDirection = 777;
        g.advanceEntityThroughForcedMovement(a); assertEquals(1600, a.x); assertEquals(2880, a.y);
        assertEquals(512, a.turnDirection); assertEquals(512, a.facingDirection);
        Entity b = entity(10, 20, 14, 24, 1);
        b.forceMoveDirection = 4; b.turnDirection = 777;
        g.advanceEntityThroughForcedMovement(b); assertEquals(777, b.turnDirection); assertEquals(777, b.facingDirection);
    }
}
