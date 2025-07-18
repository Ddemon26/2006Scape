# GameEngine

**Package:** `com.rs2`  
**Source:** [`2006Scape Server/src/main/java/com/rs2/GameEngine.java`](2006Scape Server/src/main/java/com/rs2/GameEngine.java)

## Overview

The `GameEngine` class is the core heart of the 2006Scape server. It serves as the main entry point and orchestrates all server operations through a scheduled game loop that runs every 600ms (game tick). This class manages the initialization, execution, and shutdown of all server components.

## Key Responsibilities

- **Server Initialization**: Sets up all handlers, loads game data, and initializes integrations
- **Game Loop Management**: Executes the main server tick that processes all game logic
- **Resource Management**: Handles memory management and performance monitoring
- **Integration Services**: Manages Discord, website, and external service connections
- **Graceful Shutdown**: Ensures proper cleanup and player data saving on server shutdown

## Core Components

### Static Handlers
The GameEngine maintains static instances of all major game handlers:

```java
public static ItemHandler itemHandler = new ItemHandler();
public static PlayerHandler playerHandler = new PlayerHandler();
public static NpcHandler npcHandler = new NpcHandler();
public static ShopHandler shopHandler = new ShopHandler();
public static ObjectHandler objectHandler = new ObjectHandler();
public static ObjectManager objectManager = new ObjectManager();
public static FightCaves fightCaves = new FightCaves();
public static Trawler trawler = new Trawler();
```

These handlers are accessible throughout the server and manage their respective game systems.

## Main Methods

### `main(String[] args)`
The server entry point that:
1. Configures logging streams
2. Processes command line arguments (`-gui`, `-config`)
3. Loads external configurations
4. Initializes the file server and cache system
5. Loads game definitions (items, objects, NPCs)
6. Sets up integration services (Discord, website)
7. Starts the main game loop scheduler

**Command Line Arguments:**
- `-gui`: Enables the server control panel GUI
- `-config <file>`: Loads external configuration file

### Game Loop Execution
The main game tick runs every 600ms and processes components in this order:

1. **ItemHandler** - Processes ground items, respawning, and cleanup
2. **PlayerHandler** - Updates all player states, movement, and actions
3. **NpcHandler** - Processes NPC AI, movement, and combat
4. **ShopHandler** - Updates shop inventories and restocking
5. **ObjectManager** - Manages dynamic world objects
6. **Minigames** - Processes Castle Wars, Fight Pits, Pest Control
7. **CycleEventHandler** - Executes scheduled events
8. **Integration Services** - Updates Discord activity and website stats
9. **Auto-Save** - Saves player data every 5 minutes

### Performance Monitoring
The GameEngine includes comprehensive performance monitoring:

```java
// Logs warnings for slow operations
if (totalCycleDuration > 500) {
    System.err.println("ERROR: Cycle duration exceeded 500 ms!");
} else if (totalCycleDuration > 250) {
    System.err.println("WARNING: Cycle duration exceeded 250 ms!");
}
```

## Configuration

### Server Settings
Key configuration options loaded at startup:
- `Constants.SERVER_NAME` - Server display name
- `Constants.WORLD` - World number
- `Constants.CYCLE_TIME` - Game tick interval (default: 600ms)
- `Constants.GUI_ENABLED` - Control panel visibility
- `Constants.SERVER_DEBUG` - Debug mode toggle

### Integration Settings
- `Constants.WEBSITE_INTEGRATION` - Enable website player count updates
- Discord bot configuration for activity updates
- RSA encryption keys for client communication

## Usage Examples

### Starting the Server
```bash
# Basic startup
java -jar server.jar

# With GUI enabled
java -jar server.jar -gui

# With custom config
java -jar server.jar -config myconfig.json
```

### Accessing Handlers
```java
// Get player count
int playerCount = GameEngine.playerHandler.getPlayerCount();

// Access item handler
GameEngine.itemHandler.createGroundItem(itemId, x, y, height, amount);

// Get server uptime
long uptime = System.currentTimeMillis() - GameEngine.getServerStartTime();
```

## Error Handling

The GameEngine includes robust error handling:
- **Fatal Exception Recovery**: Saves all player data before shutdown
- **Trade/Duel Cleanup**: Properly handles interrupted player interactions
- **Resource Cleanup**: Ensures proper disposal of network connections and threads

## Performance Considerations

- **Tick Budget**: Each game tick should complete within 600ms
- **Memory Management**: Automatic garbage collection monitoring
- **Thread Safety**: Uses locks for critical sections
- **Resource Monitoring**: Tracks memory usage and active thread count

## Dependencies

- **Apollo Cache System**: For game definition loading
- **Netty**: For network communication
- **JavaCord**: For Discord integration
- **XStream**: For configuration serialization

## Best Practices

1. **Never block the game thread** - Use CycleEvents for long-running operations
2. **Monitor performance** - Check cycle duration logs regularly
3. **Graceful shutdown** - Always save player data before stopping
4. **Configuration management** - Use external config files for production
5. **Error logging** - Monitor error streams for issues

## Related Classes

- [`PlayerHandler`](PlayerHandler.md) - Manages all connected players
- [`NpcHandler`](NpcHandler.md) - Controls NPC behavior and spawning
- [`ItemHandler`](ItemHandler.md) - Handles ground items and cleanup
- [`CycleEventHandler`](CycleEventHandler.md) - Manages scheduled events
- [`Constants`](Constants.md) - Server configuration constants
