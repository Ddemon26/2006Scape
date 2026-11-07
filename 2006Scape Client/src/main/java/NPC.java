// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

public final class NPC extends Entity {

	private Model buildAnimatedModel() {
		if (super.anim >= 0 && super.anInt1529 == 0) {
			int primaryFrameId = Animation.anims[super.anim].frameIds[super.anInt1527];
			int secondaryFrameId = -1;
			if (super.movementAnimationId >= 0 && super.movementAnimationId != super.idleAnimationId) {
				secondaryFrameId = Animation.anims[super.movementAnimationId].frameIds[super.anInt1518];
			}
			return desc.getAnimatedModel(secondaryFrameId, primaryFrameId, Animation.anims[super.anim].interleavedTransformIndices);
		}
		int movementFrameId = -1;
		if (super.movementAnimationId >= 0) {
			movementFrameId = Animation.anims[super.movementAnimationId].frameIds[super.anInt1518];
		}
		return desc.getAnimatedModel(-1, movementFrameId, null);
	}

	@Override
	public Model getRotatedModel() {
		if (desc == null) {
			return null;
		}
		Model model = buildAnimatedModel();
		if (model == null) {
			return null;
		}
		super.height = model.modelHeight;
		if (super.anInt1520 != -1 && super.anInt1521 != -1) {
			SpotAnim spotAnim = SpotAnim.cache[super.anInt1520];
			Model spotModel = spotAnim.getModel();
			if (spotModel != null) {
				int spotFrameId = spotAnim.animation.frameIds[super.anInt1521];
				Model spotCopy = new Model(true, AnimationFrame.isMissingFrameId(spotFrameId), false, spotModel);
				spotCopy.translate(0, -super.anInt1524, 0);
				spotCopy.buildAnimationGroups();
				spotCopy.applyAnimationFrame(spotFrameId);
				spotCopy.faceGroups = null;
				spotCopy.vertexGroups = null;
				if (spotAnim.horizontalScale128 != 128 || spotAnim.verticalScale128 != 128) {
					spotCopy.scale(spotAnim.horizontalScale128, spotAnim.horizontalScale128, spotAnim.verticalScale128);
				}
				spotCopy.applyLighting(64 + spotAnim.ambientLightOffset, 850 + spotAnim.lightDivisorOffset, -30, -50, -30, true);
				Model combinedParts[] = {model, spotCopy};
				model = new Model(combinedParts);
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
