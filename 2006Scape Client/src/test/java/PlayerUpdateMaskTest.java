import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import org.junit.Test;
import static org.junit.Assert.*;

public class PlayerUpdateMaskTest {
    private static Game game() throws Exception {
        Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (Game) ((sun.misc.Unsafe) field.get(null)).allocateInstance(Game.class);
    }

    private static void field(Game game, String name, Object value) throws Exception {
        Field field = Game.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(game, value);
    }

    private static Stream packet(int... values) {
        byte[] bytes = new byte[values.length];
        for (int index = 0; index < values.length; index++) bytes[index] = (byte) values[index];
        return new Stream(bytes);
    }

    private static Animation animation(int replay, int priority) throws Exception {
        Constructor<Animation> constructor = Animation.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Animation result = constructor.newInstance();
        result.replayMode = replay;
        result.priority = priority;
        return result;
    }

    @Test public void zeroMaskAndForcedMovementSelectBytesAndCycle() throws Exception {
        Game game = game(); Player player = new Player(); Game.loopCycle = 100;
        Stream empty = packet(19, 20); game.decodePlayerUpdateMask(0, 2, empty, player);
        assertEquals(0, empty.currentOffset);
        player.smallXYIndex = 3;
        Stream movement = packet(127, 126, 125, 124, 135, 0, 0, 137, 123);
        game.decodePlayerUpdateMask(0x400, 2, movement, player);
        assertEquals(9, movement.currentOffset);
        assertEquals(1, player.forceMoveStartX); assertEquals(2, player.forceMoveStartY);
        assertEquals(3, player.forceMoveEndX); assertEquals(4, player.forceMoveEndY);
        assertEquals(107, player.forceMoveStartCycle); assertEquals(109, player.forceMoveEndCycle);
        assertEquals(5, player.forceMoveDirection); assertEquals(0, player.smallXYIndex);
    }

    @Test public void graphicPackedWordsAndSentinelHaveDifferentEffects() throws Exception {
        Game game = game(); Player player = new Player(); Game.loopCycle = 100;
        Stream graphic = packet(42, 0, 0, 18, 0, 5);
        game.decodePlayerUpdateMask(0x100, 0, graphic, player);
        assertEquals(42, player.spotAnimId); assertEquals(18, player.spotHeight);
        assertEquals(105, player.spotStartCycle); assertEquals(-1, player.spotFrame);
        assertEquals(6, graphic.currentOffset);
        Stream sentinel = packet(255, 255, 0, 1, 0, 0);
        game.decodePlayerUpdateMask(0x100, 0, sentinel, player);
        assertEquals(-1, player.spotAnimId); assertEquals(100, player.spotStartCycle);
        assertEquals(0, player.spotFrame);
    }

    @Test public void animationIdDelayReplayAndPriorityAreSeparate() throws Exception {
        Animation[] saved = Animation.anims;
        try {
            Animation.anims = new Animation[] {animation(1, 4), animation(2, 2)};
            Game game = game(); Player player = new Player(); player.anim = -1;
            game.decodePlayerUpdateMask(8, 0, packet(0, 0, 251), player);
            assertEquals(0, player.anim); assertEquals(5, player.animationDelay);
            player.animFrame = 9;
            game.decodePlayerUpdateMask(8, 0, packet(0, 0, 254), player);
            assertEquals(0, player.animFrame); assertEquals(2, player.animationDelay);
            game.decodePlayerUpdateMask(8, 0, packet(1, 0, 250), player);
            assertEquals(0, player.anim); // Lower priority cannot replace it.
            game.decodePlayerUpdateMask(8, 0, packet(255, 255, 249), player);
            assertEquals(-1, player.anim);
        } finally { Animation.anims = saved; }
    }

    @Test public void overheadTextAndTargetSentinelConsumeOnlyTheirBranches() throws Exception {
        Game game = game(); Player player = new Player(); player.name = "Somebody";
        Stream text = packet('h', 'i', 10, 32, 10);
        game.decodePlayerUpdateMask(4, 0, text, player);
        assertEquals("hi", player.textSpoken); assertEquals(3, text.currentOffset);
        assertEquals(150, player.textCycle);
        game.decodePlayerUpdateMask(1, 0, packet(255, 255), player);
        assertEquals(-1, player.interactingEntity);
        game.decodePlayerUpdateMask(1, 0, packet(17, 0), player);
        assertEquals(17, player.interactingEntity);
    }

    @Test public void ignoredChatSkipsExactPayloadAndLeavesNextByte() throws Exception {
        Game game = game(); Player player = new Player(); player.name = "Ignored"; player.visible = true;
        field(game, "ignoreListAsLongs", new long[] {TextClass.encodeBase37Name("Other"), TextClass.encodeBase37Name("Ignored")});
        game.ignoreCount = 2;
        Stream chat = packet(0x12, 0x34, 0, 253, 11, 22, 33, 77);
        game.decodePlayerUpdateMask(0x80, 0, chat, player);
        assertEquals(7, chat.currentOffset); assertNull(player.textSpoken);
        assertEquals(77, chat.readUnsignedByte());
        game.ignoreCount = 1;
        Stream unmatched = packet(0x12, 0x34, 0, 0, 77);
        game.decodePlayerUpdateMask(0x80, 0, unmatched, player);
        assertEquals(4, unmatched.currentOffset);
        assertEquals(77, unmatched.readUnsignedByte());
    }

    @Test public void appearanceLengthAndPlayerSlotCacheCopiedBytes() throws Exception {
        Game game = game(); Player player = new Player();
        field(game, "cachedPlayerAppearances", new Stream[4]);
        Stream packet = new Stream(new byte[90]);
        packet.buffer[0] = (byte) 200; // Negative byte transform gives length 56.
        packet.currentOffset = 0;
        game.decodePlayerUpdateMask(0x10, 2, packet, player);
        assertEquals(57, packet.currentOffset);
        assertNull(game.cachedPlayerAppearances[1]);
        assertNotNull(game.cachedPlayerAppearances[2]);
        assertEquals(56, game.cachedPlayerAppearances[2].buffer.length);
    }

    @Test public void faceCoordinatesAndTwoHitFormatsKeepDamageAndTypeDistinct() throws Exception {
        Game game = game(); Player player = new Player(); Game.loopCycle = 200;
        game.decodePlayerUpdateMask(2, 0, packet(129, 1, 45, 2), player);
        assertEquals(257, player.faceHalfTileX); assertEquals(557, player.faceHalfTileY);
        game.decodePlayerUpdateMask(0x20, 0, packet(11, 130, 253, 40), player);
        assertEquals(11, player.hitArray[0]); assertEquals(2, player.hitMarkTypes[0]);
        assertEquals(3, player.currentHealth); assertEquals(40, player.maxHealth);
        game.decodePlayerUpdateMask(0x200, 0, packet(19, 123, 31, 224), player);
        assertEquals(19, player.hitArray[1]); assertEquals(5, player.hitMarkTypes[1]);
        assertEquals(31, player.currentHealth); assertEquals(32, player.maxHealth);
        assertEquals(500, player.loopCycleStatus);
    }

    // ---- per-player appearance cache: mask 0x10 stores it, addNewLocalPlayers replays it ----

    private static int[] appearanceBlock(int gender, int combat) {
        int[] block = new int[3 + 12 + 5 + 14 + 8 + 3];
        block[0] = gender;
        for (int k = 0; k < 14; k++) block[20 + k] = 255;   // seven 0xFFFF animation ids
        block[42] = combat;
        return block;
    }

    private static int gender(Player p) throws Exception {
        Field f = Player.class.getDeclaredField("gender"); f.setAccessible(true); return f.getInt(p);
    }

    private static Game listGame() throws Exception {
        Game game = game();
        game.playerArray = new Player[2048];
        game.playerIndices = new int[2048];
        game.maskUpdateIndices = new int[2048];
        game.cachedPlayerAppearances = new Stream[2048];
        return game;
    }

    @Test public void appearanceMaskStoresAPrivateCopyOfExactlyTheBlockAtThePlayersIndex() throws Exception {
        Game game = listGame();
        int[] block = appearanceBlock(1, 77);
        int[] wire = new int[block.length + 2];
        wire[0] = (-block.length) & 0xff;                     // length is sent negated
        System.arraycopy(block, 0, wire, 1, block.length);
        wire[wire.length - 1] = 0x5a;                         // next mask's byte must stay unread
        Stream in = packet(wire);
        Player player = new Player();
        game.decodePlayerUpdateMask(0x10, 37, in, player);
        Stream cached = game.cachedPlayerAppearances[37];
        assertNotNull(cached);
        assertNull(game.cachedPlayerAppearances[36]); assertNull(game.cachedPlayerAppearances[38]);
        assertNotSame(in.buffer, cached.buffer);
        assertEquals(block.length, cached.buffer.length);
        for (int i = 0; i < block.length; i++) assertEquals((byte) block[i], cached.buffer[i]);
        assertEquals(block.length + 1, in.currentOffset);
        assertEquals(1, gender(player)); assertEquals(77, player.combatLevel);
        in.buffer[1] = 0;                                     // later packet-buffer reuse does not touch the cache
        assertEquals(1, cached.buffer[0]);
        game.decodePlayerUpdateMask(0, 37, packet(9), new Player());   // other masks leave it alone
        assertSame(cached, game.cachedPlayerAppearances[37]);
    }

    @Test public void addNewLocalPlayersReplaysTheCachedAppearanceOnlyIntoANewlyCreatedPlayer() throws Exception {
        Player savedMe = Game.myPlayer;
        try {
            Game.myPlayer = new Player(); Game.myPlayer.setPos(50, 60, true);
            Game game = listGame();
            byte[] cachedBytes = new byte[appearanceBlock(0, 0).length];
            int[] block = appearanceBlock(1, 88);
            for (int i = 0; i < block.length; i++) cachedBytes[i] = (byte) block[i];
            Stream cached = new Stream(cachedBytes);
            cached.currentOffset = cachedBytes.length;          // left at the end by an earlier read
            game.cachedPlayerAppearances[9] = cached;
            Player existing = new Player(); existing.combatLevel = 12;
            game.playerArray[11] = existing; game.cachedPlayerAppearances[11] = new Stream(cachedBytes.clone());
            // add 9 (cached), 10 (no cache), 11 (already present): 11+1+1+5+5 bits each, then 2047
            Stream bits = ExistingNpcMovementBitsTest.bits(11, 9, 1, 0, 1, 1, 5, 0, 5, 0,
                    11, 10, 1, 0, 1, 1, 5, 1, 5, 1, 11, 11, 1, 0, 1, 1, 5, 2, 5, 2, 11, 2047);
            game.addNewLocalPlayers(bits, bits.buffer.length);
            Player added = game.playerArray[9];
            assertNotNull(added);
            assertTrue(added.visible); assertEquals(1, gender(added)); assertEquals(88, added.combatLevel);
            assertSame(cached, game.cachedPlayerAppearances[9]);       // replay keeps the cache entry
            Player uncached = game.playerArray[10];
            assertFalse(uncached.visible); assertEquals(0, uncached.combatLevel);
            assertNull(game.cachedPlayerAppearances[10]);
            assertSame(existing, game.playerArray[11]); assertEquals(12, existing.combatLevel);
            assertEquals(3, game.playerCount);
            // the player leaves the list (slot nulled) and comes back: the same cache is applied again
            game.playerArray[9] = null; game.playerCount = 0;
            Stream again = ExistingNpcMovementBitsTest.bits(11, 9, 1, 0, 1, 1, 5, 0, 5, 0, 11, 2047);
            game.addNewLocalPlayers(again, again.buffer.length);
            assertNotSame(added, game.playerArray[9]);
            assertEquals(88, game.playerArray[9].combatLevel); assertTrue(game.playerArray[9].visible);
        } finally {
            Game.myPlayer = savedMe;
        }
    }

    @Test public void playerListRemovalKeepsTheCachedAppearance() throws Exception {
        Player savedMe = Game.myPlayer;
        try {
            Game.myPlayer = new Player(); Game.myPlayer.setPos(50, 60, true);
            Game game = listGame();
            game.removedEntityIndices = new int[1000];
            Game.loopCycle = 731;
            Player p = new Player(); p.setPos(50, 60, true); p.lastUpdateCycle = -1;
            game.playerArray[41] = p; game.playerIndices[0] = 41; game.playerCount = 1;
            Stream cached = new Stream(new byte[1]);
            game.cachedPlayerAppearances[41] = cached;
            game.updatePlayers(2, ExistingNpcMovementBitsTest.bits(1, 0, 8, 0)); // keep 0 of 1
            assertNull(game.playerArray[41]);
            assertSame(cached, game.cachedPlayerAppearances[41]);
        } finally {
            Game.myPlayer = savedMe;
        }
    }
}
