package com.rs2.domain;

/* MuLawInputStream - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */
import java.io.InputStream;

/**
 * Converts 16-bit PCM samples stored in an integer buffer to 8-bit
 * mu-law encoded bytes.
 */
final class MuLawInputStream extends InputStream {

    /** Set when an unrecoverable error occurs. */
    boolean hasError;

    /** Lookup table used during mu-law exponent calculation. */
    private int[] segmentLookup = {
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

    /** Mapping from signed 16-bit PCM to mu-law byte. */
    private byte[] muLawTable = new byte[65536];

    /** PCM samples waiting to be encoded. */
    private int[] sampleBuffer = new int[256];
    
    private final byte encodeSample(int i) {
		int i_0_ = i >> 8 & 0x80;
		if (i_0_ != 0)
		    i = -i;
		if (i > 32635)
		    i = 32635;
		i += 132;
                int i_1_ = segmentLookup[i >> 7 & 0xff];
		int i_2_ = i >> i_1_ + 3 & 0xf;
		byte i_3_ = (byte) ((i_0_ | i_1_ << 4 | i_2_) ^ 0xffffffff);
		return i_3_;
    }
    
    public final synchronized int read(byte[] is, int i, int i_4_) {
		int i_5_;
		try {
                    if (hasError)
		    	return -1;
		    if (i_4_ > 256) {
                        read(is, i, 256);
                        read(is, i + 256, i_4_ - 256);
		    	return i_4_;
		    }
                    //Game.method486(sampleBuffer, i_4_);//TODO incase
                    for (int i_6_ = 0; i_6_ < i_4_; i_6_++) {
                        int i_7_ = sampleBuffer[i_6_];
                        if ((i_7_ + 8388608 & ~0xffffff) != 0)
                                sampleBuffer[i_6_] = 0x7fffff ^ i_7_ >> 31;
                    }
                    encodeBuffer(muLawTable, sampleBuffer, is, 0, i, i_4_);
		    i_5_ = i_4_;
                } catch (Exception exception) {
                        hasError = true;
                    return -1;
                }
                return i_5_;
    }
    
    public final int read() {
    	byte[] is = new byte[1];
    	read(is, 0, 1);
    	return is[0];
    }
    
    private static final void encodeBuffer(byte[] is, int[] is_8_, byte[] is_9_, int i, int i_10_, int i_11_) {
    	for (i = 0; i < i_11_; i++)
    		is_9_[i_10_++] = is[(is_8_[i] >> 8) + 32768];
    }	
    
    MuLawInputStream() {
        for (int i = -32768; i < 32768; i++)
                muLawTable[i + 32768] = encodeSample(i);
    }
}
