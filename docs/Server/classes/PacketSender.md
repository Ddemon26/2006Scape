# PacketSender

Package `com.rs2.net`.

Defined in [`2006Scape Server/src/main/java/com/rs2/net/PacketSender.java`](2006Scape Server/src/main/java/com/rs2/net/PacketSender.java).

Packet Sender helper class.

```java
public class PacketSender {
public PacketSender(Player player2)
public PacketSender sendUpdateItems(int frame, Item[] items)
public PacketSender sendUpdateItems(int frame, int[] itemIDs, int[] itemAmounts)
public PacketSender loginPlayer()
public PacketSender sendClan(String name, String message, String clan, int rights)
public PacketSender createPlayersObjectAnim(int X, int Y, int animationID, int tileObjectType, int orientation)
public PacketSender setInterfaceOffset(int x, int y, int id)
public PacketSender chatbox(int i1)
public PacketSender sendMessage(String s)
public PacketSender frame1()
public PacketSender setInterfaceWalkable(int ID)
public PacketSender sendFrame36(int id, int state)
public PacketSender sendFrame20(int id, int state)
public PacketSender sendString(String s, int id) { // Update string in interfaces etc
public PacketSender sendString(String s, int id, boolean forceSend) { // Update string in interfaces etc
public PacketSender sendFrame107()
public PacketSender sendPlayerDialogueHead(int Frame)
public PacketSender showInterface(int interfaceid)
public PacketSender sendFrame248(int MainFrame, int SubFrame) { //Trade-like interfaces
public PacketSender sendFrame246(int MainFrame, int SubFrame, int SubFrame2) { //A lot of generic interfaces; cooking, etc
public PacketSender sendHideInterfaceLayer(int MainFrame, boolean hidden) { //Special attack bar?
public PacketSender sendDialogueAnimation(int MainFrame, int SubFrame)
public PacketSender sendMapState(int state) { // used for disabling map
public PacketSender sendShowTab(int sideIcon)
public PacketSender sendFrame70(int i, int o, int id) { //Ranging guild minigame
public PacketSender sendNPCDialogueHead(int MainFrame, int SubFrame)
public PacketSender sendChatInterface(int Frame)
public PacketSender setPrivateMessaging(int i) { // friends and ignore list status
public PacketSender setChatOptions(int publicChat, int privateChat, int tradeBlock)
public PacketSender sendFrame87(int id, int state) { //Castlewars and duel arena texts
public PacketSender loadPM(long playerName, int world)
public PacketSender closeAllWindows()
public PacketSender sendFrame34(int id, int slot, int column, int amount)
public PacketSender sendItemOnInterface(int id, int amount, int child)
public PacketSender walkableInterface(int id)
public PacketSender openUpBank()
public PacketSender stillGfx(int id, int x, int y, int height, int time)
public PacketSender setSidebarInterface(int menuId, int form)
public PacketSender flashSideBarIcon(int i1)
public PacketSender createPlayerHints(int type, int id)
public PacketSender createObjectHints(int x, int y, int height, int pos)
public PacketSender object(int objectId, int objectX, int objectY, int face, int objectType)
public PacketSender object(int objectId, int objectX, int objectY, int objectH, int face, int objectType)
public PacketSender tempSong(int songID, int songID2)
public PacketSender frame174(int sound, int vol, int delay)
public PacketSender writeWeight(int weight)
public PacketSender sendConfig(int id, int state)
public PacketSender multiWay(int i1)
public PacketSender sendColor(int id, int color)
public PacketSender sendCrashFrame()
public PacketSender createStillGfx(int id, int x, int y, int height, int time)
public PacketSender object(int objectId, int objectX, int objectY, int objectType)
public PacketSender itemOnInterface(int interfaceChild, int zoom, int itemId)
public PacketSender setConfig(int id, int state)
public PacketSender sendLink(String s)
public PacketSender setSkillLevel(int skillNum, int currentLevel, int XP)
public PacketSender drawHeadicon(int i, int j, int k, int l)
public PacketSender createArrow(int x, int y, int height, int pos)
public PacketSender createArrow(int type, int id)
public PacketSender checkObjectSpawn(int objectId, int objectX, int objectY, int face, int objectType)
public PacketSender createObjectSpawn(int objectId, int objectX, int objectY, int height, int face, int objectType)
public PacketSender showOption(int i, int l, String s, int a)
public PacketSender sendSong(int id)
public PacketSender sendQuickSong(int id, int songDelay)
public PacketSender sendSound(int id, int type, int delay, int volume)
public PacketSender sendSound(int id, int volume, int delay)
public PacketSender sendClearScreen()
public PacketSender createGroundItem(int itemID, int itemX, int itemY, int itemAmount)
public PacketSender createGroundItem(int itemID, int itemX, int itemY, int itemAmount, int height)
public PacketSender removeGroundItem(int itemID, int itemX, int itemY, int Amount)
```
