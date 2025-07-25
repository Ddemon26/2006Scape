package core.renderers;

import core.engine.Game;
import render.core.DrawingArea;
import render.core.Texture;
import ui.RSInterface;
import ui.TextClass;
import ui.TextDrawingArea;

/** Renders the game's chat area. Extracted from {@link Game} for readability. */
public final class ChatAreaRenderer {
  private final Game game;

  public ChatAreaRenderer(Game game) {
    this.game = game;
  }

  public void drawChatArea() {
    game.fullScreenBackground.initDrawingArea();
    Texture.lineOffsets = game.chatAreaOffsets;
    game.chatBack.draw(0, 0);
    if (game.messagePromptRaised) {
      game.chatTextDrawingArea.textCenter(0, game.inputPrompt, 40, 239);
      game.chatTextDrawingArea.textCenter(128, game.promptInput + "*", 60, 239);
    } else if (game.inputDialogState == 1) {
      game.chatTextDrawingArea.textCenter(0, "Enter amount:", 40, 239);
      game.chatTextDrawingArea.textCenter(128, game.amountOrNameInput + "*", 60, 239);
    } else if (game.inputDialogState == 2) {
      game.chatTextDrawingArea.textCenter(0, "Enter name:", 40, 239);
      game.chatTextDrawingArea.textCenter(128, game.amountOrNameInput + "*", 60, 239);
    } else if (game.messagePrompt != null) {
      game.chatTextDrawingArea.textCenter(0, game.messagePrompt, 40, 239);
      game.chatTextDrawingArea.textCenter(128, "Click to continue", 60, 239);
    } else if (game.backDialogID != -1) {
      game.drawInterface(0, 0, RSInterface.interfaceCache[game.backDialogID], 0);
    } else if (game.dialogID != -1) {
      game.drawInterface(0, 0, RSInterface.interfaceCache[game.dialogID], 0);
    } else {
      TextDrawingArea textDrawingArea = game.boldFont;
      int j = 0;
      DrawingArea.setDrawingArea(77, 0, 463, 0);
      for (int k = 0; k < 100; k++) {
        if (game.chatMessages[k] != null) {
          int l = game.chatTypes[k];
          int i1 = 70 - j * 14 + game.chatScrollPosition;
          String s1 = game.chatNames[k];
          byte byte0 = 0;
          if (s1 != null && s1.startsWith("@cr1@")) {
            s1 = s1.substring(5);
            byte0 = 1;
          }
          if (s1 != null && s1.startsWith("@cr2@")) {
            s1 = s1.substring(5);
            byte0 = 2;
          }
          if (l == 0) {
            if (i1 > 0 && i1 < 110) {
              try {
                textDrawingArea.textLeftShadow(false, 4, 0, game.chatMessages[k], i1);
              } catch (Exception e) {
                // ignore drawing exceptions
              }
            }
            j++;
          }
          if ((l == 1 || l == 2)
              && (l == 1
                  || game.publicChatMode == 0
                  || game.publicChatMode == 1 && game.isFriendOrSelf(s1))) {
            if (i1 > 0 && i1 < 110) {
              int j1 = 4;
              if (byte0 == 1) {
                game.modIcons[0].draw(j1, i1 - 12);
                j1 += 14;
              }
              if (byte0 == 2) {
                game.modIcons[1].draw(j1, i1 - 12);
                j1 += 14;
              }
              textDrawingArea.textLeft(0, s1 + ":", i1, j1);
              j1 += textDrawingArea.getTextWidth(s1) + 8;
              textDrawingArea.textLeft(255, game.chatMessages[k], i1, j1);
            }
            j++;
          }
          if ((l == 3 || l == 7)
              && game.splitpublicChat == 0
              && (l == 7
                  || game.privateChatMode == 0
                  || game.privateChatMode == 1 && game.isFriendOrSelf(s1))) {
            if (i1 > 0 && i1 < 110) {
              int k1 = 4;
              textDrawingArea.textLeft(0, "From", i1, k1);
              k1 += textDrawingArea.getTextWidth("From ");
              if (byte0 == 1) {
                game.modIcons[0].draw(k1, i1 - 12);
                k1 += 14;
              }
              if (byte0 == 2) {
                game.modIcons[1].draw(k1, i1 - 12);
                k1 += 14;
              }
              textDrawingArea.textLeft(0, s1 + ":", i1, k1);
              k1 += textDrawingArea.getTextWidth(s1) + 8;
              textDrawingArea.textLeft(0x800000, game.chatMessages[k], i1, k1);
            }
            j++;
          }
          if (l == 4 && (game.tradeMode == 0 || game.tradeMode == 1 && game.isFriendOrSelf(s1))) {
            if (i1 > 0 && i1 < 110) {
              textDrawingArea.textLeft(0x800080, s1 + " " + game.chatMessages[k], i1, 4);
            }
            j++;
          }
          if (l == 5 && game.splitpublicChat == 0 && game.privateChatMode < 2) {
            if (i1 > 0 && i1 < 110) {
              textDrawingArea.textLeft(0x800000, game.chatMessages[k], i1, 4);
            }
            j++;
          }
          if (l == 6 && game.splitpublicChat == 0 && game.privateChatMode < 2) {
            if (i1 > 0 && i1 < 110) {
              textDrawingArea.textLeft(0, "To " + s1 + ":", i1, 4);
              textDrawingArea.textLeft(
                  0x800000,
                  game.chatMessages[k],
                  i1,
                  12 + textDrawingArea.getTextWidth("To " + s1));
            }
            j++;
          }
          if (l == 8 && (game.tradeMode == 0 || game.tradeMode == 1 && game.isFriendOrSelf(s1))) {
            if (i1 > 0 && i1 < 110) {
              textDrawingArea.textLeft(0x7e3200, s1 + " " + game.chatMessages[k], i1, 4);
            }
            j++;
          }
        }
      }

      DrawingArea.defaultDrawingAreaSize();
      game.chatScrollHeight = j * 14 + 7;
      if (game.chatScrollHeight < 78) {
        game.chatScrollHeight = 78;
      }
      game.drawScrollThumb(
          77, game.chatScrollHeight - game.chatScrollPosition - 77, 0, 463, game.chatScrollHeight);
      String s;
      if (game.myPlayer != null && game.myPlayer.name != null) {
        s = game.myPlayer.name;
      } else {
        s = TextClass.fixName(game.myUsername);
      }
      textDrawingArea.textLeft(0, s + ":", 90, 4);
      textDrawingArea.textLeft(
          255, game.inputString + "*", 90, 6 + textDrawingArea.getTextWidth(s + ": "));
      DrawingArea.drawHorizontalLine(77, 0, 479, 0);
    }
    if (game.menuOpen && game.menuScreenArea == 2) {
      game.drawMenu();
    }
    game.fullScreenBackground.drawGraphics(357, game.graphics, 17);
    game.tabAreaBuffer.initDrawingArea();
    Texture.lineOffsets = game.chatBoxAreaOffsets;
  }

  /** Build context menu entries for chat area messages. */
  public void buildChatAreaMenu(int j) {
    int l = 0;
    for (int i1 = 0; i1 < 100; i1++) {
      if (game.chatMessages[i1] == null) {
        continue;
      }
      int j1 = game.chatTypes[i1];
      int k1 = 70 - l * 14 + game.chatScrollPosition + 4;
      if (k1 < -20) {
        break;
      }
      String s = game.chatNames[i1];
      if (s != null && s.startsWith("@cr1@")) {
        s = s.substring(5);
      }
      if (s != null && s.startsWith("@cr2@")) {
        s = s.substring(5);
      }
      if (j1 == 0) {
        l++;
      }
      if ((j1 == 1 || j1 == 2)
          && (j1 == 1 || game.publicChatMode == 0 || game.publicChatMode == 1 && game.isFriendOrSelf(s))) {
        if (j > k1 - 14 && j <= k1 && !s.equals(game.myPlayer.name)) {
          if (game.myPrivilege >= 1 && game.myPrivilege <= 3) {
            game.menuActionName[game.menuActionRow] = "Report abuse @whi@" + s;
            game.menuActionID[game.menuActionRow] = 606;
            game.menuActionRow++;
          }
          game.menuActionName[game.menuActionRow] = "Add ignore @whi@" + s;
          game.menuActionID[game.menuActionRow] = 42;
          game.menuActionRow++;
          game.menuActionName[game.menuActionRow] = "Reply to @whi@" + s;
          game.menuActionID[game.menuActionRow] = 639;
          game.menuActionRow++;
          game.menuActionName[game.menuActionRow] = "Add friend @whi@" + s;
          game.menuActionID[game.menuActionRow] = 337;
          game.menuActionRow++;
        }
        l++;
      }
      if ((j1 == 3 || j1 == 7)
          && game.splitpublicChat == 0
          && (j1 == 7 || game.privateChatMode == 0 || game.publicChatMode == 1 && game.isFriendOrSelf(s))) {
        if (j > k1 - 14 && j <= k1) {
          if (game.myPrivilege >= 1 && game.myPrivilege <= 3) {
            game.menuActionName[game.menuActionRow] = "Report abuse @whi@" + s;
            game.menuActionID[game.menuActionRow] = 606;
            game.menuActionRow++;
          }
          game.menuActionName[game.menuActionRow] = "Add ignore @whi@" + s;
          game.menuActionID[game.menuActionRow] = 42;
          game.menuActionRow++;
          game.menuActionName[game.menuActionRow] = "Reply to @whi@" + s;
          game.menuActionID[game.menuActionRow] = 639;
          game.menuActionRow++;
          game.menuActionName[game.menuActionRow] = "Add friend @whi@" + s;
          game.menuActionID[game.menuActionRow] = 337;
          game.menuActionRow++;
        }
        l++;
      }
      if (j1 == 4 && (game.tradeMode == 0 || game.tradeMode == 1 && game.isFriendOrSelf(s))) {
        if (j > k1 - 14 && j <= k1) {
          game.menuActionName[game.menuActionRow] = "Accept trade @whi@" + s;
          game.menuActionID[game.menuActionRow] = 484;
          game.menuActionRow++;
        }
        l++;
      }
      if ((j1 == 5 || j1 == 6) && game.splitpublicChat == 0 && game.privateChatMode < 2) {
        l++;
      }
      if (j1 == 8 && (game.tradeMode == 0 || game.tradeMode == 1 && game.isFriendOrSelf(s))) {
        if (j > k1 - 14 && j <= k1) {
          game.menuActionName[game.menuActionRow] = "Accept challenge @whi@" + s;
          game.menuActionID[game.menuActionRow] = 6;
          game.menuActionRow++;
        }
        l++;
      }
    }
  }

  /** Draw split private chat messages above the game viewport. */
  public void drawSplitpublicChat() {
    if (game.splitpublicChat == 0) {
      return;
    }
    TextDrawingArea textDrawingArea = game.boldFont;
    int i = 0;
    if (game.systemUpdateTimer != 0) {
      i = 1;
    }
    for (int j = 0; j < 100; j++) {
      if (game.chatMessages[j] != null) {
        int k = game.chatTypes[j];
        String s = game.chatNames[j];
        byte byte1 = 0;
        if (s != null && s.startsWith("@cr1@")) {
          s = s.substring(5);
          byte1 = 1;
        }
        if (s != null && s.startsWith("@cr2@")) {
          s = s.substring(5);
          byte1 = 2;
        }
        if ((k == 3 || k == 7)
            && (k == 7 || game.privateChatMode == 0 || game.privateChatMode == 1 && game.isFriendOrSelf(s))) {
          int l = 329 - i * 13;
          int k1 = 4;
          textDrawingArea.textLeft(0, "From", l, k1);
          textDrawingArea.textLeft(0x00ffff, "From", l - 1, k1);
          k1 += textDrawingArea.getTextWidth("From ");
          if (byte1 == 1) {
            game.modIcons[0].draw(k1, l - 12);
            k1 += 14;
          }
          if (byte1 == 2) {
            game.modIcons[1].draw(k1, l - 12);
            k1 += 14;
          }
          textDrawingArea.textLeft(0, s + ": " + game.chatMessages[j], l, k1);
          textDrawingArea.textLeft(0x00ffff, s + ": " + game.chatMessages[j], l - 1, k1);
          if (++i >= 5) {
            return;
          }
        }
        if (k == 5 && game.privateChatMode < 2) {
          int i1 = 329 - i * 13;
          textDrawingArea.textLeft(0, game.chatMessages[j], i1, 4);
          textDrawingArea.textLeft(0x00ffff, game.chatMessages[j], i1 - 1, 4);
          if (++i >= 5) {
            return;
          }
        }
        if (k == 6 && game.privateChatMode < 2) {
          int j1 = 329 - i * 13;
          textDrawingArea.textLeft(0, "To " + s + ": " + game.chatMessages[j], j1, 4);
          textDrawingArea.textLeft(0x00ffff, "To " + s + ": " + game.chatMessages[j], j1 - 1, 4);
          if (++i >= 5) {
            return;
          }
        }
      }
    }
  }

  /** Builds the private chat context menu when split chat is enabled. */
  public void buildSplitPrivateChatMenu() {
    if (game.splitpublicChat == 0) {
      return;
    }
    int lines = 0;
    if (game.systemUpdateTimer != 0) {
      lines = 1;
    }
    for (int j = 0; j < 100; j++) {
      if (game.chatMessages[j] != null) {
        int type = game.chatTypes[j];
        String name = game.chatNames[j];
        if (name != null && name.startsWith("@cr1@")) {
          name = name.substring(5);
        }
        if (name != null && name.startsWith("@cr2@")) {
          name = name.substring(5);
        }
        if ((type == 3 || type == 7)
            && (type == 7
                || game.privateChatMode == 0
                || game.privateChatMode == 1 && game.isFriendOrSelf(name))) {
          int y = 329 - lines * 13;
          if (game.mouseX > 4 && game.mouseY - 4 > y - 10 && game.mouseY - 4 <= y + 3) {
            int width = game.boldFont.getTextWidth("From:  " + name + game.chatMessages[j]) + 25;
            if (width > 450) {
              width = 450;
            }
            if (game.mouseX < 4 + width) {
              if (game.myPrivilege >= 1 && game.myPrivilege <= 3) {
                game.menuActionName[game.menuActionRow] = "Report abuse @whi@" + name;
                game.menuActionID[game.menuActionRow] = 2606;
                game.menuActionRow++;
              }
              game.menuActionName[game.menuActionRow] = "Add ignore @whi@" + name;
              game.menuActionID[game.menuActionRow] = 2042;
              game.menuActionRow++;
              game.menuActionName[game.menuActionRow] = "Reply to @whi@" + name;
              game.menuActionID[game.menuActionRow] = 2639;
              game.menuActionRow++;
              game.menuActionName[game.menuActionRow] = "Add friend @whi@" + name;
              game.menuActionID[game.menuActionRow] = 2337;
              game.menuActionRow++;
            }
          }
          if (++lines >= 5) {
            return;
          }
        }
        if ((type == 5 || type == 6) && game.privateChatMode < 2 && ++lines >= 5) {
          return;
        }
      }
    }
  }
}
