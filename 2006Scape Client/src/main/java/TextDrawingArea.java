// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

import java.util.Random;

public final class TextDrawingArea extends DrawingArea {

	public TextDrawingArea(boolean flag, String s, StreamLoader streamLoader) {
		aByteArrayArray1491 = new byte[256][];
		anIntArray1492 = new int[256];
		anIntArray1493 = new int[256];
		anIntArray1494 = new int[256];
		anIntArray1495 = new int[256];
		glyphAdvancePixels = new int[256];
		jitterRandom = new Random();
		strikeThroughEnabled = false;
		Stream stream = new Stream(streamLoader.getDataForName(s + ".dat"));
		Stream stream_1 = new Stream(streamLoader.getDataForName("index.dat"));
		stream_1.currentOffset = stream.readUnsignedWord() + 4;
		int k = stream_1.readUnsignedByte();
		if (k > 0) {
			stream_1.currentOffset += 3 * (k - 1);
		}
		for (int l = 0; l < 256; l++) {
			anIntArray1494[l] = stream_1.readUnsignedByte();
			anIntArray1495[l] = stream_1.readUnsignedByte();
			int i1 = anIntArray1492[l] = stream_1.readUnsignedWord();
			int j1 = anIntArray1493[l] = stream_1.readUnsignedWord();
			int k1 = stream_1.readUnsignedByte();
			int l1 = i1 * j1;
			aByteArrayArray1491[l] = new byte[l1];
			if (k1 == 0) {
				for (int i2 = 0; i2 < l1; i2++) {
					aByteArrayArray1491[l][i2] = stream.readSignedByte();
				}

			} else if (k1 == 1) {
				for (int j2 = 0; j2 < i1; j2++) {
					for (int l2 = 0; l2 < j1; l2++) {
						aByteArrayArray1491[l][j2 + l2 * i1] = stream.readSignedByte();
					}

				}

			}
			if (j1 > anInt1497 && l < 128) {
				anInt1497 = j1;
			}
			anIntArray1494[l] = 1;
			glyphAdvancePixels[l] = i1 + 2;
			int k2 = 0;
			for (int i3 = j1 / 7; i3 < j1; i3++) {
				k2 += aByteArrayArray1491[l][i3 * i1];
			}

			if (k2 <= j1 / 7) {
				glyphAdvancePixels[l]--;
				anIntArray1494[l] = 0;
			}
			k2 = 0;
			for (int j3 = j1 / 7; j3 < j1; j3++) {
				k2 += aByteArrayArray1491[l][i1 - 1 + j3 * i1];
			}

			if (k2 <= j1 / 7) {
				glyphAdvancePixels[l]--;
			}
		}

		if (flag) {
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
		baselineY -= anInt1497;
		for (int characterIndex = 0; characterIndex < text.length(); characterIndex++) {
			char character = text.charAt(characterIndex);
			if (character != ' ') {
				drawGlyphMask(aByteArrayArray1491[character], x + anIntArray1494[character], baselineY + anIntArray1495[character], anIntArray1492[character], anIntArray1493[character], color);
			}
			x += glyphAdvancePixels[character];
		}
	}

	public void drawCenteredVerticalWave(int color, String text, int centerX, int phase, int baselineY) {
		if (text == null) {
			return;
		}
		centerX -= measureLiteralTextWidth(text) / 2;
		baselineY -= anInt1497;
		for (int characterIndex = 0; characterIndex < text.length(); characterIndex++) {
			char character = text.charAt(characterIndex);
			if (character != ' ') {
				drawGlyphMask(aByteArrayArray1491[character], centerX + anIntArray1494[character], baselineY + anIntArray1495[character] + (int) (Math.sin(characterIndex / 2D + phase / 5D) * 5D), anIntArray1492[character], anIntArray1493[character], color);
			}
			centerX += glyphAdvancePixels[character];
		}

	}

	public void drawCenteredWave(int centerX, String text, int phase, int baselineY, int color) {
		if (text == null) {
			return;
		}
		centerX -= measureLiteralTextWidth(text) / 2;
		baselineY -= anInt1497;
		for (int characterIndex = 0; characterIndex < text.length(); characterIndex++) {
			char character = text.charAt(characterIndex);
			if (character != ' ') {
				drawGlyphMask(aByteArrayArray1491[character], centerX + anIntArray1494[character] + (int) (Math.sin(characterIndex / 5D + phase / 5D) * 5D), baselineY + anIntArray1495[character] + (int) (Math.sin(characterIndex / 3D + phase / 5D) * 5D), anIntArray1492[character], anIntArray1493[character], color);
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
		baselineY -= anInt1497;
		for (int characterIndex = 0; characterIndex < text.length(); characterIndex++) {
			char character = text.charAt(characterIndex);
			if (character != ' ') {
				drawGlyphMask(aByteArrayArray1491[character], centerX + anIntArray1494[character], baselineY + anIntArray1495[character] + (int) (Math.sin(characterIndex / 1.5D + phase) * amplitudePixels), anIntArray1492[character], anIntArray1493[character], color);
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
		baselineY -= anInt1497;
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
						drawGlyphMask(aByteArrayArray1491[character], x + anIntArray1494[character] + 1, baselineY + anIntArray1495[character] + 1, anIntArray1492[character], anIntArray1493[character], 0);
					}
					try {
					drawGlyphMask(aByteArrayArray1491[character], x + anIntArray1494[character], baselineY + anIntArray1495[character], anIntArray1492[character], anIntArray1493[character], color);
					} catch (Exception glyphFailure) {
						
					}
				}
				x += glyphAdvancePixels[character];
			}
		}
		if (strikeThroughEnabled) {
			DrawingArea.drawHorizontalLine(baselineY + (int) (anInt1497 * 0.69999999999999996D), 0x800000, x - startX, startX);
		}
	}

	public void drawSeededJitterText(int x, int color, String text, int seed, int baselineY) {
		if (text == null) {
			return;
		}
		jitterRandom.setSeed(seed);
		int opacity256 = 192 + (jitterRandom.nextInt() & 0x1f);
		baselineY -= anInt1497;
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
					drawBlendedGlyphMask(192, x + anIntArray1494[character] + 1, aByteArrayArray1491[character], anIntArray1492[character], baselineY + anIntArray1495[character] + 1, anIntArray1493[character], 0);
					drawBlendedGlyphMask(opacity256, x + anIntArray1494[character], aByteArrayArray1491[character], anIntArray1492[character], baselineY + anIntArray1495[character], anIntArray1493[character], color);
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

	private final byte[][] aByteArrayArray1491;
	private final int[] anIntArray1492;
	private final int[] anIntArray1493;
	private final int[] anIntArray1494;
	private final int[] anIntArray1495;
	private final int[] glyphAdvancePixels;
	public int anInt1497;
	private final Random jitterRandom;
	private boolean strikeThroughEnabled;
}
