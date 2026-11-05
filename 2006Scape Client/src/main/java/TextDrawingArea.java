// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

import java.util.Random;

public final class TextDrawingArea extends DrawingArea {

	public TextDrawingArea(boolean useCapitalIForSpaceAdvance, String fontName, StreamLoader fontArchive) {
		glyphMasks = new byte[256][];
		glyphWidthPixels = new int[256];
		glyphHeightPixels = new int[256];
		glyphXOffsetPixels = new int[256];
		glyphYOffsetPixels = new int[256];
		glyphAdvancePixels = new int[256];
		jitterRandom = new Random();
		strikeThroughEnabled = false;
		Stream dataStream = new Stream(fontArchive.getDataForName(fontName + ".dat"));
		Stream indexStream = new Stream(fontArchive.getDataForName("index.dat"));
		indexStream.currentOffset = dataStream.readUnsignedShortBigEndian() + 4;
		int indexHeaderCount = indexStream.readUnsignedByte();
		if (indexHeaderCount > 0) {
			indexStream.currentOffset += 3 * (indexHeaderCount - 1);
		}
		for (int characterCode = 0; characterCode < 256; characterCode++) {
			glyphXOffsetPixels[characterCode] = indexStream.readUnsignedByte();
			glyphYOffsetPixels[characterCode] = indexStream.readUnsignedByte();
			int glyphWidth = glyphWidthPixels[characterCode] = indexStream.readUnsignedShortBigEndian();
			int glyphHeight = glyphHeightPixels[characterCode] = indexStream.readUnsignedShortBigEndian();
			int storageOrder = indexStream.readUnsignedByte();
			int pixelCount = glyphWidth * glyphHeight;
			glyphMasks[characterCode] = new byte[pixelCount];
			if (storageOrder == 0) {
				for (int pixelIndex = 0; pixelIndex < pixelCount; pixelIndex++) {
					glyphMasks[characterCode][pixelIndex] = dataStream.readSignedByte();
				}

			} else if (storageOrder == 1) {
				for (int column = 0; column < glyphWidth; column++) {
					for (int row = 0; row < glyphHeight; row++) {
						glyphMasks[characterCode][column + row * glyphWidth] = dataStream.readSignedByte();
					}

				}

			}
			if (glyphHeight > maxAsciiGlyphHeightPixels && characterCode < 128) {
				maxAsciiGlyphHeightPixels = glyphHeight;
			}
			glyphXOffsetPixels[characterCode] = 1;
			glyphAdvancePixels[characterCode] = glyphWidth + 2;
			int sampledEdgeSum = 0;
			for (int leftEdgeRow = glyphHeight / 7; leftEdgeRow < glyphHeight; leftEdgeRow++) {
				sampledEdgeSum += glyphMasks[characterCode][leftEdgeRow * glyphWidth];
			}

			if (sampledEdgeSum <= glyphHeight / 7) {
				glyphAdvancePixels[characterCode]--;
				glyphXOffsetPixels[characterCode] = 0;
			}
			sampledEdgeSum = 0;
			for (int rightEdgeRow = glyphHeight / 7; rightEdgeRow < glyphHeight; rightEdgeRow++) {
				sampledEdgeSum += glyphMasks[characterCode][glyphWidth - 1 + rightEdgeRow * glyphWidth];
			}

			if (sampledEdgeSum <= glyphHeight / 7) {
				glyphAdvancePixels[characterCode]--;
			}
		}

		if (useCapitalIForSpaceAdvance) {
			glyphAdvancePixels[32] = glyphAdvancePixels[73];
		} else {
			glyphAdvancePixels[32] = glyphAdvancePixels[105];
		}
	}

	public void drawRightAlignedLiteralText(int color, String text, int baselineY, int rightX) {
		drawLiteralText(color, text, baselineY, rightX - measureLiteralTextWidth(text));
	}

	public void drawCenteredLiteralText(int color, String text, int baselineY, int centerX) {
		drawLiteralText(color, text, baselineY, centerX - measureLiteralTextWidth(text) / 2);
	}

	public void drawCenteredTaggedText(int color, int centerX, String text, int baselineY, boolean shadow) {
		drawTaggedText(shadow, centerX - measureTaggedTextWidth(text) / 2, color, text, baselineY);
	}

	public void drawRightAlignedTaggedText(boolean shadow, int rightX, int color, String text, int baselineY) {
		drawTaggedText(shadow, rightX - measureTaggedTextWidth(text), color, text, baselineY);
	}

	public int measureTaggedTextWidth(String text) {
		if (text == null) {
			return 0;
		}
		int widthPixels = 0;
		for (int characterIndex = 0; characterIndex < text.length(); characterIndex++) {
			if (text.charAt(characterIndex) == '@' && characterIndex + 4 < text.length() && text.charAt(characterIndex + 4) == '@') {
				characterIndex += 4;
			} else {
				widthPixels += glyphAdvancePixels[text.charAt(characterIndex)];
			}
		}

		return widthPixels;
	}

	public int measureLiteralTextWidth(String text) {
		if (text == null) {
			return 0;
		}
		int widthPixels = 0;
		for (int characterIndex = 0; characterIndex < text.length(); characterIndex++) {
			widthPixels += glyphAdvancePixels[text.charAt(characterIndex)];
		}
		return widthPixels;
	}

	public void drawLiteralText(int color, String text, int baselineY, int x) {
		if (text == null) {
			return;
		}
		baselineY -= maxAsciiGlyphHeightPixels;
		for (int characterIndex = 0; characterIndex < text.length(); characterIndex++) {
			char character = text.charAt(characterIndex);
			if (character != ' ') {
				drawGlyphMask(glyphMasks[character], x + glyphXOffsetPixels[character], baselineY + glyphYOffsetPixels[character], glyphWidthPixels[character], glyphHeightPixels[character], color);
			}
			x += glyphAdvancePixels[character];
		}
	}

	public void drawCenteredVerticalWave(int color, String text, int centerX, int phase, int baselineY) {
		if (text == null) {
			return;
		}
		centerX -= measureLiteralTextWidth(text) / 2;
		baselineY -= maxAsciiGlyphHeightPixels;
		for (int characterIndex = 0; characterIndex < text.length(); characterIndex++) {
			char character = text.charAt(characterIndex);
			if (character != ' ') {
				drawGlyphMask(glyphMasks[character], centerX + glyphXOffsetPixels[character], baselineY + glyphYOffsetPixels[character] + (int) (Math.sin(characterIndex / 2D + phase / 5D) * 5D), glyphWidthPixels[character], glyphHeightPixels[character], color);
			}
			centerX += glyphAdvancePixels[character];
		}

	}

	public void drawCenteredWave(int centerX, String text, int phase, int baselineY, int color) {
		if (text == null) {
			return;
		}
		centerX -= measureLiteralTextWidth(text) / 2;
		baselineY -= maxAsciiGlyphHeightPixels;
		for (int characterIndex = 0; characterIndex < text.length(); characterIndex++) {
			char character = text.charAt(characterIndex);
			if (character != ' ') {
				drawGlyphMask(glyphMasks[character], centerX + glyphXOffsetPixels[character] + (int) (Math.sin(characterIndex / 5D + phase / 5D) * 5D), baselineY + glyphYOffsetPixels[character] + (int) (Math.sin(characterIndex / 3D + phase / 5D) * 5D), glyphWidthPixels[character], glyphHeightPixels[character], color);
			}
			centerX += glyphAdvancePixels[character];
		}

	}

	public void drawCenteredShake(int effectAge, String text, int phase, int baselineY, int centerX, int color) {
		if (text == null) {
			return;
		}
		double amplitudePixels = 7D - effectAge / 8D;
		if (amplitudePixels < 0.0D) {
			amplitudePixels = 0.0D;
		}
		centerX -= measureLiteralTextWidth(text) / 2;
		baselineY -= maxAsciiGlyphHeightPixels;
		for (int characterIndex = 0; characterIndex < text.length(); characterIndex++) {
			char character = text.charAt(characterIndex);
			if (character != ' ') {
				drawGlyphMask(glyphMasks[character], centerX + glyphXOffsetPixels[character], baselineY + glyphYOffsetPixels[character] + (int) (Math.sin(characterIndex / 1.5D + phase) * amplitudePixels), glyphWidthPixels[character], glyphHeightPixels[character], color);
			}
			centerX += glyphAdvancePixels[character];
		}

	}

	public void drawTaggedText(boolean shadow, int x, int color, String text, int baselineY) {
		strikeThroughEnabled = false;
		int startX = x;
		if (text == null) {
			return;
		}
		baselineY -= maxAsciiGlyphHeightPixels;
		for (int characterIndex = 0; characterIndex < text.length(); characterIndex++) {
			if (text.charAt(characterIndex) == '@' && characterIndex + 4 < text.length() && text.charAt(characterIndex + 4) == '@') {
				int tagColor = applyTextTag(text.substring(characterIndex + 1, characterIndex + 4));
				if (tagColor != -1) {
					color = tagColor;
				}
				characterIndex += 4;
			} else {
				char character = text.charAt(characterIndex);
				if (character != ' ') {
					if (shadow) {
						drawGlyphMask(glyphMasks[character], x + glyphXOffsetPixels[character] + 1, baselineY + glyphYOffsetPixels[character] + 1, glyphWidthPixels[character], glyphHeightPixels[character], 0);
					}
					try {
					drawGlyphMask(glyphMasks[character], x + glyphXOffsetPixels[character], baselineY + glyphYOffsetPixels[character], glyphWidthPixels[character], glyphHeightPixels[character], color);
					} catch (Exception glyphFailure) {
						
					}
				}
				x += glyphAdvancePixels[character];
			}
		}
		if (strikeThroughEnabled) {
			DrawingArea.drawHorizontalLine(baselineY + (int) (maxAsciiGlyphHeightPixels * 0.69999999999999996D), 0x800000, x - startX, startX);
		}
	}

	public void drawSeededJitterText(int x, int color, String text, int seed, int baselineY) {
		if (text == null) {
			return;
		}
		jitterRandom.setSeed(seed);
		int opacity256 = 192 + (jitterRandom.nextInt() & 0x1f);
		baselineY -= maxAsciiGlyphHeightPixels;
		for (int characterIndex = 0; characterIndex < text.length(); characterIndex++) {
			if (text.charAt(characterIndex) == '@' && characterIndex + 4 < text.length() && text.charAt(characterIndex + 4) == '@') {
				int tagColor = applyTextTag(text.substring(characterIndex + 1, characterIndex + 4));
				if (tagColor != -1) {
					color = tagColor;
				}
				characterIndex += 4;
			} else {
				char character = text.charAt(characterIndex);
				if (character != ' ') {
					drawBlendedGlyphMask(192, x + glyphXOffsetPixels[character] + 1, glyphMasks[character], glyphWidthPixels[character], baselineY + glyphYOffsetPixels[character] + 1, glyphHeightPixels[character], 0);
					drawBlendedGlyphMask(opacity256, x + glyphXOffsetPixels[character], glyphMasks[character], glyphWidthPixels[character], baselineY + glyphYOffsetPixels[character], glyphHeightPixels[character], color);
				}
				x += glyphAdvancePixels[character];
				if ((jitterRandom.nextInt() & 3) == 0) {
					x++;
				}
			}
		}

	}

	private int applyTextTag(String tag) {
		if (tag.equals("red")) {
			return 0xff0000;
		}
		if (tag.equals("gre")) {
			return 65280;
		}
		if (tag.equals("blu")) {
			return 255;
		}
		if (tag.equals("yel")) {
			return 0xffff00;
		}
		if (tag.equals("cya")) {
			return 65535;
		}
		if (tag.equals("mag")) {
			return 0xff00ff;
		}
		if (tag.equals("whi")) {
			return 0xffffff;
		}
		if (tag.equals("bla")) {
			return 0;
		}
		if (tag.equals("lre")) {
			return 0xff9040;
		}
		if (tag.equals("dre")) {
			return 0x800000;
		}
		if (tag.equals("dbl")) {
			return 128;
		}
		if (tag.equals("or1")) {
			return 0xffb000;
		}
		if (tag.equals("or2")) {
			return 0xff7000;
		}
		if (tag.equals("or3")) {
			return 0xff3000;
		}
		if (tag.equals("gr1")) {
			return 0xc0ff00;
		}
		if (tag.equals("gr2")) {
			return 0x80ff00;
		}
		if (tag.equals("gr3")) {
			return 0x40ff00;
		}
		if (tag.equals("str")) {
			strikeThroughEnabled = true;
		}
		if (tag.equals("end")) {
			strikeThroughEnabled = false;
		}
		return -1;
	}

	private void drawGlyphMask(byte mask[], int x, int y, int glyphWidth, int glyphHeight, int color) {
		int destinationIndex = x + y * DrawingArea.width;
		int destinationRowSkip = DrawingArea.width - glyphWidth;
		int sourceRowSkip = 0;
		int sourceIndex = 0;
		if (y < DrawingArea.topY) {
			int clippedTopRows = DrawingArea.topY - y;
			glyphHeight -= clippedTopRows;
			y = DrawingArea.topY;
			sourceIndex += clippedTopRows * glyphWidth;
			destinationIndex += clippedTopRows * DrawingArea.width;
		}
		if (y + glyphHeight >= DrawingArea.bottomY) {
			glyphHeight -= y + glyphHeight - DrawingArea.bottomY + 1;
		}
		if (x < DrawingArea.topX) {
			int clippedLeftColumns = DrawingArea.topX - x;
			glyphWidth -= clippedLeftColumns;
			x = DrawingArea.topX;
			sourceIndex += clippedLeftColumns;
			destinationIndex += clippedLeftColumns;
			sourceRowSkip += clippedLeftColumns;
			destinationRowSkip += clippedLeftColumns;
		}
		if (x + glyphWidth >= DrawingArea.bottomX) {
			int clippedRightColumns = x + glyphWidth - DrawingArea.bottomX + 1;
			glyphWidth -= clippedRightColumns;
			sourceRowSkip += clippedRightColumns;
			destinationRowSkip += clippedRightColumns;
		}
		if (!(glyphWidth <= 0 || glyphHeight <= 0)) {
			copyGlyphMaskRows(DrawingArea.pixels, mask, color, sourceIndex, destinationIndex, glyphWidth, glyphHeight, destinationRowSkip, sourceRowSkip);
		}
	}

	private void copyGlyphMaskRows(int destinationPixels[], byte mask[], int color, int sourceIndex, int destinationIndex, int glyphWidth, int glyphHeight, int destinationRowSkip, int sourceRowSkip) {
		int negativeGroupCount = -(glyphWidth >> 2);
		glyphWidth = -(glyphWidth & 3);
		for (int rowCounter = -glyphHeight; rowCounter < 0; rowCounter++) {
			for (int groupCounter = negativeGroupCount; groupCounter < 0; groupCounter++) {
				if (mask[sourceIndex++] != 0) {
					destinationPixels[destinationIndex++] = color;
				} else {
					destinationIndex++;
				}
				if (mask[sourceIndex++] != 0) {
					destinationPixels[destinationIndex++] = color;
				} else {
					destinationIndex++;
				}
				if (mask[sourceIndex++] != 0) {
					destinationPixels[destinationIndex++] = color;
				} else {
					destinationIndex++;
				}
				if (mask[sourceIndex++] != 0) {
					destinationPixels[destinationIndex++] = color;
				} else {
					destinationIndex++;
				}
			}

			for (int remainderCounter = glyphWidth; remainderCounter < 0; remainderCounter++) {
				if (mask[sourceIndex++] != 0) {
					destinationPixels[destinationIndex++] = color;
				} else {
					destinationIndex++;
				}
			}

			destinationIndex += destinationRowSkip;
			sourceIndex += sourceRowSkip;
		}

	}

	private void drawBlendedGlyphMask(int opacity256, int x, byte mask[], int glyphWidth, int y, int glyphHeight, int color) {
		int destinationIndex = x + y * DrawingArea.width;
		int destinationRowSkip = DrawingArea.width - glyphWidth;
		int sourceRowSkip = 0;
		int sourceIndex = 0;
		if (y < DrawingArea.topY) {
			int clippedTopRows = DrawingArea.topY - y;
			glyphHeight -= clippedTopRows;
			y = DrawingArea.topY;
			sourceIndex += clippedTopRows * glyphWidth;
			destinationIndex += clippedTopRows * DrawingArea.width;
		}
		if (y + glyphHeight >= DrawingArea.bottomY) {
			glyphHeight -= y + glyphHeight - DrawingArea.bottomY + 1;
		}
		if (x < DrawingArea.topX) {
			int clippedLeftColumns = DrawingArea.topX - x;
			glyphWidth -= clippedLeftColumns;
			x = DrawingArea.topX;
			sourceIndex += clippedLeftColumns;
			destinationIndex += clippedLeftColumns;
			sourceRowSkip += clippedLeftColumns;
			destinationRowSkip += clippedLeftColumns;
		}
		if (x + glyphWidth >= DrawingArea.bottomX) {
			int clippedRightColumns = x + glyphWidth - DrawingArea.bottomX + 1;
			glyphWidth -= clippedRightColumns;
			sourceRowSkip += clippedRightColumns;
			destinationRowSkip += clippedRightColumns;
		}
		if (glyphWidth <= 0 || glyphHeight <= 0) {
			return;
		}
		blendGlyphMaskRows(mask, glyphHeight, destinationIndex, DrawingArea.pixels, sourceIndex, glyphWidth, sourceRowSkip, destinationRowSkip, color, opacity256);
	}

	private void blendGlyphMaskRows(byte mask[], int glyphHeight, int destinationIndex, int destinationPixels[], int sourceIndex, int glyphWidth, int sourceRowSkip, int destinationRowSkip, int color, int opacity256) {
		color = ((color & 0xff00ff) * opacity256 & 0xff00ff00) + ((color & 0xff00) * opacity256 & 0xff0000) >> 8;
		opacity256 = 256 - opacity256;
		for (int rowCounter = -glyphHeight; rowCounter < 0; rowCounter++) {
			for (int columnCounter = -glyphWidth; columnCounter < 0; columnCounter++) {
				if (mask[sourceIndex++] != 0) {
					int destinationColor = destinationPixels[destinationIndex];
					destinationPixels[destinationIndex++] = (((destinationColor & 0xff00ff) * opacity256 & 0xff00ff00) + ((destinationColor & 0xff00) * opacity256 & 0xff0000) >> 8) + color;
				} else {
					destinationIndex++;
				}
			}

			destinationIndex += destinationRowSkip;
			sourceIndex += sourceRowSkip;
		}

	}

	private final byte[][] glyphMasks;
	private final int[] glyphWidthPixels;
	private final int[] glyphHeightPixels;
	private final int[] glyphXOffsetPixels;
	private final int[] glyphYOffsetPixels;
	private final int[] glyphAdvancePixels;
	public int maxAsciiGlyphHeightPixels;
	private final Random jitterRandom;
	private boolean strikeThroughEnabled;
}
