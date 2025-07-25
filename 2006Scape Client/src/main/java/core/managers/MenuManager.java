package core.managers;

import core.engine.Game;
import render.DrawingArea;
import ui.RSInterface;

/**
 * Handles menu interactions extracted from {@link Game}.
 */
public final class MenuManager {
    private final Game game;

    public MenuManager(Game game) {
        this.game = game;
    }

    void sendFrame126(String text, int id) {
        RSInterface.interfaceCache[id].disabledText = text;
        if (RSInterface.interfaceCache[id].parentID == game.tabInterfaceIDs[game.tabID]) {
            game.needDrawTabArea = true;
        }
    }

    public boolean menuHasAddFriend(int index) {
        if (index < 0) {
            return false;
        }
        int k = game.menuActionID[index];
        if (k >= 2000) {
            k -= 2000;
        }
        return k == 337;
    }

    public boolean processMenuClick() {
        if (game.activeInterfaceType != 0) {
            return false;
        }
        int j = game.clickMode3;
        if (game.spellSelected == 1 && game.saveClickX >= 516 && game.saveClickY >= 160 && game.saveClickX <= 765 && game.saveClickY <= 205) {
            j = 0;
        }
        if (game.menuOpen) {
            if (j != 1) {
                int k = game.mouseX;
                int j1 = game.mouseY;
                if (game.menuScreenArea == 0) {
                    k -= 4;
                    j1 -= 4;
                }
                if (game.menuScreenArea == 1) {
                    k -= 553;
                    j1 -= 205;
                }
                if (game.menuScreenArea == 2) {
                    k -= 17;
                    j1 -= 357;
                }
                if (k < game.menuOffsetX - 10 || k > game.menuOffsetX + game.menuWidth + 10 || j1 < game.menuOffsetY - 10 || j1 > game.menuOffsetY + game.menuHeight + 10) {
                    game.menuOpen = false;
                    if (game.menuScreenArea == 1) {
                        game.needDrawTabArea = true;
                    }
                    if (game.menuScreenArea == 2) {
                        game.inputTaken = true;
                    }
                }
            }
            if (j == 1) {
                int l = game.menuOffsetX;
                int k1 = game.menuOffsetY;
                int i2 = game.menuWidth;
                int k2 = game.saveClickX;
                int l2 = game.saveClickY;
                if (game.menuScreenArea == 0) {
                    k2 -= 4;
                    l2 -= 4;
                }
                if (game.menuScreenArea == 1) {
                    k2 -= 553;
                    l2 -= 205;
                }
                if (game.menuScreenArea == 2) {
                    k2 -= 17;
                    l2 -= 357;
                }
                int i3 = -1;
                for (int j3 = 0; j3 < game.menuActionRow; j3++) {
                    int k3 = k1 + 31 + (game.menuActionRow - 1 - j3) * 15;
                    if (k2 > l && k2 < l + i2 && l2 > k3 - 13 && l2 < k3 + 3) {
                        i3 = j3;
                    }
                }
                if (i3 != -1) {
                    game.doAction(i3);
                }
                game.menuOpen = false;
                if (game.menuScreenArea == 1) {
                    game.needDrawTabArea = true;
                }
                if (game.menuScreenArea == 2) {
                    game.inputTaken = true;
                }
            }
        } else {
            if (j == 1 && game.menuActionRow > 0) {
                int i1 = game.menuActionID[game.menuActionRow - 1];
                if (i1 == 632 || i1 == 78 || i1 == 867 || i1 == 431 || i1 == 53 || i1 == 74 || i1 == 454 || i1 == 539 || i1 == 493 || i1 == 847 || i1 == 447 || i1 == 1125) {
                    int l1 = game.menuActionCmd2[game.menuActionRow - 1];
                    int j2 = game.menuActionCmd3[game.menuActionRow - 1];
                    RSInterface targetInterface = RSInterface.interfaceCache[j2];
                    if (targetInterface.allowItemDragging || targetInterface.insertItems) {
                        game.itemBeingDragged = false;
                        game.dragCounter = 0;
                        game.dragInterfaceId = j2;
                        game.draggedSlot = l1;
                        game.activeInterfaceType = 2;
                        game.dragStartX = game.saveClickX;
                        game.dragStartY = game.saveClickY;
                        if (RSInterface.interfaceCache[j2].parentID == game.openInterfaceID) {
                            game.activeInterfaceType = 1;
                        }
                        if (RSInterface.interfaceCache[j2].parentID == game.backDialogID) {
                            game.activeInterfaceType = 3;
                        }
                        return true;
                    }
                }
            }
            if (j == 1 && (game.oneMouseButtonMode == 1 || menuHasAddFriend(game.menuActionRow - 1)) && game.menuActionRow > 2) {
                j = 2;
            }
            if (j == 1 && game.menuActionRow > 0) {
                game.doAction(game.menuActionRow - 1);
            }
            if (j == 2 && game.menuActionRow > 0) {
                determineMenuSize();
            }
        }
        return false;
    }

    public void drawMenu() {
        int xPos = game.menuOffsetX;
        int yPos = game.menuOffsetY;
        int menuW = game.menuWidth;
        int menuH = game.menuHeight;
        int fill = 0x5d5447;

        DrawingArea.fillArea(menuH, yPos, fill, menuW, xPos);
        DrawingArea.fillArea(16, yPos + 1, 0, menuW - 2, xPos + 1);
        DrawingArea.fillPixels(yPos + 18, menuH - 19, 0, xPos + 1, menuW - 2);
        game.chatTextDrawingArea.textLeft(fill, "Choose Option", yPos + 14, xPos + 3);

        int mX = game.mouseX;
        int mY = game.mouseY;
        if (game.menuScreenArea == 0) {
            mX -= 4;
            mY -= 4;
        }
        if (game.menuScreenArea == 1) {
            mX -= 553;
            mY -= 205;
        }
        if (game.menuScreenArea == 2) {
            mX -= 17;
            mY -= 357;
        }
        for (int rowItem = 0; rowItem < game.menuActionRow; rowItem++) {
            int yPosItem = yPos + 31 + (game.menuActionRow - 1 - rowItem) * 15;
            int colorItem = 0xffffff;
            if (mX > xPos && mX < xPos + menuW && mY > yPosItem - 13 && mY < yPosItem + 3) {
                colorItem = 0xffff00;
            }
            game.chatTextDrawingArea.textLeftShadow(true, xPos + 3, colorItem, game.menuActionName[rowItem], yPosItem);
        }
    }

    public void determineMenuSize() {
        int i = game.chatTextDrawingArea.getTextWidth("Choose Option");
        for (int j = 0; j < game.menuActionRow; j++) {
            int k = game.chatTextDrawingArea.getTextWidth(game.menuActionName[j]);
            if (k > i) {
                i = k;
            }
        }
        i += 8;
        int l = 15 * game.menuActionRow + 21;
        if (game.saveClickX > 4 && game.saveClickY > 4 && game.saveClickX < 516 && game.saveClickY < 338) {
            int i1 = game.saveClickX - 4 - i / 2;
            if (i1 + i > 512) {
                i1 = 512 - i;
            }
            if (i1 < 0) {
                i1 = 0;
            }
            int l1 = game.saveClickY - 4;
            if (l1 + l > 334) {
                l1 = 334 - l;
            }
            if (l1 < 0) {
                l1 = 0;
            }
            game.menuOpen = true;
            game.menuScreenArea = 0;
            game.menuOffsetX = i1;
            game.menuOffsetY = l1;
            game.menuWidth = i;
            game.menuHeight = 15 * game.menuActionRow + 22;
        }
        if (game.saveClickX > 553 && game.saveClickY > 205 && game.saveClickX < 743 && game.saveClickY < 466) {
            int j1 = game.saveClickX - 553 - i / 2;
            if (j1 < 0) {
                j1 = 0;
            } else if (j1 + i > 190) {
                j1 = 190 - i;
            }
            int i2 = game.saveClickY - 205;
            if (i2 < 0) {
                i2 = 0;
            } else if (i2 + l > 261) {
                i2 = 261 - l;
            }
            game.menuOpen = true;
            game.menuScreenArea = 1;
            game.menuOffsetX = j1;
            game.menuOffsetY = i2;
            game.menuWidth = i;
            game.menuHeight = 15 * game.menuActionRow + 22;
        }
        if (game.saveClickX > 17 && game.saveClickY > 357 && game.saveClickX < 496 && game.saveClickY < 453) {
            int k1 = game.saveClickX - 17 - i / 2;
            if (k1 < 0) {
                k1 = 0;
            } else if (k1 + i > 479) {
                k1 = 479 - i;
            }
            int j2 = game.saveClickY - 357;
            if (j2 < 0) {
                j2 = 0;
            } else if (j2 + l > 96) {
                j2 = 96 - l;
            }
            game.menuOpen = true;
            game.menuScreenArea = 2;
            game.menuOffsetX = k1;
            game.menuOffsetY = j2;
            game.menuWidth = i;
            game.menuHeight = 15 * game.menuActionRow + 22;
        }
    }
}
