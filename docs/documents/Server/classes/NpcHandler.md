# NpcHandler

**Package:** `com.rs2.game.npcs`  
**Source:** [`2006Scape Server/src/main/java/com/rs2/game/npcs/NpcHandler.java`](2006Scape Server/src/main/java/com/rs2/game/npcs/NpcHandler.java)

## Overview

The `NpcHandler` class is the central management system for all Non-Player Characters (NPCs) in the 2006Scape server. It handles NPC spawning, AI processing, combat mechanics, movement, and lifecycle management. This class is called every game tick by the [`GameEngine`](GameEngine.md) to process all active NPCs in the game world.

## Key Responsibilities

- **NPC Lifecycle Management**: Spawning, updating, and removing NPCs
- **AI Processing**: NPC behavior, movement, and decision making
- **Combat Management**: NPC combat mechanics and damage calculations
- **Data Loading**: Loading NPC definitions, spawn locations, and drop tables
- **Special Mechanics**: Boss transformations, pet behavior, and unique NPC features
- **Player Interaction**: NPC-to-player combat and following mechanics

## Core Data Structures

### NPC Arrays
```java
public static int MAX_NPCS = 4000;                    // Maximum NPCs allowed
public static Npc npcs[] = new Npc[MAX_NPCS];        // Active NPC instances
public static NpcList NpcList[] = new NpcList[maxListedNPCs]; // NPC definitions
```

### Constants
- **Face Types**: 1-Walk, 2-North, 3-South, 4-East, 5-West
- **Maximum NPCs**: 4000 concurrent NPCs supported
- **NPC Definitions**: 4000 different NPC types supported

## Core Methods

### NPC Spawning

#### `spawnNpc(Player client, int npcType, int x, int y, int heightLevel, int walkingType, int HP, int maxHit, int attack, int defence, boolean attackPlayer, boolean headIcon)`
Creates a new NPC instance in the game world:

```java
public static void spawnNpc(Player client, int npcType, int x, int y, 
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
    
    // Create and configure NPC
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
- `client` - Player spawning the NPC (for ownership tracking)
- `npcType` - NPC ID from definitions
- `x, y` - World coordinates
- `heightLevel` - Height level (0-3)
- `walkingType` - Movement behavior type
- `HP` - Current and maximum hit points
- `maxHit, attack, defence` - Combat statistics
- `attackPlayer` - Whether NPC should be aggressive
- `headIcon` - Whether to show overhead icon

#### `spawnNpc3()` - Pet/Summon Spawning
Specialized spawning method for pets and summoned creatures:

```java
public void spawnNpc3(Player c, int npcType, int x, int y, int heightLevel,
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

### NPC Processing

#### `process()`
Main processing method called every game tick:

```java
public void process() {
    // Process each active NPC
    for (int i = 0; i < MAX_NPCS; i++) {
        if (npcs[i] == null) continue;
        
        try {
            // Handle NPC death
            if (npcs[i].isDead) {
                handleNpcDeath(i);
                continue;
            }
            
            // Process NPC AI
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

### NPC AI and Behavior

#### `getClosePlayer(Player c, int i)`
Finds the closest valid player target for an NPC:

```java
public int getClosePlayer(Player c, int i) {
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
    // Determines if NPC should follow players
    return distanceRequired(i) > 1;
}

public static void followPlayer(int i, Player player) {
    // Implements NPC following logic
    // Calculates path to player
    // Updates NPC position
}

public static int distanceRequired(int i) {
    // Returns required distance for NPC to engage
    switch (npcs[i].npcType) {
        case DRAGON: return 10;
        case MAGE: return 8;
        default: return 1;
    }
}
```

### Special NPC Mechanics

#### Boss Transformations
```java
public void spawnSecondForm(Player c, final int i) {
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

#### NPC Definitions
```java
public void loadNPCList() {
    // Loads NPC definitions from data files
    // Populates NpcList array with:
    // - NPC names
    // - Combat levels
    // - Hit points
    // - Aggressive status
    // - Size information
}

public static String getNpcListName(int npcId) {
    // Returns NPC name by ID
    if (npcId >= 0 && npcId < NpcList.length && NpcList[npcId] != null) {
        return NpcList[npcId].npcName;
    }
    return "Unknown NPC";
}

public static int getNpcListHP(int npcId) {
    // Returns NPC hit points by ID
    if (npcId >= 0 && npcId < NpcList.length && NpcList[npcId] != null) {
        return NpcList[npcId].npcHP;
    }
    return 1;
}
```

#### Spawn Loading
```java
public void loadSpawnList() {
    // Loads NPC spawn locations from configuration
    // Creates initial world population
    // Sets up respawn timers
}
```

### Combat Mechanics

#### Combat Processing
```java
public static int getMaxHit(int i) {
    // Calculates NPC maximum damage
    // Considers NPC type, level, and special abilities
    return npcs[i].maxHit;
}

public static void handleSpecialEffects(Player c, int i, int damage) {
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
    // Determines if NPC fights back when attacked
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
    // Handles NPC death drops
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
    // Checks if NPC is undead type
    String name = getNpcListName(npcs[index].npcType);
    for (String s : Constants.UNDEAD) {
        if (s.equalsIgnoreCase(name)) {
            return true;
        }
    }
    return false;
}

public static boolean multiAttacks(int i) {
    // Determines if NPC can attack multiple players
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

### Spawning an NPC
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
        // Process dragon NPC
    }
}

// Get NPC information
String npcName = NpcHandler.getNpcListName(npcId);
int npcHP = NpcHandler.getNpcListHP(npcId);
int combatLevel = NpcHandler.getNpcListCombat(npcId);
```

### Custom NPC Behavior
```java
// Check if NPC should attack player
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
- **Null Checks**: Always verify NPC existence before processing
- **Distance Calculations**: Use efficient distance checking
- **Combat Processing**: Batch similar operations
- **Memory Management**: Clean up dead NPCs promptly

### Resource Management
- **NPC Limits**: Enforce maximum NPC counts
- **Spawn Control**: Manage respawn rates
- **AI Complexity**: Balance realism with performance

## Integration Points

### GameEngine Integration
```java
// Called every game tick
GameEngine.npcHandler.process();
```

### Player Interaction
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
2. **Use appropriate spawn methods** for different NPC types
3. **Implement proper cleanup** for temporary NPCs
4. **Handle exceptions gracefully** to prevent server crashes
5. **Optimize AI calculations** for performance
6. **Use constants** for NPC type checking

## Related Classes

- [`Npc`](Npc.md) - Individual NPC instance class
- [`NpcList`](NpcList.md) - NPC definition data structure
- [`NPCDropsHandler`](NPCDropsHandler.md) - Handles NPC death drops
- [`NpcCombat`](NpcCombat.md) - NPC combat mechanics
- [`PlayerHandler`](PlayerHandler.md) - Player management counterpart
- [`GameEngine`](GameEngine.md) - Calls NpcHandler.process() every tick
