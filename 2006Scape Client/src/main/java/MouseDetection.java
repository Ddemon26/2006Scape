// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

final class MouseDetection implements Runnable {

	@Override
	public void run() {
		while (running) {
			synchronized (sampleLock) {
				if (sampleCount < 500) {
					sampledMouseXs[sampleCount] = clientInstance.mouseX;
					sampledMouseYs[sampleCount] = clientInstance.mouseY;
					sampleCount++;
				}
			}
			try {
				Thread.sleep(50L);
			} catch (Exception sleepFailure) {
			}
		}
	}

	public MouseDetection(Game client) {
		sampleLock = new Object();
		sampledMouseYs = new int[500];
		running = true;
		sampledMouseXs = new int[500];
		clientInstance = client;
	}

	private Game clientInstance;
	public final Object sampleLock;
	public final int[] sampledMouseYs;
	public boolean running;
	public final int[] sampledMouseXs;
	public int sampleCount;
}
