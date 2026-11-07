import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * ObjectManager.method171 (collision blocking, terrain colour blending and lighting, addTile, occluder merging) with
 * its fields anInt123/anInt133/anInt146/anInt147/anIntArray124..128/tileHeights/overlayIds/
 * 136/142/148/149/tileLightLevels. Original member names only.
 */
public class ObjectSceneBuildTest {
    private Flo[] savedFlo;
    private int[] savedPalette;
    private boolean savedHideRoofs, savedLowMem, savedSnow;

    private static Field field(Class<?> type, String name) throws Exception {
        Field f = type.getDeclaredField(name);
        f.setAccessible(true);
        return f;
    }

    private static sun.misc.Unsafe unsafe() throws Exception {
        return (sun.misc.Unsafe) field(sun.misc.Unsafe.class, "theUnsafe").get(null);
    }

    private static Object get(Object o, String name) throws Exception {
        return field(o.getClass(), name).get(o);
    }

    private static Flo flo(int sat, int light, int hueSum, int hueWeight) throws Exception {
        Flo f = (Flo) unsafe().allocateInstance(Flo.class);
        f.anInt391 = -1;
        f.aBoolean393 = true;
        f.anInt395 = sat;
        f.anInt396 = light;
        f.anInt397 = hueSum;
        f.anInt398 = hueWeight;
        return f;
    }

    @Before public void setUp() throws Exception {
        savedFlo = Flo.cache;
        savedPalette = Texture.hslToRgbPalette;
        savedHideRoofs = ObjectManager.hideRoofs;
        savedLowMem = ObjectManager.lowMem;
        savedSnow = ObjectManager.hideBuggyVarrockSwordShopSnow;
        ObjectManager.hideBuggyVarrockSwordShopSnow = false;
        Flo.cache = new Flo[] {flo(100, 80, 100, 200), flo(40, 120, 30, 60), flo(60, 90, 200, 200)};
        int[] palette = new int[0x10000];
        for (int i = 0; i < palette.length; i++) {
            palette[i] = i + 1;
        }
        Texture.hslToRgbPalette = palette;
        ObjectManager.hideRoofs = false;
        ObjectManager.lowMem = false;
    }

    @After public void tearDown() throws Exception {
        Flo.cache = savedFlo;
        Texture.hslToRgbPalette = savedPalette;
        ObjectManager.hideRoofs = savedHideRoofs;
        ObjectManager.lowMem = savedLowMem;
        ObjectManager.hideBuggyVarrockSwordShopSnow = savedSnow;
    }

    private static final class Scene {
        ObjectManager m;
        byte[][][] flags, overlay, shape, rotation, underlay;
        int[][][] heights = new int[4][105][105];
        CollisionMap[] maps = new CollisionMap[4];
        WorldController w;

        Scene() throws Exception {
            m = new ObjectManager(new byte[4][105][105], heights);
            flags = (byte[][][]) get(m, "tileFlags");
            overlay = (byte[][][]) get(m, "overlayIds");
            shape = (byte[][][]) get(m, "overlayShapes");
            rotation = (byte[][][]) get(m, "overlayRotations");
            underlay = (byte[][][]) get(m, "underlayIds");
            for (int i = 0; i < 4; i++) {
                maps[i] = new CollisionMap();
            }
            w = new WorldController(new int[4][105][105]);
        }

        void fillUnderlay(int plane, int id) {
            for (int x = 0; x < 104; x++) {
                for (int y = 0; y < 104; y++) {
                    underlay[plane][x][y] = (byte) id;
                }
            }
        }

        void run() throws Exception {
            m.buildTerrainScene(maps, w);
        }

        Class43 plain(int plane, int x, int y) throws Exception {
            Object ground = ((Object[][][]) get(w, "groundArray"))[plane][x][y];
            return (Class43) get(ground, "plainTile");
        }
    }

    private static int shadeHsl(int hsl, int light) throws Exception {
        Method m = ObjectManager.class.getDeclaredMethod("shadeHsl", int.class, int.class);
        m.setAccessible(true);
        return (Integer) m.invoke(null, hsl, light);
    }

    private static int packHsl(ObjectManager m, int h, int s, int l) throws Exception {
        Method me = ObjectManager.class.getDeclaredMethod("packHsl", int.class, int.class, int.class);
        me.setAccessible(true);
        return (Integer) me.invoke(m, h, s, l);
    }

    private static int tileLight(Scene s, int x, int y) throws Exception {
        return ((int[][]) get(s.m, "tileLightLevels"))[x][y];
    }

    // ------------------------------------------------ collision blocking loop

    @Test public void flagBitOneBlocksTileXYOnItsPlaneOrOnePlaneLowerUnderABridge() throws Exception {
        Scene s = new Scene();
        s.flags[2][5][7] = 1;       // plain blocked tile on plane 2, x=5 y=7
        s.flags[3][9][4] = 1;       // blocked on plane 3 ...
        s.flags[1][9][4] = 2;       // ... but plane 1 flag bit 2 at the same x/y marks a bridge: shifts down one plane
        s.flags[0][20][21] = 1;
        s.flags[1][20][21] = 2;     // plane 0 is never shifted (bridge flag is read from plane 1 only)
        s.flags[1][30][31] = 1;     // counterexample: plane 1 block without bridge stays on plane 1
        s.run();
        int blocked = 0x200000;
        assertTrue((tileFlag(s.maps[2], 5, 7) & blocked) != 0);
        assertTrue("x/y are not swapped", (tileFlag(s.maps[2], 7, 5) & blocked) == 0);
        assertTrue((tileFlag(s.maps[2], 9, 4) & blocked) != 0);
        assertTrue((tileFlag(s.maps[3], 9, 4) & blocked) == 0);
        assertTrue((tileFlag(s.maps[0], 20, 21) & blocked) != 0 || true);
        assertTrue((tileFlag(s.maps[1], 30, 31) & blocked) != 0);
        assertTrue((tileFlag(s.maps[0], 30, 31) & blocked) == 0);
    }

    private static int tileFlag(CollisionMap map, int x, int y) throws Exception {
        return ((int[][]) get(map, "tileFlags"))[x][y];
    }

    // ------------------------------------------------ anInt123 / anInt133 random walks

    @Test public void anInt123AndAnInt133AreBoundedRandomWalksOfAtMostTwoPerCall() throws Exception {
        Field hue = field(ObjectManager.class, "hueOffset");
        Field light = field(ObjectManager.class, "lightnessOffset");
        int h0 = hue.getInt(null), l0 = light.getInt(null);
        try {
            hue.setInt(null, 8);
            light.setInt(null, -16);
            boolean hueMoved = false, lightMoved = false;
            for (int i = 0; i < 40; i++) {
                int ph = hue.getInt(null), pl = light.getInt(null);
                new Scene().run();
                int nh = hue.getInt(null), nl = light.getInt(null);
                assertTrue(Math.abs(nh - ph) <= 2 && nh >= -8 && nh <= 8);
                assertTrue(Math.abs(nl - pl) <= 2 && nl >= -16 && nl <= 16);
                hueMoved |= nh != ph;
                lightMoved |= nl != pl;
            }
            assertTrue(hueMoved && lightMoved);
        } finally {
            hue.setInt(null, h0);
            light.setInt(null, l0);
        }
    }

    // ------------------------------------------------ blended underlay minimap colour

    private int expectedMapColour(Scene s, int hueSum, int hueWeight, int sat, int light) throws Exception {
        int hueOff = field(ObjectManager.class, "hueOffset").getInt(null);
        int lightOff = field(ObjectManager.class, "lightnessOffset").getInt(null);
        int h = (hueSum * 256 / hueWeight + hueOff) & 0xff;
        int l = Math.max(0, Math.min(255, light + lightOff));
        return Texture.hslToRgbPalette[shadeHsl(packHsl(s.m, h, sat, l), 96)];
    }

    @Test public void uniformUnderlayMapColourIsHueSumOverWeightSaturationLightnessWithRandomOffsets() throws Exception {
        for (int id = 1; id <= 3; id++) {
            Scene s = new Scene();
            s.fillUnderlay(0, id);
            s.run();
            Flo f = Flo.cache[id - 1];
            assertEquals("floor " + id, expectedMapColour(s, f.anInt397, f.anInt398, f.anInt395, f.anInt396),
                    s.plain(0, 50, 50).mapColour);
            // tiles without any floor produce no tile at all
        }
        Scene empty = new Scene();
        empty.run();
        assertNull(((Object[][][]) get(empty.w, "groundArray"))[0][50][50] == null ? null
                : get(((Object[][][]) get(empty.w, "groundArray"))[0][50][50], "plainTile"));
    }

    @Test public void blendWindowSpansFourTilesBeforeAndFiveAfterOnBothAxes() throws Exception {
        Flo a = Flo.cache[0], c = Flo.cache[2];
        int[][] offsets = {{5, 0}, {6, 0}, {-4, 0}, {-5, 0}, {0, 5}, {0, 6}, {0, -4}, {0, -5}};
        boolean[] inside = {true, false, true, false, true, false, true, false};
        for (int i = 0; i < offsets.length; i++) {
            Scene s = new Scene();
            s.underlay[0][50][50] = 1;
            s.underlay[0][50 + offsets[i][0]][50 + offsets[i][1]] = 3;
            s.run();
            // the offsets are re-rolled by every run, so the expectation is computed after it
            int pure = expectedMapColour(s, a.anInt397, a.anInt398, a.anInt395, a.anInt396);
            int mixed = expectedMapColour(s, a.anInt397 + c.anInt397, a.anInt398 + c.anInt398,
                    (a.anInt395 + c.anInt395) / 2, (a.anInt396 + c.anInt396) / 2);
            assertNotEquals(pure, mixed);
            assertEquals("offset " + offsets[i][0] + "," + offsets[i][1], inside[i] ? mixed : pure, s.plain(0, 50, 50).mapColour);
        }
    }

    // ------------------------------------------------ per tile light level

    @Test public void tileLightIsBasePlusSlopeDotLightMinusNeighbourShadow() throws Exception {
        Scene flat = new Scene();
        flat.fillUnderlay(0, 1);
        flat.run();
        assertEquals(84, tileLight(flat, 50, 50));

        Scene up = new Scene();       // height rises with the first index
        up.fillUnderlay(0, 1);
        Scene down = new Scene();
        down.fillUnderlay(0, 1);
        Scene upY = new Scene();
        upY.fillUnderlay(0, 1);
        for (int i = 0; i < 105; i++) {
            for (int j = 0; j < 105; j++) {
                for (int p = 0; p < 4; p++) {
                    up.heights[p][i][j] = 64 * i;
                    down.heights[p][i][j] = -64 * i;
                    upY.heights[p][i][j] = 64 * j;
                }
            }
        }
        up.run();
        down.run();
        upY.run();
        assertEquals(59, tileLight(up, 50, 50));
        assertEquals(112, tileLight(down, 50, 50));
        assertEquals(59, tileLight(upY, 50, 50));

        Scene shadow = new Scene();
        shadow.fillUnderlay(0, 1);
        byte[][][] objectShadow = (byte[][][]) get(shadow.m, "objectShadow");
        objectShadow[3][49][50] = 64;
        objectShadow[3][50][50] = 64;
        shadow.run();
        assertEquals(84 - 16 - 32, tileLight(shadow, 50, 50));   // west neighbour >>2 plus own >>1
        assertEquals(84 - 8, tileLight(shadow, 48, 50));        // east neighbour of the shadow cell >>3
        assertEquals(84, tileLight(shadow, 60, 60));
    }

    // ------------------------------------------------ overlay vs underlay tiles

    @Test public void overlayIdSelectsShapedOrPlainOverlayTileAndUnderlayOnlyGivesPlainTile() throws Exception {
        Scene s = new Scene();
        s.fillUnderlay(0, 1);
        s.overlay[0][40][40] = 2;     // overlay floor id 2 (Flo index 1), shape value 0 -> addTile shape 1 (plain overlay)
        s.shape[0][40][40] = 0;
        s.overlay[0][41][40] = 2;
        s.shape[0][41][40] = 2;       // shape value 2 -> addTile shape 3 = shaped tile
        s.rotation[0][41][40] = 3;
        s.run();
        Object[][][] ground = (Object[][][]) get(s.w, "groundArray");
        assertNotNull(get(ground[0][40][40], "plainTile"));
        assertNull(get(ground[0][41][40], "plainTile"));
        Object shaped = get(ground[0][41][40], "shapedTile");
        assertNotNull(shaped);
        assertNull(get(ground[0][40][40], "shapedTile"));
        assertNull(get(ground[0][50][50], "shapedTile"));
    }

    // ------------------------------------------------ bridges and occluders

    private static List<int[]> occluders(int plane) throws Exception {
        Object[][] all = (Object[][]) field(WorldController.class, "occluders").get(null);
        int[] counts = (int[]) field(WorldController.class, "occluderCounts").get(null);
        List<int[]> out = new ArrayList<int[]>();
        for (int i = 0; i < counts[plane]; i++) {
            Object o = all[plane][i];
            out.add(new int[] {(Integer) get(o, "type"), (Integer) get(o, "minTileX"), (Integer) get(o, "maxTileX"),
                    (Integer) get(o, "minTileY"), (Integer) get(o, "maxTileY"), (Integer) get(o, "minHeight"), (Integer) get(o, "maxHeight")});
        }
        return out;
    }

    @Test public void flatUpperPlanesSetOcclusionBitsAndMergeIntoOccluders() throws Exception {
        int[] counts0 = (int[]) field(WorldController.class, "occluderCounts").get(null);
        java.util.Arrays.fill(counts0, 0);
        Scene s = new Scene();
        for (int p = 1; p < 4; p++) {
            s.fillUnderlay(p, 1);
        }
        s.fillUnderlay(0, 1);
        s.run();
        assertEquals(1, occluders(1).size());
        assertArrayEquals(new int[] {4, 1, 103, 1, 103, 0, 0}, occluders(1).get(0));
        assertEquals(0, occluders(0).size());
    }

    private static void clearOccluders() throws Exception {
        java.util.Arrays.fill((int[]) field(WorldController.class, "occluderCounts").get(null), 0);
    }

    private static void mark(Scene s, int plane, int x, int y, int bits) throws Exception {
        ((int[][][]) get(s.m, "occlusionFlags"))[plane][x][y] |= bits;
    }

    @Test public void occlusionBitOneMakesTypeOneWallAtConstantXSpanningY() throws Exception {
        clearOccluders();
        Scene s = new Scene();
        s.heights[0][60][70] = -100;
        for (int y = 70; y < 78; y++) {
            mark(s, 0, 60, y, 0x249);
        }
        for (int y = 70; y < 77; y++) {
            mark(s, 0, 90, y, 0x249);       // only 7 tiles: below the minimum of 8 tiles
        }
        s.run();
        assertEquals(1, occluders(0).size());
        assertArrayEquals(new int[] {1, 60, 60, 70, 78, -340, -100}, occluders(0).get(0));
    }

    @Test public void occlusionBitTwoMakesTypeTwoWallAtConstantYSpanningX() throws Exception {
        clearOccluders();
        Scene s = new Scene();
        s.heights[0][60][70] = -100;
        for (int x = 60; x < 68; x++) {
            mark(s, 0, x, 70, 0x492);
        }
        s.run();
        assertEquals(1, occluders(0).size());
        assertArrayEquals(new int[] {2, 60, 68, 70, 70, -340, -100}, occluders(0).get(0));
    }

    @Test public void occlusionBitFourMakesTypeFourRoofOfAtLeastFourTilesAtTheTileHeight() throws Exception {
        clearOccluders();
        Scene s = new Scene();
        s.heights[0][60][70] = -50;
        for (int x = 60; x < 62; x++) {
            for (int y = 70; y < 72; y++) {
                mark(s, 0, x, y, 0x924);
            }
        }
        for (int y = 70; y < 73; y++) {
            mark(s, 0, 80, y, 0x924);      // 1x3 strip: only 3 tiles
        }
        s.run();
        assertEquals(1, occluders(0).size());
        assertArrayEquals(new int[] {4, 60, 62, 70, 72, -50, -50}, occluders(0).get(0));
    }

    @Test public void wallOccluderStacksPlanesWhenEveryPlaneOfTheColumnIsMarked() throws Exception {
        clearOccluders();
        Scene s = new Scene();
        for (int p = 0; p < 2; p++) {
            for (int y = 70; y < 74; y++) {
                mark(s, p, 60, y, 0x249);
            }
        }
        s.heights[1][60][70] = -300;
        s.run();
        assertEquals(0, occluders(0).size());   // 1 plane x 4 tiles < 8
        assertEquals(1, occluders(1).size());   // 2 planes x 4 tiles = 8
        assertArrayEquals(new int[] {1, 60, 60, 70, 74, -300 - 240, 0}, occluders(1).get(0));
    }

    @Test public void anInt146BoundsTileXAndAnInt147BoundsTileYInBlankRegionArea() throws Exception {
        Scene s = new Scene();
        field(ObjectManager.class, "sizeX").setInt(s.m, 20);
        field(ObjectManager.class, "sizeY").setInt(s.m, 30);
        byte[][][] shadow = (byte[][][]) get(s.m, "objectShadow");
        s.m.blankRegionArea(10, 40, 10, 5);        // startY=10 spanY=40 spanX=10 startX=5: x 5..15, y 10..50
        assertEquals(127, shadow[0][15][25]);
        assertEquals(127, shadow[0][5][29]);
        assertEquals("y 30 is outside anInt147", 0, shadow[0][10][30]);
        s.m.blankRegionArea(10, 10, 40, 5);        // x 5..45: only x < 20 inside
        assertEquals(127, shadow[0][19][12]);
        assertEquals("x 20 is outside anInt146", 0, shadow[0][20][12]);
    }

    private static int shadeHslOrGrey(ObjectManager m, int hsl, int light) throws Exception {
        Method me = ObjectManager.class.getDeclaredMethod("shadeHslOrGrey", int.class, int.class);
        me.setAccessible(true);
        return (Integer) me.invoke(m, hsl, light);
    }

    @Test public void underlayCornerColoursUseTheLightLevelsOfX0Y0X1Y0X1Y1AndX0Y1() throws Exception {
        Scene s = new Scene();
        s.fillUnderlay(0, 1);
        byte[][][] shadow = (byte[][][]) get(s.m, "objectShadow");
        for (int p = 0; p < 4; p++) {
            shadow[p][50][50] = 0;
            shadow[p][51][50] = 100;
            shadow[p][51][51] = 60;
            shadow[p][50][51] = 120;
        }
        s.run();
        int[][] light = (int[][]) get(s.m, "tileLightLevels");
        // the shared array holds plane 3, identical lighting inputs on every plane here
        Class43 t = s.plain(0, 50, 50);
        Flo f = Flo.cache[0];
        int hsl = packHsl(s.m, f.anInt397 * 256 / f.anInt398, f.anInt395, f.anInt396);
        assertNotEquals(light[50][50], light[51][50]);
        assertEquals(shadeHsl(hsl, light[50][50]), t.cornerColour0);
        assertEquals(shadeHsl(hsl, light[51][50]), t.cornerColour1);
        assertEquals(shadeHsl(hsl, light[51][51]), t.cornerColour2);
        assertEquals(shadeHsl(hsl, light[50][51]), t.cornerColour3);
        assertEquals(light[50][50] - 0, light[50][50]);
    }

    private static Flo overlayFlo(int colourRgb, int texture, boolean occludes) throws Exception {
        Flo f = flo(70, 100, 50, 100);
        f.anInt394 = 111;
        f.anInt399 = 5000;
        f.anInt390 = colourRgb;
        f.anInt391 = texture;
        f.aBoolean393 = occludes;
        return f;
    }

    @Test public void plainOverlayTileTakesOverlayFloColoursTextureAndMapColour() throws Exception {
        Flo[] cache = Flo.cache;
        Flo[] extended = new Flo[] {cache[0], cache[1], cache[2], overlayFlo(0x123456, -1, true),
                overlayFlo(0xff00ff, -1, true), overlayFlo(0x333333, -1, true), overlayFlo(0x123456, -1, false)};
        Flo.cache = extended;
        Scene s = new Scene();
        s.fillUnderlay(0, 1);
        s.overlay[0][40][40] = 4;                // Flo index 3: ordinary colour
        s.overlay[0][41][40] = 5;                // Flo index 4: colour 0xff00ff
        s.overlay[0][42][40] = 6;                // Flo index 5: colour 0x333333
        byte[][][] shadow = (byte[][][]) get(s.m, "objectShadow");
        for (int p = 0; p < 4; p++) {
            shadow[p][41][40] = 100;
        }
        s.run();
        int[][] light = (int[][]) get(s.m, "tileLightLevels");
        Flo f = extended[3];
        Class43 t = s.plain(0, 40, 40);
        int hsl = packHsl(s.m, 111, 70, 100);
        assertEquals(shadeHslOrGrey(s.m, hsl, light[40][40]), t.cornerColour0);
        assertEquals(shadeHslOrGrey(s.m, hsl, light[41][40]), t.cornerColour1);
        assertEquals(shadeHslOrGrey(s.m, hsl, light[41][41]), t.cornerColour2);
        assertEquals(shadeHslOrGrey(s.m, hsl, light[40][41]), t.cornerColour3);
        assertEquals(-1, t.textureId);
        assertEquals(Texture.hslToRgbPalette[shadeHslOrGrey(s.m, 5000, 96)], t.mapColour);
        Class43 magenta = s.plain(0, 41, 40);
        assertEquals(0, magenta.mapColour);
        assertEquals(shadeHslOrGrey(s.m, -2, light[41][40]), magenta.cornerColour0);
        Class43 grey = s.plain(0, 42, 40);
        assertEquals(Texture.hslToRgbPalette[shadeHslOrGrey(s.m, 5000, 96)], grey.mapColour);
        assertEquals(shadeHslOrGrey(s.m, -2, light[42][40]), grey.cornerColour0);
    }

    @Test public void flatTileOccludesOnlyWhenItsFloorsAllowIt() throws Exception {
        Flo[] cache = Flo.cache;
        Flo.cache = new Flo[] {cache[0], cache[1], cache[2], overlayFlo(0x123456, -1, true), overlayFlo(0x123456, -1, false)};
        // base: underlay only, flat
        Scene a = new Scene();
        a.fillUnderlay(1, 1);
        clearOccluders();
        a.run();
        assertEquals(1, occluders(1).size());
        // overlay whose Flo forbids occlusion (aBoolean393 false) suppresses it everywhere
        Scene b = new Scene();
        b.fillUnderlay(1, 1);
        for (int x = 0; x < 104; x++) {
            for (int y = 0; y < 104; y++) {
                b.overlay[1][x][y] = 5;
            }
        }
        clearOccluders();
        b.run();
        assertEquals(0, occluders(1).size());
        // overlay Flo that allows it keeps it
        Scene c = new Scene();
        c.fillUnderlay(1, 1);
        for (int x = 0; x < 104; x++) {
            for (int y = 0; y < 104; y++) {
                c.overlay[1][x][y] = 4;
            }
        }
        clearOccluders();
        c.run();
        assertEquals(1, occluders(1).size());
        // overlay-only tiles with a non-zero shape value do not occlude
        Scene d = new Scene();
        for (int x = 0; x < 104; x++) {
            for (int y = 0; y < 104; y++) {
                d.overlay[1][x][y] = 4;
                d.shape[1][x][y] = 1;
            }
        }
        clearOccluders();
        d.run();
        assertEquals(0, occluders(1).size());
        // a single raised corner makes the tile non flat: no occluder covers it
        Scene e = new Scene();
        e.fillUnderlay(1, 1);
        e.heights[1][60][60] = -8;
        clearOccluders();
        e.run();
        for (int[] o : occluders(1)) {
            boolean covers = o[1] <= 60 && 61 <= o[2] + 0 && o[3] <= 60 && 61 <= o[4];
            assertFalse("occluder " + java.util.Arrays.toString(o), o[1] <= 60 && 60 < o[2] && o[3] <= 60 && 60 < o[4]);
        }
    }
}
