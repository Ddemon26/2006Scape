package render.objects; // Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.

// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3)

import game.items.ItemPile;
import game.world.BoundaryObject;
import game.world.SceneObject;
import game.world.TileDecoration;
import game.world.WallDecoration;
import render.tiles.PlainTile;
import render.tiles.ShapedTile;
import util.collections.Node;

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
