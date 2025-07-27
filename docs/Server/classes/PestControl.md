# PestControl

Package `com.rs2.game.content.minigames`.

Defined in [`2006Scape Server/src/main/java/com/rs2/game/content/minigames/PestControl.java`](2006Scape Server/src/main/java/com/rs2/game/content/minigames/PestControl.java).

@author Harlan Credits to Sanity

```java
public class PestControl {
public static HashMap<game.entities.Player, Integer> waitingBoat = new HashMap<game.entities.Player, Integer>();
public int shifter = 3732 + Misc.random(9);
public int brawler = 3772 + Misc.random(4);
public int defiler = 3762 + Misc.random(9);
public int ravager = 3742 + Misc.random(4);
public int torcher = 3752 + Misc.random(7);
public int splater = 3727 + Misc.random(4);
public void process()
public static void removePlayerGame(game.entities.Player player)
public static void setGameInterface()
public boolean allPortalsDead3()
public static void leaveWaitingBoat(game.entities.Player player)
public static void addToWaitRoom(game.entities.Player player)
public static boolean isInGame(game.entities.Player player)
public static boolean isInPcBoat(game.entities.Player player)
public static boolean npcIsPCMonster(int npcType)
public static boolean isPCPortal(int npcType)
```
