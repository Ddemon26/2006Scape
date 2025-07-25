package util.helpers;

import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/** Utility for interacting with the system clipboard. Extracted from {@link Game}. */
public final class ClipboardUtil {
  private ClipboardUtil() {}

  /** Reads plain text from the system clipboard, filtering out unsupported characters. */
  public static String getClipboardText() {
    String myString = "";
    try {
      myString =
          (String)
              Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
    } catch (HeadlessException | UnsupportedFlavorException | IOException e) {
      e.printStackTrace();
    }

    StringBuilder output = new StringBuilder();
    for (int i = 0; i < myString.length(); i++) {
      int j = myString.charAt(i);
      if (j >= 32 && j <= 122) {
        output.append((char) j);
      }
    }
    return output.toString();
  }
}
