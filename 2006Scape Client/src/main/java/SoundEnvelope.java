// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

final class SoundEnvelope {

	public void decode(Stream stream) {
		form = stream.readUnsignedByte();
		start = stream.readDWord();
		end = stream.readDWord();
		decodeSegments(stream);
	}

	public void decodeSegments(Stream stream) {
		segmentCount = stream.readUnsignedByte();
		segmentDurations = new int[segmentCount];
		segmentPhases = new int[segmentCount];
		for (int i = 0; i < segmentCount; i++) {
			segmentDurations[i] = stream.readUnsignedWord();
			segmentPhases[i] = stream.readUnsignedWord();
		}

	}

	void reset() {
		ticks = 0;
		phase = 0;
		increment = 0;
		amplitude = 0;
		time = 0;
	}

	int step(int i) {
		if (time >= ticks) {
			amplitude = segmentPhases[phase++] << 15;
			if (phase >= segmentCount) {
				phase = segmentCount - 1;
			}
			ticks = (int) (segmentDurations[phase] / 65536D * i);
			if (ticks > time) {
				increment = ((segmentPhases[phase] << 15) - amplitude) / (ticks - time);
			}
		}
		amplitude += increment;
		time++;
		return amplitude - increment >> 15;
	}

	public SoundEnvelope() {
	}

	private int segmentCount;
	private int[] segmentDurations;
	private int[] segmentPhases;
	int start;
	int end;
	int form;
	private int ticks;
	private int phase;
	private int increment;
	private int amplitude;
	private int time;
	public static int unused;
}
