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

                cacheIndex = (cacheIndex + 1) % 20;
                EntityDef entityDef = cache[cacheIndex] = new EntityDef();
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

       public Model getModel() {
		if (childrenIDs != null) {
                       EntityDef entityDef = transform();
			if (entityDef == null) {
				return null;
			} else {
                               return entityDef.getModel();
			}
		}
                if (headModelIds == null) {
                        return null;
                }
                boolean flag1 = false;
                for (int i = 0; i < headModelIds.length; i++) {
                        if (!Model.isLoaded(headModelIds[i])) {
                                flag1 = true;
                        }
                }

		if (flag1) {
			return null;
		}
                Model tempModels[] = new Model[headModelIds.length];
                for (int j = 0; j < headModelIds.length; j++) {
                        tempModels[j] = Model.create(headModelIds[j]);
                }

                Model model;
                if (tempModels.length == 1) {
                        model = tempModels[0];
                } else {
                        model = new Model(tempModels.length, tempModels);
                }
                if (originalModelColors != null) {
                        for (int k = 0; k < originalModelColors.length; k++) {
                                model.recolor(originalModelColors[k], modifiedModelColors[k]);
                        }

                }
		return model;
	}

       public EntityDef transform() {
		int j = -1;
                if (transformVarbit != -1) {
                        VarBit varBit = VarBit.cache[transformVarbit];
                        int k = varBit.configId;
                        int l = varBit.leastSignificantBit;
                        int i1 = varBit.mostSignificantBit;
			int j1 = Game.bitMasks[i1 - l];
			j = clientInstance.variousSettings[k] >> l & j1;
                } else if (transformVarp != -1) {
                        j = clientInstance.variousSettings[transformVarp];
		}
		if (j < 0 || j >= childrenIDs.length || childrenIDs[j] == -1) {
			return null;
		} else {
			return forID(childrenIDs[j]);
		}
	}

	public static int totalNPCs;

	public static void unpackConfig(StreamLoader streamLoader) {
		stream = new Stream(streamLoader.getFileData("npc.dat"));
		Stream stream2 = new Stream(streamLoader.getFileData("npc.idx"));
		totalNPCs = stream2.readUnsignedWord();
		streamIndices = new int[totalNPCs];
		int i = 2;
		for (int j = 0; j < totalNPCs; j++) {
			streamIndices[j] = i;
			i += stream2.readUnsignedWord();
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

       public Model getAnimatedModel(int primaryFrame, int secondaryFrame, int[] frameData) {
               if (childrenIDs != null) {
                       EntityDef entityDef = transform();
                       if (entityDef == null) {
                               return null;
                       } else {
                               return entityDef.getAnimatedModel(primaryFrame, secondaryFrame, frameData);
                       }
               }
               Model model = (Model) mruNodes.get(type);
               if (model == null) {
                        boolean flag = false;
                        for (int i1 = 0; i1 < modelIds.length; i1++) {
                                if (!Model.isLoaded(modelIds[i1])) {
                                        flag = true;
                                }
                        }

			if (flag) {
				return null;
			}
                        Model tempModels[] = new Model[modelIds.length];
                        for (int j1 = 0; j1 < modelIds.length; j1++) {
                                tempModels[j1] = Model.create(modelIds[j1]);
                        }

                        if (tempModels.length == 1) {
                                model = tempModels[0];
                        } else {
                                model = new Model(tempModels.length, tempModels);
                        }
                        if (originalModelColors != null) {
                                for (int k1 = 0; k1 < originalModelColors.length; k1++) {
                                        model.recolor(originalModelColors[k1], modifiedModelColors[k1]);
                                }

			}
			model.buildVertexGroups();
                        model.applyLighting(64 + ambient, 850 + contrast, -30, -50, -30, true);
                        mruNodes.put(model, type);
		}
               Model model_1 = Model.placeholderModel;
               model_1.copyFromModel(model, AnimFrame.isNullFrame(secondaryFrame) & AnimFrame.isNullFrame(primaryFrame));
               if (secondaryFrame != -1 && primaryFrame != -1) {
                       model_1.applyFrames(frameData, primaryFrame, secondaryFrame);
               } else if (secondaryFrame != -1) {
                       model_1.applyFrame(secondaryFrame);
               }
               if (modelScaleXy != 128 || modelScaleZ != 128) {
                       model_1.scaleModel(modelScaleXy, modelScaleXy, modelScaleZ);
               }
		model_1.calculateBounds();
		model_1.faceGroups = null;
		model_1.vertexGroups = null;
                if (size == 1) {
                        model_1.pickable = true;
                }
		return model_1;
	}

	private void readValues(Stream stream) {
		do {
			int i = stream.readUnsignedByte();
			if (i == 0) {
				return;
			}
			if (i == 1) {
				int j = stream.readUnsignedByte();
                                modelIds = new int[j];
                                for (int j1 = 0; j1 < j; j1++) {
                                        modelIds[j1] = stream.readUnsignedWord();
                                }

			} else if (i == 2) {
				name = stream.readString();
			} else if (i == 3) {
				description = stream.readBytes();
			} else if (i == 12) {
                                size = stream.readSignedByte();
			} else if (i == 13) {
                                standAnimation = stream.readUnsignedWord();
			} else if (i == 14) {
                                walkAnimation = stream.readUnsignedWord();
			} else if (i == 17) {
                                walkAnimation = stream.readUnsignedWord();
                                turn180Animation = stream.readUnsignedWord();
                                turn90CWAnimation = stream.readUnsignedWord();
                                turn90CCWAnimation = stream.readUnsignedWord();
			} else if (i >= 30 && i < 40) {
				if (actions == null) {
					actions = new String[5];
				}
				actions[i - 30] = stream.readString();
				if (actions[i - 30].equalsIgnoreCase("hidden")) {
					actions[i - 30] = null;
				}
			} else if (i == 40) {
				int k = stream.readUnsignedByte();
                                originalModelColors = new int[k];
                                modifiedModelColors = new int[k];
                                for (int k1 = 0; k1 < k; k1++) {
                                        originalModelColors[k1] = stream.readUnsignedWord();
                                        modifiedModelColors[k1] = stream.readUnsignedWord();
                                }

			} else if (i == 60) {
				int l = stream.readUnsignedByte();
                                headModelIds = new int[l];
                                for (int l1 = 0; l1 < l; l1++) {
                                        headModelIds[l1] = stream.readUnsignedWord();
                                }

			} else if (i == 90) {
				stream.readUnsignedWord();
			} else if (i == 91) {
				stream.readUnsignedWord();
			} else if (i == 92) {
				stream.readUnsignedWord();
			} else if (i == 93) {
                                minimapVisible = false;
			} else if (i == 95) {
				combatLevel = stream.readUnsignedWord();
                       } else if (i == 97) {
                               modelScaleXy = stream.readUnsignedWord();
                       } else if (i == 98) {
                               modelScaleZ = stream.readUnsignedWord();
			} else if (i == 99) {
                                priorityRender = true;
			} else if (i == 100) {
                                ambient = stream.readSignedByte();
			} else if (i == 101) {
                                contrast = stream.readSignedByte() * 5;
			} else if (i == 102) {
                                headIcon = stream.readUnsignedWord();
			} else if (i == 103) {
                                turnSpeed = stream.readUnsignedWord();
			} else if (i == 106) {
                                transformVarbit = stream.readUnsignedWord();
                                if (transformVarbit == 65535) {
                                        transformVarbit = -1;
                                }
                                transformVarp = stream.readUnsignedWord();
                                if (transformVarp == 65535) {
                                        transformVarp = -1;
				}
				int i1 = stream.readUnsignedByte();
				childrenIDs = new int[i1 + 1];
				for (int i2 = 0; i2 <= i1; i2++) {
					childrenIDs[i2] = stream.readUnsignedWord();
					if (childrenIDs[i2] == 65535) {
						childrenIDs[i2] = -1;
					}
				}

			} else if (i == 107) {
                                clickable = false;
			}
		} while (true);
	}

	private EntityDef() {
                turn90CCWAnimation = -1;
                transformVarbit = -1;
                turn180Animation = -1;
                transformVarp = -1;
                combatLevel = -1;
                walkAnimation = -1;
                size = 1;
                headIcon = -1;
                standAnimation = -1;
                type = -1L;
                turnSpeed = 32;
                turn90CWAnimation = -1;
                clickable = true;
               modelScaleZ = 128;
                minimapVisible = true;
               modelScaleXy = 128;
                priorityRender = false;
        }

        public int turn90CCWAnimation;
        private static int cacheIndex;
        private int transformVarbit;
        public int turn180Animation;
        private int transformVarp;
	private static Stream stream;
	public int combatLevel;
	public String name;
	public String actions[];
        public int walkAnimation;
        public byte size;
        private int[] modifiedModelColors;
	private static int[] streamIndices;
        private int[] headModelIds;
        public int headIcon;
        private int[] originalModelColors;
        public int standAnimation;
	public long type;
        public int turnSpeed;
	private static EntityDef[] cache;
	public static Game clientInstance;
        public int turn90CWAnimation;
        public boolean clickable;
        private int ambient;
       private int modelScaleZ;
        public boolean minimapVisible;
        public int childrenIDs[];
	public byte description[];
       private int modelScaleXy;
        private int contrast;
        public boolean priorityRender;
        private int[] modelIds;
    public static MRUCache mruNodes = new MRUCache(30);

}
