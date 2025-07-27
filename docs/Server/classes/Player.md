# game.entities.Player

**Package:** `com.rs2.game.players`  
**Source:** [`2006Scape Server/src/main/java/com/rs2/game/players/game.entities.Player.java`](2006Scape Server/src/main/java/com/rs2/game/players/game.entities.Player.java)

## Overview

The `game.entities.Player` class is the core abstract base class that represents a player character in the 2006Scape server. It serves as the foundation for all player-related functionality, containing comprehensive systems for skills, combat, inventory management, social features, and game mechanics. This class is extended by the [`Client`](Client.md) class to create concrete player instances.

## Key Responsibilities

- **game.entities.Player State Management**: Coordinates, health, skills, equipment, and inventory
- **core.engine.Game System Integration**: Provides access to all major game systems through assistant classes
- **Event Processing**: Handles player actions, combat, movement, and interactions
- **Network Communication**: Manages packet sending and receiving
- **Persistence**: Handles player data saving and loading
- **Social Features**: Trading, dueling, clan chat, and player interactions

## Core Architecture

### Assistant Pattern
The game.entities.Player class uses the Assistant pattern extensively, providing specialized helper classes for different aspects of gameplay:

```java
private final ItemAssistant itemAssistant = new ItemAssistant(this);
private final PlayerAssistant playerAssistant = new PlayerAssistant(this);
private final CombatAssistant combatAssistant = new CombatAssistant(this);
private final ShopAssistant shopAssistant = new ShopAssistant(this);
```

### Skill System Integration
Each skill has its own dedicated handler:

```java
private final Agility agility = new Agility(this);
private final Mining mining = new Mining();
private final Smithing smithing = new Smithing();
private final Runecrafting runecrafting = new Runecrafting(this);
private final Slayer slayer = new Slayer(this);
```

## Essential Methods

### game.entities.Player Management

#### `process()`
The main player processing method called every game tick:
- Handles desert heat damage
- Manages energy regeneration
- Processes special attack restoration
- Updates combat states and timers
- Manages prayer drain
- Handles stat restoration

```java
public void process() {
    // Desert heat processing
    if (Boundary.isIn(this, Boundary.DESERT) && heightLevel == 0) {
        DesertHeat.callHeat(this);
    }
    
    // Energy regeneration
    if (playerEnergy < 100 && System.currentTimeMillis() - lastIncrease >= getPlayerAssistant().raiseTimer()) {
        playerEnergy += 1;
        lastIncrease = System.currentTimeMillis();
    }
    
    // Special attack restoration
    if (System.currentTimeMillis() - specDelay > CombatConstants.INCREASE_SPECIAL_AMOUNT) {
        if (specAmount < 10) {
            specAmount += .5;
            getItemAssistant().addSpecialBar(playerEquipment[playerWeapon]);
        }
    }
}
```

#### `logout()` and `logout(boolean forceLogout)`
Handles player disconnection with proper cleanup:
- Saves player data
- Removes from minigames
- Handles combat restrictions
- Cleans up resources

```java
public void logout(boolean forceLogout) {
    if (!forceLogout && (underAttackBy > 0 || underAttackBy2 > 0) || duelStatus == 5) {
        getPacketSender().sendMessage("You can't logout during combat!");
        return;
    }
    
    // Cleanup minigames, pets, cannons
    // Save player state
    // Close session
}
```

### Communication & Networking

#### `flushOutStream()`
Sends queued packets to the client:
```java
public void flushOutStream() {
    if (disconnected || outStream == null || outStream.currentOffset == 0) {
        return;
    }
    byte[] temp = new byte[outStream.currentOffset];
    System.arraycopy(outStream.buffer, 0, temp, 0, temp.length);
    session.write(Unpooled.buffer().writeBytes(temp));
    outStream.currentOffset = 0;
}
```

#### `queueMessage(Packet packet)`
Queues incoming packets for processing:
```java
public void queueMessage(Packet packet) {
    if (queuedPackets.size() < 25) {
        queuedPackets.add(packet);
    }
}
```

### Event System

#### `post(Event event)`
Posts events to the plugin system:
```java
public <E extends Event> void post(E event) {
    eventProvider.post(this, event);
}
```

#### `startCurrentTask(int ticks, CycleEvent event)`
Starts a scheduled task for the player:
```java
public void startCurrentTask(int ticksBetweenExecution, CycleEvent event) {
    endCurrentTask();
    currentTask = CycleEventHandler.getSingleton().addEvent(this, event, ticksBetweenExecution);
}
```

## Assistant Classes

### Core Assistants

#### ItemAssistant
Handles all item-related operations:
- Inventory management
- Equipment handling
- game.items.Item creation and deletion
- Special item effects

```java
public ItemAssistant getItemAssistant() {
    return itemAssistant;
}
```

#### PlayerAssistant
Manages player state and utilities:
- Movement and teleportation
- Interface management
- Skill level calculations
- Area checking

```java
public PlayerAssistant getPlayerAssistant() {
    return playerAssistant;
}
```

#### CombatAssistant
Handles all combat mechanics:
- Melee, ranged, and magic combat
- Damage calculations
- Combat timers and delays
- Special attacks

```java
public CombatAssistant getCombatAssistant() {
    return combatAssistant;
}
```

### Skill Assistants

Each skill has dedicated methods for access:

```java
// Agility courses
public GnomeAgility getGnomeStrongHold() { return gnomeStrongHold; }
public BarbarianAgility getBarbarianAgility() { return barbarianAgility; }
public WildernessAgility getWildernessAgility() { return wildernessAgility; }

// Production skills
public Smithing getSmithing() { return smithing; }
public Mining getMining() { return mining; }
public Runecrafting getRC() { return runecrafting; }

// Farming system
public Allotments getAllotment() { return allotment; }
public Herbs getHerbs() { return herb; }
public WoodTrees getTrees() { return trees; }
```

## Location & Area Methods

### Area Detection
The game.entities.Player class provides numerous methods for detecting player location:

```java
public boolean inWild() // Wilderness check
public boolean inCw() // Castle Wars check
public boolean inBarrows() // Barrows check
public boolean inDuelArena() // Duel Arena check
public boolean inFightCaves() // Fight Caves check
public boolean inArea(int x, int y, int x1, int y1) // Custom area check
```

### Distance Calculations

```java
public boolean withinDistance(game.entities.Player otherPlayer) // game.entities.Player distance
public boolean withinDistance(Npc npc) // game.entities.NPC distance
public int distanceToPoint(int pointX, int pointY) // Point distance
public boolean goodDistance(int objectX, int objectY, int playerX, int playerY, int distance)
```

## Kill Count System

The game.entities.Player class includes a comprehensive game.entities.NPC kill count tracking system:

```java
public int getNpcKillCount(int npcId) {
    return npcKillCounts.getOrDefault(npcId, 0);
}

public void incrementNpcKillCount(int npcId, int count) {
    npcKillCounts.put(npcId, npcKillCounts.getOrDefault(npcId, 0) + count);
}

// Display options
public boolean displayBossKcMessages = false;
public boolean displaySlayerKcMessages = false;
public boolean displayRegularKcMessages = false;
```

## Temporary Data Storage

The game.entities.Player class provides a flexible temporary data system:

```java
public Object getTemporary(String name) {
    return temporary.get(name);
}

public void addTemporary(String name, Object value) {
    temporary.put(name, value);
}
```

## Usage Examples

### Basic game.entities.Player Operations
```java
// Get player's combat level
int combatLevel = player.calculateCombatLevel();

// Send a message to the player
player.getPacketSender().sendMessage("Welcome to 2006Scape!");

// Teleport player
player.getPlayerAssistant().movePlayer(3200, 3200, 0);

// Add experience
player.getPlayerAssistant().addSkillXP(1000, 0); // 1000 Attack XP
```

### Combat Operations
```java
// Start combat with game.entities.NPC
player.getCombatAssistant().attackNpc(npcId);

// Check if player can attack
if (player.getCombatAssistant().checkReqs()) {
    // Proceed with attack
}

// Apply damage
player.dealDamage(damage);
```

### game.items.Item Management
```java
// Add item to inventory
player.getItemAssistant().addItem(itemId, amount);

// Check if player has item
if (player.getItemAssistant().playerHasItem(itemId, amount)) {
    // game.entities.Player has the item
}

// Delete item
player.getItemAssistant().deleteItem(itemId, amount);
```

### Skill Operations
```java
// Mining example
if (player.getMining().canMine(rockId)) {
    player.getMining().mineRock(rockId);
}

// Agility example
player.getGnomeStrongHold().handleObstacle(obstacleId);

// Farming example
player.getAllotment().plantSeed(seedId, patchId);
```

## Best Practices

1. **Always check player state** before performing operations
2. **Use appropriate assistants** for different game systems
3. **Handle null checks** when accessing other players or NPCs
4. **Respect combat restrictions** when implementing PvP features
5. **Use the event system** for plugin integration
6. **Implement proper cleanup** in logout methods

## Related Classes

- [`Client`](Client.md) - Concrete implementation of game.entities.Player
- [`PlayerHandler`](PlayerHandler.md) - Manages all players
- [`PlayerAssistant`](PlayerAssistant.md) - Core player utilities
- [`ItemAssistant`](ItemAssistant.md) - game.items.Item management
- [`CombatAssistant`](CombatAssistant.md) - Combat mechanics
- [`PacketSender`](PacketSender.md) - Network communication
