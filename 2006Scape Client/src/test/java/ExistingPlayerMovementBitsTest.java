import static org.junit.Assert.*;
import org.junit.*;

/** Controlled protocol fixtures for the already tracked remote-player list. */
public class ExistingPlayerMovementBitsTest {
    private Game g;
    private int savedCycle;
    private boolean savedReporting;
    private static final int[] DX = {-1, 0, 1, -1, 1, -1, 0, 1};
    private static final int[] DY = {1, 1, 1, 0, 0, -1, -1, -1};

    @Before public void setUp() {
        g = new Game();
        savedCycle = Game.loopCycle;
        savedReporting = Signlink.reporterror;
        Game.loopCycle = 731;
        Signlink.reporterror = false;
        seed(41, 7, 93, 12);
    }
    @After public void tearDown() {
        Game.loopCycle = savedCycle;
        Signlink.reporterror = savedReporting;
    }
    private void seed(int... ids) {
        g.playerCount = ids.length;
        g.anInt839 = g.maskUpdateCount = 0;
        for (int n = 0; n < ids.length; n++) {
            g.playerIndices[n] = ids[n];
            Player p = new Player();
            p.setPos(50 + n, 60 + n, true);
            p.anInt1537 = -1;
            g.playerArray[ids[n]] = p;
        }
    }
    static Stream bits(int... fields) {
        int size = 0;
        for (int n = 0; n < fields.length; n += 2) size += fields[n];
        byte[] b = new byte[(size + 7) / 8];
        int pos = 0;
        for (int n = 0; n < fields.length; n += 2)
            for (int bit = fields[n] - 1; bit >= 0; bit--, pos++)
                if (((fields[n + 1] >>> bit) & 1) != 0) b[pos / 8] |= 1 << (7 - pos % 8);
        return new Stream(b);
    }
    private Stream apply(int... fields) {
        Stream s = bits(fields);
        g.decodeExistingPlayerMovement(s);
        return s;
    }
    private void list(int... ids) {
        assertEquals(ids.length, g.playerCount);
        for (int n = 0; n < ids.length; n++) assertEquals(ids[n], g.playerIndices[n]);
    }
    @Test public void zeroAndShortCountsQueueOnlyTheOldTailInOrder() {
        Stream s = apply(8, 2, 1, 0, 1, 0);
        list(41, 7);
        assertEquals(10, s.bitOffset);
        assertEquals(2, g.anInt839);
        assertEquals(93, g.anIntArray840[0]);
        assertEquals(12, g.anIntArray840[1]);
        assertEquals(731, g.playerArray[41].anInt1537);
        assertEquals(731, g.playerArray[7].anInt1537);
        assertEquals(-1, g.playerArray[93].anInt1537);
        assertEquals(0, g.maskUpdateCount);
        seed(41, 7, 93, 12);
        s = apply(8, 0, 1, 1);
        list();
        assertEquals(8, s.bitOffset);
        assertEquals(4, g.anInt839);
        assertArrayEquals(new int[]{41, 7, 93, 12}, java.util.Arrays.copyOf(g.anIntArray840, 4));
    }
    @Test public void oversizedCountRejectsBeforeRebuildingList() {
        Stream s = bits(8, 5);
        try { g.decodeExistingPlayerMovement(s); fail("expected invalid-count rejection"); }
        catch (RuntimeException expected) { assertEquals("eek", expected.getMessage()); }
        list(41, 7, 93, 12);
        assertEquals(8, s.bitOffset);
        assertEquals(0, g.anInt839);
    }
    @Test public void absentChangeConsumesOneBitAndDoesNotReadFollowingKind() {
        seed(93);
        Player p = g.playerArray[93];
        Stream s = apply(8, 1, 1, 0, 2, 3);
        list(93);
        assertEquals(9, s.bitOffset);
        assertEquals(50, p.smallX[0]);
        assertEquals(60, p.smallY[0]);
        assertEquals(731, p.anInt1537);
        assertEquals(0, g.maskUpdateCount);
        assertEquals(0, g.anInt839);
    }
    @Test public void kindZeroAddsMaskWithoutMovingAndAppendsToExistingQueue() {
        seed(7);
        g.maskUpdateIndices[0] = 321;
        g.maskUpdateCount = 1;
        Stream s = apply(8, 1, 1, 1, 2, 0);
        list(7);
        assertEquals(11, s.bitOffset);
        assertEquals(2, g.maskUpdateCount);
        assertEquals(321, g.maskUpdateIndices[0]);
        assertEquals(7, g.maskUpdateIndices[1]);
        assertEquals(50, g.playerArray[7].smallX[0]);
        assertEquals(60, g.playerArray[7].smallY[0]);
        assertEquals(731, g.playerArray[7].anInt1537);
        assertEquals(0, g.anInt839);
    }
    @Test public void allWalkDirectionsAndBothMaskValuesAreIndependent() {
        for (int d = 0; d < 8; d++) for (int b = 0; b < 2; b++) {
            seed(93);
            Player p = g.playerArray[93];
            Stream s = apply(8, 1, 1, 1, 2, 1, 3, d, 1, b);
            list(93);
            assertEquals(15, s.bitOffset);
            assertEquals(50 + DX[d], p.smallX[0]);
            assertEquals(60 + DY[d], p.smallY[0]);
            assertEquals(50, p.smallX[1]);
            assertEquals(60, p.smallY[1]);
            assertFalse(p.aBooleanArray1553[0]);
            assertEquals(731, p.anInt1537);
            assertEquals(b, g.maskUpdateCount);
            if (b == 1) assertEquals(93, g.maskUpdateIndices[0]);
            assertEquals(0, g.anInt839);
        }
    }
    @Test public void allRunPairsPreserveFirstStepInHistoryAndSecondAtHead() {
        for (int a = 0; a < 8; a++) for (int b = 0; b < 8; b++) for (int c = 0; c < 2; c++) {
            seed(12);
            Player p = g.playerArray[12];
            Stream s = apply(8, 1, 1, 1, 2, 2, 3, a, 3, b, 1, c);
            list(12);
            assertEquals(18, s.bitOffset);
            assertEquals(50 + DX[a], p.smallX[1]);
            assertEquals(60 + DY[a], p.smallY[1]);
            assertEquals(50 + DX[a] + DX[b], p.smallX[0]);
            assertEquals(60 + DY[a] + DY[b], p.smallY[0]);
            assertTrue(p.aBooleanArray1553[0]);
            assertTrue(p.aBooleanArray1553[1]);
            assertEquals(731, p.anInt1537);
            assertEquals(c, g.maskUpdateCount);
            if (c == 1) assertEquals(12, g.maskUpdateIndices[0]);
            assertEquals(0, g.anInt839);
        }
    }
    @Test public void removalsCompactSlotsWithoutConfusingSlotsWithPlayerIds() {
        g.anIntArray840[0] = 305;
        g.anInt839 = 1;
        Player p = g.playerArray[7];
        Stream s = apply(8, 3, 1, 1, 2, 3, 1, 1, 2, 1, 3, 4, 1, 1, 1, 0);
        list(7, 93);
        assertEquals(19, s.bitOffset);
        assertEquals(3, g.anInt839);
        assertArrayEquals(new int[]{305, 12, 41}, java.util.Arrays.copyOf(g.anIntArray840, 3));
        assertEquals(-1, g.playerArray[41].anInt1537);
        assertEquals(-1, g.playerArray[12].anInt1537);
        assertEquals(731, g.playerArray[7].anInt1537);
        assertEquals(731, g.playerArray[93].anInt1537);
        assertEquals(52, p.smallX[0]);
        assertEquals(61, p.smallY[0]);
        assertEquals(1, g.maskUpdateCount);
        assertEquals(7, g.maskUpdateIndices[0]);
        assertNotNull(g.playerArray[41]); // Actual deletion belongs to updatePlayers.
    }
    @Test public void countUsesAllEightBitsAndStopsAt255Entries() {
        int[] ids = new int[255];
        for (int n = 0; n < ids.length; n++) ids[n] = 256 + n;
        seed(ids);
        Stream s = new Stream(new byte[33]);
        s.buffer[0] = (byte) 255;
        g.decodeExistingPlayerMovement(s);
        list(ids);
        assertEquals(263, s.bitOffset);
        assertEquals(731, g.playerArray[510].anInt1537);
        assertEquals(0, g.anInt839);
        assertEquals(0, g.maskUpdateCount);
    }
}
