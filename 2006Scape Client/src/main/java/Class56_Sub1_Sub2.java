/* Class56_Sub1_Sub2 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class56_Sub1_Sub2 extends Class56_Sub1 implements Runnable
{
    private static Runnable_Impl1 eventSink;
    private static boolean shutdownRequested;
    private static boolean looping;
    private static int lastEventTime;
    private static int eventBufferLength;
    private Class11 sequenceReader = new Class11();
    private static int[] eventBuffer = new int[256];

    private static final void queueEvent(int status, int time, int data1, int data2) {
    	if (eventBuffer.length <= eventBufferLength) {
    		eventSink.method10(eventBuffer, eventBufferLength);
    		eventBufferLength = 0;
    	}
    	eventBuffer[eventBufferLength++] = time - lastEventTime;
    	lastEventTime = time;
    	eventBuffer[eventBufferLength++] = data1 << 8 | status | data2 << 16;
    }

    private static final void flushEvents() {
    	if (eventBufferLength > 0) {
    		eventSink.method10(eventBuffer, eventBufferLength);
    		eventBufferLength = 0;
    	}
    }

    final void sendMessage(int status, int data1, int data2, long timestamp) {
    	queueEvent(status, (int) timestamp, data1, data2);
    }

    final synchronized void play(int volume, byte[] midiData, int fadeAttenuation,
				      boolean loop) {
		sequenceReader.method525(midiData);
		boolean allTracksEmpty = true;
		looping = loop;
		lastEventTime = 0;
		eventSink.method12(false);
		applyFade(fadeAttenuation, volume, (long) lastEventTime);
		int trackCount = sequenceReader.method533();
		for (int track = 0; track < trackCount; track++) {
			sequenceReader.method526(track);
			while (!sequenceReader.method521()) {
				sequenceReader.method520(track);
				if (sequenceReader.anIntArray216[track] != 0) {
					allTracksEmpty = false;
					break;
				}
				dispatchNextEvent(fadeAttenuation ^ 0x70, 0L, track);
			}
		    sequenceReader.method522(track);
		}
		if (allTracksEmpty) {
			if (looping)
				throw new RuntimeException();
			resetAllChannels((long) lastEventTime);
		    sequenceReader.method523();
		}
		flushEvents();
    }

    final synchronized void setVolume(int volume) {
		setMasterVolume(volume, (long) lastEventTime);
		eventSink.method10(eventBuffer, eventBufferLength);
		eventBufferLength = 0;
    }

    final synchronized void tick(int guard) {
	if (sequenceReader.method527()) {
	    int queuedUntil = lastEventTime;
	    int lookaheadOffset = -200;
	    int sinkReading = eventSink.method14(-29810);
	    long windowEnd = ((long) (queuedUntil - (sinkReading + lookaheadOffset))
		      * (long) (sequenceReader.anInt213 * 1000));
	    for (;;) {
		int track = sequenceReader.method536();
		int eventTick = sequenceReader.anIntArray216[track];
		long eventTime = sequenceReader.method532(eventTick);
		if (windowEnd < eventTime)
		    break;
		while (eventTick
		       == sequenceReader.anIntArray216[track]) {
		    sequenceReader.method526(track);
		    dispatchNextEvent(126, eventTime, track);
		    if (sequenceReader.method521()) {
			sequenceReader.method522(track);
			if (sequenceReader.method531()) {
			    if (looping)
				sequenceReader.method534(eventTime);
			    else {
				resetAllChannels
				    ((long) (int) (eventTime
						   / (long) ((sequenceReader.anInt213)
							     * 1000)));
				sequenceReader.method523();
				flushEvents();
				return;
			    }
			}
			break;
		    }
		    sequenceReader.method520(track);
		    sequenceReader.method522(track);
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
		eventSink.method10(eventBuffer, eventBufferLength);
		eventBufferLength = 0;
		sequenceReader.method523();
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
    	int event = sequenceReader.method529(track);
    	if (event != 1) {
    		if ((event & 0x80) != 0) {
    			int timeMs  = (int) (scaledTime / (long) (sequenceReader.anInt213 * 1000));
    			int status = event & 0xff;
    			int data2 = (event & 0xffe7d5) >> 16;
				int data1 = (event & 0xfff6) >> 8;
			    if (!filterVolumeController(status, data1, data2, (long) timeMs))
			    	queueEvent(status, timeMs, data1, data2);
    		}
    	} else
    		sequenceReader.method528();
    }

    Class56_Sub1_Sub2(Runnable_Impl1 runnable_impl1) {
		eventSink = runnable_impl1;
		eventSink.method15((byte) 96);
		eventSink.method12(false);
		resetAllChannels((long) lastEventTime);
		eventSink.method10(eventBuffer, eventBufferLength);
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
