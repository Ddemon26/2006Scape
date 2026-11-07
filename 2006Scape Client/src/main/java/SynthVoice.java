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
			vibratoSweepScale = (int) ((vibratoRateEnvelope.rangeEnd - vibratoRateEnvelope.rangeStart) * 32.768000000000001D / samplesPerMs);
			vibratoBaseStep = (int) (vibratoRateEnvelope.rangeStart * 32.768000000000001D / samplesPerMs);
		}
		int tremoloSweepScale = 0;
		int tremoloBaseStep = 0;
		int tremoloPhase = 0;
		if (tremoloRateEnvelope != null) {
			tremoloRateEnvelope.resetValues();
			tremoloDepthEnvelope.resetValues();
			tremoloSweepScale = (int) ((tremoloRateEnvelope.rangeEnd - tremoloRateEnvelope.rangeStart) * 32.768000000000001D / samplesPerMs);
			tremoloBaseStep = (int) (tremoloRateEnvelope.rangeStart * 32.768000000000001D / samplesPerMs);
		}
		for (int harmonic = 0; harmonic < 5; harmonic++) {
			if (harmonicVolumes[harmonic] != 0) {
				harmonicPhases[harmonic] = 0;
				harmonicDelaySamples[harmonic] = (int) (harmonicDelays[harmonic] * samplesPerMs);
				harmonicVolumeScales[harmonic] = (harmonicVolumes[harmonic] << 14) / 100;
				harmonicPhaseSweepScales[harmonic] = (int) ((pitchEnvelope.rangeEnd - pitchEnvelope.rangeStart) * 32.768000000000001D * Math.pow(1.0057929410678534D, harmonicPitchOffsets[harmonic]) / samplesPerMs);
				harmonicPhaseBaseSteps[harmonic] = (int) (pitchEnvelope.rangeStart * 32.768000000000001D / samplesPerMs);
			}
		}

		for (int sample = 0; sample < sampleCount; sample++) {
			int pitch = pitchEnvelope.nextValue(sampleCount);
			int amplitude = amplitudeEnvelope.nextValue(sampleCount);
			if (vibratoRateEnvelope != null) {
				int vibratoRate = vibratoRateEnvelope.nextValue(sampleCount);
				int vibratoDepth = vibratoDepthEnvelope.nextValue(sampleCount);
				pitch += oscillate(vibratoDepth, vibratoPhase, vibratoRateEnvelope.waveform) >> 1;
				vibratoPhase += (vibratoRate * vibratoSweepScale >> 16) + vibratoBaseStep;
			}
			if (tremoloRateEnvelope != null) {
				int tremoloRate = tremoloRateEnvelope.nextValue(sampleCount);
				int tremoloDepth = tremoloDepthEnvelope.nextValue(sampleCount);
				amplitude = amplitude * ((oscillate(tremoloDepth, tremoloPhase, tremoloRateEnvelope.waveform) >> 1) + 32768) >> 15;
				tremoloPhase += (tremoloRate * tremoloSweepScale >> 16) + tremoloBaseStep;
			}
			for (int harmonic = 0; harmonic < 5; harmonic++) {
				if (harmonicVolumes[harmonic] != 0) {
					int outputIndex = sample + harmonicDelaySamples[harmonic];
					if (outputIndex < sampleCount) {
						sampleBuffer[outputIndex] += oscillate(amplitude * harmonicVolumeScales[harmonic] >> 15, harmonicPhases[harmonic], pitchEnvelope.waveform);
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
				int silenceValue = gateSilenceEnvelope.nextValue(sampleCount);
				int soundValue = gateSoundEnvelope.nextValue(sampleCount);
				int spanLimit;
				if (gateSilent) {
					spanLimit = gateSilenceEnvelope.rangeStart + ((gateSilenceEnvelope.rangeEnd - gateSilenceEnvelope.rangeStart) * silenceValue >> 8);
				} else {
					spanLimit = gateSilenceEnvelope.rangeStart + ((gateSilenceEnvelope.rangeEnd - gateSilenceEnvelope.rangeStart) * soundValue >> 8);
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
		if (filter.pairCounts[0] > 0 || filter.pairCounts[1] > 0) {
			filterEnvelope.resetValues();
			int k3 = filterEnvelope.nextValue(sampleCount + 1);
			int i5 = filter.computeCoefficients(0, k3 / 65536F);
			int i6 = filter.computeCoefficients(1, k3 / 65536F);
			if (sampleCount >= i5 + i6) {
				int j7 = 0;
				int l7 = i6;
				if (l7 > sampleCount - i5) {
					l7 = sampleCount - i5;
				}
				for (; j7 < l7; j7++) {
					int j8 = (int) ((long) sampleBuffer[j7 + i5] * (long) SynthFilter.gainFactorFixed >> 16);
					for (int k8 = 0; k8 < i5; k8++) {
						j8 += (int) ((long) sampleBuffer[j7 + i5 - 1 - k8] * (long) SynthFilter.coefficients[0][k8] >> 16);
					}

					for (int j9 = 0; j9 < j7; j9++) {
						j8 -= (int) ((long) sampleBuffer[j7 - 1 - j9] * (long) SynthFilter.coefficients[1][j9] >> 16);
					}

					sampleBuffer[j7] = j8;
					k3 = filterEnvelope.nextValue(sampleCount + 1);
				}

				char c = '\200';
				l7 = c;
				do {
					if (l7 > sampleCount - i5) {
						l7 = sampleCount - i5;
					}
					for (; j7 < l7; j7++) {
						int l8 = (int) ((long) sampleBuffer[j7 + i5] * (long) SynthFilter.gainFactorFixed >> 16);
						for (int k9 = 0; k9 < i5; k9++) {
							l8 += (int) ((long) sampleBuffer[j7 + i5 - 1 - k9] * (long) SynthFilter.coefficients[0][k9] >> 16);
						}

						for (int i10 = 0; i10 < i6; i10++) {
							l8 -= (int) ((long) sampleBuffer[j7 - 1 - i10] * (long) SynthFilter.coefficients[1][i10] >> 16);
						}

						sampleBuffer[j7] = l8;
						k3 = filterEnvelope.nextValue(sampleCount + 1);
					}

					if (j7 >= sampleCount - i5) {
						break;
					}
					i5 = filter.computeCoefficients(0, k3 / 65536F);
					i6 = filter.computeCoefficients(1, k3 / 65536F);
					l7 += c;
				} while (true);
				for (; j7 < sampleCount; j7++) {
					int i9 = 0;
					for (int l9 = j7 + i5 - sampleCount; l9 < i5; l9++) {
						i9 += (int) ((long) sampleBuffer[j7 + i5 - 1 - l9] * (long) SynthFilter.coefficients[0][l9] >> 16);
					}

					for (int j10 = 0; j10 < i6; j10++) {
						i9 -= (int) ((long) sampleBuffer[j7 - 1 - j10] * (long) SynthFilter.coefficients[1][j10] >> 16);
					}

					sampleBuffer[j7] = i9;
					filterEnvelope.nextValue(sampleCount + 1);
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
		pitchEnvelope = new SynthEnvelope();
		pitchEnvelope.decode(stream);
		amplitudeEnvelope = new SynthEnvelope();
		amplitudeEnvelope.decode(stream);
		int i = stream.readUnsignedByte();
		if (i != 0) {
			stream.currentOffset--;
			vibratoRateEnvelope = new SynthEnvelope();
			vibratoRateEnvelope.decode(stream);
			vibratoDepthEnvelope = new SynthEnvelope();
			vibratoDepthEnvelope.decode(stream);
		}
		i = stream.readUnsignedByte();
		if (i != 0) {
			stream.currentOffset--;
			tremoloRateEnvelope = new SynthEnvelope();
			tremoloRateEnvelope.decode(stream);
			tremoloDepthEnvelope = new SynthEnvelope();
			tremoloDepthEnvelope.decode(stream);
		}
		i = stream.readUnsignedByte();
		if (i != 0) {
			stream.currentOffset--;
			gateSilenceEnvelope = new SynthEnvelope();
			gateSilenceEnvelope.decode(stream);
			gateSoundEnvelope = new SynthEnvelope();
			gateSoundEnvelope.decode(stream);
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
		filter = new SynthFilter();
		filterEnvelope = new SynthEnvelope();
		filter.decode(stream, filterEnvelope);
	}

	public SynthVoice() {
		harmonicVolumes = new int[5];
		harmonicPitchOffsets = new int[5];
		harmonicDelays = new int[5];
		echoFeedback = 100;
		durationMs = 500;
	}

	private SynthEnvelope pitchEnvelope;
	private SynthEnvelope amplitudeEnvelope;
	private SynthEnvelope vibratoRateEnvelope;
	private SynthEnvelope vibratoDepthEnvelope;
	private SynthEnvelope tremoloRateEnvelope;
	private SynthEnvelope tremoloDepthEnvelope;
	private SynthEnvelope gateSilenceEnvelope;
	private SynthEnvelope gateSoundEnvelope;
	private final int[] harmonicVolumes;
	private final int[] harmonicPitchOffsets;
	private final int[] harmonicDelays;
	private int echoDelay;
	private int echoFeedback;
	private SynthFilter filter;
	private SynthEnvelope filterEnvelope;
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
