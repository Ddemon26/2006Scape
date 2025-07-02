// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

public final class Model extends Animable {

    public static void clearCache() {
                modelHeaders = null;
                visibilityMap1 = null;
                visibilityMap2 = null;
                projectedVertexX = null;
		projectedVertexY = null;
		projectedX = null;
		projectedY = null;
		projectedZ = null;
		depthList = null;
		vertexQueue = null;
		vertexGroups2D = null;
		cameraSine = null;
		faceLists = null;
		stackX = null;
		stackY = null;
		facePriority = null;
		modelIntArray1 = null;
		modelIntArray2 = null;
		modelIntArray3 = null;
		modelIntArray4 = null;
	}

	public static void init(int i, OnDemandFetcherParent onDemandFetcherParent) {
		modelHeaders = new ModelHeader[i];
		modelFetcher = onDemandFetcherParent;
	}

	public static void loadModelData(byte abyte0[], int j) {
                if (abyte0 == null) {
                        ModelHeader header = modelHeaders[j] = new ModelHeader();
                        header.vertexCount = 0;
                        header.faceCount = 0;
                        header.texturedTriangleCount = 0;
                        return;
                }
                Stream stream = new Stream(abyte0);
                stream.currentOffset = abyte0.length - 18;
                ModelHeader header1 = modelHeaders[j] = new ModelHeader();
                header1.data = abyte0;
                header1.vertexCount = stream.readUnsignedWord();
                header1.faceCount = stream.readUnsignedWord();
                header1.texturedTriangleCount = stream.readUnsignedByte();
		int k = stream.readUnsignedByte();
		int l = stream.readUnsignedByte();
		int i1 = stream.readUnsignedByte();
		int j1 = stream.readUnsignedByte();
		int k1 = stream.readUnsignedByte();
		int l1 = stream.readUnsignedWord();
		int i2 = stream.readUnsignedWord();
		int j2 = stream.readUnsignedWord();
		int k2 = stream.readUnsignedWord();
		int l2 = 0;
                header1.vertexFlagsOffset = l2;
                l2 += header1.vertexCount;
                header1.facePriorityOffset = l2;
                l2 += header1.faceCount;
                header1.faceAlphaOffset = l2;
                if (l == 255) {
                        l2 += header1.faceCount;
                } else {
                        header1.faceAlphaOffset = -l - 1;
                }
                header1.vertexLabelOffset = l2;
                if (j1 == 1) {
                        l2 += header1.faceCount;
                } else {
                        header1.vertexLabelOffset = -1;
                }
                header1.faceLabelOffset = l2;
                if (k == 1) {
                        l2 += header1.faceCount;
                } else {
                        header1.faceLabelOffset = -1;
                }
                header1.vertexSkinsOffset = l2;
                if (k1 == 1) {
                        l2 += header1.vertexCount;
                } else {
                        header1.vertexSkinsOffset = -1;
                }
                header1.faceTextureOffset = l2;
                if (i1 == 1) {
                        l2 += header1.faceCount;
                } else {
                        header1.faceTextureOffset = -1;
                }
                header1.faceTypeOffset = l2;
                l2 += k2;
                header1.faceSkinOffset = l2;
                l2 += header1.faceCount * 2;
                header1.faceIndicesOffset = l2;
                l2 += header1.texturedTriangleCount * 6;
                header1.vertexXOffset = l2;
                l2 += l1;
                header1.vertexYOffset = l2;
                l2 += i2;
                header1.vertexZOffset = l2;
                l2 += j2;
	}

	public static void unload(int j) {
		modelHeaders[j] = null;
	}

	public static Model create(int j) {
		if (modelHeaders == null) {
			return null;
		}
		ModelHeader header = modelHeaders.length < j  ? null : modelHeaders[j];
		if (header == null) {
                        modelFetcher.requestModel(j);
			return null;
		} else {
			return new Model(j);
		}
	}

	public static boolean isLoaded(int i) {
		if (modelHeaders == null) {
			return false;
		}
		ModelHeader header = modelHeaders[i];
		if (header == null) {
                        modelFetcher.requestModel(i);
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
		ModelHeader header = modelHeaders[i];
                anInt1626 = header.vertexCount;
                anInt1630 = header.faceCount;
                anInt1642 = header.texturedTriangleCount;
		vertexX = new int[anInt1626];
		vertexY = new int[anInt1626];
		vertexZ = new int[anInt1626];
		faceA = new int[anInt1630];
		faceB = new int[anInt1630];
		faceC = new int[anInt1630];
		anIntArray1643 = new int[anInt1642];
		anIntArray1644 = new int[anInt1642];
		anIntArray1645 = new int[anInt1642];
                if (header.vertexSkinsOffset >= 0) {
                        vertexSkins = new int[anInt1626];
                }
                if (header.faceLabelOffset >= 0) {
                        anIntArray1637 = new int[anInt1630];
                }
                if (header.faceAlphaOffset >= 0) {
                        anIntArray1638 = new int[anInt1630];
                } else {
                        anInt1641 = -header.faceAlphaOffset - 1;
                }
                if (header.faceTextureOffset >= 0) {
                        anIntArray1639 = new int[anInt1630];
                }
                if (header.vertexLabelOffset >= 0) {
                        faceSkins = new int[anInt1630];
                }
                faceColor = new int[anInt1630];
                Stream stream = new Stream(header.data);
                stream.currentOffset = header.vertexFlagsOffset;
                Stream stream_1 = new Stream(header.data);
                stream_1.currentOffset = header.vertexXOffset;
                Stream stream_2 = new Stream(header.data);
                stream_2.currentOffset = header.vertexYOffset;
                Stream stream_3 = new Stream(header.data);
                stream_3.currentOffset = header.vertexZOffset;
                Stream stream_4 = new Stream(header.data);
                stream_4.currentOffset = header.vertexSkinsOffset;
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
			vertexX[j1] = k + i2;
			vertexY[j1] = l + k2;
			vertexZ[j1] = i1 + i3;
			k = vertexX[j1];
			l = vertexY[j1];
			i1 = vertexZ[j1];
			if (vertexSkins != null) {
				vertexSkins[j1] = stream_4.readUnsignedByte();
			}
		}

                stream.currentOffset = header.faceSkinOffset;
                stream_1.currentOffset = header.faceLabelOffset;
                stream_2.currentOffset = header.faceAlphaOffset;
                stream_3.currentOffset = header.faceTextureOffset;
                stream_4.currentOffset = header.vertexLabelOffset;
		for (int l1 = 0; l1 < anInt1630; l1++) {
			faceColor[l1] = stream.readUnsignedWord();
			if (anIntArray1637 != null) {
				anIntArray1637[l1] = stream_1.readUnsignedByte();
			}
			if (anIntArray1638 != null) {
				anIntArray1638[l1] = stream_2.readUnsignedByte();
			}
			if (anIntArray1639 != null) {
				anIntArray1639[l1] = stream_3.readUnsignedByte();
			}
			if (faceSkins != null) {
				faceSkins[l1] = stream_4.readUnsignedByte();
			}
		}

                stream.currentOffset = header.faceTypeOffset;
                stream_1.currentOffset = header.facePriorityOffset;
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
				faceA[l3] = j2;
				faceB[l3] = l2;
				faceC[l3] = j3;
			}
			if (i4 == 2) {
				l2 = j3;
				j3 = stream.readSignedSmart() + k3;
				k3 = j3;
				faceA[l3] = j2;
				faceB[l3] = l2;
				faceC[l3] = j3;
			}
			if (i4 == 3) {
				j2 = j3;
				j3 = stream.readSignedSmart() + k3;
				k3 = j3;
				faceA[l3] = j2;
				faceB[l3] = l2;
				faceC[l3] = j3;
			}
			if (i4 == 4) {
				int k4 = j2;
				j2 = l2;
				l2 = k4;
				j3 = stream.readSignedSmart() + k3;
				k3 = j3;
				faceA[l3] = j2;
				faceB[l3] = l2;
				faceC[l3] = j3;
			}
		}

                stream.currentOffset = header.faceIndicesOffset;
		for (int j4 = 0; j4 < anInt1642; j4++) {
			anIntArray1643[j4] = stream.readUnsignedWord();
			anIntArray1644[j4] = stream.readUnsignedWord();
			anIntArray1645[j4] = stream.readUnsignedWord();
		}

	}

	public Model(int i, Model aclass30_sub2_sub4_sub6s[]) {
		aBoolean1659 = false;
		boolean flag = false;
		boolean flag1 = false;
		boolean flag2 = false;
		boolean flag3 = false;
		anInt1626 = 0;
		anInt1630 = 0;
		anInt1642 = 0;
		anInt1641 = -1;
		for (int k = 0; k < i; k++) {
			Model model = aclass30_sub2_sub4_sub6s[k];
			if (model != null) {
				anInt1626 += model.anInt1626;
				anInt1630 += model.anInt1630;
				anInt1642 += model.anInt1642;
				flag |= model.anIntArray1637 != null;
				if (model.anIntArray1638 != null) {
					flag1 = true;
				} else {
					if (anInt1641 == -1) {
						anInt1641 = model.anInt1641;
					}
					if (anInt1641 != model.anInt1641) {
						flag1 = true;
					}
				}
				flag2 |= model.anIntArray1639 != null;
				flag3 |= model.faceSkins != null;
			}
		}

		vertexX = new int[anInt1626];
		vertexY = new int[anInt1626];
		vertexZ = new int[anInt1626];
		vertexSkins = new int[anInt1626];
		faceA = new int[anInt1630];
		faceB = new int[anInt1630];
		faceC = new int[anInt1630];
		anIntArray1643 = new int[anInt1642];
		anIntArray1644 = new int[anInt1642];
		anIntArray1645 = new int[anInt1642];
		if (flag) {
			anIntArray1637 = new int[anInt1630];
		}
		if (flag1) {
			anIntArray1638 = new int[anInt1630];
		}
		if (flag2) {
			anIntArray1639 = new int[anInt1630];
		}
		if (flag3) {
			faceSkins = new int[anInt1630];
		}
		faceColor = new int[anInt1630];
		anInt1626 = 0;
		anInt1630 = 0;
		anInt1642 = 0;
		int l = 0;
		for (int i1 = 0; i1 < i; i1++) {
			Model model_1 = aclass30_sub2_sub4_sub6s[i1];
			if (model_1 != null) {
				for (int j1 = 0; j1 < model_1.anInt1630; j1++) {
					if (flag) {
						if (model_1.anIntArray1637 == null) {
							anIntArray1637[anInt1630] = 0;
						} else {
							int k1 = model_1.anIntArray1637[j1];
							if ((k1 & 2) == 2) {
								k1 += l << 2;
							}
							anIntArray1637[anInt1630] = k1;
						}
					}
					if (flag1) {
						if (model_1.anIntArray1638 == null) {
							anIntArray1638[anInt1630] = model_1.anInt1641;
						} else {
							anIntArray1638[anInt1630] = model_1.anIntArray1638[j1];
						}
					}
					if (flag2) {
						if (model_1.anIntArray1639 == null) {
							anIntArray1639[anInt1630] = 0;
						} else {
							anIntArray1639[anInt1630] = model_1.anIntArray1639[j1];
						}
					}
					if (flag3 && model_1.faceSkins != null) {
						faceSkins[anInt1630] = model_1.faceSkins[j1];
					}
					faceColor[anInt1630] = model_1.faceColor[j1];
					faceA[anInt1630] = getOrCreateVertex(model_1, model_1.faceA[j1]);
					faceB[anInt1630] = getOrCreateVertex(model_1, model_1.faceB[j1]);
					faceC[anInt1630] = getOrCreateVertex(model_1, model_1.faceC[j1]);
					anInt1630++;
				}

				for (int l1 = 0; l1 < model_1.anInt1642; l1++) {
					anIntArray1643[anInt1642] = getOrCreateVertex(model_1, model_1.anIntArray1643[l1]);
					anIntArray1644[anInt1642] = getOrCreateVertex(model_1, model_1.anIntArray1644[l1]);
					anIntArray1645[anInt1642] = getOrCreateVertex(model_1, model_1.anIntArray1645[l1]);
					anInt1642++;
				}

				l += model_1.anInt1642;
			}
		}

	}

	public Model(Model aclass30_sub2_sub4_sub6s[]) {
		int i = 2;// was parameter
		aBoolean1659 = false;
		boolean flag1 = false;
		boolean flag2 = false;
		boolean flag3 = false;
		boolean flag4 = false;
		anInt1626 = 0;
		anInt1630 = 0;
		anInt1642 = 0;
		anInt1641 = -1;
		for (int k = 0; k < i; k++) {
			Model model = aclass30_sub2_sub4_sub6s[k];
			if (model != null) {
				anInt1626 += model.anInt1626;
				anInt1630 += model.anInt1630;
				anInt1642 += model.anInt1642;
				flag1 |= model.anIntArray1637 != null;
				if (model.anIntArray1638 != null) {
					flag2 = true;
				} else {
					if (anInt1641 == -1) {
						anInt1641 = model.anInt1641;
					}
					if (anInt1641 != model.anInt1641) {
						flag2 = true;
					}
				}
				flag3 |= model.anIntArray1639 != null;
				flag4 |= model.faceColor != null;
			}
		}

		vertexX = new int[anInt1626];
		vertexY = new int[anInt1626];
		vertexZ = new int[anInt1626];
		faceA = new int[anInt1630];
		faceB = new int[anInt1630];
		faceC = new int[anInt1630];
		anIntArray1634 = new int[anInt1630];
		anIntArray1635 = new int[anInt1630];
		anIntArray1636 = new int[anInt1630];
		anIntArray1643 = new int[anInt1642];
		anIntArray1644 = new int[anInt1642];
		anIntArray1645 = new int[anInt1642];
		if (flag1) {
			anIntArray1637 = new int[anInt1630];
		}
		if (flag2) {
			anIntArray1638 = new int[anInt1630];
		}
		if (flag3) {
			anIntArray1639 = new int[anInt1630];
		}
		if (flag4) {
			faceColor = new int[anInt1630];
		}
		anInt1626 = 0;
		anInt1630 = 0;
		anInt1642 = 0;
		int i1 = 0;
		for (int j1 = 0; j1 < i; j1++) {
			Model model_1 = aclass30_sub2_sub4_sub6s[j1];
			if (model_1 != null) {
				int k1 = anInt1626;
				for (int l1 = 0; l1 < model_1.anInt1626; l1++) {
					vertexX[anInt1626] = model_1.vertexX[l1];
					vertexY[anInt1626] = model_1.vertexY[l1];
					vertexZ[anInt1626] = model_1.vertexZ[l1];
					anInt1626++;
				}

				for (int i2 = 0; i2 < model_1.anInt1630; i2++) {
					faceA[anInt1630] = model_1.faceA[i2] + k1;
					faceB[anInt1630] = model_1.faceB[i2] + k1;
					faceC[anInt1630] = model_1.faceC[i2] + k1;
					anIntArray1634[anInt1630] = model_1.anIntArray1634[i2];
					anIntArray1635[anInt1630] = model_1.anIntArray1635[i2];
					anIntArray1636[anInt1630] = model_1.anIntArray1636[i2];
					if (flag1) {
						if (model_1.anIntArray1637 == null) {
							anIntArray1637[anInt1630] = 0;
						} else {
							int j2 = model_1.anIntArray1637[i2];
							if ((j2 & 2) == 2) {
								j2 += i1 << 2;
							}
							anIntArray1637[anInt1630] = j2;
						}
					}
					if (flag2) {
						if (model_1.anIntArray1638 == null) {
							anIntArray1638[anInt1630] = model_1.anInt1641;
						} else {
							anIntArray1638[anInt1630] = model_1.anIntArray1638[i2];
						}
					}
					if (flag3) {
						if (model_1.anIntArray1639 == null) {
							anIntArray1639[anInt1630] = 0;
						} else {
							anIntArray1639[anInt1630] = model_1.anIntArray1639[i2];
						}
					}
					if (flag4 && model_1.faceColor != null) {
						faceColor[anInt1630] = model_1.faceColor[i2];
					}
					anInt1630++;
				}

				for (int k2 = 0; k2 < model_1.anInt1642; k2++) {
					anIntArray1643[anInt1642] = model_1.anIntArray1643[k2] + k1;
					anIntArray1644[anInt1642] = model_1.anIntArray1644[k2] + k1;
					anIntArray1645[anInt1642] = model_1.anIntArray1645[k2] + k1;
					anInt1642++;
				}

				i1 += model_1.anInt1642;
			}
		}

		calculateBounds();
	}

	public Model(boolean flag, boolean flag1, boolean flag2, Model model) {
		aBoolean1659 = false;
		anInt1626 = model.anInt1626;
		anInt1630 = model.anInt1630;
		anInt1642 = model.anInt1642;
		if (flag2) {
			vertexX = model.vertexX;
			vertexY = model.vertexY;
			vertexZ = model.vertexZ;
		} else {
			vertexX = new int[anInt1626];
			vertexY = new int[anInt1626];
			vertexZ = new int[anInt1626];
			for (int j = 0; j < anInt1626; j++) {
				vertexX[j] = model.vertexX[j];
				vertexY[j] = model.vertexY[j];
				vertexZ[j] = model.vertexZ[j];
			}

		}
		if (flag) {
			faceColor = model.faceColor;
		} else {
			faceColor = new int[anInt1630];
			System.arraycopy(model.faceColor, 0, faceColor, 0, anInt1630);

		}
		if (flag1) {
			anIntArray1639 = model.anIntArray1639;
		} else {
			anIntArray1639 = new int[anInt1630];
			if (model.anIntArray1639 == null) {
				for (int l = 0; l < anInt1630; l++) {
					anIntArray1639[l] = 0;
				}

			} else {
				System.arraycopy(model.anIntArray1639, 0, anIntArray1639, 0, anInt1630);

			}
		}
		vertexSkins = model.vertexSkins;
		faceSkins = model.faceSkins;
		anIntArray1637 = model.anIntArray1637;
		faceA = model.faceA;
		faceB = model.faceB;
		faceC = model.faceC;
		anIntArray1638 = model.anIntArray1638;
		anInt1641 = model.anInt1641;
		anIntArray1643 = model.anIntArray1643;
		anIntArray1644 = model.anIntArray1644;
		anIntArray1645 = model.anIntArray1645;
	}

	public Model(boolean flag, boolean flag1, Model model) {
		aBoolean1659 = false;
		anInt1626 = model.anInt1626;
		anInt1630 = model.anInt1630;
		anInt1642 = model.anInt1642;
		if (flag) {
			vertexY = new int[anInt1626];
			System.arraycopy(model.vertexY, 0, vertexY, 0, anInt1626);

		} else {
			vertexY = model.vertexY;
		}
		if (flag1) {
			anIntArray1634 = new int[anInt1630];
			anIntArray1635 = new int[anInt1630];
			anIntArray1636 = new int[anInt1630];
			for (int k = 0; k < anInt1630; k++) {
				anIntArray1634[k] = model.anIntArray1634[k];
				anIntArray1635[k] = model.anIntArray1635[k];
				anIntArray1636[k] = model.anIntArray1636[k];
			}

			anIntArray1637 = new int[anInt1630];
			if (model.anIntArray1637 == null) {
				for (int l = 0; l < anInt1630; l++) {
					anIntArray1637[l] = 0;
				}

			} else {
				System.arraycopy(model.anIntArray1637, 0, anIntArray1637, 0, anInt1630);

			}
                       super.vertexNormals = new VertexNormal[anInt1626];
			for (int j1 = 0; j1 < anInt1626; j1++) {
                               VertexNormal class33 = super.vertexNormals[j1] = new VertexNormal();
                               VertexNormal class33_1 = model.vertexNormals[j1];
				class33.x = class33_1.x;
				class33.y = class33_1.y;
				class33.z = class33_1.z;
				class33.magnitude = class33_1.magnitude;
			}

			aVertexNormalArray1660 = model.aVertexNormalArray1660;
		} else {
			anIntArray1634 = model.anIntArray1634;
			anIntArray1635 = model.anIntArray1635;
			anIntArray1636 = model.anIntArray1636;
			anIntArray1637 = model.anIntArray1637;
		}
		vertexX = model.vertexX;
		vertexZ = model.vertexZ;
		faceColor = model.faceColor;
		anIntArray1639 = model.anIntArray1639;
		anIntArray1638 = model.anIntArray1638;
		anInt1641 = model.anInt1641;
		faceA = model.faceA;
		faceB = model.faceB;
		faceC = model.faceC;
		anIntArray1643 = model.anIntArray1643;
		anIntArray1644 = model.anIntArray1644;
		anIntArray1645 = model.anIntArray1645;
		super.modelHeight = model.modelHeight;
		maxY = model.maxY;
		boundingRadius = model.boundingRadius;
		diagonal2D = model.diagonal2D;
		diagonal3D = model.diagonal3D;
		minX = model.minX;
		maxZ = model.maxZ;
		minZ = model.minZ;
		maxX = model.maxX;
	}

        public void copyFromModel(Model src, boolean shareColor) {
                anInt1626 = src.anInt1626;
                anInt1630 = src.anInt1630;
                anInt1642 = src.anInt1642;
                if (anIntArray1622.length < anInt1626) {
                        anIntArray1622 = new int[anInt1626 + 100];
                        anIntArray1623 = new int[anInt1626 + 100];
                        anIntArray1624 = new int[anInt1626 + 100];
                }
                vertexX = anIntArray1622;
                vertexY = anIntArray1623;
                vertexZ = anIntArray1624;
                for (int k = 0; k < anInt1626; k++) {
                        vertexX[k] = src.vertexX[k];
                        vertexY[k] = src.vertexY[k];
                        vertexZ[k] = src.vertexZ[k];
                }

                if (shareColor) {
                        anIntArray1639 = src.anIntArray1639;
                } else {
                        if (anIntArray1625.length < anInt1630) {
                                anIntArray1625 = new int[anInt1630 + 100];
                        }
                        anIntArray1639 = anIntArray1625;
                        if (src.anIntArray1639 == null) {
                                for (int l = 0; l < anInt1630; l++) {
                                        anIntArray1639[l] = 0;
                                }

                        } else {
                                System.arraycopy(src.anIntArray1639, 0, anIntArray1639, 0, anInt1630);

                        }
                }
                anIntArray1637 = src.anIntArray1637;
                faceColor = src.faceColor;
                anIntArray1638 = src.anIntArray1638;
                anInt1641 = src.anInt1641;
                faceGroups = src.faceGroups;
                vertexGroups = src.vertexGroups;
                faceA = src.faceA;
                faceB = src.faceB;
                faceC = src.faceC;
                anIntArray1634 = src.anIntArray1634;
                anIntArray1635 = src.anIntArray1635;
                anIntArray1636 = src.anIntArray1636;
                anIntArray1643 = src.anIntArray1643;
                anIntArray1644 = src.anIntArray1644;
                anIntArray1645 = src.anIntArray1645;
        }

	private int getOrCreateVertex(Model model, int i) {
		int j = -1;
		int k = model.vertexX[i];
		int l = model.vertexY[i];
		int i1 = model.vertexZ[i];
		for (int j1 = 0; j1 < anInt1626; j1++) {
			if (k != vertexX[j1] || l != vertexY[j1] || i1 != vertexZ[j1]) {
				continue;
			}
			j = j1;
			break;
		}

		if (j == -1) {
			vertexX[anInt1626] = k;
			vertexY[anInt1626] = l;
			vertexZ[anInt1626] = i1;
			if (model.vertexSkins != null) {
				vertexSkins[anInt1626] = model.vertexSkins[i];
			}
			j = anInt1626++;
		}
		return j;
	}

	public void calculateBounds() {
		super.modelHeight = 0;
		boundingRadius = 0;
		maxY = 0;
		for (int i = 0; i < anInt1626; i++) {
			int j = vertexX[i];
			int k = vertexY[i];
			int l = vertexZ[i];
			if (-k > super.modelHeight) {
				super.modelHeight = -k;
			}
			if (k > maxY) {
				maxY = k;
			}
			int i1 = j * j + l * l;
			if (i1 > boundingRadius) {
				boundingRadius = i1;
			}
		}
		boundingRadius = (int) (Math.sqrt(boundingRadius) + 0.98999999999999999D);
		diagonal2D = (int) (Math.sqrt(boundingRadius * boundingRadius + super.modelHeight * super.modelHeight) + 0.98999999999999999D);
		diagonal3D = diagonal2D + (int) (Math.sqrt(boundingRadius * boundingRadius + maxY * maxY) + 0.98999999999999999D);
	}

	public void calculateBoundsY() {
		super.modelHeight = 0;
		maxY = 0;
		for (int i = 0; i < anInt1626; i++) {
			int j = vertexY[i];
			if (-j > super.modelHeight) {
				super.modelHeight = -j;
			}
			if (j > maxY) {
				maxY = j;
			}
		}

		diagonal2D = (int) (Math.sqrt(boundingRadius * boundingRadius + super.modelHeight * super.modelHeight) + 0.98999999999999999D);
		diagonal3D = diagonal2D + (int) (Math.sqrt(boundingRadius * boundingRadius + maxY * maxY) + 0.98999999999999999D);
	}

	private void calculateExtremes() {
		super.modelHeight = 0;
		boundingRadius = 0;
		maxY = 0;
		minX = 0xf423f;
		maxX = 0xfff0bdc1;
		maxZ = 0xfffe7961;
		minZ = 0x1869f;
		for (int j = 0; j < anInt1626; j++) {
			int k = vertexX[j];
			int l = vertexY[j];
			int i1 = vertexZ[j];
			if (k < minX) {
				minX = k;
			}
			if (k > maxX) {
				maxX = k;
			}
			if (i1 < minZ) {
				minZ = i1;
			}
			if (i1 > maxZ) {
				maxZ = i1;
			}
			if (-l > super.modelHeight) {
				super.modelHeight = -l;
			}
			if (l > maxY) {
				maxY = l;
			}
			int j1 = k * k + i1 * i1;
			if (j1 > boundingRadius) {
				boundingRadius = j1;
			}
		}

		boundingRadius = (int) Math.sqrt(boundingRadius);
		diagonal2D = (int) Math.sqrt(boundingRadius * boundingRadius + super.modelHeight * super.modelHeight);
		diagonal3D = diagonal2D + (int) Math.sqrt(boundingRadius * boundingRadius + maxY * maxY);
	}

	public void buildVertexGroups() {
		if (vertexSkins != null) {
			int ai[] = new int[256];
			int j = 0;
			for (int l = 0; l < anInt1626; l++) {
				int j1 = vertexSkins[l];
				ai[j1]++;
				if (j1 > j) {
					j = j1;
				}
			}

			vertexGroups = new int[j + 1][];
			for (int k1 = 0; k1 <= j; k1++) {
				vertexGroups[k1] = new int[ai[k1]];
				ai[k1] = 0;
			}

			for (int j2 = 0; j2 < anInt1626; j2++) {
				int l2 = vertexSkins[j2];
				vertexGroups[l2][ai[l2]++] = j2;
			}

			vertexSkins = null;
		}
		if (faceSkins != null) {
			int ai1[] = new int[256];
			int k = 0;
			for (int i1 = 0; i1 < anInt1630; i1++) {
				int l1 = faceSkins[i1];
				ai1[l1]++;
				if (l1 > k) {
					k = l1;
				}
			}

			faceGroups = new int[k + 1][];
			for (int i2 = 0; i2 <= k; i2++) {
				faceGroups[i2] = new int[ai1[i2]];
				ai1[i2] = 0;
			}

			for (int k2 = 0; k2 < anInt1630; k2++) {
				int i3 = faceSkins[k2];
				faceGroups[i3][ai1[i3]++] = k2;
			}

			faceSkins = null;
		}
	}

	public void applyFrame(int i) {
		if (vertexGroups == null) {
			return;
		}
		if (i == -1) {
			return;
		}
                AnimFrame class36 = AnimFrame.forId(i);
		if (class36 == null) {
			return;
		}
                FrameBase class18 = class36.frameBase;
		anInt1681 = 0;
		anInt1682 = 0;
		anInt1683 = 0;
                for (int k = 0; k < class36.transformationCount; k++) {
                        int l = class36.transformationIndices[k];
                        transformVertices(class18.transformationType[l], class18.transformationList[l], class36.transformX[k], class36.transformY[k], class36.transformZ[k]);
		}

	}

	public void applyFrames(int ai[], int j, int k) {
		if (k == -1) {
			return;
		}
		if (ai == null || j == -1) {
			applyFrame(k);
			return;
		}
                AnimFrame class36 = AnimFrame.forId(k);
		if (class36 == null) {
			return;
		}
                AnimFrame class36_1 = AnimFrame.forId(j);
		if (class36_1 == null) {
			applyFrame(k);
			return;
		}
                FrameBase class18 = class36.frameBase;
		anInt1681 = 0;
		anInt1682 = 0;
		anInt1683 = 0;
		int l = 0;
		int i1 = ai[l++];
                for (int j1 = 0; j1 < class36.transformationCount; j1++) {
                        int k1;
                        for (k1 = class36.transformationIndices[j1]; k1 > i1; i1 = ai[l++]) {
				;
			}
                        if (k1 != i1 || class18.transformationType[k1] == 0) {
                                transformVertices(class18.transformationType[k1], class18.transformationList[k1], class36.transformX[j1], class36.transformY[j1], class36.transformZ[j1]);
			}
		}

		anInt1681 = 0;
		anInt1682 = 0;
		anInt1683 = 0;
		l = 0;
		i1 = ai[l++];
                for (int l1 = 0; l1 < class36_1.transformationCount; l1++) {
                        int i2;
                        for (i2 = class36_1.transformationIndices[l1]; i2 > i1; i1 = ai[l++]) {
                                ;
                        }
                        if (i2 == i1 || class18.transformationType[i2] == 0) {
                                transformVertices(class18.transformationType[i2], class18.transformationList[i2], class36_1.transformX[l1], class36_1.transformY[l1], class36_1.transformZ[l1]);
                        }
                }

	}

	private void transformVertices(int i, int ai[], int j, int k, int l) {
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
						anInt1681 += vertexX[j6];
						anInt1682 += vertexY[j6];
						anInt1683 += vertexZ[j6];
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
						vertexX[j5] += j;
						vertexY[j5] += k;
						vertexZ[j5] += l;
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
						vertexX[k5] -= anInt1681;
						vertexY[k5] -= anInt1682;
						vertexZ[k5] -= anInt1683;
						int k6 = (j & 0xff) * 8;
						int l6 = (k & 0xff) * 8;
						int i7 = (l & 0xff) * 8;
						if (i7 != 0) {
							int j7 = modelIntArray1[i7];
							int i8 = modelIntArray2[i7];
							int l8 = vertexY[k5] * j7 + vertexX[k5] * i8 >> 16;
							vertexY[k5] = vertexY[k5] * i8 - vertexX[k5] * j7 >> 16;
							vertexX[k5] = l8;
						}
						if (k6 != 0) {
							int k7 = modelIntArray1[k6];
							int j8 = modelIntArray2[k6];
							int i9 = vertexY[k5] * j8 - vertexZ[k5] * k7 >> 16;
							vertexZ[k5] = vertexY[k5] * k7 + vertexZ[k5] * j8 >> 16;
							vertexY[k5] = i9;
						}
						if (l6 != 0) {
							int l7 = modelIntArray1[l6];
							int k8 = modelIntArray2[l6];
							int j9 = vertexZ[k5] * l7 + vertexX[k5] * k8 >> 16;
							vertexZ[k5] = vertexZ[k5] * k8 - vertexX[k5] * l7 >> 16;
							vertexX[k5] = j9;
						}
						vertexX[k5] += anInt1681;
						vertexY[k5] += anInt1682;
						vertexZ[k5] += anInt1683;
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
						vertexX[l5] -= anInt1681;
						vertexY[l5] -= anInt1682;
						vertexZ[l5] -= anInt1683;
						vertexX[l5] = vertexX[l5] * j / 128;
						vertexY[l5] = vertexY[l5] * k / 128;
						vertexZ[l5] = vertexZ[l5] * l / 128;
						vertexX[l5] += anInt1681;
						vertexY[l5] += anInt1682;
						vertexZ[l5] += anInt1683;
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

	public void calculateNormals() {
		for (int j = 0; j < anInt1626; j++) {
			int k = vertexX[j];
			vertexX[j] = vertexZ[j];
			vertexZ[j] = -k;
		}

	}

	public void rotateX(int i) {
		int k = modelIntArray1[i];
		int l = modelIntArray2[i];
		for (int i1 = 0; i1 < anInt1626; i1++) {
			int j1 = vertexY[i1] * l - vertexZ[i1] * k >> 16;
			vertexZ[i1] = vertexY[i1] * k + vertexZ[i1] * l >> 16;
			vertexY[i1] = j1;
		}
	}

	public void translate(int i, int j, int l) {
		for (int i1 = 0; i1 < anInt1626; i1++) {
			vertexX[i1] += i;
			vertexY[i1] += j;
			vertexZ[i1] += l;
		}

	}

	public void recolor(int i, int j) {
		for (int k = 0; k < anInt1630; k++) {
			if (faceColor[k] == i) {
				faceColor[k] = j;
			}
		}

	}

	public void mirror() {
		for (int j = 0; j < anInt1626; j++) {
			vertexZ[j] = -vertexZ[j];
		}

		for (int k = 0; k < anInt1630; k++) {
			int l = faceA[k];
			faceA[k] = faceC[k];
			faceC[k] = l;
		}
	}

	public void scaleModel(int i, int j, int l) {
		for (int i1 = 0; i1 < anInt1626; i1++) {
			vertexX[i1] = vertexX[i1] * i / 128;
			vertexY[i1] = vertexY[i1] * l / 128;
			vertexZ[i1] = vertexZ[i1] * j / 128;
		}

	}

	public void applyLighting(int i, int j, int k, int l, int i1, boolean flag) {
		int j1 = (int) Math.sqrt(k * k + l * l + i1 * i1);
		int k1 = j * j1 >> 8;
		if (anIntArray1634 == null) {
			anIntArray1634 = new int[anInt1630];
			anIntArray1635 = new int[anInt1630];
			anIntArray1636 = new int[anInt1630];
		}
               if (super.vertexNormals == null) {
                       super.vertexNormals = new VertexNormal[anInt1626];
                       for (int l1 = 0; l1 < anInt1626; l1++) {
                               super.vertexNormals[l1] = new VertexNormal();
                       }

		}
		for (int i2 = 0; i2 < anInt1630; i2++) {
			int j2 = faceA[i2];
			int l2 = faceB[i2];
			int i3 = faceC[i2];
			int j3 = vertexX[l2] - vertexX[j2];
			int k3 = vertexY[l2] - vertexY[j2];
			int l3 = vertexZ[l2] - vertexZ[j2];
			int i4 = vertexX[i3] - vertexX[j2];
			int j4 = vertexY[i3] - vertexY[j2];
			int k4 = vertexZ[i3] - vertexZ[j2];
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
                               VertexNormal class33_2 = super.vertexNormals[j2];
				class33_2.x += l4;
				class33_2.y += i5;
				class33_2.z += j5;
				class33_2.magnitude++;
                               class33_2 = super.vertexNormals[l2];
				class33_2.x += l4;
				class33_2.y += i5;
				class33_2.z += j5;
				class33_2.magnitude++;
                               class33_2 = super.vertexNormals[i3];
				class33_2.x += l4;
				class33_2.y += i5;
				class33_2.z += j5;
				class33_2.magnitude++;
			} else {
				int l5 = i + (k * l4 + l * i5 + i1 * j5) / (k1 + k1 / 2);
				anIntArray1634[i2] = method481(faceColor[i2], l5, anIntArray1637[i2]);
			}
		}

		if (flag) {
			applyShading(i, k1, k, l, i1);
		} else {
			aVertexNormalArray1660 = new VertexNormal[anInt1626];
			for (int k2 = 0; k2 < anInt1626; k2++) {
                                VertexNormal class33 = super.vertexNormals[k2];
				VertexNormal class33_1 = aVertexNormalArray1660[k2] = new VertexNormal();
				class33_1.x = class33.x;
				class33_1.y = class33.y;
				class33_1.z = class33.z;
				class33_1.magnitude = class33.magnitude;
			}

		}
		if (flag) {
			calculateBounds();
		} else {
			calculateExtremes();
		}
	}

	public void applyShading(int i, int j, int k, int l, int i1) {
		for (int j1 = 0; j1 < anInt1630; j1++) {
			int k1 = faceA[j1];
			int i2 = faceB[j1];
			int j2 = faceC[j1];
			if (anIntArray1637 == null) {
				int i3 = faceColor[j1];
                                VertexNormal class33 = super.vertexNormals[k1];
				int k2 = i + (k * class33.x + l * class33.y + i1 * class33.z) / (j * class33.magnitude);
				anIntArray1634[j1] = method481(i3, k2, 0);
                                class33 = super.vertexNormals[i2];
				k2 = i + (k * class33.x + l * class33.y + i1 * class33.z) / (j * class33.magnitude);
				anIntArray1635[j1] = method481(i3, k2, 0);
                                class33 = super.vertexNormals[j2];
				k2 = i + (k * class33.x + l * class33.y + i1 * class33.z) / (j * class33.magnitude);
				anIntArray1636[j1] = method481(i3, k2, 0);
			} else if ((anIntArray1637[j1] & 1) == 0) {
				int j3 = faceColor[j1];
				int k3 = anIntArray1637[j1];
                                VertexNormal class33_1 = super.vertexNormals[k1];
				int l2 = i + (k * class33_1.x + l * class33_1.y + i1 * class33_1.z) / (j * class33_1.magnitude);
				anIntArray1634[j1] = method481(j3, l2, k3);
                                class33_1 = super.vertexNormals[i2];
				l2 = i + (k * class33_1.x + l * class33_1.y + i1 * class33_1.z) / (j * class33_1.magnitude);
				anIntArray1635[j1] = method481(j3, l2, k3);
                                class33_1 = super.vertexNormals[j2];
				l2 = i + (k * class33_1.x + l * class33_1.y + i1 * class33_1.z) / (j * class33_1.magnitude);
				anIntArray1636[j1] = method481(j3, l2, k3);
			}
		}

                super.vertexNormals = null;
		aVertexNormalArray1660 = null;
		vertexSkins = null;
		faceSkins = null;
		if (anIntArray1637 != null) {
			for (int l1 = 0; l1 < anInt1630; l1++) {
				if ((anIntArray1637[l1] & 2) == 2) {
					return;
				}
			}

		}
		faceColor = null;
	}

	private static int method481(int i, int j, int k) {
		if ((k & 2) == 2) {
			if (j < 0) {
				j = 0;
			} else if (j > 127) {
				j = 127;
			}
			j = 127 - j;
			return j;
		}
		j = j * (i & 0x7f) >> 7;
		if (j < 2) {
			j = 2;
		} else if (j > 126) {
			j = 126;
		}
		return (i & 0xff80) + j;
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
			int l4 = vertexX[k4];
			int i5 = vertexY[k4];
			int j5 = vertexZ[k4];
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
			projectedX[k4] = j5 - j4;
			projectedVertexX[k4] = l1 + (l4 << 9) / j5;
			projectedVertexY[k4] = i2 + (i5 << 9) / j5;
			if (anInt1642 > 0) {
				projectedY[k4] = l4;
				projectedZ[k4] = i5;
				depthList[k4] = j5;
			}
		}

		try {
			method483(false, false, 0);
		} catch (Exception _ex) {
		}
	}

	@Override
	public void render(int i, int j, int k, int l, int i1, int j1, int k1, int l1, int i2) {
		int j2 = l1 * i1 - j1 * l >> 16;
		int k2 = k1 * j + j2 * k >> 16;
		int l2 = boundingRadius * k >> 16;
		int i3 = k2 + l2;
		// Check distance of model to camera for rendering (default 3500)
		int distance = WorldController.drawDistance * 256;
		distance *= 1 + (Game.zoom / 10);
		if (i3 <= 50 || k2 >= distance) {
			return;
		}
		int j3 = l1 * l + j1 * i1 >> 16;
		int k3 = j3 - boundingRadius << 9;
		if (k3 / i3 >= DrawingArea.centerY) {
			return;
		}
		int l3 = j3 + boundingRadius << 9;
		if (l3 / i3 <= -DrawingArea.centerY) {
			return;
		}
		int i4 = k1 * k - j2 * j >> 16;
		int j4 = boundingRadius * j >> 16;
		int k4 = i4 + j4 << 9;
		if (k4 / i3 <= -DrawingArea.anInt1387) {
			return;
		}
		int l4 = j4 + (super.modelHeight * k >> 16);
		int i5 = i4 - l4 << 9;
		if (i5 / i3 >= DrawingArea.anInt1387) {
			return;
		}
		int j5 = l2 + (super.modelHeight * j >> 16);
		boolean flag = false;
		if (k2 - j5 <= 50) {
			flag = true;
		}
		boolean flag1 = false;
		if (i2 > 0 && withinViewport) {
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
			int k7 = vertexX[j7];
			int l7 = vertexY[j7];
			int i8 = vertexZ[j7];
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
			projectedX[j7] = i8 - k2;
			if (i8 >= 50) {
				projectedVertexX[j7] = l5 + (k7 << 9) / i8;
				projectedVertexY[j7] = j6 + (l7 << 9) / i8;
			} else {
				projectedVertexX[j7] = -5000;
				flag = true;
			}
			if (flag || anInt1642 > 0) {
				projectedY[j7] = k7;
				projectedZ[j7] = l7;
				depthList[j7] = i8;
			}
		}

		try {
			method483(flag, flag1, i2);
		} catch (Exception _ex) {
		}
	}

	private void method483(boolean flag, boolean flag1, int i) {
		for (int j = 0; j < diagonal3D; j++) {
			vertexQueue[j] = 0;
		}

		for (int k = 0; k < anInt1630; k++) {
			if (anIntArray1637 == null || anIntArray1637[k] != -1) {
				int l = faceA[k];
				int k1 = faceB[k];
				int j2 = faceC[k];
				int i3 = projectedVertexX[l];
				int l3 = projectedVertexX[k1];
				int k4 = projectedVertexX[j2];
				if (flag && (i3 == -5000 || l3 == -5000 || k4 == -5000)) {
					visibilityMap2[k] = true;
					int j5 = (projectedX[l] + projectedX[k1] + projectedX[j2]) / 3 + diagonal2D;
					vertexGroups2D[j5][vertexQueue[j5]++] = k;
				} else {
					if (flag1 && method486(anInt1685, anInt1686, projectedVertexY[l], projectedVertexY[k1], projectedVertexY[j2], i3, l3, k4)) {
						anIntArray1688[anInt1687++] = i;
						flag1 = false;
					}
					if ((i3 - l3) * (projectedVertexY[j2] - projectedVertexY[k1]) - (projectedVertexY[l] - projectedVertexY[k1]) * (k4 - l3) > 0) {
						visibilityMap2[k] = false;
						visibilityMap1[k] = i3 < 0 || l3 < 0 || k4 < 0 || i3 > DrawingArea.centerX || l3 > DrawingArea.centerX || k4 > DrawingArea.centerX;
						int k5 = (projectedX[l] + projectedX[k1] + projectedX[j2]) / 3 + diagonal2D;
						vertexGroups2D[k5][vertexQueue[k5]++] = k;
					}
				}
			}
		}

		if (anIntArray1638 == null) {
			for (int i1 = diagonal3D - 1; i1 >= 0; i1--) {
				int l1 = vertexQueue[i1];
				if (l1 > 0) {
					int ai[] = vertexGroups2D[i1];
					for (int j3 = 0; j3 < l1; j3++) {
						method484(ai[j3]);
					}

				}
			}

			return;
		}
		for (int j1 = 0; j1 < 12; j1++) {
			cameraSine[j1] = 0;
			facePriority[j1] = 0;
		}

		for (int i2 = diagonal3D - 1; i2 >= 0; i2--) {
			int k2 = vertexQueue[i2];
			if (k2 > 0) {
				int ai1[] = vertexGroups2D[i2];
				for (int i4 = 0; i4 < k2; i4++) {
					int l4 = ai1[i4];
					int l5 = anIntArray1638[l4];
					int j6 = cameraSine[l5]++;
					faceLists[l5][j6] = l4;
					if (l5 < 10) {
						facePriority[l5] += i2;
					} else if (l5 == 10) {
						stackX[j6] = i2;
					} else {
						stackY[j6] = i2;
					}
				}

			}
		}

		int l2 = 0;
		if (cameraSine[1] > 0 || cameraSine[2] > 0) {
			l2 = (facePriority[1] + facePriority[2]) / (cameraSine[1] + cameraSine[2]);
		}
		int k3 = 0;
		if (cameraSine[3] > 0 || cameraSine[4] > 0) {
			k3 = (facePriority[3] + facePriority[4]) / (cameraSine[3] + cameraSine[4]);
		}
		int j4 = 0;
		if (cameraSine[6] > 0 || cameraSine[8] > 0) {
			j4 = (facePriority[6] + facePriority[8]) / (cameraSine[6] + cameraSine[8]);
		}
		int i6 = 0;
		int k6 = cameraSine[10];
		int ai2[] = faceLists[10];
		int ai3[] = stackX;
		if (i6 == k6) {
			i6 = 0;
			k6 = cameraSine[11];
			ai2 = faceLists[11];
			ai3 = stackY;
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
				if (i6 == k6 && ai2 != faceLists[11]) {
					i6 = 0;
					k6 = cameraSine[11];
					ai2 = faceLists[11];
					ai3 = stackY;
				}
				if (i6 < k6) {
					i5 = ai3[i6];
				} else {
					i5 = -1000;
				}
			}
			while (l6 == 3 && i5 > k3) {
				method484(ai2[i6++]);
				if (i6 == k6 && ai2 != faceLists[11]) {
					i6 = 0;
					k6 = cameraSine[11];
					ai2 = faceLists[11];
					ai3 = stackY;
				}
				if (i6 < k6) {
					i5 = ai3[i6];
				} else {
					i5 = -1000;
				}
			}
			while (l6 == 5 && i5 > j4) {
				method484(ai2[i6++]);
				if (i6 == k6 && ai2 != faceLists[11]) {
					i6 = 0;
					k6 = cameraSine[11];
					ai2 = faceLists[11];
					ai3 = stackY;
				}
				if (i6 < k6) {
					i5 = ai3[i6];
				} else {
					i5 = -1000;
				}
			}
			int i7 = cameraSine[l6];
			int ai4[] = faceLists[l6];
			for (int j7 = 0; j7 < i7; j7++) {
				method484(ai4[j7]);
			}

		}

		while (i5 != -1000) {
			method484(ai2[i6++]);
			if (i6 == k6 && ai2 != faceLists[11]) {
				i6 = 0;
				ai2 = faceLists[11];
				k6 = cameraSine[11];
				ai3 = stackY;
			}
			if (i6 < k6) {
				i5 = ai3[i6];
			} else {
				i5 = -1000;
			}
		}
	}

	private void method484(int i) {
		if (visibilityMap2[i]) {
			method485(i);
			return;
		}
		int j = faceA[i];
		int k = faceB[i];
		int l = faceC[i];
		Texture.clip = visibilityMap1[i];
		if (anIntArray1639 == null) {
			Texture.alpha = 0;
		} else {
			Texture.alpha = anIntArray1639[i];
		}
		int i1;
		if (anIntArray1637 == null) {
			i1 = 0;
		} else {
			i1 = anIntArray1637[i] & 3;
		}
		if (i1 == 0) {
			Texture.drawGouraudTriangle(projectedVertexY[j], projectedVertexY[k], projectedVertexY[l], projectedVertexX[j], projectedVertexX[k], projectedVertexX[l], anIntArray1634[i], anIntArray1635[i], anIntArray1636[i]);
			return;
		}
		if (i1 == 1) {
			Texture.drawFlatTriangle(projectedVertexY[j], projectedVertexY[k], projectedVertexY[l], projectedVertexX[j], projectedVertexX[k], projectedVertexX[l], modelIntArray3[anIntArray1634[i]]);
			return;
		}
		if (i1 == 2) {
			int j1 = anIntArray1637[i] >> 2;
			int l1 = anIntArray1643[j1];
			int j2 = anIntArray1644[j1];
			int l2 = anIntArray1645[j1];
			Texture.drawTexturedTriangle(projectedVertexY[j], projectedVertexY[k], projectedVertexY[l], projectedVertexX[j], projectedVertexX[k], projectedVertexX[l], anIntArray1634[i], anIntArray1635[i], anIntArray1636[i], projectedY[l1], projectedY[j2], projectedY[l2], projectedZ[l1], projectedZ[j2], projectedZ[l2], depthList[l1], depthList[j2], depthList[l2], faceColor[i]);
			return;
		}
		if (i1 == 3) {
			int k1 = anIntArray1637[i] >> 2;
			int i2 = anIntArray1643[k1];
			int k2 = anIntArray1644[k1];
			int i3 = anIntArray1645[k1];
			Texture.drawTexturedTriangle(projectedVertexY[j], projectedVertexY[k], projectedVertexY[l], projectedVertexX[j], projectedVertexX[k], projectedVertexX[l], anIntArray1634[i], anIntArray1634[i], anIntArray1634[i], projectedY[i2], projectedY[k2], projectedY[i3], projectedZ[i2], projectedZ[k2], projectedZ[i3], depthList[i2], depthList[k2], depthList[i3], faceColor[i]);
		}
	}

	private void method485(int i) {
		int j = Texture.textureInt1;
		int k = Texture.textureInt2;
		int l = 0;
		int i1 = faceA[i];
		int j1 = faceB[i];
		int k1 = faceC[i];
		int l1 = depthList[i1];
		int i2 = depthList[j1];
		int j2 = depthList[k1];
		if (l1 >= 50) {
			SINE[l] = projectedVertexX[i1];
			COSINE[l] = projectedVertexY[i1];
			HYPOT[l++] = anIntArray1634[i];
		} else {
			int k2 = projectedY[i1];
			int k3 = projectedZ[i1];
			int k4 = anIntArray1634[i];
			if (j2 >= 50) {
				int k5 = (50 - l1) * modelIntArray4[j2 - l1];
				SINE[l] = j + (k2 + ((projectedY[k1] - k2) * k5 >> 16) << 9) / 50;
				COSINE[l] = k + (k3 + ((projectedZ[k1] - k3) * k5 >> 16) << 9) / 50;
				HYPOT[l++] = k4 + ((anIntArray1636[i] - k4) * k5 >> 16);
			}
			if (i2 >= 50) {
				int l5 = (50 - l1) * modelIntArray4[i2 - l1];
				SINE[l] = j + (k2 + ((projectedY[j1] - k2) * l5 >> 16) << 9) / 50;
				COSINE[l] = k + (k3 + ((projectedZ[j1] - k3) * l5 >> 16) << 9) / 50;
				HYPOT[l++] = k4 + ((anIntArray1635[i] - k4) * l5 >> 16);
			}
		}
		if (i2 >= 50) {
			SINE[l] = projectedVertexX[j1];
			COSINE[l] = projectedVertexY[j1];
			HYPOT[l++] = anIntArray1635[i];
		} else {
			int l2 = projectedY[j1];
			int l3 = projectedZ[j1];
			int l4 = anIntArray1635[i];
			if (l1 >= 50) {
				int i6 = (50 - i2) * modelIntArray4[l1 - i2];
				SINE[l] = j + (l2 + ((projectedY[i1] - l2) * i6 >> 16) << 9) / 50;
				COSINE[l] = k + (l3 + ((projectedZ[i1] - l3) * i6 >> 16) << 9) / 50;
				HYPOT[l++] = l4 + ((anIntArray1634[i] - l4) * i6 >> 16);
			}
			if (j2 >= 50) {
				int j6 = (50 - i2) * modelIntArray4[j2 - i2];
				SINE[l] = j + (l2 + ((projectedY[k1] - l2) * j6 >> 16) << 9) / 50;
				COSINE[l] = k + (l3 + ((projectedZ[k1] - l3) * j6 >> 16) << 9) / 50;
				HYPOT[l++] = l4 + ((anIntArray1636[i] - l4) * j6 >> 16);
			}
		}
		if (j2 >= 50) {
			SINE[l] = projectedVertexX[k1];
			COSINE[l] = projectedVertexY[k1];
			HYPOT[l++] = anIntArray1636[i];
		} else {
			int i3 = projectedY[k1];
			int i4 = projectedZ[k1];
			int i5 = anIntArray1636[i];
			if (i2 >= 50) {
				int k6 = (50 - j2) * modelIntArray4[i2 - j2];
				SINE[l] = j + (i3 + ((projectedY[j1] - i3) * k6 >> 16) << 9) / 50;
				COSINE[l] = k + (i4 + ((projectedZ[j1] - i4) * k6 >> 16) << 9) / 50;
				HYPOT[l++] = i5 + ((anIntArray1635[i] - i5) * k6 >> 16);
			}
			if (l1 >= 50) {
				int l6 = (50 - j2) * modelIntArray4[l1 - j2];
				SINE[l] = j + (i3 + ((projectedY[i1] - i3) * l6 >> 16) << 9) / 50;
				COSINE[l] = k + (i4 + ((projectedZ[i1] - i4) * l6 >> 16) << 9) / 50;
				HYPOT[l++] = i5 + ((anIntArray1634[i] - i5) * l6 >> 16);
			}
		}
		int j3 = SINE[0];
		int j4 = SINE[1];
		int j5 = SINE[2];
		int i7 = COSINE[0];
		int j7 = COSINE[1];
		int k7 = COSINE[2];
		if ((j3 - j4) * (k7 - j7) - (i7 - j7) * (j5 - j4) > 0) {
			Texture.clip = false;
			if (l == 3) {
				if (j3 < 0 || j4 < 0 || j5 < 0 || j3 > DrawingArea.centerX || j4 > DrawingArea.centerX || j5 > DrawingArea.centerX) {
					Texture.clip = true;
				}
				int l7;
				if (anIntArray1637 == null) {
					l7 = 0;
				} else {
					l7 = anIntArray1637[i] & 3;
				}
				if (l7 == 0) {
					Texture.drawGouraudTriangle(i7, j7, k7, j3, j4, j5, HYPOT[0], HYPOT[1], HYPOT[2]);
				} else if (l7 == 1) {
					Texture.drawFlatTriangle(i7, j7, k7, j3, j4, j5, modelIntArray3[anIntArray1634[i]]);
				} else if (l7 == 2) {
					int j8 = anIntArray1637[i] >> 2;
					int k9 = anIntArray1643[j8];
					int k10 = anIntArray1644[j8];
					int k11 = anIntArray1645[j8];
					Texture.drawTexturedTriangle(i7, j7, k7, j3, j4, j5, HYPOT[0], HYPOT[1], HYPOT[2], projectedY[k9], projectedY[k10], projectedY[k11], projectedZ[k9], projectedZ[k10], projectedZ[k11], depthList[k9], depthList[k10], depthList[k11], faceColor[i]);
				} else if (l7 == 3) {
					int k8 = anIntArray1637[i] >> 2;
					int l9 = anIntArray1643[k8];
					int l10 = anIntArray1644[k8];
					int l11 = anIntArray1645[k8];
					Texture.drawTexturedTriangle(i7, j7, k7, j3, j4, j5, anIntArray1634[i], anIntArray1634[i], anIntArray1634[i], projectedY[l9], projectedY[l10], projectedY[l11], projectedZ[l9], projectedZ[l10], projectedZ[l11], depthList[l9], depthList[l10], depthList[l11], faceColor[i]);
				}
			}
			if (l == 4) {
				if (j3 < 0 || j4 < 0 || j5 < 0 || j3 > DrawingArea.centerX || j4 > DrawingArea.centerX || j5 > DrawingArea.centerX || SINE[3] < 0 || SINE[3] > DrawingArea.centerX) {
					Texture.clip = true;
				}
				int i8;
				if (anIntArray1637 == null) {
					i8 = 0;
				} else {
					i8 = anIntArray1637[i] & 3;
				}
				if (i8 == 0) {
					Texture.drawGouraudTriangle(i7, j7, k7, j3, j4, j5, HYPOT[0], HYPOT[1], HYPOT[2]);
					Texture.drawGouraudTriangle(i7, k7, COSINE[3], j3, j5, SINE[3], HYPOT[0], HYPOT[2], HYPOT[3]);
					return;
				}
				if (i8 == 1) {
					int l8 = modelIntArray3[anIntArray1634[i]];
					Texture.drawFlatTriangle(i7, j7, k7, j3, j4, j5, l8);
					Texture.drawFlatTriangle(i7, k7, COSINE[3], j3, j5, SINE[3], l8);
					return;
				}
				if (i8 == 2) {
					int i9 = anIntArray1637[i] >> 2;
					int i10 = anIntArray1643[i9];
					int i11 = anIntArray1644[i9];
					int i12 = anIntArray1645[i9];
					Texture.drawTexturedTriangle(i7, j7, k7, j3, j4, j5, HYPOT[0], HYPOT[1], HYPOT[2], projectedY[i10], projectedY[i11], projectedY[i12], projectedZ[i10], projectedZ[i11], projectedZ[i12], depthList[i10], depthList[i11], depthList[i12], faceColor[i]);
					Texture.drawTexturedTriangle(i7, k7, COSINE[3], j3, j5, SINE[3], HYPOT[0], HYPOT[2], HYPOT[3], projectedY[i10], projectedY[i11], projectedY[i12], projectedZ[i10], projectedZ[i11], projectedZ[i12], depthList[i10], depthList[i11], depthList[i12], faceColor[i]);
					return;
				}
				if (i8 == 3) {
					int j9 = anIntArray1637[i] >> 2;
					int j10 = anIntArray1643[j9];
					int j11 = anIntArray1644[j9];
					int j12 = anIntArray1645[j9];
					Texture.drawTexturedTriangle(i7, j7, k7, j3, j4, j5, anIntArray1634[i], anIntArray1634[i], anIntArray1634[i], projectedY[j10], projectedY[j11], projectedY[j12], projectedZ[j10], projectedZ[j11], projectedZ[j12], depthList[j10], depthList[j11], depthList[j12], faceColor[i]);
					Texture.drawTexturedTriangle(i7, k7, COSINE[3], j3, j5, SINE[3], anIntArray1634[i], anIntArray1634[i], anIntArray1634[i], projectedY[j10], projectedY[j11], projectedY[j12], projectedZ[j10], projectedZ[j11], projectedZ[j12], depthList[j10], depthList[j11], depthList[j12], faceColor[i]);
				}
			}
		}
	}

	private boolean method486(int i, int j, int k, int l, int i1, int j1, int k1, int l1) {
		if (j < k && j < l && j < i1) {
			return false;
		}
		if (j > k && j > l && j > i1) {
			return false;
		}
		return !(i < j1 && i < k1 && i < l1) && (i <= j1 || i <= k1 || i <= l1);
	}

	public static final Model aModel_1621 = new Model();
	private static int[] anIntArray1622 = new int[2000];
	private static int[] anIntArray1623 = new int[2000];
	private static int[] anIntArray1624 = new int[2000];
	private static int[] anIntArray1625 = new int[2000];
	public int anInt1626;
	public int vertexX[];
	public int vertexY[];
	public int vertexZ[];
	public int anInt1630;
	public int faceA[];
	public int faceB[];
	public int faceC[];
	private int[] anIntArray1634;
	private int[] anIntArray1635;
	private int[] anIntArray1636;
	public int anIntArray1637[];
	private int[] anIntArray1638;
	private int[] anIntArray1639;
	public int faceColor[];
	private int anInt1641;
	private int anInt1642;
	private int[] anIntArray1643;
	private int[] anIntArray1644;
	private int[] anIntArray1645;
	public int minX;
	public int maxX;
	public int maxZ;
	public int minZ;
	public int boundingRadius;
	public int maxY;
	private int diagonal3D;
	private int diagonal2D;
	public int anInt1654;
	private int[] vertexSkins;
	private int[] faceSkins;
	public int vertexGroups[][];
	public int faceGroups[][];
	public boolean aBoolean1659;
	VertexNormal aVertexNormalArray1660[];
	private static ModelHeader[] modelHeaders;
	private static OnDemandFetcherParent modelFetcher;
	private static boolean[] visibilityMap1 = new boolean[4096];
	private static boolean[] visibilityMap2 = new boolean[4096];
	private static int[] projectedVertexX = new int[4096];
	private static int[] projectedVertexY = new int[4096];
	private static int[] projectedX = new int[4096];
	private static int[] projectedY = new int[4096];
	private static int[] projectedZ = new int[4096];
	private static int[] depthList = new int[4096];
	private static int[] vertexQueue = new int[1500];
	private static int[][] vertexGroups2D = new int[1500][512];
	private static int[] cameraSine = new int[12];
	private static int[][] faceLists = new int[12][2000];
	private static int[] stackX = new int[2000];
	private static int[] stackY = new int[2000];
	private static int[] facePriority = new int[12];
	private static final int[] SINE = new int[10];
	private static final int[] COSINE = new int[10];
	private static final int[] HYPOT = new int[10];
	private static int anInt1681;
	private static int anInt1682;
	private static int anInt1683;
	public static boolean withinViewport;
	public static int anInt1685;
	public static int anInt1686;
	public static int anInt1687;
	public static final int[] anIntArray1688 = new int[1000];
	public static int modelIntArray1[];
	public static int modelIntArray2[];
	private static int[] modelIntArray3;
	private static int[] modelIntArray4;

	static {
		modelIntArray1 = Texture.sineTable;
		modelIntArray2 = Texture.cosineTable;
		modelIntArray3 = Texture.brightnessTable;
		modelIntArray4 = Texture.reciprocal16;
	}
}
