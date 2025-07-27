package game.animation; // Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.

// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3)

import core.network.Stream;
import core.network.StreamLoader;

public final class Animation {

  public static void unpackConfig(StreamLoader streamLoader) {
    Stream stream = new Stream(streamLoader.getFileData("seq.dat"));
    int length = stream.readUnsignedWord();
    if (anims == null) {
      anims = new Animation[length];
    }
    for (int j = 0; j < length; j++) {
      if (anims[j] == null) {
        anims[j] = new Animation();
      }
      anims[j].readValues(stream);
    }
  }

  public int getFrameDelay(int frameIndex) {
    int j = frameLengths[frameIndex];
    if (j == 0) {
      AnimFrame frame = AnimFrame.forId(frameIds[frameIndex]);
      if (frame != null) {
        j = frameLengths[frameIndex] = frame.delay;
      }
    }
    if (j == 0) {
      j = 1;
    }
    return j;
  }

  private void readValues(Stream stream) {
    do {
      int i = stream.readUnsignedByte();
      if (i == 0) {
        break;
      }
      if (i == 1) {
        frameCount = stream.readUnsignedByte();
        frameIds = new int[frameCount];
        secondaryFrameIds = new int[frameCount];
        frameLengths = new int[frameCount];
        for (int j = 0; j < frameCount; j++) {
          frameIds[j] = stream.readUnsignedWord();
          secondaryFrameIds[j] = stream.readUnsignedWord();
          if (secondaryFrameIds[j] == 65535) {
            secondaryFrameIds[j] = -1;
          }
          frameLengths[j] = stream.readUnsignedWord();
        }

      } else if (i == 2) {
        frameStep = stream.readUnsignedWord();
      } else if (i == 3) {
        int k = stream.readUnsignedByte();
        interleaveOrder = new int[k + 1];
        for (int l = 0; l < k; l++) {
          interleaveOrder[l] = stream.readUnsignedByte();
        }

        interleaveOrder[k] = 0x98967f;
      } else if (i == 4) {
        stretches = true;
      } else if (i == 5) {
        priority = stream.readUnsignedByte();
      } else if (i == 6) {
        leftHandItem = stream.readUnsignedWord();
      } else if (i == 7) {
        rightHandItem = stream.readUnsignedWord();
      } else if (i == 8) {
        maxLoops = stream.readUnsignedByte();
      } else if (i == 9) {
        precedenceAnimating = stream.readUnsignedByte();
      } else if (i == 10) {
        precedenceWalking = stream.readUnsignedByte();
      } else if (i == 11) {
        replayMode = stream.readUnsignedByte();
      } else if (i == 12) {
        stream.readDWord();
      } else {
        System.out.println("Error unrecognised seq config code: " + i);
      }
    } while (true);
    if (frameCount == 0) {
      frameCount = 1;
      frameIds = new int[1];
      frameIds[0] = -1;
      secondaryFrameIds = new int[1];
      secondaryFrameIds[0] = -1;
      frameLengths = new int[1];
      frameLengths[0] = -1;
    }
    if (precedenceAnimating == -1) {
      if (interleaveOrder != null) {
        precedenceAnimating = 2;
      } else {
        precedenceAnimating = 0;
      }
    }
    if (precedenceWalking == -1) {
      if (interleaveOrder != null) {
        precedenceWalking = 2;
        return;
      }
      precedenceWalking = 0;
    }
  }

  private Animation() {
    frameStep = -1;
    stretches = false;
    priority = 5;
    leftHandItem = -1;
    rightHandItem = -1;
    maxLoops = 99;
    precedenceAnimating = -1;
    precedenceWalking = -1;
    replayMode = 2;
  }

  public static Animation anims[];
  public int frameCount;
  public int frameIds[];
  public int secondaryFrameIds[];
  private int[] frameLengths;
  public int frameStep;
  public int interleaveOrder[];
  public boolean stretches;
  public int priority;
  public int leftHandItem;
  public int rightHandItem;
  public int maxLoops;
  public int precedenceAnimating;
  public int precedenceWalking;
  public int replayMode;
  public static int animationCount;
}
