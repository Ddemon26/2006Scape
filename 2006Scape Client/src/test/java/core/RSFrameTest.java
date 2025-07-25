package core;

import static org.junit.Assert.*;

import core.engine.ClientSettings;
import core.engine.RSApplet;
import core.engine.RSFrame;
import org.junit.Test;

public class RSFrameTest {
  @Test
  public void testConstructorSetsTitleAndProperties() {
    RSApplet applet = new RSApplet();
    RSFrame frame = new RSFrame(applet);
    String expectedTitle =
        ClientSettings.SERVER_NAME
            + " World: "
            + ClientSettings.SERVER_WORLD
            + ((ClientSettings.SERVER_IP.equals("localhost")
                    || ClientSettings.SERVER_IP.equals("127.0.0.1"))
                ? " [Local]"
                : "");
    assertEquals(expectedTitle, frame.getTitle());
    assertFalse(frame.isResizable());
    assertTrue(frame.isVisible());
  }
}
