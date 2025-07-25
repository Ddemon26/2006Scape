# ItemAssistant

**Package:** `com.rs2.game.items`  
**Source:** [`2006Scape Server/src/main/java/com/rs2/game/items/ItemAssistant.java`](2006Scape Server/src/main/java/com/rs2/game/items/ItemAssistant.java)

## Overview

The `ItemAssistant` class is a comprehensive helper class that manages all item-related operations for a player. It handles inventory management, banking, equipment, item trading, combat bonuses, and special item mechanics. This class is one of the most frequently used assistant classes in the server, providing essential functionality for item manipulation, validation, and synchronization with the game client.

## Key Responsibilities

- **Inventory Management**: Adding, removing, and organizing items in player inventory
- **Banking System**: Depositing, withdrawing, and managing bank storage
- **Equipment Handling**: Wearing, removing, and managing equipped items
- **Item Validation**: Checking item existence, tradeability, and requirements
- **Combat Bonuses**: Calculating and updating equipment bonuses
- **Death Mechanics**: Managing items kept on death and item dropping
- **Special Items**: Handling unique item behaviors and restrictions

## Core Architecture

### Player Association
```java
private final Player player;

public ItemAssistant(Player player) {
    this.player = player;
}
```

Each ItemAssistant instance is tied to a specific player, managing their personal item data.

### Item ID Convention
The server uses a consistent item ID convention:
- **Storage Arrays**: Item IDs are stored as `itemId + 1` (to distinguish from empty slots which are 0)
- **Method Parameters**: Methods typically accept the actual item ID
- **Conversion**: Internal methods handle the +1/-1 conversion automatically

## Core Methods

### Inventory Management

#### `addItem(int itemId, int amount)`
Adds items to the player's inventory:

```java
public boolean addItem(int itemId, int amount) {
    // Validate parameters
    if (amount < 1) amount = 1;
    if (itemId <= 0) return false;
    
    // Check for special restrictions
    if (itemId == CastleWars.SARA_BANNER || itemId == CastleWars.ZAMMY_BANNER) {
        return false;
    }
    
    // Check if we have space
    boolean hasSpace = (freeSlots() >= 1 || playerHasItem(itemId)) && 
                       ItemDefinition.lookup(itemId).isStackable() || 
                       freeSlots() > 0 && !ItemDefinition.lookup(itemId).isStackable();
    
    if (hasSpace) {
        // Handle stackable items
        if (ItemDefinition.lookup(itemId).isStackable()) {
            for (int i = 0; i < player.playerItems.length; i++) {
                if (player.playerItems[i] == itemId + 1 && player.playerItems[i] > 0) {
                    if (player.playerItemsN[i] + amount < Constants.MAXITEM_AMOUNT) {
                        player.playerItemsN[i] += amount;
                        resetItems(3214);
                        return true;
                    }
                }
            }
        }
        
        // Add to first empty slot
        for (int i = 0; i < player.playerItems.length; i++) {
            if (player.playerItems[i] <= 0) {
                player.playerItems[i] = itemId + 1;
                player.playerItemsN[i] = amount;
                resetItems(3214);
                return true;
            }
        }
    }
    
    return false;
}
```

**Returns:** `true` if item was successfully added, `false` if inventory is full or item is restricted

#### `deleteItem(int itemId, int amount)`
Removes items from the player's inventory:

```java
public void deleteItem(int itemId, int amount) {
    int slot = getItemSlot(itemId);
    if (slot != -1) {
        deleteItem(itemId, slot, amount);
    }
}

public void deleteItem(int itemId, int slot, int amount) {
    if (player.playerItems[slot] == itemId + 1) {
        if (player.playerItemsN[slot] > amount) {
            player.playerItemsN[slot] -= amount;
        } else {
            player.playerItemsN[slot] = 0;
            player.playerItems[slot] = 0;
        }
        resetItems(3214);
    }
}
```

#### `addOrDropItem(int itemId, int amount)`
Intelligently adds items to inventory or drops them on the ground:

```java
public void addOrDropItem(int itemId, int amount) {
    if (ItemDefinition.lookup(itemId).isStackable() && hasFreeSlots(1)) {
        addItem(itemId, amount);
    } else if (!hasFreeSlots(amount) && !ItemDefinition.lookup(itemId).isStackable()) {
        GameEngine.itemHandler.createGroundItem(player, itemId, player.getX(), 
                                               player.getY(), amount, player.playerId);
        player.getPacketSender().sendMessage("You have no inventory space, so the item(s) appear beneath you.");
    } else if (ItemDefinition.lookup(itemId).isStackable() && !hasFreeSlots(1) && !playerHasItem(itemId)) {
        GameEngine.itemHandler.createGroundItem(player, itemId, player.getX(), 
                                               player.getY(), amount, player.playerId);
        player.getPacketSender().sendMessage("You have no inventory space, so the item(s) appear beneath you.");
    } else {
        addItem(itemId, amount);
    }
}
```

### Item Validation

#### `playerHasItem(int itemId)` / `playerHasItem(int itemId, int amount)`
Checks if player has specific items:

```java
public boolean playerHasItem(int itemId) {
    for (int i = 0; i < player.playerItems.length; i++) {
        if (player.playerItems[i] == itemId + 1) {
            return true;
        }
    }
    return false;
}

public boolean playerHasItem(int itemId, int amount) {
    int totalAmount = 0;
    for (int i = 0; i < player.playerItems.length; i++) {
        if (player.playerItems[i] == itemId + 1) {
            totalAmount += player.playerItemsN[i];
        }
    }
    return totalAmount >= amount;
}
```

#### `tradeable(int itemId)`
Determines if an item can be traded:

```java
public boolean tradeable(int itemId) {
    for (int untradeableItem : ItemConstants.ITEM_TRADEABLE) {
        if (itemId == untradeableItem) {
            return false;
        }
    }
    return true;
}
```

#### `specialCase(int itemId)`
Identifies items with special handling requirements:

```java
public boolean specialCase(int itemId) {
    switch (itemId) {
        case 2518: // Obsidian cape
        case 2520: // Obsidian sword
        case 2522: // Obsidian knife
        case 2524: // Obsidian ring
        case 2526: // Obsidian shield
            return true;
    }
    return false;
}
```

### Banking System

#### `addItemToBank(int itemId, int amount)`
Deposits items into the player's bank:

```java
public void addItemToBank(int itemId, int amount) {
    itemId++; // Convert to storage format
    
    for (int i = 0; i < ItemConstants.BANK_SIZE; i++) {
        if (player.bankItems[i] <= 0 || 
            (player.bankItems[i] == itemId && 
             player.bankItemsN[i] + amount < Integer.MAX_VALUE)) {
            
            player.bankItems[i] = itemId;
            player.bankItemsN[i] += amount;
            resetBank();
            return;
        }
    }
}
```

#### `removeItemFromBank(int itemId, int amount)`
Withdraws items from the player's bank:

```java
public void removeItemFromBank(int itemId, int amount) {
    itemId++; // Convert to storage format
    
    for (int i = 0; i < ItemConstants.BANK_SIZE; i++) {
        if (player.bankItems[i] == itemId) {
            player.bankItemsN[i] -= amount;
            
            if (player.bankItemsN[i] <= 0) {
                player.bankItems[i] = 0;
                player.bankItemsN[i] = 0;
            }
            
            resetBank();
            rearrangeBank();
            return;
        }
    }
}
```

#### `bankItem(int itemId, int fromSlot, int amount)`
Handles the banking process from inventory:

```java
public boolean bankItem(int itemId, int fromSlot, int amount) {
    if (!playerHasItem(itemId, amount)) {
        return false;
    }
    
    if (freeBankSlots() <= 0 && getBankQuantity(itemId) <= 0) {
        player.getPacketSender().sendMessage("Your bank is full.");
        return false;
    }
    
    deleteItem(itemId, fromSlot, amount);
    addItemToBank(itemId, amount);
    return true;
}
```

### Equipment Management

#### `wearItem(int itemId, int slot)`
Equips an item to the specified equipment slot:

```java
public boolean wearItem(int itemId, int slot) {
    if (!playerHasItem(itemId)) {
        return false;
    }
    
    // Check requirements
    if (!meetsRequirements(itemId)) {
        return false;
    }
    
    // Handle two-handed weapons
    if (is2handed(DeprecatedItems.getItemName(itemId), itemId)) {
        if (player.playerEquipment[player.playerShield] > 0) {
            // Move shield to inventory
            addItem(player.playerEquipment[player.playerShield] - 1, 
                   player.playerEquipmentN[player.playerShield]);
            player.playerEquipment[player.playerShield] = 0;
            player.playerEquipmentN[player.playerShield] = 0;
        }
    }
    
    // Unequip current item in slot
    if (player.playerEquipment[slot] > 0) {
        addItem(player.playerEquipment[slot] - 1, 
               player.playerEquipmentN[slot]);
    }
    
    // Equip new item
    deleteItem(itemId, 1);
    player.playerEquipment[slot] = itemId + 1;
    player.playerEquipmentN[slot] = 1;
    
    // Update bonuses and appearance
    resetBonus();
    getBonus();
    writeBonus();
    player.getPlayerAssistant().requestUpdates();
    
    return true;
}
```

### Combat Bonuses

#### `getBonus()`
Calculates equipment bonuses:

```java
public void getBonus() {
    // Reset bonuses
    for (int i = 0; i < player.playerBonus.length; i++) {
        player.playerBonus[i] = 0;
    }
    
    // Calculate bonuses from equipped items
    for (int i = 0; i < player.playerEquipment.length; i++) {
        if (player.playerEquipment[i] > 0) {
            EquipmentDefinition def = EquipmentDefinition.lookup(player.playerEquipment[i] - 1);
            if (def != null) {
                for (int j = 0; j < player.playerBonus.length; j++) {
                    player.playerBonus[j] += def.getBonuses()[j];
                }
            }
        }
    }
}
```

#### `writeBonus()`
Sends bonus information to the client:

```java
public void writeBonus() {
    String[] BONUS_NAMES = {
        "Stab", "Slash", "Crush", "Magic", "Range",
        "Stab", "Slash", "Crush", "Magic", "Range",
        "Strength", "Prayer"
    };
    
    int offset = 0;
    for (int i = 0; i < player.playerBonus.length; i++) {
        String send;
        if (player.playerBonus[i] >= 0) {
            send = BONUS_NAMES[i] + ": +" + player.playerBonus[i];
        } else {
            send = BONUS_NAMES[i] + ": -" + Math.abs(player.playerBonus[i]);
        }
        
        if (i == 10) offset = 1;
        player.getPacketSender().sendString(send, 1675 + i + offset);
    }
}
```

### Death Mechanics

#### `dropAllItems()`
Handles item dropping on player death:

```java
public void dropAllItems() {
    Client killer = (Client) PlayerHandler.players[player.killerId];
    
    // Drop inventory items
    for (int i = 0; i < player.playerItems.length; i++) {
        if (killer != null) {
            if (tradeable(player.playerItems[i] - 1)) {
                // Tradeable items go to killer
                GameEngine.itemHandler.createGroundItem(killer,
                    player.playerItems[i] - 1, player.getX(), player.getY(),
                    player.playerItemsN[i], player.killerId);
            } else {
                // Untradeable items stay with original owner
                if (specialCase(player.playerItems[i] - 1)) {
                    // Special items convert to coins for killer
                    GameEngine.itemHandler.createGroundItem(killer, 995, 
                        player.getX(), player.getY(),
                        getUntradePrice(player.playerItems[i] - 1), player.killerId);
                }
                GameEngine.itemHandler.createGroundItem(player,
                    player.playerItems[i] - 1, player.getX(), player.getY(),
                    player.playerItemsN[i], player.playerId);
            }
        }
    }
    
    // Drop equipment items (similar logic)
    // Drop bones for killer
    if (killer != null) {
        GameEngine.itemHandler.createGroundItem(killer, 526, 
            player.getX(), player.getY(), 1, player.killerId);
    }
}
```

### Utility Methods

#### `freeSlots()`
Counts available inventory slots:

```java
public int freeSlots() {
    int freeSlots = 0;
    for (int i = 0; i < player.playerItems.length; i++) {
        if (player.playerItems[i] <= 0) {
            freeSlots++;
        }
    }
    return freeSlots;
}
```

#### `getItemSlot(int itemId)`
Finds the slot containing a specific item:

```java
public int getItemSlot(int itemId) {
    for (int i = 0; i < player.playerItems.length; i++) {
        if (player.playerItems[i] == itemId + 1) {
            return i;
        }
    }
    return -1;
}
```

#### `resetItems(int frameId)`
Synchronizes inventory with the client:

```java
public void resetItems(int frameId) {
    if (player.getOutStream() != null) {
        player.getOutStream().createFrameVarSizeWord(53);
        player.getOutStream().writeWord(frameId);
        player.getOutStream().writeWord(player.playerItems.length);
        
        for (int i = 0; i < player.playerItems.length; i++) {
            if (player.playerItemsN[i] > 254) {
                player.getOutStream().writeByte(255);
                player.getOutStream().writeDWord_v2(player.playerItemsN[i]);
            } else {
                player.getOutStream().writeByte(player.playerItemsN[i]);
            }
            player.getOutStream().writeWordBigEndianA(player.playerItems[i]);
        }
        
        player.getOutStream().endFrameVarSizeWord();
        player.flushOutStream();
    }
}
```

## Usage Examples

### Basic Inventory Operations
```java
// Add items to inventory
if (player.getItemAssistant().addItem(995, 1000)) {
    player.getPacketSender().sendMessage("You receive 1000 coins!");
} else {
    player.getPacketSender().sendMessage("Your inventory is full!");
}

// Check if player has items
if (player.getItemAssistant().playerHasItem(995, 100)) {
    player.getItemAssistant().deleteItem(995, 100);
    player.getPacketSender().sendMessage("You spend 100 coins.");
}

// Smart item adding (drops if inventory full)
player.getItemAssistant().addOrDropItem(1234, 1);
```

### Banking Operations
```java
// Deposit items
if (player.getItemAssistant().bankItem(995, slot, 1000)) {
    player.getPacketSender().sendMessage("You deposit 1000 coins.");
}

// Withdraw items
player.getItemAssistant().fromBank(995, bankSlot, 500);

// Check bank space
int freeSlots = player.getItemAssistant().freeBankSlots();
player.getPacketSender().sendMessage("Bank slots available: " + freeSlots);
```

### Equipment Management
```java
// Equip an item
if (player.getItemAssistant().wearItem(1277, player.playerWeapon)) {
    player.getPacketSender().sendMessage("You equip the dragon sword.");
}

// Check if item is equipped
if (player.getItemAssistant().playerHasEquipped(1277)) {
    player.getPacketSender().sendMessage("You have a dragon sword equipped.");
}

// Update combat bonuses
player.getItemAssistant().getBonus();
player.getItemAssistant().writeBonus();
```

### Item Validation
```java
// Check if item is tradeable
if (player.getItemAssistant().tradeable(itemId)) {
    // Allow trading
} else {
    player.getPacketSender().sendMessage("This item cannot be traded.");
}

// Check inventory space
if (player.getItemAssistant().hasFreeSlots(5)) {
    // Give reward that requires 5 slots
}
```

## Performance Considerations

### Optimization Strategies
- **Batch Operations**: Group multiple item operations together
- **Efficient Searching**: Use getItemSlot() to avoid repeated loops
- **Client Updates**: Only call resetItems() when necessary
- **Memory Management**: Clean up temporary arrays and objects

### Common Pitfalls
- **Item ID Confusion**: Remember the +1/-1 convention for storage vs parameters
- **Stack Overflow**: Check for maximum item amounts in stackable items
- **Null Checks**: Always verify player and stream existence
- **Concurrent Modification**: Be careful when modifying arrays during iteration

## Best Practices

1. **Always validate parameters** before performing operations
2. **Check inventory space** before adding items
3. **Use addOrDropItem()** for rewards to prevent item loss
4. **Update client interfaces** after item changes
5. **Handle special cases** for unique items
6. **Log important operations** for debugging
7. **Respect item restrictions** (tradeable, requirements, etc.)

## Related Classes

- [`Player`](Player.md) - Contains ItemAssistant instance
- [`ItemHandler`](ItemHandler.md) - Manages ground items
- [`ItemDefinition`](ItemDefinition.md) - Item properties and definitions
- [`EquipmentDefinition`](EquipmentDefinition.md) - Equipment bonuses and requirements
- [`ItemConstants`](ItemConstants.md) - Item-related constants and arrays
- [`PacketSender`](PacketSender.md) - Sends item updates to client
