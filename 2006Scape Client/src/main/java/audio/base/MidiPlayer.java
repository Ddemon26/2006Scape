package audio.base;

public abstract class MidiPlayer {

  public abstract void playMidi(int i, byte[] is, int i_2_, boolean bool);

  public abstract void shutdown();

  public MidiPlayer() {
    /* empty */
  }

  public abstract void adjustVolume(int i, int i_7_);

  public abstract void setVolume(int i);

  public abstract void poll(int i);

  public abstract void stopMidi();
}
