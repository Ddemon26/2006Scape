// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

final class TextClass {

	public static long encodeBase37Name(String name) {
		long encodedName = 0L;
		for (int characterIndex = 0; characterIndex < name.length() && characterIndex < 12; characterIndex++) {
			char character = name.charAt(characterIndex);
			encodedName *= 37L;
			if (character >= 'A' && character <= 'Z') {
				encodedName += 1 + character - 65;
			} else if (character >= 'a' && character <= 'z') {
				encodedName += 1 + character - 97;
			} else if (character >= '0' && character <= '9') {
				encodedName += 27 + character - 48;
			}
		}

		for (; encodedName % 37L == 0L && encodedName != 0L; encodedName /= 37L) {
			;
		}
		return encodedName;
	}

	public static String decodeBase37Name(long encodedName) {
		try {
			if (encodedName <= 0L || encodedName >= 0x5b5b57f8a98a5dd1L) {
				return "invalid_name";
			}
			if (encodedName % 37L == 0L) {
				return "invalid_name";
			}
			int decodedLength = 0;
			char characters[] = new char[12];
			while (encodedName != 0L) {
				long valueBeforeDivision = encodedName;
				encodedName /= 37L;
				characters[11 - decodedLength++] = base37Digits[(int) (valueBeforeDivision - encodedName * 37L)];
			}
			return new String(characters, 12 - decodedLength, decodedLength);
		} catch (RuntimeException decodeFailure) {
			Signlink.reporterror("81570, " + encodedName + ", " + (byte) -99 + ", " + decodeFailure.toString());
		}
		throw new RuntimeException();
	}

	public static long hashSpriteName(String spriteName) {
		spriteName = spriteName.toUpperCase();
		long nameHash = 0L;
		for (int characterIndex = 0; characterIndex < spriteName.length(); characterIndex++) {
			nameHash = nameHash * 61L + spriteName.charAt(characterIndex) - 32L;
			nameHash = nameHash + (nameHash >> 56) & 0xffffffffffffffL;
		}
		return nameHash;
	}

	public static String formatIpv4Address(int packedAddress) {
		return (packedAddress >> 24 & 0xff) + "." + (packedAddress >> 16 & 0xff) + "." + (packedAddress >> 8 & 0xff) + "." + (packedAddress & 0xff);
	}

	public static String formatDisplayName(String name) {
		if (name.length() > 0) {
			char characters[] = name.toCharArray();
			for (int characterIndex = 0; characterIndex < characters.length; characterIndex++) {
				if (characters[characterIndex] == '_') {
					characters[characterIndex] = ' ';
					if (characterIndex + 1 < characters.length && characters[characterIndex + 1] >= 'a' && characters[characterIndex + 1] <= 'z') {
						characters[characterIndex + 1] = (char) (characters[characterIndex + 1] + 65 - 97);
					}
				}
			}

			if (characters[0] >= 'a' && characters[0] <= 'z') {
				characters[0] = (char) (characters[0] + 65 - 97);
			}
			return new String(characters);
		} else {
			return name;
		}
	}

	public static String passwordAsterisks(String inputPassword) {
		StringBuffer maskedPassword = new StringBuffer();
		for (int utf16Index = 0; utf16Index < inputPassword.length(); utf16Index++) {
			maskedPassword.append("*");
		}
		return maskedPassword.toString();
	}

	private static final char[] base37Digits = {'_', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

}
