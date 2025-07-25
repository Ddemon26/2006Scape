package render.geometry;

import core.engine.Game;
import game.animation.AnimFrame;
import game.entities.Animable;
import game.animation.FrameBase;
import core.network.OnDemandFetcherParent;
import core.network.Stream;
import core.world.WorldController;
import render.core.DrawingArea;
import render.core.Texture;

// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

public final class Model extends Animable {

    public static void clearCache() {
                modelHeaderCache = null;
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
		sineTable = null;
		cosineTable = null;
		brightnessTable = null;
		reciprocalTable = null;
	}

	public static void init(int i, OnDemandFetcherParent onDemandFetcherParent) {
		modelHeaderCache = new ModelHeader[i];
		modelFetcherParent = onDemandFetcherParent;
	}

	public static void loadModelData(byte abyte0[], int j) {
                if (abyte0 == null) {
                        ModelHeader header = modelHeaderCache[j] = new ModelHeader();
                        header.vertexCount = 0;
                        header.faceCount = 0;
                        header.texturedTriangleCount = 0;
                        return;
                }
                Stream stream = new Stream(abyte0);
                stream.currentOffset = abyte0.length - 18;
                ModelHeader header1 = modelHeaderCache[j] = new ModelHeader();
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
		modelHeaderCache[j] = null;
	}

	public static Model create(int j) {
		if (modelHeaderCache == null) {
			return null;
		}
		ModelHeader header = modelHeaderCache.length < j  ? null : modelHeaderCache[j];
		if (header == null) {
                        modelFetcherParent.requestModel(j);
			return null;
		} else {
			return new Model(j);
		}
	}

	public static boolean isLoaded(int i) {
		if (modelHeaderCache == null) {
			return false;
		}
		ModelHeader header = modelHeaderCache[i];
		if (header == null) {
                        modelFetcherParent.requestModel(i);
			return false;
		} else {
			return true;
		}
	}

	public Model() {
		pickable = false;
	}

	public Model(int i) {
		pickable = false;
		ModelHeader header = modelHeaderCache[i];
                vertexCount = header.vertexCount;
                faceCount = header.faceCount;
                texturedTriangleCount = header.texturedTriangleCount;
		vertexX = new int[vertexCount];
		vertexY = new int[vertexCount];
		vertexZ = new int[vertexCount];
		faceA = new int[faceCount];
		faceB = new int[faceCount];
		faceC = new int[faceCount];
		texTriangleX = new int[texturedTriangleCount];
		texTriangleY = new int[texturedTriangleCount];
		texTriangleZ = new int[texturedTriangleCount];
                if (header.vertexSkinsOffset >= 0) {
                        vertexSkins = new int[vertexCount];
                }
                if (header.faceLabelOffset >= 0) {
                        faceRenderTypes = new int[faceCount];
                }
                if (header.faceAlphaOffset >= 0) {
                        facePriorities = new int[faceCount];
                } else {
                        defaultPriority = -header.faceAlphaOffset - 1;
                }
                if (header.faceTextureOffset >= 0) {
                        faceAlphas = new int[faceCount];
                }
                if (header.vertexLabelOffset >= 0) {
                        faceSkins = new int[faceCount];
                }
                faceColor = new int[faceCount];
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
		for (int j1 = 0; j1 < vertexCount; j1++) {
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
		for (int l1 = 0; l1 < faceCount; l1++) {
			faceColor[l1] = stream.readUnsignedWord();
			if (faceRenderTypes != null) {
				faceRenderTypes[l1] = stream_1.readUnsignedByte();
			}
			if (facePriorities != null) {
				facePriorities[l1] = stream_2.readUnsignedByte();
			}
			if (faceAlphas != null) {
				faceAlphas[l1] = stream_3.readUnsignedByte();
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
		for (int l3 = 0; l3 < faceCount; l3++) {
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
		for (int j4 = 0; j4 < texturedTriangleCount; j4++) {
			texTriangleX[j4] = stream.readUnsignedWord();
			texTriangleY[j4] = stream.readUnsignedWord();
			texTriangleZ[j4] = stream.readUnsignedWord();
		}

	}

    public Model(int i, Model models[]) {
		pickable = false;
		boolean flag = false;
		boolean flag1 = false;
		boolean flag2 = false;
		boolean flag3 = false;
		vertexCount = 0;
		faceCount = 0;
		texturedTriangleCount = 0;
		defaultPriority = -1;
		for (int k = 0; k < i; k++) {
                    Model model = models[k];
			if (model != null) {
				vertexCount += model.vertexCount;
				faceCount += model.faceCount;
				texturedTriangleCount += model.texturedTriangleCount;
				flag |= model.faceRenderTypes != null;
				if (model.facePriorities != null) {
					flag1 = true;
				} else {
					if (defaultPriority == -1) {
						defaultPriority = model.defaultPriority;
					}
					if (defaultPriority != model.defaultPriority) {
						flag1 = true;
					}
				}
				flag2 |= model.faceAlphas != null;
				flag3 |= model.faceSkins != null;
			}
		}

		vertexX = new int[vertexCount];
		vertexY = new int[vertexCount];
		vertexZ = new int[vertexCount];
		vertexSkins = new int[vertexCount];
		faceA = new int[faceCount];
		faceB = new int[faceCount];
		faceC = new int[faceCount];
		texTriangleX = new int[texturedTriangleCount];
		texTriangleY = new int[texturedTriangleCount];
		texTriangleZ = new int[texturedTriangleCount];
		if (flag) {
			faceRenderTypes = new int[faceCount];
		}
		if (flag1) {
			facePriorities = new int[faceCount];
		}
		if (flag2) {
			faceAlphas = new int[faceCount];
		}
		if (flag3) {
			faceSkins = new int[faceCount];
		}
		faceColor = new int[faceCount];
		vertexCount = 0;
		faceCount = 0;
		texturedTriangleCount = 0;
		int l = 0;
		for (int i1 = 0; i1 < i; i1++) {
                    Model model_1 = models[i1];
			if (model_1 != null) {
				for (int j1 = 0; j1 < model_1.faceCount; j1++) {
					if (flag) {
						if (model_1.faceRenderTypes == null) {
							faceRenderTypes[faceCount] = 0;
						} else {
							int k1 = model_1.faceRenderTypes[j1];
							if ((k1 & 2) == 2) {
								k1 += l << 2;
							}
							faceRenderTypes[faceCount] = k1;
						}
					}
					if (flag1) {
						if (model_1.facePriorities == null) {
							facePriorities[faceCount] = model_1.defaultPriority;
						} else {
							facePriorities[faceCount] = model_1.facePriorities[j1];
						}
					}
					if (flag2) {
						if (model_1.faceAlphas == null) {
							faceAlphas[faceCount] = 0;
						} else {
							faceAlphas[faceCount] = model_1.faceAlphas[j1];
						}
					}
					if (flag3 && model_1.faceSkins != null) {
						faceSkins[faceCount] = model_1.faceSkins[j1];
					}
					faceColor[faceCount] = model_1.faceColor[j1];
					faceA[faceCount] = getOrCreateVertex(model_1, model_1.faceA[j1]);
					faceB[faceCount] = getOrCreateVertex(model_1, model_1.faceB[j1]);
					faceC[faceCount] = getOrCreateVertex(model_1, model_1.faceC[j1]);
					faceCount++;
				}

				for (int l1 = 0; l1 < model_1.texturedTriangleCount; l1++) {
					texTriangleX[texturedTriangleCount] = getOrCreateVertex(model_1, model_1.texTriangleX[l1]);
					texTriangleY[texturedTriangleCount] = getOrCreateVertex(model_1, model_1.texTriangleY[l1]);
					texTriangleZ[texturedTriangleCount] = getOrCreateVertex(model_1, model_1.texTriangleZ[l1]);
					texturedTriangleCount++;
				}

				l += model_1.texturedTriangleCount;
			}
		}

	}

    public Model(Model models[]) {
		int i = 2;// was parameter
		pickable = false;
		boolean flag1 = false;
		boolean flag2 = false;
		boolean flag3 = false;
		boolean flag4 = false;
		vertexCount = 0;
		faceCount = 0;
		texturedTriangleCount = 0;
		defaultPriority = -1;
		for (int k = 0; k < i; k++) {
                    Model model = models[k];
			if (model != null) {
				vertexCount += model.vertexCount;
				faceCount += model.faceCount;
				texturedTriangleCount += model.texturedTriangleCount;
				flag1 |= model.faceRenderTypes != null;
				if (model.facePriorities != null) {
					flag2 = true;
				} else {
					if (defaultPriority == -1) {
						defaultPriority = model.defaultPriority;
					}
					if (defaultPriority != model.defaultPriority) {
						flag2 = true;
					}
				}
				flag3 |= model.faceAlphas != null;
				flag4 |= model.faceColor != null;
			}
		}

		vertexX = new int[vertexCount];
		vertexY = new int[vertexCount];
		vertexZ = new int[vertexCount];
		faceA = new int[faceCount];
		faceB = new int[faceCount];
		faceC = new int[faceCount];
		shadeA = new int[faceCount];
		shadeB = new int[faceCount];
		shadeC = new int[faceCount];
		texTriangleX = new int[texturedTriangleCount];
		texTriangleY = new int[texturedTriangleCount];
		texTriangleZ = new int[texturedTriangleCount];
		if (flag1) {
			faceRenderTypes = new int[faceCount];
		}
		if (flag2) {
			facePriorities = new int[faceCount];
		}
		if (flag3) {
			faceAlphas = new int[faceCount];
		}
		if (flag4) {
			faceColor = new int[faceCount];
		}
		vertexCount = 0;
		faceCount = 0;
		texturedTriangleCount = 0;
		int i1 = 0;
		for (int j1 = 0; j1 < i; j1++) {
                    Model model_1 = models[j1];
			if (model_1 != null) {
				int k1 = vertexCount;
				for (int l1 = 0; l1 < model_1.vertexCount; l1++) {
					vertexX[vertexCount] = model_1.vertexX[l1];
					vertexY[vertexCount] = model_1.vertexY[l1];
					vertexZ[vertexCount] = model_1.vertexZ[l1];
					vertexCount++;
				}

				for (int i2 = 0; i2 < model_1.faceCount; i2++) {
					faceA[faceCount] = model_1.faceA[i2] + k1;
					faceB[faceCount] = model_1.faceB[i2] + k1;
					faceC[faceCount] = model_1.faceC[i2] + k1;
					shadeA[faceCount] = model_1.shadeA[i2];
					shadeB[faceCount] = model_1.shadeB[i2];
					shadeC[faceCount] = model_1.shadeC[i2];
					if (flag1) {
						if (model_1.faceRenderTypes == null) {
							faceRenderTypes[faceCount] = 0;
						} else {
							int j2 = model_1.faceRenderTypes[i2];
							if ((j2 & 2) == 2) {
								j2 += i1 << 2;
							}
							faceRenderTypes[faceCount] = j2;
						}
					}
					if (flag2) {
						if (model_1.facePriorities == null) {
							facePriorities[faceCount] = model_1.defaultPriority;
						} else {
							facePriorities[faceCount] = model_1.facePriorities[i2];
						}
					}
					if (flag3) {
						if (model_1.faceAlphas == null) {
							faceAlphas[faceCount] = 0;
						} else {
							faceAlphas[faceCount] = model_1.faceAlphas[i2];
						}
					}
					if (flag4 && model_1.faceColor != null) {
						faceColor[faceCount] = model_1.faceColor[i2];
					}
					faceCount++;
				}

				for (int k2 = 0; k2 < model_1.texturedTriangleCount; k2++) {
					texTriangleX[texturedTriangleCount] = model_1.texTriangleX[k2] + k1;
					texTriangleY[texturedTriangleCount] = model_1.texTriangleY[k2] + k1;
					texTriangleZ[texturedTriangleCount] = model_1.texTriangleZ[k2] + k1;
					texturedTriangleCount++;
				}

				i1 += model_1.texturedTriangleCount;
			}
		}

		calculateBounds();
	}

	public Model(boolean flag, boolean flag1, boolean flag2, Model model) {
		pickable = false;
		vertexCount = model.vertexCount;
		faceCount = model.faceCount;
		texturedTriangleCount = model.texturedTriangleCount;
		if (flag2) {
			vertexX = model.vertexX;
			vertexY = model.vertexY;
			vertexZ = model.vertexZ;
		} else {
			vertexX = new int[vertexCount];
			vertexY = new int[vertexCount];
			vertexZ = new int[vertexCount];
			for (int j = 0; j < vertexCount; j++) {
				vertexX[j] = model.vertexX[j];
				vertexY[j] = model.vertexY[j];
				vertexZ[j] = model.vertexZ[j];
			}

		}
		if (flag) {
			faceColor = model.faceColor;
		} else {
			faceColor = new int[faceCount];
			System.arraycopy(model.faceColor, 0, faceColor, 0, faceCount);

		}
		if (flag1) {
			faceAlphas = model.faceAlphas;
		} else {
			faceAlphas = new int[faceCount];
			if (model.faceAlphas == null) {
				for (int l = 0; l < faceCount; l++) {
					faceAlphas[l] = 0;
				}

			} else {
				System.arraycopy(model.faceAlphas, 0, faceAlphas, 0, faceCount);

			}
		}
		vertexSkins = model.vertexSkins;
		faceSkins = model.faceSkins;
		faceRenderTypes = model.faceRenderTypes;
		faceA = model.faceA;
		faceB = model.faceB;
		faceC = model.faceC;
		facePriorities = model.facePriorities;
		defaultPriority = model.defaultPriority;
		texTriangleX = model.texTriangleX;
		texTriangleY = model.texTriangleY;
		texTriangleZ = model.texTriangleZ;
	}

	public Model(boolean flag, boolean flag1, Model model) {
		pickable = false;
		vertexCount = model.vertexCount;
		faceCount = model.faceCount;
		texturedTriangleCount = model.texturedTriangleCount;
		if (flag) {
			vertexY = new int[vertexCount];
			System.arraycopy(model.vertexY, 0, vertexY, 0, vertexCount);

		} else {
			vertexY = model.vertexY;
		}
		if (flag1) {
			shadeA = new int[faceCount];
			shadeB = new int[faceCount];
			shadeC = new int[faceCount];
			for (int k = 0; k < faceCount; k++) {
				shadeA[k] = model.shadeA[k];
				shadeB[k] = model.shadeB[k];
				shadeC[k] = model.shadeC[k];
			}

			faceRenderTypes = new int[faceCount];
			if (model.faceRenderTypes == null) {
				for (int l = 0; l < faceCount; l++) {
					faceRenderTypes[l] = 0;
				}

			} else {
				System.arraycopy(model.faceRenderTypes, 0, faceRenderTypes, 0, faceCount);

			}
                       super.vertexNormals = new VertexNormal[vertexCount];
                for (int j1 = 0; j1 < vertexCount; j1++) {
                               VertexNormal vertexNormalDest = super.vertexNormals[j1] = new VertexNormal();
                               VertexNormal vertexNormalSrc = model.vertexNormals[j1];
                                vertexNormalDest.x = vertexNormalSrc.x;
                                vertexNormalDest.y = vertexNormalSrc.y;
                                vertexNormalDest.z = vertexNormalSrc.z;
                                vertexNormalDest.magnitude = vertexNormalSrc.magnitude;
                        }

			vertexNormalTemp = model.vertexNormalTemp;
		} else {
			shadeA = model.shadeA;
			shadeB = model.shadeB;
			shadeC = model.shadeC;
			faceRenderTypes = model.faceRenderTypes;
		}
		vertexX = model.vertexX;
		vertexZ = model.vertexZ;
		faceColor = model.faceColor;
		faceAlphas = model.faceAlphas;
		facePriorities = model.facePriorities;
		defaultPriority = model.defaultPriority;
		faceA = model.faceA;
		faceB = model.faceB;
		faceC = model.faceC;
		texTriangleX = model.texTriangleX;
		texTriangleY = model.texTriangleY;
		texTriangleZ = model.texTriangleZ;
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
                vertexCount = src.vertexCount;
                faceCount = src.faceCount;
                texturedTriangleCount = src.texturedTriangleCount;
                if (tempVertexX.length < vertexCount) {
                        tempVertexX = new int[vertexCount + 100];
                        tempVertexY = new int[vertexCount + 100];
                        tempVertexZ = new int[vertexCount + 100];
                }
                vertexX = tempVertexX;
                vertexY = tempVertexY;
                vertexZ = tempVertexZ;
                for (int k = 0; k < vertexCount; k++) {
                        vertexX[k] = src.vertexX[k];
                        vertexY[k] = src.vertexY[k];
                        vertexZ[k] = src.vertexZ[k];
                }

                if (shareColor) {
                        faceAlphas = src.faceAlphas;
                } else {
                        if (tempFaceTextures.length < faceCount) {
                                tempFaceTextures = new int[faceCount + 100];
                        }
                        faceAlphas = tempFaceTextures;
                        if (src.faceAlphas == null) {
                                for (int l = 0; l < faceCount; l++) {
                                        faceAlphas[l] = 0;
                                }

                        } else {
                                System.arraycopy(src.faceAlphas, 0, faceAlphas, 0, faceCount);

                        }
                }
                faceRenderTypes = src.faceRenderTypes;
                faceColor = src.faceColor;
                facePriorities = src.facePriorities;
                defaultPriority = src.defaultPriority;
                faceGroups = src.faceGroups;
                vertexGroups = src.vertexGroups;
                faceA = src.faceA;
                faceB = src.faceB;
                faceC = src.faceC;
                shadeA = src.shadeA;
                shadeB = src.shadeB;
                shadeC = src.shadeC;
                texTriangleX = src.texTriangleX;
                texTriangleY = src.texTriangleY;
                texTriangleZ = src.texTriangleZ;
        }

	private int getOrCreateVertex(Model model, int i) {
		int j = -1;
		int k = model.vertexX[i];
		int l = model.vertexY[i];
		int i1 = model.vertexZ[i];
		for (int j1 = 0; j1 < vertexCount; j1++) {
			if (k != vertexX[j1] || l != vertexY[j1] || i1 != vertexZ[j1]) {
				continue;
			}
			j = j1;
			break;
		}

		if (j == -1) {
			vertexX[vertexCount] = k;
			vertexY[vertexCount] = l;
			vertexZ[vertexCount] = i1;
			if (model.vertexSkins != null) {
				vertexSkins[vertexCount] = model.vertexSkins[i];
			}
			j = vertexCount++;
		}
		return j;
	}

	public void calculateBounds() {
		super.modelHeight = 0;
		boundingRadius = 0;
		maxY = 0;
		for (int i = 0; i < vertexCount; i++) {
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
		for (int i = 0; i < vertexCount; i++) {
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
		for (int j = 0; j < vertexCount; j++) {
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
			for (int l = 0; l < vertexCount; l++) {
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

			for (int j2 = 0; j2 < vertexCount; j2++) {
				int l2 = vertexSkins[j2];
				vertexGroups[l2][ai[l2]++] = j2;
			}

			vertexSkins = null;
		}
		if (faceSkins != null) {
			int ai1[] = new int[256];
			int k = 0;
			for (int i1 = 0; i1 < faceCount; i1++) {
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

			for (int k2 = 0; k2 < faceCount; k2++) {
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
                AnimFrame animFrame = AnimFrame.forId(i);
                if (animFrame == null) {
                        return;
                }
                FrameBase frameBase = animFrame.frameBase;
		transformX = 0;
		transformY = 0;
		transformZ = 0;
                for (int k = 0; k < animFrame.transformationCount; k++) {
                        int l = animFrame.transformationIndices[k];
                        transformVertices(frameBase.transformationType[l], frameBase.transformationList[l], animFrame.transformX[k], animFrame.transformY[k], animFrame.transformZ[k]);
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
                AnimFrame animFrame = AnimFrame.forId(k);
                if (animFrame == null) {
                        return;
                }
                AnimFrame secondaryFrame = AnimFrame.forId(j);
                if (secondaryFrame == null) {
                        applyFrame(k);
                        return;
                }
                FrameBase frameBase = animFrame.frameBase;
		transformX = 0;
		transformY = 0;
		transformZ = 0;
		int l = 0;
		int i1 = ai[l++];
                for (int j1 = 0; j1 < animFrame.transformationCount; j1++) {
                        int k1;
                        for (k1 = animFrame.transformationIndices[j1]; k1 > i1; i1 = ai[l++]) {
                                ;
                        }
                        if (k1 != i1 || frameBase.transformationType[k1] == 0) {
                                transformVertices(frameBase.transformationType[k1], frameBase.transformationList[k1], animFrame.transformX[j1], animFrame.transformY[j1], animFrame.transformZ[j1]);
                        }
                }

		transformX = 0;
		transformY = 0;
		transformZ = 0;
		l = 0;
		i1 = ai[l++];
                for (int l1 = 0; l1 < secondaryFrame.transformationCount; l1++) {
                        int i2;
                        for (i2 = secondaryFrame.transformationIndices[l1]; i2 > i1; i1 = ai[l++]) {
                                ;
                        }
                        if (i2 == i1 || frameBase.transformationType[i2] == 0) {
                                transformVertices(frameBase.transformationType[i2], frameBase.transformationList[i2], secondaryFrame.transformX[l1], secondaryFrame.transformY[l1], secondaryFrame.transformZ[l1]);
                        }
                }

	}

	private void transformVertices(int i, int ai[], int j, int k, int l) {
		int i1 = ai.length;
		if (i == 0) {
			int j1 = 0;
			transformX = 0;
			transformY = 0;
			transformZ = 0;
			for (int k2 = 0; k2 < i1; k2++) {
				int l3 = ai[k2];
				if (l3 < vertexGroups.length) {
					int ai5[] = vertexGroups[l3];
					for (int j6 : ai5) {
						transformX += vertexX[j6];
						transformY += vertexY[j6];
						transformZ += vertexZ[j6];
						j1++;
					}

				}
			}

			if (j1 > 0) {
				transformX = transformX / j1 + j;
				transformY = transformY / j1 + k;
				transformZ = transformZ / j1 + l;
				return;
			} else {
				transformX = j;
				transformY = k;
				transformZ = l;
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
						vertexX[k5] -= transformX;
						vertexY[k5] -= transformY;
						vertexZ[k5] -= transformZ;
						int k6 = (j & 0xff) * 8;
						int l6 = (k & 0xff) * 8;
						int i7 = (l & 0xff) * 8;
						if (i7 != 0) {
							int j7 = sineTable[i7];
							int i8 = cosineTable[i7];
							int l8 = vertexY[k5] * j7 + vertexX[k5] * i8 >> 16;
							vertexY[k5] = vertexY[k5] * i8 - vertexX[k5] * j7 >> 16;
							vertexX[k5] = l8;
						}
						if (k6 != 0) {
							int k7 = sineTable[k6];
							int j8 = cosineTable[k6];
							int i9 = vertexY[k5] * j8 - vertexZ[k5] * k7 >> 16;
							vertexZ[k5] = vertexY[k5] * k7 + vertexZ[k5] * j8 >> 16;
							vertexY[k5] = i9;
						}
						if (l6 != 0) {
							int l7 = sineTable[l6];
							int k8 = cosineTable[l6];
							int j9 = vertexZ[k5] * l7 + vertexX[k5] * k8 >> 16;
							vertexZ[k5] = vertexZ[k5] * k8 - vertexX[k5] * l7 >> 16;
							vertexX[k5] = j9;
						}
						vertexX[k5] += transformX;
						vertexY[k5] += transformY;
						vertexZ[k5] += transformZ;
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
						vertexX[l5] -= transformX;
						vertexY[l5] -= transformY;
						vertexZ[l5] -= transformZ;
						vertexX[l5] = vertexX[l5] * j / 128;
						vertexY[l5] = vertexY[l5] * k / 128;
						vertexZ[l5] = vertexZ[l5] * l / 128;
						vertexX[l5] += transformX;
						vertexY[l5] += transformY;
						vertexZ[l5] += transformZ;
					}

				}
			}

			return;
		}
		if (i == 5 && faceGroups != null && faceAlphas != null) {
			for (int j2 = 0; j2 < i1; j2++) {
				int k3 = ai[j2];
				if (k3 < faceGroups.length) {
					int ai4[] = faceGroups[k3];
					for (int element : ai4) {
						int i6 = element;
						faceAlphas[i6] += j * 8;
						if (faceAlphas[i6] < 0) {
							faceAlphas[i6] = 0;
						}
						if (faceAlphas[i6] > 255) {
							faceAlphas[i6] = 255;
						}
					}

				}
			}

		}
	}

	public void calculateNormals() {
		for (int j = 0; j < vertexCount; j++) {
			int k = vertexX[j];
			vertexX[j] = vertexZ[j];
			vertexZ[j] = -k;
		}

	}

	public void rotateX(int i) {
		int k = sineTable[i];
		int l = cosineTable[i];
		for (int i1 = 0; i1 < vertexCount; i1++) {
			int j1 = vertexY[i1] * l - vertexZ[i1] * k >> 16;
			vertexZ[i1] = vertexY[i1] * k + vertexZ[i1] * l >> 16;
			vertexY[i1] = j1;
		}
	}

	public void translate(int i, int j, int l) {
		for (int i1 = 0; i1 < vertexCount; i1++) {
			vertexX[i1] += i;
			vertexY[i1] += j;
			vertexZ[i1] += l;
		}

	}

	public void recolor(int i, int j) {
		for (int k = 0; k < faceCount; k++) {
			if (faceColor[k] == i) {
				faceColor[k] = j;
			}
		}

	}

	public void mirror() {
		for (int j = 0; j < vertexCount; j++) {
			vertexZ[j] = -vertexZ[j];
		}

		for (int k = 0; k < faceCount; k++) {
			int l = faceA[k];
			faceA[k] = faceC[k];
			faceC[k] = l;
		}
	}

	public void scaleModel(int i, int j, int l) {
		for (int i1 = 0; i1 < vertexCount; i1++) {
			vertexX[i1] = vertexX[i1] * i / 128;
			vertexY[i1] = vertexY[i1] * l / 128;
			vertexZ[i1] = vertexZ[i1] * j / 128;
		}

	}

	public void applyLighting(int i, int j, int k, int l, int i1, boolean flag) {
		int j1 = (int) Math.sqrt(k * k + l * l + i1 * i1);
		int k1 = j * j1 >> 8;
		if (shadeA == null) {
			shadeA = new int[faceCount];
			shadeB = new int[faceCount];
			shadeC = new int[faceCount];
		}
               if (super.vertexNormals == null) {
                       super.vertexNormals = new VertexNormal[vertexCount];
                       for (int l1 = 0; l1 < vertexCount; l1++) {
                               super.vertexNormals[l1] = new VertexNormal();
                       }

		}
		for (int i2 = 0; i2 < faceCount; i2++) {
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
                        if (faceRenderTypes == null || (faceRenderTypes[i2] & 1) == 0) {
                               VertexNormal vertexNormal = super.vertexNormals[j2];
                                vertexNormal.x += l4;
                                vertexNormal.y += i5;
                                vertexNormal.z += j5;
                                vertexNormal.magnitude++;
                               vertexNormal = super.vertexNormals[l2];
                                vertexNormal.x += l4;
                                vertexNormal.y += i5;
                                vertexNormal.z += j5;
                                vertexNormal.magnitude++;
                               vertexNormal = super.vertexNormals[i3];
                                vertexNormal.x += l4;
                                vertexNormal.y += i5;
                                vertexNormal.z += j5;
                                vertexNormal.magnitude++;
			} else {
				int l5 = i + (k * l4 + l * i5 + i1 * j5) / (k1 + k1 / 2);
                                shadeA[i2] = calculateShadedColor(faceColor[i2], l5, faceRenderTypes[i2]);
			}
		}

		if (flag) {
			applyShading(i, k1, k, l, i1);
		} else {
			vertexNormalTemp = new VertexNormal[vertexCount];
                        for (int k2 = 0; k2 < vertexCount; k2++) {
                                VertexNormal srcNormal = super.vertexNormals[k2];
                                VertexNormal destNormal = vertexNormalTemp[k2] = new VertexNormal();
                                destNormal.x = srcNormal.x;
                                destNormal.y = srcNormal.y;
                                destNormal.z = srcNormal.z;
                                destNormal.magnitude = srcNormal.magnitude;
                        }

		}
		if (flag) {
			calculateBounds();
		} else {
			calculateExtremes();
		}
	}

	public void applyShading(int i, int j, int k, int l, int i1) {
		for (int j1 = 0; j1 < faceCount; j1++) {
			int k1 = faceA[j1];
			int i2 = faceB[j1];
			int j2 = faceC[j1];
			if (faceRenderTypes == null) {
				int i3 = faceColor[j1];
                                VertexNormal vertexNormal = super.vertexNormals[k1];
                                int k2 = i + (k * vertexNormal.x + l * vertexNormal.y + i1 * vertexNormal.z) / (j * vertexNormal.magnitude);
                                shadeA[j1] = calculateShadedColor(i3, k2, 0);
                                vertexNormal = super.vertexNormals[i2];
                                k2 = i + (k * vertexNormal.x + l * vertexNormal.y + i1 * vertexNormal.z) / (j * vertexNormal.magnitude);
                                shadeB[j1] = calculateShadedColor(i3, k2, 0);
                                vertexNormal = super.vertexNormals[j2];
                                k2 = i + (k * vertexNormal.x + l * vertexNormal.y + i1 * vertexNormal.z) / (j * vertexNormal.magnitude);
                                shadeC[j1] = calculateShadedColor(i3, k2, 0);
			} else if ((faceRenderTypes[j1] & 1) == 0) {
				int j3 = faceColor[j1];
				int k3 = faceRenderTypes[j1];
                                VertexNormal vertexNormal1 = super.vertexNormals[k1];
                                int l2 = i + (k * vertexNormal1.x + l * vertexNormal1.y + i1 * vertexNormal1.z) / (j * vertexNormal1.magnitude);
                                shadeA[j1] = calculateShadedColor(j3, l2, k3);
                                vertexNormal1 = super.vertexNormals[i2];
                                l2 = i + (k * vertexNormal1.x + l * vertexNormal1.y + i1 * vertexNormal1.z) / (j * vertexNormal1.magnitude);
                                shadeB[j1] = calculateShadedColor(j3, l2, k3);
                                vertexNormal1 = super.vertexNormals[j2];
                                l2 = i + (k * vertexNormal1.x + l * vertexNormal1.y + i1 * vertexNormal1.z) / (j * vertexNormal1.magnitude);
                                shadeC[j1] = calculateShadedColor(j3, l2, k3);
			}
		}

                super.vertexNormals = null;
		vertexNormalTemp = null;
		vertexSkins = null;
		faceSkins = null;
		if (faceRenderTypes != null) {
			for (int l1 = 0; l1 < faceCount; l1++) {
				if ((faceRenderTypes[l1] & 2) == 2) {
					return;
				}
			}

		}
		faceColor = null;
	}

        private static int calculateShadedColor(int i, int j, int k) {
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

        public void transformVertices(int j, int k, int l, int i1, int j1, int k1) {
		int i = 0; // was a parameter
		int l1 = Texture.textureInt1;
		int i2 = Texture.textureInt2;
		int j2 = sineTable[i];
		int k2 = cosineTable[i];
		int l2 = sineTable[j];
		int i3 = cosineTable[j];
		int j3 = sineTable[k];
		int k3 = cosineTable[k];
		int l3 = sineTable[l];
		int i4 = cosineTable[l];
		int j4 = j1 * l3 + k1 * i4 >> 16;
		for (int k4 = 0; k4 < vertexCount; k4++) {
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
			if (texturedTriangleCount > 0) {
				projectedY[k4] = l4;
				projectedZ[k4] = i5;
				depthList[k4] = j5;
			}
		}

                try {
                        processVisibility(false, false, 0);
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
                if (k4 / i3 <= -DrawingArea.viewportHalfHeight) {
                        return;
                }
                int l4 = j4 + (super.modelHeight * k >> 16);
                int i5 = i4 - l4 << 9;
                if (i5 / i3 >= DrawingArea.viewportHalfHeight) {
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
			int i6 = viewportCenterX - Texture.textureInt1;
			int k6 = viewportCenterY - Texture.textureInt2;
			if (i6 > k3 && i6 < l3 && k6 > i5 && k6 < k4) {
				if (pickable) {
					faceQueue[queueLength++] = i2;
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
			l6 = sineTable[i];
			i7 = cosineTable[i];
		}
		for (int j7 = 0; j7 < vertexCount; j7++) {
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
			if (flag || texturedTriangleCount > 0) {
				projectedY[j7] = k7;
				projectedZ[j7] = l7;
				depthList[j7] = i8;
			}
		}

                try {
                        processVisibility(flag, flag1, i2);
                } catch (Exception _ex) {
                }
	}

        private void processVisibility(boolean flag, boolean flag1, int i) {
		for (int j = 0; j < diagonal3D; j++) {
			vertexQueue[j] = 0;
		}

		for (int k = 0; k < faceCount; k++) {
			if (faceRenderTypes == null || faceRenderTypes[k] != -1) {
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
                                        if (flag1 && isTriangleVisible(viewportCenterX, viewportCenterY, projectedVertexY[l], projectedVertexY[k1], projectedVertexY[j2], i3, l3, k4)) {
						faceQueue[queueLength++] = i;
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

		if (facePriorities == null) {
			for (int i1 = diagonal3D - 1; i1 >= 0; i1--) {
				int l1 = vertexQueue[i1];
				if (l1 > 0) {
					int ai[] = vertexGroups2D[i1];
					for (int j3 = 0; j3 < l1; j3++) {
                                                drawFace(ai[j3]);
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
					int l5 = facePriorities[l4];
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
				drawFace(ai2[i6++]);
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
				drawFace(ai2[i6++]);
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
				drawFace(ai2[i6++]);
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
				drawFace(ai4[j7]);
			}

		}

		while (i5 != -1000) {
			drawFace(ai2[i6++]);
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

	private void drawFace(int i) {
		if (visibilityMap2[i]) {
			drawClippedFace(i);
			return;
		}
		int j = faceA[i];
		int k = faceB[i];
		int l = faceC[i];
		Texture.clip = visibilityMap1[i];
		if (faceAlphas == null) {
			Texture.alpha = 0;
		} else {
			Texture.alpha = faceAlphas[i];
		}
		int i1;
		if (faceRenderTypes == null) {
			i1 = 0;
		} else {
			i1 = faceRenderTypes[i] & 3;
		}
		if (i1 == 0) {
			Texture.drawGouraudTriangle(projectedVertexY[j], projectedVertexY[k], projectedVertexY[l], projectedVertexX[j], projectedVertexX[k], projectedVertexX[l], shadeA[i], shadeB[i], shadeC[i]);
			return;
		}
		if (i1 == 1) {
			Texture.drawFlatTriangle(projectedVertexY[j], projectedVertexY[k], projectedVertexY[l], projectedVertexX[j], projectedVertexX[k], projectedVertexX[l], brightnessTable[shadeA[i]]);
			return;
		}
		if (i1 == 2) {
			int j1 = faceRenderTypes[i] >> 2;
			int l1 = texTriangleX[j1];
			int j2 = texTriangleY[j1];
			int l2 = texTriangleZ[j1];
			Texture.drawTexturedTriangle(projectedVertexY[j], projectedVertexY[k], projectedVertexY[l], projectedVertexX[j], projectedVertexX[k], projectedVertexX[l], shadeA[i], shadeB[i], shadeC[i], projectedY[l1], projectedY[j2], projectedY[l2], projectedZ[l1], projectedZ[j2], projectedZ[l2], depthList[l1], depthList[j2], depthList[l2], faceColor[i]);
			return;
		}
		if (i1 == 3) {
			int k1 = faceRenderTypes[i] >> 2;
			int i2 = texTriangleX[k1];
			int k2 = texTriangleY[k1];
			int i3 = texTriangleZ[k1];
			Texture.drawTexturedTriangle(projectedVertexY[j], projectedVertexY[k], projectedVertexY[l], projectedVertexX[j], projectedVertexX[k], projectedVertexX[l], shadeA[i], shadeA[i], shadeA[i], projectedY[i2], projectedY[k2], projectedY[i3], projectedZ[i2], projectedZ[k2], projectedZ[i3], depthList[i2], depthList[k2], depthList[i3], faceColor[i]);
		}
	}

	private void drawClippedFace(int i) {
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
			HYPOT[l++] = shadeA[i];
		} else {
			int k2 = projectedY[i1];
			int k3 = projectedZ[i1];
			int k4 = shadeA[i];
			if (j2 >= 50) {
				int k5 = (50 - l1) * reciprocalTable[j2 - l1];
				SINE[l] = j + (k2 + ((projectedY[k1] - k2) * k5 >> 16) << 9) / 50;
				COSINE[l] = k + (k3 + ((projectedZ[k1] - k3) * k5 >> 16) << 9) / 50;
				HYPOT[l++] = k4 + ((shadeC[i] - k4) * k5 >> 16);
			}
			if (i2 >= 50) {
				int l5 = (50 - l1) * reciprocalTable[i2 - l1];
				SINE[l] = j + (k2 + ((projectedY[j1] - k2) * l5 >> 16) << 9) / 50;
				COSINE[l] = k + (k3 + ((projectedZ[j1] - k3) * l5 >> 16) << 9) / 50;
				HYPOT[l++] = k4 + ((shadeB[i] - k4) * l5 >> 16);
			}
		}
		if (i2 >= 50) {
			SINE[l] = projectedVertexX[j1];
			COSINE[l] = projectedVertexY[j1];
			HYPOT[l++] = shadeB[i];
		} else {
			int l2 = projectedY[j1];
			int l3 = projectedZ[j1];
			int l4 = shadeB[i];
			if (l1 >= 50) {
				int i6 = (50 - i2) * reciprocalTable[l1 - i2];
				SINE[l] = j + (l2 + ((projectedY[i1] - l2) * i6 >> 16) << 9) / 50;
				COSINE[l] = k + (l3 + ((projectedZ[i1] - l3) * i6 >> 16) << 9) / 50;
				HYPOT[l++] = l4 + ((shadeA[i] - l4) * i6 >> 16);
			}
			if (j2 >= 50) {
				int j6 = (50 - i2) * reciprocalTable[j2 - i2];
				SINE[l] = j + (l2 + ((projectedY[k1] - l2) * j6 >> 16) << 9) / 50;
				COSINE[l] = k + (l3 + ((projectedZ[k1] - l3) * j6 >> 16) << 9) / 50;
				HYPOT[l++] = l4 + ((shadeC[i] - l4) * j6 >> 16);
			}
		}
		if (j2 >= 50) {
			SINE[l] = projectedVertexX[k1];
			COSINE[l] = projectedVertexY[k1];
			HYPOT[l++] = shadeC[i];
		} else {
			int i3 = projectedY[k1];
			int i4 = projectedZ[k1];
			int i5 = shadeC[i];
			if (i2 >= 50) {
				int k6 = (50 - j2) * reciprocalTable[i2 - j2];
				SINE[l] = j + (i3 + ((projectedY[j1] - i3) * k6 >> 16) << 9) / 50;
				COSINE[l] = k + (i4 + ((projectedZ[j1] - i4) * k6 >> 16) << 9) / 50;
				HYPOT[l++] = i5 + ((shadeB[i] - i5) * k6 >> 16);
			}
			if (l1 >= 50) {
				int l6 = (50 - j2) * reciprocalTable[l1 - j2];
				SINE[l] = j + (i3 + ((projectedY[i1] - i3) * l6 >> 16) << 9) / 50;
				COSINE[l] = k + (i4 + ((projectedZ[i1] - i4) * l6 >> 16) << 9) / 50;
				HYPOT[l++] = i5 + ((shadeA[i] - i5) * l6 >> 16);
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
				if (faceRenderTypes == null) {
					l7 = 0;
				} else {
					l7 = faceRenderTypes[i] & 3;
				}
				if (l7 == 0) {
					Texture.drawGouraudTriangle(i7, j7, k7, j3, j4, j5, HYPOT[0], HYPOT[1], HYPOT[2]);
				} else if (l7 == 1) {
					Texture.drawFlatTriangle(i7, j7, k7, j3, j4, j5, brightnessTable[shadeA[i]]);
				} else if (l7 == 2) {
					int j8 = faceRenderTypes[i] >> 2;
					int k9 = texTriangleX[j8];
					int k10 = texTriangleY[j8];
					int k11 = texTriangleZ[j8];
					Texture.drawTexturedTriangle(i7, j7, k7, j3, j4, j5, HYPOT[0], HYPOT[1], HYPOT[2], projectedY[k9], projectedY[k10], projectedY[k11], projectedZ[k9], projectedZ[k10], projectedZ[k11], depthList[k9], depthList[k10], depthList[k11], faceColor[i]);
				} else if (l7 == 3) {
					int k8 = faceRenderTypes[i] >> 2;
					int l9 = texTriangleX[k8];
					int l10 = texTriangleY[k8];
					int l11 = texTriangleZ[k8];
					Texture.drawTexturedTriangle(i7, j7, k7, j3, j4, j5, shadeA[i], shadeA[i], shadeA[i], projectedY[l9], projectedY[l10], projectedY[l11], projectedZ[l9], projectedZ[l10], projectedZ[l11], depthList[l9], depthList[l10], depthList[l11], faceColor[i]);
				}
			}
			if (l == 4) {
				if (j3 < 0 || j4 < 0 || j5 < 0 || j3 > DrawingArea.centerX || j4 > DrawingArea.centerX || j5 > DrawingArea.centerX || SINE[3] < 0 || SINE[3] > DrawingArea.centerX) {
					Texture.clip = true;
				}
				int i8;
				if (faceRenderTypes == null) {
					i8 = 0;
				} else {
					i8 = faceRenderTypes[i] & 3;
				}
				if (i8 == 0) {
					Texture.drawGouraudTriangle(i7, j7, k7, j3, j4, j5, HYPOT[0], HYPOT[1], HYPOT[2]);
					Texture.drawGouraudTriangle(i7, k7, COSINE[3], j3, j5, SINE[3], HYPOT[0], HYPOT[2], HYPOT[3]);
					return;
				}
				if (i8 == 1) {
					int l8 = brightnessTable[shadeA[i]];
					Texture.drawFlatTriangle(i7, j7, k7, j3, j4, j5, l8);
					Texture.drawFlatTriangle(i7, k7, COSINE[3], j3, j5, SINE[3], l8);
					return;
				}
				if (i8 == 2) {
					int i9 = faceRenderTypes[i] >> 2;
					int i10 = texTriangleX[i9];
					int i11 = texTriangleY[i9];
					int i12 = texTriangleZ[i9];
					Texture.drawTexturedTriangle(i7, j7, k7, j3, j4, j5, HYPOT[0], HYPOT[1], HYPOT[2], projectedY[i10], projectedY[i11], projectedY[i12], projectedZ[i10], projectedZ[i11], projectedZ[i12], depthList[i10], depthList[i11], depthList[i12], faceColor[i]);
					Texture.drawTexturedTriangle(i7, k7, COSINE[3], j3, j5, SINE[3], HYPOT[0], HYPOT[2], HYPOT[3], projectedY[i10], projectedY[i11], projectedY[i12], projectedZ[i10], projectedZ[i11], projectedZ[i12], depthList[i10], depthList[i11], depthList[i12], faceColor[i]);
					return;
				}
				if (i8 == 3) {
					int j9 = faceRenderTypes[i] >> 2;
					int j10 = texTriangleX[j9];
					int j11 = texTriangleY[j9];
					int j12 = texTriangleZ[j9];
					Texture.drawTexturedTriangle(i7, j7, k7, j3, j4, j5, shadeA[i], shadeA[i], shadeA[i], projectedY[j10], projectedY[j11], projectedY[j12], projectedZ[j10], projectedZ[j11], projectedZ[j12], depthList[j10], depthList[j11], depthList[j12], faceColor[i]);
					Texture.drawTexturedTriangle(i7, k7, COSINE[3], j3, j5, SINE[3], shadeA[i], shadeA[i], shadeA[i], projectedY[j10], projectedY[j11], projectedY[j12], projectedZ[j10], projectedZ[j11], projectedZ[j12], depthList[j10], depthList[j11], depthList[j12], faceColor[i]);
				}
			}
		}
	}

        private boolean isTriangleVisible(int i, int j, int k, int l, int i1, int j1, int k1, int l1) {
		if (j < k && j < l && j < i1) {
			return false;
		}
		if (j > k && j > l && j > i1) {
			return false;
		}
		return !(i < j1 && i < k1 && i < l1) && (i <= j1 || i <= k1 || i <= l1);
	}

        public static final Model placeholderModel = new Model();
        private static int[] tempVertexX = new int[2000];
        private static int[] tempVertexY = new int[2000];
        private static int[] tempVertexZ = new int[2000];
        private static int[] tempFaceTextures = new int[2000];
        public int vertexCount;
	public int vertexX[];
	public int vertexY[];
	public int vertexZ[];
        public int faceCount;
	public int faceA[];
	public int faceB[];
	public int faceC[];
        private int[] shadeA;
        private int[] shadeB;
        private int[] shadeC;
        public int faceRenderTypes[];
        private int[] facePriorities;
        private int[] faceAlphas;
	public int faceColor[];
        private int defaultPriority;
        private int texturedTriangleCount;
        private int[] texTriangleX;
        private int[] texTriangleY;
        private int[] texTriangleZ;
	public int minX;
	public int maxX;
	public int maxZ;
	public int minZ;
	public int boundingRadius;
	public int maxY;
	private int diagonal3D;
	private int diagonal2D;
        public int overrideHeight;
	private int[] vertexSkins;
	private int[] faceSkins;
	public int vertexGroups[][];
	public int faceGroups[][];
        public boolean pickable;
        public VertexNormal vertexNormalTemp[];
        private static ModelHeader[] modelHeaderCache;
        private static OnDemandFetcherParent modelFetcherParent;
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
	private static int transformX;
	private static int transformY;
	private static int transformZ;
	public static boolean withinViewport;
        public static int viewportCenterX;
        public static int viewportCenterY;
        public static int queueLength;
        public static final int[] faceQueue = new int[1000];
        public static int sineTable[];
        public static int cosineTable[];
        private static int[] brightnessTable;
        private static int[] reciprocalTable;

        static {
                sineTable = Texture.sineTable;
                cosineTable = Texture.cosineTable;
                brightnessTable = Texture.brightnessTable;
                reciprocalTable = Texture.reciprocal16;
        }
}
