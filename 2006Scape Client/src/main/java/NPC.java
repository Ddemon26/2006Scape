// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

public final class NPC extends Entity {

       private Model getBaseModel() {
		if (super.anim >= 0 && super.anInt1529 == 0) {
			int k = Animation.anims[super.anim].anIntArray353[super.anInt1527];
			int i1 = -1;
			if (super.anInt1517 >= 0 && super.anInt1517 != super.anInt1511) {
				i1 = Animation.anims[super.anInt1517].anIntArray353[super.anInt1518];
			}
                       return definition.getAnimatedModel(i1, k, Animation.anims[super.anim].anIntArray357);
		}
		int l = -1;
		if (super.anInt1517 >= 0) {
			l = Animation.anims[super.anInt1517].anIntArray353[super.anInt1518];
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
                       int j = spotAnim.animation.anIntArray353[super.spotAnimFrame];
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
               if (definition.aByte68 == 1) {
			model.aBoolean1659 = true;
		}
		return model;
	}

	@Override
	public boolean isVisible() {
               return definition != null;
	}

	NPC() {
	}

       public EntityDef definition;
}
