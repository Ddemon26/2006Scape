import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** Original-name observations of Model.method460 (header parsing), Model(int) decoding and requestModel. */
public class ModelHeaderLayoutTest {
    private Object savedHeaders, savedFetcher;
    private final java.util.List<Integer> requests = new java.util.ArrayList<Integer>();

    @Before public void setup() throws Exception {
        savedHeaders = get(Model.class, "modelHeaders");
        savedFetcher = get(Model.class, "modelRequester");
        Model.method459(8, new OnDemandFetcherParent() {
            @Override public void requestModel(int i) { requests.add(i); }
        });
    }

    @After public void restore() throws Exception {
        set(Model.class, "modelHeaders", savedHeaders);
        set(Model.class, "modelRequester", savedFetcher);
    }

    private static void b(ByteArrayOutputStream o, int... v) { for (int x : v) o.write(x); }
    private static void s(ByteArrayOutputStream o, int v) { o.write(v >> 8); o.write(v); }

    /** 3 vertices, 2 faces, 1 textured face, every optional section present. */
    private static byte[] full() {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        b(o, 7, 1, 2);            // 0: vertex flags
        b(o, 1, 2);               // 3: face types
        b(o, 11, 12);             // 5: priorities
        b(o, 21, 22);             // 7: face group ids
        b(o, 4, 8);               // 9: face render flags
        b(o, 9, 8, 7);            // 11: vertex group ids
        b(o, 100, 200);           // 14: alpha
        b(o, 64, 65, 65, 63);     // 16: face vertex data (type1: 3 smarts, type2: 1 smart)
        s(o, 0x1234); s(o, 0x0abc); // 20: colours
        s(o, 1); s(o, 2); s(o, 3);  // 24: textured face
        b(o, 74, 69);             // 30: x deltas (+10,+5)
        b(o, 67, 62);             // 32: y deltas (+3,-2)
        b(o, 71);                 // 34: z delta (+7)
        s(o, 3); s(o, 2); b(o, 1);
        b(o, 1, 255, 1, 1, 1);    // render flags, priority marker, alpha, face groups, vertex groups present
        s(o, 2); s(o, 2); s(o, 1); s(o, 4);
        return o.toByteArray();
    }

    /** 3 vertices, 2 faces, no textured faces, no optional sections, uniform priority 7. */
    private static byte[] bare() {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        b(o, 7, 1, 2);
        b(o, 1, 2);
        b(o, 64, 65, 65, 63);     // 5: face vertex data
        s(o, 0x1234); s(o, 0x0abc); // 9: colours
        b(o, 74, 69);             // 13: x
        b(o, 67, 62);             // 15: y
        b(o, 71);                 // 17: z
        s(o, 3); s(o, 2); b(o, 0);
        b(o, 0, 7, 0, 0, 0);
        s(o, 2); s(o, 2); s(o, 1); s(o, 4);
        return o.toByteArray();
    }

    @Test public void method460FullHeaderLocatesEverySection() throws Exception {
        byte[] data = full();
        Model.method460(data, 2);
        ModelHeader h = ((ModelHeader[]) get(Model.class, "modelHeaders"))[2];
        assertSame(data, h.data);
        assertEquals(3, h.vertexCount); assertEquals(2, h.faceCount); assertEquals(1, h.texturedFaceCount);
        assertEquals(0, h.vertexFlagsOffset); assertEquals(30, h.vertexXOffset); assertEquals(32, h.vertexYOffset); assertEquals(34, h.vertexZOffset);
        assertEquals(11, h.vertexGroupIdsOffset); assertEquals(16, h.faceVertexDataOffset); assertEquals(3, h.faceTypesOffset); assertEquals(20, h.faceColoursOffset);
        assertEquals(9, h.faceRenderFlagsOffset); assertEquals(5, h.facePrioritiesOffset); assertEquals(14, h.faceAlphasOffset); assertEquals(7, h.faceGroupIdsOffset);
        assertEquals(24, h.texturedFaceOffset);
    }

    @Test public void method460AbsentSectionsAreMinusOneAndUniformPriorityIsEncoded() throws Exception {
        Model.method460(bare(), 4);
        ModelHeader h = ((ModelHeader[]) get(Model.class, "modelHeaders"))[4];
        assertEquals(3, h.vertexCount); assertEquals(2, h.faceCount); assertEquals(0, h.texturedFaceCount);
        assertEquals(0, h.vertexFlagsOffset); assertEquals(3, h.faceTypesOffset); assertEquals(5, h.faceVertexDataOffset); assertEquals(9, h.faceColoursOffset);
        assertEquals(13, h.vertexXOffset); assertEquals(15, h.vertexYOffset); assertEquals(17, h.vertexZOffset);
        assertEquals(-1, h.vertexGroupIdsOffset); assertEquals(-1, h.faceRenderFlagsOffset); assertEquals(-1, h.faceAlphasOffset); assertEquals(-1, h.faceGroupIdsOffset);
        assertEquals(-8, h.facePrioritiesOffset);
    }

    /** 3 vertices (x:2 bytes, y:3, z:1), 2 faces, optional sections chosen independently. */
    private static byte[] custom(boolean rf, boolean pr, boolean al, boolean fg, boolean vg) {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        b(o, 7, 3, 2);
        b(o, 1, 2);
        if (pr) b(o, 11, 12);
        if (fg) b(o, 21, 22);
        if (rf) b(o, 4, 8);
        if (vg) b(o, 9, 8, 7);
        if (al) b(o, 100, 200);
        b(o, 64, 65, 65, 63);
        s(o, 0x1234); s(o, 0x0abc);
        b(o, 74, 69);
        b(o, 67, 62, 66);
        b(o, 71);
        s(o, 3); s(o, 2); b(o, 0);
        b(o, rf ? 1 : 0, pr ? 255 : 7, al ? 1 : 0, fg ? 1 : 0, vg ? 1 : 0);
        s(o, 2); s(o, 3); s(o, 1); s(o, 4);
        return o.toByteArray();
    }

    @Test public void method460EachOptionalFlagControlsOnlyItsOwnSection() throws Exception {
        // order after vertex flags(3) and face types(2): priorities, face groups, render flags, vertex groups(3), alpha
        int[][] expect = {
            // rf, pr, al, fg, vg -> offsets: 380 381 382 383 376 ; 377 (face data start)
            {1, 0, 0, 0, 0}, {0, 1, 0, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 0, 1, 0}, {0, 0, 0, 0, 1}, {0, 0, 0, 0, 0}};
        for (int[] e : expect) {
            Model.method460(custom(e[0] == 1, e[1] == 1, e[2] == 1, e[3] == 1, e[4] == 1), 1);
            ModelHeader h = ((ModelHeader[]) get(Model.class, "modelHeaders"))[1];
            int at = 5;
            int pr = e[1] == 1 ? at : -8; if (e[1] == 1) at += 2;
            int fg = e[3] == 1 ? at : -1; if (e[3] == 1) at += 2;
            int rf = e[0] == 1 ? at : -1; if (e[0] == 1) at += 2;
            int vg = e[4] == 1 ? at : -1; if (e[4] == 1) at += 3;
            int al = e[2] == 1 ? at : -1; if (e[2] == 1) at += 2;
            String tag = java.util.Arrays.toString(e);
            assertEquals(tag, pr, h.facePrioritiesOffset); assertEquals(tag, fg, h.faceGroupIdsOffset); assertEquals(tag, rf, h.faceRenderFlagsOffset);
            assertEquals(tag, vg, h.vertexGroupIdsOffset); assertEquals(tag, al, h.faceAlphasOffset);
            assertEquals(tag, at, h.faceVertexDataOffset);
            assertEquals(tag, at + 4, h.faceColoursOffset);
            assertEquals(tag, at + 8, h.vertexXOffset);
            assertEquals(tag, at + 10, h.vertexYOffset);
            assertEquals(tag, at + 13, h.vertexZOffset);
            Model m = Model.getModel(1);
            assertArrayEquals(tag, new int[] {10, 15, 15}, m.anIntArray1627);
            assertArrayEquals(tag, new int[] {3, 1, 3}, m.anIntArray1628);
            assertArrayEquals(tag, new int[] {7, 7, 7}, m.anIntArray1629);
            assertEquals(tag, e[0] == 1, m.anIntArray1637 != null);
            assertEquals(tag, e[1] == 1, get(m, "facePriorities") != null);
            assertEquals(tag, e[2] == 1, get(m, "faceAlphas") != null);
            assertEquals(tag, e[3] == 1, get(m, "faceGroupIds") != null);
            assertEquals(tag, e[4] == 1, get(m, "vertexGroupIds") != null);
        }
    }

    @Test public void method460NullDataStoresAnEmptyHeader()throws Exception {
        Model.method460(null, 5);
        ModelHeader h = ((ModelHeader[]) get(Model.class, "modelHeaders"))[5];
        assertNotNull(h); assertNull(h.data);
        assertEquals(0, h.vertexCount); assertEquals(0, h.faceCount); assertEquals(0, h.texturedFaceCount);
        assertTrue(Model.isModelLoaded(5));
    }

    @Test public void modelConstructorDecodesVertexSections() throws Exception {
        Model.method460(full(), 2);
        Model m = Model.getModel(2);
        assertEquals(3, m.anInt1626); assertEquals(2, m.anInt1630);
        assertEquals(1, ((Integer) get(m, "texturedFaceCount")).intValue());
        assertArrayEquals(new int[] {10, 15, 15}, m.anIntArray1627);
        assertArrayEquals(new int[] {3, 3, 1}, m.anIntArray1628);
        assertArrayEquals(new int[] {7, 7, 7}, m.anIntArray1629);
        assertArrayEquals(new int[] {9, 8, 7}, (int[]) get(m, "vertexGroupIds"));
    }

    @Test public void modelConstructorDecodesFaceSections() throws Exception {
        Model.method460(full(), 2);
        Model m = Model.getModel(2);
        assertArrayEquals(new int[] {0, 0}, m.anIntArray1631);
        assertArrayEquals(new int[] {1, 2}, m.anIntArray1632);
        assertArrayEquals(new int[] {2, 1}, m.anIntArray1633);
        assertArrayEquals(new int[] {0x1234, 0x0abc}, m.anIntArray1640);
        assertArrayEquals(new int[] {4, 8}, m.anIntArray1637);
        assertArrayEquals(new int[] {11, 12}, (int[]) get(m, "facePriorities"));
        assertArrayEquals(new int[] {100, 200}, (int[]) get(m, "faceAlphas"));
        assertArrayEquals(new int[] {21, 22}, (int[]) get(m, "faceGroupIds"));
        assertArrayEquals(new int[] {1}, (int[]) get(m, "texturedFaceVertexA"));
        assertArrayEquals(new int[] {2}, (int[]) get(m, "texturedFaceVertexB"));
        assertArrayEquals(new int[] {3}, (int[]) get(m, "texturedFaceVertexC"));
    }

    @Test public void modelConstructorWithoutOptionalSections() throws Exception {
        Model.method460(bare(), 4);
        Model m = Model.getModel(4);
        assertNull(m.anIntArray1637); assertNull(get(m, "facePriorities")); assertNull(get(m, "faceAlphas"));
        assertNull(get(m, "vertexGroupIds")); assertNull(get(m, "faceGroupIds"));
        assertEquals(7, ((Integer) get(m, "uniformFacePriority")).intValue());
        assertArrayEquals(new int[] {10, 15, 15}, m.anIntArray1627);
        assertArrayEquals(new int[] {0x1234, 0x0abc}, m.anIntArray1640);
        assertArrayEquals(new int[] {0, 0}, m.anIntArray1631);
        assertArrayEquals(new int[] {1, 2}, m.anIntArray1632);
    }

    @Test public void modelConstructorDecodesAllFourFaceVertexEncodings() throws Exception {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        b(o, 0, 0, 0, 0, 0, 0, 0);          // 7 vertices, no deltas
        b(o, 1, 2, 3, 4);                   // face types
        b(o, 64, 65, 65, 65, 66, 65);       // face vertex data: 64 is delta 0
        s(o, 1); s(o, 2); s(o, 3); s(o, 4); // colours
        s(o, 7); s(o, 4); b(o, 0);
        b(o, 0, 7, 0, 0, 0);
        s(o, 0); s(o, 0); s(o, 0); s(o, 6);
        Model.method460(o.toByteArray(), 5);
        Model m = Model.getModel(5);
        assertArrayEquals(new int[] {0, 0, 3, 2}, m.anIntArray1631);
        assertArrayEquals(new int[] {1, 2, 2, 3}, m.anIntArray1632);
        assertArrayEquals(new int[] {2, 3, 5, 6}, m.anIntArray1633);
    }

    @Test public void tableIsIndexedByModelIdAndUnloadClearsTheSlot() throws Exception {
        Model.method460(bare(), 4);
        assertTrue(Model.isModelLoaded(4));
        assertFalse(Model.isModelLoaded(6));
        assertEquals(java.util.Collections.singletonList(6), requests);
        Model.method461(4);
        assertFalse(Model.isModelLoaded(4));
        assertEquals(java.util.Arrays.asList(6, 4), requests);
    }

    @Test public void method548OnTheRealFetcherQueuesAModelFileRequestOnce() throws Exception {
        OnDemandFetcher f = new OnDemandFetcher();
        f.versions[0] = new int[] {0, 5, 5};
        NodeList queue = (NodeList) get(f, "aClass19_1370");
        f.requestModel(0);
        assertNull("version 0 means the file does not exist", queue.popTail());
        f.requestModel(2);
        f.requestModel(2);
        OnDemandData d = (OnDemandData) queue.popTail();
        assertNotNull(d);
        assertEquals(0, d.dataType); assertEquals(2, d.ID);
        assertNull("duplicate request is ignored", queue.popTail());
        f.requestModel(1);
        d = (OnDemandData) queue.popTail();
        assertEquals(0, d.dataType); assertEquals(1, d.ID);
    }

    private static Object get(Object o, String name) throws Exception {
        Class<?> c = o instanceof Class ? (Class<?>) o : o.getClass();
        Field f = c.getDeclaredField(name); f.setAccessible(true); return f.get(o instanceof Class ? null : o);
    }
    private static void set(Class<?> c, String name, Object v) throws Exception {
        Field f = c.getDeclaredField(name); f.setAccessible(true); f.set(null, v);
    }
}
