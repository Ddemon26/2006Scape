import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GameSoundQueueTest {
    private static final int SOUND_ID = 4999;
    private Game game;
    private boolean android;
    private long oldTime;
    private int oldLength;
    private Object oldSound;
    private Object oldBuffer;
    private Object oldStream;

    private static Field field(Class<?> owner, String name) throws Exception {
        Field field = owner.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    @Before public void setUp() throws Exception {
        android = ClientSettings.ANDROID;
        ClientSettings.ANDROID = true; // The Android hook is a no-op in the desktop test JVM.
        oldTime = Game.soundEndReferenceStartMillis;
        oldLength = Game.soundEndReferenceByteLength;
        oldBuffer = field(Sounds.class, "aByteArray327").get(null);
        oldStream = field(Sounds.class, "aStream_328").get(null);
        Object[] sounds = (Object[]) field(Sounds.class, "aSoundsArray325s").get(null);
        oldSound = sounds[SOUND_ID];
        Sounds.unpack(new Stream(new byte[] {(byte) 255, (byte) 255}));
        Constructor<Sounds> constructor = Sounds.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Sounds controlledSound = constructor.newInstance();
        SynthVoice instrument = new SynthVoice();
        instrument.durationMs = 1;
        instrument.startOffsetMs = 0;
        ((SynthVoice[]) field(Sounds.class, "voices").get(controlledSound))[0] = instrument;
        field(Sounds.class, "anInt330").setInt(controlledSound, 0);
        field(Sounds.class, "anInt331").setInt(controlledSound, 1);
        sounds[SOUND_ID] = controlledSound;
        Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        game = (Game) ((sun.misc.Unsafe) unsafeField.get(null)).allocateInstance(Game.class);
        for (String name : new String[] {"sound", "soundType", "soundDelay", "soundVolume"}) {
            field(Game.class, name).set(game, new int[50]);
        }
        Game.soundEndReferenceStartMillis = 0;
        Game.soundEndReferenceByteLength = 0;
    }

    @After public void tearDown() throws Exception {
        ClientSettings.ANDROID = android;
        Game.soundEndReferenceStartMillis = oldTime;
        Game.soundEndReferenceByteLength = oldLength;
        ((Object[]) field(Sounds.class, "aSoundsArray325s").get(null))[SOUND_ID] = oldSound;
        field(Sounds.class, "aByteArray327").set(null, oldBuffer);
        field(Sounds.class, "aStream_328").set(null, oldStream);
    }

    @Test public void method90UsesSoundIdAndRepeatTypeThenDrainsQueue() {
        game.sound[0] = SOUND_ID - 1; // missing sound is removed before the second entry
        game.soundType[0] = 1;
        game.sound[1] = SOUND_ID;
        game.soundType[1] = 2;
        game.soundVolume[1] = 73;
        game.soundDelay[1] = 4;
        game.currentSound = 2;
        long before = System.currentTimeMillis();
        game.drainQueuedSoundsAndAdvanceSongCountdown();
        long after = System.currentTimeMillis();
        assertEquals(0, game.currentSound);
        assertEquals(88, Game.soundEndReferenceByteLength); // 44-byte WAV header + 2 repeated 22-byte samples
        assertTrue(Game.soundEndReferenceStartMillis >= before && Game.soundEndReferenceStartMillis <= after);
        Game.soundEndReferenceByteLength = 0;
        Game.soundEndReferenceStartMillis = 0;
        game.sound[0] = SOUND_ID;
        game.soundType[0] = 1;
        game.currentSound = 1;
        game.drainQueuedSoundsAndAdvanceSongCountdown();
        assertEquals(66, Game.soundEndReferenceByteLength); // repeat type 1 retains one 22-byte sample
        assertEquals(0, game.currentSound);
    }

    @Test public void method90MissingAndEmptySoundsDoNotChangePlaybackTiming() {
        game.currentSound = 0;
        game.drainQueuedSoundsAndAdvanceSongCountdown();
        assertEquals(0, Game.soundEndReferenceByteLength);
        assertEquals(0, Game.soundEndReferenceStartMillis);
        game.sound[0] = SOUND_ID - 1;
        game.currentSound = 1;
        game.drainQueuedSoundsAndAdvanceSongCountdown();
        assertEquals(0, game.currentSound);
        assertEquals(0, Game.soundEndReferenceByteLength);
        assertEquals(0, Game.soundEndReferenceStartMillis);
    }

    @Test public void method90KeepsEndReferenceWhenNewSoundFinishesEarlier() {
        long futureStart = System.currentTimeMillis() + 10000;
        Game.soundEndReferenceStartMillis = futureStart;
        Game.soundEndReferenceByteLength = 1000;
        game.sound[0] = SOUND_ID;
        game.soundType[0] = 1;
        game.currentSound = 1;
        game.drainQueuedSoundsAndAdvanceSongCountdown();
        assertEquals(0, game.currentSound);
        assertEquals(futureStart, Game.soundEndReferenceStartMillis);
        assertEquals(1000, Game.soundEndReferenceByteLength);
    }

    @Test public void method90CountsDownSongInTwentyUnitStepsWithZeroFloor() {
        game.musicVolume = 0;
        game.previousSong = 41;
        game.drainQueuedSoundsAndAdvanceSongCountdown();
        assertEquals(21, game.previousSong);
        game.drainQueuedSoundsAndAdvanceSongCountdown();
        assertEquals(1, game.previousSong);
        game.drainQueuedSoundsAndAdvanceSongCountdown();
        assertEquals(0, game.previousSong);
        game.drainQueuedSoundsAndAdvanceSongCountdown();
        assertEquals(0, game.previousSong);
    }
}
