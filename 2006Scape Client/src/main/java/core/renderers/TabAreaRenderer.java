package core.renderers;

import cache.StreamLoader;
import core.engine.Game;
import core.engine.ClientSettings;
import render.core.Texture;
import render.core.Sprite;
import render.core.Background;
import ui.RSInterface;
import core.world.WorldController;
import java.awt.Color;

/** Renders the tab area UI extracted from {@link Game}. */
public final class TabAreaRenderer {
    private final Game game;

    public TabAreaRenderer(Game game) {
        this.game = game;
    }

    public void drawButton(boolean enabled, int x, int y, int width) {
        StreamLoader streamLoader_2 = game.streamLoaderForName(4, "2d graphics", "media", game.expectedCRCs[4], 40);
        Sprite buttonLeft = new Sprite(streamLoader_2, "miscgraphics", enabled ? 7 : 4);
        Sprite buttonRight = new Sprite(streamLoader_2, "miscgraphics", enabled ? 8 : 6);
        int curWidth = 30;
        buttonLeft.drawTransparentSprite(x, y);
        while ((curWidth + 26) < width) {
            buttonRight.drawTransparentSprite(x + curWidth, y);
            curWidth += 26;
        }
        buttonRight.drawTransparentSprite(x + width - 30, y);
    }

    public void drawCheckbox(boolean enabled, int x, int y) {
        StreamLoader streamLoader_2 = game.streamLoaderForName(4, "2d graphics", "media", game.expectedCRCs[4], 40);
        Sprite checkboxUnchecked = new Sprite(streamLoader_2, "miscgraphics", 10);
        Sprite checkboxChecked = new Sprite(streamLoader_2, "miscgraphics", 11);
    }

    public void drawTabArea() {
        game.textBackground.initDrawingArea();
        Texture.lineOffsets = game.tabAreaOffsets;
        game.invBack.draw(0, 0);
        if (game.invOverlayInterfaceID == -1) {
            if (game.tabInterfaceIDs[game.tabID] != -1) {
                if (game.tabID == 7 && ClientSettings.CUSTOM_SETTINGS_TAB) {
                    try {
                        int centerX = 95;
                        int currentY = 10;
                        int textMiddle = 22;
                        int textTop = 14;
                        int textBottom = 29;

                        drawButton(game.customSettingVisiblePlayerNames, centerX - 73, currentY, 146);
                        game.boldFont.textCenterShadow(Color.YELLOW.hashCode(), centerX, "always visible", currentY + textTop, true);
                        game.boldFont.textCenterShadow(Color.YELLOW.hashCode(), centerX, "player names", currentY + textBottom, true);

                        drawButton(true, centerX - 73, currentY += 40, 146);
                        game.boldFont.textCenterShadow(Color.YELLOW.hashCode(), centerX, "item drops visible", currentY + textTop, true);
                        game.boldFont.textCenterShadow(Color.WHITE.hashCode(), centerX, game.intToKOrMil(game.customSettingMinItemValue) + " gp", currentY + textBottom, true);

                        drawButton(true, centerX - 73, currentY += 40, 146);
                        game.boldFont.textCenterShadow(Color.YELLOW.hashCode(), centerX, "draw distance", currentY + textTop, true);
                        game.boldFont.textCenterShadow(Color.WHITE.hashCode(), centerX, WorldController.drawDistance + " tiles", currentY + textBottom, true);

                        drawButton(game.customSettingShowExperiencePerHour, centerX - 73, currentY += 40, 146);
                        game.boldFont.textCenterShadow(Color.YELLOW.hashCode(), centerX, "show exp info", currentY + textMiddle, true);

                        drawButton(game.showInfo, centerX - 73, currentY += 40, 146);
                        game.boldFont.textCenterShadow(Color.YELLOW.hashCode(), centerX, "show debug info", currentY + textMiddle, true);

                        drawButton(game.customSettingVisualFixes, centerX - 73, currentY += 40, 146);
                        game.boldFont.textCenterShadow(Color.YELLOW.hashCode(), centerX, "visual fixes", currentY + textMiddle, true);
                    } catch (Exception e) { }
                }
            }
        }
        if (game.invOverlayInterfaceID != -1) {
            game.drawInterface(0, 0, RSInterface.interfaceCache[game.invOverlayInterfaceID], 0);
        } else if (game.tabInterfaceIDs[game.tabID] != -1) {
            game.drawInterface(0, 0, RSInterface.interfaceCache[game.tabInterfaceIDs[game.tabID]], 0);
        }
        if (game.menuOpen && game.menuScreenArea == 1) {
            game.drawMenu();
        }
        game.textBackground.drawGraphics(205, game.getGraphics(), 553);
        game.tabAreaBuffer.initDrawingArea();
        Texture.lineOffsets = game.chatBoxAreaOffsets;
    }

    public void animateTextures(int j) {
        if (!game.lowMem) {
            if (Texture.textureLastUsed[17] >= j) {
                Background background = Texture.textures[17];
                int k = background.width * background.height - 1;
                int j1 = background.width * game.animationCycle * 2;
                byte[] abyte0 = background.pixels;
                byte[] abyte3 = game.soundPayload;
                for (int i2 = 0; i2 <= k; i2++) {
                    abyte3[i2] = abyte0[i2 - j1 & k];
                }

                background.pixels = abyte3;
                game.soundPayload = abyte0;
                Texture.unloadTexture(17);
            }
            if (Texture.textureLastUsed[24] >= j) {
                Background background_1 = Texture.textures[24];
                int l = background_1.width * background_1.height - 1;
                int k1 = background_1.width * game.animationCycle * 2;
                byte[] abyte1 = background_1.pixels;
                byte[] abyte4 = game.soundPayload;
                for (int j2 = 0; j2 <= l; j2++) {
                    abyte4[j2] = abyte1[j2 - k1 & l];
                }

                background_1.pixels = abyte4;
                game.soundPayload = abyte1;
                Texture.unloadTexture(24);
            }
            if (Texture.textureLastUsed[34] >= j) {
                Background background_2 = Texture.textures[34];
                int i1 = background_2.width * background_2.height - 1;
                int l1 = background_2.width * game.animationCycle * 2;
                byte[] abyte2 = background_2.pixels;
                byte[] abyte5 = game.soundPayload;
                for (int k2 = 0; k2 <= i1; k2++) {
                    abyte5[k2] = abyte2[k2 - l1 & i1];
                }

                background_2.pixels = abyte5;
                game.soundPayload = abyte2;
                Texture.unloadTexture(34);
            }
            if (Texture.textureLastUsed[40] >= j) {
                Background background_3 = Texture.textures[40];
                int i2 = background_3.width * background_3.height - 1;
                int l2 = background_3.width * game.animationCycle * 2;
                byte[] abyte6 = background_3.pixels;
                byte[] abyte7 = game.soundPayload;
                for (int k3 = 0; k3 <= i2; k3++) {
                    abyte7[k3] = abyte6[k3 - l2 & i2];
                }

                background_3.pixels = abyte7;
                game.soundPayload = abyte6;
                Texture.unloadTexture(40);
            }
        }
    }
}
