package core;

import net.Signlink;
import render.Background;

/**
 * Handles the flame background effect shown on the login screen.
 * Extracted from {@link Game}.
 */
final class FlamesEffect {
    private final Game game;

    FlamesEffect(Game game) {
        this.game = game;
    }

    void calcFlamesPosition() {
        char c = '\u0100';
        for (int j = 10; j < 117; j++) {
            int k = (int) (Math.random() * 100D);
            if (k < 50) {
                game.flameBuffer1[j + (c - 2 << 7)] = 255;
            }
        }
        for (int l = 0; l < 100; l++) {
            int i1 = (int) (Math.random() * 124D) + 2;
            int k1 = (int) (Math.random() * 128D) + 128;
            int k2 = i1 + (k1 << 7);
            game.flameBuffer1[k2] = 192;
        }

        for (int j1 = 1; j1 < c - 1; j1++) {
            for (int l1 = 1; l1 < 127; l1++) {
                int l2 = l1 + (j1 << 7);
                game.flameBuffer2[l2] = (game.flameBuffer1[l2 - 1] + game.flameBuffer1[l2 + 1] + game.flameBuffer1[l2 - 128] + game.flameBuffer1[l2 + 128]) / 4;
            }
        }

        game.flameOffset += 128;
        if (game.flameOffset > game.flameGradient1.length) {
            game.flameOffset -= game.flameGradient1.length;
            int i2 = (int) (Math.random() * 12D);
            randomizeBackground(game.runeBackgrounds[i2]);
        }
        for (int j2 = 1; j2 < c - 1; j2++) {
            for (int i3 = 1; i3 < 127; i3++) {
                int k3 = i3 + (j2 << 7);
                int i4 = game.flameBuffer2[k3 + 128] - game.flameGradient1[k3 + game.flameOffset & game.flameGradient1.length - 1] / 5;
                if (i4 < 0) {
                    i4 = 0;
                }
                game.flameBuffer1[k3] = i4;
            }
        }

        System.arraycopy(game.flameLineOffsets, 1, game.flameLineOffsets, 0, c - 1);

        game.flameLineOffsets[c - 1] = (int) (Math.sin((double) game.loopCycle / 14D) * 16D + Math.sin((double) game.loopCycle / 15D) * 14D + Math.sin((double) game.loopCycle / 16D) * 12D);
        if (game.flameMainColor > 0) {
            game.flameMainColor -= 4;
        }
        if (game.flameSecondaryColor > 0) {
            game.flameSecondaryColor -= 4;
        }
        if (game.flameMainColor == 0 && game.flameSecondaryColor == 0) {
            int l3 = (int) (Math.random() * 2000D);
            if (l3 == 0) {
                game.flameMainColor = 1024;
            }
            if (l3 == 1) {
                game.flameSecondaryColor = 1024;
            }
        }
    }

    boolean saveWave(byte[] data, int i) {
        return data == null || Signlink.wavesave(data, i);
    }

    void randomizeBackground(Background background) {
        int j = 256;
        for (int k = 0; k < game.flameGradient1.length; k++) {
            game.flameGradient1[k] = 0;
        }

        for (int l = 0; l < 5000; l++) {
            int i1 = (int) (Math.random() * 128D * (double) j);
            game.flameGradient1[i1] = (int) (Math.random() * 256D);
        }

        for (int j1 = 0; j1 < 20; j1++) {
            for (int k1 = 1; k1 < j - 1; k1++) {
                for (int i2 = 1; i2 < 127; i2++) {
                    int k2 = i2 + (k1 << 7);
                    game.flameGradient2[k2] = (game.flameGradient1[k2 - 1] + game.flameGradient1[k2 + 1] + game.flameGradient1[k2 - 128] + game.flameGradient1[k2 + 128]) / 4;
                }
            }

            int[] ai = game.flameGradient1;
            game.flameGradient1 = game.flameGradient2;
            game.flameGradient2 = ai;
        }

        if (background != null) {
            int l1 = 0;
            for (int j2 = 0; j2 < background.height; j2++) {
                for (int l2 = 0; l2 < background.width; l2++) {
                    if (background.pixels[l1++] != 0) {
                        int i3 = l2 + 16 + background.offsetX;
                        int j3 = j2 + 16 + background.offsetY;
                        int k3 = i3 + (j3 << 7);
                        game.flameGradient1[k3] = 0;
                    }
                }
            }
        }
    }

    void doFlamesDrawing() {
        char c = '\u0100';
        if (game.flameMainColor > 0) {
            for (int i = 0; i < 256; i++) {
                if (game.flameMainColor > 768) {
                    game.flameBuffer[i] = game.blendColors(game.flamePaletteRed[i], game.flamePaletteGreen[i], 1024 - game.flameMainColor);
                } else if (game.flameMainColor > 256) {
                    game.flameBuffer[i] = game.flamePaletteGreen[i];
                } else {
                    game.flameBuffer[i] = game.blendColors(game.flamePaletteGreen[i], game.flamePaletteRed[i], 256 - game.flameMainColor);
                }
            }
        } else if (game.flameSecondaryColor > 0) {
            for (int j = 0; j < 256; j++) {
                if (game.flameSecondaryColor > 768) {
                    game.flameBuffer[j] = game.blendColors(game.flamePaletteRed[j], game.flamePaletteBlue[j], 1024 - game.flameSecondaryColor);
                } else if (game.flameSecondaryColor > 256) {
                    game.flameBuffer[j] = game.flamePaletteBlue[j];
                } else {
                    game.flameBuffer[j] = game.blendColors(game.flamePaletteBlue[j], game.flamePaletteRed[j], 256 - game.flameSecondaryColor);
                }
            }
        } else {
            System.arraycopy(game.flamePaletteRed, 0, game.flameBuffer, 0, 256);
        }
        System.arraycopy(game.titleBackgroundLeft.pixels, 0, game.titleLeftProducer.pixels, 0, 33920);

        int i1 = 0;
        int j1 = 1152;
        for (int k1 = 1; k1 < c - 1; k1++) {
            int l1 = game.flameLineOffsets[k1] * (c - k1) / c;
            int j2 = 22 + l1;
            if (j2 < 0) {
                j2 = 0;
            }
            i1 += j2;
            for (int l2 = j2; l2 < 128; l2++) {
                int j3 = game.flameBuffer1[i1++];
                if (j3 != 0) {
                    int l3 = j3;
                    int j4 = 256 - j3;
                    j3 = game.flameBuffer[j3];
                    int l4 = game.titleLeftProducer.pixels[j1];
                    game.titleLeftProducer.pixels[j1++] = ((j3 & 0xff00ff) * l3 + (l4 & 0xff00ff) * j4 & 0xff00ff00) + ((j3 & 0xff00) * l3 + (l4 & 0xff00) * j4 & 0xff0000) >> 8;
                } else {
                    j1++;
                }
            }

            j1 += j2;
        }

        game.titleLeftProducer.drawGraphics(0, game.graphics, 0);
        System.arraycopy(game.titleBackgroundRight.pixels, 0, game.titleRightProducer.pixels, 0, 33920);

        i1 = 0;
        j1 = 1176;
        for (int k2 = 1; k2 < c - 1; k2++) {
            int i3 = game.flameLineOffsets[k2] * (c - k2) / c;
            int k3 = 103 - i3;
            j1 += i3;
            for (int i4 = 0; i4 < k3; i4++) {
                int k4 = game.flameBuffer1[i1++];
                if (k4 != 0) {
                    int i5 = k4;
                    int j5 = 256 - k4;
                    k4 = game.flameBuffer[k4];
                    int k5 = game.titleRightProducer.pixels[j1];
                    game.titleRightProducer.pixels[j1++] = ((k4 & 0xff00ff) * i5 + (k5 & 0xff00ff) * j5 & 0xff00ff00) + ((k4 & 0xff00) * i5 + (k5 & 0xff00) * j5 & 0xff0000) >> 8;
                } else {
                    j1++;
                }
            }

            i1 += 128 - k3;
            j1 += 128 - k3 - i3;
        }

        game.titleRightProducer.drawGraphics(0, game.graphics, 637);
    }

    void drawFlames() {
        game.drawingFlames = true;
        try {
            long l = System.currentTimeMillis();
            int i = 0;
            int j = 20;
            while (game.flameThreadActive) {
                game.flameDrawingCounter++;
                calcFlamesPosition();
                calcFlamesPosition();
                doFlamesDrawing();
                if (++i > 10) {
                    long l1 = System.currentTimeMillis();
                    int k = (int) (l1 - l) / 10 - j;
                    j = 40 - k;
                    if (j < 5) {
                        j = 5;
                    }
                    i = 0;
                    l = l1;
                }
                try {
                    Thread.sleep(j);
                } catch (Exception _ex) {
                    // ignored
                }
            }
        } catch (Exception _ex) {
            // ignored
        }
        game.drawingFlames = false;
    }
}
