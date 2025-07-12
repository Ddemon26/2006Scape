// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

public class Entity extends Animable {

	public final void setPos(int i, int j, boolean flag) {
		if (anim != -1 && Animation.anims[anim].precedenceWalking == 1) {
			anim = -1;
		}
		if (!flag) {
			int k = i - smallX[0];
			int l = j - smallY[0];
			if (k >= -8 && k <= 8 && l >= -8 && l <= 8) {
				if (smallXYIndex < 9) {
					smallXYIndex++;
				}
				for (int i1 = smallXYIndex; i1 > 0; i1--) {
					smallX[i1] = smallX[i1 - 1];
					smallY[i1] = smallY[i1 - 1];
					aBooleanArray1553[i1] = aBooleanArray1553[i1 - 1];
				}

				smallX[0] = i;
				smallY[0] = j;
				aBooleanArray1553[0] = false;
				return;
			}
		}
		smallXYIndex = 0;
		animationDelay = 0;
		movementDelay = 0;
		smallX[0] = i;
		smallY[0] = j;
		x = smallX[0] * 128 + size * 64;
		y = smallY[0] * 128 + size * 64;
	}

       /** Clears the entity's movement queue. */
       public final void clearMovement() {
               smallXYIndex = 0;
               animationDelay = 0;
       }

	public final void updateHitData(int j, int k, int l) {
		for (int i1 = 0; i1 < 4; i1++) {
			if (hitsLoopCycle[i1] <= l) {
				hitArray[i1] = k;
				hitMarkTypes[i1] = j;
				hitsLoopCycle[i1] = l + 70;
				return;
			}
		}
	}

	public final void moveInDir(boolean flag, int i) {
		int j = smallX[0];
		int k = smallY[0];
		if (i == 0) {
			j--;
			k++;
		}
		if (i == 1) {
			k++;
		}
		if (i == 2) {
			j++;
			k++;
		}
		if (i == 3) {
			j--;
		}
		if (i == 4) {
			j++;
		}
		if (i == 5) {
			j--;
			k--;
		}
		if (i == 6) {
			k--;
		}
		if (i == 7) {
			j++;
			k--;
		}
		if (anim != -1 && Animation.anims[anim].precedenceWalking == 1) {
			anim = -1;
		}
		if (smallXYIndex < 9) {
			smallXYIndex++;
		}
		for (int l = smallXYIndex; l > 0; l--) {
			smallX[l] = smallX[l - 1];
			smallY[l] = smallY[l - 1];
			aBooleanArray1553[l] = aBooleanArray1553[l - 1];
		}
		smallX[0] = j;
		smallY[0] = k;
		aBooleanArray1553[0] = flag;
	}

	public int entScreenX;
	public int entScreenY;
	public final int index = -1;
	public boolean isVisible() {
		return false;
	}

	Entity() {
		smallX = new int[10];
		smallY = new int[10];
		interactingEntity = -1;
               turnSpeed = 32;
		runAnimation = -1;
		height = 200;
		standAnimation = -1;
		turnAnimation = -1;
		hitArray = new int[4];
		hitMarkTypes = new int[4];
		hitsLoopCycle = new int[4];
		currentAnimation = -1;
               spotAnimId = -1;
		anim = -1;
		loopCycleStatus = -1000;
		textCycle = 100;
		size = 1;
		aBoolean1541 = false;
		aBooleanArray1553 = new boolean[10];
		walkAnimation = -1;
		turn180Animation = -1;
		turn90CWAnimation = -1;
		turn90CCWAnimation = -1;
	}

	public final int[] smallX;
	public final int[] smallY;
	public int interactingEntity;
	int movementDelay;
       int turnSpeed;
	int runAnimation;
	public String textSpoken;
	public int height;
	public int turnDirection;
	int standAnimation;
	int turnAnimation;
	int chatColor;
	final int[] hitArray;
	final int[] hitMarkTypes;
	final int[] hitsLoopCycle;
	int currentAnimation;
	int animationFrame;
	int animationFrameCycle;
       int spotAnimId;
       int spotAnimFrame;
       int spotAnimFrameCycle;
       int spotAnimStartTick;
       int spotAnimHeight;
	int smallXYIndex;
	public int anim;
	int graphicFrame;
	int graphicFrameCycle;
	int graphicDelay;
	int graphicCycle;
	int chatEffect;
	public int loopCycleStatus;
	public int currentHealth;
	public int maxHealth;
	int textCycle;
	int lastUpdateCycle;
	int focusX;
	int focusY;
	int size;
	boolean aBoolean1541;
	int animationDelay;
	int forceMoveStartX;
	int forceMoveEndX;
	int forceMoveStartY;
	int forceMoveEndY;
	int forceMoveStartCycle;
	int forceMoveEndCycle;
	int forceMoveDirection;
	public int x;
	public int y;
       int currentHeading;
	final boolean[] aBooleanArray1553;
	int walkAnimation;
	int turn180Animation;
	int turn90CWAnimation;
	int turn90CCWAnimation;
}
