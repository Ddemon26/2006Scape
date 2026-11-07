import java.lang.reflect.*;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

public class ModelAssemblyTest {
    private static Field field(String name) throws Exception {
        Field f = Model.class.getDeclaredField(name); f.setAccessible(true); return f;
    }
    private static void set(Model m, String name, Object value) throws Exception { field(name).set(m, value); }
    private static int[] ints(Model m, String name) throws Exception { return (int[]) field(name).get(m); }
    private static Model empty() throws Exception {
        Constructor<Model> c = Model.class.getDeclaredConstructor(); c.setAccessible(true); return c.newInstance();
    }
    private static Model part(int[] x, int[] y, int[] z, int[] a, int[] b, int[] c) throws Exception {
        Model m = empty(); m.anInt1626=x.length; m.anIntArray1627=x; m.anIntArray1628=y; m.anIntArray1629=z;
        m.anInt1630=a.length; m.anIntArray1631=a; m.anIntArray1632=b; m.anIntArray1633=c;
        m.anIntArray1640=new int[a.length]; Arrays.fill(m.anIntArray1640, 500);
        return m;
    }
    private static Model triangle() throws Exception {
        return part(new int[]{10,20,30}, new int[]{1,2,3}, new int[]{4,5,6}, new int[]{0},new int[]{1},new int[]{2});
    }
    private static int vertex(Model dst, Model src, int index) throws Exception {
        Method method=Model.class.getDeclaredMethod("getOrAddVertex",Model.class,int.class); method.setAccessible(true);
        try { return (Integer)method.invoke(dst,src,index); }
        catch(InvocationTargetException e) { throw (Exception)e.getCause(); }
    }
    @Test public void exactTriplesWeldAcrossPartsInReferenceOrderAndFirstLabelWins() throws Exception {
        Model a=triangle(), b=part(new int[]{10,20,30,999},new int[]{1,2,3,999},new int[]{4,5,6,999},new int[]{2},new int[]{0},new int[]{1});
        set(a,"vertexGroupIds",new int[]{7,8,9}); set(b,"vertexGroupIds",new int[]{17,18,19,20});
        Model out=new Model(2,new Model[]{a,b});
        assertEquals(3,out.anInt1626); assertEquals(2,out.anInt1630); assertEquals(7,out.anIntArray1627.length);
        assertArrayEquals(new int[]{10,20,30,0,0,0,0},out.anIntArray1627);
        assertArrayEquals(new int[]{0,2},out.anIntArray1631); assertArrayEquals(new int[]{1,0},out.anIntArray1632); assertArrayEquals(new int[]{2,1},out.anIntArray1633);
        assertArrayEquals(new int[]{7,8,9,0,0,0,0},ints(out,"vertexGroupIds"));
        assertArrayEquals(new int[]{10,20,30,999},b.anIntArray1627); assertArrayEquals(new int[]{17,18,19,20},ints(b,"vertexGroupIds"));
        out=new Model(2,new Model[]{b,a});
        assertArrayEquals(new int[]{30,10,20,0,0,0,0},out.anIntArray1627);
        assertArrayEquals(new int[]{19,17,18,0,0,0,0},ints(out,"vertexGroupIds"));
    }
    @Test public void changingAnySingleCoordinatePreventsWeldingWithoutQuantization() throws Exception {
        for(int axis=0;axis<3;axis++) {
            Model a=triangle(),b=triangle();
            new int[][]{b.anIntArray1627,b.anIntArray1628,b.anIntArray1629}[axis][0]++;
            Model out=new Model(2,new Model[]{a,b}); assertEquals(4,out.anInt1626);
            assertArrayEquals(new int[]{0,3},out.anIntArray1631);
        }
        Model a=part(new int[]{Integer.MIN_VALUE,Integer.MAX_VALUE},new int[]{-1,-1},new int[]{0,0},new int[]{1},new int[]{0},new int[]{1});
        Model out=new Model(1,new Model[]{a}); assertEquals(2,out.anInt1626);
        assertArrayEquals(new int[]{Integer.MAX_VALUE,Integer.MIN_VALUE},out.anIntArray1627);
    }
    @Test public void partCountSelectsPrefixAndNullPartsAreSkipped() throws Exception {
        Model a=triangle(); Model out=new Model(2,new Model[]{null,a,triangle()});
        assertEquals(3,out.anInt1626); assertEquals(1,out.anInt1630);
        for(int count:new int[]{0,-1}) { out=new Model(count,null); assertEquals(0,out.anInt1626); assertEquals(0,out.anInt1630); assertNotNull(ints(out,"vertexGroupIds")); }
        out=new Model(2,new Model[]{null,null}); assertEquals(0,out.anIntArray1627.length);
        try { new Model(2,new Model[]{a}); fail(); } catch(ArrayIndexOutOfBoundsException expected) { }
    }
    @Test public void textureTrianglesImportUnusedVerticesAndRebaseOnlyTexturedFlags() throws Exception {
        Model a=triangle(), b=triangle();
        set(a,"texturedFaceCount",1); set(a,"texturedFaceVertexA",new int[]{2}); set(a,"texturedFaceVertexB",new int[]{0}); set(a,"texturedFaceVertexC",new int[]{1});
        b.anIntArray1627=new int[]{10,20,30,40}; b.anIntArray1628=new int[]{1,2,3,4}; b.anIntArray1629=new int[]{4,5,6,7}; b.anInt1626=4;
        b.anInt1630=4; b.anIntArray1631=new int[]{0,0,0,0}; b.anIntArray1632=new int[]{1,1,1,1}; b.anIntArray1633=new int[]{2,2,2,2}; b.anIntArray1640=new int[]{11,12,13,14};
        b.anIntArray1637=new int[]{0,1,2,7}; set(b,"texturedFaceCount",1); set(b,"texturedFaceVertexA",new int[]{3}); set(b,"texturedFaceVertexB",new int[]{2}); set(b,"texturedFaceVertexC",new int[]{1});
        Model out=new Model(2,new Model[]{a,b});
        assertEquals(4,out.anInt1626); assertEquals(2,field("texturedFaceCount").getInt(out));
        assertArrayEquals(new int[]{0,0,1,6,11},out.anIntArray1637);
        assertArrayEquals(new int[]{2,3},ints(out,"texturedFaceVertexA")); assertArrayEquals(new int[]{0,2},ints(out,"texturedFaceVertexB")); assertArrayEquals(new int[]{1,1},ints(out,"texturedFaceVertexC"));
        assertArrayEquals(new int[]{500,11,12,13,14},out.anIntArray1640); assertArrayEquals(new int[]{0,1,2,7},b.anIntArray1637);
    }
    @Test public void optionalMetadataAllocatesOnlyWhenNeededAndUsesPerPartDefaults() throws Exception {
        Model a=triangle(), b=triangle(); set(a,"uniformFacePriority",4); set(b,"uniformFacePriority",4);
        Model out=new Model(2,new Model[]{a,b}); assertNull(ints(out,"facePriorities")); assertEquals(4,field("uniformFacePriority").getInt(out));
        assertNull(out.anIntArray1637); assertNull(ints(out,"faceAlphas")); assertNull(ints(out,"faceGroupIds"));
        set(b,"uniformFacePriority",9); out=new Model(2,new Model[]{a,b}); assertArrayEquals(new int[]{4,9},ints(out,"facePriorities"));
        set(b,"facePriorities",new int[]{6}); set(b,"faceAlphas",new int[]{128}); set(b,"faceGroupIds",new int[]{17}); b.anIntArray1637=new int[]{1};
        out=new Model(2,new Model[]{a,b}); assertArrayEquals(new int[]{4,6},ints(out,"facePriorities"));
        assertArrayEquals(new int[]{0,128},ints(out,"faceAlphas")); assertArrayEquals(new int[]{0,17},ints(out,"faceGroupIds")); assertArrayEquals(new int[]{0,1},out.anIntArray1637);
        assertArrayEquals(new int[6],ints(out,"vertexGroupIds"));
    }
    @Test public void helperReturnsFirstActiveMatchOrAppendsWithSourceLabel() throws Exception {
        Model dst=part(new int[]{10,10,99,88},new int[]{1,1,2,3},new int[]{4,4,5,6},new int[0],new int[0],new int[0]); dst.anInt1626=2;
        set(dst,"vertexGroupIds",new int[]{7,8,91,92}); Model src=triangle(); set(src,"vertexGroupIds",new int[]{17,18,19});
        assertEquals(0,vertex(dst,src,0)); assertEquals(2,dst.anInt1626); assertEquals(7,ints(dst,"vertexGroupIds")[0]);
        assertEquals(2,vertex(dst,src,1)); assertEquals(3,dst.anInt1626); assertEquals(18,ints(dst,"vertexGroupIds")[2]);
        set(src,"vertexGroupIds",null); assertEquals(3,vertex(dst,src,2)); assertEquals(92,ints(dst,"vertexGroupIds")[3]);
        assertArrayEquals(new int[]{10,10,20,30},dst.anIntArray1627);
    }
    @Test public void noFacesDoNotImportUnreferencedVerticesAndSourceGroupsDoNotAffectEquality() throws Exception {
        Model a=triangle(); a.anInt1630=0; Model out=new Model(1,new Model[]{a}); assertEquals(0,out.anInt1626); assertEquals(3,out.anIntArray1627.length);
        Model b=triangle(); set(b,"vertexGroupIds",new int[]{255,128,127}); out=new Model(2,new Model[]{triangle(),b});
        assertEquals(3,out.anInt1626); assertArrayEquals(new int[6],ints(out,"vertexGroupIds"));
    }
    @Test public void helperRetainsExistingBoundsFailuresAndPartialWrites() throws Exception {
        Model src=triangle(),dst=part(new int[]{99},new int[0],new int[]{88},new int[0],new int[0],new int[0]); dst.anInt1626=0;
        try { vertex(dst,src,0); fail(); } catch(ArrayIndexOutOfBoundsException expected) { }
        assertEquals(10,dst.anIntArray1627[0]); assertEquals(88,dst.anIntArray1629[0]); assertEquals(0,dst.anInt1626);
        try { vertex(dst,src,-1); fail(); } catch(ArrayIndexOutOfBoundsException expected) { }
        try { vertex(dst,null,0); fail(); } catch(NullPointerException expected) { }
    }

    private static Model shadedTriangle(int shade) throws Exception {
        Model m=triangle();
        set(m,"faceShadeA",new int[]{shade}); set(m,"faceShadeB",new int[]{shade+1}); set(m,"faceShadeC",new int[]{shade+2});
        return m;
    }
    @Test public void shadedPairConcatenatesEveryVertexWithoutWeldingAndOffsetsCorners() throws Exception {
        Model a=shadedTriangle(100),b=shadedTriangle(200);
        a.anIntArray1627=new int[]{10,20,30,40}; a.anIntArray1628=new int[]{1,2,3,-12}; a.anIntArray1629=new int[]{4,5,6,0}; a.anInt1626=4;
        Model out=new Model(new Model[]{a,b});
        assertEquals(7,out.anInt1626); assertEquals(2,out.anInt1630);
        assertArrayEquals(new int[]{10,20,30,40,10,20,30},out.anIntArray1627);
        assertArrayEquals(new int[]{1,2,3,-12,1,2,3},out.anIntArray1628);
        assertArrayEquals(new int[]{4,5,6,0,4,5,6},out.anIntArray1629);
        assertArrayEquals(new int[]{0,4},out.anIntArray1631); assertArrayEquals(new int[]{1,5},out.anIntArray1632); assertArrayEquals(new int[]{2,6},out.anIntArray1633);
        assertArrayEquals(new int[]{100,200},ints(out,"faceShadeA")); assertArrayEquals(new int[]{101,201},ints(out,"faceShadeB")); assertArrayEquals(new int[]{102,202},ints(out,"faceShadeC"));
        assertEquals(12,out.modelHeight); assertEquals(3,out.maxY); assertEquals(40,out.radiusXZ); assertEquals(42,field("depthBucketOffset").getInt(out)); assertEquals(83,field("depthBucketCount").getInt(out));
        Model reverse=new Model(new Model[]{b,a}); assertArrayEquals(new int[]{10,20,30,10,20,30,40},reverse.anIntArray1627); assertArrayEquals(new int[]{0,3},reverse.anIntArray1631); assertArrayEquals(new int[]{200,100},ints(reverse,"faceShadeA"));
        out.anIntArray1627[0]=99; ints(out,"faceShadeA")[0]=999; out.anIntArray1631[0]=6;
        assertEquals(10,a.anIntArray1627[0]); assertEquals(100,ints(a,"faceShadeA")[0]); assertEquals(0,a.anIntArray1631[0]);
    }
    @Test public void shadedPairAlwaysSelectsTwoSlotsSkipsNullAndIgnoresTrailingParts() throws Exception {
        Model a=shadedTriangle(100);
        Model out=new Model(new Model[]{null,a,null}); assertEquals(3,out.anInt1626); assertArrayEquals(new int[]{100},ints(out,"faceShadeA"));
        out=new Model(new Model[]{a,null,a}); assertEquals(3,out.anInt1626); assertEquals(1,out.anInt1630);
        out=new Model(new Model[]{null,null,a}); assertEquals(0,out.anInt1626); assertEquals(0,out.radiusXZ); assertEquals(0,field("depthBucketCount").getInt(out));
        for(Model[] inputs:new Model[][]{new Model[0],new Model[]{a}}) { try { new Model(inputs); fail(); } catch(ArrayIndexOutOfBoundsException expected) { } }
        try { new Model((Model[])null); fail(); } catch(NullPointerException expected) { }
    }
    @Test public void shadedPairRebasesTextureTrianglesAndOnlyTexturedRenderFlags() throws Exception {
        Model a=shadedTriangle(100),b=shadedTriangle(200);
        set(a,"texturedFaceCount",1); set(a,"texturedFaceVertexA",new int[]{2}); set(a,"texturedFaceVertexB",new int[]{0}); set(a,"texturedFaceVertexC",new int[]{1});
        set(b,"texturedFaceCount",1); set(b,"texturedFaceVertexA",new int[]{1}); set(b,"texturedFaceVertexB",new int[]{2}); set(b,"texturedFaceVertexC",new int[]{0});
        for(int[] example:new int[][]{{0,0},{1,1},{2,6},{3,7},{7,11}}) {
            int flags=example[0];
            b.anIntArray1637=new int[]{flags}; Model out=new Model(new Model[]{a,b});
            assertArrayEquals(new int[]{0,example[1]},out.anIntArray1637);
            assertEquals(2,field("texturedFaceCount").getInt(out)); assertArrayEquals(new int[]{2,4},ints(out,"texturedFaceVertexA")); assertArrayEquals(new int[]{0,5},ints(out,"texturedFaceVertexB")); assertArrayEquals(new int[]{1,3},ints(out,"texturedFaceVertexC"));
            assertArrayEquals(new int[]{flags},b.anIntArray1637); assertArrayEquals(new int[]{1},ints(b,"texturedFaceVertexA"));
        }
    }
    @Test public void shadedPairOptionalBuffersDistinguishDefaultPriorityAlphaAndSourceFaceValues() throws Exception {
        Model a=shadedTriangle(100),b=shadedTriangle(200); a.anIntArray1640=null; b.anIntArray1640=null;
        set(a,"uniformFacePriority",4); set(b,"uniformFacePriority",4);
        Model out=new Model(new Model[]{a,b});
        assertNull(out.anIntArray1640); assertNull(out.anIntArray1637); assertNull(ints(out,"facePriorities")); assertNull(ints(out,"faceAlphas")); assertEquals(4,field("uniformFacePriority").getInt(out));
        b.anIntArray1640=new int[]{321}; b.anIntArray1637=new int[]{1}; set(b,"uniformFacePriority",9); set(b,"faceAlphas",new int[]{128});
        out=new Model(new Model[]{a,b}); assertArrayEquals(new int[]{0,321},out.anIntArray1640); assertArrayEquals(new int[]{0,1},out.anIntArray1637); assertArrayEquals(new int[]{4,9},ints(out,"facePriorities")); assertArrayEquals(new int[]{0,128},ints(out,"faceAlphas"));
        set(b,"facePriorities",new int[]{6}); out=new Model(new Model[]{b,a}); assertArrayEquals(new int[]{6,4},ints(out,"facePriorities")); assertArrayEquals(new int[]{128,0},ints(out,"faceAlphas")); assertArrayEquals(new int[]{321,0},out.anIntArray1640);
        out.anIntArray1640[0]=999; ints(out,"faceAlphas")[0]=1; ints(out,"facePriorities")[0]=0; out.anIntArray1637[0]=0;
        assertEquals(321,b.anIntArray1640[0]); assertEquals(128,ints(b,"faceAlphas")[0]); assertEquals(6,ints(b,"facePriorities")[0]); assertEquals(1,b.anIntArray1637[0]);
    }
    @Test public void shadedPairDropsAnimationGroupsAndDoesNotRequireSourceFaceValues() throws Exception {
        Model a=shadedTriangle(100); a.anIntArray1640=null;
        set(a,"vertexGroupIds",new int[]{1,2,3}); set(a,"faceGroupIds",new int[]{4}); a.vertexGroups=new int[][]{{0}}; a.faceGroups=new int[][]{{0}};
        Model out=new Model(new Model[]{a,a}); assertEquals(6,out.anInt1626); assertEquals(2,out.anInt1630);
        assertNull(ints(out,"vertexGroupIds")); assertNull(ints(out,"faceGroupIds")); assertNull(out.vertexGroups); assertNull(out.faceGroups); assertNull(out.anIntArray1640);
        assertArrayEquals(new int[]{100,100},ints(out,"faceShadeA"));
    }
    @Test public void shadedPairRequiresPrecomputedCornerShadesAndActiveVertexCoordinates() throws Exception {
        Model a=shadedTriangle(100); set(a,"faceShadeB",null);
        try { new Model(new Model[]{a,null}); fail(); } catch(NullPointerException expected) { }
        a=shadedTriangle(100); a.anIntArray1628=new int[2];
        try { new Model(new Model[]{a,null}); fail(); } catch(ArrayIndexOutOfBoundsException expected) { }
        a=shadedTriangle(100); a.anInt1626=0; a.anInt1630=0; set(a,"faceShadeA",null); set(a,"faceShadeB",null); set(a,"faceShadeC",null);
        Model out=new Model(new Model[]{a,null}); assertEquals(0,out.anIntArray1627.length); assertEquals(0,ints(out,"faceShadeA").length);
    }

    @Test public void shadedPairSeparatesVertexFaceAndTextureIndicesWithDistinctCounts() throws Exception {
        Model a=shadedTriangle(100),b=shadedTriangle(200);
        a.anInt1626=4; a.anIntArray1627=new int[]{10,20,30,40}; a.anIntArray1628=new int[]{1,2,3,4}; a.anIntArray1629=new int[]{4,5,6,7};
        a.anInt1630=2; a.anIntArray1631=new int[]{0,2}; a.anIntArray1632=new int[]{1,0}; a.anIntArray1633=new int[]{2,1};
        set(a,"faceShadeA",new int[]{100,110}); set(a,"faceShadeB",new int[]{101,111}); set(a,"faceShadeC",new int[]{102,112});
        a.anIntArray1640=new int[]{500,501}; a.anIntArray1637=new int[]{2,6}; set(a,"facePriorities",new int[]{4,9}); set(a,"faceAlphas",new int[]{0,128});
        set(a,"texturedFaceCount",3); set(a,"texturedFaceVertexA",new int[]{2,3,1}); set(a,"texturedFaceVertexB",new int[]{0,2,3}); set(a,"texturedFaceVertexC",new int[]{1,0,2});
        b.anIntArray1637=new int[]{2}; set(b,"uniformFacePriority",7);
        set(b,"texturedFaceCount",2); set(b,"texturedFaceVertexA",new int[]{1,2}); set(b,"texturedFaceVertexB",new int[]{2,0}); set(b,"texturedFaceVertexC",new int[]{0,1});
        Model out=new Model(new Model[]{a,b});
        assertEquals(7,out.anInt1626); assertEquals(3,out.anInt1630); assertEquals(5,field("texturedFaceCount").getInt(out));
        assertArrayEquals(new int[]{0,2,4},out.anIntArray1631); assertArrayEquals(new int[]{1,0,5},out.anIntArray1632); assertArrayEquals(new int[]{2,1,6},out.anIntArray1633);
        assertArrayEquals(new int[]{100,110,200},ints(out,"faceShadeA")); assertArrayEquals(new int[]{101,111,201},ints(out,"faceShadeB")); assertArrayEquals(new int[]{102,112,202},ints(out,"faceShadeC"));
        assertArrayEquals(new int[]{500,501,500},out.anIntArray1640); assertArrayEquals(new int[]{2,6,14},out.anIntArray1637); assertArrayEquals(new int[]{4,9,7},ints(out,"facePriorities")); assertArrayEquals(new int[]{0,128,0},ints(out,"faceAlphas"));
        assertArrayEquals(new int[]{2,3,1,5,6},ints(out,"texturedFaceVertexA")); assertArrayEquals(new int[]{0,2,3,6,4},ints(out,"texturedFaceVertexB")); assertArrayEquals(new int[]{1,0,2,4,5},ints(out,"texturedFaceVertexC"));
        assertArrayEquals(new int[]{0,2},a.anIntArray1631); assertArrayEquals(new int[]{2,3,1},ints(a,"texturedFaceVertexA")); assertArrayEquals(new int[]{2},b.anIntArray1637);
    }
}
