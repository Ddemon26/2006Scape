// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

public final class Model extends Animable {

	public static void nullLoader() {
		modelHeaders = null;
		aBooleanArray1663 = null;
		aBooleanArray1664 = null;
		anIntArray1665 = null;
		anIntArray1666 = null;
		anIntArray1667 = null;
		anIntArray1668 = null;
		anIntArray1669 = null;
		anIntArray1670 = null;
		anIntArray1671 = null;
		anIntArrayArray1672 = null;
		anIntArray1673 = null;
		anIntArrayArray1674 = null;
		anIntArray1675 = null;
		anIntArray1676 = null;
		anIntArray1677 = null;
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
		anInt1642 = header.texturedFaceCount;
		anIntArray1627 = new int[anInt1626];
		anIntArray1628 = new int[anInt1626];
		anIntArray1629 = new int[anInt1626];
		anIntArray1631 = new int[anInt1630];
		anIntArray1632 = new int[anInt1630];
		anIntArray1633 = new int[anInt1630];
		anIntArray1643 = new int[anInt1642];
		anIntArray1644 = new int[anInt1642];
		anIntArray1645 = new int[anInt1642];
		if (header.vertexGroupIdsOffset >= 0) {
			vertexGroupIds = new int[anInt1626];
		}
		if (header.faceRenderFlagsOffset >= 0) {
			anIntArray1637 = new int[anInt1630];
		}
		if (header.facePrioritiesOffset >= 0) {
			anIntArray1638 = new int[anInt1630];
		} else {
			anInt1641 = -header.facePrioritiesOffset - 1;
		}
		if (header.faceAlphasOffset >= 0) {
			anIntArray1639 = new int[anInt1630];
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
		int k = 0;
		int l = 0;
		int i1 = 0;
		for (int j1 = 0; j1 < anInt1626; j1++) {
			int k1 = stream.readUnsignedByte();
			int i2 = 0;
			if ((k1 & 1) != 0) {
				i2 = stream_1.readSignedSmart();
			}
			int k2 = 0;
			if ((k1 & 2) != 0) {
				k2 = stream_2.readSignedSmart();
			}
			int i3 = 0;
			if ((k1 & 4) != 0) {
				i3 = stream_3.readSignedSmart();
			}
			anIntArray1627[j1] = k + i2;
			anIntArray1628[j1] = l + k2;
			anIntArray1629[j1] = i1 + i3;
			k = anIntArray1627[j1];
			l = anIntArray1628[j1];
			i1 = anIntArray1629[j1];
			if (vertexGroupIds != null) {
				vertexGroupIds[j1] = stream_4.readUnsignedByte();
			}
		}

		stream.currentOffset = header.faceColoursOffset;
		stream_1.currentOffset = header.faceRenderFlagsOffset;
		stream_2.currentOffset = header.facePrioritiesOffset;
		stream_3.currentOffset = header.faceAlphasOffset;
		stream_4.currentOffset = header.faceGroupIdsOffset;
		for (int l1 = 0; l1 < anInt1630; l1++) {
			anIntArray1640[l1] = stream.readUnsignedShortBigEndian();
			if (anIntArray1637 != null) {
				anIntArray1637[l1] = stream_1.readUnsignedByte();
			}
			if (anIntArray1638 != null) {
				anIntArray1638[l1] = stream_2.readUnsignedByte();
			}
			if (anIntArray1639 != null) {
				anIntArray1639[l1] = stream_3.readUnsignedByte();
			}
			if (faceGroupIds != null) {
				faceGroupIds[l1] = stream_4.readUnsignedByte();
			}
		}

		stream.currentOffset = header.faceVertexDataOffset;
		stream_1.currentOffset = header.faceTypesOffset;
		int j2 = 0;
		int l2 = 0;
		int j3 = 0;
		int k3 = 0;
		for (int l3 = 0; l3 < anInt1630; l3++) {
			int i4 = stream_1.readUnsignedByte();
			if (i4 == 1) {
				j2 = stream.readSignedSmart() + k3;
				k3 = j2;
				l2 = stream.readSignedSmart() + k3;
				k3 = l2;
				j3 = stream.readSignedSmart() + k3;
				k3 = j3;
				anIntArray1631[l3] = j2;
				anIntArray1632[l3] = l2;
				anIntArray1633[l3] = j3;
			}
			if (i4 == 2) {
				l2 = j3;
				j3 = stream.readSignedSmart() + k3;
				k3 = j3;
				anIntArray1631[l3] = j2;
				anIntArray1632[l3] = l2;
				anIntArray1633[l3] = j3;
			}
			if (i4 == 3) {
				j2 = j3;
				j3 = stream.readSignedSmart() + k3;
				k3 = j3;
				anIntArray1631[l3] = j2;
				anIntArray1632[l3] = l2;
				anIntArray1633[l3] = j3;
			}
			if (i4 == 4) {
				int k4 = j2;
				j2 = l2;
				l2 = k4;
				j3 = stream.readSignedSmart() + k3;
				k3 = j3;
				anIntArray1631[l3] = j2;
				anIntArray1632[l3] = l2;
				anIntArray1633[l3] = j3;
			}
		}

		stream.currentOffset = header.texturedFaceOffset;
		for (int j4 = 0; j4 < anInt1642; j4++) {
			anIntArray1643[j4] = stream.readUnsignedShortBigEndian();
			anIntArray1644[j4] = stream.readUnsignedShortBigEndian();
			anIntArray1645[j4] = stream.readUnsignedShortBigEndian();
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
		anInt1642 = 0;
		anInt1641 = -1;
		for (int partIndex = 0; partIndex < partCount; partIndex++) {
			Model sourcePart = parts[partIndex];
			if (sourcePart != null) {
				anInt1626 += sourcePart.anInt1626;
				anInt1630 += sourcePart.anInt1630;
				anInt1642 += sourcePart.anInt1642;
				needsFaceRenderFlags |= sourcePart.anIntArray1637 != null;
				if (sourcePart.anIntArray1638 != null) {
					needsFacePriorities = true;
				} else {
					if (anInt1641 == -1) {
						anInt1641 = sourcePart.anInt1641;
					}
					if (anInt1641 != sourcePart.anInt1641) {
						needsFacePriorities = true;
					}
				}
				needsFaceAlpha |= sourcePart.anIntArray1639 != null;
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
		anIntArray1643 = new int[anInt1642];
		anIntArray1644 = new int[anInt1642];
		anIntArray1645 = new int[anInt1642];
		if (needsFaceRenderFlags) {
			anIntArray1637 = new int[anInt1630];
		}
		if (needsFacePriorities) {
			anIntArray1638 = new int[anInt1630];
		}
		if (needsFaceAlpha) {
			anIntArray1639 = new int[anInt1630];
		}
		if (needsFaceGroupIds) {
			faceGroupIds = new int[anInt1630];
		}
		anIntArray1640 = new int[anInt1630];
		anInt1626 = 0;
		anInt1630 = 0;
		anInt1642 = 0;
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
						if (sourcePart.anIntArray1638 == null) {
							anIntArray1638[anInt1630] = sourcePart.anInt1641;
						} else {
							anIntArray1638[anInt1630] = sourcePart.anIntArray1638[faceIndex];
						}
					}
					if (needsFaceAlpha) {
						if (sourcePart.anIntArray1639 == null) {
							anIntArray1639[anInt1630] = 0;
						} else {
							anIntArray1639[anInt1630] = sourcePart.anIntArray1639[faceIndex];
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

				for (int textureTriangleIndex = 0; textureTriangleIndex < sourcePart.anInt1642; textureTriangleIndex++) {
					anIntArray1643[anInt1642] = getOrAddVertex(sourcePart, sourcePart.anIntArray1643[textureTriangleIndex]);
					anIntArray1644[anInt1642] = getOrAddVertex(sourcePart, sourcePart.anIntArray1644[textureTriangleIndex]);
					anIntArray1645[anInt1642] = getOrAddVertex(sourcePart, sourcePart.anIntArray1645[textureTriangleIndex]);
					anInt1642++;
				}

				textureTriangleOffset += sourcePart.anInt1642;
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
		anInt1642 = 0;
		anInt1641 = -1;
		for (int partIndex = 0; partIndex < partCount; partIndex++) {
			Model sourcePart = parts[partIndex];
			if (sourcePart != null) {
				anInt1626 += sourcePart.anInt1626;
				anInt1630 += sourcePart.anInt1630;
				anInt1642 += sourcePart.anInt1642;
				needsFaceRenderFlags |= sourcePart.anIntArray1637 != null;
				if (sourcePart.anIntArray1638 != null) {
					needsFacePriorities = true;
				} else {
					if (anInt1641 == -1) {
						anInt1641 = sourcePart.anInt1641;
					}
					if (anInt1641 != sourcePart.anInt1641) {
						needsFacePriorities = true;
					}
				}
				needsFaceAlpha |= sourcePart.anIntArray1639 != null;
				needsFaceValues |= sourcePart.anIntArray1640 != null;
			}
		}

		anIntArray1627 = new int[anInt1626];
		anIntArray1628 = new int[anInt1626];
		anIntArray1629 = new int[anInt1626];
		anIntArray1631 = new int[anInt1630];
		anIntArray1632 = new int[anInt1630];
		anIntArray1633 = new int[anInt1630];
		anIntArray1634 = new int[anInt1630];
		anIntArray1635 = new int[anInt1630];
		anIntArray1636 = new int[anInt1630];
		anIntArray1643 = new int[anInt1642];
		anIntArray1644 = new int[anInt1642];
		anIntArray1645 = new int[anInt1642];
		if (needsFaceRenderFlags) {
			anIntArray1637 = new int[anInt1630];
		}
		if (needsFacePriorities) {
			anIntArray1638 = new int[anInt1630];
		}
		if (needsFaceAlpha) {
			anIntArray1639 = new int[anInt1630];
		}
		if (needsFaceValues) {
			anIntArray1640 = new int[anInt1630];
		}
		anInt1626 = 0;
		anInt1630 = 0;
		anInt1642 = 0;
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
					anIntArray1634[anInt1630] = sourcePart.anIntArray1634[faceIndex];
					anIntArray1635[anInt1630] = sourcePart.anIntArray1635[faceIndex];
					anIntArray1636[anInt1630] = sourcePart.anIntArray1636[faceIndex];
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
						if (sourcePart.anIntArray1638 == null) {
							anIntArray1638[anInt1630] = sourcePart.anInt1641;
						} else {
							anIntArray1638[anInt1630] = sourcePart.anIntArray1638[faceIndex];
						}
					}
					if (needsFaceAlpha) {
						if (sourcePart.anIntArray1639 == null) {
							anIntArray1639[anInt1630] = 0;
						} else {
							anIntArray1639[anInt1630] = sourcePart.anIntArray1639[faceIndex];
						}
					}
					if (needsFaceValues && sourcePart.anIntArray1640 != null) {
						anIntArray1640[anInt1630] = sourcePart.anIntArray1640[faceIndex];
					}
					anInt1630++;
				}

				for (int textureTriangleIndex = 0; textureTriangleIndex < sourcePart.anInt1642; textureTriangleIndex++) {
					anIntArray1643[anInt1642] = sourcePart.anIntArray1643[textureTriangleIndex] + vertexOffset;
					anIntArray1644[anInt1642] = sourcePart.anIntArray1644[textureTriangleIndex] + vertexOffset;
					anIntArray1645[anInt1642] = sourcePart.anIntArray1645[textureTriangleIndex] + vertexOffset;
					anInt1642++;
				}

				textureTriangleOffset += sourcePart.anInt1642;
			}
		}

		calculateBounds();
	}

	public Model(boolean shareFaceValues, boolean shareFaceAlpha, boolean shareVertices, Model sourceModel) {
		aBoolean1659 = false;
		anInt1626 = sourceModel.anInt1626;
		anInt1630 = sourceModel.anInt1630;
		anInt1642 = sourceModel.anInt1642;
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
			anIntArray1639 = sourceModel.anIntArray1639;
		} else {
			anIntArray1639 = new int[anInt1630];
			if (sourceModel.anIntArray1639 == null) {
				for (int faceIndex = 0; faceIndex < anInt1630; faceIndex++) {
					anIntArray1639[faceIndex] = 0;
				}

			} else {
				System.arraycopy(sourceModel.anIntArray1639, 0, anIntArray1639, 0, anInt1630);

			}
		}
		vertexGroupIds = sourceModel.vertexGroupIds;
		faceGroupIds = sourceModel.faceGroupIds;
		anIntArray1637 = sourceModel.anIntArray1637;
		anIntArray1631 = sourceModel.anIntArray1631;
		anIntArray1632 = sourceModel.anIntArray1632;
		anIntArray1633 = sourceModel.anIntArray1633;
		anIntArray1638 = sourceModel.anIntArray1638;
		anInt1641 = sourceModel.anInt1641;
		anIntArray1643 = sourceModel.anIntArray1643;
		anIntArray1644 = sourceModel.anIntArray1644;
		anIntArray1645 = sourceModel.anIntArray1645;
	}

	public Model(boolean copyVertexY, boolean copyLightingData, Model sourceModel) {
		aBoolean1659 = false;
		anInt1626 = sourceModel.anInt1626;
		anInt1630 = sourceModel.anInt1630;
		anInt1642 = sourceModel.anInt1642;
		if (copyVertexY) {
			anIntArray1628 = new int[anInt1626];
			System.arraycopy(sourceModel.anIntArray1628, 0, anIntArray1628, 0, anInt1626);

		} else {
			anIntArray1628 = sourceModel.anIntArray1628;
		}
		if (copyLightingData) {
			anIntArray1634 = new int[anInt1630];
			anIntArray1635 = new int[anInt1630];
			anIntArray1636 = new int[anInt1630];
			for (int faceIndex = 0; faceIndex < anInt1630; faceIndex++) {
				anIntArray1634[faceIndex] = sourceModel.anIntArray1634[faceIndex];
				anIntArray1635[faceIndex] = sourceModel.anIntArray1635[faceIndex];
				anIntArray1636[faceIndex] = sourceModel.anIntArray1636[faceIndex];
			}

			anIntArray1637 = new int[anInt1630];
			if (sourceModel.anIntArray1637 == null) {
				for (int faceIndex = 0; faceIndex < anInt1630; faceIndex++) {
					anIntArray1637[faceIndex] = 0;
				}

			} else {
				System.arraycopy(sourceModel.anIntArray1637, 0, anIntArray1637, 0, anInt1630);

			}
			super.aClass33Array1425 = new Class33[anInt1626];
			for (int vertexIndex = 0; vertexIndex < anInt1626; vertexIndex++) {
				Class33 copiedNormal = super.aClass33Array1425[vertexIndex] = new Class33();
				Class33 sourceNormal = sourceModel.aClass33Array1425[vertexIndex];
				copiedNormal.anInt602 = sourceNormal.anInt602;
				copiedNormal.anInt603 = sourceNormal.anInt603;
				copiedNormal.anInt604 = sourceNormal.anInt604;
				copiedNormal.anInt605 = sourceNormal.anInt605;
			}

			aClass33Array1660 = sourceModel.aClass33Array1660;
		} else {
			anIntArray1634 = sourceModel.anIntArray1634;
			anIntArray1635 = sourceModel.anIntArray1635;
			anIntArray1636 = sourceModel.anIntArray1636;
			anIntArray1637 = sourceModel.anIntArray1637;
		}
		anIntArray1627 = sourceModel.anIntArray1627;
		anIntArray1629 = sourceModel.anIntArray1629;
		anIntArray1640 = sourceModel.anIntArray1640;
		anIntArray1639 = sourceModel.anIntArray1639;
		anIntArray1638 = sourceModel.anIntArray1638;
		anInt1641 = sourceModel.anInt1641;
		anIntArray1631 = sourceModel.anIntArray1631;
		anIntArray1632 = sourceModel.anIntArray1632;
		anIntArray1633 = sourceModel.anIntArray1633;
		anIntArray1643 = sourceModel.anIntArray1643;
		anIntArray1644 = sourceModel.anIntArray1644;
		anIntArray1645 = sourceModel.anIntArray1645;
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
		anInt1642 = sourceModel.anInt1642;
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
			anIntArray1639 = sourceModel.anIntArray1639;
		} else {
			if (animationScratchAlpha.length < anInt1630) {
				animationScratchAlpha = new int[anInt1630 + 100];
			}
			anIntArray1639 = animationScratchAlpha;
			if (sourceModel.anIntArray1639 == null) {
				for (int faceIndex = 0; faceIndex < anInt1630; faceIndex++) {
					anIntArray1639[faceIndex] = 0;
				}

			} else {
				System.arraycopy(sourceModel.anIntArray1639, 0, anIntArray1639, 0, anInt1630);

			}
		}
		anIntArray1637 = sourceModel.anIntArray1637;
		anIntArray1640 = sourceModel.anIntArray1640;
		anIntArray1638 = sourceModel.anIntArray1638;
		anInt1641 = sourceModel.anInt1641;
		faceGroups = sourceModel.faceGroups;
		vertexGroups = sourceModel.vertexGroups;
		anIntArray1631 = sourceModel.anIntArray1631;
		anIntArray1632 = sourceModel.anIntArray1632;
		anIntArray1633 = sourceModel.anIntArray1633;
		anIntArray1634 = sourceModel.anIntArray1634;
		anIntArray1635 = sourceModel.anIntArray1635;
		anIntArray1636 = sourceModel.anIntArray1636;
		anIntArray1643 = sourceModel.anIntArray1643;
		anIntArray1644 = sourceModel.anIntArray1644;
		anIntArray1645 = sourceModel.anIntArray1645;
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
		if (transformType == 5 && faceGroups != null && anIntArray1639 != null) {
			for (int groupIndex = 0; groupIndex < groupCount; groupIndex++) {
				int faceGroupId = groupIds[groupIndex];
				if (faceGroupId < faceGroups.length) {
					int faceIndices[] = faceGroups[faceGroupId];
					for (int groupFaceIndex : faceIndices) {
						int faceIndex = groupFaceIndex;
						anIntArray1639[faceIndex] += x * 8;
						if (anIntArray1639[faceIndex] < 0) {
							anIntArray1639[faceIndex] = 0;
						}
						if (anIntArray1639[faceIndex] > 255) {
							anIntArray1639[faceIndex] = 255;
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
		if (anIntArray1634 == null) {
			anIntArray1634 = new int[anInt1630];
			anIntArray1635 = new int[anInt1630];
			anIntArray1636 = new int[anInt1630];
		}
		if (super.aClass33Array1425 == null) {
			super.aClass33Array1425 = new Class33[anInt1626];
			for (int vertexIndex = 0; vertexIndex < anInt1626; vertexIndex++) {
				super.aClass33Array1425[vertexIndex] = new Class33();
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
				Class33 vertexNormal = super.aClass33Array1425[vertexA];
				vertexNormal.anInt602 += normalX;
				vertexNormal.anInt603 += normalY;
				vertexNormal.anInt604 += normalZ;
				vertexNormal.anInt605++;
				vertexNormal = super.aClass33Array1425[vertexB];
				vertexNormal.anInt602 += normalX;
				vertexNormal.anInt603 += normalY;
				vertexNormal.anInt604 += normalZ;
				vertexNormal.anInt605++;
				vertexNormal = super.aClass33Array1425[vertexC];
				vertexNormal.anInt602 += normalX;
				vertexNormal.anInt603 += normalY;
				vertexNormal.anInt604 += normalZ;
				vertexNormal.anInt605++;
			} else {
				int flatIntensity = ambientLight + (lightX * normalX + lightY * normalY + lightZ * normalZ) / (lightDivisor + lightDivisor / 2);
				anIntArray1634[faceIndex] = calculateFaceShade(anIntArray1640[faceIndex], flatIntensity, anIntArray1637[faceIndex]);
			}
		}

		if (bakeShading) {
			applyVertexLighting(ambientLight, lightDivisor, lightX, lightY, lightZ);
		} else {
			aClass33Array1660 = new Class33[anInt1626];
			for (int normalIndex = 0; normalIndex < anInt1626; normalIndex++) {
				Class33 sourceNormal = super.aClass33Array1425[normalIndex];
				Class33 copiedNormal = aClass33Array1660[normalIndex] = new Class33();
				copiedNormal.anInt602 = sourceNormal.anInt602;
				copiedNormal.anInt603 = sourceNormal.anInt603;
				copiedNormal.anInt604 = sourceNormal.anInt604;
				copiedNormal.anInt605 = sourceNormal.anInt605;
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
				Class33 vertexNormal = super.aClass33Array1425[vertexA];
				int intensity = ambientLight + (lightX * vertexNormal.anInt602 + lightY * vertexNormal.anInt603 + lightZ * vertexNormal.anInt604) / (lightDivisor * vertexNormal.anInt605);
				anIntArray1634[faceIndex] = calculateFaceShade(faceColor, intensity, 0);
				vertexNormal = super.aClass33Array1425[vertexB];
				intensity = ambientLight + (lightX * vertexNormal.anInt602 + lightY * vertexNormal.anInt603 + lightZ * vertexNormal.anInt604) / (lightDivisor * vertexNormal.anInt605);
				anIntArray1635[faceIndex] = calculateFaceShade(faceColor, intensity, 0);
				vertexNormal = super.aClass33Array1425[vertexC];
				intensity = ambientLight + (lightX * vertexNormal.anInt602 + lightY * vertexNormal.anInt603 + lightZ * vertexNormal.anInt604) / (lightDivisor * vertexNormal.anInt605);
				anIntArray1636[faceIndex] = calculateFaceShade(faceColor, intensity, 0);
			} else if ((anIntArray1637[faceIndex] & 1) == 0) {
				int faceColor = anIntArray1640[faceIndex];
				int faceFlags = anIntArray1637[faceIndex];
				Class33 vertexNormal = super.aClass33Array1425[vertexA];
				int intensity = ambientLight + (lightX * vertexNormal.anInt602 + lightY * vertexNormal.anInt603 + lightZ * vertexNormal.anInt604) / (lightDivisor * vertexNormal.anInt605);
				anIntArray1634[faceIndex] = calculateFaceShade(faceColor, intensity, faceFlags);
				vertexNormal = super.aClass33Array1425[vertexB];
				intensity = ambientLight + (lightX * vertexNormal.anInt602 + lightY * vertexNormal.anInt603 + lightZ * vertexNormal.anInt604) / (lightDivisor * vertexNormal.anInt605);
				anIntArray1635[faceIndex] = calculateFaceShade(faceColor, intensity, faceFlags);
				vertexNormal = super.aClass33Array1425[vertexC];
				intensity = ambientLight + (lightX * vertexNormal.anInt602 + lightY * vertexNormal.anInt603 + lightZ * vertexNormal.anInt604) / (lightDivisor * vertexNormal.anInt605);
				anIntArray1636[faceIndex] = calculateFaceShade(faceColor, intensity, faceFlags);
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
			anIntArray1667[vertexIndex] = z - originDepth;
			anIntArray1665[vertexIndex] = screenCenterX + (x << 9) / z;
			anIntArray1666[vertexIndex] = screenCenterY + (y << 9) / z;
			if (anInt1642 > 0) {
				anIntArray1668[vertexIndex] = x;
				anIntArray1669[vertexIndex] = y;
				anIntArray1670[vertexIndex] = z;
			}
		}

		try {
			method483(false, false, 0);
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
			anIntArray1667[j7] = i8 - k2;
			if (i8 >= 50) {
				anIntArray1665[j7] = l5 + (k7 << 9) / i8;
				anIntArray1666[j7] = j6 + (l7 << 9) / i8;
			} else {
				anIntArray1665[j7] = -5000;
				flag = true;
			}
			if (flag || anInt1642 > 0) {
				anIntArray1668[j7] = k7;
				anIntArray1669[j7] = l7;
				anIntArray1670[j7] = i8;
			}
		}

		try {
			method483(flag, flag1, i2);
		} catch (Exception _ex) {
		}
	}

	private void method483(boolean flag, boolean flag1, int i) {
		for (int j = 0; j < depthBucketCount; j++) {
			anIntArray1671[j] = 0;
		}

		for (int k = 0; k < anInt1630; k++) {
			if (anIntArray1637 == null || anIntArray1637[k] != -1) {
				int l = anIntArray1631[k];
				int k1 = anIntArray1632[k];
				int j2 = anIntArray1633[k];
				int i3 = anIntArray1665[l];
				int l3 = anIntArray1665[k1];
				int k4 = anIntArray1665[j2];
				if (flag && (i3 == -5000 || l3 == -5000 || k4 == -5000)) {
					aBooleanArray1664[k] = true;
					int j5 = (anIntArray1667[l] + anIntArray1667[k1] + anIntArray1667[j2]) / 3 + depthBucketOffset;
					anIntArrayArray1672[j5][anIntArray1671[j5]++] = k;
				} else {
					if (flag1 && isPointWithinFaceBounds(anInt1685, anInt1686, anIntArray1666[l], anIntArray1666[k1], anIntArray1666[j2], i3, l3, k4)) {
						anIntArray1688[anInt1687++] = i;
						flag1 = false;
					}
					if ((i3 - l3) * (anIntArray1666[j2] - anIntArray1666[k1]) - (anIntArray1666[l] - anIntArray1666[k1]) * (k4 - l3) > 0) {
						aBooleanArray1664[k] = false;
						aBooleanArray1663[k] = i3 < 0 || l3 < 0 || k4 < 0 || i3 > DrawingArea.scanlineRightLimit || l3 > DrawingArea.scanlineRightLimit || k4 > DrawingArea.scanlineRightLimit;
						int k5 = (anIntArray1667[l] + anIntArray1667[k1] + anIntArray1667[j2]) / 3 + depthBucketOffset;
						anIntArrayArray1672[k5][anIntArray1671[k5]++] = k;
					}
				}
			}
		}

		if (anIntArray1638 == null) {
			for (int i1 = depthBucketCount - 1; i1 >= 0; i1--) {
				int l1 = anIntArray1671[i1];
				if (l1 > 0) {
					int ai[] = anIntArrayArray1672[i1];
					for (int j3 = 0; j3 < l1; j3++) {
						drawFace(ai[j3]);
					}

				}
			}

			return;
		}
		for (int j1 = 0; j1 < 12; j1++) {
			anIntArray1673[j1] = 0;
			anIntArray1677[j1] = 0;
		}

		for (int i2 = depthBucketCount - 1; i2 >= 0; i2--) {
			int k2 = anIntArray1671[i2];
			if (k2 > 0) {
				int ai1[] = anIntArrayArray1672[i2];
				for (int i4 = 0; i4 < k2; i4++) {
					int l4 = ai1[i4];
					int l5 = anIntArray1638[l4];
					int j6 = anIntArray1673[l5]++;
					anIntArrayArray1674[l5][j6] = l4;
					if (l5 < 10) {
						anIntArray1677[l5] += i2;
					} else if (l5 == 10) {
						anIntArray1675[j6] = i2;
					} else {
						anIntArray1676[j6] = i2;
					}
				}

			}
		}

		int l2 = 0;
		if (anIntArray1673[1] > 0 || anIntArray1673[2] > 0) {
			l2 = (anIntArray1677[1] + anIntArray1677[2]) / (anIntArray1673[1] + anIntArray1673[2]);
		}
		int k3 = 0;
		if (anIntArray1673[3] > 0 || anIntArray1673[4] > 0) {
			k3 = (anIntArray1677[3] + anIntArray1677[4]) / (anIntArray1673[3] + anIntArray1673[4]);
		}
		int j4 = 0;
		if (anIntArray1673[6] > 0 || anIntArray1673[8] > 0) {
			j4 = (anIntArray1677[6] + anIntArray1677[8]) / (anIntArray1673[6] + anIntArray1673[8]);
		}
		int i6 = 0;
		int k6 = anIntArray1673[10];
		int ai2[] = anIntArrayArray1674[10];
		int ai3[] = anIntArray1675;
		if (i6 == k6) {
			i6 = 0;
			k6 = anIntArray1673[11];
			ai2 = anIntArrayArray1674[11];
			ai3 = anIntArray1676;
		}
		int i5;
		if (i6 < k6) {
			i5 = ai3[i6];
		} else {
			i5 = -1000;
		}
		for (int l6 = 0; l6 < 10; l6++) {
			while (l6 == 0 && i5 > l2) {
				drawFace(ai2[i6++]);
				if (i6 == k6 && ai2 != anIntArrayArray1674[11]) {
					i6 = 0;
					k6 = anIntArray1673[11];
					ai2 = anIntArrayArray1674[11];
					ai3 = anIntArray1676;
				}
				if (i6 < k6) {
					i5 = ai3[i6];
				} else {
					i5 = -1000;
				}
			}
			while (l6 == 3 && i5 > k3) {
				drawFace(ai2[i6++]);
				if (i6 == k6 && ai2 != anIntArrayArray1674[11]) {
					i6 = 0;
					k6 = anIntArray1673[11];
					ai2 = anIntArrayArray1674[11];
					ai3 = anIntArray1676;
				}
				if (i6 < k6) {
					i5 = ai3[i6];
				} else {
					i5 = -1000;
				}
			}
			while (l6 == 5 && i5 > j4) {
				drawFace(ai2[i6++]);
				if (i6 == k6 && ai2 != anIntArrayArray1674[11]) {
					i6 = 0;
					k6 = anIntArray1673[11];
					ai2 = anIntArrayArray1674[11];
					ai3 = anIntArray1676;
				}
				if (i6 < k6) {
					i5 = ai3[i6];
				} else {
					i5 = -1000;
				}
			}
			int i7 = anIntArray1673[l6];
			int ai4[] = anIntArrayArray1674[l6];
			for (int j7 = 0; j7 < i7; j7++) {
				drawFace(ai4[j7]);
			}

		}

		while (i5 != -1000) {
			drawFace(ai2[i6++]);
			if (i6 == k6 && ai2 != anIntArrayArray1674[11]) {
				i6 = 0;
				ai2 = anIntArrayArray1674[11];
				k6 = anIntArray1673[11];
				ai3 = anIntArray1676;
			}
			if (i6 < k6) {
				i5 = ai3[i6];
			} else {
				i5 = -1000;
			}
		}
	}

	private void drawFace(int faceIndex) {
		if (aBooleanArray1664[faceIndex]) {
			drawFaceClippedToNearPlane(faceIndex);
			return;
		}
		int vertexA = anIntArray1631[faceIndex];
		int vertexB = anIntArray1632[faceIndex];
		int vertexC = anIntArray1633[faceIndex];
		Texture.clipScanlines = aBooleanArray1663[faceIndex];
		if (anIntArray1639 == null) {
			Texture.transparency = 0;
		} else {
			Texture.transparency = anIntArray1639[faceIndex];
		}
		int renderType;
		if (anIntArray1637 == null) {
			renderType = 0;
		} else {
			renderType = anIntArray1637[faceIndex] & 3;
		}
		if (renderType == 0) {
			Texture.drawShadedTriangle(anIntArray1666[vertexA], anIntArray1666[vertexB], anIntArray1666[vertexC], anIntArray1665[vertexA], anIntArray1665[vertexB], anIntArray1665[vertexC], anIntArray1634[faceIndex], anIntArray1635[faceIndex], anIntArray1636[faceIndex]);
			return;
		}
		if (renderType == 1) {
			Texture.drawFlatTriangle(anIntArray1666[vertexA], anIntArray1666[vertexB], anIntArray1666[vertexC], anIntArray1665[vertexA], anIntArray1665[vertexB], anIntArray1665[vertexC], hslToRgbPalette[anIntArray1634[faceIndex]]);
			return;
		}
		if (renderType == 2) {
			int textureFaceIndex = anIntArray1637[faceIndex] >> 2;
			int mappingVertexA = anIntArray1643[textureFaceIndex];
			int mappingVertexB = anIntArray1644[textureFaceIndex];
			int mappingVertexC = anIntArray1645[textureFaceIndex];
			Texture.drawTexturedTriangle(anIntArray1666[vertexA], anIntArray1666[vertexB], anIntArray1666[vertexC], anIntArray1665[vertexA], anIntArray1665[vertexB], anIntArray1665[vertexC], anIntArray1634[faceIndex], anIntArray1635[faceIndex], anIntArray1636[faceIndex], anIntArray1668[mappingVertexA], anIntArray1668[mappingVertexB], anIntArray1668[mappingVertexC], anIntArray1669[mappingVertexA], anIntArray1669[mappingVertexB], anIntArray1669[mappingVertexC], anIntArray1670[mappingVertexA], anIntArray1670[mappingVertexB], anIntArray1670[mappingVertexC], anIntArray1640[faceIndex]);
			return;
		}
		if (renderType == 3) {
			int textureFaceIndex = anIntArray1637[faceIndex] >> 2;
			int mappingVertexA = anIntArray1643[textureFaceIndex];
			int mappingVertexB = anIntArray1644[textureFaceIndex];
			int mappingVertexC = anIntArray1645[textureFaceIndex];
			Texture.drawTexturedTriangle(anIntArray1666[vertexA], anIntArray1666[vertexB], anIntArray1666[vertexC], anIntArray1665[vertexA], anIntArray1665[vertexB], anIntArray1665[vertexC], anIntArray1634[faceIndex], anIntArray1634[faceIndex], anIntArray1634[faceIndex], anIntArray1668[mappingVertexA], anIntArray1668[mappingVertexB], anIntArray1668[mappingVertexC], anIntArray1669[mappingVertexA], anIntArray1669[mappingVertexB], anIntArray1669[mappingVertexC], anIntArray1670[mappingVertexA], anIntArray1670[mappingVertexB], anIntArray1670[mappingVertexC], anIntArray1640[faceIndex]);
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
			clippedX[clippedPointCount] = anIntArray1665[vertexA];
			clippedY[clippedPointCount] = anIntArray1666[vertexA];
			clippedShade[clippedPointCount++] = anIntArray1634[faceIndex];
		} else {
			int cameraXA = anIntArray1668[vertexA];
			int cameraYA = anIntArray1669[vertexA];
			int shadeA = anIntArray1634[faceIndex];
			if (depthC >= 50) {
				int fractionAToC = (50 - depthA) * reciprocalTable16[depthC - depthA];
				clippedX[clippedPointCount] = screenCenterX + (cameraXA + ((anIntArray1668[vertexC] - cameraXA) * fractionAToC >> 16) << 9) / 50;
				clippedY[clippedPointCount] = screenCenterY + (cameraYA + ((anIntArray1669[vertexC] - cameraYA) * fractionAToC >> 16) << 9) / 50;
				clippedShade[clippedPointCount++] = shadeA + ((anIntArray1636[faceIndex] - shadeA) * fractionAToC >> 16);
			}
			if (depthB >= 50) {
				int fractionAToB = (50 - depthA) * reciprocalTable16[depthB - depthA];
				clippedX[clippedPointCount] = screenCenterX + (cameraXA + ((anIntArray1668[vertexB] - cameraXA) * fractionAToB >> 16) << 9) / 50;
				clippedY[clippedPointCount] = screenCenterY + (cameraYA + ((anIntArray1669[vertexB] - cameraYA) * fractionAToB >> 16) << 9) / 50;
				clippedShade[clippedPointCount++] = shadeA + ((anIntArray1635[faceIndex] - shadeA) * fractionAToB >> 16);
			}
		}
		if (depthB >= 50) {
			clippedX[clippedPointCount] = anIntArray1665[vertexB];
			clippedY[clippedPointCount] = anIntArray1666[vertexB];
			clippedShade[clippedPointCount++] = anIntArray1635[faceIndex];
		} else {
			int cameraXB = anIntArray1668[vertexB];
			int cameraYB = anIntArray1669[vertexB];
			int shadeB = anIntArray1635[faceIndex];
			if (depthA >= 50) {
				int fractionBToA = (50 - depthB) * reciprocalTable16[depthA - depthB];
				clippedX[clippedPointCount] = screenCenterX + (cameraXB + ((anIntArray1668[vertexA] - cameraXB) * fractionBToA >> 16) << 9) / 50;
				clippedY[clippedPointCount] = screenCenterY + (cameraYB + ((anIntArray1669[vertexA] - cameraYB) * fractionBToA >> 16) << 9) / 50;
				clippedShade[clippedPointCount++] = shadeB + ((anIntArray1634[faceIndex] - shadeB) * fractionBToA >> 16);
			}
			if (depthC >= 50) {
				int fractionBToC = (50 - depthB) * reciprocalTable16[depthC - depthB];
				clippedX[clippedPointCount] = screenCenterX + (cameraXB + ((anIntArray1668[vertexC] - cameraXB) * fractionBToC >> 16) << 9) / 50;
				clippedY[clippedPointCount] = screenCenterY + (cameraYB + ((anIntArray1669[vertexC] - cameraYB) * fractionBToC >> 16) << 9) / 50;
				clippedShade[clippedPointCount++] = shadeB + ((anIntArray1636[faceIndex] - shadeB) * fractionBToC >> 16);
			}
		}
		if (depthC >= 50) {
			clippedX[clippedPointCount] = anIntArray1665[vertexC];
			clippedY[clippedPointCount] = anIntArray1666[vertexC];
			clippedShade[clippedPointCount++] = anIntArray1636[faceIndex];
		} else {
			int cameraXC = anIntArray1668[vertexC];
			int cameraYC = anIntArray1669[vertexC];
			int shadeC = anIntArray1636[faceIndex];
			if (depthB >= 50) {
				int fractionCToB = (50 - depthC) * reciprocalTable16[depthB - depthC];
				clippedX[clippedPointCount] = screenCenterX + (cameraXC + ((anIntArray1668[vertexB] - cameraXC) * fractionCToB >> 16) << 9) / 50;
				clippedY[clippedPointCount] = screenCenterY + (cameraYC + ((anIntArray1669[vertexB] - cameraYC) * fractionCToB >> 16) << 9) / 50;
				clippedShade[clippedPointCount++] = shadeC + ((anIntArray1635[faceIndex] - shadeC) * fractionCToB >> 16);
			}
			if (depthA >= 50) {
				int fractionCToA = (50 - depthC) * reciprocalTable16[depthA - depthC];
				clippedX[clippedPointCount] = screenCenterX + (cameraXC + ((anIntArray1668[vertexA] - cameraXC) * fractionCToA >> 16) << 9) / 50;
				clippedY[clippedPointCount] = screenCenterY + (cameraYC + ((anIntArray1669[vertexA] - cameraYC) * fractionCToA >> 16) << 9) / 50;
				clippedShade[clippedPointCount++] = shadeC + ((anIntArray1634[faceIndex] - shadeC) * fractionCToA >> 16);
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
					Texture.drawFlatTriangle(screenY0, screenY1, screenY2, screenX0, screenX1, screenX2, hslToRgbPalette[anIntArray1634[faceIndex]]);
				} else if (renderType == 2) {
					int textureFaceIndex = anIntArray1637[faceIndex] >> 2;
					int mappingVertexA = anIntArray1643[textureFaceIndex];
					int mappingVertexB = anIntArray1644[textureFaceIndex];
					int mappingVertexC = anIntArray1645[textureFaceIndex];
					Texture.drawTexturedTriangle(screenY0, screenY1, screenY2, screenX0, screenX1, screenX2, clippedShade[0], clippedShade[1], clippedShade[2], anIntArray1668[mappingVertexA], anIntArray1668[mappingVertexB], anIntArray1668[mappingVertexC], anIntArray1669[mappingVertexA], anIntArray1669[mappingVertexB], anIntArray1669[mappingVertexC], anIntArray1670[mappingVertexA], anIntArray1670[mappingVertexB], anIntArray1670[mappingVertexC], anIntArray1640[faceIndex]);
				} else if (renderType == 3) {
					int textureFaceIndex = anIntArray1637[faceIndex] >> 2;
					int mappingVertexA = anIntArray1643[textureFaceIndex];
					int mappingVertexB = anIntArray1644[textureFaceIndex];
					int mappingVertexC = anIntArray1645[textureFaceIndex];
					Texture.drawTexturedTriangle(screenY0, screenY1, screenY2, screenX0, screenX1, screenX2, anIntArray1634[faceIndex], anIntArray1634[faceIndex], anIntArray1634[faceIndex], anIntArray1668[mappingVertexA], anIntArray1668[mappingVertexB], anIntArray1668[mappingVertexC], anIntArray1669[mappingVertexA], anIntArray1669[mappingVertexB], anIntArray1669[mappingVertexC], anIntArray1670[mappingVertexA], anIntArray1670[mappingVertexB], anIntArray1670[mappingVertexC], anIntArray1640[faceIndex]);
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
					int flatColor = hslToRgbPalette[anIntArray1634[faceIndex]];
					Texture.drawFlatTriangle(screenY0, screenY1, screenY2, screenX0, screenX1, screenX2, flatColor);
					Texture.drawFlatTriangle(screenY0, screenY2, clippedY[3], screenX0, screenX2, clippedX[3], flatColor);
					return;
				}
				if (renderType == 2) {
					int textureFaceIndex = anIntArray1637[faceIndex] >> 2;
					int mappingVertexA = anIntArray1643[textureFaceIndex];
					int mappingVertexB = anIntArray1644[textureFaceIndex];
					int mappingVertexC = anIntArray1645[textureFaceIndex];
					Texture.drawTexturedTriangle(screenY0, screenY1, screenY2, screenX0, screenX1, screenX2, clippedShade[0], clippedShade[1], clippedShade[2], anIntArray1668[mappingVertexA], anIntArray1668[mappingVertexB], anIntArray1668[mappingVertexC], anIntArray1669[mappingVertexA], anIntArray1669[mappingVertexB], anIntArray1669[mappingVertexC], anIntArray1670[mappingVertexA], anIntArray1670[mappingVertexB], anIntArray1670[mappingVertexC], anIntArray1640[faceIndex]);
					Texture.drawTexturedTriangle(screenY0, screenY2, clippedY[3], screenX0, screenX2, clippedX[3], clippedShade[0], clippedShade[2], clippedShade[3], anIntArray1668[mappingVertexA], anIntArray1668[mappingVertexB], anIntArray1668[mappingVertexC], anIntArray1669[mappingVertexA], anIntArray1669[mappingVertexB], anIntArray1669[mappingVertexC], anIntArray1670[mappingVertexA], anIntArray1670[mappingVertexB], anIntArray1670[mappingVertexC], anIntArray1640[faceIndex]);
					return;
				}
				if (renderType == 3) {
					int textureFaceIndex = anIntArray1637[faceIndex] >> 2;
					int mappingVertexA = anIntArray1643[textureFaceIndex];
					int mappingVertexB = anIntArray1644[textureFaceIndex];
					int mappingVertexC = anIntArray1645[textureFaceIndex];
					Texture.drawTexturedTriangle(screenY0, screenY1, screenY2, screenX0, screenX1, screenX2, anIntArray1634[faceIndex], anIntArray1634[faceIndex], anIntArray1634[faceIndex], anIntArray1668[mappingVertexA], anIntArray1668[mappingVertexB], anIntArray1668[mappingVertexC], anIntArray1669[mappingVertexA], anIntArray1669[mappingVertexB], anIntArray1669[mappingVertexC], anIntArray1670[mappingVertexA], anIntArray1670[mappingVertexB], anIntArray1670[mappingVertexC], anIntArray1640[faceIndex]);
					Texture.drawTexturedTriangle(screenY0, screenY2, clippedY[3], screenX0, screenX2, clippedX[3], anIntArray1634[faceIndex], anIntArray1634[faceIndex], anIntArray1634[faceIndex], anIntArray1668[mappingVertexA], anIntArray1668[mappingVertexB], anIntArray1668[mappingVertexC], anIntArray1669[mappingVertexA], anIntArray1669[mappingVertexB], anIntArray1669[mappingVertexC], anIntArray1670[mappingVertexA], anIntArray1670[mappingVertexB], anIntArray1670[mappingVertexC], anIntArray1640[faceIndex]);
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
	private int[] anIntArray1634;
	private int[] anIntArray1635;
	private int[] anIntArray1636;
	public int anIntArray1637[];
	private int[] anIntArray1638;
	private int[] anIntArray1639;
	public int anIntArray1640[];
	private int anInt1641;
	private int anInt1642;
	private int[] anIntArray1643;
	private int[] anIntArray1644;
	private int[] anIntArray1645;
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
	Class33 aClass33Array1660[];
	private static ModelHeader[] modelHeaders;
	private static OnDemandFetcherParent modelRequester;
	private static boolean[] aBooleanArray1663 = new boolean[4096];
	private static boolean[] aBooleanArray1664 = new boolean[4096];
	private static int[] anIntArray1665 = new int[4096];
	private static int[] anIntArray1666 = new int[4096];
	private static int[] anIntArray1667 = new int[4096];
	private static int[] anIntArray1668 = new int[4096];
	private static int[] anIntArray1669 = new int[4096];
	private static int[] anIntArray1670 = new int[4096];
	private static int[] anIntArray1671 = new int[1500];
	private static int[][] anIntArrayArray1672 = new int[1500][512];
	private static int[] anIntArray1673 = new int[12];
	private static int[][] anIntArrayArray1674 = new int[12][2000];
	private static int[] anIntArray1675 = new int[2000];
	private static int[] anIntArray1676 = new int[2000];
	private static int[] anIntArray1677 = new int[12];
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
