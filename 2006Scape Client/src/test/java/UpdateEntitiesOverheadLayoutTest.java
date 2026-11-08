import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** Original-name observations of Game.updateEntities: overhead chat slots, overlap layout, icons, health bar and hit splats. */
public class UpdateEntitiesOverheadLayoutTest {
    private static final int W = 300, H = 200, BG = 0x345678;
    private int[] state, savedPixels;
    private Player savedMyPlayer;
    private int savedLoopCycle;

    static class Spy extends Game {
        final Map<Entity, int[]> at = new IdentityHashMap<Entity, int[]>();
        @Override public void projectEntityToScreen(Entity e, int h) {
            int[] p = at.get(e);
            if (p == null) { spriteDrawX = -1; spriteDrawY = -1; } else { spriteDrawX = p[0]; spriteDrawY = p[1]; }
        }
    }

    @Before public void before() {
        savedPixels = DrawingArea.pixels;
        state = new int[]{DrawingArea.width, DrawingArea.height, DrawingArea.topX, DrawingArea.topY, DrawingArea.bottomX,
            DrawingArea.bottomY, DrawingArea.scanlineRightLimit, DrawingArea.clipRightHalf, DrawingArea.clipBottomHalf};
        DrawingArea.bindRaster(H, W, new int[W * H]);
        Arrays.fill(DrawingArea.pixels, BG);
        savedMyPlayer = Game.myPlayer;
        savedLoopCycle = Game.loopCycle;
        Game.loopCycle = 1000;
    }
    @After public void after() {
        DrawingArea.pixels = savedPixels; DrawingArea.width = state[0]; DrawingArea.height = state[1]; DrawingArea.topX = state[2];
        DrawingArea.topY = state[3]; DrawingArea.bottomX = state[4]; DrawingArea.bottomY = state[5];
        DrawingArea.scanlineRightLimit = state[6]; DrawingArea.clipRightHalf = state[7]; DrawingArea.clipBottomHalf = state[8];
        Game.myPlayer = savedMyPlayer;
        Game.loopCycle = savedLoopCycle;
    }

    private Spy spy() throws Exception {
        Spy s = new Spy();
        s.groundArray = new NodeList[4][104][104];
        s.chatTextDrawingArea = TextWidthTest.font(false);
        s.aTextDrawingArea_1270 = s.chatTextDrawingArea;
        s.chatEffectsMode = 0; s.publicChatMode = 0;
        s.playerArray = new Player[64]; s.playerIndices = new int[64]; s.playerCount = 0;
        s.npcArray = new NPC[8]; s.npcIndices = new int[8]; s.npcCount = 0;
        Player me = new Player(); me.visible = false; me.headIcon = -1;
        Game.myPlayer = me;
        return s;
    }
    private static Player talker(String text, int color, int effect, int cycle) {
        Player p = new Player(); p.visible = true; p.headIcon = -1; p.combatLevel = 3; p.name = "AB";
        p.textSpoken = text; p.overheadChatColorCode = color; p.overheadChatEffectCode = effect; p.textCycle = cycle;
        return p;
    }
    private static Player addPlayer(Spy s, int slotId, Player p, int x, int y) {
        s.playerIndices[s.playerCount++] = slotId; s.playerArray[slotId] = p; s.at.put(p, new int[]{x, y});
        return p;
    }
    private static void run(Spy s) { s.updateEntities(); }
    private static int h(Spy s) { return s.chatTextDrawingArea.maxAsciiGlyphHeightPixels; }
    private static int count(int color) { int n = 0; for (int p : DrawingArea.pixels) if ((p & 0xffffff) == color) n++; return n; }
    private static int bottomRow(int color) { int b = -1; for (int i = 0; i < DrawingArea.pixels.length; i++) if ((DrawingArea.pixels[i] & 0xffffff) == color) b = i / W; return b; }
    private static Sprite solid(int color) { Sprite sp = new Sprite(1, 1); sp.pixels[0] = color; return sp; }

    // ---------- slot recording: anInt974 / 976-983 / j ----------
    @Test public void slotsCopyProjectedPositionMetricsStyleAndTextInEntityOrder() throws Exception {
        Spy s = spy();
        Player me = talker("A", 1, 0, 90); me.visible = true; Game.myPlayer = me; s.at.put(me, new int[]{40, 100});
        addPlayer(s, 7, talker("AB", 2, 0, 80), 100, 120);
        addPlayer(s, 9, talker("W", 3, 0, 70), -1, 50);          // not projected: must not consume a slot
        addPlayer(s, 3, talker("ABA", 4, 0, 60), 180, 140);
        NPC n = new NPC(); n.desc = (EntityDef) unsafe().allocateInstance(EntityDef.class); n.desc.headIconId = -1;
        n.textSpoken = "B"; n.overheadChatColorCode = 5; n.overheadChatEffectCode = 0; n.textCycle = 50;
        s.npcIndices[0] = 2; s.npcArray[2] = n; s.npcCount = 1; s.at.put(n, new int[]{260, 160});
        Arrays.fill(s.overheadChatDrawX, -7);
        run(s);
        String[] texts = {"A", "AB", "ABA", "B"};
        int[] xs = {40, 100, 180, 260}, ys = {100, 120, 140, 160}, colors = {1, 2, 4, 5}, cycles = {90, 80, 60, 50};
        for (int i = 0; i < 4; i++) {
            assertEquals(texts[i], s.overheadChatTexts[i]);
            assertEquals(xs[i], s.overheadChatDrawX[i]);
            assertEquals(ys[i], s.overheadChatDrawY[i]);
            assertEquals(h(s), s.overheadChatHeights[i]);
            assertEquals(s.chatTextDrawingArea.measureLiteralTextWidth(texts[i]) / 2, s.overheadChatHalfWidths[i]);
            assertEquals(colors[i], s.overheadChatColorCodes[i]);
            assertEquals(0, s.overheadChatEffectCodes[i]);
            assertEquals(cycles[i], s.overheadChatTextCycles[i]);
        }
        assertEquals(-7, s.overheadChatDrawX[4]);
        assertNull(s.overheadChatTexts[4]);
        assertEquals(6, s.overheadChatHalfWidths[1]); assertEquals(9, s.overheadChatHalfWidths[2]); // 13/2, 18/2: half widths
    }
    @Test public void publicChatModeFiltersOnlyPlayerSlotsBeforePlayerCount() throws Exception {
        Spy s = spy();
        Player me = talker("A", 0, 0, 90); me.visible = true; Game.myPlayer = me; s.at.put(me, new int[]{40, 100});
        addPlayer(s, 7, talker("AB", 0, 0, 80), 100, 120);
        NPC n = new NPC(); n.desc = (EntityDef) unsafe().allocateInstance(EntityDef.class); n.desc.headIconId = -1;
        n.textSpoken = "B"; s.npcIndices[0] = 2; s.npcArray[2] = n; s.npcCount = 1; s.at.put(n, new int[]{260, 160});
        s.publicChatMode = 2;
        run(s);
        assertEquals("B", s.overheadChatTexts[0]);
        assertNull(s.overheadChatTexts[1]);
    }
    @Test public void capacityIsFiftyAndExtraSpeakersAreDroppedWithoutAbortingLayout() throws Exception {
        Spy s = spy();
        assertEquals(50, s.overheadChatCapacity);
        assertEquals(50, s.overheadChatDrawX.length); assertEquals(50, s.overheadChatTexts.length);
        for (int i = 0; i < 60; i++) addPlayer(s, i + 1, talker(Integer.toBinaryString(i + 1).replace('0', 'A').replace('1', 'B'), 0, 0, 100), 150, 190);
        run(s);
        for (int i = 0; i < 50; i++) assertEquals(Integer.toBinaryString(i + 1).replace('0', 'A').replace('1', 'B'), s.overheadChatTexts[i]);
        assertEquals(190 - 49 * h(s), s.overheadChatDrawY[49]); // layout loop ran over all 50 slots
    }

    // ---------- overlap layout: k, k1, l1, j2, k2, flag, l2 ----------
    private Spy pair(String t0, int x0, int y0, String t1, int x1, int y1) throws Exception {
        Spy s = spy();
        addPlayer(s, 1, talker(t0, 0, 0, 100), x0, y0);
        addPlayer(s, 2, talker(t1, 0, 0, 100), x1, y1);
        run(s);
        return s;
    }
    @Test public void laterSlotMovesUpToTopOfEarlierWhenTheyOverlap() throws Exception {
        Spy s = pair("AB", 150, 100, "AB", 150, 100);
        assertEquals(100, s.overheadChatDrawY[0]);
        assertEquals(100 - h(s), s.overheadChatDrawY[1]);
    }
    @Test public void horizontalBoundaryIsSumOfOwnAndOtherHalfWidths() throws Exception {
        // slot0 "ABA" half 9, slot1 "AB" half 6 -> boundary at dx = 15 (strict)
        assertEquals(100, pair("ABA", 150, 100, "AB", 165, 100).overheadChatDrawY[1]);
        Spy s = pair("ABA", 150, 100, "AB", 164, 100);
        assertEquals(100 - h(s), s.overheadChatDrawY[1]);
        assertEquals(100, pair("ABA", 150, 100, "AB", 135, 100).overheadChatDrawY[1]);
        assertEquals(100 - h(s), pair("ABA", 150, 100, "AB", 136, 100).overheadChatDrawY[1]);
    }
    @Test public void verticalBoundaryUsesOwnHeightPlusTwoAndNeverMovesDown() throws Exception {
        int hh = h(spy());
        assertEquals(100 + hh + 2, pair("AB", 150, 100, "AB", 150, 100 + hh + 2).overheadChatDrawY[1]);
        assertEquals(100 - hh, pair("AB", 150, 100, "AB", 150, 100 + hh + 1).overheadChatDrawY[1]);
        assertEquals(100 - hh - 1, pair("AB", 150, 100, "AB", 150, 100 - hh - 1).overheadChatDrawY[1]);
        assertEquals(100 - hh, pair("AB", 150, 100, "AB", 150, 100 - hh + 1).overheadChatDrawY[1]);
    }
    @Test public void movedSlotIsRescannedAgainstEarlierSlots() throws Exception {
        Spy s = spy();
        int hh = h(s);
        addPlayer(s, 1, talker("AB", 0, 0, 100), 150, 60);
        addPlayer(s, 2, talker("AB", 0, 0, 100), 150, 60 + hh + 2);
        addPlayer(s, 3, talker("AB", 0, 0, 100), 150, 60 + hh + 2);
        run(s);
        assertEquals(60, s.overheadChatDrawY[0]);
        assertEquals(60 + hh + 2, s.overheadChatDrawY[1]);
        assertEquals(60 - hh, s.overheadChatDrawY[2]); // a single pass would leave it at 62
    }

    // ---------- renderer: s, i3, j3/k3/l3, i4, k4, j4, l4 ----------
    private Spy single(String text, int color, int effect, int cycle, int mode) throws Exception {
        Arrays.fill(DrawingArea.pixels, BG);
        Spy s = spy();
        Player me = talker(text, color, effect, cycle); Game.myPlayer = me; s.at.put(me, new int[]{150, 100});
        s.chatEffectsMode = mode;
        run(s);
        return s;
    }
    @Test public void colourCodeSelectsForegroundAndEffectsOffForcesYellow() throws Exception {
        single("AB", 2, 0, 100, 0);
        assertTrue(count(0x00ff00) > 0); assertEquals(0, count(0xffff00)); assertTrue(count(0) > 0);
        single("AB", 2, 0, 100, 1);
        assertEquals(0, count(0x00ff00)); assertTrue(count(0xffff00) > 0);
    }
    @Test public void animatedColourCodesUse150MinusTextCycleAsAge() throws Exception {
        single("AB", 9, 0, 150, 0); assertTrue(count(0xff0000) > 0);
        single("AB", 9, 0, 140, 0); assertTrue(count(0xff3200) > 0); assertEquals(0, count(0xff0000));
        single("AB", 10, 0, 140, 0); assertTrue(count(0xff0032) > 0);
        single("AB", 11, 0, 140, 0); assertTrue(count(0xcdffcd) > 0);
        single("AB", 9, 0, 90, 0); assertTrue(count(0xffff00 - 0x50000 * 10) > 0); // age 60
    }
    @Test public void scrollEffectUsesFullTextWidthAndAgeOffset() throws Exception {
        single("AB", 0, 0, 100, 0); int[] centred = DrawingArea.pixels.clone();
        single("AB", 0, 4, 75, 0);   // age 75: offset 75*(13+100)/150 = 56 -> x 144 = centred origin
        assertArrayEquals(centred, DrawingArea.pixels);
        single("AB", 0, 4, 76, 0);   // age 74: offset 55 -> x 145
        assertFalse(Arrays.equals(centred, DrawingArea.pixels));
        single("AB", 0, 4, 150, 0);  // age 0: starts at right clip edge
        assertEquals(0, count(0xffff00));
    }
    @Test public void slideEffectShiftsByAgeOutsideMiddleWindow() throws Exception {
        single("AB", 0, 5, 100, 0); int b50 = bottomRow(0xffff00);
        assertTrue(b50 > 0);
        single("AB", 0, 5, 127, 0); assertEquals(b50 - 2, bottomRow(0xffff00)); // age 23 -> -2
        single("AB", 0, 5, 23, 0);  assertEquals(b50 + 2, bottomRow(0xffff00)); // age 127 -> +2
        single("AB", 0, 5, 25, 0);  assertEquals(b50, bottomRow(0xffff00));     // age 125 -> 0
        single("AB", 0, 5, 125, 0); assertEquals(b50, bottomRow(0xffff00));     // age 25 -> 0
    }

    // ---------- BUG-001 fix: effect sizing applies to the speaker's own slot ----------
    @Test public void effectSizingAppliesToTheSpeakersOwnSlot() throws Exception {
        int hh = h(spy());
        for (int effect = 1; effect <= 3; effect++) {
            Spy s = single("AB", 0, effect, 100, 0);
            assertEquals("wave/shake height, effect " + effect, hh + 10, s.overheadChatHeights[0]);
            assertEquals("wave/shake baseline, effect " + effect, 105, s.overheadChatDrawY[0]);
            assertEquals("next slot untouched, effect " + effect, 0, s.overheadChatHeights[1]);
        }
        Spy scroll = single("AB", 0, 4, 100, 0);
        assertEquals(60, scroll.overheadChatHalfWidths[0]);
        assertEquals(0, scroll.overheadChatHalfWidths[1]);
        Spy slide = single("AB", 0, 5, 100, 0);
        assertEquals(hh + 5, slide.overheadChatHeights[0]);
        assertEquals(0, slide.overheadChatHeights[1]);
        Spy off = single("AB", 0, 1, 100, 1);  // chat effects off: plain metrics
        assertEquals(hh, off.overheadChatHeights[0]);
        assertEquals(100, off.overheadChatDrawY[0]);
        Spy plain = single("AB", 0, 0, 100, 0);
        assertEquals(hh, plain.overheadChatHeights[0]);
        assertEquals(6, plain.overheadChatHalfWidths[0]);
    }
    @Test public void wavingSpeakerReservesItsTallerBoxInTheStack() throws Exception {
        Spy s = spy();
        int hh = h(s);
        addPlayer(s, 1, talker("AB", 0, 1, 100), 150, 100);
        addPlayer(s, 2, talker("AB", 0, 0, 100), 150, 100);
        run(s);
        assertEquals(105, s.overheadChatDrawY[0]);
        assertEquals("plain line stacks above the wave box", 105 - (hh + 10), s.overheadChatDrawY[1]);
        assertEquals(hh, s.overheadChatHeights[1]);
    }
    @Test public void fiftyEffectSpeakersStillLayOutAndDraw() throws Exception {
        Spy s = spy();
        int hh = h(s);
        for (int i = 0; i < 50; i++) addPlayer(s, i + 1, talker("AB", 0, 1, 100), 150, 190);
        run(s);
        assertEquals(195, s.overheadChatDrawY[0]);
        assertEquals("layout reached the 50th slot", 195 - 49 * (hh + 10), s.overheadChatDrawY[49]);
    }

    // ---------- l (icon stack), i1 (health bar), j1 (hit slot) ----------
    private int hintRow(int skull, int head) throws Exception {
        Arrays.fill(DrawingArea.pixels, BG);
        Spy s = spy();
        s.skullIcons = new Sprite[]{solid(0x111111), solid(0x111111)};
        s.headIcons = new Sprite[8]; for (int i = 0; i < 8; i++) s.headIcons[i] = solid(0x222222);
        s.headIconsHint = new Sprite[]{solid(0x333333), solid(0x333333)};
        Player p = talker(null, 0, 0, 100); p.skullIcon = skull; p.headIcon = head;
        addPlayer(s, 5, p, 150, 100);
        s.anInt855 = 10; s.anInt933 = 5;
        run(s);
        return bottomRow(0x333333);
    }
    @Test public void hintIconStacksAboveSkullAndPrayerIcons() throws Exception {
        assertEquals(70, hintRow(2, 7));
        assertEquals(45, hintRow(0, 7));
        assertEquals(52, hintRow(2, 0));
        assertEquals(27, hintRow(1, 3));
    }
    private int[] bar(int cur, int max) throws Exception {
        Arrays.fill(DrawingArea.pixels, BG);
        Spy s = spy();
        Player me = talker(null, 0, 0, 100); Game.myPlayer = me; s.at.put(me, new int[]{150, 100});
        me.loopCycleStatus = Game.loopCycle + 5; me.currentHealth = cur; me.maxHealth = max;
        run(s);
        int g = 0, r = 0;
        for (int x = 0; x < W; x++) { int c = DrawingArea.pixels[99 * W + x] & 0xffffff; if (c == 0x00ff00) g++; if (c == 0xff0000) r++; }
        return new int[]{g, r};
    }
    @Test public void healthBarFillIsCurrentTimesThirtyOverMaxCappedAtThirty() throws Exception {
        assertArrayEquals(new int[]{10, 20}, bar(1, 3));
        assertArrayEquals(new int[]{20, 10}, bar(2, 3));
        assertArrayEquals(new int[]{30, 0}, bar(3, 3));
        assertArrayEquals(new int[]{30, 0}, bar(4, 3));
    }
    private int hitPixel(int slot, boolean active) throws Exception {
        Arrays.fill(DrawingArea.pixels, BG);
        Spy s = spy();
        s.hitMarks = new Sprite[]{solid(0x444444)};
        Player me = talker(null, 0, 0, 100); Game.myPlayer = me; s.at.put(me, new int[]{150, 100});
        me.hitsLoopCycle[slot] = active ? Game.loopCycle + 5 : Game.loopCycle;
        run(s);
        for (int i = 0; i < DrawingArea.pixels.length; i++) if ((DrawingArea.pixels[i] & 0xffffff) == 0x444444) return i;
        return -1;
    }
    @Test public void hitSlotSelectsSplatOffset() throws Exception {
        assertEquals(88 * W + 138, hitPixel(0, true));
        assertEquals(68 * W + 138, hitPixel(1, true));
        assertEquals(78 * W + 123, hitPixel(2, true));
        assertEquals(78 * W + 153, hitPixel(3, true));
        assertEquals(-1, hitPixel(2, false));
    }

    private static sun.misc.Unsafe unsafe() throws Exception {
        Field f = sun.misc.Unsafe.class.getDeclaredField("theUnsafe"); f.setAccessible(true);
        return (sun.misc.Unsafe) f.get(null);
    }
}
