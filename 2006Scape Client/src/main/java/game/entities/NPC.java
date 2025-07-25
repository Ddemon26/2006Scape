package game.entities;

import game.animation.AnimFrame;
import game.animation.Animation;
import game.animation.SpotAnim;
import game.definitions.EntityDef;
import render.geometry.Model;

// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

public final class NPC extends Entity {

       private Model getBaseModel() {
		if (super.anim >= 0 && super.graphicDelay == 0) {
			int k = Animation.anims[super.anim].frameIds[super.graphicFrame];
			int i1 = -1;
			if (super.currentAnimation >= 0 && super.currentAnimation != super.standAnimation) {
				i1 = Animation.anims[super.currentAnimation].frameIds[super.animationFrame];
			}
                       return definition.getAnimatedModel(i1, k, Animation.anims[super.anim].interleaveOrder);
		}
		int l = -1;
		if (super.currentAnimation >= 0) {
			l = Animation.anims[super.currentAnimation].frameIds[super.animationFrame];
		}
               return definition.getAnimatedModel(-1, l, null);
	}

	@Override
	public Model getRotatedModel() {
               if (definition == null) {
			return null;
		}
               Model model = getBaseModel();
		if (model == null) {
			return null;
		}
		super.height = model.modelHeight;
               if (super.spotAnimId != -1 && super.spotAnimFrame != -1) {
                       SpotAnim spotAnim = SpotAnim.cache[super.spotAnimId];
			Model model_1 = spotAnim.getModel();
			if (model_1 != null) {
                       int j = spotAnim.animation.frameIds[super.spotAnimFrame];
                               Model model_2 = new Model(true, AnimFrame.isNullFrame(j), false, model_1);
                               model_2.translate(0, -super.spotAnimHeight, 0);
				model_2.buildVertexGroups();
				model_2.applyFrame(j);
				model_2.faceGroups = null;
				model_2.vertexGroups = null;
                                if (spotAnim.scaleX != 128 || spotAnim.scaleY != 128) {
                                        model_2.scaleModel(spotAnim.scaleX, spotAnim.scaleX, spotAnim.scaleY);
                                }
                                model_2.applyLighting(64 + spotAnim.ambient, 850 + spotAnim.contrast, -30, -50, -30, true);
				Model aModel[] = {model, model_2};
				model = new Model(aModel);
			}
		}
               if (definition.size == 1) {
                        model.pickable = true;
                }
		return model;
	}

	@Override
	public boolean isVisible() {
               return definition != null;
	}

	public NPC() {
	}

       public EntityDef definition;
}
