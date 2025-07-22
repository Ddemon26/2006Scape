package audio;

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
import core.Game;

public final class SystemMidiPlayer extends AbstractMidiController implements Receiver
{
    private static Receiver midiReceiver = null;
    private static Sequencer midiSequencer = null;
    
    public final void playMidi(int i, byte[] is, int i_0_, boolean bool) {
        if (midiSequencer != null) {
    		try {
    			Sequence sequence = MidiSystem.getSequence(new ByteArrayInputStream(is));
                        midiSequencer.setSequence(sequence);
                        midiSequencer.setLoopCount(!bool ? 0 : -1);
                        applyVolumeFade(0, i, -1L);
                        midiSequencer.start();
    		} catch (Exception exception) {
    			/* empty */
    		}
    	}
    }
    
    public final void stopMidi() {
                if (midiSequencer != null) {
                    midiSequencer.stop();
		    resetAllControllers(-1L);
		}
    }
    
    public final synchronized void send(MidiMessage midimessage, long l) {
    	byte[] is = midimessage.getMessage();
    	if (is.length < 3 || !handleControlChange(is[0], is[1], is[2], l))
                midiReceiver.send(midimessage, l);
    }
    
    public SystemMidiPlayer() {
		try {
                    midiReceiver = MidiSystem.getReceiver();
                    midiSequencer = MidiSystem.getSequencer(false);
                    midiSequencer.getTransmitter().setReceiver(this);
                    midiSequencer.open();
		    resetAllControllers(-1L);
		} catch (Exception exception) {
                    core.GameMusicController.closeMidiSystem();
		}
    }
    
    public final void shutdown() {
        if (midiSequencer != null) {
                midiSequencer.close();
                midiSequencer = null;
        }
        if (midiReceiver != null) {
                midiReceiver.close();
                midiReceiver = null;
        }
    }
    
    public final void close() {
	/* empty */
    }
    
    public final void setVolume(int i) {
        if (midiSequencer != null) {
                    setMasterVolume(i, -1L);
		}
    }
    
    public final synchronized void adjustVolume(int i, int i_2_) {
        if (midiSequencer != null) {
                applyVolumeFade(i_2_, i, -1L);
    	}
    }
    
    final void sendShortMessage(int status, int data1, int data2, long timestamp) {
        try {
                ShortMessage shortmessage = new ShortMessage();
                shortmessage.setMessage(status, data1, data2);
                midiReceiver.send(shortmessage, timestamp);
        } catch (InvalidMidiDataException invalidmididataexception) {
                /* empty */
                }
    }
    
    public final void poll(int i) {
        if (i > -90)
                    stopMidi();
    }
}
