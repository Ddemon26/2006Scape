package render;

import static org.junit.Assert.*;

import org.junit.Test;
import render.objects.WallDecoration;

public class WallDecorationTest {
  @Test
  public void testDefaultsAfterConstruction() {
    WallDecoration d = new WallDecoration();
    assertEquals(0, d.plane);
    assertEquals(0, d.x);
    assertEquals(0, d.y);
    assertEquals(0, d.orientationFlags);
    assertEquals(0, d.orientation);
    assertNull(d.renderable);
    assertEquals(0, d.uid);
    assertEquals(0, d.config);
  }
}
