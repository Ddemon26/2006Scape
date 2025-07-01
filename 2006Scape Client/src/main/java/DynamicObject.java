// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

final class DynamicObject extends Animable {

	@Override
	public Model getRotatedModel() {
		int j = -1;
		if (animation != null) {
			int k = Game.loopCycle - cycleStart;
			if (k > 100 && animation.anInt356 > 0) {
				k = 100;
			}
			while (k > animation.method258(currentFrame)) {
				k -= animation.method258(currentFrame);
				currentFrame++;
				if (currentFrame < animation.anInt352) {
					continue;
				}
				currentFrame -= animation.anInt356;
				if (currentFrame >= 0 && currentFrame < animation.anInt352) {
					continue;
				}
				animation = null;
				break;
			}
			cycleStart = Game.loopCycle - k;
			if (animation != null) {
				j = animation.anIntArray353[currentFrame];
			}
		}
		ObjectDef class46;
		if (childIDs != null) {
			class46 = getChildDefinition();
		} else {
			class46 = ObjectDef.forID(id);
		}
		if (class46 == null) {
			return null;
		} else {
			return class46.getModel(type, orientation, tileHeight, tileHeight1, tileHeight2, tileHeight3, j);
		}
	}

	private ObjectDef getChildDefinition() {
		int i = -1;
		if (varbitId != -1) {
			VarBit varBit = VarBit.cache[varbitId];
			int k = varBit.anInt648;
			int l = varBit.anInt649;
			int i1 = varBit.anInt650;
			int j1 = Game.anIntArray1232[i1 - l];
			i = client.variousSettings[k] >> l & j1;
		} else if (varpId != -1) {
			i = client.variousSettings[varpId];
		}
		if (i < 0 || i >= childIDs.length || childIDs[i] == -1) {
			return null;
		} else {
			return ObjectDef.forID(childIDs[i]);
		}
	}

	public DynamicObject(int i, int j, int k, int l, int i1, int j1, int k1, int l1, boolean flag) {
		id = i;
		type = k;
		orientation = j;
		tileHeight = j1;
		tileHeight1 = l;
		tileHeight2 = i1;
		tileHeight3 = k1;
		if (l1 != -1) {
			animation = Animation.anims[l1];
			currentFrame = 0;
			cycleStart = Game.loopCycle;
			if (flag && animation.anInt356 != -1) {
				currentFrame = (int) (Math.random() * animation.anInt352);
				cycleStart -= (int) (Math.random() * animation.method258(currentFrame));
			}
		}
		ObjectDef class46 = ObjectDef.forID(id);
		varbitId = class46.anInt774;
		varpId = class46.anInt749;
		childIDs = class46.childrenIDs;
	}

	private int currentFrame;
	private final int[] childIDs;
	private final int varbitId;
	private final int varpId;
	private final int tileHeight;
	private final int tileHeight1;
	private final int tileHeight2;
	private final int tileHeight3;
	private Animation animation;
	private int cycleStart;
	public static Game client;
	private final int id;
	private final int type;
	private final int orientation;
}
