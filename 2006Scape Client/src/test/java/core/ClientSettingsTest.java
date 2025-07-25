package core;

import static org.junit.Assert.*;

import core.engine.ClientSettings;
import org.junit.Test;

public class ClientSettingsTest {
  @Test
  public void testDefaultValues() {
    assertFalse(ClientSettings.CONTROL_KEY_ZOOMING);
    assertFalse(ClientSettings.SHOW_ZOOM_LEVEL_MESSAGES);
    assertEquals("2006Scape", ClientSettings.SERVER_NAME);
    assertEquals("https://2006Scape.org/", ClientSettings.SERVER_WEBSITE);
  }
}
