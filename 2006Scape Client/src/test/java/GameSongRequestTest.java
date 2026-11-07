import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class GameSongRequestTest {
    private Class56 oldPlayer;
    private boolean oldFetch, oldLoop, oldAndroid;
    private int oldVolume, oldPending, oldOtherPending, oldFadeVolume, oldFadeStep, oldProgress;
    private byte[] oldData, oldPendingData;
    private Game game;
    private OnDemandFetcher fetcher;
    private PlayerProbe player;

    @Before public void setUp() {
        oldPlayer = Game.aClass56_749;
        oldFetch = Game.fetchMusic;
        oldLoop = Game.aBoolean995;
        oldAndroid = ClientSettings.ANDROID;
        oldVolume = Game.musicVolume2;
        oldPending = Game.pendingSongFadeStep;
        oldOtherPending = Game.anInt139;
        oldFadeVolume = Game.anInt478;
        oldFadeStep = Game.anInt720;
        oldProgress = Game.anInt155;
        oldData = Game.musicData;
        oldPendingData = Game.aByteArray347;
        game = new Game();
        fetcher = new OnDemandFetcher();
        fetcher.versions[2] = new int[] {1, 1, 1, 1};
        game.onDemandFetcher = fetcher;
        game.nextSong = -1;
        player = new PlayerProbe();
        Game.aClass56_749 = player;
        ClientSettings.ANDROID = false;
        Game.fetchMusic = true;
        Game.anInt478 = -1;
        Game.anInt720 = 0;
        Game.musicData = null;
    }

    @After public void tearDown() {
        Game.aClass56_749 = oldPlayer;
        Game.fetchMusic = oldFetch;
        Game.aBoolean995 = oldLoop;
        ClientSettings.ANDROID = oldAndroid;
        Game.musicVolume2 = oldVolume;
        Game.pendingSongFadeStep = oldPending;
        Game.anInt139 = oldOtherPending;
        Game.anInt478 = oldFadeVolume;
        Game.anInt720 = oldFadeStep;
        Game.anInt155 = oldProgress;
        Game.musicData = oldData;
        Game.aByteArray347 = oldPendingData;
    }

    @Test public void method58QueuesSongAndCarriesFirstAndSecondArguments() {
        game.requestSongWithFade(10, 64, false, 2);
        assertEquals(2, game.nextSong);
        assertEquals(1, fetcher.getNodeCount());
        assertEquals(64, Game.musicVolume2);
        assertEquals(10, Game.pendingSongFadeStep);
        assertEquals(-1, Game.anInt139);
        assertTrue(Game.aBoolean995);
        game.requestSongWithFade(20, 96, true, 3);
        assertEquals(3, game.nextSong);
        assertEquals(2, fetcher.getNodeCount());
        assertEquals(96, Game.musicVolume2);
        assertEquals(20, Game.pendingSongFadeStep);
    }

    @Test public void method56QueuesSongWithoutFirstArgumentFade() {
        game.requestSongWithFade(10, 64, false, 2);
        game.requestSongWithoutFade(127, true, 1);
        assertEquals(1, game.nextSong);
        assertEquals(2, fetcher.getNodeCount());
        assertEquals(127, Game.musicVolume2);
        assertEquals(-1, Game.pendingSongFadeStep);
        assertEquals(-1, Game.anInt139);
        assertTrue(Game.aBoolean995);
    }

    @Test public void originalCodeDispatchesFadeOnlyForMethod58() {
        byte[] data = {1, 2, 3};
        Game.anInt478 = 64;
        Game.anInt155 = 0;
        game.requestSongWithFade(10, 96, false, 2);
        Game.musicData = data;
        Game.updateMusic();
        int firstStep = Game.anInt720;
        assertTrue(firstStep > 0);
        assertEquals(0, player.plays);
        assertSame(data, Game.aByteArray347);
        assertFalse(Game.fetchMusic);

        Game.anInt478 = 64;
        Game.anInt720 = 0;
        Game.fetchMusic = true;
        game.requestSongWithFade(20, 96, false, 2);
        Game.updateMusic();
        assertTrue(Game.anInt720 < firstStep);

        Game.anInt478 = -1;
        Game.anInt720 = 0;
        Game.fetchMusic = true;
        game.requestSongWithoutFade(127, false, 1);
        Game.updateMusic();
        assertEquals(1, player.plays);
        assertEquals(127, player.volume);
        assertSame(data, player.data);
        assertTrue(player.loop);
    }

    @Test public void requestsRequirePlayer() {
        Game.aClass56_749 = null;
        game.requestSongWithFade(10, 64, false, 2);
        game.requestSongWithoutFade(127, true, 1);
        assertEquals(0, fetcher.getNodeCount());
        assertEquals(-1, game.nextSong);
    }

    @Test public void method49UsesAvailableDataAndAdvancesPlayerOncePerCall() {
        Game.pendingSongFadeStep = -1;
        Game.anInt139 = -1;
        Game.musicVolume2 = 73;
        Game.aBoolean995 = false;
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

    @Test public void method49RequiresPlayerEvenWithFetchedData() {
        byte[] data = {7};
        Game.musicData = data;
        Game.aClass56_749 = null;
        Game.updateMusic();
        assertTrue(Game.fetchMusic);
        assertSame(data, Game.musicData);
        assertEquals(0, player.plays);
        assertEquals(0, player.ticks);
    }

    private static final class PlayerProbe extends Class56 {
        int plays, volume, ticks, lastTick;
        byte[] data;
        boolean loop;
        void method827(int value, byte[] bytes, int ignored, boolean repeat) {
            plays++; volume = value; data = bytes; loop = repeat;
        }
        void method828() { }
        void method830(int a, int b) { }
        void method831(int value) { }
        void method832(int value) { ticks++; lastTick = value; }
        void method833() { }
    }
}
