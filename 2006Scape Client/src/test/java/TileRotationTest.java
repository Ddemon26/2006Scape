import static org.junit.Assert.*;
import org.junit.Test;

/** Class4 8x8 block rotation helpers (method155-158), original names only. */
public class TileRotationTest {
    // tile (x,y) rotated by quarter turns: expected (x',y') per rotation
    private static int[] expectedTile(int rot, int x, int y) {
        switch (rot & 3) {
            case 0: return new int[] {x, y};
            case 1: return new int[] {y, 7 - x};
            case 2: return new int[] {7 - x, 7 - y};
            default: return new int[] {7 - y, x};
        }
    }

    @Test public void tileRotationMatchesQuarterTurnsOnEveryTile() {
        for (int rot = 0; rot < 4; rot++)
            for (int x = 0; x < 8; x++)
                for (int y = 0; y < 8; y++) {
                    int[] e = expectedTile(rot, x, y);
                    assertEquals(e[0], TileRotation.rotatedX(rot, y, x));
                    assertEquals(e[1], TileRotation.rotatedY(y, rot, x));
                    assertTrue(e[0] >= 0 && e[0] < 8 && e[1] >= 0 && e[1] < 8);
                }
    }

    @Test public void rotationIsMaskedToTwoBitsAndFourTurnsAreIdentity() {
        for (int rot : new int[] {4, 5, 6, 7, -1, -2, 9}) {
            for (int x = 0; x < 8; x++)
                for (int y = 0; y < 8; y++) {
                    assertEquals(TileRotation.rotatedX(rot & 3, y, x), TileRotation.rotatedX(rot, y, x));
                    assertEquals(TileRotation.rotatedY(y, rot & 3, x), TileRotation.rotatedY(y, rot, x));
                }
        }
        for (int x = 0; x < 8; x++)
            for (int y = 0; y < 8; y++) {
                int cx = x, cy = y;
                for (int i = 0; i < 4; i++) {
                    int nx = TileRotation.rotatedX(1, cy, cx), ny = TileRotation.rotatedY(cy, 1, cx);
                    cx = nx; cy = ny;
                }
                assertEquals(x, cx);
                assertEquals(y, cy);
            }
        assertNotEquals(TileRotation.rotatedX(1, 2, 5), 5);
    }

    @Test public void footprintFunctionsGiveMinCornerOfRotatedFootprint() {
        for (int rot = 0; rot < 4; rot++)
            for (int sx = 1; sx <= 3; sx++)
                for (int sy = 1; sy <= 3; sy++)
                    for (int x = 0; x + sx <= 8; x++)
                        for (int y = 0; y + sy <= 8; y++) {
                            int minX = 99, minY = 99;
                            for (int dx = 0; dx < sx; dx++)
                                for (int dy = 0; dy < sy; dy++) {
                                    int[] t = expectedTile(rot, x + dx, y + dy);
                                    minX = Math.min(minX, t[0]);
                                    minY = Math.min(minY, t[1]);
                                }
                            assertEquals("x rot=" + rot + " s=" + sx + "x" + sy, minX, TileRotation.rotatedFootprintMinX(rot, sy, x, y, sx));
                            assertEquals("y rot=" + rot + " s=" + sx + "x" + sy, minY, TileRotation.rotatedFootprintMinY(y, sy, rot, sx, x));
                        }
    }

    @Test public void footprintSizeOnlyMattersForRotationsThatMirrorThatAxis() {
        // rot 0: sizes ignored
        assertEquals(3, TileRotation.rotatedFootprintMinX(0, 5, 3, 4, 6));
        assertEquals(4, TileRotation.rotatedFootprintMinY(4, 5, 0, 6, 3));
        // rot 1: x' ignores sizes (= y), y' depends on sizeX only
        assertEquals(4, TileRotation.rotatedFootprintMinX(1, 5, 3, 4, 6));
        assertEquals(TileRotation.rotatedFootprintMinY(4, 1, 1, 2, 3), TileRotation.rotatedFootprintMinY(4, 7, 1, 2, 3));
        assertNotEquals(TileRotation.rotatedFootprintMinY(4, 1, 1, 2, 3), TileRotation.rotatedFootprintMinY(4, 1, 1, 3, 3));
        // rot 2: x' depends on sizeX, y' on sizeY
        assertNotEquals(TileRotation.rotatedFootprintMinX(2, 1, 3, 4, 1), TileRotation.rotatedFootprintMinX(2, 1, 3, 4, 2));
        assertNotEquals(TileRotation.rotatedFootprintMinY(4, 1, 2, 1, 3), TileRotation.rotatedFootprintMinY(4, 2, 2, 1, 3));
        // rot 3: x' depends on sizeY, y' ignores sizes (= x)
        assertNotEquals(TileRotation.rotatedFootprintMinX(3, 1, 3, 4, 1), TileRotation.rotatedFootprintMinX(3, 2, 3, 4, 1));
        assertEquals(3, TileRotation.rotatedFootprintMinY(4, 2, 3, 5, 3));
    }
}
