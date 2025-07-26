package ui;

import core.engine.Game;
import game.definitions.ItemDef;
import render.core.DrawingArea;

/** Builds interface context menus extracted from {@link Game}. */
public final class InterfaceMenuBuilder {
  private final Game game;

  public InterfaceMenuBuilder(Game game) {
    this.game = game;
  }

  public void buildInterfaceMenu(
      int x, RSInterface parent, int mouseX, int y, int mouseY, int scroll) {
    if (parent.type != 0 || parent.children == null || parent.hideUntilHovered) {
      return;
    }
    if (mouseX < x || mouseY < y || mouseX > x + parent.width || mouseY > y + parent.height) {
      return;
    }
    int childCount = parent.children.length;
    for (int idx = 0; idx < childCount; idx++) {
      int childX = parent.childX[idx] + x;
      int childY = parent.childY[idx] + y - scroll;
      RSInterface child = RSInterface.interfaceCache[parent.children[idx]];
      childX += child.offsetX;
      childY += child.offsetY;
      if ((child.hoverTarget >= 0 || child.hoverTextColor != 0)
          && mouseX >= childX
          && mouseY >= childY
          && mouseX < childX + child.width
          && mouseY < childY + child.height) {
        game.hoveredWidgetId = child.hoverTarget >= 0 ? child.hoverTarget : child.id;
      }
      if (child.type == 0) {
        buildInterfaceMenu(childX, child, mouseX, childY, mouseY, child.scrollPosition);
        if (child.scrollMax > child.height) {
          game.handleScrollbarInput(
              childX + child.width,
              child.height,
              mouseX,
              mouseY,
              child,
              childY,
              true,
              child.scrollMax);
        }
      } else {
        if (child.atActionType == 1
            && mouseX >= childX
            && mouseY >= childY
            && mouseX < childX + child.width
            && mouseY < childY + child.height) {
          boolean flag = false;
          if (child.contentType != 0) {
            flag = game.friendManager.buildFriendsListMenu(child);
          }
          if (!flag) {
            game.menuActionName[game.menuActionRow] =
                game.showInfo ? child.tooltip + ", " + child.id : child.tooltip;
            game.menuActionID[game.menuActionRow] = 315;
            game.menuActionCmd3[game.menuActionRow] = child.id;
            game.menuActionRow++;
          }
        }
        if (child.atActionType == 2
            && game.spellSelected == 0
            && mouseX >= childX
            && mouseY >= childY
            && mouseX < childX + child.width
            && mouseY < childY + child.height) {
          String s = child.selectedActionName;
          if (s.indexOf(" ") != -1) {
            s = s.substring(0, s.indexOf(" "));
          }
          game.menuActionName[game.menuActionRow] = s + " @gre@" + child.spellName;
          game.menuActionID[game.menuActionRow] = 626;
          game.menuActionCmd3[game.menuActionRow] = child.id;
          game.menuActionRow++;
        }
        if (child.atActionType == 3
            && mouseX >= childX
            && mouseY >= childY
            && mouseX < childX + child.width
            && mouseY < childY + child.height) {
          game.menuActionName[game.menuActionRow] = "Close";
          game.menuActionID[game.menuActionRow] = 200;
          game.menuActionCmd3[game.menuActionRow] = child.id;
          game.menuActionRow++;
        }
        if (child.atActionType == 4
            && mouseX >= childX
            && mouseY >= childY
            && mouseX < childX + child.width
            && mouseY < childY + child.height) {
          game.menuActionName[game.menuActionRow] =
              game.showInfo ? child.tooltip + ", " + child.id : child.tooltip;
          game.menuActionID[game.menuActionRow] = 169;
          game.menuActionCmd3[game.menuActionRow] = child.id;
          game.menuActionRow++;
        }
        if (child.atActionType == 5
            && mouseX >= childX
            && mouseY >= childY
            && mouseX < childX + child.width
            && mouseY < childY + child.height) {
          game.menuActionName[game.menuActionRow] =
              game.showInfo ? child.tooltip + ", " + child.id : child.tooltip;
          game.menuActionID[game.menuActionRow] = 646;
          game.menuActionCmd3[game.menuActionRow] = child.id;
          game.menuActionRow++;
        }
        if (child.atActionType == 6
            && !game.actionPending
            && mouseX >= childX
            && mouseY >= childY
            && mouseX < childX + child.width
            && mouseY < childY + child.height) {
          game.menuActionName[game.menuActionRow] =
              game.showInfo ? child.tooltip + ", " + child.id : child.tooltip;
          game.menuActionID[game.menuActionRow] = 679;
          game.menuActionCmd3[game.menuActionRow] = child.id;
          game.menuActionRow++;
        }
        if (child.type == 2) {
          int index = 0;
          for (int row = 0; row < child.height; row++) {
            for (int col = 0; col < child.width; col++) {
              int j3 = childX + col * (32 + child.invSpritePadX);
              int k3 = childY + row * (32 + child.invSpritePadY);
              if (index < 20) {
                j3 += child.spritesX[index];
                k3 += child.spritesY[index];
              }
              if (mouseX >= j3 && mouseY >= k3 && mouseX < j3 + 32 && mouseY < k3 + 32) {
                game.mouseInvInterfaceIndex = index;
                game.lastActiveInvInterface = child.id;
                if (child.inv[index] > 0) {
                  ItemDef itemDef = ItemDef.lookup(child.inv[index] - 1);
                  if (game.itemSelected == 1 && child.isInventoryInterface) {
                    if (child.id != game.selectedItemInterfaceId
                            || index != game.selectedItemSlot) {
                      game.menuActionName[game.menuActionRow] =
                              "Use "
                                      + game.selectedItemName
                                      + " with @lre@"
                                      + itemDef.name;
                      game.menuActionID[game.menuActionRow] = 870;
                      game.menuActionCmd1[game.menuActionRow] = itemDef.id;
                      game.menuActionCmd2[game.menuActionRow] = index;
                      game.menuActionCmd3[game.menuActionRow] = child.id;
                      game.menuActionRow++;
                    }
                  } else if (game.spellSelected == 1 && child.isInventoryInterface) {
                    if ((game.spellUsableOn & 0x10) == 16) {
                      game.menuActionName[game.menuActionRow] =
                              game.spellTooltip + " @lre@" + itemDef.name;
                      game.menuActionID[game.menuActionRow] = 582;
                      game.menuActionCmd1[game.menuActionRow] = itemDef.id;
                      game.menuActionCmd2[game.menuActionRow] = index;
                      game.menuActionCmd3[game.menuActionRow] = child.id;
                      game.menuActionRow++;
                    }
                  } else {
                    if (child.isInventoryInterface) {
                      for (int j4 = 4; j4 >= 3; j4--) {
                        if (itemDef.actions != null && itemDef.actions[j4] != null) {
                          game.menuActionName[game.menuActionRow] =
                              itemDef.actions[j4] + " @lre@" + itemDef.name;
                          if (j4 == 3) {
                            game.menuActionID[game.menuActionRow] = 493;
                          }
                          if (j4 == 4) {
                            game.menuActionID[game.menuActionRow] = 847;
                          }
                          game.menuActionCmd1[game.menuActionRow] = itemDef.id;
                          game.menuActionCmd2[game.menuActionRow] = index;
                          game.menuActionCmd3[game.menuActionRow] = child.id;
                          game.menuActionRow++;
                        } else if (j4 == 4) {
                          game.menuActionName[game.menuActionRow] = "Drop @lre@" + itemDef.name;
                          game.menuActionID[game.menuActionRow] = 847;
                          game.menuActionCmd1[game.menuActionRow] = itemDef.id;
                          game.menuActionCmd2[game.menuActionRow] = index;
                          game.menuActionCmd3[game.menuActionRow] = child.id;
                          game.menuActionRow++;
                        }
                      }
                    }
                    if (child.usableItemInterface) {
                      if (game.shiftDown) {
                        game.menuActionName[game.menuActionRow] = "Drop @lre@" + itemDef.name;
                      } else {
                        game.menuActionName[game.menuActionRow] = "Use @lre@" + itemDef.name;
                      }
                      game.menuActionID[game.menuActionRow] = 447;
                      game.menuActionCmd1[game.menuActionRow] = itemDef.id;
                      game.menuActionCmd2[game.menuActionRow] = index;
                      game.menuActionCmd3[game.menuActionRow] = child.id;
                      game.menuActionRow++;
                    }
                    if (child.isInventoryInterface && itemDef.actions != null) {
                      for (int i4 = 2; i4 >= 0; i4--) {
                        if (itemDef.actions[i4] != null) {
                          if (game.shiftDown) {
                            game.menuActionName[game.menuActionRow] = "Drop @lre@" + itemDef.name;
                            game.menuActionCmd1[game.menuActionRow] = itemDef.id;
                            game.menuActionCmd2[game.menuActionRow] = index;
                            game.menuActionCmd3[game.menuActionRow] = child.id;
                          } else {
                            game.menuActionName[game.menuActionRow] =
                                itemDef.actions[i4] + " @lre@" + itemDef.name;
                            if (i4 == 0) {
                              game.menuActionID[game.menuActionRow] = 74;
                            }
                            if (i4 == 1) {
                              game.menuActionID[game.menuActionRow] = 454;
                            }
                            if (i4 == 2) {
                              game.menuActionID[game.menuActionRow] = 539;
                            }
                            game.menuActionCmd1[game.menuActionRow] = itemDef.id;
                            game.menuActionCmd2[game.menuActionRow] = index;
                            game.menuActionCmd3[game.menuActionRow] = child.id;
                          }
                          game.menuActionRow++;
                        }
                      }
                    }
                    if (child.actions != null) {
                      for (int k4 = 4; k4 >= 0; k4--) {
                        if (child.actions[k4] != null) {
                          game.menuActionName[game.menuActionRow] =
                              child.actions[k4] + " @lre@" + itemDef.name;
                          if (k4 == 0) {
                            game.menuActionID[game.menuActionRow] = 632;
                          }
                          if (k4 == 1) {
                            game.menuActionID[game.menuActionRow] = 78;
                          }
                          if (k4 == 2) {
                            game.menuActionID[game.menuActionRow] = 867;
                          }
                          if (k4 == 3) {
                            game.menuActionID[game.menuActionRow] = 431;
                          }
                          if (k4 == 4) {
                            game.menuActionID[game.menuActionRow] = 53;
                          }
                          game.menuActionCmd1[game.menuActionRow] = itemDef.id;
                          game.menuActionCmd2[game.menuActionRow] = index;
                          game.menuActionCmd3[game.menuActionRow] = child.id;
                          game.menuActionRow++;
                        }
                      }
                    }
                    game.menuActionName[game.menuActionRow] =
                        "Examine @lre@"
                            + itemDef.name
                            + (game.showInfo
                                ? " @gre@(@whi@" + (child.inv[index] - 1) + "@gre@)"
                                : "");
                    game.menuActionID[game.menuActionRow] = 1125;
                    game.menuActionCmd1[game.menuActionRow] = itemDef.id;
                    game.menuActionCmd2[game.menuActionRow] = index;
                    game.menuActionCmd3[game.menuActionRow] = child.id;
                    game.menuActionRow++;
                  }
                }
              }
              index++;
            }
          }
        }
      }
    }
  }

  public void drawScrollThumb(int j, int k, int l, int i1, int j1) {
    game.scrollBar1.draw(i1, l);
    game.scrollBar2.draw(i1, l + j - 16);
    DrawingArea.fillArea(j - 32, l + 16, game.scrollBarColor, 16, i1);
    int k1 = (j - 32) * j / j1;
    if (k1 < 8) {
      k1 = 8;
    }
    int l1 = (j - 32 - k1) * k / (j1 - j);
    DrawingArea.fillArea(k1, l + 16 + l1, game.scrollBarHandleColor, 16, i1);
    DrawingArea.drawVerticalLine(l + 16 + l1, game.scrollBarLightColor, k1, i1);
    DrawingArea.drawVerticalLine(l + 16 + l1, game.scrollBarLightColor, k1, i1 + 1);
    DrawingArea.drawHorizontalLine(l + 16 + l1, game.scrollBarLightColor, 16, i1);
    DrawingArea.drawHorizontalLine(l + 17 + l1, game.scrollBarLightColor, 16, i1);
    DrawingArea.drawVerticalLine(l + 16 + l1, game.scrollBarDarkColor, k1, i1 + 15);
    DrawingArea.drawVerticalLine(l + 17 + l1, game.scrollBarDarkColor, k1 - 1, i1 + 14);
    DrawingArea.drawHorizontalLine(l + 15 + l1 + k1, game.scrollBarDarkColor, 16, i1);
    DrawingArea.drawHorizontalLine(l + 14 + l1 + k1, game.scrollBarDarkColor, 15, i1 + 1);
  }
}
