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
        set(a,"anInt1642",1); set(a,"anIntArray1643",new int[]{2}); set(a,"anIntArray1644",new int[]{0}); set(a,"anIntArray1645",new int[]{1});
        b.anIntArray1627=new int[]{10,20,30,40}; b.anIntArray1628=new int[]{1,2,3,4}; b.anIntArray1629=new int[]{4,5,6,7}; b.anInt1626=4;
        b.anInt1630=4; b.anIntArray1631=new int[]{0,0,0,0}; b.anIntArray1632=new int[]{1,1,1,1}; b.anIntArray1633=new int[]{2,2,2,2}; b.anIntArray1640=new int[]{11,12,13,14};
        b.anIntArray1637=new int[]{0,1,2,7}; set(b,"anInt1642",1); set(b,"anIntArray1643",new int[]{3}); set(b,"anIntArray1644",new int[]{2}); set(b,"anIntArray1645",new int[]{1});
        Model out=new Model(2,new Model[]{a,b});
        assertEquals(4,out.anInt1626); assertEquals(2,field("anInt1642").getInt(out));
        assertArrayEquals(new int[]{0,0,1,6,11},out.anIntArray1637);
        assertArrayEquals(new int[]{2,3},ints(out,"anIntArray1643")); assertArrayEquals(new int[]{0,2},ints(out,"anIntArray1644")); assertArrayEquals(new int[]{1,1},ints(out,"anIntArray1645"));
        assertArrayEquals(new int[]{500,11,12,13,14},out.anIntArray1640); assertArrayEquals(new int[]{0,1,2,7},b.anIntArray1637);
    }
    @Test public void optionalMetadataAllocatesOnlyWhenNeededAndUsesPerPartDefaults() throws Exception {
        Model a=triangle(), b=triangle(); set(a,"anInt1641",4); set(b,"anInt1641",4);
        Model out=new Model(2,new Model[]{a,b}); assertNull(ints(out,"anIntArray1638")); assertEquals(4,field("anInt1641").getInt(out));
        assertNull(out.anIntArray1637); assertNull(ints(out,"anIntArray1639")); assertNull(ints(out,"faceGroupIds"));
        set(b,"anInt1641",9); out=new Model(2,new Model[]{a,b}); assertArrayEquals(new int[]{4,9},ints(out,"anIntArray1638"));
        set(b,"anIntArray1638",new int[]{6}); set(b,"anIntArray1639",new int[]{128}); set(b,"faceGroupIds",new int[]{17}); b.anIntArray1637=new int[]{1};
        out=new Model(2,new Model[]{a,b}); assertArrayEquals(new int[]{4,6},ints(out,"anIntArray1638"));
        assertArrayEquals(new int[]{0,128},ints(out,"anIntArray1639")); assertArrayEquals(new int[]{0,17},ints(out,"faceGroupIds")); assertArrayEquals(new int[]{0,1},out.anIntArray1637);
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
}
