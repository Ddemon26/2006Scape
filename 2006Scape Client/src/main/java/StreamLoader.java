// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

final class StreamLoader {

	public StreamLoader(byte encodedArchive[]) {
		Stream header = new Stream(encodedArchive);
		int unpackedArchiveSize = header.read3Bytes();
		int packedArchiveSize = header.read3Bytes();
		if (packedArchiveSize != unpackedArchiveSize) {
			byte unpackedData[] = new byte[unpackedArchiveSize];
			Class13.method225(unpackedData, unpackedArchiveSize, encodedArchive, packedArchiveSize, 6);
			archiveData = unpackedData;
			header = new Stream(archiveData);
			archiveDecompressed = true;
		} else {
			archiveData = encodedArchive;
			archiveDecompressed = false;
		}
		entryCount = header.readUnsignedWord();
		entryNameHashes = new int[entryCount];
		entryUnpackedSizes = new int[entryCount];
		entryPackedSizes = new int[entryCount];
		entryOffsets = new int[entryCount];
		int entryOffset = header.currentOffset + entryCount * 10;
		for (int entryIndex = 0; entryIndex < entryCount; entryIndex++) {
			entryNameHashes[entryIndex] = header.readIntBigEndian();
			entryUnpackedSizes[entryIndex] = header.read3Bytes();
			entryPackedSizes[entryIndex] = header.read3Bytes();
			entryOffsets[entryIndex] = entryOffset;
			entryOffset += entryPackedSizes[entryIndex];
		}
	}

	public byte[] getDataForName(String entryName) {
		byte entryData[] = null; // was a parameter
		int nameHash = 0;
		entryName = entryName.toUpperCase();
		for (int characterIndex = 0; characterIndex < entryName.length(); characterIndex++) {
			nameHash = nameHash * 61 + entryName.charAt(characterIndex) - 32;
		}

		for (int entryIndex = 0; entryIndex < entryCount; entryIndex++) {
			if (entryNameHashes[entryIndex] == nameHash) {
				if (entryData == null) {
					entryData = new byte[entryUnpackedSizes[entryIndex]];
				}
				if (!archiveDecompressed) {
					Class13.method225(entryData, entryUnpackedSizes[entryIndex], archiveData, entryPackedSizes[entryIndex], entryOffsets[entryIndex]);
				} else {
					System.arraycopy(archiveData, entryOffsets[entryIndex], entryData, 0, entryUnpackedSizes[entryIndex]);

				}
				return entryData;
			}
		}

		return null;
	}

	private final byte[] archiveData;
	private final int entryCount;
	private final int[] entryNameHashes;
	private final int[] entryUnpackedSizes;
	private final int[] entryPackedSizes;
	private final int[] entryOffsets;
	private final boolean archiveDecompressed;
}
