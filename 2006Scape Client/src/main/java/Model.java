// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

public final class Model extends Animable {

	public static void nullLoader() {
		modelHeaders = null;
		faceNeedsHorizontalClip = null;
		faceNearPlaneClipped = null;
		screenX = null;
		screenY = null;
		vertexDepth = null;
		vertexCameraX = null;
		vertexCameraY = null;
		vertexCameraZ = null;
		depthBucketFaceCounts = null;
		depthBucketFaces = null;
		priorityFaceCounts = null;
		priorityFaces = null;
		priority10Depths = null;
		priority11Depths = null;
		priorityDepthSums = null;
		sineTable16 = null;
		cosineTable16 = null;
		hslToRgbPalette = null;
		reciprocalTable16 = null;
	}

	public static void initModelHeaders(int i, OnDemandFetcherParent onDemandFetcherParent) {
		modelHeaders = new ModelHeader[i];
		modelRequester = onDemandFetcherParent;
	}

	public static void loadModelHeader(byte data[], int modelId) {
		if (data == null) {
			ModelHeader header = modelHeaders[modelId] = new ModelHeader();
			header.vertexCount = 0;
			header.faceCount = 0;
			header.texturedFaceCount = 0;
			return;
		}
		Stream stream = new Stream(data);
		stream.currentOffset = data.length - 18;
		ModelHeader header = modelHeaders[modelId] = new ModelHeader();
		header.data = data;
		header.vertexCount = stream.readUnsignedShortBigEndian();
		header.faceCount = stream.readUnsignedShortBigEndian();
		header.texturedFaceCount = stream.readUnsignedByte();
		int hasFaceRenderFlags = stream.readUnsignedByte();
		int facePriorityMarker = stream.readUnsignedByte();
		int hasFaceAlphas = stream.readUnsignedByte();
		int hasFaceGroupIds = stream.readUnsignedByte();
		int hasVertexGroupIds = stream.readUnsignedByte();
		int xDataLength = stream.readUnsignedShortBigEndian();
		int yDataLength = stream.readUnsignedShortBigEndian();
		int zDataLength = stream.readUnsignedShortBigEndian();
		int faceVertexDataLength = stream.readUnsignedShortBigEndian();
		int offset = 0;
		header.vertexFlagsOffset = offset;
		offset += header.vertexCount;
		header.faceTypesOffset = offset;
		offset += header.faceCount;
		header.facePrioritiesOffset = offset;
		if (facePriorityMarker == 255) {
			offset += header.faceCount;
		} else {
			header.facePrioritiesOffset = -facePriorityMarker - 1;
		}
		header.faceGroupIdsOffset = offset;
		if (hasFaceGroupIds == 1) {
			offset += header.faceCount;
		} else {
			header.faceGroupIdsOffset = -1;
		}
		header.faceRenderFlagsOffset = offset;
		if (hasFaceRenderFlags == 1) {
			offset += header.faceCount;
		} else {
			header.faceRenderFlagsOffset = -1;
		}
		header.vertexGroupIdsOffset = offset;
		if (hasVertexGroupIds == 1) {
			offset += header.vertexCount;
		} else {
			header.vertexGroupIdsOffset = -1;
		}
		header.faceAlphasOffset = offset;
		if (hasFaceAlphas == 1) {
			offset += header.faceCount;
		} else {
			header.faceAlphasOffset = -1;
		}
		header.faceVertexDataOffset = offset;
		offset += faceVertexDataLength;
		header.faceColoursOffset = offset;
		offset += header.faceCount * 2;
		header.texturedFaceOffset = offset;
		offset += header.texturedFaceCount * 6;
		header.vertexXOffset = offset;
		offset += xDataLength;
		header.vertexYOffset = offset;
		offset += yDataLength;
		header.vertexZOffset = offset;
		offset += zDataLength;
	}

	public static void unloadModelHeader(int modelId) {
		modelHeaders[modelId] = null;
	}

	public static Model getModel(int modelId) {
		if (modelHeaders == null) {
			return null;
		}
		ModelHeader header = modelHeaders.length < modelId  ? null : modelHeaders[modelId];
		if (header == null) {
			modelRequester.requestModel(modelId);
			return null;
		} else {
			return new Model(modelId);
		}
	}

	public static boolean isModelLoaded(int modelId) {
		if (modelHeaders == null) {
			return false;
		}
		ModelHeader header = modelHeaders[modelId];
		if (header == null) {
			modelRequester.requestModel(modelId);
			return false;
		} else {
			return true;
		}
	}

	private Model() {
		pickByBounds = false;
	}

	private Model(int modelId) {
		pickByBounds = false;
		ModelHeader header = modelHeaders[modelId];
		vertexCount = header.vertexCount;
		faceCount = header.faceCount;
		texturedFaceCount = header.texturedFaceCount;
		vertexX = new int[vertexCount];
		vertexY = new int[vertexCount];
		vertexZ = new int[vertexCount];
		faceVertexA = new int[faceCount];
		faceVertexB = new int[faceCount];
		faceVertexC = new int[faceCount];
		texturedFaceVertexA = new int[texturedFaceCount];
		texturedFaceVertexB = new int[texturedFaceCount];
		texturedFaceVertexC = new int[texturedFaceCount];
		if (header.vertexGroupIdsOffset >= 0) {
			vertexGroupIds = new int[vertexCount];
		}
		if (header.faceRenderFlagsOffset >= 0) {
			faceRenderFlags = new int[faceCount];
		}
		if (header.facePrioritiesOffset >= 0) {
			facePriorities = new int[faceCount];
		} else {
			uniformFacePriority = -header.facePrioritiesOffset - 1;
		}
		if (header.faceAlphasOffset >= 0) {
			faceAlphas = new int[faceCount];
		}
		if (header.faceGroupIdsOffset >= 0) {
			faceGroupIds = new int[faceCount];
		}
		faceColors = new int[faceCount];
		Stream stream = new Stream(header.data);
		stream.currentOffset = header.vertexFlagsOffset;
		Stream stream_1 = new Stream(header.data);
		stream_1.currentOffset = header.vertexXOffset;
		Stream stream_2 = new Stream(header.data);
		stream_2.currentOffset = header.vertexYOffset;
		Stream stream_3 = new Stream(header.data);
		stream_3.currentOffset = header.vertexZOffset;
		Stream stream_4 = new Stream(header.data);
		stream_4.currentOffset = header.vertexGroupIdsOffset;
		int previousX = 0;
		int previousY = 0;
		int previousZ = 0;
		for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
			int vertexFlags = stream.readUnsignedByte();
			int deltaX = 0;
			if ((vertexFlags & 1) != 0) {
				deltaX = stream_1.readSignedSmart();
			}
			int deltaY = 0;
			if ((vertexFlags & 2) != 0) {
				deltaY = stream_2.readSignedSmart();
			}
			int deltaZ = 0;
			if ((vertexFlags & 4) != 0) {
				deltaZ = stream_3.readSignedSmart();
			}
			vertexX[vertexIndex] = previousX + deltaX;
			vertexY[vertexIndex] = previousY + deltaY;
			vertexZ[vertexIndex] = previousZ + deltaZ;
			previousX = vertexX[vertexIndex];
			previousY = vertexY[vertexIndex];
			previousZ = vertexZ[vertexIndex];
			if (vertexGroupIds != null) {
				vertexGroupIds[vertexIndex] = stream_4.readUnsignedByte();
			}
		}

		stream.currentOffset = header.faceColoursOffset;
		stream_1.currentOffset = header.faceRenderFlagsOffset;
		stream_2.currentOffset = header.facePrioritiesOffset;
		stream_3.currentOffset = header.faceAlphasOffset;
		stream_4.currentOffset = header.faceGroupIdsOffset;
		for (int faceIndex = 0; faceIndex < faceCount; faceIndex++) {
			faceColors[faceIndex] = stream.readUnsignedShortBigEndian();
			if (faceRenderFlags != null) {
				faceRenderFlags[faceIndex] = stream_1.readUnsignedByte();
			}
			if (facePriorities != null) {
				facePriorities[faceIndex] = stream_2.readUnsignedByte();
			}
			if (faceAlphas != null) {
				faceAlphas[faceIndex] = stream_3.readUnsignedByte();
			}
			if (faceGroupIds != null) {
				faceGroupIds[faceIndex] = stream_4.readUnsignedByte();
			}
		}

		stream.currentOffset = header.faceVertexDataOffset;
		stream_1.currentOffset = header.faceTypesOffset;
		int vertexA = 0;
		int vertexB = 0;
		int vertexC = 0;
		int previousVertex = 0;
		for (int faceIndex = 0; faceIndex < faceCount; faceIndex++) {
			int faceType = stream_1.readUnsignedByte();
			if (faceType == 1) {
				vertexA = stream.readSignedSmart() + previousVertex;
				previousVertex = vertexA;
				vertexB = stream.readSignedSmart() + previousVertex;
				previousVertex = vertexB;
				vertexC = stream.readSignedSmart() + previousVertex;
				previousVertex = vertexC;
				faceVertexA[faceIndex] = vertexA;
				faceVertexB[faceIndex] = vertexB;
				faceVertexC[faceIndex] = vertexC;
			}
			if (faceType == 2) {
				vertexB = vertexC;
				vertexC = stream.readSignedSmart() + previousVertex;
				previousVertex = vertexC;
				faceVertexA[faceIndex] = vertexA;
				faceVertexB[faceIndex] = vertexB;
				faceVertexC[faceIndex] = vertexC;
			}
			if (faceType == 3) {
				vertexA = vertexC;
				vertexC = stream.readSignedSmart() + previousVertex;
				previousVertex = vertexC;
				faceVertexA[faceIndex] = vertexA;
				faceVertexB[faceIndex] = vertexB;
				faceVertexC[faceIndex] = vertexC;
			}
			if (faceType == 4) {
				int swapTemp = vertexA;
				vertexA = vertexB;
				vertexB = swapTemp;
				vertexC = stream.readSignedSmart() + previousVertex;
				previousVertex = vertexC;
				faceVertexA[faceIndex] = vertexA;
				faceVertexB[faceIndex] = vertexB;
				faceVertexC[faceIndex] = vertexC;
			}
		}

		stream.currentOffset = header.texturedFaceOffset;
		for (int texturedFaceIndex = 0; texturedFaceIndex < texturedFaceCount; texturedFaceIndex++) {
			texturedFaceVertexA[texturedFaceIndex] = stream.readUnsignedShortBigEndian();
			texturedFaceVertexB[texturedFaceIndex] = stream.readUnsignedShortBigEndian();
			texturedFaceVertexC[texturedFaceIndex] = stream.readUnsignedShortBigEndian();
		}

	}

	public Model(int partCount, Model parts[]) {
		pickByBounds = false;
		boolean needsFaceRenderFlags = false;
		boolean needsFacePriorities = false;
		boolean needsFaceAlpha = false;
		boolean needsFaceGroupIds = false;
		vertexCount = 0;
		faceCount = 0;
		texturedFaceCount = 0;
		uniformFacePriority = -1;
		for (int partIndex = 0; partIndex < partCount; partIndex++) {
			Model sourcePart = parts[partIndex];
			if (sourcePart != null) {
				vertexCount += sourcePart.vertexCount;
				faceCount += sourcePart.faceCount;
				texturedFaceCount += sourcePart.texturedFaceCount;
				needsFaceRenderFlags |= sourcePart.faceRenderFlags != null;
				if (sourcePart.facePriorities != null) {
					needsFacePriorities = true;
				} else {
					if (uniformFacePriority == -1) {
						uniformFacePriority = sourcePart.uniformFacePriority;
					}
					if (uniformFacePriority != sourcePart.uniformFacePriority) {
						needsFacePriorities = true;
					}
				}
				needsFaceAlpha |= sourcePart.faceAlphas != null;
				needsFaceGroupIds |= sourcePart.faceGroupIds != null;
			}
		}

		vertexX = new int[vertexCount];
		vertexY = new int[vertexCount];
		vertexZ = new int[vertexCount];
		vertexGroupIds = new int[vertexCount];
		faceVertexA = new int[faceCount];
		faceVertexB = new int[faceCount];
		faceVertexC = new int[faceCount];
		texturedFaceVertexA = new int[texturedFaceCount];
		texturedFaceVertexB = new int[texturedFaceCount];
		texturedFaceVertexC = new int[texturedFaceCount];
		if (needsFaceRenderFlags) {
			faceRenderFlags = new int[faceCount];
		}
		if (needsFacePriorities) {
			facePriorities = new int[faceCount];
		}
		if (needsFaceAlpha) {
			faceAlphas = new int[faceCount];
		}
		if (needsFaceGroupIds) {
			faceGroupIds = new int[faceCount];
		}
		faceColors = new int[faceCount];
		vertexCount = 0;
		faceCount = 0;
		texturedFaceCount = 0;
		int textureTriangleOffset = 0;
		for (int partIndex = 0; partIndex < partCount; partIndex++) {
			Model sourcePart = parts[partIndex];
			if (sourcePart != null) {
				for (int faceIndex = 0; faceIndex < sourcePart.faceCount; faceIndex++) {
					if (needsFaceRenderFlags) {
						if (sourcePart.faceRenderFlags == null) {
							faceRenderFlags[faceCount] = 0;
						} else {
							int renderFlags = sourcePart.faceRenderFlags[faceIndex];
							if ((renderFlags & 2) == 2) {
								renderFlags += textureTriangleOffset << 2;
							}
							faceRenderFlags[faceCount] = renderFlags;
						}
					}
					if (needsFacePriorities) {
						if (sourcePart.facePriorities == null) {
							facePriorities[faceCount] = sourcePart.uniformFacePriority;
						} else {
							facePriorities[faceCount] = sourcePart.facePriorities[faceIndex];
						}
					}
					if (needsFaceAlpha) {
						if (sourcePart.faceAlphas == null) {
							faceAlphas[faceCount] = 0;
						} else {
							faceAlphas[faceCount] = sourcePart.faceAlphas[faceIndex];
						}
					}
					if (needsFaceGroupIds && sourcePart.faceGroupIds != null) {
						faceGroupIds[faceCount] = sourcePart.faceGroupIds[faceIndex];
					}
					faceColors[faceCount] = sourcePart.faceColors[faceIndex];
					faceVertexA[faceCount] = getOrAddVertex(sourcePart, sourcePart.faceVertexA[faceIndex]);
					faceVertexB[faceCount] = getOrAddVertex(sourcePart, sourcePart.faceVertexB[faceIndex]);
					faceVertexC[faceCount] = getOrAddVertex(sourcePart, sourcePart.faceVertexC[faceIndex]);
					faceCount++;
				}

				for (int textureTriangleIndex = 0; textureTriangleIndex < sourcePart.texturedFaceCount; textureTriangleIndex++) {
					texturedFaceVertexA[texturedFaceCount] = getOrAddVertex(sourcePart, sourcePart.texturedFaceVertexA[textureTriangleIndex]);
					texturedFaceVertexB[texturedFaceCount] = getOrAddVertex(sourcePart, sourcePart.texturedFaceVertexB[textureTriangleIndex]);
					texturedFaceVertexC[texturedFaceCount] = getOrAddVertex(sourcePart, sourcePart.texturedFaceVertexC[textureTriangleIndex]);
					texturedFaceCount++;
				}

				textureTriangleOffset += sourcePart.texturedFaceCount;
			}
		}

	}

	public Model(Model parts[]) {
		int partCount = 2;// was parameter
		pickByBounds = false;
		boolean needsFaceRenderFlags = false;
		boolean needsFacePriorities = false;
		boolean needsFaceAlpha = false;
		boolean needsFaceValues = false;
		vertexCount = 0;
		faceCount = 0;
		texturedFaceCount = 0;
		uniformFacePriority = -1;
		for (int partIndex = 0; partIndex < partCount; partIndex++) {
			Model sourcePart = parts[partIndex];
			if (sourcePart != null) {
				vertexCount += sourcePart.vertexCount;
				faceCount += sourcePart.faceCount;
				texturedFaceCount += sourcePart.texturedFaceCount;
				needsFaceRenderFlags |= sourcePart.faceRenderFlags != null;
				if (sourcePart.facePriorities != null) {
					needsFacePriorities = true;
				} else {
					if (uniformFacePriority == -1) {
						uniformFacePriority = sourcePart.uniformFacePriority;
					}
					if (uniformFacePriority != sourcePart.uniformFacePriority) {
						needsFacePriorities = true;
					}
				}
				needsFaceAlpha |= sourcePart.faceAlphas != null;
				needsFaceValues |= sourcePart.faceColors != null;
			}
		}

		vertexX = new int[vertexCount];
		vertexY = new int[vertexCount];
		vertexZ = new int[vertexCount];
		faceVertexA = new int[faceCount];
		faceVertexB = new int[faceCount];
		faceVertexC = new int[faceCount];
		faceShadeA = new int[faceCount];
		faceShadeB = new int[faceCount];
		faceShadeC = new int[faceCount];
		texturedFaceVertexA = new int[texturedFaceCount];
		texturedFaceVertexB = new int[texturedFaceCount];
		texturedFaceVertexC = new int[texturedFaceCount];
		if (needsFaceRenderFlags) {
			faceRenderFlags = new int[faceCount];
		}
		if (needsFacePriorities) {
			facePriorities = new int[faceCount];
		}
		if (needsFaceAlpha) {
			faceAlphas = new int[faceCount];
		}
		if (needsFaceValues) {
			faceColors = new int[faceCount];
		}
		vertexCount = 0;
		faceCount = 0;
		texturedFaceCount = 0;
		int textureTriangleOffset = 0;
		for (int partIndex = 0; partIndex < partCount; partIndex++) {
			Model sourcePart = parts[partIndex];
			if (sourcePart != null) {
				int vertexOffset = vertexCount;
				for (int vertexIndex = 0; vertexIndex < sourcePart.vertexCount; vertexIndex++) {
					vertexX[vertexCount] = sourcePart.vertexX[vertexIndex];
					vertexY[vertexCount] = sourcePart.vertexY[vertexIndex];
					vertexZ[vertexCount] = sourcePart.vertexZ[vertexIndex];
					vertexCount++;
				}

				for (int faceIndex = 0; faceIndex < sourcePart.faceCount; faceIndex++) {
					faceVertexA[faceCount] = sourcePart.faceVertexA[faceIndex] + vertexOffset;
					faceVertexB[faceCount] = sourcePart.faceVertexB[faceIndex] + vertexOffset;
					faceVertexC[faceCount] = sourcePart.faceVertexC[faceIndex] + vertexOffset;
					faceShadeA[faceCount] = sourcePart.faceShadeA[faceIndex];
					faceShadeB[faceCount] = sourcePart.faceShadeB[faceIndex];
					faceShadeC[faceCount] = sourcePart.faceShadeC[faceIndex];
					if (needsFaceRenderFlags) {
						if (sourcePart.faceRenderFlags == null) {
							faceRenderFlags[faceCount] = 0;
						} else {
							int renderFlags = sourcePart.faceRenderFlags[faceIndex];
							if ((renderFlags & 2) == 2) {
								renderFlags += textureTriangleOffset << 2;
							}
							faceRenderFlags[faceCount] = renderFlags;
						}
					}
					if (needsFacePriorities) {
						if (sourcePart.facePriorities == null) {
							facePriorities[faceCount] = sourcePart.uniformFacePriority;
						} else {
							facePriorities[faceCount] = sourcePart.facePriorities[faceIndex];
						}
					}
					if (needsFaceAlpha) {
						if (sourcePart.faceAlphas == null) {
							faceAlphas[faceCount] = 0;
						} else {
							faceAlphas[faceCount] = sourcePart.faceAlphas[faceIndex];
						}
					}
					if (needsFaceValues && sourcePart.faceColors != null) {
						faceColors[faceCount] = sourcePart.faceColors[faceIndex];
					}
					faceCount++;
				}

				for (int textureTriangleIndex = 0; textureTriangleIndex < sourcePart.texturedFaceCount; textureTriangleIndex++) {
					texturedFaceVertexA[texturedFaceCount] = sourcePart.texturedFaceVertexA[textureTriangleIndex] + vertexOffset;
					texturedFaceVertexB[texturedFaceCount] = sourcePart.texturedFaceVertexB[textureTriangleIndex] + vertexOffset;
					texturedFaceVertexC[texturedFaceCount] = sourcePart.texturedFaceVertexC[textureTriangleIndex] + vertexOffset;
					texturedFaceCount++;
				}

				textureTriangleOffset += sourcePart.texturedFaceCount;
			}
		}

		calculateBounds();
	}

	public Model(boolean shareFaceValues, boolean shareFaceAlpha, boolean shareVertices, Model sourceModel) {
		pickByBounds = false;
		vertexCount = sourceModel.vertexCount;
		faceCount = sourceModel.faceCount;
		texturedFaceCount = sourceModel.texturedFaceCount;
		if (shareVertices) {
			vertexX = sourceModel.vertexX;
			vertexY = sourceModel.vertexY;
			vertexZ = sourceModel.vertexZ;
		} else {
			vertexX = new int[vertexCount];
			vertexY = new int[vertexCount];
			vertexZ = new int[vertexCount];
			for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
				vertexX[vertexIndex] = sourceModel.vertexX[vertexIndex];
				vertexY[vertexIndex] = sourceModel.vertexY[vertexIndex];
				vertexZ[vertexIndex] = sourceModel.vertexZ[vertexIndex];
			}

		}
		if (shareFaceValues) {
			faceColors = sourceModel.faceColors;
		} else {
			faceColors = new int[faceCount];
			System.arraycopy(sourceModel.faceColors, 0, faceColors, 0, faceCount);

		}
		if (shareFaceAlpha) {
			faceAlphas = sourceModel.faceAlphas;
		} else {
			faceAlphas = new int[faceCount];
			if (sourceModel.faceAlphas == null) {
				for (int faceIndex = 0; faceIndex < faceCount; faceIndex++) {
					faceAlphas[faceIndex] = 0;
				}

			} else {
				System.arraycopy(sourceModel.faceAlphas, 0, faceAlphas, 0, faceCount);

			}
		}
		vertexGroupIds = sourceModel.vertexGroupIds;
		faceGroupIds = sourceModel.faceGroupIds;
		faceRenderFlags = sourceModel.faceRenderFlags;
		faceVertexA = sourceModel.faceVertexA;
		faceVertexB = sourceModel.faceVertexB;
		faceVertexC = sourceModel.faceVertexC;
		facePriorities = sourceModel.facePriorities;
		uniformFacePriority = sourceModel.uniformFacePriority;
		texturedFaceVertexA = sourceModel.texturedFaceVertexA;
		texturedFaceVertexB = sourceModel.texturedFaceVertexB;
		texturedFaceVertexC = sourceModel.texturedFaceVertexC;
	}

	public Model(boolean copyVertexY, boolean copyLightingData, Model sourceModel) {
		pickByBounds = false;
		vertexCount = sourceModel.vertexCount;
		faceCount = sourceModel.faceCount;
		texturedFaceCount = sourceModel.texturedFaceCount;
		if (copyVertexY) {
			vertexY = new int[vertexCount];
			System.arraycopy(sourceModel.vertexY, 0, vertexY, 0, vertexCount);

		} else {
			vertexY = sourceModel.vertexY;
		}
		if (copyLightingData) {
			faceShadeA = new int[faceCount];
			faceShadeB = new int[faceCount];
			faceShadeC = new int[faceCount];
			for (int faceIndex = 0; faceIndex < faceCount; faceIndex++) {
				faceShadeA[faceIndex] = sourceModel.faceShadeA[faceIndex];
				faceShadeB[faceIndex] = sourceModel.faceShadeB[faceIndex];
				faceShadeC[faceIndex] = sourceModel.faceShadeC[faceIndex];
			}

			faceRenderFlags = new int[faceCount];
			if (sourceModel.faceRenderFlags == null) {
				for (int faceIndex = 0; faceIndex < faceCount; faceIndex++) {
					faceRenderFlags[faceIndex] = 0;
				}

			} else {
				System.arraycopy(sourceModel.faceRenderFlags, 0, faceRenderFlags, 0, faceCount);

			}
			super.vertexNormals = new VertexNormal[vertexCount];
			for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
				VertexNormal copiedNormal = super.vertexNormals[vertexIndex] = new VertexNormal();
				VertexNormal sourceNormal = sourceModel.vertexNormals[vertexIndex];
				copiedNormal.normalX = sourceNormal.normalX;
				copiedNormal.normalY = sourceNormal.normalY;
				copiedNormal.normalZ = sourceNormal.normalZ;
				copiedNormal.faceCount = sourceNormal.faceCount;
			}

			originalVertexNormals = sourceModel.originalVertexNormals;
		} else {
			faceShadeA = sourceModel.faceShadeA;
			faceShadeB = sourceModel.faceShadeB;
			faceShadeC = sourceModel.faceShadeC;
			faceRenderFlags = sourceModel.faceRenderFlags;
		}
		vertexX = sourceModel.vertexX;
		vertexZ = sourceModel.vertexZ;
		faceColors = sourceModel.faceColors;
		faceAlphas = sourceModel.faceAlphas;
		facePriorities = sourceModel.facePriorities;
		uniformFacePriority = sourceModel.uniformFacePriority;
		faceVertexA = sourceModel.faceVertexA;
		faceVertexB = sourceModel.faceVertexB;
		faceVertexC = sourceModel.faceVertexC;
		texturedFaceVertexA = sourceModel.texturedFaceVertexA;
		texturedFaceVertexB = sourceModel.texturedFaceVertexB;
		texturedFaceVertexC = sourceModel.texturedFaceVertexC;
		super.modelHeight = sourceModel.modelHeight;
		maxY = sourceModel.maxY;
		radiusXZ = sourceModel.radiusXZ;
		depthBucketOffset = sourceModel.depthBucketOffset;
		depthBucketCount = sourceModel.depthBucketCount;
		minX = sourceModel.minX;
		maxZ = sourceModel.maxZ;
		minZ = sourceModel.minZ;
		maxX = sourceModel.maxX;
	}

	public void copyIntoAnimationScratch(Model sourceModel, boolean shareFaceAlpha) {
		vertexCount = sourceModel.vertexCount;
		faceCount = sourceModel.faceCount;
		texturedFaceCount = sourceModel.texturedFaceCount;
		if (animationScratchX.length < vertexCount) {
			animationScratchX = new int[vertexCount + 100];
			animationScratchY = new int[vertexCount + 100];
			animationScratchZ = new int[vertexCount + 100];
		}
		vertexX = animationScratchX;
		vertexY = animationScratchY;
		vertexZ = animationScratchZ;
		for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
			vertexX[vertexIndex] = sourceModel.vertexX[vertexIndex];
			vertexY[vertexIndex] = sourceModel.vertexY[vertexIndex];
			vertexZ[vertexIndex] = sourceModel.vertexZ[vertexIndex];
		}

		if (shareFaceAlpha) {
			faceAlphas = sourceModel.faceAlphas;
		} else {
			if (animationScratchAlpha.length < faceCount) {
				animationScratchAlpha = new int[faceCount + 100];
			}
			faceAlphas = animationScratchAlpha;
			if (sourceModel.faceAlphas == null) {
				for (int faceIndex = 0; faceIndex < faceCount; faceIndex++) {
					faceAlphas[faceIndex] = 0;
				}

			} else {
				System.arraycopy(sourceModel.faceAlphas, 0, faceAlphas, 0, faceCount);

			}
		}
		faceRenderFlags = sourceModel.faceRenderFlags;
		faceColors = sourceModel.faceColors;
		facePriorities = sourceModel.facePriorities;
		uniformFacePriority = sourceModel.uniformFacePriority;
		faceGroups = sourceModel.faceGroups;
		vertexGroups = sourceModel.vertexGroups;
		faceVertexA = sourceModel.faceVertexA;
		faceVertexB = sourceModel.faceVertexB;
		faceVertexC = sourceModel.faceVertexC;
		faceShadeA = sourceModel.faceShadeA;
		faceShadeB = sourceModel.faceShadeB;
		faceShadeC = sourceModel.faceShadeC;
		texturedFaceVertexA = sourceModel.texturedFaceVertexA;
		texturedFaceVertexB = sourceModel.texturedFaceVertexB;
		texturedFaceVertexC = sourceModel.texturedFaceVertexC;
	}

	private int getOrAddVertex(Model sourceModel, int sourceVertexIndex) {
		int destinationVertexIndex = -1;
		int pointX = sourceModel.vertexX[sourceVertexIndex];
		int pointY = sourceModel.vertexY[sourceVertexIndex];
		int pointZ = sourceModel.vertexZ[sourceVertexIndex];
		for (int existingVertexIndex = 0; existingVertexIndex < vertexCount; existingVertexIndex++) {
			if (pointX != vertexX[existingVertexIndex] || pointY != vertexY[existingVertexIndex] || pointZ != vertexZ[existingVertexIndex]) {
				continue;
			}
			destinationVertexIndex = existingVertexIndex;
			break;
		}

		if (destinationVertexIndex == -1) {
			vertexX[vertexCount] = pointX;
			vertexY[vertexCount] = pointY;
			vertexZ[vertexCount] = pointZ;
			if (sourceModel.vertexGroupIds != null) {
				vertexGroupIds[vertexCount] = sourceModel.vertexGroupIds[sourceVertexIndex];
			}
			destinationVertexIndex = vertexCount++;
		}
		return destinationVertexIndex;
	}

	public void calculateBounds() {
		super.modelHeight = 0;
		radiusXZ = 0;
		maxY = 0;
		for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
			int pointX = vertexX[vertexIndex];
			int pointY = vertexY[vertexIndex];
			int pointZ = vertexZ[vertexIndex];
			if (-pointY > super.modelHeight) {
				super.modelHeight = -pointY;
			}
			if (pointY > maxY) {
				maxY = pointY;
			}
			int radiusSquaredXZ = pointX * pointX + pointZ * pointZ;
			if (radiusSquaredXZ > radiusXZ) {
				radiusXZ = radiusSquaredXZ;
			}
		}
		radiusXZ = (int) (Math.sqrt(radiusXZ) + 0.98999999999999999D);
		depthBucketOffset = (int) (Math.sqrt(radiusXZ * radiusXZ + super.modelHeight * super.modelHeight) + 0.98999999999999999D);
		depthBucketCount = depthBucketOffset + (int) (Math.sqrt(radiusXZ * radiusXZ + maxY * maxY) + 0.98999999999999999D);
	}

	public void recalculateYBounds() {
		super.modelHeight = 0;
		maxY = 0;
		for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
			int pointY = vertexY[vertexIndex];
			if (-pointY > super.modelHeight) {
				super.modelHeight = -pointY;
			}
			if (pointY > maxY) {
				maxY = pointY;
			}
		}

		depthBucketOffset = (int) (Math.sqrt(radiusXZ * radiusXZ + super.modelHeight * super.modelHeight) + 0.98999999999999999D);
		depthBucketCount = depthBucketOffset + (int) (Math.sqrt(radiusXZ * radiusXZ + maxY * maxY) + 0.98999999999999999D);
	}

	private void calculateBoundsWithExtents() {
		super.modelHeight = 0;
		radiusXZ = 0;
		maxY = 0;
		minX = 0xf423f;
		maxX = 0xfff0bdc1;
		maxZ = 0xfffe7961;
		minZ = 0x1869f;
		for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
			int pointX = vertexX[vertexIndex];
			int pointY = vertexY[vertexIndex];
			int pointZ = vertexZ[vertexIndex];
			if (pointX < minX) {
				minX = pointX;
			}
			if (pointX > maxX) {
				maxX = pointX;
			}
			if (pointZ < minZ) {
				minZ = pointZ;
			}
			if (pointZ > maxZ) {
				maxZ = pointZ;
			}
			if (-pointY > super.modelHeight) {
				super.modelHeight = -pointY;
			}
			if (pointY > maxY) {
				maxY = pointY;
			}
			int radiusSquaredXZ = pointX * pointX + pointZ * pointZ;
			if (radiusSquaredXZ > radiusXZ) {
				radiusXZ = radiusSquaredXZ;
			}
		}

		radiusXZ = (int) Math.sqrt(radiusXZ);
		depthBucketOffset = (int) Math.sqrt(radiusXZ * radiusXZ + super.modelHeight * super.modelHeight);
		depthBucketCount = depthBucketOffset + (int) Math.sqrt(radiusXZ * radiusXZ + maxY * maxY);
	}

	public void buildAnimationGroups() {
		if (vertexGroupIds != null) {
			int vertexGroupCounts[] = new int[256];
			int maxVertexGroupId = 0;
			for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
				int vertexGroupId = vertexGroupIds[vertexIndex];
				vertexGroupCounts[vertexGroupId]++;
				if (vertexGroupId > maxVertexGroupId) {
					maxVertexGroupId = vertexGroupId;
				}
			}

			vertexGroups = new int[maxVertexGroupId + 1][];
			for (int vertexGroupId = 0; vertexGroupId <= maxVertexGroupId; vertexGroupId++) {
				vertexGroups[vertexGroupId] = new int[vertexGroupCounts[vertexGroupId]];
				vertexGroupCounts[vertexGroupId] = 0;
			}

			for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
				int vertexGroupId = vertexGroupIds[vertexIndex];
				vertexGroups[vertexGroupId][vertexGroupCounts[vertexGroupId]++] = vertexIndex;
			}

			vertexGroupIds = null;
		}
		if (faceGroupIds != null) {
			int faceGroupCounts[] = new int[256];
			int maxFaceGroupId = 0;
			for (int faceIndex = 0; faceIndex < faceCount; faceIndex++) {
				int faceGroupId = faceGroupIds[faceIndex];
				faceGroupCounts[faceGroupId]++;
				if (faceGroupId > maxFaceGroupId) {
					maxFaceGroupId = faceGroupId;
				}
			}

			faceGroups = new int[maxFaceGroupId + 1][];
			for (int faceGroupId = 0; faceGroupId <= maxFaceGroupId; faceGroupId++) {
				faceGroups[faceGroupId] = new int[faceGroupCounts[faceGroupId]];
				faceGroupCounts[faceGroupId] = 0;
			}

			for (int faceIndex = 0; faceIndex < faceCount; faceIndex++) {
				int faceGroupId = faceGroupIds[faceIndex];
				faceGroups[faceGroupId][faceGroupCounts[faceGroupId]++] = faceIndex;
			}

			faceGroupIds = null;
		}
	}

	public void applyAnimationFrame(int frameId) {
		if (vertexGroups == null) {
			return;
		}
		if (frameId == -1) {
			return;
		}
		AnimationFrame frame = AnimationFrame.getFrame(frameId);
		if (frame == null) {
			return;
		}
		AnimationTransformTable table = frame.transformTable;
		animationPivotX = 0;
		animationPivotY = 0;
		animationPivotZ = 0;
		for (int frameTransformIndex = 0; frameTransformIndex < frame.transformCount; frameTransformIndex++) {
			int tableTransformIndex = frame.transformIndices[frameTransformIndex];
			transformAnimationGroups(table.transformTypes[tableTransformIndex], table.groupIds[tableTransformIndex], frame.transformX[frameTransformIndex], frame.transformY[frameTransformIndex], frame.transformZ[frameTransformIndex]);
		}

	}

	public void applyInterleavedFrames(int secondaryTransformIndices[], int secondaryFrameId, int primaryFrameId) {
		if (primaryFrameId == -1) {
			return;
		}
		if (secondaryTransformIndices == null || secondaryFrameId == -1) {
			applyAnimationFrame(primaryFrameId);
			return;
		}
		AnimationFrame primaryFrame = AnimationFrame.getFrame(primaryFrameId);
		if (primaryFrame == null) {
			return;
		}
		AnimationFrame secondaryFrame = AnimationFrame.getFrame(secondaryFrameId);
		if (secondaryFrame == null) {
			applyAnimationFrame(primaryFrameId);
			return;
		}
		AnimationTransformTable table = primaryFrame.transformTable;
		animationPivotX = 0;
		animationPivotY = 0;
		animationPivotZ = 0;
		int selectionCursor = 0;
		int selectedTransformIndex = secondaryTransformIndices[selectionCursor++];
		for (int primaryTransformIndex = 0; primaryTransformIndex < primaryFrame.transformCount; primaryTransformIndex++) {
			int primaryTableIndex;
			for (primaryTableIndex = primaryFrame.transformIndices[primaryTransformIndex]; primaryTableIndex > selectedTransformIndex; selectedTransformIndex = secondaryTransformIndices[selectionCursor++]) {
				;
			}
			if (primaryTableIndex != selectedTransformIndex || table.transformTypes[primaryTableIndex] == 0) {
				transformAnimationGroups(table.transformTypes[primaryTableIndex], table.groupIds[primaryTableIndex], primaryFrame.transformX[primaryTransformIndex], primaryFrame.transformY[primaryTransformIndex], primaryFrame.transformZ[primaryTransformIndex]);
			}
		}

		animationPivotX = 0;
		animationPivotY = 0;
		animationPivotZ = 0;
		selectionCursor = 0;
		selectedTransformIndex = secondaryTransformIndices[selectionCursor++];
		for (int secondaryTransformIndex = 0; secondaryTransformIndex < secondaryFrame.transformCount; secondaryTransformIndex++) {
			int secondaryTableIndex;
			for (secondaryTableIndex = secondaryFrame.transformIndices[secondaryTransformIndex]; secondaryTableIndex > selectedTransformIndex; selectedTransformIndex = secondaryTransformIndices[selectionCursor++]) {
				;
			}
			if (secondaryTableIndex == selectedTransformIndex || table.transformTypes[secondaryTableIndex] == 0) {
				transformAnimationGroups(table.transformTypes[secondaryTableIndex], table.groupIds[secondaryTableIndex], secondaryFrame.transformX[secondaryTransformIndex], secondaryFrame.transformY[secondaryTransformIndex], secondaryFrame.transformZ[secondaryTransformIndex]);
			}
		}

	}

	private void transformAnimationGroups(int transformType, int groupIds[], int x, int y, int z) {
		int groupCount = groupIds.length;
		if (transformType == 0) {
			int selectedVertexCount = 0;
			animationPivotX = 0;
			animationPivotY = 0;
			animationPivotZ = 0;
			for (int groupIndex = 0; groupIndex < groupCount; groupIndex++) {
				int vertexGroupId = groupIds[groupIndex];
				if (vertexGroupId < vertexGroups.length) {
					int vertexIndices[] = vertexGroups[vertexGroupId];
					for (int vertexIndex : vertexIndices) {
						animationPivotX += vertexX[vertexIndex];
						animationPivotY += vertexY[vertexIndex];
						animationPivotZ += vertexZ[vertexIndex];
						selectedVertexCount++;
					}

				}
			}

			if (selectedVertexCount > 0) {
				animationPivotX = animationPivotX / selectedVertexCount + x;
				animationPivotY = animationPivotY / selectedVertexCount + y;
				animationPivotZ = animationPivotZ / selectedVertexCount + z;
				return;
			} else {
				animationPivotX = x;
				animationPivotY = y;
				animationPivotZ = z;
				return;
			}
		}
		if (transformType == 1) {
			for (int groupIndex = 0; groupIndex < groupCount; groupIndex++) {
				int vertexGroupId = groupIds[groupIndex];
				if (vertexGroupId < vertexGroups.length) {
					int vertexIndices[] = vertexGroups[vertexGroupId];
					for (int groupVertexIndex : vertexIndices) {
						int vertexIndex = groupVertexIndex;
						vertexX[vertexIndex] += x;
						vertexY[vertexIndex] += y;
						vertexZ[vertexIndex] += z;
					}

				}
			}

			return;
		}
		if (transformType == 2) {
			for (int groupIndex = 0; groupIndex < groupCount; groupIndex++) {
				int vertexGroupId = groupIds[groupIndex];
				if (vertexGroupId < vertexGroups.length) {
					int vertexIndices[] = vertexGroups[vertexGroupId];
					for (int groupVertexIndex : vertexIndices) {
						int vertexIndex = groupVertexIndex;
						vertexX[vertexIndex] -= animationPivotX;
						vertexY[vertexIndex] -= animationPivotY;
						vertexZ[vertexIndex] -= animationPivotZ;
						int rotationX2048 = (x & 0xff) * 8;
						int rotationY2048 = (y & 0xff) * 8;
						int rotationZ2048 = (z & 0xff) * 8;
						if (rotationZ2048 != 0) {
							int sineZ16 = sineTable16[rotationZ2048];
							int cosineZ16 = cosineTable16[rotationZ2048];
							int rotatedX = vertexY[vertexIndex] * sineZ16 + vertexX[vertexIndex] * cosineZ16 >> 16;
							vertexY[vertexIndex] = vertexY[vertexIndex] * cosineZ16 - vertexX[vertexIndex] * sineZ16 >> 16;
							vertexX[vertexIndex] = rotatedX;
						}
						if (rotationX2048 != 0) {
							int sineX16 = sineTable16[rotationX2048];
							int cosineX16 = cosineTable16[rotationX2048];
							int rotatedY = vertexY[vertexIndex] * cosineX16 - vertexZ[vertexIndex] * sineX16 >> 16;
							vertexZ[vertexIndex] = vertexY[vertexIndex] * sineX16 + vertexZ[vertexIndex] * cosineX16 >> 16;
							vertexY[vertexIndex] = rotatedY;
						}
						if (rotationY2048 != 0) {
							int sineY16 = sineTable16[rotationY2048];
							int cosineY16 = cosineTable16[rotationY2048];
							int rotatedX = vertexZ[vertexIndex] * sineY16 + vertexX[vertexIndex] * cosineY16 >> 16;
							vertexZ[vertexIndex] = vertexZ[vertexIndex] * cosineY16 - vertexX[vertexIndex] * sineY16 >> 16;
							vertexX[vertexIndex] = rotatedX;
						}
						vertexX[vertexIndex] += animationPivotX;
						vertexY[vertexIndex] += animationPivotY;
						vertexZ[vertexIndex] += animationPivotZ;
					}

				}
			}

			return;
		}
		if (transformType == 3) {
			for (int groupIndex = 0; groupIndex < groupCount; groupIndex++) {
				int vertexGroupId = groupIds[groupIndex];
				if (vertexGroupId < vertexGroups.length) {
					int vertexIndices[] = vertexGroups[vertexGroupId];
					for (int groupVertexIndex : vertexIndices) {
						int vertexIndex = groupVertexIndex;
						vertexX[vertexIndex] -= animationPivotX;
						vertexY[vertexIndex] -= animationPivotY;
						vertexZ[vertexIndex] -= animationPivotZ;
						vertexX[vertexIndex] = vertexX[vertexIndex] * x / 128;
						vertexY[vertexIndex] = vertexY[vertexIndex] * y / 128;
						vertexZ[vertexIndex] = vertexZ[vertexIndex] * z / 128;
						vertexX[vertexIndex] += animationPivotX;
						vertexY[vertexIndex] += animationPivotY;
						vertexZ[vertexIndex] += animationPivotZ;
					}

				}
			}

			return;
		}
		if (transformType == 5 && faceGroups != null && faceAlphas != null) {
			for (int groupIndex = 0; groupIndex < groupCount; groupIndex++) {
				int faceGroupId = groupIds[groupIndex];
				if (faceGroupId < faceGroups.length) {
					int faceIndices[] = faceGroups[faceGroupId];
					for (int groupFaceIndex : faceIndices) {
						int faceIndex = groupFaceIndex;
						faceAlphas[faceIndex] += x * 8;
						if (faceAlphas[faceIndex] < 0) {
							faceAlphas[faceIndex] = 0;
						}
						if (faceAlphas[faceIndex] > 255) {
							faceAlphas[faceIndex] = 255;
						}
					}

				}
			}

		}
	}

	public void rotateYQuarterTurn() {
		for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
			int originalX = vertexX[vertexIndex];
			vertexX[vertexIndex] = vertexZ[vertexIndex];
			vertexZ[vertexIndex] = -originalX;
		}

	}

	public void rotateX(int angle2048) {
		int sine16 = sineTable16[angle2048];
		int cosine16 = cosineTable16[angle2048];
		for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
			int rotatedY = vertexY[vertexIndex] * cosine16 - vertexZ[vertexIndex] * sine16 >> 16;
			vertexZ[vertexIndex] = vertexY[vertexIndex] * sine16 + vertexZ[vertexIndex] * cosine16 >> 16;
			vertexY[vertexIndex] = rotatedY;
		}
	}

	public void translate(int deltaX, int deltaY, int deltaZ) {
		for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
			vertexX[vertexIndex] += deltaX;
			vertexY[vertexIndex] += deltaY;
			vertexZ[vertexIndex] += deltaZ;
		}

	}

	public void recolor(int sourceValue, int replacementValue) {
		for (int faceIndex = 0; faceIndex < faceCount; faceIndex++) {
			if (faceColors[faceIndex] == sourceValue) {
				faceColors[faceIndex] = replacementValue;
			}
		}

	}

	public void reflectZAndReverseWinding() {
		for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
			vertexZ[vertexIndex] = -vertexZ[vertexIndex];
		}

		for (int faceIndex = 0; faceIndex < faceCount; faceIndex++) {
			int originalFirstVertex = faceVertexA[faceIndex];
			faceVertexA[faceIndex] = faceVertexC[faceIndex];
			faceVertexC[faceIndex] = originalFirstVertex;
		}
	}

	public void scale(int scaleX128, int scaleZ128, int scaleY128) {
		for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
			vertexX[vertexIndex] = vertexX[vertexIndex] * scaleX128 / 128;
			vertexY[vertexIndex] = vertexY[vertexIndex] * scaleY128 / 128;
			vertexZ[vertexIndex] = vertexZ[vertexIndex] * scaleZ128 / 128;
		}

	}

	public void applyLighting(int ambientLight, int lightDivisorScale, int lightX, int lightY, int lightZ, boolean bakeShading) {
		int lightLength = (int) Math.sqrt(lightX * lightX + lightY * lightY + lightZ * lightZ);
		int lightDivisor = lightDivisorScale * lightLength >> 8;
		if (faceShadeA == null) {
			faceShadeA = new int[faceCount];
			faceShadeB = new int[faceCount];
			faceShadeC = new int[faceCount];
		}
		if (super.vertexNormals == null) {
			super.vertexNormals = new VertexNormal[vertexCount];
			for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
				super.vertexNormals[vertexIndex] = new VertexNormal();
			}

		}
		for (int faceIndex = 0; faceIndex < faceCount; faceIndex++) {
			int vertexA = faceVertexA[faceIndex];
			int vertexB = faceVertexB[faceIndex];
			int vertexC = faceVertexC[faceIndex];
			int abX = vertexX[vertexB] - vertexX[vertexA];
			int abY = vertexY[vertexB] - vertexY[vertexA];
			int abZ = vertexZ[vertexB] - vertexZ[vertexA];
			int acX = vertexX[vertexC] - vertexX[vertexA];
			int acY = vertexY[vertexC] - vertexY[vertexA];
			int acZ = vertexZ[vertexC] - vertexZ[vertexA];
			int normalX = abY * acZ - acY * abZ;
			int normalY = abZ * acX - acZ * abX;
			int normalZ;
			for (normalZ = abX * acY - acX * abY; normalX > 8192 || normalY > 8192 || normalZ > 8192 || normalX < -8192 || normalY < -8192 || normalZ < -8192; normalZ >>= 1) {
				normalX >>= 1;
				normalY >>= 1;
			}

			int normalLength = (int) Math.sqrt(normalX * normalX + normalY * normalY + normalZ * normalZ);
			if (normalLength <= 0) {
				normalLength = 1;
			}
			normalX = normalX * 256 / normalLength;
			normalY = normalY * 256 / normalLength;
			normalZ = normalZ * 256 / normalLength;
			if (faceRenderFlags == null || (faceRenderFlags[faceIndex] & 1) == 0) {
				VertexNormal vertexNormal = super.vertexNormals[vertexA];
				vertexNormal.normalX += normalX;
				vertexNormal.normalY += normalY;
				vertexNormal.normalZ += normalZ;
				vertexNormal.faceCount++;
				vertexNormal = super.vertexNormals[vertexB];
				vertexNormal.normalX += normalX;
				vertexNormal.normalY += normalY;
				vertexNormal.normalZ += normalZ;
				vertexNormal.faceCount++;
				vertexNormal = super.vertexNormals[vertexC];
				vertexNormal.normalX += normalX;
				vertexNormal.normalY += normalY;
				vertexNormal.normalZ += normalZ;
				vertexNormal.faceCount++;
			} else {
				int flatIntensity = ambientLight + (lightX * normalX + lightY * normalY + lightZ * normalZ) / (lightDivisor + lightDivisor / 2);
				faceShadeA[faceIndex] = calculateFaceShade(faceColors[faceIndex], flatIntensity, faceRenderFlags[faceIndex]);
			}
		}

		if (bakeShading) {
			applyVertexLighting(ambientLight, lightDivisor, lightX, lightY, lightZ);
		} else {
			originalVertexNormals = new VertexNormal[vertexCount];
			for (int normalIndex = 0; normalIndex < vertexCount; normalIndex++) {
				VertexNormal sourceNormal = super.vertexNormals[normalIndex];
				VertexNormal copiedNormal = originalVertexNormals[normalIndex] = new VertexNormal();
				copiedNormal.normalX = sourceNormal.normalX;
				copiedNormal.normalY = sourceNormal.normalY;
				copiedNormal.normalZ = sourceNormal.normalZ;
				copiedNormal.faceCount = sourceNormal.faceCount;
			}

		}
		if (bakeShading) {
			calculateBounds();
		} else {
			calculateBoundsWithExtents();
		}
	}

	public void applyVertexLighting(int ambientLight, int lightDivisor, int lightX, int lightY, int lightZ) {
		for (int faceIndex = 0; faceIndex < faceCount; faceIndex++) {
			int vertexA = faceVertexA[faceIndex];
			int vertexB = faceVertexB[faceIndex];
			int vertexC = faceVertexC[faceIndex];
			if (faceRenderFlags == null) {
				int faceColor = faceColors[faceIndex];
				VertexNormal vertexNormal = super.vertexNormals[vertexA];
				int intensity = ambientLight + (lightX * vertexNormal.normalX + lightY * vertexNormal.normalY + lightZ * vertexNormal.normalZ) / (lightDivisor * vertexNormal.faceCount);
				faceShadeA[faceIndex] = calculateFaceShade(faceColor, intensity, 0);
				vertexNormal = super.vertexNormals[vertexB];
				intensity = ambientLight + (lightX * vertexNormal.normalX + lightY * vertexNormal.normalY + lightZ * vertexNormal.normalZ) / (lightDivisor * vertexNormal.faceCount);
				faceShadeB[faceIndex] = calculateFaceShade(faceColor, intensity, 0);
				vertexNormal = super.vertexNormals[vertexC];
				intensity = ambientLight + (lightX * vertexNormal.normalX + lightY * vertexNormal.normalY + lightZ * vertexNormal.normalZ) / (lightDivisor * vertexNormal.faceCount);
				faceShadeC[faceIndex] = calculateFaceShade(faceColor, intensity, 0);
			} else if ((faceRenderFlags[faceIndex] & 1) == 0) {
				int faceColor = faceColors[faceIndex];
				int faceFlags = faceRenderFlags[faceIndex];
				VertexNormal vertexNormal = super.vertexNormals[vertexA];
				int intensity = ambientLight + (lightX * vertexNormal.normalX + lightY * vertexNormal.normalY + lightZ * vertexNormal.normalZ) / (lightDivisor * vertexNormal.faceCount);
				faceShadeA[faceIndex] = calculateFaceShade(faceColor, intensity, faceFlags);
				vertexNormal = super.vertexNormals[vertexB];
				intensity = ambientLight + (lightX * vertexNormal.normalX + lightY * vertexNormal.normalY + lightZ * vertexNormal.normalZ) / (lightDivisor * vertexNormal.faceCount);
				faceShadeB[faceIndex] = calculateFaceShade(faceColor, intensity, faceFlags);
				vertexNormal = super.vertexNormals[vertexC];
				intensity = ambientLight + (lightX * vertexNormal.normalX + lightY * vertexNormal.normalY + lightZ * vertexNormal.normalZ) / (lightDivisor * vertexNormal.faceCount);
				faceShadeC[faceIndex] = calculateFaceShade(faceColor, intensity, faceFlags);
			}
		}

		super.vertexNormals = null;
		originalVertexNormals = null;
		vertexGroupIds = null;
		faceGroupIds = null;
		if (faceRenderFlags != null) {
			for (int checkIndex = 0; checkIndex < faceCount; checkIndex++) {
				if ((faceRenderFlags[checkIndex] & 2) == 2) {
					return;
				}
			}

		}
		faceColors = null;
	}

	private static int calculateFaceShade(int colorValue, int lightIntensity, int renderFlags) {
		if ((renderFlags & 2) == 2) {
			if (lightIntensity < 0) {
				lightIntensity = 0;
			} else if (lightIntensity > 127) {
				lightIntensity = 127;
			}
			lightIntensity = 127 - lightIntensity;
			return lightIntensity;
		}
		lightIntensity = lightIntensity * (colorValue & 0x7f) >> 7;
		if (lightIntensity < 2) {
			lightIntensity = 2;
		} else if (lightIntensity > 126) {
			lightIntensity = 126;
		}
		return (colorValue & 0xff80) + lightIntensity;
	}

	public void projectRotated(int yaw, int roll, int cameraPitch, int offsetX, int offsetY, int offsetZ) {
		int i = 0; // was a parameter
		int screenCenterX = Texture.textureInt1;
		int screenCenterY = Texture.textureInt2;
		int j2 = sineTable16[i];
		int k2 = cosineTable16[i];
		int yawSine = sineTable16[yaw];
		int yawCosine = cosineTable16[yaw];
		int rollSine = sineTable16[roll];
		int rollCosine = cosineTable16[roll];
		int pitchSine = sineTable16[cameraPitch];
		int pitchCosine = cosineTable16[cameraPitch];
		int originDepth = offsetY * pitchSine + offsetZ * pitchCosine >> 16;
		for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
			int x = vertexX[vertexIndex];
			int y = vertexY[vertexIndex];
			int z = vertexZ[vertexIndex];
			if (roll != 0) {
				int rolledX = y * rollSine + x * rollCosine >> 16;
				y = y * rollCosine - x * rollSine >> 16;
				x = rolledX;
			}
			if (i != 0) {
				int l5 = y * k2 - z * j2 >> 16;
				z = y * j2 + z * k2 >> 16;
				y = l5;
			}
			if (yaw != 0) {
				int yawedX = z * yawSine + x * yawCosine >> 16;
				z = z * yawCosine - x * yawSine >> 16;
				x = yawedX;
			}
			x += offsetX;
			y += offsetY;
			z += offsetZ;
			int pitchedY = y * pitchCosine - z * pitchSine >> 16;
			z = y * pitchSine + z * pitchCosine >> 16;
			y = pitchedY;
			vertexDepth[vertexIndex] = z - originDepth;
			screenX[vertexIndex] = screenCenterX + (x << 9) / z;
			screenY[vertexIndex] = screenCenterY + (y << 9) / z;
			if (texturedFaceCount > 0) {
				vertexCameraX[vertexIndex] = x;
				vertexCameraY[vertexIndex] = y;
				vertexCameraZ[vertexIndex] = z;
			}
		}

		try {
			drawFacesByDepthAndPriority(false, false, 0);
		} catch (Exception _ex) {
		}
	}

	@Override
	public void drawFromCamera(int modelRotation, int pitchSine, int pitchCosine, int yawSine, int yawCosine, int offsetX, int offsetY, int offsetZ, int pickId) {
		int yawedDepth = offsetZ * yawCosine - offsetX * yawSine >> 16;
		int centerDepth = offsetY * pitchSine + yawedDepth * pitchCosine >> 16;
		int radiusDepth = radiusXZ * pitchCosine >> 16;
		int farDepth = centerDepth + radiusDepth;
		// Check distance of model to camera for rendering (default 3500)
		int distance = WorldController.drawDistance * 256;
		distance *= 1 + (Game.zoom / 10);
		if (farDepth <= 50 || centerDepth >= distance) {
			return;
		}
		int yawedX = offsetZ * yawSine + offsetX * yawCosine >> 16;
		int leftEdge = yawedX - radiusXZ << 9;
		if (leftEdge / farDepth >= DrawingArea.clipRightHalf) {
			return;
		}
		int rightEdge = yawedX + radiusXZ << 9;
		if (rightEdge / farDepth <= -DrawingArea.clipRightHalf) {
			return;
		}
		int pitchedY = offsetY * pitchCosine - yawedDepth * pitchSine >> 16;
		int radiusHeight = radiusXZ * pitchSine >> 16;
		int bottomEdge = pitchedY + radiusHeight << 9;
		if (bottomEdge / farDepth <= -DrawingArea.clipBottomHalf) {
			return;
		}
		int heightExtent = radiusHeight + (super.modelHeight * pitchCosine >> 16);
		int topEdge = pitchedY - heightExtent << 9;
		if (topEdge / farDepth >= DrawingArea.clipBottomHalf) {
			return;
		}
		int nearExtent = radiusDepth + (super.modelHeight * pitchSine >> 16);
		boolean nearPlaneClipping = false;
		if (centerDepth - nearExtent <= 50) {
			nearPlaneClipping = true;
		}
		boolean facePick = false;
		if (pickId > 0 && pickingEnabled) {
			int nearDepth = centerDepth - radiusDepth;
			if (nearDepth <= 50) {
				nearDepth = 50;
			}
			if (yawedX > 0) {
				leftEdge /= farDepth;
				rightEdge /= nearDepth;
			} else {
				rightEdge /= farDepth;
				leftEdge /= nearDepth;
			}
			if (pitchedY > 0) {
				topEdge /= farDepth;
				bottomEdge /= nearDepth;
			} else {
				bottomEdge /= farDepth;
				topEdge /= nearDepth;
			}
			int mouseOffsetX = pickMouseX - Texture.textureInt1;
			int mouseOffsetY = pickMouseY - Texture.textureInt2;
			if (mouseOffsetX > leftEdge && mouseOffsetX < rightEdge && mouseOffsetY > topEdge && mouseOffsetY < bottomEdge) {
				if (pickByBounds) {
					pickedIds[pickedCount++] = pickId;
				} else {
					facePick = true;
				}
			}
		}
		int screenCenterX = Texture.textureInt1;
		int screenCenterY = Texture.textureInt2;
		int rotationSine = 0;
		int rotationCosine = 0;
		if (modelRotation != 0) {
			rotationSine = sineTable16[modelRotation];
			rotationCosine = cosineTable16[modelRotation];
		}
		for (int vertexIndex = 0; vertexIndex < vertexCount; vertexIndex++) {
			int x = vertexX[vertexIndex];
			int y = vertexY[vertexIndex];
			int z = vertexZ[vertexIndex];
			if (modelRotation != 0) {
				int rotatedX = z * rotationSine + x * rotationCosine >> 16;
				z = z * rotationCosine - x * rotationSine >> 16;
				x = rotatedX;
			}
			x += offsetX;
			y += offsetY;
			z += offsetZ;
			int temp = z * yawSine + x * yawCosine >> 16;
			z = z * yawCosine - x * yawSine >> 16;
			x = temp;
			temp = y * pitchCosine - z * pitchSine >> 16;
			z = y * pitchSine + z * pitchCosine >> 16;
			y = temp;
			vertexDepth[vertexIndex] = z - centerDepth;
			if (z >= 50) {
				screenX[vertexIndex] = screenCenterX + (x << 9) / z;
				screenY[vertexIndex] = screenCenterY + (y << 9) / z;
			} else {
				screenX[vertexIndex] = -5000;
				nearPlaneClipping = true;
			}
			if (nearPlaneClipping || texturedFaceCount > 0) {
				vertexCameraX[vertexIndex] = x;
				vertexCameraY[vertexIndex] = y;
				vertexCameraZ[vertexIndex] = z;
			}
		}

		try {
			drawFacesByDepthAndPriority(nearPlaneClipping, facePick, pickId);
		} catch (Exception _ex) {
		}
	}

	private void drawFacesByDepthAndPriority(boolean nearPlaneClipping, boolean pickEnabled, int pickId) {
		for (int bucketIndex = 0; bucketIndex < depthBucketCount; bucketIndex++) {
			depthBucketFaceCounts[bucketIndex] = 0;
		}

		for (int faceIndex = 0; faceIndex < faceCount; faceIndex++) {
			if (faceRenderFlags == null || faceRenderFlags[faceIndex] != -1) {
				int vertexA = faceVertexA[faceIndex];
				int vertexB = faceVertexB[faceIndex];
				int vertexC = faceVertexC[faceIndex];
				int screenXA = screenX[vertexA];
				int screenXB = screenX[vertexB];
				int screenXC = screenX[vertexC];
				if (nearPlaneClipping && (screenXA == -5000 || screenXB == -5000 || screenXC == -5000)) {
					faceNearPlaneClipped[faceIndex] = true;
					int depthBucket = (vertexDepth[vertexA] + vertexDepth[vertexB] + vertexDepth[vertexC]) / 3 + depthBucketOffset;
					depthBucketFaces[depthBucket][depthBucketFaceCounts[depthBucket]++] = faceIndex;
				} else {
					if (pickEnabled && isPointWithinFaceBounds(pickMouseX, pickMouseY, screenY[vertexA], screenY[vertexB], screenY[vertexC], screenXA, screenXB, screenXC)) {
						pickedIds[pickedCount++] = pickId;
						pickEnabled = false;
					}
					if ((screenXA - screenXB) * (screenY[vertexC] - screenY[vertexB]) - (screenY[vertexA] - screenY[vertexB]) * (screenXC - screenXB) > 0) {
						faceNearPlaneClipped[faceIndex] = false;
						faceNeedsHorizontalClip[faceIndex] = screenXA < 0 || screenXB < 0 || screenXC < 0 || screenXA > DrawingArea.scanlineRightLimit || screenXB > DrawingArea.scanlineRightLimit || screenXC > DrawingArea.scanlineRightLimit;
						int depthBucket = (vertexDepth[vertexA] + vertexDepth[vertexB] + vertexDepth[vertexC]) / 3 + depthBucketOffset;
						depthBucketFaces[depthBucket][depthBucketFaceCounts[depthBucket]++] = faceIndex;
					}
				}
			}
		}

		if (facePriorities == null) {
			for (int bucket = depthBucketCount - 1; bucket >= 0; bucket--) {
				int bucketFaceCount = depthBucketFaceCounts[bucket];
				if (bucketFaceCount > 0) {
					int bucketFaceList[] = depthBucketFaces[bucket];
					for (int listIndex = 0; listIndex < bucketFaceCount; listIndex++) {
						drawFace(bucketFaceList[listIndex]);
					}

				}
			}

			return;
		}
		for (int priority = 0; priority < 12; priority++) {
			priorityFaceCounts[priority] = 0;
			priorityDepthSums[priority] = 0;
		}

		for (int bucket = depthBucketCount - 1; bucket >= 0; bucket--) {
			int bucketFaceCount = depthBucketFaceCounts[bucket];
			if (bucketFaceCount > 0) {
				int bucketFaceList[] = depthBucketFaces[bucket];
				for (int listIndex = 0; listIndex < bucketFaceCount; listIndex++) {
					int faceIndex = bucketFaceList[listIndex];
					int priority = facePriorities[faceIndex];
					int slot = priorityFaceCounts[priority]++;
					priorityFaces[priority][slot] = faceIndex;
					if (priority < 10) {
						priorityDepthSums[priority] += bucket;
					} else if (priority == 10) {
						priority10Depths[slot] = bucket;
					} else {
						priority11Depths[slot] = bucket;
					}
				}

			}
		}

		int priority12MeanDepth = 0;
		if (priorityFaceCounts[1] > 0 || priorityFaceCounts[2] > 0) {
			priority12MeanDepth = (priorityDepthSums[1] + priorityDepthSums[2]) / (priorityFaceCounts[1] + priorityFaceCounts[2]);
		}
		int priority34MeanDepth = 0;
		if (priorityFaceCounts[3] > 0 || priorityFaceCounts[4] > 0) {
			priority34MeanDepth = (priorityDepthSums[3] + priorityDepthSums[4]) / (priorityFaceCounts[3] + priorityFaceCounts[4]);
		}
		int priority68MeanDepth = 0;
		if (priorityFaceCounts[6] > 0 || priorityFaceCounts[8] > 0) {
			priority68MeanDepth = (priorityDepthSums[6] + priorityDepthSums[8]) / (priorityFaceCounts[6] + priorityFaceCounts[8]);
		}
		int mergeIndex = 0;
		int mergeCount = priorityFaceCounts[10];
		int mergeFaces[] = priorityFaces[10];
		int mergeDepths[] = priority10Depths;
		if (mergeIndex == mergeCount) {
			mergeIndex = 0;
			mergeCount = priorityFaceCounts[11];
			mergeFaces = priorityFaces[11];
			mergeDepths = priority11Depths;
		}
		int mergeNextDepth;
		if (mergeIndex < mergeCount) {
			mergeNextDepth = mergeDepths[mergeIndex];
		} else {
			mergeNextDepth = -1000;
		}
		for (int priorityStage = 0; priorityStage < 10; priorityStage++) {
			while (priorityStage == 0 && mergeNextDepth > priority12MeanDepth) {
				drawFace(mergeFaces[mergeIndex++]);
				if (mergeIndex == mergeCount && mergeFaces != priorityFaces[11]) {
					mergeIndex = 0;
					mergeCount = priorityFaceCounts[11];
					mergeFaces = priorityFaces[11];
					mergeDepths = priority11Depths;
				}
				if (mergeIndex < mergeCount) {
					mergeNextDepth = mergeDepths[mergeIndex];
				} else {
					mergeNextDepth = -1000;
				}
			}
			while (priorityStage == 3 && mergeNextDepth > priority34MeanDepth) {
				drawFace(mergeFaces[mergeIndex++]);
				if (mergeIndex == mergeCount && mergeFaces != priorityFaces[11]) {
					mergeIndex = 0;
					mergeCount = priorityFaceCounts[11];
					mergeFaces = priorityFaces[11];
					mergeDepths = priority11Depths;
				}
				if (mergeIndex < mergeCount) {
					mergeNextDepth = mergeDepths[mergeIndex];
				} else {
					mergeNextDepth = -1000;
				}
			}
			while (priorityStage == 5 && mergeNextDepth > priority68MeanDepth) {
				drawFace(mergeFaces[mergeIndex++]);
				if (mergeIndex == mergeCount && mergeFaces != priorityFaces[11]) {
					mergeIndex = 0;
					mergeCount = priorityFaceCounts[11];
					mergeFaces = priorityFaces[11];
					mergeDepths = priority11Depths;
				}
				if (mergeIndex < mergeCount) {
					mergeNextDepth = mergeDepths[mergeIndex];
				} else {
					mergeNextDepth = -1000;
				}
			}
			int stageFaceCount = priorityFaceCounts[priorityStage];
			int stageFaces[] = priorityFaces[priorityStage];
			for (int listIndex = 0; listIndex < stageFaceCount; listIndex++) {
				drawFace(stageFaces[listIndex]);
			}

		}

		while (mergeNextDepth != -1000) {
			drawFace(mergeFaces[mergeIndex++]);
			if (mergeIndex == mergeCount && mergeFaces != priorityFaces[11]) {
				mergeIndex = 0;
				mergeFaces = priorityFaces[11];
				mergeCount = priorityFaceCounts[11];
				mergeDepths = priority11Depths;
			}
			if (mergeIndex < mergeCount) {
				mergeNextDepth = mergeDepths[mergeIndex];
			} else {
				mergeNextDepth = -1000;
			}
		}
	}

	private void drawFace(int faceIndex) {
		if (faceNearPlaneClipped[faceIndex]) {
			drawFaceClippedToNearPlane(faceIndex);
			return;
		}
		int vertexA = faceVertexA[faceIndex];
		int vertexB = faceVertexB[faceIndex];
		int vertexC = faceVertexC[faceIndex];
		Texture.clipScanlines = faceNeedsHorizontalClip[faceIndex];
		if (faceAlphas == null) {
			Texture.transparency = 0;
		} else {
			Texture.transparency = faceAlphas[faceIndex];
		}
		int renderType;
		if (faceRenderFlags == null) {
			renderType = 0;
		} else {
			renderType = faceRenderFlags[faceIndex] & 3;
		}
		if (renderType == 0) {
			Texture.drawShadedTriangle(screenY[vertexA], screenY[vertexB], screenY[vertexC], screenX[vertexA], screenX[vertexB], screenX[vertexC], faceShadeA[faceIndex], faceShadeB[faceIndex], faceShadeC[faceIndex]);
			return;
		}
		if (renderType == 1) {
			Texture.drawFlatTriangle(screenY[vertexA], screenY[vertexB], screenY[vertexC], screenX[vertexA], screenX[vertexB], screenX[vertexC], hslToRgbPalette[faceShadeA[faceIndex]]);
			return;
		}
		if (renderType == 2) {
			int textureFaceIndex = faceRenderFlags[faceIndex] >> 2;
			int mappingVertexA = texturedFaceVertexA[textureFaceIndex];
			int mappingVertexB = texturedFaceVertexB[textureFaceIndex];
			int mappingVertexC = texturedFaceVertexC[textureFaceIndex];
			Texture.drawTexturedTriangle(screenY[vertexA], screenY[vertexB], screenY[vertexC], screenX[vertexA], screenX[vertexB], screenX[vertexC], faceShadeA[faceIndex], faceShadeB[faceIndex], faceShadeC[faceIndex], vertexCameraX[mappingVertexA], vertexCameraX[mappingVertexB], vertexCameraX[mappingVertexC], vertexCameraY[mappingVertexA], vertexCameraY[mappingVertexB], vertexCameraY[mappingVertexC], vertexCameraZ[mappingVertexA], vertexCameraZ[mappingVertexB], vertexCameraZ[mappingVertexC], faceColors[faceIndex]);
			return;
		}
		if (renderType == 3) {
			int textureFaceIndex = faceRenderFlags[faceIndex] >> 2;
			int mappingVertexA = texturedFaceVertexA[textureFaceIndex];
			int mappingVertexB = texturedFaceVertexB[textureFaceIndex];
			int mappingVertexC = texturedFaceVertexC[textureFaceIndex];
			Texture.drawTexturedTriangle(screenY[vertexA], screenY[vertexB], screenY[vertexC], screenX[vertexA], screenX[vertexB], screenX[vertexC], faceShadeA[faceIndex], faceShadeA[faceIndex], faceShadeA[faceIndex], vertexCameraX[mappingVertexA], vertexCameraX[mappingVertexB], vertexCameraX[mappingVertexC], vertexCameraY[mappingVertexA], vertexCameraY[mappingVertexB], vertexCameraY[mappingVertexC], vertexCameraZ[mappingVertexA], vertexCameraZ[mappingVertexB], vertexCameraZ[mappingVertexC], faceColors[faceIndex]);
		}
	}

	private void drawFaceClippedToNearPlane(int faceIndex) {
		int screenCenterX = Texture.textureInt1;
		int screenCenterY = Texture.textureInt2;
		int clippedPointCount = 0;
		int vertexA = faceVertexA[faceIndex];
		int vertexB = faceVertexB[faceIndex];
		int vertexC = faceVertexC[faceIndex];
		int depthA = vertexCameraZ[vertexA];
		int depthB = vertexCameraZ[vertexB];
		int depthC = vertexCameraZ[vertexC];
		if (depthA >= 50) {
			clippedX[clippedPointCount] = screenX[vertexA];
			clippedY[clippedPointCount] = screenY[vertexA];
			clippedShade[clippedPointCount++] = faceShadeA[faceIndex];
		} else {
			int cameraXA = vertexCameraX[vertexA];
			int cameraYA = vertexCameraY[vertexA];
			int shadeA = faceShadeA[faceIndex];
			if (depthC >= 50) {
				int fractionAToC = (50 - depthA) * reciprocalTable16[depthC - depthA];
				clippedX[clippedPointCount] = screenCenterX + (cameraXA + ((vertexCameraX[vertexC] - cameraXA) * fractionAToC >> 16) << 9) / 50;
				clippedY[clippedPointCount] = screenCenterY + (cameraYA + ((vertexCameraY[vertexC] - cameraYA) * fractionAToC >> 16) << 9) / 50;
				clippedShade[clippedPointCount++] = shadeA + ((faceShadeC[faceIndex] - shadeA) * fractionAToC >> 16);
			}
			if (depthB >= 50) {
				int fractionAToB = (50 - depthA) * reciprocalTable16[depthB - depthA];
				clippedX[clippedPointCount] = screenCenterX + (cameraXA + ((vertexCameraX[vertexB] - cameraXA) * fractionAToB >> 16) << 9) / 50;
				clippedY[clippedPointCount] = screenCenterY + (cameraYA + ((vertexCameraY[vertexB] - cameraYA) * fractionAToB >> 16) << 9) / 50;
				clippedShade[clippedPointCount++] = shadeA + ((faceShadeB[faceIndex] - shadeA) * fractionAToB >> 16);
			}
		}
		if (depthB >= 50) {
			clippedX[clippedPointCount] = screenX[vertexB];
			clippedY[clippedPointCount] = screenY[vertexB];
			clippedShade[clippedPointCount++] = faceShadeB[faceIndex];
		} else {
			int cameraXB = vertexCameraX[vertexB];
			int cameraYB = vertexCameraY[vertexB];
			int shadeB = faceShadeB[faceIndex];
			if (depthA >= 50) {
				int fractionBToA = (50 - depthB) * reciprocalTable16[depthA - depthB];
				clippedX[clippedPointCount] = screenCenterX + (cameraXB + ((vertexCameraX[vertexA] - cameraXB) * fractionBToA >> 16) << 9) / 50;
				clippedY[clippedPointCount] = screenCenterY + (cameraYB + ((vertexCameraY[vertexA] - cameraYB) * fractionBToA >> 16) << 9) / 50;
				clippedShade[clippedPointCount++] = shadeB + ((faceShadeA[faceIndex] - shadeB) * fractionBToA >> 16);
			}
			if (depthC >= 50) {
				int fractionBToC = (50 - depthB) * reciprocalTable16[depthC - depthB];
				clippedX[clippedPointCount] = screenCenterX + (cameraXB + ((vertexCameraX[vertexC] - cameraXB) * fractionBToC >> 16) << 9) / 50;
				clippedY[clippedPointCount] = screenCenterY + (cameraYB + ((vertexCameraY[vertexC] - cameraYB) * fractionBToC >> 16) << 9) / 50;
				clippedShade[clippedPointCount++] = shadeB + ((faceShadeC[faceIndex] - shadeB) * fractionBToC >> 16);
			}
		}
		if (depthC >= 50) {
			clippedX[clippedPointCount] = screenX[vertexC];
			clippedY[clippedPointCount] = screenY[vertexC];
			clippedShade[clippedPointCount++] = faceShadeC[faceIndex];
		} else {
			int cameraXC = vertexCameraX[vertexC];
			int cameraYC = vertexCameraY[vertexC];
			int shadeC = faceShadeC[faceIndex];
			if (depthB >= 50) {
				int fractionCToB = (50 - depthC) * reciprocalTable16[depthB - depthC];
				clippedX[clippedPointCount] = screenCenterX + (cameraXC + ((vertexCameraX[vertexB] - cameraXC) * fractionCToB >> 16) << 9) / 50;
				clippedY[clippedPointCount] = screenCenterY + (cameraYC + ((vertexCameraY[vertexB] - cameraYC) * fractionCToB >> 16) << 9) / 50;
				clippedShade[clippedPointCount++] = shadeC + ((faceShadeB[faceIndex] - shadeC) * fractionCToB >> 16);
			}
			if (depthA >= 50) {
				int fractionCToA = (50 - depthC) * reciprocalTable16[depthA - depthC];
				clippedX[clippedPointCount] = screenCenterX + (cameraXC + ((vertexCameraX[vertexA] - cameraXC) * fractionCToA >> 16) << 9) / 50;
				clippedY[clippedPointCount] = screenCenterY + (cameraYC + ((vertexCameraY[vertexA] - cameraYC) * fractionCToA >> 16) << 9) / 50;
				clippedShade[clippedPointCount++] = shadeC + ((faceShadeA[faceIndex] - shadeC) * fractionCToA >> 16);
			}
		}
		int screenX0 = clippedX[0];
		int screenX1 = clippedX[1];
		int screenX2 = clippedX[2];
		int screenY0 = clippedY[0];
		int screenY1 = clippedY[1];
		int screenY2 = clippedY[2];
		if ((screenX0 - screenX1) * (screenY2 - screenY1) - (screenY0 - screenY1) * (screenX2 - screenX1) > 0) {
			Texture.clipScanlines = false;
			if (clippedPointCount == 3) {
				if (screenX0 < 0 || screenX1 < 0 || screenX2 < 0 || screenX0 > DrawingArea.scanlineRightLimit || screenX1 > DrawingArea.scanlineRightLimit || screenX2 > DrawingArea.scanlineRightLimit) {
					Texture.clipScanlines = true;
				}
				int renderType;
				if (faceRenderFlags == null) {
					renderType = 0;
				} else {
					renderType = faceRenderFlags[faceIndex] & 3;
				}
				if (renderType == 0) {
					Texture.drawShadedTriangle(screenY0, screenY1, screenY2, screenX0, screenX1, screenX2, clippedShade[0], clippedShade[1], clippedShade[2]);
				} else if (renderType == 1) {
					Texture.drawFlatTriangle(screenY0, screenY1, screenY2, screenX0, screenX1, screenX2, hslToRgbPalette[faceShadeA[faceIndex]]);
				} else if (renderType == 2) {
					int textureFaceIndex = faceRenderFlags[faceIndex] >> 2;
					int mappingVertexA = texturedFaceVertexA[textureFaceIndex];
					int mappingVertexB = texturedFaceVertexB[textureFaceIndex];
					int mappingVertexC = texturedFaceVertexC[textureFaceIndex];
					Texture.drawTexturedTriangle(screenY0, screenY1, screenY2, screenX0, screenX1, screenX2, clippedShade[0], clippedShade[1], clippedShade[2], vertexCameraX[mappingVertexA], vertexCameraX[mappingVertexB], vertexCameraX[mappingVertexC], vertexCameraY[mappingVertexA], vertexCameraY[mappingVertexB], vertexCameraY[mappingVertexC], vertexCameraZ[mappingVertexA], vertexCameraZ[mappingVertexB], vertexCameraZ[mappingVertexC], faceColors[faceIndex]);
				} else if (renderType == 3) {
					int textureFaceIndex = faceRenderFlags[faceIndex] >> 2;
					int mappingVertexA = texturedFaceVertexA[textureFaceIndex];
					int mappingVertexB = texturedFaceVertexB[textureFaceIndex];
					int mappingVertexC = texturedFaceVertexC[textureFaceIndex];
					Texture.drawTexturedTriangle(screenY0, screenY1, screenY2, screenX0, screenX1, screenX2, faceShadeA[faceIndex], faceShadeA[faceIndex], faceShadeA[faceIndex], vertexCameraX[mappingVertexA], vertexCameraX[mappingVertexB], vertexCameraX[mappingVertexC], vertexCameraY[mappingVertexA], vertexCameraY[mappingVertexB], vertexCameraY[mappingVertexC], vertexCameraZ[mappingVertexA], vertexCameraZ[mappingVertexB], vertexCameraZ[mappingVertexC], faceColors[faceIndex]);
				}
			}
			if (clippedPointCount == 4) {
				if (screenX0 < 0 || screenX1 < 0 || screenX2 < 0 || screenX0 > DrawingArea.scanlineRightLimit || screenX1 > DrawingArea.scanlineRightLimit || screenX2 > DrawingArea.scanlineRightLimit || clippedX[3] < 0 || clippedX[3] > DrawingArea.scanlineRightLimit) {
					Texture.clipScanlines = true;
				}
				int renderType;
				if (faceRenderFlags == null) {
					renderType = 0;
				} else {
					renderType = faceRenderFlags[faceIndex] & 3;
				}
				if (renderType == 0) {
					Texture.drawShadedTriangle(screenY0, screenY1, screenY2, screenX0, screenX1, screenX2, clippedShade[0], clippedShade[1], clippedShade[2]);
					Texture.drawShadedTriangle(screenY0, screenY2, clippedY[3], screenX0, screenX2, clippedX[3], clippedShade[0], clippedShade[2], clippedShade[3]);
					return;
				}
				if (renderType == 1) {
					int flatColor = hslToRgbPalette[faceShadeA[faceIndex]];
					Texture.drawFlatTriangle(screenY0, screenY1, screenY2, screenX0, screenX1, screenX2, flatColor);
					Texture.drawFlatTriangle(screenY0, screenY2, clippedY[3], screenX0, screenX2, clippedX[3], flatColor);
					return;
				}
				if (renderType == 2) {
					int textureFaceIndex = faceRenderFlags[faceIndex] >> 2;
					int mappingVertexA = texturedFaceVertexA[textureFaceIndex];
					int mappingVertexB = texturedFaceVertexB[textureFaceIndex];
					int mappingVertexC = texturedFaceVertexC[textureFaceIndex];
					Texture.drawTexturedTriangle(screenY0, screenY1, screenY2, screenX0, screenX1, screenX2, clippedShade[0], clippedShade[1], clippedShade[2], vertexCameraX[mappingVertexA], vertexCameraX[mappingVertexB], vertexCameraX[mappingVertexC], vertexCameraY[mappingVertexA], vertexCameraY[mappingVertexB], vertexCameraY[mappingVertexC], vertexCameraZ[mappingVertexA], vertexCameraZ[mappingVertexB], vertexCameraZ[mappingVertexC], faceColors[faceIndex]);
					Texture.drawTexturedTriangle(screenY0, screenY2, clippedY[3], screenX0, screenX2, clippedX[3], clippedShade[0], clippedShade[2], clippedShade[3], vertexCameraX[mappingVertexA], vertexCameraX[mappingVertexB], vertexCameraX[mappingVertexC], vertexCameraY[mappingVertexA], vertexCameraY[mappingVertexB], vertexCameraY[mappingVertexC], vertexCameraZ[mappingVertexA], vertexCameraZ[mappingVertexB], vertexCameraZ[mappingVertexC], faceColors[faceIndex]);
					return;
				}
				if (renderType == 3) {
					int textureFaceIndex = faceRenderFlags[faceIndex] >> 2;
					int mappingVertexA = texturedFaceVertexA[textureFaceIndex];
					int mappingVertexB = texturedFaceVertexB[textureFaceIndex];
					int mappingVertexC = texturedFaceVertexC[textureFaceIndex];
					Texture.drawTexturedTriangle(screenY0, screenY1, screenY2, screenX0, screenX1, screenX2, faceShadeA[faceIndex], faceShadeA[faceIndex], faceShadeA[faceIndex], vertexCameraX[mappingVertexA], vertexCameraX[mappingVertexB], vertexCameraX[mappingVertexC], vertexCameraY[mappingVertexA], vertexCameraY[mappingVertexB], vertexCameraY[mappingVertexC], vertexCameraZ[mappingVertexA], vertexCameraZ[mappingVertexB], vertexCameraZ[mappingVertexC], faceColors[faceIndex]);
					Texture.drawTexturedTriangle(screenY0, screenY2, clippedY[3], screenX0, screenX2, clippedX[3], faceShadeA[faceIndex], faceShadeA[faceIndex], faceShadeA[faceIndex], vertexCameraX[mappingVertexA], vertexCameraX[mappingVertexB], vertexCameraX[mappingVertexC], vertexCameraY[mappingVertexA], vertexCameraY[mappingVertexB], vertexCameraY[mappingVertexC], vertexCameraZ[mappingVertexA], vertexCameraZ[mappingVertexB], vertexCameraZ[mappingVertexC], faceColors[faceIndex]);
				}
			}
		}
	}

	private boolean isPointWithinFaceBounds(int pointX, int pointY, int vertexAY, int vertexBY, int vertexCY, int vertexAX, int vertexBX, int vertexCX) {
		if (pointY < vertexAY && pointY < vertexBY && pointY < vertexCY) {
			return false;
		}
		if (pointY > vertexAY && pointY > vertexBY && pointY > vertexCY) {
			return false;
		}
		return !(pointX < vertexAX && pointX < vertexBX && pointX < vertexCX) && (pointX <= vertexAX || pointX <= vertexBX || pointX <= vertexCX);
	}

	public static final Model animationScratchModel = new Model();
	private static int[] animationScratchX = new int[2000];
	private static int[] animationScratchY = new int[2000];
	private static int[] animationScratchZ = new int[2000];
	private static int[] animationScratchAlpha = new int[2000];
	public int vertexCount;
	public int vertexX[];
	public int vertexY[];
	public int vertexZ[];
	public int faceCount;
	public int faceVertexA[];
	public int faceVertexB[];
	public int faceVertexC[];
	private int[] faceShadeA;
	private int[] faceShadeB;
	private int[] faceShadeC;
	public int faceRenderFlags[];
	private int[] facePriorities;
	private int[] faceAlphas;
	public int faceColors[];
	private int uniformFacePriority;
	private int texturedFaceCount;
	private int[] texturedFaceVertexA;
	private int[] texturedFaceVertexB;
	private int[] texturedFaceVertexC;
	public int minX;
	public int maxX;
	public int maxZ;
	public int minZ;
	public int radiusXZ;
	public int maxY;
	private int depthBucketCount;
	private int depthBucketOffset;
	public int stackSurfaceHeight;
	private int[] vertexGroupIds;
	private int[] faceGroupIds;
	public int vertexGroups[][];
	public int faceGroups[][];
	public boolean pickByBounds;
	VertexNormal originalVertexNormals[];
	private static ModelHeader[] modelHeaders;
	private static OnDemandFetcherParent modelRequester;
	private static boolean[] faceNeedsHorizontalClip = new boolean[4096];
	private static boolean[] faceNearPlaneClipped = new boolean[4096];
	private static int[] screenX = new int[4096];
	private static int[] screenY = new int[4096];
	private static int[] vertexDepth = new int[4096];
	private static int[] vertexCameraX = new int[4096];
	private static int[] vertexCameraY = new int[4096];
	private static int[] vertexCameraZ = new int[4096];
	private static int[] depthBucketFaceCounts = new int[1500];
	private static int[][] depthBucketFaces = new int[1500][512];
	private static int[] priorityFaceCounts = new int[12];
	private static int[][] priorityFaces = new int[12][2000];
	private static int[] priority10Depths = new int[2000];
	private static int[] priority11Depths = new int[2000];
	private static int[] priorityDepthSums = new int[12];
	private static final int[] clippedX = new int[10];
	private static final int[] clippedY = new int[10];
	private static final int[] clippedShade = new int[10];
	private static int animationPivotX;
	private static int animationPivotY;
	private static int animationPivotZ;
	public static boolean pickingEnabled;
	public static int pickMouseX;
	public static int pickMouseY;
	public static int pickedCount;
	public static final int[] pickedIds = new int[1000];
	public static int sineTable16[];
	public static int cosineTable16[];
	private static int[] hslToRgbPalette;
	private static int[] reciprocalTable16;

	static {
		sineTable16 = Texture.sineTable;
		cosineTable16 = Texture.cosineTable;
		hslToRgbPalette = Texture.hslToRgbPalette;
		reciprocalTable16 = Texture.reciprocalTable16;
	}
}
