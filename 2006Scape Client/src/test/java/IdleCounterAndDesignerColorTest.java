import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Original-name observations for Game.cyclesSinceLastPacket (counted up by mainGameProcessor, zeroed by parsePacket, connection dropped past 750)
 * and Game.designerColorIndices (character-designer colour slots stepped by content types 314..323 and sent by type 326).
 */
public class IdleCounterAndDesignerColorTest {
    static class CycleSpy extends Game {
        int drops;
        @Override public boolean parsePacket() { return false; }
        @Override public void loadingStages() { }
        @Override public void advancePendingSceneChanges() { }
        @Override public void advancePlayerEntities() { }
        @Override public void advanceNpcEntities() { }
        @Override public void decrementOverheadTextTimers() { }
        @Override public void dropClient() { drops++; }
    }

    private ServerSocket listener; private Socket client, server; private RSInterface[] savedCache;
    @Before public void before() { savedCache = RSInterface.interfaceCache; }
    @After public void after() throws Exception {
        RSInterface.interfaceCache = savedCache;
        if (client != null) client.close();
        if (server != null) server.close();
        if (listener != null) listener.close();
    }

    private static CycleSpy cycleGame() {
        CycleSpy s = new CycleSpy();
        s.mouseDetection = new MouseDetection(s);
        s.stream = new Stream(new byte[5000]);
        s.stream.encryption = new ISAACRandomGen(new int[]{1, 2, 3, 4});
        s.loggedIn = true;
        s.aBoolean954 = true;
        return s;
    }
    private static void cycle(Game g) {
        try { g.mainGameProcessor(); } catch (RuntimeException e) { /* later code needs a live session */ }
    }

    @Test public void mainGameProcessorAddsOnePerCycleAndDropsOnlyAbove750() {
        CycleSpy s = cycleGame();
        s.cyclesSinceLastPacket = 0;
        cycle(s); assertEquals(1, s.cyclesSinceLastPacket);
        cycle(s); assertEquals(2, s.cyclesSinceLastPacket);
        assertEquals(0, s.drops);
        s.cyclesSinceLastPacket = 749;
        cycle(s); assertEquals(750, s.cyclesSinceLastPacket); assertEquals(0, s.drops);
        cycle(s); assertEquals(751, s.cyclesSinceLastPacket); assertEquals(1, s.drops);
        cycle(s); assertEquals(752, s.cyclesSinceLastPacket); assertEquals(2, s.drops);
    }

    private RSSocket open() throws Exception {
        listener = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"));
        client = new Socket(InetAddress.getByName("127.0.0.1"), listener.getLocalPort());
        server = listener.accept();
        return new RSSocket(null, client);
    }
    private void send(RSSocket rs, int total, byte... b) throws Exception {
        server.getOutputStream().write(b); server.getOutputStream().flush();
        long end = System.currentTimeMillis() + 5000;
        while (rs.available() < total && System.currentTimeMillis() < end) Thread.sleep(5);
        assertEquals(total, rs.available());
    }

    @Test public void parsePacketZeroesCounterOnlyWhenAWholePacketArrives() throws Exception {
        RSInterface.interfaceCache = new RSInterface[]{new RSInterface()};
        Game g = new Game();
        g.inStream = new Stream(new byte[200]);
        RSSocket rs = open();
        g.socketStream = rs;
        g.pktType = 70; g.pktSize = 6;
        g.cyclesSinceLastPacket = 300;
        assertFalse(g.parsePacket());          // nothing available
        assertEquals(300, g.cyclesSinceLastPacket);
        send(rs, 3, (byte) 0, (byte) 5, (byte) 6);
        assertFalse(g.parsePacket());          // 3 of 6 payload bytes
        assertEquals(300, g.cyclesSinceLastPacket);
        send(rs, 6, (byte) 0, (byte) 0, (byte) 0);
        assertTrue(g.parsePacket());           // packet complete
        assertEquals(0, g.cyclesSinceLastPacket);
        assertEquals(-1, g.pktType);
    }

    private static RSInterface button(int type) { RSInterface r = new RSInterface(); r.contentType = type; return r; }
    private static Game designer() {
        Game g = new Game();
        g.stream = new Stream(new byte[100]);
        g.stream.encryption = new ISAACRandomGen(new int[]{1, 2, 3, 4});
        Arrays.fill(g.designerKitIds, -1);
        g.designerMale = true;
        return g;
    }

    @Test public void contentTypes314To323StepOneColourSlotWithWrap() {
        Game g = designer();
        int[][] pal = Game.appearanceColorPalettes;
        assertEquals(5, g.designerColorIndices.length);
        g.designerModelDirty = false;
        g.promptUserForInput(button(315));     // slot 0 forward
        assertArrayEquals(new int[]{1, 0, 0, 0, 0}, g.designerColorIndices);
        assertTrue(g.designerModelDirty);
        g.promptUserForInput(button(314));     // slot 0 back
        g.promptUserForInput(button(314));     // wraps below 0 to last palette entry
        assertArrayEquals(new int[]{pal[0].length - 1, 0, 0, 0, 0}, g.designerColorIndices);
        g.promptUserForInput(button(315));     // forward past last wraps to 0
        assertArrayEquals(new int[]{0, 0, 0, 0, 0}, g.designerColorIndices);
        for (int slot = 1; slot < 5; slot++) {
            g.promptUserForInput(button(314 + slot * 2));     // back from 0 wraps to this slot's palette length - 1
            assertEquals(pal[slot].length - 1, g.designerColorIndices[slot]);
            g.promptUserForInput(button(315 + slot * 2));
            assertEquals(0, g.designerColorIndices[slot]);
            g.promptUserForInput(button(315 + slot * 2));
            assertEquals(1, g.designerColorIndices[slot]);
        }
        assertArrayEquals(new int[]{0, 1, 1, 1, 1}, g.designerColorIndices);
        assertNotEquals(pal[1].length, pal[3].length);       // wrap point really differs per slot
        int[] before = g.designerColorIndices.clone();
        g.designerModelDirty = false;
        g.promptUserForInput(button(313));     // kit button (kit -1): colours untouched
        g.promptUserForInput(button(324));     // already male: no-op
        assertArrayEquals(before, g.designerColorIndices);
        assertFalse(g.designerModelDirty);
    }

    @Test public void contentType326SendsKitsThenTheFiveColourSlots() {
        Game g = designer();
        for (int i = 0; i < 7; i++) g.designerKitIds[i] = 20 + i;
        int[] colours = {3, 5, 9, 2, 7};
        System.arraycopy(colours, 0, g.designerColorIndices, 0, 5);
        g.designerMale = false;
        assertTrue(g.promptUserForInput(button(326)));
        byte[] b = g.stream.buffer;
        assertEquals(1 + 1 + 7 + 5, g.stream.currentOffset);
        assertEquals((101 + new ISAACRandomGen(new int[]{1, 2, 3, 4}).getNextKey()) & 255, b[0] & 255);
        assertEquals(1, b[1]);
        for (int i = 0; i < 7; i++) assertEquals(20 + i, b[2 + i]);
        for (int i = 0; i < 5; i++) assertEquals(colours[i], b[9 + i]);
    }
}
