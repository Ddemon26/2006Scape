package game.world;// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

import game.entities.Animable;

public final class BoundaryObject {

    public BoundaryObject() {}

    public int plane;
    public int x;
    public int y;
    public int orientation;
    public int orientation2;
    public Animable primary;
    public Animable secondary;
    public int uid;
    public byte config;
}
