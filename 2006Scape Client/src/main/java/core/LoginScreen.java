
package core;

import core.engine.ClientSettings;
import core.engine.Game;
import ui.TextClass;

/**
 * Handles login screen input and rendering. Extracted from {@link Game}.
 */
public final class LoginScreen {
    private final Game game;

    public LoginScreen(Game game) {
        this.game = game;
    }

    public void processLoginScreenInput() {
        if (game.loginScreenState == 0) {
            int i = game.myWidth / 2 - 80;
            int l = game.myHeight / 2 + 20;
            l += 20;
            if (game.clickMode3 == 1 && game.saveClickX >= i - 75 && game.saveClickX <= i + 75 && game.saveClickY >= l - 20 && game.saveClickY <= l + 20) {
                game.loginScreenState = 3;
                game.loginScreenCursorPos = 0;
            }
            i = game.myWidth / 2 + 80;
            if (game.clickMode3 == 1 && game.saveClickX >= i - 75 && game.saveClickX <= i + 75 && game.saveClickY >= l - 20 && game.saveClickY <= l + 20) {
                game.loginMessage1 = "";
                game.loginMessage2 = "Enter your username & password.";
                game.loginScreenState = 2;
                game.loginScreenCursorPos = 0;
            }
        } else {
            if (game.loginScreenState == 2) {
                int j = game.myHeight / 2 - 40;
                j += 30;
                j += 25;
                if (game.clickMode3 == 1 && game.saveClickY >= j - 15 && game.saveClickY < j) {
                    game.loginScreenCursorPos = 0;
                }
                j += 15;
                if (game.clickMode3 == 1 && game.saveClickY >= j - 15 && game.saveClickY < j) {
                    game.loginScreenCursorPos = 1;
                }
                j += 15;
                int i1 = game.myWidth / 2 - 80;
                int k1 = game.myHeight / 2 + 50;
                k1 += 20;
                if (game.clickMode3 == 1 && game.saveClickX >= i1 - 75 && game.saveClickX <= i1 + 75 && game.saveClickY >= k1 - 20 && game.saveClickY <= k1 + 20) {
                    game.loginFailures = 0;
                    game.login(game.myUsername, game.myPassword, false);
                    if (game.loggedIn) {
                        return;
                    }
                }
                i1 = game.myWidth / 2 + 80;
                if (game.clickMode3 == 1 && game.saveClickX >= i1 - 75 && game.saveClickX <= i1 + 75 && game.saveClickY >= k1 - 20 && game.saveClickY <= k1 + 20) {
                    game.loginScreenState = 0;
                }
                do {
                    int l1 = game.readChar(-796);
                    if (l1 == -1) {
                        break;
                    }
                    boolean flag1 = false;
                    for (int i2 = 0; i2 < game.validUserPassChars.length(); i2++) {
                        if (l1 != game.validUserPassChars.charAt(i2)) {
                            continue;
                        }
                        flag1 = true;
                        break;
                    }

                    if (game.loginScreenCursorPos == 0) {
                        if (l1 == 8 && game.myUsername.length() > 0) {
                            game.myUsername = game.myUsername.substring(0, game.myUsername.length() - 1);
                        }
                        if (l1 == 9 || l1 == 10 || l1 == 13) {
                            game.loginScreenCursorPos = 1;
                        }
                        if (flag1) {
                            game.myUsername += (char) l1;
                        }
                        if (game.myUsername.length() > 12) {
                            game.myUsername = game.myUsername.substring(0, 12);
                        }
                    } else if (game.loginScreenCursorPos == 1) {
                        if (l1 == 8 && game.myPassword.length() > 0) {
                            game.myPassword = game.myPassword.substring(0, game.myPassword.length() - 1);
                        }
                        if (l1 == 9 || l1 == 10 || l1 == 13) {
                            game.login(game.myUsername, game.myPassword, false);
                            game.loginScreenCursorPos = 0;
                        }
                        if (flag1) {
                            game.myPassword += (char) l1;
                        }
                        if (game.myPassword.length() > 20) {
                            game.myPassword = game.myPassword.substring(0, 20);
                        }
                    }
                } while (true);
                return;
            }
            if (game.loginScreenState == 3) {
                int k = game.myWidth / 2;
                int j1 = game.myHeight / 2 + 50;
                j1 += 20;
                if (game.clickMode3 == 1 && game.saveClickX >= k - 75 && game.saveClickX <= k + 75 && game.saveClickY >= j1 - 20 && game.saveClickY <= j1 + 20) {
                    game.loginScreenState = 0;
                }
            }
        }
    }

    public void drawLoginScreen(boolean flag) {
        game.resetImageProducers();
        game.loginRightProducer.initDrawingArea();
        game.loginBoxBackground.draw(0, 0);
        char c = '\u0168';
        char c1 = '\310';
        if (game.loginScreenState == 0) {
            int i = c1 / 2 + 80;
            game.plainFont.textCenterShadow(0x75a9a9, c / 2, game.onDemandFetcher.statusString, i, true);
            i = c1 / 2 - 20;
            game.chatTextDrawingArea.textCenterShadow(0xffff00, c / 2, "Welcome to " + ClientSettings.SERVER_NAME + "", i, true);
            i += 30;
            int l = c / 2 - 80;
            int k1 = c1 / 2 + 20;
            game.loginButtonBackground.draw(l - 73, k1 - 20);
            game.chatTextDrawingArea.textCenterShadow(0xffffff, l, "New User", k1 + 5, true);
            l = c / 2 + 80;
            game.loginButtonBackground.draw(l - 73, k1 - 20);
            game.chatTextDrawingArea.textCenterShadow(0xffffff, l, "Existing User", k1 + 5, true);
        }
        if (game.loginScreenState == 2) {
            int j = c1 / 2 - 40;
            if (game.loginMessage1.length() > 0) {
                game.chatTextDrawingArea.textCenterShadow(0xffff00, c / 2, game.loginMessage1, j - 15, true);
                game.chatTextDrawingArea.textCenterShadow(0xffff00, c / 2, game.loginMessage2, j, true);
                j += 30;
            } else {
                game.chatTextDrawingArea.textCenterShadow(0xffff00, c / 2, game.loginMessage2, j - 7, true);
                j += 30;
            }
            game.chatTextDrawingArea.textLeftShadow(true, c / 2 - 90, 0xffffff, "Username: " + game.myUsername + (game.loginScreenCursorPos == 0 & game.loopCycle % 40 < 20 ? "@yel@|" : ""), j);
            j += 15;
            game.chatTextDrawingArea.textLeftShadow(true, c / 2 - 88, 0xffffff, "Password: " + TextClass.passwordAsterisks(game.myPassword) + (game.loginScreenCursorPos == 1 & game.loopCycle % 40 < 20 ? "@yel@|" : ""), j);
            j += 15;
            if (!flag) {
                int i1 = c / 2 - 80;
                int l1 = c1 / 2 + 50;
                game.loginButtonBackground.draw(i1 - 73, l1 - 20);
                game.chatTextDrawingArea.textCenterShadow(0xffffff, i1, "Login", l1 + 5, true);
                i1 = c / 2 + 80;
                game.loginButtonBackground.draw(i1 - 73, l1 - 20);
                game.chatTextDrawingArea.textCenterShadow(0xffffff, i1, "Cancel", l1 + 5, true);
            }
        }
        if (game.loginScreenState == 3) {
            game.chatTextDrawingArea.textCenterShadow(0xffff00, c / 2, "Create a free account", c1 / 2 - 60, true);
            int k = c1 / 2 - 35;
            game.chatTextDrawingArea.textCenterShadow(0xffffff, c / 2, "To create a new account just click", k, true);
            k += 15;
            game.chatTextDrawingArea.textCenterShadow(0xffffff, c / 2, "\"Cancel\" below, and click \"Existing User\".", k, true);
            k += 15;
            game.chatTextDrawingArea.textCenterShadow(0xffffff, c / 2, "Log in with any credentials you want and an", k, true);
            k += 15;
            game.chatTextDrawingArea.textCenterShadow(0xffffff, c / 2, "account will automatically be created for you.", k, true);
            k += 15;
            int j1 = c / 2;
            int i2 = c1 / 2 + 50;
            game.loginButtonBackground.draw(j1 - 73, i2 - 20);
            game.chatTextDrawingArea.textCenterShadow(0xffffff, j1, "Cancel", i2 + 5, true);
        }
        game.loginRightProducer.drawGraphics(171, game.graphics, 202);
        if (game.welcomeScreenRaised) {
            game.welcomeScreenRaised = false;
            game.titleImageProducer.drawGraphics(0, game.graphics, 128);
            game.loginLeftProducer.drawGraphics(371, game.graphics, 202);
            game.titleTopLeftProducer.drawGraphics(265, game.graphics, 0);
            game.titleTopRightProducer.drawGraphics(265, game.graphics, 562);
            game.titleBottomLeftProducer.drawGraphics(171, game.graphics, 128);
            game.titleBottomRightProducer.drawGraphics(171, game.graphics, 562);
        }
    }
}
