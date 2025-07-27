# ShopAssistant

**Package:** `com.rs2.game.shops`  
**Source:** [`2006Scape Server/src/main/java/com/rs2/game/shops/ShopAssistant.java`](2006Scape Server/src/main/java/com/rs2/game/shops/ShopAssistant.java)

## Overview

The `ShopAssistant` class manages all player interactions with shops in the 2006Scape server. It handles opening shop interfaces, buying and selling items, price calculations, and shop inventory updates. This class serves as the bridge between the player and the global shop system managed by [`ShopHandler`](ShopHandler.md), providing player-specific shop functionality and ensuring proper synchronization between player actions and the shop state.

## Key Responsibilities

- **Shop Interface Management**: Opening and updating shop interfaces for players
- **Transaction Processing**: Handling buying and selling of items
- **Price Calculation**: Determining buy and sell prices for items
- **Inventory Management**: Transferring items between player and shop inventories
- **Currency Handling**: Managing different currency types (coins, tokkul, etc.)
- **game.entities.Player-Owned Shops**: Supporting custom player-run shops
- **Shop Validation**: Checking if shops buy/sell specific items

## Core Architecture

### game.entities.Player Association
```java
private final game.entities.Player player;

public ShopAssistant(game.entities.Player player) {
    this.player = player;
}
```

Each ShopAssistant instance is tied to a specific player, providing personalized shop interactions.

## Core Methods

### Shop Interface Management

#### `openShop(int shopId)`
Opens a shop interface for the player:

```java
public void openShop(int shopId) {
    // Validate shop ID
    if (shopId > ShopHandler.MAX_SHOPS) {
        return;
    }
    
    // Set player's shop state
    player.shopId = shopId;
    player.isShopping = true;
    player.updateShop = false;
    
    // Send shop interface
    player.getPacketSender().sendFrame248(3824, 3822);
    player.getPacketSender().sendString(ShopHandler.shopName[shopId], 3901);
    
    // Update shop items
    updateShop(shopId);
    
    // Update player's money display
    player.getItemAssistant().resetItems(3823);
    player.getPacketSender().sendString("Shop Owner\\nRight click to buy or sell items.", 3903);
}
```

#### `updateShop(int shopId)`
Updates the shop interface with current inventory:

```java
public void updateShop(int shopId) {
    // Reset shop interface
    player.getPacketSender().resetItems(3900);
    
    // Create item arrays for the interface
    int totalItems = 0;
    for (int i = 0; i < ShopHandler.MAX_SHOP_ITEMS; i++) {
        if (ShopHandler.shopItems[shopId][i] > 0) {
            totalItems++;
        }
    }
    
    // If shop is empty, show placeholder message
    if (totalItems == 0) {
        player.getPacketSender().sendString("This shop has no items in it!", 3903);
    }
    
    // Create arrays for item display
    int TotalCount = 0;
    int[] id = new int[ShopHandler.MAX_SHOP_ITEMS];
    int[] count = new int[ShopHandler.MAX_SHOP_ITEMS];
    
    // Populate arrays with shop items
    for (int i = 0; i < ShopHandler.MAX_SHOP_ITEMS; i++) {
        if (ShopHandler.shopItems[shopId][i] > 0) {
            id[TotalCount] = ShopHandler.shopItems[shopId][i] - 1;
            count[TotalCount] = ShopHandler.shopItemsN[shopId][i];
            TotalCount++;
        }
    }
    
    // Send items to client
    player.getPacketSender().sendItemsOnInterface(3900, TotalCount, id, count);
    
    // Update currency display
    updateCurrencyDisplay(shopId);
    
    // Mark shop as updated
    player.updateShop = true;
}
```

### Transaction Processing

#### `buyItem(int itemID, int fromSlot, int amount)`
Handles player purchasing items from a shop:

```java
public boolean buyItem(int itemID, int fromSlot, int amount) {
    int shopId = player.shopId;
    
    // Validate shop and item
    if (shopId == -1 || amount <= 0) {
        return false;
    }
    
    // Check if item exists in shop
    if (fromSlot >= ShopHandler.MAX_SHOP_ITEMS || ShopHandler.shopItems[shopId][fromSlot] - 1 != itemID) {
        return false;
    }
    
    // Check if shop has stock
    if (ShopHandler.shopItemsN[shopId][fromSlot] <= 0) {
        player.getPacketSender().sendMessage("This item is out of stock.");
        return false;
    }
    
    // Limit purchase amount to available stock
    if (amount > ShopHandler.shopItemsN[shopId][fromSlot]) {
        amount = ShopHandler.shopItemsN[shopId][fromSlot];
    }
    
    // Check if player has inventory space
    if (!player.getItemAssistant().playerHasItem(995, getBuyPrice(itemID, shopId) * amount) && 
        !isGeneralStore(shopId)) {
        player.getPacketSender().sendMessage("You don't have enough coins.");
        return false;
    }
    
    // Check inventory space
    int freeSlots = player.getItemAssistant().freeSlots();
    if (freeSlots < amount && !game.items.Item.itemStackable[itemID]) {
        amount = freeSlots;
        player.getPacketSender().sendMessage("You don't have enough inventory space.");
    }
    
    // Process transaction
    int totalPrice = getBuyPrice(itemID, shopId) * amount;
    if (player.getItemAssistant().playerHasItem(995, totalPrice)) {
        player.getItemAssistant().deleteItem(995, totalPrice);
        player.getItemAssistant().addItem(itemID, amount);
        
        // Update shop stock
        ShopHandler.shopItemsN[shopId][fromSlot] -= amount;
        if (ShopHandler.shopItemsN[shopId][fromSlot] <= 0 && 
            ShopHandler.shopItemsStandard[shopId] <= fromSlot) {
            ShopHandler.shopItems[shopId][fromSlot] = 0;
        }
        
        // Refresh shop for all viewers
        ShopHandler.refreshshop(shopId);
        return true;
    }
    
    return false;
}
```

#### `sellItem(int itemID, int fromSlot, int amount)`
Handles player selling items to a shop:

```java
public boolean sellItem(int itemID, int fromSlot, int amount) {
    int shopId = player.shopId;
    
    // Validate shop and item
    if (shopId == -1 || amount <= 0) {
        return false;
    }
    
    // Check if shop buys items
    if (ShopHandler.shopBModifier[shopId] == 0) {
        player.getPacketSender().sendMessage("You can't sell items to this shop.");
        return false;
    }
    
    // Check if item is tradeable
    if (!player.getItemAssistant().tradeable(itemID)) {
        player.getPacketSender().sendMessage("You can't sell this item.");
        return false;
    }
    
    // Check if player has the item
    if (!player.getItemAssistant().playerHasItem(itemID, amount)) {
        amount = player.getItemAssistant().getItemAmount(itemID);
    }
    
    // Find or create slot in shop
    int slot = -1;
    for (int i = 0; i < ShopHandler.MAX_SHOP_ITEMS; i++) {
        if (ShopHandler.shopItems[shopId][i] - 1 == itemID) {
            slot = i;
            break;
        }
    }
    
    // If item doesn't exist in shop, find empty slot
    if (slot == -1) {
        for (int i = 0; i < ShopHandler.MAX_SHOP_ITEMS; i++) {
            if (ShopHandler.shopItems[shopId][i] == 0) {
                slot = i;
                break;
            }
        }
    }
    
    // Check if shop is full
    if (slot == -1) {
        player.getPacketSender().sendMessage("The shop is currently full.");
        return false;
    }
    
    // Process transaction
    int totalPrice = getSellValue(itemID, shopId) * amount;
    player.getItemAssistant().deleteItem(itemID, amount);
    player.getItemAssistant().addItem(995, totalPrice);
    
    // Update shop inventory
    if (ShopHandler.shopItems[shopId][slot] == 0) {
        ShopHandler.shopItems[shopId][slot] = itemID + 1;
    }
    ShopHandler.shopItemsN[shopId][slot] += amount;
    
    // Refresh shop for all viewers
    ShopHandler.refreshshop(shopId);
    return true;
}
```

### Price Calculation

#### `getBuyValue(int itemId, int shopId)`
Calculates the price to buy an item from a shop:

```java
public int getBuyValue(int itemId, int shopId) {
    // Get base item value
    int baseValue = getItemShopValue(itemId, shopId, false);
    
    // Apply shop modifier
    if (baseValue > 0) {
        return (int) Math.ceil(baseValue * (ShopHandler.shopBModifier[shopId] / 100.0));
    }
    
    return 0;
}
```

#### `getSellValue(int itemId, int shopId)`
Calculates the price when selling an item to a shop:

```java
public int getSellValue(int itemId, int shopId) {
    // Get base item value
    int baseValue = getItemShopValue(itemId, shopId, true);
    
    // Apply shop modifier
    if (baseValue > 0) {
        return (int) Math.floor(baseValue * (ShopHandler.shopSModifier[shopId] / 100.0));
    }
    
    return 1; // Minimum value
}
```

## Usage Examples

### Opening a Shop
```java
// Open the general store
player.getShopAssistant().openShop(1);

// Open a specialty shop
player.getShopAssistant().openShop(MAGIC_SHOP);

// Open a player-owned shop
player.getShopAssistant().openShop(playerShopId);
```

### Buying Items
```java
// Buy 10 items from slot 0
player.getShopAssistant().buyItem(itemId, 0, 10);

// Check price before buying
int price = player.getShopAssistant().getBuyValue(itemId, shopId);
player.getPacketSender().sendMessage("This item costs " + price + " coins.");
```

### Selling Items
```java
// Sell 5 items from inventory slot 3
player.getShopAssistant().sellItem(itemId, 3, 5);

// Check if shop buys the item
if (player.getShopAssistant().shopBuysItem(shopId, itemId)) {
    // Shop will buy this item
    int value = player.getShopAssistant().getSellValue(itemId, shopId);
    player.getPacketSender().sendMessage("You can sell this for " + value + " coins.");
}
```

## Best Practices

1. **Always validate shop IDs** before performing operations
2. **Check item tradeability** before allowing sales
3. **Verify player has sufficient funds** before processing purchases
4. **Handle inventory space constraints** appropriately
5. **Use appropriate currency types** for different shops
6. **Refresh shop for all viewers** after modifications
7. **Implement proper error messages** for failed transactions

## Related Classes

- [`ShopHandler`](ShopHandler.md) - Manages global shop state
- [`ItemAssistant`](ItemAssistant.md) - Handles player inventory
- [`game.entities.Player`](game.entities.Player.md) - Contains ShopAssistant instance
- [`PacketSender`](PacketSender.md) - Sends shop interfaces to client
- [`ItemDefinition`](ItemDefinition.md) - Provides item base values