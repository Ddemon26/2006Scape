// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

public final class Player extends Entity {

	@Override
	public Model getRotatedModel() {
		if (!visible) {
			return null;
		}
                Model model = getBaseModel();
		if (model == null) {
			return null;
		}
		super.height = model.modelHeight;
		model.aBoolean1659 = true;
		if (aBoolean1699) {
			return model;
		}
               if (super.spotAnimId != -1 && super.spotAnimFrame != -1) {
                       SpotAnim spotAnim = SpotAnim.cache[super.spotAnimId];
			Model model_2 = spotAnim.getModel();
			if (model_2 != null) {
                               Model model_3 = new Model(true, AnimFrame.isNullFrame(super.spotAnimFrame), false, model_2);
                               model_3.translate(0, -super.spotAnimHeight, 0);
				model_3.buildVertexGroups();
                               model_3.applyFrame(spotAnim.aAnimation_407.anIntArray353[super.spotAnimFrame]);
				model_3.faceGroups = null;
				model_3.vertexGroups = null;
				if (spotAnim.anInt410 != 128 || spotAnim.anInt411 != 128) {
					model_3.scaleModel(spotAnim.anInt410, spotAnim.anInt410, spotAnim.anInt411);
				}
				model_3.applyLighting(64 + spotAnim.anInt413, 850 + spotAnim.anInt414, -30, -50, -30, true);
				Model aclass30_sub2_sub4_sub6_1s[] = {model, model_3};
				model = new Model(aclass30_sub2_sub4_sub6_1s);
			}
		}
		if (aModel_1714 != null) {
			if (Game.loopCycle >= animationEndCycle) {
				aModel_1714 = null;
			}
			if (Game.loopCycle >= animationStartCycle && Game.loopCycle < animationEndCycle) {
				Model model_1 = aModel_1714;
				model_1.translate(animationBaseX - super.x, animationBaseHeight - animationBaseY, animationBaseZ - super.y);
				if (super.turnDirection == 512) {
					model_1.calculateNormals();
					model_1.calculateNormals();
					model_1.calculateNormals();
				} else if (super.turnDirection == 1024) {
					model_1.calculateNormals();
					model_1.calculateNormals();
				} else if (super.turnDirection == 1536) {
					model_1.calculateNormals();
				}
				Model aclass30_sub2_sub4_sub6s[] = {model, model_1};
				model = new Model(aclass30_sub2_sub4_sub6s);
				if (super.turnDirection == 512) {
					model_1.calculateNormals();
				} else if (super.turnDirection == 1024) {
					model_1.calculateNormals();
					model_1.calculateNormals();
				} else if (super.turnDirection == 1536) {
					model_1.calculateNormals();
					model_1.calculateNormals();
					model_1.calculateNormals();
				}
				model_1.translate(super.x - animationBaseX, animationBaseY - animationBaseHeight, super.y - animationBaseZ);
			}
		}
		model.aBoolean1659 = true;
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
				desc = EntityDef.forID(stream.readUnsignedWord());
				break;
			}
			if (equipment[j] >= 512 && equipment[j] - 512 < ItemDef.totalItems) {
				int l1 = ItemDef.lookup(equipment[j] - 512).team;
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
                        bodyColors[l] = j1;
		}

		super.anInt1511 = stream.readUnsignedWord();
		if (super.anInt1511 == 65535) {
			super.anInt1511 = -1;
		}
		super.anInt1512 = stream.readUnsignedWord();
		if (super.anInt1512 == 65535) {
			super.anInt1512 = -1;
		}
		super.anInt1554 = stream.readUnsignedWord();
		if (super.anInt1554 == 65535) {
			super.anInt1554 = -1;
		}
		super.anInt1555 = stream.readUnsignedWord();
		if (super.anInt1555 == 65535) {
			super.anInt1555 = -1;
		}
		super.anInt1556 = stream.readUnsignedWord();
		if (super.anInt1556 == 65535) {
			super.anInt1556 = -1;
		}
		super.anInt1557 = stream.readUnsignedWord();
		if (super.anInt1557 == 65535) {
			super.anInt1557 = -1;
		}
		super.anInt1505 = stream.readUnsignedWord();
		if (super.anInt1505 == 65535) {
			super.anInt1505 = -1;
		}
		name = TextClass.fixName(TextClass.nameForLong(stream.readQWord()));
		combatLevel = stream.readUnsignedByte();
		skill = stream.readUnsignedWord();
		visible = true;
		appearanceHash = 0L;
		for (int k1 = 0; k1 < 12; k1++) {
			appearanceHash <<= 4;
			if (equipment[k1] >= 256) {
				appearanceHash += equipment[k1] - 256;
			}
		}

		if (equipment[0] >= 256) {
			appearanceHash += equipment[0] - 256 >> 4;
		}
		if (equipment[1] >= 256) {
			appearanceHash += equipment[1] - 256 >> 8;
		}
		for (int i2 = 0; i2 < 5; i2++) {
			appearanceHash <<= 3;
                        appearanceHash += bodyColors[i2];
		}

                appearanceHash <<= 1;
                appearanceHash += gender;
	}

        private Model getBaseModel() {
		if (desc != null) {
			int j = -1;
			if (super.anim >= 0 && super.anInt1529 == 0) {
				j = Animation.anims[super.anim].anIntArray353[super.anInt1527];
			} else if (super.anInt1517 >= 0) {
				j = Animation.anims[super.anInt1517].anIntArray353[super.anInt1518];
			}
                       Model model = desc.getAnimatedModel(-1, j, null);
			return model;
		}
		long l = appearanceHash;
		int k = -1;
		int i1 = -1;
		int j1 = -1;
		int k1 = -1;
		if (super.anim >= 0 && super.anInt1529 == 0) {
			Animation animation = Animation.anims[super.anim];
			k = animation.anIntArray353[super.anInt1527];
			if (super.anInt1517 >= 0 && super.anInt1517 != super.anInt1511) {
				i1 = Animation.anims[super.anInt1517].anIntArray353[super.anInt1518];
			}
			if (animation.anInt360 >= 0) {
				j1 = animation.anInt360;
				l += j1 - equipment[5] << 40;
			}
			if (animation.anInt361 >= 0) {
				k1 = animation.anInt361;
				l += k1 - equipment[3] << 48;
			}
		} else if (super.anInt1517 >= 0) {
			k = Animation.anims[super.anInt1517].anIntArray353[super.anInt1518];
		}
		Model model_1 = (Model) mruNodes.insertFromCache(l);
		if (model_1 == null) {
			boolean flag = false;
			for (int i2 = 0; i2 < 12; i2++) {
				int k2 = equipment[i2];
				if (k1 >= 0 && i2 == 3) {
					k2 = k1;
				}
				if (j1 >= 0 && i2 == 5) {
					k2 = j1;
				}
                                if (k2 >= 256 && k2 < 512 && !IDK.cache[k2 - 256].ready()) {
					flag = true;
				}
                               if (k2 >= 512 && !ItemDef.lookup(k2 - 512).areWearModelsCached(gender)) {
					flag = true;
				}
			}

			if (flag) {
				if (cachedModelHash != -1L) {
					model_1 = (Model) mruNodes.insertFromCache(cachedModelHash);
				}
				if (model_1 == null) {
					return null;
				}
			}
		}
		if (model_1 == null) {
			Model aclass30_sub2_sub4_sub6s[] = new Model[12];
			int j2 = 0;
			for (int l2 = 0; l2 < 12; l2++) {
				int i3 = equipment[l2];
				if (k1 >= 0 && l2 == 3) {
					i3 = k1;
				}
				if (j1 >= 0 && l2 == 5) {
					i3 = j1;
				}
				if (i3 >= 256 && i3 < 512) {
                                        Model model_3 = IDK.cache[i3 - 256].getBodyModel();
					if (model_3 != null) {
						aclass30_sub2_sub4_sub6s[j2++] = model_3;
					}
				}
				if (i3 >= 512) {
                               Model model_4 = ItemDef.lookup(i3 - 512).getWearModel(gender);
					if (model_4 != null) {
						aclass30_sub2_sub4_sub6s[j2++] = model_4;
					}
				}
			}

			model_1 = new Model(j2, aclass30_sub2_sub4_sub6s);
                        for (int j3 = 0; j3 < 5; j3++) {
                                if (bodyColors[j3] != 0) {
                                        model_1.recolor(Game.anIntArrayArray1003[j3][0], Game.anIntArrayArray1003[j3][bodyColors[j3]]);
                                        if (j3 == 1) {
                                                model_1.recolor(Game.anIntArray1204[0], Game.anIntArray1204[bodyColors[j3]]);
					}
				}
			}

			model_1.buildVertexGroups();
			model_1.applyLighting(64, 850, -30, -50, -30, true);
			mruNodes.removeFromCache(model_1, l);
			cachedModelHash = l;
		}
		if (aBoolean1699) {
			return model_1;
		}
                Model model_2 = Model.aModel_1621;
                model_2.method464(model_1, AnimFrame.isNullFrame(k) & AnimFrame.isNullFrame(i1));
		if (k != -1 && i1 != -1) {
			model_2.applyFrames(Animation.anims[super.anim].anIntArray357, i1, k);
		} else if (k != -1) {
			model_2.applyFrame(k);
		}
		model_2.calculateBounds();
		model_2.faceGroups = null;
		model_2.vertexGroups = null;
		return model_2;
	}

	@Override
	public boolean isVisible() {
		return visible;
	}

	public int privelage;
        public Model getDialogueModel() {
		if (!visible) {
			return null;
		}
               if (desc != null) {
                       return desc.getModel();
		}
		boolean flag = false;
		for (int i = 0; i < 12; i++) {
			int j = equipment[i];
                        if (j >= 256 && j < 512 && !IDK.cache[j - 256].headLoaded()) {
				flag = true;
			}
                       if (j >= 512 && !ItemDef.lookup(j - 512).areDialogueModelsCached(gender)) {
				flag = true;
			}
		}

		if (flag) {
			return null;
		}
		Model aclass30_sub2_sub4_sub6s[] = new Model[12];
		int k = 0;
		for (int l = 0; l < 12; l++) {
			int i1 = equipment[l];
			if (i1 >= 256 && i1 < 512) {
                                Model model_1 = IDK.cache[i1 - 256].getHeadModel();
				if (model_1 != null) {
					aclass30_sub2_sub4_sub6s[k++] = model_1;
				}
			}
			if (i1 >= 512) {
                               Model model_2 = ItemDef.lookup(i1 - 512).getDialogueModel(gender);
				if (model_2 != null) {
					aclass30_sub2_sub4_sub6s[k++] = model_2;
				}
			}
		}

		Model model = new Model(k, aclass30_sub2_sub4_sub6s);
                for (int j1 = 0; j1 < 5; j1++) {
                        if (bodyColors[j1] != 0) {
                                model.recolor(Game.anIntArrayArray1003[j1][0], Game.anIntArrayArray1003[j1][bodyColors[j1]]);
                                if (j1 == 1) {
                                        model.recolor(Game.anIntArray1204[0], Game.anIntArray1204[bodyColors[j1]]);
				}
			}
		}

		return model;
	}

	Player() {
		cachedModelHash = -1L;
		aBoolean1699 = false;
                bodyColors = new int[5];
		visible = false;
		equipment = new int[12];
	}

        private long cachedModelHash;
	public EntityDef desc;
	boolean aBoolean1699;
        final int[] bodyColors;
	public int team;
        private int gender;
	public String name;
	static MRUNodes mruNodes = new MRUNodes(260);
	public int combatLevel;
	public int headIcon;
	public int skullIcon;
	public int hintIcon;
        public int animationStartCycle;
        int animationEndCycle;
        int animationBaseY;
	boolean visible;
        int animationBaseX;
        int animationBaseHeight;
        int animationBaseZ;
	Model aModel_1714;
	public final int[] equipment;
        private long appearanceHash;
	int anInt1719;
	int anInt1720;
	int anInt1721;
	int anInt1722;
	int skill;

}
