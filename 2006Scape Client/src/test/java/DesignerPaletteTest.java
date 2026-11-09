import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Character-designer colour palettes (Game.appearanceColorPalettes, Game.torsoSecondaryColorPalette), the designer button handler
 * (Game.promptUserForInput content types 300-326) and the designer model build (Game.drawFriendsListOrWelcomeScreen
 * content type 327).
 */
public class DesignerPaletteTest {
    private static Field f(Class<?> c, String n) throws Exception { Field x = c.getDeclaredField(n); x.setAccessible(true); return x; }
    private static void set(Object o, String n, Object v) throws Exception { f(o.getClass(), n).set(o, v); }
    private static Object sget(Class<?> c, String n) throws Exception { return f(c, n).get(null); }
    private static void sset(Class<?> c, String n, Object v) throws Exception { f(c, n).set(null, v); }
    private static <T> T make(Class<T> c) throws Exception { Constructor<T> k = c.getDeclaredConstructor(); k.setAccessible(true); return k.newInstance(); }
    private static RSInterface button(int type) { RSInterface r = new RSInterface(); r.contentType = type; return r; }

    private Object savedHeaders, savedFetcher, savedIdk; private int savedIdkLength; private Animation[] savedAnims; private Player savedMe;
    @Before public void setUp() throws Exception {
        savedHeaders = sget(Model.class, "modelHeaders"); savedFetcher = sget(Model.class, "modelRequester");
        savedIdk = IDK.cache; savedIdkLength = IDK.length; savedAnims = Animation.anims; savedMe = Game.myPlayer;
        Model.initModelHeaders(64, new PlayerNpcSemanticsTest.Spy());
    }
    @After public void tearDown() throws Exception {
        sset(Model.class, "modelHeaders", savedHeaders); sset(Model.class, "modelRequester", savedFetcher);
        IDK.cache = (IDK[]) savedIdk; IDK.length = savedIdkLength; Animation.anims = savedAnims; Game.myPlayer = savedMe;
        RSInterface.replaceCachedModel(null, 0, 5);
    }

    private static IDK kit(int bodyPart, boolean notSelectable, int... models) throws Exception {
        IDK k = make(IDK.class); k.bodyPartIndex = bodyPart; k.notSelectable = notSelectable; set(k, "bodyModelIds", models); return k;
    }
    private static Game designer() {
        Game g = new Game();
        g.stream = new Stream(new byte[100]);
        g.stream.encryption = new ISAACRandomGen(new int[]{1, 2, 3, 4});
        Arrays.fill(g.designerKitIds, -1);
        g.designerMale = true;
        return g;
    }

    // ---- content types 300-313: kit slot k = (type-300)/2, even type steps back, odd steps forward ----
    @Test public void kitButtonsStepOnlyTheirSlotThroughSelectableKitsOfThatBodyPartWithWrap() throws Exception {
        // kits: 0 part0, 1 part1, 2 part0 (not selectable), 3 part0, 4 part7 (female part 0), 5 part1
        IDK.cache = new IDK[]{kit(0, false), kit(1, false), kit(0, true), kit(0, false), kit(7, false), kit(1, false)};
        IDK.length = 6;
        Game g = designer();
        g.designerKitIds[0] = 0; g.designerKitIds[1] = 1;
        g.designerModelDirty = false;
        g.promptUserForInput(button(301));                    // slot 0 forward: skips kit 1 (part 1) and kit 2 (not selectable)
        assertArrayEquals(new int[]{3, 1, -1, -1, -1, -1, -1}, g.designerKitIds);
        assertTrue(g.designerModelDirty);
        g.promptUserForInput(button(301));                    // forward past the end wraps to kit 0
        assertEquals(0, g.designerKitIds[0]);
        g.promptUserForInput(button(300));                    // back from 0 wraps to the last part-0 kit (3)
        assertEquals(3, g.designerKitIds[0]);
        g.promptUserForInput(button(303));                    // slot 1 forward: kit 1 -> kit 5, slot 0 untouched
        assertArrayEquals(new int[]{3, 5, -1, -1, -1, -1, -1}, g.designerKitIds);
        g.promptUserForInput(button(302));                    // slot 1 back: kit 5 -> kit 1
        assertEquals(1, g.designerKitIds[1]);
        g.designerModelDirty = false;
        g.promptUserForInput(button(305));                    // slot 2 holds -1: nothing happens
        assertArrayEquals(new int[]{3, 1, -1, -1, -1, -1, -1}, g.designerKitIds);
        assertFalse(g.designerModelDirty);
        g.designerMale = false;                               // female: slot 0 accepts only body part 0 + 7
        g.promptUserForInput(button(301));
        assertEquals(4, g.designerKitIds[0]);
        g.promptUserForInput(button(300));                    // only one part-7 kit: back lands on itself
        assertEquals(4, g.designerKitIds[0]);
    }

    // ---- content types 314-323: colour slot s = (type-314)/2, wrap at that slot's palette length ----
    @Test public void colourButtonStoresAWrappedIndexIntoThatSlotsPaletteOnly() {
        int[][] pal = Game.appearanceColorPalettes;
        Game g = designer();
        g.designerColorIndices[3] = pal[3].length - 2;
        g.promptUserForInput(button(321));                    // slot 3 forward to the last index
        assertArrayEquals(new int[]{0, 0, 0, pal[3].length - 1, 0}, g.designerColorIndices);
        g.promptUserForInput(button(321));                    // past the end of palette 3 -> 0 (palette 1 is longer)
        assertArrayEquals(new int[]{0, 0, 0, 0, 0}, g.designerColorIndices);
        assertTrue(pal[1].length > pal[3].length);
        g.promptUserForInput(button(322));                    // slot 4 back wraps at palette 4's length
        assertArrayEquals(new int[]{0, 0, 0, 0, pal[4].length - 1}, g.designerColorIndices);
    }

    @Test public void paletteLengthsBoundTheServerAppearanceIndicesAndSlotOneSharesItsLengthWithTheSecondaryPalette() {
        int[][] pal = Game.appearanceColorPalettes;
        assertEquals(5, pal.length);
        assertEquals(pal[1].length, Game.torsoSecondaryColorPalette.length);   // indexed with slot 1's index
        assertNotEquals(pal[0].length, Game.torsoSecondaryColorPalette.length);
    }

    // ---- content type 326: send gender, kits 0..6 in slot order, then colour indices 0..4 in slot order ----
    @Test public void type326WritesKitSlotsThenColourSlotsInOrder() throws Exception {
        Game g = designer();
        System.arraycopy(new int[]{10, 11, 12, -1, 14, 15, 16}, 0, g.designerKitIds, 0, 7);
        System.arraycopy(new int[]{1, 2, 3, 4, 5}, 0, g.designerColorIndices, 0, 5);
        g.designerMale = false;
        assertTrue(g.promptUserForInput(button(326)));
        assertEquals(14, g.stream.currentOffset);              // opcode + gender + 7 kits + 5 colours
        byte[] b = Arrays.copyOfRange(g.stream.buffer, 1, 14);
        assertArrayEquals(new byte[]{1, 10, 11, 12, -1, 14, 15, 16, 1, 2, 3, 4, 5}, b);
    }

    // ---- content type 327: build the designer model from the selected kits and recolour it ----
    private Game buildSetup(int[] kitColours) throws Exception {
        IDK[] cache = new IDK[kitColours.length + 1];
        for (int i = 0; i < kitColours.length; i++) {
            Model.loadModelHeader(PlayerNpcSemanticsTest.triangle(i + 1, kitColours[i]), i + 1);
            cache[i] = kit(i, false, i + 1);
        }
        cache[kitColours.length] = kit(6, false, 40);         // model 40 has no header: not loaded
        IDK.cache = cache;
        Animation a = make(Animation.class); set(a, "frameIds", new int[]{-1});
        Animation.anims = new Animation[]{a};
        Player me = new Player(); me.idleAnimationId = 0; Game.myPlayer = me;
        Game g = designer();
        for (int i = 0; i < kitColours.length; i++) g.designerKitIds[i] = i;
        g.designerModelDirty = true;
        return g;
    }
    private static Model cachedDesignerModel() throws Exception {
        return (Model) ((LruCache) sget(RSInterface.class, "modelCache")).insertFromCache((5 << 16) + 0);
    }
    private static int[] hueSat(Model m) throws Exception {
        int[] shade = (int[]) f(Model.class, "faceShadeA").get(m);
        int[] r = new int[m.faceCount]; for (int i = 0; i < r.length; i++) r[i] = shade[i] >> 7; return r;
    }
    private static int[] hueSat(int... colours) { int[] r = colours.clone(); for (int i = 0; i < r.length; i++) r[i] >>= 7; return r; }

    @Test public void type327ReplacesEachSlotsBaseColourWithThePaletteEntryAtThatSlotsIndex() throws Exception {
        int[][] pal = Game.appearanceColorPalettes; int[] sec = Game.torsoSecondaryColorPalette;
        Game g = buildSetup(new int[]{pal[0][0], pal[1][0], pal[2][0], pal[3][0], pal[4][0], sec[0]});
        System.arraycopy(new int[]{2, 1, 3, 0, 4}, 0, g.designerColorIndices, 0, 5);
        RSInterface c = button(327);
        g.drawFriendsListOrWelcomeScreen(c);
        assertFalse(g.designerModelDirty);
        assertEquals(5, c.mediaType); assertEquals(0, c.mediaID);
        Model m = cachedDesignerModel();
        assertEquals(6, m.faceCount);                         // six selected kits, slot 6 is -1 and skipped
        assertArrayEquals(hueSat(pal[0][2], pal[1][1], pal[2][3], pal[3][0], pal[4][4], sec[1]), hueSat(m));
    }

    @Test public void type327SecondaryPaletteFollowsOnlySlotOneAndIndexZeroLeavesTheBaseColour() throws Exception {
        int[][] pal = Game.appearanceColorPalettes; int[] sec = Game.torsoSecondaryColorPalette;
        Game g = buildSetup(new int[]{pal[0][0], pal[1][0], pal[2][0], pal[3][0], pal[4][0], sec[0]});
        System.arraycopy(new int[]{0, 0, 5, 0, 0}, 0, g.designerColorIndices, 0, 5);
        g.drawFriendsListOrWelcomeScreen(button(327));
        assertArrayEquals(hueSat(pal[0][0], pal[1][0], pal[2][5], pal[3][0], pal[4][0], sec[0]), hueSat(cachedDesignerModel()));
        g = buildSetup(new int[]{pal[0][0], pal[1][0], pal[2][0], pal[3][0], pal[4][0], sec[0]});
        System.arraycopy(new int[]{0, 5, 0, 0, 0}, 0, g.designerColorIndices, 0, 5);
        g.drawFriendsListOrWelcomeScreen(button(327));
        assertArrayEquals(hueSat(pal[0][0], pal[1][5], pal[2][0], pal[3][0], pal[4][0], sec[5]), hueSat(cachedDesignerModel()));
    }

    @Test public void type327CountsOnlySelectedKitsAndWaitsForUnloadedKitModels() throws Exception {
        int[][] pal = Game.appearanceColorPalettes;
        Game g = buildSetup(new int[]{pal[3][0], pal[3][0], pal[3][0]});
        g.designerKitIds[1] = -1;                             // kits in slots 0 and 2 only
        g.drawFriendsListOrWelcomeScreen(button(327));
        Model m = cachedDesignerModel();
        assertEquals(2, m.faceCount);
        assertEquals(6, m.vertexCount);
        int[] xs = Arrays.copyOf(m.vertexX, 6); Arrays.sort(xs);
        assertArrayEquals(new int[]{21, 21, 23, 23, 31, 33}, xs);   // kit models 1 and 3 (x = 20 + id), not 2
        g = buildSetup(new int[]{pal[3][0], pal[3][0], pal[3][0]});
        g.designerKitIds[6] = 3;                              // kit 3 = model 40, not loaded
        RSInterface.replaceCachedModel(null, 0, 5);
        g.drawFriendsListOrWelcomeScreen(button(327));
        assertTrue(g.designerModelDirty);                     // build postponed
        assertNull(cachedDesignerModel());
    }
}
