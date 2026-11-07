import java.lang.reflect.*;
import org.junit.Test;
import static org.junit.Assert.*;

public class ModelSpatialTransformTest {
    private static Model model(int[] x, int[] y, int[] z) throws Exception {
        Constructor<Model> c = Model.class.getDeclaredConstructor(); c.setAccessible(true);
        Model m = c.newInstance(); m.vertexCount = x.length;
        m.vertexX = x.clone(); m.vertexY = y.clone(); m.vertexZ = z.clone();
        m.faceCount = 1; m.faceVertexA = new int[]{0,91};
        m.faceVertexB = new int[]{1,92}; m.faceVertexC = new int[]{2,93};
        return m;
    }
    private static void coordinates(Model m, int[] x, int[] y, int[] z) {
        assertArrayEquals(x, m.vertexX); assertArrayEquals(y, m.vertexY);
        assertArrayEquals(z, m.vertexZ);
    }
    @Test public void translationUsesIndependentModelAxesAndOnlyActivePrefix() throws Exception {
        Model m = model(new int[]{10,-20,91}, new int[]{30,-40,92}, new int[]{50,-60,93});
        m.vertexCount = 2; m.translate(7,-11,13);
        coordinates(m, new int[]{17,-13,91}, new int[]{19,-51,92}, new int[]{63,-47,93});
        m.translate(-7,11,-13);
        coordinates(m, new int[]{10,-20,91}, new int[]{30,-40,92}, new int[]{50,-60,93});
    }
    @Test public void quarterTurnMapsPositiveXToNegativeZAndFourTurnsRestore() throws Exception {
        Model m = model(new int[]{10,0,91}, new int[]{3,4,92}, new int[]{0,20,93}); m.vertexCount = 2;
        m.rotateYQuarterTurn(); coordinates(m,new int[]{0,20,91},new int[]{3,4,92},new int[]{-10,0,93});
        for (int i=0;i<3;i++) m.rotateYQuarterTurn();
        coordinates(m,new int[]{10,0,91},new int[]{3,4,92},new int[]{0,20,93});
    }
    @Test public void xRotationUses2048UnitsPerTurnAndPreservesX() throws Exception {
        // The client's approximate radian step gives 65535 at three cardinal extrema.
        int[][] expectedY = {{7,0},{0,-11},{-7,0},{0,10}};
        int[][] expectedZ = {{0,11},{6,0},{0,-11},{-7,0}};
        for (int quadrant=0;quadrant<4;quadrant++) {
            Model m = model(new int[]{31,32,91},new int[]{7,0,92},new int[]{0,11,93}); m.vertexCount=2;
            m.rotateX(quadrant*512);
            coordinates(m,new int[]{31,32,91},new int[]{expectedY[quadrant][0],expectedY[quadrant][1],92},
                new int[]{expectedZ[quadrant][0],expectedZ[quadrant][1],93});
        }
    }
    @Test public void obliqueXRotationUses16BitTrigAndArithmeticShiftRounding() throws Exception {
        assertEquals(46340, Model.sineTable16[256]); assertEquals(46340, Model.cosineTable16[256]);
        Model m = model(new int[]{5,6,7},new int[]{1000,0,-1000},new int[]{0,1,0}); m.rotateX(256);
        coordinates(m,new int[]{5,6,7},new int[]{707,-1,-708},new int[]{707,0,-708});
    }
    @Test public void angleIsUncheckedTableIndexEvenForNoVertices() throws Exception {
        for (int angle : new int[]{-1,2048,Integer.MAX_VALUE}) {
            Model m = model(new int[0],new int[0],new int[0]);
            try { m.rotateX(angle); fail(); } catch (ArrayIndexOutOfBoundsException expected) { }
        }
        Model m = model(new int[0],new int[0],new int[0]); m.rotateX(2047);
    }
    @Test public void scalingParametersAreXThenZThenYWith128Identity() throws Exception {
        Model m = model(new int[]{8,-8,91},new int[]{12,-12,92},new int[]{16,-16,93}); m.vertexCount=2;
        m.scale(256,64,-128);
        coordinates(m,new int[]{16,-16,91},new int[]{-12,12,92},new int[]{8,-8,93});
        m.scale(128,128,128);
        coordinates(m,new int[]{16,-16,91},new int[]{-12,12,92},new int[]{8,-8,93});
    }
    @Test public void scalingTruncatesTowardZeroAndRetainsIntOverflow() throws Exception {
        Model m = model(new int[]{-3,3,Integer.MAX_VALUE},new int[]{3,-3,Integer.MIN_VALUE},new int[]{-3,3,65536});
        m.scale(64,64,64);
        coordinates(m,new int[]{-1,1,0},new int[]{1,-1,0},new int[]{-1,1,32768});
        m = model(new int[]{65536},new int[]{0},new int[]{0}); m.scale(65536,128,128);
        assertEquals(0,m.vertexX[0]);
    }
    private static long[] normal(Model m) {
        int a=m.faceVertexA[0],b=m.faceVertexB[0],c=m.faceVertexC[0];
        long ux=m.vertexX[b]-m.vertexX[a],uy=m.vertexY[b]-m.vertexY[a],uz=m.vertexZ[b]-m.vertexZ[a];
        long vx=m.vertexX[c]-m.vertexX[a],vy=m.vertexY[c]-m.vertexY[a],vz=m.vertexZ[c]-m.vertexZ[a];
        return new long[]{uy*vz-uz*vy,uz*vx-ux*vz,ux*vy-uy*vx};
    }
    @Test public void reflectionReversesFaceWindingToReflectTheGeometricNormal() throws Exception {
        Model m=model(new int[]{0,2,0,91},new int[]{0,0,3,92},new int[]{1,2,3,93}); m.vertexCount=3;
        long[] n=normal(m); m.reflectZAndReverseWinding();
        coordinates(m,new int[]{0,2,0,91},new int[]{0,0,3,92},new int[]{-1,-2,-3,93});
        assertArrayEquals(new int[]{2,91},m.faceVertexA); assertArrayEquals(new int[]{1,92},m.faceVertexB);
        assertArrayEquals(new int[]{0,93},m.faceVertexC); assertArrayEquals(new long[]{n[0],n[1],-n[2]},normal(m));
        m.reflectZAndReverseWinding(); assertArrayEquals(n,normal(m)); assertArrayEquals(new int[]{1,2,3,93},m.vertexZ);
    }
    @Test public void translationAndNegationKeepJavaWraparoundAndShortArraysKeepPartialWrites() throws Exception {
        Model m=model(new int[]{Integer.MAX_VALUE},new int[]{Integer.MIN_VALUE},new int[]{Integer.MIN_VALUE});
        m.translate(1,-1,0); coordinates(m,new int[]{Integer.MIN_VALUE},new int[]{Integer.MAX_VALUE},new int[]{Integer.MIN_VALUE});
        m.rotateYQuarterTurn(); coordinates(m,new int[]{Integer.MIN_VALUE},new int[]{Integer.MAX_VALUE},new int[]{Integer.MIN_VALUE});
        m.reflectZAndReverseWinding(); assertEquals(Integer.MIN_VALUE,m.vertexZ[0]);
        m=model(new int[]{1,2},new int[]{3},new int[]{4,5});
        try { m.translate(10,20,30); fail(); } catch (ArrayIndexOutOfBoundsException expected) { }
        coordinates(m,new int[]{11,12},new int[]{23},new int[]{34,5});
    }
    @Test public void emptyAndNegativeCountsLeaveBuffersAndNonSpatialStateUntouched() throws Exception {
        for(int count : new int[]{0,-1}) {
            Model m=model(new int[]{1},new int[]{2},new int[]{3}); m.vertexCount=count; m.faceCount=count;
            int[] x=m.vertexX,y=m.vertexY,z=m.vertexZ;
            m.rotateYQuarterTurn(); m.rotateX(256); m.translate(4,5,6); m.reflectZAndReverseWinding(); m.scale(7,8,9);
            coordinates(m,new int[]{1},new int[]{2},new int[]{3}); assertSame(x,m.vertexX);
            assertSame(y,m.vertexY); assertSame(z,m.vertexZ);
            assertArrayEquals(new int[]{0,91},m.faceVertexA); assertArrayEquals(new int[]{1,92},m.faceVertexB);
            assertArrayEquals(new int[]{2,93},m.faceVertexC); assertEquals(count,m.vertexCount); assertEquals(count,m.faceCount);
        }
    }
}
