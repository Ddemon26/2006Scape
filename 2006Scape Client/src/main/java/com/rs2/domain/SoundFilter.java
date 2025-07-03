package com.rs2.domain;

// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

final class SoundFilter {

    private float interpolateGain(int channel, int index, float f) {
                float f1 = gainCoefficients[channel][0][index] + f * (gainCoefficients[channel][1][index] - gainCoefficients[channel][0][index]);
		f1 *= 0.001525879F;
		return 1.0F - (float) Math.pow(10D, -f1 / 20F);
	}

    private float normalizeFrequency(float f) {
		float f1 = 32.7032F * (float) Math.pow(2D, f);
		return f1 * 3.141593F / 11025F;
	}

    private float interpolateFrequency(float f, int index, int channel) {
                float f1 = filterCoefficients[channel][0][index] + f * (filterCoefficients[channel][1][index] - filterCoefficients[channel][0][index]);
		return normalizeFrequency(f1);
	}

        public int compute(int channel, float f) {
                if (channel == 0) {
                        float f1 = range[0] + (range[1] - range[0]) * f;
                        f1 *= 0.003051758F;
                        forwardMultiplier = (float) Math.pow(0.10000000000000001D, f1 / 20F);
                        forwardMultiplierInt = (int) (forwardMultiplier * 65536F);
                }
                if (filterPairs[channel] == 0) {
                        return 0;
                }
                float f2 = interpolateGain(channel, 0, f);
                coefficients[channel][0] = -2F * f2 *
                                (float) Math.cos(interpolateFrequency(f, 0, channel));
                coefficients[channel][1] = f2 * f2;
                for (int k = 1; k < filterPairs[channel]; k++) {
                        float f3 = interpolateGain(channel, k, f);
                        float f4 = -2F * f3 *
                                        (float) Math.cos(interpolateFrequency(f, k, channel));
                        float f5 = f3 * f3;
                        coefficients[channel][k * 2 + 1] = coefficients[channel][k * 2 - 1] * f5;
                        coefficients[channel][k * 2] =
                                        coefficients[channel][k * 2 - 1] * f4 +
                                                        coefficients[channel][k * 2 - 2] * f5;
                        for (int j1 = k * 2 - 1; j1 >= 2; j1--) {
                                coefficients[channel][j1] +=
                                                coefficients[channel][j1 - 1] * f4 +
                                                                coefficients[channel][j1 - 2] * f5;
                        }

                        coefficients[channel][1] += coefficients[channel][0] * f4 + f5;
                        coefficients[channel][0] += f4;
                }

                if (channel == 0) {
                        for (int l = 0; l < filterPairs[0] * 2; l++) {
                                coefficients[0][l] *= forwardMultiplier;
                        }

                }
                for (int i1 = 0; i1 < filterPairs[channel] * 2; i1++) {
                        intCoefficients[channel][i1] = (int) (coefficients[channel][i1] * 65536F);
                }

                return filterPairs[channel] * 2;
        }

	public void decode(Stream stream, SoundEnvelope class29) {
		int i = stream.readUnsignedByte();
		filterPairs[0] = i >> 4;
		filterPairs[1] = i & 0xf;
		if (i != 0) {
			range[0] = stream.readUnsignedWord();
			range[1] = stream.readUnsignedWord();
			int j = stream.readUnsignedByte();
			for (int k = 0; k < 2; k++) {
				for (int l = 0; l < filterPairs[k]; l++) {
					filterCoefficients[k][0][l] = stream.readUnsignedWord();
					gainCoefficients[k][0][l] = stream.readUnsignedWord();
				}

			}

			for (int i1 = 0; i1 < 2; i1++) {
				for (int j1 = 0; j1 < filterPairs[i1]; j1++) {
					if ((j & 1 << i1 * 4 << j1) != 0) {
						filterCoefficients[i1][1][j1] = stream.readUnsignedWord();
						gainCoefficients[i1][1][j1] = stream.readUnsignedWord();
					} else {
						filterCoefficients[i1][1][j1] = filterCoefficients[i1][0][j1];
						gainCoefficients[i1][1][j1] = gainCoefficients[i1][0][j1];
					}
				}

			}

			if (j != 0 || range[1] != range[0]) {
				class29.decodeSegments(stream);
			}
		} else {
			range[0] = range[1] = 0;
		}
	}

	public SoundFilter() {
		filterPairs = new int[2];
		filterCoefficients = new int[2][2][4];
		gainCoefficients = new int[2][2][4];
		range = new int[2];
	}

	final int[] filterPairs;
	private final int[][][] filterCoefficients;
	private final int[][][] gainCoefficients;
	private final int[] range;
	private static final float[][] coefficients = new float[2][8];
	static final int[][] intCoefficients = new int[2][8];
	private static float forwardMultiplier;
	static int forwardMultiplierInt;

}
