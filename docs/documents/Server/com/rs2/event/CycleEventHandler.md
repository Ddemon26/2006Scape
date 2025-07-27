# CycleEventHandler

**Package:** `com.rs2.event`  
**Source:** [`2006Scape Server/src/main/java/com/rs2/event/CycleEventHandler.java`](2006Scape Server/src/main/java/com/rs2/event/CycleEventHandler.java)

## Overview

The `CycleEventHandler` class is the core scheduling system for the 2006Scape server, providing a robust mechanism for executing delayed and periodic tasks. It implements a tick-based event system where game events can be scheduled to run after a specific number of game cycles (ticks). This class is essential for implementing time-dependent game mechanics such as respawning, animations, skill actions, combat effects, and any feature requiring delayed execution.

## Key Responsibilities

- **Event Scheduling**: Registering events to execute after a specific number of game ticks
- **Event Processing**: Running events when their scheduled time arrives
- **Event Cancellation**: Stopping events when no longer needed
- **Owner Management**: Tracking which game objects own which events
- **Thread Safety**: Ensuring concurrent event processing is handled properly
- **Memory Management**: Cleaning up completed or stopped events

## Core Architecture

### Singleton Pattern
```java
private static CycleEventHandler instance = new CycleEventHandler();

public static CycleEventHandler getSingleton() {
    return instance;
}
```

The class uses the Singleton pattern to ensure only one event handler exists throughout the server.

### Event Storage
```java
private final List<CycleEventContainer> events = new ArrayList<CycleEventContainer>();
private final List<CycleEventContainer> eventsToAdd = new ArrayList<CycleEventContainer>();
```

Events are stored in two separate lists:
- `events`: Active events being processed
- `eventsToAdd`: Events queued to be added on the next cycle (prevents concurrent modification)

## Core Methods

### Event Registration

#### `addEvent(Object owner, CycleEvent event, int cycles)`
Schedules a new event to run after a specified number of game cycles:

```java
public CycleEventContainer addEvent(Object owner, CycleEvent event, int cycles) {
    // Create a new event container
    CycleEventContainer container = new CycleEventContainer(owner, event, cycles);
    
    // Queue the event for addition on next cycle
    eventsToAdd.add(container);
    
    // Return the container for reference (can be used to stop the event later)
    return container;
}
```

**Parameters:**
- `owner`: The object that owns this event (typically a game.entities.Player, game.entities.NPC, or game system)
- `event`: The CycleEvent implementation containing the code to execute
- `cycles`: Number of game ticks to wait before executing the event

**Returns:** A CycleEventContainer that can be used to stop the event later

### Event Processing

#### `process()`
Processes all active events, called once per game tick:

```java
public void process() {
    // Add any queued events to the main list
    if (eventsToAdd.size() > 0) {
        for (CycleEventContainer container : eventsToAdd) {
            events.add(container);
        }
        eventsToAdd.clear();
    }
    
    // Create a list for events to remove
    List<CycleEventContainer> eventsCopy = new ArrayList<CycleEventContainer>(events);
    List<CycleEventContainer> remove = new ArrayList<CycleEventContainer>();
    
    // Process each event
    for (CycleEventContainer container : eventsCopy) {
        if (container != null) {
            // Decrement the cycle count
            container.decrementTicks();
            
            // If it's time to execute
            if (container.needsExecution()) {
                try {
                    // Execute the event
                    container.execute();
                    
                    // If it's not a repeating event, mark for removal
                    if (!container.isRunning()) {
                        remove.add(container);
                    }
                } catch (Exception e) {
                    // Log any errors but continue processing other events
                    e.printStackTrace();
                    remove.add(container);
                }
            }
        }
    }
    
    // Remove completed events
    for (CycleEventContainer container : remove) {
        events.remove(container);
    }
}
```

### Event Cancellation

#### `stopEvents(Object owner)`
Stops all events associated with a specific owner:

```java
public void stopEvents(Object owner) {
    // Create a list of events to remove
    List<CycleEventContainer> remove = new ArrayList<CycleEventContainer>();
    
    // Find all events owned by this object
    for (CycleEventContainer container : events) {
        if (container.getOwner() == owner) {
            remove.add(container);
            container.stop();
        }
    }
    
    // Also check queued events
    for (CycleEventContainer container : eventsToAdd) {
        if (container.getOwner() == owner) {
            remove.add(container);
            container.stop();
        }
    }
    
    // Remove all found events
    events.removeAll(remove);
    eventsToAdd.removeAll(remove);
}
```

## Usage Examples

### Basic Delayed Execution
```java
// Execute code after 5 game ticks (3 seconds)
CycleEventHandler.getSingleton().addEvent(player, new CycleEvent() {
    @Override
    public void execute(CycleEventContainer container) {
        player.getPacketSender().sendMessage("This message appears after 3 seconds!");
        container.stop();
    }
    
    @Override
    public void stop() {
        // Cleanup code here
    }
}, 5);
```

### Repeating Events
```java
// Create a repeating event that runs every 10 ticks
CycleEventHandler.getSingleton().addEvent(player, new CycleEvent() {
    private int counter = 0;
    
    @Override
    public void execute(CycleEventContainer container) {
        player.getPacketSender().sendMessage("This message appears every 6 seconds!");
        
        // Reset the timer for the next execution
        container.setTicks(10);
        
        // Stop after 5 repetitions
        if (++counter >= 5) {
            container.stop();
        }
    }
    
    @Override
    public void stop() {
        player.getPacketSender().sendMessage("Repeating event stopped!");
    }
}, 10);
```

### Skill Actions
```java
// Woodcutting example
public void startWoodcutting(final game.entities.Player player, final int treeId, final int axeId) {
    // Store the event container for potential cancellation
    player.woodcuttingEvent = CycleEventHandler.getSingleton().addEvent(player, new CycleEvent() {
        @Override
        public void execute(CycleEventContainer container) {
            // Check if player can still cut
            if (!player.getItemAssistant().playerHasItem(axeId) || player.isBusy()) {
                container.stop();
                return;
            }
            
            // Give logs and experience
            player.getItemAssistant().addItem(LOGS, 1);
            player.getPlayerAssistant().addSkillXP(50, Constants.WOODCUTTING);
            
            // Random chance to deplete the tree
            if (Misc.random(10) == 0) {
                // Replace tree with stump
                GameEngine.objectHandler.createAnObject(player, TREE_STUMP, treeX, treeY);
                container.stop();
            } else {
                // Continue cutting - set next cycle
                container.setTicks(getWoodcuttingDelay(axeId));
            }
        }
        
        @Override
        public void stop() {
            player.startAnimation(65535); // Reset animation
            player.woodcuttingEvent = null;
        }
    }, getWoodcuttingDelay(axeId));
}
```

## Best Practices

1. **Always implement stop()** to clean up resources
2. **Check validity** of objects before operating on them
3. **Use appropriate cycle times** for different types of events
4. **Store event references** when you need to cancel them later
5. **Stop all events** when an object is being destroyed
6. **Handle exceptions** within event execution
7. **Avoid long-running operations** in event execution

## Related Classes

- [`CycleEvent`](CycleEvent.md) - Interface for event implementations
- [`CycleEventContainer`](CycleEventContainer.md) - Wrapper for events with timing
- [`GameEngine`](GameEngine.md) - Calls process() every tick
- [`game.entities.Player`](game.entities.Player.md) - Common event owner
- [`Npc`](Npc.md) - Common event owner