import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/** Controlled original-name observations of Animable.drawFromCamera and Model.drawFromCamera (draws a model's faces to the raster from a camera view). */
public class AnimableModelDrawTest {
    private static final int W = 200, H = 160, BG = 0x7f7f7f;
    private final WorldControllerDrawTest base = new WorldControllerDrawTest();
    private int[] px;
    private int savedZoom, savedLowMem;
    private boolean savedBlock;
    private Model model;

    private static final class Holder extends Animable {
        Model held;
        Holder(Model m) { held = m; }
        @Override Model getRotatedModel() { return held; }
    }

    @Before public void setUp() throws Exception {
        base.save();
        savedZoom = Game.zoom; Game.zoom = 0;
        px = new int[W * H];
        Arrays.fill(px, BG);
        DrawingArea.bindRaster(H, W, px);
        Texture.setScreenSize(W, H);
        Texture.transparency = 0;
        Constructor<Model> ctor = Model.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        model = ctor.newInstance();
        model.vertexCount = 3; model.faceCount = 1;
        model.vertexX = new int[] {-100, 0, 100};
        model.vertexY = new int[] {-100, 100, -100};
        model.vertexZ = new int[] {0, 0, 0};
        model.radiusXZ = 150; model.modelHeight = 321;
        set("depthBucketOffset", 200); set("depthBucketCount", 400);
        set("faceVertexA", new int[] {0}); set("faceVertexB", new int[] {1}); set("faceVertexC", new int[] {2});
        set("faceShadeA", new int[] {0x1234}); set("faceShadeB", new int[] {0x1234}); set("faceShadeC", new int[] {0x1234});
        set("faceRenderFlags", new int[] {1});
        set("faceColors", new int[] {0});
        set("faceNearPlaneClipped", new boolean[4096]); set("faceNeedsHorizontalClip", new boolean[4096]);
    }

    @After public void tearDown() throws Exception { Game.zoom = savedZoom; base.restore(); }

    private void set(String name, Object v) throws Exception {
        Field f = Model.class.getDeclaredField(name); f.setAccessible(true);
        if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) {
            if (!(v instanceof boolean[])) f.set(null, v);
        } else f.set(model, v);
    }

    private int painted() { int n = 0; for (int p : px) if (p != BG) n++; return n; }

    @Test public void nullModelDrawsNothingAndKeepsHeight() {
        Holder h = new Holder(null);
        h.drawFromCamera(0, 0, 65536, 0, 65536, 0, 0, 1000, 0);
        assertEquals(0, painted());
        assertEquals(1000, h.modelHeight);
    }

    @Test public void drawsTheHeldModelsFacesAndCopiesItsHeight() {
        Holder h = new Holder(model);
        h.drawFromCamera(0, 0, 65536, 0, 65536, 0, 0, 1000, 0);
        assertEquals(321, h.modelHeight);
        assertNotEquals(BG, px[(H / 2) * W + W / 2]);   // triangle covers the screen centre
        assertEquals(0x1234, px[(H / 2) * W + W / 2]);
        assertEquals(BG, px[5 * W + 5]);                // far corner untouched
        assertTrue(painted() > 500);
    }

    @Test public void offsetsMoveTheDrawnImageAndOffscreenModelsAreCulled() {
        Holder h = new Holder(model);
        h.drawFromCamera(0, 0, 65536, 0, 65536, 0, 0, 1000, 0);
        int[] centred = px.clone();
        Arrays.fill(px, BG);
        h.drawFromCamera(0, 0, 65536, 0, 65536, 400, 0, 1000, 0);   // shifted right on screen
        assertFalse(Arrays.equals(centred, px));
        assertEquals(BG, px[(H / 2) * W + W / 2]);
        Arrays.fill(px, BG);
        h.drawFromCamera(0, 0, 65536, 0, 65536, 0, 0, -1000, 0);    // behind the camera
        assertEquals(0, painted());
        Arrays.fill(px, BG);
        h.drawFromCamera(0, 0, 65536, 0, 65536, 0, 0, 1000, 0);
        assertArrayEquals(centred, px);
    }

    @Test public void modelAndAnimableWrapperDrawIdentically() {
        Holder h = new Holder(model);
        h.drawFromCamera(0, 0, 65536, 0, 65536, 30, -20, 900, 0);
        int[] viaAnimable = px.clone();
        Arrays.fill(px, BG);
        model.drawFromCamera(0, 0, 65536, 0, 65536, 30, -20, 900, 0);
        assertArrayEquals(viaAnimable, px);
        assertTrue(painted() > 0);
    }
}
