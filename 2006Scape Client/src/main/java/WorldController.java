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
		anIntArrayArrayArray445 = new int[k][j + 1][i + 1];
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
		occluder.anInt787 = minX / 128;
		occluder.anInt788 = maxX / 128;
		occluder.anInt789 = minY / 128;
		occluder.anInt790 = maxY / 128;
		occluder.anInt791 = occluderType;
		occluder.anInt792 = minX;
		occluder.anInt793 = maxX;
		occluder.anInt794 = minY;
		occluder.anInt795 = maxY;
		occluder.anInt796 = minHeight;
		occluder.anInt797 = maxHeight;
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
			int tileColour = plainTile.anInt722;
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
		int shape = shapedTile.anInt684;
		int rotation = shapedTile.anInt685;
		int baseColour = shapedTile.anInt686;
		int maskedColour = shapedTile.anInt687;
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
				anInt458 = Model.sineTable16[pitch];
				anInt459 = Model.cosineTable16[pitch];
				anInt460 = Model.sineTable16[yaw];
				anInt461 = Model.cosineTable16[yaw];
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
		int rotatedX = z * anInt460 + x * anInt461 >> 16;
		int rotatedZ = z * anInt461 - x * anInt460 >> 16;
		int depth = height * anInt458 + rotatedZ * anInt459 >> 16;
		int verticalOffset = height * anInt459 - rotatedZ * anInt458 >> 16;
		if (depth < 50 || depth > 3500) {
			return false;
		}
		int screenX = viewportCentreX + (rotatedX << 9) / depth;
		int screenY = viewportCentreY + (verticalOffset << 9) / depth;
		return screenX >= viewportMinX && screenX <= viewportMaxX && screenY >= viewportMinY && screenY <= viewportMaxY;
	}

	public void requestTilePick(int screenY, int screenX) {
		aBoolean467 = true;
		anInt468 = screenX;
		anInt469 = screenY;
		anInt470 = -1;
		anInt471 = -1;
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
		anInt448++;
		anInt458 = Model.sineTable16[pitch];
		anInt459 = Model.cosineTable16[pitch];
		anInt460 = Model.sineTable16[yaw];
		anInt461 = Model.cosineTable16[yaw];
		cameraTileVisibility = tileVisibilityTable[(pitch - 128) / 32][yaw / 64];
		anInt455 = cameraX;
		anInt456 = cameraZ;
		anInt457 = cameraY;
		anInt453 = cameraX / 128;
		anInt454 = cameraY / 128;
		anInt447 = plane;
		anInt449 = anInt453 - drawDistance;
		if (anInt449 < 0) {
			anInt449 = 0;
		}
		anInt451 = anInt454 - drawDistance;
		if (anInt451 < 0) {
			anInt451 = 0;
		}
		anInt450 = anInt453 + drawDistance;
		if (anInt450 > tileCountX) {
			anInt450 = tileCountX;
		}
		anInt452 = anInt454 + drawDistance;
		if (anInt452 > tileCountY) {
			anInt452 = tileCountY;
		}
		method319();
		anInt446 = 0;
		for (int planeIndex = lowestPlane; planeIndex < planeCount; planeIndex++) {
			Ground planeTiles[][] = groundArray[planeIndex];
			for (int tileX = anInt449; tileX < anInt450; tileX++) {
				for (int tileY = anInt451; tileY < anInt452; tileY++) {
					Ground tile = planeTiles[tileX][tileY];
					if (tile != null) {
						if (tile.anInt1321 > plane || !cameraTileVisibility[tileX - anInt453 + drawDistance][tileY - anInt454 + drawDistance] && tileHeights[planeIndex][tileX][tileY] - cameraZ < 50) {
							tile.aBoolean1322 = false;
							tile.aBoolean1323 = false;
							tile.anInt1325 = 0;
						} else {
							tile.aBoolean1322 = true;
							tile.aBoolean1323 = true;
							tile.aBoolean1324 = tile.anInt1317 > 0;
							anInt446++;
						}
					}
				}

			}

		}

		for (int planeIndex = lowestPlane; planeIndex < planeCount; planeIndex++) {
			Ground planeTiles[][] = groundArray[planeIndex];
			for (int offsetX = -drawDistance; offsetX <= 0; offsetX++) {
				int lowX = anInt453 + offsetX;
				int highX = anInt453 - offsetX;
				if (lowX >= anInt449 || highX < anInt450) {
					for (int offsetY = -drawDistance; offsetY <= 0; offsetY++) {
						int lowY = anInt454 + offsetY;
						int highY = anInt454 - offsetY;
						if (lowX >= anInt449) {
							if (lowY >= anInt451) {
								Ground tile = planeTiles[lowX][lowY];
								if (tile != null && tile.aBoolean1322) {
									method314(tile, true);
								}
							}
							if (highY < anInt452) {
								Ground tile = planeTiles[lowX][highY];
								if (tile != null && tile.aBoolean1322) {
									method314(tile, true);
								}
							}
						}
						if (highX < anInt450) {
							if (lowY >= anInt451) {
								Ground tile = planeTiles[highX][lowY];
								if (tile != null && tile.aBoolean1322) {
									method314(tile, true);
								}
							}
							if (highY < anInt452) {
								Ground tile = planeTiles[highX][highY];
								if (tile != null && tile.aBoolean1322) {
									method314(tile, true);
								}
							}
						}
						if (anInt446 == 0) {
							aBoolean467 = false;
							return;
						}
					}

				}
			}

		}

		for (int planeIndex = lowestPlane; planeIndex < planeCount; planeIndex++) {
			Ground planeTiles[][] = groundArray[planeIndex];
			for (int offsetX = -drawDistance; offsetX <= 0; offsetX++) {
				int lowX = anInt453 + offsetX;
				int highX = anInt453 - offsetX;
				if (lowX >= anInt449 || highX < anInt450) {
					for (int offsetY = -drawDistance; offsetY <= 0; offsetY++) {
						int lowY = anInt454 + offsetY;
						int highY = anInt454 - offsetY;
						if (lowX >= anInt449) {
							if (lowY >= anInt451) {
								Ground tile = planeTiles[lowX][lowY];
								if (tile != null && tile.aBoolean1322) {
									method314(tile, false);
								}
							}
							if (highY < anInt452) {
								Ground tile = planeTiles[lowX][highY];
								if (tile != null && tile.aBoolean1322) {
									method314(tile, false);
								}
							}
						}
						if (highX < anInt450) {
							if (lowY >= anInt451) {
								Ground tile = planeTiles[highX][lowY];
								if (tile != null && tile.aBoolean1322) {
									method314(tile, false);
								}
							}
							if (highY < anInt452) {
								Ground tile = planeTiles[highX][highY];
								if (tile != null && tile.aBoolean1322) {
									method314(tile, false);
								}
							}
						}
						if (anInt446 == 0) {
							aBoolean467 = false;
							return;
						}
					}

				}
			}

		}

		aBoolean467 = false;
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
					if (i <= anInt453 && i > anInt449) {
						Ground class30_sub3_3 = aclass30_sub3[i - 1][j];
						if (class30_sub3_3 != null && class30_sub3_3.aBoolean1323 && (class30_sub3_3.aBoolean1322 || (class30_sub3_1.anInt1320 & 1) == 0)) {
							continue;
						}
					}
					if (i >= anInt453 && i < anInt450 - 1) {
						Ground class30_sub3_4 = aclass30_sub3[i + 1][j];
						if (class30_sub3_4 != null && class30_sub3_4.aBoolean1323 && (class30_sub3_4.aBoolean1322 || (class30_sub3_1.anInt1320 & 4) == 0)) {
							continue;
						}
					}
					if (j <= anInt454 && j > anInt451) {
						Ground class30_sub3_5 = aclass30_sub3[i][j - 1];
						if (class30_sub3_5 != null && class30_sub3_5.aBoolean1323 && (class30_sub3_5.aBoolean1322 || (class30_sub3_1.anInt1320 & 8) == 0)) {
							continue;
						}
					}
					if (j >= anInt454 && j < anInt452 - 1) {
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
						if (!method320(0, i, j)) {
							method315(class30_sub3_7.aClass43_1311, 0, anInt458, anInt459, anInt460, anInt461, i, j);
						}
					} else if (class30_sub3_7.aClass40_1312 != null && !method320(0, i, j)) {
						method316(i, anInt458, anInt460, class30_sub3_7.aClass40_1312, anInt459, j, anInt461);
					}
					Object1 class10 = class30_sub3_7.obj1;
					if (class10 != null) {
						class10.aClass30_Sub2_Sub4_278.method443(0, anInt458, anInt459, anInt460, anInt461, class10.anInt274 - anInt455, class10.anInt273 - anInt456, class10.anInt275 - anInt457, class10.uid);
					}
					for (int i2 = 0; i2 < class30_sub3_7.anInt1317; i2++) {
						Object5 class28 = class30_sub3_7.obj5Array[i2];
						if (class28 != null) {
							class28.aClass30_Sub2_Sub4_521.method443(class28.anInt522, anInt458, anInt459, anInt460, anInt461, class28.anInt519 - anInt455, class28.anInt518 - anInt456, class28.anInt520 - anInt457, class28.uid);
						}
					}

				}
				boolean flag1 = false;
				if (class30_sub3_1.aClass43_1311 != null) {
					if (!method320(l, i, j)) {
						flag1 = true;
						method315(class30_sub3_1.aClass43_1311, l, anInt458, anInt459, anInt460, anInt461, i, j);
					}
				} else if (class30_sub3_1.aClass40_1312 != null && !method320(l, i, j)) {
					flag1 = true;
					method316(i, anInt458, anInt460, class30_sub3_1.aClass40_1312, anInt459, j, anInt461);
				}
				int j1 = 0;
				int j2 = 0;
				Object1 class10_3 = class30_sub3_1.obj1;
				Object2 class26_1 = class30_sub3_1.obj2;
				if (class10_3 != null || class26_1 != null) {
					if (anInt453 == i) {
						j1++;
					} else if (anInt453 < i) {
						j1 += 2;
					}
					if (anInt454 == j) {
						j1 += 3;
					} else if (anInt454 > j) {
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
					if ((class10_3.orientation & j2) != 0 && !method321(l, i, j, class10_3.orientation)) {
						class10_3.aClass30_Sub2_Sub4_278.method443(0, anInt458, anInt459, anInt460, anInt461, class10_3.anInt274 - anInt455, class10_3.anInt273 - anInt456, class10_3.anInt275 - anInt457, class10_3.uid);
					}
					if ((class10_3.orientation1 & j2) != 0 && !method321(l, i, j, class10_3.orientation1)) {
						class10_3.aClass30_Sub2_Sub4_279.method443(0, anInt458, anInt459, anInt460, anInt461, class10_3.anInt274 - anInt455, class10_3.anInt273 - anInt456, class10_3.anInt275 - anInt457, class10_3.uid);
					}
				}
				if (class26_1 != null && !method322(l, i, j, class26_1.aClass30_Sub2_Sub4_504.modelHeight)) {
					if ((class26_1.anInt502 & j2) != 0) {
						class26_1.aClass30_Sub2_Sub4_504.method443(class26_1.anInt503, anInt458, anInt459, anInt460, anInt461, class26_1.anInt500 - anInt455, class26_1.anInt499 - anInt456, class26_1.anInt501 - anInt457, class26_1.uid);
					} else if ((class26_1.anInt502 & 0x300) != 0) {
						int j4 = class26_1.anInt500 - anInt455;
						int l5 = class26_1.anInt499 - anInt456;
						int k6 = class26_1.anInt501 - anInt457;
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
							class26_1.aClass30_Sub2_Sub4_504.method443(i8 * 512 + 256, anInt458, anInt459, anInt460, anInt461, i11, l5, k11, class26_1.uid);
						}
						if ((class26_1.anInt502 & 0x200) != 0 && k10 > k9) {
							int j11 = j4 + anIntArray465[i8];
							int l11 = k6 + anIntArray466[i8];
							class26_1.aClass30_Sub2_Sub4_504.method443(i8 * 512 + 1280 & 0x7ff, anInt458, anInt459, anInt460, anInt461, j11, l5, l11, class26_1.uid);
						}
					}
				}
				if (flag1) {
					Object3 class49 = class30_sub3_1.obj3;
					if (class49 != null) {
						class49.aClass30_Sub2_Sub4_814.method443(0, anInt458, anInt459, anInt460, anInt461, class49.anInt812 - anInt455, class49.anInt811 - anInt456, class49.anInt813 - anInt457, class49.uid);
					}
					Object4 object4_1 = class30_sub3_1.obj4;
					if (object4_1 != null && object4_1.anInt52 == 0) {
						if (object4_1.aClass30_Sub2_Sub4_49 != null) {
							object4_1.aClass30_Sub2_Sub4_49.method443(0, anInt458, anInt459, anInt460, anInt461, object4_1.anInt46 - anInt455, object4_1.anInt45 - anInt456, object4_1.anInt47 - anInt457, object4_1.uid);
						}
						if (object4_1.aClass30_Sub2_Sub4_50 != null) {
							object4_1.aClass30_Sub2_Sub4_50.method443(0, anInt458, anInt459, anInt460, anInt461, object4_1.anInt46 - anInt455, object4_1.anInt45 - anInt456, object4_1.anInt47 - anInt457, object4_1.uid);
						}
						if (object4_1.aClass30_Sub2_Sub4_48 != null) {
							object4_1.aClass30_Sub2_Sub4_48.method443(0, anInt458, anInt459, anInt460, anInt461, object4_1.anInt46 - anInt455, object4_1.anInt45 - anInt456, object4_1.anInt47 - anInt457, object4_1.uid);
						}
					}
				}
				int k4 = class30_sub3_1.anInt1320;
				if (k4 != 0) {
					if (i < anInt453 && (k4 & 4) != 0) {
						Ground class30_sub3_17 = aclass30_sub3[i + 1][j];
						if (class30_sub3_17 != null && class30_sub3_17.aBoolean1323) {
							aClass19_477.insertHead(class30_sub3_17);
						}
					}
					if (j < anInt454 && (k4 & 2) != 0) {
						Ground class30_sub3_18 = aclass30_sub3[i][j + 1];
						if (class30_sub3_18 != null && class30_sub3_18.aBoolean1323) {
							aClass19_477.insertHead(class30_sub3_18);
						}
					}
					if (i > anInt453 && (k4 & 1) != 0) {
						Ground class30_sub3_19 = aclass30_sub3[i - 1][j];
						if (class30_sub3_19 != null && class30_sub3_19.aBoolean1323) {
							aClass19_477.insertHead(class30_sub3_19);
						}
					}
					if (j > anInt454 && (k4 & 8) != 0) {
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
					if (class30_sub3_1.obj5Array[k1].anInt528 == anInt448 || (class30_sub3_1.anIntArray1319[k1] & class30_sub3_1.anInt1325) != class30_sub3_1.anInt1326) {
						continue;
					}
					flag2 = false;
					break;
				}

				if (flag2) {
					Object1 class10_1 = class30_sub3_1.obj1;
					if (!method321(l, i, j, class10_1.orientation)) {
						class10_1.aClass30_Sub2_Sub4_278.method443(0, anInt458, anInt459, anInt460, anInt461, class10_1.anInt274 - anInt455, class10_1.anInt273 - anInt456, class10_1.anInt275 - anInt457, class10_1.uid);
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
						if (class28_1.anInt528 == anInt448) {
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
						int i5 = anInt453 - class28_1.anInt523;
						int i6 = class28_1.anInt524 - anInt453;
						if (i6 > i5) {
							i5 = i6;
						}
						int i7 = anInt454 - class28_1.anInt525;
						int j8 = class28_1.anInt526 - anInt454;
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
							if (class28_2.anInt528 != anInt448) {
								if (class28_2.anInt527 > i3) {
									i3 = class28_2.anInt527;
									l3 = j5;
								} else if (class28_2.anInt527 == i3) {
									int j7 = class28_2.anInt519 - anInt455;
									int k8 = class28_2.anInt520 - anInt457;
									int l9 = aClass28Array462[l3].anInt519 - anInt455;
									int l10 = aClass28Array462[l3].anInt520 - anInt457;
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
						class28_3.anInt528 = anInt448;
						if (!method323(l, class28_3.anInt523, class28_3.anInt524, class28_3.anInt525, class28_3.anInt526, class28_3.aClass30_Sub2_Sub4_521.modelHeight)) {
							class28_3.aClass30_Sub2_Sub4_521.method443(class28_3.anInt522, anInt458, anInt459, anInt460, anInt461, class28_3.anInt519 - anInt455, class28_3.anInt518 - anInt456, class28_3.anInt520 - anInt457, class28_3.uid);
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
			if (i <= anInt453 && i > anInt449) {
				Ground class30_sub3_8 = aclass30_sub3[i - 1][j];
				if (class30_sub3_8 != null && class30_sub3_8.aBoolean1323) {
					continue;
				}
			}
			if (i >= anInt453 && i < anInt450 - 1) {
				Ground class30_sub3_9 = aclass30_sub3[i + 1][j];
				if (class30_sub3_9 != null && class30_sub3_9.aBoolean1323) {
					continue;
				}
			}
			if (j <= anInt454 && j > anInt451) {
				Ground class30_sub3_10 = aclass30_sub3[i][j - 1];
				if (class30_sub3_10 != null && class30_sub3_10.aBoolean1323) {
					continue;
				}
			}
			if (j >= anInt454 && j < anInt452 - 1) {
				Ground class30_sub3_11 = aclass30_sub3[i][j + 1];
				if (class30_sub3_11 != null && class30_sub3_11.aBoolean1323) {
					continue;
				}
			}
			class30_sub3_1.aBoolean1323 = false;
			anInt446--;
			Object4 object4 = class30_sub3_1.obj4;
			if (object4 != null && object4.anInt52 != 0) {
				if (object4.aClass30_Sub2_Sub4_49 != null) {
					object4.aClass30_Sub2_Sub4_49.method443(0, anInt458, anInt459, anInt460, anInt461, object4.anInt46 - anInt455, object4.anInt45 - anInt456 - object4.anInt52, object4.anInt47 - anInt457, object4.uid);
				}
				if (object4.aClass30_Sub2_Sub4_50 != null) {
					object4.aClass30_Sub2_Sub4_50.method443(0, anInt458, anInt459, anInt460, anInt461, object4.anInt46 - anInt455, object4.anInt45 - anInt456 - object4.anInt52, object4.anInt47 - anInt457, object4.uid);
				}
				if (object4.aClass30_Sub2_Sub4_48 != null) {
					object4.aClass30_Sub2_Sub4_48.method443(0, anInt458, anInt459, anInt460, anInt461, object4.anInt46 - anInt455, object4.anInt45 - anInt456 - object4.anInt52, object4.anInt47 - anInt457, object4.uid);
				}
			}
			if (class30_sub3_1.anInt1328 != 0) {
				Object2 class26 = class30_sub3_1.obj2;
				if (class26 != null && !method322(l, i, j, class26.aClass30_Sub2_Sub4_504.modelHeight)) {
					if ((class26.anInt502 & class30_sub3_1.anInt1328) != 0) {
						class26.aClass30_Sub2_Sub4_504.method443(class26.anInt503, anInt458, anInt459, anInt460, anInt461, class26.anInt500 - anInt455, class26.anInt499 - anInt456, class26.anInt501 - anInt457, class26.uid);
					} else if ((class26.anInt502 & 0x300) != 0) {
						int l2 = class26.anInt500 - anInt455;
						int j3 = class26.anInt499 - anInt456;
						int i4 = class26.anInt501 - anInt457;
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
							class26.aClass30_Sub2_Sub4_504.method443(k5 * 512 + 256, anInt458, anInt459, anInt460, anInt461, i9, j3, i10, class26.uid);
						}
						if ((class26.anInt502 & 0x200) != 0 && l7 <= j6) {
							int j9 = l2 + anIntArray465[k5];
							int j10 = i4 + anIntArray466[k5];
							class26.aClass30_Sub2_Sub4_504.method443(k5 * 512 + 1280 & 0x7ff, anInt458, anInt459, anInt460, anInt461, j9, j3, j10, class26.uid);
						}
					}
				}
				Object1 class10_2 = class30_sub3_1.obj1;
				if (class10_2 != null) {
					if ((class10_2.orientation1 & class30_sub3_1.anInt1328) != 0 && !method321(l, i, j, class10_2.orientation1)) {
						class10_2.aClass30_Sub2_Sub4_279.method443(0, anInt458, anInt459, anInt460, anInt461, class10_2.anInt274 - anInt455, class10_2.anInt273 - anInt456, class10_2.anInt275 - anInt457, class10_2.uid);
					}
					if ((class10_2.orientation & class30_sub3_1.anInt1328) != 0 && !method321(l, i, j, class10_2.orientation)) {
						class10_2.aClass30_Sub2_Sub4_278.method443(0, anInt458, anInt459, anInt460, anInt461, class10_2.anInt274 - anInt455, class10_2.anInt273 - anInt456, class10_2.anInt275 - anInt457, class10_2.uid);
					}
				}
			}
			if (k < planeCount - 1) {
				Ground class30_sub3_12 = groundArray[k + 1][i][j];
				if (class30_sub3_12 != null && class30_sub3_12.aBoolean1323) {
					aClass19_477.insertHead(class30_sub3_12);
				}
			}
			if (i < anInt453) {
				Ground class30_sub3_13 = aclass30_sub3[i + 1][j];
				if (class30_sub3_13 != null && class30_sub3_13.aBoolean1323) {
					aClass19_477.insertHead(class30_sub3_13);
				}
			}
			if (j < anInt454) {
				Ground class30_sub3_14 = aclass30_sub3[i][j + 1];
				if (class30_sub3_14 != null && class30_sub3_14.aBoolean1323) {
					aClass19_477.insertHead(class30_sub3_14);
				}
			}
			if (i > anInt453) {
				Ground class30_sub3_15 = aclass30_sub3[i - 1][j];
				if (class30_sub3_15 != null && class30_sub3_15.aBoolean1323) {
					aClass19_477.insertHead(class30_sub3_15);
				}
			}
			if (j > anInt454) {
				Ground class30_sub3_16 = aclass30_sub3[i][j - 1];
				if (class30_sub3_16 != null && class30_sub3_16.aBoolean1323) {
					aClass19_477.insertHead(class30_sub3_16);
				}
			}
		} while (true);
	}

	private void method315(Class43 class43, int i, int j, int k, int l, int i1, int j1, int k1) {
		int l1;
		int i2 = l1 = (j1 << 7) - anInt455;
		int j2;
		int k2 = j2 = (k1 << 7) - anInt457;
		int l2;
		int i3 = l2 = i2 + 128;
		int j3;
		int k3 = j3 = k2 + 128;
		int l3 = tileHeights[i][j1][k1] - anInt456;
		int i4 = tileHeights[i][j1 + 1][k1] - anInt456;
		int j4 = tileHeights[i][j1 + 1][k1 + 1] - anInt456;
		int k4 = tileHeights[i][j1][k1 + 1] - anInt456;
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
		Texture.transparency = 0;
		if ((i6 - k6) * (l5 - l6) - (j6 - l6) * (k5 - k6) > 0) {
			Texture.clipScanlines = i6 < 0 || k6 < 0 || k5 < 0 || i6 > DrawingArea.scanlineRightLimit || k6 > DrawingArea.scanlineRightLimit || k5 > DrawingArea.scanlineRightLimit;
			if (aBoolean467 && method318(anInt468, anInt469, j6, l6, l5, i6, k6, k5)) {
				anInt470 = j1;
				anInt471 = k1;
			}
			if (class43.anInt720 == -1) {
				if (class43.anInt718 != 0xbc614e) {
					Texture.drawShadedTriangle(j6, l6, l5, i6, k6, k5, class43.anInt718, class43.anInt719, class43.anInt717);
				}
			} else if (!lowMem) {
				if (class43.aBoolean721) {
					Texture.drawTexturedTriangle(j6, l6, l5, i6, k6, k5, class43.anInt718, class43.anInt719, class43.anInt717, i2, i3, l1, l3, i4, k4, k2, j2, j3, class43.anInt720);
				} else {
					Texture.drawTexturedTriangle(j6, l6, l5, i6, k6, k5, class43.anInt718, class43.anInt719, class43.anInt717, l2, l1, i3, j4, k4, i4, k3, j3, j2, class43.anInt720);
				}
			} else {
				int i7 = anIntArray485[class43.anInt720];
				Texture.drawShadedTriangle(j6, l6, l5, i6, k6, k5, method317(i7, class43.anInt718), method317(i7, class43.anInt719), method317(i7, class43.anInt717));
			}
		}
		if ((i5 - k5) * (l6 - l5) - (j5 - l5) * (k6 - k5) > 0) {
			Texture.clipScanlines = i5 < 0 || k5 < 0 || k6 < 0 || i5 > DrawingArea.scanlineRightLimit || k5 > DrawingArea.scanlineRightLimit || k6 > DrawingArea.scanlineRightLimit;
			if (aBoolean467 && method318(anInt468, anInt469, j5, l5, l6, i5, k5, k6)) {
				anInt470 = j1;
				anInt471 = k1;
			}
			if (class43.anInt720 == -1) {
				if (class43.anInt716 != 0xbc614e) {
					Texture.drawShadedTriangle(j5, l5, l6, i5, k5, k6, class43.anInt716, class43.anInt717, class43.anInt719);
				}
			} else {
				if (!lowMem) {
					Texture.drawTexturedTriangle(j5, l5, l6, i5, k5, k6, class43.anInt716, class43.anInt717, class43.anInt719, i2, i3, l1, l3, i4, k4, k2, j2, j3, class43.anInt720);
					return;
				}
				int j7 = anIntArray485[class43.anInt720];
				Texture.drawShadedTriangle(j5, l5, l6, i5, k5, k6, method317(j7, class43.anInt716), method317(j7, class43.anInt717), method317(j7, class43.anInt719));
			}
		}
	}

	private void method316(int i, int j, int k, Class40 class40, int l, int i1, int j1) {
		int k1 = class40.anIntArray673.length;
		for (int l1 = 0; l1 < k1; l1++) {
			int i2 = class40.anIntArray673[l1] - anInt455;
			int k2 = class40.anIntArray674[l1] - anInt456;
			int i3 = class40.anIntArray675[l1] - anInt457;
			int k3 = i3 * k + i2 * j1 >> 16;
			i3 = i3 * j1 - i2 * k >> 16;
			i2 = k3;
			k3 = k2 * l - i3 * j >> 16;
			i3 = k2 * j + i3 * l >> 16;
			k2 = k3;
			if (i3 < 50) {
				return;
			}
			if (class40.anIntArray682 != null) {
				Class40.anIntArray690[l1] = i2;
				Class40.anIntArray691[l1] = k2;
				Class40.anIntArray692[l1] = i3;
			}
			Class40.anIntArray688[l1] = Texture.textureInt1 + (i2 << 9) / i3;
			Class40.anIntArray689[l1] = Texture.textureInt2 + (k2 << 9) / i3;
		}

		Texture.transparency = 0;
		k1 = class40.anIntArray679.length;
		for (int j2 = 0; j2 < k1; j2++) {
			int l2 = class40.anIntArray679[j2];
			int j3 = class40.anIntArray680[j2];
			int l3 = class40.anIntArray681[j2];
			int i4 = Class40.anIntArray688[l2];
			int j4 = Class40.anIntArray688[j3];
			int k4 = Class40.anIntArray688[l3];
			int l4 = Class40.anIntArray689[l2];
			int i5 = Class40.anIntArray689[j3];
			int j5 = Class40.anIntArray689[l3];
			if ((i4 - j4) * (j5 - i5) - (l4 - i5) * (k4 - j4) > 0) {
				Texture.clipScanlines = i4 < 0 || j4 < 0 || k4 < 0 || i4 > DrawingArea.scanlineRightLimit || j4 > DrawingArea.scanlineRightLimit || k4 > DrawingArea.scanlineRightLimit;
				if (aBoolean467 && method318(anInt468, anInt469, l4, i5, j5, i4, j4, k4)) {
					anInt470 = i;
					anInt471 = i1;
				}
				if (class40.anIntArray682 == null || class40.anIntArray682[j2] == -1) {
					if (class40.anIntArray676[j2] != 0xbc614e) {
						Texture.drawShadedTriangle(l4, i5, j5, i4, j4, k4, class40.anIntArray676[j2], class40.anIntArray677[j2], class40.anIntArray678[j2]);
					}
				} else if (!lowMem) {
					if (class40.aBoolean683) {
						Texture.drawTexturedTriangle(l4, i5, j5, i4, j4, k4, class40.anIntArray676[j2], class40.anIntArray677[j2], class40.anIntArray678[j2], Class40.anIntArray690[0], Class40.anIntArray690[1], Class40.anIntArray690[3], Class40.anIntArray691[0], Class40.anIntArray691[1], Class40.anIntArray691[3], Class40.anIntArray692[0], Class40.anIntArray692[1], Class40.anIntArray692[3], class40.anIntArray682[j2]);
					} else {
						Texture.drawTexturedTriangle(l4, i5, j5, i4, j4, k4, class40.anIntArray676[j2], class40.anIntArray677[j2], class40.anIntArray678[j2], Class40.anIntArray690[l2], Class40.anIntArray690[j3], Class40.anIntArray690[l3], Class40.anIntArray691[l2], Class40.anIntArray691[j3], Class40.anIntArray691[l3], Class40.anIntArray692[l2], Class40.anIntArray692[j3], Class40.anIntArray692[l3], class40.anIntArray682[j2]);
					}
				} else {
					int k5 = anIntArray485[class40.anIntArray682[j2]];
					Texture.drawShadedTriangle(l4, i5, j5, i4, j4, k4, method317(k5, class40.anIntArray676[j2]), method317(k5, class40.anIntArray677[j2]), method317(k5, class40.anIntArray678[j2]));
				}
			}
		}

	}

	private int method317(int j, int k) {
		k = 127 - k;
		k = k * (j & 0x7f) / 160;
		if (k < 2) {
			k = 2;
		} else if (k > 126) {
			k = 126;
		}
		return (j & 0xff80) + k;
	}

	private boolean method318(int i, int j, int k, int l, int i1, int j1, int k1, int l1) {
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

	private void method319() {
		int j = occluderCounts[anInt447];
		Class47 aclass47[] = occluders[anInt447];
		anInt475 = 0;
		for (int k = 0; k < j; k++) {
			Class47 class47 = aclass47[k];
			if (class47.anInt791 == 1) {
				int l = class47.anInt787 - anInt453 + drawDistance;
				if (l < 0 || l > 50) {
					continue;
				}
				int k1 = class47.anInt789 - anInt454 + drawDistance;
				if (k1 < 0) {
					k1 = 0;
				}
				int j2 = class47.anInt790 - anInt454 + drawDistance;
				if (j2 > 50) {
					j2 = 50;
				}
				boolean flag = false;
				while (k1 <= j2) {
					if (cameraTileVisibility[l][k1++]) {
						flag = true;
						break;
					}
				}
				if (!flag) {
					continue;
				}
				int j3 = anInt455 - class47.anInt792;
				if (j3 > 32) {
					class47.anInt798 = 1;
				} else {
					if (j3 >= -32) {
						continue;
					}
					class47.anInt798 = 2;
					j3 = -j3;
				}
				class47.anInt801 = (class47.anInt794 - anInt457 << 8) / j3;
				class47.anInt802 = (class47.anInt795 - anInt457 << 8) / j3;
				class47.anInt803 = (class47.anInt796 - anInt456 << 8) / j3;
				class47.anInt804 = (class47.anInt797 - anInt456 << 8) / j3;
				aClass47Array476[anInt475++] = class47;
				continue;
			}
			if (class47.anInt791 == 2) {
				int i1 = class47.anInt789 - anInt454 + drawDistance;
				if (i1 < 0 || i1 > 50) {
					continue;
				}
				int l1 = class47.anInt787 - anInt453 + drawDistance;
				if (l1 < 0) {
					l1 = 0;
				}
				int k2 = class47.anInt788 - anInt453 + drawDistance;
				if (k2 > 50) {
					k2 = 50;
				}
				boolean flag1 = false;
				while (l1 <= k2) {
					if (cameraTileVisibility[l1++][i1]) {
						flag1 = true;
						break;
					}
				}
				if (!flag1) {
					continue;
				}
				int k3 = anInt457 - class47.anInt794;
				if (k3 > 32) {
					class47.anInt798 = 3;
				} else {
					if (k3 >= -32) {
						continue;
					}
					class47.anInt798 = 4;
					k3 = -k3;
				}
				class47.anInt799 = (class47.anInt792 - anInt455 << 8) / k3;
				class47.anInt800 = (class47.anInt793 - anInt455 << 8) / k3;
				class47.anInt803 = (class47.anInt796 - anInt456 << 8) / k3;
				class47.anInt804 = (class47.anInt797 - anInt456 << 8) / k3;
				aClass47Array476[anInt475++] = class47;
			} else if (class47.anInt791 == 4) {
				int j1 = class47.anInt796 - anInt456;
				if (j1 > 128) {
					int i2 = class47.anInt789 - anInt454 + drawDistance;
					if (i2 < 0) {
						i2 = 0;
					}
					int l2 = class47.anInt790 - anInt454 + drawDistance;
					if (l2 > 50) {
						l2 = 50;
					}
					if (i2 <= l2) {
						int i3 = class47.anInt787 - anInt453 + drawDistance;
						if (i3 < 0) {
							i3 = 0;
						}
						int l3 = class47.anInt788 - anInt453 + drawDistance;
						if (l3 > 50) {
							l3 = 50;
						}
						boolean flag2 = false;
						label0 : for (int i4 = i3; i4 <= l3; i4++) {
							for (int j4 = i2; j4 <= l2; j4++) {
								if (!cameraTileVisibility[i4][j4]) {
									continue;
								}
								flag2 = true;
								break label0;
							}

						}

						if (flag2) {
							class47.anInt798 = 5;
							class47.anInt799 = (class47.anInt792 - anInt455 << 8) / j1;
							class47.anInt800 = (class47.anInt793 - anInt455 << 8) / j1;
							class47.anInt801 = (class47.anInt794 - anInt457 << 8) / j1;
							class47.anInt802 = (class47.anInt795 - anInt457 << 8) / j1;
							aClass47Array476[anInt475++] = class47;
						}
					}
				}
			}
		}

	}

	private boolean method320(int i, int j, int k) {
		int l = anIntArrayArrayArray445[i][j][k];
		if (l == -anInt448) {
			return false;
		}
		if (l == anInt448) {
			return true;
		}
		int i1 = j << 7;
		int j1 = k << 7;
		if (method324(i1 + 1, tileHeights[i][j][k], j1 + 1) && method324(i1 + 128 - 1, tileHeights[i][j + 1][k], j1 + 1) && method324(i1 + 128 - 1, tileHeights[i][j + 1][k + 1], j1 + 128 - 1) && method324(i1 + 1, tileHeights[i][j][k + 1], j1 + 128 - 1)) {
			anIntArrayArrayArray445[i][j][k] = anInt448;
			return true;
		} else {
			anIntArrayArrayArray445[i][j][k] = -anInt448;
			return false;
		}
	}

	private boolean method321(int i, int j, int k, int l) {
		if (!method320(i, j, k)) {
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
				if (i1 > anInt455) {
					if (!method324(i1, k1, j1)) {
						return false;
					}
					if (!method324(i1, k1, j1 + 128)) {
						return false;
					}
				}
				if (i > 0) {
					if (!method324(i1, l1, j1)) {
						return false;
					}
					if (!method324(i1, l1, j1 + 128)) {
						return false;
					}
				}
				return method324(i1, i2, j1) && method324(i1, i2, j1 + 128);
			}
			if (l == 2) {
				if (j1 < anInt457) {
					if (!method324(i1, k1, j1 + 128)) {
						return false;
					}
					if (!method324(i1 + 128, k1, j1 + 128)) {
						return false;
					}
				}
				if (i > 0) {
					if (!method324(i1, l1, j1 + 128)) {
						return false;
					}
					if (!method324(i1 + 128, l1, j1 + 128)) {
						return false;
					}
				}
				return method324(i1, i2, j1 + 128) && method324(i1 + 128, i2, j1 + 128);
			}
			if (l == 4) {
				if (i1 < anInt455) {
					if (!method324(i1 + 128, k1, j1)) {
						return false;
					}
					if (!method324(i1 + 128, k1, j1 + 128)) {
						return false;
					}
				}
				if (i > 0) {
					if (!method324(i1 + 128, l1, j1)) {
						return false;
					}
					if (!method324(i1 + 128, l1, j1 + 128)) {
						return false;
					}
				}
				return method324(i1 + 128, i2, j1) && method324(i1 + 128, i2, j1 + 128);
			}
			if (l == 8) {
				if (j1 > anInt457) {
					if (!method324(i1, k1, j1)) {
						return false;
					}
					if (!method324(i1 + 128, k1, j1)) {
						return false;
					}
				}
				if (i > 0) {
					if (!method324(i1, l1, j1)) {
						return false;
					}
					if (!method324(i1 + 128, l1, j1)) {
						return false;
					}
				}
				return method324(i1, i2, j1) && method324(i1 + 128, i2, j1);
			}
		}
		if (!method324(i1 + 64, j2, j1 + 64)) {
			return false;
		}
		if (l == 16) {
			return method324(i1, i2, j1 + 128);
		}
		if (l == 32) {
			return method324(i1 + 128, i2, j1 + 128);
		}
		if (l == 64) {
			return method324(i1 + 128, i2, j1);
		}
		if (l == 128) {
			return method324(i1, i2, j1);
		} else {
			System.out.println("Warning unsupported wall type");
			return true;
		}
	}

	private boolean method322(int i, int j, int k, int l) {
		if (!method320(i, j, k)) {
			return false;
		}
		int i1 = j << 7;
		int j1 = k << 7;
		return method324(i1 + 1, tileHeights[i][j][k] - l, j1 + 1) && method324(i1 + 128 - 1, tileHeights[i][j + 1][k] - l, j1 + 1) && method324(i1 + 128 - 1, tileHeights[i][j + 1][k + 1] - l, j1 + 128 - 1) && method324(i1 + 1, tileHeights[i][j][k + 1] - l, j1 + 128 - 1);
	}

	private boolean method323(int i, int j, int k, int l, int i1, int j1) {
		if (j == k && l == i1) {
			if (!method320(i, j, l)) {
				return false;
			}
			int k1 = j << 7;
			int i2 = l << 7;
			return method324(k1 + 1, tileHeights[i][j][l] - j1, i2 + 1) && method324(k1 + 128 - 1, tileHeights[i][j + 1][l] - j1, i2 + 1) && method324(k1 + 128 - 1, tileHeights[i][j + 1][l + 1] - j1, i2 + 128 - 1) && method324(k1 + 1, tileHeights[i][j][l + 1] - j1, i2 + 128 - 1);
		}
		for (int l1 = j; l1 <= k; l1++) {
			for (int j2 = l; j2 <= i1; j2++) {
				if (anIntArrayArrayArray445[i][l1][j2] == -anInt448) {
					return false;
				}
			}

		}

		int k2 = (j << 7) + 1;
		int l2 = (l << 7) + 2;
		int i3 = tileHeights[i][j][l] - j1;
		if (!method324(k2, i3, l2)) {
			return false;
		}
		int j3 = (k << 7) - 1;
		if (!method324(j3, i3, l2)) {
			return false;
		}
		int k3 = (i1 << 7) - 1;
		return method324(k2, i3, k3) && method324(j3, i3, k3);
	}

	private boolean method324(int i, int j, int k) {
		for (int l = 0; l < anInt475; l++) {
			Class47 class47 = aClass47Array476[l];
			if (class47.anInt798 == 1) {
				int i1 = class47.anInt792 - i;
				if (i1 > 0) {
					int j2 = class47.anInt794 + (class47.anInt801 * i1 >> 8);
					int k3 = class47.anInt795 + (class47.anInt802 * i1 >> 8);
					int l4 = class47.anInt796 + (class47.anInt803 * i1 >> 8);
					int i6 = class47.anInt797 + (class47.anInt804 * i1 >> 8);
					if (k >= j2 && k <= k3 && j >= l4 && j <= i6) {
						return true;
					}
				}
			} else if (class47.anInt798 == 2) {
				int j1 = i - class47.anInt792;
				if (j1 > 0) {
					int k2 = class47.anInt794 + (class47.anInt801 * j1 >> 8);
					int l3 = class47.anInt795 + (class47.anInt802 * j1 >> 8);
					int i5 = class47.anInt796 + (class47.anInt803 * j1 >> 8);
					int j6 = class47.anInt797 + (class47.anInt804 * j1 >> 8);
					if (k >= k2 && k <= l3 && j >= i5 && j <= j6) {
						return true;
					}
				}
			} else if (class47.anInt798 == 3) {
				int k1 = class47.anInt794 - k;
				if (k1 > 0) {
					int l2 = class47.anInt792 + (class47.anInt799 * k1 >> 8);
					int i4 = class47.anInt793 + (class47.anInt800 * k1 >> 8);
					int j5 = class47.anInt796 + (class47.anInt803 * k1 >> 8);
					int k6 = class47.anInt797 + (class47.anInt804 * k1 >> 8);
					if (i >= l2 && i <= i4 && j >= j5 && j <= k6) {
						return true;
					}
				}
			} else if (class47.anInt798 == 4) {
				int l1 = k - class47.anInt794;
				if (l1 > 0) {
					int i3 = class47.anInt792 + (class47.anInt799 * l1 >> 8);
					int j4 = class47.anInt793 + (class47.anInt800 * l1 >> 8);
					int k5 = class47.anInt796 + (class47.anInt803 * l1 >> 8);
					int l6 = class47.anInt797 + (class47.anInt804 * l1 >> 8);
					if (i >= i3 && i <= j4 && j >= k5 && j <= l6) {
						return true;
					}
				}
			} else if (class47.anInt798 == 5) {
				int i2 = j - class47.anInt796;
				if (i2 > 0) {
					int j3 = class47.anInt792 + (class47.anInt799 * i2 >> 8);
					int k4 = class47.anInt793 + (class47.anInt800 * i2 >> 8);
					int l5 = class47.anInt794 + (class47.anInt801 * i2 >> 8);
					int i7 = class47.anInt795 + (class47.anInt802 * i2 >> 8);
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
	private final int tileCountX;
	private final int tileCountY;
	private final int[][][] tileHeights;
	private final Ground[][][] groundArray;
	private int lowestPlane;
	private int obj5CacheCurrPos;
	private final Object5[] obj5Cache;
	private final int[][][] anIntArrayArrayArray445;
	private static int anInt446;
	private static int anInt447;
	private static int anInt448;
	private static int anInt449;
	private static int anInt450;
	private static int anInt451;
	private static int anInt452;
	private static int anInt453;
	private static int anInt454;
	private static int anInt455;
	private static int anInt456;
	private static int anInt457;
	private static int anInt458;
	private static int anInt459;
	private static int anInt460;
	private static int anInt461;
	private static Object5[] aClass28Array462 = new Object5[100];
	private static final int[] anIntArray463 = {53, -53, -53, 53};
	private static final int[] anIntArray464 = {-53, -53, 53, 53};
	private static final int[] anIntArray465 = {-45, 45, 45, -45};
	private static final int[] anIntArray466 = {45, 45, -45, -45};
	private static boolean aBoolean467;
	private static int anInt468;
	private static int anInt469;
	public static int anInt470 = -1;
	public static int anInt471 = -1;
	private static final int occluderPlaneCount;
	private static int[] occluderCounts;
	private static Class47[][] occluders;
	private static int anInt475;
	private static final Class47[] aClass47Array476 = new Class47[500];
	private static NodeList aClass19_477 = new NodeList();
	private static final int[] anIntArray478 = {19, 55, 38, 155, 255, 110, 137, 205, 76};
	private static final int[] anIntArray479 = {160, 192, 80, 96, 0, 144, 80, 48, 160};
	private static final int[] anIntArray480 = {76, 8, 137, 4, 0, 1, 38, 2, 19};
	private static final int[] anIntArray481 = {0, 0, 2, 0, 0, 2, 1, 1, 0};
	private static final int[] anIntArray482 = {2, 0, 0, 2, 0, 0, 0, 4, 4};
	private static final int[] anIntArray483 = {0, 4, 4, 8, 0, 0, 8, 0, 0};
	private static final int[] anIntArray484 = {1, 1, 0, 0, 0, 8, 0, 0, 8};
	private static final int[] anIntArray485 = {41, 39248, 41, 4643, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 43086, 41, 41, 41, 41, 41, 41, 41, 8602, 41, 28992, 41, 41, 41, 41, 41, 5056, 41, 41, 41, 7079, 41, 41, 41, 41, 41, 41, 41, 41, 41, 41, 3131, 41, 41, 41};
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
