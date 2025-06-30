// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3)

public final class AnimFrame {

    public static void init(int capacity) {
        frames = new AnimFrame[capacity + 1];
        validFrame = new boolean[capacity + 1];
        for (int j = 0; j < capacity + 1; j++) {
            validFrame[j] = true;
        }

    }

       public static void load(byte[] data) {
               Stream stream = new Stream(data);
               stream.currentOffset = data.length - 8;
		int i = stream.readUnsignedWord();
		int j = stream.readUnsignedWord();
		int k = stream.readUnsignedWord();
		int l = stream.readUnsignedWord();
		int i1 = 0;
               Stream stream_1 = new Stream(data);
		stream_1.currentOffset = i1;
		i1 += i + 2;
               Stream stream_2 = new Stream(data);
		stream_2.currentOffset = i1;
		i1 += j;
               Stream stream_3 = new Stream(data);
		stream_3.currentOffset = i1;
		i1 += k;
               Stream stream_4 = new Stream(data);
		stream_4.currentOffset = i1;
		i1 += l;
               Stream stream_5 = new Stream(data);
		stream_5.currentOffset = i1;
                FrameBase base = new FrameBase(stream_5);
		int k1 = stream_1.readUnsignedWord();
		int ai[] = new int[500];
		int ai1[] = new int[500];
		int ai2[] = new int[500];
		int ai3[] = new int[500];
		for (int l1 = 0; l1 < k1; l1++) {
			int i2 = stream_1.readUnsignedWord();
                AnimFrame frame = frames[i2] = new AnimFrame();
                frame.delay = stream_4.readUnsignedByte();
                frame.frameBase = base;
			int j2 = stream_1.readUnsignedByte();
			int k2 = -1;
			int l2 = 0;
			for (int i3 = 0; i3 < j2; i3++) {
				int j3 = stream_2.readUnsignedByte();
				if (j3 > 0) {
                                        if (base.transformationType[i3] != 0) {
						for (int l3 = i3 - 1; l3 > k2; l3--) {
                                                        if (base.transformationType[l3] != 0) {
								continue;
							}
							ai[l2] = l3;
							ai1[l2] = 0;
							ai2[l2] = 0;
							ai3[l2] = 0;
							l2++;
							break;
						}

					}
					ai[l2] = i3;
					char c = '\0';
                                        if (base.transformationType[i3] == 3) {
						c = '\200';
					}
					if ((j3 & 1) != 0) {
						ai1[l2] = stream_3.method421();
					} else {
						ai1[l2] = c;
					}
					if ((j3 & 2) != 0) {
						ai2[l2] = stream_3.method421();
					} else {
						ai2[l2] = c;
					}
					if ((j3 & 4) != 0) {
						ai3[l2] = stream_3.method421();
					} else {
						ai3[l2] = c;
					}
					k2 = i3;
					l2++;
                                        if (base.transformationType[i3] == 5) {
                                                validFrame[i2] = false;
					}
				}
			}

                        frame.transformationCount = l2;
                        frame.transformationIndices = new int[l2];
                        frame.transformX = new int[l2];
                        frame.transformY = new int[l2];
                        frame.transformZ = new int[l2];
                        for (int k3 = 0; k3 < l2; k3++) {
                                frame.transformationIndices[k3] = ai[k3];
                                frame.transformX[k3] = ai1[k3];
                                frame.transformY[k3] = ai2[k3];
                                frame.transformZ[k3] = ai3[k3];
                        }

		}

	}

       public static void clear() {
               frames = null;
       }

       public static AnimFrame forId(int id) {
               if (frames == null) {
                       return null;
               } else {
                       return frames[id];
               }
       }

       public static boolean isNullFrame(int id) {
               return id == -1;
       }

       private AnimFrame() {
       }

       private static AnimFrame[] frames;
       public int delay;
       public FrameBase frameBase;
       public int transformationCount;
       public int[] transformationIndices;
       public int[] transformX;
       public int[] transformY;
       public int[] transformZ;
       private static boolean[] validFrame;

}
