// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

public final class IDK {

	public static void unpackConfig(StreamLoader streamLoader) {
		Stream stream = new Stream(streamLoader.getDataForName("idk.dat"));
		length = stream.readUnsignedShortBigEndian();
		if (cache == null) {
			cache = new IDK[length];
		}
		for (int kitIndex = 0; kitIndex < length; kitIndex++) {
			if (cache[kitIndex] == null) {
				cache[kitIndex] = new IDK();
			}
			cache[kitIndex].readValues(stream);
		}
	}

	private void readValues(Stream stream) {
		do {
			int opcode = stream.readUnsignedByte();
			if (opcode == 0) {
				return;
			}
			if (opcode == 1) {
				bodyPartIndex = stream.readUnsignedByte();
			} else if (opcode == 2) {
				int modelCount = stream.readUnsignedByte();
				bodyModelIds = new int[modelCount];
				for (int modelIndex = 0; modelIndex < modelCount; modelIndex++) {
					bodyModelIds[modelIndex] = stream.readUnsignedShortBigEndian();
				}

			} else if (opcode == 3) {
				notSelectable = true;
			} else if (opcode >= 40 && opcode < 50) {
				recolourSources[opcode - 40] = stream.readUnsignedShortBigEndian();
			} else if (opcode >= 50 && opcode < 60) {
				recolourReplacements[opcode - 50] = stream.readUnsignedShortBigEndian();
			} else if (opcode >= 60 && opcode < 70) {
				headModelIds[opcode - 60] = stream.readUnsignedShortBigEndian();
			} else {
				System.out.println("Error unrecognised config code: " + opcode);
			}
		} while (true);
	}

	public boolean areBodyModelsLoaded() {
		if (bodyModelIds == null) {
			return true;
		}
		boolean allLoaded = true;
		for (int modelIndex = 0; modelIndex < bodyModelIds.length; modelIndex++) {
			if (!Model.isModelLoaded(bodyModelIds[modelIndex])) {
				allLoaded = false;
			}
		}

		return allLoaded;
	}

	public Model buildBodyModel() {
		if (bodyModelIds == null) {
			return null;
		}
		Model partModels[] = new Model[bodyModelIds.length];
		for (int modelIndex = 0; modelIndex < bodyModelIds.length; modelIndex++) {
			partModels[modelIndex] = Model.getModel(bodyModelIds[modelIndex]);
		}

		Model kitModel;
		if (partModels.length == 1) {
			kitModel = partModels[0];
		} else {
			kitModel = new Model(partModels.length, partModels);
		}
		for (int recolourIndex = 0; recolourIndex < 6; recolourIndex++) {
			if (recolourSources[recolourIndex] == 0) {
				break;
			}
			kitModel.recolor(recolourSources[recolourIndex], recolourReplacements[recolourIndex]);
		}

		return kitModel;
	}

	public boolean areHeadModelsLoaded() {
		boolean allLoaded = true;
		for (int modelIndex = 0; modelIndex < 5; modelIndex++) {
			if (headModelIds[modelIndex] != -1 && !Model.isModelLoaded(headModelIds[modelIndex])) {
				allLoaded = false;
			}
		}

		return allLoaded;
	}

	public Model buildHeadModel() {
		Model partModels[] = new Model[5];
		int partCount = 0;
		for (int headIndex = 0; headIndex < 5; headIndex++) {
			if (headModelIds[headIndex] != -1) {
				partModels[partCount++] = Model.getModel(headModelIds[headIndex]);
			}
		}

		Model kitModel = new Model(partCount, partModels);
		for (int recolourIndex = 0; recolourIndex < 6; recolourIndex++) {
			if (recolourSources[recolourIndex] == 0) {
				break;
			}
			kitModel.recolor(recolourSources[recolourIndex], recolourReplacements[recolourIndex]);
		}

		return kitModel;
	}

	private IDK() {
		bodyPartIndex = -1;
		recolourSources = new int[6];
		recolourReplacements = new int[6];
		notSelectable = false;
	}

	public static int length;
	public static IDK cache[];
	public int bodyPartIndex;
	private int[] bodyModelIds;
	private final int[] recolourSources;
	private final int[] recolourReplacements;
	private final int[] headModelIds = {-1, -1, -1, -1, -1};
	public boolean notSelectable;
}
