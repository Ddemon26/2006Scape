package audio;

import core.network.Stream;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.*;

public class SoundEnvelopeTest {
    @Test
    public void testDecodeSegments() throws Exception {
        byte[] data = {1, 0,1, 0,2};
        SoundEnvelope env = new SoundEnvelope();
        env.decodeSegments(new Stream(data));

        Field countField = SoundEnvelope.class.getDeclaredField("segmentCount");
        countField.setAccessible(true);
        assertEquals(1, countField.getInt(env));

        Field durField = SoundEnvelope.class.getDeclaredField("segmentDurations");
        durField.setAccessible(true);
        Field phaseField = SoundEnvelope.class.getDeclaredField("segmentPhases");
        phaseField.setAccessible(true);
        assertArrayEquals(new int[]{1}, (int[]) durField.get(env));
        assertArrayEquals(new int[]{2}, (int[]) phaseField.get(env));
    }

    @Test
    public void testStepBasic() {
        byte[] data = {1, 0,1, 0,2};
        SoundEnvelope env = new SoundEnvelope();
        env.decodeSegments(new Stream(data));
        env.reset();
        assertEquals(2, env.step(100));
        assertEquals(2, env.step(100));
    }
}
