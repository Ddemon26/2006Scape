// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

final class Sounds {

	private Sounds() {
		voices = new SynthVoice[10];
	}

	public static void unpack(Stream stream) {
		waveBuffer = new byte[0x6baa8];
		waveStream = new Stream(waveBuffer);
		SynthVoice.initWaveTables();
		do {
			int soundId = stream.readUnsignedShortBigEndian();
			if (soundId == 65535) {
				return;
			}
			soundsById[soundId] = new Sounds();
			soundsById[soundId].readFrom(stream);
			leadingSilence20msUnits[soundId] = soundsById[soundId].trimLeadingSilence();
		} while (true);
	}

	public static Stream buildWave(int playCount, int soundId) {
		if (soundsById[soundId] != null) {
			Sounds sound = soundsById[soundId];
			return sound.writeWav(playCount);
		} else {
			return null;
		}
	}

	private void readFrom(Stream stream) {
		for (int instrumentIndex = 0; instrumentIndex < 10; instrumentIndex++) {
			int flag = stream.readUnsignedByte();
			if (flag != 0) {
				stream.currentOffset--;
				voices[instrumentIndex] = new SynthVoice();
				voices[instrumentIndex].decode(stream);
			}
		}
		loopStartMillis = stream.readUnsignedShortBigEndian();
		loopEndMillis = stream.readUnsignedShortBigEndian();
	}

	private int trimLeadingSilence() {
		int minUnits = 0x98967f;
		for (int instrumentIndex = 0; instrumentIndex < 10; instrumentIndex++) {
			if (voices[instrumentIndex] != null && voices[instrumentIndex].startOffsetMs / 20 < minUnits) {
				minUnits = voices[instrumentIndex].startOffsetMs / 20;
			}
		}

		if (loopStartMillis < loopEndMillis && loopStartMillis / 20 < minUnits) {
			minUnits = loopStartMillis / 20;
		}
		if (minUnits == 0x98967f || minUnits == 0) {
			return 0;
		}
		for (int shiftIndex = 0; shiftIndex < 10; shiftIndex++) {
			if (voices[shiftIndex] != null) {
				voices[shiftIndex].startOffsetMs -= minUnits * 20;
			}
		}

		if (loopStartMillis < loopEndMillis) {
			loopStartMillis -= minUnits * 20;
			loopEndMillis -= minUnits * 20;
		}
		return minUnits;
	}

	private Stream writeWav(int playCount) {
		int dataLength = renderSamples(playCount);
		waveStream.currentOffset = 0;
		waveStream.writeIntBigEndian(0x52494646);
		waveStream.writeIntLittleEndian(36 + dataLength);
		waveStream.writeIntBigEndian(0x57415645);
		waveStream.writeIntBigEndian(0x666d7420);
		waveStream.writeIntLittleEndian(16);
		waveStream.writeWaveShortLittleEndian(1);
		waveStream.writeWaveShortLittleEndian(1);
		waveStream.writeIntLittleEndian(22050);
		waveStream.writeIntLittleEndian(22050);
		waveStream.writeWaveShortLittleEndian(1);
		waveStream.writeWaveShortLittleEndian(8);
		waveStream.writeIntBigEndian(0x64617461);
		waveStream.writeIntLittleEndian(dataLength);
		waveStream.currentOffset += dataLength;
		return waveStream;
	}

	private int renderSamples(int playCount) {
		int totalMillis = 0;
		for (int instrumentIndex = 0; instrumentIndex < 10; instrumentIndex++) {
			if (voices[instrumentIndex] != null && voices[instrumentIndex].durationMs + voices[instrumentIndex].startOffsetMs > totalMillis) {
				totalMillis = voices[instrumentIndex].durationMs + voices[instrumentIndex].startOffsetMs;
			}
		}

		if (totalMillis == 0) {
			return 0;
		}
		int totalSamples = 22050 * totalMillis / 1000;
		int loopStartSample = 22050 * loopStartMillis / 1000;
		int loopEndSample = 22050 * loopEndMillis / 1000;
		if (loopStartSample < 0 || loopStartSample > totalSamples || loopEndSample < 0 || loopEndSample > totalSamples || loopStartSample >= loopEndSample) {
			playCount = 0;
		}
		int outputLength = totalSamples + (loopEndSample - loopStartSample) * (playCount - 1);
		for (int clearIndex = 44; clearIndex < outputLength + 44; clearIndex++) {
			waveBuffer[clearIndex] = -128;
		}

		for (int voiceIndex = 0; voiceIndex < 10; voiceIndex++) {
			if (voices[voiceIndex] != null) {
				int sampleCount = voices[voiceIndex].durationMs * 22050 / 1000;
				int startSample = voices[voiceIndex].startOffsetMs * 22050 / 1000;
				int samples[] = voices[voiceIndex].synthesize(sampleCount, voices[voiceIndex].durationMs);
				for (int sampleIndex = 0; sampleIndex < sampleCount; sampleIndex++) {
					waveBuffer[sampleIndex + startSample + 44] += (byte) (samples[sampleIndex] >> 8);
				}

			}
		}

		if (playCount > 1) {
			loopStartSample += 44;
			loopEndSample += 44;
			totalSamples += 44;
			int tailShift = (outputLength += 44) - totalSamples;
			for (int tailIndex = totalSamples - 1; tailIndex >= loopEndSample; tailIndex--) {
				waveBuffer[tailIndex + tailShift] = waveBuffer[tailIndex];
			}

			for (int repeatIndex = 1; repeatIndex < playCount; repeatIndex++) {
				int repeatOffset = (loopEndSample - loopStartSample) * repeatIndex;
				System.arraycopy(waveBuffer, loopStartSample, waveBuffer, loopStartSample + repeatOffset, loopEndSample - loopStartSample);

			}

			outputLength -= 44;
		}
		return outputLength;
	}

	private static final Sounds[] soundsById = new Sounds[5000];
	public static final int[] leadingSilence20msUnits = new int[5000];
	private static byte[] waveBuffer;
	private static Stream waveStream;
	private final SynthVoice[] voices;
	private int loopStartMillis;
	private int loopEndMillis;

}
