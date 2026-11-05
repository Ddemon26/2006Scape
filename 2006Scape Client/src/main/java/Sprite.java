// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

public final class Sprite extends DrawingArea {

	public Sprite(int imageWidth, int imageHeight) {
		pixels = new int[imageWidth * imageHeight];
		width = trimWidth = imageWidth;
		height = trimHeight = imageHeight;
		offsetX = offsetY = 0;
	}

	public Sprite(byte encodedImageBytes[]) {
		this(encodedImageBytes, null);
	}

	public Sprite(byte encodedImageBytes[], Object component) {
		if (ClientSettings.ANDROID) {
			AndroidHooks.decodeImage(this, encodedImageBytes);
		} else {
			DesktopHooks.decodeImage(this, encodedImageBytes, component);
		}
	}

	public Sprite(StreamLoader streamLoader, String imageName, int imageIndex) {
		Stream pixelData = new Stream(streamLoader.getDataForName(imageName + ".dat"));
		Stream indexData = new Stream(streamLoader.getDataForName("index.dat"));
		indexData.currentOffset = pixelData.readUnsignedShortBigEndian();
		trimWidth = indexData.readUnsignedShortBigEndian();
		trimHeight = indexData.readUnsignedShortBigEndian();
		int paletteSize = indexData.readUnsignedByte();
		int paletteRgb[] = new int[paletteSize];
		for (int paletteEntry = 0; paletteEntry < paletteSize - 1; paletteEntry++) {
			paletteRgb[paletteEntry + 1] = indexData.readUnsignedMediumBigEndian();
			if (paletteRgb[paletteEntry + 1] == 0) {
				paletteRgb[paletteEntry + 1] = 1;
			}
		}

		for (int skippedImage = 0; skippedImage < imageIndex; skippedImage++) {
			indexData.currentOffset += 2;
			pixelData.currentOffset += indexData.readUnsignedShortBigEndian() * indexData.readUnsignedShortBigEndian();
			indexData.currentOffset++;
		}

		offsetX = indexData.readUnsignedByte();
		offsetY = indexData.readUnsignedByte();
		width = indexData.readUnsignedShortBigEndian();
		height = indexData.readUnsignedShortBigEndian();
		int storageOrder = indexData.readUnsignedByte();
		int pixelCount = width * height;
		pixels = new int[pixelCount];
		if (storageOrder == 0) {
			for (int pixelIndex = 0; pixelIndex < pixelCount; pixelIndex++) {
				pixels[pixelIndex] = paletteRgb[pixelData.readUnsignedByte()];
			}

			return;
		}
		if (storageOrder == 1) {
			for (int imageX = 0; imageX < width; imageX++) {
				for (int imageY = 0; imageY < height; imageY++) {
					pixels[imageX + imageY * width] = paletteRgb[pixelData.readUnsignedByte()];
				}

			}

		}
	}

	public void setAsDrawingArea() {
		DrawingArea.bindRaster(height, width, pixels);
	}

	public void adjustRgb(int redDelta, int greenDelta, int blueDelta) {
		for (int pixelIndex = 0; pixelIndex < pixels.length; pixelIndex++) {
			int sourcePixel = pixels[pixelIndex];
			if (sourcePixel != 0) {
				int red = sourcePixel >> 16 & 0xff;
				red += redDelta;
				if (red < 1) {
					red = 1;
				} else if (red > 255) {
					red = 255;
				}
				int green = sourcePixel >> 8 & 0xff;
				green += greenDelta;
				if (green < 1) {
					green = 1;
				} else if (green > 255) {
					green = 255;
				}
				int blue = sourcePixel & 0xff;
				blue += blueDelta;
				if (blue < 1) {
					blue = 1;
				} else if (blue > 255) {
					blue = 255;
				}
				pixels[pixelIndex] = (red << 16) + (green << 8) + blue;
			}
		}

	}

	public void expandToCanvas() {
		/*int ai[] = new int[trimWidth * trimHeight];
		for (int j = 0; j < height; j++) {
			System.arraycopy(pixels, j * width, ai, j + offsetY * trimWidth + offsetX, width);
		}

		pixels = ai;
		width = trimWidth;
		height = trimHeight;
		offsetX = 0;
		offsetY = 0;*/
		int canvasPixels[] = new int[trimWidth * trimHeight];
		for (int sourceY = 0; sourceY < height; sourceY++) {
			for (int sourceX = 0; sourceX < width; sourceX++)
				canvasPixels[(sourceY + offsetY) * trimWidth + (sourceX + offsetX)] = pixels[sourceY
						* width + sourceX];
		}
		pixels = canvasPixels;
		width = trimWidth;
		height = trimHeight;
		offsetX = 0;
		offsetY = 0;
	}

	public void drawOpaque(int destinationX, int destinationY) {
		destinationX += offsetX;
		destinationY += offsetY;
		int destinationIndex = destinationX + destinationY * DrawingArea.width;
		int sourceIndex = 0;
		int copyHeight = height;
		int copyWidth = width;
		int destinationRowSkip = DrawingArea.width - copyWidth;
		int sourceRowSkip = 0;
		if (destinationY < DrawingArea.topY) {
			int clippedTopRows = DrawingArea.topY - destinationY;
			copyHeight -= clippedTopRows;
			destinationY = DrawingArea.topY;
			sourceIndex += clippedTopRows * copyWidth;
			destinationIndex += clippedTopRows * DrawingArea.width;
		}
		if (destinationY + copyHeight > DrawingArea.bottomY) {
			copyHeight -= destinationY + copyHeight - DrawingArea.bottomY;
		}
		if (destinationX < DrawingArea.topX) {
			int clippedLeftColumns = DrawingArea.topX - destinationX;
			copyWidth -= clippedLeftColumns;
			destinationX = DrawingArea.topX;
			sourceIndex += clippedLeftColumns;
			destinationIndex += clippedLeftColumns;
			sourceRowSkip += clippedLeftColumns;
			destinationRowSkip += clippedLeftColumns;
		}
		if (destinationX + copyWidth > DrawingArea.bottomX) {
			int clippedRightColumns = destinationX + copyWidth - DrawingArea.bottomX;
			copyWidth -= clippedRightColumns;
			sourceRowSkip += clippedRightColumns;
			destinationRowSkip += clippedRightColumns;
		}
		if (copyWidth <= 0 || copyHeight <= 0) {
		} else {
			copyOpaqueRows(destinationIndex, copyWidth, copyHeight, sourceRowSkip, sourceIndex, destinationRowSkip, pixels, DrawingArea.pixels);
		}
	}

	private void copyOpaqueRows(int destinationIndex, int copyWidth, int rowCount, int sourceRowSkip, int sourceIndex, int destinationRowSkip, int source[], int destination[]) {
		int negativeFourPixelBlockCount = -(copyWidth >> 2);
		copyWidth = -(copyWidth & 3);
		for (int rowCounter = -rowCount; rowCounter < 0; rowCounter++) {
			for (int fourPixelBlockCounter = negativeFourPixelBlockCount; fourPixelBlockCounter < 0; fourPixelBlockCounter++) {
				destination[destinationIndex++] = source[sourceIndex++];
				destination[destinationIndex++] = source[sourceIndex++];
				destination[destinationIndex++] = source[sourceIndex++];
				destination[destinationIndex++] = source[sourceIndex++];
			}

			for (int tailPixelCounter = copyWidth; tailPixelCounter < 0; tailPixelCounter++) {
				destination[destinationIndex++] = source[sourceIndex++];
			}

			destinationIndex += destinationRowSkip;
			sourceIndex += sourceRowSkip;
		}
	}

	public void drawHalfBlended(int destinationX, int destinationY) {
		int sourceWeight256 = 128;// was parameter
		destinationX += offsetX;
		destinationY += offsetY;
		int destinationIndex = destinationX + destinationY * DrawingArea.width;
		int sourceIndex = 0;
		int copyHeight = height;
		int copyWidth = width;
		int destinationRowSkip = DrawingArea.width - copyWidth;
		int sourceRowSkip = 0;
		if (destinationY < DrawingArea.topY) {
			int clippedTopRows = DrawingArea.topY - destinationY;
			copyHeight -= clippedTopRows;
			destinationY = DrawingArea.topY;
			sourceIndex += clippedTopRows * copyWidth;
			destinationIndex += clippedTopRows * DrawingArea.width;
		}
		if (destinationY + copyHeight > DrawingArea.bottomY) {
			copyHeight -= destinationY + copyHeight - DrawingArea.bottomY;
		}
		if (destinationX < DrawingArea.topX) {
			int clippedLeftColumns = DrawingArea.topX - destinationX;
			copyWidth -= clippedLeftColumns;
			destinationX = DrawingArea.topX;
			sourceIndex += clippedLeftColumns;
			destinationIndex += clippedLeftColumns;
			sourceRowSkip += clippedLeftColumns;
			destinationRowSkip += clippedLeftColumns;
		}
		if (destinationX + copyWidth > DrawingArea.bottomX) {
			int clippedRightColumns = destinationX + copyWidth - DrawingArea.bottomX;
			copyWidth -= clippedRightColumns;
			sourceRowSkip += clippedRightColumns;
			destinationRowSkip += clippedRightColumns;
		}
		if (!(copyWidth <= 0 || copyHeight <= 0)) {
			blendNonzeroRows(sourceIndex, copyWidth, DrawingArea.pixels, pixels, sourceRowSkip, copyHeight, destinationRowSkip, sourceWeight256, destinationIndex);
		}
	}

	public void drawSprite(int destinationX, int destinationY) {
		destinationX += offsetX;
		destinationY += offsetY;
		int destinationIndex = destinationX + destinationY * DrawingArea.width;
		int sourceIndex = 0;
		int copyHeight = height;
		int copyWidth = width;
		int destinationRowSkip = DrawingArea.width - copyWidth;
		int sourceRowSkip = 0;
		if (destinationY < DrawingArea.topY) {
			int clippedTopRows = DrawingArea.topY - destinationY;
			copyHeight -= clippedTopRows;
			destinationY = DrawingArea.topY;
			sourceIndex += clippedTopRows * copyWidth;
			destinationIndex += clippedTopRows * DrawingArea.width;
		}
		if (destinationY + copyHeight > DrawingArea.bottomY) {
			copyHeight -= destinationY + copyHeight - DrawingArea.bottomY;
		}
		if (destinationX < DrawingArea.topX) {
			int clippedLeftColumns = DrawingArea.topX - destinationX;
			copyWidth -= clippedLeftColumns;
			destinationX = DrawingArea.topX;
			sourceIndex += clippedLeftColumns;
			destinationIndex += clippedLeftColumns;
			sourceRowSkip += clippedLeftColumns;
			destinationRowSkip += clippedLeftColumns;
		}
		if (destinationX + copyWidth > DrawingArea.bottomX) {
			int clippedRightColumns = destinationX + copyWidth - DrawingArea.bottomX;
			copyWidth -= clippedRightColumns;
			sourceRowSkip += clippedRightColumns;
			destinationRowSkip += clippedRightColumns;
		}
		if (!(copyWidth <= 0 || copyHeight <= 0)) {
			copyNonzeroRows(DrawingArea.pixels, pixels, sourceIndex, destinationIndex, copyWidth, copyHeight, destinationRowSkip, sourceRowSkip);
		}
	}

	private void copyNonzeroRows(int destination[], int source[], int sourceIndex, int destinationIndex, int copyWidth, int rowCount, int destinationRowSkip, int sourceRowSkip) {
		int sourcePixel;// was parameter
		int negativeFourPixelBlockCount = -(copyWidth >> 2);
		copyWidth = -(copyWidth & 3);
		for (int rowCounter = -rowCount; rowCounter < 0; rowCounter++) {
			for (int fourPixelBlockCounter = negativeFourPixelBlockCount; fourPixelBlockCounter < 0; fourPixelBlockCounter++) {
				sourcePixel = source[sourceIndex++];
				if (sourcePixel != 0) {
					destination[destinationIndex++] = sourcePixel;
				} else {
					destinationIndex++;
				}
				sourcePixel = source[sourceIndex++];
				if (sourcePixel != 0) {
					destination[destinationIndex++] = sourcePixel;
				} else {
					destinationIndex++;
				}
				sourcePixel = source[sourceIndex++];
				if (sourcePixel != 0) {
					destination[destinationIndex++] = sourcePixel;
				} else {
					destinationIndex++;
				}
				sourcePixel = source[sourceIndex++];
				if (sourcePixel != 0) {
					destination[destinationIndex++] = sourcePixel;
				} else {
					destinationIndex++;
				}
			}

			for (int tailPixelCounter = copyWidth; tailPixelCounter < 0; tailPixelCounter++) {
				sourcePixel = source[sourceIndex++];
				if (sourcePixel != 0) {
					destination[destinationIndex++] = sourcePixel;
				} else {
					destinationIndex++;
				}
			}

			destinationIndex += destinationRowSkip;
			sourceIndex += sourceRowSkip;
		}

	}

	private void blendNonzeroRows(int sourceIndex, int copyWidth, int destination[], int source[], int sourceRowSkip, int rowCount, int destinationRowSkip, int sourceWeight256, int destinationIndex) {
		int sourcePixel;// was parameter
		int destinationWeight256 = 256 - sourceWeight256;
		for (int rowCounter = -rowCount; rowCounter < 0; rowCounter++) {
			for (int columnCounter = -copyWidth; columnCounter < 0; columnCounter++) {
				sourcePixel = source[sourceIndex++];
				if (sourcePixel != 0) {
					int destinationPixel = destination[destinationIndex];
					destination[destinationIndex++] = ((sourcePixel & 0xff00ff) * sourceWeight256 + (destinationPixel & 0xff00ff) * destinationWeight256 & 0xff00ff00) + ((sourcePixel & 0xff00) * sourceWeight256 + (destinationPixel & 0xff00) * destinationWeight256 & 0xff0000) >> 8;
				} else {
					destinationIndex++;
				}
			}

			destinationIndex += destinationRowSkip;
			sourceIndex += sourceRowSkip;
		}
	}

	public void drawRotatedScanlines(int rowCount, int angleUnits, int rowPixelCounts[], int sourceScale256, int rowStartOffsets[], int sourceCenterY, int destinationY, int destinationX, int horizontalCenteringSpan, int sourceCenterX) {
		try {
			int centeredColumnOffset = -horizontalCenteringSpan / 2;
			int centeredRowOffset = -rowCount / 2;
			int sineStep16 = (int) (Math.sin(angleUnits / 326.11000000000001D) * 65536D);
			int cosineStep16 = (int) (Math.cos(angleUnits / 326.11000000000001D) * 65536D);
			sineStep16 = sineStep16 * sourceScale256 >> 8;
			cosineStep16 = cosineStep16 * sourceScale256 >> 8;
			int rowSourceX16 = (sourceCenterX << 16) + centeredRowOffset * sineStep16 + centeredColumnOffset * cosineStep16;
			int rowSourceY16 = (sourceCenterY << 16) + centeredRowOffset * cosineStep16 - centeredColumnOffset * sineStep16;
			int destinationRowIndex = destinationX + destinationY * DrawingArea.width;
			for (destinationY = 0; destinationY < rowCount; destinationY++) {
				int rowStartColumn = rowStartOffsets[destinationY];
				int destinationIndex = destinationRowIndex + rowStartColumn;
				int sourceX16 = rowSourceX16 + cosineStep16 * rowStartColumn;
				int sourceY16 = rowSourceY16 - sineStep16 * rowStartColumn;
				for (destinationX = -rowPixelCounts[destinationY]; destinationX < 0; destinationX++) {
					if (ClientSettings.BILINEAR_MINIMAP_FILTERING) {
						int sourceLeft = sourceX16 >> 16;
						int sourceTop = sourceY16 >> 16;
						int sourceRight = sourceLeft + 1;
						int sourceBottom = sourceTop + 1;
						int topLeftColor = pixels[sourceLeft + sourceTop * width];
						int topRightColor = pixels[sourceRight + sourceTop * width];
						int bottomLeftColor = pixels[sourceLeft + sourceBottom * width];
						int bottomRightColor = pixels[sourceRight + sourceBottom * width];
						int xFraction256 = (sourceX16 >> 8) - (sourceLeft << 8);
						int yFraction256 = (sourceY16 >> 8) - (sourceTop << 8);
						int inverseXFraction256 = (sourceRight << 8) - (sourceX16 >> 8);
						int inverseYFraction256 = (sourceBottom << 8) - (sourceY16 >> 8);
						int topLeftWeight65536 = inverseXFraction256 * inverseYFraction256;
						int topRightWeight65536 = xFraction256 * inverseYFraction256;
						int bottomLeftWeight65536 = inverseXFraction256 * yFraction256;
						int bottomRightWeight65536 = xFraction256 * yFraction256;
						int red = (topLeftColor >> 16 & 0xff) * topLeftWeight65536 + (topRightColor >> 16 & 0xff) * topRightWeight65536 + (bottomLeftColor >> 16 & 0xff) * bottomLeftWeight65536 + (bottomRightColor >> 16 & 0xff) * bottomRightWeight65536 & 0xff0000;
						int green = (topLeftColor >> 8 & 0xff) * topLeftWeight65536 + (topRightColor >> 8 & 0xff) * topRightWeight65536 + (bottomLeftColor >> 8 & 0xff) * bottomLeftWeight65536 + (bottomRightColor >> 8 & 0xff) * bottomRightWeight65536 >> 8 & 0xff00;
						int blue = (topLeftColor & 0xff) * topLeftWeight65536 + (topRightColor & 0xff) * topRightWeight65536 + (bottomLeftColor & 0xff) * bottomLeftWeight65536 + (bottomRightColor & 0xff) * bottomRightWeight65536 >> 16;
						DrawingArea.pixels[destinationIndex++] = red | green | blue;
					} else {
						DrawingArea.pixels[destinationIndex++] = pixels[(sourceX16 >> 16) + (sourceY16 >> 16) * width];
					}
					sourceX16 += cosineStep16;
					sourceY16 -= sineStep16;
				}

				rowSourceX16 += sineStep16;
				rowSourceY16 += cosineStep16;
				destinationRowIndex += DrawingArea.width;
			}

		} catch (Exception _ex) {
		}
	}

	public void drawRotatedNonzero20x20(int destinationY, double angleRadians, int destinationX) {
		// all of the following were parameters
		int sourceCenterY = 15;
		int columnCount = 20;
		int sourceCenterX = 15;
		int sourceScale256 = 256;
		int rowCount = 20;
		// all of the previous were parameters
		try {
			int centeredColumnOffset = -columnCount / 2;
			int centeredRowOffset = -rowCount / 2;
			int sineStep16 = (int) (Math.sin(angleRadians) * 65536D);
			int cosineStep16 = (int) (Math.cos(angleRadians) * 65536D);
			sineStep16 = sineStep16 * sourceScale256 >> 8;
			cosineStep16 = cosineStep16 * sourceScale256 >> 8;
			int rowSourceX16 = (sourceCenterX << 16) + centeredRowOffset * sineStep16 + centeredColumnOffset * cosineStep16;
			int rowSourceY16 = (sourceCenterY << 16) + centeredRowOffset * cosineStep16 - centeredColumnOffset * sineStep16;
			int destinationRowIndex = destinationX + destinationY * DrawingArea.width;
			for (destinationY = 0; destinationY < rowCount; destinationY++) {
				int destinationIndex = destinationRowIndex;
				int sourceX16 = rowSourceX16;
				int sourceY16 = rowSourceY16;
				for (destinationX = -columnCount; destinationX < 0; destinationX++) {
					int sampleColor = pixels[(sourceX16 >> 16) + (sourceY16 >> 16) * width];
					if (sampleColor != 0) {
						DrawingArea.pixels[destinationIndex++] = sampleColor;
					} else {
						destinationIndex++;
					}
					sourceX16 += cosineStep16;
					sourceY16 -= sineStep16;
				}

				rowSourceX16 += sineStep16;
				rowSourceY16 += cosineStep16;
				destinationRowIndex += DrawingArea.width;
			}

		} catch (Exception _ex) {
		}
	}

	public void drawNonzeroThroughZeroMask(Background maskBackground, int destinationY, int destinationX) {
		destinationX += offsetX;
		destinationY += offsetY;
		int destinationIndex = destinationX + destinationY * DrawingArea.width;
		int sourceIndex = 0;
		int copyHeight = height;
		int copyWidth = width;
		int destinationRowSkip = DrawingArea.width - copyWidth;
		int sourceRowSkip = 0;
		if (destinationY < DrawingArea.topY) {
			int clippedTopRows = DrawingArea.topY - destinationY;
			copyHeight -= clippedTopRows;
			destinationY = DrawingArea.topY;
			sourceIndex += clippedTopRows * copyWidth;
			destinationIndex += clippedTopRows * DrawingArea.width;
		}
		if (destinationY + copyHeight > DrawingArea.bottomY) {
			copyHeight -= destinationY + copyHeight - DrawingArea.bottomY;
		}
		if (destinationX < DrawingArea.topX) {
			int clippedLeftColumns = DrawingArea.topX - destinationX;
			copyWidth -= clippedLeftColumns;
			destinationX = DrawingArea.topX;
			sourceIndex += clippedLeftColumns;
			destinationIndex += clippedLeftColumns;
			sourceRowSkip += clippedLeftColumns;
			destinationRowSkip += clippedLeftColumns;
		}
		if (destinationX + copyWidth > DrawingArea.bottomX) {
			int clippedRightColumns = destinationX + copyWidth - DrawingArea.bottomX;
			copyWidth -= clippedRightColumns;
			sourceRowSkip += clippedRightColumns;
			destinationRowSkip += clippedRightColumns;
		}
		if (!(copyWidth <= 0 || copyHeight <= 0)) {
			copyNonzeroRowsThroughZeroMask(pixels, copyWidth, maskBackground.paletteIndices, copyHeight, DrawingArea.pixels, 0, destinationRowSkip, destinationIndex, sourceRowSkip, sourceIndex);
		}
	}

	private void copyNonzeroRowsThroughZeroMask(int source[], int copyWidth, byte mask[], int rowCount, int destination[], int sourcePixel, int destinationRowSkip, int destinationIndex, int sourceRowSkip, int sourceIndex) {
		int negativeFourPixelBlockCount = -(copyWidth >> 2);
		copyWidth = -(copyWidth & 3);
		for (int rowCounter = -rowCount; rowCounter < 0; rowCounter++) {
			for (int fourPixelBlockCounter = negativeFourPixelBlockCount; fourPixelBlockCounter < 0; fourPixelBlockCounter++) {
				sourcePixel = source[sourceIndex++];
				if (sourcePixel != 0 && mask[destinationIndex] == 0) {
					destination[destinationIndex++] = sourcePixel;
				} else {
					destinationIndex++;
				}
				sourcePixel = source[sourceIndex++];
				if (sourcePixel != 0 && mask[destinationIndex] == 0) {
					destination[destinationIndex++] = sourcePixel;
				} else {
					destinationIndex++;
				}
				sourcePixel = source[sourceIndex++];
				if (sourcePixel != 0 && mask[destinationIndex] == 0) {
					destination[destinationIndex++] = sourcePixel;
				} else {
					destinationIndex++;
				}
				sourcePixel = source[sourceIndex++];
				if (sourcePixel != 0 && mask[destinationIndex] == 0) {
					destination[destinationIndex++] = sourcePixel;
				} else {
					destinationIndex++;
				}
			}

			for (int tailPixelCounter = copyWidth; tailPixelCounter < 0; tailPixelCounter++) {
				sourcePixel = source[sourceIndex++];
				if (sourcePixel != 0 && mask[destinationIndex] == 0) {
					destination[destinationIndex++] = sourcePixel;
				} else {
					destinationIndex++;
				}
			}

			destinationIndex += destinationRowSkip;
			sourceIndex += sourceRowSkip;
		}

	}

	public int pixels[];
	public int width;
	public int height;
	int offsetX;
	int offsetY;
	public int trimWidth;
	public int trimHeight;
}
