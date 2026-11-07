import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class GameMusicControlTest {
    private Class56 oldPlayer;
    private boolean oldFetch;
    private int oldVolume, oldFade, oldProgress;
    private byte[] oldData;
    private FakePlayer player;

    @Before public void setUp() {
        oldPlayer = Game.aClass56_749;
        oldFetch = Game.fetchMusic;
        oldVolume = Game.anInt478;
        oldFade = Game.anInt720;
        oldProgress = Game.anInt155;
        oldData = Game.aByteArray347;
        player = new FakePlayer();
        Game.aClass56_749 = player;
        Game.fetchMusic = true;
        Game.anInt478 = 75;
        Game.anInt720 = 0;
        Game.anInt155 = 37;
        Game.aByteArray347 = new byte[] {9};
    }

    @After public void tearDown() {
        Game.aClass56_749 = oldPlayer;
        Game.fetchMusic = oldFetch;
        Game.anInt478 = oldVolume;
        Game.anInt720 = oldFade;
        Game.anInt155 = oldProgress;
        Game.aByteArray347 = oldData;
    }

    @Test public void method55StopsActivePlayerAndClearsPendingFetch() {
        Game.stopMusic(false);
        assertEquals(1, player.stops);
        assertFalse(Game.fetchMusic);
        assertEquals(-1, Game.anInt478);
        assertNull(Game.aByteArray347);
        assertEquals(20, Game.anInt720);
        assertEquals(0, Game.anInt155);
        assertEquals(0, player.plays);
        Game.stopMusic(true);
        assertEquals(1, player.stops);
    }

    @Test public void method55RequiresPlayerAndMethod891DoesNotClearPendingFetch() {
        Game.aClass56_749 = null;
        Game.stopMusic(false);
        assertTrue(Game.fetchMusic);
        assertEquals(75, Game.anInt478);
        Game.aClass56_749 = player;
        Game.stopMusicPlayback(true);
        assertTrue(Game.fetchMusic);
        assertEquals(1, player.stops);
        assertEquals(-1, Game.anInt478);
    }

    @Test public void method853UsesVolumeDataAndLoopFlag() {
        byte[] first = {1, 2};
        Game.anInt478 = -1;
        Game.anInt720 = 20;
        Game.setMusicData(64, first, false);
        assertEquals(0, player.stops);
        assertEquals(1, player.plays);
        assertEquals(64, player.volume);
        assertSame(first, player.data);
        assertFalse(player.loop);
        assertEquals(64, Game.anInt478);
        assertEquals(0, Game.anInt720);
        byte[] second = {3};
        Game.setMusicData(127, second, true);
        assertEquals(1, player.stops);
        assertEquals(2, player.plays);
        assertEquals(127, player.volume);
        assertSame(second, player.data);
        assertTrue(player.loop);
        assertSame(second, player.data);
    }

    @Test public void method853NullDataStopsWithoutStartingOrChangingFetch() {
        Game.setMusicData(0, null, true);
        assertEquals(1, player.stops);
        assertEquals(0, player.plays);
        assertTrue(Game.fetchMusic);
        assertEquals(-1, Game.anInt478);
        assertNull(Game.aByteArray347);
    }

    private static final class FakePlayer extends Class56 {
        int stops, plays, volume;
        byte[] data;
        boolean loop;
        void method827(int i, byte[] is, int i_2_, boolean bool) { plays++; volume = i; data = is; loop = bool; }
        void method828() { }
        void method830(int i, int i_7_) { }
        void method831(int i) { }
        void method832(int i) { }
        void method833() { stops++; }
    }
}
