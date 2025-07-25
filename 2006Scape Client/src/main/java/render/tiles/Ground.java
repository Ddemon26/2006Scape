package render.tiles;

import game.items.ItemPile;
import render.objects.BoundaryObject;
import render.objects.SceneObject;
import render.objects.TileDecoration;
import render.objects.WallDecoration;
import util.collections.Node;

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

  public int plane;
  public final int x;
  public final int y;
  public final int basePlane;
  public PlainTile plainTile;
  public ShapedTile shapedTile;
  public BoundaryObject boundaryObject;
  public WallDecoration wallDecoration;
  public TileDecoration tileDecoration;
  public ItemPile itemPile;
  public int sceneObjectCount;
  public final SceneObject[] sceneObjects;
  public final int[] sceneObjectFlags;
  public int combinedFlags;
  public int groundFlag;
  public boolean tileActive;
  public boolean inQueue;
  public boolean needsProcessing;
  public int cullFlags;
  public int cullOrientation;
  public int cullOpposite;
  public int boundaryFlags;
  public Ground linkedTile;
}
