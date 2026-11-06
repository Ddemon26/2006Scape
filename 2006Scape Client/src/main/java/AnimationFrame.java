// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3)

public final class AnimationFrame {

	public static void initialize(int maximumFrameId) {
		loadedFrames = new AnimationFrame[maximumFrameId + 1];
		alphaTransformNotSeen = new boolean[maximumFrameId + 1];
		for (int frameId = 0; frameId < maximumFrameId + 1; frameId++) {
			alphaTransformNotSeen[frameId] = true;
		}

	}

	public static void decodeFile(byte data[]) {
		Stream footer = new Stream(data);
		footer.currentOffset = data.length - 8;
		int headerLength = footer.readUnsignedShortBigEndian();
		int flagsLength = footer.readUnsignedShortBigEndian();
		int valuesLength = footer.readUnsignedShortBigEndian();
		int delaysLength = footer.readUnsignedShortBigEndian();
		int sectionOffset = 0;
		Stream headers = new Stream(data);
		headers.currentOffset = sectionOffset;
		sectionOffset += headerLength + 2;
		Stream flags = new Stream(data);
		flags.currentOffset = sectionOffset;
		sectionOffset += flagsLength;
		Stream values = new Stream(data);
		values.currentOffset = sectionOffset;
		sectionOffset += valuesLength;
		Stream delays = new Stream(data);
		delays.currentOffset = sectionOffset;
		sectionOffset += delaysLength;
		Stream tableInput = new Stream(data);
		tableInput.currentOffset = sectionOffset;
		AnimationTransformTable table = new AnimationTransformTable(tableInput);
		int frameCount = headers.readUnsignedShortBigEndian();
		int indicesBuffer[] = new int[500];
		int xBuffer[] = new int[500];
		int yBuffer[] = new int[500];
		int zBuffer[] = new int[500];
		for (int frameIndex = 0; frameIndex < frameCount; frameIndex++) {
			int frameId = headers.readUnsignedShortBigEndian();
			AnimationFrame frame = loadedFrames[frameId] = new AnimationFrame();
			frame.frameDelay = delays.readUnsignedByte();
			frame.transformTable = table;
			int tableEntryCount = headers.readUnsignedByte();
			int lastExplicitTransformIndex = -1;
			int outputCount = 0;
			for (int transformIndex = 0; transformIndex < tableEntryCount; transformIndex++) {
				int componentMask = flags.readUnsignedByte();
				if (componentMask > 0) {
					if (table.transformTypes[transformIndex] != 0) {
						for (int pivotSearchIndex = transformIndex - 1; pivotSearchIndex > lastExplicitTransformIndex; pivotSearchIndex--) {
							if (table.transformTypes[pivotSearchIndex] != 0) {
								continue;
							}
							indicesBuffer[outputCount] = pivotSearchIndex;
							xBuffer[outputCount] = 0;
							yBuffer[outputCount] = 0;
							zBuffer[outputCount] = 0;
							outputCount++;
							break;
						}

					}
					indicesBuffer[outputCount] = transformIndex;
					char defaultComponent = '\0';
					if (table.transformTypes[transformIndex] == 3) {
						defaultComponent = '\200';
					}
					if ((componentMask & 1) != 0) {
						xBuffer[outputCount] = values.readSignedSmart();
					} else {
						xBuffer[outputCount] = defaultComponent;
					}
					if ((componentMask & 2) != 0) {
						yBuffer[outputCount] = values.readSignedSmart();
					} else {
						yBuffer[outputCount] = defaultComponent;
					}
					if ((componentMask & 4) != 0) {
						zBuffer[outputCount] = values.readSignedSmart();
					} else {
						zBuffer[outputCount] = defaultComponent;
					}
					lastExplicitTransformIndex = transformIndex;
					outputCount++;
					if (table.transformTypes[transformIndex] == 5) {
						alphaTransformNotSeen[frameId] = false;
					}
				}
			}

			frame.transformCount = outputCount;
			frame.transformIndices = new int[outputCount];
			frame.transformX = new int[outputCount];
			frame.transformY = new int[outputCount];
			frame.transformZ = new int[outputCount];
			for (int outputIndex = 0; outputIndex < outputCount; outputIndex++) {
				frame.transformIndices[outputIndex] = indicesBuffer[outputIndex];
				frame.transformX[outputIndex] = xBuffer[outputIndex];
				frame.transformY[outputIndex] = yBuffer[outputIndex];
				frame.transformZ[outputIndex] = zBuffer[outputIndex];
			}

		}

	}

	public static void nullLoader() {
		loadedFrames = null;
	}

	public static AnimationFrame getFrame(int frameId) {
		if (loadedFrames == null) {
			return null;
		} else {
			return loadedFrames[frameId];
		}
	}

	public static boolean isMissingFrameId(int frameId) {
		return frameId == -1;
	}

	private AnimationFrame() {
	}

	private static AnimationFrame[] loadedFrames;
	public int frameDelay;
	public AnimationTransformTable transformTable;
	public int transformCount;
	public int transformIndices[];
	public int transformX[];
	public int transformY[];
	public int transformZ[];
	private static boolean[] alphaTransformNotSeen;

}
