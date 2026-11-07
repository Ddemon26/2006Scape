// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

final class Projectile extends Animable {

	public void retarget(int currentCycle, int targetY, int targetHeight, int targetX) {
		if (!inFlight) {
			double deltaX = targetX - originX;
			double deltaY = targetY - originY;
			double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
			positionX = originX + deltaX * startOffset / distance;
			positionY = originY + deltaY * startOffset / distance;
			positionHeight = originHeight;
		}
		double remainingCycles = endCycle + 1 - currentCycle;
		velocityX = (targetX - positionX) / remainingCycles;
		velocityY = (targetY - positionY) / remainingCycles;
		horizontalSpeed = Math.sqrt(velocityX * velocityX + velocityY * velocityY);
		if (!inFlight) {
			velocityHeight = -horizontalSpeed * Math.tan(launchAngle * 0.02454369D);
		}
		accelerationHeight = 2D * (targetHeight - positionHeight - velocityHeight * remainingCycles) / (remainingCycles * remainingCycles);
	}

	@Override
	public Model getRotatedModel() {
		Model model = spotAnim.getModel();
		if (model == null) {
			return null;
		}
		int j = -1;
		if (spotAnim.animation != null) {
			j = spotAnim.animation.frameIds[frameIndex];
		}
		Model model_1 = new Model(true, AnimationFrame.isMissingFrameId(j), false, model);
		if (j != -1) {
			model_1.buildAnimationGroups();
			model_1.applyAnimationFrame(j);
			model_1.faceGroups = null;
			model_1.vertexGroups = null;
		}
		if (spotAnim.horizontalScale128 != 128 || spotAnim.verticalScale128 != 128) {
			model_1.scale(spotAnim.horizontalScale128, spotAnim.horizontalScale128, spotAnim.verticalScale128);
		}
		model_1.rotateX(pitch);
		model_1.applyLighting(64 + spotAnim.ambientLightOffset, 850 + spotAnim.lightDivisorOffset, -30, -50, -30, true);
		return model_1;
	}

	public Projectile(int angle, int heightOffset, int start, int end, int offset, int plane, int height, int y, int x, int targetIndex, int graphicId) {
		inFlight = false;
		spotAnim = SpotAnim.cache[graphicId];
		this.plane = plane;
		originX = x;
		originY = y;
		originHeight = height;
		startCycle = start;
		endCycle = end;
		launchAngle = angle;
		startOffset = offset;
		target = targetIndex;
		targetHeightOffset = heightOffset;
		inFlight = false;
	}

	public void advance(int cycles) {
		inFlight = true;
		positionX += velocityX * cycles;
		positionY += velocityY * cycles;
		positionHeight += velocityHeight * cycles + 0.5D * accelerationHeight * cycles * cycles;
		velocityHeight += accelerationHeight * cycles;
		yaw = (int) (Math.atan2(velocityX, velocityY) * 325.94900000000001D) + 1024 & 0x7ff;
		pitch = (int) (Math.atan2(velocityHeight, horizontalSpeed) * 325.94900000000001D) & 0x7ff;
		if (spotAnim.animation != null) {
			for (frameTimer += cycles; frameTimer > spotAnim.animation.getFrameDelay(frameIndex);) {
				frameTimer -= spotAnim.animation.getFrameDelay(frameIndex) + 1;
				frameIndex++;
				if (frameIndex >= spotAnim.animation.frameCount) {
					frameIndex = 0;
				}
			}
		}

	}

	public final int startCycle;
	public final int endCycle;
	private double velocityX;
	private double velocityY;
	private double horizontalSpeed;
	private double velocityHeight;
	private double accelerationHeight;
	private boolean inFlight;
	private final int originX;
	private final int originY;
	private final int originHeight;
	public final int targetHeightOffset;
	public double positionX;
	public double positionY;
	public double positionHeight;
	private final int launchAngle;
	private final int startOffset;
	public final int target;
	private final SpotAnim spotAnim;
	private int frameIndex;
	private int frameTimer;
	public int yaw;
	private int pitch;
	public final int plane;
}
