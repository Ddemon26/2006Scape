// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3)

final class GraphicsObject extends Animable {

    public GraphicsObject(int plane, int startCycle, int delay, int spotAnimId, int height, int y, int x) {
        finished = false;
        spotAnimation = SpotAnim.cache[spotAnimId];
        this.plane = plane;
        this.x = x;
        this.y = y;
        this.height = height;
        endCycle = startCycle + delay;
        finished = false;
    }

    @Override
    public Model getRotatedModel() {
        Model model = spotAnimation.getModel();
        if (model == null) {
            return null;
        }
        int   j       = spotAnimation.animation.anIntArray353[frame];
        Model model_1 = new Model(true, AnimFrame.isNullFrame(j), false, model);
        if (!finished) {
            model_1.buildVertexGroups();
            model_1.applyFrame(j);
            model_1.faceGroups = null;
            model_1.vertexGroups = null;
        }
        if (spotAnimation.scaleX != 128 || spotAnimation.scaleY != 128) {
            model_1.scaleModel(spotAnimation.scaleX, spotAnimation.scaleX, spotAnimation.scaleY);
        }
        if (spotAnimation.rotation != 0) {
            if (spotAnimation.rotation == 90) {
                model_1.calculateNormals();
            }
            if (spotAnimation.rotation == 180) {
                model_1.calculateNormals();
                model_1.calculateNormals();
            }
            if (spotAnimation.rotation == 270) {
                model_1.calculateNormals();
                model_1.calculateNormals();
                model_1.calculateNormals();
            }
        }
        model_1.applyLighting(64 + spotAnimation.ambient, 850 + spotAnimation.contrast, -30, -50, -30, true);
        return model_1;
    }

    public void update(int elapsed) {
        for (frameCycle += elapsed; frameCycle > spotAnimation.animation.getFrameDelay(frame); ) {
            frameCycle -= spotAnimation.animation.getFrameDelay(frame) + 1;
            frame++;
            if (frame >= spotAnimation.animation.anInt352 && (frame < 0 || frame >= spotAnimation.animation.anInt352)) {
                frame = 0;
                finished = true;
            }
        }

    }

    public final  int      plane;
    public final  int      x;
    public final  int      y;
    public final  int      height;
    public final  int      endCycle;
    public        boolean  finished;
    private final SpotAnim spotAnimation;
    private       int      frame;
    private       int      frameCycle;
}
