import java.io.*;
import java.lang.reflect.*;
import org.junit.*;
import static org.junit.Assert.*;

public class ModelAnimationGroupsTest {
    private Field vertexIds, faceIds, headers;
    private Object savedHeaders;

    @Before public void isolate() throws Exception {
        vertexIds = field("vertexGroupIds"); faceIds = field("faceGroupIds");
        headers = field("aClass21Array1661"); savedHeaders = headers.get(null);
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
        Method t = Model.class.getDeclaredMethod("method472", int.class, int[].class, int.class, int.class, int.class);
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
        headers.set(null, new Class21[1]); Model.method460(bytes.toByteArray(), 0); Model m = Model.method462(0);
        assertArrayEquals(v, (int[])vertexIds.get(m)); assertArrayEquals(f, (int[])faceIds.get(m));
        m.buildAnimationGroups(); assertArrayEquals(new int[]{0,4}, m.vertexGroups[255]);
        assertArrayEquals(new int[]{1}, m.faceGroups[255]);
        assertArrayEquals(new int[]{0,4}, m.faceGroups[0]);
    }
}
