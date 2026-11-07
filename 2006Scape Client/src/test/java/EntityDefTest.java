import java.io.ByteArrayOutputStream;
import java.lang.reflect.*;
import java.util.*;
import org.junit.*;
import static org.junit.Assert.*;

/** Pass 144: semantic tests for EntityDef.method160 / method164, written against the original identifiers only. */
public class EntityDefTest {
    private static Field f(Class<?> c, String n) throws Exception { Field x = c.getDeclaredField(n); x.setAccessible(true); return x; }
    private static void seti(Object o, String n, int v) throws Exception { f(o.getClass(), n).setInt(o, v); }
    private static void set(Object o, String n, Object v) throws Exception { f(o.getClass(), n).set(o, v); }
    private static void sset(Class<?> c, String n, Object v) throws Exception { f(c, n).set(null, v); }
    private static Object sget(Class<?> c, String n) throws Exception { return f(c, n).get(null); }
    private static EntityDef newDef() throws Exception { Constructor<EntityDef> c = EntityDef.class.getDeclaredConstructor(); c.setAccessible(true); return c.newInstance(); }
    private static int[] xs(Model m) { return Arrays.copyOf(m.vertexX, m.vertexCount); }
    private static int[] ys(Model m) { return Arrays.copyOf(m.vertexY, m.vertexCount); }
    private static int[] zs(Model m) { return Arrays.copyOf(m.vertexZ, m.vertexCount); }
    private static int[] sh(Model m, int n) throws Exception { return (int[]) f(Model.class, "faceShade" + "ABC".charAt(n - 4)).get(m); }

    /** Triangle (X,0,0) (X+10,0,0) (X,10,10) with X=20+id; one face of colour `color`. */
    static byte[] triangle(int id, int color) {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        int x = 20 + id;
        for (int i = 0; i < 3; i++) o.write(7);
        o.write(1);
        o.write(0 + 64); o.write(1 + 64); o.write(1 + 64);
        o.write(color >> 8); o.write(color);
        o.write(x + 64); o.write(10 + 64); o.write(-10 + 64);
        o.write(64); o.write(64); o.write(10 + 64);
        o.write(64); o.write(64); o.write(10 + 64);
        o.write(0); o.write(3); o.write(0); o.write(1);
        o.write(0); o.write(0); o.write(0); o.write(0); o.write(0); o.write(0);
        o.write(0); o.write(3); o.write(0); o.write(3); o.write(0); o.write(3);
        o.write(0); o.write(3);
        return o.toByteArray();
    }
    static final class Spy extends OnDemandFetcherParent {
        final List<Integer> requested = new ArrayList<Integer>();
        @Override public void requestModel(int i) { requested.add(i); }
    }
    private Object savedArr, savedFetcher; private Spy spy;
    @Before public void setUp() throws Exception {
        savedArr = sget(Model.class, "modelHeaders"); savedFetcher = sget(Model.class, "modelRequester");
        spy = new Spy(); Model.initModelHeaders(64, spy); EntityDef.mruNodes.clear();
    }
    @After public void tearDown() throws Exception {
        sset(Model.class, "modelHeaders", savedArr); sset(Model.class, "modelRequester", savedFetcher); EntityDef.mruNodes.clear();
    }

    // ---------------- method160 ----------------
    @Test public void method160IsNullWithoutAModelListAndRequestsEveryMissingModel() throws Exception {
        EntityDef d = newDef();
        assertNull(d.getHeadModel());
        set(d, "headModelIds", new int[]{5, 6, 7});
        Model.loadModelHeader(triangle(6, 40000), 6);
        assertNull(d.getHeadModel());                                  // 5 and 7 absent: nothing built, even though 6 is loaded
        assertEquals(Arrays.asList(5, 7), spy.requested);
    }
    @Test public void method160ReturnsTheSingleModelOrTheListConcatenatedInOrderAndIsNotLit() throws Exception {
        Model.loadModelHeader(triangle(5, 40000), 5); Model.loadModelHeader(triangle(6, 40000), 6);
        EntityDef d = newDef();
        set(d, "headModelIds", new int[]{5});
        Model one = d.getHeadModel();
        assertArrayEquals(xs(Model.getModel(5)), xs(one));
        assertNull(sh(one, 4));                                    // no lighting applied
        set(d, "headModelIds", new int[]{6, 5});
        Model two = d.getHeadModel();
        assertArrayEquals(new int[]{26, 36, 26, 25, 35, 25}, xs(two));
        assertNotSame(two, d.getHeadModel());                         // built fresh each call (not cached)
        EntityDef body = newDef(); set(body, "bodyModelIds", new int[]{5}); set(body, "headModelIds", new int[]{6});
        assertArrayEquals(new int[]{26, 36, 26}, xs(body.getHeadModel()));   // uses the 73 list, not the 94 list
    }
    @Test public void method160RecolorsFacesWhoseColourEqualsTheFirstArrayWithTheSecond() throws Exception {
        Model.loadModelHeader(triangle(5, 40000), 5);
        EntityDef d = newDef(); set(d, "headModelIds", new int[]{5});
        assertEquals(40000, d.getHeadModel().faceColors[0]);
        set(d, "recolorSources", new int[]{123, 40000}); set(d, "recolorReplacements", new int[]{77, 555});
        assertEquals(555, d.getHeadModel().faceColors[0]);        // 40000 -> 555 (76 = source, 70 = replacement)
        set(d, "recolorSources", new int[]{555}); set(d, "recolorReplacements", new int[]{1});
        assertEquals(40000, d.getHeadModel().faceColors[0]);      // reversed roles: no face has colour 555
    }

    // ---------------- method164 ----------------
    @Test public void method164BuildsFromTheNinetyFourListLightsItWithOffsetsAndCachesItUnderType() throws Exception {
        Model.loadModelHeader(triangle(5, 40000), 5); Model.loadModelHeader(triangle(6, 40000), 6);
        for (int[] ab : new int[][]{{0, 0}, {-5, 15}, {20, -30}}) {
            EntityDef.mruNodes.clear();
            EntityDef d = newDef(); d.type = 77L;
            set(d, "bodyModelIds", new int[]{6, 5}); seti(d, "ambientLightOffset", ab[0]); seti(d, "lightDivisorOffset", ab[1]);
            Model m = d.getAnimatedModel(-1, -1, null);
            Model both = new Model(2, new Model[]{Model.getModel(6), Model.getModel(5)});
            both.buildAnimationGroups(); both.applyLighting(64 + ab[0], 850 + ab[1], -30, -50, -30, true);
            assertArrayEquals(Arrays.toString(ab), xs(both), xs(m));
            assertArrayEquals(Arrays.toString(ab), sh(both, 4), sh(m, 4));
            assertSame(Model.animationScratchModel, m);
        }
        Model a0 = Model.getModel(5); a0.applyLighting(64, 850, -30, -50, -30, true);
        Model a1 = Model.getModel(5); a1.applyLighting(84, 850, -30, -50, -30, true);
        Model a2 = Model.getModel(5); a2.applyLighting(64, 1700, -30, -50, -30, true);
        assertFalse(Arrays.equals(sh(a0, 4), sh(a1, 4))); assertFalse(Arrays.equals(sh(a0, 4), sh(a2, 4)));
        // cached under the long id: a second definition with the same id reuses the first build
        EntityDef.mruNodes.clear();
        EntityDef e1 = newDef(); e1.type = 9L; set(e1, "bodyModelIds", new int[]{5});
        EntityDef e2 = newDef(); e2.type = 9L; set(e2, "bodyModelIds", new int[]{6});
        assertArrayEquals(new int[]{25, 35, 25}, xs(e1.getAnimatedModel(-1, -1, null)));
        assertArrayEquals(new int[]{25, 35, 25}, xs(e2.getAnimatedModel(-1, -1, null)));
        EntityDef e3 = newDef(); e3.type = 10L; set(e3, "bodyModelIds", new int[]{6});
        assertArrayEquals(new int[]{26, 36, 26}, xs(e3.getAnimatedModel(-1, -1, null)));
    }
    @Test public void method164ScalesXAndZByAnInt91AndYByAnInt86ThenRecolors() throws Exception {
        Model.loadModelHeader(triangle(5, 40000), 5);
        EntityDef d = newDef(); d.type = 3L; set(d, "bodyModelIds", new int[]{5});
        set(d, "recolorSources", new int[]{40000}); set(d, "recolorReplacements", new int[]{321});
        seti(d, "horizontalScale", 256); seti(d, "verticalScale", 64);
        Model m = d.getAnimatedModel(-1, -1, null);
        assertArrayEquals(new int[]{50, 70, 50}, xs(m));           // x doubled
        assertArrayEquals(new int[]{0, 0, 5}, ys(m));              // y (anInt86) halved
        assertArrayEquals(new int[]{0, 0, 20}, zs(m));             // z doubled (anInt91)
        Model ref = Model.getModel(5); ref.recolor(40000, 321); ref.buildAnimationGroups(); ref.applyLighting(64, 850, -30, -50, -30, true);
        Model unrecoloured = Model.getModel(5); unrecoloured.buildAnimationGroups(); unrecoloured.applyLighting(64, 850, -30, -50, -30, true);
        Model cached = (Model) EntityDef.mruNodes.insertFromCache(3L);
        assertArrayEquals(sh(ref, 6), sh(cached, 6)); assertArrayEquals(sh(ref, 4), sh(cached, 4));
        assertFalse(Arrays.equals(sh(ref, 6), sh(unrecoloured, 6)) && Arrays.equals(sh(ref, 4), sh(unrecoloured, 4)));
        EntityDef.mruNodes.clear();
        EntityDef n = newDef(); n.type = 4L; set(n, "bodyModelIds", new int[]{5}); seti(n, "horizontalScale", 128); seti(n, "verticalScale", 256);
        Model q = n.getAnimatedModel(-1, -1, null);
        assertArrayEquals(new int[]{25, 35, 25}, xs(q)); assertArrayEquals(new int[]{0, 0, 20}, ys(q)); assertArrayEquals(new int[]{0, 0, 10}, zs(q));
    }
    @Test public void method164UnloadedModelIsNullAndRequested() throws Exception {
        EntityDef d = newDef(); d.type = 11L; set(d, "bodyModelIds", new int[]{5, 6});
        Model.loadModelHeader(triangle(6, 40000), 6);
        assertNull(d.getAnimatedModel(-1, -1, null)); assertEquals(Arrays.asList(5), spy.requested);
    }

    // frames: parameter j = second frame (for masked transforms), k = main frame, ai = mask
    private static AnimationFrame frame(AnimationTransformTable t, int dx) throws Exception {
        Constructor<AnimationFrame> c = AnimationFrame.class.getDeclaredConstructor(); c.setAccessible(true); AnimationFrame fr = c.newInstance();
        fr.transformTable = t; fr.transformCount = 2; fr.transformIndices = new int[]{0, 1};
        fr.transformX = new int[]{dx, dx}; fr.transformY = new int[]{0, 0}; fr.transformZ = new int[]{0, 0}; return fr;
    }
    private static int[] animated(EntityDef d, int j, int k, int[] ai) throws Exception {
        Constructor<Model> c = Model.class.getDeclaredConstructor(); c.setAccessible(true); Model m = c.newInstance();
        m.vertexCount = 2; m.faceCount = 0; m.vertexX = new int[]{10, 20}; m.vertexY = new int[]{0, 0}; m.vertexZ = new int[]{0, 0};
        m.vertexGroups = new int[][]{{0}, {1}};
        EntityDef.mruNodes.clear(); EntityDef.mruNodes.removeFromCache(m, d.type);
        return xs(d.getAnimatedModel(j, k, ai));
    }
    @Test public void method164AppliesKAsTheMainFrameAndJOnlyThroughTheMask() throws Exception {
        Object savedFrames = sget(AnimationFrame.class, "loadedFrames");
        try {
            ByteArrayOutputStream o = new ByteArrayOutputStream();
            for (int v : new int[]{2, 1, 1, 1, 0, 1, 1}) o.write(v);
            AnimationTransformTable t = new AnimationTransformTable(new Stream(o.toByteArray()));
            AnimationFrame[] fr = new AnimationFrame[8]; fr[4] = frame(t, 100); fr[6] = frame(t, 9);
            sset(AnimationFrame.class, "loadedFrames", fr);
            EntityDef d = newDef(); d.type = 21L;
            int[] mask = {1, 1000};
            assertArrayEquals(new int[]{110, 120}, animated(d, -1, 4, null));      // k alone: every transform
            assertArrayEquals(new int[]{19, 29}, animated(d, -1, 6, null));
            assertArrayEquals(new int[]{110, 29}, animated(d, 6, 4, mask));        // k main, j supplies the masked group 1
            assertArrayEquals(new int[]{19, 120}, animated(d, 4, 6, mask));        // swapped roles give another result
            assertArrayEquals(new int[]{10, 20}, animated(d, -1, -1, null));      // no frames: untouched
        } finally { sset(AnimationFrame.class, "loadedFrames", savedFrames); EntityDef.mruNodes.clear(); }
    }
}
