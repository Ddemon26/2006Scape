package render;

import org.junit.Test;

import static org.junit.Assert.*;

public class PlainTileTest {
    @Test
    public void testConstructorSetsFields() {
        PlainTile tile = new PlainTile(1, 2, 3, 4, 5, 6, false);
        assertEquals(1, tile.southWestColor);
        assertEquals(2, tile.southEastColor);
        assertEquals(3, tile.northEastColor);
        assertEquals(4, tile.northWestColor);
        assertEquals(5, tile.textureId);
        assertEquals(6, tile.orientation);
        assertFalse(tile.flatShade);
    }
}
