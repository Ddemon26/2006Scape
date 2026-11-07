import java.lang.reflect.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class ModelBoundsTest {
    private static Model model(int[] x, int[] y, int[] z) throws Exception {
        Constructor<Model> c = Model.class.getDeclaredConstructor(); c.setAccessible(true);
        Model m = c.newInstance(); m.vertexCount = x.length;
        m.vertexX = x.clone(); m.vertexY = y.clone(); m.vertexZ = z.clone();
        return m;
    }
    private static int field(Model m, String name) throws Exception {
        Field f = Model.class.getDeclaredField(name); f.setAccessible(true); return f.getInt(m);
    }
    private static void extents(Model m) throws Exception {
        Method f = Model.class.getDeclaredMethod("calculateBoundsWithExtents"); f.setAccessible(true); f.invoke(m);
    }
    private static void bounds(Model m, int radius, int above, int below, int offset, int buckets) throws Exception {
        assertEquals(radius, m.radiusXZ); assertEquals(above, m.modelHeight); assertEquals(below, m.maxY);
        assertEquals(offset, field(m,"depthBucketOffset")); assertEquals(buckets, field(m,"depthBucketCount"));
    }
    @Test public void independentAxesUseOriginCenteredXZRadiusAndSignedYExtents() throws Exception {
        Model m = model(new int[]{3,0,900},new int[]{-12,8,-900},new int[]{4,0,900}); m.vertexCount=2;
        m.calculateBounds(); bounds(m,5,12,8,13,23);
        assertArrayEquals(new int[]{3,0,900},m.vertexX);
        assertArrayEquals(new int[]{-12,8,-900},m.vertexY);
        assertArrayEquals(new int[]{4,0,900},m.vertexZ);
        // A second point on the opposite side does not turn radius into diameter or box half-width.
        m = model(new int[]{-3,3},new int[]{0,0},new int[]{-4,4}); m.calculateBounds(); bounds(m,5,0,0,5,10);
        m = model(new int[]{0},new int[]{7},new int[]{0}); m.calculateBounds(); bounds(m,0,0,7,0,7);
        m = model(new int[]{0},new int[]{-7},new int[]{0}); m.calculateBounds(); bounds(m,0,7,0,7,7);
    }
    @Test public void roundedVariantAddsPoint99RatherThanAlwaysTakingCeiling() throws Exception {
        Model m=model(new int[]{100},new int[]{0},new int[]{1}); m.calculateBounds(); bounds(m,100,0,0,100,200);
        m=model(new int[]{100},new int[]{0},new int[]{2}); m.calculateBounds(); bounds(m,101,0,0,101,202);
        m=model(new int[]{49},new int[]{0},new int[]{1}); m.calculateBounds(); bounds(m,50,0,0,50,100);
        extents(m); bounds(m,49,0,0,49,98);
    }
    @Test public void yOnlyRefreshUsesCachedRadiusAndNeverReadsXZ() throws Exception {
        Model m=model(new int[]{3,0},new int[]{-12,8},new int[]{4,0}); m.calculateBounds();
        m.vertexX=null; m.vertexZ=null; m.vertexY=new int[]{-24,7};
        m.minX=91; m.maxX=92; m.maxZ=93; m.minZ=94;
        m.recalculateYBounds(); bounds(m,5,24,7,25,34);
        assertEquals(91,m.minX); assertEquals(92,m.maxX); assertEquals(93,m.maxZ); assertEquals(94,m.minZ);
        m.radiusXZ=0; m.recalculateYBounds(); bounds(m,0,24,7,24,31);
    }
    @Test public void fullExtentsTrackXAndZSeparatelyAndTruncateDepthRadii() throws Exception {
        Model m=model(new int[]{-3,6,999},new int[]{-12,8,-999},new int[]{4,-8,999}); m.vertexCount=2;
        extents(m); bounds(m,10,12,8,15,27);
        assertEquals(-3,m.minX); assertEquals(6,m.maxX); assertEquals(-8,m.minZ); assertEquals(4,m.maxZ);
        m.calculateBounds(); bounds(m,10,12,8,16,29);
        // The rounded routine intentionally leaves previously calculated box extents alone.
        assertEquals(-3,m.minX); assertEquals(6,m.maxX); assertEquals(-8,m.minZ); assertEquals(4,m.maxZ);
    }
    @Test public void emptyAndNegativeCountsResetBoundsButRetainExtentSentinels() throws Exception {
        for(int count:new int[]{0,-1}) {
            Model m=model(new int[]{5},new int[]{6},new int[]{7}); m.vertexCount=count;
            m.modelHeight=81; m.radiusXZ=82; m.maxY=83; m.calculateBounds(); bounds(m,0,0,0,0,0);
            m.radiusXZ=5; m.recalculateYBounds(); bounds(m,5,0,0,5,10);
            extents(m); bounds(m,0,0,0,0,0);
            assertEquals(999999,m.minX); assertEquals(-999999,m.maxX);
            assertEquals(-99999,m.maxZ); assertEquals(99999,m.minZ);
        }
    }
    @Test public void extentSentinelsLimitExtremeInputsInsteadOfGuaranteeingExactMinMax() throws Exception {
        Model m=model(new int[]{1000000},new int[]{0},new int[]{100000}); extents(m);
        assertEquals(999999,m.minX); assertEquals(1000000,m.maxX);
        assertEquals(99999,m.minZ); assertEquals(100000,m.maxZ);
        m=model(new int[]{-1000000},new int[]{0},new int[]{-100000}); extents(m);
        assertEquals(-1000000,m.minX); assertEquals(-999999,m.maxX);
        assertEquals(-100000,m.minZ); assertEquals(-99999,m.maxZ);
    }
    @Test public void integerOverflowAndNaNConversionRemainObservable() throws Exception {
        Model m=model(new int[]{46341},new int[]{Integer.MIN_VALUE},new int[]{0}); m.calculateBounds(); bounds(m,0,0,0,0,0);
        m=model(new int[]{65536},new int[]{46341},new int[]{0}); m.calculateBounds(); bounds(m,0,0,46341,0,0);
        extents(m); bounds(m,0,0,46341,0,0);
        m=model(new int[]{0},new int[]{-46341},new int[]{0}); m.calculateBounds(); bounds(m,0,46341,0,0,0);
    }
    @Test public void malformedArraysKeepOriginalPartialWritesAndFailureOrder() throws Exception {
        Model m=model(new int[]{3,8},new int[]{-12},new int[]{4,7});
        try { m.calculateBounds(); fail(); } catch(ArrayIndexOutOfBoundsException expected) { }
        bounds(m,25,12,0,0,0); // Radius field still holds squared distance when the loop fails.
        m=model(new int[]{3,8},new int[]{-12},new int[]{4,7});
        try { extents(m); fail(); } catch(InvocationTargetException expected) {
            assertTrue(expected.getCause() instanceof ArrayIndexOutOfBoundsException);
        }
        bounds(m,25,12,0,0,0); assertEquals(3,m.minX); assertEquals(3,m.maxX);
        assertEquals(4,m.maxZ); assertEquals(4,m.minZ);
    }
}
