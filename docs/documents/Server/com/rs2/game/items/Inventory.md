# Inventory

Package `com.rs2.game.items`.

Defined in [`2006Scape Server/src/main/java/com/rs2/game/items/Inventory.java`](2006Scape Server/src/main/java/com/rs2/game/items/Inventory.java).

@author ArrowzFtw @note itemId+1 is the playerItems @note playerItems-1 = normalItemId

```java
public class Inventory {
public Inventory(game.entities.Player player)
public void removeItem(game.items.Item i)
public void addItemToSlot(game.items.Item i, int slot)
public int get(int slot)
public void update()
public boolean contains(int id)
public boolean contains(game.items.Item item)
public boolean contains(int id, int amount)
public Inventory getItemContainer()
public void addItem(game.items.Item item)
public int getItemAmount(int id)
public void replace(int item, int newItem)
public int getCount(int i)
public void set(int slot, game.items.Item item)
public int freeSlots()
public void add(int id)
public boolean add(int id, int amount)
public boolean canAddItem(game.items.Item item)
public void addItem(game.items.Item item, boolean drop)
public boolean playerHasItem(int item)
public void removeItemSlot(game.items.Item item, int slot)
```
