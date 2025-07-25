package util;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import org.junit.Test;
import util.configuration.VarBit;

public class VarBitTest {
  @Test
  public void testDefaultConstructorSetsInactive() throws Exception {
    VarBit vb = new VarBit();
    Field active = VarBit.class.getDeclaredField("isActive");
    active.setAccessible(true);
    assertFalse(active.getBoolean(vb));
  }
}
