// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

final class SynthFilter {

	private float pairRadius(int direction, int pair, float interpolation) {
		float magnitudeDecibels = pairMagnitudes[direction][0][pair] + interpolation * (pairMagnitudes[direction][1][pair] - pairMagnitudes[direction][0][pair]);
		magnitudeDecibels *= 0.001525879F;
		return 1.0F - (float) Math.pow(10D, -magnitudeDecibels / 20F);
	}

	private float octaveToRadians(float octave) {
		float frequencyHz = 32.7032F * (float) Math.pow(2D, octave);
		return frequencyHz * 3.141593F / 11025F;
	}

	private float pairAngle(float interpolation, int pair, int direction) {
		float octave = pairFrequencies[direction][0][pair] + interpolation * (pairFrequencies[direction][1][pair] - pairFrequencies[direction][0][pair]);
		octave *= 0.0001220703F;
		return octaveToRadians(octave);
	}

	public int computeCoefficients(int direction, float interpolation) {
		if (direction == 0) {
			float gainDecibels = gainStartEnd[0] + (gainStartEnd[1] - gainStartEnd[0]) * interpolation;
			gainDecibels *= 0.003051758F;
			gainFactor = (float) Math.pow(0.10000000000000001D, gainDecibels / 20F);
			gainFactorFixed = (int) (gainFactor * 65536F);
		}
		if (pairCounts[direction] == 0) {
			return 0;
		}
		float firstRadius = pairRadius(direction, 0, interpolation);
		floatCoefficients[direction][0] = -2F * firstRadius * (float) Math.cos(pairAngle(interpolation, 0, direction));
		floatCoefficients[direction][1] = firstRadius * firstRadius;
		for (int pair = 1; pair < pairCounts[direction]; pair++) {
			float radius = pairRadius(direction, pair, interpolation);
			float linearTerm = -2F * radius * (float) Math.cos(pairAngle(interpolation, pair, direction));
			float radiusSquared = radius * radius;
			floatCoefficients[direction][pair * 2 + 1] = floatCoefficients[direction][pair * 2 - 1] * radiusSquared;
			floatCoefficients[direction][pair * 2] = floatCoefficients[direction][pair * 2 - 1] * linearTerm + floatCoefficients[direction][pair * 2 - 2] * radiusSquared;
			for (int coefficientIndex = pair * 2 - 1; coefficientIndex >= 2; coefficientIndex--) {
				floatCoefficients[direction][coefficientIndex] += floatCoefficients[direction][coefficientIndex - 1] * linearTerm + floatCoefficients[direction][coefficientIndex - 2] * radiusSquared;
			}

			floatCoefficients[direction][1] += floatCoefficients[direction][0] * linearTerm + radiusSquared;
			floatCoefficients[direction][0] += linearTerm;
		}

		if (direction == 0) {
			for (int coefficientIndex = 0; coefficientIndex < pairCounts[0] * 2; coefficientIndex++) {
				floatCoefficients[0][coefficientIndex] *= gainFactor;
			}

		}
		for (int coefficientIndex = 0; coefficientIndex < pairCounts[direction] * 2; coefficientIndex++) {
			coefficients[direction][coefficientIndex] = (int) (floatCoefficients[direction][coefficientIndex] * 65536F);
		}

		return pairCounts[direction] * 2;
	}

	public void decode(Stream stream, SynthEnvelope envelope) {
		int pairCountsByte = stream.readUnsignedByte();
		pairCounts[0] = pairCountsByte >> 4;
		pairCounts[1] = pairCountsByte & 0xf;
		if (pairCountsByte != 0) {
			gainStartEnd[0] = stream.readUnsignedShortBigEndian();
			gainStartEnd[1] = stream.readUnsignedShortBigEndian();
			int endMask = stream.readUnsignedByte();
			for (int direction = 0; direction < 2; direction++) {
				for (int pair = 0; pair < pairCounts[direction]; pair++) {
					pairFrequencies[direction][0][pair] = stream.readUnsignedShortBigEndian();
					pairMagnitudes[direction][0][pair] = stream.readUnsignedShortBigEndian();
				}

			}

			for (int direction = 0; direction < 2; direction++) {
				for (int pair = 0; pair < pairCounts[direction]; pair++) {
					if ((endMask & 1 << direction * 4 << pair) != 0) {
						pairFrequencies[direction][1][pair] = stream.readUnsignedShortBigEndian();
						pairMagnitudes[direction][1][pair] = stream.readUnsignedShortBigEndian();
					} else {
						pairFrequencies[direction][1][pair] = pairFrequencies[direction][0][pair];
						pairMagnitudes[direction][1][pair] = pairMagnitudes[direction][0][pair];
					}
				}

			}

			if (endMask != 0 || gainStartEnd[1] != gainStartEnd[0]) {
				envelope.decodePoints(stream);
			}
		} else {
			gainStartEnd[0] = gainStartEnd[1] = 0;
		}
	}

	public SynthFilter() {
		pairCounts = new int[2];
		pairFrequencies = new int[2][2][4];
		pairMagnitudes = new int[2][2][4];
		gainStartEnd = new int[2];
	}

	final int[] pairCounts;
	private final int[][][] pairFrequencies;
	private final int[][][] pairMagnitudes;
	private final int[] gainStartEnd;
	private static final float[][] floatCoefficients = new float[2][8];
	static final int[][] coefficients = new int[2][8];
	private static float gainFactor;
	static int gainFactorFixed;

}
