// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

import java.math.BigInteger;

public final class Stream extends NodeSub {

	public static Stream create() {
		synchronized (nodeList) {
			Stream stream = null;
			if (anInt1412 > 0) {
				anInt1412--;
				stream = (Stream) nodeList.popTail();
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
	
	public int method426() {
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

	public void method400(int i) {
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

	public void method403(int j) {
		buffer[currentOffset++] = (byte) j;
		buffer[currentOffset++] = (byte) (j >> 8);
		buffer[currentOffset++] = (byte) (j >> 16);
		buffer[currentOffset++] = (byte) (j >> 24);
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

	public void writeLfTerminatedString(String text) {
		// text.getBytes(0, text.length(), buffer, currentOffset); //deprecated
		System.arraycopy(text.getBytes(), 0, buffer, currentOffset, text.length());
		currentOffset += text.length();
		buffer[currentOffset++] = 10;
	}

	public void writeBytes(byte source[], int length, int sourceOffset) {
		for (int sourceIndex = sourceOffset; sourceIndex < sourceOffset + length; sourceIndex++) {
			buffer[currentOffset++] = source[sourceIndex];
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

	public String readLfTerminatedString() {
		int payloadStart = currentOffset;
		while (buffer[currentOffset++] != 10) {
			;
		}
		return new String(buffer, payloadStart, currentOffset - payloadStart - 1);
	}

	public byte[] readLfTerminatedBytes() {
		int payloadStart = currentOffset;
		while (buffer[currentOffset++] != 10) {
			;
		}
		byte payloadBytes[] = new byte[currentOffset - payloadStart - 1];
		System.arraycopy(buffer, payloadStart, payloadBytes, payloadStart - payloadStart, currentOffset - 1 - payloadStart);
		return payloadBytes;
	}

	public void readBytes(int length, int destinationOffset, byte destination[]) {
		for (int destinationIndex = destinationOffset; destinationIndex < destinationOffset + length; destinationIndex++) {
			destination[destinationIndex] = buffer[currentOffset++];
		}
	}

	public void beginBitReading() {
		bitOffset = currentOffset * 8;
	}

	public int readBitsMsbFirst(int remainingBits) {
		int byteIndex = bitOffset >> 3;
		int bitsInByte = 8 - (bitOffset & 7);
		int value = 0;
		bitOffset += remainingBits;
		for (; remainingBits > bitsInByte; bitsInByte = 8) {
			value += (buffer[byteIndex++] & lowBitMasks[bitsInByte]) << remainingBits - bitsInByte;
			remainingBits -= bitsInByte;
		}
		if (remainingBits == bitsInByte) {
			value += buffer[byteIndex] & lowBitMasks[bitsInByte];
		} else {
			value += buffer[byteIndex] >> bitsInByte - remainingBits & lowBitMasks[remainingBits];
		}
		return value;
	}

	public void finishBitReading() {
		currentOffset = (bitOffset + 7) / 8;
	}

	public int readSignedSmart() {
		int firstByte = buffer[currentOffset] & 0xff;
		if (firstByte < 128) {
			return readUnsignedByte() - 64;
		} else {
			return readUnsignedWord() - 49152;
		}
	}

	public int readUnsignedSmart() {
		int firstByte = buffer[currentOffset] & 0xff;
		if (firstByte < 128) {
			return readUnsignedByte();
		} else {
			return readUnsignedWord() - 32768;
		}
	}

	public void encryptRsaBlock() {
		int plaintextLength = currentOffset;
		currentOffset = 0;
		byte plaintextBytes[] = new byte[plaintextLength];
		readBytes(plaintextLength, 0, plaintextBytes);
		BigInteger plaintext = new BigInteger(plaintextBytes);
		BigInteger ciphertext = plaintext.modPow(ClientSettings.RSA_EXPONENT, ClientSettings.RSA_MODULUS);
		byte ciphertextBytes[] = ciphertext.toByteArray();
		currentOffset = 0;
		writeWordBigEndian(ciphertextBytes.length);
		writeBytes(ciphertextBytes, ciphertextBytes.length, 0);
	}
	

	public void method424(int i) {
		buffer[currentOffset++] = (byte) -i;
	}

	public void method425(int j) {
		buffer[currentOffset++] = (byte) (128 - j);
	}

	public int readUnsignedByteA() {
		return buffer[currentOffset++] - 128 & 0xff;
	}

	public int method427() {
		return -buffer[currentOffset++] & 0xff;
	}

	public int method428() {
		return 128 - buffer[currentOffset++] & 0xff;
	}

	public byte method429() {
		return (byte) -buffer[currentOffset++];
	}

	public byte method430() {
		return (byte) (128 - buffer[currentOffset++]);
	}

	public void method431(int i) {
		buffer[currentOffset++] = (byte) i;
		buffer[currentOffset++] = (byte) (i >> 8);
	}

	public void method432(int j) {
		buffer[currentOffset++] = (byte) (j >> 8);
		buffer[currentOffset++] = (byte) (j + 128);
	}

	public void method433(int j) {
		buffer[currentOffset++] = (byte) (j + 128);
		buffer[currentOffset++] = (byte) (j >> 8);
	}

	public int method434() {
		currentOffset += 2;
		return ((buffer[currentOffset - 1] & 0xff) << 8) + (buffer[currentOffset - 2] & 0xff);
	}

	public int method435() {
		currentOffset += 2;
		return ((buffer[currentOffset - 2] & 0xff) << 8) + (buffer[currentOffset - 1] - 128 & 0xff);
	}

	public int method436() {
		currentOffset += 2;
		return ((buffer[currentOffset - 1] & 0xff) << 8) + (buffer[currentOffset - 2] - 128 & 0xff);
	}

	public int method437() {
		currentOffset += 2;
		int j = ((buffer[currentOffset - 1] & 0xff) << 8) + (buffer[currentOffset - 2] & 0xff);
		if (j > 32767) {
			j -= 0x10000;
		}
		return j;
	}

	public int method438() {
		currentOffset += 2;
		int j = ((buffer[currentOffset - 1] & 0xff) << 8) + (buffer[currentOffset - 2] - 128 & 0xff);
		if (j > 32767) {
			j -= 0x10000;
		}
		return j;
	}

	public int method439() {
		currentOffset += 4;
		return ((buffer[currentOffset - 2] & 0xff) << 24) + ((buffer[currentOffset - 1] & 0xff) << 16) + ((buffer[currentOffset - 4] & 0xff) << 8) + (buffer[currentOffset - 3] & 0xff);
	}

	public int method440() {
		currentOffset += 4;
		return ((buffer[currentOffset - 3] & 0xff) << 24) + ((buffer[currentOffset - 4] & 0xff) << 16) + ((buffer[currentOffset - 1] & 0xff) << 8) + (buffer[currentOffset - 2] & 0xff);
	}

	public void writeReversedBytesAdd128(int sourceOffset, byte source[], int length) {
		for (int sourceIndex = sourceOffset + length - 1; sourceIndex >= sourceOffset; sourceIndex--) {
			buffer[currentOffset++] = (byte) (source[sourceIndex] + 128);
		}

	}

	public void readReversedBytes(int length, int destinationOffset, byte destination[]) {
		for (int destinationIndex = destinationOffset + length - 1; destinationIndex >= destinationOffset; destinationIndex--) {
			destination[destinationIndex] = buffer[currentOffset++];
		}

	}

	public byte buffer[];
	public int currentOffset;
	public int bitOffset;
	private static final int[] lowBitMasks = {0, 1, 3, 7, 15, 31, 63, 127, 255, 511, 1023, 2047, 4095, 8191, 16383, 32767, 65535, 0x1ffff, 0x3ffff, 0x7ffff, 0xfffff, 0x1fffff, 0x3fffff, 0x7fffff, 0xffffff, 0x1ffffff, 0x3ffffff, 0x7ffffff, 0xfffffff, 0x1fffffff, 0x3fffffff, 0x7fffffff, -1};
	public ISAACRandomGen encryption;
	private static int anInt1412;
	private static final NodeList nodeList = new NodeList();

	// removed useless static initializer
}
