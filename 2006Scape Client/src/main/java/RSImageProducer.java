// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;

final class RSImageProducer implements ImageProducer, ImageObserver {

       public RSImageProducer(int width, int height, Component component) {
               this.width = width;
               this.height = height;
               pixels = new int[width * height];
               colorModel = new DirectColorModel(32, 0xff0000, 65280, 255);
               image = component.createImage(this);
               updateImage();
               component.prepareImage(image, this);
               updateImage();
               component.prepareImage(image, this);
               updateImage();
               component.prepareImage(image, this);
               initDrawingArea();
       }

       public void initDrawingArea() {
               DrawingArea.initDrawingArea(height, width, pixels);
       }

       public void drawGraphics(int i, Graphics g, int k) {
               updateImage();
               g.drawImage(image, k, i, this);
       }

	@Override
       public synchronized void addConsumer(ImageConsumer imageconsumer) {
               this.imageConsumer = imageconsumer;
               imageconsumer.setDimensions(width, height);
               imageconsumer.setProperties(null);
               imageconsumer.setColorModel(colorModel);
               imageconsumer.setHints(14);
       }

	@Override
       public synchronized boolean isConsumer(ImageConsumer imageconsumer) {
               return this.imageConsumer == imageconsumer;
       }

	@Override
       public synchronized void removeConsumer(ImageConsumer imageconsumer) {
               if (this.imageConsumer == imageconsumer) {
                       this.imageConsumer = null;
               }
       }

	@Override
       public void startProduction(ImageConsumer imageconsumer) {
               addConsumer(imageconsumer);
       }

	@Override
       public void requestTopDownLeftRightResend(ImageConsumer imageconsumer) {
               System.out.println("TDLR");
       }

       private synchronized void updateImage() {
               if (imageConsumer != null) {
                       imageConsumer.setPixels(0, 0, width, height, colorModel, pixels, 0, width);
                       imageConsumer.imageComplete(2);
               }
       }

	@Override
	public boolean imageUpdate(Image image, int i, int j, int k, int l, int i1) {
		return true;
	}

       public final int[] pixels;
       private final int width;
       private final int height;
       private final ColorModel colorModel;
       private ImageConsumer imageConsumer;
       private final Image image;
}
