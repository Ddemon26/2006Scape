// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

final class CollisionMap {

	public CollisionMap() {
		originX = 0;
		originY = 0;
		width = 104;
		height = 104;
		tileFlags = new int[width][height];
		reset();
	}

	public void reset() {
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (x == 0 || y == 0 || x == width - 1 || y == height - 1) {
					tileFlags[x][y] = 0xffffff;
				} else {
					tileFlags[x][y] = 0x1000000;
				}
			}

		}

	}

	public void addWall(int tileY, int orientation, int tileX, int shape, boolean blocksProjectiles) {
		tileX -= originX;
		tileY -= originY;
		if (shape == 0) {
			if (orientation == 0) {
				addFlags(tileX, tileY, 128);
				addFlags(tileX - 1, tileY, 8);
			}
			if (orientation == 1) {
				addFlags(tileX, tileY, 2);
				addFlags(tileX, tileY + 1, 32);
			}
			if (orientation == 2) {
				addFlags(tileX, tileY, 8);
				addFlags(tileX + 1, tileY, 128);
			}
			if (orientation == 3) {
				addFlags(tileX, tileY, 32);
				addFlags(tileX, tileY - 1, 2);
			}
		}
		if (shape == 1 || shape == 3) {
			if (orientation == 0) {
				addFlags(tileX, tileY, 1);
				addFlags(tileX - 1, tileY + 1, 16);
			}
			if (orientation == 1) {
				addFlags(tileX, tileY, 4);
				addFlags(tileX + 1, tileY + 1, 64);
			}
			if (orientation == 2) {
				addFlags(tileX, tileY, 16);
				addFlags(tileX + 1, tileY - 1, 1);
			}
			if (orientation == 3) {
				addFlags(tileX, tileY, 64);
				addFlags(tileX - 1, tileY - 1, 4);
			}
		}
		if (shape == 2) {
			if (orientation == 0) {
				addFlags(tileX, tileY, 130);
				addFlags(tileX - 1, tileY, 8);
				addFlags(tileX, tileY + 1, 32);
			}
			if (orientation == 1) {
				addFlags(tileX, tileY, 10);
				addFlags(tileX, tileY + 1, 32);
				addFlags(tileX + 1, tileY, 128);
			}
			if (orientation == 2) {
				addFlags(tileX, tileY, 40);
				addFlags(tileX + 1, tileY, 128);
				addFlags(tileX, tileY - 1, 2);
			}
			if (orientation == 3) {
				addFlags(tileX, tileY, 160);
				addFlags(tileX, tileY - 1, 2);
				addFlags(tileX - 1, tileY, 8);
			}
		}
		if (blocksProjectiles) {
			if (shape == 0) {
				if (orientation == 0) {
					addFlags(tileX, tileY, 0x10000);
					addFlags(tileX - 1, tileY, 4096);
				}
				if (orientation == 1) {
					addFlags(tileX, tileY, 1024);
					addFlags(tileX, tileY + 1, 16384);
				}
				if (orientation == 2) {
					addFlags(tileX, tileY, 4096);
					addFlags(tileX + 1, tileY, 0x10000);
				}
				if (orientation == 3) {
					addFlags(tileX, tileY, 16384);
					addFlags(tileX, tileY - 1, 1024);
				}
			}
			if (shape == 1 || shape == 3) {
				if (orientation == 0) {
					addFlags(tileX, tileY, 512);
					addFlags(tileX - 1, tileY + 1, 8192);
				}
				if (orientation == 1) {
					addFlags(tileX, tileY, 2048);
					addFlags(tileX + 1, tileY + 1, 32768);
				}
				if (orientation == 2) {
					addFlags(tileX, tileY, 8192);
					addFlags(tileX + 1, tileY - 1, 512);
				}
				if (orientation == 3) {
					addFlags(tileX, tileY, 32768);
					addFlags(tileX - 1, tileY - 1, 2048);
				}
			}
			if (shape == 2) {
				if (orientation == 0) {
					addFlags(tileX, tileY, 0x10400);
					addFlags(tileX - 1, tileY, 4096);
					addFlags(tileX, tileY + 1, 16384);
				}
				if (orientation == 1) {
					addFlags(tileX, tileY, 5120);
					addFlags(tileX, tileY + 1, 16384);
					addFlags(tileX + 1, tileY, 0x10000);
				}
				if (orientation == 2) {
					addFlags(tileX, tileY, 20480);
					addFlags(tileX + 1, tileY, 0x10000);
					addFlags(tileX, tileY - 1, 1024);
				}
				if (orientation == 3) {
					addFlags(tileX, tileY, 0x14000);
					addFlags(tileX, tileY - 1, 1024);
					addFlags(tileX - 1, tileY, 4096);
				}
			}
		}
	}

	public void addObject(boolean blocksProjectiles, int sizeX, int sizeY, int tileX, int tileY, int orientation) {
		int blockMask = 256;
		if (blocksProjectiles) {
			blockMask += 0x20000;
		}
		tileX -= originX;
		tileY -= originY;
		if (orientation == 1 || orientation == 3) {
			int previousSizeX = sizeX;
			sizeX = sizeY;
			sizeY = previousSizeX;
		}
		for (int x = tileX; x < tileX + sizeX; x++) {
			if (x >= 0 && x < width) {
				for (int y = tileY; y < tileY + sizeY; y++) {
					if (y >= 0 && y < height) {
						addFlags(x, y, blockMask);
					}
				}

			}
		}

	}

	public void blockTile(int tileY, int tileX) {
		tileX -= originX;
		tileY -= originY;
		tileFlags[tileX][tileY] |= 0x200000;
	}

	private void addFlags(int tileX, int tileY, int mask) {
		tileFlags[tileX][tileY] |= mask;
	}

	public void removeWall(int orientation, int shape, boolean blocksProjectiles, int tileX, int tileY) {
		tileX -= originX;
		tileY -= originY;
		if (shape == 0) {
			if (orientation == 0) {
				clearFlags(128, tileX, tileY);
				clearFlags(8, tileX - 1, tileY);
			}
			if (orientation == 1) {
				clearFlags(2, tileX, tileY);
				clearFlags(32, tileX, tileY + 1);
			}
			if (orientation == 2) {
				clearFlags(8, tileX, tileY);
				clearFlags(128, tileX + 1, tileY);
			}
			if (orientation == 3) {
				clearFlags(32, tileX, tileY);
				clearFlags(2, tileX, tileY - 1);
			}
		}
		if (shape == 1 || shape == 3) {
			if (orientation == 0) {
				clearFlags(1, tileX, tileY);
				clearFlags(16, tileX - 1, tileY + 1);
			}
			if (orientation == 1) {
				clearFlags(4, tileX, tileY);
				clearFlags(64, tileX + 1, tileY + 1);
			}
			if (orientation == 2) {
				clearFlags(16, tileX, tileY);
				clearFlags(1, tileX + 1, tileY - 1);
			}
			if (orientation == 3) {
				clearFlags(64, tileX, tileY);
				clearFlags(4, tileX - 1, tileY - 1);
			}
		}
		if (shape == 2) {
			if (orientation == 0) {
				clearFlags(130, tileX, tileY);
				clearFlags(8, tileX - 1, tileY);
				clearFlags(32, tileX, tileY + 1);
			}
			if (orientation == 1) {
				clearFlags(10, tileX, tileY);
				clearFlags(32, tileX, tileY + 1);
				clearFlags(128, tileX + 1, tileY);
			}
			if (orientation == 2) {
				clearFlags(40, tileX, tileY);
				clearFlags(128, tileX + 1, tileY);
				clearFlags(2, tileX, tileY - 1);
			}
			if (orientation == 3) {
				clearFlags(160, tileX, tileY);
				clearFlags(2, tileX, tileY - 1);
				clearFlags(8, tileX - 1, tileY);
			}
		}
		if (blocksProjectiles) {
			if (shape == 0) {
				if (orientation == 0) {
					clearFlags(0x10000, tileX, tileY);
					clearFlags(4096, tileX - 1, tileY);
				}
				if (orientation == 1) {
					clearFlags(1024, tileX, tileY);
					clearFlags(16384, tileX, tileY + 1);
				}
				if (orientation == 2) {
					clearFlags(4096, tileX, tileY);
					clearFlags(0x10000, tileX + 1, tileY);
				}
				if (orientation == 3) {
					clearFlags(16384, tileX, tileY);
					clearFlags(1024, tileX, tileY - 1);
				}
			}
			if (shape == 1 || shape == 3) {
				if (orientation == 0) {
					clearFlags(512, tileX, tileY);
					clearFlags(8192, tileX - 1, tileY + 1);
				}
				if (orientation == 1) {
					clearFlags(2048, tileX, tileY);
					clearFlags(32768, tileX + 1, tileY + 1);
				}
				if (orientation == 2) {
					clearFlags(8192, tileX, tileY);
					clearFlags(512, tileX + 1, tileY - 1);
				}
				if (orientation == 3) {
					clearFlags(32768, tileX, tileY);
					clearFlags(2048, tileX - 1, tileY - 1);
				}
			}
			if (shape == 2) {
				if (orientation == 0) {
					clearFlags(0x10400, tileX, tileY);
					clearFlags(4096, tileX - 1, tileY);
					clearFlags(16384, tileX, tileY + 1);
				}
				if (orientation == 1) {
					clearFlags(5120, tileX, tileY);
					clearFlags(16384, tileX, tileY + 1);
					clearFlags(0x10000, tileX + 1, tileY);
				}
				if (orientation == 2) {
					clearFlags(20480, tileX, tileY);
					clearFlags(0x10000, tileX + 1, tileY);
					clearFlags(1024, tileX, tileY - 1);
				}
				if (orientation == 3) {
					clearFlags(0x14000, tileX, tileY);
					clearFlags(1024, tileX, tileY - 1);
					clearFlags(4096, tileX - 1, tileY);
				}
			}
		}
	}

	public void removeObject(int orientation, int sizeX, int tileX, int tileY, int sizeY, boolean blocksProjectiles) {
		int blockMask = 256;
		if (blocksProjectiles) {
			blockMask += 0x20000;
		}
		tileX -= originX;
		tileY -= originY;
		if (orientation == 1 || orientation == 3) {
			int previousSizeX = sizeX;
			sizeX = sizeY;
			sizeY = previousSizeX;
		}
		for (int x = tileX; x < tileX + sizeX; x++) {
			if (x >= 0 && x < width) {
				for (int y = tileY; y < tileY + sizeY; y++) {
					if (y >= 0 && y < height) {
						clearFlags(blockMask, x, y);
					}
				}

			}
		}

	}

	private void clearFlags(int mask, int tileX, int tileY) {
		tileFlags[tileX][tileY] &= 0xffffff - mask;
	}

	public void unblockTile(int tileY, int tileX) {
		tileX -= originX;
		tileY -= originY;
		tileFlags[tileX][tileY] &= 0xdfffff;
	}

	public boolean reachedWallObject(int targetX, int tileX, int tileY, int orientation, int shape, int targetY) {
		if (tileX == targetX && tileY == targetY) {
			return true;
		}
		tileX -= originX;
		tileY -= originY;
		targetX -= originX;
		targetY -= originY;
		if (shape == 0) {
			if (orientation == 0) {
				if (tileX == targetX - 1 && tileY == targetY) {
					return true;
				}
				if (tileX == targetX && tileY == targetY + 1 && (tileFlags[tileX][tileY] & 0x1280120) == 0) {
					return true;
				}
				if (tileX == targetX && tileY == targetY - 1 && (tileFlags[tileX][tileY] & 0x1280102) == 0) {
					return true;
				}
			} else if (orientation == 1) {
				if (tileX == targetX && tileY == targetY + 1) {
					return true;
				}
				if (tileX == targetX - 1 && tileY == targetY && (tileFlags[tileX][tileY] & 0x1280108) == 0) {
					return true;
				}
				if (tileX == targetX + 1 && tileY == targetY && (tileFlags[tileX][tileY] & 0x1280180) == 0) {
					return true;
				}
			} else if (orientation == 2) {
				if (tileX == targetX + 1 && tileY == targetY) {
					return true;
				}
				if (tileX == targetX && tileY == targetY + 1 && (tileFlags[tileX][tileY] & 0x1280120) == 0) {
					return true;
				}
				if (tileX == targetX && tileY == targetY - 1 && (tileFlags[tileX][tileY] & 0x1280102) == 0) {
					return true;
				}
			} else if (orientation == 3) {
				if (tileX == targetX && tileY == targetY - 1) {
					return true;
				}
				if (tileX == targetX - 1 && tileY == targetY && (tileFlags[tileX][tileY] & 0x1280108) == 0) {
					return true;
				}
				if (tileX == targetX + 1 && tileY == targetY && (tileFlags[tileX][tileY] & 0x1280180) == 0) {
					return true;
				}
			}
		}
		if (shape == 2) {
			if (orientation == 0) {
				if (tileX == targetX - 1 && tileY == targetY) {
					return true;
				}
				if (tileX == targetX && tileY == targetY + 1) {
					return true;
				}
				if (tileX == targetX + 1 && tileY == targetY && (tileFlags[tileX][tileY] & 0x1280180) == 0) {
					return true;
				}
				if (tileX == targetX && tileY == targetY - 1 && (tileFlags[tileX][tileY] & 0x1280102) == 0) {
					return true;
				}
			} else if (orientation == 1) {
				if (tileX == targetX - 1 && tileY == targetY && (tileFlags[tileX][tileY] & 0x1280108) == 0) {
					return true;
				}
				if (tileX == targetX && tileY == targetY + 1) {
					return true;
				}
				if (tileX == targetX + 1 && tileY == targetY) {
					return true;
				}
				if (tileX == targetX && tileY == targetY - 1 && (tileFlags[tileX][tileY] & 0x1280102) == 0) {
					return true;
				}
			} else if (orientation == 2) {
				if (tileX == targetX - 1 && tileY == targetY && (tileFlags[tileX][tileY] & 0x1280108) == 0) {
					return true;
				}
				if (tileX == targetX && tileY == targetY + 1 && (tileFlags[tileX][tileY] & 0x1280120) == 0) {
					return true;
				}
				if (tileX == targetX + 1 && tileY == targetY) {
					return true;
				}
				if (tileX == targetX && tileY == targetY - 1) {
					return true;
				}
			} else if (orientation == 3) {
				if (tileX == targetX - 1 && tileY == targetY) {
					return true;
				}
				if (tileX == targetX && tileY == targetY + 1 && (tileFlags[tileX][tileY] & 0x1280120) == 0) {
					return true;
				}
				if (tileX == targetX + 1 && tileY == targetY && (tileFlags[tileX][tileY] & 0x1280180) == 0) {
					return true;
				}
				if (tileX == targetX && tileY == targetY - 1) {
					return true;
				}
			}
		}
		if (shape == 9) {
			if (tileX == targetX && tileY == targetY + 1 && (tileFlags[tileX][tileY] & 0x20) == 0) {
				return true;
			}
			if (tileX == targetX && tileY == targetY - 1 && (tileFlags[tileX][tileY] & 2) == 0) {
				return true;
			}
			if (tileX == targetX - 1 && tileY == targetY && (tileFlags[tileX][tileY] & 8) == 0) {
				return true;
			}
			if (tileX == targetX + 1 && tileY == targetY && (tileFlags[tileX][tileY] & 0x80) == 0) {
				return true;
			}
		}
		return false;
	}

	public boolean reachedWallDecoration(int targetX, int targetY, int tileY, int shape, int orientation, int tileX) {
		if (tileX == targetX && tileY == targetY) {
			return true;
		}
		tileX -= originX;
		tileY -= originY;
		targetX -= originX;
		targetY -= originY;
		if (shape == 6 || shape == 7) {
			if (shape == 7) {
				orientation = orientation + 2 & 3;
			}
			if (orientation == 0) {
				if (tileX == targetX + 1 && tileY == targetY && (tileFlags[tileX][tileY] & 0x80) == 0) {
					return true;
				}
				if (tileX == targetX && tileY == targetY - 1 && (tileFlags[tileX][tileY] & 2) == 0) {
					return true;
				}
			} else if (orientation == 1) {
				if (tileX == targetX - 1 && tileY == targetY && (tileFlags[tileX][tileY] & 8) == 0) {
					return true;
				}
				if (tileX == targetX && tileY == targetY - 1 && (tileFlags[tileX][tileY] & 2) == 0) {
					return true;
				}
			} else if (orientation == 2) {
				if (tileX == targetX - 1 && tileY == targetY && (tileFlags[tileX][tileY] & 8) == 0) {
					return true;
				}
				if (tileX == targetX && tileY == targetY + 1 && (tileFlags[tileX][tileY] & 0x20) == 0) {
					return true;
				}
			} else if (orientation == 3) {
				if (tileX == targetX + 1 && tileY == targetY && (tileFlags[tileX][tileY] & 0x80) == 0) {
					return true;
				}
				if (tileX == targetX && tileY == targetY + 1 && (tileFlags[tileX][tileY] & 0x20) == 0) {
					return true;
				}
			}
		}
		if (shape == 8) {
			if (tileX == targetX && tileY == targetY + 1 && (tileFlags[tileX][tileY] & 0x20) == 0) {
				return true;
			}
			if (tileX == targetX && tileY == targetY - 1 && (tileFlags[tileX][tileY] & 2) == 0) {
				return true;
			}
			if (tileX == targetX - 1 && tileY == targetY && (tileFlags[tileX][tileY] & 8) == 0) {
				return true;
			}
			if (tileX == targetX + 1 && tileY == targetY && (tileFlags[tileX][tileY] & 0x80) == 0) {
				return true;
			}
		}
		return false;
	}

	// Something to do with moving to objects/npcs etc when clicked on
	// Maybe checking distance?
	public boolean reachedObjectFootprint(int targetY, int targetX, int tileX, int sizeY, int blockedSidesMask, int sizeX, int tileY) {
		int maxX = targetX + sizeX - 1;
		int maxY = targetY + sizeY - 1;
		if (tileX >= targetX && tileX <= maxX && tileY >= targetY && tileY <= maxY) {
			return true;
		}
		if (tileX == targetX - 1 && tileY >= targetY && tileY <= maxY && (tileFlags[tileX - originX][tileY - originY] & 8) == 0 && (blockedSidesMask & 8) == 0) {
			return true;
		}
		if (tileX == maxX + 1 && tileY >= targetY && tileY <= maxY && (tileFlags[tileX - originX][tileY - originY] & 0x80) == 0 && (blockedSidesMask & 2) == 0) {
			return true;
		}
		return tileY == targetY - 1 && tileX >= targetX && tileX <= maxX && (tileFlags[tileX - originX][tileY - originY] & 2) == 0 && (blockedSidesMask & 4) == 0 || tileY == maxY + 1 && tileX >= targetX && tileX <= maxX && (tileFlags[tileX - originX][tileY - originY] & 0x20) == 0 && (blockedSidesMask & 1) == 0;
	}

	private final int originX;
	private final int originY;
	private final int width;
	private final int height;
	public final int[][] tileFlags;
}
