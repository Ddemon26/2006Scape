// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

final class Projectile extends Animable {

    public void track(int currentCycle, int targetY, int targetZ, int targetX) {
		if (!inMotion) {
                double d = targetX - startX;
                double d2 = targetY - startY;
			double d3 = Math.sqrt(d * d + d2 * d2);
                currentX = startX + d * startDistance / d3;
                currentY = startY + d2 * startDistance / d3;
                currentHeight = startHeight;
		}
                double d1 = endCycle + 1 - currentCycle;
                speedX = (targetX - currentX) / d1;
                speedY = (targetY - currentY) / d1;
                speed = Math.sqrt(speedX * speedX + speedY * speedY);
                if (!inMotion) {
                        speedZ = -speed * Math.tan(slope * 0.02454369D);
                }
                accelerationZ = 2D * (targetZ - currentHeight - speedZ * d1) / (d1 * d1);
        }

	@Override
	public Model getRotatedModel() {
		Model model = spotAnim.getModel();
		if (model == null) {
			return null;
		}
		int j = -1;
		if (spotAnim.aAnimation_407 != null) {
			j = spotAnim.aAnimation_407.anIntArray353[frame];
		}
                Model model_1 = new Model(true, AnimFrame.isNullFrame(j), false, model);
		if (j != -1) {
			model_1.buildVertexGroups();
			model_1.applyFrame(j);
			model_1.faceGroups = null;
			model_1.vertexGroups = null;
		}
		if (spotAnim.anInt410 != 128 || spotAnim.anInt411 != 128) {
			model_1.scaleModel(spotAnim.anInt410, spotAnim.anInt410, spotAnim.anInt411);
		}
		model_1.rotateX(pitch);
		model_1.applyLighting(64 + spotAnim.anInt413, 850 + spotAnim.anInt414, -30, -50, -30, true);
		return model_1;
	}

	public Projectile(int i, int j, int l, int i1, int j1, int k1, int l1, int i2, int j2, int k2, int l2) {
		inMotion = false;
		spotAnim = SpotAnim.cache[l2];
		plane = k1;
		startX = j2;
		startY = i2;
		startHeight = l1;
		startCycle = l;
		endCycle = i1;
		slope = i;
		startDistance = j1;
		targetIndex = k2;
		heightOffset = j;
		inMotion = false;
	}

	public void update(int elapsed) {
                inMotion = true;
                currentX += speedX * elapsed;
                currentY += speedY * elapsed;
                currentHeight += speedZ * elapsed + 0.5D * accelerationZ * elapsed * elapsed;
                speedZ += accelerationZ * elapsed;
                yaw = (int) (Math.atan2(speedX, speedY) * 325.94900000000001D) + 1024 & 0x7ff;
                pitch = (int) (Math.atan2(speedZ, speed) * 325.94900000000001D) & 0x7ff;
                if (spotAnim.aAnimation_407 != null) {
                        for (frameCycle += elapsed; frameCycle > spotAnim.aAnimation_407.method258(frame);) {
                                frameCycle -= spotAnim.aAnimation_407.method258(frame) + 1;
                                frame++;
                                if (frame >= spotAnim.aAnimation_407.anInt352) {
                                        frame = 0;
                                }
                        }
                }
        }


        public final int startCycle;
        public final int endCycle;
        private double speedX;
	private double speedY;
	private double speed;
	private double speedZ;
	private double accelerationZ;
	private boolean inMotion;
	private final int startX;
	private final int startY;
	private final int startHeight;
	public final int heightOffset;
	public double currentX;
	public double currentY;
	public double currentHeight;
	private final int slope;
	private final int startDistance;
	public final int targetIndex;
	private final SpotAnim spotAnim;
	private int frame;
	private int frameCycle;
	public int yaw;
	private int pitch;
	public final int plane;
}
