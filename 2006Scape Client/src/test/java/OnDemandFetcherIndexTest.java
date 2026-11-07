import static org.junit.Assert.*;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Pass 153: OnDemandFetcher map_index / anim_index / midi_index tables. Written against the original
 * identifiers (mapIndices1-4, method554, method562, method564, method569, anIntArray1348, anIntArray1360).
 * The archive is built by hand so OnDemandFetcher.start parses real bytes.
 */
public class OnDemandFetcherIndexTest {
    private static final int MAP_FILES = 2100;
    private OnDemandFetcher fetcher;
    private Game game;

    private static Field field(Class<?> c, String n) throws Exception {
        Field f = c.getDeclaredField(n);
        f.setAccessible(true);
        return f;
    }
    private static sun.misc.Unsafe unsafe() throws Exception {
        return (sun.misc.Unsafe) field(sun.misc.Unsafe.class, "theUnsafe").get(null);
    }
    private Object get(String n) throws Exception { return field(OnDemandFetcher.class, n).get(fetcher); }
    private int[] ints(String n) throws Exception { return (int[]) get(n); }

    private static int nameHash(String name) {
        int h = 0;
        name = name.toUpperCase();
        for (int i = 0; i < name.length(); i++) h = h * 61 + name.charAt(i) - 32;
        return h;
    }
    private static final class Out {
        final ByteArrayOutputStream o = new ByteArrayOutputStream();
        Out b(int v) { o.write(v); return this; }
        Out s(int v) { o.write(v >> 8); o.write(v); return this; }
        Out i(int v) { s(v >>> 16); s(v & 0xffff); return this; }
        byte[] done() { return o.toByteArray(); }
    }

    private static byte[] mapIndex() {
        Out o = new Out();
        o.s((3 << 8) + 7).s(1001).s(2002).b(1);      // region x=3 y=7
        o.s((4 << 8) + 9).s(1003).s(2004).b(0);      // flag 0
        o.s((5 << 8) + 11).s(1005).s(2006).b(255);   // flag 255
        o.s((3 << 8) + 7).s(1007).s(2008).b(0);      // duplicate key, later record
        o.b(9).b(9).b(9).b(9);                       // partial record: ignored
        return o.done();
    }
    private static byte[] animIndex() { return new Out().s(0x1234).s(2).s(65535).b(77).done(); } // 3 anims + stray byte
    private static byte[] midiIndex() { return new byte[] {0, 1, 2, (byte) 255, 1, 0, 3}; }

    private StreamLoader archive() throws Exception {
        String[] names = {"model_version", "anim_version", "midi_version", "map_version",
            "model_crc", "anim_crc", "midi_crc", "map_crc", "model_index", "map_index", "anim_index", "midi_index"};
        byte[][] data = new byte[names.length][];
        byte[] mapVersion = new byte[MAP_FILES * 2];
        for (int i = 0; i < MAP_FILES; i++) { mapVersion[2 * i + 1] = 1; }
        data[0] = new Out().s(0x0102).s(0x0304).s(0xFFFF).s(0).done();                 // 4 models
        data[1] = new Out().s(0x0A0B).s(0x0C0D).s(7).done();                           // 3 anims
        data[2] = new byte[14];                                                        // 7 songs
        data[3] = mapVersion;
        data[4] = new Out().i(0x01020304).i(-1).i(5).i(0x80000000).done();
        data[5] = new Out().i(0x7F000001).i(0xFE).i(-2).done();
        data[6] = new byte[28]; data[7] = new byte[MAP_FILES * 4];
        data[8] = new byte[] {1, 2, 3};        // one byte fewer than the 4 models: last entry is padded with 0
        data[9] = mapIndex(); data[10] = animIndex(); data[11] = midiIndex();
        int total = 0;
        for (byte[] d : data) total += d.length;
        byte[] all = new byte[total];
        int[] hashes = new int[names.length], sizes = new int[names.length], offsets = new int[names.length];
        int at = 0;
        for (int i = 0; i < names.length; i++) {
            hashes[i] = nameHash(names[i]); sizes[i] = data[i].length; offsets[i] = at;
            System.arraycopy(data[i], 0, all, at, data[i].length); at += data[i].length;
        }
        StreamLoader loader = (StreamLoader) unsafe().allocateInstance(StreamLoader.class);
        field(StreamLoader.class, "archiveData").set(loader, all);
        field(StreamLoader.class, "archiveDecompressed").setBoolean(loader, true);
        field(StreamLoader.class, "entryCount").setInt(loader, names.length);
        field(StreamLoader.class, "entryNameHashes").set(loader, hashes);
        field(StreamLoader.class, "entryUnpackedSizes").set(loader, sizes);
        field(StreamLoader.class, "entryPackedSizes").set(loader, sizes);
        field(StreamLoader.class, "entryOffsets").set(loader, offsets);
        return loader;
    }

    @Before public void setUp() throws Exception {
        fetcher = new OnDemandFetcher();
        game = (Game) unsafe().allocateInstance(Game.class);
        CacheStore[] stores = new CacheStore[5];
        for (int i = 0; i < 5; i++) {
            java.io.File d = java.io.File.createTempFile("odi", ".dat");
            java.io.File x = java.io.File.createTempFile("odi", ".idx");
            d.deleteOnExit(); x.deleteOnExit();
            stores[i] = new CacheStore(new java.io.RandomAccessFile(d, "rw"), new java.io.RandomAccessFile(x, "rw"), i);
        }
        field(Game.class, "cacheStores").set(game, stores);
        fetcher.start(archive(), game);
        fetcher.disable();
    }
    @After public void tearDown() { fetcher.disable(); }

    @Test public void mapIndexRecordsSplitIntoFourColumnsAndIgnorePartialTail() throws Exception {
        assertArrayEquals(new int[] {775, 1033, 1291, 775}, ints("mapRegionKeys"));
        assertArrayEquals(new int[] {1001, 1003, 1005, 1007}, ints("mapTerrainFileIds"));
        assertArrayEquals(new int[] {2002, 2004, 2006, 2008}, ints("mapObjectFileIds"));
        assertArrayEquals(new int[] {1, 0, 255, 0}, ints("mapPreloadFlags"));
    }

    @Test public void method562KeyIsXHighYLowAndKindSelectsTheFileColumn() throws Exception {
        // signature (kind, y, x): key = (x << 8) + y
        assertEquals(1001, fetcher.getMapFileId(0, 7, 3));
        assertEquals(2002, fetcher.getMapFileId(1, 7, 3));
        assertEquals(1003, fetcher.getMapFileId(0, 9, 4));
        assertEquals(2006, fetcher.getMapFileId(1, 11, 5));
        // any nonzero kind selects the object column
        assertEquals(2006, fetcher.getMapFileId(2, 11, 5));
        assertEquals(2006, fetcher.getMapFileId(-1, 11, 5));
        // swapped coordinates are different keys
        assertEquals(-1, fetcher.getMapFileId(0, 3, 7));
        assertEquals(-1, fetcher.getMapFileId(1, 4, 9));
        // absent regions
        assertEquals(-1, fetcher.getMapFileId(0, 7, 4));
        assertEquals(-1, fetcher.getMapFileId(1, 0, 0));
        // first matching record wins over the later duplicate key (terrain 1001 not 1007, object 2002 not 2008)
        assertEquals(1001, fetcher.getMapFileId(0, 7, 3));
        assertEquals(2002, fetcher.getMapFileId(1, 7, 3));
        // y is not masked: (x=3,y=263) is key 1031 (absent), (x=2,y=263) is key 775
        assertEquals(-1, fetcher.getMapFileId(0, 263, 3));
        assertEquals(1001, fetcher.getMapFileId(0, 263, 2));
    }

    @Test public void method564TrueOnlyForObjectColumnIds() throws Exception {
        for (int id : new int[] {2002, 2004, 2006, 2008}) assertTrue("object id " + id, fetcher.isMapObjectFileId(id));
        for (int id : new int[] {1001, 1003, 1005, 1007}) assertFalse("terrain id " + id, fetcher.isMapObjectFileId(id));
        assertFalse(fetcher.isMapObjectFileId(775));    // region key value
        assertFalse(fetcher.isMapObjectFileId(255));    // flag value
        assertFalse(fetcher.isMapObjectFileId(0));
        assertFalse(fetcher.isMapObjectFileId(-1));
        assertFalse(fetcher.isMapObjectFileId(2003));
    }

    private byte[] status3() throws Exception { return ((byte[][]) get("fileStatus"))[3]; }

    @Test public void method554FlagFalseMarksOnlyNonzeroPreloadRegions() throws Exception {
        fetcher.markMapFilesForDownload(false);
        byte[] s = status3();
        for (int id : new int[] {1001, 2002, 1005, 2006}) assertEquals("marked " + id, 2, s[id]);
        for (int id : new int[] {1003, 2004, 1007, 2008}) assertEquals("skipped " + id, 0, s[id]);
        assertEquals(4, ((Integer) get("totalFiles")).intValue());
        int marked = 0;
        for (byte b : s) if (b != 0) marked++;
        assertEquals(4, marked);   // nothing else touched
    }

    @Test public void method554FlagTrueMarksEveryRegionTerrainAndObjectFile() throws Exception {
        fetcher.markMapFilesForDownload(true);
        byte[] s = status3();
        for (int id : new int[] {1001, 2002, 1003, 2004, 1005, 2006, 1007, 2008}) assertEquals("marked " + id, 2, s[id]);
        assertEquals(8, ((Integer) get("totalFiles")).intValue());
        int marked = 0;
        for (byte b : s) if (b != 0) marked++;
        assertEquals(8, marked);
        assertEquals(2, ((Integer) get("highestPendingPriority")).intValue());
    }

    @Test public void method569IsTrueOnlyForTheExactValueOne() throws Exception {
        assertArrayEquals(new int[] {0, 1, 2, 255, 1, 0, 3}, ints("songPreloadFlags"));
        boolean[] expect = {false, true, false, false, true, false, false};
        for (int i = 0; i < expect.length; i++) assertEquals("song " + i, expect[i], fetcher.isSongPreloadFlagged(i));
        try { fetcher.isSongPreloadFlagged(7); fail("one entry per midi_index byte"); } catch (ArrayIndexOutOfBoundsException ok) { }
    }

    @Test public void animTableHasOneEntryPerTwoBytesAndLengthIsTheAnimCount() throws Exception {
        assertArrayEquals(new int[] {0x1234, 2, 65535}, ints("animIndices"));
        assertEquals(3, fetcher.getAnimCount());
    }

    @Test public void versionAndCrcTablesAreBigEndianAndSizedPerArchiveEntry() throws Exception {
        assertArrayEquals(new int[] {0x0102, 0x0304, 0xFFFF, 0}, fetcher.versions[0]);
        assertArrayEquals(new int[] {0x0A0B, 0x0C0D, 7}, fetcher.versions[1]);
        assertEquals(7, fetcher.versions[2].length);
        assertEquals(MAP_FILES, fetcher.versions[3].length);
        assertArrayEquals(new int[] {0x01020304, -1, 5, 0x80000000}, fetcher.crcs[0]);
        assertArrayEquals(new int[] {0x7F000001, 0xFE, -2}, fetcher.crcs[1]);
        assertEquals(7, fetcher.crcs[2].length);
        assertEquals(MAP_FILES, fetcher.crcs[3].length);
        byte[][] status = (byte[][]) get("fileStatus");
        assertEquals(4, status[0].length);
        assertEquals(3, status[1].length);
        assertEquals(MAP_FILES, status[3].length);
    }

    @Test public void modelIndexTableIsPaddedWithZeroToTheModelVersionCount() throws Exception {
        assertEquals(1, fetcher.getModelIndex(0));
        assertEquals(2, fetcher.getModelIndex(1));
        assertEquals(3, fetcher.getModelIndex(2));
        assertEquals(0, fetcher.getModelIndex(3));
        try { fetcher.getModelIndex(4); fail("one entry per model version"); } catch (ArrayIndexOutOfBoundsException ok) { }
    }
}
