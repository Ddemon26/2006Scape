package ui;

import org.junit.Test;
import static org.junit.Assert.*;

public class RSInterfaceTest {
    @Test
    public void testSwapInventoryItems() {
        RSInterface rsInterface = new RSInterface();
        rsInterface.inv = new int[] {1, 2, 3};
        rsInterface.invStackSizes = new int[] {10, 20, 30};
        rsInterface.swapInventoryItems(0, 1);
        assertArrayEquals(new int[] {2, 1, 3}, rsInterface.inv);
        assertArrayEquals(new int[] {20, 10, 30}, rsInterface.invStackSizes);
    }
}
