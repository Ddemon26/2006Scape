/* SystemMidiPlayer - Decompiled by JODE
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

final class SystemMidiPlayer extends AbstractMidiController implements Receiver
{
    private static Receiver aReceiver1850 = null;
    private static Sequencer aSequencer1851 = null;
    
    final void playMidi(int i, byte[] is, int i_0_, boolean bool) {
    	if (aSequencer1851 != null) {
    		try {
    			Sequence sequence = MidiSystem.getSequence(new ByteArrayInputStream(is));
    			aSequencer1851.setSequence(sequence);
    			aSequencer1851.setLoopCount(!bool ? 0 : -1);
    			applyVolumeFade(0, i, -1L);
    			aSequencer1851.start();
    		} catch (Exception exception) {
    			/* empty */
    		}
    	}
    }
    
    final void stopMidi() {
		if (aSequencer1851 != null) {
		    aSequencer1851.stop();
		    resetAllControllers(-1L);
		}
    }
    
    public final synchronized void send(MidiMessage midimessage, long l) {
    	byte[] is = midimessage.getMessage();
    	if (is.length < 3 || !handleControlChange(is[0], is[1], is[2], l))
    		aReceiver1850.send(midimessage, l);
    }
    
    SystemMidiPlayer() {
		try {
		    aReceiver1850 = MidiSystem.getReceiver();
		    aSequencer1851 = MidiSystem.getSequencer(false);
		    aSequencer1851.getTransmitter().setReceiver(this);
		    aSequencer1851.open();
		    resetAllControllers(-1L);
		} catch (Exception exception) {
                   Game.closeMidiSystem();
		}
    }
    
    final void shutdown() {
    	if (aSequencer1851 != null) {
    		aSequencer1851.close();
    		aSequencer1851 = null;
    	}
    	if (aReceiver1850 != null) {
    		aReceiver1850.close();
    		aReceiver1850 = null;
    	}
    }
    
    public final void close() {
	/* empty */
    }
    
    final void setVolume(int i) {
		if (aSequencer1851 != null) {
		    setMasterVolume(i, -1L);
		}
    }
    
    final synchronized void adjustVolume(int i, int i_2_) {
    	if (aSequencer1851 != null) {
    		applyVolumeFade(i_2_, i, -1L);
    	}
    }
    
    final void sendShortMessage(int status, int data1, int data2, long timestamp) {
        try {
                ShortMessage shortmessage = new ShortMessage();
                shortmessage.setMessage(status, data1, data2);
                aReceiver1850.send(shortmessage, timestamp);
        } catch (InvalidMidiDataException invalidmididataexception) {
                /* empty */
                }
    }
    
    final void poll(int i) {
        if (i > -90)
                    stopMidi();
    }
}
