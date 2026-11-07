import static org.junit.Assert.*;

import java.util.Arrays;
import org.junit.Test;

/** Original-name semantic tests for the ShapedTile constructor (19 int parameters, positional) and its shape tables. */
public class Class40ShapeTest {
    // Positions of the constructor arguments (original parameter order i,j,k,l,i1,j1,k1,l1,i2,j2,k2,l2,i3,j3,k3,l3,i4,k4,l4).
    static final int I = 0, J = 1, K = 2, L = 3, I1 = 4, J1 = 5, K1 = 6, L1 = 7, I2 = 8, J2 = 9, K2 = 10, L2 = 11, I3 = 12, J3 = 13, K3 = 14, L3 = 15, I4 = 16, K4 = 17, L4 = 18;
    // Corner 0..3 values come from these argument positions (experiment roles, established by the tests below).
    static final int[] HEIGHT = {I3, L2, L, K2};
    static final int[] LOW = {L1, I4, J2, K};
    static final int[] HIGH = {J, L3, J1, K3};

    static final int[][] VERTS = {{1, 3, 5, 7}, {1, 3, 5, 7}, {1, 3, 5, 7}, {1, 3, 5, 7, 6}, {1, 3, 5, 7, 6}, {1, 3, 5, 7, 6}, {1, 3, 5, 7, 6}, {1, 3, 5, 7, 2, 6}, {1, 3, 5, 7, 2, 8}, {1, 3, 5, 7, 2, 8}, {1, 3, 5, 7, 11, 12}, {1, 3, 5, 7, 11, 12}, {1, 3, 5, 7, 13, 14}};
    static final int[][] FACES = {{0, 1, 2, 3, 0, 0, 1, 3}, {1, 1, 2, 3, 1, 0, 1, 3}, {0, 1, 2, 3, 1, 0, 1, 3}, {0, 0, 1, 2, 0, 0, 2, 4, 1, 0, 4, 3}, {0, 0, 1, 4, 0, 0, 4, 3, 1, 1, 2, 4}, {0, 0, 4, 3, 1, 0, 1, 2, 1, 0, 2, 4}, {0, 1, 2, 4, 1, 0, 1, 4, 1, 0, 4, 3}, {0, 4, 1, 2, 0, 4, 2, 5, 1, 0, 4, 5, 1, 0, 5, 3}, {0, 4, 1, 2, 0, 4, 2, 3, 0, 4, 3, 5, 1, 0, 4, 5}, {0, 0, 4, 5, 1, 4, 1, 2, 1, 4, 2, 3, 1, 4, 3, 5}, {0, 0, 1, 5, 0, 1, 4, 5, 0, 1, 2, 4, 1, 0, 5, 3, 1, 5, 4, 3, 1, 4, 2, 3}, {1, 0, 1, 5, 1, 1, 4, 5, 1, 1, 2, 4, 0, 0, 5, 3, 0, 5, 4, 3, 0, 4, 2, 3}, {1, 0, 5, 4, 1, 0, 1, 5, 0, 0, 4, 3, 0, 4, 5, 3, 0, 5, 2, 3, 0, 1, 2, 5}};
    // Point id -> {x offset, y offset, first corner averaged, second corner averaged}.
    static final int[][] POINT = {null, {0, 0, 0, 0}, {64, 0, 0, 1}, {128, 0, 1, 1}, {128, 64, 1, 2}, {128, 128, 2, 2}, {64, 128, 2, 3}, {0, 128, 3, 3}, {0, 64, 3, 0},
            {64, 32, 0, 1}, {96, 64, 1, 2}, {64, 96, 2, 3}, {32, 64, 3, 0}, {32, 32, 0, 0}, {96, 32, 1, 1}, {96, 96, 2, 2}, {32, 96, 3, 3}};

    static int[] base() {
        return new int[] {25, 101, 103, 20, 7, 105, 0, 107, 4321, 109, 40, 60, 10, 0, 111, 113, 115, 30, 8765};
    }

    static ShapedTile make(int[] a) {
        return new ShapedTile(a[I], a[J], a[K], a[L], a[I1], a[J1], a[K1], a[L1], a[I2], a[J2], a[K2], a[L2], a[I3], a[J3], a[K3], a[L3], a[I4], a[K4], a[L4]);
    }

    /** One quarter turn per step: (x, y) -> (y, 128 - x). */
    static int[] rotate(int x, int y, int times) {
        for (int t = 0; t < times; t++) { int nx = y, ny = 128 - x; x = nx; y = ny; }
        return new int[] {x, y};
    }

    static int idAt(int x, int y) {
        for (int id = 1; id <= 16; id++) if (POINT[id][0] == x && POINT[id][1] == y) return id;
        throw new AssertionError("no point at " + x + "," + y);
    }

    static int compose(int[] a, int[] role, int id) { return (a[role[POINT[id][2]]] + a[role[POINT[id][3]]]) >> 1; }

    private static void check(int[] a) {
        int shape = a[J3], rot = a[K1];
        ShapedTile t = make(a);
        int[] ids = VERTS[shape];
        assertEquals(ids.length, t.vertexX.length);
        assertEquals(ids.length, t.vertexY.length);
        assertEquals(ids.length, t.vertexHeight.length);
        String ctx = "shape " + shape + " rotation " + rot;
        for (int k = 0; k < ids.length; k++) {
            int[] p = rotate(POINT[ids[k]][0], POINT[ids[k]][1], ids[k] == 1 || ids[k] == 3 || ids[k] == 5 || ids[k] == 7 ? 0 : rot); // corner vertices stay in place
            assertEquals(ctx + " vertex " + k + " x", a[K4] * 128 + p[0], t.vertexX[k]);
            assertEquals(ctx + " vertex " + k + " y", a[I] * 128 + p[1], t.vertexY[k]);
            assertEquals(ctx + " vertex " + k + " height", compose(a, HEIGHT, idAt(p[0], p[1])), t.vertexHeight[k]);
        }
        int[] faces = FACES[shape];
        int n = faces.length / 4;
        assertEquals(n, t.faceVertexA.length);
        assertEquals(n, t.faceColourA.length);
        for (int f = 0; f < n; f++) {
            boolean high = faces[f * 4] != 0;
            int[] vs = {t.faceVertexA[f], t.faceVertexB[f], t.faceVertexC[f]};
            int[] cols = {t.faceColourA[f], t.faceColourB[f], t.faceColourC[f]};
            for (int c = 0; c < 3; c++) {
                int id0 = ids[faces[f * 4 + 1 + c]];
                int[] p = rotate(POINT[id0][0], POINT[id0][1], rot);
                assertEquals(ctx + " face " + f + " corner " + c + " x", a[K4] * 128 + p[0], t.vertexX[vs[c]]);
                assertEquals(ctx + " face " + f + " corner " + c + " y", a[I] * 128 + p[1], t.vertexY[vs[c]]);
                assertEquals(ctx + " face " + f + " corner " + c + " colour", compose(a, high ? HIGH : LOW, idAt(p[0], p[1])), cols[c]);
            }
            if (t.faceTexture != null) assertEquals(ctx + " face " + f + " texture", high ? a[I1] : -1, t.faceTexture[f]);
        }
        if (a[I1] == -1) assertNull(t.faceTexture); else assertNotNull(t.faceTexture);
        assertEquals(shape, t.shape);
        assertEquals(rot, t.rotation);
        assertEquals(a[I2], t.underlayMapColour);
        assertEquals(a[L4], t.overlayMapColour);
    }

    @Test public void everyShapeAndRotationPlacesVerticesFacesColoursAndTexturesFromTheArguments() {
        for (int shape = 0; shape < 13; shape++) for (int rot = 0; rot < 4; rot++) {
            int[] a = base(); a[J3] = shape; a[K1] = rot; check(a);
            a[I1] = 9; check(a);
        }
    }

    @Test public void eachArgumentMovesOnlyTheOutputsItClaims() {
        int[] a = base(); a[J3] = 12;
        ShapedTile ref = make(a);
        for (int p : new int[] {I, K4}) {
            int[] b = a.clone(); b[p] += 3;
            ShapedTile t = make(b);
            for (int k = 0; k < ref.vertexX.length; k++) {
                assertEquals(ref.vertexX[k] + (p == K4 ? 384 : 0), t.vertexX[k]);
                assertEquals(ref.vertexY[k] + (p == I ? 384 : 0), t.vertexY[k]);
                assertEquals(ref.vertexHeight[k], t.vertexHeight[k]);
            }
            assertArrayEquals(ref.faceColourA, t.faceColourA);
        }
        for (int p : HEIGHT) {
            int[] b = a.clone(); b[p] += 100;
            ShapedTile t = make(b);
            assertFalse(Arrays.equals(ref.vertexHeight, t.vertexHeight));
            assertArrayEquals(ref.faceColourA, t.faceColourA); assertArrayEquals(ref.faceColourB, t.faceColourB); assertArrayEquals(ref.faceColourC, t.faceColourC);
        }
        for (int[] role : new int[][] {LOW, HIGH}) for (int p : role) {
            boolean changed = false;
            for (int shape = 0; shape < 13; shape++) {
                int[] c = a.clone(); c[J3] = shape;
                int[] b = c.clone(); b[p] += 2000;
                ShapedTile r = make(c), t = make(b);
                assertArrayEquals(r.vertexHeight, t.vertexHeight);
                assertArrayEquals(r.vertexX, t.vertexX);
                changed |= !(Arrays.equals(r.faceColourA, t.faceColourA) && Arrays.equals(r.faceColourB, t.faceColourB) && Arrays.equals(r.faceColourC, t.faceColourC));
            }
            assertTrue("argument " + p + " reaches face colours", changed);
        }
    }

    @Test public void flatIsTrueExactlyWhenAllFourHeightArgumentsAreEqual() {
        int[] a = base(); a[J3] = 2;
        for (int v : new int[] {0, 17}) { for (int p : HEIGHT) a[p] = v; assertTrue(make(a).flat); }
        for (int q : HEIGHT) { int[] b = a.clone(); b[q] += 1; assertFalse("only height arg " + q + " differs", make(b).flat); }
        assertFalse(make(base()).flat);
    }
}
