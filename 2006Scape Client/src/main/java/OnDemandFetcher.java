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
			if (pendingChunkLength == 0 && available >= 6) {
				madeProgress = true;
				for (int headerBytesRead = 0; headerBytesRead < 6; headerBytesRead += inputStream.read(ioBuffer, headerBytesRead, 6 - headerBytesRead)) {
					;
				}
				available = inputStream.available();
				int fileType = ioBuffer[0] & 0xff;
				int fileId = ((ioBuffer[1] & 0xff) << 8) + (ioBuffer[2] & 0xff);
				int fileSize = ((ioBuffer[3] & 0xff) << 8) + (ioBuffer[4] & 0xff);
				int chunkIndex = ioBuffer[5] & 0xff;
				current = null;
				for (OnDemandData inFlightRequest = (OnDemandData) inFlightRequests.firstFromTail(); inFlightRequest != null; inFlightRequest = (OnDemandData) inFlightRequests.nextTowardHead()) {
					if (inFlightRequest.dataType == fileType && inFlightRequest.ID == fileId) {
						current = inFlightRequest;
					}
					if (current != null) {
						inFlightRequest.idleCycles = 0;
					}
				}

				if (current != null) {
					connectionIdleCycles = 0;
					if (fileSize == 0) {
						Signlink.reporterror("Rej: " + fileType + "," + fileId);
						current.buffer = null;
						if (current.urgent) {
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
				chunkStartOffset = chunkIndex * 500;
				pendingChunkLength = 500;
				if (pendingChunkLength > fileSize - chunkIndex * 500) {
					pendingChunkLength = fileSize - chunkIndex * 500;
				}
			}
			if (pendingChunkLength > 0 && available >= pendingChunkLength) {
				madeProgress = true;
				byte destinationBuffer[] = ioBuffer;
				int destinationOffset = 0;
				if (current != null) {
					destinationBuffer = current.buffer;
					destinationOffset = chunkStartOffset;
				}
				for (int bytesRead = 0; bytesRead < pendingChunkLength; bytesRead += inputStream.read(destinationBuffer, bytesRead + destinationOffset, pendingChunkLength - bytesRead)) {
					;
				}
				if (pendingChunkLength + chunkStartOffset >= destinationBuffer.length && current != null) {
					if (clientInstance.cacheStores[0] != null) {
						clientInstance.cacheStores[current.dataType + 1].writeFile(destinationBuffer.length, destinationBuffer, current.ID);
					}
					if (!current.urgent && current.dataType == 3) {
						current.urgent = true;
						current.dataType = 93;
					}
					if (current.urgent) {
						synchronized (completedRequests) {
							completedRequests.insertHead(current);
						}
					} else {
						current.unlink();
					}
				}
				pendingChunkLength = 0;
			}
		} catch (IOException ioexception) {
			try {
				socket.close();
			} catch (Exception _ex) {
			}
			socket = null;
			inputStream = null;
			outputStream = null;
			pendingChunkLength = 0;
		}
	}

	public void start(StreamLoader streamLoader, Game client) {
		String versionNames[] = {"model_version", "anim_version", "midi_version", "map_version"};
		for (int tableIndex = 0; tableIndex < 4; tableIndex++) {
			byte versionData[] = streamLoader.getDataForName(versionNames[tableIndex]);
			int versionCount = versionData.length / 2;
			Stream versionStream = new Stream(versionData);
			versions[tableIndex] = new int[versionCount];
			extraFilePriorities[tableIndex] = new byte[versionCount];
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

	public int getOutstandingRequestCount() {
		synchronized (outstandingRequests) {
			return outstandingRequests.getNodeCount();
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

				connectionIdleCycles = 0;
			}
			ioBuffer[0] = (byte) request.dataType;
			ioBuffer[1] = (byte) (request.ID >> 8);
			ioBuffer[2] = (byte) request.ID;
			if (request.urgent) {
				ioBuffer[3] = 2;
			} else if (!clientInstance.loggedIn) {
				ioBuffer[3] = 1;
			} else {
				ioBuffer[3] = 0;
			}
			outputStream.write(ioBuffer, 0, 4);
			cyclesSinceLastWrite = 0;
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
		pendingChunkLength = 0;
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
		synchronized (outstandingRequests) {
			for (OnDemandData existingRequest = (OnDemandData) outstandingRequests.firstFromTail(); existingRequest != null; existingRequest = (OnDemandData) outstandingRequests.nextTowardHead()) {
				if (existingRequest.dataType == dataType && existingRequest.ID == fileId) {
					return;
				}
			}

			OnDemandData newRequest = new OnDemandData();
			newRequest.dataType = dataType;
			newRequest.ID = fileId;
			newRequest.urgent = true;
			synchronized (cacheLookupQueue) {
				cacheLookupQueue.insertHead(newRequest);
			}
			outstandingRequests.insertHead(newRequest);
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
				madeProgress = true;
				for (int pollPass = 0; pollPass < 100; pollPass++) {
					if (!madeProgress) {
						break;
					}
					madeProgress = false;
					checkReceived();
					handleFailed();
					if (urgentInFlightCount == 0 && pollPass >= 5) {
						break;
					}
					dispatchExtraFileRequests();
					if (inputStream != null) {
						readData();
					}
				}

				boolean hasOutstandingRequest = false;
				for (OnDemandData request = (OnDemandData) inFlightRequests.firstFromTail(); request != null; request = (OnDemandData) inFlightRequests.nextTowardHead()) {
					if (request.urgent) {
						hasOutstandingRequest = true;
						request.idleCycles++;
						if (request.idleCycles > 50) {
							request.idleCycles = 0;
							sendRequest(request);
						}
					}
				}

				if (!hasOutstandingRequest) {
					for (OnDemandData request = (OnDemandData) inFlightRequests.firstFromTail(); request != null; request = (OnDemandData) inFlightRequests.nextTowardHead()) {
						hasOutstandingRequest = true;
						request.idleCycles++;
						if (request.idleCycles > 50) {
							request.idleCycles = 0;
							sendRequest(request);
						}
					}

				}
				if (hasOutstandingRequest) {
					connectionIdleCycles++;
					if (connectionIdleCycles > 750) {
						try {
							socket.close();
						} catch (Exception _ex) {
						}
						socket = null;
						inputStream = null;
						outputStream = null;
						pendingChunkLength = 0;
					}
				} else {
					connectionIdleCycles = 0;
					statusString = "";
				}
				if (clientInstance.loggedIn && socket != null && outputStream != null && (highestPendingPriority > 0 || clientInstance.cacheStores[0] == null)) {
					cyclesSinceLastWrite++;
					if (cyclesSinceLastWrite > 500) {
						cyclesSinceLastWrite = 0;
						ioBuffer[0] = 0;
						ioBuffer[1] = 0;
						ioBuffer[2] = 0;
						ioBuffer[3] = 10;
						try {
							outputStream.write(ioBuffer, 0, 4);
						} catch (IOException _ex) {
							connectionIdleCycles = 5000;
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
		if (extraFilePriorities[dataType][fileId] == 0) {
			return;
		}
		if (highestPendingPriority == 0) {
			return;
		}
		OnDemandData extraRequest = new OnDemandData();
		extraRequest.dataType = dataType;
		extraRequest.ID = fileId;
		extraRequest.urgent = false;
		synchronized (extraFileQueue) {
			extraFileQueue.insertHead(extraRequest);
		}
	}

	public OnDemandData getNextNode() {
		OnDemandData completedRequest;
		synchronized (completedRequests) {
			completedRequest = (OnDemandData) completedRequests.popTail();
		}
		if (completedRequest == null) {
			return null;
		}
		synchronized (outstandingRequests) {
			completedRequest.unlinkSub();
		}
		if (completedRequest.buffer == null) {
			return completedRequest;
		}
		int inflatedLength = 0;
		try {
			GZIPInputStream gzipInput = new GZIPInputStream(new ByteArrayInputStream(completedRequest.buffer));
			do {
				if (inflatedLength == gzipInputBuffer.length) {
					throw new RuntimeException("buffer overflow!");
				}
				int bytesRead = gzipInput.read(gzipInputBuffer, inflatedLength, gzipInputBuffer.length - inflatedLength);
				if (bytesRead == -1) {
					break;
				}
				inflatedLength += bytesRead;
			} while (true);
		} catch (IOException _ex) {
			throw new RuntimeException("error unzipping");
		}
		completedRequest.buffer = new byte[inflatedLength];
		System.arraycopy(gzipInputBuffer, 0, completedRequest.buffer, 0, inflatedLength);

		return completedRequest;
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
		extraFilePriorities[dataType][fileId] = priority;
		if (priority > highestPendingPriority) {
			highestPendingPriority = priority;
		}
		extraFileMarkCount++;
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
		urgentInFlightCount = 0;
		extraInFlightCount = 0;
		for (OnDemandData inFlightRequest = (OnDemandData) inFlightRequests.firstFromTail(); inFlightRequest != null; inFlightRequest = (OnDemandData) inFlightRequests.nextTowardHead()) {
			if (inFlightRequest.urgent) {
				urgentInFlightCount++;
			} else {
				extraInFlightCount++;
			}
		}

		while (urgentInFlightCount < 10) {
			OnDemandData missedRequest = (OnDemandData) cacheMissQueue.popTail();
			if (missedRequest == null) {
				break;
			}
			if (extraFilePriorities[missedRequest.dataType][missedRequest.ID] != 0) {
				extraFileRequestCount++;
			}
			extraFilePriorities[missedRequest.dataType][missedRequest.ID] = 0;
			inFlightRequests.insertHead(missedRequest);
			urgentInFlightCount++;
			sendRequest(missedRequest);
			madeProgress = true;
		}
	}

	public void clearExtraFileQueue() {
		synchronized (extraFileQueue) {
			extraFileQueue.removeAll();
		}
	}

	private void checkReceived() {
		OnDemandData lookupRequest;
		synchronized (cacheLookupQueue) {
			lookupRequest = (OnDemandData) cacheLookupQueue.popTail();
		}
		while (lookupRequest != null) {
			madeProgress = true;
			byte cachedData[] = null;
			if (clientInstance.cacheStores[0] != null) {
				cachedData = clientInstance.cacheStores[lookupRequest.dataType + 1].readFile(lookupRequest.ID);
			}
			if (!matchesVersionAndCrc32(versions[lookupRequest.dataType][lookupRequest.ID], crcs[lookupRequest.dataType][lookupRequest.ID], cachedData)) {
				cachedData = null;
			}
			synchronized (cacheLookupQueue) {
				if (cachedData == null) {
					cacheMissQueue.insertHead(lookupRequest);
				} else {
					lookupRequest.buffer = cachedData;
					synchronized (completedRequests) {
						completedRequests.insertHead(lookupRequest);
					}
				}
				lookupRequest = (OnDemandData) cacheLookupQueue.popTail();
			}
		}
	}

	private void dispatchExtraFileRequests() {
		while (urgentInFlightCount == 0 && extraInFlightCount < 10) {
			if (highestPendingPriority == 0) {
				break;
			}
			OnDemandData queuedRequest;
			synchronized (extraFileQueue) {
				queuedRequest = (OnDemandData) extraFileQueue.popTail();
			}
			while (queuedRequest != null) {
				if (extraFilePriorities[queuedRequest.dataType][queuedRequest.ID] != 0) {
					extraFilePriorities[queuedRequest.dataType][queuedRequest.ID] = 0;
					inFlightRequests.insertHead(queuedRequest);
					sendRequest(queuedRequest);
					madeProgress = true;
					if (extraFileRequestCount < extraFileMarkCount) {
						extraFileRequestCount++;
					}
					statusString = "Loading extra files - " + extraFileRequestCount * 100 / extraFileMarkCount + "%";
					extraInFlightCount++;
					if (extraInFlightCount == 10) {
						return;
					}
				}
				synchronized (extraFileQueue) {
					queuedRequest = (OnDemandData) extraFileQueue.popTail();
				}
			}
			for (int dataType = 0; dataType < 4; dataType++) {
				byte typePriorities[] = extraFilePriorities[dataType];
				int fileCount = typePriorities.length;
				for (int fileId = 0; fileId < fileCount; fileId++) {
					if (typePriorities[fileId] == highestPendingPriority) {
						typePriorities[fileId] = 0;
						OnDemandData scannedRequest = new OnDemandData();
						scannedRequest.dataType = dataType;
						scannedRequest.ID = fileId;
						scannedRequest.urgent = false;
						inFlightRequests.insertHead(scannedRequest);
						sendRequest(scannedRequest);
						madeProgress = true;
						if (extraFileRequestCount < extraFileMarkCount) {
							extraFileRequestCount++;
						}
						statusString = "Loading extra files - " + extraFileRequestCount * 100 / extraFileMarkCount + "%";
						extraInFlightCount++;
						if (extraInFlightCount == 10) {
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
		inFlightRequests = new NodeList();
		statusString = "";
		crc32 = new CRC32();
		ioBuffer = new byte[500];
		extraFilePriorities = new byte[4][];
		extraFileQueue = new NodeList();
		running = true;
		madeProgress = false;
		completedRequests = new NodeList();
		gzipInputBuffer = new byte[65000];
		outstandingRequests = new NodeSubList();
		versions = new int[4][];
		crcs = new int[4][];
		cacheMissQueue = new NodeList();
		cacheLookupQueue = new NodeList();
	}

	private int extraFileMarkCount;
	private final NodeList inFlightRequests;
	private int highestPendingPriority;
	public String statusString;
	private int cyclesSinceLastWrite;
	private long openSocketTime;
	private int[] mapObjectFileIds;
	private final CRC32 crc32;
	private final byte[] ioBuffer;
	public int onDemandCycle;
	private final byte[][] extraFilePriorities;
	private Game clientInstance;
	private final NodeList extraFileQueue;
	private int chunkStartOffset;
	private int pendingChunkLength;
	int[] songPreloadFlags;
	public int connectionFailureCount;
	private int[] mapTerrainFileIds;
	private int extraFileRequestCount;
	private boolean running;
	private OutputStream outputStream;
	private int[] mapPreloadFlags;
	private boolean madeProgress;
	private final NodeList completedRequests;
	private final byte[] gzipInputBuffer;
	private int[] animIndices;
	private final NodeSubList outstandingRequests;
	private InputStream inputStream;
	private Socket socket;
	public final int[][] versions;
	public final int[][] crcs;
	private int urgentInFlightCount;
	private int extraInFlightCount;
	private final NodeList cacheMissQueue;
	private OnDemandData current;
	private final NodeList cacheLookupQueue;
	private int[] mapRegionKeys;
	private byte[] modelIndices;
	private int connectionIdleCycles;
}
