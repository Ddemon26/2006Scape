package util;

import org.junit.Test;
import util.configuration.Varp;

import java.lang.reflect.Constructor;

import static org.junit.Assert.*;

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
