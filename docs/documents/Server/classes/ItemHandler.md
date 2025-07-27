# ItemHandler

**Package:** `com.rs2.world`  
**Source:** [`2006Scape Server/src/main/java/com/rs2/world/ItemHandler.java`](2006Scape Server/src/main/java/com/rs2/world/ItemHandler.java)

## Overview

The `ItemHandler` class manages all ground items in the 2006Scape server world. It handles the creation, visibility, pickup, and cleanup of items dropped on the ground by players, NPCs, or spawned by the server. This class implements a sophisticated visibility system where items are initially visible only to their owner before becoming visible to all players, and eventually disappearing if not picked up.

## Key Responsibilities

- **render.objects.Ground game.items.Item Management**: Creating, tracking, and removing items on the ground
- **Visibility Control**: Managing item visibility between private and public states
- **game.items.Item Lifecycle**: Handling item aging, expiration, and cleanup
- **game.entities.Player Interaction**: Processing item pickup and drop requests
- **World Synchronization**: Updating item visibility for all nearby players
- **Special game.items.Item Handling**: Managing unique behaviors for specific items
- **Performance Optimization**: Efficient processing of large numbers of ground items

## Core Architecture

### game.items.Item Storage
```java
public List<GroundItem> items = new ArrayList<GroundItem>();
public static final int HIDE_TICKS = 100;
```

The ItemHandler maintains a list of all ground items and uses a tick-based system for managing item visibility and expiration.

### game.items.Item Lifecycle States
1. **Private Phase**: game.items.Item visible only to owner (HIDE_TICKS duration)
2. **Public Phase**: game.items.Item visible to all players (HIDE_TICKS duration)
3. **Removal**: game.items.Item deleted from world

## Core Methods

### game.items.Item Creation

#### `createGroundItem(game.entities.Player c, int itemId, int itemX, int itemY, int itemAmount, int playerId)`
Creates a new ground item at the specified location:

```java
public void createGroundItem(game.entities.Player c, int itemId, int itemX, int itemY, int itemAmount, int playerId) {
    if (itemId <= 0) return;
    
    // Handle special item cases
    if (itemId >= 2412 && itemId <= 2414) {
        c.getPacketSender().sendMessage("The cape vanishes as it touches the ground.");
        return;
    }
    
    // Handle broken barrows items
    if (itemId >= 4708 && itemId <= 4759) {
        for (int[] brokenBarrow : brokenBarrows) {
            if (brokenBarrow[0] == itemId) {
                itemId = brokenBarrow[1];
                break;
            }
        }
    }
    
    // Handle non-stackable items
    if (!ItemDefinition.lookup(itemId).isStackable() && itemAmount > 0) {
        for (int j = 0; j < itemAmount; j++) {
            GroundItem item = new GroundItem(itemId, itemX, itemY, c.getH(), 1, 
                                           c.playerId, HIDE_TICKS, 
                                           PlayerHandler.players[playerId].playerName);
            addItem(item);
            
            // Log item drop
            if (!c.isDead && itemId != 526) {
                if (c.getPlayerAssistant().isPlayer()) {
                    GameLogger.writeLog(c.playerName, "dropitem", 
                        c.playerName + " dropped " + itemAmount + " " + 
                        DeprecatedItems.getItemName(itemId).toLowerCase() + 
                        " absX: " + c.absX + " absY: " + c.absY);
                }
            }
        }
    } else {
        // Handle stackable items
        GroundItem item = new GroundItem(itemId, itemX, itemY, c.getH(), itemAmount, 
                                       c.playerId, HIDE_TICKS, 
                                       PlayerHandler.players[playerId].playerName);
        addItem(item);
        
        // Log item drop
        if (!c.isDead && itemId != 526) {
            if (c.getPlayerAssistant().isPlayer()) {
                GameLogger.writeLog(c.playerName, "dropitem", 
                    c.playerName + " dropped " + itemAmount + " " + 
                    DeprecatedItems.getItemName(itemId).toLowerCase() + 
                    " absX: " + c.absX + " absY: " + c.absY);
            }
        }
    }
    
    // Update client view
    reloadItems(c);
}
```

**Parameters:**
- `c`: game.entities.Player creating the item
- `itemId`: ID of the item to create
- `itemX, itemY`: World coordinates
- `itemAmount`: Quantity of items
- `playerId`: ID of the player who owns the item

### game.items.Item Visibility Management

#### `createGlobalItem(GroundItem item)`
Makes an item visible to all players:

```java
public void createGlobalItem(GroundItem item) {
    if (!itemExists(item.getItemId(), item.getItemX(), item.getItemY())) {
        addItem(item);
    }
    
    // Show item to all nearby players
    for (game.entities.Player p : PlayerHandler.players) {
        if (p != null) {
            Client person = (Client) p;
            if (person != null) {
                // Skip item owner
                if (person.playerId != item.getItemController()) {
                    // Check if item is tradeable for non-owners
                    if (!person.getItemAssistant().tradeable(item.getItemId()) && 
                        person.playerId != item.getItemController()) {
                        continue;
                    }
                    
                    // Check distance and height
                    if (person.getH() == item.getItemH() && 
                        person.distanceToPoint(item.getItemX(), item.getItemY()) <= 60) {
                        person.getPacketSender().createGroundItem(
                            item.getItemId(), item.getItemX(), item.getItemY(),
                            item.getItemAmount());
                    }
                }
            }
        }
    }
}
```

#### `reloadItems(game.entities.Player c)`
Updates item visibility for a specific player:

```java
public void reloadItems(game.entities.Player c) {
    // First pass: Remove all items from client view
    for (GroundItem i : items) {
        if (c != null && i != null) {
            if (c.getH() == i.getItemH() && 
                c.distanceToPoint(i.getItemX(), i.getItemY()) <= 120) {
                c.getPacketSender().removeGroundItem(
                    i.getItemId(), i.getItemX(), i.getItemY(), i.getItemAmount());
            }
        }
    }
    
    // Second pass: Show visible items
    for (GroundItem i : items) {
        if (c != null && i != null) {
            // Check if item should be visible to this player
            if (c.getItemAssistant().tradeable(i.getItemId()) || 
                i.getName().equalsIgnoreCase(c.playerName)) {
                
                // Check distance and height
                if (c.getH() == i.getItemH() && 
                    c.distanceToPoint(i.getItemX(), i.getItemY()) <= 60) {
                    
                    // Show private items to owner only
                    if (i.hideTicks > 0 && i.getName().equalsIgnoreCase(c.playerName)) {
                        c.getPacketSender().createGroundItem(
                            i.getItemId(), i.getItemX(), i.getItemY(), i.getItemAmount());
                    }
                    
                    // Show public items to everyone
                    if (i.hideTicks == 0) {
                        c.getPacketSender().createGroundItem(
                            i.getItemId(), i.getItemX(), i.getItemY(), i.getItemAmount());
                    }
                }
            }
        }
    }
}
```

### game.items.Item Removal

#### `removeGroundItem(game.entities.Player c, int itemId, int itemX, int itemY, boolean add)`
Handles item pickup by players:

```java
public void removeGroundItem(game.entities.Player c, int itemId, int itemX, int itemY, boolean add) {
    for (GroundItem i : items) {
        if (i.getItemId() == itemId && i.getItemX() == itemX && i.getItemY() == itemY) {
            
            // Handle private items (owner only)
            if (i.hideTicks > 0 && i.getName().equalsIgnoreCase(c.playerName)) {
                if (add) {
                    if (!c.getItemAssistant().specialCase(itemId)) {
                        if (c.getItemAssistant().addItem(i.getItemId(), i.getItemAmount())) {
                            removeControllersItem(i, c, i.getItemId(), 
                                                i.getItemX(), i.getItemY(), i.getItemAmount());
                            break;
                        }
                    } else {
                        removeControllersItem(i, c, i.getItemId(), 
                                            i.getItemX(), i.getItemY(), i.getItemAmount());
                        break;
                    }
                } else {
                    removeControllersItem(i, c, i.getItemId(), 
                                        i.getItemX(), i.getItemY(), i.getItemAmount());
                    break;
                }
            }
            
            // Handle public items (visible to all)
            else if (i.hideTicks <= 0) {
                if (add) {
                    if (c.getItemAssistant().addItem(i.getItemId(), i.getItemAmount())) {
                        removeGlobalItem(i, i.getItemId(), i.getItemX(), 
                                       i.getItemY(), i.getItemAmount());
                        break;
                    }
                } else {
                    removeGlobalItem(i, i.getItemId(), i.getItemX(), 
                                   i.getItemY(), i.getItemAmount());
                    break;
                }
            }
        }
    }
}
```

#### `removeGlobalItem(GroundItem i, int itemId, int itemX, int itemY, int itemAmount)`
Removes an item from all players' views:

```java
public void removeGlobalItem(GroundItem i, int itemId, int itemX, int itemY, int itemAmount) {
    for (game.entities.Player p : PlayerHandler.players) {
        if (p != null) {
            Client person = (Client) p;
            if (person != null) {
                if (person.distanceToPoint(itemX, itemY) <= 60) {
                    person.getPacketSender().removeGroundItem(itemId, itemX, itemY, itemAmount);
                }
            }
        }
    }
    removeItem(i);
}
```

### game.items.Item Processing

#### `process()`
Main processing method called every game tick:

```java
public void process() {
    ArrayList<GroundItem> toRemove = new ArrayList<GroundItem>();
    
    // Process all ground items
    for (int j = 0; j < items.size(); j++) {
        if (items.get(j) != null) {
            GroundItem i = items.get(j);
            
            // Handle private visibility timer
            if (i.hideTicks > 0) {
                i.hideTicks--;
            }
            
            // Transition from private to public
            if (i.hideTicks == 1) {
                i.hideTicks = 0;
                createGlobalItem(i);
                i.removeTicks = HIDE_TICKS;
            }
            
            // Handle removal timer
            if (i.removeTicks > 0) {
                i.removeTicks--;
            }
            
            // Mark for removal
            if (i.removeTicks == 1) {
                i.removeTicks = 0;
                toRemove.add(i);
            }
        }
    }
    
    // Remove expired items
    for (GroundItem i : toRemove) {
        removeGlobalItem(i, i.getItemId(), i.getItemX(), i.getItemY(), i.getItemAmount());
    }
}
```

### Utility Methods

#### `itemExists(int itemId, int itemX, int itemY)`
Checks if an item exists at specific coordinates:

```java
public boolean itemExists(int itemId, int itemX, int itemY) {
    for (GroundItem i : items) {
        if (i.getItemId() == itemId && i.getItemX() == itemX && i.getItemY() == itemY) {
            return true;
        }
    }
    
    // Check global drops as well
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
    for (GroundItem i : items) {
        if (i.getName().equalsIgnoreCase(name)) {
            if (i.getItemId() == itemId && i.getItemX() == itemX && i.getItemY() == itemY) {
                return i.getItemAmount();
            }
        }
    }
    return 0;
}
```

#### `moveItem(GroundItem item, int itemX, int itemY)`
Moves an item to a new location:

```java
public void moveItem(GroundItem item, int itemX, int itemY) {
    if (items.remove(item)) {
        int oldX = item.itemX;
        int oldY = item.itemY;
        item.itemX = itemX;
        item.itemY = itemY;
        items.add(item);
        
        // Update all players
        for (game.entities.Player p : PlayerHandler.players) {
            if (p == null) continue;
            p.getPacketSender().removeGroundItem(item.itemId, oldX, oldY, item.itemAmount);
            reloadItems(p);
        }
    }
}
```

## Special game.items.Item Handling

### Broken Barrows Items
```java
public int[][] brokenBarrows = { 
    { 4708, 4860 }, { 4710, 4866 }, { 4712, 4872 }, { 4714, 4878 }, 
    { 4716, 4884 }, { 4720, 4896 }, { 4718, 4890 }, { 4720, 4896 }, 
    { 4722, 4902 }, { 4732, 4932 }, { 4734, 4938 }, { 4736, 4944 }, 
    { 4738, 4950 }, { 4724, 4908 }, { 4726, 4914 }, { 4728, 4920 }, 
    { 4730, 4926 }, { 4745, 4956 }, { 4747, 4926 }, { 4749, 4968 }, 
    { 4751, 4994 }, { 4753, 4980 }, { 4755, 4986 }, { 4757, 4992 }, 
    { 4759, 4998 } 
};
```

Broken barrows items are automatically converted to their broken versions when dropped.

### Vanishing Items
Certain items like capes (IDs 2412-2414) vanish when dropped instead of appearing on the ground.

## Usage Examples

### Creating render.objects.Ground Items
```java
// game.entities.Player drops an item
ItemHandler itemHandler = GameEngine.itemHandler;
itemHandler.createGroundItem(player, 995, player.absX, player.absY, 1000, player.playerId);

// game.entities.NPC drops loot
itemHandler.createGroundItem(killer, 4151, npc.absX, npc.absY, 1, killer.playerId);

// Spawn a global item
GroundItem item = new GroundItem(itemId, x, y, height, amount, -1, 0, "");
itemHandler.createGlobalItem(item);
```

### Picking Up Items
```java
// game.entities.Player attempts to pick up item
itemHandler.removeGroundItem(player, itemId, x, y, true);

// Check if item exists before pickup
if (itemHandler.itemExists(itemId, x, y)) {
    int amount = itemHandler.itemAmount(player.playerName, itemId, x, y);
    // Process pickup
}
```

### Managing game.items.Item Visibility
```java
// Reload items for player entering new area
itemHandler.reloadItems(player);

// Move an item to new location
GroundItem item = findGroundItem(itemId, x, y);
if (item != null) {
    itemHandler.moveItem(item, newX, newY);
}
```

## Performance Considerations

### Optimization Strategies
- **Efficient Processing**: Use ArrayList for fast iteration and removal
- **Distance Checking**: Only update items for nearby players
- **Batch Operations**: Group item updates together
- **Memory Management**: Clean up expired items promptly

### Resource Management
- **game.items.Item Limits**: Monitor total number of ground items
- **Update Frequency**: Balance between responsiveness and performance
- **Network Optimization**: Minimize unnecessary packet sends

## Best Practices

1. **Always validate coordinates** before creating items
2. **Check item validity** before processing
3. **Handle special cases** for unique items
4. **Log important operations** for debugging
5. **Clean up expired items** regularly
6. **Optimize for nearby players** only
7. **Handle disconnections** gracefully

## Integration Points

### GameEngine Integration
```java
// Called every game tick
GameEngine.itemHandler.process();
```

### game.entities.Player Integration
```java
// game.entities.Player drops item
player.getItemAssistant().dropItem(itemId);

// game.entities.Player picks up item
player.getItemAssistant().pickupItem(itemId, x, y);
```

### Combat Integration
```java
// game.entities.NPC death drops
itemHandler.createGroundItem(killer, dropId, npc.absX, npc.absY, amount, killer.playerId);
```

## Related Classes

- [`GroundItem`](GroundItem.md) - Individual ground item data structure
- [`game.entities.Player`](game.entities.Player.md) - Players who interact with ground items
- [`ItemAssistant`](ItemAssistant.md) - Handles player item operations
- [`PacketSender`](PacketSender.md) - Sends ground item packets to client
- [`GlobalDropsHandler`](GlobalDropsHandler.md) - Manages global item spawns
- [`GameEngine`](GameEngine.md) - Calls ItemHandler.process() every tick