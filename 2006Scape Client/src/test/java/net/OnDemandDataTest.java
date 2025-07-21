package net;

import org.junit.Test;
import static org.junit.Assert.*;

public class OnDemandDataTest {
    @Test
    public void testConstructorSetsIncomplete() {
        OnDemandData data = new OnDemandData();
        assertTrue(data.incomplete);
    }
}
