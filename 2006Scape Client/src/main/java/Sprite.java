// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

import java.awt.Component;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.PixelGrabber;

public final class Sprite extends DrawingArea {

        public Sprite(int i, int j) {
                pixels = new int[i * j];
                width = trimWidth = i;
                height = trimHeight = j;
                offsetX = offsetY = 0;
        }

	public Sprite(byte abyte0[], Component component) {
		try {
			// Image image =
			// Toolkit.getDefaultToolkit().getImage(signlink.findcachedir()+"mopar.jpg");
			Image image = Toolkit.getDefaultToolkit().createImage(abyte0);
			MediaTracker mediatracker = new MediaTracker(component);
			mediatracker.addImage(image, 0);
			mediatracker.waitForAll();
			width = image.getWidth(component);
			height = image.getHeight(component);
			trimWidth = width;
			trimHeight = height;
                        offsetX = 0;
                        offsetY = 0;
			pixels = new int[width * height];
			PixelGrabber pixelgrabber = new PixelGrabber(image, 0, 0, width, height, pixels, 0, width);
			pixelgrabber.grabPixels();
		} catch (Exception _ex) {
			System.out.println("Error converting jpg");
		}
	}

	public Sprite(StreamLoader streamLoader, String s, int i) {
		Stream stream = new Stream(streamLoader.getFileData(s + ".dat"));
		Stream stream_1 = new Stream(streamLoader.getFileData("index.dat"));
		stream_1.currentOffset = stream.readUnsignedWord();
		trimWidth = stream_1.readUnsignedWord();
		trimHeight = stream_1.readUnsignedWord();
		int j = stream_1.readUnsignedByte();
		int ai[] = new int[j];
		for (int k = 0; k < j - 1; k++) {
			ai[k + 1] = stream_1.read3Bytes();
			if (ai[k + 1] == 0) {
				ai[k + 1] = 1;
			}
		}

		for (int l = 0; l < i; l++) {
			stream_1.currentOffset += 2;
			stream.currentOffset += stream_1.readUnsignedWord() * stream_1.readUnsignedWord();
			stream_1.currentOffset++;
		}

                offsetX = stream_1.readUnsignedByte();
                offsetY = stream_1.readUnsignedByte();
		width = stream_1.readUnsignedWord();
		height = stream_1.readUnsignedWord();
		int i1 = stream_1.readUnsignedByte();
		int j1 = width * height;
		pixels = new int[j1];
		if (i1 == 0) {
			for (int k1 = 0; k1 < j1; k1++) {
				pixels[k1] = ai[stream.readUnsignedByte()];
			}

			return;
		}
		if (i1 == 1) {
			for (int l1 = 0; l1 < width; l1++) {
				for (int i2 = 0; i2 < height; i2++) {
					pixels[l1 + i2 * width] = ai[stream.readUnsignedByte()];
				}

			}

		}
	}

    public void initializeDrawingArea() {
            DrawingArea.initDrawingArea(height, width, pixels);
    }

    public void adjustRgb(int redOffset, int greenOffset, int blueOffset) {
        for (int i1 = 0; i1 < pixels.length; i1++) {
                int j1 = pixels[i1];
                if (j1 != 0) {
                        int k1 = j1 >> 16 & 0xff;
                        k1 += redOffset;
                        if (k1 < 1) {
                                k1 = 1;
                        } else if (k1 > 255) {
                                k1 = 255;
                        }
                        int l1 = j1 >> 8 & 0xff;
                        l1 += greenOffset;
                        if (l1 < 1) {
                                l1 = 1;
                        } else if (l1 > 255) {
                                l1 = 255;
                        }
                        int i2 = j1 & 0xff;
                        i2 += blueOffset;
                        if (i2 < 1) {
                                i2 = 1;
                        } else if (i2 > 255) {
                                i2 = 255;
                        }
				pixels[i1] = (k1 << 16) + (l1 << 8) + i2;
			}
		}

	}

    public void crop() {
                /*int ai[] = new int[trimWidth * trimHeight];
                for (int j = 0; j < height; j++) {
                        System.arraycopy(pixels, j * width, ai, j + offsetY * trimWidth + offsetX, width);
                }

                pixels = ai;
                width = trimWidth;
                height = trimHeight;
                offsetX = 0;
                offsetY = 0;*/
		int ai[] = new int[trimWidth * trimHeight];
		for (int j = 0; j < height; j++) {
                        for (int k = 0; k < width; k++)
                                ai[(j + offsetY) * trimWidth + (k + offsetX)] = pixels[j
                                                * width + k];
		}
		pixels = ai;
		width = trimWidth;
		height = trimHeight;
                offsetX = 0;
                offsetY = 0;
        }

        public void drawSprite(int x, int y) {
                x += offsetX;
                y += offsetY;
                int l = x + y * DrawingArea.width;
		int i1 = 0;
		int j1 = height;
		int k1 = width;
		int l1 = DrawingArea.width - k1;
		int i2 = 0;
                if (y < DrawingArea.topY) {
                        int j2 = DrawingArea.topY - y;
			j1 -= j2;
                        y = DrawingArea.topY;
			i1 += j2 * k1;
			l += j2 * DrawingArea.width;
		}
                if (y + j1 > DrawingArea.bottomY) {
                        j1 -= y + j1 - DrawingArea.bottomY;
		}
                if (x < DrawingArea.topX) {
                        int k2 = DrawingArea.topX - x;
			k1 -= k2;
                        x = DrawingArea.topX;
			i1 += k2;
			l += k2;
			i2 += k2;
			l1 += k2;
		}
                if (x + k1 > DrawingArea.bottomX) {
                        int l2 = x + k1 - DrawingArea.bottomX;
			k1 -= l2;
			i2 += l2;
			l1 += l2;
		}
		if (k1 <= 0 || j1 <= 0) {
		} else {
                        copyToCanvas(l, k1, j1, i2, i1, l1, pixels, DrawingArea.pixels);
		}
	}

        private void copyToCanvas(int i, int j, int k, int l, int i1, int k1, int ai[], int ai1[]) {
		int l1 = -(j >> 2);
		j = -(j & 3);
		for (int i2 = -k; i2 < 0; i2++) {
			for (int j2 = l1; j2 < 0; j2++) {
				ai1[i++] = ai[i1++];
				ai1[i++] = ai[i1++];
				ai1[i++] = ai[i1++];
				ai1[i++] = ai[i1++];
			}

			for (int k2 = j; k2 < 0; k2++) {
				ai1[i++] = ai[i1++];
			}

			i += k1;
			i1 += l;
		}
	}

        public void drawSprite1(int i, int j) {
                int k = 128;// was parameter
                i += offsetX;
                j += offsetY;
		int i1 = i + j * DrawingArea.width;
		int j1 = 0;
		int k1 = height;
		int l1 = width;
		int i2 = DrawingArea.width - l1;
		int j2 = 0;
		if (j < DrawingArea.topY) {
			int k2 = DrawingArea.topY - j;
			k1 -= k2;
			j = DrawingArea.topY;
			j1 += k2 * l1;
			i1 += k2 * DrawingArea.width;
		}
		if (j + k1 > DrawingArea.bottomY) {
			k1 -= j + k1 - DrawingArea.bottomY;
		}
		if (i < DrawingArea.topX) {
			int l2 = DrawingArea.topX - i;
			l1 -= l2;
			i = DrawingArea.topX;
			j1 += l2;
			i1 += l2;
			j2 += l2;
			i2 += l2;
		}
		if (i + l1 > DrawingArea.bottomX) {
			int i3 = i + l1 - DrawingArea.bottomX;
			l1 -= i3;
			j2 += i3;
			i2 += i3;
		}
		if (!(l1 <= 0 || k1 <= 0)) {
                        blendPixels(j1, l1, DrawingArea.pixels, pixels, j2, k1, i2, k, i1);
		}
	}

        public void drawTransparentSprite(int i, int k) {
                i += offsetX;
                k += offsetY;
                int l = i + k * DrawingArea.width;
		int i1 = 0;
		int j1 = height;
		int k1 = width;
		int l1 = DrawingArea.width - k1;
		int i2 = 0;
		if (k < DrawingArea.topY) {
			int j2 = DrawingArea.topY - k;
			j1 -= j2;
			k = DrawingArea.topY;
			i1 += j2 * k1;
			l += j2 * DrawingArea.width;
		}
		if (k + j1 > DrawingArea.bottomY) {
			j1 -= k + j1 - DrawingArea.bottomY;
		}
		if (i < DrawingArea.topX) {
			int k2 = DrawingArea.topX - i;
			k1 -= k2;
			i = DrawingArea.topX;
			i1 += k2;
			l += k2;
			i2 += k2;
			l1 += k2;
		}
		if (i + k1 > DrawingArea.bottomX) {
			int l2 = i + k1 - DrawingArea.bottomX;
			k1 -= l2;
			i2 += l2;
			l1 += l2;
		}
                if (!(k1 <= 0 || j1 <= 0)) {
                        drawTransparent(DrawingArea.pixels, pixels, i1, l, k1, j1, l1, i2);
                }
        }

        private void drawTransparent(int ai[], int ai1[], int j, int k, int l, int i1, int j1, int k1) {
		int i;// was parameter
		int l1 = -(l >> 2);
		l = -(l & 3);
		for (int i2 = -i1; i2 < 0; i2++) {
			for (int j2 = l1; j2 < 0; j2++) {
				i = ai1[j++];
				if (i != 0) {
					ai[k++] = i;
				} else {
					k++;
				}
				i = ai1[j++];
				if (i != 0) {
					ai[k++] = i;
				} else {
					k++;
				}
				i = ai1[j++];
				if (i != 0) {
					ai[k++] = i;
				} else {
					k++;
				}
				i = ai1[j++];
				if (i != 0) {
					ai[k++] = i;
				} else {
					k++;
				}
			}

			for (int k2 = l; k2 < 0; k2++) {
				i = ai1[j++];
				if (i != 0) {
					ai[k++] = i;
				} else {
					k++;
				}
			}

			k += j1;
			j += k1;
		}

	}

        private void blendPixels(int i, int j, int ai[], int ai1[], int l, int i1, int j1, int k1, int l1) {
		int k;// was parameter
		int j2 = 256 - k1;
		for (int k2 = -i1; k2 < 0; k2++) {
			for (int l2 = -j; l2 < 0; l2++) {
				k = ai1[i++];
				if (k != 0) {
					int i3 = ai[l1];
					ai[l1++] = ((k & 0xff00ff) * k1 + (i3 & 0xff00ff) * j2 & 0xff00ff00) + ((k & 0xff00) * k1 + (i3 & 0xff00) * j2 & 0xff0000) >> 8;
				} else {
					l1++;
				}
			}

			l1 += j1;
			i += l;
		}
	}

	public void drawTransformed(int i, int j, int ai[], int k, int ai1[], int i1, int j1, int k1, int l1, int i2) {
		try {
			int j2 = -l1 / 2;
			int k2 = -i / 2;
			int l2 = (int) (Math.sin(j / 326.11000000000001D) * 65536D);
			int i3 = (int) (Math.cos(j / 326.11000000000001D) * 65536D);
			l2 = l2 * k >> 8;
			i3 = i3 * k >> 8;
			int j3 = (i2 << 16) + k2 * l2 + j2 * i3;
			int k3 = (i1 << 16) + k2 * i3 - j2 * l2;
			int l3 = k1 + j1 * DrawingArea.width;
			for (j1 = 0; j1 < i; j1++) {
				int i4 = ai1[j1];
				int j4 = l3 + i4;
				int k4 = j3 + i3 * i4;
				int l4 = k3 - l2 * i4;
				for (k1 = -ai[j1]; k1 < 0; k1++) {
					if (ClientSettings.BILINEAR_MINIMAP_FILTERING) {
						int x1 = k4 >> 16;
						int y1 = l4 >> 16;
						int x2 = x1 + 1;
						int y2 = y1 + 1;
						int sampleColor1 = pixels[x1 + y1 * width];
						int sampleColor2 = pixels[x2 + y1 * width];
						int sampleColor3 = pixels[x1 + y2 * width];
						int sampleColor4 = pixels[x2 + y2 * width];
						int x1Distance = (k4 >> 8) - (x1 << 8);
						int y1Distance = (l4 >> 8) - (y1 << 8);
						int x2Distance = (x2 << 8) - (k4 >> 8);
						int y2Distance = (y2 << 8) - (l4 >> 8);
						int sampleAlpha1 = x2Distance * y2Distance;
						int sampleAlpha2 = x1Distance * y2Distance;
						int sampleAlpha3 = x2Distance * y1Distance;
						int sampleAlpha4 = x1Distance * y1Distance;
						int red = (sampleColor1 >> 16 & 0xff) * sampleAlpha1 + (sampleColor2 >> 16 & 0xff) * sampleAlpha2 + (sampleColor3 >> 16 & 0xff) * sampleAlpha3 + (sampleColor4 >> 16 & 0xff) * sampleAlpha4 & 0xff0000;
						int green = (sampleColor1 >> 8 & 0xff) * sampleAlpha1 + (sampleColor2 >> 8 & 0xff) * sampleAlpha2 + (sampleColor3 >> 8 & 0xff) * sampleAlpha3 + (sampleColor4 >> 8 & 0xff) * sampleAlpha4 >> 8 & 0xff00;
						int blue = (sampleColor1 & 0xff) * sampleAlpha1 + (sampleColor2 & 0xff) * sampleAlpha2 + (sampleColor3 & 0xff) * sampleAlpha3 + (sampleColor4 & 0xff) * sampleAlpha4 >> 16;
						DrawingArea.pixels[j4++] = red | green | blue;
					} else {
						DrawingArea.pixels[j4++] = pixels[(k4 >> 16) + (l4 >> 16) * width];
					}
					k4 += i3;
					l4 -= l2;
				}

				j3 += l2;
				k3 += i3;
				l3 += DrawingArea.width;
			}

		} catch (Exception _ex) {
		}
	}

	public void drawRotated(int i, double d, int l1) {
		// all of the following were parameters
		int j = 15;
		int k = 20;
		int l = 15;
		int j1 = 256;
		int k1 = 20;
		// all of the previous were parameters
		try {
			int i2 = -k / 2;
			int j2 = -k1 / 2;
			int k2 = (int) (Math.sin(d) * 65536D);
			int l2 = (int) (Math.cos(d) * 65536D);
			k2 = k2 * j1 >> 8;
			l2 = l2 * j1 >> 8;
			int i3 = (l << 16) + j2 * k2 + i2 * l2;
			int j3 = (j << 16) + j2 * l2 - i2 * k2;
			int k3 = l1 + i * DrawingArea.width;
			for (i = 0; i < k1; i++) {
				int l3 = k3;
				int i4 = i3;
				int j4 = j3;
				for (l1 = -k; l1 < 0; l1++) {
					int k4 = pixels[(i4 >> 16) + (j4 >> 16) * width];
					if (k4 != 0) {
						DrawingArea.pixels[l3++] = k4;
					} else {
						l3++;
					}
					i4 += l2;
					j4 -= k2;
				}

				i3 += k2;
				j3 += l2;
				k3 += DrawingArea.width;
			}

		} catch (Exception _ex) {
		}
	}

        public void drawWithMask(Background background, int i, int j) {
                j += offsetX;
                i += offsetY;
		int k = j + i * DrawingArea.width;
		int l = 0;
		int i1 = height;
		int j1 = width;
		int k1 = DrawingArea.width - j1;
		int l1 = 0;
		if (i < DrawingArea.topY) {
			int i2 = DrawingArea.topY - i;
			i1 -= i2;
			i = DrawingArea.topY;
			l += i2 * j1;
			k += i2 * DrawingArea.width;
		}
		if (i + i1 > DrawingArea.bottomY) {
			i1 -= i + i1 - DrawingArea.bottomY;
		}
		if (j < DrawingArea.topX) {
			int j2 = DrawingArea.topX - j;
			j1 -= j2;
			j = DrawingArea.topX;
			l += j2;
			k += j2;
			l1 += j2;
			k1 += j2;
		}
		if (j + j1 > DrawingArea.bottomX) {
			int k2 = j + j1 - DrawingArea.bottomX;
			j1 -= k2;
			l1 += k2;
			k1 += k2;
		}
		if (!(j1 <= 0 || i1 <= 0)) {
                        copyMasked(pixels, j1, background.pixels, i1, DrawingArea.pixels, 0, k1, k, l1, l);
		}
	}

        private void copyMasked(int ai[], int i, byte abyte0[], int j, int ai1[], int k, int l, int i1, int j1, int k1) {
		int l1 = -(i >> 2);
		i = -(i & 3);
		for (int j2 = -j; j2 < 0; j2++) {
			for (int k2 = l1; k2 < 0; k2++) {
				k = ai[k1++];
				if (k != 0 && abyte0[i1] == 0) {
					ai1[i1++] = k;
				} else {
					i1++;
				}
				k = ai[k1++];
				if (k != 0 && abyte0[i1] == 0) {
					ai1[i1++] = k;
				} else {
					i1++;
				}
				k = ai[k1++];
				if (k != 0 && abyte0[i1] == 0) {
					ai1[i1++] = k;
				} else {
					i1++;
				}
				k = ai[k1++];
				if (k != 0 && abyte0[i1] == 0) {
					ai1[i1++] = k;
				} else {
					i1++;
				}
			}

			for (int l2 = i; l2 < 0; l2++) {
				k = ai[k1++];
				if (k != 0 && abyte0[i1] == 0) {
					ai1[i1++] = k;
				} else {
					i1++;
				}
			}

			i1 += l;
			k1 += j1;
		}

	}

	public int pixels[];
	public int width;
	public int height;
        private int offsetX;
        private int offsetY;
	public int trimWidth;
	public int trimHeight;
}
