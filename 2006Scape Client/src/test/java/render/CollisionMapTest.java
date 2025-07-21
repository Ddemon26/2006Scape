package render;

import org.junit.Test;

import static org.junit.Assert.*;

public class CollisionMapTest {
    @Test
    public void testResetInitializesClippingFlags() {
        CollisionMap map = new CollisionMap();
        for (int x = 0; x < 104; x++) {
            assertEquals(0xffffff, map.clippingFlags[x][0]);
            assertEquals(0xffffff, map.clippingFlags[x][103]);
        }
        for (int y = 1; y < 103; y++) {
            assertEquals(0xffffff, map.clippingFlags[0][y]);
            assertEquals(0xffffff, map.clippingFlags[103][y]);
        }
        assertEquals(0x1000000, map.clippingFlags[1][1]);
    }

    @Test
    public void testBlockAndUnblockTile() {
        CollisionMap map = new CollisionMap();
        map.blockTile(1, 1);
        assertEquals(0x1200000, map.clippingFlags[1][1]);
        map.unblockTile(1, 1);
        assertEquals(0, map.clippingFlags[1][1]);
    }
}
