import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.sound.midi.Receiver;

import org.junit.Test;

import static org.junit.Assert.*;

/** Roles of the music class family and its sink interface, stated structurally (behaviour is in MusicPlayerTest). */
public class MusicClassRolesTest {
    private static Set<String> abstractNames(Class<?> c) {
        Set<String> s = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isAbstract(m.getModifiers())) s.add(m.getName());
        return s;
    }

    @Test public void baseClassIsTheAbstractPlayerContractGameHolds() throws Exception {
        assertTrue(Modifier.isAbstract(MusicPlayer.class.getModifiers()));
        assertEquals(MusicPlayer.class, Game.class.getDeclaredField("musicPlayer").getType());
        assertEquals(new HashSet<String>(Arrays.asList("play", "shutdown", "fade", "setVolume", "tick", "stop")),
                abstractNames(MusicPlayer.class));
        // the player contract carries no MIDI sink, device or event-buffer state
        assertEquals(0, MusicPlayer.class.getDeclaredFields().length);
    }

    @Test public void midiLayerIsAbstractAndAddsOnlyTheMessageHook() {
        assertSame(MusicPlayer.class, MidiMusicPlayer.class.getSuperclass());
        assertTrue(Modifier.isAbstract(MidiMusicPlayer.class.getModifiers()));
        assertEquals(new HashSet<String>(Arrays.asList("sendMessage")), abstractNames(MidiMusicPlayer.class));
        assertFalse(Receiver.class.isAssignableFrom(MidiMusicPlayer.class));
        assertFalse(Runnable.class.isAssignableFrom(MidiMusicPlayer.class));
    }

    @Test public void desktopPlayerIsTheJavaxReceiverAndNeverTakesTheSink() {
        assertSame(MidiMusicPlayer.class, JavaMidiMusicPlayer.class.getSuperclass());
        assertTrue(Receiver.class.isAssignableFrom(JavaMidiMusicPlayer.class));
        assertFalse(Runnable.class.isAssignableFrom(JavaMidiMusicPlayer.class));
        assertFalse(Modifier.isAbstract(JavaMidiMusicPlayer.class.getModifiers()));
        for (java.lang.reflect.Constructor<?> k : JavaMidiMusicPlayer.class.getDeclaredConstructors())
            assertEquals(0, k.getParameterTypes().length);
    }

    @Test public void bufferedPlayerIsARunnablePumpFedToTheSinkAndNotAReceiver() throws Exception {
        assertSame(MidiMusicPlayer.class, BufferedMidiMusicPlayer.class.getSuperclass());
        assertTrue(Runnable.class.isAssignableFrom(BufferedMidiMusicPlayer.class));
        assertFalse(Receiver.class.isAssignableFrom(BufferedMidiMusicPlayer.class));
        assertNotNull(BufferedMidiMusicPlayer.class.getDeclaredConstructor(MidiEventSink.class));
        assertEquals(MidiEventSink.class, BufferedMidiMusicPlayer.class.getDeclaredField("eventSink").getType());
    }

    @Test public void sinkInterfaceIsARunnableThatAcceptsEventBatchesAndReportsPosition() throws Exception {
        assertTrue(MidiEventSink.class.isInterface());
        assertTrue(Runnable.class.isAssignableFrom(MidiEventSink.class));
        assertNotNull(MidiEventSink.class.getMethod("writeEvents", int[].class, int.class));
        assertNotNull(MidiEventSink.class.getMethod("getPlaybackPosition", int.class));
        assertNotNull(MidiEventSink.class.getMethod("close"));
        // only the sink-fed player uses it; the javax player and the base never reference it
        for (Class<?> c : new Class<?>[] {MusicPlayer.class, MidiMusicPlayer.class, JavaMidiMusicPlayer.class})
            for (java.lang.reflect.Field f : c.getDeclaredFields()) assertNotEquals(MidiEventSink.class, f.getType());
    }
}
