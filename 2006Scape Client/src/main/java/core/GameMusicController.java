package core;

import net.Signlink;
import net.OnDemandFetcher;
import ui.RSInterface;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Handles music related actions originally embedded in {@link Game}.
 */
public final class GameMusicController {
    private final Game game;

    GameMusicController(Game game) {
        this.game = game;
    }

    static boolean musicIsntNull() {
        return MusicSystem.musicIsntNull();
    }

    public static void closeMidiSystem() {
        MusicSystem.closeMidiSystem();
    }

    void musics() {
        for (int MusicIndex = 0; MusicIndex < 3536; MusicIndex++) {
            byte[] abyte0 = getMusic(MusicIndex);
            if (abyte0 != null && abyte0.length > 0) {
                game.decompressors[3].writeEntry(abyte0.length, abyte0, MusicIndex);
            }
        }
    }

    byte[] getMusic(int Index) {
        try {
            File Music = new File(Signlink.findcachedir() + "./sounds/" + Index + ".gz");
            byte[] aByte = new byte[(int) Music.length()];
            FileInputStream Fis = new FileInputStream(Music);
            Fis.read(aByte);
            Fis.close();
            return aByte;
        } catch (Exception e) {
            return null;
        }
    }

    static void setVolume(int i) {
        MusicSystem.setVolume(i);
    }

    static void setMidiVolume(int i) {
        MusicSystem.setMidiVolume(i);
    }

    static synchronized void stopMusic(boolean bool) {
        MusicSystem.stopMusic(bool);
    }

    static void stopMidiPlayback(boolean bool) {
        MusicSystem.stopMidiPlayback(bool);
    }

    static boolean constructMusic() {
        return MusicSystem.constructMusic();
    }

    synchronized void queueSong(int delay, int volume, boolean bool, int music) {
        if (MusicSystem.musicIsntNull()) {
            game.nextSong = music;
            game.onDemandFetcher.queueRequest(2, game.nextSong);
            MusicSystem.musicVolume2 = volume;
            MusicSystem.queuedSongId = -1;
            MusicSystem.autoPlaySong = true;
            MusicSystem.nextSongDelay = delay;
        }
    }

    synchronized void playSong(int volume, boolean bool, int music) {
        if (MusicSystem.musicIsntNull()) {
            game.nextSong = music;
            game.onDemandFetcher.queueRequest(2, game.nextSong);
            MusicSystem.musicVolume2 = volume;
            MusicSystem.queuedSongId = -1;
            MusicSystem.autoPlaySong = true;
            MusicSystem.nextSongDelay = -1;
        }
    }

    static synchronized void processMusicQueue() {
        MusicSystem.processMusicQueue();
    }

    static int calculateLogVolume(int i) {
        return MusicSystem.calculateLogVolume(i);
    }

    static void playMidiTrack(int i_2_, byte[] is, boolean bool) {
        MusicSystem.playMidiTrack(i_2_, is, bool);
    }

    static void queueMidiTrack(int i, int i_29_, boolean bool, byte[] is, int i_30_) {
        MusicSystem.queueMidiTrack(i, i_29_, bool, is, i_30_);
    }

    static void initiateMidiFade(boolean bool, int i, int i_2_, byte[] is) {
        MusicSystem.initiateMidiFade(bool, i, i_2_, is);
    }

    static void updateMidiFade(int i) {
        MusicSystem.updateMidiFade(i);
    }

    void stopMidi() {
        MusicSystem.stopMidi();
    }
}
