import java.lang.reflect.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class ModelFaceColorTest {
    private static Model model(int count, int... colors) throws Exception {
        Constructor<Model> c = Model.class.getDeclaredConstructor(); c.setAccessible(true);
        Model m = c.newInstance(); m.faceCount = count; m.faceColors = colors;
        return m;
    }
    private static int shade(int color, int light, int flags) throws Exception {
        Method m = Model.class.getDeclaredMethod("calculateFaceShade", int.class, int.class, int.class);
        m.setAccessible(true); return (Integer) m.invoke(null, color, light, flags);
    }
    @Test public void recoloringReplacesExactMatchesAcrossActiveFacesWithoutTouchingTailOrGeometry() throws Exception {
        Model m = model(5, 500, 501, 500, -500, 900, 500);
        m.vertexCount = 1; m.vertexX = new int[]{500};
        m.faceVertexA = new int[]{0,1,2,3,4}; m.faceRenderFlags = new int[]{0,1,2,3,0};
        int[] colors = m.faceColors;
        m.recolor(500, 900);
        assertSame(colors, m.faceColors);
        assertArrayEquals(new int[]{900,501,900,-500,900,500}, colors);
        assertArrayEquals(new int[]{500}, m.vertexX);
        assertArrayEquals(new int[]{0,1,2,3,4}, m.faceVertexA);
        assertArrayEquals(new int[]{0,1,2,3,0}, m.faceRenderFlags);
        assertEquals(5, m.faceCount); assertEquals(1, m.vertexCount);
    }
    @Test public void replacementIsDirectionalAndSequentialCallsCanCascade() throws Exception {
        Model m = model(4, 10,20,10,30);
        m.recolor(10,20); assertArrayEquals(new int[]{20,20,20,30},m.faceColors);
        m.recolor(20,30); assertArrayEquals(new int[]{30,30,30,30},m.faceColors);
        m.recolor(30,30); assertArrayEquals(new int[]{30,30,30,30},m.faceColors);
        m.recolor(99,0); assertArrayEquals(new int[]{30,30,30,30},m.faceColors);
    }
    @Test public void replacementUsesFullSignedIntegersAndIgnoresRenderFlags() throws Exception {
        Model m = model(4, -1,65535,Integer.MIN_VALUE,Integer.MAX_VALUE);
        m.faceRenderFlags = new int[]{0,1,2,3};
        m.recolor(-1,Integer.MAX_VALUE); m.recolor(Integer.MIN_VALUE,-1);
        assertArrayEquals(new int[]{Integer.MAX_VALUE,65535,-1,Integer.MAX_VALUE},m.faceColors);
        m.recolor(65535,0x12345678);
        assertEquals(0x12345678,m.faceColors[1]);
    }
    @Test public void nonpositiveCountsDoNotReadNullButMalformedPositiveCountsRetainPartialWrites() throws Exception {
        Model m = model(0); m.faceColors = null; m.recolor(1,2);
        m.faceCount = -1; m.recolor(1,2);
        m.faceCount = 1;
        try { m.recolor(1,2); fail(); } catch (NullPointerException expected) { }
        m = model(3, 7,8);
        try { m.recolor(7,9); fail(); } catch (ArrayIndexOutOfBoundsException expected) { }
        assertArrayEquals(new int[]{9,8},m.faceColors);
    }
    @Test public void untexturedShadeScalesLowSevenBitsAndRetainsUpperPackedColor() throws Exception {
        assertEquals(0x1240,shade(0x1240,128,0));
        assertEquals(0x1220,shade(0x1240,64,0));
        assertEquals(0x1230,shade(0x1260,64,0));
        assertEquals(0x3420,shade(0x3440,64,0));
        assertEquals(0x1202,shade(0x1200,128,0));
        assertEquals(0x1202,shade(0x1201,128,0));
        assertEquals(0x127e,shade(0x127f,128,0));
    }
    @Test public void untexturedLightClampsAfterMultiplicationAndUsesArithmeticShiftRounding() throws Exception {
        assertEquals(0x1202,shade(0x1240,-1,0));
        assertEquals(0x1202,shade(0x1240,0,0));
        assertEquals(0x1202,shade(0x1240,4,0));
        assertEquals(0x1203,shade(0x1240,6,0));
        assertEquals(0x127e,shade(0x1240,252,0));
        assertEquals(0x127e,shade(0x1240,254,0));
        assertEquals(0x123f,shade(0x127f,64,0));
    }
    @Test public void texturedShadeIgnoresColorClampsLightToZeroThrough127AndReversesIt() throws Exception {
        int[] light = {Integer.MIN_VALUE,-1,0,1,64,126,127,128,Integer.MAX_VALUE};
        int[] expected = {127,127,127,126,63,1,0,0,0};
        for (int i=0;i<light.length;i++) {
            assertEquals(expected[i],shade(0x1240,light[i],2));
            assertEquals(expected[i],shade(Integer.MIN_VALUE,light[i],3));
        }
    }
    @Test public void onlyFlagBitOneSelectsTextureAndPackedColorIsMaskedTo16Bits() throws Exception {
        for (int flags : new int[]{0,1,4,8,Integer.MIN_VALUE}) assertEquals(0x1220,shade(0x1240,64,flags));
        for (int flags : new int[]{2,3,6,-1,Integer.MIN_VALUE+2}) assertEquals(63,shade(0x1240,64,flags));
        assertEquals(0x1220,shade(0x76541240,64,0));
        assertEquals(65534,shade(-1,128,0));
        assertEquals(2,shade(Integer.MIN_VALUE,128,0));
    }
    @Test public void untexturedMultiplicationKeepsJavaIntOverflowBeforeShiftAndClamp() throws Exception {
        assertEquals(2,shade(64,1<<26,0));
        assertEquals(126,shade(1,Integer.MAX_VALUE,0));
        assertEquals(2,shade(64,Integer.MAX_VALUE,0));
        assertEquals(2,shade(127,Integer.MIN_VALUE,0));
    }
}
