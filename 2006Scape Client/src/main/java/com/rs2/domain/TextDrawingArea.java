package com.rs2.domain;

// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

import java.util.Random;

public final class TextDrawingArea extends DrawingArea {

	public TextDrawingArea(boolean flag, String s, StreamLoader streamLoader) {
                glyphPixels = new byte[256][];
                glyphWidths = new int[256];
                glyphHeights = new int[256];
                xOffsets = new int[256];
                yOffsets = new int[256];
                glyphAdvances = new int[256];
                random = new Random();
                strikethrough = false;
		Stream stream = new Stream(streamLoader.getFileData(s + ".dat"));
		Stream stream_1 = new Stream(streamLoader.getFileData("index.dat"));
		stream_1.currentOffset = stream.readUnsignedWord() + 4;
		int k = stream_1.readUnsignedByte();
		if (k > 0) {
			stream_1.currentOffset += 3 * (k - 1);
		}
		for (int l = 0; l < 256; l++) {
                        xOffsets[l] = stream_1.readUnsignedByte();
                        yOffsets[l] = stream_1.readUnsignedByte();
                        int i1 = glyphWidths[l] = stream_1.readUnsignedWord();
                        int j1 = glyphHeights[l] = stream_1.readUnsignedWord();
			int k1 = stream_1.readUnsignedByte();
			int l1 = i1 * j1;
			glyphPixels[l] = new byte[l1];
			if (k1 == 0) {
				for (int i2 = 0; i2 < l1; i2++) {
					glyphPixels[l][i2] = stream.readSignedByte();
				}

			} else if (k1 == 1) {
				for (int j2 = 0; j2 < i1; j2++) {
					for (int l2 = 0; l2 < j1; l2++) {
						glyphPixels[l][j2 + l2 * i1] = stream.readSignedByte();
					}

				}

			}
                        if (j1 > fontHeight && l < 128) {
                                fontHeight = j1;
			}
                        xOffsets[l] = 1;
                        glyphAdvances[l] = i1 + 2;
			int k2 = 0;
			for (int i3 = j1 / 7; i3 < j1; i3++) {
                                k2 += glyphPixels[l][i3 * i1];
			}

			if (k2 <= j1 / 7) {
                                glyphAdvances[l]--;
                                xOffsets[l] = 0;
			}
			k2 = 0;
			for (int j3 = j1 / 7; j3 < j1; j3++) {
                                k2 += glyphPixels[l][i1 - 1 + j3 * i1];
			}

			if (k2 <= j1 / 7) {
                                glyphAdvances[l]--;
			}
		}

		if (flag) {
                        glyphAdvances[32] = glyphAdvances[73];
		} else {
                        glyphAdvances[32] = glyphAdvances[105];
		}
	}

	public void textRight(int i, String s, int k, int l) {
                textLeft(i, s, k, l - measurePlainTextWidth(s));
	}

	public void textCenter(int i, String s, int k, int l) {
                textLeft(i, s, k, l - measurePlainTextWidth(s) / 2);
	}

	public void textCenterShadow(int _color, int _x, String s, int _y, boolean _shadow) {
		textLeftShadow(_shadow, _x - getTextWidth(s) / 2, _color, s, _y);
	}

	public void textRightShadow(boolean _shadow, int _x, int _color, String s, int _y) {
		textLeftShadow(_shadow, _x - getTextWidth(s), _color, s, _y);
	}

	public int getTextWidth(String s) {
		if (s == null) {
			return 0;
		}
		int j = 0;
		for (int k = 0; k < s.length(); k++) {
			if (s.charAt(k) == '@' && k + 4 < s.length() && s.charAt(k + 4) == '@') {
				k += 4;
			} else {
				j += glyphAdvances[s.charAt(k)];
			}
		}

		return j;
	}

        public int measurePlainTextWidth(String s) {
		if (s == null) {
			return 0;
		}
                int j = 0;
                for (int k = 0; k < s.length(); k++) {
                        j += glyphAdvances[s.charAt(k)];
                }
                return j;
	}

	public void textLeft(int i, String s, int j, int l) {
		if (s == null) {
			return;
		}
		j -= fontHeight;
		for (int i1 = 0; i1 < s.length(); i1++) {
			char c = s.charAt(i1);
			if (c != ' ') {
                                renderGlyph(glyphPixels[c], l + xOffsets[c], j + yOffsets[c], glyphWidths[c], glyphHeights[c], i);
			}
			l += glyphAdvances[c];
		}
	}

        public void drawWavyCenteredText(int i, String s, int j, int k, int l) {
		if (s == null) {
			return;
		}
                j -= measurePlainTextWidth(s) / 2;
		l -= fontHeight;
		for (int i1 = 0; i1 < s.length(); i1++) {
			char c = s.charAt(i1);
			if (c != ' ') {
                                renderGlyph(glyphPixels[c], j + xOffsets[c], l + yOffsets[c] + (int) (Math.sin(i1 / 2D + k / 5D) * 5D), glyphWidths[c], glyphHeights[c], i);
			}
			j += glyphAdvances[c];
		}

	}

        public void drawWavyText(int i, String s, int j, int k, int l) {
		if (s == null) {
			return;
		}
                i -= measurePlainTextWidth(s) / 2;
		k -= fontHeight;
		for (int i1 = 0; i1 < s.length(); i1++) {
			char c = s.charAt(i1);
			if (c != ' ') {
                                renderGlyph(glyphPixels[c], i + xOffsets[c] + (int) (Math.sin(i1 / 5D + j / 5D) * 5D), k + yOffsets[c] + (int) (Math.sin(i1 / 3D + j / 5D) * 5D), glyphWidths[c], glyphHeights[c], l);
			}
			i += glyphAdvances[c];
		}

	}

        public void drawShakeText(int i, String s, int j, int k, int l, int i1) {
		if (s == null) {
			return;
		}
		double d = 7D - i / 8D;
		if (d < 0.0D) {
			d = 0.0D;
		}
                l -= measurePlainTextWidth(s) / 2;
		k -= fontHeight;
		for (int k1 = 0; k1 < s.length(); k1++) {
			char c = s.charAt(k1);
			if (c != ' ') {
                                renderGlyph(glyphPixels[c], l + xOffsets[c], k + yOffsets[c] + (int) (Math.sin(k1 / 1.5D + j) * d), glyphWidths[c], glyphHeights[c], i1);
			}
			l += glyphAdvances[c];
		}

	}

	public void textLeftShadow(boolean _shadow, int _x, int _color, String s, int _y) {
		strikethrough = false;
		int l = _x;
		if (s == null) {
			return;
		}
		_y -= fontHeight;
		for (int i1 = 0; i1 < s.length(); i1++) {
			if (s.charAt(i1) == '@' && i1 + 4 < s.length() && s.charAt(i1 + 4) == '@') {
				int j1 = getColorByName(s.substring(i1 + 1, i1 + 4));
				if (j1 != -1) {
					_color = j1;
				}
				i1 += 4;
			} else {
				char c = s.charAt(i1);
				if (c != ' ') {
					if (_shadow) {
                                                renderGlyph(glyphPixels[c], _x + xOffsets[c] + 1, _y + yOffsets[c] + 1, glyphWidths[c], glyphHeights[c], 0);
					}
					try {
                                        renderGlyph(glyphPixels[c], _x + xOffsets[c], _y + yOffsets[c], glyphWidths[c], glyphHeights[c], _color);
					} catch (Exception e) {
						
					}
				}
				_x += glyphAdvances[c];
			}
		}
		if (strikethrough) {
			DrawingArea.drawHorizontalLine(_y + (int) (fontHeight * 0.69999999999999996D), 0x800000, _x - l, l);
		}
	}

        public void drawRandomColorText(int i, int j, String s, int k, int i1) {
		if (s == null) {
			return;
		}
		random.setSeed(k);
		int j1 = 192 + (random.nextInt() & 0x1f);
		i1 -= fontHeight;
		for (int k1 = 0; k1 < s.length(); k1++) {
			if (s.charAt(k1) == '@' && k1 + 4 < s.length() && s.charAt(k1 + 4) == '@') {
				int l1 = getColorByName(s.substring(k1 + 1, k1 + 4));
				if (l1 != -1) {
					j = l1;
				}
				k1 += 4;
			} else {
				char c = s.charAt(k1);
				if (c != ' ') {
                                    blitGlyph(192, i + xOffsets[c] + 1, glyphPixels[c], glyphWidths[c], i1 + yOffsets[c] + 1, glyphHeights[c], 0);
                                    blitGlyph(j1, i + xOffsets[c], glyphPixels[c], glyphWidths[c], i1 + yOffsets[c], glyphHeights[c], j);
				}
				i += glyphAdvances[c];
				if ((random.nextInt() & 3) == 0) {
					i++;
				}
			}
		}

	}

	private int getColorByName(String s) {
		if (s.equals("red")) {
			return 0xff0000;
		}
		if (s.equals("gre")) {
			return 65280;
		}
		if (s.equals("blu")) {
			return 255;
		}
		if (s.equals("yel")) {
			return 0xffff00;
		}
		if (s.equals("cya")) {
			return 65535;
		}
		if (s.equals("mag")) {
			return 0xff00ff;
		}
		if (s.equals("whi")) {
			return 0xffffff;
		}
		if (s.equals("bla")) {
			return 0;
		}
		if (s.equals("lre")) {
			return 0xff9040;
		}
		if (s.equals("dre")) {
			return 0x800000;
		}
		if (s.equals("dbl")) {
			return 128;
		}
		if (s.equals("or1")) {
			return 0xffb000;
		}
		if (s.equals("or2")) {
			return 0xff7000;
		}
		if (s.equals("or3")) {
			return 0xff3000;
		}
		if (s.equals("gr1")) {
			return 0xc0ff00;
		}
		if (s.equals("gr2")) {
			return 0x80ff00;
		}
		if (s.equals("gr3")) {
			return 0x40ff00;
		}
		if (s.equals("str")) {
			strikethrough = true;
		}
		if (s.equals("end")) {
			strikethrough = false;
		}
		return -1;
	}

        private void renderGlyph(byte abyte0[], int i, int j, int k, int l, int i1) {
		int j1 = i + j * DrawingArea.width;
		int k1 = DrawingArea.width - k;
		int l1 = 0;
		int i2 = 0;
		if (j < DrawingArea.topY) {
			int j2 = DrawingArea.topY - j;
			l -= j2;
			j = DrawingArea.topY;
			i2 += j2 * k;
			j1 += j2 * DrawingArea.width;
		}
		if (j + l >= DrawingArea.bottomY) {
			l -= j + l - DrawingArea.bottomY + 1;
		}
		if (i < DrawingArea.topX) {
			int k2 = DrawingArea.topX - i;
			k -= k2;
			i = DrawingArea.topX;
			i2 += k2;
			j1 += k2;
			l1 += k2;
			k1 += k2;
		}
		if (i + k >= DrawingArea.bottomX) {
			int l2 = i + k - DrawingArea.bottomX + 1;
			k -= l2;
			l1 += l2;
			k1 += l2;
		}
		if (!(k <= 0 || l <= 0)) {
                        blitGlyphRow(DrawingArea.pixels, abyte0, i1, i2, j1, k, l, k1, l1);
		}
	}

        private void blitGlyphRow(int ai[], byte abyte0[], int i, int j, int k, int l, int i1, int j1, int k1) {
		int l1 = -(l >> 2);
		l = -(l & 3);
		for (int i2 = -i1; i2 < 0; i2++) {
			for (int j2 = l1; j2 < 0; j2++) {
				if (abyte0[j++] != 0) {
					ai[k++] = i;
				} else {
					k++;
				}
				if (abyte0[j++] != 0) {
					ai[k++] = i;
				} else {
					k++;
				}
				if (abyte0[j++] != 0) {
					ai[k++] = i;
				} else {
					k++;
				}
				if (abyte0[j++] != 0) {
					ai[k++] = i;
				} else {
					k++;
				}
			}

			for (int k2 = l; k2 < 0; k2++) {
				if (abyte0[j++] != 0) {
					ai[k++] = i;
				} else {
					k++;
				}
			}

			k += j1;
			j += k1;
		}

	}

        private void blitGlyph(int i, int j, byte abyte0[], int k, int l, int i1, int j1) {
		int k1 = j + l * DrawingArea.width;
		int l1 = DrawingArea.width - k;
		int i2 = 0;
		int j2 = 0;
		if (l < DrawingArea.topY) {
			int k2 = DrawingArea.topY - l;
			i1 -= k2;
			l = DrawingArea.topY;
			j2 += k2 * k;
			k1 += k2 * DrawingArea.width;
		}
		if (l + i1 >= DrawingArea.bottomY) {
			i1 -= l + i1 - DrawingArea.bottomY + 1;
		}
		if (j < DrawingArea.topX) {
			int l2 = DrawingArea.topX - j;
			k -= l2;
			j = DrawingArea.topX;
			j2 += l2;
			k1 += l2;
			i2 += l2;
			l1 += l2;
		}
		if (j + k >= DrawingArea.bottomX) {
			int i3 = j + k - DrawingArea.bottomX + 1;
			k -= i3;
			i2 += i3;
			l1 += i3;
		}
		if (k <= 0 || i1 <= 0) {
			return;
		}
                blendGlyph(abyte0, i1, k1, DrawingArea.pixels, j2, k, i2, l1, j1, i);
	}

        private void blendGlyph(byte abyte0[], int i, int j, int ai[], int l, int i1, int j1, int k1, int l1, int i2) {
		l1 = ((l1 & 0xff00ff) * i2 & 0xff00ff00) + ((l1 & 0xff00) * i2 & 0xff0000) >> 8;
		i2 = 256 - i2;
		for (int j2 = -i; j2 < 0; j2++) {
			for (int k2 = -i1; k2 < 0; k2++) {
				if (abyte0[l++] != 0) {
					int l2 = ai[j];
					ai[j++] = (((l2 & 0xff00ff) * i2 & 0xff00ff00) + ((l2 & 0xff00) * i2 & 0xff0000) >> 8) + l1;
				} else {
					j++;
				}
			}

			j += k1;
			l += j1;
		}

	}

	private final byte[][] glyphPixels;
	private final int[] glyphWidths;
	private final int[] glyphHeights;
	private final int[] xOffsets;
	private final int[] yOffsets;
	private final int[] glyphAdvances;
	public int fontHeight;
	private final Random random;
	private boolean strikethrough;
}
