import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class GameMenuBuildersTest {
    private Player priorPlayer;
    private int priorVolume;

    @Before public void save() { priorPlayer = Game.myPlayer; priorVolume = Signlink.wavevol; }
    @After public void restore() { Game.myPlayer = priorPlayer; Signlink.wavevol = priorVolume; }

    private static void set(Object o, String name, Object v) throws Exception {
        Class<?> c = o.getClass();
        while (true) {
            try { Field f = c.getDeclaredField(name); f.setAccessible(true); f.set(o, v); return; }
            catch (NoSuchFieldException e) { c = c.getSuperclass(); if (c == null) throw e; }
        }
    }

    private static Game game() throws Exception {
        Field uf = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        uf.setAccessible(true);
        Game g = (Game) ((sun.misc.Unsafe) uf.get(null)).allocateInstance(Game.class);
        set(g, "menuActionName", new String[500]);
        set(g, "menuActionID", new int[500]);
        set(g, "menuActionCmd1", new int[500]);
        set(g, "menuActionCmd2", new int[500]);
        set(g, "menuActionCmd3", new int[500]);
        set(g, "atPlayerActions", new String[5]);
        set(g, "atPlayerArray", new boolean[5]);
        set(g, "chatTypes", new int[100]);
        set(g, "chatNames", new String[100]);
        set(g, "chatMessages", new String[100]);
        set(g, "dialogID", -1);
        set(g, "backDialogID", -1);
        Game.myPlayer = new Player();
        return g;
    }

    private static EntityDef def() throws Exception {
        Constructor<EntityDef> c = EntityDef.class.getDeclaredConstructor();
        c.setAccessible(true);
        EntityDef d = c.newInstance();
        d.name = "Guard"; d.interactable = true; d.combatLevel = 0;
        d.actions = new String[5]; d.actions[2] = "Talk-to"; d.type = 1234;
        return d;
    }

    @Test public void npcMenuCommandsCarryIndexThenXThenY() throws Exception {
        Game g = game();
        g.itemSelected = 1; g.selectedItemName = "Bread";
        g.buildAtNPCMenu(def(), 321, 55, 44);  // i, j, k
        assertEquals(1, g.menuActionRow);
        assertEquals(582, g.menuActionID[0]);
        assertEquals(321, g.menuActionCmd1[0]);
        assertEquals(44, g.menuActionCmd2[0]);
        assertEquals(55, g.menuActionCmd3[0]);
        Game h = game();
        h.buildAtNPCMenu(def(), 7, 11, 13);
        assertEquals(2, h.menuActionRow);
        assertEquals("Talk-to @yel@Guard", h.menuActionName[0]);
        assertEquals(225, h.menuActionID[0]);
        assertEquals(1025, h.menuActionID[1]);
        for (int r = 0; r < 2; r++) {
            assertEquals(7, h.menuActionCmd1[r]);
            assertEquals(13, h.menuActionCmd2[r]);
            assertEquals(11, h.menuActionCmd3[r]);
        }
    }

    @Test public void npcMenuIgnoredWhenNotInteractableOrMenuFull() throws Exception {
        Game g = game();
        EntityDef d = def(); d.interactable = false;
        g.buildAtNPCMenu(d, 1, 2, 3);
        assertEquals(0, g.menuActionRow);
        g.menuActionRow = 400;
        g.buildAtNPCMenu(def(), 1, 2, 3);
        assertEquals(400, g.menuActionRow);
    }

    @Test public void playerMenuCommandsCarryIndexThenXThenY() throws Exception {
        Game g = game();
        Player p = new Player(); p.name = "Bob";
        g.itemSelected = 1; g.selectedItemName = "Bread";
        g.buildAtPlayerMenu(61, 777, p, 62);  // i, j, k
        assertEquals(1, g.menuActionRow);
        assertEquals(491, g.menuActionID[0]);
        assertEquals(777, g.menuActionCmd1[0]);
        assertEquals(61, g.menuActionCmd2[0]);
        assertEquals(62, g.menuActionCmd3[0]);
        Game h = game();
        h.atPlayerActions[2] = "Follow";
        h.buildAtPlayerMenu(5, 9, p, 17);
        assertEquals(27, h.menuActionID[0]);
        assertEquals(9, h.menuActionCmd1[0]);
        assertEquals(5, h.menuActionCmd2[0]);
        assertEquals(17, h.menuActionCmd3[0]);
        Game m = game();
        Game.myPlayer = p;
        m.atPlayerActions[2] = "Follow";
        m.buildAtPlayerMenu(1, 2, p, 3);
        assertEquals(0, m.menuActionRow);
    }

    @Test public void pushMessageStoresTypeNameMessageAndShiftsOlderEntries() throws Exception {
        Game g = game();
        g.pushMessage("hello", 3, "Alice");
        g.pushMessage("again", 5, "Carol");
        assertEquals(5, g.chatTypes[0]); assertEquals("Carol", g.chatNames[0]); assertEquals("again", g.chatMessages[0]);
        assertEquals(3, g.chatTypes[1]); assertEquals("Alice", g.chatNames[1]); assertEquals("hello", g.chatMessages[1]);
        assertTrue(g.inputTaken);
        for (int n = 0; n < 150; n++) g.pushMessage("m" + n, n, "n" + n);
        assertEquals("m149", g.chatMessages[0]);
        assertEquals("m50", g.chatMessages[99]);
    }

    @Test public void pushMessageTypeZeroCapturesDialogTextOnlyWhenDialogOpen() throws Exception {
        Game g = game();
        g.pushMessage("x", 0, "");
        assertNull(g.aString844);
        set(g, "dialogID", 5);
        g.pushMessage("y", 1, "");
        assertNull(g.aString844);
        g.pushMessage("z", 0, "");
        assertEquals("z", g.aString844);
    }

    // pack bit fields MSB first
    private static byte[] bits(int[][] fields, int size) {
        byte[] b = new byte[size]; int pos = 0;
        for (int[] f : fields)
            for (int i = f[1] - 1; i >= 0; i--, pos++)
                if ((f[0] >> i & 1) != 0) b[pos >> 3] |= 128 >> (pos & 7);
        return b;
    }

    private static Game playerGame() throws Exception {
        Game g = game();
        set(g, "playerArray", new Player[2048]);
        set(g, "playerIndices", new int[2048]);
        set(g, "cachedPlayerAppearances", new Stream[2048]);
        set(g, "maskUpdateIndices", new int[2048]);
        set(g, "loopCycle", 42);
        Game.myPlayer.smallX[0] = 100; Game.myPlayer.smallY[0] = 200;
        return g;
    }

    @Test public void addPlayersReadsIndexUpdateFlagTeleportFlagThenYThenX() throws Exception {
        Game g = playerGame();
        // index 9, update=1, teleport=1, dy=-2 (30), dx=3, terminator
        byte[] data = bits(new int[][]{{9, 11}, {1, 1}, {1, 1}, {30, 5}, {3, 5}, {2047, 11}}, 8);
        Stream s = new Stream(data); s.beginBitReading();
        g.addNewLocalPlayers(s, 8);
        assertEquals(1, g.playerCount);
        assertEquals(9, g.playerIndices[0]);
        Player p = g.playerArray[9];
        assertEquals(42, p.lastUpdateCycle);
        assertEquals(103, p.smallX[0]);
        assertEquals(198, p.smallY[0]);
        assertEquals(103 * 128 + 64, p.x);
        assertEquals(1, g.maskUpdateCount);
        assertEquals(9, g.maskUpdateIndices[0]);
    }

    @Test public void addPlayersWithoutTeleportFlagQueuesWalkStepAndWithoutUpdateFlagSkipsUpdateList() throws Exception {
        Game g = playerGame();
        Player pre = new Player(); pre.smallX[0] = 100; pre.smallY[0] = 200; g.playerArray[12] = pre;
        byte[] data = bits(new int[][]{{12, 11}, {0, 1}, {0, 1}, {29, 5}, {5, 5}, {2047, 11}}, 8);
        Stream s = new Stream(data); s.beginBitReading();
        g.addNewLocalPlayers(s, 8);
        assertSame(pre, g.playerArray[12]);
        assertEquals(0, g.maskUpdateCount);
        assertEquals(105, pre.smallX[0]);   // dx=5
        assertEquals(197, pre.smallY[0]);   // dy=29 -> -3
        assertEquals(100, pre.smallX[1]);   // previous position queued: walk step, not snap
        assertEquals(200, pre.smallY[1]);
    }

    @Test public void addPlayersStopsWhenFewerThanElevenBitsRemainInDeclaredSize() throws Exception {
        Game g = playerGame();
        byte[] data = bits(new int[][]{{9, 11}, {0, 1}, {1, 1}, {0, 5}, {0, 5}, {10, 11}, {0, 1}, {1, 1}, {0, 5}, {0, 5}, {2047, 11}}, 32);
        Stream s = new Stream(data); s.beginBitReading();
        g.addNewLocalPlayers(s, 4);   // 32 bits: first record at bit 0, second at bit 23 (23+10 >= 32)
        assertEquals(1, g.playerCount);
        Game h = playerGame();
        Stream t = new Stream(data); t.beginBitReading();
        h.addNewLocalPlayers(t, 16);
        assertEquals(2, h.playerCount);
    }

    @Test public void formatterAndVolumeSetters() throws Exception {
        Game g = game();
        assertEquals("5", g.interfaceIntToString(5));
        assertEquals("999999998", g.interfaceIntToString(0x3b9ac9ff - 1));
        assertEquals("*", g.interfaceIntToString(0x3b9ac9ff));
        g.setWaveVolume(77);
        assertEquals(77, Signlink.wavevol);
    }
}
