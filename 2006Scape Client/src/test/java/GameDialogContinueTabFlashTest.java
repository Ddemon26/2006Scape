import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Random;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Original-name observations for the dialog "click here to continue" pending flag (dialogContinuePending) and the server-flashed
 * sidebar tab (flashingTabId): packet 24, processTabClick, doAction 679, buildInterfaceMenu, drawInterface and the dialog packets.
 */
public class GameDialogContinueTabFlashTest {
    private static final int W = 200, H = 100, BG = 0x123456, TEXT = 0x00ff00;
    private int[] savedPixels, state;
    private RSInterface[] savedCache;

    @Before public void before() {
        savedPixels = DrawingArea.pixels;
        state = new int[]{DrawingArea.width, DrawingArea.height, DrawingArea.topX, DrawingArea.topY, DrawingArea.bottomX,
            DrawingArea.bottomY, DrawingArea.scanlineRightLimit, DrawingArea.clipRightHalf, DrawingArea.clipBottomHalf};
        DrawingArea.bindRaster(H, W, new int[W * H]);
        Arrays.fill(DrawingArea.pixels, BG);
        savedCache = RSInterface.interfaceCache;
        RSInterface.interfaceCache = new RSInterface[100];
    }
    @After public void after() {
        DrawingArea.pixels = savedPixels; DrawingArea.width = state[0]; DrawingArea.height = state[1]; DrawingArea.topX = state[2];
        DrawingArea.topY = state[3]; DrawingArea.bottomX = state[4]; DrawingArea.bottomY = state[5];
        DrawingArea.scanlineRightLimit = state[6]; DrawingArea.clipRightHalf = state[7]; DrawingArea.clipBottomHalf = state[8];
        RSInterface.interfaceCache = savedCache;
    }

    private static Game game() {
        Game g = new Game();
        g.stream = new Stream(new byte[64]);
        g.stream.encryption = new ISAACRandomGen(new int[]{9, 8, 7, 6});
        return g;
    }
    private static int key() { return new ISAACRandomGen(new int[]{9, 8, 7, 6}).getNextKey(); }

    /** Delivers one server packet through a real socket and runs parsePacket once. */
    private static void deliver(Game g, int... bytes) throws Exception {
        InetAddress host = InetAddress.getByName("127.0.0.1");
        try (ServerSocket listener = new ServerSocket(0, 1, host);
             Socket client = new Socket(host, listener.getLocalPort());
             Socket server = listener.accept()) {
            byte[] b = new byte[bytes.length];
            for (int n = 0; n < b.length; n++) b[n] = (byte) bytes[n];
            server.getOutputStream().write(b);
            server.getOutputStream().flush();
            g.socketStream = new RSSocket(null, client);
            long end = System.currentTimeMillis() + 5000;
            while (g.socketStream.available() < b.length && System.currentTimeMillis() < end) Thread.sleep(5);
            g.inStream = new Stream(new byte[6000]);
            g.pktType = -1;
            assertTrue(g.parsePacket());
            assertEquals(-1, g.pktType);
        }
    }

    // ---------- flashingTabId: packet 24 stores the flashed tab (128 - byte) and moves the selection off it ----------
    @Test public void packet24StoresTabAndMovesSelectionOffIt() throws Exception {
        assertEquals(1, SizeConstants.packetSizes[24]);
        Game g = game(); g.tabID = 4;
        deliver(g, 24, 128 - 6);
        assertEquals(6, g.flashingTabId);
        assertEquals(4, g.tabID);                    // another tab selected: selection untouched
        g = game(); g.tabID = 6;
        deliver(g, 24, 128 - 6);
        assertEquals(6, g.flashingTabId);
        assertEquals(3, g.tabID);                    // flashed tab was selected: moved to inventory
        g = game(); g.tabID = 3;
        deliver(g, 24, 128 - 3);
        assertEquals(3, g.flashingTabId);
        assertEquals(1, g.tabID);                    // flashed tab is the inventory itself: moved to skills
        g = game(); g.tabID = 2;
        deliver(g, 24, 128);
        assertEquals(0, g.flashingTabId);                // 128-minus encoding, not the raw byte
    }

    // ---------- flashingTabId: clicking the flashed tab reports opcode 152 with the tab index ----------
    private static Game clickTab(int flashed, int x) {
        Game g = game();
        Arrays.fill(g.tabInterfaceIDs, -1);
        g.tabInterfaceIDs[0] = 10; g.tabInterfaceIDs[6] = 16;
        g.tabID = 3; g.flashingTabId = flashed;
        g.clickMode3 = 1; g.saveClickX = x; g.saveClickY = 180;
        g.processTabClick();
        return g;
    }
    @Test public void clickingFlashedTabSendsOpcode152() {
        Game g = clickTab(6, 730);
        assertEquals(6, g.tabID);
        assertEquals(2, g.stream.currentOffset);
        assertEquals((byte) (152 + key()), g.stream.buffer[0]);
        assertEquals(6, g.stream.buffer[1]);
        g = clickTab(6, 550);                         // a different tab: no report
        assertEquals(0, g.tabID);
        assertEquals(0, g.stream.currentOffset);
        g = clickTab(-1, 730);                        // nothing flashing: no report
        assertEquals(6, g.tabID);
        assertEquals(0, g.stream.currentOffset);
        g = clickTab(0, 550);                         // tab 0 is a real value, not "none"
        assertEquals(2, g.stream.currentOffset);
        assertEquals(0, g.stream.buffer[1]);
    }

    // ---------- dialogContinuePending: continue click is sent once and then blocked ----------
    private static Game continueAction(Game g, int componentId) {
        g.menuActionID[0] = 679; g.menuActionCmd3[0] = componentId; g.menuActionCmd2[0] = 0; g.menuActionCmd1[0] = 0;
        g.doAction(0);
        return g;
    }
    @Test public void continueActionSendsOpcode40OnceUntilCleared() {
        Game g = game();
        g.dialogContinuePending = false;
        continueAction(g, 0x1234);
        assertTrue(g.dialogContinuePending);
        assertEquals(3, g.stream.currentOffset);
        assertEquals((byte) (40 + key()), g.stream.buffer[0]);
        assertEquals(0x12, g.stream.buffer[1] & 0xff);   // component id, big-endian (high byte first)
        assertEquals(0x34, g.stream.buffer[2] & 0xff);
        continueAction(g, 0x1234);                      // still pending: second click is swallowed
        assertEquals(3, g.stream.currentOffset);
        g.dialogContinuePending = false;
        continueAction(g, 0x0042);                      // cleared: next click goes out again
        assertEquals(6, g.stream.currentOffset);
        assertEquals(0x00, g.stream.buffer[4] & 0xff); assertEquals(0x42, g.stream.buffer[5] & 0xff);
    }

    private static RSInterface continueScene(int actionType) {
        RSInterface root = new RSInterface();
        root.id = 1; root.type = 0; root.width = W; root.height = H;
        root.children = new int[]{2}; root.childX = new int[]{10}; root.childY = new int[]{20};
        RSInterface c = new RSInterface();
        c.id = 2; c.type = 4; c.width = 60; c.height = 10; c.atActionType = actionType; c.tooltip = "Continue";
        RSInterface.interfaceCache[1] = root; RSInterface.interfaceCache[2] = c;
        return root;
    }
    @Test public void continueMenuEntryOnlyOfferedWhileNotPending() {
        Game g = game();
        RSInterface root = continueScene(6);
        g.menuActionRow = 0; g.dialogContinuePending = false; g.buildInterfaceMenu(0, root, 15, 0, 24, 0);
        assertEquals(1, g.menuActionRow);
        assertEquals(679, g.menuActionID[0]);
        assertEquals(2, g.menuActionCmd3[0]);
        g.menuActionRow = 0; g.dialogContinuePending = true; g.buildInterfaceMenu(0, root, 15, 0, 24, 0);
        assertEquals(0, g.menuActionRow);
    }

    // ---------- dialogContinuePending: continue text becomes "Please wait..." while pending ----------
    private static Object unsafe() throws Exception {
        Field f = sun.misc.Unsafe.class.getDeclaredField("theUnsafe"); f.setAccessible(true); return f.get(null);
    }
    private static void set(Object o, String name, Object v) throws Exception {
        Field f = o.getClass().getDeclaredField(name); f.setAccessible(true); f.set(o, v);
    }
    /** One-pixel glyphs, one pixel advance, each glyph drawn on one row: drawn pixel count = text length. */
    private static TextDrawingArea font() throws Exception {
        TextDrawingArea t = (TextDrawingArea) ((sun.misc.Unsafe) unsafe()).allocateInstance(TextDrawingArea.class);
        byte[][] masks = new byte[256][];
        int[] one = new int[256], adv = new int[256];
        for (int c = 0; c < 256; c++) { masks[c] = new byte[]{1}; one[c] = 1; adv[c] = 1; }
        set(t, "glyphMasks", masks); set(t, "glyphWidthPixels", one); set(t, "glyphHeightPixels", one.clone());
        set(t, "glyphXOffsetPixels", new int[256]); set(t, "glyphYOffsetPixels", new int[256]);
        set(t, "glyphAdvancePixels", adv); set(t, "jitterRandom", new Random(0)); set(t, "strikeThroughEnabled", false);
        t.maxAsciiGlyphHeightPixels = 1;
        return t;
    }
    private static int drawnText(boolean pending, int actionType) throws Exception {
        Arrays.fill(DrawingArea.pixels, BG);
        RSInterface root = continueScene(actionType);
        RSInterface c = RSInterface.interfaceCache[2];
        c.textDrawingAreas = font(); c.disabledText = "Continue!!"; c.enabledText = ""; c.textColor = TEXT;
        Game g = game();
        g.dialogContinuePending = pending;
        g.drawInterface(0, 0, root, 0);
        int n = 0;
        for (int p : DrawingArea.pixels) if (p == TEXT) n++;
        return n;
    }
    @Test public void continueTextReplacedByPleaseWaitWhilePending() throws Exception {
        assertEquals("Continue!!".length(), drawnText(false, 6));
        assertEquals("Pleasewait...".length(), drawnText(true, 6));          // the space glyph draws no pixel
        assertEquals("Continue!!".length(), drawnText(true, 1));  // only continue-type (6) components are replaced
    }

    // ---------- dialogContinuePending: dialog open/close packets clear it, unrelated packets do not ----------
    private static boolean afterPacket(int... bytes) throws Exception {
        Game g = game();
        g.dialogContinuePending = true;
        deliver(g, bytes);
        return g.dialogContinuePending;
    }
    @Test public void dialogPacketsClearPendingContinue() throws Exception {
        assertFalse(afterPacket(219));                // close all interfaces
        assertFalse(afterPacket(164, 5, 0));          // open chatbox dialog 5
        assertTrue(afterPacket(85, 1, 2));            // update-block origin: unrelated, stays pending
        assertTrue(afterPacket(24, 128 - 6));         // tab flash: unrelated, stays pending
    }
}
