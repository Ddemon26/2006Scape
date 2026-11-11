import static org.junit.Assert.*;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Which of the three client-wide font fields the routines read. Each routine is run with exactly one font field set and
 * the others null: a read of any other field throws NullPointerException, so a pass shows the routine reads only that one.
 * The font that is set is the font whose glyph output is compared with a direct draw.
 */
public class GameFontFieldRolesTest {
    private static final class Fixture extends java.awt.Component {}

    private final DrawingPrimitivesTest state = new DrawingPrimitivesTest();
    private Game g;

    @Before public void before() throws Exception {
        state.saveState();
        g = new Game();
        g.worldViewIP = new RSImageProducer(512, 334, new Fixture());
        BufferedImage screen = new BufferedImage(765, 503, BufferedImage.TYPE_INT_RGB);
        g.graphics = screen.getGraphics();
    }

    @After public void after() {
        g.graphics.dispose();
        state.restoreState();
    }

    private int[] drawTextOnScreen(String upper, String lower) {
        Arrays.fill(g.worldViewIP.pixels, 0x123456);
        g.drawTextOnScreen(upper, lower);
        return g.worldViewIP.pixels.clone();
    }

    private static int[] reference(TextDrawingArea font, String upper, String lower) {
        int[] raster = new int[512 * 334];
        Arrays.fill(raster, 0x123456);
        DrawingArea.bindRaster(334, 512, raster);
        int baseline = upper != null ? 144 : 151;
        font.drawCenteredLiteralText(0, lower, baseline, 257);
        font.drawCenteredLiteralText(0xffffff, lower, baseline - 1, 256);
        if (upper != null) {
            font.drawCenteredLiteralText(0, upper, baseline + 15, 257);
            font.drawCenteredLiteralText(0xffffff, upper, baseline + 14, 256);
        }
        return raster;
    }

    @Test public void drawTextOnScreenReadsOnlyTheSecondFontField() {
        TextDrawingArea font = TextWidthTest.font(false);
        g.p12TextDrawingArea = font;
        int[] drawn = drawTextOnScreen("Please wait", "Connection lost");
        assertArrayEquals(reference(font, "Please wait", "Connection lost"), drawn);
        assertFalse(Arrays.equals(reference(font, null, ""), drawn));
        g.p12TextDrawingArea = font;
        int[] single = drawTextOnScreen(null, "Connection lost");
        assertArrayEquals(reference(font, null, "Connection lost"), single);
    }

    @Test(expected = NullPointerException.class)
    public void drawTextOnScreenFailsWhenOnlyTheOtherFontFieldsAreSet() {
        TextDrawingArea font = TextWidthTest.font(false);
        g.p11TextDrawingArea = font;
        g.chatTextDrawingArea = font;
        drawTextOnScreen("Please wait", "Connection lost");
    }
}
