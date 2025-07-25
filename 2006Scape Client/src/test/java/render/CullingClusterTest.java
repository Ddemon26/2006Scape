package render;

import static org.junit.Assert.*;

import core.world.CullingCluster;
import org.junit.Test;

public class CullingClusterTest {
  @Test
  public void testFieldsDefaultToZero() {
    CullingCluster cc = new CullingCluster();
    assertEquals(0, cc.minTileX);
    assertEquals(0, cc.maxTileX);
    assertEquals(0, cc.minTileZ);
    assertEquals(0, cc.maxTileZ);
    assertEquals(0, cc.type);
    assertEquals(0, cc.minX);
    assertEquals(0, cc.maxX);
    assertEquals(0, cc.minZ);
    assertEquals(0, cc.maxZ);
    assertEquals(0, cc.minY);
    assertEquals(0, cc.maxY);
  }
}
