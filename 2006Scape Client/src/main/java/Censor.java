// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3)

final class Censor {

	public static void loadConfig(StreamLoader streamLoader) {
		Stream fragmentsStream = new Stream(streamLoader.getDataForName("fragmentsenc.txt"));
		Stream badEncStream = new Stream(streamLoader.getDataForName("badenc.txt"));
		Stream domainEncStream = new Stream(streamLoader.getDataForName("domainenc.txt"));
		Stream tldListStream = new Stream(streamLoader.getDataForName("tldlist.txt"));
		readValues(fragmentsStream, badEncStream, domainEncStream, tldListStream);
	}

	private static void readValues(Stream fragmentsStream, Stream badEncStream, Stream domainEncStream, Stream tldListStream) {
		readBadEnc(badEncStream);
		readDomainEnc(domainEncStream);
		readFragmentsEnc(fragmentsStream);
		readTldList(tldListStream);
	}

	private static void readTldList(Stream stream) {
		int count = stream.readIntBigEndian();
		topLevelDomains = new char[count][];
		topLevelDomainStrictness = new int[count];
		for (int entry = 0; entry < count; entry++) {
			topLevelDomainStrictness[entry] = stream.readUnsignedByte();
			char word[] = new char[stream.readUnsignedByte()];
			for (int charIndex = 0; charIndex < word.length; charIndex++) {
				word[charIndex] = (char) stream.readUnsignedByte();
			}

			topLevelDomains[entry] = word;
		}

	}

	private static void readBadEnc(Stream stream) {
		int count = stream.readIntBigEndian();
		badWords = new char[count][];
		badWordExemptionPairs = new byte[count][][];
		readBadWordEntries(stream, badWords, badWordExemptionPairs);
	}

	private static void readDomainEnc(Stream stream) {
		int count = stream.readIntBigEndian();
		domains = new char[count][];
		readDomainEntries(domains, stream);
	}

	private static void readFragmentsEnc(Stream stream) {
		allowedFragmentCodes = new int[stream.readIntBigEndian()];
		for (int index = 0; index < allowedFragmentCodes.length; index++) {
			allowedFragmentCodes[index] = stream.readUnsignedShortBigEndian();
		}
	}

	private static void readBadWordEntries(Stream stream, char words[][], byte exemptionPairs[][][]) {
		for (int entry = 0; entry < words.length; entry++) {
			char word[] = new char[stream.readUnsignedByte()];
			for (int charIndex = 0; charIndex < word.length; charIndex++) {
				word[charIndex] = (char) stream.readUnsignedByte();
			}

			words[entry] = word;
			byte pairs[][] = new byte[stream.readUnsignedByte()][2];
			for (int pairIndex = 0; pairIndex < pairs.length; pairIndex++) {
				pairs[pairIndex][0] = (byte) stream.readUnsignedByte();
				pairs[pairIndex][1] = (byte) stream.readUnsignedByte();
			}

			if (pairs.length > 0) {
				exemptionPairs[entry] = pairs;
			}
		}

	}

	private static void readDomainEntries(char words[][], Stream stream) {
		for (int entry = 0; entry < words.length; entry++) {
			char word[] = new char[stream.readUnsignedByte()];
			for (int charIndex = 0; charIndex < word.length; charIndex++) {
				word[charIndex] = (char) stream.readUnsignedByte();
			}

			words[entry] = word;
		}

	}

	private static void sanitizeCharacters(char text[]) {
		int writeIndex = 0;
		for (int readIndex = 0; readIndex < text.length; readIndex++) {
			if (isPermittedCharacter(text[readIndex])) {
				text[writeIndex] = text[readIndex];
			} else {
				text[writeIndex] = ' ';
			}
			if (writeIndex == 0 || text[writeIndex] != ' ' || text[writeIndex - 1] != ' ') {
				writeIndex++;
			}
		}
		for (int fillIndex = writeIndex; fillIndex < text.length; fillIndex++) {
			text[fillIndex] = ' ';
		}

	}

	private static boolean isPermittedCharacter(char c) {
		return c >= ' ' && c <= '\177' || c == ' ' || c == '\n' || c == '\t' || c == '\243' || c == '\u20AC';
	}

	public static String doCensor(String s) {
		System.currentTimeMillis();
		char ac[] = s.toCharArray();
		sanitizeCharacters(ac);
		String s1 = new String(ac).trim();
		ac = s1.toLowerCase().toCharArray();
		String s2 = s1.toLowerCase();
		censorTopLevelDomains(ac);
		censorBadWords(ac);
		censorDomains(ac);
		censorIpAddresses(ac);
		for (String exception : exceptions) {
			for (int k = -1; (k = s2.indexOf(exception, k + 1)) != -1;) {
				char ac1[] = exception.toCharArray();
				System.arraycopy(ac1, 0, ac, k, ac1.length);

			}

		}
		restoreUpperCase(s1.toCharArray(), ac);
		lowerTrailingCapitals(ac);
		System.currentTimeMillis();
		return s; // xxx chat filter, return s to avoid new String(ac).trim()
	}

	private static void restoreUpperCase(char original[], char censored[]) {
		for (int index = 0; index < original.length; index++) {
			if (censored[index] != '*' && isUpperCaseLetter(original[index])) {
				censored[index] = original[index];
			}
		}
	}

	private static void lowerTrailingCapitals(char text[]) {
		boolean beforeFirstLowercase = true;
		for (int index = 0; index < text.length; index++) {
			char ch = text[index];
			if (isLetter(ch)) {
				if (beforeFirstLowercase) {
					if (isLowerCaseLetter(ch)) {
						beforeFirstLowercase = false;
					}
				} else if (isUpperCaseLetter(ch)) {
					text[index] = (char) (ch + 97 - 65);
				}
			} else {
				beforeFirstLowercase = true;
			}
		}
	}

	private static void censorBadWords(char text[]) {
		for (int pass = 0; pass < 2; pass++) {
			for (int wordIndex = badWords.length - 1; wordIndex >= 0; wordIndex--) {
				censorWord(badWordExemptionPairs[wordIndex], text, badWords[wordIndex]);
			}

		}
	}

	private static void censorDomains(char text[]) {
		char atMasked[] = text.clone();
		char atPattern[] = {'(', 'a', ')'};
		censorWord(null, atMasked, atPattern);
		char dotMasked[] = text.clone();
		char dotPattern[] = {'d', 'o', 't'};
		censorWord(null, dotMasked, dotPattern);
		for (int domainIndex = domains.length - 1; domainIndex >= 0; domainIndex--) {
			censorDomain(text, domains[domainIndex], dotMasked, atMasked);
		}
	}

	private static void censorDomain(char text[], char domain[], char dotMasked[], char atMasked[]) {
		if (domain.length > text.length) {
			return;
		}
		int step;
		for (int start = 0; start <= text.length - domain.length; start += step) {
			int end = start;
			int domainIndex = 0;
			step = 1;
			while (end < text.length) {
				int consumed;
				char current = text[end];
				char next = '\0';
				if (end + 1 < text.length) {
					next = text[end + 1];
				}
				if (domainIndex < domain.length && (consumed = simpleSubstitutionLength(current, domain[domainIndex], next)) > 0) {
					end += consumed;
					domainIndex++;
					continue;
				}
				if (domainIndex == 0) {
					break;
				}
				if ((consumed = simpleSubstitutionLength(current, domain[domainIndex - 1], next)) > 0) {
					end += consumed;
					if (domainIndex == 1) {
						step++;
					}
					continue;
				}
				if (domainIndex >= domain.length || !isSeparator(current)) {
					break;
				}
				end++;
			}
			if (domainIndex >= domain.length) {
				boolean shouldMask = false;
				int prefixClass = classifyDomainPrefix(text, atMasked, start);
				int suffixClass = classifyDomainSuffix(dotMasked, end - 1, text);
				if (prefixClass > 2 || suffixClass > 2) {
					shouldMask = true;
				}
				if (shouldMask) {
					for (int maskIndex = start; maskIndex < end; maskIndex++) {
						text[maskIndex] = '*';
					}

				}
			}
		}

	}

	private static int classifyDomainPrefix(char text[], char atMasked[], int start) {
		if (start == 0) {
			return 2;
		}
		for (int index = start - 1; index >= 0; index--) {
			if (!isSeparator(text[index])) {
				break;
			}
			if (text[index] == '@') {
				return 3;
			}
		}

		int maskCount = 0;
		for (int index = start - 1; index >= 0; index--) {
			if (!isSeparator(atMasked[index])) {
				break;
			}
			if (atMasked[index] == '*') {
				maskCount++;
			}
		}

		if (maskCount >= 3) {
			return 4;
		}
		return !isSeparator(text[start - 1]) ? 0 : 1;
	}

	private static int classifyDomainSuffix(char dotMasked[], int lastIndex, char text[]) {
		if (lastIndex + 1 == text.length) {
			return 2;
		}
		for (int index = lastIndex + 1; index < text.length; index++) {
			if (!isSeparator(text[index])) {
				break;
			}
			if (text[index] == '.' || text[index] == ',') {
				return 3;
			}
		}
		int maskCount = 0;
		for (int index = lastIndex + 1; index < text.length; index++) {
			if (!isSeparator(dotMasked[index])) {
				break;
			}
			if (dotMasked[index] == '*') {
				maskCount++;
			}
		}

		if (maskCount >= 3) {
			return 4;
		}
		return !isSeparator(text[lastIndex + 1]) ? 0 : 1;
	}

	private static void censorTopLevelDomains(char text[]) {
		char dotMasked[] = text.clone();
		char dotPattern[] = {'d', 'o', 't'};
		censorWord(null, dotMasked, dotPattern);
		char slashMasked[] = text.clone();
		char slashPattern[] = {'s', 'l', 'a', 's', 'h'};
		censorWord(null, slashMasked, slashPattern);
		for (int index = 0; index < topLevelDomains.length; index++) {
			censorTopLevelDomain(slashMasked, topLevelDomains[index], topLevelDomainStrictness[index], dotMasked, text);
		}

	}

	private static void censorTopLevelDomain(char slashMasked[], char tld[], int strictness, char dotMasked[], char text[]) {
		if (tld.length > text.length) {
			return;
		}
		int step;
		for (int start = 0; start <= text.length - tld.length; start += step) {
			int end = start;
			int tldIndex = 0;
			step = 1;
			while (end < text.length) {
				int consumed;
				char current = text[end];
				char next = '\0';
				if (end + 1 < text.length) {
					next = text[end + 1];
				}
				if (tldIndex < tld.length && (consumed = simpleSubstitutionLength(current, tld[tldIndex], next)) > 0) {
					end += consumed;
					tldIndex++;
					continue;
				}
				if (tldIndex == 0) {
					break;
				}
				if ((consumed = simpleSubstitutionLength(current, tld[tldIndex - 1], next)) > 0) {
					end += consumed;
					if (tldIndex == 1) {
						step++;
					}
					continue;
				}
				if (tldIndex >= tld.length || !isSeparator(current)) {
					break;
				}
				end++;
			}
			if (tldIndex >= tld.length) {
				boolean shouldMask = false;
				int prefixClass = classifyTldPrefix(text, start, dotMasked);
				int suffixClass = classifyTldSuffix(text, slashMasked, end - 1);
				if (strictness == 1 && prefixClass > 0 && suffixClass > 0) {
					shouldMask = true;
				}
				if (strictness == 2 && (prefixClass > 2 && suffixClass > 0 || prefixClass > 0 && suffixClass > 2)) {
					shouldMask = true;
				}
				if (strictness == 3 && prefixClass > 0 && suffixClass > 2) {
					shouldMask = true;
				}
				if (shouldMask) {
					int maskStart = start;
					int maskEnd = end - 1;
					if (prefixClass > 2) {
						if (prefixClass == 4) {
							boolean inMaskRun = false;
							for (int index = maskStart - 1; index >= 0; index--) {
								if (inMaskRun) {
									if (dotMasked[index] != '*') {
										break;
									}
									maskStart = index;
								} else if (dotMasked[index] == '*') {
									maskStart = index;
									inMaskRun = true;
								}
							}

						}
						boolean inWord = false;
						for (int index = maskStart - 1; index >= 0; index--) {
							if (inWord) {
								if (isSeparator(text[index])) {
									break;
								}
								maskStart = index;
							} else if (!isSeparator(text[index])) {
								inWord = true;
								maskStart = index;
							}
						}

					}
					if (suffixClass > 2) {
						if (suffixClass == 4) {
							boolean inMaskRun = false;
							for (int index = maskEnd + 1; index < text.length; index++) {
								if (inMaskRun) {
									if (slashMasked[index] != '*') {
										break;
									}
									maskEnd = index;
								} else if (slashMasked[index] == '*') {
									maskEnd = index;
									inMaskRun = true;
								}
							}

						}
						boolean inWord = false;
						for (int index = maskEnd + 1; index < text.length; index++) {
							if (inWord) {
								if (isSeparator(text[index])) {
									break;
								}
								maskEnd = index;
							} else if (!isSeparator(text[index])) {
								inWord = true;
								maskEnd = index;
							}
						}

					}
					for (int maskIndex = maskStart; maskIndex <= maskEnd; maskIndex++) {
						text[maskIndex] = '*';
					}

				}
			}
		}
	}

	private static int classifyTldPrefix(char text[], int start, char dotMasked[]) {
		if (start == 0) {
			return 2;
		}
		for (int index = start - 1; index >= 0; index--) {
			if (!isSeparator(text[index])) {
				break;
			}
			if (text[index] == ',' || text[index] == '.') {
				return 3;
			}
		}

		int maskCount = 0;
		for (int index = start - 1; index >= 0; index--) {
			if (!isSeparator(dotMasked[index])) {
				break;
			}
			if (dotMasked[index] == '*') {
				maskCount++;
			}
		}
		if (maskCount >= 3) {
			return 4;
		}
		return !isSeparator(text[start - 1]) ? 0 : 1;
	}

	private static int classifyTldSuffix(char text[], char slashMasked[], int lastIndex) {
		if (lastIndex + 1 == text.length) {
			return 2;
		}
		for (int index = lastIndex + 1; index < text.length; index++) {
			if (!isSeparator(text[index])) {
				break;
			}
			if (text[index] == '\\' || text[index] == '/') {
				return 3;
			}
		}

		int maskCount = 0;
		for (int index = lastIndex + 1; index < text.length; index++) {
			if (!isSeparator(slashMasked[index])) {
				break;
			}
			if (slashMasked[index] == '*') {
				maskCount++;
			}
		}

		if (maskCount >= 5) {
			return 4;
		}
		return !isSeparator(text[lastIndex + 1]) ? 0 : 1;
	}

	private static void censorWord(byte exemptionPairs[][], char text[], char word[]) {
		if (word.length > text.length) {
			return;
		}
		int step;
		for (int start = 0; start <= text.length - word.length; start += step) {
			int end = start;
			int wordIndex = 0;
			int fillerCount = 0;
			step = 1;
			boolean hasSeparatorFiller = false;
			boolean digitLookalike = false;
			boolean digitFiller = false;
			while (end < text.length && (!digitLookalike || !digitFiller)) {
				int consumed;
				char current = text[end];
				char next = '\0';
				if (end + 1 < text.length) {
					next = text[end + 1];
				}
				if (wordIndex < word.length && (consumed = lookalikeSubstitutionLength(next, current, word[wordIndex])) > 0) {
					if (consumed == 1 && isDigit(current)) {
						digitLookalike = true;
					}
					if (consumed == 2 && (isDigit(current) || isDigit(next))) {
						digitLookalike = true;
					}
					end += consumed;
					wordIndex++;
					continue;
				}
				if (wordIndex == 0) {
					break;
				}
				if ((consumed = lookalikeSubstitutionLength(next, current, word[wordIndex - 1])) > 0) {
					end += consumed;
					if (wordIndex == 1) {
						step++;
					}
					continue;
				}
				if (wordIndex >= word.length || !isSkippableFiller(current)) {
					break;
				}
				if (isSeparator(current) && current != '\'') {
					hasSeparatorFiller = true;
				}
				if (isDigit(current)) {
					digitFiller = true;
				}
				end++;
				if (++fillerCount * 100 / (end - start) > 90) {
					break;
				}
			}
			if (wordIndex >= word.length && (!digitLookalike || !digitFiller)) {
				boolean shouldMask = true;
				if (!hasSeparatorFiller) {
					char previousChar = ' ';
					if (start - 1 >= 0) {
						previousChar = text[start - 1];
					}
					char nextChar = ' ';
					if (end < text.length) {
						nextChar = text[end];
					}
					byte previousCode = neighbourCode(previousChar);
					byte nextCode = neighbourCode(nextChar);
					if (exemptionPairs != null && isExemptNeighbourPair(previousCode, exemptionPairs, nextCode)) {
						shouldMask = false;
					}
				} else {
					boolean startsAtBoundary = false;
					boolean endsAtBoundary = false;
					if (start - 1 < 0 || isSeparator(text[start - 1]) && text[start - 1] != '\'') {
						startsAtBoundary = true;
					}
					if (end >= text.length || isSeparator(text[end]) && text[end] != '\'') {
						endsAtBoundary = true;
					}
					if (!startsAtBoundary || !endsAtBoundary) {
						boolean foundUnallowedFragment = false;
						int windowStart = start - 2;
						if (startsAtBoundary) {
							windowStart = start;
						}
						for (; !foundUnallowedFragment && windowStart < end; windowStart++) {
							if (windowStart >= 0 && (!isSeparator(text[windowStart]) || text[windowStart] == '\'')) {
								char window[] = new char[3];
								int windowLength;
								for (windowLength = 0; windowLength < 3; windowLength++) {
									if (windowStart + windowLength >= text.length || isSeparator(text[windowStart + windowLength]) && text[windowStart + windowLength] != '\'') {
										break;
									}
									window[windowLength] = text[windowStart + windowLength];
								}

								boolean windowCounts = true;
								if (windowLength == 0) {
									windowCounts = false;
								}
								if (windowLength < 3 && windowStart - 1 >= 0 && (!isSeparator(text[windowStart - 1]) || text[windowStart - 1] == '\'')) {
									windowCounts = false;
								}
								if (windowCounts && !isAllowedFragment(window)) {
									foundUnallowedFragment = true;
								}
							}
						}

						if (!foundUnallowedFragment) {
							shouldMask = false;
						}
					}
				}
				if (shouldMask) {
					int digitCount = 0;
					int letterCount = 0;
					int lastLetterIndex = -1;
					for (int scanIndex = start; scanIndex < end; scanIndex++) {
						if (isDigit(text[scanIndex])) {
							digitCount++;
						} else if (isLetter(text[scanIndex])) {
							letterCount++;
							lastLetterIndex = scanIndex;
						}
					}

					if (lastLetterIndex > -1) {
						digitCount -= end - 1 - lastLetterIndex;
					}
					if (digitCount <= letterCount) {
						for (int maskIndex = start; maskIndex < end; maskIndex++) {
							text[maskIndex] = '*';
						}

					} else {
						step = 1;
					}
				}
			}
		}

	}

	private static boolean isExemptNeighbourPair(byte previousCode, byte pairs[][], byte nextCode) {
		int low = 0;
		if (pairs[low][0] == previousCode && pairs[low][1] == nextCode) {
			return true;
		}
		int high = pairs.length - 1;
		if (pairs[high][0] == previousCode && pairs[high][1] == nextCode) {
			return true;
		}
		do {
			int middle = (low + high) / 2;
			if (pairs[middle][0] == previousCode && pairs[middle][1] == nextCode) {
				return true;
			}
			if (previousCode < pairs[middle][0] || previousCode == pairs[middle][0] && nextCode < pairs[middle][1]) {
				high = middle;
			} else {
				low = middle;
			}
		} while (low != high && low + 1 != high);
		return false;
	}

	private static int simpleSubstitutionLength(char textChar, char patternChar, char nextTextChar) {
		if (patternChar == textChar) {
			return 1;
		}
		if (patternChar == 'o' && textChar == '0') {
			return 1;
		}
		if (patternChar == 'o' && textChar == '(' && nextTextChar == ')') {
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

	private static int lookalikeSubstitutionLength(char nextTextChar, char currentTextChar, char patternChar) {
		if (patternChar == currentTextChar) {
			return 1;
		}
		if (patternChar >= 'a' && patternChar <= 'm') {
			if (patternChar == 'a') {
				if (currentTextChar == '4' || currentTextChar == '@' || currentTextChar == '^') {
					return 1;
				}
				return currentTextChar != '/' || nextTextChar != '\\' ? 0 : 2;
			}
			if (patternChar == 'b') {
				if (currentTextChar == '6' || currentTextChar == '8') {
					return 1;
				}
				return (currentTextChar != '1' || nextTextChar != '3') && (currentTextChar != 'i' || nextTextChar != '3') ? 0 : 2;
			}
			if (patternChar == 'c') {
				return currentTextChar != '(' && currentTextChar != '<' && currentTextChar != '{' && currentTextChar != '[' ? 0 : 1;
			}
			if (patternChar == 'd') {
				return (currentTextChar != '[' || nextTextChar != ')') && (currentTextChar != 'i' || nextTextChar != ')') ? 0 : 2;
			}
			if (patternChar == 'e') {
				return currentTextChar != '3' && currentTextChar != '\u20AC' ? 0 : 1;
			}
			if (patternChar == 'f') {
				if (currentTextChar == 'p' && nextTextChar == 'h') {
					return 2;
				}
				return currentTextChar != '\243' ? 0 : 1;
			}
			if (patternChar == 'g') {
				return currentTextChar != '9' && currentTextChar != '6' && currentTextChar != 'q' ? 0 : 1;
			}
			if (patternChar == 'h') {
				return currentTextChar != '#' ? 0 : 1;
			}
			if (patternChar == 'i') {
				return currentTextChar != 'y' && currentTextChar != 'l' && currentTextChar != 'j' && currentTextChar != '1' && currentTextChar != '!' && currentTextChar != ':' && currentTextChar != ';' && currentTextChar != '|' ? 0 : 1;
			}
			if (patternChar == 'j') {
				return 0;
			}
			if (patternChar == 'k') {
				return 0;
			}
			if (patternChar == 'l') {
				return currentTextChar != '1' && currentTextChar != '|' && currentTextChar != 'i' ? 0 : 1;
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
				if (currentTextChar == '0' || currentTextChar == '*') {
					return 1;
				}
				return (currentTextChar != '(' || nextTextChar != ')') && (currentTextChar != '[' || nextTextChar != ']') && (currentTextChar != '{' || nextTextChar != '}') && (currentTextChar != '<' || nextTextChar != '>') ? 0 : 2;
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
				return currentTextChar != '5' && currentTextChar != 'z' && currentTextChar != '$' && currentTextChar != '2' ? 0 : 1;
			}
			if (patternChar == 't') {
				return currentTextChar != '7' && currentTextChar != '+' ? 0 : 1;
			}
			if (patternChar == 'u') {
				if (currentTextChar == 'v') {
					return 1;
				}
				return (currentTextChar != '\\' || nextTextChar != '/') && (currentTextChar != '\\' || nextTextChar != '|') && (currentTextChar != '|' || nextTextChar != '/') ? 0 : 2;
			}
			if (patternChar == 'v') {
				return (currentTextChar != '\\' || nextTextChar != '/') && (currentTextChar != '\\' || nextTextChar != '|') && (currentTextChar != '|' || nextTextChar != '/') ? 0 : 2;
			}
			if (patternChar == 'w') {
				return currentTextChar != 'v' || nextTextChar != 'v' ? 0 : 2;
			}
			if (patternChar == 'x') {
				return (currentTextChar != ')' || nextTextChar != '(') && (currentTextChar != '}' || nextTextChar != '{') && (currentTextChar != ']' || nextTextChar != '[') && (currentTextChar != '>' || nextTextChar != '<') ? 0 : 2;
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
				if (currentTextChar == 'o' || currentTextChar == 'O') {
					return 1;
				}
				return (currentTextChar != '(' || nextTextChar != ')') && (currentTextChar != '{' || nextTextChar != '}') && (currentTextChar != '[' || nextTextChar != ']') ? 0 : 2;
			}
			if (patternChar == '1') {
				return currentTextChar != 'l' ? 0 : 1;
			} else {
				return 0;
			}
		}
		if (patternChar == ',') {
			return currentTextChar != '.' ? 0 : 1;
		}
		if (patternChar == '.') {
			return currentTextChar != ',' ? 0 : 1;
		}
		if (patternChar == '!') {
			return currentTextChar != 'i' ? 0 : 1;
		} else {
			return 0;
		}
	}

	private static byte neighbourCode(char c) {
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

	private static void censorIpAddresses(char text[]) {
		int numberStart;
		int previousNumberEnd = 0;
		int runCount = 0;
		int runStart = 0;
		while ((numberStart = findNextDigit(text, previousNumberEnd)) != -1) {
			boolean brokenByLetter = false;
			for (int scanIndex = previousNumberEnd; scanIndex >= 0 && scanIndex < numberStart && !brokenByLetter; scanIndex++) {
				if (!isSeparator(text[scanIndex]) && !isSkippableFiller(text[scanIndex])) {
					brokenByLetter = true;
				}
			}

			if (brokenByLetter) {
				runCount = 0;
			}
			if (runCount == 0) {
				runStart = numberStart;
			}
			previousNumberEnd = findDigitRunEnd(text, numberStart);
			int value = 0;
			for (int digitIndex = numberStart; digitIndex < previousNumberEnd; digitIndex++) {
				value = value * 10 + text[digitIndex] - 48;
			}

			if (value > 255 || previousNumberEnd - numberStart > 8) {
				runCount = 0;
			} else {
				runCount++;
			}
			if (runCount == 4) {
				for (int maskIndex = runStart; maskIndex < previousNumberEnd; maskIndex++) {
					text[maskIndex] = '*';
				}

				runCount = 0;
			}
		}
	}

	private static int findNextDigit(char text[], int start) {
		for (int index = start; index < text.length && index >= 0; index++) {
			if (text[index] >= '0' && text[index] <= '9') {
				return index;
			}
		}

		return -1;
	}

	private static int findDigitRunEnd(char text[], int start) {
		for (int index = start; index < text.length && index >= 0; index++) {
			if (text[index] < '0' || text[index] > '9') {
				return index;
			}
		}
		return text.length;
	}

	private static boolean isSeparator(char c) {
		return !isLetter(c) && !isDigit(c);
	}

	private static boolean isSkippableFiller(char c) {
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

	private static boolean isAllowedFragment(char text[]) {
		boolean digitsOnly = true;
		for (int index = 0; index < text.length; index++) {
			if (!isDigit(text[index]) && text[index] != 0) {
				digitsOnly = false;
			}
		}

		if (digitsOnly) {
			return true;
		}
		int code = encodeFragment(text);
		int low = 0;
		int high = allowedFragmentCodes.length - 1;
		if (code == allowedFragmentCodes[low] || code == allowedFragmentCodes[high]) {
			return true;
		}
		do {
			int middle = (low + high) / 2;
			if (code == allowedFragmentCodes[middle]) {
				return true;
			}
			if (code < allowedFragmentCodes[middle]) {
				high = middle;
			} else {
				low = middle;
			}
		} while (low != high && low + 1 != high);
		return false;
	}

	private static int encodeFragment(char text[]) {
		if (text.length > 6) {
			return 0;
		}
		int code = 0;
		for (int index = 0; index < text.length; index++) {
			char c = text[text.length - index - 1];
			if (c >= 'a' && c <= 'z') {
				code = code * 38 + c - 97 + 1;
			} else if (c == '\'') {
				code = code * 38 + 27;
			} else if (c >= '0' && c <= '9') {
				code = code * 38 + c - 48 + 28;
			} else if (c != 0) {
				return 0;
			}
		}

		return code;
	}

	private static int[] allowedFragmentCodes;
	private static char[][] badWords;
	private static byte[][][] badWordExemptionPairs;
	private static char[][] domains;
	private static char[][] topLevelDomains;
	private static int[] topLevelDomainStrictness;
	private static final String[] exceptions = {"cook", "cook's", "cooks", "seeks", "sheet", "woop", "woops", "faq", "noob", "noobs"};

}
