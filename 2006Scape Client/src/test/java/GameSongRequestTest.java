import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class GameSongRequestTest {
    private MusicPlayer oldPlayer;
    private boolean oldFetch, oldLoop, oldAndroid;
    private int oldVolume, oldPending, oldOtherPending, oldFadeVolume, oldFadeStep, oldProgress;
    private byte[] oldData, oldPendingData;
    private Game game;
    private OnDemandFetcher fetcher;
    private PlayerProbe player;

    @Before public void setUp() {
        oldPlayer = Game.musicPlayer;
        oldFetch = Game.fetchMusic;
        oldLoop = Game.pendingSongLoop;
        oldAndroid = ClientSettings.ANDROID;
        oldVolume = Game.pendingSongVolume;
        oldPending = Game.pendingSongFadeStep;
        oldOtherPending = Game.pendingSongFadeTicks;
        oldFadeVolume = Game.currentSongVolume;
        oldFadeStep = Game.songTransitionTicks;
        oldProgress = Game.fadeProgress;
        oldData = Game.musicData;
        oldPendingData = Game.queuedSongData;
        game = new Game();
        fetcher = new OnDemandFetcher();
        fetcher.versions[2] = new int[] {1, 1, 1, 1};
        game.onDemandFetcher = fetcher;
        game.nextSong = -1;
        player = new PlayerProbe();
        Game.musicPlayer = player;
        ClientSettings.ANDROID = false;
        Game.fetchMusic = true;
        Game.currentSongVolume = -1;
        Game.songTransitionTicks = 0;
        Game.musicData = null;
    }

    @After public void tearDown() {
        Game.musicPlayer = oldPlayer;
        Game.fetchMusic = oldFetch;
        Game.pendingSongLoop = oldLoop;
        ClientSettings.ANDROID = oldAndroid;
        Game.pendingSongVolume = oldVolume;
        Game.pendingSongFadeStep = oldPending;
        Game.pendingSongFadeTicks = oldOtherPending;
        Game.currentSongVolume = oldFadeVolume;
        Game.songTransitionTicks = oldFadeStep;
        Game.fadeProgress = oldProgress;
        Game.musicData = oldData;
        Game.queuedSongData = oldPendingData;
    }

    @Test public void method58QueuesSongAndCarriesFirstAndSecondArguments() {
        game.requestSongWithFade(10, 64, false, 2);
        assertEquals(2, game.nextSong);
        assertEquals(1, fetcher.getOutstandingRequestCount());
        assertEquals(64, Game.pendingSongVolume);
        assertEquals(10, Game.pendingSongFadeStep);
        assertEquals(-1, Game.pendingSongFadeTicks);
        assertTrue(Game.pendingSongLoop);
        game.requestSongWithFade(20, 96, true, 3);
        assertEquals(3, game.nextSong);
        assertEquals(2, fetcher.getOutstandingRequestCount());
        assertEquals(96, Game.pendingSongVolume);
        assertEquals(20, Game.pendingSongFadeStep);
    }

    @Test public void method56QueuesSongWithoutFirstArgumentFade() {
        game.requestSongWithFade(10, 64, false, 2);
        game.requestSongWithoutFade(127, true, 1);
        assertEquals(1, game.nextSong);
        assertEquals(2, fetcher.getOutstandingRequestCount());
        assertEquals(127, Game.pendingSongVolume);
        assertEquals(-1, Game.pendingSongFadeStep);
        assertEquals(-1, Game.pendingSongFadeTicks);
        assertTrue(Game.pendingSongLoop);
    }

    @Test public void originalCodeDispatchesFadeOnlyForMethod58() {
        byte[] data = {1, 2, 3};
        Game.currentSongVolume = 64;
        Game.fadeProgress = 0;
        game.requestSongWithFade(10, 96, false, 2);
        Game.musicData = data;
        Game.updateMusic();
        int firstStep = Game.songTransitionTicks;
        assertTrue(firstStep > 0);
        assertEquals(0, player.plays);
        assertSame(data, Game.queuedSongData);
        assertFalse(Game.fetchMusic);

        Game.currentSongVolume = 64;
        Game.songTransitionTicks = 0;
        Game.fetchMusic = true;
        game.requestSongWithFade(20, 96, false, 2);
        Game.updateMusic();
        assertTrue(Game.songTransitionTicks < firstStep);

        Game.currentSongVolume = -1;
        Game.songTransitionTicks = 0;
        Game.fetchMusic = true;
        game.requestSongWithoutFade(127, false, 1);
        Game.updateMusic();
        assertEquals(1, player.plays);
        assertEquals(127, player.volume);
        assertSame(data, player.data);
        assertTrue(player.loop);
    }

    @Test public void requestsRequirePlayer() {
        Game.musicPlayer = null;
        game.requestSongWithFade(10, 64, false, 2);
        game.requestSongWithoutFade(127, true, 1);
        assertEquals(0, fetcher.getOutstandingRequestCount());
        assertEquals(-1, game.nextSong);
    }

    @Test public void method49UsesAvailableDataAndAdvancesPlayerOncePerCall() {
        Game.pendingSongFadeStep = -1;
        Game.pendingSongFadeTicks = -1;
        Game.pendingSongVolume = 73;
        Game.pendingSongLoop = false;
        Game.updateMusic();
        assertEquals(0, player.plays);
        assertTrue(Game.fetchMusic);
        assertEquals(1, player.ticks);
        assertEquals(-122, player.lastTick);

        byte[] data = {4, 5, 6};
        Game.musicData = data;
        Game.updateMusic();
        assertEquals(1, player.plays);
        assertSame(data, player.data);
        assertEquals(73, player.volume);
        assertFalse(player.loop);
        assertFalse(Game.fetchMusic);
        assertEquals(2, player.ticks);

        Game.updateMusic();
        assertEquals(1, player.plays);
        assertEquals(3, player.ticks);
    }

    @Test public void setVolumeKeepsArgumentForPendingSongWhileFetchingElseFadesCurrentSong() {
        Game.pendingSongVolume = 0;
        Game.pendingSongFadeStep = -1;
        Game.pendingSongFadeTicks = -1;
        Game.pendingSongLoop = true;
        Game.setVolume(77);                       // fetch pending: stored, nothing faded
        assertEquals(77, Game.pendingSongVolume);
        assertEquals("", player.events.toString());
        Game.musicData = new byte[] {9};
        Game.updateMusic();                       // the fetched song starts at that volume, looping per pendingSongLoop
        assertEquals(77, player.volume);
        assertTrue(player.loop);
        assertFalse(Game.fetchMusic);

        Game.currentSongVolume = 50;              // no fetch pending: the argument fades the current song
        Game.songTransitionTicks = 0;
        Game.setVolume(88);
        assertEquals(77, Game.pendingSongVolume);
        assertEquals(88, Game.currentSongVolume);
        assertEquals("fade88;", player.events.toString());

        Game.songTransitionTicks = 5;             // during a transition it becomes the queued song's volume
        Game.queuedSongData = new byte[] {1};
        int oldQueuedVolume = Game.queuedSongVolume;
        Game.applySongVolume(99);
        assertEquals(99, Game.queuedSongVolume);
        assertEquals(88, Game.currentSongVolume);
        assertEquals("fade88;", player.events.toString());
        Game.queuedSongVolume = oldQueuedVolume;

        Game.songTransitionTicks = 0;             // nothing playing: ignored
        Game.currentSongVolume = -1;
        Game.applySongVolume(33);
        assertEquals(-1, Game.currentSongVolume);
        assertEquals("fade88;", player.events.toString());
    }

    @Test public void method49RequiresPlayerEvenWithFetchedData() {
        byte[] data = {7};
        Game.musicData = data;
        Game.musicPlayer = null;
        Game.updateMusic();
        assertTrue(Game.fetchMusic);
        assertSame(data, Game.musicData);
        assertEquals(0, player.plays);
        assertEquals(0, player.ticks);
    }

    @Test public void method790ClosesPlayerAndClearsAttachmentAfterStoppingPlayback() {
        Game.currentSongVolume = 70;
        Game.songTransitionTicks = 4;
        Game.queuedSongData = new byte[] {1};
        Game.shutdownMusicPlayer();
        assertEquals("stop;volume256;close;", player.events.toString());
        assertNull(Game.musicPlayer);
        assertEquals(-1, Game.currentSongVolume);
        assertEquals(0, Game.songTransitionTicks);
        assertNull(Game.queuedSongData);
        Game.shutdownMusicPlayer();
        assertEquals("stop;volume256;close;", player.events.toString());
    }

    @Test public void method790HandlesNoPlayerAndIdlePlayer() {
        Game.musicPlayer = null;
        Game.songTransitionTicks = 7;
        Game.shutdownMusicPlayer();
        assertEquals(7, Game.songTransitionTicks);
        Game.musicPlayer = player;
        Game.songTransitionTicks = 0;
        Game.shutdownMusicPlayer();
        assertEquals("close;", player.events.toString());
        assertNull(Game.musicPlayer);
    }

    private static final class PlayerProbe extends MusicPlayer {
        int plays, volume, ticks, lastTick;
        final StringBuilder events = new StringBuilder();
        byte[] data;
        boolean loop;
        void play(int value, byte[] bytes, int ignored, boolean repeat) {
            plays++; volume = value; data = bytes; loop = repeat;
        }
        void shutdown() { events.append("close;"); }
        void fade(int a, int b) { events.append("fade").append(a).append(';'); }
        void setVolume(int value) { events.append("volume").append(value).append(';'); }
        void tick(int value) { ticks++; lastTick = value; }
        void stop() { events.append("stop;"); }
    }
}
