// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

final class Texture extends DrawingArea {

	public static void nullLoader() {
		reciprocalTable15 = null;
		reciprocalTable15 = null;
		sineTable = null;
		cosineTable = null;
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

	public static void drawShadedTriangle(int y1, int y2, int y3, int x1, int x2, int x3, int hsl1, int hsl2, int hsl3) {
		int edge12XStep = 0;
		int edge12ColorStep = 0;
		if (y2 != y1) {
			edge12XStep = (x2 - x1 << 16) / (y2 - y1);
			edge12ColorStep = (hsl2 - hsl1 << 15) / (y2 - y1);
		}
		int edge23XStep = 0;
		int edge23ColorStep = 0;
		if (y3 != y2) {
			edge23XStep = (x3 - x2 << 16) / (y3 - y2);
			edge23ColorStep = (hsl3 - hsl2 << 15) / (y3 - y2);
		}
		int edge31XStep = 0;
		int edge31ColorStep = 0;
		if (y3 != y1) {
			edge31XStep = (x1 - x3 << 16) / (y1 - y3);
			edge31ColorStep = (hsl1 - hsl3 << 15) / (y1 - y3);
		}
		if (y1 <= y2 && y1 <= y3) {
			if (y1 >= DrawingArea.bottomY) {
				return;
			}
			if (y2 > DrawingArea.bottomY) {
				y2 = DrawingArea.bottomY;
			}
			if (y3 > DrawingArea.bottomY) {
				y3 = DrawingArea.bottomY;
			}
			if (y2 < y3) {
				x3 = x1 <<= 16;
				hsl3 = hsl1 <<= 15;
				if (y1 < 0) {
					x3 -= edge31XStep * y1;
					x1 -= edge12XStep * y1;
					hsl3 -= edge31ColorStep * y1;
					hsl1 -= edge12ColorStep * y1;
					y1 = 0;
				}
				x2 <<= 16;
				hsl2 <<= 15;
				if (y2 < 0) {
					x2 -= edge23XStep * y2;
					hsl2 -= edge23ColorStep * y2;
					y2 = 0;
				}
				if (y1 != y2 && edge31XStep < edge12XStep || y1 == y2 && edge31XStep > edge23XStep) {
					y3 -= y2;
					y2 -= y1;
					for (y1 = lineOffsets[y1]; --y2 >= 0; y1 += DrawingArea.width) {
						drawShadedScanline(DrawingArea.pixels, y1, x3 >> 16, x1 >> 16, hsl3 >> 7, hsl1 >> 7);
						x3 += edge31XStep;
						x1 += edge12XStep;
						hsl3 += edge31ColorStep;
						hsl1 += edge12ColorStep;
					}

					while (--y3 >= 0) {
						drawShadedScanline(DrawingArea.pixels, y1, x3 >> 16, x2 >> 16, hsl3 >> 7, hsl2 >> 7);
						x3 += edge31XStep;
						x2 += edge23XStep;
						hsl3 += edge31ColorStep;
						hsl2 += edge23ColorStep;
						y1 += DrawingArea.width;
					}
					return;
				}
				y3 -= y2;
				y2 -= y1;
				for (y1 = lineOffsets[y1]; --y2 >= 0; y1 += DrawingArea.width) {
					drawShadedScanline(DrawingArea.pixels, y1, x1 >> 16, x3 >> 16, hsl1 >> 7, hsl3 >> 7);
					x3 += edge31XStep;
					x1 += edge12XStep;
					hsl3 += edge31ColorStep;
					hsl1 += edge12ColorStep;
				}

				while (--y3 >= 0) {
					drawShadedScanline(DrawingArea.pixels, y1, x2 >> 16, x3 >> 16, hsl2 >> 7, hsl3 >> 7);
					x3 += edge31XStep;
					x2 += edge23XStep;
					hsl3 += edge31ColorStep;
					hsl2 += edge23ColorStep;
					y1 += DrawingArea.width;
				}
				return;
			}
			x2 = x1 <<= 16;
			hsl2 = hsl1 <<= 15;
			if (y1 < 0) {
				x2 -= edge31XStep * y1;
				x1 -= edge12XStep * y1;
				hsl2 -= edge31ColorStep * y1;
				hsl1 -= edge12ColorStep * y1;
				y1 = 0;
			}
			x3 <<= 16;
			hsl3 <<= 15;
			if (y3 < 0) {
				x3 -= edge23XStep * y3;
				hsl3 -= edge23ColorStep * y3;
				y3 = 0;
			}
			if (y1 != y3 && edge31XStep < edge12XStep || y1 == y3 && edge23XStep > edge12XStep) {
				y2 -= y3;
				y3 -= y1;
				for (y1 = lineOffsets[y1]; --y3 >= 0; y1 += DrawingArea.width) {
					drawShadedScanline(DrawingArea.pixels, y1, x2 >> 16, x1 >> 16, hsl2 >> 7, hsl1 >> 7);
					x2 += edge31XStep;
					x1 += edge12XStep;
					hsl2 += edge31ColorStep;
					hsl1 += edge12ColorStep;
				}

				while (--y2 >= 0) {
					drawShadedScanline(DrawingArea.pixels, y1, x3 >> 16, x1 >> 16, hsl3 >> 7, hsl1 >> 7);
					x3 += edge23XStep;
					x1 += edge12XStep;
					hsl3 += edge23ColorStep;
					hsl1 += edge12ColorStep;
					y1 += DrawingArea.width;
				}
				return;
			}
			y2 -= y3;
			y3 -= y1;
			for (y1 = lineOffsets[y1]; --y3 >= 0; y1 += DrawingArea.width) {
				drawShadedScanline(DrawingArea.pixels, y1, x1 >> 16, x2 >> 16, hsl1 >> 7, hsl2 >> 7);
				x2 += edge31XStep;
				x1 += edge12XStep;
				hsl2 += edge31ColorStep;
				hsl1 += edge12ColorStep;
			}

			while (--y2 >= 0) {
				drawShadedScanline(DrawingArea.pixels, y1, x1 >> 16, x3 >> 16, hsl1 >> 7, hsl3 >> 7);
				x3 += edge23XStep;
				x1 += edge12XStep;
				hsl3 += edge23ColorStep;
				hsl1 += edge12ColorStep;
				y1 += DrawingArea.width;
			}
			return;
		}
		if (y2 <= y3) {
			if (y2 >= DrawingArea.bottomY) {
				return;
			}
			if (y3 > DrawingArea.bottomY) {
				y3 = DrawingArea.bottomY;
			}
			if (y1 > DrawingArea.bottomY) {
				y1 = DrawingArea.bottomY;
			}
			if (y3 < y1) {
				x1 = x2 <<= 16;
				hsl1 = hsl2 <<= 15;
				if (y2 < 0) {
					x1 -= edge12XStep * y2;
					x2 -= edge23XStep * y2;
					hsl1 -= edge12ColorStep * y2;
					hsl2 -= edge23ColorStep * y2;
					y2 = 0;
				}
				x3 <<= 16;
				hsl3 <<= 15;
				if (y3 < 0) {
					x3 -= edge31XStep * y3;
					hsl3 -= edge31ColorStep * y3;
					y3 = 0;
				}
				if (y2 != y3 && edge12XStep < edge23XStep || y2 == y3 && edge12XStep > edge31XStep) {
					y1 -= y3;
					y3 -= y2;
					for (y2 = lineOffsets[y2]; --y3 >= 0; y2 += DrawingArea.width) {
						drawShadedScanline(DrawingArea.pixels, y2, x1 >> 16, x2 >> 16, hsl1 >> 7, hsl2 >> 7);
						x1 += edge12XStep;
						x2 += edge23XStep;
						hsl1 += edge12ColorStep;
						hsl2 += edge23ColorStep;
					}

					while (--y1 >= 0) {
						drawShadedScanline(DrawingArea.pixels, y2, x1 >> 16, x3 >> 16, hsl1 >> 7, hsl3 >> 7);
						x1 += edge12XStep;
						x3 += edge31XStep;
						hsl1 += edge12ColorStep;
						hsl3 += edge31ColorStep;
						y2 += DrawingArea.width;
					}
					return;
				}
				y1 -= y3;
				y3 -= y2;
				for (y2 = lineOffsets[y2]; --y3 >= 0; y2 += DrawingArea.width) {
					drawShadedScanline(DrawingArea.pixels, y2, x2 >> 16, x1 >> 16, hsl2 >> 7, hsl1 >> 7);
					x1 += edge12XStep;
					x2 += edge23XStep;
					hsl1 += edge12ColorStep;
					hsl2 += edge23ColorStep;
				}

				while (--y1 >= 0) {
					drawShadedScanline(DrawingArea.pixels, y2, x3 >> 16, x1 >> 16, hsl3 >> 7, hsl1 >> 7);
					x1 += edge12XStep;
					x3 += edge31XStep;
					hsl1 += edge12ColorStep;
					hsl3 += edge31ColorStep;
					y2 += DrawingArea.width;
				}
				return;
			}
			x3 = x2 <<= 16;
			hsl3 = hsl2 <<= 15;
			if (y2 < 0) {
				x3 -= edge12XStep * y2;
				x2 -= edge23XStep * y2;
				hsl3 -= edge12ColorStep * y2;
				hsl2 -= edge23ColorStep * y2;
				y2 = 0;
			}
			x1 <<= 16;
			hsl1 <<= 15;
			if (y1 < 0) {
				x1 -= edge31XStep * y1;
				hsl1 -= edge31ColorStep * y1;
				y1 = 0;
			}
			if (edge12XStep < edge23XStep) {
				y3 -= y1;
				y1 -= y2;
				for (y2 = lineOffsets[y2]; --y1 >= 0; y2 += DrawingArea.width) {
					drawShadedScanline(DrawingArea.pixels, y2, x3 >> 16, x2 >> 16, hsl3 >> 7, hsl2 >> 7);
					x3 += edge12XStep;
					x2 += edge23XStep;
					hsl3 += edge12ColorStep;
					hsl2 += edge23ColorStep;
				}

				while (--y3 >= 0) {
					drawShadedScanline(DrawingArea.pixels, y2, x1 >> 16, x2 >> 16, hsl1 >> 7, hsl2 >> 7);
					x1 += edge31XStep;
					x2 += edge23XStep;
					hsl1 += edge31ColorStep;
					hsl2 += edge23ColorStep;
					y2 += DrawingArea.width;
				}
				return;
			}
			y3 -= y1;
			y1 -= y2;
			for (y2 = lineOffsets[y2]; --y1 >= 0; y2 += DrawingArea.width) {
				drawShadedScanline(DrawingArea.pixels, y2, x2 >> 16, x3 >> 16, hsl2 >> 7, hsl3 >> 7);
				x3 += edge12XStep;
				x2 += edge23XStep;
				hsl3 += edge12ColorStep;
				hsl2 += edge23ColorStep;
			}

			while (--y3 >= 0) {
				drawShadedScanline(DrawingArea.pixels, y2, x2 >> 16, x1 >> 16, hsl2 >> 7, hsl1 >> 7);
				x1 += edge31XStep;
				x2 += edge23XStep;
				hsl1 += edge31ColorStep;
				hsl2 += edge23ColorStep;
				y2 += DrawingArea.width;
			}
			return;
		}
		if (y3 >= DrawingArea.bottomY) {
			return;
		}
		if (y1 > DrawingArea.bottomY) {
			y1 = DrawingArea.bottomY;
		}
		if (y2 > DrawingArea.bottomY) {
			y2 = DrawingArea.bottomY;
		}
		if (y1 < y2) {
			x2 = x3 <<= 16;
			hsl2 = hsl3 <<= 15;
			if (y3 < 0) {
				x2 -= edge23XStep * y3;
				x3 -= edge31XStep * y3;
				hsl2 -= edge23ColorStep * y3;
				hsl3 -= edge31ColorStep * y3;
				y3 = 0;
			}
			x1 <<= 16;
			hsl1 <<= 15;
			if (y1 < 0) {
				x1 -= edge12XStep * y1;
				hsl1 -= edge12ColorStep * y1;
				y1 = 0;
			}
			if (edge23XStep < edge31XStep) {
				y2 -= y1;
				y1 -= y3;
				for (y3 = lineOffsets[y3]; --y1 >= 0; y3 += DrawingArea.width) {
					drawShadedScanline(DrawingArea.pixels, y3, x2 >> 16, x3 >> 16, hsl2 >> 7, hsl3 >> 7);
					x2 += edge23XStep;
					x3 += edge31XStep;
					hsl2 += edge23ColorStep;
					hsl3 += edge31ColorStep;
				}

				while (--y2 >= 0) {
					drawShadedScanline(DrawingArea.pixels, y3, x2 >> 16, x1 >> 16, hsl2 >> 7, hsl1 >> 7);
					x2 += edge23XStep;
					x1 += edge12XStep;
					hsl2 += edge23ColorStep;
					hsl1 += edge12ColorStep;
					y3 += DrawingArea.width;
				}
				return;
			}
			y2 -= y1;
			y1 -= y3;
			for (y3 = lineOffsets[y3]; --y1 >= 0; y3 += DrawingArea.width) {
				drawShadedScanline(DrawingArea.pixels, y3, x3 >> 16, x2 >> 16, hsl3 >> 7, hsl2 >> 7);
				x2 += edge23XStep;
				x3 += edge31XStep;
				hsl2 += edge23ColorStep;
				hsl3 += edge31ColorStep;
			}

			while (--y2 >= 0) {
				drawShadedScanline(DrawingArea.pixels, y3, x1 >> 16, x2 >> 16, hsl1 >> 7, hsl2 >> 7);
				x2 += edge23XStep;
				x1 += edge12XStep;
				hsl2 += edge23ColorStep;
				hsl1 += edge12ColorStep;
				y3 += DrawingArea.width;
			}
			return;
		}
		x1 = x3 <<= 16;
		hsl1 = hsl3 <<= 15;
		if (y3 < 0) {
			x1 -= edge23XStep * y3;
			x3 -= edge31XStep * y3;
			hsl1 -= edge23ColorStep * y3;
			hsl3 -= edge31ColorStep * y3;
			y3 = 0;
		}
		x2 <<= 16;
		hsl2 <<= 15;
		if (y2 < 0) {
			x2 -= edge12XStep * y2;
			hsl2 -= edge12ColorStep * y2;
			y2 = 0;
		}
		if (edge23XStep < edge31XStep) {
			y1 -= y2;
			y2 -= y3;
			for (y3 = lineOffsets[y3]; --y2 >= 0; y3 += DrawingArea.width) {
				drawShadedScanline(DrawingArea.pixels, y3, x1 >> 16, x3 >> 16, hsl1 >> 7, hsl3 >> 7);
				x1 += edge23XStep;
				x3 += edge31XStep;
				hsl1 += edge23ColorStep;
				hsl3 += edge31ColorStep;
			}

			while (--y1 >= 0) {
				drawShadedScanline(DrawingArea.pixels, y3, x2 >> 16, x3 >> 16, hsl2 >> 7, hsl3 >> 7);
				x2 += edge12XStep;
				x3 += edge31XStep;
				hsl2 += edge12ColorStep;
				hsl3 += edge31ColorStep;
				y3 += DrawingArea.width;
			}
			return;
		}
		y1 -= y2;
		y2 -= y3;
		for (y3 = lineOffsets[y3]; --y2 >= 0; y3 += DrawingArea.width) {
			drawShadedScanline(DrawingArea.pixels, y3, x3 >> 16, x1 >> 16, hsl3 >> 7, hsl1 >> 7);
			x1 += edge23XStep;
			x3 += edge31XStep;
			hsl1 += edge23ColorStep;
			hsl3 += edge31ColorStep;
		}

		while (--y1 >= 0) {
			drawShadedScanline(DrawingArea.pixels, y3, x3 >> 16, x2 >> 16, hsl3 >> 7, hsl2 >> 7);
			x2 += edge12XStep;
			x3 += edge31XStep;
			hsl2 += edge12ColorStep;
			hsl3 += edge31ColorStep;
			y3 += DrawingArea.width;
		}
	}

	private static void drawShadedScanline(int pixels[], int rowOffset, int leftX, int rightX, int leftHsl, int rightHsl) {
		int rgb;// was parameter
		int loopCounter;// was parameter
		if (blockShading) {
			int blockColorStep;
			if (clipScanlines) {
				if (rightX - leftX > 3) {
					blockColorStep = (rightHsl - leftHsl) / (rightX - leftX);
				} else {
					blockColorStep = 0;
				}
				if (rightX > DrawingArea.scanlineRightLimit) {
					rightX = DrawingArea.scanlineRightLimit;
				}
				if (leftX < 0) {
					leftHsl -= leftX * blockColorStep;
					leftX = 0;
				}
				if (leftX >= rightX) {
					return;
				}
				rowOffset += leftX;
				loopCounter = rightX - leftX >> 2;
				blockColorStep <<= 2;
			} else {
				if (leftX >= rightX) {
					return;
				}
				rowOffset += leftX;
				loopCounter = rightX - leftX >> 2;
				if (loopCounter > 0) {
					blockColorStep = (rightHsl - leftHsl) * reciprocalTable15[loopCounter] >> 15;
				} else {
					blockColorStep = 0;
				}
			}
			if (transparency == 0) {
				while (--loopCounter >= 0) {
					rgb = hslToRgbPalette[leftHsl >> 8];
					leftHsl += blockColorStep;
					pixels[rowOffset++] = rgb;
					pixels[rowOffset++] = rgb;
					pixels[rowOffset++] = rgb;
					pixels[rowOffset++] = rgb;
				}
				loopCounter = rightX - leftX & 3;
				if (loopCounter > 0) {
					rgb = hslToRgbPalette[leftHsl >> 8];
					do {
						pixels[rowOffset++] = rgb;
					} while (--loopCounter > 0);
					return;
				}
			} else {
				int destinationWeight = transparency;
				int sourceWeight = 256 - transparency;
				if (ClientSettings.FIX_TRANSPARENCY_OVERFLOW) {
					while (--loopCounter >= 0) {
						rgb = hslToRgbPalette[leftHsl >> 8];
						leftHsl += blockColorStep;
						rgb = ((rgb & 0xff00ff) * sourceWeight >> 8 & 0xff00ff) + ((rgb & 0xff00) * sourceWeight >> 8 & 0xff00);
						pixels[rowOffset] = rgb + ((pixels[rowOffset] & 0xff00ff) * destinationWeight >> 8 & 0xff00ff) + ((pixels[rowOffset] & 0xff00) * destinationWeight >> 8 & 0xff00);
						rowOffset++;
						pixels[rowOffset] = rgb + ((pixels[rowOffset] & 0xff00ff) * destinationWeight >> 8 & 0xff00ff) + ((pixels[rowOffset] & 0xff00) * destinationWeight >> 8 & 0xff00);
						rowOffset++;
						pixels[rowOffset] = rgb + ((pixels[rowOffset] & 0xff00ff) * destinationWeight >> 8 & 0xff00ff) + ((pixels[rowOffset] & 0xff00) * destinationWeight >> 8 & 0xff00);
						rowOffset++;
						pixels[rowOffset] = rgb + ((pixels[rowOffset] & 0xff00ff) * destinationWeight >> 8 & 0xff00ff) + ((pixels[rowOffset] & 0xff00) * destinationWeight >> 8 & 0xff00);
						rowOffset++;
					}
					loopCounter = rightX - leftX & 3;
					if (loopCounter > 0) {
						rgb = hslToRgbPalette[leftHsl >> 8];
						rgb = ((rgb & 0xff00ff) * sourceWeight >> 8 & 0xff00ff) + ((rgb & 0xff00) * sourceWeight >> 8 & 0xff00);
						do {
							pixels[rowOffset] = rgb + ((pixels[rowOffset] & 0xff00ff) * destinationWeight >> 8 & 0xff00ff) + ((pixels[rowOffset] & 0xff00) * destinationWeight >> 8 & 0xff00);
							rowOffset++;
						} while (--loopCounter > 0);
					}
				} else {
					while (--loopCounter >= 0) {
						rgb = hslToRgbPalette[leftHsl >> 8];
						leftHsl += blockColorStep;
						rgb = ((rgb & 0xff00ff) * sourceWeight >> 8 & 0xff00ff) + ((rgb & 0xff00) * sourceWeight >> 8 & 0xff00);
						pixels[rowOffset++] = rgb + ((pixels[rowOffset] & 0xff00ff) * destinationWeight >> 8 & 0xff00ff) + ((pixels[rowOffset] & 0xff00) * destinationWeight >> 8 & 0xff00);
						pixels[rowOffset++] = rgb + ((pixels[rowOffset] & 0xff00ff) * destinationWeight >> 8 & 0xff00ff) + ((pixels[rowOffset] & 0xff00) * destinationWeight >> 8 & 0xff00);
						pixels[rowOffset++] = rgb + ((pixels[rowOffset] & 0xff00ff) * destinationWeight >> 8 & 0xff00ff) + ((pixels[rowOffset] & 0xff00) * destinationWeight >> 8 & 0xff00);
						pixels[rowOffset++] = rgb + ((pixels[rowOffset] & 0xff00ff) * destinationWeight >> 8 & 0xff00ff) + ((pixels[rowOffset] & 0xff00) * destinationWeight >> 8 & 0xff00);
					}
					loopCounter = rightX - leftX & 3;
					if (loopCounter > 0) {
						rgb = hslToRgbPalette[leftHsl >> 8];
						rgb = ((rgb & 0xff00ff) * sourceWeight >> 8 & 0xff00ff) + ((rgb & 0xff00) * sourceWeight >> 8 & 0xff00);
						do {
							pixels[rowOffset++] = rgb + ((pixels[rowOffset] & 0xff00ff) * destinationWeight >> 8 & 0xff00ff) + ((pixels[rowOffset] & 0xff00) * destinationWeight >> 8 & 0xff00);
						} while (--loopCounter > 0);
					}
				}
			}
			return;
		}
		if (leftX >= rightX) {
			return;
		}
		int pixelColorStep = (rightHsl - leftHsl) / (rightX - leftX);
		if (clipScanlines) {
			if (rightX > DrawingArea.scanlineRightLimit) {
				rightX = DrawingArea.scanlineRightLimit;
			}
			if (leftX < 0) {
				leftHsl -= leftX * pixelColorStep;
				leftX = 0;
			}
			if (leftX >= rightX) {
				return;
			}
		}
		rowOffset += leftX;
		loopCounter = rightX - leftX;
		if (transparency == 0) {
			do {
				pixels[rowOffset++] = hslToRgbPalette[leftHsl >> 8];
				leftHsl += pixelColorStep;
			} while (--loopCounter > 0);
			return;
		}
		int destinationWeight = transparency;
		int sourceWeight = 256 - transparency;
		if (ClientSettings.FIX_TRANSPARENCY_OVERFLOW) {
			do {
				rgb = hslToRgbPalette[leftHsl >> 8];
				leftHsl += pixelColorStep;
				rgb = ((rgb & 0xff00ff) * sourceWeight >> 8 & 0xff00ff) + ((rgb & 0xff00) * sourceWeight >> 8 & 0xff00);
				pixels[rowOffset] = rgb + ((pixels[rowOffset] & 0xff00ff) * destinationWeight >> 8 & 0xff00ff) + ((pixels[rowOffset] & 0xff00) * destinationWeight >> 8 & 0xff00);
				rowOffset++;
			} while (--loopCounter > 0);
		} else {
			do {
				rgb = hslToRgbPalette[leftHsl >> 8];
				leftHsl += pixelColorStep;
				rgb = ((rgb & 0xff00ff) * sourceWeight >> 8 & 0xff00ff) + ((rgb & 0xff00) * sourceWeight >> 8 & 0xff00);
				pixels[rowOffset++] = rgb + ((pixels[rowOffset] & 0xff00ff) * destinationWeight >> 8 & 0xff00ff) + ((pixels[rowOffset] & 0xff00) * destinationWeight >> 8 & 0xff00);
			} while (--loopCounter > 0);
		}
	}

	public static void drawFlatTriangle(int y1, int y2, int y3, int x1, int x2, int x3, int rgb) {
		int edge12XStep = 0;
		if (y2 != y1) {
			edge12XStep = (x2 - x1 << 16) / (y2 - y1);
		}
		int edge23XStep = 0;
		if (y3 != y2) {
			edge23XStep = (x3 - x2 << 16) / (y3 - y2);
		}
		int edge31XStep = 0;
		if (y3 != y1) {
			edge31XStep = (x1 - x3 << 16) / (y1 - y3);
		}
		if (y1 <= y2 && y1 <= y3) {
			if (y1 >= DrawingArea.bottomY) {
				return;
			}
			if (y2 > DrawingArea.bottomY) {
				y2 = DrawingArea.bottomY;
			}
			if (y3 > DrawingArea.bottomY) {
				y3 = DrawingArea.bottomY;
			}
			if (y2 < y3) {
				x3 = x1 <<= 16;
				if (y1 < 0) {
					x3 -= edge31XStep * y1;
					x1 -= edge12XStep * y1;
					y1 = 0;
				}
				x2 <<= 16;
				if (y2 < 0) {
					x2 -= edge23XStep * y2;
					y2 = 0;
				}
				if (y1 != y2 && edge31XStep < edge12XStep || y1 == y2 && edge31XStep > edge23XStep) {
					y3 -= y2;
					y2 -= y1;
					for (y1 = lineOffsets[y1]; --y2 >= 0; y1 += DrawingArea.width) {
						drawFlatScanline(DrawingArea.pixels, y1, rgb, x3 >> 16, x1 >> 16);
						x3 += edge31XStep;
						x1 += edge12XStep;
					}

					while (--y3 >= 0) {
						drawFlatScanline(DrawingArea.pixels, y1, rgb, x3 >> 16, x2 >> 16);
						x3 += edge31XStep;
						x2 += edge23XStep;
						y1 += DrawingArea.width;
					}
					return;
				}
				y3 -= y2;
				y2 -= y1;
				for (y1 = lineOffsets[y1]; --y2 >= 0; y1 += DrawingArea.width) {
					drawFlatScanline(DrawingArea.pixels, y1, rgb, x1 >> 16, x3 >> 16);
					x3 += edge31XStep;
					x1 += edge12XStep;
				}

				while (--y3 >= 0) {
					drawFlatScanline(DrawingArea.pixels, y1, rgb, x2 >> 16, x3 >> 16);
					x3 += edge31XStep;
					x2 += edge23XStep;
					y1 += DrawingArea.width;
				}
				return;
			}
			x2 = x1 <<= 16;
			if (y1 < 0) {
				x2 -= edge31XStep * y1;
				x1 -= edge12XStep * y1;
				y1 = 0;
			}
			x3 <<= 16;
			if (y3 < 0) {
				x3 -= edge23XStep * y3;
				y3 = 0;
			}
			if (y1 != y3 && edge31XStep < edge12XStep || y1 == y3 && edge23XStep > edge12XStep) {
				y2 -= y3;
				y3 -= y1;
				for (y1 = lineOffsets[y1]; --y3 >= 0; y1 += DrawingArea.width) {
					drawFlatScanline(DrawingArea.pixels, y1, rgb, x2 >> 16, x1 >> 16);
					x2 += edge31XStep;
					x1 += edge12XStep;
				}

				while (--y2 >= 0) {
					drawFlatScanline(DrawingArea.pixels, y1, rgb, x3 >> 16, x1 >> 16);
					x3 += edge23XStep;
					x1 += edge12XStep;
					y1 += DrawingArea.width;
				}
				return;
			}
			y2 -= y3;
			y3 -= y1;
			for (y1 = lineOffsets[y1]; --y3 >= 0; y1 += DrawingArea.width) {
				drawFlatScanline(DrawingArea.pixels, y1, rgb, x1 >> 16, x2 >> 16);
				x2 += edge31XStep;
				x1 += edge12XStep;
			}

			while (--y2 >= 0) {
				drawFlatScanline(DrawingArea.pixels, y1, rgb, x1 >> 16, x3 >> 16);
				x3 += edge23XStep;
				x1 += edge12XStep;
				y1 += DrawingArea.width;
			}
			return;
		}
		if (y2 <= y3) {
			if (y2 >= DrawingArea.bottomY) {
				return;
			}
			if (y3 > DrawingArea.bottomY) {
				y3 = DrawingArea.bottomY;
			}
			if (y1 > DrawingArea.bottomY) {
				y1 = DrawingArea.bottomY;
			}
			if (y3 < y1) {
				x1 = x2 <<= 16;
				if (y2 < 0) {
					x1 -= edge12XStep * y2;
					x2 -= edge23XStep * y2;
					y2 = 0;
				}
				x3 <<= 16;
				if (y3 < 0) {
					x3 -= edge31XStep * y3;
					y3 = 0;
				}
				if (y2 != y3 && edge12XStep < edge23XStep || y2 == y3 && edge12XStep > edge31XStep) {
					y1 -= y3;
					y3 -= y2;
					for (y2 = lineOffsets[y2]; --y3 >= 0; y2 += DrawingArea.width) {
						drawFlatScanline(DrawingArea.pixels, y2, rgb, x1 >> 16, x2 >> 16);
						x1 += edge12XStep;
						x2 += edge23XStep;
					}

					while (--y1 >= 0) {
						drawFlatScanline(DrawingArea.pixels, y2, rgb, x1 >> 16, x3 >> 16);
						x1 += edge12XStep;
						x3 += edge31XStep;
						y2 += DrawingArea.width;
					}
					return;
				}
				y1 -= y3;
				y3 -= y2;
				for (y2 = lineOffsets[y2]; --y3 >= 0; y2 += DrawingArea.width) {
					drawFlatScanline(DrawingArea.pixels, y2, rgb, x2 >> 16, x1 >> 16);
					x1 += edge12XStep;
					x2 += edge23XStep;
				}

				while (--y1 >= 0) {
					drawFlatScanline(DrawingArea.pixels, y2, rgb, x3 >> 16, x1 >> 16);
					x1 += edge12XStep;
					x3 += edge31XStep;
					y2 += DrawingArea.width;
				}
				return;
			}
			x3 = x2 <<= 16;
			if (y2 < 0) {
				x3 -= edge12XStep * y2;
				x2 -= edge23XStep * y2;
				y2 = 0;
			}
			x1 <<= 16;
			if (y1 < 0) {
				x1 -= edge31XStep * y1;
				y1 = 0;
			}
			if (edge12XStep < edge23XStep) {
				y3 -= y1;
				y1 -= y2;
				for (y2 = lineOffsets[y2]; --y1 >= 0; y2 += DrawingArea.width) {
					drawFlatScanline(DrawingArea.pixels, y2, rgb, x3 >> 16, x2 >> 16);
					x3 += edge12XStep;
					x2 += edge23XStep;
				}

				while (--y3 >= 0) {
					drawFlatScanline(DrawingArea.pixels, y2, rgb, x1 >> 16, x2 >> 16);
					x1 += edge31XStep;
					x2 += edge23XStep;
					y2 += DrawingArea.width;
				}
				return;
			}
			y3 -= y1;
			y1 -= y2;
			for (y2 = lineOffsets[y2]; --y1 >= 0; y2 += DrawingArea.width) {
				drawFlatScanline(DrawingArea.pixels, y2, rgb, x2 >> 16, x3 >> 16);
				x3 += edge12XStep;
				x2 += edge23XStep;
			}

			while (--y3 >= 0) {
				drawFlatScanline(DrawingArea.pixels, y2, rgb, x2 >> 16, x1 >> 16);
				x1 += edge31XStep;
				x2 += edge23XStep;
				y2 += DrawingArea.width;
			}
			return;
		}
		if (y3 >= DrawingArea.bottomY) {
			return;
		}
		if (y1 > DrawingArea.bottomY) {
			y1 = DrawingArea.bottomY;
		}
		if (y2 > DrawingArea.bottomY) {
			y2 = DrawingArea.bottomY;
		}
		if (y1 < y2) {
			x2 = x3 <<= 16;
			if (y3 < 0) {
				x2 -= edge23XStep * y3;
				x3 -= edge31XStep * y3;
				y3 = 0;
			}
			x1 <<= 16;
			if (y1 < 0) {
				x1 -= edge12XStep * y1;
				y1 = 0;
			}
			if (edge23XStep < edge31XStep) {
				y2 -= y1;
				y1 -= y3;
				for (y3 = lineOffsets[y3]; --y1 >= 0; y3 += DrawingArea.width) {
					drawFlatScanline(DrawingArea.pixels, y3, rgb, x2 >> 16, x3 >> 16);
					x2 += edge23XStep;
					x3 += edge31XStep;
				}

				while (--y2 >= 0) {
					drawFlatScanline(DrawingArea.pixels, y3, rgb, x2 >> 16, x1 >> 16);
					x2 += edge23XStep;
					x1 += edge12XStep;
					y3 += DrawingArea.width;
				}
				return;
			}
			y2 -= y1;
			y1 -= y3;
			for (y3 = lineOffsets[y3]; --y1 >= 0; y3 += DrawingArea.width) {
				drawFlatScanline(DrawingArea.pixels, y3, rgb, x3 >> 16, x2 >> 16);
				x2 += edge23XStep;
				x3 += edge31XStep;
			}

			while (--y2 >= 0) {
				drawFlatScanline(DrawingArea.pixels, y3, rgb, x1 >> 16, x2 >> 16);
				x2 += edge23XStep;
				x1 += edge12XStep;
				y3 += DrawingArea.width;
			}
			return;
		}
		x1 = x3 <<= 16;
		if (y3 < 0) {
			x1 -= edge23XStep * y3;
			x3 -= edge31XStep * y3;
			y3 = 0;
		}
		x2 <<= 16;
		if (y2 < 0) {
			x2 -= edge12XStep * y2;
			y2 = 0;
		}
		if (edge23XStep < edge31XStep) {
			y1 -= y2;
			y2 -= y3;
			for (y3 = lineOffsets[y3]; --y2 >= 0; y3 += DrawingArea.width) {
				drawFlatScanline(DrawingArea.pixels, y3, rgb, x1 >> 16, x3 >> 16);
				x1 += edge23XStep;
				x3 += edge31XStep;
			}

			while (--y1 >= 0) {
				drawFlatScanline(DrawingArea.pixels, y3, rgb, x2 >> 16, x3 >> 16);
				x2 += edge12XStep;
				x3 += edge31XStep;
				y3 += DrawingArea.width;
			}
			return;
		}
		y1 -= y2;
		y2 -= y3;
		for (y3 = lineOffsets[y3]; --y2 >= 0; y3 += DrawingArea.width) {
			drawFlatScanline(DrawingArea.pixels, y3, rgb, x3 >> 16, x1 >> 16);
			x1 += edge23XStep;
			x3 += edge31XStep;
		}

		while (--y1 >= 0) {
			drawFlatScanline(DrawingArea.pixels, y3, rgb, x3 >> 16, x2 >> 16);
			x2 += edge12XStep;
			x3 += edge31XStep;
			y3 += DrawingArea.width;
		}
	}

	private static void drawFlatScanline(int pixels[], int rowOffset, int rgb, int leftX, int rightX) {
		int loopCounter;// was parameter
		if (clipScanlines) {
			if (rightX > DrawingArea.scanlineRightLimit) {
				rightX = DrawingArea.scanlineRightLimit;
			}
			if (leftX < 0) {
				leftX = 0;
			}
		}
		if (leftX >= rightX) {
			return;
		}
		rowOffset += leftX;
		loopCounter = rightX - leftX >> 2;
		if (transparency == 0) {
			while (--loopCounter >= 0) {
				pixels[rowOffset++] = rgb;
				pixels[rowOffset++] = rgb;
				pixels[rowOffset++] = rgb;
				pixels[rowOffset++] = rgb;
			}
			for (loopCounter = rightX - leftX & 3; --loopCounter >= 0;) {
				pixels[rowOffset++] = rgb;
			}

			return;
		}
		int destinationWeight = transparency;
		int sourceWeight = 256 - transparency;
		rgb = ((rgb & 0xff00ff) * sourceWeight >> 8 & 0xff00ff) + ((rgb & 0xff00) * sourceWeight >> 8 & 0xff00);
		if (ClientSettings.FIX_TRANSPARENCY_OVERFLOW) {
			while (--loopCounter >= 0) {
				pixels[rowOffset] = rgb + ((pixels[rowOffset] & 0xff00ff) * destinationWeight >> 8 & 0xff00ff) + ((pixels[rowOffset] & 0xff00) * destinationWeight >> 8 & 0xff00);
				rowOffset++;
				pixels[rowOffset] = rgb + ((pixels[rowOffset] & 0xff00ff) * destinationWeight >> 8 & 0xff00ff) + ((pixels[rowOffset] & 0xff00) * destinationWeight >> 8 & 0xff00);
				rowOffset++;
				pixels[rowOffset] = rgb + ((pixels[rowOffset] & 0xff00ff) * destinationWeight >> 8 & 0xff00ff) + ((pixels[rowOffset] & 0xff00) * destinationWeight >> 8 & 0xff00);
				rowOffset++;
				pixels[rowOffset] = rgb + ((pixels[rowOffset] & 0xff00ff) * destinationWeight >> 8 & 0xff00ff) + ((pixels[rowOffset] & 0xff00) * destinationWeight >> 8 & 0xff00);
				rowOffset++;
			}
			for (loopCounter = rightX - leftX & 3; --loopCounter >= 0;) {
				pixels[rowOffset] = rgb + ((pixels[rowOffset] & 0xff00ff) * destinationWeight >> 8 & 0xff00ff) + ((pixels[rowOffset] & 0xff00) * destinationWeight >> 8 & 0xff00);
				rowOffset++;
			}
		} else {
			while (--loopCounter >= 0) {
				pixels[rowOffset++] = rgb + ((pixels[rowOffset] & 0xff00ff) * destinationWeight >> 8 & 0xff00ff) + ((pixels[rowOffset] & 0xff00) * destinationWeight >> 8 & 0xff00);
				pixels[rowOffset++] = rgb + ((pixels[rowOffset] & 0xff00ff) * destinationWeight >> 8 & 0xff00ff) + ((pixels[rowOffset] & 0xff00) * destinationWeight >> 8 & 0xff00);
				pixels[rowOffset++] = rgb + ((pixels[rowOffset] & 0xff00ff) * destinationWeight >> 8 & 0xff00ff) + ((pixels[rowOffset] & 0xff00) * destinationWeight >> 8 & 0xff00);
				pixels[rowOffset++] = rgb + ((pixels[rowOffset] & 0xff00ff) * destinationWeight >> 8 & 0xff00ff) + ((pixels[rowOffset] & 0xff00) * destinationWeight >> 8 & 0xff00);
			}
			for (loopCounter = rightX - leftX & 3; --loopCounter >= 0;) {
				pixels[rowOffset++] = rgb + ((pixels[rowOffset] & 0xff00ff) * destinationWeight >> 8 & 0xff00ff) + ((pixels[rowOffset] & 0xff00) * destinationWeight >> 8 & 0xff00);
			}
		}
	}

	public static void drawTexturedTriangle(int y1, int y2, int y3, int x1, int x2, int x3, int shade1, int shade2, int shade3, int originX, int uPointX, int vPointX, int originY, int uPointY, int vPointY, int originZ, int uPointZ, int vPointZ, int textureId) {
		int texturePixels[] = getTexturePixels(textureId);
		opaqueTexture = !hasTransparentPixels[textureId];
		uPointX = originX - uPointX;
		uPointY = originY - uPointY;
		uPointZ = originZ - uPointZ;
		vPointX -= originX;
		vPointY -= originY;
		vPointZ -= originZ;
		int uNumerator = vPointX * originY - vPointY * originX << 14;
		int uXStep = vPointY * originZ - vPointZ * originY << 8;
		int uRowStep = vPointZ * originX - vPointX * originZ << 5;
		int vNumerator = uPointX * originY - uPointY * originX << 14;
		int vXStep = uPointY * originZ - uPointZ * originY << 8;
		int vRowStep = uPointZ * originX - uPointX * originZ << 5;
		int depthNumerator = uPointY * vPointX - uPointX * vPointY << 14;
		int depthXStep = uPointZ * vPointY - uPointY * vPointZ << 8;
		int depthRowStep = uPointX * vPointZ - uPointZ * vPointX << 5;
		int edge12XStep = 0;
		int edge12ShadeStep = 0;
		if (y2 != y1) {
			edge12XStep = (x2 - x1 << 16) / (y2 - y1);
			edge12ShadeStep = (shade2 - shade1 << 16) / (y2 - y1);
		}
		int edge23XStep = 0;
		int edge23ShadeStep = 0;
		if (y3 != y2) {
			edge23XStep = (x3 - x2 << 16) / (y3 - y2);
			edge23ShadeStep = (shade3 - shade2 << 16) / (y3 - y2);
		}
		int edge31XStep = 0;
		int edge31ShadeStep = 0;
		if (y3 != y1) {
			edge31XStep = (x1 - x3 << 16) / (y1 - y3);
			edge31ShadeStep = (shade1 - shade3 << 16) / (y1 - y3);
		}
		if (y1 <= y2 && y1 <= y3) {
			if (y1 >= DrawingArea.bottomY) {
				return;
			}
			if (y2 > DrawingArea.bottomY) {
				y2 = DrawingArea.bottomY;
			}
			if (y3 > DrawingArea.bottomY) {
				y3 = DrawingArea.bottomY;
			}
			if (y2 < y3) {
				x3 = x1 <<= 16;
				shade3 = shade1 <<= 16;
				if (y1 < 0) {
					x3 -= edge31XStep * y1;
					x1 -= edge12XStep * y1;
					shade3 -= edge31ShadeStep * y1;
					shade1 -= edge12ShadeStep * y1;
					y1 = 0;
				}
				x2 <<= 16;
				shade2 <<= 16;
				if (y2 < 0) {
					x2 -= edge23XStep * y2;
					shade2 -= edge23ShadeStep * y2;
					y2 = 0;
				}
				int rowFromCentre = y1 - textureInt2;
				uNumerator += uRowStep * rowFromCentre;
				vNumerator += vRowStep * rowFromCentre;
				depthNumerator += depthRowStep * rowFromCentre;
				if (y1 != y2 && edge31XStep < edge12XStep || y1 == y2 && edge31XStep > edge23XStep) {
					y3 -= y2;
					y2 -= y1;
					y1 = lineOffsets[y1];
					while (--y2 >= 0) {
						drawTexturedScanline(DrawingArea.pixels, texturePixels, y1, x3 >> 16, x1 >> 16, shade3 >> 8, shade1 >> 8, uNumerator, vNumerator, depthNumerator, uXStep, vXStep, depthXStep);
						x3 += edge31XStep;
						x1 += edge12XStep;
						shade3 += edge31ShadeStep;
						shade1 += edge12ShadeStep;
						y1 += DrawingArea.width;
						uNumerator += uRowStep;
						vNumerator += vRowStep;
						depthNumerator += depthRowStep;
					}
					while (--y3 >= 0) {
						drawTexturedScanline(DrawingArea.pixels, texturePixels, y1, x3 >> 16, x2 >> 16, shade3 >> 8, shade2 >> 8, uNumerator, vNumerator, depthNumerator, uXStep, vXStep, depthXStep);
						x3 += edge31XStep;
						x2 += edge23XStep;
						shade3 += edge31ShadeStep;
						shade2 += edge23ShadeStep;
						y1 += DrawingArea.width;
						uNumerator += uRowStep;
						vNumerator += vRowStep;
						depthNumerator += depthRowStep;
					}
					return;
				}
				y3 -= y2;
				y2 -= y1;
				y1 = lineOffsets[y1];
				while (--y2 >= 0) {
					drawTexturedScanline(DrawingArea.pixels, texturePixels, y1, x1 >> 16, x3 >> 16, shade1 >> 8, shade3 >> 8, uNumerator, vNumerator, depthNumerator, uXStep, vXStep, depthXStep);
					x3 += edge31XStep;
					x1 += edge12XStep;
					shade3 += edge31ShadeStep;
					shade1 += edge12ShadeStep;
					y1 += DrawingArea.width;
					uNumerator += uRowStep;
					vNumerator += vRowStep;
					depthNumerator += depthRowStep;
				}
				while (--y3 >= 0) {
					drawTexturedScanline(DrawingArea.pixels, texturePixels, y1, x2 >> 16, x3 >> 16, shade2 >> 8, shade3 >> 8, uNumerator, vNumerator, depthNumerator, uXStep, vXStep, depthXStep);
					x3 += edge31XStep;
					x2 += edge23XStep;
					shade3 += edge31ShadeStep;
					shade2 += edge23ShadeStep;
					y1 += DrawingArea.width;
					uNumerator += uRowStep;
					vNumerator += vRowStep;
					depthNumerator += depthRowStep;
				}
				return;
			}
			x2 = x1 <<= 16;
			shade2 = shade1 <<= 16;
			if (y1 < 0) {
				x2 -= edge31XStep * y1;
				x1 -= edge12XStep * y1;
				shade2 -= edge31ShadeStep * y1;
				shade1 -= edge12ShadeStep * y1;
				y1 = 0;
			}
			x3 <<= 16;
			shade3 <<= 16;
			if (y3 < 0) {
				x3 -= edge23XStep * y3;
				shade3 -= edge23ShadeStep * y3;
				y3 = 0;
			}
			int rowFromCentre = y1 - textureInt2;
			uNumerator += uRowStep * rowFromCentre;
			vNumerator += vRowStep * rowFromCentre;
			depthNumerator += depthRowStep * rowFromCentre;
			if (y1 != y3 && edge31XStep < edge12XStep || y1 == y3 && edge23XStep > edge12XStep) {
				y2 -= y3;
				y3 -= y1;
				y1 = lineOffsets[y1];
				while (--y3 >= 0) {
					drawTexturedScanline(DrawingArea.pixels, texturePixels, y1, x2 >> 16, x1 >> 16, shade2 >> 8, shade1 >> 8, uNumerator, vNumerator, depthNumerator, uXStep, vXStep, depthXStep);
					x2 += edge31XStep;
					x1 += edge12XStep;
					shade2 += edge31ShadeStep;
					shade1 += edge12ShadeStep;
					y1 += DrawingArea.width;
					uNumerator += uRowStep;
					vNumerator += vRowStep;
					depthNumerator += depthRowStep;
				}
				while (--y2 >= 0) {
					drawTexturedScanline(DrawingArea.pixels, texturePixels, y1, x3 >> 16, x1 >> 16, shade3 >> 8, shade1 >> 8, uNumerator, vNumerator, depthNumerator, uXStep, vXStep, depthXStep);
					x3 += edge23XStep;
					x1 += edge12XStep;
					shade3 += edge23ShadeStep;
					shade1 += edge12ShadeStep;
					y1 += DrawingArea.width;
					uNumerator += uRowStep;
					vNumerator += vRowStep;
					depthNumerator += depthRowStep;
				}
				return;
			}
			y2 -= y3;
			y3 -= y1;
			y1 = lineOffsets[y1];
			while (--y3 >= 0) {
				drawTexturedScanline(DrawingArea.pixels, texturePixels, y1, x1 >> 16, x2 >> 16, shade1 >> 8, shade2 >> 8, uNumerator, vNumerator, depthNumerator, uXStep, vXStep, depthXStep);
				x2 += edge31XStep;
				x1 += edge12XStep;
				shade2 += edge31ShadeStep;
				shade1 += edge12ShadeStep;
				y1 += DrawingArea.width;
				uNumerator += uRowStep;
				vNumerator += vRowStep;
				depthNumerator += depthRowStep;
			}
			while (--y2 >= 0) {
				drawTexturedScanline(DrawingArea.pixels, texturePixels, y1, x1 >> 16, x3 >> 16, shade1 >> 8, shade3 >> 8, uNumerator, vNumerator, depthNumerator, uXStep, vXStep, depthXStep);
				x3 += edge23XStep;
				x1 += edge12XStep;
				shade3 += edge23ShadeStep;
				shade1 += edge12ShadeStep;
				y1 += DrawingArea.width;
				uNumerator += uRowStep;
				vNumerator += vRowStep;
				depthNumerator += depthRowStep;
			}
			return;
		}
		if (y2 <= y3) {
			if (y2 >= DrawingArea.bottomY) {
				return;
			}
			if (y3 > DrawingArea.bottomY) {
				y3 = DrawingArea.bottomY;
			}
			if (y1 > DrawingArea.bottomY) {
				y1 = DrawingArea.bottomY;
			}
			if (y3 < y1) {
				x1 = x2 <<= 16;
				shade1 = shade2 <<= 16;
				if (y2 < 0) {
					x1 -= edge12XStep * y2;
					x2 -= edge23XStep * y2;
					shade1 -= edge12ShadeStep * y2;
					shade2 -= edge23ShadeStep * y2;
					y2 = 0;
				}
				x3 <<= 16;
				shade3 <<= 16;
				if (y3 < 0) {
					x3 -= edge31XStep * y3;
					shade3 -= edge31ShadeStep * y3;
					y3 = 0;
				}
				int rowFromCentre = y2 - textureInt2;
				uNumerator += uRowStep * rowFromCentre;
				vNumerator += vRowStep * rowFromCentre;
				depthNumerator += depthRowStep * rowFromCentre;
				if (y2 != y3 && edge12XStep < edge23XStep || y2 == y3 && edge12XStep > edge31XStep) {
					y1 -= y3;
					y3 -= y2;
					y2 = lineOffsets[y2];
					while (--y3 >= 0) {
						drawTexturedScanline(DrawingArea.pixels, texturePixels, y2, x1 >> 16, x2 >> 16, shade1 >> 8, shade2 >> 8, uNumerator, vNumerator, depthNumerator, uXStep, vXStep, depthXStep);
						x1 += edge12XStep;
						x2 += edge23XStep;
						shade1 += edge12ShadeStep;
						shade2 += edge23ShadeStep;
						y2 += DrawingArea.width;
						uNumerator += uRowStep;
						vNumerator += vRowStep;
						depthNumerator += depthRowStep;
					}
					while (--y1 >= 0) {
						drawTexturedScanline(DrawingArea.pixels, texturePixels, y2, x1 >> 16, x3 >> 16, shade1 >> 8, shade3 >> 8, uNumerator, vNumerator, depthNumerator, uXStep, vXStep, depthXStep);
						x1 += edge12XStep;
						x3 += edge31XStep;
						shade1 += edge12ShadeStep;
						shade3 += edge31ShadeStep;
						y2 += DrawingArea.width;
						uNumerator += uRowStep;
						vNumerator += vRowStep;
						depthNumerator += depthRowStep;
					}
					return;
				}
				y1 -= y3;
				y3 -= y2;
				y2 = lineOffsets[y2];
				while (--y3 >= 0) {
					drawTexturedScanline(DrawingArea.pixels, texturePixels, y2, x2 >> 16, x1 >> 16, shade2 >> 8, shade1 >> 8, uNumerator, vNumerator, depthNumerator, uXStep, vXStep, depthXStep);
					x1 += edge12XStep;
					x2 += edge23XStep;
					shade1 += edge12ShadeStep;
					shade2 += edge23ShadeStep;
					y2 += DrawingArea.width;
					uNumerator += uRowStep;
					vNumerator += vRowStep;
					depthNumerator += depthRowStep;
				}
				while (--y1 >= 0) {
					drawTexturedScanline(DrawingArea.pixels, texturePixels, y2, x3 >> 16, x1 >> 16, shade3 >> 8, shade1 >> 8, uNumerator, vNumerator, depthNumerator, uXStep, vXStep, depthXStep);
					x1 += edge12XStep;
					x3 += edge31XStep;
					shade1 += edge12ShadeStep;
					shade3 += edge31ShadeStep;
					y2 += DrawingArea.width;
					uNumerator += uRowStep;
					vNumerator += vRowStep;
					depthNumerator += depthRowStep;
				}
				return;
			}
			x3 = x2 <<= 16;
			shade3 = shade2 <<= 16;
			if (y2 < 0) {
				x3 -= edge12XStep * y2;
				x2 -= edge23XStep * y2;
				shade3 -= edge12ShadeStep * y2;
				shade2 -= edge23ShadeStep * y2;
				y2 = 0;
			}
			x1 <<= 16;
			shade1 <<= 16;
			if (y1 < 0) {
				x1 -= edge31XStep * y1;
				shade1 -= edge31ShadeStep * y1;
				y1 = 0;
			}
			int rowFromCentre = y2 - textureInt2;
			uNumerator += uRowStep * rowFromCentre;
			vNumerator += vRowStep * rowFromCentre;
			depthNumerator += depthRowStep * rowFromCentre;
			if (edge12XStep < edge23XStep) {
				y3 -= y1;
				y1 -= y2;
				y2 = lineOffsets[y2];
				while (--y1 >= 0) {
					drawTexturedScanline(DrawingArea.pixels, texturePixels, y2, x3 >> 16, x2 >> 16, shade3 >> 8, shade2 >> 8, uNumerator, vNumerator, depthNumerator, uXStep, vXStep, depthXStep);
					x3 += edge12XStep;
					x2 += edge23XStep;
					shade3 += edge12ShadeStep;
					shade2 += edge23ShadeStep;
					y2 += DrawingArea.width;
					uNumerator += uRowStep;
					vNumerator += vRowStep;
					depthNumerator += depthRowStep;
				}
				while (--y3 >= 0) {
					drawTexturedScanline(DrawingArea.pixels, texturePixels, y2, x1 >> 16, x2 >> 16, shade1 >> 8, shade2 >> 8, uNumerator, vNumerator, depthNumerator, uXStep, vXStep, depthXStep);
					x1 += edge31XStep;
					x2 += edge23XStep;
					shade1 += edge31ShadeStep;
					shade2 += edge23ShadeStep;
					y2 += DrawingArea.width;
					uNumerator += uRowStep;
					vNumerator += vRowStep;
					depthNumerator += depthRowStep;
				}
				return;
			}
			y3 -= y1;
			y1 -= y2;
			y2 = lineOffsets[y2];
			while (--y1 >= 0) {
				drawTexturedScanline(DrawingArea.pixels, texturePixels, y2, x2 >> 16, x3 >> 16, shade2 >> 8, shade3 >> 8, uNumerator, vNumerator, depthNumerator, uXStep, vXStep, depthXStep);
				x3 += edge12XStep;
				x2 += edge23XStep;
				shade3 += edge12ShadeStep;
				shade2 += edge23ShadeStep;
				y2 += DrawingArea.width;
				uNumerator += uRowStep;
				vNumerator += vRowStep;
				depthNumerator += depthRowStep;
			}
			while (--y3 >= 0) {
				drawTexturedScanline(DrawingArea.pixels, texturePixels, y2, x2 >> 16, x1 >> 16, shade2 >> 8, shade1 >> 8, uNumerator, vNumerator, depthNumerator, uXStep, vXStep, depthXStep);
				x1 += edge31XStep;
				x2 += edge23XStep;
				shade1 += edge31ShadeStep;
				shade2 += edge23ShadeStep;
				y2 += DrawingArea.width;
				uNumerator += uRowStep;
				vNumerator += vRowStep;
				depthNumerator += depthRowStep;
			}
			return;
		}
		if (y3 >= DrawingArea.bottomY) {
			return;
		}
		if (y1 > DrawingArea.bottomY) {
			y1 = DrawingArea.bottomY;
		}
		if (y2 > DrawingArea.bottomY) {
			y2 = DrawingArea.bottomY;
		}
		if (y1 < y2) {
			x2 = x3 <<= 16;
			shade2 = shade3 <<= 16;
			if (y3 < 0) {
				x2 -= edge23XStep * y3;
				x3 -= edge31XStep * y3;
				shade2 -= edge23ShadeStep * y3;
				shade3 -= edge31ShadeStep * y3;
				y3 = 0;
			}
			x1 <<= 16;
			shade1 <<= 16;
			if (y1 < 0) {
				x1 -= edge12XStep * y1;
				shade1 -= edge12ShadeStep * y1;
				y1 = 0;
			}
			int rowFromCentre = y3 - textureInt2;
			uNumerator += uRowStep * rowFromCentre;
			vNumerator += vRowStep * rowFromCentre;
			depthNumerator += depthRowStep * rowFromCentre;
			if (edge23XStep < edge31XStep) {
				y2 -= y1;
				y1 -= y3;
				y3 = lineOffsets[y3];
				while (--y1 >= 0) {
					drawTexturedScanline(DrawingArea.pixels, texturePixels, y3, x2 >> 16, x3 >> 16, shade2 >> 8, shade3 >> 8, uNumerator, vNumerator, depthNumerator, uXStep, vXStep, depthXStep);
					x2 += edge23XStep;
					x3 += edge31XStep;
					shade2 += edge23ShadeStep;
					shade3 += edge31ShadeStep;
					y3 += DrawingArea.width;
					uNumerator += uRowStep;
					vNumerator += vRowStep;
					depthNumerator += depthRowStep;
				}
				while (--y2 >= 0) {
					drawTexturedScanline(DrawingArea.pixels, texturePixels, y3, x2 >> 16, x1 >> 16, shade2 >> 8, shade1 >> 8, uNumerator, vNumerator, depthNumerator, uXStep, vXStep, depthXStep);
					x2 += edge23XStep;
					x1 += edge12XStep;
					shade2 += edge23ShadeStep;
					shade1 += edge12ShadeStep;
					y3 += DrawingArea.width;
					uNumerator += uRowStep;
					vNumerator += vRowStep;
					depthNumerator += depthRowStep;
				}
				return;
			}
			y2 -= y1;
			y1 -= y3;
			y3 = lineOffsets[y3];
			while (--y1 >= 0) {
				drawTexturedScanline(DrawingArea.pixels, texturePixels, y3, x3 >> 16, x2 >> 16, shade3 >> 8, shade2 >> 8, uNumerator, vNumerator, depthNumerator, uXStep, vXStep, depthXStep);
				x2 += edge23XStep;
				x3 += edge31XStep;
				shade2 += edge23ShadeStep;
				shade3 += edge31ShadeStep;
				y3 += DrawingArea.width;
				uNumerator += uRowStep;
				vNumerator += vRowStep;
				depthNumerator += depthRowStep;
			}
			while (--y2 >= 0) {
				drawTexturedScanline(DrawingArea.pixels, texturePixels, y3, x1 >> 16, x2 >> 16, shade1 >> 8, shade2 >> 8, uNumerator, vNumerator, depthNumerator, uXStep, vXStep, depthXStep);
				x2 += edge23XStep;
				x1 += edge12XStep;
				shade2 += edge23ShadeStep;
				shade1 += edge12ShadeStep;
				y3 += DrawingArea.width;
				uNumerator += uRowStep;
				vNumerator += vRowStep;
				depthNumerator += depthRowStep;
			}
			return;
		}
		x1 = x3 <<= 16;
		shade1 = shade3 <<= 16;
		if (y3 < 0) {
			x1 -= edge23XStep * y3;
			x3 -= edge31XStep * y3;
			shade1 -= edge23ShadeStep * y3;
			shade3 -= edge31ShadeStep * y3;
			y3 = 0;
		}
		x2 <<= 16;
		shade2 <<= 16;
		if (y2 < 0) {
			x2 -= edge12XStep * y2;
			shade2 -= edge12ShadeStep * y2;
			y2 = 0;
		}
		int rowFromCentre = y3 - textureInt2;
		uNumerator += uRowStep * rowFromCentre;
		vNumerator += vRowStep * rowFromCentre;
		depthNumerator += depthRowStep * rowFromCentre;
		if (edge23XStep < edge31XStep) {
			y1 -= y2;
			y2 -= y3;
			y3 = lineOffsets[y3];
			while (--y2 >= 0) {
				drawTexturedScanline(DrawingArea.pixels, texturePixels, y3, x1 >> 16, x3 >> 16, shade1 >> 8, shade3 >> 8, uNumerator, vNumerator, depthNumerator, uXStep, vXStep, depthXStep);
				x1 += edge23XStep;
				x3 += edge31XStep;
				shade1 += edge23ShadeStep;
				shade3 += edge31ShadeStep;
				y3 += DrawingArea.width;
				uNumerator += uRowStep;
				vNumerator += vRowStep;
				depthNumerator += depthRowStep;
			}
			while (--y1 >= 0) {
				drawTexturedScanline(DrawingArea.pixels, texturePixels, y3, x2 >> 16, x3 >> 16, shade2 >> 8, shade3 >> 8, uNumerator, vNumerator, depthNumerator, uXStep, vXStep, depthXStep);
				x2 += edge12XStep;
				x3 += edge31XStep;
				shade2 += edge12ShadeStep;
				shade3 += edge31ShadeStep;
				y3 += DrawingArea.width;
				uNumerator += uRowStep;
				vNumerator += vRowStep;
				depthNumerator += depthRowStep;
			}
			return;
		}
		y1 -= y2;
		y2 -= y3;
		y3 = lineOffsets[y3];
		while (--y2 >= 0) {
			drawTexturedScanline(DrawingArea.pixels, texturePixels, y3, x3 >> 16, x1 >> 16, shade3 >> 8, shade1 >> 8, uNumerator, vNumerator, depthNumerator, uXStep, vXStep, depthXStep);
			x1 += edge23XStep;
			x3 += edge31XStep;
			shade1 += edge23ShadeStep;
			shade3 += edge31ShadeStep;
			y3 += DrawingArea.width;
			uNumerator += uRowStep;
			vNumerator += vRowStep;
			depthNumerator += depthRowStep;
		}
		while (--y1 >= 0) {
			drawTexturedScanline(DrawingArea.pixels, texturePixels, y3, x3 >> 16, x2 >> 16, shade3 >> 8, shade2 >> 8, uNumerator, vNumerator, depthNumerator, uXStep, vXStep, depthXStep);
			x2 += edge12XStep;
			x3 += edge31XStep;
			shade2 += edge12ShadeStep;
			shade3 += edge31ShadeStep;
			y3 += DrawingArea.width;
			uNumerator += uRowStep;
			vNumerator += vRowStep;
			depthNumerator += depthRowStep;
		}
	}

	private static void drawTexturedScanline(int pixels[], int texture[], int rowOffset, int leftX, int rightX, int leftShade, int rightShade, int uNumerator, int vNumerator, int depthNumerator, int uXStep, int vXStep, int depthXStep) {
		int u = 0;// was parameter
		int v = 0;// was parameter
		if (leftX >= rightX) {
			return;
		}
		int shadeStep;
		int spanCount;
		if (clipScanlines) {
			shadeStep = (rightShade - leftShade) / (rightX - leftX);
			if (rightX > DrawingArea.scanlineRightLimit) {
				rightX = DrawingArea.scanlineRightLimit;
			}
			if (leftX < 0) {
				leftShade -= leftX * shadeStep;
				leftX = 0;
			}
			if (leftX >= rightX) {
				return;
			}
			spanCount = rightX - leftX >> 3;
			shadeStep <<= 12;
			leftShade <<= 9;
		} else {
			if (rightX - leftX > 7) {
				spanCount = rightX - leftX >> 3;
				shadeStep = (rightShade - leftShade) * reciprocalTable15[spanCount] >> 6;
			} else {
				spanCount = 0;
				shadeStep = 0;
			}
			leftShade <<= 9;
		}
		rowOffset += leftX;
		if (lowMem) {
			int nextU = 0;
			int nextV = 0;
			int xFromCentre = leftX - textureInt1;
			uNumerator += (uXStep >> 3) * xFromCentre;
			vNumerator += (vXStep >> 3) * xFromCentre;
			depthNumerator += (depthXStep >> 3) * xFromCentre;
			int divisor = depthNumerator >> 12;
			if (divisor != 0) {
				u = uNumerator / divisor;
				v = vNumerator / divisor;
				if (u < 0) {
					u = 0;
				} else if (u > 4032) {
					u = 4032;
				}
			}
			uNumerator += uXStep;
			vNumerator += vXStep;
			depthNumerator += depthXStep;
			divisor = depthNumerator >> 12;
			if (divisor != 0) {
				nextU = uNumerator / divisor;
				nextV = vNumerator / divisor;
				if (nextU < 7) {
					nextU = 7;
				} else if (nextU > 4032) {
					nextU = 4032;
				}
			}
			int uStep = nextU - u >> 3;
			int vStep = nextV - v >> 3;
			u += (leftShade & 0x600000) >> 3;
			int shadeShift = leftShade >> 23;
			if (opaqueTexture) {
				while (spanCount-- > 0) {
					pixels[rowOffset++] = texture[(v & 0xfc0) + (u >> 6)] >>> shadeShift;
					u += uStep;
					v += vStep;
					pixels[rowOffset++] = texture[(v & 0xfc0) + (u >> 6)] >>> shadeShift;
					u += uStep;
					v += vStep;
					pixels[rowOffset++] = texture[(v & 0xfc0) + (u >> 6)] >>> shadeShift;
					u += uStep;
					v += vStep;
					pixels[rowOffset++] = texture[(v & 0xfc0) + (u >> 6)] >>> shadeShift;
					u += uStep;
					v += vStep;
					pixels[rowOffset++] = texture[(v & 0xfc0) + (u >> 6)] >>> shadeShift;
					u += uStep;
					v += vStep;
					pixels[rowOffset++] = texture[(v & 0xfc0) + (u >> 6)] >>> shadeShift;
					u += uStep;
					v += vStep;
					pixels[rowOffset++] = texture[(v & 0xfc0) + (u >> 6)] >>> shadeShift;
					u += uStep;
					v += vStep;
					pixels[rowOffset++] = texture[(v & 0xfc0) + (u >> 6)] >>> shadeShift;
					u = nextU;
					v = nextV;
					uNumerator += uXStep;
					vNumerator += vXStep;
					depthNumerator += depthXStep;
					int loopDivisor = depthNumerator >> 12;
					if (loopDivisor != 0) {
						nextU = uNumerator / loopDivisor;
						nextV = vNumerator / loopDivisor;
						if (nextU < 7) {
							nextU = 7;
						} else if (nextU > 4032) {
							nextU = 4032;
						}
					}
					uStep = nextU - u >> 3;
					vStep = nextV - v >> 3;
					leftShade += shadeStep;
					u += (leftShade & 0x600000) >> 3;
					shadeShift = leftShade >> 23;
				}
				for (spanCount = rightX - leftX & 7; spanCount-- > 0;) {
					pixels[rowOffset++] = texture[(v & 0xfc0) + (u >> 6)] >>> shadeShift;
					u += uStep;
					v += vStep;
				}

				return;
			}
			while (spanCount-- > 0) {
				int texel;
				if ((texel = texture[(v & 0xfc0) + (u >> 6)] >>> shadeShift) != 0) {
					pixels[rowOffset] = texel;
				}
				rowOffset++;
				u += uStep;
				v += vStep;
				if ((texel = texture[(v & 0xfc0) + (u >> 6)] >>> shadeShift) != 0) {
					pixels[rowOffset] = texel;
				}
				rowOffset++;
				u += uStep;
				v += vStep;
				if ((texel = texture[(v & 0xfc0) + (u >> 6)] >>> shadeShift) != 0) {
					pixels[rowOffset] = texel;
				}
				rowOffset++;
				u += uStep;
				v += vStep;
				if ((texel = texture[(v & 0xfc0) + (u >> 6)] >>> shadeShift) != 0) {
					pixels[rowOffset] = texel;
				}
				rowOffset++;
				u += uStep;
				v += vStep;
				if ((texel = texture[(v & 0xfc0) + (u >> 6)] >>> shadeShift) != 0) {
					pixels[rowOffset] = texel;
				}
				rowOffset++;
				u += uStep;
				v += vStep;
				if ((texel = texture[(v & 0xfc0) + (u >> 6)] >>> shadeShift) != 0) {
					pixels[rowOffset] = texel;
				}
				rowOffset++;
				u += uStep;
				v += vStep;
				if ((texel = texture[(v & 0xfc0) + (u >> 6)] >>> shadeShift) != 0) {
					pixels[rowOffset] = texel;
				}
				rowOffset++;
				u += uStep;
				v += vStep;
				if ((texel = texture[(v & 0xfc0) + (u >> 6)] >>> shadeShift) != 0) {
					pixels[rowOffset] = texel;
				}
				rowOffset++;
				u = nextU;
				v = nextV;
				uNumerator += uXStep;
				vNumerator += vXStep;
				depthNumerator += depthXStep;
				int loopDivisor = depthNumerator >> 12;
				if (loopDivisor != 0) {
					nextU = uNumerator / loopDivisor;
					nextV = vNumerator / loopDivisor;
					if (nextU < 7) {
						nextU = 7;
					} else if (nextU > 4032) {
						nextU = 4032;
					}
				}
				uStep = nextU - u >> 3;
				vStep = nextV - v >> 3;
				leftShade += shadeStep;
				u += (leftShade & 0x600000) >> 3;
				shadeShift = leftShade >> 23;
			}
			for (spanCount = rightX - leftX & 7; spanCount-- > 0;) {
				int texel;
				if ((texel = texture[(v & 0xfc0) + (u >> 6)] >>> shadeShift) != 0) {
					pixels[rowOffset] = texel;
				}
				rowOffset++;
				u += uStep;
				v += vStep;
			}

			return;
		}
		int nextU = 0;
		int nextV = 0;
		int xFromCentre = leftX - textureInt1;
		uNumerator += (uXStep >> 3) * xFromCentre;
		vNumerator += (vXStep >> 3) * xFromCentre;
		depthNumerator += (depthXStep >> 3) * xFromCentre;
		int divisor = depthNumerator >> 14;
		if (divisor != 0) {
			u = uNumerator / divisor;
			v = vNumerator / divisor;
			if (u < 0) {
				u = 0;
			} else if (u > 16256) {
				u = 16256;
			}
		}
		uNumerator += uXStep;
		vNumerator += vXStep;
		depthNumerator += depthXStep;
		divisor = depthNumerator >> 14;
		if (divisor != 0) {
			nextU = uNumerator / divisor;
			nextV = vNumerator / divisor;
			if (nextU < 7) {
				nextU = 7;
			} else if (nextU > 16256) {
				nextU = 16256;
			}
		}
		int uStep = nextU - u >> 3;
		int vStep = nextV - v >> 3;
		u += leftShade & 0x600000;
		int shadeShift = leftShade >> 23;
		if (opaqueTexture) {
			while (spanCount-- > 0) {
				pixels[rowOffset++] = texture[(v & 0x3f80) + (u >> 7)] >>> shadeShift;
				u += uStep;
				v += vStep;
				pixels[rowOffset++] = texture[(v & 0x3f80) + (u >> 7)] >>> shadeShift;
				u += uStep;
				v += vStep;
				pixels[rowOffset++] = texture[(v & 0x3f80) + (u >> 7)] >>> shadeShift;
				u += uStep;
				v += vStep;
				pixels[rowOffset++] = texture[(v & 0x3f80) + (u >> 7)] >>> shadeShift;
				u += uStep;
				v += vStep;
				pixels[rowOffset++] = texture[(v & 0x3f80) + (u >> 7)] >>> shadeShift;
				u += uStep;
				v += vStep;
				pixels[rowOffset++] = texture[(v & 0x3f80) + (u >> 7)] >>> shadeShift;
				u += uStep;
				v += vStep;
				pixels[rowOffset++] = texture[(v & 0x3f80) + (u >> 7)] >>> shadeShift;
				u += uStep;
				v += vStep;
				pixels[rowOffset++] = texture[(v & 0x3f80) + (u >> 7)] >>> shadeShift;
				u = nextU;
				v = nextV;
				uNumerator += uXStep;
				vNumerator += vXStep;
				depthNumerator += depthXStep;
				int loopDivisor = depthNumerator >> 14;
				if (loopDivisor != 0) {
					nextU = uNumerator / loopDivisor;
					nextV = vNumerator / loopDivisor;
					if (nextU < 7) {
						nextU = 7;
					} else if (nextU > 16256) {
						nextU = 16256;
					}
				}
				uStep = nextU - u >> 3;
				vStep = nextV - v >> 3;
				leftShade += shadeStep;
				u += leftShade & 0x600000;
				shadeShift = leftShade >> 23;
			}
			for (spanCount = rightX - leftX & 7; spanCount-- > 0;) {
				pixels[rowOffset++] = texture[(v & 0x3f80) + (u >> 7)] >>> shadeShift;
				u += uStep;
				v += vStep;
			}

			return;
		}
		while (spanCount-- > 0) {
			int texel;
			if ((texel = texture[(v & 0x3f80) + (u >> 7)] >>> shadeShift) != 0) {
				pixels[rowOffset] = texel;
			}
			rowOffset++;
			u += uStep;
			v += vStep;
			if ((texel = texture[(v & 0x3f80) + (u >> 7)] >>> shadeShift) != 0) {
				pixels[rowOffset] = texel;
			}
			rowOffset++;
			u += uStep;
			v += vStep;
			if ((texel = texture[(v & 0x3f80) + (u >> 7)] >>> shadeShift) != 0) {
				pixels[rowOffset] = texel;
			}
			rowOffset++;
			u += uStep;
			v += vStep;
			if ((texel = texture[(v & 0x3f80) + (u >> 7)] >>> shadeShift) != 0) {
				pixels[rowOffset] = texel;
			}
			rowOffset++;
			u += uStep;
			v += vStep;
			if ((texel = texture[(v & 0x3f80) + (u >> 7)] >>> shadeShift) != 0) {
				pixels[rowOffset] = texel;
			}
			rowOffset++;
			u += uStep;
			v += vStep;
			if ((texel = texture[(v & 0x3f80) + (u >> 7)] >>> shadeShift) != 0) {
				pixels[rowOffset] = texel;
			}
			rowOffset++;
			u += uStep;
			v += vStep;
			if ((texel = texture[(v & 0x3f80) + (u >> 7)] >>> shadeShift) != 0) {
				pixels[rowOffset] = texel;
			}
			rowOffset++;
			u += uStep;
			v += vStep;
			if ((texel = texture[(v & 0x3f80) + (u >> 7)] >>> shadeShift) != 0) {
				pixels[rowOffset] = texel;
			}
			rowOffset++;
			u = nextU;
			v = nextV;
			uNumerator += uXStep;
			vNumerator += vXStep;
			depthNumerator += depthXStep;
			int loopDivisor = depthNumerator >> 14;
			if (loopDivisor != 0) {
				nextU = uNumerator / loopDivisor;
				nextV = vNumerator / loopDivisor;
				if (nextU < 7) {
					nextU = 7;
				} else if (nextU > 16256) {
					nextU = 16256;
				}
			}
			uStep = nextU - u >> 3;
			vStep = nextV - v >> 3;
			leftShade += shadeStep;
			u += leftShade & 0x600000;
			shadeShift = leftShade >> 23;
		}
		for (int remainder = rightX - leftX & 7; remainder-- > 0;) {
			int texel;
			if ((texel = texture[(v & 0x3f80) + (u >> 7)] >>> shadeShift) != 0) {
				pixels[rowOffset] = texel;
			}
			rowOffset++;
			u += uStep;
			v += vStep;
		}

	}

	public static final int anInt1459 = -477;
	public static boolean lowMem = true;
	static boolean clipScanlines;
	private static boolean opaqueTexture;
	public static boolean blockShading = true;
	public static int transparency;
	public static int textureInt1;
	public static int textureInt2;
	private static int[] reciprocalTable15;
	public static final int[] reciprocalTable16;
	public static int sineTable[];
	public static int cosineTable[];
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
		reciprocalTable15 = new int[2048];
		reciprocalTable16 = new int[2048];
		sineTable = new int[2048];
		cosineTable = new int[2048];
		for (int i = 1; i < 2048; i++) {
			reciprocalTable15[i] = 32768 / i;
		}

		for (int j = 1; j < 2048; j++) {
			reciprocalTable16[j] = 0x10000 / j;
		}

		for (int k = 0; k < 2048; k++) {
			sineTable[k] = (int) (65536D * Math.sin(k * 0.0030679614999999999D));
			cosineTable[k] = (int) (65536D * Math.cos(k * 0.0030679614999999999D));
		}

	}
}
