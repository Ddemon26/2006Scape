// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

public final class NPC extends Entity {

	private Model method450() {
		if (super.anim >= 0 && super.anInt1529 == 0) {
			int k = Animation.anims[super.anim].frameIds[super.anInt1527];
			int i1 = -1;
			if (super.movementAnimationId >= 0 && super.movementAnimationId != super.idleAnimationId) {
				i1 = Animation.anims[super.movementAnimationId].frameIds[super.anInt1518];
			}
			return desc.getAnimatedModel(i1, k, Animation.anims[super.anim].interleavedTransformIndices);
		}
		int l = -1;
		if (super.movementAnimationId >= 0) {
			l = Animation.anims[super.movementAnimationId].frameIds[super.anInt1518];
		}
		return desc.getAnimatedModel(-1, l, null);
	}

	@Override
	public Model getRotatedModel() {
		if (desc == null) {
			return null;
		}
		Model model = method450();
		if (model == null) {
			return null;
		}
		super.height = model.modelHeight;
		if (super.anInt1520 != -1 && super.anInt1521 != -1) {
			SpotAnim spotAnim = SpotAnim.cache[super.anInt1520];
			Model model_1 = spotAnim.getModel();
			if (model_1 != null) {
				int j = spotAnim.animation.frameIds[super.anInt1521];
				Model model_2 = new Model(true, AnimationFrame.isMissingFrameId(j), false, model_1);
				model_2.translate(0, -super.anInt1524, 0);
				model_2.buildAnimationGroups();
				model_2.applyAnimationFrame(j);
				model_2.faceGroups = null;
				model_2.vertexGroups = null;
				if (spotAnim.horizontalScale128 != 128 || spotAnim.verticalScale128 != 128) {
					model_2.scale(spotAnim.horizontalScale128, spotAnim.horizontalScale128, spotAnim.verticalScale128);
				}
				model_2.applyLighting(64 + spotAnim.ambientLightOffset, 850 + spotAnim.lightDivisorOffset, -30, -50, -30, true);
				Model aModel[] = {model, model_2};
				model = new Model(aModel);
			}
		}
		if (desc.tileSize == 1) {
			model.pickByBounds = true;
		}
		return model;
	}

	@Override
	public boolean isVisible() {
		return desc != null;
	}

	NPC() {
	}

	public EntityDef desc;
}
