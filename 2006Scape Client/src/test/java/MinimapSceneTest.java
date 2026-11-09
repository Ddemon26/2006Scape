import static org.junit.Assert.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Minimap image build (Game.buildMinimapImage / Game.drawMinimapTileObjects), the map-function icon list it collects, the map-scene
 * backgrounds, and the two ObjectDef members (mapFunctionId / mapSceneId) that drive them. Uses the renamed members (call sites updated after the pass 155 renames; assertions unchanged).
 */
public class MinimapSceneTest {
    private final DrawingPrimitivesTest state = new DrawingPrimitivesTest();
    private Game g;
    private Sprite map;
    private Object savedCache;

    private static Field field(Class<?> type, String name) throws Exception {
        Field f = type.getDeclaredField(name);
        f.setAccessible(true);
        return f;
    }

    private static ObjectDef newDef(int type) throws Exception {
        Constructor<ObjectDef> c = ObjectDef.class.getDeclaredConstructor();
        c.setAccessible(true);
        ObjectDef def = c.newInstance();
        Method defaults = ObjectDef.class.getDeclaredMethod("setDefaults");
        defaults.setAccessible(true);
        defaults.invoke(def);
        def.type = type;
        return def;
    }

    private static ObjectDef decode(int... bytes) throws Exception {
        byte[] data = new byte[bytes.length];
        for (int n = 0; n < bytes.length; n++) data[n] = (byte) bytes[n];
        Stream stream = new Stream(data);
        ObjectDef def = newDef(-1);
        Method read = ObjectDef.class.getDeclaredMethod("readValues", Stream.class);
        read.setAccessible(true);
        read.invoke(def, stream);
        assertEquals("decoder consumed exactly the bytes", data.length, stream.currentOffset);
        return def;
    }

    /** Puts the definitions in the object cache (forID finds them by type). */
    private static void install(ObjectDef... defs) throws Exception {
        ObjectDef[] cache = new ObjectDef[20];
        for (int n = 0; n < 20; n++) cache[n] = n < defs.length ? defs[n] : newDef(-1);
        field(ObjectDef.class, "cache").set(null, cache);
    }

    private static Background scene(int w, int h, int rgb, int offsetX, int offsetY) {
        Background b = ScrollbarTest.image(rgb);
        b.imageWidth = w;
        b.imageHeight = h;
        b.canvasWidth = w;
        b.offsetX = offsetX;
        b.offsetY = offsetY;
        b.paletteIndices = new byte[w * h];
        Arrays.fill(b.paletteIndices, (byte) 1);
        return b;
    }

    @Before public void before() throws Exception {
        state.saveState();
        savedCache = field(ObjectDef.class, "cache").get(null);
        g = new Game();
        map = new Sprite(512, 512);
        g.minimapSprite = map;
        g.worldController = new WorldController(new int[4][105][105]);
        g.mapScenes = new Background[100];
        g.mapFunctions = new Sprite[100];
        g.byteGroundArray = new byte[4][104][104];
        g.aClass11Array1230 = new CollisionMap[4];
        for (int p = 0; p < 4; p++) {
            g.aClass11Array1230[p] = new CollisionMap();
            openFlags(p, 0);
        }
        g.mapFunctionIconTileX = new int[1000];
        g.mapFunctionIconTileY = new int[1000];
        g.mapFunctionIconSprites = new Sprite[1000];
        map.setAsDrawingArea();
    }

    @After public void after() throws Exception {
        field(ObjectDef.class, "cache").set(null, savedCache);
        state.restoreState();
    }

    private void openFlags(int plane, int value) throws Exception {
        int[][] flags = (int[][]) field(CollisionMap.class, "tileFlags").get(g.aClass11Array1230[plane]);
        for (int[] row : flags) Arrays.fill(row, value);
    }

    private static int uid(int x, int y, int id, boolean hasActions) {
        int uid = x + (y << 7) + (id << 14);
        return hasActions ? uid : uid + 0x80000000;
    }

    private int wallUid(int plane, int x, int y, int id, boolean actions, int shape, int rotation) {
        int uid = uid(x, y, id, actions);
        g.worldController.addWall(0, new Animable(), uid, y, (byte) ((rotation << 6) + shape), x, null, 0, 0, plane);
        return uid;
    }

    private int sceneUid(int plane, int x, int y, int id, boolean actions, int shape, int rotation) {
        int uid = uid(x, y, id, actions) + 0x40000000;
        g.worldController.addSceneObject(uid, (byte) ((rotation << 6) + shape), 0, 1, new Animable(), 1, plane, rotation, y, x);
        return uid;
    }

    private int groundUid(int plane, int x, int y, int id) {
        int uid = uid(x, y, id, true);
        g.worldController.addGroundDecoration(plane, 0, y, new Animable(), (byte) 22, uid, x);
        return uid;
    }

    private int pixelAt(int x, int y) {
        return map.pixels[x + y * 512];
    }

    /** Block origin of a tile in the 512-wide minimap image. */
    private static int blockX(int tileX) {
        return 48 + tileX * 4;
    }

    private static int blockY(int tileY) {
        return 48 + (103 - tileY) * 4;
    }

    private static int[] left() {
        return new int[] {0, 0, 0, 1, 0, 2, 0, 3};
    }

    private static int[] top() {
        return new int[] {0, 0, 1, 0, 2, 0, 3, 0};
    }

    private static int[] right() {
        return new int[] {3, 0, 3, 1, 3, 2, 3, 3};
    }

    private static int[] bottom() {
        return new int[] {0, 3, 1, 3, 2, 3, 3, 3};
    }

    private static int[] concat(int[] a, int[] b) {
        int[] r = Arrays.copyOf(a, a.length + b.length);
        System.arraycopy(b, 0, r, a.length, b.length);
        return r;
    }

    private void assertOnly(int tileX, int tileY, int colour, int[] relative, String what) {
        int[] expected = new int[512 * 512];
        for (int n = 0; n < relative.length; n += 2) {
            expected[blockX(tileX) + relative[n] + (blockY(tileY) + relative[n + 1]) * 512] = colour;
        }
        for (int n = 0; n < expected.length; n++) {
            if (expected[n] != map.pixels[n]) fail(what + ": pixel " + (n % 512) + "," + (n / 512) + " expected " + Integer.toHexString(expected[n]) + " was " + Integer.toHexString(map.pixels[n]));
        }
    }

    // ------------------------------------------------------------ ObjectDef decoding

    @Test public void opcodeSixtyAndSixtyEightReadIndependentUnsignedShorts() throws Exception {
        ObjectDef fresh = newDef(0);
        assertEquals(-1, fresh.mapFunctionId);
        assertEquals(-1, fresh.mapSceneId);
        ObjectDef a = decode(60, 0x01, 0x02, 0);
        assertEquals("big endian", 0x0102, a.mapFunctionId);
        assertEquals("opcode 60 leaves the other member alone", -1, a.mapSceneId);
        ObjectDef b = decode(68, 0x00, 0x2a, 0);
        assertEquals(42, b.mapSceneId);
        assertEquals(-1, b.mapFunctionId);
        ObjectDef both = decode(60, 0x00, 0x07, 68, 0x00, 0x09, 0);
        assertEquals(7, both.mapFunctionId);
        assertEquals(9, both.mapSceneId);
        ObjectDef big = decode(60, 0xff, 0xff, 68, 0xff, 0xff, 0);
        assertEquals("no 65535 -> -1 conversion for these two", 65535, big.mapFunctionId);
        assertEquals(65535, big.mapSceneId);
        ObjectDef low = decode(60, 0, 0, 68, 0, 0, 0);
        assertEquals("zero is a real value, distinct from the default", 0, low.mapFunctionId);
        assertEquals(0, low.mapSceneId);
    }

    // ------------------------------------------------------------ drawMinimapTileObjects: one tile of the minimap image

    @Test public void wallEdgesFollowShapeAndRotationAndUseTheInteractiveColourForPositiveUids() throws Exception {
        install(newDef(5));
        int[][] edgesShape0 = {left(), top(), right(), bottom()};
        int[][] edgesShape2 = {top(), right(), bottom(), left()};
        int[][] corner = {{0, 0}, {3, 0}, {3, 3}, {0, 3}};
        for (int rotation = 0; rotation < 4; rotation++) {
            for (int shape : new int[] {0, 2, 3, 1}) {
                for (boolean actions : new boolean[] {true, false}) {
                    g.worldController = new WorldController(new int[4][105][105]);
                    Arrays.fill(map.pixels, 0);
                    wallUid(1, 10, 20, 5, actions, shape, rotation);
                    g.drawMinimapTileObjects(20, 0x111111, 10, 0x222222, 1);
                    int colour = actions ? 0x222222 : 0x111111;
                    int[] expected = shape == 0 ? edgesShape0[rotation] : shape == 2 ? concat(edgesShape0[rotation], edgesShape2[rotation]) : shape == 3 ? corner[rotation] : new int[0];
                    assertOnly(10, 20, colour, expected, "shape " + shape + " rotation " + rotation + " actions " + actions);
                }
            }
        }
    }

    @Test public void methodFiftyOnlyLooksAtTheRequestedPlaneAndTile() throws Exception {
        install(newDef(5));
        wallUid(2, 30, 40, 5, true, 0, 0);
        g.drawMinimapTileObjects(40, 1, 30, 2, 1);
        assertOnly(30, 40, 0, new int[0], "other plane");
        g.drawMinimapTileObjects(41, 1, 30, 2, 2);
        assertOnly(30, 40, 0, new int[0], "other tile");
        g.drawMinimapTileObjects(40, 1, 30, 2, 2);
        assertOnly(30, 40, 2, left(), "right tile and plane");
    }

    @Test public void diagonalSceneObjectsUseFixedColoursAndNotTheArguments() throws Exception {
        install(newDef(6));
        int[] anti = {0, 3, 1, 2, 2, 1, 3, 0};
        int[] main = {0, 0, 1, 1, 2, 2, 3, 3};
        for (int rotation = 0; rotation < 4; rotation++) {
            for (boolean actions : new boolean[] {true, false}) {
                g.worldController = new WorldController(new int[4][105][105]);
                Arrays.fill(map.pixels, 0);
                sceneUid(0, 12, 15, 6, actions, 9, rotation);
                g.drawMinimapTileObjects(15, 0x111111, 12, 0x222222, 0);
                assertOnly(12, 15, actions ? 0xee0000 : 0xeeeeee, rotation % 2 == 0 ? anti : main, "rotation " + rotation + " actions " + actions);
            }
        }
        g.worldController = new WorldController(new int[4][105][105]);
        Arrays.fill(map.pixels, 0);
        sceneUid(0, 12, 15, 6, true, 10, 0);
        g.drawMinimapTileObjects(15, 0x111111, 12, 0x222222, 0);
        assertOnly(12, 15, 0, new int[0], "other shapes draw nothing");
    }

    @Test public void mapSceneBackgroundReplacesTheWallOrObjectPixelsAndIsCentredOnTheFootprint() throws Exception {
        ObjectDef def = newDef(7);
        def.mapSceneId = 3;
        def.sizeX = 2;
        def.sizeY = 3;
        install(def);
        g.mapScenes[3] = scene(5, 7, 0x00ff00, 0, 0);
        Sprite icon = new Sprite(5, 7);
        Arrays.fill(icon.pixels, 0xff00ff);
        g.mapFunctions[3] = icon;
        // wall, scene object and ground decoration each take the same background path
        for (int kind = 0; kind < 3; kind++) {
            g.worldController = new WorldController(new int[4][105][105]);
            Arrays.fill(map.pixels, 0);
            if (kind == 0) wallUid(0, 10, 20, 7, true, 0, 0);
            if (kind == 1) sceneUid(0, 10, 20, 7, true, 9, 0);
            if (kind == 2) groundUid(0, 10, 20, 7);
            g.drawMinimapTileObjects(20, 0x111111, 10, 0x222222, 0);
            // (sizeX*4 - w) / 2 = 1 ; (sizeY*4 - h) / 2 = 2 ; row from (104 - tileY - sizeY)
            int x0 = 48 + 10 * 4 + (2 * 4 - 5) / 2;
            int y0 = 48 + (104 - 20 - 3) * 4 + (3 * 4 - 7) / 2;
            for (int y = 0; y < 512; y++) {
                for (int x = 0; x < 512; x++) {
                    boolean inside = x >= x0 && x < x0 + 5 && y >= y0 && y < y0 + 7;
                    assertEquals("kind " + kind + " pixel " + x + "," + y, inside ? 0x00ff00 : 0, pixelAt(x, y));
                }
            }
        }
    }

    @Test public void mapSceneCentringTruncatesTowardZeroAndHonoursTheBackgroundOffset() throws Exception {
        ObjectDef def = newDef(7);
        def.mapSceneId = 3;
        install(def);
        // 1x1 footprint = 4x4 pixels; a 7x2 background gives (4-7)/2 = -1 and (4-2)/2 = 1; a 5x5 gives 0 (not -1)
        g.mapScenes[3] = scene(7, 2, 0x00ff00, 0, 0);
        wallUid(0, 10, 20, 7, true, 0, 0);
        g.drawMinimapTileObjects(20, 0x111111, 10, 0x222222, 0);
        assertEquals(0x00ff00, pixelAt(blockX(10) - 1, blockY(20) + 1));
        assertEquals(0, pixelAt(blockX(10) - 2, blockY(20) + 1));
        assertEquals(0x00ff00, pixelAt(blockX(10) + 5, blockY(20) + 2));
        assertEquals(0, pixelAt(blockX(10) + 5, blockY(20) + 3));
        Arrays.fill(map.pixels, 0);
        g.mapScenes[3] = scene(5, 5, 0x00ff00, 0, 0);
        g.drawMinimapTileObjects(20, 0x111111, 10, 0x222222, 0);
        assertEquals("(4-5)/2 truncates to 0", 0x00ff00, pixelAt(blockX(10), blockY(20)));
        assertEquals(0, pixelAt(blockX(10) - 1, blockY(20)));
        Arrays.fill(map.pixels, 0);
        g.mapScenes[3] = scene(4, 4, 0x00ff00, 3, -2);
        g.drawMinimapTileObjects(20, 0x111111, 10, 0x222222, 0);
        assertEquals("background offsets shift the drawing", 0x00ff00, pixelAt(blockX(10) + 3, blockY(20) - 2));
        assertEquals(0, pixelAt(blockX(10), blockY(20)));
    }

    @Test public void missingMapSceneBackgroundDrawsNothingAndTheFunctionMemberIsIgnored() throws Exception {
        ObjectDef def = newDef(7);
        def.mapSceneId = 3;
        def.mapFunctionId = 4;
        install(def);
        Sprite icon = new Sprite(4, 4);
        Arrays.fill(icon.pixels, 0xff00ff);
        g.mapFunctions[3] = icon;
        g.mapFunctions[4] = icon;
        wallUid(0, 10, 20, 7, true, 0, 0);
        g.drawMinimapTileObjects(20, 0x111111, 10, 0x222222, 0);
        assertOnly(10, 20, 0, new int[0], "scene entry null: neither edge pixels nor a function sprite");
        // only mapSceneId selects the replacement; mapFunctionId alone leaves the edge pixels
        def.mapSceneId = -1;
        g.mapScenes[4] = scene(4, 4, 0x00ff00, 0, 0);
        g.drawMinimapTileObjects(20, 0x111111, 10, 0x222222, 0);
        assertOnly(10, 20, 0x222222, left(), "mapFunctionId does not select a map scene");
        // and a ground decoration without a map scene draws nothing at all
        g.worldController = new WorldController(new int[4][105][105]);
        Arrays.fill(map.pixels, 0);
        groundUid(0, 10, 20, 7);
        g.drawMinimapTileObjects(20, 0x111111, 10, 0x222222, 0);
        assertOnly(10, 20, 0, new int[0], "ground decoration, no scene");
    }

    // ------------------------------------------------------------ buildMinimapImage: whole minimap image

    @Test public void method24ClearsTheImageThenDrawsInteriorTilesOnly() throws Exception {
        install(newDef(5));
        Arrays.fill(map.pixels, 0x7f7f7f);
        g.buildMinimapImage(0);
        for (int v : map.pixels) assertEquals(0, v);
        wallUid(0, 0, 5, 5, true, 0, 0);
        wallUid(0, 1, 5, 5, true, 0, 0);
        wallUid(0, 102, 5, 5, true, 0, 0);
        wallUid(0, 103, 5, 5, true, 0, 0);
        wallUid(0, 7, 0, 5, true, 0, 0);
        wallUid(0, 7, 1, 5, true, 0, 0);
        wallUid(0, 7, 102, 5, true, 0, 0);
        wallUid(0, 7, 103, 5, true, 0, 0);
        g.buildMinimapImage(0);
        int[][] drawn = {{1, 5}, {102, 5}, {7, 1}, {7, 102}};
        int[][] skipped = {{0, 5}, {103, 5}, {7, 0}, {7, 103}};
        for (int[] t : drawn) assertNotEquals("tile " + t[0] + "," + t[1], 0, pixelAt(blockX(t[0]), blockY(t[1])));
        for (int[] t : skipped) assertEquals("tile " + t[0] + "," + t[1], 0, pixelAt(blockX(t[0]), blockY(t[1])));
    }

    @Test public void method24SelectsPerTileObjectsByPlaneFlags() throws Exception {
        install(newDef(5));
        // plane 0 tile drawn only when (flags & 0x18) == 0
        for (int flags : new int[] {0, 0x8, 0x10, 0x18, 0x7, 0x20}) {
            g.worldController = new WorldController(new int[4][105][105]);
            Arrays.fill(map.pixels, 0);
            g.byteGroundArray[0][20][30] = (byte) flags;
            wallUid(0, 20, 30, 5, true, 0, 0);
            g.buildMinimapImage(0);
            assertEquals("flags " + flags, (flags & 0x18) == 0, pixelAt(blockX(20), blockY(30)) != 0);
        }
        // the plane above is drawn when its flag bit 8 is set (and the plane is below 3)
        for (int flags : new int[] {0, 0x8, 0x10, 0x18, 0x7}) {
            g.worldController = new WorldController(new int[4][105][105]);
            Arrays.fill(map.pixels, 0);
            g.byteGroundArray[0][20][30] = 0x8; // plane 0 itself is skipped
            g.byteGroundArray[1][20][30] = (byte) flags;
            wallUid(1, 20, 30, 5, true, 0, 0);
            g.buildMinimapImage(0);
            assertEquals("upper flags " + flags, (flags & 8) != 0, pixelAt(blockX(20), blockY(30)) != 0);
        }
        // plane 3 never looks at a plane 4
        g.worldController = new WorldController(new int[4][105][105]);
        Arrays.fill(map.pixels, 0);
        g.byteGroundArray[3][20][30] = 0;
        wallUid(3, 20, 30, 5, true, 0, 0);
        g.buildMinimapImage(3);
        assertNotEquals(0, pixelAt(blockX(20), blockY(30)));
    }

    @Test public void method24WallColoursAreOneRandomPairPerBuildNearGreyAndNearRed() throws Exception {
        install(newDef(5), newDef(6));
        java.util.Set<Integer> greys = new java.util.HashSet<Integer>();
        java.util.Set<Integer> reds = new java.util.HashSet<Integer>();
        for (int trial = 0; trial < 60; trial++) {
            g.worldController = new WorldController(new int[4][105][105]);
            Arrays.fill(map.pixels, 0);
            wallUid(0, 20, 30, 5, false, 3, 0);
            wallUid(0, 25, 31, 5, false, 3, 0);
            wallUid(0, 22, 33, 5, true, 3, 0);
            wallUid(0, 23, 34, 5, true, 3, 0);
            g.buildMinimapImage(0);
            int grey = pixelAt(blockX(20), blockY(30));
            int red = pixelAt(blockX(22), blockY(33));
            assertEquals("one grey value per build", grey, pixelAt(blockX(25), blockY(31)));
            assertEquals("one red value per build", red, pixelAt(blockX(23), blockY(34)));
            for (int shift = 0; shift <= 16; shift += 8) {
                int channel = grey >> shift & 0xff;
                assertTrue("grey channel " + channel, channel >= 228 && channel <= 247);
            }
            assertEquals(0, red & 0xffff);
            assertTrue("red channel " + (red >> 16), (red >> 16) >= 228 && (red >> 16) <= 247);
            greys.add(grey);
            reds.add(red);
        }
        assertTrue("grey varies between builds", greys.size() > 5);
        assertTrue("red varies between builds", reds.size() > 3);
    }

    // ------------------------------------------------------------ buildMinimapImage: map-function icon list

    @Test public void iconListHoldsOneEntryPerGroundDecorationWithAFunctionIdInColumnMajorOrder() throws Exception {
        ObjectDef none = newDef(1);
        ObjectDef a = newDef(2);
        a.mapFunctionId = 22;
        ObjectDef b = newDef(3);
        b.mapFunctionId = 29;
        ObjectDef sceneOnly = newDef(4);
        sceneOnly.mapSceneId = 9;
        install(none, a, b, sceneOnly);
        Sprite s22 = new Sprite(2, 2), s29 = new Sprite(2, 2);
        g.mapFunctions[22] = s22;
        g.mapFunctions[29] = s29;
        groundUid(0, 6, 3, 3);
        groundUid(0, 5, 9, 2);
        groundUid(0, 5, 2, 1);
        groundUid(0, 7, 7, 4);
        wallUid(0, 8, 8, 2, true, 0, 0);
        g.mapFunctionIconCount = 99;
        g.buildMinimapImage(0);
        assertEquals("count is rebuilt from zero", 2, g.mapFunctionIconCount);
        assertSame(s22, g.mapFunctionIconSprites[0]);
        assertEquals(5, g.mapFunctionIconTileX[0]);
        assertEquals(9, g.mapFunctionIconTileY[0]);
        assertSame(s29, g.mapFunctionIconSprites[1]);
        assertEquals(6, g.mapFunctionIconTileX[1]);
        assertEquals(3, g.mapFunctionIconTileY[1]);
        g.buildMinimapImage(0);
        assertEquals("rebuilding does not accumulate", 2, g.mapFunctionIconCount);
    }

    @Test public void iconsComeFromTheGamePlaneFieldNotTheArgument() throws Exception {
        ObjectDef a = newDef(2);
        a.mapFunctionId = 22;
        install(a);
        g.mapFunctions[22] = new Sprite(2, 2);
        groundUid(2, 40, 41, 2);
        g.plane = 0;
        g.buildMinimapImage(2);
        assertEquals(0, g.mapFunctionIconCount);
        g.plane = 2;
        g.buildMinimapImage(0);
        assertEquals(1, g.mapFunctionIconCount);
        assertEquals(40, g.mapFunctionIconTileX[0]);
        assertEquals(41, g.mapFunctionIconTileY[0]);
    }

    @Test public void iconIdsOutsideTheStationarySetMoveWithinThreeTilesAndNeverIntoBlockedDirections() throws Exception {
        int[] still = {22, 29, 34, 36, 46, 47, 48};
        ObjectDef[] defs = new ObjectDef[20];
        for (int n = 0; n < 20; n++) {
            defs[n] = newDef(n + 1);
            defs[n].mapFunctionId = n < still.length ? still[n] : 20 + n; // 20..39 for the remaining definitions
        }
        install(defs);
        for (int n = 0; n < 20; n++) g.mapFunctions[defs[n].mapFunctionId] = new Sprite(2, 2);
        for (int n = 0; n < still.length; n++) {
            groundUid(0, 10 + n * 8, 50, n + 1);
        }
        boolean movedSomewhere = false;
        for (int trial = 0; trial < 40; trial++) {
            g.buildMinimapImage(0);
            assertEquals(still.length, g.mapFunctionIconCount);
            for (int n = 0; n < still.length; n++) {
                assertEquals("id " + still[n] + " stays", 10 + n * 8, g.mapFunctionIconTileX[n]);
                assertEquals(50, g.mapFunctionIconTileY[n]);
            }
        }
        // a movable id (36 is in the set; 41 is not) wanders only with open neighbours
        ObjectDef movable = newDef(30);
        movable.mapFunctionId = 41;
        install(movable);
        g.mapFunctions[41] = new Sprite(2, 2);
        g.worldController = new WorldController(new int[4][105][105]);
        groundUid(0, 50, 50, 30);
        int minX = 99, maxX = 0, minY = 99, maxY = 0;
        for (int trial = 0; trial < 1500; trial++) {
            g.buildMinimapImage(0);
            minX = Math.min(minX, g.mapFunctionIconTileX[0]);
            maxX = Math.max(maxX, g.mapFunctionIconTileX[0]);
            minY = Math.min(minY, g.mapFunctionIconTileY[0]);
            maxY = Math.max(maxY, g.mapFunctionIconTileY[0]);
        }
        assertEquals("reaches three tiles left", 47, minX);
        assertEquals("reaches three tiles right", 53, maxX);
        assertEquals("reaches three tiles down", 47, minY);
        assertEquals("reaches three tiles up", 53, maxY);
    }

    /** One movable icon at (x, y); returns [dx, dy] after one build with the given collision flag value everywhere. */
    private int[] wander(int x, int y, int flags) throws Exception {
        openFlags(0, flags);
        g.buildMinimapImage(0);
        assertEquals(1, g.mapFunctionIconCount);
        return new int[] {g.mapFunctionIconTileX[0] - x, g.mapFunctionIconTileY[0] - y};
    }

    private void oneMovableIcon(int x, int y) throws Exception {
        ObjectDef movable = newDef(30);
        movable.mapFunctionId = 41;
        install(movable);
        g.mapFunctions[41] = new Sprite(2, 2);
        g.worldController = new WorldController(new int[4][105][105]);
        groundUid(0, x, y, 30);
    }

    @Test public void eachDirectionHasItsOwnBlockingMask() throws Exception {
        oneMovableIcon(50, 50);
        // flag value that blocks exactly one direction: left 0x8, right 0x80, down 0x2, up 0x20
        int[] block = {0x8, 0x80, 0x2, 0x20};
        for (int d = 0; d < 4; d++) {
            boolean sawX = false, sawY = false;
            for (int trial = 0; trial < 300; trial++) {
                int[] delta = wander(50, 50, block[d]);
                if (d == 0) assertTrue("never left: " + delta[0], delta[0] >= 0);
                if (d == 1) assertTrue("never right: " + delta[0], delta[0] <= 0);
                if (d == 2) assertTrue("never down: " + delta[1], delta[1] >= 0);
                if (d == 3) assertTrue("never up: " + delta[1], delta[1] <= 0);
                sawX |= delta[0] != 0;
                sawY |= delta[1] != 0;
            }
            assertTrue("still moves along x", sawX);
            assertTrue("still moves along y", sawY);
        }
        // the shared high bits block every direction
        for (int flags : new int[] {0x1280000 & 0x80000, 0x200000, 0x1000000, 0xAA}) {
            for (int trial = 0; trial < 60; trial++) {
                int[] delta = wander(50, 50, flags);
                assertEquals("flags " + Integer.toHexString(flags), 0, delta[0]);
                assertEquals(0, delta[1]);
            }
        }
    }

    @Test public void iconsStayInsideTheHundredAndFourTileGrid() throws Exception {
        int free = 0x2 | 0x20 | 0x8; // only "right" open
        oneMovableIcon(103, 50);
        for (int trial = 0; trial < 100; trial++) assertEquals(0, wander(103, 50, free)[0]);
        oneMovableIcon(102, 50);
        int max = 0;
        for (int trial = 0; trial < 400; trial++) max = Math.max(max, wander(102, 50, free)[0]);
        assertEquals("102 -> 103 but no further", 1, max);
        int up = 0x2 | 0x80 | 0x8; // only "up" open
        oneMovableIcon(50, 103);
        for (int trial = 0; trial < 100; trial++) assertEquals(0, wander(50, 103, up)[1]);
        oneMovableIcon(50, 102);
        max = 0;
        for (int trial = 0; trial < 400; trial++) max = Math.max(max, wander(50, 102, up)[1]);
        assertEquals(1, max);
        int leftOnly = 0x2 | 0x20 | 0x80;
        oneMovableIcon(0, 50);
        for (int trial = 0; trial < 100; trial++) assertEquals(0, wander(0, 50, leftOnly)[0]);
        oneMovableIcon(1, 50);
        int min = 0;
        for (int trial = 0; trial < 400; trial++) min = Math.min(min, wander(1, 50, leftOnly)[0]);
        assertEquals(-1, min);
        int downOnly = 0x8 | 0x20 | 0x80;
        oneMovableIcon(50, 0);
        for (int trial = 0; trial < 100; trial++) assertEquals(0, wander(50, 0, downOnly)[1]);
    }

    @Test public void tenRandomDirectionAttemptsPerMovableIcon() throws Exception {
        // Only "right" is open, so dx = min(3, number of attempts that picked right); attempts are uniform
        // over four directions. E[dx] for 10 attempts is 2.17407 (9 attempts: 2.0243, 11 attempts: 2.3055).
        oneMovableIcon(50, 50);
        int free = 0x2 | 0x20 | 0x8;
        int trials = 5000;
        long sum = 0;
        for (int trial = 0; trial < trials; trial++) {
            int dx = wander(50, 50, free)[0];
            assertTrue(dx >= 0 && dx <= 3);
            sum += dx;
        }
        assertEquals(2.17407, sum / (double) trials, 0.07);
    }

    // ------------------------------------------------------------ rebuild trigger and use of the image

    @Test public void loadingStagesRebuildsTheImageOnlyWhenThePlaneDiffersFromTheLastBuiltOne() throws Exception {
        install(newDef(5));
        g.loadingStage = 2;
        g.plane = 1;
        g.minimapBuiltPlane = -1;
        Arrays.fill(map.pixels, 0x7f7f7f);
        g.loadingStages();
        assertEquals(1, g.minimapBuiltPlane);
        assertEquals("rebuilt: image cleared", 0, pixelAt(0, 0));
        Arrays.fill(map.pixels, 0x7f7f7f);
        g.loadingStages();
        assertEquals("same plane: no rebuild", 0x7f7f7f, pixelAt(0, 0));
        g.plane = 2;
        g.loadingStages();
        assertEquals(2, g.minimapBuiltPlane);
        assertEquals(0, pixelAt(0, 0));
        Arrays.fill(map.pixels, 0x7f7f7f);
        g.minimapBuiltPlane = -1;
        g.loadingStage = 0;
        g.loadingStages();
        assertEquals("only in loading stage 2", -1, g.minimapBuiltPlane);
        assertEquals(0x7f7f7f, pixelAt(0, 0));
    }

    private static final class Fixture extends java.awt.Component {
        @Override public java.awt.Image createImage(java.awt.image.ImageProducer p) { return null; }
        @Override public boolean prepareImage(java.awt.Image i, java.awt.image.ImageObserver o) { return false; }
    }

    private static final class Marker {
        final Sprite sprite;
        final int x, y;
        Marker(Sprite s, int x, int y) { sprite = s; this.x = x; this.y = y; }
    }

    @Test public void drawMinimapRotatesTheImageAroundThePlayerAndDrawsTheIconListAsMarkers() throws Exception {
        final java.util.List<Marker> markers = new java.util.ArrayList<Marker>();
        Game game = new Game() {
            @Override public void drawMinimapMarker(Sprite sprite, int offsetX, int offsetY) { markers.add(new Marker(sprite, offsetX, offsetY)); }
        };
        Object savedLineOffsets = Texture.lineOffsets;
        Player savedPlayer = Game.myPlayer;
        try {
            Fixture component = new Fixture();
            game.minimapIP = new RSImageProducer(172, 156, component);
            game.worldViewIP = new RSImageProducer(512, 334, component);
            game.minimapSprite = map;
            game.compass = new Sprite(33, 33);
            game.groundArray = new NodeList[4][104][104];
            Game.myPlayer = new Player();
            Game.myPlayer.x = 250 * 32;
            Game.myPlayer.y = 250 * 32;
            Arrays.fill(game.minimapScanlineWidths, 146);
            Arrays.fill(game.minimapScanlineStarts, 0);
            Arrays.fill(map.pixels, 0x123456);
            // 4x4 block of the tile the player stands on (tile 62,62 -> x/32 = 250 is its block centre + 0)
            int px = 250 - 2;
            assertEquals(0, px % 4);
            int tx = px / 4, ty = 103 - (464 - 250 - 2 - 48) / 4; // tile 62,62
            for (int dy = 0; dy < 4; dy++) for (int dx = 0; dx < 4; dx++) map.pixels[blockX(tx) + dx + (blockY(ty) + dy) * 512] = 0xff0000;
            Sprite a = new Sprite(2, 2), b = new Sprite(2, 2), c = new Sprite(2, 2);
            game.mapFunctionIconSprites[0] = a;
            game.mapFunctionIconSprites[1] = b;
            game.mapFunctionIconSprites[2] = c;
            game.mapFunctionIconTileX[0] = 63;
            game.mapFunctionIconTileY[0] = 61;
            game.mapFunctionIconTileX[1] = 60;
            game.mapFunctionIconTileY[1] = 70;
            game.mapFunctionIconTileX[2] = 1;
            game.mapFunctionIconTileY[2] = 1;
            game.mapFunctionIconCount = 2;
            game.drawMinimap();
            int[] raster = game.minimapIP.pixels;
            assertEquals("image rows are drawn at x=25, y=5 with 146 columns", 0x123456, raster[25 + 5 * 172]);
            assertEquals(0x123456, raster[25 + 145 + 5 * 172]);
            assertEquals(0, raster[24 + 5 * 172]);
            assertEquals(0, raster[25 + 146 + 5 * 172]);
            assertEquals("player tile block is at the centre (the white 3x3 player square covers 97..99, 78..80)", 0xff0000, raster[98 + 81 * 172]);
            assertEquals(0xff0000, raster[96 + 79 * 172]);
            assertEquals(0x123456, raster[100 + 81 * 172]);
            assertEquals(0x123456, raster[98 + 82 * 172]);
            assertEquals(0x123456, raster[95 + 79 * 172]);
            assertEquals(0x123456, raster[98 + 77 * 172]);
            assertEquals(0x123456, raster[93 + 80 * 172]);
            assertEquals("only the first mapFunctionIconCount icons", 2, markerCount(markers, a, b, c));
            assertSame(a, markers.get(0).sprite);
            assertEquals(63 * 4 + 2 - 250, markers.get(0).x);
            assertEquals(61 * 4 + 2 - 250, markers.get(0).y);
            assertSame(b, markers.get(1).sprite);
            assertEquals(60 * 4 + 2 - 250, markers.get(1).x);
            assertEquals(70 * 4 + 2 - 250, markers.get(1).y);
        } finally {
            Texture.lineOffsets = (int[]) savedLineOffsets;
            Game.myPlayer = savedPlayer;
        }
    }

    private static int markerCount(java.util.List<Marker> markers, Sprite... sprites) {
        int n = 0;
        for (Marker m : markers) for (Sprite s : sprites) if (m.sprite == s) n++;
        return n;
    }

    // ------------------------------------------------------------ ObjectDef static model scratch array

    private static Model triangleAt(int xOffset) throws Exception {
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

    @Test public void aModelArray741sHoldsTheComponentModelsOnlyWhenSeveralAreCombined() throws Exception {
        Model[] scratch = (Model[]) field(ObjectDef.class, "combinedModelParts").get(null);
        assertEquals(4, scratch.length);
        Arrays.fill(scratch, null);
        ObjectDef single = newDef(9701);
        field(ObjectDef.class, "modelIds").set(single, new int[] {8301});
        Model only = triangleAt(10);
        ObjectDef.mruNodes1.removeFromCache(only, 8301);
        Method build = ObjectDef.class.getDeclaredMethod("buildModel", int.class, int.class, int.class);
        build.setAccessible(true);
        build.invoke(single, 10, -1, 0);
        for (Model m : scratch) assertNull("one model: scratch untouched", m);

        ObjectDef several = newDef(9702);
        field(ObjectDef.class, "modelIds").set(several, new int[] {8302, 8303, 8304});
        Model[] parts = {triangleAt(100), triangleAt(200), triangleAt(300)};
        for (int n = 0; n < 3; n++) ObjectDef.mruNodes1.removeFromCache(parts[n], 8302 + n);
        Model combined = (Model) build.invoke(several, 10, -1, 0);
        assertSame(parts[0], scratch[0]);
        assertSame(parts[1], scratch[1]);
        assertSame(parts[2], scratch[2]);
        assertNull("fourth slot unused for three models", scratch[3]);
        assertEquals("combined model holds every component", 9, combined.vertexCount);
        Arrays.fill(scratch, null);
    }
}
