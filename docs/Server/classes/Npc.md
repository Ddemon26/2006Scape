# Npc

**Package:** `com.rs2.game.npcs`  
**Source:** [`2006Scape Server/src/main/java/com/rs2/game/npcs/Npc.java`](2006Scape Server/src/main/java/com/rs2/game/npcs/Npc.java)

## Overview

The `Npc` class represents an individual Non-Player Character in the 2006Scape server. Each NPC instance contains all the data and behavior for a single NPC, including position, combat stats, AI state, animations, and interactions. This class handles NPC movement, combat, transformations, and client synchronization. It serves as the foundation for all NPC behavior in the game world.

## Key Responsibilities

- **NPC State Management**: Position, health, combat stats, and status flags
- **Movement and AI**: Walking patterns, pathfinding, and behavioral logic
- **Combat System**: Attack patterns, damage handling, and combat timers
- **Visual Updates**: Animations, graphics, transformations, and facing direction
- **Player Interaction**: Dialogue, trading, and special NPC mechanics
- **Client Synchronization**: Sending NPC updates to nearby players

## Core Architecture

### NPC Identity and Type
```java
public int npcId;    // Unique instance ID (slot in NpcHandler array)
public int npcType;  // NPC definition ID (determines appearance, stats, etc.)
```

### Position and Movement
```java
public int absX, absY;           // Current world coordinates
public int heightLevel;          // Height level (0-3)
public int makeX, makeY;         // Spawn coordinates
public int spawnX, spawnY;       // Original spawn point
public int moveX, moveY;         // Movement deltas
public int direction;            // Current facing direction
public int walkingType;          // Movement behavior type
```

### Combat Statistics
```java
public int HP, MaxHP;            // Current and maximum hit points
public int maxHit;               // Maximum damage this NPC can deal
public int defence, attack;      // Combat stats
public int combatLevel;          // Combat level
public int attackType;           // 0=melee, 1=range, 2=mage
```

## Core Methods

### Initialization

#### `Npc(int npcId, int npcType)`
Creates a new NPC instance:

```java
public Npc(int npcId, int npcType) {
    this.npcId = npcId;
    this.npcType = npcType;
    direction = -1;
    isDead = false;
    applyDead = false;
    actionTimer = 0;
    randomWalk = true;
}
```

**Parameters:**
- `npcId` - Unique slot ID in the NpcHandler array
- `npcType` - NPC definition ID that determines appearance and behavior

### Movement and Positioning

#### `getNextWalkingDirection2()`
Calculates the next movement direction:

```java
public int getNextWalkingDirection2() {
    int dir = Misc.direction(absX, absY, absX + moveX, absY + moveY);
    dir >>= 1;
    absX += moveX;
    absY += moveY;
    return dir;
}
```

#### `getRandomAndHomeNPCWalking(int npcIndex)`
Handles random walking and pathfinding:

```java
public void getRandomAndHomeNPCWalking(int npcIndex) {
    direction = -1;
    
    // Only move if not frozen
    if (NpcHandler.npcs[npcIndex].freezeTimer == 0) {
        direction = getNextWalkingDirection2();
    }
}
```

#### Position Accessors
```java
public int getX() { return absX; }
public int getY() { return absY; }
public void setAbsX(int absX) { this.absX = absX; }
public void setAbsY(int absY) { this.absY = absY; }
```

### Visual Effects and Animations

#### `gfx0(int gfx)` / `gfx100(int gfx)`
Displays graphics effects on the NPC:

```java
public void gfx0(int gfx) {
    mask80var1 = gfx;
    mask80var2 = 65536;    // Ground level graphic
    mask80update = true;
    updateRequired = true;
}

public void gfx100(int gfx) {
    mask80var1 = gfx;
    mask80var2 = 6553600;  // Height 100 graphic
    mask80update = true;
    updateRequired = true;
}
```

#### `forceChat(String text)`
Makes the NPC display chat text:

```java
public void forceChat(String text) {
    forcedText = text;
    forcedChatRequired = true;
    updateRequired = true;
}
```

**Usage Example:**
```java
npc.forceChat("Help! I'm being attacked!");
```

### Facing and Interaction

#### `facePlayer(Player player)`
Makes the NPC face a specific player:

```java
public void facePlayer(Player player) {
    // Check if NPC is immobile (some NPCs don't turn)
    for (int immobileNpc : immobileNpcs) {
        if (npcType == immobileNpc) {
            return;
        }
    }
    
    face = (player != null) ? player.playerId + 32768 : 32768;
    dirUpdateRequired = true;
    updateRequired = true;
}
```

#### `turnNpc(int x, int y)`
Makes the NPC face specific coordinates:

```java
public void turnNpc(int x, int y) {
    FocusPointX = 2 * x + 1;
    FocusPointY = 2 * y + 1;
    updateRequired = true;
    turnUpdateRequired = true;
}
```

### Transformations

#### `requestTransform(int id)`
Transforms the NPC into a different type:

```java
public void requestTransform(int id) {
    transformId = id;
    transformUpdateRequired = true;
    updateRequired = true;
}
```

#### `shearSheep()` - Special Transformation Example
Handles sheep shearing with temporary transformation:

```java
public void shearSheep(Player player, int itemNeeded, int itemGiven, 
                      int animation, final int currentId, final int newId, 
                      int transformTime) {
    // Check if player has required tool
    if (!player.getItemAssistant().playerHasItem(itemNeeded)) {
        player.getPacketSender().sendMessage("You need " + 
            DeprecatedItems.getItemName(itemNeeded).toLowerCase() + " to do that.");
        return;
    }
    
    // Check if already sheared
    if (transformId == newId) {
        player.getPacketSender().sendMessage("This sheep has already been shorn.");
        return;
    }
    
    // Prevent double-shearing
    if (NpcHandler.npcs[npcId].isTransformed) {
        return;
    }
    
    // Perform shearing
    if (animation > 0) {
        player.startAnimation(animation);
    }
    
    requestTransform(newId);  // Transform to sheared sheep
    player.getItemAssistant().addItem(itemGiven, 1);  // Give wool
    player.getPacketSender().sendMessage("You get some " + 
        DeprecatedItems.getItemName(itemGiven).toLowerCase() + ".");
    
    // Schedule transformation back to normal
    CycleEventHandler.getSingleton().addEvent(player, new CycleEvent() {
        @Override
        public void execute(CycleEventContainer container) {
            requestTransform(currentId);  // Transform back
            container.stop();
        }
        
        @Override
        public void stop() {
            NpcHandler.npcs[npcId].isTransformed = false;
        }
    }, transformTime);
}
```

### Combat and Health

#### `handleHitMask(int damage)`
Processes damage and updates hit display:

```java
public void handleHitMask(int damage) {
    if (damage > 0) {
        hitDiff = damage;
        hitUpdateRequired = true;
        updateRequired = true;
        
        // Update health
        HP -= damage;
        if (HP <= 0) {
            HP = 0;
            isDead = true;
        }
    }
}
```

### Client Synchronization

#### `updateNPCMovement(Stream str)`
Sends movement updates to clients:

```java
public void updateNPCMovement(Stream str) {
    if (str != null) {
        if (direction == -1) {
            // No movement
            if (updateRequired) {
                str.writeBits(1, 1);
                str.writeBits(2, 0);
            } else {
                str.writeBits(1, 0);
            }
        } else {
            // Movement occurred
            str.writeBits(1, 1);
            str.writeBits(2, 1);
            str.writeBits(3, Misc.xlateDirectionToClient[direction]);
            
            if (updateRequired) {
                str.writeBits(1, 1);
            } else {
                str.writeBits(1, 0);
            }
        }
    }
}
```

#### `appendNPCUpdateBlock(Stream str)`
Sends comprehensive NPC updates to clients:

```java
public void appendNPCUpdateBlock(Stream str) {
    if (!updateRequired) return;
    
    int updateMask = 0;
    
    // Build update mask based on what changed
    if (animUpdateRequired) updateMask |= 0x10;
    if (hitUpdateRequired2) updateMask |= 8;
    if (mask80update) updateMask |= 0x80;
    if (dirUpdateRequired) updateMask |= 0x20;
    if (forcedChatRequired) updateMask |= 1;
    if (hitUpdateRequired) updateMask |= 0x40;
    if (transformUpdateRequired) updateMask |= 2;
    if (turnUpdateRequired) updateMask |= 4;
    
    str.writeByte(updateMask);
    
    // Send specific updates based on mask
    if (animUpdateRequired) appendAnimUpdate(str);
    if (hitUpdateRequired2) appendHitUpdate2(str);
    if (mask80update) appendMask80Update(str);
    if (dirUpdateRequired) appendFaceEntity(str);
    if (forcedChatRequired) str.writeString(forcedText);
    if (hitUpdateRequired) appendHitUpdate(str);
    if (transformUpdateRequired) appendTransformUpdate(str);
    if (turnUpdateRequired) appendFaceToUpdate(str);
}
```

#### `clearUpdateFlags()`
Resets all update flags after synchronization:

```java
public void clearUpdateFlags() {
    updateRequired = false;
    animUpdateRequired = false;
    hitUpdateRequired = false;
    hitUpdateRequired2 = false;
    dirUpdateRequired = false;
    forcedChatRequired = false;
    mask80update = false;
    transformUpdateRequired = false;
    turnUpdateRequired = false;
}
```

## NPC State Management

### Combat States
```java
public boolean isDead;           // NPC is dead
public boolean applyDead;        // Death processing required
public boolean needRespawn;      // Respawn required
public boolean underAttack;      // Currently in combat
public int underAttackBy;        // Player ID attacking this NPC
public int killerId;             // Player who killed this NPC
public int attackTimer;          // Attack cooldown timer
public int freezeTimer;          // Freeze/stun timer
```

### Behavioral States
```java
public boolean randomWalk;       // Can walk randomly
public boolean walkingHome;      // Returning to spawn point
public boolean aggressive;       // Attacks players on sight
public boolean summoner;         // Is a summoned creature
public int summonedBy;          // Player who summoned this NPC
public int chasingRat;          // For cats chasing rats
```

### Update Flags
```java
public boolean updateRequired;           // Any update needed
public boolean animUpdateRequired;       // Animation changed
public boolean hitUpdateRequired;        // Hit splat needed
public boolean dirUpdateRequired;        // Facing direction changed
public boolean forcedChatRequired;       // Chat text to display
public boolean transformUpdateRequired;  // Transformation occurred
public boolean turnUpdateRequired;       // Turn to face coordinates
```

## Usage Examples

### Basic NPC Operations
```java
// Create a new NPC
Npc npc = new Npc(slotId, npcTypeId);
npc.absX = 3200;
npc.absY = 3200;
npc.heightLevel = 0;

// Make NPC face a player
npc.facePlayer(player);

// Make NPC say something
npc.forceChat("Welcome to my shop!");

// Apply damage to NPC
npc.handleHitMask(25);
```

### Movement and AI
```java
// Set NPC movement
npc.moveX = 1;  // Move east
npc.moveY = 0;
npc.getRandomAndHomeNPCWalking(npcIndex);

// Make NPC face specific coordinates
npc.turnNpc(3200, 3200);

// Check if NPC can move
if (npc.freezeTimer == 0 && !npc.isDead) {
    // Process movement
}
```

### Visual Effects
```java
// Play animation
npc.animNumber = 123;
npc.animUpdateRequired = true;
npc.updateRequired = true;

// Show graphics
npc.gfx0(456);  // Ground level graphic

// Transform NPC
npc.requestTransform(newNpcTypeId);
```

### Combat Integration
```java
// Set combat stats
npc.HP = 100;
npc.MaxHP = 100;
npc.maxHit = 15;
npc.attack = 60;
npc.defence = 45;
npc.combatLevel = 50;

// Handle combat
if (npc.underAttack && npc.attackTimer <= 0) {
    // NPC can attack back
    npc.attackTimer = 4; // 4 tick delay
}
```

### Special Mechanics
```java
// Sheep shearing example
if (npcType == SHEEP && !npc.isTransformed) {
    npc.shearSheep(player, 1735, 1737, 893, SHEEP, SHEEP_SHEARED, 100);
}

// Pet following
if (npc.summoner && npc.summonedBy == player.playerId) {
    // Make pet follow player
}
```

## Performance Considerations

### Optimization Strategies
- **Update Flags**: Only send updates when NPC state actually changes
- **Distance Checking**: Only process NPCs near players
- **Batch Processing**: Group similar operations together
- **Memory Management**: Clean up dead NPCs promptly

### Common Pitfalls
- **Update Flag Management**: Always clear flags after processing
- **Null Pointer Exceptions**: Check for null NPCs before operations
- **Infinite Loops**: Ensure movement calculations don't cause loops
- **Memory Leaks**: Properly clean up event handlers and references

## Best Practices

1. **Always validate NPC state** before performing operations
2. **Use appropriate update flags** for different changes
3. **Handle special NPC types** with custom logic
4. **Implement proper cleanup** for dead or removed NPCs
5. **Optimize distance calculations** for performance
6. **Use event handlers** for timed transformations
7. **Respect NPC behavioral patterns** for authentic gameplay

## Integration Points

### NpcHandler Integration
```java
// NPCs are managed by NpcHandler
NpcHandler.npcs[npcId] = new Npc(npcId, npcType);

// Processing occurs in NpcHandler.process()
for (Npc npc : NpcHandler.npcs) {
    if (npc != null) {
        npc.process();
    }
}
```

### Player Integration
```java
// Players interact with NPCs
player.npcIndex = npcId;  // Target for combat
npc.facePlayer(player);   // NPC faces player
npc.underAttackBy = player.playerId;  // Track attacker
```

### Combat Integration
```java
// Combat system uses NPC data
int damage = calculateDamage(npc.defence);
npc.handleHitMask(damage);
if (npc.isDead) {
    handleNpcDeath(npc);
}
```

## Related Classes

- [`NpcHandler`](NpcHandler.md) - Manages all NPC instances
- [`Player`](Player.md) - Interacts with NPCs
- [`CombatAssistant`](CombatAssistant.md) - Handles NPC combat
- [`NpcActions`](NpcActions.md) - Processes NPC interactions
- [`NpcData`](NpcData.md) - NPC definitions and statistics
- [`Stream`](Stream.md) - Handles NPC update packets
