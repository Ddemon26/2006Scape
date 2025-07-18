# Client

**Package:** `com.rs2.game.players`  
**Source:** [`2006Scape Server/src/main/java/com/rs2/game/players/Client.java`](2006Scape Server/src/main/java/com/rs2/game/players/Client.java)

## Overview

The `Client` class is the concrete implementation of the abstract [`Player`](Player.md) class. It represents an actual connected player in the 2006Scape server, handling the network session and providing the bridge between the game logic and the client connection. Every logged-in player is represented by a Client instance.

## Key Responsibilities

- **Network Session Management**: Maintains the connection to the game client
- **Player Instantiation**: Creates concrete player instances with proper initialization
- **Bot Support**: Handles both real players and bot accounts
- **Stream Management**: Manages input/output streams for packet communication

## Class Structure

```java
public class Client extends Player {
    // Inherits all Player functionality
    // Adds network session management
    // Provides concrete implementation
}
```

## Constructors

### `Client(GameSession session, int playerId)`
Creates a new Client instance for a real player connection:

```java
public Client(GameSession s, int _playerId) {
    super(_playerId);
    session = s;
    outStream = new Stream(new byte[Constants.BUFFER_SIZE]);
    outStream.currentOffset = 0;
    buffer = new byte[Constants.BUFFER_SIZE];
}
```

**Parameters:**
- `session` - The network session handling client communication
- `playerId` - Unique identifier for the player slot

**Initialization:**
- Sets up the output stream for sending packets
- Allocates buffer for packet processing
- Links the network session

### `Client(GameSession session)` - Bot Constructor
Creates a Client instance for bot accounts:

```java
public Client(GameSession s) {
    super(-1);
    isBot = true;
    session = null;
    buffer = new byte[Constants.BUFFER_SIZE];
}
```

**Bot Characteristics:**
- Uses player ID -1 to indicate bot status
- Sets `isBot = true` flag
- No network session (bots don't need network communication)
- Still allocates buffer for internal processing

## Core Methods

### `setSession(GameSession session)`
Updates the network session for the client:

```java
public void setSession(GameSession session) {
    this.session = session;
}
```

This method is typically used when:
- Reconnecting a player after network issues
- Transferring sessions during server maintenance
- Updating session parameters

## Network Integration

### GameSession Integration
The Client class works closely with the Apollo network framework:

```java
// Session provides:
// - Packet reading/writing
// - Connection state management
// - Encryption/decryption
// - Bandwidth management
```

### Stream Management
Each Client maintains its own output stream:

```java
// Output stream characteristics:
// - Buffer size: Constants.BUFFER_SIZE
// - Handles packet queuing
// - Manages packet encryption
// - Automatic flushing
```

## Usage Examples

### Creating a New Player Connection
```java
// When a player connects
GameSession session = // ... obtained from network layer
int playerId = PlayerHandler.getNextAvailableSlot();
Client client = new Client(session, playerId);

// Add to player handler
PlayerHandler.players[playerId] = client;
```

### Creating a Bot
```java
// Creating a bot account
Client bot = new Client(null); // No session needed
bot.playerName = "Bot_" + botId;
bot.isBot = true;

// Configure bot behavior
bot.setXPRate(1);
// ... additional bot setup
```

### Session Management
```java
// Updating session (reconnection scenario)
if (client.getSession() == null || !client.getSession().isActive()) {
    client.setSession(newSession);
    client.getPacketSender().sendMessage("Connection restored!");
}
```

## Relationship with Player Class

The Client class inherits all functionality from Player:

```java
// All Player methods are available:
client.getItemAssistant().addItem(995, 1000); // Add coins
client.getPlayerAssistant().movePlayer(3200, 3200, 0); // Teleport
client.getCombatAssistant().attackNpc(npcId); // Attack NPC
client.getPacketSender().sendMessage("Hello!"); // Send message
```

## Bot vs Real Player Differences

### Real Players
- Have active GameSession
- Process network packets
- Require authentication
- Subject to network timeouts
- Can disconnect/reconnect

### Bots
- No GameSession (session = null)
- No network communication
- Programmatically controlled
- Never timeout from network issues
- Always "connected" until manually removed

## Packet Processing

### Outgoing Packets
```java
// Client sends packets through inherited methods:
client.getPacketSender().sendMessage("Welcome!");
client.getPacketSender().sendInterface(interfaceId);
client.flushOutStream(); // Sends queued packets
```

### Incoming Packets
```java
// Packets are queued and processed:
client.queueMessage(incomingPacket);
client.processQueuedPackets(); // Process in game loop
```

## Memory Management

### Buffer Allocation
Each Client allocates its own buffers:
- Output stream buffer: `Constants.BUFFER_SIZE`
- General purpose buffer: `Constants.BUFFER_SIZE`
- Automatic cleanup on disconnect

### Resource Cleanup
```java
// Cleanup happens in Player.destruct():
// - Closes network session
// - Clears buffers
// - Removes from player list
// - Saves player data
```

## Error Handling

### Network Errors
- Automatic disconnection on session errors
- Graceful handling of connection loss
- Proper cleanup of resources

### Bot Errors
- Bots don't have network-related errors
- Still subject to game logic errors
- Can be safely removed without network cleanup

## Best Practices

1. **Always check session state** before sending packets to real players
2. **Handle bot detection** when implementing features that require network communication
3. **Proper resource cleanup** when removing clients
4. **Session validation** before performing network operations
5. **Bot-specific logic** for features that don't apply to automated accounts

## Integration Points

### PlayerHandler Integration
```java
// Client instances are managed by PlayerHandler
PlayerHandler.players[playerId] = client;
PlayerHandler.playerCount++; // If not a bot
```

### Network Layer Integration
```java
// Session callbacks trigger Client methods
session.onPacketReceived(packet -> client.queueMessage(packet));
session.onDisconnect(() -> client.logout(true));
```

## Related Classes

- [`Player`](Player.md) - Abstract base class with all game functionality
- [`PlayerHandler`](PlayerHandler.md) - Manages all Client instances
- [`GameSession`](GameSession.md) - Network session management
- [`PacketSender`](PacketSender.md) - Handles outgoing packet communication
- [`Stream`](Stream.md) - Packet data serialization
