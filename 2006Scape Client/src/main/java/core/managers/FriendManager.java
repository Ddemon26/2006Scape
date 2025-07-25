package core.managers;

import core.engine.Game;
import core.network.Signlink;
import ui.TextClass;

/**
 * Handles friend list operations extracted from {@link Game}.
 */
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
                game.pushMessage("Your friendlist is full. Max of 100 for free users, and 200 for members", 0, "");
                return;
            }
            if (game.friendsCount >= 200) {
                game.pushMessage("Your friendlist is full. Max of 100 for free users, and 200 for members", 0, "");
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
}
