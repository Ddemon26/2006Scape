// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

public final class Background extends DrawingArea {

	public Background(StreamLoader streamLoader, String imageName, int imageIndex) {
		Stream pixelData = new Stream(streamLoader.getDataForName(imageName + ".dat"));
		Stream indexData = new Stream(streamLoader.getDataForName("index.dat"));
		indexData.currentOffset = pixelData.readUnsignedWord();
		canvasWidth = indexData.readUnsignedWord();
		canvasHeight = indexData.readUnsignedWord();
		int paletteSize = indexData.readUnsignedByte();
		paletteRgb = new int[paletteSize];
		for (int paletteEntry = 0; paletteEntry < paletteSize - 1; paletteEntry++) {
			paletteRgb[paletteEntry + 1] = indexData.read3Bytes();
		}

		for (int skippedImage = 0; skippedImage < imageIndex; skippedImage++) {
			indexData.currentOffset += 2;
			pixelData.currentOffset += indexData.readUnsignedWord() * indexData.readUnsignedWord();
			indexData.currentOffset++;
		}

		offsetX = indexData.readUnsignedByte();
		offsetY = indexData.readUnsignedByte();
		imageWidth = indexData.readUnsignedWord();
		imageHeight = indexData.readUnsignedWord();
		int storageOrder = indexData.readUnsignedByte();
		int pixelCount = imageWidth * imageHeight;
		paletteIndices = new byte[pixelCount];
		if (storageOrder == 0) {
			for (int pixelIndex = 0; pixelIndex < pixelCount; pixelIndex++) {
				paletteIndices[pixelIndex] = pixelData.readSignedByte();
			}

			return;
		}
		if (storageOrder == 1) {
			for (int imageX = 0; imageX < imageWidth; imageX++) {
				for (int imageY = 0; imageY < imageHeight; imageY++) {
					paletteIndices[imageX + imageY * imageWidth] = pixelData.readSignedByte();
				}

			}

		}
	}

	public void reduceToHalfCanvas() {
		canvasWidth /= 2;
		canvasHeight /= 2;
		byte canvasIndices[] = new byte[canvasWidth * canvasHeight];
		int sourceIndex = 0;
		for (int sourceY = 0; sourceY < imageHeight; sourceY++) {
			for (int sourceX = 0; sourceX < imageWidth; sourceX++) {
				canvasIndices[(sourceX + offsetX >> 1) + (sourceY + offsetY >> 1) * canvasWidth] = paletteIndices[sourceIndex++];
			}

		}

		paletteIndices = canvasIndices;
		imageWidth = canvasWidth;
		imageHeight = canvasHeight;
		offsetX = 0;
		offsetY = 0;
	}

	public void expandToCanvas() {
		if (imageWidth == canvasWidth && imageHeight == canvasHeight) {
			return;
		}
		byte canvasIndices[] = new byte[canvasWidth * canvasHeight];
		int sourceIndex = 0;
		for (int sourceY = 0; sourceY < imageHeight; sourceY++) {
			for (int sourceX = 0; sourceX < imageWidth; sourceX++) {
				canvasIndices[sourceX + offsetX + (sourceY + offsetY) * canvasWidth] = paletteIndices[sourceIndex++];
			}

		}

		paletteIndices = canvasIndices;
		imageWidth = canvasWidth;
		imageHeight = canvasHeight;
		offsetX = 0;
		offsetY = 0;
	}

	public void flipHorizontally() {
		byte mirroredIndices[] = new byte[imageWidth * imageHeight];
		int destinationIndex = 0;
		for (int sourceY = 0; sourceY < imageHeight; sourceY++) {
			for (int sourceX = imageWidth - 1; sourceX >= 0; sourceX--) {
				mirroredIndices[destinationIndex++] = paletteIndices[sourceX + sourceY * imageWidth];
			}

		}

		paletteIndices = mirroredIndices;
		offsetX = canvasWidth - imageWidth - offsetX;
	}

	public void flipVertically() {
		byte mirroredIndices[] = new byte[imageWidth * imageHeight];
		int destinationIndex = 0;
		for (int sourceY = imageHeight - 1; sourceY >= 0; sourceY--) {
			for (int sourceX = 0; sourceX < imageWidth; sourceX++) {
				mirroredIndices[destinationIndex++] = paletteIndices[sourceX + sourceY * imageWidth];
			}

		}

		paletteIndices = mirroredIndices;
		offsetY = canvasHeight - imageHeight - offsetY;
	}

	public void adjustPaletteRgb(int redDelta, int greenDelta, int blueDelta) {
		for (int paletteIndex = 0; paletteIndex < paletteRgb.length; paletteIndex++) {
			int red = paletteRgb[paletteIndex] >> 16 & 0xff;
			red += redDelta;
			if (red < 0) {
				red = 0;
			} else if (red > 255) {
				red = 255;
			}
			int green = paletteRgb[paletteIndex] >> 8 & 0xff;
			green += greenDelta;
			if (green < 0) {
				green = 0;
			} else if (green > 255) {
				green = 255;
			}
			int blue = paletteRgb[paletteIndex] & 0xff;
			blue += blueDelta;
			if (blue < 0) {
				blue = 0;
			} else if (blue > 255) {
				blue = 255;
			}
			paletteRgb[paletteIndex] = (red << 16) + (green << 8) + blue;
		}
	}

	public void drawNonzeroIndexed(int destinationX, int destinationY) {
		destinationX += offsetX;
		destinationY += offsetY;
		int destinationIndex = destinationX + destinationY * DrawingArea.width;
		int sourceIndex = 0;
		int visibleHeight = imageHeight;
		int visibleWidth = imageWidth;
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
			copyNonzeroIndexedRows(visibleHeight, DrawingArea.pixels, paletteIndices, destinationRowSkip, destinationIndex, visibleWidth, sourceIndex, paletteRgb, sourceRowSkip);
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

	public byte paletteIndices[];
	public final int[] paletteRgb;
	public int imageWidth;
	public int imageHeight;
	public int offsetX;
	public int offsetY;
	public int canvasWidth;
	private int canvasHeight;
}
