public final class RSInterface {

	public void swapInventoryItems(int i, int j) {
		int k = inv[i];
		inv[i] = inv[j];
		inv[j] = k;
		k = invStackSizes[i];
		invStackSizes[i] = invStackSizes[j];
		invStackSizes[j] = k;
	}
	
	public static void unpack(StreamLoader streamLoader, TextDrawingArea textDrawingAreas[], StreamLoader streamLoader_1) {
              spriteCache = new MRUCache(50000);
		Stream stream = new Stream(streamLoader.getFileData("data"));
		int i = -1;
		int j = stream.readUnsignedWord();
		interfaceCache = new RSInterface[j];
		while (stream.currentOffset < stream.buffer.length) {
			int k = stream.readUnsignedWord();
			if (k == 65535) {
				i = stream.readUnsignedWord();
				k = stream.readUnsignedWord();
			}
			RSInterface rsInterface = interfaceCache[k] = new RSInterface();
			rsInterface.id = k;
			rsInterface.parentID = i;
			rsInterface.type = stream.readUnsignedByte();
			rsInterface.atActionType = stream.readUnsignedByte();
			rsInterface.anInt214 = stream.readUnsignedWord();
			rsInterface.width = stream.readUnsignedWord();
			rsInterface.height = stream.readUnsignedWord();
			rsInterface.aByte254 = (byte) stream.readUnsignedByte();
			rsInterface.anInt230 = stream.readUnsignedByte();
			if (rsInterface.anInt230 != 0) {
				rsInterface.anInt230 = (rsInterface.anInt230 - 1 << 8) + stream.readUnsignedByte();
			} else {
				rsInterface.anInt230 = -1;
			}
			int i1 = stream.readUnsignedByte();
			if (i1 > 0) {
				rsInterface.anIntArray245 = new int[i1];
				rsInterface.anIntArray212 = new int[i1];
				for (int j1 = 0; j1 < i1; j1++) {
					rsInterface.anIntArray245[j1] = stream.readUnsignedByte();
					rsInterface.anIntArray212[j1] = stream.readUnsignedWord();
				}

			}
			int k1 = stream.readUnsignedByte();
			if (k1 > 0) {
				rsInterface.valueIndexArray = new int[k1][];
				for (int l1 = 0; l1 < k1; l1++) {
					int i3 = stream.readUnsignedWord();
					rsInterface.valueIndexArray[l1] = new int[i3];
					for (int l4 = 0; l4 < i3; l4++) {
						rsInterface.valueIndexArray[l1][l4] = stream.readUnsignedWord();
					}

				}

			}
			if (rsInterface.type == 0) {
				rsInterface.scrollMax = stream.readUnsignedWord();
				rsInterface.aBoolean266 = stream.readUnsignedByte() == 1;
				int i2 = stream.readUnsignedWord();
				rsInterface.children = new int[i2];
				rsInterface.childX = new int[i2];
				rsInterface.childY = new int[i2];
				for (int j3 = 0; j3 < i2; j3++) {
					rsInterface.children[j3] = stream.readUnsignedWord();
					rsInterface.childX[j3] = stream.readSignedWord();
					rsInterface.childY[j3] = stream.readSignedWord();
				}

			}
			if (rsInterface.type == 1) {
				stream.readUnsignedWord();
				stream.readUnsignedByte();
			}
			if (rsInterface.type == 2) {
				rsInterface.inv = new int[rsInterface.width * rsInterface.height];
				rsInterface.invStackSizes = new int[rsInterface.width * rsInterface.height];
				rsInterface.aBoolean259 = stream.readUnsignedByte() == 1;
				rsInterface.isInventoryInterface = stream.readUnsignedByte() == 1;
				rsInterface.usableItemInterface = stream.readUnsignedByte() == 1;
				rsInterface.aBoolean235 = stream.readUnsignedByte() == 1;
				rsInterface.invSpritePadX = stream.readUnsignedByte();
				rsInterface.invSpritePadY = stream.readUnsignedByte();
				rsInterface.spritesX = new int[20];
				rsInterface.spritesY = new int[20];
				rsInterface.sprites = new Sprite[20];
				for (int j2 = 0; j2 < 20; j2++) {
					int k3 = stream.readUnsignedByte();
					if (k3 == 1) {
						rsInterface.spritesX[j2] = stream.readSignedWord();
						rsInterface.spritesY[j2] = stream.readSignedWord();
						String s1 = stream.readString();
						if (streamLoader_1 != null && s1.length() > 0) {
							int i5 = s1.lastIndexOf(",");
                                    rsInterface.sprites[j2] = loadSprite(Integer.parseInt(s1.substring(i5 + 1)), streamLoader_1, s1.substring(0, i5));
						}
					}
				}

				rsInterface.actions = new String[5];
				for (int l3 = 0; l3 < 5; l3++) {
					rsInterface.actions[l3] = stream.readString();
					if (rsInterface.actions[l3].length() == 0) {
						rsInterface.actions[l3] = null;
					}
				}
				if(rsInterface.parentID == 3822) {
					rsInterface.actions[2] = "Sell 10";
					rsInterface.actions[3] = "Sell X";
				}
				if(rsInterface.id == 3900) {
					rsInterface.actions[2] = "Buy 10";
					rsInterface.actions[3] = "Buy X";
				}
			}
			if (rsInterface.type == 3) {
				rsInterface.aBoolean227 = stream.readUnsignedByte() == 1;
			}
			if (rsInterface.type == 4 || rsInterface.type == 1) {
				rsInterface.aBoolean223 = stream.readUnsignedByte() == 1;
				int k2 = stream.readUnsignedByte();
				if (textDrawingAreas != null) {
					rsInterface.textDrawingAreas = textDrawingAreas[k2];
				}
				rsInterface.aBoolean268 = stream.readUnsignedByte() == 1;
			}
			if (rsInterface.type == 4) {
				rsInterface.disabledText = stream.readString().replaceAll("RuneScape", ClientSettings.SERVER_NAME);
				rsInterface.enabledText = stream.readString();
			}
			if (rsInterface.type == 1 || rsInterface.type == 3 || rsInterface.type == 4) {
				rsInterface.textColor = stream.readDWord();
			}
			if (rsInterface.type == 3 || rsInterface.type == 4) {
				rsInterface.anInt219 = stream.readDWord();
				rsInterface.anInt216 = stream.readDWord();
				rsInterface.anInt239 = stream.readDWord();
			}
			if (rsInterface.type == 5) {
				String s = stream.readString();
				if (streamLoader_1 != null && s.length() > 0) {
					int i4 = s.lastIndexOf(",");
                                    rsInterface.sprite1 = loadSprite(Integer.parseInt(s.substring(i4 + 1)), streamLoader_1, s.substring(0, i4));
				}
				s = stream.readString();
				if (streamLoader_1 != null && s.length() > 0) {
					int j4 = s.lastIndexOf(",");
                                    rsInterface.sprite2 = loadSprite(Integer.parseInt(s.substring(j4 + 1)), streamLoader_1, s.substring(0, j4));
				}
			}
			if (rsInterface.type == 6) {
				int l = stream.readUnsignedByte();
				if (l != 0) {
                                        rsInterface.mediaType = 1;
                                        rsInterface.mediaId = (l - 1 << 8) + stream.readUnsignedByte();
				}
                                l = stream.readUnsignedByte();
                                if (l != 0) {
                                        rsInterface.enabledMediaType = 1;
                                        rsInterface.enabledMediaId = (l - 1 << 8) + stream.readUnsignedByte();
                                }
                                l = stream.readUnsignedByte();
                                if (l != 0) {
                                        rsInterface.disabledAnimation = (l - 1 << 8) + stream.readUnsignedByte();
                                } else {
                                        rsInterface.disabledAnimation = -1;
                                }
                                l = stream.readUnsignedByte();
                                if (l != 0) {
                                        rsInterface.enabledAnimation = (l - 1 << 8) + stream.readUnsignedByte();
                                } else {
                                        rsInterface.enabledAnimation = -1;
                                }
                                rsInterface.modelZoom = stream.readUnsignedWord();
                                rsInterface.modelRotation1 = stream.readUnsignedWord();
                                rsInterface.modelRotation2 = stream.readUnsignedWord();
			}
			if (rsInterface.type == 7) {
				rsInterface.inv = new int[rsInterface.width * rsInterface.height];
				rsInterface.invStackSizes = new int[rsInterface.width * rsInterface.height];
				rsInterface.aBoolean223 = stream.readUnsignedByte() == 1;
				int l2 = stream.readUnsignedByte();
				if (textDrawingAreas != null) {
					rsInterface.textDrawingAreas = textDrawingAreas[l2];
				}
				rsInterface.aBoolean268 = stream.readUnsignedByte() == 1;
				rsInterface.textColor = stream.readDWord();
				rsInterface.invSpritePadX = stream.readSignedWord();
				rsInterface.invSpritePadY = stream.readSignedWord();
				rsInterface.isInventoryInterface = stream.readUnsignedByte() == 1;
				rsInterface.actions = new String[5];
				for (int k4 = 0; k4 < 5; k4++) {
					rsInterface.actions[k4] = stream.readString();
					if (rsInterface.actions[k4].length() == 0) {
						rsInterface.actions[k4] = null;
					}
				}

			}
			if (rsInterface.atActionType == 2 || rsInterface.type == 2) {
				rsInterface.selectedActionName = stream.readString();
				rsInterface.spellName = stream.readString();
				rsInterface.spellUsableOn = stream.readUnsignedWord();
			}
			if (rsInterface.type == 8) {
				rsInterface.disabledText = stream.readString();
			}
			if (rsInterface.atActionType == 1 || rsInterface.atActionType == 4 || rsInterface.atActionType == 5 || rsInterface.atActionType == 6) {
				rsInterface.tooltip = stream.readString();
				if (rsInterface.tooltip.length() == 0) {
					if (rsInterface.atActionType == 1) {
						rsInterface.tooltip = "Ok";
					}
					if (rsInterface.atActionType == 4) {
						rsInterface.tooltip = "Select";
					}
					if (rsInterface.atActionType == 5) {
						rsInterface.tooltip = "Select";
					}
					if (rsInterface.atActionType == 6) {
						rsInterface.tooltip = "Continue";
					}
				}
			}
		}
               spriteCache = null;
	}

    private Model getModelForMedia(int i, int j) {
		ItemDef itemDefinition = null;
		if (type == 4) {
			itemDefinition = ItemDef.lookup(id);
                       lightness += itemDefinition.ambient;
                       shading += itemDefinition.contrast;
		}
                Model model = (Model) modelCache.get((i << 16) + j);
		if (model != null)
			return model;
		if (i == 1)
			model = Model.create(j);
               if (i == 2)
                        model = EntityDef.forID(j).getModel();
                if (i == 3)
                        model = Game.myPlayer.getDialogueModel();
		if (i == 4)
                       model = ItemDef.lookup(j).getInterfaceModel(50);
		if (i == 5)
			model = null;
		if (model != null)
                        modelCache.put(model, (i << 16) + j);
		return model;
	}

    private static Sprite loadSprite(int i, StreamLoader streamLoader, String s) {
               long l = (TextClass.hashSpriteName(s) << 8) + i;
                Sprite sprite = (Sprite) spriteCache.get(l);
		if (sprite != null) {
			return sprite;
		}
		try {
			sprite = new Sprite(streamLoader, s, i);
                        spriteCache.put(sprite, l);
		} catch (Exception _ex) {
			return null;
		}
		return sprite;
	}
	
	public static void discardInterface(int i) {
		if (i == -1)
			return;
		for (int j = 0; j < interfaceCache.length; j++)
			if (interfaceCache[j] != null
					&& interfaceCache[j].parentID == i
					&& interfaceCache[j].type != 2)
				interfaceCache[j] = null;

	}

        public static void clearModelCache(Model model, int id, int type) {
                modelCache.unlinkAll();
                if (model != null && type != 4) {
                        modelCache.put(model, (type << 16) + id);
                }
        }

        public Model prepareModel(int j, int k, boolean flag) {
		lightness = 64;
		shading = 768;
		Model model;
                if (flag) {
                        model = getModelForMedia(enabledMediaType, enabledMediaId);
		} else {
                        model = getModelForMedia(mediaType, mediaId);
		}
		if (model == null) {
			return null;
		}
		if (k == -1 && j == -1 && model.faceColor == null) {
			return model;
		}
                Model model_1 = new Model(true, AnimFrame.isNullFrame(k) & AnimFrame.isNullFrame(j), false, model);
		if (k != -1 || j != -1) {
			model_1.buildVertexGroups();
		}
		if (k != -1) {
			model_1.applyFrame(k);
		}
		if (j != -1) {
			model_1.applyFrame(j);
		}
		model_1.applyLighting(lightness, shading, -50, -10, -50, true);
		return model_1;
	}

	public RSInterface() {
	}

	public Sprite sprite1;
	public int anInt208;
	public Sprite sprites[];
	public static RSInterface interfaceCache[];
	public int anIntArray212[];
	public int anInt214;
	public int spritesX[];
	public int anInt216;
	public int atActionType;
	public String spellName;
	public int anInt219;
	public int width;
	public String tooltip;
	public String selectedActionName;
	public boolean aBoolean223;
	public int scrollPosition;
	public String actions[];
	public int valueIndexArray[][];
	public boolean aBoolean227;
	public String enabledText;
	public int anInt230;
	public int invSpritePadX;
	public int textColor;
        public int mediaType;
        public int mediaId;
	public boolean aBoolean235;
	public int parentID;
	public int spellUsableOn;
        private static MRUCache spriteCache;
	public int anInt239;
	public int children[];
	public int childX[];
	public boolean usableItemInterface;
	public TextDrawingArea textDrawingAreas;
	public int invSpritePadY;
	public int anIntArray245[];
	public int anInt246;
	public int spritesY[];
	public String disabledText;
	public boolean isInventoryInterface;
	public int id;
	public int invStackSizes[];
	public int inv[];
	public byte aByte254;
        private int enabledMediaType;
        private int enabledMediaId;
        public int disabledAnimation;
        public int enabledAnimation;
	public boolean aBoolean259;
	public Sprite sprite2;
	public int scrollMax;
	public int type;
        public int offsetX;
        private static final MRUCache modelCache = new MRUCache(30);
	public int anInt265;
	public boolean aBoolean266;
	public int height;
	public static int shading;
	public static int lightness;
	public boolean aBoolean268;
        public int modelZoom;
        public int modelRotation1;
        public int modelRotation2;
	public int childY[];

}
