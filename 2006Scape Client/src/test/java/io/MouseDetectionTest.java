package io;

import static org.junit.Assert.*;

import org.junit.Test;

public class MouseDetectionTest {
  @Test
  public void testConstructorInitializesFields() {
    MouseDetection md = new MouseDetection(null);
    assertEquals(500, md.coordsX.length);
    assertEquals(500, md.coordsY.length);
    assertTrue(md.running);
    assertEquals(0, md.coordsIndex);
  }
}
