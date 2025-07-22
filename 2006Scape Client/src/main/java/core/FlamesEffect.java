package core;

import net.Signlink;
import render.Background;

/**
 * Handles the flame background effect shown on the login screen.
 * Extracted from {@link Game}.
 *
 * NOTE: Only names/constants added/renamed. **No logic has been changed.**
 */
final class FlamesEffect {
    private static final char GRID_SIZE_256 = '\u0100';          // 256, stored as char as in original
    private static final int WIDTH_128 = 128;                      // width of flame buffers
    private static final int HEIGHT_256 = 256;                     // height of flame buffers
    private static final int RANDOM_SPARK_START_ROW = 10;          // first row to seed sparks
    private static final int RANDOM_SPARK_END_ROW = 117;           // last row exclusive
    private static final int SPARK_CHANCE_PERCENT = 50;            // threshold for random spark

    private static final int RANDOM_DOT_COUNT = 5000;              // dots for randomizeBackground
    private static final int RANDOM_BG_ITERATIONS = 20;            // smoothing iterations

    private static final int COLOR_MAX = 256;                      // palette size
    private static final int COLOR_BLEND_MAX = 1024;               // blend scale used in Game.blendColors

    private static final int IMAGE_COPY_COUNT = 33920;             // bytes copied for title backgrounds
    private static final int LEFT_BG_START = 1152;                 // start index into left producer pixels
    private static final int RIGHT_BG_START = 1176;                // start index into right producer pixels
    private static final int FLAME_LEFT_OFFSET = 22;               // X offset used while drawing left flames
    private static final int RIGHT_FLAME_BASELINE = 103;           // 103 - i3 in right drawing loop
    private static final int DRAW_RIGHT_X = 637;                   // x draw offset for right image

    private static final int FLAME_OFFSET_INCREMENT = 128;         // scroll amount per tick
    private static final int GRADIENT_DIVISOR = 5;                 // divide gradient value by 5

    private static final int MAIN_COLOR_DECREMENT = 4;             // fade speeds
    private static final int SECONDARY_COLOR_DECREMENT = 4;

    private static final int RANDOM_COLOR_TRIGGER_RANGE = 2000;    // random trigger range for colors
    private static final int RANDOM_COLOR_VALUE = 1024;            // value assigned when triggered

    private static final int DRAW_LOOP_MAX_ITER = 10;              // i > 10 check in drawFlames loop
    private static final int FRAME_TARGET_MS = 40;                  // 40 - k target frame time slice
    private static final int MIN_SLEEP_MS = 5;                      // minimum sleep duration
    private static final int INITIAL_SLEEP_MS = 20;                 // initial sleep value

    private final Game game;

    FlamesEffect(Game game) {
        this.game = game;
    }

    void calcFlamesPosition() {
        char grid = GRID_SIZE_256; // keep original char usage

        for (int row = RANDOM_SPARK_START_ROW; row < RANDOM_SPARK_END_ROW; row++) {
            int rand = (int) (Math.random() * 100D);
            if (rand < SPARK_CHANCE_PERCENT) {
                game.flameBuffer1[row + (grid - 2 << 7)] = 255;
            }
        }

        for (int n = 0; n < 100; n++) {
            int randX = (int) (Math.random() * 124D) + 2;
            int randY = (int) (Math.random() * 128D) + 128;
            int idx = randX + (randY << 7);
            game.flameBuffer1[idx] = 192;
        }

        for (int y = 1; y < grid - 1; y++) {
            for (int x = 1; x < 127; x++) {
                int pos = x + (y << 7);
                game.flameBuffer2[pos] = (game.flameBuffer1[pos - 1]
                        + game.flameBuffer1[pos + 1]
                        + game.flameBuffer1[pos - WIDTH_128]
                        + game.flameBuffer1[pos + WIDTH_128]) / 4;
            }
        }

        game.flameOffset += FLAME_OFFSET_INCREMENT;
        if (game.flameOffset > game.flameGradient1.length) {
            game.flameOffset -= game.flameGradient1.length;
            int bgIndex = (int) (Math.random() * 12D);
            randomizeBackground(game.runeBackgrounds[bgIndex]);
        }

        for (int y = 1; y < grid - 1; y++) {
            for (int x = 1; x < 127; x++) {
                int pos = x + (y << 7);
                int value = game.flameBuffer2[pos + WIDTH_128]
                        - game.flameGradient1[pos + game.flameOffset & game.flameGradient1.length - 1] / GRADIENT_DIVISOR;
                if (value < 0) {
                    value = 0;
                }
                game.flameBuffer1[pos] = value;
            }
        }

        // shift line offsets up by one
        System.arraycopy(game.flameLineOffsets, 1, game.flameLineOffsets, 0, grid - 1);

        game.flameLineOffsets[grid - 1] = (int) (Math.sin((double) game.loopCycle / 14D) * 16D
                + Math.sin((double) game.loopCycle / 15D) * 14D
                + Math.sin((double) game.loopCycle / 16D) * 12D);

        if (game.flameMainColor > 0) {
            game.flameMainColor -= MAIN_COLOR_DECREMENT;
        }
        if (game.flameSecondaryColor > 0) {
            game.flameSecondaryColor -= SECONDARY_COLOR_DECREMENT;
        }
        if (game.flameMainColor == 0 && game.flameSecondaryColor == 0) {
            int rand = (int) (Math.random() * RANDOM_COLOR_TRIGGER_RANGE);
            if (rand == 0) {
                game.flameMainColor = RANDOM_COLOR_VALUE;
            }
            if (rand == 1) {
                game.flameSecondaryColor = RANDOM_COLOR_VALUE;
            }
        }
    }

    boolean saveWave(byte[] data, int index) {
        return data == null || Signlink.wavesave(data, index);
    }

    void randomizeBackground(Background background) {
        int height = HEIGHT_256; // 256
        for (int i = 0; i < game.flameGradient1.length; i++) {
            game.flameGradient1[i] = 0;
        }

        for (int i = 0; i < RANDOM_DOT_COUNT; i++) {
            int randIdx = (int) (Math.random() * WIDTH_128 * (double) height);
            game.flameGradient1[randIdx] = (int) (Math.random() * COLOR_MAX);
        }

        for (int iter = 0; iter < RANDOM_BG_ITERATIONS; iter++) {
            for (int y = 1; y < height - 1; y++) {
                for (int x = 1; x < 127; x++) {
                    int pos = x + (y << 7);
                    game.flameGradient2[pos] = (game.flameGradient1[pos - 1]
                            + game.flameGradient1[pos + 1]
                            + game.flameGradient1[pos - WIDTH_128]
                            + game.flameGradient1[pos + WIDTH_128]) / 4;
                }
            }

            int[] swap = game.flameGradient1;
            game.flameGradient1 = game.flameGradient2;
            game.flameGradient2 = swap;
        }

        if (background != null) {
            int p = 0;
            for (int y = 0; y < background.height; y++) {
                for (int x = 0; x < background.width; x++) {
                    if (background.pixels[p++] != 0) {
                        int drawX = x + 16 + background.offsetX;
                        int drawY = y + 16 + background.offsetY;
                        int pos = drawX + (drawY << 7);
                        game.flameGradient1[pos] = 0;
                    }
                }
            }
        }
    }

    void doFlamesDrawing() {
        char grid = GRID_SIZE_256;
        if (game.flameMainColor > 0) {
            for (int i = 0; i < COLOR_MAX; i++) {
                if (game.flameMainColor > 768) {
                    game.flameBuffer[i] = game.blendColors(game.flamePaletteRed[i], game.flamePaletteGreen[i], COLOR_BLEND_MAX - game.flameMainColor);
                } else if (game.flameMainColor > 256) {
                    game.flameBuffer[i] = game.flamePaletteGreen[i];
                } else {
                    game.flameBuffer[i] = game.blendColors(game.flamePaletteGreen[i], game.flamePaletteRed[i], 256 - game.flameMainColor);
                }
            }
        } else if (game.flameSecondaryColor > 0) {
            for (int i = 0; i < COLOR_MAX; i++) {
                if (game.flameSecondaryColor > 768) {
                    game.flameBuffer[i] = game.blendColors(game.flamePaletteRed[i], game.flamePaletteBlue[i], COLOR_BLEND_MAX - game.flameSecondaryColor);
                } else if (game.flameSecondaryColor > 256) {
                    game.flameBuffer[i] = game.flamePaletteBlue[i];
                } else {
                    game.flameBuffer[i] = game.blendColors(game.flamePaletteBlue[i], game.flamePaletteRed[i], 256 - game.flameSecondaryColor);
                }
            }
        } else {
            System.arraycopy(game.flamePaletteRed, 0, game.flameBuffer, 0, COLOR_MAX);
        }

        System.arraycopy(game.titleBackgroundLeft.pixels, 0, game.titleLeftProducer.pixels, 0, IMAGE_COPY_COUNT);

        int srcIdx = 0;
        int dstIdx = LEFT_BG_START;
        for (int y = 1; y < grid - 1; y++) {
            int lineOffset = game.flameLineOffsets[y] * (grid - y) / grid;
            int startX = FLAME_LEFT_OFFSET + lineOffset;
            if (startX < 0) {
                startX = 0;
            }
            srcIdx += startX;
            for (int x = startX; x < WIDTH_128; x++) {
                int intensity = game.flameBuffer1[srcIdx++];
                if (intensity != 0) {
                    int alpha = intensity;
                    int invAlpha = 256 - intensity;
                    int color = game.flameBuffer[intensity];
                    int bg = game.titleLeftProducer.pixels[dstIdx];
                    game.titleLeftProducer.pixels[dstIdx++] = ((color & 0xff00ff) * alpha + (bg & 0xff00ff) * invAlpha & 0xff00ff00)
                            + ((color & 0xff00) * alpha + (bg & 0xff00) * invAlpha & 0xff0000) >> 8;
                } else {
                    dstIdx++;
                }
            }

            dstIdx += startX;
        }

        game.titleLeftProducer.drawGraphics(0, game.graphics, 0);
        System.arraycopy(game.titleBackgroundRight.pixels, 0, game.titleRightProducer.pixels, 0, IMAGE_COPY_COUNT);

        srcIdx = 0;
        dstIdx = RIGHT_BG_START;
        for (int y = 1; y < grid - 1; y++) {
            int lineOffset = game.flameLineOffsets[y] * (grid - y) / grid;
            int width = RIGHT_FLAME_BASELINE - lineOffset;
            dstIdx += lineOffset;
            for (int x = 0; x < width; x++) {
                int intensity = game.flameBuffer1[srcIdx++];
                if (intensity != 0) {
                    int alpha = intensity;
                    int invAlpha = 256 - intensity;
                    int color = game.flameBuffer[intensity];
                    int bg = game.titleRightProducer.pixels[dstIdx];
                    game.titleRightProducer.pixels[dstIdx++] = ((color & 0xff00ff) * alpha + (bg & 0xff00ff) * invAlpha & 0xff00ff00)
                            + ((color & 0xff00) * alpha + (bg & 0xff00) * invAlpha & 0xff0000) >> 8;
                } else {
                    dstIdx++;
                }
            }

            srcIdx += WIDTH_128 - width;
            dstIdx += WIDTH_128 - width - lineOffset;
        }

        game.titleRightProducer.drawGraphics(0, game.graphics, DRAW_RIGHT_X);
    }

    void drawFlames() {
        game.drawingFlames = true;
        try {
            long lastTime = System.currentTimeMillis();
            int iterCounter = 0;
            int sleepTime = INITIAL_SLEEP_MS;
            while (game.flameThreadActive) {
                game.flameDrawingCounter++;
                calcFlamesPosition();
                calcFlamesPosition();
                doFlamesDrawing();
                if (++iterCounter > DRAW_LOOP_MAX_ITER) {
                    long now = System.currentTimeMillis();
                    int delta = (int) (now - lastTime) / 10 - sleepTime;
                    sleepTime = FRAME_TARGET_MS - delta;
                    if (sleepTime < MIN_SLEEP_MS) {
                        sleepTime = MIN_SLEEP_MS;
                    }
                    iterCounter = 0;
                    lastTime = now;
                }
                try {
                    Thread.sleep(sleepTime);
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