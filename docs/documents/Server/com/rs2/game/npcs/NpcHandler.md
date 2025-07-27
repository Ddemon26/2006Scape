# NpcHandler

**Package:** `com.rs2.game.npcs`  
**Source:** [`2006Scape Server/src/main/java/com/rs2/game/npcs/NpcHandler.java`](2006Scape Server/src/main/java/com/rs2/game/npcs/NpcHandler.java)

## Overview

The `NpcHandler` class is the central management system for all Non-game.entities.Player Characters (NPCs) in the 2006Scape server. It handles game.entities.NPC spawning, AI processing, combat mechanics, movement, and lifecycle management. This class is called every game tick by the [`GameEngine`](GameEngine.md) to process all active NPCs in the game world.

## Key Responsibilities

- **game.entities.NPC Lifecycle Management**: Spawning, updating, and removing NPCs
- **AI Processing**: game.entities.NPC behavior, movement, and decision making
- **Combat Management**: game.entities.NPC combat mechanics and damage calculations
- **Data Loading**: Loading game.entities.NPC definitions, spawn locations, and drop tables
- **Special Mechanics**: Boss transformations, pet behavior, and unique game.entities.NPC features
- **game.entities.Player Interaction**: game.entities.NPC-to-player combat and following mechanics

## Core Data Structures

### game.entities.NPC Arrays
```java
public static int MAX_NPCS = 4000;                    // Maximum NPCs allowed
public static Npc npcs[] = new Npc[MAX_NPCS];        // Active game.entities.NPC instances
public static NpcList NpcList[] = new NpcList[maxListedNPCs]; // game.entities.NPC definitions
```

### Constants
- **Face Types**: 1-Walk, 2-North, 3-South, 4-East, 5-West
- **Maximum NPCs**: 4000 concurrent NPCs supported
- **game.entities.NPC Definitions**: 4000 different game.entities.NPC types supported

## Core Methods

### game.entities.NPC Spawning

#### `spawnNpc(game.entities.Player client, int npcType, int x, int y, int heightLevel, int walkingType, int HP, int maxHit, int attack, int defence, boolean attackPlayer, boolean headIcon)`
Creates a new game.entities.NPC instance in the game world:

```java
public static void spawnNpc(game.entities.Player client, int npcType, int x, int y, 
                           int heightLevel, int walkingType, int HP, 
                           int maxHit, int attack, int defence, 
                           boolean attackPlayer, boolean headIcon) {
    // Find available slot
    int slot = -1;
    for (int i = 1; i < MAX_NPCS; i++) {
        if (npcs[i] == null) {
            slot = i;
            break;
        }
    }
    
    if (slot == -1) return; // No free slots
    
    // Create and configure game.entities.NPC
    Npc newNPC = new Npc(slot, npcType);
    newNPC.absX = x;
    newNPC.absY = y;
    newNPC.heightLevel = heightLevel;
    newNPC.HP = HP;
    newNPC.MaxHP = HP;
    // ... additional configuration
    
    npcs[slot] = newNPC;
}
```

**Parameters:**
- `client` - game.entities.Player spawning the game.entities.NPC (for ownership tracking)
- `npcType` - game.entities.NPC ID from definitions
- `x, y` - World coordinates
- `heightLevel` - Height level (0-3)
- `walkingType` - Movement behavior type
- `HP` - Current and maximum hit points
- `maxHit, attack, defence` - Combat statistics
- `attackPlayer` - Whether game.entities.NPC should be aggressive
- `headIcon` - Whether to show overhead icon

#### `spawnNpc3()` - Pet/Summon Spawning
Specialized spawning method for pets and summoned creatures:

```java
public void spawnNpc3(game.entities.Player c, int npcType, int x, int y, int heightLevel,
                      int walkingType, int HP, int maxHit, int attack, int defence,
                      boolean attackPlayer, boolean headIcon, boolean summonFollow) {
    // Similar to spawnNpc but with additional pet/summon logic
    if (summonFollow) {
        newNPC.summoner = true;
        newNPC.summonedBy = c.playerId;
        c.summonId = npcType;
        c.hasNpc = true;
    }
}
```

### game.entities.NPC Processing

#### `process()`
Main processing method called every game tick:

```java
public void process() {
    // Process each active game.entities.NPC
    for (int i = 0; i < MAX_NPCS; i++) {
        if (npcs[i] == null) continue;
        
        try {
            // Handle game.entities.NPC death
            if (npcs[i].isDead) {
                handleNpcDeath(i);
                continue;
            }
            
            // Process game.entities.NPC AI
            processNpcAI(i);
            
            // Handle combat
            processNpcCombat(i);
            
            // Update movement
            processNpcMovement(i);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### game.entities.NPC AI and Behavior

#### `getClosePlayer(game.entities.Player c, int i)`
Finds the closest valid player target for an game.entities.NPC:

```java
public int getClosePlayer(game.entities.Player c, int i) {
    for (int j = 0; j < PlayerHandler.players.length; j++) {
        if (PlayerHandler.players[j] != null) {
            // Check if player is spawner (priority target)
            if (j == npcs[i].spawnedBy) {
                return j;
            }
            
            // Check distance and combat availability
            if (goodDistance(PlayerHandler.players[j].absX, PlayerHandler.players[j].absY, 
                           npcs[i].absX, npcs[i].absY, 
                           2 + distanceRequired(i) + followDistance(i))) {
                
                // Check if player can be attacked
                if (PlayerHandler.players[j].underAttackBy <= 0 || 
                    Boundary.isIn(PlayerHandler.players[j], Boundary.MULTI)) {
                    
                    if (PlayerHandler.players[j].heightLevel == npcs[i].heightLevel) {
                        return j;
                    }
                }
            }
        }
    }
    return 0; // No valid target found
}
```

#### Movement and Following

```java
public static boolean followPlayer(int i) {
    // Determines if game.entities.NPC should follow players
    return distanceRequired(i) > 1;
}

public static void followPlayer(int i, game.entities.Player player) {
    // Implements game.entities.NPC following logic
    // Calculates path to player
    // Updates game.entities.NPC position
}

public static int distanceRequired(int i) {
    // Returns required distance for game.entities.NPC to engage
    switch (npcs[i].npcType) {
        case DRAGON: return 10;
        case MAGE: return 8;
        default: return 1;
    }
}
```

### Special game.entities.NPC Mechanics

#### Boss Transformations
```java
public void spawnSecondForm(game.entities.Player c, final int i) {
    // Kalphite Queen transformation example
    CycleEventHandler.getSingleton().addEvent(c, new CycleEvent() {
        @Override
        public void execute(CycleEventContainer container) {
            spawnNpc2(KALPHITE_QUEEN_1160, npcs[i].absX, npcs[i].absY, 
                     0, 1, 230, 45, 500, 300, true);
            container.stop();
        }
    }, 15); // 15 tick delay
}
```

#### Pet Behavior
```java
public void catchRat(final int npcIndex) {
    // Cat/kitten catching rats
    int foundRat = findNearbyRat(npcIndex);
    
    if (foundRat != -1) {
        npcs[npcIndex].chasingRat = foundRat;
        
        // Schedule catch attempt
        CycleEventHandler.getSingleton().addEvent(this, new CycleEvent() {
            @Override
            public void execute(CycleEventContainer container) {
                if (attemptCatch(npcIndex, foundRat)) {
                    handleSuccessfulCatch(npcIndex, foundRat);
                } else {
                    handleFailedCatch(npcIndex);
                }
                container.stop();
            }
        }, 4);
    }
}
```

### Data Management

#### game.entities.NPC Definitions
```java
public void loadNPCList() {
    // Loads game.entities.NPC definitions from data files
    // Populates NpcList array with:
    // - game.entities.NPC names
    // - Combat levels
    // - Hit points
    // - Aggressive status
    // - Size information
}

public static String getNpcListName(int npcId) {
    // Returns game.entities.NPC name by ID
    if (npcId >= 0 && npcId < NpcList.length && NpcList[npcId] != null) {
        return NpcList[npcId].npcName;
    }
    return "Unknown game.entities.NPC";
}

public static int getNpcListHP(int npcId) {
    // Returns game.entities.NPC hit points by ID
    if (npcId >= 0 && npcId < NpcList.length && NpcList[npcId] != null) {
        return NpcList[npcId].npcHP;
    }
    return 1;
}
```

#### Spawn Loading
```java
public void loadSpawnList() {
    // Loads game.entities.NPC spawn locations from configuration
    // Creates initial world population
    // Sets up respawn timers
}
```

### Combat Mechanics

#### Combat Processing
```java
public static int getMaxHit(int i) {
    // Calculates game.entities.NPC maximum damage
    // Considers game.entities.NPC type, level, and special abilities
    return npcs[i].maxHit;
}

public static void handleSpecialEffects(game.entities.Player c, int i, int damage) {
    // Handles special combat effects
    // Poison, disease, stat draining, etc.
    switch (npcs[i].npcType) {
        case POISON_SPIDER:
            c.poisonDamage = 4;
            break;
        case STAT_DRAINER:
            c.playerLevel[0] -= 1; // Drain attack
            break;
    }
}

public boolean retaliates(int npcType) {
    // Determines if game.entities.NPC fights back when attacked
    switch (npcType) {
        case CHICKEN:
        case COW:
        case SHEEP:
            return false;
        default:
            return true;
    }
}
```

#### Death Handling
```java
public void dropItems(int i) {
    // Handles game.entities.NPC death drops
    // Calculates drop table
    // Creates ground items
    // Awards experience
    
    NPCDropsHandler.handleDrops(npcs[i]);
    appendSlayerExperience(i);
    resetPlayersInCombat(i);
}
```

### Utility Methods

#### Distance and Positioning
```java
public static boolean goodDistance(int objectX, int objectY, int playerX, int playerY, int distance) {
    // Checks if two points are within specified distance
    return Math.abs(objectX - playerX) <= distance && 
           Math.abs(objectY - playerY) <= distance;
}

public static int GetMove(int Place1, int Place2) {
    // Calculates movement direction
    if (Place1 - Place2 == 0) return 0;
    else if (Place1 - Place2 < 0) return 1;
    else if (Place1 - Place2 > 0) return -1;
    return 0;
}
```

#### Type Checking
```java
public static boolean isUndead(int index) {
    // Checks if game.entities.NPC is undead type
    String name = getNpcListName(npcs[index].npcType);
    for (String s : Constants.UNDEAD) {
        if (s.equalsIgnoreCase(name)) {
            return true;
        }
    }
    return false;
}

public static boolean multiAttacks(int i) {
    // Determines if game.entities.NPC can attack multiple players
    switch (npcs[i].npcType) {
        case DRAGON:
        case DEMON:
            return true;
        default:
            return false;
    }
}
```

## Usage Examples

### Spawning an game.entities.NPC
```java
// Spawn a guard at Lumbridge
NpcHandler.spawnNpc(player, 9, 3200, 3200, 0, 1, 22, 3, 20, 20, false, false);

// Spawn an aggressive dragon
NpcHandler.spawnNpc(player, 50, 2850, 9650, 0, 1, 100, 15, 60, 40, true, true);
```

### Finding NPCs
```java
// Find all NPCs of a specific type
for (int i = 0; i < NpcHandler.MAX_NPCS; i++) {
    if (NpcHandler.npcs[i] != null && 
        NpcHandler.npcs[i].npcType == DRAGON) {
        // Process dragon game.entities.NPC
    }
}

// Get game.entities.NPC information
String npcName = NpcHandler.getNpcListName(npcId);
int npcHP = NpcHandler.getNpcListHP(npcId);
int combatLevel = NpcHandler.getNpcListCombat(npcId);
```

### Custom game.entities.NPC Behavior
```java
// Check if game.entities.NPC should attack player
if (NpcHandler.npcs[i] != null && !NpcHandler.npcs[i].isDead) {
    int targetPlayer = npcHandler.getClosePlayer(player, i);
    if (targetPlayer > 0) {
        // Initiate combat
        NpcCombat.attackPlayer(i, targetPlayer);
    }
}
```

## Performance Considerations

### Optimization Strategies
- **Null Checks**: Always verify game.entities.NPC existence before processing
- **Distance Calculations**: Use efficient distance checking
- **Combat Processing**: Batch similar operations
- **Memory Management**: Clean up dead NPCs promptly

### Resource Management
- **game.entities.NPC Limits**: Enforce maximum game.entities.NPC counts
- **Spawn Control**: Manage respawn rates
- **AI Complexity**: Balance realism with performance

## Integration Points

### GameEngine Integration
```java
// Called every game tick
GameEngine.npcHandler.process();
```

### game.entities.Player Interaction
```java
// NPCs interact with players through:
// - Combat system
// - Dialogue system
// - Trading system
// - Quest system
```

### World Integration
```java
// NPCs interact with world through:
// - Pathfinding system
// - Boundary checking
// - Object interaction
// - Area restrictions
```

## Best Practices

1. **Always check for null NPCs** before processing
2. **Use appropriate spawn methods** for different game.entities.NPC types
3. **Implement proper cleanup** for temporary NPCs
4. **Handle exceptions gracefully** to prevent server crashes
5. **Optimize AI calculations** for performance
6. **Use constants** for game.entities.NPC type checking

## Related Classes

- [`Npc`](Npc.md) - Individual game.entities.NPC instance class
- [`NpcList`](NpcList.md) - game.entities.NPC definition data structure
- [`NPCDropsHandler`](NPCDropsHandler.md) - Handles game.entities.NPC death drops
- [`NpcCombat`](NpcCombat.md) - game.entities.NPC combat mechanics
- [`PlayerHandler`](PlayerHandler.md) - game.entities.Player management counterpart
- [`GameEngine`](GameEngine.md) - Calls NpcHandler.process() every tick
