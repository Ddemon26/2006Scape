import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Content-type dispatch of Game.promptUserForInput and Game.drawFriendsListOrWelcomeScreen on the widget argument
 * (friend/ignore list rows, list scroll heights, friend-list prompts, report-abuse and recovery-question text).
 */
public class FriendsPromptDispatchTest {
    private static RSInterface w(int type) { RSInterface r = new RSInterface(); r.contentType = type; return r; }
    private int savedNode, savedLoop;
    @Before public void save() { savedNode = Game.nodeID; savedLoop = Game.loopCycle; }
    @After public void restore() { Game.nodeID = savedNode; Game.loopCycle = savedLoop; }

    private static Game withFriends(int state, int count) {
        Game g = new Game();
        g.anInt900 = state; g.friendsCount = count;
        g.friendsList = new String[200];
        for (int i = 0; i < 200; i++) g.friendsList[i] = "f" + i;
        return g;
    }

    // ---- friend name rows: types 1-100 -> index type-1, 701-800 -> index type-601 ----
    @Test public void friendNameRowsShowTheNameAtTheTypeDerivedIndexOnlyBelowTheCount() {
        Game g = withFriends(2, 3);
        RSInterface a = w(1); g.drawFriendsListOrWelcomeScreen(a);
        assertEquals("f0", a.disabledText); assertEquals(1, a.atActionType);
        a = w(3); g.drawFriendsListOrWelcomeScreen(a);
        assertEquals("f2", a.disabledText); assertEquals(1, a.atActionType);
        a = w(4); a.atActionType = 1; g.drawFriendsListOrWelcomeScreen(a);                 // index 3 == count: empty
        assertEquals("", a.disabledText); assertEquals(0, a.atActionType);
        a = w(100); g.drawFriendsListOrWelcomeScreen(a);
        assertEquals("", a.disabledText);
        g.friendsCount = 101;
        a = w(100); g.drawFriendsListOrWelcomeScreen(a);                                   // index 99
        assertEquals("f99", a.disabledText);
        a = w(701); g.drawFriendsListOrWelcomeScreen(a);                                   // index 100 (701-601)
        assertEquals("f100", a.disabledText); assertEquals(1, a.atActionType);
        g.friendsCount = 100;
        a = w(701); g.drawFriendsListOrWelcomeScreen(a);
        assertEquals("", a.disabledText);
    }
    @Test public void friendNameRowsShowLoadingTextsUntilTheListIsReady() {
        Game g = withFriends(0, 3);
        RSInterface a = w(1); a.atActionType = 1; g.drawFriendsListOrWelcomeScreen(a);
        assertEquals("Loading friend list", a.disabledText); assertEquals(0, a.atActionType);
        a = w(2); g.drawFriendsListOrWelcomeScreen(a); assertEquals("Please wait...", a.disabledText);
        a = w(3); g.drawFriendsListOrWelcomeScreen(a); assertEquals("", a.disabledText);   // count treated as 0
        g.anInt900 = 1;
        a = w(1); g.drawFriendsListOrWelcomeScreen(a); assertEquals("Connecting to friendserver", a.disabledText);
        a = w(2); g.drawFriendsListOrWelcomeScreen(a); assertEquals("Please wait...", a.disabledText);
    }

    // ---- friend world rows: types 101-200 -> index type-101, 801-900 -> index type-701 ----
    @Test public void friendWorldRowsColourByWhetherTheFriendIsOnThisWorld() {
        Game g = withFriends(2, 4);
        Game.nodeID = 10;
        g.friendsNodeIDs[0] = 0; g.friendsNodeIDs[1] = 9; g.friendsNodeIDs[2] = 10; g.friendsNodeIDs[3] = 11;
        String[] expect = {"@red@Offline", "@red@Offline", "@gre@World-1", "@yel@World-2"};
        for (int i = 0; i < 4; i++) {
            RSInterface a = w(101 + i); g.drawFriendsListOrWelcomeScreen(a);
            assertEquals(expect[i], a.disabledText); assertEquals(1, a.atActionType);
        }
        RSInterface a = w(105); a.atActionType = 1; g.drawFriendsListOrWelcomeScreen(a);
        assertEquals("", a.disabledText); assertEquals(0, a.atActionType);
        g.friendsCount = 101; g.friendsNodeIDs[100] = 11;
        a = w(801); g.drawFriendsListOrWelcomeScreen(a);                                   // index 100
        assertEquals("@yel@World-2", a.disabledText);
        g.anInt900 = 1;
        a = w(101); g.drawFriendsListOrWelcomeScreen(a); assertEquals("", a.disabledText);
    }
    @Test public void friendListScrollHeightGrowsWithCountButNeverBelowViewHeightPlusOne() {
        Game g = withFriends(2, 10);
        RSInterface a = w(203); a.height = 100; g.drawFriendsListOrWelcomeScreen(a);
        assertEquals(170, a.scrollMax);
        a = w(203); a.height = 300; g.drawFriendsListOrWelcomeScreen(a);
        assertEquals(301, a.scrollMax);
        g.anInt900 = 1;
        a = w(203); a.height = 5; g.drawFriendsListOrWelcomeScreen(a);                     // not ready: count 0
        assertEquals(20, a.scrollMax);
    }

    // ---- ignore rows: types 401-500 -> index type-401 ----
    @Test public void ignoreRowsShowTheFormattedNameAtTheTypeDerivedIndex() {
        Game g = new Game();
        g.anInt900 = 2; g.ignoreCount = 2;
        g.ignoreListAsLongs[0] = TextClass.encodeBase37Name("bob"); g.ignoreListAsLongs[1] = TextClass.encodeBase37Name("al");
        RSInterface a = w(401); g.drawFriendsListOrWelcomeScreen(a);
        assertEquals("Bob", a.disabledText); assertEquals(1, a.atActionType);
        a = w(402); g.drawFriendsListOrWelcomeScreen(a);
        assertEquals("Al", a.disabledText);
        a = w(403); a.atActionType = 1; g.drawFriendsListOrWelcomeScreen(a);
        assertEquals("", a.disabledText); assertEquals(0, a.atActionType);
        g.anInt900 = 0;
        a = w(401); g.drawFriendsListOrWelcomeScreen(a); assertEquals("Loading ignore list", a.disabledText);
        a = w(402); g.drawFriendsListOrWelcomeScreen(a); assertEquals("Please wait...", a.disabledText);
        a = w(403); g.drawFriendsListOrWelcomeScreen(a); assertEquals("", a.disabledText);
        g.anInt900 = 1;                                                                    // only state 0 hides the ignore list
        a = w(401); g.drawFriendsListOrWelcomeScreen(a); assertEquals("Bob", a.disabledText);
    }
    @Test public void ignoreListScrollHeightUsesTheIgnoreCountRegardlessOfLoadState() {
        Game g = new Game(); g.anInt900 = 0; g.ignoreCount = 7;
        RSInterface a = w(503); a.height = 50; g.drawFriendsListOrWelcomeScreen(a);
        assertEquals(125, a.scrollMax);
        a = w(503); a.height = 200; g.drawFriendsListOrWelcomeScreen(a);
        assertEquals(201, a.scrollMax);
    }

    // ---- designer sprite swap, report abuse, moderator mute, recovery questions ----
    @Test public void genderSpriteTypesPickTheMaleOrFemaleSpriteOfTheWidget() {
        Game g = new Game();
        Sprite s1 = new Sprite(1, 1), s2 = new Sprite(2, 2);
        RSInterface a = w(324); a.sprite1 = s1; a.sprite2 = s2; g.designerMale = false;
        g.drawFriendsListOrWelcomeScreen(a); assertSame(s1, a.sprite1);
        g.designerMale = true; g.drawFriendsListOrWelcomeScreen(a); assertSame(s2, a.sprite1);
        RSInterface b = w(325); b.sprite1 = s1; b.sprite2 = s2; g.aClass30_Sub2_Sub1_Sub1_931 = null; g.designerMale = true;
        g.drawFriendsListOrWelcomeScreen(b); assertSame(s1, b.sprite1);
        g.designerMale = false; g.drawFriendsListOrWelcomeScreen(b); assertSame(s2, b.sprite1);
    }
    @Test public void type600BlinksACursorAfterTheReportAbuseInput() {
        Game g = new Game(); g.reportAbuseInput = "abc";
        Game.loopCycle = 5; RSInterface a = w(600); g.drawFriendsListOrWelcomeScreen(a); assertEquals("abc|", a.disabledText);
        Game.loopCycle = 15; a = w(600); g.drawFriendsListOrWelcomeScreen(a); assertEquals("abc ", a.disabledText);
    }
    @Test public void type613ShowsTheMuteOptionOnlyToModerators() {
        Game g = new Game();
        g.myPrivilege = 1; g.canMute = true;
        RSInterface a = w(613); g.drawFriendsListOrWelcomeScreen(a);
        assertEquals(0xff0000, a.textColor); assertTrue(a.disabledText.endsWith("<ON>"));
        g.canMute = false; a = w(613); g.drawFriendsListOrWelcomeScreen(a);
        assertEquals(0xffffff, a.textColor); assertTrue(a.disabledText.endsWith("<OFF>"));
        g.myPrivilege = 0; a = w(613); g.drawFriendsListOrWelcomeScreen(a); assertEquals("", a.disabledText);
        g.myPrivilege = 4; a = w(613); g.drawFriendsListOrWelcomeScreen(a); assertEquals("", a.disabledText);
    }
    @Test public void recoveryQuestionTextsDependOnTheDaysSinceTheRequest() {
        Game g = new Game(); g.anInt1170 = 1000;
        g.anInt1034 = 0; RSInterface a = w(661); g.drawFriendsListOrWelcomeScreen(a);
        assertTrue(a.disabledText.contains("not yet set any recovery"));
        g.anInt1034 = 990; a = w(661); g.drawFriendsListOrWelcomeScreen(a);
        assertTrue(a.disabledText.contains("Recovery Questions Last Set"));
        g.anInt1034 = 1014; a = w(661); g.drawFriendsListOrWelcomeScreen(a);               // 14 days ahead: requested today
        assertTrue(a.disabledText.startsWith("Earlier today"));
        g.anInt1034 = 1013; a = w(661); g.drawFriendsListOrWelcomeScreen(a);
        assertTrue(a.disabledText.startsWith("Yesterday"));
        g.anInt1034 = 1011; a = w(661); g.drawFriendsListOrWelcomeScreen(a);
        assertTrue(a.disabledText.startsWith("3 days ago"));
        g.anInt1034 = 1011; a = w(668); g.drawFriendsListOrWelcomeScreen(a);
        assertTrue(a.disabledText.contains("To cancel"));
        g.anInt1034 = 990; a = w(668); g.drawFriendsListOrWelcomeScreen(a);
        assertTrue(a.disabledText.contains("To change"));
        g.anInt1083 = 0; a = w(663); g.drawFriendsListOrWelcomeScreen(a);
        assertTrue(a.disabledText.contains("Never changed"));
    }

    // ---- promptUserForInput friend / ignore / logout / report-abuse content types ----
    @Test public void friendPromptTypesOpenTheNamePromptWithTheirAction() {
        Object[][] cases = {{201, 1, "Enter name of friend to add to list"}, {202, 2, "Enter name of friend to delete from list"}};
        for (Object[] c : cases) {
            Game g = new Game(); g.anInt900 = 2;
            assertFalse(g.promptUserForInput(w((Integer) c[0])));
            assertEquals(c[1], g.friendsListAction); assertEquals(c[2], g.aString1121);
            assertTrue(g.messagePromptRaised); assertEquals("", g.promptInput); assertEquals(0, g.inputDialogState);
            g = new Game(); g.anInt900 = 1;                                                // list not ready: ignored
            g.promptUserForInput(w((Integer) c[0])); assertEquals(0, g.friendsListAction); assertFalse(g.messagePromptRaised);
        }
    }
    @Test public void ignorePromptTypesOpenTheNamePromptRegardlessOfLoadState() {
        Game g = new Game(); g.anInt900 = 0;
        g.promptUserForInput(w(501)); assertEquals(4, g.friendsListAction); assertEquals("Enter name of player to add to list", g.aString1121);
        g = new Game(); g.promptUserForInput(w(502)); assertEquals(5, g.friendsListAction); assertEquals("Enter name of player to delete from list", g.aString1121);
    }
    @Test public void type205RequestsLogoutAndReturnsTrue() {
        Game g = new Game();
        assertTrue(g.promptUserForInput(w(205))); assertEquals(250, g.logoutRequestCycles);
        g = new Game(); assertFalse(g.promptUserForInput(w(204))); assertEquals(0, g.logoutRequestCycles);
    }
    @Test public void reportAbuseTypes601To612SendTheirOffsetAndType613TogglesMute() throws Exception {
        Game g = new Game();
        g.stream = new Stream(new byte[100]); g.stream.encryption = new ISAACRandomGen(new int[]{1, 2, 3, 4});
        g.reportAbuseInput = "bob"; g.canMute = true;
        g.promptUserForInput(w(604));
        assertEquals(12, g.stream.currentOffset);                                          // 130, then 218 + name + offset + mute
        assertEquals(3, g.stream.buffer[10]); assertEquals(1, g.stream.buffer[11]);
        g.stream.currentOffset = 0; g.reportAbuseInput = "";
        g.promptUserForInput(w(612)); assertEquals(1, g.stream.currentOffset);             // no name: only closeOpenInterfaces
        g.stream.currentOffset = 0; g.reportAbuseInput = "x"; g.promptUserForInput(w(600)); g.promptUserForInput(w(614));
        assertEquals(0, g.stream.currentOffset);                                           // 600 and 614 are outside 601-612
        assertTrue(g.canMute); g.promptUserForInput(w(613)); assertFalse(g.canMute); g.promptUserForInput(w(613)); assertTrue(g.canMute);
    }
}
