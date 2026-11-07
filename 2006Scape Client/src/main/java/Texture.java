// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

final class Texture extends DrawingArea {

	public static void nullLoader() {
		anIntArray1468 = null;
		anIntArray1468 = null;
		anIntArray1470 = null;
		anIntArray1471 = null;
		lineOffsets = null;
		textureBackgrounds = null;
		hasTransparentPixels = null;
		averageColors = null;
		freeBuffers = null;
		textureBuffers = null;
		lastUsedStamps = null;
		hslToRgbPalette = null;
		adjustedPalettes = null;
	}

	public static void setScreenSizeFromDrawingArea() {
		lineOffsets = new int[DrawingArea.height];
		for (int row = 0; row < DrawingArea.height; row++) {
			lineOffsets[row] = DrawingArea.width * row;
		}

		textureInt1 = DrawingArea.width / 2;
		textureInt2 = DrawingArea.height / 2;
	}

	public static void setScreenSize(int width, int height) {
		lineOffsets = new int[height];
		for (int row = 0; row < height; row++) {
			lineOffsets[row] = width * row;
		}

		textureInt1 = width / 2;
		textureInt2 = height / 2;
	}

	public static void releaseAllTextureBuffers() {
		freeBuffers = null;
		for (int textureId = 0; textureId < 50; textureId++) {
			textureBuffers[textureId] = null;
		}

	}

	public static void allocateBufferPool() {
		if (freeBuffers == null) {
			freeBufferCount = 20;// was parameter
			if (lowMem) {
				freeBuffers = new int[freeBufferCount][16384];
			} else {
				freeBuffers = new int[freeBufferCount][0x10000];
			}
			for (int textureId = 0; textureId < 50; textureId++) {
				textureBuffers[textureId] = null;
			}

		}
	}

	public static void loadTextures(StreamLoader streamLoader) {
		loadedTextureCount = 0;
		for (int textureId = 0; textureId < 50; textureId++) {
			try {
				textureBackgrounds[textureId] = new Background(streamLoader, String.valueOf(textureId), 0);
				if (lowMem && textureBackgrounds[textureId].canvasWidth == 128) {
					textureBackgrounds[textureId].reduceToHalfCanvas();
				} else {
					textureBackgrounds[textureId].expandToCanvas();
				}
				loadedTextureCount++;
			} catch (Exception _ex) {
			}
		}

	}

	public static int getAverageColor(int textureId) {
		if (averageColors[textureId] != 0) {
			return averageColors[textureId];
		}
		int redSum = 0;
		int greenSum = 0;
		int blueSum = 0;
		int paletteLength = adjustedPalettes[textureId].length;
		for (int entry = 0; entry < paletteLength; entry++) {
			redSum += adjustedPalettes[textureId][entry] >> 16 & 0xff;
			greenSum += adjustedPalettes[textureId][entry] >> 8 & 0xff;
			blueSum += adjustedPalettes[textureId][entry] & 0xff;
		}

		int averageColor = (redSum / paletteLength << 16) + (greenSum / paletteLength << 8) + blueSum / paletteLength;
		averageColor = applyGamma(averageColor, 1.3999999999999999D);
		if (averageColor == 0) {
			averageColor = 1;
		}
		averageColors[textureId] = averageColor;
		return averageColor;
	}

	public static void releaseTextureBuffer(int textureId) {
		if (textureBuffers[textureId] == null) {
			return;
		}
		freeBuffers[freeBufferCount++] = textureBuffers[textureId];
		textureBuffers[textureId] = null;
	}

	private static int[] getTexturePixels(int textureId) {
		lastUsedStamps[textureId] = useCounter++;
		if (textureBuffers[textureId] != null) {
			return textureBuffers[textureId];
		}
		int pixels[];
		if (freeBufferCount > 0) {
			pixels = freeBuffers[--freeBufferCount];
			freeBuffers[freeBufferCount] = null;
		} else {
			int oldestStamp = 0;
			int evictedTexture = -1;
			for (int candidate = 0; candidate < loadedTextureCount; candidate++) {
				if (textureBuffers[candidate] != null && (lastUsedStamps[candidate] < oldestStamp || evictedTexture == -1)) {
					oldestStamp = lastUsedStamps[candidate];
					evictedTexture = candidate;
				}
			}

			pixels = textureBuffers[evictedTexture];
			textureBuffers[evictedTexture] = null;
		}
		textureBuffers[textureId] = pixels;
		Background background = textureBackgrounds[textureId];
		int palette[] = adjustedPalettes[textureId];
		if (lowMem) {
			hasTransparentPixels[textureId] = false;
			for (int pixel = 0; pixel < 4096; pixel++) {
				int color = pixels[pixel] = palette[background.paletteIndices[pixel]] & 0xf8f8ff;
				if (color == 0) {
					hasTransparentPixels[textureId] = true;
				}
				pixels[4096 + pixel] = color - (color >>> 3) & 0xf8f8ff;
				pixels[8192 + pixel] = color - (color >>> 2) & 0xf8f8ff;
				pixels[12288 + pixel] = color - (color >>> 2) - (color >>> 3) & 0xf8f8ff;
			}

		} else {
			if (background.imageWidth == 64) {
				for (int row = 0; row < 128; row++) {
					for (int column = 0; column < 128; column++) {
						pixels[column + (row << 7)] = palette[background.paletteIndices[(column >> 1) + (row >> 1 << 6)]];
					}

				}

			} else {
				for (int pixel = 0; pixel < 16384; pixel++) {
					pixels[pixel] = palette[background.paletteIndices[pixel]];
				}

			}
			hasTransparentPixels[textureId] = false;
			for (int pixel = 0; pixel < 16384; pixel++) {
				pixels[pixel] &= 0xf8f8ff;
				int color = pixels[pixel];
				if (color == 0) {
					hasTransparentPixels[textureId] = true;
				}
				pixels[16384 + pixel] = color - (color >>> 3) & 0xf8f8ff;
				pixels[32768 + pixel] = color - (color >>> 2) & 0xf8f8ff;
				pixels[49152 + pixel] = color - (color >>> 2) - (color >>> 3) & 0xf8f8ff;
			}

		}
		return pixels;
	}

	public static void buildPalettes(double gamma) {
		gamma += Math.random() * 0.029999999999999999D - 0.014999999999999999D;
		int outputIndex = 0;
		for (int hueSaturationIndex = 0; hueSaturationIndex < 512; hueSaturationIndex++) {
			double hue = hueSaturationIndex / 8 / 64D + 0.0078125D;
			double saturation = (hueSaturationIndex & 7) / 8D + 0.0625D;
			for (int lightnessIndex = 0; lightnessIndex < 128; lightnessIndex++) {
				double lightness = lightnessIndex / 128D;
				double red = lightness;
				double green = lightness;
				double blue = lightness;
				if (saturation != 0.0D) {
					double upper;
					if (lightness < 0.5D) {
						upper = lightness * (1.0D + saturation);
					} else {
						upper = lightness + saturation - lightness * saturation;
					}
					double lower = 2D * lightness - upper;
					double redHue = hue + 0.33333333333333331D;
					if (redHue > 1.0D) {
						redHue--;
					}
					double greenHue = hue;
					double blueHue = hue - 0.33333333333333331D;
					if (blueHue < 0.0D) {
						blueHue++;
					}
					if (6D * redHue < 1.0D) {
						red = lower + (upper - lower) * 6D * redHue;
					} else if (2D * redHue < 1.0D) {
						red = upper;
					} else if (3D * redHue < 2D) {
						red = lower + (upper - lower) * (0.66666666666666663D - redHue) * 6D;
					} else {
						red = lower;
					}
					if (6D * greenHue < 1.0D) {
						green = lower + (upper - lower) * 6D * greenHue;
					} else if (2D * greenHue < 1.0D) {
						green = upper;
					} else if (3D * greenHue < 2D) {
						green = lower + (upper - lower) * (0.66666666666666663D - greenHue) * 6D;
					} else {
						green = lower;
					}
					if (6D * blueHue < 1.0D) {
						blue = lower + (upper - lower) * 6D * blueHue;
					} else if (2D * blueHue < 1.0D) {
						blue = upper;
					} else if (3D * blueHue < 2D) {
						blue = lower + (upper - lower) * (0.66666666666666663D - blueHue) * 6D;
					} else {
						blue = lower;
					}
				}
				int redLevel = (int) (red * 256D);
				int greenLevel = (int) (green * 256D);
				int blueLevel = (int) (blue * 256D);
				int rgb = (redLevel << 16) + (greenLevel << 8) + blueLevel;
				rgb = applyGamma(rgb, gamma);
				if (rgb == 0) {
					rgb = 1;
				}
				hslToRgbPalette[outputIndex++] = rgb;
			}

		}

		for (int textureId = 0; textureId < 50; textureId++) {
			if (textureBackgrounds[textureId] != null) {
				int sourcePalette[] = textureBackgrounds[textureId].paletteRgb;
				adjustedPalettes[textureId] = new int[sourcePalette.length];
				for (int paletteIndex = 0; paletteIndex < sourcePalette.length; paletteIndex++) {
					adjustedPalettes[textureId][paletteIndex] = applyGamma(sourcePalette[paletteIndex], gamma);
					if ((adjustedPalettes[textureId][paletteIndex] & 0xf8f8ff) == 0 && paletteIndex != 0) {
						adjustedPalettes[textureId][paletteIndex] = 1;
					}
				}

			}
		}

		for (int textureId = 0; textureId < 50; textureId++) {
			releaseTextureBuffer(textureId);
		}

	}

	private static int applyGamma(int rgb, double gamma) {
		double red = (rgb >> 16) / 256D;
		double green = (rgb >> 8 & 0xff) / 256D;
		double blue = (rgb & 0xff) / 256D;
		red = Math.pow(red, gamma);
		green = Math.pow(green, gamma);
		blue = Math.pow(blue, gamma);
		int redLevel = (int) (red * 256D);
		int greenLevel = (int) (green * 256D);
		int blueLevel = (int) (blue * 256D);
		return (redLevel << 16) + (greenLevel << 8) + blueLevel;
	}

	public static void method374(int i, int j, int k, int l, int i1, int j1, int k1, int l1, int i2) {
		int j2 = 0;
		int k2 = 0;
		if (j != i) {
			j2 = (i1 - l << 16) / (j - i);
			k2 = (l1 - k1 << 15) / (j - i);
		}
		int l2 = 0;
		int i3 = 0;
		if (k != j) {
			l2 = (j1 - i1 << 16) / (k - j);
			i3 = (i2 - l1 << 15) / (k - j);
		}
		int j3 = 0;
		int k3 = 0;
		if (k != i) {
			j3 = (l - j1 << 16) / (i - k);
			k3 = (k1 - i2 << 15) / (i - k);
		}
		if (i <= j && i <= k) {
			if (i >= DrawingArea.bottomY) {
				return;
			}
			if (j > DrawingArea.bottomY) {
				j = DrawingArea.bottomY;
			}
			if (k > DrawingArea.bottomY) {
				k = DrawingArea.bottomY;
			}
			if (j < k) {
				j1 = l <<= 16;
				i2 = k1 <<= 15;
				if (i < 0) {
					j1 -= j3 * i;
					l -= j2 * i;
					i2 -= k3 * i;
					k1 -= k2 * i;
					i = 0;
				}
				i1 <<= 16;
				l1 <<= 15;
				if (j < 0) {
					i1 -= l2 * j;
					l1 -= i3 * j;
					j = 0;
				}
				if (i != j && j3 < j2 || i == j && j3 > l2) {
					k -= j;
					j -= i;
					for (i = lineOffsets[i]; --j >= 0; i += DrawingArea.width) {
						method375(DrawingArea.pixels, i, j1 >> 16, l >> 16, i2 >> 7, k1 >> 7);
						j1 += j3;
						l += j2;
						i2 += k3;
						k1 += k2;
					}

					while (--k >= 0) {
						method375(DrawingArea.pixels, i, j1 >> 16, i1 >> 16, i2 >> 7, l1 >> 7);
						j1 += j3;
						i1 += l2;
						i2 += k3;
						l1 += i3;
						i += DrawingArea.width;
					}
					return;
				}
				k -= j;
				j -= i;
				for (i = lineOffsets[i]; --j >= 0; i += DrawingArea.width) {
					method375(DrawingArea.pixels, i, l >> 16, j1 >> 16, k1 >> 7, i2 >> 7);
					j1 += j3;
					l += j2;
					i2 += k3;
					k1 += k2;
				}

				while (--k >= 0) {
					method375(DrawingArea.pixels, i, i1 >> 16, j1 >> 16, l1 >> 7, i2 >> 7);
					j1 += j3;
					i1 += l2;
					i2 += k3;
					l1 += i3;
					i += DrawingArea.width;
				}
				return;
			}
			i1 = l <<= 16;
			l1 = k1 <<= 15;
			if (i < 0) {
				i1 -= j3 * i;
				l -= j2 * i;
				l1 -= k3 * i;
				k1 -= k2 * i;
				i = 0;
			}
			j1 <<= 16;
			i2 <<= 15;
			if (k < 0) {
				j1 -= l2 * k;
				i2 -= i3 * k;
				k = 0;
			}
			if (i != k && j3 < j2 || i == k && l2 > j2) {
				j -= k;
				k -= i;
				for (i = lineOffsets[i]; --k >= 0; i += DrawingArea.width) {
					method375(DrawingArea.pixels, i, i1 >> 16, l >> 16, l1 >> 7, k1 >> 7);
					i1 += j3;
					l += j2;
					l1 += k3;
					k1 += k2;
				}

				while (--j >= 0) {
					method375(DrawingArea.pixels, i, j1 >> 16, l >> 16, i2 >> 7, k1 >> 7);
					j1 += l2;
					l += j2;
					i2 += i3;
					k1 += k2;
					i += DrawingArea.width;
				}
				return;
			}
			j -= k;
			k -= i;
			for (i = lineOffsets[i]; --k >= 0; i += DrawingArea.width) {
				method375(DrawingArea.pixels, i, l >> 16, i1 >> 16, k1 >> 7, l1 >> 7);
				i1 += j3;
				l += j2;
				l1 += k3;
				k1 += k2;
			}

			while (--j >= 0) {
				method375(DrawingArea.pixels, i, l >> 16, j1 >> 16, k1 >> 7, i2 >> 7);
				j1 += l2;
				l += j2;
				i2 += i3;
				k1 += k2;
				i += DrawingArea.width;
			}
			return;
		}
		if (j <= k) {
			if (j >= DrawingArea.bottomY) {
				return;
			}
			if (k > DrawingArea.bottomY) {
				k = DrawingArea.bottomY;
			}
			if (i > DrawingArea.bottomY) {
				i = DrawingArea.bottomY;
			}
			if (k < i) {
				l = i1 <<= 16;
				k1 = l1 <<= 15;
				if (j < 0) {
					l -= j2 * j;
					i1 -= l2 * j;
					k1 -= k2 * j;
					l1 -= i3 * j;
					j = 0;
				}
				j1 <<= 16;
				i2 <<= 15;
				if (k < 0) {
					j1 -= j3 * k;
					i2 -= k3 * k;
					k = 0;
				}
				if (j != k && j2 < l2 || j == k && j2 > j3) {
					i -= k;
					k -= j;
					for (j = lineOffsets[j]; --k >= 0; j += DrawingArea.width) {
						method375(DrawingArea.pixels, j, l >> 16, i1 >> 16, k1 >> 7, l1 >> 7);
						l += j2;
						i1 += l2;
						k1 += k2;
						l1 += i3;
					}

					while (--i >= 0) {
						method375(DrawingArea.pixels, j, l >> 16, j1 >> 16, k1 >> 7, i2 >> 7);
						l += j2;
						j1 += j3;
						k1 += k2;
						i2 += k3;
						j += DrawingArea.width;
					}
					return;
				}
				i -= k;
				k -= j;
				for (j = lineOffsets[j]; --k >= 0; j += DrawingArea.width) {
					method375(DrawingArea.pixels, j, i1 >> 16, l >> 16, l1 >> 7, k1 >> 7);
					l += j2;
					i1 += l2;
					k1 += k2;
					l1 += i3;
				}

				while (--i >= 0) {
					method375(DrawingArea.pixels, j, j1 >> 16, l >> 16, i2 >> 7, k1 >> 7);
					l += j2;
					j1 += j3;
					k1 += k2;
					i2 += k3;
					j += DrawingArea.width;
				}
				return;
			}
			j1 = i1 <<= 16;
			i2 = l1 <<= 15;
			if (j < 0) {
				j1 -= j2 * j;
				i1 -= l2 * j;
				i2 -= k2 * j;
				l1 -= i3 * j;
				j = 0;
			}
			l <<= 16;
			k1 <<= 15;
			if (i < 0) {
				l -= j3 * i;
				k1 -= k3 * i;
				i = 0;
			}
			if (j2 < l2) {
				k -= i;
				i -= j;
				for (j = lineOffsets[j]; --i >= 0; j += DrawingArea.width) {
					method375(DrawingArea.pixels, j, j1 >> 16, i1 >> 16, i2 >> 7, l1 >> 7);
					j1 += j2;
					i1 += l2;
					i2 += k2;
					l1 += i3;
				}

				while (--k >= 0) {
					method375(DrawingArea.pixels, j, l >> 16, i1 >> 16, k1 >> 7, l1 >> 7);
					l += j3;
					i1 += l2;
					k1 += k3;
					l1 += i3;
					j += DrawingArea.width;
				}
				return;
			}
			k -= i;
			i -= j;
			for (j = lineOffsets[j]; --i >= 0; j += DrawingArea.width) {
				method375(DrawingArea.pixels, j, i1 >> 16, j1 >> 16, l1 >> 7, i2 >> 7);
				j1 += j2;
				i1 += l2;
				i2 += k2;
				l1 += i3;
			}

			while (--k >= 0) {
				method375(DrawingArea.pixels, j, i1 >> 16, l >> 16, l1 >> 7, k1 >> 7);
				l += j3;
				i1 += l2;
				k1 += k3;
				l1 += i3;
				j += DrawingArea.width;
			}
			return;
		}
		if (k >= DrawingArea.bottomY) {
			return;
		}
		if (i > DrawingArea.bottomY) {
			i = DrawingArea.bottomY;
		}
		if (j > DrawingArea.bottomY) {
			j = DrawingArea.bottomY;
		}
		if (i < j) {
			i1 = j1 <<= 16;
			l1 = i2 <<= 15;
			if (k < 0) {
				i1 -= l2 * k;
				j1 -= j3 * k;
				l1 -= i3 * k;
				i2 -= k3 * k;
				k = 0;
			}
			l <<= 16;
			k1 <<= 15;
			if (i < 0) {
				l -= j2 * i;
				k1 -= k2 * i;
				i = 0;
			}
			if (l2 < j3) {
				j -= i;
				i -= k;
				for (k = lineOffsets[k]; --i >= 0; k += DrawingArea.width) {
					method375(DrawingArea.pixels, k, i1 >> 16, j1 >> 16, l1 >> 7, i2 >> 7);
					i1 += l2;
					j1 += j3;
					l1 += i3;
					i2 += k3;
				}

				while (--j >= 0) {
					method375(DrawingArea.pixels, k, i1 >> 16, l >> 16, l1 >> 7, k1 >> 7);
					i1 += l2;
					l += j2;
					l1 += i3;
					k1 += k2;
					k += DrawingArea.width;
				}
				return;
			}
			j -= i;
			i -= k;
			for (k = lineOffsets[k]; --i >= 0; k += DrawingArea.width) {
				method375(DrawingArea.pixels, k, j1 >> 16, i1 >> 16, i2 >> 7, l1 >> 7);
				i1 += l2;
				j1 += j3;
				l1 += i3;
				i2 += k3;
			}

			while (--j >= 0) {
				method375(DrawingArea.pixels, k, l >> 16, i1 >> 16, k1 >> 7, l1 >> 7);
				i1 += l2;
				l += j2;
				l1 += i3;
				k1 += k2;
				k += DrawingArea.width;
			}
			return;
		}
		l = j1 <<= 16;
		k1 = i2 <<= 15;
		if (k < 0) {
			l -= l2 * k;
			j1 -= j3 * k;
			k1 -= i3 * k;
			i2 -= k3 * k;
			k = 0;
		}
		i1 <<= 16;
		l1 <<= 15;
		if (j < 0) {
			i1 -= j2 * j;
			l1 -= k2 * j;
			j = 0;
		}
		if (l2 < j3) {
			i -= j;
			j -= k;
			for (k = lineOffsets[k]; --j >= 0; k += DrawingArea.width) {
				method375(DrawingArea.pixels, k, l >> 16, j1 >> 16, k1 >> 7, i2 >> 7);
				l += l2;
				j1 += j3;
				k1 += i3;
				i2 += k3;
			}

			while (--i >= 0) {
				method375(DrawingArea.pixels, k, i1 >> 16, j1 >> 16, l1 >> 7, i2 >> 7);
				i1 += j2;
				j1 += j3;
				l1 += k2;
				i2 += k3;
				k += DrawingArea.width;
			}
			return;
		}
		i -= j;
		j -= k;
		for (k = lineOffsets[k]; --j >= 0; k += DrawingArea.width) {
			method375(DrawingArea.pixels, k, j1 >> 16, l >> 16, i2 >> 7, k1 >> 7);
			l += l2;
			j1 += j3;
			k1 += i3;
			i2 += k3;
		}

		while (--i >= 0) {
			method375(DrawingArea.pixels, k, j1 >> 16, i1 >> 16, i2 >> 7, l1 >> 7);
			i1 += j2;
			j1 += j3;
			l1 += k2;
			i2 += k3;
			k += DrawingArea.width;
		}
	}

	private static void method375(int ai[], int i, int l, int i1, int j1, int k1) {
		int j;// was parameter
		int k;// was parameter
		if (aBoolean1464) {
			int l1;
			if (aBoolean1462) {
				if (i1 - l > 3) {
					l1 = (k1 - j1) / (i1 - l);
				} else {
					l1 = 0;
				}
				if (i1 > DrawingArea.scanlineRightLimit) {
					i1 = DrawingArea.scanlineRightLimit;
				}
				if (l < 0) {
					j1 -= l * l1;
					l = 0;
				}
				if (l >= i1) {
					return;
				}
				i += l;
				k = i1 - l >> 2;
				l1 <<= 2;
			} else {
				if (l >= i1) {
					return;
				}
				i += l;
				k = i1 - l >> 2;
				if (k > 0) {
					l1 = (k1 - j1) * anIntArray1468[k] >> 15;
				} else {
					l1 = 0;
				}
			}
			if (anInt1465 == 0) {
				while (--k >= 0) {
					j = hslToRgbPalette[j1 >> 8];
					j1 += l1;
					ai[i++] = j;
					ai[i++] = j;
					ai[i++] = j;
					ai[i++] = j;
				}
				k = i1 - l & 3;
				if (k > 0) {
					j = hslToRgbPalette[j1 >> 8];
					do {
						ai[i++] = j;
					} while (--k > 0);
					return;
				}
			} else {
				int j2 = anInt1465;
				int l2 = 256 - anInt1465;
				if (ClientSettings.FIX_TRANSPARENCY_OVERFLOW) {
					while (--k >= 0) {
						j = hslToRgbPalette[j1 >> 8];
						j1 += l1;
						j = ((j & 0xff00ff) * l2 >> 8 & 0xff00ff) + ((j & 0xff00) * l2 >> 8 & 0xff00);
						ai[i] = j + ((ai[i] & 0xff00ff) * j2 >> 8 & 0xff00ff) + ((ai[i] & 0xff00) * j2 >> 8 & 0xff00);
						i++;
						ai[i] = j + ((ai[i] & 0xff00ff) * j2 >> 8 & 0xff00ff) + ((ai[i] & 0xff00) * j2 >> 8 & 0xff00);
						i++;
						ai[i] = j + ((ai[i] & 0xff00ff) * j2 >> 8 & 0xff00ff) + ((ai[i] & 0xff00) * j2 >> 8 & 0xff00);
						i++;
						ai[i] = j + ((ai[i] & 0xff00ff) * j2 >> 8 & 0xff00ff) + ((ai[i] & 0xff00) * j2 >> 8 & 0xff00);
						i++;
					}
					k = i1 - l & 3;
					if (k > 0) {
						j = hslToRgbPalette[j1 >> 8];
						j = ((j & 0xff00ff) * l2 >> 8 & 0xff00ff) + ((j & 0xff00) * l2 >> 8 & 0xff00);
						do {
							ai[i] = j + ((ai[i] & 0xff00ff) * j2 >> 8 & 0xff00ff) + ((ai[i] & 0xff00) * j2 >> 8 & 0xff00);
							i++;
						} while (--k > 0);
					}
				} else {
					while (--k >= 0) {
						j = hslToRgbPalette[j1 >> 8];
						j1 += l1;
						j = ((j & 0xff00ff) * l2 >> 8 & 0xff00ff) + ((j & 0xff00) * l2 >> 8 & 0xff00);
						ai[i++] = j + ((ai[i] & 0xff00ff) * j2 >> 8 & 0xff00ff) + ((ai[i] & 0xff00) * j2 >> 8 & 0xff00);
						ai[i++] = j + ((ai[i] & 0xff00ff) * j2 >> 8 & 0xff00ff) + ((ai[i] & 0xff00) * j2 >> 8 & 0xff00);
						ai[i++] = j + ((ai[i] & 0xff00ff) * j2 >> 8 & 0xff00ff) + ((ai[i] & 0xff00) * j2 >> 8 & 0xff00);
						ai[i++] = j + ((ai[i] & 0xff00ff) * j2 >> 8 & 0xff00ff) + ((ai[i] & 0xff00) * j2 >> 8 & 0xff00);
					}
					k = i1 - l & 3;
					if (k > 0) {
						j = hslToRgbPalette[j1 >> 8];
						j = ((j & 0xff00ff) * l2 >> 8 & 0xff00ff) + ((j & 0xff00) * l2 >> 8 & 0xff00);
						do {
							ai[i++] = j + ((ai[i] & 0xff00ff) * j2 >> 8 & 0xff00ff) + ((ai[i] & 0xff00) * j2 >> 8 & 0xff00);
						} while (--k > 0);
					}
				}
			}
			return;
		}
		if (l >= i1) {
			return;
		}
		int i2 = (k1 - j1) / (i1 - l);
		if (aBoolean1462) {
			if (i1 > DrawingArea.scanlineRightLimit) {
				i1 = DrawingArea.scanlineRightLimit;
			}
			if (l < 0) {
				j1 -= l * i2;
				l = 0;
			}
			if (l >= i1) {
				return;
			}
		}
		i += l;
		k = i1 - l;
		if (anInt1465 == 0) {
			do {
				ai[i++] = hslToRgbPalette[j1 >> 8];
				j1 += i2;
			} while (--k > 0);
			return;
		}
		int k2 = anInt1465;
		int i3 = 256 - anInt1465;
		if (ClientSettings.FIX_TRANSPARENCY_OVERFLOW) {
			do {
				j = hslToRgbPalette[j1 >> 8];
				j1 += i2;
				j = ((j & 0xff00ff) * i3 >> 8 & 0xff00ff) + ((j & 0xff00) * i3 >> 8 & 0xff00);
				ai[i] = j + ((ai[i] & 0xff00ff) * k2 >> 8 & 0xff00ff) + ((ai[i] & 0xff00) * k2 >> 8 & 0xff00);
				i++;
			} while (--k > 0);
		} else {
			do {
				j = hslToRgbPalette[j1 >> 8];
				j1 += i2;
				j = ((j & 0xff00ff) * i3 >> 8 & 0xff00ff) + ((j & 0xff00) * i3 >> 8 & 0xff00);
				ai[i++] = j + ((ai[i] & 0xff00ff) * k2 >> 8 & 0xff00ff) + ((ai[i] & 0xff00) * k2 >> 8 & 0xff00);
			} while (--k > 0);
		}
	}

	public static void method376(int i, int j, int k, int l, int i1, int j1, int k1) {
		int l1 = 0;
		if (j != i) {
			l1 = (i1 - l << 16) / (j - i);
		}
		int i2 = 0;
		if (k != j) {
			i2 = (j1 - i1 << 16) / (k - j);
		}
		int j2 = 0;
		if (k != i) {
			j2 = (l - j1 << 16) / (i - k);
		}
		if (i <= j && i <= k) {
			if (i >= DrawingArea.bottomY) {
				return;
			}
			if (j > DrawingArea.bottomY) {
				j = DrawingArea.bottomY;
			}
			if (k > DrawingArea.bottomY) {
				k = DrawingArea.bottomY;
			}
			if (j < k) {
				j1 = l <<= 16;
				if (i < 0) {
					j1 -= j2 * i;
					l -= l1 * i;
					i = 0;
				}
				i1 <<= 16;
				if (j < 0) {
					i1 -= i2 * j;
					j = 0;
				}
				if (i != j && j2 < l1 || i == j && j2 > i2) {
					k -= j;
					j -= i;
					for (i = lineOffsets[i]; --j >= 0; i += DrawingArea.width) {
						method377(DrawingArea.pixels, i, k1, j1 >> 16, l >> 16);
						j1 += j2;
						l += l1;
					}

					while (--k >= 0) {
						method377(DrawingArea.pixels, i, k1, j1 >> 16, i1 >> 16);
						j1 += j2;
						i1 += i2;
						i += DrawingArea.width;
					}
					return;
				}
				k -= j;
				j -= i;
				for (i = lineOffsets[i]; --j >= 0; i += DrawingArea.width) {
					method377(DrawingArea.pixels, i, k1, l >> 16, j1 >> 16);
					j1 += j2;
					l += l1;
				}

				while (--k >= 0) {
					method377(DrawingArea.pixels, i, k1, i1 >> 16, j1 >> 16);
					j1 += j2;
					i1 += i2;
					i += DrawingArea.width;
				}
				return;
			}
			i1 = l <<= 16;
			if (i < 0) {
				i1 -= j2 * i;
				l -= l1 * i;
				i = 0;
			}
			j1 <<= 16;
			if (k < 0) {
				j1 -= i2 * k;
				k = 0;
			}
			if (i != k && j2 < l1 || i == k && i2 > l1) {
				j -= k;
				k -= i;
				for (i = lineOffsets[i]; --k >= 0; i += DrawingArea.width) {
					method377(DrawingArea.pixels, i, k1, i1 >> 16, l >> 16);
					i1 += j2;
					l += l1;
				}

				while (--j >= 0) {
					method377(DrawingArea.pixels, i, k1, j1 >> 16, l >> 16);
					j1 += i2;
					l += l1;
					i += DrawingArea.width;
				}
				return;
			}
			j -= k;
			k -= i;
			for (i = lineOffsets[i]; --k >= 0; i += DrawingArea.width) {
				method377(DrawingArea.pixels, i, k1, l >> 16, i1 >> 16);
				i1 += j2;
				l += l1;
			}

			while (--j >= 0) {
				method377(DrawingArea.pixels, i, k1, l >> 16, j1 >> 16);
				j1 += i2;
				l += l1;
				i += DrawingArea.width;
			}
			return;
		}
		if (j <= k) {
			if (j >= DrawingArea.bottomY) {
				return;
			}
			if (k > DrawingArea.bottomY) {
				k = DrawingArea.bottomY;
			}
			if (i > DrawingArea.bottomY) {
				i = DrawingArea.bottomY;
			}
			if (k < i) {
				l = i1 <<= 16;
				if (j < 0) {
					l -= l1 * j;
					i1 -= i2 * j;
					j = 0;
				}
				j1 <<= 16;
				if (k < 0) {
					j1 -= j2 * k;
					k = 0;
				}
				if (j != k && l1 < i2 || j == k && l1 > j2) {
					i -= k;
					k -= j;
					for (j = lineOffsets[j]; --k >= 0; j += DrawingArea.width) {
						method377(DrawingArea.pixels, j, k1, l >> 16, i1 >> 16);
						l += l1;
						i1 += i2;
					}

					while (--i >= 0) {
						method377(DrawingArea.pixels, j, k1, l >> 16, j1 >> 16);
						l += l1;
						j1 += j2;
						j += DrawingArea.width;
					}
					return;
				}
				i -= k;
				k -= j;
				for (j = lineOffsets[j]; --k >= 0; j += DrawingArea.width) {
					method377(DrawingArea.pixels, j, k1, i1 >> 16, l >> 16);
					l += l1;
					i1 += i2;
				}

				while (--i >= 0) {
					method377(DrawingArea.pixels, j, k1, j1 >> 16, l >> 16);
					l += l1;
					j1 += j2;
					j += DrawingArea.width;
				}
				return;
			}
			j1 = i1 <<= 16;
			if (j < 0) {
				j1 -= l1 * j;
				i1 -= i2 * j;
				j = 0;
			}
			l <<= 16;
			if (i < 0) {
				l -= j2 * i;
				i = 0;
			}
			if (l1 < i2) {
				k -= i;
				i -= j;
				for (j = lineOffsets[j]; --i >= 0; j += DrawingArea.width) {
					method377(DrawingArea.pixels, j, k1, j1 >> 16, i1 >> 16);
					j1 += l1;
					i1 += i2;
				}

				while (--k >= 0) {
					method377(DrawingArea.pixels, j, k1, l >> 16, i1 >> 16);
					l += j2;
					i1 += i2;
					j += DrawingArea.width;
				}
				return;
			}
			k -= i;
			i -= j;
			for (j = lineOffsets[j]; --i >= 0; j += DrawingArea.width) {
				method377(DrawingArea.pixels, j, k1, i1 >> 16, j1 >> 16);
				j1 += l1;
				i1 += i2;
			}

			while (--k >= 0) {
				method377(DrawingArea.pixels, j, k1, i1 >> 16, l >> 16);
				l += j2;
				i1 += i2;
				j += DrawingArea.width;
			}
			return;
		}
		if (k >= DrawingArea.bottomY) {
			return;
		}
		if (i > DrawingArea.bottomY) {
			i = DrawingArea.bottomY;
		}
		if (j > DrawingArea.bottomY) {
			j = DrawingArea.bottomY;
		}
		if (i < j) {
			i1 = j1 <<= 16;
			if (k < 0) {
				i1 -= i2 * k;
				j1 -= j2 * k;
				k = 0;
			}
			l <<= 16;
			if (i < 0) {
				l -= l1 * i;
				i = 0;
			}
			if (i2 < j2) {
				j -= i;
				i -= k;
				for (k = lineOffsets[k]; --i >= 0; k += DrawingArea.width) {
					method377(DrawingArea.pixels, k, k1, i1 >> 16, j1 >> 16);
					i1 += i2;
					j1 += j2;
				}

				while (--j >= 0) {
					method377(DrawingArea.pixels, k, k1, i1 >> 16, l >> 16);
					i1 += i2;
					l += l1;
					k += DrawingArea.width;
				}
				return;
			}
			j -= i;
			i -= k;
			for (k = lineOffsets[k]; --i >= 0; k += DrawingArea.width) {
				method377(DrawingArea.pixels, k, k1, j1 >> 16, i1 >> 16);
				i1 += i2;
				j1 += j2;
			}

			while (--j >= 0) {
				method377(DrawingArea.pixels, k, k1, l >> 16, i1 >> 16);
				i1 += i2;
				l += l1;
				k += DrawingArea.width;
			}
			return;
		}
		l = j1 <<= 16;
		if (k < 0) {
			l -= i2 * k;
			j1 -= j2 * k;
			k = 0;
		}
		i1 <<= 16;
		if (j < 0) {
			i1 -= l1 * j;
			j = 0;
		}
		if (i2 < j2) {
			i -= j;
			j -= k;
			for (k = lineOffsets[k]; --j >= 0; k += DrawingArea.width) {
				method377(DrawingArea.pixels, k, k1, l >> 16, j1 >> 16);
				l += i2;
				j1 += j2;
			}

			while (--i >= 0) {
				method377(DrawingArea.pixels, k, k1, i1 >> 16, j1 >> 16);
				i1 += l1;
				j1 += j2;
				k += DrawingArea.width;
			}
			return;
		}
		i -= j;
		j -= k;
		for (k = lineOffsets[k]; --j >= 0; k += DrawingArea.width) {
			method377(DrawingArea.pixels, k, k1, j1 >> 16, l >> 16);
			l += i2;
			j1 += j2;
		}

		while (--i >= 0) {
			method377(DrawingArea.pixels, k, k1, j1 >> 16, i1 >> 16);
			i1 += l1;
			j1 += j2;
			k += DrawingArea.width;
		}
	}

	private static void method377(int ai[], int i, int j, int l, int i1) {
		int k;// was parameter
		if (aBoolean1462) {
			if (i1 > DrawingArea.scanlineRightLimit) {
				i1 = DrawingArea.scanlineRightLimit;
			}
			if (l < 0) {
				l = 0;
			}
		}
		if (l >= i1) {
			return;
		}
		i += l;
		k = i1 - l >> 2;
		if (anInt1465 == 0) {
			while (--k >= 0) {
				ai[i++] = j;
				ai[i++] = j;
				ai[i++] = j;
				ai[i++] = j;
			}
			for (k = i1 - l & 3; --k >= 0;) {
				ai[i++] = j;
			}

			return;
		}
		int j1 = anInt1465;
		int k1 = 256 - anInt1465;
		j = ((j & 0xff00ff) * k1 >> 8 & 0xff00ff) + ((j & 0xff00) * k1 >> 8 & 0xff00);
		if (ClientSettings.FIX_TRANSPARENCY_OVERFLOW) {
			while (--k >= 0) {
				ai[i] = j + ((ai[i] & 0xff00ff) * j1 >> 8 & 0xff00ff) + ((ai[i] & 0xff00) * j1 >> 8 & 0xff00);
				i++;
				ai[i] = j + ((ai[i] & 0xff00ff) * j1 >> 8 & 0xff00ff) + ((ai[i] & 0xff00) * j1 >> 8 & 0xff00);
				i++;
				ai[i] = j + ((ai[i] & 0xff00ff) * j1 >> 8 & 0xff00ff) + ((ai[i] & 0xff00) * j1 >> 8 & 0xff00);
				i++;
				ai[i] = j + ((ai[i] & 0xff00ff) * j1 >> 8 & 0xff00ff) + ((ai[i] & 0xff00) * j1 >> 8 & 0xff00);
				i++;
			}
			for (k = i1 - l & 3; --k >= 0;) {
				ai[i] = j + ((ai[i] & 0xff00ff) * j1 >> 8 & 0xff00ff) + ((ai[i] & 0xff00) * j1 >> 8 & 0xff00);
				i++;
			}
		} else {
			while (--k >= 0) {
				ai[i++] = j + ((ai[i] & 0xff00ff) * j1 >> 8 & 0xff00ff) + ((ai[i] & 0xff00) * j1 >> 8 & 0xff00);
				ai[i++] = j + ((ai[i] & 0xff00ff) * j1 >> 8 & 0xff00ff) + ((ai[i] & 0xff00) * j1 >> 8 & 0xff00);
				ai[i++] = j + ((ai[i] & 0xff00ff) * j1 >> 8 & 0xff00ff) + ((ai[i] & 0xff00) * j1 >> 8 & 0xff00);
				ai[i++] = j + ((ai[i] & 0xff00ff) * j1 >> 8 & 0xff00ff) + ((ai[i] & 0xff00) * j1 >> 8 & 0xff00);
			}
			for (k = i1 - l & 3; --k >= 0;) {
				ai[i++] = j + ((ai[i] & 0xff00ff) * j1 >> 8 & 0xff00ff) + ((ai[i] & 0xff00) * j1 >> 8 & 0xff00);
			}
		}
	}

	public static void method378(int i, int j, int k, int l, int i1, int j1, int k1, int l1, int i2, int j2, int k2, int l2, int i3, int j3, int k3, int l3, int i4, int j4, int k4) {
		int ai[] = getTexturePixels(k4);
		aBoolean1463 = !hasTransparentPixels[k4];
		k2 = j2 - k2;
		j3 = i3 - j3;
		i4 = l3 - i4;
		l2 -= j2;
		k3 -= i3;
		j4 -= l3;
		int l4 = l2 * i3 - k3 * j2 << 14;
		int i5 = k3 * l3 - j4 * i3 << 8;
		int j5 = j4 * j2 - l2 * l3 << 5;
		int k5 = k2 * i3 - j3 * j2 << 14;
		int l5 = j3 * l3 - i4 * i3 << 8;
		int i6 = i4 * j2 - k2 * l3 << 5;
		int j6 = j3 * l2 - k2 * k3 << 14;
		int k6 = i4 * k3 - j3 * j4 << 8;
		int l6 = k2 * j4 - i4 * l2 << 5;
		int i7 = 0;
		int j7 = 0;
		if (j != i) {
			i7 = (i1 - l << 16) / (j - i);
			j7 = (l1 - k1 << 16) / (j - i);
		}
		int k7 = 0;
		int l7 = 0;
		if (k != j) {
			k7 = (j1 - i1 << 16) / (k - j);
			l7 = (i2 - l1 << 16) / (k - j);
		}
		int i8 = 0;
		int j8 = 0;
		if (k != i) {
			i8 = (l - j1 << 16) / (i - k);
			j8 = (k1 - i2 << 16) / (i - k);
		}
		if (i <= j && i <= k) {
			if (i >= DrawingArea.bottomY) {
				return;
			}
			if (j > DrawingArea.bottomY) {
				j = DrawingArea.bottomY;
			}
			if (k > DrawingArea.bottomY) {
				k = DrawingArea.bottomY;
			}
			if (j < k) {
				j1 = l <<= 16;
				i2 = k1 <<= 16;
				if (i < 0) {
					j1 -= i8 * i;
					l -= i7 * i;
					i2 -= j8 * i;
					k1 -= j7 * i;
					i = 0;
				}
				i1 <<= 16;
				l1 <<= 16;
				if (j < 0) {
					i1 -= k7 * j;
					l1 -= l7 * j;
					j = 0;
				}
				int k8 = i - textureInt2;
				l4 += j5 * k8;
				k5 += i6 * k8;
				j6 += l6 * k8;
				if (i != j && i8 < i7 || i == j && i8 > k7) {
					k -= j;
					j -= i;
					i = lineOffsets[i];
					while (--j >= 0) {
						method379(DrawingArea.pixels, ai, i, j1 >> 16, l >> 16, i2 >> 8, k1 >> 8, l4, k5, j6, i5, l5, k6);
						j1 += i8;
						l += i7;
						i2 += j8;
						k1 += j7;
						i += DrawingArea.width;
						l4 += j5;
						k5 += i6;
						j6 += l6;
					}
					while (--k >= 0) {
						method379(DrawingArea.pixels, ai, i, j1 >> 16, i1 >> 16, i2 >> 8, l1 >> 8, l4, k5, j6, i5, l5, k6);
						j1 += i8;
						i1 += k7;
						i2 += j8;
						l1 += l7;
						i += DrawingArea.width;
						l4 += j5;
						k5 += i6;
						j6 += l6;
					}
					return;
				}
				k -= j;
				j -= i;
				i = lineOffsets[i];
				while (--j >= 0) {
					method379(DrawingArea.pixels, ai, i, l >> 16, j1 >> 16, k1 >> 8, i2 >> 8, l4, k5, j6, i5, l5, k6);
					j1 += i8;
					l += i7;
					i2 += j8;
					k1 += j7;
					i += DrawingArea.width;
					l4 += j5;
					k5 += i6;
					j6 += l6;
				}
				while (--k >= 0) {
					method379(DrawingArea.pixels, ai, i, i1 >> 16, j1 >> 16, l1 >> 8, i2 >> 8, l4, k5, j6, i5, l5, k6);
					j1 += i8;
					i1 += k7;
					i2 += j8;
					l1 += l7;
					i += DrawingArea.width;
					l4 += j5;
					k5 += i6;
					j6 += l6;
				}
				return;
			}
			i1 = l <<= 16;
			l1 = k1 <<= 16;
			if (i < 0) {
				i1 -= i8 * i;
				l -= i7 * i;
				l1 -= j8 * i;
				k1 -= j7 * i;
				i = 0;
			}
			j1 <<= 16;
			i2 <<= 16;
			if (k < 0) {
				j1 -= k7 * k;
				i2 -= l7 * k;
				k = 0;
			}
			int l8 = i - textureInt2;
			l4 += j5 * l8;
			k5 += i6 * l8;
			j6 += l6 * l8;
			if (i != k && i8 < i7 || i == k && k7 > i7) {
				j -= k;
				k -= i;
				i = lineOffsets[i];
				while (--k >= 0) {
					method379(DrawingArea.pixels, ai, i, i1 >> 16, l >> 16, l1 >> 8, k1 >> 8, l4, k5, j6, i5, l5, k6);
					i1 += i8;
					l += i7;
					l1 += j8;
					k1 += j7;
					i += DrawingArea.width;
					l4 += j5;
					k5 += i6;
					j6 += l6;
				}
				while (--j >= 0) {
					method379(DrawingArea.pixels, ai, i, j1 >> 16, l >> 16, i2 >> 8, k1 >> 8, l4, k5, j6, i5, l5, k6);
					j1 += k7;
					l += i7;
					i2 += l7;
					k1 += j7;
					i += DrawingArea.width;
					l4 += j5;
					k5 += i6;
					j6 += l6;
				}
				return;
			}
			j -= k;
			k -= i;
			i = lineOffsets[i];
			while (--k >= 0) {
				method379(DrawingArea.pixels, ai, i, l >> 16, i1 >> 16, k1 >> 8, l1 >> 8, l4, k5, j6, i5, l5, k6);
				i1 += i8;
				l += i7;
				l1 += j8;
				k1 += j7;
				i += DrawingArea.width;
				l4 += j5;
				k5 += i6;
				j6 += l6;
			}
			while (--j >= 0) {
				method379(DrawingArea.pixels, ai, i, l >> 16, j1 >> 16, k1 >> 8, i2 >> 8, l4, k5, j6, i5, l5, k6);
				j1 += k7;
				l += i7;
				i2 += l7;
				k1 += j7;
				i += DrawingArea.width;
				l4 += j5;
				k5 += i6;
				j6 += l6;
			}
			return;
		}
		if (j <= k) {
			if (j >= DrawingArea.bottomY) {
				return;
			}
			if (k > DrawingArea.bottomY) {
				k = DrawingArea.bottomY;
			}
			if (i > DrawingArea.bottomY) {
				i = DrawingArea.bottomY;
			}
			if (k < i) {
				l = i1 <<= 16;
				k1 = l1 <<= 16;
				if (j < 0) {
					l -= i7 * j;
					i1 -= k7 * j;
					k1 -= j7 * j;
					l1 -= l7 * j;
					j = 0;
				}
				j1 <<= 16;
				i2 <<= 16;
				if (k < 0) {
					j1 -= i8 * k;
					i2 -= j8 * k;
					k = 0;
				}
				int i9 = j - textureInt2;
				l4 += j5 * i9;
				k5 += i6 * i9;
				j6 += l6 * i9;
				if (j != k && i7 < k7 || j == k && i7 > i8) {
					i -= k;
					k -= j;
					j = lineOffsets[j];
					while (--k >= 0) {
						method379(DrawingArea.pixels, ai, j, l >> 16, i1 >> 16, k1 >> 8, l1 >> 8, l4, k5, j6, i5, l5, k6);
						l += i7;
						i1 += k7;
						k1 += j7;
						l1 += l7;
						j += DrawingArea.width;
						l4 += j5;
						k5 += i6;
						j6 += l6;
					}
					while (--i >= 0) {
						method379(DrawingArea.pixels, ai, j, l >> 16, j1 >> 16, k1 >> 8, i2 >> 8, l4, k5, j6, i5, l5, k6);
						l += i7;
						j1 += i8;
						k1 += j7;
						i2 += j8;
						j += DrawingArea.width;
						l4 += j5;
						k5 += i6;
						j6 += l6;
					}
					return;
				}
				i -= k;
				k -= j;
				j = lineOffsets[j];
				while (--k >= 0) {
					method379(DrawingArea.pixels, ai, j, i1 >> 16, l >> 16, l1 >> 8, k1 >> 8, l4, k5, j6, i5, l5, k6);
					l += i7;
					i1 += k7;
					k1 += j7;
					l1 += l7;
					j += DrawingArea.width;
					l4 += j5;
					k5 += i6;
					j6 += l6;
				}
				while (--i >= 0) {
					method379(DrawingArea.pixels, ai, j, j1 >> 16, l >> 16, i2 >> 8, k1 >> 8, l4, k5, j6, i5, l5, k6);
					l += i7;
					j1 += i8;
					k1 += j7;
					i2 += j8;
					j += DrawingArea.width;
					l4 += j5;
					k5 += i6;
					j6 += l6;
				}
				return;
			}
			j1 = i1 <<= 16;
			i2 = l1 <<= 16;
			if (j < 0) {
				j1 -= i7 * j;
				i1 -= k7 * j;
				i2 -= j7 * j;
				l1 -= l7 * j;
				j = 0;
			}
			l <<= 16;
			k1 <<= 16;
			if (i < 0) {
				l -= i8 * i;
				k1 -= j8 * i;
				i = 0;
			}
			int j9 = j - textureInt2;
			l4 += j5 * j9;
			k5 += i6 * j9;
			j6 += l6 * j9;
			if (i7 < k7) {
				k -= i;
				i -= j;
				j = lineOffsets[j];
				while (--i >= 0) {
					method379(DrawingArea.pixels, ai, j, j1 >> 16, i1 >> 16, i2 >> 8, l1 >> 8, l4, k5, j6, i5, l5, k6);
					j1 += i7;
					i1 += k7;
					i2 += j7;
					l1 += l7;
					j += DrawingArea.width;
					l4 += j5;
					k5 += i6;
					j6 += l6;
				}
				while (--k >= 0) {
					method379(DrawingArea.pixels, ai, j, l >> 16, i1 >> 16, k1 >> 8, l1 >> 8, l4, k5, j6, i5, l5, k6);
					l += i8;
					i1 += k7;
					k1 += j8;
					l1 += l7;
					j += DrawingArea.width;
					l4 += j5;
					k5 += i6;
					j6 += l6;
				}
				return;
			}
			k -= i;
			i -= j;
			j = lineOffsets[j];
			while (--i >= 0) {
				method379(DrawingArea.pixels, ai, j, i1 >> 16, j1 >> 16, l1 >> 8, i2 >> 8, l4, k5, j6, i5, l5, k6);
				j1 += i7;
				i1 += k7;
				i2 += j7;
				l1 += l7;
				j += DrawingArea.width;
				l4 += j5;
				k5 += i6;
				j6 += l6;
			}
			while (--k >= 0) {
				method379(DrawingArea.pixels, ai, j, i1 >> 16, l >> 16, l1 >> 8, k1 >> 8, l4, k5, j6, i5, l5, k6);
				l += i8;
				i1 += k7;
				k1 += j8;
				l1 += l7;
				j += DrawingArea.width;
				l4 += j5;
				k5 += i6;
				j6 += l6;
			}
			return;
		}
		if (k >= DrawingArea.bottomY) {
			return;
		}
		if (i > DrawingArea.bottomY) {
			i = DrawingArea.bottomY;
		}
		if (j > DrawingArea.bottomY) {
			j = DrawingArea.bottomY;
		}
		if (i < j) {
			i1 = j1 <<= 16;
			l1 = i2 <<= 16;
			if (k < 0) {
				i1 -= k7 * k;
				j1 -= i8 * k;
				l1 -= l7 * k;
				i2 -= j8 * k;
				k = 0;
			}
			l <<= 16;
			k1 <<= 16;
			if (i < 0) {
				l -= i7 * i;
				k1 -= j7 * i;
				i = 0;
			}
			int k9 = k - textureInt2;
			l4 += j5 * k9;
			k5 += i6 * k9;
			j6 += l6 * k9;
			if (k7 < i8) {
				j -= i;
				i -= k;
				k = lineOffsets[k];
				while (--i >= 0) {
					method379(DrawingArea.pixels, ai, k, i1 >> 16, j1 >> 16, l1 >> 8, i2 >> 8, l4, k5, j6, i5, l5, k6);
					i1 += k7;
					j1 += i8;
					l1 += l7;
					i2 += j8;
					k += DrawingArea.width;
					l4 += j5;
					k5 += i6;
					j6 += l6;
				}
				while (--j >= 0) {
					method379(DrawingArea.pixels, ai, k, i1 >> 16, l >> 16, l1 >> 8, k1 >> 8, l4, k5, j6, i5, l5, k6);
					i1 += k7;
					l += i7;
					l1 += l7;
					k1 += j7;
					k += DrawingArea.width;
					l4 += j5;
					k5 += i6;
					j6 += l6;
				}
				return;
			}
			j -= i;
			i -= k;
			k = lineOffsets[k];
			while (--i >= 0) {
				method379(DrawingArea.pixels, ai, k, j1 >> 16, i1 >> 16, i2 >> 8, l1 >> 8, l4, k5, j6, i5, l5, k6);
				i1 += k7;
				j1 += i8;
				l1 += l7;
				i2 += j8;
				k += DrawingArea.width;
				l4 += j5;
				k5 += i6;
				j6 += l6;
			}
			while (--j >= 0) {
				method379(DrawingArea.pixels, ai, k, l >> 16, i1 >> 16, k1 >> 8, l1 >> 8, l4, k5, j6, i5, l5, k6);
				i1 += k7;
				l += i7;
				l1 += l7;
				k1 += j7;
				k += DrawingArea.width;
				l4 += j5;
				k5 += i6;
				j6 += l6;
			}
			return;
		}
		l = j1 <<= 16;
		k1 = i2 <<= 16;
		if (k < 0) {
			l -= k7 * k;
			j1 -= i8 * k;
			k1 -= l7 * k;
			i2 -= j8 * k;
			k = 0;
		}
		i1 <<= 16;
		l1 <<= 16;
		if (j < 0) {
			i1 -= i7 * j;
			l1 -= j7 * j;
			j = 0;
		}
		int l9 = k - textureInt2;
		l4 += j5 * l9;
		k5 += i6 * l9;
		j6 += l6 * l9;
		if (k7 < i8) {
			i -= j;
			j -= k;
			k = lineOffsets[k];
			while (--j >= 0) {
				method379(DrawingArea.pixels, ai, k, l >> 16, j1 >> 16, k1 >> 8, i2 >> 8, l4, k5, j6, i5, l5, k6);
				l += k7;
				j1 += i8;
				k1 += l7;
				i2 += j8;
				k += DrawingArea.width;
				l4 += j5;
				k5 += i6;
				j6 += l6;
			}
			while (--i >= 0) {
				method379(DrawingArea.pixels, ai, k, i1 >> 16, j1 >> 16, l1 >> 8, i2 >> 8, l4, k5, j6, i5, l5, k6);
				i1 += i7;
				j1 += i8;
				l1 += j7;
				i2 += j8;
				k += DrawingArea.width;
				l4 += j5;
				k5 += i6;
				j6 += l6;
			}
			return;
		}
		i -= j;
		j -= k;
		k = lineOffsets[k];
		while (--j >= 0) {
			method379(DrawingArea.pixels, ai, k, j1 >> 16, l >> 16, i2 >> 8, k1 >> 8, l4, k5, j6, i5, l5, k6);
			l += k7;
			j1 += i8;
			k1 += l7;
			i2 += j8;
			k += DrawingArea.width;
			l4 += j5;
			k5 += i6;
			j6 += l6;
		}
		while (--i >= 0) {
			method379(DrawingArea.pixels, ai, k, j1 >> 16, i1 >> 16, i2 >> 8, l1 >> 8, l4, k5, j6, i5, l5, k6);
			i1 += i7;
			j1 += i8;
			l1 += j7;
			i2 += j8;
			k += DrawingArea.width;
			l4 += j5;
			k5 += i6;
			j6 += l6;
		}
	}

	private static void method379(int ai[], int ai1[], int k, int l, int i1, int j1, int k1, int l1, int i2, int j2, int k2, int l2, int i3) {
		int i = 0;// was parameter
		int j = 0;// was parameter
		if (l >= i1) {
			return;
		}
		int j3;
		int k3;
		if (aBoolean1462) {
			j3 = (k1 - j1) / (i1 - l);
			if (i1 > DrawingArea.scanlineRightLimit) {
				i1 = DrawingArea.scanlineRightLimit;
			}
			if (l < 0) {
				j1 -= l * j3;
				l = 0;
			}
			if (l >= i1) {
				return;
			}
			k3 = i1 - l >> 3;
			j3 <<= 12;
			j1 <<= 9;
		} else {
			if (i1 - l > 7) {
				k3 = i1 - l >> 3;
				j3 = (k1 - j1) * anIntArray1468[k3] >> 6;
			} else {
				k3 = 0;
				j3 = 0;
			}
			j1 <<= 9;
		}
		k += l;
		if (lowMem) {
			int i4 = 0;
			int k4 = 0;
			int k6 = l - textureInt1;
			l1 += (k2 >> 3) * k6;
			i2 += (l2 >> 3) * k6;
			j2 += (i3 >> 3) * k6;
			int i5 = j2 >> 12;
			if (i5 != 0) {
				i = l1 / i5;
				j = i2 / i5;
				if (i < 0) {
					i = 0;
				} else if (i > 4032) {
					i = 4032;
				}
			}
			l1 += k2;
			i2 += l2;
			j2 += i3;
			i5 = j2 >> 12;
			if (i5 != 0) {
				i4 = l1 / i5;
				k4 = i2 / i5;
				if (i4 < 7) {
					i4 = 7;
				} else if (i4 > 4032) {
					i4 = 4032;
				}
			}
			int i7 = i4 - i >> 3;
			int k7 = k4 - j >> 3;
			i += (j1 & 0x600000) >> 3;
			int i8 = j1 >> 23;
			if (aBoolean1463) {
				while (k3-- > 0) {
					ai[k++] = ai1[(j & 0xfc0) + (i >> 6)] >>> i8;
					i += i7;
					j += k7;
					ai[k++] = ai1[(j & 0xfc0) + (i >> 6)] >>> i8;
					i += i7;
					j += k7;
					ai[k++] = ai1[(j & 0xfc0) + (i >> 6)] >>> i8;
					i += i7;
					j += k7;
					ai[k++] = ai1[(j & 0xfc0) + (i >> 6)] >>> i8;
					i += i7;
					j += k7;
					ai[k++] = ai1[(j & 0xfc0) + (i >> 6)] >>> i8;
					i += i7;
					j += k7;
					ai[k++] = ai1[(j & 0xfc0) + (i >> 6)] >>> i8;
					i += i7;
					j += k7;
					ai[k++] = ai1[(j & 0xfc0) + (i >> 6)] >>> i8;
					i += i7;
					j += k7;
					ai[k++] = ai1[(j & 0xfc0) + (i >> 6)] >>> i8;
					i = i4;
					j = k4;
					l1 += k2;
					i2 += l2;
					j2 += i3;
					int j5 = j2 >> 12;
					if (j5 != 0) {
						i4 = l1 / j5;
						k4 = i2 / j5;
						if (i4 < 7) {
							i4 = 7;
						} else if (i4 > 4032) {
							i4 = 4032;
						}
					}
					i7 = i4 - i >> 3;
					k7 = k4 - j >> 3;
					j1 += j3;
					i += (j1 & 0x600000) >> 3;
					i8 = j1 >> 23;
				}
				for (k3 = i1 - l & 7; k3-- > 0;) {
					ai[k++] = ai1[(j & 0xfc0) + (i >> 6)] >>> i8;
					i += i7;
					j += k7;
				}

				return;
			}
			while (k3-- > 0) {
				int k8;
				if ((k8 = ai1[(j & 0xfc0) + (i >> 6)] >>> i8) != 0) {
					ai[k] = k8;
				}
				k++;
				i += i7;
				j += k7;
				if ((k8 = ai1[(j & 0xfc0) + (i >> 6)] >>> i8) != 0) {
					ai[k] = k8;
				}
				k++;
				i += i7;
				j += k7;
				if ((k8 = ai1[(j & 0xfc0) + (i >> 6)] >>> i8) != 0) {
					ai[k] = k8;
				}
				k++;
				i += i7;
				j += k7;
				if ((k8 = ai1[(j & 0xfc0) + (i >> 6)] >>> i8) != 0) {
					ai[k] = k8;
				}
				k++;
				i += i7;
				j += k7;
				if ((k8 = ai1[(j & 0xfc0) + (i >> 6)] >>> i8) != 0) {
					ai[k] = k8;
				}
				k++;
				i += i7;
				j += k7;
				if ((k8 = ai1[(j & 0xfc0) + (i >> 6)] >>> i8) != 0) {
					ai[k] = k8;
				}
				k++;
				i += i7;
				j += k7;
				if ((k8 = ai1[(j & 0xfc0) + (i >> 6)] >>> i8) != 0) {
					ai[k] = k8;
				}
				k++;
				i += i7;
				j += k7;
				if ((k8 = ai1[(j & 0xfc0) + (i >> 6)] >>> i8) != 0) {
					ai[k] = k8;
				}
				k++;
				i = i4;
				j = k4;
				l1 += k2;
				i2 += l2;
				j2 += i3;
				int k5 = j2 >> 12;
				if (k5 != 0) {
					i4 = l1 / k5;
					k4 = i2 / k5;
					if (i4 < 7) {
						i4 = 7;
					} else if (i4 > 4032) {
						i4 = 4032;
					}
				}
				i7 = i4 - i >> 3;
				k7 = k4 - j >> 3;
				j1 += j3;
				i += (j1 & 0x600000) >> 3;
				i8 = j1 >> 23;
			}
			for (k3 = i1 - l & 7; k3-- > 0;) {
				int l8;
				if ((l8 = ai1[(j & 0xfc0) + (i >> 6)] >>> i8) != 0) {
					ai[k] = l8;
				}
				k++;
				i += i7;
				j += k7;
			}

			return;
		}
		int j4 = 0;
		int l4 = 0;
		int l6 = l - textureInt1;
		l1 += (k2 >> 3) * l6;
		i2 += (l2 >> 3) * l6;
		j2 += (i3 >> 3) * l6;
		int l5 = j2 >> 14;
		if (l5 != 0) {
			i = l1 / l5;
			j = i2 / l5;
			if (i < 0) {
				i = 0;
			} else if (i > 16256) {
				i = 16256;
			}
		}
		l1 += k2;
		i2 += l2;
		j2 += i3;
		l5 = j2 >> 14;
		if (l5 != 0) {
			j4 = l1 / l5;
			l4 = i2 / l5;
			if (j4 < 7) {
				j4 = 7;
			} else if (j4 > 16256) {
				j4 = 16256;
			}
		}
		int j7 = j4 - i >> 3;
		int l7 = l4 - j >> 3;
		i += j1 & 0x600000;
		int j8 = j1 >> 23;
		if (aBoolean1463) {
			while (k3-- > 0) {
				ai[k++] = ai1[(j & 0x3f80) + (i >> 7)] >>> j8;
				i += j7;
				j += l7;
				ai[k++] = ai1[(j & 0x3f80) + (i >> 7)] >>> j8;
				i += j7;
				j += l7;
				ai[k++] = ai1[(j & 0x3f80) + (i >> 7)] >>> j8;
				i += j7;
				j += l7;
				ai[k++] = ai1[(j & 0x3f80) + (i >> 7)] >>> j8;
				i += j7;
				j += l7;
				ai[k++] = ai1[(j & 0x3f80) + (i >> 7)] >>> j8;
				i += j7;
				j += l7;
				ai[k++] = ai1[(j & 0x3f80) + (i >> 7)] >>> j8;
				i += j7;
				j += l7;
				ai[k++] = ai1[(j & 0x3f80) + (i >> 7)] >>> j8;
				i += j7;
				j += l7;
				ai[k++] = ai1[(j & 0x3f80) + (i >> 7)] >>> j8;
				i = j4;
				j = l4;
				l1 += k2;
				i2 += l2;
				j2 += i3;
				int i6 = j2 >> 14;
				if (i6 != 0) {
					j4 = l1 / i6;
					l4 = i2 / i6;
					if (j4 < 7) {
						j4 = 7;
					} else if (j4 > 16256) {
						j4 = 16256;
					}
				}
				j7 = j4 - i >> 3;
				l7 = l4 - j >> 3;
				j1 += j3;
				i += j1 & 0x600000;
				j8 = j1 >> 23;
			}
			for (k3 = i1 - l & 7; k3-- > 0;) {
				ai[k++] = ai1[(j & 0x3f80) + (i >> 7)] >>> j8;
				i += j7;
				j += l7;
			}

			return;
		}
		while (k3-- > 0) {
			int i9;
			if ((i9 = ai1[(j & 0x3f80) + (i >> 7)] >>> j8) != 0) {
				ai[k] = i9;
			}
			k++;
			i += j7;
			j += l7;
			if ((i9 = ai1[(j & 0x3f80) + (i >> 7)] >>> j8) != 0) {
				ai[k] = i9;
			}
			k++;
			i += j7;
			j += l7;
			if ((i9 = ai1[(j & 0x3f80) + (i >> 7)] >>> j8) != 0) {
				ai[k] = i9;
			}
			k++;
			i += j7;
			j += l7;
			if ((i9 = ai1[(j & 0x3f80) + (i >> 7)] >>> j8) != 0) {
				ai[k] = i9;
			}
			k++;
			i += j7;
			j += l7;
			if ((i9 = ai1[(j & 0x3f80) + (i >> 7)] >>> j8) != 0) {
				ai[k] = i9;
			}
			k++;
			i += j7;
			j += l7;
			if ((i9 = ai1[(j & 0x3f80) + (i >> 7)] >>> j8) != 0) {
				ai[k] = i9;
			}
			k++;
			i += j7;
			j += l7;
			if ((i9 = ai1[(j & 0x3f80) + (i >> 7)] >>> j8) != 0) {
				ai[k] = i9;
			}
			k++;
			i += j7;
			j += l7;
			if ((i9 = ai1[(j & 0x3f80) + (i >> 7)] >>> j8) != 0) {
				ai[k] = i9;
			}
			k++;
			i = j4;
			j = l4;
			l1 += k2;
			i2 += l2;
			j2 += i3;
			int j6 = j2 >> 14;
			if (j6 != 0) {
				j4 = l1 / j6;
				l4 = i2 / j6;
				if (j4 < 7) {
					j4 = 7;
				} else if (j4 > 16256) {
					j4 = 16256;
				}
			}
			j7 = j4 - i >> 3;
			l7 = l4 - j >> 3;
			j1 += j3;
			i += j1 & 0x600000;
			j8 = j1 >> 23;
		}
		for (int l3 = i1 - l & 7; l3-- > 0;) {
			int j9;
			if ((j9 = ai1[(j & 0x3f80) + (i >> 7)] >>> j8) != 0) {
				ai[k] = j9;
			}
			k++;
			i += j7;
			j += l7;
		}

	}

	public static final int anInt1459 = -477;
	public static boolean lowMem = true;
	static boolean aBoolean1462;
	private static boolean aBoolean1463;
	public static boolean aBoolean1464 = true;
	public static int anInt1465;
	public static int textureInt1;
	public static int textureInt2;
	private static int[] anIntArray1468;
	public static final int[] anIntArray1469;
	public static int anIntArray1470[];
	public static int anIntArray1471[];
	public static int lineOffsets[];
	private static int loadedTextureCount;
	public static Background textureBackgrounds[] = new Background[50];
	private static boolean[] hasTransparentPixels = new boolean[50];
	private static int[] averageColors = new int[50];
	private static int freeBufferCount;
	private static int[][] freeBuffers;
	private static int[][] textureBuffers = new int[50][];
	public static int lastUsedStamps[] = new int[50];
	public static int useCounter;
	public static int hslToRgbPalette[] = new int[0x10000];
	private static int[][] adjustedPalettes = new int[50][];

	static {
		anIntArray1468 = new int[512];
		anIntArray1469 = new int[2048];
		anIntArray1470 = new int[2048];
		anIntArray1471 = new int[2048];
		for (int i = 1; i < 512; i++) {
			anIntArray1468[i] = 32768 / i;
		}

		for (int j = 1; j < 2048; j++) {
			anIntArray1469[j] = 0x10000 / j;
		}

		for (int k = 0; k < 2048; k++) {
			anIntArray1470[k] = (int) (65536D * Math.sin(k * 0.0030679614999999999D));
			anIntArray1471[k] = (int) (65536D * Math.cos(k * 0.0030679614999999999D));
		}

	}
}
