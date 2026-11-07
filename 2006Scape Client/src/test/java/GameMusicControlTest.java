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
        oldPlayer = Game.musicPlayer;
        oldFetch = Game.fetchMusic;
        oldVolume = Game.currentSongVolume;
        oldFade = Game.songTransitionTicks;
        oldProgress = Game.fadeProgress;
        oldData = Game.queuedSongData;
        player = new FakePlayer();
        Game.musicPlayer = player;
        Game.fetchMusic = true;
        Game.currentSongVolume = 75;
        Game.songTransitionTicks = 0;
        Game.fadeProgress = 37;
        Game.queuedSongData = new byte[] {9};
    }

    @After public void tearDown() {
        Game.musicPlayer = oldPlayer;
        Game.fetchMusic = oldFetch;
        Game.currentSongVolume = oldVolume;
        Game.songTransitionTicks = oldFade;
        Game.fadeProgress = oldProgress;
        Game.queuedSongData = oldData;
    }

    @Test public void method55StopsActivePlayerAndClearsPendingFetch() {
        Game.stopMusic(false);
        assertEquals(1, player.stops);
        assertFalse(Game.fetchMusic);
        assertEquals(-1, Game.currentSongVolume);
        assertNull(Game.queuedSongData);
        assertEquals(20, Game.songTransitionTicks);
        assertEquals(0, Game.fadeProgress);
        assertEquals(0, player.plays);
        Game.stopMusic(true);
        assertEquals(1, player.stops);
    }

    @Test public void method55RequiresPlayerAndMethod891DoesNotClearPendingFetch() {
        Game.musicPlayer = null;
        Game.stopMusic(false);
        assertTrue(Game.fetchMusic);
        assertEquals(75, Game.currentSongVolume);
        Game.musicPlayer = player;
        Game.stopMusicPlayback(true);
        assertTrue(Game.fetchMusic);
        assertEquals(1, player.stops);
        assertEquals(-1, Game.currentSongVolume);
    }

    @Test public void method853UsesVolumeDataAndLoopFlag() {
        byte[] first = {1, 2};
        Game.currentSongVolume = -1;
        Game.songTransitionTicks = 20;
        Game.setMusicData(64, first, false);
        assertEquals(0, player.stops);
        assertEquals(1, player.plays);
        assertEquals(64, player.volume);
        assertSame(first, player.data);
        assertFalse(player.loop);
        assertEquals(64, Game.currentSongVolume);
        assertEquals(0, Game.songTransitionTicks);
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
        assertEquals(-1, Game.currentSongVolume);
        assertNull(Game.queuedSongData);
    }

    private static final class FakePlayer extends Class56 {
        int stops, plays, volume;
        byte[] data;
        boolean loop;
        void play(int i, byte[] is, int i_2_, boolean bool) { plays++; volume = i; data = is; loop = bool; }
        void shutdown() { }
        void fade(int i, int i_7_) { }
        void setVolume(int i) { }
        void tick(int i) { }
        void stop() { stops++; }
    }
}
