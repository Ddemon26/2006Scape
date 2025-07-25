package game;

import static org.junit.Assert.*;

import game.entities.Animable;
import org.junit.Test;

public class AnimableTest {
  @Test
  public void testDefaultModelHeight() {
    Animable animable = new Animable();
    assertEquals(1000, animable.modelHeight);
  }
}
