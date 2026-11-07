public final class RSInterface {

	public void swapInventoryItems(int firstSlot, int secondSlot) {
		int savedValue = itemIdsPlusOne[firstSlot];
		itemIdsPlusOne[firstSlot] = itemIdsPlusOne[secondSlot];
		itemIdsPlusOne[secondSlot] = savedValue;
		savedValue = itemQuantities[firstSlot];
		itemQuantities[firstSlot] = itemQuantities[secondSlot];
		itemQuantities[secondSlot] = savedValue;
	}
	
	public static void unpack(StreamLoader interfaceArchive, TextDrawingArea textDrawingAreas[], StreamLoader spriteArchive) {
		spriteCache = new LruCache(50000);
		Stream stream = new Stream(interfaceArchive.getDataForName("data"));
		int currentParentId = -1;
		int interfaceCount = stream.readUnsignedShortBigEndian();
		interfaceCache = new RSInterface[interfaceCount];
		while (stream.currentOffset < stream.buffer.length) {
			int interfaceId = stream.readUnsignedShortBigEndian();
			if (interfaceId == 65535) {
				currentParentId = stream.readUnsignedShortBigEndian();
				interfaceId = stream.readUnsignedShortBigEndian();
			}
			RSInterface rsInterface = interfaceCache[interfaceId] = new RSInterface();
			rsInterface.id = interfaceId;
			rsInterface.parentID = currentParentId;
			rsInterface.type = stream.readUnsignedByte();
			rsInterface.atActionType = stream.readUnsignedByte();
			rsInterface.contentType = stream.readUnsignedShortBigEndian();
			rsInterface.width = stream.readUnsignedShortBigEndian();
			rsInterface.height = stream.readUnsignedShortBigEndian();
			rsInterface.transparency = (byte) stream.readUnsignedByte();
			rsInterface.hoverRedirectId = stream.readUnsignedByte();
			if (rsInterface.hoverRedirectId != 0) {
				rsInterface.hoverRedirectId = (rsInterface.hoverRedirectId - 1 << 8) + stream.readUnsignedByte();
			} else {
				rsInterface.hoverRedirectId = -1;
			}
			int conditionCount = stream.readUnsignedByte();
			if (conditionCount > 0) {
				rsInterface.conditionOperators = new int[conditionCount];
				rsInterface.conditionValues = new int[conditionCount];
				for (int conditionIndex = 0; conditionIndex < conditionCount; conditionIndex++) {
					rsInterface.conditionOperators[conditionIndex] = stream.readUnsignedByte();
					rsInterface.conditionValues[conditionIndex] = stream.readUnsignedShortBigEndian();
				}

			}
			int valueArrayCount = stream.readUnsignedByte();
			if (valueArrayCount > 0) {
				rsInterface.valueIndexArray = new int[valueArrayCount][];
				for (int valueArrayIndex = 0; valueArrayIndex < valueArrayCount; valueArrayIndex++) {
					int valueArrayLength = stream.readUnsignedShortBigEndian();
					rsInterface.valueIndexArray[valueArrayIndex] = new int[valueArrayLength];
					for (int valueIndex = 0; valueIndex < valueArrayLength; valueIndex++) {
						rsInterface.valueIndexArray[valueArrayIndex][valueIndex] = stream.readUnsignedShortBigEndian();
					}

				}

			}
			if (rsInterface.type == 0) {
				rsInterface.scrollMax = stream.readUnsignedShortBigEndian();
				rsInterface.hidden = stream.readUnsignedByte() == 1;
				int childCount = stream.readUnsignedShortBigEndian();
				rsInterface.children = new int[childCount];
				rsInterface.childX = new int[childCount];
				rsInterface.childY = new int[childCount];
				for (int childIndex = 0; childIndex < childCount; childIndex++) {
					rsInterface.children[childIndex] = stream.readUnsignedShortBigEndian();
					rsInterface.childX[childIndex] = stream.readSignedShortBigEndian();
					rsInterface.childY[childIndex] = stream.readSignedShortBigEndian();
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
				for (int slotIndex = 0; slotIndex < 20; slotIndex++) {
					int hasSlotSprite = stream.readUnsignedByte();
					if (hasSlotSprite == 1) {
						rsInterface.spritesX[slotIndex] = stream.readSignedShortBigEndian();
						rsInterface.spritesY[slotIndex] = stream.readSignedShortBigEndian();
						String slotSpriteRef = stream.readLfTerminatedString();
						if (spriteArchive != null && slotSpriteRef.length() > 0) {
							int slotSpriteSeparator = slotSpriteRef.lastIndexOf(",");
							rsInterface.sprites[slotIndex] = loadCachedSprite(Integer.parseInt(slotSpriteRef.substring(slotSpriteSeparator + 1)), spriteArchive, slotSpriteRef.substring(0, slotSpriteSeparator));
						}
					}
				}

				rsInterface.actions = new String[5];
				for (int actionIndex = 0; actionIndex < 5; actionIndex++) {
					rsInterface.actions[actionIndex] = stream.readLfTerminatedString();
					if (rsInterface.actions[actionIndex].length() == 0) {
						rsInterface.actions[actionIndex] = null;
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
				rsInterface.filled = stream.readUnsignedByte() == 1;
			}
			if (rsInterface.type == 4 || rsInterface.type == 1) {
				rsInterface.centeredText = stream.readUnsignedByte() == 1;
				int fontIndex = stream.readUnsignedByte();
				if (textDrawingAreas != null) {
					rsInterface.textDrawingAreas = textDrawingAreas[fontIndex];
				}
				rsInterface.shadowedText = stream.readUnsignedByte() == 1;
			}
			if (rsInterface.type == 4) {
				rsInterface.disabledText = stream.readLfTerminatedString().replaceAll("RuneScape", ClientSettings.SERVER_NAME);
				rsInterface.enabledText = stream.readLfTerminatedString();
			}
			if (rsInterface.type == 1 || rsInterface.type == 3 || rsInterface.type == 4) {
				rsInterface.textColor = stream.readIntBigEndian();
			}
			if (rsInterface.type == 3 || rsInterface.type == 4) {
				rsInterface.enabledColor = stream.readIntBigEndian();
				rsInterface.disabledHoverColor = stream.readIntBigEndian();
				rsInterface.enabledHoverColor = stream.readIntBigEndian();
			}
			if (rsInterface.type == 5) {
				String spriteRef = stream.readLfTerminatedString();
				if (spriteArchive != null && spriteRef.length() > 0) {
					int spriteSeparator1 = spriteRef.lastIndexOf(",");
					rsInterface.sprite1 = loadCachedSprite(Integer.parseInt(spriteRef.substring(spriteSeparator1 + 1)), spriteArchive, spriteRef.substring(0, spriteSeparator1));
				}
				spriteRef = stream.readLfTerminatedString();
				if (spriteArchive != null && spriteRef.length() > 0) {
					int spriteSeparator2 = spriteRef.lastIndexOf(",");
					rsInterface.sprite2 = loadCachedSprite(Integer.parseInt(spriteRef.substring(spriteSeparator2 + 1)), spriteArchive, spriteRef.substring(0, spriteSeparator2));
				}
			}
			if (rsInterface.type == 6) {
				int mediaFlag = stream.readUnsignedByte();
				if (mediaFlag != 0) {
					rsInterface.mediaType = 1;
					rsInterface.mediaID = (mediaFlag - 1 << 8) + stream.readUnsignedByte();
				}
				mediaFlag = stream.readUnsignedByte();
				if (mediaFlag != 0) {
					rsInterface.enabledMediaType = 1;
					rsInterface.enabledMediaID = (mediaFlag - 1 << 8) + stream.readUnsignedByte();
				}
				mediaFlag = stream.readUnsignedByte();
				if (mediaFlag != 0) {
					rsInterface.disabledAnimationId = (mediaFlag - 1 << 8) + stream.readUnsignedByte();
				} else {
					rsInterface.disabledAnimationId = -1;
				}
				mediaFlag = stream.readUnsignedByte();
				if (mediaFlag != 0) {
					rsInterface.enabledAnimationId = (mediaFlag - 1 << 8) + stream.readUnsignedByte();
				} else {
					rsInterface.enabledAnimationId = -1;
				}
				rsInterface.modelZoom = stream.readUnsignedShortBigEndian();
				rsInterface.modelPitch = stream.readUnsignedShortBigEndian();
				rsInterface.modelYaw = stream.readUnsignedShortBigEndian();
			}
			if (rsInterface.type == 7) {
				rsInterface.itemIdsPlusOne = new int[rsInterface.width * rsInterface.height];
				rsInterface.itemQuantities = new int[rsInterface.width * rsInterface.height];
				rsInterface.centeredText = stream.readUnsignedByte() == 1;
				int fontIndex = stream.readUnsignedByte();
				if (textDrawingAreas != null) {
					rsInterface.textDrawingAreas = textDrawingAreas[fontIndex];
				}
				rsInterface.shadowedText = stream.readUnsignedByte() == 1;
				rsInterface.textColor = stream.readIntBigEndian();
				rsInterface.invSpritePadX = stream.readSignedShortBigEndian();
				rsInterface.invSpritePadY = stream.readSignedShortBigEndian();
				rsInterface.isInventoryInterface = stream.readUnsignedByte() == 1;
				rsInterface.actions = new String[5];
				for (int actionIndex = 0; actionIndex < 5; actionIndex++) {
					rsInterface.actions[actionIndex] = stream.readLfTerminatedString();
					if (rsInterface.actions[actionIndex].length() == 0) {
						rsInterface.actions[actionIndex] = null;
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
		Model model = (Model) modelCache.insertFromCache((modelType << 16) + modelId);
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
			modelCache.removeFromCache(model, (modelType << 16) + modelId);
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
	
	public static void discardInterface(int parentId) {
		if (parentId == -1)
			return;
		for (int index = 0; index < interfaceCache.length; index++)
			if (interfaceCache[index] != null
					&& interfaceCache[index].parentID == parentId
					&& interfaceCache[index].type != 2)
				interfaceCache[index] = null;

	}

	public static void replaceCachedModel(Model model, int modelId, int modelType) {
		modelCache.clear();
		if (model != null && modelType != 4) {
			modelCache.removeFromCache(model, (modelType << 16) + modelId);
		}
	}

	public Model getMediaModel(int j, int k, boolean useEnabledModel) {
		lightness = 64;
		shading = 768;
		Model model;
		if (useEnabledModel) {
			model = loadMediaModel(enabledMediaType, enabledMediaID);
		} else {
			model = loadMediaModel(mediaType, mediaID);
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
	public int contentType;
	public int spritesX[];
	public int disabledHoverColor;
	public int atActionType;
	public String spellName;
	public int enabledColor;
	public int width;
	public String tooltip;
	public String selectedActionName;
	public boolean centeredText;
	public int scrollPosition;
	public String actions[];
	public int valueIndexArray[][];
	public boolean filled;
	public String enabledText;
	public int hoverRedirectId;
	public int invSpritePadX;
	public int textColor;
	public int mediaType;
	public int mediaID;
	public boolean aBoolean235;
	public int parentID;
	public int spellUsableOn;
	private static LruCache spriteCache;
	public int enabledHoverColor;
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
	public byte transparency;
	private int enabledMediaType;
	private int enabledMediaID;
	public int disabledAnimationId;
	public int enabledAnimationId;
	public boolean aBoolean259;
	public Sprite sprite2;
	public int scrollMax;
	public int type;
	public int offsetX;
	private static final LruCache modelCache = new LruCache(30);
	public int offsetY;
	public boolean hidden;
	public int height;
	public static int shading;
	public static int lightness;
	public boolean shadowedText;
	public int modelZoom;
	public int modelPitch;
	public int modelYaw;
	public int childY[];

}
