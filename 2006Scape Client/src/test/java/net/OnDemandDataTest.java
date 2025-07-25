package net;

import static org.junit.Assert.*;

import core.network.OnDemandData;
import org.junit.Test;

public class OnDemandDataTest {
  @Test
  public void testConstructorSetsIncomplete() {
    OnDemandData data = new OnDemandData();
    assertTrue(data.incomplete);
  }
}
