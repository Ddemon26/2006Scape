package core.managers;

import core.engine.Game;
import core.network.Signlink;
import audio.SoundPlayer;
import audio.Sounds;
import core.network.Stream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;

/** Handles music related actions originally embedded in {@link Game}. */
public final class GameMusicController {
  private final Game game;

  public GameMusicController(Game game) {
    this.game = game;
  }

  public static boolean musicIsntNull() {
    return MusicSystem.musicIsntNull();
  }

  public static void closeMidiSystem() {
    MusicSystem.closeMidiSystem();
  }

  public void musics() {
    for (int MusicIndex = 0; MusicIndex < 3536; MusicIndex++) {
      byte[] abyte0 = getMusic(MusicIndex);
      if (abyte0 != null && abyte0.length > 0) {
        game.decompressors[3].writeEntry(abyte0.length, abyte0, MusicIndex);
      }
    }
  }

  public byte[] getMusic(int Index) {
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
    MusicSystem.setVolume(i);
  }

  public static void setMidiVolume(int i) {
    MusicSystem.setMidiVolume(i);
  }

  public static synchronized void stopMusic(boolean bool) {
    MusicSystem.stopMusic(bool);
  }

  public static void stopMidiPlayback(boolean bool) {
    MusicSystem.stopMidiPlayback(bool);
  }

  public static boolean constructMusic() {
    return MusicSystem.constructMusic();
  }

  public synchronized void queueSong(int delay, int volume, boolean bool, int music) {
    if (MusicSystem.musicIsntNull()) {
      game.nextSong = music;
      game.onDemandFetcher.queueRequest(2, game.nextSong);
      MusicSystem.musicVolume2 = volume;
      MusicSystem.queuedSongId = -1;
      MusicSystem.autoPlaySong = true;
      MusicSystem.nextSongDelay = delay;
    }
  }

  public synchronized void playSong(int volume, boolean bool, int music) {
    if (MusicSystem.musicIsntNull()) {
      game.nextSong = music;
      game.onDemandFetcher.queueRequest(2, game.nextSong);
      MusicSystem.musicVolume2 = volume;
      MusicSystem.queuedSongId = -1;
      MusicSystem.autoPlaySong = true;
      MusicSystem.nextSongDelay = -1;
    }
  }

  /**
   * Process the queued sound effects and background music.
   *
   * <p>Originally part of {@link core.engine.Game#processSoundQueue()}.
   */
  public void processSoundQueue() {
    for (int index = 0; index < game.currentSound; index++) {
      boolean flag1 = false;
      try {
        Stream stream = Sounds.createSoundStream(game.soundType[index], game.sound[index]);
        new SoundPlayer(
            (InputStream) new ByteArrayInputStream(stream.buffer, 0, stream.currentOffset),
            game.soundVolume[index],
            game.soundDelay[index]);
        if (System.currentTimeMillis() + (long) (stream.currentOffset / 22)
            > game.lastSoundUpdate + (long) (game.soundBufferOffset / 22)) {
          game.soundBufferOffset = stream.currentOffset;
          game.lastSoundUpdate = System.currentTimeMillis();
        }
      } catch (Exception exception) {
        exception.printStackTrace();
      }
      if (!flag1 || game.soundDelay[index] == -5) {
        game.currentSound--;
        for (int j = index; j < game.currentSound; j++) {
          game.sound[j] = game.sound[j + 1];
          game.soundType[j] = game.soundType[j + 1];
          game.soundDelay[j] = game.soundDelay[j + 1];
          game.soundVolume[j] = game.soundVolume[j + 1];
        }
        index--;
      } else {
        game.soundDelay[index] = -5;
      }
    }
    if (game.previousSong > 0) {
      game.previousSong -= 20;
      if (game.previousSong < 0) game.previousSong = 0;
      if (game.previousSong == 0 && game.musicVolume != 0 && game.currentSong != -1) {
        playSong(game.musicVolume, false, game.currentSong);
      }
    }
  }

  public static synchronized void processMusicQueue() {
    MusicSystem.processMusicQueue();
  }

  public static int calculateLogVolume(int i) {
    return MusicSystem.calculateLogVolume(i);
  }

  public static void playMidiTrack(int i_2_, byte[] is, boolean bool) {
    MusicSystem.playMidiTrack(i_2_, is, bool);
  }

  public static void queueMidiTrack(int i, int i_29_, boolean bool, byte[] is, int i_30_) {
    MusicSystem.queueMidiTrack(i, i_29_, bool, is, i_30_);
  }

  public static void initiateMidiFade(boolean bool, int i, int i_2_, byte[] is) {
    MusicSystem.initiateMidiFade(bool, i, i_2_, is);
  }

  public static void updateMidiFade(int i) {
    MusicSystem.updateMidiFade(i);
  }

  public void stopMidi() {
    MusicSystem.stopMidi();
  }
}
