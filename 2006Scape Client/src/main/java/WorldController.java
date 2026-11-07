// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

final class WorldController {
	public static int drawDistance = 25;

	public WorldController(int ai[][][]) {
		int i = 104;// was parameter
		int j = 104;// was parameter
		int k = 4;// was parameter
		aBoolean434 = true;
		obj5Cache = new Object5[5000];
		modelSharedVertexStamps = new int[10000];
		otherSharedVertexStamps = new int[10000];
		planeCount = k;
		tileCountX = j;
		tileCountY = i;
		groundArray = new Ground[k][j][i];
		tileOcclusionCache = new int[k][j + 1][i + 1];
		tileHeights = ai;
		initToNull();
	}

	public static void nullLoader() {
		aClass28Array462 = null;
		occluderCounts = null;
		occluders = null;
		aClass19_477 = null;
		tileVisibilityTable = null;
		cameraTileVisibility = null;
	}

	public void initToNull() {
		for (int j = 0; j < planeCount; j++) {
			for (int k = 0; k < tileCountX; k++) {
				for (int i1 = 0; i1 < tileCountY; i1++) {
					groundArray[j][k][i1] = null;
				}

			}

		}
		for (int l = 0; l < occluderPlaneCount; l++) {
			for (int j1 = 0; j1 < occluderCounts[l]; j1++) {
				occluders[l][j1] = null;
			}

			occluderCounts[l] = 0;
		}

		for (int k1 = 0; k1 < obj5CacheCurrPos; k1++) {
			obj5Cache[k1] = null;
		}

		obj5CacheCurrPos = 0;
		for (int l1 = 0; l1 < aClass28Array462.length; l1++) {
			aClass28Array462[l1] = null;
		}

	}

	public void createPlaneTiles(int plane) {
		lowestPlane = plane;
		for (int tileX = 0; tileX < tileCountX; tileX++) {
			for (int tileY = 0; tileY < tileCountY; tileY++) {
				if (groundArray[plane][tileX][tileY] == null) {
					groundArray[plane][tileX][tileY] = new Ground(plane, tileX, tileY);
				}
			}

		}

	}

	public void lowerColumnPlanes(int tileY, int tileX) {
		Ground oldGround = groundArray[0][tileX][tileY];
		for (int plane = 0; plane < 3; plane++) {
			Ground tile = groundArray[plane][tileX][tileY] = groundArray[plane + 1][tileX][tileY];
			if (tile != null) {
				tile.anInt1307--;
				for (int index = 0; index < tile.anInt1317; index++) {
					Object5 sceneObject = tile.obj5Array[index];
					if ((sceneObject.uid >> 29 & 3) == 2 && sceneObject.anInt523 == tileX && sceneObject.anInt525 == tileY) {
						sceneObject.anInt517--;
					}
				}

			}
		}
		if (groundArray[0][tileX][tileY] == null) {
			groundArray[0][tileX][tileY] = new Ground(0, tileX, tileY);
		}
		groundArray[0][tileX][tileY].aClass30_Sub3_1329 = oldGround;
		groundArray[3][tileX][tileY] = null;
	}

	public static void addOccluder(int plane, int minX, int maxHeight, int maxX, int maxY, int minHeight, int minY, int occluderType) {
		Class47 occluder = new Class47();
		occluder.minTileX = minX / 128;
		occluder.maxTileX = maxX / 128;
		occluder.minTileY = minY / 128;
		occluder.maxTileY = maxY / 128;
		occluder.type = occluderType;
		occluder.minX = minX;
		occluder.maxX = maxX;
		occluder.minY = minY;
		occluder.maxY = maxY;
		occluder.minHeight = minHeight;
		occluder.maxHeight = maxHeight;
		occluders[plane][occluderCounts[plane]++] = occluder;
	}

	public void setMinCameraPlane(int plane, int tileX, int tileY, int minCameraPlane) {
		Ground tile = groundArray[plane][tileX][tileY];
		if (tile != null) {
			groundArray[plane][tileX][tileY].anInt1321 = minCameraPlane;
		}
	}

	/**
	 * Corners are ordered (minX,minY), (maxX,minY), (maxX,maxY), (minX,maxY) in world x/y.
	 * Shape 0 is a plain underlay-only tile, shape 1 a plain overlay tile, any other shape indexes the Class40 shape tables.
	 */
	public void addTile(int plane, int tileX, int tileY, int shape, int rotation, int textureId, int height0, int height1, int height2, int height3, int underlayColour0, int underlayColour1, int underlayColour2, int underlayColour3, int overlayColour0, int overlayColour1, int overlayColour2, int overlayColour3, int underlayMapColour, int overlayMapColour) {
		if (shape == 0) {
			Class43 plainTile = new Class43(underlayColour0, underlayColour1, underlayColour2, underlayColour3, -1, underlayMapColour, false);
			for (int planeIndex = plane; planeIndex >= 0; planeIndex--) {
				if (groundArray[planeIndex][tileX][tileY] == null) {
					groundArray[planeIndex][tileX][tileY] = new Ground(planeIndex, tileX, tileY);
				}
			}

			groundArray[plane][tileX][tileY].aClass43_1311 = plainTile;
			return;
		}
		if (shape == 1) {
			Class43 overlayTile = new Class43(overlayColour0, overlayColour1, overlayColour2, overlayColour3, textureId, overlayMapColour, height0 == height1 && height0 == height2 && height0 == height3);
			for (int planeIndex = plane; planeIndex >= 0; planeIndex--) {
				if (groundArray[planeIndex][tileX][tileY] == null) {
					groundArray[planeIndex][tileX][tileY] = new Ground(planeIndex, tileX, tileY);
				}
			}

			groundArray[plane][tileX][tileY].aClass43_1311 = overlayTile;
			return;
		}
		Class40 shapedTile = new Class40(tileY, overlayColour0, underlayColour3, height2, textureId, overlayColour2, rotation, underlayColour0, underlayMapColour, underlayColour2, height3, height1, height0, shape, overlayColour3, overlayColour1, underlayColour1, tileX, overlayMapColour);
		for (int planeIndex = plane; planeIndex >= 0; planeIndex--) {
			if (groundArray[planeIndex][tileX][tileY] == null) {
				groundArray[planeIndex][tileX][tileY] = new Ground(planeIndex, tileX, tileY);
			}
		}

		groundArray[plane][tileX][tileY].aClass40_1312 = shapedTile;
	}

	public void addGroundDecoration(int plane, int height, int tileY, Animable model, byte info, int uid, int tileX) {
		if (model == null) {
			return;
		}
		Object3 groundDecoration = new Object3();
		groundDecoration.aClass30_Sub2_Sub4_814 = model;
		groundDecoration.anInt812 = tileX * 128 + 64;
		groundDecoration.anInt813 = tileY * 128 + 64;
		groundDecoration.anInt811 = height;
		groundDecoration.uid = uid;
		groundDecoration.aByte816 = info;
		if (groundArray[plane][tileX][tileY] == null) {
			groundArray[plane][tileX][tileY] = new Ground(plane, tileX, tileY);
		}
		groundArray[plane][tileX][tileY].obj3 = groundDecoration;
	}

	public void addItemStack(int tileX, int uid, Animable otherItemModel1, int height, Animable otherItemModel2, Animable topItemModel, int plane, int tileY) {
		Object4 itemStack = new Object4();
		itemStack.aClass30_Sub2_Sub4_48 = topItemModel;
		itemStack.anInt46 = tileX * 128 + 64;
		itemStack.anInt47 = tileY * 128 + 64;
		itemStack.anInt45 = height;
		itemStack.uid = uid;
		itemStack.aClass30_Sub2_Sub4_49 = otherItemModel1;
		itemStack.aClass30_Sub2_Sub4_50 = otherItemModel2;
		int tallestObjectHeight = 0;
		Ground tile = groundArray[plane][tileX][tileY];
		if (tile != null) {
			for (int index = 0; index < tile.anInt1317; index++) {
				if (tile.obj5Array[index].aClass30_Sub2_Sub4_521 instanceof Model) {
					int objectHeight = ((Model) tile.obj5Array[index].aClass30_Sub2_Sub4_521).anInt1654;
					if (objectHeight > tallestObjectHeight) {
						tallestObjectHeight = objectHeight;
					}
				}
			}

		}
		itemStack.anInt52 = tallestObjectHeight;
		if (groundArray[plane][tileX][tileY] == null) {
			groundArray[plane][tileX][tileY] = new Ground(plane, tileX, tileY);
		}
		groundArray[plane][tileX][tileY].obj4 = itemStack;
	}

	public void addWall(int orientation, Animable model, int uid, int tileY, byte info, int tileX, Animable secondaryModel, int height, int secondaryOrientation, int plane) {
		if (model == null && secondaryModel == null) {
			return;
		}
		Object1 wall = new Object1();
		wall.uid = uid;
		wall.aByte281 = info;
		wall.anInt274 = tileX * 128 + 64;
		wall.anInt275 = tileY * 128 + 64;
		wall.anInt273 = height;
		wall.aClass30_Sub2_Sub4_278 = model;
		wall.aClass30_Sub2_Sub4_279 = secondaryModel;
		wall.orientation = orientation;
		wall.orientation1 = secondaryOrientation;
		for (int planeIndex = plane; planeIndex >= 0; planeIndex--) {
			if (groundArray[planeIndex][tileX][tileY] == null) {
				groundArray[planeIndex][tileX][tileY] = new Ground(planeIndex, tileX, tileY);
			}
		}

		groundArray[plane][tileX][tileY].obj1 = wall;
	}

	public void addWallDecoration(int uid, int tileY, int rotation, int plane, int xOffset, int height, Animable model, int tileX, byte info, int yOffset, int drawMask) {
		if (model == null) {
			return;
		}
		Object2 wallDecoration = new Object2();
		wallDecoration.uid = uid;
		wallDecoration.aByte506 = info;
		wallDecoration.anInt500 = tileX * 128 + 64 + xOffset;
		wallDecoration.anInt501 = tileY * 128 + 64 + yOffset;
		wallDecoration.anInt499 = height;
		wallDecoration.aClass30_Sub2_Sub4_504 = model;
		wallDecoration.anInt502 = drawMask;
		wallDecoration.anInt503 = rotation;
		for (int planeIndex = plane; planeIndex >= 0; planeIndex--) {
			if (groundArray[planeIndex][tileX][tileY] == null) {
				groundArray[planeIndex][tileX][tileY] = new Ground(planeIndex, tileX, tileY);
			}
		}

		groundArray[plane][tileX][tileY].obj2 = wallDecoration;
	}

	public boolean addSceneObject(int uid, byte info, int height, int sizeY, Animable model, int sizeX, int plane, int rotation, int tileY, int tileX) {
		if (model == null) {
			return true;
		} else {
			int x = tileX * 128 + 64 * sizeX;
			int y = tileY * 128 + 64 * sizeY;
			return placeSceneObject(plane, tileX, tileY, sizeX, sizeY, x, y, height, model, rotation, false, uid, info);
		}
	}

	public boolean addEntity(int plane, int yaw, int height, int uid, int y, int radius, int x, Animable model, boolean extendByYaw) {
		if (model == null) {
			return true;
		}
		int minX = x - radius;
		int minY = y - radius;
		int maxX = x + radius;
		int maxY = y + radius;
		if (extendByYaw) {
			if (yaw > 640 && yaw < 1408) {
				maxY += 128;
			}
			if (yaw > 1152 && yaw < 1920) {
				maxX += 128;
			}
			if (yaw > 1664 || yaw < 384) {
				minY -= 128;
			}
			if (yaw > 128 && yaw < 896) {
				minX -= 128;
			}
		}
		minX /= 128;
		minY /= 128;
		maxX /= 128;
		maxY /= 128;
		return placeSceneObject(plane, minX, minY, maxX - minX + 1, maxY - minY + 1, x, y, height, model, yaw, true, uid, (byte) 0);
	}

	public boolean addEntityInTileBounds(int plane, int y, Animable model, int yaw, int maxTileY, int x, int height, int minTileX, int maxTileX, int uid, int minTileY) {
		return model == null || placeSceneObject(plane, minTileX, minTileY, maxTileX - minTileX + 1, maxTileY - minTileY + 1, x, y, height, model, yaw, true, uid, (byte) 0);
	}

	private boolean placeSceneObject(int plane, int tileX, int tileY, int sizeX, int sizeY, int x, int y, int height, Animable model, int rotation, boolean addToObj5Cache, int uid, byte info) {
		for (int scanX = tileX; scanX < tileX + sizeX; scanX++) {
			for (int scanY = tileY; scanY < tileY + sizeY; scanY++) {
				if (scanX < 0 || scanY < 0 || scanX >= tileCountX || scanY >= tileCountY) {
					return false;
				}
				Ground tile = groundArray[plane][scanX][scanY];
				if (tile != null && tile.anInt1317 >= 5) {
					return false;
				}
			}

		}

		Object5 sceneObject = new Object5();
		sceneObject.uid = uid;
		sceneObject.aByte530 = info;
		sceneObject.anInt517 = plane;
		sceneObject.anInt519 = x;
		sceneObject.anInt520 = y;
		sceneObject.anInt518 = height;
		sceneObject.aClass30_Sub2_Sub4_521 = model;
		sceneObject.anInt522 = rotation;
		sceneObject.anInt523 = tileX;
		sceneObject.anInt525 = tileY;
		sceneObject.anInt524 = tileX + sizeX - 1;
		sceneObject.anInt526 = tileY + sizeY - 1;
		for (int coverX = tileX; coverX < tileX + sizeX; coverX++) {
			for (int coverY = tileY; coverY < tileY + sizeY; coverY++) {
				int edgeMask = 0;
				if (coverX > tileX) {
					edgeMask++;
				}
				if (coverX < tileX + sizeX - 1) {
					edgeMask += 4;
				}
				if (coverY > tileY) {
					edgeMask += 8;
				}
				if (coverY < tileY + sizeY - 1) {
					edgeMask += 2;
				}
				for (int planeIndex = plane; planeIndex >= 0; planeIndex--) {
					if (groundArray[planeIndex][coverX][coverY] == null) {
						groundArray[planeIndex][coverX][coverY] = new Ground(planeIndex, coverX, coverY);
					}
				}

				Ground tile = groundArray[plane][coverX][coverY];
				tile.obj5Array[tile.anInt1317] = sceneObject;
				tile.anIntArray1319[tile.anInt1317] = edgeMask;
				tile.anInt1320 |= edgeMask;
				tile.anInt1317++;
			}

		}

		if (addToObj5Cache) {
			obj5Cache[obj5CacheCurrPos++] = sceneObject;
		}
		return true;
	}

	public void clearObj5Cache() {
		for (int i = 0; i < obj5CacheCurrPos; i++) {
			Object5 object5 = obj5Cache[i];
			detachSceneObject(object5);
			obj5Cache[i] = null;
		}

		obj5CacheCurrPos = 0;
	}

	private void detachSceneObject(Object5 sceneObject) {
		for (int tileX = sceneObject.anInt523; tileX <= sceneObject.anInt524; tileX++) {
			for (int tileY = sceneObject.anInt525; tileY <= sceneObject.anInt526; tileY++) {
				Ground tile = groundArray[sceneObject.anInt517][tileX][tileY];
				if (tile != null) {
					for (int index = 0; index < tile.anInt1317; index++) {
						if (tile.obj5Array[index] != sceneObject) {
							continue;
						}
						tile.anInt1317--;
						for (int shiftIndex = index; shiftIndex < tile.anInt1317; shiftIndex++) {
							tile.obj5Array[shiftIndex] = tile.obj5Array[shiftIndex + 1];
							tile.anIntArray1319[shiftIndex] = tile.anIntArray1319[shiftIndex + 1];
						}

						tile.obj5Array[tile.anInt1317] = null;
						break;
					}

					tile.anInt1320 = 0;
					for (int maskIndex = 0; maskIndex < tile.anInt1317; maskIndex++) {
						tile.anInt1320 |= tile.anIntArray1319[maskIndex];
					}

				}
			}

		}

	}

	public void scaleWallDecorationOffset(int tileY, int offsetScaleSixteenths, int tileX, int plane) {
		Ground tile = groundArray[plane][tileX][tileY];
		if (tile == null) {
			return;
		}
		Object2 wallDecoration = tile.obj2;
		if (wallDecoration != null) {
			int centreX = tileX * 128 + 64;
			int centreY = tileY * 128 + 64;
			wallDecoration.anInt500 = centreX + (wallDecoration.anInt500 - centreX) * offsetScaleSixteenths / 16;
			wallDecoration.anInt501 = centreY + (wallDecoration.anInt501 - centreY) * offsetScaleSixteenths / 16;
		}
	}

	public void removeWall(int tileX, int plane, int tileY, byte byte0) {
		Ground tile = groundArray[plane][tileX][tileY];
		if (byte0 != -119) {
			aBoolean434 = !aBoolean434;
		}
		if (tile != null) {
			tile.obj1 = null;
		}
	}

	public void removeWallDecoration(int tileY, int plane, int tileX) {
		Ground tile = groundArray[plane][tileX][tileY];
		if (tile != null) {
			tile.obj2 = null;
		}
	}

	public void removeSceneObject(int plane, int tileX, int tileY) {
		Ground tile = groundArray[plane][tileX][tileY];
		if (tile == null) {
			return;
		}
		for (int index = 0; index < tile.anInt1317; index++) {
			Object5 sceneObject = tile.obj5Array[index];
			if ((sceneObject.uid >> 29 & 3) == 2 && sceneObject.anInt523 == tileX && sceneObject.anInt525 == tileY) {
				detachSceneObject(sceneObject);
				return;
			}
		}

	}

	public void removeGroundDecoration(int plane, int tileY, int tileX) {
		Ground tile = groundArray[plane][tileX][tileY];
		if (tile == null) {
			return;
		}
		tile.obj3 = null;
	}

	public void removeGroundItemStack(int plane, int tileX, int tileY) {
		Ground tile = groundArray[plane][tileX][tileY];
		if (tile != null) {
			tile.obj4 = null;
		}
	}

	public Object1 getWall(int plane, int tileX, int tileY) {
		Ground tile = groundArray[plane][tileX][tileY];
		if (tile == null) {
			return null;
		} else {
			return tile.obj1;
		}
	}

	public Object2 getWallDecoration(int tileX, int tileY, int plane) {
		Ground tile = groundArray[plane][tileX][tileY];
		if (tile == null) {
			return null;
		} else {
			return tile.obj2;
		}
	}

	public Object5 getSceneObject(int tileX, int tileY, int plane) {
		Ground tile = groundArray[plane][tileX][tileY];
		if (tile == null) {
			return null;
		}
		for (int index = 0; index < tile.anInt1317; index++) {
			Object5 sceneObject = tile.obj5Array[index];
			if ((sceneObject.uid >> 29 & 3) == 2 && sceneObject.anInt523 == tileX && sceneObject.anInt525 == tileY) {
				return sceneObject;
			}
		}
		return null;
	}

	public Object3 getGroundDecoration(int tileY, int tileX, int plane) {
		Ground tile = groundArray[plane][tileX][tileY];
		if (tile == null || tile.obj3 == null) {
			return null;
		} else {
			return tile.obj3;
		}
	}

	public int getWallUid(int plane, int tileX, int tileY) {
		Ground tile = groundArray[plane][tileX][tileY];
		if (tile == null || tile.obj1 == null) {
			return 0;
		} else {
			return tile.obj1.uid;
		}
	}

	public int getWallDecorationUid(int plane, int tileX, int tileY) {
		Ground tile = groundArray[plane][tileX][tileY];
		if (tile == null || tile.obj2 == null) {
			return 0;
		} else {
			return tile.obj2.uid;
		}
	}

	public int getSceneObjectUid(int plane, int tileX, int tileY) {
		Ground tile = groundArray[plane][tileX][tileY];
		if (tile == null) {
			return 0;
		}
		for (int index = 0; index < tile.anInt1317; index++) {
			Object5 sceneObject = tile.obj5Array[index];
			if ((sceneObject.uid >> 29 & 3) == 2 && sceneObject.anInt523 == tileX && sceneObject.anInt525 == tileY) {
				return sceneObject.uid;
			}
		}

		return 0;
	}

	public int getGroundDecorationUid(int plane, int tileX, int tileY) {
		Ground tile = groundArray[plane][tileX][tileY];
		if (tile == null || tile.obj3 == null) {
			return 0;
		} else {
			return tile.obj3.uid;
		}
	}

	public int getObjectInfo(int plane, int tileX, int tileY, int uid) {
		Ground tile = groundArray[plane][tileX][tileY];
		if (tile == null) {
			return -1;
		}
		if (tile.obj1 != null && tile.obj1.uid == uid) {
			return tile.obj1.aByte281 & 0xff;
		}
		if (tile.obj2 != null && tile.obj2.uid == uid) {
			return tile.obj2.aByte506 & 0xff;
		}
		if (tile.obj3 != null && tile.obj3.uid == uid) {
			return tile.obj3.aByte816 & 0xff;
		}
		for (int index = 0; index < tile.anInt1317; index++) {
			if (tile.obj5Array[index].uid == uid) {
				return tile.obj5Array[index].aByte530 & 0xff;
			}
		}

		return -1;
	}

	public void lightScene(int lightY, int lightX, int lightZ) {
		int ambientLight = 64;// was parameter
		int contrastBase = 768;// was parameter
		int lightMagnitude = (int) Math.sqrt(lightX * lightX + lightY * lightY + lightZ * lightZ);
		int lightContrast = contrastBase * lightMagnitude >> 8;
		for (int plane = 0; plane < planeCount; plane++) {
			for (int tileX = 0; tileX < tileCountX; tileX++) {
				for (int tileY = 0; tileY < tileCountY; tileY++) {
					Ground ground = groundArray[plane][tileX][tileY];
					if (ground != null) {
						Object1 wall = ground.obj1;
						if (wall != null && wall.aClass30_Sub2_Sub4_278 != null && wall.aClass30_Sub2_Sub4_278.aClass33Array1425 != null) {
							mergeNeighbourNormals(plane, 1, 1, tileX, tileY, (Model) wall.aClass30_Sub2_Sub4_278);
							if (wall.aClass30_Sub2_Sub4_279 != null && wall.aClass30_Sub2_Sub4_279.aClass33Array1425 != null) {
								mergeNeighbourNormals(plane, 1, 1, tileX, tileY, (Model) wall.aClass30_Sub2_Sub4_279);
								mergeSharedVertexNormals((Model) wall.aClass30_Sub2_Sub4_278, (Model) wall.aClass30_Sub2_Sub4_279, 0, 0, 0, false);
								((Model) wall.aClass30_Sub2_Sub4_279).applyVertexLighting(ambientLight, lightContrast, lightX, lightY, lightZ);
							}
							((Model) wall.aClass30_Sub2_Sub4_278).applyVertexLighting(ambientLight, lightContrast, lightX, lightY, lightZ);
						}
						for (int objectIndex = 0; objectIndex < ground.anInt1317; objectIndex++) {
							Object5 sceneObject = ground.obj5Array[objectIndex];
							if (sceneObject != null && sceneObject.aClass30_Sub2_Sub4_521 != null && sceneObject.aClass30_Sub2_Sub4_521.aClass33Array1425 != null) {
								mergeNeighbourNormals(plane, sceneObject.anInt524 - sceneObject.anInt523 + 1, sceneObject.anInt526 - sceneObject.anInt525 + 1, tileX, tileY, (Model) sceneObject.aClass30_Sub2_Sub4_521);
								((Model) sceneObject.aClass30_Sub2_Sub4_521).applyVertexLighting(ambientLight, lightContrast, lightX, lightY, lightZ);
							}
						}

						Object3 groundDecoration = ground.obj3;
						if (groundDecoration != null && groundDecoration.aClass30_Sub2_Sub4_814.aClass33Array1425 != null) {
							mergeGroundDecorationNormals(tileX, plane, (Model) groundDecoration.aClass30_Sub2_Sub4_814, tileY);
							((Model) groundDecoration.aClass30_Sub2_Sub4_814).applyVertexLighting(ambientLight, lightContrast, lightX, lightY, lightZ);
						}
					}
				}

			}

		}

	}

	private void mergeGroundDecorationNormals(int tileX, int plane, Model model, int tileY) {
		if (tileX < tileCountX) {
			Ground neighbourPlusX = groundArray[plane][tileX + 1][tileY];
			if (neighbourPlusX != null && neighbourPlusX.obj3 != null && neighbourPlusX.obj3.aClass30_Sub2_Sub4_814.aClass33Array1425 != null) {
				mergeSharedVertexNormals(model, (Model) neighbourPlusX.obj3.aClass30_Sub2_Sub4_814, 128, 0, 0, true);
			}
		}
		if (tileY < tileCountX) {
			Ground neighbourPlusY = groundArray[plane][tileX][tileY + 1];
			if (neighbourPlusY != null && neighbourPlusY.obj3 != null && neighbourPlusY.obj3.aClass30_Sub2_Sub4_814.aClass33Array1425 != null) {
				mergeSharedVertexNormals(model, (Model) neighbourPlusY.obj3.aClass30_Sub2_Sub4_814, 0, 0, 128, true);
			}
		}
		if (tileX < tileCountX && tileY < tileCountY) {
			Ground neighbourPlusXPlusY = groundArray[plane][tileX + 1][tileY + 1];
			if (neighbourPlusXPlusY != null && neighbourPlusXPlusY.obj3 != null && neighbourPlusXPlusY.obj3.aClass30_Sub2_Sub4_814.aClass33Array1425 != null) {
				mergeSharedVertexNormals(model, (Model) neighbourPlusXPlusY.obj3.aClass30_Sub2_Sub4_814, 128, 0, 128, true);
			}
		}
		if (tileX < tileCountX && tileY > 0) {
			Ground neighbourPlusXMinusY = groundArray[plane][tileX + 1][tileY - 1];
			if (neighbourPlusXMinusY != null && neighbourPlusXMinusY.obj3 != null && neighbourPlusXMinusY.obj3.aClass30_Sub2_Sub4_814.aClass33Array1425 != null) {
				mergeSharedVertexNormals(model, (Model) neighbourPlusXMinusY.obj3.aClass30_Sub2_Sub4_814, 128, 0, -128, true);
			}
		}
	}

	private void mergeNeighbourNormals(int plane, int sizeX, int sizeY, int tileX, int tileY, Model model) {
		boolean onOwnPlane = true;
		int startX = tileX;
		int endX = tileX + sizeX;
		int startY = tileY - 1;
		int endY = tileY + sizeY;
		for (int scanPlane = plane; scanPlane <= plane + 1; scanPlane++) {
			if (scanPlane != planeCount) {
				for (int scanX = startX; scanX <= endX; scanX++) {
					if (scanX >= 0 && scanX < tileCountX) {
						for (int scanY = startY; scanY <= endY; scanY++) {
							if (scanY >= 0 && scanY < tileCountY && (!onOwnPlane || scanX >= endX || scanY >= endY || scanY < tileY && scanX != tileX)) {
								Ground neighbour = groundArray[scanPlane][scanX][scanY];
								if (neighbour != null) {
									int heightOffset = (tileHeights[scanPlane][scanX][scanY] + tileHeights[scanPlane][scanX + 1][scanY] + tileHeights[scanPlane][scanX][scanY + 1] + tileHeights[scanPlane][scanX + 1][scanY + 1]) / 4 - (tileHeights[plane][tileX][tileY] + tileHeights[plane][tileX + 1][tileY] + tileHeights[plane][tileX][tileY + 1] + tileHeights[plane][tileX + 1][tileY + 1]) / 4;
									Object1 wall = neighbour.obj1;
									if (wall != null && wall.aClass30_Sub2_Sub4_278 != null && wall.aClass30_Sub2_Sub4_278.aClass33Array1425 != null) {
										mergeSharedVertexNormals(model, (Model) wall.aClass30_Sub2_Sub4_278, (scanX - tileX) * 128 + (1 - sizeX) * 64, heightOffset, (scanY - tileY) * 128 + (1 - sizeY) * 64, onOwnPlane);
									}
									if (wall != null && wall.aClass30_Sub2_Sub4_279 != null && wall.aClass30_Sub2_Sub4_279.aClass33Array1425 != null) {
										mergeSharedVertexNormals(model, (Model) wall.aClass30_Sub2_Sub4_279, (scanX - tileX) * 128 + (1 - sizeX) * 64, heightOffset, (scanY - tileY) * 128 + (1 - sizeY) * 64, onOwnPlane);
									}
									for (int objectIndex = 0; objectIndex < neighbour.anInt1317; objectIndex++) {
										Object5 sceneObject = neighbour.obj5Array[objectIndex];
										if (sceneObject != null && sceneObject.aClass30_Sub2_Sub4_521 != null && sceneObject.aClass30_Sub2_Sub4_521.aClass33Array1425 != null) {
											int otherSizeX = sceneObject.anInt524 - sceneObject.anInt523 + 1;
											int otherSizeY = sceneObject.anInt526 - sceneObject.anInt525 + 1;
											mergeSharedVertexNormals(model, (Model) sceneObject.aClass30_Sub2_Sub4_521, (sceneObject.anInt523 - tileX) * 128 + (otherSizeX - sizeX) * 64, heightOffset, (sceneObject.anInt525 - tileY) * 128 + (otherSizeY - sizeY) * 64, onOwnPlane);
										}
									}

								}
							}
						}

					}
				}

				startX--;
				onOwnPlane = false;
			}
		}

	}

	private void mergeSharedVertexNormals(Model model, Model other, int offsetX, int offsetY, int offsetZ, boolean hideSharedFaces) {
		mergeStamp++;
		int sharedCount = 0;
		int otherX[] = other.anIntArray1627;
		int otherVertexCount = other.anInt1626;
		for (int vertexIndex = 0; vertexIndex < model.anInt1626; vertexIndex++) {
			Class33 normal = model.aClass33Array1425[vertexIndex];
			Class33 originalNormal = model.aClass33Array1660[vertexIndex];
			if (originalNormal.anInt605 != 0) {
				int shiftedY = model.anIntArray1628[vertexIndex] - offsetY;
				if (shiftedY <= other.maxY) {
					int shiftedX = model.anIntArray1627[vertexIndex] - offsetX;
					if (shiftedX >= other.minX && shiftedX <= other.maxX) {
						int shiftedZ = model.anIntArray1629[vertexIndex] - offsetZ;
						if (shiftedZ >= other.minZ && shiftedZ <= other.maxZ) {
							for (int otherVertexIndex = 0; otherVertexIndex < otherVertexCount; otherVertexIndex++) {
								Class33 otherNormal = other.aClass33Array1425[otherVertexIndex];
								Class33 otherOriginalNormal = other.aClass33Array1660[otherVertexIndex];
								if (shiftedX == otherX[otherVertexIndex] && shiftedZ == other.anIntArray1629[otherVertexIndex] && shiftedY == other.anIntArray1628[otherVertexIndex] && otherOriginalNormal.anInt605 != 0) {
									normal.anInt602 += otherOriginalNormal.anInt602;
									normal.anInt603 += otherOriginalNormal.anInt603;
									normal.anInt604 += otherOriginalNormal.anInt604;
									normal.anInt605 += otherOriginalNormal.anInt605;
									otherNormal.anInt602 += originalNormal.anInt602;
									otherNormal.anInt603 += originalNormal.anInt603;
									otherNormal.anInt604 += originalNormal.anInt604;
									otherNormal.anInt605 += originalNormal.anInt605;
									sharedCount++;
									modelSharedVertexStamps[vertexIndex] = mergeStamp;
									otherSharedVertexStamps[otherVertexIndex] = mergeStamp;
								}
							}

						}
					}
				}
			}
		}

		if (sharedCount < 3 || !hideSharedFaces) {
			return;
		}
		for (int faceIndex = 0; faceIndex < model.anInt1630; faceIndex++) {
			if (modelSharedVertexStamps[model.anIntArray1631[faceIndex]] == mergeStamp && modelSharedVertexStamps[model.anIntArray1632[faceIndex]] == mergeStamp && modelSharedVertexStamps[model.anIntArray1633[faceIndex]] == mergeStamp) {
				model.anIntArray1637[faceIndex] = -1;
			}
		}

		for (int otherFaceIndex = 0; otherFaceIndex < other.anInt1630; otherFaceIndex++) {
			if (otherSharedVertexStamps[other.anIntArray1631[otherFaceIndex]] == mergeStamp && otherSharedVertexStamps[other.anIntArray1632[otherFaceIndex]] == mergeStamp && otherSharedVertexStamps[other.anIntArray1633[otherFaceIndex]] == mergeStamp) {
				other.anIntArray1637[otherFaceIndex] = -1;
			}
		}

	}

	public void drawMinimapTile(int pixels[], int pixelOffset, int plane, int tileX, int tileY) {
		int pixelStride = 512;// was parameter
		Ground ground = groundArray[plane][tileX][tileY];
		if (ground == null) {
			return;
		}
		Class43 plainTile = ground.aClass43_1311;
		if (plainTile != null) {
			int tileColour = plainTile.mapColour;
			if (tileColour == 0) {
				return;
			}
			for (int row = 0; row < 4; row++) {
				pixels[pixelOffset] = tileColour;
				pixels[pixelOffset + 1] = tileColour;
				pixels[pixelOffset + 2] = tileColour;
				pixels[pixelOffset + 3] = tileColour;
				pixelOffset += pixelStride;
			}

			return;
		}
		Class40 shapedTile = ground.aClass40_1312;
		if (shapedTile == null) {
			return;
		}
		int shape = shapedTile.shape;
		int rotation = shapedTile.rotation;
		int baseColour = shapedTile.underlayMapColour;
		int maskedColour = shapedTile.overlayMapColour;
		int shapeMask[] = tileShapeMasks[shape];
		int rotationOrder[] = tileRotationOrders[rotation];
		int maskPosition = 0;
		if (baseColour != 0) {
			for (int row = 0; row < 4; row++) {
				pixels[pixelOffset] = shapeMask[rotationOrder[maskPosition++]] != 0 ? maskedColour : baseColour;
				pixels[pixelOffset + 1] = shapeMask[rotationOrder[maskPosition++]] != 0 ? maskedColour : baseColour;
				pixels[pixelOffset + 2] = shapeMask[rotationOrder[maskPosition++]] != 0 ? maskedColour : baseColour;
				pixels[pixelOffset + 3] = shapeMask[rotationOrder[maskPosition++]] != 0 ? maskedColour : baseColour;
				pixelOffset += pixelStride;
			}

			return;
		}
		for (int row = 0; row < 4; row++) {
			if (shapeMask[rotationOrder[maskPosition++]] != 0) {
				pixels[pixelOffset] = maskedColour;
			}
			if (shapeMask[rotationOrder[maskPosition++]] != 0) {
				pixels[pixelOffset + 1] = maskedColour;
			}
			if (shapeMask[rotationOrder[maskPosition++]] != 0) {
				pixels[pixelOffset + 2] = maskedColour;
			}
			if (shapeMask[rotationOrder[maskPosition++]] != 0) {
				pixels[pixelOffset + 3] = maskedColour;
			}
			pixelOffset += pixelStride;
		}

	}

	public static void buildTileVisibilityTable(int sampleSpanBefore, int sampleSpanAfter, int viewportWidth, int viewportHeight, int cameraHeightByPitch[]) {
		viewportMinX = 0;
		viewportMinY = 0;
		viewportMaxX = viewportWidth;
		viewportMaxY = viewportHeight;
		viewportCentreX = viewportWidth / 2;
		viewportCentreY = viewportHeight / 2;
		boolean pointVisible[][][][] = new boolean[9][32][256][256];
		for (int pitch = 128; pitch <= 384; pitch += 32) {
			for (int yaw = 0; yaw < 2048; yaw += 64) {
				pitchSine = Model.sineTable16[pitch];
				pitchCosine = Model.cosineTable16[pitch];
				yawSine = Model.sineTable16[yaw];
				yawCosine = Model.cosineTable16[yaw];
				int pitchIndex = (pitch - 128) / 32;
				int yawIndex = yaw / 64;
				for (int tileOffsetX = -(drawDistance + 1); tileOffsetX <= (drawDistance + 1); tileOffsetX++) {
					for (int tileOffsetZ = -(drawDistance + 1); tileOffsetZ <= (drawDistance + 1); tileOffsetZ++) {
						int worldX = tileOffsetX * 128;
						int worldZ = tileOffsetZ * 128;
						boolean pointSeen = false;
						for (int heightSample = -sampleSpanBefore; heightSample <= sampleSpanAfter; heightSample += 128) {
							if (!isPointInViewport(cameraHeightByPitch[pitchIndex] + heightSample, worldZ, worldX)) {
								continue;
							}
							pointSeen = true;
							break;
						}

						pointVisible[pitchIndex][yawIndex][tileOffsetX + drawDistance + 1][tileOffsetZ + drawDistance + 1] = pointSeen;
					}

				}

			}

		}

		for (int pitchIndex = 0; pitchIndex < 8; pitchIndex++) {
			for (int yawIndex = 0; yawIndex < 32; yawIndex++) {
				for (int tileOffsetX = -drawDistance; tileOffsetX < drawDistance; tileOffsetX++) {
					for (int tileOffsetZ = -drawDistance; tileOffsetZ < drawDistance; tileOffsetZ++) {
						boolean tileVisible = false;
						neighbourSearch : for (int neighbourX = -1; neighbourX <= 1; neighbourX++) {
							for (int neighbourZ = -1; neighbourZ <= 1; neighbourZ++) {
								if (pointVisible[pitchIndex][yawIndex][tileOffsetX + neighbourX + drawDistance + 1][tileOffsetZ + neighbourZ + drawDistance + 1]) {
									tileVisible = true;
								} else if (pointVisible[pitchIndex][(yawIndex + 1) % 31][tileOffsetX + neighbourX + drawDistance + 1][tileOffsetZ + neighbourZ + drawDistance + 1]) {
									tileVisible = true;
								} else if (pointVisible[pitchIndex + 1][yawIndex][tileOffsetX + neighbourX + drawDistance + 1][tileOffsetZ + neighbourZ + drawDistance + 1]) {
									tileVisible = true;
								} else {
									if (!pointVisible[pitchIndex + 1][(yawIndex + 1) % 31][tileOffsetX + neighbourX + drawDistance + 1][tileOffsetZ + neighbourZ + drawDistance + 1]) {
										continue;
									}
									tileVisible = true;
								}
								break neighbourSearch;
							}

						}

						tileVisibilityTable[pitchIndex][yawIndex][tileOffsetX + drawDistance][tileOffsetZ + drawDistance] = tileVisible;
					}

				}

			}

		}

	}

	private static boolean isPointInViewport(int height, int z, int x) {
		int rotatedX = z * yawSine + x * yawCosine >> 16;
		int rotatedZ = z * yawCosine - x * yawSine >> 16;
		int depth = height * pitchSine + rotatedZ * pitchCosine >> 16;
		int verticalOffset = height * pitchCosine - rotatedZ * pitchSine >> 16;
		if (depth < 50 || depth > 3500) {
			return false;
		}
		int screenX = viewportCentreX + (rotatedX << 9) / depth;
		int screenY = viewportCentreY + (verticalOffset << 9) / depth;
		return screenX >= viewportMinX && screenX <= viewportMaxX && screenY >= viewportMinY && screenY <= viewportMaxY;
	}

	public void requestTilePick(int screenY, int screenX) {
		tilePickRequested = true;
		pickScreenX = screenX;
		pickScreenY = screenY;
		pickedTileX = -1;
		pickedTileY = -1;
	}

	public void drawScene(int cameraX, int cameraY, int yaw, int cameraZ, int plane, int pitch) {
		if (cameraX < 0) {
			cameraX = 0;
		} else if (cameraX >= tileCountX * 128) {
			cameraX = tileCountX * 128 - 1;
		}
		if (cameraY < 0) {
			cameraY = 0;
		} else if (cameraY >= tileCountY * 128) {
			cameraY = tileCountY * 128 - 1;
		}
		drawCycle++;
		pitchSine = Model.sineTable16[pitch];
		pitchCosine = Model.cosineTable16[pitch];
		yawSine = Model.sineTable16[yaw];
		yawCosine = Model.cosineTable16[yaw];
		cameraTileVisibility = tileVisibilityTable[(pitch - 128) / 32][yaw / 64];
		cameraWorldX = cameraX;
		cameraWorldHeight = cameraZ;
		cameraWorldY = cameraY;
		cameraTileX = cameraX / 128;
		cameraTileY = cameraY / 128;
		cameraPlane = plane;
		drawMinTileX = cameraTileX - drawDistance;
		if (drawMinTileX < 0) {
			drawMinTileX = 0;
		}
		drawMinTileY = cameraTileY - drawDistance;
		if (drawMinTileY < 0) {
			drawMinTileY = 0;
		}
		drawMaxTileX = cameraTileX + drawDistance;
		if (drawMaxTileX > tileCountX) {
			drawMaxTileX = tileCountX;
		}
		drawMaxTileY = cameraTileY + drawDistance;
		if (drawMaxTileY > tileCountY) {
			drawMaxTileY = tileCountY;
		}
		selectActiveOccluders();
		pendingTileCount = 0;
		for (int planeIndex = lowestPlane; planeIndex < planeCount; planeIndex++) {
			Ground planeTiles[][] = groundArray[planeIndex];
			for (int tileX = drawMinTileX; tileX < drawMaxTileX; tileX++) {
				for (int tileY = drawMinTileY; tileY < drawMaxTileY; tileY++) {
					Ground tile = planeTiles[tileX][tileY];
					if (tile != null) {
						if (tile.anInt1321 > plane || !cameraTileVisibility[tileX - cameraTileX + drawDistance][tileY - cameraTileY + drawDistance] && tileHeights[planeIndex][tileX][tileY] - cameraZ < 50) {
							tile.aBoolean1322 = false;
							tile.aBoolean1323 = false;
							tile.anInt1325 = 0;
						} else {
							tile.aBoolean1322 = true;
							tile.aBoolean1323 = true;
							tile.aBoolean1324 = tile.anInt1317 > 0;
							pendingTileCount++;
						}
					}
				}

			}

		}

		for (int planeIndex = lowestPlane; planeIndex < planeCount; planeIndex++) {
			Ground planeTiles[][] = groundArray[planeIndex];
			for (int offsetX = -drawDistance; offsetX <= 0; offsetX++) {
				int lowX = cameraTileX + offsetX;
				int highX = cameraTileX - offsetX;
				if (lowX >= drawMinTileX || highX < drawMaxTileX) {
					for (int offsetY = -drawDistance; offsetY <= 0; offsetY++) {
						int lowY = cameraTileY + offsetY;
						int highY = cameraTileY - offsetY;
						if (lowX >= drawMinTileX) {
							if (lowY >= drawMinTileY) {
								Ground tile = planeTiles[lowX][lowY];
								if (tile != null && tile.aBoolean1322) {
									method314(tile, true);
								}
							}
							if (highY < drawMaxTileY) {
								Ground tile = planeTiles[lowX][highY];
								if (tile != null && tile.aBoolean1322) {
									method314(tile, true);
								}
							}
						}
						if (highX < drawMaxTileX) {
							if (lowY >= drawMinTileY) {
								Ground tile = planeTiles[highX][lowY];
								if (tile != null && tile.aBoolean1322) {
									method314(tile, true);
								}
							}
							if (highY < drawMaxTileY) {
								Ground tile = planeTiles[highX][highY];
								if (tile != null && tile.aBoolean1322) {
									method314(tile, true);
								}
							}
						}
						if (pendingTileCount == 0) {
							tilePickRequested = false;
							return;
						}
					}

				}
			}

		}

		for (int planeIndex = lowestPlane; planeIndex < planeCount; planeIndex++) {
			Ground planeTiles[][] = groundArray[planeIndex];
			for (int offsetX = -drawDistance; offsetX <= 0; offsetX++) {
				int lowX = cameraTileX + offsetX;
				int highX = cameraTileX - offsetX;
				if (lowX >= drawMinTileX || highX < drawMaxTileX) {
					for (int offsetY = -drawDistance; offsetY <= 0; offsetY++) {
						int lowY = cameraTileY + offsetY;
						int highY = cameraTileY - offsetY;
						if (lowX >= drawMinTileX) {
							if (lowY >= drawMinTileY) {
								Ground tile = planeTiles[lowX][lowY];
								if (tile != null && tile.aBoolean1322) {
									method314(tile, false);
								}
							}
							if (highY < drawMaxTileY) {
								Ground tile = planeTiles[lowX][highY];
								if (tile != null && tile.aBoolean1322) {
									method314(tile, false);
								}
							}
						}
						if (highX < drawMaxTileX) {
							if (lowY >= drawMinTileY) {
								Ground tile = planeTiles[highX][lowY];
								if (tile != null && tile.aBoolean1322) {
									method314(tile, false);
								}
							}
							if (highY < drawMaxTileY) {
								Ground tile = planeTiles[highX][highY];
								if (tile != null && tile.aBoolean1322) {
									method314(tile, false);
								}
							}
						}
						if (pendingTileCount == 0) {
							tilePickRequested = false;
							return;
						}
					}

				}
			}

		}

		tilePickRequested = false;
	}

	private void method314(Ground class30_sub3, boolean flag) {
		aClass19_477.insertHead(class30_sub3);
		do {
			Ground class30_sub3_1;
			do {
				class30_sub3_1 = (Ground) aClass19_477.popTail();
				if (class30_sub3_1 == null) {
					return;
				}
			} while (!class30_sub3_1.aBoolean1323);
			int i = class30_sub3_1.anInt1308;
			int j = class30_sub3_1.anInt1309;
			int k = class30_sub3_1.anInt1307;
			int l = class30_sub3_1.anInt1310;
			Ground aclass30_sub3[][] = groundArray[k];
			if (class30_sub3_1.aBoolean1322) {
				if (flag) {
					if (k > 0) {
						Ground class30_sub3_2 = groundArray[k - 1][i][j];
						if (class30_sub3_2 != null && class30_sub3_2.aBoolean1323) {
							continue;
						}
					}
					if (i <= cameraTileX && i > drawMinTileX) {
						Ground class30_sub3_3 = aclass30_sub3[i - 1][j];
						if (class30_sub3_3 != null && class30_sub3_3.aBoolean1323 && (class30_sub3_3.aBoolean1322 || (class30_sub3_1.anInt1320 & 1) == 0)) {
							continue;
						}
					}
					if (i >= cameraTileX && i < drawMaxTileX - 1) {
						Ground class30_sub3_4 = aclass30_sub3[i + 1][j];
						if (class30_sub3_4 != null && class30_sub3_4.aBoolean1323 && (class30_sub3_4.aBoolean1322 || (class30_sub3_1.anInt1320 & 4) == 0)) {
							continue;
						}
					}
					if (j <= cameraTileY && j > drawMinTileY) {
						Ground class30_sub3_5 = aclass30_sub3[i][j - 1];
						if (class30_sub3_5 != null && class30_sub3_5.aBoolean1323 && (class30_sub3_5.aBoolean1322 || (class30_sub3_1.anInt1320 & 8) == 0)) {
							continue;
						}
					}
					if (j >= cameraTileY && j < drawMaxTileY - 1) {
						Ground class30_sub3_6 = aclass30_sub3[i][j + 1];
						if (class30_sub3_6 != null && class30_sub3_6.aBoolean1323 && (class30_sub3_6.aBoolean1322 || (class30_sub3_1.anInt1320 & 2) == 0)) {
							continue;
						}
					}
				} else {
					flag = true;
				}
				class30_sub3_1.aBoolean1322 = false;
				if (class30_sub3_1.aClass30_Sub3_1329 != null) {
					Ground class30_sub3_7 = class30_sub3_1.aClass30_Sub3_1329;
					if (class30_sub3_7.aClass43_1311 != null) {
						if (!isTileOccluded(0, i, j)) {
							drawPlainTile(class30_sub3_7.aClass43_1311, 0, pitchSine, pitchCosine, yawSine, yawCosine, i, j);
						}
					} else if (class30_sub3_7.aClass40_1312 != null && !isTileOccluded(0, i, j)) {
						drawShapedTile(i, pitchSine, yawSine, class30_sub3_7.aClass40_1312, pitchCosine, j, yawCosine);
					}
					Object1 class10 = class30_sub3_7.obj1;
					if (class10 != null) {
						class10.aClass30_Sub2_Sub4_278.method443(0, pitchSine, pitchCosine, yawSine, yawCosine, class10.anInt274 - cameraWorldX, class10.anInt273 - cameraWorldHeight, class10.anInt275 - cameraWorldY, class10.uid);
					}
					for (int i2 = 0; i2 < class30_sub3_7.anInt1317; i2++) {
						Object5 class28 = class30_sub3_7.obj5Array[i2];
						if (class28 != null) {
							class28.aClass30_Sub2_Sub4_521.method443(class28.anInt522, pitchSine, pitchCosine, yawSine, yawCosine, class28.anInt519 - cameraWorldX, class28.anInt518 - cameraWorldHeight, class28.anInt520 - cameraWorldY, class28.uid);
						}
					}

				}
				boolean flag1 = false;
				if (class30_sub3_1.aClass43_1311 != null) {
					if (!isTileOccluded(l, i, j)) {
						flag1 = true;
						drawPlainTile(class30_sub3_1.aClass43_1311, l, pitchSine, pitchCosine, yawSine, yawCosine, i, j);
					}
				} else if (class30_sub3_1.aClass40_1312 != null && !isTileOccluded(l, i, j)) {
					flag1 = true;
					drawShapedTile(i, pitchSine, yawSine, class30_sub3_1.aClass40_1312, pitchCosine, j, yawCosine);
				}
				int j1 = 0;
				int j2 = 0;
				Object1 class10_3 = class30_sub3_1.obj1;
				Object2 class26_1 = class30_sub3_1.obj2;
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
					if ((class10_3.orientation & j2) != 0 && !isWallOccluded(l, i, j, class10_3.orientation)) {
						class10_3.aClass30_Sub2_Sub4_278.method443(0, pitchSine, pitchCosine, yawSine, yawCosine, class10_3.anInt274 - cameraWorldX, class10_3.anInt273 - cameraWorldHeight, class10_3.anInt275 - cameraWorldY, class10_3.uid);
					}
					if ((class10_3.orientation1 & j2) != 0 && !isWallOccluded(l, i, j, class10_3.orientation1)) {
						class10_3.aClass30_Sub2_Sub4_279.method443(0, pitchSine, pitchCosine, yawSine, yawCosine, class10_3.anInt274 - cameraWorldX, class10_3.anInt273 - cameraWorldHeight, class10_3.anInt275 - cameraWorldY, class10_3.uid);
					}
				}
				if (class26_1 != null && !isTileColumnOccluded(l, i, j, class26_1.aClass30_Sub2_Sub4_504.modelHeight)) {
					if ((class26_1.anInt502 & j2) != 0) {
						class26_1.aClass30_Sub2_Sub4_504.method443(class26_1.anInt503, pitchSine, pitchCosine, yawSine, yawCosine, class26_1.anInt500 - cameraWorldX, class26_1.anInt499 - cameraWorldHeight, class26_1.anInt501 - cameraWorldY, class26_1.uid);
					} else if ((class26_1.anInt502 & 0x300) != 0) {
						int j4 = class26_1.anInt500 - cameraWorldX;
						int l5 = class26_1.anInt499 - cameraWorldHeight;
						int k6 = class26_1.anInt501 - cameraWorldY;
						int i8 = class26_1.anInt503;
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
						if ((class26_1.anInt502 & 0x100) != 0 && k10 < k9) {
							int i11 = j4 + anIntArray463[i8];
							int k11 = k6 + anIntArray464[i8];
							class26_1.aClass30_Sub2_Sub4_504.method443(i8 * 512 + 256, pitchSine, pitchCosine, yawSine, yawCosine, i11, l5, k11, class26_1.uid);
						}
						if ((class26_1.anInt502 & 0x200) != 0 && k10 > k9) {
							int j11 = j4 + anIntArray465[i8];
							int l11 = k6 + anIntArray466[i8];
							class26_1.aClass30_Sub2_Sub4_504.method443(i8 * 512 + 1280 & 0x7ff, pitchSine, pitchCosine, yawSine, yawCosine, j11, l5, l11, class26_1.uid);
						}
					}
				}
				if (flag1) {
					Object3 class49 = class30_sub3_1.obj3;
					if (class49 != null) {
						class49.aClass30_Sub2_Sub4_814.method443(0, pitchSine, pitchCosine, yawSine, yawCosine, class49.anInt812 - cameraWorldX, class49.anInt811 - cameraWorldHeight, class49.anInt813 - cameraWorldY, class49.uid);
					}
					Object4 object4_1 = class30_sub3_1.obj4;
					if (object4_1 != null && object4_1.anInt52 == 0) {
						if (object4_1.aClass30_Sub2_Sub4_49 != null) {
							object4_1.aClass30_Sub2_Sub4_49.method443(0, pitchSine, pitchCosine, yawSine, yawCosine, object4_1.anInt46 - cameraWorldX, object4_1.anInt45 - cameraWorldHeight, object4_1.anInt47 - cameraWorldY, object4_1.uid);
						}
						if (object4_1.aClass30_Sub2_Sub4_50 != null) {
							object4_1.aClass30_Sub2_Sub4_50.method443(0, pitchSine, pitchCosine, yawSine, yawCosine, object4_1.anInt46 - cameraWorldX, object4_1.anInt45 - cameraWorldHeight, object4_1.anInt47 - cameraWorldY, object4_1.uid);
						}
						if (object4_1.aClass30_Sub2_Sub4_48 != null) {
							object4_1.aClass30_Sub2_Sub4_48.method443(0, pitchSine, pitchCosine, yawSine, yawCosine, object4_1.anInt46 - cameraWorldX, object4_1.anInt45 - cameraWorldHeight, object4_1.anInt47 - cameraWorldY, object4_1.uid);
						}
					}
				}
				int k4 = class30_sub3_1.anInt1320;
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
				for (int k1 = 0; k1 < class30_sub3_1.anInt1317; k1++) {
					if (class30_sub3_1.obj5Array[k1].anInt528 == drawCycle || (class30_sub3_1.anIntArray1319[k1] & class30_sub3_1.anInt1325) != class30_sub3_1.anInt1326) {
						continue;
					}
					flag2 = false;
					break;
				}

				if (flag2) {
					Object1 class10_1 = class30_sub3_1.obj1;
					if (!isWallOccluded(l, i, j, class10_1.orientation)) {
						class10_1.aClass30_Sub2_Sub4_278.method443(0, pitchSine, pitchCosine, yawSine, yawCosine, class10_1.anInt274 - cameraWorldX, class10_1.anInt273 - cameraWorldHeight, class10_1.anInt275 - cameraWorldY, class10_1.uid);
					}
					class30_sub3_1.anInt1325 = 0;
				}
			}
			if (class30_sub3_1.aBoolean1324) {
				try {
					int i1 = class30_sub3_1.anInt1317;
					class30_sub3_1.aBoolean1324 = false;
					int l1 = 0;
					label0 : for (int k2 = 0; k2 < i1; k2++) {
						Object5 class28_1 = class30_sub3_1.obj5Array[k2];
						if (class28_1.anInt528 == drawCycle) {
							continue;
						}
						for (int k3 = class28_1.anInt523; k3 <= class28_1.anInt524; k3++) {
							for (int l4 = class28_1.anInt525; l4 <= class28_1.anInt526; l4++) {
								Ground class30_sub3_21 = aclass30_sub3[k3][l4];
								if (class30_sub3_21.aBoolean1322) {
									class30_sub3_1.aBoolean1324 = true;
								} else {
									if (class30_sub3_21.anInt1325 == 0) {
										continue;
									}
									int l6 = 0;
									if (k3 > class28_1.anInt523) {
										l6++;
									}
									if (k3 < class28_1.anInt524) {
										l6 += 4;
									}
									if (l4 > class28_1.anInt525) {
										l6 += 8;
									}
									if (l4 < class28_1.anInt526) {
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
						int i5 = cameraTileX - class28_1.anInt523;
						int i6 = class28_1.anInt524 - cameraTileX;
						if (i6 > i5) {
							i5 = i6;
						}
						int i7 = cameraTileY - class28_1.anInt525;
						int j8 = class28_1.anInt526 - cameraTileY;
						if (j8 > i7) {
							class28_1.anInt527 = i5 + j8;
						} else {
							class28_1.anInt527 = i5 + i7;
						}
					}

					while (l1 > 0) {
						int i3 = -50;
						int l3 = -1;
						for (int j5 = 0; j5 < l1; j5++) {
							Object5 class28_2 = aClass28Array462[j5];
							if (class28_2.anInt528 != drawCycle) {
								if (class28_2.anInt527 > i3) {
									i3 = class28_2.anInt527;
									l3 = j5;
								} else if (class28_2.anInt527 == i3) {
									int j7 = class28_2.anInt519 - cameraWorldX;
									int k8 = class28_2.anInt520 - cameraWorldY;
									int l9 = aClass28Array462[l3].anInt519 - cameraWorldX;
									int l10 = aClass28Array462[l3].anInt520 - cameraWorldY;
									if (j7 * j7 + k8 * k8 > l9 * l9 + l10 * l10) {
										l3 = j5;
									}
								}
							}
						}

						if (l3 == -1) {
							break;
						}
						Object5 class28_3 = aClass28Array462[l3];
						class28_3.anInt528 = drawCycle;
						if (!isFootprintOccluded(l, class28_3.anInt523, class28_3.anInt524, class28_3.anInt525, class28_3.anInt526, class28_3.aClass30_Sub2_Sub4_521.modelHeight)) {
							class28_3.aClass30_Sub2_Sub4_521.method443(class28_3.anInt522, pitchSine, pitchCosine, yawSine, yawCosine, class28_3.anInt519 - cameraWorldX, class28_3.anInt518 - cameraWorldHeight, class28_3.anInt520 - cameraWorldY, class28_3.uid);
						}
						for (int k7 = class28_3.anInt523; k7 <= class28_3.anInt524; k7++) {
							for (int l8 = class28_3.anInt525; l8 <= class28_3.anInt526; l8++) {
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
			if (i <= cameraTileX && i > drawMinTileX) {
				Ground class30_sub3_8 = aclass30_sub3[i - 1][j];
				if (class30_sub3_8 != null && class30_sub3_8.aBoolean1323) {
					continue;
				}
			}
			if (i >= cameraTileX && i < drawMaxTileX - 1) {
				Ground class30_sub3_9 = aclass30_sub3[i + 1][j];
				if (class30_sub3_9 != null && class30_sub3_9.aBoolean1323) {
					continue;
				}
			}
			if (j <= cameraTileY && j > drawMinTileY) {
				Ground class30_sub3_10 = aclass30_sub3[i][j - 1];
				if (class30_sub3_10 != null && class30_sub3_10.aBoolean1323) {
					continue;
				}
			}
			if (j >= cameraTileY && j < drawMaxTileY - 1) {
				Ground class30_sub3_11 = aclass30_sub3[i][j + 1];
				if (class30_sub3_11 != null && class30_sub3_11.aBoolean1323) {
					continue;
				}
			}
			class30_sub3_1.aBoolean1323 = false;
			pendingTileCount--;
			Object4 object4 = class30_sub3_1.obj4;
			if (object4 != null && object4.anInt52 != 0) {
				if (object4.aClass30_Sub2_Sub4_49 != null) {
					object4.aClass30_Sub2_Sub4_49.method443(0, pitchSine, pitchCosine, yawSine, yawCosine, object4.anInt46 - cameraWorldX, object4.anInt45 - cameraWorldHeight - object4.anInt52, object4.anInt47 - cameraWorldY, object4.uid);
				}
				if (object4.aClass30_Sub2_Sub4_50 != null) {
					object4.aClass30_Sub2_Sub4_50.method443(0, pitchSine, pitchCosine, yawSine, yawCosine, object4.anInt46 - cameraWorldX, object4.anInt45 - cameraWorldHeight - object4.anInt52, object4.anInt47 - cameraWorldY, object4.uid);
				}
				if (object4.aClass30_Sub2_Sub4_48 != null) {
					object4.aClass30_Sub2_Sub4_48.method443(0, pitchSine, pitchCosine, yawSine, yawCosine, object4.anInt46 - cameraWorldX, object4.anInt45 - cameraWorldHeight - object4.anInt52, object4.anInt47 - cameraWorldY, object4.uid);
				}
			}
			if (class30_sub3_1.anInt1328 != 0) {
				Object2 class26 = class30_sub3_1.obj2;
				if (class26 != null && !isTileColumnOccluded(l, i, j, class26.aClass30_Sub2_Sub4_504.modelHeight)) {
					if ((class26.anInt502 & class30_sub3_1.anInt1328) != 0) {
						class26.aClass30_Sub2_Sub4_504.method443(class26.anInt503, pitchSine, pitchCosine, yawSine, yawCosine, class26.anInt500 - cameraWorldX, class26.anInt499 - cameraWorldHeight, class26.anInt501 - cameraWorldY, class26.uid);
					} else if ((class26.anInt502 & 0x300) != 0) {
						int l2 = class26.anInt500 - cameraWorldX;
						int j3 = class26.anInt499 - cameraWorldHeight;
						int i4 = class26.anInt501 - cameraWorldY;
						int k5 = class26.anInt503;
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
						if ((class26.anInt502 & 0x100) != 0 && l7 >= j6) {
							int i9 = l2 + anIntArray463[k5];
							int i10 = i4 + anIntArray464[k5];
							class26.aClass30_Sub2_Sub4_504.method443(k5 * 512 + 256, pitchSine, pitchCosine, yawSine, yawCosine, i9, j3, i10, class26.uid);
						}
						if ((class26.anInt502 & 0x200) != 0 && l7 <= j6) {
							int j9 = l2 + anIntArray465[k5];
							int j10 = i4 + anIntArray466[k5];
							class26.aClass30_Sub2_Sub4_504.method443(k5 * 512 + 1280 & 0x7ff, pitchSine, pitchCosine, yawSine, yawCosine, j9, j3, j10, class26.uid);
						}
					}
				}
				Object1 class10_2 = class30_sub3_1.obj1;
				if (class10_2 != null) {
					if ((class10_2.orientation1 & class30_sub3_1.anInt1328) != 0 && !isWallOccluded(l, i, j, class10_2.orientation1)) {
						class10_2.aClass30_Sub2_Sub4_279.method443(0, pitchSine, pitchCosine, yawSine, yawCosine, class10_2.anInt274 - cameraWorldX, class10_2.anInt273 - cameraWorldHeight, class10_2.anInt275 - cameraWorldY, class10_2.uid);
					}
					if ((class10_2.orientation & class30_sub3_1.anInt1328) != 0 && !isWallOccluded(l, i, j, class10_2.orientation)) {
						class10_2.aClass30_Sub2_Sub4_278.method443(0, pitchSine, pitchCosine, yawSine, yawCosine, class10_2.anInt274 - cameraWorldX, class10_2.anInt273 - cameraWorldHeight, class10_2.anInt275 - cameraWorldY, class10_2.uid);
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

	private void drawPlainTile(Class43 tile, int plane, int pitchSine, int pitchCosine, int yawSine, int yawCosine, int tileX, int tileY) {
		int x3;
		int x0 = x3 = (tileX << 7) - cameraWorldX;
		int depth1;
		int depth0 = depth1 = (tileY << 7) - cameraWorldY;
		int x2;
		int x1 = x2 = x0 + 128;
		int depth3;
		int depth2 = depth3 = depth0 + 128;
		int height0 = tileHeights[plane][tileX][tileY] - cameraWorldHeight;
		int height1 = tileHeights[plane][tileX + 1][tileY] - cameraWorldHeight;
		int height2 = tileHeights[plane][tileX + 1][tileY + 1] - cameraWorldHeight;
		int height3 = tileHeights[plane][tileX][tileY + 1] - cameraWorldHeight;
		int rotated = depth0 * yawSine + x0 * yawCosine >> 16;
		depth0 = depth0 * yawCosine - x0 * yawSine >> 16;
		x0 = rotated;
		rotated = height0 * pitchCosine - depth0 * pitchSine >> 16;
		depth0 = height0 * pitchSine + depth0 * pitchCosine >> 16;
		height0 = rotated;
		if (depth0 < 50) {
			return;
		}
		rotated = depth1 * yawSine + x1 * yawCosine >> 16;
		depth1 = depth1 * yawCosine - x1 * yawSine >> 16;
		x1 = rotated;
		rotated = height1 * pitchCosine - depth1 * pitchSine >> 16;
		depth1 = height1 * pitchSine + depth1 * pitchCosine >> 16;
		height1 = rotated;
		if (depth1 < 50) {
			return;
		}
		rotated = depth2 * yawSine + x2 * yawCosine >> 16;
		depth2 = depth2 * yawCosine - x2 * yawSine >> 16;
		x2 = rotated;
		rotated = height2 * pitchCosine - depth2 * pitchSine >> 16;
		depth2 = height2 * pitchSine + depth2 * pitchCosine >> 16;
		height2 = rotated;
		if (depth2 < 50) {
			return;
		}
		rotated = depth3 * yawSine + x3 * yawCosine >> 16;
		depth3 = depth3 * yawCosine - x3 * yawSine >> 16;
		x3 = rotated;
		rotated = height3 * pitchCosine - depth3 * pitchSine >> 16;
		depth3 = height3 * pitchSine + depth3 * pitchCosine >> 16;
		height3 = rotated;
		if (depth3 < 50) {
			return;
		}
		int screenX0 = Texture.textureInt1 + (x0 << 9) / depth0;
		int screenY0 = Texture.textureInt2 + (height0 << 9) / depth0;
		int screenX1 = Texture.textureInt1 + (x1 << 9) / depth1;
		int screenY1 = Texture.textureInt2 + (height1 << 9) / depth1;
		int screenX2 = Texture.textureInt1 + (x2 << 9) / depth2;
		int screenY2 = Texture.textureInt2 + (height2 << 9) / depth2;
		int screenX3 = Texture.textureInt1 + (x3 << 9) / depth3;
		int screenY3 = Texture.textureInt2 + (height3 << 9) / depth3;
		Texture.transparency = 0;
		if ((screenX2 - screenX3) * (screenY1 - screenY3) - (screenY2 - screenY3) * (screenX1 - screenX3) > 0) {
			Texture.clipScanlines = screenX2 < 0 || screenX3 < 0 || screenX1 < 0 || screenX2 > DrawingArea.scanlineRightLimit || screenX3 > DrawingArea.scanlineRightLimit || screenX1 > DrawingArea.scanlineRightLimit;
			if (tilePickRequested && isPointInTriangle(pickScreenX, pickScreenY, screenY2, screenY3, screenY1, screenX2, screenX3, screenX1)) {
				pickedTileX = tileX;
				pickedTileY = tileY;
			}
			if (tile.textureId == -1) {
				if (tile.cornerColour2 != 0xbc614e) {
					Texture.drawShadedTriangle(screenY2, screenY3, screenY1, screenX2, screenX3, screenX1, tile.cornerColour2, tile.cornerColour3, tile.cornerColour1);
				}
			} else if (!lowMem) {
				if (tile.flat) {
					Texture.drawTexturedTriangle(screenY2, screenY3, screenY1, screenX2, screenX3, screenX1, tile.cornerColour2, tile.cornerColour3, tile.cornerColour1, x0, x1, x3, height0, height1, height3, depth0, depth1, depth3, tile.textureId);
				} else {
					Texture.drawTexturedTriangle(screenY2, screenY3, screenY1, screenX2, screenX3, screenX1, tile.cornerColour2, tile.cornerColour3, tile.cornerColour1, x2, x3, x1, height2, height3, height1, depth2, depth3, depth1, tile.textureId);
				}
			} else {
				int textureHsl = lowMemTextureHsl[tile.textureId];
				Texture.drawShadedTriangle(screenY2, screenY3, screenY1, screenX2, screenX3, screenX1, shadeTextureHsl(textureHsl, tile.cornerColour2), shadeTextureHsl(textureHsl, tile.cornerColour3), shadeTextureHsl(textureHsl, tile.cornerColour1));
			}
		}
		if ((screenX0 - screenX1) * (screenY3 - screenY1) - (screenY0 - screenY1) * (screenX3 - screenX1) > 0) {
			Texture.clipScanlines = screenX0 < 0 || screenX1 < 0 || screenX3 < 0 || screenX0 > DrawingArea.scanlineRightLimit || screenX1 > DrawingArea.scanlineRightLimit || screenX3 > DrawingArea.scanlineRightLimit;
			if (tilePickRequested && isPointInTriangle(pickScreenX, pickScreenY, screenY0, screenY1, screenY3, screenX0, screenX1, screenX3)) {
				pickedTileX = tileX;
				pickedTileY = tileY;
			}
			if (tile.textureId == -1) {
				if (tile.cornerColour0 != 0xbc614e) {
					Texture.drawShadedTriangle(screenY0, screenY1, screenY3, screenX0, screenX1, screenX3, tile.cornerColour0, tile.cornerColour1, tile.cornerColour3);
				}
			} else {
				if (!lowMem) {
					Texture.drawTexturedTriangle(screenY0, screenY1, screenY3, screenX0, screenX1, screenX3, tile.cornerColour0, tile.cornerColour1, tile.cornerColour3, x0, x1, x3, height0, height1, height3, depth0, depth1, depth3, tile.textureId);
					return;
				}
				int textureHsl = lowMemTextureHsl[tile.textureId];
				Texture.drawShadedTriangle(screenY0, screenY1, screenY3, screenX0, screenX1, screenX3, shadeTextureHsl(textureHsl, tile.cornerColour0), shadeTextureHsl(textureHsl, tile.cornerColour1), shadeTextureHsl(textureHsl, tile.cornerColour3));
			}
		}
	}

	private void drawShapedTile(int tileX, int pitchSine, int yawSine, Class40 tile, int pitchCosine, int tileY, int yawCosine) {
		int count = tile.vertexX.length;
		for (int vertex = 0; vertex < count; vertex++) {
			int x = tile.vertexX[vertex] - cameraWorldX;
			int height = tile.vertexHeight[vertex] - cameraWorldHeight;
			int depth = tile.vertexY[vertex] - cameraWorldY;
			int rotated = depth * yawSine + x * yawCosine >> 16;
			depth = depth * yawCosine - x * yawSine >> 16;
			x = rotated;
			rotated = height * pitchCosine - depth * pitchSine >> 16;
			depth = height * pitchSine + depth * pitchCosine >> 16;
			height = rotated;
			if (depth < 50) {
				return;
			}
			if (tile.faceTexture != null) {
				Class40.cameraSpaceX[vertex] = x;
				Class40.cameraSpaceHeight[vertex] = height;
				Class40.cameraSpaceDepth[vertex] = depth;
			}
			Class40.projectedX[vertex] = Texture.textureInt1 + (x << 9) / depth;
			Class40.projectedY[vertex] = Texture.textureInt2 + (height << 9) / depth;
		}

		Texture.transparency = 0;
		count = tile.faceVertexA.length;
		for (int face = 0; face < count; face++) {
			int vertexA = tile.faceVertexA[face];
			int vertexB = tile.faceVertexB[face];
			int vertexC = tile.faceVertexC[face];
			int screenXA = Class40.projectedX[vertexA];
			int screenXB = Class40.projectedX[vertexB];
			int screenXC = Class40.projectedX[vertexC];
			int screenYA = Class40.projectedY[vertexA];
			int screenYB = Class40.projectedY[vertexB];
			int screenYC = Class40.projectedY[vertexC];
			if ((screenXA - screenXB) * (screenYC - screenYB) - (screenYA - screenYB) * (screenXC - screenXB) > 0) {
				Texture.clipScanlines = screenXA < 0 || screenXB < 0 || screenXC < 0 || screenXA > DrawingArea.scanlineRightLimit || screenXB > DrawingArea.scanlineRightLimit || screenXC > DrawingArea.scanlineRightLimit;
				if (tilePickRequested && isPointInTriangle(pickScreenX, pickScreenY, screenYA, screenYB, screenYC, screenXA, screenXB, screenXC)) {
					pickedTileX = tileX;
					pickedTileY = tileY;
				}
				if (tile.faceTexture == null || tile.faceTexture[face] == -1) {
					if (tile.faceColourA[face] != 0xbc614e) {
						Texture.drawShadedTriangle(screenYA, screenYB, screenYC, screenXA, screenXB, screenXC, tile.faceColourA[face], tile.faceColourB[face], tile.faceColourC[face]);
					}
				} else if (!lowMem) {
					if (tile.flat) {
						Texture.drawTexturedTriangle(screenYA, screenYB, screenYC, screenXA, screenXB, screenXC, tile.faceColourA[face], tile.faceColourB[face], tile.faceColourC[face], Class40.cameraSpaceX[0], Class40.cameraSpaceX[1], Class40.cameraSpaceX[3], Class40.cameraSpaceHeight[0], Class40.cameraSpaceHeight[1], Class40.cameraSpaceHeight[3], Class40.cameraSpaceDepth[0], Class40.cameraSpaceDepth[1], Class40.cameraSpaceDepth[3], tile.faceTexture[face]);
					} else {
						Texture.drawTexturedTriangle(screenYA, screenYB, screenYC, screenXA, screenXB, screenXC, tile.faceColourA[face], tile.faceColourB[face], tile.faceColourC[face], Class40.cameraSpaceX[vertexA], Class40.cameraSpaceX[vertexB], Class40.cameraSpaceX[vertexC], Class40.cameraSpaceHeight[vertexA], Class40.cameraSpaceHeight[vertexB], Class40.cameraSpaceHeight[vertexC], Class40.cameraSpaceDepth[vertexA], Class40.cameraSpaceDepth[vertexB], Class40.cameraSpaceDepth[vertexC], tile.faceTexture[face]);
					}
				} else {
					int textureHsl = lowMemTextureHsl[tile.faceTexture[face]];
					Texture.drawShadedTriangle(screenYA, screenYB, screenYC, screenXA, screenXB, screenXC, shadeTextureHsl(textureHsl, tile.faceColourA[face]), shadeTextureHsl(textureHsl, tile.faceColourB[face]), shadeTextureHsl(textureHsl, tile.faceColourC[face]));
				}
			}
		}

	}

	private int shadeTextureHsl(int textureHsl, int lightness) {
		lightness = 127 - lightness;
		lightness = lightness * (textureHsl & 0x7f) / 160;
		if (lightness < 2) {
			lightness = 2;
		} else if (lightness > 126) {
			lightness = 126;
		}
		return (textureHsl & 0xff80) + lightness;
	}

	private boolean isPointInTriangle(int pointX, int pointY, int y1, int y2, int y3, int x1, int x2, int x3) {
		if (pointY < y1 && pointY < y2 && pointY < y3) {
			return false;
		}
		if (pointY > y1 && pointY > y2 && pointY > y3) {
			return false;
		}
		if (pointX < x1 && pointX < x2 && pointX < x3) {
			return false;
		}
		if (pointX > x1 && pointX > x2 && pointX > x3) {
			return false;
		}
		int side12 = (pointY - y1) * (x2 - x1) - (pointX - x1) * (y2 - y1);
		int side31 = (pointY - y3) * (x1 - x3) - (pointX - x3) * (y1 - y3);
		int side23 = (pointY - y2) * (x3 - x2) - (pointX - x2) * (y3 - y2);
		return side12 * side23 > 0 && side23 * side31 > 0;
	}

	private void selectActiveOccluders() {
		int occluderCount = occluderCounts[cameraPlane];
		Class47 planeOccluders[] = occluders[cameraPlane];
		activeOccluderCount = 0;
		for (int index = 0; index < occluderCount; index++) {
			Class47 occluder = planeOccluders[index];
			if (occluder.type == 1) {
				int tableColumn = occluder.minTileX - cameraTileX + drawDistance;
				if (tableColumn < 0 || tableColumn > 50) {
					continue;
				}
				int firstTableRow = occluder.minTileY - cameraTileY + drawDistance;
				if (firstTableRow < 0) {
					firstTableRow = 0;
				}
				int lastTableRow = occluder.maxTileY - cameraTileY + drawDistance;
				if (lastTableRow > 50) {
					lastTableRow = 50;
				}
				boolean anyTileVisible = false;
				while (firstTableRow <= lastTableRow) {
					if (cameraTileVisibility[tableColumn][firstTableRow++]) {
						anyTileVisible = true;
						break;
					}
				}
				if (!anyTileVisible) {
					continue;
				}
				int distance = cameraWorldX - occluder.minX;
				if (distance > 32) {
					occluder.side = 1;
				} else {
					if (distance >= -32) {
						continue;
					}
					occluder.side = 2;
					distance = -distance;
				}
				occluder.minYSlope = (occluder.minY - cameraWorldY << 8) / distance;
				occluder.maxYSlope = (occluder.maxY - cameraWorldY << 8) / distance;
				occluder.minHeightSlope = (occluder.minHeight - cameraWorldHeight << 8) / distance;
				occluder.maxHeightSlope = (occluder.maxHeight - cameraWorldHeight << 8) / distance;
				activeOccluders[activeOccluderCount++] = occluder;
				continue;
			}
			if (occluder.type == 2) {
				int tableRow = occluder.minTileY - cameraTileY + drawDistance;
				if (tableRow < 0 || tableRow > 50) {
					continue;
				}
				int firstTableColumn = occluder.minTileX - cameraTileX + drawDistance;
				if (firstTableColumn < 0) {
					firstTableColumn = 0;
				}
				int lastTableColumn = occluder.maxTileX - cameraTileX + drawDistance;
				if (lastTableColumn > 50) {
					lastTableColumn = 50;
				}
				boolean anyTileVisible = false;
				while (firstTableColumn <= lastTableColumn) {
					if (cameraTileVisibility[firstTableColumn++][tableRow]) {
						anyTileVisible = true;
						break;
					}
				}
				if (!anyTileVisible) {
					continue;
				}
				int distance = cameraWorldY - occluder.minY;
				if (distance > 32) {
					occluder.side = 3;
				} else {
					if (distance >= -32) {
						continue;
					}
					occluder.side = 4;
					distance = -distance;
				}
				occluder.minXSlope = (occluder.minX - cameraWorldX << 8) / distance;
				occluder.maxXSlope = (occluder.maxX - cameraWorldX << 8) / distance;
				occluder.minHeightSlope = (occluder.minHeight - cameraWorldHeight << 8) / distance;
				occluder.maxHeightSlope = (occluder.maxHeight - cameraWorldHeight << 8) / distance;
				activeOccluders[activeOccluderCount++] = occluder;
			} else if (occluder.type == 4) {
				int heightDistance = occluder.minHeight - cameraWorldHeight;
				if (heightDistance > 128) {
					int firstTableRow = occluder.minTileY - cameraTileY + drawDistance;
					if (firstTableRow < 0) {
						firstTableRow = 0;
					}
					int lastTableRow = occluder.maxTileY - cameraTileY + drawDistance;
					if (lastTableRow > 50) {
						lastTableRow = 50;
					}
					if (firstTableRow <= lastTableRow) {
						int firstTableColumn = occluder.minTileX - cameraTileX + drawDistance;
						if (firstTableColumn < 0) {
							firstTableColumn = 0;
						}
						int lastTableColumn = occluder.maxTileX - cameraTileX + drawDistance;
						if (lastTableColumn > 50) {
							lastTableColumn = 50;
						}
						boolean anyTileVisible = false;
						label0 : for (int column = firstTableColumn; column <= lastTableColumn; column++) {
							for (int row = firstTableRow; row <= lastTableRow; row++) {
								if (!cameraTileVisibility[column][row]) {
									continue;
								}
								anyTileVisible = true;
								break label0;
							}

						}

						if (anyTileVisible) {
							occluder.side = 5;
							occluder.minXSlope = (occluder.minX - cameraWorldX << 8) / heightDistance;
							occluder.maxXSlope = (occluder.maxX - cameraWorldX << 8) / heightDistance;
							occluder.minYSlope = (occluder.minY - cameraWorldY << 8) / heightDistance;
							occluder.maxYSlope = (occluder.maxY - cameraWorldY << 8) / heightDistance;
							activeOccluders[activeOccluderCount++] = occluder;
						}
					}
				}
			}
		}

	}

	private boolean isTileOccluded(int plane, int tileX, int tileY) {
		int stamp = tileOcclusionCache[plane][tileX][tileY];
		if (stamp == -drawCycle) {
			return false;
		}
		if (stamp == drawCycle) {
			return true;
		}
		int worldX = tileX << 7;
		int worldY = tileY << 7;
		if (isPointOccluded(worldX + 1, tileHeights[plane][tileX][tileY], worldY + 1) && isPointOccluded(worldX + 128 - 1, tileHeights[plane][tileX + 1][tileY], worldY + 1) && isPointOccluded(worldX + 128 - 1, tileHeights[plane][tileX + 1][tileY + 1], worldY + 128 - 1) && isPointOccluded(worldX + 1, tileHeights[plane][tileX][tileY + 1], worldY + 128 - 1)) {
			tileOcclusionCache[plane][tileX][tileY] = drawCycle;
			return true;
		} else {
			tileOcclusionCache[plane][tileX][tileY] = -drawCycle;
			return false;
		}
	}

	private boolean isWallOccluded(int plane, int tileX, int tileY, int orientation) {
		if (!isTileOccluded(plane, tileX, tileY)) {
			return false;
		}
		int worldX = tileX << 7;
		int worldY = tileY << 7;
		int baseHeight = tileHeights[plane][tileX][tileY] - 1;
		int heightMinus120 = baseHeight - 120;
		int heightMinus230 = baseHeight - 230;
		int heightMinus238 = baseHeight - 238;
		if (orientation < 16) {
			if (orientation == 1) {
				if (worldX > cameraWorldX) {
					if (!isPointOccluded(worldX, baseHeight, worldY)) {
						return false;
					}
					if (!isPointOccluded(worldX, baseHeight, worldY + 128)) {
						return false;
					}
				}
				if (plane > 0) {
					if (!isPointOccluded(worldX, heightMinus120, worldY)) {
						return false;
					}
					if (!isPointOccluded(worldX, heightMinus120, worldY + 128)) {
						return false;
					}
				}
				return isPointOccluded(worldX, heightMinus230, worldY) && isPointOccluded(worldX, heightMinus230, worldY + 128);
			}
			if (orientation == 2) {
				if (worldY < cameraWorldY) {
					if (!isPointOccluded(worldX, baseHeight, worldY + 128)) {
						return false;
					}
					if (!isPointOccluded(worldX + 128, baseHeight, worldY + 128)) {
						return false;
					}
				}
				if (plane > 0) {
					if (!isPointOccluded(worldX, heightMinus120, worldY + 128)) {
						return false;
					}
					if (!isPointOccluded(worldX + 128, heightMinus120, worldY + 128)) {
						return false;
					}
				}
				return isPointOccluded(worldX, heightMinus230, worldY + 128) && isPointOccluded(worldX + 128, heightMinus230, worldY + 128);
			}
			if (orientation == 4) {
				if (worldX < cameraWorldX) {
					if (!isPointOccluded(worldX + 128, baseHeight, worldY)) {
						return false;
					}
					if (!isPointOccluded(worldX + 128, baseHeight, worldY + 128)) {
						return false;
					}
				}
				if (plane > 0) {
					if (!isPointOccluded(worldX + 128, heightMinus120, worldY)) {
						return false;
					}
					if (!isPointOccluded(worldX + 128, heightMinus120, worldY + 128)) {
						return false;
					}
				}
				return isPointOccluded(worldX + 128, heightMinus230, worldY) && isPointOccluded(worldX + 128, heightMinus230, worldY + 128);
			}
			if (orientation == 8) {
				if (worldY > cameraWorldY) {
					if (!isPointOccluded(worldX, baseHeight, worldY)) {
						return false;
					}
					if (!isPointOccluded(worldX + 128, baseHeight, worldY)) {
						return false;
					}
				}
				if (plane > 0) {
					if (!isPointOccluded(worldX, heightMinus120, worldY)) {
						return false;
					}
					if (!isPointOccluded(worldX + 128, heightMinus120, worldY)) {
						return false;
					}
				}
				return isPointOccluded(worldX, heightMinus230, worldY) && isPointOccluded(worldX + 128, heightMinus230, worldY);
			}
		}
		if (!isPointOccluded(worldX + 64, heightMinus238, worldY + 64)) {
			return false;
		}
		if (orientation == 16) {
			return isPointOccluded(worldX, heightMinus230, worldY + 128);
		}
		if (orientation == 32) {
			return isPointOccluded(worldX + 128, heightMinus230, worldY + 128);
		}
		if (orientation == 64) {
			return isPointOccluded(worldX + 128, heightMinus230, worldY);
		}
		if (orientation == 128) {
			return isPointOccluded(worldX, heightMinus230, worldY);
		} else {
			System.out.println("Warning unsupported wall type");
			return true;
		}
	}

	private boolean isTileColumnOccluded(int plane, int tileX, int tileY, int height) {
		if (!isTileOccluded(plane, tileX, tileY)) {
			return false;
		}
		int worldX = tileX << 7;
		int worldY = tileY << 7;
		return isPointOccluded(worldX + 1, tileHeights[plane][tileX][tileY] - height, worldY + 1) && isPointOccluded(worldX + 128 - 1, tileHeights[plane][tileX + 1][tileY] - height, worldY + 1) && isPointOccluded(worldX + 128 - 1, tileHeights[plane][tileX + 1][tileY + 1] - height, worldY + 128 - 1) && isPointOccluded(worldX + 1, tileHeights[plane][tileX][tileY + 1] - height, worldY + 128 - 1);
	}

	private boolean isFootprintOccluded(int plane, int minTileX, int maxTileX, int minTileY, int maxTileY, int height) {
		if (minTileX == maxTileX && minTileY == maxTileY) {
			if (!isTileOccluded(plane, minTileX, minTileY)) {
				return false;
			}
			int worldX = minTileX << 7;
			int worldY = minTileY << 7;
			return isPointOccluded(worldX + 1, tileHeights[plane][minTileX][minTileY] - height, worldY + 1) && isPointOccluded(worldX + 128 - 1, tileHeights[plane][minTileX + 1][minTileY] - height, worldY + 1) && isPointOccluded(worldX + 128 - 1, tileHeights[plane][minTileX + 1][minTileY + 1] - height, worldY + 128 - 1) && isPointOccluded(worldX + 1, tileHeights[plane][minTileX][minTileY + 1] - height, worldY + 128 - 1);
		}
		for (int tileX = minTileX; tileX <= maxTileX; tileX++) {
			for (int tileY = minTileY; tileY <= maxTileY; tileY++) {
				if (tileOcclusionCache[plane][tileX][tileY] == -drawCycle) {
					return false;
				}
			}

		}

		int minX = (minTileX << 7) + 1;
		int minY = (minTileY << 7) + 2;
		int cornerHeight = tileHeights[plane][minTileX][minTileY] - height;
		if (!isPointOccluded(minX, cornerHeight, minY)) {
			return false;
		}
		int maxX = (maxTileX << 7) - 1;
		if (!isPointOccluded(maxX, cornerHeight, minY)) {
			return false;
		}
		int maxY = (maxTileY << 7) - 1;
		return isPointOccluded(minX, cornerHeight, maxY) && isPointOccluded(maxX, cornerHeight, maxY);
	}

	private boolean isPointOccluded(int x, int height, int y) {
		for (int index = 0; index < activeOccluderCount; index++) {
			Class47 occluder = activeOccluders[index];
			if (occluder.side == 1) {
				int distance = occluder.minX - x;
				if (distance > 0) {
					int minY = occluder.minY + (occluder.minYSlope * distance >> 8);
					int maxY = occluder.maxY + (occluder.maxYSlope * distance >> 8);
					int minHeight = occluder.minHeight + (occluder.minHeightSlope * distance >> 8);
					int maxHeight = occluder.maxHeight + (occluder.maxHeightSlope * distance >> 8);
					if (y >= minY && y <= maxY && height >= minHeight && height <= maxHeight) {
						return true;
					}
				}
			} else if (occluder.side == 2) {
				int distance = x - occluder.minX;
				if (distance > 0) {
					int minY = occluder.minY + (occluder.minYSlope * distance >> 8);
					int maxY = occluder.maxY + (occluder.maxYSlope * distance >> 8);
					int minHeight = occluder.minHeight + (occluder.minHeightSlope * distance >> 8);
					int maxHeight = occluder.maxHeight + (occluder.maxHeightSlope * distance >> 8);
					if (y >= minY && y <= maxY && height >= minHeight && height <= maxHeight) {
						return true;
					}
				}
			} else if (occluder.side == 3) {
				int distance = occluder.minY - y;
				if (distance > 0) {
					int minX = occluder.minX + (occluder.minXSlope * distance >> 8);
					int maxX = occluder.maxX + (occluder.maxXSlope * distance >> 8);
					int minHeight = occluder.minHeight + (occluder.minHeightSlope * distance >> 8);
					int maxHeight = occluder.maxHeight + (occluder.maxHeightSlope * distance >> 8);
					if (x >= minX && x <= maxX && height >= minHeight && height <= maxHeight) {
						return true;
					}
				}
			} else if (occluder.side == 4) {
				int distance = y - occluder.minY;
				if (distance > 0) {
					int minX = occluder.minX + (occluder.minXSlope * distance >> 8);
					int maxX = occluder.maxX + (occluder.maxXSlope * distance >> 8);
					int minHeight = occluder.minHeight + (occluder.minHeightSlope * distance >> 8);
					int maxHeight = occluder.maxHeight + (occluder.maxHeightSlope * distance >> 8);
					if (x >= minX && x <= maxX && height >= minHeight && height <= maxHeight) {
						return true;
					}
				}
			} else if (occluder.side == 5) {
				int heightDistance = height - occluder.minHeight;
				if (heightDistance > 0) {
					int minX = occluder.minX + (occluder.minXSlope * heightDistance >> 8);
					int maxX = occluder.maxX + (occluder.maxXSlope * heightDistance >> 8);
					int minY = occluder.minY + (occluder.minYSlope * heightDistance >> 8);
					int maxY = occluder.maxY + (occluder.maxYSlope * heightDistance >> 8);
					if (x >= minX && x <= maxX && y >= minY && y <= maxY) {
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
	private final int tileCountX;
	private final int tileCountY;
	private final int[][][] tileHeights;
	private final Ground[][][] groundArray;
	private int lowestPlane;
	private int obj5CacheCurrPos;
	private final Object5[] obj5Cache;
	private final int[][][] tileOcclusionCache;
	private static int pendingTileCount;
	private static int cameraPlane;
	private static int drawCycle;
	private static int drawMinTileX;
	private static int drawMaxTileX;
	private static int drawMinTileY;
	private static int drawMaxTileY;
	private static int cameraTileX;
	private static int cameraTileY;
	private static int cameraWorldX;
	private static int cameraWorldHeight;
	private static int cameraWorldY;
	private static int pitchSine;
	private static int pitchCosine;
	private static int yawSine;
	private static int yawCosine;
	private static Object5[] aClass28Array462 = new Object5[100];
	private static final int[] anIntArray463 = {53, -53, -53, 53};
	private static final int[] anIntArray464 = {-53, -53, 53, 53};
	private static final int[] anIntArray465 = {-45, 45, 45, -45};
	private static final int[] anIntArray466 = {45, 45, -45, -45};
	private static boolean tilePickRequested;
	private static int pickScreenX;
	private static int pickScreenY;
	public static int pickedTileX = -1;
	public static int pickedTileY = -1;
	private static final int occluderPlaneCount;
	private static int[] occluderCounts;
	private static Class47[][] occluders;
	private static int activeOccluderCount;
	private static final Class47[] activeOccluders = new Class47[500];
	private static NodeList aClass19_477 = new NodeList();
	private static final int[] anIntArray478 = {19, 55, 38, 155, 255, 110, 137, 205, 76};
	private static final int[] anIntArray479 = {160, 192, 80, 96, 0, 144, 80, 48, 160};
	private static final int[] anIntArray480 = {76, 8, 137, 4, 0, 1, 38, 2, 19};
	private static final int[] anIntArray481 = {0, 0, 2, 0, 0, 2, 1, 1, 0};
	private static final int[] anIntArray482 = {2, 0, 0, 2, 0, 0, 0, 4, 4};
	private static final int[] anIntArray483 = {0, 4, 4, 8, 0, 0, 8, 0, 0};
	private static final int[] anIntArray484 = {1, 1, 0, 0, 0, 8, 0, 0, 8};
	private static final int[] lowMemTextureHsl = {41, 39248, 41, 4643, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 43086, 41, 41, 41, 41, 41, 41, 41, 8602, 41, 28992, 41, 41, 41, 41, 41, 5056, 41, 41, 41, 7079, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 3131, 41, 41, 41};
	private final int[] modelSharedVertexStamps;
	private final int[] otherSharedVertexStamps;
	private int mergeStamp;
	private final int[][] tileShapeMasks = {new int[16], {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, {1, 0, 0, 0, 1, 1, 0, 0, 1, 1, 1, 0, 1, 1, 1, 1}, {1, 1, 0, 0, 1, 1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0}, {0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 0, 1, 0, 0, 0, 1}, {0, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1}, {1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1}, {1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 1, 0, 0}, {0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 1, 0, 0}, {1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 0, 0, 1, 1}, {1, 1, 1, 1, 1, 1, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0}, {0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1}, {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 1, 1, 1, 1}};
	private final int[][] tileRotationOrders = {{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15}, {12, 8, 4, 0, 13, 9, 5, 1, 14, 10, 6, 2, 15, 11, 7, 3}, {15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0}, {3, 7, 11, 15, 2, 6, 10, 14, 1, 5, 9, 13, 0, 4, 8, 12}};
	private static boolean[][][][] tileVisibilityTable = new boolean[8][32][256][256];
	private static boolean[][] cameraTileVisibility;
	private static int viewportCentreX;
	private static int viewportCentreY;
	private static int viewportMinX;
	private static int viewportMinY;
	private static int viewportMaxX;
	private static int viewportMaxY;

	static {
		occluderPlaneCount = 4;
		occluderCounts = new int[occluderPlaneCount];
		occluders = new Class47[occluderPlaneCount][500];
	}
}
