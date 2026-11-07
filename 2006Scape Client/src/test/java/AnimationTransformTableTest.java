import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import org.junit.*;
import static org.junit.Assert.*;

public class AnimationTransformTableTest {
    private Object savedFrames;
    private final int[] savedPivot = new int[3];
    private static Field field(Class<?> owner, String name) throws Exception {
        Field f = owner.getDeclaredField(name); f.setAccessible(true); return f;
    }
    @Before public void save() throws Exception {
        savedFrames = field(AnimationFrame.class, "loadedFrames").get(null);
        for (int i = 0; i < 3; i++) savedPivot[i] = field(Model.class, "animationPivot" + "XYZ".charAt(i)).getInt(null);
    }
    @After public void restore() throws Exception {
        field(AnimationFrame.class, "loadedFrames").set(null, savedFrames);
        for (int i = 0; i < 3; i++) field(Model.class, "animationPivot" + "XYZ".charAt(i)).setInt(null, savedPivot[i]);
    }
    private static Object read(Stream s) throws Exception {
        return Class.forName("AnimationTransformTable").getConstructor(Stream.class).newInstance(s);
    }
    private static int[] a(Object o) throws Exception { return (int[])field(o.getClass(), "transformTypes").get(o); }
    private static int[][] b(Object o) throws Exception { return (int[][])field(o.getClass(), "groupIds").get(o); }
    private static byte[] bytes(int[] types, int[]... rows) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream(); out.write(types.length);
        for (int v : types) out.write(v);
        for (int[] row : rows) { out.write(row.length); for (int v : row) out.write(v); }
        return out.toByteArray();
    }
    @Test public void readsSeparateOrderedSectionsAtCurrentCursorAndStopsBeforeTail() throws Exception {
        byte[] raw = bytes(new int[]{5, 1, 200}, new int[]{255, 0, 128}, new int[0], new int[]{7, 7});
        byte[] padded = new byte[raw.length + 4]; padded[0] = 99; padded[1] = 88;
        System.arraycopy(raw, 0, padded, 2, raw.length); padded[padded.length - 2] = 61;
        Stream s = new Stream(padded); s.currentOffset = 2; Object o = read(s);
        assertArrayEquals(new int[]{5, 1, 200}, a(o));
        assertArrayEquals(new int[]{255, 0, 128}, b(o)[0]);
        assertArrayEquals(new int[0], b(o)[1]); assertArrayEquals(new int[]{7, 7}, b(o)[2]);
        assertEquals(raw.length + 2, s.currentOffset); assertEquals(61, s.readUnsignedByte()); assertEquals(99, padded[0]);
    }
    @Test public void zeroAndMaximumOuterCountsAndMaximumRowLengthAreUnsigned() throws Exception {
        Stream empty = new Stream(new byte[]{0, 42}); Object zero = read(empty);
        assertEquals(0, a(zero).length); assertEquals(0, b(zero).length); assertEquals(1, empty.currentOffset);
        int[] types = new int[255]; int[][] rows = new int[255][];
        for (int i = 0; i < 255; i++) { types[i] = (i * 37) & 255; rows[i] = i == 254 ? new int[255] : new int[]{i}; }
        for (int i = 0; i < 255; i++) rows[254][i] = 255 - i;
        byte[] raw = bytes(types, rows); Stream s = new Stream(raw); Object o = read(s);
        assertArrayEquals(types, a(o)); assertEquals(255, b(o).length);
        for (int i = 0; i < 255; i++) assertArrayEquals("row " + i, rows[i], b(o)[i]);
        assertEquals(raw.length, s.currentOffset);
    }
    @Test public void varyingOneRowDoesNotMoveOrMergeOtherRowsAndStorageIsIndependent() throws Exception {
        int[] types = {1, 3, 5}; int[][] rows = {{2}, {0, 2}, {1}};
        Object first = read(new Stream(bytes(types, rows)));
        for (int slot = 0; slot < 3; slot++) {
            int[][] changed = rows.clone(); changed[slot] = new int[]{128, 255, slot, slot};
            Object o = read(new Stream(bytes(types, changed))); assertArrayEquals(types, a(o));
            for (int i = 0; i < 3; i++) assertArrayEquals(changed[i], b(o)[i]);
        }
        byte[] raw = bytes(types, rows); Object second = read(new Stream(raw)); Arrays.fill(raw, (byte)0);
        a(second)[0] = 77; b(second)[0][0] = 99;
        assertArrayEquals(types, a(first)); assertArrayEquals(new int[]{2}, b(first)[0]);
        assertArrayEquals(new int[]{0, 2}, b(second)[1]);
    }
    @Test public void everyTruncatedPrefixFailsRatherThanInventingMissingValues() throws Exception {
        byte[] raw = bytes(new int[]{1, 3}, new int[]{255, 0}, new int[]{128});
        for (int n = 0; n < raw.length; n++) {
            try { read(new Stream(Arrays.copyOf(raw, n))); fail("prefix " + n); }
            catch (InvocationTargetException e) { assertTrue(e.getCause() instanceof ArrayIndexOutOfBoundsException); }
        }
        assertEquals(2, a(read(new Stream(raw))).length);
    }
    private static Model model() throws Exception {
        Constructor<Model> c = Model.class.getDeclaredConstructor(); c.setAccessible(true); Model m = c.newInstance();
        m.anInt1626 = 3; m.anInt1630 = 3;
        m.anIntArray1627 = new int[]{10, 20, 30}; m.anIntArray1628 = new int[]{40, 50, 60}; m.anIntArray1629 = new int[]{70, 80, 90};
        m.vertexGroups = new int[][]{{2}, {0}, {1}}; m.faceGroups = new int[][]{{1}, {2}, {0}};
        field(Model.class, "faceAlphas").set(m, new int[]{11, 22, 33}); return m;
    }
    private static void apply(Model m, Object table, int slot, int x, int y, int z) throws Exception {
        Constructor<AnimationFrame> c = AnimationFrame.class.getDeclaredConstructor(); c.setAccessible(true); AnimationFrame f = c.newInstance();
        field(AnimationFrame.class, "transformTable").set(f, table); f.transformCount = 1; f.transformIndices = new int[]{slot};
        f.transformX = new int[]{x}; f.transformY = new int[]{y}; f.transformZ = new int[]{z};
        field(AnimationFrame.class, "loadedFrames").set(null, new AnimationFrame[]{f}); m.applyAnimationFrame(0);
    }
    @Test public void decodedRowsSelectOperationsAndGroupIdsRatherThanVertexOrFaceIndices() throws Exception {
        Object o = read(new Stream(bytes(new int[]{1, 3, 5, 200}, new int[]{1}, new int[]{0}, new int[]{2}, new int[]{1})));
        Model m = model(); apply(m, o, 0, 7, -2, 5);
        assertArrayEquals(new int[]{17, 20, 30}, m.anIntArray1627); assertArrayEquals(new int[]{38, 50, 60}, m.anIntArray1628);
        assertArrayEquals(new int[]{75, 80, 90}, m.anIntArray1629); assertArrayEquals(new int[]{11, 22, 33}, (int[])field(Model.class, "faceAlphas").get(m));
        m = model(); apply(m, o, 1, 256, 64, 128);
        assertArrayEquals(new int[]{10, 20, 60}, m.anIntArray1627); assertArrayEquals(new int[]{40, 50, 30}, m.anIntArray1628);
        m = model(); apply(m, o, 2, 2, 99, 99);
        assertArrayEquals(new int[]{27, 22, 33}, (int[])field(Model.class, "faceAlphas").get(m)); assertArrayEquals(new int[]{10, 20, 30}, m.anIntArray1627);
        m = model(); apply(m, o, 3, 7, 8, 9);
        assertArrayEquals(new int[]{10, 20, 30}, m.anIntArray1627); assertArrayEquals(new int[]{11, 22, 33}, (int[])field(Model.class, "faceAlphas").get(m));
    }
    @Test public void orderedDuplicateGroupIdsRepeatOperationAndUnsignedMissingGroupIsIgnored() throws Exception {
        Object o = read(new Stream(bytes(new int[]{1, 5, 1}, new int[]{1, 1}, new int[]{2, 2}, new int[]{255})));
        Model m = model(); apply(m, o, 0, 3, 0, 0); assertArrayEquals(new int[]{16, 20, 30}, m.anIntArray1627);
        m = model(); apply(m, o, 1, 1, 0, 0); assertArrayEquals(new int[]{27, 22, 33}, (int[])field(Model.class, "faceAlphas").get(m));
        m = model(); apply(m, o, 2, 3, 0, 0); assertArrayEquals(new int[]{10, 20, 30}, m.anIntArray1627);
    }
}
