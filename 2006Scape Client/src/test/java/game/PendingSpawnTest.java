package game;

import org.junit.Test;
import static org.junit.Assert.*;

public class PendingSpawnTest {
    @Test
    public void testDefaultDelay() {
        PendingSpawn spawn = new PendingSpawn();
        assertEquals(-1, spawn.delay);
    }
}
