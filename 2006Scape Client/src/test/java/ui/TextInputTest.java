package ui;

import static org.junit.Assert.*;

import core.network.Stream;
import org.junit.Test;

public class TextInputTest {

  @Test
  public void testEncodeDecodeRoundTrip() {
    Stream stream = new Stream(new byte[100]);
    TextInput.encodeChatMessage("hello", stream);
    int len = stream.currentOffset;
    stream.currentOffset = 0;
    String result = TextInput.decodeChatMessage(len, stream);
    assertEquals("Hello", result);
  }

  @Test
  public void testProcessText() {
    assertEquals("Hello", TextInput.processText("hello"));
  }
}
