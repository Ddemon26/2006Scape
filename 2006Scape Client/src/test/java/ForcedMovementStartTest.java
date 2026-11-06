import static org.junit.Assert.*;
import org.junit.Test;

public class ForcedMovementStartTest {
    private Entity entity(int x, int y, int tileX, int tileY, int size, int direction) {
        Entity e = new Entity();
        e.x = x; e.y = y; e.anInt1543 = tileX; e.anInt1545 = tileY;
        e.anInt1540 = size; e.anInt1549 = direction; e.anInt1503 = 9;
        e.turnDirection = 777;
        return e;
    }

    @Test public void remainingTicksControlApproachToIndependentTileCenters() {
        Game g = new Game(); g.loopCycle = 100;
        Entity a = entity(1000, 2000, 10, 20, 1, 0);
        a.anInt1547 = 104; g.advanceEntityToForcedMovementStart(a);
        assertEquals(1086, a.x); assertEquals(2156, a.y);
        assertEquals(0, a.anInt1503); assertEquals(1024, a.turnDirection);
        Entity b = entity(1000, 2000, 11, 20, 1, 1);
        b.anInt1547 = 104; g.advanceEntityToForcedMovementStart(b);
        assertEquals(1118, b.x); assertEquals(2156, b.y);
        assertEquals(1536, b.turnDirection);
        Entity c = entity(1000, 2000, 10, 21, 2, 2);
        c.anInt1547 = 102; g.advanceEntityToForcedMovementStart(c);
        assertEquals(1204, c.x); assertEquals(2408, c.y);
        assertEquals(0, c.turnDirection);
    }

    @Test public void oneTickAndUnmappedDirectionAreBoundaries() {
        Game g = new Game(); g.loopCycle = 100;
        Entity a = entity(2000, 3000, 10, 20, 1, 3);
        a.anInt1547 = 101; g.advanceEntityToForcedMovementStart(a);
        assertEquals(1344, a.x); assertEquals(2624, a.y);
        assertEquals(512, a.turnDirection);
        Entity b = entity(2000, 3000, 10, 20, 1, 4);
        b.anInt1547 = 104; g.advanceEntityToForcedMovementStart(b);
        assertEquals(1836, b.x); assertEquals(2906, b.y);
        assertEquals(777, b.turnDirection);
    }
}
