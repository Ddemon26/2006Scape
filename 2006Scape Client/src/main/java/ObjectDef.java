// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

public final class ObjectDef {

	public static ObjectDef forID(int objectId) {
		for (int cacheSlot = 0; cacheSlot < 20; cacheSlot++) {
			if (cache[cacheSlot].type == objectId) {
				return cache[cacheSlot];
			}
		}

		cacheIndex = (cacheIndex + 1) % 20;
		ObjectDef definition = cache[cacheIndex];
		stream.currentOffset = streamIndices[objectId];
		definition.type = objectId;
		definition.setDefaults();
		definition.readValues(stream);
		if (objectId == 6) {
			definition.actions[1] = "Load";
			definition.actions[2] = "Pick-up";
		}
		switch (objectId) {

		}
		return definition;
	}

	private void setDefaults() {
		modelIds = null;
		modelShapes = null;
		name = null;
		description = null;
		modifiedModelColors = null;
		originalModelColors = null;
		sizeX = 1;
		sizeY = 1;
		blocksMovement = true;
		blocksProjectiles = true;
		hasActions = false;
		aBoolean762 = false;
		aBoolean769 = false;
		aBoolean764 = false;
		animationId = -1;
		wallDecorationOffset = 16;
		aByte737 = 0;
		aByte742 = 0;
		actions = null;
		anInt746 = -1;
		anInt758 = -1;
		aBoolean751 = false;
		aBoolean779 = true;
		anInt748 = 128;
		anInt772 = 128;
		anInt740 = 128;
		blockedSidesMask = 0;
		anInt738 = 0;
		anInt745 = 0;
		anInt783 = 0;
		showInLowMemory = false;
		aBoolean766 = false;
		anInt760 = -1;
		varBitId = -1;
		settingId = -1;
		childrenIDs = null;
	}

	public void requestModels(OnDemandFetcher fetcher) {
		if (modelIds == null) {
			return;
		}
		for (int modelId : modelIds) {
			fetcher.method560(modelId & 0xffff, 0);
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
		stream = new Stream(streamLoader.getDataForName("loc.dat"));
		Stream stream = new Stream(streamLoader.getDataForName("loc.idx"));
		totalObjects = stream.readUnsignedShortBigEndian();
		streamIndices = new int[totalObjects];
		int i = 2;
		for (int j = 0; j < totalObjects; j++) {
			streamIndices[j] = i;
			i += stream.readUnsignedShortBigEndian();
		}

		cache = new ObjectDef[20];
		for (int k = 0; k < 20; k++) {
			cache[k] = new ObjectDef();
		}

	}

	public boolean areModelsLoaded(int shape) {
		if (modelShapes == null) {
			if (modelIds == null) {
				return true;
			}
			if (shape != 10) {
				return true;
			}
			boolean allLoaded = true;
			for (int modelId : modelIds) {
				allLoaded &= Model.method463(modelId & 0xffff);
			}

			return allLoaded;
		}
		for (int index = 0; index < modelShapes.length; index++) {
			if (modelShapes[index] == shape) {
				return Model.method463(modelIds[index] & 0xffff);
			}
		}

		return true;
	}

	public Model method578(int i, int j, int k, int l, int i1, int j1, int k1) {
		Model model = method581(i, k1, j);
		if (model == null) {
			return null;
		}
		if (aBoolean762 || aBoolean769) {
			model = new Model(aBoolean762, aBoolean769, model);
		}
		if (aBoolean762) {
			int l1 = (k + l + i1 + j1) / 4;
			for (int i2 = 0; i2 < model.anInt1626; i2++) {
				int j2 = model.anIntArray1627[i2];
				int k2 = model.anIntArray1629[i2];
				int l2 = k + (l - k) * (j2 + 64) / 128;
				int i3 = j1 + (i1 - j1) * (j2 + 64) / 128;
				int j3 = l2 + (i3 - l2) * (k2 + 64) / 128;
				model.anIntArray1628[i2] += j3 - l1;
			}

			model.recalculateYBounds();
		}
		return model;
	}

	public boolean areAllModelsLoaded() {
		if (modelIds == null) {
			return true;
		}
		boolean allLoaded = true;
		for (int modelId : modelIds) {
			allLoaded &= Model.method463(modelId & 0xffff);
		}
		return allLoaded;
	}

	public ObjectDef resolveMorph() {
		int childIndex = -1;
		if (varBitId != -1) {
			VarBit varBit = VarBit.cache[varBitId];
			int settingIndex = varBit.anInt648;
			int lowBit = varBit.anInt649;
			int highBit = varBit.anInt650;
			int valueMask = Game.anIntArray1232[highBit - lowBit];
			childIndex = clientInstance.variousSettings[settingIndex] >> lowBit & valueMask;
		} else if (settingId != -1) {
			childIndex = clientInstance.variousSettings[settingId];
		}
		if (childIndex < 0 || childIndex >= childrenIDs.length || childrenIDs[childIndex] == -1) {
			return null;
		} else {
			return forID(childrenIDs[childIndex]);
		}
	}

	private Model method581(int j, int k, int l) {
		Model model = null;
		long l1;
		if (modelShapes == null) {
			if (j != 10) {
				return null;
			}
			l1 = (type << 6) + l + ((long) (k + 1) << 32);
			Model model_1 = (Model) mruNodes2.insertFromCache(l1);
			if (model_1 != null) {
				return model_1;
			}
			if (modelIds == null) {
				return null;
			}
			boolean flag1 = aBoolean751 ^ l > 3;
			int k1 = modelIds.length;
			for (int i2 = 0; i2 < k1; i2++) {
				int l2 = modelIds[i2];
				if (flag1) {
					l2 += 0x10000;
				}
				model = (Model) mruNodes1.insertFromCache(l2);
				if (model == null) {
					model = Model.method462(l2 & 0xffff);
					if (model == null) {
						return null;
					}
					if (flag1) {
						model.reflectZAndReverseWinding();
					}
					mruNodes1.removeFromCache(model, l2);
				}
				if (k1 > 1) {
					aModelArray741s[i2] = model;
				}
			}

			if (k1 > 1) {
				model = new Model(k1, aModelArray741s);
			}
		} else {
			int i1 = -1;
			for (int j1 = 0; j1 < modelShapes.length; j1++) {
				if (modelShapes[j1] != j) {
					continue;
				}
				i1 = j1;
				break;
			}

			if (i1 == -1) {
				return null;
			}
			l1 = (type << 6) + (i1 << 3) + l + ((long) (k + 1) << 32);
			Model model_2 = (Model) mruNodes2.insertFromCache(l1);
			if (model_2 != null) {
				return model_2;
			}
			int j2 = modelIds[i1];
			boolean flag3 = aBoolean751 ^ l > 3;
			if (flag3) {
				j2 += 0x10000;
			}
			model = (Model) mruNodes1.insertFromCache(j2);
			if (model == null) {
				model = Model.method462(j2 & 0xffff);
				if (model == null) {
					return null;
				}
				if (flag3) {
					model.reflectZAndReverseWinding();
				}
				mruNodes1.removeFromCache(model, j2);
			}
		}
		boolean flag;
		flag = anInt748 != 128 || anInt772 != 128 || anInt740 != 128;
		boolean flag2;
		flag2 = anInt738 != 0 || anInt745 != 0 || anInt783 != 0;
		Model model_3 = new Model(modifiedModelColors == null, AnimationFrame.isMissingFrameId(k), l == 0 && k == -1 && !flag && !flag2, model);
		if (k != -1) {
			model_3.buildAnimationGroups();
			model_3.applyAnimationFrame(k);
			model_3.faceGroups = null;
			model_3.vertexGroups = null;
		}
		while (l-- > 0) {
			model_3.rotateYQuarterTurn();
		}
		if (modifiedModelColors != null) {
			for (int k2 = 0; k2 < modifiedModelColors.length; k2++) {
				model_3.recolor(modifiedModelColors[k2], originalModelColors[k2]);
			}

		}
		if (flag) {
			model_3.scale(anInt748, anInt740, anInt772);
		}
		if (flag2) {
			model_3.translate(anInt738, anInt745, anInt783);
		}
		model_3.method479(64 + aByte737, 768 + aByte742 * 5, -50, -10, -50, !aBoolean769);
		if (anInt760 == 1) {
			model_3.anInt1654 = model_3.modelHeight;
		}
		mruNodes2.removeFromCache(model_3, l1);
		return model_3;
	}

	private void readValues(Stream stream) {
		int interactionByte = -1;
		label0 : do {
			int opcode;
			do {
				opcode = stream.readUnsignedByte();
				if (opcode == 0) {
					break label0;
				}
				if (opcode == 1) {
					int shapedModelCount = stream.readUnsignedByte();
					if (shapedModelCount > 0) {
						if (modelIds == null || lowMem) {
							modelShapes = new int[shapedModelCount];
							modelIds = new int[shapedModelCount];
							for (int modelIndex = 0; modelIndex < shapedModelCount; modelIndex++) {
								modelIds[modelIndex] = stream.readUnsignedShortBigEndian();
								modelShapes[modelIndex] = stream.readUnsignedByte();
							}

						} else {
							stream.currentOffset += shapedModelCount * 3;
						}
					}
				} else if (opcode == 2) {
					name = stream.readLfTerminatedString();
				} else if (opcode == 3) {
					description = stream.readLfTerminatedBytes();
				} else if (opcode == 5) {
					int modelCount = stream.readUnsignedByte();
					if (modelCount > 0) {
						if (modelIds == null || lowMem) {
							modelShapes = null;
							modelIds = new int[modelCount];
							for (int modelIndex = 0; modelIndex < modelCount; modelIndex++) {
								modelIds[modelIndex] = stream.readUnsignedShortBigEndian();
							}

						} else {
							stream.currentOffset += modelCount * 2;
						}
					}
				} else if (opcode == 14) {
					sizeX = stream.readUnsignedByte();
				} else if (opcode == 15) {
					sizeY = stream.readUnsignedByte();
				} else if (opcode == 17) {
					blocksMovement = false;
				} else if (opcode == 18) {
					blocksProjectiles = false;
				} else if (opcode == 19) {
					interactionByte = stream.readUnsignedByte();
					if (interactionByte == 1) {
						hasActions = true;
					}
				} else if (opcode == 21) {
					aBoolean762 = true;
				} else if (opcode == 22) {
					aBoolean769 = true;
				} else if (opcode == 23) {
					aBoolean764 = true;
				} else if (opcode == 24) {
					animationId = stream.readUnsignedShortBigEndian();
					if (animationId == 65535) {
						animationId = -1;
					}
				} else if (opcode == 28) {
					wallDecorationOffset = stream.readUnsignedByte();
				} else if (opcode == 29) {
					aByte737 = stream.readSignedByte();
				} else if (opcode == 39) {
					aByte742 = stream.readSignedByte();
				} else if (opcode >= 30 && opcode < 39) {
					if (actions == null) {
						actions = new String[5];
					}
					actions[opcode - 30] = stream.readLfTerminatedString();
					if (actions[opcode - 30].equalsIgnoreCase("hidden")) {
						actions[opcode - 30] = null;
					}
				} else if (opcode == 40) {
					int recolorCount = stream.readUnsignedByte();
					modifiedModelColors = new int[recolorCount];
					originalModelColors = new int[recolorCount];
					for (int colorIndex = 0; colorIndex < recolorCount; colorIndex++) {
						modifiedModelColors[colorIndex] = stream.readUnsignedShortBigEndian();
						originalModelColors[colorIndex] = stream.readUnsignedShortBigEndian();
					}

				} else if (opcode == 60) {
					anInt746 = stream.readUnsignedShortBigEndian();
				} else if (opcode == 62) {
					aBoolean751 = true;
				} else if (opcode == 64) {
					aBoolean779 = false;
				} else if (opcode == 65) {
					anInt748 = stream.readUnsignedShortBigEndian();
				} else if (opcode == 66) {
					anInt772 = stream.readUnsignedShortBigEndian();
				} else if (opcode == 67) {
					anInt740 = stream.readUnsignedShortBigEndian();
				} else if (opcode == 68) {
					anInt758 = stream.readUnsignedShortBigEndian();
				} else if (opcode == 69) {
					blockedSidesMask = stream.readUnsignedByte();
				} else if (opcode == 70) {
					anInt738 = stream.readSignedShortBigEndian();
				} else if (opcode == 71) {
					anInt745 = stream.readSignedShortBigEndian();
				} else if (opcode == 72) {
					anInt783 = stream.readSignedShortBigEndian();
				} else if (opcode == 73) {
					showInLowMemory = true;
				} else if (opcode == 74) {
					aBoolean766 = true;
				} else {
					if (opcode != 75) {
						continue;
					}
					anInt760 = stream.readUnsignedByte();
				}
				continue label0;
			} while (opcode != 77);
			varBitId = stream.readUnsignedShortBigEndian();
			if (varBitId == 65535) {
				varBitId = -1;
			}
			settingId = stream.readUnsignedShortBigEndian();
			if (settingId == 65535) {
				settingId = -1;
			}
			int lastChildIndex = stream.readUnsignedByte();
			childrenIDs = new int[lastChildIndex + 1];
			for (int childIndex = 0; childIndex <= lastChildIndex; childIndex++) {
				childrenIDs[childIndex] = stream.readUnsignedShortBigEndian();
				if (childrenIDs[childIndex] == 65535) {
					childrenIDs[childIndex] = -1;
				}
			}

		} while (true);
		if (interactionByte == -1) {
			hasActions = modelIds != null && (modelShapes == null || modelShapes[0] == 10);
			if (actions != null) {
				hasActions = true;
			}
		}
		if (aBoolean766) {
			blocksMovement = false;
			blocksProjectiles = false;
		}
		if (anInt760 == -1) {
			anInt760 = blocksMovement ? 1 : 0;
		}
	}

	private ObjectDef() {
		type = -1;
	}

	public boolean showInLowMemory;
	private byte aByte737;
	private int anInt738;
	public String name;
	private int anInt740;
	private static final Model[] aModelArray741s = new Model[4];
	private byte aByte742;
	public int sizeX;
	private int anInt745;
	public int anInt746;
	private int[] originalModelColors;
	private int anInt748;
	public int settingId;
	private boolean aBoolean751;
	public static boolean lowMem;
	private static Stream stream;
	public int type;
	private static int[] streamIndices;
	public boolean blocksProjectiles;
	public int anInt758;
	public int childrenIDs[];
	private int anInt760;
	public int sizeY;
	public boolean aBoolean762;
	public boolean aBoolean764;
	public static Game clientInstance;
	private boolean aBoolean766;
	public boolean blocksMovement;
	public int blockedSidesMask;
	private boolean aBoolean769;
	private static int cacheIndex;
	private int anInt772;
	private int[] modelIds;
	public int varBitId;
	public int wallDecorationOffset;
	private int[] modelShapes;
	public byte description[];
	public boolean hasActions;
	public boolean aBoolean779;
	public static LruCache mruNodes2 = new LruCache(30);
	public int animationId;
	private static ObjectDef[] cache;
	private int anInt783;
	private int[] modifiedModelColors;
	public static LruCache mruNodes1 = new LruCache(500);
	public String actions[];

}
