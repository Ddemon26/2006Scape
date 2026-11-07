// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.zip.CRC32;
import java.util.zip.GZIPInputStream;

public final class OnDemandFetcher extends OnDemandFetcherParent implements Runnable {

	/* private boolean matchesVersionAndCrc32(int i, int j, byte abyte0[])
    {
            //if(abyte0 == null || abyte0.length < 2)
                    //return false;
            int k = abyte0.length - 2;
            int l = ((abyte0[k] & 0xff) << 8) + (abyte0[k + 1] & 0xff);
            crc32.reset();
            crc32.update(abyte0, 0, k);
            int i1 = (int) crc32.getValue();
            //return l == i && i1 == j;
            return true;

    }*/

	// XXX: Fixed and refactored the matchesVersionAndCrc32 method. - Ryley
	private boolean matchesVersionAndCrc32(int expectedVersion, int expectedCrc, byte[] fileData) {
		if (fileData == null || fileData.length < 2) {
			return false;
		}
		int payloadLength = fileData.length - 2;
		int storedVersion = ((fileData[payloadLength] & 0xFF) << 8) + (fileData[payloadLength + 1] & 0xFF);
		crc32.reset();
		crc32.update(fileData, 0, payloadLength);
		int payloadCrc = (int) crc32.getValue();
		return storedVersion == expectedVersion && payloadCrc == expectedCrc;
	}

	private void readData() {
		try {
			int available = inputStream.available();
			if (expectedSize == 0 && available >= 6) {
				waiting = true;
				for (int k = 0; k < 6; k += inputStream.read(ioBuffer, k, 6 - k)) {
					;
				}
				int l = ioBuffer[0] & 0xff;
				int j1 = ((ioBuffer[1] & 0xff) << 8) + (ioBuffer[2] & 0xff);
				int l1 = ((ioBuffer[3] & 0xff) << 8) + (ioBuffer[4] & 0xff);
				int i2 = ioBuffer[5] & 0xff;
				current = null;
				for (OnDemandData onDemandData = (OnDemandData) requested.firstFromTail(); onDemandData != null; onDemandData = (OnDemandData) requested.nextTowardHead()) {
					if (onDemandData.dataType == l && onDemandData.ID == j1) {
						current = onDemandData;
					}
					if (current != null) {
						onDemandData.loopCycle = 0;
					}
				}

				if (current != null) {
					loopCycle = 0;
					if (l1 == 0) {
						Signlink.reporterror("Rej: " + l + "," + j1);
						current.buffer = null;
						if (current.incomplete) {
							synchronized (aClass19_1358) {
								aClass19_1358.insertHead(current);
							}
						} else {
							current.unlink();
						}
						current = null;
					} else {
						if (current.buffer == null && i2 == 0) {
							current.buffer = new byte[l1];
						}
						if (current.buffer == null && i2 != 0) {
							throw new IOException("missing start of file");
						}
					}
				}
				completedSize = i2 * 500;
				expectedSize = 500;
				if (expectedSize > l1 - i2 * 500) {
					expectedSize = l1 - i2 * 500;
				}
			}
			if (expectedSize > 0 && available >= expectedSize) {
				waiting = true;
				byte abyte0[] = ioBuffer;
				int i1 = 0;
				if (current != null) {
					abyte0 = current.buffer;
					i1 = completedSize;
				}
				for (int k1 = 0; k1 < expectedSize; k1 += inputStream.read(abyte0, k1 + i1, expectedSize - k1)) {
					;
				}
				if (expectedSize + completedSize >= abyte0.length && current != null) {
					if (clientInstance.cacheStores[0] != null) {
						clientInstance.cacheStores[current.dataType + 1].writeFile(abyte0.length, abyte0, current.ID);
					}
					if (!current.incomplete && current.dataType == 3) {
						current.incomplete = true;
						current.dataType = 93;
					}
					if (current.incomplete) {
						synchronized (aClass19_1358) {
							aClass19_1358.insertHead(current);
						}
					} else {
						current.unlink();
					}
				}
				expectedSize = 0;
			}
		} catch (IOException ioexception) {
			try {
				socket.close();
			} catch (Exception _ex) {
			}
			socket = null;
			inputStream = null;
			outputStream = null;
			expectedSize = 0;
		}
	}

	public void start(StreamLoader streamLoader, Game client) {
		String versionNames[] = {"model_version", "anim_version", "midi_version", "map_version"};
		for (int tableIndex = 0; tableIndex < 4; tableIndex++) {
			byte versionData[] = streamLoader.getDataForName(versionNames[tableIndex]);
			int versionCount = versionData.length / 2;
			Stream versionStream = new Stream(versionData);
			versions[tableIndex] = new int[versionCount];
			fileStatus[tableIndex] = new byte[versionCount];
			for (int versionIndex = 0; versionIndex < versionCount; versionIndex++) {
				versions[tableIndex][versionIndex] = versionStream.readUnsignedShortBigEndian();
			}

		}

		String crcNames[] = {"model_crc", "anim_crc", "midi_crc", "map_crc"};
		for (int tableIndex = 0; tableIndex < 4; tableIndex++) {
			byte crcData[] = streamLoader.getDataForName(crcNames[tableIndex]);
			int crcCount = crcData.length / 4;
			Stream crcStream = new Stream(crcData);
			crcs[tableIndex] = new int[crcCount];
			for (int crcIndex = 0; crcIndex < crcCount; crcIndex++) {
				crcs[tableIndex][crcIndex] = crcStream.readIntBigEndian();
			}

		}

		byte tableData[] = streamLoader.getDataForName("model_index");
		int entryCount = versions[0].length;
		modelIndices = new byte[entryCount];
		for (int entryIndex = 0; entryIndex < entryCount; entryIndex++) {
			if (entryIndex < tableData.length) {
				modelIndices[entryIndex] = tableData[entryIndex];
			} else {
				modelIndices[entryIndex] = 0;
			}
		}

		tableData = streamLoader.getDataForName("map_index");
		Stream tableStream = new Stream(tableData);
		entryCount = tableData.length / 7;
		mapRegionKeys = new int[entryCount];
		mapTerrainFileIds = new int[entryCount];
		mapObjectFileIds = new int[entryCount];
		mapPreloadFlags = new int[entryCount];
		for (int entryIndex = 0; entryIndex < entryCount; entryIndex++) {
			mapRegionKeys[entryIndex] = tableStream.readUnsignedShortBigEndian();
			mapTerrainFileIds[entryIndex] = tableStream.readUnsignedShortBigEndian();
			mapObjectFileIds[entryIndex] = tableStream.readUnsignedShortBigEndian();
			mapPreloadFlags[entryIndex] = tableStream.readUnsignedByte();
		}

		tableData = streamLoader.getDataForName("anim_index");
		tableStream = new Stream(tableData);
		entryCount = tableData.length / 2;
		animIndices = new int[entryCount];
		for (int entryIndex = 0; entryIndex < entryCount; entryIndex++) {
			animIndices[entryIndex] = tableStream.readUnsignedShortBigEndian();
		}

		tableData = streamLoader.getDataForName("midi_index");
		tableStream = new Stream(tableData);
		entryCount = tableData.length;
		songPreloadFlags = new int[entryCount];
		for (int entryIndex = 0; entryIndex < entryCount; entryIndex++) {
			songPreloadFlags[entryIndex] = tableStream.readUnsignedByte();
		}

		clientInstance = client;
		running = true;
		clientInstance.startRunnable(this, 2);
	}

	public int getNodeCount() {
		synchronized (nodeSubList) {
			return nodeSubList.getNodeCount();
		}
	}

	public void disable() {
		running = false;
	}

	public void markMapFilesForDownload(boolean allRegions) {
		int regionCount = mapRegionKeys.length;
		for (int recordIndex = 0; recordIndex < regionCount; recordIndex++) {
			if (allRegions || mapPreloadFlags[recordIndex] != 0) {
				markExtraFileForDownload((byte) 2, 3, mapObjectFileIds[recordIndex]);
				markExtraFileForDownload((byte) 2, 3, mapTerrainFileIds[recordIndex]);
			}
		}

	}

	public int getVersionCount(int j) {
		return versions[j].length;
	}

	private void closeRequest(OnDemandData onDemandData) {
		try {
			if (socket == null) {
				long l = System.currentTimeMillis();
				if (l - openSocketTime < 4000L) {
					return;
				}
				openSocketTime = l;
				socket = clientInstance.openSocket((ClientSettings.SINGLE_ONDEMAND || ClientSettings.SERVER_WORLD == 1) ? 43594 : 43596 + ClientSettings.SERVER_WORLD + Game.portOff);
				inputStream = socket.getInputStream();
				outputStream = socket.getOutputStream();
				outputStream.write(15);
				for (int j = 0; j < 8; j++) {
					inputStream.read();
				}

				loopCycle = 0;
			}
			ioBuffer[0] = (byte) onDemandData.dataType;
			ioBuffer[1] = (byte) (onDemandData.ID >> 8);
			ioBuffer[2] = (byte) onDemandData.ID;
			if (onDemandData.incomplete) {
				ioBuffer[3] = 2;
			} else if (!clientInstance.loggedIn) {
				ioBuffer[3] = 1;
			} else {
				ioBuffer[3] = 0;
			}
			outputStream.write(ioBuffer, 0, 4);
			writeLoopCycle = 0;
			anInt1349 = -10000;
			return;
		} catch (IOException ioexception) {
		}
		try {
			socket.close();
		} catch (Exception _ex) {
		}
		socket = null;
		inputStream = null;
		outputStream = null;
		expectedSize = 0;
		anInt1349++;
	}

	public int getAnimCount() {
		return animIndices.length;
	}

	public void method558(int i, int j) {
		if (i < 0 || i > versions.length || j < 0 || j > versions[i].length) {
			return;
		}
		if (versions[i][j] == 0) {
			return;
		}
		synchronized (nodeSubList) {
			for (OnDemandData onDemandData = (OnDemandData) nodeSubList.firstFromTail(); onDemandData != null; onDemandData = (OnDemandData) nodeSubList.nextTowardHead()) {
				if (onDemandData.dataType == i && onDemandData.ID == j) {
					return;
				}
			}

			OnDemandData onDemandData_1 = new OnDemandData();
			onDemandData_1.dataType = i;
			onDemandData_1.ID = j;
			onDemandData_1.incomplete = true;
			synchronized (aClass19_1370) {
				aClass19_1370.insertHead(onDemandData_1);
			}
			nodeSubList.insertHead(onDemandData_1);
		}
	}

	public int getModelIndex(int i) {
		return modelIndices[i] & 0xff;
	}

	@Override
	public void run() {
		try {
			while (running) {
				onDemandCycle++;
				int i = 20;
				if (highestPendingPriority == 0 && clientInstance.cacheStores[0] != null) {
					i = 50;
				}
				try {
					Thread.sleep(i);
				} catch (Exception _ex) {
				}
				waiting = true;
				for (int j = 0; j < 100; j++) {
					if (!waiting) {
						break;
					}
					waiting = false;
					checkReceived();
					handleFailed();
					if (uncompletedCount == 0 && j >= 5) {
						break;
					}
					dispatchExtraFileRequests();
					if (inputStream != null) {
						readData();
					}
				}

				boolean flag = false;
				for (OnDemandData onDemandData = (OnDemandData) requested.firstFromTail(); onDemandData != null; onDemandData = (OnDemandData) requested.nextTowardHead()) {
					if (onDemandData.incomplete) {
						flag = true;
						onDemandData.loopCycle++;
						if (onDemandData.loopCycle > 50) {
							onDemandData.loopCycle = 0;
							closeRequest(onDemandData);
						}
					}
				}

				if (!flag) {
					for (OnDemandData onDemandData_1 = (OnDemandData) requested.firstFromTail(); onDemandData_1 != null; onDemandData_1 = (OnDemandData) requested.nextTowardHead()) {
						flag = true;
						onDemandData_1.loopCycle++;
						if (onDemandData_1.loopCycle > 50) {
							onDemandData_1.loopCycle = 0;
							closeRequest(onDemandData_1);
						}
					}

				}
				if (flag) {
					loopCycle++;
					if (loopCycle > 750) {
						try {
							socket.close();
						} catch (Exception _ex) {
						}
						socket = null;
						inputStream = null;
						outputStream = null;
						expectedSize = 0;
					}
				} else {
					loopCycle = 0;
					statusString = "";
				}
				if (clientInstance.loggedIn && socket != null && outputStream != null && (highestPendingPriority > 0 || clientInstance.cacheStores[0] == null)) {
					writeLoopCycle++;
					if (writeLoopCycle > 500) {
						writeLoopCycle = 0;
						ioBuffer[0] = 0;
						ioBuffer[1] = 0;
						ioBuffer[2] = 0;
						ioBuffer[3] = 10;
						try {
							outputStream.write(ioBuffer, 0, 4);
						} catch (IOException _ex) {
							loopCycle = 5000;
						}
					}
				}
			}
		} catch (Exception exception) {
			Signlink.reporterror("od_ex " + exception.getMessage());
		}
	}

	public void requestExtraFile(int fileId, int dataType) {
		if (clientInstance.cacheStores[0] == null) {
			return;
		}
		if (versions[dataType][fileId] == 0) {
			return;
		}
		if (fileStatus[dataType][fileId] == 0) {
			return;
		}
		if (highestPendingPriority == 0) {
			return;
		}
		OnDemandData onDemandData = new OnDemandData();
		onDemandData.dataType = dataType;
		onDemandData.ID = fileId;
		onDemandData.incomplete = false;
		synchronized (extraFileQueue) {
			extraFileQueue.insertHead(onDemandData);
		}
	}

	public OnDemandData getNextNode() {
		OnDemandData onDemandData;
		synchronized (aClass19_1358) {
			onDemandData = (OnDemandData) aClass19_1358.popTail();
		}
		if (onDemandData == null) {
			return null;
		}
		synchronized (nodeSubList) {
			onDemandData.unlinkSub();
		}
		if (onDemandData.buffer == null) {
			return onDemandData;
		}
		int i = 0;
		try {
			GZIPInputStream gzipinputstream = new GZIPInputStream(new ByteArrayInputStream(onDemandData.buffer));
			do {
				if (i == gzipInputBuffer.length) {
					throw new RuntimeException("buffer overflow!");
				}
				int k = gzipinputstream.read(gzipInputBuffer, i, gzipInputBuffer.length - i);
				if (k == -1) {
					break;
				}
				i += k;
			} while (true);
		} catch (IOException _ex) {
			throw new RuntimeException("error unzipping");
		}
		onDemandData.buffer = new byte[i];
		System.arraycopy(gzipInputBuffer, 0, onDemandData.buffer, 0, i);

		return onDemandData;
	}

	public int getMapFileId(int fileKind, int regionY, int regionX) {
		int regionKey = (regionX << 8) + regionY;
		for (int recordIndex = 0; recordIndex < mapRegionKeys.length; recordIndex++) {
			if (mapRegionKeys[recordIndex] == regionKey) {
				if (fileKind == 0) {
					return mapTerrainFileIds[recordIndex];
				} else {
					return mapObjectFileIds[recordIndex];
				}
			}
		}
		return -1;
	}

	@Override
	public void requestModel(int i) {
		method558(0, i);
	}

	public void markExtraFileForDownload(byte priority, int dataType, int fileId) {
		if (clientInstance.cacheStores[0] == null) {
			return;
		}
		if (versions[dataType][fileId] == 0) {
			return;
		}
		byte cachedData[] = clientInstance.cacheStores[dataType + 1].readFile(fileId);
		if (matchesVersionAndCrc32(versions[dataType][fileId], crcs[dataType][fileId], cachedData)) {
			return;
		}
		fileStatus[dataType][fileId] = priority;
		if (priority > highestPendingPriority) {
			highestPendingPriority = priority;
		}
		totalFiles++;
	}

	public boolean isMapObjectFileId(int fileId) {
		for (int recordIndex = 0; recordIndex < mapRegionKeys.length; recordIndex++) {
			if (mapObjectFileIds[recordIndex] == fileId) {
				return true;
			}
		}
		return false;
	}

	private void handleFailed() {
		uncompletedCount = 0;
		completedCount = 0;
		for (OnDemandData onDemandData = (OnDemandData) requested.firstFromTail(); onDemandData != null; onDemandData = (OnDemandData) requested.nextTowardHead()) {
			if (onDemandData.incomplete) {
				uncompletedCount++;
			} else {
				completedCount++;
			}
		}

		while (uncompletedCount < 10) {
			OnDemandData onDemandData_1 = (OnDemandData) aClass19_1368.popTail();
			if (onDemandData_1 == null) {
				break;
			}
			if (fileStatus[onDemandData_1.dataType][onDemandData_1.ID] != 0) {
				filesLoaded++;
			}
			fileStatus[onDemandData_1.dataType][onDemandData_1.ID] = 0;
			requested.insertHead(onDemandData_1);
			uncompletedCount++;
			closeRequest(onDemandData_1);
			waiting = true;
		}
	}

	public void clearExtraFileQueue() {
		synchronized (extraFileQueue) {
			extraFileQueue.removeAll();
		}
	}

	private void checkReceived() {
		OnDemandData onDemandData;
		synchronized (aClass19_1370) {
			onDemandData = (OnDemandData) aClass19_1370.popTail();
		}
		while (onDemandData != null) {
			waiting = true;
			byte abyte0[] = null;
			if (clientInstance.cacheStores[0] != null) {
				abyte0 = clientInstance.cacheStores[onDemandData.dataType + 1].readFile(onDemandData.ID);
			}
			if (!matchesVersionAndCrc32(versions[onDemandData.dataType][onDemandData.ID], crcs[onDemandData.dataType][onDemandData.ID], abyte0)) {
				abyte0 = null;
			}
			synchronized (aClass19_1370) {
				if (abyte0 == null) {
					aClass19_1368.insertHead(onDemandData);
				} else {
					onDemandData.buffer = abyte0;
					synchronized (aClass19_1358) {
						aClass19_1358.insertHead(onDemandData);
					}
				}
				onDemandData = (OnDemandData) aClass19_1370.popTail();
			}
		}
	}

	private void dispatchExtraFileRequests() {
		while (uncompletedCount == 0 && completedCount < 10) {
			if (highestPendingPriority == 0) {
				break;
			}
			OnDemandData queuedRequest;
			synchronized (extraFileQueue) {
				queuedRequest = (OnDemandData) extraFileQueue.popTail();
			}
			while (queuedRequest != null) {
				if (fileStatus[queuedRequest.dataType][queuedRequest.ID] != 0) {
					fileStatus[queuedRequest.dataType][queuedRequest.ID] = 0;
					requested.insertHead(queuedRequest);
					closeRequest(queuedRequest);
					waiting = true;
					if (filesLoaded < totalFiles) {
						filesLoaded++;
					}
					statusString = "Loading extra files - " + filesLoaded * 100 / totalFiles + "%";
					completedCount++;
					if (completedCount == 10) {
						return;
					}
				}
				synchronized (extraFileQueue) {
					queuedRequest = (OnDemandData) extraFileQueue.popTail();
				}
			}
			for (int dataType = 0; dataType < 4; dataType++) {
				byte typePriorities[] = fileStatus[dataType];
				int fileCount = typePriorities.length;
				for (int fileId = 0; fileId < fileCount; fileId++) {
					if (typePriorities[fileId] == highestPendingPriority) {
						typePriorities[fileId] = 0;
						OnDemandData scannedRequest = new OnDemandData();
						scannedRequest.dataType = dataType;
						scannedRequest.ID = fileId;
						scannedRequest.incomplete = false;
						requested.insertHead(scannedRequest);
						closeRequest(scannedRequest);
						waiting = true;
						if (filesLoaded < totalFiles) {
							filesLoaded++;
						}
						statusString = "Loading extra files - " + filesLoaded * 100 / totalFiles + "%";
						completedCount++;
						if (completedCount == 10) {
							return;
						}
					}
				}

			}

			highestPendingPriority--;
		}
	}

	public boolean isSongPreloadFlagged(int songId) {
		return songPreloadFlags[songId] == 1;
	}

	public OnDemandFetcher() {
		requested = new NodeList();
		statusString = "";
		crc32 = new CRC32();
		ioBuffer = new byte[500];
		fileStatus = new byte[4][];
		extraFileQueue = new NodeList();
		running = true;
		waiting = false;
		aClass19_1358 = new NodeList();
		gzipInputBuffer = new byte[65000];
		nodeSubList = new NodeSubList();
		versions = new int[4][];
		crcs = new int[4][];
		aClass19_1368 = new NodeList();
		aClass19_1370 = new NodeList();
	}

	private int totalFiles;
	private final NodeList requested;
	private int highestPendingPriority;
	public String statusString;
	private int writeLoopCycle;
	private long openSocketTime;
	private int[] mapObjectFileIds;
	private final CRC32 crc32;
	private final byte[] ioBuffer;
	public int onDemandCycle;
	private final byte[][] fileStatus;
	private Game clientInstance;
	private final NodeList extraFileQueue;
	private int completedSize;
	private int expectedSize;
	int[] songPreloadFlags;
	public int anInt1349;
	private int[] mapTerrainFileIds;
	private int filesLoaded;
	private boolean running;
	private OutputStream outputStream;
	private int[] mapPreloadFlags;
	private boolean waiting;
	private final NodeList aClass19_1358;
	private final byte[] gzipInputBuffer;
	private int[] animIndices;
	private final NodeSubList nodeSubList;
	private InputStream inputStream;
	private Socket socket;
	public final int[][] versions;
	public final int[][] crcs;
	private int uncompletedCount;
	private int completedCount;
	private final NodeList aClass19_1368;
	private OnDemandData current;
	private final NodeList aClass19_1370;
	private int[] mapRegionKeys;
	private byte[] modelIndices;
	private int loopCycle;
}
