// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

final class Censor {

	public static void loadConfig(StreamLoader streamLoader) {
		Stream stream = new Stream(streamLoader.getDataForName("fragmentsenc.txt"));
		Stream stream_1 = new Stream(streamLoader.getDataForName("badenc.txt"));
		Stream stream_2 = new Stream(streamLoader.getDataForName("domainenc.txt"));
		Stream stream_3 = new Stream(streamLoader.getDataForName("tldlist.txt"));
		readValues(stream, stream_1, stream_2, stream_3);
	}

	private static void readValues(Stream stream, Stream stream_1, Stream stream_2, Stream stream_3) {
		readBadEnc(stream_1);
		readDomainEnc(stream_2);
		readFragmentsEnc(stream);
		readTldList(stream_3);
	}

	private static void readTldList(Stream stream) {
		int i = stream.readDWord();
                topLevelDomains = new char[i][];
                tldBehavior = new int[i];
		for (int j = 0; j < i; j++) {
                        tldBehavior[j] = stream.readUnsignedByte();
			char ac[] = new char[stream.readUnsignedByte()];
			for (int k = 0; k < ac.length; k++) {
				ac[k] = (char) stream.readUnsignedByte();
			}

                        topLevelDomains[j] = ac;
		}

	}

	private static void readBadEnc(Stream stream) {
		int j = stream.readDWord();
                badWords = new char[j][];
                badWordPatterns = new byte[j][][];
                loadBannedWords(stream, badWords, badWordPatterns);
	}

	private static void readDomainEnc(Stream stream) {
		int i = stream.readDWord();
                domainWords = new char[i][];
                readCharArrayTable(domainWords, stream);
	}

	private static void readFragmentsEnc(Stream stream) {
                bannedNameHashes = new int[stream.readDWord()];
                for (int i = 0; i < bannedNameHashes.length; i++) {
                        bannedNameHashes[i] = stream.readUnsignedWord();
                }
	}

        private static void loadBannedWords(Stream stream, char ac[][], byte abyte0[][][]) {
		for (int j = 0; j < ac.length; j++) {
			char ac1[] = new char[stream.readUnsignedByte()];
			for (int k = 0; k < ac1.length; k++) {
				ac1[k] = (char) stream.readUnsignedByte();
			}

			ac[j] = ac1;
			byte abyte1[][] = new byte[stream.readUnsignedByte()][2];
			for (int l = 0; l < abyte1.length; l++) {
				abyte1[l][0] = (byte) stream.readUnsignedByte();
				abyte1[l][1] = (byte) stream.readUnsignedByte();
			}

			if (abyte1.length > 0) {
				abyte0[j] = abyte1;
			}
		}

	}

        private static void readCharArrayTable(char ac[][], Stream stream) {
		for (int j = 0; j < ac.length; j++) {
			char ac1[] = new char[stream.readUnsignedByte()];
			for (int k = 0; k < ac1.length; k++) {
				ac1[k] = (char) stream.readUnsignedByte();
			}

			ac[j] = ac1;
		}

	}

        private static void sanitizeInput(char ac[]) {
		int i = 0;
		for (int j = 0; j < ac.length; j++) {
                        if (isAllowedCharacter(ac[j])) {
				ac[i] = ac[j];
			} else {
				ac[i] = ' ';
			}
			if (i == 0 || ac[i] != ' ' || ac[i - 1] != ' ') {
				i++;
			}
		}
		for (int k = i; k < ac.length; k++) {
			ac[k] = ' ';
		}

	}

        private static boolean isAllowedCharacter(char c) {
		return c >= ' ' && c <= '\177' || c == ' ' || c == '\n' || c == '\t' || c == '\243' || c == '\u20AC';
	}

	public static String doCensor(String s) {
		System.currentTimeMillis();
		char ac[] = s.toCharArray();
                sanitizeInput(ac);
		String s1 = new String(ac).trim();
		ac = s1.toLowerCase().toCharArray();
		String s2 = s1.toLowerCase();
                censorTopLevelDomains(ac);
                censorWords(ac);
                censorDomains(ac);
            censorLongNumbers(ac);
		for (String exception : exceptions) {
			for (int k = -1; (k = s2.indexOf(exception, k + 1)) != -1;) {
				char ac1[] = exception.toCharArray();
				System.arraycopy(ac1, 0, ac, k, ac1.length);

			}

		}
                restoreCapitalization(s1.toCharArray(), ac);
                fixSentenceCase(ac);
		System.currentTimeMillis();
		return s; // xxx chat filter, return s to avoid new String(ac).trim()
	}

        private static void restoreCapitalization(char original[], char filtered[]) {
                for (int j = 0; j < original.length; j++) {
                        if (filtered[j] != '*' && isUpperCaseLetter(original[j])) {
                                filtered[j] = original[j];
                        }
                }
        }

        private static void fixSentenceCase(char ac[]) {
		boolean flag = true;
		for (int j = 0; j < ac.length; j++) {
			char c = ac[j];
			if (isLetter(c)) {
				if (flag) {
					if (isLowerCaseLetter(c)) {
						flag = false;
					}
				} else if (isUpperCaseLetter(c)) {
					ac[j] = (char) (c + 97 - 65);
				}
			} else {
				flag = true;
			}
		}
	}

        private static void censorWords(char ac[]) {
                for (int i = 0; i < 2; i++) {
                        for (int j = badWords.length - 1; j >= 0; j--) {
                                applyWordFilter(badWordPatterns[j], ac, badWords[j]);
                        }

                }
        }

        private static void censorDomains(char ac[]) {
		char ac1[] = ac.clone();
		char ac2[] = {'(', 'a', ')'};
                applyWordFilter(null, ac1, ac2);
		char ac3[] = ac.clone();
		char ac4[] = {'d', 'o', 't'};
                applyWordFilter(null, ac3, ac4);
                for (int i = domainWords.length - 1; i >= 0; i--) {
                        censorDomain(ac, domainWords[i], ac3, ac1);
                }
        }

        private static void censorDomain(char ac[], char ac1[], char ac2[], char ac3[]) {
		if (ac1.length > ac.length) {
			return;
		}
		int j;
		for (int k = 0; k <= ac.length - ac1.length; k += j) {
			int l = k;
			int i1 = 0;
			j = 1;
			while (l < ac.length) {
				int j1;
				char c = ac[l];
				char c1 = '\0';
				if (l + 1 < ac.length) {
					c1 = ac[l + 1];
				}
				if (i1 < ac1.length && (j1 = matchDomainCharacter(c, ac1[i1], c1)) > 0) {
					l += j1;
					i1++;
					continue;
				}
				if (i1 == 0) {
					break;
				}
				if ((j1 = matchDomainCharacter(c, ac1[i1 - 1], c1)) > 0) {
					l += j1;
					if (i1 == 1) {
						j++;
					}
					continue;
				}
				if (i1 >= ac1.length || !isNonAlphanumeric(c)) {
					break;
				}
				l++;
			}
			if (i1 >= ac1.length) {
				boolean flag1 = false;
                                int k1 = checkPrecedingContext(ac, ac3, k);
                                int l1 = checkFollowingContext(ac2, l - 1, ac);
				if (k1 > 2 || l1 > 2) {
					flag1 = true;
				}
				if (flag1) {
					for (int i2 = k; i2 < l; i2++) {
						ac[i2] = '*';
					}

				}
			}
		}

	}

        private static int checkPrecedingContext(char ac[], char ac1[], int j) {
		if (j == 0) {
			return 2;
		}
		for (int k = j - 1; k >= 0; k--) {
			if (!isNonAlphanumeric(ac[k])) {
				break;
			}
			if (ac[k] == '@') {
				return 3;
			}
		}

		int l = 0;
		for (int i1 = j - 1; i1 >= 0; i1--) {
			if (!isNonAlphanumeric(ac1[i1])) {
				break;
			}
			if (ac1[i1] == '*') {
				l++;
			}
		}

		if (l >= 3) {
			return 4;
		}
		return !isNonAlphanumeric(ac[j - 1]) ? 0 : 1;
	}

        private static int checkFollowingContext(char ac[], int i, char ac1[]) {
		if (i + 1 == ac1.length) {
			return 2;
		}
		for (int j = i + 1; j < ac1.length; j++) {
			if (!isNonAlphanumeric(ac1[j])) {
				break;
			}
			if (ac1[j] == '.' || ac1[j] == ',') {
				return 3;
			}
		}
		int k = 0;
		for (int l = i + 1; l < ac1.length; l++) {
			if (!isNonAlphanumeric(ac[l])) {
				break;
			}
			if (ac[l] == '*') {
				k++;
			}
		}

		if (k >= 3) {
			return 4;
		}
		return !isNonAlphanumeric(ac1[i + 1]) ? 0 : 1;
	}

        private static void censorTopLevelDomains(char ac[]) {
		char ac1[] = ac.clone();
		char ac2[] = {'d', 'o', 't'};
                applyWordFilter(null, ac1, ac2);
		char ac3[] = ac.clone();
		char ac4[] = {'s', 'l', 'a', 's', 'h'};
                applyWordFilter(null, ac3, ac4);
                for (int i = 0; i < topLevelDomains.length; i++) {
                        censorTldHelper(ac3, topLevelDomains[i], tldBehavior[i], ac1, ac);
                }

	}

        private static void censorTldHelper(char ac[], char ac1[], int i, char ac2[], char ac3[]) {
		if (ac1.length > ac3.length) {
			return;
		}
		int j;
		for (int k = 0; k <= ac3.length - ac1.length; k += j) {
			int l = k;
			int i1 = 0;
			j = 1;
			while (l < ac3.length) {
				int j1;
				char c = ac3[l];
				char c1 = '\0';
				if (l + 1 < ac3.length) {
					c1 = ac3[l + 1];
				}
				if (i1 < ac1.length && (j1 = matchDomainCharacter(c, ac1[i1], c1)) > 0) {
					l += j1;
					i1++;
					continue;
				}
				if (i1 == 0) {
					break;
				}
				if ((j1 = matchDomainCharacter(c, ac1[i1 - 1], c1)) > 0) {
					l += j1;
					if (i1 == 1) {
						j++;
					}
					continue;
				}
				if (i1 >= ac1.length || !isNonAlphanumeric(c)) {
					break;
				}
				l++;
			}
			if (i1 >= ac1.length) {
				boolean flag1 = false;
                                int k1 = checkPrecedingPunctuation(ac3, k, ac2);
                                int l1 = checkFollowingPunctuation(ac3, ac, l - 1);
				if (i == 1 && k1 > 0 && l1 > 0) {
					flag1 = true;
				}
				if (i == 2 && (k1 > 2 && l1 > 0 || k1 > 0 && l1 > 2)) {
					flag1 = true;
				}
				if (i == 3 && k1 > 0 && l1 > 2) {
					flag1 = true;
				}
				if (flag1) {
					int i2 = k;
					int j2 = l - 1;
					if (k1 > 2) {
						if (k1 == 4) {
							boolean flag2 = false;
							for (int l2 = i2 - 1; l2 >= 0; l2--) {
								if (flag2) {
									if (ac2[l2] != '*') {
										break;
									}
									i2 = l2;
								} else if (ac2[l2] == '*') {
									i2 = l2;
									flag2 = true;
								}
							}

						}
						boolean flag3 = false;
						for (int i3 = i2 - 1; i3 >= 0; i3--) {
							if (flag3) {
								if (isNonAlphanumeric(ac3[i3])) {
									break;
								}
								i2 = i3;
							} else if (!isNonAlphanumeric(ac3[i3])) {
								flag3 = true;
								i2 = i3;
							}
						}

					}
					if (l1 > 2) {
						if (l1 == 4) {
							boolean flag4 = false;
							for (int j3 = j2 + 1; j3 < ac3.length; j3++) {
								if (flag4) {
									if (ac[j3] != '*') {
										break;
									}
									j2 = j3;
								} else if (ac[j3] == '*') {
									j2 = j3;
									flag4 = true;
								}
							}

						}
						boolean flag5 = false;
						for (int k3 = j2 + 1; k3 < ac3.length; k3++) {
							if (flag5) {
								if (isNonAlphanumeric(ac3[k3])) {
									break;
								}
								j2 = k3;
							} else if (!isNonAlphanumeric(ac3[k3])) {
								flag5 = true;
								j2 = k3;
							}
						}

					}
					for (int k2 = i2; k2 <= j2; k2++) {
						ac3[k2] = '*';
					}

				}
			}
		}
	}

        private static int checkPrecedingPunctuation(char ac[], int j, char ac1[]) {
		if (j == 0) {
			return 2;
		}
		for (int k = j - 1; k >= 0; k--) {
			if (!isNonAlphanumeric(ac[k])) {
				break;
			}
			if (ac[k] == ',' || ac[k] == '.') {
				return 3;
			}
		}

		int l = 0;
		for (int i1 = j - 1; i1 >= 0; i1--) {
			if (!isNonAlphanumeric(ac1[i1])) {
				break;
			}
			if (ac1[i1] == '*') {
				l++;
			}
		}
		if (l >= 3) {
			return 4;
		}
		return !isNonAlphanumeric(ac[j - 1]) ? 0 : 1;
	}

        private static int checkFollowingPunctuation(char ac[], char ac1[], int i) {
		if (i + 1 == ac.length) {
			return 2;
		}
		for (int j = i + 1; j < ac.length; j++) {
			if (!isNonAlphanumeric(ac[j])) {
				break;
			}
			if (ac[j] == '\\' || ac[j] == '/') {
				return 3;
			}
		}

		int k = 0;
		for (int l = i + 1; l < ac.length; l++) {
			if (!isNonAlphanumeric(ac1[l])) {
				break;
			}
			if (ac1[l] == '*') {
				k++;
			}
		}

		if (k >= 5) {
			return 4;
		}
		return !isNonAlphanumeric(ac[i + 1]) ? 0 : 1;
	}

    private static void applyWordFilter(byte[][] patterns, char[] text, char[] word) {
                if (word.length > text.length) {
                        return;
                }
                int j;
                for (int k = 0; k <= text.length - word.length; k += j) {
                        int l = k;
                        int i1 = 0;
                        int j1 = 0;
                        j = 1;
			boolean flag1 = false;
			boolean flag2 = false;
			boolean flag3 = false;
                        while (l < text.length && (!flag2 || !flag3)) {
                                int k1;
                                char c = text[l];
                                char c2 = '\0';
                                if (l + 1 < text.length) {
                                        c2 = text[l + 1];
                                }
                                if (i1 < word.length && (k1 = matchLeetCharacter(c2, c, word[i1])) > 0) {
					if (k1 == 1 && isDigit(c)) {
						flag2 = true;
					}
					if (k1 == 2 && (isDigit(c) || isDigit(c2))) {
						flag2 = true;
					}
                                        l += k1;
                                        i1++;
                                        continue;
                                }
                                if (i1 == 0) {
                                        break;
                                }
                                if ((k1 = matchLeetCharacter(c2, c, word[i1 - 1])) > 0) {
                                        l += k1;
                                        if (i1 == 1) {
                                                j++;
                                        }
                                        continue;
                                }
                                if (i1 >= word.length || !isFillerCharacter(c)) {
                                        break;
                                }
                                if (isNonAlphanumeric(c) && c != '\'') {
                                        flag1 = true;
                                }
                                if (isDigit(c)) {
                                        flag3 = true;
                                }
                                l++;
                                if (++j1 * 100 / (l - k) > 90) {
                                        break;
                                }
                        }
                        if (i1 >= word.length && (!flag2 || !flag3)) {
                                boolean flag4 = true;
                                if (!flag1) {
                                        char c1 = ' ';
                                        if (k - 1 >= 0) {
                                                c1 = text[k - 1];
                                        }
                                        char c3 = ' ';
                                        if (l < text.length) {
                                                c3 = text[l];
                                        }
                                        byte byte0 = charToByte(c1);
                                        byte byte1 = charToByte(c3);
                                        if (patterns != null && lookupCharPair(byte0, patterns, byte1)) {
                                                flag4 = false;
                                        }
                                } else {
					boolean flag5 = false;
					boolean flag6 = false;
                                        if (k - 1 < 0 || isNonAlphanumeric(text[k - 1]) && text[k - 1] != '\'') {
                                                flag5 = true;
                                        }
                                        if (l >= text.length || isNonAlphanumeric(text[l]) && text[l] != '\'') {
                                                flag6 = true;
                                        }
                                        if (!flag5 || !flag6) {
                                                boolean flag7 = false;
                                                int k2 = k - 2;
                                                if (flag5) {
                                                        k2 = k;
                                                }
                                                for (; !flag7 && k2 < l; k2++) {
                                                        if (k2 >= 0 && (!isNonAlphanumeric(text[k2]) || text[k2] == '\'')) {
                                                                char tmp[] = new char[3];
                                                                int j3;
                                                                for (j3 = 0; j3 < 3; j3++) {
                                                                        if (k2 + j3 >= text.length || isNonAlphanumeric(text[k2 + j3]) && text[k2 + j3] != '\'') {
                                                                                break;
                                                                        }
                                                                        tmp[j3] = text[k2 + j3];
                                                                }

                                                                boolean flag8 = true;
                                                                if (j3 == 0) {
                                                                        flag8 = false;
                                                                }
                                                                if (j3 < 3 && k2 - 1 >= 0 && (!isNonAlphanumeric(text[k2 - 1]) || text[k2 - 1] == '\'')) {
                                                                        flag8 = false;
                                                                }
                                                                if (flag8 && !isBannedName(tmp)) {
                                                                        flag7 = true;
                                                                }
                                                        }
                                                }

						if (!flag7) {
							flag4 = false;
						}
					}
				}
				if (flag4) {
					int l1 = 0;
					int i2 = 0;
					int j2 = -1;
                                        for (int l2 = k; l2 < l; l2++) {
                                                if (isDigit(text[l2])) {
                                                        l1++;
                                                } else if (isLetter(text[l2])) {
                                                        i2++;
                                                        j2 = l2;
                                                }
                                        }

                                        if (j2 > -1) {
                                                l1 -= l - 1 - j2;
                                        }
                                        if (l1 <= i2) {
                                                for (int i3 = k; i3 < l; i3++) {
                                                        text[i3] = '*';
                                                }

					} else {
						j = 1;
					}
				}
			}
		}

	}

        private static boolean lookupCharPair(byte byte0, byte[][] table, byte byte2) {
                int i = 0;
                if (table[i][0] == byte0 && table[i][1] == byte2) {
                        return true;
                }
                int j = table.length - 1;
                if (table[j][0] == byte0 && table[j][1] == byte2) {
                        return true;
                }
                do {
                        int k = (i + j) / 2;
                        if (table[k][0] == byte0 && table[k][1] == byte2) {
                                return true;
                        }
                        if (byte0 < table[k][0] || byte0 == table[k][0] && byte2 < table[k][1]) {
                                j = k;
                        } else {
                                i = k;
                        }
                } while (i != j && i + 1 != j);
                return false;
        }

        private static int matchDomainCharacter(char textChar, char patternChar, char nextChar) {
                if (patternChar == textChar) {
                        return 1;
                }
                if (patternChar == 'o' && textChar == '0') {
                        return 1;
                }
                if (patternChar == 'o' && textChar == '(' && nextChar == ')') {
                        return 2;
                }
                if (patternChar == 'c' && (textChar == '(' || textChar == '<' || textChar == '[')) {
                        return 1;
                }
                if (patternChar == 'e' && textChar == '\u20AC') {
                        return 1;
                }
                if (patternChar == 's' && textChar == '$') {
                        return 1;
                }
                return patternChar != 'l' || textChar != 'i' ? 0 : 1;
        }

        private static int matchLeetCharacter(char nextChar, char currentChar, char patternChar) {
                if (patternChar == currentChar) {
                        return 1;
                }
                if (patternChar >= 'a' && patternChar <= 'm') {
                        if (patternChar == 'a') {
                                if (currentChar == '4' || currentChar == '@' || currentChar == '^') {
                                        return 1;
                                }
                                return currentChar != '/' || nextChar != '\\' ? 0 : 2;
                        }
                        if (patternChar == 'b') {
                                if (currentChar == '6' || currentChar == '8') {
                                        return 1;
                                }
                                return (currentChar != '1' || nextChar != '3') && (currentChar != 'i' || nextChar != '3') ? 0 : 2;
                        }
                        if (patternChar == 'c') {
                                return currentChar != '(' && currentChar != '<' && currentChar != '{' && currentChar != '[' ? 0 : 1;
                        }
                        if (patternChar == 'd') {
                                return (currentChar != '[' || nextChar != ')') && (currentChar != 'i' || nextChar != ')') ? 0 : 2;
                        }
                        if (patternChar == 'e') {
                                return currentChar != '3' && currentChar != '\u20AC' ? 0 : 1;
                        }
                        if (patternChar == 'f') {
                                if (currentChar == 'p' && nextChar == 'h') {
                                        return 2;
                                }
                                return currentChar != '\243' ? 0 : 1;
                        }
                        if (patternChar == 'g') {
                                return currentChar != '9' && currentChar != '6' && currentChar != 'q' ? 0 : 1;
                        }
                        if (patternChar == 'h') {
                                return currentChar != '#' ? 0 : 1;
                        }
                        if (patternChar == 'i') {
                                return currentChar != 'y' && currentChar != 'l' && currentChar != 'j' && currentChar != '1' && currentChar != '!' && currentChar != ':' && currentChar != ';' && currentChar != '|' ? 0 : 1;
                        }
                        if (patternChar == 'j') {
                                return 0;
                        }
                        if (patternChar == 'k') {
                                return 0;
                        }
                        if (patternChar == 'l') {
                                return currentChar != '1' && currentChar != '|' && currentChar != 'i' ? 0 : 1;
                        }
                        if (patternChar == 'm') {
                                return 0;
                        }
                }
                if (patternChar >= 'n' && patternChar <= 'z') {
                        if (patternChar == 'n') {
                                return 0;
                        }
                        if (patternChar == 'o') {
                                if (currentChar == '0' || currentChar == '*') {
                                        return 1;
                                }
                                return (currentChar != '(' || nextChar != ')') && (currentChar != '[' || nextChar != ']') && (currentChar != '{' || nextChar != '}') && (currentChar != '<' || nextChar != '>') ? 0 : 2;
                        }
                        if (patternChar == 'p') {
                                return 0;
                        }
                        if (patternChar == 'q') {
                                return 0;
                        }
                        if (patternChar == 'r') {
                                return 0;
                        }
                        if (patternChar == 's') {
                                return currentChar != '5' && currentChar != 'z' && currentChar != '$' && currentChar != '2' ? 0 : 1;
                        }
                        if (patternChar == 't') {
                                return currentChar != '7' && currentChar != '+' ? 0 : 1;
                        }
                        if (patternChar == 'u') {
                                if (currentChar == 'v') {
                                        return 1;
                                }
                                return (currentChar != '\\' || nextChar != '/') && (currentChar != '\\' || nextChar != '|') && (currentChar != '|' || nextChar != '/') ? 0 : 2;
                        }
                        if (patternChar == 'v') {
                                return (currentChar != '\\' || nextChar != '/') && (currentChar != '\\' || nextChar != '|') && (currentChar != '|' || nextChar != '/') ? 0 : 2;
                        }
                        if (patternChar == 'w') {
                                return currentChar != 'v' || nextChar != 'v' ? 0 : 2;
                        }
                        if (patternChar == 'x') {
                                return (currentChar != ')' || nextChar != '(') && (currentChar != '}' || nextChar != '{') && (currentChar != ']' || nextChar != '[') && (currentChar != '>' || nextChar != '<') ? 0 : 2;
                        }
                        if (patternChar == 'y') {
                                return 0;
                        }
                        if (patternChar == 'z') {
                                return 0;
                        }
                }
                if (patternChar >= '0' && patternChar <= '9') {
                        if (patternChar == '0') {
                                if (currentChar == 'o' || currentChar == 'O') {
                                        return 1;
                                }
                                return (currentChar != '(' || nextChar != ')') && (currentChar != '{' || nextChar != '}') && (currentChar != '[' || nextChar != ']') ? 0 : 2;
                        }
                        if (patternChar == '1') {
                                return currentChar != 'l' ? 0 : 1;
                        } else {
                                return 0;
                        }
                }
                if (patternChar == ',') {
                        return currentChar != '.' ? 0 : 1;
                }
                if (patternChar == '.') {
                        return currentChar != ',' ? 0 : 1;
                }
                if (patternChar == '!') {
                        return currentChar != 'i' ? 0 : 1;
                } else {
                        return 0;
                }
        }

        private static byte charToByte(char c) {
                if (c >= 'a' && c <= 'z') {
                        return (byte) (c - 97 + 1);
                }
                if (c == '\'') {
                        return 28;
                }
                if (c >= '0' && c <= '9') {
                        return (byte) (c - 48 + 29);
                } else {
                        return 27;
                }
        }

        private static void censorLongNumbers(char[] text) {
                int j;
                int k = 0;
                int l = 0;
                int i1 = 0;
                while ((j = findNextDigit(text, k)) != -1) {
                        boolean flag = false;
                        for (int j1 = k; j1 >= 0 && j1 < j && !flag; j1++) {
                                if (!isNonAlphanumeric(text[j1]) && !isFillerCharacter(text[j1])) {
                                        flag = true;
                                }
                        }

                        if (flag) {
                                l = 0;
                        }
                        if (l == 0) {
                                i1 = j;
                        }
                        k = findNonDigit(text, j);
                        int k1 = 0;
                        for (int l1 = j; l1 < k; l1++) {
                                k1 = k1 * 10 + text[l1] - 48;
                        }

                        if (k1 > 255 || k - j > 8) {
                                l = 0;
                        } else {
                                l++;
                        }
                        if (l == 4) {
                                for (int i2 = i1; i2 < k; i2++) {
                                        text[i2] = '*';
                                }

                                l = 0;
                        }
                }
        }

        private static int findNextDigit(char[] text, int index) {
                for (int k = index; k < text.length && k >= 0; k++) {
                        if (text[k] >= '0' && text[k] <= '9') {
                                return k;
                        }
                }

                return -1;
        }

        private static int findNonDigit(char[] text, int index) {
                for (int k = index; k < text.length && k >= 0; k++) {
                        if (text[k] < '0' || text[k] > '9') {
                                return k;
                        }
                }
                return text.length;
        }

	private static boolean isNonAlphanumeric(char c) {
		return !isLetter(c) && !isDigit(c);
	}

	private static boolean isFillerCharacter(char c) {
		return c < 'a' || c > 'z' || c == 'v' || c == 'x' || c == 'j' || c == 'q' || c == 'z';
	}

	private static boolean isLetter(char c) {
		return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z';
	}

	private static boolean isDigit(char c) {
		return c >= '0' && c <= '9';
	}

	private static boolean isLowerCaseLetter(char c) {
		return c >= 'a' && c <= 'z';
	}

	private static boolean isUpperCaseLetter(char c) {
		return c >= 'A' && c <= 'Z';
	}

        private static boolean isBannedName(char[] nameChars) {
                boolean flag = true;
                for (int i = 0; i < nameChars.length; i++) {
                        if (!isDigit(nameChars[i]) && nameChars[i] != 0) {
                                flag = false;
                        }
                }

		if (flag) {
			return true;
		}
                int j = computeNameHash(nameChars);
		int k = 0;
                int l = bannedNameHashes.length - 1;
                if (j == bannedNameHashes[k] || j == bannedNameHashes[l]) {
			return true;
		}
		do {
			int i1 = (k + l) / 2;
                        if (j == bannedNameHashes[i1]) {
                                return true;
                        }
                        if (j < bannedNameHashes[i1]) {
                                l = i1;
                        } else {
                                k = i1;
                        }
		} while (k != l && k + 1 != l);
		return false;
	}

        private static int computeNameHash(char[] nameChars) {
                if (nameChars.length > 6) {
                        return 0;
                }
                int k = 0;
                for (int l = 0; l < nameChars.length; l++) {
                        char c = nameChars[nameChars.length - l - 1];
                        if (c >= 'a' && c <= 'z') {
                                k = k * 38 + c - 97 + 1;
                        } else if (c == '\'') {
                                k = k * 38 + 27;
                        } else if (c >= '0' && c <= '9') {
                                k = k * 38 + c - 48 + 28;
                        } else if (c != 0) {
                                return 0;
                        }
                }

		return k;
	}

        private static int[] bannedNameHashes;
        private static char[][] badWords;
        private static byte[][][] badWordPatterns;
        private static char[][] domainWords;
        private static char[][] topLevelDomains;
        private static int[] tldBehavior;
	private static final String[] exceptions = {"cook", "cook's", "cooks", "seeks", "sheet", "woop", "woops", "faq", "noob", "noobs"};

}
