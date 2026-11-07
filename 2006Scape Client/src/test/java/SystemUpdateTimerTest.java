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
 * split private-chat rows it displaces (drawSplitpublicChat / buildSplitPrivateChatMenu locals).
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
        @Override public void drawSplitpublicChat() { splitCalls++; }
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
        g.crossType = 0; g.anInt1018 = -1; g.openInterfaceID = -1; g.menuOpen = false; g.anInt1055 = 0;
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

    // ---------- drawSplitpublicChat locals ----------
    private Game chat(int advance) throws Exception {
        Game g = new Game();
        g.splitpublicChat = 1;
        g.aTextDrawingArea_1271 = font(advance, null);
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
        g.drawSplitpublicChat();
        assertEquals(Arrays.asList(316, 329), rowsDrawn());
        assertEquals(Arrays.asList(4), xs(328, CYAN));            // slot 0 (one char) on the bottom row
        assertEquals(Arrays.asList(4, 6, 8), xs(315, CYAN));      // slot 1 (three chars) one row up
    }
    @Test public void iStartsAtOneWhileUpdateNoticeShows() throws Exception {
        Game g = chat(2);
        msg(g, 0, 5, null, "a");
        g.systemUpdateCycles = 1;
        g.drawSplitpublicChat();
        assertEquals(Arrays.asList(316), rowsDrawn());
        g = chat(2); msg(g, 0, 5, null, "a"); g.systemUpdateCycles = 0;
        Arrays.fill(DrawingArea.pixels, BG);
        g.drawSplitpublicChat();
        assertEquals(Arrays.asList(329), rowsDrawn());
    }
    @Test public void iStopsAfterRowFour() throws Exception {
        Game g = chat(2);
        for (int s = 0; s < 7; s++) msg(g, s, 5, null, "m");
        g.drawSplitpublicChat();
        assertEquals(Arrays.asList(277, 290, 303, 316, 329), rowsDrawn());
        Arrays.fill(DrawingArea.pixels, BG);
        g.systemUpdateCycles = 9;
        g.drawSplitpublicChat();
        assertEquals(Arrays.asList(277, 290, 303, 316), rowsDrawn());
    }
    @Test public void kFiltersTypesAndOnlyDrawnRowsConsumeI() throws Exception {
        Game g = chat(2);
        msg(g, 0, 0, "Pub", "public");    // public chat: skipped, no row
        msg(g, 1, 4, "Tr", "trade");      // other type: skipped
        msg(g, 2, 6, "Bob", "hi");        // "To Bob: hi"
        msg(g, 3, 3, "Ann", "yo");        // "From" + "Ann: yo"
        g.drawSplitpublicChat();
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
        g.drawSplitpublicChat();
        assertEquals(Arrays.asList(329), rowsDrawn());
        assertEquals(Arrays.asList(4, 6, 8, 10, 14, 16, 18, 20, 24), xs(328, CYAN));
    }
    @Test public void sStripsCrownPrefixAndByte1PicksIconAndShiftsK1() throws Exception {
        Game g = chat(2);
        msg(g, 0, 3, "Bob", "hi");
        msg(g, 1, 3, "@cr1@Bob", "hi");
        msg(g, 2, 3, "@cr2@Bob", "hi");
        g.drawSplitpublicChat();
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
        g.drawSplitpublicChat();
        assertEquals(Arrays.asList(4, 7, 10, 13, 19, 22, 25, 31), xs(328, CYAN));
        assertEquals(Arrays.asList(4, 7, 10, 13, 19, 22, 25, 31), xs(329, 0)); // black pass on the baseline itself
    }
    @Test public void splitChatDisabledDrawsNothing() throws Exception {
        Game g = chat(2);
        g.splitpublicChat = 0;
        msg(g, 0, 5, null, "a");
        g.drawSplitpublicChat();
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
