// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

public final class IDK {

	public static void unpackConfig(StreamLoader streamLoader) {
		Stream stream = new Stream(streamLoader.getDataForName("idk.dat"));
		length = stream.readUnsignedWord();
		if (cache == null) {
			cache = new IDK[length];
		}
		for (int j = 0; j < length; j++) {
			if (cache[j] == null) {
				cache[j] = new IDK();
			}
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
                                bodyPartId = stream.readUnsignedByte();
			} else if (i == 2) {
				int j = stream.readUnsignedByte();
                                modelIds = new int[j];
				for (int k = 0; k < j; k++) {
                                        modelIds[k] = stream.readUnsignedWord();
				}

			} else if (i == 3) {
                                nonSelectable = true;
			} else if (i >= 40 && i < 50) {
                                recolorOriginal[i - 40] = stream.readUnsignedWord();
			} else if (i >= 50 && i < 60) {
                                recolorTarget[i - 50] = stream.readUnsignedWord();
			} else if (i >= 60 && i < 70) {
                                headModelIds[i - 60] = stream.readUnsignedWord();
			} else {
				System.out.println("Error unrecognised config code: " + i);
			}
		} while (true);
	}

        public boolean ready() {
                if (modelIds == null) {
                        return true;
                }
                boolean flag = true;
                for (int j = 0; j < modelIds.length; j++) {
                        if (!Model.isLoaded(modelIds[j])) {
                                flag = false;
                        }
                }

                return flag;
        }

        public Model getBodyModel() {
                if (modelIds == null) {
                        return null;
                }
                Model aclass30_sub2_sub4_sub6s[] = new Model[modelIds.length];
                for (int i = 0; i < modelIds.length; i++) {
                        aclass30_sub2_sub4_sub6s[i] = Model.create(modelIds[i]);
                }

		Model model;
                if (aclass30_sub2_sub4_sub6s.length == 1) {
                        model = aclass30_sub2_sub4_sub6s[0];
                } else {
                        model = new Model(aclass30_sub2_sub4_sub6s.length, aclass30_sub2_sub4_sub6s);
                }
                for (int j = 0; j < 6; j++) {
                        if (recolorOriginal[j] == 0) {
                                break;
                        }
                        model.recolor(recolorOriginal[j], recolorTarget[j]);
                }

                return model;
        }

        public boolean headLoaded() {
                boolean flag1 = true;
                for (int i = 0; i < 5; i++) {
                        if (headModelIds[i] != -1 && !Model.isLoaded(headModelIds[i])) {
                                flag1 = false;
                        }
                }

		return flag1;
	}

        public Model getHeadModel() {
                Model aclass30_sub2_sub4_sub6s[] = new Model[5];
                int j = 0;
                for (int k = 0; k < 5; k++) {
                        if (headModelIds[k] != -1) {
                                aclass30_sub2_sub4_sub6s[j++] = Model.create(headModelIds[k]);
                        }
                }

                Model model = new Model(j, aclass30_sub2_sub4_sub6s);
                for (int l = 0; l < 6; l++) {
                        if (recolorOriginal[l] == 0) {
                                break;
                        }
                        model.recolor(recolorOriginal[l], recolorTarget[l]);
                }

                return model;
        }

        private IDK() {
                bodyPartId = -1;
                recolorOriginal = new int[6];
                recolorTarget = new int[6];
                nonSelectable = false;
        }

	public static int length;
	public static IDK cache[];
        public int bodyPartId;
        private int[] modelIds;
        private final int[] recolorOriginal;
        private final int[] recolorTarget;
        private final int[] headModelIds = {-1, -1, -1, -1, -1};
        public boolean nonSelectable;
}
