// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

public final class Background extends DrawingArea {

	public Background(StreamLoader streamLoader, String s, int i) {
		Stream stream = new Stream(streamLoader.getDataForName(s + ".dat"));
		Stream stream_1 = new Stream(streamLoader.getDataForName("index.dat"));
		stream_1.currentOffset = stream.readUnsignedWord();
		anInt1456 = stream_1.readUnsignedWord();
		anInt1457 = stream_1.readUnsignedWord();
		int j = stream_1.readUnsignedByte();
		anIntArray1451 = new int[j];
		for (int k = 0; k < j - 1; k++) {
			anIntArray1451[k + 1] = stream_1.read3Bytes();
		}

		for (int l = 0; l < i; l++) {
			stream_1.currentOffset += 2;
			stream.currentOffset += stream_1.readUnsignedWord() * stream_1.readUnsignedWord();
			stream_1.currentOffset++;
		}

		offsetX = stream_1.readUnsignedByte();
		offsetY = stream_1.readUnsignedByte();
		anInt1452 = stream_1.readUnsignedWord();
		anInt1453 = stream_1.readUnsignedWord();
		int i1 = stream_1.readUnsignedByte();
		int j1 = anInt1452 * anInt1453;
		aByteArray1450 = new byte[j1];
		if (i1 == 0) {
			for (int k1 = 0; k1 < j1; k1++) {
				aByteArray1450[k1] = stream.readSignedByte();
			}

			return;
		}
		if (i1 == 1) {
			for (int l1 = 0; l1 < anInt1452; l1++) {
				for (int i2 = 0; i2 < anInt1453; i2++) {
					aByteArray1450[l1 + i2 * anInt1452] = stream.readSignedByte();
				}

			}

		}
	}

	public void method356() {
		anInt1456 /= 2;
		anInt1457 /= 2;
		byte abyte0[] = new byte[anInt1456 * anInt1457];
		int i = 0;
		for (int j = 0; j < anInt1453; j++) {
			for (int k = 0; k < anInt1452; k++) {
				abyte0[(k + offsetX >> 1) + (j + offsetY >> 1) * anInt1456] = aByteArray1450[i++];
			}

		}

		aByteArray1450 = abyte0;
		anInt1452 = anInt1456;
		anInt1453 = anInt1457;
		offsetX = 0;
		offsetY = 0;
	}

	public void method357() {
		if (anInt1452 == anInt1456 && anInt1453 == anInt1457) {
			return;
		}
		byte abyte0[] = new byte[anInt1456 * anInt1457];
		int i = 0;
		for (int j = 0; j < anInt1453; j++) {
			for (int k = 0; k < anInt1452; k++) {
				abyte0[k + offsetX + (j + offsetY) * anInt1456] = aByteArray1450[i++];
			}

		}

		aByteArray1450 = abyte0;
		anInt1452 = anInt1456;
		anInt1453 = anInt1457;
		offsetX = 0;
		offsetY = 0;
	}

	public void flipHorizontally() {
		byte abyte0[] = new byte[anInt1452 * anInt1453];
		int j = 0;
		for (int k = 0; k < anInt1453; k++) {
			for (int l = anInt1452 - 1; l >= 0; l--) {
				abyte0[j++] = aByteArray1450[l + k * anInt1452];
			}

		}

		aByteArray1450 = abyte0;
		offsetX = anInt1456 - anInt1452 - offsetX;
	}

	public void flipVertically() {
		byte abyte0[] = new byte[anInt1452 * anInt1453];
		int i = 0;
		for (int j = anInt1453 - 1; j >= 0; j--) {
			for (int k = 0; k < anInt1452; k++) {
				abyte0[i++] = aByteArray1450[k + j * anInt1452];
			}

		}

		aByteArray1450 = abyte0;
		offsetY = anInt1457 - anInt1453 - offsetY;
	}

	public void adjustPaletteRgb(int redDelta, int greenDelta, int blueDelta) {
		for (int i1 = 0; i1 < anIntArray1451.length; i1++) {
			int j1 = anIntArray1451[i1] >> 16 & 0xff;
			j1 += redDelta;
			if (j1 < 0) {
				j1 = 0;
			} else if (j1 > 255) {
				j1 = 255;
			}
			int k1 = anIntArray1451[i1] >> 8 & 0xff;
			k1 += greenDelta;
			if (k1 < 0) {
				k1 = 0;
			} else if (k1 > 255) {
				k1 = 255;
			}
			int l1 = anIntArray1451[i1] & 0xff;
			l1 += blueDelta;
			if (l1 < 0) {
				l1 = 0;
			} else if (l1 > 255) {
				l1 = 255;
			}
			anIntArray1451[i1] = (j1 << 16) + (k1 << 8) + l1;
		}
	}

	public void method361(int i, int k) {
		i += offsetX;
		k += offsetY;
		int l = i + k * DrawingArea.width;
		int i1 = 0;
		int j1 = anInt1453;
		int k1 = anInt1452;
		int l1 = DrawingArea.width - k1;
		int i2 = 0;
		if (k < DrawingArea.topY) {
			int j2 = DrawingArea.topY - k;
			j1 -= j2;
			k = DrawingArea.topY;
			i1 += j2 * k1;
			l += j2 * DrawingArea.width;
		}
		if (k + j1 > DrawingArea.bottomY) {
			j1 -= k + j1 - DrawingArea.bottomY;
		}
		if (i < DrawingArea.topX) {
			int k2 = DrawingArea.topX - i;
			k1 -= k2;
			i = DrawingArea.topX;
			i1 += k2;
			l += k2;
			i2 += k2;
			l1 += k2;
		}
		if (i + k1 > DrawingArea.bottomX) {
			int l2 = i + k1 - DrawingArea.bottomX;
			k1 -= l2;
			i2 += l2;
			l1 += l2;
		}
		if (!(k1 <= 0 || j1 <= 0)) {
			method362(j1, DrawingArea.pixels, aByteArray1450, l1, l, k1, i1, anIntArray1451, i2);
		}
	}

	private void method362(int i, int ai[], byte abyte0[], int j, int k, int l, int i1, int ai1[], int j1) {
		int k1 = -(l >> 2);
		l = -(l & 3);
		for (int l1 = -i; l1 < 0; l1++) {
			for (int i2 = k1; i2 < 0; i2++) {
				byte byte1 = abyte0[i1++];
				if (byte1 != 0) {
					ai[k++] = ai1[byte1 & 0xff];
				} else {
					k++;
				}
				byte1 = abyte0[i1++];
				if (byte1 != 0) {
					ai[k++] = ai1[byte1 & 0xff];
				} else {
					k++;
				}
				byte1 = abyte0[i1++];
				if (byte1 != 0) {
					ai[k++] = ai1[byte1 & 0xff];
				} else {
					k++;
				}
				byte1 = abyte0[i1++];
				if (byte1 != 0) {
					ai[k++] = ai1[byte1 & 0xff];
				} else {
					k++;
				}
			}

			for (int j2 = l; j2 < 0; j2++) {
				byte byte2 = abyte0[i1++];
				if (byte2 != 0) {
					ai[k++] = ai1[byte2 & 0xff];
				} else {
					k++;
				}
			}

			k += j;
			i1 += j1;
		}

	}

	public byte aByteArray1450[];
	public final int[] anIntArray1451;
	public int anInt1452;
	public int anInt1453;
	public int offsetX;
	public int offsetY;
	public int anInt1456;
	private int anInt1457;
}
