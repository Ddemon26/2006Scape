import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** Controlled original-name observations of Model.method479/method480/method482 and the modelIntArray tables. */
public class ModelLightingProjectionTest {
    private static final String[] STATICS = {"anIntArray1665", "anIntArray1666", "anIntArray1667", "anIntArray1668",
            "anIntArray1669", "anIntArray1670"};
    private final Object[] saved = new Object[STATICS.length];
    private int savedC1, savedC2;

    @Before public void setup() throws Exception {
        for (int n = 0; n < STATICS.length; n++) { saved[n] = f(STATICS[n]).get(null); f(STATICS[n]).set(null, new int[64]); }
        savedC1 = Texture.textureInt1; savedC2 = Texture.textureInt2;
    }
    @After public void restore() throws Exception {
        for (int n = 0; n < STATICS.length; n++) f(STATICS[n]).set(null, saved[n]);
        Texture.textureInt1 = savedC1; Texture.textureInt2 = savedC2;
    }
    private static Field f(String name) throws Exception { Field x = Model.class.getDeclaredField(name); x.setAccessible(true); return x; }
    private static int[] ints(Model m, String n) throws Exception { return (int[]) f(n).get(m); }
    private static int[] stat(String n) throws Exception { return (int[]) f(n).get(null); }

    private static Model model(int[] x, int[] y, int[] z, int[] a, int[] b, int[] c, int[] flags, int color) throws Exception {
        Constructor<Model> k = Model.class.getDeclaredConstructor(); k.setAccessible(true);
        Model m = k.newInstance(); m.anInt1626 = x.length; m.anIntArray1627 = x; m.anIntArray1628 = y; m.anIntArray1629 = z;
        m.anInt1630 = a.length; m.anIntArray1631 = a; m.anIntArray1632 = b; m.anIntArray1633 = c; m.anIntArray1637 = flags;
        m.anIntArray1640 = new int[a.length]; java.util.Arrays.fill(m.anIntArray1640, color);
        m.minX = 12345;
        return m;
    }
    private static Model flatTriangle(int flag) throws Exception {
        return model(new int[]{0,10,0}, new int[]{0,0,10}, new int[]{0,0,0}, new int[]{0}, new int[]{1}, new int[]{2}, new int[]{flag}, 127);
    }
    private static int shade0(Model m) throws Exception { return ints(m, "anIntArray1634")[0]; }

    @Test public void tablesAreSineAndCosineOf2048UnitTurnsIn16Bit() {
        assertSame(Texture.sineTable, Model.sineTable16); assertSame(Texture.cosineTable, Model.cosineTable16);
        assertEquals(0, Model.sineTable16[0]); assertEquals(65536, Model.cosineTable16[0]);
        assertEquals(65535, Model.sineTable16[512]); assertTrue(Math.abs(Model.cosineTable16[512]) < 4);
        assertEquals(46340, Model.sineTable16[256]); assertEquals(-65535, Model.cosineTable16[1024]);
        assertTrue(Model.sineTable16[1536] < -65530);
    }

    @Test public void flatFaceShadeIsAmbientPlusLightDotNormalOverOneAndAHalfDivisor() throws Exception {
        Model m = flatTriangle(1); m.applyLighting(200, 256, 0, 0, -100, true); assertEquals(29, shade0(m));   // 200-170=30
        m = flatTriangle(1); m.applyLighting(200, 512, 0, 0, -100, true); assertEquals(114, shade0(m));       // 200-85=115
        m = flatTriangle(1); m.applyLighting(200, 256, 0, 0, -200, true); assertEquals(29, shade0(m));       // length cancels
        m = flatTriangle(1); m.applyLighting(210, 256, 0, 0, -100, true); assertEquals(39, shade0(m));       // ambient adds 1:1
        m = flatTriangle(1); m.applyLighting(200, 256, 0, 0, 100, true); assertEquals(126, shade0(m));        // facing away clamps high
        m = flatTriangle(1); m.applyLighting(200, 256, 100, 0, 0, true); assertEquals(126, shade0(m));        // perpendicular
        m = flatTriangle(1); m.applyLighting(0, 256, 0, 0, -100, true); assertEquals(2, shade0(m));          // clamps low
        m = flatTriangle(1); m.applyLighting(200, 256, 0, 0, -100, true);
        assertEquals(0, ints(m, "anIntArray1635")[0]); assertEquals(0, ints(m, "anIntArray1636")[0]);
    }
    @Test public void divisorIsOnlyUsedWhenLightIsApplied() throws Exception {
        try { Model m = flatTriangle(1); m.applyLighting(200, 0, 0, 0, -100, true); fail(); } catch (ArithmeticException expected) { }
        Model m = flatTriangle(0); m.applyLighting(200, 0, 0, 0, -100, false); // normals only; no division yet
        assertEquals(256, m.aClass33Array1425[0].anInt604);
    }
    @Test public void bitZeroOfFaceFlagSelectsFlatShadingAndOtherBitsAccumulateNormals() throws Exception {
        for (int flag : new int[]{0, 2, 4}) {
            Model m = flatTriangle(flag); m.applyLighting(200, 256, 0, 0, -100, false);
            for (int v = 0; v < 3; v++) { assertEquals(1, m.aClass33Array1425[v].anInt605); assertEquals(256, m.aClass33Array1425[v].anInt604); }
            assertEquals(0, shade0(m));
        }
        for (int flag : new int[]{1, 3}) {
            Model m = flatTriangle(flag); m.applyLighting(200, 256, 0, 0, -100, false);
            for (int v = 0; v < 3; v++) assertEquals(0, m.aClass33Array1425[v].anInt605);
            if (flag == 1) assertEquals(29, shade0(m));
        }
    }
    @Test public void faceNormalIsReducedByCommonShiftAndScaledToLength256() throws Exception {
        Model m = model(new int[]{0,20000,0}, new int[]{0,0,20000}, new int[]{0,0,20000}, new int[]{0}, new int[]{1}, new int[]{2}, null, 5);
        m.applyLighting(0, 256, 0, 0, -100, false);
        Class33 n = m.aClass33Array1425[0]; assertEquals(0, n.anInt602); assertTrue(Math.abs(n.anInt603 + 181) <= 1); assertTrue(Math.abs(n.anInt604 - 181) <= 1);
        m = model(new int[]{0,20000,0}, new int[]{0,0,20000}, new int[]{0,0,0}, new int[]{0}, new int[]{1}, new int[]{2}, null, 5);
        m.applyLighting(0, 256, 0, 0, -100, false); assertEquals(256, m.aClass33Array1425[0].anInt604);
        m = model(new int[]{0,0,0}, new int[]{0,0,0}, new int[]{0,0,0}, new int[]{0}, new int[]{1}, new int[]{2}, null, 5); // zero-area face
        m.applyLighting(0, 256, 0, 0, -100, false);
        assertEquals(0, m.aClass33Array1425[0].anInt604); assertEquals(1, m.aClass33Array1425[0].anInt605);
    }
    private static Model twoTriangles() throws Exception {
        return model(new int[]{0,10,0,0}, new int[]{0,0,10,0}, new int[]{0,0,0,10}, new int[]{0,0}, new int[]{1,2}, new int[]{2,3}, null, 127);
    }
    @Test public void sharedVertexNormalsSumAndCountAndFalseKeepsACopy() throws Exception {
        Model m = twoTriangles(); m.applyLighting(300, 256, -60, 0, -80, false);
        int[][] sum = {{256,0,256,2},{0,0,256,1},{256,0,256,2},{256,0,0,1}};
        for (int v = 0; v < 4; v++) {
            Class33 a = m.aClass33Array1425[v], b = m.aClass33Array1660[v];
            assertEquals(sum[v][0], a.anInt602); assertEquals(sum[v][1], a.anInt603); assertEquals(sum[v][2], a.anInt604); assertEquals(sum[v][3], a.anInt605);
            assertNotSame(a, b); assertEquals(a.anInt604, b.anInt604); assertEquals(a.anInt605, b.anInt605);
        }
        assertEquals(0, m.minX); // extents recalculated, not left at sentinel
        assertEquals(0, ints(m, "anIntArray1634")[0]);
    }
    @Test public void trueBakesPerVertexShadeFromNormalSumOverCountAndFreesNormals() throws Exception {
        Model m = twoTriangles(); m.applyLighting(300, 256, -60, 0, -80, true);
        assertArrayEquals(new int[]{120, 95, 120}, new int[]{ints(m, "anIntArray1634")[0], ints(m, "anIntArray1635")[0], ints(m, "anIntArray1636")[0]});
        assertArrayEquals(new int[]{120, 120, 126}, new int[]{ints(m, "anIntArray1634")[1], ints(m, "anIntArray1635")[1], ints(m, "anIntArray1636")[1]});
        assertNull(m.aClass33Array1425); assertNull(m.aClass33Array1660); assertNull(m.anIntArray1640);
        assertEquals(12345, m.minX); // rounded bounds routine leaves extents alone
    }
    @Test public void method480SkipsFlatFacesUsesDivisorDirectlyAndKeepsColoursOnlyForBitOneFaces() throws Exception {
        Model m = twoTriangles(); m.applyLighting(300, 256, -60, 0, -80, false);
        ints(m, "anIntArray1634")[1] = 777; m.anIntArray1637 = new int[]{0, 1};
        m.applyVertexLighting(300, 100, -60, 0, -80);
        assertEquals(120, ints(m, "anIntArray1634")[0]); assertEquals(777, ints(m, "anIntArray1634")[1]);
        assertNull(m.anIntArray1640);
        m = twoTriangles(); m.applyLighting(300, 256, -60, 0, -80, false);
        m.applyVertexLighting(300, 200, -60, 0, -80); // doubled divisor halves the light term: 300-89=211 -> clamp
        assertEquals(126, ints(m, "anIntArray1636")[1]);
        m = twoTriangles(); m.applyLighting(300, 256, -60, 0, -80, false); m.anIntArray1637 = new int[]{2, 0};
        m.applyVertexLighting(300, 100, -60, 0, -80); assertNotNull(m.anIntArray1640);
    }

    // ---- method482 ----
    private static final double TURN = 2 * Math.PI / 2048;
    private Model verts(int[] x, int[] y, int[] z) throws Exception {
        return model(x, y, z, new int[0], new int[0], new int[0], null, 0);
    }
    private void expect(int j, int k, int l, int tx, int ty, int tz, int vx, int vy, int vz) throws Exception {
        double x = vx, y = vy, z = vz;
        if (k != 0) { double c = Math.cos(k * TURN), s = Math.sin(k * TURN); double nx = x * c + y * s; y = y * c - x * s; x = nx; }
        if (j != 0) { double c = Math.cos(j * TURN), s = Math.sin(j * TURN); double nx = x * c + z * s; z = z * c - x * s; x = nx; }
        x += tx; y += ty; z += tz;
        double c = Math.cos(l * TURN), s = Math.sin(l * TURN);
        double ny = y * c - z * s; z = y * s + z * c; y = ny;
        double depth = z - (ty * s + tz * c);
        assertEquals(depth, stat("anIntArray1667")[0], 2.0);
        assertEquals(Texture.textureInt1 + x * 512 / z, stat("anIntArray1665")[0], 2.0);
        assertEquals(Texture.textureInt2 + y * 512 / z, stat("anIntArray1666")[0], 2.0);
    }
    @Test public void identityAndTranslationProjectAroundScreenCentreWithZDivide() throws Exception {
        Texture.textureInt1 = 160; Texture.textureInt2 = 90;
        Model m = verts(new int[]{100}, new int[]{50}, new int[]{0});
        m.projectRotated(0, 0, 0, 0, 0, 512);
        assertEquals(260, stat("anIntArray1665")[0]); assertEquals(140, stat("anIntArray1666")[0]); assertEquals(0, stat("anIntArray1667")[0]);
        m.projectRotated(0, 0, 0, 20, 0, 512); assertEquals(280, stat("anIntArray1665")[0]);        // i1 = x offset
        m.projectRotated(0, 0, 0, 0, 30, 512); assertEquals(170, stat("anIntArray1666")[0]); assertEquals(0, stat("anIntArray1667")[0]); // j1 = y
        m.projectRotated(0, 0, 0, 0, 0, 1024); assertEquals(210, stat("anIntArray1665")[0]); assertEquals(115, stat("anIntArray1666")[0]); // k1 = depth
        m = verts(new int[]{100}, new int[]{0}, new int[]{200});
        m.projectRotated(0, 0, 0, 0, 0, 512); assertEquals(200, stat("anIntArray1667")[0]); // depth is relative to the origin
        assertEquals(Texture.textureInt1 + 100 * 512 / 712, stat("anIntArray1665")[0], 1.0);
    }
    @Test public void quarterTurnsAndRotationOrder() throws Exception {
        Texture.textureInt1 = 0; Texture.textureInt2 = 0;
        Model m = verts(new int[]{100}, new int[]{0}, new int[]{0});
        m.projectRotated(0, 512, 0, 0, 0, 512); // k = roll in the x/y plane: +x -> -y
        assertEquals(0, stat("anIntArray1665")[0], 1.0); assertEquals(-100, stat("anIntArray1666")[0], 1.0);
        m.projectRotated(512, 0, 0, 0, 0, 512); // j = yaw in the x/z plane: +x -> -z
        assertEquals(0, stat("anIntArray1665")[0], 1.0); assertEquals(-100, stat("anIntArray1667")[0], 1.0);
        m.projectRotated(512, 512, 0, 0, 0, 512); // roll first, then yaw: (100,0,0)->(0,-100,0)->unchanged by yaw
        assertEquals(-100, stat("anIntArray1666")[0], 1.0); assertEquals(0, stat("anIntArray1667")[0], 1.0);
    }
    @Test public void cameraPitchAppliesAfterTranslationAndDepthIsRelativeToTranslatedOrigin() throws Exception {
        Texture.textureInt1 = 0; Texture.textureInt2 = 0;
        Model m = verts(new int[]{0}, new int[]{0}, new int[]{0});
        m.projectRotated(0, 0, 256, 0, 0, 1000);
        assertEquals(0, stat("anIntArray1667")[0], 1.0); assertEquals(-512, stat("anIntArray1666")[0], 2.0);
        for (int[] c : new int[][]{{200, 300, 100, 20, -30, 700, 50, -40, 60}, {0, 0, 128, 0, 0, 900, 10, 20, 30}, {512, 100, 300, -50, 40, 800, -70, 15, 25}}) {
            m = verts(new int[]{c[6]}, new int[]{c[7]}, new int[]{c[8]});
            m.projectRotated(c[0], c[1], c[2], c[3], c[4], c[5]); expect(c[0], c[1], c[2], c[3], c[4], c[5], c[6], c[7], c[8]);
        }
    }
    @Test public void onlyActiveVertexPrefixAndOptionalCameraSpaceCopies() throws Exception {
        Texture.textureInt1 = 0; Texture.textureInt2 = 0;
        Model m = verts(new int[]{10, 20, 30}, new int[]{0, 0, 0}, new int[]{0, 0, 0}); m.anInt1626 = 2;
        for (String n : STATICS) java.util.Arrays.fill(stat(n), -99);
        m.projectRotated(0, 0, 0, 0, 0, 512);
        assertEquals(10, stat("anIntArray1665")[0]); assertEquals(20, stat("anIntArray1665")[1]); assertEquals(-99, stat("anIntArray1665")[2]);
        assertEquals(-99, stat("anIntArray1668")[0]);
        f("anInt1642").setInt(m, 1); m.projectRotated(0, 0, 0, 0, 0, 512);
        assertEquals(10, stat("anIntArray1668")[0]); assertEquals(0, stat("anIntArray1669")[0]); assertEquals(512, stat("anIntArray1670")[0]);
    }
}
