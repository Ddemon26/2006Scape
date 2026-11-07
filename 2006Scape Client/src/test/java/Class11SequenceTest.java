import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import org.junit.Test;

/** Behaviour of the MIDI sequence reader Class11, written against its original names. */
public class Class11SequenceTest {

    /** Event bytes: first the delta stored as 128 - delta, then the raw bytes. */
    private static byte[] ev(int delta, int... raw) {
        byte[] b = new byte[raw.length + 1];
        b[0] = (byte) (128 - delta);
        for (int i = 0; i < raw.length; i++) b[i + 1] = (byte) raw[i];
        return b;
    }

    private static byte[] cat(byte[]... parts) {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        for (byte[] p : parts) o.write(p, 0, p.length);
        return o.toByteArray();
    }

    private static byte[] chunk(String tag, byte[] body) {
        byte[] head = { (byte) tag.charAt(0), (byte) tag.charAt(1), (byte) tag.charAt(2), (byte) tag.charAt(3),
            0, 0, (byte) (body.length >> 8), (byte) body.length };
        return cat(head, body);
    }

    private static byte[] song(int division, int declaredTracks, byte[]... chunks) {
        byte[] head = { 'M', 'T', 'h', 'd', 0, 0, 0, 6, 0, 1, (byte) (declaredTracks >> 8), (byte) declaredTracks,
            (byte) (division >> 8), (byte) division };
        return cat(head, cat(chunks));
    }

    private static Class11 load(byte[] data) {
        Class11 r = new Class11();
        r.load(data);
        return r;
    }

    /** Reads delta then event of the given track from its start. */
    private static int first(Class11 r, int track) {
        r.restoreTrackPosition(track);
        r.readDelta(track);
        return r.readEvent(track);
    }

    @Test public void loadReadsTheDivisionCountsOnlyTrackChunksAndStartsEachTrackAtItsOwnData() {
        byte[] t0 = ev(9, 0x90, 60, 64);
        byte[] t1 = ev(4, 0x80, 61, 0);
        Class11 r = load(song(480, 2, chunk("MTrk", t0), chunk("XXXX", new byte[] { 1, 2, 3, 4, 5 }), chunk("MTrk", t1)));
        assertEquals(480, r.division);
        assertEquals(2, r.trackCount());
        assertTrue(r.isLoaded());
        assertEquals(0x90 | 60 << 8 | 64 << 16, first(r, 0));
        assertEquals(9, r.trackTicks[0]);
        assertEquals(0x80 | 61 << 8, first(r, 1));
        assertEquals(4, r.trackTicks[1]);
        Class11 one = load(song(96, 1, chunk("MTrk", t0), chunk("MTrk", t1)));
        assertEquals("only the declared number of track chunks", 1, one.trackCount());
        assertEquals(96, one.division);
    }

    @Test public void deltaIsOneByteStoredAs128MinusValueAndAccumulatesIntoTheTrackTick() {
        Class11 r = load(song(480, 1, chunk("MTrk", cat(ev(7, 0xC0, 1), ev(0, 0xC0, 2), ev(200, 0xC0, 3), ev(3, 0xC0, 4)))));
        r.restoreTrackPosition(0);
        int[] expected = { 7, 7, 207, 210 };
        for (int e : expected) {
            r.readDelta(0);
            assertEquals(e, r.trackTicks[0]);
            r.readEvent(0);
        }
    }

    @Test public void eventsCarryTheirDataBytesAndReuseTheRunningStatus() {
        byte[] t = cat(ev(0, 0xC0, 5), ev(0, 0x90, 60, 64), ev(10, 62, 65), ev(0, 0xE0, 1, 2), ev(0, 0xD0, 7));
        Class11 r = load(song(480, 1, chunk("MTrk", t)));
        r.restoreTrackPosition(0);
        r.readDelta(0);
        assertEquals(0xC0 | 5 << 8, r.readEvent(0));
        r.readDelta(0);
        assertEquals(0x90 | 60 << 8 | 64 << 16, r.readEvent(0));
        r.readDelta(0);
        assertEquals("running status reuses 0x90", 0x90 | 62 << 8 | 65 << 16, r.readEvent(0));
        assertEquals(10, r.trackTicks[0]);
        r.readDelta(0);
        assertEquals(0xE0 | 1 << 8 | 2 << 16, r.readEvent(0));
        r.readDelta(0);
        assertEquals(0xD0 | 7 << 8, r.readEvent(0));
    }

    @Test public void everyChannelStatusConsumesItsOwnNumberOfDataBytes() {
        for (int s = 0x80; s < 0xF0; s++) {
            int n = (s & 0xF0) == 0xC0 || (s & 0xF0) == 0xD0 ? 1 : 2;
            byte[] first = n == 1 ? ev(0, s, 0x11) : ev(0, s, 0x11, 0x22);
            Class11 r = load(song(480, 1, chunk("MTrk", cat(first, ev(0, 0xC0, 0x33)))));
            r.restoreTrackPosition(0);
            r.readDelta(0);
            int m = r.readEvent(0);
            assertEquals(s | 0x11 << 8 | (n == 2 ? 0x22 << 16 : 0), m);
            r.readDelta(0);
            assertEquals("status " + s, 0xC0 | 0x33 << 8, r.readEvent(0));
        }
    }

    @Test public void metaEventsReportEndOfTrackTempoOrOtherAndSysexIsSkipped() {
        byte[] t = cat(ev(0, 0xFF, 0x01, 128 - 2, 'a', 'b'), ev(0, 0xF0, 128 - 2, 9, 9),
            ev(0, 0xFF, 0x51, 128 - 3, 0x03, 0xD0, 0x90), ev(0, 0xFF, 0x2F, 128 - 0), ev(0, 0xC0, 1));
        Class11 r = load(song(480, 1, chunk("MTrk", t)));
        r.restoreTrackPosition(0);
        r.readDelta(0);
        assertEquals("other meta", 3, r.readEvent(0));
        r.readDelta(0);
        assertEquals("sysex", 0, r.readEvent(0));
        r.readDelta(0);
        assertEquals("tempo", 2, r.readEvent(0));
        r.readDelta(0);
        assertEquals("end of track", 1, r.readEvent(0));
        r.readDelta(0);
        assertEquals("the length byte skipped the right amount", 0xC0 | 1 << 8, r.readEvent(0));
    }

    @Test public void ticksConvertToTimeAtTheTempoAndATempoChangeKeepsEarlierTimeContinuous() {
        byte[] t = cat(ev(4, 0xFF, 0x51, 128 - 3, 0x03, 0xD0, 0x90), ev(4, 0xC0, 1));
        Class11 r = load(song(480, 1, chunk("MTrk", t)));
        assertEquals(0L, r.tickToScaledTime(0));
        assertEquals(5000000L, r.tickToScaledTime(10));
        r.restoreTrackPosition(0);
        r.readDelta(0);
        assertEquals(2, r.readEvent(0)); // tempo 250000 at tick 4
        assertEquals(2000000L, r.tickToScaledTime(4));
        assertEquals(3000000L, r.tickToScaledTime(8));
        assertEquals(250000L, r.tickToScaledTime(9) - r.tickToScaledTime(8));
    }

    @Test public void savedPositionsRestoreAndTheEndMarkerEndsTheTrackUntilItIsSaved() {
        byte[] t = cat(ev(0, 0xC0, 1), ev(0, 0xC0, 2));
        Class11 r = load(song(480, 1, chunk("MTrk", t)));
        r.restoreTrackPosition(0);
        assertFalse(r.isTrackEnded());
        r.readDelta(0);
        r.readEvent(0);
        r.saveTrackPosition(0);                       // saved after the first event
        r.readDelta(0);
        assertEquals(0xC0 | 2 << 8, r.readEvent(0));
        r.restoreTrackPosition(0);                       // back to the saved position
        r.readDelta(0);
        assertEquals(0xC0 | 2 << 8, r.readEvent(0));
        assertFalse(r.allTracksEnded());
        r.markTrackEnded();
        assertTrue(r.isTrackEnded());
        r.restoreTrackPosition(0);
        assertFalse("the end marker was never saved", r.isTrackEnded());
        r.markTrackEnded();
        r.saveTrackPosition(0);
        assertTrue(r.allTracksEnded());
        r.restoreTrackPosition(0);
        assertTrue(r.isTrackEnded());
    }

    @Test public void nextTrackIsTheUnfinishedTrackWithTheSmallestTickAndTheFirstOnATie() {
        Class11 r = load(song(480, 3, chunk("MTrk", ev(9, 0xC0, 1)), chunk("MTrk", ev(4, 0xC0, 1)), chunk("MTrk", ev(4, 0xC0, 1))));
        for (int i = 0; i < 3; i++) {
            r.restoreTrackPosition(i);
            r.readDelta(i);
            r.saveTrackPosition(i);
        }
        assertEquals(1, r.nextTrack());
        r.restoreTrackPosition(1);
        r.markTrackEnded();
        r.saveTrackPosition(1);
        assertEquals(2, r.nextTrack());
        r.restoreTrackPosition(2);
        r.markTrackEnded();
        r.saveTrackPosition(2);
        assertEquals(0, r.nextTrack());
        r.restoreTrackPosition(0);
        r.markTrackEnded();
        r.saveTrackPosition(0);
        assertEquals(-1, r.nextTrack());
        assertTrue(r.allTracksEnded());
    }

    @Test public void restartRewindsEveryTrackToItsFirstEventAtTheGivenTimeKeepingTheTempo() {
        byte[] a = cat(ev(6, 0xFF, 0x51, 128 - 3, 0x03, 0xD0, 0x90), ev(2, 0x90, 60, 64));
        byte[] b = cat(ev(3, 0x80, 61, 0));
        Class11 r = load(song(480, 2, chunk("MTrk", a), chunk("MTrk", b)));
        first(r, 0);
        r.trackTicks[0] = 50;
        r.markTrackEnded();
        r.saveTrackPosition(0);
        first(r, 1);
        r.markTrackEnded();
        r.saveTrackPosition(1);
        r.restart(777L);
        assertEquals(777L, r.tickToScaledTime(0));
        assertEquals(6, r.trackTicks[0]);
        assertEquals(3, r.trackTicks[1]);
        assertFalse(r.allTracksEnded());
        r.restoreTrackPosition(0);
        assertEquals("first event follows the first delta", 2, r.readEvent(0));
        r.restoreTrackPosition(1);
        assertEquals(0x80 | 61 << 8, r.readEvent(1));
        assertEquals(1, r.nextTrack());
    }

    @Test public void releaseDropsTheSong() {
        Class11 r = load(song(480, 1, chunk("MTrk", ev(0, 0xC0, 1))));
        assertTrue(r.isLoaded());
        r.release();
        assertFalse(r.isLoaded());
        assertFalse(new Class11().isLoaded());
    }
}
