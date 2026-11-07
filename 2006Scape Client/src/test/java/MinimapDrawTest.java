import static org.junit.Assert.*;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Game.drawMinimap as a whole routine, observed through its image raster and the marker/hint calls it makes (original identifiers only).
 * Player stands at world (320, 640), i.e. 10 and 20 minimap units; markers are recorded instead of drawn.
 */
public class MinimapDrawTest {
    private static final class Fixture extends java.awt.Component {
        @Override public java.awt.Image createImage(java.awt.image.ImageProducer p) { return null; }
        @Override public boolean prepareImage(java.awt.Image i, java.awt.image.ImageObserver o) { return false; }
    }

    private static final class Rec extends Game {
        final List<Object[]> markers = new ArrayList<Object[]>();
        final List<Object[]> hints = new ArrayList<Object[]>();
        @Override public void drawMinimapMarker(Sprite sprite, int offsetX, int offsetY) { markers.add(new Object[] {sprite, offsetX, offsetY}); }
        @Override public void drawMinimapHint(Sprite sprite, int offsetY, int offsetX) { hints.add(new Object[] {sprite, offsetY, offsetX}); }
        List<Object[]> markersOf(Sprite s) {
            List<Object[]> r = new ArrayList<Object[]>();
            for (Object[] m : markers) if (m[0] == s) r.add(m);
            return r;
        }
    }

    private Rec g;
    private Object savedLineOffsets;
    private Player savedPlayer;
    private final DrawingPrimitivesTest state = new DrawingPrimitivesTest();
    private Sprite map;

    private static EntityDef npcDef() throws Exception {
        Constructor<EntityDef> c = EntityDef.class.getDeclaredConstructor();
        c.setAccessible(true);
        return c.newInstance();
    }

    @Before public void before() throws Exception {
        state.saveState();
        savedLineOffsets = Texture.lineOffsets;
        savedPlayer = Game.myPlayer;
        g = new Rec();
        Fixture c = new Fixture();
        g.aRSImageProducer_1164 = new RSImageProducer(172, 156, c);
        g.aRSImageProducer_1165 = new RSImageProducer(512, 334, c);
        map = new Sprite(512, 512);
        g.minimapSprite = map;
        g.compass = new Sprite(33, 33);
        g.groundArray = new NodeList[4][104][104];
        g.npcArray = new NPC[16];
        g.npcIndices = new int[16];
        g.playerArray = new Player[16];
        g.playerIndices = new int[16];
        g.friendsListAsLongs = new long[8];
        g.friendsNodeIDs = new int[8];
        g.mapDotItem = new Sprite(1, 1);
        g.mapDotNPC = new Sprite(1, 1);
        g.mapDotPlayer = new Sprite(1, 1);
        g.mapDotFriend = new Sprite(1, 1);
        g.mapDotTeam = new Sprite(1, 1);
        g.mapMarker = new Sprite(1, 1);
        g.mapFlag = new Sprite(1, 1);
        Arrays.fill(g.anIntArray1229, 146);
        Arrays.fill(g.anIntArray1052, 0);
        Game.myPlayer = new Player();
        Game.myPlayer.x = 320;
        Game.myPlayer.y = 640;
    }

    @After public void after() {
        Texture.lineOffsets = (int[]) savedLineOffsets;
        Game.myPlayer = savedPlayer;
    }

    private Player other(int x, int y, int team, int combatLevel, String name) {
        Player p = new Player();
        p.x = x; p.y = y; p.team = team; p.combatLevel = combatLevel; p.name = name; p.visible = true;
        return p;
    }

    private void addPlayers(Player... ps) {
        g.playerCount = ps.length;
        for (int n = 0; n < ps.length; n++) { g.playerArray[n + 3] = ps[n]; g.playerIndices[n] = n + 3; }
    }

    private void drawFresh() {
        Arrays.fill(g.aRSImageProducer_1164.pixels, 0);
        g.drawMinimap();
    }

    private int[] scan(int rgb) {
        int[] r = g.aRSImageProducer_1164.pixels;
        long sx = 0, sy = 0; int n = 0;
        for (int y = 0; y < 156; y++) for (int x = 0; x < 172; x++) if (r[x + y * 172] == rgb) { sx += x; sy += y; n++; }
        return n == 0 ? new int[] {0, -1, -1} : new int[] {n, (int) (sx / n), (int) (sy / n)};
    }

    @Test public void imageCentreFollowsThePlayerColumnFromXAndRowFromYAsymmetrically() {
        Game.myPlayer.x = 300 * 32;
        Game.myPlayer.y = 200 * 32;
        Arrays.fill(map.pixels, 0x101010);
        int col = 48 + 300, row = 464 - 200;
        for (int dy = -1; dy <= 1; dy++) for (int dx = -1; dx <= 1; dx++) map.pixels[col + dx + (row + dy) * 512] = 0x00ff00;
        drawFresh();
        int[] found = scan(0x00ff00);
        assertTrue(found[0] > 0);
        assertTrue("green block is at the dest centre (98, 80)", Math.abs(found[1] - 98) <= 2 && Math.abs(found[2] - 80) <= 2);
        Game.myPlayer.x = 200 * 32;
        Game.myPlayer.y = 300 * 32;
        drawFresh();
        int[] swapped = scan(0x00ff00);
        assertTrue("swapped x/y misses the block", swapped[0] == 0 || Math.abs(swapped[1] - 98) > 2 || Math.abs(swapped[2] - 80) > 2);
    }

    @Test public void rotationIsCompassPlusMinimapAngleMaskedTo2048() {
        Game.myPlayer.x = 250 * 32;
        Game.myPlayer.y = 250 * 32;
        Arrays.fill(map.pixels, 0x101010);
        int col = 48 + 250, row = 464 - 250;
        for (int dy = -1; dy <= 1; dy++) for (int dx = 9; dx <= 11; dx++) map.pixels[col + dx + (row + dy) * 512] = 0x00ff00;
        g.cameraYawBase = 0; g.minimapRotationDrift = 0;
        drawFresh();
        int[] a0 = scan(0x00ff00);
        assertTrue(a0[0] > 0 && a0[1] > 98 + 5);
        g.cameraYawBase = 1024; g.minimapRotationDrift = 0;
        drawFresh();
        int[] half = scan(0x00ff00);
        assertTrue("half turn mirrors to the other side " + Arrays.toString(a0) + Arrays.toString(half), half[0] > 0 && half[1] < 98 - 5);
        g.cameraYawBase = 600; g.minimapRotationDrift = 424;
        drawFresh();
        assertArrayEquals("sum of the two angles", half, scan(0x00ff00));
        g.cameraYawBase = 2000; g.minimapRotationDrift = 100;
        drawFresh();
        int[] saved = g.aRSImageProducer_1164.pixels.clone();
        g.cameraYawBase = 52; g.minimapRotationDrift = 0;
        drawFresh();
        assertArrayEquals("2100 & 0x7ff == 52", saved, g.aRSImageProducer_1164.pixels);
    }

    @Test public void zoomOffsetScalesTheImageAboutThePlayerWithoutRotatingIt() {
        Game.myPlayer.x = 250 * 32;
        Game.myPlayer.y = 250 * 32;
        Arrays.fill(map.pixels, 0x101010);
        int col = 48 + 250, row = 464 - 250;
        for (int dy = -1; dy <= 1; dy++) for (int dx = 19; dx <= 21; dx++) map.pixels[col + dx + (row + dy) * 512] = 0x00ff00;
        g.cameraYawBase = 0; g.minimapRotationDrift = 0;
        int[] d = new int[3]; int[] z = {0, 128, -64};
        for (int n = 0; n < 3; n++) {
            g.minimapZoomDrift = z[n];
            drawFresh();
            int[] f = scan(0x00ff00);
            assertTrue("block visible at zoom " + z[n], f[0] > 0);
            assertTrue("zoom does not rotate: row stays at the centre " + Arrays.toString(f), Math.abs(f[2] - 80) <= 2);
            d[n] = f[1] - 98;
        }
        assertTrue("256 is unit scale " + d[0], Math.abs(d[0] - 20) <= 2);
        assertTrue("positive offset shrinks distance to 20*256/384 " + d[1], Math.abs(d[1] - 13) <= 2);
        assertTrue("negative offset grows distance to 20*256/192 " + d[2], Math.abs(d[2] - 27) <= 2);
    }

    @Test public void interfaceModeTwoOnlyBlanksTheBackMaskAndDrawsNoMarkers() {
        Background back = ScrollbarTest.image(0x123456);
        back.paletteIndices = new byte[172 * 156];
        back.paletteIndices[5] = 1;
        g.mapBack = back;
        g.minimapState = 2;
        Arrays.fill(g.aRSImageProducer_1164.pixels, 0x778899);
        g.groundArray[0][3][3] = new NodeList();
        g.drawMinimap();
        assertEquals("index 0 cleared", 0, g.aRSImageProducer_1164.pixels[0]);
        assertEquals("non-zero index kept", 0x778899, g.aRSImageProducer_1164.pixels[5]);
        assertEquals(0, g.markers.size());
    }

    @Test public void groundItemDotsUseTileOffsetsFromThePlayerAndOnlyTheCurrentPlane() {
        g.plane = 1;
        g.groundArray[1][7][9] = new NodeList();
        g.groundArray[0][20][20] = new NodeList();
        g.drawMinimap();
        List<Object[]> items = g.markersOf(g.mapDotItem);
        assertEquals(1, items.size());
        assertEquals(7 * 4 + 2 - 10, items.get(0)[1]);
        assertEquals(9 * 4 + 2 - 20, items.get(0)[2]);
    }

    @Test public void npcDotOffsetsAreWorldDifferenceInUnitsWithXThenY() throws Exception {
        NPC npc = new NPC();
        npc.desc = npcDef();
        npc.x = 320 + 32 * 5; npc.y = 640 - 32 * 3;
        g.npcArray[4] = npc;
        g.npcIndices[0] = 4;
        g.npcCount = 1;
        g.drawMinimap();
        List<Object[]> dots = g.markersOf(g.mapDotNPC);
        assertEquals(1, dots.size());
        assertEquals(5, dots.get(0)[1]);
        assertEquals(-3, dots.get(0)[2]);
    }

    @Test public void playerDotKindIsTeamThenFriendThenPlain() {
        long bob = TextClass.encodeBase37Name("bob");
        long amy = TextClass.encodeBase37Name("amy");
        g.friendsCount = 2;
        g.friendsListAsLongs[0] = bob; g.friendsNodeIDs[0] = 1;
        g.friendsListAsLongs[1] = amy; g.friendsNodeIDs[1] = 0;
        Game.myPlayer.team = 3;
        addPlayers(
            other(320 + 64, 640 + 96, 3, 50, "zed"),
            other(320 + 32, 640 + 32, 4, 50, "bob"),
            other(320 - 32, 640 - 64, 0, 50, "amy"),
            other(320, 640, 0, 0, "zed"),
            other(320 + 160, 640, 4, 50, "kim"),
            other(320 + 5 * 32, 640 + 2 * 32, 3, 50, "bob"));
        g.drawMinimap();
        assertEquals(3, g.markersOf(g.mapDotTeam).size());
        assertEquals(1, g.markersOf(g.mapDotFriend).size());
        assertEquals(2, g.markersOf(g.mapDotPlayer).size());
        Object[] t = g.markersOf(g.mapDotTeam).get(0);
        assertEquals(2, t[1]); assertEquals(3, t[2]);
        Object[] f = g.markersOf(g.mapDotFriend).get(0);
        assertEquals(1, f[1]); assertEquals(1, f[2]);
        Game.myPlayer.team = 0;
        g.markers.clear();
        addPlayers(other(320, 640, 0, 50, "zed"));
        g.drawMinimap();
        assertEquals(0, g.markersOf(g.mapDotTeam).size());
        assertEquals(1, g.markersOf(g.mapDotPlayer).size());
        g.markers.clear();
        Player hidden = other(320, 640, 0, 50, "zed");
        hidden.visible = false;
        addPlayers(hidden);
        g.drawMinimap();
        assertEquals(0, g.markers.size());
    }

    @Test public void hintArrowPicksNpcLocationOrPlayerByModeAndBlinksOnLoopCycle() throws Exception {
        NPC npc = new NPC(); npc.x = 320 + 64; npc.y = 640 + 96; npc.desc = npcDef();
        g.npcArray[5] = npc;
        g.playerArray[6] = other(320 - 32, 640 - 128, 0, 50, "zed");
        g.baseX = 3000; g.baseY = 3100;
        g.anInt934 = 3010; g.anInt935 = 3120;
        g.anInt1222 = 5; g.anInt933 = 6;
        int saved = Game.loopCycle;
        try {
            Game.loopCycle = 0;
            g.anInt855 = 1; g.drawMinimap();
            assertEquals(1, g.hints.size());
            assertEquals("offsetY first", 3, g.hints.get(0)[1]); assertEquals(2, g.hints.get(0)[2]);
            g.hints.clear(); g.anInt855 = 2; g.drawMinimap();
            assertEquals(20 * 4 + 2 - 20, g.hints.get(0)[1]); assertEquals(10 * 4 + 2 - 10, g.hints.get(0)[2]);
            g.hints.clear(); g.anInt855 = 10; g.drawMinimap();
            assertEquals(-4, g.hints.get(0)[1]); assertEquals(-1, g.hints.get(0)[2]);
            g.hints.clear(); g.anInt855 = 3; g.drawMinimap();
            assertEquals("other modes draw nothing", 0, g.hints.size());
            g.anInt855 = 1; g.anInt1222 = 16; g.drawMinimap();
            assertEquals("index beyond the array", 0, g.hints.size());
            g.anInt1222 = 7; g.drawMinimap();
            assertEquals("empty slot", 0, g.hints.size());
            g.anInt1222 = 5; Game.loopCycle = 10; g.drawMinimap();
            assertEquals("blink off half", 0, g.hints.size());
            Game.loopCycle = 19; g.drawMinimap();
            assertEquals(0, g.hints.size());
            Game.loopCycle = 20; g.drawMinimap();
            assertEquals(1, g.hints.size());
            g.hints.clear(); g.anInt855 = 0; g.drawMinimap();
            assertEquals(0, g.hints.size());
        } finally { Game.loopCycle = saved; }
    }

    @Test public void destinationFlagIsDrawnOnlyWhenDestXIsNonZero() {
        g.destX = 0; g.destY = 9;
        g.drawMinimap();
        assertEquals(0, g.markersOf(g.mapFlag).size());
        g.destX = 6; g.destY = 11;
        g.drawMinimap();
        List<Object[]> f = g.markersOf(g.mapFlag);
        assertEquals(1, f.size());
        assertEquals(6 * 4 + 2 - 10, f.get(0)[1]);
        assertEquals(11 * 4 + 2 - 20, f.get(0)[2]);
    }
}
