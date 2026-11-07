import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/** Original-name semantic tests for Projectile (flying graphic) and AnimatedObject (animated object). */
public class AnimableSub45SemanticsTest {
    private SpotAnim[] savedSpots;
    private Animation[] savedAnims;
    private ObjectDef[] savedDefs;
    private VarBit[] savedBits;
    private int[] savedMasks;
    private Game savedClient;
    private int savedCycle;

    private static Field field(Class<?> type, String name) throws Exception {
        Field f = type.getDeclaredField(name);
        f.setAccessible(true);
        return f;
    }
    private static sun.misc.Unsafe unsafe() throws Exception {
        return (sun.misc.Unsafe) field(sun.misc.Unsafe.class, "theUnsafe").get(null);
    }
    private static int geti(Object o, String n) throws Exception { return field(o.getClass(), n).getInt(o); }

    @Before public void setUp() throws Exception {
        savedSpots = SpotAnim.cache;
        savedAnims = Animation.anims;
        savedDefs = (ObjectDef[]) field(ObjectDef.class, "cache").get(null);
        savedBits = VarBit.cache;
        savedMasks = Game.anIntArray1232;
        savedClient = AnimatedObject.clientInstance;
        savedCycle = Game.loopCycle;
        Constructor<SpotAnim> c = SpotAnim.class.getDeclaredConstructor();
        c.setAccessible(true);
        SpotAnim.cache = new SpotAnim[]{c.newInstance()};
    }

    @After public void tearDown() throws Exception {
        SpotAnim.cache = savedSpots;
        Animation.anims = savedAnims;
        field(ObjectDef.class, "cache").set(null, savedDefs);
        VarBit.cache = savedBits;
        Game.anIntArray1232 = savedMasks;
        AnimatedObject.clientInstance = savedClient;
        Game.loopCycle = savedCycle;
    }

    private static Animation animation(int frames, int delay, int loopOffset) throws Exception {
        Animation a = (Animation) unsafe().allocateInstance(Animation.class);
        a.frameCount = frames;
        a.loopBackFrames = loopOffset;
        a.frameIds = new int[frames];
        int[] delays = new int[frames];
        for (int n = 0; n < frames; n++) delays[n] = delay;
        field(Animation.class, "frameDelays").set(a, delays);
        return a;
    }

    // ------------------------------------------------------------------ Projectile

    private static Projectile shot(int angle, int startOffset, int originHeight, int originY, int originX, int end) {
        return new Projectile(angle, 0, 10, end, startOffset, 0, originHeight, originY, originX, 0, 0);
    }

    @Test public void sub4ConstructorStoresEachArgumentInItsOwnSlot() throws Exception {
        Projectile p = new Projectile(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 0);
        assertEquals(1, geti(p, "launchAngle"));
        assertEquals(2, p.targetHeightOffset);
        assertEquals(3, p.startCycle);
        assertEquals(4, p.endCycle);
        assertEquals(5, geti(p, "startOffset"));
        assertEquals(6, p.plane);
        assertEquals(7, geti(p, "originHeight"));
        assertEquals(8, geti(p, "originY"));
        assertEquals(9, geti(p, "originX"));
        assertEquals(10, p.target);
        assertSame(SpotAnim.cache[0], field(Projectile.class, "spotAnim").get(p));
    }

    @Test public void sub4OriginIsPlacedAndOffsetMovesAlongTheLineToTheTarget() throws Exception {
        Projectile p = shot(0, 64, 40, 320, 256, 21);
        p.retarget(10, 320, 0, 768);               // target x 768, y 320: offset moves along x
        assertEquals(256 + 64.0, p.positionX, 1e-9);
        assertEquals(320.0, p.positionY, 1e-9);
        assertEquals(40.0, p.positionHeight, 1e-9);     // starts at the origin height
        Projectile q = shot(0, 64, 40, 320, 256, 21);
        q.retarget(10, 960, 0, 256);               // target straight along +y
        assertEquals(256.0, q.positionX, 1e-9);
        assertEquals(320 + 64.0, q.positionY, 1e-9);
        Projectile none = shot(0, 0, 40, 320, 256, 21);
        none.retarget(10, 320, 0, 768);
        assertEquals(256.0, none.positionX, 1e-9);
        // before the first advance, retargeting recomputes the origin from scratch
        p.retarget(10, 320, 0, 768);
        assertEquals(320.0, p.positionX, 1e-9);
        // after advancing, the current position is kept
        p.advance(3);
        double x = p.positionX;
        p.retarget(13, 320, 0, 768);
        assertEquals(x, p.positionX, 1e-9);
    }

    @Test public void sub4ArrivesAtTargetAtEndCyclePlusOneAndMovesLinearly() throws Exception {
        Projectile p = shot(0, 0, 0, 320, 320, 20);
        p.retarget(10, 640, 100, 448);              // 11 cycles remain (end + 1 - current)
        p.advance(2);
        assertEquals(320 + 2 * 128.0 / 11, p.positionX, 1e-9);
        assertEquals(320 + 2 * 320.0 / 11, p.positionY, 1e-9);
        p.advance(9);
        assertEquals(448.0, p.positionX, 1e-9);
        assertEquals(640.0, p.positionY, 1e-9);
        assertEquals("height reaches the requested target height", 100.0, p.positionHeight, 1e-9);
        Projectile r = shot(0, 0, 0, 320, 320, 20);
        r.retarget(10, 640, 100, 448);
        r.advance(4);
        r.retarget(14, 160, -50, 900);
        r.advance(20 + 1 - 14);
        assertEquals(900.0, r.positionX, 1e-9);
        assertEquals(160.0, r.positionY, 1e-9);
        assertEquals(-50.0, r.positionHeight, 1e-9);
    }

    @Test public void sub4LaunchAngleGivesParabolaWithApexAQuarterOfTheRange() throws Exception {
        double range = 1200;
        for (int angle : new int[]{0, 32, -32}) {
            Projectile p = shot(angle, 0, 0, 320, 320, 21);
            p.retarget(10, 320, 0, 320 + 1200);     // 12 cycles; equal start and end height
            p.advance(6);                          // half way
            double expected = angle == 0 ? 0 : (angle > 0 ? -range / 4 : range / 4);
            assertEquals("angle " + angle, expected, p.positionHeight, 0.01);
            p.advance(6);
            assertEquals("lands at target height", 0.0, p.positionHeight, 0.01);
        }
    }

    @Test public void sub4FacingAnglesFollowTheVelocity() throws Exception {
        int[][] cases = {{448, 320, 1536}, {192, 320, 512}, {320, 448, 1024}, {320, 192, 0}};
        for (int[] c : cases) {
            Projectile p = shot(0, 0, 0, 320, 320, 20);
            p.retarget(10, c[1], 0, c[0]);
            p.advance(1);
            int diff = Math.abs(p.yaw - c[2]);
            assertTrue("yaw " + p.yaw + " for " + c[0] + "," + c[1], diff <= 1 || diff >= 2047);
        }
        Projectile up = shot(32, 0, 0, 320, 320, 21);
        up.retarget(10, 320, 0, 1520);
        up.advance(1);
        int pitchUp = geti(up, "pitch");
        assertTrue("ascending: pitch " + pitchUp, pitchUp > 1536 && pitchUp < 2048);
        up.advance(10);
        int pitchDown = geti(up, "pitch");
        assertTrue("descending: pitch " + pitchDown, pitchDown > 0 && pitchDown < 512);
    }

    @Test public void sub4AnimationFrameAdvancesAfterDelayPlusOneCyclesAndWraps() throws Exception {
        SpotAnim.cache[0].animation = animation(3, 2, -1);
        Projectile p = new Projectile(0, 0, 10, 99, 0, 0, 0, 320, 320, 0, 0);
        p.retarget(10, 320, 0, 448);
        p.advance(1);
        assertEquals(0, geti(p, "frameIndex")); assertEquals(1, geti(p, "frameTimer"));
        p.advance(2);
        assertEquals(1, geti(p, "frameIndex")); assertEquals(0, geti(p, "frameTimer"));
        p.advance(2);
        assertEquals("2 is not more than the delay", 1, geti(p, "frameIndex")); assertEquals(2, geti(p, "frameTimer"));
        p.advance(1);
        assertEquals(2, geti(p, "frameIndex")); assertEquals(0, geti(p, "frameTimer"));
        p.advance(3);
        assertEquals("wraps to frame 0 after the last", 0, geti(p, "frameIndex"));
    }

    // ------------------------------------------------------------------ AnimatedObject

    private static ObjectDef def(int type) throws Exception {
        Constructor<ObjectDef> c = ObjectDef.class.getDeclaredConstructor();
        c.setAccessible(true);
        ObjectDef d = c.newInstance();
        Method defaults = ObjectDef.class.getDeclaredMethod("setDefaults");
        defaults.setAccessible(true);
        defaults.invoke(d);
        d.type = type;
        return d;
    }

    private void install(ObjectDef... defs) throws Exception {
        ObjectDef[] cache = new ObjectDef[20];
        for (int n = 0; n < 20; n++) {
            cache[n] = n < defs.length ? defs[n] : def(-1);
        }
        field(ObjectDef.class, "cache").set(null, cache);
    }

    private AnimatedObject animated(int anim, boolean flag) throws Exception {
        ObjectDef d = def(605);
        d.animationId = anim;
        install(d);
        return new AnimatedObject(605, 3, 2, 11, 12, 13, 14, anim, flag);
    }

    private static Animation[] anims() throws Exception {
        Animation[] anims = new Animation[3];
        anims[1] = animation(4, 5, 2);
        anims[2] = animation(4, 5, -1);
        Animation.anims = anims;
        return anims;
    }

    @Test public void sub5ConstructorStoresEachArgumentInItsOwnSlot() throws Exception {
        AnimatedObject a = animated(-1, false);
        assertEquals(605, geti(a, "objectId"));
        assertEquals(3, geti(a, "orientation"));
        assertEquals(2, geti(a, "type"));
        assertEquals(11, geti(a, "heightX1Y0"));
        assertEquals(12, geti(a, "heightX1Y1"));
        assertEquals(13, geti(a, "heightX0Y0"));
        assertEquals(14, geti(a, "heightX0Y1"));
        assertNull(field(AnimatedObject.class, "animation").get(a));
    }

    @Test public void sub5StartFrameIsRandomOnlyWhenFlaggedAndLooping() throws Exception {
        Animation[] anims = anims();
        Game.loopCycle = 1000;
        AnimatedObject fixed = animated(1, false);
        assertEquals(0, geti(fixed, "frameIndex"));
        assertEquals(1000, geti(fixed, "frameStartCycle"));
        assertSame(anims[1], field(AnimatedObject.class, "animation").get(fixed));
        AnimatedObject once = animated(2, true);
        assertEquals("non-looping animations always start at frame 0", 0, geti(once, "frameIndex"));
        assertEquals(1000, geti(once, "frameStartCycle"));
        boolean sawNonZero = false;
        for (int n = 0; n < 200; n++) {
            AnimatedObject r = animated(1, true);
            int f = geti(r, "frameIndex");
            assertTrue(f >= 0 && f < 4);
            sawNonZero |= f != 0;
            int start = geti(r, "frameStartCycle");
            assertTrue(start <= 1000 && start > 1000 - 5);
        }
        assertTrue(sawNonZero);
    }

    @Test public void sub5GetRotatedModelAdvancesFrameByElapsedCyclesSinceFrameStart() throws Exception {
        anims();
        Game.loopCycle = 1000;
        AnimatedObject a = animated(1, false);
        Game.loopCycle = 1005;
        a.getRotatedModel();
        assertEquals("elapsed 5 is not more than the delay 5", 0, geti(a, "frameIndex"));
        assertEquals(1000, geti(a, "frameStartCycle"));
        Game.loopCycle = 1006;
        a.getRotatedModel();
        assertEquals(1, geti(a, "frameIndex"));
        assertEquals("frame began 1 cycle ago", 1005, geti(a, "frameStartCycle"));
        AnimatedObject b = animated(1, false);        // started at 1006
        Game.loopCycle = 1006 + 23;                  // 23 cycles: frames 0,1,2 consumed, wraps 4 -> 2 with 3 left
        b.getRotatedModel();
        assertEquals(2, geti(b, "frameIndex"));
        assertEquals(1006 + 20, geti(b, "frameStartCycle"));
    }

    @Test public void sub5NonLoopingAnimationEndsAndLongWaitsAreCapped() throws Exception {
        anims();
        Game.loopCycle = 1000;
        AnimatedObject once = animated(2, false);
        Game.loopCycle = 1100;
        once.getRotatedModel();
        assertNull("play-once animation is dropped when it runs out", field(AnimatedObject.class, "animation").get(once));
        Game.loopCycle = 1000;
        AnimatedObject looping = animated(1, false);
        Game.loopCycle = 1000 + 100000;
        looping.getRotatedModel();
        assertNotNull(field(AnimatedObject.class, "animation").get(looping));
        int frame = geti(looping, "frameIndex");
        assertTrue(frame >= 0 && frame < 4);
        assertTrue("elapsed time is capped at 100 cycles for looping animations",
                geti(looping, "frameStartCycle") >= Game.loopCycle - 100);
    }

    @Test public void sub5ResolvesChildDefinitionByVarBitThenSetting() throws Exception {
        ObjectDef parent = def(605);
        parent.childrenIDs = new int[]{500, -1, 502, 503};
        parent.varBitId = 7;
        ObjectDef bySetting = def(606);
        bySetting.childrenIDs = new int[]{500, -1, 502, 503};
        bySetting.settingId = 9;
        ObjectDef[] cache = new ObjectDef[20];
        for (int n = 0; n < 20; n++) cache[n] = def(-1);
        cache[0] = parent; cache[1] = bySetting;
        cache[2] = def(500); cache[3] = def(502); cache[4] = def(503);
        field(ObjectDef.class, "cache").set(null, cache);
        Game game = new Game();
        game.variousSettings = new int[50];
        AnimatedObject.clientInstance = game;
        Game.anIntArray1232 = new int[32];
        for (int k = 0, i = 2; k < 32; k++, i += i) Game.anIntArray1232[k] = i - 1;
        VarBit[] bits = new VarBit[10];
        for (int n = 0; n < bits.length; n++) bits[n] = (VarBit) unsafe().allocateInstance(VarBit.class);
        bits[7].varpIndex = 3; bits[7].lowBit = 2; bits[7].highBit = 4;
        VarBit.cache = bits;
        AnimatedObject a = new AnimatedObject(605, 0, 10, 0, 0, 0, 0, -1, false);
        AnimatedObject b = new AnimatedObject(606, 0, 10, 0, 0, 0, 0, -1, false);
        Method resolve = AnimatedObject.class.getDeclaredMethod("resolveChildDefinition");
        resolve.setAccessible(true);
        game.variousSettings[3] = 12;           assertEquals(503, ((ObjectDef) resolve.invoke(a)).type);
        game.variousSettings[3] = 8;            assertEquals(502, ((ObjectDef) resolve.invoke(a)).type);
        game.variousSettings[3] = 3;            assertEquals("bits below the low bit ignored", 500, ((ObjectDef) resolve.invoke(a)).type);
        game.variousSettings[3] = 4;            assertNull("child id -1", resolve.invoke(a));
        game.variousSettings[3] = 16;           assertNull("index past children", resolve.invoke(a));
        game.variousSettings[3] = 96;           assertEquals("bits above the high bit masked", 500, ((ObjectDef) resolve.invoke(a)).type);
        game.variousSettings[9] = 2;            assertEquals(502, ((ObjectDef) resolve.invoke(b)).type);
        game.variousSettings[9] = 4;            assertNull(resolve.invoke(b));
        game.variousSettings[9] = -1;           assertNull(resolve.invoke(b));
    }
}
