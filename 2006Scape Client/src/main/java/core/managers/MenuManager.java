package core.managers;

import core.engine.Game;
import render.core.DrawingArea;
import ui.RSInterface;
import game.definitions.EntityDef;
import game.definitions.ObjectDef;
import game.definitions.ItemDef;
import game.entities.Player;
import game.entities.NPC;
import game.items.Item;
import render.geometry.Model;
import util.collections.NodeList;

/** Handles menu interactions extracted from {@link Game}. */
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
    if (game.spellSelected == 1
        && game.saveClickX >= 516
        && game.saveClickY >= 160
        && game.saveClickX <= 765
        && game.saveClickY <= 205) {
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
        if (k < game.menuOffsetX - 10
            || k > game.menuOffsetX + game.menuWidth + 10
            || j1 < game.menuOffsetY - 10
            || j1 > game.menuOffsetY + game.menuHeight + 10) {
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
        if (i1 == 632
            || i1 == 78
            || i1 == 867
            || i1 == 431
            || i1 == 53
            || i1 == 74
            || i1 == 454
            || i1 == 539
            || i1 == 493
            || i1 == 847
            || i1 == 447
            || i1 == 1125) {
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
      if (j == 1
          && (game.oneMouseButtonMode == 1 || menuHasAddFriend(game.menuActionRow - 1))
          && game.menuActionRow > 2) {
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
      game.chatTextDrawingArea.textLeftShadow(
          true, xPos + 3, colorItem, game.menuActionName[rowItem], yPosItem);
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
    if (game.saveClickX > 4
        && game.saveClickY > 4
        && game.saveClickX < 516
        && game.saveClickY < 338) {
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
    if (game.saveClickX > 553
        && game.saveClickY > 205
        && game.saveClickX < 743
        && game.saveClickY < 466) {
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
    if (game.saveClickX > 17
        && game.saveClickY > 357
        && game.saveClickX < 496
        && game.saveClickY < 453) {
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

  /** Build the 3D scene context menu for objects, NPCs, players and items. */
  public void build3dScreenMenu() {
    if (game.itemSelected == 0 && game.spellSelected == 0) {
      game.menuActionName[game.menuActionRow] = "Walk here";
      game.menuActionID[game.menuActionRow] = 516;
      game.menuActionCmd2[game.menuActionRow] = game.mouseX;
      game.menuActionCmd3[game.menuActionRow] = game.mouseY;
      game.menuActionRow++;
    }
    int prev = -1;
    for (int k = 0; k < Model.queueLength; k++) {
      int uid = Model.faceQueue[k];
      int tileX = uid & 0x7f;
      int tileY = uid >> 7 & 0x7f;
      int type = uid >> 29 & 3;
      int id = uid >> 14 & 0x7fff;
      if (uid == prev) {
        continue;
      }
      prev = uid;
      if (type == 2 && game.worldController.getObjectConfig(game.plane, tileX, tileY, uid) >= 0) {
        ObjectDef def = ObjectDef.forID(id);
        if (def.childrenIDs != null) {
          def = def.getChildDefinition();
        }
        if (def == null) {
          continue;
        }
        if (game.itemSelected == 1) {
          game.menuActionName[game.menuActionRow] =
              "Use " + game.selectedItemName + " with @cya@" + def.name;
          game.menuActionID[game.menuActionRow] = 62;
          game.menuActionCmd1[game.menuActionRow] = uid;
          game.menuActionCmd2[game.menuActionRow] = tileX;
          game.menuActionCmd3[game.menuActionRow] = tileY;
          game.menuActionRow++;
        } else if (game.spellSelected == 1) {
          if ((game.spellUsableOn & 4) == 4) {
            game.menuActionName[game.menuActionRow] = game.spellTooltip + " @cya@" + def.name;
            game.menuActionID[game.menuActionRow] = 956;
            game.menuActionCmd1[game.menuActionRow] = uid;
            game.menuActionCmd2[game.menuActionRow] = tileX;
            game.menuActionCmd3[game.menuActionRow] = tileY;
            game.menuActionRow++;
          }
        } else {
          if (def.actions != null) {
            for (int opt = 4; opt >= 0; opt--) {
              if (def.actions[opt] != null) {
                game.menuActionName[game.menuActionRow] = def.actions[opt] + " @cya@" + def.name;
                if (opt == 0) {
                  game.menuActionID[game.menuActionRow] = 502;
                }
                if (opt == 1) {
                  game.menuActionID[game.menuActionRow] = 900;
                }
                if (opt == 2) {
                  game.menuActionID[game.menuActionRow] = 113;
                }
                if (opt == 3) {
                  game.menuActionID[game.menuActionRow] = 872;
                }
                if (opt == 4) {
                  game.menuActionID[game.menuActionRow] = 1062;
                }
                game.menuActionCmd1[game.menuActionRow] = uid;
                game.menuActionCmd2[game.menuActionRow] = tileX;
                game.menuActionCmd3[game.menuActionRow] = tileY;
                game.menuActionRow++;
              }
            }
          }
          game.menuActionName[game.menuActionRow] =
              "Examine @cya@" + def.name
                  + (game.showInfo
                      ? " @gre@(@whi@" + id + "@gre@) (@whi@" + (tileX + game.baseX) + "," + (tileY + game.baseY) + "@gre@)"
                      : "");
          game.menuActionID[game.menuActionRow] = 1226;
          game.menuActionCmd1[game.menuActionRow] = def.type << 14;
          game.menuActionCmd2[game.menuActionRow] = tileX;
          game.menuActionCmd3[game.menuActionRow] = tileY;
          game.menuActionRow++;
        }
      }
      if (type == 1) {
        NPC npc = game.npcArray[id];
        if (npc.definition.size == 1 && (npc.x & 0x7f) == 64 && (npc.y & 0x7f) == 64) {
          for (int j2 = 0; j2 < game.npcCount; j2++) {
            NPC npc2 = game.npcArray[game.npcIndices[j2]];
            if (npc2 != null
                && npc2 != npc
                && npc2.definition.size == 1
                && npc2.x == npc.x
                && npc2.y == npc.y) {
              buildAtNPCMenu(npc2.definition, game.npcIndices[j2], tileY, tileX);
            }
          }

          for (int l2 = 0; l2 < game.playerCount; l2++) {
            Player pl = game.playerArray[game.playerIndices[l2]];
            if (pl != null && pl.x == npc.x && pl.y == npc.y) {
              buildAtPlayerMenu(tileX, game.playerIndices[l2], pl, tileY);
            }
          }
        }
        buildAtNPCMenu(npc.definition, id, tileY, tileX);
      }
      if (type == 0) {
        Player player = game.playerArray[id];
        if ((player.x & 0x7f) == 64 && (player.y & 0x7f) == 64) {
          for (int k2 = 0; k2 < game.npcCount; k2++) {
            NPC npc = game.npcArray[game.npcIndices[k2]];
            if (npc != null && npc.definition.size == 1 && npc.x == player.x && npc.y == player.y) {
              buildAtNPCMenu(npc.definition, game.npcIndices[k2], tileY, tileX);
            }
          }

          for (int i3 = 0; i3 < game.playerCount; i3++) {
            Player target = game.playerArray[game.playerIndices[i3]];
            if (target != null && target != player && target.x == player.x && target.y == player.y) {
              buildAtPlayerMenu(tileX, game.playerIndices[i3], target, tileY);
            }
          }
        }
        buildAtPlayerMenu(tileX, id, player, tileY);
      }
      if (type == 3) {
        NodeList itemList = game.groundArray[game.plane][tileX][tileY];
        if (itemList != null) {
          for (Item item = (Item) itemList.getFirst(); item != null; item = (Item) itemList.getNext()) {
            ItemDef itemDef = ItemDef.lookup(item.ID);
            if (game.itemSelected == 1) {
              game.menuActionName[game.menuActionRow] =
                  "Use " + game.selectedItemName + " with @lre@" + itemDef.name;
              game.menuActionID[game.menuActionRow] = 511;
              game.menuActionCmd1[game.menuActionRow] = item.ID;
              game.menuActionCmd2[game.menuActionRow] = tileX;
              game.menuActionCmd3[game.menuActionRow] = tileY;
              game.menuActionRow++;
            } else if (game.spellSelected == 1) {
              if ((game.spellUsableOn & 1) == 1) {
                game.menuActionName[game.menuActionRow] = game.spellTooltip + " @lre@" + itemDef.name;
                game.menuActionID[game.menuActionRow] = 94;
                game.menuActionCmd1[game.menuActionRow] = item.ID;
                game.menuActionCmd2[game.menuActionRow] = tileX;
                game.menuActionCmd3[game.menuActionRow] = tileY;
                game.menuActionRow++;
              }
            } else {
              for (int j3 = 4; j3 >= 0; j3--) {
                if (itemDef.groundActions != null && itemDef.groundActions[j3] != null) {
                  game.menuActionName[game.menuActionRow] =
                      itemDef.groundActions[j3] + " @lre@" + itemDef.name;
                  if (j3 == 0) {
                    game.menuActionID[game.menuActionRow] = 652;
                  }
                  if (j3 == 1) {
                    game.menuActionID[game.menuActionRow] = 567;
                  }
                  if (j3 == 2) {
                    game.menuActionID[game.menuActionRow] = 234;
                  }
                  if (j3 == 3) {
                    game.menuActionID[game.menuActionRow] = 244;
                  }
                  if (j3 == 4) {
                    game.menuActionID[game.menuActionRow] = 213;
                  }
                  game.menuActionCmd1[game.menuActionRow] = item.ID;
                  game.menuActionCmd2[game.menuActionRow] = tileX;
                  game.menuActionCmd3[game.menuActionRow] = tileY;
                  game.menuActionRow++;
                } else if (j3 == 2) {
                  game.menuActionName[game.menuActionRow] = "Take @lre@" + itemDef.name;
                  game.menuActionID[game.menuActionRow] = 234;
                  game.menuActionCmd1[game.menuActionRow] = item.ID;
                  game.menuActionCmd2[game.menuActionRow] = tileX;
                  game.menuActionCmd3[game.menuActionRow] = tileY;
                  game.menuActionRow++;
                }
              }

              game.menuActionName[game.menuActionRow] =
                  "Examine @lre@" + itemDef.name + (game.showInfo ? " @gre@(@whi@" + item.ID + "@gre@)" : "");
              game.menuActionID[game.menuActionRow] = 1448;
              game.menuActionCmd1[game.menuActionRow] = item.ID;
              game.menuActionCmd2[game.menuActionRow] = tileX;
              game.menuActionCmd3[game.menuActionRow] = tileY;
              game.menuActionRow++;
            }
          }
        }
      }
    }
  }

  /** Build context menu entries for interacting with an NPC. */
  public void buildAtNPCMenu(EntityDef entityDef, int i, int j, int k) {
    if (game.menuActionRow >= 400) {
      return;
    }
    if (entityDef.childrenIDs != null) {
      entityDef = entityDef.transform();
    }
    if (entityDef == null || !entityDef.clickable) {
      return;
    }
    String s = entityDef.name;
    if (entityDef.combatLevel != 0) {
      s =
          s
              + game.combatDiffColor(game.myPlayer.combatLevel, entityDef.combatLevel)
              + " (level-"
              + entityDef.combatLevel
              + ")";
    }
    if (game.itemSelected == 1) {
      game.menuActionName[game.menuActionRow] = "Use " + game.selectedItemName + " with @yel@" + s;
      game.menuActionID[game.menuActionRow] = 582;
      game.menuActionCmd1[game.menuActionRow] = i;
      game.menuActionCmd2[game.menuActionRow] = k;
      game.menuActionCmd3[game.menuActionRow] = j;
      game.menuActionRow++;
      return;
    }
    if (game.spellSelected == 1) {
      if ((game.spellUsableOn & 2) == 2) {
        game.menuActionName[game.menuActionRow] = game.spellTooltip + " @yel@" + s;
        game.menuActionID[game.menuActionRow] = 413;
        game.menuActionCmd1[game.menuActionRow] = i;
        game.menuActionCmd2[game.menuActionRow] = k;
        game.menuActionCmd3[game.menuActionRow] = j;
        game.menuActionRow++;
      }
    } else {
      if (entityDef.actions != null) {
        for (int l = 4; l >= 0; l--) {
          if (entityDef.actions[l] != null && !entityDef.actions[l].equalsIgnoreCase("attack")) {
            game.menuActionName[game.menuActionRow] = entityDef.actions[l] + " @yel@" + s;
            if (l == 0) {
              game.menuActionID[game.menuActionRow] = 20;
            }
            if (l == 1) {
              game.menuActionID[game.menuActionRow] = 412;
            }
            if (l == 2) {
              game.menuActionID[game.menuActionRow] = 225;
            }
            if (l == 3) {
              game.menuActionID[game.menuActionRow] = 965;
            }
            if (l == 4) {
              game.menuActionID[game.menuActionRow] = 478;
            }
            game.menuActionCmd1[game.menuActionRow] = i;
            game.menuActionCmd2[game.menuActionRow] = k;
            game.menuActionCmd3[game.menuActionRow] = j;
            game.menuActionRow++;
          }
        }
      }
      if (entityDef.actions != null) {
        for (int i1 = 4; i1 >= 0; i1--) {
          if (entityDef.actions[i1] != null && entityDef.actions[i1].equalsIgnoreCase("attack")) {
            char c = '\0';
            if (entityDef.combatLevel > game.myPlayer.combatLevel) {
              c = '\u07D0';
            }
            game.menuActionName[game.menuActionRow] = entityDef.actions[i1] + " @yel@" + s;
            if (i1 == 0) {
              game.menuActionID[game.menuActionRow] = 20 + c;
            }
            if (i1 == 1) {
              game.menuActionID[game.menuActionRow] = 412 + c;
            }
            if (i1 == 2) {
              game.menuActionID[game.menuActionRow] = 225 + c;
            }
            if (i1 == 3) {
              game.menuActionID[game.menuActionRow] = 965 + c;
            }
            if (i1 == 4) {
              game.menuActionID[game.menuActionRow] = 478 + c;
            }
            game.menuActionCmd1[game.menuActionRow] = i;
            game.menuActionCmd2[game.menuActionRow] = k;
            game.menuActionCmd3[game.menuActionRow] = j;
            game.menuActionRow++;
          }
        }
      }
      game.menuActionName[game.menuActionRow] =
          "Examine @yel@" + s + (game.showInfo ? " @gre@(@whi@" + entityDef.type + "@gre@)" : "");
      game.menuActionID[game.menuActionRow] = 1025;
      game.menuActionCmd1[game.menuActionRow] = i;
      game.menuActionCmd2[game.menuActionRow] = k;
      game.menuActionCmd3[game.menuActionRow] = j;
      game.menuActionRow++;
    }
  }

  /** Build context menu entries for interacting with another player. */
  public void buildAtPlayerMenu(int i, int j, Player player, int k) {
    if (player == game.myPlayer) {
      return;
    }
    if (game.menuActionRow >= 400) {
      return;
    }
    String s;
    if (player.skill == 0) {
      if (player.combatLevel > 0) {
        s =
            player.name
                + game.combatDiffColor(game.myPlayer.combatLevel, player.combatLevel)
                + " (level-"
                + player.combatLevel
                + ")";
      } else {
        s = player.name + " @cya@(store)";
      }
    } else {
      s = player.name + " (skill-" + player.skill + ")";
    }
    if (game.itemSelected == 1) {
      game.menuActionName[game.menuActionRow] = "Use " + game.selectedItemName + " with @whi@" + s;
      game.menuActionID[game.menuActionRow] = 491;
      game.menuActionCmd1[game.menuActionRow] = j;
      game.menuActionCmd2[game.menuActionRow] = i;
      game.menuActionCmd3[game.menuActionRow] = k;
      game.menuActionRow++;
    } else if (game.spellSelected == 1) {
      if ((game.spellUsableOn & 8) == 8) {
        game.menuActionName[game.menuActionRow] = game.spellTooltip + " @whi@" + s;
        game.menuActionID[game.menuActionRow] = 365;
        game.menuActionCmd1[game.menuActionRow] = j;
        game.menuActionCmd2[game.menuActionRow] = i;
        game.menuActionCmd3[game.menuActionRow] = k;
        game.menuActionRow++;
      }
    } else {
      for (int l = 4; l >= 0; l--) {
        if (game.atPlayerActions[l] != null) {
          game.menuActionName[game.menuActionRow] = game.atPlayerActions[l] + " @whi@" + s;
          char c = '\0';
          if (game.atPlayerActions[l].equalsIgnoreCase("attack")) {
            if (player.combatLevel > game.myPlayer.combatLevel) {
              c = '\u07D0';
            }
            if (game.myPlayer.team != 0 && player.team != 0) {
              if (game.myPlayer.team == player.team) {
                c = '\u07D0';
              } else {
                c = '\0';
              }
            }
          } else if (game.atPlayerArray[l]) {
            c = '\u07D0';
          }
          if (l == 0) {
            game.menuActionID[game.menuActionRow] = 561 + c;
          }
          if (l == 1) {
            game.menuActionID[game.menuActionRow] = 779 + c;
          }
          if (l == 2) {
            game.menuActionID[game.menuActionRow] = 27 + c;
          }
          if (l == 3) {
            game.menuActionID[game.menuActionRow] = 577 + c;
          }
          if (l == 4) {
            game.menuActionID[game.menuActionRow] = 729 + c;
          }
          game.menuActionCmd1[game.menuActionRow] = j;
          game.menuActionCmd2[game.menuActionRow] = i;
          game.menuActionCmd3[game.menuActionRow] = k;
          game.menuActionRow++;
        }
      }
    }
    for (int i1 = 0; i1 < game.menuActionRow; i1++) {
      if (game.menuActionID[i1] == 516) {
        game.menuActionName[i1] = "Walk here @whi@" + s;
        return;
      }
    }
  }
}
