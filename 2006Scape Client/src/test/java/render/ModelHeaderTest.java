package render;

import org.junit.Test;
import render.geometry.ModelHeader;

import static org.junit.Assert.*;

public class ModelHeaderTest {
    @Test
    public void testDefaultsAfterConstruction() {
        ModelHeader header = new ModelHeader();
        assertNull(header.data);
        assertEquals(0, header.vertexCount);
        assertEquals(0, header.faceCount);
        assertEquals(0, header.texturedTriangleCount);
        assertEquals(0, header.vertexFlagsOffset);
        assertEquals(0, header.vertexXOffset);
        assertEquals(0, header.vertexYOffset);
        assertEquals(0, header.vertexZOffset);
        assertEquals(0, header.vertexSkinsOffset);
        assertEquals(0, header.faceTypeOffset);
        assertEquals(0, header.facePriorityOffset);
        assertEquals(0, header.faceSkinOffset);
        assertEquals(0, header.faceLabelOffset);
        assertEquals(0, header.faceAlphaOffset);
        assertEquals(0, header.faceTextureOffset);
        assertEquals(0, header.vertexLabelOffset);
        assertEquals(0, header.faceIndicesOffset);
    }
}
