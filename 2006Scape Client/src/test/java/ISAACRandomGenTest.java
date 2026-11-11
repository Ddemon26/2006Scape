import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Semantic tests for ISAACRandomGen using only its original member names. The oracle is an independent
 * implementation of Bob Jenkins' published ISAAC (rand.c, randinit(TRUE)), plus the published zero-seed vector.
 */
public class ISAACRandomGenTest {
    /** Reference with switches that deliberately break one role, to prove the tests can fail. */
    private static final class Reference {
        int rounds = 4;
        boolean swapSeedWords01;
        boolean secondPassReadsResults;
        boolean skipSecondPass;
        int memoryPassBlocks = 32;
        boolean newValueUsedForPartner; // swaps the roles of old/new memory value in the results lookup

        int[] mem = new int[256];
        int[] rsl = new int[256];
        int aa, bb, cc;

        static void mix(int[] w) {
            int a = w[0], b = w[1], c = w[2], d = w[3], e = w[4], f = w[5], g = w[6], h = w[7];
            a ^= b << 11; d += a; b += c;
            b ^= c >>> 2; e += b; c += d;
            c ^= d << 8; f += c; d += e;
            d ^= e >>> 16; g += d; e += f;
            e ^= f << 10; h += e; f += g;
            f ^= g >>> 4; a += f; g += h;
            g ^= h << 8; b += g; h += a;
            h ^= a >>> 9; c += h; a += b;
            w[0] = a; w[1] = b; w[2] = c; w[3] = d; w[4] = e; w[5] = f; w[6] = g; w[7] = h;
        }

        void init(int[] seed) {
            System.arraycopy(seed, 0, rsl, 0, seed.length);
            int[] w = new int[8];
            Arrays.fill(w, 0x9e3779b9);
            for (int i = 0; i < rounds; i++) mix(w);
            for (int base = 0; base < 256; base += 8) {
                for (int n = 0; n < 8; n++) w[n] += rsl[base + n];
                if (swapSeedWords01) { int t = w[0]; w[0] = w[1]; w[1] = t; }
                mix(w);
                System.arraycopy(w, 0, mem, base, 8);
            }
            if (!skipSecondPass) {
                for (int block = 0; block < memoryPassBlocks; block++) {
                    int base = block * 8;
                    int[] src = secondPassReadsResults ? rsl : mem;
                    for (int n = 0; n < 8; n++) w[n] += src[base + n];
                    mix(w);
                    System.arraycopy(w, 0, mem, base, 8);
                }
            }
            generate();
        }

        void generate() {
            cc++;
            bb += cc;
            for (int i = 0; i < 256; i++) {
                int x = mem[i];
                switch (i & 3) {
                    case 0: aa ^= aa << 13; break;
                    case 1: aa ^= aa >>> 6; break;
                    case 2: aa ^= aa << 2; break;
                    default: aa ^= aa >>> 16; break;
                }
                aa += mem[(i + 128) & 255];
                int y = mem[(x >>> 2) & 255] + aa + bb;
                mem[i] = y;
                int lookupSource = newValueUsedForPartner ? x : y;
                bb = mem[(lookupSource >>> 10) & 255] + (newValueUsedForPartner ? y : x);
                rsl[i] = bb;
            }
        }
    }

    private static Field field(String name) throws Exception {
        Field field = ISAACRandomGen.class.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }

    private static int[] ints(ISAACRandomGen g, String name) throws Exception {
        return (int[]) field(name).get(g);
    }

    private static Reference reference(int[] seed) {
        Reference r = new Reference();
        r.init(seed);
        return r;
    }

    private static void assertSameState(Reference r, ISAACRandomGen g) throws Exception {
        assertArrayEquals(r.mem, ints(g, "memory"));
        assertArrayEquals(r.rsl, ints(g, "results"));
        assertEquals(r.aa, field("accumulator").getInt(g));
        assertEquals(r.bb, field("lastResult").getInt(g));
        assertEquals(r.cc, field("counter").getInt(g));
    }

    private static int[][] seeds() {
        Random random = new Random(2006);
        int[] full = new int[256];
        for (int i = 0; i < 256; i++) full[i] = random.nextInt();
        int[] four = {random.nextInt(), random.nextInt(), random.nextInt(), random.nextInt()};
        return new int[][] {new int[0], new int[4], {1, 2, 3, 4}, four, new int[] {-1, -1, -1, -1}, full};
    }

    @Test public void publishedZeroSeedVector() throws Exception {
        // Jenkins' rand.c prints the output of the second isaac() call for an all-zero seed: f650e4c8 e448e96d 98db2fb4 ...
        ISAACRandomGen g = new ISAACRandomGen(new int[4]);
        int[] first = ints(g, "results").clone();
        assertEquals(0xe76dd339, first[0]);
        for (int i = 0; i < 256; i++) g.getNextKey();
        g.getNextKey(); // first key of the second batch triggers the second isaac
        int[] second = ints(g, "results");
        assertEquals(0xf650e4c8, second[0]);
        assertEquals(0xe448e96d, second[1]);
        assertEquals(0x98db2fb4, second[2]);
        assertEquals(0xf5fad54f, second[3]);
    }

    @Test public void constructedStateEqualsTextbookReference() throws Exception {
        for (int[] seed : seeds()) {
            ISAACRandomGen g = new ISAACRandomGen(seed);
            assertSameState(reference(seed), g);
            assertEquals(256, field("count").getInt(g));
            assertEquals(1, field("counter").getInt(g));
        }
    }

    @Test public void keyStreamIsResultsReadFromTheEndAcrossBatches() throws Exception {
        for (int[] seed : seeds()) {
            ISAACRandomGen g = new ISAACRandomGen(seed);
            Reference r = reference(seed);
            for (int batch = 0; batch < 3; batch++) {
                if (batch > 0) r.generate(); // the next batch is generated lazily when the previous one is used up
                for (int index = 255; index >= 0; index--) {
                    assertEquals(r.rsl[index], g.getNextKey());
                }
            }
            assertSameState(r, g);
        }
    }

    @Test public void isaacOnArbitraryStateMatchesReferenceAndPerStepInvariants() throws Exception {
        Method isaac = ISAACRandomGen.class.getDeclaredMethod("isaac");
        isaac.setAccessible(true);
        Random random = new Random(7);
        for (int trial = 0; trial < 20; trial++) {
            ISAACRandomGen g = new ISAACRandomGen(new int[] {trial, 5, 6, 7});
            int[] mem = ints(g, "memory");
            for (int i = 0; i < 256; i++) mem[i] = random.nextInt();
            field("accumulator").setInt(g, random.nextInt());
            field("lastResult").setInt(g, random.nextInt());
            field("counter").setInt(g, random.nextInt());
            Reference r = new Reference();
            System.arraycopy(mem, 0, r.mem, 0, 256);
            r.aa = field("accumulator").getInt(g);
            r.bb = field("lastResult").getInt(g);
            r.cc = field("counter").getInt(g);
            int[] oldMemory = mem.clone();
            isaac.invoke(g);
            r.generate();
            assertSameState(r, g);
            int[] after = ints(g, "memory");
            int[] results = ints(g, "results");
            for (int i = 0; i < 256; i++) {
                int oldValue = oldMemory[i];          // original j
                int newValue = after[i];              // original k
                int slot = (newValue >> 8 & 0x3fc) >> 2;
                int looked = slot <= i ? after[slot] : oldMemory[slot]; // memory[i] is already the new value when the lookup runs
                assertEquals("results[" + i + "] = lookup(new memory[i]) + OLD memory[i]", looked + oldValue, results[i]);
                assertNotEquals(looked + newValue, results[i]);
            }
        }
    }

    @Test public void everySeedWordMatters() throws Exception {
        int[] zero = new int[256];
        Reference base = reference(zero);
        for (int index = 0; index < 256; index++) {
            int[] seed = new int[256];
            seed[index] = 1;
            ISAACRandomGen g = new ISAACRandomGen(seed);
            assertSameState(reference(seed), g);
            assertFalse("seed word " + index, Arrays.equals(base.rsl, ints(g, "results")));
            assertFalse(Arrays.equals(base.mem, ints(g, "memory")));
        }
    }

    @Test public void wrongRolesDoNotMatch() throws Exception {
        int[] seed = seeds()[5];
        ISAACRandomGen g = new ISAACRandomGen(seed);
        int[] expected = ints(g, "results").clone();
        Reference good = reference(seed);
        assertArrayEquals(expected, good.rsl);

        Reference r;
        r = new Reference(); r.rounds = 3; r.init(seed);
        assertFalse("3 scramble rounds", Arrays.equals(expected, r.rsl));
        r = new Reference(); r.rounds = 5; r.init(seed);
        assertFalse("5 scramble rounds", Arrays.equals(expected, r.rsl));
        r = new Reference(); r.swapSeedWords01 = true; r.init(seed);
        assertFalse("words 0 and 1 swapped", Arrays.equals(expected, r.rsl));
        r = new Reference(); r.secondPassReadsResults = true; r.init(seed);
        assertFalse("second pass reads results", Arrays.equals(expected, r.rsl));
        r = new Reference(); r.skipSecondPass = true; r.init(seed);
        assertFalse("no second pass", Arrays.equals(expected, r.rsl));
        r = new Reference(); r.memoryPassBlocks = 16; r.init(seed);
        assertFalse("memory pass covers 128 slots only", Arrays.equals(expected, r.rsl));
        r = new Reference(); r.newValueUsedForPartner = true; r.init(seed);
        assertFalse("old/new memory value roles swapped", Arrays.equals(expected, r.rsl));
    }

    @Test public void seedIsNotModifiedAndOversizedSeedIsRejected() throws Exception {
        int[] seed = {10, 20, 30, 40};
        int[] copy = seed.clone();
        ISAACRandomGen first = new ISAACRandomGen(seed);
        assertArrayEquals(copy, seed);
        ISAACRandomGen second = new ISAACRandomGen(copy);
        for (int i = 0; i < 600; i++) assertEquals(first.getNextKey(), second.getNextKey());
        ISAACRandomGen other = new ISAACRandomGen(new int[] {10, 20, 30, 41});
        assertNotEquals(new ISAACRandomGen(seed).getNextKey(), other.getNextKey());
        try {
            new ISAACRandomGen(new int[257]);
            fail("seed longer than 256 words must be rejected");
        } catch (ArrayIndexOutOfBoundsException expected) {
        }
    }
}
