import java.lang.reflect.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import org.junit.*;
import static org.junit.Assert.*;

/** Pass 189: semantic tests for the camera and shake fields used by Game.drawWorldView, original identifiers only. */
public class CameraShakeFieldsTest {
    private static Field f(Class<?> c, String n) throws Exception { Field x = c.getDeclaredField(n); x.setAccessible(true); return x; }
    private static sun.misc.Unsafe unsafe() throws Exception { return (sun.misc.Unsafe) f(sun.misc.Unsafe.class, "theUnsafe").get(null); }
    private static int sfield(String n) throws Exception { return f(WorldController.class, n).getInt(null); }

    private Player savedMe; private int[] ras; private int[] savedPx; private int savedUse;
    @Before public void save() { savedMe = Game.myPlayer; ras = new int[] {DrawingArea.width, DrawingArea.height}; savedPx = DrawingArea.pixels; savedUse = Texture.useCounter; }
    @After public void restore() { Game.myPlayer = savedMe; DrawingArea.width = ras[0]; DrawingArea.height = ras[1]; DrawingArea.pixels = savedPx; Texture.useCounter = savedUse; }

    private static EntitySceneInsertionTest.TestGame newGame(boolean fixedCamera) throws Exception {
        EntitySceneInsertionTest.TestGame g = new EntitySceneInsertionTest.TestGame();
        g.byteGroundArray = new byte[4][104][104]; g.intGroundArray = new int[4][105][105];
        g.tileCentreEntityCycle = new int[104][104]; g.sceneDrawCycle = 1; g.plane = 0;
        g.worldController = new WorldController(new int[4][105][105]);
        DrawingArea.width = 8; DrawingArea.height = 8; DrawingArea.pixels = new int[64];
        g.aRSImageProducer_1165 = (RSImageProducer) unsafe().allocateInstance(RSImageProducer.class);
        f(RSApplet.class, "graphics").set(g, new java.awt.image.BufferedImage(8, 8, 1).createGraphics());
        g.aBoolean1160 = fixedCamera;
        g.xCameraPos = 1000; g.zCameraPos = 22; g.yCameraPos = 33; g.yCameraCurve = 200; g.xCameraCurve = 44;
        Constructor<Player> c = Player.class.getDeclaredConstructor(); c.setAccessible(true);
        Player p = c.newInstance(); p.x = 5 * 128; p.y = 6 * 128; f(Player.class, "visible").setBoolean(p, false);
        Game.myPlayer = p; g.playerCount = 0; g.npcCount = 0; g.fixedPlane = 0;
        return g;
    }
    /** Offset the shake added to xCameraPos (channel 0) in the frame just drawn. */
    private static int xOffset() throws Exception { return sfield("cameraWorldX") - 1000; }

    @Test public void randomAmplitudeBoundsTheJitterAndZeroMeansNoJitter() throws Exception {
        EntitySceneInsertionTest.TestGame g = newGame(true);
        g.shakeChannelActive[0] = true; g.shakeSineAmplitude[0] = 0; g.shakePhase[0] = 0; g.shakeSineSpeed[0] = 0;
        g.shakeRandomAmplitude[0] = 0;
        int lo0 = 99, hi0 = -99;
        for (int i = 0; i < 40; i++) { g.drawWorldView(); lo0 = Math.min(lo0, xOffset()); hi0 = Math.max(hi0, xOffset()); }
        assertTrue("amplitude 0 -> no jitter", lo0 >= 0 && hi0 <= 0);
        g.shakeRandomAmplitude[0] = 30;
        int lo = 99, hi = -99;
        for (int i = 0; i < 400; i++) { g.drawWorldView(); lo = Math.min(lo, xOffset()); hi = Math.max(hi, xOffset()); }
        assertTrue("never beyond +-30", lo >= -30 && hi <= 30);
        assertTrue("jitter reaches both sides " + lo + " " + hi, lo < -15 && hi > 15);
    }

    @Test public void sineAmplitudeSpeedAndPhaseDriveTheOscillation() throws Exception {
        EntitySceneInsertionTest.TestGame g = newGame(true);
        g.shakeChannelActive[0] = true; g.shakeRandomAmplitude[0] = 0; g.shakeSineAmplitude[0] = 100;
        g.shakePhase[0] = 157; g.shakeSineSpeed[0] = 1; g.drawWorldView();
        assertTrue("phase*speed/100 = pi/2 -> +amplitude", Math.abs(xOffset() - 100) <= 1);
        g.shakePhase[0] = 471; g.drawWorldView();
        assertTrue("phase 3pi/2 -> -amplitude, got " + xOffset(), Math.abs(xOffset() + 100) <= 1);
        g.shakePhase[0] = 0; g.drawWorldView();
        assertTrue("phase 0 -> zero", Math.abs(xOffset()) <= 1);
        g.shakePhase[0] = 157; g.shakeSineSpeed[0] = 0; g.drawWorldView();
        assertTrue("speed 0 -> zero regardless of phase", Math.abs(xOffset()) <= 1);
        g.shakeSineSpeed[0] = 2; g.shakePhase[0] = 157 / 2; g.drawWorldView();
        assertTrue("speed doubles the angle rate: 78*2/100 ~ pi/2", Math.abs(xOffset() - 100) <= 2);
        g.shakeSineAmplitude[0] = 50; g.drawWorldView();
        assertTrue("amplitude scales linearly", Math.abs(xOffset() - 50) <= 2);
    }

    @Test public void activeFlagGatesEachChannelIndependently() throws Exception {
        EntitySceneInsertionTest.TestGame g = newGame(true);
        for (int c = 0; c < 5; c++) { g.shakeRandomAmplitude[c] = 0; g.shakeSineAmplitude[c] = 100; g.shakePhase[c] = 157; g.shakeSineSpeed[c] = 1; g.shakeChannelActive[c] = false; }
        g.drawWorldView();
        assertEquals(0, xOffset()); assertEquals(33, sfield("cameraWorldY")); assertEquals(22, sfield("cameraWorldHeight"));
        g.shakeChannelActive[1] = true; g.drawWorldView();
        assertEquals("only channel 1 (z) moves", 0, xOffset()); assertEquals(33, sfield("cameraWorldY"));
        assertTrue(Math.abs(sfield("cameraWorldHeight") - 122) <= 1);
    }

    @Test public void followCoordinatesAndYawDriftReachSetCameraPos() throws Exception {
        EntitySceneInsertionTest.TestGame g = newGame(false);
        for (int c = 0; c < 5; c++) g.shakeChannelActive[c] = false;
        g.cameraFollowX = 777; g.cameraFollowY = 888; g.cameraPitchBase = 200; g.cameraTerrainClearance = 0; g.cameraYawBase = 100; g.cameraYawDrift = 0;
        g.drawWorldView();
        assertEquals("cameraFollowX -> third argument", 777, g.cameraArgs[2]);
        assertEquals("cameraFollowY -> sixth argument", 888, g.cameraArgs[5]);
        assertEquals(100, g.cameraArgs[4]);
        g.cameraYawDrift = -40; g.drawWorldView();
        assertEquals("drift is added to yaw", 60, g.cameraArgs[4]);
        g.cameraYawBase = 20; g.cameraYawDrift = -40; g.drawWorldView();
        assertEquals("yaw wraps modulo 2048", (20 - 40) & 0x7ff, g.cameraArgs[4]);
        assertEquals(200, g.cameraArgs[1]);
    }

    private void deliver(Game g, int opcode, byte[] payload, int size) throws Exception {
        ServerSocket l = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"));
        Socket client = new Socket(InetAddress.getByName("127.0.0.1"), l.getLocalPort());
        Socket server = l.accept();
        try {
            RSSocket rs = new RSSocket(null, client);
            server.getOutputStream().write(payload); server.getOutputStream().flush();
            long end = System.currentTimeMillis() + 5000;
            while (rs.available() < payload.length && System.currentTimeMillis() < end) Thread.sleep(5);
            g.socketStream = rs; g.pktType = opcode; g.pktSize = size;
            assertTrue(g.parsePacket());
        } finally { client.close(); server.close(); l.close(); }
    }

    @Test public void shakePacketFillsChannelFieldsInWireOrderAndResetPacketClearsFlags() throws Exception {
        Game g = new Game();
        g.inStream = new Stream(new byte[600]);
        g.shakePhase[3] = 999;
        deliver(g, 35, new byte[] {3, 11, 22, 33}, 4);
        assertTrue(g.shakeChannelActive[3]);
        assertEquals("second byte", 11, g.shakeRandomAmplitude[3]);
        assertEquals("third byte", 22, g.shakeSineAmplitude[3]);
        assertEquals("fourth byte", 33, g.shakeSineSpeed[3]);
        assertEquals("phase reset to 0", 0, g.shakePhase[3]);
        assertFalse(g.shakeChannelActive[2]); assertEquals(0, g.shakeRandomAmplitude[2]);
        g.shakeChannelActive[0] = true; g.shakeChannelActive[4] = true;
        deliver(g, 107, new byte[] {0}, 0);
        for (int c = 0; c < 5; c++) assertFalse("opcode 107 clears channel " + c, g.shakeChannelActive[c]);
    }
}
