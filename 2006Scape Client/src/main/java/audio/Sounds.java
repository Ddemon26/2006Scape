// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

final class Sounds {

        private Sounds() {
                instruments = new Instrument[10];
        }

	public static void unpack(Stream stream) {
                buffer = new byte[0x6baa8];
                waveStream = new Stream(buffer);
		Instrument.initializeTables();
		do {
			int j = stream.readUnsignedWord();
			if (j == 65535) {
				return;
			}
                        sounds[j] = new Sounds();
                        sounds[j].decodeSound(stream);
                        delays[j] = sounds[j].calculateDelay();
		} while (true);
	}

        public static Stream createSoundStream(int loops, int id) {
                if (sounds[id] != null) {
                        Sounds sound = sounds[id];
                        return sound.createWavStream(loops);
                } else {
                        return null;
                }
        }

        private void decodeSound(Stream stream) {
		for (int i = 0; i < 10; i++) {
			int j = stream.readUnsignedByte();
			if (j != 0) {
				stream.currentOffset--;
                                instruments[i] = new Instrument();
                                instruments[i].decode(stream);
			}
		}
                loopStart = stream.readUnsignedWord();
                loopEnd = stream.readUnsignedWord();
	}

        private int calculateDelay() {
		int j = 0x98967f;
		for (int k = 0; k < 10; k++) {
                        if (instruments[k] != null && instruments[k].offset / 20 < j) {
                                j = instruments[k].offset / 20;
			}
		}

                if (loopStart < loopEnd && loopStart / 20 < j) {
                        j = loopStart / 20;
		}
		if (j == 0x98967f || j == 0) {
			return 0;
		}
		for (int l = 0; l < 10; l++) {
                        if (instruments[l] != null) {
                                instruments[l].offset -= j * 20;
			}
		}

                if (loopStart < loopEnd) {
                        loopStart -= j * 20;
                        loopEnd -= j * 20;
		}
		return j;
	}

        private Stream createWavStream(int loops) {
                int k = mixAudioData(loops);
                waveStream.currentOffset = 0;
                waveStream.writeDWord(0x52494646);
                waveStream.writeIntLE(36 + k);
                waveStream.writeDWord(0x57415645);
                waveStream.writeDWord(0x666d7420);
                waveStream.writeIntLE(16);
                waveStream.writeShortLE(1);
                waveStream.writeShortLE(1);
                waveStream.writeIntLE(22050);
                waveStream.writeIntLE(22050);
                waveStream.writeShortLE(1);
                waveStream.writeShortLE(8);
                waveStream.writeDWord(0x64617461);
                waveStream.writeIntLE(k);
                waveStream.currentOffset += k;
                return waveStream;
	}

        private int mixAudioData(int loops) {
		int j = 0;
		for (int k = 0; k < 10; k++) {
                        if (instruments[k] != null && instruments[k].duration + instruments[k].offset > j) {
                                j = instruments[k].duration + instruments[k].offset;
			}
		}

		if (j == 0) {
			return 0;
		}
		int l = 22050 * j / 1000;
                int i1 = 22050 * loopStart / 1000;
                int j1 = 22050 * loopEnd / 1000;
                if (i1 < 0 || i1 > l || j1 < 0 || j1 > l || i1 >= j1) {
                        loops = 0;
                }
                int k1 = l + (j1 - i1) * (loops - 1);
		for (int l1 = 44; l1 < k1 + 44; l1++) {
                        buffer[l1] = -128;
		}

		for (int i2 = 0; i2 < 10; i2++) {
                        if (instruments[i2] != null) {
                                int j2 = instruments[i2].duration * 22050 / 1000;
                                int i3 = instruments[i2].offset * 22050 / 1000;
                                int ai[] = instruments[i2].synthesize(j2, instruments[i2].duration);
				for (int l3 = 0; l3 < j2; l3++) {
                                        buffer[l3 + i3 + 44] += (byte) (ai[l3] >> 8);
				}

			}
		}

                if (loops > 1) {
                        i1 += 44;
                        j1 += 44;
                        l += 44;
                        int k2 = (k1 += 44) - l;
                        for (int j3 = l - 1; j3 >= j1; j3--) {
                                buffer[j3 + k2] = buffer[j3];
                        }

                        for (int k3 = 1; k3 < loops; k3++) {
                                int l2 = (j1 - i1) * k3;
                                System.arraycopy(buffer, i1, buffer, i1 + l2, j1 - i1);

                        }

			k1 -= 44;
		}
		return k1;
	}

        private static final Sounds[] sounds = new Sounds[5000];
        public static final int[] delays = new int[5000];
        private static byte[] buffer;
        private static Stream waveStream;
        private final Instrument[] instruments;
        private int loopStart;
        private int loopEnd;

}
