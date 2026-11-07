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
		readyObjectBuffer = null;
		occluderCounts = null;
		occluders = null;
		drawQueue = null;
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
		for (int l1 = 0; l1 < readyObjectBuffer.length; l1++) {
			readyObjectBuffer[l1] = null;
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
				tile.plane--;
				for (int index = 0; index < tile.objectCount; index++) {
					Object5 sceneObject = tile.obj5Array[index];
					if ((sceneObject.uid >> 29 & 3) == 2 && sceneObject.minTileX == tileX && sceneObject.minTileY == tileY) {
						sceneObject.plane--;
					}
				}

			}
		}
		if (groundArray[0][tileX][tileY] == null) {
			groundArray[0][tileX][tileY] = new Ground(0, tileX, tileY);
		}
		groundArray[0][tileX][tileY].lowerGround = oldGround;
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
			groundArray[plane][tileX][tileY].minCameraPlane = minCameraPlane;
		}
	}

	/**
	 * Corners are ordered (minX,minY), (maxX,minY), (maxX,maxY), (minX,maxY) in world x/y.
	 * Shape 0 is a plain underlay-only tile, shape 1 a plain overlay tile, any other shape indexes the ShapedTile shape tables.
	 */
	public void addTile(int plane, int tileX, int tileY, int shape, int rotation, int textureId, int height0, int height1, int height2, int height3, int underlayColour0, int underlayColour1, int underlayColour2, int underlayColour3, int overlayColour0, int overlayColour1, int overlayColour2, int overlayColour3, int underlayMapColour, int overlayMapColour) {
		if (shape == 0) {
			Class43 plainTile = new Class43(underlayColour0, underlayColour1, underlayColour2, underlayColour3, -1, underlayMapColour, false);
			for (int planeIndex = plane; planeIndex >= 0; planeIndex--) {
				if (groundArray[planeIndex][tileX][tileY] == null) {
					groundArray[planeIndex][tileX][tileY] = new Ground(planeIndex, tileX, tileY);
				}
			}

			groundArray[plane][tileX][tileY].plainTile = plainTile;
			return;
		}
		if (shape == 1) {
			Class43 overlayTile = new Class43(overlayColour0, overlayColour1, overlayColour2, overlayColour3, textureId, overlayMapColour, height0 == height1 && height0 == height2 && height0 == height3);
			for (int planeIndex = plane; planeIndex >= 0; planeIndex--) {
				if (groundArray[planeIndex][tileX][tileY] == null) {
					groundArray[planeIndex][tileX][tileY] = new Ground(planeIndex, tileX, tileY);
				}
			}

			groundArray[plane][tileX][tileY].plainTile = overlayTile;
			return;
		}
		ShapedTile shapedTile = new ShapedTile(tileY, overlayColour0, underlayColour3, height2, textureId, overlayColour2, rotation, underlayColour0, underlayMapColour, underlayColour2, height3, height1, height0, shape, overlayColour3, overlayColour1, underlayColour1, tileX, overlayMapColour);
		for (int planeIndex = plane; planeIndex >= 0; planeIndex--) {
			if (groundArray[planeIndex][tileX][tileY] == null) {
				groundArray[planeIndex][tileX][tileY] = new Ground(planeIndex, tileX, tileY);
			}
		}

		groundArray[plane][tileX][tileY].shapedTile = shapedTile;
	}

	public void addGroundDecoration(int plane, int height, int tileY, Animable model, byte info, int uid, int tileX) {
		if (model == null) {
			return;
		}
		Object3 groundDecoration = new Object3();
		groundDecoration.model = model;
		groundDecoration.worldX = tileX * 128 + 64;
		groundDecoration.worldY = tileY * 128 + 64;
		groundDecoration.height = height;
		groundDecoration.uid = uid;
		groundDecoration.info = info;
		if (groundArray[plane][tileX][tileY] == null) {
			groundArray[plane][tileX][tileY] = new Ground(plane, tileX, tileY);
		}
		groundArray[plane][tileX][tileY].obj3 = groundDecoration;
	}

	public void addItemStack(int tileX, int uid, Animable otherItemModel1, int height, Animable otherItemModel2, Animable topItemModel, int plane, int tileY) {
		Object4 itemStack = new Object4();
		itemStack.topModel = topItemModel;
		itemStack.worldX = tileX * 128 + 64;
		itemStack.worldY = tileY * 128 + 64;
		itemStack.height = height;
		itemStack.uid = uid;
		itemStack.otherModel1 = otherItemModel1;
		itemStack.otherModel2 = otherItemModel2;
		int tallestObjectHeight = 0;
		Ground tile = groundArray[plane][tileX][tileY];
		if (tile != null) {
			for (int index = 0; index < tile.objectCount; index++) {
				if (tile.obj5Array[index].model instanceof Model) {
					int objectHeight = ((Model) tile.obj5Array[index].model).stackSurfaceHeight;
					if (objectHeight > tallestObjectHeight) {
						tallestObjectHeight = objectHeight;
					}
				}
			}

		}
		itemStack.stackHeight = tallestObjectHeight;
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
		wall.info = info;
		wall.worldX = tileX * 128 + 64;
		wall.worldY = tileY * 128 + 64;
		wall.height = height;
		wall.model = model;
		wall.secondaryModel = secondaryModel;
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
		wallDecoration.info = info;
		wallDecoration.worldX = tileX * 128 + 64 + xOffset;
		wallDecoration.worldY = tileY * 128 + 64 + yOffset;
		wallDecoration.height = height;
		wallDecoration.model = model;
		wallDecoration.drawMask = drawMask;
		wallDecoration.rotation = rotation;
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
				if (tile != null && tile.objectCount >= 5) {
					return false;
				}
			}

		}

		Object5 sceneObject = new Object5();
		sceneObject.uid = uid;
		sceneObject.info = info;
		sceneObject.plane = plane;
		sceneObject.worldX = x;
		sceneObject.worldY = y;
		sceneObject.height = height;
		sceneObject.model = model;
		sceneObject.rotation = rotation;
		sceneObject.minTileX = tileX;
		sceneObject.minTileY = tileY;
		sceneObject.maxTileX = tileX + sizeX - 1;
		sceneObject.maxTileY = tileY + sizeY - 1;
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
				tile.obj5Array[tile.objectCount] = sceneObject;
				tile.objectEdgeMasks[tile.objectCount] = edgeMask;
				tile.objectEdgeMask |= edgeMask;
				tile.objectCount++;
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
		for (int tileX = sceneObject.minTileX; tileX <= sceneObject.maxTileX; tileX++) {
			for (int tileY = sceneObject.minTileY; tileY <= sceneObject.maxTileY; tileY++) {
				Ground tile = groundArray[sceneObject.plane][tileX][tileY];
				if (tile != null) {
					for (int index = 0; index < tile.objectCount; index++) {
						if (tile.obj5Array[index] != sceneObject) {
							continue;
						}
						tile.objectCount--;
						for (int shiftIndex = index; shiftIndex < tile.objectCount; shiftIndex++) {
							tile.obj5Array[shiftIndex] = tile.obj5Array[shiftIndex + 1];
							tile.objectEdgeMasks[shiftIndex] = tile.objectEdgeMasks[shiftIndex + 1];
						}

						tile.obj5Array[tile.objectCount] = null;
						break;
					}

					tile.objectEdgeMask = 0;
					for (int maskIndex = 0; maskIndex < tile.objectCount; maskIndex++) {
						tile.objectEdgeMask |= tile.objectEdgeMasks[maskIndex];
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
			wallDecoration.worldX = centreX + (wallDecoration.worldX - centreX) * offsetScaleSixteenths / 16;
			wallDecoration.worldY = centreY + (wallDecoration.worldY - centreY) * offsetScaleSixteenths / 16;
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
		for (int index = 0; index < tile.objectCount; index++) {
			Object5 sceneObject = tile.obj5Array[index];
			if ((sceneObject.uid >> 29 & 3) == 2 && sceneObject.minTileX == tileX && sceneObject.minTileY == tileY) {
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
		for (int index = 0; index < tile.objectCount; index++) {
			Object5 sceneObject = tile.obj5Array[index];
			if ((sceneObject.uid >> 29 & 3) == 2 && sceneObject.minTileX == tileX && sceneObject.minTileY == tileY) {
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
		for (int index = 0; index < tile.objectCount; index++) {
			Object5 sceneObject = tile.obj5Array[index];
			if ((sceneObject.uid >> 29 & 3) == 2 && sceneObject.minTileX == tileX && sceneObject.minTileY == tileY) {
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
			return tile.obj1.info & 0xff;
		}
		if (tile.obj2 != null && tile.obj2.uid == uid) {
			return tile.obj2.info & 0xff;
		}
		if (tile.obj3 != null && tile.obj3.uid == uid) {
			return tile.obj3.info & 0xff;
		}
		for (int index = 0; index < tile.objectCount; index++) {
			if (tile.obj5Array[index].uid == uid) {
				return tile.obj5Array[index].info & 0xff;
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
						if (wall != null && wall.model != null && wall.model.vertexNormals != null) {
							mergeNeighbourNormals(plane, 1, 1, tileX, tileY, (Model) wall.model);
							if (wall.secondaryModel != null && wall.secondaryModel.vertexNormals != null) {
								mergeNeighbourNormals(plane, 1, 1, tileX, tileY, (Model) wall.secondaryModel);
								mergeSharedVertexNormals((Model) wall.model, (Model) wall.secondaryModel, 0, 0, 0, false);
								((Model) wall.secondaryModel).applyVertexLighting(ambientLight, lightContrast, lightX, lightY, lightZ);
							}
							((Model) wall.model).applyVertexLighting(ambientLight, lightContrast, lightX, lightY, lightZ);
						}
						for (int objectIndex = 0; objectIndex < ground.objectCount; objectIndex++) {
							Object5 sceneObject = ground.obj5Array[objectIndex];
							if (sceneObject != null && sceneObject.model != null && sceneObject.model.vertexNormals != null) {
								mergeNeighbourNormals(plane, sceneObject.maxTileX - sceneObject.minTileX + 1, sceneObject.maxTileY - sceneObject.minTileY + 1, tileX, tileY, (Model) sceneObject.model);
								((Model) sceneObject.model).applyVertexLighting(ambientLight, lightContrast, lightX, lightY, lightZ);
							}
						}

						Object3 groundDecoration = ground.obj3;
						if (groundDecoration != null && groundDecoration.model.vertexNormals != null) {
							mergeGroundDecorationNormals(tileX, plane, (Model) groundDecoration.model, tileY);
							((Model) groundDecoration.model).applyVertexLighting(ambientLight, lightContrast, lightX, lightY, lightZ);
						}
					}
				}

			}

		}

	}

	private void mergeGroundDecorationNormals(int tileX, int plane, Model model, int tileY) {
		if (tileX < tileCountX) {
			Ground neighbourPlusX = groundArray[plane][tileX + 1][tileY];
			if (neighbourPlusX != null && neighbourPlusX.obj3 != null && neighbourPlusX.obj3.model.vertexNormals != null) {
				mergeSharedVertexNormals(model, (Model) neighbourPlusX.obj3.model, 128, 0, 0, true);
			}
		}
		if (tileY < tileCountX) {
			Ground neighbourPlusY = groundArray[plane][tileX][tileY + 1];
			if (neighbourPlusY != null && neighbourPlusY.obj3 != null && neighbourPlusY.obj3.model.vertexNormals != null) {
				mergeSharedVertexNormals(model, (Model) neighbourPlusY.obj3.model, 0, 0, 128, true);
			}
		}
		if (tileX < tileCountX && tileY < tileCountY) {
			Ground neighbourPlusXPlusY = groundArray[plane][tileX + 1][tileY + 1];
			if (neighbourPlusXPlusY != null && neighbourPlusXPlusY.obj3 != null && neighbourPlusXPlusY.obj3.model.vertexNormals != null) {
				mergeSharedVertexNormals(model, (Model) neighbourPlusXPlusY.obj3.model, 128, 0, 128, true);
			}
		}
		if (tileX < tileCountX && tileY > 0) {
			Ground neighbourPlusXMinusY = groundArray[plane][tileX + 1][tileY - 1];
			if (neighbourPlusXMinusY != null && neighbourPlusXMinusY.obj3 != null && neighbourPlusXMinusY.obj3.model.vertexNormals != null) {
				mergeSharedVertexNormals(model, (Model) neighbourPlusXMinusY.obj3.model, 128, 0, -128, true);
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
									if (wall != null && wall.model != null && wall.model.vertexNormals != null) {
										mergeSharedVertexNormals(model, (Model) wall.model, (scanX - tileX) * 128 + (1 - sizeX) * 64, heightOffset, (scanY - tileY) * 128 + (1 - sizeY) * 64, onOwnPlane);
									}
									if (wall != null && wall.secondaryModel != null && wall.secondaryModel.vertexNormals != null) {
										mergeSharedVertexNormals(model, (Model) wall.secondaryModel, (scanX - tileX) * 128 + (1 - sizeX) * 64, heightOffset, (scanY - tileY) * 128 + (1 - sizeY) * 64, onOwnPlane);
									}
									for (int objectIndex = 0; objectIndex < neighbour.objectCount; objectIndex++) {
										Object5 sceneObject = neighbour.obj5Array[objectIndex];
										if (sceneObject != null && sceneObject.model != null && sceneObject.model.vertexNormals != null) {
											int otherSizeX = sceneObject.maxTileX - sceneObject.minTileX + 1;
											int otherSizeY = sceneObject.maxTileY - sceneObject.minTileY + 1;
											mergeSharedVertexNormals(model, (Model) sceneObject.model, (sceneObject.minTileX - tileX) * 128 + (otherSizeX - sizeX) * 64, heightOffset, (sceneObject.minTileY - tileY) * 128 + (otherSizeY - sizeY) * 64, onOwnPlane);
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
		int otherX[] = other.vertexX;
		int otherVertexCount = other.vertexCount;
		for (int vertexIndex = 0; vertexIndex < model.vertexCount; vertexIndex++) {
			VertexNormal normal = model.vertexNormals[vertexIndex];
			VertexNormal originalNormal = model.originalVertexNormals[vertexIndex];
			if (originalNormal.faceCount != 0) {
				int shiftedY = model.vertexY[vertexIndex] - offsetY;
				if (shiftedY <= other.maxY) {
					int shiftedX = model.vertexX[vertexIndex] - offsetX;
					if (shiftedX >= other.minX && shiftedX <= other.maxX) {
						int shiftedZ = model.vertexZ[vertexIndex] - offsetZ;
						if (shiftedZ >= other.minZ && shiftedZ <= other.maxZ) {
							for (int otherVertexIndex = 0; otherVertexIndex < otherVertexCount; otherVertexIndex++) {
								VertexNormal otherNormal = other.vertexNormals[otherVertexIndex];
								VertexNormal otherOriginalNormal = other.originalVertexNormals[otherVertexIndex];
								if (shiftedX == otherX[otherVertexIndex] && shiftedZ == other.vertexZ[otherVertexIndex] && shiftedY == other.vertexY[otherVertexIndex] && otherOriginalNormal.faceCount != 0) {
									normal.normalX += otherOriginalNormal.normalX;
									normal.normalY += otherOriginalNormal.normalY;
									normal.normalZ += otherOriginalNormal.normalZ;
									normal.faceCount += otherOriginalNormal.faceCount;
									otherNormal.normalX += originalNormal.normalX;
									otherNormal.normalY += originalNormal.normalY;
									otherNormal.normalZ += originalNormal.normalZ;
									otherNormal.faceCount += originalNormal.faceCount;
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
		for (int faceIndex = 0; faceIndex < model.faceCount; faceIndex++) {
			if (modelSharedVertexStamps[model.faceVertexA[faceIndex]] == mergeStamp && modelSharedVertexStamps[model.faceVertexB[faceIndex]] == mergeStamp && modelSharedVertexStamps[model.faceVertexC[faceIndex]] == mergeStamp) {
				model.faceRenderFlags[faceIndex] = -1;
			}
		}

		for (int otherFaceIndex = 0; otherFaceIndex < other.faceCount; otherFaceIndex++) {
			if (otherSharedVertexStamps[other.faceVertexA[otherFaceIndex]] == mergeStamp && otherSharedVertexStamps[other.faceVertexB[otherFaceIndex]] == mergeStamp && otherSharedVertexStamps[other.faceVertexC[otherFaceIndex]] == mergeStamp) {
				other.faceRenderFlags[otherFaceIndex] = -1;
			}
		}

	}

	public void drawMinimapTile(int pixels[], int pixelOffset, int plane, int tileX, int tileY) {
		int pixelStride = 512;// was parameter
		Ground ground = groundArray[plane][tileX][tileY];
		if (ground == null) {
			return;
		}
		Class43 plainTile = ground.plainTile;
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
		ShapedTile shapedTile = ground.shapedTile;
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
						if (tile.minCameraPlane > plane || !cameraTileVisibility[tileX - cameraTileX + drawDistance][tileY - cameraTileY + drawDistance] && tileHeights[planeIndex][tileX][tileY] - cameraZ < 50) {
							tile.firstPassPending = false;
							tile.drawPending = false;
							tile.cornerWallEdgeMask = 0;
						} else {
							tile.firstPassPending = true;
							tile.drawPending = true;
							tile.objectsPending = tile.objectCount > 0;
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
								if (tile != null && tile.firstPassPending) {
									drawTileQueue(tile, true);
								}
							}
							if (highY < drawMaxTileY) {
								Ground tile = planeTiles[lowX][highY];
								if (tile != null && tile.firstPassPending) {
									drawTileQueue(tile, true);
								}
							}
						}
						if (highX < drawMaxTileX) {
							if (lowY >= drawMinTileY) {
								Ground tile = planeTiles[highX][lowY];
								if (tile != null && tile.firstPassPending) {
									drawTileQueue(tile, true);
								}
							}
							if (highY < drawMaxTileY) {
								Ground tile = planeTiles[highX][highY];
								if (tile != null && tile.firstPassPending) {
									drawTileQueue(tile, true);
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
								if (tile != null && tile.firstPassPending) {
									drawTileQueue(tile, false);
								}
							}
							if (highY < drawMaxTileY) {
								Ground tile = planeTiles[lowX][highY];
								if (tile != null && tile.firstPassPending) {
									drawTileQueue(tile, false);
								}
							}
						}
						if (highX < drawMaxTileX) {
							if (lowY >= drawMinTileY) {
								Ground tile = planeTiles[highX][lowY];
								if (tile != null && tile.firstPassPending) {
									drawTileQueue(tile, false);
								}
							}
							if (highY < drawMaxTileY) {
								Ground tile = planeTiles[highX][highY];
								if (tile != null && tile.firstPassPending) {
									drawTileQueue(tile, false);
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

	private void drawTileQueue(Ground startTile, boolean checkBlockers) {
		drawQueue.insertHead(startTile);
		do {
			Ground tile;
			do {
				tile = (Ground) drawQueue.popTail();
				if (tile == null) {
					return;
				}
			} while (!tile.drawPending);
			int tileX = tile.tileX;
			int tileY = tile.tileY;
			int plane = tile.plane;
			int originalPlane = tile.originalPlane;
			Ground planeTiles[][] = groundArray[plane];
			if (tile.firstPassPending) {
				if (checkBlockers) {
					if (plane > 0) {
						Ground lowerPlaneTile = groundArray[plane - 1][tileX][tileY];
						if (lowerPlaneTile != null && lowerPlaneTile.drawPending) {
							continue;
						}
					}
					if (tileX <= cameraTileX && tileX > drawMinTileX) {
						Ground xMinusTile = planeTiles[tileX - 1][tileY];
						if (xMinusTile != null && xMinusTile.drawPending && (xMinusTile.firstPassPending || (tile.objectEdgeMask & 1) == 0)) {
							continue;
						}
					}
					if (tileX >= cameraTileX && tileX < drawMaxTileX - 1) {
						Ground xPlusTile = planeTiles[tileX + 1][tileY];
						if (xPlusTile != null && xPlusTile.drawPending && (xPlusTile.firstPassPending || (tile.objectEdgeMask & 4) == 0)) {
							continue;
						}
					}
					if (tileY <= cameraTileY && tileY > drawMinTileY) {
						Ground yMinusTile = planeTiles[tileX][tileY - 1];
						if (yMinusTile != null && yMinusTile.drawPending && (yMinusTile.firstPassPending || (tile.objectEdgeMask & 8) == 0)) {
							continue;
						}
					}
					if (tileY >= cameraTileY && tileY < drawMaxTileY - 1) {
						Ground yPlusTile = planeTiles[tileX][tileY + 1];
						if (yPlusTile != null && yPlusTile.drawPending && (yPlusTile.firstPassPending || (tile.objectEdgeMask & 2) == 0)) {
							continue;
						}
					}
				} else {
					checkBlockers = true;
				}
				tile.firstPassPending = false;
				if (tile.lowerGround != null) {
					Ground lowerGround = tile.lowerGround;
					if (lowerGround.plainTile != null) {
						if (!isTileOccluded(0, tileX, tileY)) {
							drawPlainTile(lowerGround.plainTile, 0, pitchSine, pitchCosine, yawSine, yawCosine, tileX, tileY);
						}
					} else if (lowerGround.shapedTile != null && !isTileOccluded(0, tileX, tileY)) {
						drawShapedTile(tileX, pitchSine, yawSine, lowerGround.shapedTile, pitchCosine, tileY, yawCosine);
					}
					Object1 lowerWall = lowerGround.obj1;
					if (lowerWall != null) {
						lowerWall.model.drawFromCamera(0, pitchSine, pitchCosine, yawSine, yawCosine, lowerWall.worldX - cameraWorldX, lowerWall.height - cameraWorldHeight, lowerWall.worldY - cameraWorldY, lowerWall.uid);
					}
					for (int lowerObjectIndex = 0; lowerObjectIndex < lowerGround.objectCount; lowerObjectIndex++) {
						Object5 lowerObject = lowerGround.obj5Array[lowerObjectIndex];
						if (lowerObject != null) {
							lowerObject.model.drawFromCamera(lowerObject.rotation, pitchSine, pitchCosine, yawSine, yawCosine, lowerObject.worldX - cameraWorldX, lowerObject.height - cameraWorldHeight, lowerObject.worldY - cameraWorldY, lowerObject.uid);
						}
					}

				}
				boolean tileContentDrawn = false;
				if (tile.plainTile != null) {
					if (!isTileOccluded(originalPlane, tileX, tileY)) {
						tileContentDrawn = true;
						drawPlainTile(tile.plainTile, originalPlane, pitchSine, pitchCosine, yawSine, yawCosine, tileX, tileY);
					}
				} else if (tile.shapedTile != null && !isTileOccluded(originalPlane, tileX, tileY)) {
					tileContentDrawn = true;
					drawShapedTile(tileX, pitchSine, yawSine, tile.shapedTile, pitchCosine, tileY, yawCosine);
				}
				int cameraRelation = 0;
				int wallFirstMask = 0;
				Object1 wall = tile.obj1;
				Object2 wallDecoration = tile.obj2;
				if (wall != null || wallDecoration != null) {
					if (cameraTileX == tileX) {
						cameraRelation++;
					} else if (cameraTileX < tileX) {
						cameraRelation += 2;
					}
					if (cameraTileY == tileY) {
						cameraRelation += 3;
					} else if (cameraTileY > tileY) {
						cameraRelation += 6;
					}
					wallFirstMask = wallFirstMasks[cameraRelation];
					tile.lateWallMask = wallLateMasks[cameraRelation];
				}
				if (wall != null) {
					if ((wall.orientation & cornerWallOrderMasks[cameraRelation]) != 0) {
						if (wall.orientation == 16) {
							tile.cornerWallEdgeMask = 3;
							tile.cornerWallFarEdgeValue = cornerWallFarValues16[cameraRelation];
							tile.cornerWallNearEdgeValue = 3 - tile.cornerWallFarEdgeValue;
						} else if (wall.orientation == 32) {
							tile.cornerWallEdgeMask = 6;
							tile.cornerWallFarEdgeValue = cornerWallFarValues32[cameraRelation];
							tile.cornerWallNearEdgeValue = 6 - tile.cornerWallFarEdgeValue;
						} else if (wall.orientation == 64) {
							tile.cornerWallEdgeMask = 12;
							tile.cornerWallFarEdgeValue = cornerWallFarValues64[cameraRelation];
							tile.cornerWallNearEdgeValue = 12 - tile.cornerWallFarEdgeValue;
						} else {
							tile.cornerWallEdgeMask = 9;
							tile.cornerWallFarEdgeValue = cornerWallFarValues128[cameraRelation];
							tile.cornerWallNearEdgeValue = 9 - tile.cornerWallFarEdgeValue;
						}
					} else {
						tile.cornerWallEdgeMask = 0;
					}
					if ((wall.orientation & wallFirstMask) != 0 && !isWallOccluded(originalPlane, tileX, tileY, wall.orientation)) {
						wall.model.drawFromCamera(0, pitchSine, pitchCosine, yawSine, yawCosine, wall.worldX - cameraWorldX, wall.height - cameraWorldHeight, wall.worldY - cameraWorldY, wall.uid);
					}
					if ((wall.orientation1 & wallFirstMask) != 0 && !isWallOccluded(originalPlane, tileX, tileY, wall.orientation1)) {
						wall.secondaryModel.drawFromCamera(0, pitchSine, pitchCosine, yawSine, yawCosine, wall.worldX - cameraWorldX, wall.height - cameraWorldHeight, wall.worldY - cameraWorldY, wall.uid);
					}
				}
				if (wallDecoration != null && !isTileColumnOccluded(originalPlane, tileX, tileY, wallDecoration.model.modelHeight)) {
					if ((wallDecoration.drawMask & wallFirstMask) != 0) {
						wallDecoration.model.drawFromCamera(wallDecoration.rotation, pitchSine, pitchCosine, yawSine, yawCosine, wallDecoration.worldX - cameraWorldX, wallDecoration.height - cameraWorldHeight, wallDecoration.worldY - cameraWorldY, wallDecoration.uid);
					} else if ((wallDecoration.drawMask & 0x300) != 0) {
						int decorationX = wallDecoration.worldX - cameraWorldX;
						int decorationHeight = wallDecoration.height - cameraWorldHeight;
						int decorationY = wallDecoration.worldY - cameraWorldY;
						int rotation = wallDecoration.rotation;
						int orientedX;
						if (rotation == 1 || rotation == 2) {
							orientedX = -decorationX;
						} else {
							orientedX = decorationX;
						}
						int orientedY;
						if (rotation == 2 || rotation == 3) {
							orientedY = -decorationY;
						} else {
							orientedY = decorationY;
						}
						if ((wallDecoration.drawMask & 0x100) != 0 && orientedY < orientedX) {
							int firstDiagonalX = decorationX + firstDiagonalOffsetX[rotation];
							int firstDiagonalY = decorationY + firstDiagonalOffsetY[rotation];
							wallDecoration.model.drawFromCamera(rotation * 512 + 256, pitchSine, pitchCosine, yawSine, yawCosine, firstDiagonalX, decorationHeight, firstDiagonalY, wallDecoration.uid);
						}
						if ((wallDecoration.drawMask & 0x200) != 0 && orientedY > orientedX) {
							int secondDiagonalX = decorationX + secondDiagonalOffsetX[rotation];
							int secondDiagonalY = decorationY + secondDiagonalOffsetY[rotation];
							wallDecoration.model.drawFromCamera(rotation * 512 + 1280 & 0x7ff, pitchSine, pitchCosine, yawSine, yawCosine, secondDiagonalX, decorationHeight, secondDiagonalY, wallDecoration.uid);
						}
					}
				}
				if (tileContentDrawn) {
					Object3 groundDecoration = tile.obj3;
					if (groundDecoration != null) {
						groundDecoration.model.drawFromCamera(0, pitchSine, pitchCosine, yawSine, yawCosine, groundDecoration.worldX - cameraWorldX, groundDecoration.height - cameraWorldHeight, groundDecoration.worldY - cameraWorldY, groundDecoration.uid);
					}
					Object4 itemStack = tile.obj4;
					if (itemStack != null && itemStack.stackHeight == 0) {
						if (itemStack.otherModel1 != null) {
							itemStack.otherModel1.drawFromCamera(0, pitchSine, pitchCosine, yawSine, yawCosine, itemStack.worldX - cameraWorldX, itemStack.height - cameraWorldHeight, itemStack.worldY - cameraWorldY, itemStack.uid);
						}
						if (itemStack.otherModel2 != null) {
							itemStack.otherModel2.drawFromCamera(0, pitchSine, pitchCosine, yawSine, yawCosine, itemStack.worldX - cameraWorldX, itemStack.height - cameraWorldHeight, itemStack.worldY - cameraWorldY, itemStack.uid);
						}
						if (itemStack.topModel != null) {
							itemStack.topModel.drawFromCamera(0, pitchSine, pitchCosine, yawSine, yawCosine, itemStack.worldX - cameraWorldX, itemStack.height - cameraWorldHeight, itemStack.worldY - cameraWorldY, itemStack.uid);
						}
					}
				}
				int edgeMask = tile.objectEdgeMask;
				if (edgeMask != 0) {
					if (tileX < cameraTileX && (edgeMask & 4) != 0) {
						Ground xPlusTile = planeTiles[tileX + 1][tileY];
						if (xPlusTile != null && xPlusTile.drawPending) {
							drawQueue.insertHead(xPlusTile);
						}
					}
					if (tileY < cameraTileY && (edgeMask & 2) != 0) {
						Ground yPlusTile = planeTiles[tileX][tileY + 1];
						if (yPlusTile != null && yPlusTile.drawPending) {
							drawQueue.insertHead(yPlusTile);
						}
					}
					if (tileX > cameraTileX && (edgeMask & 1) != 0) {
						Ground xMinusTile = planeTiles[tileX - 1][tileY];
						if (xMinusTile != null && xMinusTile.drawPending) {
							drawQueue.insertHead(xMinusTile);
						}
					}
					if (tileY > cameraTileY && (edgeMask & 8) != 0) {
						Ground yMinusTile = planeTiles[tileX][tileY - 1];
						if (yMinusTile != null && yMinusTile.drawPending) {
							drawQueue.insertHead(yMinusTile);
						}
					}
				}
			}
			if (tile.cornerWallEdgeMask != 0) {
				boolean noObjectBeforeCornerWall = true;
				for (int objectIndex = 0; objectIndex < tile.objectCount; objectIndex++) {
					if (tile.obj5Array[objectIndex].lastDrawCycle == drawCycle || (tile.objectEdgeMasks[objectIndex] & tile.cornerWallEdgeMask) != tile.cornerWallFarEdgeValue) {
						continue;
					}
					noObjectBeforeCornerWall = false;
					break;
				}

				if (noObjectBeforeCornerWall) {
					Object1 cornerWall = tile.obj1;
					if (!isWallOccluded(originalPlane, tileX, tileY, cornerWall.orientation)) {
						cornerWall.model.drawFromCamera(0, pitchSine, pitchCosine, yawSine, yawCosine, cornerWall.worldX - cameraWorldX, cornerWall.height - cameraWorldHeight, cornerWall.worldY - cameraWorldY, cornerWall.uid);
					}
					tile.cornerWallEdgeMask = 0;
				}
			}
			if (tile.objectsPending) {
				try {
					int objectCount = tile.objectCount;
					tile.objectsPending = false;
					int readyObjectCount = 0;
					objectScan : for (int objectIndex = 0; objectIndex < objectCount; objectIndex++) {
						Object5 sceneObject = tile.obj5Array[objectIndex];
						if (sceneObject.lastDrawCycle == drawCycle) {
							continue;
						}
						for (int footprintX = sceneObject.minTileX; footprintX <= sceneObject.maxTileX; footprintX++) {
							for (int footprintY = sceneObject.minTileY; footprintY <= sceneObject.maxTileY; footprintY++) {
								Ground footprintTile = planeTiles[footprintX][footprintY];
								if (footprintTile.firstPassPending) {
									tile.objectsPending = true;
								} else {
									if (footprintTile.cornerWallEdgeMask == 0) {
										continue;
									}
									int footprintEdgeMask = 0;
									if (footprintX > sceneObject.minTileX) {
										footprintEdgeMask++;
									}
									if (footprintX < sceneObject.maxTileX) {
										footprintEdgeMask += 4;
									}
									if (footprintY > sceneObject.minTileY) {
										footprintEdgeMask += 8;
									}
									if (footprintY < sceneObject.maxTileY) {
										footprintEdgeMask += 2;
									}
									if ((footprintEdgeMask & footprintTile.cornerWallEdgeMask) != tile.cornerWallNearEdgeValue) {
										continue;
									}
									tile.objectsPending = true;
								}
								continue objectScan;
							}

						}

						readyObjectBuffer[readyObjectCount++] = sceneObject;
						int farXDistance = cameraTileX - sceneObject.minTileX;
						int xDistanceToMax = sceneObject.maxTileX - cameraTileX;
						if (xDistanceToMax > farXDistance) {
							farXDistance = xDistanceToMax;
						}
						int yDistanceToMin = cameraTileY - sceneObject.minTileY;
						int yDistanceToMax = sceneObject.maxTileY - cameraTileY;
						if (yDistanceToMax > yDistanceToMin) {
							sceneObject.cameraDistance = farXDistance + yDistanceToMax;
						} else {
							sceneObject.cameraDistance = farXDistance + yDistanceToMin;
						}
					}

					while (readyObjectCount > 0) {
						int farthestDistance = -50;
						int farthestIndex = -1;
						for (int readyIndex = 0; readyIndex < readyObjectCount; readyIndex++) {
							Object5 candidate = readyObjectBuffer[readyIndex];
							if (candidate.lastDrawCycle != drawCycle) {
								if (candidate.cameraDistance > farthestDistance) {
									farthestDistance = candidate.cameraDistance;
									farthestIndex = readyIndex;
								} else if (candidate.cameraDistance == farthestDistance) {
									int candidateRelX = candidate.worldX - cameraWorldX;
									int candidateRelY = candidate.worldY - cameraWorldY;
									int farthestRelX = readyObjectBuffer[farthestIndex].worldX - cameraWorldX;
									int farthestRelY = readyObjectBuffer[farthestIndex].worldY - cameraWorldY;
									if (candidateRelX * candidateRelX + candidateRelY * candidateRelY > farthestRelX * farthestRelX + farthestRelY * farthestRelY) {
										farthestIndex = readyIndex;
									}
								}
							}
						}

						if (farthestIndex == -1) {
							break;
						}
						Object5 drawnObject = readyObjectBuffer[farthestIndex];
						drawnObject.lastDrawCycle = drawCycle;
						if (!isFootprintOccluded(originalPlane, drawnObject.minTileX, drawnObject.maxTileX, drawnObject.minTileY, drawnObject.maxTileY, drawnObject.model.modelHeight)) {
							drawnObject.model.drawFromCamera(drawnObject.rotation, pitchSine, pitchCosine, yawSine, yawCosine, drawnObject.worldX - cameraWorldX, drawnObject.height - cameraWorldHeight, drawnObject.worldY - cameraWorldY, drawnObject.uid);
						}
						for (int footprintX = drawnObject.minTileX; footprintX <= drawnObject.maxTileX; footprintX++) {
							for (int footprintY = drawnObject.minTileY; footprintY <= drawnObject.maxTileY; footprintY++) {
								Ground footprintTile = planeTiles[footprintX][footprintY];
								if (footprintTile.cornerWallEdgeMask != 0) {
									drawQueue.insertHead(footprintTile);
								} else if ((footprintX != tileX || footprintY != tileY) && footprintTile.drawPending) {
									drawQueue.insertHead(footprintTile);
								}
							}

						}

					}
					if (tile.objectsPending) {
						continue;
					}
				} catch (Exception _ex) {
					tile.objectsPending = false;
				}
			}
			if (!tile.drawPending || tile.cornerWallEdgeMask != 0) {
				continue;
			}
			if (tileX <= cameraTileX && tileX > drawMinTileX) {
				Ground xMinusTile = planeTiles[tileX - 1][tileY];
				if (xMinusTile != null && xMinusTile.drawPending) {
					continue;
				}
			}
			if (tileX >= cameraTileX && tileX < drawMaxTileX - 1) {
				Ground xPlusTile = planeTiles[tileX + 1][tileY];
				if (xPlusTile != null && xPlusTile.drawPending) {
					continue;
				}
			}
			if (tileY <= cameraTileY && tileY > drawMinTileY) {
				Ground yMinusTile = planeTiles[tileX][tileY - 1];
				if (yMinusTile != null && yMinusTile.drawPending) {
					continue;
				}
			}
			if (tileY >= cameraTileY && tileY < drawMaxTileY - 1) {
				Ground yPlusTile = planeTiles[tileX][tileY + 1];
				if (yPlusTile != null && yPlusTile.drawPending) {
					continue;
				}
			}
			tile.drawPending = false;
			pendingTileCount--;
			Object4 liftedItemStack = tile.obj4;
			if (liftedItemStack != null && liftedItemStack.stackHeight != 0) {
				if (liftedItemStack.otherModel1 != null) {
					liftedItemStack.otherModel1.drawFromCamera(0, pitchSine, pitchCosine, yawSine, yawCosine, liftedItemStack.worldX - cameraWorldX, liftedItemStack.height - cameraWorldHeight - liftedItemStack.stackHeight, liftedItemStack.worldY - cameraWorldY, liftedItemStack.uid);
				}
				if (liftedItemStack.otherModel2 != null) {
					liftedItemStack.otherModel2.drawFromCamera(0, pitchSine, pitchCosine, yawSine, yawCosine, liftedItemStack.worldX - cameraWorldX, liftedItemStack.height - cameraWorldHeight - liftedItemStack.stackHeight, liftedItemStack.worldY - cameraWorldY, liftedItemStack.uid);
				}
				if (liftedItemStack.topModel != null) {
					liftedItemStack.topModel.drawFromCamera(0, pitchSine, pitchCosine, yawSine, yawCosine, liftedItemStack.worldX - cameraWorldX, liftedItemStack.height - cameraWorldHeight - liftedItemStack.stackHeight, liftedItemStack.worldY - cameraWorldY, liftedItemStack.uid);
				}
			}
			if (tile.lateWallMask != 0) {
				Object2 lateWallDecoration = tile.obj2;
				if (lateWallDecoration != null && !isTileColumnOccluded(originalPlane, tileX, tileY, lateWallDecoration.model.modelHeight)) {
					if ((lateWallDecoration.drawMask & tile.lateWallMask) != 0) {
						lateWallDecoration.model.drawFromCamera(lateWallDecoration.rotation, pitchSine, pitchCosine, yawSine, yawCosine, lateWallDecoration.worldX - cameraWorldX, lateWallDecoration.height - cameraWorldHeight, lateWallDecoration.worldY - cameraWorldY, lateWallDecoration.uid);
					} else if ((lateWallDecoration.drawMask & 0x300) != 0) {
						int decorationX = lateWallDecoration.worldX - cameraWorldX;
						int decorationHeight = lateWallDecoration.height - cameraWorldHeight;
						int decorationY = lateWallDecoration.worldY - cameraWorldY;
						int rotation = lateWallDecoration.rotation;
						int orientedX;
						if (rotation == 1 || rotation == 2) {
							orientedX = -decorationX;
						} else {
							orientedX = decorationX;
						}
						int orientedY;
						if (rotation == 2 || rotation == 3) {
							orientedY = -decorationY;
						} else {
							orientedY = decorationY;
						}
						if ((lateWallDecoration.drawMask & 0x100) != 0 && orientedY >= orientedX) {
							int firstDiagonalX = decorationX + firstDiagonalOffsetX[rotation];
							int firstDiagonalY = decorationY + firstDiagonalOffsetY[rotation];
							lateWallDecoration.model.drawFromCamera(rotation * 512 + 256, pitchSine, pitchCosine, yawSine, yawCosine, firstDiagonalX, decorationHeight, firstDiagonalY, lateWallDecoration.uid);
						}
						if ((lateWallDecoration.drawMask & 0x200) != 0 && orientedY <= orientedX) {
							int secondDiagonalX = decorationX + secondDiagonalOffsetX[rotation];
							int secondDiagonalY = decorationY + secondDiagonalOffsetY[rotation];
							lateWallDecoration.model.drawFromCamera(rotation * 512 + 1280 & 0x7ff, pitchSine, pitchCosine, yawSine, yawCosine, secondDiagonalX, decorationHeight, secondDiagonalY, lateWallDecoration.uid);
						}
					}
				}
				Object1 lateWall = tile.obj1;
				if (lateWall != null) {
					if ((lateWall.orientation1 & tile.lateWallMask) != 0 && !isWallOccluded(originalPlane, tileX, tileY, lateWall.orientation1)) {
						lateWall.secondaryModel.drawFromCamera(0, pitchSine, pitchCosine, yawSine, yawCosine, lateWall.worldX - cameraWorldX, lateWall.height - cameraWorldHeight, lateWall.worldY - cameraWorldY, lateWall.uid);
					}
					if ((lateWall.orientation & tile.lateWallMask) != 0 && !isWallOccluded(originalPlane, tileX, tileY, lateWall.orientation)) {
						lateWall.model.drawFromCamera(0, pitchSine, pitchCosine, yawSine, yawCosine, lateWall.worldX - cameraWorldX, lateWall.height - cameraWorldHeight, lateWall.worldY - cameraWorldY, lateWall.uid);
					}
				}
			}
			if (plane < planeCount - 1) {
				Ground upperPlaneTile = groundArray[plane + 1][tileX][tileY];
				if (upperPlaneTile != null && upperPlaneTile.drawPending) {
					drawQueue.insertHead(upperPlaneTile);
				}
			}
			if (tileX < cameraTileX) {
				Ground xPlusTile = planeTiles[tileX + 1][tileY];
				if (xPlusTile != null && xPlusTile.drawPending) {
					drawQueue.insertHead(xPlusTile);
				}
			}
			if (tileY < cameraTileY) {
				Ground yPlusTile = planeTiles[tileX][tileY + 1];
				if (yPlusTile != null && yPlusTile.drawPending) {
					drawQueue.insertHead(yPlusTile);
				}
			}
			if (tileX > cameraTileX) {
				Ground xMinusTile = planeTiles[tileX - 1][tileY];
				if (xMinusTile != null && xMinusTile.drawPending) {
					drawQueue.insertHead(xMinusTile);
				}
			}
			if (tileY > cameraTileY) {
				Ground yMinusTile = planeTiles[tileX][tileY - 1];
				if (yMinusTile != null && yMinusTile.drawPending) {
					drawQueue.insertHead(yMinusTile);
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

	private void drawShapedTile(int tileX, int pitchSine, int yawSine, ShapedTile tile, int pitchCosine, int tileY, int yawCosine) {
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
				ShapedTile.cameraSpaceX[vertex] = x;
				ShapedTile.cameraSpaceHeight[vertex] = height;
				ShapedTile.cameraSpaceDepth[vertex] = depth;
			}
			ShapedTile.projectedX[vertex] = Texture.textureInt1 + (x << 9) / depth;
			ShapedTile.projectedY[vertex] = Texture.textureInt2 + (height << 9) / depth;
		}

		Texture.transparency = 0;
		count = tile.faceVertexA.length;
		for (int face = 0; face < count; face++) {
			int vertexA = tile.faceVertexA[face];
			int vertexB = tile.faceVertexB[face];
			int vertexC = tile.faceVertexC[face];
			int screenXA = ShapedTile.projectedX[vertexA];
			int screenXB = ShapedTile.projectedX[vertexB];
			int screenXC = ShapedTile.projectedX[vertexC];
			int screenYA = ShapedTile.projectedY[vertexA];
			int screenYB = ShapedTile.projectedY[vertexB];
			int screenYC = ShapedTile.projectedY[vertexC];
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
						Texture.drawTexturedTriangle(screenYA, screenYB, screenYC, screenXA, screenXB, screenXC, tile.faceColourA[face], tile.faceColourB[face], tile.faceColourC[face], ShapedTile.cameraSpaceX[0], ShapedTile.cameraSpaceX[1], ShapedTile.cameraSpaceX[3], ShapedTile.cameraSpaceHeight[0], ShapedTile.cameraSpaceHeight[1], ShapedTile.cameraSpaceHeight[3], ShapedTile.cameraSpaceDepth[0], ShapedTile.cameraSpaceDepth[1], ShapedTile.cameraSpaceDepth[3], tile.faceTexture[face]);
					} else {
						Texture.drawTexturedTriangle(screenYA, screenYB, screenYC, screenXA, screenXB, screenXC, tile.faceColourA[face], tile.faceColourB[face], tile.faceColourC[face], ShapedTile.cameraSpaceX[vertexA], ShapedTile.cameraSpaceX[vertexB], ShapedTile.cameraSpaceX[vertexC], ShapedTile.cameraSpaceHeight[vertexA], ShapedTile.cameraSpaceHeight[vertexB], ShapedTile.cameraSpaceHeight[vertexC], ShapedTile.cameraSpaceDepth[vertexA], ShapedTile.cameraSpaceDepth[vertexB], ShapedTile.cameraSpaceDepth[vertexC], tile.faceTexture[face]);
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
	private static Object5[] readyObjectBuffer = new Object5[100];
	private static final int[] firstDiagonalOffsetX = {53, -53, -53, 53};
	private static final int[] firstDiagonalOffsetY = {-53, -53, 53, 53};
	private static final int[] secondDiagonalOffsetX = {-45, 45, 45, -45};
	private static final int[] secondDiagonalOffsetY = {45, 45, -45, -45};
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
	private static NodeList drawQueue = new NodeList();
	private static final int[] wallFirstMasks = {19, 55, 38, 155, 255, 110, 137, 205, 76};
	private static final int[] cornerWallOrderMasks = {160, 192, 80, 96, 0, 144, 80, 48, 160};
	private static final int[] wallLateMasks = {76, 8, 137, 4, 0, 1, 38, 2, 19};
	private static final int[] cornerWallFarValues16 = {0, 0, 2, 0, 0, 2, 1, 1, 0};
	private static final int[] cornerWallFarValues32 = {2, 0, 0, 2, 0, 0, 0, 4, 4};
	private static final int[] cornerWallFarValues64 = {0, 4, 4, 8, 0, 0, 8, 0, 0};
	private static final int[] cornerWallFarValues128 = {1, 1, 0, 0, 0, 8, 0, 0, 8};
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
