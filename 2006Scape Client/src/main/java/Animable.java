// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

public class Animable extends NodeSub {

	public void drawFromCamera(int modelRotation, int pitchSine, int pitchCosine, int yawSine, int yawCosine, int offsetX, int offsetY, int offsetZ, int pickId) {
		Model model = getRotatedModel();
		if (model != null) {
			modelHeight = model.modelHeight;
			model.drawFromCamera(modelRotation, pitchSine, pitchCosine, yawSine, yawCosine, offsetX, offsetY, offsetZ, pickId);
		}
	}

	Model getRotatedModel() {
		return null;
	}

	Animable() {
		modelHeight = 1000;
	}

	VertexNormal vertexNormals[];
	public int modelHeight;
}
