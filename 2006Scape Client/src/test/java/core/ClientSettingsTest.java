package core;

import org.junit.Test;
import static org.junit.Assert.*;

public class ClientSettingsTest {
    @Test
    public void testDefaultValues() {
        assertFalse(ClientSettings.CONTROL_KEY_ZOOMING);
        assertFalse(ClientSettings.SHOW_ZOOM_LEVEL_MESSAGES);
        assertEquals("2006Scape", ClientSettings.SERVER_NAME);
        assertEquals("https://2006Scape.org/", ClientSettings.SERVER_WEBSITE);
    }
}
