// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) package com.rs2;

public final class ISAACRandomGen {

	public ISAACRandomGen(int seed[]) {
		memory = new int[256];
		results = new int[256];
		System.arraycopy(seed, 0, results, 0, seed.length);
		initializeKeySet();
	}

	public int getNextKey() {
		if (count-- == 0) {
			isaac();
			count = 255;
		}
		return results[count];
	}

	private void isaac() {
		lastResult += ++counter;
		for (int slot = 0; slot < 256; slot++) {
			int oldValue = memory[slot];
			if ((slot & 3) == 0) {
				accumulator ^= accumulator << 13;
			} else if ((slot & 3) == 1) {
				accumulator ^= accumulator >>> 6;
			} else if ((slot & 3) == 2) {
				accumulator ^= accumulator << 2;
			} else if ((slot & 3) == 3) {
				accumulator ^= accumulator >>> 16;
			}
			accumulator += memory[slot + 128 & 0xff];
			int newValue;
			memory[slot] = newValue = memory[(oldValue & 0x3fc) >> 2] + accumulator + lastResult;
			results[slot] = lastResult = memory[(newValue >> 8 & 0x3fc) >> 2] + oldValue;
		}

	}

	private void initializeKeySet() {
		int mixWord1;
		int mixWord2;
		int mixWord3;
		int mixWord4;
		int mixWord5;
		int mixWord6;
		int mixWord7;
		int mixWord0 = mixWord1 = mixWord2 = mixWord3 = mixWord4 = mixWord5 = mixWord6 = mixWord7 = 0x9e3779b9;
		for (int scrambleRound = 0; scrambleRound < 4; scrambleRound++) {
			mixWord0 ^= mixWord1 << 11;
			mixWord3 += mixWord0;
			mixWord1 += mixWord2;
			mixWord1 ^= mixWord2 >>> 2;
			mixWord4 += mixWord1;
			mixWord2 += mixWord3;
			mixWord2 ^= mixWord3 << 8;
			mixWord5 += mixWord2;
			mixWord3 += mixWord4;
			mixWord3 ^= mixWord4 >>> 16;
			mixWord6 += mixWord3;
			mixWord4 += mixWord5;
			mixWord4 ^= mixWord5 << 10;
			mixWord7 += mixWord4;
			mixWord5 += mixWord6;
			mixWord5 ^= mixWord6 >>> 4;
			mixWord0 += mixWord5;
			mixWord6 += mixWord7;
			mixWord6 ^= mixWord7 << 8;
			mixWord1 += mixWord6;
			mixWord7 += mixWord0;
			mixWord7 ^= mixWord0 >>> 9;
			mixWord2 += mixWord7;
			mixWord0 += mixWord1;
		}

		for (int seedBlock = 0; seedBlock < 256; seedBlock += 8) {
			mixWord0 += results[seedBlock];
			mixWord1 += results[seedBlock + 1];
			mixWord2 += results[seedBlock + 2];
			mixWord3 += results[seedBlock + 3];
			mixWord4 += results[seedBlock + 4];
			mixWord5 += results[seedBlock + 5];
			mixWord6 += results[seedBlock + 6];
			mixWord7 += results[seedBlock + 7];
			mixWord0 ^= mixWord1 << 11;
			mixWord3 += mixWord0;
			mixWord1 += mixWord2;
			mixWord1 ^= mixWord2 >>> 2;
			mixWord4 += mixWord1;
			mixWord2 += mixWord3;
			mixWord2 ^= mixWord3 << 8;
			mixWord5 += mixWord2;
			mixWord3 += mixWord4;
			mixWord3 ^= mixWord4 >>> 16;
			mixWord6 += mixWord3;
			mixWord4 += mixWord5;
			mixWord4 ^= mixWord5 << 10;
			mixWord7 += mixWord4;
			mixWord5 += mixWord6;
			mixWord5 ^= mixWord6 >>> 4;
			mixWord0 += mixWord5;
			mixWord6 += mixWord7;
			mixWord6 ^= mixWord7 << 8;
			mixWord1 += mixWord6;
			mixWord7 += mixWord0;
			mixWord7 ^= mixWord0 >>> 9;
			mixWord2 += mixWord7;
			mixWord0 += mixWord1;
			memory[seedBlock] = mixWord0;
			memory[seedBlock + 1] = mixWord1;
			memory[seedBlock + 2] = mixWord2;
			memory[seedBlock + 3] = mixWord3;
			memory[seedBlock + 4] = mixWord4;
			memory[seedBlock + 5] = mixWord5;
			memory[seedBlock + 6] = mixWord6;
			memory[seedBlock + 7] = mixWord7;
		}

		for (int memoryBlock = 0; memoryBlock < 256; memoryBlock += 8) {
			mixWord0 += memory[memoryBlock];
			mixWord1 += memory[memoryBlock + 1];
			mixWord2 += memory[memoryBlock + 2];
			mixWord3 += memory[memoryBlock + 3];
			mixWord4 += memory[memoryBlock + 4];
			mixWord5 += memory[memoryBlock + 5];
			mixWord6 += memory[memoryBlock + 6];
			mixWord7 += memory[memoryBlock + 7];
			mixWord0 ^= mixWord1 << 11;
			mixWord3 += mixWord0;
			mixWord1 += mixWord2;
			mixWord1 ^= mixWord2 >>> 2;
			mixWord4 += mixWord1;
			mixWord2 += mixWord3;
			mixWord2 ^= mixWord3 << 8;
			mixWord5 += mixWord2;
			mixWord3 += mixWord4;
			mixWord3 ^= mixWord4 >>> 16;
			mixWord6 += mixWord3;
			mixWord4 += mixWord5;
			mixWord4 ^= mixWord5 << 10;
			mixWord7 += mixWord4;
			mixWord5 += mixWord6;
			mixWord5 ^= mixWord6 >>> 4;
			mixWord0 += mixWord5;
			mixWord6 += mixWord7;
			mixWord6 ^= mixWord7 << 8;
			mixWord1 += mixWord6;
			mixWord7 += mixWord0;
			mixWord7 ^= mixWord0 >>> 9;
			mixWord2 += mixWord7;
			mixWord0 += mixWord1;
			memory[memoryBlock] = mixWord0;
			memory[memoryBlock + 1] = mixWord1;
			memory[memoryBlock + 2] = mixWord2;
			memory[memoryBlock + 3] = mixWord3;
			memory[memoryBlock + 4] = mixWord4;
			memory[memoryBlock + 5] = mixWord5;
			memory[memoryBlock + 6] = mixWord6;
			memory[memoryBlock + 7] = mixWord7;
		}

		isaac();
		count = 256;
	}

	private int count;
	private final int[] results;
	private final int[] memory;
	private int accumulator;
	private int lastResult;
	private int counter;
}
