import java.io.*;
import java.lang.reflect.*;
import org.junit.*;
import static org.junit.Assert.*;

public class ModelAnimationGroupsTest {
    private Field vertexIds, faceIds, headers;
    private Object savedHeaders;
    private Object[] saved78;
    private static final String[] names78 = {"animationPivotX", "animationPivotY", "animationPivotZ"};

    @Before public void isolate78() throws Exception {
        saved78 = new Object[5];
        for (int a = 0; a < 3; a++) saved78[a] = field(names78[a]).get(null);
        saved78[3] = f78(AnimationFrame.class, "loadedFrames").get(null);
        saved78[4] = f78(AnimationFrame.class, "alphaTransformNotSeen").get(null);
    }
    @After public void restore78() throws Exception {
        for (int a = 0; a < 3; a++) field(names78[a]).set(null, saved78[a]);
        f78(AnimationFrame.class, "loadedFrames").set(null, saved78[3]);
        f78(AnimationFrame.class, "alphaTransformNotSeen").set(null, saved78[4]);
    }
    private static Field f78(Class<?> c, String n) throws Exception {
        Field f = c.getDeclaredField(n); f.setAccessible(true); return f;
    }
    private Model m78() throws Exception {
        Model m = model(null, null); m.anInt1626 = 3; m.anInt1630 = 3;
        m.anIntArray1627 = new int[]{10,21,-9,999};
        m.anIntArray1628 = new int[]{20,-5,12,998};
        m.anIntArray1629 = new int[]{30,7,-18,997};
        m.vertexGroups = new int[][]{{2,0},{},{1}};
        m.faceGroups = new int[][]{{1},{},{2,0}};
        field("anIntArray1639").set(m, new int[]{8,100,250,996});
        for (String n : names78) field(n).setInt(null, 0);
        return m;
    }
    private static void p78(int... a) throws Exception {
        for (int b = 0; b < 3; b++) assertEquals(names78[b], a[b], field(names78[b]).getInt(null));
    }
    private static void xyz78(Model m, int[] a, int[] b, int[] c) {
        assertArrayEquals(a,m.anIntArray1627); assertArrayEquals(b,m.anIntArray1628); assertArrayEquals(c,m.anIntArray1629);
    }
    private static AnimationTransformTable t78(int[] a, int[][] b) throws Exception {
        ByteArrayOutputStream o = new ByteArrayOutputStream(); o.write(a.length);
        for (int v : a) o.write(v);
        for (int[] g : b) {o.write(g.length); for (int v : g) o.write(v);}
        return new AnimationTransformTable(new Stream(o.toByteArray()));
    }
    private static AnimationFrame a78(AnimationTransformTable t, int[] a, int[] b, int[] c, int[] d, int n) throws Exception {
        Constructor<AnimationFrame> x = AnimationFrame.class.getDeclaredConstructor(); x.setAccessible(true);
        AnimationFrame f = x.newInstance(); f.transformTable=t; f.transformCount=n;
        f.transformIndices=a; f.transformX=b; f.transformY=c; f.transformZ=d; return f;
    }
    private static void put78(int a, AnimationFrame b) throws Exception {
        ((AnimationFrame[]) f78(AnimationFrame.class,"loadedFrames").get(null))[a]=b;
    }

    @Test public void application78T01() throws Exception {
        Model m=m78(); transform(m,0,new int[]{2,0,2,1,99},7,-11,13);
        // Four selected members, including vertex 1 twice; divide before adding.
        p78(17,-6,19);
        xyz78(m,new int[]{10,21,-9,999},new int[]{20,-5,12,998},new int[]{30,7,-18,997});
        transform(m,3,new int[]{0},256,64,-128);
        xyz78(m,new int[]{3,21,-35,999},new int[]{7,-5,3,998},new int[]{8,7,56,997});
        transform(m,0,new int[]{1,99},-7,8,-9); p78(-7,8,-9);
        transform(m,0,new int[0],1,2,3); p78(1,2,3);
    }
    @Test public void application78T02() throws Exception {
        Model m=m78(); transform(m,1,new int[]{1,2,99,0,2},7,-3,11);
        xyz78(m,new int[]{17,35,-2,999},new int[]{17,-11,9,998},new int[]{41,29,-7,997});
        p78(0,0,0); assertArrayEquals(new int[]{8,100,250,996},(int[])field("anIntArray1639").get(m));
        transform(m,4,new int[]{-1},1,2,3); // Unsupported type does not dereference members.
        transform(m,1,new int[]{1,99},100,100,100);
        assertEquals(35,m.anIntArray1627[1]);
        try {transform(m,1,new int[]{2,-1},1,0,0); fail();}
        catch (InvocationTargetException ex) {assertTrue(ex.getCause() instanceof ArrayIndexOutOfBoundsException);}
        assertEquals(36,m.anIntArray1627[1]);
    }
    @Test public void application78T04() throws Exception {
        // Independent externally specified fixed-point expectations for 45 degrees.
        int[][] b={{21,-9,1},{19,-5,-10},{11,-19,7}};
        for(int n=0;n<3;n++) {
            Model m=m78(); int[] a=new int[3]; a[n]=32;
            transform(m,2,new int[]{2},a[0],a[1],a[2]);
            assertEquals(b[n][0],m.anIntArray1627[1]); assertEquals(b[n][1],m.anIntArray1628[1]); assertEquals(b[n][2],m.anIntArray1629[1]);
        }
        for(int n : new int[]{0,1,31,32,63,64,127,128,192,255}) {
            Model a=m78(),b0=m78(),c=m78(); transform(a,2,new int[]{0,2},n,n,n);
            transform(b0,2,new int[]{0,2},n+256,n-256,n+512);
            transform(c,2,new int[]{0,2},n-512,n+512,n-256);
            xyz78(b0,a.anIntArray1627,a.anIntArray1628,a.anIntArray1629);
            xyz78(c,a.anIntArray1627,a.anIntArray1628,a.anIntArray1629);
        }
    }
    @Test public void application78T05() throws Exception {
        Model m=m78(); transform(m,0,new int[0],3,-4,5);
        transform(m,3,new int[]{1,2,99,0},64,256,-128);
        xyz78(m,new int[]{6,12,-3,999},new int[]{44,-6,28,998},new int[]{-20,3,28,997}); p78(3,-4,5);
        m=m78(); transform(m,3,new int[]{2},0,128,128);
        xyz78(m,new int[]{10,0,-9,999},new int[]{20,-5,12,998},new int[]{30,7,-18,997});
        m=m78(); transform(m,3,new int[]{2},127,-127,129);
        assertEquals(20,m.anIntArray1627[1]); assertEquals(4,m.anIntArray1628[1]); assertEquals(7,m.anIntArray1629[1]);
        m=m78(); m.anIntArray1627[1]=Integer.MAX_VALUE; transform(m,3,new int[]{2},2,128,128); assertEquals(0,m.anIntArray1627[1]);
    }
    @Test public void application78T06() throws Exception {
        Model m=m78(); transform(m,5,new int[]{1,2,99,0,2},2,999,-999);
        assertArrayEquals(new int[]{40,116,255,996},(int[])field("anIntArray1639").get(m));
        transform(m,5,new int[]{2},-40,1,1); assertArrayEquals(new int[]{0,116,0,996},(int[])field("anIntArray1639").get(m));
        xyz78(m,new int[]{10,21,-9,999},new int[]{20,-5,12,998},new int[]{30,7,-18,997}); p78(0,0,0);
        m.faceGroups=null; transform(m,5,new int[]{-1},2,0,0);
        m.faceGroups=new int[][]{{0}}; field("anIntArray1639").set(m,null); transform(m,5,new int[]{-1},2,0,0);
        m=m78(); transform(m,5,new int[]{0},Integer.MAX_VALUE,0,0); assertEquals(92,((int[])field("anIntArray1639").get(m))[1]);
    }
    @Test public void application78T07() throws Exception {
        AnimationFrame.initialize(13); Model m=m78();
        AnimationTransformTable t=t78(new int[]{3,1,5,0},new int[][]{{2},{0},{0},{}});
        put78(11,a78(t,new int[]{3,0,1,2,99},new int[]{3,256,7,2,999},new int[]{-4,64,-3,99,999},new int[]{5,128,11,-99,999},4));
        m.applyAnimationFrame(11);
        xyz78(m,new int[]{17,39,-2,999},new int[]{17,-4,9,998},new int[]{41,7,-7,997});
        assertArrayEquals(new int[]{8,116,250,996},(int[])field("anIntArray1639").get(m)); p78(3,-4,5);
    }
    @Test public void application78T08() throws Exception {
        AnimationFrame.initialize(4); Model m=m78(); transform(m,0,new int[0],1,2,3);
        m.applyAnimationFrame(-1); p78(1,2,3); m.applyAnimationFrame(4); p78(1,2,3);
        m.vertexGroups=null; m.applyAnimationFrame(Integer.MAX_VALUE); p78(1,2,3);
        m=m78(); transform(m,0,new int[0],1,2,3);
        AnimationTransformTable t=t78(new int[]{3},new int[][]{{2}});
        put78(3,a78(t,new int[]{0},new int[]{256},new int[]{256},new int[]{256},1)); m.applyAnimationFrame(3);
        xyz78(m,new int[]{10,42,-9,999},new int[]{20,-10,12,998},new int[]{30,14,-18,997}); p78(0,0,0);
        try {m.applyAnimationFrame(-2); fail();} catch(ArrayIndexOutOfBoundsException expected) { }
        put78(2,a78(t,new int[0],new int[0],new int[0],new int[0],0)); transform(m,0,new int[0],9,8,7); m.applyAnimationFrame(2); p78(0,0,0);
    }
    @Test public void application78T10() throws Exception {
        AnimationFrame.initialize(9); AnimationTransformTable t=t78(new int[]{0,3},new int[][]{{},{2}});
        put78(8,a78(t,new int[]{0},new int[]{100},new int[]{200},new int[]{300},1));
        put78(5,a78(t,new int[]{1},new int[]{256},new int[]{256},new int[]{256},1));
        Model m=m78(); m.applyInterleavedFrames(new int[]{1,1000},5,8);
        xyz78(m,new int[]{10,42,-9,999},new int[]{20,-10,12,998},new int[]{30,14,-18,997}); p78(0,0,0);
        // Repeated sentinels are not needed; skipped selection entries advance the cursor.
        AnimationTransformTable u=t78(new int[]{4,4,4,1,4,1},new int[][]{{},{},{},{2},{},{0}});
        put78(8,a78(u,new int[]{3,5},new int[]{7,11},new int[]{0,0},new int[]{0,0},2));
        put78(5,a78(u,new int[]{3,5},new int[]{70,110},new int[]{0,0},new int[]{0,0},2));
        m=m78(); m.applyInterleavedFrames(new int[]{0,1,3,4,1000},5,8); assertArrayEquals(new int[]{21,91,2,999},m.anIntArray1627);
    }
    @Test public void application78T11() throws Exception {
        AnimationFrame.initialize(9); AnimationTransformTable t=t78(new int[]{1},new int[][]{{2}});
        put78(8,a78(t,new int[]{0},new int[]{7},new int[]{-3},new int[]{11},1));
        for(int n=0;n<3;n++) {
            Model m=m78(); int[] a=n==0?null:new int[]{0,1000}; int b=n==1?-1:4;
            m.applyInterleavedFrames(a,b,8); xyz78(m,new int[]{10,28,-9,999},new int[]{20,-8,12,998},new int[]{30,18,-18,997});
        }
        Model m=m78(); transform(m,0,new int[0],1,2,3); m.applyInterleavedFrames(new int[0],8,-1); p78(1,2,3);
        m.applyInterleavedFrames(new int[0],8,4); p78(1,2,3);
        m.vertexGroups=null; m.applyInterleavedFrames(null,8,8); p78(1,2,3);
        m=m78(); put78(5,a78(t,new int[0],new int[0],new int[0],new int[0],0));
        try {m.applyInterleavedFrames(new int[0],5,8); fail();} catch(ArrayIndexOutOfBoundsException expected) { }
        try {m.applyInterleavedFrames(new int[]{0},5,8); } catch(ArrayIndexOutOfBoundsException unexpected) {fail();}
    }
    @Test public void application78T13() throws Exception {
        int[] a=Model.sineTable16,b=Model.cosineTable16;
        try {
            Model.sineTable16=new int[2048]; Model.cosineTable16=new int[2048];
            Model.sineTable16[512]=65536; Model.cosineTable16[512]=0;
            int[][] c={{64,0,0},{0,64,0},{0,0,64},{64,64,64}};
            int[][] d={{21,-7,-5},{7,-5,-21},{-5,-21,7},{-21,-7,5}};
            for(int n=0;n<c.length;n++) {
                Model m=m78(); transform(m,2,new int[]{1,99,2},c[n][0],c[n][1],c[n][2]);
                xyz78(m,new int[]{10,d[n][0],-9,999},new int[]{20,d[n][1],12,998},new int[]{30,d[n][2],-18,997}); p78(0,0,0);
            }
            Model m=m78(); transform(m,0,new int[0],3,4,5); transform(m,2,new int[]{2},0,0,64);
            xyz78(m,new int[]{10,-6,-9,999},new int[]{20,-14,12,998},new int[]{30,7,-18,997}); p78(3,4,5);
            // Distinct coefficients distinguish the two lookup tables and 16-bit shifts.
            Model.sineTable16[512]=32768; Model.cosineTable16[512]=16384;
            int[][] v={{21,-5,-1},{8,-5,-9},{2,-12,7}};
            for(int n=0;n<3;n++) {
                m=m78(); int[] q=new int[3];q[n]=64;transform(m,2,new int[]{2},q[0],q[1],q[2]);
                assertEquals(v[n][0],m.anIntArray1627[1]);assertEquals(v[n][1],m.anIntArray1628[1]);assertEquals(v[n][2],m.anIntArray1629[1]);
            }
        } finally {Model.sineTable16=a;Model.cosineTable16=b;}
    }
    @Test public void application78T14() throws Exception {
        AnimationFrame.initialize(12);
        AnimationTransformTable t=t78(new int[]{0,1,1,1,5,0},new int[][]{{},{0},{2},{2},{0},{}});
        put78(11,a78(t,new int[]{0,1,2,3,4,5,99},new int[]{3,7,8,9,1,10,999},new int[]{4,0,0,0,0,20,999},new int[]{5,0,0,0,0,30,999},6));
        AnimationTransformTable u=t78(new int[]{4,4,4,4,4,4},new int[][]{{},{},{},{},{},{}});
        put78(7,a78(u,new int[]{0,1,2,3,4,5,99},new int[]{30,70,80,90,2,100,999},new int[]{40,0,0,0,0,200,999},new int[]{50,0,0,0,0,300,999},6));
        Model m=m78();m.applyInterleavedFrames(new int[]{0,2,4,5,1000},7,11);
        // Original 21 plus primary row 3 (9), secondary row 2 (80).
        xyz78(m,new int[]{17,110,-2,999},new int[]{20,-5,12,998},new int[]{30,7,-18,997});
        assertArrayEquals(new int[]{8,116,250,996},(int[])field("anIntArray1639").get(m));p78(100,200,300);
        m=m78();m.applyInterleavedFrames(new int[]{1000},7,11);assertArrayEquals(new int[]{17,38,-2,999},m.anIntArray1627);p78(100,200,300);
        m=m78();m.applyInterleavedFrames(new int[]{0,1,2,3,4,5,1000},7,11);assertArrayEquals(new int[]{80,191,61,999},m.anIntArray1627);p78(100,200,300);
    }
    @Test public void application78T15() throws Exception {
        Model m=m78();transform(m,0,new int[]{0},0,0,0);p78(0,16,6);
        Model n=m78();transform(m,0,new int[0],3,4,5);transform(n,3,new int[]{2,2},256,128,128);
        assertEquals(75,n.anIntArray1627[1]);p78(3,4,5);
        Model q=m78();q.anIntArray1627[1]=75;transform(m,0,new int[0],3,4,5);
        transform(n,2,new int[]{2,2},32,17,9);transform(q,2,new int[]{2},32,17,9);transform(q,2,new int[]{2},32,17,9);
        xyz78(n,q.anIntArray1627,q.anIntArray1628,q.anIntArray1629);
        m=m78();m.vertexGroups=null;transform(m,5,new int[]{0},1,0,0);assertEquals(108,((int[])field("anIntArray1639").get(m))[1]);
        try {transform(m,0,null,0,0,0);fail();}catch(InvocationTargetException ex){assertTrue(ex.getCause() instanceof NullPointerException);}
        transform(m,0,new int[0],1,2,3);p78(1,2,3);
    }

    @Before public void isolate() throws Exception {
        vertexIds = field("vertexGroupIds"); faceIds = field("faceGroupIds");
        headers = field("modelHeaders"); savedHeaders = headers.get(null);
    }
    @After public void restore() throws Exception { headers.set(null, savedHeaders); }
    private static Field field(String name) throws Exception {
        Field f = Model.class.getDeclaredField(name); f.setAccessible(true); return f;
    }
    private Model model(int[] vertices, int[] faces) throws Exception {
        Constructor<Model> c = Model.class.getDeclaredConstructor(); c.setAccessible(true);
        Model m = c.newInstance(); m.anInt1626 = vertices == null ? 0 : vertices.length;
        m.anInt1630 = faces == null ? 0 : faces.length;
        vertexIds.set(m, vertices); faceIds.set(m, faces); return m;
    }
    private static void groups(int[][] actual, int[]... expected) {
        assertEquals(expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) assertArrayEquals("group " + i, expected[i], actual[i]);
    }
    private static void transform(Model m, int type, int[] ids, int x, int y, int z) throws Exception {
        Method t = Model.class.getDeclaredMethod("transformAnimationGroups", int.class, int[].class, int.class, int.class, int.class);
        t.setAccessible(true); t.invoke(m, type, ids, x, y, z);
    }

    @Test public void sparseRepeatedIdsProduceStableMemberIndicesInSeparateDomains() throws Exception {
        int[] vertices = {3, 0, 3, 1, 0}, faces = {1, 3, 1};
        Model m = model(vertices, faces); m.buildAnimationGroups();
        groups(m.vertexGroups, new int[]{1,4}, new int[]{3}, new int[0], new int[]{0,2});
        groups(m.faceGroups, new int[0], new int[]{0,2}, new int[0], new int[]{1});
        assertNull(vertexIds.get(m)); assertNull(faceIds.get(m));
        assertArrayEquals(new int[]{3,0,3,1,0}, vertices); assertArrayEquals(new int[]{1,3,1}, faces);
    }
    @Test public void allByteIdsRemainUnsignedGroupIndicesRatherThanMemberIndices() throws Exception {
        int[] ids = {255, 128, 0, 127, 255}; Model m = model(ids, ids.clone()); m.buildAnimationGroups();
        for (int[][] table : new int[][][]{m.vertexGroups, m.faceGroups}) {
            assertEquals(256, table.length); assertArrayEquals(new int[]{0,4}, table[255]);
            assertArrayEquals(new int[]{1}, table[128]); assertArrayEquals(new int[]{2}, table[0]);
            assertArrayEquals(new int[]{3}, table[127]); assertArrayEquals(new int[0], table[254]);
        }
    }
    @Test public void nullInputsPreserveExistingGroupsAndEmptyInputsCreateOneEmptyGroup() throws Exception {
        Model m = model(null, null); int[][] v = {{8}}, f = {{9}};
        m.vertexGroups = v; m.faceGroups = f; m.buildAnimationGroups();
        assertSame(v, m.vertexGroups); assertSame(f, m.faceGroups);
        vertexIds.set(m, new int[0]); faceIds.set(m, new int[0]); m.buildAnimationGroups();
        groups(m.vertexGroups, new int[0]); groups(m.faceGroups, new int[0]);
        assertNotSame(v, m.vertexGroups); assertNotSame(f, m.faceGroups);
    }
    @Test public void vertexAndFaceBranchesRunIndependentlyAndRepeatIsIdentityNoOp() throws Exception {
        Model m = model(new int[]{2,2}, null); int[][] oldFaces = {{7}}; m.faceGroups = oldFaces;
        m.buildAnimationGroups(); assertSame(oldFaces, m.faceGroups);
        int[][] v = m.vertexGroups; faceIds.set(m, new int[]{0}); m.anInt1630 = 1; m.buildAnimationGroups();
        assertSame(v, m.vertexGroups); groups(m.faceGroups, new int[]{0});
        int[][] f = m.faceGroups; m.buildAnimationGroups();
        assertSame(v, m.vertexGroups); assertSame(f, m.faceGroups);
    }
    @Test public void activeCountsExcludeUnusedArrayTails() throws Exception {
        Model m = model(new int[]{2,0,255}, new int[]{1,256}); m.anInt1626 = 2; m.anInt1630 = 1;
        m.buildAnimationGroups(); groups(m.vertexGroups, new int[]{1}, new int[0], new int[]{0});
        groups(m.faceGroups, new int[0], new int[]{0});
    }
    @Test public void invalidIdsAndShortArraysKeepExistingFailureAndPartialState() throws Exception {
        for (int bad : new int[]{-1,256}) {
            Model m = model(new int[]{bad}, new int[]{0}); int[][] v = {{4}}; m.vertexGroups = v;
            try { m.buildAnimationGroups(); fail(); } catch (ArrayIndexOutOfBoundsException expected) { }
            assertSame(v, m.vertexGroups); assertNotNull(vertexIds.get(m)); assertNotNull(faceIds.get(m));
            m = model(new int[]{0}, new int[]{bad});
            try { m.buildAnimationGroups(); fail(); } catch (ArrayIndexOutOfBoundsException expected) { }
            assertNull(vertexIds.get(m)); groups(m.vertexGroups, new int[]{0}); assertNotNull(faceIds.get(m));
        }
        Model m = model(new int[]{0}, null); m.anInt1626 = 2;
        try { m.buildAnimationGroups(); fail(); } catch (ArrayIndexOutOfBoundsException expected) { }
        assertNotNull(vertexIds.get(m)); assertNull(m.vertexGroups);
    }
    @Test public void vertexGroupsSelectTranslationTargetsWithoutChangingOtherVerticesOrFaces() throws Exception {
        Model m = model(new int[]{2,0,2}, new int[]{2,0});
        m.anIntArray1627 = new int[]{10,20,30}; m.anIntArray1628 = new int[]{1,2,3};
        m.anIntArray1629 = new int[]{4,5,6}; field("anIntArray1639").set(m, new int[]{40,50}); m.buildAnimationGroups();
        transform(m, 1, new int[]{2}, 7, -3, 11);
        assertArrayEquals(new int[]{17,20,37}, m.anIntArray1627);
        assertArrayEquals(new int[]{-2,2,0}, m.anIntArray1628);
        assertArrayEquals(new int[]{15,5,17}, m.anIntArray1629); assertArrayEquals(new int[]{40,50}, (int[])field("anIntArray1639").get(m));
        transform(m, 1, new int[]{1,255}, 99,99,99); assertArrayEquals(new int[]{17,20,37}, m.anIntArray1627);
    }
    @Test public void faceGroupsSelectAlphaTargetsIndependentlyAndPreserveClampBehavior() throws Exception {
        Model m = model(new int[]{0,2}, new int[]{2,0,2}); m.anIntArray1627 = new int[]{10,20};
        field("anIntArray1639").set(m, new int[]{8,100,250}); m.buildAnimationGroups();
        transform(m, 5, new int[]{2}, 2,999,999); assertArrayEquals(new int[]{24,100,255}, (int[])field("anIntArray1639").get(m));
        transform(m, 5, new int[]{0}, -20,0,0); assertArrayEquals(new int[]{24,0,255}, (int[])field("anIntArray1639").get(m));
        assertArrayEquals(new int[]{10,20}, m.anIntArray1627);
    }
    @Test public void binaryModelLabelsDecodeAsIndependentUnsignedBytes() throws Exception {
        // Five coincident vertices and five degenerate faces; no coordinate/index deltas.
        int[] v = {255,128,0,127,255}, f = {0,255,128,127,0};
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(); DataOutputStream out = new DataOutputStream(bytes);
        for (int i = 0; i < 10; i++) out.writeByte(0); // vertex flags, face index modes
        for (int id : f) out.writeByte(id); for (int id : v) out.writeByte(id);
        for (int i = 0; i < 5; i++) out.writeShort(123); // face colors
        out.writeShort(5); out.writeShort(5); out.writeByte(0); out.writeByte(0); out.writeByte(0);
        out.writeByte(0); out.writeByte(1); out.writeByte(1); // no alpha, face labels, vertex labels
        for (int i = 0; i < 4; i++) out.writeShort(0);
        headers.set(null, new ModelHeader[1]); Model.method460(bytes.toByteArray(), 0); Model m = Model.getModel(0);
        assertArrayEquals(v, (int[])vertexIds.get(m)); assertArrayEquals(f, (int[])faceIds.get(m));
        m.buildAnimationGroups(); assertArrayEquals(new int[]{0,4}, m.vertexGroups[255]);
        assertArrayEquals(new int[]{1}, m.faceGroups[255]);
        assertArrayEquals(new int[]{0,4}, m.faceGroups[0]);
    }
}
