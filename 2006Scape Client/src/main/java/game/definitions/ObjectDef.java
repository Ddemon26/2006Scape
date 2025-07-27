package game.definitions;// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

import core.engine.Game;
import core.managers.OnDemandFetcher;
import core.network.Stream;
import core.network.StreamLoader;
import game.animation.AnimFrame;
import render.geometry.Model;
import util.collections.MRUCache;

public final class ObjectDef {

	public static ObjectDef forID(int i) {
		for (int j = 0; j < 20; j++) {
			if (cache[j].type == i) {
				return cache[j];
			}
		}

		cacheIndex = (cacheIndex + 1) % 20;
		ObjectDef class46 = cache[cacheIndex];
		stream.currentOffset = streamIndices[i];
		class46.type = i;
		class46.setDefaults();
		class46.decode(stream);
		if (i == 6) {
			class46.actions[1] = "Load";
			class46.actions[2] = "Pick-up";
		}
		switch (i) {

		}
		return class46;
	}

	private void setDefaults() {
		modelIds = null;
		modelTypes = null;
		name = null;
		description = null;
		modifiedModelColors = null;
		originalModelColors = null;
		sizeX = 1;
		sizeY = 1;
		isSolid = true;
		impenetrable = true;
		interactive = false;
		contouredGround = false;
		delayedShading = false;
		adjustToTerrain = false;
		animationId = -1;
		wallDecoOffset = 16;
		ambient = 0;
		contrast = 0;
		actions = null;
		mapIconId = -1;
                mapSceneId = -1;
		mirrorOnRotate = false;
		clipped = true;
		scaleX = 128;
		scaleY = 128;
		scaleZ = 128;
                defaultOrientation = 0;
                offsetX = 0;
                offsetY = 0;
                offsetZ = 0;
		occludes = false;
		hollow = false;
                supportsItems = -1;
                varbitId = -1;
                varpId = -1;
		childrenIDs = null;
	}

	public void requestModels(OnDemandFetcher class42_sub1) {
		if (modelIds == null) {
			return;
		}
                for (int element : modelIds) {
                        class42_sub1.requestFileNow(element & 0xffff, 0);
                }
	}

	public static void nullLoader() {
		mruNodes1 = null;
		mruNodes2 = null;
		streamIndices = null;
		cache = null;
		stream = null;
	}

	public static int totalObjects;

	public static void unpackConfig(StreamLoader streamLoader) {
		stream = new Stream(streamLoader.getFileData("loc.dat"));
		Stream stream = new Stream(streamLoader.getFileData("loc.idx"));
		totalObjects = stream.readUnsignedWord();
		streamIndices = new int[totalObjects];
		int i = 2;
		for (int j = 0; j < totalObjects; j++) {
			streamIndices[j] = i;
			i += stream.readUnsignedWord();
		}

		cache = new ObjectDef[20];
		for (int k = 0; k < 20; k++) {
			cache[k] = new ObjectDef();
		}

	}

	public boolean isModelReady(int i) {
		if (modelTypes == null) {
			if (modelIds == null) {
				return true;
			}
			if (i != 10) {
				return true;
			}
			boolean flag1 = true;
			for (int element : modelIds) {
				flag1 &= Model.isLoaded(element & 0xffff);
			}

			return flag1;
		}
		for (int j = 0; j < modelTypes.length; j++) {
			if (modelTypes[j] == i) {
				return Model.isLoaded(modelIds[j] & 0xffff);
			}
		}

		return true;
	}

	public Model getModel(int i, int j, int k, int l, int i1, int j1, int k1) {
		Model model = buildModel(i, k1, j);
		if (model == null) {
			return null;
		}
		if (contouredGround || delayedShading) {
			model = new Model(contouredGround, delayedShading, model);
		}
		if (contouredGround) {
			int l1 = (k + l + i1 + j1) / 4;
			for (int i2 = 0; i2 < model.vertexCount; i2++) {
				int j2 = model.vertexX[i2];
				int k2 = model.vertexZ[i2];
				int l2 = k + (l - k) * (j2 + 64) / 128;
				int i3 = j1 + (i1 - j1) * (j2 + 64) / 128;
				int j3 = l2 + (i3 - l2) * (k2 + 64) / 128;
				model.vertexY[i2] += j3 - l1;
			}

			model.calculateBoundsY();
		}
		return model;
	}

	public boolean areModelsReady() {
		if (modelIds == null) {
			return true;
		}
		boolean flag1 = true;
		for (int element : modelIds) {
			flag1 &= Model.isLoaded(element & 0xffff);
		}
		return flag1;
	}

	public ObjectDef getChildDefinition() {
		int i = -1;
            if (varbitId != -1) {
                    VarBit varBit = VarBit.cache[varbitId];
                        int j = varBit.configId;
                        int k = varBit.leastSignificantBit;
                        int l = varBit.mostSignificantBit;
			int i1 = Game.bitMasks[l - k];
			i = clientInstance.variousSettings[j] >> k & i1;
            } else if (varpId != -1) {
                    i = clientInstance.variousSettings[varpId];
		}
		if (i < 0 || i >= childrenIDs.length || childrenIDs[i] == -1) {
			return null;
		} else {
			return forID(childrenIDs[i]);
		}
	}

	private Model buildModel(int j, int k, int l) {
		Model model = null;
		long l1;
		if (modelTypes == null) {
			if (j != 10) {
				return null;
			}
			l1 = (type << 6) + l + ((long) (k + 1) << 32);
                    Model model_1 = (Model) mruNodes2.get(l1);
			if (model_1 != null) {
				return model_1;
			}
			if (modelIds == null) {
				return null;
			}
			boolean flag1 = mirrorOnRotate ^ l > 3;
			int k1 = modelIds.length;
			for (int i2 = 0; i2 < k1; i2++) {
				int l2 = modelIds[i2];
				if (flag1) {
					l2 += 0x10000;
				}
                            model = (Model) mruNodes1.get(l2);
				if (model == null) {
					model = Model.create(l2 & 0xffff);
					if (model == null) {
						return null;
					}
					if (flag1) {
						model.mirror();
					}
                                    mruNodes1.put(model, l2);
				}
				if (k1 > 1) {
                                tempModelArray[i2] = model;
				}
			}

			if (k1 > 1) {
                            model = new Model(k1, tempModelArray);
			}
		} else {
			int i1 = -1;
			for (int j1 = 0; j1 < modelTypes.length; j1++) {
				if (modelTypes[j1] != j) {
					continue;
				}
				i1 = j1;
				break;
			}

			if (i1 == -1) {
				return null;
			}
			l1 = (type << 6) + (i1 << 3) + l + ((long) (k + 1) << 32);
                    Model model_2 = (Model) mruNodes2.get(l1);
			if (model_2 != null) {
				return model_2;
			}
			int j2 = modelIds[i1];
			boolean flag3 = mirrorOnRotate ^ l > 3;
			if (flag3) {
				j2 += 0x10000;
			}
                    model = (Model) mruNodes1.get(j2);
			if (model == null) {
				model = Model.create(j2 & 0xffff);
				if (model == null) {
					return null;
				}
				if (flag3) {
					model.mirror();
				}
                            mruNodes1.put(model, j2);
			}
		}
		boolean flag;
		flag = scaleX != 128 || scaleY != 128 || scaleZ != 128;
		boolean flag2;
            flag2 = offsetX != 0 || offsetY != 0 || offsetZ != 0;
                Model model_3 = new Model(modifiedModelColors == null, AnimFrame.isNullFrame(k), l == 0 && k == -1 && !flag && !flag2, model);
		if (k != -1) {
			model_3.buildVertexGroups();
			model_3.applyFrame(k);
			model_3.faceGroups = null;
			model_3.vertexGroups = null;
		}
		while (l-- > 0) {
			model_3.calculateNormals();
		}
		if (modifiedModelColors != null) {
			for (int k2 = 0; k2 < modifiedModelColors.length; k2++) {
				model_3.recolor(modifiedModelColors[k2], originalModelColors[k2]);
			}

		}
		if (flag) {
			model_3.scaleModel(scaleX, scaleZ, scaleY);
		}
		if (flag2) {
                    model_3.translate(offsetX, offsetY, offsetZ);
		}
		model_3.applyLighting(64 + ambient, 768 + contrast * 5, -50, -10, -50, !delayedShading);
            if (supportsItems == 1) {
			model_3.overrideHeight = model_3.modelHeight;
		}
            mruNodes2.put(model_3, l1);
		return model_3;
	}

	private void decode(Stream stream) {
		int i = -1;
		label0 : do {
			int j;
			do {
				j = stream.readUnsignedByte();
				if (j == 0) {
					break label0;
				}
				if (j == 1) {
					int k = stream.readUnsignedByte();
					if (k > 0) {
						if (modelIds == null || lowMem) {
							modelTypes = new int[k];
							modelIds = new int[k];
							for (int k1 = 0; k1 < k; k1++) {
								modelIds[k1] = stream.readUnsignedWord();
								modelTypes[k1] = stream.readUnsignedByte();
							}

						} else {
							stream.currentOffset += k * 3;
						}
					}
				} else if (j == 2) {
					name = stream.readString();
				} else if (j == 3) {
					description = stream.readBytes();
				} else if (j == 5) {
					int l = stream.readUnsignedByte();
					if (l > 0) {
						if (modelIds == null || lowMem) {
							modelTypes = null;
							modelIds = new int[l];
							for (int l1 = 0; l1 < l; l1++) {
								modelIds[l1] = stream.readUnsignedWord();
							}

						} else {
							stream.currentOffset += l * 2;
						}
					}
				} else if (j == 14) {
					sizeX = stream.readUnsignedByte();
				} else if (j == 15) {
					sizeY = stream.readUnsignedByte();
				} else if (j == 17) {
					isSolid = false;
				} else if (j == 18) {
					impenetrable = false;
				} else if (j == 19) {
					i = stream.readUnsignedByte();
					if (i == 1) {
						interactive = true;
					}
				} else if (j == 21) {
					contouredGround = true;
				} else if (j == 22) {
					delayedShading = true;
				} else if (j == 23) {
					adjustToTerrain = true;
				} else if (j == 24) {
					animationId = stream.readUnsignedWord();
					if (animationId == 65535) {
						animationId = -1;
					}
				} else if (j == 28) {
					wallDecoOffset = stream.readUnsignedByte();
				} else if (j == 29) {
					ambient = stream.readSignedByte();
				} else if (j == 39) {
					contrast = stream.readSignedByte();
				} else if (j >= 30 && j < 39) {
					if (actions == null) {
						actions = new String[5];
					}
					actions[j - 30] = stream.readString();
					if (actions[j - 30].equalsIgnoreCase("hidden")) {
						actions[j - 30] = null;
					}
				} else if (j == 40) {
					int i1 = stream.readUnsignedByte();
					modifiedModelColors = new int[i1];
					originalModelColors = new int[i1];
					for (int i2 = 0; i2 < i1; i2++) {
						modifiedModelColors[i2] = stream.readUnsignedWord();
						originalModelColors[i2] = stream.readUnsignedWord();
					}

				} else if (j == 60) {
					mapIconId = stream.readUnsignedWord();
				} else if (j == 62) {
					mirrorOnRotate = true;
				} else if (j == 64) {
					clipped = false;
				} else if (j == 65) {
					scaleX = stream.readUnsignedWord();
				} else if (j == 66) {
					scaleY = stream.readUnsignedWord();
				} else if (j == 67) {
					scaleZ = stream.readUnsignedWord();
				} else if (j == 68) {
                                    mapSceneId = stream.readUnsignedWord();
				} else if (j == 69) {
					defaultOrientation = stream.readUnsignedByte();
				} else if (j == 70) {
                                    offsetX = stream.readSignedWord();
				} else if (j == 71) {
                                    offsetY = stream.readSignedWord();
				} else if (j == 72) {
                                    offsetZ = stream.readSignedWord();
				} else if (j == 73) {
					occludes = true;
				} else if (j == 74) {
					hollow = true;
				} else {
					if (j != 75) {
						continue;
					}
                                    supportsItems = stream.readUnsignedByte();
				}
				continue label0;
			} while (j != 77);
                    varbitId = stream.readUnsignedWord();
                    if (varbitId == 65535) {
                            varbitId = -1;
			}
                    varpId = stream.readUnsignedWord();
                    if (varpId == 65535) {
                            varpId = -1;
			}
			int j1 = stream.readUnsignedByte();
			childrenIDs = new int[j1 + 1];
			for (int j2 = 0; j2 <= j1; j2++) {
				childrenIDs[j2] = stream.readUnsignedWord();
				if (childrenIDs[j2] == 65535) {
					childrenIDs[j2] = -1;
				}
			}

		} while (true);
		if (i == -1) {
			interactive = modelIds != null && (modelTypes == null || modelTypes[0] == 10);
			if (actions != null) {
				interactive = true;
			}
		}
		if (hollow) {
			isSolid = false;
			impenetrable = false;
		}
            if (supportsItems == -1) {
                    supportsItems = isSolid ? 1 : 0;
		}
	}

	private ObjectDef() {
		type = -1;
	}

        public boolean occludes;
        private byte ambient;
        private int offsetX;
	public String name;
	private int scaleZ;
    private static final Model[] tempModelArray = new Model[4];
        private byte contrast;
	public int sizeX;
        private int offsetY;
        public int mapIconId;
	private int[] originalModelColors;
	private int scaleX;
        public int varpId;
        private boolean mirrorOnRotate;
	public static boolean lowMem;
	private static Stream stream;
	public int type;
	private static int[] streamIndices;
	public boolean impenetrable;
        public int mapSceneId;
	public int childrenIDs[];
        private int supportsItems;
	public int sizeY;
        public boolean contouredGround;
        public boolean adjustToTerrain;
	public static Game clientInstance;
        private boolean hollow;
        public boolean isSolid;
        public int defaultOrientation;
        private boolean delayedShading;
	private static int cacheIndex;
	private int scaleY;
	private int[] modelIds;
        public int varbitId;
        public int wallDecoOffset;
	private int[] modelTypes;
	public byte description[];
	public boolean interactive;
        public boolean clipped;
        public static MRUCache mruNodes2 = new MRUCache(30);
	public int animationId;
	private static ObjectDef[] cache;
        private int offsetZ;
	private int[] modifiedModelColors;
        public static MRUCache mruNodes1 = new MRUCache(500);
	public String actions[];

}
