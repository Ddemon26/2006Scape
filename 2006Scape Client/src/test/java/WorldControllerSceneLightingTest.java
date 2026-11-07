import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** Semantic tests for the WorldController lighting, minimap tile, visibility-table and scene-setup routines (pass 126). */
public class WorldControllerSceneLightingTest {
    private static final int PLANES = 4;
    private static final int SIZE = 104;

    private int savedDrawDistance;
    private Object savedTable;

    @Before public void save() throws Exception {
        savedDrawDistance = WorldController.drawDistance;
        savedTable = sget("tileVisibilityTable");
    }

    @After public void restore() throws Exception {
        WorldController.drawDistance = savedDrawDistance;
        sset("tileVisibilityTable", savedTable);
        sset("cameraTileVisibility", null);
        sset("tilePickRequested", false);
    }

    // ---- reflection helpers (original names only) ----
    private static Field field(Class<?> c, String n) throws Exception { Field f = c.getDeclaredField(n); f.setAccessible(true); return f; }
    private static Object get(Object o, String n) throws Exception { return field(o.getClass(), n).get(o); }
    private static void set(Object o, String n, Object v) throws Exception { field(o.getClass(), n).set(o, v); }
    private static Object sget(String n) throws Exception { return field(WorldController.class, n).get(null); }
    private static void sset(String n, Object v) throws Exception { field(WorldController.class, n).set(null, v); }
    private static int sint(String n) throws Exception { return (Integer) sget(n); }
    private static Object call(Object target, String name, Class<?>[] types, Object... args) throws Exception {
        Method m = WorldController.class.getDeclaredMethod(name, types);
        m.setAccessible(true);
        try { return m.invoke(target, args); } catch (InvocationTargetException e) { if (e.getCause() instanceof Exception) throw (Exception) e.getCause(); throw e; }
    }
    private static final Class<?>[] M306 = {int.class, int.class, Model.class, int.class};
    private static final Class<?>[] M307 = {int.class, int.class, int.class, int.class, int.class, Model.class};
    private static final Class<?>[] M308 = {Model.class, Model.class, int.class, int.class, int.class, boolean.class};

    private static WorldController scene() throws Exception { return new WorldController(new int[PLANES][SIZE + 1][SIZE + 1]); }
    private static Ground[][][] tiles(WorldController wc) throws Exception { return (Ground[][][]) get(wc, "groundArray"); }
    private static int[][][] heights(WorldController wc) throws Exception { return (int[][][]) get(wc, "tileHeights"); }

    private static Ground tile(WorldController wc, int plane, int x, int y) throws Exception {
        Ground[][][] t = tiles(wc);
        if (t[plane][x][y] == null) t[plane][x][y] = new Ground(plane, x, y);
        return t[plane][x][y];
    }

    // ---- model fixtures ----
    private static VertexNormal normal(int x, int y, int z, int count) {
        VertexNormal n = new VertexNormal();
        n.normalX = x; n.normalY = y; n.normalZ = z; n.faceCount = count;
        return n;
    }

    /** Model with the given vertices (x,y,z triples), working and original normals count 1 and generous bounds. */
    private static Model model(int[]... verts) throws Exception {
        java.lang.reflect.Constructor<Model> c = Model.class.getDeclaredConstructor();
        c.setAccessible(true);
        Model m = c.newInstance();
        m.vertexCount = verts.length;
        m.vertexX = new int[verts.length];
        m.vertexY = new int[verts.length];
        m.vertexZ = new int[verts.length];
        m.vertexNormals = new VertexNormal[verts.length];
        m.originalVertexNormals = new VertexNormal[verts.length];
        for (int i = 0; i < verts.length; i++) {
            m.vertexX[i] = verts[i][0];
            m.vertexY[i] = verts[i][1];
            m.vertexZ[i] = verts[i][2];
            m.vertexNormals[i] = normal(0, 0, 0, 1);
            m.originalVertexNormals[i] = normal(0, 0, 0, 1);
        }
        m.minX = m.minZ = -100000; m.maxX = m.maxZ = m.maxY = 100000;
        m.faceCount = 0;
        m.faceVertexA = new int[0]; m.faceVertexB = new int[0]; m.faceVertexC = new int[0];
        return m;
    }

    private static void faces(Model m, int[]... f) {
        m.faceCount = f.length;
        m.faceVertexA = new int[f.length]; m.faceVertexB = new int[f.length]; m.faceVertexC = new int[f.length];
        m.faceRenderFlags = new int[f.length];
        m.faceColors = new int[f.length];
        for (int i = 0; i < f.length; i++) { m.faceVertexA[i] = f[i][0]; m.faceVertexB[i] = f[i][1]; m.faceVertexC[i] = f[i][2]; m.faceColors[i] = 64; }
        try {
            for (String n : new String[] {"faceShadeA", "faceShadeB", "faceShadeC"}) set(m, n, new int[f.length]);
        } catch (Exception e) { throw new RuntimeException(e); }
    }

    private static int count(Model m, int v) { return m.vertexNormals[v].faceCount; }

    // ================= mergeSharedVertexNormals: shared-vertex normal merge =================

    @Test public void method308MergesWorkingWithOriginalNormalsOfCoincidingVertices() throws Exception {
        WorldController wc = scene();
        Model a = model(new int[] {128 + 5, -60 + 6, 64 + 7}, new int[] {1000, 0, 0});
        Model b = model(new int[] {5, 6, 7}, new int[] {-1000, 0, 0});
        a.originalVertexNormals[0] = normal(10, 20, 30, 4); a.vertexNormals[0] = normal(11, 21, 31, 5);
        b.originalVertexNormals[0] = normal(100, 200, 300, 7); b.vertexNormals[0] = normal(101, 201, 301, 8);
        call(wc, "mergeSharedVertexNormals", M308, a, b, 128, -60, 64, false);
        VertexNormal aw = a.vertexNormals[0], bw = b.vertexNormals[0];
        assertArrayEquals(new int[] {111, 221, 331, 12}, new int[] {aw.normalX, aw.normalY, aw.normalZ, aw.faceCount});
        assertArrayEquals(new int[] {111, 221, 331, 12}, new int[] {bw.normalX, bw.normalY, bw.normalZ, bw.faceCount});
        // originals untouched; the non-coinciding vertex untouched
        assertEquals(4, a.originalVertexNormals[0].faceCount);
        assertEquals(7, b.originalVertexNormals[0].faceCount);
        assertEquals(1, count(a, 1));
        assertEquals(1, count(b, 1));
    }

    @Test public void method308OffsetComponentsAreXThenYThenZ() throws Exception {
        WorldController wc = scene();
        // vertex differs from the other model's by (128,-60,64); any other assignment of the three offsets must not merge
        int[][] wrong = {{128, 64, -60}, {-60, 128, 64}, {64, -60, 128}, {-128, 60, -64}, {0, 0, 0}};
        for (int[] o : wrong) {
            Model a = model(new int[] {128, -60, 64});
            Model b = model(new int[] {0, 0, 0});
            call(wc, "mergeSharedVertexNormals", M308, a, b, o[0], o[1], o[2], false);
            assertEquals("offset " + java.util.Arrays.toString(o), 1, count(a, 0));
        }
        Model a = model(new int[] {128, -60, 64});
        Model b = model(new int[] {0, 0, 0});
        call(wc, "mergeSharedVertexNormals", M308, a, b, 128, -60, 64, false);
        assertEquals(2, count(a, 0));
        assertEquals(2, count(b, 0));
    }

    @Test public void method308SkipsVerticesWhoseOriginalNormalCountIsZeroOnEitherSide() throws Exception {
        WorldController wc = scene();
        Model a = model(new int[] {0, 0, 0});
        Model b = model(new int[] {0, 0, 0});
        a.originalVertexNormals[0].faceCount = 0;
        call(wc, "mergeSharedVertexNormals", M308, a, b, 0, 0, 0, false);
        assertEquals(1, count(a, 0)); assertEquals(1, count(b, 0));
        a.originalVertexNormals[0].faceCount = 1; b.originalVertexNormals[0].faceCount = 0;
        call(wc, "mergeSharedVertexNormals", M308, a, b, 0, 0, 0, false);
        assertEquals(1, count(a, 0)); assertEquals(1, count(b, 0));
        b.originalVertexNormals[0].faceCount = 1;
        call(wc, "mergeSharedVertexNormals", M308, a, b, 0, 0, 0, false);
        assertEquals(2, count(a, 0)); assertEquals(2, count(b, 0));
    }

    @Test public void method308BoundsPrefilterUsesUpperYAndBothXZRangesButNoLowerY() throws Exception {
        WorldController wc = scene();
        Model b = model(new int[] {0, 0, 0});
        Model a;
        b.maxY = -1;                                  // vertex y(0) above the other's maxY: skipped although equal
        a = model(new int[] {0, 0, 0}); call(wc, "mergeSharedVertexNormals", M308, a, b, 0, 0, 0, false); assertEquals(1, count(a, 0));
        b.maxY = 0; b.maxX = -1;                      // x outside range
        a = model(new int[] {0, 0, 0}); call(wc, "mergeSharedVertexNormals", M308, a, b, 0, 0, 0, false); assertEquals(1, count(a, 0));
        b.maxX = 0; b.minX = 1;
        a = model(new int[] {0, 0, 0}); call(wc, "mergeSharedVertexNormals", M308, a, b, 0, 0, 0, false); assertEquals(1, count(a, 0));
        b.minX = 0; b.maxZ = -1;
        a = model(new int[] {0, 0, 0}); call(wc, "mergeSharedVertexNormals", M308, a, b, 0, 0, 0, false); assertEquals(1, count(a, 0));
        b.maxZ = 0; b.minZ = 1;
        a = model(new int[] {0, 0, 0}); call(wc, "mergeSharedVertexNormals", M308, a, b, 0, 0, 0, false); assertEquals(1, count(a, 0));
        b.minZ = 0;                                   // all ranges include the vertex: merges
        a = model(new int[] {0, 0, 0}); call(wc, "mergeSharedVertexNormals", M308, a, b, 0, 0, 0, false); assertEquals(2, count(a, 0));
    }

    private static Model triangleModel(int[] extraVertex) throws Exception {
        Model m = extraVertex == null ? model(new int[] {0, 0, 0}, new int[] {10, 0, 0}, new int[] {0, 0, 10})
                : model(new int[] {0, 0, 0}, new int[] {10, 0, 0}, new int[] {0, 0, 10}, extraVertex);
        faces(m, extraVertex == null ? new int[][] {{0, 1, 2}} : new int[][] {{0, 1, 2}, {1, 2, 3}});
        return m;
    }

    @Test public void method308HidesFacesWhoseThreeVerticesAreSharedOnlyWhenFlagSetAndAtLeastThreeShared() throws Exception {
        WorldController wc = scene();
        Model a = triangleModel(null), b = triangleModel(null);
        call(wc, "mergeSharedVertexNormals", M308, a, b, 0, 0, 0, false);     // flag off: merged, nothing hidden
        assertEquals(0, a.faceRenderFlags[0]); assertEquals(0, b.faceRenderFlags[0]);
        assertEquals(2, count(a, 0));
        a = triangleModel(null); b = triangleModel(null);
        call(wc, "mergeSharedVertexNormals", M308, a, b, 0, 0, 0, true);      // 3 shared, flag on: hidden in both
        assertEquals(-1, a.faceRenderFlags[0]); assertEquals(-1, b.faceRenderFlags[0]);
        // only two shared vertices: merged but not hidden
        a = triangleModel(null); b = model(new int[] {0, 0, 0}, new int[] {10, 0, 0}, new int[] {500, 0, 500});
        faces(b, new int[] {0, 1, 2});
        call(wc, "mergeSharedVertexNormals", M308, a, b, 0, 0, 0, true);
        assertEquals(0, a.faceRenderFlags[0]); assertEquals(0, b.faceRenderFlags[0]);
        assertEquals(2, count(a, 0)); assertEquals(2, count(a, 1)); assertEquals(1, count(a, 2));
    }

    @Test public void method308StampsAreFreshPerCallSoStaleSharedVerticesDoNotHideFaces() throws Exception {
        WorldController wc = scene();
        Model a = triangleModel(new int[] {10, 0, 10});
        Model b1 = triangleModel(null);                        // shares vertices 0,1,2 of a
        int before = (Integer) get(wc, "mergeStamp");
        call(wc, "mergeSharedVertexNormals", M308, a, b1, 0, 0, 0, false);
        assertEquals(before + 1, ((Integer) get(wc, "mergeStamp")).intValue());
        int[] aStamps = (int[]) get(wc, "modelSharedVertexStamps"), bStamps = (int[]) get(wc, "otherSharedVertexStamps");
        int stamp = (Integer) get(wc, "mergeStamp");
        assertEquals(stamp, aStamps[0]); assertEquals(stamp, aStamps[1]); assertEquals(stamp, aStamps[2]);
        assertNotEquals(stamp, aStamps[3]);
        assertEquals(stamp, bStamps[0]); assertEquals(stamp, bStamps[2]);
        // second call shares a's vertices 1,2,3 only; face {1,2,3} hides, face {0,1,2} keeps because vertex 0 carries an old stamp
        Model b2 = model(new int[] {10, 0, 0}, new int[] {0, 0, 10}, new int[] {10, 0, 10});
        faces(b2, new int[] {0, 1, 2});
        call(wc, "mergeSharedVertexNormals", M308, a, b2, 0, 0, 0, true);
        assertEquals(0, a.faceRenderFlags[0]);
        assertEquals(-1, a.faceRenderFlags[1]);
        assertEquals(-1, b2.faceRenderFlags[0]);
        assertEquals(stamp + 1, ((Integer) get(wc, "mergeStamp")).intValue());
    }

    // ================= mergeGroundDecorationNormals: ground-decoration neighbours =================

    private static Object3 decoration(Model m) { Object3 d = new Object3(); d.model = m; return d; }

    @Test public void method306MergesWithPlusXPlusYAndBothPlusXDiagonalGroundDecorationsOnly() throws Exception {
        WorldController wc = scene();
        int x = 50, y = 50, plane = 1;
        // model vertices placed where each neighbour's single vertex (0,0,0) coincides after the expected offset
        int[][] expected = {{128, 0, 0}, {0, 0, 128}, {128, 0, 128}, {128, 0, -128}};
        int[][] decoys = {{-128, 0, 0}, {0, 0, -128}, {-128, 0, -128}, {-128, 0, 128}, {0, 0, 0}};
        int n = expected.length + decoys.length;
        int[][] verts = new int[n][];
        for (int i = 0; i < expected.length; i++) verts[i] = expected[i];
        for (int i = 0; i < decoys.length; i++) verts[expected.length + i] = decoys[i];
        Model m = model(verts);
        int[][] tiles = {{1, 0}, {0, 1}, {1, 1}, {1, -1}, {-1, 0}, {0, -1}, {-1, -1}, {-1, 1}};
        Model[] neighbours = new Model[tiles.length];
        for (int i = 0; i < tiles.length; i++) {
            neighbours[i] = model(new int[] {0, 0, 0});
            tile(wc, plane, x + tiles[i][0], y + tiles[i][1]).obj3 = decoration(neighbours[i]);
        }
        call(wc, "mergeGroundDecorationNormals", M306, x, plane, m, y);
        for (int i = 0; i < 4; i++) {
            assertEquals("neighbour " + i, 2, count(neighbours[i], 0));
            assertEquals("vertex " + i, 2, count(m, i));
        }
        for (int i = 4; i < tiles.length; i++) assertEquals("decoy neighbour " + i, 1, count(neighbours[i], 0));
        for (int i = 4; i < n; i++) assertEquals("decoy vertex " + i, 1, count(m, i));
    }

    @Test public void method306IgnoresNeighboursWithoutGroundDecorationOrNormalsAndOtherSlots() throws Exception {
        WorldController wc = scene();
        Model m = model(new int[] {128, 0, 0});
        Model noNormals = model(new int[] {0, 0, 0});
        noNormals.vertexNormals = null;
        tile(wc, 1, 51, 50).obj3 = decoration(noNormals);              // present but not lit-able: skipped without error
        call(wc, "mergeGroundDecorationNormals", M306, 50, 1, m, 50);
        assertEquals(1, count(m, 0));
        Model wallModel = model(new int[] {0, 0, 0});
        Ground g = tile(wc, 1, 51, 50);
        g.obj3 = null;
        g.obj1 = new Object1(); g.obj1.model = wallModel;
        call(wc, "mergeGroundDecorationNormals", M306, 50, 1, m, 50);                  // wall (slot 1) on the neighbour: not merged
        assertEquals(1, count(m, 0)); assertEquals(1, count(wallModel, 0));
        // another plane
        Model other = model(new int[] {0, 0, 0});
        tile(wc, 2, 51, 50).obj3 = decoration(other);
        call(wc, "mergeGroundDecorationNormals", M306, 50, 1, m, 50);
        assertEquals(1, count(other, 0));
    }

    // ================= mergeNeighbourNormals: neighbour-tile normal merge =================

    @Test public void method307ScansEastColumnAndNorthTileOnOwnPlaneAndFullThreeByThreeOnPlaneAbove() throws Exception {
        WorldController wc = scene();
        int x = 50, y = 50, plane = 1;
        int[][][] h = heights(wc);
        for (int[][] p : h) for (int[] r : p) java.util.Arrays.fill(r, 100);
        for (int[] r : h[plane + 1]) java.util.Arrays.fill(r, 40);     // upper plane corners 40 vs own 100: difference -60
        int[] offs = {-1, 0, 1};
        // model vertices: one per (plane layer, dx, dz)
        int[][] verts = new int[18][];
        for (int layer = 0; layer < 2; layer++)
            for (int dx = 0; dx < 3; dx++)
                for (int dz = 0; dz < 3; dz++)
                    verts[layer * 9 + dx * 3 + dz] = new int[] {offs[dx] * 128, layer == 0 ? 0 : -60, offs[dz] * 128};
        Model m = model(verts);
        Model[][] ns = new Model[2][9];
        for (int layer = 0; layer < 2; layer++)
            for (int dx = 0; dx < 3; dx++)
                for (int dz = 0; dz < 3; dz++) {
                    Model nm = model(new int[] {0, 0, 0});
                    ns[layer][dx * 3 + dz] = nm;
                    Ground g = tile(wc, plane + layer, x + offs[dx], y + offs[dz]);
                    g.obj1 = new Object1(); g.obj1.model = nm;
                }
        // decoys on planes outside the scanned pair
        Model low = model(new int[] {0, 0, 0}), high = model(new int[] {0, 0, 0});
        tile(wc, plane - 1, x + 1, y).obj1 = new Object1(); tiles(wc)[plane - 1][x + 1][y].obj1.model = low;
        tile(wc, plane + 2, x + 1, y).obj1 = new Object1(); tiles(wc)[plane + 2][x + 1][y].obj1.model = high;
        call(wc, "mergeNeighbourNormals", M307, plane, 1, 1, x, y, m);
        boolean[][] sameExpected = new boolean[3][3];
        sameExpected[2][0] = sameExpected[2][1] = sameExpected[2][2] = true;   // east column
        sameExpected[1][2] = true;                                             // north tile
        for (int dx = 0; dx < 3; dx++)
            for (int dz = 0; dz < 3; dz++) {
                assertEquals("own plane dx=" + offs[dx] + " dz=" + offs[dz], sameExpected[dx][dz] ? 2 : 1, count(ns[0][dx * 3 + dz], 0));
                assertEquals("upper plane dx=" + offs[dx] + " dz=" + offs[dz], 2, count(ns[1][dx * 3 + dz], 0));
            }
        assertEquals(1, count(low, 0)); assertEquals(1, count(high, 0));
    }

    @Test public void method307OnTopPlaneSkipsTheNonexistentPlaneAbove() throws Exception {
        WorldController wc = scene();
        Model m = model(new int[] {128, 0, 0});
        Model nm = model(new int[] {0, 0, 0});
        tile(wc, 3, 51, 50).obj1 = new Object1(); tiles(wc)[3][51][50].obj1.model = nm;
        call(wc, "mergeNeighbourNormals", M307, 3, 1, 1, 50, 50, m);
        assertEquals(2, count(nm, 0));
    }

    @Test public void method307UsesSecondWallModelAndScaledSceneObjectFootprintOffsets() throws Exception {
        WorldController wc = scene();
        int x = 50, y = 50, plane = 1;
        // own model has footprint 2 x 1 tiles: neighbour 1x1 wall secondary model at (x+2, y) -> x offset (2)*128 + (1-2)*64 = 192
        Model m = model(new int[] {192, 0, 0}, new int[] {128 + 64, 0, 0}, new int[] {-1, -1, -1});
        Model secondary = model(new int[] {0, 0, 0});
        Ground g = tile(wc, plane, x + 2, y);
        g.obj1 = new Object1(); g.obj1.secondaryModel = secondary;
        call(wc, "mergeNeighbourNormals", M307, plane, 2, 1, x, y, m);
        assertEquals(3, count(secondary, 0));
        assertEquals(2, count(m, 0));
        assertEquals(2, count(m, 1)); assertEquals(1, count(m, 2));
    }

    @Test public void method307MergesSceneObjectOncePerCoveredTileInScanRange() throws Exception {
        WorldController wc = scene();
        int x = 50, y = 50, plane = 1;
        // vertical 1x2 object at (x+1, y..y+1): footprint width 1, height 2: z offset (y-y)*128 + (2-1)*64 = 64
        Model m = model(new int[] {128, 0, 64});
        Model om = model(new int[] {0, 0, 0});
        Object5 o = new Object5();
        o.minTileX = x + 1; o.maxTileX = x + 1; o.minTileY = y; o.maxTileY = y + 1;
        o.model = om;
        for (int ty = y; ty <= y + 1; ty++) {
            Ground g = tile(wc, plane, x + 1, ty);
            g.obj5Array[g.objectCount++] = o;
        }
        call(wc, "mergeNeighbourNormals", M307, plane, 1, 1, x, y, m);
        assertEquals(3, count(m, 0));            // both covered tiles lie in the scan range: merged twice
        assertEquals(3, count(om, 0));
    }

    // ================= lightScene: lighting =================

    private static int[] shades(Model m, int face) throws Exception {
        return new int[] {((int[]) get(m, "faceShadeA"))[face], ((int[]) get(m, "faceShadeB"))[face], ((int[]) get(m, "faceShadeC"))[face]};
    }

    private static Model litModel() throws Exception {
        Model m = model(new int[] {0, 0, 0}, new int[] {10, 0, 0}, new int[] {0, 0, 10});
        faces(m, new int[] {0, 1, 2});
        m.vertexNormals[0] = normal(256, 0, 0, 1);
        m.vertexNormals[1] = normal(0, 256, 0, 1);
        m.vertexNormals[2] = normal(0, 0, 256, 1);
        m.faceRenderFlags = null;
        return m;
    }

    @Test public void method305ArgumentsAreLightYThenLightXThenLightZAgainstNormalXYZ() throws Exception {
        WorldController wc = scene();
        Model m = litModel();
        Ground g = tile(wc, 0, 10, 10);
        g.obj3 = decoration(m);
        call(wc, "lightScene", new Class<?>[] {int.class, int.class, int.class}, 4, 3, 0);
        // |L|=5, contrast 768*5>>8=15, ambient 64: x-normal 64+768/15=115, y-normal 64+1024/15=132, z-normal 64; colour 64 halves
        assertArrayEquals(new int[] {115 * 64 >> 7, 132 * 64 >> 7, 64 * 64 >> 7}, shades(m, 0));
        assertNull(m.vertexNormals); assertNull(m.originalVertexNormals);
        // swapped first/second argument gives a different answer for x and y normals
        Model s = litModel();
        tiles(wc)[0][10][10].obj3 = decoration(s);
        call(wc, "lightScene", new Class<?>[] {int.class, int.class, int.class}, 3, 4, 0);
        assertArrayEquals(new int[] {(64 + 1024 / 15) * 64 >> 7, (64 + 768 / 15) * 64 >> 7, 32}, shades(s, 0));
    }

    @Test public void method305LightsWallModelsSceneObjectsAndGroundDecorationsButNotWallDecorationOrItemStack() throws Exception {
        WorldController wc = scene();
        Model wall = litModel(), wall2 = litModel(), scene = litModel(), deco = litModel(), wallDeco = litModel();
        Ground g = tile(wc, 2, 20, 21);
        g.obj1 = new Object1(); g.obj1.model = wall; g.obj1.secondaryModel = wall2;
        Object5 o = new Object5(); o.minTileX = o.maxTileX = 20; o.minTileY = o.maxTileY = 21; o.model = scene;
        g.obj5Array[g.objectCount++] = o;
        g.obj3 = decoration(deco);
        g.obj2 = new Object2(); g.obj2.model = wallDeco;
        call(wc, "lightScene", new Class<?>[] {int.class, int.class, int.class}, -10, -50, -50);
        assertNull(wall.vertexNormals); assertNull(wall2.vertexNormals);
        assertNull(scene.vertexNormals); assertNull(deco.vertexNormals);
        assertNotNull(wallDeco.vertexNormals);
    }

    @Test public void method305MergesWallPrimaryWithSecondaryAtZeroOffsetBeforeLightingAndLightsAModelOnlyOnce() throws Exception {
        WorldController wc = scene();
        Model a = model(new int[] {0, 0, 0}, new int[] {10, 0, 0}, new int[] {0, 0, 10});
        faces(a, new int[] {0, 1, 2});
        Model b = model(new int[] {0, 0, 0}, new int[] {10, 0, 0}, new int[] {0, 0, 10});
        faces(b, new int[] {0, 1, 2});
        a.faceRenderFlags = null; b.faceRenderFlags = null;
        a.vertexNormals[0] = normal(256, 0, 0, 1); a.originalVertexNormals[0] = normal(256, 0, 0, 1);
        b.vertexNormals[0] = normal(0, 256, 0, 1); b.originalVertexNormals[0] = normal(0, 256, 0, 1);
        Ground g = tile(wc, 0, 30, 30);
        g.obj1 = new Object1(); g.obj1.model = a; g.obj1.secondaryModel = b;
        call(wc, "lightScene", new Class<?>[] {int.class, int.class, int.class}, 4, 3, 0);
        // merged normal (256,256,0) count 2: 64 + (3*256+4*256)/(15*2) = 64+59
        assertEquals((64 + 1792 / 30) * 64 >> 7, shades(a, 0)[0]);
        assertEquals((64 + 1792 / 30) * 64 >> 7, shades(b, 0)[0]);
        // a scene object shared by two tiles is lit once: the second tile sees null normals and skips
        Model so = litModel();
        Object5 o = new Object5(); o.minTileX = 40; o.maxTileX = 41; o.minTileY = 40; o.maxTileY = 40; o.model = so;
        for (int tx = 40; tx <= 41; tx++) { Ground t = tile(wc, 0, tx, 40); t.obj5Array[t.objectCount++] = o; }
        call(wc, "lightScene", new Class<?>[] {int.class, int.class, int.class}, 4, 3, 0);
        assertNull(so.vertexNormals);
    }

    @Test public void method305SkipsModelsWithoutNormalsAndEmptyTiles() throws Exception {
        WorldController wc = scene();
        Model m = litModel();
        m.vertexNormals = null;
        Ground g = tile(wc, 1, 5, 5);
        g.obj3 = decoration(m);
        tile(wc, 1, 6, 5);
        call(wc, "lightScene", new Class<?>[] {int.class, int.class, int.class}, 4, 3, 0);
        assertArrayEquals(new int[] {0, 0, 0}, shades(m, 0));
    }

    // ================= drawMinimapTile: minimap tile =================

    private static final int STRIDE = 512;

    private static int[] raster() { int[] r = new int[STRIDE * 6]; java.util.Arrays.fill(r, 0xABCDEF); return r; }

    private static ShapedTile shaped(int shape, int rotation, int base, int masked) throws Exception {
        Field f = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        ShapedTile c = (ShapedTile) ((sun.misc.Unsafe) f.get(null)).allocateInstance(ShapedTile.class);
        set(c, "shape", shape); set(c, "rotation", rotation); set(c, "underlayMapColour", base); set(c, "overlayMapColour", masked);
        return c;
    }

    private static final Class<?>[] M309 = {int[].class, int.class, int.class, int.class, int.class};

    @Test public void method309PlainTileFillsFourByFourWithItsColourUsingStride512AndSkipsColourZero() throws Exception {
        WorldController wc = scene();
        tile(wc, 2, 7, 9).plainTile = new Class43(0, 0, 0, 0, 0, 0x336699, true);
        int[] px = raster();
        call(wc, "drawMinimapTile", M309, px, 100, 2, 7, 9);
        for (int row = 0; row < 4; row++)
            for (int col = 0; col < STRIDE; col++) {
                int idx = 100 + row * STRIDE + col;
                if (idx >= px.length) break;
                int expected = col < 4 ? 0x336699 : 0xABCDEF;
                assertEquals(expected, px[idx]);
            }
        assertEquals(0xABCDEF, px[99]);
        assertEquals(0xABCDEF, px[100 + 4 * STRIDE]);
        tile(wc, 2, 8, 9).plainTile = new Class43(0, 0, 0, 0, 0, 0, true);
        int[] px2 = raster();
        call(wc, "drawMinimapTile", M309, px2, 100, 2, 8, 9);
        assertArrayEquals(raster(), px2);
        // wrong tile / empty tile / missing tile leave the raster alone
        call(wc, "drawMinimapTile", M309, px2, 100, 2, 50, 50);
        call(wc, "drawMinimapTile", M309, px2, 100, 1, 7, 9);
        assertArrayEquals(raster(), px2);
    }

    private static int[] block(int[] px, int off) {
        int[] r = new int[16];
        for (int row = 0; row < 4; row++) for (int col = 0; col < 4; col++) r[row * 4 + col] = px[off + row * STRIDE + col];
        return r;
    }

    @Test public void method309ShapedTileUsesMaskForSecondColourAndRotationReordersCells() throws Exception {
        WorldController wc = scene();
        int[][] masks = (int[][]) get(wc, "tileShapeMasks");
        int[][] rot = (int[][]) get(wc, "tileRotationOrders");
        assertEquals(13, masks.length);
        for (int[] m : masks) assertEquals(16, m.length);
        assertEquals(4, rot.length);
        for (int[] r : rot) { assertEquals(16, r.length); boolean[] seen = new boolean[16]; for (int v : r) seen[v] = true; for (boolean s : seen) assertTrue(s); }
        int base = 0x111111, masked = 0x222222;
        for (int shape = 0; shape < masks.length; shape++)
            for (int rotation = 0; rotation < 4; rotation++) {
                tile(wc, 1, 3, 4).plainTile = null;
                tile(wc, 1, 3, 4).shapedTile = shaped(shape, rotation, base, masked);
                int[] px = raster();
                call(wc, "drawMinimapTile", M309, px, 0, 1, 3, 4);
                int[] got = block(px, 0);
                for (int p = 0; p < 16; p++)
                    assertEquals("shape " + shape + " rot " + rotation + " cell " + p, masks[shape][rot[rotation][p]] != 0 ? masked : base, got[p]);
            }
        // mask 1 cells really use the second colour and mask 0 cells the first: shape 1 is all ones, shape 0 all zeros
        for (int v : masks[0]) assertEquals(0, v);
        for (int v : masks[1]) assertEquals(1, v);
    }

    @Test public void method309ShapedTileWithZeroBaseColourOnlyPaintsMaskedCells() throws Exception {
        WorldController wc = scene();
        int[][] masks = (int[][]) get(wc, "tileShapeMasks");
        int[][] rot = (int[][]) get(wc, "tileRotationOrders");
        tile(wc, 1, 3, 4).shapedTile = shaped(2, 1, 0, 0x222222);
        int[] px = raster();
        call(wc, "drawMinimapTile", M309, px, 0, 1, 3, 4);
        int[] got = block(px, 0);
        int painted = 0;
        for (int p = 0; p < 16; p++) {
            boolean on = masks[2][rot[1][p]] != 0;
            assertEquals(on ? 0x222222 : 0xABCDEF, got[p]);
            if (on) painted++;
        }
        assertTrue(painted > 0 && painted < 16);
        // zero masked colour with zero base still paints cells as 0 (colour value is written for mask 1)
        tile(wc, 1, 3, 5).shapedTile = shaped(1, 0, 0, 0);
        int[] p2 = raster();
        call(wc, "drawMinimapTile", M309, p2, 0, 1, 3, 5);
        for (int v : block(p2, 0)) assertEquals(0, v);
        // a plain tile takes precedence over a shaped tile on the same ground
        tile(wc, 1, 3, 6).plainTile = new Class43(0, 0, 0, 0, 0, 0x333333, true);
        tile(wc, 1, 3, 6).shapedTile = shaped(1, 0, 0x444444, 0x555555);
        int[] p3 = raster();
        call(wc, "drawMinimapTile", M309, p3, 0, 1, 3, 6);
        for (int v : block(p3, 0)) assertEquals(0x333333, v);
    }

    @Test public void method309RotationTablesAreQuarterTurnsOfTheFourByFourCells() throws Exception {
        WorldController wc = scene();
        int[][] rot = (int[][]) get(wc, "tileRotationOrders");
        for (int p = 0; p < 16; p++) assertEquals(p, rot[0][p]);
        for (int p = 0; p < 16; p++) {
            int row = p / 4, col = p % 4;
            int turned = rot[1][p];
            // one quarter turn: the source cell is the one reached by moving a quarter turn about the 4x4 centre
            assertEquals((3 - col) * 4 + row, turned);
            assertEquals(15 - p, rot[2][p]);
            assertEquals(col * 4 + (3 - row), rot[3][p]);
        }
    }

    // ================= buildTileVisibilityTable / isPointInViewport: tile visibility table =================

    private static boolean[][][][] table() throws Exception { return (boolean[][][][]) sget("tileVisibilityTable"); }

    private static void build(int below, int above, int w, int h, int[] camHeights) throws Exception {
        sset("tileVisibilityTable", new boolean[8][32][256][256]);
        Method m = WorldController.class.getDeclaredMethod("buildTileVisibilityTable", int.class, int.class, int.class, int.class, int[].class);
        m.setAccessible(true);
        m.invoke(null, below, above, w, h, camHeights);
    }

    private static int visibleCount(int pitchIdx, int yawIdx, int dd) throws Exception {
        int n = 0;
        for (int x = 0; x < 2 * dd; x++) for (int z = 0; z < 2 * dd; z++) if (table()[pitchIdx][yawIdx][x][z]) n++;
        return n;
    }

    private static boolean vis(int pitchIdx, int yawIdx, int dd, int dx, int dz) throws Exception { return table()[pitchIdx][yawIdx][dx + dd][dz + dd]; }

    @Test public void method310ViewportCentreAndBoundsAreHalfWidthHalfHeightAndZeroToWidthHeight() throws Exception {
        WorldController.drawDistance = 3;
        build(500, 800, 512, 334, new int[9]);
        assertEquals(256, sint("viewportCentreX")); assertEquals(167, sint("viewportCentreY"));
        assertEquals(0, sint("viewportMinX")); assertEquals(0, sint("viewportMinY"));
        assertEquals(512, sint("viewportMaxX")); assertEquals(334, sint("viewportMaxY"));
        build(500, 800, 100, 51, new int[9]);
        assertEquals(50, sint("viewportCentreX")); assertEquals(25, sint("viewportCentreY"));
        assertEquals(100, sint("viewportMaxX")); assertEquals(51, sint("viewportMaxY"));
    }

    @Test public void method310YawZeroSeesAheadAlongPositiveZAndNeverBehindTheCamera() throws Exception {
        int dd = 8;
        WorldController.drawDistance = dd;
        int[] heights = new int[9];
        java.util.Arrays.fill(heights, 200);
        build(500, 800, 512, 334, heights);
        boolean ahead = false, behind = false;
        for (int dx = -dd; dx < dd; dx++)
            for (int dz = -dd; dz < dd; dz++) {
                if (vis(0, 0, dd, dx, dz)) { if (dz >= 0) ahead = true; if (dz < -2) behind = true; }
            }
        assertTrue(ahead);
        assertFalse(behind);
        // lateral extent grows with viewport width but forward tiles in the centre stay visible
        build(500, 800, 512, 334, heights);
        int wide = visibleCount(0, 0, dd);
        build(500, 800, 64, 334, heights);
        int narrow = visibleCount(0, 0, dd);
        assertTrue(narrow < wide);
        for (int dx = -dd; dx < dd; dx++) for (int dz = -dd; dz < dd; dz++) if (vis(0, 0, dd, dx, dz)) { /* narrow is a subset of wide */ }
    }

    @Test public void method310NarrowViewportIsSubsetOfWideAndHeightRangeExtendsCoverage() throws Exception {
        int dd = 8;
        WorldController.drawDistance = dd;
        int[] heights = new int[9];
        java.util.Arrays.fill(heights, 200);
        build(500, 800, 512, 334, heights);
        boolean[][] wide = new boolean[2 * dd][2 * dd];
        for (int x = 0; x < 2 * dd; x++) for (int z = 0; z < 2 * dd; z++) wide[x][z] = table()[0][0][x][z];
        build(500, 800, 100, 334, heights);
        for (int x = 0; x < 2 * dd; x++) for (int z = 0; z < 2 * dd; z++) if (table()[0][0][x][z]) assertTrue(wide[x][z]);
        // vertical viewport size: smaller height shrinks the set, never grows it
        build(500, 800, 512, 100, heights);
        for (int x = 0; x < 2 * dd; x++) for (int z = 0; z < 2 * dd; z++) if (table()[0][0][x][z]) assertTrue(wide[x][z]);
        // samples sit at -i + 128n up to j: with multiples of 128 the zero sample is included, so widening only adds tiles
        build(0, 0, 512, 334, heights);
        int none = visibleCount(0, 0, dd);
        build(512, 0, 512, 334, heights);
        int onlyBelow = visibleCount(0, 0, dd);
        build(0, 768, 512, 334, heights);
        int onlyAbove = visibleCount(0, 0, dd);
        build(512, 768, 512, 334, heights);
        int both = visibleCount(0, 0, dd);
        assertTrue(onlyBelow >= none); assertTrue(onlyAbove >= none);
        assertTrue(both >= onlyBelow); assertTrue(both >= onlyAbove);
        assertTrue(both > none);
    }

    @Test public void method310YawIndexTurnsTheViewAndPitchIndexSelectsTheCameraHeightEntry() throws Exception {
        int dd = 8;
        WorldController.drawDistance = dd;
        int[] heights = new int[9];
        java.util.Arrays.fill(heights, 200);
        build(500, 800, 512, 334, heights);
        // yaw 512 (index 8) looks toward negative x, yaw 1536 (index 24) toward positive x
        int negNeg = 0, negPos = 0, posNeg = 0, posPos = 0;
        for (int dx = -dd; dx < dd; dx++)
            for (int dz = -dd; dz < dd; dz++) {
                if (vis(0, 8, dd, dx, dz)) { if (dx < 0) negNeg++; else if (dx > 0) negPos++; }
                if (vis(0, 24, dd, dx, dz)) { if (dx < 0) posNeg++; else if (dx > 0) posPos++; }
            }
        assertTrue(negNeg > 0); assertTrue(negPos == 0);
        assertTrue(posPos > 0); assertTrue(posNeg == 0);
        // yaw 1024 (index 16) looks toward negative z
        int zNeg = 0, zPos = 0;
        for (int dx = -dd; dx < dd; dx++) for (int dz = -dd; dz < dd; dz++) if (vis(0, 16, dd, dx, dz)) { if (dz < 0) zNeg++; else if (dz > 2) zPos++; }
        assertTrue(zNeg > 0); assertEquals(0, zPos);
        // only the pitch bucket whose camera-height entry is large changes: entry index = (pitch-128)/32
        int[] only3 = new int[9]; java.util.Arrays.fill(only3, 200);
        build(512, 768, 512, 334, only3);
        int[] before = new int[8];
        for (int r = 0; r < 8; r++) before[r] = visibleCount(r, 0, dd);
        only3[3] = 3000;
        build(512, 768, 512, 334, only3);
        // a table row unions its own pitch bucket with the next one, so entry 3 reaches rows 2 and 3 only; rows 0,1,4..7 are unchanged
        for (int r = 0; r < 8; r++) if (r != 2 && r != 3) assertEquals("row " + r, before[r], visibleCount(r, 0, dd));
        assertTrue(visibleCount(2, 0, dd) < before[2]);
    }

    @Test public void method310StoresTilesRelativeToCameraTileWithinDrawDistanceOnly() throws Exception {
        int dd = 4;
        WorldController.drawDistance = dd;
        int[] heights = new int[9];
        java.util.Arrays.fill(heights, 200);
        build(500, 800, 512, 334, heights);
        // nothing is stored beyond index 2*dd-1 on either axis
        for (int x = 0; x < 256; x++) for (int z = 0; z < 256; z++) if (x >= 2 * dd || z >= 2 * dd) assertFalse(table()[0][0][x][z]);
        // pitch rows 8 (index beyond 0..7) are not stored: the table has exactly 8 pitch rows
        assertEquals(8, table().length);
        assertEquals(32, table()[0].length);
    }

    private static boolean pointOnScreen(int height, int z, int x) throws Exception {
        Method m = WorldController.class.getDeclaredMethod("isPointInViewport", int.class, int.class, int.class);
        m.setAccessible(true);
        return (Boolean) m.invoke(null, height, z, x);
    }

    private static void camera(int pitch, int yaw, int w, int h) throws Exception {
        WorldController.drawDistance = 1;
        build(0, 0, w, h, new int[9]);          // sets viewport statics
        sset("pitchSine", Model.sineTable16[pitch]); sset("pitchCosine", Model.cosineTable16[pitch]);
        sset("yawSine", Model.sineTable16[yaw]); sset("yawCosine", Model.cosineTable16[yaw]);
    }

    @Test public void method311ProjectsDepthWithinFiftyToThirtyFiveHundredAndChecksViewportInclusively() throws Exception {
        camera(0, 0, 512, 334);                  // pitch 0: depth = z, screen y = height
        assertFalse(pointOnScreen(0, 49, 0));
        assertTrue(pointOnScreen(0, 50, 0));
        assertTrue(pointOnScreen(0, 3500, 0));
        assertFalse(pointOnScreen(0, 3501, 0));
        assertFalse(pointOnScreen(0, -100, 0));  // behind
        // screen x = 256 + (x<<9)/depth: depth 512 -> x offset equals pixels; edges 0 and 512 inclusive
        assertTrue(pointOnScreen(0, 512, 256)); assertFalse(pointOnScreen(0, 512, 257));
        assertTrue(pointOnScreen(0, 512, -256)); assertFalse(pointOnScreen(0, 512, -257));
        // screen y = 167 + (height<<9)/depth: 0..334 inclusive
        assertTrue(pointOnScreen(167, 512, 0)); assertFalse(pointOnScreen(168, 512, 0));
        assertTrue(pointOnScreen(-167, 512, 0)); assertFalse(pointOnScreen(-168, 512, 0));
    }

    @Test public void method311ArgumentOrderIsHeightThenZThenX() throws Exception {
        camera(0, 0, 512, 334);
        // x moves the point sideways only, z is the distance in front, height moves it vertically
        assertTrue(pointOnScreen(0, 512, 200)); assertFalse(pointOnScreen(0, 200, 512));
        assertTrue(pointOnScreen(100, 512, 0)); assertFalse(pointOnScreen(512, 100, 0));
    }

    // ================= requestTilePick: tile pick request =================

    @Test public void method312StoresPickPointAndClearsPreviousResult() throws Exception {
        WorldController wc = scene();
        WorldController.pickedTileX = 5; WorldController.pickedTileY = 6;
        sset("tilePickRequested", false);
        wc.requestTilePick(33, 77);
        assertTrue((Boolean) sget("tilePickRequested"));
        assertEquals(77, sint("pickScreenX")); assertEquals(33, sint("pickScreenY"));
        assertEquals(-1, WorldController.pickedTileX); assertEquals(-1, WorldController.pickedTileY);
        // the stored (468,469) pair is read as (x, y) by the point-in-triangle test the tile drawing uses
        Method tri = WorldController.class.getDeclaredMethod("isPointInTriangle", int.class, int.class, int.class, int.class, int.class, int.class, int.class, int.class);
        tri.setAccessible(true);
        // triangle spanning x 70..90, y 30..40 contains (x=77,y=33) but not (x=33,y=77)
        int[] ys = {30, 30, 40}, xs = {70, 90, 80};
        assertTrue((Boolean) tri.invoke(wc, sint("pickScreenX"), sint("pickScreenY"), ys[0], ys[1], ys[2], xs[0], xs[1], xs[2]));
        assertFalse((Boolean) tri.invoke(wc, sint("pickScreenY"), sint("pickScreenX"), ys[0], ys[1], ys[2], xs[0], xs[1], xs[2]));
    }

    // ================= drawScene: scene setup =================

    private static final Class<?>[] M313 = {int.class, int.class, int.class, int.class, int.class, int.class};

    private static void draw(WorldController wc, int cx, int cy, int yaw, int cz, int plane, int pitch) throws Exception {
        wc.drawScene(cx, cy, yaw, cz, plane, pitch);
    }

    @Test public void method313ClampsCameraToMapStoresTrigAndBoundsAndFinishesPickRequest() throws Exception {
        WorldController wc = scene();
        WorldController.drawDistance = 5;
        sset("tileVisibilityTable", new boolean[8][32][256][256]);
        wc.requestTilePick(1, 2);
        int frame = sint("drawCycle");
        draw(wc, 40 * 128 + 17, 60 * 128 + 99, 640, 250, 2, 160);
        assertEquals(frame + 1, sint("drawCycle"));
        assertEquals(40 * 128 + 17, sint("cameraWorldX")); assertEquals(250, sint("cameraWorldHeight")); assertEquals(60 * 128 + 99, sint("cameraWorldY"));
        assertEquals(40, sint("cameraTileX")); assertEquals(60, sint("cameraTileY"));
        assertEquals(2, sint("cameraPlane"));
        assertEquals(Model.sineTable16[160], sint("pitchSine")); assertEquals(Model.cosineTable16[160], sint("pitchCosine"));
        assertEquals(Model.sineTable16[640], sint("yawSine")); assertEquals(Model.cosineTable16[640], sint("yawCosine"));
        assertSame(table()[(160 - 128) / 32][640 / 64], sget("cameraTileVisibility"));
        assertEquals(35, sint("drawMinTileX")); assertEquals(45, sint("drawMaxTileX"));
        assertEquals(55, sint("drawMinTileY")); assertEquals(65, sint("drawMaxTileY"));
        assertFalse((Boolean) sget("tilePickRequested"));
        // clamps: below zero -> 0; at or beyond the far edge -> size*128-1; window clipped to the map
        draw(wc, -5, -9, 0, 0, 0, 128);
        assertEquals(0, sint("cameraWorldX")); assertEquals(0, sint("cameraWorldY"));
        assertEquals(0, sint("cameraTileX")); assertEquals(0, sint("drawMinTileX")); assertEquals(5, sint("drawMaxTileX"));
        draw(wc, SIZE * 128 + 500, SIZE * 128, 0, 0, 0, 128);
        assertEquals(SIZE * 128 - 1, sint("cameraWorldX")); assertEquals(SIZE * 128 - 1, sint("cameraWorldY"));
        assertEquals(SIZE - 1, sint("cameraTileX")); assertEquals(SIZE, sint("drawMaxTileX")); assertEquals(SIZE, sint("drawMaxTileY"));
        assertEquals(SIZE - 1 - 5, sint("drawMinTileX"));
    }

    private static Ground marked(WorldController wc, int plane, int x, int y, int topPlane) throws Exception {
        Ground g = tile(wc, plane, x, y);
        g.minCameraPlane = topPlane; g.objectsPending = true; g.cornerWallEdgeMask = 0;
        g.firstPassPending = true; g.drawPending = true;
        return g;
    }

    @Test public void method313MarksTilesVisibleByTableOrHeightAndHidesTilesAbovePlaneOrOutOfView() throws Exception {
        WorldController wc = scene();
        WorldController.drawDistance = 5;
        boolean[][][][] t = new boolean[8][32][256][256];
        sset("tileVisibilityTable", t);
        int cx = 40, cy = 60, plane = 2;
        boolean[][] row = t[(160 - 128) / 32][640 / 64];
        int[][][] h = heights(wc);
        Ground a = marked(wc, 0, cx + 3, cy + 1, 0);  row[3 + 5][1 + 5] = true;      // in the table, top plane below drawn plane
        Ground b = marked(wc, 1, cx + 3, cy + 2, 3);  row[3 + 5][2 + 5] = true;      // in the table but its plane value 3 > 2
        Ground bq = marked(wc, 1, cx + 4, cy + 2, 2); row[4 + 5][2 + 5] = true;      // equal to the drawn plane stays
        Ground c = marked(wc, 0, cx + 2, cy + 1, 0);                                 // not in table, terrain at camera height
        Ground d = marked(wc, 0, cx + 1, cy, 0);  h[0][cx + 1][cy] = 200;            // not in table, terrain 200 above camera
        Ground e = marked(wc, 0, cx + 1, cy + 1, 0); h[0][cx + 1][cy + 1] = 49;      // 49 above camera: still hidden
        Ground f = marked(wc, 0, cx, cy + 1, 0);  h[0][cx][cy + 1] = 50;             // 50 above camera: visible
        b.cornerWallEdgeMask = 7; c.cornerWallEdgeMask = 7; e.cornerWallEdgeMask = 7;
        draw(wc, cx * 128 + 10, cy * 128 + 10, 640, 0, plane, 160);
        for (Ground g : new Ground[] {a, bq, d, f}) assertFalse("visible tile gets its object flag recomputed", g.objectsPending);
        for (Ground g : new Ground[] {b, c, e}) {
            assertTrue("hidden tile keeps flag", g.objectsPending);
            assertEquals(0, g.cornerWallEdgeMask); assertFalse(g.firstPassPending); assertFalse(g.drawPending);
        }
    }

    @Test public void method313DrawsVisibleTilesInAllFourQuadrantsWithinDrawDistanceAndLeavesOutsideTilesAlone() throws Exception {
        WorldController wc = scene();
        int dd = 5;
        WorldController.drawDistance = dd;
        boolean[][][][] t = new boolean[8][32][256][256];
        sset("tileVisibilityTable", t);
        int cx = 40, cy = 60;
        boolean[][] row = t[(160 - 128) / 32][640 / 64];
        int[][] offs = {{-2, -2}, {-2, 2}, {2, -2}, {2, 2}, {0, 0}, {-dd, -dd}};
        Ground[] visible = new Ground[offs.length];
        for (int i = 0; i < offs.length; i++) {
            visible[i] = marked(wc, 0, cx + offs[i][0], cy + offs[i][1], 0);
            row[offs[i][0] + dd][offs[i][1] + dd] = true;
        }
        Ground outside = marked(wc, 0, cx + dd + 1, cy, 0);           // beyond the window [cx-dd, cx+dd): never touched
        Ground edge = marked(wc, 0, cx + dd - 1, cy, 0);              // last tile inside the window
        row[2 * dd - 1][dd] = true;
        draw(wc, cx * 128 + 10, cy * 128 + 10, 640, 0, 0, 160);
        for (int i = 0; i < offs.length; i++) { assertFalse("flag " + i, visible[i].objectsPending); assertFalse("drawn " + i, visible[i].firstPassPending); }
        assertFalse(edge.objectsPending); assertFalse(edge.firstPassPending);
        assertTrue(outside.objectsPending); assertTrue(outside.firstPassPending); assertEquals(0, outside.cornerWallEdgeMask);
        assertEquals(0, sint("pendingTileCount"));
    }
}
