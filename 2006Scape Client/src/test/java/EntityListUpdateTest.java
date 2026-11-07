import static org.junit.Assert.*;
import org.junit.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

/** Whole-packet fixtures for updateNPCs / updatePlayers: the shared removal list, size check and list validation. */
public class EntityListUpdateTest {
    private int savedCycle;
    private boolean savedReporting;
    private Player savedPlayer;

    @Before public void setUp() {
        savedCycle = Game.loopCycle;
        savedReporting = Signlink.reporterror;
        savedPlayer = Game.myPlayer;
        Game.loopCycle = 731;
        Signlink.reporterror = false;
        Game.myPlayer = new Player();
        Game.myPlayer.setPos(50, 60, true);
    }
    @After public void tearDown() {
        Game.loopCycle = savedCycle;
        Signlink.reporterror = savedReporting;
        Game.myPlayer = savedPlayer;
    }

    private static Stream bits(int... fields) { return ExistingNpcMovementBitsTest.bits(fields); }

    private static EntityDef def(int type) throws Exception {
        Constructor<EntityDef> c = EntityDef.class.getDeclaredConstructor(); c.setAccessible(true);
        EntityDef d = c.newInstance(); d.type = type; return d;
    }

    private static void seedNpcs(Game g, int... ids) throws Exception {
        g.npcCount = ids.length;
        for (int n = 0; n < ids.length; n++) {
            g.npcIndices[n] = ids[n];
            NPC p = new NPC(); p.setPos(50 + n, 60 + n, true); p.lastUpdateCycle = -1; p.desc = def(500 + n);
            g.npcArray[ids[n]] = p;
        }
    }
    private static void seedPlayers(Game g, int... ids) {
        g.playerCount = ids.length;
        for (int n = 0; n < ids.length; n++) {
            g.playerIndices[n] = ids[n];
            Player p = new Player(); p.setPos(50 + n, 60 + n, true); p.lastUpdateCycle = -1;
            g.playerArray[ids[n]] = p;
        }
    }

    // ---- updateNPCs ----

    @Test public void npcTailRemovalNullsQueuedSlotsAndClearsTheirDefinition() throws Exception {
        Game g = new Game(); seedNpcs(g, 41, 7, 93, 12);
        NPC removed93 = g.npcArray[93], kept41 = g.npcArray[41];
        Stream s = bits(8, 2, 1, 0, 1, 0); // keep 2 of 4, both without update
        g.updateNPCs(s, 2);
        assertEquals(2, g.removedEntityCount);                         // count of queued removals
        assertEquals(93, g.removedEntityIndices[0]);                // slot ids, not list positions 2/3
        assertEquals(12, g.removedEntityIndices[1]);
        assertNull(g.npcArray[93]); assertNull(g.npcArray[12]);
        assertNull(removed93.desc);                          // l is the npcArray slot whose NPC is cleared
        assertSame(kept41, g.npcArray[41]); assertNotNull(kept41.desc);
        assertNull(g.npcArray[2]); assertNull(g.npcArray[0]); // positions are not nulled
        // the stale list position npcIndices[2] (=93, now null) lies beyond npcCount and is not validated
        assertEquals(2, g.npcCount); assertEquals(93, g.npcIndices[2]);
    }

    @Test public void npcRemovalCountIsResetSoStaleEntriesAreIgnored() throws Exception {
        Game g = new Game(); seedNpcs(g, 41, 7, 93, 12);
        NPC unlisted = new NPC(); unlisted.lastUpdateCycle = -1; g.npcArray[300] = unlisted;
        g.removedEntityIndices[0] = 300; g.removedEntityIndices[1] = 300; g.removedEntityCount = 2; // left from an earlier packet
        g.updateNPCs(bits(8, 4, 1, 0, 1, 0, 1, 0, 1, 0), 2);
        assertEquals(0, g.removedEntityCount);
        assertSame(unlisted, g.npcArray[300]);
    }

    @Test public void npcQueuedButRefreshedInSamePacketSurvives() throws Exception {
        Field cf = EntityDef.class.getDeclaredField("cache"); cf.setAccessible(true); Object savedCache = cf.get(null);
        try {
            EntityDef[] cache = new EntityDef[20];
            for (int n = 0; n < 20; n++) cache[n] = def(1000 + n);
            cache[0] = def(7);
            cf.set(null, cache);
            Game g = new Game(); seedNpcs(g, 41, 7, 93, 12);
            NPC npc93 = g.npcArray[93];
            // keep 3: 41 idle, 7 idle, 93 explicit removal (1,3); tail 12 queued; then add 93 again
            Stream s = bits(8, 3, 1, 0, 1, 0, 1, 1, 2, 3,
                            14, 93, 5, 3, 5, 30, 1, 1, 12, 7, 1, 0);
            g.updateNPCs(s, 7);
            assertEquals(2, g.removedEntityCount);
            assertEquals(12, g.removedEntityIndices[0]); // tail first
            assertEquals(93, g.removedEntityIndices[1]); // then explicit removal
            assertNull(g.npcArray[12]);
            assertSame(npc93, g.npcArray[93]);    // queued, but anInt1537 == loopCycle after re-add
            assertEquals(3, g.npcCount); assertEquals(93, g.npcIndices[2]);
        } finally { cf.set(null, savedCache); }
    }

    @Test public void npcPacketSizeMustEqualFinalOffset() throws Exception {
        Game g = new Game(); seedNpcs(g, 41, 7, 93, 12);
        try { g.updateNPCs(bits(8, 2, 1, 0, 1, 0), 1); fail("size 1 with offset 2 must throw"); }
        catch (RuntimeException e) { assertEquals("eek", e.getMessage()); }
        Game h = new Game(); seedNpcs(h, 41, 7, 93, 12);
        try { h.updateNPCs(bits(8, 2, 1, 0, 1, 0), 3); fail("size 3 with offset 2 must throw"); }
        catch (RuntimeException e) { assertEquals("eek", e.getMessage()); }
    }

    @Test public void npcListValidationCoversExactlyTheListedPositions() throws Exception {
        final int[] nullSlot = {7};
        Game g = new Game() {
            @Override public void decodeNpcUpdateMasks(Stream packet) { super.decodeNpcUpdateMasks(packet); npcArray[nullSlot[0]] = null; }
        };
        seedNpcs(g, 41, 7, 93, 12);
        try { g.updateNPCs(bits(8, 2, 1, 0, 1, 0), 2); fail("listed null slot at position 1 must throw"); }
        catch (RuntimeException e) { assertEquals("eek", e.getMessage()); }
        // counterexample: a null slot that is not listed (position >= npcCount) passes
        nullSlot[0] = 600;
        Game h = new Game() {
            @Override public void decodeNpcUpdateMasks(Stream packet) { super.decodeNpcUpdateMasks(packet); npcArray[nullSlot[0]] = null; npcIndices[2] = 600; }
        };
        seedNpcs(h, 41, 7, 93, 12);
        h.updateNPCs(bits(8, 2, 1, 0, 1, 0), 2);
        assertNotNull(h.npcArray[41]); assertNotNull(h.npcArray[7]);
    }

    // ---- updatePlayers ----

    @Test public void playerTailRemovalNullsQueuedSlots() {
        Game g = new Game(); seedPlayers(g, 41, 7, 93, 12);
        Player kept7 = g.playerArray[7];
        g.updatePlayers(2, bits(1, 0, 8, 2, 1, 0, 1, 0)); // no local move; keep 2 of 4
        assertEquals(2, g.removedEntityCount);
        assertEquals(93, g.removedEntityIndices[0]); assertEquals(12, g.removedEntityIndices[1]);
        assertNull(g.playerArray[93]); assertNull(g.playerArray[12]);
        assertSame(kept7, g.playerArray[7]);
        assertEquals(2, g.playerCount); assertEquals(93, g.playerIndices[2]);
    }

    @Test public void playerRemovalCountIsResetSoStaleEntriesAreIgnored() {
        Game g = new Game(); seedPlayers(g, 41, 7, 93, 12);
        Player unlisted = new Player(); unlisted.lastUpdateCycle = -1; g.playerArray[300] = unlisted;
        g.removedEntityIndices[0] = 300; g.removedEntityCount = 1;
        g.updatePlayers(2, bits(1, 0, 8, 4, 1, 0, 1, 0, 1, 0, 1, 0));
        assertEquals(0, g.removedEntityCount);
        assertSame(unlisted, g.playerArray[300]);
    }

    @Test public void playerQueuedButRefreshedInSamePacketSurvives() {
        Game g = new Game(); seedPlayers(g, 41, 7, 93, 12);
        Player p93 = g.playerArray[93];
        Stream s = bits(1, 0, 8, 3, 1, 0, 1, 0, 1, 1, 2, 3,
                        11, 93, 1, 0, 1, 1, 5, 3, 5, 30);
        g.updatePlayers(5, s);
        assertEquals(2, g.removedEntityCount);
        assertEquals(12, g.removedEntityIndices[0]); assertEquals(93, g.removedEntityIndices[1]);
        assertNull(g.playerArray[12]);
        assertSame(p93, g.playerArray[93]);
        assertEquals(3, g.playerCount);
    }

    @Test public void playerPacketSizeMustEqualFinalOffset() {
        Game g = new Game(); seedPlayers(g, 41, 7, 93, 12);
        try { g.updatePlayers(1, bits(1, 0, 8, 2, 1, 0, 1, 0)); fail("size 1 with offset 2 must throw"); }
        catch (RuntimeException e) { assertEquals("eek", e.getMessage()); }
    }

    @Test public void playerListValidationCoversExactlyTheListedPositions() {
        Game g = new Game() {
            @Override public void decodePlayerUpdateMasks(Stream stream) { super.decodePlayerUpdateMasks(stream); playerArray[41] = null; }
        };
        seedPlayers(g, 41, 7, 93, 12);
        try { g.updatePlayers(2, bits(1, 0, 8, 2, 1, 0, 1, 0)); fail("listed null slot at position 0 must throw"); }
        catch (RuntimeException e) { assertEquals("eek", e.getMessage()); }
        Game h = new Game() {
            @Override public void decodePlayerUpdateMasks(Stream stream) { super.decodePlayerUpdateMasks(stream); playerArray[600] = null; playerIndices[2] = 600; }
        };
        seedPlayers(h, 41, 7, 93, 12);
        h.updatePlayers(2, bits(1, 0, 8, 2, 1, 0, 1, 0));
        assertNotNull(h.playerArray[41]);
    }
}
