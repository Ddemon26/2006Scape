// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

final class AnimatedObject extends Animable {

	@Override
	public Model getRotatedModel() {
		int frameId = -1;
		if (animation != null) {
			int cyclesIntoFrame = Game.loopCycle - frameStartCycle;
			if (cyclesIntoFrame > 100 && animation.loopBackFrames > 0) {
				cyclesIntoFrame = 100;
			}
			while (cyclesIntoFrame > animation.getFrameDelay(frameIndex)) {
				cyclesIntoFrame -= animation.getFrameDelay(frameIndex);
				frameIndex++;
				if (frameIndex < animation.frameCount) {
					continue;
				}
				frameIndex -= animation.loopBackFrames;
				if (frameIndex >= 0 && frameIndex < animation.frameCount) {
					continue;
				}
				animation = null;
				break;
			}
			frameStartCycle = Game.loopCycle - cyclesIntoFrame;
			if (animation != null) {
				frameId = animation.frameIds[frameIndex];
			}
		}
		ObjectDef objectDef;
		if (childObjectIds != null) {
			objectDef = resolveChildDefinition();
		} else {
			objectDef = ObjectDef.forID(objectId);
		}
		if (objectDef == null) {
			return null;
		} else {
			return objectDef.getModel(type, orientation, heightX0Y0, heightX1Y0, heightX1Y1, heightX0Y1, frameId);
		}
	}

	private ObjectDef resolveChildDefinition() {
		int childIndex = -1;
		if (varBitId != -1) {
			VarBit varBit = VarBit.cache[varBitId];
			int settingIndex = varBit.anInt648;
			int lowBit = varBit.anInt649;
			int highBit = varBit.anInt650;
			int mask = Game.anIntArray1232[highBit - lowBit];
			childIndex = clientInstance.variousSettings[settingIndex] >> lowBit & mask;
		} else if (settingId != -1) {
			childIndex = clientInstance.variousSettings[settingId];
		}
		if (childIndex < 0 || childIndex >= childObjectIds.length || childObjectIds[childIndex] == -1) {
			return null;
		} else {
			return ObjectDef.forID(childObjectIds[childIndex]);
		}
	}

	public AnimatedObject(int objectId, int orientation, int type, int heightX1Y0, int heightX1Y1, int heightX0Y0, int heightX0Y1, int animationId, boolean randomizeStart) {
		this.objectId = objectId;
		this.type = type;
		this.orientation = orientation;
		this.heightX0Y0 = heightX0Y0;
		this.heightX1Y0 = heightX1Y0;
		this.heightX1Y1 = heightX1Y1;
		this.heightX0Y1 = heightX0Y1;
		if (animationId != -1) {
			animation = Animation.anims[animationId];
			frameIndex = 0;
			frameStartCycle = Game.loopCycle;
			if (randomizeStart && animation.loopBackFrames != -1) {
				frameIndex = (int) (Math.random() * animation.frameCount);
				frameStartCycle -= (int) (Math.random() * animation.getFrameDelay(frameIndex));
			}
		}
		ObjectDef objectDef = ObjectDef.forID(objectId);
		varBitId = objectDef.varBitId;
		settingId = objectDef.settingId;
		childObjectIds = objectDef.childrenIDs;
	}

	private int frameIndex;
	private final int[] childObjectIds;
	private final int varBitId;
	private final int settingId;
	private final int heightX0Y0;
	private final int heightX1Y0;
	private final int heightX1Y1;
	private final int heightX0Y1;
	private Animation animation;
	private int frameStartCycle;
	public static Game clientInstance;
	private final int objectId;
	private final int type;
	private final int orientation;
}
