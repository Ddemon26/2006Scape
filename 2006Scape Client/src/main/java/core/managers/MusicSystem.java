package core.managers;

import audio.MidiPlayer;
import core.network.Signlink;

/** Extracted music system logic from the massive {@link Game} class. */
public final class MusicSystem {
  public static int midiVolume = 256;
  public static int[] midiChannels =
      new int[] {
        12800, 12800, 12800, 12800, 12800, 12800,
        12800, 12800, 12800, 12800, 12800, 12800,
        12800, 12800, 12800, 12800
      };
  public static int midiFadeCycles = 0;
  public static MidiPlayer midiPlayer;
  public static boolean fetchMusic = false;
  public static int musicVolume2;
  public static int currentMidiVolume = -1;
  public static byte[] queuedMidiData;
  public static int fadeVolume = 0;
  public static int fadeStep = 0;
  public static int queuedMidiVolume;
  public static boolean midiLooping;
  public static int nextSongDelay;
  public static boolean autoPlaySong;
  public static int queuedSongId;
  public static int musicVolume = 0;
  public static byte[] musicData;

  private MusicSystem() {}

  static boolean musicIsntNull() {
    return midiPlayer != null;
  }

  public static synchronized void closeMidiSystem() {
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
      midiPlayer = (MidiPlayer) Class.forName("audio.SystemMidiPlayer").newInstance();
    } catch (Throwable throwable) {
      return false;
    }
    return true;
  }

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

  static void playMidiTrack(int volume, byte[] data, boolean loop) {
    if (midiPlayer != null) {
      if (currentMidiVolume >= 0) {
        midiPlayer.stopMidi();
        currentMidiVolume = -1;
        queuedMidiData = null;
        midiFadeCycles = 20;
        fadeVolume = 0;
      }
      if (data != null) {
        if (midiFadeCycles > 0) {
          midiPlayer.setVolume(volume);
          midiFadeCycles = 0;
        }
        currentMidiVolume = volume;
        midiPlayer.playMidi(volume, data, 0, loop);
      }
    }
  }

  static void queueMidiTrack(int delay, int compare, boolean loop, byte[] data, int volume) {
    if (midiPlayer != null) {
      if (compare >= currentMidiVolume) {
        delay -= 20;
        if (delay < 1) delay = 1;
        midiFadeCycles = delay;
        if (currentMidiVolume == 0) fadeStep = 0;
        else {
          int v = calculateLogVolume(currentMidiVolume);
          v -= fadeVolume;
          fadeStep = ((fadeStep - 1 + (v + 3600)) / fadeStep);
        }
        midiLooping = loop;
        queuedMidiData = data;
        queuedMidiVolume = volume;
      } else if (midiFadeCycles != 0) {
        midiLooping = loop;
        queuedMidiData = data;
        queuedMidiVolume = volume;
      } else playMidiTrack(volume, data, loop);
    }
  }

  static void initiateMidiFade(boolean loop, int step, int volume, byte[] data) {
    if (midiPlayer != null) {
      if (currentMidiVolume >= 0) {
        fadeStep = step;
        if (currentMidiVolume != 0) {
          int v = calculateLogVolume(currentMidiVolume);
          v -= fadeVolume;
          midiFadeCycles = (v + 3600) / step;
          if (midiFadeCycles < 1) midiFadeCycles = 1;
        } else midiFadeCycles = 1;
        queuedMidiData = data;
        queuedMidiVolume = volume;
        midiLooping = loop;
      } else if (midiFadeCycles == 0) playMidiTrack(volume, data, loop);
      else {
        queuedMidiVolume = volume;
        midiLooping = loop;
        queuedMidiData = data;
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

  public static void stopMidi() {
    if (Signlink.music != null) {
      if (Signlink.music.isRunning()) {
        Signlink.fadeOut();
        Signlink.music.stop();
        Signlink.midi = "stop";
      }
    }
  }
}
