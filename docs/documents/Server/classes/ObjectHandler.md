# ObjectHandler

**Package:** `com.rs2.world`  
**Source:** [`2006Scape Server/src/main/java/com/rs2/world/ObjectHandler.java`](2006Scape Server/src/main/java/com/rs2/world/ObjectHandler.java)

## Overview

The `ObjectHandler` class manages dynamic world objects in the 2006Scape server. It handles the creation, modification, and removal of temporary objects that are not part of the static world map, such as respawning resources (trees, rocks), doors, interactive objects, and special mechanics like wilderness obelisks. This class works alongside the static world data to provide a dynamic and interactive game environment.

## Key Responsibilities

- **Dynamic Object Management**: Creating, tracking, and removing temporary world objects
- **Resource Respawning**: Managing the respawn cycle for harvestable resources
- **Object Visibility**: Controlling which players can see specific objects
- **Special Mechanics**: Handling unique object behaviors like wilderness obelisks
- **World Synchronization**: Updating object states for all nearby players
- **Collision Management**: Integrating with the clipping system for pathfinding
- **Temporary Objects**: Managing objects with limited lifespans

## Core Architecture

### Object Storage
```java
public List<Objects> globalObjects = new ArrayList<Objects>();
public static List<Objects> mapObjects = new ArrayList<Objects>();
public static List<Objects> removedObjects = new ArrayList<Objects>();
```

The ObjectHandler maintains separate lists for different types of objects:
- `globalObjects`: Dynamic objects created by the server
- `mapObjects`: Static objects loaded from map data
- `removedObjects`: Objects that have been removed from the world

## Core Methods

### Object Creation

#### `createAnObject(int id, int x, int y, int face)`
Creates a new object at the specified location:

```java
public void createAnObject(int id, int x, int y, int face) {
    Objects object = new Objects(id, x, y, 0, face, 10, 0);
    
    if (id == -1) {
        // ID -1 means remove object
        removeObject(object);
    } else {
        // Add new object
        addObject(object);
    }
    
    // Place object in world and update players
    GameEngine.objectHandler.placeObject(object);
}
```

#### Overloaded Creation Methods
```java
// Create object for specific player
public void createAnObject(Player c, int id, int x, int y) {
    Objects object = new Objects(id, x, y, c.heightLevel, 0, 10, 0);
    processObjectCreation(object, id);
}

// Create object with height and face
public void createAnObject(Player player, int id, int x, int y, int h, int face) {
    Objects object = new Objects(id, x, y, h, face, 10, 0);
    processObjectCreation(object, id);
}

// Create object with full parameters
public void createAnObject(int id, int x, int y, int h, int face, int type) {
    Objects object = new Objects(id, x, y, h, face, type, 0);
    processObjectCreation(object, id);
}

private void processObjectCreation(Objects object, int id) {
    if (id == -1) {
        removeObject(object);
    } else {
        addObject(object);
    }
    GameEngine.objectHandler.placeObject(object);
}
```

**Parameters:**
- `id`: Object ID (-1 to remove object)
- `x, y`: World coordinates
- `h`: Height level (0-3)
- `face`: Object orientation (0-3)
- `type`: Object type (affects interaction and appearance)

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

#### `removeAllObjects(Objects o)`
Removes all objects at the same position:

```java
public void removeAllObjects(Objects o) {
    // Using removeIf for thread-safe concurrent modification
    globalObjects.removeIf(s -> s.getObjectX() == o.getObjectX() &&
                                s.getObjectY() == o.getObjectY() &&
                                s.getObjectHeight() == o.getObjectHeight());
}
```

### Object Queries

#### `objectExists(int objectX, int objectY, int objectHeight)`
Checks if an object exists at specific coordinates:

```java
public Objects objectExists(int objectX, int objectY, int objectHeight) {
    for (Objects o : globalObjects) {
        if (o.getObjectX() == objectX && 
            o.getObjectY() == objectY && 
            o.getObjectHeight() == objectHeight) {
            return o;
        }
    }
    return null;
}
```

#### `getObjectByPosition(int x, int y)`
Retrieves an object at specific coordinates:

```java
public Objects getObjectByPosition(int x, int y) {
    for (Objects o : globalObjects) {
        if (o.objectX == x && o.objectY == y) {
            return o;
        }
    }
    return null;
}
```

### Object Visibility and Updates

#### `updateObjects(Player c)`
Updates object visibility for a specific player:

```java
public void updateObjects(Player c) {
    for (Objects o : globalObjects) {
        if (c != null) {
            // Special handling for trees and rocks at height 0
            if (c.heightLevel == 0 && o.objectTicks == 0 && 
                c.distanceToPoint(o.getObjectX(), o.getObjectY()) <= 60) {
                
                if (Woodcutting.playerTrees(c, o.getObjectId()) || 
                    Mining.rockExists(o.getObjectId())) {
                    c.getPacketSender().object(o.getObjectId(), o.getObjectX(), 
                                             o.getObjectY(), 0, o.getObjectFace(), 
                                             o.getObjectType());
                }
            }
            
            // Regular objects at matching height
            if (c.heightLevel == o.getObjectHeight() && 
                !Woodcutting.playerTrees(c, o.getObjectId()) && 
                !Mining.rockExists(o.getObjectId()) && 
                o.objectTicks == 0 && 
                c.distanceToPoint(o.getObjectX(), o.getObjectY()) <= 60) {
                
                c.getPacketSender().object(o.getObjectId(), o.getObjectX(), 
                                         o.getObjectY(), c.heightLevel, 
                                         o.getObjectFace(), o.getObjectType());
            }
        }
    }
}
```

#### `placeObject(Objects o)`
Places an object in the world and updates all nearby players:

```java
public void placeObject(Objects o) {
    // Add clipping for pathfinding
    Region.addClipping(o.getObjectX(), o.getObjectY(), o.getObjectHeight(), 0);
    
    // Update all nearby players
    for (Player p : PlayerHandler.players) {
        if (p != null) {
            Client person = (Client) p;
            
            // Check if player is on same height and within range
            if (person.heightLevel == o.getObjectHeight() && o.objectTicks == 0) {
                if (person.distanceToPoint(o.getObjectX(), o.getObjectY()) <= 60) {
                    // Remove any existing objects at this position
                    removeAllObjects(o);
                    
                    // Add new object
                    globalObjects.add(o);
                    
                    // Send object to client
                    person.getPacketSender().object(o.getObjectId(), o.getObjectX(), 
                                                  o.getObjectY(), o.getObjectFace(), 
                                                  o.getObjectType());
                }
            }
        }
    }
}
```

### Object Processing

#### `process()`
Main processing method called every game tick:

```java
public void process() {
    for (int j = 0; j < globalObjects.size(); j++) {
        if (globalObjects.get(j) != null) {
            Objects o = globalObjects.get(j);
            
            // Handle object timers
            if (o.objectTicks > 0) {
                o.objectTicks--;
            }
            
            // Process object when timer expires
            if (o.objectTicks == 1) {
                Objects deleteObject = objectExists(o.getObjectX(), 
                                                  o.getObjectY(), 
                                                  o.getObjectHeight());
                removeObject(deleteObject);
                o.objectTicks = 0;
                placeObject(o);
                removeObject(o);
                
                // Handle special obelisk mechanics
                if (isObelisk(o.objectId)) {
                    int index = getObeliskIndex(o.objectId);
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

## Special Mechanics: Wilderness Obelisks

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

### Obelisk Activation

#### `startObelisk(int obeliskId)`
Activates a wilderness obelisk:

```java
public void startObelisk(int obeliskId) {
    int index = getObeliskIndex(obeliskId);
    if (index >= 0 && !activated[index]) {
        activated[index] = true;
        
        // Create activated obelisk objects (4 corners)
        Objects[] activatedObjects = new Objects[4];
        int baseX = obeliskCoords[index][0];
        int baseY = obeliskCoords[index][1];
        
        activatedObjects[0] = new Objects(IN_USE_ID, baseX, baseY, 0, -1, 10, 0);
        activatedObjects[1] = new Objects(IN_USE_ID, baseX + 4, baseY, 0, -1, 10, 0);
        activatedObjects[2] = new Objects(IN_USE_ID, baseX, baseY + 4, 0, -1, 10, 0);
        activatedObjects[3] = new Objects(IN_USE_ID, baseX + 4, baseY + 4, 0, -1, 10, 0);
        
        // Add and place activated objects
        for (Objects obj : activatedObjects) {
            addObject(obj);
            placeObject(obj);
        }
        
        // Create timer objects for deactivation
        Objects[] timerObjects = new Objects[4];
        timerObjects[0] = new Objects(obeliskIds[index], baseX, baseY, 0, -1, 10, 10);
        timerObjects[1] = new Objects(obeliskIds[index], baseX + 4, baseY, 0, -1, 10, 10);
        timerObjects[2] = new Objects(obeliskIds[index], baseX, baseY + 4, 0, -1, 10, 10);
        timerObjects[3] = new Objects(obeliskIds[index], baseX + 4, baseY + 4, 0, -1, 10, 10);
        
        // Add timer objects
        for (Objects obj : timerObjects) {
            addObject(obj);
        }
    }
}
```

#### `teleportObelisk(int port)`
Teleports players from one obelisk to another:

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
            Client c = (Client) player;
            
            // Check if player is within obelisk area
            if (Misc.goodDistance(c.getX(), c.getY(),
                                 obeliskCoords[port][0] + 2, 
                                 obeliskCoords[port][1] + 2, 1)) {
                
                // Teleport to random obelisk
                c.getPlayerAssistant().startTeleport(
                    obeliskCoords[random][0] + 2,
                    obeliskCoords[random][1] + 2, 0, "null");
            }
        }
    }
}
```

### Obelisk Utility Methods

```java
public boolean isObelisk(int id) {
    for (int obeliskId : obeliskIds) {
        if (obeliskId == id) {
            return true;
        }
    }
    return false;
}

public int getObeliskIndex(int id) {
    for (int j = 0; j < obeliskIds.length; j++) {
        if (obeliskIds[j] == id) {
            return j;
        }
    }
    return -1;
}
```

## Usage Examples

### Creating Objects
```java
ObjectHandler objectHandler = GameEngine.objectHandler;

// Create a simple object
objectHandler.createAnObject(1234, 3200, 3200, 0);

// Create object for specific player
objectHandler.createAnObject(player, 5678, player.absX, player.absY);

// Create object with full parameters
objectHandler.createAnObject(9999, 3100, 3100, 0, 2, 10);

// Remove object (use ID -1)
objectHandler.createAnObject(-1, 3200, 3200, 0);
```

### Resource Respawning
```java
// Tree chopped down - create stump and schedule respawn
objectHandler.createAnObject(player, TREE_STUMP, treeX, treeY);

// Schedule tree respawn after delay
CycleEventHandler.getSingleton().addEvent(null, new CycleEvent() {
    @Override
    public void execute(CycleEventContainer container) {
        objectHandler.createAnObject(ORIGINAL_TREE, treeX, treeY);
        container.stop();
    }
    
    @Override
    public void stop() {}
}, respawnDelay);
```

### Object Queries
```java
// Check if object exists
Objects obj = objectHandler.objectExists(x, y, height);
if (obj != null) {
    // Object exists at location
}

// Get object by position
Objects foundObject = objectHandler.getObjectByPosition(x, y);
if (foundObject != null) {
    int objectId = foundObject.getObjectId();
    // Process object
}
```

### Special Mechanics
```java
// Activate wilderness obelisk
objectHandler.startObelisk(14829);

// Check if object is an obelisk
if (objectHandler.isObelisk(objectId)) {
    int index = objectHandler.getObeliskIndex(objectId);
    // Handle obelisk interaction
}
```

## Performance Considerations

### Optimization Strategies
- **Distance Checking**: Only update objects for nearby players
- **Efficient Iteration**: Use appropriate data structures for fast lookups
- **Batch Processing**: Group object updates together
- **Memory Management**: Clean up expired objects promptly

### Resource Management
- **Object Limits**: Monitor total number of dynamic objects
- **Update Frequency**: Balance between responsiveness and performance
- **Collision Integration**: Efficiently update pathfinding data

## Best Practices

1. **Always validate coordinates** before creating objects
2. **Use appropriate object types** for different interactions
3. **Handle object removal** properly with ID -1
4. **Update nearby players** when objects change
5. **Integrate with collision system** for pathfinding
6. **Clean up temporary objects** when no longer needed
7. **Handle special cases** for unique object behaviors

## Integration Points

### GameEngine Integration
```java
// Called every game tick
GameEngine.objectHandler.process();
```

### Player Integration
```java
// Update objects when player moves to new area
objectHandler.updateObjects(player);

// Player interacts with object
Objects obj = objectHandler.objectExists(x, y, player.heightLevel);
if (obj != null) {
    // Handle interaction
}
```

### Skill Integration
```java
// Woodcutting - replace tree with stump
objectHandler.createAnObject(player, TREE_STUMP, treeX, treeY);

// Mining - replace rock with depleted version
objectHandler.createAnObject(player, DEPLETED_ROCK, rockX, rockY);
```

### Collision Integration
```java
// Objects automatically update clipping when placed
Region.addClipping(x, y, height, clipType);
```

## Related Classes

- [`Objects`](Objects.md) - Individual object data structure
- [`Player`](Player.md) - Players who interact with objects
- [`PacketSender`](PacketSender.md) - Sends object updates to client
- [`Region`](Region.md) - Handles collision and clipping
- [`GameEngine`](GameEngine.md) - Calls ObjectHandler.process() every tick
- [`Woodcutting`](Woodcutting.md) - Uses ObjectHandler for tree respawning
- [`Mining`](Mining.md) - Uses ObjectHandler for rock respawning