# GroundItem

**Package:** `com.rs2.game.items`  
**Source:** [`2006Scape Server/src/main/java/com/rs2/game/items/GroundItem.java`](2006Scape Server/src/main/java/com/rs2/game/items/GroundItem.java)

## Overview

The `GroundItem` class represents an individual item that exists on the ground in the game world. It serves as a data container that holds all the necessary information about a dropped item, including its identity, location, ownership, visibility rules, and lifecycle timers. This class is the fundamental building block of the ground item system and works closely with the [`ItemHandler`](ItemHandler.md) to manage item visibility and pickup mechanics.

## Key Responsibilities

- **Item Data Storage**: Storing item ID, amount, and location information
- **Ownership Tracking**: Managing who dropped the item and can see it
- **Visibility Control**: Handling private/public visibility phases
- **Timer Management**: Tracking hide and removal timers
- **Location Management**: Maintaining precise world coordinates and height

## Core Architecture

### Data Structure
```java
public class GroundItem {
    public int itemId;          // Item definition ID
    public int itemX, itemY;    // World coordinates
    public int itemH;           // Height level (0-3)
    public int itemAmount;      // Stack size
    public int itemController;  // Player ID who owns/dropped the item
    public int hideTicks;       // Ticks until item becomes public
    public int removeTicks;     // Ticks until item despawns
    public String ownerName;    // Name of the player who dropped it
}
```

### Lifecycle Phases
Ground items go through distinct phases during their lifetime:

1. **Private Phase** (`hideTicks > 0`): Only visible to the dropper
2. **Public Phase** (`hideTicks = 0, removeTicks > 0`): Visible to all players
3. **Removal** (`removeTicks = 0`): Item is deleted from the world

## Constructor

### `GroundItem(int id, int x, int y, int height, int amount, int controller, int hideTicks, String name)`
Creates a new ground item with all necessary properties:

```java
public GroundItem(int id, int x, int y, int height, int amount, 
                 int controller, int hideTicks, String name) {
    this.itemId = id;
    this.itemX = x;
    this.itemY = y;
    this.itemH = height;
    this.itemAmount = amount;
    this.itemController = controller;
    this.hideTicks = hideTicks;
    this.ownerName = name;
}
```

**Parameters:**
- `id` - Item definition ID from the cache
- `x, y` - World coordinates where the item is located
- `height` - Height level (0=ground, 1-3=upper levels)
- `amount` - Number of items in the stack
- `controller` - Player ID who dropped the item (determines initial visibility)
- `hideTicks` - Number of game ticks before item becomes public (typically 100)
- `name` - Username of the player who dropped the item

## Accessor Methods

### Item Identity
```java
public int getItemId() {
    return itemId;
}

public int getItemAmount() {
    return itemAmount;
}
```

### Location Information
```java
public int getItemX() {
    return itemX;
}

public int getItemY() {
    return itemY;
}

public int getItemH() {
    return itemH;
}
```

### Ownership Information
```java
public int getItemController() {
    return itemController;
}

public String getName() {
    return ownerName;
}
```

## Usage in the Item System

### Creation Process
Ground items are typically created when:
- Players drop items from their inventory
- NPCs die and drop loot
- Objects are harvested (mining, woodcutting)
- Quest rewards are given
- Special events occur

```java
// Example: Player drops an item
GroundItem droppedItem = new GroundItem(
    995,                    // Coins
    player.absX,           // Player's X coordinate
    player.absY,           // Player's Y coordinate
    player.heightLevel,    // Player's height level
    1000,                  // Amount (1000 coins)
    player.playerId,       // Player who dropped it
    ItemHandler.HIDE_TICKS, // 100 ticks private visibility
    player.playerName      // Player's name
);
```

### Integration with ItemHandler
The ItemHandler manages collections of GroundItem objects:

```java
// Adding to the world
GameEngine.itemHandler.addItem(groundItem);

// Processing timers
for (GroundItem item : items) {
    if (item.hideTicks > 0) {
        item.hideTicks--;
    }
    if (item.removeTicks > 0) {
        item.removeTicks--;
    }
}

// Visibility checks
if (item.hideTicks > 0 && item.getName().equalsIgnoreCase(player.playerName)) {
    // Show to owner only
    player.getPacketSender().createGroundItem(item.getItemId(), 
        item.getItemX(), item.getItemY(), item.getItemAmount());
} else if (item.hideTicks == 0) {
    // Show to everyone
    player.getPacketSender().createGroundItem(item.getItemId(), 
        item.getItemX(), item.getItemY(), item.getItemAmount());
}
```

## Visibility Rules

### Private Visibility Phase
During the private phase (`hideTicks > 0`):
- Only the original dropper can see the item
- Other players cannot see or interact with the item
- Prevents item sniping and gives dropper time to reclaim

### Public Visibility Phase
During the public phase (`hideTicks = 0, removeTicks > 0`):
- All players can see the item
- Anyone can pick up the item
- First come, first served basis

### Special Cases
- **Untradeable Items**: May remain private to the original owner
- **PvP Drops**: May have different visibility rules for the killer
- **Instance Items**: May only be visible to specific players

## Timer Management

### Hide Timer (`hideTicks`)
- Starts at `ItemHandler.HIDE_TICKS` (typically 100 ticks = ~60 seconds)
- Decrements each game tick
- When it reaches 0, item becomes public
- Controls the private → public transition

### Remove Timer (`removeTicks`)
- Set when item becomes public (typically another 100 ticks)
- Decrements each game tick
- When it reaches 0, item is removed from the world
- Controls the public → removal transition

```java
// Timer processing in ItemHandler
if (item.hideTicks > 0) {
    item.hideTicks--;
    
    // Transition to public
    if (item.hideTicks == 1) {
        item.hideTicks = 0;
        item.removeTicks = ItemHandler.HIDE_TICKS;
        // Make visible to all players
    }
}

if (item.removeTicks > 0) {
    item.removeTicks--;
    
    // Mark for removal
    if (item.removeTicks == 1) {
        item.removeTicks = 0;
        // Item will be deleted
    }
}
```

## Usage Examples

### Creating Different Types of Ground Items

#### Player Drop
```java
// Player drops coins
GroundItem coins = new GroundItem(
    995,                        // Coins ID
    player.absX, player.absY,   // Player location
    player.heightLevel,         // Same height as player
    500,                        // 500 coins
    player.playerId,            // Player owns it
    100,                        // 100 ticks private
    player.playerName           // Player's name
);
```

#### NPC Death Drop
```java
// NPC drops loot for killer
GroundItem loot = new GroundItem(
    1277,                       // Dragon sword
    npc.absX, npc.absY,        // NPC location
    npc.heightLevel,           // Same height as NPC
    1,                         // Single item
    killer.playerId,           // Killer gets it first
    100,                       // Private for 100 ticks
    killer.playerName          // Killer's name
);
```

#### Public Item (No Private Phase)
```java
// Item that's immediately public
GroundItem publicItem = new GroundItem(
    itemId, x, y, height,
    amount,
    -1,                        // No specific owner
    0,                         // No hide time
    ""                         // No owner name
);
publicItem.removeTicks = 200;  // Will despawn in 200 ticks
```

### Checking Item Properties
```java
// Check if item belongs to specific player
if (groundItem.getName().equalsIgnoreCase(player.playerName)) {
    // Player can pick up this item
}

// Check if item is in private phase
if (groundItem.hideTicks > 0) {
    // Only owner can see it
} else {
    // Everyone can see it
}

// Check location
if (groundItem.getItemX() == targetX && 
    groundItem.getItemY() == targetY && 
    groundItem.getItemH() == targetHeight) {
    // Item is at target location
}
```

### Working with ItemHandler
```java
// Find ground item at location
for (GroundItem item : itemHandler.items) {
    if (item.getItemX() == x && item.getItemY() == y && 
        item.getItemH() == height && item.getItemId() == itemId) {
        // Found the item
        return item;
    }
}

// Remove ground item
itemHandler.removeItem(groundItem);

// Move ground item to new location
itemHandler.moveItem(groundItem, newX, newY);
```

## Performance Considerations

### Memory Efficiency
- GroundItem objects are lightweight data containers
- No complex logic or heavy operations
- Efficient for storing in collections

### Processing Efficiency
- Simple field access methods
- No expensive calculations
- Suitable for frequent iteration

## Best Practices

1. **Always set appropriate timers** for item lifecycle management
2. **Use correct ownership information** for proper visibility rules
3. **Validate coordinates** before creating ground items
4. **Handle special item types** (untradeable, quest items) appropriately
5. **Clean up references** when items are removed
6. **Consider height levels** for multi-level areas
7. **Use meaningful owner names** for debugging and logging

## Integration Points

### ItemHandler Integration
```java
// ItemHandler manages collections of GroundItems
List<GroundItem> items = new ArrayList<GroundItem>();
items.add(groundItem);

// Processing occurs in ItemHandler.process()
for (GroundItem item : items) {
    // Process timers and visibility
}
```

### Player Integration
```java
// Players interact with ground items
player.getPacketSender().createGroundItem(
    item.getItemId(), item.getItemX(), item.getItemY(), item.getItemAmount());

// Pickup attempts
if (item.getName().equalsIgnoreCase(player.playerName) || item.hideTicks == 0) {
    // Player can pick up this item
}
```

### Combat Integration
```java
// NPCs drop items when they die
GroundItem drop = new GroundItem(
    dropId, npc.absX, npc.absY, npc.heightLevel,
    amount, killer.playerId, 100, killer.playerName);
```

## Related Classes

- [`ItemHandler`](ItemHandler.md) - Manages collections of GroundItems
- [`ItemAssistant`](ItemAssistant.md) - Handles player item operations
- [`GameEngine`](GameEngine.md) - Processes ground items every tick
- [`Player`](Player.md) - Interacts with ground items
- [`PacketSender`](PacketSender.md) - Sends ground item updates to clients
