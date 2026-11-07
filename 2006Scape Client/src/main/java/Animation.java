// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

public final class Animation {

	public static void unpackConfig(StreamLoader streamLoader) {
		Stream stream = new Stream(streamLoader.getDataForName("seq.dat"));
		int recordCount = stream.readUnsignedShortBigEndian();
		if (anims == null) {
			anims = new Animation[recordCount];
		}
		for (int index = 0; index < recordCount; index++) {
			if (anims[index] == null) {
				anims[index] = new Animation();
			}
			anims[index].readValues(stream);
		}
	}

	public int getFrameDelay(int frameIndex) {
		int delay = frameDelays[frameIndex];
		if (delay == 0) {
			AnimationFrame frame = AnimationFrame.getFrame(frameIds[frameIndex]);
			if (frame != null) {
				delay = frameDelays[frameIndex] = frame.frameDelay;
			}
		}
		if (delay == 0) {
			delay = 1;
		}
		return delay;
	}

	private void readValues(Stream stream) {
		do {
			int opcode = stream.readUnsignedByte();
			if (opcode == 0) {
				break;
			}
			if (opcode == 1) {
				frameCount = stream.readUnsignedByte();
				frameIds = new int[frameCount];
				secondFrameIds = new int[frameCount];
				frameDelays = new int[frameCount];
				for (int frame = 0; frame < frameCount; frame++) {
					frameIds[frame] = stream.readUnsignedShortBigEndian();
					secondFrameIds[frame] = stream.readUnsignedShortBigEndian();
					if (secondFrameIds[frame] == 65535) {
						secondFrameIds[frame] = -1;
					}
					frameDelays[frame] = stream.readUnsignedShortBigEndian();
				}

			} else if (opcode == 2) {
				loopBackFrames = stream.readUnsignedShortBigEndian();
			} else if (opcode == 3) {
				int maskLength = stream.readUnsignedByte();
				interleavedTransformIndices = new int[maskLength + 1];
				for (int maskIndex = 0; maskIndex < maskLength; maskIndex++) {
					interleavedTransformIndices[maskIndex] = stream.readUnsignedByte();
				}

				interleavedTransformIndices[maskLength] = 0x98967f;
			} else if (opcode == 4) {
				extendByYaw = true;
			} else if (opcode == 5) {
				priority = stream.readUnsignedByte();
			} else if (opcode == 6) {
				slot5EquipmentOverride = stream.readUnsignedShortBigEndian();
			} else if (opcode == 7) {
				slot3EquipmentOverride = stream.readUnsignedShortBigEndian();
			} else if (opcode == 8) {
				maxLoops = stream.readUnsignedByte();
			} else if (opcode == 9) {
				movingPrecedence = stream.readUnsignedByte();
			} else if (opcode == 10) {
				idlePrecedence = stream.readUnsignedByte();
			} else if (opcode == 11) {
				replayMode = stream.readUnsignedByte();
			} else if (opcode == 12) {
				stream.readIntBigEndian();
			} else {
				System.out.println("Error unrecognised seq config code: " + opcode);
			}
		} while (true);
		if (frameCount == 0) {
			frameCount = 1;
			frameIds = new int[1];
			frameIds[0] = -1;
			secondFrameIds = new int[1];
			secondFrameIds[0] = -1;
			frameDelays = new int[1];
			frameDelays[0] = -1;
		}
		if (movingPrecedence == -1) {
			if (interleavedTransformIndices != null) {
				movingPrecedence = 2;
			} else {
				movingPrecedence = 0;
			}
		}
		if (idlePrecedence == -1) {
			if (interleavedTransformIndices != null) {
				idlePrecedence = 2;
				return;
			}
			idlePrecedence = 0;
		}
	}

	private Animation() {
		loopBackFrames = -1;
		extendByYaw = false;
		priority = 5;
		slot5EquipmentOverride = -1;
		slot3EquipmentOverride = -1;
		maxLoops = 99;
		movingPrecedence = -1;
		idlePrecedence = -1;
		replayMode = 2;
	}

	public static Animation anims[];
	public int frameCount;
	public int frameIds[];
	public int secondFrameIds[];
	private int[] frameDelays;
	public int loopBackFrames;
	public int interleavedTransformIndices[];
	public boolean extendByYaw;
	public int priority;
	public int slot5EquipmentOverride;
	public int slot3EquipmentOverride;
	public int maxLoops;
	public int movingPrecedence;
	public int idlePrecedence;
	public int replayMode;
	public static int anInt367;
}
