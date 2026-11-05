// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

public class DrawingArea extends NodeSub {

	public static void initDrawingArea(int i, int j, int ai[]) {
		pixels = ai;
		width = j;
		height = i;
		setDrawingArea(i, 0, j, 0);
	}

	public static void defaultDrawingAreaSize() {
		topX = 0;
		topY = 0;
		bottomX = width;
		bottomY = height;
		if (ClientSettings.FULL_512PX_VIEWPORT) {
			centerX = bottomX;
		} else {
			centerX = bottomX - 1;
		}
		centerY = bottomX / 2;
	}

	public static void setDrawingArea(int i, int j, int k, int l) {
		if (j < 0) {
			j = 0;
		}
		if (l < 0) {
			l = 0;
		}
		if (k > width) {
			k = width;
		}
		if (i > height) {
			i = height;
		}
		topX = j;
		topY = l;
		bottomX = k;
		bottomY = i;
		if (ClientSettings.FULL_512PX_VIEWPORT) {
			centerX = bottomX;
		} else {
			centerX = bottomX - 1;
		}
		centerY = bottomX / 2;
		anInt1387 = bottomY / 2;
	}

	public static void setAllPixelsToZero() {
		int i = width * height;
		for (int j = 0; j < i; j++) {
			pixels[j] = 0;
		}

	}

	public static void fillBlendedRectangle(int color, int y, int rectangleWidth, int rectangleHeight, int alpha256, int x) {
		if (x < topX) {
			rectangleWidth -= topX - x;
			x = topX;
		}
		if (y < topY) {
			rectangleHeight -= topY - y;
			y = topY;
		}
		if (x + rectangleWidth > bottomX) {
			rectangleWidth = bottomX - x;
		}
		if (y + rectangleHeight > bottomY) {
			rectangleHeight = bottomY - y;
		}
		int inverseAlpha256 = 256 - alpha256;
		int weightedSourceRed = (color >> 16 & 0xff) * alpha256;
		int weightedSourceGreen = (color >> 8 & 0xff) * alpha256;
		int weightedSourceBlue = (color & 0xff) * alpha256;
		int destinationRowSkip = width - rectangleWidth;
		int destinationIndex = x + y * width;
		for (int rowCounter = 0; rowCounter < rectangleHeight; rowCounter++) {
			for (int columnCounter = -rectangleWidth; columnCounter < 0; columnCounter++) {
				int weightedDestinationRed = (pixels[destinationIndex] >> 16 & 0xff) * inverseAlpha256;
				int weightedDestinationGreen = (pixels[destinationIndex] >> 8 & 0xff) * inverseAlpha256;
				int weightedDestinationBlue = (pixels[destinationIndex] & 0xff) * inverseAlpha256;
				int blendedColor = (weightedSourceRed + weightedDestinationRed >> 8 << 16) + (weightedSourceGreen + weightedDestinationGreen >> 8 << 8) + (weightedSourceBlue + weightedDestinationBlue >> 8);
				pixels[destinationIndex++] = blendedColor;
			}

			destinationIndex += destinationRowSkip;
		}
	}

	public static void fillRectangle(int rectangleHeight, int y, int color, int rectangleWidth, int x) {
		if (x < topX) {
			rectangleWidth -= topX - x;
			x = topX;
		}
		if (y < topY) {
			rectangleHeight -= topY - y;
			y = topY;
		}
		if (x + rectangleWidth > bottomX) {
			rectangleWidth = bottomX - x;
		}
		if (y + rectangleHeight > bottomY) {
			rectangleHeight = bottomY - y;
		}
		int destinationRowSkip = width - rectangleWidth;
		int destinationIndex = x + y * width;
		for (int rowCounter = -rectangleHeight; rowCounter < 0; rowCounter++) {
			for (int columnCounter = -rectangleWidth; columnCounter < 0; columnCounter++) {
				pixels[destinationIndex++] = color;
			}

			destinationIndex += destinationRowSkip;
		}

	}

	public static void drawRectangleOutline(int y, int rectangleHeight, int color, int x, int rectangleWidth) {
		drawHorizontalLine(y, color, rectangleWidth, x);
		drawHorizontalLine(y + rectangleHeight - 1, color, rectangleWidth, x);
		drawVerticalLine(y, color, rectangleHeight, x);
		drawVerticalLine(y, color, rectangleHeight, x + rectangleWidth - 1);
	}

	public static void drawBlendedRectangleOutline(int y, int rectangleHeight, int alpha256, int color, int rectangleWidth, int x) {
		drawBlendedHorizontalLine(color, rectangleWidth, y, alpha256, x);
		drawBlendedHorizontalLine(color, rectangleWidth, y + rectangleHeight - 1, alpha256, x);
		if (rectangleHeight >= 3) {
			drawBlendedVerticalLine(color, x, alpha256, y + 1, rectangleHeight - 2);
			drawBlendedVerticalLine(color, x + rectangleWidth - 1, alpha256, y + 1, rectangleHeight - 2);
		}
	}

	public static void drawHorizontalLine(int y, int color, int lineLength, int x) {
		if (y < topY || y >= bottomY) {
			return;
		}
		if (x < topX) {
			lineLength -= topX - x;
			x = topX;
		}
		if (x + lineLength > bottomX) {
			lineLength = bottomX - x;
		}
		int destinationIndex = x + y * width;
		for (int columnOffset = 0; columnOffset < lineLength; columnOffset++) {
			pixels[destinationIndex + columnOffset] = color;
		}

	}

	private static void drawBlendedHorizontalLine(int color, int lineLength, int y, int alpha256, int x) {
		if (y < topY || y >= bottomY) {
			return;
		}
		if (x < topX) {
			lineLength -= topX - x;
			x = topX;
		}
		if (x + lineLength > bottomX) {
			lineLength = bottomX - x;
		}
		int inverseAlpha256 = 256 - alpha256;
		int weightedSourceRed = (color >> 16 & 0xff) * alpha256;
		int weightedSourceGreen = (color >> 8 & 0xff) * alpha256;
		int weightedSourceBlue = (color & 0xff) * alpha256;
		int destinationIndex = x + y * width;
		for (int columnCounter = 0; columnCounter < lineLength; columnCounter++) {
			int weightedDestinationRed = (pixels[destinationIndex] >> 16 & 0xff) * inverseAlpha256;
			int weightedDestinationGreen = (pixels[destinationIndex] >> 8 & 0xff) * inverseAlpha256;
			int weightedDestinationBlue = (pixels[destinationIndex] & 0xff) * inverseAlpha256;
			int blendedColor = (weightedSourceRed + weightedDestinationRed >> 8 << 16) + (weightedSourceGreen + weightedDestinationGreen >> 8 << 8) + (weightedSourceBlue + weightedDestinationBlue >> 8);
			pixels[destinationIndex++] = blendedColor;
		}

	}

	public static void drawVerticalLine(int y, int color, int lineLength, int x) {
		if (x < topX || x >= bottomX) {
			return;
		}
		if (y < topY) {
			lineLength -= topY - y;
			y = topY;
		}
		if (y + lineLength > bottomY) {
			lineLength = bottomY - y;
		}
		int destinationIndex = x + y * width;
		for (int rowOffset = 0; rowOffset < lineLength; rowOffset++) {
			pixels[destinationIndex + rowOffset * width] = color;
		}

	}

	private static void drawBlendedVerticalLine(int color, int x, int alpha256, int y, int lineLength) {
		if (x < topX || x >= bottomX) {
			return;
		}
		if (y < topY) {
			lineLength -= topY - y;
			y = topY;
		}
		if (y + lineLength > bottomY) {
			lineLength = bottomY - y;
		}
		int inverseAlpha256 = 256 - alpha256;
		int weightedSourceRed = (color >> 16 & 0xff) * alpha256;
		int weightedSourceGreen = (color >> 8 & 0xff) * alpha256;
		int weightedSourceBlue = (color & 0xff) * alpha256;
		int destinationIndex = x + y * width;
		for (int rowCounter = 0; rowCounter < lineLength; rowCounter++) {
			int weightedDestinationRed = (pixels[destinationIndex] >> 16 & 0xff) * inverseAlpha256;
			int weightedDestinationGreen = (pixels[destinationIndex] >> 8 & 0xff) * inverseAlpha256;
			int weightedDestinationBlue = (pixels[destinationIndex] & 0xff) * inverseAlpha256;
			int blendedColor = (weightedSourceRed + weightedDestinationRed >> 8 << 16) + (weightedSourceGreen + weightedDestinationGreen >> 8 << 8) + (weightedSourceBlue + weightedDestinationBlue >> 8);
			pixels[destinationIndex] = blendedColor;
			destinationIndex += width;
		}

	}

	DrawingArea() {
	}

	public static int pixels[];
	public static int width;
	public static int height;
	public static int topY;
	public static int bottomY;
	public static int topX;
	public static int bottomX;
	public static int centerX;
	public static int centerY;
	public static int anInt1387;

}
