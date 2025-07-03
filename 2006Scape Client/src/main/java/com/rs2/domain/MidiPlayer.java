package com.rs2.domain;

abstract class MidiPlayer {

  abstract void playMidi(int i, byte[] is, int i_2_, boolean bool);

  abstract void shutdown();

  public MidiPlayer() {
    /* empty */
  }

  abstract void adjustVolume(int i, int i_7_);

  abstract void setVolume(int i);

  abstract void poll(int i);

  abstract void stopMidi();
}
