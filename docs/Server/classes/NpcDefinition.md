# NpcDefinition

Package `org.apollo.cache.def`.

Defined in [`2006Scape Server/src/main/java/org/apollo/cache/def/NpcDefinition.java`](2006Scape Server/src/main/java/org/apollo/cache/def/NpcDefinition.java).

Represents a type of Npc.  @author Chris Fletcher

```java
public final class NpcDefinition {
public static int count()
public static NpcDefinition[] getDefinitions()
public static void init(NpcDefinition[] definitions)
public static NpcDefinition lookup(int id)
public NpcDefinition(int id)
public int getCombatLevel()
public String getDescription()
public int getId()
public String getInteraction(int slot)
public String[] getInteractions()
public String getName()
public int getSize()
public int getStandAnimation()
public int getWalkAnimation()
public int getWalkBackAnimation()
public int getWalkLeftAnimation()
public int getWalkRightAnimation()
public boolean hasCombatLevel()
public boolean hasInteraction(int slot)
public boolean hasStandAnimation()
public boolean hasWalkAnimation()
public boolean hasWalkBackAnimation()
public boolean hasWalkLeftAnimation()
public boolean hasWalkRightAnimation()
public void setCombatLevel(int combatLevel)
public void setDescription(String description)
public void setInteraction(int slot, String interaction)
public void setName(String name)
public void setSize(int size)
public void setStandAnimation(int standAnim)
public void setWalkAnimation(int walkAnim)
public void setWalkAnimations(int walkAnim, int walkBackAnim, int walkLeftAnim, int walkRightAnim)
```
