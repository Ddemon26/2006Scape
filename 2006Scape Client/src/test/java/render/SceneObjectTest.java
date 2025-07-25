package render;

import static org.junit.Assert.*;

import org.junit.Test;
import render.objects.SceneObject;

public class SceneObjectTest {
  @Test
  public void testDefaultsAfterConstruction() {
    SceneObject obj = new SceneObject();
    assertEquals(0, obj.plane);
    assertEquals(0, obj.height);
    assertEquals(0, obj.x);
    assertEquals(0, obj.y);
    assertNull(obj.renderable);
    assertEquals(0, obj.orientation);
    assertEquals(0, obj.uid);
    assertEquals(0, obj.config);
  }
}
