/* QueuedMidiPlayer - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class QueuedMidiPlayer extends AbstractMidiController implements Runnable
{
    private static MidiHandler midiDevice;
    private static boolean stopThread;
    private static boolean strictMode;
    private static int lastTimestamp;
    private static int bufferPos;
    private MidiFile midiFile = new MidiFile();
    private static int[] messageBuffer = new int[256];
    
    private static final void queueMessage(int i, int i_1_, int i_2_, int i_3_) {
    	if (messageBuffer.length <= bufferPos) {
    		midiDevice.processBuffer(messageBuffer, bufferPos);
    		bufferPos = 0;
    	}
    	messageBuffer[bufferPos++] = i_1_ - lastTimestamp;
    	lastTimestamp = i_1_;
    	messageBuffer[bufferPos++] = i_2_ << 8 | i | i_3_ << 16;
    }
    
    private static final void flushMessages() {
    	if (bufferPos > 0) {
    		midiDevice.processBuffer(messageBuffer, bufferPos);
    		bufferPos = 0;
    	}
    }
    
    final void sendShortMessage(int status, int data1, int data2, long timestamp) {
        queueMessage(status, (int) timestamp, data1, data2);
    }
    
    final synchronized void playMidi(int i, byte[] is, int i_6_,
                                      boolean bool) {
		midiFile.load(is);
		boolean bool_7_ = true;
		strictMode = bool;
		lastTimestamp = 0;
		midiDevice.setActive(false);
		applyVolumeFade(i_6_, i, (long) lastTimestamp);
		int i_8_ = midiFile.getTrackCount();
		for (int i_9_ = 0; i_9_ < i_8_; i_9_++) {
			midiFile.seekTrack(i_9_);
			while (!midiFile.isTrackFinished()) {
				midiFile.readDeltaTime(i_9_);
				if (midiFile.trackTicks[i_9_] != 0) {
					bool_7_ = false;
					break;
				}
				dispatchEvent(i_6_ ^ 0x70, 0L, i_9_);
			}
		    midiFile.saveTrackPosition(i_9_);
		}
		if (bool_7_) {
			if (strictMode)
				throw new RuntimeException();
			resetAllControllers((long) lastTimestamp);
		    midiFile.clear();
		}
		flushMessages();
    }
    
    final synchronized void setVolume(int i) {
		setMasterVolume(i, (long) lastTimestamp);
		midiDevice.processBuffer(messageBuffer, bufferPos);
		bufferPos = 0;
    }
    
    final synchronized void poll(int i) {
	if (midiFile.isLoaded()) {
	    int i_11_ = lastTimestamp;
	    int i_12_ = -200;
	    int i_13_ = midiDevice.getPosition(-29810);
	    long l = ((long) (i_11_ - (i_13_ + i_12_))
		      * (long) (midiFile.timeDivision * 1000));
	    for (;;) {
		int i_14_ = midiFile.getNextTrack();
		int i_15_ = midiFile.trackTicks[i_14_];
		long l_16_ = midiFile.getTimeForTick(i_15_);
		if (l < l_16_)
		    break;
		while (i_15_
		       == midiFile.trackTicks[i_14_]) {
		    midiFile.seekTrack(i_14_);
		    dispatchEvent(126, l_16_, i_14_);
		    if (midiFile.isTrackFinished()) {
			midiFile.saveTrackPosition(i_14_);
			if (midiFile.allTracksFinished()) {
			    if (strictMode)
				midiFile.resetTracks(l_16_);
			    else {
				resetAllControllers
				    ((long) (int) (l_16_
						   / (long) ((midiFile.timeDivision)
							     * 1000)));
				midiFile.clear();
				flushMessages();
				return;
			    }
			}
			break;
		    }
		    midiFile.readDeltaTime(i_14_);
		    midiFile.saveTrackPosition(i_14_);
		}
	    }
	    if (i > -90)
		midiDevice = null;
	    flushMessages();
	}
    }
    
    final synchronized void stopMidi() {
		midiDevice.setActive(false);
		resetAllControllers((long) lastTimestamp);
		midiDevice.processBuffer(messageBuffer, bufferPos);
		bufferPos = 0;
		midiFile.clear();
    }
    
    final void shutdown() {
    	synchronized (this) {
    		stopThread = true;
    	}
    	for (;;) {
    		synchronized (this) {
    			if (!stopThread)
    				break;
    		}
    		Game.sleep(20L);
    	}
    	midiDevice.requestShutdown(true);
    }
    
    public final void run() {
    	try {
    		for (;;) {
    			synchronized (this) {
    				if (stopThread) {
    					stopThread = false;
    					break;
    				}
                                poll(-126);
    			}
    			Game.sleep(100L);
    		}
    	} catch (Exception exception) {
		}
    }
    
    private final void dispatchEvent(int i, long l, int i_17_) {
    	int i_18_ = midiFile.getNextEvent(i_17_);
    	if (i_18_ != 1) {
    		if ((i_18_ & 0x80) != 0) {
    			int i_19_  = (int) (l / (long) (midiFile.timeDivision * 1000));
    			int i_20_ = i_18_ & 0xff;
    			int i_21_ = (i_18_ & 0xffe7d5) >> 16;
				int i_22_ = (i_18_ & 0xfff6) >> 8;
			    if (!handleControlChange(i_20_, i_22_, i_21_, (long) i_19_))
			    	queueMessage(i_20_, i_19_, i_22_, i_21_);
    		}
    	} else
    		midiFile.markTrackEnd();
    }
    
    QueuedMidiPlayer(MidiHandler handler) {
                midiDevice = handler;
                midiDevice.initialize((byte) 96);
                midiDevice.setActive(false);
                resetAllControllers((long) lastTimestamp);
                midiDevice.processBuffer(messageBuffer, bufferPos);
		bufferPos = 0;
		Thread thread = new Thread(this);
		thread.setDaemon(true);
		thread.start();
		thread.setPriority(10);
    }
    
    final synchronized void adjustVolume(int i, int i_23_) {
    	applyVolumeFade(i_23_, i, (long) lastTimestamp);
    }
}
