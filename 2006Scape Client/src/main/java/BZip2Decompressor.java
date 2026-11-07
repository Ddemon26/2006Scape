// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

final class BZip2Decompressor {

	public static int decompress(byte dest[], int destLength, byte src[], int srcLength, int srcOffset) {
		synchronized (sharedState) {
			sharedState.input = src;
			sharedState.inputPos = srcOffset;
			sharedState.output = dest;
			sharedState.outputPos = 0;
			sharedState.inputRemaining = srcLength;
			sharedState.outputRemaining = destLength;
			sharedState.bitCount = 0;
			sharedState.bitBuffer = 0;
			sharedState.inputTotalLow = 0;
			sharedState.inputTotalHigh = 0;
			sharedState.outputTotalLow = 0;
			sharedState.outputTotalHigh = 0;
			sharedState.blockCount = 0;
			decodeBlocks(sharedState);
			destLength -= sharedState.outputRemaining;
			return destLength;
		}
	}

	private static void emitBlockOutput(BZip2State state) {
		byte pendingByte = state.pendingByte;
		int pendingRepeat = state.pendingRepeat;
		int blockPosition = state.blockPosition;
		int lookaheadByte = state.lookaheadByte;
		int translationTable[] = BZip2State.inverseBwtTable;
		int nextIndex = state.nextIndex;
		byte output[] = state.output;
		int outputPos = state.outputPos;
		int outputRemaining = state.outputRemaining;
		int startOutputRemaining = outputRemaining;
		int blockEnd = state.blockLength + 1;
		label0 : do {
			if (pendingRepeat > 0) {
				do {
					if (outputRemaining == 0) {
						break label0;
					}
					if (pendingRepeat == 1) {
						break;
					}
					output[outputPos] = pendingByte;
					pendingRepeat--;
					outputPos++;
					outputRemaining--;
				} while (true);
				if (outputRemaining == 0) {
					pendingRepeat = 1;
					break;
				}
				output[outputPos] = pendingByte;
				outputPos++;
				outputRemaining--;
			}
			boolean flag = true;
			while (flag) {
				flag = false;
				if (blockPosition == blockEnd) {
					pendingRepeat = 0;
					break label0;
				}
				pendingByte = (byte) lookaheadByte;
				nextIndex = translationTable[nextIndex];
				byte nextByte = (byte) (nextIndex & 0xff);
				nextIndex >>= 8;
				blockPosition++;
				if (nextByte != lookaheadByte) {
					lookaheadByte = nextByte;
					if (outputRemaining == 0) {
						pendingRepeat = 1;
					} else {
						output[outputPos] = pendingByte;
						outputPos++;
						outputRemaining--;
						flag = true;
						continue;
					}
					break label0;
				}
				if (blockPosition != blockEnd) {
					continue;
				}
				if (outputRemaining == 0) {
					pendingRepeat = 1;
					break label0;
				}
				output[outputPos] = pendingByte;
				outputPos++;
				outputRemaining--;
				flag = true;
			}
			pendingRepeat = 2;
			nextIndex = translationTable[nextIndex];
			byte secondByte = (byte) (nextIndex & 0xff);
			nextIndex >>= 8;
			if (++blockPosition != blockEnd) {
				if (secondByte != lookaheadByte) {
					lookaheadByte = secondByte;
				} else {
					pendingRepeat = 3;
					nextIndex = translationTable[nextIndex];
					byte thirdByte = (byte) (nextIndex & 0xff);
					nextIndex >>= 8;
					if (++blockPosition != blockEnd) {
						if (thirdByte != lookaheadByte) {
							lookaheadByte = thirdByte;
						} else {
							nextIndex = translationTable[nextIndex];
							byte runCountByte = (byte) (nextIndex & 0xff);
							nextIndex >>= 8;
							blockPosition++;
							pendingRepeat = (runCountByte & 0xff) + 4;
							nextIndex = translationTable[nextIndex];
							lookaheadByte = (byte) (nextIndex & 0xff);
							nextIndex >>= 8;
							blockPosition++;
						}
					}
				}
			}
		} while (true);
		int previousTotalLow = state.outputTotalLow;
		state.outputTotalLow += startOutputRemaining - outputRemaining;
		if (state.outputTotalLow < previousTotalLow) {
			state.outputTotalHigh++;
		}
		state.pendingByte = pendingByte;
		state.pendingRepeat = pendingRepeat;
		state.blockPosition = blockPosition;
		state.lookaheadByte = lookaheadByte;
		BZip2State.inverseBwtTable = translationTable;
		state.nextIndex = nextIndex;
		state.output = output;
		state.outputPos = outputPos;
		state.outputRemaining = outputRemaining;
	}

	private static void decodeBlocks(BZip2State state) {
		int minLen = 0;
		int limit[] = null;
		int base[] = null;
		int perm[] = null;
		state.blockSize100k = 1;
		if (BZip2State.inverseBwtTable == null) {
			BZip2State.inverseBwtTable = new int[state.blockSize100k * 0x186a0];
		}
		boolean blockFullyEmitted = true;
		while (blockFullyEmitted) {
			byte headerByte = readByte(state);
			if (headerByte == 23) {
				return;
			}
			headerByte = readByte(state);
			headerByte = readByte(state);
			headerByte = readByte(state);
			headerByte = readByte(state);
			headerByte = readByte(state);
			state.blockCount++;
			headerByte = readByte(state);
			headerByte = readByte(state);
			headerByte = readByte(state);
			headerByte = readByte(state);
			headerByte = readBit(state);
			state.randomised = headerByte != 0;
			if (state.randomised) {
				System.out.println("PANIC! RANDOMISED BLOCK!");
			}
			state.originPointer = 0;
			headerByte = readByte(state);
			state.originPointer = state.originPointer << 8 | headerByte & 0xff;
			headerByte = readByte(state);
			state.originPointer = state.originPointer << 8 | headerByte & 0xff;
			headerByte = readByte(state);
			state.originPointer = state.originPointer << 8 | headerByte & 0xff;
			for (int rangeIndex = 0; rangeIndex < 16; rangeIndex++) {
				byte rangeBit = readBit(state);
				state.rangeInUse[rangeIndex] = rangeBit == 1;
			}

			for (int byteValue = 0; byteValue < 256; byteValue++) {
				state.byteInUse[byteValue] = false;
			}

			for (int rangeIndex = 0; rangeIndex < 16; rangeIndex++) {
				if (state.rangeInUse[rangeIndex]) {
					for (int offsetInRange = 0; offsetInRange < 16; offsetInRange++) {
						byte presenceBit = readBit(state);
						if (presenceBit == 1) {
							state.byteInUse[rangeIndex * 16 + offsetInRange] = true;
						}
					}

				}
			}

			buildSymbolMap(state);
			int alphaSize = state.symbolsInUse + 2;
			int tableCount = readBits(3, state);
			int selectorCount = readBits(15, state);
			for (int selectorIndex = 0; selectorIndex < selectorCount; selectorIndex++) {
				int selectorMtfValue = 0;
				do {
					byte unaryBit = readBit(state);
					if (unaryBit == 0) {
						break;
					}
					selectorMtfValue++;
				} while (true);
				state.selectorMtf[selectorIndex] = (byte) selectorMtfValue;
			}

			byte mtfOrder[] = new byte[6];
			for (byte tableIndex = 0; tableIndex < tableCount; tableIndex++) {
				mtfOrder[tableIndex] = tableIndex;
			}

			for (int selectorIndex = 0; selectorIndex < selectorCount; selectorIndex++) {
				byte mtfPosition = state.selectorMtf[selectorIndex];
				byte selectedTable = mtfOrder[mtfPosition];
				for (; mtfPosition > 0; mtfPosition--) {
					mtfOrder[mtfPosition] = mtfOrder[mtfPosition - 1];
				}

				mtfOrder[0] = selectedTable;
				state.selectors[selectorIndex] = selectedTable;
			}

			for (int tableIndex = 0; tableIndex < tableCount; tableIndex++) {
				int codeLength = readBits(5, state);
				for (int symbolIndex = 0; symbolIndex < alphaSize; symbolIndex++) {
					do {
						byte lengthBit = readBit(state);
						if (lengthBit == 0) {
							break;
						}
						lengthBit = readBit(state);
						if (lengthBit == 0) {
							codeLength++;
						} else {
							codeLength--;
						}
					} while (true);
					state.codeLengths[tableIndex][symbolIndex] = (byte) codeLength;
				}

			}

			for (int tableIndex = 0; tableIndex < tableCount; tableIndex++) {
				byte tableMinLen = 32;
				int tableMaxLen = 0;
				for (int symbolIndex = 0; symbolIndex < alphaSize; symbolIndex++) {
					if (state.codeLengths[tableIndex][symbolIndex] > tableMaxLen) {
						tableMaxLen = state.codeLengths[tableIndex][symbolIndex];
					}
					if (state.codeLengths[tableIndex][symbolIndex] < tableMinLen) {
						tableMinLen = state.codeLengths[tableIndex][symbolIndex];
					}
				}

				buildDecodeTables(state.limit[tableIndex], state.base[tableIndex], state.perm[tableIndex], state.codeLengths[tableIndex], tableMinLen, tableMaxLen, alphaSize);
				state.minLengths[tableIndex] = tableMinLen;
			}

			int endOfBlockSymbol = state.symbolsInUse + 1;
			int i5 = -1;
			int j5 = 0;
			for (int byteValue = 0; byteValue <= 255; byteValue++) {
				state.byteCounts[byteValue] = 0;
			}

			int j9 = 4095;
			for (int l8 = 15; l8 >= 0; l8--) {
				for (int i9 = 15; i9 >= 0; i9--) {
					state.aByteArray592[j9] = (byte) (l8 * 16 + i9);
					j9--;
				}

				state.anIntArray593[l8] = j9 + 1;
			}

			int blockLength = 0;
			if (j5 == 0) {
				i5++;
				j5 = 50;
				byte byte12 = state.selectors[i5];
				minLen = state.minLengths[byte12];
				limit = state.limit[byte12];
				perm = state.perm[byte12];
				base = state.base[byte12];
			}
			j5--;
			int i7 = minLen;
			int l7;
			byte byte9;
			for (l7 = readBits(i7, state); l7 > limit[i7]; l7 = l7 << 1 | byte9) {
				i7++;
				byte9 = readBit(state);
			}

			for (int k5 = perm[l7 - base[i7]]; k5 != endOfBlockSymbol;) {
				if (k5 == 0 || k5 == 1) {
					int j6 = -1;
					int k6 = 1;
					do {
						if (k5 == 0) {
							j6 += k6;
						} else if (k5 == 1) {
							j6 += 2 * k6;
						}
						k6 *= 2;
						if (j5 == 0) {
							i5++;
							j5 = 50;
							byte byte13 = state.selectors[i5];
							minLen = state.minLengths[byte13];
							limit = state.limit[byte13];
							perm = state.perm[byte13];
							base = state.base[byte13];
						}
						j5--;
						int j7 = minLen;
						int i8;
						byte byte10;
						for (i8 = readBits(j7, state); i8 > limit[j7]; i8 = i8 << 1 | byte10) {
							j7++;
							byte10 = readBit(state);
						}

						k5 = perm[i8 - base[j7]];
					} while (k5 == 0 || k5 == 1);
					j6++;
					byte byte5 = state.symbolToByte[state.aByteArray592[state.anIntArray593[0]] & 0xff];
					state.byteCounts[byte5 & 0xff] += j6;
					for (; j6 > 0; j6--) {
						BZip2State.inverseBwtTable[blockLength] = byte5 & 0xff;
						blockLength++;
					}

				} else {
					int j11 = k5 - 1;
					byte byte6;
					if (j11 < 16) {
						int j10 = state.anIntArray593[0];
						byte6 = state.aByteArray592[j10 + j11];
						for (; j11 > 3; j11 -= 4) {
							int k11 = j10 + j11;
							state.aByteArray592[k11] = state.aByteArray592[k11 - 1];
							state.aByteArray592[k11 - 1] = state.aByteArray592[k11 - 2];
							state.aByteArray592[k11 - 2] = state.aByteArray592[k11 - 3];
							state.aByteArray592[k11 - 3] = state.aByteArray592[k11 - 4];
						}

						for (; j11 > 0; j11--) {
							state.aByteArray592[j10 + j11] = state.aByteArray592[j10 + j11 - 1];
						}

						state.aByteArray592[j10] = byte6;
					} else {
						int l10 = j11 / 16;
						int i11 = j11 % 16;
						int k10 = state.anIntArray593[l10] + i11;
						byte6 = state.aByteArray592[k10];
						for (; k10 > state.anIntArray593[l10]; k10--) {
							state.aByteArray592[k10] = state.aByteArray592[k10 - 1];
						}

						state.anIntArray593[l10]++;
						for (; l10 > 0; l10--) {
							state.anIntArray593[l10]--;
							state.aByteArray592[state.anIntArray593[l10]] = state.aByteArray592[state.anIntArray593[l10 - 1] + 16 - 1];
						}

						state.anIntArray593[0]--;
						state.aByteArray592[state.anIntArray593[0]] = byte6;
						if (state.anIntArray593[0] == 0) {
							int i10 = 4095;
							for (int k9 = 15; k9 >= 0; k9--) {
								for (int l9 = 15; l9 >= 0; l9--) {
									state.aByteArray592[i10] = state.aByteArray592[state.anIntArray593[k9] + l9];
									i10--;
								}

								state.anIntArray593[k9] = i10 + 1;
							}

						}
					}
					state.byteCounts[state.symbolToByte[byte6 & 0xff] & 0xff]++;
					BZip2State.inverseBwtTable[blockLength] = state.symbolToByte[byte6 & 0xff] & 0xff;
					blockLength++;
					if (j5 == 0) {
						i5++;
						j5 = 50;
						byte byte14 = state.selectors[i5];
						minLen = state.minLengths[byte14];
						limit = state.limit[byte14];
						perm = state.perm[byte14];
						base = state.base[byte14];
					}
					j5--;
					int k7 = minLen;
					int j8;
					byte byte11;
					for (j8 = readBits(k7, state); j8 > limit[k7]; j8 = j8 << 1 | byte11) {
						k7++;
						byte11 = readBit(state);
					}

					k5 = perm[j8 - base[k7]];
				}
			}

			state.pendingRepeat = 0;
			state.pendingByte = 0;
			state.cumulativeCounts[0] = 0;
			for (int byteValue = 1; byteValue <= 256; byteValue++) {
				state.cumulativeCounts[byteValue] = state.byteCounts[byteValue - 1];
			}

			for (int byteValue = 1; byteValue <= 256; byteValue++) {
				state.cumulativeCounts[byteValue] += state.cumulativeCounts[byteValue - 1];
			}

			for (int blockIndex = 0; blockIndex < blockLength; blockIndex++) {
				byte sortedByte = (byte) (BZip2State.inverseBwtTable[blockIndex] & 0xff);
				BZip2State.inverseBwtTable[state.cumulativeCounts[sortedByte & 0xff]] |= blockIndex << 8;
				state.cumulativeCounts[sortedByte & 0xff]++;
			}

			state.nextIndex = BZip2State.inverseBwtTable[state.originPointer] >> 8;
			state.blockPosition = 0;
			state.nextIndex = BZip2State.inverseBwtTable[state.nextIndex];
			state.lookaheadByte = (byte) (state.nextIndex & 0xff);
			state.nextIndex >>= 8;
			state.blockPosition++;
			state.blockLength = blockLength;
			emitBlockOutput(state);
			blockFullyEmitted = state.blockPosition == state.blockLength + 1 && state.pendingRepeat == 0;
		}
	}

	private static byte readByte(BZip2State state) {
		return (byte) readBits(8, state);
	}

	private static byte readBit(BZip2State state) {
		return (byte) readBits(1, state);
	}

	private static int readBits(int count, BZip2State state) {
		int result;
		do {
			if (state.bitCount >= count) {
				int bits = state.bitBuffer >> state.bitCount - count & (1 << count) - 1;
				state.bitCount -= count;
				result = bits;
				break;
			}
			state.bitBuffer = state.bitBuffer << 8 | state.input[state.inputPos] & 0xff;
			state.bitCount += 8;
			state.inputPos++;
			state.inputRemaining--;
			state.inputTotalLow++;
			if (state.inputTotalLow == 0) {
				state.inputTotalHigh++;
			}
		} while (true);
		return result;
	}

	private static void buildSymbolMap(BZip2State state) {
		state.symbolsInUse = 0;
		for (int byteValue = 0; byteValue < 256; byteValue++) {
			if (state.byteInUse[byteValue]) {
				state.symbolToByte[state.symbolsInUse] = (byte) byteValue;
				state.symbolsInUse++;
			}
		}

	}

	private static void buildDecodeTables(int limit[], int base[], int perm[], byte codeLengths[], int minLen, int maxLen, int alphaSize) {
		int permIndex = 0;
		for (int codeLength = minLen; codeLength <= maxLen; codeLength++) {
			for (int symbol = 0; symbol < alphaSize; symbol++) {
				if (codeLengths[symbol] == codeLength) {
					perm[permIndex] = symbol;
					permIndex++;
				}
			}

		}

		for (int lengthIndex = 0; lengthIndex < 23; lengthIndex++) {
			base[lengthIndex] = 0;
		}

		for (int symbolIndex = 0; symbolIndex < alphaSize; symbolIndex++) {
			base[codeLengths[symbolIndex] + 1]++;
		}

		for (int lengthIndex = 1; lengthIndex < 23; lengthIndex++) {
			base[lengthIndex] += base[lengthIndex - 1];
		}

		for (int lengthIndex = 0; lengthIndex < 23; lengthIndex++) {
			limit[lengthIndex] = 0;
		}

		int codeValue = 0;
		for (int codeLength = minLen; codeLength <= maxLen; codeLength++) {
			codeValue += base[codeLength + 1] - base[codeLength];
			limit[codeLength] = codeValue - 1;
			codeValue <<= 1;
		}

		for (int codeLength = minLen + 1; codeLength <= maxLen; codeLength++) {
			base[codeLength] = (limit[codeLength - 1] + 1 << 1) - base[codeLength];
		}

	}

	private static final BZip2State sharedState = new BZip2State();

}
