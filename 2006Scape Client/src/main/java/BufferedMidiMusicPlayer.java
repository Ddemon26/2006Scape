/* BufferedMidiMusicPlayer - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class BufferedMidiMusicPlayer extends MidiMusicPlayer implements Runnable
{
    private static MidiEventSink eventSink;
    private static boolean shutdownRequested;
    private static boolean looping;
    private static int lastEventTime;
    private static int eventBufferLength;
    private Class11 sequenceReader = new Class11();
    private static int[] eventBuffer = new int[256];

    private static final void queueEvent(int status, int time, int data1, int data2) {
    	if (eventBuffer.length <= eventBufferLength) {
		eventSink.writeEvents(eventBuffer, eventBufferLength);
    		eventBufferLength = 0;
    	}
    	eventBuffer[eventBufferLength++] = time - lastEventTime;
    	lastEventTime = time;
    	eventBuffer[eventBufferLength++] = data1 << 8 | status | data2 << 16;
    }

    private static final void flushEvents() {
    	if (eventBufferLength > 0) {
		eventSink.writeEvents(eventBuffer, eventBufferLength);
    		eventBufferLength = 0;
    	}
    }

    final void sendMessage(int status, int data1, int data2, long timestamp) {
    	queueEvent(status, (int) timestamp, data1, data2);
    }

    final synchronized void play(int volume, byte[] midiData, int fadeAttenuation,
				      boolean loop) {
		sequenceReader.load(midiData);
		boolean allTracksEmpty = true;
		looping = loop;
		lastEventTime = 0;
		eventSink.method12(false);
		applyFade(fadeAttenuation, volume, (long) lastEventTime);
		int trackCount = sequenceReader.trackCount();
		for (int track = 0; track < trackCount; track++) {
			sequenceReader.restoreTrackPosition(track);
			while (!sequenceReader.isTrackEnded()) {
				sequenceReader.readDelta(track);
				if (sequenceReader.trackTicks[track] != 0) {
					allTracksEmpty = false;
					break;
				}
				dispatchNextEvent(fadeAttenuation ^ 0x70, 0L, track);
			}
		    sequenceReader.saveTrackPosition(track);
		}
		if (allTracksEmpty) {
			if (looping)
				throw new RuntimeException();
			resetAllChannels((long) lastEventTime);
		    sequenceReader.release();
		}
		flushEvents();
    }

    final synchronized void setVolume(int volume) {
		setMasterVolume(volume, (long) lastEventTime);
		eventSink.writeEvents(eventBuffer, eventBufferLength);
		eventBufferLength = 0;
    }

    final synchronized void tick(int guard) {
	if (sequenceReader.isLoaded()) {
	    int queuedUntil = lastEventTime;
	    int lookaheadOffset = -200;
	    int sinkReading = eventSink.getPlaybackPosition(-29810);
	    long windowEnd = ((long) (queuedUntil - (sinkReading + lookaheadOffset))
		      * (long) (sequenceReader.division * 1000));
	    for (;;) {
		int track = sequenceReader.nextTrack();
		int eventTick = sequenceReader.trackTicks[track];
		long eventTime = sequenceReader.tickToScaledTime(eventTick);
		if (windowEnd < eventTime)
		    break;
		while (eventTick
		       == sequenceReader.trackTicks[track]) {
		    sequenceReader.restoreTrackPosition(track);
		    dispatchNextEvent(126, eventTime, track);
		    if (sequenceReader.isTrackEnded()) {
			sequenceReader.saveTrackPosition(track);
			if (sequenceReader.allTracksEnded()) {
			    if (looping)
				sequenceReader.restart(eventTime);
			    else {
				resetAllChannels
				    ((long) (int) (eventTime
						   / (long) ((sequenceReader.division)
							     * 1000)));
				sequenceReader.release();
				flushEvents();
				return;
			    }
			}
			break;
		    }
		    sequenceReader.readDelta(track);
		    sequenceReader.saveTrackPosition(track);
		}
	    }
	    if (guard > -90)
		eventSink = null;
	    flushEvents();
	}
    }

    final synchronized void stop() {
		eventSink.method12(false);
		resetAllChannels((long) lastEventTime);
		eventSink.writeEvents(eventBuffer, eventBufferLength);
		eventBufferLength = 0;
		sequenceReader.release();
    }

    final void shutdown() {
    	synchronized (this) {
    		shutdownRequested = true;
    	}
    	for (;;) {
    		synchronized (this) {
    			if (!shutdownRequested)
    				break;
    		}
    		Game.sleep(20L);
    	}
    	eventSink.method11(true);
    }

    public final void run() {
    	try {
    		for (;;) {
    			synchronized (this) {
    				if (shutdownRequested) {
    					shutdownRequested = false;
    					break;
    				}
    				tick(-126);
    			}
    			Game.sleep(100L);
    		}
    	} catch (Exception exception) {
		}
    }

    private final void dispatchNextEvent(int unused, long scaledTime, int track) {
		int event = sequenceReader.readEvent(track);
    	if (event != 1) {
    		if ((event & 0x80) != 0) {
				int timeMs  = (int) (scaledTime / (long) (sequenceReader.division * 1000));
    			int status = event & 0xff;
    			int data2 = (event & 0xffe7d5) >> 16;
				int data1 = (event & 0xfff6) >> 8;
			    if (!filterVolumeController(status, data1, data2, (long) timeMs))
			    	queueEvent(status, timeMs, data1, data2);
    		}
    	} else
		sequenceReader.markTrackEnded();
    }

    BufferedMidiMusicPlayer(MidiEventSink sink) {
		eventSink = sink;
		eventSink.method15((byte) 96);
		eventSink.method12(false);
		resetAllChannels((long) lastEventTime);
		eventSink.writeEvents(eventBuffer, eventBufferLength);
		eventBufferLength = 0;
		Thread thread = new Thread(this);
		thread.setDaemon(true);
		thread.start();
		thread.setPriority(10);
    }

    final synchronized void fade(int volume, int fadeAttenuation) {
    	applyFade(fadeAttenuation, volume, (long) lastEventTime);
    }
}
