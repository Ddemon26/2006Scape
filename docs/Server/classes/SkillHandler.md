# SkillHandler

Package `com.rs2.game.content.skills`.

Defined in [`2006Scape Server/src/main/java/com/rs2/game/content/skills/SkillHandler.java`](2006Scape Server/src/main/java/com/rs2/game/content/skills/SkillHandler.java).

Skillhandler @author Andrew (I'm A Boss on Rune-Server, Mr Extremez on Moparscape & Runelocus)

```java
public class SkillHandler {
public static boolean isSkilling(game.entities.Player player)
public static void resetItemOnNpc(game.entities.Player player)
public static void resetSkills(game.entities.Player player) {// call when walking, dropping,
public static boolean canDoAction(int timer)
public static boolean noInventorySpace(game.entities.Player c, String skill)
public static void deleteTime(game.entities.Player c)
public static void stopEvents(game.entities.Player player, int eventId)
public static void send1Item(game.entities.Player c, int itemId)
public static void resetPlayerSkillVariables(game.entities.Player c)
public static boolean skillCheck(int level, int levelRequired, int itemBonus)
public static String getLine(game.entities.Player c)
```
