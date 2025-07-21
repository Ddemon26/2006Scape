package audio;

import org.junit.Test;

import static org.junit.Assert.*;

public class MuLawInputStreamTest {
    @Test
    public void testReadReturnsEncodedSample() {
        MuLawInputStream stream = new MuLawInputStream();
        byte[] buf = new byte[1];
        int read = stream.read(buf, 0, 1);
        assertEquals(1, read);
        assertEquals((byte)0xFF, buf[0]);
    }

    @Test
    public void testReadWhenErrored() {
        MuLawInputStream stream = new MuLawInputStream();
        stream.hasError = true;
        byte[] buf = new byte[1];
        int read = stream.read(buf, 0, 1);
        assertEquals(-1, read);
    }
}
