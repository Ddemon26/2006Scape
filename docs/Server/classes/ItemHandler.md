# ItemHandler

**Package:** `com.rs2.world`  
**Source:** [`2006Scape Server/src/main/java/com/rs2/world/ItemHandler.java`](2006Scape Server/src/main/java/com/rs2/world/ItemHandler.java)

## Overview

The `ItemHandler` class manages all ground items in the 2006Scape server. It handles the lifecycle of dropped items, including visibility rules, pickup mechanics, despawn timers, and synchronization between players. This system ensures that items dropped by players behave according to RuneScape's original mechanics where items are initially visible only to the dropper, then become visible to all players, and eventually despawn.

## Key Responsibilities

- **Ground Item Management**: Creating, tracking, and removing items on the ground
- **Visibility Control**: Managing who can see which items and when
- **Timer Management**: Handling hide/show timers and despawn mechanics
- **Player Synchronization**: Ensuring all players see the correct ground items
- **Special Item Handling**: Managing untradeable items and special cases
- **Performance Optimization**: Efficient processing of thousands of ground items

## Core Data Structures

### Item Storage
```java
public List<GroundItem> items = new ArrayList<GroundItem>();
```
The main list containing all active ground items in the game world.

### Constants
```java
public static final int HIDE_TICKS = 100; // ~60 seconds at 600ms per tick
```
Default time before items become visible to all players.

## Ground Item Lifecycle

### Phase 1: Private Visibility (0-100 ticks)
- Item is only visible to the player who dropped it
- `hideTicks > 0` indicates private phase
- Other players cannot see or pick up the item

### Phase 2: Public Visibility (100-200 ticks)
- Item becomes visible to all players
- `hideTicks = 0` and `removeTicks > 0`
- Anyone can pick up the item

### Phase 3: Despawn (200+ ticks)
- Item is removed from the game world
- `removeTicks = 0` triggers removal

## Core Methods

### Item Creation

#### `createGroundItem(Player c, int itemId, int itemX, int itemY, int itemAmount, int playerId)`
Creates a new ground item when a player drops something:

```java
public void createGroundItem(Player c, int itemId, int itemX, int itemY, 
                           int itemAmount, int playerId) {
    if (itemId > 0) {
        // Handle special cases
        if (itemId >= 2412 && itemId <= 2414) { // Fire capes
            c.getPacketSender().sendMessage("The cape vanishes as it touches the ground.");
            return;
        }
        
        // Handle broken barrows items
        if (itemId >= 4708 && itemId <= 4759) {
            itemId = convertToBrokenBarrows(itemId);
        }
        
        // Handle stackable vs non-stackable items
        if (!ItemDefinition.lookup(itemId).isStackable() && itemAmount > 0) {
            // Create individual items for non-stackable
            for (int j = 0; j < itemAmount; j++) {
                GroundItem item = new GroundItem(itemId, itemX, itemY, c.getH(), 
                                               1, c.playerId, HIDE_TICKS, 
                                               PlayerHandler.players[playerId].playerName);
                addItem(item);
            }
        } else {
            // Create single stack for stackable items
            GroundItem item = new GroundItem(itemId, itemX, itemY, c.getH(), 
                                           itemAmount, c.playerId, HIDE_TICKS, 
                                           PlayerHandler.players[playerId].playerName);
            addItem(item);
        }
        
        // Log the drop
        GameLogger.writeLog(c.playerName, "dropitem", 
                          c.playerName + " dropped " + itemAmount + " " + 
                          itemName + " at " + c.absX + "," + c.absY);
        
        reloadItems(c);
    }
}
```

**Special Handling:**
- **Fire Capes**: Vanish when dropped (untradeable)
- **Barrows Items**: Convert to broken versions when dropped
- **Stackable Items**: Create single ground item with full amount
- **Non-Stackable**: Create individual ground items for each

### Item Processing

#### `process()`
Main processing method called every game tick:

```java
public void process() {
    ArrayList<GroundItem> toRemove = new ArrayList<GroundItem>();
    
    for (GroundItem item : items) {
        if (item != null) {
            // Handle private visibility timer
            if (item.hideTicks > 0) {
                item.hideTicks--;
                
                // Transition to public visibility
                if (item.hideTicks == 1) {
                    item.hideTicks = 0;
                    createGlobalItem(item);
                    item.removeTicks = HIDE_TICKS;
                }
            }
            
            // Handle despawn timer
            if (item.removeTicks > 0) {
                item.removeTicks--;
                
                // Mark for removal
                if (item.removeTicks == 1) {
                    item.removeTicks = 0;
                    toRemove.add(item);
                }
            }
        }
    }
    
    // Remove expired items
    for (GroundItem item : toRemove) {
        removeGlobalItem(item, item.getItemId(), item.getItemX(), 
                        item.getItemY(), item.getItemAmount());
    }
}
```

### Visibility Management

#### `createGlobalItem(GroundItem item)`
Makes an item visible to all eligible players:

```java
public void createGlobalItem(GroundItem item) {
    for (Player p : PlayerHandler.players) {
        if (p != null) {
            Client player = (Client) p;
            
            // Skip the original dropper
            if (player.playerId != item.getItemController()) {
                
                // Check if item is tradeable for this player
                if (!player.getItemAssistant().tradeable(item.getItemId()) && 
                    player.playerId != item.getItemController()) {
                    continue;
                }
                
                // Check distance and height
                if (player.getH() == item.getItemH() && 
                    player.distanceToPoint(item.getItemX(), item.getItemY()) <= 60) {
                    
                    player.getPacketSender().createGroundItem(
                        item.getItemId(), item.getItemX(), item.getItemY(),
                        item.getItemAmount());
                }
            }
        }
    }
}
```

#### `reloadItems(Player c)`
Refreshes ground item visibility for a specific player:

```java
public void reloadItems(Player c) {
    // First, remove all items from client view
    for (GroundItem item : items) {
        if (c != null && item != null) {
            if (c.getH() == item.getItemH() && 
                c.distanceToPoint(item.getItemX(), item.getItemY()) <= 120) {
                
                c.getPacketSender().removeGroundItem(
                    item.getItemId(), item.getItemX(), item.getItemY(),
                    item.getItemAmount());
            }
        }
    }
    
    // Then, show items the player should see
    for (GroundItem item : items) {
        if (c != null && item != null) {
            boolean canSee = false;
            
            // Check if it's the player's item or tradeable
            if (c.getItemAssistant().tradeable(item.getItemId()) || 
                item.getName().equalsIgnoreCase(c.playerName)) {
                
                // Check distance and height
                if (c.getH() == item.getItemH() && 
                    c.distanceToPoint(item.getItemX(), item.getItemY()) <= 60) {
                    
                    // Show private items to owner only
                    if (item.hideTicks > 0 && 
                        item.getName().equalsIgnoreCase(c.playerName)) {
                        canSee = true;
                    }
                    
                    // Show public items to everyone
                    if (item.hideTicks == 0) {
                        canSee = true;
                    }
                    
                    if (canSee) {
                        c.getPacketSender().createGroundItem(
                            item.getItemId(), item.getItemX(), item.getItemY(),
                            item.getItemAmount());
                    }
                }
            }
        }
    }
}
```

### Item Removal

#### `removeGroundItem(Player c, int itemId, int itemX, int itemY, boolean add)`
Handles player pickup attempts:

```java
public void removeGroundItem(Player c, int itemId, int itemX, int itemY, boolean add) {
    for (GroundItem item : items) {
        if (item.getItemId() == itemId && 
            item.getItemX() == itemX && 
            item.getItemY() == itemY) {
            
            // Handle private items (owner only)
            if (item.hideTicks > 0 && 
                item.getName().equalsIgnoreCase(c.playerName)) {
                
                if (add) {
                    if (c.getItemAssistant().addItem(item.getItemId(), 
                                                   item.getItemAmount())) {
                        removeControllersItem(item, c, itemId, itemX, itemY, 
                                            item.getItemAmount());
                        break;
                    }
                } else {
                    removeControllersItem(item, c, itemId, itemX, itemY, 
                                        item.getItemAmount());
                    break;
                }
            }
            
            // Handle public items (anyone can pick up)
            else if (item.hideTicks <= 0) {
                if (add) {
                    if (c.getItemAssistant().addItem(item.getItemId(), 
                                                   item.getItemAmount())) {
                        removeGlobalItem(item, itemId, itemX, itemY, 
                                       item.getItemAmount());
                        break;
                    }
                } else {
                    removeGlobalItem(item, itemId, itemX, itemY, 
                                   item.getItemAmount());
                    break;
                }
            }
        }
    }
}
```

### Utility Methods

#### `itemExists(int itemId, int itemX, int itemY)`
Checks if a ground item exists at specific coordinates:

```java
public boolean itemExists(int itemId, int itemX, int itemY) {
    for (GroundItem item : items) {
        if (item.getItemId() == itemId && 
            item.getItemX() == itemX && 
            item.getItemY() == itemY) {
            return true;
        }
    }
    
    // Also check global drops
    if (GlobalDropsHandler.itemExists(itemId, itemX, itemY, true)) {
        return true;
    }
    
    return false;
}
```

#### `itemAmount(String name, int itemId, int itemX, int itemY)`
Gets the amount of a specific item at coordinates:

```java
public int itemAmount(String name, int itemId, int itemX, int itemY) {
    for (GroundItem item : items) {
        if (item.getName().equalsIgnoreCase(name) && 
            item.getItemId() == itemId && 
            item.getItemX() == itemX && 
            item.getItemY() == itemY) {
            return item.getItemAmount();
        }
    }
    return 0;
}
```

#### `moveItem(GroundItem item, int itemX, int itemY)`
Moves a ground item to new coordinates:

```java
public void moveItem(GroundItem item, int itemX, int itemY) {
    if (items.remove(item)) {
        int oldX = item.itemX;
        int oldY = item.itemY;
        
        item.itemX = itemX;
        item.itemY = itemY;
        items.add(item);
        
        // Update all players
        for (Player p : PlayerHandler.players) {
            if (p != null) {
                p.getPacketSender().removeGroundItem(item.itemId, oldX, oldY, 
                                                   item.itemAmount);
                reloadItems(p);
            }
        }
    }
}
```

## Special Item Handling

### Barrows Equipment
```java
public int[][] brokenBarrows = {
    { 4708, 4860 }, // Ahrim's hood -> broken
    { 4710, 4866 }, // Ahrim's robe top -> broken
    // ... more mappings
};
```

When barrows items are dropped, they automatically convert to their broken versions to prevent item duplication exploits.

### Untradeable Items
- Fire capes vanish when dropped
- Untradeable items are only visible to the original owner
- Some items have special pickup restrictions

## Performance Considerations

### Optimization Strategies
- **Distance Checking**: Only process items within reasonable range
- **Batch Processing**: Group similar operations together
- **Efficient Removal**: Use ArrayList for O(1) removal by index
- **Memory Management**: Clean up expired items promptly

### Scalability
- **Item Limits**: Monitor total ground item count
- **Processing Time**: Optimize tick processing for large item counts
- **Memory Usage**: Regular cleanup prevents memory leaks

## Usage Examples

### Creating Ground Items
```java
// Player drops an item
ItemHandler itemHandler = GameEngine.itemHandler;
itemHandler.createGroundItem(player, 995, player.absX, player.absY, 1000, player.playerId);

// NPC drops loot
itemHandler.createGroundItem(killer, itemId, npc.absX, npc.absY, amount, killer.playerId);
```

### Checking for Items
```java
// Check if item exists at location
if (itemHandler.itemExists(995, x, y)) {
    int amount = itemHandler.itemAmount(player.playerName, 995, x, y);
    System.out.println("Found " + amount + " coins at " + x + "," + y);
}
```

### Manual Item Management
```java
// Move an item (for special mechanics)
GroundItem item = findItemAt(x, y);
if (item != null) {
    itemHandler.moveItem(item, newX, newY);
}

// Force remove an item
itemHandler.removeGroundItem(player, itemId, x, y, false);
```

## Integration Points

### GameEngine Integration
```java
// Called every game tick
GameEngine.itemHandler.process();
```

### Player Integration
```java
// When player enters new area
itemHandler.reloadItems(player);

// When player picks up item
itemHandler.removeGroundItem(player, itemId, x, y, true);
```

### Combat Integration
```java
// When NPC dies, drop loot
for (ItemDrop drop : npcDrops) {
    itemHandler.createGroundItem(killer, drop.itemId, npc.absX, npc.absY, 
                               drop.amount, killer.playerId);
}
```

## Best Practices

1. **Always check item existence** before operations
2. **Use appropriate visibility rules** for different item types
3. **Handle special cases** for untradeable items
4. **Optimize distance calculations** for performance
5. **Log important item operations** for debugging
6. **Clean up expired items** regularly

## Related Classes

- [`GroundItem`](GroundItem.md) - Individual ground item data structure
- [`GlobalDropsHandler`](GlobalDropsHandler.md) - Handles permanent world items
- [`ItemAssistant`](ItemAssistant.md) - Player inventory management
- [`GameEngine`](GameEngine.md) - Calls ItemHandler.process() every tick
- [`PlayerHandler`](PlayerHandler.md) - Player management for item visibility
