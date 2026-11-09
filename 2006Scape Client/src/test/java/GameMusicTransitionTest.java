import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class GameMusicTransitionTest {
    private MusicPlayer oldPlayer;
    private int oldVolume, oldTicks, oldProgress, oldStep, oldQueuedVolume, oldOtherPending, oldPendingStep, oldMusicVolume2, oldOutput;
    private boolean oldQueuedLoop, oldFetch, oldLoop, oldAndroid;
    private byte[] oldQueued, oldData;
    private Probe player;

    @Before public void setUp() {
        oldPlayer = Game.musicPlayer;
        oldVolume = Game.currentSongVolume;
        oldTicks = Game.songTransitionTicks;
        oldProgress = Game.fadeProgress;
        oldStep = Game.fadeStepPerTick;
        oldQueuedVolume = Game.queuedSongVolume;
        oldQueuedLoop = Game.queuedSongLoop;
        oldQueued = Game.queuedSongData;
        oldOtherPending = Game.pendingSongFadeTicks;
        oldPendingStep = Game.pendingSongFadeStep;
        oldMusicVolume2 = Game.pendingSongVolume;
        oldFetch = Game.fetchMusic;
        oldLoop = Game.pendingSongLoop;
        oldData = Game.musicData;
        oldAndroid = ClientSettings.ANDROID;
        oldOutput = Game.musicOutputVolume;
        player = new Probe();
        Game.musicPlayer = player;
        Game.currentSongVolume = -1;
        Game.songTransitionTicks = 0;
        Game.fadeProgress = 0;
        Game.fadeStepPerTick = 0;
        Game.queuedSongVolume = 0;
        Game.queuedSongLoop = false;
        Game.queuedSongData = null;
        ClientSettings.ANDROID = false;
    }

    @After public void tearDown() {
        Game.musicPlayer = oldPlayer;
        Game.currentSongVolume = oldVolume;
        Game.songTransitionTicks = oldTicks;
        Game.fadeProgress = oldProgress;
        Game.fadeStepPerTick = oldStep;
        Game.queuedSongVolume = oldQueuedVolume;
        Game.queuedSongLoop = oldQueuedLoop;
        Game.queuedSongData = oldQueued;
        Game.pendingSongFadeTicks = oldOtherPending;
        Game.pendingSongFadeStep = oldPendingStep;
        Game.pendingSongVolume = oldMusicVolume2;
        Game.fetchMusic = oldFetch;
        Game.pendingSongLoop = oldLoop;
        Game.musicData = oldData;
        ClientSettings.ANDROID = oldAndroid;
        Game.musicOutputVolume = oldOutput;
    }

    // ---- tickSongTransition: idle branch (no song playing) ----

    @Test public void method368IdleCountsDownOncePerCallAndRestoresVolumeAtZero() {
        Game.songTransitionTicks = 3;
        Game.fadeProgress = 55;
        Game.tickSongTransition(0);
        assertEquals(2, Game.songTransitionTicks);
        Game.tickSongTransition(0);
        assertEquals(1, Game.songTransitionTicks);
        assertEquals("tick-122;tick-122;", player.events.toString());
        Game.tickSongTransition(0);
        assertEquals(0, Game.songTransitionTicks);
        assertEquals("tick-122;tick-122;volume256;tick-122;", player.events.toString());
        assertEquals(0, Game.fadeProgress);
        assertEquals(-1, Game.currentSongVolume);
        Game.tickSongTransition(0); // counter already 0: nothing more than the tick
        assertEquals(0, Game.songTransitionTicks);
        assertTrue(player.events.toString().endsWith("volume256;tick-122;tick-122;"));
        assertEquals(0, player.plays);
    }

    @Test public void method368IdleStartsQueuedSongWithQueuedVolumeAndLoopFlag() {
        byte[] queued = {9, 8, 7};
        Game.songTransitionTicks = 2;
        Game.queuedSongData = queued;
        Game.queuedSongVolume = 77;
        Game.queuedSongLoop = true;
        Game.fadeProgress = 40;
        Game.tickSongTransition(0);
        assertEquals(0, player.plays);
        assertSame(queued, Game.queuedSongData);
        Game.tickSongTransition(0);
        assertEquals(1, player.plays);
        assertEquals(77, player.volume);
        assertSame(queued, player.data);
        assertTrue(player.loop);
        assertEquals(77, Game.currentSongVolume);
        assertNull(Game.queuedSongData);
        assertEquals(0, Game.fadeProgress);
        assertTrue(player.events.toString().contains("volume77;play77;"));
        assertFalse(player.events.toString().contains("volume256;"));
        // same with loop false and a different volume
        Game.currentSongVolume = -1;
        Game.songTransitionTicks = 1;
        Game.queuedSongData = new byte[] {1};
        Game.queuedSongVolume = 12;
        Game.queuedSongLoop = false;
        Game.tickSongTransition(0);
        assertEquals(2, player.plays);
        assertEquals(12, player.volume);
        assertFalse(player.loop);
    }

    // ---- tickSongTransition: playing branch (fade out then stop) ----

    @Test public void method368PlayingFadesOutByStepPerTickThenStopsAndArmsTwentyTickGap() {
        Game.currentSongVolume = 64;
        Game.songTransitionTicks = 2;
        Game.fadeStepPerTick = 300;
        Game.fadeProgress = 100;
        Game.queuedSongData = new byte[] {1};
        Game.tickSongTransition(0);
        assertEquals(400, Game.fadeProgress);
        assertEquals(1, Game.songTransitionTicks);
        assertEquals("fade64,400;tick-122;", player.events.toString());
        assertEquals(64, Game.currentSongVolume);
        Game.tickSongTransition(0);
        assertEquals(700, Game.fadeProgress);
        assertEquals(20, Game.songTransitionTicks);
        assertEquals(-1, Game.currentSongVolume);
        assertEquals("fade64,400;tick-122;fade64,700;stop;tick-122;", player.events.toString());
        assertNotNull(Game.queuedSongData); // queued data survives the stop
    }

    @Test public void method368PlayingWithZeroCounterOnlyTicks() {
        Game.currentSongVolume = 64;
        Game.songTransitionTicks = 0;
        Game.fadeStepPerTick = 300;
        Game.fadeProgress = 100;
        Game.tickSongTransition(0);
        assertEquals(100, Game.fadeProgress);
        assertEquals(64, Game.currentSongVolume);
        assertEquals(0, Game.songTransitionTicks);
        assertEquals("tick-122;", player.events.toString());
    }

    @Test public void method368ThresholdParameterSeparatesSilentPlayingFromIdle() {
        // volume 0 is "playing" for the real argument 0 but "idle" for argument 1
        Game.currentSongVolume = 0;
        Game.songTransitionTicks = 2;
        Game.fadeStepPerTick = 5;
        Game.tickSongTransition(0);
        assertEquals(1, Game.songTransitionTicks);
        assertEquals(5, Game.fadeProgress);
        assertEquals("fade0,5;tick-122;", player.events.toString());
        Game.fadeProgress = 0;
        Game.songTransitionTicks = 1;
        player.events.setLength(0);
        Game.tickSongTransition(1);
        assertEquals(0, Game.songTransitionTicks);
        assertEquals("volume256;tick-121;", player.events.toString());
        assertEquals(0, Game.fadeProgress);
    }

    @Test public void method368RequiresPlayer() {
        Game.musicPlayer = null;
        Game.songTransitionTicks = 3;
        Game.tickSongTransition(0);
        assertEquals(3, Game.songTransitionTicks);
        assertEquals("", player.events.toString());
    }

    // ---- full transition: fade out the old song, 20 tick gap, start queued song ----

    @Test public void fullTransitionFadesOutWaitsTwentyTicksThenStartsQueuedSong() {
        byte[] next = {5, 5, 5};
        Game.currentSongVolume = 64;
        Game.fadeProgress = 0;
        Game.fadeOutAndQueueSong(true, 600, 90, next); // fadeOutAndQueueSong -> fade step 600
        int fadeTicks = Game.songTransitionTicks;
        assertTrue(fadeTicks > 1);
        for (int i = 0; i < fadeTicks; i++) {
            assertEquals(64, Game.currentSongVolume);
            Game.tickSongTransition(0);
        }
        assertEquals(-1, Game.currentSongVolume);
        assertEquals(1, player.stops);
        assertEquals(20, Game.songTransitionTicks);
        assertEquals(0, player.plays);
        for (int i = 0; i < 19; i++) Game.tickSongTransition(0);
        assertEquals(1, Game.songTransitionTicks);
        assertEquals(0, player.plays);
        Game.tickSongTransition(0);
        assertEquals(0, Game.songTransitionTicks);
        assertEquals(1, player.plays);
        assertEquals(90, player.volume);
        assertSame(next, player.data);
        assertTrue(player.loop);
        assertEquals(90, Game.currentSongVolume);
        assertEquals(0, Game.fadeProgress);
    }

    // ---- fadeOutAndQueueSong: fade out and queue ----

    @Test public void method684PlayingArmsFadeCountdownFromStepAndProgressAndQueuesSong() {
        byte[] next = {1, 2};
        Game.currentSongVolume = 64;
        Game.fadeProgress = 100;
        Game.songTransitionTicks = 0;
        Game.fadeOutAndQueueSong(true, 10, 90, next);
        assertEquals(10, Game.fadeStepPerTick);
        int ticksAt100 = Game.songTransitionTicks;
        assertTrue(ticksAt100 > 100);
        assertSame(next, Game.queuedSongData);
        assertEquals(90, Game.queuedSongVolume);
        assertTrue(Game.queuedSongLoop);
        assertEquals(0, player.plays);
        // 300 more progress units remove exactly 30 ticks at step 10
        Game.fadeProgress = 400;
        Game.fadeOutAndQueueSong(false, 10, 91, next);
        assertEquals(ticksAt100 - 30, Game.songTransitionTicks);
        assertFalse(Game.queuedSongLoop);
        assertEquals(91, Game.queuedSongVolume);
        // doubling the step roughly halves the ticks
        Game.fadeProgress = 100;
        Game.fadeOutAndQueueSong(true, 20, 90, next);
        assertEquals(20, Game.fadeStepPerTick);
        assertEquals(ticksAt100 / 2, Game.songTransitionTicks);
    }

    @Test public void method684FloorsCountdownAtOneTickAndHandlesSilentCurrentVolume() {
        Game.currentSongVolume = 64;
        Game.fadeProgress = 100000;
        Game.fadeOutAndQueueSong(false, 10, 5, new byte[] {1});
        assertEquals(1, Game.songTransitionTicks);
        Game.currentSongVolume = 0;
        Game.fadeProgress = 0;
        Game.songTransitionTicks = 0;
        Game.fadeOutAndQueueSong(false, 7, 5, new byte[] {1});
        assertEquals(1, Game.songTransitionTicks);
        assertEquals(7, Game.fadeStepPerTick);
    }

    @Test public void method684NotPlayingPlaysImmediatelyWhenCounterZeroOtherwiseOnlyQueues() {
        byte[] data = {3, 3};
        Game.currentSongVolume = -1;
        Game.songTransitionTicks = 0;
        Game.fadeOutAndQueueSong(true, 10, 66, data);
        assertEquals(1, player.plays);
        assertEquals(66, player.volume);
        assertSame(data, player.data);
        assertTrue(player.loop);
        assertEquals(66, Game.currentSongVolume);
        assertNull(Game.queuedSongData);

        Game.currentSongVolume = -1;
        Game.songTransitionTicks = 7;
        Game.fadeOutAndQueueSong(false, 10, 44, data);
        assertEquals(1, player.plays);
        assertEquals(7, Game.songTransitionTicks);
        assertSame(data, Game.queuedSongData);
        assertEquals(44, Game.queuedSongVolume);
        assertFalse(Game.queuedSongLoop);
    }

    @Test public void method684RequiresPlayer() {
        Game.musicPlayer = null;
        Game.fadeOutAndQueueSong(true, 10, 66, new byte[] {1});
        assertNull(Game.queuedSongData);
    }

    // ---- queueSongAfterFadeTicks: fade for explicit tick count then queue ----

    @Test public void method899PlayingUsesTickCountMinusTwentyFloorOneAndQueuesSong() {
        byte[] next = {4};
        Game.currentSongVolume = 64;
        Game.fadeStepPerTick = 10;
        Game.queueSongAfterFadeTicks(50, -1, true, next, 80);
        assertEquals(30, Game.songTransitionTicks);
        assertSame(next, Game.queuedSongData);
        assertEquals(80, Game.queuedSongVolume);
        assertTrue(Game.queuedSongLoop);
        Game.queueSongAfterFadeTicks(21, -1, false, next, 81);
        assertEquals(1, Game.songTransitionTicks);
        Game.queueSongAfterFadeTicks(5, -1, false, next, 82);
        assertEquals(1, Game.songTransitionTicks);
        assertEquals(82, Game.queuedSongVolume);
        assertEquals(0, player.plays);
    }

    @Test public void method899PlayingRewritesStepFromRemainingFadeOffsetAndProgress() {
        // step slot becomes ceil((offset(volume) - progress + 3600) / previousStep); progress 300 lower -> 30 units less at step 10
        Game.currentSongVolume = 64;
        Game.fadeStepPerTick = 10;
        Game.fadeProgress = 100;
        Game.queueSongAfterFadeTicks(50, -1, false, new byte[] {1}, 80);
        int atHundred = Game.fadeStepPerTick;
        assertEquals(230, atHundred); // (-1204 - 100 + 3600 + 9) / 10
        Game.fadeStepPerTick = 10;
        Game.fadeProgress = 400;
        Game.queueSongAfterFadeTicks(50, -1, false, new byte[] {1}, 80);
        assertEquals(atHundred - 30, Game.fadeStepPerTick);
        Game.fadeStepPerTick = 20;
        Game.fadeProgress = 100;
        Game.queueSongAfterFadeTicks(50, -1, false, new byte[] {1}, 80);
        assertEquals(115, Game.fadeStepPerTick); // (2296 + 19) / 20
    }

    @Test public void method899SilentPlayingZeroesStepAndThresholdParameterShiftsPlayingTest() {
        Game.currentSongVolume = 0;
        Game.fadeStepPerTick = 9;
        Game.queueSongAfterFadeTicks(50, -1, false, new byte[] {1}, 80);
        assertEquals(0, Game.fadeStepPerTick);
        assertEquals(30, Game.songTransitionTicks);
        // threshold 0 treats stopped player (-1) as playing; -1 does not
        Game.currentSongVolume = -1;
        Game.songTransitionTicks = 0;
        Game.queueSongAfterFadeTicks(50, -1, false, new byte[] {1}, 80);
        assertEquals(1, player.plays);
        Game.currentSongVolume = -1;
        Game.songTransitionTicks = 0;
        Game.fadeStepPerTick = 9;
        Game.queueSongAfterFadeTicks(50, 0, false, new byte[] {1}, 80);
        assertEquals(30, Game.songTransitionTicks);
    }

    @Test public void method899NotPlayingQueuesWhileCounterRunningElsePlaysNow() {
        byte[] data = {6};
        Game.currentSongVolume = -1;
        Game.songTransitionTicks = 4;
        Game.queueSongAfterFadeTicks(50, -1, true, data, 55);
        assertEquals(0, player.plays);
        assertEquals(4, Game.songTransitionTicks);
        assertSame(data, Game.queuedSongData);
        assertEquals(55, Game.queuedSongVolume);
        Game.songTransitionTicks = 0;
        Game.queuedSongData = null;
        Game.queueSongAfterFadeTicks(50, -1, true, data, 56);
        assertEquals(1, player.plays);
        assertEquals(56, player.volume);
        assertNull(Game.queuedSongData);
    }

    @Test public void fetchedDataRoutesThroughMethod899WhenPendingFadeTicksNonNegative() {
        byte[] data = {2, 2, 2};
        Game.fetchMusic = true;
        Game.musicData = data;
        Game.pendingSongFadeStep = -1;
        Game.pendingSongFadeTicks = 45;
        Game.pendingSongVolume = 70;
        Game.pendingSongLoop = true;
        Game.currentSongVolume = 64;
        Game.fadeStepPerTick = 10;
        Game.updateMusic();
        assertEquals(24, Game.songTransitionTicks); // armed to 25, then updateMusic's own tickSongTransition tick
        assertSame(data, Game.queuedSongData);
        assertEquals(70, Game.queuedSongVolume);
        assertEquals(0, player.plays);
        assertFalse(Game.fetchMusic);
    }

    // ---- applySongVolume: volume change ----

    @Test public void method900SetsVolumeWhenSteadyPlayingAndNotifiesPlayerWithoutFade() {
        Game.currentSongVolume = 50;
        Game.songTransitionTicks = 0;
        Game.applySongVolume(90);
        assertEquals(90, Game.currentSongVolume);
        assertEquals("fade90,0;", player.events.toString());
        Game.currentSongVolume = -1; // not playing: ignored
        Game.applySongVolume(30);
        assertEquals(-1, Game.currentSongVolume);
        assertEquals("fade90,0;", player.events.toString());
    }

    @Test public void method900DuringTransitionUpdatesOnlyQueuedVolumeWhenSongQueued() {
        Game.currentSongVolume = 50;
        Game.songTransitionTicks = 5;
        Game.queuedSongVolume = 11;
        Game.applySongVolume(90);
        assertEquals(50, Game.currentSongVolume);
        assertEquals(11, Game.queuedSongVolume); // no queued data: nothing changes
        Game.queuedSongData = new byte[] {1};
        Game.applySongVolume(90);
        assertEquals(50, Game.currentSongVolume);
        assertEquals(90, Game.queuedSongVolume);
        assertEquals("", player.events.toString());
    }

    @Test public void method900RequiresPlayer() {
        Game.musicPlayer = null;
        Game.currentSongVolume = 50;
        Game.applySongVolume(90);
        assertEquals(50, Game.currentSongVolume);
    }

    // ---- volumeToFadeOffset: volume to fade offset ----

    @Test public void method1004IsNegativeOfFadeUnitsThatAttenuateFullVolumeToTheGivenVolume() {
        FadeProbe fade = new FadeProbe();
        assertEquals(0, Game.volumeToFadeOffset(256));
        for (int volume : new int[] {1, 4, 32, 64, 100, 128, 200, 255}) {
            int offset = Game.volumeToFadeOffset(volume);
            assertTrue("offset negative for " + volume, offset < 0);
            Game.musicOutputVolume = -12345;
            fade.applyFade(-offset, 256, 0);
            assertEquals("volume " + volume, volume, Game.musicOutputVolume, 1);
        }
        assertTrue(Game.volumeToFadeOffset(128) < Game.volumeToFadeOffset(129));
        assertTrue(Game.volumeToFadeOffset(300) > 0); // above full volume: positive
        Game.musicOutputVolume = -12345;
        fade.applyFade(-Game.volumeToFadeOffset(300), 256, 0);
        assertEquals(300, Game.musicOutputVolume, 1);
    }

    private static final class FadeProbe extends MidiMusicPlayer {
        void play(int a, byte[] b, int c, boolean d) { }
        void shutdown() { }
        void fade(int a, int b) { }
        void setVolume(int a) { }
        void tick(int a) { }
        void stop() { }
        void sendMessage(int a, int b, int c, long d) { }
    }

    private static final class Probe extends MusicPlayer {
        int plays, volume, stops;
        byte[] data;
        boolean loop;
        final StringBuilder events = new StringBuilder();
        void play(int value, byte[] bytes, int ignored, boolean repeat) {
            plays++; volume = value; data = bytes; loop = repeat;
            events.append("play").append(value).append(';');
        }
        void shutdown() { events.append("close;"); }
        void fade(int value, int progress) { events.append("fade").append(value).append(',').append(progress).append(';'); }
        void setVolume(int value) { events.append("volume").append(value).append(';'); }
        void tick(int value) { events.append("tick").append(value).append(';'); }
        void stop() { stops++; events.append("stop;"); }
    }
}
