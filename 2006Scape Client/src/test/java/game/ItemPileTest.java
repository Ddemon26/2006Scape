package game;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class ItemPileTest {

    private ItemPile itemPile;
    private Animable testAnimable1;
    private Animable testAnimable2;
    private Animable testAnimable3;

    @Before
    public void setUp() {
        itemPile = new ItemPile();
        testAnimable1 = new Animable();
        testAnimable2 = new Animable();
        testAnimable3 = new Animable();
    }

    @Test
    public void testConstructorInitializesCorrectly() {
        assertNotNull("ItemPile should be created successfully", itemPile);
        assertEquals("Height should default to 0", 0, itemPile.height);
        assertEquals("X coordinate should default to 0", 0, itemPile.x);
        assertEquals("Y coordinate should default to 0", 0, itemPile.y);
        assertEquals("UID should default to 0", 0, itemPile.uid);
        assertEquals("OffsetY should default to 0", 0, itemPile.offsetY);
        assertNull("TopItem should be null initially", itemPile.topItem);
        assertNull("SecondItem should be null initially", itemPile.secondItem);
        assertNull("ThirdItem should be null initially", itemPile.thirdItem);
    }

    @Test
    public void testSetPosition() {
        int testX = 100;
        int testY = 200;
        int testHeight = 50;
        
        itemPile.x = testX;
        itemPile.y = testY;
        itemPile.height = testHeight;
        
        assertEquals("X coordinate should be set correctly", testX, itemPile.x);
        assertEquals("Y coordinate should be set correctly", testY, itemPile.y);
        assertEquals("Height should be set correctly", testHeight, itemPile.height);
    }

    @Test
    public void testSetUID() {
        int testUID = 12345;
        
        itemPile.uid = testUID;
        
        assertEquals("UID should be set correctly", testUID, itemPile.uid);
    }

    @Test
    public void testSetOffsetY() {
        int testOffset = 25;
        
        itemPile.offsetY = testOffset;
        
        assertEquals("OffsetY should be set correctly", testOffset, itemPile.offsetY);
    }

    @Test
    public void testSetTopItem() {
        itemPile.topItem = testAnimable1;
        
        assertSame("TopItem should be set correctly", testAnimable1, itemPile.topItem);
    }

    @Test
    public void testSetSecondItem() {
        itemPile.secondItem = testAnimable2;
        
        assertSame("SecondItem should be set correctly", testAnimable2, itemPile.secondItem);
    }

    @Test
    public void testSetThirdItem() {
        itemPile.thirdItem = testAnimable3;
        
        assertSame("ThirdItem should be set correctly", testAnimable3, itemPile.thirdItem);
    }

    @Test
    public void testSetAllItems() {
        itemPile.topItem = testAnimable1;
        itemPile.secondItem = testAnimable2;
        itemPile.thirdItem = testAnimable3;
        
        assertSame("TopItem should be set correctly", testAnimable1, itemPile.topItem);
        assertSame("SecondItem should be set correctly", testAnimable2, itemPile.secondItem);
        assertSame("ThirdItem should be set correctly", testAnimable3, itemPile.thirdItem);
    }

    @Test
    public void testReplaceItems() {
        Animable replacement = new Animable();
        
        itemPile.topItem = testAnimable1;
        itemPile.topItem = replacement;
        
        assertSame("TopItem should be replaced correctly", replacement, itemPile.topItem);
        assertNotSame("Original item should not be referenced", testAnimable1, itemPile.topItem);
    }

    @Test
    public void testNegativeCoordinates() {
        int negativeX = -50;
        int negativeY = -100;
        
        itemPile.x = negativeX;
        itemPile.y = negativeY;
        
        assertEquals("Negative X should be set correctly", negativeX, itemPile.x);
        assertEquals("Negative Y should be set correctly", negativeY, itemPile.y);
    }

    @Test
    public void testLargeCoordinates() {
        int largeX = Integer.MAX_VALUE;
        int largeY = Integer.MAX_VALUE;
        int largeHeight = Integer.MAX_VALUE;
        
        itemPile.x = largeX;
        itemPile.y = largeY;
        itemPile.height = largeHeight;
        
        assertEquals("Large X should be set correctly", largeX, itemPile.x);
        assertEquals("Large Y should be set correctly", largeY, itemPile.y);
        assertEquals("Large height should be set correctly", largeHeight, itemPile.height);
    }

    @Test
    public void testItemPileWithoutItems() {
        itemPile.x = 10;
        itemPile.y = 20;
        itemPile.height = 5;
        itemPile.uid = 999;
        itemPile.offsetY = 3;
        
        assertNull("TopItem should remain null", itemPile.topItem);
        assertNull("SecondItem should remain null", itemPile.secondItem);
        assertNull("ThirdItem should remain null", itemPile.thirdItem);
        
        assertEquals("Position and attributes should still work", 10, itemPile.x);
        assertEquals("Position and attributes should still work", 20, itemPile.y);
        assertEquals("Position and attributes should still work", 5, itemPile.height);
        assertEquals("Position and attributes should still work", 999, itemPile.uid);
        assertEquals("Position and attributes should still work", 3, itemPile.offsetY);
    }

    @Test
    public void testNullItemAssignments() {
        itemPile.topItem = testAnimable1;
        itemPile.topItem = null;
        
        assertNull("TopItem should be set to null", itemPile.topItem);
    }
}