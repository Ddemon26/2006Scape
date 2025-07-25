package render;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import org.junit.Test;
import render.core.Sprite;

public class SpriteTest {
  @Test
  public void testAdjustRgbClampsValues() {
    Sprite sprite = new Sprite(1, 1);
    sprite.pixels[0] = 0x000102;
    sprite.adjustRgb(-10, 300, 0);
    assertEquals(0x0101FF & 0xFFFFFF, sprite.pixels[0]);
  }

  @Test
  public void testCropExpandsCanvas() throws Exception {
    Sprite sprite = new Sprite(2, 2);
    sprite.pixels[0] = 1;
    sprite.pixels[1] = 2;
    sprite.pixels[2] = 3;
    sprite.pixels[3] = 4;
    sprite.trimWidth = 3;
    sprite.trimHeight = 3;
    Field offX = Sprite.class.getDeclaredField("offsetX");
    Field offY = Sprite.class.getDeclaredField("offsetY");
    offX.setAccessible(true);
    offY.setAccessible(true);
    offX.setInt(sprite, 1);
    offY.setInt(sprite, 1);
    sprite.crop();
    assertEquals(3, sprite.width);
    assertEquals(3, sprite.height);
    int[] expected = {
      0, 0, 0,
      0, 1, 2,
      0, 3, 4
    };
    assertArrayEquals(expected, sprite.pixels);
  }
}
