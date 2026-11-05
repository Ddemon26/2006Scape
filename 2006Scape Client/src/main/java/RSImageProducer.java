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

	public RSImageProducer(int pixelWidth, int pixelHeight, Component component) {
		width = pixelWidth;
		height = pixelHeight;
		pixels = new int[pixelWidth * pixelHeight];
		colorModel = new DirectColorModel(32, 0xff0000, 65280, 255);
		image = component.createImage(this);
		pushFrameToConsumer();
		component.prepareImage(image, this);
		pushFrameToConsumer();
		component.prepareImage(image, this);
		pushFrameToConsumer();
		component.prepareImage(image, this);
		setAsDrawingArea();
	}

	public void setAsDrawingArea() {
		DrawingArea.bindRaster(height, width, pixels);
	}

	public void drawGraphics(int destinationY, Graphics graphics, int destinationX) {
		pushFrameToConsumer();
		graphics.drawImage(image, destinationX, destinationY, this);
	}

	@Override
	public synchronized void addConsumer(ImageConsumer consumer) {
		imageConsumer = consumer;
		consumer.setDimensions(width, height);
		consumer.setProperties(null);
		consumer.setColorModel(colorModel);
		consumer.setHints(14);
	}

	@Override
	public synchronized boolean isConsumer(ImageConsumer consumer) {
		return imageConsumer == consumer;
	}

	@Override
	public synchronized void removeConsumer(ImageConsumer consumer) {
		if (imageConsumer == consumer) {
			imageConsumer = null;
		}
	}

	@Override
	public void startProduction(ImageConsumer consumer) {
		addConsumer(consumer);
	}

	@Override
	public void requestTopDownLeftRightResend(ImageConsumer consumer) {
		System.out.println("TDLR");
	}

	private synchronized void pushFrameToConsumer() {
		if (imageConsumer != null) {
			imageConsumer.setPixels(0, 0, width, height, colorModel, pixels, 0, width);
			imageConsumer.imageComplete(2);
		}
	}

	@Override
	public boolean imageUpdate(Image image, int infoFlags, int x, int y, int width, int height) {
		return true;
	}

	public final int[] pixels;
	private final int width;
	private final int height;
	private final ColorModel colorModel;
	private ImageConsumer imageConsumer;
	private final Image image;
}
