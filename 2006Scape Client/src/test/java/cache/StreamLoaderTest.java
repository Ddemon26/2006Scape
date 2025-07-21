package cache;

import org.junit.Test;

import static org.junit.Assert.*;

public class StreamLoaderTest {
    @Test
    public void testEmptyArchiveReturnsNull() {
        byte[] archive = new byte[] {
            0,0,8, 0,0,8, // lengths match
            0,0 // zero files
        };
        StreamLoader loader = new StreamLoader(archive);
        assertNull(loader.getFileData("test"));
    }
}
