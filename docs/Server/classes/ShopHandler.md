# ShopHandler

**Package:** `com.rs2.game.shops`  
**Source:** [`2006Scape Server/src/main/java/com/rs2/game/shops/ShopHandler.java`](2006Scape Server/src/main/java/com/rs2/game/shops/ShopHandler.java)

## Overview

The `ShopHandler` class manages the entire shop system for the 2006Scape server, including both game.entities.NPC shops and player-owned shops. It handles shop inventory management, restocking mechanics, price calculations, and player interactions with shops. This system supports dynamic inventory changes, automatic restocking, and real-time updates for all players viewing a shop.

## Key Responsibilities

- **Shop Management**: Loading, creating, and managing shop configurations
- **Inventory Control**: Tracking stock levels, restocking items, and managing shop inventories
- **game.entities.Player Shops**: Supporting player-owned shops with custom inventories
- **Real-time Updates**: Synchronizing shop changes across all viewing players
- **Economic Balance**: Managing buy/sell prices and stock limitations
- **Data Persistence**: Loading shop configurations from JSON files

## Core Architecture

### Shop Data Structure
```java
public static int MAX_SHOPS = 800;              // Maximum number of shops
public static int MAX_SHOP_ITEMS = 40;          // Items per shop
public static int[][] shopItems = new int[MAX_SHOPS][MAX_SHOP_ITEMS];      // game.items.Item IDs
public static int[][] shopItemsN = new int[MAX_SHOPS][MAX_SHOP_ITEMS];     // Stock amounts
public static int[][] shopItemsDelay = new int[MAX_SHOPS][MAX_SHOP_ITEMS]; // Restock timers
public static int[][] shopItemsSN = new int[MAX_SHOPS][MAX_SHOP_ITEMS];    // Standard stock
public static String[] shopName = new String[MAX_SHOPS];                   // Shop names
public static int[] shopSModifier = new int[MAX_SHOPS];                    // Sell price modifier
public static int[] shopBModifier = new int[MAX_SHOPS];                    // Buy price modifier
```

### Timing Constants
```java
public static int SHOW_DELAY = 1;      // Restock 1 item every tick
public static int SPECIAL_DELAY = 60;  // Remove overstocked items after 60 ticks
```

## Core Methods

### Initialization and Loading

#### `ShopHandler()`
Initializes the shop system and loads shop configurations:

```java
public ShopHandler() {
    // Initialize all shop arrays
    for (int i = 0; i < MAX_SHOPS; i++) {
        for (int j = 0; j < MAX_SHOP_ITEMS; j++) {
            ResetItem(i, j);
            shopItemsSN[i][j] = 0;
        }
        shopItemsStandard[i] = 0;
        shopSModifier[i] = 0;
        shopBModifier[i] = 0;
        shopName[i] = "";
    }
    
    totalshops = 0;
    loadShops(); // Load from configuration file
}
```

#### `loadShops()`
Loads shop configurations from JSON file:

```java
public void loadShops() {
    Gson gson = new Gson();
    
    try {
        Type collectionType = new TypeToken<ShopData[]>() {}.getType();
        ShopData[] data = gson.fromJson(new FileReader("./data/cfg/shops.json"), collectionType);
        
        for (ShopData shop : data) {
            int shopID = shop.getId();
            shopName[shopID] = shop.getName();
            shopSModifier[shopID] = shop.getSellModifier();
            shopBModifier[shopID] = shop.getBuyModifier();
            
            // Load shop items
            for (int i = 0; i < shop.getItems().length; i++) {
                if (shop.getItems()[i].getItemId() > 0) {
                    shopItems[shopID][i] = shop.getItems()[i].getItemId() + 1; // +1 for storage format
                    shopItemsN[shopID][i] = shop.getItems()[i].getItemAmount();
                    shopItemsSN[shopID][i] = shop.getItems()[i].getItemAmount(); // Standard stock
                    shopItemsStandard[shopID]++;
                } else {
                    break;
                }
            }
            totalshops++;
        }
    } catch (FileNotFoundException e) {
        System.out.println("shops.json: file not found.");
    }
}
```

### Shop Processing and Restocking

#### `process()`
Main processing method called every game tick to handle restocking:

```java
public void process() {
    boolean didUpdate = false;
    
    for (int shopId = 1; shopId <= totalshops; shopId++) {
        // Skip shops without price modifiers
        if (shopBModifier[shopId] == 0 || shopSModifier[shopId] == 0) {
            continue;
        }
        
        for (int itemSlot = 0; itemSlot < MAX_SHOP_ITEMS; itemSlot++) {
            if (shopItems[shopId][itemSlot] > 0) {
                if (shopItemsDelay[shopId][itemSlot] >= SHOW_DELAY) {
                    
                    // Handle restocking for standard items
                    if (itemSlot <= shopItemsStandard[shopId] && 
                        shopItemsN[shopId][itemSlot] <= shopItemsSN[shopId][itemSlot]) {
                        
                        if (shopItemsN[shopId][itemSlot] < shopItemsSN[shopId][itemSlot] && 
                            System.currentTimeMillis() - shopItemsRestock[shopId][itemSlot] > 
                            restockTimeItem(shopItems[shopId][itemSlot])) {
                            
                            // Restock one item
                            shopItemsN[shopId][itemSlot] += 1;
                            shopItemsDelay[shopId][itemSlot] = 0;
                            didUpdate = true;
                            shopItemsRestock[shopId][itemSlot] = System.currentTimeMillis();
                        }
                    }
                    // Handle removal of overstocked items (player-sold items)
                    else if (shopItemsDelay[shopId][itemSlot] >= SPECIAL_DELAY) {
                        DiscountItem(shopId, itemSlot);
                        shopItemsDelay[shopId][itemSlot] = 0;
                        didUpdate = true;
                    }
                    
                    if (didUpdate) {
                        refreshshop(shopId);
                    }
                }
                shopItemsDelay[shopId][itemSlot]++;
            }
        }
    }
}
```

#### `restockTimeItem(int itemId)`
Determines restock time for specific items:

```java
public static int restockTimeItem(int itemId) {
    switch (itemId) {
        case 995:  // Coins - fast restock
            return 100;
        case 4151: // Whip - slow restock
            return 30000;
        default:
            return 1000; // Default 1 second
    }
}
```

### Shop Inventory Management

#### `buyItem(int shopId, int itemId, int amount)`
Handles item purchases from shops:

```java
public static void buyItem(int shopId, int itemId, int amount) {
    itemId++; // Convert to storage format
    
    for (int slot = 0; slot < MAX_SHOP_ITEMS; slot++) {
        if (shopItems[shopId][slot] == itemId) {
            shopItemsN[shopId][slot] -= amount;
            
            // Ensure stock doesn't go negative
            if (shopItemsN[shopId][slot] < 0) {
                shopItemsN[shopId][slot] = 0;
            }
            break;
        }
    }
    
    refreshshop(shopId);
}
```

#### `getStock(int shopId, int itemId)`
Returns current stock level for an item:

```java
public static int getStock(int shopId, int itemId) {
    itemId++; // Convert to storage format
    
    for (int slot = 0; slot < MAX_SHOP_ITEMS; slot++) {
        if (shopItems[shopId][slot] == itemId) {
            return shopItemsN[shopId][slot];
        }
    }
    return -1; // game.items.Item not found
}
```

#### `refreshshop(int shopId)`
Updates shop display for all viewing players:

```java
public static void refreshshop(int shopId) {
    // Clean up empty slots (except standard items)
    for (int slot = shopItemsStandard[shopId]; slot < MAX_SHOP_ITEMS; slot++) {
        if (shopItemsN[shopId][slot] <= 0) {
            ResetItem(shopId, slot);
            
            // Shift items up to fill gaps
            int nextSlot = slot + 1;
            if (nextSlot < MAX_SHOP_ITEMS && shopItemsN[shopId][nextSlot] > 0) {
                shopItems[shopId][slot] = shopItems[shopId][nextSlot];
                shopItemsN[shopId][slot] = shopItemsN[shopId][nextSlot];
                shopItemsDelay[shopId][slot] = shopItemsDelay[shopId][nextSlot];
                ResetItem(shopId, nextSlot);
            }
        }
    }
    
    // Update all players viewing this shop
    for (int playerId = 1; playerId < PlayerHandler.players.length; playerId++) {
        if (PlayerHandler.players[playerId] != null) {
            game.entities.Player player = PlayerHandler.players[playerId];
            if (player.isShopping && player.shopId == shopId) {
                player.updateShop = true;
                player.updateShop(shopId);
            }
        }
    }
}
```

### game.entities.Player-Owned Shops

#### `createPlayerShop(Client player)`
Creates a player-owned shop using their bank items:

```java
public static void createPlayerShop(Client player) {
    int shopId = getEmptyshop();
    if (shopId == -1) {
        player.getPacketSender().sendMessage("No shop slots available.");
        return;
    }
    
    player.shopId = shopId;
    shopSModifier[shopId] = 0;  // game.entities.Player shops don't buy items
    shopBModifier[shopId] = 0;  // Custom pricing
    shopName[shopId] = player.properName + "'s Store";
    
    // Copy bank items to shop
    for (int i = 0; i < MAX_SHOP_ITEMS; i++) {
        shopItems[shopId][i] = player.bankItems[i];
        shopItemsN[shopId][i] = player.bankItemsN[i];
        shopItemsSN[shopId][i] = 0; // No restocking for player shops
        shopItemsDelay[shopId][i] = 0;
    }
    
    totalshops++;
}
```

#### `closePlayerShop(Client player)`
Closes a player-owned shop:

```java
public static void closePlayerShop(Client player) {
    for (int shopId = getEmptyshop(); shopId >= 0; shopId--) {
        if (shopName[shopId].equals(player.properName + "'s Store")) {
            // Clear shop data
            for (int i = 0; i < MAX_SHOP_ITEMS; i++) {
                shopItems[shopId][i] = 0;
                shopItemsN[shopId][i] = 0;
                shopItemsSN[shopId][i] = 0;
                shopItemsDelay[shopId][i] = 0;
            }
            
            shopName[shopId] = "";
            refreshshop(shopId);
            totalshops--;
            break;
        }
    }
}
```

### Utility Methods

#### `ResetItem(int shopId, int slot)`
Clears a shop item slot:

```java
private static void ResetItem(int shopId, int slot) {
    // Don't reset standard shop items
    if (shopItemsStandard[shopId] > slot) {
        return;
    }
    
    shopItems[shopId][slot] = 0;
    shopItemsN[shopId][slot] = 0;
    shopItemsDelay[shopId][slot] = 0;
}
```

#### `DiscountItem(int shopId, int slot)`
Reduces stock of overstocked items:

```java
private void DiscountItem(int shopId, int slot) {
    shopItemsN[shopId][slot] -= 1;
    
    if (shopItemsN[shopId][slot] <= 0) {
        shopItemsN[shopId][slot] = 0;
        
        // Remove non-standard items completely
        if (shopItemsStandard[shopId] <= slot) {
            ResetItem(shopId, slot);
        }
    }
}
```

#### `getEmptyshop()`
Finds an available shop slot:

```java
private static int getEmptyshop() {
    for (int i = 0; i < MAX_SHOPS; i++) {
        if (shopName[i].equals("")) {
            return i;
        }
    }
    return -1; // No empty slots
}
```

## Shop Configuration Format

### JSON Structure
```json
[
  {
    "id": 1,
    "name": "General Store",
    "sellModifier": 40,
    "buyModifier": 60,
    "items": [
      {
        "itemId": 1931,
        "itemAmount": 10
      },
      {
        "itemId": 1925,
        "itemAmount": 5
      }
    ]
  }
]
```

### Configuration Fields
- **id**: Unique shop identifier
- **name**: Display name for the shop
- **sellModifier**: Percentage of item value when selling to shop
- **buyModifier**: Percentage of item value when buying from shop
- **items**: Array of items with ID and stock amount

## Usage Examples

### Basic Shop Operations
```java
// Check if item is in stock
int stock = ShopHandler.getStock(shopId, itemId);
if (stock > 0) {
    // game.items.Item is available
}

// Purchase items from shop
ShopHandler.buyItem(shopId, itemId, amount);

// Refresh shop display
ShopHandler.refreshshop(shopId);
```

### game.entities.Player Shop Management
```java
// Create player shop
ShopHandler.createPlayerShop(player);

// Close player shop
ShopHandler.closePlayerShop(player);

// Check if player owns shop
if (ShopHandler.playerOwnsStore(shopId, player)) {
    // game.entities.Player can manage this shop
}
```

### Shop Processing
```java
// Called every game tick by GameEngine
shopHandler.process();

// Get restock time for specific item
int restockTime = ShopHandler.restockTimeItem(itemId);
```

## Economic Mechanics

### Price Calculation
Shop prices are calculated using modifiers:
- **Selling to shop**: `baseValue * (sellModifier / 100)`
- **Buying from shop**: `baseValue * (buyModifier / 100)`

### Stock Management
- **Standard Items**: Automatically restock to original amounts
- **game.entities.Player-Sold Items**: Gradually removed from shop over time
- **Overstocking**: Items sold by players are slowly discounted

### Restocking System
- Items restock one at a time based on `restockTimeItem()`
- Different items have different restock rates
- Rare items restock slower than common items

## Performance Considerations

### Optimization Strategies
- **Efficient Loops**: Only process active shops with valid modifiers
- **Batch Updates**: Group shop refreshes together
- **Memory Management**: Clean up empty shop slots regularly
- **game.entities.Player Filtering**: Only update players actually viewing shops

### Scalability
- **Shop Limits**: Maximum of 800 shops supported
- **game.items.Item Limits**: 40 items per shop maximum
- **Update Frequency**: Process every game tick for responsive restocking

## Best Practices

1. **Always validate shop IDs** before performing operations
2. **Check stock levels** before allowing purchases
3. **Handle edge cases** for empty shops and invalid items
4. **Implement proper error handling** for file loading
5. **Use appropriate restock times** for different item types
6. **Monitor shop performance** to prevent lag
7. **Backup shop configurations** regularly

## Integration Points

### GameEngine Integration
```java
// Called every game tick
GameEngine.shopHandler.process();
```

### game.entities.Player Integration
```java
// When player opens shop
player.isShopping = true;
player.shopId = shopId;
player.updateShop(shopId);

// When player closes shop
player.isShopping = false;
player.shopId = -1;
```

### Economy Integration
```java
// Price calculations use ShopAssistant
int buyPrice = player.getShopAssistant().getBuyPrice(itemId, shopId);
int sellPrice = player.getShopAssistant().getSellPrice(itemId, shopId);
```

## Related Classes

- [`ShopAssistant`](ShopAssistant.md) - game.entities.Player-specific shop interactions
- [`ShopData`](ShopData.md) - Shop configuration data structure
- [`GameEngine`](GameEngine.md) - Calls ShopHandler.process() every tick
- [`game.entities.Player`](game.entities.Player.md) - Contains shop state and interactions
- [`ItemAssistant`](ItemAssistant.md) - Handles item transactions
