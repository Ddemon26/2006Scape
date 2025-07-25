package core.managers;

import core.engine.Game;
import core.network.Signlink;
import ui.TextClass;

/**
 * Handles ignore list operations extracted from {@link Game}.
 */
public final class IgnoreManager {
    private final Game game;

    public IgnoreManager(Game game) {
        this.game = game;
    }

    public void addIgnore(long l) {
        try {
            if (l == 0L) {
                return;
            }
            if (game.ignoreCount >= 100) {
                game.pushMessage("Your ignore list is full. Max of 100 hit", 0, "");
                return;
            }
            String s = TextClass.fixName(TextClass.nameForLong(l));
            for (int j = 0; j < game.ignoreCount; j++) {
                if (game.ignoreListAsLongs[j] == l) {
                    game.pushMessage(s + " is already on your ignore list", 0, "");
                    return;
                }
            }
            for (int k = 0; k < game.friendsCount; k++) {
                if (game.friendsListAsLongs[k] == l) {
                    game.pushMessage("Please remove " + s + " from your friend list first", 0, "");
                    return;
                }
            }
            game.ignoreListAsLongs[game.ignoreCount++] = l;
            game.needDrawTabArea = true;
            game.stream.createFrame(133);
            game.stream.writeQWord(l);
            return;
        } catch (RuntimeException runtimeexception) {
            Signlink.reporterror("45688, " + l + ", " + 4 + ", " + runtimeexception.toString());
        }
        throw new RuntimeException();
    }

    public void delIgnore(long l) {
        try {
            if (l == 0L) {
                return;
            }
            for (int j = 0; j < game.ignoreCount; j++) {
                if (game.ignoreListAsLongs[j] == l) {
                    game.ignoreCount--;
                    game.needDrawTabArea = true;
                    System.arraycopy(game.ignoreListAsLongs, j + 1, game.ignoreListAsLongs, j, game.ignoreCount - j);
                    game.stream.createFrame(74);
                    game.stream.writeQWord(l);
                    return;
                }
            }
            return;
        } catch (RuntimeException runtimeexception) {
            Signlink.reporterror("47229, " + 3 + ", " + l + ", " + runtimeexception.toString());
        }
        throw new RuntimeException();
    }
}
