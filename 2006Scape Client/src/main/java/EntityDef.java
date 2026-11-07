// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

public final class EntityDef {
	
	public static EntityDef forID(int npcId) {
		for (int cacheSlot = 0; cacheSlot < 20; cacheSlot++) {
			if (cache[cacheSlot].type == npcId) {
				return cache[cacheSlot];
			}
		}

		cacheIndex = (cacheIndex + 1) % 20;
		EntityDef entityDef = cache[cacheIndex] = new EntityDef();
		stream.currentOffset = streamIndices[npcId];
		entityDef.type = npcId;
		entityDef.readValues(stream);
		switch(npcId) {
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
		int childIndex = -1;
		if (morphVarBitId != -1) {
			VarBit varBit = VarBit.cache[morphVarBitId];
			int settingIndex = varBit.anInt648;
			int lowBit = varBit.anInt649;
			int highBit = varBit.anInt650;
			int valueMask = Game.anIntArray1232[highBit - lowBit];
			childIndex = clientInstance.variousSettings[settingIndex] >> lowBit & valueMask;
		} else if (morphSettingId != -1) {
			childIndex = clientInstance.variousSettings[morphSettingId];
		}
		if (childIndex < 0 || childIndex >= childrenIDs.length || childrenIDs[childIndex] == -1) {
			return null;
		} else {
			return forID(childrenIDs[childIndex]);
		}
	}

	public static int totalNPCs;

	public static void unpackConfig(StreamLoader streamLoader) {
		stream = new Stream(streamLoader.getDataForName("npc.dat"));
		Stream stream2 = new Stream(streamLoader.getDataForName("npc.idx"));
		totalNPCs = stream2.readUnsignedShortBigEndian();
		streamIndices = new int[totalNPCs];
		int recordOffset = 2;
		for (int npcIndex = 0; npcIndex < totalNPCs; npcIndex++) {
			streamIndices[npcIndex] = recordOffset;
			recordOffset += stream2.readUnsignedShortBigEndian();
		}

		cache = new EntityDef[20];
		for (int cacheSlot = 0; cacheSlot < 20; cacheSlot++) {
			cache[cacheSlot] = new EntityDef();
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
			animatedModel.pickByBounds = true;
		}
		return animatedModel;
	}

	private void readValues(Stream stream) {
		do {
			int opcode = stream.readUnsignedByte();
			if (opcode == 0) {
				return;
			}
			if (opcode == 1) {
				int bodyModelCount = stream.readUnsignedByte();
				bodyModelIds = new int[bodyModelCount];
				for (int bodyModelIndex = 0; bodyModelIndex < bodyModelCount; bodyModelIndex++) {
					bodyModelIds[bodyModelIndex] = stream.readUnsignedShortBigEndian();
				}

			} else if (opcode == 2) {
				name = stream.readLfTerminatedString();
			} else if (opcode == 3) {
				description = stream.readLfTerminatedBytes();
			} else if (opcode == 12) {
				aByte68 = stream.readSignedByte();
			} else if (opcode == 13) {
				anInt77 = stream.readUnsignedShortBigEndian();
			} else if (opcode == 14) {
				anInt67 = stream.readUnsignedShortBigEndian();
			} else if (opcode == 17) {
				anInt67 = stream.readUnsignedShortBigEndian();
				anInt58 = stream.readUnsignedShortBigEndian();
				anInt83 = stream.readUnsignedShortBigEndian();
				anInt55 = stream.readUnsignedShortBigEndian();
			} else if (opcode >= 30 && opcode < 40) {
				if (actions == null) {
					actions = new String[5];
				}
				actions[opcode - 30] = stream.readLfTerminatedString();
				if (actions[opcode - 30].equalsIgnoreCase("hidden")) {
					actions[opcode - 30] = null;
				}
			} else if (opcode == 40) {
				int recolorCount = stream.readUnsignedByte();
				recolorSources = new int[recolorCount];
				recolorReplacements = new int[recolorCount];
				for (int recolorIndex = 0; recolorIndex < recolorCount; recolorIndex++) {
					recolorSources[recolorIndex] = stream.readUnsignedShortBigEndian();
					recolorReplacements[recolorIndex] = stream.readUnsignedShortBigEndian();
				}

			} else if (opcode == 60) {
				int headModelCount = stream.readUnsignedByte();
				headModelIds = new int[headModelCount];
				for (int headModelIndex = 0; headModelIndex < headModelCount; headModelIndex++) {
					headModelIds[headModelIndex] = stream.readUnsignedShortBigEndian();
				}

			} else if (opcode == 90) {
				stream.readUnsignedShortBigEndian();
			} else if (opcode == 91) {
				stream.readUnsignedShortBigEndian();
			} else if (opcode == 92) {
				stream.readUnsignedShortBigEndian();
			} else if (opcode == 93) {
				aBoolean87 = false;
			} else if (opcode == 95) {
				combatLevel = stream.readUnsignedShortBigEndian();
			} else if (opcode == 97) {
				horizontalScale = stream.readUnsignedShortBigEndian();
			} else if (opcode == 98) {
				verticalScale = stream.readUnsignedShortBigEndian();
			} else if (opcode == 99) {
				aBoolean93 = true;
			} else if (opcode == 100) {
				ambientLightOffset = stream.readSignedByte();
			} else if (opcode == 101) {
				lightDivisorOffset = stream.readSignedByte() * 5;
			} else if (opcode == 102) {
				anInt75 = stream.readUnsignedShortBigEndian();
			} else if (opcode == 103) {
				anInt79 = stream.readUnsignedShortBigEndian();
			} else if (opcode == 106) {
				morphVarBitId = stream.readUnsignedShortBigEndian();
				if (morphVarBitId == 65535) {
					morphVarBitId = -1;
				}
				morphSettingId = stream.readUnsignedShortBigEndian();
				if (morphSettingId == 65535) {
					morphSettingId = -1;
				}
				int lastChildIndex = stream.readUnsignedByte();
				childrenIDs = new int[lastChildIndex + 1];
				for (int childIndex = 0; childIndex <= lastChildIndex; childIndex++) {
					childrenIDs[childIndex] = stream.readUnsignedShortBigEndian();
					if (childrenIDs[childIndex] == 65535) {
						childrenIDs[childIndex] = -1;
					}
				}

			} else if (opcode == 107) {
				aBoolean84 = false;
			}
		} while (true);
	}

	private EntityDef() {
		anInt55 = -1;
		morphVarBitId = -1;
		anInt58 = -1;
		morphSettingId = -1;
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
	private static int cacheIndex;
	private int morphVarBitId;
	public int anInt58;
	private int morphSettingId;
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
