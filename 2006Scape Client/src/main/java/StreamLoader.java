// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

final class StreamLoader {

	public StreamLoader(byte abyte0[]) {
                Stream stream = new Stream(abyte0);
		int i = stream.read3Bytes();
		int j = stream.read3Bytes();
		if (j != i) {
                        byte abyte1[] = new byte[i];
                        BZip2Decompressor.decompress(abyte1, i, abyte0, j, 6);
                        dataBuffer = abyte1;
                        stream = new Stream(dataBuffer);
                        usesCompression = true;
                } else {
                        dataBuffer = abyte0;
                        usesCompression = false;
                }
                fileCount = stream.readUnsignedWord();
                fileHashes = new int[fileCount];
                uncompressedSizes = new int[fileCount];
                compressedSizes = new int[fileCount];
                fileOffsets = new int[fileCount];
                int k = stream.currentOffset + fileCount * 10;
                for (int l = 0; l < fileCount; l++) {
                        fileHashes[l] = stream.readDWord();
                        uncompressedSizes[l] = stream.read3Bytes();
                        compressedSizes[l] = stream.read3Bytes();
                        fileOffsets[l] = k;
                        k += compressedSizes[l];
                }
        }

        public byte[] getFileData(String s) {
                byte abyte0[] = null; // was a parameter
                int i = 0;
                s = s.toUpperCase();
                for (int j = 0; j < s.length(); j++) {
                        i = i * 61 + s.charAt(j) - 32;
                }

                for (int k = 0; k < fileCount; k++) {
                        if (fileHashes[k] == i) {
                                if (abyte0 == null) {
                                        abyte0 = new byte[uncompressedSizes[k]];
                                }
                                if (!usesCompression) {
                                        BZip2Decompressor.decompress(abyte0, uncompressedSizes[k], dataBuffer, compressedSizes[k], fileOffsets[k]);
                                } else {
                                        System.arraycopy(dataBuffer, fileOffsets[k], abyte0, 0, uncompressedSizes[k]);

                                }
                                return abyte0;
                        }
                }

		return null;
	}

        private final byte[] dataBuffer;
        private final int fileCount;
        private final int[] fileHashes;
        private final int[] uncompressedSizes;
        private final int[] compressedSizes;
        private final int[] fileOffsets;
        private final boolean usesCompression;
}
