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
        priorCounter = Game.junkPacketCounter;
        Game.junkPacketCounter = 0;
    }

    @After public void restoreStatics() {
        Game.myPlayer = priorPlayer;
        Game.junkPacketCounter = priorCounter;
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
        game.cameraTerrainClearance = 32768;
        Game.myPlayer = new Player();
        Game.myPlayer.x = 50 * 128 + 64;
        Game.myPlayer.y = 50 * 128 + 64;
        return game;
    }

    @Test public void followsEachWorldAxisAndSnapsBeyondFiveHundredUnits() throws Exception {
        Game game = game();
        game.updateCameraFollow();
        assertEquals(Game.myPlayer.x, game.cameraFollowX);
        assertEquals(Game.myPlayer.y, game.cameraFollowY);
        Game.myPlayer.x += 160;
        game.updateCameraFollow();
        assertEquals(Game.myPlayer.x - 150, game.cameraFollowX);
        assertEquals(Game.myPlayer.y, game.cameraFollowY);
        Game.myPlayer.y += 160;
        game.updateCameraFollow();
        assertEquals(Game.myPlayer.y - 150, game.cameraFollowY);
        game.cameraDriftX = 32;
        game.cameraDriftY = -32;
        game.updateCameraFollow();
        assertEquals(Game.myPlayer.x - 150 + (150 / 16) + (173 / 16), game.cameraFollowX);
        assertEquals(Game.myPlayer.y - 150 + (118 / 16), game.cameraFollowY);
        Game.myPlayer.x += 501;
        game.updateCameraFollow();
        assertEquals(Game.myPlayer.x + 32, game.cameraFollowX);
    }

    @Test public void keyboardVelocityChangesRotationAndClampsPitch() throws Exception {
        Game game = game(); game.updateCameraFollow();
        game.keyArray[2] = 1;
        game.keyArray[3] = 1;
        game.cameraPitchBase = 380;
        game.updateCameraFollow();
        assertEquals(12, game.cameraYawVelocity);
        assertEquals(6, game.cameraYawBase);
        assertEquals(6, game.cameraPitchVelocity);
        assertEquals(383, game.cameraPitchBase);
        game.keyArray[2] = 0; game.keyArray[3] = 0;
        game.keyArray[1] = 1; game.keyArray[4] = 1;
        game.cameraPitchBase = 128;
        game.updateCameraFollow();
        assertEquals(-6, game.cameraYawVelocity);
        assertEquals(3, game.cameraYawBase);
        assertEquals(-3, game.cameraPitchVelocity);
        assertEquals(128, game.cameraPitchBase);
        game.keyArray[1] = 0; game.keyArray[4] = 0;
        game.updateCameraFollow();
        assertEquals(-3, game.cameraYawVelocity);
        assertEquals(-1, game.cameraPitchVelocity);
    }

    @Test public void nearbyGroundClearanceSetsAndSmoothsCameraDistance() throws Exception {
        Game game = game(); game.updateCameraFollow();
        assertEquals(32768, game.cameraTerrainClearance);
        for (int x = 50; x <= 51; x++) for (int y = 50; y <= 51; y++) game.intGroundArray[0][x][y] = 1000;
        game.updateCameraFollow();
        assertEquals(32768 + (0x17f00 - 32768) / 24, game.cameraTerrainClearance);
        game.cameraTerrainClearance = 0x17f00;
        for (int x = 50; x <= 51; x++) for (int y = 50; y <= 51; y++) game.intGroundArray[0][x][y] = 0;
        game.updateCameraFollow();
        assertEquals(0x17f00 - (0x17f00 - 32768) / 80, game.cameraTerrainClearance);
        game.cameraTerrainClearance = 32768;
        for (int x = 46; x <= 54; x++) for (int y = 46; y <= 54; y++) game.intGroundArray[0][x][y] = 1000;
        game.byteGroundArray[1][49][50] = 2;
        game.intGroundArray[1][49][50] = 0;
        game.updateCameraFollow();
        assertEquals(32768 + (0x17f00 - 32768) / 24, game.cameraTerrainClearance);
        game.byteGroundArray[1][49][50] = 0;
        game.cameraTerrainClearance = 32768;
        game.updateCameraFollow();
        assertEquals(32768, game.cameraTerrainClearance);
    }

    // mainGameProcessor camera cycle: opcode-86 send, drift timer and drift bounce
    static class CycleSpy extends Game {
        @Override public boolean parsePacket() { return false; }
        @Override public void loadingStages() { }
        @Override public void advancePendingSceneChanges() { }
        @Override public void advancePlayerEntities() { }
        @Override public void advanceNpcEntities() { }
        @Override public void decrementOverheadTextTimers() { }
        @Override public void dropClient() { }
    }

    private static CycleSpy cycleGame() {
        CycleSpy s = new CycleSpy();
        s.mouseDetection = new MouseDetection(s);
        s.stream = new Stream(new byte[5000]);
        s.stream.encryption = new ISAACRandomGen(new int[]{1, 2, 3, 4});
        s.loggedIn = true;
        s.focusReportedToServer = true;
        s.cameraDriftTimer = 0;
        return s;
    }

    private static void cycle(Game g) {
        try { g.mainGameProcessor(); } catch (RuntimeException e) { /* later code needs a live session */ }
    }

    /** Number of opcode-86 payloads (pitch short, yaw short +128) present in the written stream. */
    private static int cameraSends(Game g) {
        byte[] b = g.stream.buffer; int n = 0;
        for (int i = 1; i + 3 < g.stream.currentOffset; i++) {
            if (b[i] == (byte) (g.cameraPitchBase >> 8) && b[i + 1] == (byte) g.cameraPitchBase
                && b[i + 2] == (byte) (g.cameraYawBase >> 8) && b[i + 3] == (byte) (g.cameraYawBase + 128)) n++;
        }
        return n;
    }

    @Test public void arrowKeysMarkCameraMovedAndSendOpcode86AtMostEveryTwentyCycles() throws Exception {
        CycleSpy g = cycleGame();
        assertFalse(g.cameraMovePending);
        g.cameraPitchBase = 0x0123; g.cameraYawBase = 0x0456;
        cycle(g);                                    // no key, no pending: nothing sent, cooldown stays 0
        assertEquals(0, g.cameraSendCooldown); assertFalse(g.cameraMovePending); assertEquals(0, cameraSends(g));
        assertEquals(1, g.cameraDriftTimer);                 // cycle reached the drift code
        int before = g.stream.currentOffset;
        g.keyArray[3] = 1;
        cycle(g);                                    // key and no cooldown: send now, cooldown 20, flag cleared
        assertEquals(1, cameraSends(g)); assertEquals(5, g.stream.currentOffset - before);
        assertEquals(20, g.cameraSendCooldown); assertFalse(g.cameraMovePending);
        cycle(g);                                    // key still held during cooldown: pending, no send
        assertEquals(1, cameraSends(g)); assertEquals(19, g.cameraSendCooldown); assertTrue(g.cameraMovePending);
        g.keyArray[3] = 0;
        for (int i = 0; i < 18; i++) cycle(g);       // released: flag stays pending through the cooldown
        assertEquals(1, cameraSends(g)); assertEquals(1, g.cameraSendCooldown); assertTrue(g.cameraMovePending);
        cycle(g);                                    // cooldown hits 0: the pending move is sent without a key
        assertEquals(2, cameraSends(g)); assertEquals(20, g.cameraSendCooldown); assertFalse(g.cameraMovePending);
        for (int k = 1; k <= 4; k++) {               // each arrow key alone marks the camera moved
            CycleSpy h = cycleGame(); h.cameraSendCooldown = 5; h.keyArray[k] = 1;
            cycle(h);
            assertTrue("key " + k, h.cameraMovePending); assertEquals(4, h.cameraSendCooldown);
        }
        CycleSpy h = cycleGame(); h.cameraSendCooldown = 5; h.keyArray[5] = 1;   // a non-arrow key does not
        cycle(h); assertFalse(h.cameraMovePending);
    }

    @Test public void driftTimerAppliesStepsOnlyAfterFiveHundredCyclesWithIndependentAxisBits() throws Exception {
        CycleSpy g = cycleGame();
        g.cameraDriftTimer = 499; g.cameraDriftX = 10; g.cameraDriftY = 20; g.cameraYawDrift = 30;
        cycle(g);                                    // 499 -> 500: no drift yet
        assertEquals(500, g.cameraDriftTimer); assertEquals(10, g.cameraDriftX); assertEquals(20, g.cameraDriftY); assertEquals(30, g.cameraYawDrift);
        boolean[] combos = new boolean[8];
        for (int t = 0; t < 400; t++) {
            g.cameraDriftTimer = 500; g.cameraDriftX = 10; g.cameraDriftY = 20; g.cameraYawDrift = 30;
            g.cameraDriftXStep = 2; g.cameraDriftYStep = -2; g.cameraYawDriftStep = 1;
            cycle(g);
            assertEquals(0, g.cameraDriftTimer);             // the drift tick resets the timer
            int dx = g.cameraDriftX - 10, dy = g.cameraDriftY - 20, dw = g.cameraYawDrift - 30;
            assertTrue(dx == 0 || dx == 2); assertTrue(dy == 0 || dy == -2); assertTrue(dw == 0 || dw == 1);
            combos[(dx != 0 ? 1 : 0) | (dy != 0 ? 2 : 0) | (dw != 0 ? 4 : 0)] = true;
        }
        for (int c = 0; c < 8; c++) assertTrue("axis combination " + c, combos[c]);
    }

    @Test public void driftStepsBounceAtTheirBounds() throws Exception {
        int[][] cases = { // x, y, yaw, expected x step, y step, yaw step (start steps 7)
            {-51, -56, -41, 2, 2, 1}, {51, 56, 41, -2, -2, -1}, {-50, -55, -40, 7, 7, 7}, {50, 55, 40, 7, 7, 7}};
        for (int[] c : cases) {
            CycleSpy g = cycleGame();
            g.cameraDriftX = c[0]; g.cameraDriftY = c[1]; g.cameraYawDrift = c[2];
            g.cameraDriftXStep = 7; g.cameraDriftYStep = 7; g.cameraYawDriftStep = 7;
            cycle(g);
            assertEquals(c[3], g.cameraDriftXStep); assertEquals(c[4], g.cameraDriftYStep); assertEquals(c[5], g.cameraYawDriftStep);
            assertEquals(c[0], g.cameraDriftX); assertEquals(c[1], g.cameraDriftY); assertEquals(c[2], g.cameraYawDrift);
        }
        CycleSpy fresh = new CycleSpy();             // constructor starts every drift moving positive
        assertEquals(2, fresh.cameraDriftXStep); assertEquals(2, fresh.cameraDriftYStep); assertEquals(1, fresh.cameraYawDriftStep);
        assertFalse(fresh.cameraMovePending); assertEquals(0, fresh.cameraSendCooldown);
    }

    // mainGameProcessor minimap drift: timer, axis bits and bounce (rotation and zoom offsets)
    @Test public void minimapDriftTimerAppliesStepsOnlyAfterFiveHundredCyclesWithIndependentAxisBits() throws Exception {
        CycleSpy g = cycleGame();
        g.minimapDriftTimer = 499; g.minimapRotationDrift = 10; g.minimapZoomDrift = -5;
        cycle(g);                                    // 499 -> 500: no drift yet
        assertEquals(500, g.minimapDriftTimer); assertEquals(10, g.minimapRotationDrift); assertEquals(-5, g.minimapZoomDrift);
        boolean[] combos = new boolean[4];
        for (int t = 0; t < 300; t++) {
            g.minimapDriftTimer = 500; g.minimapRotationDrift = 10; g.minimapZoomDrift = -5;
            g.minimapRotationDriftStep = -2; g.minimapZoomDriftStep = 1;
            cycle(g);
            assertEquals(0, g.minimapDriftTimer);            // the drift tick resets the timer
            int dr = g.minimapRotationDrift - 10, dz = g.minimapZoomDrift + 5;
            assertTrue(dr == 0 || dr == -2); assertTrue(dz == 0 || dz == 1);
            combos[(dr != 0 ? 1 : 0) | (dz != 0 ? 2 : 0)] = true;
        }
        for (int c = 0; c < 4; c++) assertTrue("minimap axis combination " + c, combos[c]);
        g.minimapDriftTimer = 0; g.minimapRotationDrift = 10; g.minimapZoomDrift = -5; g.minimapRotationDriftStep = -2; g.minimapZoomDriftStep = 1;
        cycle(g);                                    // ordinary cycle only counts the timer up
        assertEquals(1, g.minimapDriftTimer); assertEquals(10, g.minimapRotationDrift); assertEquals(-5, g.minimapZoomDrift);
    }

    @Test public void minimapDriftStepsBounceAtTheirBounds() throws Exception {
        int[][] cases = { // rotation, zoom, expected rotation step, zoom step (start steps 7)
            {-61, -21, 2, 1}, {61, 11, -2, -1}, {-60, -20, 7, 7}, {60, 10, 7, 7}, {0, 0, 7, 7}};
        for (int[] c : cases) {
            CycleSpy g = cycleGame();
            g.minimapRotationDrift = c[0]; g.minimapZoomDrift = c[1]; g.minimapRotationDriftStep = 7; g.minimapZoomDriftStep = 7;
            cycle(g);
            assertEquals(c[2], g.minimapRotationDriftStep); assertEquals(c[3], g.minimapZoomDriftStep);
            assertEquals(c[0], g.minimapRotationDrift); assertEquals(c[1], g.minimapZoomDrift);
        }
        CycleSpy fresh = new CycleSpy();             // constructor starts both minimap drifts moving positive
        assertEquals(2, fresh.minimapRotationDriftStep); assertEquals(1, fresh.minimapZoomDriftStep);
    }

    @Test public void wheelDragAddsToYawAndPitchVelocities() throws Exception {
        Game game = game(); game.updateCameraFollow();
        int yaw = game.cameraYawBase, pitch = game.cameraPitchBase = 200;
        game.middleButtonDown = false;
        game.middleButtonDragged(4, 5);
        assertEquals(0, game.cameraYawVelocity); assertEquals(0, game.cameraPitchVelocity);
        game.middleButtonDown = true;
        game.middleButtonDragged(4, 0);
        assertEquals(12, game.cameraYawVelocity); assertEquals(0, game.cameraPitchVelocity);
        game.middleButtonDragged(0, 5);
        assertEquals(12, game.cameraYawVelocity); assertEquals(10, game.cameraPitchVelocity);
        game.updateCameraFollow();                   // no key: velocity halves, then half of it is applied
        assertEquals(6, game.cameraYawVelocity); assertEquals(5, game.cameraPitchVelocity);
        assertEquals(yaw + 3 & 0x7ff, game.cameraYawBase); assertEquals(pitch + 2, game.cameraPitchBase);
    }
}
