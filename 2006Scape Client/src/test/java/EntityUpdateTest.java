import static org.junit.Assert.*;
import java.util.*;
import org.junit.*;

public class EntityUpdateTest {
    private static class Probe extends Game {
        final List<String> calls = new ArrayList<String>();
        @Override public void advanceEntityToForcedMovementStart(Entity e) { calls.add("start"); }
        @Override public void advanceEntityThroughForcedMovement(Entity e) { calls.add("forced"); }
        @Override public void advanceEntityAlongWalkingPath(Entity e) { calls.add("walk"); }
        @Override public void updateEntityTurning(Entity e) { calls.add("turn"); }
        @Override public void updateEntityAnimations(Entity e) { calls.add("animate"); }
    }
    private Player saved;
    private int savedCycle;
    @Before public void before() { saved = Game.myPlayer; savedCycle = Game.loopCycle; Game.myPlayer = null; Game.loopCycle = 100; }
    @After public void after() { Game.myPlayer = saved; Game.loopCycle = savedCycle; }
    private Entity entity(int x, int y) {
        Entity e = new Entity(); e.x = x; e.y = y;
        e.smallX[0] = 40; e.smallY[0] = 50; e.tileSize = 1;
        e.anim = 7; e.spotAnimId = 8; e.anInt1547 = 0; e.anInt1548 = 0;
        e.smallXYIndex = 3; e.anInt1542 = 2;
        return e;
    }
    @Test public void worldBoundsResetEachAxisAndContinueDispatch() {
        for (int[] position : new int[][] {{127, 4000}, {4000, 127}, {13184, 4000}, {4000, 13184}}) {
            Probe g = new Probe(); Entity e = entity(position[0], position[1]);
            g.advanceEntityMovementAndAnimation(e);
            assertEquals(40 * 128 + 64, e.x); assertEquals(50 * 128 + 64, e.y);
            assertEquals(-1, e.anim); assertEquals(-1, e.spotAnimId);
            assertEquals(0, e.smallXYIndex); assertEquals(0, e.anInt1542);
            assertEquals(Arrays.asList("walk", "turn", "animate"), g.calls);
        }
        Probe g = new Probe(); Entity e = entity(128, 13183);
        g.advanceEntityMovementAndAnimation(e);
        assertEquals(128, e.x); assertEquals(13183, e.y);
        assertEquals(7, e.anim); assertEquals(3, e.smallXYIndex);
        assertEquals(Arrays.asList("walk", "turn", "animate"), g.calls);
    }
    @Test public void localPlayerHasTighterBoundsButOtherEntitiesDoNot() {
        Probe g = new Probe(); Player p = new Player(); p.x = 1535; p.y = 11776;
        p.smallX[0] = 42; p.smallY[0] = 43; p.tileSize = 1;
        p.anim = 7; p.spotAnimId = 8; p.smallXYIndex = 2; p.anInt1542 = 3;
        Game.myPlayer = p; g.advanceEntityMovementAndAnimation(p);
        assertEquals(42 * 128 + 64, p.x); assertEquals(43 * 128 + 64, p.y);
        assertEquals(-1, p.anim); assertEquals(0, p.smallXYIndex);
        Entity e = entity(1535, 11776); g.calls.clear(); g.advanceEntityMovementAndAnimation(e);
        assertEquals(1535, e.x); assertEquals(11776, e.y);
        assertEquals(7, e.anim); assertEquals(3, e.smallXYIndex);
        assertEquals(Arrays.asList("walk", "turn", "animate"), g.calls);
    }
    @Test public void timingSelectsOneMovementBranchThenTurnsAndAnimates() {
        Probe g = new Probe(); Entity e = entity(5000, 6000);
        e.anInt1547 = 101; e.anInt1548 = 105;
        g.advanceEntityMovementAndAnimation(e); assertEquals(Arrays.asList("start", "turn", "animate"), g.calls);
        g.calls.clear(); e.anInt1547 = 100;
        g.advanceEntityMovementAndAnimation(e); assertEquals(Arrays.asList("forced", "turn", "animate"), g.calls);
        g.calls.clear(); e.anInt1548 = 99;
        g.advanceEntityMovementAndAnimation(e); assertEquals(Arrays.asList("walk", "turn", "animate"), g.calls);
    }
}
