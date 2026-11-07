// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3)

final class ObjectManager {

	public ObjectManager(byte abyte0[][][], int ai[][][]) {
		anInt145 = 99;
		sizeX = 104;
		sizeY = 104;
		tileHeights = ai;
		tileFlags = abyte0;
		underlayIds = new byte[4][sizeX][sizeY];
		overlayIds = new byte[4][sizeX][sizeY];
		overlayShapes = new byte[4][sizeX][sizeY];
		overlayRotations = new byte[4][sizeX][sizeY];
		occlusionFlags = new int[4][sizeX + 1][sizeY + 1];
		objectShadow = new byte[4][sizeX + 1][sizeY + 1];
		tileLightLevels = new int[sizeX + 1][sizeY + 1];
		blendHueSum = new int[sizeY];
		blendSaturationSum = new int[sizeY];
		blendLightnessSum = new int[sizeY];
		blendHueWeightSum = new int[sizeY];
		blendTileCount = new int[sizeY];
	}

	private static int hashNoise(int x, int y) {
		int seed = x + y * 57;
		seed = seed << 13 ^ seed;
		int hash = seed * (seed * seed * 15731 + 0xc0ae5) + 0x5208dd0d & 0x7fffffff;
		return hash >> 19 & 0xff;
	}

	public final void buildTerrainScene(CollisionMap collisionMaps[], WorldController worldController) {
		for (int plane = 0; plane < 4; plane++) {
			for (int tileX = 0; tileX < 104; tileX++) {
				for (int tileY = 0; tileY < 104; tileY++) {
					if ((tileFlags[plane][tileX][tileY] & 1) == 1) {
						int collisionPlane = plane;
						if ((tileFlags[1][tileX][tileY] & 2) == 2) {
							collisionPlane--;
						}
						if (collisionPlane >= 0) {
							collisionMaps[collisionPlane].blockTile(tileY, tileX);
						}
					}
				}

			}

		}
		hueOffset += (int) (Math.random() * 5D) - 2;
		if (hueOffset < -8) {
			hueOffset = -8;
		}
		if (hueOffset > 8) {
			hueOffset = 8;
		}
		lightnessOffset += (int) (Math.random() * 5D) - 2;
		if (lightnessOffset < -16) {
			lightnessOffset = -16;
		}
		if (lightnessOffset > 16) {
			lightnessOffset = 16;
		}
		for (int plane = 0; plane < 4; plane++) {
			byte planeShadow[][] = objectShadow[plane];
			byte baseLight = 96;
			char contrastBase = '\u0300';
			byte lightDirX = -50;
			byte lightDirUp = -10;
			byte lightDirY = -50;
			int lightMagnitude = (int) Math.sqrt(lightDirX * lightDirX + lightDirUp * lightDirUp + lightDirY * lightDirY);
			int lightContrast = contrastBase * lightMagnitude >> 8;
			for (int tileY = 1; tileY < sizeY - 1; tileY++) {
				for (int tileX = 1; tileX < sizeX - 1; tileX++) {
					int heightSlopeX = tileHeights[plane][tileX + 1][tileY] - tileHeights[plane][tileX - 1][tileY];
					int heightSlopeY = tileHeights[plane][tileX][tileY + 1] - tileHeights[plane][tileX][tileY - 1];
					int normalLength = (int) Math.sqrt(heightSlopeX * heightSlopeX + 0x10000 + heightSlopeY * heightSlopeY);
					int normalX = (heightSlopeX << 8) / normalLength;
					int normalUp = 0x10000 / normalLength;
					int normalY = (heightSlopeY << 8) / normalLength;
					int directLight = baseLight + (lightDirX * normalX + lightDirUp * normalUp + lightDirY * normalY) / lightContrast;
					int shadowShade = (planeShadow[tileX - 1][tileY] >> 2) + (planeShadow[tileX + 1][tileY] >> 3) + (planeShadow[tileX][tileY - 1] >> 2) + (planeShadow[tileX][tileY + 1] >> 3) + (planeShadow[tileX][tileY] >> 1);
					tileLightLevels[tileX][tileY] = directLight - shadowShade;
				}

			}

			for (int column = 0; column < sizeY; column++) {
				blendHueSum[column] = 0;
				blendSaturationSum[column] = 0;
				blendLightnessSum[column] = 0;
				blendHueWeightSum[column] = 0;
				blendTileCount[column] = 0;
			}

			for (int tileX = -5; tileX < sizeX + 5; tileX++) {
				for (int tileY = 0; tileY < sizeY; tileY++) {
					int enterX = tileX + 5;
					if (enterX >= 0 && enterX < sizeX) {
						int enterUnderlayId = underlayIds[plane][enterX][tileY] & 0xff;
						if (enterUnderlayId > 0) {
							Flo enterFlo = Flo.cache[enterUnderlayId - 1];
							blendHueSum[tileY] += enterFlo.anInt397;
							blendSaturationSum[tileY] += enterFlo.anInt395;
							blendLightnessSum[tileY] += enterFlo.anInt396;
							blendHueWeightSum[tileY] += enterFlo.anInt398;
							blendTileCount[tileY]++;
						}
					}
					int exitX = tileX - 5;
					if (exitX >= 0 && exitX < sizeX) {
						int exitUnderlayId = underlayIds[plane][exitX][tileY] & 0xff;
						if (exitUnderlayId > 0) {
							Flo exitFlo = Flo.cache[exitUnderlayId - 1];
							blendHueSum[tileY] -= exitFlo.anInt397;
							blendSaturationSum[tileY] -= exitFlo.anInt395;
							blendLightnessSum[tileY] -= exitFlo.anInt396;
							blendHueWeightSum[tileY] -= exitFlo.anInt398;
							blendTileCount[tileY]--;
						}
					}
				}

				if (tileX >= 1 && tileX < sizeX - 1) {
					int windowHueSum = 0;
					int windowSaturationSum = 0;
					int windowLightnessSum = 0;
					int windowHueWeightSum = 0;
					int windowTileCount = 0;
					for (int tileY = -5; tileY < sizeY + 5; tileY++) {
						int enterY = tileY + 5;
						if (enterY >= 0 && enterY < sizeY) {
							windowHueSum += blendHueSum[enterY];
							windowSaturationSum += blendSaturationSum[enterY];
							windowLightnessSum += blendLightnessSum[enterY];
							windowHueWeightSum += blendHueWeightSum[enterY];
							windowTileCount += blendTileCount[enterY];
						}
						int exitY = tileY - 5;
						if (exitY >= 0 && exitY < sizeY) {
							windowHueSum -= blendHueSum[exitY];
							windowSaturationSum -= blendSaturationSum[exitY];
							windowLightnessSum -= blendLightnessSum[exitY];
							windowHueWeightSum -= blendHueWeightSum[exitY];
							windowTileCount -= blendTileCount[exitY];
						}
						if (tileY >= 1 && tileY < sizeY - 1 && ((!hideBuggyVarrockSwordShopSnow && !hideRoofs && !lowMem) || (tileFlags[0][tileX][tileY] & 2) != 0 || (tileFlags[plane][tileX][tileY] & 0x10) == 0 && getEffectivePlane(tileY, plane, tileX) == anInt131)) {
							if (plane < anInt145) {
								anInt145 = plane;
							}
							int underlayId = underlayIds[plane][tileX][tileY] & 0xff;
							int overlayId = overlayIds[plane][tileX][tileY] & 0xff;
							if (underlayId > 0 || overlayId > 0) {
								int heightX0Y0 = tileHeights[plane][tileX][tileY];
								int heightX1Y0 = tileHeights[plane][tileX + 1][tileY];
								int heightX1Y1 = tileHeights[plane][tileX + 1][tileY + 1];
								int heightX0Y1 = tileHeights[plane][tileX][tileY + 1];
								int lightX0Y0 = tileLightLevels[tileX][tileY];
								int lightX1Y0 = tileLightLevels[tileX + 1][tileY];
								int lightX1Y1 = tileLightLevels[tileX + 1][tileY + 1];
								int lightX0Y1 = tileLightLevels[tileX][tileY + 1];
								int underlayHsl = -1;
								int jitteredHsl = -1;
								if (underlayId > 0) {
									int hue = windowHueSum * 256 / windowHueWeightSum;
									int saturation = windowSaturationSum / windowTileCount;
									int lightness = windowLightnessSum / windowTileCount;
									underlayHsl = packHsl(hue, saturation, lightness);
									hue = hue + hueOffset & 0xff;
									lightness += lightnessOffset;
									if (lightness < 0) {
										lightness = 0;
									} else if (lightness > 255) {
										lightness = 255;
									}
									jitteredHsl = packHsl(hue, saturation, lightness);
								}
								if (plane > 0) {
									boolean canOcclude = true;
									if (underlayId == 0 && overlayShapes[plane][tileX][tileY] != 0) {
										canOcclude = false;
									}
									if (overlayId > 0 && !Flo.cache[overlayId - 1].aBoolean393) {
										canOcclude = false;
									}
									if (canOcclude && heightX0Y0 == heightX1Y0 && heightX0Y0 == heightX1Y1 && heightX0Y0 == heightX0Y1) {
										occlusionFlags[plane][tileX][tileY] |= 0x924;
									}
								}
								int underlayMapColour = 0;
								if (underlayHsl != -1) {
									underlayMapColour = Texture.hslToRgbPalette[shadeHsl(jitteredHsl, 96)];
								}
								if (overlayId == 0) {
									worldController.addTile(plane, tileX, tileY, 0, 0, -1, heightX0Y0, heightX1Y0, heightX1Y1, heightX0Y1, shadeHsl(underlayHsl, lightX0Y0), shadeHsl(underlayHsl, lightX1Y0), shadeHsl(underlayHsl, lightX1Y1), shadeHsl(underlayHsl, lightX0Y1), 0, 0, 0, 0, underlayMapColour, 0);
								} else {
									int shapeId = overlayShapes[plane][tileX][tileY] + 1;
									byte overlayRotation = overlayRotations[plane][tileX][tileY];
									Flo overlayFlo = Flo.cache[overlayId - 1];
									int textureId = overlayFlo.anInt391;
									int overlayHsl;
									int overlayMapColour;
									if (textureId >= 0) {
										overlayMapColour = Texture.getAverageColor(textureId);
										overlayHsl = -1;
									} else if (overlayFlo.anInt390 == 0xff00ff) {
										/*overlayHsl = -2;
										textureId = -1;
										overlayMapColour = Texture.hslToRgbPalette[shadeHslOrGrey(overlayFlo.anInt399, 96)];*/
										overlayMapColour = 0;
										overlayHsl = -2;
										textureId = -1;
									} else if(overlayFlo.anInt390 == 0x333333) {
										overlayMapColour = Texture.hslToRgbPalette[shadeHslOrGrey(overlayFlo.anInt399, 96)];
										overlayHsl = -2;
										textureId = -1;
									} else {
										overlayHsl = packHsl(overlayFlo.anInt394, overlayFlo.anInt395, overlayFlo.anInt396);
										overlayMapColour = Texture.hslToRgbPalette[shadeHslOrGrey(overlayFlo.anInt399, 96)];
									}
									worldController.addTile(plane, tileX, tileY, shapeId, overlayRotation, textureId, heightX0Y0, heightX1Y0, heightX1Y1, heightX0Y1, shadeHsl(underlayHsl, lightX0Y0), shadeHsl(underlayHsl, lightX1Y0), shadeHsl(underlayHsl, lightX1Y1), shadeHsl(underlayHsl, lightX0Y1), shadeHslOrGrey(overlayHsl, lightX0Y0), shadeHslOrGrey(overlayHsl, lightX1Y0), shadeHslOrGrey(overlayHsl, lightX1Y1), shadeHslOrGrey(overlayHsl, lightX0Y1), underlayMapColour, overlayMapColour);
								}
							}
						}
					}

				}
			}

			for (int tileY = 1; tileY < sizeY - 1; tileY++) {
				for (int tileX = 1; tileX < sizeX - 1; tileX++) {
					worldController.setMinCameraPlane(plane, tileX, tileY, getEffectivePlane(tileY, plane, tileX));
				}

			}

		}

		worldController.lightScene(-10, -50, -50);
		for (int tileX = 0; tileX < sizeX; tileX++) {
			for (int tileY = 0; tileY < sizeY; tileY++) {
				if ((tileFlags[1][tileX][tileY] & 2) == 2) {
					worldController.lowerColumnPlanes(tileY, tileX);
				}
			}

		}

		int xWallBit = 1;
		int yWallBit = 2;
		int roofBit = 4;
		for (int maxPlane = 0; maxPlane < 4; maxPlane++) {
			if (maxPlane > 0) {
				xWallBit <<= 3;
				yWallBit <<= 3;
				roofBit <<= 3;
			}
			for (int startPlane = 0; startPlane <= maxPlane; startPlane++) {
				for (int tileY = 0; tileY <= sizeY; tileY++) {
					for (int tileX = 0; tileX <= sizeX; tileX++) {
						if ((occlusionFlags[startPlane][tileX][tileY] & xWallBit) != 0) {
							int wallStartY = tileY;
							int wallEndY = tileY;
							int wallLowPlane = startPlane;
							int wallHighPlane = startPlane;
							for (; wallStartY > 0 && (occlusionFlags[startPlane][tileX][wallStartY - 1] & xWallBit) != 0; wallStartY--) {
								;
							}
							for (; wallEndY < sizeY && (occlusionFlags[startPlane][tileX][wallEndY + 1] & xWallBit) != 0; wallEndY++) {
								;
							}
							label0 : for (; wallLowPlane > 0; wallLowPlane--) {
								for (int checkY = wallStartY; checkY <= wallEndY; checkY++) {
									if ((occlusionFlags[wallLowPlane - 1][tileX][checkY] & xWallBit) == 0) {
										break label0;
									}
								}

							}

							label1 : for (; wallHighPlane < maxPlane; wallHighPlane++) {
								for (int checkY = wallStartY; checkY <= wallEndY; checkY++) {
									if ((occlusionFlags[wallHighPlane + 1][tileX][checkY] & xWallBit) == 0) {
										break label1;
									}
								}

							}

							int wallArea = (wallHighPlane + 1 - wallLowPlane) * (wallEndY - wallStartY + 1);
							if (wallArea >= 8) {
								char wallHeight = '\360';
								int wallTopHeight = tileHeights[wallHighPlane][tileX][wallStartY] - wallHeight;
								int wallBottomHeight = tileHeights[wallLowPlane][tileX][wallStartY];
								WorldController.addOccluder(maxPlane, tileX * 128, wallBottomHeight, tileX * 128, wallEndY * 128 + 128, wallTopHeight, wallStartY * 128, 1);
								for (int clearPlane = wallLowPlane; clearPlane <= wallHighPlane; clearPlane++) {
									for (int clearY = wallStartY; clearY <= wallEndY; clearY++) {
										occlusionFlags[clearPlane][tileX][clearY] &= ~xWallBit;
									}

								}

							}
						}
						if ((occlusionFlags[startPlane][tileX][tileY] & yWallBit) != 0) {
							int wallStartX = tileX;
							int wallEndX = tileX;
							int wallLowPlane = startPlane;
							int wallHighPlane = startPlane;
							for (; wallStartX > 0 && (occlusionFlags[startPlane][wallStartX - 1][tileY] & yWallBit) != 0; wallStartX--) {
								;
							}
							for (; wallEndX < sizeX && (occlusionFlags[startPlane][wallEndX + 1][tileY] & yWallBit) != 0; wallEndX++) {
								;
							}
							label2 : for (; wallLowPlane > 0; wallLowPlane--) {
								for (int checkX = wallStartX; checkX <= wallEndX; checkX++) {
									if ((occlusionFlags[wallLowPlane - 1][checkX][tileY] & yWallBit) == 0) {
										break label2;
									}
								}

							}

							label3 : for (; wallHighPlane < maxPlane; wallHighPlane++) {
								for (int checkX = wallStartX; checkX <= wallEndX; checkX++) {
									if ((occlusionFlags[wallHighPlane + 1][checkX][tileY] & yWallBit) == 0) {
										break label3;
									}
								}

							}

							int wallArea = (wallHighPlane + 1 - wallLowPlane) * (wallEndX - wallStartX + 1);
							if (wallArea >= 8) {
								char wallHeight = '\360';
								int wallTopHeight = tileHeights[wallHighPlane][wallStartX][tileY] - wallHeight;
								int wallBottomHeight = tileHeights[wallLowPlane][wallStartX][tileY];
								WorldController.addOccluder(maxPlane, wallStartX * 128, wallBottomHeight, wallEndX * 128 + 128, tileY * 128, wallTopHeight, tileY * 128, 2);
								for (int clearPlane = wallLowPlane; clearPlane <= wallHighPlane; clearPlane++) {
									for (int clearX = wallStartX; clearX <= wallEndX; clearX++) {
										occlusionFlags[clearPlane][clearX][tileY] &= ~yWallBit;
									}

								}

							}
						}
						if ((occlusionFlags[startPlane][tileX][tileY] & roofBit) != 0) {
							int roofStartX = tileX;
							int roofEndX = tileX;
							int roofStartY = tileY;
							int roofEndY = tileY;
							for (; roofStartY > 0 && (occlusionFlags[startPlane][tileX][roofStartY - 1] & roofBit) != 0; roofStartY--) {
								;
							}
							for (; roofEndY < sizeY && (occlusionFlags[startPlane][tileX][roofEndY + 1] & roofBit) != 0; roofEndY++) {
								;
							}
							label4 : for (; roofStartX > 0; roofStartX--) {
								for (int checkY = roofStartY; checkY <= roofEndY; checkY++) {
									if ((occlusionFlags[startPlane][roofStartX - 1][checkY] & roofBit) == 0) {
										break label4;
									}
								}

							}

							label5 : for (; roofEndX < sizeX; roofEndX++) {
								for (int checkY = roofStartY; checkY <= roofEndY; checkY++) {
									if ((occlusionFlags[startPlane][roofEndX + 1][checkY] & roofBit) == 0) {
										break label5;
									}
								}

							}

							if ((roofEndX - roofStartX + 1) * (roofEndY - roofStartY + 1) >= 4) {
								int roofHeight = tileHeights[startPlane][roofStartX][roofStartY];
								WorldController.addOccluder(maxPlane, roofStartX * 128, roofHeight, roofEndX * 128 + 128, roofEndY * 128 + 128, roofHeight, roofStartY * 128, 4);
								for (int clearX = roofStartX; clearX <= roofEndX; clearX++) {
									for (int clearY = roofStartY; clearY <= roofEndY; clearY++) {
										occlusionFlags[startPlane][clearX][clearY] &= ~roofBit;
									}

								}

							}
						}
					}

				}

			}

		}

	}

	private static int proceduralHeight(int x, int y) {
		int height = interpolatedNoise(x + 45365, y + 0x16713, 4) - 128 + (interpolatedNoise(x + 10294, y + 37821, 2) - 128 >> 1) + (interpolatedNoise(x, y, 1) - 128 >> 2);
		height = (int) (height * 0.29999999999999999D) + 35;
		if (height < 10) {
			height = 10;
		} else if (height > 60) {
			height = 60;
		}
		return height;
	}

	public static void requestObjectModels(Stream objectData, OnDemandFetcher fetcher) {
		label0 : {
			int objectId = -1;
			do {
				int objectIdDelta = objectData.readUnsignedSmart();
				if (objectIdDelta == 0) {
					break label0;
				}
				objectId += objectIdDelta;
				ObjectDef objectDef = ObjectDef.forID(objectId);
				objectDef.requestModels(fetcher);
				do {
					int positionDelta = objectData.readUnsignedSmart();
					if (positionDelta == 0) {
						break;
					}
					objectData.readUnsignedByte();
				} while (true);
			} while (true);
		}
	}

	public final void blankRegionArea(int startY, int spanY, int spanX, int startX) {
		for (int tileY = startY; tileY <= startY + spanY; tileY++) {
			for (int tileX = startX; tileX <= startX + spanX; tileX++) {
				if (tileX >= 0 && tileX < sizeX && tileY >= 0 && tileY < sizeY) {
					objectShadow[0][tileX][tileY] = 127;
					if (tileX == startX && tileX > 0) {
						tileHeights[0][tileX][tileY] = tileHeights[0][tileX - 1][tileY];
					}
					if (tileX == startX + spanX && tileX < sizeX - 1) {
						tileHeights[0][tileX][tileY] = tileHeights[0][tileX + 1][tileY];
					}
					if (tileY == startY && tileY > 0) {
						tileHeights[0][tileX][tileY] = tileHeights[0][tileX][tileY - 1];
					}
					if (tileY == startY + spanY && tileY < sizeY - 1) {
						tileHeights[0][tileX][tileY] = tileHeights[0][tileX][tileY + 1];
					}
				}
			}

		}
	}

	private void placeObject(int tileY, WorldController worldController, CollisionMap collisionMap, int shape, int plane, int tileX, int objectId, int orientation) {
		if ((hideRoofs || lowMem) && (tileFlags[0][tileX][tileY] & 2) == 0) {
			if ((tileFlags[plane][tileX][tileY] & 0x10) != 0) {
				return;
			}
			if (getEffectivePlane(tileY, plane, tileX) != anInt131) {
				return;
			}
		}
		if (plane < anInt145) {
			anInt145 = plane;
		}
		int heightX0Y0 = tileHeights[plane][tileX][tileY];
		int heightX1Y0 = tileHeights[plane][tileX + 1][tileY];
		int heightX1Y1 = tileHeights[plane][tileX + 1][tileY + 1];
		int heightX0Y1 = tileHeights[plane][tileX][tileY + 1];
		int averageHeight = heightX0Y0 + heightX1Y0 + heightX1Y1 + heightX0Y1 >> 2;
		ObjectDef objectDef = ObjectDef.forID(objectId);
		int uid = tileX + (tileY << 7) + (objectId << 14) + 0x40000000;
		if (!objectDef.hasActions) {
			uid += 0x80000000;
		}
		byte shapeAndOrientation = (byte) ((orientation << 6) + shape);
		if (shape == 22) {
			if (lowMem && !objectDef.hasActions && !objectDef.showInLowMemory) {
				return;
			}
			Object obj;
			if (objectDef.animationId == -1 && objectDef.childrenIDs == null) {
				obj = objectDef.getModel(22, orientation, heightX0Y0, heightX1Y0, heightX1Y1, heightX0Y1, -1);
			} else {
				obj = new Animable_Sub5(objectId, orientation, 22, heightX1Y0, heightX1Y1, heightX0Y0, heightX0Y1, objectDef.animationId, true);
			}
			worldController.addGroundDecoration(plane, averageHeight, tileY, ((Animable) obj), shapeAndOrientation, uid, tileX);
			if (objectDef.blocksMovement && objectDef.hasActions && collisionMap != null) {
				collisionMap.blockTile(tileY, tileX);
			}
			return;
		}
		if (shape == 10 || shape == 11) {
			Object obj1;
			if (objectDef.animationId == -1 && objectDef.childrenIDs == null) {
				obj1 = objectDef.getModel(10, orientation, heightX0Y0, heightX1Y0, heightX1Y1, heightX0Y1, -1);
			} else {
				obj1 = new Animable_Sub5(objectId, orientation, 10, heightX1Y0, heightX1Y1, heightX0Y0, heightX0Y1, objectDef.animationId, true);
			}
			if (obj1 != null) {
				int i5 = 0;
				if (shape == 11) {
					i5 += 256;
				}
				int footprintX;
				int footprintY;
				if (orientation == 1 || orientation == 3) {
					footprintX = objectDef.sizeY;
					footprintY = objectDef.sizeX;
				} else {
					footprintX = objectDef.sizeX;
					footprintY = objectDef.sizeY;
				}
				if (worldController.addSceneObject(uid, shapeAndOrientation, averageHeight, footprintY, ((Animable) obj1), footprintX, plane, i5, tileY, tileX) && objectDef.castsShadow) {
					Model model;
					if (obj1 instanceof Model) {
						model = (Model) obj1;
					} else {
						model = objectDef.getModel(10, orientation, heightX0Y0, heightX1Y0, heightX1Y1, heightX0Y1, -1);
					}
					if (model != null) {
						for (int shadowOffsetX = 0; shadowOffsetX <= footprintX; shadowOffsetX++) {
							for (int shadowOffsetY = 0; shadowOffsetY <= footprintY; shadowOffsetY++) {
								int shadowStrength = model.radiusXZ / 4;
								if (shadowStrength > 30) {
									shadowStrength = 30;
								}
								if (shadowStrength > objectShadow[plane][tileX + shadowOffsetX][tileY + shadowOffsetY]) {
									objectShadow[plane][tileX + shadowOffsetX][tileY + shadowOffsetY] = (byte) shadowStrength;
								}
							}

						}

					}
				}
			}
			if (objectDef.blocksMovement && collisionMap != null) {
				collisionMap.addObject(objectDef.blocksProjectiles, objectDef.sizeX, objectDef.sizeY, tileX, tileY, orientation);
			}
			return;
		}
		if (shape >= 12) {
			Object obj2;
			if (objectDef.animationId == -1 && objectDef.childrenIDs == null) {
				obj2 = objectDef.getModel(shape, orientation, heightX0Y0, heightX1Y0, heightX1Y1, heightX0Y1, -1);
			} else {
				obj2 = new Animable_Sub5(objectId, orientation, shape, heightX1Y0, heightX1Y1, heightX0Y0, heightX0Y1, objectDef.animationId, true);
			}
			worldController.addSceneObject(uid, shapeAndOrientation, averageHeight, 1, ((Animable) obj2), 1, plane, 0, tileY, tileX);
			if (shape >= 12 && shape <= 17 && shape != 13 && plane > 0) {
				occlusionFlags[plane][tileX][tileY] |= 0x924;
			}
			if (objectDef.blocksMovement && collisionMap != null) {
				collisionMap.addObject(objectDef.blocksProjectiles, objectDef.sizeX, objectDef.sizeY, tileX, tileY, orientation);
			}
			return;
		}
		if (shape == 0) {
			Object obj3;
			if (objectDef.animationId == -1 && objectDef.childrenIDs == null) {
				obj3 = objectDef.getModel(0, orientation, heightX0Y0, heightX1Y0, heightX1Y1, heightX0Y1, -1);
			} else {
				obj3 = new Animable_Sub5(objectId, orientation, 0, heightX1Y0, heightX1Y1, heightX0Y0, heightX0Y1, objectDef.animationId, true);
			}
			worldController.addWall(anIntArray152[orientation], ((Animable) obj3), uid, tileY, shapeAndOrientation, tileX, null, averageHeight, 0, plane);
			if (orientation == 0) {
				if (objectDef.castsShadow) {
					objectShadow[plane][tileX][tileY] = 50;
					objectShadow[plane][tileX][tileY + 1] = 50;
				}
				if (objectDef.occludes) {
					occlusionFlags[plane][tileX][tileY] |= 0x249;
				}
			} else if (orientation == 1) {
				if (objectDef.castsShadow) {
					objectShadow[plane][tileX][tileY + 1] = 50;
					objectShadow[plane][tileX + 1][tileY + 1] = 50;
				}
				if (objectDef.occludes) {
					occlusionFlags[plane][tileX][tileY + 1] |= 0x492;
				}
			} else if (orientation == 2) {
				if (objectDef.castsShadow) {
					objectShadow[plane][tileX + 1][tileY] = 50;
					objectShadow[plane][tileX + 1][tileY + 1] = 50;
				}
				if (objectDef.occludes) {
					occlusionFlags[plane][tileX + 1][tileY] |= 0x249;
				}
			} else if (orientation == 3) {
				if (objectDef.castsShadow) {
					objectShadow[plane][tileX][tileY] = 50;
					objectShadow[plane][tileX + 1][tileY] = 50;
				}
				if (objectDef.occludes) {
					occlusionFlags[plane][tileX][tileY] |= 0x492;
				}
			}
			if (objectDef.blocksMovement && collisionMap != null) {
				collisionMap.addWall(tileY, orientation, tileX, shape, objectDef.blocksProjectiles);
			}
			if (objectDef.wallDecorationOffset != 16) {
				worldController.scaleWallDecorationOffset(tileY, objectDef.wallDecorationOffset, tileX, plane);
			}
			return;
		}
		if (shape == 1) {
			Object obj4;
			if (objectDef.animationId == -1 && objectDef.childrenIDs == null) {
				obj4 = objectDef.getModel(1, orientation, heightX0Y0, heightX1Y0, heightX1Y1, heightX0Y1, -1);
			} else {
				obj4 = new Animable_Sub5(objectId, orientation, 1, heightX1Y0, heightX1Y1, heightX0Y0, heightX0Y1, objectDef.animationId, true);
			}
			worldController.addWall(anIntArray140[orientation], ((Animable) obj4), uid, tileY, shapeAndOrientation, tileX, null, averageHeight, 0, plane);
			if (objectDef.castsShadow) {
				if (orientation == 0) {
					objectShadow[plane][tileX][tileY + 1] = 50;
				} else if (orientation == 1) {
					objectShadow[plane][tileX + 1][tileY + 1] = 50;
				} else if (orientation == 2) {
					objectShadow[plane][tileX + 1][tileY] = 50;
				} else if (orientation == 3) {
					objectShadow[plane][tileX][tileY] = 50;
				}
			}
			if (objectDef.blocksMovement && collisionMap != null) {
				collisionMap.addWall(tileY, orientation, tileX, shape, objectDef.blocksProjectiles);
			}
			return;
		}
		if (shape == 2) {
			int i3 = orientation + 1 & 3;
			Object obj11;
			Object obj12;
			if (objectDef.animationId == -1 && objectDef.childrenIDs == null) {
				obj11 = objectDef.getModel(2, 4 + orientation, heightX0Y0, heightX1Y0, heightX1Y1, heightX0Y1, -1);
				obj12 = objectDef.getModel(2, i3, heightX0Y0, heightX1Y0, heightX1Y1, heightX0Y1, -1);
			} else {
				obj11 = new Animable_Sub5(objectId, 4 + orientation, 2, heightX1Y0, heightX1Y1, heightX0Y0, heightX0Y1, objectDef.animationId, true);
				obj12 = new Animable_Sub5(objectId, i3, 2, heightX1Y0, heightX1Y1, heightX0Y0, heightX0Y1, objectDef.animationId, true);
			}
			worldController.addWall(anIntArray152[orientation], ((Animable) obj11), uid, tileY, shapeAndOrientation, tileX, ((Animable) obj12), averageHeight, anIntArray152[i3], plane);
			if (objectDef.occludes) {
				if (orientation == 0) {
					occlusionFlags[plane][tileX][tileY] |= 0x249;
					occlusionFlags[plane][tileX][tileY + 1] |= 0x492;
				} else if (orientation == 1) {
					occlusionFlags[plane][tileX][tileY + 1] |= 0x492;
					occlusionFlags[plane][tileX + 1][tileY] |= 0x249;
				} else if (orientation == 2) {
					occlusionFlags[plane][tileX + 1][tileY] |= 0x249;
					occlusionFlags[plane][tileX][tileY] |= 0x492;
				} else if (orientation == 3) {
					occlusionFlags[plane][tileX][tileY] |= 0x492;
					occlusionFlags[plane][tileX][tileY] |= 0x249;
				}
			}
			if (objectDef.blocksMovement && collisionMap != null) {
				collisionMap.addWall(tileY, orientation, tileX, shape, objectDef.blocksProjectiles);
			}
			if (objectDef.wallDecorationOffset != 16) {
				worldController.scaleWallDecorationOffset(tileY, objectDef.wallDecorationOffset, tileX, plane);
			}
			return;
		}
		if (shape == 3) {
			Object obj5;
			if (objectDef.animationId == -1 && objectDef.childrenIDs == null) {
				obj5 = objectDef.getModel(3, orientation, heightX0Y0, heightX1Y0, heightX1Y1, heightX0Y1, -1);
			} else {
				obj5 = new Animable_Sub5(objectId, orientation, 3, heightX1Y0, heightX1Y1, heightX0Y0, heightX0Y1, objectDef.animationId, true);
			}
			worldController.addWall(anIntArray140[orientation], ((Animable) obj5), uid, tileY, shapeAndOrientation, tileX, null, averageHeight, 0, plane);
			if (objectDef.castsShadow) {
				if (orientation == 0) {
					objectShadow[plane][tileX][tileY + 1] = 50;
				} else if (orientation == 1) {
					objectShadow[plane][tileX + 1][tileY + 1] = 50;
				} else if (orientation == 2) {
					objectShadow[plane][tileX + 1][tileY] = 50;
				} else if (orientation == 3) {
					objectShadow[plane][tileX][tileY] = 50;
				}
			}
			if (objectDef.blocksMovement && collisionMap != null) {
				collisionMap.addWall(tileY, orientation, tileX, shape, objectDef.blocksProjectiles);
			}
			return;
		}
		if (shape == 9) {
			Object obj6;
			if (objectDef.animationId == -1 && objectDef.childrenIDs == null) {
				obj6 = objectDef.getModel(shape, orientation, heightX0Y0, heightX1Y0, heightX1Y1, heightX0Y1, -1);
			} else {
				obj6 = new Animable_Sub5(objectId, orientation, shape, heightX1Y0, heightX1Y1, heightX0Y0, heightX0Y1, objectDef.animationId, true);
			}
			worldController.addSceneObject(uid, shapeAndOrientation, averageHeight, 1, ((Animable) obj6), 1, plane, 0, tileY, tileX);
			if (objectDef.blocksMovement && collisionMap != null) {
				collisionMap.addObject(objectDef.blocksProjectiles, objectDef.sizeX, objectDef.sizeY, tileX, tileY, orientation);
			}
			return;
		}
		if (objectDef.adjustsToTerrain) {
			if (orientation == 1) {
				int j3 = heightX0Y1;
				heightX0Y1 = heightX1Y1;
				heightX1Y1 = heightX1Y0;
				heightX1Y0 = heightX0Y0;
				heightX0Y0 = j3;
			} else if (orientation == 2) {
				int k3 = heightX0Y1;
				heightX0Y1 = heightX1Y0;
				heightX1Y0 = k3;
				k3 = heightX1Y1;
				heightX1Y1 = heightX0Y0;
				heightX0Y0 = k3;
			} else if (orientation == 3) {
				int l3 = heightX0Y1;
				heightX0Y1 = heightX0Y0;
				heightX0Y0 = heightX1Y0;
				heightX1Y0 = heightX1Y1;
				heightX1Y1 = l3;
			}
		}
		if (shape == 4) {
			Object obj7;
			if (objectDef.animationId == -1 && objectDef.childrenIDs == null) {
				obj7 = objectDef.getModel(4, 0, heightX0Y0, heightX1Y0, heightX1Y1, heightX0Y1, -1);
			} else {
				obj7 = new Animable_Sub5(objectId, 0, 4, heightX1Y0, heightX1Y1, heightX0Y0, heightX0Y1, objectDef.animationId, true);
			}
			worldController.addWallDecoration(uid, tileY, orientation * 512, plane, 0, averageHeight, ((Animable) obj7), tileX, shapeAndOrientation, 0, anIntArray152[orientation]);
			return;
		}
		if (shape == 5) {
			int i4 = 16;
			int k4 = worldController.getWallUid(plane, tileX, tileY);
			if (k4 > 0) {
				i4 = ObjectDef.forID(k4 >> 14 & 0x7fff).wallDecorationOffset;
			}
			Object obj13;
			if (objectDef.animationId == -1 && objectDef.childrenIDs == null) {
				obj13 = objectDef.getModel(4, 0, heightX0Y0, heightX1Y0, heightX1Y1, heightX0Y1, -1);
			} else {
				obj13 = new Animable_Sub5(objectId, 0, 4, heightX1Y0, heightX1Y1, heightX0Y0, heightX0Y1, objectDef.animationId, true);
			}
			worldController.addWallDecoration(uid, tileY, orientation * 512, plane, anIntArray137[orientation] * i4, averageHeight, ((Animable) obj13), tileX, shapeAndOrientation, anIntArray144[orientation] * i4, anIntArray152[orientation]);
			return;
		}
		if (shape == 6) {
			Object obj8;
			if (objectDef.animationId == -1 && objectDef.childrenIDs == null) {
				obj8 = objectDef.getModel(4, 0, heightX0Y0, heightX1Y0, heightX1Y1, heightX0Y1, -1);
			} else {
				obj8 = new Animable_Sub5(objectId, 0, 4, heightX1Y0, heightX1Y1, heightX0Y0, heightX0Y1, objectDef.animationId, true);
			}
			worldController.addWallDecoration(uid, tileY, orientation, plane, 0, averageHeight, ((Animable) obj8), tileX, shapeAndOrientation, 0, 256);
			return;
		}
		if (shape == 7) {
			Object obj9;
			if (objectDef.animationId == -1 && objectDef.childrenIDs == null) {
				obj9 = objectDef.getModel(4, 0, heightX0Y0, heightX1Y0, heightX1Y1, heightX0Y1, -1);
			} else {
				obj9 = new Animable_Sub5(objectId, 0, 4, heightX1Y0, heightX1Y1, heightX0Y0, heightX0Y1, objectDef.animationId, true);
			}
			worldController.addWallDecoration(uid, tileY, orientation, plane, 0, averageHeight, ((Animable) obj9), tileX, shapeAndOrientation, 0, 512);
			return;
		}
		if (shape == 8) {
			Object obj10;
			if (objectDef.animationId == -1 && objectDef.childrenIDs == null) {
				obj10 = objectDef.getModel(4, 0, heightX0Y0, heightX1Y0, heightX1Y1, heightX0Y1, -1);
			} else {
				obj10 = new Animable_Sub5(objectId, 0, 4, heightX1Y0, heightX1Y1, heightX0Y0, heightX0Y1, objectDef.animationId, true);
			}
			worldController.addWallDecoration(uid, tileY, orientation, plane, 0, averageHeight, ((Animable) obj10), tileX, shapeAndOrientation, 0, 768);
		}
	}

	private static int interpolatedNoise(int x, int y, int period) {
		int cellX = x / period;
		int offsetX = x & period - 1;
		int cellY = y / period;
		int offsetY = y & period - 1;
		int noiseX0Y0 = smoothedNoise(cellX, cellY);
		int noiseX1Y0 = smoothedNoise(cellX + 1, cellY);
		int noiseX0Y1 = smoothedNoise(cellX, cellY + 1);
		int noiseX1Y1 = smoothedNoise(cellX + 1, cellY + 1);
		int rowY0 = cosineInterpolate(noiseX0Y0, noiseX1Y0, offsetX, period);
		int rowY1 = cosineInterpolate(noiseX0Y1, noiseX1Y1, offsetX, period);
		return cosineInterpolate(rowY0, rowY1, offsetY, period);
	}

	private int packHsl(int hue, int saturation, int lightness) {
		if (lightness > 179) {
			saturation /= 2;
		}
		if (lightness > 192) {
			saturation /= 2;
		}
		if (lightness > 217) {
			saturation /= 2;
		}
		if (lightness > 243) {
			saturation /= 2;
		}
		return (hue / 4 << 10) + (saturation / 32 << 7) + lightness / 2;
	}

	public static boolean areObjectModelsLoaded(int objectId, int shape) {
		ObjectDef objectDef = ObjectDef.forID(objectId);
		if (shape == 11) {
			shape = 10;
		}
		if (shape >= 5 && shape <= 8) {
			shape = 4;
		}
		return objectDef.areModelsLoaded(shape);
	}

	public final void loadTerrainChunk(int sourcePlane, int rotation, CollisionMap collisionMaps[], int destX, int sourceX, byte data[], int sourceY, int destPlane, int destY) {
		for (int offsetX = 0; offsetX < 8; offsetX++) {
			for (int offsetY = 0; offsetY < 8; offsetY++) {
				if (destX + offsetX > 0 && destX + offsetX < 103 && destY + offsetY > 0 && destY + offsetY < 103) {
					collisionMaps[destPlane].tileFlags[destX + offsetX][destY + offsetY] &= 0xfeffffff;
				}
			}

		}
		Stream stream = new Stream(data);
		for (int plane = 0; plane < 4; plane++) {
			for (int regionX = 0; regionX < 64; regionX++) {
				for (int regionY = 0; regionY < 64; regionY++) {
					if (plane == sourcePlane && regionX >= sourceX && regionX < sourceX + 8 && regionY >= sourceY && regionY < sourceY + 8) {
						readTerrainTile(destY + TileRotation.rotatedY(regionY & 7, rotation, regionX & 7), 0, stream, destX + TileRotation.rotatedX(rotation, regionY & 7, regionX & 7), destPlane, rotation, 0);
					} else {
						readTerrainTile(-1, 0, stream, -1, 0, 0, 0);
					}
				}

			}

		}

	}

	public final void loadTerrainRegion(byte data[], int baseY, int baseX, int noiseOffsetX, int noiseOffsetY, CollisionMap collisionMaps[]) {
		for (int plane = 0; plane < 4; plane++) {
			for (int offsetX = 0; offsetX < 64; offsetX++) {
				for (int offsetY = 0; offsetY < 64; offsetY++) {
					if (baseX + offsetX > 0 && baseX + offsetX < 103 && baseY + offsetY > 0 && baseY + offsetY < 103) {
						collisionMaps[plane].tileFlags[baseX + offsetX][baseY + offsetY] &= 0xfeffffff;
					}
				}

			}

		}

		Stream stream = new Stream(data);
		for (int plane = 0; plane < 4; plane++) {
			for (int offsetX = 0; offsetX < 64; offsetX++) {
				for (int offsetY = 0; offsetY < 64; offsetY++) {
					readTerrainTile(offsetY + baseY, noiseOffsetY, stream, offsetX + baseX, plane, 0, noiseOffsetX);
				}

			}

		}
	}

	private void readTerrainTile(int tileY, int noiseOffsetY, Stream stream, int tileX, int plane, int rotation, int noiseOffsetX) {
		if (tileX >= 0 && tileX < 104 && tileY >= 0 && tileY < 104) {
			tileFlags[plane][tileX][tileY] = 0;
			do {
				int opcode = stream.readUnsignedByte();
				if (opcode == 0) {
					if (plane == 0) {
						tileHeights[0][tileX][tileY] = -proceduralHeight(0xe3b7b + tileX + noiseOffsetX, 0x87cce + tileY + noiseOffsetY) * 8;
						return;
					} else {
						tileHeights[plane][tileX][tileY] = tileHeights[plane - 1][tileX][tileY] - 240;
						return;
					}
				}
				if (opcode == 1) {
					int explicitHeight = stream.readUnsignedByte();
					if (explicitHeight == 1) {
						explicitHeight = 0;
					}
					if (plane == 0) {
						tileHeights[0][tileX][tileY] = -explicitHeight * 8;
						return;
					} else {
						tileHeights[plane][tileX][tileY] = tileHeights[plane - 1][tileX][tileY] - explicitHeight * 8;
						return;
					}
				}
				if (opcode <= 49) {
					overlayIds[plane][tileX][tileY] = stream.readSignedByte();
					overlayShapes[plane][tileX][tileY] = (byte) ((opcode - 2) / 4);
					overlayRotations[plane][tileX][tileY] = (byte) (opcode - 2 + rotation & 3);
				} else if (opcode <= 81) {
					tileFlags[plane][tileX][tileY] = (byte) (opcode - 49);
				} else {
					underlayIds[plane][tileX][tileY] = (byte) (opcode - 81);
				}
			} while (true);
		}
		do {
			int skippedOpcode = stream.readUnsignedByte();
			if (skippedOpcode == 0) {
				break;
			}
			if (skippedOpcode == 1) {
				stream.readUnsignedByte();
				return;
			}
			if (skippedOpcode <= 49) {
				stream.readUnsignedByte();
			}
		} while (true);
	}

	private int getEffectivePlane(int tileY, int plane, int tileX) {
		if ((tileFlags[plane][tileX][tileY] & 8) != 0) {
			return 0;
		}
		if (plane > 0 && (tileFlags[1][tileX][tileY] & 2) != 0) {
			return plane - 1;
		} else {
			return plane;
		}
	}

	public final void loadObjectChunk(CollisionMap collisionMaps[], WorldController worldController, int sourcePlane, int destX, int sourceY, int destPlane, byte data[], int sourceX, int rotation, int destY) {
		label0 : {
			Stream stream = new Stream(data);
			int objectId = -1;
			do {
				int objectIdDelta = stream.readUnsignedSmart();
				if (objectIdDelta == 0) {
					break label0;
				}
				objectId += objectIdDelta;
				int packedPosition = 0;
				do {
					int positionDelta = stream.readUnsignedSmart();
					if (positionDelta == 0) {
						break;
					}
					packedPosition += positionDelta - 1;
					int localY = packedPosition & 0x3f;
					int localX = packedPosition >> 6 & 0x3f;
					int objectPlane = packedPosition >> 12;
					int attribute = stream.readUnsignedByte();
					int shape = attribute >> 2;
					int objectOrientation = attribute & 3;
					if (objectPlane == sourcePlane && localX >= sourceX && localX < sourceX + 8 && localY >= sourceY && localY < sourceY + 8) {
						ObjectDef objectDef = ObjectDef.forID(objectId);
						int destTileX = destX + TileRotation.rotatedFootprintMinX(rotation, objectDef.sizeY, localX & 7, localY & 7, objectDef.sizeX);
						int destTileY = destY + TileRotation.rotatedFootprintMinY(localY & 7, objectDef.sizeY, rotation, objectDef.sizeX, localX & 7);
						if (destTileX > 0 && destTileY > 0 && destTileX < 103 && destTileY < 103) {
							int collisionPlane = objectPlane;
							if ((tileFlags[1][destTileX][destTileY] & 2) == 2) {
								collisionPlane--;
							}
							CollisionMap collisionMap = null;
							if (collisionPlane >= 0) {
								collisionMap = collisionMaps[collisionPlane];
							}
							placeObject(destTileY, worldController, collisionMap, shape, destPlane, destTileX, objectId, objectOrientation + rotation & 3);
						}
					}
				} while (true);
			} while (true);
		}
	}

	private static int cosineInterpolate(int from, int to, int position, int period) {
		int blend = 0x10000 - Texture.cosineTable[position * 1024 / period] >> 1;
		return (from * (0x10000 - blend) >> 16) + (to * blend >> 16);
	}

	private int shadeHslOrGrey(int hsl, int lightLevel) {
		if (hsl == -2) {
			return 0xbc614e;
		}
		if (hsl == -1) {
			if (lightLevel < 0) {
				lightLevel = 0;
			} else if (lightLevel > 127) {
				lightLevel = 127;
			}
			lightLevel = 127 - lightLevel;
			return lightLevel;
		}
		lightLevel = lightLevel * (hsl & 0x7f) / 128;
		if (lightLevel < 2) {
			lightLevel = 2;
		} else if (lightLevel > 126) {
			lightLevel = 126;
		}
		return (hsl & 0xff80) + lightLevel;
	}

	private static int smoothedNoise(int x, int y) {
		int cornerSum = hashNoise(x - 1, y - 1) + hashNoise(x + 1, y - 1) + hashNoise(x - 1, y + 1) + hashNoise(x + 1, y + 1);
		int edgeSum = hashNoise(x - 1, y) + hashNoise(x + 1, y) + hashNoise(x, y - 1) + hashNoise(x, y + 1);
		int centre = hashNoise(x, y);
		return cornerSum / 16 + edgeSum / 8 + centre / 4;
	}

	private static int shadeHsl(int hsl, int lightLevel) {
		if (hsl == -1) {
			return 0xbc614e;
		}
		lightLevel = lightLevel * (hsl & 0x7f) / 128;
		if (lightLevel < 2) {
			lightLevel = 2;
		} else if (lightLevel > 126) {
			lightLevel = 126;
		}
		return (hsl & 0xff80) + lightLevel;
	}

	public static void placeObjectWithoutShading(WorldController worldController, int orientation, int tileY, int shape, int heightPlane, CollisionMap collisionMap, int groundHeights[][][], int tileX, int objectId, int plane) {
		int heightX0Y0 = groundHeights[heightPlane][tileX][tileY];
		int heightX1Y0 = groundHeights[heightPlane][tileX + 1][tileY];
		int heightX1Y1 = groundHeights[heightPlane][tileX + 1][tileY + 1];
		int heightX0Y1 = groundHeights[heightPlane][tileX][tileY + 1];
		int averageHeight = heightX0Y0 + heightX1Y0 + heightX1Y1 + heightX0Y1 >> 2;
		ObjectDef objectDef = ObjectDef.forID(objectId);
		int uid = tileX + (tileY << 7) + (objectId << 14) + 0x40000000;
		if (!objectDef.hasActions) {
			uid += 0x80000000;
		}
		byte shapeAndOrientation = (byte) ((orientation << 6) + shape);
		if (shape == 22) {
			Object obj;
			if (objectDef.animationId == -1 && objectDef.childrenIDs == null) {
				obj = objectDef.getModel(22, orientation, heightX0Y0, heightX1Y0, heightX1Y1, heightX0Y1, -1);
			} else {
				obj = new Animable_Sub5(objectId, orientation, 22, heightX1Y0, heightX1Y1, heightX0Y0, heightX0Y1, objectDef.animationId, true);
			}
			worldController.addGroundDecoration(plane, averageHeight, tileY, ((Animable) obj), shapeAndOrientation, uid, tileX);
			if (objectDef.blocksMovement && objectDef.hasActions) {
				collisionMap.blockTile(tileY, tileX);
			}
			return;
		}
		if (shape == 10 || shape == 11) {
			Object obj1;
			if (objectDef.animationId == -1 && objectDef.childrenIDs == null) {
				obj1 = objectDef.getModel(10, orientation, heightX0Y0, heightX1Y0, heightX1Y1, heightX0Y1, -1);
			} else {
				obj1 = new Animable_Sub5(objectId, orientation, 10, heightX1Y0, heightX1Y1, heightX0Y0, heightX0Y1, objectDef.animationId, true);
			}
			if (obj1 != null) {
				int j5 = 0;
				if (shape == 11) {
					j5 += 256;
				}
				int k4;
				int i5;
				if (orientation == 1 || orientation == 3) {
					k4 = objectDef.sizeY;
					i5 = objectDef.sizeX;
				} else {
					k4 = objectDef.sizeX;
					i5 = objectDef.sizeY;
				}
				worldController.addSceneObject(uid, shapeAndOrientation, averageHeight, i5, ((Animable) obj1), k4, plane, j5, tileY, tileX);
			}
			if (objectDef.blocksMovement) {
				collisionMap.addObject(objectDef.blocksProjectiles, objectDef.sizeX, objectDef.sizeY, tileX, tileY, orientation);
			}
			return;
		}
		if (shape >= 12) {
			Object obj2;
			if (objectDef.animationId == -1 && objectDef.childrenIDs == null) {
				obj2 = objectDef.getModel(shape, orientation, heightX0Y0, heightX1Y0, heightX1Y1, heightX0Y1, -1);
			} else {
				obj2 = new Animable_Sub5(objectId, orientation, shape, heightX1Y0, heightX1Y1, heightX0Y0, heightX0Y1, objectDef.animationId, true);
			}
			worldController.addSceneObject(uid, shapeAndOrientation, averageHeight, 1, ((Animable) obj2), 1, plane, 0, tileY, tileX);
			if (objectDef.blocksMovement) {
				collisionMap.addObject(objectDef.blocksProjectiles, objectDef.sizeX, objectDef.sizeY, tileX, tileY, orientation);
			}
			return;
		}
		if (shape == 0) {
			Object obj3;
			if (objectDef.animationId == -1 && objectDef.childrenIDs == null) {
				obj3 = objectDef.getModel(0, orientation, heightX0Y0, heightX1Y0, heightX1Y1, heightX0Y1, -1);
			} else {
				obj3 = new Animable_Sub5(objectId, orientation, 0, heightX1Y0, heightX1Y1, heightX0Y0, heightX0Y1, objectDef.animationId, true);
			}
			worldController.addWall(anIntArray152[orientation], ((Animable) obj3), uid, tileY, shapeAndOrientation, tileX, null, averageHeight, 0, plane);
			if (objectDef.blocksMovement) {
				collisionMap.addWall(tileY, orientation, tileX, shape, objectDef.blocksProjectiles);
			}
			return;
		}
		if (shape == 1) {
			Object obj4;
			if (objectDef.animationId == -1 && objectDef.childrenIDs == null) {
				obj4 = objectDef.getModel(1, orientation, heightX0Y0, heightX1Y0, heightX1Y1, heightX0Y1, -1);
			} else {
				obj4 = new Animable_Sub5(objectId, orientation, 1, heightX1Y0, heightX1Y1, heightX0Y0, heightX0Y1, objectDef.animationId, true);
			}
			worldController.addWall(anIntArray140[orientation], ((Animable) obj4), uid, tileY, shapeAndOrientation, tileX, null, averageHeight, 0, plane);
			if (objectDef.blocksMovement) {
				collisionMap.addWall(tileY, orientation, tileX, shape, objectDef.blocksProjectiles);
			}
			return;
		}
		if (shape == 2) {
			int j3 = orientation + 1 & 3;
			Object obj11;
			Object obj12;
			if (objectDef.animationId == -1 && objectDef.childrenIDs == null) {
				obj11 = objectDef.getModel(2, 4 + orientation, heightX0Y0, heightX1Y0, heightX1Y1, heightX0Y1, -1);
				obj12 = objectDef.getModel(2, j3, heightX0Y0, heightX1Y0, heightX1Y1, heightX0Y1, -1);
			} else {
				obj11 = new Animable_Sub5(objectId, 4 + orientation, 2, heightX1Y0, heightX1Y1, heightX0Y0, heightX0Y1, objectDef.animationId, true);
				obj12 = new Animable_Sub5(objectId, j3, 2, heightX1Y0, heightX1Y1, heightX0Y0, heightX0Y1, objectDef.animationId, true);
			}
			worldController.addWall(anIntArray152[orientation], ((Animable) obj11), uid, tileY, shapeAndOrientation, tileX, ((Animable) obj12), averageHeight, anIntArray152[j3], plane);
			if (objectDef.blocksMovement) {
				collisionMap.addWall(tileY, orientation, tileX, shape, objectDef.blocksProjectiles);
			}
			return;
		}
		if (shape == 3) {
			Object obj5;
			if (objectDef.animationId == -1 && objectDef.childrenIDs == null) {
				obj5 = objectDef.getModel(3, orientation, heightX0Y0, heightX1Y0, heightX1Y1, heightX0Y1, -1);
			} else {
				obj5 = new Animable_Sub5(objectId, orientation, 3, heightX1Y0, heightX1Y1, heightX0Y0, heightX0Y1, objectDef.animationId, true);
			}
			worldController.addWall(anIntArray140[orientation], ((Animable) obj5), uid, tileY, shapeAndOrientation, tileX, null, averageHeight, 0, plane);
			if (objectDef.blocksMovement) {
				collisionMap.addWall(tileY, orientation, tileX, shape, objectDef.blocksProjectiles);
			}
			return;
		}
		if (shape == 9) {
			Object obj6;
			if (objectDef.animationId == -1 && objectDef.childrenIDs == null) {
				obj6 = objectDef.getModel(shape, orientation, heightX0Y0, heightX1Y0, heightX1Y1, heightX0Y1, -1);
			} else {
				obj6 = new Animable_Sub5(objectId, orientation, shape, heightX1Y0, heightX1Y1, heightX0Y0, heightX0Y1, objectDef.animationId, true);
			}
			worldController.addSceneObject(uid, shapeAndOrientation, averageHeight, 1, ((Animable) obj6), 1, plane, 0, tileY, tileX);
			if (objectDef.blocksMovement) {
				collisionMap.addObject(objectDef.blocksProjectiles, objectDef.sizeX, objectDef.sizeY, tileX, tileY, orientation);
			}
			return;
		}
		if (objectDef.adjustsToTerrain) {
			if (orientation == 1) {
				int k3 = heightX0Y1;
				heightX0Y1 = heightX1Y1;
				heightX1Y1 = heightX1Y0;
				heightX1Y0 = heightX0Y0;
				heightX0Y0 = k3;
			} else if (orientation == 2) {
				int l3 = heightX0Y1;
				heightX0Y1 = heightX1Y0;
				heightX1Y0 = l3;
				l3 = heightX1Y1;
				heightX1Y1 = heightX0Y0;
				heightX0Y0 = l3;
			} else if (orientation == 3) {
				int i4 = heightX0Y1;
				heightX0Y1 = heightX0Y0;
				heightX0Y0 = heightX1Y0;
				heightX1Y0 = heightX1Y1;
				heightX1Y1 = i4;
			}
		}
		if (shape == 4) {
			Object obj7;
			if (objectDef.animationId == -1 && objectDef.childrenIDs == null) {
				obj7 = objectDef.getModel(4, 0, heightX0Y0, heightX1Y0, heightX1Y1, heightX0Y1, -1);
			} else {
				obj7 = new Animable_Sub5(objectId, 0, 4, heightX1Y0, heightX1Y1, heightX0Y0, heightX0Y1, objectDef.animationId, true);
			}
			worldController.addWallDecoration(uid, tileY, orientation * 512, plane, 0, averageHeight, ((Animable) obj7), tileX, shapeAndOrientation, 0, anIntArray152[orientation]);
			return;
		}
		if (shape == 5) {
			int j4 = 16;
			int l4 = worldController.getWallUid(plane, tileX, tileY);
			if (l4 > 0) {
				j4 = ObjectDef.forID(l4 >> 14 & 0x7fff).wallDecorationOffset;
			}
			Object obj13;
			if (objectDef.animationId == -1 && objectDef.childrenIDs == null) {
				obj13 = objectDef.getModel(4, 0, heightX0Y0, heightX1Y0, heightX1Y1, heightX0Y1, -1);
			} else {
				obj13 = new Animable_Sub5(objectId, 0, 4, heightX1Y0, heightX1Y1, heightX0Y0, heightX0Y1, objectDef.animationId, true);
			}
			worldController.addWallDecoration(uid, tileY, orientation * 512, plane, anIntArray137[orientation] * j4, averageHeight, ((Animable) obj13), tileX, shapeAndOrientation, anIntArray144[orientation] * j4, anIntArray152[orientation]);
			return;
		}
		if (shape == 6) {
			Object obj8;
			if (objectDef.animationId == -1 && objectDef.childrenIDs == null) {
				obj8 = objectDef.getModel(4, 0, heightX0Y0, heightX1Y0, heightX1Y1, heightX0Y1, -1);
			} else {
				obj8 = new Animable_Sub5(objectId, 0, 4, heightX1Y0, heightX1Y1, heightX0Y0, heightX0Y1, objectDef.animationId, true);
			}
			worldController.addWallDecoration(uid, tileY, orientation, plane, 0, averageHeight, ((Animable) obj8), tileX, shapeAndOrientation, 0, 256);
			return;
		}
		if (shape == 7) {
			Object obj9;
			if (objectDef.animationId == -1 && objectDef.childrenIDs == null) {
				obj9 = objectDef.getModel(4, 0, heightX0Y0, heightX1Y0, heightX1Y1, heightX0Y1, -1);
			} else {
				obj9 = new Animable_Sub5(objectId, 0, 4, heightX1Y0, heightX1Y1, heightX0Y0, heightX0Y1, objectDef.animationId, true);
			}
			worldController.addWallDecoration(uid, tileY, orientation, plane, 0, averageHeight, ((Animable) obj9), tileX, shapeAndOrientation, 0, 512);
			return;
		}
		if (shape == 8) {
			Object obj10;
			if (objectDef.animationId == -1 && objectDef.childrenIDs == null) {
				obj10 = objectDef.getModel(4, 0, heightX0Y0, heightX1Y0, heightX1Y1, heightX0Y1, -1);
			} else {
				obj10 = new Animable_Sub5(objectId, 0, 4, heightX1Y0, heightX1Y1, heightX0Y0, heightX0Y1, objectDef.animationId, true);
			}
			worldController.addWallDecoration(uid, tileY, orientation, plane, 0, averageHeight, ((Animable) obj10), tileX, shapeAndOrientation, 0, 768);
		}
	}

	public static boolean method189(int i, byte[] is, int i_250_) // xxx bad
																	// method,
																	// decompiled
																	// with JODE
	{
		boolean bool = true;
		Stream stream = new Stream(is);
		int i_252_ = -1;
		for (;;) {
			int i_253_ = stream.readUnsignedSmart();
			if (i_253_ == 0) {
				break;
			}
			i_252_ += i_253_;
			int i_254_ = 0;
			boolean bool_255_ = false;
			for (;;) {
				if (bool_255_) {
					int i_256_ = stream.readUnsignedSmart();
					if (i_256_ == 0) {
						break;
					}
					stream.readUnsignedByte();
				} else {
					int i_257_ = stream.readUnsignedSmart();
					if (i_257_ == 0) {
						break;
					}
					i_254_ += i_257_ - 1;
					int i_258_ = i_254_ & 0x3f;
					int i_259_ = i_254_ >> 6 & 0x3f;
					int i_260_ = stream.readUnsignedByte() >> 2;
					int i_261_ = i_259_ + i;
					int i_262_ = i_258_ + i_250_;
					if (i_261_ > 0 && i_262_ > 0 && i_261_ < 103 && i_262_ < 103) {
						ObjectDef class46 = ObjectDef.forID(i_252_);
						if (i_260_ != 22 || !lowMem || class46.hasActions || class46.showInLowMemory) {
							bool &= class46.areAllModelsLoaded();
							bool_255_ = true;
						}
					}
				}
			}
		}
		return bool;
	}

	public final void loadObjects(int baseX, CollisionMap collisionMaps[], int baseY, WorldController worldController, byte objectData[]) {
		label0 : {
			Stream stream = new Stream(objectData);
			int objectId = -1;
			do {
				int idDelta = stream.readUnsignedSmart();
				if (idDelta == 0) {
					break label0;
				}
				objectId += idDelta;
				int packedPosition = 0;
				do {
					int positionDelta = stream.readUnsignedSmart();
					if (positionDelta == 0) {
						break;
					}
					packedPosition += positionDelta - 1;
					int localY = packedPosition & 0x3f;
					int localX = packedPosition >> 6 & 0x3f;
					int plane = packedPosition >> 12;
					int typeAndOrientation = stream.readUnsignedByte();
					int shape = typeAndOrientation >> 2;
					int orientation = typeAndOrientation & 3;
					int tileX = localX + baseX;
					int tileY = localY + baseY;
					if (tileX > 0 && tileY > 0 && tileX < 103 && tileY < 103) {
						int collisionPlane = plane;
						if ((tileFlags[1][tileX][tileY] & 2) == 2) {
							collisionPlane--;
						}
						CollisionMap collisionMap = null;
						if (collisionPlane >= 0) {
							collisionMap = collisionMaps[collisionPlane];
						}
						placeObject(tileY, worldController, collisionMap, shape, plane, tileX, objectId, orientation);
					}
				} while (true);
			} while (true);
		}
	}

	private static int hueOffset = (int) (Math.random() * 17D) - 8;
	private final int[] blendHueSum;
	private final int[] blendSaturationSum;
	private final int[] blendLightnessSum;
	private final int[] blendHueWeightSum;
	private final int[] blendTileCount;
	private final int[][][] tileHeights;
	private final byte[][][] overlayIds;
	static int anInt131;
	private static int lightnessOffset = (int) (Math.random() * 33D) - 16;
	private final byte[][][] objectShadow;
	private final int[][][] occlusionFlags;
	private final byte[][][] overlayShapes;
	private static final int anIntArray137[] = {1, 0, -1, 0};
	private final int[][] tileLightLevels;
	private static final int anIntArray140[] = {16, 32, 64, 128};
	private final byte[][][] underlayIds;
	private static final int anIntArray144[] = {0, -1, 0, 1};
	static int anInt145 = 99;
	private final int sizeX;
	private final int sizeY;
	private final byte[][][] overlayRotations;
	private final byte[][][] tileFlags;
	static boolean lowMem = true;
	
	static boolean hideRoofs = ClientSettings.HIDE_ROOFS;
	
	static boolean hideBuggyVarrockSwordShopSnow = ClientSettings.HIDE_BUGGY_VARROCK_SWORD_SHOP_SNOW; //There is some buggy snow near Varrock sword shop, let's hide it. I know, this is not ideal, but it does work.
	private static final int anIntArray152[] = {1, 2, 4, 8};

}
