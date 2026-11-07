// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

public final class EntityDef {
	
	public static EntityDef forID(int i) {
		for (int j = 0; j < 20; j++) {
			if (cache[j].type == i) {
				return cache[j];
			}
		}

		anInt56 = (anInt56 + 1) % 20;
		EntityDef entityDef = cache[anInt56] = new EntityDef();
		stream.currentOffset = streamIndices[i];
		entityDef.type = i;
		entityDef.readValues(stream);
		switch(i) {
			case 2258 :
				entityDef.actions = new String[5];
				entityDef.actions[0] = "Talk-to";
				entityDef.actions[2] = "Trade";
				entityDef.actions[3] = "Teleport";
				break;
				
			case 945:
				entityDef.name = ClientSettings.SERVER_NAME + " Guide"; 
				break;
		}
		return entityDef;
	}

	public Model getHeadModel() {
		if (childrenIDs != null) {
			EntityDef entityDef = method161();
			if (entityDef == null) {
				return null;
			} else {
				return entityDef.getHeadModel();
			}
		}
		if (headModelIds == null) {
			return null;
		}
		boolean anyModelMissing = false;
		for (int modelIndex = 0; modelIndex < headModelIds.length; modelIndex++) {
			if (!Model.isModelLoaded(headModelIds[modelIndex])) {
				anyModelMissing = true;
			}
		}

		if (anyModelMissing) {
			return null;
		}
		Model models[] = new Model[headModelIds.length];
		for (int loadIndex = 0; loadIndex < headModelIds.length; loadIndex++) {
			models[loadIndex] = Model.getModel(headModelIds[loadIndex]);
		}

		Model model;
		if (models.length == 1) {
			model = models[0];
		} else {
			model = new Model(models.length, models);
		}
		if (recolorSources != null) {
			for (int recolorIndex = 0; recolorIndex < recolorSources.length; recolorIndex++) {
				model.recolor(recolorSources[recolorIndex], recolorReplacements[recolorIndex]);
			}

		}
		return model;
	}

	public EntityDef method161() {
		int j = -1;
		if (anInt57 != -1) {
			VarBit varBit = VarBit.cache[anInt57];
			int k = varBit.anInt648;
			int l = varBit.anInt649;
			int i1 = varBit.anInt650;
			int j1 = Game.anIntArray1232[i1 - l];
			j = clientInstance.variousSettings[k] >> l & j1;
		} else if (anInt59 != -1) {
			j = clientInstance.variousSettings[anInt59];
		}
		if (j < 0 || j >= childrenIDs.length || childrenIDs[j] == -1) {
			return null;
		} else {
			return forID(childrenIDs[j]);
		}
	}

	public static int totalNPCs;

	public static void unpackConfig(StreamLoader streamLoader) {
		stream = new Stream(streamLoader.getDataForName("npc.dat"));
		Stream stream2 = new Stream(streamLoader.getDataForName("npc.idx"));
		totalNPCs = stream2.readUnsignedShortBigEndian();
		streamIndices = new int[totalNPCs];
		int i = 2;
		for (int j = 0; j < totalNPCs; j++) {
			streamIndices[j] = i;
			i += stream2.readUnsignedShortBigEndian();
		}

		cache = new EntityDef[20];
		for (int k = 0; k < 20; k++) {
			cache[k] = new EntityDef();
		}

	}

	public static void nullLoader() {
		mruNodes = null;
		streamIndices = null;
		cache = null;
		stream = null;
	}

	public Model getAnimatedModel(int secondaryFrameId, int primaryFrameId, int secondaryTransformIndices[]) {
		if (childrenIDs != null) {
			EntityDef entityDef = method161();
			if (entityDef == null) {
				return null;
			} else {
				return entityDef.getAnimatedModel(secondaryFrameId, primaryFrameId, secondaryTransformIndices);
			}
		}
		Model model = (Model) mruNodes.insertFromCache(type);
		if (model == null) {
			boolean anyModelMissing = false;
			for (int modelIndex = 0; modelIndex < bodyModelIds.length; modelIndex++) {
				if (!Model.isModelLoaded(bodyModelIds[modelIndex])) {
					anyModelMissing = true;
				}
			}

			if (anyModelMissing) {
				return null;
			}
			Model models[] = new Model[bodyModelIds.length];
			for (int loadIndex = 0; loadIndex < bodyModelIds.length; loadIndex++) {
				models[loadIndex] = Model.getModel(bodyModelIds[loadIndex]);
			}

			if (models.length == 1) {
				model = models[0];
			} else {
				model = new Model(models.length, models);
			}
			if (recolorSources != null) {
				for (int recolorIndex = 0; recolorIndex < recolorSources.length; recolorIndex++) {
					model.recolor(recolorSources[recolorIndex], recolorReplacements[recolorIndex]);
				}

			}
			model.buildAnimationGroups();
			model.applyLighting(64 + ambientLightOffset, 850 + lightDivisorOffset, -30, -50, -30, true);
			mruNodes.removeFromCache(model, type);
		}
		Model animatedModel = Model.animationScratchModel;
		animatedModel.copyIntoAnimationScratch(model, AnimationFrame.isMissingFrameId(primaryFrameId) & AnimationFrame.isMissingFrameId(secondaryFrameId));
		if (primaryFrameId != -1 && secondaryFrameId != -1) {
			animatedModel.applyInterleavedFrames(secondaryTransformIndices, secondaryFrameId, primaryFrameId);
		} else if (primaryFrameId != -1) {
			animatedModel.applyAnimationFrame(primaryFrameId);
		}
		if (horizontalScale != 128 || verticalScale != 128) {
			animatedModel.scale(horizontalScale, horizontalScale, verticalScale);
		}
		animatedModel.calculateBounds();
		animatedModel.faceGroups = null;
		animatedModel.vertexGroups = null;
		if (aByte68 == 1) {
			animatedModel.aBoolean1659 = true;
		}
		return animatedModel;
	}

	private void readValues(Stream stream) {
		do {
			int i = stream.readUnsignedByte();
			if (i == 0) {
				return;
			}
			if (i == 1) {
				int j = stream.readUnsignedByte();
				bodyModelIds = new int[j];
				for (int j1 = 0; j1 < j; j1++) {
					bodyModelIds[j1] = stream.readUnsignedShortBigEndian();
				}

			} else if (i == 2) {
				name = stream.readLfTerminatedString();
			} else if (i == 3) {
				description = stream.readLfTerminatedBytes();
			} else if (i == 12) {
				aByte68 = stream.readSignedByte();
			} else if (i == 13) {
				anInt77 = stream.readUnsignedShortBigEndian();
			} else if (i == 14) {
				anInt67 = stream.readUnsignedShortBigEndian();
			} else if (i == 17) {
				anInt67 = stream.readUnsignedShortBigEndian();
				anInt58 = stream.readUnsignedShortBigEndian();
				anInt83 = stream.readUnsignedShortBigEndian();
				anInt55 = stream.readUnsignedShortBigEndian();
			} else if (i >= 30 && i < 40) {
				if (actions == null) {
					actions = new String[5];
				}
				actions[i - 30] = stream.readLfTerminatedString();
				if (actions[i - 30].equalsIgnoreCase("hidden")) {
					actions[i - 30] = null;
				}
			} else if (i == 40) {
				int k = stream.readUnsignedByte();
				recolorSources = new int[k];
				recolorReplacements = new int[k];
				for (int k1 = 0; k1 < k; k1++) {
					recolorSources[k1] = stream.readUnsignedShortBigEndian();
					recolorReplacements[k1] = stream.readUnsignedShortBigEndian();
				}

			} else if (i == 60) {
				int l = stream.readUnsignedByte();
				headModelIds = new int[l];
				for (int l1 = 0; l1 < l; l1++) {
					headModelIds[l1] = stream.readUnsignedShortBigEndian();
				}

			} else if (i == 90) {
				stream.readUnsignedShortBigEndian();
			} else if (i == 91) {
				stream.readUnsignedShortBigEndian();
			} else if (i == 92) {
				stream.readUnsignedShortBigEndian();
			} else if (i == 93) {
				aBoolean87 = false;
			} else if (i == 95) {
				combatLevel = stream.readUnsignedShortBigEndian();
			} else if (i == 97) {
				horizontalScale = stream.readUnsignedShortBigEndian();
			} else if (i == 98) {
				verticalScale = stream.readUnsignedShortBigEndian();
			} else if (i == 99) {
				aBoolean93 = true;
			} else if (i == 100) {
				ambientLightOffset = stream.readSignedByte();
			} else if (i == 101) {
				lightDivisorOffset = stream.readSignedByte() * 5;
			} else if (i == 102) {
				anInt75 = stream.readUnsignedShortBigEndian();
			} else if (i == 103) {
				anInt79 = stream.readUnsignedShortBigEndian();
			} else if (i == 106) {
				anInt57 = stream.readUnsignedShortBigEndian();
				if (anInt57 == 65535) {
					anInt57 = -1;
				}
				anInt59 = stream.readUnsignedShortBigEndian();
				if (anInt59 == 65535) {
					anInt59 = -1;
				}
				int i1 = stream.readUnsignedByte();
				childrenIDs = new int[i1 + 1];
				for (int i2 = 0; i2 <= i1; i2++) {
					childrenIDs[i2] = stream.readUnsignedShortBigEndian();
					if (childrenIDs[i2] == 65535) {
						childrenIDs[i2] = -1;
					}
				}

			} else if (i == 107) {
				aBoolean84 = false;
			}
		} while (true);
	}

	private EntityDef() {
		anInt55 = -1;
		anInt57 = -1;
		anInt58 = -1;
		anInt59 = -1;
		combatLevel = -1;
		anInt67 = -1;
		aByte68 = 1;
		anInt75 = -1;
		anInt77 = -1;
		type = -1L;
		anInt79 = 32;
		anInt83 = -1;
		aBoolean84 = true;
		verticalScale = 128;
		aBoolean87 = true;
		horizontalScale = 128;
		aBoolean93 = false;
	}

	public int anInt55;
	private static int anInt56;
	private int anInt57;
	public int anInt58;
	private int anInt59;
	private static Stream stream;
	public int combatLevel;
	public String name;
	public String actions[];
	public int anInt67;
	public byte aByte68;
	private int[] recolorReplacements;
	private static int[] streamIndices;
	private int[] headModelIds;
	public int anInt75;
	private int[] recolorSources;
	public int anInt77;
	public long type;
	public int anInt79;
	private static EntityDef[] cache;
	public static Game clientInstance;
	public int anInt83;
	public boolean aBoolean84;
	private int ambientLightOffset;
	private int verticalScale;
	public boolean aBoolean87;
	public int childrenIDs[];
	public byte description[];
	private int horizontalScale;
	private int lightDivisorOffset;
	public boolean aBoolean93;
	private int[] bodyModelIds;
	public static LruCache mruNodes = new LruCache(30);

}
