# ItemHandler

Package `com.rs2.world`.

Defined in [`2006Scape Server/src/main/java/com/rs2/world/ItemHandler.java`](2006Scape Server/src/main/java/com/rs2/world/ItemHandler.java).

Handles ground items

```java
public class ItemHandler {
public              List<GroundItem> items      = new ArrayList<GroundItem>();
public ItemHandler()
public void addItem(GroundItem item)
public void removeItem(GroundItem item)
public int itemAmount(String name, int itemId, int itemX, int itemY)
public boolean itemExists(int itemId, int itemX, int itemY)
public void moveItem(GroundItem item, int itemX, int itemY)
public void reloadItems(Player c)
public void process()
public void createGroundItem(Player c, int itemId, int itemX, int itemY, int itemAmount, int playerId)
public void createGlobalItem(GroundItem i)
public void removeGroundItem(Player c, int itemId, int itemX, int itemY, boolean add)
```
