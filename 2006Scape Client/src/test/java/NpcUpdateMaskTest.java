import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import org.junit.Test;
import static org.junit.Assert.*;

public class NpcUpdateMaskTest {
    private static Game game() throws Exception {
        Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        Game game = (Game) ((sun.misc.Unsafe) field.get(null)).allocateInstance(Game.class);
        game.npcArray = new NPC[8];
        game.maskUpdateIndices = new int[8];
        return game;
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

    @Test public void queuedSlotsSelectTheirOwnNpcAndMaskByte() throws Exception {
        Game game = game(); NPC first = new NPC(), second = new NPC();
        game.npcArray[2] = first; game.npcArray[5] = second;
        game.maskUpdateIndices[0] = 2; game.maskUpdateIndices[1] = 5; game.maskUpdateCount = 2;
        Stream stream = packet(0, 1, 'h', 'i', 10, 77);
        game.decodeNpcUpdateMasks(stream);
        assertNull(first.textSpoken); assertEquals("hi", second.textSpoken);
        assertEquals(100, second.textCycle); assertEquals(5, stream.currentOffset);
        assertEquals(77, stream.readUnsignedByte());
        game.maskUpdateCount = 0;
        Stream empty = packet(99); game.decodeNpcUpdateMasks(empty);
        assertEquals(0, empty.currentOffset);
    }

    @Test public void animationIdDelayReplayAndPriorityRemainDistinct() throws Exception {
        Animation[] saved = Animation.anims;
        try {
            Animation.anims = new Animation[] {animation(1, 4), animation(2, 2)};
            Game game = game(); NPC npc = new NPC(); npc.anim = -1; npc.smallXYIndex = 3;
            game.npcArray[2] = npc; game.maskUpdateIndices[0] = 2; game.maskUpdateCount = 1;
            Stream start = packet(0x10, 0, 0, 5);
            game.decodeNpcUpdateMasks(start);
            assertEquals(4, start.currentOffset); assertEquals(0, npc.anim);
            assertEquals(5, npc.animationDelay); assertEquals(3, npc.anInt1542);
            npc.animFrame = 9; npc.anInt1530 = 9;
            game.decodeNpcUpdateMasks(packet(0x10, 0, 0, 2));
            assertEquals(0, npc.animFrame); assertEquals(2, npc.animationDelay);
            assertEquals(0, npc.anInt1530);
            game.decodeNpcUpdateMasks(packet(0x10, 1, 0, 7));
            assertEquals(0, npc.anim); assertEquals(2, npc.animationDelay);
            game.decodeNpcUpdateMasks(packet(0x10, 255, 255, 8));
            assertEquals(-1, npc.anim); assertEquals(8, npc.animationDelay);
            npc.anim = 1; npc.anInt1530 = 9;
            game.decodeNpcUpdateMasks(packet(0x10, 1, 0, 6));
            assertEquals(0, npc.anInt1530); assertEquals(8, npc.animationDelay);
        } finally { Animation.anims = saved; }
    }

    @Test public void twoHitFormatsKeepDamageAndTypeSeparate() throws Exception {
        Game game = game(); NPC npc = new NPC(); Game.loopCycle = 200;
        game.npcArray[2] = npc; game.maskUpdateIndices[0] = 2; game.maskUpdateCount = 1;
        Stream stream = packet(0x48, 139, 254, 131, 40, 237, 123, 97, 224, 77);
        game.decodeNpcUpdateMasks(stream);
        assertEquals(9, stream.currentOffset); assertEquals(77, stream.readUnsignedByte());
        assertEquals(11, npc.hitArray[0]); assertEquals(2, npc.hitMarkTypes[0]);
        assertEquals(19, npc.hitArray[1]); assertEquals(5, npc.hitMarkTypes[1]);
        assertEquals(270, npc.hitsLoopCycle[0]); assertEquals(270, npc.hitsLoopCycle[1]);
        assertEquals(31, npc.currentHealth); assertEquals(32, npc.maxHealth);
        assertEquals(500, npc.loopCycleStatus);
    }

    @Test public void graphicPackedHalvesAndSentinelAreIndependent() throws Exception {
        Game game = game(); NPC npc = new NPC(); Game.loopCycle = 100;
        game.npcArray[2] = npc; game.maskUpdateIndices[0] = 2; game.maskUpdateCount = 1;
        Stream future = packet(0x80, 0, 42, 0, 18, 0, 5);
        game.decodeNpcUpdateMasks(future);
        assertEquals(7, future.currentOffset); assertEquals(42, npc.spotAnimId);
        assertEquals(18, npc.spotHeight); assertEquals(105, npc.spotStartCycle);
        assertEquals(-1, npc.spotFrame);
        game.decodeNpcUpdateMasks(packet(0x80, 255, 255, 0, 3, 0, 0));
        assertEquals(-1, npc.spotAnimId); assertEquals(3, npc.spotHeight);
        assertEquals(100, npc.spotStartCycle); assertEquals(0, npc.spotFrame);
    }

    @Test public void targetTextAndFaceCoordinatesUseSeparateMaskBits() throws Exception {
        Game game = game(); NPC npc = new NPC();
        game.npcArray[2] = npc; game.maskUpdateIndices[0] = 2; game.maskUpdateCount = 1;
        Stream stream = packet(0x25, 0, 17, 'o', 'k', 10, 2, 1, 4, 3, 77);
        game.decodeNpcUpdateMasks(stream);
        assertEquals(10, stream.currentOffset); assertEquals(77, stream.readUnsignedByte());
        assertEquals(17, npc.interactingEntity); assertEquals("ok", npc.textSpoken);
        assertEquals(258, npc.anInt1538); assertEquals(772, npc.anInt1539);
        game.decodeNpcUpdateMasks(packet(0x20, 255, 255));
        assertEquals(-1, npc.interactingEntity);
    }
}
