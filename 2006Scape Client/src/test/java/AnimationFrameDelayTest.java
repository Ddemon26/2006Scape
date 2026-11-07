import java.lang.reflect.*;
import org.junit.*;
import static org.junit.Assert.*;

public class AnimationFrameDelayTest {
    private Field frames, delays;
    private Object savedFrames, savedFlags;
    private Animation[] savedAnimations;

    @Before public void isolate() throws Exception {
        frames = AnimationFrame.class.getDeclaredField("loadedFrames"); frames.setAccessible(true);
        delays = Animation.class.getDeclaredField("frameDelays"); delays.setAccessible(true);
        savedFrames = frames.get(null); savedFlags = hidden("alphaTransformNotSeen").get(null); savedAnimations = Animation.anims;
        frames.set(null, new AnimationFrame[4]);
    }
    @After public void restore() throws Exception { frames.set(null, savedFrames); hidden("alphaTransformNotSeen").set(null, savedFlags); Animation.anims = savedAnimations; }
    private Animation sequence(int[] ids, int... values) throws Exception {
        Constructor<Animation> c = Animation.class.getDeclaredConstructor(); c.setAccessible(true);
        Animation a = c.newInstance(); a.anInt352 = values.length; a.anIntArray353 = ids;
        delays.set(a, values); return a;
    }
    private AnimationFrame frame(int id, int delay) throws Exception {
        Constructor<AnimationFrame> c = AnimationFrame.class.getDeclaredConstructor(); c.setAccessible(true);
        AnimationFrame f = c.newInstance(); f.frameDelay = delay; ((AnimationFrame[]) frames.get(null))[id] = f; return f;
    }
    private int stored(Animation a, int i) throws Exception { return ((int[]) delays.get(a))[i]; }

    @Test public void directSequenceValuesBypassFrameLookupIncludingNegativeValues() throws Exception {
        Animation a = sequence(null, 2, 65535, -1, Integer.MIN_VALUE, Integer.MAX_VALUE);
        for (int i = 0; i < 5; i++) assertEquals(stored(a, i), a.getFrameDelay(i));
    }
    @Test public void sequenceIndexSelectsPrimaryFrameIdAndCachesNonzeroFallback() throws Exception {
        AnimationFrame f = frame(3, 7); frame(1, 19);
        Animation a = sequence(new int[]{3, 1}, 0, 0);
        assertEquals(7, a.getFrameDelay(0)); assertEquals(7, stored(a, 0)); assertEquals(0, stored(a, 1));
        f.frameDelay = 29; assertEquals(7, a.getFrameDelay(0)); assertEquals(19, a.getFrameDelay(1));
    }
    @Test public void zeroAndMissingFallbackReturnOneWithoutCachingOneAndRetryLater() throws Exception {
        Animation a = sequence(new int[]{2}, 0);
        assertEquals(1, a.getFrameDelay(0)); assertEquals(0, stored(a, 0));
        AnimationFrame f = frame(2, 0); assertEquals(1, a.getFrameDelay(0)); assertEquals(0, stored(a, 0));
        f.frameDelay = 11; assertEquals(11, a.getFrameDelay(0)); assertEquals(11, stored(a, 0));
        frames.set(null, null); assertEquals(11, a.getFrameDelay(0));
    }
    @Test public void fallbackNegativeValueIsCachedWithoutClamping() throws Exception {
        frame(1, -7); Animation a = sequence(new int[]{1}, 0);
        assertEquals(-7, a.getFrameDelay(0)); assertEquals(-7, stored(a, 0));
    }
    @Test public void lookupReturnsExactSlotIdentityAndNullTableIgnoresIndex() throws Exception {
        AnimationFrame f = frame(3, 9); assertSame(f, AnimationFrame.getFrame(3)); assertNull(AnimationFrame.getFrame(0));
        for (int id : new int[]{-1, 4}) {
            try { AnimationFrame.getFrame(id); fail(); } catch (ArrayIndexOutOfBoundsException expected) { }
        }
        frames.set(null, null);
        for (int id : new int[]{-1, 0, Integer.MAX_VALUE}) assertNull(AnimationFrame.getFrame(id));
        assertEquals(1, sequence(new int[]{-1}, 0).getFrameDelay(0));
    }
    @Test public void invalidSequenceIndexAndFrameIdRetainFailuresWithoutCaching() throws Exception {
        Animation a = sequence(new int[]{4}, 0);
        for (int i : new int[]{-1, 1}) {
            try { a.getFrameDelay(i); fail(); } catch (ArrayIndexOutOfBoundsException expected) { }
        }
        try { a.getFrameDelay(0); fail(); } catch (ArrayIndexOutOfBoundsException expected) { }
        assertEquals(0, stored(a, 0));
    }
    @Test public void frameFileDecodesDelayAsUnsignedByteAtSelectedFrameId() throws Exception {
        // Two zero-transform frames, distinct IDs; footer splits header/flags/values/delays/skeleton.
        for (int delay : new int[]{0, 1, 127, 128, 255}) {
            byte[] file = {0,2, 0,3,0, 0,1,0, (byte)delay,17, 0, 0,6, 0,0, 0,0, 0,2};
            AnimationFrame.decodeFile(file);
            assertEquals(delay, AnimationFrame.getFrame(3).frameDelay);
            assertEquals(17, AnimationFrame.getFrame(1).frameDelay);
            assertEquals(delay == 0 ? 1 : delay, sequence(new int[]{3}, 0).getFrameDelay(0));
        }
    }
    @Test public void sequenceFileDelaysAreUnsignedShortsAndZeroIsFallback() throws Exception {
        Animation a = sequence(new int[0]);
        Method read = Animation.class.getDeclaredMethod("readValues", Stream.class); read.setAccessible(true);
        read.invoke(a, new Stream(new byte[]{1,2, 0,3, (byte)255,(byte)255, 0,0, 0,1, 0,2, (byte)255,(byte)255, 0}));
        frame(3, 13); assertEquals(13, a.getFrameDelay(0)); assertEquals(65535, a.getFrameDelay(1));
    }
    @Test public void gameUpdaterAdvancesOnlyAfterDelayLoopCyclesAndWraps() throws Exception {
        Field uf = sun.misc.Unsafe.class.getDeclaredField("theUnsafe"); uf.setAccessible(true);
        Game g = (Game) ((sun.misc.Unsafe) uf.get(null)).allocateInstance(Game.class);
        for (int delay : new int[]{1, 2, 7}) {
            Animation.anims = new Animation[]{sequence(null, delay, 3)};
            Entity e = new Entity(); e.anInt1517 = 0;
            for (int cycle = 1; cycle <= delay; cycle++) {
                g.updateEntityAnimations(e); assertEquals(0, e.anInt1518); assertEquals(cycle, e.anInt1519);
            }
            g.updateEntityAnimations(e); assertEquals(1, e.anInt1518); assertEquals(0, e.anInt1519);
            for (int cycle = 0; cycle < 4; cycle++) g.updateEntityAnimations(e);
            assertEquals(0, e.anInt1518); assertEquals(0, e.anInt1519);
        }
    }

    private static Field hidden(String n) throws Exception {
        Field f = AnimationFrame.class.getDeclaredField(n); f.setAccessible(true); return f;
    }
    private static void u16(java.io.ByteArrayOutputStream o, int n) { o.write(n >>> 8); o.write(n); }
    private static void smart(java.io.ByteArrayOutputStream o, int n) {
        if (n >= -64 && n <= 63) o.write(n + 64); else u16(o, n + 49152);
    }
    // An encoder of controlled sections; no decoder or proposed production names.
    public static byte[] fixture(int[] t, int[][] g, int[] ids, int[] ds, int[][] masks, int[][][] xyz, int pad) throws Exception {
        java.io.ByteArrayOutputStream h = new java.io.ByteArrayOutputStream(), f = new java.io.ByteArrayOutputStream();
        java.io.ByteArrayOutputStream v = new java.io.ByteArrayOutputStream(), d = new java.io.ByteArrayOutputStream();
        u16(h, ids.length);
        for (int a = 0; a < ids.length; a++) {
            u16(h, ids[a]); h.write(masks[a].length); d.write(ds[a]);
            for (int b = 0; b < masks[a].length; b++) {
                f.write(masks[a][b]);
                for (int c = 0; c < 3; c++) if ((masks[a][b] & (1 << c)) != 0) smart(v, xyz[a][b][c]);
            }
        }
        java.io.ByteArrayOutputStream o = new java.io.ByteArrayOutputStream();
        o.write(h.toByteArray()); for (int a = 0; a < pad; a++) o.write(203);
        o.write(f.toByteArray()); for (int a = 0; a < pad + 1; a++) o.write(211);
        o.write(v.toByteArray()); for (int a = 0; a < pad + 2; a++) o.write(223);
        o.write(d.toByteArray()); for (int a = 0; a < pad + 3; a++) o.write(227);
        o.write(t.length); for (int n : t) o.write(n);
        for (int[] row : g) { o.write(row.length); for (int n : row) o.write(n); }
        u16(o, h.size() - 2 + pad); u16(o, f.size() + pad + 1);
        u16(o, v.size() + pad + 2); u16(o, d.size() + pad + 3); return o.toByteArray();
    }
    private static int[][] blank(int n) { return new int[n][3]; }
    @Test public void loaderUsesInclusiveMaximumAndReinitializesFlagsWhileSentinelIsExactlyMinusOne() throws Exception {
        for (int n : new int[]{0, 1, 7, 65535}) {
            AnimationFrame.initialize(n); assertEquals(n + 1, ((AnimationFrame[])hidden("loadedFrames").get(null)).length);
            boolean[] b = (boolean[])hidden("alphaTransformNotSeen").get(null); assertEquals(n + 1, b.length);
            for (int a = 0; a <= n; a++) { assertNull(AnimationFrame.getFrame(a)); assertTrue(b[a]); }
        }
        AnimationFrame.initialize(-1); assertEquals(0, ((AnimationFrame[])hidden("loadedFrames").get(null)).length);
        try { AnimationFrame.initialize(-2); fail(); } catch (NegativeArraySizeException expected) { }
        for (int n : new int[]{Integer.MIN_VALUE, -2, -1, 0, 1, 65535, Integer.MAX_VALUE}) assertEquals(n == -1, AnimationFrame.isMissingFrameId(n));
        AnimationFrame.nullLoader(); assertNull(AnimationFrame.getFrame(Integer.MAX_VALUE));
    }
    @Test public void independentFooterLengthsSelectSparseIdsSeparateDelaysAndSharedTable() throws Exception {
        for (int pad : new int[]{0, 1, 5, 128}) {
            AnimationFrame.initialize(60000);
            byte[] raw = fixture(new int[]{1, 5}, new int[][]{{1}, {2, 255}}, new int[]{60000, 2}, new int[]{255, 7},
                new int[][]{{7}, {0, 5}}, new int[][][]{ {{-64, 63, -65}}, {{0,0,0},{16383,0,-16384}} }, pad);
            AnimationFrame.decodeFile(raw); AnimationFrame a = AnimationFrame.getFrame(60000), b = AnimationFrame.getFrame(2);
            assertEquals(255, a.frameDelay); assertEquals(7, b.frameDelay); assertNull(AnimationFrame.getFrame(0));
            assertSame(a.transformTable, b.transformTable); assertArrayEquals(new int[]{1,5}, a.transformTable.transformTypes);
            assertArrayEquals(new int[]{2,255}, a.transformTable.groupIds[1]);
            assertEquals(1, a.transformCount); assertArrayEquals(new int[]{0}, a.transformIndices);
            assertArrayEquals(new int[]{-64}, a.transformX); assertArrayEquals(new int[]{63}, a.transformY); assertArrayEquals(new int[]{-65}, a.transformZ);
            assertArrayEquals(new int[]{1}, b.transformIndices); assertArrayEquals(new int[]{16383}, b.transformX);
            assertArrayEquals(new int[]{0}, b.transformY); assertArrayEquals(new int[]{-16384}, b.transformZ);
            boolean[] flags = (boolean[])hidden("alphaTransformNotSeen").get(null); assertTrue(flags[60000]); assertFalse(flags[2]);
        }
    }
    @Test public void everyMaskSelectsIndependentSignedComponentsAndOnlyScaleDefaultsTo128() throws Exception {
        for (int t : new int[]{0, 1, 2, 3, 5, 200}) for (int mask : new int[]{0,1,2,3,4,5,6,7,8,9,255})
        for (int val : new int[]{-16384,-65,-64,-1,0,1,63,64,16383}) {
            AnimationFrame.initialize(3);
            AnimationFrame.decodeFile(fixture(new int[]{t}, new int[][]{{0}}, new int[]{3}, new int[]{128}, new int[][]{{mask}}, new int[][][]{ {{val, -val-1, val}} }, 0));
            AnimationFrame a = AnimationFrame.getFrame(3); int count = mask == 0 ? 0 : 1; assertEquals(count, a.transformCount);
            assertEquals(count, a.transformIndices.length); assertEquals(count, a.transformX.length); assertEquals(count, a.transformY.length); assertEquals(count, a.transformZ.length);
            if (count == 1) {
                assertArrayEquals(new int[]{0}, a.transformIndices); int def = t == 3 ? 128 : 0;
                assertEquals((mask & 1) != 0 ? val : def, a.transformX[0]);
                assertEquals((mask & 2) != 0 ? -val-1 : def, a.transformY[0]);
                assertEquals((mask & 4) != 0 ? val : def, a.transformZ[0]);
            }
            assertEquals(!(t == 5 && mask != 0), ((boolean[])hidden("alphaTransformNotSeen").get(null))[3]);
        }
    }
    @Test public void insertsNearestSkippedPivotOnceAndPreservesExplicitOrderingAndExactOutputPrefix() throws Exception {
        AnimationFrame.initialize(9); int[] masks = {0,0,7,0,1,2,1,0};
        int[][] xyz = {{0,0,0},{0,0,0},{7,-3,9},{0,0,0},{-65,0,0},{0,256,0},{2,0,0},{0,0,0}};
        AnimationFrame.decodeFile(fixture(new int[]{0,0,1,0,2,3,5,1}, new int[][]{{0},{1},{1},{0},{1},{1},{2},{0}}, new int[]{9}, new int[]{1}, new int[][]{masks}, new int[][][]{xyz}, 3));
        AnimationFrame a = AnimationFrame.getFrame(9); assertEquals(6, a.transformCount);
        assertArrayEquals(new int[]{1,2,3,4,5,6}, a.transformIndices);
        assertArrayEquals(new int[]{0,7,0,-65,128,2}, a.transformX);
        assertArrayEquals(new int[]{0,-3,0,0,256,0}, a.transformY);
        assertArrayEquals(new int[]{0,9,0,0,128,0}, a.transformZ);
        // An explicit zero-type row is not preceded by another zero row; pivot already emitted is not repeated.
        AnimationFrame.decodeFile(fixture(new int[]{0,0,1,1}, new int[][]{{0},{0},{0},{0}}, new int[]{2}, new int[]{0}, new int[][]{{0,1,1,1}}, new int[][][]{blank(4)}, 0));
        assertArrayEquals(new int[]{1,2,3}, AnimationFrame.getFrame(2).transformIndices);
    }
    @Test public void maximumEntryCountEmptyFileReplacementAndAlphaObservationStateAreDistinct() throws Exception {
        AnimationFrame.initialize(9); int[] t = new int[255], masks = new int[255]; int[][] g = new int[255][];
        java.util.Arrays.fill(t, 1); java.util.Arrays.fill(masks, 1); for (int i = 0; i < 255; i++) g[i] = new int[]{i};
        int[][] xyz = blank(255); for (int i = 0; i < 255; i++) xyz[i][0] = i - 127;
        AnimationFrame.decodeFile(fixture(t,g,new int[]{9},new int[]{0},new int[][]{masks},new int[][][]{xyz},0));
        AnimationFrame a = AnimationFrame.getFrame(9); assertEquals(255,a.transformCount); assertEquals(254,a.transformIndices[254]); assertEquals(127,a.transformX[254]);
        AnimationFrame.decodeFile(fixture(new int[0],new int[0][],new int[0],new int[0],new int[0][],new int[0][][],0)); assertSame(a,AnimationFrame.getFrame(9));
        AnimationFrame.decodeFile(fixture(new int[]{5},new int[][]{{0}},new int[]{2},new int[]{0},new int[][]{{1}},new int[][][]{blank(1)},0));
        AnimationFrame.decodeFile(fixture(new int[]{1},new int[][]{{0}},new int[]{2},new int[]{0},new int[][]{{0}},new int[][][]{blank(1)},0));
        assertEquals(0,AnimationFrame.getFrame(2).transformCount); assertFalse(((boolean[])hidden("alphaTransformNotSeen").get(null))[2]);
        AnimationFrame.initialize(9); assertTrue(((boolean[])hidden("alphaTransformNotSeen").get(null))[2]);
    }
    @Test public void decodedRowsDriveActualModelXYZAndFaceAlphaThroughPermutedGroups() throws Exception {
        AnimationFrame.initialize(4);
        AnimationFrame.decodeFile(fixture(new int[]{1,5},new int[][]{{1},{2}},new int[]{4},new int[]{9},new int[][]{{7,1}},new int[][][]{ {{7,-2,5},{2,0,0}} },0));
        Constructor<Model> c=Model.class.getDeclaredConstructor(); c.setAccessible(true); Model m=c.newInstance();
        m.anInt1626=3; m.anInt1630=3; m.anIntArray1627=new int[]{10,20,30}; m.anIntArray1628=new int[]{40,50,60}; m.anIntArray1629=new int[]{70,80,90};
        m.vertexGroups=new int[][]{{2},{0},{1}}; m.faceGroups=new int[][]{{1},{2},{0}};
        Field alpha=Model.class.getDeclaredField("faceAlphas"); alpha.setAccessible(true); alpha.set(m,new int[]{11,22,33});
        int[] old=new int[3]; Field[] pivots=new Field[3];
        for(int i=0;i<3;i++){pivots[i]=Model.class.getDeclaredField("animationPivot"+"XYZ".charAt(i));pivots[i].setAccessible(true);old[i]=pivots[i].getInt(null);}
        try {m.applyAnimationFrame(4); assertArrayEquals(new int[]{17,20,30},m.anIntArray1627); assertArrayEquals(new int[]{38,50,60},m.anIntArray1628); assertArrayEquals(new int[]{75,80,90},m.anIntArray1629); assertArrayEquals(new int[]{27,22,33},(int[])alpha.get(m));
            // Nearest skipped zero row selects vertex0 as the centroid; row0 would select vertex2.
            AnimationFrame.decodeFile(fixture(new int[]{0,0,3},new int[][]{{0},{1},{2}},new int[]{2},new int[]{1},new int[][]{{0,0,1}},new int[][][]{ {{0,0,0},{0,0,0},{256,0,0}} },0));
            m.anIntArray1627=new int[]{10,20,30}; m.anIntArray1628=new int[]{40,50,60}; m.anIntArray1629=new int[]{70,80,90};
            m.applyAnimationFrame(2); assertArrayEquals(new int[]{10,30,30},m.anIntArray1627);
            assertArrayEquals(new int[]{40,50,60},m.anIntArray1628); assertArrayEquals(new int[]{70,80,90},m.anIntArray1629);
        }
        finally {for(int i=0;i<3;i++)pivots[i].setInt(null,old[i]);}
    }
}
