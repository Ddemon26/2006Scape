import static org.junit.Assert.*;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RSImageProducerTest {
    private int[] savedPixels;
    private int[] savedState;
    private boolean savedViewport;

    @Before public void saveRaster() {
        savedPixels = DrawingArea.pixels;
        savedState = new int[] {DrawingArea.width, DrawingArea.height, DrawingArea.topX,
                DrawingArea.topY, DrawingArea.bottomX, DrawingArea.bottomY,
                DrawingArea.scanlineRightLimit, DrawingArea.clipRightHalf, DrawingArea.clipBottomHalf};
        savedViewport = ClientSettings.FULL_512PX_VIEWPORT;
    }

    @After public void restoreRaster() {
        DrawingArea.pixels = savedPixels;
        DrawingArea.width = savedState[0]; DrawingArea.height = savedState[1];
        DrawingArea.topX = savedState[2]; DrawingArea.topY = savedState[3];
        DrawingArea.bottomX = savedState[4]; DrawingArea.bottomY = savedState[5];
        DrawingArea.scanlineRightLimit = savedState[6];
        DrawingArea.clipRightHalf = savedState[7]; DrawingArea.clipBottomHalf = savedState[8];
        ClientSettings.FULL_512PX_VIEWPORT = savedViewport;
    }

    @Test public void constructorUsesUnequalPixelDimensionsAndBindsSharedRowMajorStorage() {
        for (boolean viewport : new boolean[] {false, true}) {
            ClientSettings.FULL_512PX_VIEWPORT = viewport;
            Fixture component = new Fixture(5, 3, false);
            RSImageProducer producer = new RSImageProducer(5, 3, component);
            assertSame(producer, component.source);
            assertSame(producer.pixels, DrawingArea.pixels);
            assertEquals(15, producer.pixels.length);
            assertArrayEquals(new int[15], producer.pixels);
            assertEquals(5, DrawingArea.width); assertEquals(3, DrawingArea.height);
            assertEquals(0, DrawingArea.topX); assertEquals(0, DrawingArea.topY);
            assertEquals(5, DrawingArea.bottomX); assertEquals(3, DrawingArea.bottomY);
            assertEquals(viewport ? 5 : 4, DrawingArea.scanlineRightLimit);
            DrawingArea.fillRectangle(1, 2, 0x123456, 1, 4);
            assertEquals(0x123456, producer.pixels[14]);
            producer.pixels[6] = 0xabcdef;
            assertEquals(0xabcdef, DrawingArea.pixels[1 + 5]);
            DrawingArea.bindRaster(2, 2, new int[4]);
            producer.setAsDrawingArea();
            assertEquals(0x123456, DrawingArea.pixels[14]);
            assertEquals(0xabcdef, DrawingArea.pixels[6]);
        }
    }

    @Test public void constructorCreatesOneImageAndUploadsBeforeEachOfThreePrepareCalls() {
        Fixture component = new Fixture(4, 2, true);
        RSImageProducer producer = new RSImageProducer(4, 2, component);
        assertEquals(Arrays.asList("create", "dimensions:4x2", "properties", "model", "hints:14",
                "pixels", "complete:2", "prepare", "pixels", "complete:2", "prepare",
                "pixels", "complete:2", "prepare"), component.events);
        assertEquals(3, component.record.frames.size());
        for (int[] frame : component.record.frames) assertArrayEquals(new int[8], frame);
        assertSame(producer.pixels, component.record.storage);
        assertSame(producer, component.observer);
        assertTrue(producer.isConsumer(component.record));
    }

    @Test public void registrationDescribesRgbWithoutUploadingOrCompletingAFrame() {
        Fixture component = new Fixture(3, 5, false);
        RSImageProducer producer = new RSImageProducer(3, 5, component);
        Recording consumer = new Recording(producer, component.events);
        component.events.clear();
        producer.startProduction(consumer);
        assertEquals(Arrays.asList("dimensions:3x5", "properties", "model", "hints:14"), component.events);
        assertNull(consumer.properties);
        assertEquals(ImageConsumer.TOPDOWNLEFTRIGHT | ImageConsumer.COMPLETESCANLINES
                | ImageConsumer.SINGLEPASS, consumer.hints);
        assertTrue(consumer.frames.isEmpty());
        DirectColorModel model = (DirectColorModel) consumer.model;
        assertEquals(32, model.getPixelSize());
        assertEquals(0xff0000, model.getRedMask()); assertEquals(0xff00, model.getGreenMask());
        assertEquals(0xff, model.getBlueMask()); assertEquals(0, model.getAlphaMask());
        assertFalse(model.hasAlpha());
        for (int sample : new int[] {0, 0x123456, 0xab123456, -1}) {
            assertEquals(0xff000000 | (sample & 0xffffff), model.getRGB(sample));
        }
    }

    @Test public void consumerSlotUsesIdentityReplacesPreviousAndOnlyRemovesCurrent() throws Exception {
        Fixture component = new Fixture(3, 2, false);
        RSImageProducer producer = new RSImageProducer(3, 2, component);
        Recording first = new Recording(producer, component.events);
        Recording second = new Recording(producer, component.events);
        assertTrue(first.equals(second)); // Deliberate counterexample to equals-based registration.
        assertTrue(producer.isConsumer(null));
        producer.addConsumer(first); producer.addConsumer(second);
        assertFalse(producer.isConsumer(first)); assertTrue(producer.isConsumer(second));
        producer.removeConsumer(first); upload(producer);
        assertTrue(first.frames.isEmpty()); assertEquals(1, second.frames.size());
        producer.removeConsumer(second); upload(producer);
        assertFalse(producer.isConsumer(second)); assertTrue(producer.isConsumer(null));
        assertEquals(1, second.frames.size());
        producer.startProduction(first); upload(producer);
        assertEquals(1, first.frames.size()); assertFalse(producer.isConsumer(second));
    }

    @Test public void uploadSendsWholeLiveBufferAtOriginWithWidthStrideAndSingleFrameCompletion() throws Exception {
        Fixture component = new Fixture(5, 3, false);
        RSImageProducer producer = new RSImageProducer(5, 3, component);
        Recording consumer = new Recording(producer, component.events);
        producer.addConsumer(consumer);
        for (int index = 0; index < 15; index++) producer.pixels[index] = 0x102030 + index * 17;
        DrawingArea.setClip(2, 1, 3, 1);
        component.events.clear(); upload(producer);
        assertEquals(Arrays.asList("pixels", "complete:2"), component.events);
        assertSame(producer.pixels, consumer.storage);
        assertArrayEquals(producer.pixels, consumer.frames.get(0));
        assertArrayEquals(new int[] {0, 0, 5, 3, 0, 5}, consumer.rectangle);
        assertEquals(ImageConsumer.SINGLEFRAMEDONE, consumer.completion);
        int original = consumer.frames.get(0)[7];
        producer.pixels[7] = 0xabcdef;
        assertEquals(original, consumer.frames.get(0)[7]);
        upload(producer);
        assertEquals(0xabcdef, consumer.frames.get(1)[7]);
        assertEquals(2, consumer.frames.size());
    }

    @Test public void drawingUploadsBeforePlacingTheCreatedImageAtThirdXFirstY() {
        Fixture component = new Fixture(3, 2, false);
        RSImageProducer producer = new RSImageProducer(3, 2, component);
        Recording consumer = new Recording(producer, component.events);
        producer.addConsumer(consumer);
        component.image.setRGB(0, 0, 0xff112233);
        component.image.setRGB(2, 1, 0xff445566);
        consumer.onComplete = () -> component.image.setRGB(1, 0, 0xff778899);
        BufferedImage destination = filledImage(12, 11, 0xffa0b0c0);
        Graphics graphics = destination.getGraphics();
        try { producer.drawGraphics(6, graphics, 4); } finally { graphics.dispose(); }
        for (int y = 0; y < 11; y++) for (int x = 0; x < 12; x++) {
            assertEquals(x >= 4 && x < 7 && y >= 6 && y < 8
                    ? component.image.getRGB(x - 4, y - 6) : 0xffa0b0c0, destination.getRGB(x, y));
        }
        assertEquals(0xff778899, destination.getRGB(5, 6));
        assertEquals(1, consumer.frames.size());
        assertArrayEquals(new int[6], producer.pixels); // Image identity is independent of our fake upload target.
    }

    @Test public void drawingWithoutConsumerStillDrawsAndHonorsGraphicsClipAndNegativeDestination() {
        Fixture component = new Fixture(3, 2, false);
        RSImageProducer producer = new RSImageProducer(3, 2, component);
        component.image.setRGB(1, 0, 0xff123456); component.image.setRGB(2, 1, 0xffabcdef);
        BufferedImage destination = filledImage(4, 4, 0xff998877);
        Graphics graphics = destination.getGraphics(); graphics.setClip(0, 1, 1, 2);
        try { producer.drawGraphics(1, graphics, -1); } finally { graphics.dispose(); }
        assertEquals(0xff123456, destination.getRGB(0, 1));
        assertEquals(0xff998877, destination.getRGB(1, 2));
        assertEquals(0xff998877, destination.getRGB(0, 0));
    }

    @Test public void resendOnlyPrintsDiagnosticAndObserverAlwaysRequestsFurtherUpdates() {
        Fixture component = new Fixture(3, 2, false);
        RSImageProducer producer = new RSImageProducer(3, 2, component);
        Recording current = new Recording(producer, component.events);
        Recording other = new Recording(producer, component.events);
        producer.addConsumer(current); component.events.clear();
        ByteArrayOutputStream output = new ByteArrayOutputStream(); PrintStream previous = System.out;
        try {
            System.setOut(new PrintStream(output));
            producer.requestTopDownLeftRightResend(other);
            producer.requestTopDownLeftRightResend(null);
        } finally { System.setOut(previous); }
        assertEquals("TDLR" + System.lineSeparator() + "TDLR" + System.lineSeparator(), output.toString());
        assertTrue(component.events.isEmpty()); assertTrue(producer.isConsumer(current));
        for (int flags : new int[] {0, ImageObserver.WIDTH, ImageObserver.HEIGHT, ImageObserver.SOMEBITS,
                ImageObserver.FRAMEBITS, ImageObserver.ALLBITS, ImageObserver.ERROR, ImageObserver.ABORT, -1}) {
            assertTrue(producer.imageUpdate(component.image, flags, 7, -3, 2, 5));
            assertTrue(producer.imageUpdate(null, flags, Integer.MIN_VALUE, Integer.MAX_VALUE, -1, 0));
        }
        assertTrue(component.events.isEmpty());
    }

    @Test public void zeroDimensionsStillRegisterAndUploadEmptyStorageAndNullRegistrationClearsSlot() throws Exception {
        for (int[] dimensions : new int[][] {{0, 3}, {4, 0}, {0, 0}}) {
            Fixture component = new Fixture(1, 1, false);
            RSImageProducer producer = new RSImageProducer(dimensions[0], dimensions[1], component);
            Recording consumer = new Recording(producer, component.events);
            producer.addConsumer(consumer); upload(producer);
            assertEquals(0, producer.pixels.length);
            assertArrayEquals(new int[] {0, 0, dimensions[0], dimensions[1], 0, dimensions[0]}, consumer.rectangle);
            assertEquals(ImageConsumer.SINGLEFRAMEDONE, consumer.completion);
            try { producer.addConsumer(null); fail("Null registration should retain original failure"); }
            catch (NullPointerException expected) { assertTrue(producer.isConsumer(null)); }
            upload(producer); assertEquals(1, consumer.frames.size());
        }
    }

    private static void upload(RSImageProducer producer) throws Exception {
        Method method = RSImageProducer.class.getDeclaredMethod("pushFrameToConsumer");
        method.setAccessible(true); method.invoke(producer);
    }

    private static BufferedImage filledImage(int width, int height, int color) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) for (int x = 0; x < width; x++) image.setRGB(x, y, color);
        return image;
    }

    private static final class Fixture extends Component {
        final BufferedImage image;
        final boolean autoRegister;
        final List<String> events = new ArrayList<>();
        RSImageProducer source;
        ImageObserver observer;
        Recording record;
        Fixture(int width, int height, boolean autoRegister) {
            image = filledImage(width, height, 0xff000000); this.autoRegister = autoRegister;
        }
        @Override public Image createImage(ImageProducer producer) {
            events.add("create"); source = (RSImageProducer) producer;
            if (autoRegister) { record = new Recording(source, events); source.addConsumer(record); }
            return image;
        }
        @Override public boolean prepareImage(Image requested, ImageObserver requestedObserver) {
            assertSame(image, requested); assertSame(source, requestedObserver);
            observer = requestedObserver; events.add("prepare"); return false;
        }
    }

    private static final class Recording implements ImageConsumer {
        final RSImageProducer owner;
        final List<String> events;
        final List<int[]> frames = new ArrayList<>();
        ColorModel model;
        Hashtable<?, ?> properties;
        int hints, completion;
        int[] storage, rectangle;
        Runnable onComplete;
        Recording(RSImageProducer owner, List<String> events) { this.owner = owner; this.events = events; }
        private void locked() { assertTrue("Consumer callback must hold producer monitor", Thread.holdsLock(owner)); }
        @Override public void setDimensions(int width, int height) { locked(); events.add("dimensions:" + width + "x" + height); }
        @Override public void setProperties(Hashtable<?, ?> properties) { locked(); this.properties = properties; events.add("properties"); }
        @Override public void setColorModel(ColorModel model) { locked(); this.model = model; events.add("model"); }
        @Override public void setHints(int hints) { locked(); this.hints = hints; events.add("hints:" + hints); }
        @Override public void setPixels(int x, int y, int width, int height, ColorModel model, byte[] pixels, int offset, int stride) {
            fail("Expected integer pixels, not indexed bytes");
        }
        @Override public void setPixels(int x, int y, int width, int height, ColorModel model, int[] pixels, int offset, int stride) {
            locked(); assertSame(this.model, model); storage = pixels;
            rectangle = new int[] {x, y, width, height, offset, stride};
            frames.add(pixels.clone()); events.add("pixels");
        }
        @Override public void imageComplete(int status) {
            locked(); completion = status; events.add("complete:" + status);
            if (onComplete != null) onComplete.run();
        }
        @Override public boolean equals(Object other) { return other instanceof Recording; }
        @Override public int hashCode() { return 1; }
    }
}
