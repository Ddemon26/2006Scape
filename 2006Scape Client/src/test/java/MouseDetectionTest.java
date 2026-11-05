import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;
import static org.junit.Assert.*;

public class MouseDetectionTest {
    private static Game game() throws Exception {
        Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (Game) ((sun.misc.Unsafe) field.get(null)).allocateInstance(Game.class);
    }

    // Hold the producer's monitor until its worker has passed the running check.
    // Setting running=false before release permits exactly one original iteration.
    private static Throwable iteration(MouseDetection detector, boolean interrupt) throws Exception {
        AtomicReference<Throwable> failure = new AtomicReference<>();
        Thread worker = new Thread(detector, "mouse-semantic-test");
        worker.setDaemon(true);
        worker.setUncaughtExceptionHandler((thread, error) -> failure.set(error));
        synchronized (detector.sampleLock) {
            detector.running = true;
            worker.start();
            long deadline = System.nanoTime() + 3000000000L;
            while (worker.getState() != Thread.State.BLOCKED && System.nanoTime() < deadline) Thread.yield();
            detector.running = false;
            if (interrupt) worker.interrupt();
            assertEquals("worker must wait on the sample monitor", Thread.State.BLOCKED, worker.getState());
        }
        worker.join(3000);
        assertFalse("worker failed to terminate", worker.isAlive());
        if (interrupt) assertFalse("sleep catches and clears the interrupt", worker.isInterrupted());
        return failure.get();
    }

    @Test public void constructorCreatesIndependentEmptyBuffersAndBindsTheSuppliedClient() throws Exception {
        Game first = game(), second = game();
        first.mouseX = 17; first.mouseY = 93;
        second.mouseX = 201; second.mouseY = 38;
        MouseDetection a = new MouseDetection(first), b = new MouseDetection(second);
        assertTrue(a.running); assertEquals(0, a.sampleCount);
        assertEquals(500, a.sampledMouseXs.length); assertEquals(500, a.sampledMouseYs.length);
        assertArrayEquals(new int[500], a.sampledMouseXs); assertArrayEquals(new int[500], a.sampledMouseYs);
        assertNotSame(a.sampledMouseXs, a.sampledMouseYs); assertNotSame(a.sampledMouseXs, b.sampledMouseXs);
        assertNotSame(a.sampledMouseYs, b.sampledMouseYs); assertNotSame(a.sampleLock, b.sampleLock);
        assertNull(iteration(a, false)); assertNull(iteration(b, false));
        assertEquals(17, a.sampledMouseXs[0]); assertEquals(93, a.sampledMouseYs[0]);
        assertEquals(201, b.sampledMouseXs[0]); assertEquals(38, b.sampledMouseYs[0]);
        first.mouseX = 71; first.mouseY = 39;
        assertNull(iteration(a, false));
        assertEquals(2, a.sampleCount); assertEquals(71, a.sampledMouseXs[1]); assertEquals(39, a.sampledMouseYs[1]);
    }

    @Test public void samplesRawOrderedAxesIncludingRepeatedExitAndOutOfViewportValues() throws Exception {
        Game client = game(); MouseDetection detector = new MouseDetection(client);
        int[][] points = {{23,91},{23,91},{-1,-1},{-1,55},{88,-1},{764,502},{765,503},
                {-40,900},{Integer.MIN_VALUE,Integer.MAX_VALUE}};
        Arrays.fill(detector.sampledMouseXs, 123456); Arrays.fill(detector.sampledMouseYs, -654321);
        for (int index = 0; index < points.length; index++) {
            client.mouseX = points[index][0]; client.mouseY = points[index][1];
            assertNull(iteration(detector, false));
            assertEquals(index + 1, detector.sampleCount);
            for (int prior = 0; prior <= index; prior++) {
                assertEquals(points[prior][0], detector.sampledMouseXs[prior]);
                assertEquals(points[prior][1], detector.sampledMouseYs[prior]);
            }
            assertEquals(123456, detector.sampledMouseXs[index + 1]);
            assertEquals(-654321, detector.sampledMouseYs[index + 1]);
        }
    }

    @Test public void lastSlotSaturatesWithoutWrappingAndConsumerResetReusesPrefix() throws Exception {
        Game client = game(); client.mouseX = 73; client.mouseY = 19;
        MouseDetection detector = new MouseDetection(client);
        Arrays.fill(detector.sampledMouseXs, 222); Arrays.fill(detector.sampledMouseYs, 333);
        detector.sampleCount = 499;
        assertNull(iteration(detector, false)); assertEquals(500, detector.sampleCount);
        assertEquals(222, detector.sampledMouseXs[498]); assertEquals(333, detector.sampledMouseYs[498]);
        assertEquals(73, detector.sampledMouseXs[499]); assertEquals(19, detector.sampledMouseYs[499]);
        int[] xs = detector.sampledMouseXs.clone(), ys = detector.sampledMouseYs.clone();
        client.mouseX = 456; client.mouseY = 321;
        assertNull(iteration(detector, false)); assertEquals(500, detector.sampleCount);
        assertArrayEquals(xs, detector.sampledMouseXs); assertArrayEquals(ys, detector.sampledMouseYs);
        detector.sampleCount = 501;
        assertNull(iteration(detector, false)); assertEquals(501, detector.sampleCount);
        assertArrayEquals(xs, detector.sampledMouseXs); assertArrayEquals(ys, detector.sampledMouseYs);
        detector.sampleCount = 0;
        assertNull(iteration(detector, false)); assertEquals(1, detector.sampleCount);
        assertEquals(456, detector.sampledMouseXs[0]); assertEquals(321, detector.sampledMouseYs[0]);
        assertEquals(73, detector.sampledMouseXs[499]); assertEquals(19, detector.sampledMouseYs[499]);
    }

    @Test public void stoppedLoopDoesNotTouchEvenAnAbsentClient() {
        MouseDetection detector = new MouseDetection(null);
        detector.running = false; detector.run();
        assertEquals(0, detector.sampleCount); assertArrayEquals(new int[500], detector.sampledMouseXs);
    }

    @Test public void sharedMonitorGuardsBothCoordinateReadsAndCountPublication() throws Exception {
        Game client = game(); client.mouseX = 1; client.mouseY = 2;
        MouseDetection detector = new MouseDetection(client);
        Thread worker = new Thread(detector); worker.setDaemon(true);
        synchronized (detector.sampleLock) {
            worker.start(); long deadline = System.nanoTime() + 3000000000L;
            while (worker.getState() != Thread.State.BLOCKED && System.nanoTime() < deadline) Thread.yield();
            detector.running = false;
            assertEquals(Thread.State.BLOCKED, worker.getState());
            assertEquals(0, detector.sampleCount);
            assertEquals(0, detector.sampledMouseXs[0]); assertEquals(0, detector.sampledMouseYs[0]);
            client.mouseX = 412; client.mouseY = 107;
        }
        worker.join(3000); assertFalse(worker.isAlive());
        assertEquals(1, detector.sampleCount);
        assertEquals(412, detector.sampledMouseXs[0]); assertEquals(107, detector.sampledMouseYs[0]);
    }

    @Test public void interruptedSleepIsCaughtAfterSampling() throws Exception {
        Game client = game(); client.mouseX = 63; client.mouseY = 129;
        MouseDetection detector = new MouseDetection(client);
        assertNull(iteration(detector, true)); assertEquals(1, detector.sampleCount);
        assertEquals(63, detector.sampledMouseXs[0]); assertEquals(129, detector.sampledMouseYs[0]);
    }

    @Test public void invalidInputFailuresAndFullBufferReadSuppressionRemainObservable() throws Exception {
        MouseDetection absent = new MouseDetection(null);
        assertTrue(iteration(absent, false) instanceof NullPointerException);
        assertEquals(0, absent.sampleCount);
        absent.sampleCount = 500;
        assertNull(iteration(absent, false)); assertEquals(500, absent.sampleCount);
        MouseDetection negative = new MouseDetection(game()); negative.sampleCount = -1;
        assertTrue(iteration(negative, false) instanceof ArrayIndexOutOfBoundsException);
        assertEquals(-1, negative.sampleCount);
    }
}
