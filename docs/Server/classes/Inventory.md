# Inventory

Package `com.rs2.game.items`.

Defined in [`2006Scape Server/src/main/java/com/rs2/game/items/Inventory.java`](2006Scape Server/src/main/java/com/rs2/game/items/Inventory.java).

@author ArrowzFtw @note itemId+1 is the playerItems @note playerItems-1 = normalItemId

```java
public class Inventory {
public Inventory(Player player)
public void removeItem(Item i)
public void addItemToSlot(Item i, int slot)
public int get(int slot)
public void update()
```
