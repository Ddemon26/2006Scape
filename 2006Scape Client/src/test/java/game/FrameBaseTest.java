package game;

import core.network.Stream;
import game.animation.FrameBase;
import org.junit.Test;

import static org.junit.Assert.*;

public class FrameBaseTest {

    @Test
    public void testConstructorParsesData() {
        byte[] data = new byte[] {2, 1, 2, 1, 5, 2, 10, 11};
        Stream stream = new Stream(data);
        FrameBase base = new FrameBase(stream);
        assertArrayEquals(new int[] {1, 2}, base.transformationType);
        assertArrayEquals(new int[] {5}, base.transformationList[0]);
        assertArrayEquals(new int[] {10, 11}, base.transformationList[1]);
    }
}
