package util.helpers;

import core.engine.ClientSettings;
import core.engine.Game;
import javax.imageio.ImageIO;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/** Utility for capturing screenshots. Extracted from {@link Game}. */
public final class ScreenshotUtil {
    private ScreenshotUtil() {}

    /**
     * Captures the current game window to a PNG file. A message is sent in-game
     * when {@code sendMessage} is true.
     */
    public static void capture(Game game, boolean sendMessage, String... subfolders) {
        try {
            Window window = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusedWindow();
            if (window == null) {
                return;
            }
            Point point = window.getLocationOnScreen();
            int x = (int) point.getX();
            int y = (int) point.getY();
            int w = window.getWidth();
            int h = window.getHeight();
            Robot robot = new Robot(window.getGraphicsConfiguration().getDevice());
            Rectangle captureSize = new Rectangle(x, y, w, h);
            BufferedImage image = robot.createScreenCapture(captureSize);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss");
            String dateTime = dateFormat.format(new Date());
            String fileExtension = game.myUsername != null && !game.myUsername.isEmpty()
                ? game.myUsername : ClientSettings.SERVER_NAME;

            String subfolderPath = String.join(File.separator, subfolders);
            if (!subfolderPath.isEmpty()) {
                subfolderPath += File.separator;
            }
            String screenshotDir = System.getProperty("user.home") + File.separatorChar
                + ClientSettings.SERVER_NAME + File.separatorChar + "screenshots" + File.separatorChar
                + subfolderPath;
            File dir = new File(screenshotDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(screenshotDir, fileExtension + "_" + dateTime + ".png");
            if (!file.exists()) {
                ImageIO.write(image, "png", file);
                if (sendMessage) {
                    game.pushMessage("A picture has been saved in your screenshots folder.", 0, "");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
