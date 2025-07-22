package core;

import render.DrawingArea;
import render.Texture;
import ui.RSInterface;
import ui.TextClass;
import ui.TextDrawingArea;

/**
 * Renders the game's chat area. Extracted from {@link Game} for readability.
 */
final class ChatAreaRenderer {
    private final Game game;

    ChatAreaRenderer(Game game) {
        this.game = game;
    }

    void drawChatArea() {
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
                    if ((l == 1 || l == 2) && (l == 1 || game.publicChatMode == 0 || game.publicChatMode == 1 && game.isFriendOrSelf(s1))) {
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
                    if ((l == 3 || l == 7) && game.splitpublicChat == 0 && (l == 7 || game.privateChatMode == 0 || game.privateChatMode == 1 && game.isFriendOrSelf(s1))) {
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
                            textDrawingArea.textLeft(0x800000, game.chatMessages[k], i1, 12 + textDrawingArea.getTextWidth("To " + s1));
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
            game.drawScrollThumb(77, game.chatScrollHeight - game.chatScrollPosition - 77, 0, 463, game.chatScrollHeight);
            String s;
            if (game.myPlayer != null && game.myPlayer.name != null) {
                s = game.myPlayer.name;
            } else {
                s = TextClass.fixName(game.myUsername);
            }
            textDrawingArea.textLeft(0, s + ":", 90, 4);
            textDrawingArea.textLeft(255, game.inputString + "*", 90, 6 + textDrawingArea.getTextWidth(s + ": "));
            DrawingArea.drawHorizontalLine(77, 0, 479, 0);
        }
        if (game.menuOpen && game.menuScreenArea == 2) {
            game.drawMenu();
        }
        game.fullScreenBackground.drawGraphics(357, game.graphics, 17);
        game.tabAreaBuffer.initDrawingArea();
        Texture.lineOffsets = game.chatBoxAreaOffsets;
    }
}
