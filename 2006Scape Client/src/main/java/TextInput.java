// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

final class TextInput {

	public static String decodeChatText(int byteCount, Stream input) {
		int decodedCharacterCount = 0;
		int extendedPrefixNibble = -1;
		for (int byteIndex = 0; byteIndex < byteCount; byteIndex++) {
			int packedByte = input.readUnsignedByte();
			int nibble = packedByte >> 4 & 0xf;
			if (extendedPrefixNibble == -1) {
				if (nibble < 13) {
					decodedCharacters[decodedCharacterCount++] = chatAlphabet[nibble];
				} else {
					extendedPrefixNibble = nibble;
				}
			} else {
				decodedCharacters[decodedCharacterCount++] = chatAlphabet[(extendedPrefixNibble << 4) + nibble - 195];
				extendedPrefixNibble = -1;
			}
			nibble = packedByte & 0xf;
			if (extendedPrefixNibble == -1) {
				if (nibble < 13) {
					decodedCharacters[decodedCharacterCount++] = chatAlphabet[nibble];
				} else {
					extendedPrefixNibble = nibble;
				}
			} else {
				decodedCharacters[decodedCharacterCount++] = chatAlphabet[(extendedPrefixNibble << 4) + nibble - 195];
				extendedPrefixNibble = -1;
			}
		}

		boolean capitalizeNextLetter = true;
		for (int characterIndex = 0; characterIndex < decodedCharacterCount; characterIndex++) {
			char character = decodedCharacters[characterIndex];
			if (capitalizeNextLetter && character >= 'a' && character <= 'z') {
				decodedCharacters[characterIndex] += '\uFFE0';
				capitalizeNextLetter = false;
			}
			if (character == '.' || character == '!' || character == '?') {
				capitalizeNextLetter = true;
			}
		}
		return new String(decodedCharacters, 0, decodedCharacterCount);
	}

	public static void encodeChatText(String inputText, Stream output) {
		if (inputText.length() > 80) {
			inputText = inputText.substring(0, 80);
		}
		inputText = inputText.toLowerCase();
		int pendingNibble = -1;
		for (int characterIndex = 0; characterIndex < inputText.length(); characterIndex++) {
			char character = inputText.charAt(characterIndex);
			int characterCode = 0;
			for (int alphabetIndex = 0; alphabetIndex < chatAlphabet.length; alphabetIndex++) {
				if (character != chatAlphabet[alphabetIndex]) {
					continue;
				}
				characterCode = alphabetIndex;
				break;
			}

			if (characterCode > 12) {
				characterCode += 195;
			}
			if (pendingNibble == -1) {
				if (characterCode < 13) {
					pendingNibble = characterCode;
				} else {
					output.writeWordBigEndian(characterCode);
				}
			} else if (characterCode < 13) {
				output.writeWordBigEndian((pendingNibble << 4) + characterCode);
				pendingNibble = -1;
			} else {
				output.writeWordBigEndian((pendingNibble << 4) + (characterCode >> 4));
				pendingNibble = characterCode & 0xf;
			}
		}
		if (pendingNibble != -1) {
			output.writeWordBigEndian(pendingNibble << 4);
		}
	}

	public static String normalizeChatText(String inputText) {
		normalizationStream.currentOffset = 0;
		encodeChatText(inputText, normalizationStream);
		int encodedByteCount = normalizationStream.currentOffset;
		normalizationStream.currentOffset = 0;
		String normalizedText = decodeChatText(encodedByteCount, normalizationStream);
		return normalizedText;
	}

	private static final char[] decodedCharacters = new char[100];
	private static final Stream normalizationStream = new Stream(new byte[100]);
	private static final char[] chatAlphabet = {' ', 'e', 't', 'a', 'o', 'i', 'h', 'n', 's', 'r', 'd', 'l', 'u', 'm', 'w', 'c', 'y', 'f', 'g', 'p', 'b', 'v', 'k', 'x', 'j', 'q', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', ' ', '!', '?', '.', ',', ':', ';', '(', ')', '-', '&', '*', '\\', '\'', '@', '#', '+', '=', '\243', '$', '%', '"', '[', ']'};

}
