# ObjectHandler

**Package:** `com.rs2.world`  
**Source:** [`2006Scape Server/src/main/java/com/rs2/world/ObjectHandler.java`](2006Scape Server/src/main/java/com/rs2/world/ObjectHandler.java)

**Author:** Sanity

## Overview

The `ObjectHandler` class manages all dynamic world objects in the 2006Scape server. It handles the creation, placement, removal, and lifecycle management of temporary and permanent objects that players can interact with. This includes skill-related objects (trees, rocks), temporary objects with timers, special mechanics like wilderness obelisks, and custom objects created by server events or player actions.

## Key Responsibilities

- **Dynamic Object Management**: Creating, placing, and removing world objects
- **Object Synchronization**: Ensuring all players see the correct objects
- **Temporary Objects**: Managing objects with expiration timers
- **Special Mechanics**: Handling unique objects like wilderness obelisks
- **Skill Integration**: Managing skill-related objects (trees, mining rocks)
- **Collision Detection**: Updating world clipping when objects change
- **Performance Optimization**: Efficient object processing and updates

## Core Architecture

### Object Storage
```java
public List<Objects> globalObjects = new ArrayList<Objects>();      // Active dynamic objects
public static List<Objects> mapObjects = new ArrayList<Objects>();  // Static map objects
public static List<Objects> removedObjects = new ArrayList<Objects>(); // Removed objects tracking
```

### Object Lifecycle
1. **Creation**: Objects are created with specific properties (ID, position, type, timer)
2. **Placement**: Objects are synchronized to all nearby players
3. **Processing**: Objects with timers are processed each game tick
4. **Removal**: Expired or deleted objects are cleaned up and removed

## Core Methods

### Object Creation

#### `createAnObject()` - Multiple Overloads
Creates objects with various parameter combinations:

```java
// Basic object creation
public void createAnObject(int id, int x, int y) {
    Objects object = new Objects(id, x, y, 0, 0, 10, 0);
    if (id == -1) {
        removeObject(object);
    } else {
        addObject(object);
    }
    placeObject(object);
}

// Full parameter object creation
public void createAnObject(int id, int x, int y, int height, int face, int type) {
    Objects object = new Objects(id, x, y, height, face, type, 0);
    if (id == -1) {
        removeObject(object);
    } else {
        addObject(object);
    }
    placeObject(object);
}

// Player-specific object creation
public void createAnObject(Player player, int id, int x, int y, int height, int face) {
    Objects object = new Objects(id, x, y, height, face, 10, 0);
    if (id == -1) {
        removeObject(object);
    } else {
        addObject(object);
    }
    placeObject(object);
}
```

**Parameters:**
- `id` - Object ID from cache definitions (-1 to remove)
- `x, y` - World coordinates
- `height` - Height level (0-3)
- `face` - Object orientation (0-3)
- `type` - Object type (affects interaction and appearance)

### Object Management

#### `addObject(Objects object)`
Adds an object to the global object list:

```java
public void addObject(Objects object) {
    globalObjects.add(object);
}
```

#### `removeObject(Objects object)`
Removes an object from the global object list:

```java
public void removeObject(Objects object) {
    globalObjects.remove(object);
}
```

#### `removeAllObjects(Objects object)`
Removes all objects at the same position:

```java
public void removeAllObjects(Objects object) {
    // Using Iterator for thread safety
    globalObjects.removeIf(s -> s.getObjectX() == object.getObjectX() &&
                               s.getObjectY() == object.getObjectY() &&
                               s.getObjectHeight() == object.getObjectHeight());
}
```

### Object Placement and Synchronization

#### `placeObject(Objects object)`
Places an object in the world and synchronizes it to nearby players:

```java
public void placeObject(Objects object) {
    // Add collision clipping
    Region.addClipping(object.getObjectX(), object.getObjectY(), 
                      object.getObjectHeight(), 0);
    
    // Send object to all nearby players
    for (Player p : PlayerHandler.players) {
        if (p != null) {
            Client player = (Client) p;
            
            // Check if player is on same height level and object is active
            if (player.heightLevel == object.getObjectHeight() && 
                object.objectTicks == 0) {
                
                // Check if player is within viewing distance (60 tiles)
                if (player.distanceToPoint(object.getObjectX(), 
                                         object.getObjectY()) <= 60) {
                    
                    // Remove any existing objects at this position
                    removeAllObjects(object);
                    globalObjects.add(object);
                    
                    // Send object packet to client
                    player.getPacketSender().object(
                        object.getObjectId(), object.getObjectX(),
                        object.getObjectY(), object.getObjectFace(),
                        object.getObjectType());
                }
            }
        }
    }
}
```

#### `updateObjects(Player player)`
Updates object visibility for a specific player (used on login/region change):

```java
public void updateObjects(Player player) {
    for (Objects object : globalObjects) {
        if (player != null) {
            boolean shouldShow = false;
            
            // Special handling for skill objects (trees, rocks)
            if (player.heightLevel == 0 && object.objectTicks == 0 && 
                player.distanceToPoint(object.getObjectX(), object.getObjectY()) <= 60) {
                
                if (Woodcutting.playerTrees(player, object.getObjectId()) || 
                    Mining.rockExists(object.getObjectId())) {
                    shouldShow = true;
                }
            }
            
            // Standard object visibility
            if (player.heightLevel == object.getObjectHeight() && 
                !Woodcutting.playerTrees(player, object.getObjectId()) && 
                !Mining.rockExists(object.getObjectId()) && 
                object.objectTicks == 0 && 
                player.distanceToPoint(object.getObjectX(), object.getObjectY()) <= 60) {
                shouldShow = true;
            }
            
            if (shouldShow) {
                player.getPacketSender().object(
                    object.getObjectId(), object.getObjectX(), object.getObjectY(),
                    player.heightLevel, object.getObjectFace(), object.getObjectType());
            }
        }
    }
}
```

### Object Processing

#### `process()`
Main processing method called every game tick to handle object timers:

```java
public void process() {
    for (int i = 0; i < globalObjects.size(); i++) {
        if (globalObjects.get(i) != null) {
            Objects object = globalObjects.get(i);
            
            // Process object timer
            if (object.objectTicks > 0) {
                object.objectTicks--;
            }
            
            // Handle object expiration
            if (object.objectTicks == 1) {
                Objects deleteObject = objectExists(object.getObjectX(),
                                                  object.getObjectY(), 
                                                  object.getObjectHeight());
                removeObject(deleteObject);
                object.objectTicks = 0;
                placeObject(object);
                removeObject(object);
                
                // Special handling for obelisks
                if (isObelisk(object.objectId)) {
                    int index = getObeliskIndex(object.objectId);
                    if (activated[index]) {
                        activated[index] = false;
                        teleportObelisk(index);
                    }
                }
            }
        }
    }
}
```

### Object Queries

#### `objectExists(int x, int y, int height)`
Checks if an object exists at specific coordinates:

```java
public Objects objectExists(int objectX, int objectY, int objectHeight) {
    for (Objects object : globalObjects) {
        if (object.getObjectX() == objectX && 
            object.getObjectY() == objectY &&
            object.getObjectHeight() == objectHeight) {
            return object;
        }
    }
    return null;
}
```

#### `getObjectByPosition(int x, int y)`
Retrieves an object at specific coordinates:

```java
public Objects getObjectByPosition(int x, int y) {
    for (Objects object : globalObjects) {
        if (object.objectX == x && object.objectY == y) {
            return object;
        }
    }
    return null;
}
```

## Special Mechanics: Wilderness Obelisks

The ObjectHandler includes special handling for wilderness obelisks that provide random teleportation:

### Obelisk Configuration
```java
public final int IN_USE_ID = 14825;
public int[] obeliskIds = { 14829, 14830, 111235, 14828, 14826, 14831 };
public int[][] obeliskCoords = { 
    { 3154, 3618 }, { 3225, 3665 }, { 3033, 3730 }, 
    { 3104, 3792 }, { 2978, 3864 }, { 3305, 3914 } 
};
public boolean[] activated = { false, false, false, false, false, false };
```

### Obelisk Methods

#### `startObelisk(int obeliskId)`
Activates an obelisk for teleportation:

```java
public void startObelisk(int obeliskId) {
    int index = getObeliskIndex(obeliskId);
    if (index >= 0 && !activated[index]) {
        activated[index] = true;
        
        // Create activation objects around the obelisk
        int x = obeliskCoords[index][0];
        int y = obeliskCoords[index][1];
        
        Objects[] activationObjects = {
            new Objects(14825, x, y, 0, -1, 10, 0),
            new Objects(14825, x + 4, y, 0, -1, 10, 0),
            new Objects(14825, x, y + 4, 0, -1, 10, 0),
            new Objects(14825, x + 4, y + 4, 0, -1, 10, 0)
        };
        
        // Place activation objects
        for (Objects obj : activationObjects) {
            addObject(obj);
            placeObject(obj);
        }
        
        // Create timed obelisk objects
        Objects[] timedObjects = {
            new Objects(obeliskIds[index], x, y, 0, -1, 10, 10),
            new Objects(obeliskIds[index], x + 4, y, 0, -1, 10, 10),
            new Objects(obeliskIds[index], x, y + 4, 0, -1, 10, 10),
            new Objects(obeliskIds[index], x + 4, y + 4, 0, -1, 10, 10)
        };
        
        // Add timed objects (will trigger teleport when expired)
        for (Objects obj : timedObjects) {
            addObject(obj);
        }
    }
}
```

#### `teleportObelisk(int port)`
Teleports players from one obelisk to a random destination:

```java
public void teleportObelisk(int port) {
    // Choose random destination (not the same obelisk)
    int random = Misc.random(5);
    while (random == port) {
        random = Misc.random(5);
    }
    
    // Teleport all players near the obelisk
    for (Player player : PlayerHandler.players) {
        if (player != null) {
            Client client = (Client) player;
            
            // Check if player is within 1 tile of obelisk center
            if (Misc.goodDistance(client.getX(), client.getY(),
                                obeliskCoords[port][0] + 2, 
                                obeliskCoords[port][1] + 2, 1)) {
                
                // Teleport to random obelisk
                client.getPlayerAssistant().startTeleport(
                    obeliskCoords[random][0] + 2,
                    obeliskCoords[random][1] + 2, 0, "null");
            }
        }
    }
}
```

## Usage Examples

### Basic Object Operations
```java
// Create a simple object
objectHandler.createAnObject(1276, 3200, 3200); // Tree at Lumbridge

// Create object with full parameters
objectHandler.createAnObject(1276, 3200, 3200, 0, 0, 10);

// Remove an object (use ID -1)
objectHandler.createAnObject(-1, 3200, 3200);

// Check if object exists
Objects obj = objectHandler.objectExists(3200, 3200, 0);
if (obj != null) {
    // Object exists at this location
}
```

### Temporary Objects
```java
// Create object that lasts 10 ticks
Objects tempObject = new Objects(1276, 3200, 3200, 0, 0, 10, 10);
objectHandler.addObject(tempObject);
objectHandler.placeObject(tempObject);
```

### Player-Specific Objects
```java
// Create object for specific player's height level
objectHandler.createAnObject(player, 1276, 3200, 3200, player.heightLevel);

// Update objects when player changes regions
objectHandler.updateObjects(player);
```

### Skill-Related Objects
```java
// Create a tree that can be cut
objectHandler.createAnObject(1276, 3200, 3200, 0, 0, 10);

// Create a mining rock
objectHandler.createAnObject(2090, 3200, 3200, 0, 0, 10);
```

### Obelisk Operations
```java
// Activate an obelisk
objectHandler.startObelisk(14829);

// Check if object is an obelisk
if (objectHandler.isObelisk(objectId)) {
    // Handle obelisk interaction
}
```

## Performance Considerations

### Optimization Strategies
- **Distance Checking**: Only update objects within 60 tiles of players
- **Height Level Filtering**: Only show objects on the same height level
- **Efficient Removal**: Use removeIf() for thread-safe bulk removal
- **Timer Processing**: Process object timers efficiently in batches

### Memory Management
- **Object Cleanup**: Remove expired objects promptly
- **List Management**: Use ArrayList for efficient access and iteration
- **Collision Updates**: Update clipping data when objects change

## Best Practices

1. **Always validate coordinates** before creating objects
2. **Check for existing objects** at the same position
3. **Use appropriate object types** for different interactions
4. **Handle height levels correctly** for multi-level areas
5. **Clean up temporary objects** to prevent memory leaks
6. **Update collision data** when objects affect movement
7. **Synchronize object changes** to all affected players

## Integration Points

### GameEngine Integration
```java
// Called every game tick
GameEngine.objectHandler.process();
```

### Skill System Integration
```java
// Woodcutting creates temporary "cut" objects
if (Woodcutting.playerTrees(player, objectId)) {
    // Handle tree cutting
}

// Mining creates temporary "mined" objects
if (Mining.rockExists(objectId)) {
    // Handle rock mining
}
```

### Player Integration
```java
// Update objects when player logs in or changes regions
objectHandler.updateObjects(player);

// Create player-specific objects
objectHandler.createAnObject(player, objectId, x, y, player.heightLevel);
```

## Related Classes

- [`Objects`](Objects.md) - Individual object data structure
- [`GameEngine`](GameEngine.md) - Calls ObjectHandler.process() every tick
- [`Region`](Region.md) - Handles collision clipping for objects
- [`Player`](Player.md) - Receives object updates and interactions
- [`Woodcutting`](Woodcutting.md) - Creates temporary tree objects
- [`Mining`](Mining.md) - Creates temporary rock objects
