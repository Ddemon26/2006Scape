// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

public final class VarBit {

	public static void unpackConfig(StreamLoader streamLoader) {
		Stream stream = new Stream(streamLoader.getDataForName("varbit.dat"));
		int cacheSize = stream.readUnsignedWord();
		if (cache == null) {
			cache = new VarBit[cacheSize];
		}
		for (int j = 0; j < cacheSize; j++) {
			if (cache[j] == null) {
				cache[j] = new VarBit();
			}
			cache[j].readValues(stream);
                        if (cache[j].isActive) {
                                Varp.cache[cache[j].configId].aBoolean713 = true;
			}
		}

		if (stream.currentOffset != stream.buffer.length) {
			System.out.println("varbit load mismatch");
		}
	}

	private void readValues(Stream stream) {
		do {
			int j = stream.readUnsignedByte();
			if (j == 0) {
				return;
			}
			if (j == 1) {
                                configId = stream.readUnsignedWord();
                                leastSignificantBit = stream.readUnsignedByte();
                                mostSignificantBit = stream.readUnsignedByte();
			} else if (j == 10) {
				stream.readString();
			} else if (j == 2) {
                                isActive = true;
			} else if (j == 3) {
				stream.readDWord();
			} else if (j == 4) {
				stream.readDWord();
			} else {
				System.out.println("Error unrecognised config code: " + j);
			}
		} while (true);
	}

	private VarBit() {
                isActive = false;
	}

	public static VarBit cache[];
        public int configId;
        public int leastSignificantBit;
        public int mostSignificantBit;
        private boolean isActive;
}
