import static org.junit.Assert.*;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.text.AttributedCharacterIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Semantics of the title-screen image producers (titleTopBannerProducer..1115) and flameThreadActive:
 * size, screen position, what is drawn into each, and the flame-thread flag.
 */
public class TitleScreenProducersTest {
    private static final int TITLE_W = 382, TITLE_H = 503;

    /** Graphics that only records drawImage(image, x, y) calls. */
    private static final class Rec extends Graphics {
        final List<Object[]> calls = new ArrayList<>();
        @Override public boolean drawImage(Image img, int x, int y, ImageObserver o) { calls.add(new Object[] {img, x, y}); return true; }
        @Override public Graphics create() { return this; }
        @Override public void translate(int x, int y) {}
        @Override public Color getColor() { return Color.BLACK; }
        @Override public void setColor(Color c) {}
        @Override public void setPaintMode() {}
        @Override public void setXORMode(Color c) {}
        @Override public Font getFont() { return null; }
        @Override public void setFont(Font f) {}
        @Override public FontMetrics getFontMetrics(Font f) { return null; }
        @Override public Rectangle getClipBounds() { return null; }
        @Override public void clipRect(int x, int y, int w, int h) {}
        @Override public void setClip(int x, int y, int w, int h) {}
        @Override public Shape getClip() { return null; }
        @Override public void setClip(Shape s) {}
        @Override public void copyArea(int x, int y, int w, int h, int dx, int dy) {}
        @Override public void drawLine(int a, int b, int c, int d) {}
        @Override public void fillRect(int x, int y, int w, int h) {}
        @Override public void clearRect(int x, int y, int w, int h) {}
        @Override public void drawRoundRect(int x, int y, int w, int h, int a, int b) {}
        @Override public void fillRoundRect(int x, int y, int w, int h, int a, int b) {}
        @Override public void drawOval(int x, int y, int w, int h) {}
        @Override public void fillOval(int x, int y, int w, int h) {}
        @Override public void drawArc(int x, int y, int w, int h, int a, int b) {}
        @Override public void fillArc(int x, int y, int w, int h, int a, int b) {}
        @Override public void drawPolyline(int[] x, int[] y, int n) {}
        @Override public void drawPolygon(int[] x, int[] y, int n) {}
        @Override public void fillPolygon(int[] x, int[] y, int n) {}
        @Override public void drawString(String s, int x, int y) {}
        @Override public void drawString(AttributedCharacterIterator s, int x, int y) {}
        @Override public boolean drawImage(Image i, int x, int y, int w, int h, ImageObserver o) { return true; }
        @Override public boolean drawImage(Image i, int x, int y, Color c, ImageObserver o) { return true; }
        @Override public boolean drawImage(Image i, int x, int y, int w, int h, Color c, ImageObserver o) { return true; }
        @Override public boolean drawImage(Image i, int a, int b, int c, int d, int e, int f, int g, int h, ImageObserver o) { return true; }
        @Override public boolean drawImage(Image i, int a, int b, int c, int d, int e, int f, int g, int h, Color k, ImageObserver o) { return true; }
        @Override public void dispose() {}
    }

    private final DrawingPrimitivesTest state = new DrawingPrimitivesTest();
    private Game g;
    private Rec rec;
    private boolean savedAndroid;

    @Before public void before() {
        state.saveState();
        savedAndroid = ClientSettings.ANDROID;
        ClientSettings.ANDROID = false; // Sprite(byte[]) decodes through the desktop hook
        g = new Game();
        rec = new Rec();
        g.graphics = rec;
        g.resetImageProducers(); // titleStreamLoader == null: only creates the producers
    }

    @After public void after() {
        ClientSettings.ANDROID = savedAndroid;
        state.restoreState();
    }

    private RSImageProducer[] producers() {
        return new RSImageProducer[] {g.titleTopBannerProducer, g.titleBottomStripProducer, g.titleCenterBoxProducer,
            g.titleLeftFlamePaneProducer, g.titleRightFlamePaneProducer, g.titleBottomLeftProducer, g.titleBottomRightProducer,
            g.titleCenterLeftProducer, g.titleCenterRightProducer};
    }

    private static int intField(Object o, String name) throws Exception {
        Field f = o.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f.getInt(o);
    }

    private static Object imageOf(RSImageProducer p) throws Exception {
        Field f = RSImageProducer.class.getDeclaredField("image");
        f.setAccessible(true);
        return f.get(p);
    }

    /** Returns {x, y} of the recorded drawImage for producer p, or null if it was not drawn. */
    private int[] drawnAt(RSImageProducer p) throws Exception {
        Object img = imageOf(p);
        int[] at = null;
        for (Object[] c : rec.calls) {
            if (c[0] == img) {
                assertNull("drawn twice", at);
                at = new int[] {(Integer) c[1], (Integer) c[2]};
            }
        }
        return at;
    }

    private void attachFixtureArchive() throws Exception {
        Field f = TextWidthTest.class.getDeclaredField("ARCHIVE");
        f.setAccessible(true);
        g.titleStreamLoader = new StreamLoader(Base64.getDecoder().decode((String) f.get(null)));
        g.chatTextDrawingArea = TextWidthTest.font(false);
    }

    private static final int[][] LAYOUT = { // width, height, screenX, screenY in producers() order
        {509, 171, 128, 0}, {360, 132, 202, 371}, {360, 200, 202, 171}, {128, 265, 0, 0}, {128, 265, 637, 0},
        {202, 238, 0, 265}, {203, 238, 562, 265}, {74, 94, 128, 171}, {75, 94, 562, 171}};

    @Test public void producersHaveTheTitleScreenPaneSizes() throws Exception {
        RSImageProducer[] ps = producers();
        for (int i = 0; i < ps.length; i++) {
            assertEquals("width " + i, LAYOUT[i][0], intField(ps[i], "width"));
            assertEquals("height " + i, LAYOUT[i][1], intField(ps[i], "height"));
            assertEquals("pixels " + i, LAYOUT[i][0] * LAYOUT[i][1], ps[i].pixels.length);
        }
    }

    /** The panes tile the 765x503 title screen exactly once: every screen pixel is covered by exactly one pane. */
    @Test public void panesTileTheScreenWithoutOverlapOrGaps() {
        int[][] cover = new int[503][765];
        for (int[] l : LAYOUT) {
            for (int y = l[3]; y < l[3] + l[1]; y++) {
                for (int x = l[2]; x < l[2] + l[0]; x++) cover[y][x]++;
            }
        }
        int uncovered = 0, overlapped = 0;
        for (int y = 0; y < 503; y++) for (int x = 0; x < 765; x++) {
            if (cover[y][x] == 0) uncovered++;
            if (cover[y][x] > 1) overlapped++;
        }
        assertEquals(0, overlapped);
        assertEquals(0, uncovered);
    }

    /** drawLoadingText draws every pane at its screen position (x, y) when the welcome screen is raised. */
    @Test public void loadingScreenDrawsEveryPaneAtItsPosition() throws Exception {
        attachFixtureArchive();
        g.welcomeScreenRaised = true;
        g.flameThreadActive = false;
        g.drawLoadingText(50, "x");
        RSImageProducer[] ps = producers();
        for (int i = 0; i < ps.length; i++) {
            assertArrayEquals("pane " + i, new int[] {LAYOUT[i][2], LAYOUT[i][3]}, drawnAt(ps[i]));
        }
        assertEquals(9, rec.calls.size());
        assertFalse(g.welcomeScreenRaised);
    }

    /** Counterexample: without welcomeScreenRaised only the centre box (pane 1109) is drawn. */
    @Test public void withoutRaiseOnlyCentreBoxIsDrawn() throws Exception {
        attachFixtureArchive();
        g.welcomeScreenRaised = false;
        g.drawLoadingText(50, "x");
        assertEquals(1, rec.calls.size());
        assertArrayEquals(new int[] {202, 171}, drawnAt(g.titleCenterBoxProducer));
    }

    /** flameThreadActive true (flame thread owns the side panes): the two 128x265 side panes are not redrawn. */
    @Test public void flagTrueSkipsOnlyTheTwoSidePanes() throws Exception {
        attachFixtureArchive();
        g.welcomeScreenRaised = true;
        g.flameThreadActive = true;
        g.drawLoadingText(50, "x");
        assertNull(drawnAt(g.titleLeftFlamePaneProducer));
        assertNull(drawnAt(g.titleRightFlamePaneProducer));
        assertEquals(7, rec.calls.size());
        for (int i : new int[] {0, 1, 2, 5, 6, 7, 8}) {
            assertNotNull("pane " + i, drawnAt(producers()[i]));
        }
    }

    // ---- what is drawn into the panes: drawLogo ----

    private static int titleColor(int x, int y) { return ((x + 1) << 12 | (y + 1)) & 0xffffff; }

    private static byte[] titlePng() throws Exception {
        BufferedImage bi = new BufferedImage(TITLE_W, TITLE_H, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < TITLE_H; y++) for (int x = 0; x < TITLE_W; x++) bi.setRGB(x, y, titleColor(x, y));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(bi, "png", out);
        return out.toByteArray();
    }

    private static final int LOGO_W = 10, LOGO_H = 4, LOGO_RGB = 0x00ff00;

    /** Archive with title.dat (the PNG), logo.dat and index.dat (one 10x4 logo, every pixel palette entry 1 = green). */
    private static StreamLoader archive(byte[] title) throws Exception {
        byte[] logo = new byte[2 + LOGO_W * LOGO_H];
        for (int i = 2; i < logo.length; i++) logo[i] = 1;
        // index.dat: trimWidth, trimHeight, paletteSize, palette[1], offsetX, offsetY, width, height, storage order
        byte[] index = {0, 20, 0, 4, 2, 0, (byte) 0xff, 0, 0, 0, 0, LOGO_W, 0, LOGO_H, 0};
        String[] names = {"title.dat", "logo.dat", "index.dat"};
        byte[][] data = {title, logo, index};
        int n = names.length;
        int total = 8 + n * 10;
        for (byte[] d : data) total += d.length;
        byte[] a = new byte[total];
        int p = 6;
        a[p++] = 0;
        a[p++] = (byte) n;
        int pos = p + n * 10;
        for (int i = 0; i < n; i++) {
            int h = 0;
            for (char ch : names[i].toUpperCase().toCharArray()) h = h * 61 + ch - 32;
            a[p++] = (byte) (h >> 24); a[p++] = (byte) (h >> 16); a[p++] = (byte) (h >> 8); a[p++] = (byte) h;
            int len = data[i].length;
            for (int k = 0; k < 2; k++) { a[p++] = (byte) (len >> 16); a[p++] = (byte) (len >> 8); a[p++] = (byte) len; }
        }
        for (byte[] d : data) { System.arraycopy(d, 0, a, pos, d.length); pos += d.length; }
        // Build the "already decompressed" archive layout directly (the constructor would need a bzip2 stream).
        StreamLoader s = (StreamLoader) unsafe().allocateInstance(StreamLoader.class);
        setField(s, "archiveData", a);
        setField(s, "archiveDecompressed", true);
        Stream hdr = new Stream(a);
        hdr.currentOffset = 6;
        int cnt = hdr.readUnsignedShortBigEndian();
        int[] hashes = new int[cnt], un = new int[cnt], pk = new int[cnt], offs = new int[cnt];
        int eo = hdr.currentOffset + cnt * 10;
        for (int i = 0; i < cnt; i++) {
            hashes[i] = hdr.readIntBigEndian();
            un[i] = hdr.readUnsignedMediumBigEndian();
            pk[i] = hdr.readUnsignedMediumBigEndian();
            offs[i] = eo;
            eo += pk[i];
        }
        setField(s, "entryCount", cnt);
        setField(s, "entryNameHashes", hashes);
        setField(s, "entryUnpackedSizes", un);
        setField(s, "entryPackedSizes", pk);
        setField(s, "entryOffsets", offs);
        return s;
    }

    private static sun.misc.Unsafe unsafe() throws Exception {
        Field f = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        return (sun.misc.Unsafe) f.get(null);
    }

    private static void setField(Object o, String n, Object v) throws Exception {
        Field f = o.getClass().getDeclaredField(n);
        f.setAccessible(true);
        f.set(o, v);
    }

    /**
     * title.dat is a 382x503 picture. Each pane shows the part of the 765x503 screen it covers: screen columns
     * 0..381 are the picture, columns 382..764 are its horizontal mirror. Verified for every pixel of every pane,
     * except the logo rectangle on the top pane.
     */
    @Test public void drawLogoFillsEachPaneWithItsScreenRegionOfTheMirroredTitlePicture() throws Exception {
        g.titleStreamLoader = archive(titlePng());
        g.drawLogo();
        RSImageProducer[] ps = producers();
        int logoLeft = 382 - LOGO_W / 2;
        for (int i = 0; i < ps.length; i++) {
            int w = LAYOUT[i][0], h = LAYOUT[i][1], sx = LAYOUT[i][2], sy = LAYOUT[i][3];
            for (int y = 0; y < h; y++) for (int x = 0; x < w; x++) {
                int screenX = sx + x, screenY = sy + y;
                boolean inLogo = i == 0 && screenX >= logoLeft && screenX < logoLeft + LOGO_W && screenY >= 18 && screenY < 18 + LOGO_H;
                if (inLogo) continue;
                int col = screenX < TITLE_W ? screenX : TITLE_W - 1 - (screenX - TITLE_W);
                // screen column 764 lies past the mirrored picture (382 + 382 columns cover 0..763): left untouched
                int expected = col < 0 ? 0 : titleColor(col, screenY);
                assertEquals("pane " + i + " local " + x + "," + y, expected, ps[i].pixels[y * w + x] & 0xffffff);
            }
        }
    }

    /** The 10x4 logo is drawn over the top pane (1107) with its left edge at screen x 382 - width/2, top at y 18. */
    @Test public void logoIsDrawnCentredOnTheTopPane() throws Exception {
        g.titleStreamLoader = archive(titlePng());
        g.drawLogo();
        int[] p = g.titleTopBannerProducer.pixels;
        int lx = 382 - LOGO_W / 2 - 128, ly = 18;
        for (int y = ly - 1; y <= ly + LOGO_H; y++) for (int x = lx - 1; x <= lx + LOGO_W; x++) {
            boolean in = x >= lx && x < lx + LOGO_W && y >= ly && y < ly + LOGO_H;
            if (in) assertEquals(LOGO_RGB, p[y * 509 + x] & 0xffffff);
            else assertNotEquals("outside logo " + x + "," + y, LOGO_RGB, p[y * 509 + x] & 0xffffff);
        }
        for (RSImageProducer q : producers()) {
            if (q == g.titleTopBannerProducer) continue;
            for (int v : q.pixels) assertNotEquals(LOGO_RGB, v & 0xffffff);
        }
    }

    // ---- the two side panes are the flame panes ----

    private void flameFixture(int[] flame) {
        g.aClass30_Sub2_Sub1_Sub1_1201 = new Sprite(128, 265);
        g.aClass30_Sub2_Sub1_Sub1_1202 = new Sprite(128, 265);
        Arrays.fill(g.aClass30_Sub2_Sub1_Sub1_1201.pixels, 0x112233);
        Arrays.fill(g.aClass30_Sub2_Sub1_Sub1_1202.pixels, 0x445566);
        g.anIntArray850 = new int[256];
        g.anIntArray851 = new int[256];
        g.anIntArray852 = new int[256];
        g.anIntArray853 = new int[256];
        for (int i = 0; i < 256; i++) g.anIntArray851[i] = 0xabcdef;
        g.anIntArray828 = flame;
        g.anInt1040 = 0;
        g.anInt1041 = 0;
    }

    @Test public void flameDrawingFillsAndPresentsTheTwoSidePanes() throws Exception {
        flameFixture(new int[32768]);
        g.doFlamesDrawing();
        for (int v : g.titleLeftFlamePaneProducer.pixels) assertEquals(0x112233, v);
        for (int v : g.titleRightFlamePaneProducer.pixels) assertEquals(0x445566, v);
        assertArrayEquals(new int[] {0, 0}, drawnAt(g.titleLeftFlamePaneProducer));
        assertArrayEquals(new int[] {637, 0}, drawnAt(g.titleRightFlamePaneProducer));
        assertEquals(2, rec.calls.size());
        for (int i : new int[] {0, 1, 2, 5, 6, 7, 8}) assertNull(drawnAt(producers()[i]));
    }

    @Test public void flameIntensityChangesSidePanePixels() throws Exception {
        int[] flame = new int[32768];
        Arrays.fill(flame, 255);
        flameFixture(flame);
        g.doFlamesDrawing();
        int changedLeft = 0, changedRight = 0;
        for (int v : g.titleLeftFlamePaneProducer.pixels) if (v != 0x112233) changedLeft++;
        for (int v : g.titleRightFlamePaneProducer.pixels) if (v != 0x445566) changedRight++;
        assertTrue(changedLeft > 1000);
        assertTrue(changedRight > 1000);
        for (int v : g.titleTopBannerProducer.pixels) assertEquals(0, v);
    }

    // ---- flameThreadActive: flame-thread flag ----

    @Test public void flameLoopRunsOnlyWhileFlagIsSet() {
        g.anInt1208 = 0;
        g.flameThreadActive = false;
        g.drawFlames();
        assertEquals("flag false: loop body never entered", 0, g.anInt1208);
        assertFalse(g.drawingFlames);
        g.flameThreadActive = true; // flame arrays missing: first iteration throws and the loop exits
        g.drawFlames();
        assertEquals("flag true: loop body entered", 1, g.anInt1208);
        assertFalse(g.drawingFlames);
    }

    @Test public void nullLoaderClearsFlagAndWaitsForFlameThread() throws Exception {
        g.flameThreadActive = true;
        g.drawingFlames = true;
        Thread t = new Thread(g::nullLoader);
        t.start();
        long end = System.currentTimeMillis() + 3000;
        while (g.flameThreadActive && System.currentTimeMillis() < end) Thread.sleep(5);
        assertFalse("nullLoader clears the flag", g.flameThreadActive);
        Thread.sleep(150);
        assertTrue("nullLoader still waiting for the flame thread", t.isAlive());
        g.drawingFlames = false;
        t.join(3000);
        assertFalse(t.isAlive());
    }

    @Test public void flagStartsFalse() {
        assertFalse(new Game().flameThreadActive);
    }
}
