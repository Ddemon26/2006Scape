import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** Controlled original-name observations of Model.method443 (view transform, culling, picking). */
public class ModelProjectionPickTest {
    private static final int SENT = 123456, CX = 100, CY = 80;
    private int tex1, tex2, clipX, clipY, drawDistance, zoom;
    private boolean pickOn;
    private int pickX, pickY, pickCount;
    private int[] pickIds, savedSx, savedSy, savedDepth, saved68, saved69, saved70;
    private Model model;
    private int sin512, cos512;

    @Before public void setup() throws Exception {
        tex1 = Texture.textureInt1; tex2 = Texture.textureInt2;
        clipX = DrawingArea.clipRightHalf; clipY = DrawingArea.clipBottomHalf;
        drawDistance = WorldController.drawDistance; zoom = Game.zoom;
        pickOn = Model.pickingEnabled; pickX = Model.pickMouseX; pickY = Model.pickMouseY; pickCount = Model.pickedCount;
        pickIds = Model.pickedIds.clone();
        savedSx = ((int[]) get("screenX", null)).clone(); savedSy = ((int[]) get("screenY", null)).clone();
        savedDepth = ((int[]) get("vertexDepth", null)).clone();
        saved68 = ((int[]) get("vertexCameraX", null)).clone(); saved69 = ((int[]) get("vertexCameraY", null)).clone();
        saved70 = ((int[]) get("vertexCameraZ", null)).clone();
        Texture.textureInt1 = CX; Texture.textureInt2 = CY;
        DrawingArea.clipRightHalf = 1000; DrawingArea.clipBottomHalf = 1000;
        WorldController.drawDistance = 25; Game.zoom = 0;
        Model.pickingEnabled = false; Model.pickedCount = 0;
        sin512 = Model.sineTable16[512]; cos512 = Model.cosineTable16[512];
        Constructor<Model> ctor = Model.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        model = ctor.newInstance();
        model.vertexCount = 3; model.faceCount = 0;
        model.vertexX = new int[] {10, 0, 0};
        model.vertexY = new int[] {20, 0, 0};
        model.vertexZ = new int[] {30, 0, 0};
        model.radiusXZ = 100; model.modelHeight = 0;
        reset();
    }

    @After public void restore() throws Exception {
        Texture.textureInt1 = tex1; Texture.textureInt2 = tex2;
        DrawingArea.clipRightHalf = clipX; DrawingArea.clipBottomHalf = clipY;
        WorldController.drawDistance = drawDistance; Game.zoom = zoom;
        Model.pickingEnabled = pickOn; Model.pickMouseX = pickX; Model.pickMouseY = pickY; Model.pickedCount = pickCount;
        System.arraycopy(pickIds, 0, Model.pickedIds, 0, pickIds.length);
        System.arraycopy(savedSx, 0, get("screenX", null), 0, savedSx.length);
        System.arraycopy(savedSy, 0, get("screenY", null), 0, savedSy.length);
        System.arraycopy(savedDepth, 0, get("vertexDepth", null), 0, savedDepth.length);
        System.arraycopy(saved68, 0, get("vertexCameraX", null), 0, saved68.length);
        System.arraycopy(saved69, 0, get("vertexCameraY", null), 0, saved69.length);
        System.arraycopy(saved70, 0, get("vertexCameraZ", null), 0, saved70.length);
    }

    private void reset() throws Exception {
        for (String n : new String[] {"screenX", "screenY", "vertexDepth", "vertexCameraX", "vertexCameraY", "vertexCameraZ"})
            java.util.Arrays.fill((int[]) get(n, null), 0, 3, SENT);
        Model.pickedCount = 0;
    }

    private int[] arr(String name) throws Exception { return (int[]) get(name, null); }

    /** Draws with identity rotation and camera angles; true if the vertices were transformed (model not culled). */
    private boolean drawn(int offX, int offY, int offZ) throws Exception {
        reset();
        model.method443(0, 0, 65536, 0, 65536, offX, offY, offZ, 0);
        return arr("screenX")[0] != SENT;
    }

    @Test public void tableAssumption() {
        assertTrue(Math.abs(sin512 - 65536) <= 1); assertTrue(Math.abs(cos512) <= 1);
        assertEquals(0, Model.sineTable16[0]); assertEquals(65536, Model.cosineTable16[0]);
    }

    @Test public void identityProjectionOffsetsDepthAndCameraCoordinates() throws Exception {
        model.method443(0, 0, 65536, 0, 65536, 5, 6, 1000, 0);
        // camera-space (15, 26, 1030); depth is relative to the model centre depth 1000
        assertEquals(CX + (15 << 9) / 1030, arr("screenX")[0]);
        assertEquals(CY + (26 << 9) / 1030, arr("screenY")[0]);
        assertEquals(30, arr("vertexDepth")[0]);
        // camera arrays are only written when the model has textured faces or a vertex is behind the near plane
        assertEquals(SENT, arr("vertexCameraX")[0]);
        set("texturedFaceCount", model, 1);
        reset();
        model.method443(0, 0, 65536, 0, 65536, 5, 6, 1000, 0);
        assertEquals(15, arr("vertexCameraX")[0]);
        assertEquals(26, arr("vertexCameraY")[0]);
        assertEquals(1030, arr("vertexCameraZ")[0]);
        // second vertex (0,0,0): camera-space equals the offsets
        assertEquals(5, arr("vertexCameraX")[1]); assertEquals(6, arr("vertexCameraY")[1]); assertEquals(1000, arr("vertexCameraZ")[1]);
    }

    @Test public void modelRotationYawAndOffsetsCompose() throws Exception {
        set("texturedFaceCount", model, 1);
        // rotation 512 maps (x,y,z) to (z,y,-x): (30,20,-10); plus offsets (-1000,6,7) -> (-970,26,-3);
        // yaw 512 maps (x,z) to (z,-x): (-3,26,970); centre depth = 1000
        model.method443(512, 0, 65536, sin512, cos512, -1000, 6, 7, 0);
        // the 16-bit trig tables are off by one at 90 degrees, so allow one unit
        int cx = arr("vertexCameraX")[0], cy = arr("vertexCameraY")[0], cz = arr("vertexCameraZ")[0];
        assertTrue(cx + " vs -3", Math.abs(cx + 3) <= 1);
        assertEquals(26, cy);
        assertTrue(cz + " vs 970", Math.abs(cz - 970) <= 1);
        assertTrue(Math.abs(arr("vertexDepth")[0] + 30) <= 2);
        assertEquals(CX + (cx << 9) / cz, arr("screenX")[0]);
        assertEquals(CY + (cy << 9) / cz, arr("screenY")[0]);
    }

    @Test public void vertexBehindNearPlaneIsMarkedAndKeepsCameraCoordinates() throws Exception {
        model.vertexZ = new int[] {-951, 0, 0};   // camera z 49
        model.method443(0, 0, 65536, 0, 65536, 0, 0, 1000, 0);
        assertEquals(-5000, arr("screenX")[0]);
        assertEquals(49, arr("vertexCameraZ")[0]);
        reset();
        model.vertexZ = new int[] {-950, 0, 0};   // camera z 50
        model.method443(0, 0, 65536, 0, 65536, 0, 0, 1000, 0);
        assertEquals(CX + (10 << 9) / 50, arr("screenX")[0]);
        assertEquals(SENT, arr("vertexCameraZ")[0]);
    }

    @Test public void cullsAtTheRightAndLeftScreenEdges() throws Exception {
        DrawingArea.clipRightHalf = 100;
        // right edge: ((offX - radius) << 9) / (offZ + radius) >= clipRightHalf culls
        assertTrue(drawn(314, 0, 1000)); assertFalse(drawn(315, 0, 1000));
        // left edge: ((offX + radius) << 9) / (offZ + radius) <= -clipRightHalf culls
        assertTrue(drawn(-314, 0, 1000)); assertFalse(drawn(-315, 0, 1000));
        // a wider model reaches further: radius 200 culls only past offX - 200
        model.radiusXZ = 200;
        assertTrue(drawn(400, 0, 1000)); assertFalse(drawn(500, 0, 1000));
    }

    @Test public void cullsAtTheVerticalEdgesAndModelHeightExtendsTheTopEdge() throws Exception {
        DrawingArea.clipBottomHalf = 100;
        // vertical offset is the model centre y: (offY << 9) / (offZ + radius) >= clipBottomHalf culls at the top edge
        assertTrue(drawn(0, 214, 1000)); assertFalse(drawn(0, 215, 1000));
        assertTrue(drawn(0, -214, 1000)); assertFalse(drawn(0, -215, 1000));
        // model height extends the top edge by modelHeight, so a tall model at the same offset is still drawn
        model.modelHeight = 300;
        assertTrue(drawn(0, 400, 1000)); assertFalse(drawn(0, 600, 1000));
    }

    @Test public void cullsAtTheNearFarPlanes() throws Exception {
        // near: offZ + radius <= 50 culls
        assertTrue(drawn(0, 0, -49)); assertFalse(drawn(0, 0, -50));
        // far: centre depth >= drawDistance * 256 * (1 + zoom / 10) culls
        int far = WorldController.drawDistance * 256;
        assertTrue(drawn(0, 0, far - 1)); assertFalse(drawn(0, 0, far));
        Game.zoom = 10;
        assertTrue(drawn(0, 0, far * 2 - 1)); assertFalse(drawn(0, 0, far * 2));
    }

    @Test public void nearPlaneFlagUsesTheNearestExtentOfTheBounds() throws Exception {
        // identity pitch: nearest extent = radius; centre depth - radius <= 50 forces camera-space output
        model.radiusXZ = 100; set("texturedFaceCount", model, 0);
        reset(); model.method443(0, 0, 65536, 0, 65536, 0, 0, 150, 0);
        assertEquals(150 + 30, arr("vertexCameraZ")[0]);
        reset(); model.method443(0, 0, 65536, 0, 65536, 0, 0, 151, 0);
        assertEquals(SENT, arr("vertexCameraZ")[0]);
        // pitched 90 degrees the radius no longer reaches toward the camera but the model height does
        int sin = Model.sineTable16[512], cos = Model.cosineTable16[512];
        model.modelHeight = 900;
        reset(); model.method443(0, sin, cos, 0, 65536, 0, 1000, 0, 0);
        assertEquals(SENT, arr("vertexCameraZ")[0]);
        model.modelHeight = 960;
        reset(); model.method443(0, sin, cos, 0, 65536, 0, 1000, 0, 0);
        assertNotEquals(SENT, arr("vertexCameraZ")[0]);
    }

    /** First offsetZ in [0, 2000] (or negative direction) at which the model is culled, offsetY fixed. */
    private int firstCulledZ(int radius, int step) throws Exception {
        model.radiusXZ = radius;
        int sin = Model.sineTable16[512], cos = Model.cosineTable16[512];
        for (int z = 0; Math.abs(z) < 2000; z += step) {
            reset(); model.method443(0, sin, cos, 0, 65536, 0, 1000, z, 0);
            if (arr("screenX")[0] == SENT) return z;
        }
        return Integer.MAX_VALUE;
    }

    @Test public void pitchedRadiusWidensTheVerticalExtentOnBothEdges() throws Exception {
        DrawingArea.clipBottomHalf = 100;
        // bottom edge ((i4 + radius*pitchSine) << 9) / depth <= -clip culls as the model drops with positive offsetZ
        int small = firstCulledZ(100, 1), large = firstCulledZ(300, 1);
        assertTrue(large - small >= 198 && large - small <= 202);
        // top edge: the same radius also lifts the top by the pitched radius when moving the other way
        int smallTop = firstCulledZ(100, -1), largeTop = firstCulledZ(300, -1);
        assertTrue(smallTop - largeTop >= 198 && smallTop - largeTop <= 202);
    }

    @Test public void pickRecordsIdWhenMouseIsInsideTheProjectedBoundsOfAWholeModelPickTarget() throws Exception {
        model.modelHeight = 200;
        set("pickByBounds", model, true);
        Model.pickingEnabled = true;
        Model.pickMouseX = CX; Model.pickMouseY = CY + 20;
        model.method443(0, 0, 65536, 0, 65536, 0, 100, 1000, 77);
        assertEquals(1, Model.pickedCount); assertEquals(77, Model.pickedIds[0]);
        // each coordinate crossing the box edge stops the pick: x box [-56, 46], y box [-46, 56] around the centre
        for (int[] miss : new int[][] {{CX + 47, CY + 20}, {CX - 57, CY + 20}, {CX, CY + 57}, {CX, CY - 47}}) {
            Model.pickedCount = 0; Model.pickMouseX = miss[0]; Model.pickMouseY = miss[1];
            model.method443(0, 0, 65536, 0, 65536, 0, 100, 1000, 77);
            assertEquals("miss at " + miss[0] + "," + miss[1], 0, Model.pickedCount);
        }
        for (int[] hit : new int[][] {{CX + 45, CY + 20}, {CX - 55, CY + 20}, {CX, CY + 55}, {CX, CY - 45}}) {
            Model.pickedCount = 0; Model.pickMouseX = hit[0]; Model.pickMouseY = hit[1];
            model.method443(0, 0, 65536, 0, 65536, 0, 100, 1000, 77);
            assertEquals("hit at " + hit[0] + "," + hit[1], 1, Model.pickedCount);
        }
    }

    @Test public void pickIsOffWhenDisabledZeroIdOrNotAWholeModelTarget() throws Exception {
        model.modelHeight = 200;
        Model.pickMouseX = CX; Model.pickMouseY = CY + 20;
        set("pickByBounds", model, true);
        Model.pickingEnabled = false;
        model.method443(0, 0, 65536, 0, 65536, 0, 100, 1000, 77);
        assertEquals(0, Model.pickedCount);
        Model.pickingEnabled = true;
        model.method443(0, 0, 65536, 0, 65536, 0, 100, 1000, 0);
        assertEquals(0, Model.pickedCount);
        set("pickByBounds", model, false);   // no faces, so a per-face pick records nothing
        model.method443(0, 0, 65536, 0, 65536, 0, 100, 1000, 77);
        assertEquals(0, Model.pickedCount);
    }

    @Test public void method459SizesTheHeaderTableAndStoresTheRequester() throws Exception {
        Object savedHeaders = get("modelHeaders", null), savedRequester = get("modelRequester", null);
        try {
            OnDemandFetcherParent requester = new OnDemandFetcherParent() { @Override public void requestModel(int i) { } };
            Model.initModelHeaders(37, requester);
            assertEquals(37, ((Object[]) get("modelHeaders", null)).length);
            assertSame(requester, get("modelRequester", null));
        } finally {
            set("modelHeaders", null, savedHeaders); set("modelRequester", null, savedRequester);
        }
    }

    private static Object get(String name, Object target) throws Exception {
        Field f = Model.class.getDeclaredField(name); f.setAccessible(true); return f.get(target);
    }
    private static void set(String name, Object target, Object value) throws Exception {
        Field f = Model.class.getDeclaredField(name); f.setAccessible(true); f.set(target, value);
    }
}
