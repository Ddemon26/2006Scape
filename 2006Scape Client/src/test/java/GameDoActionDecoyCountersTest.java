import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Original-name observations for the counters Game.doAction keeps next to the packets it sends (challengeDecoyCounter, attackPlayerDecoyCounter, npcSecondClickDecoyCounter,
 * npcThirdClickDecoyCounter, npcFifthOptionDecoyCounter, itemThirdOptionDecoyCounter, objectFifthClickDecoyCounter) and for the selected-spell component selectedSpellInterfaceId. Every packet is decoded from the
 * real ISAAC-encrypted stream, so the opcode order, the payload bytes and the counter value after each click are asserted.
 */
public class GameDoActionDecoyCountersTest {
    private static final int[] SEED = {9, 8, 7, 6};
    private static final int[] SIZE = new int[256];
    static {
        Arrays.fill(SIZE, -1);
        SIZE[136] = 0; SIZE[183] = 3; SIZE[189] = 1; SIZE[230] = 1; SIZE[152] = 1; SIZE[200] = 2; SIZE[85] = 1;   // silent packets
        SIZE[128] = 2; SIZE[73] = 2; SIZE[17] = 2; SIZE[21] = 2; SIZE[18] = 2; SIZE[43] = 6; SIZE[228] = 6; SIZE[139] = 2;
        SIZE[130] = 0; SIZE[155] = 2; SIZE[237] = 8; SIZE[131] = 4; SIZE[249] = 4; SIZE[35] = 8; SIZE[181] = 8;
    }

    /** doWalkTo and walkToSceneObject are replaced so the stream holds only the packets of the code under test. */
    public static class DoActionGame extends Game {
        int walks;
        @Override public boolean doWalkTo(int a, int b, int c, int d, int e, int f, int g, int h, int i, boolean j, int k) { walks++; return true; }
        @Override public boolean walkToSceneObject(int a, int b, int c) { walks++; return true; }
    }

    private Player savedPlayer;
    private int[] savedCounters;
    private RSInterface[] savedCache;
    private DoActionGame g;
    private ISAACRandomGen decoder;
    private List<int[]> lastSent = new ArrayList<int[]>();

    @Before public void before() {
        savedPlayer = Game.myPlayer;
        savedCounters = new int[]{Game.challengeDecoyCounter, Game.attackPlayerDecoyCounter, Game.npcSecondClickDecoyCounter, Game.npcThirdClickDecoyCounter, Game.npcFifthOptionDecoyCounter, Game.itemThirdOptionDecoyCounter, Game.objectFifthClickDecoyCounter};
        savedCache = RSInterface.interfaceCache;
        RSInterface.interfaceCache = new RSInterface[4000];
        Game.myPlayer = new Player();
        g = new DoActionGame();
        g.stream = new Stream(new byte[5000]);
        g.stream.encryption = new ISAACRandomGen(SEED);
        g.playerArray = new Player[2048];
        g.npcArray = new NPC[16384];
        g.playerIndices = new int[2048];
        g.baseX = 3200; g.baseY = 3100;
        zeroCounters();
        decoder = new ISAACRandomGen(SEED);
    }
    @After public void after() {
        Game.myPlayer = savedPlayer;
        Game.challengeDecoyCounter = savedCounters[0]; Game.attackPlayerDecoyCounter = savedCounters[1]; Game.npcSecondClickDecoyCounter = savedCounters[2];
        Game.npcThirdClickDecoyCounter = savedCounters[3]; Game.npcFifthOptionDecoyCounter = savedCounters[4]; Game.itemThirdOptionDecoyCounter = savedCounters[5]; Game.objectFifthClickDecoyCounter = savedCounters[6];
        RSInterface.interfaceCache = savedCache;
    }
    private static void zeroCounters() {
        Game.challengeDecoyCounter = Game.attackPlayerDecoyCounter = Game.npcSecondClickDecoyCounter = Game.npcThirdClickDecoyCounter = Game.npcFifthOptionDecoyCounter = Game.itemThirdOptionDecoyCounter = Game.objectFifthClickDecoyCounter = 0;
    }

    private static int[] be(int v) { return new int[]{(v >> 8) & 255, v & 255}; }
    private static int[] le(int v) { return new int[]{v & 255, (v >> 8) & 255}; }
    private static int[] beAdd(int v) { return new int[]{(v >> 8) & 255, (v + 128) & 255}; }
    private static int[] leAdd(int v) { return new int[]{(v + 128) & 255, (v >> 8) & 255}; }

    /** Decodes everything written to the stream so far: each entry is {opcode, payload bytes...}. */
    private List<int[]> sent() {

        List<int[]> out = new ArrayList<int[]>();
        int p = 0;
        while (p < g.stream.currentOffset) {
            int op = ((g.stream.buffer[p++] & 255) - decoder.getNextKey()) & 255;
            int n = SIZE[op];
            assertTrue("unexpected opcode " + op, n >= 0);
            int[] r = new int[n + 1];
            r[0] = op;
            for (int i = 0; i < n; i++) r[i + 1] = g.stream.buffer[p++] & 255;
            out.add(r);
        }
        return out;
    }
    private static int[] pkt(int op, int[]... parts) {
        int n = 1;
        for (int[] x : parts) n += x.length;
        int[] r = new int[n];
        r[0] = op;
        int p = 1;
        for (int[] x : parts) for (int v : x) r[p++] = v;
        return r;
    }
    private void assertSent(int[]... expected) {
        List<int[]> got = lastSent;
        assertEquals("packet count " + describe(got), expected.length, got.size());
        for (int i = 0; i < expected.length; i++) assertArrayEquals("packet " + i + " of " + describe(got), expected[i], got.get(i));
    }
    private static String describe(List<int[]> l) {
        StringBuilder s = new StringBuilder();
        for (int[] a : l) s.append(Arrays.toString(a));
        return s.toString();
    }
    private void act(int id, int cmd1, int cmd2, int cmd3) {
        g.menuActionID[0] = id; g.menuActionCmd1[0] = cmd1; g.menuActionCmd2[0] = cmd2; g.menuActionCmd3[0] = cmd3;
        g.stream.currentOffset = 0;
        g.doAction(0);
        lastSent = sent();
    }
    private void player(int index) { g.playerArray[index] = new Player(); }
    private void npc(int index) { g.npcArray[index] = new NPC(); }
    private void item(int component) { RSInterface r = new RSInterface(); r.parentID = 3000; RSInterface.interfaceCache[component] = r; }

    // ---------- challengeDecoyCounter: action 561 (and 6) -> packet 128 ----------
    @Test public void challengeCounterAddsPlayerIndexAndFiresSilent136AtNinety() {
        player(45);
        act(561, 45, 0, 0);
        assertEquals(45, Game.challengeDecoyCounter);
        assertSent(pkt(128, be(45)));
        act(561, 45, 0, 0);                                   // 45 + 45 = 90: reaches the threshold
        assertEquals(0, Game.challengeDecoyCounter);
        assertSent(pkt(136), pkt(128, be(45)));               // silent packet first, then the real one
        act(561, 45, 0, 0);
        assertEquals(45, Game.challengeDecoyCounter);                     // restarts from zero
        assertSent(pkt(128, be(45)));
    }
    @Test public void challengeCounterBoundariesAndCounterexamples() {
        player(0); player(1); player(300);
        Game.challengeDecoyCounter = 89;
        act(561, 0, 0, 0);                                    // adds the index (0), not 1
        assertEquals(89, Game.challengeDecoyCounter);
        assertSent(pkt(128, be(0)));
        act(561, 1, 0, 0);                                    // 89 + 1 = 90
        assertEquals(0, Game.challengeDecoyCounter);
        assertSent(pkt(136), pkt(128, be(1)));
        Game.challengeDecoyCounter = 0;
        act(561, 300, 0, 0);                                  // one large index crosses at once
        assertEquals(0, Game.challengeDecoyCounter);
        assertSent(pkt(136), pkt(128, be(300)));
        Game.challengeDecoyCounter = 10;
        act(561, 7, 0, 0);                                    // no player in the slot: nothing happens
        assertEquals(10, Game.challengeDecoyCounter);
        assertSent();
    }
    @Test public void acceptChallengeAddsMenuCmd1NotThePlayerIndex() {
        Player other = new Player(); other.name = "Bob"; g.playerArray[7] = other;
        g.playerCount = 1; g.playerIndices[0] = 7;
        g.menuActionName[0] = "Accept challenge @whi@Bob";
        act(6, 50, 0, 0);
        assertEquals(50, Game.challengeDecoyCounter);                     // cmd1 (50), not the player index (7)
        assertSent(pkt(128, be(7)));                          // the packet carries the player index
        act(6, 40, 0, 0);
        assertEquals(0, Game.challengeDecoyCounter);
        assertSent(pkt(136), pkt(128, be(7)));
        Game.challengeDecoyCounter = 5;
        g.menuActionName[0] = "Accept trade @whi@Bob";
        act(484, 99, 0, 0);                                   // accept trade sends 139 and leaves the counter alone
        assertEquals(5, Game.challengeDecoyCounter);
        assertSent(pkt(139, le(7)));
    }

    // ---------- attackPlayerDecoyCounter: action 27 -> packet 73 ----------
    @Test public void attackPlayerCounterThresholdFiftyFour() {
        player(27); player(0); player(1); player(54);
        act(27, 27, 0, 0);
        assertEquals(27, Game.attackPlayerDecoyCounter);
        assertSent(pkt(73, le(27)));
        act(27, 27, 0, 0);
        assertEquals(0, Game.attackPlayerDecoyCounter);
        assertSent(pkt(189, new int[]{234}), pkt(73, le(27)));
        Game.attackPlayerDecoyCounter = 53;
        act(27, 0, 0, 0);
        assertEquals(53, Game.attackPlayerDecoyCounter);
        assertSent(pkt(73, le(0)));
        act(27, 1, 0, 0);
        assertEquals(0, Game.attackPlayerDecoyCounter);
        assertSent(pkt(189, new int[]{234}), pkt(73, le(1)));
        act(27, 54, 0, 0);
        assertEquals(0, Game.attackPlayerDecoyCounter);
        assertSent(pkt(189, new int[]{234}), pkt(73, le(54)));
        Game.challengeDecoyCounter = 3;                                   // the other player counter is not shared
        act(27, 5, 0, 0);                                     // empty slot: nothing
        assertEquals(0, Game.attackPlayerDecoyCounter);
        assertEquals(3, Game.challengeDecoyCounter);
    }

    // ---------- npcSecondClickDecoyCounter: action 225 -> packet 17 ----------
    @Test public void npcSecondClickCounterThresholdEightyFive() {
        npc(85); npc(40); npc(84); npc(1);
        act(225, 40, 0, 0);
        assertEquals(40, Game.npcSecondClickDecoyCounter);
        assertSent(pkt(17, leAdd(40)));
        act(225, 40, 0, 0);
        assertEquals(80, Game.npcSecondClickDecoyCounter);
        assertSent(pkt(17, leAdd(40)));
        act(225, 1, 0, 0);
        assertEquals(81, Game.npcSecondClickDecoyCounter);
        Game.npcSecondClickDecoyCounter = 84;
        act(225, 84, 0, 0);                                   // 84 + 84 = 168 >= 85
        assertEquals(0, Game.npcSecondClickDecoyCounter);
        assertSent(pkt(230, new int[]{239}), pkt(17, leAdd(84)));
        Game.npcSecondClickDecoyCounter = 0;
        act(225, 85, 0, 0);
        assertEquals(0, Game.npcSecondClickDecoyCounter);
        assertSent(pkt(230, new int[]{239}), pkt(17, leAdd(85)));
    }

    // ---------- npcThirdClickDecoyCounter: action 965 -> packet 21 ----------
    @Test public void npcThirdClickCounterCountsClicksNotIndices() {
        npc(0); npc(250);
        act(965, 250, 0, 0);
        assertEquals(1, Game.npcThirdClickDecoyCounter);                      // +1, whatever the index
        assertSent(pkt(21, be(250)));
        act(965, 0, 0, 0);
        assertEquals(2, Game.npcThirdClickDecoyCounter);
        Game.npcThirdClickDecoyCounter = 94;
        act(965, 250, 0, 0);
        assertEquals(95, Game.npcThirdClickDecoyCounter);
        assertSent(pkt(21, be(250)));
        act(965, 250, 0, 0);
        assertEquals(0, Game.npcThirdClickDecoyCounter);                      // 96th click: silent packet, reset
        assertSent(pkt(152, new int[]{88}), pkt(21, be(250)));
        act(965, 250, 0, 0);
        assertEquals(1, Game.npcThirdClickDecoyCounter);
    }

    // ---------- npcFifthOptionDecoyCounter: action 478 -> packet 18 ----------
    @Test public void npcFifthOptionCounterOnlyCountsIndicesDivisibleByFour() {
        npc(4); npc(5); npc(0); npc(7); npc(8);
        act(478, 5, 0, 0);
        assertEquals(0, Game.npcFifthOptionDecoyCounter);                      // index & 3 != 0: not counted
        assertSent(pkt(18, le(5)));
        act(478, 7, 0, 0);
        assertEquals(0, Game.npcFifthOptionDecoyCounter);
        act(478, 4, 0, 0);
        assertEquals(1, Game.npcFifthOptionDecoyCounter);
        act(478, 0, 0, 0);
        assertEquals(2, Game.npcFifthOptionDecoyCounter);
        act(478, 8, 0, 0);
        assertEquals(3, Game.npcFifthOptionDecoyCounter);
        Game.npcFifthOptionDecoyCounter = 52;
        act(478, 5, 0, 0);
        assertEquals(52, Game.npcFifthOptionDecoyCounter);
        assertSent(pkt(18, le(5)));
        act(478, 4, 0, 0);                                    // 53rd counted click
        assertEquals(0, Game.npcFifthOptionDecoyCounter);
        assertSent(pkt(85, new int[]{66}), pkt(18, le(4)));
        Game.npcFifthOptionDecoyCounter = 53;                                  // the threshold test is outside the divisibility test
        act(478, 5, 0, 0);
        assertEquals(0, Game.npcFifthOptionDecoyCounter);
        assertSent(pkt(85, new int[]{66}), pkt(18, le(5)));
    }

    // ---------- itemThirdOptionDecoyCounter: action 867 -> packet 43 ----------
    @Test public void itemThirdOptionCounterOnlyCountsItemIdsDivisibleByFour() {
        item(3214);
        act(867, 1511, 4, 3214);                              // 1511 & 3 == 3: not counted
        assertEquals(0, Game.itemThirdOptionDecoyCounter);
        assertSent(pkt(43, le(3214), beAdd(1511), beAdd(4)));
        act(867, 1512, 4, 3214);                              // 1512 & 3 == 0
        assertEquals(1, Game.itemThirdOptionDecoyCounter);
        act(867, 4, 4, 3214);
        assertEquals(2, Game.itemThirdOptionDecoyCounter);
        Game.itemThirdOptionDecoyCounter = 58;
        act(867, 1511, 4, 3214);
        assertEquals(58, Game.itemThirdOptionDecoyCounter);
        assertSent(pkt(43, le(3214), beAdd(1511), beAdd(4)));
        act(867, 1512, 4, 3214);                              // 59
        assertEquals(0, Game.itemThirdOptionDecoyCounter);
        assertSent(pkt(200, be(25501)), pkt(43, le(3214), beAdd(1512), beAdd(4)));
        Game.itemThirdOptionDecoyCounter = 59;                                  // threshold is tested even when the item is not counted
        act(867, 1511, 4, 3214);
        assertEquals(0, Game.itemThirdOptionDecoyCounter);
        assertSent(pkt(200, be(25501)), pkt(43, le(3214), beAdd(1511), beAdd(4)));
    }

    // ---------- objectFifthClickDecoyCounter: action 1062 -> packet 228 ----------
    @Test public void objectFifthClickCounterAddsRegionBaseX() {
        int uid = (1234 << 14) | 7;
        g.baseX = 50;
        act(1062, uid, 10, 20);
        assertEquals(50, Game.objectFifthClickDecoyCounter);
        assertSent(pkt(228, beAdd(1234), beAdd(20 + 3100), be(10 + 50)));
        act(1062, uid, 10, 20);
        assertEquals(100, Game.objectFifthClickDecoyCounter);
        assertSent(pkt(228, beAdd(1234), beAdd(20 + 3100), be(10 + 50)));
        act(1062, uid, 10, 20);                               // 150 >= 113
        assertEquals(0, Game.objectFifthClickDecoyCounter);
        assertSent(pkt(183, new int[]{0xe6, 0x32, 0x71}), pkt(228, beAdd(1234), beAdd(20 + 3100), be(10 + 50)));
        g.baseX = 112;
        act(1062, uid, 10, 20);                               // exactly below the threshold
        assertEquals(112, Game.objectFifthClickDecoyCounter);
        assertSent(pkt(228, beAdd(1234), beAdd(20 + 3100), be(10 + 112)));
        Game.objectFifthClickDecoyCounter = 0; g.baseX = 113;
        act(1062, uid, 10, 20);                               // exactly at the threshold
        assertEquals(0, Game.objectFifthClickDecoyCounter);
        assertSent(pkt(183, new int[]{0xe6, 0x32, 0x71}), pkt(228, beAdd(1234), beAdd(20 + 3100), be(10 + 113)));
        Game.objectFifthClickDecoyCounter = 0; g.baseX = 3200; g.baseY = 5;       // baseY is not what is added
        act(1062, uid, 10, 20);
        assertEquals(0, Game.objectFifthClickDecoyCounter);
        g.baseX = 0; g.baseY = 3000;
        act(1062, uid, 10, 20);
        assertEquals(0, Game.objectFifthClickDecoyCounter);
        assertSent(pkt(228, beAdd(1234), beAdd(20 + 3000), be(10)));
    }

    // ---------- selectedSpellInterfaceId: set by action 626, sent by 543, 413, 365, 956 and 94 ----------
    private void spell(int component, int usableOn) {
        RSInterface r = new RSInterface();
        r.selectedActionName = "Cast"; r.spellName = "Test spell"; r.spellUsableOn = usableOn;
        RSInterface.interfaceCache[component] = r;
        act(626, 0, 0, component);
    }
    @Test public void selectedSpellComponentIsStoredByAction626AndSentByTheSpellActions() {
        item(3214); npc(9); player(11);
        assertEquals(0, g.selectedSpellInterfaceId);
        spell(1162, 16);
        assertEquals(1162, g.selectedSpellInterfaceId);
        assertEquals(1, g.spellSelected);
        assertSent();                                          // selecting sends nothing
        act(543, 1511, 4, 3214);                               // spell on inventory item
        assertSent(pkt(237, be(4), beAdd(1511), be(3214), beAdd(1162)));
        assertEquals(1162, g.selectedSpellInterfaceId);                       // spellSelected is cleared, the component stays
        assertEquals(0, g.spellSelected);
        spell(1152, 2);
        assertEquals(1152, g.selectedSpellInterfaceId);
        act(413, 9, 0, 0);                                     // spell on npc
        assertSent(pkt(131, leAdd(9), beAdd(1152)));
        spell(1153, 8);
        act(365, 11, 0, 0);                                    // spell on player
        assertSent(pkt(249, beAdd(11), le(1153)));
        spell(1179, 4);
        act(956, (2151 << 14), 5, 6);                          // spell on scene object
        assertSent(pkt(35, le(5 + 3200), beAdd(1179), beAdd(6 + 3100), le(2151)));
        spell(1168, 1);
        act(94, 1511, 5, 6);                                   // spell on ground item
        assertSent(pkt(181, le(6 + 3100), be(1511), le(5 + 3200), beAdd(1168)));
        assertEquals(1168, g.selectedSpellInterfaceId);
    }
    @Test public void otherActionsLeaveSelectedSpellComponentAlone() {
        item(3214); npc(9);
        spell(1162, 16);
        act(200, 0, 0, 0);                                     // close-interfaces action
        assertEquals(1162, g.selectedSpellInterfaceId);
        act(225, 9, 0, 0);
        act(20, 9, 0, 0);
        assertEquals(1162, g.selectedSpellInterfaceId);
        g.stream.currentOffset = 0;
        spell(7, 2);                                           // a later selection replaces it
        assertEquals(7, g.selectedSpellInterfaceId);
    }
}
