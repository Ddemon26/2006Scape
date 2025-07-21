package core;

import org.junit.Test;

import static org.junit.Assert.*;

public class LocalGameTest {
    @Test
    public void testConstructorSetsLocalhostServer() {
        new LocalGame();
        assertEquals("127.0.0.1", Game.server);
    }
}
