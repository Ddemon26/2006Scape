// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

public final class Ground extends Node {

        public Ground(int i, int j, int k) {
                obj5Array = new SceneObject[5];
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
        public BoundaryObject obj1;
        public WallDecoration obj2;
        public TileDecoration obj3;
        public ItemPile itemPile;
        int sceneObjectCount;
        public final SceneObject[] obj5Array;
        final int[] sceneObjectFlags;
        int combinedFlags;
        int groundFlag;
	boolean aBoolean1322;
	boolean aBoolean1323;
	boolean aBoolean1324;
	int anInt1325;
	int anInt1326;
	int anInt1327;
	int anInt1328;
        public Ground linkedTile;
}
