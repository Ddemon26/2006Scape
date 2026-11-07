public final class RSInterface {

	public void swapInventoryItems(int firstSlot, int secondSlot) {
		int savedValue = itemIdsPlusOne[firstSlot];
		itemIdsPlusOne[firstSlot] = itemIdsPlusOne[secondSlot];
		itemIdsPlusOne[secondSlot] = savedValue;
		savedValue = itemQuantities[firstSlot];
		itemQuantities[firstSlot] = itemQuantities[secondSlot];
		itemQuantities[secondSlot] = savedValue;
	}
	
	public static void unpack(StreamLoader streamLoader, TextDrawingArea textDrawingAreas[], StreamLoader streamLoader_1) {
		spriteCache = new LruCache(50000);
		Stream stream = new Stream(streamLoader.getDataForName("data"));
		int i = -1;
		int j = stream.readUnsignedShortBigEndian();
		interfaceCache = new RSInterface[j];
		while (stream.currentOffset < stream.buffer.length) {
			int k = stream.readUnsignedShortBigEndian();
			if (k == 65535) {
				i = stream.readUnsignedShortBigEndian();
				k = stream.readUnsignedShortBigEndian();
			}
			RSInterface rsInterface = interfaceCache[k] = new RSInterface();
			rsInterface.id = k;
			rsInterface.parentID = i;
			rsInterface.type = stream.readUnsignedByte();
			rsInterface.atActionType = stream.readUnsignedByte();
			rsInterface.anInt214 = stream.readUnsignedShortBigEndian();
			rsInterface.width = stream.readUnsignedShortBigEndian();
			rsInterface.height = stream.readUnsignedShortBigEndian();
			rsInterface.aByte254 = (byte) stream.readUnsignedByte();
			rsInterface.anInt230 = stream.readUnsignedByte();
			if (rsInterface.anInt230 != 0) {
				rsInterface.anInt230 = (rsInterface.anInt230 - 1 << 8) + stream.readUnsignedByte();
			} else {
				rsInterface.anInt230 = -1;
			}
			int i1 = stream.readUnsignedByte();
			if (i1 > 0) {
				rsInterface.conditionOperators = new int[i1];
				rsInterface.conditionValues = new int[i1];
				for (int j1 = 0; j1 < i1; j1++) {
					rsInterface.conditionOperators[j1] = stream.readUnsignedByte();
					rsInterface.conditionValues[j1] = stream.readUnsignedShortBigEndian();
				}

			}
			int k1 = stream.readUnsignedByte();
			if (k1 > 0) {
				rsInterface.valueIndexArray = new int[k1][];
				for (int l1 = 0; l1 < k1; l1++) {
					int i3 = stream.readUnsignedShortBigEndian();
					rsInterface.valueIndexArray[l1] = new int[i3];
					for (int l4 = 0; l4 < i3; l4++) {
						rsInterface.valueIndexArray[l1][l4] = stream.readUnsignedShortBigEndian();
					}

				}

			}
			if (rsInterface.type == 0) {
				rsInterface.scrollMax = stream.readUnsignedShortBigEndian();
				rsInterface.aBoolean266 = stream.readUnsignedByte() == 1;
				int i2 = stream.readUnsignedShortBigEndian();
				rsInterface.children = new int[i2];
				rsInterface.childX = new int[i2];
				rsInterface.childY = new int[i2];
				for (int j3 = 0; j3 < i2; j3++) {
					rsInterface.children[j3] = stream.readUnsignedShortBigEndian();
					rsInterface.childX[j3] = stream.readSignedShortBigEndian();
					rsInterface.childY[j3] = stream.readSignedShortBigEndian();
				}

			}
			if (rsInterface.type == 1) {
				stream.readUnsignedShortBigEndian();
				stream.readUnsignedByte();
			}
			if (rsInterface.type == 2) {
				rsInterface.itemIdsPlusOne = new int[rsInterface.width * rsInterface.height];
				rsInterface.itemQuantities = new int[rsInterface.width * rsInterface.height];
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
						rsInterface.spritesX[j2] = stream.readSignedShortBigEndian();
						rsInterface.spritesY[j2] = stream.readSignedShortBigEndian();
						String s1 = stream.readLfTerminatedString();
						if (streamLoader_1 != null && s1.length() > 0) {
							int i5 = s1.lastIndexOf(",");
							rsInterface.sprites[j2] = loadCachedSprite(Integer.parseInt(s1.substring(i5 + 1)), streamLoader_1, s1.substring(0, i5));
						}
					}
				}

				rsInterface.actions = new String[5];
				for (int l3 = 0; l3 < 5; l3++) {
					rsInterface.actions[l3] = stream.readLfTerminatedString();
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
				rsInterface.disabledText = stream.readLfTerminatedString().replaceAll("RuneScape", ClientSettings.SERVER_NAME);
				rsInterface.enabledText = stream.readLfTerminatedString();
			}
			if (rsInterface.type == 1 || rsInterface.type == 3 || rsInterface.type == 4) {
				rsInterface.textColor = stream.readIntBigEndian();
			}
			if (rsInterface.type == 3 || rsInterface.type == 4) {
				rsInterface.anInt219 = stream.readIntBigEndian();
				rsInterface.anInt216 = stream.readIntBigEndian();
				rsInterface.anInt239 = stream.readIntBigEndian();
			}
			if (rsInterface.type == 5) {
				String s = stream.readLfTerminatedString();
				if (streamLoader_1 != null && s.length() > 0) {
					int i4 = s.lastIndexOf(",");
					rsInterface.sprite1 = loadCachedSprite(Integer.parseInt(s.substring(i4 + 1)), streamLoader_1, s.substring(0, i4));
				}
				s = stream.readLfTerminatedString();
				if (streamLoader_1 != null && s.length() > 0) {
					int j4 = s.lastIndexOf(",");
					rsInterface.sprite2 = loadCachedSprite(Integer.parseInt(s.substring(j4 + 1)), streamLoader_1, s.substring(0, j4));
				}
			}
			if (rsInterface.type == 6) {
				int l = stream.readUnsignedByte();
				if (l != 0) {
					rsInterface.anInt233 = 1;
					rsInterface.mediaID = (l - 1 << 8) + stream.readUnsignedByte();
				}
				l = stream.readUnsignedByte();
				if (l != 0) {
					rsInterface.anInt255 = 1;
					rsInterface.anInt256 = (l - 1 << 8) + stream.readUnsignedByte();
				}
				l = stream.readUnsignedByte();
				if (l != 0) {
					rsInterface.anInt257 = (l - 1 << 8) + stream.readUnsignedByte();
				} else {
					rsInterface.anInt257 = -1;
				}
				l = stream.readUnsignedByte();
				if (l != 0) {
					rsInterface.anInt258 = (l - 1 << 8) + stream.readUnsignedByte();
				} else {
					rsInterface.anInt258 = -1;
				}
				rsInterface.anInt269 = stream.readUnsignedShortBigEndian();
				rsInterface.anInt270 = stream.readUnsignedShortBigEndian();
				rsInterface.anInt271 = stream.readUnsignedShortBigEndian();
			}
			if (rsInterface.type == 7) {
				rsInterface.itemIdsPlusOne = new int[rsInterface.width * rsInterface.height];
				rsInterface.itemQuantities = new int[rsInterface.width * rsInterface.height];
				rsInterface.aBoolean223 = stream.readUnsignedByte() == 1;
				int l2 = stream.readUnsignedByte();
				if (textDrawingAreas != null) {
					rsInterface.textDrawingAreas = textDrawingAreas[l2];
				}
				rsInterface.aBoolean268 = stream.readUnsignedByte() == 1;
				rsInterface.textColor = stream.readIntBigEndian();
				rsInterface.invSpritePadX = stream.readSignedShortBigEndian();
				rsInterface.invSpritePadY = stream.readSignedShortBigEndian();
				rsInterface.isInventoryInterface = stream.readUnsignedByte() == 1;
				rsInterface.actions = new String[5];
				for (int k4 = 0; k4 < 5; k4++) {
					rsInterface.actions[k4] = stream.readLfTerminatedString();
					if (rsInterface.actions[k4].length() == 0) {
						rsInterface.actions[k4] = null;
					}
				}

			}
			if (rsInterface.atActionType == 2 || rsInterface.type == 2) {
				rsInterface.selectedActionName = stream.readLfTerminatedString();
				rsInterface.spellName = stream.readLfTerminatedString();
				rsInterface.spellUsableOn = stream.readUnsignedShortBigEndian();
			}
			if (rsInterface.type == 8) {
				rsInterface.disabledText = stream.readLfTerminatedString();
			}
			if (rsInterface.atActionType == 1 || rsInterface.atActionType == 4 || rsInterface.atActionType == 5 || rsInterface.atActionType == 6) {
				rsInterface.tooltip = stream.readLfTerminatedString();
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

	private Model loadMediaModel(int modelType, int modelId) {
		ItemDef itemDefinition = null;
		if (type == 4) {
			itemDefinition = ItemDef.forID(id);
			lightness += itemDefinition.ambientLightOffset;
			shading += itemDefinition.lightDivisorOffset;
		}
		Model model = (Model) aMRUNodes_264.insertFromCache((modelType << 16) + modelId);
		if (model != null)
			return model;
		if (modelType == 1)
			model = Model.getModel(modelId);
		if (modelType == 2)
			model = EntityDef.forID(modelId).getHeadModel();
		if (modelType == 3)
			model = Game.myPlayer.buildHeadModel();
		if (modelType == 4)
			model = ItemDef.forID(modelId).getUnlitModel(50);
		if (modelType == 5)
			model = null;
		if (model != null)
			aMRUNodes_264.removeFromCache(model, (modelType << 16) + modelId);
		return model;
	}

	private static Sprite loadCachedSprite(int spriteIndex, StreamLoader streamLoader, String spriteName) {
		long cacheKey = (TextClass.hashSpriteName(spriteName) << 8) + spriteIndex;
		Sprite sprite = (Sprite) spriteCache.insertFromCache(cacheKey);
		if (sprite != null) {
			return sprite;
		}
		try {
			sprite = new Sprite(streamLoader, spriteName, spriteIndex);
			spriteCache.removeFromCache(sprite, cacheKey);
		} catch (Exception loadFailure) {
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

	public static void method208(Model model, int id, int type) {
		aMRUNodes_264.clear();
		if (model != null && type != 4) {
			aMRUNodes_264.removeFromCache(model, (type << 16) + id);
		}
	}

	public Model method209(int j, int k, boolean flag) {
		lightness = 64;
		shading = 768;
		Model model;
		if (flag) {
			model = loadMediaModel(anInt255, anInt256);
		} else {
			model = loadMediaModel(anInt233, mediaID);
		}
		if (model == null) {
			return null;
		}
		if (k == -1 && j == -1 && model.faceColors == null) {
			return model;
		}
		Model model_1 = new Model(true, AnimationFrame.isMissingFrameId(k) & AnimationFrame.isMissingFrameId(j), false, model);
		if (k != -1 || j != -1) {
			model_1.buildAnimationGroups();
		}
		if (k != -1) {
			model_1.applyAnimationFrame(k);
		}
		if (j != -1) {
			model_1.applyAnimationFrame(j);
		}
		model_1.applyLighting(lightness, shading, -50, -10, -50, true);
		return model_1;
	}

	public RSInterface() {
	}

	public Sprite sprite1;
	public int modelAnimationTicks;
	public Sprite sprites[];
	public static RSInterface interfaceCache[];
	public int conditionValues[];
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
	public int anInt233;
	public int mediaID;
	public boolean aBoolean235;
	public int parentID;
	public int spellUsableOn;
	private static LruCache spriteCache;
	public int anInt239;
	public int children[];
	public int childX[];
	public boolean usableItemInterface;
	public TextDrawingArea textDrawingAreas;
	public int invSpritePadY;
	public int conditionOperators[];
	public int modelAnimationFrame;
	public int spritesY[];
	public String disabledText;
	public boolean isInventoryInterface;
	public int id;
	public int itemQuantities[];
	public int itemIdsPlusOne[];
	public byte aByte254;
	private int anInt255;
	private int anInt256;
	public int anInt257;
	public int anInt258;
	public boolean aBoolean259;
	public Sprite sprite2;
	public int scrollMax;
	public int type;
	public int anInt263;
	private static final LruCache aMRUNodes_264 = new LruCache(30);
	public int anInt265;
	public boolean aBoolean266;
	public int height;
	public static int shading;
	public static int lightness;
	public boolean aBoolean268;
	public int anInt269;
	public int anInt270;
	public int anInt271;
	public int childY[];

}
