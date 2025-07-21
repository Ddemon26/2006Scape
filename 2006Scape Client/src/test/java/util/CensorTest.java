package util;

import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.*;

public class CensorTest {

    private Object invokePrivate(String name, Class<?>[] types, Object... args) throws Exception {
        Method m = Censor.class.getDeclaredMethod(name, types);
        m.setAccessible(true);
        return m.invoke(null, args);
    }

    @Test
    public void testComputeNameHash() throws Exception {
        int hash1 = (int) invokePrivate("computeNameHash", new Class[] {char[].class}, "bob".toCharArray());
        int hash2 = (int) invokePrivate("computeNameHash", new Class[] {char[].class}, "bob12".toCharArray());
        assertEquals(3460, hash1);
        assertEquals(64148828, hash2);
    }

    @Test
    public void testCharacterChecks() throws Exception {
        assertTrue((Boolean) invokePrivate("isDigit", new Class[] {char.class}, '5'));
        assertTrue((Boolean) invokePrivate("isLetter", new Class[] {char.class}, 'A'));
        assertTrue((Boolean) invokePrivate("isLowerCaseLetter", new Class[] {char.class}, 'z'));
        assertTrue((Boolean) invokePrivate("isUpperCaseLetter", new Class[] {char.class}, 'Z'));
        assertFalse((Boolean) invokePrivate("isNonAlphanumeric", new Class[] {char.class}, 'a'));
    }
}
