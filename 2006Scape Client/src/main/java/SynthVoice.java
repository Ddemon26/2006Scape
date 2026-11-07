// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

final class SynthVoice {

	public static void initWaveTables() {
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

	public int[] synthesize(int sampleCount, int lengthMs) {
		for (int sample = 0; sample < sampleCount; sample++) {
			sampleBuffer[sample] = 0;
		}

		if (lengthMs < 10) {
			return sampleBuffer;
		}
		double samplesPerMs = sampleCount / (lengthMs + 0.0D);
		pitchEnvelope.resetValues();
		amplitudeEnvelope.resetValues();
		int vibratoSweepScale = 0;
		int vibratoBaseStep = 0;
		int vibratoPhase = 0;
		if (vibratoRateEnvelope != null) {
			vibratoRateEnvelope.resetValues();
			vibratoDepthEnvelope.resetValues();
			vibratoSweepScale = (int) ((vibratoRateEnvelope.anInt539 - vibratoRateEnvelope.anInt538) * 32.768000000000001D / samplesPerMs);
			vibratoBaseStep = (int) (vibratoRateEnvelope.anInt538 * 32.768000000000001D / samplesPerMs);
		}
		int tremoloSweepScale = 0;
		int tremoloBaseStep = 0;
		int tremoloPhase = 0;
		if (tremoloRateEnvelope != null) {
			tremoloRateEnvelope.resetValues();
			tremoloDepthEnvelope.resetValues();
			tremoloSweepScale = (int) ((tremoloRateEnvelope.anInt539 - tremoloRateEnvelope.anInt538) * 32.768000000000001D / samplesPerMs);
			tremoloBaseStep = (int) (tremoloRateEnvelope.anInt538 * 32.768000000000001D / samplesPerMs);
		}
		for (int harmonic = 0; harmonic < 5; harmonic++) {
			if (harmonicVolumes[harmonic] != 0) {
				harmonicPhases[harmonic] = 0;
				harmonicDelaySamples[harmonic] = (int) (harmonicDelays[harmonic] * samplesPerMs);
				harmonicVolumeScales[harmonic] = (harmonicVolumes[harmonic] << 14) / 100;
				harmonicPhaseSweepScales[harmonic] = (int) ((pitchEnvelope.anInt539 - pitchEnvelope.anInt538) * 32.768000000000001D * Math.pow(1.0057929410678534D, harmonicPitchOffsets[harmonic]) / samplesPerMs);
				harmonicPhaseBaseSteps[harmonic] = (int) (pitchEnvelope.anInt538 * 32.768000000000001D / samplesPerMs);
			}
		}

		for (int sample = 0; sample < sampleCount; sample++) {
			int pitch = pitchEnvelope.method328(sampleCount);
			int amplitude = amplitudeEnvelope.method328(sampleCount);
			if (vibratoRateEnvelope != null) {
				int vibratoRate = vibratoRateEnvelope.method328(sampleCount);
				int vibratoDepth = vibratoDepthEnvelope.method328(sampleCount);
				pitch += oscillate(vibratoDepth, vibratoPhase, vibratoRateEnvelope.anInt540) >> 1;
				vibratoPhase += (vibratoRate * vibratoSweepScale >> 16) + vibratoBaseStep;
			}
			if (tremoloRateEnvelope != null) {
				int tremoloRate = tremoloRateEnvelope.method328(sampleCount);
				int tremoloDepth = tremoloDepthEnvelope.method328(sampleCount);
				amplitude = amplitude * ((oscillate(tremoloDepth, tremoloPhase, tremoloRateEnvelope.anInt540) >> 1) + 32768) >> 15;
				tremoloPhase += (tremoloRate * tremoloSweepScale >> 16) + tremoloBaseStep;
			}
			for (int harmonic = 0; harmonic < 5; harmonic++) {
				if (harmonicVolumes[harmonic] != 0) {
					int outputIndex = sample + harmonicDelaySamples[harmonic];
					if (outputIndex < sampleCount) {
						sampleBuffer[outputIndex] += oscillate(amplitude * harmonicVolumeScales[harmonic] >> 15, harmonicPhases[harmonic], pitchEnvelope.anInt540);
						harmonicPhases[harmonic] += (pitch * harmonicPhaseSweepScales[harmonic] >> 16) + harmonicPhaseBaseSteps[harmonic];
					}
				}
			}

		}

		if (gateSilenceEnvelope != null) {
			gateSilenceEnvelope.resetValues();
			gateSoundEnvelope.resetValues();
			int gateCounter = 0;
			boolean gateSilent = true;
			for (int sample = 0; sample < sampleCount; sample++) {
				int silenceValue = gateSilenceEnvelope.method328(sampleCount);
				int soundValue = gateSoundEnvelope.method328(sampleCount);
				int spanLimit;
				if (gateSilent) {
					spanLimit = gateSilenceEnvelope.anInt538 + ((gateSilenceEnvelope.anInt539 - gateSilenceEnvelope.anInt538) * silenceValue >> 8);
				} else {
					spanLimit = gateSilenceEnvelope.anInt538 + ((gateSilenceEnvelope.anInt539 - gateSilenceEnvelope.anInt538) * soundValue >> 8);
				}
				if ((gateCounter += 256) >= spanLimit) {
					gateCounter = 0;
					gateSilent = !gateSilent;
				}
				if (gateSilent) {
					sampleBuffer[sample] = 0;
				}
			}

		}
		if (echoDelay > 0 && echoFeedback > 0) {
			int echoDelaySamples = (int) (echoDelay * samplesPerMs);
			for (int sample = echoDelaySamples; sample < sampleCount; sample++) {
				sampleBuffer[sample] += sampleBuffer[sample - echoDelaySamples] * echoFeedback / 100;
			}

		}
		if (filter.anIntArray665[0] > 0 || filter.anIntArray665[1] > 0) {
			filterEnvelope.resetValues();
			int k3 = filterEnvelope.method328(sampleCount + 1);
			int i5 = filter.method544(0, k3 / 65536F);
			int i6 = filter.method544(1, k3 / 65536F);
			if (sampleCount >= i5 + i6) {
				int j7 = 0;
				int l7 = i6;
				if (l7 > sampleCount - i5) {
					l7 = sampleCount - i5;
				}
				for (; j7 < l7; j7++) {
					int j8 = (int) ((long) sampleBuffer[j7 + i5] * (long) Class39.anInt672 >> 16);
					for (int k8 = 0; k8 < i5; k8++) {
						j8 += (int) ((long) sampleBuffer[j7 + i5 - 1 - k8] * (long) Class39.anIntArrayArray670[0][k8] >> 16);
					}

					for (int j9 = 0; j9 < j7; j9++) {
						j8 -= (int) ((long) sampleBuffer[j7 - 1 - j9] * (long) Class39.anIntArrayArray670[1][j9] >> 16);
					}

					sampleBuffer[j7] = j8;
					k3 = filterEnvelope.method328(sampleCount + 1);
				}

				char c = '\200';
				l7 = c;
				do {
					if (l7 > sampleCount - i5) {
						l7 = sampleCount - i5;
					}
					for (; j7 < l7; j7++) {
						int l8 = (int) ((long) sampleBuffer[j7 + i5] * (long) Class39.anInt672 >> 16);
						for (int k9 = 0; k9 < i5; k9++) {
							l8 += (int) ((long) sampleBuffer[j7 + i5 - 1 - k9] * (long) Class39.anIntArrayArray670[0][k9] >> 16);
						}

						for (int i10 = 0; i10 < i6; i10++) {
							l8 -= (int) ((long) sampleBuffer[j7 - 1 - i10] * (long) Class39.anIntArrayArray670[1][i10] >> 16);
						}

						sampleBuffer[j7] = l8;
						k3 = filterEnvelope.method328(sampleCount + 1);
					}

					if (j7 >= sampleCount - i5) {
						break;
					}
					i5 = filter.method544(0, k3 / 65536F);
					i6 = filter.method544(1, k3 / 65536F);
					l7 += c;
				} while (true);
				for (; j7 < sampleCount; j7++) {
					int i9 = 0;
					for (int l9 = j7 + i5 - sampleCount; l9 < i5; l9++) {
						i9 += (int) ((long) sampleBuffer[j7 + i5 - 1 - l9] * (long) Class39.anIntArrayArray670[0][l9] >> 16);
					}

					for (int j10 = 0; j10 < i6; j10++) {
						i9 -= (int) ((long) sampleBuffer[j7 - 1 - j10] * (long) Class39.anIntArrayArray670[1][j10] >> 16);
					}

					sampleBuffer[j7] = i9;
					filterEnvelope.method328(sampleCount + 1);
				}

			}
		}
		for (int sample = 0; sample < sampleCount; sample++) {
			if (sampleBuffer[sample] < -32768) {
				sampleBuffer[sample] = -32768;
			}
			if (sampleBuffer[sample] > 32767) {
				sampleBuffer[sample] = 32767;
			}
		}

		return sampleBuffer;
	}

	private int oscillate(int amplitude, int phase, int waveform) {
		if (waveform == 1) {
			if ((phase & 0x7fff) < 16384) {
				return amplitude;
			} else {
				return -amplitude;
			}
		}
		if (waveform == 2) {
			return sineTable[phase & 0x7fff] * amplitude >> 14;
		}
		if (waveform == 3) {
			return ((phase & 0x7fff) * amplitude >> 14) - amplitude;
		}
		if (waveform == 4) {
			return noiseTable[phase / 2607 & 0x7fff] * amplitude;
		} else {
			return 0;
		}
	}

	public void decode(Stream stream) {
		pitchEnvelope = new Class29();
		pitchEnvelope.method325(stream);
		amplitudeEnvelope = new Class29();
		amplitudeEnvelope.method325(stream);
		int i = stream.readUnsignedByte();
		if (i != 0) {
			stream.currentOffset--;
			vibratoRateEnvelope = new Class29();
			vibratoRateEnvelope.method325(stream);
			vibratoDepthEnvelope = new Class29();
			vibratoDepthEnvelope.method325(stream);
		}
		i = stream.readUnsignedByte();
		if (i != 0) {
			stream.currentOffset--;
			tremoloRateEnvelope = new Class29();
			tremoloRateEnvelope.method325(stream);
			tremoloDepthEnvelope = new Class29();
			tremoloDepthEnvelope.method325(stream);
		}
		i = stream.readUnsignedByte();
		if (i != 0) {
			stream.currentOffset--;
			gateSilenceEnvelope = new Class29();
			gateSilenceEnvelope.method325(stream);
			gateSoundEnvelope = new Class29();
			gateSoundEnvelope.method325(stream);
		}
		for (int j = 0; j < 10; j++) {
			int k = stream.readUnsignedSmart();
			if (k == 0) {
				break;
			}
			harmonicVolumes[j] = k;
			harmonicPitchOffsets[j] = stream.readSignedSmart();
			harmonicDelays[j] = stream.readUnsignedSmart();
		}

		echoDelay = stream.readUnsignedSmart();
		echoFeedback = stream.readUnsignedSmart();
		durationMs = stream.readUnsignedShortBigEndian();
		startOffsetMs = stream.readUnsignedShortBigEndian();
		filter = new Class39();
		filterEnvelope = new Class29();
		filter.method545(stream, filterEnvelope);
	}

	public SynthVoice() {
		harmonicVolumes = new int[5];
		harmonicPitchOffsets = new int[5];
		harmonicDelays = new int[5];
		echoFeedback = 100;
		durationMs = 500;
	}

	private Class29 pitchEnvelope;
	private Class29 amplitudeEnvelope;
	private Class29 vibratoRateEnvelope;
	private Class29 vibratoDepthEnvelope;
	private Class29 tremoloRateEnvelope;
	private Class29 tremoloDepthEnvelope;
	private Class29 gateSilenceEnvelope;
	private Class29 gateSoundEnvelope;
	private final int[] harmonicVolumes;
	private final int[] harmonicPitchOffsets;
	private final int[] harmonicDelays;
	private int echoDelay;
	private int echoFeedback;
	private Class39 filter;
	private Class29 filterEnvelope;
	int durationMs;
	int startOffsetMs;
	private static int[] sampleBuffer;
	private static int[] noiseTable;
	private static int[] sineTable;
	private static final int[] harmonicPhases = new int[5];
	private static final int[] harmonicDelaySamples = new int[5];
	private static final int[] harmonicVolumeScales = new int[5];
	private static final int[] harmonicPhaseSweepScales = new int[5];
	private static final int[] harmonicPhaseBaseSteps = new int[5];

}
