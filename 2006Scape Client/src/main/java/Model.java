// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

public final class Model extends Animable {

	public static void nullLoader() {
		aClass21Array1661 = null;
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
		modelIntArray1 = null;
		modelIntArray2 = null;
		modelIntArray3 = null;
		modelIntArray4 = null;
	}

	public static void method459(int i, OnDemandFetcherParent onDemandFetcherParent) {
		aClass21Array1661 = new Class21[i];
		aOnDemandFetcherParent_1662 = onDemandFetcherParent;
	}

	public static void method460(byte abyte0[], int j) {
		if (abyte0 == null) {
			Class21 class21 = aClass21Array1661[j] = new Class21();
			class21.anInt369 = 0;
			class21.anInt370 = 0;
			class21.anInt371 = 0;
			return;
		}
		Stream stream = new Stream(abyte0);
		stream.currentOffset = abyte0.length - 18;
		Class21 class21_1 = aClass21Array1661[j] = new Class21();
		class21_1.aByteArray368 = abyte0;
		class21_1.anInt369 = stream.readUnsignedShortBigEndian();
		class21_1.anInt370 = stream.readUnsignedShortBigEndian();
		class21_1.anInt371 = stream.readUnsignedByte();
		int k = stream.readUnsignedByte();
		int l = stream.readUnsignedByte();
		int i1 = stream.readUnsignedByte();
		int j1 = stream.readUnsignedByte();
		int k1 = stream.readUnsignedByte();
		int l1 = stream.readUnsignedShortBigEndian();
		int i2 = stream.readUnsignedShortBigEndian();
		int j2 = stream.readUnsignedShortBigEndian();
		int k2 = stream.readUnsignedShortBigEndian();
		int l2 = 0;
		class21_1.anInt372 = l2;
		l2 += class21_1.anInt369;
		class21_1.anInt378 = l2;
		l2 += class21_1.anInt370;
		class21_1.anInt381 = l2;
		if (l == 255) {
			l2 += class21_1.anInt370;
		} else {
			class21_1.anInt381 = -l - 1;
		}
		class21_1.anInt383 = l2;
		if (j1 == 1) {
			l2 += class21_1.anInt370;
		} else {
			class21_1.anInt383 = -1;
		}
		class21_1.anInt380 = l2;
		if (k == 1) {
			l2 += class21_1.anInt370;
		} else {
			class21_1.anInt380 = -1;
		}
		class21_1.anInt376 = l2;
		if (k1 == 1) {
			l2 += class21_1.anInt369;
		} else {
			class21_1.anInt376 = -1;
		}
		class21_1.anInt382 = l2;
		if (i1 == 1) {
			l2 += class21_1.anInt370;
		} else {
			class21_1.anInt382 = -1;
		}
		class21_1.anInt377 = l2;
		l2 += k2;
		class21_1.anInt379 = l2;
		l2 += class21_1.anInt370 * 2;
		class21_1.anInt384 = l2;
		l2 += class21_1.anInt371 * 6;
		class21_1.anInt373 = l2;
		l2 += l1;
		class21_1.anInt374 = l2;
		l2 += i2;
		class21_1.anInt375 = l2;
		l2 += j2;
	}

	public static void method461(int j) {
		aClass21Array1661[j] = null;
	}

	public static Model method462(int j) {
		if (aClass21Array1661 == null) {
			return null;
		}
		Class21 class21 = aClass21Array1661.length < j  ? null : aClass21Array1661[j];
		if (class21 == null) {
			aOnDemandFetcherParent_1662.method548(j);
			return null;
		} else {
			return new Model(j);
		}
	}

	public static boolean method463(int i) {
		if (aClass21Array1661 == null) {
			return false;
		}
		Class21 class21 = aClass21Array1661[i];
		if (class21 == null) {
			aOnDemandFetcherParent_1662.method548(i);
			return false;
		} else {
			return true;
		}
	}

	private Model() {
		aBoolean1659 = false;
	}

	private Model(int i) {
		aBoolean1659 = false;
		Class21 class21 = aClass21Array1661[i];
		anInt1626 = class21.anInt369;
		anInt1630 = class21.anInt370;
		anInt1642 = class21.anInt371;
		anIntArray1627 = new int[anInt1626];
		anIntArray1628 = new int[anInt1626];
		anIntArray1629 = new int[anInt1626];
		anIntArray1631 = new int[anInt1630];
		anIntArray1632 = new int[anInt1630];
		anIntArray1633 = new int[anInt1630];
		anIntArray1643 = new int[anInt1642];
		anIntArray1644 = new int[anInt1642];
		anIntArray1645 = new int[anInt1642];
		if (class21.anInt376 >= 0) {
			vertexGroupIds = new int[anInt1626];
		}
		if (class21.anInt380 >= 0) {
			anIntArray1637 = new int[anInt1630];
		}
		if (class21.anInt381 >= 0) {
			anIntArray1638 = new int[anInt1630];
		} else {
			anInt1641 = -class21.anInt381 - 1;
		}
		if (class21.anInt382 >= 0) {
			anIntArray1639 = new int[anInt1630];
		}
		if (class21.anInt383 >= 0) {
			faceGroupIds = new int[anInt1630];
		}
		anIntArray1640 = new int[anInt1630];
		Stream stream = new Stream(class21.aByteArray368);
		stream.currentOffset = class21.anInt372;
		Stream stream_1 = new Stream(class21.aByteArray368);
		stream_1.currentOffset = class21.anInt373;
		Stream stream_2 = new Stream(class21.aByteArray368);
		stream_2.currentOffset = class21.anInt374;
		Stream stream_3 = new Stream(class21.aByteArray368);
		stream_3.currentOffset = class21.anInt375;
		Stream stream_4 = new Stream(class21.aByteArray368);
		stream_4.currentOffset = class21.anInt376;
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

		stream.currentOffset = class21.anInt379;
		stream_1.currentOffset = class21.anInt380;
		stream_2.currentOffset = class21.anInt381;
		stream_3.currentOffset = class21.anInt382;
		stream_4.currentOffset = class21.anInt383;
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

		stream.currentOffset = class21.anInt377;
		stream_1.currentOffset = class21.anInt378;
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

		stream.currentOffset = class21.anInt384;
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

	public void method470(int i) {
		if (vertexGroups == null) {
			return;
		}
		if (i == -1) {
			return;
		}
		Class36 class36 = Class36.getFrame(i);
		if (class36 == null) {
			return;
		}
		Class18 class18 = class36.aClass18_637;
		anInt1681 = 0;
		anInt1682 = 0;
		anInt1683 = 0;
		for (int k = 0; k < class36.anInt638; k++) {
			int l = class36.anIntArray639[k];
			method472(class18.anIntArray342[l], class18.anIntArrayArray343[l], class36.anIntArray640[k], class36.anIntArray641[k], class36.anIntArray642[k]);
		}

	}

	public void method471(int ai[], int j, int k) {
		if (k == -1) {
			return;
		}
		if (ai == null || j == -1) {
			method470(k);
			return;
		}
		Class36 class36 = Class36.getFrame(k);
		if (class36 == null) {
			return;
		}
		Class36 class36_1 = Class36.getFrame(j);
		if (class36_1 == null) {
			method470(k);
			return;
		}
		Class18 class18 = class36.aClass18_637;
		anInt1681 = 0;
		anInt1682 = 0;
		anInt1683 = 0;
		int l = 0;
		int i1 = ai[l++];
		for (int j1 = 0; j1 < class36.anInt638; j1++) {
			int k1;
			for (k1 = class36.anIntArray639[j1]; k1 > i1; i1 = ai[l++]) {
				;
			}
			if (k1 != i1 || class18.anIntArray342[k1] == 0) {
				method472(class18.anIntArray342[k1], class18.anIntArrayArray343[k1], class36.anIntArray640[j1], class36.anIntArray641[j1], class36.anIntArray642[j1]);
			}
		}

		anInt1681 = 0;
		anInt1682 = 0;
		anInt1683 = 0;
		l = 0;
		i1 = ai[l++];
		for (int l1 = 0; l1 < class36_1.anInt638; l1++) {
			int i2;
			for (i2 = class36_1.anIntArray639[l1]; i2 > i1; i1 = ai[l++]) {
				;
			}
			if (i2 == i1 || class18.anIntArray342[i2] == 0) {
				method472(class18.anIntArray342[i2], class18.anIntArrayArray343[i2], class36_1.anIntArray640[l1], class36_1.anIntArray641[l1], class36_1.anIntArray642[l1]);
			}
		}

	}

	private void method472(int i, int ai[], int j, int k, int l) {
		int i1 = ai.length;
		if (i == 0) {
			int j1 = 0;
			anInt1681 = 0;
			anInt1682 = 0;
			anInt1683 = 0;
			for (int k2 = 0; k2 < i1; k2++) {
				int l3 = ai[k2];
				if (l3 < vertexGroups.length) {
					int ai5[] = vertexGroups[l3];
					for (int j6 : ai5) {
						anInt1681 += anIntArray1627[j6];
						anInt1682 += anIntArray1628[j6];
						anInt1683 += anIntArray1629[j6];
						j1++;
					}

				}
			}

			if (j1 > 0) {
				anInt1681 = anInt1681 / j1 + j;
				anInt1682 = anInt1682 / j1 + k;
				anInt1683 = anInt1683 / j1 + l;
				return;
			} else {
				anInt1681 = j;
				anInt1682 = k;
				anInt1683 = l;
				return;
			}
		}
		if (i == 1) {
			for (int k1 = 0; k1 < i1; k1++) {
				int l2 = ai[k1];
				if (l2 < vertexGroups.length) {
					int ai1[] = vertexGroups[l2];
					for (int element : ai1) {
						int j5 = element;
						anIntArray1627[j5] += j;
						anIntArray1628[j5] += k;
						anIntArray1629[j5] += l;
					}

				}
			}

			return;
		}
		if (i == 2) {
			for (int l1 = 0; l1 < i1; l1++) {
				int i3 = ai[l1];
				if (i3 < vertexGroups.length) {
					int ai2[] = vertexGroups[i3];
					for (int element : ai2) {
						int k5 = element;
						anIntArray1627[k5] -= anInt1681;
						anIntArray1628[k5] -= anInt1682;
						anIntArray1629[k5] -= anInt1683;
						int k6 = (j & 0xff) * 8;
						int l6 = (k & 0xff) * 8;
						int i7 = (l & 0xff) * 8;
						if (i7 != 0) {
							int j7 = modelIntArray1[i7];
							int i8 = modelIntArray2[i7];
							int l8 = anIntArray1628[k5] * j7 + anIntArray1627[k5] * i8 >> 16;
							anIntArray1628[k5] = anIntArray1628[k5] * i8 - anIntArray1627[k5] * j7 >> 16;
							anIntArray1627[k5] = l8;
						}
						if (k6 != 0) {
							int k7 = modelIntArray1[k6];
							int j8 = modelIntArray2[k6];
							int i9 = anIntArray1628[k5] * j8 - anIntArray1629[k5] * k7 >> 16;
							anIntArray1629[k5] = anIntArray1628[k5] * k7 + anIntArray1629[k5] * j8 >> 16;
							anIntArray1628[k5] = i9;
						}
						if (l6 != 0) {
							int l7 = modelIntArray1[l6];
							int k8 = modelIntArray2[l6];
							int j9 = anIntArray1629[k5] * l7 + anIntArray1627[k5] * k8 >> 16;
							anIntArray1629[k5] = anIntArray1629[k5] * k8 - anIntArray1627[k5] * l7 >> 16;
							anIntArray1627[k5] = j9;
						}
						anIntArray1627[k5] += anInt1681;
						anIntArray1628[k5] += anInt1682;
						anIntArray1629[k5] += anInt1683;
					}

				}
			}

			return;
		}
		if (i == 3) {
			for (int i2 = 0; i2 < i1; i2++) {
				int j3 = ai[i2];
				if (j3 < vertexGroups.length) {
					int ai3[] = vertexGroups[j3];
					for (int element : ai3) {
						int l5 = element;
						anIntArray1627[l5] -= anInt1681;
						anIntArray1628[l5] -= anInt1682;
						anIntArray1629[l5] -= anInt1683;
						anIntArray1627[l5] = anIntArray1627[l5] * j / 128;
						anIntArray1628[l5] = anIntArray1628[l5] * k / 128;
						anIntArray1629[l5] = anIntArray1629[l5] * l / 128;
						anIntArray1627[l5] += anInt1681;
						anIntArray1628[l5] += anInt1682;
						anIntArray1629[l5] += anInt1683;
					}

				}
			}

			return;
		}
		if (i == 5 && faceGroups != null && anIntArray1639 != null) {
			for (int j2 = 0; j2 < i1; j2++) {
				int k3 = ai[j2];
				if (k3 < faceGroups.length) {
					int ai4[] = faceGroups[k3];
					for (int element : ai4) {
						int i6 = element;
						anIntArray1639[i6] += j * 8;
						if (anIntArray1639[i6] < 0) {
							anIntArray1639[i6] = 0;
						}
						if (anIntArray1639[i6] > 255) {
							anIntArray1639[i6] = 255;
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
		int sine16 = modelIntArray1[angle2048];
		int cosine16 = modelIntArray2[angle2048];
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

	public void method479(int i, int j, int k, int l, int i1, boolean flag) {
		int j1 = (int) Math.sqrt(k * k + l * l + i1 * i1);
		int k1 = j * j1 >> 8;
		if (anIntArray1634 == null) {
			anIntArray1634 = new int[anInt1630];
			anIntArray1635 = new int[anInt1630];
			anIntArray1636 = new int[anInt1630];
		}
		if (super.aClass33Array1425 == null) {
			super.aClass33Array1425 = new Class33[anInt1626];
			for (int l1 = 0; l1 < anInt1626; l1++) {
				super.aClass33Array1425[l1] = new Class33();
			}

		}
		for (int i2 = 0; i2 < anInt1630; i2++) {
			int j2 = anIntArray1631[i2];
			int l2 = anIntArray1632[i2];
			int i3 = anIntArray1633[i2];
			int j3 = anIntArray1627[l2] - anIntArray1627[j2];
			int k3 = anIntArray1628[l2] - anIntArray1628[j2];
			int l3 = anIntArray1629[l2] - anIntArray1629[j2];
			int i4 = anIntArray1627[i3] - anIntArray1627[j2];
			int j4 = anIntArray1628[i3] - anIntArray1628[j2];
			int k4 = anIntArray1629[i3] - anIntArray1629[j2];
			int l4 = k3 * k4 - j4 * l3;
			int i5 = l3 * i4 - k4 * j3;
			int j5;
			for (j5 = j3 * j4 - i4 * k3; l4 > 8192 || i5 > 8192 || j5 > 8192 || l4 < -8192 || i5 < -8192 || j5 < -8192; j5 >>= 1) {
				l4 >>= 1;
				i5 >>= 1;
			}

			int k5 = (int) Math.sqrt(l4 * l4 + i5 * i5 + j5 * j5);
			if (k5 <= 0) {
				k5 = 1;
			}
			l4 = l4 * 256 / k5;
			i5 = i5 * 256 / k5;
			j5 = j5 * 256 / k5;
			if (anIntArray1637 == null || (anIntArray1637[i2] & 1) == 0) {
				Class33 class33_2 = super.aClass33Array1425[j2];
				class33_2.anInt602 += l4;
				class33_2.anInt603 += i5;
				class33_2.anInt604 += j5;
				class33_2.anInt605++;
				class33_2 = super.aClass33Array1425[l2];
				class33_2.anInt602 += l4;
				class33_2.anInt603 += i5;
				class33_2.anInt604 += j5;
				class33_2.anInt605++;
				class33_2 = super.aClass33Array1425[i3];
				class33_2.anInt602 += l4;
				class33_2.anInt603 += i5;
				class33_2.anInt604 += j5;
				class33_2.anInt605++;
			} else {
				int l5 = i + (k * l4 + l * i5 + i1 * j5) / (k1 + k1 / 2);
				anIntArray1634[i2] = calculateFaceShade(anIntArray1640[i2], l5, anIntArray1637[i2]);
			}
		}

		if (flag) {
			method480(i, k1, k, l, i1);
		} else {
			aClass33Array1660 = new Class33[anInt1626];
			for (int k2 = 0; k2 < anInt1626; k2++) {
				Class33 class33 = super.aClass33Array1425[k2];
				Class33 class33_1 = aClass33Array1660[k2] = new Class33();
				class33_1.anInt602 = class33.anInt602;
				class33_1.anInt603 = class33.anInt603;
				class33_1.anInt604 = class33.anInt604;
				class33_1.anInt605 = class33.anInt605;
			}

		}
		if (flag) {
			calculateBounds();
		} else {
			calculateBoundsWithExtents();
		}
	}

	public void method480(int i, int j, int k, int l, int i1) {
		for (int j1 = 0; j1 < anInt1630; j1++) {
			int k1 = anIntArray1631[j1];
			int i2 = anIntArray1632[j1];
			int j2 = anIntArray1633[j1];
			if (anIntArray1637 == null) {
				int i3 = anIntArray1640[j1];
				Class33 class33 = super.aClass33Array1425[k1];
				int k2 = i + (k * class33.anInt602 + l * class33.anInt603 + i1 * class33.anInt604) / (j * class33.anInt605);
				anIntArray1634[j1] = calculateFaceShade(i3, k2, 0);
				class33 = super.aClass33Array1425[i2];
				k2 = i + (k * class33.anInt602 + l * class33.anInt603 + i1 * class33.anInt604) / (j * class33.anInt605);
				anIntArray1635[j1] = calculateFaceShade(i3, k2, 0);
				class33 = super.aClass33Array1425[j2];
				k2 = i + (k * class33.anInt602 + l * class33.anInt603 + i1 * class33.anInt604) / (j * class33.anInt605);
				anIntArray1636[j1] = calculateFaceShade(i3, k2, 0);
			} else if ((anIntArray1637[j1] & 1) == 0) {
				int j3 = anIntArray1640[j1];
				int k3 = anIntArray1637[j1];
				Class33 class33_1 = super.aClass33Array1425[k1];
				int l2 = i + (k * class33_1.anInt602 + l * class33_1.anInt603 + i1 * class33_1.anInt604) / (j * class33_1.anInt605);
				anIntArray1634[j1] = calculateFaceShade(j3, l2, k3);
				class33_1 = super.aClass33Array1425[i2];
				l2 = i + (k * class33_1.anInt602 + l * class33_1.anInt603 + i1 * class33_1.anInt604) / (j * class33_1.anInt605);
				anIntArray1635[j1] = calculateFaceShade(j3, l2, k3);
				class33_1 = super.aClass33Array1425[j2];
				l2 = i + (k * class33_1.anInt602 + l * class33_1.anInt603 + i1 * class33_1.anInt604) / (j * class33_1.anInt605);
				anIntArray1636[j1] = calculateFaceShade(j3, l2, k3);
			}
		}

		super.aClass33Array1425 = null;
		aClass33Array1660 = null;
		vertexGroupIds = null;
		faceGroupIds = null;
		if (anIntArray1637 != null) {
			for (int l1 = 0; l1 < anInt1630; l1++) {
				if ((anIntArray1637[l1] & 2) == 2) {
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

	public void method482(int j, int k, int l, int i1, int j1, int k1) {
		int i = 0; // was a parameter
		int l1 = Texture.textureInt1;
		int i2 = Texture.textureInt2;
		int j2 = modelIntArray1[i];
		int k2 = modelIntArray2[i];
		int l2 = modelIntArray1[j];
		int i3 = modelIntArray2[j];
		int j3 = modelIntArray1[k];
		int k3 = modelIntArray2[k];
		int l3 = modelIntArray1[l];
		int i4 = modelIntArray2[l];
		int j4 = j1 * l3 + k1 * i4 >> 16;
		for (int k4 = 0; k4 < anInt1626; k4++) {
			int l4 = anIntArray1627[k4];
			int i5 = anIntArray1628[k4];
			int j5 = anIntArray1629[k4];
			if (k != 0) {
				int k5 = i5 * j3 + l4 * k3 >> 16;
				i5 = i5 * k3 - l4 * j3 >> 16;
				l4 = k5;
			}
			if (i != 0) {
				int l5 = i5 * k2 - j5 * j2 >> 16;
				j5 = i5 * j2 + j5 * k2 >> 16;
				i5 = l5;
			}
			if (j != 0) {
				int i6 = j5 * l2 + l4 * i3 >> 16;
				j5 = j5 * i3 - l4 * l2 >> 16;
				l4 = i6;
			}
			l4 += i1;
			i5 += j1;
			j5 += k1;
			int j6 = i5 * i4 - j5 * l3 >> 16;
			j5 = i5 * l3 + j5 * i4 >> 16;
			i5 = j6;
			anIntArray1667[k4] = j5 - j4;
			anIntArray1665[k4] = l1 + (l4 << 9) / j5;
			anIntArray1666[k4] = i2 + (i5 << 9) / j5;
			if (anInt1642 > 0) {
				anIntArray1668[k4] = l4;
				anIntArray1669[k4] = i5;
				anIntArray1670[k4] = j5;
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
			l6 = modelIntArray1[i];
			i7 = modelIntArray2[i];
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
						method484(ai[j3]);
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
				method484(ai2[i6++]);
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
				method484(ai2[i6++]);
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
				method484(ai2[i6++]);
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
				method484(ai4[j7]);
			}

		}

		while (i5 != -1000) {
			method484(ai2[i6++]);
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

	private void method484(int i) {
		if (aBooleanArray1664[i]) {
			method485(i);
			return;
		}
		int j = anIntArray1631[i];
		int k = anIntArray1632[i];
		int l = anIntArray1633[i];
		Texture.aBoolean1462 = aBooleanArray1663[i];
		if (anIntArray1639 == null) {
			Texture.anInt1465 = 0;
		} else {
			Texture.anInt1465 = anIntArray1639[i];
		}
		int i1;
		if (anIntArray1637 == null) {
			i1 = 0;
		} else {
			i1 = anIntArray1637[i] & 3;
		}
		if (i1 == 0) {
			Texture.method374(anIntArray1666[j], anIntArray1666[k], anIntArray1666[l], anIntArray1665[j], anIntArray1665[k], anIntArray1665[l], anIntArray1634[i], anIntArray1635[i], anIntArray1636[i]);
			return;
		}
		if (i1 == 1) {
			Texture.method376(anIntArray1666[j], anIntArray1666[k], anIntArray1666[l], anIntArray1665[j], anIntArray1665[k], anIntArray1665[l], modelIntArray3[anIntArray1634[i]]);
			return;
		}
		if (i1 == 2) {
			int j1 = anIntArray1637[i] >> 2;
			int l1 = anIntArray1643[j1];
			int j2 = anIntArray1644[j1];
			int l2 = anIntArray1645[j1];
			Texture.method378(anIntArray1666[j], anIntArray1666[k], anIntArray1666[l], anIntArray1665[j], anIntArray1665[k], anIntArray1665[l], anIntArray1634[i], anIntArray1635[i], anIntArray1636[i], anIntArray1668[l1], anIntArray1668[j2], anIntArray1668[l2], anIntArray1669[l1], anIntArray1669[j2], anIntArray1669[l2], anIntArray1670[l1], anIntArray1670[j2], anIntArray1670[l2], anIntArray1640[i]);
			return;
		}
		if (i1 == 3) {
			int k1 = anIntArray1637[i] >> 2;
			int i2 = anIntArray1643[k1];
			int k2 = anIntArray1644[k1];
			int i3 = anIntArray1645[k1];
			Texture.method378(anIntArray1666[j], anIntArray1666[k], anIntArray1666[l], anIntArray1665[j], anIntArray1665[k], anIntArray1665[l], anIntArray1634[i], anIntArray1634[i], anIntArray1634[i], anIntArray1668[i2], anIntArray1668[k2], anIntArray1668[i3], anIntArray1669[i2], anIntArray1669[k2], anIntArray1669[i3], anIntArray1670[i2], anIntArray1670[k2], anIntArray1670[i3], anIntArray1640[i]);
		}
	}

	private void method485(int i) {
		int j = Texture.textureInt1;
		int k = Texture.textureInt2;
		int l = 0;
		int i1 = anIntArray1631[i];
		int j1 = anIntArray1632[i];
		int k1 = anIntArray1633[i];
		int l1 = anIntArray1670[i1];
		int i2 = anIntArray1670[j1];
		int j2 = anIntArray1670[k1];
		if (l1 >= 50) {
			anIntArray1678[l] = anIntArray1665[i1];
			anIntArray1679[l] = anIntArray1666[i1];
			anIntArray1680[l++] = anIntArray1634[i];
		} else {
			int k2 = anIntArray1668[i1];
			int k3 = anIntArray1669[i1];
			int k4 = anIntArray1634[i];
			if (j2 >= 50) {
				int k5 = (50 - l1) * modelIntArray4[j2 - l1];
				anIntArray1678[l] = j + (k2 + ((anIntArray1668[k1] - k2) * k5 >> 16) << 9) / 50;
				anIntArray1679[l] = k + (k3 + ((anIntArray1669[k1] - k3) * k5 >> 16) << 9) / 50;
				anIntArray1680[l++] = k4 + ((anIntArray1636[i] - k4) * k5 >> 16);
			}
			if (i2 >= 50) {
				int l5 = (50 - l1) * modelIntArray4[i2 - l1];
				anIntArray1678[l] = j + (k2 + ((anIntArray1668[j1] - k2) * l5 >> 16) << 9) / 50;
				anIntArray1679[l] = k + (k3 + ((anIntArray1669[j1] - k3) * l5 >> 16) << 9) / 50;
				anIntArray1680[l++] = k4 + ((anIntArray1635[i] - k4) * l5 >> 16);
			}
		}
		if (i2 >= 50) {
			anIntArray1678[l] = anIntArray1665[j1];
			anIntArray1679[l] = anIntArray1666[j1];
			anIntArray1680[l++] = anIntArray1635[i];
		} else {
			int l2 = anIntArray1668[j1];
			int l3 = anIntArray1669[j1];
			int l4 = anIntArray1635[i];
			if (l1 >= 50) {
				int i6 = (50 - i2) * modelIntArray4[l1 - i2];
				anIntArray1678[l] = j + (l2 + ((anIntArray1668[i1] - l2) * i6 >> 16) << 9) / 50;
				anIntArray1679[l] = k + (l3 + ((anIntArray1669[i1] - l3) * i6 >> 16) << 9) / 50;
				anIntArray1680[l++] = l4 + ((anIntArray1634[i] - l4) * i6 >> 16);
			}
			if (j2 >= 50) {
				int j6 = (50 - i2) * modelIntArray4[j2 - i2];
				anIntArray1678[l] = j + (l2 + ((anIntArray1668[k1] - l2) * j6 >> 16) << 9) / 50;
				anIntArray1679[l] = k + (l3 + ((anIntArray1669[k1] - l3) * j6 >> 16) << 9) / 50;
				anIntArray1680[l++] = l4 + ((anIntArray1636[i] - l4) * j6 >> 16);
			}
		}
		if (j2 >= 50) {
			anIntArray1678[l] = anIntArray1665[k1];
			anIntArray1679[l] = anIntArray1666[k1];
			anIntArray1680[l++] = anIntArray1636[i];
		} else {
			int i3 = anIntArray1668[k1];
			int i4 = anIntArray1669[k1];
			int i5 = anIntArray1636[i];
			if (i2 >= 50) {
				int k6 = (50 - j2) * modelIntArray4[i2 - j2];
				anIntArray1678[l] = j + (i3 + ((anIntArray1668[j1] - i3) * k6 >> 16) << 9) / 50;
				anIntArray1679[l] = k + (i4 + ((anIntArray1669[j1] - i4) * k6 >> 16) << 9) / 50;
				anIntArray1680[l++] = i5 + ((anIntArray1635[i] - i5) * k6 >> 16);
			}
			if (l1 >= 50) {
				int l6 = (50 - j2) * modelIntArray4[l1 - j2];
				anIntArray1678[l] = j + (i3 + ((anIntArray1668[i1] - i3) * l6 >> 16) << 9) / 50;
				anIntArray1679[l] = k + (i4 + ((anIntArray1669[i1] - i4) * l6 >> 16) << 9) / 50;
				anIntArray1680[l++] = i5 + ((anIntArray1634[i] - i5) * l6 >> 16);
			}
		}
		int j3 = anIntArray1678[0];
		int j4 = anIntArray1678[1];
		int j5 = anIntArray1678[2];
		int i7 = anIntArray1679[0];
		int j7 = anIntArray1679[1];
		int k7 = anIntArray1679[2];
		if ((j3 - j4) * (k7 - j7) - (i7 - j7) * (j5 - j4) > 0) {
			Texture.aBoolean1462 = false;
			if (l == 3) {
				if (j3 < 0 || j4 < 0 || j5 < 0 || j3 > DrawingArea.scanlineRightLimit || j4 > DrawingArea.scanlineRightLimit || j5 > DrawingArea.scanlineRightLimit) {
					Texture.aBoolean1462 = true;
				}
				int l7;
				if (anIntArray1637 == null) {
					l7 = 0;
				} else {
					l7 = anIntArray1637[i] & 3;
				}
				if (l7 == 0) {
					Texture.method374(i7, j7, k7, j3, j4, j5, anIntArray1680[0], anIntArray1680[1], anIntArray1680[2]);
				} else if (l7 == 1) {
					Texture.method376(i7, j7, k7, j3, j4, j5, modelIntArray3[anIntArray1634[i]]);
				} else if (l7 == 2) {
					int j8 = anIntArray1637[i] >> 2;
					int k9 = anIntArray1643[j8];
					int k10 = anIntArray1644[j8];
					int k11 = anIntArray1645[j8];
					Texture.method378(i7, j7, k7, j3, j4, j5, anIntArray1680[0], anIntArray1680[1], anIntArray1680[2], anIntArray1668[k9], anIntArray1668[k10], anIntArray1668[k11], anIntArray1669[k9], anIntArray1669[k10], anIntArray1669[k11], anIntArray1670[k9], anIntArray1670[k10], anIntArray1670[k11], anIntArray1640[i]);
				} else if (l7 == 3) {
					int k8 = anIntArray1637[i] >> 2;
					int l9 = anIntArray1643[k8];
					int l10 = anIntArray1644[k8];
					int l11 = anIntArray1645[k8];
					Texture.method378(i7, j7, k7, j3, j4, j5, anIntArray1634[i], anIntArray1634[i], anIntArray1634[i], anIntArray1668[l9], anIntArray1668[l10], anIntArray1668[l11], anIntArray1669[l9], anIntArray1669[l10], anIntArray1669[l11], anIntArray1670[l9], anIntArray1670[l10], anIntArray1670[l11], anIntArray1640[i]);
				}
			}
			if (l == 4) {
				if (j3 < 0 || j4 < 0 || j5 < 0 || j3 > DrawingArea.scanlineRightLimit || j4 > DrawingArea.scanlineRightLimit || j5 > DrawingArea.scanlineRightLimit || anIntArray1678[3] < 0 || anIntArray1678[3] > DrawingArea.scanlineRightLimit) {
					Texture.aBoolean1462 = true;
				}
				int i8;
				if (anIntArray1637 == null) {
					i8 = 0;
				} else {
					i8 = anIntArray1637[i] & 3;
				}
				if (i8 == 0) {
					Texture.method374(i7, j7, k7, j3, j4, j5, anIntArray1680[0], anIntArray1680[1], anIntArray1680[2]);
					Texture.method374(i7, k7, anIntArray1679[3], j3, j5, anIntArray1678[3], anIntArray1680[0], anIntArray1680[2], anIntArray1680[3]);
					return;
				}
				if (i8 == 1) {
					int l8 = modelIntArray3[anIntArray1634[i]];
					Texture.method376(i7, j7, k7, j3, j4, j5, l8);
					Texture.method376(i7, k7, anIntArray1679[3], j3, j5, anIntArray1678[3], l8);
					return;
				}
				if (i8 == 2) {
					int i9 = anIntArray1637[i] >> 2;
					int i10 = anIntArray1643[i9];
					int i11 = anIntArray1644[i9];
					int i12 = anIntArray1645[i9];
					Texture.method378(i7, j7, k7, j3, j4, j5, anIntArray1680[0], anIntArray1680[1], anIntArray1680[2], anIntArray1668[i10], anIntArray1668[i11], anIntArray1668[i12], anIntArray1669[i10], anIntArray1669[i11], anIntArray1669[i12], anIntArray1670[i10], anIntArray1670[i11], anIntArray1670[i12], anIntArray1640[i]);
					Texture.method378(i7, k7, anIntArray1679[3], j3, j5, anIntArray1678[3], anIntArray1680[0], anIntArray1680[2], anIntArray1680[3], anIntArray1668[i10], anIntArray1668[i11], anIntArray1668[i12], anIntArray1669[i10], anIntArray1669[i11], anIntArray1669[i12], anIntArray1670[i10], anIntArray1670[i11], anIntArray1670[i12], anIntArray1640[i]);
					return;
				}
				if (i8 == 3) {
					int j9 = anIntArray1637[i] >> 2;
					int j10 = anIntArray1643[j9];
					int j11 = anIntArray1644[j9];
					int j12 = anIntArray1645[j9];
					Texture.method378(i7, j7, k7, j3, j4, j5, anIntArray1634[i], anIntArray1634[i], anIntArray1634[i], anIntArray1668[j10], anIntArray1668[j11], anIntArray1668[j12], anIntArray1669[j10], anIntArray1669[j11], anIntArray1669[j12], anIntArray1670[j10], anIntArray1670[j11], anIntArray1670[j12], anIntArray1640[i]);
					Texture.method378(i7, k7, anIntArray1679[3], j3, j5, anIntArray1678[3], anIntArray1634[i], anIntArray1634[i], anIntArray1634[i], anIntArray1668[j10], anIntArray1668[j11], anIntArray1668[j12], anIntArray1669[j10], anIntArray1669[j11], anIntArray1669[j12], anIntArray1670[j10], anIntArray1670[j11], anIntArray1670[j12], anIntArray1640[i]);
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
	private static Class21[] aClass21Array1661;
	private static OnDemandFetcherParent aOnDemandFetcherParent_1662;
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
	private static final int[] anIntArray1678 = new int[10];
	private static final int[] anIntArray1679 = new int[10];
	private static final int[] anIntArray1680 = new int[10];
	private static int anInt1681;
	private static int anInt1682;
	private static int anInt1683;
	public static boolean aBoolean1684;
	public static int anInt1685;
	public static int anInt1686;
	public static int anInt1687;
	public static final int[] anIntArray1688 = new int[1000];
	public static int modelIntArray1[];
	public static int modelIntArray2[];
	private static int[] modelIntArray3;
	private static int[] modelIntArray4;

	static {
		modelIntArray1 = Texture.anIntArray1470;
		modelIntArray2 = Texture.anIntArray1471;
		modelIntArray3 = Texture.anIntArray1482;
		modelIntArray4 = Texture.anIntArray1469;
	}
}
