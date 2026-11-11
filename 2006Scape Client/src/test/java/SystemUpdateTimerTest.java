import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Original-name observations for the opcode-114 system-update countdown (systemUpdateCycles, systemUpdateNoticeDrawCount) and the
 * split private-chat rows it displaces (drawSplitPrivateChat / buildSplitPrivateChatMenu locals).
 */
public class SystemUpdateTimerTest {
    private static final int W = 520, H = 400, BG = 0x123456, CYAN = 0x00ffff, ICON1 = 0xff00ff, ICON2 = 0x00ff00;
    private int[] savedPixels, state;
    private int savedCounter;

    private static sun.misc.Unsafe unsafe() throws Exception {
        Field f = sun.misc.Unsafe.class.getDeclaredField("theUnsafe"); f.setAccessible(true);
        return (sun.misc.Unsafe) f.get(null);
    }
    private static void set(Object o, String name, Object v) throws Exception {
        Field f = o.getClass().getDeclaredField(name); f.setAccessible(true); f.set(o, v);
    }
    /** One-pixel-wide glyphs; heights[c] rows tall; every glyph advances {@code advance} pixels, spaces included. */
    private static TextDrawingArea font(int advance, int[] heights) throws Exception {
        TextDrawingArea t = (TextDrawingArea) unsafe().allocateInstance(TextDrawingArea.class);
        byte[][] masks = new byte[256][];
        int[] w = new int[256], h = new int[256], adv = new int[256];
        for (int c = 0; c < 256; c++) {
            w[c] = 1; h[c] = heights == null ? 1 : heights[c]; adv[c] = advance;
            masks[c] = new byte[h[c]]; Arrays.fill(masks[c], (byte) 1);
        }
        set(t, "glyphMasks", masks); set(t, "glyphWidthPixels", w); set(t, "glyphHeightPixels", h);
        set(t, "glyphXOffsetPixels", new int[256]); set(t, "glyphYOffsetPixels", new int[256]);
        set(t, "glyphAdvancePixels", adv); set(t, "jitterRandom", new Random(0)); set(t, "strikeThroughEnabled", false);
        t.maxAsciiGlyphHeightPixels = 0;
        return t;
    }
    private static Background icon(int rgb) throws Exception {
        Background b = (Background) unsafe().allocateInstance(Background.class);
        b.imageWidth = 1; b.imageHeight = 1; b.paletteIndices = new byte[]{1};
        set(b, "paletteRgb", new int[]{0, rgb});
        return b;
    }

    @Before public void before() {
        savedPixels = DrawingArea.pixels;
        state = new int[]{DrawingArea.width, DrawingArea.height, DrawingArea.topX, DrawingArea.topY, DrawingArea.bottomX,
            DrawingArea.bottomY, DrawingArea.scanlineRightLimit, DrawingArea.clipRightHalf, DrawingArea.clipBottomHalf};
        DrawingArea.bindRaster(H, W, new int[W * H]);
        Arrays.fill(DrawingArea.pixels, BG);
        savedCounter = Game.systemUpdateNoticeDrawCount;
    }
    @After public void after() {
        DrawingArea.pixels = savedPixels; DrawingArea.width = state[0]; DrawingArea.height = state[1]; DrawingArea.topX = state[2];
        DrawingArea.topY = state[3]; DrawingArea.bottomX = state[4]; DrawingArea.bottomY = state[5];
        DrawingArea.scanlineRightLimit = state[6]; DrawingArea.clipRightHalf = state[7]; DrawingArea.clipBottomHalf = state[8];
        Game.systemUpdateNoticeDrawCount = savedCounter;
    }

    // ---------- opcode 114: systemUpdateCycles = LE u16 * 30 ----------
    private static int deliver114(int lo, int hi) throws Exception {
        Game g = new Game();
        g.systemUpdateCycles = 777;
        InetAddress host = InetAddress.getByName("127.0.0.1");
        try (ServerSocket listener = new ServerSocket(0, 1, host);
             Socket client = new Socket(host, listener.getLocalPort());
             Socket server = listener.accept()) {
            server.getOutputStream().write(new byte[]{114, (byte) lo, (byte) hi});
            server.getOutputStream().flush();
            g.socketStream = new RSSocket(null, client);
            long end = System.currentTimeMillis() + 5000;
            while (g.socketStream.available() < 3 && System.currentTimeMillis() < end) Thread.sleep(5);
            g.inStream = new Stream(new byte[6000]);
            g.pktType = -1;
            assertTrue(g.parsePacket());
            assertEquals(-1, g.pktType);
            assertEquals(2, g.inStream.currentOffset);
            return g.systemUpdateCycles;
        }
    }
    @Test public void opcode114StoresLittleEndianShortTimesThirty() throws Exception {
        assertEquals(2, SizeConstants.packetSizes[114]);
        assertEquals(5 * 30, deliver114(5, 0));
        assertEquals(256 * 30, deliver114(0, 1));          // big-endian reading would give 1 * 30
        assertEquals(0xffff * 30, deliver114(0xff, 0xff)); // unsigned
        assertEquals(0, deliver114(0, 0));                 // 0 clears a running countdown
    }

    // ---------- mainGameProcessor: one cycle off, never below 1 ----------
    private static int afterOneCycle(int value) {
        Game g = new Game();
        g.systemUpdateCycles = value;
        try { g.mainGameProcessor(); } catch (Throwable t) { /* later work needs a live session; the countdown is first */ }
        return g.systemUpdateCycles;
    }
    @Test public void mainGameProcessorCountsDownOneCycleAndStopsAtOne() {
        assertEquals(149, afterOneCycle(150));
        assertEquals(1, afterOneCycle(2));
        assertEquals(1, afterOneCycle(1));    // boundary: never reaches 0 on its own
        assertEquals(0, afterOneCycle(0));
        assertEquals(-4, afterOneCycle(-4));
    }

    // ---------- draw3dScreen notice: systemUpdateCycles / 50 seconds, systemUpdateNoticeDrawCount report counter ----------
    static class HudGame extends Game {
        int splitCalls;
        @Override public void drawSplitPrivateChat() { splitCalls++; }
        @Override public void updateChatSuppressionZone() { }
        @Override public void processRightClick() { }
        @Override public void drawTooltip() { }
        @Override public void drawMenu() { }
    }
    private HudGame hud(int value) throws Exception {
        int[] heights = new int[256];
        Arrays.fill(heights, 1);
        for (char c = '0'; c <= '9'; c++) heights[c] = c - '0' + 2;
        heights[':'] = 13;
        HudGame g = new HudGame();
        g.chatTextDrawingArea = font(2, heights);
        g.crossType = 0; g.walkableInterfaceId = -1; g.openInterfaceID = -1; g.menuOpen = false; g.multiCombatOverlayState = 0;
        g.stream = new Stream(new byte[16]);
        g.stream.encryption = new ISAACRandomGen(new int[]{9, 8, 7, 6});
        g.systemUpdateCycles = value;
        return g;
    }
    /** Decodes yellow glyph columns left to right: height 2..11 = digit, 13 = ':'. */
    private static String yellowText() {
        StringBuilder sb = new StringBuilder();
        for (int x = 0; x < W; x++) {
            int n = 0;
            for (int y = 0; y < H; y++) if (DrawingArea.pixels[x + y * W] == RSColors.YELLOW) n++;
            if (n == 13) sb.append(':'); else if (n >= 2 && n <= 11) sb.append((char) ('0' + n - 2)); else if (n != 0) sb.append('?');
        }
        return sb.toString();
    }
    private String shown(int value) throws Exception {
        Arrays.fill(DrawingArea.pixels, BG);
        hud(value).draw3dScreen();
        return yellowText();
    }
    @Test public void noticeShowsCyclesDividedByFiftyAsClock() throws Exception {
        assertEquals("03", shown(150));          // zero minutes and hours are omitted
        assertEquals("03", shown(199));          // truncating division
        assertEquals("04", shown(200));
        assertEquals("59", shown(59 * 50 + 49));
        assertEquals("00", shown(1));            // the countdown's resting value still shows the notice
        assertEquals("01:00", shown(60 * 50));
        assertEquals("01:02:05", shown(3725 * 50));
        assertEquals("01:05", shown(3605 * 50)); // zero minutes omitted even with hours
        assertEquals("", shown(0));              // 0 hides the notice
    }
    @Test public void anInt849CountsNoticeFramesAndReportsOpcode148OnThe76th() throws Exception {
        Game.systemUpdateNoticeDrawCount = 0;
        HudGame g = hud(500);
        for (int n = 1; n <= 75; n++) { g.draw3dScreen(); assertEquals(n, Game.systemUpdateNoticeDrawCount); }
        assertEquals(0, g.stream.currentOffset);
        g.draw3dScreen();
        assertEquals(0, Game.systemUpdateNoticeDrawCount);
        assertEquals(1, g.stream.currentOffset);
        int key = new ISAACRandomGen(new int[]{9, 8, 7, 6}).getNextKey();
        assertEquals((byte) (148 + key), g.stream.buffer[0]);
        assertEquals(76, g.splitCalls);
    }
    @Test public void anInt849UnchangedWhileNoticeHidden() throws Exception {
        Game.systemUpdateNoticeDrawCount = 40;
        HudGame g = hud(0);
        for (int n = 0; n < 100; n++) g.draw3dScreen();
        assertEquals(40, Game.systemUpdateNoticeDrawCount);
        assertEquals(0, g.stream.currentOffset);
    }

    // ---------- pass 207: draw3dScreen notice box layout locals (debugItems, debugWidth, debugHeight, debugX, debugY) ----------
    private static int px(int x, int y) { return DrawingArea.pixels[x + y * W]; }
    private static void assertNoticeBox(int advance) throws Exception {
        Arrays.fill(DrawingArea.pixels, BG);
        HudGame g = new HudGame();
        g.chatTextDrawingArea = font(advance, null);
        g.crossType = 0; g.walkableInterfaceId = -1; g.openInterfaceID = -1; g.menuOpen = false; g.multiCombatOverlayState = 0;
        g.stream = new Stream(new byte[16]);
        g.stream.encryption = new ISAACRandomGen(new int[]{9, 8, 7, 6});
        g.systemUpdateCycles = 250;                         // "00:05"
        Game.systemUpdateNoticeDrawCount = 0;
        g.draw3dScreen();
        int width = "System update in: 00:00".length() * advance + 10; // debugWidth = measured template + 10
        int height = 25 + 1 * 15;                            // debugHeight = 25 + debugItems * 15, one body row
        int x = 511 - width, y = 334 - height;               // debugX / debugY: box anchored to the viewport's bottom-right
        // box extent: top-left corner and bottom-right corner inside, neighbours outside untouched
        assertNotEquals(BG, px(x, y)); assertEquals(BG, px(x - 1, y)); assertEquals(BG, px(x, y - 1));
        assertNotEquals(BG, px(x + width - 1, y + height - 1));
        assertEquals(BG, px(x + width, y + height - 1)); assertEquals(BG, px(x + width - 1, y + height));
        // body outline: starts 18 below the top, inset 1, black edges
        assertEquals(RSColors.BLACK, px(x + 1, y + 25)); assertEquals(RSColors.BLACK, px(x + width - 2, y + 25));
        assertNotEquals(RSColors.BLACK, px(x + 2, y + 25));
        // header text on the debugY + 14 baseline, 3 right of debugX
        assertEquals(RSColors.WHITE_DARKER, px(x + 3, y + 14)); assertNotEquals(RSColors.WHITE_DARKER, px(x + 2, y + 14));
        // exactly one body row (debugItems = 1), on baseline debugY + 14 + 3 + 15, white label at debugX + 4
        int whiteRows = 0, yellowRows = 0, maxYellowX = -1;
        for (int row = 0; row < H; row++) {
            boolean w = false, yl = false;
            for (int col = 0; col < W; col++) {
                if (px(col, row) == RSColors.WHITE) w = true;
                if (px(col, row) == RSColors.YELLOW) { yl = true; maxYellowX = Math.max(maxYellowX, col); }
            }
            if (w) { whiteRows++; assertEquals(y + 32, row); }
            if (yl) { yellowRows++; assertEquals(y + 32, row); }
        }
        assertEquals(1, whiteRows); assertEquals(1, yellowRows);
        assertEquals(RSColors.WHITE, px(x + 4, y + 32)); assertNotEquals(RSColors.WHITE, px(x + 3, y + 32));
        // clock right-aligned to debugX + debugWidth - 4: last glyph starts one advance before it
        assertEquals(x + width - 4 - advance, maxYellowX);
        assertTrue(y + 32 < y + height);                     // the single body row fits inside the box
    }
    @Test public void noticeBoxWidthFollowsFontAndRightEdgeStaysFixed() throws Exception {
        assertNoticeBox(2);   // width 56, left 455, top 294
        assertNoticeBox(3);   // width 79, left 432: width tracks the measured template, right edge stays at 510
    }

    // ---------- pass 200: draw3dScreen overlays (walkCrossDrawCount, multiCombatOverlayState, walkableInterfaceId) and logoutRequestCycles ----------
    private static Sprite dot(int rgb) {
        Sprite s = new Sprite(1, 1);
        s.pixels[0] = rgb;
        return s;
    }
    private static List<Integer> where(int rgb) {
        List<Integer> out = new ArrayList<Integer>();
        for (int i = 0; i < W * H; i++) if (DrawingArea.pixels[i] == rgb) { out.add(i % W); out.add(i / W); }
        return out;
    }
    /** Delivers one raw server packet (opcode + payload) through a loopback socket into parsePacket. */
    private static void deliver(Game g, int opcode, int... payload) throws Exception {
        InetAddress host = InetAddress.getByName("127.0.0.1");
        try (ServerSocket listener = new ServerSocket(0, 1, host);
             Socket client = new Socket(host, listener.getLocalPort());
             Socket server = listener.accept()) {
            byte[] raw = new byte[payload.length + 1];
            raw[0] = (byte) opcode;
            for (int i = 0; i < payload.length; i++) raw[i + 1] = (byte) payload[i];
            server.getOutputStream().write(raw);
            server.getOutputStream().flush();
            g.socketStream = new RSSocket(null, client);
            long end = System.currentTimeMillis() + 5000;
            while (g.socketStream.available() < raw.length && System.currentTimeMillis() < end) Thread.sleep(5);
            g.inStream = new Stream(new byte[6000]);
            g.pktType = -1;
            assertTrue(g.parsePacket());
            assertEquals(-1, g.pktType);
            assertEquals(payload.length, g.inStream.currentOffset);
        }
    }

    // walkCrossDrawCount: frames on which the walk (crossType 1) cross is drawn; opcode 78 on the 68th, then reset
    private HudGame crossGame(int type) throws Exception {
        HudGame g = hud(0);
        g.crosses = new Sprite[8];
        for (int i = 0; i < 8; i++) g.crosses[i] = dot(0x100 + i);
        g.crossType = type; g.crossX = 100; g.crossY = 50; g.crossIndex = 250;
        return g;
    }
    @Test public void anInt1142CountsWalkCrossFramesAndSendsOpcode78OnThe68th() throws Exception {
        int saved = Game.walkCrossDrawCount;
        try {
            Game.walkCrossDrawCount = 0;
            HudGame g = crossGame(1);
            g.draw3dScreen();
            assertEquals(Arrays.asList(88, 38), where(0x102));   // crosses[crossIndex / 100] at (crossX - 12, crossY - 12)
            assertEquals(1, Game.walkCrossDrawCount);
            for (int n = 2; n <= 67; n++) { g.draw3dScreen(); assertEquals(n, Game.walkCrossDrawCount); }
            assertEquals(0, g.stream.currentOffset);
            g.draw3dScreen();
            assertEquals(0, Game.walkCrossDrawCount);
            assertEquals(1, g.stream.currentOffset);
            int key = new ISAACRandomGen(new int[]{9, 8, 7, 6}).getNextKey();
            assertEquals((byte) (78 + key), g.stream.buffer[0]);
        } finally { Game.walkCrossDrawCount = saved; }
    }
    @Test public void anInt1142UnchangedForActionCrossOrNoCross() throws Exception {
        int saved = Game.walkCrossDrawCount;
        try {
            Game.walkCrossDrawCount = 30;
            HudGame g = crossGame(2);
            for (int n = 0; n < 100; n++) g.draw3dScreen();
            assertEquals(Arrays.asList(88, 38), where(0x106));   // red action cross is crosses[4 + crossIndex / 100]
            assertEquals(30, Game.walkCrossDrawCount);
            g = crossGame(0);
            for (int n = 0; n < 100; n++) g.draw3dScreen();
            assertEquals(30, Game.walkCrossDrawCount);
            assertEquals(0, g.stream.currentOffset);
        } finally { Game.walkCrossDrawCount = saved; }
    }

    // multiCombatOverlayState: packet 61 unsigned byte; multiOverlay drawn at (472, 296) only for value 1
    @Test public void anInt1055IsPacket61UnsignedByte() throws Exception {
        assertEquals(1, SizeConstants.packetSizes[61]);
        Game g = new Game();
        deliver(g, 61, 1); assertEquals(1, g.multiCombatOverlayState);
        deliver(g, 61, 0); assertEquals(0, g.multiCombatOverlayState);
        deliver(g, 61, 0xff); assertEquals(255, g.multiCombatOverlayState);   // unsigned
    }
    @Test public void anInt1055OneDrawsMultiCombatOverlayOtherValuesDoNot() throws Exception {
        for (int value : new int[]{0, 1, 2, 255}) {
            Arrays.fill(DrawingArea.pixels, BG);
            HudGame g = hud(0);
            g.multiOverlay = dot(0xabcdef);
            g.multiCombatOverlayState = value;
            g.draw3dScreen();
            assertEquals("value " + value, value == 1 ? Arrays.asList(472, 296) : Arrays.<Integer>asList(), where(0xabcdef));
        }
    }

    // walkableInterfaceId: packet 208 signed LE short; drawn by draw3dScreen beneath openInterfaceID; survives closeOpenInterfaces
    static class InterfaceHud extends HudGame {
        final List<Integer> drawn = new ArrayList<Integer>(), advanced = new ArrayList<Integer>();
        @Override public void drawInterface(int scrollPos, int originX, RSInterface container, int originY) {
            drawn.add(container.id); assertEquals(0, scrollPos); assertEquals(0, originX); assertEquals(0, originY);
        }
        @Override public boolean advanceInterfaceModelAnimations(int elapsedTicks, int interfaceId) { advanced.add(interfaceId); return false; }
    }
    private static RSInterface[] interfaces() {
        RSInterface[] cache = new RSInterface[0x8000];
        for (int id : new int[]{0, 5, 197, 4535, 0x1234, 0x7fff}) { cache[id] = new RSInterface(); cache[id].id = id; }
        return cache;
    }
    @Test public void anInt1018IsPacket208SignedLittleEndianShort() throws Exception {
        RSInterface[] saved = RSInterface.interfaceCache;
        try {
            RSInterface.interfaceCache = interfaces();
            assertEquals(2, SizeConstants.packetSizes[208]);
            Game g = new Game();
            deliver(g, 208, 0x34, 0x12); assertEquals(0x1234, g.walkableInterfaceId);   // big-endian would be 0x3412
            deliver(g, 208, 0xff, 0xff); assertEquals(-1, g.walkableInterfaceId);       // signed: -1 clears it
            deliver(g, 208, 0xc5, 0x00); assertEquals(197, g.walkableInterfaceId);
        } finally { RSInterface.interfaceCache = saved; }
    }
    @Test public void anInt1018DrawnBeneathOpenInterfaceOnlyWhenNotMinusOne() throws Exception {
        RSInterface[] saved = RSInterface.interfaceCache;
        try {
            RSInterface.interfaceCache = interfaces();
            InterfaceHud g = new InterfaceHud();
            g.chatTextDrawingArea = font(2, null); g.crossType = 0; g.menuOpen = false; g.multiCombatOverlayState = 0;
            g.stream = new Stream(new byte[16]); g.systemUpdateCycles = 0; g.cyclesSinceDraw = 3;
            g.walkableInterfaceId = 4535; g.openInterfaceID = 5;
            g.draw3dScreen();
            assertEquals(Arrays.asList(4535, 5), g.drawn);      // walkable layer first, open interface over it
            assertEquals(Arrays.asList(4535, 5), g.advanced);
            g.drawn.clear(); g.advanced.clear();
            g.walkableInterfaceId = -1;
            g.draw3dScreen();
            assertEquals(Arrays.asList(5), g.drawn);
            g.drawn.clear();
            g.walkableInterfaceId = 0; g.openInterfaceID = -1;             // boundary: id 0 is drawn
            g.draw3dScreen();
            assertEquals(Arrays.asList(0), g.drawn);
        } finally { RSInterface.interfaceCache = saved; }
    }
    @Test public void anInt1018SurvivesCloseOpenInterfaces() {
        Game g = new Game();
        g.stream = new Stream(new byte[16]); g.stream.encryption = new ISAACRandomGen(new int[]{1, 2, 3, 4});
        g.walkableInterfaceId = 197; g.openInterfaceID = 5; g.invOverlayInterfaceID = 6;
        g.closeOpenInterfaces();
        assertEquals(-1, g.openInterfaceID);
        assertEquals(-1, g.invOverlayInterfaceID);
        assertEquals(197, g.walkableInterfaceId);
    }

    // logoutRequestCycles: logout button (content type 205) arms 250 cycles; counts down per cycle; while > 0 a lost connection logs out
    private static int afterOneCycle1011(int value) {
        Game g = new Game();
        g.logoutRequestCycles = value;
        try { g.mainGameProcessor(); } catch (Throwable t) { /* the countdown runs before anything needing a session */ }
        return g.logoutRequestCycles;
    }
    @Test public void anInt1011ArmedOnlyByLogoutContentType205() {
        for (int type : new int[]{205, 204, 206, 0, 201}) {
            Game g = new Game();
            g.logoutRequestCycles = 0;
            RSInterface button = new RSInterface();
            button.contentType = type;
            boolean handled = false;
            try { handled = g.promptUserForInput(button); } catch (Throwable t) { /* other content types may need UI state */ }
            assertEquals("type " + type, type == 205 ? 250 : 0, g.logoutRequestCycles);
            if (type == 205) assertTrue(handled);
        }
    }
    @Test public void anInt1011CountsDownOnePerCycleToZero() {
        assertEquals(249, afterOneCycle1011(250));
        assertEquals(0, afterOneCycle1011(1));
        assertEquals(0, afterOneCycle1011(0));       // stops at 0
        assertEquals(-3, afterOneCycle1011(-3));
    }
    static class DropGame extends Game {
        int logouts, reconnectNotices;
        @Override public void resetLogout() { logouts++; }
        @Override public void drawTextOnScreen(String s, String s1) { reconnectNotices++; throw new IllegalStateException("reconnect path"); }
    }
    @Test public void anInt1011PositiveMakesDropClientLogOutInsteadOfReconnecting() {
        DropGame g = new DropGame();
        g.logoutRequestCycles = 1;
        g.dropClient();
        assertEquals(1, g.logouts); assertEquals(0, g.reconnectNotices);
        g = new DropGame();
        g.logoutRequestCycles = 0;
        try { g.dropClient(); fail(); } catch (IllegalStateException expected) { }
        assertEquals(0, g.logouts); assertEquals(1, g.reconnectNotices);
    }
    private static String afterResetLogout(int value) {
        Game g = new Game();
        g.loggedIn = true; g.pktType = 0; g.myUsername = "someone"; g.loginMessage1 = "";
        g.logoutRequestCycles = value;
        try { g.resetLogout(); } catch (Throwable t) { /* later teardown may need a live client */ }
        return g.loginMessage1;
    }
    @Test public void anInt1011PositiveSuppressesDisconnectedNoticeOnResetLogout() {
        assertEquals("You were disconnected from the server.", afterResetLogout(0));
        assertFalse("You were disconnected from the server.".equals(afterResetLogout(1)));
        assertFalse("You were disconnected from the server.".equals(afterResetLogout(250)));
    }

    // splitPrivateChat / drawSplitPrivateChat: the split-chat option (varp setting action 8) for PRIVATE lines
    @Test public void drawSplitpublicChatDrawsOnlyPrivateTypesNeverPublicTradeOrDuel() throws Exception {
        for (int type = 0; type <= 8; type++) {
            Arrays.fill(DrawingArea.pixels, BG);
            Game g = chat(2);
            msg(g, 0, type, "Ann", "x");
            g.drawSplitPrivateChat();
            boolean privateType = type == 3 || type == 5 || type == 6 || type == 7;
            assertEquals("type " + type, privateType ? Arrays.asList(329) : Arrays.<Integer>asList(), rowsDrawn());
        }
    }
    @Test public void splitpublicChatIsVarpSettingAction8() {
        Varp[] saved = Varp.cache;
        try {
            Varp.cache = new Varp[3];
            for (int i = 0; i < 3; i++) { Varp v; try { v = (Varp) unsafe().allocateInstance(Varp.class); } catch (Exception e) { throw new AssertionError(e); } Varp.cache[i] = v; }
            Varp.cache[1].settingAction = 8;
            Varp.cache[2].settingAction = 5;
            Game g = new Game();
            g.splitPrivateChat = 0;
            g.variousSettings[1] = 1; g.applyVarpSetting(1); assertEquals(1, g.splitPrivateChat);
            g.variousSettings[1] = 0; g.applyVarpSetting(1); assertEquals(0, g.splitPrivateChat);
            g.variousSettings[2] = 1; g.applyVarpSetting(2); assertEquals(0, g.splitPrivateChat);   // other action untouched
        } finally { Varp.cache = saved; }
    }

    // ---------- drawSplitPrivateChat locals ----------
    private Game chat(int advance) throws Exception {
        Game g = new Game();
        g.splitPrivateChat = 1;
        g.p12TextDrawingArea = font(advance, null);
        g.modIcons[0] = icon(ICON1); g.modIcons[1] = icon(ICON2);
        g.privateChatMode = 0;
        return g;
    }
    private static void msg(Game g, int slot, int type, String name, String text) {
        g.chatTypes[slot] = type; g.chatNames[slot] = name; g.chatMessages[slot] = text;
    }
    private static List<Integer> xs(int y, int color) {
        List<Integer> out = new ArrayList<Integer>();
        for (int x = 0; x < W; x++) if (DrawingArea.pixels[x + y * W] == color) out.add(x);
        return out;
    }
    private static List<Integer> rowsDrawn() {
        List<Integer> out = new ArrayList<Integer>();
        for (int y = 1; y < H; y++) if (DrawingArea.pixels[4 + (y - 1) * W] == CYAN && DrawingArea.pixels[4 + y * W] == 0) out.add(y);
        return out;
    }
    @Test public void iRowsStartAt329AndStep13UpwardAndJSlot0IsBottom() throws Exception {
        Game g = chat(2);
        msg(g, 0, 5, null, "a");
        msg(g, 1, 5, null, "bbb");
        g.drawSplitPrivateChat();
        assertEquals(Arrays.asList(316, 329), rowsDrawn());
        assertEquals(Arrays.asList(4), xs(328, CYAN));            // slot 0 (one char) on the bottom row
        assertEquals(Arrays.asList(4, 6, 8), xs(315, CYAN));      // slot 1 (three chars) one row up
    }
    @Test public void iStartsAtOneWhileUpdateNoticeShows() throws Exception {
        Game g = chat(2);
        msg(g, 0, 5, null, "a");
        g.systemUpdateCycles = 1;
        g.drawSplitPrivateChat();
        assertEquals(Arrays.asList(316), rowsDrawn());
        g = chat(2); msg(g, 0, 5, null, "a"); g.systemUpdateCycles = 0;
        Arrays.fill(DrawingArea.pixels, BG);
        g.drawSplitPrivateChat();
        assertEquals(Arrays.asList(329), rowsDrawn());
    }
    @Test public void iStopsAfterRowFour() throws Exception {
        Game g = chat(2);
        for (int s = 0; s < 7; s++) msg(g, s, 5, null, "m");
        g.drawSplitPrivateChat();
        assertEquals(Arrays.asList(277, 290, 303, 316, 329), rowsDrawn());
        Arrays.fill(DrawingArea.pixels, BG);
        g.systemUpdateCycles = 9;
        g.drawSplitPrivateChat();
        assertEquals(Arrays.asList(277, 290, 303, 316), rowsDrawn());
    }
    @Test public void kFiltersTypesAndOnlyDrawnRowsConsumeI() throws Exception {
        Game g = chat(2);
        msg(g, 0, 0, "Pub", "public");    // public chat: skipped, no row
        msg(g, 1, 4, "Tr", "trade");      // other type: skipped
        msg(g, 2, 6, "Bob", "hi");        // "To Bob: hi"
        msg(g, 3, 3, "Ann", "yo");        // "From" + "Ann: yo"
        g.drawSplitPrivateChat();
        assertEquals(Arrays.asList(316, 329), rowsDrawn());
        assertEquals(Arrays.asList(4, 6, 10, 12, 14, 16, 20, 22), xs(328, CYAN));
        assertEquals(Arrays.asList(4, 6, 8, 10, 14, 16, 18, 20, 24, 26), xs(315, CYAN));
    }
    @Test public void kSevenIgnoresPrivateChatModeButThreeFiveSixDoNot() throws Exception {
        Game g = chat(2);
        g.privateChatMode = 2;
        msg(g, 0, 3, "Ann", "a");
        msg(g, 1, 5, null, "b");
        msg(g, 2, 6, "Bob", "c");
        msg(g, 3, 7, "Cid", "d");
        g.drawSplitPrivateChat();
        assertEquals(Arrays.asList(329), rowsDrawn());
        assertEquals(Arrays.asList(4, 6, 8, 10, 14, 16, 18, 20, 24), xs(328, CYAN));
    }
    @Test public void sStripsCrownPrefixAndByte1PicksIconAndShiftsK1() throws Exception {
        Game g = chat(2);
        msg(g, 0, 3, "Bob", "hi");
        msg(g, 1, 3, "@cr1@Bob", "hi");
        msg(g, 2, 3, "@cr2@Bob", "hi");
        g.drawSplitPrivateChat();
        assertEquals(Arrays.asList(4, 6, 8, 10, 14, 16, 18, 20, 24, 26), xs(328, CYAN));
        assertEquals(Arrays.asList(4, 6, 8, 10, 28, 30, 32, 34, 38, 40), xs(315, CYAN)); // prefix gone, text +14
        assertEquals(Arrays.asList(4, 6, 8, 10, 28, 30, 32, 34, 38, 40), xs(302, CYAN));
        assertEquals(Arrays.asList(), xs(329 - 12, ICON1));
        assertEquals(Arrays.asList(14), xs(316 - 12, ICON1));
        assertEquals(Arrays.asList(), xs(316 - 12, ICON2));
        assertEquals(Arrays.asList(14), xs(303 - 12, ICON2));
        assertEquals(Arrays.asList(), xs(303 - 12, ICON1));
    }
    @Test public void k1StartsAfterFromSpaceWidth() throws Exception {
        Game g = chat(3);                 // "From " is now 15 px wide: text starts at 19
        msg(g, 0, 7, "Bo", "x");
        g.drawSplitPrivateChat();
        assertEquals(Arrays.asList(4, 7, 10, 13, 19, 22, 25, 31), xs(328, CYAN));
        assertEquals(Arrays.asList(4, 7, 10, 13, 19, 22, 25, 31), xs(329, 0)); // black pass on the baseline itself
    }
    @Test public void splitChatDisabledDrawsNothing() throws Exception {
        Game g = chat(2);
        g.splitPrivateChat = 0;
        msg(g, 0, 5, null, "a");
        g.drawSplitPrivateChat();
        assertEquals(Arrays.asList(), rowsDrawn());
    }

    // ---------- buildSplitPrivateChatMenu locals ----------
    private static List<String> menu(Game g, int mouseX, int mouseY) {
        g.menuActionRow = 0; g.mouseX = mouseX; g.mouseY = mouseY; g.myPrivilege = 0;
        g.buildSplitPrivateChatMenu();
        List<String> out = new ArrayList<String>();
        for (int n = 0; n < g.menuActionRow; n++) out.add(g.menuActionName[n]);
        return out;
    }
    private static List<String> rows(String name) {
        return Arrays.asList("Add ignore @whi@" + name, "Reply to @whi@" + name, "Add friend @whi@" + name);
    }
    @Test public void lHitRowIsMouseYMinus4InLMinus10ToLPlus3() throws Exception {
        Game g = chat(2);
        msg(g, 0, 3, "Bob", "hi");
        assertEquals(rows("Bob"), menu(g, 10, 324));
        assertEquals(Arrays.asList(), menu(g, 10, 323));
        assertEquals(rows("Bob"), menu(g, 10, 336));
        assertEquals(Arrays.asList(), menu(g, 10, 337));
    }
    @Test public void iShiftsHitRowsWhileUpdateNoticeShowsAndJSlot1IsOneRowUp() throws Exception {
        Game g = chat(2);
        msg(g, 0, 3, "Bob", "hi");
        msg(g, 1, 3, "Ann", "hi");
        assertEquals(rows("Ann"), menu(g, 10, 320));
        g.systemUpdateCycles = 5;
        assertEquals(rows("Bob"), menu(g, 10, 320));
        assertEquals(Arrays.asList(), menu(g, 10, 330));
        assertEquals(rows("Ann"), menu(g, 10, 307));
    }
    @Test public void kFiveAndSixConsumeRowsWithoutEntriesOthersSkip() throws Exception {
        Game g = chat(2);
        msg(g, 0, 0, "Pub", "x");   // skipped, no row
        msg(g, 1, 5, null, "x");    // row 0, no entries
        msg(g, 2, 6, "To", "x");    // row 1, no entries
        msg(g, 3, 7, "Cid", "x");   // row 2
        assertEquals(Arrays.asList(), menu(g, 10, 330));
        assertEquals(Arrays.asList(), menu(g, 10, 317));
        assertEquals(rows("Cid"), menu(g, 10, 304));
        g.privateChatMode = 2;      // 7 still listed; 5 and 6 no longer take rows
        assertEquals(rows("Cid"), menu(g, 10, 330));
    }
    @Test public void iStopsAfterRowFourInMenu() throws Exception {
        Game g = chat(2);
        for (int s = 0; s < 6; s++) msg(g, s, 3, "P" + s, "x");
        assertEquals(rows("P4"), menu(g, 10, 277 - 4));
        assertEquals(Arrays.asList(), menu(g, 10, 264 - 4));
    }
    @Test public void sStripsCrownInMenu() throws Exception {
        Game g = chat(2);
        msg(g, 0, 3, "@cr2@Bob", "hi");
        assertEquals(rows("Bob"), menu(g, 10, 330));
    }
    @Test public void i1IsFromWidthPlus25CappedAt450() throws Exception {
        Game g = chat(2);
        msg(g, 0, 3, "Bob", "hi");             // "From:  Bobhi" = 12 * 2 = 24, + 25 = 49
        assertEquals(rows("Bob"), menu(g, 52, 330));
        assertEquals(Arrays.asList(), menu(g, 53, 330));
        assertEquals(rows("Bob"), menu(g, 5, 330));
        assertEquals(Arrays.asList(), menu(g, 4, 330));
        char[] longText = new char[300]; Arrays.fill(longText, 'z');
        msg(g, 0, 3, "Bob", new String(longText));
        assertEquals(rows("Bob"), menu(g, 453, 330));
        assertEquals(Arrays.asList(), menu(g, 454, 330));  // uncapped width would still hit
        Game g3 = chat(3);
        msg(g3, 0, 3, "Bob", "hi");            // 12 * 3 = 36, + 25 = 61
        assertEquals(rows("Bob"), menu(g3, 64, 330));
        assertEquals(Arrays.asList(), menu(g3, 65, 330));
    }
}
