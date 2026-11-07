import java.lang.reflect.Field;
import org.junit.Test;
import static org.junit.Assert.*;

public class PlayerChatRightsTest {
    private static Game game() throws Exception {
        Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        Game game = (Game) ((sun.misc.Unsafe) field.get(null)).allocateInstance(Game.class);
        set(game, "chatTypes", new int[100]); set(game, "chatNames", new String[100]); set(game, "chatMessages", new String[100]);
        game.aStream_834 = new Stream(new byte[5000]);
        emptyCensor("allowedFragmentCodes", new int[0]); emptyCensor("badWords", new char[0][]); emptyCensor("badWordExemptionPairs", new byte[0][][]);
        emptyCensor("domains", new char[0][]); emptyCensor("topLevelDomains", new char[0][]); emptyCensor("topLevelDomainStrictness", new int[0]);
        return game;
    }
    private static void emptyCensor(String name, Object value) throws Exception {
        Field field = Censor.class.getDeclaredField(name); field.setAccessible(true); field.set(null, value);
    }
    private static void set(Game game, String name, Object value) throws Exception {
        Field field = Game.class.getDeclaredField(name); field.setAccessible(true); field.set(game, value);
    }
    private static Stream chat(int rights) {
        return new Stream(new byte[] {0x12, 0x34, (byte) rights, (byte) 253, 0, 0, 0});
    }
    private static Player speaker() { Player p = new Player(); p.name = "Speaker"; p.visible = true; return p; }

    @Test public void privelageHoldsTheRightsByteOfTheChatMask() throws Exception {
        for (int rights = 0; rights <= 3; rights++) {
            Game game = game(); game.ignoreCount = 0; Player p = speaker();
            Stream s = chat(rights);
            game.decodePlayerUpdateMask(0x80, 0, s, p);
            assertEquals(rights, p.rights);
            assertEquals(7, s.currentOffset);
        }
    }

    @Test public void rightsOfTwoOrMoreBypassIgnoreListWhileZeroAndOneAreIgnored() throws Exception {
        for (int rights = 0; rights <= 3; rights++) {
            Game game = game(); Player p = speaker();
            set(game, "ignoreListAsLongs", new long[] {TextClass.encodeBase37Name("Speaker")});
            game.ignoreCount = 1;
            game.decodePlayerUpdateMask(0x80, 0, chat(rights), p);
            assertEquals(rights <= 1 ? 0 : rights, p.rights);
        }
    }

    @Test public void rightsSelectCrownPrefixOfTheChatLineName() throws Exception {
        String[] expected = {"Speaker", "@cr1@Speaker", "@cr2@Speaker", "@cr2@Speaker"};
        for (int rights = 0; rights <= 3; rights++) {
            Game game = game(); game.ignoreCount = 0; Player p = speaker();
            game.decodePlayerUpdateMask(0x80, 0, chat(rights), p);
            assertEquals(expected[rights], game.chatNames[0]);
        }
    }
}
