// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

public final class Ground extends Node {

	public Ground(int plane, int tileX, int tileY) {
		obj5Array = new Object5[5];
		objectEdgeMasks = new int[5];
		originalPlane = this.plane = plane;
		this.tileX = tileX;
		this.tileY = tileY;
	}

	int plane;
	final int tileX;
	final int tileY;
	final int originalPlane;
	public Class43 plainTile;
	public Class40 shapedTile;
	public Object1 obj1;
	public Object2 obj2;
	public Object3 obj3;
	public Object4 obj4;
	int objectCount;
	public final Object5[] obj5Array;
	final int[] objectEdgeMasks;
	int objectEdgeMask;
	int minCameraPlane;
	boolean firstPassPending;
	boolean drawPending;
	boolean objectsPending;
	int anInt1325;
	int anInt1326;
	int anInt1327;
	int lateWallMask;
	public Ground lowerGround;
}
