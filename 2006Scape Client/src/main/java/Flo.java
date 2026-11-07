// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

import java.util.Calendar;
import java.util.GregorianCalendar;

public final class Flo {

	public static void unpackConfig(StreamLoader streamLoader) {
		Stream stream = new Stream(streamLoader.getDataForName("flo.dat"));
		int cacheSize = stream.readUnsignedShortBigEndian();
		if (cache == null) {
			cache = new Flo[cacheSize];
		}
		for (int j = 0; j < cacheSize; j++) {
			if (cache[j] == null) {
				cache[j] = new Flo();
			}
			cache[j].readValues(stream);
		}
	}
	
	public static String getTodaysDate() {
		Calendar date = new GregorianCalendar();
		return date.get(Calendar.DAY_OF_MONTH) + "."+ (date.get(Calendar.MONTH) + 1) + "." + date.get(Calendar.YEAR);
	}


	private void readValues(Stream stream) {
		do {
			int opcode = stream.readUnsignedByte();
			if (opcode == 0) {
				return;
			} else if (opcode == 1) {
				rgb = stream.readUnsignedMediumBigEndian();
				Calendar date = new GregorianCalendar();
				if (ClientSettings.SNOW_FLOOR_FORCE_ENABLED || (ClientSettings.SNOW_FLOOR_ENABLED && (date.get(Calendar.MONTH) + 1) == Integer.parseInt(ClientSettings.SNOW_MONTH.substring(1)))) {
					rgb = 0xffffff;
				}
				setHslFromRgb(rgb);
			} else if (opcode == 2) {
				textureId = stream.readUnsignedByte();
			} else if (opcode == 3) {
			} else if (opcode == 5) {
				occludes = false;
			} else if (opcode == 6) {
				stream.readLfTerminatedString();
			} else if (opcode == 7) {
				int savedHue = hue;
				int savedSaturation = saturation;
				int savedLightness = lightness;
				int savedWeightedHue = weightedHue;
				int secondaryRgb = stream.readUnsignedMediumBigEndian();
				setHslFromRgb(secondaryRgb);
				hue = savedHue;
				saturation = savedSaturation;
				lightness = savedLightness;
				weightedHue = savedWeightedHue;
				hueWeight = savedWeightedHue;
			} else {
				System.out.println("Error unrecognised config code: " + opcode);
			}
		} while (true);
	}

	private void setHslFromRgb(int rgbColour) {
		double red = (rgbColour >> 16 & 0xff) / 256D;
		double green = (rgbColour >> 8 & 0xff) / 256D;
		double blue = (rgbColour & 0xff) / 256D;
		double minChannel = red;
		if (green < minChannel) {
			minChannel = green;
		}
		if (blue < minChannel) {
			minChannel = blue;
		}
		double maxChannel = red;
		if (green > maxChannel) {
			maxChannel = green;
		}
		if (blue > maxChannel) {
			maxChannel = blue;
		}
		double hueFraction = 0.0D;
		double saturationFraction = 0.0D;
		double lightnessFraction = (minChannel + maxChannel) / 2D;
		if (minChannel != maxChannel) {
			if (lightnessFraction < 0.5D) {
				saturationFraction = (maxChannel - minChannel) / (maxChannel + minChannel);
			}
			if (lightnessFraction >= 0.5D) {
				saturationFraction = (maxChannel - minChannel) / (2D - maxChannel - minChannel);
			}
			if (red == maxChannel) {
				hueFraction = (green - blue) / (maxChannel - minChannel);
			} else if (green == maxChannel) {
				hueFraction = 2D + (blue - red) / (maxChannel - minChannel);
			} else if (blue == maxChannel) {
				hueFraction = 4D + (red - green) / (maxChannel - minChannel);
			}
		}
		hueFraction /= 6D;
		hue = (int) (hueFraction * 256D);
		saturation = (int) (saturationFraction * 256D);
		lightness = (int) (lightnessFraction * 256D);
		if (saturation < 0) {
			saturation = 0;
		} else if (saturation > 255) {
			saturation = 255;
		}
		if (lightness < 0) {
			lightness = 0;
		} else if (lightness > 255) {
			lightness = 255;
		}
		if (lightnessFraction > 0.5D) {
			hueWeight = (int) ((1.0D - lightnessFraction) * saturationFraction * 512D);
		} else {
			hueWeight = (int) (lightnessFraction * saturationFraction * 512D);
		}
		if (hueWeight < 1) {
			hueWeight = 1;
		}
		weightedHue = (int) (hueFraction * hueWeight);
		int jitteredHue = hue + (int) (Math.random() * 16D) - 8;
		if (jitteredHue < 0) {
			jitteredHue = 0;
		} else if (jitteredHue > 255) {
			jitteredHue = 255;
		}
		int jitteredSaturation = saturation + (int) (Math.random() * 48D) - 24;
		if (jitteredSaturation < 0) {
			jitteredSaturation = 0;
		} else if (jitteredSaturation > 255) {
			jitteredSaturation = 255;
		}
		int jitteredLightness = lightness + (int) (Math.random() * 48D) - 24;
		if (jitteredLightness < 0) {
			jitteredLightness = 0;
		} else if (jitteredLightness > 255) {
			jitteredLightness = 255;
		}
		minimapHsl = packHsl(jitteredHue, jitteredSaturation, jitteredLightness);
	}

	private int packHsl(int hue, int saturation, int lightness) {
		if (lightness > 179) {
			saturation /= 2;
		}
		if (lightness > 192) {
			saturation /= 2;
		}
		if (lightness > 217) {
			saturation /= 2;
		}
		if (lightness > 243) {
			saturation /= 2;
		}
		return (hue / 4 << 10) + (saturation / 32 << 7) + lightness / 2;
	}

	private Flo() {
		textureId = -1;
		occludes = true;
	}

	public static Flo cache[];
	public int rgb;
	public int textureId;
	public boolean occludes;
	public int hue;
	public int saturation;
	public int lightness;
	public int weightedHue;
	public int hueWeight;
	public int minimapHsl;
	
}
