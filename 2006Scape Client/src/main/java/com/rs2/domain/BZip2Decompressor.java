package com.rs2.domain;

// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

final class BZip2Decompressor {

       public static int decompress(byte[] outputBuffer, int outputLength, byte[] inputBuffer, int inputLength, int startIndex) {
               synchronized (state) {
                       state.input = inputBuffer;
                       state.nextIn = startIndex;
                       state.output = outputBuffer;
                       state.nextOut = 0;
                       state.availIn = inputLength;
                       state.outRemaining = outputLength;
                       state.bsLive = 0;
                       state.bsBuff = 0;
                       state.totalInLo32 = 0;
                       state.totalInHi32 = 0;
                       state.totalOutLo32 = 0;
                       state.totalOutHi32 = 0;
                       state.blockNo = 0;
                       decompressStream(state);
                       outputLength -= state.outRemaining;
                        return outputLength;
                }
        }

       private static void decompressBlock(BZip2State class32) {
                byte byte4 = class32.stateOutCh;
                int i = class32.stateOutLen;
                int j = class32.nblockUsed;
                int k = class32.k0;
                int ai[] = BZip2State.tt;
                int l = class32.tPos;
                byte abyte0[] = class32.output;
                int i1 = class32.nextOut;
                int j1 = class32.outRemaining;
                int k1 = j1;
                int l1 = class32.saveNblock + 1;
		label0 : do {
			if (i > 0) {
				do {
					if (j1 == 0) {
						break label0;
					}
					if (i == 1) {
						break;
					}
					abyte0[i1] = byte4;
					i--;
					i1++;
					j1--;
				} while (true);
				if (j1 == 0) {
					i = 1;
					break;
				}
				abyte0[i1] = byte4;
				i1++;
				j1--;
			}
			boolean flag = true;
			while (flag) {
				flag = false;
				if (j == l1) {
					i = 0;
					break label0;
				}
				byte4 = (byte) k;
				l = ai[l];
				byte byte0 = (byte) (l & 0xff);
				l >>= 8;
				j++;
				if (byte0 != k) {
					k = byte0;
					if (j1 == 0) {
						i = 1;
					} else {
						abyte0[i1] = byte4;
						i1++;
						j1--;
						flag = true;
						continue;
					}
					break label0;
				}
				if (j != l1) {
					continue;
				}
				if (j1 == 0) {
					i = 1;
					break label0;
				}
				abyte0[i1] = byte4;
				i1++;
				j1--;
				flag = true;
			}
			i = 2;
			l = ai[l];
			byte byte1 = (byte) (l & 0xff);
			l >>= 8;
			if (++j != l1) {
				if (byte1 != k) {
					k = byte1;
				} else {
					i = 3;
					l = ai[l];
					byte byte2 = (byte) (l & 0xff);
					l >>= 8;
					if (++j != l1) {
						if (byte2 != k) {
							k = byte2;
						} else {
							l = ai[l];
							byte byte3 = (byte) (l & 0xff);
							l >>= 8;
							j++;
							i = (byte3 & 0xff) + 4;
							l = ai[l];
							k = (byte) (l & 0xff);
							l >>= 8;
							j++;
						}
					}
				}
			}
		} while (true);
                int i2 = class32.totalOutLo32;
                class32.totalOutLo32 += k1 - j1;
                if (class32.totalOutLo32 < i2) {
                        class32.totalOutHi32++;
                }
                class32.stateOutCh = byte4;
                class32.stateOutLen = i;
                class32.nblockUsed = j;
                class32.k0 = k;
                BZip2State.tt = ai;
                class32.tPos = l;
                class32.output = abyte0;
                class32.nextOut = i1;
                class32.outRemaining = j1;
	}

       private static void decompressStream(BZip2State class32) {
		int k8 = 0;
		int ai[] = null;
		int ai1[] = null;
		int ai2[] = null;
                class32.blockSize100k = 1;
                if (BZip2State.tt == null) {
                        BZip2State.tt = new int[class32.blockSize100k * 0x186a0];
		}
		boolean flag19 = true;
		while (flag19) {
			byte byte0 = readByte(class32);
			if (byte0 == 23) {
				return;
			}
                        byte0 = readByte(class32);
                        byte0 = readByte(class32);
                        byte0 = readByte(class32);
                        byte0 = readByte(class32);
                        byte0 = readByte(class32);
                        class32.blockNo++;
			byte0 = readByte(class32);
			byte0 = readByte(class32);
			byte0 = readByte(class32);
			byte0 = readByte(class32);
                        byte0 = readBit(class32);
                        class32.blockRandomised = byte0 != 0;
                        if (class32.blockRandomised) {
				System.out.println("PANIC! RANDOMISED BLOCK!");
			}
                        class32.origPtr = 0;
                        byte0 = readByte(class32);
                        class32.origPtr = class32.origPtr << 8 | byte0 & 0xff;
                        byte0 = readByte(class32);
                        class32.origPtr = class32.origPtr << 8 | byte0 & 0xff;
                        byte0 = readByte(class32);
                        class32.origPtr = class32.origPtr << 8 | byte0 & 0xff;
                        for (int j = 0; j < 16; j++) {
                                byte byte1 = readBit(class32);
                                class32.inUse16[j] = byte1 == 1;
                        }

                        for (int k = 0; k < 256; k++) {
                                class32.inUse[k] = false;
                        }

			for (int l = 0; l < 16; l++) {
                                if (class32.inUse16[l]) {
					for (int i3 = 0; i3 < 16; i3++) {
						byte byte2 = readBit(class32);
                                                if (byte2 == 1) {
                                                        class32.inUse[l * 16 + i3] = true;
                                                }
					}

				}
			}

                        makeMaps(class32);
                        int i4 = class32.nInUse + 2;
			int j4 = readBits(3, class32);
			int k4 = readBits(15, class32);
			for (int i1 = 0; i1 < k4; i1++) {
				int j3 = 0;
				do {
					byte byte3 = readBit(class32);
					if (byte3 == 0) {
						break;
					}
					j3++;
				} while (true);
                                class32.selectorMtf[i1] = (byte) j3;
			}

			byte abyte0[] = new byte[6];
			for (byte byte16 = 0; byte16 < j4; byte16++) {
				abyte0[byte16] = byte16;
			}

			for (int j1 = 0; j1 < k4; j1++) {
                                byte byte17 = class32.selectorMtf[j1];
				byte byte15 = abyte0[byte17];
				for (; byte17 > 0; byte17--) {
					abyte0[byte17] = abyte0[byte17 - 1];
				}

                                abyte0[0] = byte15;
                                class32.selector[j1] = byte15;
			}

			for (int k3 = 0; k3 < j4; k3++) {
				int l6 = readBits(5, class32);
				for (int k1 = 0; k1 < i4; k1++) {
					do {
						byte byte4 = readBit(class32);
						if (byte4 == 0) {
							break;
						}
						byte4 = readBit(class32);
						if (byte4 == 0) {
							l6++;
						} else {
							l6--;
						}
					} while (true);
                                        class32.tempLen[k3][k1] = (byte) l6;
				}

			}

			for (int l3 = 0; l3 < j4; l3++) {
				byte byte8 = 32;
				int i = 0;
				for (int l1 = 0; l1 < i4; l1++) {
                                        if (class32.tempLen[l3][l1] > i) {
                                                i = class32.tempLen[l3][l1];
					}
                                        if (class32.tempLen[l3][l1] < byte8) {
                                                byte8 = class32.tempLen[l3][l1];
					}
				}

                                createHuffmanTables(class32.limit[l3], class32.base[l3], class32.perm[l3], class32.tempLen[l3], byte8, i, i4);
                                class32.minLens[l3] = byte8;
			}

                        int l4 = class32.nInUse + 1;
			int i5 = -1;
			int j5 = 0;
			for (int i2 = 0; i2 <= 255; i2++) {
                            class32.unzftab[i2] = 0;
			}

			int j9 = 4095;
			for (int l8 = 15; l8 >= 0; l8--) {
				for (int i9 = 15; i9 >= 0; i9--) {
                                        class32.mtfa[j9] = (byte) (l8 * 16 + i9);
					j9--;
				}

                                class32.mtfbase[l8] = j9 + 1;
			}

			int i6 = 0;
			if (j5 == 0) {
				i5++;
				j5 = 50;
                                byte byte12 = class32.selector[i5];
                                k8 = class32.minLens[byte12];
                                ai = class32.limit[byte12];
                                ai2 = class32.perm[byte12];
                                ai1 = class32.base[byte12];
			}
			j5--;
			int i7 = k8;
			int l7;
			byte byte9;
			for (l7 = readBits(i7, class32); l7 > ai[i7]; l7 = l7 << 1 | byte9) {
				i7++;
				byte9 = readBit(class32);
			}

			for (int k5 = ai2[l7 - ai1[i7]]; k5 != l4;) {
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
                                                        byte byte13 = class32.selector[i5];
                                                        k8 = class32.minLens[byte13];
                                                        ai = class32.limit[byte13];
                                                        ai2 = class32.perm[byte13];
                                                        ai1 = class32.base[byte13];
						}
						j5--;
						int j7 = k8;
						int i8;
						byte byte10;
						for (i8 = readBits(j7, class32); i8 > ai[j7]; i8 = i8 << 1 | byte10) {
							j7++;
							byte10 = readBit(class32);
						}

						k5 = ai2[i8 - ai1[j7]];
					} while (k5 == 0 || k5 == 1);
					j6++;
                                        byte byte5 = class32.seqToUnseq[class32.mtfa[class32.mtfbase[0]] & 0xff];
                                    class32.unzftab[byte5 & 0xff] += j6;
					for (; j6 > 0; j6--) {
                                                BZip2State.tt[i6] = byte5 & 0xff;
						i6++;
					}

				} else {
					int j11 = k5 - 1;
					byte byte6;
					if (j11 < 16) {
                                                int j10 = class32.mtfbase[0];
                                                byte6 = class32.mtfa[j10 + j11];
						for (; j11 > 3; j11 -= 4) {
							int k11 = j10 + j11;
                                                        class32.mtfa[k11] = class32.mtfa[k11 - 1];
                                                        class32.mtfa[k11 - 1] = class32.mtfa[k11 - 2];
                                                        class32.mtfa[k11 - 2] = class32.mtfa[k11 - 3];
                                                        class32.mtfa[k11 - 3] = class32.mtfa[k11 - 4];
						}

						for (; j11 > 0; j11--) {
                                                        class32.mtfa[j10 + j11] = class32.mtfa[j10 + j11 - 1];
						}

                                                class32.mtfa[j10] = byte6;
					} else {
						int l10 = j11 / 16;
						int i11 = j11 % 16;
                                                int k10 = class32.mtfbase[l10] + i11;
                                                byte6 = class32.mtfa[k10];
                                                for (; k10 > class32.mtfbase[l10]; k10--) {
                                                        class32.mtfa[k10] = class32.mtfa[k10 - 1];
						}

                                                class32.mtfbase[l10]++;
                                                for (; l10 > 0; l10--) {
                                                        class32.mtfbase[l10]--;
                                                        class32.mtfa[class32.mtfbase[l10]] = class32.mtfa[class32.mtfbase[l10 - 1] + 16 - 1];
                                                }

                                                class32.mtfbase[0]--;
                                                class32.mtfa[class32.mtfbase[0]] = byte6;
                                                if (class32.mtfbase[0] == 0) {
							int i10 = 4095;
							for (int k9 = 15; k9 >= 0; k9--) {
								for (int l9 = 15; l9 >= 0; l9--) {
                                                                        class32.mtfa[i10] = class32.mtfa[class32.mtfbase[k9] + l9];
									i10--;
								}

                                                                class32.mtfbase[k9] = i10 + 1;
							}

						}
					}
                                    class32.unzftab[class32.seqToUnseq[byte6 & 0xff] & 0xff]++;
                                        BZip2State.tt[i6] = class32.seqToUnseq[byte6 & 0xff] & 0xff;
					i6++;
					if (j5 == 0) {
						i5++;
						j5 = 50;
                                                byte byte14 = class32.selector[i5];
                                                k8 = class32.minLens[byte14];
                                                ai = class32.limit[byte14];
                                                ai2 = class32.perm[byte14];
                                                ai1 = class32.base[byte14];
					}
					j5--;
					int k7 = k8;
					int j8;
					byte byte11;
					for (j8 = readBits(k7, class32); j8 > ai[k7]; j8 = j8 << 1 | byte11) {
						k7++;
						byte11 = readBit(class32);
					}

					k5 = ai2[j8 - ai1[k7]];
				}
			}

                        class32.stateOutLen = 0;
                        class32.stateOutCh = 0;
                        class32.cftab[0] = 0;
			for (int j2 = 1; j2 <= 256; j2++) {
                            class32.cftab[j2] = class32.unzftab[j2 - 1];
			}

                        for (int k2 = 1; k2 <= 256; k2++) {
                                class32.cftab[k2] += class32.cftab[k2 - 1];
                        }

			for (int l2 = 0; l2 < i6; l2++) {
                                byte byte7 = (byte) (BZip2State.tt[l2] & 0xff);
                                BZip2State.tt[class32.cftab[byte7 & 0xff]] |= l2 << 8;
                                class32.cftab[byte7 & 0xff]++;
			}

                        class32.tPos = BZip2State.tt[class32.origPtr] >> 8;
                        class32.nblockUsed = 0;
                        class32.tPos = BZip2State.tt[class32.tPos];
                        class32.k0 = (byte) (class32.tPos & 0xff);
                        class32.tPos >>= 8;
                        class32.nblockUsed++;
                        class32.saveNblock = i6;
                        decompressBlock(class32);
                        flag19 = class32.nblockUsed == class32.saveNblock + 1 && class32.stateOutLen == 0;
		}
	}

	private static byte readByte(BZip2State class32) {
		return (byte) readBits(8, class32);
	}

	private static byte readBit(BZip2State class32) {
		return (byte) readBits(1, class32);
	}

	private static int readBits(int i, BZip2State class32) {
		int j;
		do {
                        if (class32.bsLive >= i) {
                                int k = class32.bsBuff >> class32.bsLive - i & (1 << i) - 1;
                                class32.bsLive -= i;
				j = k;
				break;
			}
                        class32.bsBuff = class32.bsBuff << 8 | class32.input[class32.nextIn] & 0xff;
                        class32.bsLive += 8;
                        class32.nextIn++;
                        class32.availIn--;
                        class32.totalInLo32++;
                        if (class32.totalInLo32 == 0) {
                                class32.totalInHi32++;
                        }
                } while (true);
		return j;
	}

        private static void makeMaps(BZip2State class32) {
                class32.nInUse = 0;
                for (int i = 0; i < 256; i++) {
                        if (class32.inUse[i]) {
                                class32.seqToUnseq[class32.nInUse] = (byte) i;
                                class32.nInUse++;
                        }
                }

	}

	private static void createHuffmanTables(int ai[], int ai1[], int ai2[], byte abyte0[], int i, int j, int k) {
		int l = 0;
		for (int i1 = i; i1 <= j; i1++) {
			for (int l2 = 0; l2 < k; l2++) {
				if (abyte0[l2] == i1) {
					ai2[l] = l2;
					l++;
				}
			}

		}

		for (int j1 = 0; j1 < 23; j1++) {
			ai1[j1] = 0;
		}

		for (int k1 = 0; k1 < k; k1++) {
			ai1[abyte0[k1] + 1]++;
		}

		for (int l1 = 1; l1 < 23; l1++) {
			ai1[l1] += ai1[l1 - 1];
		}

		for (int i2 = 0; i2 < 23; i2++) {
			ai[i2] = 0;
		}

		int i3 = 0;
		for (int j2 = i; j2 <= j; j2++) {
			i3 += ai1[j2 + 1] - ai1[j2];
			ai[j2] = i3 - 1;
			i3 <<= 1;
		}

		for (int k2 = i + 1; k2 <= j; k2++) {
			ai1[k2] = (ai[k2 - 1] + 1 << 1) - ai1[k2];
		}

	}

    private static final BZip2State state = new BZip2State();

}
