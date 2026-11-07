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
			int filterPosition = filterEnvelope.nextValue(sampleCount + 1);
			int feedforwardTaps = filter.computeCoefficients(0, filterPosition / 65536F);
			int feedbackTaps = filter.computeCoefficients(1, filterPosition / 65536F);
			if (sampleCount >= feedforwardTaps + feedbackTaps) {
				int sample = 0;
				int blockEnd = feedbackTaps;
				if (blockEnd > sampleCount - feedforwardTaps) {
					blockEnd = sampleCount - feedforwardTaps;
				}
				for (; sample < blockEnd; sample++) {
					int filtered = (int) ((long) sampleBuffer[sample + feedforwardTaps] * (long) SynthFilter.gainFactorFixed >> 16);
					for (int feedforwardTap = 0; feedforwardTap < feedforwardTaps; feedforwardTap++) {
						filtered += (int) ((long) sampleBuffer[sample + feedforwardTaps - 1 - feedforwardTap] * (long) SynthFilter.coefficients[0][feedforwardTap] >> 16);
					}

					for (int feedbackTap = 0; feedbackTap < sample; feedbackTap++) {
						filtered -= (int) ((long) sampleBuffer[sample - 1 - feedbackTap] * (long) SynthFilter.coefficients[1][feedbackTap] >> 16);
					}

					sampleBuffer[sample] = filtered;
					filterPosition = filterEnvelope.nextValue(sampleCount + 1);
				}

				char refreshInterval = '\200';
				blockEnd = refreshInterval;
				do {
					if (blockEnd > sampleCount - feedforwardTaps) {
						blockEnd = sampleCount - feedforwardTaps;
					}
					for (; sample < blockEnd; sample++) {
						int filtered = (int) ((long) sampleBuffer[sample + feedforwardTaps] * (long) SynthFilter.gainFactorFixed >> 16);
						for (int feedforwardTap = 0; feedforwardTap < feedforwardTaps; feedforwardTap++) {
							filtered += (int) ((long) sampleBuffer[sample + feedforwardTaps - 1 - feedforwardTap] * (long) SynthFilter.coefficients[0][feedforwardTap] >> 16);
						}

						for (int feedbackTap = 0; feedbackTap < feedbackTaps; feedbackTap++) {
							filtered -= (int) ((long) sampleBuffer[sample - 1 - feedbackTap] * (long) SynthFilter.coefficients[1][feedbackTap] >> 16);
						}

						sampleBuffer[sample] = filtered;
						filterPosition = filterEnvelope.nextValue(sampleCount + 1);
					}

					if (sample >= sampleCount - feedforwardTaps) {
						break;
					}
					feedforwardTaps = filter.computeCoefficients(0, filterPosition / 65536F);
					feedbackTaps = filter.computeCoefficients(1, filterPosition / 65536F);
					blockEnd += refreshInterval;
				} while (true);
				for (; sample < sampleCount; sample++) {
					int filtered = 0;
					for (int feedforwardTap = sample + feedforwardTaps - sampleCount; feedforwardTap < feedforwardTaps; feedforwardTap++) {
						filtered += (int) ((long) sampleBuffer[sample + feedforwardTaps - 1 - feedforwardTap] * (long) SynthFilter.coefficients[0][feedforwardTap] >> 16);
					}

					for (int feedbackTap = 0; feedbackTap < feedbackTaps; feedbackTap++) {
						filtered -= (int) ((long) sampleBuffer[sample - 1 - feedbackTap] * (long) SynthFilter.coefficients[1][feedbackTap] >> 16);
					}

					sampleBuffer[sample] = filtered;
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
