// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3)

import java.io.IOException;
import java.io.RandomAccessFile;

final class CacheStore {

	public CacheStore(RandomAccessFile cacheDataFile, RandomAccessFile cacheIndexFile, int cacheStoreId) {
		storeId = cacheStoreId;
		dataFile = cacheDataFile;
		indexFile = cacheIndexFile;
	}

	public synchronized byte[] readFile(int fileId) {
		try {
			seekTo(indexFile, fileId * 6);
			int indexReadCount;
			for (int indexBytesRead = 0; indexBytesRead < 6; indexBytesRead += indexReadCount) {
				indexReadCount = indexFile.read(ioBuffer, indexBytesRead, 6 - indexBytesRead);
				if (indexReadCount == -1) {
					return null;
				}
			}

			int fileSize = ((ioBuffer[0] & 0xff) << 16) + ((ioBuffer[1] & 0xff) << 8) + (ioBuffer[2] & 0xff);
			int sector = ((ioBuffer[3] & 0xff) << 16) + ((ioBuffer[4] & 0xff) << 8) + (ioBuffer[5] & 0xff);
			// if(fileSize < 0 || fileSize > 0x7a120)
			// return null;
			if (sector <= 0 || sector > dataFile.length() / 520L) {
				return null;
			}
			byte fileData[] = new byte[fileSize];
			int bytesCopied = 0;
			for (int chunkIndex = 0; bytesCopied < fileSize; chunkIndex++) {
				if (sector == 0) {
					return null;
				}
				seekTo(dataFile, sector * 520);
				int sectorBytesRead = 0;
				int chunkSize = fileSize - bytesCopied;
				if (chunkSize > 512) {
					chunkSize = 512;
				}
				int sectorReadCount;
				for (; sectorBytesRead < chunkSize + 8; sectorBytesRead += sectorReadCount) {
					sectorReadCount = dataFile.read(ioBuffer, sectorBytesRead, chunkSize + 8 - sectorBytesRead);
					if (sectorReadCount == -1) {
						return null;
					}
				}

				int headerFileId = ((ioBuffer[0] & 0xff) << 8) + (ioBuffer[1] & 0xff);
				int headerChunkIndex = ((ioBuffer[2] & 0xff) << 8) + (ioBuffer[3] & 0xff);
				int nextSector = ((ioBuffer[4] & 0xff) << 16) + ((ioBuffer[5] & 0xff) << 8) + (ioBuffer[6] & 0xff);
				int headerStoreId = ioBuffer[7] & 0xff;
				if (headerFileId != fileId || headerChunkIndex != chunkIndex || headerStoreId != storeId) {
					return null;
				}
				if (nextSector < 0 || nextSector > dataFile.length() / 520L) {
					return null;
				}
				for (int chunkOffset = 0; chunkOffset < chunkSize; chunkOffset++) {
					fileData[bytesCopied++] = ioBuffer[chunkOffset + 8];
				}

				sector = nextSector;
			}

			return fileData;
		} catch (IOException ioFailure) {
			return null;
		}
	}

	public synchronized boolean writeFile(int fileSize, byte fileData[], int fileId)
	{
		boolean written = writeFileSectors(true, fileId, fileSize, fileData);
		if(!written)
			written = writeFileSectors(false, fileId, fileSize, fileData);
		return written;
	}

	private synchronized boolean writeFileSectors(boolean reuseSectors, int fileId, int fileSize, byte fileData[]) {
		try {
			int sector;
			if (reuseSectors) {
				seekTo(indexFile, fileId * 6);
				int indexReadCount;
				for (int indexBytesRead = 0; indexBytesRead < 6; indexBytesRead += indexReadCount) {
					indexReadCount = indexFile.read(ioBuffer, indexBytesRead, 6 - indexBytesRead);
					if (indexReadCount == -1) {
						return false;
					}
				}

				sector = ((ioBuffer[3] & 0xff) << 16) + ((ioBuffer[4] & 0xff) << 8) + (ioBuffer[5] & 0xff);
				if (sector <= 0 || sector > dataFile.length() / 520L) {
					return false;
				}
			} else {
				sector = (int) ((dataFile.length() + 519L) / 520L);
				if (sector == 0) {
					sector = 1;
				}
			}
			ioBuffer[0] = (byte) (fileSize >> 16);
			ioBuffer[1] = (byte) (fileSize >> 8);
			ioBuffer[2] = (byte) fileSize;
			ioBuffer[3] = (byte) (sector >> 16);
			ioBuffer[4] = (byte) (sector >> 8);
			ioBuffer[5] = (byte) sector;
			seekTo(indexFile, fileId * 6);
			indexFile.write(ioBuffer, 0, 6);
			int bytesWritten = 0;
			for (int chunkIndex = 0; bytesWritten < fileSize; chunkIndex++) {
				int nextSector = 0;
				if (reuseSectors) {
					seekTo(dataFile, sector * 520);
					int headerBytesRead;
					int headerReadCount;
					for (headerBytesRead = 0; headerBytesRead < 8; headerBytesRead += headerReadCount) {
						headerReadCount = dataFile.read(ioBuffer, headerBytesRead, 8 - headerBytesRead);
						if (headerReadCount == -1) {
							break;
						}
					}

					if (headerBytesRead == 8) {
						int headerFileId = ((ioBuffer[0] & 0xff) << 8) + (ioBuffer[1] & 0xff);
						int headerChunkIndex = ((ioBuffer[2] & 0xff) << 8) + (ioBuffer[3] & 0xff);
						nextSector = ((ioBuffer[4] & 0xff) << 16) + ((ioBuffer[5] & 0xff) << 8) + (ioBuffer[6] & 0xff);
						int headerStoreId = ioBuffer[7] & 0xff;
						if (headerFileId != fileId || headerChunkIndex != chunkIndex || headerStoreId != storeId) {
							return false;
						}
						if (nextSector < 0 || nextSector > dataFile.length() / 520L) {
							return false;
						}
					}
				}
				if (nextSector == 0) {
					reuseSectors = false;
					nextSector = (int) ((dataFile.length() + 519L) / 520L);
					if (nextSector == 0) {
						nextSector++;
					}
					if (nextSector == sector) {
						nextSector++;
					}
				}
				if (fileSize - bytesWritten <= 512) {
					nextSector = 0;
				}
				ioBuffer[0] = (byte) (fileId >> 8);
				ioBuffer[1] = (byte) fileId;
				ioBuffer[2] = (byte) (chunkIndex >> 8);
				ioBuffer[3] = (byte) chunkIndex;
				ioBuffer[4] = (byte) (nextSector >> 16);
				ioBuffer[5] = (byte) (nextSector >> 8);
				ioBuffer[6] = (byte) nextSector;
				ioBuffer[7] = (byte) storeId;
				seekTo(dataFile, sector * 520);
				dataFile.write(ioBuffer, 0, 8);
				int chunkSize = fileSize - bytesWritten;
				if (chunkSize > 512) {
					chunkSize = 512;
				}
				dataFile.write(fileData, bytesWritten, chunkSize);
				bytesWritten += chunkSize;
				sector = nextSector;
			}

			return true;
		} catch (IOException ioFailure) {
			return false;
		}
	}

	private synchronized void seekTo(RandomAccessFile file, int byteOffset) throws IOException {
		if (byteOffset < 0 || byteOffset > 0x3c00000) {
			System.out.println("Badseek - pos:" + byteOffset + " len:" + file.length());
			byteOffset = 0x3c00000;
			try {
				Thread.sleep(1000L);
			} catch (Exception sleepFailure) {
			}
		}
		file.seek(byteOffset);
	}

	private static final byte[] ioBuffer = new byte[520];
	private final RandomAccessFile dataFile;
	private final RandomAccessFile indexFile;
	private final int storeId;

}
