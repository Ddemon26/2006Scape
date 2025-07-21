package net;

import org.junit.Test;

import static org.junit.Assert.*;

public class StreamTest {
    @Test
    public void testWriteAndReadWord() {
        Stream stream = new Stream(new byte[4]);
        stream.writeWord(0xABCD);
        stream.currentOffset = 0;
        assertEquals(0xABCD, stream.readUnsignedWord());
    }

    @Test
    public void testWriteAndReadDWord() {
        Stream stream = new Stream(new byte[8]);
        stream.writeDWord(0x12345678);
        stream.currentOffset = 0;
        assertEquals(0x12345678, stream.readDWord());
    }

    @Test
    public void testReadBits() {
        Stream stream = new Stream(new byte[] {(byte)0b10101010, (byte)0b11001100});
        stream.initBitAccess();
        assertEquals(0b1010, stream.readBits(4));
        assertEquals(0b10101100, stream.readBits(8));
        stream.finishBitAccess();
        assertEquals(2, stream.currentOffset);
    }
}
