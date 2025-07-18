# Cooking

Package `com.rs2.game.content.skills.cooking`.

Defined in [`2006Scape Server/src/main/java/com/rs2/game/content/skills/cooking/Cooking.java`](2006Scape Server/src/main/java/com/rs2/game/content/skills/cooking/Cooking.java).

Cooking helper class.

```java
public class Cooking extends SkillHandler {
public static CookingItems forId(int itemId)
public static void makeBreadOptions(Player c, int item)
public static void pastryCreation(Player c, int itemID1, int itemID2, int giveItem, String message)
public static void cookingAddon(Player c, int itemID1, int itemID2, int giveItem, int requiredLevel, int expGained)
public static void setCooking(Player player, boolean isCooking)
public static boolean startCooking(Player c, int itemId, int objectId)
public static void cookItem(final Player player, final int itemId, final int amount, final int objectId)
public void execute(CycleEventContainer container)
public void stop()
```
