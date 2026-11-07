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
		anInt437 = k;
		anInt438 = j;
		anInt439 = i;
		groundArray = new Ground[k][j][i];
		anIntArrayArrayArray445 = new int[k][j + 1][i + 1];
		anIntArrayArrayArray440 = ai;
		initToNull();
	}

	public static void nullLoader() {
		aClass28Array462 = null;
		anIntArray473 = null;
		aClass47ArrayArray474 = null;
		aClass19_477 = null;
		tileVisibilityTable = null;
		cameraTileVisibility = null;
	}

	public void initToNull() {
		for (int j = 0; j < anInt437; j++) {
			for (int k = 0; k < anInt438; k++) {
				for (int i1 = 0; i1 < anInt439; i1++) {
					groundArray[j][k][i1] = null;
				}

			}

		}
		for (int l = 0; l < anInt472; l++) {
			for (int j1 = 0; j1 < anIntArray473[l]; j1++) {
				aClass47ArrayArray474[l][j1] = null;
			}

			anIntArray473[l] = 0;
		}

		for (int k1 = 0; k1 < obj5CacheCurrPos; k1++) {
			obj5Cache[k1] = null;
		}

		obj5CacheCurrPos = 0;
		for (int l1 = 0; l1 < aClass28Array462.length; l1++) {
			aClass28Array462[l1] = null;
		}

	}

	public void method275(int i) {
		anInt442 = i;
		for (int k = 0; k < anInt438; k++) {
			for (int l = 0; l < anInt439; l++) {
				if (groundArray[i][k][l] == null) {
					groundArray[i][k][l] = new Ground(i, k, l);
				}
			}

		}

	}

	public void method276(int i, int j) {
		Ground class30_sub3 = groundArray[0][j][i];
		for (int l = 0; l < 3; l++) {
			Ground class30_sub3_1 = groundArray[l][j][i] = groundArray[l + 1][j][i];
			if (class30_sub3_1 != null) {
				class30_sub3_1.anInt1307--;
				for (int j1 = 0; j1 < class30_sub3_1.anInt1317; j1++) {
					Object5 class28 = class30_sub3_1.obj5Array[j1];
					if ((class28.uid >> 29 & 3) == 2 && class28.anInt523 == j && class28.anInt525 == i) {
						class28.anInt517--;
					}
				}

			}
		}
		if (groundArray[0][j][i] == null) {
			groundArray[0][j][i] = new Ground(0, j, i);
		}
		groundArray[0][j][i].aClass30_Sub3_1329 = class30_sub3;
		groundArray[3][j][i] = null;
	}

	public static void method277(int i, int j, int k, int l, int i1, int j1, int l1, int i2) {
		Class47 class47 = new Class47();
		class47.anInt787 = j / 128;
		class47.anInt788 = l / 128;
		class47.anInt789 = l1 / 128;
		class47.anInt790 = i1 / 128;
		class47.anInt791 = i2;
		class47.anInt792 = j;
		class47.anInt793 = l;
		class47.anInt794 = l1;
		class47.anInt795 = i1;
		class47.anInt796 = j1;
		class47.anInt797 = k;
		aClass47ArrayArray474[i][anIntArray473[i]++] = class47;
	}

	public void method278(int i, int j, int k, int l) {
		Ground class30_sub3 = groundArray[i][j][k];
		if (class30_sub3 != null) {
			groundArray[i][j][k].anInt1321 = l;
		}
	}

	public void method279(int i, int j, int k, int l, int i1, int j1, int k1, int l1, int i2, int j2, int k2, int l2, int i3, int j3, int k3, int l3, int i4, int j4, int k4, int l4) {
		if (l == 0) {
			Class43 class43 = new Class43(k2, l2, i3, j3, -1, k4, false);
			for (int i5 = i; i5 >= 0; i5--) {
				if (groundArray[i5][j][k] == null) {
					groundArray[i5][j][k] = new Ground(i5, j, k);
				}
			}

			groundArray[i][j][k].aClass43_1311 = class43;
			return;
		}
		if (l == 1) {
			Class43 class43_1 = new Class43(k3, l3, i4, j4, j1, l4, k1 == l1 && k1 == i2 && k1 == j2);
			for (int j5 = i; j5 >= 0; j5--) {
				if (groundArray[j5][j][k] == null) {
					groundArray[j5][j][k] = new Ground(j5, j, k);
				}
			}

			groundArray[i][j][k].aClass43_1311 = class43_1;
			return;
		}
		Class40 class40 = new Class40(k, k3, j3, i2, j1, i4, i1, k2, k4, i3, j2, l1, k1, l, j4, l3, l2, j, l4);
		for (int k5 = i; k5 >= 0; k5--) {
			if (groundArray[k5][j][k] == null) {
				groundArray[k5][j][k] = new Ground(k5, j, k);
			}
		}

		groundArray[i][j][k].aClass40_1312 = class40;
	}

	public void method280(int i, int j, int k, Animable class30_sub2_sub4, byte byte0, int i1, int j1) {
		if (class30_sub2_sub4 == null) {
			return;
		}
		Object3 class49 = new Object3();
		class49.aClass30_Sub2_Sub4_814 = class30_sub2_sub4;
		class49.anInt812 = j1 * 128 + 64;
		class49.anInt813 = k * 128 + 64;
		class49.anInt811 = j;
		class49.uid = i1;
		class49.aByte816 = byte0;
		if (groundArray[i][j1][k] == null) {
			groundArray[i][j1][k] = new Ground(i, j1, k);
		}
		groundArray[i][j1][k].obj3 = class49;
	}

	public void method281(int i, int j, Animable class30_sub2_sub4, int k, Animable class30_sub2_sub4_1, Animable class30_sub2_sub4_2, int l, int i1) {
		Object4 object4 = new Object4();
		object4.aClass30_Sub2_Sub4_48 = class30_sub2_sub4_2;
		object4.anInt46 = i * 128 + 64;
		object4.anInt47 = i1 * 128 + 64;
		object4.anInt45 = k;
		object4.uid = j;
		object4.aClass30_Sub2_Sub4_49 = class30_sub2_sub4;
		object4.aClass30_Sub2_Sub4_50 = class30_sub2_sub4_1;
		int j1 = 0;
		Ground class30_sub3 = groundArray[l][i][i1];
		if (class30_sub3 != null) {
			for (int k1 = 0; k1 < class30_sub3.anInt1317; k1++) {
				if (class30_sub3.obj5Array[k1].aClass30_Sub2_Sub4_521 instanceof Model) {
					int l1 = ((Model) class30_sub3.obj5Array[k1].aClass30_Sub2_Sub4_521).anInt1654;
					if (l1 > j1) {
						j1 = l1;
					}
				}
			}

		}
		object4.anInt52 = j1;
		if (groundArray[l][i][i1] == null) {
			groundArray[l][i][i1] = new Ground(l, i, i1);
		}
		groundArray[l][i][i1].obj4 = object4;
	}

	public void method282(int i, Animable class30_sub2_sub4, int j, int k, byte byte0, int l, Animable class30_sub2_sub4_1, int i1, int j1, int k1) {
		if (class30_sub2_sub4 == null && class30_sub2_sub4_1 == null) {
			return;
		}
		Object1 object1 = new Object1();
		object1.uid = j;
		object1.aByte281 = byte0;
		object1.anInt274 = l * 128 + 64;
		object1.anInt275 = k * 128 + 64;
		object1.anInt273 = i1;
		object1.aClass30_Sub2_Sub4_278 = class30_sub2_sub4;
		object1.aClass30_Sub2_Sub4_279 = class30_sub2_sub4_1;
		object1.orientation = i;
		object1.orientation1 = j1;
		for (int l1 = k1; l1 >= 0; l1--) {
			if (groundArray[l1][l][k] == null) {
				groundArray[l1][l][k] = new Ground(l1, l, k);
			}
		}

		groundArray[k1][l][k].obj1 = object1;
	}

	public void method283(int i, int j, int k, int i1, int j1, int k1, Animable class30_sub2_sub4, int l1, byte byte0, int i2, int j2) {
		if (class30_sub2_sub4 == null) {
			return;
		}
		Object2 class26 = new Object2();
		class26.uid = i;
		class26.aByte506 = byte0;
		class26.anInt500 = l1 * 128 + 64 + j1;
		class26.anInt501 = j * 128 + 64 + i2;
		class26.anInt499 = k1;
		class26.aClass30_Sub2_Sub4_504 = class30_sub2_sub4;
		class26.anInt502 = j2;
		class26.anInt503 = k;
		for (int k2 = i1; k2 >= 0; k2--) {
			if (groundArray[k2][l1][j] == null) {
				groundArray[k2][l1][j] = new Ground(k2, l1, j);
			}
		}

		groundArray[i1][l1][j].obj2 = class26;
	}

	public boolean method284(int i, byte byte0, int j, int k, Animable class30_sub2_sub4, int l, int i1, int j1, int k1, int l1) {
		if (class30_sub2_sub4 == null) {
			return true;
		} else {
			int i2 = l1 * 128 + 64 * l;
			int j2 = k1 * 128 + 64 * k;
			return method287(i1, l1, k1, l, k, i2, j2, j, class30_sub2_sub4, j1, false, i, byte0);
		}
	}

	public boolean method285(int i, int j, int k, int l, int i1, int j1, int k1, Animable class30_sub2_sub4, boolean flag) {
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
		return method287(i, l1, i2, j2 - l1 + 1, k2 - i2 + 1, k1, i1, k, class30_sub2_sub4, j, true, l, (byte) 0);
	}

	public boolean method286(int j, int k, Animable class30_sub2_sub4, int l, int i1, int j1, int k1, int l1, int i2, int j2, int k2) {
		return class30_sub2_sub4 == null || method287(j, l1, k2, i2 - l1 + 1, i1 - k2 + 1, j1, k, k1, class30_sub2_sub4, l, true, j2, (byte) 0);
	}

	private boolean method287(int i, int j, int k, int l, int i1, int j1, int k1, int l1, Animable class30_sub2_sub4, int i2, boolean flag, int j2, byte byte0) {
		for (int k2 = j; k2 < j + l; k2++) {
			for (int l2 = k; l2 < k + i1; l2++) {
				if (k2 < 0 || l2 < 0 || k2 >= anInt438 || l2 >= anInt439) {
					return false;
				}
				Ground class30_sub3 = groundArray[i][k2][l2];
				if (class30_sub3 != null && class30_sub3.anInt1317 >= 5) {
					return false;
				}
			}

		}

		Object5 class28 = new Object5();
		class28.uid = j2;
		class28.aByte530 = byte0;
		class28.anInt517 = i;
		class28.anInt519 = j1;
		class28.anInt520 = k1;
		class28.anInt518 = l1;
		class28.aClass30_Sub2_Sub4_521 = class30_sub2_sub4;
		class28.anInt522 = i2;
		class28.anInt523 = j;
		class28.anInt525 = k;
		class28.anInt524 = j + l - 1;
		class28.anInt526 = k + i1 - 1;
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
				class30_sub3_1.obj5Array[class30_sub3_1.anInt1317] = class28;
				class30_sub3_1.anIntArray1319[class30_sub3_1.anInt1317] = k3;
				class30_sub3_1.anInt1320 |= k3;
				class30_sub3_1.anInt1317++;
			}

		}

		if (flag) {
			obj5Cache[obj5CacheCurrPos++] = class28;
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
		for (int plane = 0; plane < anInt437; plane++) {
			for (int tileX = 0; tileX < anInt438; tileX++) {
				for (int tileY = 0; tileY < anInt439; tileY++) {
					Ground ground = groundArray[plane][tileX][tileY];
					if (ground != null) {
						Object1 wall = ground.obj1;
						if (wall != null && wall.aClass30_Sub2_Sub4_278 != null && wall.aClass30_Sub2_Sub4_278.aClass33Array1425 != null) {
							mergeNeighbourNormals(plane, 1, 1, tileX, tileY, (Model) wall.aClass30_Sub2_Sub4_278);
							if (wall.aClass30_Sub2_Sub4_279 != null && wall.aClass30_Sub2_Sub4_279.aClass33Array1425 != null) {
								mergeNeighbourNormals(plane, 1, 1, tileX, tileY, (Model) wall.aClass30_Sub2_Sub4_279);
								mergeSharedVertexNormals((Model) wall.aClass30_Sub2_Sub4_278, (Model) wall.aClass30_Sub2_Sub4_279, 0, 0, 0, false);
								((Model) wall.aClass30_Sub2_Sub4_279).method480(ambientLight, lightContrast, lightX, lightY, lightZ);
							}
							((Model) wall.aClass30_Sub2_Sub4_278).method480(ambientLight, lightContrast, lightX, lightY, lightZ);
						}
						for (int objectIndex = 0; objectIndex < ground.anInt1317; objectIndex++) {
							Object5 sceneObject = ground.obj5Array[objectIndex];
							if (sceneObject != null && sceneObject.aClass30_Sub2_Sub4_521 != null && sceneObject.aClass30_Sub2_Sub4_521.aClass33Array1425 != null) {
								mergeNeighbourNormals(plane, sceneObject.anInt524 - sceneObject.anInt523 + 1, sceneObject.anInt526 - sceneObject.anInt525 + 1, tileX, tileY, (Model) sceneObject.aClass30_Sub2_Sub4_521);
								((Model) sceneObject.aClass30_Sub2_Sub4_521).method480(ambientLight, lightContrast, lightX, lightY, lightZ);
							}
						}

						Object3 groundDecoration = ground.obj3;
						if (groundDecoration != null && groundDecoration.aClass30_Sub2_Sub4_814.aClass33Array1425 != null) {
							mergeGroundDecorationNormals(tileX, plane, (Model) groundDecoration.aClass30_Sub2_Sub4_814, tileY);
							((Model) groundDecoration.aClass30_Sub2_Sub4_814).method480(ambientLight, lightContrast, lightX, lightY, lightZ);
						}
					}
				}

			}

		}

	}

	private void mergeGroundDecorationNormals(int tileX, int plane, Model model, int tileY) {
		if (tileX < anInt438) {
			Ground neighbourPlusX = groundArray[plane][tileX + 1][tileY];
			if (neighbourPlusX != null && neighbourPlusX.obj3 != null && neighbourPlusX.obj3.aClass30_Sub2_Sub4_814.aClass33Array1425 != null) {
				mergeSharedVertexNormals(model, (Model) neighbourPlusX.obj3.aClass30_Sub2_Sub4_814, 128, 0, 0, true);
			}
		}
		if (tileY < anInt438) {
			Ground neighbourPlusY = groundArray[plane][tileX][tileY + 1];
			if (neighbourPlusY != null && neighbourPlusY.obj3 != null && neighbourPlusY.obj3.aClass30_Sub2_Sub4_814.aClass33Array1425 != null) {
				mergeSharedVertexNormals(model, (Model) neighbourPlusY.obj3.aClass30_Sub2_Sub4_814, 0, 0, 128, true);
			}
		}
		if (tileX < anInt438 && tileY < anInt439) {
			Ground neighbourPlusXPlusY = groundArray[plane][tileX + 1][tileY + 1];
			if (neighbourPlusXPlusY != null && neighbourPlusXPlusY.obj3 != null && neighbourPlusXPlusY.obj3.aClass30_Sub2_Sub4_814.aClass33Array1425 != null) {
				mergeSharedVertexNormals(model, (Model) neighbourPlusXPlusY.obj3.aClass30_Sub2_Sub4_814, 128, 0, 128, true);
			}
		}
		if (tileX < anInt438 && tileY > 0) {
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
			if (scanPlane != anInt437) {
				for (int scanX = startX; scanX <= endX; scanX++) {
					if (scanX >= 0 && scanX < anInt438) {
						for (int scanY = startY; scanY <= endY; scanY++) {
							if (scanY >= 0 && scanY < anInt439 && (!onOwnPlane || scanX >= endX || scanY >= endY || scanY < tileY && scanX != tileX)) {
								Ground neighbour = groundArray[scanPlane][scanX][scanY];
								if (neighbour != null) {
									int heightOffset = (anIntArrayArrayArray440[scanPlane][scanX][scanY] + anIntArrayArrayArray440[scanPlane][scanX + 1][scanY] + anIntArrayArrayArray440[scanPlane][scanX][scanY + 1] + anIntArrayArrayArray440[scanPlane][scanX + 1][scanY + 1]) / 4 - (anIntArrayArrayArray440[plane][tileX][tileY] + anIntArrayArrayArray440[plane][tileX + 1][tileY] + anIntArrayArrayArray440[plane][tileX][tileY + 1] + anIntArrayArrayArray440[plane][tileX + 1][tileY + 1]) / 4;
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
				anInt458 = Model.modelIntArray1[pitch];
				anInt459 = Model.modelIntArray2[pitch];
				anInt460 = Model.modelIntArray1[yaw];
				anInt461 = Model.modelIntArray2[yaw];
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
		} else if (cameraX >= anInt438 * 128) {
			cameraX = anInt438 * 128 - 1;
		}
		if (cameraY < 0) {
			cameraY = 0;
		} else if (cameraY >= anInt439 * 128) {
			cameraY = anInt439 * 128 - 1;
		}
		anInt448++;
		anInt458 = Model.modelIntArray1[pitch];
		anInt459 = Model.modelIntArray2[pitch];
		anInt460 = Model.modelIntArray1[yaw];
		anInt461 = Model.modelIntArray2[yaw];
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
		if (anInt450 > anInt438) {
			anInt450 = anInt438;
		}
		anInt452 = anInt454 + drawDistance;
		if (anInt452 > anInt439) {
			anInt452 = anInt439;
		}
		method319();
		anInt446 = 0;
		for (int planeIndex = anInt442; planeIndex < anInt437; planeIndex++) {
			Ground planeTiles[][] = groundArray[planeIndex];
			for (int tileX = anInt449; tileX < anInt450; tileX++) {
				for (int tileY = anInt451; tileY < anInt452; tileY++) {
					Ground tile = planeTiles[tileX][tileY];
					if (tile != null) {
						if (tile.anInt1321 > plane || !cameraTileVisibility[tileX - anInt453 + drawDistance][tileY - anInt454 + drawDistance] && anIntArrayArrayArray440[planeIndex][tileX][tileY] - cameraZ < 50) {
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

		for (int planeIndex = anInt442; planeIndex < anInt437; planeIndex++) {
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

		for (int planeIndex = anInt442; planeIndex < anInt437; planeIndex++) {
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
			if (k < anInt437 - 1) {
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
		int l3 = anIntArrayArrayArray440[i][j1][k1] - anInt456;
		int i4 = anIntArrayArrayArray440[i][j1 + 1][k1] - anInt456;
		int j4 = anIntArrayArrayArray440[i][j1 + 1][k1 + 1] - anInt456;
		int k4 = anIntArrayArrayArray440[i][j1][k1 + 1] - anInt456;
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
		Texture.anInt1465 = 0;
		if ((i6 - k6) * (l5 - l6) - (j6 - l6) * (k5 - k6) > 0) {
			Texture.aBoolean1462 = i6 < 0 || k6 < 0 || k5 < 0 || i6 > DrawingArea.scanlineRightLimit || k6 > DrawingArea.scanlineRightLimit || k5 > DrawingArea.scanlineRightLimit;
			if (aBoolean467 && method318(anInt468, anInt469, j6, l6, l5, i6, k6, k5)) {
				anInt470 = j1;
				anInt471 = k1;
			}
			if (class43.anInt720 == -1) {
				if (class43.anInt718 != 0xbc614e) {
					Texture.method374(j6, l6, l5, i6, k6, k5, class43.anInt718, class43.anInt719, class43.anInt717);
				}
			} else if (!lowMem) {
				if (class43.aBoolean721) {
					Texture.method378(j6, l6, l5, i6, k6, k5, class43.anInt718, class43.anInt719, class43.anInt717, i2, i3, l1, l3, i4, k4, k2, j2, j3, class43.anInt720);
				} else {
					Texture.method378(j6, l6, l5, i6, k6, k5, class43.anInt718, class43.anInt719, class43.anInt717, l2, l1, i3, j4, k4, i4, k3, j3, j2, class43.anInt720);
				}
			} else {
				int i7 = anIntArray485[class43.anInt720];
				Texture.method374(j6, l6, l5, i6, k6, k5, method317(i7, class43.anInt718), method317(i7, class43.anInt719), method317(i7, class43.anInt717));
			}
		}
		if ((i5 - k5) * (l6 - l5) - (j5 - l5) * (k6 - k5) > 0) {
			Texture.aBoolean1462 = i5 < 0 || k5 < 0 || k6 < 0 || i5 > DrawingArea.scanlineRightLimit || k5 > DrawingArea.scanlineRightLimit || k6 > DrawingArea.scanlineRightLimit;
			if (aBoolean467 && method318(anInt468, anInt469, j5, l5, l6, i5, k5, k6)) {
				anInt470 = j1;
				anInt471 = k1;
			}
			if (class43.anInt720 == -1) {
				if (class43.anInt716 != 0xbc614e) {
					Texture.method374(j5, l5, l6, i5, k5, k6, class43.anInt716, class43.anInt717, class43.anInt719);
				}
			} else {
				if (!lowMem) {
					Texture.method378(j5, l5, l6, i5, k5, k6, class43.anInt716, class43.anInt717, class43.anInt719, i2, i3, l1, l3, i4, k4, k2, j2, j3, class43.anInt720);
					return;
				}
				int j7 = anIntArray485[class43.anInt720];
				Texture.method374(j5, l5, l6, i5, k5, k6, method317(j7, class43.anInt716), method317(j7, class43.anInt717), method317(j7, class43.anInt719));
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

		Texture.anInt1465 = 0;
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
				Texture.aBoolean1462 = i4 < 0 || j4 < 0 || k4 < 0 || i4 > DrawingArea.scanlineRightLimit || j4 > DrawingArea.scanlineRightLimit || k4 > DrawingArea.scanlineRightLimit;
				if (aBoolean467 && method318(anInt468, anInt469, l4, i5, j5, i4, j4, k4)) {
					anInt470 = i;
					anInt471 = i1;
				}
				if (class40.anIntArray682 == null || class40.anIntArray682[j2] == -1) {
					if (class40.anIntArray676[j2] != 0xbc614e) {
						Texture.method374(l4, i5, j5, i4, j4, k4, class40.anIntArray676[j2], class40.anIntArray677[j2], class40.anIntArray678[j2]);
					}
				} else if (!lowMem) {
					if (class40.aBoolean683) {
						Texture.method378(l4, i5, j5, i4, j4, k4, class40.anIntArray676[j2], class40.anIntArray677[j2], class40.anIntArray678[j2], Class40.anIntArray690[0], Class40.anIntArray690[1], Class40.anIntArray690[3], Class40.anIntArray691[0], Class40.anIntArray691[1], Class40.anIntArray691[3], Class40.anIntArray692[0], Class40.anIntArray692[1], Class40.anIntArray692[3], class40.anIntArray682[j2]);
					} else {
						Texture.method378(l4, i5, j5, i4, j4, k4, class40.anIntArray676[j2], class40.anIntArray677[j2], class40.anIntArray678[j2], Class40.anIntArray690[l2], Class40.anIntArray690[j3], Class40.anIntArray690[l3], Class40.anIntArray691[l2], Class40.anIntArray691[j3], Class40.anIntArray691[l3], Class40.anIntArray692[l2], Class40.anIntArray692[j3], Class40.anIntArray692[l3], class40.anIntArray682[j2]);
					}
				} else {
					int k5 = anIntArray485[class40.anIntArray682[j2]];
					Texture.method374(l4, i5, j5, i4, j4, k4, method317(k5, class40.anIntArray676[j2]), method317(k5, class40.anIntArray677[j2]), method317(k5, class40.anIntArray678[j2]));
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
		int j = anIntArray473[anInt447];
		Class47 aclass47[] = aClass47ArrayArray474[anInt447];
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
		if (method324(i1 + 1, anIntArrayArrayArray440[i][j][k], j1 + 1) && method324(i1 + 128 - 1, anIntArrayArrayArray440[i][j + 1][k], j1 + 1) && method324(i1 + 128 - 1, anIntArrayArrayArray440[i][j + 1][k + 1], j1 + 128 - 1) && method324(i1 + 1, anIntArrayArrayArray440[i][j][k + 1], j1 + 128 - 1)) {
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
		int k1 = anIntArrayArrayArray440[i][j][k] - 1;
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
		return method324(i1 + 1, anIntArrayArrayArray440[i][j][k] - l, j1 + 1) && method324(i1 + 128 - 1, anIntArrayArrayArray440[i][j + 1][k] - l, j1 + 1) && method324(i1 + 128 - 1, anIntArrayArrayArray440[i][j + 1][k + 1] - l, j1 + 128 - 1) && method324(i1 + 1, anIntArrayArrayArray440[i][j][k + 1] - l, j1 + 128 - 1);
	}

	private boolean method323(int i, int j, int k, int l, int i1, int j1) {
		if (j == k && l == i1) {
			if (!method320(i, j, l)) {
				return false;
			}
			int k1 = j << 7;
			int i2 = l << 7;
			return method324(k1 + 1, anIntArrayArrayArray440[i][j][l] - j1, i2 + 1) && method324(k1 + 128 - 1, anIntArrayArrayArray440[i][j + 1][l] - j1, i2 + 1) && method324(k1 + 128 - 1, anIntArrayArrayArray440[i][j + 1][l + 1] - j1, i2 + 128 - 1) && method324(k1 + 1, anIntArrayArrayArray440[i][j][l + 1] - j1, i2 + 128 - 1);
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
		int i3 = anIntArrayArrayArray440[i][j][l] - j1;
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
	private final int anInt437;
	private final int anInt438;
	private final int anInt439;
	private final int[][][] anIntArrayArrayArray440;
	private final Ground[][][] groundArray;
	private int anInt442;
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
	private static final int anInt472;
	private static int[] anIntArray473;
	private static Class47[][] aClass47ArrayArray474;
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
		anInt472 = 4;
		anIntArray473 = new int[anInt472];
		aClass47ArrayArray474 = new Class47[anInt472][500];
	}
}
