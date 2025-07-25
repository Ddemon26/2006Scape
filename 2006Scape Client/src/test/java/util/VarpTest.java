package util;

import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import org.junit.Test;
import util.configuration.Varp;

public class VarpTest {

  @Test
  public void testPrivateConstructorSetsInactive() throws Exception {
    Constructor<Varp> ctor = Varp.class.getDeclaredConstructor();
    ctor.setAccessible(true);
    Varp varp = ctor.newInstance();
    assertFalse(varp.isActive);
    assertEquals(0, varp.actionType);
  }
}
