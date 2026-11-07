// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

public final class SpotAnim {

	public static void unpackConfig(StreamLoader streamLoader) {
		Stream stream = new Stream(streamLoader.getDataForName("spotanim.dat"));
		int recordCount = stream.readUnsignedShortBigEndian();
		if (cache == null) {
			cache = new SpotAnim[recordCount];
		}
		for (int index = 0; index < recordCount; index++) {
			if (cache[index] == null) {
				cache[index] = new SpotAnim();
			}
			cache[index].recordIndex = index;
			cache[index].readValues(stream);
		}

	}

	private void readValues(Stream stream) {
		do {
			int opcode = stream.readUnsignedByte();
			if (opcode == 0) {
				return;
			}
			if (opcode == 1) {
				modelId = stream.readUnsignedShortBigEndian();
			} else if (opcode == 2) {
				animationId = stream.readUnsignedShortBigEndian();
				if (Animation.anims != null) {
					animation = Animation.anims[animationId];
				}
			} else if (opcode == 4) {
				horizontalScale128 = stream.readUnsignedShortBigEndian();
			} else if (opcode == 5) {
				verticalScale128 = stream.readUnsignedShortBigEndian();
			} else if (opcode == 6) {
				rotationDegrees = stream.readUnsignedShortBigEndian();
			} else if (opcode == 7) {
				ambientLightOffset = stream.readUnsignedByte();
			} else if (opcode == 8) {
				lightDivisorOffset = stream.readUnsignedByte();
			} else if (opcode >= 40 && opcode < 50) {
				recolorSources[opcode - 40] = stream.readUnsignedShortBigEndian();
			} else if (opcode >= 50 && opcode < 60) {
				recolorReplacements[opcode - 50] = stream.readUnsignedShortBigEndian();
			} else {
				System.out.println("Error unrecognised spotanim config code: " + opcode);
			}
		} while (true);
	}

	public Model getModel() {
		Model model = (Model) modelCache.insertFromCache(recordIndex);
		if (model != null) {
			return model;
		}
		model = Model.getModel(modelId);
		if (model == null) {
			return null;
		}
		for (int slot = 0; slot < 6; slot++) {
			if (recolorSources[0] != 0) {
				model.recolor(recolorSources[slot], recolorReplacements[slot]);
			}
		}

		modelCache.removeFromCache(model, recordIndex);
		return model;
	}

	private SpotAnim() {
		animationId = -1;
		recolorSources = new int[6];
		recolorReplacements = new int[6];
		horizontalScale128 = 128;
		verticalScale128 = 128;
	}

	public static SpotAnim cache[];
	private int recordIndex;
	private int modelId;
	private int animationId;
	public Animation animation;
	private final int[] recolorSources;
	private final int[] recolorReplacements;
	public int horizontalScale128;
	public int verticalScale128;
	public int rotationDegrees;
	public int ambientLightOffset;
	public int lightDivisorOffset;
	public static LruCache modelCache = new LruCache(30);

}
