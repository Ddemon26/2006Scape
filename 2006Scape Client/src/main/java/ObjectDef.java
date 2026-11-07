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
		adjustsToTerrain = false;
		keepsVertexNormals = false;
		occludes = false;
		animationId = -1;
		wallDecorationOffset = 16;
		ambientLightOffset = 0;
		lightDivisorSteps = 0;
		actions = null;
		mapFunctionId = -1;
		mapSceneId = -1;
		mirrored = false;
		castsShadow = true;
		modelScaleX = 128;
		modelScaleY = 128;
		modelScaleZ = 128;
		blockedSidesMask = 0;
		modelOffsetX = 0;
		modelOffsetY = 0;
		modelOffsetZ = 0;
		showInLowMemory = false;
		forcesNonSolid = false;
		raisesItemStacks = -1;
		varBitId = -1;
		settingId = -1;
		childrenIDs = null;
	}

	public void requestModels(OnDemandFetcher fetcher) {
		if (modelIds == null) {
			return;
		}
		for (int modelId : modelIds) {
			fetcher.requestExtraFile(modelId & 0xffff, 0);
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
		int recordOffset = 2;
		for (int objectIndex = 0; objectIndex < totalObjects; objectIndex++) {
			streamIndices[objectIndex] = recordOffset;
			recordOffset += stream.readUnsignedShortBigEndian();
		}

		cache = new ObjectDef[20];
		for (int cacheSlot = 0; cacheSlot < 20; cacheSlot++) {
			cache[cacheSlot] = new ObjectDef();
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
				allLoaded &= Model.isModelLoaded(modelId & 0xffff);
			}

			return allLoaded;
		}
		for (int index = 0; index < modelShapes.length; index++) {
			if (modelShapes[index] == shape) {
				return Model.isModelLoaded(modelIds[index] & 0xffff);
			}
		}

		return true;
	}

	public Model getModel(int shape, int orientation, int heightX0Z0, int heightX1Z0, int heightX1Z1, int heightX0Z1, int frameId) {
		Model model = buildModel(shape, frameId, orientation);
		if (model == null) {
			return null;
		}
		if (adjustsToTerrain || keepsVertexNormals) {
			model = new Model(adjustsToTerrain, keepsVertexNormals, model);
		}
		if (adjustsToTerrain) {
			int meanHeight = (heightX0Z0 + heightX1Z0 + heightX1Z1 + heightX0Z1) / 4;
			for (int vertexIndex = 0; vertexIndex < model.vertexCount; vertexIndex++) {
				int vertexX = model.vertexX[vertexIndex];
				int vertexZ = model.vertexZ[vertexIndex];
				int lowZHeight = heightX0Z0 + (heightX1Z0 - heightX0Z0) * (vertexX + 64) / 128;
				int highZHeight = heightX0Z1 + (heightX1Z1 - heightX0Z1) * (vertexX + 64) / 128;
				int vertexHeight = lowZHeight + (highZHeight - lowZHeight) * (vertexZ + 64) / 128;
				model.vertexY[vertexIndex] += vertexHeight - meanHeight;
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
			allLoaded &= Model.isModelLoaded(modelId & 0xffff);
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

	private Model buildModel(int shape, int frameId, int orientation) {
		Model model = null;
		long cacheKey;
		if (modelShapes == null) {
			if (shape != 10) {
				return null;
			}
			cacheKey = (type << 6) + orientation + ((long) (frameId + 1) << 32);
			Model cachedModel = (Model) mruNodes2.insertFromCache(cacheKey);
			if (cachedModel != null) {
				return cachedModel;
			}
			if (modelIds == null) {
				return null;
			}
			boolean mirrorModel = mirrored ^ orientation > 3;
			int modelCount = modelIds.length;
			for (int modelIndex = 0; modelIndex < modelCount; modelIndex++) {
				int modelKey = modelIds[modelIndex];
				if (mirrorModel) {
					modelKey += 0x10000;
				}
				model = (Model) mruNodes1.insertFromCache(modelKey);
				if (model == null) {
					model = Model.getModel(modelKey & 0xffff);
					if (model == null) {
						return null;
					}
					if (mirrorModel) {
						model.reflectZAndReverseWinding();
					}
					mruNodes1.removeFromCache(model, modelKey);
				}
				if (modelCount > 1) {
					combinedModelParts[modelIndex] = model;
				}
			}

			if (modelCount > 1) {
				model = new Model(modelCount, combinedModelParts);
			}
		} else {
			int shapeIndex = -1;
			for (int shapeSearchIndex = 0; shapeSearchIndex < modelShapes.length; shapeSearchIndex++) {
				if (modelShapes[shapeSearchIndex] != shape) {
					continue;
				}
				shapeIndex = shapeSearchIndex;
				break;
			}

			if (shapeIndex == -1) {
				return null;
			}
			cacheKey = (type << 6) + (shapeIndex << 3) + orientation + ((long) (frameId + 1) << 32);
			Model cachedShapeModel = (Model) mruNodes2.insertFromCache(cacheKey);
			if (cachedShapeModel != null) {
				return cachedShapeModel;
			}
			int shapeModelKey = modelIds[shapeIndex];
			boolean mirrorShapeModel = mirrored ^ orientation > 3;
			if (mirrorShapeModel) {
				shapeModelKey += 0x10000;
			}
			model = (Model) mruNodes1.insertFromCache(shapeModelKey);
			if (model == null) {
				model = Model.getModel(shapeModelKey & 0xffff);
				if (model == null) {
					return null;
				}
				if (mirrorShapeModel) {
					model.reflectZAndReverseWinding();
				}
				mruNodes1.removeFromCache(model, shapeModelKey);
			}
		}
		boolean scaled;
		scaled = modelScaleX != 128 || modelScaleY != 128 || modelScaleZ != 128;
		boolean translated;
		translated = modelOffsetX != 0 || modelOffsetY != 0 || modelOffsetZ != 0;
		Model builtModel = new Model(modifiedModelColors == null, AnimationFrame.isMissingFrameId(frameId), orientation == 0 && frameId == -1 && !scaled && !translated, model);
		if (frameId != -1) {
			builtModel.buildAnimationGroups();
			builtModel.applyAnimationFrame(frameId);
			builtModel.faceGroups = null;
			builtModel.vertexGroups = null;
		}
		while (orientation-- > 0) {
			builtModel.rotateYQuarterTurn();
		}
		if (modifiedModelColors != null) {
			for (int colorIndex = 0; colorIndex < modifiedModelColors.length; colorIndex++) {
				builtModel.recolor(modifiedModelColors[colorIndex], originalModelColors[colorIndex]);
			}

		}
		if (scaled) {
			builtModel.scale(modelScaleX, modelScaleZ, modelScaleY);
		}
		if (translated) {
			builtModel.translate(modelOffsetX, modelOffsetY, modelOffsetZ);
		}
		builtModel.applyLighting(64 + ambientLightOffset, 768 + lightDivisorSteps * 5, -50, -10, -50, !keepsVertexNormals);
		if (raisesItemStacks == 1) {
			builtModel.stackSurfaceHeight = builtModel.modelHeight;
		}
		mruNodes2.removeFromCache(builtModel, cacheKey);
		return builtModel;
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
					adjustsToTerrain = true;
				} else if (opcode == 22) {
					keepsVertexNormals = true;
				} else if (opcode == 23) {
					occludes = true;
				} else if (opcode == 24) {
					animationId = stream.readUnsignedShortBigEndian();
					if (animationId == 65535) {
						animationId = -1;
					}
				} else if (opcode == 28) {
					wallDecorationOffset = stream.readUnsignedByte();
				} else if (opcode == 29) {
					ambientLightOffset = stream.readSignedByte();
				} else if (opcode == 39) {
					lightDivisorSteps = stream.readSignedByte();
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
					mapFunctionId = stream.readUnsignedShortBigEndian();
				} else if (opcode == 62) {
					mirrored = true;
				} else if (opcode == 64) {
					castsShadow = false;
				} else if (opcode == 65) {
					modelScaleX = stream.readUnsignedShortBigEndian();
				} else if (opcode == 66) {
					modelScaleY = stream.readUnsignedShortBigEndian();
				} else if (opcode == 67) {
					modelScaleZ = stream.readUnsignedShortBigEndian();
				} else if (opcode == 68) {
					mapSceneId = stream.readUnsignedShortBigEndian();
				} else if (opcode == 69) {
					blockedSidesMask = stream.readUnsignedByte();
				} else if (opcode == 70) {
					modelOffsetX = stream.readSignedShortBigEndian();
				} else if (opcode == 71) {
					modelOffsetY = stream.readSignedShortBigEndian();
				} else if (opcode == 72) {
					modelOffsetZ = stream.readSignedShortBigEndian();
				} else if (opcode == 73) {
					showInLowMemory = true;
				} else if (opcode == 74) {
					forcesNonSolid = true;
				} else {
					if (opcode != 75) {
						continue;
					}
					raisesItemStacks = stream.readUnsignedByte();
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
		if (forcesNonSolid) {
			blocksMovement = false;
			blocksProjectiles = false;
		}
		if (raisesItemStacks == -1) {
			raisesItemStacks = blocksMovement ? 1 : 0;
		}
	}

	private ObjectDef() {
		type = -1;
	}

	public boolean showInLowMemory;
	private byte ambientLightOffset;
	private int modelOffsetX;
	public String name;
	private int modelScaleZ;
	private static final Model[] combinedModelParts = new Model[4];
	private byte lightDivisorSteps;
	public int sizeX;
	private int modelOffsetY;
	public int mapFunctionId;
	private int[] originalModelColors;
	private int modelScaleX;
	public int settingId;
	private boolean mirrored;
	public static boolean lowMem;
	private static Stream stream;
	public int type;
	private static int[] streamIndices;
	public boolean blocksProjectiles;
	public int mapSceneId;
	public int childrenIDs[];
	private int raisesItemStacks;
	public int sizeY;
	public boolean adjustsToTerrain;
	public boolean occludes;
	public static Game clientInstance;
	private boolean forcesNonSolid;
	public boolean blocksMovement;
	public int blockedSidesMask;
	private boolean keepsVertexNormals;
	private static int cacheIndex;
	private int modelScaleY;
	private int[] modelIds;
	public int varBitId;
	public int wallDecorationOffset;
	private int[] modelShapes;
	public byte description[];
	public boolean hasActions;
	public boolean castsShadow;
	public static LruCache mruNodes2 = new LruCache(30);
	public int animationId;
	private static ObjectDef[] cache;
	private int modelOffsetZ;
	private int[] modifiedModelColors;
	public static LruCache mruNodes1 = new LruCache(500);
	public String actions[];

}
