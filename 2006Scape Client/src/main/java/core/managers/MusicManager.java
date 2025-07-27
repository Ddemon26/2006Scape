package core.managers;

import audio.base.MidiPlayer;
import audio.midi.SystemMidiPlayer;
import core.engine.Game;
import core.handlers.Signlink;

import java.io.File;
import java.io.FileInputStream;

public class MusicManager {
    Game game;

    public static MidiPlayer midiPlayer;
    public boolean musicEnabled;
    public boolean songChanging;
    public static boolean fetchMusic = false;
    public static int musicVolume = 0;
    public static int musicVolume2;
    public static int midiFadeCycles = 0;
    public static byte[] queuedMidiData;
    public static int queuedMidiVolume;
    public static int currentMidiVolume = -1;
    public static int fadeVolume = 0;
    public static int fadeStep = 0;
    public static boolean midiLooping;
    public static int nextSongDelay;
    public static boolean autoPlaySong;
    public static int queuedSongId;
    public static int currentSong;
    public static int nextSong;
    public static int previousSong;

    public MusicManager(Game game) {
        this.game = game;
        /*if (midiPlayer == null) {
            if (!constructMusic()) {
                System.out.println("Failed to initialize MIDI player.");
            }
        }*/

        currentSong = -1;
        musicEnabled = true;
        songChanging = true;
    }

    static boolean musicIsntNull() {
        if (midiPlayer == null) return false;
        return true;
    }

    public static void closeMidiSystem() {
        if (midiPlayer != null) {
            stopMidiPlayback(false);
            if (midiFadeCycles > 0) {
                midiPlayer.setVolume(256);
                midiFadeCycles = 0;
            }
            midiPlayer.shutdown();
            midiPlayer = null;
        }
    }

    public void musics() {
        for (int MusicIndex = 0; MusicIndex < 3536; MusicIndex++) {
            byte[] abyte0 = GetMusic(MusicIndex);
            if (abyte0 != null && abyte0.length > 0) {
                game.decompressors[3].writeEntry(abyte0.length, abyte0, MusicIndex);
            }
        }
    }

    public byte[] GetMusic(int Index) {
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

    public static void setVolume(int i) {
        if (musicIsntNull()) {
            if (fetchMusic) musicVolume2 = i;
            else setMidiVolume(i);
        }
    }

    static void setMidiVolume(int i) {
        if (midiPlayer != null) {
            if (midiFadeCycles == 0) {
                if (currentMidiVolume >= 0) {
                    currentMidiVolume = i;
                    midiPlayer.adjustVolume(i, 0);
                }
            } else if (queuedMidiData != null) queuedMidiVolume = i;
        }
    }

    public static synchronized void stopMusic(boolean bool) {
        if (musicIsntNull()) {
            stopMidiPlayback(bool);
            fetchMusic = false;
        }
    }

    static void stopMidiPlayback(boolean bool) {
        playMidiTrack(0, null, bool);
    }

     public static boolean constructMusic() {
        midiFadeCycles = 20;
        try {
            // changed from getting the class via string, not sure why it was done like that.
            midiPlayer = SystemMidiPlayer.class.getDeclaredConstructor().newInstance();
        } catch (Throwable throwable) {
            System.out.println("Error creating midi player: " + throwable.getMessage());
            return false;
        }
        return true;
    }

    public final synchronized void queueSong(int i_30_, int volume, boolean bool, int music) {
        if (musicIsntNull()) {
            nextSong = music;
            game.onDemandFetcher.queueRequest(2, nextSong);
            musicVolume2 = volume;
            queuedSongId = -1;
            autoPlaySong = true;
            nextSongDelay = i_30_;
        }
    }

    public final synchronized void playSong(int i, boolean bool, int music) {
        if (musicIsntNull()) {
            nextSong = music;
            game.onDemandFetcher.queueRequest(2, nextSong);
            musicVolume2 = i;
            queuedSongId = -1;
            autoPlaySong = true;
            nextSongDelay = -1;
        }
    }

    public static byte[] musicData;

    public static synchronized void processMusicQueue() {
        if (musicIsntNull()) {
            if (fetchMusic) {
                byte[] is = musicData;
                if (is != null) {
                    if (nextSongDelay >= 0) initiateMidiFade(autoPlaySong, nextSongDelay, musicVolume2, is);
                    else if (queuedSongId >= 0)
                        queueMidiTrack(queuedSongId, -1, autoPlaySong, is, musicVolume2);
                    else playMidiTrack(musicVolume2, is, autoPlaySong);
                    fetchMusic = false;
                }
            }
            updateMidiFade(0);
        }
    }

    static int calculateLogVolume(int i) {
        return (int) (Math.log((double) i * 0.00390625) * 868.5889638065036 + 0.5);
    }

    static void playMidiTrack(int i_2_, byte[] is, boolean bool) {
        if (midiPlayer != null) {
            if (currentMidiVolume >= 0) {
                midiPlayer.stopMidi();
                currentMidiVolume = -1;
                queuedMidiData = null;
                midiFadeCycles = 20;
                fadeVolume = 0;
            }
            if (is != null) {
                if (midiFadeCycles > 0) {
                    midiPlayer.setVolume(i_2_);
                    midiFadeCycles = 0;
                }
                currentMidiVolume = i_2_;
                midiPlayer.playMidi(i_2_, is, 0, bool);
            }
        }
    }

    static void queueMidiTrack(int i, int i_29_, boolean bool, byte[] is, int i_30_) {
        if (midiPlayer != null) {
            if (i_29_ >= (currentMidiVolume ^ 0xffffffff)) {
                i -= 20;
                if (i < 1) i = 1;
                midiFadeCycles = i;
                if (currentMidiVolume == 0) fadeStep = 0;
                else {
                    int i_31_ = calculateLogVolume(currentMidiVolume);
                    i_31_ -= fadeVolume;
                    fadeStep = ((fadeStep - 1 + (i_31_ + 3600)) / fadeStep);
                }
                midiLooping = bool;
                queuedMidiData = is;
                queuedMidiVolume = i_30_;
            } else if (midiFadeCycles != 0) {
                midiLooping = bool;
                queuedMidiData = is;
                queuedMidiVolume = i_30_;
            } else playMidiTrack(i_30_, is, bool);
        }
    }

    static void initiateMidiFade(boolean bool, int i, int i_2_, byte[] is) {
        if (midiPlayer != null) {
            if (currentMidiVolume >= 0) {
                fadeStep = i;
                if (currentMidiVolume != 0) {
                    int i_4_ = calculateLogVolume(currentMidiVolume);
                    i_4_ -= fadeVolume;
                    midiFadeCycles = (i_4_ + 3600) / i;
                    if (midiFadeCycles < 1) midiFadeCycles = 1;
                } else midiFadeCycles = 1;
                queuedMidiData = is;
                queuedMidiVolume = i_2_;
                midiLooping = bool;
            } else if (midiFadeCycles == 0) playMidiTrack(i_2_, is, bool);
            else {
                queuedMidiVolume = i_2_;
                midiLooping = bool;
                queuedMidiData = is;
            }
        }
    }

    static void updateMidiFade(int i) {
        if (midiPlayer != null) {
            if (currentMidiVolume < i) {
                if (midiFadeCycles > 0) {
                    midiFadeCycles--;
                    if (midiFadeCycles == 0) {
                        if (queuedMidiData == null) midiPlayer.setVolume(256);
                        else {
                            midiPlayer.setVolume(queuedMidiVolume);
                            currentMidiVolume = queuedMidiVolume;
                            midiPlayer.playMidi(queuedMidiVolume, queuedMidiData, 0, midiLooping);
                            queuedMidiData = null;
                        }
                        fadeVolume = 0;
                    }
                }
            } else if (midiFadeCycles > 0) {
                fadeVolume += fadeStep;
                midiPlayer.adjustVolume(currentMidiVolume, fadeVolume);
                midiFadeCycles--;
                if (midiFadeCycles == 0) {
                    midiPlayer.stopMidi();
                    midiFadeCycles = 20;
                    currentMidiVolume = -1;
                }
            }
            midiPlayer.poll(i - 122);
        }
    }

    public void stopMidi() {
        if (Signlink.music != null) {
            if (Signlink.music.isRunning()) {
                Signlink.fadeOut();
                Signlink.music.stop();
                Signlink.midi = "stop";
            }
        }
    }

    public void saveMidi(boolean flag, byte abyte0[]) {
        Signlink.midifade = flag ? 1 : 0;
        Signlink.saveMidi(abyte0, abyte0.length);
    }
}
