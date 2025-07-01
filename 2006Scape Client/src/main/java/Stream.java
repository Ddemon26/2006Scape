// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

import java.math.BigInteger;

public final class Stream extends NodeSub {

        public static Stream create() {
                synchronized (pool) {
                        Stream stream = null;
                        if (poolSize > 0) {
                                poolSize--;
                                stream = (Stream) pool.popHead();
                        }
			if (stream != null) {
				stream.currentOffset = 0;
				return stream;
			}
		}
		Stream stream_1 = new Stream(null);
		stream_1.currentOffset = 0;
		stream_1.buffer = new byte[5000];
		return stream_1;
	}
	
        public int readUnsignedByteAdd() {
                return buffer[currentOffset++] - 128 & 0xff;
        }

	public Stream(byte abyte0[]) {
		buffer = abyte0;
		currentOffset = 0;
	}

	public void createFrame(int i) {
		buffer[currentOffset++] = (byte) (i + encryption.getNextKey());
	}

	public void writeWordBigEndian(int i) {
		buffer[currentOffset++] = (byte) i;
	}

	public void writeWord(int i) {
		buffer[currentOffset++] = (byte) (i >> 8);
		buffer[currentOffset++] = (byte) i;
	}

        public void writeShortLE(int i) {
                buffer[currentOffset++] = (byte) i;
                buffer[currentOffset++] = (byte) (i >> 8);
        }

	public void writeDWordBigEndian(int i) {
		buffer[currentOffset++] = (byte) (i >> 16);
		buffer[currentOffset++] = (byte) (i >> 8);
		buffer[currentOffset++] = (byte) i;
	}

	public void writeDWord(int i) {
		buffer[currentOffset++] = (byte) (i >> 24);
		buffer[currentOffset++] = (byte) (i >> 16);
		buffer[currentOffset++] = (byte) (i >> 8);
		buffer[currentOffset++] = (byte) i;
	}

        public void writeIntLE(int value) {
                buffer[currentOffset++] = (byte) value;
                buffer[currentOffset++] = (byte) (value >> 8);
                buffer[currentOffset++] = (byte) (value >> 16);
                buffer[currentOffset++] = (byte) (value >> 24);
        }

	public void writeQWord(long l) {
		try {
			buffer[currentOffset++] = (byte) (int) (l >> 56);
			buffer[currentOffset++] = (byte) (int) (l >> 48);
			buffer[currentOffset++] = (byte) (int) (l >> 40);
			buffer[currentOffset++] = (byte) (int) (l >> 32);
			buffer[currentOffset++] = (byte) (int) (l >> 24);
			buffer[currentOffset++] = (byte) (int) (l >> 16);
			buffer[currentOffset++] = (byte) (int) (l >> 8);
			buffer[currentOffset++] = (byte) (int) l;
		} catch (RuntimeException runtimeexception) {
			Signlink.reporterror("14395, " + 5 + ", " + l + ", " + runtimeexception.toString());
			throw new RuntimeException();
		}
	}

	public void writeString(String s) {
		// s.getBytes(0, s.length(), buffer, currentOffset); //deprecated
		System.arraycopy(s.getBytes(), 0, buffer, currentOffset, s.length());
		currentOffset += s.length();
		buffer[currentOffset++] = 10;
	}

	public void writeBytes(byte abyte0[], int i, int j) {
		for (int k = j; k < j + i; k++) {
			buffer[currentOffset++] = abyte0[k];
		}

	}

	public void writeBytes(int i) {
		buffer[currentOffset - i - 1] = (byte) i;
	}

	public int readUnsignedByte() {
		return buffer[currentOffset++] & 0xff;
	}

	public byte readSignedByte() {
		return buffer[currentOffset++];
	}

	public int readUnsignedWord() {
		currentOffset += 2;
		return ((buffer[currentOffset - 2] & 0xff) << 8) + (buffer[currentOffset - 1] & 0xff);
	}

	public int readSignedWord() {
		currentOffset += 2;
		int i = ((buffer[currentOffset - 2] & 0xff) << 8) + (buffer[currentOffset - 1] & 0xff);
		if (i > 32767) {
			i -= 0x10000;
		}
		return i;
	}

	public int read3Bytes() {
		currentOffset += 3;
		return ((buffer[currentOffset - 3] & 0xff) << 16) + ((buffer[currentOffset - 2] & 0xff) << 8) + (buffer[currentOffset - 1] & 0xff);
	}

	public int readDWord() {
		currentOffset += 4;
		return ((buffer[currentOffset - 4] & 0xff) << 24) + ((buffer[currentOffset - 3] & 0xff) << 16) + ((buffer[currentOffset - 2] & 0xff) << 8) + (buffer[currentOffset - 1] & 0xff);
	}

	public long readQWord() {
		long l = readDWord() & 0xffffffffL;
		long l1 = readDWord() & 0xffffffffL;
		return (l << 32) + l1;
	}

	public String readString() {
		int i = currentOffset;
		while (buffer[currentOffset++] != 10) {
			;
		}
		return new String(buffer, i, currentOffset - i - 1);
	}

	public byte[] readBytes() {
		int i = currentOffset;
		while (buffer[currentOffset++] != 10) {
			;
		}
		byte abyte0[] = new byte[currentOffset - i - 1];
		System.arraycopy(buffer, i, abyte0, i - i, currentOffset - 1 - i);
		return abyte0;
	}

	public void readBytes(int i, int j, byte abyte0[]) {
		for (int l = j; l < j + i; l++) {
			abyte0[l] = buffer[currentOffset++];
		}
	}

	public void initBitAccess() {
		bitPosition = currentOffset * 8;
	}

	public int readBits(int i) {
		int k = bitPosition >> 3;
		int l = 8 - (bitPosition & 7);
		int i1 = 0;
		bitPosition += i;
		for (; i > l; l = 8) {
                        i1 += (buffer[k++] & BIT_MASKS[l]) << i - l;
			i -= l;
		}
		if (i == l) {
                        i1 += buffer[k] & BIT_MASKS[l];
		} else {
                        i1 += buffer[k] >> l - i & BIT_MASKS[i];
		}
		return i1;
	}

	public void finishBitAccess() {
		currentOffset = (bitPosition + 7) / 8;
	}

        public int readSignedSmart() {
                int peek = buffer[currentOffset] & 0xff;
                if (peek < 128) {
                        return readUnsignedByte() - 64;
                } else {
                        return readUnsignedWord() - 49152;
                }
        }

        public int readUnsignedSmart() {
                int peek = buffer[currentOffset] & 0xff;
                if (peek < 128) {
                        return readUnsignedByte();
                } else {
                        return readUnsignedWord() - 32768;
                }
        }

        public void rsaEncrypt() {
                int length = currentOffset;
                currentOffset = 0;
                byte[] data = new byte[length];
                readBytes(length, 0, data);
                BigInteger value = new BigInteger(data);
                BigInteger encrypted = value.modPow(ClientSettings.RSA_EXPONENT, ClientSettings.RSA_MODULUS);
                byte[] bytes = encrypted.toByteArray();
                currentOffset = 0;
                writeWordBigEndian(bytes.length);
                writeBytes(bytes, bytes.length, 0);
        }
	

        public void writeByteNeg(int value) {
                buffer[currentOffset++] = (byte) -value;
        }

        public void writeByteSub(int value) {
                buffer[currentOffset++] = (byte) (128 - value);
        }

	public int readUnsignedByteA() {
		return buffer[currentOffset++] - 128 & 0xff;
	}

        public int readUnsignedByteNeg() {
                return -buffer[currentOffset++] & 0xff;
        }

        public int readUnsignedByteSub() {
                return 128 - buffer[currentOffset++] & 0xff;
        }

        public byte readByteNeg() {
                return (byte) -buffer[currentOffset++];
        }

        public byte readByteSub() {
                return (byte) (128 - buffer[currentOffset++]);
        }

        public void writeShortLEDup(int value) {
                buffer[currentOffset++] = (byte) value;
                buffer[currentOffset++] = (byte) (value >> 8);
        }

        public void writeShortA(int value) {
                buffer[currentOffset++] = (byte) (value >> 8);
                buffer[currentOffset++] = (byte) (value + 128);
        }

        public void writeShortLEA(int value) {
                buffer[currentOffset++] = (byte) (value + 128);
                buffer[currentOffset++] = (byte) (value >> 8);
        }

        public int readShortLE() {
                currentOffset += 2;
                return ((buffer[currentOffset - 1] & 0xff) << 8) + (buffer[currentOffset - 2] & 0xff);
        }

        public int readShortAdd() {
                currentOffset += 2;
                return ((buffer[currentOffset - 2] & 0xff) << 8) + (buffer[currentOffset - 1] - 128 & 0xff);
        }

        public int readShortLEAdd() {
                currentOffset += 2;
                return ((buffer[currentOffset - 1] & 0xff) << 8) + (buffer[currentOffset - 2] - 128 & 0xff);
        }

        public int readShortLESigned() {
                currentOffset += 2;
                int value = ((buffer[currentOffset - 1] & 0xff) << 8) + (buffer[currentOffset - 2] & 0xff);
                if (value > 32767) {
                        value -= 0x10000;
                }
                return value;
        }

        public int readShortLEAddSigned() {
                currentOffset += 2;
                int value = ((buffer[currentOffset - 1] & 0xff) << 8) + (buffer[currentOffset - 2] - 128 & 0xff);
                if (value > 32767) {
                        value -= 0x10000;
                }
                return value;
        }

        public int readIntV1() {
                currentOffset += 4;
                return ((buffer[currentOffset - 2] & 0xff) << 24) + ((buffer[currentOffset - 1] & 0xff) << 16) + ((buffer[currentOffset - 4] & 0xff) << 8) + (buffer[currentOffset - 3] & 0xff);
        }

        public int readIntV2() {
                currentOffset += 4;
                return ((buffer[currentOffset - 3] & 0xff) << 24) + ((buffer[currentOffset - 4] & 0xff) << 16) + ((buffer[currentOffset - 1] & 0xff) << 8) + (buffer[currentOffset - 2] & 0xff);
        }

        public void writeBytesReverseAdd(int length, byte[] data, int offset) {
                for (int k = offset + length - 1; k >= offset; k--) {
                        buffer[currentOffset++] = (byte) (data[k] + 128);
                }

        }

        public void readBytesReverse(int length, int offset, byte[] data) {
                for (int k = offset + length - 1; k >= offset; k--) {
                        data[k] = buffer[currentOffset++];
                }

        }

	public byte buffer[];
	public int currentOffset;
	public int bitPosition;
        private static final int[] BIT_MASKS = {0, 1, 3, 7, 15, 31, 63, 127, 255, 511, 1023, 2047, 4095, 8191, 16383, 32767, 65535, 0x1ffff, 0x3ffff, 0x7ffff, 0xfffff, 0x1fffff, 0x3fffff, 0x7fffff, 0xffffff, 0x1ffffff, 0x3ffffff, 0x7ffffff, 0xfffffff, 0x1fffffff, 0x3fffffff, 0x7fffffff, -1};
	public ISAACRandomGen encryption;
        private static int poolSize;
        private static final NodeList pool = new NodeList();

	// removed useless static initializer
}
