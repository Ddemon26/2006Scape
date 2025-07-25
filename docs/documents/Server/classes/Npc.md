# Npc

**Package:** `com.rs2.game.npcs`  
**Source:** [`2006Scape Server/src/main/java/com/rs2/game/npcs/Npc.java`](2006Scape Server/src/main/java/com/rs2/game/npcs/Npc.java)

## Overview

The `Npc` class represents an individual Non-Player Character in the 2006Scape server. It contains all the data and behavior for a single NPC instance, including position, combat stats, AI state, animations, and interactions. This class works in conjunction with the [`NpcHandler`](NpcHandler.md) to provide a complete NPC system with movement, combat, and special behaviors.

## Key Responsibilities

- **NPC State Management**: Tracking position, health, and status
- **Movement and Pathfinding**: Handling NPC movement and walking patterns
- **Combat Integration**: Managing combat stats, damage, and death states
- **Animation and Graphics**: Controlling NPC animations and visual effects
- **Player Interaction**: Handling NPC-player interactions and facing
- **Special Behaviors**: Supporting unique NPC mechanics and transformations
- **Update Management**: Coordinating visual updates sent to players

## Core Architecture

### Basic Properties
```java
public int npcId;           // Unique instance ID
public int npcType;         // NPC type/definition ID
public int absX, absY;      // World coordinates
public int heightLevel;     // Height level (0-3)
public int spawnX, spawnY;  // Original spawn location
```

### Combat Properties
```java
public int HP, MaxHP;       // Current and maximum hit points
public int maxHit;          // Maximum damage this NPC can deal
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
public boolean randomWalk;  // Whether NPC walks randomly
public boolean walkingHome; // Returning to spawn point
```

## Core Methods

### Constructor and Initialization

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
- `npcId`: Unique instance identifier
- `npcType`: NPC definition type

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

This method is called by the NpcHandler to process NPC movement each tick.

#### `turnNpc(int x, int y)`
Makes the NPC face a specific coordinate:

```java
public void turnNpc(int x, int y) {
    FocusPointX = 2 * x + 1;
    FocusPointY = 2 * y + 1;
    updateRequired = true;
    turnUpdateRequired = true;
}
```

### Player Interaction

#### `facePlayer(Player player)`
Makes the NPC face a specific player:

```java
public void facePlayer(Player player) {
    // Check if NPC is immobile
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

### Visual Effects and Animation

#### `startAnimation(int animId, int npcId)`
Starts an animation for the NPC:

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

- `gfx0()`: Ground-level graphics effect
- `gfx100()`: Height-100 graphics effect

#### `forceChat(String text)`
Makes the NPC display chat text:

```java
public void forceChat(String text) {
    forcedText = text;
    forcedChatRequired = true;
    updateRequired = true;
}
```

### Transformation System

#### `requestTransform(int id)`
Transforms the NPC into a different type:

```java
public void requestTransform(int id) {
    transformId = id;
    transformUpdateRequired = true;
    updateRequired = true;
}
```

#### `shearSheep(Player player, int itemNeeded, int itemGiven, int animation, final int currentId, final int newId, int transformTime)`
Specialized method for sheep shearing mechanics:

```java
public void shearSheep(Player player, int itemNeeded, int itemGiven, int animation, 
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

#### `updateNPCMovement(Stream str)`
Writes movement data to the update stream:

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
Writes all pending updates to the stream:

```java
public void appendNPCUpdateBlock(Stream str) {
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
Gets the NPC's display name:

```java
public String name() {
    return NpcHandler.getNpcListName(this.npcType);
}
```

#### Hit Update Methods
```java
public void appendHitUpdate(Stream str) {
    str.writeByte(hitDiff);
    str.writeByteA(hitUpdateRequired ? 1 : 0);
    str.writeByte(HP);
    str.writeByte(MaxHP);
}

public void appendHitUpdate2(Stream str) {
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
public int underAttackBy;       // Player ID attacking this NPC
public int killerId;            // Player who will get loot rights
public int attackTimer;         // Combat delay timer
public int freezeTimer;         // Freeze spell duration
```

### Update Flags
```java
public boolean updateRequired;          // Needs client update
public boolean animUpdateRequired;      // Animation changed
public boolean hitUpdateRequired;       // Took damage
public boolean forcedChatRequired;      // Has chat message
public boolean dirUpdateRequired;       // Direction changed
public boolean transformUpdateRequired; // Appearance changed
```

### Special Properties
```java
public boolean summoner;        // Is a summoned creature
public int summonedBy;          // Player who summoned this NPC
public int spawnedBy;           // Player who spawned this NPC
public boolean respawns;        // Should respawn after death
public int chasingRat;          // For cat NPCs chasing rats
```

## Usage Examples

### Creating and Configuring NPCs
```java
// Create new NPC
Npc npc = new Npc(slotId, NPC_TYPE_GUARD);
npc.absX = 3200;
npc.absY = 3200;
npc.heightLevel = 0;
npc.HP = 100;
npc.MaxHP = 100;

// Make NPC face a player
npc.facePlayer(player);

// Start animation
npc.startAnimation(ATTACK_ANIMATION, npc.npcId);
```

### Visual Effects
```java
// Ground graphics
npc.gfx0(EXPLOSION_GFX);

// Height graphics
npc.gfx100(TELEPORT_GFX);

// Force chat
npc.forceChat("Help! I'm being attacked!");
```

### Transformations
```java
// Transform NPC
npc.requestTransform(NEW_NPC_TYPE);

// Sheep shearing example
npc.shearSheep(player, SHEARS, WOOL, SHEARING_ANIMATION, 
               SHEEP_ID, SHEARED_SHEEP_ID, 100); // 100 ticks to regrow
```

### Movement Control
```java
// Make NPC face specific coordinates
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
- **Animation Caching**: Reuse animation data where possible
- **Graphics Optimization**: Minimize graphics effect usage
- **Update Frequency**: Balance update rate with performance

## Best Practices

1. **Always validate NPC state** before performing operations
2. **Use appropriate update flags** to minimize network traffic
3. **Handle special cases** for unique NPC types
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

### Player Integration
```java
// NPCs interact with players
npc.facePlayer(player);
npc.underAttackBy = player.playerId;
```

### Combat Integration
```java
// Combat affects NPC state
npc.HP -= damage;
npc.hitDiff = damage;
npc.hitUpdateRequired = true;
```

## Related Classes

- [`NpcHandler`](NpcHandler.md) - Manages all NPC instances
- [`Player`](Player.md) - Interacts with NPCs
- [`Stream`](Stream.md) - Handles update packet data
- [`CycleEventHandler`](CycleEventHandler.md) - Manages NPC events
- [`NpcList`](NpcList.md) - NPC definition data
- [`Misc`](Misc.md) - Utility methods for calculations