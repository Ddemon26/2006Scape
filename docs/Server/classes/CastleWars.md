# CastleWars

Package `com.rs2.game.content.minigames.castlewars`.

Defined in [`2006Scape Server/src/main/java/com/rs2/game/content/minigames/castlewars/CastleWars.java`](2006Scape Server/src/main/java/com/rs2/game/content/minigames/castlewars/CastleWars.java).

@Author Sanity for base @Author Satan666 @Author Andrew (Mr Extremez) fixing it up

```java
public class CastleWars {
public static boolean deleteCastleWarsItems(game.entities.Player c, int itemId)
public static void resetPlayer(game.entities.Player player)
public static void collapseCave(int cave)
public static void addToWaitRoom(game.entities.Player p, int team)
public static void toWaitingRoom(game.entities.Player p, int team)
public static void returnFlag(game.entities.Player player, int wearItem)
public static void captureFlag(game.entities.Player player)
public static void addFlag(game.entities.Player player, int flagId)
public static void dropFlag(game.entities.Player player, int flagId)
public static void pickupFlag(game.entities.Player player)
public static void createHintIcon(game.entities.Player player, int t)
public static void createFlagHintIcon(int x, int y)
public static int getTeamNumber(game.entities.Player player)
public static void leaveWaitingRoom(game.entities.Player player)
public static void process()
public static void updatePlayers()
public static void updateInGamePlayers()
public static void startGame()
public static void endGame()
public static void resetGame()
public static void removePlayerFromCw(game.entities.Player player)
public static void addCapes(game.entities.Player p, int capeId)
public static void deleteGameItems(game.entities.Player player)
public static int getZammyPlayers()
public static int getSaraPlayers()
public static boolean isInCw(game.entities.Player player)
public static boolean isInCwWait(game.entities.Player player)
public static void setSaraFlag(int status)
public static void setZammyFlag(int status)
public static void changeFlagObject(int objectId, int team)
```
