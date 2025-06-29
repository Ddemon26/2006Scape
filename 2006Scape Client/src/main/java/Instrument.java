// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

final class Instrument {

    public static void initializeTables() {
		noiseTable = new int[32768];
		for (int i = 0; i < 32768; i++) {
			if (Math.random() > 0.5D) {
				noiseTable[i] = 1;
			} else {
				noiseTable[i] = -1;
			}
		}

		sineTable = new int[32768];
		for (int j = 0; j < 32768; j++) {
			sineTable[j] = (int) (Math.sin(j / 5215.1903000000002D) * 16384D);
		}

		sampleBuffer = new int[0x35d54];
	}

    public int[] synthesize(int length, int rate) {
		for (int k = 0; k < length; k++) {
			sampleBuffer[k] = 0;
		}

		if (rate < 10) {
			return sampleBuffer;
		}
		double d = length / (rate + 0.0D);
		pitchEnvelope.reset();
		volumeEnvelope.reset();
		int l = 0;
		int i1 = 0;
		int j1 = 0;
		if (pitchModifierEnvelope != null) {
			pitchModifierEnvelope.reset();
			pitchModifierAmplitude.reset();
			l = (int) ((pitchModifierEnvelope.end - pitchModifierEnvelope.start) * 32.768000000000001D / d);
			i1 = (int) (pitchModifierEnvelope.start * 32.768000000000001D / d);
		}
		int k1 = 0;
		int l1 = 0;
		int i2 = 0;
		if (volumeModifierEnvelope != null) {
			volumeModifierEnvelope.reset();
			volumeModifierAmplitude.reset();
			k1 = (int) ((volumeModifierEnvelope.end - volumeModifierEnvelope.start) * 32.768000000000001D / d);
			l1 = (int) (volumeModifierEnvelope.start * 32.768000000000001D / d);
		}
		for (int j2 = 0; j2 < 5; j2++) {
			if (oscillatorVolume[j2] != 0) {
				oscillatorPhase[j2] = 0;
				oscillatorDelaySamples[j2] = (int) (oscillatorDelay[j2] * d);
				oscillatorVolumeStep[j2] = (oscillatorVolume[j2] << 14) / 100;
				oscillatorPitchStep[j2] = (int) ((pitchEnvelope.end - pitchEnvelope.start) * 32.768000000000001D * Math.pow(1.0057929410678534D, oscillatorPitch[j2]) / d);
				oscillatorPitchBase[j2] = (int) (pitchEnvelope.start * 32.768000000000001D / d);
			}
		}

		for (int k2 = 0; k2 < length; k2++) {
			int l2 = pitchEnvelope.step(length);
			int j4 = volumeEnvelope.step(length);
			if (pitchModifierEnvelope != null) {
				int j5 = pitchModifierEnvelope.step(length);
				int j6 = pitchModifierAmplitude.step(length);
				l2 += evaluateWaveform(j6, j1, pitchModifierEnvelope.form) >> 1;
				j1 += (j5 * l >> 16) + i1;
			}
			if (volumeModifierEnvelope != null) {
				int k5 = volumeModifierEnvelope.step(length);
				int k6 = volumeModifierAmplitude.step(length);
				j4 = j4 * ((evaluateWaveform(k6, i2, volumeModifierEnvelope.form) >> 1) + 32768) >> 15;
				i2 += (k5 * k1 >> 16) + l1;
			}
			for (int l5 = 0; l5 < 5; l5++) {
				if (oscillatorVolume[l5] != 0) {
					int l6 = k2 + oscillatorDelaySamples[l5];
					if (l6 < length) {
						sampleBuffer[l6] += evaluateWaveform(j4 * oscillatorVolumeStep[l5] >> 15, oscillatorPhase[l5], pitchEnvelope.form);
						oscillatorPhase[l5] += (l2 * oscillatorPitchStep[l5] >> 16) + oscillatorPitchBase[l5];
					}
				}
			}

		}

		if (releaseEnvelope != null) {
			releaseEnvelope.reset();
			attackEnvelope.reset();
			int i3 = 0;
			boolean flag1 = true;
			for (int i7 = 0; i7 < length; i7++) {
				int k7 = releaseEnvelope.step(length);
				int i8 = attackEnvelope.step(length);
				int k4;
				if (flag1) {
					k4 = releaseEnvelope.start + ((releaseEnvelope.end - releaseEnvelope.start) * k7 >> 8);
				} else {
					k4 = releaseEnvelope.start + ((releaseEnvelope.end - releaseEnvelope.start) * i8 >> 8);
				}
				if ((i3 += 256) >= k4) {
					i3 = 0;
					flag1 = !flag1;
				}
				if (flag1) {
					sampleBuffer[i7] = 0;
				}
			}

		}
		if (delayTime > 0 && delayDecay > 0) {
			int j3 = (int) (delayTime * d);
			for (int l4 = j3; l4 < length; l4++) {
				sampleBuffer[l4] += sampleBuffer[l4 - j3] * delayDecay / 100;
			}

		}
		if (soundFilter.filterPairs[0] > 0 || soundFilter.filterPairs[1] > 0) {
			filterEnvelope.reset();
			int k3 = filterEnvelope.step(length + 1);
			int i5 = soundFilter.compute(0, k3 / 65536F);
			int i6 = soundFilter.compute(1, k3 / 65536F);
			if (length >= i5 + i6) {
				int j7 = 0;
				int l7 = i6;
				if (l7 > length - i5) {
					l7 = length - i5;
				}
				for (; j7 < l7; j7++) {
					int j8 = (int) ((long) sampleBuffer[j7 + i5] * (long) SoundFilter.forwardMultiplierInt >> 16);
					for (int k8 = 0; k8 < i5; k8++) {
						j8 += (int) ((long) sampleBuffer[j7 + i5 - 1 - k8] * (long) SoundFilter.intCoefficients[0][k8] >> 16);
					}

					for (int j9 = 0; j9 < j7; j9++) {
						j8 -= (int) ((long) sampleBuffer[j7 - 1 - j9] * (long) SoundFilter.intCoefficients[1][j9] >> 16);
					}

					sampleBuffer[j7] = j8;
					k3 = filterEnvelope.step(length + 1);
				}

				char c = '\200';
				l7 = c;
				do {
					if (l7 > length - i5) {
						l7 = length - i5;
					}
					for (; j7 < l7; j7++) {
						int l8 = (int) ((long) sampleBuffer[j7 + i5] * (long) SoundFilter.forwardMultiplierInt >> 16);
						for (int k9 = 0; k9 < i5; k9++) {
							l8 += (int) ((long) sampleBuffer[j7 + i5 - 1 - k9] * (long) SoundFilter.intCoefficients[0][k9] >> 16);
						}

						for (int i10 = 0; i10 < i6; i10++) {
							l8 -= (int) ((long) sampleBuffer[j7 - 1 - i10] * (long) SoundFilter.intCoefficients[1][i10] >> 16);
						}

						sampleBuffer[j7] = l8;
						k3 = filterEnvelope.step(length + 1);
					}

					if (j7 >= length - i5) {
						break;
					}
					i5 = soundFilter.compute(0, k3 / 65536F);
					i6 = soundFilter.compute(1, k3 / 65536F);
					l7 += c;
				} while (true);
				for (; j7 < length; j7++) {
					int i9 = 0;
					for (int l9 = j7 + i5 - length; l9 < i5; l9++) {
						i9 += (int) ((long) sampleBuffer[j7 + i5 - 1 - l9] * (long) SoundFilter.intCoefficients[0][l9] >> 16);
					}

					for (int j10 = 0; j10 < i6; j10++) {
						i9 -= (int) ((long) sampleBuffer[j7 - 1 - j10] * (long) SoundFilter.intCoefficients[1][j10] >> 16);
					}

					sampleBuffer[j7] = i9;
					filterEnvelope.step(length + 1);
				}

			}
		}
		for (int i4 = 0; i4 < length; i4++) {
			if (sampleBuffer[i4] < -32768) {
				sampleBuffer[i4] = -32768;
			}
			if (sampleBuffer[i4] > 32767) {
				sampleBuffer[i4] = 32767;
			}
		}

		return sampleBuffer;
	}

        private int evaluateWaveform(int amplitude, int phase, int type) {
                if (type == 1) {
                        if ((phase & 0x7fff) < 16384) {
                                return amplitude;
                        } else {
                                return -amplitude;
                        }
                }
                if (type == 2) {
                        return sineTable[phase & 0x7fff] * amplitude >> 14;
                }
                if (type == 3) {
                        return ((phase & 0x7fff) * amplitude >> 14) - amplitude;
                }
                if (type == 4) {
                        return noiseTable[phase / 2607 & 0x7fff] * amplitude;
                } else {
                        return 0;
                }
        }

	public void decode(Stream stream) {
		pitchEnvelope = new SoundEnvelope();
		pitchEnvelope.decode(stream);
		volumeEnvelope = new SoundEnvelope();
		volumeEnvelope.decode(stream);
		int i = stream.readUnsignedByte();
		if (i != 0) {
			stream.currentOffset--;
			pitchModifierEnvelope = new SoundEnvelope();
			pitchModifierEnvelope.decode(stream);
			pitchModifierAmplitude = new SoundEnvelope();
			pitchModifierAmplitude.decode(stream);
		}
		i = stream.readUnsignedByte();
		if (i != 0) {
			stream.currentOffset--;
			volumeModifierEnvelope = new SoundEnvelope();
			volumeModifierEnvelope.decode(stream);
			volumeModifierAmplitude = new SoundEnvelope();
			volumeModifierAmplitude.decode(stream);
		}
		i = stream.readUnsignedByte();
		if (i != 0) {
			stream.currentOffset--;
			releaseEnvelope = new SoundEnvelope();
			releaseEnvelope.decode(stream);
			attackEnvelope = new SoundEnvelope();
			attackEnvelope.decode(stream);
		}
		for (int j = 0; j < 10; j++) {
			int k = stream.method422();
			if (k == 0) {
				break;
			}
			oscillatorVolume[j] = k;
			oscillatorPitch[j] = stream.method421();
			oscillatorDelay[j] = stream.method422();
		}

		delayTime = stream.method422();
		delayDecay = stream.method422();
		duration = stream.readUnsignedWord();
		offset = stream.readUnsignedWord();
		soundFilter = new SoundFilter();
		filterEnvelope = new SoundEnvelope();
		soundFilter.decode(stream, filterEnvelope);
	}

	public Instrument() {
		oscillatorVolume = new int[5];
		oscillatorPitch = new int[5];
		oscillatorDelay = new int[5];
		delayDecay = 100;
		duration = 500;
	}

	private SoundEnvelope pitchEnvelope;
	private SoundEnvelope volumeEnvelope;
	private SoundEnvelope pitchModifierEnvelope;
	private SoundEnvelope pitchModifierAmplitude;
	private SoundEnvelope volumeModifierEnvelope;
	private SoundEnvelope volumeModifierAmplitude;
	private SoundEnvelope releaseEnvelope;
	private SoundEnvelope attackEnvelope;
	private final int[] oscillatorVolume;
	private final int[] oscillatorPitch;
	private final int[] oscillatorDelay;
	private int delayTime;
	private int delayDecay;
	private SoundFilter soundFilter;
	private SoundEnvelope filterEnvelope;
	int duration;
	int offset;
	private static int[] sampleBuffer;
	private static int[] noiseTable;
	private static int[] sineTable;
	private static final int[] oscillatorPhase = new int[5];
	private static final int[] oscillatorDelaySamples = new int[5];
	private static final int[] oscillatorVolumeStep = new int[5];
	private static final int[] oscillatorPitchStep = new int[5];
	private static final int[] oscillatorPitchBase = new int[5];

}
