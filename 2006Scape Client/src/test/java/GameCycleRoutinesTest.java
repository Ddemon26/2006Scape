import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/** Semantic tests (original names) for Game.scrollAnimatedTextures/38/46/49 and their fields. */
public class GameCycleRoutinesTest {
    private static final int[] TEX = {17, 24, 34, 40};
    private Object[] saved;
    private Player savedPlayer;
    private boolean savedLowMem;

    private static Field tf(String n) throws Exception {
        Field f = Texture.class.getDeclaredField(n); f.setAccessible(true); return f;
    }
    private static sun.misc.Unsafe unsafe() throws Exception {
        Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (sun.misc.Unsafe) field.get(null);
    }
    private static Game game() throws Exception {
        return (Game) unsafe().allocateInstance(Game.class);
    }
    private static Background bg(int w, int h, byte[] px) throws Exception {
        Background b = (Background) unsafe().allocateInstance(Background.class);
        b.imageWidth = w; b.imageHeight = h; b.paletteIndices = px; return b;
    }

    @Before public void setUp() throws Exception {
        saved = new Object[]{tf("textureBackgrounds").get(null), tf("lastUsedStamps").get(null),
            tf("textureBuffers").get(null), tf("freeBuffers").get(null), tf("freeBufferCount").get(null)};
        savedPlayer = Game.myPlayer;
        savedLowMem = Game.lowMem;
    }
    @After public void tearDown() throws Exception {
        String[] n = {"textureBackgrounds", "lastUsedStamps", "textureBuffers", "freeBuffers", "freeBufferCount"};
        for (int i = 0; i < n.length; i++) tf(n[i]).set(null, saved[i]);
        Game.myPlayer = savedPlayer; Game.lowMem = savedLowMem;
    }

    private static byte[] ramp(int n) { byte[] b = new byte[n]; for (int i = 0; i < n; i++) b[i] = (byte) i; return b; }

    @Test public void method37ScrollsOnlyRecentlyUsedAnimatedTexturesByWidthTimesCyclesTimesTwo() throws Exception {
        Game g = game(); Game.lowMem = false;
        Background[] bgs = new Background[64]; int[] stamps = new int[64]; int[][] bufs = new int[64][];
        for (int t : TEX) { bgs[t] = bg(4, 4, ramp(16)); bufs[t] = new int[1]; }
        stamps[17] = 10; stamps[24] = 9; stamps[34] = 11; stamps[40] = 10;
        tf("textureBackgrounds").set(null, bgs); tf("lastUsedStamps").set(null, stamps);
        tf("textureBuffers").set(null, bufs); tf("freeBuffers").set(null, new int[8][]); tf("freeBufferCount").set(null, 0);
        g.textureScrollScratch = new byte[16]; g.cyclesSinceDraw = 1;
        g.scrollAnimatedTextures(10);
        // shift = width*cycles*2 = 8: out[i] = in[(i-8)&15]
        assertEquals(8, bgs[17].paletteIndices[0] & 0xff); assertEquals(0, bgs[17].paletteIndices[8]);
        assertEquals(8, bgs[34].paletteIndices[0] & 0xff); assertEquals(8, bgs[40].paletteIndices[0] & 0xff);
        assertEquals(0, bgs[24].paletteIndices[0]); assertEquals(5, bgs[24].paletteIndices[5]); // stamp 9 < 10: untouched
        assertNull(bufs[17]); assertNotNull(bufs[24]); assertNull(bufs[34]); assertNull(bufs[40]);
        // cycles = 2 -> shift 16 == full wrap -> identity
        for (int t : TEX) { bgs[t].paletteIndices = ramp(16); bufs[t] = new int[1]; }
        g.cyclesSinceDraw = 2; g.scrollAnimatedTextures(10);
        assertEquals(0, bgs[17].paletteIndices[0]); assertEquals(7, bgs[17].paletteIndices[7]);
        // cycles = 0 -> no movement
        g.cyclesSinceDraw = 0; g.scrollAnimatedTextures(10);
        assertEquals(3, bgs[34].paletteIndices[3]);
        // width matters: 2x8 texture, one cycle -> shift 4
        bgs[40] = bg(2, 8, ramp(16)); bufs[40] = new int[1]; g.cyclesSinceDraw = 1; g.scrollAnimatedTextures(10);
        assertEquals(12, bgs[40].paletteIndices[0] & 0xff);
    }

    @Test public void method37DoesNothingInLowMemory() throws Exception {
        Game g = game(); Game.lowMem = true;
        Background[] bgs = new Background[64]; int[] stamps = new int[64];
        bgs[17] = bg(4, 4, ramp(16)); stamps[17] = 50;
        tf("textureBackgrounds").set(null, bgs); tf("lastUsedStamps").set(null, stamps);
        g.textureScrollScratch = new byte[16]; g.cyclesSinceDraw = 1; g.scrollAnimatedTextures(0);
        assertEquals(0, bgs[17].paletteIndices[0]);
    }

    @Test public void method38DecrementsTimersAndClearsTextAtZero() throws Exception {
        Game g = game();
        g.playerArray = new Player[2048]; g.npcArray = new NPC[4];
        g.playerIndices = new int[4]; g.npcIndices = new int[4];
        Player me = new Player(), other = new Player(), unlisted = new Player();
        g.playerArray[g.myPlayerIndex] = me; g.playerArray[2] = other; g.playerArray[3] = unlisted;
        g.playerIndices[0] = 2; g.playerIndices[1] = 3; g.playerCount = 1;
        me.textCycle = 1; me.textSpoken = "a";
        other.textCycle = 3; other.textSpoken = "b";
        unlisted.textCycle = 5; unlisted.textSpoken = "c";
        NPC n = new NPC(), n2 = new NPC(), n3 = new NPC();
        g.npcArray[0] = n; g.npcArray[1] = n2; g.npcArray[2] = n3; g.npcIndices[0] = 0; g.npcIndices[1] = 1; g.npcCount = 2;
        n.textCycle = 1; n.textSpoken = "x"; n2.textCycle = 4; n2.textSpoken = "y"; n3.textCycle = 4; n3.textSpoken = "z";
        g.decrementOverheadTextTimers();
        assertEquals(0, me.textCycle); assertNull(me.textSpoken);
        assertEquals(2, other.textCycle); assertEquals("b", other.textSpoken);
        assertEquals(5, unlisted.textCycle); // beyond playerCount
        assertEquals(0, n.textCycle); assertNull(n.textSpoken);
        assertEquals(3, n2.textCycle); assertEquals("y", n2.textSpoken);
        assertEquals(4, n3.textCycle);
        g.decrementOverheadTextTimers(); // zero stays zero
        assertEquals(0, me.textCycle); assertEquals(0, n.textCycle);
    }

    @Test public void method49ReadsMaskForEachQueuedPlayer() throws Exception {
        Game g = game(); g.playerArray = new Player[8]; g.maskUpdateIndices = new int[8];
        Player a = new Player(), b = new Player(), c = new Player();
        g.playerArray[2] = a; g.playerArray[5] = b; g.playerArray[6] = c;
        g.maskUpdateIndices[0] = 5; g.maskUpdateIndices[1] = 2; g.maskUpdateIndices[2] = 6; g.maskUpdateCount = 2; // third is beyond count
        Stream s = new Stream(new byte[]{4, 'h', 'i', 10, 4, 'o', 'k', 10, 99});
        g.decodePlayerUpdateMasks(s);
        assertEquals("hi", b.textSpoken); assertEquals("ok", a.textSpoken); assertNull(c.textSpoken);
        assertEquals(8, s.currentOffset);
    }

    @Test public void method49ExtendsMaskWithSecondByteWhen0x40Set() throws Exception {
        Game g = game(); g.playerArray = new Player[8]; g.maskUpdateIndices = new int[8];
        Player a = new Player(); g.playerArray[3] = a;
        g.maskUpdateIndices[0] = 3; g.maskUpdateCount = 1;
        Stream s = new Stream(new byte[]{0x44, 0x00, (byte) 104, (byte) 105, 10, 77});
        g.decodePlayerUpdateMasks(s);
        assertEquals("hi", a.textSpoken); assertEquals(5, s.currentOffset);
        Player b = new Player(); g.playerArray[3] = b;
        Stream t = new Stream(new byte[]{4, (byte) 113, (byte) 113, 10, 77});
        g.decodePlayerUpdateMasks(t);
        assertEquals("qq", b.textSpoken); assertEquals(4, t.currentOffset);
    }

    private static EntityDef[] defs(int type, int size) throws Exception {
        Constructor<EntityDef> c = EntityDef.class.getDeclaredConstructor(); c.setAccessible(true);
        EntityDef[] d = new EntityDef[20];
        for (int i = 0; i < 20; i++) { d[i] = c.newInstance(); d[i].type = 1000 + i; }
        d[0].type = type; d[0].tileSize = (byte) size; d[0].turnSpeed = (byte) 32;
        return d;
    }

    private static Stream bits(int[][] fields) {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        long acc = 0; int n = 0;
        for (int[] f : fields) {
            acc = (acc << f[1]) | (f[0] & ((1L << f[1]) - 1)); n += f[1];
            while (n >= 8) { out.write((int) (acc >> (n - 8)) & 0xff); n -= 8; acc &= (1L << n) - 1; }
        }
        if (n > 0) out.write((int) (acc << (8 - n)) & 0xff);
        byte[] b = out.toByteArray(); byte[] r = new byte[b.length + 8]; System.arraycopy(b, 0, r, 0, b.length);
        return new Stream(r);
    }

    private static Field entityCache() throws Exception {
        Field cf = EntityDef.class.getDeclaredField("cache"); cf.setAccessible(true); return cf;
    }

    @Test public void method46AddsNpcsWithOffsetsSnapFlagAndMaskQueue() throws Exception {
        Field cf = entityCache(); Object savedCache = cf.get(null);
        try {
            cf.set(null, defs(7, 1));
            Game g = game(); g.npcArray = new NPC[16384]; g.npcIndices = new int[16]; g.maskUpdateIndices = new int[16];
            Game.myPlayer = new Player(); Game.myPlayer.setPos(50, 60, true); Game.loopCycle = 33;
            // npc 9: yOff=+3 xOff=-2 snap=1 type=7 mask=1 ; npc 12: yOff=-1 xOff=+4 snap=0 mask=0 ; then 16383
            Stream s = bits(new int[][]{{9,14},{3,5},{30,5},{1,1},{7,12},{1,1},
                                        {12,14},{31,5},{4,5},{0,1},{7,12},{0,1},{16383,14}});
            g.addNewNpcs(8, s);
            assertEquals(2, g.npcCount); assertEquals(9, g.npcIndices[0]); assertEquals(12, g.npcIndices[1]);
            NPC a = g.npcArray[9], b = g.npcArray[12];
            assertEquals(48, a.smallX[0]); assertEquals(63, a.smallY[0]); // x = 50 + xOff, y = 60 + yOff
            assertEquals(54, b.smallX[0]); assertEquals(59, b.smallY[0]);
            assertEquals(33, a.anInt1537); assertEquals(7, a.desc.type);
            assertEquals(1, g.maskUpdateCount); assertEquals(9, g.maskUpdateIndices[0]);
            assertEquals(48 * 128 + 64, a.x);
            // stops when fewer than 21 bits remain
            Game g2 = game(); g2.npcArray = new NPC[16384]; g2.npcIndices = new int[16]; g2.maskUpdateIndices = new int[16];
            g2.addNewNpcs(2, bits(new int[][]{{9,14},{3,5},{30,5},{1,1},{7,12},{1,1}}));
            assertEquals(0, g2.npcCount);
        } finally { cf.set(null, savedCache); }
    }

    @Test public void method46SnapFlagControlsWalkVersusTeleport() throws Exception {
        Field cf = entityCache(); Object savedCache = cf.get(null);
        try {
            cf.set(null, defs(7, 1));
            Game g = game(); g.npcArray = new NPC[16384]; g.npcIndices = new int[16]; g.maskUpdateIndices = new int[16];
            Game.myPlayer = new Player(); Game.myPlayer.setPos(50, 60, true);
            NPC pre = new NPC(); pre.setPos(45, 63, true); g.npcArray[9] = pre;
            g.addNewNpcs(8, bits(new int[][]{{9,14},{3,5},{30,5},{0,1},{7,12},{0,1},{16383,14}}));
            assertEquals(1, pre.smallXYIndex); assertEquals(48, pre.smallX[0]); // flag 0, within 8 tiles: queued step
            g.npcCount = 0; pre.setPos(45, 63, true);
            g.addNewNpcs(8, bits(new int[][]{{9,14},{3,5},{30,5},{1,1},{7,12},{0,1},{16383,14}}));
            assertEquals(0, pre.smallXYIndex); assertEquals(48, pre.smallX[0]); // flag 1: snap despite being near
        } finally { cf.set(null, savedCache); }
    }
}
