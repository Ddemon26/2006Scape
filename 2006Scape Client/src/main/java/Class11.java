/* Class11 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

final class Class11
{
    private static byte[] eventDataLengths
	= { 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
	    2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
	    2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1,
	    1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
	    1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
	    2, 2, 0, 1, 2, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
    private Stream midiStream = new Stream(null);
    private int[] runningStatus;
    int division;
    private int[] trackPositions;
    private long timeBase;
    int[] trackTicks;
    private int[] trackStarts;
    private int tempo;

    final void readDelta(int track) {
	int delta = midiStream.readUnsignedByte128MinusValue();
	trackTicks[track] += delta;
    }

    final boolean isTrackEnded() {
	if (midiStream.currentOffset >= 0)
	    return false;
	return true;
    }

    final void saveTrackPosition(int track) {
	trackPositions[track] = midiStream.currentOffset;
    }

    final void release() {
	midiStream.buffer = null;
	trackStarts = null;
	trackPositions = null;
	trackTicks = null;
	runningStatus = null;
    }

    private final int decodeNextEvent(int track) {
	int status = (midiStream.buffer
		    [midiStream.currentOffset]);
	if (status < 0) {
	    status &= 0xff;
	    runningStatus[track] = status;
	    midiStream.currentOffset++;
	} else
	    status = runningStatus[track];
	if (status == 240 || status == 247) {
	    int length = midiStream.readUnsignedByte128MinusValue();
	    if (status == 247 && length > 0) {
		int nextByte = ((midiStream.buffer
			     [midiStream.currentOffset])
			    & 0xff);
		if (nextByte >= 241 && nextByte <= 243 || nextByte == 246 || nextByte == 248
		    || nextByte >= 250 && nextByte <= 252 || nextByte == 254) {
		    midiStream.currentOffset++;
		    runningStatus[track] = nextByte;
		    return decodeEvent(track, nextByte);
		}
	    }
	    midiStream.currentOffset += length;
	    return 0;
	}
	return decodeEvent(track, status);
    }

    final void load(byte[] midiData) {
	midiStream.buffer = midiData;
	midiStream.currentOffset = 10;
	int trackCount = midiStream.readUnsignedShortBigEndian();
	division = midiStream.readUnsignedShortBigEndian();
	tempo = 500000;
	trackStarts = new int[trackCount];
	int tracksFound = 0;
	while (tracksFound < trackCount) {
	    int chunkTag = midiStream.readIntBigEndian();
	    int chunkLength = midiStream.readIntBigEndian();
	    if (chunkTag == 1297379947) {
		trackStarts[tracksFound]
		    = midiStream.currentOffset;
		tracksFound++;
	    }
	    midiStream.currentOffset += chunkLength;
	}
	trackPositions = trackStarts.clone();
	trackTicks = new int[trackCount];
	runningStatus = new int[trackCount];
    }

    final void restoreTrackPosition(int track) {
	midiStream.currentOffset = trackPositions[track];
    }

    final boolean isLoaded() {
	if (midiStream.buffer == null)
	    return false;
	return true;
    }

    final void markTrackEnded() {
	midiStream.currentOffset = -1;
    }

    final int readEvent(int track) {
	int event = decodeNextEvent(track);
	return event;
    }

    public static void reset() {
	eventDataLengths = null;
    }

    final boolean allTracksEnded() {
	int trackCount = trackPositions.length;
	for (int track = 0; track < trackCount; track++) {
	    if (trackPositions[track] >= 0)
		return false;
	}
	return true;
    }

    final long tickToScaledTime(int tick) {
	return timeBase + (long) tick * (long) tempo;
    }

    final int trackCount() {
	return trackPositions.length;
    }

    final void restart(long startTime) {
	timeBase = startTime;
	int trackCount = trackPositions.length;
	for (int track = 0; track < trackCount; track++) {
	    trackTicks[track] = 0;
	    runningStatus[track] = 0;
	    midiStream.currentOffset = trackStarts[track];
	    readDelta(track);
	    trackPositions[track] = midiStream.currentOffset;
	}
    }

    private final int decodeEvent(int track, int status) {
	if (status == 255) {
	    int metaType = midiStream.readUnsignedByte();
	    int metaLength = midiStream.readUnsignedByte128MinusValue();
	    if (metaType == 47) {
		midiStream.currentOffset += metaLength;
		return 1;
	    }
	    if (metaType == 81) {
		int newTempo = midiStream.readUnsignedMediumBigEndian();
		metaLength -= 3;
		int tick = trackTicks[track];
		timeBase += (long) tick * (long) (tempo - newTempo);
		tempo = newTempo;
		midiStream.currentOffset += metaLength;
		return 2;
	    }
	    midiStream.currentOffset += metaLength;
	    return 3;
	}
	byte dataLength = eventDataLengths[status - 128];
	int message = status;
	if (dataLength >= 1)
	    message |= midiStream.readUnsignedByte() << 8;
	if (dataLength >= 2)
	    message |= midiStream.readUnsignedByte() << 16;
	return message;
    }

    final int nextTrack() {
	int trackCount = trackPositions.length;
	int best = -1;
	int bestTick = 2147483647;
	for (int track = 0; track < trackCount; track++) {
	    if (trackPositions[track] >= 0
		&& trackTicks[track] < bestTick) {
		best = track;
		bestTick = trackTicks[track];
	    }
	}
	return best;
    }

    public Class11() {
	/* empty */
    }
}
