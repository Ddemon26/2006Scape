package com.rs2.domain;

// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

public final class FrameBase {

    public FrameBase(Stream stream) {
        int count = stream.readUnsignedByte();
        transformationType = new int[count];
        transformationList = new int[count][];
        for (int j = 0; j < count; j++) {
            transformationType[j] = stream.readUnsignedByte();
        }

        for (int k = 0; k < count; k++) {
            int length = stream.readUnsignedByte();
            transformationList[k] = new int[length];
            for (int i1 = 0; i1 < length; i1++) {
                transformationList[k][i1] = stream.readUnsignedByte();
            }
        }
    }

    public final int[] transformationType;
    public final int[][] transformationList;
}
