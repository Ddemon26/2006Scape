# Dueling

**Package:** `com.rs2.game.content.minigames`  
**Source:** [`2006Scape Server/src/main/java/com/rs2/game/content/minigames/Dueling.java`](2006Scape Server/src/main/java/com/rs2/game/content/minigames/Dueling.java)

## Overview

The `Dueling` class manages the player-versus-player dueling system in the 2006Scape server. It handles duel requests, stake management, duel rules configuration, and the complete dueling process from initiation to completion. This class implements the classic RuneScape dueling arena mechanics where players can stake items and fight under various rule restrictions.

## Key Responsibilities

- **Duel Requests**: Managing duel invitations and acceptances between players
- **Stake Management**: Handling items staked by both players in the duel
- **Rule Configuration**: Managing duel rules and restrictions (no magic, no ranged, etc.)
- **Interface Management**: Controlling the dueling interface and displays
- **Duel Execution**: Processing the actual combat and determining winners
- **game.items.Item Distribution**: Awarding staked items to the winner
- **Security**: Preventing scamming and ensuring fair duels

## Core Architecture

### game.entities.Player Association
```java
private final game.entities.Player player;

public Dueling(game.entities.Player player) {
    this.player = player;
}
```

Each Dueling instance is tied to a specific player, managing their dueling interactions.

### Stake Management
```java
public CopyOnWriteArrayList<GameItem> otherStakedItems = new CopyOnWriteArrayList<GameItem>();
public CopyOnWriteArrayList<GameItem> stakedItems = new CopyOnWriteArrayList<GameItem>();
```

Thread-safe collections store the items staked by each player in the duel.

## Core Methods

### Duel Initiation

#### `requestDuel(int playerId)`
Initiates a duel request with another player:

```java
public void requestDuel(int playerId) {
    try {
        // Prevent self-dueling
        if (playerId == player.playerId) {
            return;
        }
        
        // Check for Castle Wars items
        if (!CastleWars.deleteCastleWarsItems(player, playerId)) {
            return;
        }
        
        // Validate player state
        if (player.inTrade || player.isShopping) {
            player.getPacketSender().sendMessage("You can not stake currently.");
            return;
        }
        
        // Must be in duel arena
        if (!player.inDuelArena()) {
            player.getPacketSender().sendMessage("You must be in the duel arena to do that.");
            return;
        }
        
        // Reset duel state
        resetDuel();
        resetDuelItems();
        player.duelingWith = playerId;
        
        Client opponent = (Client) PlayerHandler.players[playerId];
        if (opponent == null) {
            return;
        }
        
        player.duelRequested = true;
        
        // Check if both players want to duel
        if (player.duelStatus == 0 && opponent.duelStatus == 0 && 
            opponent.duelRequested && player.duelingWith == opponent.getId() && 
            opponent.duelingWith == player.getId()) {
            
            // Check distance
            if (player.goodDistance(player.getX(), player.getY(), 
                                   opponent.getX(), opponent.getY(), 2)) {
                player.getDueling().openDuel();
                opponent.getDueling().openDuel();
            } else {
                player.getPacketSender().sendMessage(
                    "You need to get closer to your opponent to start the duel.");
            }
        } else {
            // Send duel request
            player.getPacketSender().sendMessage("Sending duel request...");
            opponent.getPacketSender().sendMessage(player.playerName + ":duelreq:");
        }
    } catch (Exception e) {
        System.out.println("Error requesting duel.");
    }
}
```

#### `openDuel()`
Opens the dueling interface for both players:

```java
public void openDuel() {
    Client opponent = (Client) PlayerHandler.players[player.duelingWith];
    if (opponent == null) {
        return;
    }
    
    // Validate state
    if (player.inTrade || player.isShopping) {
        player.getPacketSender().sendMessage("You can not stake currently.");
        return;
    }
    
    if (!player.inDuelArena()) {
        player.getPacketSender().sendMessage("You must be in the duel arena to do that.");
        return;
    }
    
    if (player.duelingArena()) {
        player.getPacketSender().sendMessage("You can't do that in a duel!");
        return;
    }
    
    // Set duel state
    player.duelStatus = 1;
    refreshduelRules();
    refreshDuelScreen();
    player.openDuel = true;
    opponent.openDuel = true;
    
    // Send equipment to interface
    for (int i = 0; i < player.playerEquipment.length; i++) {
        sendDuelEquipment(player.playerEquipment[i], player.playerEquipmentN[i], i);
    }
    
    // Setup interface
    player.getPacketSender().sendString("Dueling with: " + opponent.playerName + 
                                       " (level-" + opponent.combatLevel + ")", 6671);
    player.getPacketSender().sendString("", 6684);
    player.getPacketSender().sendFrame248(6575, 3321);
    player.getItemAssistant().resetItems(3322);
}
```

### Stake Management

#### `stakeItem(int itemID, int fromSlot, int amount)`
Adds an item to the player's stake:

```java
public boolean stakeItem(int itemID, int fromSlot, int amount) {
    // Check if item is stakeable
    for (int i : ItemConstants.ITEM_TRADEABLE) {
        if (i == itemID || (itemID >= 6864 && itemID <= 6882)) {
            player.getPacketSender().sendMessage("You can't stake that item.");
            return false;
        }
    }
    
    // Validate player state
    if (player.inTrade || player.isShopping) {
        player.getPacketSender().sendMessage("You can not stake currently.");
        return false;
    }
    
    if (!player.inDuelArena()) {
        player.getPacketSender().sendMessage("You must be in the duel arena to do that.");
        return false;
    }
    
    if (player.duelingArena()) {
        player.getPacketSender().sendMessage("You can't do that in a duel!");
        return false;
    }
    
    // Check rare item protection
    if (!RareProtection.removeItemOtherActions(player, itemID)) {
        return false;
    }
    
    if (amount <= 0) {
        return false;
    }
    
    Client opponent = (Client) PlayerHandler.players[player.duelingWith];
    if (!player.openDuel && !opponent.openDuel) {
        declineDuel();
        return false;
    }
    
    if (!player.getItemAssistant().playerHasItem(itemID, amount)) {
        return false;
    }
    
    if (opponent == null) {
        declineDuel();
        return false;
    }
    
    if (opponent.duelStatus <= 0 || player.duelStatus <= 0) {
        declineDuel();
        opponent.getDueling().declineDuel();
        return false;
    }
    
    // Reset confirmations when items change
    changeDuelStuff();
    
    // Handle non-stackable items
    if (!ItemDefinition.lookup(itemID).isStackable()) {
        for (int a = 0; a < amount; a++) {
            if (player.getItemAssistant().playerHasItem(itemID, 1)) {
                stakedItems.add(new GameItem(itemID, 1));
                player.getItemAssistant().deleteItem(itemID,
                    player.getItemAssistant().getItemSlot(itemID), 1);
            }
        }
    } else {
        // Handle stackable items
        boolean found = false;
        for (GameItem item : stakedItems) {
            if (item.id == itemID) {
                found = true;
                item.amount += amount;
                player.getItemAssistant().deleteItem(itemID, fromSlot, amount);
                break;
            }
        }
        
        if (!found) {
            player.getItemAssistant().deleteItem(itemID, fromSlot, amount);
            stakedItems.add(new GameItem(itemID, amount));
        }
    }
    
    // Update interfaces
    updateDuelInterfaces(opponent);
    return true;
}
```

#### `fromDuel(int itemID, int fromSlot, int amount)`
Removes an item from the player's stake:

```java
public boolean fromDuel(int itemID, int fromSlot, int amount) {
    Client opponent = (Client) PlayerHandler.players[player.duelingWith];
    if (opponent == null) {
        declineDuel();
        return false;
    }
    
    if (!player.openDuel && !opponent.openDuel) {
        declineDuel();
        return false;
    }
    
    // Reset confirmations when items change
    changeDuelStuff();
    
    // Find and remove item from stake
    for (GameItem item : stakedItems) {
        if (item.id == itemID) {
            if (item.amount > amount) {
                item.amount -= amount;
                player.getItemAssistant().addItem(itemID, amount);
            } else {
                player.getItemAssistant().addItem(itemID, item.amount);
                stakedItems.remove(item);
            }
            break;
        }
    }
    
    // Update interfaces
    updateDuelInterfaces(opponent);
    return true;
}
```

### Interface Management

#### `refreshDuelScreen()`
Updates the dueling interface with current stakes:

```java
public void refreshDuelScreen() {
    synchronized (player) {
        Client opponent = (Client) PlayerHandler.players[player.duelingWith];
        if (opponent == null) {
            return;
        }
        
        // Send player's staked items
        player.getOutStream().createFrameVarSizeWord(53);
        player.getOutStream().writeWord(6669);
        player.getOutStream().writeWord(stakedItems.toArray().length);
        
        int current = 0;
        for (GameItem item : stakedItems) {
            if (item.amount > 254) {
                player.getOutStream().writeByte(255);
                player.getOutStream().writeDWord_v2(item.amount);
            } else {
                player.getOutStream().writeByte(item.amount);
            }
            
            if (item.id > Constants.ITEM_LIMIT || item.id < 0) {
                item.id = Constants.ITEM_LIMIT;
            }
            player.getOutStream().writeWordBigEndianA(item.id + 1);
            current++;
        }
        
        // Fill remaining slots
        if (current < 27) {
            for (int i = current; i < 28; i++) {
                player.getOutStream().writeByte(1);
                player.getOutStream().writeWordBigEndianA(-1);
            }
        }
        player.getOutStream().endFrameVarSizeWord();
        player.flushOutStream();
        
        // Send opponent's staked items
        player.getOutStream().createFrameVarSizeWord(53);
        player.getOutStream().writeWord(6670);
        player.getOutStream().writeWord(opponent.getDueling().stakedItems.toArray().length);
        
        current = 0;
        for (GameItem item : opponent.getDueling().stakedItems) {
            if (item.amount > 254) {
                player.getOutStream().writeByte(255);
                player.getOutStream().writeDWord_v2(item.amount);
            } else {
                player.getOutStream().writeByte(item.amount);
            }
            
            if (item.id > Constants.ITEM_LIMIT || item.id < 0) {
                item.id = Constants.ITEM_LIMIT;
            }
            player.getOutStream().writeWordBigEndianA(item.id + 1);
            current++;
        }
        
        // Fill remaining slots
        if (current < 27) {
            for (int i = current; i < 28; i++) {
                player.getOutStream().writeByte(1);
                player.getOutStream().writeWordBigEndianA(-1);
            }
        }
        player.getOutStream().endFrameVarSizeWord();
        player.flushOutStream();
    }
}
```

#### `sendDuelEquipment(int itemId, int amount, int slot)`
Sends equipment information to the duel interface:

```java
public void sendDuelEquipment(int itemId, int amount, int slot) {
    synchronized (player) {
        if (itemId != 0) {
            player.getOutStream().createFrameVarSizeWord(34);
            player.getOutStream().writeWord(13824);
            player.getOutStream().writeByte(slot);
            player.getOutStream().writeWord(itemId + 1);
            
            if (amount > 254) {
                player.getOutStream().writeByte(255);
                player.getOutStream().writeDWord(amount);
            } else {
                player.getOutStream().writeByte(amount);
            }
            
            player.getOutStream().endFrameVarSizeWord();
            player.flushOutStream();
        }
    }
}
```

### Duel Rules Management

#### `refreshduelRules()`
Resets all duel rules to default state:

```java
public void refreshduelRules() {
    for (int i = 0; i < player.duelRule.length; i++) {
        player.duelRule[i] = false;
    }
    player.getPacketSender().sendFrame87(286, 0);
    player.duelOption = 0;
}
```

#### `checkDuelRules(int ruleId)`
Toggles a specific duel rule:

```java
public void checkDuelRules(int ruleId) {
    Client opponent = (Client) PlayerHandler.players[player.duelingWith];
    if (opponent == null) {
        return;
    }
    
    // Reset confirmations when rules change
    changeDuelStuff();
    
    // Toggle rule
    player.duelRule[ruleId] = !player.duelRule[ruleId];
    
    // Update interface
    player.getPacketSender().sendFrame87(286, player.duelOption);
    
    // Sync rules with opponent
    opponent.duelRule[ruleId] = player.duelRule[ruleId];
    opponent.getPacketSender().sendFrame87(286, opponent.duelOption);
}
```

### Duel Completion

#### `duelVictory()`
Handles duel victory and item distribution:

```java
public void duelVictory() {
    Client opponent = (Client) PlayerHandler.players[player.duelingWith];
    if (opponent == null) {
        return;
    }
    
    // Award winner's items back
    for (GameItem item : stakedItems) {
        player.getItemAssistant().addItemToBank(item.id, item.amount);
    }
    
    // Award opponent's staked items to winner
    for (GameItem item : opponent.getDueling().stakedItems) {
        player.getItemAssistant().addItemToBank(item.id, item.amount);
    }
    
    // Log the duel result
    GameLogger.writeLog(player.playerName, "duel_win", 
        player.playerName + " won duel against " + opponent.playerName);
    GameLogger.writeLog(opponent.playerName, "duel_loss", 
        opponent.playerName + " lost duel against " + player.playerName);
    
    // Send victory message
    player.getPacketSender().sendMessage("You won the duel!");
    
    // Reset duel state
    resetDuel();
    resetDuelItems();
    
    // Teleport back to duel arena lobby
    player.getPlayerAssistant().movePlayer(3366, 3266, 0);
}
```

#### `declineDuel()`
Handles duel cancellation and item return:

```java
public void declineDuel() {
    Client opponent = (Client) PlayerHandler.players[player.duelingWith];
    
    // Return staked items to player
    for (GameItem item : stakedItems) {
        player.getItemAssistant().addItem(item.id, item.amount);
    }
    
    // Reset duel state
    resetDuel();
    resetDuelItems();
    
    // Close interfaces
    player.getPacketSender().closeAllWindows();
    player.getPacketSender().sendMessage("Duel declined.");
    
    // Notify opponent
    if (opponent != null) {
        opponent.getDueling().declineDuel();
        opponent.getPacketSender().sendMessage(player.playerName + " has declined the duel.");
    }
}
```

### Utility Methods

#### `resetDuel()`
Resets all duel-related player state:

```java
public void resetDuel() {
    player.duelStatus = 0;
    player.duelingWith = 0;
    player.duelRequested = false;
    player.openDuel = false;
    player.duelRule = new boolean[22];
    player.duelOption = 0;
}
```

#### `resetDuelItems()`
Clears all staked items:

```java
public void resetDuelItems() {
    stakedItems.clear();
    otherStakedItems.clear();
}
```

#### `changeDuelStuff()`
Resets confirmations when duel state changes:

```java
public void changeDuelStuff() {
    Client opponent = (Client) PlayerHandler.players[player.duelingWith];
    if (opponent == null) {
        return;
    }
    
    // Reset confirmations
    player.duelStatus = 1;
    opponent.duelStatus = 1;
    
    // Update interface messages
    player.getPacketSender().sendString("", 6684);
    opponent.getPacketSender().sendString("", 6684);
}
```

## Usage Examples

### Initiating a Duel
```java
// game.entities.Player right-clicks another player and selects "Challenge"
player.getDueling().requestDuel(targetPlayerId);

// Both players must request each other to start duel
targetPlayer.getDueling().requestDuel(player.playerId);
```

### Managing Stakes
```java
// game.entities.Player stakes an item
player.getDueling().stakeItem(itemId, inventorySlot, amount);

// game.entities.Player removes item from stake
player.getDueling().fromDuel(itemId, stakeSlot, amount);
```

### Configuring Rules
```java
// Toggle no magic rule
player.getDueling().checkDuelRules(RULE_NO_MAGIC);

// Toggle no ranged rule
player.getDueling().checkDuelRules(RULE_NO_RANGED);

// Toggle no melee rule
player.getDueling().checkDuelRules(RULE_NO_MELEE);
```

### Completing Duels
```java
// Winner gets all staked items
winner.getDueling().duelVictory();

// Loser loses their staked items
loser.getDueling().resetDuel();
loser.getDueling().resetDuelItems();
```

## Performance Considerations

### Optimization Strategies
- **Thread-Safe Collections**: Use CopyOnWriteArrayList for concurrent access
- **Efficient Interface Updates**: Batch interface refreshes
- **Memory Management**: Clean up duel state properly
- **Network Optimization**: Minimize unnecessary packet sends

### Resource Management
- **game.items.Item Validation**: Check item existence before staking
- **State Synchronization**: Keep both players' states in sync
- **Exception Handling**: Gracefully handle disconnections during duels

## Best Practices

1. **Always validate player state** before processing duel actions
2. **Check item tradeability** before allowing stakes
3. **Synchronize duel states** between both players
4. **Handle disconnections gracefully** during duels
5. **Log important duel events** for monitoring
6. **Validate duel arena location** for all duel actions
7. **Reset confirmations** when duel state changes

## Integration Points

### game.entities.Player Integration
```java
// Dueling is part of every player
game.entities.Player player = new game.entities.Player();
Dueling dueling = player.getDueling();
```

### Combat Integration
```java
// Duel rules affect combat mechanics
if (player.duelRule[RULE_NO_MAGIC]) {
    // Disable magic attacks
}
```

### game.items.Item Integration
```java
// Items are transferred during dueling
player.getItemAssistant().addItem(itemId, amount);
player.getItemAssistant().deleteItem(itemId, amount);
```

## Related Classes

- [`game.entities.Player`](game.entities.Player.md) - Contains Dueling instance
- [`ItemAssistant`](ItemAssistant.md) - Handles item transfers
- [`GameItem`](GameItem.md) - Represents staked items
- [`RareProtection`](RareProtection.md) - Protects valuable items
- [`CastleWars`](CastleWars.md) - Handles Castle Wars item restrictions
- [`GameLogger`](GameLogger.md) - Logs duel events