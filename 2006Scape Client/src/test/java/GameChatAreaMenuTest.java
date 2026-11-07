import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.util.Arrays;
import static org.junit.Assert.*;

/** Observable chat hit testing with controlled array slots and display filters. */
public class GameChatAreaMenuTest {
    private Game g;
    private Player saved;

    @Before public void setup() throws Exception {
        saved = Game.myPlayer;
        Game.myPlayer = new Player();
        Game.myPlayer.name = "Self";
        g = new Game();
        // Oversize the fixture only, to distinguish the routine's 100-slot cap
        // from merely stopping at an array's length.
        for (String field : new String[] {"chatMessages", "chatNames", "chatTypes"}) {
            java.lang.reflect.Field f = Game.class.getField(field);
            f.setAccessible(true);
            f.set(g, field.equals("chatTypes") ? new int[101] : new String[101]);
        }
        g.publicChatMode = g.privateChatMode = g.tradeMode = g.splitpublicChat = 0;
        g.myPrivilege = 0;
    }
    @After public void restore() { Game.myPlayer = saved; }
    private void put(int slot, int type, String name) {
        g.chatMessages[slot] = "unrelated body " + slot;
        g.chatTypes[slot] = type;
        g.chatNames[slot] = name;
    }
    private int[] at(int y) {
        g.menuActionRow = 0;
        g.buildChatAreaMenu(y);
        return Arrays.copyOf(g.menuActionID, g.menuActionRow);
    }
    private void social(int y) { assertArrayEquals(new int[] {42, 639, 337}, at(y)); }

    @Test public void firstRowHasExclusiveTopInclusiveBottomAndScrollMovesIt() {
        put(0, 2, "Other");
        assertEquals(0, at(60).length); social(61); social(74);
        assertEquals(0, at(75).length);
        g.anInt1089 = 19;
        assertEquals(0, at(79).length); social(80); social(93);
        assertEquals(0, at(94).length);
    }
    @Test public void nullSlotsAndHiddenTypesDoNotConsumeRowsButSystemLinesDo() {
        put(2, 2, "Hidden"); g.publicChatMode = 2;
        put(7, 0, null); put(12, 1, "Visible");
        assertEquals(0, at(74).length); social(60);
        assertEquals("Add ignore @whi@Visible", g.menuActionName[0]);
        // Ordinary public type 2 now consumes a row as well.
        g.publicChatMode = 0;
        social(74); assertEquals("Add ignore @whi@Hidden", g.menuActionName[0]);
        assertEquals(0, at(60).length); social(46);
        assertEquals("Add ignore @whi@Visible", g.menuActionName[0]);
    }
    @Test public void scanIncludesSlot99Excludes100AndContinuesPastHoles() {
        put(99, 2, "Last"); put(100, 2, "Beyond");
        social(74); assertEquals("Add ignore @whi@Last", g.menuActionName[0]);
        assertEquals(0, at(60).length);
        g.chatMessages[99] = null;
        assertEquals(0, at(74).length);
    }
    @Test public void admittedLineCountControlsSpacingAndMinus20Cutoff() {
        for (int i = 0; i < 7; i++) put(i, 0, null);
        put(7, 2, "Below"); // row bottom -24, rejected before dispatch
        assertEquals(0, at(-24).length);
        g.chatMessages[6] = null; // six admitted prior rows: bottom -10
        social(-10); assertEquals(0, at(-24).length);
    }
    @Test public void typesSelectSocialTradeChallengeOrNonActionableRows() {
        for (int type : new int[] {1, 2, 3, 7}) {
            put(0, type, "Other"); social(74);
        }
        put(0, 4, "Trader"); assertArrayEquals(new int[] {484}, at(74));
        assertEquals("Accept trade @whi@Trader", g.menuActionName[0]);
        put(0, 8, "Dueler"); assertArrayEquals(new int[] {6}, at(74));
        assertEquals("Accept challenge @whi@Dueler", g.menuActionName[0]);
        for (int type : new int[] {0, 5, 6}) {
            put(0, type, "NoAction"); put(1, 1, "Next");
            assertEquals(0, at(74).length); social(60);
        }
    }
    @Test public void publicFiltersSelfRankPrefixesAndPrivilegesAffectActions() {
        put(0, 2, "@cr1@@cr2@Other"); social(74);
        assertEquals("Add ignore @whi@Other", g.menuActionName[0]);
        g.publicChatMode = 2; assertEquals(0, at(74).length);
        g.publicChatMode = 1; assertEquals(0, at(74).length);
        g.friendsCount = 1; g.friendsList[0] = "Other"; social(74);
        put(0, 2, "@cr2@Self"); assertEquals(0, at(74).length);
        put(0, 1, "Other"); g.publicChatMode = 2;
        for (int privilege : new int[] {0, 1, 2, 3, 4}) {
            g.myPrivilege = privilege;
            assertArrayEquals(privilege >= 1 && privilege <= 3
                    ? new int[] {606, 42, 639, 337} : new int[] {42, 639, 337}, at(74));
        }
    }
    @Test public void privateSplitAndExistingCrossModeGuardArePreserved() {
        put(0, 3, "Other"); social(74);
        g.splitpublicChat = 1; assertEquals(0, at(74).length);
        g.splitpublicChat = 0; g.privateChatMode = 2;
        assertEquals(0, at(74).length);
        g.publicChatMode = 1; g.friendsCount = 1;
        g.friendsList[0] = "Other"; social(74);
        g.publicChatMode = 0; assertEquals(0, at(74).length);
        put(0, 7, "Other"); social(74);
        g.splitpublicChat = 1; assertEquals(0, at(74).length);
        for (int type : new int[] {5, 6}) {
            put(0, type, "Other"); put(1, 1, "Next");
            social(74); // split rows consume no space
            g.splitpublicChat = 0; g.privateChatMode = 0; social(60);
            g.privateChatMode = 2; social(74);
            g.splitpublicChat = 1;
        }
    }
    @Test public void tradeAndChallengeFollowTradeVisibilityIncludingFriends() {
        for (int type : new int[] {4, 8}) {
            put(0, type, "Other");
            g.tradeMode = 2; assertEquals(0, at(74).length);
            g.tradeMode = 1; g.friendsCount = 0; assertEquals(0, at(74).length);
            g.friendsCount = 1; g.friendsList[0] = "Other";
            assertArrayEquals(new int[] {type == 4 ? 484 : 6}, at(74));
        }
    }
}
