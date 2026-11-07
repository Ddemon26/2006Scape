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
		anIntArray1668 = null;
		anIntArray1669 = null;
		anIntArray1670 = null;
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

	public static void method459(int i, OnDemandFetcherParent onDemandFetcherParent) {
		modelHeaders = new ModelHeader[i];
		modelRequester = onDemandFetcherParent;
	}

	public static void method460(byte data[], int modelId) {
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

	public static void method461(int modelId) {
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
		aBoolean1659 = false;
	}

	private Model(int modelId) {
		aBoolean1659 = false;
		ModelHeader header = modelHeaders[modelId];
		anInt1626 = header.vertexCount;
		anInt1630 = header.faceCount;
		texturedFaceCount = header.texturedFaceCount;
		anIntArray1627 = new int[anInt1626];
		anIntArray1628 = new int[anInt1626];
		anIntArray1629 = new int[anInt1626];
		anIntArray1631 = new int[anInt1630];
		anIntArray1632 = new int[anInt1630];
		anIntArray1633 = new int[anInt1630];
		texturedFaceVertexA = new int[texturedFaceCount];
		texturedFaceVertexB = new int[texturedFaceCount];
		texturedFaceVertexC = new int[texturedFaceCount];
		if (header.vertexGroupIdsOffset >= 0) {
			vertexGroupIds = new int[anInt1626];
		}
		if (header.faceRenderFlagsOffset >= 0) {
			anIntArray1637 = new int[anInt1630];
		}
		if (header.facePrioritiesOffset >= 0) {
			facePriorities = new int[anInt1630];
		} else {
			uniformFacePriority = -header.facePrioritiesOffset - 1;
		}
		if (header.faceAlphasOffset >= 0) {
			faceAlphas = new int[anInt1630];
		}
		if (header.faceGroupIdsOffset >= 0) {
			faceGroupIds = new int[anInt1630];
		}
		anIntArray1640 = new int[anInt1630];
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
		for (int vertexIndex = 0; vertexIndex < anInt1626; vertexIndex++) {
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
			anIntArray1627[vertexIndex] = previousX + deltaX;
			anIntArray1628[vertexIndex] = previousY + deltaY;
			anIntArray1629[vertexIndex] = previousZ + deltaZ;
			previousX = anIntArray1627[vertexIndex];
			previousY = anIntArray1628[vertexIndex];
			previousZ = anIntArray1629[vertexIndex];
			if (vertexGroupIds != null) {
				vertexGroupIds[vertexIndex] = stream_4.readUnsignedByte();
			}
		}

		stream.currentOffset = header.faceColoursOffset;
		stream_1.currentOffset = header.faceRenderFlagsOffset;
		stream_2.currentOffset = header.facePrioritiesOffset;
		stream_3.currentOffset = header.faceAlphasOffset;
		stream_4.currentOffset = header.faceGroupIdsOffset;
		for (int faceIndex = 0; faceIndex < anInt1630; faceIndex++) {
			anIntArray1640[faceIndex] = stream.readUnsignedShortBigEndian();
			if (anIntArray1637 != null) {
				anIntArray1637[faceIndex] = stream_1.readUnsignedByte();
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
		for (int faceIndex = 0; faceIndex < anInt1630; faceIndex++) {
			int faceType = stream_1.readUnsignedByte();
			if (faceType == 1) {
				vertexA = stream.readSignedSmart() + previousVertex;
				previousVertex = vertexA;
				vertexB = stream.readSignedSmart() + previousVertex;
				previousVertex = vertexB;
				vertexC = stream.readSignedSmart() + previousVertex;
				previousVertex = vertexC;
				anIntArray1631[faceIndex] = vertexA;
				anIntArray1632[faceIndex] = vertexB;
				anIntArray1633[faceIndex] = vertexC;
			}
			if (faceType == 2) {
				vertexB = vertexC;
				vertexC = stream.readSignedSmart() + previousVertex;
				previousVertex = vertexC;
				anIntArray1631[faceIndex] = vertexA;
				anIntArray1632[faceIndex] = vertexB;
				anIntArray1633[faceIndex] = vertexC;
			}
			if (faceType == 3) {
				vertexA = vertexC;
				vertexC = stream.readSignedSmart() + previousVertex;
				previousVertex = vertexC;
				anIntArray1631[faceIndex] = vertexA;
				anIntArray1632[faceIndex] = vertexB;
				anIntArray1633[faceIndex] = vertexC;
			}
			if (faceType == 4) {
				int swapTemp = vertexA;
				vertexA = vertexB;
				vertexB = swapTemp;
				vertexC = stream.readSignedSmart() + previousVertex;
				previousVertex = vertexC;
				anIntArray1631[faceIndex] = vertexA;
				anIntArray1632[faceIndex] = vertexB;
				anIntArray1633[faceIndex] = vertexC;
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
		aBoolean1659 = false;
		boolean needsFaceRenderFlags = false;
		boolean needsFacePriorities = false;
		boolean needsFaceAlpha = false;
		boolean needsFaceGroupIds = false;
		anInt1626 = 0;
		anInt1630 = 0;
		texturedFaceCount = 0;
		uniformFacePriority = -1;
		for (int partIndex = 0; partIndex < partCount; partIndex++) {
			Model sourcePart = parts[partIndex];
			if (sourcePart != null) {
				anInt1626 += sourcePart.anInt1626;
				anInt1630 += sourcePart.anInt1630;
				texturedFaceCount += sourcePart.texturedFaceCount;
				needsFaceRenderFlags |= sourcePart.anIntArray1637 != null;
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

		anIntArray1627 = new int[anInt1626];
		anIntArray1628 = new int[anInt1626];
		anIntArray1629 = new int[anInt1626];
		vertexGroupIds = new int[anInt1626];
		anIntArray1631 = new int[anInt1630];
		anIntArray1632 = new int[anInt1630];
		anIntArray1633 = new int[anInt1630];
		texturedFaceVertexA = new int[texturedFaceCount];
		texturedFaceVertexB = new int[texturedFaceCount];
		texturedFaceVertexC = new int[texturedFaceCount];
		if (needsFaceRenderFlags) {
			anIntArray1637 = new int[anInt1630];
		}
		if (needsFacePriorities) {
			facePriorities = new int[anInt1630];
		}
		if (needsFaceAlpha) {
			faceAlphas = new int[anInt1630];
		}
		if (needsFaceGroupIds) {
			faceGroupIds = new int[anInt1630];
		}
		anIntArray1640 = new int[anInt1630];
		anInt1626 = 0;
		anInt1630 = 0;
		texturedFaceCount = 0;
		int textureTriangleOffset = 0;
		for (int partIndex = 0; partIndex < partCount; partIndex++) {
			Model sourcePart = parts[partIndex];
			if (sourcePart != null) {
				for (int faceIndex = 0; faceIndex < sourcePart.anInt1630; faceIndex++) {
					if (needsFaceRenderFlags) {
						if (sourcePart.anIntArray1637 == null) {
							anIntArray1637[anInt1630] = 0;
						} else {
							int faceRenderFlags = sourcePart.anIntArray1637[faceIndex];
							if ((faceRenderFlags & 2) == 2) {
								faceRenderFlags += textureTriangleOffset << 2;
							}
							anIntArray1637[anInt1630] = faceRenderFlags;
						}
					}
					if (needsFacePriorities) {
						if (sourcePart.facePriorities == null) {
							facePriorities[anInt1630] = sourcePart.uniformFacePriority;
						} else {
							facePriorities[anInt1630] = sourcePart.facePriorities[faceIndex];
						}
					}
					if (needsFaceAlpha) {
						if (sourcePart.faceAlphas == null) {
							faceAlphas[anInt1630] = 0;
						} else {
							faceAlphas[anInt1630] = sourcePart.faceAlphas[faceIndex];
						}
					}
					if (needsFaceGroupIds && sourcePart.faceGroupIds != null) {
						faceGroupIds[anInt1630] = sourcePart.faceGroupIds[faceIndex];
					}
					anIntArray1640[anInt1630] = sourcePart.anIntArray1640[faceIndex];
					anIntArray1631[anInt1630] = getOrAddVertex(sourcePart, sourcePart.anIntArray1631[faceIndex]);
					anIntArray1632[anInt1630] = getOrAddVertex(sourcePart, sourcePart.anIntArray1632[faceIndex]);
					anIntArray1633[anInt1630] = getOrAddVertex(sourcePart, sourcePart.anIntArray1633[faceIndex]);
					anInt1630++;
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
		aBoolean1659 = false;
		boolean needsFaceRenderFlags = false;
		boolean needsFacePriorities = false;
		boolean needsFaceAlpha = false;
		boolean needsFaceValues = false;
		anInt1626 = 0;
		anInt1630 = 0;
		texturedFaceCount = 0;
		uniformFacePriority = -1;
		for (int partIndex = 0; partIndex < partCount; partIndex++) {
			Model sourcePart = parts[partIndex];
			if (sourcePart != null) {
				anInt1626 += sourcePart.anInt1626;
				anInt1630 += sourcePart.anInt1630;
				texturedFaceCount += sourcePart.texturedFaceCount;
				needsFaceRenderFlags |= sourcePart.anIntArray1637 != null;
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
				needsFaceValues |= sourcePart.anIntArray1640 != null;
			}
		}

		anIntArray1627 = new int[anInt1626];
		anIntArray1628 = new int[anInt1626];
		anIntArray1629 = new int[anInt1626];
		anIntArray1631 = new int[anInt1630];
		anIntArray1632 = new int[anInt1630];
		anIntArray1633 = new int[anInt1630];
		faceShadeA = new int[anInt1630];
		faceShadeB = new int[anInt1630];
		faceShadeC = new int[anInt1630];
		texturedFaceVertexA = new int[texturedFaceCount];
		texturedFaceVertexB = new int[texturedFaceCount];
		texturedFaceVertexC = new int[texturedFaceCount];
		if (needsFaceRenderFlags) {
			anIntArray1637 = new int[anInt1630];
		}
		if (needsFacePriorities) {
			facePriorities = new int[anInt1630];
		}
		if (needsFaceAlpha) {
			faceAlphas = new int[anInt1630];
		}
		if (needsFaceValues) {
			anIntArray1640 = new int[anInt1630];
		}
		anInt1626 = 0;
		anInt1630 = 0;
		texturedFaceCount = 0;
		int textureTriangleOffset = 0;
		for (int partIndex = 0; partIndex < partCount; partIndex++) {
			Model sourcePart = parts[partIndex];
			if (sourcePart != null) {
				int vertexOffset = anInt1626;
				for (int vertexIndex = 0; vertexIndex < sourcePart.anInt1626; vertexIndex++) {
					anIntArray1627[anInt1626] = sourcePart.anIntArray1627[vertexIndex];
					anIntArray1628[anInt1626] = sourcePart.anIntArray1628[vertexIndex];
					anIntArray1629[anInt1626] = sourcePart.anIntArray1629[vertexIndex];
					anInt1626++;
				}

				for (int faceIndex = 0; faceIndex < sourcePart.anInt1630; faceIndex++) {
					anIntArray1631[anInt1630] = sourcePart.anIntArray1631[faceIndex] + vertexOffset;
					anIntArray1632[anInt1630] = sourcePart.anIntArray1632[faceIndex] + vertexOffset;
					anIntArray1633[anInt1630] = sourcePart.anIntArray1633[faceIndex] + vertexOffset;
					faceShadeA[anInt1630] = sourcePart.faceShadeA[faceIndex];
					faceShadeB[anInt1630] = sourcePart.faceShadeB[faceIndex];
					faceShadeC[anInt1630] = sourcePart.faceShadeC[faceIndex];
					if (needsFaceRenderFlags) {
						if (sourcePart.anIntArray1637 == null) {
							anIntArray1637[anInt1630] = 0;
						} else {
							int faceRenderFlags = sourcePart.anIntArray1637[faceIndex];
							if ((faceRenderFlags & 2) == 2) {
								faceRenderFlags += textureTriangleOffset << 2;
							}
							anIntArray1637[anInt1630] = faceRenderFlags;
						}
					}
					if (needsFacePriorities) {
						if (sourcePart.facePriorities == null) {
							facePriorities[anInt1630] = sourcePart.uniformFacePriority;
						} else {
							facePriorities[anInt1630] = sourcePart.facePriorities[faceIndex];
						}
					}
					if (needsFaceAlpha) {
						if (sourcePart.faceAlphas == null) {
							faceAlphas[anInt1630] = 0;
						} else {
							faceAlphas[anInt1630] = sourcePart.faceAlphas[faceIndex];
						}
					}
					if (needsFaceValues && sourcePart.anIntArray1640 != null) {
						anIntArray1640[anInt1630] = sourcePart.anIntArray1640[faceIndex];
					}
					anInt1630++;
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
		aBoolean1659 = false;
		anInt1626 = sourceModel.anInt1626;
		anInt1630 = sourceModel.anInt1630;
		texturedFaceCount = sourceModel.texturedFaceCount;
		if (shareVertices) {
			anIntArray1627 = sourceModel.anIntArray1627;
			anIntArray1628 = sourceModel.anIntArray1628;
			anIntArray1629 = sourceModel.anIntArray1629;
		} else {
			anIntArray1627 = new int[anInt1626];
			anIntArray1628 = new int[anInt1626];
			anIntArray1629 = new int[anInt1626];
			for (int vertexIndex = 0; vertexIndex < anInt1626; vertexIndex++) {
				anIntArray1627[vertexIndex] = sourceModel.anIntArray1627[vertexIndex];
				anIntArray1628[vertexIndex] = sourceModel.anIntArray1628[vertexIndex];
				anIntArray1629[vertexIndex] = sourceModel.anIntArray1629[vertexIndex];
			}

		}
		if (shareFaceValues) {
			anIntArray1640 = sourceModel.anIntArray1640;
		} else {
			anIntArray1640 = new int[anInt1630];
			System.arraycopy(sourceModel.anIntArray1640, 0, anIntArray1640, 0, anInt1630);

		}
		if (shareFaceAlpha) {
			faceAlphas = sourceModel.faceAlphas;
		} else {
			faceAlphas = new int[anInt1630];
			if (sourceModel.faceAlphas == null) {
				for (int faceIndex = 0; faceIndex < anInt1630; faceIndex++) {
					faceAlphas[faceIndex] = 0;
				}

			} else {
				System.arraycopy(sourceModel.faceAlphas, 0, faceAlphas, 0, anInt1630);

			}
		}
		vertexGroupIds = sourceModel.vertexGroupIds;
		faceGroupIds = sourceModel.faceGroupIds;
		anIntArray1637 = sourceModel.anIntArray1637;
		anIntArray1631 = sourceModel.anIntArray1631;
		anIntArray1632 = sourceModel.anIntArray1632;
		anIntArray1633 = sourceModel.anIntArray1633;
		facePriorities = sourceModel.facePriorities;
		uniformFacePriority = sourceModel.uniformFacePriority;
		texturedFaceVertexA = sourceModel.texturedFaceVertexA;
		texturedFaceVertexB = sourceModel.texturedFaceVertexB;
		texturedFaceVertexC = sourceModel.texturedFaceVertexC;
	}

	public Model(boolean copyVertexY, boolean copyLightingData, Model sourceModel) {
		aBoolean1659 = false;
		anInt1626 = sourceModel.anInt1626;
		anInt1630 = sourceModel.anInt1630;
		texturedFaceCount = sourceModel.texturedFaceCount;
		if (copyVertexY) {
			anIntArray1628 = new int[anInt1626];
			System.arraycopy(sourceModel.anIntArray1628, 0, anIntArray1628, 0, anInt1626);

		} else {
			anIntArray1628 = sourceModel.anIntArray1628;
		}
		if (copyLightingData) {
			faceShadeA = new int[anInt1630];
			faceShadeB = new int[anInt1630];
			faceShadeC = new int[anInt1630];
			for (int faceIndex = 0; faceIndex < anInt1630; faceIndex++) {
				faceShadeA[faceIndex] = sourceModel.faceShadeA[faceIndex];
				faceShadeB[faceIndex] = sourceModel.faceShadeB[faceIndex];
				faceShadeC[faceIndex] = sourceModel.faceShadeC[faceIndex];
			}

			anIntArray1637 = new int[anInt1630];
			if (sourceModel.anIntArray1637 == null) {
				for (int faceIndex = 0; faceIndex < anInt1630; faceIndex++) {
					anIntArray1637[faceIndex] = 0;
				}

			} else {
				System.arraycopy(sourceModel.anIntArray1637, 0, anIntArray1637, 0, anInt1630);

			}
			super.aClass33Array1425 = new VertexNormal[anInt1626];
			for (int vertexIndex = 0; vertexIndex < anInt1626; vertexIndex++) {
				VertexNormal copiedNormal = super.aClass33Array1425[vertexIndex] = new VertexNormal();
				VertexNormal sourceNormal = sourceModel.aClass33Array1425[vertexIndex];
				copiedNormal.normalX = sourceNormal.normalX;
				copiedNormal.normalY = sourceNormal.normalY;
				copiedNormal.normalZ = sourceNormal.normalZ;
				copiedNormal.faceCount = sourceNormal.faceCount;
			}

			aClass33Array1660 = sourceModel.aClass33Array1660;
		} else {
			faceShadeA = sourceModel.faceShadeA;
			faceShadeB = sourceModel.faceShadeB;
			faceShadeC = sourceModel.faceShadeC;
			anIntArray1637 = sourceModel.anIntArray1637;
		}
		anIntArray1627 = sourceModel.anIntArray1627;
		anIntArray1629 = sourceModel.anIntArray1629;
		anIntArray1640 = sourceModel.anIntArray1640;
		faceAlphas = sourceModel.faceAlphas;
		facePriorities = sourceModel.facePriorities;
		uniformFacePriority = sourceModel.uniformFacePriority;
		anIntArray1631 = sourceModel.anIntArray1631;
		anIntArray1632 = sourceModel.anIntArray1632;
		anIntArray1633 = sourceModel.anIntArray1633;
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
		anInt1626 = sourceModel.anInt1626;
		anInt1630 = sourceModel.anInt1630;
		texturedFaceCount = sourceModel.texturedFaceCount;
		if (animationScratchX.length < anInt1626) {
			animationScratchX = new int[anInt1626 + 100];
			animationScratchY = new int[anInt1626 + 100];
			animationScratchZ = new int[anInt1626 + 100];
		}
		anIntArray1627 = animationScratchX;
		anIntArray1628 = animationScratchY;
		anIntArray1629 = animationScratchZ;
		for (int vertexIndex = 0; vertexIndex < anInt1626; vertexIndex++) {
			anIntArray1627[vertexIndex] = sourceModel.anIntArray1627[vertexIndex];
			anIntArray1628[vertexIndex] = sourceModel.anIntArray1628[vertexIndex];
			anIntArray1629[vertexIndex] = sourceModel.anIntArray1629[vertexIndex];
		}

		if (shareFaceAlpha) {
			faceAlphas = sourceModel.faceAlphas;
		} else {
			if (animationScratchAlpha.length < anInt1630) {
				animationScratchAlpha = new int[anInt1630 + 100];
			}
			faceAlphas = animationScratchAlpha;
			if (sourceModel.faceAlphas == null) {
				for (int faceIndex = 0; faceIndex < anInt1630; faceIndex++) {
					faceAlphas[faceIndex] = 0;
				}

			} else {
				System.arraycopy(sourceModel.faceAlphas, 0, faceAlphas, 0, anInt1630);

			}
		}
		anIntArray1637 = sourceModel.anIntArray1637;
		anIntArray1640 = sourceModel.anIntArray1640;
		facePriorities = sourceModel.facePriorities;
		uniformFacePriority = sourceModel.uniformFacePriority;
		faceGroups = sourceModel.faceGroups;
		vertexGroups = sourceModel.vertexGroups;
		anIntArray1631 = sourceModel.anIntArray1631;
		anIntArray1632 = sourceModel.anIntArray1632;
		anIntArray1633 = sourceModel.anIntArray1633;
		faceShadeA = sourceModel.faceShadeA;
		faceShadeB = sourceModel.faceShadeB;
		faceShadeC = sourceModel.faceShadeC;
		texturedFaceVertexA = sourceModel.texturedFaceVertexA;
		texturedFaceVertexB = sourceModel.texturedFaceVertexB;
		texturedFaceVertexC = sourceModel.texturedFaceVertexC;
	}

	private int getOrAddVertex(Model sourceModel, int sourceVertexIndex) {
		int destinationVertexIndex = -1;
		int vertexX = sourceModel.anIntArray1627[sourceVertexIndex];
		int vertexY = sourceModel.anIntArray1628[sourceVertexIndex];
		int vertexZ = sourceModel.anIntArray1629[sourceVertexIndex];
		for (int existingVertexIndex = 0; existingVertexIndex < anInt1626; existingVertexIndex++) {
			if (vertexX != anIntArray1627[existingVertexIndex] || vertexY != anIntArray1628[existingVertexIndex] || vertexZ != anIntArray1629[existingVertexIndex]) {
				continue;
			}
			destinationVertexIndex = existingVertexIndex;
			break;
		}

		if (destinationVertexIndex == -1) {
			anIntArray1627[anInt1626] = vertexX;
			anIntArray1628[anInt1626] = vertexY;
			anIntArray1629[anInt1626] = vertexZ;
			if (sourceModel.vertexGroupIds != null) {
				vertexGroupIds[anInt1626] = sourceModel.vertexGroupIds[sourceVertexIndex];
			}
			destinationVertexIndex = anInt1626++;
		}
		return destinationVertexIndex;
	}

	public void calculateBounds() {
		super.modelHeight = 0;
		radiusXZ = 0;
		maxY = 0;
		for (int vertexIndex = 0; vertexIndex < anInt1626; vertexIndex++) {
			int vertexX = anIntArray1627[vertexIndex];
			int vertexY = anIntArray1628[vertexIndex];
			int vertexZ = anIntArray1629[vertexIndex];
			if (-vertexY > super.modelHeight) {
				super.modelHeight = -vertexY;
			}
			if (vertexY > maxY) {
				maxY = vertexY;
			}
			int radiusSquaredXZ = vertexX * vertexX + vertexZ * vertexZ;
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
		for (int vertexIndex = 0; vertexIndex < anInt1626; vertexIndex++) {
			int vertexY = anIntArray1628[vertexIndex];
			if (-vertexY > super.modelHeight) {
				super.modelHeight = -vertexY;
			}
			if (vertexY > maxY) {
				maxY = vertexY;
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
		for (int vertexIndex = 0; vertexIndex < anInt1626; vertexIndex++) {
			int vertexX = anIntArray1627[vertexIndex];
			int vertexY = anIntArray1628[vertexIndex];
			int vertexZ = anIntArray1629[vertexIndex];
			if (vertexX < minX) {
				minX = vertexX;
			}
			if (vertexX > maxX) {
				maxX = vertexX;
			}
			if (vertexZ < minZ) {
				minZ = vertexZ;
			}
			if (vertexZ > maxZ) {
				maxZ = vertexZ;
			}
			if (-vertexY > super.modelHeight) {
				super.modelHeight = -vertexY;
			}
			if (vertexY > maxY) {
				maxY = vertexY;
			}
			int radiusSquaredXZ = vertexX * vertexX + vertexZ * vertexZ;
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
			for (int vertexIndex = 0; vertexIndex < anInt1626; vertexIndex++) {
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

			for (int vertexIndex = 0; vertexIndex < anInt1626; vertexIndex++) {
				int vertexGroupId = vertexGroupIds[vertexIndex];
				vertexGroups[vertexGroupId][vertexGroupCounts[vertexGroupId]++] = vertexIndex;
			}

			vertexGroupIds = null;
		}
		if (faceGroupIds != null) {
			int faceGroupCounts[] = new int[256];
			int maxFaceGroupId = 0;
			for (int faceIndex = 0; faceIndex < anInt1630; faceIndex++) {
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

			for (int faceIndex = 0; faceIndex < anInt1630; faceIndex++) {
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
						animationPivotX += anIntArray1627[vertexIndex];
						animationPivotY += anIntArray1628[vertexIndex];
						animationPivotZ += anIntArray1629[vertexIndex];
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
						anIntArray1627[vertexIndex] += x;
						anIntArray1628[vertexIndex] += y;
						anIntArray1629[vertexIndex] += z;
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
						anIntArray1627[vertexIndex] -= animationPivotX;
						anIntArray1628[vertexIndex] -= animationPivotY;
						anIntArray1629[vertexIndex] -= animationPivotZ;
						int rotationX2048 = (x & 0xff) * 8;
						int rotationY2048 = (y & 0xff) * 8;
						int rotationZ2048 = (z & 0xff) * 8;
						if (rotationZ2048 != 0) {
							int sineZ16 = sineTable16[rotationZ2048];
							int cosineZ16 = cosineTable16[rotationZ2048];
							int rotatedX = anIntArray1628[vertexIndex] * sineZ16 + anIntArray1627[vertexIndex] * cosineZ16 >> 16;
							anIntArray1628[vertexIndex] = anIntArray1628[vertexIndex] * cosineZ16 - anIntArray1627[vertexIndex] * sineZ16 >> 16;
							anIntArray1627[vertexIndex] = rotatedX;
						}
						if (rotationX2048 != 0) {
							int sineX16 = sineTable16[rotationX2048];
							int cosineX16 = cosineTable16[rotationX2048];
							int rotatedY = anIntArray1628[vertexIndex] * cosineX16 - anIntArray1629[vertexIndex] * sineX16 >> 16;
							anIntArray1629[vertexIndex] = anIntArray1628[vertexIndex] * sineX16 + anIntArray1629[vertexIndex] * cosineX16 >> 16;
							anIntArray1628[vertexIndex] = rotatedY;
						}
						if (rotationY2048 != 0) {
							int sineY16 = sineTable16[rotationY2048];
							int cosineY16 = cosineTable16[rotationY2048];
							int rotatedX = anIntArray1629[vertexIndex] * sineY16 + anIntArray1627[vertexIndex] * cosineY16 >> 16;
							anIntArray1629[vertexIndex] = anIntArray1629[vertexIndex] * cosineY16 - anIntArray1627[vertexIndex] * sineY16 >> 16;
							anIntArray1627[vertexIndex] = rotatedX;
						}
						anIntArray1627[vertexIndex] += animationPivotX;
						anIntArray1628[vertexIndex] += animationPivotY;
						anIntArray1629[vertexIndex] += animationPivotZ;
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
						anIntArray1627[vertexIndex] -= animationPivotX;
						anIntArray1628[vertexIndex] -= animationPivotY;
						anIntArray1629[vertexIndex] -= animationPivotZ;
						anIntArray1627[vertexIndex] = anIntArray1627[vertexIndex] * x / 128;
						anIntArray1628[vertexIndex] = anIntArray1628[vertexIndex] * y / 128;
						anIntArray1629[vertexIndex] = anIntArray1629[vertexIndex] * z / 128;
						anIntArray1627[vertexIndex] += animationPivotX;
						anIntArray1628[vertexIndex] += animationPivotY;
						anIntArray1629[vertexIndex] += animationPivotZ;
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
		for (int vertexIndex = 0; vertexIndex < anInt1626; vertexIndex++) {
			int originalX = anIntArray1627[vertexIndex];
			anIntArray1627[vertexIndex] = anIntArray1629[vertexIndex];
			anIntArray1629[vertexIndex] = -originalX;
		}

	}

	public void rotateX(int angle2048) {
		int sine16 = sineTable16[angle2048];
		int cosine16 = cosineTable16[angle2048];
		for (int vertexIndex = 0; vertexIndex < anInt1626; vertexIndex++) {
			int rotatedY = anIntArray1628[vertexIndex] * cosine16 - anIntArray1629[vertexIndex] * sine16 >> 16;
			anIntArray1629[vertexIndex] = anIntArray1628[vertexIndex] * sine16 + anIntArray1629[vertexIndex] * cosine16 >> 16;
			anIntArray1628[vertexIndex] = rotatedY;
		}
	}

	public void translate(int deltaX, int deltaY, int deltaZ) {
		for (int vertexIndex = 0; vertexIndex < anInt1626; vertexIndex++) {
			anIntArray1627[vertexIndex] += deltaX;
			anIntArray1628[vertexIndex] += deltaY;
			anIntArray1629[vertexIndex] += deltaZ;
		}

	}

	public void recolor(int sourceValue, int replacementValue) {
		for (int faceIndex = 0; faceIndex < anInt1630; faceIndex++) {
			if (anIntArray1640[faceIndex] == sourceValue) {
				anIntArray1640[faceIndex] = replacementValue;
			}
		}

	}

	public void reflectZAndReverseWinding() {
		for (int vertexIndex = 0; vertexIndex < anInt1626; vertexIndex++) {
			anIntArray1629[vertexIndex] = -anIntArray1629[vertexIndex];
		}

		for (int faceIndex = 0; faceIndex < anInt1630; faceIndex++) {
			int originalFirstVertex = anIntArray1631[faceIndex];
			anIntArray1631[faceIndex] = anIntArray1633[faceIndex];
			anIntArray1633[faceIndex] = originalFirstVertex;
		}
	}

	public void scale(int scaleX128, int scaleZ128, int scaleY128) {
		for (int vertexIndex = 0; vertexIndex < anInt1626; vertexIndex++) {
			anIntArray1627[vertexIndex] = anIntArray1627[vertexIndex] * scaleX128 / 128;
			anIntArray1628[vertexIndex] = anIntArray1628[vertexIndex] * scaleY128 / 128;
			anIntArray1629[vertexIndex] = anIntArray1629[vertexIndex] * scaleZ128 / 128;
		}

	}

	public void applyLighting(int ambientLight, int lightDivisorScale, int lightX, int lightY, int lightZ, boolean bakeShading) {
		int lightLength = (int) Math.sqrt(lightX * lightX + lightY * lightY + lightZ * lightZ);
		int lightDivisor = lightDivisorScale * lightLength >> 8;
		if (faceShadeA == null) {
			faceShadeA = new int[anInt1630];
			faceShadeB = new int[anInt1630];
			faceShadeC = new int[anInt1630];
		}
		if (super.aClass33Array1425 == null) {
			super.aClass33Array1425 = new VertexNormal[anInt1626];
			for (int vertexIndex = 0; vertexIndex < anInt1626; vertexIndex++) {
				super.aClass33Array1425[vertexIndex] = new VertexNormal();
			}

		}
		for (int faceIndex = 0; faceIndex < anInt1630; faceIndex++) {
			int vertexA = anIntArray1631[faceIndex];
			int vertexB = anIntArray1632[faceIndex];
			int vertexC = anIntArray1633[faceIndex];
			int abX = anIntArray1627[vertexB] - anIntArray1627[vertexA];
			int abY = anIntArray1628[vertexB] - anIntArray1628[vertexA];
			int abZ = anIntArray1629[vertexB] - anIntArray1629[vertexA];
			int acX = anIntArray1627[vertexC] - anIntArray1627[vertexA];
			int acY = anIntArray1628[vertexC] - anIntArray1628[vertexA];
			int acZ = anIntArray1629[vertexC] - anIntArray1629[vertexA];
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
			if (anIntArray1637 == null || (anIntArray1637[faceIndex] & 1) == 0) {
				VertexNormal vertexNormal = super.aClass33Array1425[vertexA];
				vertexNormal.normalX += normalX;
				vertexNormal.normalY += normalY;
				vertexNormal.normalZ += normalZ;
				vertexNormal.faceCount++;
				vertexNormal = super.aClass33Array1425[vertexB];
				vertexNormal.normalX += normalX;
				vertexNormal.normalY += normalY;
				vertexNormal.normalZ += normalZ;
				vertexNormal.faceCount++;
				vertexNormal = super.aClass33Array1425[vertexC];
				vertexNormal.normalX += normalX;
				vertexNormal.normalY += normalY;
				vertexNormal.normalZ += normalZ;
				vertexNormal.faceCount++;
			} else {
				int flatIntensity = ambientLight + (lightX * normalX + lightY * normalY + lightZ * normalZ) / (lightDivisor + lightDivisor / 2);
				faceShadeA[faceIndex] = calculateFaceShade(anIntArray1640[faceIndex], flatIntensity, anIntArray1637[faceIndex]);
			}
		}

		if (bakeShading) {
			applyVertexLighting(ambientLight, lightDivisor, lightX, lightY, lightZ);
		} else {
			aClass33Array1660 = new VertexNormal[anInt1626];
			for (int normalIndex = 0; normalIndex < anInt1626; normalIndex++) {
				VertexNormal sourceNormal = super.aClass33Array1425[normalIndex];
				VertexNormal copiedNormal = aClass33Array1660[normalIndex] = new VertexNormal();
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
		for (int faceIndex = 0; faceIndex < anInt1630; faceIndex++) {
			int vertexA = anIntArray1631[faceIndex];
			int vertexB = anIntArray1632[faceIndex];
			int vertexC = anIntArray1633[faceIndex];
			if (anIntArray1637 == null) {
				int faceColor = anIntArray1640[faceIndex];
				VertexNormal vertexNormal = super.aClass33Array1425[vertexA];
				int intensity = ambientLight + (lightX * vertexNormal.normalX + lightY * vertexNormal.normalY + lightZ * vertexNormal.normalZ) / (lightDivisor * vertexNormal.faceCount);
				faceShadeA[faceIndex] = calculateFaceShade(faceColor, intensity, 0);
				vertexNormal = super.aClass33Array1425[vertexB];
				intensity = ambientLight + (lightX * vertexNormal.normalX + lightY * vertexNormal.normalY + lightZ * vertexNormal.normalZ) / (lightDivisor * vertexNormal.faceCount);
				faceShadeB[faceIndex] = calculateFaceShade(faceColor, intensity, 0);
				vertexNormal = super.aClass33Array1425[vertexC];
				intensity = ambientLight + (lightX * vertexNormal.normalX + lightY * vertexNormal.normalY + lightZ * vertexNormal.normalZ) / (lightDivisor * vertexNormal.faceCount);
				faceShadeC[faceIndex] = calculateFaceShade(faceColor, intensity, 0);
			} else if ((anIntArray1637[faceIndex] & 1) == 0) {
				int faceColor = anIntArray1640[faceIndex];
				int faceFlags = anIntArray1637[faceIndex];
				VertexNormal vertexNormal = super.aClass33Array1425[vertexA];
				int intensity = ambientLight + (lightX * vertexNormal.normalX + lightY * vertexNormal.normalY + lightZ * vertexNormal.normalZ) / (lightDivisor * vertexNormal.faceCount);
				faceShadeA[faceIndex] = calculateFaceShade(faceColor, intensity, faceFlags);
				vertexNormal = super.aClass33Array1425[vertexB];
				intensity = ambientLight + (lightX * vertexNormal.normalX + lightY * vertexNormal.normalY + lightZ * vertexNormal.normalZ) / (lightDivisor * vertexNormal.faceCount);
				faceShadeB[faceIndex] = calculateFaceShade(faceColor, intensity, faceFlags);
				vertexNormal = super.aClass33Array1425[vertexC];
				intensity = ambientLight + (lightX * vertexNormal.normalX + lightY * vertexNormal.normalY + lightZ * vertexNormal.normalZ) / (lightDivisor * vertexNormal.faceCount);
				faceShadeC[faceIndex] = calculateFaceShade(faceColor, intensity, faceFlags);
			}
		}

		super.aClass33Array1425 = null;
		aClass33Array1660 = null;
		vertexGroupIds = null;
		faceGroupIds = null;
		if (anIntArray1637 != null) {
			for (int checkIndex = 0; checkIndex < anInt1630; checkIndex++) {
				if ((anIntArray1637[checkIndex] & 2) == 2) {
					return;
				}
			}

		}
		anIntArray1640 = null;
	}

	private static int calculateFaceShade(int colorValue, int lightIntensity, int faceRenderFlags) {
		if ((faceRenderFlags & 2) == 2) {
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
		for (int vertexIndex = 0; vertexIndex < anInt1626; vertexIndex++) {
			int x = anIntArray1627[vertexIndex];
			int y = anIntArray1628[vertexIndex];
			int z = anIntArray1629[vertexIndex];
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
				anIntArray1668[vertexIndex] = x;
				anIntArray1669[vertexIndex] = y;
				anIntArray1670[vertexIndex] = z;
			}
		}

		try {
			drawFacesByDepthAndPriority(false, false, 0);
		} catch (Exception _ex) {
		}
	}

	@Override
	public void method443(int i, int j, int k, int l, int i1, int j1, int k1, int l1, int i2) {
		int j2 = l1 * i1 - j1 * l >> 16;
		int k2 = k1 * j + j2 * k >> 16;
		int l2 = radiusXZ * k >> 16;
		int i3 = k2 + l2;
		// Check distance of model to camera for rendering (default 3500)
		int distance = WorldController.drawDistance * 256;
		distance *= 1 + (Game.zoom / 10);
		if (i3 <= 50 || k2 >= distance) {
			return;
		}
		int j3 = l1 * l + j1 * i1 >> 16;
		int k3 = j3 - radiusXZ << 9;
		if (k3 / i3 >= DrawingArea.clipRightHalf) {
			return;
		}
		int l3 = j3 + radiusXZ << 9;
		if (l3 / i3 <= -DrawingArea.clipRightHalf) {
			return;
		}
		int i4 = k1 * k - j2 * j >> 16;
		int j4 = radiusXZ * j >> 16;
		int k4 = i4 + j4 << 9;
		if (k4 / i3 <= -DrawingArea.clipBottomHalf) {
			return;
		}
		int l4 = j4 + (super.modelHeight * k >> 16);
		int i5 = i4 - l4 << 9;
		if (i5 / i3 >= DrawingArea.clipBottomHalf) {
			return;
		}
		int j5 = l2 + (super.modelHeight * j >> 16);
		boolean flag = false;
		if (k2 - j5 <= 50) {
			flag = true;
		}
		boolean flag1 = false;
		if (i2 > 0 && aBoolean1684) {
			int k5 = k2 - l2;
			if (k5 <= 50) {
				k5 = 50;
			}
			if (j3 > 0) {
				k3 /= i3;
				l3 /= k5;
			} else {
				l3 /= i3;
				k3 /= k5;
			}
			if (i4 > 0) {
				i5 /= i3;
				k4 /= k5;
			} else {
				k4 /= i3;
				i5 /= k5;
			}
			int i6 = anInt1685 - Texture.textureInt1;
			int k6 = anInt1686 - Texture.textureInt2;
			if (i6 > k3 && i6 < l3 && k6 > i5 && k6 < k4) {
				if (aBoolean1659) {
					anIntArray1688[anInt1687++] = i2;
				} else {
					flag1 = true;
				}
			}
		}
		int l5 = Texture.textureInt1;
		int j6 = Texture.textureInt2;
		int l6 = 0;
		int i7 = 0;
		if (i != 0) {
			l6 = sineTable16[i];
			i7 = cosineTable16[i];
		}
		for (int j7 = 0; j7 < anInt1626; j7++) {
			int k7 = anIntArray1627[j7];
			int l7 = anIntArray1628[j7];
			int i8 = anIntArray1629[j7];
			if (i != 0) {
				int j8 = i8 * l6 + k7 * i7 >> 16;
				i8 = i8 * i7 - k7 * l6 >> 16;
				k7 = j8;
			}
			k7 += j1;
			l7 += k1;
			i8 += l1;
			int k8 = i8 * l + k7 * i1 >> 16;
			i8 = i8 * i1 - k7 * l >> 16;
			k7 = k8;
			k8 = l7 * k - i8 * j >> 16;
			i8 = l7 * j + i8 * k >> 16;
			l7 = k8;
			vertexDepth[j7] = i8 - k2;
			if (i8 >= 50) {
				screenX[j7] = l5 + (k7 << 9) / i8;
				screenY[j7] = j6 + (l7 << 9) / i8;
			} else {
				screenX[j7] = -5000;
				flag = true;
			}
			if (flag || texturedFaceCount > 0) {
				anIntArray1668[j7] = k7;
				anIntArray1669[j7] = l7;
				anIntArray1670[j7] = i8;
			}
		}

		try {
			drawFacesByDepthAndPriority(flag, flag1, i2);
		} catch (Exception _ex) {
		}
	}

	private void drawFacesByDepthAndPriority(boolean nearPlaneClipping, boolean pickEnabled, int pickId) {
		for (int bucketIndex = 0; bucketIndex < depthBucketCount; bucketIndex++) {
			depthBucketFaceCounts[bucketIndex] = 0;
		}

		for (int faceIndex = 0; faceIndex < anInt1630; faceIndex++) {
			if (anIntArray1637 == null || anIntArray1637[faceIndex] != -1) {
				int vertexA = anIntArray1631[faceIndex];
				int vertexB = anIntArray1632[faceIndex];
				int vertexC = anIntArray1633[faceIndex];
				int screenXA = screenX[vertexA];
				int screenXB = screenX[vertexB];
				int screenXC = screenX[vertexC];
				if (nearPlaneClipping && (screenXA == -5000 || screenXB == -5000 || screenXC == -5000)) {
					faceNearPlaneClipped[faceIndex] = true;
					int depthBucket = (vertexDepth[vertexA] + vertexDepth[vertexB] + vertexDepth[vertexC]) / 3 + depthBucketOffset;
					depthBucketFaces[depthBucket][depthBucketFaceCounts[depthBucket]++] = faceIndex;
				} else {
					if (pickEnabled && isPointWithinFaceBounds(anInt1685, anInt1686, screenY[vertexA], screenY[vertexB], screenY[vertexC], screenXA, screenXB, screenXC)) {
						anIntArray1688[anInt1687++] = pickId;
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
		int vertexA = anIntArray1631[faceIndex];
		int vertexB = anIntArray1632[faceIndex];
		int vertexC = anIntArray1633[faceIndex];
		Texture.clipScanlines = faceNeedsHorizontalClip[faceIndex];
		if (faceAlphas == null) {
			Texture.transparency = 0;
		} else {
			Texture.transparency = faceAlphas[faceIndex];
		}
		int renderType;
		if (anIntArray1637 == null) {
			renderType = 0;
		} else {
			renderType = anIntArray1637[faceIndex] & 3;
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
			int textureFaceIndex = anIntArray1637[faceIndex] >> 2;
			int mappingVertexA = texturedFaceVertexA[textureFaceIndex];
			int mappingVertexB = texturedFaceVertexB[textureFaceIndex];
			int mappingVertexC = texturedFaceVertexC[textureFaceIndex];
			Texture.drawTexturedTriangle(screenY[vertexA], screenY[vertexB], screenY[vertexC], screenX[vertexA], screenX[vertexB], screenX[vertexC], faceShadeA[faceIndex], faceShadeB[faceIndex], faceShadeC[faceIndex], anIntArray1668[mappingVertexA], anIntArray1668[mappingVertexB], anIntArray1668[mappingVertexC], anIntArray1669[mappingVertexA], anIntArray1669[mappingVertexB], anIntArray1669[mappingVertexC], anIntArray1670[mappingVertexA], anIntArray1670[mappingVertexB], anIntArray1670[mappingVertexC], anIntArray1640[faceIndex]);
			return;
		}
		if (renderType == 3) {
			int textureFaceIndex = anIntArray1637[faceIndex] >> 2;
			int mappingVertexA = texturedFaceVertexA[textureFaceIndex];
			int mappingVertexB = texturedFaceVertexB[textureFaceIndex];
			int mappingVertexC = texturedFaceVertexC[textureFaceIndex];
			Texture.drawTexturedTriangle(screenY[vertexA], screenY[vertexB], screenY[vertexC], screenX[vertexA], screenX[vertexB], screenX[vertexC], faceShadeA[faceIndex], faceShadeA[faceIndex], faceShadeA[faceIndex], anIntArray1668[mappingVertexA], anIntArray1668[mappingVertexB], anIntArray1668[mappingVertexC], anIntArray1669[mappingVertexA], anIntArray1669[mappingVertexB], anIntArray1669[mappingVertexC], anIntArray1670[mappingVertexA], anIntArray1670[mappingVertexB], anIntArray1670[mappingVertexC], anIntArray1640[faceIndex]);
		}
	}

	private void drawFaceClippedToNearPlane(int faceIndex) {
		int screenCenterX = Texture.textureInt1;
		int screenCenterY = Texture.textureInt2;
		int clippedPointCount = 0;
		int vertexA = anIntArray1631[faceIndex];
		int vertexB = anIntArray1632[faceIndex];
		int vertexC = anIntArray1633[faceIndex];
		int depthA = anIntArray1670[vertexA];
		int depthB = anIntArray1670[vertexB];
		int depthC = anIntArray1670[vertexC];
		if (depthA >= 50) {
			clippedX[clippedPointCount] = screenX[vertexA];
			clippedY[clippedPointCount] = screenY[vertexA];
			clippedShade[clippedPointCount++] = faceShadeA[faceIndex];
		} else {
			int cameraXA = anIntArray1668[vertexA];
			int cameraYA = anIntArray1669[vertexA];
			int shadeA = faceShadeA[faceIndex];
			if (depthC >= 50) {
				int fractionAToC = (50 - depthA) * reciprocalTable16[depthC - depthA];
				clippedX[clippedPointCount] = screenCenterX + (cameraXA + ((anIntArray1668[vertexC] - cameraXA) * fractionAToC >> 16) << 9) / 50;
				clippedY[clippedPointCount] = screenCenterY + (cameraYA + ((anIntArray1669[vertexC] - cameraYA) * fractionAToC >> 16) << 9) / 50;
				clippedShade[clippedPointCount++] = shadeA + ((faceShadeC[faceIndex] - shadeA) * fractionAToC >> 16);
			}
			if (depthB >= 50) {
				int fractionAToB = (50 - depthA) * reciprocalTable16[depthB - depthA];
				clippedX[clippedPointCount] = screenCenterX + (cameraXA + ((anIntArray1668[vertexB] - cameraXA) * fractionAToB >> 16) << 9) / 50;
				clippedY[clippedPointCount] = screenCenterY + (cameraYA + ((anIntArray1669[vertexB] - cameraYA) * fractionAToB >> 16) << 9) / 50;
				clippedShade[clippedPointCount++] = shadeA + ((faceShadeB[faceIndex] - shadeA) * fractionAToB >> 16);
			}
		}
		if (depthB >= 50) {
			clippedX[clippedPointCount] = screenX[vertexB];
			clippedY[clippedPointCount] = screenY[vertexB];
			clippedShade[clippedPointCount++] = faceShadeB[faceIndex];
		} else {
			int cameraXB = anIntArray1668[vertexB];
			int cameraYB = anIntArray1669[vertexB];
			int shadeB = faceShadeB[faceIndex];
			if (depthA >= 50) {
				int fractionBToA = (50 - depthB) * reciprocalTable16[depthA - depthB];
				clippedX[clippedPointCount] = screenCenterX + (cameraXB + ((anIntArray1668[vertexA] - cameraXB) * fractionBToA >> 16) << 9) / 50;
				clippedY[clippedPointCount] = screenCenterY + (cameraYB + ((anIntArray1669[vertexA] - cameraYB) * fractionBToA >> 16) << 9) / 50;
				clippedShade[clippedPointCount++] = shadeB + ((faceShadeA[faceIndex] - shadeB) * fractionBToA >> 16);
			}
			if (depthC >= 50) {
				int fractionBToC = (50 - depthB) * reciprocalTable16[depthC - depthB];
				clippedX[clippedPointCount] = screenCenterX + (cameraXB + ((anIntArray1668[vertexC] - cameraXB) * fractionBToC >> 16) << 9) / 50;
				clippedY[clippedPointCount] = screenCenterY + (cameraYB + ((anIntArray1669[vertexC] - cameraYB) * fractionBToC >> 16) << 9) / 50;
				clippedShade[clippedPointCount++] = shadeB + ((faceShadeC[faceIndex] - shadeB) * fractionBToC >> 16);
			}
		}
		if (depthC >= 50) {
			clippedX[clippedPointCount] = screenX[vertexC];
			clippedY[clippedPointCount] = screenY[vertexC];
			clippedShade[clippedPointCount++] = faceShadeC[faceIndex];
		} else {
			int cameraXC = anIntArray1668[vertexC];
			int cameraYC = anIntArray1669[vertexC];
			int shadeC = faceShadeC[faceIndex];
			if (depthB >= 50) {
				int fractionCToB = (50 - depthC) * reciprocalTable16[depthB - depthC];
				clippedX[clippedPointCount] = screenCenterX + (cameraXC + ((anIntArray1668[vertexB] - cameraXC) * fractionCToB >> 16) << 9) / 50;
				clippedY[clippedPointCount] = screenCenterY + (cameraYC + ((anIntArray1669[vertexB] - cameraYC) * fractionCToB >> 16) << 9) / 50;
				clippedShade[clippedPointCount++] = shadeC + ((faceShadeB[faceIndex] - shadeC) * fractionCToB >> 16);
			}
			if (depthA >= 50) {
				int fractionCToA = (50 - depthC) * reciprocalTable16[depthA - depthC];
				clippedX[clippedPointCount] = screenCenterX + (cameraXC + ((anIntArray1668[vertexA] - cameraXC) * fractionCToA >> 16) << 9) / 50;
				clippedY[clippedPointCount] = screenCenterY + (cameraYC + ((anIntArray1669[vertexA] - cameraYC) * fractionCToA >> 16) << 9) / 50;
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
				if (anIntArray1637 == null) {
					renderType = 0;
				} else {
					renderType = anIntArray1637[faceIndex] & 3;
				}
				if (renderType == 0) {
					Texture.drawShadedTriangle(screenY0, screenY1, screenY2, screenX0, screenX1, screenX2, clippedShade[0], clippedShade[1], clippedShade[2]);
				} else if (renderType == 1) {
					Texture.drawFlatTriangle(screenY0, screenY1, screenY2, screenX0, screenX1, screenX2, hslToRgbPalette[faceShadeA[faceIndex]]);
				} else if (renderType == 2) {
					int textureFaceIndex = anIntArray1637[faceIndex] >> 2;
					int mappingVertexA = texturedFaceVertexA[textureFaceIndex];
					int mappingVertexB = texturedFaceVertexB[textureFaceIndex];
					int mappingVertexC = texturedFaceVertexC[textureFaceIndex];
					Texture.drawTexturedTriangle(screenY0, screenY1, screenY2, screenX0, screenX1, screenX2, clippedShade[0], clippedShade[1], clippedShade[2], anIntArray1668[mappingVertexA], anIntArray1668[mappingVertexB], anIntArray1668[mappingVertexC], anIntArray1669[mappingVertexA], anIntArray1669[mappingVertexB], anIntArray1669[mappingVertexC], anIntArray1670[mappingVertexA], anIntArray1670[mappingVertexB], anIntArray1670[mappingVertexC], anIntArray1640[faceIndex]);
				} else if (renderType == 3) {
					int textureFaceIndex = anIntArray1637[faceIndex] >> 2;
					int mappingVertexA = texturedFaceVertexA[textureFaceIndex];
					int mappingVertexB = texturedFaceVertexB[textureFaceIndex];
					int mappingVertexC = texturedFaceVertexC[textureFaceIndex];
					Texture.drawTexturedTriangle(screenY0, screenY1, screenY2, screenX0, screenX1, screenX2, faceShadeA[faceIndex], faceShadeA[faceIndex], faceShadeA[faceIndex], anIntArray1668[mappingVertexA], anIntArray1668[mappingVertexB], anIntArray1668[mappingVertexC], anIntArray1669[mappingVertexA], anIntArray1669[mappingVertexB], anIntArray1669[mappingVertexC], anIntArray1670[mappingVertexA], anIntArray1670[mappingVertexB], anIntArray1670[mappingVertexC], anIntArray1640[faceIndex]);
				}
			}
			if (clippedPointCount == 4) {
				if (screenX0 < 0 || screenX1 < 0 || screenX2 < 0 || screenX0 > DrawingArea.scanlineRightLimit || screenX1 > DrawingArea.scanlineRightLimit || screenX2 > DrawingArea.scanlineRightLimit || clippedX[3] < 0 || clippedX[3] > DrawingArea.scanlineRightLimit) {
					Texture.clipScanlines = true;
				}
				int renderType;
				if (anIntArray1637 == null) {
					renderType = 0;
				} else {
					renderType = anIntArray1637[faceIndex] & 3;
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
					int textureFaceIndex = anIntArray1637[faceIndex] >> 2;
					int mappingVertexA = texturedFaceVertexA[textureFaceIndex];
					int mappingVertexB = texturedFaceVertexB[textureFaceIndex];
					int mappingVertexC = texturedFaceVertexC[textureFaceIndex];
					Texture.drawTexturedTriangle(screenY0, screenY1, screenY2, screenX0, screenX1, screenX2, clippedShade[0], clippedShade[1], clippedShade[2], anIntArray1668[mappingVertexA], anIntArray1668[mappingVertexB], anIntArray1668[mappingVertexC], anIntArray1669[mappingVertexA], anIntArray1669[mappingVertexB], anIntArray1669[mappingVertexC], anIntArray1670[mappingVertexA], anIntArray1670[mappingVertexB], anIntArray1670[mappingVertexC], anIntArray1640[faceIndex]);
					Texture.drawTexturedTriangle(screenY0, screenY2, clippedY[3], screenX0, screenX2, clippedX[3], clippedShade[0], clippedShade[2], clippedShade[3], anIntArray1668[mappingVertexA], anIntArray1668[mappingVertexB], anIntArray1668[mappingVertexC], anIntArray1669[mappingVertexA], anIntArray1669[mappingVertexB], anIntArray1669[mappingVertexC], anIntArray1670[mappingVertexA], anIntArray1670[mappingVertexB], anIntArray1670[mappingVertexC], anIntArray1640[faceIndex]);
					return;
				}
				if (renderType == 3) {
					int textureFaceIndex = anIntArray1637[faceIndex] >> 2;
					int mappingVertexA = texturedFaceVertexA[textureFaceIndex];
					int mappingVertexB = texturedFaceVertexB[textureFaceIndex];
					int mappingVertexC = texturedFaceVertexC[textureFaceIndex];
					Texture.drawTexturedTriangle(screenY0, screenY1, screenY2, screenX0, screenX1, screenX2, faceShadeA[faceIndex], faceShadeA[faceIndex], faceShadeA[faceIndex], anIntArray1668[mappingVertexA], anIntArray1668[mappingVertexB], anIntArray1668[mappingVertexC], anIntArray1669[mappingVertexA], anIntArray1669[mappingVertexB], anIntArray1669[mappingVertexC], anIntArray1670[mappingVertexA], anIntArray1670[mappingVertexB], anIntArray1670[mappingVertexC], anIntArray1640[faceIndex]);
					Texture.drawTexturedTriangle(screenY0, screenY2, clippedY[3], screenX0, screenX2, clippedX[3], faceShadeA[faceIndex], faceShadeA[faceIndex], faceShadeA[faceIndex], anIntArray1668[mappingVertexA], anIntArray1668[mappingVertexB], anIntArray1668[mappingVertexC], anIntArray1669[mappingVertexA], anIntArray1669[mappingVertexB], anIntArray1669[mappingVertexC], anIntArray1670[mappingVertexA], anIntArray1670[mappingVertexB], anIntArray1670[mappingVertexC], anIntArray1640[faceIndex]);
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
	public int anInt1626;
	public int anIntArray1627[];
	public int anIntArray1628[];
	public int anIntArray1629[];
	public int anInt1630;
	public int anIntArray1631[];
	public int anIntArray1632[];
	public int anIntArray1633[];
	private int[] faceShadeA;
	private int[] faceShadeB;
	private int[] faceShadeC;
	public int anIntArray1637[];
	private int[] facePriorities;
	private int[] faceAlphas;
	public int anIntArray1640[];
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
	public int anInt1654;
	private int[] vertexGroupIds;
	private int[] faceGroupIds;
	public int vertexGroups[][];
	public int faceGroups[][];
	public boolean aBoolean1659;
	VertexNormal aClass33Array1660[];
	private static ModelHeader[] modelHeaders;
	private static OnDemandFetcherParent modelRequester;
	private static boolean[] faceNeedsHorizontalClip = new boolean[4096];
	private static boolean[] faceNearPlaneClipped = new boolean[4096];
	private static int[] screenX = new int[4096];
	private static int[] screenY = new int[4096];
	private static int[] vertexDepth = new int[4096];
	private static int[] anIntArray1668 = new int[4096];
	private static int[] anIntArray1669 = new int[4096];
	private static int[] anIntArray1670 = new int[4096];
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
	public static boolean aBoolean1684;
	public static int anInt1685;
	public static int anInt1686;
	public static int anInt1687;
	public static final int[] anIntArray1688 = new int[1000];
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
