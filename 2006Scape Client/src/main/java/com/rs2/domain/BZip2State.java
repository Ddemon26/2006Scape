package com.rs2.domain;

// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

final class BZip2State {

        BZip2State() {
                unzftab = new int[256];
                cftab = new int[257];
                inUse = new boolean[256];
                inUse16 = new boolean[16];
                seqToUnseq = new byte[256];
                mtfa = new byte[4096];
                mtfbase = new int[16];
                selector = new byte[18002];
                selectorMtf = new byte[18002];
                tempLen = new byte[6][258];
                limit = new int[6][258];
                base = new int[6][258];
                perm = new int[6][258];
                minLens = new int[6];
	}

        byte[] input;
        int nextIn;
        int availIn;
        int totalInLo32;
        int totalInHi32;
        byte[] output;
        int nextOut;
        int outRemaining;
        int totalOutLo32;
        int totalOutHi32;
        byte stateOutCh;
        int stateOutLen;
        boolean blockRandomised;
        int bsBuff;
        int bsLive;
        int blockSize100k;
        int blockNo;
        int origPtr;
        int tPos;
        int k0;
        final int[] unzftab;
        int nblockUsed;
        final int[] cftab;
        public static int tt[];
        int nInUse;
        final boolean[] inUse;
        final boolean[] inUse16;
        final byte[] seqToUnseq;
        final byte[] mtfa;
        final int[] mtfbase;
        final byte[] selector;
        final byte[] selectorMtf;
        final byte[][] tempLen;
        final int[][] limit;
        final int[][] base;
        final int[][] perm;
        final int[] minLens;
        int saveNblock;
}
