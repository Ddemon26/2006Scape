# RSImageProducer

Defined in [`2006Scape Client/src/main/java/graphics/RSImageProducer.java`](2006Scape Client/src/main/java/graphics/RSImageProducer.java).

RSImage Producer helper class.

```java
final class RSImageProducer implements ImageProducer, ImageObserver {
public RSImageProducer(int width, int height, Component component)
public void initDrawingArea()
public void drawGraphics(int i, Graphics g, int k)
public synchronized void addConsumer(ImageConsumer imageconsumer)
public synchronized boolean isConsumer(ImageConsumer imageconsumer)
public synchronized void removeConsumer(ImageConsumer imageconsumer)
public void startProduction(ImageConsumer imageconsumer)
public void requestTopDownLeftRightResend(ImageConsumer imageconsumer)
public boolean imageUpdate(Image image, int i, int j, int k, int l, int i1)
```
