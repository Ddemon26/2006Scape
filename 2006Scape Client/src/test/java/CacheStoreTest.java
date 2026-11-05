import static org.junit.Assert.*;

import java.io.*;
import java.lang.reflect.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.Arrays;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class CacheStoreTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();

    // Independent literal layout: file 513, store 201, chunks at sectors 4 -> 1 -> 3.
    // Payload byte coordinates are authored independently of the production chain traversal.
    private Fixture fragmented() throws Exception {
        Fixture f = new Fixture(201);
        byte[] dat = new byte[5 * 520]; Arrays.fill(dat, (byte)0x6d);
        byte[] content = pattern(1200, 17);
        ByteBuffer bytes = ByteBuffer.wrap(dat);
        bytes.position(2080); bytes.put(new byte[] {2,1,0,0,0,0,1,(byte)201});
        bytes.put(content, 0, 512);
        bytes.position(520); bytes.put(new byte[] {2,1,0,1,0,0,3,(byte)201});
        bytes.put(content, 512, 512);
        bytes.position(1560); bytes.put(new byte[] {2,1,0,2,0,0,0,(byte)201});
        bytes.put(content, 1024, 176);
        f.data.write(dat);
        f.index.seek(3078); f.index.write(new byte[] {0,4,(byte)176,0,0,4});
        return f;
    }

    @Test public void fragmentedSectorsReturnExactRawBytesAndFreshStorage() throws Exception {
        try (Fixture f = fragmented()) {
            byte[] expected = pattern(1200, 17);
            assertArrayEquals(expected, f.store.readFile(513));
            byte[] result = f.store.readFile(513); result[512] ^= 1;
            assertArrayEquals(expected, f.store.readFile(513));
            assertNull(f.store.readFile(512));
            assertNull(f.store.readFile(514));
            assertNull(new CacheStore(f.data, f.index, 200).readFile(513));
            assertNull(new CacheStore(f.index, f.data, 201).readFile(513));
        }
    }

    @Test public void everyHeaderIdentityAndChainBoundaryIsChecked() throws Exception {
        int[][] corruptions = {{2080,3},{2083,1},{2087,200},{2084,1},
                {2086,0},{524,1},{525,1},{526,9},{1563,1}};
        for (int[] corruption : corruptions) try (Fixture f = fragmented()) {
            f.data.seek(corruption[0]); f.data.write(corruption[1]);
            assertNull("offset " + corruption[0], f.store.readFile(513));
        }
        try (Fixture f = fragmented()) {
            f.data.setLength(2080 + 8 + 511); // one byte short of the first chunk
            assertNull(f.store.readFile(513));
        }
        try (Fixture f = fragmented()) {
            f.index.setLength(3083); assertNull(f.store.readFile(513));
        }
        try (Fixture f = fragmented()) {
            f.index.seek(3081); f.index.write(new byte[] {0,0,0});
            assertNull(f.store.readFile(513));
            f.index.seek(3083); f.index.write(6);
            assertNull(f.store.readFile(513));
        }
    }

    @Test public void writesUseByteCountPrefixAndSixByteIndexWithEightByteSectorHeaders() throws Exception {
        int[] lengths = {0,1,511,512,513,1024,1025,65537};
        for (int length : lengths) try (Fixture f = new Fixture(255)) {
            byte[] source = pattern(length + 11, 93), before = source.clone();
            assertTrue(f.store.writeFile(length, source, 65535));
            byte[] expected = Arrays.copyOf(source, length);
            inspect(f, 65535, 255, expected);
            if (length > 0) assertArrayEquals(expected, f.store.readFile(65535));
            assertArrayEquals(before, source);
            assertEquals(6L * 65536, f.index.length());
            f.index.seek(0); assertEquals(0, f.index.readInt());
            if (length == 0) {
                assertEquals(0, f.data.length());
                assertNull(f.store.readFile(65535)); // zero record still points to absent sector 1
            }
        }
    }

    @Test public void overwriteReusesFragmentedChainAndShorteningTerminatesIt() throws Exception {
        try (Fixture f = fragmented()) {
            byte[] bytes = pattern(1100, 44);
            assertTrue(f.store.writeFile(bytes.length, bytes, 513));
            assertArrayEquals(new int[] {4,1,3}, inspect(f,513,201,bytes));
            assertEquals(2600, f.data.length());
            byte[] shortBytes = {9,0,(byte)255};
            assertTrue(f.store.writeFile(3, shortBytes, 513));
            assertArrayEquals(new int[] {4}, inspect(f,513,201,shortBytes));
            assertArrayEquals(shortBytes, f.store.readFile(513));
            assertEquals(2600, f.data.length());
            assertTrue(f.store.writeFile(0, new byte[0], 513));
            assertArrayEquals(new byte[0], f.store.readFile(513));
        }
    }

    @Test public void growthExtendsAtRoundedFileEndWithoutReusingCurrentSector() throws Exception {
        try (Fixture f = fragmented()) {
            byte[] bytes = pattern(1800, 61);
            assertTrue(f.store.writeFile(bytes.length, bytes, 513));
            assertArrayEquals(new int[] {4,1,3,5}, inspect(f,513,201,bytes));
            assertArrayEquals(bytes, f.store.readFile(513));
        }
        try (Fixture f = new Fixture(1)) {
            f.data.setLength(521);
            assertTrue(f.store.writeFile(513, pattern(513,7), 7));
            assertArrayEquals(new int[] {2,3}, inspect(f,7,1,pattern(513,7)));
        }
    }

    @Test public void invalidReuseFallsBackToAppendAndPreservesOldSector() throws Exception {
        for (int offset : new int[] {2080,2083,2087,2084}) try (Fixture f = fragmented()) {
            f.data.seek(offset); f.data.write(127);
            byte[] oldData = Files.readAllBytes(f.dataPath.toPath());
            byte[] bytes = pattern(513, 22);
            assertTrue(f.store.writeFile(bytes.length,bytes,513));
            assertArrayEquals(new int[] {5,6}, inspect(f,513,201,bytes));
            assertArrayEquals(oldData,Arrays.copyOf(Files.readAllBytes(f.dataPath.toPath()),oldData.length));
        }
    }

    @Test public void directSectorWriterFlagSelectsReuseOrFreshAllocation() throws Exception {
        try (Fixture f = fragmented()) {
            byte[] bytes = {4,5,6};
            assertTrue(writeSectors(f,true,513,bytes.length,bytes));
            assertArrayEquals(new int[] {4},inspect(f,513,201,bytes));
            assertTrue(writeSectors(f,false,513,bytes.length,bytes));
            assertArrayEquals(new int[] {5},inspect(f,513,201,bytes));
            assertFalse(writeSectors(f,true,514,bytes.length,bytes));
        }
        try (Fixture f = new Fixture(7)) {
            f.data.setLength(520); f.index.seek(6); f.index.write(new byte[] {0,0,1,0,0,1});
            assertTrue(writeSectors(f,true,1,513,pattern(513,8)));
            assertArrayEquals(new int[] {1,2},inspect(f,1,7,pattern(513,8)));
        }
    }

    @Test public void ioFailuresReturnSentinelsAndReadOnlyFilesCannotWrite() throws Exception {
        Fixture f = fragmented(); f.index.close();
        assertNull(f.store.readFile(513)); assertFalse(f.store.writeFile(1,new byte[] {1},513));
        f.close();
        try (Fixture original = fragmented();
             RandomAccessFile data = new RandomAccessFile(original.dataPath,"r");
             RandomAccessFile index = new RandomAccessFile(original.indexPath,"r")) {
            CacheStore readOnly = new CacheStore(data,index,201);
            assertArrayEquals(pattern(1200,17),readOnly.readFile(513));
            assertFalse(readOnly.writeFile(1,new byte[] {1},513));
        }
    }

    @Test public void storeIdIsUnsignedHeaderByteButConstructorDoesNotMaskIt() throws Exception {
        for (int storeId : new int[] {0,1,127,128,255,256}) try (Fixture f = new Fixture(storeId)) {
            byte[] bytes = {0,31,(byte)139,(byte)255};
            assertTrue(f.store.writeFile(4,bytes,1));
            f.data.seek(527); assertEquals(storeId & 255,f.data.readUnsignedByte());
            if (storeId < 256) assertArrayEquals(bytes,f.store.readFile(1));
            else assertNull(f.store.readFile(1));
        }
    }

    @Test public void seekUsesByteOffsetsAndClampsOutsideInclusiveSixtyMiB() throws Exception {
        try (Fixture f = new Fixture(1)) {
            Method seek = CacheStore.class.getDeclaredMethod("seekTo",RandomAccessFile.class,int.class);
            seek.setAccessible(true);
            for (int position : new int[] {0,519,520,62914560,-1,62914561}) {
                seek.invoke(f.store,f.data,position);
                assertEquals(position < 0 || position > 62914560 ? 62914560 : position,f.data.getFilePointer());
                assertEquals(0,f.index.getFilePointer());
                assertEquals(0,f.data.length()); // seeking does not extend the file
            }
        }
    }

    private static boolean writeSectors(Fixture f,boolean reuse,int id,int length,byte[] bytes) throws Exception {
        Method method;
        try { method = CacheStore.class.getDeclaredMethod("method235",boolean.class,int.class,int.class,byte[].class); }
        catch (NoSuchMethodException renamed) {
            method = CacheStore.class.getDeclaredMethod("writeFileSectors",boolean.class,int.class,int.class,byte[].class);
        }
        method.setAccessible(true); return (Boolean)method.invoke(f.store,reuse,id,length,bytes);
    }

    // Inspect encoded records via DataInput's independent big-endian primitives.
    // Payload bytes are compared with the supplied source, never with the production reader.
    private static int[] inspect(Fixture f,int id,int store,byte[] expected) throws Exception {
        f.index.seek(id * 6L);
        assertEquals(expected.length,read24(f.index));
        int sector = read24(f.index), count = (expected.length + 511) / 512;
        int[] sectors = new int[count]; ByteArrayOutputStream content = new ByteArrayOutputStream();
        for (int chunk=0; chunk<count; chunk++) {
            assertTrue(sector > 0); sectors[chunk] = sector; f.data.seek(sector * 520L);
            assertEquals(id,f.data.readUnsignedShort()); assertEquals(chunk,f.data.readUnsignedShort());
            sector = read24(f.data); assertEquals(store,f.data.readUnsignedByte());
            byte[] part = new byte[Math.min(512,expected.length-content.size())]; f.data.readFully(part); content.write(part);
        }
        if (count > 0) assertEquals(0,sector);
        else assertTrue(sector > 0);
        assertArrayEquals(expected,content.toByteArray()); return sectors;
    }

    private static int read24(DataInput input) throws IOException {
        return input.readUnsignedByte()*65536+input.readUnsignedShort();
    }
    private static byte[] pattern(int length,int salt) {
        byte[] bytes = new byte[length];
        for (int i=0;i<length;i++) bytes[i]=(byte)(i*73+(i/251)*19+salt);
        return bytes;
    }
    private final class Fixture implements AutoCloseable {
        final File dataPath = temporary.newFile(), indexPath = temporary.newFile();
        final RandomAccessFile data = new RandomAccessFile(dataPath,"rw"), index = new RandomAccessFile(indexPath,"rw");
        final CacheStore store;
        Fixture(int storeId) throws Exception { store = new CacheStore(data,index,storeId); }
        public void close() throws IOException { data.close(); index.close(); }
    }
}
