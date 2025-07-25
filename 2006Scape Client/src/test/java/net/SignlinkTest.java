package net;

import core.network.Signlink;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

public class SignlinkTest {
    @Test
    public void testSetVolumeWithoutSynthesizer() {
        Signlink.synthesizer = null;
        assertFalse(Signlink.setVolume(100));
    }

    @Test
    public void testFindcachedirCreatesPath() {
        String tmp = System.getProperty("java.io.tmpdir");
        System.setProperty("user.home", tmp);
        String path = Signlink.findcachedir();
        assertTrue(path.startsWith(tmp));
        assertTrue(new File(path).exists());
    }
}
