// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

final class SynthEnvelope {

	public void decode(Stream stream) {
		waveform = stream.readUnsignedByte();
		rangeStart = stream.readIntBigEndian();
		rangeEnd = stream.readIntBigEndian();
		decodePoints(stream);
	}

	public void decodePoints(Stream stream) {
		pointCount = stream.readUnsignedByte();
		pointPositions = new int[pointCount];
		pointValues = new int[pointCount];
		for (int point = 0; point < pointCount; point++) {
			pointPositions[point] = stream.readUnsignedShortBigEndian();
			pointValues[point] = stream.readUnsignedShortBigEndian();
		}

	}

	void resetValues() {
		segmentEndSample = 0;
		targetPoint = 0;
		stepPerSample = 0;
		currentValue = 0;
		sampleIndex = 0;
	}

	int nextValue(int totalSamples) {
		if (sampleIndex >= segmentEndSample) {
			currentValue = pointValues[targetPoint++] << 15;
			if (targetPoint >= pointCount) {
				targetPoint = pointCount - 1;
			}
			segmentEndSample = (int) (pointPositions[targetPoint] / 65536D * totalSamples);
			if (segmentEndSample > sampleIndex) {
				stepPerSample = ((pointValues[targetPoint] << 15) - currentValue) / (segmentEndSample - sampleIndex);
			}
		}
		currentValue += stepPerSample;
		sampleIndex++;
		return currentValue - stepPerSample >> 15;
	}

	public SynthEnvelope() {
	}

	private int pointCount;
	private int[] pointPositions;
	private int[] pointValues;
	int rangeStart;
	int rangeEnd;
	int waveform;
	private int segmentEndSample;
	private int targetPoint;
	private int stepPerSample;
	private int currentValue;
	private int sampleIndex;
	public static int anInt546;
}
