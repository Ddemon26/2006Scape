# PlayerHandler

**Package:** `com.rs2.game.players`  
**Source:** [`2006Scape Server/src/main/java/com/rs2/game/players/PlayerHandler.java`](2006Scape Server/src/main/java/com/rs2/game/players/PlayerHandler.java)

## Overview

The `PlayerHandler` class is the central management system for all connected players in the 2006Scape server. It maintains the global player list, handles player connections and disconnections, processes player updates, and coordinates player-related operations across the server. This class is called every game tick by the [`GameEngine`](GameEngine.md) to process all active players and manage their states.

## Key Responsibilities

- **Player Management**: Maintaining the global array of connected players
- **Connection Handling**: Managing new player connections and disconnections
- **Player Processing**: Coordinating player updates, movement, and actions
- **State Management**: Tracking player counts, names, and online status
- **Cleanup Operations**: Handling logout procedures and resource cleanup
- **Update Coordination**: Managing player and NPC update packets
- **Server Updates**: Coordinating server restarts and player kicks

## Core Data Structures

### Player Arrays and Counters
```java
public static Player players[] = new Player[Constants.MAX_PLAYERS];
public static int playerCount = 0;
public static int playerShopCount = 0;
public static String playersCurrentlyOn[] = new String[Constants.MAX_PLAYERS];
```

### Update Management
```java
public static boolean updateAnnounced;
public static boolean updateRunning;
public static int updateSeconds;
public static long updateStartTime;
private boolean kickAllPlayers = false;
```

## Core Methods

### Player Connection Management

#### `newPlayerClient(Client client)`
Handles new player connections by finding an available slot:

```java
public boolean newPlayerClient(Client client) {
    int slot = -1;
    
    // Find available player slot
    for (int i = 1; i < Constants.MAX_PLAYERS; i++) {
        if (players[i] == null || players[i].disconnected) {
            slot = i;
            break;
        }
    }
    
    // Check if server is full
    if (slot == -1) {
        return false;
    }
    
    // Initialize player in slot
    client.handler = this;
    client.playerId = slot;
    players[slot] = client;
    players[slot].isActive = true;
    
    // Set connection information
    players[slot].connectedFrom = client.isBot ? "127.0.0.1" : 
        ((InetSocketAddress) client.getSession().getRemoteAddress())
        .getAddress().getHostAddress();
    
    if (Constants.SERVER_DEBUG) {
        System.out.println("Player assigned to slot " + slot);
    }
    
    return true;
}
```

**Returns:** `true` if player was successfully added, `false` if server is full

#### `removePlayer(Player player)`
Handles player disconnection and cleanup:

```java
public void removePlayer(Player player) {
    if (player == null) return;
    
    // Handle ongoing activities
    if (player.inTrade) {
        Client otherPlayer = (Client) players[player.tradeWith];
        if (otherPlayer != null) {
            otherPlayer.getTrading().declineTrade();
        }
    }
    
    // Handle dueling
    if (player.duelStatus >= 1 && player.duelStatus <= 4) {
        Client opponent = (Client) players[player.duelingWith];
        if (opponent != null) {
            opponent.getDueling().declineDuel();
        }
    } else if (player.duelStatus == 5) {
        Client opponent = (Client) players[player.duelingWith];
        if (opponent != null) {
            opponent.getDueling().duelVictory();
        }
    }
    
    // Handle minigames
    if (GameEngine.trawler.players.contains(player)) {
        GameEngine.trawler.players.remove(player);
    }
    
    // Handle pets/summons
    if (player.hasNpc) {
        Client client = (Client) player;
        client.getSummon().quickPickup(client, player.summonId);
    }
    
    // Save player data
    Client client = (Client) player;
    if (PlayerSave.saveGame(client)) {
        System.out.println("Game saved for player " + player.playerName);
    } else {
        System.out.println("Could not save for " + player.playerName);
    }
    
    // Update last known position
    player.lastX = player.absX;
    player.lastY = player.absY;
    player.lastH = player.heightLevel;
}
```

### Player Information Management

#### `updatePlayerNames()`
Updates the global player name list and counts:

```java
public void updatePlayerNames() {
    playerShopCount = 0;
    playerCount = 0;
    
    for (int i = 0; i < Constants.MAX_PLAYERS; i++) {
        if (players[i] != null) {
            playersCurrentlyOn[i] = players[i].playerName;
            
            if (players[i].isBot) {
                playerShopCount++;
            } else {
                playerCount++;
            }
        } else {
            playersCurrentlyOn[i] = "";
        }
    }
}
```

#### Player Lookup Methods

```java
public static int getPlayerID(String playerName) {
    for (int i = 0; i < players.length; i++) {
        if (playersCurrentlyOn[i] != null && 
            playersCurrentlyOn[i].equalsIgnoreCase(playerName)) {
            return i;
        }
    }
    return -1;
}

public static boolean isPlayerOn(String playerName) {
    for (int i = 0; i < players.length; i++) {
        if (playersCurrentlyOn[i] != null && 
            playersCurrentlyOn[i].equalsIgnoreCase(playerName)) {
            return true;
        }
    }
    return false;
}

public static int getPlayerCount() {
    return playerCount;
}

public static int getNonPlayerCount() {
    int count = 0;
    for (int i = 0; i < Constants.MAX_PLAYERS; i++) {
        if (players[i] != null && players[i].playerRights >= 1) {
            count++;
        }
    }
    return count;
}

public static int getPlayerShopCount() {
    return playerShopCount;
}
```

### Main Processing Loop

#### `process()`
Main processing method called every game tick:

```java
public void process() {
    // Update player names and counts
    updatePlayerNames();
    
    // Handle server updates/kicks
    if (kickAllPlayers) {
        for (int i = 0; i < players.length; i++) {
            if (players[i] != null) {
                players[i].disconnected = true;
            }
        }
        if (updateRunning) {
            GameEngine.shutdownServer = true;
        }
    }
    
    // First pass: Process disconnections and basic player processing
    for (int i = 0; i < players.length; i++) {
        if (players[i] == null || !players[i].isActive) {
            continue;
        }
        
        try {
            Client client = (Client) players[i];
            
            // Handle disconnected players
            if (players[i].disconnected) {
                handleDisconnection(client, i);
                continue;
            }
            
            // Process active players
            players[i].processQueuedPackets();
            players[i].process();
            players[i].postProcessing();
            players[i].getNextPlayerMovement();
            players[i].preProcessing();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Second pass: Handle updates and final cleanup
    for (int i = 0; i < players.length; i++) {
        if (players[i] == null || !players[i].isActive) {
            continue;
        }
        
        try {
            if (players[i].disconnected) {
                // Final disconnection handling
                finalizeDisconnection(i);
            } else {
                // Handle player updates
                if (!players[i].initialized) {
                    players[i].getPacketSender().loginPlayer();
                    players[i].initialized = true;
                } else {
                    players[i].update();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Handle server update announcements
    if (updateRunning && !updateAnnounced) {
        updateAnnounced = true;
        GameEngine.updateServer = true;
    }
    
    // Check if it's time to kick all players for update
    if (updateRunning && 
        System.currentTimeMillis() - updateStartTime > updateSeconds * 1000) {
        kickAllPlayers = true;
    }
    
    // Final pass: Clear update flags
    for (int i = 0; i < players.length; i++) {
        if (players[i] == null || !players[i].isActive) {
            continue;
        }
        
        try {
            players[i].clearUpdateFlags();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### Update System Management

#### Player Update Processing
```java
public void updatePlayer(Player player, Stream stream) {
    // Handle player movement and appearance updates
    // Synchronize player positions with other players
    // Send appearance changes to nearby players
    // Process player animations and graphics
}
```

#### NPC Update Processing
```java
public void updateNPC(Player player, Stream stream) {
    // Update NPC list for the player
    // Handle NPC movement and animations
    // Add/remove NPCs from player's view
    // Send NPC update packets to client
    
    updateBlock.currentOffset = 0;
    if (stream != null) {
        stream.createFrameVarSizeWord(65);
        stream.initBitAccess();
        stream.writeBits(8, player.npcListSize);
    }
    
    int size = player.npcListSize;
    player.npcListSize = 0;
    
    // Process existing NPCs in player's list
    for (int i = 0; i < size; i++) {
        if (!player.rebuildNPCList && player.withinDistance(player.npcList[i])) {
            // Update NPC movement and add to update block
            player.npcList[i].updateNPCMovement(stream);
            player.npcList[i].appendNPCUpdateBlock(updateBlock);
            player.npcList[player.npcListSize++] = player.npcList[i];
        } else {
            // Remove NPC from player's list
            int npcId = player.npcList[i].npcId;
            stream.writeBits(1, 1);
            stream.writeBits(2, 3);
        }
    }
    
    // Add new NPCs to player's list
    addNewNPCs(player, stream);
    
    // Finalize update packet
    if (updateBlock.currentOffset > 0) {
        stream.writeBits(14, 16383);
        stream.finishBitAccess();
        stream.writeBytes(updateBlock.buffer, updateBlock.currentOffset, 0);
    } else {
        stream.finishBitAccess();
    }
    
    if (stream != null) {
        stream.endFrameVarSizeWord();
    }
}
```

### Server Update Management

#### `announceUpdate(int seconds)`
Announces a server update to all players:

```java
public void announceUpdate(int seconds) {
    updateSeconds = seconds;
    updateStartTime = System.currentTimeMillis();
    updateRunning = true;
    updateAnnounced = false;
    
    // Notify all players
    for (int i = 0; i < players.length; i++) {
        if (players[i] != null && !players[i].disconnected) {
            players[i].getPacketSender().sendMessage("System update in " + seconds + " seconds.");
            players[i].getPacketSender().sendSystemUpdate(seconds);
        }
    }
}
```

#### `kickAllPlayers()`
Initiates disconnection of all players:

```java
public void kickAllPlayers() {
    kickAllPlayers = true;
    
    for (int i = 0; i < players.length; i++) {
        if (players[i] != null) {
            players[i].disconnected = true;
            players[i].properLogout = true;
        }
    }
}
```

## Usage Examples

### Finding Players
```java
// Check if a player is online
if (PlayerHandler.isPlayerOn("PlayerName")) {
    int playerId = PlayerHandler.getPlayerID("PlayerName");
    Player player = PlayerHandler.players[playerId];
    // Interact with player
}

// Get current player count
int onlinePlayers = PlayerHandler.getPlayerCount();
int totalPlayers = PlayerHandler.getPlayerCount() + PlayerHandler.getPlayerShopCount();
```

### Iterating Through Players
```java
// Process all online players
for (int i = 0; i < PlayerHandler.players.length; i++) {
    if (PlayerHandler.players[i] != null && !PlayerHandler.players[i].disconnected) {
        Player player = PlayerHandler.players[i];
        // Process player
    }
}

// Send message to all players
for (Player player : PlayerHandler.players) {
    if (player != null && !player.disconnected) {
        player.getPacketSender().sendMessage("Server announcement!");
    }
}
```

### Server Management
```java
// Announce server update
PlayerHandler handler = new PlayerHandler();
handler.announceUpdate(60); // 60 second warning

// Get server statistics
int regularPlayers = PlayerHandler.getPlayerCount();
int staffMembers = PlayerHandler.getNonPlayerCount();
int playerShops = PlayerHandler.getPlayerShopCount();
```

## Performance Considerations

### Optimization Strategies
- **Efficient Iteration**: Skip null and inactive players during processing
- **Batch Operations**: Group similar operations together
- **Exception Handling**: Isolate player errors to prevent server crashes
- **Memory Management**: Properly clean up disconnected players

### Resource Management
- **Player Limits**: Enforce maximum player counts
- **Connection Tracking**: Monitor connection sources and limits
- **Update Coordination**: Efficiently manage player and NPC updates

## Best Practices

1. **Always check for null players** before processing
2. **Handle disconnections gracefully** with proper cleanup
3. **Use appropriate synchronization** for thread safety
4. **Monitor player counts** and server capacity
5. **Implement proper error handling** to prevent crashes
6. **Log important player events** for debugging
7. **Coordinate updates efficiently** to minimize bandwidth

## Integration Points

### GameEngine Integration
```java
// Called every game tick
GameEngine.playerHandler.process();
```

### Player Lifecycle
```java
// New connection
playerHandler.newPlayerClient(client);

// Player processing
player.process();
player.update();

// Disconnection
playerHandler.removePlayer(player);
```

### Update System
```java
// Player updates
playerHandler.updatePlayer(player, stream);

// NPC updates
playerHandler.updateNPC(player, stream);
```

## Related Classes

- [`Player`](Player.md) - Individual player instance
- [`Client`](Client.md) - Concrete player with network session
- [`GameEngine`](GameEngine.md) - Calls PlayerHandler.process() every tick
- [`PlayerSave`](PlayerSave.md) - Handles player data persistence
- [`Stream`](Stream.md) - Network packet handling
- [`NpcHandler`](NpcHandler.md) - NPC management counterpart