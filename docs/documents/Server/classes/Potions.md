# Potions

Package `com.rs2.game.content.consumables`.

Defined in [`2006Scape Server/src/main/java/com/rs2/game/content/consumables/Potions.java`](2006Scape Server/src/main/java/com/rs2/game/content/consumables/Potions.java).

Potions helper class.

```java
public class Potions {
public Potions(Player player)
public void handlePotion(int itemId, int slot)
public void execute(CycleEventContainer container)
public void stop()
public void curePoison(long delay)
public void restoreStats()
public void doTheBrewzam(int itemId, int replaceItem, int slot)
public void doTheBrew(int itemId, int replaceItem, int slot)
public void enchanceStat(int skillID, boolean sup)
public void antifirePot(int itemId, int replaceItem, int slot)
public int getBrewStat(int skill, double amount)
public int getBoostedStat(int skill, boolean sup)
public boolean isPotion(int itemId)
public boolean potionNames(int itemId)
```
