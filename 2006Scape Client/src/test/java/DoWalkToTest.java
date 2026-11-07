import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/** Behaviour of Game.doWalkTo (breadth-first walk search plus walk packet), written against the original names. */
public class DoWalkToTest {
    private static final int BLOCKED = 0x200000;
    private static final int BASE_X = 3200, BASE_Y = 3100;
    private int savedKeepAlive;
    private Game game;

    @Before public void setUp() {
        savedKeepAlive = Game.waypointsSinceKeepAlive;
        Game.waypointsSinceKeepAlive = 0;
        game = new Game();
        game.pathBackDirection = new int[104][104];
        game.aClass11Array1230 = new CollisionMap[4];
        for (int p = 0; p < 4; p++) {
            game.aClass11Array1230[p] = new CollisionMap();
            for (int[] column : game.aClass11Array1230[p].tileFlags) Arrays.fill(column, 0);
        }
        game.plane = 0;
        game.baseX = BASE_X;
        game.baseY = BASE_Y;
        game.stream = new Stream(new byte[5000]);
        game.stream.encryption = new ISAACRandomGen(new int[4]);
    }

    @After public void tearDown() {
        Game.waypointsSinceKeepAlive = savedKeepAlive;
    }

    private int[][] flags() { return game.aClass11Array1230[game.plane].tileFlags; }

    private void blockAll() { for (int[] column : flags()) Arrays.fill(column, BLOCKED); }

    private void open(int x, int y) { flags()[x][y] = 0; }

    private int walk(int mode, int orientation, int sizeY, int shapePlusOne, int startY, int sizeX, int mask,
            int targetY, int startX, boolean approximate, int targetX) {
        return game.doWalkTo(mode, orientation, sizeY, shapePlusOne, startY, sizeX, mask, targetY, startX, approximate, targetX)
                ? 1 : 0;
    }

    private int walk(int startX, int startY, int targetX, int targetY) {
        return walk(2, 0, 0, 0, startY, 0, 0, targetY, startX, false, targetX);
    }

    private static class Packet {
        int opcodeByte, length, endOffset;
        int[][] waypoints;
    }

    private Packet packet(int offset, int extraLength) {
        byte[] b = game.stream.buffer;
        Packet p = new Packet();
        p.opcodeByte = b[offset] & 0xff;
        p.length = b[offset + 1] & 0xff;
        int count = (p.length - 3 - extraLength) / 2;
        int firstX = (((b[offset + 2] & 0xff) - 128) & 0xff) | (b[offset + 3] & 0xff) << 8;
        int pos = offset + 4;
        int[] dx = new int[count], dy = new int[count];
        for (int n = 1; n < count; n++) { dx[n] = b[pos++]; dy[n] = b[pos++]; }
        int firstY = (b[pos] & 0xff) | (b[pos + 1] & 0xff) << 8;
        pos += 2;
        p.endOffset = pos + 1;
        p.waypoints = new int[count][];
        for (int n = 0; n < count; n++) {
            p.waypoints[n] = new int[] {firstX - BASE_X + dx[n], firstY - BASE_Y + dy[n]};
        }
        return p;
    }

    private Packet packet() { return packet(0, 0); }

    private static int key(int index) {
        ISAACRandomGen gen = new ISAACRandomGen(new int[4]);
        int k = 0;
        for (int n = 0; n <= index; n++) k = gen.getNextKey();
        return k;
    }

    /** Expands start -> waypoints into the tile list walked (each leg must be straight or diagonal). */
    private static List<int[]> expand(int startX, int startY, int[][] waypoints) {
        List<int[]> tiles = new ArrayList<int[]>();
        int x = startX, y = startY;
        for (int[] w : waypoints) {
            int dx = Integer.signum(w[0] - x), dy = Integer.signum(w[1] - y);
            assertTrue("leg must be straight or diagonal", w[0] == x || w[1] == y || Math.abs(w[0] - x) == Math.abs(w[1] - y));
            while (x != w[0] || y != w[1]) {
                x += dx;
                y += dy;
                tiles.add(new int[] {x, y});
            }
        }
        return tiles;
    }

    /** Independent shortest path length (8-way, diagonals need both neighbours free) over BLOCKED tiles, -1 if none. */
    private int shortest(int sx, int sy, int tx, int ty) {
        int[][] dist = new int[104][104];
        for (int[] row : dist) Arrays.fill(row, -1);
        LinkedList<int[]> queue = new LinkedList<int[]>();
        dist[sx][sy] = 0;
        queue.add(new int[] {sx, sy});
        while (!queue.isEmpty()) {
            int[] c = queue.removeFirst();
            if (c[0] == tx && c[1] == ty) return dist[tx][ty];
            for (int dx = -1; dx <= 1; dx++) for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                int nx = c[0] + dx, ny = c[1] + dy;
                if (nx < 0 || ny < 0 || nx > 103 || ny > 103 || dist[nx][ny] != -1) continue;
                if ((flags()[nx][ny] & BLOCKED) != 0) continue;
                if (dx != 0 && dy != 0 && ((flags()[nx][c[1]] & BLOCKED) != 0 || (flags()[c[0]][ny] & BLOCKED) != 0)) continue;
                dist[nx][ny] = dist[c[0]][c[1]] + 1;
                queue.add(new int[] {nx, ny});
            }
        }
        return -1;
    }

    @Test public void startAndTargetSelectTheWaypointAndTheOtherWayRound() {
        game.walkDestinationAdjusted = 1;
        assertEquals(1, walk(10, 20, 15, 20));
        Packet p = packet();
        assertEquals(98 + key(0) & 0xff, p.opcodeByte);
        assertEquals(5, p.length);
        assertArrayEquals(new int[][] {{15, 20}}, p.waypoints);
        assertEquals(15, game.destX);
        assertEquals(20, game.destY);
        assertEquals("exact path clears the adjusted flag", 0, game.walkDestinationAdjusted);
        assertEquals(p.endOffset, game.stream.currentOffset);

        game.stream.currentOffset = 0;
        assertEquals(1, walk(15, 20, 10, 20));
        assertArrayEquals(new int[][] {{10, 20}}, packet(0, 0).waypoints);
        game.stream.currentOffset = 0;
        assertEquals(1, walk(10, 20, 10, 27));
        assertArrayEquals(new int[][] {{10, 27}}, packet().waypoints);
        assertEquals(27, game.destY);
        game.stream.currentOffset = 0;
        assertEquals(1, walk(10, 20, 17, 27));
        assertArrayEquals(new int[][] {{17, 27}}, packet().waypoints);
    }

    @Test public void walkTypeSelectsOpcodeAndPayloadLength() {
        int[] opcodes = {164, 248, 98};
        int[] extra = {0, 14, 0};
        for (int mode = 0; mode < 3; mode++) {
            game.stream.currentOffset = 0;
            game.stream.encryption = new ISAACRandomGen(new int[4]);
            assertEquals(1, walk(mode, 0, 0, 0, 20, 0, 0, 27, 10, false, 17));
            Packet p = packet(0, extra[mode]);
            assertEquals(opcodes[mode] + key(0) & 0xff, p.opcodeByte);
            assertEquals(5 + extra[mode], p.length);
            assertEquals(1, p.waypoints.length);
        }
    }

    @Test public void runKeyByteFollowsKeyArraySlotFive() {
        walk(10, 20, 12, 20);
        Packet p = packet();
        assertEquals(0, game.stream.buffer[p.endOffset - 1]);
        game.stream.currentOffset = 0;
        game.keyArray[5] = 1;
        walk(10, 20, 12, 20);
        assertEquals(-1, game.stream.buffer[p.endOffset - 1]);
    }

    @Test public void detourAroundBlockedTilesIsTheShortestRoute() {
        for (int y = 5; y <= 15; y++) flags()[12][y] = BLOCKED;
        int expected = shortest(10, 10, 14, 10);
        assertTrue(expected > 4);
        assertEquals(1, walk(10, 10, 14, 10));
        Packet p = packet();
        assertTrue(p.waypoints.length > 1);
        List<int[]> tiles = expand(10, 10, p.waypoints);
        assertEquals(expected, tiles.size());
        for (int[] t : tiles) assertEquals(0, flags()[t[0]][t[1]] & BLOCKED);
        assertArrayEquals(new int[] {14, 10}, tiles.get(tiles.size() - 1));
        assertEquals(14, game.destX);
        assertEquals(10, game.destY);
    }

    @Test public void searchLeavesBackDirectionsAndDistances() {
        walk(10, 10, 13, 10);
        assertEquals(99, game.pathBackDirection[10][10]);
        assertEquals(0, game.pathDistance[10][10]);
        assertEquals(1, game.pathDistance[9][10]);
        assertEquals(2, game.pathBackDirection[9][10]);
        assertEquals(8, game.pathBackDirection[11][10]);
        assertEquals(1, game.pathBackDirection[10][9]);
        assertEquals(4, game.pathBackDirection[10][11]);
        assertEquals(3, game.pathBackDirection[9][9]);
        assertEquals(9, game.pathBackDirection[11][9]);
        assertEquals(6, game.pathBackDirection[9][11]);
        assertEquals(12, game.pathBackDirection[11][11]);
        assertEquals(1, game.pathDistance[11][11]);
        assertEquals(3, game.pathDistance[13][10]);
        assertEquals("beyond the reached tile nothing was visited", 0x5f5e0ff, game.pathDistance[40][40]);
        assertEquals(0, game.pathBackDirection[40][40]);
    }

    @Test public void searchCoversWholeGridAndStopsAtItsEdge() {
        assertEquals(1, walk(0, 0, 50, 50));
        game.stream.currentOffset = 0;
        assertEquals(1, walk(0, 0, 103, 103));
        assertArrayEquals(new int[][] {{103, 103}}, packet().waypoints);
        // unreachable target: the search floods the whole 104x104 grid
        blockAll();
        for (int x = 0; x < 104; x++) for (int y = 0; y < 104; y++) open(x, y);
        flags()[60][60] = BLOCKED;
        assertEquals(0, walk(2, 0, 0, 0, 0, 0, 0, 60, 0, false, 60));
        assertEquals(99, game.pathBackDirection[0][0]);
        assertEquals(shortest(0, 0, 103, 103), game.pathDistance[103][103]);
        assertEquals(103, game.pathDistance[103][0]);
        assertEquals(0x5f5e0ff, game.pathDistance[60][60]);
    }

    @Test public void queueWrapsAtTheLengthOfTheQueueArrays() {
        game.bigX = new int[40];
        game.bigY = new int[40];
        blockAll();
        for (int x = 2; x <= 101; x++) open(x, 5);
        assertEquals(1, walk(2, 5, 101, 5));
        assertArrayEquals(new int[][] {{101, 5}}, packet().waypoints);
        assertEquals(99, game.pathDistance[101][5]);
    }

    @Test public void unreachableTargetNeedsTheApproximateFlagToWalkNextToIt() {
        flags()[14][10] = BLOCKED;
        game.walkDestinationAdjusted = 0;
        assertEquals(0, walk(2, 0, 0, 0, 10, 0, 0, 10, 10, false, 14));
        assertEquals("nothing written when no path", 0, game.stream.currentOffset);
        assertEquals(0, game.walkDestinationAdjusted);
        assertEquals(1, walk(2, 0, 0, 0, 10, 0, 0, 10, 10, true, 14));
        Packet p = packet();
        int[] last = p.waypoints[p.waypoints.length - 1];
        assertTrue(Math.abs(last[0] - 14) <= 1 && Math.abs(last[1] - 10) <= 1);
        assertEquals(3, game.pathDistance[last[0]][last[1]]);
        assertEquals(1, game.walkDestinationAdjusted);
        assertEquals(last[0], game.destX);
        assertEquals(last[1], game.destY);
    }

    @Test public void approximationOnlyConsidersTheEightNeighboursWithinDistanceHundred() {
        // 3x3 block around the target blocked, tile two away reachable: no approximation possible
        for (int x = 19; x <= 21; x++) for (int y = 19; y <= 21; y++) flags()[x][y] = BLOCKED;
        assertEquals(0, walk(2, 0, 0, 0, 10, 0, 0, 20, 10, true, 20));
        assertEquals(0, game.walkDestinationAdjusted);
        assertEquals(0, game.stream.currentOffset);
        // reachable neighbour but only through a serpentine longer than 100 steps
        blockAll();
        for (int x = 0; x <= 102; x++) { open(x, 0); open(x, 2); }
        open(102, 1);
        open(103, 1);
        flags()[0][3] = BLOCKED;
        assertEquals(0, walk(2, 0, 0, 0, 0, 0, 0, 3, 0, true, 0));
        assertEquals(0, game.walkDestinationAdjusted);
        assertEquals(102 + 2 + 102, game.pathDistance[0][2]);
        // the same neighbour within reach is accepted
        open(1, 1);
        assertEquals(1, walk(2, 0, 0, 0, 0, 0, 0, 3, 0, true, 0));
        assertEquals(1, game.walkDestinationAdjusted);
    }

    @Test public void footprintSizesAndBlockedSidesDecideWhereTheSearchStopsBesideAnObject() {
        // object footprint starts at (20,20); sizeX is the x extent (param k1), sizeY the y extent (param k)
        assertEquals(1, walk(2, 0, 2, 0, 20, 3, 0, 20, 40, false, 20));
        assertEquals("approach from the east stops beside the third column", 23, game.destX);
        game.stream.currentOffset = 0;
        assertEquals(1, walk(2, 0, 2, 0, 40, 3, 0, 20, 20, false, 20));
        assertEquals("approach from the north stops beside the second row", 22, game.destY);
        game.stream.currentOffset = 0;
        assertEquals(1, walk(2, 0, 3, 0, 20, 2, 0, 20, 40, false, 20));
        assertEquals(22, game.destX);
        // a zero size means the search runs to the target tile itself
        game.stream.currentOffset = 0;
        assertEquals(1, walk(2, 0, 2, 0, 20, 0, 0, 20, 40, false, 20));
        assertEquals(20, game.destX);
        game.stream.currentOffset = 0;
        assertEquals(1, walk(2, 0, 0, 0, 20, 3, 0, 20, 40, false, 20));
        assertEquals(20, game.destX);
        // blocked-sides mask (param l1): bit 2 is the east side, bit 8 the west side
        game.stream.currentOffset = 0;
        assertEquals(1, walk(2, 0, 2, 0, 20, 3, 2, 20, 40, false, 20));
        assertNotEquals("east side blocked: cannot stop beside it", 23, game.destX);
        assertTrue(game.destX >= 20 && game.destX <= 22 || game.destX == 19);
        game.stream.currentOffset = 0;
        assertEquals(1, walk(2, 0, 2, 0, 20, 3, 8, 20, 40, false, 20));
        assertEquals("west side blocked: east side still reachable", 23, game.destX);
    }

    @Test public void shapePlusOneAndOrientationSelectWallReachTiles() {
        CollisionMap map = game.aClass11Array1230[0];
        for (int shape = 0; shape < 3; shape += 2) {
            for (int orientation = 0; orientation < 4; orientation++) {
                game.stream.currentOffset = 0;
                assertEquals(1, walk(2, orientation, 0, shape + 1, 30, 0, 0, 30, 10, false, 30));
                assertTrue("shape " + shape + " orientation " + orientation,
                        map.reachedWallObject(30, game.destX, game.destY, orientation, shape, 30));
            }
        }
        for (int shape = 6; shape <= 8; shape++) {
            for (int orientation = 0; orientation < 4; orientation++) {
                game.stream.currentOffset = 0;
                assertEquals(1, walk(2, orientation, 0, shape + 1, 30, 0, 0, 30, 10, false, 30));
                assertTrue(map.reachedWallDecoration(30, 30, game.destY, shape, orientation, game.destX));
            }
        }
        // without a shape the search runs to the target tile; with one it stops earlier
        game.stream.currentOffset = 0;
        walk(2, 0, 0, 0, 30, 0, 0, 30, 10, false, 30);
        assertEquals(30, game.destX);
        game.stream.currentOffset = 0;
        walk(2, 0, 0, 1, 30, 0, 0, 30, 10, false, 30);
        assertTrue(game.destX < 30);
    }

    @Test public void packetCarriesAtMostTwentyFiveWaypointsAndTracksKeepAlive() {
        blockAll();
        for (int s = 0; s < 30; s++) { open(5 + s, 5 + s); open(6 + s, 5 + s); }
        assertEquals(1, walk(5, 5, 35, 34));
        Packet p = packet();
        assertEquals(25, p.waypoints.length);
        assertEquals(2 * 25 + 3, p.length);
        assertEquals("destination is the real target even when the packet is truncated", 35, game.destX);
        assertEquals(34, game.destY);
        assertEquals(25, Game.waypointsSinceKeepAlive);
        List<int[]> tiles = expand(5, 5, p.waypoints);
        for (int[] t : tiles) assertEquals(0, flags()[t[0]][t[1]] & BLOCKED);
        assertEquals(98 + key(0) & 0xff, p.opcodeByte);

        // 90 + 25 >= 92: a keep-alive packet (opcode 36, int 0) precedes the walk packet and the counter resets
        game.stream.currentOffset = 0;
        game.stream.encryption = new ISAACRandomGen(new int[4]);
        Game.waypointsSinceKeepAlive = 90;
        assertEquals(1, walk(5, 5, 35, 34));
        byte[] b = game.stream.buffer;
        assertEquals(36 + key(0) & 0xff, b[0] & 0xff);
        assertEquals(0, b[1] | b[2] | b[3] | b[4]);
        assertEquals(98 + key(1) & 0xff, b[5] & 0xff);
        assertEquals(0, Game.waypointsSinceKeepAlive);

        // 90 + 1 < 92: no keep-alive
        game.stream.currentOffset = 0;
        game.stream.encryption = new ISAACRandomGen(new int[4]);
        Game.waypointsSinceKeepAlive = 90;
        assertEquals(1, walk(5, 5, 6, 5));
        assertEquals(98 + key(0) & 0xff, game.stream.buffer[0] & 0xff);
        assertEquals(91, Game.waypointsSinceKeepAlive);
    }

    @Test public void approximationWindowIsTargetXByTargetYAndStaleSearchDataIsCleared() {
        game.pathBackDirection[50][50] = 7;
        game.pathDistance[50][50] = 5;
        for (int dx = -1; dx <= 1; dx++) for (int dy = -1; dy <= 1; dy++) flags()[20 + dx][30 + dy] = BLOCKED;
        open(21, 30);
        flags()[50][50] = BLOCKED;
        assertEquals(1, walk(2, 0, 0, 0, 30, 0, 0, 30, 25, true, 20));
        assertEquals("stale direction code cleared", 0, game.pathBackDirection[50][50]);
        assertEquals("stale distance reset to unreached", 0x5f5e0ff, game.pathDistance[50][50]);
        assertEquals(21, game.destX);
        assertEquals(30, game.destY);
        assertEquals(1, game.walkDestinationAdjusted);
        open(21, 30);
        flags()[21][30] = BLOCKED;
        open(20, 31);
        game.stream.currentOffset = 0;
        assertEquals(1, walk(2, 0, 0, 0, 36, 0, 0, 30, 20, true, 20));
        assertEquals(20, game.destX);
        assertEquals(31, game.destY);
    }

    @Test public void searchUsesTheCollisionMapOfTheCurrentPlane() {
        game.plane = 1;
        for (int y = 0; y < 104; y++) flags()[12][y] = BLOCKED;
        assertEquals(0, walk(10, 10, 14, 10));
        game.plane = 0;
        assertEquals(1, walk(10, 10, 14, 10));
    }
}
