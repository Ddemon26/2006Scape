// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

final class BZip2State {

	BZip2State() {
		byteCounts = new int[256];
		cumulativeCounts = new int[257];
		byteInUse = new boolean[256];
		rangeInUse = new boolean[16];
		symbolToByte = new byte[256];
		mtfBuffer = new byte[4096];
		mtfGroupStart = new int[16];
		selectors = new byte[18002];
		selectorMtf = new byte[18002];
		codeLengths = new byte[6][258];
		limit = new int[6][258];
		base = new int[6][258];
		perm = new int[6][258];
		minLengths = new int[6];
	}

	byte input[];
	int inputPos;
	int inputRemaining;
	int inputTotalLow;
	int inputTotalHigh;
	byte output[];
	int outputPos;
	int outputRemaining;
	int outputTotalLow;
	int outputTotalHigh;
	byte pendingByte;
	int pendingRepeat;
	boolean randomised;
	int bitBuffer;
	int bitCount;
	int blockSize100k;
	int blockCount;
	int originPointer;
	int nextIndex;
	int lookaheadByte;
	final int[] byteCounts;
	int blockPosition;
	final int[] cumulativeCounts;
	public static int inverseBwtTable[];
	int symbolsInUse;
	final boolean[] byteInUse;
	final boolean[] rangeInUse;
	final byte[] symbolToByte;
	final byte[] mtfBuffer;
	final int[] mtfGroupStart;
	final byte[] selectors;
	final byte[] selectorMtf;
	final byte[][] codeLengths;
	final int[][] limit;
	final int[][] base;
	final int[][] perm;
	final int[] minLengths;
	int blockLength;
}
