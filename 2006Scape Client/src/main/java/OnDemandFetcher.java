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
				for (int headerBytesRead = 0; headerBytesRead < 6; headerBytesRead += inputStream.read(ioBuffer, headerBytesRead, 6 - headerBytesRead)) {
					;
				}
				int fileType = ioBuffer[0] & 0xff;
				int fileId = ((ioBuffer[1] & 0xff) << 8) + (ioBuffer[2] & 0xff);
				int fileSize = ((ioBuffer[3] & 0xff) << 8) + (ioBuffer[4] & 0xff);
				int chunkIndex = ioBuffer[5] & 0xff;
				current = null;
				for (OnDemandData onDemandData = (OnDemandData) requested.firstFromTail(); onDemandData != null; onDemandData = (OnDemandData) requested.nextTowardHead()) {
					if (onDemandData.dataType == fileType && onDemandData.ID == fileId) {
						current = onDemandData;
					}
					if (current != null) {
						onDemandData.loopCycle = 0;
					}
				}

				if (current != null) {
					loopCycle = 0;
					if (fileSize == 0) {
						Signlink.reporterror("Rej: " + fileType + "," + fileId);
						current.buffer = null;
						if (current.incomplete) {
							synchronized (completedRequests) {
								completedRequests.insertHead(current);
							}
						} else {
							current.unlink();
						}
						current = null;
					} else {
						if (current.buffer == null && chunkIndex == 0) {
							current.buffer = new byte[fileSize];
						}
						if (current.buffer == null && chunkIndex != 0) {
							throw new IOException("missing start of file");
						}
					}
				}
				completedSize = chunkIndex * 500;
				expectedSize = 500;
				if (expectedSize > fileSize - chunkIndex * 500) {
					expectedSize = fileSize - chunkIndex * 500;
				}
			}
			if (expectedSize > 0 && available >= expectedSize) {
				waiting = true;
				byte destinationBuffer[] = ioBuffer;
				int destinationOffset = 0;
				if (current != null) {
					destinationBuffer = current.buffer;
					destinationOffset = completedSize;
				}
				for (int bytesRead = 0; bytesRead < expectedSize; bytesRead += inputStream.read(destinationBuffer, bytesRead + destinationOffset, expectedSize - bytesRead)) {
					;
				}
				if (expectedSize + completedSize >= destinationBuffer.length && current != null) {
					if (clientInstance.cacheStores[0] != null) {
						clientInstance.cacheStores[current.dataType + 1].writeFile(destinationBuffer.length, destinationBuffer, current.ID);
					}
					if (!current.incomplete && current.dataType == 3) {
						current.incomplete = true;
						current.dataType = 93;
					}
					if (current.incomplete) {
						synchronized (completedRequests) {
							completedRequests.insertHead(current);
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

	public int getVersionCount(int dataType) {
		return versions[dataType].length;
	}

	private void sendRequest(OnDemandData request) {
		try {
			if (socket == null) {
				long currentTime = System.currentTimeMillis();
				if (currentTime - openSocketTime < 4000L) {
					return;
				}
				openSocketTime = currentTime;
				socket = clientInstance.openSocket((ClientSettings.SINGLE_ONDEMAND || ClientSettings.SERVER_WORLD == 1) ? 43594 : 43596 + ClientSettings.SERVER_WORLD + Game.portOff);
				inputStream = socket.getInputStream();
				outputStream = socket.getOutputStream();
				outputStream.write(15);
				for (int handshakeIndex = 0; handshakeIndex < 8; handshakeIndex++) {
					inputStream.read();
				}

				loopCycle = 0;
			}
			ioBuffer[0] = (byte) request.dataType;
			ioBuffer[1] = (byte) (request.ID >> 8);
			ioBuffer[2] = (byte) request.ID;
			if (request.incomplete) {
				ioBuffer[3] = 2;
			} else if (!clientInstance.loggedIn) {
				ioBuffer[3] = 1;
			} else {
				ioBuffer[3] = 0;
			}
			outputStream.write(ioBuffer, 0, 4);
			writeLoopCycle = 0;
			connectionFailureCount = -10000;
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
		connectionFailureCount++;
	}

	public int getAnimCount() {
		return animIndices.length;
	}

	public void requestFile(int dataType, int fileId) {
		if (dataType < 0 || dataType > versions.length || fileId < 0 || fileId > versions[dataType].length) {
			return;
		}
		if (versions[dataType][fileId] == 0) {
			return;
		}
		synchronized (nodeSubList) {
			for (OnDemandData onDemandData = (OnDemandData) nodeSubList.firstFromTail(); onDemandData != null; onDemandData = (OnDemandData) nodeSubList.nextTowardHead()) {
				if (onDemandData.dataType == dataType && onDemandData.ID == fileId) {
					return;
				}
			}

			OnDemandData onDemandData_1 = new OnDemandData();
			onDemandData_1.dataType = dataType;
			onDemandData_1.ID = fileId;
			onDemandData_1.incomplete = true;
			synchronized (cacheLookupQueue) {
				cacheLookupQueue.insertHead(onDemandData_1);
			}
			nodeSubList.insertHead(onDemandData_1);
		}
	}

	public int getModelIndex(int modelId) {
		return modelIndices[modelId] & 0xff;
	}

	@Override
	public void run() {
		try {
			while (running) {
				onDemandCycle++;
				int sleepMillis = 20;
				if (highestPendingPriority == 0 && clientInstance.cacheStores[0] != null) {
					sleepMillis = 50;
				}
				try {
					Thread.sleep(sleepMillis);
				} catch (Exception _ex) {
				}
				waiting = true;
				for (int pollPass = 0; pollPass < 100; pollPass++) {
					if (!waiting) {
						break;
					}
					waiting = false;
					checkReceived();
					handleFailed();
					if (uncompletedCount == 0 && pollPass >= 5) {
						break;
					}
					dispatchExtraFileRequests();
					if (inputStream != null) {
						readData();
					}
				}

				boolean hasOutstandingRequest = false;
				for (OnDemandData request = (OnDemandData) requested.firstFromTail(); request != null; request = (OnDemandData) requested.nextTowardHead()) {
					if (request.incomplete) {
						hasOutstandingRequest = true;
						request.loopCycle++;
						if (request.loopCycle > 50) {
							request.loopCycle = 0;
							sendRequest(request);
						}
					}
				}

				if (!hasOutstandingRequest) {
					for (OnDemandData request = (OnDemandData) requested.firstFromTail(); request != null; request = (OnDemandData) requested.nextTowardHead()) {
						hasOutstandingRequest = true;
						request.loopCycle++;
						if (request.loopCycle > 50) {
							request.loopCycle = 0;
							sendRequest(request);
						}
					}

				}
				if (hasOutstandingRequest) {
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
		synchronized (completedRequests) {
			onDemandData = (OnDemandData) completedRequests.popTail();
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
		int inflatedLength = 0;
		try {
			GZIPInputStream gzipinputstream = new GZIPInputStream(new ByteArrayInputStream(onDemandData.buffer));
			do {
				if (inflatedLength == gzipInputBuffer.length) {
					throw new RuntimeException("buffer overflow!");
				}
				int bytesRead = gzipinputstream.read(gzipInputBuffer, inflatedLength, gzipInputBuffer.length - inflatedLength);
				if (bytesRead == -1) {
					break;
				}
				inflatedLength += bytesRead;
			} while (true);
		} catch (IOException _ex) {
			throw new RuntimeException("error unzipping");
		}
		onDemandData.buffer = new byte[inflatedLength];
		System.arraycopy(gzipInputBuffer, 0, onDemandData.buffer, 0, inflatedLength);

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
	public void requestModel(int modelId) {
		requestFile(0, modelId);
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
			OnDemandData onDemandData_1 = (OnDemandData) cacheMissQueue.popTail();
			if (onDemandData_1 == null) {
				break;
			}
			if (fileStatus[onDemandData_1.dataType][onDemandData_1.ID] != 0) {
				filesLoaded++;
			}
			fileStatus[onDemandData_1.dataType][onDemandData_1.ID] = 0;
			requested.insertHead(onDemandData_1);
			uncompletedCount++;
			sendRequest(onDemandData_1);
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
		synchronized (cacheLookupQueue) {
			onDemandData = (OnDemandData) cacheLookupQueue.popTail();
		}
		while (onDemandData != null) {
			waiting = true;
			byte cachedData[] = null;
			if (clientInstance.cacheStores[0] != null) {
				cachedData = clientInstance.cacheStores[onDemandData.dataType + 1].readFile(onDemandData.ID);
			}
			if (!matchesVersionAndCrc32(versions[onDemandData.dataType][onDemandData.ID], crcs[onDemandData.dataType][onDemandData.ID], cachedData)) {
				cachedData = null;
			}
			synchronized (cacheLookupQueue) {
				if (cachedData == null) {
					cacheMissQueue.insertHead(onDemandData);
				} else {
					onDemandData.buffer = cachedData;
					synchronized (completedRequests) {
						completedRequests.insertHead(onDemandData);
					}
				}
				onDemandData = (OnDemandData) cacheLookupQueue.popTail();
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
					sendRequest(queuedRequest);
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
						sendRequest(scannedRequest);
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
		completedRequests = new NodeList();
		gzipInputBuffer = new byte[65000];
		nodeSubList = new NodeSubList();
		versions = new int[4][];
		crcs = new int[4][];
		cacheMissQueue = new NodeList();
		cacheLookupQueue = new NodeList();
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
	public int connectionFailureCount;
	private int[] mapTerrainFileIds;
	private int filesLoaded;
	private boolean running;
	private OutputStream outputStream;
	private int[] mapPreloadFlags;
	private boolean waiting;
	private final NodeList completedRequests;
	private final byte[] gzipInputBuffer;
	private int[] animIndices;
	private final NodeSubList nodeSubList;
	private InputStream inputStream;
	private Socket socket;
	public final int[][] versions;
	public final int[][] crcs;
	private int uncompletedCount;
	private int completedCount;
	private final NodeList cacheMissQueue;
	private OnDemandData current;
	private final NodeList cacheLookupQueue;
	private int[] mapRegionKeys;
	private byte[] modelIndices;
	private int loopCycle;
}
