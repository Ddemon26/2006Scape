# ItemDefinition

Package `org.apollo.cache.def`.

Defined in [`2006Scape Server/src/main/java/org/apollo/cache/def/ItemDefinition.java`](2006Scape Server/src/main/java/org/apollo/cache/def/ItemDefinition.java).

Represents a type of game.items.Item.  @author Graham

```java
public final class ItemDefinition {
public static int count()
public static ItemDefinition[] getDefinitions()
public static void init(ItemDefinition[] definitions)
public static int itemToNote(int id)
public static ItemDefinition lookup(int id)
public static int noteToItem(int id)
public ItemDefinition(int id)
public String getDescription()
public String getGroundAction(int id)
public int getId()
public String getInventoryAction(int id)
public String getName()
public int getNoteGraphicId()
public int getNoteInfoId()
public int getTeam()
public int getValue()
public boolean isMembersOnly()
public boolean isNote()
public boolean isStackable()
public void setDescription(String description)
public void setGroundAction(int id, String action)
public void setInventoryAction(int id, String action)
public void setMembersOnly(boolean members)
public void setName(String name)
public void setNoteGraphicId(int noteGraphicId)
public void setNoteInfoId(int noteInfoId)
public void setStackable(boolean stackable)
public void setTeam(int team)
public void setValue(int value)
public void toNote()
```
