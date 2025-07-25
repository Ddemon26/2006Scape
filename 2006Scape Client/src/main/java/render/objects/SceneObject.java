package render.objects;

import game.entities.Animable;

// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3)

public final class SceneObject {

  public SceneObject() {}

  public int plane;
  public int height;
  public int x;
  public int y;
  public Animable renderable;
  public int orientation;
  public int startX;
  public int endX;
  public int startY;
  public int endY;
  public int distanceFromCamera;
  public int lastDrawn;
  public int uid;
  public byte config;
}
