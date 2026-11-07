// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

final class Class40 {

	public Class40(int tileY, int overlayColour0, int underlayColour3, int height2, int textureId, int overlayColour2, int rotation, int underlayColour0, int underlayMapColour, int underlayColour2, int height3, int height1, int height0, int shape, int overlayColour3, int overlayColour1, int underlayColour1, int tileX, int overlayMapColour) {
		flat = !(height0 != height1 || height0 != height2 || height0 != height3);
		this.shape = shape;
		this.rotation = rotation;
		this.underlayMapColour = underlayMapColour;
		this.overlayMapColour = overlayMapColour;
		char tileSize = '\200';
		int halfSize = tileSize / 2;
		int quarterSize = tileSize / 4;
		int threeQuarterSize = tileSize * 3 / 4;
		int pointIds[] = shapeVertexPointIds[shape];
		int vertexCount = pointIds.length;
		vertexX = new int[vertexCount];
		vertexHeight = new int[vertexCount];
		vertexY = new int[vertexCount];
		int vertexUnderlayColour[] = new int[vertexCount];
		int vertexOverlayColour[] = new int[vertexCount];
		int tileBaseX = tileX * tileSize;
		int tileBaseY = tileY * tileSize;
		for (int vertex = 0; vertex < vertexCount; vertex++) {
			int pointId = pointIds[vertex];
			if ((pointId & 1) == 0 && pointId <= 8) {
				pointId = (pointId - rotation - rotation - 1 & 7) + 1;
			}
			if (pointId > 8 && pointId <= 12) {
				pointId = (pointId - 9 - rotation & 3) + 9;
			}
			if (pointId > 12 && pointId <= 16) {
				pointId = (pointId - 13 - rotation & 3) + 13;
			}
			int pointX;
			int pointY;
			int pointHeight;
			int pointUnderlayColour;
			int pointOverlayColour;
			if (pointId == 1) {
				pointX = tileBaseX;
				pointY = tileBaseY;
				pointHeight = height0;
				pointUnderlayColour = underlayColour0;
				pointOverlayColour = overlayColour0;
			} else if (pointId == 2) {
				pointX = tileBaseX + halfSize;
				pointY = tileBaseY;
				pointHeight = height0 + height1 >> 1;
				pointUnderlayColour = underlayColour0 + underlayColour1 >> 1;
				pointOverlayColour = overlayColour0 + overlayColour1 >> 1;
			} else if (pointId == 3) {
				pointX = tileBaseX + tileSize;
				pointY = tileBaseY;
				pointHeight = height1;
				pointUnderlayColour = underlayColour1;
				pointOverlayColour = overlayColour1;
			} else if (pointId == 4) {
				pointX = tileBaseX + tileSize;
				pointY = tileBaseY + halfSize;
				pointHeight = height1 + height2 >> 1;
				pointUnderlayColour = underlayColour1 + underlayColour2 >> 1;
				pointOverlayColour = overlayColour1 + overlayColour2 >> 1;
			} else if (pointId == 5) {
				pointX = tileBaseX + tileSize;
				pointY = tileBaseY + tileSize;
				pointHeight = height2;
				pointUnderlayColour = underlayColour2;
				pointOverlayColour = overlayColour2;
			} else if (pointId == 6) {
				pointX = tileBaseX + halfSize;
				pointY = tileBaseY + tileSize;
				pointHeight = height2 + height3 >> 1;
				pointUnderlayColour = underlayColour2 + underlayColour3 >> 1;
				pointOverlayColour = overlayColour2 + overlayColour3 >> 1;
			} else if (pointId == 7) {
				pointX = tileBaseX;
				pointY = tileBaseY + tileSize;
				pointHeight = height3;
				pointUnderlayColour = underlayColour3;
				pointOverlayColour = overlayColour3;
			} else if (pointId == 8) {
				pointX = tileBaseX;
				pointY = tileBaseY + halfSize;
				pointHeight = height3 + height0 >> 1;
				pointUnderlayColour = underlayColour3 + underlayColour0 >> 1;
				pointOverlayColour = overlayColour3 + overlayColour0 >> 1;
			} else if (pointId == 9) {
				pointX = tileBaseX + halfSize;
				pointY = tileBaseY + quarterSize;
				pointHeight = height0 + height1 >> 1;
				pointUnderlayColour = underlayColour0 + underlayColour1 >> 1;
				pointOverlayColour = overlayColour0 + overlayColour1 >> 1;
			} else if (pointId == 10) {
				pointX = tileBaseX + threeQuarterSize;
				pointY = tileBaseY + halfSize;
				pointHeight = height1 + height2 >> 1;
				pointUnderlayColour = underlayColour1 + underlayColour2 >> 1;
				pointOverlayColour = overlayColour1 + overlayColour2 >> 1;
			} else if (pointId == 11) {
				pointX = tileBaseX + halfSize;
				pointY = tileBaseY + threeQuarterSize;
				pointHeight = height2 + height3 >> 1;
				pointUnderlayColour = underlayColour2 + underlayColour3 >> 1;
				pointOverlayColour = overlayColour2 + overlayColour3 >> 1;
			} else if (pointId == 12) {
				pointX = tileBaseX + quarterSize;
				pointY = tileBaseY + halfSize;
				pointHeight = height3 + height0 >> 1;
				pointUnderlayColour = underlayColour3 + underlayColour0 >> 1;
				pointOverlayColour = overlayColour3 + overlayColour0 >> 1;
			} else if (pointId == 13) {
				pointX = tileBaseX + quarterSize;
				pointY = tileBaseY + quarterSize;
				pointHeight = height0;
				pointUnderlayColour = underlayColour0;
				pointOverlayColour = overlayColour0;
			} else if (pointId == 14) {
				pointX = tileBaseX + threeQuarterSize;
				pointY = tileBaseY + quarterSize;
				pointHeight = height1;
				pointUnderlayColour = underlayColour1;
				pointOverlayColour = overlayColour1;
			} else if (pointId == 15) {
				pointX = tileBaseX + threeQuarterSize;
				pointY = tileBaseY + threeQuarterSize;
				pointHeight = height2;
				pointUnderlayColour = underlayColour2;
				pointOverlayColour = overlayColour2;
			} else {
				pointX = tileBaseX + quarterSize;
				pointY = tileBaseY + threeQuarterSize;
				pointHeight = height3;
				pointUnderlayColour = underlayColour3;
				pointOverlayColour = overlayColour3;
			}
			vertexX[vertex] = pointX;
			vertexHeight[vertex] = pointHeight;
			vertexY[vertex] = pointY;
			vertexUnderlayColour[vertex] = pointUnderlayColour;
			vertexOverlayColour[vertex] = pointOverlayColour;
		}

		int shapeFaceData[] = shapeFaces[shape];
		int faceCount = shapeFaceData.length / 4;
		faceVertexA = new int[faceCount];
		faceVertexB = new int[faceCount];
		faceVertexC = new int[faceCount];
		faceColourA = new int[faceCount];
		faceColourB = new int[faceCount];
		faceColourC = new int[faceCount];
		if (textureId != -1) {
			faceTexture = new int[faceCount];
		}
		int faceDataIndex = 0;
		for (int face = 0; face < faceCount; face++) {
			int faceType = shapeFaceData[faceDataIndex];
			int vertexA = shapeFaceData[faceDataIndex + 1];
			int vertexB = shapeFaceData[faceDataIndex + 2];
			int vertexC = shapeFaceData[faceDataIndex + 3];
			faceDataIndex += 4;
			if (vertexA < 4) {
				vertexA = vertexA - rotation & 3;
			}
			if (vertexB < 4) {
				vertexB = vertexB - rotation & 3;
			}
			if (vertexC < 4) {
				vertexC = vertexC - rotation & 3;
			}
			faceVertexA[face] = vertexA;
			faceVertexB[face] = vertexB;
			faceVertexC[face] = vertexC;
			if (faceType == 0) {
				faceColourA[face] = vertexUnderlayColour[vertexA];
				faceColourB[face] = vertexUnderlayColour[vertexB];
				faceColourC[face] = vertexUnderlayColour[vertexC];
				if (faceTexture != null) {
					faceTexture[face] = -1;
				}
			} else {
				faceColourA[face] = vertexOverlayColour[vertexA];
				faceColourB[face] = vertexOverlayColour[vertexB];
				faceColourC[face] = vertexOverlayColour[vertexC];
				if (faceTexture != null) {
					faceTexture[face] = textureId;
				}
			}
		}

		int i9 = height0;
		int l9 = height1;
		if (height1 < i9) {
			i9 = height1;
		}
		if (height1 > l9) {
			l9 = height1;
		}
		if (height2 < i9) {
			i9 = height2;
		}
		if (height2 > l9) {
			l9 = height2;
		}
		if (height3 < i9) {
			i9 = height3;
		}
		if (height3 > l9) {
			l9 = height3;
		}
		i9 /= 14;
		l9 /= 14;
	}

	final int[] vertexX;
	final int[] vertexHeight;
	final int[] vertexY;
	final int[] faceColourA;
	final int[] faceColourB;
	final int[] faceColourC;
	final int[] faceVertexA;
	final int[] faceVertexB;
	final int[] faceVertexC;
	int faceTexture[];
	final boolean flat;
	final int shape;
	final int rotation;
	final int underlayMapColour;
	final int overlayMapColour;
	static final int[] projectedX = new int[6];
	static final int[] projectedY = new int[6];
	static final int[] cameraSpaceX = new int[6];
	static final int[] cameraSpaceHeight = new int[6];
	static final int[] cameraSpaceDepth = new int[6];
	static final int[] anIntArray693 = {1, 0};
	static final int[] anIntArray694 = {2, 1};
	static final int[] anIntArray695 = {3, 3};
	private static final int[][] shapeVertexPointIds = {{1, 3, 5, 7}, {1, 3, 5, 7}, {1, 3, 5, 7}, {1, 3, 5, 7, 6}, {1, 3, 5, 7, 6}, {1, 3, 5, 7, 6}, {1, 3, 5, 7, 6}, {1, 3, 5, 7, 2, 6}, {1, 3, 5, 7, 2, 8}, {1, 3, 5, 7, 2, 8}, {1, 3, 5, 7, 11, 12}, {1, 3, 5, 7, 11, 12}, {1, 3, 5, 7, 13, 14}};
	private static final int[][] shapeFaces = {{0, 1, 2, 3, 0, 0, 1, 3}, {1, 1, 2, 3, 1, 0, 1, 3}, {0, 1, 2, 3, 1, 0, 1, 3}, {0, 0, 1, 2, 0, 0, 2, 4, 1, 0, 4, 3}, {0, 0, 1, 4, 0, 0, 4, 3, 1, 1, 2, 4}, {0, 0, 4, 3, 1, 0, 1, 2, 1, 0, 2, 4}, {0, 1, 2, 4, 1, 0, 1, 4, 1, 0, 4, 3}, {0, 4, 1, 2, 0, 4, 2, 5, 1, 0, 4, 5, 1, 0, 5, 3}, {0, 4, 1, 2, 0, 4, 2, 3, 0, 4, 3, 5, 1, 0, 4, 5}, {0, 0, 4, 5, 1, 4, 1, 2, 1, 4, 2, 3, 1, 4, 3, 5}, {0, 0, 1, 5, 0, 1, 4, 5, 0, 1, 2, 4, 1, 0, 5, 3, 1, 5, 4, 3, 1, 4, 2, 3}, {1, 0, 1, 5, 1, 1, 4, 5, 1, 1, 2, 4, 0, 0, 5, 3, 0, 5, 4, 3, 0, 4, 2, 3}, {1, 0, 5, 4, 1, 0, 1, 5, 0, 0, 4, 3, 0, 4, 5, 3, 0, 5, 2, 3, 0, 1, 2, 5}};

}
