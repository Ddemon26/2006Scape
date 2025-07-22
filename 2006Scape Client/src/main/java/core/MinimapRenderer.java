package core;

import game.ObjectDef;
import render.Texture;

/** Handles minimap generation extracted from {@link Game}. */
final class MinimapRenderer {
    private final Game game;

    MinimapRenderer(Game game) {
        this.game = game;
    }

    void generateMinimap(int i) {
        int[] ai = game.minimapImage.pixels;
        for (int k = 0; k < ai.length; k++) {
            ai[k] = 0;
        }
        for (int l = 1; l < 103; l++) {
            int i1 = 24628 + (103 - l) * 512 * 4;
            for (int k1 = 1; k1 < 103; k1++) {
                if ((game.tileFlags[i][k1][l] & 0x18) == 0) {
                    game.worldController.renderMinimapTile(ai, i1, i, k1, l);
                }
                if (i < 3 && (game.tileFlags[i + 1][k1][l] & 8) != 0) {
                    game.worldController.renderMinimapTile(ai, i1, i + 1, k1, l);
                }
                i1 += 4;
            }
        }
        int j1 = (238 + (int) (Math.random() * 20D) - 10 << 16)
            + (238 + (int) (Math.random() * 20D) - 10 << 8)
            + 238 + (int) (Math.random() * 20D) - 10;
        int l1 = 238 + (int) (Math.random() * 20D) - 10 << 16;
        game.minimapImage.initializeDrawingArea();
        for (int i2 = 1; i2 < 103; i2++) {
            for (int j2 = 1; j2 < 103; j2++) {
                if ((game.tileFlags[i][j2][i2] & 0x18) == 0) {
                    game.drawMinimapLoc(i2, j1, j2, l1, i);
                }
                if (i < 3 && (game.tileFlags[i + 1][j2][i2] & 8) != 0) {
                    game.drawMinimapLoc(i2, j1, j2, l1, i + 1);
                }
            }
        }
        if (game.tabAreaBuffer != null) {
            game.tabAreaBuffer.initDrawingArea();
            Texture.lineOffsets = game.chatBoxAreaOffsets;
        }
        game.minimapIconCount = 0;
        for (int k2 = 0; k2 < 104; k2++) {
            for (int l2 = 0; l2 < 104; l2++) {
                int i3 = game.worldController.getTileDecorationUid(game.plane, k2, l2);
                if (i3 != 0) {
                    i3 = i3 >> 14 & 0x7fff;
                    int j3 = ObjectDef.forID(i3).mapIconId;
                    if (j3 >= 0) {
                        int k3 = k2;
                        int l3 = l2;
                        if (j3 != 22 && j3 != 29 && j3 != 34 && j3 != 36
                            && j3 != 46 && j3 != 47 && j3 != 48) {
                            byte byte0 = 104;
                            byte byte1 = 104;
                            int[][] ai1 = game.collisionMaps[game.plane].clippingFlags;
                            for (int i4 = 0; i4 < 10; i4++) {
                                int j4 = (int) (Math.random() * 4D);
                                if (j4 == 0 && k3 > 0 && k3 > k2 - 3
                                        && (ai1[k3 - 1][l3] & 0x1280108) == 0) {
                                    k3--;
                                }
                                if (j4 == 1 && k3 < byte0 - 1 && k3 < k2 + 3
                                        && (ai1[k3 + 1][l3] & 0x1280180) == 0) {
                                    k3++;
                                }
                                if (j4 == 2 && l3 > 0 && l3 > l2 - 3
                                        && (ai1[k3][l3 - 1] & 0x1280102) == 0) {
                                    l3--;
                                }
                                if (j4 == 3 && l3 < byte1 - 1 && l3 < l2 + 3
                                        && (ai1[k3][l3 + 1] & 0x1280120) == 0) {
                                    l3++;
                                }
                            }
                        }
                        game.minimapIconSprites[game.minimapIconCount] = game.mapFunctions[j3];
                        game.minimapIconX[game.minimapIconCount] = k3;
                        game.minimapIconY[game.minimapIconCount] = l3;
                        game.minimapIconCount++;
                    }
                }
            }
        }
    }
}
