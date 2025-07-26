package ui;

import core.engine.ClientSettings;
import core.engine.Game;
import render.core.Background;
import render.core.Sprite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

/** Handles login screen input and rendering. Extracted from {@link Game}. */
public final class LoginScreen {
  private final Game game;

  public LoginScreen(Game game) {
    this.game = game;
  }

  /** Load title screen graphics and initialise flame buffers. */
  public void loadTitleScreen() {
    game.loginBoxBackground = new Background(game.titleStreamLoader, "titlebox", 0);
    game.loginButtonBackground = new Background(game.titleStreamLoader, "titlebutton", 0);
    game.runeBackgrounds = new Background[12];
    int j = 0;
    try {
      j = Integer.parseInt(game.getParameter("fl_icon"));
    } catch (Exception _ex) {
    }
    if (j == 0) {
      for (int k = 0; k < 12; k++) {
        game.runeBackgrounds[k] = new Background(game.titleStreamLoader, "runes", k);
      }
    } else {
      for (int l = 0; l < 12; l++) {
        game.runeBackgrounds[l] =
            new Background(game.titleStreamLoader, "runes", 12 + (l & 3));
      }
    }
    game.titleBackgroundLeft = new Sprite(128, 265);
    game.titleBackgroundRight = new Sprite(128, 265);
    System.arraycopy(game.titleLeftProducer.pixels, 0, game.titleBackgroundLeft.pixels, 0, 33920);
    System.arraycopy(game.titleRightProducer.pixels, 0, game.titleBackgroundRight.pixels, 0, 33920);

    game.flamePaletteRed = new int[256];
    for (int k1 = 0; k1 < 64; k1++) {
      game.flamePaletteRed[k1] = k1 * 0x40000;
    }
    for (int l1 = 0; l1 < 64; l1++) {
      game.flamePaletteRed[l1 + 64] = 0xff0000 + 1024 * l1;
    }
    for (int i2 = 0; i2 < 64; i2++) {
      game.flamePaletteRed[i2 + 128] = 0xffff00 + 4 * i2;
    }
    for (int j2 = 0; j2 < 64; j2++) {
      game.flamePaletteRed[j2 + 192] = 0xffffff;
    }

    game.flamePaletteGreen = new int[256];
    for (int k2 = 0; k2 < 64; k2++) {
      game.flamePaletteGreen[k2] = k2 * 1024;
    }
    for (int l2 = 0; l2 < 64; l2++) {
      game.flamePaletteGreen[l2 + 64] = 0x00ff00 + 4 * l2;
    }
    for (int i3 = 0; i3 < 64; i3++) {
      game.flamePaletteGreen[i3 + 128] = 0x00ffff + 0x40000 * i3;
    }
    for (int j3 = 0; j3 < 64; j3++) {
      game.flamePaletteGreen[j3 + 192] = 0xffffff;
    }

    game.flamePaletteBlue = new int[256];
    for (int k3 = 0; k3 < 64; k3++) {
      game.flamePaletteBlue[k3] = k3 * 4;
    }
    for (int l3 = 0; l3 < 64; l3++) {
      game.flamePaletteBlue[l3 + 64] = 255 + 0x40000 * l3;
    }
    for (int i4 = 0; i4 < 64; i4++) {
      game.flamePaletteBlue[i4 + 128] = 0xff00ff + 1024 * i4;
    }
    for (int j4 = 0; j4 < 64; j4++) {
      game.flamePaletteBlue[j4 + 192] = 0xffffff;
    }

    game.flameBuffer = new int[256];
    game.flameGradient1 = new int[32768];
    game.flameGradient2 = new int[32768];
    game.randomizeBackground(null);
    game.flameBuffer1 = new int[32768];
    game.flameBuffer2 = new int[32768];
    game.drawLoadingText(10, "Connecting to fileserver");
    if (!game.flameThreadActive) {
      game.drawFlames = true;
      game.flameThreadActive = true;
      game.startRunnable(game, 2);
    }
  }

  /** Draw the cached logo and title screen pieces. */
  public void drawLogo() {
    byte[] abyte0 = game.titleStreamLoader.getFileData("title.dat");
    Sprite sprite = new Sprite(abyte0, game);
    game.titleLeftProducer.initDrawingArea();
    sprite.drawSprite(0, 0);
    game.titleRightProducer.initDrawingArea();
    sprite.drawSprite(-637, 0);
    game.titleImageProducer.initDrawingArea();
    sprite.drawSprite(-128, 0);
    game.loginLeftProducer.initDrawingArea();
    sprite.drawSprite(-202, -371);
    game.loginRightProducer.initDrawingArea();
    sprite.drawSprite(-202, -171);
    game.titleTopLeftProducer.initDrawingArea();
    sprite.drawSprite(0, -265);
    game.titleTopRightProducer.initDrawingArea();
    sprite.drawSprite(-562, -265);
    game.titleBottomLeftProducer.initDrawingArea();
    sprite.drawSprite(-128, -171);
    game.titleBottomRightProducer.initDrawingArea();
    sprite.drawSprite(-562, -171);
    int[] ai = new int[sprite.width];
    for (int j = 0; j < sprite.height; j++) {
      for (int k = 0; k < sprite.width; k++) {
        ai[k] = sprite.pixels[sprite.width - k - 1 + sprite.width * j];
      }
      System.arraycopy(ai, 0, sprite.pixels, sprite.width * j, sprite.width);
    }
    game.titleLeftProducer.initDrawingArea();
    sprite.drawSprite(382, 0);
    game.titleRightProducer.initDrawingArea();
    sprite.drawSprite(-255, 0);
    game.titleImageProducer.initDrawingArea();
    sprite.drawSprite(254, 0);
    game.loginLeftProducer.initDrawingArea();
    sprite.drawSprite(180, -371);
    game.loginRightProducer.initDrawingArea();
    sprite.drawSprite(180, -171);
    game.titleTopLeftProducer.initDrawingArea();
    sprite.drawSprite(382, -265);
    game.titleTopRightProducer.initDrawingArea();
    sprite.drawSprite(-180, -265);
    game.titleBottomLeftProducer.initDrawingArea();
    sprite.drawSprite(254, -171);
    game.titleBottomRightProducer.initDrawingArea();
    sprite.drawSprite(-180, -171);
    sprite = new Sprite(game.titleStreamLoader, "logo", 0);
    game.titleImageProducer.initDrawingArea();
    sprite.drawTransparentSprite(382 - sprite.width / 2 - 128, 18);
    sprite = null;
    System.gc();
  }

  public void processLoginScreenInput() {
    if (game.loginScreenState == 0) {
      int i = game.myWidth / 2 - 80;
      int l = game.myHeight / 2 + 20;
      l += 20;
      if (game.clickMode3 == 1
          && game.saveClickX >= i - 75
          && game.saveClickX <= i + 75
          && game.saveClickY >= l - 20
          && game.saveClickY <= l + 20) {
        game.loginScreenState = 3;
        game.loginScreenCursorPos = 0;
      }
      i = game.myWidth / 2 + 80;
      if (game.clickMode3 == 1
          && game.saveClickX >= i - 75
          && game.saveClickX <= i + 75
          && game.saveClickY >= l - 20
          && game.saveClickY <= l + 20) {
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
        if (game.clickMode3 == 1
            && game.saveClickX >= i1 - 75
            && game.saveClickX <= i1 + 75
            && game.saveClickY >= k1 - 20
            && game.saveClickY <= k1 + 20) {
          game.loginFailures = 0;
          game.login(game.myUsername, game.myPassword, false);
          if (game.loggedIn) {
            return;
          }
        }
        i1 = game.myWidth / 2 + 80;
        if (game.clickMode3 == 1
            && game.saveClickX >= i1 - 75
            && game.saveClickX <= i1 + 75
            && game.saveClickY >= k1 - 20
            && game.saveClickY <= k1 + 20) {
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
        if (game.clickMode3 == 1
            && game.saveClickX >= k - 75
            && game.saveClickX <= k + 75
            && game.saveClickY >= j1 - 20
            && game.saveClickY <= j1 + 20) {
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
      game.chatTextDrawingArea.textCenterShadow(
          0xffff00, c / 2, "Welcome to " + ClientSettings.SERVER_NAME + "", i, true);
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
        game.chatTextDrawingArea.textCenterShadow(
            0xffff00, c / 2, game.loginMessage1, j - 15, true);
        game.chatTextDrawingArea.textCenterShadow(0xffff00, c / 2, game.loginMessage2, j, true);
        j += 30;
      } else {
        game.chatTextDrawingArea.textCenterShadow(0xffff00, c / 2, game.loginMessage2, j - 7, true);
        j += 30;
      }
      game.chatTextDrawingArea.textLeftShadow(
          true,
          c / 2 - 90,
          0xffffff,
          "Username: "
              + game.myUsername
              + (game.loginScreenCursorPos == 0 & game.loopCycle % 40 < 20 ? "@yel@|" : ""),
          j);
      j += 15;
      game.chatTextDrawingArea.textLeftShadow(
          true,
          c / 2 - 88,
          0xffffff,
          "Password: "
              + TextClass.passwordAsterisks(game.myPassword)
              + (game.loginScreenCursorPos == 1 & game.loopCycle % 40 < 20 ? "@yel@|" : ""),
          j);
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
      game.chatTextDrawingArea.textCenterShadow(
          0xffff00, c / 2, "Create a free account", c1 / 2 - 60, true);
      int k = c1 / 2 - 35;
      game.chatTextDrawingArea.textCenterShadow(
          0xffffff, c / 2, "To create a new account just click", k, true);
      k += 15;
      game.chatTextDrawingArea.textCenterShadow(
          0xffffff, c / 2, "\"Cancel\" below, and click \"Existing User\".", k, true);
      k += 15;
      game.chatTextDrawingArea.textCenterShadow(
          0xffffff, c / 2, "Log in with any credentials you want and an", k, true);
      k += 15;
      game.chatTextDrawingArea.textCenterShadow(
          0xffffff, c / 2, "account will automatically be created for you.", k, true);
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

  /** Draws an error screen when loading fails. */
  public void showErrorScreen() {
    Graphics g = game.getGameComponent().getGraphics();
    g.setColor(Color.black);
    g.fillRect(0, 0, 765, 503);
    game.setFrameRate(1);
    if (game.loadingError) {
      game.flameThreadActive = false;
      g.setFont(new Font("Helvetica", Font.BOLD, 16));
      g.setColor(Color.yellow);
      int k = 35;
      g.drawString(
          "Sorry, an error has occured whilst loading " + ClientSettings.SERVER_NAME + "",
          30,
          k);
      k += 50;
      g.setColor(Color.white);
      g.drawString("To fix this try the following (in order):", 30, k);
      k += 50;
      g.setColor(Color.white);
      g.setFont(new Font("Helvetica", Font.BOLD, 12));
      g.drawString("1: Try closing ALL open web-browser windows, and reloading", 30, k);
      k += 30;
      g.drawString("2: Try clearing your web-browsers cache from tools->internet options", 30, k);
      k += 30;
      g.drawString("3: Try using a different game-world", 30, k);
      k += 30;
      g.drawString("4: Try rebooting your computer", 30, k);
      k += 30;
      g.drawString("5: Try selecting a different version of Java from the play-game menu", 30, k);
    }
    if (game.genericLoadingError) {
      game.flameThreadActive = false;
      g.setFont(new Font("Helvetica", Font.BOLD, 20));
      g.setColor(Color.white);
      g.drawString("Error - unable to load game!", 50, 50);
      g.drawString("To play " + ClientSettings.SERVER_NAME + " make sure you play from", 50, 100);
      g.drawString("" + ClientSettings.SERVER_WEBSITE + "", 50, 150);
    }
    if (game.rsAlreadyLoaded) {
      game.flameThreadActive = false;
      g.setColor(Color.yellow);
      int l = 35;
      g.drawString(
          "Error a copy of " + ClientSettings.SERVER_NAME + " already appears to be loaded",
          30,
          l);
      l += 50;
      g.setColor(Color.white);
      g.drawString("To fix this try the following (in order):", 30, l);
      l += 50;
      g.setColor(Color.white);
      g.setFont(new Font("Helvetica", Font.BOLD, 12));
      g.drawString("1: Try closing ALL open web-browser windows, and reloading", 30, l);
      l += 30;
      g.drawString("2: Try rebooting your computer, and reloading", 30, l);
      l += 30;
    }
  }
}
