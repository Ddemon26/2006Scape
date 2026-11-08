import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Hint icon state (packet 254, Game.updateEntities head arrows) and Game.updateEntities ground-item labels.
 * Original member names only; packet bytes travel through a real loopback RSSocket.
 */
public class HintIconAndGroundLabelTest {
    private static final int W = 300, H = 200, BG = 0x345678, ARROW_NPC = 0x333331, ARROW_PLAYER = 0x333332;
    private int[] state, savedPixels;
    private Player savedMyPlayer;
    private int savedLoopCycle, savedMinValue;
    private Object savedItemCache;
    private final List<Socket> sockets = new ArrayList<Socket>();
    private ServerSocket listener;

    /** Records every projectWorldToScreen call and places entities / labels at fixed screen points. */
    static class Spy extends Game {
        final Map<Entity, int[]> at = new IdentityHashMap<Entity, int[]>();
        final List<int[]> projected = new ArrayList<int[]>();
        @Override public void projectEntityToScreen(Entity e, int h) {
            int[] p = at.get(e);
            if (p == null) { spriteDrawX = -1; spriteDrawY = -1; } else { spriteDrawX = p[0]; spriteDrawY = p[1]; }
        }
        @Override public void projectWorldToScreen(int x, int h, int y) {
            projected.add(new int[]{x, h, y}); spriteDrawX = 150; spriteDrawY = 100;
        }
    }

    private static Field f(Class<?> c, String n) throws Exception { Field x = c.getDeclaredField(n); x.setAccessible(true); return x; }

    @Before public void before() throws Exception {
        savedPixels = DrawingArea.pixels;
        state = new int[]{DrawingArea.width, DrawingArea.height, DrawingArea.topX, DrawingArea.topY, DrawingArea.bottomX,
            DrawingArea.bottomY, DrawingArea.scanlineRightLimit, DrawingArea.clipRightHalf, DrawingArea.clipBottomHalf};
        DrawingArea.bindRaster(H, W, new int[W * H]);
        Arrays.fill(DrawingArea.pixels, BG);
        savedMyPlayer = Game.myPlayer; savedLoopCycle = Game.loopCycle; savedMinValue = Game.customSettingMinItemValue;
        savedItemCache = f(ItemDef.class, "cache").get(null);
        Game.loopCycle = 1000;
    }
    @After public void after() throws Exception {
        DrawingArea.pixels = savedPixels; DrawingArea.width = state[0]; DrawingArea.height = state[1]; DrawingArea.topX = state[2];
        DrawingArea.topY = state[3]; DrawingArea.bottomX = state[4]; DrawingArea.bottomY = state[5];
        DrawingArea.scanlineRightLimit = state[6]; DrawingArea.clipRightHalf = state[7]; DrawingArea.clipBottomHalf = state[8];
        Game.myPlayer = savedMyPlayer; Game.loopCycle = savedLoopCycle; Game.customSettingMinItemValue = savedMinValue;
        f(ItemDef.class, "cache").set(null, savedItemCache);
        for (Socket s : sockets) try { s.close(); } catch (Exception e) { }
        if (listener != null) listener.close();
    }

    // ---------- fixtures ----------
    private Spy spy() throws Exception {
        Spy s = new Spy();
        s.groundArray = new NodeList[4][104][104];
        s.chatTextDrawingArea = TextWidthTest.font(false);
        s.aTextDrawingArea_1270 = s.chatTextDrawingArea;
        s.chatEffectsMode = 0; s.publicChatMode = 0;
        s.playerArray = new Player[64]; s.playerIndices = new int[64]; s.playerCount = 0;
        s.npcArray = new NPC[64]; s.npcIndices = new int[64]; s.npcCount = 0;
        s.headIcons = new Sprite[8]; for (int i = 0; i < 8; i++) s.headIcons[i] = solid(0x222222);
        s.skullIcons = new Sprite[]{solid(0x111111), solid(0x111111)};
        s.headIconsHint = new Sprite[]{solid(ARROW_NPC), solid(ARROW_PLAYER)};
        Player me = new Player(); me.visible = false; me.headIcon = -1;
        Game.myPlayer = me;
        Game.customSettingMinItemValue = Integer.MAX_VALUE;
        return s;
    }
    private static Sprite solid(int color) { Sprite sp = new Sprite(1, 1); sp.pixels[0] = color; return sp; }
    private static int count(int color) { int n = 0; for (int p : DrawingArea.pixels) if ((p & 0xffffff) == color) n++; return n; }
    private static int bottomRow(int color) { int b = -1; for (int i = 0; i < DrawingArea.pixels.length; i++) if ((DrawingArea.pixels[i] & 0xffffff) == color) b = i / W; return b; }
    private static int topRow(int color) { for (int i = 0; i < DrawingArea.pixels.length; i++) if ((DrawingArea.pixels[i] & 0xffffff) == color) return i / W; return -1; }

    private static void addPlayer(Spy s, int slotId, int x, int y) {
        Player p = new Player(); p.visible = true; p.headIcon = -1; p.skullIcon = -1; p.combatLevel = 3; p.name = "AB";
        s.playerIndices[s.playerCount++] = slotId; s.playerArray[slotId] = p; s.at.put(p, new int[]{x, y});
    }
    private static void addNpc(Spy s, int slotId, int x, int y) throws Exception {
        Constructor<EntityDef> k = EntityDef.class.getDeclaredConstructor(); k.setAccessible(true);
        EntityDef d = k.newInstance(); d.childrenIDs = null; d.headIconId = -1;
        NPC n = new NPC(); n.desc = d;
        s.npcIndices[s.npcCount++] = slotId; s.npcArray[slotId] = n; s.at.put(n, new int[]{x, y});
    }

    // ---------- packet 254: hintIconType / hintIconNpcIndex / hintIconPlayerIndex ----------
    private void deliver(Game g, byte[] payload) throws Exception {
        if (listener == null) listener = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"));
        Socket client = new Socket(InetAddress.getByName("127.0.0.1"), listener.getLocalPort());
        Socket server = listener.accept();
        sockets.add(client); sockets.add(server);
        RSSocket rs = new RSSocket(null, client);
        server.getOutputStream().write(payload); server.getOutputStream().flush();
        long end = System.currentTimeMillis() + 5000;
        while (rs.available() < payload.length && System.currentTimeMillis() < end) Thread.sleep(5);
        g.socketStream = rs; g.inStream = new Stream(new byte[6000]);
        g.pktType = 254; g.pktSize = payload.length;
        assertTrue(g.parsePacket());
        assertEquals(-1, g.pktType);
        assertEquals("bytes consumed", payload.length, g.inStream.currentOffset);
    }
    private static byte[] b(int... v) { byte[] r = new byte[v.length]; for (int i = 0; i < v.length; i++) r[i] = (byte) v[i]; return r; }

    @Test public void packet254TypeOneStoresBigEndianNpcSlotOnlyInAnInt1222() throws Exception {
        Game g = new Game(); g.hintIconNpcIndex = -7; g.hintIconPlayerIndex = -8;
        deliver(g, b(1, 0x01, 0x2c));
        assertEquals(1, g.hintIconType); assertEquals(300, g.hintIconNpcIndex); assertEquals("player target untouched", -8, g.hintIconPlayerIndex);
    }
    @Test public void packet254TypeTenStoresBigEndianPlayerSlotOnlyInAnInt933() throws Exception {
        Game g = new Game(); g.hintIconNpcIndex = -7; g.hintIconPlayerIndex = -8;
        deliver(g, b(10, 0x07, 0xd0));
        assertEquals(10, g.hintIconType); assertEquals(2000, g.hintIconPlayerIndex); assertEquals("npc target untouched", -7, g.hintIconNpcIndex);
    }
    @Test public void packet254TypesTwoToSixCollapseToTwoAndReadTileHeight() throws Exception {
        Game g = new Game(); g.hintIconNpcIndex = -7; g.hintIconPlayerIndex = -8;
        deliver(g, b(3, 0x0c, 0xa0, 0x0c, 0x93, 9));
        assertEquals(2, g.hintIconType); assertEquals(0, g.anInt937); assertEquals(64, g.anInt938);
        assertEquals(3232, g.anInt934); assertEquals(3219, g.anInt935); assertEquals(9, g.anInt936);
        assertEquals(-7, g.hintIconNpcIndex); assertEquals(-8, g.hintIconPlayerIndex);
    }
    @Test public void packet254TypeZeroAndUnknownTypesReadNoTarget() throws Exception {
        Game g = new Game(); g.hintIconType = 10; g.hintIconNpcIndex = -7; g.hintIconPlayerIndex = -8;
        deliver(g, b(0));
        assertEquals(0, g.hintIconType); assertEquals(-7, g.hintIconNpcIndex); assertEquals(-8, g.hintIconPlayerIndex);
        deliver(g, b(7));
        assertEquals("unknown type stored as-is", 7, g.hintIconType); assertEquals(-7, g.hintIconNpcIndex); assertEquals(-8, g.hintIconPlayerIndex);
    }

    // ---------- updateEntities head arrows ----------
    private int[] arrows(int type, int npcTarget, int playerTarget, int loopCycle) throws Exception {
        Arrays.fill(DrawingArea.pixels, BG);
        Spy s = spy(); Game.loopCycle = loopCycle;
        addPlayer(s, 5, 60, 100); addPlayer(s, 6, 120, 100);
        addNpc(s, 5, 180, 100); addNpc(s, 6, 240, 100);
        s.hintIconType = type; s.hintIconNpcIndex = npcTarget; s.hintIconPlayerIndex = playerTarget;
        s.updateEntities();
        // column of each arrow: x - 12
        int[] r = new int[4];
        for (int i = 0; i < DrawingArea.pixels.length; i++) {
            int c = DrawingArea.pixels[i] & 0xffffff, x = i % W;
            if (c == ARROW_NPC || c == ARROW_PLAYER) { int e = (x + 12) / 60 - 1; if (e >= 0 && e < 4) r[e] = c; }
        }
        return r;
    }
    @Test public void npcArrowFollowsAnInt1222SlotIdAndFlashes() throws Exception {
        assertArrayEquals(new int[]{0, 0, 0, ARROW_NPC}, arrows(1, 6, 6, 1000));
        assertArrayEquals(new int[]{0, 0, ARROW_NPC, 0}, arrows(1, 5, 6, 1000));
        assertArrayEquals("hidden on odd 10-cycle half", new int[]{0, 0, 0, 0}, arrows(1, 6, 6, 1010));
        assertArrayEquals("other type ignores npc slot", new int[]{0, 0, 0, 0}, arrows(3, 6, -1, 1000));
    }
    @Test public void playerArrowFollowsAnInt933SlotIdWithoutFlashing() throws Exception {
        assertArrayEquals(new int[]{0, ARROW_PLAYER, 0, 0}, arrows(10, 6, 6, 1000));
        assertArrayEquals(new int[]{ARROW_PLAYER, 0, 0, 0}, arrows(10, 6, 5, 1000));
        assertArrayEquals("player arrow does not flash", new int[]{ARROW_PLAYER, 0, 0, 0}, arrows(10, 6, 5, 1010));
        assertArrayEquals("type 0 draws nothing", new int[]{0, 0, 0, 0}, arrows(0, 6, 6, 1000));
    }

    // ---------- updateEntities ground-item labels: k5 / l5 / class19 ----------
    private static void installItems(int... idValueAmount) throws Exception {
        ItemDef[] cache = new ItemDef[10];
        for (int i = 0; i < 10; i++) {
            Constructor<ItemDef> k = ItemDef.class.getDeclaredConstructor(); k.setAccessible(true);
            cache[i] = k.newInstance(); cache[i].id = -100 - i;
        }
        for (int i = 0; i < idValueAmount.length / 2; i++) { cache[i].id = idValueAmount[i * 2]; cache[i].value = idValueAmount[i * 2 + 1]; cache[i].name = "AB"; }
        f(ItemDef.class, "cache").set(null, cache);
    }
    private static void drop(Spy s, int plane, int x, int y, int id, int amount) {
        if (s.groundArray[plane][x][y] == null) s.groundArray[plane][x][y] = new NodeList();
        Item it = new Item(); it.ID = id; it.amount = amount; s.groundArray[plane][x][y].insertHead(it);
    }

    @Test public void labelsProjectEachPileAtItsOwnTileCentreOnTheCurrentPlaneOnly() throws Exception {
        installItems(1, 5000, 2, 5000);
        Spy s = spy(); Game.customSettingMinItemValue = 0; s.plane = 0;
        drop(s, 0, 3, 7, 1, 1);
        drop(s, 0, 40, 2, 2, 1);
        drop(s, 1, 9, 9, 1, 1);
        s.updateEntities();
        assertEquals(2, s.projected.size());
        assertArrayEquals("x outer loop first: tile (3,7)", new int[]{3 * 128 + 64, 20, 7 * 128 + 64}, s.projected.get(0));
        assertArrayEquals("tile (40,2)", new int[]{40 * 128 + 64, 20, 2 * 128 + 64}, s.projected.get(1));
        s.projected.clear(); s.plane = 1; s.updateEntities();
        assertEquals("plane 1 pile only", 1, s.projected.size());
        assertArrayEquals(new int[]{9 * 128 + 64, 20, 9 * 128 + 64}, s.projected.get(0));
    }
    @Test public void everyItemOfOnePileGetsAStackedLabel() throws Exception {
        installItems(1, 5000, 2, 5000);
        Spy s = spy(); Game.customSettingMinItemValue = 0; s.plane = 0;
        drop(s, 0, 3, 7, 1, 1);
        drop(s, 0, 3, 7, 2, 1);
        s.updateEntities();
        assertEquals("one projection per item of the pile", 2, s.projected.size());
        assertArrayEquals(s.projected.get(0), s.projected.get(1));
        int bottom = bottomRow(RSColors.YELLOW), top = topRow(RSColors.YELLOW);
        assertTrue("two labels 10 rows apart: " + top + ".." + bottom, bottom - top >= 10);
        Arrays.fill(DrawingArea.pixels, BG);
        Spy one = spy(); Game.customSettingMinItemValue = 0; one.plane = 0; drop(one, 0, 3, 7, 1, 1);
        one.updateEntities();
        assertEquals(1, one.projected.size());
        assertEquals("single label spans fewer rows", true, bottomRow(RSColors.YELLOW) - topRow(RSColors.YELLOW) < 10);
    }
}
