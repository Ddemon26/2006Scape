import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.junit.Test;
import static org.junit.Assert.*;

public class ModelFaceBoundsTest {
    private static boolean call(int... a) throws Exception {
        Constructor<Model> c = Model.class.getDeclaredConstructor();
        c.setAccessible(true);
        Model m = c.newInstance();
        Method f = Model.class.getDeclaredMethod("isPointWithinFaceBounds", int.class, int.class,
                int.class, int.class, int.class, int.class, int.class, int.class);
        f.setAccessible(true);
        Object[] args = new Object[a.length];
        for (int n = 0; n < a.length; n++) args[n] = a[n];
        return (Boolean) f.invoke(m, args);
    }

    @Test public void allFourSidesAreInclusiveAndOnePastIsRejected() throws Exception {
        for (int[] p : new int[][]{{10,20},{40,20},{10,60},{40,60},{25,35}})
            assertTrue(call(p[0],p[1],20,60,35,10,40,25));
        for (int[] p : new int[][]{{9,35},{41,35},{25,19},{25,61}})
            assertFalse(call(p[0],p[1],20,60,35,10,40,25));
    }

    @Test public void argumentsZeroAndOneVaryIndependentlyOnAsymmetricAxes() throws Exception {
        assertTrue(call(-8,70,60,80,75,-10,-5,-7));
        assertFalse(call(70,-8,60,80,75,-10,-5,-7));
        assertFalse(call(-11,70,60,80,75,-10,-5,-7));
        assertFalse(call(-8,81,60,80,75,-10,-5,-7));
        assertTrue(call(-8,60,60,80,75,-10,-5,-7));
        assertTrue(call(-5,70,60,80,75,-10,-5,-7));
    }

    @Test public void eachOfTheSixVertexArgumentsCanAloneSetEitherLimit() throws Exception {
        for (int slot = 2; slot < 8; slot++) {
            int[] a = {0,0,10,10,10,10,10,10};
            // The other coordinate is held exactly on its sole admissible value.
            a[slot < 5 ? 0 : 1] = 10;
            a[slot] = -3;
            a[slot < 5 ? 1 : 0] = -3;
            assertTrue("slot " + slot + " lower edge",call(a));
            a[slot < 5 ? 1 : 0] = -4;
            assertFalse("slot " + slot + " below",call(a));
            a[slot] = 23;
            a[slot < 5 ? 1 : 0] = 23;
            assertTrue("slot " + slot + " upper edge",call(a));
            a[slot < 5 ? 1 : 0] = 24;
            assertFalse("slot " + slot + " above",call(a));
        }
    }

    @Test public void permutationsAndIndependentCoordinatePermutationsDoNotChangeResults() throws Exception {
        int[][] orders = {{0,1,2},{0,2,1},{1,0,2},{1,2,0},{2,0,1},{2,1,0}};
        int[] x = {-7,13,4}, y = {80,40,65};
        for (int[] a : orders) for (int[] b : orders) {
            assertTrue(call(12,79,y[a[0]],y[a[1]],y[a[2]],x[b[0]],x[b[1]],x[b[2]]));
            assertFalse(call(14,79,y[a[0]],y[a[1]],y[a[2]],x[b[0]],x[b[1]],x[b[2]]));
            assertFalse(call(12,39,y[a[0]],y[a[1]],y[a[2]],x[b[0]],x[b[1]],x[b[2]]));
        }
    }

    @Test public void acceptsPointsOutsideTriangleAndDoesNotTestWinding() throws Exception {
        // Triangle (0,0),(10,0),(0,10): (9,9) lies outside x+y<=10.
        assertTrue(call(9,9,0,0,10,0,10,0));
        assertTrue(call(9,9,10,0,0,0,10,0));
        assertFalse(call(11,9,0,0,10,0,10,0));
        // Independent coordinate ranges do not require a vertex at the point.
        assertTrue(call(10,10,0,0,10,0,10,0));
    }

    @Test public void degenerateInputsKeepInclusiveLineAndPointMembership() throws Exception {
        assertTrue(call(4,-2,-2,-2,-2,4,4,4));
        assertFalse(call(5,-2,-2,-2,-2,4,4,4));
        assertFalse(call(4,-1,-2,-2,-2,4,4,4));
        assertTrue(call(4,7,-2,7,3,4,4,4));
        assertFalse(call(3,7,-2,7,3,4,4,4));
        assertTrue(call(7,-2,-2,-2,-2,4,9,6));
        assertFalse(call(7,-3,-2,-2,-2,4,9,6));
    }

    @Test public void signedIntegerLimitsUseComparisonsWithoutOverflowingDistances() throws Exception {
        int lo = Integer.MIN_VALUE, hi = Integer.MAX_VALUE;
        assertTrue(call(lo,hi,lo,hi,0,hi,lo,0));
        assertTrue(call(hi,lo,lo,hi,0,hi,lo,0));
        assertFalse(call(lo,0,lo,hi,0,lo+1,hi,0));
        assertFalse(call(0,hi,lo,hi-1,0,hi,lo,0));
        assertTrue(call(-25,-35,-50,-20,-30,-40,-10,-20));
        assertFalse(call(-25,-19,-50,-20,-30,-40,-10,-20));
    }
}
