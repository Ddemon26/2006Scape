import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** Controlled original-name observations of RSInterface fields, unpack, method208/method209 and discardInterface. */
public class InterfaceFieldRolesTest {
    private static final int W = 160, H = 100, BG = 0x345678;
    private Game g;
    private int[] savedPixels, state;
    private RSInterface[] savedCache;
    private int savedC1, savedC2;
    private Object[] savedModelStatics = new Object[3];
    private Object savedHeaders;
    private static final String[] MODEL_STATICS = {"screenX", "screenY", "vertexDepth"};

    @Before public void before() throws Exception {
        g = new Game();
        savedPixels = DrawingArea.pixels;
        state = new int[]{DrawingArea.width, DrawingArea.height, DrawingArea.topX, DrawingArea.topY, DrawingArea.bottomX,
            DrawingArea.bottomY, DrawingArea.scanlineRightLimit, DrawingArea.clipRightHalf, DrawingArea.clipBottomHalf};
        DrawingArea.bindRaster(H, W, new int[W * H]);
        Arrays.fill(DrawingArea.pixels, BG);
        savedCache = RSInterface.interfaceCache;
        RSInterface.interfaceCache = new RSInterface[20];
        savedC1 = Texture.textureInt1; savedC2 = Texture.textureInt2;
        for (int n = 0; n < 3; n++) { Field f = Model.class.getDeclaredField(MODEL_STATICS[n]); f.setAccessible(true); savedModelStatics[n] = f.get(null); f.set(null, new int[64]); }
        Field h = Model.class.getDeclaredField("modelHeaders"); h.setAccessible(true); savedHeaders = h.get(null); h.set(null, null);
    }
    @After public void after() throws Exception {
        DrawingArea.pixels = savedPixels; DrawingArea.width = state[0]; DrawingArea.height = state[1]; DrawingArea.topX = state[2];
        DrawingArea.topY = state[3]; DrawingArea.bottomX = state[4]; DrawingArea.bottomY = state[5];
        DrawingArea.scanlineRightLimit = state[6]; DrawingArea.clipRightHalf = state[7]; DrawingArea.clipBottomHalf = state[8];
        RSInterface.interfaceCache = savedCache;
        Texture.textureInt1 = savedC1; Texture.textureInt2 = savedC2;
        for (int n = 0; n < 3; n++) { Field f = Model.class.getDeclaredField(MODEL_STATICS[n]); f.setAccessible(true); f.set(null, savedModelStatics[n]); }
        Field h = Model.class.getDeclaredField("modelHeaders"); h.setAccessible(true); h.set(null, savedHeaders);
        RSInterface.replaceCachedModel(null, 0, 0);
    }

    // ---------- scene helpers ----------
    private RSInterface scene(RSInterface child) {
        RSInterface root = new RSInterface();
        root.id = 1; root.type = 0; root.width = W; root.height = H;
        root.children = new int[]{2}; root.childX = new int[]{10}; root.childY = new int[]{20};
        child.id = 2;
        RSInterface.interfaceCache[1] = root; RSInterface.interfaceCache[2] = child;
        return root;
    }
    private static void select(RSInterface c, boolean on) {
        c.conditionOperators = new int[]{1}; c.conditionValues = new int[]{-7};
        c.valueIndexArray = new int[][]{{20, on ? -7 : -6, 0}};
    }
    private static int px(int x, int y) { return DrawingArea.pixels[y * W + x]; }
    private void draw(RSInterface root) { g.drawInterface(0, 0, root, 0); }
    private static RSInterface rect(int w, int h) { RSInterface c = new RSInterface(); c.type = 3; c.width = w; c.height = h; return c; }

    // ---------- colours: type 3 outline top-left pixel ----------
    private int outlineColor(boolean selected, int hoverId, int textColor, int a219, int a216, int a239) {
        Arrays.fill(DrawingArea.pixels, BG);
        RSInterface c = rect(12, 8); c.textColor = textColor; c.enabledColor = a219; c.disabledHoverColor = a216; c.enabledHoverColor = a239;
        select(c, selected);
        g.anInt1026 = hoverId == 1026 ? 2 : 0; g.anInt1039 = hoverId == 1039 ? 2 : 0; g.anInt1048 = hoverId == 1048 ? 2 : 0;
        draw(scene(c));
        return px(10, 20);
    }
    @Test public void colourFieldsAreSelectedAndHoverVariants() {
        for (int hover : new int[]{1026, 1039, 1048}) {
            assertEquals(0x111111, outlineColor(false, 0, 0x111111, 0x222222, 0x333333, 0x444444));
            assertEquals(0x333333, outlineColor(false, hover, 0x111111, 0x222222, 0x333333, 0x444444));  // anInt216 = unselected hover
            assertEquals(0x222222, outlineColor(true, 0, 0x111111, 0x222222, 0x333333, 0x444444));      // anInt219 = selected
            assertEquals(0x444444, outlineColor(true, hover, 0x111111, 0x222222, 0x333333, 0x444444));  // anInt239 = selected hover
        }
        // zero hover colour means "no hover colour"
        assertEquals(0x111111, outlineColor(false, 1039, 0x111111, 0x222222, 0, 0x444444));
        assertEquals(0x222222, outlineColor(true, 1039, 0x111111, 0x222222, 0x333333, 0));
        // a different id is not hovered
        Arrays.fill(DrawingArea.pixels, BG);
        RSInterface c = rect(12, 8); c.textColor = 0x111111; c.disabledHoverColor = 0x333333; select(c, false); g.anInt1026 = 0; g.anInt1048 = 0; g.anInt1039 = 3;
        draw(scene(c)); assertEquals(0x111111, px(10, 20));
    }

    // ---------- fill and transparency ----------
    @Test public void filledAndTransparency() {
        RSInterface c = rect(12, 8); c.textColor = 0xff0000; c.filled = false; draw(scene(c));
        assertEquals(0xff0000, px(10, 20)); assertEquals(BG, px(15, 24));                 // outline only
        Arrays.fill(DrawingArea.pixels, BG);
        c = rect(12, 8); c.textColor = 0xff0000; c.filled = true; draw(scene(c));
        assertEquals(0x2a251e, px(15, 24)); assertEquals(0x2a251e, px(10, 20)); assertEquals(BG, px(9, 20)); // opaque fill ignores colour
        for (int a : new int[]{1, 100, 200, 255, 128}) {
            for (boolean filled : new boolean[]{true, false}) {
                Arrays.fill(DrawingArea.pixels, BG);
                c = rect(12, 8); c.textColor = 0xff8040; c.filled = filled; c.transparency = (byte) a; draw(scene(c));
                int[] actual = DrawingArea.pixels.clone();
                Arrays.fill(DrawingArea.pixels, BG);
                int alpha = 256 - a;   // a is an unsigned 0..255 transparency, 0 = opaque
                if (filled) DrawingArea.fillBlendedRectangle(0xff8040, 20, 12, 8, alpha, 10);
                else DrawingArea.drawBlendedRectangleOutline(20, 8, alpha, 0xff8040, 12, 10);
                assertArrayEquals("a=" + a + " filled=" + filled, DrawingArea.pixels.clone(), actual);
            }
        }
        // larger transparency value = fainter
        Arrays.fill(DrawingArea.pixels, 0);
        c = rect(12, 8); c.textColor = 0xff0000; c.filled = true; c.transparency = (byte) 200; draw(scene(c)); int faint = px(12, 22) >> 16 & 0xff;
        Arrays.fill(DrawingArea.pixels, 0);
        c = rect(12, 8); c.textColor = 0xff0000; c.filled = true; c.transparency = (byte) 50; draw(scene(c)); int strong = px(12, 22) >> 16 & 0xff;
        assertTrue(faint < strong);
    }

    // ---------- hidden ----------
    @Test public void hiddenParentIsNotDrawnUnlessAHoverIdMatches() {
        g.anInt1026 = 0; g.anInt1039 = 0; g.anInt1048 = 0;
        RSInterface c = rect(12, 8); c.textColor = 0xff0000; RSInterface root = scene(c);
        root.hidden = true; draw(root); assertEquals(BG, px(10, 20));
        g.anInt1026 = 1; draw(root); assertEquals(0xff0000, px(10, 20)); g.anInt1026 = 0; Arrays.fill(DrawingArea.pixels, BG);
        g.anInt1048 = 1; draw(root); assertEquals(0xff0000, px(10, 20)); g.anInt1048 = 0; Arrays.fill(DrawingArea.pixels, BG);
        g.anInt1039 = 1; draw(root); assertEquals(0xff0000, px(10, 20)); g.anInt1039 = 0; Arrays.fill(DrawingArea.pixels, BG);
        root.hidden = false; draw(root); assertEquals(0xff0000, px(10, 20));
        // the flag is read on the container only: a flagged child still draws
        Arrays.fill(DrawingArea.pixels, BG); c.hidden = true; draw(root); assertEquals(0xff0000, px(10, 20));
    }
    @Test public void hiddenParentContributesNoMenuRows() {
        RSInterface c = new RSInterface(); c.type = 5; c.width = 12; c.height = 8; c.atActionType = 1; c.tooltip = "Go";
        RSInterface root = scene(c);
        g.menuActionRow = 0; g.buildInterfaceMenu(0, root, 15, 0, 24, 0); assertEquals(1, g.menuActionRow);
        g.menuActionRow = 0; root.hidden = true; g.buildInterfaceMenu(0, root, 15, 0, 24, 0); assertEquals(0, g.menuActionRow);
    }

    // ---------- offsets ----------
    @Test public void positionOffsetsMoveDrawingAndHitTest() {
        RSInterface c = rect(12, 8); c.textColor = 0xff0000; c.offsetX = 3; c.offsetY = 4; draw(scene(c));
        assertEquals(0xff0000, px(13, 24)); assertEquals(BG, px(10, 20)); assertEquals(BG, px(12, 23));
        Arrays.fill(DrawingArea.pixels, BG); c.offsetX = -2; c.offsetY = -5; draw(scene(c));
        assertEquals(0xff0000, px(8, 15)); assertEquals(BG, px(10, 20));
        RSInterface m = new RSInterface(); m.type = 5; m.width = 12; m.height = 8; m.atActionType = 1; m.tooltip = "Go"; m.offsetX = 3; m.offsetY = 4;
        RSInterface root = scene(m);
        g.menuActionRow = 0; g.buildInterfaceMenu(0, root, 11, 0, 21, 0); assertEquals(0, g.menuActionRow);   // old position no longer hits
        g.menuActionRow = 0; g.buildInterfaceMenu(0, root, 14, 0, 25, 0); assertEquals(1, g.menuActionRow);   // shifted position hits
    }

    // ---------- hover redirect id ----------
    @Test public void hoverRedirectIdReplacesTheHoveredId() {
        RSInterface c = new RSInterface(); c.type = 5; c.width = 12; c.height = 8; c.hoverRedirectId = 9; RSInterface root = scene(c);
        g.anInt886 = 0; g.buildInterfaceMenu(0, root, 15, 0, 24, 0); assertEquals(9, g.anInt886);          // redirect id wins over own id
        c.hoverRedirectId = 0; g.anInt886 = 55; g.buildInterfaceMenu(0, root, 15, 0, 24, 0); assertEquals(0, g.anInt886);   // 0 is a real redirect target
        c.hoverRedirectId = -1; g.anInt886 = 55; g.buildInterfaceMenu(0, root, 15, 0, 24, 0); assertEquals(55, g.anInt886); // -1 and no hover colour: untouched
        c.disabledHoverColor = 0x1234; g.anInt886 = 0; g.buildInterfaceMenu(0, root, 15, 0, 24, 0); assertEquals(2, g.anInt886); // hover colour alone tracks own id
        c.hoverRedirectId = 9; g.anInt886 = 0; g.buildInterfaceMenu(0, root, 40, 0, 24, 0); assertEquals(0, g.anInt886);     // outside
    }

    // ---------- content type ----------
    @Test public void contentTypeTriggersTheSpecialContentRoutines() {
        final List<Integer> drawn = new ArrayList<Integer>(), menu = new ArrayList<Integer>();
        Game spy = new Game() {
            @Override public void drawFriendsListOrWelcomeScreen(RSInterface c) { drawn.add(c.id); }
            @Override public boolean buildFriendsListMenu(RSInterface c) { menu.add(c.id); return true; }
        };
        for (int v : new int[]{0, 1, 5, 600, -3}) {
            drawn.clear(); menu.clear();
            RSInterface c = new RSInterface(); c.type = 5; c.width = 12; c.height = 8; c.atActionType = 1; c.tooltip = "T"; c.contentType = v;
            RSInterface root = scene(c);
            spy.drawInterface(0, 0, root, 0);
            spy.menuActionRow = 0; spy.buildInterfaceMenu(0, root, 15, 0, 24, 0);
            assertEquals("draw " + v, v > 0 ? 1 : 0, drawn.size());
            assertEquals("menu " + v, v != 0 ? 1 : 0, menu.size());
            assertEquals(v != 0 ? 0 : 1, spy.menuActionRow);   // buildFriendsListMenu(true) suppresses the tooltip row
        }
    }

    // ---------- text flags ----------
    private int[] drawText(boolean centred, boolean shadow) {
        Arrays.fill(DrawingArea.pixels, BG);
        TextDrawingArea font = TextWidthTest.font(false);
        RSInterface c = new RSInterface(); c.type = 4; c.width = 60; c.height = 12; c.textDrawingAreas = font;
        c.disabledText = "ABA"; c.enabledText = ""; c.textColor = 0xffffff; c.centeredText = centred; c.shadowedText = shadow;
        draw(scene(c));
        return DrawingArea.pixels.clone();
    }
    private int[] oracle(boolean centred, boolean shadow) {
        Arrays.fill(DrawingArea.pixels, BG);
        TextDrawingArea font = TextWidthTest.font(false);
        int baseline = 20 + font.maxAsciiGlyphHeightPixels;
        if (centred) font.drawCenteredTaggedText(0xffffff, 10 + 30, "ABA", baseline, shadow);
        else font.drawTaggedText(shadow, 10, 0xffffff, "ABA", baseline);
        return DrawingArea.pixels.clone();
    }
    @Test public void textFlagsSelectCentringAndShadow() {
        int[][] all = new int[4][];
        int n = 0;
        for (boolean centred : new boolean[]{false, true}) for (boolean shadow : new boolean[]{false, true}) {
            int[] a = drawText(centred, shadow);
            assertArrayEquals("c=" + centred + " s=" + shadow, oracle(centred, shadow), a);
            all[n++] = a;
        }
        for (int i = 0; i < 4; i++) for (int j = i + 1; j < 4; j++) assertFalse(i + "," + j, Arrays.equals(all[i], all[j]));
    }

    // ---------- models ----------
    private static Model vertexModel(int vx, int vy, int vz) throws Exception {
        Constructor<Model> k = Model.class.getDeclaredConstructor(); k.setAccessible(true);
        Model m = k.newInstance(); m.vertexCount = 1; m.vertexX = new int[]{vx}; m.vertexY = new int[]{vy}; m.vertexZ = new int[]{vz};
        m.faceCount = 0; m.faceVertexA = new int[0]; m.faceVertexB = new int[0]; m.faceVertexC = new int[0]; m.minX = 12345;
        return m;
    }
    private static int[] stat(String n) throws Exception { Field x = Model.class.getDeclaredField(n); x.setAccessible(true); return (int[]) x.get(null); }

    @Test public void mediaPairsKeyTheCacheByTypeThenId() throws Exception {
        Model a = vertexModel(1, 2, 3), b = vertexModel(4, 5, 6);
        RSInterface c = new RSInterface();
        c.mediaType = 1; c.mediaID = 77; priv(c, "enabledMediaType", 5); priv(c, "enabledMediaID", 0);
        RSInterface.replaceCachedModel(a, 77, 1);
        assertSame(a, c.getMediaModel(-1, -1, false));
        assertNull(c.getMediaModel(-1, -1, true));                      // enabled pair is type 5 (no model)
        RSInterface d = new RSInterface();
        d.mediaType = 5; d.mediaID = 0; priv(d, "enabledMediaType", 1); priv(d, "enabledMediaID", 88);
        RSInterface.replaceCachedModel(b, 88, 1);
        assertSame(b, d.getMediaModel(-1, -1, true));
        assertNull(d.getMediaModel(-1, -1, false));
        assertNull(c.getMediaModel(-1, -1, false));                      // method208 cleared the earlier entry
        // swapped halves of the key must not match: key is (type << 16) + id
        RSInterface e = new RSInterface(); e.mediaType = 88; e.mediaID = 1; priv(e, "enabledMediaType", 5); priv(e, "enabledMediaID", 0);
        assertNull(e.getMediaModel(-1, -1, false));
        // null model and type 4 store nothing but still clear
        RSInterface.replaceCachedModel(a, 77, 1);
        RSInterface.replaceCachedModel(null, 99, 1);
        assertNull(c.getMediaModel(-1, -1, false));
        RSInterface.replaceCachedModel(a, 77, 1);
        RSInterface.replaceCachedModel(b, 88, 4);
        assertNull(c.getMediaModel(-1, -1, false));
        assertNull(d.getMediaModel(-1, -1, true));
    }

    private static void priv(RSInterface c, String n, int v) { try { Field f = RSInterface.class.getDeclaredField(n); f.setAccessible(true); f.setInt(c, v); } catch (Exception e) { throw new AssertionError(e); } }
    private static int priv(RSInterface c, String n) { try { Field f = RSInterface.class.getDeclaredField(n); f.setAccessible(true); return f.getInt(c); } catch (Exception e) { throw new AssertionError(e); } }
    private void drawModel(RSInterface c) { c.type = 6; c.width = 50; c.height = 40; draw(scene(c)); }
    private static void cachedFor(RSInterface c, boolean enabled, int vx, int vy, int vz) throws Exception {
        Model m = vertexModel(vx, vy, vz);
        if (enabled) RSInterface.replaceCachedModel(m, priv(c, "enabledMediaID"), priv(c, "enabledMediaType")); else RSInterface.replaceCachedModel(m, c.mediaID, c.mediaType);
    }
    @Test public void zoomPitchYawAreProjectionDistanceAndAngles() throws Exception {
        // centre of the 50x40 model area at (10,20): (35,40)
        RSInterface c = new RSInterface(); c.mediaType = 1; c.mediaID = 5; c.disabledAnimationId = -1; c.enabledAnimationId = -1;
        cachedFor(c, false, 100, 50, 0);
        c.modelZoom = 512; c.modelPitch = 0; c.modelYaw = 0; drawModel(c);
        assertEquals(35 + 100, stat("screenX")[0]); assertEquals(40 + 50, stat("screenY")[0]);
        cachedFor(c, false, 100, 50, 0);
        c.modelZoom = 1024; drawModel(c);                              // zoom doubles the distance: offsets halve
        assertEquals(35 + 50, stat("screenX")[0]); assertEquals(40 + 25, stat("screenY")[0]);
        cachedFor(c, false, 100, 0, 0);
        c.modelZoom = 512; c.modelYaw = 512; drawModel(c);              // quarter turn about the vertical axis puts x into depth
        assertEquals(35, stat("screenX")[0], 2);
        cachedFor(c, false, 0, 0, 100);
        c.modelYaw = 512; drawModel(c);                                // the z axis turns into +/- x
        assertTrue(Math.abs(stat("screenX")[0] - 35) > 50);
        cachedFor(c, false, 0, 100, 0);
        c.modelYaw = 0; c.modelPitch = 512; drawModel(c);                // quarter pitch: y axis points along the view direction
        assertEquals(35, stat("screenX")[0], 2);
        // yaw and pitch are different axes: pitch leaves x alone, yaw does not
        cachedFor(c, false, 100, 0, 0);
        c.modelPitch = 256; c.modelYaw = 0; drawModel(c);
        int pitchOnly = stat("screenX")[0];
        cachedFor(c, false, 100, 0, 0);
        c.modelPitch = 0; c.modelYaw = 256; drawModel(c);
        int yawOnly = stat("screenX")[0];
        assertTrue(yawOnly != pitchOnly);
        assertEquals(135, pitchOnly, 2);
    }
    @Test public void animationIdsPickByEnabledState() throws Exception {
        Animation[] saved = Animation.anims;
        try {
            Animation.anims = new Animation[10];
            Constructor<Animation> k = Animation.class.getDeclaredConstructor(); k.setAccessible(true);
            Animation ok = k.newInstance(); ok.frameIds = new int[]{-1}; ok.secondFrameIds = new int[]{-1};
            Animation.anims[6] = ok;                                   // index 5 is null: choosing it throws
            RSInterface c = new RSInterface(); c.mediaType = 1; c.mediaID = 5; priv(c, "enabledMediaType", 1); priv(c, "enabledMediaID", 5);
            c.modelZoom = 512;
            c.disabledAnimationId = 6; c.enabledAnimationId = 5; select(c, false);
            cachedFor(c, false, 10, 10, 10); drawModel(c);              // not selected -> 257
            assertEquals(45, stat("screenX")[0], 2);
            c.disabledAnimationId = 5; c.enabledAnimationId = 6; select(c, true);
            cachedFor(c, true, 10, 10, 10); drawModel(c);               // selected -> 258
            assertEquals(45, stat("screenX")[0], 2);
            c.disabledAnimationId = 6; c.enabledAnimationId = 5;                             // selected but 258 points at the null entry
            try { drawModel(c); fail(); } catch (NullPointerException expected) { }
            c.disabledAnimationId = 5; c.enabledAnimationId = 6; select(c, false);           // not selected but 257 points at the null entry
            try { drawModel(c); fail(); } catch (NullPointerException expected) { }
        } finally { Animation.anims = saved; }
    }

    // ---------- discardInterface ----------
    @Test public void discardRemovesNonInventoryChildrenOfOneParent() {
        for (int n = 0; n < 8; n++) { RSInterface r = new RSInterface(); r.id = n; r.parentID = n % 2 == 0 ? 3 : 4; r.type = n < 4 ? 0 : 2; RSInterface.interfaceCache[n] = r; }
        RSInterface.discardInterface(3);
        for (int n = 0; n < 8; n++) assertEquals("slot " + n, n % 2 == 0 && n < 4, RSInterface.interfaceCache[n] == null);
        RSInterface.discardInterface(-1);
        assertNotNull(RSInterface.interfaceCache[1]);
    }

    // ---------- unpack ----------
    private static final class Bin {
        final ByteArrayOutputStream o = new ByteArrayOutputStream();
        Bin u8(int v) { o.write(v); return this; }
        Bin u16(int v) { o.write(v >> 8); o.write(v); return this; }
        Bin i32(int v) { u16(v >>> 16); u16(v & 0xffff); return this; }
        Bin str(String s) { byte[] b = s.getBytes(); o.write(b, 0, b.length); o.write(10); return this; }
        byte[] bytes() { return o.toByteArray(); }
    }
    private static StreamLoader loaderFor(byte[] data) throws Exception {
        Field u = sun.misc.Unsafe.class.getDeclaredField("theUnsafe"); u.setAccessible(true);
        StreamLoader l = (StreamLoader) ((sun.misc.Unsafe) u.get(null)).allocateInstance(StreamLoader.class);
        int hash = 0; for (char ch : "DATA".toCharArray()) hash = hash * 61 + ch - 32;
        set(l, "archiveData", data); set(l, "archiveDecompressed", true); set(l, "entryCount", 1);
        set(l, "entryNameHashes", new int[]{hash}); set(l, "entryUnpackedSizes", new int[]{data.length});
        set(l, "entryPackedSizes", new int[]{data.length}); set(l, "entryOffsets", new int[]{0});
        return l;
    }
    private static void set(Object o, String n, Object v) throws Exception { Field f = StreamLoader.class.getDeclaredField(n); f.setAccessible(true); f.set(o, v); }
    private static void common(Bin b, int id, int type, int atAction, int a214, int w, int h, int a254, int hoverHi, int hoverLo) {
        b.u16(id).u8(type).u8(atAction).u16(a214).u16(w).u16(h).u8(a254).u8(hoverHi);
        if (hoverHi != 0) b.u8(hoverLo);
    }
    private static StreamLoader spriteArchive() throws Exception {
        Method m = InterfaceSpriteCacheTest.class.getDeclaredMethod("archive"); m.setAccessible(true); return (StreamLoader) m.invoke(null);
    }

    @Test public void unpackReadsEveryFieldInOrder() throws Exception {
        Bin b = new Bin();
        b.u16(10);
        // id 1: container, with parent marker 100
        b.u16(65535).u16(100);
        common(b, 1, 0, 0, 7, 40, 30, 200, 3, 5);
        b.u8(2).u8(1).u16(10).u8(2).u16(20);                       // 2 conditions
        b.u8(2).u16(2).u16(3).u16(4).u16(1).u16(9);                // 2 value arrays of length 2 and 1
        b.u16(300).u8(1).u16(2).u16(2).u16(5).u16(6).u16(3).u16(0xfffb).u16(0xfff0);  // scrollMax, flag, 2 children
        // id 2: inventory (same parent)
        common(b, 2, 2, 0, 0, 2, 2, 0, 0, 0);
        b.u8(0).u8(0);                                              // no conditions, no value arrays
        b.u8(1).u8(0).u8(1).u8(0).u8(3).u8(4);
        for (int s = 0; s < 20; s++) {
            if (s == 0) b.u8(1).u16(0xfffb).u16(6).str("a,1");
            else if (s == 5) b.u8(1).u16(7).u16(0xfff8).str("");
            else b.u8(0);
        }
        b.str("Use").str("").str("Drop").str("").str("Examine");
        b.str("Sel").str("Spell").u16(77);
        // id 3: rectangle
        common(b, 3, 3, 0, 0, 12, 8, 0, 0, 0); b.u8(0).u8(0);
        b.u8(1);
        b.i32(0x111111).i32(0x222222).i32(0x333333).i32(0x444444);
        // id 4: text
        common(b, 4, 4, 1, 0, 60, 12, 0, 0, 0); b.u8(0).u8(0);
        b.u8(1).u8(1).u8(1).str("RuneScape rocks").str("on text").i32(0xabcdef).i32(5).i32(6).i32(7);
        b.str("");                                                  // tooltip -> "Ok"
        // id 5: two sprites
        common(b, 5, 5, 0, 0, 12, 8, 0, 0, 0); b.u8(0).u8(0);
        b.str("a,0").str("b,0");
        // id 6: model
        common(b, 6, 6, 0, 0, 12, 8, 0, 0, 0); b.u8(0).u8(0);
        b.u8(2).u8(9).u8(3).u8(4).u8(0).u8(1).u8(7).u16(100).u16(200).u16(300);
        // id 7: item text list
        common(b, 7, 7, 0, 0, 3, 2, 0, 0, 0); b.u8(0).u8(0);
        b.u8(1).u8(2).u8(1).i32(0x778899).u16(0xfffe).u16(4).u8(1);
        b.str("A").str("").str("C").str("").str("");
        // id 8: plain text, atAction 6 with empty tooltip
        common(b, 8, 8, 6, 0, 3, 2, 0, 0, 0); b.u8(0).u8(0);
        b.str("plain").str("");
        // id 9: type 1 with spell action
        common(b, 9, 1, 2, 0, 3, 2, 0, 0, 0); b.u8(0).u8(0);
        b.u16(0).u8(0).u8(0).u8(2).u8(1).i32(0x010203);
        b.str("Cast").str("Wind Strike").u16(321);
        TextDrawingArea[] fonts = {TextWidthTest.font(false), TextWidthTest.font(true), TextWidthTest.font(false)};
        RSInterface.unpack(loaderFor(b.bytes()), fonts, spriteArchive());

        assertEquals(10, RSInterface.interfaceCache.length);
        RSInterface c = RSInterface.interfaceCache[1];
        assertEquals(1, c.id); assertEquals(100, c.parentID); assertEquals(0, c.type); assertEquals(7, c.contentType);
        assertEquals(40, c.width); assertEquals(30, c.height); assertEquals((byte) 200, c.transparency);
        assertEquals((3 - 1 << 8) + 5, c.hoverRedirectId);
        assertArrayEquals(new int[]{1, 2}, c.conditionOperators); assertArrayEquals(new int[]{10, 20}, c.conditionValues);
        assertEquals(2, c.valueIndexArray.length); assertArrayEquals(new int[]{3, 4}, c.valueIndexArray[0]); assertArrayEquals(new int[]{9}, c.valueIndexArray[1]);
        assertEquals(300, c.scrollMax); assertTrue(c.hidden);
        assertArrayEquals(new int[]{2, 3}, c.children); assertArrayEquals(new int[]{5, -5}, c.childX); assertArrayEquals(new int[]{6, -16}, c.childY);

        RSInterface inv = RSInterface.interfaceCache[2];
        assertEquals(100, inv.parentID); assertEquals(-1, inv.hoverRedirectId); assertEquals(4, inv.itemIdsPlusOne.length); assertEquals(4, inv.itemQuantities.length);
        assertTrue(inv.aBoolean259); assertFalse(inv.isInventoryInterface); assertTrue(inv.usableItemInterface); assertFalse(inv.aBoolean235);
        assertEquals(3, inv.invSpritePadX); assertEquals(4, inv.invSpritePadY);
        assertEquals(20, inv.sprites.length); assertEquals(-5, inv.spritesX[0]); assertEquals(6, inv.spritesY[0]);
        assertEquals(7, inv.spritesX[5]); assertEquals(-8, inv.spritesY[5]);
        assertArrayEquals(new int[]{0x0000ff}, inv.sprites[0].pixels); assertNull(inv.sprites[5]); assertNull(inv.sprites[1]);
        assertArrayEquals(new String[]{"Use", null, "Drop", null, "Examine"}, inv.actions);
        assertEquals("Sel", inv.selectedActionName); assertEquals("Spell", inv.spellName); assertEquals(77, inv.spellUsableOn);

        RSInterface r = RSInterface.interfaceCache[3];
        assertTrue(r.filled); assertEquals(0x111111, r.textColor); assertEquals(0x222222, r.enabledColor); assertEquals(0x333333, r.disabledHoverColor); assertEquals(0x444444, r.enabledHoverColor);

        RSInterface t = RSInterface.interfaceCache[4];
        assertTrue(t.centeredText); assertSame(fonts[1], t.textDrawingAreas); assertTrue(t.shadowedText);
        assertEquals("RuneScape rocks".replaceAll("RuneScape", ClientSettings.SERVER_NAME), t.disabledText); assertEquals("on text", t.enabledText);
        assertEquals(0xabcdef, t.textColor); assertEquals(5, t.enabledColor); assertEquals(6, t.disabledHoverColor); assertEquals(7, t.enabledHoverColor);
        assertEquals("Ok", t.tooltip);

        RSInterface s = RSInterface.interfaceCache[5];
        assertArrayEquals(new int[]{0xff0000}, s.sprite1.pixels); assertArrayEquals(new int[]{0x0000ff}, s.sprite2.pixels);

        RSInterface m = RSInterface.interfaceCache[6];
        assertEquals(1, m.mediaType); assertEquals((2 - 1 << 8) + 9, m.mediaID);
        assertEquals(1, priv(m, "enabledMediaType")); assertEquals((3 - 1 << 8) + 4, priv(m, "enabledMediaID"));
        assertEquals(-1, m.disabledAnimationId); assertEquals(7, m.enabledAnimationId);
        assertEquals(100, m.modelZoom); assertEquals(200, m.modelPitch); assertEquals(300, m.modelYaw);

        RSInterface l = RSInterface.interfaceCache[7];
        assertEquals(6, l.itemIdsPlusOne.length); assertTrue(l.centeredText); assertSame(fonts[2], l.textDrawingAreas); assertTrue(l.shadowedText);
        assertEquals(0x778899, l.textColor); assertEquals(-2, l.invSpritePadX); assertEquals(4, l.invSpritePadY); assertTrue(l.isInventoryInterface);
        assertArrayEquals(new String[]{"A", null, "C", null, null}, l.actions);

        RSInterface p = RSInterface.interfaceCache[8];
        assertEquals("plain", p.disabledText); assertEquals("Continue", p.tooltip);

        RSInterface z = RSInterface.interfaceCache[9];
        assertFalse(z.centeredText); assertSame(fonts[2], z.textDrawingAreas); assertTrue(z.shadowedText); assertEquals(0x010203, z.textColor);
        assertEquals("Cast", z.selectedActionName); assertEquals("Wind Strike", z.spellName); assertEquals(321, z.spellUsableOn);
        assertNull(RSInterface.interfaceCache[0]);
    }
}
