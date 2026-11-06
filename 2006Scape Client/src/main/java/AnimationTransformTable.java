// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3)

public final class AnimationTransformTable {

	public AnimationTransformTable(Stream input) {
		int transformCount = input.readUnsignedByte();
		transformTypes = new int[transformCount];
		groupIds = new int[transformCount][];
		for (int typeIndex = 0; typeIndex < transformCount; typeIndex++) {
			transformTypes[typeIndex] = input.readUnsignedByte();
		}

		for (int transformIndex = 0; transformIndex < transformCount; transformIndex++) {
			int groupCount = input.readUnsignedByte();
			groupIds[transformIndex] = new int[groupCount];
			for (int groupIndex = 0; groupIndex < groupCount; groupIndex++) {
				groupIds[transformIndex][groupIndex] = input.readUnsignedByte();
			}

		}

	}

	public final int[] transformTypes;
	public final int[][] groupIds;
}
