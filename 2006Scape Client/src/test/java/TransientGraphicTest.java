import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class TransientGraphicTest {
    private SpotAnim[] old;

    @Before public void save() { old = SpotAnim.cache; }
    @After public void restore() { SpotAnim.cache = old; }

    private static Animation sequence(int... delays) throws Exception {
        Constructor<Animation> constructor = Animation.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Animation result = constructor.newInstance();
        result.frameCount = delays.length;
        Field field = Animation.class.getDeclaredField("frameDelays");
        field.setAccessible(true);
        field.set(result, delays);
        return result;
    }

    private static SpotAnim spot(Animation animation) throws Exception {
        Constructor<SpotAnim> constructor = SpotAnim.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        SpotAnim result = constructor.newInstance();
        result.animation = animation;
        return result;
    }

    private static int privateInt(StationaryGraphic object, String name) throws Exception {
        Field field = StationaryGraphic.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.getInt(object);
    }

    private static Object privateObject(StationaryGraphic object, String name) throws Exception {
        Field field = StationaryGraphic.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(object);
    }

    private static Game game() throws Exception {
        Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (Game) ((sun.misc.Unsafe) field.get(null)).allocateInstance(Game.class);
    }

    private static Ground[][][] grounds(WorldController scene) throws Exception {
        Field field = WorldController.class.getDeclaredField("groundArray");
        field.setAccessible(true);
        return (Ground[][][]) field.get(scene);
    }

    @Test public void case1() throws Exception {
        SpotAnim first = spot(sequence(2));
        SpotAnim second = spot(sequence(3));
        SpotAnim.cache = new SpotAnim[]{first, second};
        StationaryGraphic a = new StationaryGraphic(2, 100, 7, 1, 345, 260, 388);
        assertEquals(2, a.plane);
        assertEquals(388, a.worldY);
        assertEquals(260, a.worldX);
        assertEquals(345, a.height);
        assertEquals(107, a.startCycle);
        assertFalse(a.finished);
        assertSame(second, privateObject(a, "spotAnimation"));
        StationaryGraphic b = new StationaryGraphic(0, 100, 0, 0, 8, 128, 256);
        assertEquals(100, b.startCycle);
        assertSame(first, privateObject(b, "spotAnimation"));
    }

    @Test public void case2() throws Exception {
        SpotAnim.cache = new SpotAnim[]{spot(sequence(2, 3))};
        StationaryGraphic a = new StationaryGraphic(0, 0, 0, 0, 0, 64, 64);
        a.advanceAnimation(2);
        assertEquals(0, privateInt(a, "frameIndex"));
        assertEquals(2, privateInt(a, "frameTicks"));
        assertFalse(a.finished);
        a.advanceAnimation(1);
        assertEquals(1, privateInt(a, "frameIndex"));
        assertEquals(0, privateInt(a, "frameTicks"));
        a.advanceAnimation(4);
        assertEquals(0, privateInt(a, "frameIndex"));
        assertEquals(0, privateInt(a, "frameTicks"));
        assertTrue(a.finished);
    }

    @Test public void case3() throws Exception {
        SpotAnim.cache = new SpotAnim[]{spot(sequence(1))};
        Game g = game();
        g.transientGraphics = new NodeList();
        g.worldController = new WorldController(new int[4][105][105]);
        g.plane = 1;
        g.loopCycle = 11;
        g.cyclesSinceDraw = 1;
        StationaryGraphic delayed = new StationaryGraphic(1, 10, 2, 0, 55, 320, 448);
        StationaryGraphic other = new StationaryGraphic(2, 10, 0, 0, 66, 576, 704);
        g.transientGraphics.insertHead(delayed);
        g.transientGraphics.insertHead(other);
        g.updateTransientGraphics();
        assertSame(delayed, g.transientGraphics.firstFromTail());
        assertNull(g.transientGraphics.nextTowardHead());
        assertNull(grounds(g.worldController)[1][3][2]);
        g.loopCycle = 12;
        g.updateTransientGraphics();
        Ground tile = grounds(g.worldController)[1][3][2];
        assertNotNull(tile);
        assertSame(delayed, tile.obj5Array[0].model);
        assertEquals(55, tile.obj5Array[0].height);
        assertEquals(448, tile.obj5Array[0].worldX);
        assertEquals(320, tile.obj5Array[0].worldY);
        assertSame(delayed, g.transientGraphics.firstFromTail());
        g.cyclesSinceDraw = 2;
        g.updateTransientGraphics();
        assertTrue(delayed.finished);
        assertNull(g.transientGraphics.firstFromTail());
    }
}
