# PlayerHandler

**Package:** `com.rs2.game.players`  
**Source:** [`2006Scape Server/src/main/java/com/rs2/game/players/PlayerHandler.java`](2006Scape Server/src/main/java/com/rs2/game/players/PlayerHandler.java)

## Overview

The `PlayerHandler` class is the central management system for all player connections and operations in the 2006Scape server. It maintains the master player list, handles player lifecycle management, processes player updates, and manages the synchronization between players and NPCs. This class is called every game tick by the [`GameEngine`](GameEngine.md) to process all connected players.

## Key Responsibilities

- **Player Registration**: Adding new players to the server
- **Player Processing**: Executing player logic every game tick
- **Update Management**: Synchronizing player and NPC states
- **Connection Management**: Handling disconnections and cleanup
- **Server Updates**: Managing planned server restarts
- **Player Lookup**: Finding players by name or ID

## Core Data Structures

### Player Array
```java
public static Player players[] = new Player[Constants.MAX_PLAYERS];
```
The main array holding all connected players. Index 0 is typically reserved, with player slots starting from index 1.

### Player Tracking
```java
public static int playerCount = 0;           // Real players online
public static int playerShopCount = 0;       // Bot accounts online
public static String playersCurrentlyOn[] = new String[Constants.MAX_PLAYERS];
```

### Update System
```java
public static boolean updateAnnounced;       // Update notification sent
public static boolean updateRunning;         // Update in progress
public static int updateSeconds;             // Countdown timer
public static long updateStartTime;          // Update start time
```

## Core Methods

### Player Registration

#### `newPlayerClient(Client client)`
Registers a new player connection to the server:

```java
public boolean newPlayerClient(Client client) {
    // Find available slot
    int slot = -1;
    for (int i = 1; i < Constants.MAX_PLAYERS; i++) {
        if (players[i] == null || players[i].disconnected) {
            slot = i;
            break;
        }
    }
    
    if (slot == -1) {
        return false; // Server full
    }
    
    // Register player
    client.handler = this;
    client.playerId = slot;
    players[slot] = client;
    players[slot].isActive = true;
    
    return true;
}
```

**Returns:** `true` if player was successfully registered, `false` if server is full

### Player Information

#### `getPlayerCount()`
Returns the number of real players online (excludes bots):
```java
public static int getPlayerCount() {
    return playerCount;
}
```

#### `getNonPlayerCount()`
Returns the number of staff members online:
```java
public static int getNonPlayerCount() {
    int count = 0;
    for (int i = 0; i < Constants.MAX_PLAYERS; i++) {
        if (players[i] != null && players[i].playerRights >= 1) {
            count++;
        }
    }
    return count;
}
```

#### `getPlayerShopCount()`
Returns the number of bot accounts online:
```java
public static int getPlayerShopCount() {
    return playerShopCount;
}
```

### Player Lookup

#### `getPlayerID(String playerName)`
Finds a player's ID by their username:
```java
public static int getPlayerID(String playerName) {
    for (int i = 0; i < players.length; i++) {
        if (playersCurrentlyOn[i] != null && 
            playersCurrentlyOn[i].equalsIgnoreCase(playerName)) {
            return i;
        }
    }
    return -1; // Player not found
}
```

#### `isPlayerOn(String playerName)`
Checks if a player is currently online:
```java
public static boolean isPlayerOn(String playerName) {
    for (int i = 0; i < players.length; i++) {
        if (playersCurrentlyOn[i] != null && 
            playersCurrentlyOn[i].equalsIgnoreCase(playerName)) {
            return true;
        }
    }
    return false;
}
```

## Main Processing Loop

### `process()`
The main processing method called every game tick:

```java
public void process() {
    updatePlayerNames(); // Update player counts and names
    
    // Handle server shutdown
    if (kickAllPlayers) {
        for (Player player : players) {
            if (player != null) {
                player.disconnected = true;
            }
        }
    }
    
    // Process each player
    for (int i = 0; i < players.length; i++) {
        if (players[i] == null || !players[i].isActive) {
            continue;
        }
        
        try {
            Client client = (Client) players[i];
            
            // Handle disconnected players
            if (players[i].disconnected) {
                handleDisconnection(client);
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
    
    // Send updates to all players
    for (Player player : players) {
        if (player != null && player.isActive) {
            if (!player.initialized) {
                player.getPacketSender().loginPlayer();
                player.initialized = true;
            } else {
                player.update();
            }
        }
    }
    
    // Clear update flags
    for (Player player : players) {
        if (player != null && player.isActive) {
            player.clearUpdateFlags();
        }
    }
}
```

### Player Processing Phases

1. **Name Updates**: Update player counts and online lists
2. **Disconnection Handling**: Process players leaving the server
3. **Packet Processing**: Handle incoming packets from clients
4. **Game Logic**: Execute player-specific game logic
5. **Movement Processing**: Handle player movement and pathfinding
6. **Update Synchronization**: Send world updates to clients
7. **Cleanup**: Clear temporary flags and data

## Update System Management

### Player Updates
The `updatePlayer()` method synchronizes player states:

```java
public void updatePlayer(Player player, Stream outStream) {
    // Handle server update announcements
    if (updateRunning && !updateAnnounced && outStream != null) {
        outStream.createFrame(114);
        outStream.writeWordBigEndian(updateSeconds * 50 / 30);
    }
    
    // Update player movement
    player.updateThisPlayerMovement(outStream);
    
    // Process player list updates
    // Add/remove players from view
    // Send appearance and state changes
}
```

### NPC Updates
The `updateNPC()` method synchronizes NPC states:

```java
public void updateNPC(Player player, Stream stream) {
    // Update existing NPCs in player's view
    // Remove NPCs that are out of range
    // Add new NPCs that came into range
    // Send NPC movement and state updates
}
```

## Disconnection Handling

### Graceful Disconnection
When a player disconnects, the handler:

1. **Saves Player Data**: Ensures character progress is preserved
2. **Cleans Up Interactions**: Cancels trades, duels, and minigames
3. **Removes from Activities**: Exits Castle Wars, Trawler, etc.
4. **Handles Pets**: Picks up summoned pets
5. **Updates Friends**: Notifies friends of offline status
6. **Frees Resources**: Cleans up memory and network connections

```java
private void handleDisconnection(Client client) {
    // Save player data
    PlayerSave.saveGame(client);
    
    // Handle active trades
    if (client.inTrade) {
        Client other = (Client) players[client.tradeWith];
        if (other != null) {
            other.getTrading().declineTrade();
        }
    }
    
    // Handle duels
    if (client.duelStatus == 5) {
        Client other = (Client) players[client.duelingWith];
        if (other != null) {
            other.getDueling().duelVictory();
        }
    }
    
    // Remove from minigames
    if (GameEngine.trawler.players.contains(client)) {
        GameEngine.trawler.players.remove(client);
    }
    
    // Clean up and remove
    removePlayer(client);
    players[client.playerId] = null;
}
```

## Server Update Management

### Planned Updates
The PlayerHandler manages server restarts:

```java
// Announce update
if (updateRunning && !updateAnnounced) {
    updateAnnounced = true;
    GameEngine.updateServer = true;
}

// Kick all players when time expires
if (updateRunning && 
    System.currentTimeMillis() - updateStartTime > updateSeconds * 1000) {
    kickAllPlayers = true;
}
```

## Usage Examples

### Finding a Player
```java
// Check if player is online
if (PlayerHandler.isPlayerOn("PlayerName")) {
    int playerId = PlayerHandler.getPlayerID("PlayerName");
    Client player = (Client) PlayerHandler.players[playerId];
    // Interact with player
}
```

### Getting Server Statistics
```java
int totalPlayers = PlayerHandler.getPlayerCount();
int staffOnline = PlayerHandler.getNonPlayerCount();
int botsOnline = PlayerHandler.getPlayerShopCount();

System.out.println("Players: " + totalPlayers + 
                  ", Staff: " + staffOnline + 
                  ", Bots: " + botsOnline);
```

### Iterating Through Players
```java
for (int i = 1; i < PlayerHandler.players.length; i++) {
    if (PlayerHandler.players[i] != null && PlayerHandler.players[i].isActive) {
        Client player = (Client) PlayerHandler.players[i];
        // Process each player
        player.getPacketSender().sendMessage("Server announcement!");
    }
}
```

## Performance Considerations

### Optimization Strategies
- **Null Checks**: Always verify player existence before processing
- **Active Checks**: Skip inactive players to save CPU cycles
- **Exception Handling**: Isolate player errors to prevent server crashes
- **Batch Processing**: Group similar operations for efficiency

### Memory Management
- **Proper Cleanup**: Remove disconnected players promptly
- **Resource Limits**: Enforce maximum player limits
- **Garbage Collection**: Clear references when removing players

## Thread Safety

The PlayerHandler is designed to be called from the main game thread only. It is **not thread-safe** and should not be accessed concurrently from multiple threads.

## Error Handling

### Player Processing Errors
Each player's processing is wrapped in try-catch blocks to prevent one player's error from affecting others:

```java
try {
    players[i].process();
} catch (Exception e) {
    e.printStackTrace();
    // Player continues to be processed next tick
}
```

### Critical Error Recovery
If a player encounters repeated errors, they may be automatically disconnected to maintain server stability.

## Related Classes

- [`Player`](Player.md) - Base player class with game functionality
- [`Client`](Client.md) - Concrete player implementation
- [`GameEngine`](GameEngine.md) - Calls PlayerHandler.process() every tick
- [`PlayerSave`](PlayerSave.md) - Handles player data persistence
- [`NpcHandler`](NpcHandler.md) - Similar handler for NPCs
