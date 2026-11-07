// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3)

/** Quarter-turn rotation of tile coordinates inside an 8x8 region block (used when copying map blocks). */
final class TileRotation {

	/** New x of the tile (tileX, tileY) after rotating the block by rotation quarter turns (masked to 0-3). */
	public static int rotatedX(int rotation, int tileY, int tileX) {
		rotation &= 3;
		if (rotation == 0) {
			return tileX;
		}
		if (rotation == 1) {
			return tileY;
		}
		if (rotation == 2) {
			return 7 - tileX;
		} else {
			return 7 - tileY;
		}
	}

	/** New y of the tile (tileX, tileY) after rotating the block by rotation quarter turns (masked to 0-3). */
	public static int rotatedY(int tileY, int rotation, int tileX) {
		rotation &= 3;
		if (rotation == 0) {
			return tileY;
		}
		if (rotation == 1) {
			return 7 - tileX;
		}
		if (rotation == 2) {
			return 7 - tileY;
		} else {
			return tileX;
		}
	}

	/** Lowest x of a sizeX by sizeY footprint anchored at (tileX, tileY) after the block rotates. */
	public static int rotatedFootprintMinX(int rotation, int sizeY, int tileX, int tileY, int sizeX) {
		rotation &= 3;
		if (rotation == 0) {
			return tileX;
		}
		if (rotation == 1) {
			return tileY;
		}
		if (rotation == 2) {
			return 7 - tileX - (sizeX - 1);
		} else {
			return 7 - tileY - (sizeY - 1);
		}
	}

	/** Lowest y of a sizeX by sizeY footprint anchored at (tileX, tileY) after the block rotates. */
	public static int rotatedFootprintMinY(int tileY, int sizeY, int rotation, int sizeX, int tileX) {
		rotation &= 3;
		if (rotation == 0) {
			return tileY;
		}
		if (rotation == 1) {
			return 7 - tileX - (sizeX - 1);
		}
		if (rotation == 2) {
			return 7 - tileY - (sizeY - 1);
		} else {
			return tileX;
		}
	}

}
