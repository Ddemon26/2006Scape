import java.lang.reflect.Field;
import java.util.Map;
import java.util.TreeMap;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CollisionMapTest {
    private static final int W = 0x80, N = 0x02, E = 0x08, S = 0x20, NW = 1, NE = 4, SE = 0x10, SW = 0x40;

    private static void setField(CollisionMap map, String name, int value) throws Exception {
        Field f = CollisionMap.class.getDeclaredField(name);
        f.setAccessible(true);
        f.setInt(map, value);
    }

    private static CollisionMap blank() {
        CollisionMap map = new CollisionMap();
        for (int[] column : map.tileFlags) {
            java.util.Arrays.fill(column, 0);
        }
        return map;
    }

    private static Map<String, Integer> nonZero(CollisionMap map) {
        Map<String, Integer> result = new TreeMap<String, Integer>();
        for (int x = 0; x < map.tileFlags.length; x++) {
            for (int y = 0; y < map.tileFlags[x].length; y++) {
                if (map.tileFlags[x][y] != 0) {
                    result.put(x + "," + y, map.tileFlags[x][y]);
                }
            }
        }
        return result;
    }

    private static void add(Map<String, Integer> expected, int x, int y, int bits) {
        String key = x + "," + y;
        Integer old = expected.get(key);
        expected.put(key, (old == null ? 0 : old) | bits);
    }

    // {dx, dy, bits} entries relative to the wall tile
    private static int[][] wallEntries(int shape, int orientation) {
        if (shape == 0) {
            switch (orientation) {
                case 0: return new int[][] {{0, 0, W}, {-1, 0, E}};
                case 1: return new int[][] {{0, 0, N}, {0, 1, S}};
                case 2: return new int[][] {{0, 0, E}, {1, 0, W}};
                default: return new int[][] {{0, 0, S}, {0, -1, N}};
            }
        }
        if (shape == 1 || shape == 3) {
            switch (orientation) {
                case 0: return new int[][] {{0, 0, NW}, {-1, 1, SE}};
                case 1: return new int[][] {{0, 0, NE}, {1, 1, SW}};
                case 2: return new int[][] {{0, 0, SE}, {1, -1, NW}};
                default: return new int[][] {{0, 0, SW}, {-1, -1, NE}};
            }
        }
        switch (orientation) {
            case 0: return new int[][] {{0, 0, W | N}, {-1, 0, E}, {0, 1, S}};
            case 1: return new int[][] {{0, 0, N | E}, {0, 1, S}, {1, 0, W}};
            case 2: return new int[][] {{0, 0, E | S}, {1, 0, W}, {0, -1, N}};
            default: return new int[][] {{0, 0, S | W}, {0, -1, N}, {-1, 0, E}};
        }
    }

    private static Map<String, Integer> expectedWall(int x, int y, int shape, int orientation, boolean projectile) {
        Map<String, Integer> expected = new TreeMap<String, Integer>();
        for (int[] e : wallEntries(shape, orientation)) {
            add(expected, x + e[0], y + e[1], projectile ? e[2] | e[2] << 9 : e[2]);
        }
        return expected;
    }

    @Test public void constructorAndResetMarkBordersAndInterior() throws Exception {
        CollisionMap map = new CollisionMap();
        assertEquals(104, map.tileFlags.length);
        assertEquals(104, map.tileFlags[0].length);
        assertEquals(0xffffff, map.tileFlags[0][50]);
        assertEquals(0xffffff, map.tileFlags[50][0]);
        assertEquals(0xffffff, map.tileFlags[103][50]);
        assertEquals(0xffffff, map.tileFlags[50][103]);
        assertEquals(0x1000000, map.tileFlags[1][1]);
        assertEquals(0x1000000, map.tileFlags[102][102]);
        map.tileFlags[10][10] = 5;
        map.tileFlags[0][0] = 5;
        map.reset();
        assertEquals(0x1000000, map.tileFlags[10][10]);
        assertEquals(0xffffff, map.tileFlags[0][0]);
        // non-square grid: width is the first index, height the second
        CollisionMap small = new CollisionMap();
        setField(small, "width", 5);
        setField(small, "height", 8);
        Field array = CollisionMap.class.getDeclaredField("tileFlags");
        array.setAccessible(true);
        array.set(small, new int[5][8]);
        small.reset();
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 8; y++) {
                boolean border = x == 0 || y == 0 || x == 4 || y == 7;
                assertEquals(x + "," + y, border ? 0xffffff : 0x1000000, small.tileFlags[x][y]);
            }
        }
    }

    @Test public void addWallSetsOwnAndNeighbourSides() {
        for (int shape = 0; shape < 4; shape++) {
            for (int orientation = 0; orientation < 4; orientation++) {
                for (int p = 0; p < 2; p++) {
                    CollisionMap map = blank();
                    map.addWall(60, orientation, 50, shape, p == 1);
                    assertEquals("shape " + shape + " orientation " + orientation + " projectile " + p,
                            expectedWall(50, 60, shape, orientation, p == 1), nonZero(map));
                }
            }
        }
    }

    @Test public void removeWallReversesAddWallAndKeepsOtherBits() {
        for (int shape = 0; shape < 4; shape++) {
            for (int orientation = 0; orientation < 4; orientation++) {
                for (int p = 0; p < 2; p++) {
                    CollisionMap map = blank();
                    map.addWall(60, orientation, 50, shape, p == 1);
                    map.removeWall(orientation, shape, p == 1, 50, 60);
                    assertEquals("shape " + shape + " orientation " + orientation, new TreeMap<String, Integer>(), nonZero(map));
                }
            }
        }
        CollisionMap map = blank();
        map.tileFlags[50][60] = 0x100;
        map.addWall(60, 0, 50, 0, false);
        map.removeWall(0, 0, false, 50, 60);
        assertEquals(0x100, map.tileFlags[50][60]);
        // removing without the projectile flag leaves the projectile bits
        map = blank();
        map.addWall(60, 0, 50, 0, true);
        map.removeWall(0, 0, false, 50, 60);
        assertEquals(W << 9, map.tileFlags[50][60]);
        assertEquals(E << 9, map.tileFlags[49][60]);
        // removal also clears the not-yet-loaded bit of the touched tiles
        map = new CollisionMap();
        map.addWall(60, 0, 50, 0, false);
        map.removeWall(0, 0, false, 50, 60);
        assertEquals(0, map.tileFlags[50][60]);
        assertEquals(0x1000000, map.tileFlags[51][60]);
    }

    private static Map<String, Integer> expectedObject(int x, int y, boolean projectile, int w, int h) {
        Map<String, Integer> expected = new TreeMap<String, Integer>();
        for (int i = x; i < x + w; i++) {
            for (int j = y; j < y + h; j++) {
                if (i >= 0 && i < 104 && j >= 0 && j < 104) {
                    add(expected, i, j, projectile ? 0x20100 : 0x100);
                }
            }
        }
        return expected;
    }

    @Test public void addObjectFillsFootprintWithOrientationSwap() {
        for (int orientation = 0; orientation < 4; orientation++) {
            for (int p = 0; p < 2; p++) {
                CollisionMap map = blank();
                map.addObject(p == 1, 3, 2, 50, 60, orientation);
                boolean swapped = orientation == 1 || orientation == 3;
                assertEquals("orientation " + orientation, expectedObject(50, 60, p == 1, swapped ? 2 : 3, swapped ? 3 : 2), nonZero(map));
            }
        }
        // clipped at the high edge and the low edge, x and y independently
        CollisionMap map = blank();
        map.addObject(false, 4, 3, 102, 101, 0);
        assertEquals(expectedObject(102, 101, false, 4, 3), nonZero(map));
        assertEquals(6, nonZero(map).size());
        map = blank();
        map.addObject(false, 3, 3, -1, -2, 0);
        assertEquals(expectedObject(-1, -2, false, 3, 3), nonZero(map));
        assertEquals(2, nonZero(map).size());
    }

    @Test public void removeObjectReversesAddObject() {
        for (int orientation = 0; orientation < 4; orientation++) {
            for (int p = 0; p < 2; p++) {
                CollisionMap map = blank();
                map.addObject(p == 1, 3, 2, 50, 60, orientation);
                map.removeObject(orientation, 3, 50, 60, 2, p == 1);
                assertEquals(new TreeMap<String, Integer>(), nonZero(map));
            }
        }
        CollisionMap map = blank();
        map.addObject(true, 2, 2, 50, 60, 0);
        map.removeObject(0, 2, 50, 60, 2, false);
        assertEquals(0x20000, map.tileFlags[50][60]);
        assertEquals(4, nonZero(map).size());
        // other bits of the footprint tiles survive
        map = blank();
        map.tileFlags[50][60] = 0x4;
        map.addObject(false, 1, 1, 50, 60, 0);
        map.removeObject(0, 1, 50, 60, 1, false);
        assertEquals(4, map.tileFlags[50][60]);
    }

    @Test public void blockAndUnblockTileUseYThenX() {
        CollisionMap map = blank();
        map.blockTile(60, 50);
        assertEquals(0x200000, map.tileFlags[50][60]);
        assertEquals(1, nonZero(map).size());
        map.tileFlags[50][60] |= 0x108;
        map.unblockTile(60, 50);
        assertEquals(0x108, map.tileFlags[50][60]);
        map.tileFlags[7][9] = 0x200000;
        map.unblockTile(7, 9);
        assertEquals(0x200000, map.tileFlags[7][9]);
        map.unblockTile(9, 7);
        assertEquals(0, map.tileFlags[7][9]);
    }

    @Test public void originShiftsArrayIndexForAllEdits() throws Exception {
        CollisionMap map = blank();
        setField(map, "originX", 10);
        setField(map, "originY", 20);
        map.addWall(80, 0, 60, 0, false);
        assertEquals(W, map.tileFlags[50][60]);
        assertEquals(E, map.tileFlags[49][60]);
        map.removeWall(0, 0, false, 60, 80);
        assertEquals(0, nonZero(map).size());
        map.addObject(false, 2, 3, 60, 80, 0);
        assertEquals(6, nonZero(map).size());
        assertEquals(0x100, map.tileFlags[50][60]);
        assertEquals(0x100, map.tileFlags[51][62]);
        map.removeObject(0, 2, 60, 80, 3, false);
        assertEquals(0, nonZero(map).size());
        map.blockTile(80, 60);
        assertEquals(0x200000, map.tileFlags[50][60]);
        map.unblockTile(80, 60);
        assertEquals(0, nonZero(map).size());
        // the bounds clip applies after the origin is subtracted
        map.addObject(false, 2, 2, 9, 19, 0);
        assertEquals(1, nonZero(map).size());
        assertEquals(0x100, map.tileFlags[0][0]);
        // x is clipped by the first dimension, y by the second
        map = blank();
        setField(map, "width", 6);
        setField(map, "height", 9);
        map.addObject(false, 10, 10, 0, 0, 0);
        assertEquals(6 * 9, nonZero(map).size());
        assertEquals(0x100, map.tileFlags[5][8]);
    }

    private static final int[] DX = {-1, 0, 1, 0};
    private static final int[] DY = {0, 1, 0, -1};
    private static final String SIDES = "WNES";
    // flag of a neighbour tile that faces the target, for a neighbour on the given side of the target
    private static final int[] FACING = {E, S, W, N};
    private static final int TILE_UNUSABLE = 0x1280100;

    private interface Reach {
        boolean test(CollisionMap map, int x, int y);
    }

    // spec: upper case = side reachable only when the neighbour's facing flag (and, if tileBitsToo, the
    // tile-unusable bits) are clear; lower case = always reachable; absent side = never reachable
    private static void checkNeighbours(String label, Reach reach, String spec, boolean tileBitsToo, int cx, int cy) {
        CollisionMap map = blank();
        for (int dx = -3; dx <= 3; dx++) {
            for (int dy = -3; dy <= 3; dy++) {
                int side = -1;
                for (int s = 0; s < 4; s++) {
                    if (DX[s] == dx && DY[s] == dy) {
                        side = s;
                    }
                }
                boolean expected = dx == 0 && dy == 0 || side >= 0 && spec.toUpperCase().indexOf(SIDES.charAt(side)) >= 0;
                assertEquals(label + " clean " + dx + "," + dy, expected, reach.test(map, cx + dx, cy + dy));
            }
        }
        for (int s = 0; s < 4; s++) {
            char c = SIDES.charAt(s);
            int tx = cx + DX[s], ty = cy + DY[s];
            if (spec.indexOf(c) >= 0) {
                int mask = FACING[s] | (tileBitsToo ? TILE_UNUSABLE : 0);
                for (int bit = 1; bit <= 0x1000000; bit <<= 1) {
                    map = blank();
                    map.tileFlags[tx][ty] = bit;
                    assertEquals(label + " side " + c + " bit " + Integer.toHexString(bit), (mask & bit) == 0, reach.test(map, tx, ty));
                }
            } else if (spec.indexOf(Character.toLowerCase(c)) >= 0) {
                map = blank();
                map.tileFlags[tx][ty] = 0x1ffffff;
                assertTrue(label + " unconditional " + c, reach.test(map, tx, ty));
            }
        }
    }

    @Test public void reachedWallObjectNeighbourTables() {
        final int tx = 50, ty = 60;
        String[] straight = {"wNS", "WnE", "NeS", "WEs"};
        String[] corner = {"wnES", "WneS", "WNes", "wNEs"};
        for (int orientation = 0; orientation < 4; orientation++) {
            final int or = orientation;
            checkNeighbours("shape 0 ori " + orientation, new Reach() {
                public boolean test(CollisionMap m, int x, int y) {
                    return m.reachedWallObject(tx, x, y, or, 0, ty);
                }
            }, straight[orientation], true, tx, ty);
            checkNeighbours("shape 2 ori " + orientation, new Reach() {
                public boolean test(CollisionMap m, int x, int y) {
                    return m.reachedWallObject(tx, x, y, or, 2, ty);
                }
            }, corner[orientation], true, tx, ty);
            checkNeighbours("shape 9 ori " + orientation, new Reach() {
                public boolean test(CollisionMap m, int x, int y) {
                    return m.reachedWallObject(tx, x, y, or, 9, ty);
                }
            }, "WNES", false, tx, ty);
        }
        // shapes with no table never reach a neighbour
        for (int shape : new int[] {1, 3, 4, 5, 8, 10}) {
            assertFalse("shape " + shape, blank().reachedWallObject(tx, tx - 1, ty, 0, shape, ty));
            assertTrue("same tile, shape " + shape, blank().reachedWallObject(tx, tx, ty, 0, shape, ty));
        }
    }

    @Test public void reachedWallDecorationNeighbourTables() {
        final int tx = 50, ty = 60;
        String[] diagonal = {"ES", "WS", "WN", "EN"};
        for (int orientation = 0; orientation < 4; orientation++) {
            final int or = orientation;
            checkNeighbours("shape 6 ori " + orientation, new Reach() {
                public boolean test(CollisionMap m, int x, int y) {
                    return m.reachedWallDecoration(tx, ty, y, 6, or, x);
                }
            }, diagonal[orientation], false, tx, ty);
            checkNeighbours("shape 7 ori " + orientation, new Reach() {
                public boolean test(CollisionMap m, int x, int y) {
                    return m.reachedWallDecoration(tx, ty, y, 7, or, x);
                }
            }, diagonal[(orientation + 2) & 3], false, tx, ty);
            checkNeighbours("shape 8 ori " + orientation, new Reach() {
                public boolean test(CollisionMap m, int x, int y) {
                    return m.reachedWallDecoration(tx, ty, y, 8, or, x);
                }
            }, "WNES", false, tx, ty);
        }
        for (int shape : new int[] {0, 4, 5, 9}) {
            assertFalse("shape " + shape, blank().reachedWallDecoration(tx, ty, ty, shape, 0, tx + 1));
            assertTrue("same tile shape " + shape, blank().reachedWallDecoration(tx, ty, ty, shape, 0, tx));
        }
    }

    private static boolean footprint(CollisionMap m, int tx, int ty, int sizeX, int sizeY, int mask, int cx, int cy) {
        return m.reachedObjectFootprint(ty, tx, cx, sizeY, mask, sizeX, cy);
    }

    @Test public void reachedObjectFootprintSides() {
        final int tx = 50, ty = 60, sizeX = 2, sizeY = 3;
        for (int cx = tx - 2; cx <= tx + sizeX + 1; cx++) {
            for (int cy = ty - 2; cy <= ty + sizeY + 1; cy++) {
                boolean inside = cx >= tx && cx < tx + sizeX && cy >= ty && cy < ty + sizeY;
                boolean west = cx == tx - 1 && cy >= ty && cy < ty + sizeY;
                boolean east = cx == tx + sizeX && cy >= ty && cy < ty + sizeY;
                boolean south = cy == ty - 1 && cx >= tx && cx < tx + sizeX;
                boolean north = cy == ty + sizeY && cx >= tx && cx < tx + sizeX;
                assertEquals(cx + "," + cy, inside || west || east || south || north, footprint(blank(), tx, ty, sizeX, sizeY, 0, cx, cy));
                if (inside) {
                    // inside the footprint, flags and mask are ignored
                    CollisionMap map = blank();
                    map.tileFlags[cx][cy] = 0x1ffffff;
                    assertTrue(footprint(map, tx, ty, sizeX, sizeY, 15, cx, cy));
                }
            }
        }
        // {walker x, walker y, blocking flag bit on the walker tile, blocking mask bit}
        int[][] sides = {{tx - 1, ty + 1, E, 8}, {tx + sizeX, ty + 1, W, 2}, {tx, ty - 1, N, 4}, {tx + 1, ty + sizeY, S, 1}};
        for (int[] side : sides) {
            for (int bit = 1; bit <= 0x1000000; bit <<= 1) {
                CollisionMap map = blank();
                map.tileFlags[side[0]][side[1]] = bit;
                assertEquals("flag " + Integer.toHexString(bit), bit != side[2], footprint(map, tx, ty, sizeX, sizeY, 0, side[0], side[1]));
            }
            for (int bit = 1; bit <= 8; bit <<= 1) {
                assertEquals("mask " + bit, bit != side[3], footprint(blank(), tx, ty, sizeX, sizeY, bit, side[0], side[1]));
            }
            assertFalse(footprint(blank(), tx, ty, sizeX, sizeY, 15, side[0], side[1]));
        }
    }

    @Test public void reachingUsesOriginOnlyForArrayIndex() throws Exception {
        CollisionMap map = blank();
        setField(map, "originX", 10);
        setField(map, "originY", 20);
        // world walker (59,80) west of wall target (60,80) at orientation 1 is conditional on its E flag
        assertTrue(map.reachedWallObject(60, 59, 80, 1, 0, 80));
        map.tileFlags[49][60] = E;
        assertFalse(map.reachedWallObject(60, 59, 80, 1, 0, 80));
        map.tileFlags[49][60] = 0;
        assertTrue(map.reachedWallDecoration(60, 80, 80, 8, 0, 61));
        map.tileFlags[51][60] = W;
        assertFalse(map.reachedWallDecoration(60, 80, 80, 8, 0, 61));
        map.tileFlags[51][60] = 0;
        assertTrue(footprint(map, 60, 80, 2, 2, 0, 59, 80));
        map.tileFlags[49][60] = E;
        assertFalse(footprint(map, 60, 80, 2, 2, 0, 59, 80));
        // the footprint range itself is in world coordinates
        assertTrue(footprint(map, 60, 80, 2, 2, 0, 61, 81));
        assertFalse(footprint(map, 60, 80, 2, 2, 0, 51, 61));
    }
}
