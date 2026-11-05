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

	public void reduceToHalfCanvas() {
		anInt1456 /= 2;
		anInt1457 /= 2;
		byte canvasIndices[] = new byte[anInt1456 * anInt1457];
		int sourceIndex = 0;
		for (int sourceY = 0; sourceY < anInt1453; sourceY++) {
			for (int sourceX = 0; sourceX < anInt1452; sourceX++) {
				canvasIndices[(sourceX + offsetX >> 1) + (sourceY + offsetY >> 1) * anInt1456] = aByteArray1450[sourceIndex++];
			}

		}

		aByteArray1450 = canvasIndices;
		anInt1452 = anInt1456;
		anInt1453 = anInt1457;
		offsetX = 0;
		offsetY = 0;
	}

	public void expandToCanvas() {
		if (anInt1452 == anInt1456 && anInt1453 == anInt1457) {
			return;
		}
		byte canvasIndices[] = new byte[anInt1456 * anInt1457];
		int sourceIndex = 0;
		for (int sourceY = 0; sourceY < anInt1453; sourceY++) {
			for (int sourceX = 0; sourceX < anInt1452; sourceX++) {
				canvasIndices[sourceX + offsetX + (sourceY + offsetY) * anInt1456] = aByteArray1450[sourceIndex++];
			}

		}

		aByteArray1450 = canvasIndices;
		anInt1452 = anInt1456;
		anInt1453 = anInt1457;
		offsetX = 0;
		offsetY = 0;
	}

	public void flipHorizontally() {
		byte mirroredIndices[] = new byte[anInt1452 * anInt1453];
		int destinationIndex = 0;
		for (int sourceY = 0; sourceY < anInt1453; sourceY++) {
			for (int sourceX = anInt1452 - 1; sourceX >= 0; sourceX--) {
				mirroredIndices[destinationIndex++] = aByteArray1450[sourceX + sourceY * anInt1452];
			}

		}

		aByteArray1450 = mirroredIndices;
		offsetX = anInt1456 - anInt1452 - offsetX;
	}

	public void flipVertically() {
		byte mirroredIndices[] = new byte[anInt1452 * anInt1453];
		int destinationIndex = 0;
		for (int sourceY = anInt1453 - 1; sourceY >= 0; sourceY--) {
			for (int sourceX = 0; sourceX < anInt1452; sourceX++) {
				mirroredIndices[destinationIndex++] = aByteArray1450[sourceX + sourceY * anInt1452];
			}

		}

		aByteArray1450 = mirroredIndices;
		offsetY = anInt1457 - anInt1453 - offsetY;
	}

	public void adjustPaletteRgb(int redDelta, int greenDelta, int blueDelta) {
		for (int paletteIndex = 0; paletteIndex < anIntArray1451.length; paletteIndex++) {
			int red = anIntArray1451[paletteIndex] >> 16 & 0xff;
			red += redDelta;
			if (red < 0) {
				red = 0;
			} else if (red > 255) {
				red = 255;
			}
			int green = anIntArray1451[paletteIndex] >> 8 & 0xff;
			green += greenDelta;
			if (green < 0) {
				green = 0;
			} else if (green > 255) {
				green = 255;
			}
			int blue = anIntArray1451[paletteIndex] & 0xff;
			blue += blueDelta;
			if (blue < 0) {
				blue = 0;
			} else if (blue > 255) {
				blue = 255;
			}
			anIntArray1451[paletteIndex] = (red << 16) + (green << 8) + blue;
		}
	}

	public void drawNonzeroIndexed(int destinationX, int destinationY) {
		destinationX += offsetX;
		destinationY += offsetY;
		int destinationIndex = destinationX + destinationY * DrawingArea.width;
		int sourceIndex = 0;
		int visibleHeight = anInt1453;
		int visibleWidth = anInt1452;
		int destinationRowSkip = DrawingArea.width - visibleWidth;
		int sourceRowSkip = 0;
		if (destinationY < DrawingArea.topY) {
			int clippedTopRows = DrawingArea.topY - destinationY;
			visibleHeight -= clippedTopRows;
			destinationY = DrawingArea.topY;
			sourceIndex += clippedTopRows * visibleWidth;
			destinationIndex += clippedTopRows * DrawingArea.width;
		}
		if (destinationY + visibleHeight > DrawingArea.bottomY) {
			visibleHeight -= destinationY + visibleHeight - DrawingArea.bottomY;
		}
		if (destinationX < DrawingArea.topX) {
			int clippedLeftColumns = DrawingArea.topX - destinationX;
			visibleWidth -= clippedLeftColumns;
			destinationX = DrawingArea.topX;
			sourceIndex += clippedLeftColumns;
			destinationIndex += clippedLeftColumns;
			sourceRowSkip += clippedLeftColumns;
			destinationRowSkip += clippedLeftColumns;
		}
		if (destinationX + visibleWidth > DrawingArea.bottomX) {
			int clippedRightColumns = destinationX + visibleWidth - DrawingArea.bottomX;
			visibleWidth -= clippedRightColumns;
			sourceRowSkip += clippedRightColumns;
			destinationRowSkip += clippedRightColumns;
		}
		if (!(visibleWidth <= 0 || visibleHeight <= 0)) {
			copyNonzeroIndexedRows(visibleHeight, DrawingArea.pixels, aByteArray1450, destinationRowSkip, destinationIndex, visibleWidth, sourceIndex, anIntArray1451, sourceRowSkip);
		}
	}

	private void copyNonzeroIndexedRows(int rows, int destination[], byte sourceIndices[], int destinationRowSkip, int destinationIndex, int width, int sourceIndex, int palette[], int sourceRowSkip) {
		int negativeBlocksOfFour = -(width >> 2);
		width = -(width & 3);
		for (int negativeRow = -rows; negativeRow < 0; negativeRow++) {
			for (int negativeBlock = negativeBlocksOfFour; negativeBlock < 0; negativeBlock++) {
				byte paletteIndex = sourceIndices[sourceIndex++];
				if (paletteIndex != 0) {
					destination[destinationIndex++] = palette[paletteIndex & 0xff];
				} else {
					destinationIndex++;
				}
				paletteIndex = sourceIndices[sourceIndex++];
				if (paletteIndex != 0) {
					destination[destinationIndex++] = palette[paletteIndex & 0xff];
				} else {
					destinationIndex++;
				}
				paletteIndex = sourceIndices[sourceIndex++];
				if (paletteIndex != 0) {
					destination[destinationIndex++] = palette[paletteIndex & 0xff];
				} else {
					destinationIndex++;
				}
				paletteIndex = sourceIndices[sourceIndex++];
				if (paletteIndex != 0) {
					destination[destinationIndex++] = palette[paletteIndex & 0xff];
				} else {
					destinationIndex++;
				}
			}

			for (int negativeTailColumn = width; negativeTailColumn < 0; negativeTailColumn++) {
				byte tailPaletteIndex = sourceIndices[sourceIndex++];
				if (tailPaletteIndex != 0) {
					destination[destinationIndex++] = palette[tailPaletteIndex & 0xff];
				} else {
					destinationIndex++;
				}
			}

			destinationIndex += destinationRowSkip;
			sourceIndex += sourceRowSkip;
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
