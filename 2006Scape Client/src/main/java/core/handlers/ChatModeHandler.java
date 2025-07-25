package core.handlers;

import core.engine.Game;

/** Handles chat mode toggling extracted from {@link Game}. */
public final class ChatModeHandler {
    private final Game game;

    public ChatModeHandler(Game game) {
        this.game = game;
    }

    public void processChatModeClick() {
        if (game.clickMode3 == 1) {
            if (game.saveClickX >= 6 && game.saveClickX <= 106 && game.saveClickY >= 467 && game.saveClickY <= 499) {
                game.publicChatMode = (game.publicChatMode + 1) % 4;
                game.chatSettingsUpdateNeeded = true;
                game.inputTaken = true;
                game.stream.createFrame(95);
                game.stream.writeWordBigEndian(game.publicChatMode);
                game.stream.writeWordBigEndian(game.privateChatMode);
                game.stream.writeWordBigEndian(game.tradeMode);
            }
            if (game.saveClickX >= 135 && game.saveClickX <= 235 && game.saveClickY >= 467 && game.saveClickY <= 499) {
                game.privateChatMode = (game.privateChatMode + 1) % 3;
                game.chatSettingsUpdateNeeded = true;
                game.inputTaken = true;
                game.stream.createFrame(95);
                game.stream.writeWordBigEndian(game.publicChatMode);
                game.stream.writeWordBigEndian(game.privateChatMode);
                game.stream.writeWordBigEndian(game.tradeMode);
            }
            if (game.saveClickX >= 273 && game.saveClickX <= 373 && game.saveClickY >= 467 && game.saveClickY <= 499) {
                game.tradeMode = (game.tradeMode + 1) % 3;
                game.chatSettingsUpdateNeeded = true;
                game.inputTaken = true;
                game.stream.createFrame(95);
                game.stream.writeWordBigEndian(game.publicChatMode);
                game.stream.writeWordBigEndian(game.privateChatMode);
                game.stream.writeWordBigEndian(game.tradeMode);
            }
            if (game.saveClickX >= 412 && game.saveClickX <= 512 && game.saveClickY >= 467 && game.saveClickY <= 499) {
                if (game.openInterfaceID == -1) {
                    game.closeOpenInterfaces();
                    game.reportAbuseInput = "";
                    game.canMute = false;
                    for (ui.RSInterface element : ui.RSInterface.interfaceCache) {
                        if (element == null || element.contentType != 600) {
                            continue;
                        }
                        game.reportAbuseInterfaceID = game.openInterfaceID = element.parentID;
                        break;
                    }
                } else {
                    game.pushMessage("Please close the public interface you have open before using 'report abuse'", 0, "");
                }
            }
            game.abuseReportCounter++;
            if (game.abuseReportCounter > 1386) {
                game.abuseReportCounter = 0;
                game.stream.createFrame(165);
                game.stream.writeWordBigEndian(0);
                int j = game.stream.currentOffset;
                game.stream.writeWordBigEndian(139);
                game.stream.writeWordBigEndian(150);
                game.stream.writeWord(32131);
                game.stream.writeWordBigEndian((int) (Math.random() * 256D));
                game.stream.writeWord(3250);
                game.stream.writeWordBigEndian(177);
                game.stream.writeWord(24859);
                game.stream.writeWordBigEndian(119);
                if ((int) (Math.random() * 2D) == 0) {
                    game.stream.writeWord(47234);
                }
                if ((int) (Math.random() * 2D) == 0) {
                    game.stream.writeWordBigEndian(21);
                }
                game.stream.writeBytes(game.stream.currentOffset - j);
            }
        }
    }
}
