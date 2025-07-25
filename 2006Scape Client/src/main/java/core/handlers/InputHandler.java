package core.handlers;

import core.engine.ClientSettings;
import core.engine.Game;
import core.world.WorldController;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import util.helpers.ClipboardUtil;
import util.helpers.ScreenshotUtil;
import util.helpers.Censor;
import util.helpers.DefinitionSearcher;
import ui.TextClass;
import ui.TextInput;

/** Processes keyboard and mouse wheel input extracted from {@link Game}. */
public final class InputHandler {
  private InputHandler() {}

  public static void handleKeyPressed(Game game, KeyEvent keyevent) {
    switch (keyevent.getKeyCode()) {
      case KeyEvent.VK_ESCAPE:
        game.closeOpenInterfaces();
        break;
      case KeyEvent.VK_F1:
        game.needDrawTabArea = true;
        game.tabID = 3;
        game.tabAreaAltered = true;
        break;
      case KeyEvent.VK_F2:
        game.needDrawTabArea = true;
        game.tabID = 4;
        game.tabAreaAltered = true;
        break;
      case KeyEvent.VK_F3:
        game.needDrawTabArea = true;
        game.tabID = 5;
        game.tabAreaAltered = true;
        break;
      case KeyEvent.VK_F4:
        game.needDrawTabArea = true;
        game.tabID = 6;
        game.tabAreaAltered = true;
        break;
      case KeyEvent.VK_F5:
        game.needDrawTabArea = true;
        game.tabID = 0;
        game.tabAreaAltered = true;
        break;
      case KeyEvent.VK_F6:
        game.needDrawTabArea = true;
        game.tabID = 1;
        game.tabAreaAltered = true;
        break;
      case KeyEvent.VK_F7:
        game.needDrawTabArea = true;
        game.tabID = 2;
        game.tabAreaAltered = true;
        break;
      case KeyEvent.VK_F8:
        game.needDrawTabArea = true;
        game.tabID = 8;
        game.tabAreaAltered = true;
        break;
      case KeyEvent.VK_F9:
        game.needDrawTabArea = true;
        game.tabID = 11;
        game.tabAreaAltered = true;
        break;
      case KeyEvent.VK_F10:
        game.needDrawTabArea = true;
        game.tabID = 12;
        game.tabAreaAltered = true;
        break;
      case KeyEvent.VK_F11:
        game.needDrawTabArea = true;
        game.tabID = 13;
        game.tabAreaAltered = true;
        break;
      case KeyEvent.VK_F12:
        game.needDrawTabArea = true;
        game.tabID = 10;
        game.tabAreaAltered = true;
        break;
      case KeyEvent.VK_PAGE_UP:
        if (Game.zoom > -1) {
          Game.zoom--;
          if (ClientSettings.SHOW_ZOOM_LEVEL_MESSAGES) {
            game.pushMessage("Your zoom level is now: " + Game.zoom, 0, "");
          }
        }
        break;
      case KeyEvent.VK_PAGE_DOWN:
        if (Game.zoom < (WorldController.drawDistance / 3)) {
          Game.zoom++;
          if (ClientSettings.SHOW_ZOOM_LEVEL_MESSAGES) {
            game.pushMessage("Your zoom level is now: " + Game.zoom, 0, "");
          }
        }
        break;
      case KeyEvent.VK_V:
        if (keyevent.isControlDown()) {
          game.inputString += ClipboardUtil.getClipboardText();
          if (game.inputString.length() > 80) {
            game.inputString = game.inputString.substring(0, 80);
          }
          game.inputTaken = true;
        }
        break;
      default:
        break;
    }
    if (ClientSettings.SCREENSHOTS_ENABLED
        && keyevent.getKeyCode() == KeyEvent.VK_PRINTSCREEN
        && keyevent.isControlDown()) {
      ScreenshotUtil.capture(game, true);
    }
  }

  public static void handleMouseWheelMoved(Game game, MouseWheelEvent e) {
    int notches = e.getWheelRotation();
    if (ClientSettings.CONTROL_KEY_ZOOMING && !e.isControlDown()) {
      return;
    }
    if (game.openInterfaceID == -1 && game.mouseX < 515 && game.mouseY < 340) {
      if (notches < 0) {
        if (Game.zoom > -1) {
          Game.zoom--;
          if (ClientSettings.SHOW_ZOOM_LEVEL_MESSAGES) {
            game.pushMessage("Your zoom level is now: " + Game.zoom, 0, "");
          }
        }
      } else {
        if (Game.zoom < (WorldController.drawDistance / 3)) {
          Game.zoom++;
          if (ClientSettings.SHOW_ZOOM_LEVEL_MESSAGES) {
            game.pushMessage("Your zoom level is now: " + Game.zoom, 0, "");
          }
        }
      }
    }
  }

  public static void handleMouseWheelDragged(Game game, int dx, int dy) {
    if (!game.mouseWheelDown) {
      return;
    }
    game.cameraYawAccel += dx * 3;
    game.cameraPitchAccel += (dy << 1);
  }

  public static void processInput(Game game) {
    do {
      int j = game.readChar(-796);
      if (j == -1) {
        break;
      }
      if (game.customTabAction == 1 || game.customTabAction == 2) {
        if (j >= 48
            && j <= 57
            && game.promptInput.length() < 10
            && !game.promptInput.toLowerCase().contains("k")
            && !game.promptInput.toLowerCase().contains("m")
            && !game.promptInput.toLowerCase().contains("b")) {
          game.promptInput += (char) j;
          game.inputTaken = true;
        }
        if ((!game.promptInput.toLowerCase().contains("k")
                && !game.promptInput.toLowerCase().contains("m")
                && !game.promptInput.toLowerCase().contains("b"))
                && (j == 107 || j == 109)
            || j == 98) {
          game.promptInput += (char) j;
          game.inputTaken = true;
        }
        if (j == 8 && game.promptInput.length() > 0) {
          game.promptInput = game.promptInput.substring(0, game.promptInput.length() - 1);
          game.inputTaken = true;
        }
        try {
          if (j == 13 || j == 10) {
            if (game.promptInput.length() > 0) {
              if (game.promptInput.toLowerCase().contains("k")) {
                game.promptInput = game.promptInput.replaceAll("k", "000");
              } else if (game.promptInput.toLowerCase().contains("m")) {
                game.promptInput = game.promptInput.replaceAll("m", "000000");
              } else if (game.promptInput.toLowerCase().contains("b")) {
                game.promptInput = game.promptInput.replaceAll("b", "000000000");
              }
              if (game.customTabAction == 1) {
                Game.customSettingMinItemValue = Integer.parseInt(game.promptInput);
              }
              if (game.customTabAction == 2) {
                WorldController.drawDistance =
                    Math.max(10, Math.min(100, Integer.parseInt(game.promptInput)));
                Game.zoom = Math.min(Game.zoom, WorldController.drawDistance / 3);
              }
            }
            game.customTabAction = 0;
            game.inputTaken = true;
            game.messagePromptRaised = false;
            game.drawTabArea();
          }
        } catch (NumberFormatException nfe) {
          game.customTabAction = 0;
          game.inputTaken = true;
          game.messagePromptRaised = false;
          game.pushMessage("Please enter a lower amount.", 0, "");
        }
      } else if (game.openInterfaceID != -1 && game.openInterfaceID == game.reportAbuseInterfaceID) {
        if (j == 8 && game.reportAbuseInput.length() > 0) {
          game.reportAbuseInput = game.reportAbuseInput.substring(0, game.reportAbuseInput.length() - 1);
        }
        if ((j >= 97 && j <= 122 || j >= 65 && j <= 90 || j >= 48 && j <= 57 || j == 32)
            && game.reportAbuseInput.length() < 12) {
          game.reportAbuseInput += (char) j;
        }
      } else if (game.messagePromptRaised) {
        if (j >= 32 && j <= 122 && game.promptInput.length() < 80) {
          game.promptInput += (char) j;
          game.inputTaken = true;
        }
        if (j == 8 && game.promptInput.length() > 0) {
          game.promptInput = game.promptInput.substring(0, game.promptInput.length() - 1);
          game.inputTaken = true;
        }
        if (j == 13 || j == 10) {
          game.messagePromptRaised = false;
          game.inputTaken = true;
          if (game.friendsListAction == 1) {
            long l = TextClass.longForName(game.promptInput);
            game.addFriend(l);
          }
          if (game.friendsListAction == 2 && game.friendsCount > 0) {
            long l1 = TextClass.longForName(game.promptInput);
            game.delFriend(l1);
          }
          if (game.friendsListAction == 3 && game.promptInput.length() > 0) {
            game.stream.createFrame(126);
            game.stream.writeWordBigEndian(0);
            int k = game.stream.currentOffset;
            game.stream.writeQWord(game.privateMessageRecipient);
            TextInput.encodeChatMessage(game.promptInput, game.stream);
            game.stream.writeBytes(game.stream.currentOffset - k);
            game.promptInput = TextInput.processText(game.promptInput);
            game.promptInput = Censor.doCensor(game.promptInput);
            game.pushMessage(
                game.promptInput,
                6,
                TextClass.fixName(TextClass.nameForLong(game.privateMessageRecipient)));
            if (game.privateChatMode == 2) {
              game.privateChatMode = 1;
              game.chatSettingsUpdateNeeded = true;
              game.stream.createFrame(95);
              game.stream.writeWordBigEndian(game.publicChatMode);
              game.stream.writeWordBigEndian(game.privateChatMode);
              game.stream.writeWordBigEndian(game.tradeMode);
            }
          }
          if (game.friendsListAction == 4 && game.ignoreCount < 100) {
            long l2 = TextClass.longForName(game.promptInput);
            game.addIgnore(l2);
          }
          if (game.friendsListAction == 5 && game.ignoreCount > 0) {
            long l3 = TextClass.longForName(game.promptInput);
            game.delIgnore(l3);
          }
        }
      } else if (game.inputDialogState == 1) {
        if (j >= 48
            && j <= 57
            && game.amountOrNameInput.length() < 10
            && !game.amountOrNameInput.toLowerCase().contains("k")
            && !game.amountOrNameInput.toLowerCase().contains("m")
            && !game.amountOrNameInput.toLowerCase().contains("b")) {
          game.amountOrNameInput += (char) j;
          game.inputTaken = true;
        }
        if ((!game.amountOrNameInput.toLowerCase().contains("k")
                && !game.amountOrNameInput.toLowerCase().contains("m")
                && !game.amountOrNameInput.toLowerCase().contains("b"))
                && (j == 107 || j == 109)
            || j == 98) {
          game.amountOrNameInput += (char) j;
          game.inputTaken = true;
        }
        if (j == 8 && game.amountOrNameInput.length() > 0) {
          game.amountOrNameInput = game.amountOrNameInput.substring(0, game.amountOrNameInput.length() - 1);
          game.inputTaken = true;
        }
        try {
          if (j == 13 || j == 10) {
            if (game.amountOrNameInput.length() > 0) {
              if (game.amountOrNameInput.toLowerCase().contains("k")) {
                game.amountOrNameInput = game.amountOrNameInput.replaceAll("k", "000");
              } else if (game.amountOrNameInput.toLowerCase().contains("m")) {
                game.amountOrNameInput = game.amountOrNameInput.replaceAll("m", "000000");
              } else if (game.amountOrNameInput.toLowerCase().contains("b")) {
                game.amountOrNameInput = game.amountOrNameInput.replaceAll("b", "000000000");
              }
              int amount = 0;
              amount = Integer.parseInt(game.amountOrNameInput);
              game.stream.createFrame(208);
              game.stream.writeDWord(amount);
            }
            game.inputDialogState = 0;
            game.inputTaken = true;
          }
        } catch (NumberFormatException nfe) {
          game.inputDialogState = 0;
          game.inputTaken = true;
          game.pushMessage("Please enter a lower amount.", 0, "");
        }
      } else if (game.inputDialogState == 2) {
        if (j >= 32 && j <= 122 && game.amountOrNameInput.length() < 12) {
          game.amountOrNameInput += (char) j;
          game.inputTaken = true;
        }
        if (j == 8 && game.amountOrNameInput.length() > 0) {
          game.amountOrNameInput = game.amountOrNameInput.substring(0, game.amountOrNameInput.length() - 1);
          game.inputTaken = true;
        }
        if (j == 13 || j == 10) {
          if (game.amountOrNameInput.length() > 0) {
            game.stream.createFrame(60);
            game.stream.writeQWord(TextClass.longForName(game.amountOrNameInput));
          }
          game.inputDialogState = 0;
          game.inputTaken = true;
        }
      } else {
        if (j >= 32 && j <= 122 && game.inputString.length() < 80) {
          game.inputString += (char) j;
          game.inputTaken = true;
          if (game.inputString.startsWith("::search")) {
            String[] args = game.inputString.split(" ");
            game.inputDialogState = 3;
            int searchType = 1;
            String searchString = "";
            if (args.length < 2) {
              return;
            }
            try {
              searchType = Integer.parseInt(args[1]);
              if (args.length >= 3) {
                searchString =
                    game.inputString.substring(game.inputString.indexOf(args[1]) + args[1].length() + 1);
              }
            } catch (Exception e) {
              searchType = 1;
              searchString = game.inputString.substring(args[0].length() + 1);
            }
            DefinitionSearcher.search(game, searchString, searchType);
          }
        }
        if (j == 8 && game.inputString.length() > 0) {
          game.inputString = game.inputString.substring(0, game.inputString.length() - 1);
          game.inputTaken = true;
          if (game.inputString.startsWith("::search")) {
            String[] args = game.inputString.split(" ");
            game.inputDialogState = 3;
            int searchType = 1;
            String searchString = "";
            if (args.length < 2) {
              return;
            }
            try {
              searchType = Integer.parseInt(args[1]);
              if (args.length >= 3) {
                searchString =
                    game.inputString.substring(game.inputString.indexOf(args[1]) + args[1].length() + 1);
              }
            } catch (Exception e) {
              searchType = 1;
              searchString = game.inputString.substring(args[0].length() + 1);
            }
            DefinitionSearcher.search(game, searchString, searchType);
          }
        }
        if ((j == 13 || j == 10) && game.inputString.length() > 0) {
          if (game.inputString.equals("::gfxtgl")
              || game.inputString.equals("::tglgfx")
              || game.inputString.equals("::togglerender")
              || game.inputString.equals("::togglegfx")) {
            game.graphicsEnabled = !game.graphicsEnabled;
          }
          if (game.inputString.equals("::crtlkeyzoom") || game.inputString.equals("::controlkeyzoom")) {
            ClientSettings.CONTROL_KEY_ZOOMING = !ClientSettings.CONTROL_KEY_ZOOMING;
            game.pushMessage(
                "Your control key zooming is now: "
                    + (ClientSettings.CONTROL_KEY_ZOOMING ? "enabled" : "disabled"),
                0,
                "");
          }
          if (game.myPrivilege >= 2) {
            if (game.inputString.equals("::noclip"))
              for (int k1 = 0; k1 < 4; k1++)
                for (int i2 = 1; i2 < 103; i2++)
                  for (int k2 = 1; k2 < 103; k2++) game.collisionMaps[k1].clippingFlags[i2][k2] = 0;
            if (game.inputString.equals("::clientdrop")) {
              game.dropClient();
            }
            if (game.inputString.equals("::lag")) {
              game.printDebug();
            }
            if (game.inputString.startsWith("::int")) {
              String[] args = game.inputString.split(" ");
              int interfaceID = 1;
              try {
                interfaceID = Integer.parseInt(args[1]);
              } catch (Exception e) {
                interfaceID = 1;
              }
              game.openInterface(interfaceID);
              game.inputString = "";
              game.inputTaken = true;
              return;
            }
            if (game.inputString.equals("::mg")) {
              if (game.tabInterfaceIDs[6] == 12855) game.openSideInterface(6, 1151);
              else game.openSideInterface(6, 12855);
              game.inputString = "";
              game.inputTaken = true;
              return;
            }
            if (game.inputString.equals("::prefetchmusic")) {
              for (int j1 = 0; j1 < game.onDemandFetcher.getVersionCount(2); j1++) {
                game.onDemandFetcher.validateOrQueue((byte) 1, 2, j1);
              }
            }
          }
          if (game.inputString.startsWith("::dd")) {
            String[] args = game.inputString.split(" ");
            int distance = 25;
            try {
              distance = Math.max(10, Math.min(100, Integer.parseInt(args[1])));
            } catch (Exception e) {
              distance = 25;
            }
            WorldController.drawDistance = distance;
            if (Game.zoom > (WorldController.drawDistance / 3)) Game.zoom = WorldController.drawDistance / 3;
            game.inputString = "";
            game.inputTaken = true;
            return;
          }
          if (game.inputString.equals("::dataon")) {
            game.showInfo = !game.showInfo;
          }
          if (game.inputString.startsWith("::")) {
            game.stream.createFrame(103);
            game.stream.writeWordBigEndian(game.inputString.length() - 1);
            game.stream.writeString(game.inputString.substring(2));
          } else {
            String s = game.inputString.toLowerCase();
            int j2 = 0;
            if (s.startsWith("yellow:")) {
              j2 = 0;
              game.inputString = game.inputString.substring(7);
            } else if (s.startsWith("red:")) {
              j2 = 1;
              game.inputString = game.inputString.substring(4);
            } else if (s.startsWith("green:")) {
              j2 = 2;
              game.inputString = game.inputString.substring(6);
            } else if (s.startsWith("cyan:")) {
              j2 = 3;
              game.inputString = game.inputString.substring(5);
            } else if (s.startsWith("purple:")) {
              j2 = 4;
              game.inputString = game.inputString.substring(7);
            } else if (s.startsWith("white:")) {
              j2 = 5;
              game.inputString = game.inputString.substring(6);
            } else if (s.startsWith("flash1:")) {
              j2 = 6;
              game.inputString = game.inputString.substring(7);
            } else if (s.startsWith("flash2:")) {
              j2 = 7;
              game.inputString = game.inputString.substring(7);
            } else if (s.startsWith("flash3:")) {
              j2 = 8;
              game.inputString = game.inputString.substring(7);
            } else if (s.startsWith("glow1:")) {
              j2 = 9;
              game.inputString = game.inputString.substring(6);
            } else if (s.startsWith("glow2:")) {
              j2 = 10;
              game.inputString = game.inputString.substring(6);
            } else if (s.startsWith("glow3:")) {
              j2 = 11;
              game.inputString = game.inputString.substring(6);
            }
            s = game.inputString.toLowerCase();
            int i3 = 0;
            if (s.startsWith("wave:")) {
              i3 = 1;
              game.inputString = game.inputString.substring(5);
            } else if (s.startsWith("wave2:")) {
              i3 = 2;
              game.inputString = game.inputString.substring(6);
            } else if (s.startsWith("shake:")) {
              i3 = 3;
              game.inputString = game.inputString.substring(6);
            } else if (s.startsWith("scroll:")) {
              i3 = 4;
              game.inputString = game.inputString.substring(7);
            } else if (s.startsWith("slide:")) {
              i3 = 5;
              game.inputString = game.inputString.substring(6);
            }
            game.stream.createFrame(4);
            game.stream.writeWordBigEndian(0);
            int j3 = game.stream.currentOffset;
            game.stream.writeByteSub(i3);
            game.stream.writeByteSub(j2);
            game.chatBuffer.currentOffset = 0;
            TextInput.encodeChatMessage(game.inputString, game.chatBuffer);
            game.stream.writeBytesReverseAdd(0, game.chatBuffer.buffer, game.chatBuffer.currentOffset);
            game.stream.writeBytes(game.stream.currentOffset - j3);
            game.inputString = TextInput.processText(game.inputString);
            game.inputString = Censor.doCensor(game.inputString);
            game.myPlayer.textSpoken = game.inputString;
            game.myPlayer.chatColor = j2;
            game.myPlayer.chatEffect = i3;
            game.myPlayer.textCycle = 150;
            if (game.myPrivilege >= 1) {
              game.pushMessage(
                  game.myPlayer.textSpoken,
                  2,
                  "@cr" + Math.min(2, game.myPrivilege) + "@" + game.myPlayer.name);
            } else {
              game.pushMessage(game.myPlayer.textSpoken, 2, game.myPlayer.name);
            }
            if (game.publicChatMode == 2) {
              game.publicChatMode = 3;
              game.chatSettingsUpdateNeeded = true;
              game.stream.createFrame(95);
              game.stream.writeWordBigEndian(game.publicChatMode);
              game.stream.writeWordBigEndian(game.privateChatMode);
              game.stream.writeWordBigEndian(game.tradeMode);
            }
          }
          game.inputString = "";
          game.inputTaken = true;
        }
      }
    } while (true);
  }
}
