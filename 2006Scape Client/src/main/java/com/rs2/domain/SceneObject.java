package com.rs2.domain;

// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

public final class SceneObject {

    public SceneObject() {
        }

    int plane;
    int height;
    int x;
    int y;
    public Animable renderable;
    public int orientation;
    int startX;
    int endX;
    int startY;
    int endY;
    int distanceFromCamera;
    int lastDrawn;
    public int uid;
    byte config;
}
