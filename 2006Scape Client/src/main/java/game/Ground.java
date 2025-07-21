// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

public final class Ground extends Node {

        public Ground(int i, int j, int k) {
                sceneObjects = new SceneObject[5];
                sceneObjectFlags = new int[5];
                basePlane = plane = i;
                x = j;
                y = k;
	}

        int plane;
        final int x;
        final int y;
        final int basePlane;
        public PlainTile plainTile;
        public ShapedTile shapedTile;
        public BoundaryObject boundaryObject;
        public WallDecoration wallDecoration;
        public TileDecoration tileDecoration;
        public ItemPile itemPile;
        int sceneObjectCount;
        public final SceneObject[] sceneObjects;
        final int[] sceneObjectFlags;
        int combinedFlags;
        int groundFlag;
	boolean tileActive;
	boolean inQueue;
	boolean needsProcessing;
	int cullFlags;
	int cullOrientation;
	int cullOpposite;
	int boundaryFlags;
        public Ground linkedTile;
}
