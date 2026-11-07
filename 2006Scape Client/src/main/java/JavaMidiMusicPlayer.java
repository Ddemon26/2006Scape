/* JavaMidiMusicPlayer - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */
import java.io.ByteArrayInputStream;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;

final class JavaMidiMusicPlayer extends MidiMusicPlayer implements Receiver
{
    private static Receiver receiver = null;
    private static Sequencer sequencer = null;

    final void play(int volume, byte[] midiData, int fadeAttenuation, boolean loop) {
    	if (sequencer != null) {
    		try {
    			Sequence sequence = MidiSystem.getSequence(new ByteArrayInputStream(midiData));
    			sequencer.setSequence(sequence);
    			sequencer.setLoopCount(!loop ? 0 : -1);
    			applyFade(0, volume, -1L);
    			sequencer.start();
    		} catch (Exception exception) {
    			/* empty */
    		}
    	}
    }

    final void stop() {
		if (sequencer != null) {
		    sequencer.stop();
		    resetAllChannels(-1L);
		}
    }

    public final synchronized void send(MidiMessage midimessage, long l) {
    	byte[] is = midimessage.getMessage();
    	if (is.length < 3 || !filterVolumeController(is[0], is[1], is[2], l))
    		receiver.send(midimessage, l);
    }

    JavaMidiMusicPlayer() {
		try {
		    receiver = MidiSystem.getReceiver();
		    sequencer = MidiSystem.getSequencer(false);
		    sequencer.getTransmitter().setReceiver(this);
		    sequencer.open();
		    resetAllChannels(-1L);
		} catch (Exception exception) {
		    Game.shutdownMusicPlayer();
		}
    }

    final void shutdown() {
    	if (sequencer != null) {
    		sequencer.close();
    		sequencer = null;
    	}
    	if (receiver != null) {
    		receiver.close();
    		receiver = null;
    	}
    }

    public final void close() {
	/* empty */
    }

    final void setVolume(int volume) {
		if (sequencer != null) {
		    setMasterVolume(volume, -1L);
		}
    }

    final synchronized void fade(int volume, int fadeAttenuation) {
    	if (sequencer != null) {
    		applyFade(fadeAttenuation, volume, -1L);
    	}
    }

    final void sendMessage(int status, int data1, int data2, long timestamp) {
    	try {
    		ShortMessage shortmessage = new ShortMessage();
    		shortmessage.setMessage(status, data1, data2);
    		receiver.send(shortmessage, timestamp);
    	} catch (InvalidMidiDataException invalidmididataexception) {
    		/* empty */
		}
    }

    final void tick(int guard) {
	if (guard > -90)
	    stop();
    }
}
