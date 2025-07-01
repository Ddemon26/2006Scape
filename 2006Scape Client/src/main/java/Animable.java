// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

public class Animable extends NodeSub {

    public void render(
            int rotation,
            int pitchSin,
            int pitchCos,
            int yawSin,
            int yawCos,
            int x,
            int z,
            int y,
            int uid) {
        Model model = getRotatedModel();
        if (model != null) {
            modelHeight = model.modelHeight;
            model.render(rotation, pitchSin, pitchCos, yawSin, yawCos, x, z, y, uid);
        }
    }

	Model getRotatedModel() {
		return null;
	}

	Animable() {
		modelHeight = 1000;
	}

    VertexNormal[] vertexNormals;
	public int modelHeight;
}
