// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3)

final class StationaryGraphic extends Animable {

    public StationaryGraphic(int plane, int currentCycle, int delay, int spotAnimationId, int height, int worldX, int worldY) {
        finished = false;
        spotAnimation = SpotAnim.cache[spotAnimationId];
        this.plane = plane;
        this.worldY = worldY;
        this.worldX = worldX;
        this.height = height;
        startCycle = currentCycle + delay;
        finished = false;
    }

    @Override
    public Model getRotatedModel() {
        Model model = spotAnimation.getModel();
        if (model == null) {
            return null;
        }
        int   j       = spotAnimation.animation.frameIds[frameIndex];
        Model model_1 = new Model(true, AnimationFrame.isMissingFrameId(j), false, model);
        if (!finished) {
            model_1.buildAnimationGroups();
            model_1.applyAnimationFrame(j);
            model_1.faceGroups = null;
            model_1.vertexGroups = null;
        }
        if (spotAnimation.horizontalScale128 != 128 || spotAnimation.verticalScale128 != 128) {
            model_1.scale(spotAnimation.horizontalScale128, spotAnimation.horizontalScale128, spotAnimation.verticalScale128);
        }
        if (spotAnimation.rotationDegrees != 0) {
            if (spotAnimation.rotationDegrees == 90) {
                model_1.rotateYQuarterTurn();
            }
            if (spotAnimation.rotationDegrees == 180) {
                model_1.rotateYQuarterTurn();
                model_1.rotateYQuarterTurn();
            }
            if (spotAnimation.rotationDegrees == 270) {
                model_1.rotateYQuarterTurn();
                model_1.rotateYQuarterTurn();
                model_1.rotateYQuarterTurn();
            }
        }
        model_1.applyLighting(64 + spotAnimation.ambientLightOffset, 850 + spotAnimation.lightDivisorOffset, -30, -50, -30, true);
        return model_1;
    }

    public void advanceAnimation(int elapsedTicks) {
        for (frameTicks += elapsedTicks; frameTicks > spotAnimation.animation.getFrameDelay(frameIndex); ) {
            frameTicks -= spotAnimation.animation.getFrameDelay(frameIndex) + 1;
            frameIndex++;
            if (frameIndex >= spotAnimation.animation.frameCount && (frameIndex < 0 || frameIndex >= spotAnimation.animation.frameCount)) {
                frameIndex = 0;
                finished = true;
            }
        }

    }

    public final  int      plane;
    public final  int      worldY;
    public final  int      worldX;
    public final  int      height;
    public final  int      startCycle;
    public        boolean  finished;
    private final SpotAnim spotAnimation;
    private       int      frameIndex;
    private       int      frameTicks;
}
