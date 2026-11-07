/* MuLawInputStream - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */
import java.io.InputStream;

final class MuLawInputStream extends InputStream {
	
    boolean failed;
    private int[] segmentExponents = {
    		0, 0, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4,
    		4, 4, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
    		5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 6, 6,
    		6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
    		6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
    		6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7,
    		7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
    		7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
    		7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
    		7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
    		7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7,
    		7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7, 7
    };
    private byte[] muLawTable = new byte[65536];
    private int[] sampleBuffer = new int[256];
    
    private final byte encodeSample(int sample) {
		int sign = sample >> 8 & 0x80;
		if (sign != 0)
		    sample = -sample;
		if (sample > 32635)
		    sample = 32635;
		sample += 132;
		int exponent = segmentExponents[sample >> 7 & 0xff];
		int mantissa = sample >> exponent + 3 & 0xf;
		byte code = (byte) ((sign | exponent << 4 | mantissa) ^ 0xffffffff);
		return code;
    }
    
    public final synchronized int read(byte[] buffer, int offset, int length) {
		int result;
		try {
		    if (failed)
		    	return -1;
		    if (length > 256) {
		    	read(buffer, offset, 256);
		    	read(buffer, offset + 256, length - 256);
		    	return length;
		    }
		    //Game.method486(sampleBuffer, length);//TODO incase
		    for (int index = 0; index < length; index++) {
		    	int sampleValue = sampleBuffer[index];
		    	if ((sampleValue + 8388608 & ~0xffffff) != 0)
		    		sampleBuffer[index] = 0x7fffff ^ sampleValue >> 31;
		    }
		    encodeSamples(muLawTable, sampleBuffer, buffer, 0, offset, length);
		    result = length;
		} catch (Exception exception) {
			failed = true;
		    return -1;
		}
		return result;
    }
    
    public final int read() {
    	byte[] single = new byte[1];
    	read(single, 0, 1);
    	return single[0];
    }
    
    private static final void encodeSamples(byte[] table, int[] samples, byte[] out, int index, int outOffset, int count) {
    	for (index = 0; index < count; index++)
    		out[outOffset++] = table[(samples[index] >> 8) + 32768];
    }	
    
    MuLawInputStream() {
    	for (int sample = -32768; sample < 32768; sample++)
    		muLawTable[sample + 32768] = encodeSample(sample);
    }
}
