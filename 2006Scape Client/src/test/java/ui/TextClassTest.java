package ui;

import static org.junit.Assert.*;

import org.junit.Test;

public class TextClassTest {
  @Test
  public void testLongForNameCaseInsensitive() {
    long upper = TextClass.longForName("RuneBot");
    long lower = TextClass.longForName("runebot");
    assertEquals(upper, lower);
  }

  @Test
  public void testNameForLongRoundTrip() {
    long value = TextClass.longForName("RuneBot");
    assertEquals("runebot", TextClass.nameForLong(value));
  }

  @Test
  public void testHashSpriteNameDeterministic() {
    assertEquals(11943852L, TextClass.hashSpriteName("test"));
    assertEquals(TextClass.hashSpriteName("TEST"), TextClass.hashSpriteName("test"));
  }

  @Test
  public void testIntToIpString() {
    assertEquals("127.0.0.1", TextClass.intToIpString(0x7F000001));
  }

  @Test
  public void testFixName() {
    assertEquals("Hello World", TextClass.fixName("hello_world"));
  }

  @Test
  public void testPasswordAsterisks() {
    assertEquals("******", TextClass.passwordAsterisks("secret"));
  }
}
