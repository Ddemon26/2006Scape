// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

public class Entity extends Animable {

	public final void setPos(int tileX, int tileY, boolean teleport) {
		if (anim != -1 && Animation.anims[anim].idlePrecedence == 1) {
			anim = -1;
		}
		if (!teleport) {
			int deltaX = tileX - smallX[0];
			int deltaY = tileY - smallY[0];
			if (deltaX >= -8 && deltaX <= 8 && deltaY >= -8 && deltaY <= 8) {
				if (smallXYIndex < 9) {
					smallXYIndex++;
				}
				for (int queueIndex = smallXYIndex; queueIndex > 0; queueIndex--) {
					smallX[queueIndex] = smallX[queueIndex - 1];
					smallY[queueIndex] = smallY[queueIndex - 1];
					pathRunFlags[queueIndex] = pathRunFlags[queueIndex - 1];
				}

				smallX[0] = tileX;
				smallY[0] = tileY;
				pathRunFlags[0] = false;
				return;
			}
		}
		smallXYIndex = 0;
		stepsQueuedBeforeAnimation = 0;
		deferredMovementTicks = 0;
		smallX[0] = tileX;
		smallY[0] = tileY;
		x = smallX[0] * 128 + tileSize * 64;
		y = smallY[0] * 128 + tileSize * 64;
	}

	public final void resetPath() {
		smallXYIndex = 0;
		stepsQueuedBeforeAnimation = 0;
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

	public final void moveInDir(boolean run, int direction) {
		int nextX = smallX[0];
		int nextY = smallY[0];
		if (direction == 0) {
			nextX--;
			nextY++;
		}
		if (direction == 1) {
			nextY++;
		}
		if (direction == 2) {
			nextX++;
			nextY++;
		}
		if (direction == 3) {
			nextX--;
		}
		if (direction == 4) {
			nextX++;
		}
		if (direction == 5) {
			nextX--;
			nextY--;
		}
		if (direction == 6) {
			nextY--;
		}
		if (direction == 7) {
			nextX++;
			nextY--;
		}
		if (anim != -1 && Animation.anims[anim].idlePrecedence == 1) {
			anim = -1;
		}
		if (smallXYIndex < 9) {
			smallXYIndex++;
		}
		for (int queueIndex = smallXYIndex; queueIndex > 0; queueIndex--) {
			smallX[queueIndex] = smallX[queueIndex - 1];
			smallY[queueIndex] = smallY[queueIndex - 1];
			pathRunFlags[queueIndex] = pathRunFlags[queueIndex - 1];
		}
		smallX[0] = nextX;
		smallY[0] = nextY;
		pathRunFlags[0] = run;
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
		runAnimationId = -1;
		height = 200;
		idleAnimationId = -1;
		turnInPlaceAnimationId = -1;
		hitArray = new int[4];
		hitMarkTypes = new int[4];
		hitsLoopCycle = new int[4];
		movementAnimationId = -1;
		spotAnimId = -1;
		anim = -1;
		loopCycleStatus = -1000;
		textCycle = 100;
		tileSize = 1;
		animExtendByYaw = false;
		pathRunFlags = new boolean[10];
		walkAnimationId = -1;
		reverseWalkAnimationId = -1;
		sidestepNegativeAnimationId = -1;
		sidestepPositiveAnimationId = -1;
	}

	public final int[] smallX;
	public final int[] smallY;
	public int interactingEntity;
	int deferredMovementTicks;
	int turnSpeed;
	int runAnimationId;
	public String textSpoken;
	public int height;
	public int turnDirection;
	int idleAnimationId;
	int turnInPlaceAnimationId;
	int overheadChatColorCode;
	final int[] hitArray;
	final int[] hitMarkTypes;
	final int[] hitsLoopCycle;
	int movementAnimationId;
	int movementFrame;
	int movementFrameTimer;
	int spotAnimId;
	int spotFrame;
	int spotFrameTimer;
	int spotStartCycle;
	int spotHeight;
	int smallXYIndex;
	public int anim;
	int animFrame;
	int animFrameTimer;
	int animationDelay;
	int animLoopCount;
	int overheadChatEffectCode;
	public int loopCycleStatus;
	public int currentHealth;
	public int maxHealth;
	int textCycle;
	int lastUpdateCycle;
	int faceHalfTileX;
	int faceHalfTileY;
	int tileSize;
	boolean animExtendByYaw;
	int stepsQueuedBeforeAnimation;
	int forceMoveStartX;
	int forceMoveEndX;
	int forceMoveStartY;
	int forceMoveEndY;
	int forceMoveStartCycle;
	int forceMoveEndCycle;
	int forceMoveDirection;
	public int x;
	public int y;
	int facingDirection;
	final boolean[] pathRunFlags;
	int walkAnimationId;
	int reverseWalkAnimationId;
	int sidestepNegativeAnimationId;
	int sidestepPositiveAnimationId;
}
