# ToolLeprechaun

Package `com.rs2.game.content.skills.farming`.

Defined in [`2006Scape Server/src/main/java/com/rs2/game/content/skills/farming/ToolLeprechaun.java`](2006Scape Server/src/main/java/com/rs2/game/content/skills/farming/ToolLeprechaun.java).

Created by IntelliJ IDEA. User: vayken Date: 23/02/12 Time: 12:12 To change this template use File | Settings | File Templates.

```java
public class ToolLeprechaun {
public ToolLeprechaun(game.entities.Player player)
public game.items.Item[] storeItems = { new game.items.Item(5341), new game.items.Item(5343), new game.items.Item(952),
public game.items.Item[] storeItems2 = { new game.items.Item(1925), new game.items.Item(6032),
public game.items.Item[] storeItemsClient = { new game.items.Item(5341), new game.items.Item(5343),
public game.items.Item[] storeItems2Client = { new game.items.Item(1925), new game.items.Item(6032),
public static ToolStoreData forId(int toolId)
public static ToolStoreData forIndex(int index)
public int getToolIndex()
public int getToolId()
public int getToolConfig()
public int getToolMaxQuantity()
public int getToolFrameId()
public int getToolCountFrameId()
public String getToolName()
public void loadInterfaces()
public void handleAdditionalTools()
public void checkWateringCanQuantity()
public boolean hasWateringCanInStore()
public void updateStore()
public void storeItems(int itemId, int amount)
public void withdrawItems(int itemId, int amount)
public boolean noteItem(int itemId)
```
