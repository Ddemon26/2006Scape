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
        assertEquals(1, player.anInt1543); assertEquals(2, player.anInt1545);
        assertEquals(3, player.anInt1544); assertEquals(4, player.anInt1546);
        assertEquals(107, player.anInt1547); assertEquals(109, player.anInt1548);
        assertEquals(5, player.anInt1549); assertEquals(0, player.smallXYIndex);
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
        field(game, "aStreamArray895s", new Stream[4]);
        Stream packet = new Stream(new byte[90]);
        packet.buffer[0] = (byte) 200; // Negative byte transform gives length 56.
        packet.currentOffset = 0;
        game.decodePlayerUpdateMask(0x10, 2, packet, player);
        assertEquals(57, packet.currentOffset);
        assertNull(game.aStreamArray895s[1]);
        assertNotNull(game.aStreamArray895s[2]);
        assertEquals(56, game.aStreamArray895s[2].buffer.length);
    }

    @Test public void faceCoordinatesAndTwoHitFormatsKeepDamageAndTypeDistinct() throws Exception {
        Game game = game(); Player player = new Player(); Game.loopCycle = 200;
        game.decodePlayerUpdateMask(2, 0, packet(129, 1, 45, 2), player);
        assertEquals(257, player.anInt1538); assertEquals(557, player.anInt1539);
        game.decodePlayerUpdateMask(0x20, 0, packet(11, 130, 253, 40), player);
        assertEquals(11, player.hitArray[0]); assertEquals(2, player.hitMarkTypes[0]);
        assertEquals(3, player.currentHealth); assertEquals(40, player.maxHealth);
        game.decodePlayerUpdateMask(0x200, 0, packet(19, 123, 31, 224), player);
        assertEquals(19, player.hitArray[1]); assertEquals(5, player.hitMarkTypes[1]);
        assertEquals(31, player.currentHealth); assertEquals(32, player.maxHealth);
        assertEquals(500, player.loopCycleStatus);
    }
}
