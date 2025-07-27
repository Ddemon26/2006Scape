package game.entities;// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

import core.engine.Game;
import core.network.Stream;
import core.renderers.TextClass;
import game.animation.AnimFrame;
import game.animation.Animation;
import game.definitions.EntityDef;
import game.definitions.IDK;
import game.definitions.ItemDef;
import game.definitions.SpotAnim;
import render.geometry.Model;
import util.collections.MRUCache;

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
		model.pickable = true;
		if (skipAnimations) {
			return model;
		}
               if (super.spotAnimId != -1 && super.spotAnimFrame != -1) {
                       SpotAnim spotAnim = SpotAnim.cache[super.spotAnimId];
			Model model_2 = spotAnim.getModel();
			if (model_2 != null) {
                               Model model_3 = new Model(true, AnimFrame.isNullFrame(super.spotAnimFrame), false, model_2);
                               model_3.translate(0, -super.spotAnimHeight, 0);
				model_3.buildVertexGroups();
                               model_3.applyFrame(spotAnim.animation.frameIds[super.spotAnimFrame]);
				model_3.faceGroups = null;
				model_3.vertexGroups = null;
                                if (spotAnim.scaleX != 128 || spotAnim.scaleY != 128) {
                                        model_3.scaleModel(spotAnim.scaleX, spotAnim.scaleX, spotAnim.scaleY);
                                }
                                model_3.applyLighting(64 + spotAnim.ambient, 850 + spotAnim.contrast, -30, -50, -30, true);
                                Model combinedModels[] = {model, model_3};
                                model = new Model(combinedModels);
			}
		}
                if (overlayModel != null) {
                        if (Game.loopCycle >= animationEndCycle) {
                                overlayModel = null;
                        }
                        if (Game.loopCycle >= animationStartCycle && Game.loopCycle < animationEndCycle) {
                                Model model_1 = overlayModel;
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
                                Model combinedModels[] = {model, model_1};
                                model = new Model(combinedModels);
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
		model.pickable = true;
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
			if (j1 < 0 || j1 >= Game.appearanceColorOptions[l].length) {
				j1 = 0;
			}
                        bodyColors[l] = j1;
		}

		super.standAnimation = stream.readUnsignedWord();
		if (super.standAnimation == 65535) {
			super.standAnimation = -1;
		}
		super.turnAnimation = stream.readUnsignedWord();
		if (super.turnAnimation == 65535) {
			super.turnAnimation = -1;
		}
		super.walkAnimation = stream.readUnsignedWord();
		if (super.walkAnimation == 65535) {
			super.walkAnimation = -1;
		}
		super.turn180Animation = stream.readUnsignedWord();
		if (super.turn180Animation == 65535) {
			super.turn180Animation = -1;
		}
		super.turn90CWAnimation = stream.readUnsignedWord();
		if (super.turn90CWAnimation == 65535) {
			super.turn90CWAnimation = -1;
		}
		super.turn90CCWAnimation = stream.readUnsignedWord();
		if (super.turn90CCWAnimation == 65535) {
			super.turn90CCWAnimation = -1;
		}
		super.runAnimation = stream.readUnsignedWord();
		if (super.runAnimation == 65535) {
			super.runAnimation = -1;
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
			if (super.anim >= 0 && super.graphicDelay == 0) {
				j = Animation.anims[super.anim].frameIds[super.graphicFrame];
			} else if (super.currentAnimation >= 0) {
				j = Animation.anims[super.currentAnimation].frameIds[super.animationFrame];
			}
                       Model model = desc.getAnimatedModel(-1, j, null);
			return model;
		}
		long l = appearanceHash;
		int k = -1;
		int i1 = -1;
		int j1 = -1;
		int k1 = -1;
		if (super.anim >= 0 && super.graphicDelay == 0) {
			Animation animation = Animation.anims[super.anim];
			k = animation.frameIds[super.graphicFrame];
			if (super.currentAnimation >= 0 && super.currentAnimation != super.standAnimation) {
				i1 = Animation.anims[super.currentAnimation].frameIds[super.animationFrame];
			}
			if (animation.leftHandItem >= 0) {
				j1 = animation.leftHandItem;
				l += j1 - equipment[5] << 40;
			}
			if (animation.rightHandItem >= 0) {
				k1 = animation.rightHandItem;
				l += k1 - equipment[3] << 48;
			}
		} else if (super.currentAnimation >= 0) {
			k = Animation.anims[super.currentAnimation].frameIds[super.animationFrame];
		}
            Model model_1 = (Model) mruNodes.get(l);
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
                                    model_1 = (Model) mruNodes.get(cachedModelHash);
				}
				if (model_1 == null) {
					return null;
				}
			}
		}
		if (model_1 == null) {
                        Model modelParts[] = new Model[12];
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
                                                modelParts[j2++] = model_3;
					}
				}
				if (i3 >= 512) {
                               Model model_4 = ItemDef.lookup(i3 - 512).getWearModel(gender);
					if (model_4 != null) {
                                                modelParts[j2++] = model_4;
					}
				}
			}

                        model_1 = new Model(j2, modelParts);
                        for (int j3 = 0; j3 < 5; j3++) {
                                if (bodyColors[j3] != 0) {
                                        model_1.recolor(Game.appearanceColorOptions[j3][0], Game.appearanceColorOptions[j3][bodyColors[j3]]);
                                        if (j3 == 1) {
                                                model_1.recolor(Game.additionalColorCodes[0], Game.additionalColorCodes[bodyColors[j3]]);
					}
				}
			}

			model_1.buildVertexGroups();
			model_1.applyLighting(64, 850, -30, -50, -30, true);
                    mruNodes.put(model_1, l);
			cachedModelHash = l;
		}
		if (skipAnimations) {
			return model_1;
		}
                Model model_2 = Model.placeholderModel;
                model_2.copyFromModel(model_1, AnimFrame.isNullFrame(k) & AnimFrame.isNullFrame(i1));
		if (k != -1 && i1 != -1) {
			model_2.applyFrames(Animation.anims[super.anim].interleaveOrder, i1, k);
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
                Model modelParts[] = new Model[12];
		int k = 0;
		for (int l = 0; l < 12; l++) {
			int i1 = equipment[l];
			if (i1 >= 256 && i1 < 512) {
                                Model model_1 = IDK.cache[i1 - 256].getHeadModel();
				if (model_1 != null) {
                                        modelParts[k++] = model_1;
				}
			}
			if (i1 >= 512) {
                               Model model_2 = ItemDef.lookup(i1 - 512).getDialogueModel(gender);
				if (model_2 != null) {
                                        modelParts[k++] = model_2;
				}
			}
		}

                Model model = new Model(k, modelParts);
                for (int j1 = 0; j1 < 5; j1++) {
                        if (bodyColors[j1] != 0) {
                                model.recolor(Game.appearanceColorOptions[j1][0], Game.appearanceColorOptions[j1][bodyColors[j1]]);
                                if (j1 == 1) {
                                        model.recolor(Game.additionalColorCodes[0], Game.additionalColorCodes[bodyColors[j1]]);
				}
			}
		}

		return model;
	}

	public Player() {
		cachedModelHash = -1L;
                skipAnimations = false;
                bodyColors = new int[5];
		visible = false;
		equipment = new int[12];
	}

        private long cachedModelHash;
	public EntityDef desc;
        public boolean skipAnimations;
        public final int[] bodyColors;
	public int team;
        private int gender;
	public String name;
        public static MRUCache mruNodes = new MRUCache(260);
	public int combatLevel;
	public int headIcon;
	public int skullIcon;
	public int hintIcon;
        public int animationStartCycle;
        public int animationEndCycle;
        public int animationBaseY;
	public boolean visible;
        public int animationBaseX;
        public int animationBaseHeight;
        public int animationBaseZ;
        public Model overlayModel;
	public final int[] equipment;
        private long appearanceHash;
        public int boundingBoxMinX;
        public int boundingBoxMinY;
        public int boundingBoxMaxX;
        public int boundingBoxMaxY;
	public int skill;

}
