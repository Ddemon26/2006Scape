package util;

import static org.junit.Assert.*;

import org.junit.Test;
import util.compression.BZip2State;

public class BZip2StateTest {
  @Test
  public void testArrayLengths() {
    BZip2State state = new BZip2State();
    assertEquals(256, state.unzftab.length);
    assertEquals(257, state.cftab.length);
    assertEquals(256, state.inUse.length);
    assertEquals(16, state.inUse16.length);
    assertEquals(256, state.seqToUnseq.length);
    assertEquals(4096, state.mtfa.length);
    assertEquals(16, state.mtfbase.length);
    assertEquals(18002, state.selector.length);
    assertEquals(6, state.tempLen.length);
    assertEquals(258, state.tempLen[0].length);
    assertEquals(6, state.limit.length);
    assertEquals(258, state.limit[0].length);
    assertEquals(6, state.base.length);
    assertEquals(258, state.base[0].length);
    assertEquals(6, state.perm.length);
    assertEquals(258, state.perm[0].length);
    assertEquals(6, state.minLens.length);
  }
}
