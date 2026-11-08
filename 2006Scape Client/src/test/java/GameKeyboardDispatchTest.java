import java.lang.reflect.Field;
import java.util.Arrays;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class GameKeyboardDispatchTest {
    private static final class Probe extends Game {
        long friendAdded, friendRemoved, ignoreAdded, ignoreRemoved;
        @Override public void addFriend(long value) { friendAdded = value; }
        @Override public void delFriend(long value) { friendRemoved = value; }
        @Override public void addIgnore(long value) { ignoreAdded = value; }
        @Override public void delIgnore(long value) { ignoreRemoved = value; }
        @Override public void drawTabArea() { }
    }

    private Probe game;
    private Player previousPlayer;
    private boolean previousAndroid;
    private final Object[] previousCensor = new Object[6];
    private static final String[] CENSOR_FIELDS = {"allowedFragmentCodes", "badWords", "badWordExemptionPairs", "domains", "topLevelDomains", "topLevelDomainStrictness"};

    @Before public void setUp() throws Exception {
        previousAndroid = ClientSettings.ANDROID;
        previousPlayer = Game.myPlayer;
        ClientSettings.ANDROID = false;
        game = new Probe();
        game.stream = new Stream(new byte[500]);
        game.stream.encryption = new ISAACRandomGen(new int[4]);
        Game.myPlayer = new Player();
        Game.myPlayer.name = "Smokeone";
        Object[] empty = {new int[0], new char[0][], new byte[0][][], new char[0][], new char[0][], new int[0]};
        for (int i = 0; i < CENSOR_FIELDS.length; i++) {
            Field field = Censor.class.getDeclaredField(CENSOR_FIELDS[i]);
            field.setAccessible(true);
            previousCensor[i] = field.get(null);
            field.set(null, empty[i]);
        }
    }

    @After public void tearDown() throws Exception {
        Game.myPlayer = previousPlayer;
        ClientSettings.ANDROID = previousAndroid;
        for (int i = 0; i < CENSOR_FIELDS.length; i++) {
            Field field = Censor.class.getDeclaredField(CENSOR_FIELDS[i]);
            field.setAccessible(true);
            field.set(null, previousCensor[i]);
        }
    }

    private static Field field(String name) throws Exception {
        Field result = RSApplet.class.getDeclaredField(name);
        result.setAccessible(true);
        return result;
    }

    private void keys(int... values) throws Exception {
        int[] queue = (int[]) field("charQueue").get(game);
        Arrays.fill(queue, 0);
        for (int i = 0; i < values.length; i++) queue[i] = values[i];
        field("readIndex").setInt(game, 0);
        field("writeIndex").setInt(game, values.length);
        game.processKeyboardInput();
        assertEquals(values.length, field("readIndex").getInt(game));
    }

    @Test public void queuedCharactersSelectTheActivePromptAndEnforceBoundaries() throws Exception {
        game.openInterfaceID = 44;
        game.reportAbuseInterfaceID = 44;
        game.reportAbuseInput = "123456789012";
        keys('X', '!', 8, 'z');
        assertEquals("12345678901z", game.reportAbuseInput);
        assertEquals("", game.inputString);
        game.openInterfaceID = -1;
        game.messagePromptRaised = true;
        game.promptInput = repeat('a', 79);
        keys('B', 'C', 8);
        assertEquals(repeat('a', 79), game.promptInput);
        assertTrue(game.messagePromptRaised);
        game.messagePromptRaised = false;
        keys('h', 'i', 8);
        assertEquals("h", game.inputString);
    }

    @Test public void namePromptEncodesTheEnteredIdentityForEachListAction() throws Exception {
        long expected = TextClass.encodeBase37Name("Abc 42");
        for (int action = 1; action <= 5; action++) {
            if (action == 3) continue;
            game.messagePromptRaised = true;
            game.promptInput = "";
            game.friendsListAction = action;
            game.friendsCount = 1;
            game.ignoreCount = 1;
            keys('A', 'b', 'c', ' ', '4', '2', 13);
            assertFalse(game.messagePromptRaised);
            if (action == 1) assertEquals(expected, game.friendAdded);
            if (action == 2) assertEquals(expected, game.friendRemoved);
            if (action == 4) assertEquals(expected, game.ignoreAdded);
            if (action == 5) assertEquals(expected, game.ignoreRemoved);
        }
        game.messagePromptRaised = true;
        game.promptInput = "No";
        game.friendsListAction = 2;
        game.friendsCount = 0;
        game.friendRemoved = 0;
        keys(13);
        assertEquals(0, game.friendRemoved);
    }

    @Test public void amountAndNameDialogsWriteDistinctPayloads() throws Exception {
        game.inputDialogState = 1;
        game.amountOrNameInput = "";
        keys('1', '2', 'k', '3', 13);
        assertEquals(0, game.inputDialogState);
        assertEquals("12000", game.amountOrNameInput);
        assertEquals(5, game.stream.currentOffset);
        assertArrayEquals(new byte[] {0, 0, 46, (byte) 224}, Arrays.copyOfRange(game.stream.buffer, 1, 5));
        game.stream.currentOffset = 0;
        game.inputDialogState = 2;
        game.amountOrNameInput = "";
        keys('B', 'o', 'b', 13);
        assertEquals(0, game.inputDialogState);
        assertEquals(9, game.stream.currentOffset);
        Stream decoded = new Stream(Arrays.copyOfRange(game.stream.buffer, 1, 9));
        assertEquals(TextClass.encodeBase37Name("Bob"), decoded.readLongBigEndian());
    }

    @Test public void publicMessageSeparatesColorAndMotionAndBackpatchesLength() throws Exception {
        game.inputString = "ReD:WaVe:Hi";
        keys(13);
        assertEquals("", game.inputString);
        assertEquals(1, Game.myPlayer.overheadChatColorCode);
        assertEquals(1, Game.myPlayer.overheadChatEffectCode);
        assertEquals("Hi", Game.myPlayer.textSpoken);
        assertEquals(game.stream.currentOffset - 2, game.stream.buffer[1] & 255);
        assertEquals(127, game.stream.buffer[2] & 255);
        assertEquals(127, game.stream.buffer[3] & 255);
        game.stream.currentOffset = 0;
        game.inputString = "plain";
        keys(10);
        assertEquals(0, Game.myPlayer.overheadChatColorCode);
        assertEquals(0, Game.myPlayer.overheadChatEffectCode);
    }

    @Test public void privateMessageBackpatchesEncodedTextAfterRecipient() throws Exception {
        game.messagePromptRaised = true;
        game.friendsListAction = 3;
        game.aLong953 = TextClass.encodeBase37Name("Smoketwo");
        game.promptInput = "Hi";
        keys(13);
        assertEquals("Hi", game.promptInput);
        assertEquals(8 + 1, game.stream.buffer[1] & 255);
        assertEquals(game.stream.currentOffset - 2, game.stream.buffer[1] & 255);
        game.stream.currentOffset = 0;
        game.messagePromptRaised = true;
        game.promptInput = "";
        keys(13);
        assertEquals(0, game.stream.currentOffset);
    }

    private static String repeat(char c, int length) {
        char[] chars = new char[length];
        Arrays.fill(chars, c);
        return new String(chars);
    }
}
