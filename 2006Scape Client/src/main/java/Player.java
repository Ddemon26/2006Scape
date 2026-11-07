// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

public final class Player extends Entity {

	@Override
	public Model getRotatedModel() {
		if (!visible) {
			return null;
		}
		Model model = buildBodyModel();
		if (model == null) {
			return null;
		}
		super.height = model.modelHeight;
		model.pickByBounds = true;
		if (staticModelOnly) {
			return model;
		}
		if (super.spotAnimId != -1 && super.spotFrame != -1) {
			SpotAnim spotAnim = SpotAnim.cache[super.spotAnimId];
			Model spotModel = spotAnim.getModel();
			if (spotModel != null) {
				Model spotCopy = new Model(true, AnimationFrame.isMissingFrameId(super.spotFrame), false, spotModel);
				spotCopy.translate(0, -super.spotHeight, 0);
				spotCopy.buildAnimationGroups();
				spotCopy.applyAnimationFrame(spotAnim.animation.frameIds[super.spotFrame]);
				spotCopy.faceGroups = null;
				spotCopy.vertexGroups = null;
				if (spotAnim.horizontalScale128 != 128 || spotAnim.verticalScale128 != 128) {
					spotCopy.scale(spotAnim.horizontalScale128, spotAnim.horizontalScale128, spotAnim.verticalScale128);
				}
				spotCopy.applyLighting(64 + spotAnim.ambientLightOffset, 850 + spotAnim.lightDivisorOffset, -30, -50, -30, true);
				Model spotParts[] = {model, spotCopy};
				model = new Model(spotParts);
			}
		}
		if (attachedModel != null) {
			if (Game.loopCycle >= attachedModelEndCycle) {
				attachedModel = null;
			}
			if (Game.loopCycle >= attachedModelStartCycle && Game.loopCycle < attachedModelEndCycle) {
				Model attachedPart = attachedModel;
				attachedPart.translate(attachedModelX - super.x, attachedModelHeight - groundHeight, attachedModelY - super.y);
				if (super.turnDirection == 512) {
					attachedPart.rotateYQuarterTurn();
					attachedPart.rotateYQuarterTurn();
					attachedPart.rotateYQuarterTurn();
				} else if (super.turnDirection == 1024) {
					attachedPart.rotateYQuarterTurn();
					attachedPart.rotateYQuarterTurn();
				} else if (super.turnDirection == 1536) {
					attachedPart.rotateYQuarterTurn();
				}
				Model attachedParts[] = {model, attachedPart};
				model = new Model(attachedParts);
				if (super.turnDirection == 512) {
					attachedPart.rotateYQuarterTurn();
				} else if (super.turnDirection == 1024) {
					attachedPart.rotateYQuarterTurn();
					attachedPart.rotateYQuarterTurn();
				} else if (super.turnDirection == 1536) {
					attachedPart.rotateYQuarterTurn();
					attachedPart.rotateYQuarterTurn();
					attachedPart.rotateYQuarterTurn();
				}
				attachedPart.translate(super.x - attachedModelX, groundHeight - attachedModelHeight, super.y - attachedModelY);
			}
		}
		model.pickByBounds = true;
		return model;
	}

	public void updatePlayer(Stream stream) {
		stream.currentOffset = 0;
		gender = stream.readUnsignedByte();
		headIcon = stream.readUnsignedByte();
		skullIcon = stream.readUnsignedByte();
		desc = null;
		team = 0;
		for (int equipmentSlot = 0; equipmentSlot < 12; equipmentSlot++) {
			int equipmentHighByte = stream.readUnsignedByte();
			if (equipmentHighByte == 0) {
				equipment[equipmentSlot] = 0;
				continue;
			}
			int equipmentLowByte = stream.readUnsignedByte();
			equipment[equipmentSlot] = (equipmentHighByte << 8) + equipmentLowByte;
			if (equipmentSlot == 0 && equipment[0] == 65535) {
				desc = EntityDef.forID(stream.readUnsignedShortBigEndian());
				break;
			}
			if (equipment[equipmentSlot] >= 512 && equipment[equipmentSlot] - 512 < ItemDef.totalItems) {
				int l1 = ItemDef.forID(equipment[equipmentSlot] - 512).team;
				if (l1 != 0) {
					team = l1;
				}
			}
		}

		for (int colourSlot = 0; colourSlot < 5; colourSlot++) {
			int colourIndex = stream.readUnsignedByte();
			if (colourIndex < 0 || colourIndex >= Game.anIntArrayArray1003[colourSlot].length) {
				colourIndex = 0;
			}
			appearanceColours[colourSlot] = colourIndex;
		}

		super.idleAnimationId = stream.readUnsignedShortBigEndian();
		if (super.idleAnimationId == 65535) {
			super.idleAnimationId = -1;
		}
		super.turnInPlaceAnimationId = stream.readUnsignedShortBigEndian();
		if (super.turnInPlaceAnimationId == 65535) {
			super.turnInPlaceAnimationId = -1;
		}
		super.walkAnimationId = stream.readUnsignedShortBigEndian();
		if (super.walkAnimationId == 65535) {
			super.walkAnimationId = -1;
		}
		super.reverseWalkAnimationId = stream.readUnsignedShortBigEndian();
		if (super.reverseWalkAnimationId == 65535) {
			super.reverseWalkAnimationId = -1;
		}
		super.sidestepNegativeAnimationId = stream.readUnsignedShortBigEndian();
		if (super.sidestepNegativeAnimationId == 65535) {
			super.sidestepNegativeAnimationId = -1;
		}
		super.sidestepPositiveAnimationId = stream.readUnsignedShortBigEndian();
		if (super.sidestepPositiveAnimationId == 65535) {
			super.sidestepPositiveAnimationId = -1;
		}
		super.runAnimationId = stream.readUnsignedShortBigEndian();
		if (super.runAnimationId == 65535) {
			super.runAnimationId = -1;
		}
		name = TextClass.formatDisplayName(TextClass.decodeBase37Name(stream.readLongBigEndian()));
		combatLevel = stream.readUnsignedByte();
		skill = stream.readUnsignedShortBigEndian();
		visible = true;
		appearanceKey = 0L;
		for (int keySlot = 0; keySlot < 12; keySlot++) {
			appearanceKey <<= 4;
			if (equipment[keySlot] >= 256) {
				appearanceKey += equipment[keySlot] - 256;
			}
		}

		if (equipment[0] >= 256) {
			appearanceKey += equipment[0] - 256 >> 4;
		}
		if (equipment[1] >= 256) {
			appearanceKey += equipment[1] - 256 >> 8;
		}
		for (int keyColourSlot = 0; keyColourSlot < 5; keyColourSlot++) {
			appearanceKey <<= 3;
			appearanceKey += appearanceColours[keyColourSlot];
		}

		appearanceKey <<= 1;
		appearanceKey += gender;
	}

	private Model buildBodyModel() {
		if (desc != null) {
			int frameId = -1;
			if (super.anim >= 0 && super.animationDelay == 0) {
				frameId = Animation.anims[super.anim].frameIds[super.animFrame];
			} else if (super.movementAnimationId >= 0) {
				frameId = Animation.anims[super.movementAnimationId].frameIds[super.movementFrame];
			}
			Model model = desc.getAnimatedModel(-1, frameId, null);
			return model;
		}
		long cacheKey = appearanceKey;
		int primaryFrameId = -1;
		int secondaryFrameId = -1;
		int slot5Override = -1;
		int slot3Override = -1;
		if (super.anim >= 0 && super.animationDelay == 0) {
			Animation animation = Animation.anims[super.anim];
			primaryFrameId = animation.frameIds[super.animFrame];
			if (super.movementAnimationId >= 0 && super.movementAnimationId != super.idleAnimationId) {
				secondaryFrameId = Animation.anims[super.movementAnimationId].frameIds[super.movementFrame];
			}
			if (animation.slot5EquipmentOverride >= 0) {
				slot5Override = animation.slot5EquipmentOverride;
				cacheKey += slot5Override - equipment[5] << 40;
			}
			if (animation.slot3EquipmentOverride >= 0) {
				slot3Override = animation.slot3EquipmentOverride;
				cacheKey += slot3Override - equipment[3] << 48;
			}
		} else if (super.movementAnimationId >= 0) {
			primaryFrameId = Animation.anims[super.movementAnimationId].frameIds[super.movementFrame];
		}
		Model bodyModel = (Model) mruNodes.insertFromCache(cacheKey);
		if (bodyModel == null) {
			boolean anyPartNotReady = false;
			for (int slot = 0; slot < 12; slot++) {
				int equipmentCode = equipment[slot];
				if (slot3Override >= 0 && slot == 3) {
					equipmentCode = slot3Override;
				}
				if (slot5Override >= 0 && slot == 5) {
					equipmentCode = slot5Override;
				}
				if (equipmentCode >= 256 && equipmentCode < 512 && !IDK.cache[equipmentCode - 256].areBodyModelsLoaded()) {
					anyPartNotReady = true;
				}
				if (equipmentCode >= 512 && !ItemDef.forID(equipmentCode - 512).isWornModelReady(gender)) {
					anyPartNotReady = true;
				}
			}

			if (anyPartNotReady) {
				if (lastBuiltModelKey != -1L) {
					bodyModel = (Model) mruNodes.insertFromCache(lastBuiltModelKey);
				}
				if (bodyModel == null) {
					return null;
				}
			}
		}
		if (bodyModel == null) {
			Model parts[] = new Model[12];
			int partCount = 0;
			for (int partSlot = 0; partSlot < 12; partSlot++) {
				int partEquipmentCode = equipment[partSlot];
				if (slot3Override >= 0 && partSlot == 3) {
					partEquipmentCode = slot3Override;
				}
				if (slot5Override >= 0 && partSlot == 5) {
					partEquipmentCode = slot5Override;
				}
				if (partEquipmentCode >= 256 && partEquipmentCode < 512) {
					Model kitPart = IDK.cache[partEquipmentCode - 256].buildBodyModel();
					if (kitPart != null) {
						parts[partCount++] = kitPart;
					}
				}
				if (partEquipmentCode >= 512) {
					Model itemPart = ItemDef.forID(partEquipmentCode - 512).getWornModel(gender);
					if (itemPart != null) {
						parts[partCount++] = itemPart;
					}
				}
			}

			bodyModel = new Model(partCount, parts);
			for (int colourSlot = 0; colourSlot < 5; colourSlot++) {
				if (appearanceColours[colourSlot] != 0) {
					bodyModel.recolor(Game.anIntArrayArray1003[colourSlot][0], Game.anIntArrayArray1003[colourSlot][appearanceColours[colourSlot]]);
					if (colourSlot == 1) {
						bodyModel.recolor(Game.anIntArray1204[0], Game.anIntArray1204[appearanceColours[colourSlot]]);
					}
				}
			}

			bodyModel.buildAnimationGroups();
			bodyModel.applyLighting(64, 850, -30, -50, -30, true);
			mruNodes.removeFromCache(bodyModel, cacheKey);
			lastBuiltModelKey = cacheKey;
		}
		if (staticModelOnly) {
			return bodyModel;
		}
		Model animatedModel = Model.animationScratchModel;
		animatedModel.copyIntoAnimationScratch(bodyModel, AnimationFrame.isMissingFrameId(primaryFrameId) & AnimationFrame.isMissingFrameId(secondaryFrameId));
		if (primaryFrameId != -1 && secondaryFrameId != -1) {
			animatedModel.applyInterleavedFrames(Animation.anims[super.anim].interleavedTransformIndices, secondaryFrameId, primaryFrameId);
		} else if (primaryFrameId != -1) {
			animatedModel.applyAnimationFrame(primaryFrameId);
		}
		animatedModel.calculateBounds();
		animatedModel.faceGroups = null;
		animatedModel.vertexGroups = null;
		return animatedModel;
	}

	@Override
	public boolean isVisible() {
		return visible;
	}

	public int rights;
	public Model buildHeadModel() {
		if (!visible) {
			return null;
		}
		if (desc != null) {
			return desc.getHeadModel();
		}
		boolean anyPartNotReady = false;
		for (int slot = 0; slot < 12; slot++) {
			int equipmentCode = equipment[slot];
			if (equipmentCode >= 256 && equipmentCode < 512 && !IDK.cache[equipmentCode - 256].areHeadModelsLoaded()) {
				anyPartNotReady = true;
			}
			if (equipmentCode >= 512 && !ItemDef.forID(equipmentCode - 512).isHeadModelReady(gender)) {
				anyPartNotReady = true;
			}
		}

		if (anyPartNotReady) {
			return null;
		}
		Model parts[] = new Model[12];
		int partCount = 0;
		for (int partSlot = 0; partSlot < 12; partSlot++) {
			int equipmentCode = equipment[partSlot];
			if (equipmentCode >= 256 && equipmentCode < 512) {
				Model kitPart = IDK.cache[equipmentCode - 256].buildHeadModel();
				if (kitPart != null) {
					parts[partCount++] = kitPart;
				}
			}
			if (equipmentCode >= 512) {
				Model itemPart = ItemDef.forID(equipmentCode - 512).getHeadModel(gender);
				if (itemPart != null) {
					parts[partCount++] = itemPart;
				}
			}
		}

		Model model = new Model(partCount, parts);
		for (int colourSlot = 0; colourSlot < 5; colourSlot++) {
			if (appearanceColours[colourSlot] != 0) {
				model.recolor(Game.anIntArrayArray1003[colourSlot][0], Game.anIntArrayArray1003[colourSlot][appearanceColours[colourSlot]]);
				if (colourSlot == 1) {
					model.recolor(Game.anIntArray1204[0], Game.anIntArray1204[appearanceColours[colourSlot]]);
				}
			}
		}

		return model;
	}

	Player() {
		lastBuiltModelKey = -1L;
		staticModelOnly = false;
		appearanceColours = new int[5];
		visible = false;
		equipment = new int[12];
	}

	private long lastBuiltModelKey;
	public EntityDef desc;
	boolean staticModelOnly;
	final int[] appearanceColours;
	public int team;
	private int gender;
	public String name;
	static LruCache mruNodes = new LruCache(260);
	public int combatLevel;
	public int headIcon;
	public int skullIcon;
	public int hintIcon;
	public int attachedModelStartCycle;
	int attachedModelEndCycle;
	int groundHeight;
	boolean visible;
	int attachedModelX;
	int attachedModelHeight;
	int attachedModelY;
	Model attachedModel;
	public final int[] equipment;
	private long appearanceKey;
	int attachedMinTileX;
	int attachedMinTileY;
	int attachedMaxTileX;
	int attachedMaxTileY;
	int skill;

}
