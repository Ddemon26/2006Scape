import java.awt.Component;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.lang.reflect.Field;
import java.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/** Controlled glyphs make text placement observable without copying the drawing algorithm. */
public class GameChatHistoryDrawingTest {
    private static final int BACK = 0x334455;
    private final DrawingPrimitivesTest state = new DrawingPrimitivesTest();
    private int[] savedOffsets;
    private Player savedPlayer;
    private Rec g;
    private static class Rec extends Game {
        int[] bar;
        @Override public void drawScrollbar(int a, int b, int c, int d, int e) {
            bar = new int[] {a, b, c, d, e};
        }
    }
    private static class Surface extends Component {
        @Override public Image createImage(java.awt.image.ImageProducer p) {
            return new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        }
        @Override public boolean prepareImage(Image i, ImageObserver o) { return false; }
    }
    private static void set(Object o, String n, Object v) throws Exception {
        Field f = (o instanceof Game ? Game.class : o.getClass()).getDeclaredField(n); f.setAccessible(true); f.set(o, v);
    }
    private static <T> T allocate(Class<T> c) throws Exception {
        Field f = sun.misc.Unsafe.class.getDeclaredField("theUnsafe"); f.setAccessible(true);
        return c.cast(((sun.misc.Unsafe) f.get(null)).allocateInstance(c));
    }
    private static TextDrawingArea font(int advance) throws Exception {
        TextDrawingArea f = allocate(TextDrawingArea.class);
        byte[][] masks = new byte[256][];
        for (int i = 0; i < masks.length; i++) masks[i] = new byte[] {1};
        int[] ones = new int[256]; Arrays.fill(ones, 1);
        int[] steps = new int[256]; Arrays.fill(steps, advance);
        set(f, "glyphMasks", masks); set(f, "glyphWidthPixels", ones); set(f, "glyphHeightPixels", ones);
        set(f, "glyphXOffsetPixels", new int[256]); set(f, "glyphYOffsetPixels", new int[256]);
        set(f, "glyphAdvancePixels", steps); set(f, "jitterRandom", new java.util.Random(0));
        f.maxAsciiGlyphHeightPixels = 1;
        return f;
    }
    private static Background solid(int w, int h, int color) throws Exception {
        Background b = allocate(Background.class);
        b.imageWidth = w; b.imageHeight = h; b.paletteIndices = new byte[w * h];
        Arrays.fill(b.paletteIndices, (byte) 1); set(b, "paletteRgb", new int[] {0, color});
        return b;
    }
    @Before public void before() throws Exception {
        state.saveState(); savedOffsets = Texture.lineOffsets; savedPlayer = Game.myPlayer;
        Game.myPlayer = new Player(); Game.myPlayer.name = "Self";
        g = new Rec(); Surface c = new Surface();
        g.chatPanelIP = new RSImageProducer(479, 96, c);
        g.worldViewIP = new RSImageProducer(512, 334, c);
        g.graphics = new BufferedImage(765, 503, BufferedImage.TYPE_INT_RGB).getGraphics();
        g.chatBack = solid(479, 96, BACK);
        g.p12TextDrawingArea = font(3); g.chatTextDrawingArea = font(7);
        set(g, "modIcons", new Background[] {solid(2, 2, 0xaa1111), solid(2, 2, 0x11aa11)});
        g.backDialogID = g.dialogID = -1;
        g.publicChatMode = g.privateChatMode = g.tradeMode = g.splitPrivateChat = 0;
    }
    @After public void after() {
        g.graphics.dispose(); Game.myPlayer = savedPlayer; Texture.lineOffsets = savedOffsets; state.restoreState();
    }
    private void put(int index, int type, String name, String text) {
        g.chatTypes[index] = type; g.chatNames[index] = name; g.chatMessages[index] = text;
    }
    private int pixel(int x, int y) { return g.chatPanelIP.pixels[y * 479 + x]; }
    private void drawn(int x, int y, int color) { assertEquals("pixel " + x + "," + y, color, pixel(x,y)); }
    @Test public void capturedTextIsCenteredAboveContinueAndBelowPromptPriority() {
        g.dialogID = 0;
        g.pushMessage("ABC", 0, "");
        g.drawChatArea();
        drawn(229,39,0); drawn(236,39,0); drawn(243,39,0);
        drawn(228,39,BACK); drawn(250,39,BACK);
        // Seventeen characters at seven-pixel advance, centered at x239.
        drawn(180,59,128); drawn(187,59,128);
        g.pushMessage("", 0, "");
        g.drawChatArea();
        drawn(229,39,BACK); drawn(180,59,128);
        g.pushMessage("ABC", 0, "");
        g.inputDialogState = 1;
        g.amountOrNameInput = "";
        g.drawChatArea();
        drawn(194,39,0); // Enter amount's first glyph lies outside ABC.
        g.inputDialogState = 2;
        g.drawChatArea();
        drawn(201,39,0); drawn(194,39,BACK);
        g.inputDialogState = 0;
        g.messagePromptRaised = true;
        g.aString1121 = "Z"; g.promptInput = "";
        g.drawChatArea();
        drawn(236,39,0); drawn(229,39,BACK);
        g.messagePromptRaised = false;
        g.drawChatArea(); drawn(229,39,0);
    }
    @Test public void constructorAndEmptyHistoryHaveMinimumAndDedicatedState() {
        assertEquals(0, g.chatScrollOffsetPixels); assertEquals(78, g.chatContentHeightPixels);
        assertNotNull(g.chatScrollbarState); assertNotSame(g.chatScrollbarState, new Game().chatScrollbarState);
        g.drawChatArea(); assertArrayEquals(new int[] {77,1,0,463,78}, g.bar);
        assertEquals(78, g.chatContentHeightPixels);
    }
    @Test public void sparseSlotsInclude99Exclude100AndCountAdmittedNotArrayIndex() throws Exception {
        set(g, "chatMessages", new String[101]); set(g, "chatNames", new String[101]); set(g, "chatTypes", new int[101]);
        put(2,0,null,"A"); put(99,0,null,"B"); put(100,0,null,"C");
        g.drawChatArea(); drawn(4,69,0); drawn(4,55,0); drawn(4,41,BACK);
        assertEquals(78,g.chatContentHeightPixels);
        for(int i=0;i<6;i++) put(i,0,null,"A");
        g.drawChatArea(); assertEquals(105,g.chatContentHeightPixels); // seven admitted slots, not 100 or 101
    }
    @Test public void scrollMovesHistoryDownInPixelsButLeavesInputFixedAndCountsOffscreenRows() {
        for(int i=0;i<10;i++) put(i,0,null,"A");
        g.drawChatArea(); assertEquals(147,g.chatContentHeightPixels); drawn(4,69,0); drawn(4,89,0);
        g.chatScrollOffsetPixels=5; g.drawChatArea(); drawn(4,74,0); drawn(4,69,BACK); drawn(4,89,0);
        assertArrayEquals(new int[]{77,65,0,463,147},g.bar);
        g.chatScrollOffsetPixels=70; g.drawChatArea(); assertArrayEquals(new int[]{77,0,0,463,147},g.bar);
    }
    @Test public void baselineZeroIsExcludedAndPositiveBaselineSurvives() {
        for(int i=0;i<6;i++) put(i,0,null,"A");
        g.drawChatArea(); drawn(4,0,BACK);
        g.chatScrollOffsetPixels=1; g.drawChatArea(); drawn(4,0,0);
        g.chatScrollOffsetPixels=7; g.drawChatArea(); drawn(4,76,BACK); // baseline 77 clipped by the 77-high viewport
    }
    @Test public void typeControlsTextLayoutAndColor() {
        int[] types={0,1,2,3,7,4,5,6,8};
        int[] colors={0,255,255,0x800000,0x800000,0x800080,0x800000,0x800000,0x7e3200};
        int[] xs={4,18,18,33,33,13,4,27,13};
        for(int i=0;i<types.length;i++) {
            put(0,types[i],"AB","Z"); g.drawChatArea(); drawn(xs[i],69,colors[i]);
        }
        put(0,9,"AB","Z"); g.drawChatArea(); drawn(4,69,BACK);
    }
    @Test public void filtersRemoveRowsRatherThanLeavingBlankGaps() {
        put(0,2,"Stranger","A"); put(1,3,"Stranger","B"); put(2,4,"Stranger","C");
        put(3,8,"Stranger","D"); put(4,5,null,"E"); put(5,6,"Stranger","F"); put(6,0,null,"G");
        g.publicChatMode=g.privateChatMode=g.tradeMode=2; g.drawChatArea();
        drawn(4,69,0); drawn(4,55,BACK); assertEquals(78,g.chatContentHeightPixels);
        put(0,1,"AB","Z"); put(1,7,"AB","Z"); g.drawChatArea();
        drawn(18,69,255); drawn(33,55,0x800000); drawn(4,41,0);
        g.splitPrivateChat=1; g.drawChatArea(); drawn(4,55,0); drawn(4,41,BACK);
    }
    @Test public void friendOnlyFiltersDistinguishStrangerFriendAndSelf() {
        g.publicChatMode=g.privateChatMode=g.tradeMode=1;
        g.friendsCount=1; g.friendsList[0]="Friend"; g.friendsListAsLongs[0]=TextClass.encodeBase37Name("Friend");
        for(int type:new int[]{2,3,4,8}) {
            put(0,type,"Stranger","Z"); g.drawChatArea(); drawn(4,69,BACK);
            put(0,type,"Friend","Z"); g.drawChatArea(); assertNotEquals(BACK,pixel(4,69));
            put(0,type,"Self","Z"); g.drawChatArea(); assertNotEquals(BACK,pixel(4,69));
        }
    }
    @Test public void publicAndPrivateCrownPrefixesAreStrippedAndMoveTextBy14Pixels() {
        for(int type:new int[]{2,3}) for(int crown=0;crown<=2;crown++) {
            put(0,type,crown==0?"AB":"@cr"+crown+"@AB","Z"); g.drawChatArea();
            int x=type==2?4:19;
            if(crown>0) { drawn(x,58,crown==1?0xaa1111:0x11aa11); x+=14; }
            drawn(x,69,0); drawn(x+3,69,0); drawn(x+14,69,type==2?255:0x800000);
            drawn(x+6,69,0); // the colon follows AB, not the raw five-character prefix
        }
    }
    @Test public void inputUsesPlayerNameOrFormattedLoginNameAndHistoryFontWidths() {
        g.inputString="Z"; g.drawChatArea(); drawn(4,89,0); drawn(24,89,255); drawn(10,89,0);
        Game.myPlayer.name=null; g.myUsername="ab_cd"; g.drawChatArea(); drawn(27,89,255);
        Game.myPlayer=null; g.drawChatArea(); drawn(27,89,255);
    }
}
