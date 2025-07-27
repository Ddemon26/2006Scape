# Mining

Package `com.rs2.game.content.skills.core`.

Defined in [`2006Scape Server/src/main/java/com/rs2/game/content/skills/core/Mining.java`](2006Scape Server/src/main/java/com/rs2/game/content/skills/core/Mining.java).

Mining helper class.

```java
public class Mining {
public boolean giveGem(game.entities.Player player)
public void obtainGem(game.entities.Player player)
public static int getRandom()
public int getObject(final int object)
public static rockData getRock(final int object)
public int getRequiredLevel()
public int getXp()
public int getTimer()
public int getRespawnTimer()
public int[] getOreIds()
public int getOre(int playerLevel)
public void repeatAnimation(final game.entities.Player c)
public void execute(CycleEventContainer container)
public void stop()
public void startMining(final game.entities.Player player, final int objectID, final int objectX, final int objectY, final int type)
public void execute(CycleEventContainer container)
public void stop()
public static void resetMining(game.entities.Player player)
public int getTimer(rockData rock, int pick, int level)
public void mineRock(int respawnTime, int x, int y, int type, int i)
public static void prospectRock(final game.entities.Player player, final String itemName)
public void execute(CycleEventContainer container)
public void stop()
public void execute(CycleEventContainer container)
public void stop()
public static void prospectNothing(final game.entities.Player c)
public void execute(CycleEventContainer container)
public void stop()
public static boolean rockExists(int rockID)
```
