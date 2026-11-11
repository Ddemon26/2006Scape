import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Pass 239: the server-driven (cutscene) camera of Game, original identifiers only. Packet 166 writes cutsceneCameraTileX..1102 and
 * cutsceneCameraActive, packet 177 writes cutsceneLookTileX..999 and cutsceneCameraActive, packet 107 clears cutsceneCameraActive; calcCameraPos moves the
 * camera toward them once per cycle. Each field is isolated by changing only that field and observing the camera.
 */
public class CutsceneCameraTest {
    private Player priorPlayer;
    private int priorCounter;
    private final List<Socket> sockets = new ArrayList<Socket>();
    private ServerSocket listener;

    @Before public void saveStatics() { priorPlayer = Game.myPlayer; priorCounter = Game.junkPacketCounter; Game.junkPacketCounter = 0; }

    @After public void restoreStatics() throws Exception {
        Game.myPlayer = priorPlayer; Game.junkPacketCounter = priorCounter;
        for (Socket s : sockets) try { s.close(); } catch (Exception e) { }
        if (listener != null) listener.close();
    }

    private static Game game() throws Exception {
        Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        Game g = (Game) ((sun.misc.Unsafe) unsafeField.get(null)).allocateInstance(Game.class);
        g.intGroundArray = new int[4][105][105];
        g.byteGroundArray = new byte[4][105][105];
        return g;
    }

    private static void flatGround(Game g, int height) {
        for (int p = 0; p < 4; p++) for (int x = 0; x < 105; x++) for (int y = 0; y < 105; y++) g.intGroundArray[p][x][y] = height;
    }

    /** Position fields so one calcCameraPos call lands exactly on the target (rate 1000 per mille, no constant step). */
    private static void snapPosition(Game g, int tileX, int tileY, int height) {
        g.cutsceneCameraTileX = tileX; g.cutsceneCameraTileY = tileY; g.cutsceneCameraHeight = height; g.cutsceneCameraMoveStep = 0; g.cutsceneCameraMoveRate = 1000;
    }

    // ------------------------------------------------ position target (cutsceneCameraTileX, cutsceneCameraTileY, cutsceneCameraHeight)

    @Test public void positionTargetTilesSetTheTileCentreOfEachWorldAxisIndependently() throws Exception {
        Game g = game(); flatGround(g, 500);
        snapPosition(g, 40, 30, 0);
        g.calcCameraPos();
        assertEquals(40 * 128 + 64, g.xCameraPos);
        assertEquals(30 * 128 + 64, g.yCameraPos);
        assertEquals(500, g.zCameraPos);
        snapPosition(g, 41, 30, 0);                  // only cutsceneCameraTileX changed: only x moves
        g.calcCameraPos();
        assertEquals(41 * 128 + 64, g.xCameraPos);
        assertEquals(30 * 128 + 64, g.yCameraPos);
        assertEquals(500, g.zCameraPos);
        snapPosition(g, 41, 7, 0);                   // only cutsceneCameraTileY changed: only y moves
        g.calcCameraPos();
        assertEquals(41 * 128 + 64, g.xCameraPos);
        assertEquals(7 * 128 + 64, g.yCameraPos);
        assertEquals(500, g.zCameraPos);
    }

    @Test public void positionHeightIsSubtractedFromTheGroundHeightAtTheTarget() throws Exception {
        Game g = game();
        for (int x = 0; x < 105; x++) for (int y = 0; y < 105; y++) { g.intGroundArray[0][x][y] = 7 * x; g.intGroundArray[1][x][y] = 1000 + 7 * x; }
        snapPosition(g, 40, 30, 0);
        g.calcCameraPos();
        assertEquals(7 * 40 + 3, g.zCameraPos);      // tile centre is half way between x=40 and x=41 columns (7*40.5 truncated)
        snapPosition(g, 40, 30, 120);                // only cutsceneCameraHeight changed
        g.calcCameraPos();
        assertEquals(7 * 40 + 3 - 120, g.zCameraPos);
        assertEquals(40 * 128 + 64, g.xCameraPos);
        assertEquals(30 * 128 + 64, g.yCameraPos);
        snapPosition(g, 40, 30, 120);                // the ground follows the current plane
        g.plane = 1;
        g.calcCameraPos();
        assertEquals(1000 + 7 * 40 + 3 - 120, g.zCameraPos);
        snapPosition(g, 40, 30, 3000);               // a larger height puts the camera higher (z becomes more negative)
        g.plane = 0;
        g.calcCameraPos();
        assertEquals(7 * 40 + 3 - 3000, g.zCameraPos);
    }

    @Test public void positionMovesByConstantStepWhenRateIsZeroOnAllThreeAxesBothDirections() throws Exception {
        Game g = game(); flatGround(g, 500);
        g.xCameraPos = 1000; g.yCameraPos = 9000; g.zCameraPos = 100;
        g.cutsceneCameraTileX = 20; g.cutsceneCameraTileY = 10; g.cutsceneCameraHeight = 0; g.cutsceneCameraMoveStep = 7; g.cutsceneCameraMoveRate = 0;
        g.calcCameraPos();                           // targets: x 2624 (above), y 1344 (below), z 500 (above)
        assertEquals(1007, g.xCameraPos);
        assertEquals(8993, g.yCameraPos);
        assertEquals(107, g.zCameraPos);
        g.xCameraPos = 2620; g.yCameraPos = 1350; g.zCameraPos = 496;   // within one step of the target: clamp, never overshoot
        g.calcCameraPos();
        assertEquals(2624, g.xCameraPos);
        assertEquals(1343 + 1, g.yCameraPos);
        assertEquals(500, g.zCameraPos);
        g.xCameraPos = 2624; g.calcCameraPos();      // at the target: stays
        assertEquals(2624, g.xCameraPos);
    }

    @Test public void positionStepAndRateAreDistinctAndTheRateIsPerMilleOfTheRemainingDistance() throws Exception {
        Game g = game(); flatGround(g, 500);
        g.xCameraPos = 2624 - 2000; g.yCameraPos = 1344; g.zCameraPos = 500;
        g.cutsceneCameraTileX = 20; g.cutsceneCameraTileY = 10; g.cutsceneCameraHeight = 0; g.cutsceneCameraMoveStep = 3; g.cutsceneCameraMoveRate = 50;
        g.calcCameraPos();                           // 3 + 2000 * 50 / 1000, not 50 + 2000 * 3 / 1000
        assertEquals(2624 - 2000 + 3 + 100, g.xCameraPos);
        g.xCameraPos = 2624 + 2000;                  // below the camera: the same size of step toward the target
        g.calcCameraPos();
        assertEquals(2624 + 2000 - 103, g.xCameraPos);
        g.xCameraPos = 2624 - 2000; g.cutsceneCameraMoveStep = 0; g.cutsceneCameraMoveRate = 50;     // rate alone
        g.calcCameraPos();
        assertEquals(2624 - 2000 + 100, g.xCameraPos);
        g.xCameraPos = 2624 - 5; g.cutsceneCameraMoveStep = 0; g.cutsceneCameraMoveRate = 50;        // 5 * 50 / 1000 rounds down to 0: no movement
        g.calcCameraPos();
        assertEquals(2624 - 5, g.xCameraPos);
        g.xCameraPos = 2624 - 2000; g.cutsceneCameraMoveStep = 100; g.cutsceneCameraMoveRate = 0;    // step alone
        g.calcCameraPos();
        assertEquals(2624 - 2000 + 100, g.xCameraPos);
        g.xCameraPos = 2624 - 2000; g.cutsceneCameraMoveStep = 0; g.cutsceneCameraMoveRate = 1000;   // rate 1000 covers the whole distance
        g.calcCameraPos();
        assertEquals(2624, g.xCameraPos);
        g.xCameraPos = 2624 - 2000; g.cutsceneCameraMoveStep = 0; g.cutsceneCameraMoveRate = 0;      // both zero: the camera never moves
        g.calcCameraPos();
        assertEquals(2624 - 2000, g.xCameraPos);
    }

    // ------------------------------------------------ look-at target (cutsceneLookTileX..cutsceneLookTurnRate)

    /** Camera fixed at tile (10, 10), height 0 over flat ground 0; then the look-at point changes. */
    private static Game lookGame(int lookTileX, int lookTileY, int lookHeight, int step, int rate) throws Exception {
        Game g = game(); flatGround(g, 0);
        snapPosition(g, 10, 10, 0);
        g.cutsceneLookTileX = lookTileX; g.cutsceneLookTileY = lookTileY; g.cutsceneLookHeight = lookHeight; g.cutsceneLookTurnStep = step; g.cutsceneLookTurnRate = rate;
        return g;
    }

    @Test public void lookTilesDecideTheYawWorldYIsZeroAndWorldXIsMinusQuarterTurn() throws Exception {
        int[][] cases = {{10, 20, 0}, {20, 10, 1537}, {0, 10, 511}, {10, 0, 1025}};   // look tile x, y -> yaw (2048 = full turn; 325.949 truncates quarter turns to 511/1537 and the half turn to 1025)
        for (int[] c : cases) {
            Game g = lookGame(c[0], c[1], 0, 0, 1000);
            g.calcCameraPos();
            assertEquals("yaw toward " + c[0] + "," + c[1], c[2], g.xCameraCurve);
            assertEquals("level look gives the minimum pitch", 128, g.yCameraCurve);
        }
        Game g = lookGame(10, 20, 0, 0, 1000);       // look tile (10, 20): yaw 0
        g.calcCameraPos();
        g.cutsceneLookTileX = 20;                             // x alone moved: (20, 20) is a diagonal, an eighth of a turn from +y toward -x (-255)
        g.calcCameraPos();
        assertEquals(1793, g.xCameraCurve);
        g.cutsceneLookTileY = 10;                             // y alone moved: (20, 10) is the quarter turn
        g.calcCameraPos();
        assertEquals(1537, g.xCameraCurve);
        g.cutsceneLookTileY = 0; g.cutsceneLookTileX = 10;
        g.calcCameraPos();
        assertEquals(1025, g.xCameraCurve);
    }

    @Test public void lookHeightMinusCameraHeightOverTheHorizontalHypotenuseGivesPitchClampedTo128To383() throws Exception {
        // dz = camera height (cutsceneCameraHeight) - look height (cutsceneLookHeight) over flat ground; d = horizontal distance to the look point
        Game g = lookGame(10, 20, 0, 0, 1000);
        snapPosition(g, 10, 10, 1280);               // camera 1280 above ground; look point on the ground, 1280 away
        g.calcCameraPos();
        assertEquals(255, g.yCameraCurve);           // 45 degrees = 255.99 units (atan2 * 325.949 truncated)
        g.cutsceneLookHeight = 1280;                           // look point as high as the camera: level
        g.calcCameraPos();
        assertEquals(128, g.yCameraCurve);           // level (0) clamps up to 128
        g.cutsceneLookHeight = 0; g.cutsceneLookTileX = 13; g.cutsceneLookTileY = 14; // dx 3 tiles, dy 4 tiles: hypotenuse 640 (neither 384 nor 512)
        snapPosition(g, 10, 10, 640);
        g.calcCameraPos();
        assertEquals(255, g.yCameraCurve);
        snapPosition(g, 10, 10, 12000);              // very steep look-down clamps to 383
        g.cutsceneLookTileX = 10; g.cutsceneLookTileY = 20;
        g.calcCameraPos();
        assertEquals(383, g.yCameraCurve);
        snapPosition(g, 10, 10, 0); g.cutsceneLookHeight = 5000;      // look point above the camera: negative angle wraps and clamps to 383
        g.calcCameraPos();
        assertEquals(383, g.yCameraCurve);
        snapPosition(g, 10, 10, 640); g.cutsceneLookHeight = 0;       // 640 over 1280: about 26.6 degrees = 151 units
        g.calcCameraPos();
        assertEquals(151, g.yCameraCurve);
        g.cutsceneLookHeight = 320;                            // camera 640, look 320: dz 320 over 1280 = 14 degrees = 80 units, clamps to 128
        g.calcCameraPos();
        assertEquals(128, g.yCameraCurve);
    }

    @Test public void lookTurnStepAndRateMoveYawAndPitchTogetherAndNeverOvershoot() throws Exception {
        Game g = lookGame(10, 20, 0, 0, 0);          // yaw target 0, pitch target 255
        g.cutsceneCameraHeight = 1280; snapPosition(g, 10, 10, 1280); g.cutsceneLookTurnStep = 0; g.cutsceneLookTurnRate = 0;
        g.xCameraCurve = 1100; g.yCameraCurve = 300;
        g.calcCameraPos();                           // no step and no rate: nothing moves
        assertEquals(1100, g.xCameraCurve); assertEquals(300, g.yCameraCurve);
        g.cutsceneLookTurnStep = 10;                             // constant step 10 on both axes: pitch down toward 255, yaw forward through the wrap toward 0
        g.calcCameraPos();
        assertEquals(1110, g.xCameraCurve); assertEquals(290, g.yCameraCurve);
        g.xCameraCurve = 100; g.yCameraCurve = 200;  // yaw target 0 is behind (negative delta), pitch target 255 is ahead
        g.cutsceneLookTurnStep = 30;
        g.calcCameraPos();
        assertEquals(70, g.xCameraCurve); assertEquals(230, g.yCameraCurve);
        g.xCameraCurve = 20; g.yCameraCurve = 250;   // overshoot: both axes clamp at their target
        g.calcCameraPos();
        assertEquals(0, g.xCameraCurve); assertEquals(255, g.yCameraCurve);
        g.cutsceneLookTurnStep = 3; g.cutsceneLookTurnRate = 50; g.xCameraCurve = 200; g.yCameraCurve = 135;
        g.calcCameraPos();                           // pitch: 3 + (255-135)*50/1000 = 9 ; yaw: 3 + 200*50/1000 = 13 toward 0
        assertEquals(144, g.yCameraCurve); assertEquals(187, g.xCameraCurve);
        g.cutsceneLookTurnStep = 0; g.cutsceneLookTurnRate = 1000; g.xCameraCurve = 1700; g.yCameraCurve = 135;   // rate 1000 lands exactly on the target
        g.calcCameraPos();
        assertEquals(0, g.xCameraCurve); assertEquals(255, g.yCameraCurve);
    }

    @Test public void yawTakesTheShortWayAroundTheCircleAndSnapsWhenAStepOvershoots() throws Exception {
        Game g = lookGame(0, 10, 0, 100, 0);         // yaw target 511 (quarter turn, truncated)
        g.xCameraCurve = 1900;                       // 660 ahead going up through 2047 -> 0: shorter than 1388 the other way
        g.calcCameraPos();
        assertEquals(1900 + 100 & 0x7ff, g.xCameraCurve);
        g.xCameraCurve = 600;                        // target is 88 behind: a 100 step overshoots, the camera snaps to the target
        g.calcCameraPos();
        assertEquals(511, g.xCameraCurve);
        g.xCameraCurve = 700;                        // target is 188 behind: a 100 step does not overshoot
        g.calcCameraPos();
        assertEquals(600, g.xCameraCurve);
        g.xCameraCurve = 400;                        // target is 112 ahead: a 100 step does not overshoot
        g.calcCameraPos();
        assertEquals(500, g.xCameraCurve);
        g.xCameraCurve = 450;                        // target is 62 ahead: a 100 step overshoots and snaps
        g.calcCameraPos();
        assertEquals(511, g.xCameraCurve);
        g.xCameraCurve = 1535;                 // exactly half a turn away (delta -1024 is not wrapped): turns the negative way
        g.calcCameraPos();
        assertEquals(1535 - 100, g.xCameraCurve);
    }

    @Test public void lookIsAimedFromTheCameraPositionAfterThisCyclesMove() throws Exception {
        Game g = game(); flatGround(g, 0);
        snapPosition(g, 10, 10, 0);
        g.xCameraPos = 30 * 128 + 64;                // camera starts beyond the look tile on x and moves to tile 10 this cycle
        g.cutsceneLookTileX = 20; g.cutsceneLookTileY = 10; g.cutsceneLookHeight = 0; g.cutsceneLookTurnStep = 0; g.cutsceneLookTurnRate = 1000;
        g.calcCameraPos();
        assertEquals(10 * 128 + 64, g.xCameraPos);
        assertEquals(1537, g.xCameraCurve);          // looks along +x from the new position (from the old one it would be 512)
        g.cutsceneCameraTileX = 40;                            // the camera now moves to tile 40, beyond the look tile
        g.calcCameraPos();
        assertEquals(40 * 128 + 64, g.xCameraPos);
        assertEquals(511, g.xCameraCurve);
    }

    // ------------------------------------------------ packets 166, 177, 107 through parsePacket

    private void deliver(Game g, int opcode, byte[] payload) throws Exception { deliverSized(g, opcode, payload, payload.length); }

    private void deliverSized(Game g, int opcode, byte[] payload, int size) throws Exception {
        if (listener == null) listener = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"));
        Socket client = new Socket(InetAddress.getByName("127.0.0.1"), listener.getLocalPort());
        Socket server = listener.accept();
        sockets.add(client); sockets.add(server);
        RSSocket rs = new RSSocket(null, client);
        server.getOutputStream().write(payload); server.getOutputStream().flush();
        long end = System.currentTimeMillis() + 5000;
        while (rs.available() < payload.length && System.currentTimeMillis() < end) Thread.sleep(5);
        g.socketStream = rs;
        g.pktType = opcode; g.pktSize = size;
        assertTrue(g.parsePacket());
        assertEquals(-1, g.pktType);
    }

    private static Game packetGame() throws Exception {
        Game g = game(); flatGround(g, 500);
        g.inStream = new Stream(new byte[6000]);
        Field shake = Game.class.getDeclaredField("shakeChannelActive"); shake.setAccessible(true); shake.set(g, new boolean[5]);
        g.pktType = -1;
        return g;
    }

    @Test public void packet166WritesThePositionTargetInWireOrderAndSnapsOnlyWhenTheRateIsAtLeast100() throws Exception {
        Game g = packetGame();
        g.xCameraPos = 111; g.yCameraPos = 222; g.zCameraPos = 333;
        assertFalse(g.cutsceneCameraActive);
        deliver(g, 166, new byte[] {50, 60, 0x01, 0x2c, 9, 99});   // x 50, y 60, height 300 (big-endian short), step 9, rate 99
        assertTrue(g.cutsceneCameraActive);
        assertEquals(50, g.cutsceneCameraTileX); assertEquals(60, g.cutsceneCameraTileY); assertEquals(300, g.cutsceneCameraHeight);
        assertEquals(9, g.cutsceneCameraMoveStep); assertEquals(99, g.cutsceneCameraMoveRate);
        assertEquals(111, g.xCameraPos); assertEquals(222, g.yCameraPos); assertEquals(333, g.zCameraPos);   // rate 99: no snap
        deliver(g, 166, new byte[] {51, 61, 0x02, 0x58, 8, 100});  // rate 100 snaps to the tile centre / ground minus height
        assertEquals(51, g.cutsceneCameraTileX); assertEquals(61, g.cutsceneCameraTileY); assertEquals(600, g.cutsceneCameraHeight);
        assertEquals(8, g.cutsceneCameraMoveStep); assertEquals(100, g.cutsceneCameraMoveRate);
        assertEquals(51 * 128 + 64, g.xCameraPos); assertEquals(61 * 128 + 64, g.yCameraPos); assertEquals(500 - 600, g.zCameraPos);
        assertEquals(0, g.cutsceneLookTileX); assertEquals(0, g.cutsceneLookTurnRate);   // the look fields are not touched by 166
        deliver(g, 166, new byte[] {1, 2, (byte) 0xff, (byte) 0xff, (byte) 255, (byte) 255});   // unsigned bytes and short
        assertEquals(1, g.cutsceneCameraTileX); assertEquals(2, g.cutsceneCameraTileY); assertEquals(65535, g.cutsceneCameraHeight);
        assertEquals(255, g.cutsceneCameraMoveStep); assertEquals(255, g.cutsceneCameraMoveRate);
    }

    @Test public void packet177WritesTheLookTargetInWireOrderAndSnapsPitchAndYawOnlyWhenTheRateIsAtLeast100() throws Exception {
        Game g = packetGame();
        g.xCameraPos = 10 * 128 + 64; g.yCameraPos = 10 * 128 + 64; g.zCameraPos = 500 - 1280;
        g.yCameraCurve = 321; g.xCameraCurve = 654;
        assertFalse(g.cutsceneCameraActive);
        deliver(g, 177, new byte[] {10, 20, 0, 0, 4, 99});          // look tile (10, 20), height 0, step 4, rate 99
        assertTrue(g.cutsceneCameraActive);
        assertEquals(10, g.cutsceneLookTileX); assertEquals(20, g.cutsceneLookTileY); assertEquals(0, g.cutsceneLookHeight);
        assertEquals(4, g.cutsceneLookTurnStep); assertEquals(99, g.cutsceneLookTurnRate);
        assertEquals(321, g.yCameraCurve); assertEquals(654, g.xCameraCurve);   // rate 99: no snap
        deliver(g, 177, new byte[] {20, 10, 0x01, 0x2c, 6, 100});   // look tile (20, 10), height 300, rate 100 snaps
        assertEquals(20, g.cutsceneLookTileX); assertEquals(10, g.cutsceneLookTileY); assertEquals(300, g.cutsceneLookHeight);
        assertEquals(6, g.cutsceneLookTurnStep); assertEquals(100, g.cutsceneLookTurnRate);
        assertEquals(1537, g.xCameraCurve);
        assertEquals((int) (Math.atan2(500 - 300 - (500 - 1280), 1280) * 325.949) & 0x7ff, g.yCameraCurve);
        deliver(g, 177, new byte[] {10, 20, 0x00, 0x00, 0, (byte) 200});   // look at the ground 1280 ahead of a camera 1280 up: 45 degrees
        assertEquals(0, g.xCameraCurve);
        assertEquals(255, g.yCameraCurve);
    }

    @Test public void packet177SnapHorizontalDistanceUsesBothAxes() throws Exception {
        Game g = packetGame();
        g.xCameraPos = 10 * 128 + 64; g.yCameraPos = 10 * 128 + 64; g.zCameraPos = 500 - 1920;
        deliver(g, 177, new byte[] {19, 22, 0, 0, 0, (byte) 150});    // dx 9 tiles, dy 12 tiles: hypotenuse 15 tiles = 1920, dz = 1920
        assertEquals(255, g.yCameraCurve);
        deliver(g, 177, new byte[] {19, 22, 0x0d, (byte) 0xac, 0, (byte) 150});   // look point 3500 above the ground, above the camera: negative pitch wraps and clamps to 383
        assertEquals(383, g.yCameraCurve);
        g.zCameraPos = 500;
        deliver(g, 177, new byte[] {19, 22, 0x00, 0x00, 0, (byte) 150});    // look at ground level from ground level: level
        assertEquals(128, g.yCameraCurve);
    }

    @Test public void packet107ClearsTheActiveFlagAndLeavesTheTargetsAlone() throws Exception {
        Game g = packetGame();
        deliver(g, 166, new byte[] {50, 60, 0, 10, 9, 10});
        deliver(g, 177, new byte[] {10, 20, 0, 5, 4, 10});
        assertTrue(g.cutsceneCameraActive);
        deliverSized(g, 107, new byte[] {0}, 0);   // one byte pending so parsePacket runs; opcode 107 has no payload
        assertFalse(g.cutsceneCameraActive);
        assertEquals(50, g.cutsceneCameraTileX); assertEquals(10, g.cutsceneLookTileX);
        deliverSized(g, 107, new byte[] {0}, 0);                 // clearing an inactive camera is harmless
        assertFalse(g.cutsceneCameraActive);
    }

    // ------------------------------------------------ junkPacketCounter: the opcode 77 filler packet every 1513th camera-follow update

    private static Game followGame() throws Exception {
        Game g = game();
        Field keys = RSApplet.class.getDeclaredField("keyArray");
        keys.setAccessible(true);
        keys.set(g, new int[128]);
        g.stream = new Stream(new byte[256]);
        g.stream.encryption = new ISAACRandomGen(new int[] {1, 2, 3, 4});
        g.cameraTerrainClearance = 32768;
        Game.myPlayer = new Player(); Game.myPlayer.x = 50 * 128 + 64; Game.myPlayer.y = 50 * 128 + 64;
        return g;
    }

    @Test public void everyFifteenHundredAndThirteenthCameraFollowUpdateSendsTheOpcode77FillerPacket() throws Exception {
        Game g = followGame();
        ISAACRandomGen mirror = new ISAACRandomGen(new int[] {1, 2, 3, 4});
        for (int i = 1; i <= 1512; i++) { g.updateCameraFollow(); assertEquals(i, Game.junkPacketCounter); }
        assertEquals("nothing sent for 1512 updates", 0, g.stream.currentOffset);
        g.updateCameraFollow();                       // the 1513th
        assertEquals(0, Game.junkPacketCounter);
        byte[] b = g.stream.buffer; int n = g.stream.currentOffset;
        assertEquals((byte) (77 + mirror.getNextKey()), b[0]);
        int length = b[1] & 0xff;
        assertTrue("payload 12 or 14 bytes, got " + length, length == 12 || length == 14);
        assertEquals(2 + length, n);
        assertEquals(101, b[3]); assertEquals(233, b[4] & 0xff);
        assertEquals(45092, (b[5] & 0xff) << 8 | b[6] & 0xff);
        int p = 7;
        if (length == 14) { assertEquals(35784, (b[7] & 0xff) << 8 | b[8] & 0xff); p = 9; }
        assertEquals(64, b[p + 1]); assertEquals(38, b[p + 2]);
        assertEquals("random bytes follow in two shorts", p + 7, n);
        g.updateCameraFollow();                       // the counter restarts at 1
        assertEquals(1, Game.junkPacketCounter); assertEquals(n, g.stream.currentOffset);
        boolean short12 = false, long14 = false;      // the optional short is chosen at random
        for (int i = 0; i < 300; i++) {
            Game.junkPacketCounter = 1512; int before = g.stream.currentOffset;
            g.updateCameraFollow();
            int len = g.stream.buffer[before + 1] & 0xff;
            short12 |= len == 12; long14 |= len == 14;
            g.stream.currentOffset = 0;
        }
        assertTrue(short12 && long14);
    }

    @Test public void counterOnlyAdvancesWithCameraFollowUpdatesAndNoPacketIsSentOneShort() throws Exception {
        Game g = followGame();
        Game.junkPacketCounter = 1511;
        g.updateCameraFollow();
        assertEquals(1512, Game.junkPacketCounter); assertEquals(0, g.stream.currentOffset);
        Game.junkPacketCounter = 1512;
        g.updateCameraFollow();
        assertEquals(0, Game.junkPacketCounter); assertTrue(g.stream.currentOffset > 0);
    }

    // ------------------------------------------------ gating: calcCameraPos only runs in mainGameProcessor while cutsceneCameraActive and loadingStage 2

    static class CountingSpy extends CameraFollowTest.CycleSpy {
        int calcCalls;
        @Override public void calcCameraPos() { calcCalls++; super.calcCameraPos(); }
    }

    private static CountingSpy gateGame(int stage, boolean active) {
        CountingSpy s = new CountingSpy();
        s.mouseDetection = new MouseDetection(s);
        s.stream = new Stream(new byte[5000]);
        s.stream.encryption = new ISAACRandomGen(new int[] {1, 2, 3, 4});
        s.loggedIn = true;
        s.intGroundArray = new int[4][105][105];
        s.byteGroundArray = new byte[4][105][105];
        s.cameraTerrainClearance = 32768;
        Game.myPlayer = new Player(); Game.myPlayer.x = 50 * 128 + 64; Game.myPlayer.y = 50 * 128 + 64;
        s.loadingStage = stage; s.cutsceneCameraActive = active;
        s.xCameraPos = 111; s.yCameraPos = 222; s.zCameraPos = 333;
        s.cutsceneCameraTileX = 40; s.cutsceneCameraTileY = 30; s.cutsceneCameraHeight = 0; s.cutsceneCameraMoveStep = 0; s.cutsceneCameraMoveRate = 1000;
        return s;
    }

    private static void run(Game g) {
        try { g.mainGameProcessor(); } catch (RuntimeException e) { /* later code needs a live session */ }
    }

    @Test public void mainGameProcessorMovesTheCameraOnlyWhileTheCutsceneCameraIsActiveInLoadingStageTwo() throws Exception {
        CountingSpy on = gateGame(2, true);
        run(on);
        assertEquals(1, on.calcCalls);
        assertEquals(40 * 128 + 64, on.xCameraPos); assertEquals(30 * 128 + 64, on.yCameraPos);
        CountingSpy off = gateGame(2, false);          // normal camera: not called
        run(off);
        assertEquals(0, off.calcCalls); assertEquals(111, off.xCameraPos); assertEquals(222, off.yCameraPos);
        CountingSpy loading = gateGame(1, true);       // active but the scene is not ready: not called
        run(loading);
        assertEquals(0, loading.calcCalls); assertEquals(111, loading.xCameraPos);
    }
}
