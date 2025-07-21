package util;

import org.junit.Test;
import static org.junit.Assert.*;

public class SizeConstantsTest {
    @Test
    public void testPermutationTableBasics() {
        assertEquals(257, SizeConstants.permutationTable.length);
        assertEquals(6, SizeConstants.permutationTable[0]);
        assertEquals(0, SizeConstants.permutationTable[256]);
    }

    @Test
    public void testPacketSizesBasics() {
        assertEquals(282, SizeConstants.packetSizes.length);
        assertEquals(0, SizeConstants.packetSizes[0]);
        assertTrue(SizeConstants.packetSizes[36] < 0);
    }
}
