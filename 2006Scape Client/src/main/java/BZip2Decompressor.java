// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

final class BZip2Decompressor {

    public static int decompress(byte abyte0[], int i, byte abyte1[], int j, int k) {
        synchronized (bzip2State) {
			bzip2State.aByteArray563 = abyte1;
			bzip2State.anInt564 = k;
			bzip2State.aByteArray568 = abyte0;
			bzip2State.anInt569 = 0;
			bzip2State.anInt565 = j;
			bzip2State.anInt570 = i;
			bzip2State.anInt577 = 0;
			bzip2State.anInt576 = 0;
			bzip2State.anInt566 = 0;
			bzip2State.anInt567 = 0;
			bzip2State.anInt571 = 0;
			bzip2State.anInt572 = 0;
			bzip2State.anInt579 = 0;
                        initBlock(bzip2State);
			i -= bzip2State.anInt570;
			return i;
		}
	}

private static void decodeBlock(BZip2State state) {
		byte byte4 = state.aByte573;
		int i = state.anInt574;
		int j = state.anInt584;
		int k = state.anInt582;
		int ai[] = BZip2State.anIntArray587;
		int l = state.anInt581;
		byte abyte0[] = state.aByteArray568;
		int i1 = state.anInt569;
		int j1 = state.anInt570;
		int k1 = j1;
		int l1 = state.anInt601 + 1;
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
		int i2 = state.anInt571;
		state.anInt571 += k1 - j1;
		if (state.anInt571 < i2) {
			state.anInt572++;
		}
		state.aByte573 = byte4;
		state.anInt574 = i;
		state.anInt584 = j;
		state.anInt582 = k;
		BZip2State.anIntArray587 = ai;
		state.anInt581 = l;
		state.aByteArray568 = abyte0;
		state.anInt569 = i1;
		state.anInt570 = j1;
	}

        private static void initBlock(BZip2State state) {
		int k8 = 0;
		int ai[] = null;
		int ai1[] = null;
		int ai2[] = null;
		state.anInt578 = 1;
		if (BZip2State.anIntArray587 == null) {
			BZip2State.anIntArray587 = new int[state.anInt578 * 0x186a0];
		}
		boolean flag19 = true;
		while (flag19) {
			byte byte0 = method228(state);
			if (byte0 == 23) {
				return;
			}
			byte0 = method228(state);
			byte0 = method228(state);
			byte0 = method228(state);
			byte0 = method228(state);
			byte0 = method228(state);
			state.anInt579++;
			byte0 = method228(state);
			byte0 = method228(state);
			byte0 = method228(state);
			byte0 = method228(state);
			byte0 = method229(state);
			state.aBoolean575 = byte0 != 0;
			if (state.aBoolean575) {
				System.out.println("PANIC! RANDOMISED BLOCK!");
			}
			state.anInt580 = 0;
			byte0 = method228(state);
			state.anInt580 = state.anInt580 << 8 | byte0 & 0xff;
			byte0 = method228(state);
			state.anInt580 = state.anInt580 << 8 | byte0 & 0xff;
			byte0 = method228(state);
			state.anInt580 = state.anInt580 << 8 | byte0 & 0xff;
			for (int j = 0; j < 16; j++) {
				byte byte1 = method229(state);
				state.aBooleanArray590[j] = byte1 == 1;
			}

			for (int k = 0; k < 256; k++) {
				state.aBooleanArray589[k] = false;
			}

			for (int l = 0; l < 16; l++) {
				if (state.aBooleanArray590[l]) {
					for (int i3 = 0; i3 < 16; i3++) {
						byte byte2 = method229(state);
						if (byte2 == 1) {
							state.aBooleanArray589[l * 16 + i3] = true;
						}
					}

				}
			}

			method231(state);
			int i4 = state.anInt588 + 2;
			int j4 = method230(3, state);
			int k4 = method230(15, state);
			for (int i1 = 0; i1 < k4; i1++) {
				int j3 = 0;
				do {
					byte byte3 = method229(state);
					if (byte3 == 0) {
						break;
					}
					j3++;
				} while (true);
				state.aByteArray595[i1] = (byte) j3;
			}

			byte abyte0[] = new byte[6];
			for (byte byte16 = 0; byte16 < j4; byte16++) {
				abyte0[byte16] = byte16;
			}

			for (int j1 = 0; j1 < k4; j1++) {
				byte byte17 = state.aByteArray595[j1];
				byte byte15 = abyte0[byte17];
				for (; byte17 > 0; byte17--) {
					abyte0[byte17] = abyte0[byte17 - 1];
				}

				abyte0[0] = byte15;
				state.aByteArray594[j1] = byte15;
			}

			for (int k3 = 0; k3 < j4; k3++) {
				int l6 = method230(5, state);
				for (int k1 = 0; k1 < i4; k1++) {
					do {
						byte byte4 = method229(state);
						if (byte4 == 0) {
							break;
						}
						byte4 = method229(state);
						if (byte4 == 0) {
							l6++;
						} else {
							l6--;
						}
					} while (true);
					state.aByteArrayArray596[k3][k1] = (byte) l6;
				}

			}

			for (int l3 = 0; l3 < j4; l3++) {
				byte byte8 = 32;
				int i = 0;
				for (int l1 = 0; l1 < i4; l1++) {
					if (state.aByteArrayArray596[l3][l1] > i) {
						i = state.aByteArrayArray596[l3][l1];
					}
					if (state.aByteArrayArray596[l3][l1] < byte8) {
						byte8 = state.aByteArrayArray596[l3][l1];
					}
				}

				method232(state.anIntArrayArray597[l3], state.anIntArrayArray598[l3], state.anIntArrayArray599[l3], state.aByteArrayArray596[l3], byte8, i, i4);
				state.anIntArray600[l3] = byte8;
			}

			int l4 = state.anInt588 + 1;
			int i5 = -1;
			int j5 = 0;
			for (int i2 = 0; i2 <= 255; i2++) {
				state.anIntArray583[i2] = 0;
			}

			int j9 = 4095;
			for (int l8 = 15; l8 >= 0; l8--) {
				for (int i9 = 15; i9 >= 0; i9--) {
					state.aByteArray592[j9] = (byte) (l8 * 16 + i9);
					j9--;
				}

				state.anIntArray593[l8] = j9 + 1;
			}

			int i6 = 0;
			if (j5 == 0) {
				i5++;
				j5 = 50;
				byte byte12 = state.aByteArray594[i5];
				k8 = state.anIntArray600[byte12];
				ai = state.anIntArrayArray597[byte12];
				ai2 = state.anIntArrayArray599[byte12];
				ai1 = state.anIntArrayArray598[byte12];
			}
			j5--;
			int i7 = k8;
			int l7;
			byte byte9;
			for (l7 = method230(i7, state); l7 > ai[i7]; l7 = l7 << 1 | byte9) {
				i7++;
				byte9 = method229(state);
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
							byte byte13 = state.aByteArray594[i5];
							k8 = state.anIntArray600[byte13];
							ai = state.anIntArrayArray597[byte13];
							ai2 = state.anIntArrayArray599[byte13];
							ai1 = state.anIntArrayArray598[byte13];
						}
						j5--;
						int j7 = k8;
						int i8;
						byte byte10;
						for (i8 = method230(j7, state); i8 > ai[j7]; i8 = i8 << 1 | byte10) {
							j7++;
							byte10 = method229(state);
						}

						k5 = ai2[i8 - ai1[j7]];
					} while (k5 == 0 || k5 == 1);
					j6++;
					byte byte5 = state.aByteArray591[state.aByteArray592[state.anIntArray593[0]] & 0xff];
					state.anIntArray583[byte5 & 0xff] += j6;
					for (; j6 > 0; j6--) {
						BZip2State.anIntArray587[i6] = byte5 & 0xff;
						i6++;
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
					state.anIntArray583[state.aByteArray591[byte6 & 0xff] & 0xff]++;
					BZip2State.anIntArray587[i6] = state.aByteArray591[byte6 & 0xff] & 0xff;
					i6++;
					if (j5 == 0) {
						i5++;
						j5 = 50;
						byte byte14 = state.aByteArray594[i5];
						k8 = state.anIntArray600[byte14];
						ai = state.anIntArrayArray597[byte14];
						ai2 = state.anIntArrayArray599[byte14];
						ai1 = state.anIntArrayArray598[byte14];
					}
					j5--;
					int k7 = k8;
					int j8;
					byte byte11;
					for (j8 = method230(k7, state); j8 > ai[k7]; j8 = j8 << 1 | byte11) {
						k7++;
						byte11 = method229(state);
					}

					k5 = ai2[j8 - ai1[k7]];
				}
			}

			state.anInt574 = 0;
			state.aByte573 = 0;
			state.anIntArray585[0] = 0;
			for (int j2 = 1; j2 <= 256; j2++) {
				state.anIntArray585[j2] = state.anIntArray583[j2 - 1];
			}

			for (int k2 = 1; k2 <= 256; k2++) {
				state.anIntArray585[k2] += state.anIntArray585[k2 - 1];
			}

			for (int l2 = 0; l2 < i6; l2++) {
				byte byte7 = (byte) (BZip2State.anIntArray587[l2] & 0xff);
				BZip2State.anIntArray587[state.anIntArray585[byte7 & 0xff]] |= l2 << 8;
				state.anIntArray585[byte7 & 0xff]++;
			}

			state.anInt581 = BZip2State.anIntArray587[state.anInt580] >> 8;
			state.anInt584 = 0;
			state.anInt581 = BZip2State.anIntArray587[state.anInt581];
			state.anInt582 = (byte) (state.anInt581 & 0xff);
			state.anInt581 >>= 8;
			state.anInt584++;
			state.anInt601 = i6;
                        decodeBlock(state);
			flag19 = state.anInt584 == state.anInt601 + 1 && state.anInt574 == 0;
		}
	}

	private static byte method228(BZip2State state) {
		return (byte) method230(8, state);
	}

	private static byte method229(BZip2State state) {
		return (byte) method230(1, state);
	}

	private static int method230(int i, BZip2State state) {
		int j;
		do {
			if (state.anInt577 >= i) {
				int k = state.anInt576 >> state.anInt577 - i & (1 << i) - 1;
				state.anInt577 -= i;
				j = k;
				break;
			}
			state.anInt576 = state.anInt576 << 8 | state.aByteArray563[state.anInt564] & 0xff;
			state.anInt577 += 8;
			state.anInt564++;
			state.anInt565--;
			state.anInt566++;
			if (state.anInt566 == 0) {
				state.anInt567++;
			}
		} while (true);
		return j;
	}

	private static void method231(BZip2State state) {
		state.anInt588 = 0;
		for (int i = 0; i < 256; i++) {
			if (state.aBooleanArray589[i]) {
				state.aByteArray591[state.anInt588] = (byte) i;
				state.anInt588++;
			}
		}

	}

	private static void method232(int ai[], int ai1[], int ai2[], byte abyte0[], int i, int j, int k) {
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

	private static final BZip2State bzip2State = new BZip2State();

}
