import static org.junit.Assert.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** Pass 196: semantic tests (original identifiers) for the Entity forced-movement, walking-queue and action-animation bookkeeping fields. */
public class EntityForcedMovementFieldsTest {
    private Animation[] oldAnimations;
    private int oldCycle;
    private boolean oldReporting;
    @Before public void save() { oldAnimations = Animation.anims; oldCycle = Game.loopCycle; oldReporting = Signlink.reporterror; Signlink.reporterror = false; }
    @After public void restore() { Animation.anims = oldAnimations; Game.loopCycle = oldCycle; Signlink.reporterror = oldReporting; }

    private static Game game() throws Exception {
        Field f = sun.misc.Unsafe.class.getDeclaredField("theUnsafe"); f.setAccessible(true);
        return (Game) ((sun.misc.Unsafe) f.get(null)).allocateInstance(Game.class);
    }
    private static Stream packet(int... values) {
        byte[] bytes = new byte[values.length];
        for (int i = 0; i < values.length; i++) bytes[i] = (byte) values[i];
        return new Stream(bytes);
    }
    private static Animation sequence(int... delays) throws Exception {
        Constructor<Animation> c = Animation.class.getDeclaredConstructor(); c.setAccessible(true);
        Animation a = c.newInstance(); a.frameCount = delays.length;
        Field f = Animation.class.getDeclaredField("frameDelays"); f.setAccessible(true); f.set(a, delays);
        return a;
    }
    private static Entity walker(int depth) {
        Entity e = new Entity();
        e.x = 1344; e.y = 1344; e.smallXYIndex = depth;
        for (int i = 0; i < depth; i++) { e.smallX[i] = 11; e.smallY[i] = 10; }
        e.facingDirection = 1536;
        return e;
    }

    // anInt1543/1545 start tile, anInt1544/1546 end tile, anInt1547/1548 start/end cycle (offset + loopCycle), anInt1549 direction;
    // the real update dispatcher approaches the start tile before anInt1547, interpolates until anInt1548, then walks again.
    @Test public void forcedMovementMaskDrivesApproachInterpolationAndRelease() throws Exception {
        Game g = game(); Game.loopCycle = 100;
        Player p = new Player(); p.setPos(5, 5, true);
        p.smallXYIndex = 2; p.stepsQueuedBeforeAnimation = 2;
        Stream s = packet(118, 108, 114, 104, 130, 0, 0, 134, 127);
        g.decodePlayerUpdateMask(0x400, 1, s, p);
        assertEquals(9, s.currentOffset);
        assertEquals(10, p.forceMoveStartX); assertEquals(20, p.forceMoveStartY); assertEquals(14, p.forceMoveEndX); assertEquals(24, p.forceMoveEndY);
        assertEquals(102, p.forceMoveStartCycle); assertEquals(106, p.forceMoveEndCycle); assertEquals(1, p.forceMoveDirection);
        assertEquals(0, p.smallXYIndex); assertEquals(0, p.stepsQueuedBeforeAnimation);
        int[] xs = new int[8], ys = new int[8];
        for (int c = 100; c <= 107; c++) { Game.loopCycle = c; g.advanceEntityMovementAndAnimation(p); xs[c - 100] = p.x; ys[c - 100] = p.y; }
        // cycles 100,101: approach start tile centre (10*128+64, 20*128+64) by remaining ticks
        assertEquals(1024, xs[0]); assertEquals(1344, xs[1]); assertEquals(2624, ys[1]);
        // 102..106: linear between start and end tile centres over anInt1548-anInt1547 ticks
        assertEquals(1344, xs[2]); assertEquals(1600, xs[4]); assertEquals(1856, xs[6]);
        assertEquals(2624, ys[2]); assertEquals(2880, ys[4]); assertEquals(3136, ys[6]);
        // 107: past the end cycle the walking path (now empty) leaves the entity at the end tile
        assertEquals(1856, xs[7]); assertEquals(3136, ys[7]);
        assertEquals(1536, p.turnDirection);
    }

    @Test public void forcedMovementDirectionValuesMapToFixedFacings() {
        Game g = new Game(); Game.loopCycle = 100;
        int[] expected = {1024, 1536, 0, 512, 777};
        for (int d = 0; d <= 4; d++) {
            Entity e = new Entity(); e.forceMoveStartX = 10; e.forceMoveStartY = 10; e.forceMoveStartCycle = 105; e.forceMoveEndCycle = 110;
            e.x = 1344; e.y = 1344; e.turnDirection = 777; e.forceMoveDirection = d;
            g.advanceEntityToForcedMovementStart(e);
            assertEquals(expected[d], e.turnDirection);
        }
    }

    // anInt1537: stamped with loopCycle when an entity is carried in this cycle's list; removal candidates with a stale stamp are dropped.
    @Test public void anInt1537KeepsOnlyEntitiesUpdatedThisCycle() {
        for (int readd = 0; readd <= 1; readd++) {
            Game g = new Game(); Game.loopCycle = 731;
            g.myPlayer = new Player(); g.myPlayer.setPos(60, 60, true);
            int[] ids = {41, 7, 93, 12};
            g.playerCount = ids.length; g.removedEntityCount = 0; g.maskUpdateCount = 0;
            for (int n = 0; n < ids.length; n++) { g.playerIndices[n] = ids[n]; Player q = new Player(); q.setPos(50 + n, 60, true); q.lastUpdateCycle = 730; g.playerArray[ids[n]] = q; }
            Stream s = readd == 0
                ? ExistingPlayerMovementBitsTest.bits(1, 0, 8, 4, 1, 1, 2, 3, 1, 0, 1, 0, 1, 0, 11, 2047)
                : ExistingPlayerMovementBitsTest.bits(1, 0, 8, 4, 1, 1, 2, 3, 1, 0, 1, 0, 1, 0, 11, 41, 1, 0, 1, 1, 5, 0, 5, 0, 11, 2047);
            g.updatePlayers(s.buffer.length, s);
            assertEquals(731, g.playerArray[7].lastUpdateCycle);
            if (readd == 0) assertNull(g.playerArray[41]);
            else { assertNotNull(g.playerArray[41]); assertEquals(731, g.playerArray[41].lastUpdateCycle); }
        }
    }

    // anInt1538/anInt1539: one-shot face target in half-tile world units: local = (v - 2*base) * 64.
    @Test public void anInt1538And1539AreOneShotHalfTileFaceTarget() throws Exception {
        Game g = game(); g.baseX = 100; g.baseY = 50;
        Player p = new Player(); p.x = 1344; p.y = 1344; p.turnDirection = 5;
        Stream face = packet(97, 0, 121, 0);
        g.decodePlayerUpdateMask(2, 1, face, p);
        assertEquals(4, face.currentOffset); assertEquals(225, p.faceHalfTileX); assertEquals(121, p.faceHalfTileY);
        g.updateEntityTurning(p);
        assertEquals((int) (Math.atan2(1344 - 1600, 0) * 325.94900000000001D) & 0x7ff, p.turnDirection); assertEquals(0, p.faceHalfTileX); assertEquals(0, p.faceHalfTileY);
        Entity off = new Entity(); off.x = 1344; off.y = 1344; off.turnDirection = 5; off.faceHalfTileX = 225; off.faceHalfTileY = 123;
        g.updateEntityTurning(off);
        assertEquals((int) (Math.atan2(-256, -128) * 325.94900000000001D) & 0x7ff, off.turnDirection);
        // even value = tile edge, exactly on the entity: no turn, still consumed
        Entity same = new Entity(); same.x = 1536; same.y = 1344; same.turnDirection = 5; same.faceHalfTileX = 224; same.faceHalfTileY = 121;
        g.updateEntityTurning(same);
        assertEquals(5, same.turnDirection); assertEquals(0, same.faceHalfTileX);
        // counterexample: while walking (queue, no deferred ticks) the target is kept for later
        Entity walking = walker(1); walking.turnDirection = 5; walking.faceHalfTileX = 225; walking.faceHalfTileY = 121;
        g.updateEntityTurning(walking);
        assertEquals(5, walking.turnDirection); assertEquals(225, walking.faceHalfTileX);
    }

    // anInt1542: queue depth captured when an action animation starts, counted down as those steps are walked; method446 clears it with the queue.
    @Test public void anInt1542CountsStepsQueuedBeforeTheAnimation() throws Exception {
        Game g = game(); Game.loopCycle = 100;
        Animation a = sequence(5, 5); a.priority = 5; a.movingPrecedence = 1; a.idlePrecedence = 1;
        Animation.anims = new Animation[]{a};
        Player p = new Player(); p.smallXYIndex = 3;
        g.decodePlayerUpdateMask(8, 1, packet(0, 0, 0), p);
        assertEquals(0, p.anim); assertEquals(3, p.stepsQueuedBeforeAnimation); assertEquals(0, p.animLoopCount);
        Entity w = walker(2); w.smallX[1] = 10; w.smallX[0] = 11; w.stepsQueuedBeforeAnimation = 2;
        w.x = 1340; g.advanceEntityAlongWalkingPath(w);
        assertEquals(1, w.smallXYIndex); assertEquals(1, w.stepsQueuedBeforeAnimation);
        Entity z = walker(1); z.x = 1340; z.smallX[0] = 10; z.stepsQueuedBeforeAnimation = 0;
        g.advanceEntityAlongWalkingPath(z);
        assertEquals(0, z.stepsQueuedBeforeAnimation);
        // while >0 a movingPrecedence==1 animation is held (delay 1) unless forced movement runs
        Entity h = new Entity(); h.anim = 0; h.animationDelay = 0; h.stepsQueuedBeforeAnimation = 1;
        g.updateEntityAnimations(h); assertEquals(1, h.animationDelay); assertEquals(0, h.animFrameTimer);
        Entity r = new Entity(); r.anim = 0; r.animationDelay = 0; r.stepsQueuedBeforeAnimation = 0;
        g.updateEntityAnimations(r); assertEquals(0, r.animationDelay); assertEquals(1, r.animFrameTimer);
        Entity f = new Entity(); f.anim = 0; f.stepsQueuedBeforeAnimation = 1; f.forceMoveEndCycle = 100;
        g.updateEntityAnimations(f); assertEquals(1, f.animFrameTimer);
        Entity m = walker(3); m.stepsQueuedBeforeAnimation = 2; m.deferredMovementTicks = 4; m.x = 777;
        m.resetPath();
        assertEquals(0, m.smallXYIndex); assertEquals(0, m.stepsQueuedBeforeAnimation); assertEquals(4, m.deferredMovementTicks); assertEquals(777, m.x);
    }

    // anInt1530 counts completed loops of the action animation; reaching maxLoops ends it. aBoolean1541 mirrors the running animation's extendByYaw.
    @Test public void anInt1530CountsLoopsAndABoolean1541MirrorsExtendByYaw() throws Exception {
        Game g = game(); Game.loopCycle = 100;
        Animation a = sequence(1, 1); a.loopBackFrames = 2; a.maxLoops = 3; a.extendByYaw = true;
        Animation b = sequence(1, 1); b.loopBackFrames = 2; b.extendByYaw = false;
        Animation.anims = new Animation[]{a, b};
        Entity e = new Entity(); e.anim = 0;
        int[] loops = new int[8];
        for (int c = 0; c < 8; c++) { g.updateEntityAnimations(e); loops[c] = e.animLoopCount; if (c == 0) assertTrue(e.animExtendByYaw); }
        assertArrayEquals(new int[]{0, 0, 1, 1, 2, 2, 3, 3}, loops);
        assertEquals(-1, e.anim);
        g.updateEntityAnimations(e); assertFalse(e.animExtendByYaw);
        Entity u = new Entity(); u.anim = 0; a.maxLoops = 99;
        for (int c = 0; c < 8; c++) g.updateEntityAnimations(u);
        assertEquals(3, u.animLoopCount); assertEquals(0, u.anim);
        Entity n = new Entity(); n.anim = 1; g.updateEntityAnimations(n); assertFalse(n.animExtendByYaw);
        Entity d = new Entity(); d.anim = 0; d.animationDelay = 2; d.animExtendByYaw = true; g.updateEntityAnimations(d); assertFalse(d.animExtendByYaw);
        // a new animation from the mask resets the loop count
        Player p = new Player(); p.anim = 1; p.animLoopCount = 7; a.priority = 9;
        g.decodePlayerUpdateMask(8, 1, packet(0, 0, 0), p);
        assertEquals(0, p.animLoopCount);
    }

    // aBooleanArray1553: per-waypoint run flag shifted with smallX/smallY; the target waypoint's flag doubles the step.
    @Test public void aBooleanArray1553IsPerWaypointRunFlag() {
        Game g = new Game();
        Entity e = new Entity(); e.setPos(10, 10, true); e.facingDirection = 1536;
        e.moveInDir(true, 4); e.moveInDir(false, 1);
        assertEquals(2, e.smallXYIndex);
        assertFalse(e.pathRunFlags[0]); assertTrue(e.pathRunFlags[1]);
        assertEquals(11, e.smallX[0]); assertEquals(11, e.smallY[0]); assertEquals(11, e.smallX[1]); assertEquals(10, e.smallY[1]);
        int x0 = e.x;
        g.advanceEntityAlongWalkingPath(e); // target is waypoint 1 (run) -> 8 units
        assertEquals(x0 + 8, e.x);
        Entity w = walker(1); w.pathRunFlags[0] = false; g.advanceEntityAlongWalkingPath(w); assertEquals(1348, w.x);
        Entity r = walker(1); r.pathRunFlags[0] = true; g.advanceEntityAlongWalkingPath(r); assertEquals(1352, r.x);
        e.setPos(12, 11, false);
        assertFalse(e.pathRunFlags[0]); assertTrue(e.pathRunFlags[2]);
    }

    // moveInDir(flag, i): i is one of 8 directions from the head tile, flag is the step's run flag.
    @Test public void moveInDirDirectionsAndRunFlag() {
        int[] dx = {-1, 0, 1, -1, 1, -1, 0, 1}, dy = {1, 1, 1, 0, 0, -1, -1, -1};
        for (int d = 0; d < 8; d++) {
            Entity e = new Entity(); e.setPos(20, 30, true);
            e.moveInDir(d % 2 == 0, d);
            assertEquals(20 + dx[d], e.smallX[0]); assertEquals(30 + dy[d], e.smallY[0]);
            assertEquals(20, e.smallX[1]); assertEquals(1, e.smallXYIndex); assertEquals(d % 2 == 0, e.pathRunFlags[0]);
        }
        Entity none = new Entity(); none.setPos(20, 30, true); none.moveInDir(false, 8);
        assertEquals(20, none.smallX[0]); assertEquals(30, none.smallY[0]);
        Entity full = new Entity(); full.setPos(20, 30, true);
        for (int k = 0; k < 12; k++) full.moveInDir(false, 4);
        assertEquals(9, full.smallXYIndex); assertEquals(32, full.smallX[0]);
    }

    // setPos(i, j, flag): i/j tile; flag forces a snap; otherwise a move within 8 tiles of the head is queued.
    @Test public void setPosSnapsOrQueuesByDistanceAndFlag() {
        Entity e = new Entity(); e.setPos(20, 30, true);
        assertEquals(0, e.smallXYIndex); assertEquals(20 * 128 + 64, e.x); assertEquals(30 * 128 + 64, e.y);
        e.setPos(28, 22, false);
        assertEquals(1, e.smallXYIndex); assertEquals(28, e.smallX[0]); assertEquals(22, e.smallY[0]); assertEquals(20 * 128 + 64, e.x);
        e.setPos(37, 22, false); // 9 tiles from head: snap
        assertEquals(0, e.smallXYIndex); assertEquals(37 * 128 + 64, e.x);
        e.setPos(37, 13, false); // dy = -9: snap
        assertEquals(0, e.smallXYIndex); assertEquals(13 * 128 + 64, e.y);
        e.setPos(36, 13, true); // close but flagged: snap
        assertEquals(0, e.smallXYIndex); assertEquals(36 * 128 + 64, e.x);
        e.tileSize = 2; e.setPos(36, 13, true); assertEquals(36 * 128 + 128, e.x);
    }
}
