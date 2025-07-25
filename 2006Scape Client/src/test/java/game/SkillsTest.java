package game;

import static org.junit.Assert.*;

import game.mechanics.Skills;
import org.junit.Test;

public class SkillsTest {
  @Test
  public void testSkillArrays() {
    assertEquals(Skills.skillsCount, Skills.skillNames.length);
    assertEquals(Skills.skillsCount, Skills.skillEnabled.length);
    assertTrue(Skills.skillEnabled[0]);
  }
}
