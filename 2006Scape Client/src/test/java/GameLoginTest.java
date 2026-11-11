import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.net.*;
import java.security.*;
import java.security.interfaces.*;
import java.util.*;
import java.util.concurrent.*;
import org.junit.*;
import static org.junit.Assert.*;

/** Wire-level and state-reset behaviour of Game.login against a scripted loopback server. */
public class GameLoginTest {
    /** Server-side record of one connection. */
    static final class Conn {
        int[] hello = new int[2];
        long serverKey;
        int opcode = -1, size = -1;
        byte[] block;
        int[] plain; String name, password;
    }
    /** Connection script: first response (-1 = send none and close), then optional bytes after the login block. */
    static final class Script {
        int first; byte[] afterFirst = new byte[0]; boolean full; byte[] afterBlock = new byte[0];
    }
    static final class TestGame extends Game {
        int serverPort; List<String> drawn = new ArrayList<String>(); List<Integer> ports = new ArrayList<Integer>();
        boolean failOpen;
        @Override public Socket openSocket(int port) throws IOException {
            ports.add(port);
            if (failOpen) throw new IOException("fixture");
            return new Socket("127.0.0.1", serverPort);
        }
        @Override public void drawLoginScreen(boolean b) { drawn.add(loginMessage1 + "|" + loginMessage2); }
        @Override public void resetImageProducers2() { }
    }

    private ServerSocket server; private Thread thread; private BigInteger priv, modulus, oldModulus;
    private final BlockingQueue<Conn> conns = new LinkedBlockingQueue<Conn>();
    private final Queue<Script> scripts = new ConcurrentLinkedQueue<Script>();
    private TestGame g; private Player savedPlayer; private boolean savedLowMem, savedFlagged; private String savedErr;

    static void setStaticFinal(Class<?> c, String name, Object v) throws Exception {
        Field f = c.getDeclaredField(name); f.setAccessible(true);
        Field m = Field.class.getDeclaredField("modifiers"); m.setAccessible(true);
        m.setInt(f, f.getModifiers() & ~Modifier.FINAL); f.set(null, v);
    }

    @Before public void setup() throws Exception {
        KeyPairGenerator kg = KeyPairGenerator.getInstance("RSA"); kg.initialize(1024);
        KeyPair kp = kg.generateKeyPair();
        modulus = ((RSAPublicKey) kp.getPublic()).getModulus(); priv = ((RSAPrivateKey) kp.getPrivate()).getPrivateExponent();
        oldModulus = ClientSettings.RSA_MODULUS;
        setStaticFinal(ClientSettings.class, "RSA_MODULUS", modulus);
        savedPlayer = Game.myPlayer; savedLowMem = Game.lowMem; savedFlagged = Game.flagged; savedErr = Signlink.errorname;
        server = new ServerSocket(0, 5, InetAddress.getByName("127.0.0.1"));
        thread = new Thread(new Runnable() { public void run() {
            try { while (true) { Socket s = server.accept(); handle(s); } } catch (Exception e) { }
        } });
        thread.setDaemon(true); thread.start();
        g = new TestGame(); g.serverPort = server.getLocalPort();
        g.mouseDetection = new MouseDetection(g);
    }
    @After public void teardown() throws Exception {
        server.close(); setStaticFinal(ClientSettings.class, "RSA_MODULUS", oldModulus);
        Game.myPlayer = savedPlayer; Game.lowMem = savedLowMem; Game.flagged = savedFlagged; Signlink.errorname = savedErr;
    }

    private void handle(Socket s) throws Exception {
        Conn c = new Conn(); Script sc = scripts.poll();
        try {
            DataInputStream in = new DataInputStream(s.getInputStream()); OutputStream out = s.getOutputStream();
            c.hello[0] = in.readUnsignedByte(); c.hello[1] = in.readUnsignedByte();
            out.write(new byte[8]);
            if (sc.first >= 0) out.write(sc.first);
            out.write(sc.afterFirst); out.flush();
            if (sc.full) {
                c.serverKey = 0x1122334455667788L ^ System.nanoTime();
                DataOutputStream d = new DataOutputStream(out); d.writeLong(c.serverKey); d.flush();
                c.opcode = in.readUnsignedByte(); c.size = in.readUnsignedByte();
                c.block = new byte[c.size]; in.readFully(c.block);
                decode(c);
                out.write(sc.afterBlock); out.flush();
            }
            Thread.sleep(150);
        } finally { conns.add(c); s.close(); }
    }
    private void decode(Conn c) throws Exception {
        // layout after the 2 header bytes: 255, short 1, lowMem, 9 CRCs, rsaLen, rsa bytes
        int rsaLen = c.block[40] & 255;
        byte[] rsa = Arrays.copyOfRange(c.block, 41, 41 + rsaLen);
        byte[] m = new BigInteger(rsa).modPow(priv, modulus).toByteArray();
        DataInputStream d = new DataInputStream(new ByteArrayInputStream(m));
        assertEquals(10, d.readUnsignedByte());
        c.plain = new int[5]; for (int i = 0; i < 5; i++) c.plain[i] = d.readInt();
        StringBuilder sb = new StringBuilder(); int ch;
        while ((ch = d.read()) != 10) sb.append((char) ch); c.name = sb.toString();
        sb = new StringBuilder(); while ((ch = d.read()) != 10) sb.append((char) ch); c.password = sb.toString();
    }
    private Script s(int first, boolean full, int... afterBlock) {
        Script sc = new Script(); sc.first = first; sc.full = full;
        sc.afterBlock = new byte[afterBlock.length]; for (int i = 0; i < afterBlock.length; i++) sc.afterBlock[i] = (byte) afterBlock[i];
        scripts.add(sc); return sc;
    }
    private Conn next() throws Exception { Conn c = conns.poll(10, TimeUnit.SECONDS); assertNotNull(c); return c; }

    private static long base37(String n) {
        long l = 0; for (int i = 0; i < n.length() && i < 12; i++) {
            char c = n.charAt(i); l *= 37;
            if (c >= 'A' && c <= 'Z') l += c - 'A' + 1; else if (c >= 'a' && c <= 'z') l += c - 'a' + 1; else if (c >= '0' && c <= '9') l += c - '0' + 27;
        } return l;
    }

    @Test public void handshakeCarriesNameHashByteForDifferentNames() throws Exception {
        String[] names = {"smokeone", "zq", "Mix3d"};
        Set<Integer> bytes = new HashSet<Integer>();
        for (String n : names) {
            s(3, false);
            g.login(n, "pw", true);
            Conn c = next();
            assertEquals(14, c.hello[0]);
            assertEquals((int) (base37(n) >> 16 & 31L), c.hello[1]);
            assertEquals(n, Signlink.errorname);
            bytes.add(c.hello[1]);
        }
        assertTrue("fixture must distinguish a constant byte", bytes.size() > 1);
    }

    @Test public void loginBlockHeaderRsaPlaintextAndCrcOrder() throws Exception {
        for (int pass = 0; pass < 2; pass++) {
            boolean flag = pass == 1; Game.lowMem = flag;
            for (int i = 0; i < 9; i++) g.expectedCRCs[i] = 1000 + i * 77;
            s(0, true, 3);
            g.login("Alice", flag ? "S3cret" : "Other", flag);
            Conn c = next();
            assertEquals(flag ? 18 : 16, c.opcode);
            assertEquals(c.block.length, c.size);
            assertEquals(255, c.block[0] & 255);
            assertEquals(0, c.block[1]); assertEquals(1, c.block[2]);
            assertEquals(flag ? 1 : 0, c.block[3]);
            DataInputStream d = new DataInputStream(new ByteArrayInputStream(c.block, 4, 36));
            for (int i = 0; i < 9; i++) assertEquals(1000 + i * 77, d.readInt());
            assertEquals(c.size, (c.block[40] & 255) + 1 + 36 + 1 + 1 + 2);
            assertEquals("Alice", c.name); assertEquals(flag ? "S3cret" : "Other", c.password);
            assertTrue(c.plain[0] >= 0 && c.plain[0] < 99999999 && c.plain[1] >= 0 && c.plain[1] < 99999999);
            assertEquals((int) (c.serverKey >> 32), c.plain[2]); assertEquals((int) c.serverKey, c.plain[3]);
            assertEquals(ClientSettings.UID, c.plain[4]);
        }
    }

    @Test public void cipherSeedsAreSeedAndSeedPlus50ForOutAndIn() throws Exception {
        s(0, true, 2, 0, 0);
        g.login("Alice", "pw", true);
        Conn c = next();
        int[] seed = {c.plain[0], c.plain[1], c.plain[2], c.plain[3]};
        int[] seed50 = {seed[0] + 50, seed[1] + 50, seed[2] + 50, seed[3] + 50};
        ISAACRandomGen out = new ISAACRandomGen(seed), in = new ISAACRandomGen(seed50);
        for (int i = 0; i < 5; i++) {
            assertEquals(out.getNextKey(), g.stream.encryption.getNextKey());
            assertEquals(in.getNextKey(), g.encryption.getNextKey());
        }
        assertNotEquals(new ISAACRandomGen(seed).getNextKey(), new ISAACRandomGen(seed50).getNextKey());
    }

    @Test public void successResponseReadsPrivilegeAndFlaggedAndResetsState() throws Exception {
        int maxP = g.maxPlayers, me = g.myPlayerIndex;
        Arrays.fill(g.chatMessages, "x");
        g.playerArray = new Player[maxP + 1]; Arrays.fill(g.playerArray, new Player());
        g.cachedPlayerAppearances = new Stream[maxP + 1]; Arrays.fill(g.cachedPlayerAppearances, new Stream(new byte[1]));
        g.npcArray = new NPC[16385]; Arrays.fill(g.npcArray, new NPC());
        g.groundArray = new NodeList[5][105][105];
        for (NodeList[][] a : g.groundArray) for (NodeList[] b : a) Arrays.fill(b, new NodeList());
        Arrays.fill(g.designerColorIndices, 7);
        Arrays.fill(g.atPlayerActions, "a");
        Arrays.fill(g.atPlayerArray, true);
        g.myPrivilege = 0; Game.flagged = false;
        s(0, true, 2, 2, 1);
        g.login("Alice", "pw", true);
        next();
        assertTrue(g.loggedIn); assertEquals(2, g.myPrivilege); assertTrue(Game.flagged);
        assertEquals(100, g.chatMessages.length);
        for (int i = 0; i < 100; i++) assertNull(g.chatMessages[i]);
        for (int i = 0; i < maxP; i++) {
            if (i == me) continue;
            assertNull(g.playerArray[i]); assertNull(g.cachedPlayerAppearances[i]);
        }
        assertSame(g.playerArray[me], Game.myPlayer); assertNotNull(Game.myPlayer);
        assertNotNull(g.playerArray[maxP]); assertNotNull(g.cachedPlayerAppearances[maxP]);
        for (int i = 0; i < 16384; i++) assertNull(g.npcArray[i]);
        assertNotNull(g.npcArray[16384]);
        for (int p = 0; p < 5; p++) for (int x = 0; x < 105; x++) for (int y = 0; y < 105; y++)
            assertEquals("p" + p + "x" + x + "y" + y, p < 4 && x < 104 && y < 104, g.groundArray[p][x][y] == null);
        assertEquals(5, g.designerColorIndices.length); assertEquals(5, g.atPlayerActions.length); assertEquals(5, g.atPlayerArray.length);
        for (int i = 0; i < 5; i++) { assertEquals(0, g.designerColorIndices[i]); assertNull(g.atPlayerActions[i]); assertFalse(g.atPlayerArray[i]); }
        assertTrue(g.drawn.isEmpty());
    }

    private static void setDoActionCounters(int v) {
        Game.itemThirdOptionDecoyCounter = Game.npcThirdClickDecoyCounter = Game.attackPlayerDecoyCounter = Game.objectFifthClickDecoyCounter = Game.challengeDecoyCounter = Game.npcFifthOptionDecoyCounter = Game.npcSecondClickDecoyCounter = v;
    }
    private static void assertDoActionCounters(int v) {
        assertEquals(v, Game.itemThirdOptionDecoyCounter); assertEquals(v, Game.npcThirdClickDecoyCounter); assertEquals(v, Game.attackPlayerDecoyCounter);
        assertEquals(v, Game.objectFifthClickDecoyCounter); assertEquals(v, Game.challengeDecoyCounter); assertEquals(v, Game.npcFifthOptionDecoyCounter); assertEquals(v, Game.npcSecondClickDecoyCounter);
    }
    @Test public void successResponseZeroesTheDoActionCountersButNotTheSelectedSpell() throws Exception {
        try {
            setDoActionCounters(7);
            g.selectedSpellInterfaceId = 1162;
            s(0, true, 3);                                // a rejected login resets nothing
            g.login("Alice", "pw", true);
            next();
            assertFalse(g.loggedIn);
            assertDoActionCounters(7);
            s(0, true, 2, 2, 1);
            g.login("Alice", "pw", true);
            next();
            assertTrue(g.loggedIn);
            assertDoActionCounters(0);
            assertEquals(1162, g.selectedSpellInterfaceId);              // not part of the reset
        } finally { setDoActionCounters(0); }
    }

    @Test public void successResponseResetsClickTimeRepeatCountFocusAndSampleCountButNotLastSampleOrSendCounter() throws Exception {
        g.lastReportedClickTime = 123456789L; g.mouseSampleRepeatCount = 17; g.lastMouseSampleX = 11; g.lastMouseSampleY = 22; g.cyclesSinceLastSend = 33;
        g.focusReportedToServer = false; g.awtFocus = false; g.mouseDetection.sampleCount = 77;
        s(3, false);                                      // refused login: none of the state is touched
        g.login("Alice", "pw", true);
        next();
        assertFalse(g.loggedIn);
        assertEquals(123456789L, g.lastReportedClickTime); assertEquals(17, g.mouseSampleRepeatCount); assertFalse(g.focusReportedToServer); assertFalse(g.awtFocus);
        assertEquals(77, g.mouseDetection.sampleCount);
        s(0, true, 2, 0, 0);
        g.login("Alice", "pw", true);
        next();
        assertTrue(g.loggedIn);
        assertEquals(0L, g.lastReportedClickTime); assertEquals(0, g.mouseSampleRepeatCount);
        assertTrue(g.focusReportedToServer); assertTrue(g.awtFocus);
        assertEquals(0, g.mouseDetection.sampleCount);
        assertEquals(11, g.lastMouseSampleX); assertEquals(22, g.lastMouseSampleY); assertEquals(33, g.cyclesSinceLastSend);
    }

    @Test public void serverKeyIsStoredAndLoginBlockIsBuiltInItsOwnBuffer() throws Exception {
        long previousKey = 0;
        for (int pass = 0; pass < 2; pass++) {
            boolean flag = pass == 1;
            g.cyclesSinceLastPacket = 600;
            s(0, true, 3);
            g.login("Alice", "pw", flag);
            Conn c = next();
            assertEquals(c.serverKey, g.serverSessionKey);
            assertNotEquals(previousKey, g.serverSessionKey); previousKey = g.serverSessionKey;
            assertEquals((int) (g.serverSessionKey >> 32), c.plain[2]); assertEquals((int) g.serverSessionKey, c.plain[3]);
            assertNotSame(g.stream, g.loginBuffer);
            assertEquals(c.size + 2, g.loginBuffer.currentOffset);
            assertEquals(c.opcode, g.loginBuffer.buffer[0] & 255);
            assertEquals(c.size, g.loginBuffer.buffer[1] & 255);
            for (int i = 0; i < c.size; i++) assertEquals(c.block[i], g.loginBuffer.buffer[2 + i]);
            assertFalse(g.loggedIn);
            assertEquals(600, g.cyclesSinceLastPacket);
        }
        g.cyclesSinceLastPacket = 600;
        s(0, true, 2, 0, 0);
        g.login("Alice", "pw", false);
        next();
        assertTrue(g.loggedIn); assertEquals(0, g.cyclesSinceLastPacket);
    }

    @Test public void failureResponsesShowTheirMessages() throws Exception {
        String W = ClientSettings.SERVER_WEBSITE, N = ClientSettings.SERVER_NAME;
        Object[][] t = {
            {3, "", "Invalid username or password."}, {4, "Your account has been disabled.", "Or you entered an invalid character for user/pass"},
            {5, "Your account is already logged in.", "Try again in 60 secs..."}, {6, N + " has been updated!", "Please reload this page."},
            {7, "This world is full.", "Please use a different world."}, {8, "Unable to connect.", "Login server offline."},
            {9, "Login limit exceeded.", "Too many connections from your address."}, {10, "Unable to connect.", "Bad session id."},
            {11, "Login server rejected session.", "Please try again."},
            {12, "Only staff are allowed to play right now.", "Please login using a staff account."},
            {13, "Could not complete login.", "Please try using a different world."},
            {14, "The server is being updated.", "Please wait 1 minute and try again."},
            {16, "Login attempts exceeded.", "Please wait 1 minute and try again."},
            {17, "You are standing in a members-only area.", "To play on this world move to a free area first"},
            {20, "Invalid loginserver requested", "Please try using a different world."},
            {30, "You need a forum account to play.", "Go to " + W + " to register."},
            {31, "You're using an invalid or outdated client.", "Get the latest version at " + W},
            {32, "You need to recover your account first,", "go to " + W + "/help for a tutorial!"},
            {33, "You need a members account to login to this world.", "Please subscribe, or use a different world."},
            {34, "You need to activate your forum account first.", "Click on confirm in the email we have send you!"},
            {99, "Unexpected server response", "Please try using a different world."},
        };
        for (Object[] row : t) {
            g.loginMessage1 = "old"; g.loginMessage2 = "old"; g.loggedIn = false;
            s((Integer) row[0], false);
            g.login("a", "b", true); next();
            assertEquals("code " + row[0], row[1], g.loginMessage1);
            assertEquals("code " + row[0], row[2], g.loginMessage2);
            assertFalse(g.loggedIn);
        }
    }

    @Test public void responseFifteenMarksLoggedInWithoutPrivilegeRead() throws Exception {
        g.myPrivilege = 9; s(15, false);
        g.login("a", "b", true); next();
        assertTrue(g.loggedIn); assertEquals(9, g.myPrivilege); assertEquals(0, g.menuActionRow);
        assertEquals(-1, g.pktType);
    }

    @Test public void firstResponseSelectsNoResponseVersusConnectError() throws Exception {
        // EOF instead of a first response: first response is -1 (not 0) -> "No response"
        s(-1, false);
        g.login("a", "b", true); next();
        assertEquals("No response from server", g.loginMessage1);
        assertEquals("Please try closing and opening your client again.", g.loginMessage2);
        // first response 0, handshake done, then EOF, retries exhausted -> connect error
        s(0, true); g.loginFailures = 2;
        g.login("a", "b", true); next();
        assertEquals("Error connecting to server.", g.loginMessage1);
        assertEquals(2, g.loginFailures);
        // with failures remaining it retries on a new connection and counts the failure
        g.loginFailures = 1; s(0, true); s(3, false);
        g.login("a", "b", true); next(); next();
        assertEquals(2, g.loginFailures); assertEquals("", g.loginMessage1);
        assertEquals("Invalid username or password.", g.loginMessage2);
    }

    @Test public void responseOneRetriesWithSameFlag() throws Exception {
        s(1, false); s(0, true, 3);
        g.login("Retry", "pw", true);
        next(); Conn c = next();
        assertEquals(18, c.opcode); assertEquals("Retry", c.name);
        assertEquals("Invalid username or password.", g.loginMessage2);
    }

    @Test public void countdownResponseCountsDownThenRetries() throws Exception {
        Script sc = s(21, false); sc.afterFirst = new byte[] {2};
        s(0, true, 3);
        g.login("a", "b", true);
        next(); next();
        List<String> seconds = new ArrayList<String>();
        for (String d : g.drawn) seconds.add(d.substring(d.indexOf('|') + 1));
        assertEquals(Arrays.asList(
            "Your profile will be transferred in: 2 seconds", "Your profile will be transferred in: 1 seconds",
            "Your profile will be transferred in: 0 seconds"), seconds);
        assertEquals("You have only just left another world", g.drawn.get(0).split("\\|")[0]);
    }

    @Test public void connectingMessageOnlyWhenNotReconnecting() throws Exception {
        s(3, false); g.login("a", "b", true); next();
        assertTrue(g.drawn.isEmpty());
        s(3, false); g.login("a", "b", false); next();
        assertEquals(1, g.drawn.size()); assertEquals("|Connecting to server...", g.drawn.get(0));
        assertEquals(Arrays.asList(43594, 43594), g.ports);
    }

    @Test public void socketOpenFailureReportsConnectError() {
        g.failOpen = true; g.loginMessage1 = "x"; g.loginMessage2 = "y";
        g.login("a", "b", true);
        assertEquals("", g.loginMessage1); assertEquals("Error connecting to server.", g.loginMessage2);
    }
}
