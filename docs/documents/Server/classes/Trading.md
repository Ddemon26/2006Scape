# Trading

**Package:** `com.rs2.game.players`  
**Source:** [`2006Scape Server/src/main/java/com/rs2/game/players/Trading.java`](2006Scape Server/src/main/java/com/rs2/game/players/Trading.java)

## Overview

The `Trading` class manages the player-to-player trading system in the 2006Scape server. It handles all aspects of the trading process, including trade requests, item offers, trade confirmation, and the final exchange of items between players. This class implements the classic two-stage trading system with initial offers and confirmation screens to prevent scamming, ensuring secure and fair player transactions.

## Key Responsibilities

- **Trade Requests**: Initiating and accepting trade requests between players
- **Trade Interface**: Managing the trading interface and its components
- **game.items.Item Offers**: Handling the addition and removal of items from trade offers
- **Trade Validation**: Ensuring trades are valid and players have sufficient inventory space
- **Trade Confirmation**: Implementing the two-stage confirmation process
- **game.items.Item Exchange**: Transferring items between players when trades complete
- **Trade Cancellation**: Handling declined trades and returning items

## Core Architecture

### game.entities.Player Association
```java
private final game.entities.Player player;

public Trading(game.entities.Player player) {
    this.player = player;
}
```

Each Trading instance is tied to a specific player, managing their side of any trade.

### Trade States
```java
// In game.entities.Player class
public boolean inTrade;
public boolean tradeConfirmed;
public boolean tradeConfirmed2;
public int tradeWith;
public int[] offeredItems = new int[Constants.BANK_SIZE];
public int[] offeredItemsN = new int[Constants.BANK_SIZE];
public int offeredItemsCount;
```

These fields track the current state of a player's trade, including who they're trading with, what items they've offered, and whether they've confirmed the trade.

## Core Methods

### Trade Initiation

#### `requestTrade(int playerId)`
Initiates a trade request with another player:

```java
public void requestTrade(int playerId) {
    game.entities.Player otherPlayer = PlayerHandler.players[playerId];
    
    // Validate trade request
    if (otherPlayer == null || !validTradeRequest(otherPlayer)) {
        return;
    }
    
    // Check if other player has pending trade request from this player
    if (otherPlayer.tradeWith == player.playerId && !player.inTrade) {
        // Both players want to trade, so start the trade
        openTrade();
        otherPlayer.getTrading().openTrade();
    } else if (!player.inTrade) {
        // Send trade request
        player.tradeWith = playerId;
        player.getPacketSender().sendMessage("Sending trade request...");
        otherPlayer.getPacketSender().sendMessage(player.playerName + ":tradereq:");
    }
}
```

#### `openTrade()`
Opens the trading interface for a player:

```java
public void openTrade() {
    game.entities.Player otherPlayer = PlayerHandler.players[player.tradeWith];
    
    // Reset trade state
    resetTrade();
    
    // Set trade flags
    player.inTrade = true;
    player.canOffer = true;
    
    // Send trade interface
    player.getPacketSender().sendFrame248(3323, 3321);
    player.getPacketSender().sendString("Trading with: " + otherPlayer.properName, 3417);
    player.getPacketSender().sendString("", 3431);
    
    // Show player inventories
    player.getItemAssistant().resetItems(3322);
    updateOfferComponents();
    player.getPacketSender().sendString("Trading with: " + otherPlayer.properName, 3417);
}
```

### game.items.Item Management

#### `tradeItem(int itemId, int fromSlot, int amount)`
Adds an item to the trade offer:

```java
public void tradeItem(int itemId, int fromSlot, int amount) {
    game.entities.Player otherPlayer = PlayerHandler.players[player.tradeWith];
    
    // Validate trade state
    if (!player.inTrade || !player.canOffer) {
        return;
    }
    
    // Check if item exists and is tradeable
    if (!player.getItemAssistant().playerHasItem(itemId, amount) || 
        !player.getItemAssistant().tradeable(itemId)) {
        player.getPacketSender().sendMessage("You cannot trade this item.");
        return;
    }
    
    // Reset trade confirmation
    player.tradeConfirmed = false;
    otherPlayer.tradeConfirmed = false;
    
    // Handle stackable items
    if (game.items.Item.itemStackable[itemId]) {
        boolean itemInTrade = false;
        for (int i = 0; i < player.offeredItemsCount; i++) {
            if (player.offeredItems[i] == itemId) {
                itemInTrade = true;
                player.offeredItemsN[i] += amount;
                player.getItemAssistant().deleteItem(itemId, amount);
                break;
            }
        }
        
        if (!itemInTrade) {
            player.offeredItems[player.offeredItemsCount] = itemId;
            player.offeredItemsN[player.offeredItemsCount] = amount;
            player.offeredItemsCount++;
            player.getItemAssistant().deleteItem(itemId, amount);
        }
    } else {
        // Non-stackable items
        for (int i = 0; i < amount; i++) {
            if (player.getItemAssistant().playerHasItem(itemId, 1)) {
                player.offeredItems[player.offeredItemsCount] = itemId;
                player.offeredItemsN[player.offeredItemsCount] = 1;
                player.offeredItemsCount++;
                player.getItemAssistant().deleteItem(itemId, 1);
            }
        }
    }
    
    // Update trade interfaces
    updateOfferComponents();
    otherPlayer.getTrading().updateOfferComponents();
}
```

#### `fromTrade(int itemId, int fromSlot, int amount)`
Removes an item from the trade offer:

```java
public boolean fromTrade(int itemId, int fromSlot, int amount) {
    game.entities.Player otherPlayer = PlayerHandler.players[player.tradeWith];
    
    // Validate trade state
    if (!player.inTrade || !player.canOffer) {
        return false;
    }
    
    // Reset trade confirmation
    player.tradeConfirmed = false;
    otherPlayer.tradeConfirmed = false;
    
    // Check if item exists in trade
    if (amount > player.offeredItemsN[fromSlot]) {
        amount = player.offeredItemsN[fromSlot];
    }
    
    // Handle item removal
    if (amount <= 0) {
        return false;
    }
    
    // Add item back to inventory
    player.getItemAssistant().addItem(player.offeredItems[fromSlot], amount);
    
    // Update trade offer
    player.offeredItemsN[fromSlot] -= amount;
    if (player.offeredItemsN[fromSlot] <= 0) {
        // Shift remaining items
        for (int i = fromSlot; i < player.offeredItemsCount - 1; i++) {
            player.offeredItems[i] = player.offeredItems[i + 1];
            player.offeredItemsN[i] = player.offeredItemsN[i + 1];
        }
        player.offeredItems[player.offeredItemsCount - 1] = 0;
        player.offeredItemsN[player.offeredItemsCount - 1] = 0;
        player.offeredItemsCount--;
    }
    
    // Update trade interfaces
    updateOfferComponents();
    otherPlayer.getTrading().updateOfferComponents();
    return true;
}
```

### Trade Confirmation

#### `confirmScreen()`
Displays the trade confirmation screen:

```java
public void confirmScreen() {
    game.entities.Player otherPlayer = PlayerHandler.players[player.tradeWith];
    
    // Send confirmation interface
    player.getPacketSender().sendFrame248(3443, 3213);
    player.getPacketSender().sendString("Are you sure you want to make this trade?", 3535);
    
    // Build item lists
    StringBuilder playerItems = new StringBuilder("You are about to give:");
    StringBuilder otherItems = new StringBuilder(otherPlayer.properName + " is offering:");
    
    // List player's offered items
    if (player.offeredItemsCount == 0) {
        playerItems.append("\\nNothing!");
    }
    for (int i = 0; i < player.offeredItemsCount; i++) {
        String itemName = game.items.Item.getItemName(player.offeredItems[i]);
        if (player.offeredItemsN[i] > 1) {
            playerItems.append("\\n").append(player.offeredItemsN[i]).append(" x ").append(itemName);
        } else {
            playerItems.append("\\n").append(itemName);
        }
    }
    
    // List other player's offered items
    if (otherPlayer.offeredItemsCount == 0) {
        otherItems.append("\\nNothing!");
    }
    for (int i = 0; i < otherPlayer.offeredItemsCount; i++) {
        String itemName = game.items.Item.getItemName(otherPlayer.offeredItems[i]);
        if (otherPlayer.offeredItemsN[i] > 1) {
            otherItems.append("\\n").append(otherPlayer.offeredItemsN[i]).append(" x ").append(itemName);
        } else {
            otherItems.append("\\n").append(itemName);
        }
    }
    
    // Send item lists to interface
    player.getPacketSender().sendString(playerItems.toString(), 3557);
    player.getPacketSender().sendString(otherItems.toString(), 3558);
    
    // Update player state
    player.canOffer = false;
}
```

#### `confirmAccepted()`
Handles the final trade confirmation:

```java
public void confirmAccepted() {
    game.entities.Player otherPlayer = PlayerHandler.players[player.tradeWith];
    
    // Mark this player as confirmed
    player.tradeConfirmed2 = true;
    
    // Check if both players have confirmed
    if (otherPlayer.tradeConfirmed2) {
        // Check if trade is valid
        if (!tradeSuccessful()) {
            return;
        }
        
        // Exchange items
        giveItems();
        otherPlayer.getTrading().giveItems();
        
        // Reset trade state
        player.getPacketSender().sendMessage("Trade accepted!");
        player.inTrade = false;
        player.tradeWith = 0;
        player.canOffer = true;
        player.tradeConfirmed = false;
        player.tradeConfirmed2 = false;
        player.getPacketSender().closeAllWindows();
        
        // Reset other player's state
        otherPlayer.inTrade = false;
        otherPlayer.tradeWith = 0;
        otherPlayer.canOffer = true;
        otherPlayer.tradeConfirmed = false;
        otherPlayer.tradeConfirmed2 = false;
        otherPlayer.getPacketSender().closeAllWindows();
    } else {
        // Wait for other player to confirm
        player.getPacketSender().sendString("Waiting for other player...", 3431);
    }
}
```

### Trade Completion

#### `giveItems()`
Transfers items from the other player to this player:

```java
public void giveItems() {
    game.entities.Player otherPlayer = PlayerHandler.players[player.tradeWith];
    
    // Check if other player has offered items
    if (otherPlayer.offeredItemsCount == 0) {
        return;
    }
    
    // Transfer each item
    for (int i = 0; i < otherPlayer.offeredItemsCount; i++) {
        if (otherPlayer.offeredItems[i] > 0) {
            player.getItemAssistant().addItem(otherPlayer.offeredItems[i], otherPlayer.offeredItemsN[i]);
            
            // Log the trade
            GameLogger.writeLog(player.playerName, "trade", 
                player.playerName + " received " + otherPlayer.offeredItemsN[i] + 
                " x " + game.items.Item.getItemName(otherPlayer.offeredItems[i]) + 
                " from " + otherPlayer.playerName);
        }
    }
}
```

#### `tradeSuccessful()`
Verifies that both players have enough inventory space for the trade:

```java
public boolean tradeSuccessful() {
    game.entities.Player otherPlayer = PlayerHandler.players[player.tradeWith];
    
    // Count required inventory slots
    int freeSlots = player.getItemAssistant().freeSlots();
    int requiredSlots = 0;
    
    // Calculate slots needed for non-stackable items
    for (int i = 0; i < otherPlayer.offeredItemsCount; i++) {
        if (otherPlayer.offeredItems[i] > 0) {
            if (game.items.Item.itemStackable[otherPlayer.offeredItems[i]] || 
                game.items.Item.itemIsNote[otherPlayer.offeredItems[i]]) {
                // Check if player already has this stackable item
                if (player.getItemAssistant().playerHasItem(otherPlayer.offeredItems[i])) {
                    continue;
                }
                requiredSlots++;
            } else {
                requiredSlots += otherPlayer.offeredItemsN[i];
            }
        }
    }
    
    // Check if player has enough space
    if (freeSlots < requiredSlots) {
        player.getPacketSender().sendMessage("You don't have enough inventory space for this trade.");
        otherPlayer.getPacketSender().sendMessage(player.properName + " doesn't have enough inventory space for this trade.");
        declineTrade(false);
        return false;
    }
    
    // Check if other player has enough space (similar logic)
    // ... validation for other player
    
    return true;
}
```

### Trade Cancellation

#### `declineTrade(boolean tellOther)`
Cancels the trade and returns all items:

```java
public void declineTrade(boolean tellOther) {
    game.entities.Player otherPlayer = PlayerHandler.players[player.tradeWith];
    
    // Return player's offered items
    for (int i = 0; i < player.offeredItemsCount; i++) {
        if (player.offeredItems[i] > 0) {
            player.getItemAssistant().addItem(player.offeredItems[i], player.offeredItemsN[i]);
        }
    }
    
    // Reset trade state
    player.getPacketSender().closeAllWindows();
    player.tradeConfirmed = false;
    player.tradeConfirmed2 = false;
    player.inTrade = false;
    player.tradeWith = 0;
    player.canOffer = true;
    player.getPacketSender().sendMessage("Trade declined.");
    
    // Notify other player if needed
    if (tellOther && otherPlayer != null) {
        otherPlayer.getTrading().declineTrade(false);
        otherPlayer.getPacketSender().sendMessage(player.properName + " has declined the trade.");
    }
}
```

## Usage Examples

### Initiating a Trade
```java
// game.entities.Player clicks "Trade with" option on another player
player.getTrading().requestTrade(targetPlayerId);

// Target player accepts the trade request
targetPlayer.getTrading().requestTrade(player.playerId);
// This automatically opens the trade interface for both players
```

### Offering Items
```java
// game.entities.Player offers 100 coins from inventory slot 0
player.getTrading().tradeItem(995, 0, 100);

// game.entities.Player offers all coins from inventory slot 0
int coinAmount = player.getItemAssistant().getItemAmount(995);
player.getTrading().tradeItem(995, 0, coinAmount);
```

### Stopping Events
```java
// Store event reference for later cancellation
CycleEventContainer delayedTask = CycleEventHandler.getSingleton().addEvent(player, new CycleEvent() {
    @Override
    public void execute(CycleEventContainer container) {
        // Task code
    }
    
    @Override
    public void stop() {
        // Cleanup
    }
}, 10);

// Later, cancel the event if needed
delayedTask.stop();

// Or stop all events for an object
CycleEventHandler.getSingleton().stopEvents(player);
```

## Best Practices

1. **Always validate trade state** before performing operations
2. **Check item tradeability** before allowing offers
3. **Verify inventory space** before completing trades
4. **Handle disconnections gracefully** during trades
5. **Log important trade operations** for monitoring
6. **Implement proper confirmation** to prevent scamming
7. **Return items safely** when trades are cancelled

## Related Classes

- [`game.entities.Player`](game.entities.Player.md) - Contains Trading instance
- [`ItemAssistant`](ItemAssistant.md) - Handles item transfers
- [`PacketSender`](PacketSender.md) - Sends trade interfaces
- [`PlayerHandler`](PlayerHandler.md) - Manages player references
- [`GameLogger`](GameLogger.md) - Logs trade transactions