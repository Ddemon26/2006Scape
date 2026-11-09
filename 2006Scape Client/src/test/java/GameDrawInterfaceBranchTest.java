import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Random;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** Original-name observations of Game.drawInterface per-type branch locals (types 2, 3, 4, 6, 7). */
public class GameDrawInterfaceBranchTest {
    private static final int W = 300, H = 200, BG = 0x345678;
    private Game g;
    private int[] savedPixels, state;
    private RSInterface[] savedCache;
    private Animation[] savedAnims;
    private Object savedItemCache;
    private int savedT1, savedT2;

    private static sun.misc.Unsafe unsafe() throws Exception {
        Field f = sun.misc.Unsafe.class.getDeclaredField("theUnsafe"); f.setAccessible(true);
        return (sun.misc.Unsafe) f.get(null);
    }
    private static Field field(Class<?> c, String name) throws Exception {
        for (Class<?> k = c; k != null; k = k.getSuperclass()) {
            try { Field f = k.getDeclaredField(name); f.setAccessible(true); return f; } catch (NoSuchFieldException e) { }
        }
        throw new NoSuchFieldException(name);
    }
    private static void set(Object o, String name, Object v) throws Exception { field(o.getClass(), name).set(o, v); }
    private static Object sget(Class<?> c, String name) throws Exception { return field(c, name).get(null); }

    @Before public void before() throws Exception {
        g = new Game();
        savedPixels = DrawingArea.pixels;
        state = new int[]{DrawingArea.width, DrawingArea.height, DrawingArea.topX, DrawingArea.topY, DrawingArea.bottomX,
            DrawingArea.bottomY, DrawingArea.scanlineRightLimit, DrawingArea.clipRightHalf, DrawingArea.clipBottomHalf};
        DrawingArea.bindRaster(H, W, new int[W * H]);
        Arrays.fill(DrawingArea.pixels, BG);
        savedCache = RSInterface.interfaceCache;
        RSInterface.interfaceCache = new RSInterface[20];
        savedAnims = Animation.anims;
        savedItemCache = sget(ItemDef.class, "cache");
        savedT1 = Texture.textureInt1; savedT2 = Texture.textureInt2;
    }
    @After public void after() throws Exception {
        DrawingArea.pixels = savedPixels; DrawingArea.width = state[0]; DrawingArea.height = state[1]; DrawingArea.topX = state[2];
        DrawingArea.topY = state[3]; DrawingArea.bottomX = state[4]; DrawingArea.bottomY = state[5];
        DrawingArea.scanlineRightLimit = state[6]; DrawingArea.clipRightHalf = state[7]; DrawingArea.clipBottomHalf = state[8];
        RSInterface.interfaceCache = savedCache;
        Animation.anims = savedAnims;
        field(ItemDef.class, "cache").set(null, savedItemCache);
        Texture.textureInt1 = savedT1; Texture.textureInt2 = savedT2;
        ItemDef.mruNodes1.clear();
        ((LruCache) sget(RSInterface.class, "modelCache")).clear();
    }

    // ---------- fixtures ----------
    /** Root container (id 1) at the origin holding one child (id 2) at (cx, cy). */
    private RSInterface root(RSInterface child, int cx, int cy) {
        RSInterface root = new RSInterface();
        root.id = 1; root.type = 0; root.width = W; root.height = H;
        root.children = new int[]{2}; root.childX = new int[]{cx}; root.childY = new int[]{cy};
        child.id = 2;
        RSInterface.interfaceCache[1] = root; RSInterface.interfaceCache[2] = child;
        return root;
    }
    /** One-pixel glyphs, one pixel advance, drawn on the baseline row; line height h. */
    private static TextDrawingArea font(int h) throws Exception {
        TextDrawingArea t = (TextDrawingArea) unsafe().allocateInstance(TextDrawingArea.class);
        byte[][] masks = new byte[256][];
        int[] one = new int[256], adv = new int[256];
        for (int c = 0; c < 256; c++) { masks[c] = new byte[]{1}; one[c] = 1; adv[c] = 1; }
        set(t, "glyphMasks", masks); set(t, "glyphWidthPixels", one); set(t, "glyphHeightPixels", one.clone());
        set(t, "glyphXOffsetPixels", new int[256]); set(t, "glyphYOffsetPixels", new int[256]);
        set(t, "glyphAdvancePixels", adv); set(t, "jitterRandom", new Random(0)); set(t, "strikeThroughEnabled", false);
        t.maxAsciiGlyphHeightPixels = h;
        return t;
    }
    private static Sprite sprite(int w, int h, int color) {
        Sprite s = new Sprite(w, h);
        Arrays.fill(s.pixels, color);
        s.trimHeight = -1;   // cached item sprite valid for any amount
        return s;
    }
    private static int px(int x, int y) { return DrawingArea.pixels[y * W + x] & 0xffffff; }
    /** {leftmost x, count} of pixels of colour c on row y, or {-1, 0}. */
    private static int[] run(int c, int y) {
        int min = -1, n = 0;
        for (int x = 0; x < W; x++) if (px(x, y) == c) { if (min < 0) min = x; n++; }
        return new int[]{min, n};
    }
    /** {min x, min y} of every pixel that is not background, colour 0 or 0xffff00 (text). */
    private static int[] spriteOrigin() {
        int mx = -1, my = -1;
        for (int y = 0; y < H; y++) for (int x = 0; x < W; x++) {
            int p = px(x, y);
            if (p != BG && p != 0 && p != 0xffff00) { if (my < 0) my = y; if (mx < 0 || x < mx) mx = x; }
        }
        return new int[]{mx, my};
    }

    // ---------- type 2: i3, l3, l4, k5, j6, class30_sub2_sub1_sub1_1 ----------
    private RSInterface inventory(int cols, int rows, int padX, int padY) {
        RSInterface c = new RSInterface();
        c.type = 2; c.width = cols; c.height = rows; c.invSpritePadX = padX; c.invSpritePadY = padY;
        c.spritesX = new int[20]; c.spritesY = new int[20];
        c.itemIdsPlusOne = new int[cols * rows]; c.itemQuantities = new int[cols * rows];
        return c;
    }
    @Test public void type2EmptySlotSpriteIndexedRowMajorAtColumnAndRowPitch() {
        RSInterface inv = inventory(3, 2, 4, 6);
        inv.sprites = new Sprite[20];
        for (int k = 0; k < 6; k++) inv.sprites[k] = sprite(1, 1, 0x100000 + k);
        inv.spritesX[4] = 1; inv.spritesY[4] = 2;
        g.drawInterface(0, 0, root(inv, 10, 20), 0);
        for (int k = 0; k < 6; k++) {
            int col = k % 3, row = k / 3;
            int x = 10 + col * (32 + 4) + inv.spritesX[k], y = 20 + row * (32 + 6) + inv.spritesY[k];
            assertEquals("slot " + k, 0x100000 + k, px(x, y));
        }
        assertEquals(BG, px(10 + 36, 20 + 38));          // slot 4 moved by spritesX/Y[4]
        assertEquals(BG, px(10, 20 + 36));                // column/row swap would put slot 1 here
    }
    @Test public void type2SlotsFromTwentyIgnoreSpriteOffsetsAndEmptySprites() {
        RSInterface inv = inventory(5, 5, 0, 0);
        inv.sprites = new Sprite[25];
        for (int k = 0; k < 25; k++) inv.sprites[k] = sprite(1, 1, 0x200000 + k);
        g.drawInterface(0, 0, root(inv, 0, 0), 0);
        assertEquals(0x200000 + 19, px(4 * 32, 3 * 32));
        for (int k = 20; k < 25; k++) assertEquals(BG, px((k % 5) * 32, 4 * 32));
    }

    // ---------- type 2: j9, class30_sub2_sub1_sub1_2, k10 ----------
    @Test public void type2ItemSpriteLookedUpByIdMinusOneAndQuantityTextFromSlot() throws Exception {
        RSInterface inv = inventory(3, 1, 0, 0);
        ItemDef.mruNodes1.removeFromCache(sprite(2, 2, 0x00aa00), 9001);
        ItemDef.mruNodes1.removeFromCache(sprite(2, 2, 0x0000aa), 9002);
        inv.itemIdsPlusOne = new int[]{9002, 0, 9003};
        inv.itemQuantities = new int[]{1, 0, 150000};
        g.aTextDrawingArea_1270 = font(1);
        g.drawInterface(0, 0, root(inv, 10, 20), 0);
        assertEquals(0x00aa00, px(10, 20));
        assertEquals(BG, px(42, 20));
        assertEquals(0x0000aa, px(74, 20));
        assertArrayEquals(new int[]{74, 4}, run(0xffff00, 20 + 9 - 1));   // "150K" from slot 2 quantity
        assertArrayEquals(new int[]{75, 4}, run(0, 20 + 10 - 1));     // shadow one pixel right/down
        // quantity 7 -> one glyph; quantity 1 with a normal sprite -> no text
        Arrays.fill(DrawingArea.pixels, BG);
        inv.itemQuantities = new int[]{1, 0, 7};
        g.drawInterface(0, 0, root(inv, 10, 20), 0);
        assertArrayEquals(new int[]{74, 1}, run(0xffff00, 28));
    }

    // ---------- type 2: l9 ----------
    private boolean selectionThrows(int itemSelected, int slot, int interfaceId) throws Exception {
        RSInterface inv = inventory(2, 1, 0, 0);
        ItemDef.mruNodes1.removeFromCache(sprite(2, 2, 0x00aa00), 9001);
        ItemDef.mruNodes1.removeFromCache(sprite(2, 2, 0x0000aa), 9002);
        inv.itemIdsPlusOne = new int[]{9002, 9003}; inv.itemQuantities = new int[]{1, 1};
        field(ItemDef.class, "cache").set(null, null);
        g.itemSelected = itemSelected; g.anInt1283 = slot; g.anInt1284 = interfaceId;
        Arrays.fill(DrawingArea.pixels, BG);
        try { g.drawInterface(0, 0, root(inv, 0, 0), 0); } catch (RuntimeException e) { return true; }
        assertEquals(0x00aa00, px(0, 0)); assertEquals(0x0000aa, px(32, 0));   // cached (outline 0) sprites drawn
        return false;
    }
    @Test public void type2NonzeroOutlineOnlyForTheSelectedItemSlot() throws Exception {
        assertTrue(selectionThrows(1, 1, 2));    // outline != 0 bypasses the sprite cache (forID on unloaded defs)
        assertTrue(selectionThrows(1, 0, 2));
        assertFalse(selectionThrows(0, 1, 2));
        assertFalse(selectionThrows(1, 2, 2));
        assertFalse(selectionThrows(1, 1, 3));
    }

    // ---------- type 2: k6, j7 ----------
    private int[] dragged(int dx, int dy, int holdCycles) throws Exception {
        RSInterface inv = inventory(2, 1, 0, 0);
        ItemDef.mruNodes1.removeFromCache(sprite(2, 2, 0x00aa00), 9001);
        inv.itemIdsPlusOne = new int[]{0, 9002}; inv.itemQuantities = new int[]{0, 1};
        g.activeInterfaceType = 1; g.anInt1085 = 1; g.anInt1084 = 2; g.anInt989 = holdCycles;
        g.anInt1087 = 100; g.anInt1088 = 100; g.mouseX = 100 + dx; g.mouseY = 100 + dy;
        Arrays.fill(DrawingArea.pixels, BG);
        g.drawInterface(0, 0, root(inv, 60, 80), 0);
        int[] o = spriteOrigin();
        return new int[]{o[0] - (60 + 32), o[1] - 80};
    }
    @Test public void type2DraggedItemOffsetIsMouseMinusPressWithDeadZoneAndHoldDelay() throws Exception {
        assertArrayEquals(new int[]{20, 7}, dragged(20, 7, 10));
        assertArrayEquals(new int[]{-12, 0}, dragged(-12, 4, 10));
        assertArrayEquals(new int[]{0, -9}, dragged(4, -9, 10));
        assertArrayEquals(new int[]{5, -5}, dragged(5, -5, 10));
        assertArrayEquals(new int[]{0, 0}, dragged(-4, 3, 10));
        assertArrayEquals(new int[]{0, 0}, dragged(20, 7, 4));    // held < 5 cycles: no offset
        assertArrayEquals(new int[]{20, 7}, dragged(20, 7, 5));
    }

    // ---------- type 2: i10, j10 ----------
    /** Container clip y in [30,130), scrollMax 400; dragged slot y before offset = 40. Returns {scroll, anInt1088}. */
    private int[] autoScroll(int scroll, int cycles, int dy) throws Exception {
        RSInterface inv = inventory(1, 1, 0, 0);
        ItemDef.mruNodes1.removeFromCache(sprite(2, 2, 0x00aa00), 9001);
        inv.itemIdsPlusOne = new int[]{9002}; inv.itemQuantities = new int[]{1};
        RSInterface root = root(inv, 0, 10 + scroll);
        root.height = 100; root.scrollMax = 400; root.scrollPosition = scroll;
        g.activeInterfaceType = 1; g.anInt1085 = 0; g.anInt1084 = 2; g.anInt989 = 10; g.cyclesSinceDraw = cycles;
        g.anInt1087 = 100; g.anInt1088 = 100; g.mouseX = 100; g.mouseY = 100 + dy;
        g.drawInterface(scroll, 0, root, 30);
        return new int[]{root.scrollPosition, g.anInt1088};
    }
    @Test public void type2DragAboveClipScrollsUpByDistanceCappedByCyclesAndScroll() throws Exception {
        assertArrayEquals(new int[]{37, 113}, autoScroll(50, 2, -30));   // 2*(30-10)/3 = 13
        assertArrayEquals(new int[]{44, 106}, autoScroll(50, 1, -30));   // 1*20/3 = 6
        assertArrayEquals(new int[]{30, 120}, autoScroll(50, 2, -80));   // 2*70/3 = 46 -> cycles*10 = 20
        assertArrayEquals(new int[]{0, 105}, autoScroll(5, 2, -30));     // capped by scrollPosition 5
        assertArrayEquals(new int[]{0, 100}, autoScroll(0, 2, -30));     // already at top
    }
    @Test public void type2DragBelowClipScrollsDownByDistanceCappedByCyclesAndRemaining() throws Exception {
        assertArrayEquals(new int[]{64, 86}, autoScroll(50, 2, 80));     // 2*(120+32-130)/3 = 14
        assertArrayEquals(new int[]{70, 80}, autoScroll(50, 2, 150));    // 61 -> cycles*10 = 20
        assertArrayEquals(new int[]{300, 95}, autoScroll(295, 2, 80));   // capped by scrollMax-height-scroll = 5
        assertArrayEquals(new int[]{300, 100}, autoScroll(300, 2, 80));  // already at bottom
        assertArrayEquals(new int[]{50, 100}, autoScroll(50, 2, 50));    // 90+32 < 130: no scroll
    }

    // ---------- type 3: flag ----------
    private int rectColor(boolean selected, boolean hoverColorSet, int h1026, int h1039, int h1048) {
        RSInterface c = new RSInterface();
        c.type = 3; c.width = 5; c.height = 5; c.textColor = 0x111111; c.disabledHoverColor = hoverColorSet ? 0x222222 : 0;
        c.enabledColor = 0x333333; c.enabledHoverColor = hoverColorSet ? 0x444444 : 0;
        c.conditionOperators = selected ? new int[0] : null;
        g.anInt1026 = h1026; g.anInt1039 = h1039; g.anInt1048 = h1048;
        Arrays.fill(DrawingArea.pixels, BG);
        g.drawInterface(0, 0, root(c, 10, 10), 0);
        return px(10, 10);
    }
    @Test public void type3HoverFlagSetByAnyOfThreeHoverIdsSelectsHoverColour() {
        assertEquals(0x111111, rectColor(false, true, 0, 0, 0));
        assertEquals(0x222222, rectColor(false, true, 2, 0, 0));
        assertEquals(0x222222, rectColor(false, true, 0, 2, 0));
        assertEquals(0x222222, rectColor(false, true, 0, 0, 2));
        assertEquals(0x111111, rectColor(false, true, 3, 3, 3));
        assertEquals(0x111111, rectColor(false, false, 2, 0, 0));   // zero hover colour keeps the normal one
        assertEquals(0x333333, rectColor(true, true, 0, 0, 0));
        assertEquals(0x444444, rectColor(true, true, 0, 2, 0));
    }

    // ---------- type 4: s, flag1, i4, l6, k7..k8, l8, s1 ----------
    private RSInterface text(String disabled, String enabled, boolean selected) throws Exception {
        RSInterface c = new RSInterface();
        c.type = 4; c.width = 100; c.height = 40; c.textDrawingAreas = font(7);
        c.disabledText = disabled; c.enabledText = enabled; c.textColor = 0x111111; c.disabledHoverColor = 0x222222;
        c.enabledColor = 0x333333; c.enabledHoverColor = 0x444444; c.conditionOperators = selected ? new int[0] : null;
        return c;
    }
    private int[] drawText(RSInterface c, int color, int lineY) {
        Arrays.fill(DrawingArea.pixels, BG);
        g.drawInterface(0, 0, root(c, 10, 20), 0);
        return run(color, lineY);
    }
    @Test public void type4TextAndColourChosenBySelectionHoverAndPendingContinue() throws Exception {
        assertArrayEquals(new int[]{10, 2}, drawText(text("ab", "xyz", false), 0x111111, 20));
        assertArrayEquals(new int[]{10, 3}, drawText(text("ab", "xyz", true), 0x333333, 20));
        assertArrayEquals(new int[]{10, 2}, drawText(text("ab", "", true), 0x333333, 20));    // empty enabled text keeps disabled
        g.anInt1048 = 2;
        assertArrayEquals(new int[]{10, 2}, drawText(text("ab", "xyz", false), 0x222222, 20));
        assertArrayEquals(new int[]{10, 3}, drawText(text("ab", "xyz", true), 0x444444, 20));
        g.anInt1048 = 0;
        RSInterface c = text("ab", "xyz", true); c.atActionType = 6; g.dialogContinuePending = true;
        assertArrayEquals(new int[]{10, 13}, drawText(c, 0x111111, 20));                    // "Please wait..." minus space
    }
    @Test public void type4LinesSplitAtBackslashNAndAdvanceByGlyphHeight() throws Exception {
        RSInterface c = text("ab\\ncde\\nf", "", false);
        Arrays.fill(DrawingArea.pixels, BG);
        g.drawInterface(0, 0, root(c, 10, 20), 0);
        assertArrayEquals(new int[]{10, 2}, run(0x111111, 20));
        assertArrayEquals(new int[]{10, 3}, run(0x111111, 27));
        assertArrayEquals(new int[]{10, 1}, run(0x111111, 34));
        assertArrayEquals(new int[]{-1, 0}, run(0x111111, 41));
        // a real newline character does not split
        assertArrayEquals(new int[]{10, 3}, drawText(text("a\nb", "", false), 0x111111, 20));
        assertArrayEquals(new int[]{-1, 0}, run(0x111111, 27));
    }
    @Test public void type4PercentNReplacedInPlaceByValueOfScriptNMinusOne() throws Exception {
        g.currentStats[0] = 1; g.currentStats[1] = 22; g.currentStats[2] = 333; g.currentStats[3] = 4444; g.currentStats[4] = 55555;
        int[][] scripts = {{1, 0, 0}, {1, 1, 0}, {1, 2, 0}, {1, 3, 0}, {1, 4, 0}};
        String[] in = {"%1", "%2", "%3", "%4", "%5", "%1%1", "a%4b", "%6", "%2\\n%3"};
        int[] len = {1, 2, 3, 4, 5, 2, 6, 2, 2};
        for (int i = 0; i < in.length; i++) {
            RSInterface c = text(in[i], "", false); c.valueIndexArray = scripts;
            assertArrayEquals(in[i], new int[]{10, len[i]}, drawText(c, 0x111111, 20));
        }
        assertArrayEquals(new int[]{10, 3}, run(0x111111, 27));   // second line's %3 replaced too
        RSInterface c = text("%2", "", false); c.valueIndexArray = new int[][]{{1, 0, 0}};
        assertArrayEquals(new int[]{10, 2}, drawText(c, 0x111111, 20));   // missing script -> "-2"
    }

    // ---------- type 6: k3, j4, i5, l5, flag2, i7 ----------
    private static Model model(int... xyz) throws Exception {
        Model m = (Model) unsafe().allocateInstance(Model.class);
        int n = xyz.length / 3;
        int[] x = new int[n], y = new int[n], z = new int[n];
        for (int i = 0; i < n; i++) { x[i] = xyz[3 * i]; y[i] = xyz[3 * i + 1]; z[i] = xyz[3 * i + 2]; }
        m.vertexCount = n; m.vertexX = x; set(m, "vertexY", y); set(m, "vertexZ", z);
        return m;
    }
    private static int screen(String axis, int i) throws Exception { return ((int[]) sget(Model.class, axis))[i]; }
    private RSInterface modelComponent(int pitch, int zoom) throws Exception {
        LruCache mc = (LruCache) sget(RSInterface.class, "modelCache");
        mc.removeFromCache(model(0, 0, 0, 64, 0, 0), (1 << 16) + 50);
        mc.removeFromCache(model(64, 0, 0), (1 << 16) + 51);
        RSInterface c = new RSInterface();
        c.type = 6; c.width = 40; c.height = 30; c.modelPitch = pitch; c.modelZoom = zoom;
        c.mediaType = 1; c.mediaID = 50; set(c, "enabledMediaType", 1); set(c, "enabledMediaID", 51);
        c.disabledAnimationId = -1; c.enabledAnimationId = -1;
        return c;
    }
    @Test public void type6ProjectsAroundComponentCentreAndRestoresTextureCentre() throws Exception {
        Texture.textureInt1 = 777; Texture.textureInt2 = 555;
        g.drawInterface(0, 0, root(modelComponent(0, 512), 10, 20), 0);
        assertEquals(10 + 20, screen("screenX", 0));
        assertEquals(20 + 15, screen("screenY", 0));
        assertEquals(777, Texture.textureInt1);
        assertEquals(555, Texture.textureInt2);
    }
    @Test public void type6ModelOffsetsPlaceOriginZoomUnitsAlongPitchedViewAxis() throws Exception {
        int[] pitches = {0, 100, 300, 400};
        int[] zooms = {512, 1024};
        for (int p : pitches) for (int zoom : zooms) {
            g.drawInterface(0, 0, root(modelComponent(p, zoom), 10, 20), 0);
            String m = "pitch " + p + " zoom " + zoom;
            assertTrue(m, Math.abs(screen("screenY", 0) - 35) <= 1);           // origin on the view axis
            assertEquals(m, 30, screen("screenX", 0));
            assertTrue(m, Math.abs(screen("screenX", 1) - 30 - 32768 / zoom) <= 1);   // depth == zoom
        }
    }
    @Test public void type6SelectionPicksEnabledModelAndEnabledAnimation() throws Exception {
        g.drawInterface(0, 0, root(modelComponent(0, 512), 10, 20), 0);
        assertEquals(30, screen("screenX", 0));                         // disabled model: vertex 0 at origin
        RSInterface c = modelComponent(0, 512); c.conditionOperators = new int[0];
        g.drawInterface(0, 0, root(c, 10, 20), 0);
        assertEquals(30 + 64, screen("screenX", 0));                    // enabled model: vertex 0 at x=64
        Animation a = (Animation) unsafe().allocateInstance(Animation.class);
        a.frameIds = new int[]{-1}; a.secondFrameIds = new int[]{-1};
        Animation.anims = new Animation[10]; Animation.anims[5] = a;
        c = modelComponent(0, 512); c.disabledAnimationId = 5; c.enabledAnimationId = 6;
        g.drawInterface(0, 0, root(c, 10, 20), 0);                      // unselected: anims[5] used
        c = modelComponent(0, 512); c.disabledAnimationId = 5; c.enabledAnimationId = 6; c.conditionOperators = new int[0];
        try { g.drawInterface(0, 0, root(c, 10, 20), 0); fail(); } catch (NullPointerException expected) { }   // anims[6] missing
        c.disabledAnimationId = 6; c.enabledAnimationId = 5;
        g.drawInterface(0, 0, root(c, 10, 20), 0);
        assertEquals(30 + 64, screen("screenX", 0));
    }

    // ---------- type 7: k4, j5, i6, s2, i9, k9 ----------
    private static ItemDef item(int id, String name, boolean stackable) throws Exception {
        ItemDef d = (ItemDef) unsafe().allocateInstance(ItemDef.class);
        set(d, "id", id); d.name = name; d.stackable = stackable;
        return d;
    }
    @Test public void type7LabelsAtColumnAndRowPitchWithQuantitySuffix() throws Exception {
        ItemDef[] cache = new ItemDef[10];
        for (int i = 0; i < 10; i++) cache[i] = item(-1, "", false);
        cache[0] = item(900, "ab", false); cache[1] = item(901, "cd", false); cache[2] = item(902, "ef", true);
        field(ItemDef.class, "cache").set(null, cache);
        RSInterface c = new RSInterface();
        c.type = 7; c.width = 2; c.height = 2; c.invSpritePadX = 3; c.invSpritePadY = 2; c.textDrawingAreas = font(1);
        c.textColor = 0x111111;
        c.itemIdsPlusOne = new int[]{901, 902, 0, 903}; c.itemQuantities = new int[]{1, 3, 0, 1};
        g.drawInterface(0, 0, root(c, 10, 20), 0);
        int col1 = 10 + 115 + 3, row1 = 20 + 12 + 2;
        assertEquals(10, run(0x111111, 19)[0]);
        assertEquals(2 + 4, run(0x111111, 19)[1]);                         // "ab" + "cdx3" (space draws nothing)
        assertEquals(0x111111, px(col1, 19)); assertEquals(BG, px(col1 - 1, 19));
        assertArrayEquals(new int[]{col1, 4}, run(0x111111, row1 - 1));         // stackable "ef x1"; slot 2 empty
    }
}
