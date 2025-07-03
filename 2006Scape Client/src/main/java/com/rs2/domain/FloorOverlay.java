package com.rs2.domain;

// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

import java.util.Calendar;
import java.util.GregorianCalendar;

public final class FloorOverlay {

	public static void unpackConfig(StreamLoader streamLoader) {
		Stream stream = new Stream(streamLoader.getFileData("flo.dat"));
		int cacheSize = stream.readUnsignedWord();
		if (cache == null) {
			cache = new FloorOverlay[cacheSize];
		}
		for (int j = 0; j < cacheSize; j++) {
			if (cache[j] == null) {
				cache[j] = new FloorOverlay();
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
			int i = stream.readUnsignedByte();
			if (i == 0) {
				return;
			} else if (i == 1) {
				rgbColor = stream.read3Bytes();
				Calendar date = new GregorianCalendar();
				if (ClientSettings.SNOW_FLOOR_FORCE_ENABLED || (ClientSettings.SNOW_FLOOR_ENABLED && (date.get(Calendar.MONTH) + 1) == Integer.parseInt(ClientSettings.SNOW_MONTH.substring(1)))) {
					rgbColor = 0xffffff;
				}
				updateHslFromRgb(rgbColor);
			} else if (i == 2) {
				textureId = stream.readUnsignedByte();
			} else if (i == 3) {
			} else if (i == 5) {
				isWalkable = false;
			} else if (i == 6) {
				stream.readString();
			} else if (i == 7) {
				int j = hue;
				int k = saturation;
				int l = lightness;
				int i1 = hslValue;
				int j1 = stream.read3Bytes();
				updateHslFromRgb(j1);
				hue = j;
				saturation = k;
				lightness = l;
				hslValue = i1;
				hslMultiplier = i1;
			} else {
				System.out.println("Error unrecognised config code: " + i);
			}
		} while (true);
	}

	private void updateHslFromRgb(int i) {
		double d = (i >> 16 & 0xff) / 256D;
		double d1 = (i >> 8 & 0xff) / 256D;
		double d2 = (i & 0xff) / 256D;
		double d3 = d;
		if (d1 < d3) {
			d3 = d1;
		}
		if (d2 < d3) {
			d3 = d2;
		}
		double d4 = d;
		if (d1 > d4) {
			d4 = d1;
		}
		if (d2 > d4) {
			d4 = d2;
		}
		double d5 = 0.0D;
		double d6 = 0.0D;
		double d7 = (d3 + d4) / 2D;
		if (d3 != d4) {
			if (d7 < 0.5D) {
				d6 = (d4 - d3) / (d4 + d3);
			}
			if (d7 >= 0.5D) {
				d6 = (d4 - d3) / (2D - d4 - d3);
			}
			if (d == d4) {
				d5 = (d1 - d2) / (d4 - d3);
			} else if (d1 == d4) {
				d5 = 2D + (d2 - d) / (d4 - d3);
			} else if (d2 == d4) {
				d5 = 4D + (d - d1) / (d4 - d3);
			}
		}
		d5 /= 6D;
		hue = (int) (d5 * 256D);
		saturation = (int) (d6 * 256D);
		lightness = (int) (d7 * 256D);
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
		if (d7 > 0.5D) {
			hslMultiplier = (int) ((1.0D - d7) * d6 * 512D);
		} else {
			hslMultiplier = (int) (d7 * d6 * 512D);
		}
		if (hslMultiplier < 1) {
			hslMultiplier = 1;
		}
		hslValue = (int) (d5 * hslMultiplier);
		int k = hue + (int) (Math.random() * 16D) - 8;
		if (k < 0) {
			k = 0;
		} else if (k > 255) {
			k = 255;
		}
		int l = saturation + (int) (Math.random() * 48D) - 24;
		if (l < 0) {
			l = 0;
		} else if (l > 255) {
			l = 255;
		}
		int i1 = lightness + (int) (Math.random() * 48D) - 24;
		if (i1 < 0) {
			i1 = 0;
		} else if (i1 > 255) {
			i1 = 255;
		}
		blendColor = hslToRgb(k, l, i1);
	}

	private int hslToRgb(int i, int j, int k) {
		if (k > 179) {
			j /= 2;
		}
		if (k > 192) {
			j /= 2;
		}
		if (k > 217) {
			j /= 2;
		}
		if (k > 243) {
			j /= 2;
		}
		return (i / 4 << 10) + (j / 32 << 7) + k / 2;
	}

	private FloorOverlay() {
		textureId = -1;
		isWalkable = true;
	}

	public static FloorOverlay cache[];
	public int rgbColor;
	public int textureId;
	public boolean isWalkable;
	public int hue;
	public int saturation;
	public int lightness;
	public int hslValue;
	public int hslMultiplier;
	public int blendColor;
	
}
