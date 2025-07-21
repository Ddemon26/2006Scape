// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

public final class SpotAnim {

	public static void unpackConfig(StreamLoader streamLoader) {
		Stream stream = new Stream(streamLoader.getFileData("spotanim.dat"));
		int length = stream.readUnsignedWord();
		if (cache == null) {
			cache = new SpotAnim[length];
		}
		for (int j = 0; j < length; j++) {
			if (cache[j] == null) {
				cache[j] = new SpotAnim();
			}
                        cache[j].id = j;
                        cache[j].readValues(stream);
                }

	}

	private void readValues(Stream stream) {
		do {
			int i = stream.readUnsignedByte();
			if (i == 0) {
				return;
			}
			if (i == 1) {
                                modelId = stream.readUnsignedWord();
			} else if (i == 2) {
                                animationId = stream.readUnsignedWord();
                                if (Animation.anims != null) {
                                        animation = Animation.anims[animationId];
                                }
			} else if (i == 4) {
                                scaleX = stream.readUnsignedWord();
			} else if (i == 5) {
                                scaleY = stream.readUnsignedWord();
			} else if (i == 6) {
                                rotation = stream.readUnsignedWord();
			} else if (i == 7) {
                                ambient = stream.readUnsignedByte();
			} else if (i == 8) {
                                contrast = stream.readUnsignedByte();
			} else if (i >= 40 && i < 50) {
                                originalModelColors[i - 40] = stream.readUnsignedWord();
			} else if (i >= 50 && i < 60) {
                                modifiedModelColors[i - 50] = stream.readUnsignedWord();
			} else {
				System.out.println("Error unrecognised spotanim config code: " + i);
			}
		} while (true);
	}

        public Model getModel() {
                Model model = (Model) modelCache.get(id);
                if (model != null) {
                        return model;
                }
                model = Model.create(modelId);
                if (model == null) {
                        return null;
                }
                for (int i = 0; i < 6; i++) {
                        if (originalModelColors[0] != 0) {
                                model.recolor(originalModelColors[i], modifiedModelColors[i]);
                        }
                }

                modelCache.put(model, id);
                return model;
        }

	private SpotAnim() {
                animationId = -1;
                originalModelColors = new int[6];
                modifiedModelColors = new int[6];
                scaleX = 128;
                scaleY = 128;
        }

        public static SpotAnim cache[];
        private int id;
        private int modelId;
        private int animationId;
        public Animation animation;
        private final int[] originalModelColors;
        private final int[] modifiedModelColors;
        public int scaleX;
        public int scaleY;
        public int rotation;
        public int ambient;
        public int contrast;
        public static MRUCache modelCache = new MRUCache(30);

}
