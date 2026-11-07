import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Terrain noise / lighting helpers of ObjectManager (method170..method187), the corner-height locals of
 * method175/method188 and the buildModel locals plus ObjectDef.aBoolean769/aBoolean751. Original member names only.
 */
public class ObjectTerrainTest {
    private ObjectDef[] savedCache;
    private boolean savedHideRoofs, savedManagerLowMem, savedDefLowMem;
    private Animation[] savedAnims;

    private static Field field(Class<?> type, String name) throws Exception {
        Field f = type.getDeclaredField(name);
        f.setAccessible(true);
        return f;
    }

    private static sun.misc.Unsafe unsafe() throws Exception {
        return (sun.misc.Unsafe) field(sun.misc.Unsafe.class, "theUnsafe").get(null);
    }

    @Before public void setUp() throws Exception {
        savedCache = (ObjectDef[]) field(ObjectDef.class, "cache").get(null);
        savedAnims = Animation.anims;
        savedHideRoofs = ObjectManager.hideRoofs;
        savedManagerLowMem = ObjectManager.lowMem;
        savedDefLowMem = ObjectDef.lowMem;
        ObjectManager.lowMem = false;
        ObjectDef.lowMem = false;
        ObjectManager.hideRoofs = false;
        Animation[] anims = new Animation[6];
        Animation a = (Animation) unsafe().allocateInstance(Animation.class);
        a.loopBackFrames = -1;
        a.frameCount = 1;
        anims[3] = a;
        Animation.anims = anims;
    }

    @After public void tearDown() throws Exception {
        field(ObjectDef.class, "cache").set(null, savedCache);
        Animation.anims = savedAnims;
        ObjectManager.hideRoofs = savedHideRoofs;
        ObjectManager.lowMem = savedManagerLowMem;
        ObjectDef.lowMem = savedDefLowMem;
    }

    // ------------------------------------------------ reflection helpers (one place per original method name)

    private static Object call(String name, Object target, Class<?>[] types, Object... args) throws Exception {
        Method m = ObjectManager.class.getDeclaredMethod(name, types);
        m.setAccessible(true);
        return m.invoke(target, args);
    }

    private static final Class<?>[] II = {int.class, int.class};
    private static final Class<?>[] III = {int.class, int.class, int.class};
    private static final Class<?>[] IIII = {int.class, int.class, int.class, int.class};

    private static int m170(int i, int j) throws Exception { return (Integer) call("hashNoise", null, II, i, j); }
    private static int m172(int i, int j) throws Exception { return (Integer) call("proceduralHeight", null, II, i, j); }
    private static int m176(int i, int j, int k) throws Exception { return (Integer) call("interpolatedNoise", null, III, i, j, k); }
    private static int m177(ObjectManager m, int i, int j, int k) throws Exception { return (Integer) call("packHsl", m, III, i, j, k); }
    private static int m182(ObjectManager m, int i, int j, int k) throws Exception { return (Integer) call("getEffectivePlane", m, III, i, j, k); }
    private static int m184(int i, int j, int k, int l) throws Exception { return (Integer) call("cosineInterpolate", null, IIII, i, j, k, l); }
    private static int m185(ObjectManager m, int i, int j) throws Exception { return (Integer) call("shadeHslOrGrey", m, II, i, j); }
    private static int m186(int i, int j) throws Exception { return (Integer) call("smoothedNoise", null, II, i, j); }
    private static int m187(int i, int j) throws Exception { return (Integer) call("shadeHsl", null, II, i, j); }

    private static ObjectManager manager() {
        return new ObjectManager(new byte[4][105][105], new int[4][105][105]);
    }

    // ------------------------------------------------ method170 hash noise

    @Test public void method170IsADeterministicByteValuedHashOfXPlus57Y() throws Exception {
        Set<Integer> seen = new HashSet<Integer>();
        boolean asymmetric = false;
        for (int x = -30; x < 70; x++) {
            for (int y = -30; y < 70; y++) {
                int v = m170(x, y);
                assertTrue(v >= 0 && v <= 255);
                assertEquals(v, m170(x, y));
                assertEquals("depends only on x + 57*y", v, m170(x + 57, y - 1));
                seen.add(v);
                if (v != m170(y, x)) asymmetric = true;
            }
        }
        assertTrue("many distinct values", seen.size() > 200);
        assertTrue("x and y are not interchangeable", asymmetric);
    }

    // ------------------------------------------------ method186 smoothed noise

    @Test public void method186WeightsCentreQuarterEdgesEighthCornersSixteenth() throws Exception {
        for (int x = -5; x < 40; x += 3) {
            for (int y = -5; y < 40; y += 2) {
                int corners = m170(x - 1, y - 1) + m170(x + 1, y - 1) + m170(x - 1, y + 1) + m170(x + 1, y + 1);
                int edges = m170(x - 1, y) + m170(x + 1, y) + m170(x, y - 1) + m170(x, y + 1);
                int centre = m170(x, y);
                int v = m186(x, y);
                assertEquals(corners / 16 + edges / 8 + centre / 4, v);
                assertTrue(v >= 0 && v <= 255);
            }
        }
        boolean differsFromCentre = false;
        for (int x = 0; x < 30; x++) if (m186(x, 3) != m170(x, 3)) differsFromCentre = true;
        assertTrue(differsFromCentre);
    }

    // ------------------------------------------------ method184 cosine interpolation

    @Test public void method184BlendsFromFirstToSecondValueAlongThePositionOverPeriod() throws Exception {
        assertEquals("position 0 gives the first value", 100, m184(100, 200, 0, 8));
        assertTrue("equal ends stay put (fixed-point rounding)", Math.abs(m184(70, 70, 3, 8) - 70) <= 1);
        int mid = m184(100, 200, 4, 8);
        assertTrue("half way gives the mean: " + mid, Math.abs(mid - 150) <= 1);
        int previous = 99;
        for (int pos = 0; pos < 8; pos++) {
            int v = m184(100, 200, pos, 8);
            assertTrue("rising from first to second", v >= previous);
            assertTrue(v >= 100 && v <= 200);
            previous = v;
        }
        assertTrue("last step is near the second value", m184(100, 200, 7, 8) > 190);
        previous = 201;
        for (int pos = 0; pos < 8; pos++) {
            int v = m184(200, 100, pos, 8);
            assertTrue("falling when the first is larger", v <= previous);
            previous = v;
        }
        assertEquals("only position/period matters", m184(10, 90, 1, 4), m184(10, 90, 2, 8));
        assertTrue("cosine ease: quarter way is well below the linear quarter", m184(0, 1000, 1, 4) < 250);
        assertTrue("and three quarters is above", m184(0, 1000, 3, 4) > 750);
    }

    // ------------------------------------------------ method176 interpolated noise

    @Test public void method176InterpolatesTheSmoothedNoiseOfTheFourSurroundingCells() throws Exception {
        int period = 4;
        int cellX = 6, cellY = 9;
        int x0 = cellX * period, y0 = cellY * period;
        assertEquals("on a lattice point it is the smoothed noise of that cell", m186(cellX, cellY), m176(x0, y0, period));
        for (int off = 1; off < period; off++) {
            assertEquals("x offset blends cell and cell x+1 along the row",
                    m184(m186(cellX, cellY), m186(cellX + 1, cellY), off, period), m176(x0 + off, y0, period));
            assertEquals("y offset blends cell and cell y+1 along the column",
                    m184(m186(cellX, cellY), m186(cellX, cellY + 1), off, period), m176(x0, y0 + off, period));
        }
        for (int ox = 1; ox < period; ox++) {
            for (int oy = 1; oy < period; oy++) {
                int row0 = m184(m186(cellX, cellY), m186(cellX + 1, cellY), ox, period);
                int row1 = m184(m186(cellX, cellY + 1), m186(cellX + 1, cellY + 1), ox, period);
                assertEquals(m184(row0, row1, oy, period), m176(x0 + ox, y0 + oy, period));
            }
        }
        assertEquals("period 1 is the smoothed noise itself", m186(13, -4), m176(13, -4, 1));
        assertEquals("period 2: cell = coordinate / 2", m186(5, 7), m176(10, 14, 2));
    }

    // ------------------------------------------------ method172 procedural terrain height

    @Test public void method172SumsThreeNoiseOctavesScaledAndClampedToTenToSixty() throws Exception {
        Set<Integer> seen = new HashSet<Integer>();
        for (int x = 0; x < 60; x += 3) {
            for (int y = 0; y < 60; y += 3) {
                int v = m172(x, y);
                assertTrue(v >= 10 && v <= 60);
                assertEquals(v, m172(x, y));
                int k = m176(x + 45365, y + 0x16713, 4) - 128 + (m176(x + 10294, y + 37821, 2) - 128 >> 1) + (m176(x, y, 1) - 128 >> 2);
                int expected = (int) (k * 0.3) + 35;
                expected = Math.max(10, Math.min(60, expected));
                assertEquals(expected, v);
                seen.add(v);
            }
        }
        assertTrue("varies", seen.size() > 5);
    }

    // ------------------------------------------------ method177 / method185 / method187 lighting colours

    @Test public void method177PacksHueSaturationAndLightnessIntoTenSevenAndSevenBits() throws Exception {
        ObjectManager m = manager();
        assertEquals(0, m177(m, 0, 0, 0));
        assertEquals("hue/4 in bits 10+", 5 << 10, m177(m, 20, 0, 0));
        assertEquals("hue 3 rounds to 0", 0, m177(m, 3, 0, 0));
        assertEquals("saturation/32 in bits 7..9", 3 << 7, m177(m, 0, 100, 0));
        assertEquals("lightness/2 in bits 0..6", 50, m177(m, 0, 0, 100));
        assertEquals((5 << 10) + (3 << 7) + 50, m177(m, 20, 100, 100));
        // saturation is halved once per lightness threshold crossed (strictly above 179, 192, 217, 243)
        assertEquals(224 / 32 << 7, m177(m, 0, 224, 179) & 0x380);
        assertEquals(112 / 32 << 7, m177(m, 0, 224, 180) & 0x380);
        assertEquals(224 / 2 / 32 << 7, m177(m, 0, 224, 192) & 0x380);
        assertEquals(56 / 32 << 7, m177(m, 0, 224, 193) & 0x380);
        assertEquals(56 / 32 << 7, m177(m, 0, 224, 217) & 0x380);
        assertEquals(28 / 32 << 7, m177(m, 0, 224, 218) & 0x380);
        assertEquals(28 / 32 << 7, m177(m, 0, 224, 243) & 0x380);
        assertEquals(14 / 32 << 7, m177(m, 0, 224, 244) & 0x380);
        assertEquals("lightness itself is not reduced", 244 / 2, m177(m, 0, 224, 244) & 0x7f);
    }

    @Test public void method187ScalesTheLightnessBitsOfAHslColourByTheSecondArgument() throws Exception {
        int hsl = (7 << 7) + 100;
        assertEquals("-1 means no colour", 0xbc614e, m187(-1, 50));
        assertEquals("lightness 100 * 64 / 128 = 50 keeps hue bits", (7 << 7) + 50, m187(hsl, 64));
        assertEquals("100 * 127 / 128 = 99", (7 << 7) + 99, m187(hsl, 127));
        assertEquals("never below 2", (7 << 7) + 2, m187(hsl, 0));
        assertEquals("never above 126", (7 << 7) + 126, m187(hsl, 1000));
        assertEquals("hue and saturation bits are untouched", 7 << 7, m187(hsl, 64) & 0xff80);
        assertEquals("a different colour keeps its own bits", (300 << 7) + 2, m187((300 << 7) + 1, 64));
    }

    @Test public void method185AddsTheNegativeSentinelCasesToMethod187() throws Exception {
        ObjectManager m = manager();
        int hsl = (7 << 7) + 100;
        for (int light : new int[] {0, 1, 64, 127, 1000}) {
            assertEquals("ordinary colours shade like method187", m187(hsl, light), m185(m, hsl, light));
        }
        assertEquals("-2 means no colour", 0xbc614e, m185(m, -2, 64));
        assertEquals("-1 is a grey ramp: 127 - light", 127 - 30, m185(m, -1, 30));
        assertEquals(127, m185(m, -1, -50));
        assertEquals(0, m185(m, -1, 500));
        assertNotEquals("-1 is not the no-colour value here", 0xbc614e, m185(m, -1, 30));
    }

    // ------------------------------------------------ method182 effective plane

    @Test public void method182ReturnsPlaneMinusOneUnderABridgeAndZeroForFlagEight() throws Exception {
        byte[][][] flags = new byte[4][105][105];
        ObjectManager m = new ObjectManager(flags, new int[4][105][105]);
        int x = 20, y = 30;
        for (int plane = 0; plane < 4; plane++) {
            assertEquals("plain tile keeps the plane", plane, m182(m, y, plane, x));
        }
        flags[1][x][y] = 2;
        assertEquals("plane 0 unaffected", 0, m182(m, y, 0, x));
        assertEquals(0, m182(m, y, 1, x));
        assertEquals(1, m182(m, y, 2, x));
        assertEquals(2, m182(m, y, 3, x));
        assertEquals("other tile unaffected", 3, m182(m, y, 3, x + 1));
        assertEquals("x and y are the third and first arguments", 3, m182(m, x, 3, y));
        flags[2][x][y] = 8;
        assertEquals("flag 8 forces plane 0", 0, m182(m, y, 2, x));
        assertEquals("only on that plane", 2, m182(m, y, 3, x));
        flags[3][x][y] = 8;
        assertEquals(0, m182(m, y, 3, x));
    }

    // ------------------------------------------------ corner height locals of method175 / method188

    private static ObjectDef animated(int type) throws Exception {
        Constructor<ObjectDef> constructor = ObjectDef.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        ObjectDef def = constructor.newInstance();
        Method defaults = ObjectDef.class.getDeclaredMethod("setDefaults");
        defaults.setAccessible(true);
        defaults.invoke(def);
        def.type = type;
        def.animationId = 3;
        def.blocksMovement = false;
        return def;
    }

    private static void install(ObjectDef def) throws Exception {
        ObjectDef[] cache = new ObjectDef[20];
        for (int n = 0; n < 20; n++) {
            if (n == 0) {
                cache[n] = def;
            } else {
                Constructor<ObjectDef> c = ObjectDef.class.getDeclaredConstructor();
                c.setAccessible(true);
                ObjectDef other = c.newInstance();
                other.type = -1;
                cache[n] = other;
            }
        }
        field(ObjectDef.class, "cache").set(null, cache);
    }

    private static int[] animatedCorners(WorldController w, int x, int y) throws Exception {
        Ground[][][] g = (Ground[][][]) field(WorldController.class, "groundArray").get(w);
        Object animable = g[0][x][y].obj5Array[0].model;
        int[] out = new int[4];
        for (int n = 0; n < 4; n++) {
            out[n] = field(animable.getClass(), new String[] {"heightX0Y0", "heightX1Y0", "heightX1Y1", "heightX0Y1"}[n]).getInt(animable);
        }
        return out;
    }

    private static int[][][] distinctHeights(int x, int y) {
        int[][][] ground = new int[4][105][105];
        ground[0][x][y] = 10;
        ground[0][x + 1][y] = 20;
        ground[0][x + 1][y + 1] = 30;
        ground[0][x][y + 1] = 40;
        return ground;
    }

    @Test public void method175FourCornerLocalsAreTheHeightsAtX0Y0X1Y0X1Y1AndX0Y1() throws Exception {
        install(animated(604));
        int x = 20, y = 30;
        int[][][] ground = distinctHeights(x, y);
        WorldController w = new WorldController(new int[4][105][105]);
        ObjectManager m = new ObjectManager(new byte[4][105][105], ground);
        Method method = ObjectManager.class.getDeclaredMethod("placeObject", int.class, WorldController.class,
                CollisionMap.class, int.class, int.class, int.class, int.class, int.class);
        method.setAccessible(true);
        method.invoke(m, y, w, null, 10, 0, x, 604, 0);
        // AnimatedObject stores them as (x0y0, x1y0, x1y1, x0y1) = heightX0Y0..heightX0Y1, the getModel parameter order
        assertArrayEquals(new int[] {10, 20, 30, 40}, animatedCorners(w, x, y));
    }

    @Test public void method188FourCornerLocalsAreTheHeightsAtX0Y0X1Y0X1Y1AndX0Y1() throws Exception {
        install(animated(604));
        int x = 20, y = 30;
        int[][][] ground = distinctHeights(x, y);
        WorldController w = new WorldController(new int[4][105][105]);
        ObjectManager.placeObjectWithoutShading(w, 0, y, 10, 0, new CollisionMap(), ground, x, 604, 0);
        assertArrayEquals(new int[] {10, 20, 30, 40}, animatedCorners(w, x, y));
    }

    // ------------------------------------------------ buildModel locals and ObjectDef.aBoolean769 / aBoolean751

    private static Model triangle(int xOffset) throws Exception {
        Constructor<Model> c = Model.class.getDeclaredConstructor();
        c.setAccessible(true);
        Model m = c.newInstance();
        m.vertexCount = 3;
        m.vertexX = new int[] {xOffset, xOffset + 64, xOffset};
        m.vertexY = new int[] {0, 0, 40};
        m.vertexZ = new int[] {0, 0, 0};
        m.faceCount = 1;
        m.faceVertexA = new int[] {0};
        m.faceVertexB = new int[] {1};
        m.faceVertexC = new int[] {2};
        m.faceColors = new int[] {1234};
        return m;
    }

    private static ObjectDef withModel(int type, int modelId) throws Exception {
        ObjectDef d = animated(type);
        field(ObjectDef.class, "modelIds").set(d, new int[] {modelId});
        return d;
    }

    private static Model build(ObjectDef d, int shape, int frame, int orientation) throws Exception {
        Method m = ObjectDef.class.getDeclaredMethod("buildModel", int.class, int.class, int.class);
        m.setAccessible(true);
        return (Model) m.invoke(d, shape, frame, orientation);
    }

    private static int[] shade(Model m) throws Exception {
        return (int[]) field(Model.class, "faceShadeA").get(m);
    }

    private static int maxAbsXZ(Model m) {
        int max = 0;
        for (int v = 0; v < m.vertexCount; v++) {
            max = Math.max(max, Math.max(Math.abs(m.vertexX[v]), Math.abs(m.vertexZ[v])));
        }
        return max;
    }

    @Test public void aBoolean751XorOrientationAbove3SelectsTheMirroredModelVariant() throws Exception {
        for (boolean flag : new boolean[] {false, true}) {
            for (int orientation = 0; orientation < 8; orientation++) {
                int id = 7000 + (flag ? 100 : 0) + orientation;
                ObjectDef d = withModel(9100 + id, id);
                field(ObjectDef.class, "mirrored").setBoolean(d, flag);
                // the plain variant lives at the model id, the mirrored one at id + 0x10000 (far from the origin)
                ObjectDef.mruNodes1.removeFromCache(triangle(0), id);
                ObjectDef.mruNodes1.removeFromCache(triangle(500), id + 0x10000);
                Model out = build(d, 10, -1, orientation);
                assertNotNull(out);
                boolean expectMirrored = flag ^ orientation > 3;
                assertEquals("flag=" + flag + " orientation=" + orientation, expectMirrored, maxAbsXZ(out) >= 500);
            }
        }
    }

    @Test public void buildModelSharesVerticesUnlessScaledTranslatedRotatedOrAnimated() throws Exception {
        String[] scaled = {"modelScaleX", "modelScaleY", "modelScaleZ"};
        String[] translated = {"modelOffsetX", "modelOffsetY", "modelOffsetZ"};
        int id = 7300;
        Model source = triangle(0);
        ObjectDef.mruNodes1.removeFromCache(source, id);
        assertSame("nothing to change: vertex array shared", source.vertexX,
                build(withModel(9400, id), 10, -1, 0).vertexX);
        int n = 1;
        for (String f : scaled) {
            ObjectDef d = withModel(9400 + n++, id);
            field(ObjectDef.class, f).setInt(d, 256);
            assertNotSame(f + " forces a private vertex copy", source.vertexX, build(d, 10, -1, 0).vertexX);
        }
        for (String f : translated) {
            ObjectDef d = withModel(9400 + n++, id);
            field(ObjectDef.class, f).setInt(d, 5);
            assertNotSame(f + " forces a private vertex copy", source.vertexX, build(d, 10, -1, 0).vertexX);
        }
        assertNotSame("orientation forces a copy", source.vertexX, build(withModel(9400 + n++, id), 10, -1, 1).vertexX);
        // scale(anInt748 -> x), then translate(anInt738 -> x, anInt745 -> y, anInt783 -> z)
        ObjectDef both = withModel(9400 + n, id);
        field(ObjectDef.class, "modelScaleX").setInt(both, 256);
        field(ObjectDef.class, "modelOffsetX").setInt(both, 3);
        field(ObjectDef.class, "modelOffsetY").setInt(both, 4);
        field(ObjectDef.class, "modelOffsetZ").setInt(both, 5);
        Model out = build(both, 10, -1, 0);
        assertArrayEquals(new int[] {3, 64 * 2 + 3, 3}, out.vertexX);
        assertArrayEquals(new int[] {4, 4, 44}, out.vertexY);
        assertArrayEquals(new int[] {5, 5, 5}, out.vertexZ);
    }

    @Test public void aBoolean769KeepsVertexNormalsInsteadOfBakingVertexShading() throws Exception {
        for (boolean flag : new boolean[] {false, true}) {
            int id = 7500 + (flag ? 1 : 0);
            ObjectDef d = withModel(9500 + id, id);
            field(ObjectDef.class, "keepsVertexNormals").setBoolean(d, flag);
            ObjectDef.mruNodes1.removeFromCache(triangle(0), id);
            Model out = build(d, 10, -1, 0);
            assertNotNull(shade(out));
            if (flag) {
                assertNotNull("normals are kept for later lighting", out.originalVertexNormals);
            } else {
                assertNull("normals are consumed into per-vertex shading", out.originalVertexNormals);
            }
        }
    }

    @Test public void aBoolean769MakesGetModelCopyTheLightingArrays() throws Exception {
        for (boolean flag : new boolean[] {false, true}) {
            int id = 7600 + (flag ? 1 : 0);
            ObjectDef d = withModel(9600 + id, id);
            d.adjustsToTerrain = true;
            field(ObjectDef.class, "keepsVertexNormals").setBoolean(d, flag);
            ObjectDef.mruNodes1.removeFromCache(triangle(0), id);
            Model built = build(d, 10, -1, 0);
            Model out = d.getModel(10, 0, 0, 0, 0, 0, -1);
            assertNotSame(built, out);
            if (flag) {
                assertNotSame(shade(built), shade(out));
                assertArrayEquals(shade(built), shade(out));
            } else {
                assertSame(shade(built), shade(out));
            }
        }
    }

    // ------------------------------------------------ pass 149: ObjectDef scale/offset/lighting/item-stack members (original names only)

    private static Model triangle3() throws Exception {
        Model m = triangle(0);
        m.vertexX = new int[] {10, 74, 10};
        m.vertexY = new int[] {-20, -20, -60};
        m.vertexZ = new int[] {30, 30, 90};
        return m;
    }

    private static ObjectDef decodeObject(boolean solid, int... bytes) throws Exception {
        ObjectDef d = animated(9900);
        d.blocksMovement = solid;
        d.blocksProjectiles = solid;
        byte[] data = new byte[bytes.length];
        for (int n = 0; n < bytes.length; n++) data[n] = (byte) bytes[n];
        Stream stream = new Stream(data);
        Method read = ObjectDef.class.getDeclaredMethod("readValues", Stream.class);
        read.setAccessible(true);
        read.invoke(d, stream);
        assertEquals(data.length, stream.currentOffset);
        return d;
    }

    private static int[][] axes(Model m) {
        return new int[][] {m.vertexX.clone(), m.vertexY.clone(), m.vertexZ.clone()};
    }

    @Test public void scaleFieldsScaleExactlyOneAxisEach() throws Exception {
        String[] names = {"modelScaleX", "modelScaleY", "modelScaleZ"}; // expected axis x, y, z
        for (int axis = 0; axis < 3; axis++) {
            for (int scale : new int[] {64, 256, 0}) {
                int id = 41000 + axis * 10 + scale % 7;
                ObjectDef d = withModel(9800 + id, id);
                field(ObjectDef.class, names[axis]).setInt(d, scale);
                ObjectDef.mruNodes1.removeFromCache(triangle3(), id);
                int[][] before = axes(triangle3());
                int[][] after = axes(build(d, 10, -1, 0));
                for (int a = 0; a < 3; a++) {
                    for (int v = 0; v < 3; v++) {
                        int expect = a == axis ? before[a][v] * scale / 128 : before[a][v];
                        assertEquals(names[axis] + "=" + scale + " axis " + a + " vertex " + v, expect, after[a][v]);
                    }
                }
            }
        }
    }

    @Test public void offsetFieldsTranslateExactlyOneAxisEach() throws Exception {
        String[] names = {"modelOffsetX", "modelOffsetY", "modelOffsetZ"}; // expected axis x, y, z
        for (int axis = 0; axis < 3; axis++) {
            for (int delta : new int[] {7, -7}) {
                int id = 41100 + axis * 10 + (delta > 0 ? 1 : 2);
                ObjectDef d = withModel(9900 + id, id);
                field(ObjectDef.class, names[axis]).setInt(d, delta);
                ObjectDef.mruNodes1.removeFromCache(triangle3(), id);
                int[][] before = axes(triangle3());
                int[][] after = axes(build(d, 10, -1, 0));
                for (int a = 0; a < 3; a++) {
                    for (int v = 0; v < 3; v++) {
                        assertEquals(names[axis] + "=" + delta + " axis " + a, before[a][v] + (a == axis ? delta : 0), after[a][v]);
                    }
                }
            }
        }
    }

    @Test public void scaleAndOffsetOpcodesDecodeToTheirFields() throws Exception {
        // 65/66/67 unsigned shorts; 70/71/72 signed shorts; defaults 128 and 0
        ObjectDef def = animated(9990);
        assertEquals(128, field(ObjectDef.class, "modelScaleX").getInt(def));
        assertEquals(128, field(ObjectDef.class, "modelScaleY").getInt(def));
        assertEquals(128, field(ObjectDef.class, "modelScaleZ").getInt(def));
        assertEquals(0, field(ObjectDef.class, "modelOffsetX").getInt(def));
        ObjectDef d = decodeObject(true, 65, 0x01, 0x00, 66, 0x02, 0x00, 67, 0x03, 0x00, 70, 0xFF, 0xFE, 71, 0x00, 0x05, 72, 0xFF, 0xF0, 0);
        assertEquals(256, field(ObjectDef.class, "modelScaleX").getInt(d));
        assertEquals(512, field(ObjectDef.class, "modelScaleY").getInt(d));
        assertEquals(768, field(ObjectDef.class, "modelScaleZ").getInt(d));
        assertEquals(-2, field(ObjectDef.class, "modelOffsetX").getInt(d));
        assertEquals(5, field(ObjectDef.class, "modelOffsetY").getInt(d));
        assertEquals(-16, field(ObjectDef.class, "modelOffsetZ").getInt(d));
    }

    @Test public void lightingBytesAreTheAmbientAndDivisorOffsetsOfApplyLighting() throws Exception {
        int[][] cases = {{0, 0}, {10, 0}, {-10, 0}, {0, 10}, {0, -10}, {20, -6}};
        Set<String> distinct = new HashSet<String>();
        for (int c = 0; c < cases.length; c++) {
            int id = 41200 + c;
            ObjectDef d = withModel(10000 + id, id);
            field(ObjectDef.class, "ambientLightOffset").setByte(d, (byte) cases[c][0]);
            field(ObjectDef.class, "lightDivisorSteps").setByte(d, (byte) cases[c][1]);
            ObjectDef.mruNodes1.removeFromCache(triangle3(), id);
            Model out = build(d, 10, -1, 0);
            Model copy = new Model(true, true, false, triangle3());
            copy.applyLighting(64 + cases[c][0], 768 + cases[c][1] * 5, -50, -10, -50, true);
            assertArrayEquals("case " + c, shade(copy), shade(out));
            distinct.add(java.util.Arrays.toString(shade(out)));
        }
        assertTrue("different offsets change the lighting", distinct.size() >= 4);
        // opcode 29 -> ambient byte, opcode 39 -> divisor byte (both signed)
        ObjectDef d = decodeObject(true, 29, 0xFB, 39, 0x07, 0);
        assertEquals(-5, field(ObjectDef.class, "ambientLightOffset").getByte(d));
        assertEquals(7, field(ObjectDef.class, "lightDivisorSteps").getByte(d));
    }

    @Test public void anInt760SetsTheBuiltModelsItemStackHeightToItsHeightWhenOne() throws Exception {
        for (int value : new int[] {1, 0, 2}) {
            int id = 41300 + value;
            ObjectDef d = withModel(10100 + id, id);
            field(ObjectDef.class, "raisesItemStacks").setInt(d, value);
            ObjectDef.mruNodes1.removeFromCache(triangle3(), id);
            Model out = build(d, 10, -1, 0);
            assertEquals(60, out.modelHeight);
            assertEquals("anInt760=" + value, value == 1 ? 60 : 0, out.stackSurfaceHeight);
        }
    }

    @Test public void anInt760DefaultsFromMovementBlockingUnlessOpcode75GivesIt() throws Exception {
        assertEquals(-1, field(ObjectDef.class, "raisesItemStacks").getInt(animated(9991)));
        assertEquals(1, field(ObjectDef.class, "raisesItemStacks").getInt(decodeObject(true, 0)));
        assertEquals(0, field(ObjectDef.class, "raisesItemStacks").getInt(decodeObject(false, 17, 0)));
        assertEquals("explicit value wins over a solid definition", 0, field(ObjectDef.class, "raisesItemStacks").getInt(decodeObject(true, 75, 0, 0)));
        assertEquals("explicit value wins over a passable definition", 1, field(ObjectDef.class, "raisesItemStacks").getInt(decodeObject(false, 17, 75, 1, 0)));
    }

    @Test public void opcode74ForcesTheDefinitionNonSolidWhateverElseSetsIt() throws Exception {
        assertFalse(field(ObjectDef.class, "forcesNonSolid").getBoolean(animated(9996)));
        ObjectDef without = decodeObject(true, 0);
        assertTrue(without.blocksMovement && without.blocksProjectiles);
        ObjectDef with = decodeObject(true, 74, 0);
        assertTrue(field(ObjectDef.class, "forcesNonSolid").getBoolean(with));
        assertFalse(with.blocksMovement);
        assertFalse(with.blocksProjectiles);
        assertEquals("the item stack default then sees a non-solid definition", 0, field(ObjectDef.class, "raisesItemStacks").getInt(with));
        ObjectDef set = decodeObject(true, 74, 75, 1, 0);
        assertFalse(set.blocksMovement);
        assertEquals(1, field(ObjectDef.class, "raisesItemStacks").getInt(set));
    }
}
