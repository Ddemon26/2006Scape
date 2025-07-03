package com.rs2.domain;

// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

final class WorldController {
        public static int drawDistance = 25;

        public WorldController(int[][][] heights) {
                int regionHeight = 104; // was parameter
                int regionWidth = 104; // was parameter
                int planeLevels = 4; // was parameter
                aBoolean434 = true;
        sceneObjectCache = new SceneObject[5000];
                vertexVisitA = new int[10000];
                vertexVisitB = new int[10000];
                planeCount = planeLevels;
                worldWidth = regionWidth;
                worldHeight = regionHeight;
                groundArray = new Ground[planeLevels][regionWidth][regionHeight];
                tileVisibility = new int[planeLevels][regionWidth + 1][regionHeight + 1];
                tileHeights = heights;
                initToNull();
        }

	public static void nullLoader() {
                aClass28Array462 = null;
                cullingClusterCounts = null;
		aCullingClusters = null;
		aClass19_477 = null;
		aBooleanArrayArrayArrayArray491 = null;
		aBooleanArrayArray492 = null;
	}

	public void initToNull() {
		for (int j = 0; j < planeCount; j++) {
			for (int k = 0; k < worldWidth; k++) {
				for (int i1 = 0; i1 < worldHeight; i1++) {
					groundArray[j][k][i1] = null;
				}

			}

		}
                for (int l = 0; l < CLUSTER_PLANES; l++) {
                        for (int j1 = 0; j1 < cullingClusterCounts[l]; j1++) {
                                aCullingClusters[l][j1] = null;
                        }

                        cullingClusterCounts[l] = 0;
                }

                for (int k1 = 0; k1 < sceneObjectCachePos; k1++) {
                        sceneObjectCache[k1] = null;
                }

                sceneObjectCachePos = 0;
		for (int l1 = 0; l1 < aClass28Array462.length; l1++) {
			aClass28Array462[l1] = null;
		}

	}

	public void setActivePlane(int i) {
		activePlane = i;
		for (int k = 0; k < worldWidth; k++) {
			for (int l = 0; l < worldHeight; l++) {
				if (groundArray[i][k][l] == null) {
					groundArray[i][k][l] = new Ground(i, k, l);
				}
			}

		}

	}

	public void shiftDownPlanes(int i, int j) {
		Ground class30_sub3 = groundArray[0][j][i];
		for (int l = 0; l < 3; l++) {
			Ground class30_sub3_1 = groundArray[l][j][i] = groundArray[l + 1][j][i];
			if (class30_sub3_1 != null) {
                                class30_sub3_1.plane--;
                                for (int j1 = 0; j1 < class30_sub3_1.sceneObjectCount; j1++) {
                                        SceneObject sceneObject = class30_sub3_1.obj5Array[j1];
                                        if ((sceneObject.uid >> 29 & 3) == 2 && sceneObject.startX == j && sceneObject.startY == i) {
                                                sceneObject.plane--;
                                        }
                                }

			}
		}
		if (groundArray[0][j][i] == null) {
			groundArray[0][j][i] = new Ground(0, j, i);
		}
                groundArray[0][j][i].linkedTile = class30_sub3;
		groundArray[3][j][i] = null;
	}

        public static void addCullingCluster(int i, int j, int k, int l, int i1, int j1, int l1, int i2) {
                CullingCluster cluster = new CullingCluster();
                cluster.minTileX = j / 128;
                cluster.maxTileX = l / 128;
                cluster.minTileZ = l1 / 128;
                cluster.maxTileZ = i1 / 128;
                cluster.type = i2;
                cluster.minX = j;
                cluster.maxX = l;
                cluster.minZ = l1;
                cluster.maxZ = i1;
                cluster.minY = j1;
                cluster.maxY = k;
                aCullingClusters[i][cullingClusterCounts[i]++] = cluster;
        }

        public void setGroundFlag(int i, int j, int k, int l) {
		Ground class30_sub3 = groundArray[i][j][k];
                if (class30_sub3 != null) {
                        groundArray[i][j][k].groundFlag = l;
                }
	}

        public void addTile(int i, int j, int k, int l, int i1, int j1, int k1, int l1, int i2, int j2, int k2, int l2, int i3, int j3, int k3, int l3, int i4, int j4, int k4, int l4) {
		if (l == 0) {
                    PlainTile class43 = new PlainTile(k2, l2, i3, j3, -1, k4, false);
			for (int i5 = i; i5 >= 0; i5--) {
				if (groundArray[i5][j][k] == null) {
					groundArray[i5][j][k] = new Ground(i5, j, k);
				}
			}

                        groundArray[i][j][k].plainTile = class43;
			return;
		}
		if (l == 1) {
                    PlainTile class43_1 = new PlainTile(k3, l3, i4, j4, j1, l4, k1 == l1 && k1 == i2 && k1 == j2);
			for (int j5 = i; j5 >= 0; j5--) {
				if (groundArray[j5][j][k] == null) {
					groundArray[j5][j][k] = new Ground(j5, j, k);
				}
			}

                        groundArray[i][j][k].plainTile = class43_1;
			return;
		}
                ShapedTile shapedTile = new ShapedTile(k, k3, j3, i2, j1, i4, i1, k2, k4, i3, j2, l1, k1, l, j4, l3, l2, j, l4);
                for (int k5 = i; k5 >= 0; k5--) {
                        if (groundArray[k5][j][k] == null) {
                                groundArray[k5][j][k] = new Ground(k5, j, k);
                        }
                }

                groundArray[i][j][k].shapedTile = shapedTile;
        }

       public void addTileDecoration(int plane, int height, int tileY, Animable renderable, byte config, int uid, int tileX) {
                if (renderable == null) {
                        return;
                }
            TileDecoration class49 = new TileDecoration();
                class49.renderable = renderable;
                class49.x = tileX * 128 + 64;
                class49.y = tileY * 128 + 64;
                class49.tileHeight = height;
                class49.uid = uid;
                class49.config = config;
                if (groundArray[plane][tileX][tileY] == null) {
                        groundArray[plane][tileX][tileY] = new Ground(plane, tileX, tileY);
                }
                groundArray[plane][tileX][tileY].obj3 = class49;
        }

       public void addItemPile(int i, int j, Animable class30_sub2_sub4, int k, Animable class30_sub2_sub4_1, Animable class30_sub2_sub4_2, int l, int i1) {
                ItemPile itemPile = new ItemPile();
                itemPile.topItem = class30_sub2_sub4_2;
                itemPile.x = i * 128 + 64;
                itemPile.y = i1 * 128 + 64;
                itemPile.height = k;
                itemPile.uid = j;
                itemPile.secondItem = class30_sub2_sub4;
                itemPile.thirdItem = class30_sub2_sub4_1;
		int j1 = 0;
                Ground class30_sub3 = groundArray[l][i][i1];
                if (class30_sub3 != null) {
                        for (int k1 = 0; k1 < class30_sub3.sceneObjectCount; k1++) {
                                if (class30_sub3.obj5Array[k1].renderable instanceof Model) {
                                        int l1 = ((Model) class30_sub3.obj5Array[k1].renderable).anInt1654;
					if (l1 > j1) {
						j1 = l1;
					}
				}
			}

		}
                itemPile.offsetY = j1;
                if (groundArray[l][i][i1] == null) {
                        groundArray[l][i][i1] = new Ground(l, i, i1);
                }
                groundArray[l][i][i1].itemPile = itemPile;
	}

       public void addBoundaryObject(int i, Animable class30_sub2_sub4, int j, int k, byte byte0, int l, Animable class30_sub2_sub4_1, int i1, int j1, int k1) {
		if (class30_sub2_sub4 == null && class30_sub2_sub4_1 == null) {
			return;
		}
                BoundaryObject object1 = new BoundaryObject();
                object1.uid = j;
                object1.config = byte0;
                object1.x = l * 128 + 64;
                object1.y = k * 128 + 64;
                object1.plane = i1;
                object1.primary = class30_sub2_sub4;
                object1.secondary = class30_sub2_sub4_1;
                object1.orientation = i;
                object1.orientation2 = j1;
		for (int l1 = k1; l1 >= 0; l1--) {
			if (groundArray[l1][l][k] == null) {
				groundArray[l1][l][k] = new Ground(l1, l, k);
			}
		}

		groundArray[k1][l][k].obj1 = object1;
	}

       public void addWallDecoration(int i, int j, int k, int i1, int j1, int k1, Animable class30_sub2_sub4, int l1, byte byte0, int i2, int j2) {
		if (class30_sub2_sub4 == null) {
			return;
		}
                WallDecoration decoration = new WallDecoration();
                decoration.uid = i;
                decoration.config = byte0;
                decoration.x = l1 * 128 + 64 + j1;
                decoration.y = j * 128 + 64 + i2;
                decoration.plane = k1;
                decoration.renderable = class30_sub2_sub4;
                decoration.orientationFlags = j2;
                decoration.orientation = k;
                for (int k2 = i1; k2 >= 0; k2--) {
                        if (groundArray[k2][l1][j] == null) {
                                groundArray[k2][l1][j] = new Ground(k2, l1, j);
                        }
                }

                groundArray[i1][l1][j].obj2 = decoration;
        }

       public boolean addGameObject(int i, byte byte0, int j, int k, Animable class30_sub2_sub4, int l, int i1, int j1, int k1, int l1) {
		if (class30_sub2_sub4 == null) {
			return true;
		} else {
			int i2 = l1 * 128 + 64 * l;
			int j2 = k1 * 128 + 64 * k;
			return addSceneObject(i1, l1, k1, l, k, i2, j2, j, class30_sub2_sub4, j1, false, i, byte0);
		}
	}

       public boolean addAnimableObject(int i, int j, int k, int l, int i1, int j1, int k1, Animable class30_sub2_sub4, boolean flag) {
		if (class30_sub2_sub4 == null) {
			return true;
		}
		int l1 = k1 - j1;
		int i2 = i1 - j1;
		int j2 = k1 + j1;
		int k2 = i1 + j1;
		if (flag) {
			if (j > 640 && j < 1408) {
				k2 += 128;
			}
			if (j > 1152 && j < 1920) {
				j2 += 128;
			}
			if (j > 1664 || j < 384) {
				i2 -= 128;
			}
			if (j > 128 && j < 896) {
				l1 -= 128;
			}
		}
		l1 /= 128;
		i2 /= 128;
		j2 /= 128;
		k2 /= 128;
		return addSceneObject(i, l1, i2, j2 - l1 + 1, k2 - i2 + 1, k1, i1, k, class30_sub2_sub4, j, true, l, (byte) 0);
	}

       public boolean addAnimatingObject(int j, int k, Animable class30_sub2_sub4, int l, int i1, int j1, int k1, int l1, int i2, int j2, int k2) {
		return class30_sub2_sub4 == null || addSceneObject(j, l1, k2, i2 - l1 + 1, i1 - k2 + 1, j1, k, k1, class30_sub2_sub4, l, true, j2, (byte) 0);
	}

	private boolean addSceneObject(int i, int j, int k, int l, int i1, int j1, int k1, int l1, Animable class30_sub2_sub4, int i2, boolean flag, int j2, byte byte0) {
		for (int k2 = j; k2 < j + l; k2++) {
			for (int l2 = k; l2 < k + i1; l2++) {
				if (k2 < 0 || l2 < 0 || k2 >= worldWidth || l2 >= worldHeight) {
					return false;
				}
				Ground class30_sub3 = groundArray[i][k2][l2];
                                if (class30_sub3 != null && class30_sub3.sceneObjectCount >= 5) {
					return false;
				}
			}

		}

                SceneObject sceneObject = new SceneObject();
                sceneObject.uid = j2;
                sceneObject.config = byte0;
                sceneObject.plane = i;
                sceneObject.x = j1;
                sceneObject.y = k1;
                sceneObject.height = l1;
                sceneObject.renderable = class30_sub2_sub4;
                sceneObject.orientation = i2;
                sceneObject.startX = j;
                sceneObject.startY = k;
                sceneObject.endX = j + l - 1;
                sceneObject.endY = k + i1 - 1;
		for (int i3 = j; i3 < j + l; i3++) {
			for (int j3 = k; j3 < k + i1; j3++) {
				int k3 = 0;
				if (i3 > j) {
					k3++;
				}
				if (i3 < j + l - 1) {
					k3 += 4;
				}
				if (j3 > k) {
					k3 += 8;
				}
				if (j3 < k + i1 - 1) {
					k3 += 2;
				}
				for (int l3 = i; l3 >= 0; l3--) {
					if (groundArray[l3][i3][j3] == null) {
						groundArray[l3][i3][j3] = new Ground(l3, i3, j3);
					}
				}

                                Ground class30_sub3_1 = groundArray[i][i3][j3];
                                class30_sub3_1.obj5Array[class30_sub3_1.sceneObjectCount] = sceneObject;
                                class30_sub3_1.sceneObjectFlags[class30_sub3_1.sceneObjectCount] = k3;
                                class30_sub3_1.combinedFlags |= k3;
                                class30_sub3_1.sceneObjectCount++;
                        }

		}

                if (flag) {
                        sceneObjectCache[sceneObjectCachePos++] = sceneObject;
                }
		return true;
	}

        public void clearObj5Cache() {
                for (int i = 0; i < sceneObjectCachePos; i++) {
                        SceneObject object = sceneObjectCache[i];
                        removeSceneObject(object);
                        sceneObjectCache[i] = null;
                }

            sceneObjectCachePos = 0;
	}

        private void removeSceneObject(SceneObject sceneObject) {
                for (int j = sceneObject.startX; j <= sceneObject.endX; j++) {
                        for (int k = sceneObject.startY; k <= sceneObject.endY; k++) {
                                Ground class30_sub3 = groundArray[sceneObject.plane][j][k];
                                if (class30_sub3 != null) {
                                        for (int l = 0; l < class30_sub3.sceneObjectCount; l++) {
                                                if (class30_sub3.obj5Array[l] != sceneObject) {
                                                        continue;
                                                }
                                                class30_sub3.sceneObjectCount--;
                                                for (int i1 = l; i1 < class30_sub3.sceneObjectCount; i1++) {
                                                        class30_sub3.obj5Array[i1] = class30_sub3.obj5Array[i1 + 1];
                                                        class30_sub3.sceneObjectFlags[i1] = class30_sub3.sceneObjectFlags[i1 + 1];
                                                }

                                                class30_sub3.obj5Array[class30_sub3.sceneObjectCount] = null;
                                                break;
                                        }

                                        class30_sub3.combinedFlags = 0;
                                        for (int j1 = 0; j1 < class30_sub3.sceneObjectCount; j1++) {
                                                class30_sub3.combinedFlags |= class30_sub3.sceneObjectFlags[j1];
                                        }

				}
			}

		}

	}

       public void updateWallDecorationPosition(int i, int k, int l, int i1) {
		Ground class30_sub3 = groundArray[i1][l][i];
		if (class30_sub3 == null) {
			return;
		}
                WallDecoration decoration = class30_sub3.obj2;
                if (decoration != null) {
                        int j1 = l * 128 + 64;
                        int k1 = i * 128 + 64;
                        decoration.x = j1 + (decoration.x - j1) * k / 16;
                        decoration.y = k1 + (decoration.y - k1) * k / 16;
                }
        }

        public void clearBoundaryObject(int i, int j, int k, byte byte0) {
		Ground class30_sub3 = groundArray[j][i][k];
		if (byte0 != -119) {
			aBoolean434 = !aBoolean434;
		}
		if (class30_sub3 != null) {
			class30_sub3.obj1 = null;
		}
	}

        public void clearWallDecoration(int j, int k, int l) {
		Ground class30_sub3 = groundArray[k][l][j];
		if (class30_sub3 != null) {
			class30_sub3.obj2 = null;
		}
	}

        public void removeSceneObject(int i, int k, int l) {
		Ground class30_sub3 = groundArray[i][k][l];
		if (class30_sub3 == null) {
			return;
		}
                for (int j1 = 0; j1 < class30_sub3.sceneObjectCount; j1++) {
                        SceneObject sceneObject = class30_sub3.obj5Array[j1];
                        if ((sceneObject.uid >> 29 & 3) == 2 && sceneObject.startX == k && sceneObject.startY == l) {
                                removeSceneObject(sceneObject);
                                return;
                        }
                }

	}

        public void clearTileDecoration(int i, int j, int k) {
		Ground class30_sub3 = groundArray[i][k][j];
		if (class30_sub3 == null) {
			return;
		}
		class30_sub3.obj3 = null;
	}

        public void clearItemPile(int i, int j, int k) {
		Ground class30_sub3 = groundArray[i][j][k];
		if (class30_sub3 != null) {
                        class30_sub3.itemPile = null;
		}
	}

       public BoundaryObject getBoundaryObject(int i, int j, int k) {
		Ground class30_sub3 = groundArray[i][j][k];
		if (class30_sub3 == null) {
			return null;
		} else {
                        return class30_sub3.obj1;
		}
	}

       public WallDecoration getWallDecoration(int i, int k, int l) {
                Ground class30_sub3 = groundArray[l][i][k];
                if (class30_sub3 == null) {
                        return null;
                } else {
                        return class30_sub3.obj2;
                }
	}

       public SceneObject getSceneObject(int i, int j, int k) {
                Ground class30_sub3 = groundArray[k][i][j];
                if (class30_sub3 == null) {
                        return null;
                }
                for (int l = 0; l < class30_sub3.sceneObjectCount; l++) {
                        SceneObject sceneObject = class30_sub3.obj5Array[l];
                        if ((sceneObject.uid >> 29 & 3) == 2 && sceneObject.startX == i && sceneObject.startY == j) {
                                return sceneObject;
                        }
                }
                return null;
        }

   public TileDecoration getTileDecoration(int i, int j, int k) {
		Ground class30_sub3 = groundArray[k][j][i];
		if (class30_sub3 == null || class30_sub3.obj3 == null) {
			return null;
		} else {
			return class30_sub3.obj3;
		}
	}

       public int getBoundaryObjectUid(int i, int j, int k) {
		Ground class30_sub3 = groundArray[i][j][k];
		if (class30_sub3 == null || class30_sub3.obj1 == null) {
			return 0;
		} else {
			return class30_sub3.obj1.uid;
		}
	}

       public int getWallDecorationUid(int i, int j, int l) {
		Ground class30_sub3 = groundArray[i][j][l];
		if (class30_sub3 == null || class30_sub3.obj2 == null) {
			return 0;
		} else {
			return class30_sub3.obj2.uid;
		}
	}

       public int getSceneObjectUid(int i, int j, int k) {
		Ground class30_sub3 = groundArray[i][j][k];
		if (class30_sub3 == null) {
			return 0;
		}
                for (int l = 0; l < class30_sub3.sceneObjectCount; l++) {
                        SceneObject sceneObject = class30_sub3.obj5Array[l];
                        if ((sceneObject.uid >> 29 & 3) == 2 && sceneObject.startX == j && sceneObject.startY == k) {
                                return sceneObject.uid;
                        }
                }

		return 0;
	}

       public int getTileDecorationUid(int i, int j, int k) {
		Ground class30_sub3 = groundArray[i][j][k];
		if (class30_sub3 == null || class30_sub3.obj3 == null) {
			return 0;
		} else {
			return class30_sub3.obj3.uid;
		}
	}

       public int getObjectConfig(int i, int j, int k, int l) {
		Ground class30_sub3 = groundArray[i][j][k];
		if (class30_sub3 == null) {
			return -1;
		}
		if (class30_sub3.obj1 != null && class30_sub3.obj1.uid == l) {
			return class30_sub3.obj1.config & 0xff;
		}
                if (class30_sub3.obj2 != null && class30_sub3.obj2.uid == l) {
                        return class30_sub3.obj2.config & 0xff;
                }
                if (class30_sub3.obj3 != null && class30_sub3.obj3.uid == l) {
                        return class30_sub3.obj3.config & 0xff;
                }
                for (int i1 = 0; i1 < class30_sub3.sceneObjectCount; i1++) {
                        if (class30_sub3.obj5Array[i1].uid == l) {
                                return class30_sub3.obj5Array[i1].config & 0xff;
                        }
                }

		return -1;
	}

       public void applySceneLighting(int i, int k, int i1) {
		int j = 64;// was parameter
		int l = 768;// was parameter
		int j1 = (int) Math.sqrt(k * k + i * i + i1 * i1);
		int k1 = l * j1 >> 8;
		for (int l1 = 0; l1 < planeCount; l1++) {
			for (int i2 = 0; i2 < worldWidth; i2++) {
				for (int j2 = 0; j2 < worldHeight; j2++) {
					Ground class30_sub3 = groundArray[l1][i2][j2];
					if (class30_sub3 != null) {
						BoundaryObject class10 = class30_sub3.obj1;
						if (class10 != null && class10.primary != null && class10.primary.vertexNormals != null) {
                                                       blendModels(l1, 1, 1, i2, j2, (Model) class10.primary);
							if (class10.secondary != null && class10.secondary.vertexNormals != null) {
                                                               blendModels(l1, 1, 1, i2, j2, (Model) class10.secondary);
                                                               mergeNormals((Model) class10.primary, (Model) class10.secondary, 0, 0, 0, false);
								((Model) class10.secondary).applyShading(j, k1, k, i, i1);
							}
							((Model) class10.primary).applyShading(j, k1, k, i, i1);
						}
                                                for (int k2 = 0; k2 < class30_sub3.sceneObjectCount; k2++) {
                                                        SceneObject sceneObject = class30_sub3.obj5Array[k2];
                                                        if (sceneObject != null && sceneObject.renderable != null && sceneObject.renderable.vertexNormals != null) {
                                                                blendModels(l1, sceneObject.endX - sceneObject.startX + 1, sceneObject.endY - sceneObject.startY + 1, i2, j2, (Model) sceneObject.renderable);
                                                                ((Model) sceneObject.renderable).applyShading(j, k1, k, i, i1);
                                                        }
                                                }

                                            TileDecoration class49 = class30_sub3.obj3;
                                                if (class49 != null && class49.renderable.vertexNormals != null) {
                                                       blendDecorationLighting(i2, l1, (Model) class49.renderable, j2);
                                                        ((Model) class49.renderable).applyShading(j, k1, k, i, i1);
                                                }
					}
				}

			}

		}

	}

       private void blendDecorationLighting(int i, int j, Model model, int k) {
		if (i < worldWidth) {
			Ground class30_sub3 = groundArray[j][i + 1][k];
                        if (class30_sub3 != null && class30_sub3.obj3 != null && class30_sub3.obj3.renderable.vertexNormals != null) {
                                mergeNormals(model, (Model) class30_sub3.obj3.renderable, 128, 0, 0, true);
                        }
                }
                if (k < worldWidth) {
                        Ground class30_sub3_1 = groundArray[j][i][k + 1];
                        if (class30_sub3_1 != null && class30_sub3_1.obj3 != null && class30_sub3_1.obj3.renderable.vertexNormals != null) {
                                mergeNormals(model, (Model) class30_sub3_1.obj3.renderable, 0, 0, 128, true);
                        }
                }
                if (i < worldWidth && k < worldHeight) {
                        Ground class30_sub3_2 = groundArray[j][i + 1][k + 1];
                        if (class30_sub3_2 != null && class30_sub3_2.obj3 != null && class30_sub3_2.obj3.renderable.vertexNormals != null) {
                                mergeNormals(model, (Model) class30_sub3_2.obj3.renderable, 128, 0, 128, true);
                        }
                }
                if (i < worldWidth && k > 0) {
                        Ground class30_sub3_3 = groundArray[j][i + 1][k - 1];
                        if (class30_sub3_3 != null && class30_sub3_3.obj3 != null && class30_sub3_3.obj3.renderable.vertexNormals != null) {
                                mergeNormals(model, (Model) class30_sub3_3.obj3.renderable, 128, 0, -128, true);
                        }
                }
	}

        private void blendModels(int i, int j, int k, int l, int i1, Model model) {
		boolean flag = true;
		int j1 = l;
		int k1 = l + j;
		int l1 = i1 - 1;
		int i2 = i1 + k;
		for (int j2 = i; j2 <= i + 1; j2++) {
			if (j2 != planeCount) {
				for (int k2 = j1; k2 <= k1; k2++) {
					if (k2 >= 0 && k2 < worldWidth) {
						for (int l2 = l1; l2 <= i2; l2++) {
							if (l2 >= 0 && l2 < worldHeight && (!flag || k2 >= k1 || l2 >= i2 || l2 < i1 && k2 != l)) {
								Ground class30_sub3 = groundArray[j2][k2][l2];
								if (class30_sub3 != null) {
									int i3 = (tileHeights[j2][k2][l2] + tileHeights[j2][k2 + 1][l2] + tileHeights[j2][k2][l2 + 1] + tileHeights[j2][k2 + 1][l2 + 1]) / 4 - (tileHeights[i][l][i1] + tileHeights[i][l + 1][i1] + tileHeights[i][l][i1 + 1] + tileHeights[i][l + 1][i1 + 1]) / 4;
									BoundaryObject class10 = class30_sub3.obj1;
									if (class10 != null && class10.primary != null && class10.primary.vertexNormals != null) {
                                                mergeNormals(model, (Model) class10.primary, (k2 - l) * 128 + (1 - j) * 64, i3, (l2 - i1) * 128 + (1 - k) * 64, flag);
									}
									if (class10 != null && class10.secondary != null && class10.secondary.vertexNormals != null) {
                                                mergeNormals(model, (Model) class10.secondary, (k2 - l) * 128 + (1 - j) * 64, i3, (l2 - i1) * 128 + (1 - k) * 64, flag);
									}
                                                                        for (int j3 = 0; j3 < class30_sub3.sceneObjectCount; j3++) {
                                                                               SceneObject sceneObject = class30_sub3.obj5Array[j3];
                                                                               if (sceneObject != null && sceneObject.renderable != null && sceneObject.renderable.vertexNormals != null) {
                                                                               int k3 = sceneObject.endX - sceneObject.startX + 1;
                                                                               int l3 = sceneObject.endY - sceneObject.startY + 1;
                                                mergeNormals(model, (Model) sceneObject.renderable, (sceneObject.startX - l) * 128 + (k3 - j) * 64, i3, (sceneObject.startY - i1) * 128 + (l3 - k) * 64, flag);
                                                                               }
                                                                        }

								}
							}
						}

					}
				}

				j1--;
				flag = false;
			}
		}

	}

        private void mergeNormals(Model model, Model model_1, int i, int j, int k, boolean flag) {
		mergeCycleId++;
		int l = 0;
		int ai[] = model_1.vertexX;
		int i1 = model_1.anInt1626;
		for (int j1 = 0; j1 < model.anInt1626; j1++) {
			VertexNormal class33 = model.vertexNormals[j1];
			VertexNormal class33_1 = model.aVertexNormalArray1660[j1];
			if (class33_1.magnitude != 0) {
				int i2 = model.vertexY[j1] - j;
				if (i2 <= model_1.maxY) {
					int j2 = model.vertexX[j1] - i;
					if (j2 >= model_1.minX && j2 <= model_1.maxX) {
						int k2 = model.vertexZ[j1] - k;
						if (k2 >= model_1.minZ && k2 <= model_1.maxZ) {
							for (int l2 = 0; l2 < i1; l2++) {
								VertexNormal class33_2 = model_1.vertexNormals[l2];
								VertexNormal class33_3 = model_1.aVertexNormalArray1660[l2];
								if (j2 == ai[l2] && k2 == model_1.vertexZ[l2] && i2 == model_1.vertexY[l2] && class33_3.magnitude != 0) {
									class33.x += class33_3.x;
									class33.y += class33_3.y;
									class33.z += class33_3.z;
									class33.magnitude += class33_3.magnitude;
									class33_2.x += class33_1.x;
									class33_2.y += class33_1.y;
									class33_2.z += class33_1.z;
									class33_2.magnitude += class33_1.magnitude;
									l++;
									vertexVisitA[j1] = mergeCycleId;
									vertexVisitB[l2] = mergeCycleId;
								}
							}

						}
					}
				}
			}
		}

		if (l < 3 || !flag) {
			return;
		}
		for (int k1 = 0; k1 < model.anInt1630; k1++) {
			if (vertexVisitA[model.faceA[k1]] == mergeCycleId && vertexVisitA[model.faceB[k1]] == mergeCycleId && vertexVisitA[model.faceC[k1]] == mergeCycleId) {
				model.anIntArray1637[k1] = -1;
			}
		}

		for (int l1 = 0; l1 < model_1.anInt1630; l1++) {
			if (vertexVisitB[model_1.faceA[l1]] == mergeCycleId && vertexVisitB[model_1.faceB[l1]] == mergeCycleId && vertexVisitB[model_1.faceC[l1]] == mergeCycleId) {
				model_1.anIntArray1637[l1] = -1;
			}
		}

	}

        public void renderMinimapTile(int ai[], int i, int k, int l, int i1) {
		int j = 512;// was parameter
		Ground class30_sub3 = groundArray[k][l][i1];
		if (class30_sub3 == null) {
			return;
		}
                PlainTile class43 = class30_sub3.plainTile;
                if (class43 != null) {
                        int j1 = class43.orientation;
			if (j1 == 0) {
				return;
			}
			for (int k1 = 0; k1 < 4; k1++) {
				ai[i] = j1;
				ai[i + 1] = j1;
				ai[i + 2] = j1;
				ai[i + 3] = j1;
				i += j;
			}

			return;
		}
                ShapedTile shapedTile = class30_sub3.shapedTile;
                if (shapedTile == null) {
                        return;
                }
                int l1 = shapedTile.shape;
                int i2 = shapedTile.rotation;
                int j2 = shapedTile.baseColor;
                int k2 = shapedTile.shadeColor;
		int ai1[] = anIntArrayArray489[l1];
		int ai2[] = anIntArrayArray490[i2];
		int l2 = 0;
		if (j2 != 0) {
			for (int i3 = 0; i3 < 4; i3++) {
				ai[i] = ai1[ai2[l2++]] != 0 ? k2 : j2;
				ai[i + 1] = ai1[ai2[l2++]] != 0 ? k2 : j2;
				ai[i + 2] = ai1[ai2[l2++]] != 0 ? k2 : j2;
				ai[i + 3] = ai1[ai2[l2++]] != 0 ? k2 : j2;
				i += j;
			}

			return;
		}
		for (int j3 = 0; j3 < 4; j3++) {
			if (ai1[ai2[l2++]] != 0) {
				ai[i] = k2;
			}
			if (ai1[ai2[l2++]] != 0) {
				ai[i + 1] = k2;
			}
			if (ai1[ai2[l2++]] != 0) {
				ai[i + 2] = k2;
			}
			if (ai1[ai2[l2++]] != 0) {
				ai[i + 3] = k2;
			}
			i += j;
		}

	}

        public static void buildVisibilityMap(int i, int j, int k, int l, int ai[]) {
		viewportMinX = 0;
		viewportMinY = 0;
		viewportMaxX = k;
		viewportMaxY = l;
		halfViewportWidth = k / 2;
		halfViewportHeight = l / 2;
		boolean aflag[][][][] = new boolean[9][32][256][256];
		for (int i1 = 128; i1 <= 384; i1 += 32) {
			for (int j1 = 0; j1 < 2048; j1 += 64) {
				pitchSin = Model.modelIntArray1[i1];
				pitchCos = Model.modelIntArray2[i1];
				yawSin = Model.modelIntArray1[j1];
				yawCos = Model.modelIntArray2[j1];
				int l1 = (i1 - 128) / 32;
				int j2 = j1 / 64;
				for (int l2 = -(drawDistance + 1); l2 <= (drawDistance + 1); l2++) {
					for (int j3 = -(drawDistance + 1); j3 <= (drawDistance + 1); j3++) {
						int k3 = l2 * 128;
						int i4 = j3 * 128;
						boolean flag2 = false;
						for (int k4 = -i; k4 <= j; k4 += 128) {
                                                        if (!isPointInView(ai[l1] + k4, i4, k3)) {
								continue;
							}
							flag2 = true;
							break;
						}

						aflag[l1][j2][l2 + drawDistance + 1][j3 + drawDistance + 1] = flag2;
					}

				}

			}

		}

		for (int k1 = 0; k1 < 8; k1++) {
			for (int i2 = 0; i2 < 32; i2++) {
				for (int k2 = -drawDistance; k2 < drawDistance; k2++) {
					for (int i3 = -drawDistance; i3 < drawDistance; i3++) {
						boolean flag1 = false;
						label0 : for (int l3 = -1; l3 <= 1; l3++) {
							for (int j4 = -1; j4 <= 1; j4++) {
								if (aflag[k1][i2][k2 + l3 + drawDistance + 1][i3 + j4 + drawDistance + 1]) {
									flag1 = true;
								} else if (aflag[k1][(i2 + 1) % 31][k2 + l3 + drawDistance + 1][i3 + j4 + drawDistance + 1]) {
									flag1 = true;
								} else if (aflag[k1 + 1][i2][k2 + l3 + drawDistance + 1][i3 + j4 + drawDistance + 1]) {
									flag1 = true;
								} else {
									if (!aflag[k1 + 1][(i2 + 1) % 31][k2 + l3 + drawDistance + 1][i3 + j4 + drawDistance + 1]) {
										continue;
									}
									flag1 = true;
								}
								break label0;
							}

						}

						aBooleanArrayArrayArrayArray491[k1][i2][k2 + drawDistance][i3 + drawDistance] = flag1;
					}

				}

			}

		}

	}

        private static boolean isPointInView(int i, int j, int k) {
		int l = j * yawSin + k * yawCos >> 16;
		int i1 = j * yawCos - k * yawSin >> 16;
		int j1 = i * pitchSin + i1 * pitchCos >> 16;
		int k1 = i * pitchCos - i1 * pitchSin >> 16;
		if (j1 < 50 || j1 > 3500) {
			return false;
		}
		int l1 = halfViewportWidth + (l << 9) / j1;
		int i2 = halfViewportHeight + (k1 << 9) / j1;
		return l1 >= viewportMinX && l1 <= viewportMaxX && i2 >= viewportMinY && i2 <= viewportMaxY;
	}

        public void queueClick(int i, int j) {
                pendingClick = true;
                pendingClickX = j;
                pendingClickY = i;
                clickedTileX = -1;
                clickedTileY = -1;
	}

        public void renderScene(int i, int j, int k, int l, int i1, int j1) {
		if (i < 0) {
			i = 0;
		} else if (i >= worldWidth * 128) {
			i = worldWidth * 128 - 1;
		}
		if (j < 0) {
			j = 0;
		} else if (j >= worldHeight * 128) {
			j = worldHeight * 128 - 1;
		}
               renderCycle++;
		pitchSin = Model.modelIntArray1[j1];
		pitchCos = Model.modelIntArray2[j1];
		yawSin = Model.modelIntArray1[k];
		yawCos = Model.modelIntArray2[k];
		aBooleanArrayArray492 = aBooleanArrayArrayArrayArray491[(j1 - 128) / 32][k / 64];
		cameraX = i;
		cameraZ = l;
		cameraY = j;
		cameraTileX = i / 128;
		cameraTileY = j / 128;
               cameraPlane = i1;
		minVisibleX = cameraTileX - drawDistance;
		if (minVisibleX < 0) {
			minVisibleX = 0;
		}
		minVisibleY = cameraTileY - drawDistance;
		if (minVisibleY < 0) {
			minVisibleY = 0;
		}
		maxVisibleX = cameraTileX + drawDistance;
		if (maxVisibleX > worldWidth) {
			maxVisibleX = worldWidth;
		}
		maxVisibleY = cameraTileY + drawDistance;
		if (maxVisibleY > worldHeight) {
			maxVisibleY = worldHeight;
		}
		updateCullingClusters();
		visibleTileCount = 0;
		for (int k1 = activePlane; k1 < planeCount; k1++) {
			Ground aclass30_sub3[][] = groundArray[k1];
			for (int i2 = minVisibleX; i2 < maxVisibleX; i2++) {
				for (int k2 = minVisibleY; k2 < maxVisibleY; k2++) {
					Ground class30_sub3 = aclass30_sub3[i2][k2];
                                        if (class30_sub3 != null) {
                                                if (class30_sub3.groundFlag > i1 || !aBooleanArrayArray492[i2 - cameraTileX + drawDistance][k2 - cameraTileY + drawDistance] && tileHeights[k1][i2][k2] - l < 50) {
							class30_sub3.aBoolean1322 = false;
							class30_sub3.aBoolean1323 = false;
							class30_sub3.anInt1325 = 0;
						} else {
                                                        class30_sub3.aBoolean1322 = true;
                                                        class30_sub3.aBoolean1323 = true;
                                                        class30_sub3.aBoolean1324 = class30_sub3.sceneObjectCount > 0;
							visibleTileCount++;
						}
					}
				}

			}

		}

		for (int l1 = activePlane; l1 < planeCount; l1++) {
			Ground aclass30_sub3_1[][] = groundArray[l1];
			for (int l2 = -drawDistance; l2 <= 0; l2++) {
				int i3 = cameraTileX + l2;
				int k3 = cameraTileX - l2;
				if (i3 >= minVisibleX || k3 < maxVisibleX) {
					for (int i4 = -drawDistance; i4 <= 0; i4++) {
						int k4 = cameraTileY + i4;
						int i5 = cameraTileY - i4;
						if (i3 >= minVisibleX) {
							if (k4 >= minVisibleY) {
								Ground class30_sub3_1 = aclass30_sub3_1[i3][k4];
								if (class30_sub3_1 != null && class30_sub3_1.aBoolean1322) {
									processTile(class30_sub3_1, true);
								}
							}
							if (i5 < maxVisibleY) {
								Ground class30_sub3_2 = aclass30_sub3_1[i3][i5];
								if (class30_sub3_2 != null && class30_sub3_2.aBoolean1322) {
									processTile(class30_sub3_2, true);
								}
							}
						}
						if (k3 < maxVisibleX) {
							if (k4 >= minVisibleY) {
								Ground class30_sub3_3 = aclass30_sub3_1[k3][k4];
								if (class30_sub3_3 != null && class30_sub3_3.aBoolean1322) {
									processTile(class30_sub3_3, true);
								}
							}
							if (i5 < maxVisibleY) {
								Ground class30_sub3_4 = aclass30_sub3_1[k3][i5];
								if (class30_sub3_4 != null && class30_sub3_4.aBoolean1322) {
									processTile(class30_sub3_4, true);
								}
							}
						}
						if (visibleTileCount == 0) {
                                                    pendingClick = false;
							return;
						}
					}

				}
			}

		}

		for (int j2 = activePlane; j2 < planeCount; j2++) {
			Ground aclass30_sub3_2[][] = groundArray[j2];
			for (int j3 = -drawDistance; j3 <= 0; j3++) {
				int l3 = cameraTileX + j3;
				int j4 = cameraTileX - j3;
				if (l3 >= minVisibleX || j4 < maxVisibleX) {
					for (int l4 = -drawDistance; l4 <= 0; l4++) {
						int j5 = cameraTileY + l4;
						int k5 = cameraTileY - l4;
						if (l3 >= minVisibleX) {
							if (j5 >= minVisibleY) {
								Ground class30_sub3_5 = aclass30_sub3_2[l3][j5];
								if (class30_sub3_5 != null && class30_sub3_5.aBoolean1322) {
									processTile(class30_sub3_5, false);
								}
							}
							if (k5 < maxVisibleY) {
								Ground class30_sub3_6 = aclass30_sub3_2[l3][k5];
								if (class30_sub3_6 != null && class30_sub3_6.aBoolean1322) {
									processTile(class30_sub3_6, false);
								}
							}
						}
						if (j4 < maxVisibleX) {
							if (j5 >= minVisibleY) {
								Ground class30_sub3_7 = aclass30_sub3_2[j4][j5];
								if (class30_sub3_7 != null && class30_sub3_7.aBoolean1322) {
									processTile(class30_sub3_7, false);
								}
							}
							if (k5 < maxVisibleY) {
								Ground class30_sub3_8 = aclass30_sub3_2[j4][k5];
								if (class30_sub3_8 != null && class30_sub3_8.aBoolean1322) {
									processTile(class30_sub3_8, false);
								}
							}
						}
						if (visibleTileCount == 0) {
                                                   pendingClick = false;
							return;
						}
					}

				}
			}

		}

           pendingClick = false;
	}

	private void processTile(Ground class30_sub3, boolean flag) {
		aClass19_477.insertHead(class30_sub3);
		do {
			Ground class30_sub3_1;
			do {
				class30_sub3_1 = (Ground) aClass19_477.popHead();
				if (class30_sub3_1 == null) {
					return;
				}
			} while (!class30_sub3_1.aBoolean1323);
                        int i = class30_sub3_1.x;
                        int j = class30_sub3_1.y;
                        int k = class30_sub3_1.plane;
                        int l = class30_sub3_1.basePlane;
			Ground aclass30_sub3[][] = groundArray[k];
			if (class30_sub3_1.aBoolean1322) {
				if (flag) {
					if (k > 0) {
						Ground class30_sub3_2 = groundArray[k - 1][i][j];
						if (class30_sub3_2 != null && class30_sub3_2.aBoolean1323) {
							continue;
						}
					}
					if (i <= cameraTileX && i > minVisibleX) {
						Ground class30_sub3_3 = aclass30_sub3[i - 1][j];
                                                if (class30_sub3_3 != null && class30_sub3_3.aBoolean1323 && (class30_sub3_3.aBoolean1322 || (class30_sub3_1.combinedFlags & 1) == 0)) {
							continue;
						}
					}
					if (i >= cameraTileX && i < maxVisibleX - 1) {
						Ground class30_sub3_4 = aclass30_sub3[i + 1][j];
                                                if (class30_sub3_4 != null && class30_sub3_4.aBoolean1323 && (class30_sub3_4.aBoolean1322 || (class30_sub3_1.combinedFlags & 4) == 0)) {
							continue;
						}
					}
					if (j <= cameraTileY && j > minVisibleY) {
						Ground class30_sub3_5 = aclass30_sub3[i][j - 1];
                                                if (class30_sub3_5 != null && class30_sub3_5.aBoolean1323 && (class30_sub3_5.aBoolean1322 || (class30_sub3_1.combinedFlags & 8) == 0)) {
							continue;
						}
					}
					if (j >= cameraTileY && j < maxVisibleY - 1) {
						Ground class30_sub3_6 = aclass30_sub3[i][j + 1];
                                                if (class30_sub3_6 != null && class30_sub3_6.aBoolean1323 && (class30_sub3_6.aBoolean1322 || (class30_sub3_1.combinedFlags & 2) == 0)) {
							continue;
						}
					}
				} else {
					flag = true;
				}
				class30_sub3_1.aBoolean1322 = false;
                                if (class30_sub3_1.linkedTile != null) {
                                        Ground class30_sub3_7 = class30_sub3_1.linkedTile;
                                        if (class30_sub3_7.plainTile != null) {
						if (!isTileVisible(0, i, j)) {
                                                        drawPlainTile(class30_sub3_7.plainTile, 0, pitchSin, pitchCos, yawSin, yawCos, i, j);
						}
                                } else if (class30_sub3_7.shapedTile != null && !isTileVisible(0, i, j)) {
                                                drawShapedTile(i, pitchSin, yawSin, class30_sub3_7.shapedTile, pitchCos, j, yawCos);
					}
					BoundaryObject class10 = class30_sub3_7.obj1;
					if (class10 != null) {
						class10.primary.render(0, pitchSin, pitchCos, yawSin, yawCos, class10.x - cameraX, class10.plane - cameraZ, class10.y - cameraY, class10.uid);
					}
                                        for (int i2 = 0; i2 < class30_sub3_7.sceneObjectCount; i2++) {
                                                SceneObject sceneObject = class30_sub3_7.obj5Array[i2];
                                                if (sceneObject != null) {
                                                        sceneObject.renderable.render(sceneObject.orientation, pitchSin, pitchCos, yawSin, yawCos, sceneObject.x - cameraX, sceneObject.height - cameraZ, sceneObject.y - cameraY, sceneObject.uid);
                                                }
                                        }

				}
				boolean flag1 = false;
                                if (class30_sub3_1.plainTile != null) {
					if (!isTileVisible(l, i, j)) {
						flag1 = true;
                                                drawPlainTile(class30_sub3_1.plainTile, l, pitchSin, pitchCos, yawSin, yawCos, i, j);
					}
                                } else if (class30_sub3_1.shapedTile != null && !isTileVisible(l, i, j)) {
                                        flag1 = true;
                                        drawShapedTile(i, pitchSin, yawSin, class30_sub3_1.shapedTile, pitchCos, j, yawCos);
				}
				int j1 = 0;
				int j2 = 0;
				BoundaryObject class10_3 = class30_sub3_1.obj1;
                                WallDecoration class26_1 = class30_sub3_1.obj2;
				if (class10_3 != null || class26_1 != null) {
					if (cameraTileX == i) {
						j1++;
					} else if (cameraTileX < i) {
						j1 += 2;
					}
					if (cameraTileY == j) {
						j1 += 3;
					} else if (cameraTileY > j) {
						j1 += 6;
					}
					j2 = anIntArray478[j1];
					class30_sub3_1.anInt1328 = anIntArray480[j1];
				}
				if (class10_3 != null) {
					if ((class10_3.orientation & anIntArray479[j1]) != 0) {
						if (class10_3.orientation == 16) {
							class30_sub3_1.anInt1325 = 3;
							class30_sub3_1.anInt1326 = anIntArray481[j1];
							class30_sub3_1.anInt1327 = 3 - class30_sub3_1.anInt1326;
						} else if (class10_3.orientation == 32) {
							class30_sub3_1.anInt1325 = 6;
							class30_sub3_1.anInt1326 = anIntArray482[j1];
							class30_sub3_1.anInt1327 = 6 - class30_sub3_1.anInt1326;
						} else if (class10_3.orientation == 64) {
							class30_sub3_1.anInt1325 = 12;
							class30_sub3_1.anInt1326 = anIntArray483[j1];
							class30_sub3_1.anInt1327 = 12 - class30_sub3_1.anInt1326;
						} else {
							class30_sub3_1.anInt1325 = 9;
							class30_sub3_1.anInt1326 = anIntArray484[j1];
							class30_sub3_1.anInt1327 = 9 - class30_sub3_1.anInt1326;
						}
					} else {
						class30_sub3_1.anInt1325 = 0;
					}
					if ((class10_3.orientation & j2) != 0 && !isWallVisible(l, i, j, class10_3.orientation)) {
						class10_3.primary.render(0, pitchSin, pitchCos, yawSin, yawCos, class10_3.x - cameraX, class10_3.plane - cameraZ, class10_3.y - cameraY, class10_3.uid);
					}
					if ((class10_3.orientation2 & j2) != 0 && !isWallVisible(l, i, j, class10_3.orientation2)) {
						class10_3.secondary.render(0, pitchSin, pitchCos, yawSin, yawCos, class10_3.x - cameraX, class10_3.plane - cameraZ, class10_3.y - cameraY, class10_3.uid);
					}
				}
                                if (class26_1 != null && !isWallDecorationVisible(l, i, j, class26_1.renderable.modelHeight)) {
                                        if ((class26_1.orientationFlags & j2) != 0) {
                                                class26_1.renderable.render(class26_1.orientation, pitchSin, pitchCos, yawSin, yawCos, class26_1.x - cameraX, class26_1.plane - cameraZ, class26_1.y - cameraY, class26_1.uid);
                                        } else if ((class26_1.orientationFlags & 0x300) != 0) {
                                                int j4 = class26_1.x - cameraX;
                                                int l5 = class26_1.plane - cameraZ;
                                                int k6 = class26_1.y - cameraY;
                                                int i8 = class26_1.orientation;
						int k9;
						if (i8 == 1 || i8 == 2) {
							k9 = -j4;
						} else {
							k9 = j4;
						}
						int k10;
						if (i8 == 2 || i8 == 3) {
							k10 = -k6;
						} else {
							k10 = k6;
						}
                                                if ((class26_1.orientationFlags & 0x100) != 0 && k10 < k9) {
                                                        int i11 = j4 + anIntArray463[i8];
                                                        int k11 = k6 + anIntArray464[i8];
                                                        class26_1.renderable.render(i8 * 512 + 256, pitchSin, pitchCos, yawSin, yawCos, i11, l5, k11, class26_1.uid);
                                                }
                                                if ((class26_1.orientationFlags & 0x200) != 0 && k10 > k9) {
                                                        int j11 = j4 + anIntArray465[i8];
                                                        int l11 = k6 + anIntArray466[i8];
                                                        class26_1.renderable.render(i8 * 512 + 1280 & 0x7ff, pitchSin, pitchCos, yawSin, yawCos, j11, l5, l11, class26_1.uid);
                                                }
                                        }
                                }
				if (flag1) {
                                   TileDecoration class49 = class30_sub3_1.obj3;
                                        if (class49 != null) {
                                                class49.renderable.render(0, pitchSin, pitchCos, yawSin, yawCos, class49.x - cameraX, class49.tileHeight - cameraZ, class49.y - cameraY, class49.uid);
                                        }
                                        ItemPile pile1 = class30_sub3_1.itemPile;
                                        if (pile1 != null && pile1.offsetY == 0) {
                                                if (pile1.secondItem != null) {
                                                        pile1.secondItem.render(0, pitchSin, pitchCos, yawSin, yawCos, pile1.x - cameraX, pile1.height - cameraZ, pile1.y - cameraY, pile1.uid);
                                                }
                                                if (pile1.thirdItem != null) {
                                                        pile1.thirdItem.render(0, pitchSin, pitchCos, yawSin, yawCos, pile1.x - cameraX, pile1.height - cameraZ, pile1.y - cameraY, pile1.uid);
                                                }
                                                if (pile1.topItem != null) {
                                                        pile1.topItem.render(0, pitchSin, pitchCos, yawSin, yawCos, pile1.x - cameraX, pile1.height - cameraZ, pile1.y - cameraY, pile1.uid);
                                                }
                                        }
				}
                                int k4 = class30_sub3_1.combinedFlags;
				if (k4 != 0) {
					if (i < cameraTileX && (k4 & 4) != 0) {
						Ground class30_sub3_17 = aclass30_sub3[i + 1][j];
						if (class30_sub3_17 != null && class30_sub3_17.aBoolean1323) {
							aClass19_477.insertHead(class30_sub3_17);
						}
					}
					if (j < cameraTileY && (k4 & 2) != 0) {
						Ground class30_sub3_18 = aclass30_sub3[i][j + 1];
						if (class30_sub3_18 != null && class30_sub3_18.aBoolean1323) {
							aClass19_477.insertHead(class30_sub3_18);
						}
					}
					if (i > cameraTileX && (k4 & 1) != 0) {
						Ground class30_sub3_19 = aclass30_sub3[i - 1][j];
						if (class30_sub3_19 != null && class30_sub3_19.aBoolean1323) {
							aClass19_477.insertHead(class30_sub3_19);
						}
					}
					if (j > cameraTileY && (k4 & 8) != 0) {
						Ground class30_sub3_20 = aclass30_sub3[i][j - 1];
						if (class30_sub3_20 != null && class30_sub3_20.aBoolean1323) {
							aClass19_477.insertHead(class30_sub3_20);
						}
					}
				}
			}
                        if (class30_sub3_1.anInt1325 != 0) {
                                boolean flag2 = true;
                                for (int k1 = 0; k1 < class30_sub3_1.sceneObjectCount; k1++) {
                                        if (class30_sub3_1.obj5Array[k1].lastDrawn == renderCycle || (class30_sub3_1.sceneObjectFlags[k1] & class30_sub3_1.anInt1325) != class30_sub3_1.anInt1326) {
                                                continue;
                                        }
					flag2 = false;
					break;
				}

				if (flag2) {
					BoundaryObject class10_1 = class30_sub3_1.obj1;
					if (!isWallVisible(l, i, j, class10_1.orientation)) {
						class10_1.primary.render(0, pitchSin, pitchCos, yawSin, yawCos, class10_1.x - cameraX, class10_1.plane - cameraZ, class10_1.y - cameraY, class10_1.uid);
					}
					class30_sub3_1.anInt1325 = 0;
				}
			}
			if (class30_sub3_1.aBoolean1324) {
				try {
                                        int i1 = class30_sub3_1.sceneObjectCount;
					class30_sub3_1.aBoolean1324 = false;
					int l1 = 0;
					label0 : for (int k2 = 0; k2 < i1; k2++) {
                                                SceneObject class28_1 = class30_sub3_1.obj5Array[k2];
                                                if (class28_1.lastDrawn == renderCycle) {
                                                        continue;
                                                }
                                                for (int k3 = class28_1.startX; k3 <= class28_1.endX; k3++) {
                                                        for (int l4 = class28_1.startY; l4 <= class28_1.endY; l4++) {
                                                                Ground class30_sub3_21 = aclass30_sub3[k3][l4];
                                                                if (class30_sub3_21.aBoolean1322) {
                                                                        class30_sub3_1.aBoolean1324 = true;
                                                                } else {
                                                                        if (class30_sub3_21.anInt1325 == 0) {
										continue;
									}
                                                                        int l6 = 0;
                                                                        if (k3 > class28_1.startX) {
                                                                                l6++;
                                                                        }
                                                                        if (k3 < class28_1.endX) {
                                                                                l6 += 4;
                                                                        }
                                                                        if (l4 > class28_1.startY) {
                                                                                l6 += 8;
                                                                        }
                                                                        if (l4 < class28_1.endY) {
                                                                                l6 += 2;
                                                                        }
                                                                        if ((l6 & class30_sub3_21.anInt1325) != class30_sub3_1.anInt1327) {
										continue;
									}
									class30_sub3_1.aBoolean1324 = true;
								}
								continue label0;
							}

						}

                                                aClass28Array462[l1++] = class28_1;
                                                int i5 = cameraTileX - class28_1.startX;
                                                int i6 = class28_1.endX - cameraTileX;
						if (i6 > i5) {
							i5 = i6;
						}
                                                int i7 = cameraTileY - class28_1.startY;
                                                int j8 = class28_1.endY - cameraTileY;
                                                if (j8 > i7) {
                                                        class28_1.distanceFromCamera = i5 + j8;
                                                } else {
                                                        class28_1.distanceFromCamera = i5 + i7;
                                                }
                                        }

					while (l1 > 0) {
						int i3 = -50;
						int l3 = -1;
						for (int j5 = 0; j5 < l1; j5++) {
                                                        SceneObject class28_2 = aClass28Array462[j5];
                                                        if (class28_2.lastDrawn != renderCycle) {
                                                                if (class28_2.distanceFromCamera > i3) {
                                                                        i3 = class28_2.distanceFromCamera;
                                                                        l3 = j5;
                                                                } else if (class28_2.distanceFromCamera == i3) {
                                                                        int j7 = class28_2.x - cameraX;
                                                                        int k8 = class28_2.y - cameraY;
                                                                        int l9 = aClass28Array462[l3].x - cameraX;
                                                                        int l10 = aClass28Array462[l3].y - cameraY;
                                                                        if (j7 * j7 + k8 * k8 > l9 * l9 + l10 * l10) {
                                                                                l3 = j5;
                                                                        }
                                                                }
                                                        }
                                                }

						if (l3 == -1) {
							break;
						}
                                                SceneObject class28_3 = aClass28Array462[l3];
                                                class28_3.lastDrawn = renderCycle;
                                                if (!isAreaVisible(l, class28_3.startX, class28_3.endX, class28_3.startY, class28_3.endY, class28_3.renderable.modelHeight)) {
                                                        class28_3.renderable.render(class28_3.orientation, pitchSin, pitchCos, yawSin, yawCos, class28_3.x - cameraX, class28_3.height - cameraZ, class28_3.y - cameraY, class28_3.uid);
                                                }
                                                for (int k7 = class28_3.startX; k7 <= class28_3.endX; k7++) {
                                                        for (int l8 = class28_3.startY; l8 <= class28_3.endY; l8++) {
                                                                Ground class30_sub3_22 = aclass30_sub3[k7][l8];
                                                                if (class30_sub3_22.anInt1325 != 0) {
                                                                        aClass19_477.insertHead(class30_sub3_22);
								} else if ((k7 != i || l8 != j) && class30_sub3_22.aBoolean1323) {
									aClass19_477.insertHead(class30_sub3_22);
								}
							}

						}

					}
					if (class30_sub3_1.aBoolean1324) {
						continue;
					}
				} catch (Exception _ex) {
					class30_sub3_1.aBoolean1324 = false;
				}
			}
			if (!class30_sub3_1.aBoolean1323 || class30_sub3_1.anInt1325 != 0) {
				continue;
			}
			if (i <= cameraTileX && i > minVisibleX) {
				Ground class30_sub3_8 = aclass30_sub3[i - 1][j];
				if (class30_sub3_8 != null && class30_sub3_8.aBoolean1323) {
					continue;
				}
			}
			if (i >= cameraTileX && i < maxVisibleX - 1) {
				Ground class30_sub3_9 = aclass30_sub3[i + 1][j];
				if (class30_sub3_9 != null && class30_sub3_9.aBoolean1323) {
					continue;
				}
			}
			if (j <= cameraTileY && j > minVisibleY) {
				Ground class30_sub3_10 = aclass30_sub3[i][j - 1];
				if (class30_sub3_10 != null && class30_sub3_10.aBoolean1323) {
					continue;
				}
			}
			if (j >= cameraTileY && j < maxVisibleY - 1) {
				Ground class30_sub3_11 = aclass30_sub3[i][j + 1];
				if (class30_sub3_11 != null && class30_sub3_11.aBoolean1323) {
					continue;
				}
			}
			class30_sub3_1.aBoolean1323 = false;
			visibleTileCount--;
                        ItemPile pile = class30_sub3_1.itemPile;
                        if (pile != null && pile.offsetY != 0) {
                                if (pile.secondItem != null) {
                                        pile.secondItem.render(0, pitchSin, pitchCos, yawSin, yawCos, pile.x - cameraX, pile.height - cameraZ - pile.offsetY, pile.y - cameraY, pile.uid);
                                }
                                if (pile.thirdItem != null) {
                                        pile.thirdItem.render(0, pitchSin, pitchCos, yawSin, yawCos, pile.x - cameraX, pile.height - cameraZ - pile.offsetY, pile.y - cameraY, pile.uid);
                                }
                                if (pile.topItem != null) {
                                        pile.topItem.render(0, pitchSin, pitchCos, yawSin, yawCos, pile.x - cameraX, pile.height - cameraZ - pile.offsetY, pile.y - cameraY, pile.uid);
                                }
                        }
			if (class30_sub3_1.anInt1328 != 0) {
                                WallDecoration class26 = class30_sub3_1.obj2;
                                if (class26 != null && !isWallDecorationVisible(l, i, j, class26.renderable.modelHeight)) {
                                        if ((class26.orientationFlags & class30_sub3_1.anInt1328) != 0) {
                                                class26.renderable.render(class26.orientation, pitchSin, pitchCos, yawSin, yawCos, class26.x - cameraX, class26.plane - cameraZ, class26.y - cameraY, class26.uid);
                                        } else if ((class26.orientationFlags & 0x300) != 0) {
                                                int l2 = class26.x - cameraX;
                                                int j3 = class26.plane - cameraZ;
                                                int i4 = class26.y - cameraY;
                                                int k5 = class26.orientation;
                                                int j6;
                                                if (k5 == 1 || k5 == 2) {
                                                        j6 = -l2;
                                                } else {
                                                        j6 = l2;
						}
						int l7;
						if (k5 == 2 || k5 == 3) {
							l7 = -i4;
						} else {
							l7 = i4;
						}
                                                if ((class26.orientationFlags & 0x100) != 0 && l7 >= j6) {
                                                        int i9 = l2 + anIntArray463[k5];
                                                        int i10 = i4 + anIntArray464[k5];
                                                        class26.renderable.render(k5 * 512 + 256, pitchSin, pitchCos, yawSin, yawCos, i9, j3, i10, class26.uid);
                                                }
                                                if ((class26.orientationFlags & 0x200) != 0 && l7 <= j6) {
                                                        int j9 = l2 + anIntArray465[k5];
                                                        int j10 = i4 + anIntArray466[k5];
                                                        class26.renderable.render(k5 * 512 + 1280 & 0x7ff, pitchSin, pitchCos, yawSin, yawCos, j9, j3, j10, class26.uid);
                                                }
                                        }
                                }
				BoundaryObject class10_2 = class30_sub3_1.obj1;
				if (class10_2 != null) {
					if ((class10_2.orientation2 & class30_sub3_1.anInt1328) != 0 && !isWallVisible(l, i, j, class10_2.orientation2)) {
						class10_2.secondary.render(0, pitchSin, pitchCos, yawSin, yawCos, class10_2.x - cameraX, class10_2.plane - cameraZ, class10_2.y - cameraY, class10_2.uid);
					}
					if ((class10_2.orientation & class30_sub3_1.anInt1328) != 0 && !isWallVisible(l, i, j, class10_2.orientation)) {
						class10_2.primary.render(0, pitchSin, pitchCos, yawSin, yawCos, class10_2.x - cameraX, class10_2.plane - cameraZ, class10_2.y - cameraY, class10_2.uid);
					}
				}
			}
			if (k < planeCount - 1) {
				Ground class30_sub3_12 = groundArray[k + 1][i][j];
				if (class30_sub3_12 != null && class30_sub3_12.aBoolean1323) {
					aClass19_477.insertHead(class30_sub3_12);
				}
			}
			if (i < cameraTileX) {
				Ground class30_sub3_13 = aclass30_sub3[i + 1][j];
				if (class30_sub3_13 != null && class30_sub3_13.aBoolean1323) {
					aClass19_477.insertHead(class30_sub3_13);
				}
			}
			if (j < cameraTileY) {
				Ground class30_sub3_14 = aclass30_sub3[i][j + 1];
				if (class30_sub3_14 != null && class30_sub3_14.aBoolean1323) {
					aClass19_477.insertHead(class30_sub3_14);
				}
			}
			if (i > cameraTileX) {
				Ground class30_sub3_15 = aclass30_sub3[i - 1][j];
				if (class30_sub3_15 != null && class30_sub3_15.aBoolean1323) {
					aClass19_477.insertHead(class30_sub3_15);
				}
			}
			if (j > cameraTileY) {
				Ground class30_sub3_16 = aclass30_sub3[i][j - 1];
				if (class30_sub3_16 != null && class30_sub3_16.aBoolean1323) {
					aClass19_477.insertHead(class30_sub3_16);
				}
			}
		} while (true);
	}

       private void drawPlainTile(PlainTile class43, int i, int j, int k, int l, int i1, int j1, int k1) {
		int l1;
		int i2 = l1 = (j1 << 7) - cameraX;
		int j2;
		int k2 = j2 = (k1 << 7) - cameraY;
		int l2;
		int i3 = l2 = i2 + 128;
		int j3;
		int k3 = j3 = k2 + 128;
		int l3 = tileHeights[i][j1][k1] - cameraZ;
		int i4 = tileHeights[i][j1 + 1][k1] - cameraZ;
		int j4 = tileHeights[i][j1 + 1][k1 + 1] - cameraZ;
		int k4 = tileHeights[i][j1][k1 + 1] - cameraZ;
		int l4 = k2 * l + i2 * i1 >> 16;
		k2 = k2 * i1 - i2 * l >> 16;
		i2 = l4;
		l4 = l3 * k - k2 * j >> 16;
		k2 = l3 * j + k2 * k >> 16;
		l3 = l4;
		if (k2 < 50) {
			return;
		}
		l4 = j2 * l + i3 * i1 >> 16;
		j2 = j2 * i1 - i3 * l >> 16;
		i3 = l4;
		l4 = i4 * k - j2 * j >> 16;
		j2 = i4 * j + j2 * k >> 16;
		i4 = l4;
		if (j2 < 50) {
			return;
		}
		l4 = k3 * l + l2 * i1 >> 16;
		k3 = k3 * i1 - l2 * l >> 16;
		l2 = l4;
		l4 = j4 * k - k3 * j >> 16;
		k3 = j4 * j + k3 * k >> 16;
		j4 = l4;
		if (k3 < 50) {
			return;
		}
		l4 = j3 * l + l1 * i1 >> 16;
		j3 = j3 * i1 - l1 * l >> 16;
		l1 = l4;
		l4 = k4 * k - j3 * j >> 16;
		j3 = k4 * j + j3 * k >> 16;
		k4 = l4;
		if (j3 < 50) {
			return;
		}
		int i5 = Texture.textureInt1 + (i2 << 9) / k2;
		int j5 = Texture.textureInt2 + (l3 << 9) / k2;
		int k5 = Texture.textureInt1 + (i3 << 9) / j2;
		int l5 = Texture.textureInt2 + (i4 << 9) / j2;
		int i6 = Texture.textureInt1 + (l2 << 9) / k3;
		int j6 = Texture.textureInt2 + (j4 << 9) / k3;
		int k6 = Texture.textureInt1 + (l1 << 9) / j3;
		int l6 = Texture.textureInt2 + (k4 << 9) / j3;
		Texture.alpha = 0;
		if ((i6 - k6) * (l5 - l6) - (j6 - l6) * (k5 - k6) > 0) {
			Texture.clip = i6 < 0 || k6 < 0 || k5 < 0 || i6 > DrawingArea.centerX || k6 > DrawingArea.centerX || k5 > DrawingArea.centerX;
                        if (pendingClick && pointInsideTriangle(pendingClickX, pendingClickY, j6, l6, l5, i6, k6, k5)) {
                                clickedTileX = j1;
                                clickedTileY = k1;
                        }
                        if (class43.textureId == -1) {
                                if (class43.northEastColor != 0xbc614e) {
                                        Texture.drawGouraudTriangle(j6, l6, l5, i6, k6, k5, class43.northEastColor, class43.northWestColor, class43.southEastColor);
                                }
                        } else if (!lowMem) {
                                if (class43.flatShade) {
                                        Texture.drawTexturedTriangle(j6, l6, l5, i6, k6, k5, class43.northEastColor, class43.northWestColor, class43.southEastColor, i2, i3, l1, l3, i4, k4, k2, j2, j3, class43.textureId);
                                } else {
                                        Texture.drawTexturedTriangle(j6, l6, l5, i6, k6, k5, class43.northEastColor, class43.northWestColor, class43.southEastColor, l2, l1, i3, j4, k4, i4, k3, j3, j2, class43.textureId);
                                }
                        } else {
                                int i7 = anIntArray485[class43.textureId];
                                Texture.drawGouraudTriangle(j6, l6, l5, i6, k6, k5, applyBrightness(i7, class43.northEastColor), applyBrightness(i7, class43.northWestColor), applyBrightness(i7, class43.southEastColor));
                        }
		}
		if ((i5 - k5) * (l6 - l5) - (j5 - l5) * (k6 - k5) > 0) {
			Texture.clip = i5 < 0 || k5 < 0 || k6 < 0 || i5 > DrawingArea.centerX || k5 > DrawingArea.centerX || k6 > DrawingArea.centerX;
                        if (pendingClick && pointInsideTriangle(pendingClickX, pendingClickY, j5, l5, l6, i5, k5, k6)) {
                                clickedTileX = j1;
                                clickedTileY = k1;
                        }
                        if (class43.textureId == -1) {
                                if (class43.southWestColor != 0xbc614e) {
                                        Texture.drawGouraudTriangle(j5, l5, l6, i5, k5, k6, class43.southWestColor, class43.southEastColor, class43.northWestColor);
                                }
                        } else {
                                if (!lowMem) {
                                        Texture.drawTexturedTriangle(j5, l5, l6, i5, k5, k6, class43.southWestColor, class43.southEastColor, class43.northWestColor, i2, i3, l1, l3, i4, k4, k2, j2, j3, class43.textureId);
                                        return;
                                }
                                int j7 = anIntArray485[class43.textureId];
                                Texture.drawGouraudTriangle(j5, l5, l6, i5, k5, k6, applyBrightness(j7, class43.southWestColor), applyBrightness(j7, class43.southEastColor), applyBrightness(j7, class43.northWestColor));
                        }
		}
	}

        private void drawShapedTile(int i, int j, int k, ShapedTile shapedTile, int l, int i1, int j1) {
                int k1 = shapedTile.vertexX.length;
                for (int l1 = 0; l1 < k1; l1++) {
                        int i2 = shapedTile.vertexX[l1] - cameraX;
                        int k2 = shapedTile.vertexZ[l1] - cameraZ;
                        int i3 = shapedTile.vertexY[l1] - cameraY;
			int k3 = i3 * k + i2 * j1 >> 16;
			i3 = i3 * j1 - i2 * k >> 16;
			i2 = k3;
			k3 = k2 * l - i3 * j >> 16;
			i3 = k2 * j + i3 * l >> 16;
			k2 = k3;
			if (i3 < 50) {
				return;
			}
                        if (shapedTile.faceTexture != null) {
                                ShapedTile.cameraVertexX[l1] = i2;
                                ShapedTile.cameraVertexY[l1] = k2;
                                ShapedTile.cameraVertexZ[l1] = i3;
                        }
                        ShapedTile.projectedX[l1] = Texture.textureInt1 + (i2 << 9) / i3;
                        ShapedTile.projectedY[l1] = Texture.textureInt2 + (k2 << 9) / i3;
                }

		Texture.alpha = 0;
                k1 = shapedTile.faceVertexA.length;
                for (int j2 = 0; j2 < k1; j2++) {
                        int l2 = shapedTile.faceVertexA[j2];
                        int j3 = shapedTile.faceVertexB[j2];
                        int l3 = shapedTile.faceVertexC[j2];
                        int i4 = ShapedTile.projectedX[l2];
                        int j4 = ShapedTile.projectedX[j3];
                        int k4 = ShapedTile.projectedX[l3];
                        int l4 = ShapedTile.projectedY[l2];
                        int i5 = ShapedTile.projectedY[j3];
                        int j5 = ShapedTile.projectedY[l3];
			if ((i4 - j4) * (j5 - i5) - (l4 - i5) * (k4 - j4) > 0) {
				Texture.clip = i4 < 0 || j4 < 0 || k4 < 0 || i4 > DrawingArea.centerX || j4 > DrawingArea.centerX || k4 > DrawingArea.centerX;
                           if (pendingClick && pointInsideTriangle(pendingClickX, pendingClickY, l4, i5, j5, i4, j4, k4)) {
                                   clickedTileX = i;
                                   clickedTileY = i1;
				}
                                if (shapedTile.faceTexture == null || shapedTile.faceTexture[j2] == -1) {
                                        if (shapedTile.faceColorA[j2] != 0xbc614e) {
                                                Texture.drawGouraudTriangle(l4, i5, j5, i4, j4, k4, shapedTile.faceColorA[j2], shapedTile.faceColorB[j2], shapedTile.faceColorC[j2]);
                                        }
                                } else if (!lowMem) {
                                        if (shapedTile.flatShading) {
                                                Texture.drawTexturedTriangle(l4, i5, j5, i4, j4, k4, shapedTile.faceColorA[j2], shapedTile.faceColorB[j2], shapedTile.faceColorC[j2], ShapedTile.cameraVertexX[0], ShapedTile.cameraVertexX[1], ShapedTile.cameraVertexX[3], ShapedTile.cameraVertexY[0], ShapedTile.cameraVertexY[1], ShapedTile.cameraVertexY[3], ShapedTile.cameraVertexZ[0], ShapedTile.cameraVertexZ[1], ShapedTile.cameraVertexZ[3], shapedTile.faceTexture[j2]);
                                        } else {
                                                Texture.drawTexturedTriangle(l4, i5, j5, i4, j4, k4, shapedTile.faceColorA[j2], shapedTile.faceColorB[j2], shapedTile.faceColorC[j2], ShapedTile.cameraVertexX[l2], ShapedTile.cameraVertexX[j3], ShapedTile.cameraVertexX[l3], ShapedTile.cameraVertexY[l2], ShapedTile.cameraVertexY[j3], ShapedTile.cameraVertexY[l3], ShapedTile.cameraVertexZ[l2], ShapedTile.cameraVertexZ[j3], ShapedTile.cameraVertexZ[l3], shapedTile.faceTexture[j2]);
                                        }
                                } else {
                                        int k5 = anIntArray485[shapedTile.faceTexture[j2]];
                                        Texture.drawGouraudTriangle(l4, i5, j5, i4, j4, k4, applyBrightness(k5, shapedTile.faceColorA[j2]), applyBrightness(k5, shapedTile.faceColorB[j2]), applyBrightness(k5, shapedTile.faceColorC[j2]));
                                }
                        }
                }

	}

	private int applyBrightness(int j, int k) {
		k = 127 - k;
		k = k * (j & 0x7f) / 160;
		if (k < 2) {
			k = 2;
		} else if (k > 126) {
			k = 126;
		}
		return (j & 0xff80) + k;
	}

	private boolean pointInsideTriangle(int i, int j, int k, int l, int i1, int j1, int k1, int l1) {
		if (j < k && j < l && j < i1) {
			return false;
		}
		if (j > k && j > l && j > i1) {
			return false;
		}
		if (i < j1 && i < k1 && i < l1) {
			return false;
		}
		if (i > j1 && i > k1 && i > l1) {
			return false;
		}
		int i2 = (j - k) * (k1 - j1) - (i - j1) * (l - k);
		int j2 = (j - i1) * (j1 - l1) - (i - l1) * (k - i1);
		int k2 = (j - l) * (l1 - k1) - (i - k1) * (i1 - l);
		return i2 * k2 > 0 && k2 * j2 > 0;
	}

        private void updateCullingClusters() {
               int j = cullingClusterCounts[cameraPlane];
               CullingCluster aclass47[] = aCullingClusters[cameraPlane];
               cullingClusterBufferCount = 0;
		for (int k = 0; k < j; k++) {
			CullingCluster class47 = aclass47[k];
			if (class47.type == 1) {
				int l = class47.minTileX - cameraTileX + drawDistance;
				if (l < 0 || l > 50) {
					continue;
				}
				int k1 = class47.minTileZ - cameraTileY + drawDistance;
				if (k1 < 0) {
					k1 = 0;
				}
				int j2 = class47.maxTileZ - cameraTileY + drawDistance;
				if (j2 > 50) {
					j2 = 50;
				}
				boolean flag = false;
				while (k1 <= j2) {
					if (aBooleanArrayArray492[l][k1++]) {
						flag = true;
						break;
					}
				}
				if (!flag) {
					continue;
				}
				int j3 = cameraX - class47.minX;
				if (j3 > 32) {
					class47.searchMask = 1;
				} else {
					if (j3 >= -32) {
						continue;
					}
					class47.searchMask = 2;
					j3 = -j3;
				}
				class47.startZFactor = (class47.minZ - cameraY << 8) / j3;
				class47.endZFactor = (class47.maxZ - cameraY << 8) / j3;
				class47.startYFactor = (class47.minY - cameraZ << 8) / j3;
				class47.endYFactor = (class47.maxY - cameraZ << 8) / j3;
                               cullingClusterBuffer[cullingClusterBufferCount++] = class47;
				continue;
			}
			if (class47.type == 2) {
				int i1 = class47.minTileZ - cameraTileY + drawDistance;
				if (i1 < 0 || i1 > 50) {
					continue;
				}
				int l1 = class47.minTileX - cameraTileX + drawDistance;
				if (l1 < 0) {
					l1 = 0;
				}
				int k2 = class47.maxTileX - cameraTileX + drawDistance;
				if (k2 > 50) {
					k2 = 50;
				}
				boolean flag1 = false;
				while (l1 <= k2) {
					if (aBooleanArrayArray492[l1++][i1]) {
						flag1 = true;
						break;
					}
				}
				if (!flag1) {
					continue;
				}
				int k3 = cameraY - class47.minZ;
				if (k3 > 32) {
					class47.searchMask = 3;
				} else {
					if (k3 >= -32) {
						continue;
					}
					class47.searchMask = 4;
					k3 = -k3;
				}
				class47.startXFactor = (class47.minX - cameraX << 8) / k3;
				class47.endXFactor = (class47.maxX - cameraX << 8) / k3;
				class47.startYFactor = (class47.minY - cameraZ << 8) / k3;
				class47.endYFactor = (class47.maxY - cameraZ << 8) / k3;
                               cullingClusterBuffer[cullingClusterBufferCount++] = class47;
			} else if (class47.type == 4) {
				int j1 = class47.minY - cameraZ;
				if (j1 > 128) {
					int i2 = class47.minTileZ - cameraTileY + drawDistance;
					if (i2 < 0) {
						i2 = 0;
					}
					int l2 = class47.maxTileZ - cameraTileY + drawDistance;
					if (l2 > 50) {
						l2 = 50;
					}
					if (i2 <= l2) {
						int i3 = class47.minTileX - cameraTileX + drawDistance;
						if (i3 < 0) {
							i3 = 0;
						}
						int l3 = class47.maxTileX - cameraTileX + drawDistance;
						if (l3 > 50) {
							l3 = 50;
						}
						boolean flag2 = false;
						label0 : for (int i4 = i3; i4 <= l3; i4++) {
							for (int j4 = i2; j4 <= l2; j4++) {
								if (!aBooleanArrayArray492[i4][j4]) {
									continue;
								}
								flag2 = true;
								break label0;
							}

						}

						if (flag2) {
							class47.searchMask = 5;
							class47.startXFactor = (class47.minX - cameraX << 8) / j1;
							class47.endXFactor = (class47.maxX - cameraX << 8) / j1;
							class47.startZFactor = (class47.minZ - cameraY << 8) / j1;
							class47.endZFactor = (class47.maxZ - cameraY << 8) / j1;
                                                   cullingClusterBuffer[cullingClusterBufferCount++] = class47;
						}
					}
				}
			}
		}

	}

	private boolean isTileVisible(int i, int j, int k) {
		int l = tileVisibility[i][j][k];
		if (l == -renderCycle) {
			return false;
		}
		if (l == renderCycle) {
			return true;
		}
		int i1 = j << 7;
		int j1 = k << 7;
		if (isPointVisible(i1 + 1, tileHeights[i][j][k], j1 + 1) && isPointVisible(i1 + 128 - 1, tileHeights[i][j + 1][k], j1 + 1) && isPointVisible(i1 + 128 - 1, tileHeights[i][j + 1][k + 1], j1 + 128 - 1) && isPointVisible(i1 + 1, tileHeights[i][j][k + 1], j1 + 128 - 1)) {
			tileVisibility[i][j][k] = renderCycle;
			return true;
		} else {
			tileVisibility[i][j][k] = -renderCycle;
			return false;
		}
	}

	private boolean isWallVisible(int i, int j, int k, int l) {
		if (!isTileVisible(i, j, k)) {
			return false;
		}
		int i1 = j << 7;
		int j1 = k << 7;
		int k1 = tileHeights[i][j][k] - 1;
		int l1 = k1 - 120;
		int i2 = k1 - 230;
		int j2 = k1 - 238;
		if (l < 16) {
			if (l == 1) {
				if (i1 > cameraX) {
					if (!isPointVisible(i1, k1, j1)) {
						return false;
					}
					if (!isPointVisible(i1, k1, j1 + 128)) {
						return false;
					}
				}
				if (i > 0) {
					if (!isPointVisible(i1, l1, j1)) {
						return false;
					}
					if (!isPointVisible(i1, l1, j1 + 128)) {
						return false;
					}
				}
				return isPointVisible(i1, i2, j1) && isPointVisible(i1, i2, j1 + 128);
			}
			if (l == 2) {
				if (j1 < cameraY) {
					if (!isPointVisible(i1, k1, j1 + 128)) {
						return false;
					}
					if (!isPointVisible(i1 + 128, k1, j1 + 128)) {
						return false;
					}
				}
				if (i > 0) {
					if (!isPointVisible(i1, l1, j1 + 128)) {
						return false;
					}
					if (!isPointVisible(i1 + 128, l1, j1 + 128)) {
						return false;
					}
				}
				return isPointVisible(i1, i2, j1 + 128) && isPointVisible(i1 + 128, i2, j1 + 128);
			}
			if (l == 4) {
				if (i1 < cameraX) {
					if (!isPointVisible(i1 + 128, k1, j1)) {
						return false;
					}
					if (!isPointVisible(i1 + 128, k1, j1 + 128)) {
						return false;
					}
				}
				if (i > 0) {
					if (!isPointVisible(i1 + 128, l1, j1)) {
						return false;
					}
					if (!isPointVisible(i1 + 128, l1, j1 + 128)) {
						return false;
					}
				}
				return isPointVisible(i1 + 128, i2, j1) && isPointVisible(i1 + 128, i2, j1 + 128);
			}
			if (l == 8) {
				if (j1 > cameraY) {
					if (!isPointVisible(i1, k1, j1)) {
						return false;
					}
					if (!isPointVisible(i1 + 128, k1, j1)) {
						return false;
					}
				}
				if (i > 0) {
					if (!isPointVisible(i1, l1, j1)) {
						return false;
					}
					if (!isPointVisible(i1 + 128, l1, j1)) {
						return false;
					}
				}
				return isPointVisible(i1, i2, j1) && isPointVisible(i1 + 128, i2, j1);
			}
		}
		if (!isPointVisible(i1 + 64, j2, j1 + 64)) {
			return false;
		}
		if (l == 16) {
			return isPointVisible(i1, i2, j1 + 128);
		}
		if (l == 32) {
			return isPointVisible(i1 + 128, i2, j1 + 128);
		}
		if (l == 64) {
			return isPointVisible(i1 + 128, i2, j1);
		}
		if (l == 128) {
			return isPointVisible(i1, i2, j1);
		} else {
			System.out.println("Warning unsupported wall type");
			return true;
		}
	}

	private boolean isWallDecorationVisible(int i, int j, int k, int l) {
		if (!isTileVisible(i, j, k)) {
			return false;
		}
		int i1 = j << 7;
		int j1 = k << 7;
		return isPointVisible(i1 + 1, tileHeights[i][j][k] - l, j1 + 1) && isPointVisible(i1 + 128 - 1, tileHeights[i][j + 1][k] - l, j1 + 1) && isPointVisible(i1 + 128 - 1, tileHeights[i][j + 1][k + 1] - l, j1 + 128 - 1) && isPointVisible(i1 + 1, tileHeights[i][j][k + 1] - l, j1 + 128 - 1);
	}

	private boolean isAreaVisible(int i, int j, int k, int l, int i1, int j1) {
		if (j == k && l == i1) {
			if (!isTileVisible(i, j, l)) {
				return false;
			}
			int k1 = j << 7;
			int i2 = l << 7;
			return isPointVisible(k1 + 1, tileHeights[i][j][l] - j1, i2 + 1) && isPointVisible(k1 + 128 - 1, tileHeights[i][j + 1][l] - j1, i2 + 1) && isPointVisible(k1 + 128 - 1, tileHeights[i][j + 1][l + 1] - j1, i2 + 128 - 1) && isPointVisible(k1 + 1, tileHeights[i][j][l + 1] - j1, i2 + 128 - 1);
		}
		for (int l1 = j; l1 <= k; l1++) {
			for (int j2 = l; j2 <= i1; j2++) {
				if (tileVisibility[i][l1][j2] == -renderCycle) {
					return false;
				}
			}

		}

		int k2 = (j << 7) + 1;
		int l2 = (l << 7) + 2;
		int i3 = tileHeights[i][j][l] - j1;
		if (!isPointVisible(k2, i3, l2)) {
			return false;
		}
		int j3 = (k << 7) - 1;
		if (!isPointVisible(j3, i3, l2)) {
			return false;
		}
		int k3 = (i1 << 7) - 1;
		return isPointVisible(k2, i3, k3) && isPointVisible(j3, i3, k3);
	}

	private boolean isPointVisible(int i, int j, int k) {
           for (int l = 0; l < cullingClusterBufferCount; l++) {
			CullingCluster class47 = cullingClusterBuffer[l];
			if (class47.searchMask == 1) {
				int i1 = class47.minX - i;
				if (i1 > 0) {
					int j2 = class47.minZ + (class47.startZFactor * i1 >> 8);
					int k3 = class47.maxZ + (class47.endZFactor * i1 >> 8);
					int l4 = class47.minY + (class47.startYFactor * i1 >> 8);
					int i6 = class47.maxY + (class47.endYFactor * i1 >> 8);
					if (k >= j2 && k <= k3 && j >= l4 && j <= i6) {
						return true;
					}
				}
			} else if (class47.searchMask == 2) {
				int j1 = i - class47.minX;
				if (j1 > 0) {
					int k2 = class47.minZ + (class47.startZFactor * j1 >> 8);
					int l3 = class47.maxZ + (class47.endZFactor * j1 >> 8);
					int i5 = class47.minY + (class47.startYFactor * j1 >> 8);
					int j6 = class47.maxY + (class47.endYFactor * j1 >> 8);
					if (k >= k2 && k <= l3 && j >= i5 && j <= j6) {
						return true;
					}
				}
			} else if (class47.searchMask == 3) {
				int k1 = class47.minZ - k;
				if (k1 > 0) {
					int l2 = class47.minX + (class47.startXFactor * k1 >> 8);
					int i4 = class47.maxX + (class47.endXFactor * k1 >> 8);
					int j5 = class47.minY + (class47.startYFactor * k1 >> 8);
					int k6 = class47.maxY + (class47.endYFactor * k1 >> 8);
					if (i >= l2 && i <= i4 && j >= j5 && j <= k6) {
						return true;
					}
				}
			} else if (class47.searchMask == 4) {
				int l1 = k - class47.minZ;
				if (l1 > 0) {
					int i3 = class47.minX + (class47.startXFactor * l1 >> 8);
					int j4 = class47.maxX + (class47.endXFactor * l1 >> 8);
					int k5 = class47.minY + (class47.startYFactor * l1 >> 8);
					int l6 = class47.maxY + (class47.endYFactor * l1 >> 8);
					if (i >= i3 && i <= j4 && j >= k5 && j <= l6) {
						return true;
					}
				}
			} else if (class47.searchMask == 5) {
				int i2 = j - class47.minY;
				if (i2 > 0) {
					int j3 = class47.minX + (class47.startXFactor * i2 >> 8);
					int k4 = class47.maxX + (class47.endXFactor * i2 >> 8);
					int l5 = class47.minZ + (class47.startZFactor * i2 >> 8);
					int i7 = class47.maxZ + (class47.endZFactor * i2 >> 8);
					if (i >= j3 && i <= k4 && k >= l5 && k <= i7) {
						return true;
					}
				}
			}
		}

		return false;
	}

	private boolean aBoolean434;
	public static boolean lowMem = true;
	private final int planeCount;
	private final int worldWidth;
	private final int worldHeight;
	private final int[][][] tileHeights;
	private final Ground[][][] groundArray;
	private int activePlane;
        private int sceneObjectCachePos;
        private final SceneObject[] sceneObjectCache;
	private final int[][][] tileVisibility;
	private static int visibleTileCount;
       private static int cameraPlane;
	private static int renderCycle;
	private static int minVisibleX;
	private static int maxVisibleX;
	private static int minVisibleY;
	private static int maxVisibleY;
	private static int cameraTileX;
	private static int cameraTileY;
        private static int cameraX;
        private static int cameraZ;
        private static int cameraY;
        private static int pitchSin;
        private static int pitchCos;
        private static int yawSin;
        private static int yawCos;
        private static SceneObject[] aClass28Array462 = new SceneObject[100];
	private static final int[] anIntArray463 = {53, -53, -53, 53};
	private static final int[] anIntArray464 = {-53, -53, 53, 53};
	private static final int[] anIntArray465 = {-45, 45, 45, -45};
	private static final int[] anIntArray466 = {45, 45, -45, -45};
        private static boolean pendingClick;
        private static int pendingClickX;
        private static int pendingClickY;
        public static int clickedTileX = -1;
        public static int clickedTileY = -1;
        private static final int CLUSTER_PLANES;
        private static int[] cullingClusterCounts;
	private static CullingCluster[][] aCullingClusters;
       private static int cullingClusterBufferCount;
	private static final CullingCluster[] cullingClusterBuffer = new CullingCluster[500];
	private static NodeList aClass19_477 = new NodeList();
	private static final int[] anIntArray478 = {19, 55, 38, 155, 255, 110, 137, 205, 76};
	private static final int[] anIntArray479 = {160, 192, 80, 96, 0, 144, 80, 48, 160};
	private static final int[] anIntArray480 = {76, 8, 137, 4, 0, 1, 38, 2, 19};
	private static final int[] anIntArray481 = {0, 0, 2, 0, 0, 2, 1, 1, 0};
	private static final int[] anIntArray482 = {2, 0, 0, 2, 0, 0, 0, 4, 4};
	private static final int[] anIntArray483 = {0, 4, 4, 8, 0, 0, 8, 0, 0};
	private static final int[] anIntArray484 = {1, 1, 0, 0, 0, 8, 0, 0, 8};
	private static final int[] anIntArray485 = {41, 39248, 41, 4643, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 43086, 41, 41, 41, 41, 41, 41, 41, 8602, 41, 28992, 41, 41, 41, 41, 41, 5056, 41, 41, 41, 7079, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 3131, 41, 41, 41};
	private final int[] vertexVisitA;
	private final int[] vertexVisitB;
	private int mergeCycleId;
	private final int[][] anIntArrayArray489 = {new int[16], {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, {1, 0, 0, 0, 1, 1, 0, 0, 1, 1, 1, 0, 1, 1, 1, 1}, {1, 1, 0, 0, 1, 1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0}, {0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 0, 1, 0, 0, 0, 1}, {0, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, {1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1}, {1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 1, 0, 0}, {1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 0, 0, 1, 1}, {1, 1, 1, 1, 1, 1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 1, 1, 1}};
	private final int[][] anIntArrayArray490 = {{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15}, {12, 8, 4, 0, 13, 9, 5, 1, 14, 10, 6, 2, 15, 11, 7, 3}, {15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0}, {3, 7, 11, 15, 2, 6, 10, 14, 1, 5, 9, 13, 0, 4, 8, 12}};
	private static boolean[][][][] aBooleanArrayArrayArrayArray491 = new boolean[8][32][256][256];
	private static boolean[][] aBooleanArrayArray492;
	private static int halfViewportWidth;
	private static int halfViewportHeight;
	private static int viewportMinX;
	private static int viewportMinY;
	private static int viewportMaxX;
	private static int viewportMaxY;

        static {
                CLUSTER_PLANES = 4;
                cullingClusterCounts = new int[CLUSTER_PLANES];
                aCullingClusters = new CullingCluster[CLUSTER_PLANES][500];
        }
}
