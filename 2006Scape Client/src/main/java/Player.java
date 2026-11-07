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
		if (aBoolean1699) {
			return model;
		}
		if (super.anInt1520 != -1 && super.anInt1521 != -1) {
			SpotAnim spotAnim = SpotAnim.cache[super.anInt1520];
			Model model_2 = spotAnim.getModel();
			if (model_2 != null) {
				Model model_3 = new Model(true, AnimationFrame.isMissingFrameId(super.anInt1521), false, model_2);
				model_3.translate(0, -super.anInt1524, 0);
				model_3.buildAnimationGroups();
				model_3.applyAnimationFrame(spotAnim.aAnimation_407.anIntArray353[super.anInt1521]);
				model_3.faceGroups = null;
				model_3.vertexGroups = null;
				if (spotAnim.anInt410 != 128 || spotAnim.anInt411 != 128) {
					model_3.scale(spotAnim.anInt410, spotAnim.anInt410, spotAnim.anInt411);
				}
				model_3.applyLighting(64 + spotAnim.anInt413, 850 + spotAnim.anInt414, -30, -50, -30, true);
				Model aclass30_sub2_sub4_sub6_1s[] = {model, model_3};
				model = new Model(aclass30_sub2_sub4_sub6_1s);
			}
		}
		if (aModel_1714 != null) {
			if (Game.loopCycle >= anInt1708) {
				aModel_1714 = null;
			}
			if (Game.loopCycle >= anInt1707 && Game.loopCycle < anInt1708) {
				Model model_1 = aModel_1714;
				model_1.translate(anInt1711 - super.x, anInt1712 - anInt1709, anInt1713 - super.y);
				if (super.turnDirection == 512) {
					model_1.rotateYQuarterTurn();
					model_1.rotateYQuarterTurn();
					model_1.rotateYQuarterTurn();
				} else if (super.turnDirection == 1024) {
					model_1.rotateYQuarterTurn();
					model_1.rotateYQuarterTurn();
				} else if (super.turnDirection == 1536) {
					model_1.rotateYQuarterTurn();
				}
				Model aclass30_sub2_sub4_sub6s[] = {model, model_1};
				model = new Model(aclass30_sub2_sub4_sub6s);
				if (super.turnDirection == 512) {
					model_1.rotateYQuarterTurn();
				} else if (super.turnDirection == 1024) {
					model_1.rotateYQuarterTurn();
					model_1.rotateYQuarterTurn();
				} else if (super.turnDirection == 1536) {
					model_1.rotateYQuarterTurn();
					model_1.rotateYQuarterTurn();
					model_1.rotateYQuarterTurn();
				}
				model_1.translate(super.x - anInt1711, anInt1709 - anInt1712, super.y - anInt1713);
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
		for (int j = 0; j < 12; j++) {
			int k = stream.readUnsignedByte();
			if (k == 0) {
				equipment[j] = 0;
				continue;
			}
			int i1 = stream.readUnsignedByte();
			equipment[j] = (k << 8) + i1;
			if (j == 0 && equipment[0] == 65535) {
				desc = EntityDef.forID(stream.readUnsignedShortBigEndian());
				break;
			}
			if (equipment[j] >= 512 && equipment[j] - 512 < ItemDef.totalItems) {
				int l1 = ItemDef.forID(equipment[j] - 512).team;
				if (l1 != 0) {
					team = l1;
				}
			}
		}

		for (int l = 0; l < 5; l++) {
			int j1 = stream.readUnsignedByte();
			if (j1 < 0 || j1 >= Game.anIntArrayArray1003[l].length) {
				j1 = 0;
			}
			anIntArray1700[l] = j1;
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
		for (int k1 = 0; k1 < 12; k1++) {
			appearanceKey <<= 4;
			if (equipment[k1] >= 256) {
				appearanceKey += equipment[k1] - 256;
			}
		}

		if (equipment[0] >= 256) {
			appearanceKey += equipment[0] - 256 >> 4;
		}
		if (equipment[1] >= 256) {
			appearanceKey += equipment[1] - 256 >> 8;
		}
		for (int i2 = 0; i2 < 5; i2++) {
			appearanceKey <<= 3;
			appearanceKey += anIntArray1700[i2];
		}

		appearanceKey <<= 1;
		appearanceKey += gender;
	}

	private Model buildBodyModel() {
		if (desc != null) {
			int frameId = -1;
			if (super.anim >= 0 && super.anInt1529 == 0) {
				frameId = Animation.anims[super.anim].anIntArray353[super.anInt1527];
			} else if (super.movementAnimationId >= 0) {
				frameId = Animation.anims[super.movementAnimationId].anIntArray353[super.anInt1518];
			}
			Model model = desc.getAnimatedModel(-1, frameId, null);
			return model;
		}
		long cacheKey = appearanceKey;
		int primaryFrameId = -1;
		int secondaryFrameId = -1;
		int slot5Override = -1;
		int slot3Override = -1;
		if (super.anim >= 0 && super.anInt1529 == 0) {
			Animation animation = Animation.anims[super.anim];
			primaryFrameId = animation.anIntArray353[super.anInt1527];
			if (super.movementAnimationId >= 0 && super.movementAnimationId != super.idleAnimationId) {
				secondaryFrameId = Animation.anims[super.movementAnimationId].anIntArray353[super.anInt1518];
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
			primaryFrameId = Animation.anims[super.movementAnimationId].anIntArray353[super.anInt1518];
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
				if (equipmentCode >= 256 && equipmentCode < 512 && !IDK.cache[equipmentCode - 256].method537()) {
					anyPartNotReady = true;
				}
				if (equipmentCode >= 512 && !ItemDef.forID(equipmentCode - 512).isWornModelReady(gender)) {
					anyPartNotReady = true;
				}
			}

			if (anyPartNotReady) {
				if (aLong1697 != -1L) {
					bodyModel = (Model) mruNodes.insertFromCache(aLong1697);
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
					Model kitPart = IDK.cache[partEquipmentCode - 256].method538();
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
				if (anIntArray1700[colourSlot] != 0) {
					bodyModel.recolor(Game.anIntArrayArray1003[colourSlot][0], Game.anIntArrayArray1003[colourSlot][anIntArray1700[colourSlot]]);
					if (colourSlot == 1) {
						bodyModel.recolor(Game.anIntArray1204[0], Game.anIntArray1204[anIntArray1700[colourSlot]]);
					}
				}
			}

			bodyModel.buildAnimationGroups();
			bodyModel.applyLighting(64, 850, -30, -50, -30, true);
			mruNodes.removeFromCache(bodyModel, cacheKey);
			aLong1697 = cacheKey;
		}
		if (aBoolean1699) {
			return bodyModel;
		}
		Model animatedModel = Model.animationScratchModel;
		animatedModel.copyIntoAnimationScratch(bodyModel, AnimationFrame.isMissingFrameId(primaryFrameId) & AnimationFrame.isMissingFrameId(secondaryFrameId));
		if (primaryFrameId != -1 && secondaryFrameId != -1) {
			animatedModel.applyInterleavedFrames(Animation.anims[super.anim].anIntArray357, secondaryFrameId, primaryFrameId);
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

	public int privelage;
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
			if (equipmentCode >= 256 && equipmentCode < 512 && !IDK.cache[equipmentCode - 256].method539()) {
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
				Model kitPart = IDK.cache[equipmentCode - 256].method540();
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
			if (anIntArray1700[colourSlot] != 0) {
				model.recolor(Game.anIntArrayArray1003[colourSlot][0], Game.anIntArrayArray1003[colourSlot][anIntArray1700[colourSlot]]);
				if (colourSlot == 1) {
					model.recolor(Game.anIntArray1204[0], Game.anIntArray1204[anIntArray1700[colourSlot]]);
				}
			}
		}

		return model;
	}

	Player() {
		aLong1697 = -1L;
		aBoolean1699 = false;
		anIntArray1700 = new int[5];
		visible = false;
		equipment = new int[12];
	}

	private long aLong1697;
	public EntityDef desc;
	boolean aBoolean1699;
	final int[] anIntArray1700;
	public int team;
	private int gender;
	public String name;
	static LruCache mruNodes = new LruCache(260);
	public int combatLevel;
	public int headIcon;
	public int skullIcon;
	public int hintIcon;
	public int anInt1707;
	int anInt1708;
	int anInt1709;
	boolean visible;
	int anInt1711;
	int anInt1712;
	int anInt1713;
	Model aModel_1714;
	public final int[] equipment;
	private long appearanceKey;
	int anInt1719;
	int anInt1720;
	int anInt1721;
	int anInt1722;
	int skill;

}
