import static org.junit.Assert.*;

import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Base64;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** Semantics of Game.drawLoadingText (title-screen loading bar drawn into the 360x200 title box). */
public class GameLoadingBarTest {
    private static final int W = 360, H = 200, SENTINEL = 0x123456, RED = 0x8c1111, WHITE = 0xffffff;

    private static final class Fixture extends java.awt.Component {}

    private final DrawingPrimitivesTest state = new DrawingPrimitivesTest();
    private Game g;
    private BufferedImage screen;

    @Before public void before() throws Exception {
        state.saveState();
        g = new Game();
        Fixture c = new Fixture();
        g.aRSImageProducer_1107 = new RSImageProducer(1, 1, c); // resetImageProducers returns early
        g.aRSImageProducer_1109 = new RSImageProducer(W, H, c);
        Field archive = TextWidthTest.class.getDeclaredField("ARCHIVE");
        archive.setAccessible(true);
        g.titleStreamLoader = new StreamLoader(Base64.getDecoder().decode((String) archive.get(null)));
        g.chatTextDrawingArea = TextWidthTest.font(false);
        g.welcomeScreenRaised = false;
        screen = new BufferedImage(765, 503, BufferedImage.TYPE_INT_RGB);
        g.graphics = screen.getGraphics();
    }

    @After public void after() {
        g.graphics.dispose();
        state.restoreState();
    }

    private int[] draw(int i, String s) {
        Arrays.fill(g.aRSImageProducer_1109.pixels, SENTINEL);
        g.drawLoadingText(i, s);
        return g.aRSImageProducer_1109.pixels;
    }

    private static int px(int[] p, int x, int y) { return p[y * W + x]; }

    private static int redInRow(int[] p, int y) {
        int n = 0;
        for (int x = 30; x < 330; x++) if (px(p, x, y) == RED) n++;
        return n;
    }

    /** i is a percentage: the inner fill (x 30..329) is red for exactly 3*i px from the left, black after. */
    @Test public void firstParameterIsPercentOfThreeHundredPixelFill() {
        for (int i : new int[] {0, 1, 37, 99, 100}) {
            int[] p = draw(i, "");
            assertEquals("red px for i=" + i, 3 * i, redInRow(p, 79));
            if (i > 0) assertEquals(RED, px(p, 30 + 3 * i - 1, 79));
            if (i < 100) assertEquals(0, px(p, 30 + 3 * i, 79));
        }
    }

    /** j is the bar's top row: outer red outline rows 62..95, inner black outline 63..94, fill 64..93. */
    @Test public void barTopRowIsSixtyTwo() {
        int[] p = draw(50, "");
        assertEquals(SENTINEL, px(p, 100, 61));
        assertEquals(RED, px(p, 100, 62));
        assertEquals(0, px(p, 100, 63));
        assertEquals(RED, px(p, 100, 64));
        assertEquals(RED, px(p, 100, 93));
        assertEquals(0, px(p, 100, 94));
        assertEquals(RED, px(p, 100, 95));
        assertEquals(SENTINEL, px(p, 100, 96));
        // vertical anchor: barY = areaHeight/2 - 18 - lift = 100 - 18 - 20
        assertEquals(H / 2 - 18 - 20, 62);
    }

    /** c is the 360-px box width: bar outline spans x 28..331, symmetric about x=180 (= 360/2). */
    @Test public void barIsCentredOnBoxWidth() {
        int[] p = draw(100, "");
        assertEquals(SENTINEL, px(p, 27, 79));
        assertEquals(RED, px(p, 28, 79));
        assertEquals(RED, px(p, 331, 79));
        assertEquals(SENTINEL, px(p, 332, 79));
        assertEquals(W - 1, 28 + 331);
    }

    private static int[] whiteBounds(int[] p, int y0, int y1) {
        int minX = W, maxX = -1, minY = H, maxY = -1;
        for (int y = y0; y < y1; y++) for (int x = 0; x < W; x++) if (px(p, x, y) == WHITE) {
            minX = Math.min(minX, x); maxX = Math.max(maxX, x); minY = Math.min(minY, y); maxY = Math.max(maxY, y);
        }
        return new int[] {minX, maxX, minY, maxY};
    }

    /** s is the message: its glyphs are drawn centred at x=180 with baseline 85 (inside the bar); "" draws none. */
    @Test public void secondParameterIsMessageDrawnInsideBar() {
        int[] empty = draw(0, "");
        assertEquals(-1, whiteBounds(empty, 62, 96)[1]);
        int[] w = draw(0, "W");
        int[] b = whiteBounds(w, 62, 96);
        assertTrue("message drawn inside bar", b[1] >= 0);
        assertTrue("baseline at 85 (y<=85)", b[3] <= 85 && b[3] >= 75);
        int widthW = g.chatTextDrawingArea.measureLiteralTextWidth("W");
        assertTrue("left edge at 180 - width/2", b[0] >= 180 - widthW / 2 && b[0] <= 180);
        int[] ww = draw(0, "WWW");
        int[] b3 = whiteBounds(ww, 62, 96);
        assertTrue("wider message extends further left (centred)", b3[0] < b[0]);
        assertTrue("and further right", b3[1] > b[1]);
        // Rows above the bar (heading) are the same for both messages: message does not affect heading.
        for (int y = 0; y < 62; y++) for (int x = 0; x < W; x++) assertEquals(px(empty, x, y), px(ww, x, y));
    }

    /** byte1 lifts all three parts: heading baseline 100-26-20=54 lies above bar top 62, message 100+5-20=85. */
    @Test public void headingIsDrawnAboveBarAtLiftedBaseline() {
        int[] p = draw(0, "");
        int[] b = whiteBounds(p, 0, 62);
        assertTrue("heading drawn", b[1] >= 0);
        assertTrue("heading bottom at baseline 54", b[3] <= 54 && b[3] >= 44);
    }

    /** anInt1079 / aString1049 hold the latest call's percent and message. */
    @Test public void fieldsRememberLatestPercentAndMessage() {
        draw(37, "first");
        assertEquals(37, g.lastLoadingPercent);
        assertEquals("first", g.lastLoadingMessage);
        draw(80, "second");
        assertEquals(80, g.lastLoadingPercent);
        assertEquals("second", g.lastLoadingMessage);
    }

    /** The 360x200 box is blitted to the screen at (202,171). */
    @Test public void boxIsCopiedToScreenAtTitleBoxOrigin() {
        draw(50, "");
        assertEquals(RED, screen.getRGB(202 + 30, 171 + 79) & 0xffffff);
        assertEquals(0, screen.getRGB(202 + 30 + 150, 171 + 79) & 0xffffff);
    }
}
