/* MidiFile - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class MidiFile
{
    private static byte[] opcodeSizeTable
	= { 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
	    2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
	    2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1,
	    1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
	    1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
	    2, 2, 0, 1, 2, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    private Stream stream = new Stream(null);
    private int[] trackStatus;
    int timeDivision;
    private int[] trackPositions;
    private long currentTime;
    int[] trackTicks;
    private int[] trackOffsets;
    private int microsecondsPerQuarterNote;
    
    final void readDeltaTime(int i) {
	int i_0_ = stream.readUnsignedByteSub();
	trackTicks[i] += i_0_;
    }
    
    final boolean isTrackFinished() {
	if (stream.currentOffset >= 0)
	    return false;
	return true;
    }
    
    final void saveTrackPosition(int i) {
	trackPositions[i] = stream.currentOffset;
    }
    
    final void clear() {
	stream.buffer = null;
	trackOffsets = null;
	trackPositions = null;
	trackTicks = null;
	trackStatus = null;
    }
    
    private final int readEvent(int i) {
	int i_1_ = (stream.buffer
		    [stream.currentOffset]);
	if (i_1_ < 0) {
	    i_1_ &= 0xff;
	    trackStatus[i] = i_1_;
	    stream.currentOffset++;
	} else
	    i_1_ = trackStatus[i];
	if (i_1_ == 240 || i_1_ == 247) {
	    int i_2_ = stream.readUnsignedByteSub();
	    if (i_1_ == 247 && i_2_ > 0) {
		int i_3_ = ((stream.buffer
			     [stream.currentOffset])
			    & 0xff);
		if (i_3_ >= 241 && i_3_ <= 243 || i_3_ == 246 || i_3_ == 248
		    || i_3_ >= 250 && i_3_ <= 252 || i_3_ == 254) {
		    stream.currentOffset++;
		    trackStatus[i] = i_3_;
		    return parseEvent(i, i_3_);
		}
	    }
	    stream.currentOffset += i_2_;
	    return 0;
	}
	return parseEvent(i, i_1_);
    }
    
    final void load(byte[] is) {
	stream.buffer = is;
	stream.currentOffset = 10;
	int i = stream.readUnsignedWord();
	timeDivision = stream.readUnsignedWord();
	microsecondsPerQuarterNote = 500000;
	trackOffsets = new int[i];
	int i_4_ = 0;
	while (i_4_ < i) {
	    int i_5_ = stream.readDWord();
	    int i_6_ = stream.readDWord();
	    if (i_5_ == 1297379947) {
		trackOffsets[i_4_]
		    = stream.currentOffset;
		i_4_++;
	    }
	    stream.currentOffset += i_6_;
	}
	trackPositions = trackOffsets.clone();
	trackTicks = new int[i];
	trackStatus = new int[i];
    }
    
    final void seekTrack(int i) {
	stream.currentOffset = trackPositions[i];
    }
    
    final boolean isLoaded() {
	if (stream.buffer == null)
	    return false;
	return true;
    }
    
    final void markTrackEnd() {
	stream.currentOffset = -1;
    }
    
    final int getNextEvent(int i) {
	int i_7_ = readEvent(i);
	return i_7_;
    }
    
    public static void reset() {
	opcodeSizeTable = null;
    }
    
    final boolean allTracksFinished() {
	int i = trackPositions.length;
	for (int i_8_ = 0; i_8_ < i; i_8_++) {
	    if (trackPositions[i_8_] >= 0)
		return false;
	}
	return true;
    }
    
    final long getTimeForTick(int i) {
	return currentTime + (long) i * (long) microsecondsPerQuarterNote;
    }
    
    final int getTrackCount() {
	return trackPositions.length;
    }
    
    final void resetTracks(long l) {
	currentTime = l;
	int i = trackPositions.length;
	for (int i_9_ = 0; i_9_ < i; i_9_++) {
	    trackTicks[i_9_] = 0;
	    trackStatus[i_9_] = 0;
	    stream.currentOffset = trackOffsets[i_9_];
	    readDeltaTime(i_9_);
	    trackPositions[i_9_] = stream.currentOffset;
	}
    }
    
    private final int parseEvent(int i, int i_10_) {
	if (i_10_ == 255) {
	    int i_11_ = stream.readUnsignedByte();
	    int i_12_ = stream.readUnsignedByteSub();
	    if (i_11_ == 47) {
		stream.currentOffset += i_12_;
		return 1;
	    }
	    if (i_11_ == 81) {
		int i_13_ = stream.read3Bytes();
		i_12_ -= 3;
		int i_14_ = trackTicks[i];
		currentTime += (long) i_14_ * (long) (microsecondsPerQuarterNote - i_13_);
		microsecondsPerQuarterNote = i_13_;
		stream.currentOffset += i_12_;
		return 2;
	    }
	    stream.currentOffset += i_12_;
	    return 3;
	}
	byte i_15_ = opcodeSizeTable[i_10_ - 128];
	int i_16_ = i_10_;
	if (i_15_ >= 1)
	    i_16_ |= stream.readUnsignedByte() << 8;
	if (i_15_ >= 2)
	    i_16_ |= stream.readUnsignedByte() << 16;
	return i_16_;
    }
    
    final int getNextTrack() {
	int i = trackPositions.length;
	int i_17_ = -1;
	int i_18_ = 2147483647;
	for (int i_19_ = 0; i_19_ < i; i_19_++) {
	    if (trackPositions[i_19_] >= 0
		&& trackTicks[i_19_] < i_18_) {
		i_17_ = i_19_;
		i_18_ = trackTicks[i_19_];
	    }
	}
	return i_17_;
    }
    
    public MidiFile() {
	/* empty */
    }
}
