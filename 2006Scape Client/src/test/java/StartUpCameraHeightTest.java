import static org.junit.Assert.*;
import org.junit.*;

/** Building blocks of the startUp camera-height table passed to WorldController.buildTileVisibilityTable. */
public class StartUpCameraHeightTest {
    private int[] texSine, texCos, modelSine, modelCos;
    private static int[] real(boolean sine) {
        int[] t = new int[2048];
        for (int k = 0; k < 2048; k++) t[k] = (int) (65536D * (sine ? Math.sin(k * 0.0030679614999999999D) : Math.cos(k * 0.0030679614999999999D)));
        return t;
    }
    @Before public void save() { texSine = Texture.sineTable; texCos = Texture.cosineTable; modelSine = Model.sineTable16; modelCos = Model.cosineTable16; }
    @After public void restore() { Texture.sineTable = texSine; Texture.cosineTable = texCos; Model.sineTable16 = modelSine; Model.cosineTable16 = modelCos; }

    /** Texture.sineTable is the sine of a 2048-unit angle, scaled by 65536 (not cosine, not a 360-degree angle). */
    @Test public void textureSineTableIsSineOf2048UnitAngleIn16Point16() {
        int[] s = Texture.sineTable;
        assertEquals(2048, s.length);
        assertEquals(0, s[0]);
        assertEquals(65536, s[512], 1);
        assertEquals(-65536, s[1536], 1);
        assertEquals(46340, s[256], 1);
        assertNotEquals(Texture.cosineTable[0], s[0]);
        for (int n = 0; n < 9; n++) {
            int p = 128 + n * 32 + 15;
            assertEquals("pitch " + p, 65536D * Math.sin(p * Math.PI / 1024D), s[p], 1D);
            assertTrue("rising over the pitch range", n == 0 || s[p] > s[p - 32]);
        }
    }

    /** Original setCameraPos(distance, pitch, x, z, yaw, y): the camera ends up distance*sin(pitch)>>16 above the focus z, independent of yaw. */
    @Test public void setCameraPosPlacesCameraDistanceTimesPitchSineAboveFocus() {
        Texture.sineTable = real(true); Texture.cosineTable = real(false);
        Model.sineTable16 = Texture.sineTable; Model.cosineTable16 = Texture.cosineTable;
        Game g = new Game();
        int[][] cases = {{600 + 143 * 3, 143}, {600 + 399 * 3, 399}, {1000, 143}, {2000, 143}, {1000, 271}};
        for (int[] c : cases) {
            for (int yaw : new int[] {0, 300, 1024, 1900}) {
                g.setCameraPos(c[0], c[1], 1000, -500, yaw, 2000);
                int height = -500 - g.zCameraPos;
                int expected = c[0] * Texture.sineTable[c[1]] >> 16;
                assertEquals("distance " + c[0] + " pitch " + c[1] + " yaw " + yaw, expected, height, 1);
                assertEquals("pitch stored", c[1], g.yCameraCurve);
            }
        }
        // counterexample: the height follows the sine, not the cosine, of the pitch
        g.setCameraPos(1000, 143, 0, 0, 0, 0);
        assertTrue(Math.abs(-g.zCameraPos - (1000 * Texture.cosineTable[143] >> 16)) > 100);
        // distance alone scales the height: doubling distance doubles it (within rounding)
        g.setCameraPos(1000, 271, 0, 0, 0, 0); int h1 = -g.zCameraPos;
        g.setCameraPos(2000, 271, 0, 0, 0, 0); int h2 = -g.zCameraPos;
        assertEquals(2 * h1, h2, 2);
    }

    /** The render camera distance is 600 + pitch * zoom and the default zoom is 3. */
    @Test public void defaultZoomIsThree() {
        assertEquals(3, Game.zoom);
    }
}
