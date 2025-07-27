# EquipmentDefinition

Package `org.apollo.cache.def`.

Defined in [`2006Scape Server/src/main/java/org/apollo/cache/def/EquipmentDefinition.java`](2006Scape Server/src/main/java/org/apollo/cache/def/EquipmentDefinition.java).

Represents a type of game.items.Item that may be equipped.  @author Graham

```java
public final class EquipmentDefinition {
public static int count()
public static void init(EquipmentDefinition[] definitions)
public static EquipmentDefinition lookup(int id)
public EquipmentDefinition(int id)
public int getAttackLevel()
public int getDefenceLevel()
public int getHitpointsLevel()
public int getMagicLevel()
public int getPrayerLevel()
public int getRangedLevel()
public int getStrengthLevel()
public int getId()
public int getLevel(int skill)
public int getSlot()
public boolean isFullBody()
public boolean isFullHat()
public boolean isFullMask()
public boolean isTwoHanded()
public void setFlags(boolean twoHanded, boolean fullBody, boolean fullHat, boolean fullMask)
public void setLevels(int attack, int strength, int defence, int ranged, int prayer, int magic)
public void setLevels(int attack, int strength, int defence, int hitpoints, int ranged, int prayer, int magic)
public void setSlot(int slot)
```
