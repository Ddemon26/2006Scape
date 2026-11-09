import static org.junit.Assert.*;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.lang.reflect.Field;
import java.text.AttributedCharacterIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Which screen area each in-game frame image producer (chatModeButtonsIP..1125 and _1163..1166) backs:
 * its size, where it is blitted, and which drawing routine binds it. Original identifiers only.
 */
public class GameFrameAreaProducersTest {
    /** Component whose createImage hands out a distinct image per producer so blits can be attributed. */
    private static final class Fixture extends Component {
        @Override public Image createImage(java.awt.image.ImageProducer p) { return new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB); }
        @Override public boolean prepareImage(Image i, ImageObserver o) { return false; }
    }

    /** Graphics that only records drawImage(image, x, y). */
    private static final class Blits extends Graphics {
        final List<Object[]> calls = new ArrayList<Object[]>();
        @Override public boolean drawImage(Image img, int x, int y, ImageObserver o) { calls.add(new Object[] {img, x, y}); return true; }
        @Override public Graphics create() { return this; }
        @Override public void translate(int x, int y) { }
        @Override public Color getColor() { return Color.BLACK; }
        @Override public void setColor(Color c) { }
        @Override public void setPaintMode() { }
        @Override public void setXORMode(Color c) { }
        @Override public Font getFont() { return null; }
        @Override public void setFont(Font f) { }
        @Override public FontMetrics getFontMetrics(Font f) { return null; }
        @Override public Rectangle getClipBounds() { return null; }
        @Override public void clipRect(int x, int y, int w, int h) { }
        @Override public void setClip(int x, int y, int w, int h) { }
        @Override public Shape getClip() { return null; }
        @Override public void setClip(Shape s) { }
        @Override public void copyArea(int x, int y, int w, int h, int dx, int dy) { }
        @Override public void drawLine(int x1, int y1, int x2, int y2) { }
        @Override public void fillRect(int x, int y, int w, int h) { }
        @Override public void clearRect(int x, int y, int w, int h) { }
        @Override public void drawRoundRect(int x, int y, int w, int h, int aw, int ah) { }
        @Override public void fillRoundRect(int x, int y, int w, int h, int aw, int ah) { }
        @Override public void drawOval(int x, int y, int w, int h) { }
        @Override public void fillOval(int x, int y, int w, int h) { }
        @Override public void drawArc(int x, int y, int w, int h, int a, int b) { }
        @Override public void fillArc(int x, int y, int w, int h, int a, int b) { }
        @Override public void drawPolyline(int[] x, int[] y, int n) { }
        @Override public void drawPolygon(int[] x, int[] y, int n) { }
        @Override public void fillPolygon(int[] x, int[] y, int n) { }
        @Override public void drawString(String s, int x, int y) { }
        @Override public void drawString(AttributedCharacterIterator it, int x, int y) { }
        @Override public boolean drawImage(Image img, int x, int y, int w, int h, ImageObserver o) { return true; }
        @Override public boolean drawImage(Image img, int x, int y, Color bg, ImageObserver o) { return true; }
        @Override public boolean drawImage(Image img, int x, int y, int w, int h, Color bg, ImageObserver o) { return true; }
        @Override public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, ImageObserver o) { return true; }
        @Override public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, Color bg, ImageObserver o) { return true; }
        @Override public void dispose() { }
    }

    private static class Rec extends Game {
        boolean stubTabAndChat;
        int tabCalls, chatCalls;
        java.util.Map<Integer, Boolean> animResult;
        List<int[]> animCalls = new ArrayList<int[]>();
        Integer scrollbarSets;
        List<Object[]> scrollbarCalls = new ArrayList<Object[]>();
        @Override public void drawTabArea() { tabCalls++; if (!stubTabAndChat) super.drawTabArea(); }
        @Override public void drawChatArea() { chatCalls++; if (!stubTabAndChat) super.drawChatArea(); }
        @Override public boolean advanceInterfaceModelAnimations(int t, int id) {
            if (animResult == null) return super.advanceInterfaceModelAnimations(t, id);
            animCalls.add(new int[] {t, id});
            Boolean b = animResult.get(id);
            return b != null && b;
        }
        @Override public void handleScrollbarInput(int a, int b, int c, int d, RSInterface comp, int e, boolean f, int h) {
            if (scrollbarSets == null) { super.handleScrollbarInput(a, b, c, d, comp, e, f, h); return; }
            scrollbarCalls.add(new Object[] {a, b, c, d, comp, e, f, h});
            comp.scrollPosition = scrollbarSets;
        }
    }

    private static TextDrawingArea blankFont() throws Exception {
        sun.misc.Unsafe u;
        Field uf = sun.misc.Unsafe.class.getDeclaredField("theUnsafe"); uf.setAccessible(true);
        u = (sun.misc.Unsafe) uf.get(null);
        TextDrawingArea t = (TextDrawingArea) u.allocateInstance(TextDrawingArea.class);
        byte[][] masks = new byte[256][];
        for (int c = 0; c < 256; c++) masks[c] = new byte[0];
        set(t, "glyphMasks", masks); set(t, "glyphWidthPixels", new int[256]); set(t, "glyphHeightPixels", new int[256]);
        set(t, "glyphXOffsetPixels", new int[256]); set(t, "glyphYOffsetPixels", new int[256]);
        set(t, "glyphAdvancePixels", new int[256]); set(t, "jitterRandom", new java.util.Random(0)); set(t, "strikeThroughEnabled", false);
        t.maxAsciiGlyphHeightPixels = 0;
        return t;
    }
    private static void set(Object o, String name, Object v) throws Exception {
        Field f = o.getClass().getDeclaredField(name); f.setAccessible(true); f.set(o, v);
    }

    private final DrawingPrimitivesTest state = new DrawingPrimitivesTest();
    private Object savedLineOffsets;
    private Rec g;
    private Blits out;

    private static Background solid(int w, int h, int rgb) throws Exception {
        sun.misc.Unsafe u;
        Field f = sun.misc.Unsafe.class.getDeclaredField("theUnsafe"); f.setAccessible(true);
        u = (sun.misc.Unsafe) f.get(null);
        Background b = (Background) u.allocateInstance(Background.class);
        b.imageWidth = w; b.imageHeight = h; b.offsetX = 0; b.offsetY = 0;
        b.paletteIndices = new byte[w * h];
        Arrays.fill(b.paletteIndices, (byte) 1);
        Field pal = Background.class.getDeclaredField("paletteRgb"); pal.setAccessible(true);
        pal.set(b, new int[] {0, rgb});
        return b;
    }

    @Before public void before() throws Exception {
        state.saveState();
        savedLineOffsets = Texture.lineOffsets;
        g = new Rec();
        out = new Blits();
        g.graphics = out;
        g.loadingStage = 0;
        g.fullScreenInterfaceId = -1;
        g.invOverlayInterfaceID = -1;
        g.backDialogID = -1;
        g.dialogID = -1;
        g.flashingTabId = -1;
        g.menuOpen = false;
    }

    @After public void after() { Texture.lineOffsets = (int[]) savedLineOffsets; state.restoreState(); }

    private static int fieldInt(Object o, String n) throws Exception { Field f = o.getClass().getDeclaredField(n); f.setAccessible(true); return f.getInt(o); }
    private static int wid(RSImageProducer p) throws Exception { return fieldInt(p, "width"); }
    private static int hei(RSImageProducer p) throws Exception { return fieldInt(p, "height"); }
    private static Object img(RSImageProducer p) throws Exception { Field f = RSImageProducer.class.getDeclaredField("image"); f.setAccessible(true); return f.get(p); }

    private static boolean all(int[] a, int v) { for (int x : a) if (x != v) return false; return true; }
    private static boolean none(int[] a) { return all(a, 0); }

    private RSImageProducer[] all7() {
        return new RSImageProducer[] {g.chatModeButtonsIP, g.tabIconsBottomIP, g.tabIconsTopIP,
                g.tabPanelIP, g.minimapIP, g.worldViewIP, g.chatPanelIP};
    }

    /** Blit position (x, y) of the producer's image, or null when it was not blitted; fails on repeats. */
    private int[] blit(RSImageProducer p) throws Exception {
        int[] r = null;
        for (Object[] c : out.calls) if (c[0] == img(p)) {
            assertNull("one blit only", r);
            r = new int[] {(Integer) c[1], (Integer) c[2]};
        }
        return r;
    }

    @Test public void resetImageProducers2CreatesTheSevenProducersWithTheirAreaSizes() throws Exception {
        g.mapBack = solid(172, 156, 0x00aa11);
        // chatPanelIP != null means "already created": nothing is replaced.
        RSImageProducer sentinel = new RSImageProducer(1, 1, new Fixture());
        g.chatPanelIP = sentinel;
        g.resetImageProducers2();
        assertSame(sentinel, g.chatPanelIP);
        assertNull(g.minimapIP); assertNull(g.worldViewIP); assertNull(g.tabPanelIP);
        assertNull(g.chatModeButtonsIP); assertNull(g.tabIconsBottomIP); assertNull(g.tabIconsTopIP);
        g.chatPanelIP = null;
        Rec withComponent = new Rec() { @Override Component getGameComponent() { return new Fixture(); } };
        withComponent.mapBack = g.mapBack;
        withComponent.resetImageProducers2();
        int[][] expect = {{496, 50}, {269, 37}, {249, 45}, {190, 261}, {172, 156}, {512, 334}, {479, 96}};
        RSImageProducer[] p = {withComponent.chatModeButtonsIP, withComponent.tabIconsBottomIP, withComponent.tabIconsTopIP,
                withComponent.tabPanelIP, withComponent.minimapIP, withComponent.worldViewIP,
                withComponent.chatPanelIP};
        for (int i = 0; i < 7; i++) {
            assertEquals("width " + i, expect[i][0], wid(p[i]));
            assertEquals("height " + i, expect[i][1], hei(p[i]));
            assertEquals("pixels " + i, expect[i][0] * expect[i][1], p[i].pixels.length);
        }
        // mapBack is painted into the 172x156 producer (minimapIP) only.
        assertTrue(all(p[4].pixels, 0x00aa11));
        for (int i = 0; i < 7; i++) if (i != 4) assertTrue("no map background in " + i, none(p[i].pixels));
        // The 512x334 producer is left bound as the drawing area.
        assertSame(p[5].pixels, DrawingArea.pixels);
        assertEquals(512, DrawingArea.width); assertEquals(334, DrawingArea.height);
    }

    @Test public void drawChatAreaDrawsChatBackIntoTheFullWidthChatProducerAndBlitsItAt17_357() throws Exception {
        Fixture c = new Fixture();
        g.chatPanelIP = new RSImageProducer(479, 96, c);
        g.worldViewIP = new RSImageProducer(512, 334, c);
        g.tabPanelIP = new RSImageProducer(190, 261, c);
        g.minimapIP = new RSImageProducer(172, 156, c);
        g.chatBack = solid(479, 96, 0x445566);
        g.scrollUpArrow = solid(16, 16, 0x990000); g.scrollDownArrow = solid(16, 16, 0x990000);
        g.chatTextDrawingArea = blankFont(); g.aTextDrawingArea_1271 = blankFont();
        g.drawChatArea();
        int[] q = g.chatPanelIP.pixels;
        // chatBack covers the whole 479x96 producer; only the scrollbar column x>=463, y<77 is drawn over it.
        assertEquals(0x445566, q[0]); assertEquals(0x445566, q[462]); assertEquals(0x445566, q[95 * 479]);
        assertEquals(0x445566, q[95 * 479 + 478]); assertEquals(0x445566, q[60 * 479 + 100]);
        assertEquals(0x990000, q[463]);
        assertTrue(none(g.worldViewIP.pixels)); assertTrue(none(g.tabPanelIP.pixels));
        assertTrue(none(g.minimapIP.pixels));
        assertArrayEquals(new int[] {17, 357}, blit(g.chatPanelIP));
        assertEquals(1, out.calls.size());
        // Left bound as the world-view raster afterwards.
        assertSame(g.worldViewIP.pixels, DrawingArea.pixels);
    }

    @Test public void drawTabAreaDrawsInvBackIntoThe190x261ProducerAndBlitsItAt553_205() throws Exception {
        Fixture c = new Fixture();
        g.chatPanelIP = new RSImageProducer(479, 96, c);
        g.worldViewIP = new RSImageProducer(512, 334, c);
        g.tabPanelIP = new RSImageProducer(190, 261, c);
        g.minimapIP = new RSImageProducer(172, 156, c);
        g.invBack = solid(190, 261, 0x778899);
        g.drawTabArea();
        assertTrue(all(g.tabPanelIP.pixels, 0x778899));
        assertTrue(none(g.worldViewIP.pixels)); assertTrue(none(g.chatPanelIP.pixels));
        assertTrue(none(g.minimapIP.pixels));
        assertArrayEquals(new int[] {553, 205}, blit(g.tabPanelIP));
        assertEquals(1, out.calls.size());
        assertSame(g.worldViewIP.pixels, DrawingArea.pixels);
    }

    @Test public void firstFrameBlitsMinimapAndWorldProducersThenTheThreeStripProducers() throws Exception {
        final Fixture c = new Fixture();
        Rec f = new Rec() { @Override Component getGameComponent() { return c; } };
        f.stubTabAndChat = true;
        f.graphics = out;
        f.loadingStage = 0; f.fullScreenInterfaceId = -1; f.invOverlayInterfaceID = -1; f.backDialogID = -1;
        f.dialogID = -1; f.flashingTabId = -1;
        f.mapBack = solid(172, 156, 0x00aa11);
        f.backHmid1 = solid(249, 45, 0x101010);   // bound while tabIconsTopIP is the drawing area
        f.backBase2 = solid(269, 37, 0x202020);   // bound while tabIconsBottomIP is the drawing area
        f.backBase1 = solid(496, 50, 0x303030);   // bound while chatModeButtonsIP is the drawing area
        for (String n : new String[] {"backLeftIP1", "backLeftIP2", "backRightIP1", "backRightIP2", "backTopIP1",
                "backVmidIP1", "backVmidIP2", "backVmidIP3", "backVmidIP2_2"}) {
            Game.class.getField(n).set(f, new RSImageProducer(2, 2, c));
        }
        f.aTextDrawingArea_1271 = blankFont();
        f.welcomeScreenRaised = true;
        f.tabInterfaceIDs[0] = -1;
        f.drawGameScreen();
        RSImageProducer[] p = {f.chatModeButtonsIP, f.tabIconsBottomIP, f.tabIconsTopIP,
                f.tabPanelIP, f.minimapIP, f.worldViewIP, f.chatPanelIP};
        out.calls.size();
        // Attribute each blit to a producer by image identity.
        int[][] pos = new int[7][];
        for (int i = 0; i < 7; i++) for (Object[] call : out.calls) if (call[0] == img(p[i])) {
            assertNull("blit once " + i, pos[i]);
            pos[i] = new int[] {(Integer) call[1], (Integer) call[2]};
        }
        assertArrayEquals(new int[] {0, 453}, pos[0]);     // 496x50, y=453 x=0
        assertArrayEquals(new int[] {496, 466}, pos[1]);   // 269x37
        assertArrayEquals(new int[] {516, 160}, pos[2]);   // 249x45
        assertNull(pos[3]); assertNull(pos[6]);            // tab and chat panels are the stubbed routines
        assertArrayEquals(new int[] {550, 4}, pos[4]);     // 172x156 minimap
        assertArrayEquals(new int[] {4, 4}, pos[5]);       // 512x334 world view
        assertTrue(all(p[2].pixels, 0x101010)); assertTrue(all(p[1].pixels, 0x202020)); assertTrue(all(p[0].pixels, 0x303030));
        assertTrue(all(p[4].pixels, 0x00aa11));
        assertTrue(none(p[3].pixels)); assertTrue(none(p[5].pixels)); assertTrue(none(p[6].pixels));
        // Right edge / bottom edge of every area stays inside the 765x503 frame.
        for (int i : new int[] {0, 1, 2, 4, 5}) {
            assertTrue(pos[i][0] + wid(p[i]) <= 765); assertTrue(pos[i][1] + hei(p[i]) <= 503);
        }
    }

    /** A Rec wired for drawGameScreen with the tab/chat routines stubbed and loadingStage 0. */
    private Rec frame(final Fixture c) throws Exception {
        Rec f = new Rec() { @Override Component getGameComponent() { return c; } };
        f.stubTabAndChat = true;
        f.graphics = out;
        f.loadingStage = 0; f.fullScreenInterfaceId = -1; f.invOverlayInterfaceID = -1; f.backDialogID = -1;
        f.dialogID = -1; f.flashingTabId = -1;
        f.worldViewIP = new RSImageProducer(512, 334, c);
        f.chatModeButtonsIP = new RSImageProducer(496, 50, c);
        f.backBase1 = solid(496, 50, 0x303030);
        f.aTextDrawingArea_1271 = blankFont();
        return f;
    }

    @Test public void needDrawChatModeButtonsGatesTheChatModeButtonStripRedrawAndIsConsumed() throws Exception {
        Fixture c = new Fixture();
        Rec f = frame(c);
        f.needDrawTabArea = true; f.inputTaken = true;          // other redraw flags must not draw the strip
        f.drawGameScreen();
        assertNull(blit(f.chatModeButtonsIP)); assertTrue(none(f.chatModeButtonsIP.pixels));
        assertFalse(f.needDrawChatModeButtons);
        f.needDrawChatModeButtons = true;
        f.drawGameScreen();
        assertArrayEquals(new int[] {0, 453}, blit(f.chatModeButtonsIP));
        assertTrue(all(f.chatModeButtonsIP.pixels, 0x303030));
        assertFalse(f.needDrawChatModeButtons);
        int n = out.calls.size();
        f.drawGameScreen();                                      // consumed: no second strip blit
        assertEquals(n, out.calls.size());
    }

    @Test public void chatModeButtonClickSetsAboolean1233() throws Exception {
        Fixture c = new Fixture();
        Rec f = frame(c);
        f.stream = new Stream(new byte[64]);
        f.stream.encryption = new ISAACRandomGen(new int[] {9, 8, 7, 6});
        f.publicChatMode = 0;
        f.clickMode3 = 1; f.saveClickX = 5; f.saveClickY = 480;   // just left of the public chat button
        f.processChatModeClick();
        assertFalse(f.needDrawChatModeButtons); assertEquals(0, f.publicChatMode);
        f.saveClickX = 50;
        f.processChatModeClick();
        assertTrue(f.needDrawChatModeButtons); assertEquals(1, f.publicChatMode);
    }

    @Test public void drawGameScreenLocalsFlag1AndFlag2AreTheModelAnimationAdvancedResults() throws Exception {
        Fixture c = new Fixture();
        Rec f = frame(c);
        f.animResult = new java.util.HashMap<Integer, Boolean>();
        f.invOverlayInterfaceID = 5; f.backDialogID = 7; f.cyclesSinceDraw = 3;
        f.drawGameScreen();
        assertEquals(0, f.tabCalls); assertEquals(0, f.chatCalls);
        assertEquals(2, f.animCalls.size());
        assertArrayEquals(new int[] {3, 5}, f.animCalls.get(0)); assertArrayEquals(new int[] {3, 7}, f.animCalls.get(1));
        assertEquals(0, f.cyclesSinceDraw);
        f.animResult.put(5, true);                                // inventory overlay animated: tab area only
        f.drawGameScreen();
        assertEquals(1, f.tabCalls); assertEquals(0, f.chatCalls); assertFalse(f.needDrawTabArea);
        f.animResult.put(5, false); f.animResult.put(7, true);    // back dialog animated: chat area only
        f.drawGameScreen();
        assertEquals(1, f.tabCalls); assertEquals(1, f.chatCalls); assertFalse(f.inputTaken);
    }

    @Test public void drawGameScreenLocalIIsTheClampedChatScrollOffsetFromTheScrollbar() throws Exception {
        Fixture c = new Fixture();
        Rec f = frame(c);
        f.anInt1211 = 300; f.anInt1089 = 40;
        f.drawGameScreen();                                       // mouse outside: scrollbar position derives from anInt1089
        assertEquals(300 - 40 - 77, f.aClass9_1059.scrollPosition);
        assertEquals(40, f.anInt1089); assertEquals(0, f.chatCalls); assertEquals(0, f.scrollbarCalls.size());
        f.scrollbarSets = 100;
        f.mouseX = 448; f.mouseY = 340; f.drawGameScreen();       // mouseX boundary: not > 448
        f.mouseX = 500; f.mouseY = 332; f.drawGameScreen();       // mouseY boundary: not > 332
        assertEquals(0, f.scrollbarCalls.size());
        f.mouseX = 500; f.mouseY = 340;
        f.drawGameScreen();
        assertEquals(1, f.scrollbarCalls.size());
        Object[] a = f.scrollbarCalls.get(0);
        assertArrayEquals(new Object[] {463, 77, 483, -17, f.aClass9_1059, 0, false, 300}, a);
        assertEquals(300 - 77 - 100, f.anInt1089); assertEquals(1, f.chatCalls); assertFalse(f.inputTaken);
        f.scrollbarSets = -50; f.drawGameScreen();                // above the top: clamped to anInt1211 - 77
        assertEquals(223, f.anInt1089); assertEquals(2, f.chatCalls);
        f.scrollbarSets = 500; f.drawGameScreen();                // below the bottom: clamped to 0
        assertEquals(0, f.anInt1089); assertEquals(3, f.chatCalls);
        f.scrollbarSets = 223; f.drawGameScreen();                // equals current offset (0): no redraw
        assertEquals(0, f.anInt1089); assertEquals(3, f.chatCalls);
    }
}
