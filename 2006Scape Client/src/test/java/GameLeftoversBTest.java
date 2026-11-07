import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** Original-name observations: drawInterface origin params k/l, applyVarpSetting varp settings, designerModelDirty, scaleHslLightness. */
public class GameLeftoversBTest {
    private static final int W = 200, H = 150, BG = 0x345678;
    private Game g;
    private int[] savedPixels, state;
    private RSInterface[] savedCache;
    private Varp[] savedVarp;
    private IDK[] savedIdk;
    private int savedIdkLength;
    private Object savedHeaders;

    private static sun.misc.Unsafe unsafe() throws Exception {
        Field f = sun.misc.Unsafe.class.getDeclaredField("theUnsafe"); f.setAccessible(true);
        return (sun.misc.Unsafe) f.get(null);
    }
    @Before public void before() throws Exception {
        g = new Game();
        savedPixels = DrawingArea.pixels;
        state = new int[]{DrawingArea.width, DrawingArea.height, DrawingArea.topX, DrawingArea.topY, DrawingArea.bottomX,
            DrawingArea.bottomY, DrawingArea.scanlineRightLimit, DrawingArea.clipRightHalf, DrawingArea.clipBottomHalf};
        DrawingArea.bindRaster(H, W, new int[W * H]);
        Arrays.fill(DrawingArea.pixels, BG);
        savedCache = RSInterface.interfaceCache;
        RSInterface.interfaceCache = new RSInterface[20];
        savedVarp = Varp.cache;
        savedIdk = IDK.cache; savedIdkLength = IDK.length;
        Field h = Model.class.getDeclaredField("modelHeaders"); h.setAccessible(true); savedHeaders = h.get(null); h.set(null, null);
    }
    @After public void after() throws Exception {
        DrawingArea.pixels = savedPixels; DrawingArea.width = state[0]; DrawingArea.height = state[1]; DrawingArea.topX = state[2];
        DrawingArea.topY = state[3]; DrawingArea.bottomX = state[4]; DrawingArea.bottomY = state[5];
        DrawingArea.scanlineRightLimit = state[6]; DrawingArea.clipRightHalf = state[7]; DrawingArea.clipBottomHalf = state[8];
        RSInterface.interfaceCache = savedCache;
        Varp.cache = savedVarp;
        IDK.cache = savedIdk; IDK.length = savedIdkLength;
        Field h = Model.class.getDeclaredField("modelHeaders"); h.setAccessible(true); h.set(null, savedHeaders);
    }

    // ---------- drawInterface k / l ----------
    private RSInterface scene(int rootW, int rootH, int childX, int childY, int cw, int ch) {
        RSInterface root = new RSInterface();
        root.id = 1; root.type = 0; root.width = rootW; root.height = rootH;
        root.children = new int[]{2}; root.childX = new int[]{childX}; root.childY = new int[]{childY};
        RSInterface c = new RSInterface(); c.id = 2; c.type = 3; c.width = cw; c.height = ch; c.filled = true; c.textColor = 0x2a251e;
        RSInterface.interfaceCache[1] = root; RSInterface.interfaceCache[2] = c;
        return root;
    }
    private static boolean red(int x, int y) { return (DrawingArea.pixels[y * W + x] & 0xffffff) == 0x2a251e; }

    @Test public void kIsHorizontalOriginAndLIsVerticalOrigin() {
        g.drawInterface(0, 50, scene(100, 80, 7, 11, 10, 6), 30);   // k=50, l=30
        assertTrue(red(50 + 7, 30 + 11));
        assertTrue(red(50 + 7 + 9, 30 + 11 + 5));
        assertFalse(red(30 + 7, 50 + 11));          // swapped meaning would paint here
        assertFalse(red(50 + 6, 30 + 11));
        assertFalse(red(50 + 7, 30 + 10));
        assertFalse(red(50 + 7 + 10, 30 + 11));
    }
    @Test public void kAndLAlsoOriginOfClipRectangleOfWidthAndHeight() {
        // child extends beyond the 20x12 container; only the part inside [k,k+20) x [l,l+12) shows
        g.drawInterface(0, 60, scene(20, 12, 5, 4, 40, 40), 40);
        assertTrue(red(60 + 19, 40 + 11));
        assertFalse(red(60 + 20, 40 + 11));
        assertFalse(red(60 + 19, 40 + 12));
        assertTrue(red(60 + 5, 40 + 4));
        assertFalse(red(60 + 4, 40 + 4));
    }
    @Test public void scrollPosShiftsChildrenUpButNotOrigin() {
        g.drawInterface(3, 50, scene(100, 80, 7, 11, 10, 6), 30);
        assertTrue(red(50 + 7, 30 + 11 - 3));
        assertFalse(red(50 + 7, 30 + 11 + 3));
    }

    @Test public void drawInterfaceRestoresCallerClipAndDrawsEveryChild() {
        DrawingArea.setClip(120, 15, 170, 25);
        int[] before = {DrawingArea.topX, DrawingArea.topY, DrawingArea.bottomX, DrawingArea.bottomY};
        RSInterface root = scene(100, 80, 7, 11, 10, 6);
        RSInterface second = new RSInterface(); second.id = 3; second.type = 3; second.width = 4; second.height = 4; second.filled = true;
        RSInterface.interfaceCache[3] = second;
        root.children = new int[]{2, 3}; root.childX = new int[]{7, 40}; root.childY = new int[]{11, 30};
        g.drawInterface(0, 50, root, 30);
        assertArrayEquals(before, new int[]{DrawingArea.topX, DrawingArea.topY, DrawingArea.bottomX, DrawingArea.bottomY});
        DrawingArea.setClip(H, 0, W, 0);
        assertTrue(red(50 + 40, 30 + 30));          // second child drawn
    }

    // ---------- applyVarpSetting ----------
    private void setVarp(int idx, int action, int value) throws Exception {
        Varp v = (Varp) unsafe().allocateInstance(Varp.class);
        v.settingAction = action;
        Varp.cache = new Varp[idx + 1]; Varp.cache[idx] = v;
        g.variousSettings[idx] = value;
    }
    @Test public void method33DispatchesByVarpSettingActionOfIndexI() throws Exception {
        g.leftClickOpensMenu = 9; g.chatEffectsMode = 9; g.insertDragMode = 9;
        setVarp(7, 5, 1); g.applyVarpSetting(7);
        assertEquals(1, g.leftClickOpensMenu); assertEquals(9, g.chatEffectsMode); assertEquals(9, g.insertDragMode);
        setVarp(3, 6, 2); g.applyVarpSetting(3);
        assertEquals(2, g.chatEffectsMode); assertEquals(1, g.leftClickOpensMenu); assertEquals(9, g.insertDragMode);
        setVarp(5, 9, 4); g.applyVarpSetting(5);
        assertEquals(4, g.insertDragMode); assertEquals(2, g.chatEffectsMode); assertEquals(1, g.leftClickOpensMenu);
        setVarp(2, 0, 3); g.applyVarpSetting(2);   // action 0 is a no-op
        assertEquals(4, g.insertDragMode);
        setVarp(2, 8, 1); g.applyVarpSetting(2);   // another action leaves the three untouched
        assertEquals(1, g.leftClickOpensMenu); assertEquals(2, g.chatEffectsMode); assertEquals(4, g.insertDragMode);
        setVarp(2, 9, 0); g.applyVarpSetting(2);   // value is variousSettings[i]
        assertEquals(0, g.insertDragMode);
    }

    // leftClickOpensMenu consumer: processMenuClick
    static class MenuSpy extends Game {
        List<String> calls = new ArrayList<String>();
        @Override public void doAction(int i) { calls.add("doAction" + i); }
        @Override public void determineMenuSize() { calls.add("menu"); }
    }
    private List<String> click(int value, int rows, int actionId) {
        MenuSpy s = new MenuSpy();
        s.leftClickOpensMenu = value; s.menuActionRow = rows;
        s.menuActionID[rows - 1] = actionId;
        s.clickMode3 = 1; s.activeInterfaceType = 0;
        s.processMenuClick();
        return s.calls;
    }
    @Test public void anInt1253MakesLeftClickOpenMenuWhenMoreThanTwoRows() {
        assertEquals(Arrays.asList("menu"), click(1, 3, 100));
        assertEquals(Arrays.asList("doAction2"), click(0, 3, 100));
        assertEquals(Arrays.asList("doAction1"), click(1, 2, 100));   // boundary: needs >2 rows
        assertEquals(Arrays.asList("doAction2"), click(2, 3, 100));   // only the value 1
        assertEquals(Arrays.asList("menu"), click(0, 3, 337));         // add-friend row forces the menu regardless
    }

    // insertDragMode consumer: mainGameProcessor drag-and-drop of inventory items
    static class DragSpy extends Game {
        int hoverSlot;
        @Override public boolean parsePacket() { return false; }
        @Override public void loadingStages() { }
        @Override public void advancePendingSceneChanges() { }
        @Override public void advancePlayerEntities() { }
        @Override public void advanceNpcEntities() { }
        @Override public void decrementOverheadTextTimers() { }
        @Override public void dropClient() { }
        @Override public void processRightClick() { lastActiveInvInterface = anInt1084; mouseInvInterfaceIndex = hoverSlot; }
    }
    private int[] drag(int mode, int contentType, int[] ids, int from, int to) throws Exception {
        DragSpy s = new DragSpy();
        s.mouseDetection = new MouseDetection(s);
        s.stream = new Stream(new byte[5000]); s.stream.encryption = new ISAACRandomGen(new int[]{1, 2, 3, 4});
        s.insertDragMode = mode; s.loggedIn = true;
        RSInterface inv = new RSInterface(); inv.id = 4; inv.contentType = contentType;
        inv.itemIdsPlusOne = ids.clone(); inv.itemQuantities = new int[ids.length];
        RSInterface.interfaceCache[4] = inv;
        s.anInt1084 = 4; s.anInt1085 = from; s.hoverSlot = to;
        s.activeInterfaceType = 1; s.aBoolean1242 = true; s.anInt989 = 10; s.clickMode2 = 0;
        try { s.mainGameProcessor(); } catch (RuntimeException e) { /* code after the drag needs a live session */ }
        return inv.itemIdsPlusOne;
    }
    @Test public void anInt913InsertsInsteadOfSwappingOnlyForContentType206() throws Exception {
        assertArrayEquals(new int[]{22, 33, 11, 44}, drag(1, 206, new int[]{11, 22, 33, 44}, 0, 2));   // insert
        assertArrayEquals(new int[]{33, 22, 11, 44}, drag(0, 206, new int[]{11, 22, 33, 44}, 0, 2));   // swap
        assertArrayEquals(new int[]{33, 22, 11, 44}, drag(1, 207, new int[]{11, 22, 33, 44}, 0, 2));   // other content
        assertArrayEquals(new int[]{33, 22, 11, 44}, drag(2, 206, new int[]{11, 22, 33, 44}, 0, 2));   // only value 1
        assertArrayEquals(new int[]{11, 22, 33, 44}, drag(1, 206, new int[]{11, 22, 33, 44}, 0, 0));   // same slot untouched
        assertArrayEquals(new int[]{0, 22, 33, 11}, drag(1, 206, new int[]{11, 22, 33, 0}, 0, 3));     // empty target: plain swap
    }

    // chatEffectsMode consumer: updateEntities overhead-text metrics
    static class EntitySpy extends Game {
        @Override public void projectEntityToScreen(Entity e, int h) { spriteDrawX = 100; spriteDrawY = 100; }
    }
    private int[] overhead(int mode, int effect) throws Exception {
        EntitySpy s = new EntitySpy();
        s.groundArray = new NodeList[4][104][104];
        s.chatTextDrawingArea = TextWidthTest.font(false);
        s.aTextDrawingArea_1270 = s.chatTextDrawingArea;
        s.chatEffectsMode = mode; s.publicChatMode = 0;
        Player p = new Player(); s.myPlayer = p;
        p.textSpoken = "AB"; p.overheadChatColorCode = 0; p.overheadChatEffectCode = effect; p.textCycle = 100; p.headIcon = -1; p.visible = true;
        try { s.updateEntities(); } catch (RuntimeException e) { }
        return new int[]{s.anIntArray977[1], s.anIntArray978[1], s.anIntArray979[1]};
    }
    @Test public void anInt1249ZeroEnablesChatEffectMetricsNonzeroDisablesThem() throws Exception {
        int[] base = overhead(0, 0);
        int[] wave = overhead(0, 1);
        assertEquals(base[0] + 5, wave[0]); assertEquals(base[1] + 10, wave[1]);
        int[] scroll = overhead(0, 4);
        assertEquals(60, scroll[2]);
        int[] glow = overhead(0, 5);
        assertEquals(base[1] + 5, glow[1]);
        for (int m = 1; m <= 2; m++) {
            assertArrayEquals(base, overhead(m, 1));
            assertArrayEquals(base, overhead(m, 4));
            assertArrayEquals(base, overhead(m, 5));
        }
    }

    // ---------- designerModelDirty ----------
    private IDK kit(int[] modelIds) throws Exception {
        IDK k = (IDK) unsafe().allocateInstance(IDK.class); Field f = IDK.class.getDeclaredField("bodyModelIds"); f.setAccessible(true); f.set(k, modelIds); return k;
    }
    private RSInterface designerModel() { RSInterface c = new RSInterface(); c.contentType = 327; return c; }
    @Test public void aBoolean1031IsSetBySelectionAndHeldWhileKitModelsAreLoading() throws Exception {
        IDK.cache = new IDK[]{kit(new int[]{5})}; IDK.length = 1;
        assertFalse(g.designerModelDirty);
        g.selectDefaultDesignerKits();
        assertTrue(g.designerModelDirty);
        g.designerKitIds[0] = 0;
        g.drawFriendsListOrWelcomeScreen(designerModel());        // model 5 not loaded: stay dirty
        assertTrue(g.designerModelDirty);
        IDK.cache = new IDK[]{kit(null)};
        g.designerKitIds[0] = 0;
        try { g.drawFriendsListOrWelcomeScreen(designerModel()); } catch (RuntimeException e) { }
        assertFalse(g.designerModelDirty);                              // loaded: rebuild consumed the flag
        g.designerModelDirty = false;
        g.designerKitIds[0] = 0;
        IDK.cache = new IDK[]{kit(new int[]{5})};
        g.drawFriendsListOrWelcomeScreen(designerModel());        // flag clear: nothing happens
        assertFalse(g.designerModelDirty);
    }

    // ---------- scaleHslLightness ----------
    private static int m77(int i, int h) throws Exception {
        Method m = Game.class.getDeclaredMethod("scaleHslLightness", int.class, int.class); m.setAccessible(true);
        return (Integer) m.invoke(null, i, h);
    }
    @Test public void method77ScalesHslLightnessKeepingHueAndSaturation() throws Exception {
        int hs = 0xAB80;                      // hue/saturation bits, lightness 0
        assertEquals(12345678, m77(5, -2));
        assertEquals(127 - 40, m77(40, -1));
        assertEquals(127, m77(-5, -1)); assertEquals(0, m77(500, -1));
        assertEquals(hs + 32, m77(64, hs + 64));           // 64 * 64/128
        assertEquals(hs + 2, m77(1, hs + 64));             // lower clamp
        assertEquals(hs + 126, m77(127, hs + 127));        // 127*127/128 = 126
        assertEquals(hs + 126, m77(300, hs + 127));        // upper clamp
        assertEquals(hs + 10, m77(20, hs + 64));           // proportional to the hsl lightness
    }
}
