package util;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import util.cryptography.ISAACRandomGen;

public class ISAACRandomGenTest {

  private ISAACRandomGen generator;
  private int[] testSeed;

  @Before
  public void setUp() {
    testSeed = new int[] {1, 2, 3, 4, 5};
    generator = new ISAACRandomGen(testSeed);
  }

  @Test
  public void testConstructorWithValidSeed() {
    assertNotNull("Generator should be created successfully", generator);
  }

  @Test
  public void testConstructorWithEmptySeed() {
    int[] emptySeed = new int[0];
    ISAACRandomGen emptyGenerator = new ISAACRandomGen(emptySeed);
    assertNotNull("Generator should handle empty seed", emptyGenerator);
  }

  @Test
  public void testGetNextKeyReturnsIntegers() {
    int key1 = generator.getNextKey();
    int key2 = generator.getNextKey();

    assertNotNull("Should return integer values", key1);
    assertNotNull("Should return integer values", key2);
  }

  @Test
  public void testDeterministicBehavior() {
    ISAACRandomGen gen1 = new ISAACRandomGen(testSeed);
    ISAACRandomGen gen2 = new ISAACRandomGen(testSeed.clone());

    for (int i = 0; i < 10; i++) {
      assertEquals("Same seed should produce same sequence", gen1.getNextKey(), gen2.getNextKey());
    }
  }

  @Test
  public void testDifferentSeedsProduceDifferentSequences() {
    int[] differentSeed = new int[] {10, 20, 30, 40, 50};
    ISAACRandomGen differentGenerator = new ISAACRandomGen(differentSeed);

    boolean foundDifference = false;
    for (int i = 0; i < 100; i++) {
      if (generator.getNextKey() != differentGenerator.getNextKey()) {
        foundDifference = true;
        break;
      }
    }

    assertTrue("Different seeds should produce different sequences", foundDifference);
  }

  @Test
  public void testSequenceLength() {
    ISAACRandomGen testGenerator = new ISAACRandomGen(testSeed);

    for (int i = 0; i < 300; i++) {
      int key = testGenerator.getNextKey();
      assertNotNull("Should continue generating keys beyond 256 calls", key);
    }
  }

  @Test
  public void testLargeSeedArray() {
    int[] largeSeed = new int[256];
    for (int i = 0; i < 256; i++) {
      largeSeed[i] = i;
    }

    ISAACRandomGen largeGenerator = new ISAACRandomGen(largeSeed);
    assertNotNull("Should handle large seed arrays", largeGenerator);

    int key = largeGenerator.getNextKey();
    assertNotNull("Should generate valid keys with large seed", key);
  }

  @Test
  public void testRandomnessDistribution() {
    int[] counts = new int[4];
    ISAACRandomGen testGen = new ISAACRandomGen(new int[] {42, 123, 456, 789});

    for (int i = 0; i < 1000; i++) {
      int key = Math.abs(testGen.getNextKey());
      counts[key % 4]++;
    }

    for (int count : counts) {
      assertTrue("Distribution should be somewhat uniform", count > 200);
    }
  }
}
