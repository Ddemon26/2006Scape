# Greegree

Package `com.rs2.game.items.impl`.

Defined in [`2006Scape Server/src/main/java/com/rs2/game/items/impl/Greegree.java`](2006Scape Server/src/main/java/com/rs2/game/items/impl/Greegree.java).

Greegree helper class.

```java
public class Greegree {
public int getGreegreeID()
public int getNpcID()
public int getStandAnim()
public int getWalkAnim()
public int getRunAnim()
public int getBlockAnim()
public int getAttackAnim()
public static MonkeyData forId(int id)
public static boolean isWearingGreegree(game.entities.Player p)
public static boolean isAnim(int animId)
public static boolean canWear(game.entities.Player player)
public static boolean attemptGreegree(game.entities.Player p, int weaponID)
public static void setAnimations(game.entities.Player p, MonkeyData data)
public static void resetAnimations(game.entities.Player p)
public static boolean attemptRemove(game.entities.Player p, int slot)
```
