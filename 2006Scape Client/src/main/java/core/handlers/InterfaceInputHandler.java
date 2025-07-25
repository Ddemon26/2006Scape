package core.handlers;

import core.engine.Game;
import ui.RSInterface;
import ui.TextClass;
import util.configuration.IDK;

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
}
