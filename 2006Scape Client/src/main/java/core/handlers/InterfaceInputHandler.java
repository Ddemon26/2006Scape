package core.handlers;

import core.engine.Game;
import core.engine.ClientSettings;
import render.tiles.FloorOverlay;
import ui.RSInterface;
import ui.TextClass;
import util.configuration.IDK;
import util.helpers.ScreenshotUtil;
import entities.PlayerStatsCalculator;

/** Handles interface widget input processing extracted from {@link Game}. */
public final class InterfaceInputHandler {
  private final Game game;

  public InterfaceInputHandler(Game game) {
    this.game = game;
  }

  public boolean promptUserForInput(RSInterface widget) {
    int j = widget.contentType;
    if (game.interfaceMode == 2) {
      if (j == 201) {
        game.inputTaken = true;
        game.inputDialogState = 0;
        game.messagePromptRaised = true;
        game.promptInput = "";
        game.friendsListAction = 1;
        game.inputPrompt = "Enter name of friend to add to list";
      }
      if (j == 202) {
        game.inputTaken = true;
        game.inputDialogState = 0;
        game.messagePromptRaised = true;
        game.promptInput = "";
        game.friendsListAction = 2;
        game.inputPrompt = "Enter name of friend to delete from list";
      }
    }
    if (j == 205) {
      game.reconnectDelay = 250;
      return true;
    }
    if (j == 501) {
      game.inputTaken = true;
      game.inputDialogState = 0;
      game.messagePromptRaised = true;
      game.promptInput = "";
      game.friendsListAction = 4;
      game.inputPrompt = "Enter name of player to add to list";
    }
    if (j == 502) {
      game.inputTaken = true;
      game.inputDialogState = 0;
      game.messagePromptRaised = true;
      game.promptInput = "";
      game.friendsListAction = 5;
      game.inputPrompt = "Enter name of player to delete from list";
    }
    if (j >= 300 && j <= 313) {
      int k = (j - 300) / 2;
      int j1 = j & 1;
      int i2 = game.characterStyle[k];
      if (i2 != -1) {
        do {
          if (j1 == 0 && --i2 < 0) {
            i2 = IDK.length - 1;
          }
          if (j1 == 1 && ++i2 >= IDK.length) {
            i2 = 0;
          }
        } while (IDK.cache[i2].nonSelectable
            || IDK.cache[i2].bodyPartId != k + (game.isMaleCharacter ? 0 : 7));
        game.characterStyle[k] = i2;
        game.characterDesignChanged = true;
      }
    }
    if (j >= 314 && j <= 323) {
      int l = (j - 314) / 2;
      int k1 = j & 1;
      int j2 = game.characterColorIndices[l];
      if (k1 == 0 && --j2 < 0) {
        j2 = game.appearanceColorOptions[l].length - 1;
      }
      if (k1 == 1 && ++j2 >= game.appearanceColorOptions[l].length) {
        j2 = 0;
      }
      game.characterColorIndices[l] = j2;
      game.characterDesignChanged = true;
    }
    if (j == 324 && !game.isMaleCharacter) {
      game.isMaleCharacter = true;
      game.resetCharacterOptions();
    }
    if (j == 325 && game.isMaleCharacter) {
      game.isMaleCharacter = false;
      game.resetCharacterOptions();
    }
    if (j == 326) {
      game.stream.createFrame(101);
      game.stream.writeWordBigEndian(game.isMaleCharacter ? 0 : 1);
      for (int i1 = 0; i1 < 7; i1++) {
        game.stream.writeWordBigEndian(game.characterStyle[i1]);
      }

      for (int l1 = 0; l1 < 5; l1++) {
        game.stream.writeWordBigEndian(game.characterColorIndices[l1]);
      }

      return true;
    }
    if (j == 613) {
      game.canMute = !game.canMute;
    }
    if (j >= 601 && j <= 612) {
      game.closeOpenInterfaces();
      if (game.reportAbuseInput.length() > 0) {
        game.stream.createFrame(218);
        game.stream.writeQWord(TextClass.longForName(game.reportAbuseInput));
        game.stream.writeWordBigEndian(j - 601);
        game.stream.writeWordBigEndian(game.canMute ? 1 : 0);
      }
    }
    return false;
  }

  public void handleScrollbarInput(
      int i,
      int j,
      int k,
      int l,
      RSInterface scrollInterface,
      int i1,
      boolean flag,
      int j1) {
    int scrollPadding;
    if (game.scrollBarDragging) {
      scrollPadding = 32;
    } else {
      scrollPadding = 0;
    }
    game.scrollBarDragging = false;
    if (k >= i && k < i + 16 && l >= i1 && l < i1 + 16) {
      scrollInterface.scrollPosition -= game.clickCycle * 4;
      if (flag) {
        game.needDrawTabArea = true;
      }
    } else if (k >= i && k < i + 16 && l >= i1 + j - 16 && l < i1 + j) {
      scrollInterface.scrollPosition += game.clickCycle * 4;
      if (flag) {
        game.needDrawTabArea = true;
      }
    } else if (k >= i - scrollPadding
        && k < i + 16 + scrollPadding
        && l >= i1 + 16
        && l < i1 + j - 16
        && game.clickCycle > 0) {
      int l1 = (j - 32) * j / j1;
      if (l1 < 8) {
        l1 = 8;
      }
      int i2 = l - i1 - 16 - l1 / 2;
      int j2 = j - 32 - l1;
      scrollInterface.scrollPosition = (j1 - j) * i2 / j2;
      if (flag) {
        game.needDrawTabArea = true;
      }
      game.scrollBarDragging = true;
    }
  }

  public void resetInterfaceAnimation(int i) {
    RSInterface parentInterface = RSInterface.interfaceCache[i];
    if (parentInterface == null || parentInterface.children == null) return;
    for (int element : parentInterface.children) {
      if (element == -1) {
        break;
      }
      RSInterface childWidget = RSInterface.interfaceCache[element];
      if (childWidget.type == 1) {
        resetInterfaceAnimation(childWidget.id);
      }
      childWidget.animationFrame = 0;
      childWidget.animationCycle = 0;
    }
  }

  public void openInterface(int interfaceID) {
    resetInterfaceAnimation(interfaceID);
    if (game.invOverlayInterfaceID != -1) {
      game.invOverlayInterfaceID = -1;
      game.needDrawTabArea = true;
      game.tabAreaAltered = true;
    }
    if (game.backDialogID != -1) {
      game.backDialogID = -1;
      game.inputTaken = true;
    }
    if (game.inputDialogState != 0) {
      game.inputDialogState = 0;
      game.inputTaken = true;
    }
    if (interfaceID == 15244) {
      if (ClientSettings.SNOW_OVERLAY_FORCE_ENABLED
          || (ClientSettings.SNOW_OVERLAY_ENABLED
              && FloorOverlay.getTodaysDate().contains(ClientSettings.SNOW_MONTH))) {
        game.openInterfaceID = 15819;
      } else {
        game.openInterfaceID = 15801;
      }
      game.fullScreenInterfaceId = 15244;
    } else {
      game.openInterfaceID = interfaceID;
    }
    game.actionPending = false;
  }

  public void openSideInterface(int tab, int interfaceID) {
    if (interfaceID == 0x00ffff) {
      interfaceID = -1;
    }
    game.tabInterfaceIDs[tab] = interfaceID;
    game.needDrawTabArea = true;
    game.tabAreaAltered = true;
  }

  /** Handles tab clicking logic previously in {@link Game}. */
  public void processTabClick() {
    if (game.clickMode3 != 1) {
      return;
    }
    if (game.saveClickX >= 539
        && game.saveClickX <= 573
        && game.saveClickY >= 169
        && game.saveClickY < 205
        && game.tabInterfaceIDs[0] != -1) {
      game.needDrawTabArea = true;
      game.tabID = 0;
      game.tabAreaAltered = true;
    }
    if (game.saveClickX >= 569
        && game.saveClickX <= 599
        && game.saveClickY >= 168
        && game.saveClickY < 205
        && game.tabInterfaceIDs[1] != -1) {
      game.needDrawTabArea = true;
      game.tabID = 1;
      game.tabAreaAltered = true;
      if (ClientSettings.SCREENSHOTS_ENABLED && ClientSettings.AUTOMATIC_SCREENSHOTS_ENABLED) {
        java.util.Timer timer = new java.util.Timer();
        java.util.TimerTask delayedScreenshot =
            new java.util.TimerTask() {
              @Override
              public void run() {
                ScreenshotUtil.capture(game, false, "stats");
              }
            };
        timer.schedule(delayedScreenshot, 300);
      }
    }
    if (game.saveClickX >= 597
        && game.saveClickX <= 627
        && game.saveClickY >= 168
        && game.saveClickY < 205
        && game.tabInterfaceIDs[2] != -1) {
      game.needDrawTabArea = true;
      game.tabID = 2;
      game.tabAreaAltered = true;
    }
    if (game.saveClickX >= 625
        && game.saveClickX <= 669
        && game.saveClickY >= 168
        && game.saveClickY < 203
        && game.tabInterfaceIDs[3] != -1) {
      game.needDrawTabArea = true;
      game.tabID = 3;
      game.tabAreaAltered = true;
    }
    if (game.saveClickX >= 666
        && game.saveClickX <= 696
        && game.saveClickY >= 168
        && game.saveClickY < 205
        && game.tabInterfaceIDs[4] != -1) {
      game.needDrawTabArea = true;
      game.tabID = 4;
      game.tabAreaAltered = true;
    }
    if (game.saveClickX >= 694
        && game.saveClickX <= 724
        && game.saveClickY >= 168
        && game.saveClickY < 205
        && game.tabInterfaceIDs[5] != -1) {
      game.needDrawTabArea = true;
      game.tabID = 5;
      game.tabAreaAltered = true;
    }
    if (game.saveClickX >= 722
        && game.saveClickX <= 756
        && game.saveClickY >= 169
        && game.saveClickY < 205
        && game.tabInterfaceIDs[6] != -1) {
      game.needDrawTabArea = true;
      game.tabID = 6;
      game.tabAreaAltered = true;
    }
    if (game.saveClickX >= 540
        && game.saveClickX <= 574
        && game.saveClickY >= 466
        && game.saveClickY < 502
        && game.tabInterfaceIDs[7] != -1
        && ClientSettings.CUSTOM_SETTINGS_TAB) {
      game.needDrawTabArea = true;
      game.tabID = 7;
      game.tabAreaAltered = true;
    }
    if (game.saveClickX >= 572
        && game.saveClickX <= 602
        && game.saveClickY >= 466
        && game.saveClickY < 503
        && game.tabInterfaceIDs[8] != -1) {
      game.needDrawTabArea = true;
      game.tabID = 8;
      game.tabAreaAltered = true;
    }
    if (game.saveClickX >= 599
        && game.saveClickX <= 629
        && game.saveClickY >= 466
        && game.saveClickY < 503
        && game.tabInterfaceIDs[9] != -1) {
      game.needDrawTabArea = true;
      game.tabID = 9;
      game.tabAreaAltered = true;
    }
    if (game.saveClickX >= 627
        && game.saveClickX <= 671
        && game.saveClickY >= 467
        && game.saveClickY < 502
        && game.tabInterfaceIDs[10] != -1) {
      game.needDrawTabArea = true;
      game.tabID = 10;
      game.tabAreaAltered = true;
    }
    if (game.saveClickX >= 669
        && game.saveClickX <= 699
        && game.saveClickY >= 466
        && game.saveClickY < 503
        && game.tabInterfaceIDs[11] != -1) {
      game.needDrawTabArea = true;
      game.tabID = 11;
      game.tabAreaAltered = true;
    }
    if (game.saveClickX >= 696
        && game.saveClickX <= 726
        && game.saveClickY >= 466
        && game.saveClickY < 503
        && game.tabInterfaceIDs[12] != -1) {
      game.needDrawTabArea = true;
      game.tabID = 12;
      game.tabAreaAltered = true;
    }
    if (game.saveClickX >= 724
        && game.saveClickX <= 758
        && game.saveClickY >= 466
        && game.saveClickY < 502
        && game.tabInterfaceIDs[13] != -1) {
      game.needDrawTabArea = true;
      game.tabID = 13;
      game.tabAreaAltered = true;
    }
    if (game.invOverlayInterfaceID == -1 && game.tabInterfaceIDs[game.tabID] != -1) {
      if (game.tabID == 7
          && ClientSettings.CUSTOM_SETTINGS_TAB
          && game.saveClickX >= 575
          && game.saveClickX <= 720
          && game.saveClickY >= 210
          && game.saveClickY <= 465) {
        int startY = 220;
        if (game.saveClickY >= startY && game.saveClickY <= startY + 30) {
          Game.customSettingVisiblePlayerNames = !Game.customSettingVisiblePlayerNames;
        }
        startY += 40;
        if (game.saveClickY >= startY && game.saveClickY <= startY + 30) {
          game.inputTaken = true;
          game.inputDialogState = 0;
          game.messagePromptRaised = true;
          game.promptInput = "";
          game.inputPrompt = "Enter minimum item value";
          game.customTabAction = 1;
        }
        startY += 40;
        if (game.saveClickY >= startY && game.saveClickY <= startY + 30) {
          game.inputTaken = true;
          game.inputDialogState = 0;
          game.messagePromptRaised = true;
          game.promptInput = "";
          game.inputPrompt = "Enter new draw distance";
          game.customTabAction = 2;
        }
        startY += 40;
        if (game.saveClickY >= startY && game.saveClickY <= startY + 30) {
          game.customSettingShowExperiencePerHour = !game.customSettingShowExperiencePerHour;
          game.customSettingShowExperiencePerHourStart = System.currentTimeMillis();
          game.customSettingShowExperiencePerHourStartExp =
              PlayerStatsCalculator.calculateTotalExp(game.currentExp);
          game.customSettingShowExperiencePerHourStartLevels =
              PlayerStatsCalculator.calculateTotalLevels(game.maxStats);
        }
        startY += 40;
        if (game.saveClickY >= startY && game.saveClickY <= startY + 30) {
          game.showInfo = !game.showInfo;
        }
        startY += 40;
        if (game.saveClickY >= startY && game.saveClickY <= startY + 30) {
          game.customSettingVisualFixes = !game.customSettingVisualFixes;
          ClientSettings.BILINEAR_MINIMAP_FILTERING = !ClientSettings.BILINEAR_MINIMAP_FILTERING;
          ClientSettings.FIX_TRANSPARENCY_OVERFLOW = !ClientSettings.FIX_TRANSPARENCY_OVERFLOW;
          ClientSettings.FULL_512PX_VIEWPORT = !ClientSettings.FULL_512PX_VIEWPORT;
        }
      }
    }
    if (game.flashingTabId == game.tabID) {
      game.stream.createFrame(152);
      game.stream.writeWordBigEndian(game.tabID);
    }
  }
}
