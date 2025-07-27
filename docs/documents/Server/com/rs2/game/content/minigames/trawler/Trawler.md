# Trawler

Package `com.rs2.game.content.minigames.trawler`.

Defined in [`2006Scape Server/src/main/java/com/rs2/game/content/minigames/trawler/Trawler.java`](2006Scape Server/src/main/java/com/rs2/game/content/minigames/trawler/Trawler.java).

Trawler helper class.

```java
public class Trawler extends GroupMinigame {
public WaitingRoom waiting_room = new TrawlerWaitingRoom(this);
public ArrayList<game.entities.Player> players = new ArrayList<game.entities.Player>();
public ArrayList<game.entities.Player> players_to_remove = new ArrayList<game.entities.Player>();
public static int getIndex(int x, int y)
public static Wall getWallByIndex(int index, boolean sinking)
public int getAvaliableWallSize()
public int[] getAvaliableWalls()
public void breakRandomWall()
public void resetWalls()
public void updateWall(int index)
public void playerUpdates()
public void onStart()
public void execute(CycleEventContainer container)
public void stop()
public void onEndLose()
public void start()
public void execute(CycleEventContainer container)
public void stop()
public void tick()
public void ripNet()
public void increaseWaterLevel()
public void fixHole(game.entities.Player player, int x, int y)
public void upLadder(game.entities.Player player, int obX, int obY)
public void downLadder(game.entities.Player player, int obX, int obY)
public void fixNet(game.entities.Player p)
public boolean doAction(game.entities.Player p)
public void bail(game.entities.Player p)
public void emptyBucket(game.entities.Player p)
public void increaseFish()
public int end()
public void setSwimmingAnimations()
public void switchBoats()
public void startGameTimer()
public void execute(CycleEventContainer container)
public void stop()
public int chanceByLevel(game.entities.Player p, int fish)
public void movePlayersLoss()
public void movePlayerWin(final ArrayList<game.entities.Player> pl)
public void execute(CycleEventContainer container)
public void stop()
public ArrayList<GameItem> playerReward(game.entities.Player p)
public boolean skillCheck(int level, int levelRequired, int itemBonus)
public WaitingRoom getWaitingRoom()
public String getWaitingRoomMessage()
public boolean inProgress()
public int getGameTime()
public void resetRewardsInterface(game.entities.Player player)
public void showReward(game.entities.Player player)
public void updateRewardSlot(game.entities.Player player, int slot)
public int getRewardSlot(int j)
```
