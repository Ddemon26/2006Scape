package core.managers;

import core.engine.Game;
import core.network.Signlink;
import game.animation.Animation;
import render.geometry.Model;
import ui.RSInterface;
import ui.TextClass;
import util.configuration.IDK;

/** Handles friend list operations extracted from {@link Game}. */
public final class FriendManager {
  private final Game game;

  public FriendManager(Game game) {
    this.game = game;
  }

  public void addFriend(long l) {
    try {
      if (l == 0L) {
        return;
      }
      if (game.friendsCount >= 100 && game.friendsListStatus != 1) {
        game.pushMessage(
            "Your friendlist is full. Max of 100 for free users, and 200 for members", 0, "");
        return;
      }
      if (game.friendsCount >= 200) {
        game.pushMessage(
            "Your friendlist is full. Max of 100 for free users, and 200 for members", 0, "");
        return;
      }
      String s = TextClass.fixName(TextClass.nameForLong(l));
      for (int i = 0; i < game.friendsCount; i++) {
        if (game.friendsListAsLongs[i] == l) {
          game.pushMessage(s + " is already on your friend list", 0, "");
          return;
        }
      }
      for (int j = 0; j < game.ignoreCount; j++) {
        if (game.ignoreListAsLongs[j] == l) {
          game.pushMessage("Please remove " + s + " from your ignore list first", 0, "");
          return;
        }
      }
      if (s.equals(game.myPlayer.name)) {
        return;
      } else {
        game.friendsList[game.friendsCount] = s;
        game.friendsListAsLongs[game.friendsCount] = l;
        game.friendsNodeIDs[game.friendsCount] = 0;
        game.friendsCount++;
        game.needDrawTabArea = true;
        game.stream.createFrame(188);
        game.stream.writeQWord(l);
        return;
      }
    } catch (RuntimeException runtimeexception) {
      Signlink.reporterror("15283, " + (byte) 68 + ", " + l + ", " + runtimeexception.toString());
    }
    throw new RuntimeException();
  }

  public void delFriend(long l) {
    try {
      if (l == 0L) {
        return;
      }
      for (int i = 0; i < game.friendsCount; i++) {
        if (game.friendsListAsLongs[i] != l) {
          continue;
        }
        game.friendsCount--;
        game.needDrawTabArea = true;
        for (int j = i; j < game.friendsCount; j++) {
          game.friendsList[j] = game.friendsList[j + 1];
          game.friendsNodeIDs[j] = game.friendsNodeIDs[j + 1];
          game.friendsListAsLongs[j] = game.friendsListAsLongs[j + 1];
        }
        game.stream.createFrame(215);
        game.stream.writeQWord(l);
        break;
      }
    } catch (RuntimeException runtimeexception) {
      Signlink.reporterror("18622, " + false + ", " + l + ", " + runtimeexception.toString());
      throw new RuntimeException();
    }
  }

  public boolean isFriendOrSelf(String s) {
    if (s == null) {
      return false;
    }
    for (int i = 0; i < game.friendsCount; i++) {
      if (s.equalsIgnoreCase(game.friendsList[i])) {
        return true;
      }
    }
    return s.equalsIgnoreCase(game.myPlayer.name);
  }

  /** Render friends list or welcome screen interface content. */
  public void drawFriendsListOrWelcomeScreen(RSInterface interfaceComponent) {
    int j = interfaceComponent.contentType;
    if (j >= 1 && j <= 100 || j >= 701 && j <= 800) {
      if (j == 1 && game.interfaceMode == 0) {
        interfaceComponent.disabledText = "Loading friend list";
        interfaceComponent.atActionType = 0;
        return;
      }
      if (j == 1 && game.interfaceMode == 1) {
        interfaceComponent.disabledText = "Connecting to friendserver";
        interfaceComponent.atActionType = 0;
        return;
      }
      if (j == 2 && game.interfaceMode != 2) {
        interfaceComponent.disabledText = "Please wait...";
        interfaceComponent.atActionType = 0;
        return;
      }
      int k = game.friendsCount;
      if (game.interfaceMode != 2) {
        k = 0;
      }
      if (j > 700) {
        j -= 601;
      } else {
        j--;
      }
      if (j >= k) {
        interfaceComponent.disabledText = "";
        interfaceComponent.atActionType = 0;
      } else {
        interfaceComponent.disabledText = game.friendsList[j];
        interfaceComponent.atActionType = 1;
      }
      return;
    }
    if (j >= 101 && j <= 200 || j >= 801 && j <= 900) {
      int l = game.friendsCount;
      if (game.interfaceMode != 2) {
        l = 0;
      }
      if (j > 800) {
        j -= 701;
      } else {
        j -= 101;
      }
      if (j >= l) {
        interfaceComponent.disabledText = "";
        interfaceComponent.atActionType = 0;
        return;
      }
      if (game.friendsNodeIDs[j] - 9 <= 0) {
        interfaceComponent.disabledText = "@red@Offline";
      } else if (game.friendsNodeIDs[j] == game.nodeID) {
        interfaceComponent.disabledText = "@gre@World-" + (game.friendsNodeIDs[j] - 9);
      } else {
        interfaceComponent.disabledText = "@yel@World-" + (game.friendsNodeIDs[j] - 9);
      }
      interfaceComponent.atActionType = 1;
      return;
    }
    if (j == 203) {
      int i1 = game.friendsCount;
      if (game.interfaceMode != 2) {
        i1 = 0;
      }
      interfaceComponent.scrollMax = i1 * 15 + 20;
      if (interfaceComponent.scrollMax <= interfaceComponent.height) {
        interfaceComponent.scrollMax = interfaceComponent.height + 1;
      }
      return;
    }
    if (j >= 401 && j <= 500) {
      if ((j -= 401) == 0 && game.interfaceMode == 0) {
        interfaceComponent.disabledText = "Loading ignore list";
        interfaceComponent.atActionType = 0;
        return;
      }
      if (j == 1 && game.interfaceMode == 0) {
        interfaceComponent.disabledText = "Please wait...";
        interfaceComponent.atActionType = 0;
        return;
      }
      int j1 = game.ignoreCount;
      if (game.interfaceMode == 0) {
        j1 = 0;
      }
      if (j >= j1) {
        interfaceComponent.disabledText = "";
        interfaceComponent.atActionType = 0;
      } else {
        interfaceComponent.disabledText =
            TextClass.fixName(TextClass.nameForLong(game.ignoreListAsLongs[j]));
        interfaceComponent.atActionType = 1;
      }
      return;
    }
    if (j == 503) {
      interfaceComponent.scrollMax = game.ignoreCount * 15 + 20;
      if (interfaceComponent.scrollMax <= interfaceComponent.height) {
        interfaceComponent.scrollMax = interfaceComponent.height + 1;
      }
      return;
    }
    if (j == 327) {
      interfaceComponent.modelRotation1 = 150;
      interfaceComponent.modelRotation2 = (int) (Math.sin((double) game.loopCycle / 40D) * 256D) & 0x7ff;
      if (game.characterDesignChanged) {
        for (int k1 = 0; k1 < 7; k1++) {
          int l1 = game.characterStyle[k1];
          if (l1 >= 0 && !IDK.cache[l1].ready()) {
            return;
          }
        }

        game.characterDesignChanged = false;
        Model modelParts[] = new Model[7];
        int i2 = 0;
        for (int j2 = 0; j2 < 7; j2++) {
          int k2 = game.characterStyle[j2];
          if (k2 >= 0) {
            modelParts[i2++] = IDK.cache[k2].getBodyModel();
          }
        }

        Model model = new Model(i2, modelParts);
        for (int l2 = 0; l2 < 5; l2++) {
          if (game.characterColorIndices[l2] != 0) {
            model.recolor(
                Game.appearanceColorOptions[l2][0],
                Game.appearanceColorOptions[l2][game.characterColorIndices[l2]]);
            if (l2 == 1) {
              model.recolor(
                  Game.additionalColorCodes[0], Game.additionalColorCodes[game.characterColorIndices[l2]]);
            }
          }
        }

        model.buildVertexGroups();
        model.applyFrame(Animation.anims[game.myPlayer.standAnimation].frameIds[0]);
        model.applyLighting(64, 850, -30, -50, -30, true);
        interfaceComponent.mediaType = 5;
        interfaceComponent.mediaId = 0;
        RSInterface.clearModelCache(model, 0, 5);
      }
      return;
    }
    if (j == 324) {
      if (game.maleIconSprite == null) {
        game.maleIconSprite = interfaceComponent.sprite1;
        game.femaleIconSprite = interfaceComponent.sprite2;
      }
      if (game.isMaleCharacter) {
        interfaceComponent.sprite1 = game.femaleIconSprite;
      } else {
        interfaceComponent.sprite1 = game.maleIconSprite;
      }
      return;
    }
    if (j == 325) {
      if (game.maleIconSprite == null) {
        game.maleIconSprite = interfaceComponent.sprite1;
        game.femaleIconSprite = interfaceComponent.sprite2;
      }
      if (game.isMaleCharacter) {
        interfaceComponent.sprite1 = game.maleIconSprite;
      } else {
        interfaceComponent.sprite1 = game.femaleIconSprite;
      }
      return;
    }
    if (j == 600) {
      interfaceComponent.disabledText = game.reportAbuseInput;
      if (game.loopCycle % 20 < 10) {
        interfaceComponent.disabledText += "|";
      } else {
        interfaceComponent.disabledText += " ";
      }
      return;
    }
    if (j == 613) {
      if (game.myPrivilege >= 1 && game.myPrivilege <= 3) {
        if (game.canMute) {
          interfaceComponent.textColor = 0xff0000;
          interfaceComponent.disabledText = "Moderator option: Mute player for 48 hours: <ON>";
        } else {
          interfaceComponent.textColor = 0xffffff;
          interfaceComponent.disabledText = "Moderator option: Mute player for 48 hours: <OFF>";
        }
      } else {
        interfaceComponent.disabledText = "";
      }
      return;
    }
    if (j == 661) {
      if (game.recoveryQuestionChangeDate == 0) {
        interfaceComponent.disabledText = // Client accepts \\n for new line, but not \n for interface components
                "\\nYou have not yet set any recovery questions.\\nIt is @lre@strongly@yel@ recommended that you do so." +
                        "\\n\\nIf you don't you will be @lre@unable to recover your\\n" +
                        "@lre@password@yel@ if you forget it, or it is stolen.";
      } else if (game.recoveryQuestionChangeDate <= game.currentDateOffset) {
        interfaceComponent.disabledText =
            "\\n\\nRecovery Questions Last Set:\\n@gre@" + game.formatDate(game.recoveryQuestionChangeDate);
      } else {
        int l1 = (game.currentDateOffset + 14) - game.recoveryQuestionChangeDate;
        String s2;
        if (l1 <= 0) s2 = "Earlier today";
        else if (l1 == 1) s2 = "Yesterday";
        else s2 = l1 + " days ago";
        interfaceComponent.disabledText =
            s2
                + " you requested@lre@ new recovery\\n@lre@questions.@yel@ The requested change will occur\\non: @lre@"
                + game.formatDate(game.recoveryQuestionChangeDate)
                + "\\n\\nIf you do not remember making this request\\ncancel it immediately, and change your password.";
      }
      return;
    }
    if (j == 663) {
      if (game.lastPasswordChange <= 0 || game.lastPasswordChange > game.currentDateOffset + 10)
        interfaceComponent.disabledText = "Last password change:\\n@gre@Never changed";
      else
        interfaceComponent.disabledText =
            "Last password change:\\n@gre@" + game.formatDate(game.lastPasswordChange);
      return;
    }
    if (j == 668) {
      if (game.recoveryQuestionChangeDate > game.currentDateOffset) {
        interfaceComponent.disabledText =
            "To cancel this request:\\n1) Logout and return to the frontpage of this website.\\n2) Choose 'Cancel recovery questions'.";
      } else {
        interfaceComponent.disabledText =
            "To change your recovery questions:\\n1) Logout and return to the frontpage of this website.\\n2) Choose 'Set new recovery questions'.";
      }
    }
  }

  /** Build context menu entries for friends list widgets. */
  public boolean buildFriendsListMenu(RSInterface listInterface) {
    int i = listInterface.contentType;
    if (i >= 1 && i <= 200 || i >= 701 && i <= 900) {
      if (i >= 801) {
        i -= 701;
      } else if (i >= 701) {
        i -= 601;
      } else if (i >= 101) {
        i -= 101;
      } else {
        i--;
      }
      game.menuActionName[game.menuActionRow] = "Remove @whi@" + game.friendsList[i];
      game.menuActionID[game.menuActionRow] = 792;
      game.menuActionRow++;
      game.menuActionName[game.menuActionRow] = "Message @whi@" + game.friendsList[i];
      game.menuActionID[game.menuActionRow] = 639;
      game.menuActionRow++;
      return true;
    }
    if (i >= 401 && i <= 500) {
      game.menuActionName[game.menuActionRow] = "Remove @whi@" + listInterface.disabledText;
      game.menuActionID[game.menuActionRow] = 322;
      game.menuActionRow++;
      return true;
    }
    return false;
  }
}
