import java.lang.reflect.*;
import java.util.*;
import org.junit.*;
import static org.junit.Assert.*;

/** Pass 169: semantic tests for Game.method26 / method47 / method146, written against the original identifiers only. */
public class EntitySceneInsertionTest {
    private static Field f(Class<?> c, String n) throws Exception { Field x = c.getDeclaredField(n); x.setAccessible(true); return x; }
    private static sun.misc.Unsafe unsafe() throws Exception { return (sun.misc.Unsafe) f(sun.misc.Unsafe.class, "theUnsafe").get(null); }
    private static EntityDef newDef() throws Exception { Constructor<EntityDef> c = EntityDef.class.getDeclaredConstructor(); c.setAccessible(true); return c.newInstance(); }

    private static int sfield(String n) throws Exception { return f(WorldController.class, n).getInt(null); }
    /** Camera values the last drawScene stored: x, y, height(z), plane, pitchSine, yawSine. */
    private static int[] cam() throws Exception { return new int[] {sfield("cameraWorldX"), sfield("cameraWorldY"), sfield("cameraWorldHeight"), sfield("cameraPlane"), sfield("pitchSine"), sfield("yawSine")}; }
    private static WorldController world() { return new WorldController(new int[4][105][105]); }

    static class TestGame extends Game {
        List<String> log = new ArrayList<String>();
        boolean bumpCounterInEntities; int[] cameraArgs; int methodArg37 = Integer.MIN_VALUE; int roofPlane = 2, fixedPlane = 3;
        @Override public void addPlayersToScene(boolean flag) { log.add("m47:" + flag); super.addPlayersToScene(flag); }
        @Override public void addNpcsToScene(boolean flag) { log.add("m26:" + flag); super.addNpcsToScene(flag); }
        @Override public void updateProjectiles() { log.add("proj"); }
        @Override public void updateTransientGraphics() { log.add("gfx"); }
        @Override public void setCameraPos(int j, int k, int l, int i1, int j1, int k1) {
            cameraArgs = new int[] {j, k, l, i1, j1, k1};
            xCameraPos = 1000; zCameraPos = 2000; yCameraPos = 3000; yCameraCurve = 200; xCameraCurve = 300;
        }
        @Override public int selectRoofVisibilityPlane() { return roofPlane; }
        @Override public int selectFixedCameraPlane() { return fixedPlane; }
        @Override public void updateEntities() { log.add("entities"); if (bumpCounterInEntities) Texture.useCounter = 123; }
        @Override public void drawHeadIcon() { log.add("icons"); }
        @Override public void scrollAnimatedTextures(int j) { methodArg37 = j; log.add("m37"); }
        @Override public void draw3dScreen() { log.add("3d"); }
    }

    private static boolean sineNear(int sine, int yaw) { for (int d = -1; d <= 1; d++) if (Model.sineTable16[(yaw + d) & 0x7ff] == sine) return true; return false; }
    private static Ground[][][] tiles(WorldController w) throws Exception { return (Ground[][][]) f(WorldController.class, "groundArray").get(w); }
    private static TestGame newGame() throws Exception {
        TestGame g = new TestGame();
        g.byteGroundArray = new byte[4][104][104]; g.intGroundArray = new int[4][105][105];
        g.anIntArrayArray929 = new int[104][104]; g.anInt1265 = 1; g.plane = 0;
        g.worldController = world();
        return g;
    }
    private static Player player(int x, int y) throws Exception {
        Constructor<Player> c = Player.class.getDeclaredConstructor(); c.setAccessible(true);
        Player p = c.newInstance(); p.x = x; p.y = y; f(Player.class, "visible").setBoolean(p, true); return p;
    }
    private static Player hidden(Player p) throws Exception { f(Player.class, "visible").setBoolean(p, false); return p; }
    private static NPC npc(int x, int y, int size, boolean first) throws Exception {
        Constructor<NPC> c = NPC.class.getDeclaredConstructor(); c.setAccessible(true);
        NPC n = c.newInstance(); EntityDef d = newDef(); d.tileSize = (byte) size; d.placedInFirstPass = first; n.desc = d;
        n.tileSize = size; n.x = x; n.y = y; return n;
    }
    private static int count(TestGame g, int tx, int ty) throws Exception { Ground t = tiles(g.worldController)[0][tx][ty]; return t == null ? 0 : t.objectCount; }
    private static int uid(TestGame g, int tx, int ty, int n) throws Exception { return tiles(g.worldController)[0][tx][ty].obj5Array[n].uid; }

    @Test public void flagSelectsLocalPlayerOnlyOrTheIndexedPlayerList() throws Exception {
        Player savedMe = Game.myPlayer;
        try {
            TestGame g = newGame();
            Game.myPlayer = player(10 * 128 + 5, 20 * 128 + 7);
            g.playerArray = new Player[2048];
            g.playerArray[3] = player(30 * 128 + 5, 40 * 128 + 7);
            g.playerArray[4] = player(50 * 128 + 5, 60 * 128 + 7);
            g.playerIndices = new int[] {3, 4}; g.playerCount = 2; // the local player is NOT in the list
            g.addPlayersToScene(true);
            assertEquals("local player placed at (x>>7, y>>7), not transposed", 1, count(g, 10, 20));
            assertEquals(0, count(g, 20, 10));
            assertEquals("uid = myPlayerIndex << 14", g.myPlayerIndex << 14, uid(g, 10, 20, 0));
            assertEquals("flag=true ignores the list", 0, count(g, 30, 40)); assertEquals(0, count(g, 50, 60));
            g.worldController = world();
            g.addPlayersToScene(false);
            assertEquals("flag=false skips the local player", 0, count(g, 10, 20));
            assertEquals(3 << 14, uid(g, 30, 40, 0)); assertEquals(4 << 14, uid(g, 50, 60, 0));
            assertEquals("tile X comes from x", 0, count(g, 40, 30));
            g.worldController = world(); g.playerCount = 1;
            g.addPlayersToScene(false);
            assertEquals(1, count(g, 30, 40)); assertEquals("playerCount bounds the loop", 0, count(g, 50, 60));
            g.worldController = world(); g.playerCount = 0;
            g.addPlayersToScene(true);
            assertEquals("local player placed with playerCount 0", 1, count(g, 10, 20));
            g.worldController = world();
            g.addPlayersToScene(false);
            assertEquals(0, count(g, 30, 40));
        } finally { Game.myPlayer = savedMe; }
    }

    @Test public void playersOffTheMapAreSkippedAndCentredTilesAreDeduplicatedPerFrame() throws Exception {
        Player savedMe = Game.myPlayer;
        try {
            TestGame g = newGame();
            Game.myPlayer = player(0, 0);
            g.playerArray = new Player[2048];
            int[][] pos = {{103 * 128 + 64, 5 * 128 + 64}, {104 * 128, 5 * 128 + 64}, {5 * 128 + 64, 104 * 128}, {-1, 128}, {128, -1},
                    {7 * 128 + 64, 7 * 128 + 64}, {7 * 128 + 64, 7 * 128 + 64}, {8 * 128 + 63, 8 * 128 + 64}, {8 * 128 + 63, 8 * 128 + 64}};
            g.playerIndices = new int[pos.length];
            for (int n = 0; n < pos.length; n++) { g.playerArray[10 + n] = player(pos[n][0], pos[n][1]); g.playerIndices[n] = 10 + n; }
            g.playerCount = pos.length;
            g.addPlayersToScene(false);
            assertEquals("x tile 103 is on the map", 1, count(g, 103, 5));
            assertEquals("centred twice on one tile: second skipped", 1, count(g, 7, 7));
            assertEquals("off-centre twice on one tile: both kept", 2, count(g, 8, 8));
            assertEquals("marker for tile centre", 1, g.anIntArrayArray929[7][7]); assertEquals("off-centre sets no marker", 0, g.anIntArrayArray929[8][8]);
            g.worldController = world(); g.anInt1265 = 2;
            g.addPlayersToScene(false);
            assertEquals("new frame places the centred tile again", 1, count(g, 7, 7));
            int placed = 0; Ground[][] plane = tiles(g.worldController)[0];
            for (Ground[] col : plane) for (Ground t : col) if (t != null) placed++;
            assertTrue("off-map players are never placed (only in-range tiles exist)", placed >= 3);
        } finally { Game.myPlayer = savedMe; }
    }

    @Test public void localPlayerOnDestinationTileClearsDestX() throws Exception {
        Player savedMe = Game.myPlayer;
        try {
            TestGame g = newGame(); Game.myPlayer = player(10 * 128 + 5, 20 * 128 + 7); g.playerCount = 0;
            g.destX = 10; g.destY = 20; g.addPlayersToScene(true); assertEquals(0, g.destX);
            g.destX = 10; g.destY = 21; g.addPlayersToScene(true); assertEquals("different Y keeps destX", 10, g.destX);
            g.destX = 11; g.destY = 20; g.addPlayersToScene(true); assertEquals("different X keeps destX", 11, g.destX);
        } finally { Game.myPlayer = savedMe; }
    }

    @Test public void npcSlotUidTilesAndDeduplicationBehaveAsPerTileCoordinates() throws Exception {
        TestGame g = newGame();
        g.npcArray = new NPC[16];
        g.npcArray[5] = npc(10 * 128 + 5, 20 * 128 + 7, 1, false);
        g.npcArray[9] = npc(30 * 128 + 64, 40 * 128 + 64, 1, false);
        g.npcArray[11] = npc(30 * 128 + 64, 40 * 128 + 64, 1, false);
        g.npcArray[12] = npc(50 * 128 + 64, 60 * 128 + 64, 3, false);
        g.npcArray[13] = npc(50 * 128 + 64, 60 * 128 + 64, 3, false);
        g.npcArray[14] = npc(104 * 128, 5, 1, false); g.npcArray[15] = npc(5, 104 * 128, 1, false);
        g.npcIndices = new int[] {5, 9, 11, 12, 13, 14, 15}; g.npcCount = 7;
        g.addNpcsToScene(false);
        assertEquals(1, count(g, 10, 20)); assertEquals(0, count(g, 20, 10));
        assertEquals(0x20000000 + (5 << 14), uid(g, 10, 20, 0));
        assertEquals("size-1 centred duplicate skipped", 1, count(g, 30, 40)); assertEquals(0x20000000 + (9 << 14), uid(g, 30, 40, 0));
        assertEquals("larger NPCs are not deduplicated", 2, count(g, 50, 60));
        g.worldController = world(); g.npcCount = 1; g.anInt1265 = 2;
        g.addNpcsToScene(false);
        assertEquals("npcCount bounds the loop", 0, count(g, 30, 40)); assertEquals(1, count(g, 10, 20));
    }

    private static void setupFrame(TestGame g) throws Exception {
        DrawingArea.width = 8; DrawingArea.height = 8; DrawingArea.pixels = new int[64];
        g.aRSImageProducer_1165 = (RSImageProducer) unsafe().allocateInstance(RSImageProducer.class);
        f(RSApplet.class, "graphics").set(g, new java.awt.image.BufferedImage(8, 8, 1).createGraphics());
        g.aBoolean1160 = true;
        g.xCameraPos = 11; g.zCameraPos = 22; g.yCameraPos = 33; g.yCameraCurve = 200; g.xCameraCurve = 44;
    }

    @Test public void worldViewInsertsLocalPlayerThenFirstPassNpcsThenOtherPlayersThenOtherNpcsInOneFrame() throws Exception {
        Player savedMe = Game.myPlayer; int[] ras = {DrawingArea.width, DrawingArea.height}; int[] savedPx = DrawingArea.pixels;
        try {
            TestGame g = newGame(); setupFrame(g); g.fixedPlane = 0; f(WorldController.class, "planeCount").setInt(g.worldController, 0);
            Game.myPlayer = hidden(player(1, 1)); g.playerCount = 0; g.npcCount = 0;
            g.anInt1265 = 41;
            g.drawWorldView();
            assertEquals("counter bumped once per frame", 42, g.anInt1265);
            assertEquals(Arrays.asList("m47:true", "m26:true", "m47:false", "m26:false", "proj", "gfx", "entities", "icons", "m37", "3d"), g.log);
        } finally { Game.myPlayer = savedMe; DrawingArea.width = ras[0]; DrawingArea.height = ras[1]; DrawingArea.pixels = savedPx; }
    }

    @Test public void cameraIsRestoredAfterShakeAndShakeChannelsMapToTheirOwnFields() throws Exception {
        Player savedMe = Game.myPlayer; int[] ras = {DrawingArea.width, DrawingArea.height}; int[] savedPx = DrawingArea.pixels; int savedUse = Texture.useCounter;
        try {
            TestGame g = newGame(); setupFrame(g); Game.myPlayer = hidden(player(1, 1)); g.playerCount = 0; g.npcCount = 0;
            // angle 1.57 rad -> sin ~ 1, so the offset is about the amplitude (plus 0 or 1 from the random term)
            for (int c = 0; c < 5; c++) { g.anIntArray873[c] = 0; g.anIntArray1030[c] = 157; g.anIntArray928[c] = 1; g.anIntArray1203[c] = 100 * (c + 1); g.aBooleanArray876[c] = true; }
            g.aBooleanArray876[2] = false;
            g.fixedPlane = 1;
            g.drawWorldView();
            int[] a = cam(); // x, y, height(z), plane, pitchSine, yawSine
            assertEquals("fixed-camera plane goes to drawScene", 1, a[3]);
            assertTrue("channel 0 -> xCameraPos", Math.abs(a[0] - (11 + 100)) <= 1);
            assertTrue("channel 1 -> zCameraPos", Math.abs(a[2] - (22 + 200)) <= 1);
            assertEquals("channel 2 off -> yCameraPos unchanged", 33, a[1]);
            assertTrue("channel 3 -> xCameraCurve (yaw)", sineNear(a[5], 44 + 400));
            assertFalse("yaw really moved", sineNear(a[5], 44));
            assertEquals("channel 4 -> yCameraCurve clamped to 383", Model.sineTable16[383], a[4]);
            assertEquals("restored xCameraPos", 11, g.xCameraPos); assertEquals(22, g.zCameraPos); assertEquals(33, g.yCameraPos);
            assertEquals(200, g.yCameraCurve); assertEquals(44, g.xCameraCurve);
            g.xCameraCurve = 2000; g.yCameraCurve = 200;
            g.anIntArray1203[3] = 100; g.anIntArray1203[4] = -500;
            g.drawWorldView();
            a = cam();
            int wrapped = (2000 + 100) & 0x7ff;
            assertTrue("xCameraCurve wraps at 0x7ff", sineNear(a[5], wrapped));
            assertEquals("yCameraCurve clamped to 128", Model.sineTable16[128], a[4]);
            assertEquals(2000, g.xCameraCurve); assertEquals(200, g.yCameraCurve);
        } finally { Game.myPlayer = savedMe; DrawingArea.width = ras[0]; DrawingArea.height = ras[1]; DrawingArea.pixels = savedPx; Texture.useCounter = savedUse; }
    }

    @Test public void freeCameraPitchYawAndRoofPlaneFeedSetCameraPosAndDrawScene() throws Exception {
        Player savedMe = Game.myPlayer; int[] ras = {DrawingArea.width, DrawingArea.height}; int[] savedPx = DrawingArea.pixels; int savedZoom = Game.zoom; int savedUse = Texture.useCounter;
        try {
            TestGame g = newGame(); setupFrame(g); g.aBoolean1160 = false; Game.myPlayer = hidden(player(5 * 128, 6 * 128)); g.playerCount = 0; g.npcCount = 0;
            Game.zoom = 3; g.roofPlane = 2; g.fixedPlane = 3;
            for (int c = 0; c < 5; c++) g.aBooleanArray876[c] = false;
            g.anInt1184 = 150; g.anInt984 = 256 * 90; g.minimapInt1 = 1000; g.anInt896 = 1500;
            g.drawWorldView();
            assertEquals("roof plane (free camera) goes to drawScene", 2, cam()[3]);
            assertEquals("pitch = max source (anInt1184)", 150, g.cameraArgs[1]);
            assertEquals("distance = 600 + pitch * zoom", 600 + 150 * 3, g.cameraArgs[0]);
            assertEquals("yaw = (minimapInt1 + anInt896) & 0x7ff", (1000 + 1500) & 0x7ff, g.cameraArgs[4]);
            assertEquals("camera height = ground - 70 at the player", -70, g.cameraArgs[3]);
            g.anInt984 = 256 * 400;
            g.drawWorldView(); assertEquals("pitch from anInt984/256", 400, g.cameraArgs[1]);
            g.aBooleanArray876[4] = true; g.anIntArray1203[4] = 300; g.anIntArray873[4] = 0; g.anIntArray1030[4] = 0;
            g.drawWorldView(); assertEquals("pitch from shake amplitude + 128", 428, g.cameraArgs[1]);
            g.anIntArray1203[4] = 200; g.drawWorldView(); assertEquals("shake loses to anInt984/256", 400, g.cameraArgs[1]);
            g.aBooleanArray876[4] = false; Texture.useCounter = 99;
            g.bumpCounterInEntities = true;
            g.drawWorldView();
            assertEquals("method37 gets the counter sampled before the scene is drawn", 99, g.methodArg37);
        } finally { Game.myPlayer = savedMe; Game.zoom = savedZoom; Texture.useCounter = savedUse; DrawingArea.width = ras[0]; DrawingArea.height = ras[1]; DrawingArea.pixels = savedPx; }
    }
}
