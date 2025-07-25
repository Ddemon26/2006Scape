package core.handlers;

import core.engine.Game;
import core.engine.ClientSettings;
import util.helpers.ClipboardUtil;
import util.helpers.ScreenshotUtil;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import core.world.WorldController;

/**
 * Processes keyboard and mouse wheel input extracted from {@link Game}.
 */
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
        if (ClientSettings.SCREENSHOTS_ENABLED &&
            keyevent.getKeyCode() == KeyEvent.VK_PRINTSCREEN &&
            keyevent.isControlDown()) {
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
}
