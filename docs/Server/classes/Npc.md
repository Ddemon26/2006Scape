# Npc

**Package:** `com.rs2.game.npcs`  
**Source:** [`2006Scape Server/src/main/java/com/rs2/game/npcs/Npc.java`](2006Scape Server/src/main/java/com/rs2/game/npcs/Npc.java)

## Overview

The `Npc` class represents an individual Non-game.entities.Player Character in the 2006Scape server. It contains all the data and behavior for a single game.entities.NPC instance, including position, combat stats, AI state, animations, and interactions. This class works in conjunction with the [`NpcHandler`](NpcHandler.md) to provide a complete game.entities.NPC system with movement, combat, and special behaviors.

## Key Responsibilities

- **game.entities.NPC State Management**: Tracking position, health, and status
- **Movement and Pathfinding**: Handling game.entities.NPC movement and walking patterns
- **Combat Integration**: Managing combat stats, damage, and death states
- **game.animation.Animation and Graphics**: Controlling game.entities.NPC animations and visual effects
- **game.entities.Player Interaction**: Handling game.entities.NPC-player interactions and facing
- **Special Behaviors**: Supporting unique game.entities.NPC mechanics and transformations
- **Update Management**: Coordinating visual updates sent to players

## Core Architecture

### Basic Properties
```java
public int npcId;           // Unique instance ID
public int npcType;         // game.entities.NPC type/definition ID
public int absX, absY;      // World coordinates
public int heightLevel;     // Height level (0-3)
public int spawnX, spawnY;  // Original spawn location
```

### Combat Properties
```java
public int HP, MaxHP;       // Current and maximum hit points
public int maxHit;          // Maximum damage this game.entities.NPC can deal
public int defence, attack; // Combat stats
public int combatLevel;     // Combat level
public boolean isDead;      // Death state
public boolean underAttack; // Currently in combat
```

### Movement Properties
```java
public int moveX, moveY;    // Movement deltas
public int direction;       // Current facing direction
public int walkingType;     // Movement behavior type
public boolean randomWalk;  // Whether game.entities.NPC walks randomly
public boolean walkingHome; // Returning to spawn point
```

## Core Methods

### Constructor and Initialization

#### `Npc(int npcId, int npcType)`
Creates a new game.entities.NPC instance:

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
- `npcId`: Unique instance identifier
- `npcType`: game.entities.NPC definition type

### Movement and Positioning

#### `getNextWalkingDirection2()`
Calculates the next movement direction:

```java
public int getNextWalkingDirection2() {
    int dir;
    dir = Misc.direction(absX, absY, absX + moveX, absY + moveY);
    dir >>= 1;
    absX += moveX;
    absY += moveY;
    return dir;
}
```

**Returns:** Direction value for client update

#### `getRandomAndHomeNPCWalking(int i)`
Handles random walking and returning home:

```java
public void getRandomAndHomeNPCWalking(int i) {
    direction = -1;
    if (NpcHandler.npcs[i].freezeTimer == 0) {
        direction = getNextWalkingDirection2();
    }
}
```

This method is called by the NpcHandler to process game.entities.NPC movement each tick.

#### `turnNpc(int x, int y)`
Makes the game.entities.NPC face a specific coordinate:

```java
public void turnNpc(int x, int y) {
    FocusPointX = 2 * x + 1;
    FocusPointY = 2 * y + 1;
    updateRequired = true;
    turnUpdateRequired = true;
}
```

### game.entities.Player Interaction

#### `facePlayer(game.entities.Player player)`
Makes the game.entities.NPC face a specific player:

```java
public void facePlayer(game.entities.Player player) {
    // Check if game.entities.NPC is immobile
    for (int element : immobileNpcs) {
        if (npcType == element) {
            return;
        }
    }
    
    face = (player != null) ? player.playerId + 32768 : 32768;
    dirUpdateRequired = true;
    updateRequired = true;
}
```

**Immobile NPCs:** Some NPCs (like certain quest NPCs) don't turn to face players:
```java
private static int[] immobileNpcs = {
    OSPAK, STYRMIR, TORBRUND, FRIDGEIR
};
```

### Visual Effects and game.animation.Animation

#### `startAnimation(int animId, int npcId)`
Starts an animation for the game.entities.NPC:

```java
public int startAnimation(int animId, int npcId) {
    animNumber = animId;
    animUpdateRequired = true;
    updateRequired = true;
    return animNumber;
}
```

#### Graphics Effects

```java
public void gfx0(int gfx) {
    mask80var1 = gfx;
    mask80var2 = 65536;
    mask80update = true;
    updateRequired = true;
}

public void gfx100(int gfx) {
    mask80var1 = gfx;
    mask80var2 = 6553600;
    mask80update = true;
    updateRequired = true;
}
```

- `gfx0()`: render.objects.Ground-level graphics effect
- `gfx100()`: Height-100 graphics effect

#### `forceChat(String text)`
Makes the game.entities.NPC display chat text:

```java
public void forceChat(String text) {
    forcedText = text;
    forcedChatRequired = true;
    updateRequired = true;
}
```

### Transformation System

#### `requestTransform(int id)`
Transforms the game.entities.NPC into a different type:

```java
public void requestTransform(int id) {
    transformId = id;
    transformUpdateRequired = true;
    updateRequired = true;
}
```

#### `shearSheep(game.entities.Player player, int itemNeeded, int itemGiven, int animation, final int currentId, final int newId, int transformTime)`
Specialized method for sheep shearing mechanics:

```java
public void shearSheep(game.entities.Player player, int itemNeeded, int itemGiven, int animation, 
                      final int currentId, final int newId, int transformTime) {
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
    
    // Prevent multiple shearing
    if (NpcHandler.npcs[npcId].isTransformed) {
        return;
    }
    
    // Perform shearing
    if (animation > 0) {
        player.startAnimation(animation);
    }
    
    requestTransform(newId);
    player.getItemAssistant().addItem(itemGiven, 1);
    player.getPacketSender().sendMessage("You get some " + 
        DeprecatedItems.getItemName(itemGiven).toLowerCase() + ".");
    
    // Schedule transformation back to original form
    CycleEventHandler.getSingleton().addEvent(player, new CycleEvent() {
        @Override
        public void execute(CycleEventContainer container) {
            requestTransform(currentId);
            container.stop();
        }
        
        @Override
        public void stop() {
            NpcHandler.npcs[npcId].isTransformed = false;
        }
    }, transformTime);
}
```

### Update System

#### `updateNPCMovement(core.network.Stream str)`
Writes movement data to the update stream:

```java
public void updateNPCMovement(core.network.Stream str) {
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

#### `appendNPCUpdateBlock(core.network.Stream str)`
Writes all pending updates to the stream:

```java
public void appendNPCUpdateBlock(core.network.Stream str) {
    if (!updateRequired) {
        return;
    }
    
    int updateMask = 0;
    
    // Build update mask
    if (animUpdateRequired) updateMask |= 0x10;
    if (hitUpdateRequired2) updateMask |= 8;
    if (mask80update) updateMask |= 0x80;
    if (dirUpdateRequired) updateMask |= 0x20;
    if (forcedChatRequired) updateMask |= 1;
    if (hitUpdateRequired) updateMask |= 0x40;
    if (transformUpdateRequired) updateMask |= 2;
    if (turnUpdateRequired) updateMask |= 4;
    
    str.writeByte(updateMask);
    
    // Write update data in specific order
    if (animUpdateRequired) appendAnimUpdate(str);
    if (hitUpdateRequired2) appendHitUpdate2(str);
    if (mask80update) appendMask80Update(str);
    if (dirUpdateRequired) appendFaceEntity(str);
    if (forcedChatRequired) str.writeString(forcedText);
    if (hitUpdateRequired) appendHitUpdate(str);
    if (transformUpdateRequired) appendTransformUpdate(str);
    if (turnUpdateRequired) appendSetFocusDestination(str);
}
```

### Utility Methods

#### `name()`
Gets the game.entities.NPC's display name:

```java
public String name() {
    return NpcHandler.getNpcListName(this.npcType);
}
```

#### Hit Update Methods
```java
public void appendHitUpdate(core.network.Stream str) {
    str.writeByte(hitDiff);
    str.writeByteA(hitUpdateRequired ? 1 : 0);
    str.writeByte(HP);
    str.writeByte(MaxHP);
}

public void appendHitUpdate2(core.network.Stream str) {
    str.writeByte(hitDiff2);
    str.writeByteS(hitUpdateRequired2 ? 1 : 0);
    str.writeByte(HP);
    str.writeByte(MaxHP);
}
```

## State Management

### Combat State
```java
public boolean underAttack;     // Currently being attacked
public boolean aggressive;      // Attacks players on sight
public int underAttackBy;       // game.entities.Player ID attacking this game.entities.NPC
public int killerId;            // game.entities.Player who will get loot rights
public int attackTimer;         // Combat delay timer
public int freezeTimer;         // Freeze spell duration
```

### Update Flags
```java
public boolean updateRequired;          // Needs client update
public boolean animUpdateRequired;      // game.animation.Animation changed
public boolean hitUpdateRequired;       // Took damage
public boolean forcedChatRequired;      // Has chat message
public boolean dirUpdateRequired;       // Direction changed
public boolean transformUpdateRequired; // Appearance changed
```

### Special Properties
```java
public boolean summoner;        // Is a summoned creature
public int summonedBy;          // game.entities.Player who summoned this game.entities.NPC
public int spawnedBy;           // game.entities.Player who spawned this game.entities.NPC
public boolean respawns;        // Should respawn after death
public int chasingRat;          // For cat NPCs chasing rats
```

## Usage Examples

### Creating and Configuring NPCs
```java
// Create new game.entities.NPC
Npc npc = new Npc(slotId, NPC_TYPE_GUARD);
npc.absX = 3200;
npc.absY = 3200;
npc.heightLevel = 0;
npc.HP = 100;
npc.MaxHP = 100;

// Make game.entities.NPC face a player
npc.facePlayer(player);

// Start animation
npc.startAnimation(ATTACK_ANIMATION, npc.npcId);
```

### Visual Effects
```java
// render.objects.Ground graphics
npc.gfx0(EXPLOSION_GFX);

// Height graphics
npc.gfx100(TELEPORT_GFX);

// Force chat
npc.forceChat("Help! I'm being attacked!");
```

### Transformations
```java
// Transform game.entities.NPC
npc.requestTransform(NEW_NPC_TYPE);

// Sheep shearing example
npc.shearSheep(player, SHEARS, WOOL, SHEARING_ANIMATION, 
               SHEEP_ID, SHEARED_SHEEP_ID, 100); // 100 ticks to regrow
```

### Movement Control
```java
// Make game.entities.NPC face specific coordinates
npc.turnNpc(targetX, targetY);

// Set movement destination
npc.moveX = deltaX;
npc.moveY = deltaY;

// Process movement
int direction = npc.getNextWalkingDirection2();
```

## Performance Considerations

### Optimization Strategies
- **Update Batching**: Only send updates when necessary
- **Efficient State Tracking**: Use boolean flags for state changes
- **Memory Management**: Clean up references when NPCs are removed
- **Distance Checking**: Only update NPCs near players

### Resource Management
- **game.animation.Animation Caching**: Reuse animation data where possible
- **Graphics Optimization**: Minimize graphics effect usage
- **Update Frequency**: Balance update rate with performance

## Best Practices

1. **Always validate game.entities.NPC state** before performing operations
2. **Use appropriate update flags** to minimize network traffic
3. **Handle special cases** for unique game.entities.NPC types
4. **Clean up resources** when NPCs are removed
5. **Coordinate with NpcHandler** for global operations
6. **Use transformation system** for temporary appearance changes
7. **Implement proper facing behavior** for immersive interactions

## Integration Points

### NpcHandler Integration
```java
// NPCs are managed by NpcHandler
NpcHandler.npcs[npcId] = new Npc(npcId, npcType);

// Processing is coordinated by NpcHandler
npcHandler.process(); // Calls methods on individual NPCs
```

### game.entities.Player Integration
```java
// NPCs interact with players
npc.facePlayer(player);
npc.underAttackBy = player.playerId;
```

### Combat Integration
```java
// Combat affects game.entities.NPC state
npc.HP -= damage;
npc.hitDiff = damage;
npc.hitUpdateRequired = true;
```

## Related Classes

- [`NpcHandler`](NpcHandler.md) - Manages all game.entities.NPC instances
- [`game.entities.Player`](game.entities.Player.md) - Interacts with NPCs
- [`core.network.Stream`](core.network.Stream.md) - Handles update packet data
- [`CycleEventHandler`](CycleEventHandler.md) - Manages game.entities.NPC events
- [`NpcList`](NpcList.md) - game.entities.NPC definition data
- [`Misc`](Misc.md) - Utility methods for calculations