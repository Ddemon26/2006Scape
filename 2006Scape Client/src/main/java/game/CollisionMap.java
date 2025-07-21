// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

final class CollisionMap {

	public CollisionMap() {
		xInset = 0;
		yInset = 0;
		width = 104;
		height = 104;
		clippingFlags = new int[width][height];
		reset();
	}

	public void reset() {
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (i == 0 || j == 0 || i == width - 1 || j == height - 1) {
					clippingFlags[i][j] = 0xffffff;
				} else {
					clippingFlags[i][j] = 0x1000000;
				}
			}

		}

	}

	public void addWall(int i, int j, int k, int l, boolean flag) {
		k -= xInset;
		i -= yInset;
		if (l == 0) {
			if (j == 0) {
				addFlag(k, i, 128);
				addFlag(k - 1, i, 8);
			}
			if (j == 1) {
				addFlag(k, i, 2);
				addFlag(k, i + 1, 32);
			}
			if (j == 2) {
				addFlag(k, i, 8);
				addFlag(k + 1, i, 128);
			}
			if (j == 3) {
				addFlag(k, i, 32);
				addFlag(k, i - 1, 2);
			}
		}
		if (l == 1 || l == 3) {
			if (j == 0) {
				addFlag(k, i, 1);
				addFlag(k - 1, i + 1, 16);
			}
			if (j == 1) {
				addFlag(k, i, 4);
				addFlag(k + 1, i + 1, 64);
			}
			if (j == 2) {
				addFlag(k, i, 16);
				addFlag(k + 1, i - 1, 1);
			}
			if (j == 3) {
				addFlag(k, i, 64);
				addFlag(k - 1, i - 1, 4);
			}
		}
		if (l == 2) {
			if (j == 0) {
				addFlag(k, i, 130);
				addFlag(k - 1, i, 8);
				addFlag(k, i + 1, 32);
			}
			if (j == 1) {
				addFlag(k, i, 10);
				addFlag(k, i + 1, 32);
				addFlag(k + 1, i, 128);
			}
			if (j == 2) {
				addFlag(k, i, 40);
				addFlag(k + 1, i, 128);
				addFlag(k, i - 1, 2);
			}
			if (j == 3) {
				addFlag(k, i, 160);
				addFlag(k, i - 1, 2);
				addFlag(k - 1, i, 8);
			}
		}
		if (flag) {
			if (l == 0) {
				if (j == 0) {
					addFlag(k, i, 0x10000);
					addFlag(k - 1, i, 4096);
				}
				if (j == 1) {
					addFlag(k, i, 1024);
					addFlag(k, i + 1, 16384);
				}
				if (j == 2) {
					addFlag(k, i, 4096);
					addFlag(k + 1, i, 0x10000);
				}
				if (j == 3) {
					addFlag(k, i, 16384);
					addFlag(k, i - 1, 1024);
				}
			}
			if (l == 1 || l == 3) {
				if (j == 0) {
					addFlag(k, i, 512);
					addFlag(k - 1, i + 1, 8192);
				}
				if (j == 1) {
					addFlag(k, i, 2048);
					addFlag(k + 1, i + 1, 32768);
				}
				if (j == 2) {
					addFlag(k, i, 8192);
					addFlag(k + 1, i - 1, 512);
				}
				if (j == 3) {
					addFlag(k, i, 32768);
					addFlag(k - 1, i - 1, 2048);
				}
			}
			if (l == 2) {
				if (j == 0) {
					addFlag(k, i, 0x10400);
					addFlag(k - 1, i, 4096);
					addFlag(k, i + 1, 16384);
				}
				if (j == 1) {
					addFlag(k, i, 5120);
					addFlag(k, i + 1, 16384);
					addFlag(k + 1, i, 0x10000);
				}
				if (j == 2) {
					addFlag(k, i, 20480);
					addFlag(k + 1, i, 0x10000);
					addFlag(k, i - 1, 1024);
				}
				if (j == 3) {
					addFlag(k, i, 0x14000);
					addFlag(k, i - 1, 1024);
					addFlag(k - 1, i, 4096);
				}
			}
		}
	}

	public void addObject(boolean flag, int j, int k, int l, int i1, int j1) {
		int k1 = 256;
		if (flag) {
			k1 += 0x20000;
		}
		l -= xInset;
		i1 -= yInset;
		if (j1 == 1 || j1 == 3) {
			int l1 = j;
			j = k;
			k = l1;
		}
		for (int i2 = l; i2 < l + j; i2++) {
			if (i2 >= 0 && i2 < width) {
				for (int j2 = i1; j2 < i1 + k; j2++) {
					if (j2 >= 0 && j2 < height) {
						addFlag(i2, j2, k1);
					}
				}

			}
		}

	}

	public void blockTile(int i, int k) {
		k -= xInset;
		i -= yInset;
		clippingFlags[k][i] |= 0x200000;
	}

	private void addFlag(int i, int j, int k) {
		clippingFlags[i][j] |= k;
	}

	public void removeWall(int i, int j, boolean flag, int k, int l) {
		k -= xInset;
		l -= yInset;
		if (j == 0) {
			if (i == 0) {
				removeFlag(128, k, l);
				removeFlag(8, k - 1, l);
			}
			if (i == 1) {
				removeFlag(2, k, l);
				removeFlag(32, k, l + 1);
			}
			if (i == 2) {
				removeFlag(8, k, l);
				removeFlag(128, k + 1, l);
			}
			if (i == 3) {
				removeFlag(32, k, l);
				removeFlag(2, k, l - 1);
			}
		}
		if (j == 1 || j == 3) {
			if (i == 0) {
				removeFlag(1, k, l);
				removeFlag(16, k - 1, l + 1);
			}
			if (i == 1) {
				removeFlag(4, k, l);
				removeFlag(64, k + 1, l + 1);
			}
			if (i == 2) {
				removeFlag(16, k, l);
				removeFlag(1, k + 1, l - 1);
			}
			if (i == 3) {
				removeFlag(64, k, l);
				removeFlag(4, k - 1, l - 1);
			}
		}
		if (j == 2) {
			if (i == 0) {
				removeFlag(130, k, l);
				removeFlag(8, k - 1, l);
				removeFlag(32, k, l + 1);
			}
			if (i == 1) {
				removeFlag(10, k, l);
				removeFlag(32, k, l + 1);
				removeFlag(128, k + 1, l);
			}
			if (i == 2) {
				removeFlag(40, k, l);
				removeFlag(128, k + 1, l);
				removeFlag(2, k, l - 1);
			}
			if (i == 3) {
				removeFlag(160, k, l);
				removeFlag(2, k, l - 1);
				removeFlag(8, k - 1, l);
			}
		}
		if (flag) {
			if (j == 0) {
				if (i == 0) {
					removeFlag(0x10000, k, l);
					removeFlag(4096, k - 1, l);
				}
				if (i == 1) {
					removeFlag(1024, k, l);
					removeFlag(16384, k, l + 1);
				}
				if (i == 2) {
					removeFlag(4096, k, l);
					removeFlag(0x10000, k + 1, l);
				}
				if (i == 3) {
					removeFlag(16384, k, l);
					removeFlag(1024, k, l - 1);
				}
			}
			if (j == 1 || j == 3) {
				if (i == 0) {
					removeFlag(512, k, l);
					removeFlag(8192, k - 1, l + 1);
				}
				if (i == 1) {
					removeFlag(2048, k, l);
					removeFlag(32768, k + 1, l + 1);
				}
				if (i == 2) {
					removeFlag(8192, k, l);
					removeFlag(512, k + 1, l - 1);
				}
				if (i == 3) {
					removeFlag(32768, k, l);
					removeFlag(2048, k - 1, l - 1);
				}
			}
			if (j == 2) {
				if (i == 0) {
					removeFlag(0x10400, k, l);
					removeFlag(4096, k - 1, l);
					removeFlag(16384, k, l + 1);
				}
				if (i == 1) {
					removeFlag(5120, k, l);
					removeFlag(16384, k, l + 1);
					removeFlag(0x10000, k + 1, l);
				}
				if (i == 2) {
					removeFlag(20480, k, l);
					removeFlag(0x10000, k + 1, l);
					removeFlag(1024, k, l - 1);
				}
				if (i == 3) {
					removeFlag(0x14000, k, l);
					removeFlag(1024, k, l - 1);
					removeFlag(4096, k - 1, l);
				}
			}
		}
	}

	public void removeObject(int i, int j, int k, int l, int i1, boolean flag) {
		int j1 = 256;
		if (flag) {
			j1 += 0x20000;
		}
		k -= xInset;
		l -= yInset;
		if (i == 1 || i == 3) {
			int k1 = j;
			j = i1;
			i1 = k1;
		}
		for (int l1 = k; l1 < k + j; l1++) {
			if (l1 >= 0 && l1 < width) {
				for (int i2 = l; i2 < l + i1; i2++) {
					if (i2 >= 0 && i2 < height) {
						removeFlag(j1, l1, i2);
					}
				}

			}
		}

	}

	private void removeFlag(int i, int j, int k) {
		clippingFlags[j][k] &= 0xffffff - i;
	}

	public void unblockTile(int j, int k) {
		k -= xInset;
		j -= yInset;
		clippingFlags[k][j] &= 0xdfffff;
	}

	public boolean canReachWall(int i, int j, int k, int i1, int j1, int k1) {
		if (j == i && k == k1) {
			return true;
		}
		j -= xInset;
		k -= yInset;
		i -= xInset;
		k1 -= yInset;
		if (j1 == 0) {
			if (i1 == 0) {
				if (j == i - 1 && k == k1) {
					return true;
				}
				if (j == i && k == k1 + 1 && (clippingFlags[j][k] & 0x1280120) == 0) {
					return true;
				}
				if (j == i && k == k1 - 1 && (clippingFlags[j][k] & 0x1280102) == 0) {
					return true;
				}
			} else if (i1 == 1) {
				if (j == i && k == k1 + 1) {
					return true;
				}
				if (j == i - 1 && k == k1 && (clippingFlags[j][k] & 0x1280108) == 0) {
					return true;
				}
				if (j == i + 1 && k == k1 && (clippingFlags[j][k] & 0x1280180) == 0) {
					return true;
				}
			} else if (i1 == 2) {
				if (j == i + 1 && k == k1) {
					return true;
				}
				if (j == i && k == k1 + 1 && (clippingFlags[j][k] & 0x1280120) == 0) {
					return true;
				}
				if (j == i && k == k1 - 1 && (clippingFlags[j][k] & 0x1280102) == 0) {
					return true;
				}
			} else if (i1 == 3) {
				if (j == i && k == k1 - 1) {
					return true;
				}
				if (j == i - 1 && k == k1 && (clippingFlags[j][k] & 0x1280108) == 0) {
					return true;
				}
				if (j == i + 1 && k == k1 && (clippingFlags[j][k] & 0x1280180) == 0) {
					return true;
				}
			}
		}
		if (j1 == 2) {
			if (i1 == 0) {
				if (j == i - 1 && k == k1) {
					return true;
				}
				if (j == i && k == k1 + 1) {
					return true;
				}
				if (j == i + 1 && k == k1 && (clippingFlags[j][k] & 0x1280180) == 0) {
					return true;
				}
				if (j == i && k == k1 - 1 && (clippingFlags[j][k] & 0x1280102) == 0) {
					return true;
				}
			} else if (i1 == 1) {
				if (j == i - 1 && k == k1 && (clippingFlags[j][k] & 0x1280108) == 0) {
					return true;
				}
				if (j == i && k == k1 + 1) {
					return true;
				}
				if (j == i + 1 && k == k1) {
					return true;
				}
				if (j == i && k == k1 - 1 && (clippingFlags[j][k] & 0x1280102) == 0) {
					return true;
				}
			} else if (i1 == 2) {
				if (j == i - 1 && k == k1 && (clippingFlags[j][k] & 0x1280108) == 0) {
					return true;
				}
				if (j == i && k == k1 + 1 && (clippingFlags[j][k] & 0x1280120) == 0) {
					return true;
				}
				if (j == i + 1 && k == k1) {
					return true;
				}
				if (j == i && k == k1 - 1) {
					return true;
				}
			} else if (i1 == 3) {
				if (j == i - 1 && k == k1) {
					return true;
				}
				if (j == i && k == k1 + 1 && (clippingFlags[j][k] & 0x1280120) == 0) {
					return true;
				}
				if (j == i + 1 && k == k1 && (clippingFlags[j][k] & 0x1280180) == 0) {
					return true;
				}
				if (j == i && k == k1 - 1) {
					return true;
				}
			}
		}
		if (j1 == 9) {
			if (j == i && k == k1 + 1 && (clippingFlags[j][k] & 0x20) == 0) {
				return true;
			}
			if (j == i && k == k1 - 1 && (clippingFlags[j][k] & 2) == 0) {
				return true;
			}
			if (j == i - 1 && k == k1 && (clippingFlags[j][k] & 8) == 0) {
				return true;
			}
			if (j == i + 1 && k == k1 && (clippingFlags[j][k] & 0x80) == 0) {
				return true;
			}
		}
		return false;
	}

	public boolean canReachObject(int i, int j, int k, int l, int i1, int j1) {
		if (j1 == i && k == j) {
			return true;
		}
		j1 -= xInset;
		k -= yInset;
		i -= xInset;
		j -= yInset;
		if (l == 6 || l == 7) {
			if (l == 7) {
				i1 = i1 + 2 & 3;
			}
			if (i1 == 0) {
				if (j1 == i + 1 && k == j && (clippingFlags[j1][k] & 0x80) == 0) {
					return true;
				}
				if (j1 == i && k == j - 1 && (clippingFlags[j1][k] & 2) == 0) {
					return true;
				}
			} else if (i1 == 1) {
				if (j1 == i - 1 && k == j && (clippingFlags[j1][k] & 8) == 0) {
					return true;
				}
				if (j1 == i && k == j - 1 && (clippingFlags[j1][k] & 2) == 0) {
					return true;
				}
			} else if (i1 == 2) {
				if (j1 == i - 1 && k == j && (clippingFlags[j1][k] & 8) == 0) {
					return true;
				}
				if (j1 == i && k == j + 1 && (clippingFlags[j1][k] & 0x20) == 0) {
					return true;
				}
			} else if (i1 == 3) {
				if (j1 == i + 1 && k == j && (clippingFlags[j1][k] & 0x80) == 0) {
					return true;
				}
				if (j1 == i && k == j + 1 && (clippingFlags[j1][k] & 0x20) == 0) {
					return true;
				}
			}
		}
		if (l == 8) {
			if (j1 == i && k == j + 1 && (clippingFlags[j1][k] & 0x20) == 0) {
				return true;
			}
			if (j1 == i && k == j - 1 && (clippingFlags[j1][k] & 2) == 0) {
				return true;
			}
			if (j1 == i - 1 && k == j && (clippingFlags[j1][k] & 8) == 0) {
				return true;
			}
			if (j1 == i + 1 && k == j && (clippingFlags[j1][k] & 0x80) == 0) {
				return true;
			}
		}
		return false;
	}

	// Something to do with moving to objects/npcs etc when clicked on
	// Maybe checking distance?
	public boolean canReachArea(int i, int j, int k, int l, int i1, int j1, int k1) {
		int l1 = j + j1 - 1;
		int i2 = i + l - 1;
		if (k >= j && k <= l1 && k1 >= i && k1 <= i2) {
			return true;
		}
		if (k == j - 1 && k1 >= i && k1 <= i2 && (clippingFlags[k - xInset][k1 - yInset] & 8) == 0 && (i1 & 8) == 0) {
			return true;
		}
		if (k == l1 + 1 && k1 >= i && k1 <= i2 && (clippingFlags[k - xInset][k1 - yInset] & 0x80) == 0 && (i1 & 2) == 0) {
			return true;
		}
		return k1 == i - 1 && k >= j && k <= l1 && (clippingFlags[k - xInset][k1 - yInset] & 2) == 0 && (i1 & 4) == 0 || k1 == i2 + 1 && k >= j && k <= l1 && (clippingFlags[k - xInset][k1 - yInset] & 0x20) == 0 && (i1 & 1) == 0;
	}

	private final int xInset;
	private final int yInset;
	private final int width;
	private final int height;
	public final int[][] clippingFlags;
}
