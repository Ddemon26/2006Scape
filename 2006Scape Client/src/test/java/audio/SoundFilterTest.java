package audio;

import core.network.Stream;
import org.junit.Test;

import static org.junit.Assert.*;

public class SoundFilterTest {
    @Test
    public void testComputeWithNoPairsReturnsZero() {
        SoundFilter filter = new SoundFilter();
        assertEquals(0, filter.compute(0, 0.5f));
    }

    @Test
    public void testDecodeSetsFilterPairs() {
        byte[] data = new byte[] {
            17, // pairs:1,1
            0,1, 0,1, // range[0], range[1]
            0, // j
            0,5, 0,10, // channel 0 coeffs
            0,20, 0,30 // channel 1 coeffs
        };
        SoundEnvelope env = new SoundEnvelope();
        SoundFilter filter = new SoundFilter();
        filter.decode(new Stream(data), env);
        assertEquals(1, filter.filterPairs[0]);
        assertEquals(1, filter.filterPairs[1]);
        assertEquals(1, filter.compute(0, 0f));
    }
}
