// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

final class StreamLoader {

       public StreamLoader(byte archiveData[]) {
               Stream stream = new Stream(archiveData);
               int expectedLength = stream.read3Bytes();
               int actualLength = stream.read3Bytes();
               if (actualLength != expectedLength) {
                       byte decompressedData[] = new byte[expectedLength];
                       BZip2Decompressor.decompress(decompressedData, expectedLength, archiveData, actualLength, 6);
                       dataBuffer = decompressedData;
                       stream = new Stream(dataBuffer);
                       usesCompression = true;
               } else {
                       dataBuffer = archiveData;
                       usesCompression = false;
               }
                fileCount = stream.readUnsignedWord();
                fileHashes = new int[fileCount];
                uncompressedSizes = new int[fileCount];
                compressedSizes = new int[fileCount];
                fileOffsets = new int[fileCount];
               int offset = stream.currentOffset + fileCount * 10;
               for (int index = 0; index < fileCount; index++) {
                       fileHashes[index] = stream.readDWord();
                       uncompressedSizes[index] = stream.read3Bytes();
                       compressedSizes[index] = stream.read3Bytes();
                       fileOffsets[index] = offset;
                       offset += compressedSizes[index];
               }
       }

       public byte[] getFileData(String s) {
               byte fileBuffer[] = null; // was a parameter
               int fileHash = 0;
               s = s.toUpperCase();
               for (int i = 0; i < s.length(); i++) {
                       fileHash = fileHash * 61 + s.charAt(i) - 32;
               }

               for (int index = 0; index < fileCount; index++) {
                       if (fileHashes[index] == fileHash) {
                               if (fileBuffer == null) {
                                       fileBuffer = new byte[uncompressedSizes[index]];
                               }
                               if (!usesCompression) {
                                       BZip2Decompressor.decompress(fileBuffer, uncompressedSizes[index], dataBuffer, compressedSizes[index], fileOffsets[index]);
                               } else {
                                       System.arraycopy(dataBuffer, fileOffsets[index], fileBuffer, 0, uncompressedSizes[index]);

                               }
                               return fileBuffer;
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
