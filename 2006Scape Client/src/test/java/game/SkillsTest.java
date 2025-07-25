package game;

import game.mechanics.Skills;
import org.junit.Test;
import static org.junit.Assert.*;

public class SkillsTest {
    @Test
    public void testSkillArrays() {
        assertEquals(Skills.skillsCount, Skills.skillNames.length);
        assertEquals(Skills.skillsCount, Skills.skillEnabled.length);
        assertTrue(Skills.skillEnabled[0]);
    }
}
