import java.lang.reflect.*;
import org.junit.*;
import static org.junit.Assert.*;

public class AnimationFrameDelayTest {
    private Field frames, delays;
    private Object savedFrames;
    private Animation[] savedAnimations;

    @Before public void isolate() throws Exception {
        frames = Class36.class.getDeclaredField("loadedFrames"); frames.setAccessible(true);
        delays = Animation.class.getDeclaredField("frameDelays"); delays.setAccessible(true);
        savedFrames = frames.get(null); savedAnimations = Animation.anims;
        frames.set(null, new Class36[4]);
    }
    @After public void restore() throws Exception { frames.set(null, savedFrames); Animation.anims = savedAnimations; }
    private Animation sequence(int[] ids, int... values) throws Exception {
        Constructor<Animation> c = Animation.class.getDeclaredConstructor(); c.setAccessible(true);
        Animation a = c.newInstance(); a.anInt352 = values.length; a.anIntArray353 = ids;
        delays.set(a, values); return a;
    }
    private Class36 frame(int id, int delay) throws Exception {
        Constructor<Class36> c = Class36.class.getDeclaredConstructor(); c.setAccessible(true);
        Class36 f = c.newInstance(); f.frameDelay = delay; ((Class36[]) frames.get(null))[id] = f; return f;
    }
    private int stored(Animation a, int i) throws Exception { return ((int[]) delays.get(a))[i]; }

    @Test public void directSequenceValuesBypassFrameLookupIncludingNegativeValues() throws Exception {
        Animation a = sequence(null, 2, 65535, -1, Integer.MIN_VALUE, Integer.MAX_VALUE);
        for (int i = 0; i < 5; i++) assertEquals(stored(a, i), a.getFrameDelay(i));
    }
    @Test public void sequenceIndexSelectsPrimaryFrameIdAndCachesNonzeroFallback() throws Exception {
        Class36 f = frame(3, 7); frame(1, 19);
        Animation a = sequence(new int[]{3, 1}, 0, 0);
        assertEquals(7, a.getFrameDelay(0)); assertEquals(7, stored(a, 0)); assertEquals(0, stored(a, 1));
        f.frameDelay = 29; assertEquals(7, a.getFrameDelay(0)); assertEquals(19, a.getFrameDelay(1));
    }
    @Test public void zeroAndMissingFallbackReturnOneWithoutCachingOneAndRetryLater() throws Exception {
        Animation a = sequence(new int[]{2}, 0);
        assertEquals(1, a.getFrameDelay(0)); assertEquals(0, stored(a, 0));
        Class36 f = frame(2, 0); assertEquals(1, a.getFrameDelay(0)); assertEquals(0, stored(a, 0));
        f.frameDelay = 11; assertEquals(11, a.getFrameDelay(0)); assertEquals(11, stored(a, 0));
        frames.set(null, null); assertEquals(11, a.getFrameDelay(0));
    }
    @Test public void fallbackNegativeValueIsCachedWithoutClamping() throws Exception {
        frame(1, -7); Animation a = sequence(new int[]{1}, 0);
        assertEquals(-7, a.getFrameDelay(0)); assertEquals(-7, stored(a, 0));
    }
    @Test public void lookupReturnsExactSlotIdentityAndNullTableIgnoresIndex() throws Exception {
        Class36 f = frame(3, 9); assertSame(f, Class36.getFrame(3)); assertNull(Class36.getFrame(0));
        for (int id : new int[]{-1, 4}) {
            try { Class36.getFrame(id); fail(); } catch (ArrayIndexOutOfBoundsException expected) { }
        }
        frames.set(null, null);
        for (int id : new int[]{-1, 0, Integer.MAX_VALUE}) assertNull(Class36.getFrame(id));
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
            Class36.method529(file);
            assertEquals(delay, Class36.getFrame(3).frameDelay);
            assertEquals(17, Class36.getFrame(1).frameDelay);
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
                g.method101(e); assertEquals(0, e.anInt1518); assertEquals(cycle, e.anInt1519);
            }
            g.method101(e); assertEquals(1, e.anInt1518); assertEquals(0, e.anInt1519);
            for (int cycle = 0; cycle < 4; cycle++) g.method101(e);
            assertEquals(0, e.anInt1518); assertEquals(0, e.anInt1519);
        }
    }
}
