package render.objects;

import core.engine.Game;
import game.entities.Animable;
import game.animation.Animation;
import game.definitions.ObjectDef;
import render.geometry.Model;
import util.configuration.VarBit;

// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

public final class DynamicObject extends Animable {

	@Override
	public Model getRotatedModel() {
		int j = -1;
		if (animation != null) {
			int k = Game.loopCycle - cycleStart;
			if (k > 100 && animation.frameStep > 0) {
				k = 100;
			}
                       while (k > animation.getFrameDelay(currentFrame)) {
                               k -= animation.getFrameDelay(currentFrame);
				currentFrame++;
				if (currentFrame < animation.frameCount) {
					continue;
				}
				currentFrame -= animation.frameStep;
				if (currentFrame >= 0 && currentFrame < animation.frameCount) {
					continue;
				}
				animation = null;
				break;
			}
			cycleStart = Game.loopCycle - k;
			if (animation != null) {
				j = animation.frameIds[currentFrame];
			}
		}
                ObjectDef objectDef;
                if (childIDs != null) {
                        objectDef = getChildDefinition();
                } else {
                        objectDef = ObjectDef.forID(id);
                }
                if (objectDef == null) {
                        return null;
                } else {
                        return objectDef.getModel(type, orientation, tileHeight, tileHeight1, tileHeight2, tileHeight3, j);
                }
	}

	private ObjectDef getChildDefinition() {
		int i = -1;
		if (varbitId != -1) {
			VarBit varBit = VarBit.cache[varbitId];
                        int k = varBit.configId;
                        int l = varBit.leastSignificantBit;
                        int i1 = varBit.mostSignificantBit;
			int j1 = Game.bitMasks[i1 - l];
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
			if (flag && animation.frameStep != -1) {
				currentFrame = (int) (Math.random() * animation.frameCount);
                                cycleStart -= (int) (Math.random() * animation.getFrameDelay(currentFrame));
			}
		}
                ObjectDef objectDef = ObjectDef.forID(id);
                varbitId = objectDef.varbitId;
                varpId = objectDef.varpId;
                childIDs = objectDef.childrenIDs;
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
