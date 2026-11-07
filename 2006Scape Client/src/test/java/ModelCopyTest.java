import java.lang.reflect.*;
import java.util.Arrays;
import org.junit.Test;
import static org.junit.Assert.*;

public class ModelCopyTest {
    private static Field field(String n) throws Exception { Field f=Model.class.getDeclaredField(n); f.setAccessible(true); return f; }
    private static Object get(Model m,String n) throws Exception { return field(n).get(m); }
    private static void set(Model m,String n,Object v) throws Exception { field(n).set(m,v); }
    private static int[] ints(Model m,String n) throws Exception { return (int[])get(m,n); }
    private static Model empty() throws Exception { Constructor<Model> c=Model.class.getDeclaredConstructor(); c.setAccessible(true); return c.newInstance(); }
    private static final String[] A={"anIntArray1627","anIntArray1628","anIntArray1629","anIntArray1631","anIntArray1632","anIntArray1633","anIntArray1634","anIntArray1635","anIntArray1636","anIntArray1637","anIntArray1638","anIntArray1639","anIntArray1640","anIntArray1643","anIntArray1644","anIntArray1645","vertexGroupIds","faceGroupIds"};
    private static final String[] B={"modelHeight","maxY","radiusXZ","depthBucketOffset","depthBucketCount","minX","maxX","minZ","maxZ"};
    private static final String[] S={"animationScratchX","animationScratchY","animationScratchZ","animationScratchAlpha"};
    private static Model fixture() throws Exception {
        Model m=empty(); m.anInt1626=3; m.anInt1630=2; set(m,"anInt1642",1); set(m,"anInt1641",7);
        for(int i=0;i<A.length;i++) { int count=i<3||i==16?4:i>=13&&i<=15?2:3; int[] a=new int[count]; for(int j=0;j<count;j++)a[j]=100*i+10+j; set(m,A[i],a); }
        m.vertexGroups=new int[][]{{0,2},{1}}; m.faceGroups=new int[][]{{1},{0}};
        m.aClass33Array1425=new Class33[4]; m.aClass33Array1660=new Class33[4];
        for(int i=0;i<4;i++) { Class33 a=new Class33(); a.anInt602=11+i; a.anInt603=21+i; a.anInt604=-31-i; a.anInt605=41+i; m.aClass33Array1425[i]=a; m.aClass33Array1660[i]=new Class33(); }
        for(int i=0;i<B.length;i++) { if(i==0)m.modelHeight=70; else set(m,B[i],70+i); }
        m.aBoolean1659=true; return m;
    }
    private static void equalPrefix(int[] a,int[] b,int count) { assertArrayEquals(Arrays.copyOf(a,count),Arrays.copyOf(b,count)); }
    private static void counts(Model a,Model b) throws Exception { assertEquals(a.anInt1626,b.anInt1626);assertEquals(a.anInt1630,b.anInt1630);assertEquals(get(a,"anInt1642"),get(b,"anInt1642"));assertEquals(get(a,"anInt1641"),get(b,"anInt1641")); }
    @Test public void threeBooleansIndependentlySelectAliasingAcrossAllCombinations() throws Exception {
        for(int mask=0;mask<8;mask++) {
            Model a=fixture(), b=new Model((mask&1)!=0,(mask&2)!=0,(mask&4)!=0,a); counts(a,b); assertFalse(b.aBoolean1659);
            for(int j=0;j<3;j++) { assertEquals((mask&4)!=0,ints(a,A[j])==ints(b,A[j])); equalPrefix(ints(a,A[j]),ints(b,A[j]),3); if((mask&4)==0)assertEquals(3,ints(b,A[j]).length); }
            for(int j:new int[]{11,12}) { int bit=j==11?2:1; assertEquals((mask&bit)!=0,ints(a,A[j])==ints(b,A[j]));equalPrefix(ints(a,A[j]),ints(b,A[j]),2); if((mask&bit)==0)assertEquals(2,ints(b,A[j]).length); }
            for(int j:new int[]{3,4,5,9,10,13,14,15,16,17})assertSame(ints(a,A[j]),ints(b,A[j]));
            for(int j=6;j<9;j++)assertNull(get(b,A[j])); assertNull(b.vertexGroups);assertNull(b.faceGroups);assertNull(b.aClass33Array1425);assertNull(b.aClass33Array1660);assertEquals(1000,b.modelHeight);
            b.anIntArray1627[2]=999; assertEquals((mask&4)!=0?999:12,a.anIntArray1627[2]);
            b.anIntArray1640[1]=888;assertEquals((mask&1)!=0?888:1211,a.anIntArray1640[1]);
            ints(b,A[11])[0]=777;assertEquals((mask&2)!=0?777:1110,ints(a,A[11])[0]);
        }
    }
    @Test public void threeBooleanNullAlphaDistinguishesSharingFromTwoZeroFaces() throws Exception {
        Model a=fixture();set(a,A[11],null);
        assertNull(get(new Model(true,true,true,a),A[11]));Model b=new Model(true,false,true,a);
        assertArrayEquals(new int[]{0,0},ints(b,A[11]));assertEquals(4,a.anIntArray1627.length);
        a.anIntArray1640=null;assertNull(new Model(true,true,true,a).anIntArray1640);
        try {new Model(false,true,true,a);fail();}catch(NullPointerException expected) { }
    }
    @Test public void twoBooleansIndependentlyCopyYAndLightingButShareOtherArrays() throws Exception {
        for(int mask=0;mask<4;mask++) {
            Model a=fixture(),b=new Model((mask&1)!=0,(mask&2)!=0,a);counts(a,b);
            for(int j=0;j<A.length;j++) { if(j==16||j==17) {assertNull(get(b,A[j]));continue;} boolean copy=j==1&&(mask&1)!=0||j>=6&&j<=9&&(mask&2)!=0;
                assertEquals(!copy,ints(a,A[j])==ints(b,A[j]));equalPrefix(ints(a,A[j]),ints(b,A[j]),j<3?3:j>=13&&j<=15?1:2);
                if(copy)assertEquals(j==1?3:2,ints(b,A[j]).length);
            }
            for(int j=0;j<B.length;j++)assertEquals(j==0?a.modelHeight:get(a,B[j]),j==0?b.modelHeight:get(b,B[j]));
            assertNull(b.vertexGroups);assertNull(b.faceGroups);assertFalse(b.aBoolean1659);
            if((mask&2)==0) { assertNull(b.aClass33Array1425);assertNull(b.aClass33Array1660); }
            else { assertNotSame(a.aClass33Array1425,b.aClass33Array1425);assertEquals(3,b.aClass33Array1425.length);assertSame(a.aClass33Array1660,b.aClass33Array1660);
                for(int j=0;j<3;j++) { Class33 x=a.aClass33Array1425[j],y=b.aClass33Array1425[j];assertNotSame(x,y);assertEquals(11+j,y.anInt602);assertEquals(21+j,y.anInt603);assertEquals(-31-j,y.anInt604);assertEquals(41+j,y.anInt605);y.anInt602=999;assertEquals(11+j,x.anInt602); }
            }
            b.anIntArray1628[2]=900;assertEquals((mask&1)!=0?112:900,a.anIntArray1628[2]);
            ints(b,A[7])[1]=901;assertEquals((mask&2)!=0?711:901,ints(a,A[7])[1]);
        }
    }
    @Test public void lightingCopyZerosMissingFlagsAndDoesNotReadNormalsWhenDisabled() throws Exception {
        Model a=fixture();a.anIntArray1637=null;Model b=new Model(false,true,a);assertArrayEquals(new int[]{0,0},b.anIntArray1637);
        a.aClass33Array1425=null;assertNull(new Model(false,false,a).aClass33Array1425);
        try {new Model(false,true,a);fail();}catch(NullPointerException expected) { }
    }
    @Test public void copiedVertexRecordsFollowFaceWindingAndLeaveUnusedVertexZero() throws Exception {
        for(boolean reverse:new boolean[]{false,true}) {
            Model a=empty();a.anInt1626=4;a.anInt1630=1;a.anIntArray1627=new int[]{0,10,0,99};a.anIntArray1628=new int[]{0,0,10,99};a.anIntArray1629=new int[]{0,0,0,99};
            a.anIntArray1631=new int[]{0};a.anIntArray1632=new int[]{reverse?2:1};a.anIntArray1633=new int[]{reverse?1:2};a.anIntArray1640=new int[]{500};
            a.applyLighting(64,850,-30,-50,-30,false);Model b=new Model(false,true,a);
            for(int j=0;j<4;j++) { Class33 x=b.aClass33Array1425[j];assertEquals(0,x.anInt602);assertEquals(0,x.anInt603);assertEquals(j==3?0:reverse?-256:256,x.anInt604);assertEquals(j==3?0:1,x.anInt605);assertNotSame(a.aClass33Array1425[j],x); }
        }
    }
    @Test public void scratchCopiesActivePrefixesSharesMetadataAndReusesAcrossDestinations() throws Exception {
        Object[] old=new Object[4];for(int i=0;i<4;i++) {old[i]=field(S[i]).get(null);field(S[i]).set(null,new int[]{-1,-2,-3,-4,-5});}
        try {
            Model a=fixture(),b=empty();b.copyIntoAnimationScratch(a,false);counts(a,b);
            for(int i=0;i<3;i++) {assertSame(field(S[i]).get(null),ints(b,A[i]));assertNotSame(ints(a,A[i]),ints(b,A[i]));assertArrayEquals(new int[]{100*i+10,100*i+11,100*i+12,-4,-5},ints(b,A[i]));}
            assertSame(field(S[3]).get(null),ints(b,A[11]));assertArrayEquals(new int[]{1110,1111,-3,-4,-5},ints(b,A[11]));
            for(int i=3;i<16;i++)if(i!=11)assertSame(ints(a,A[i]),ints(b,A[i]));assertSame(a.vertexGroups,b.vertexGroups);assertSame(a.faceGroups,b.faceGroups);
            assertNull(get(b,A[16]));assertNull(get(b,A[17]));assertNull(b.aClass33Array1425);assertEquals(1000,b.modelHeight);
            Model c=fixture();c.anIntArray1627[0]=888;Model d=empty();d.copyIntoAnimationScratch(c,true);
            assertSame(b.anIntArray1627,d.anIntArray1627);assertEquals(888,b.anIntArray1627[0]);assertEquals(10,a.anIntArray1627[0]);assertSame(ints(c,A[11]),ints(d,A[11]));
            set(c,A[11],null);d.copyIntoAnimationScratch(c,false);assertArrayEquals(new int[]{0,0,-3,-4,-5},ints(d,A[11]));d.copyIntoAnimationScratch(c,true);assertNull(get(d,A[11]));
        } finally {for(int i=0;i<4;i++)field(S[i]).set(null,old[i]);}
    }
    @Test public void scratchGrowthUsesVertexAndFaceCountsSeparatelyAndRetainsCapacityAtBoundary() throws Exception {
        Object[] old=new Object[4];for(int i=0;i<4;i++) {old[i]=field(S[i]).get(null);field(S[i]).set(null,new int[2]);}
        try {
            Model a=fixture(),b=empty();b.copyIntoAnimationScratch(a,false);
            for(int i=0;i<3;i++)assertEquals(103,ints(b,A[i]).length);assertEquals(2,ints(b,A[11]).length);
            Object[] retained=new Object[4];for(int i=0;i<4;i++)retained[i]=field(S[i]).get(null);
            a.anInt1626=2;a.anInt1630=3;b.copyIntoAnimationScratch(a,false);
            for(int i=0;i<3;i++)assertSame(retained[i],field(S[i]).get(null));assertNotSame(retained[3],field(S[3]).get(null));assertEquals(103,ints(b,A[11]).length);
            Model.animationScratchModel.copyIntoAnimationScratch(a,true);assertSame(field(S[0]).get(null),Model.animationScratchModel.anIntArray1627);
        } finally {for(int i=0;i<4;i++)field(S[i]).set(null,old[i]);}
    }
    @Test public void emptyCopiesDoNotReadMissingEntriesButPositiveCountsKeepFailures() throws Exception {
        Model a=empty();a.anIntArray1640=new int[0];a.anIntArray1627=new int[0];a.anIntArray1628=new int[0];a.anIntArray1629=new int[0];
        Model b=new Model(false,false,false,a);assertEquals(0,b.anIntArray1627.length);assertEquals(0,ints(b,A[11]).length);
        b=new Model(true,true,a);assertEquals(0,b.aClass33Array1425.length);assertEquals(0,ints(b,A[6]).length);
        a.anInt1626=1;try {new Model(false,true,false,a);fail();}catch(ArrayIndexOutOfBoundsException expected) { }
        try {new Model(true,false,a);fail();}catch(ArrayIndexOutOfBoundsException expected) { }
        try {new Model(true,true,true,null);fail();}catch(NullPointerException expected) { }
        try {new Model(false,false,(Model)null);fail();}catch(NullPointerException expected) { }
    }
}
