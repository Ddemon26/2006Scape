# PacketSender

**Package:** `com.rs2.net`  
**Source:** [`2006Scape Server/src/main/java/com/rs2/net/PacketSender.java`](2006Scape Server/src/main/java/com/rs2/net/PacketSender.java)

## Overview

The `PacketSender` class is the primary interface for sending data packets from the server to the game client. It provides a comprehensive set of methods for updating the client's interface, sending messages, managing inventory displays, handling animations, and controlling all aspects of the player's visual experience. This class uses a fluent interface pattern, allowing method chaining for efficient packet sending.

## Key Responsibilities

- **Interface Management**: Showing/hiding interfaces, updating interface text and components
- **Inventory Updates**: Sending item data to various interface containers
- **Visual Effects**: Animations, graphics, sounds, and screen effects
- **Communication**: Chat messages, private messages, and clan chat
- **World Updates**: Ground items, objects, NPCs, and environmental changes
- **Player State**: Skill levels, configurations, and status updates
- **Login Process**: Complete player initialization and world synchronization

## Core Architecture

### Fluent Interface Pattern
```java
player.getPacketSender()
    .sendMessage("Welcome!")
    .showInterface(3559)
    .sendString("Hello World", 3560)
    .sendConfig(173, 1);
```

Each method returns `this`, allowing for method chaining and cleaner code.

### Player Association
```java
private final Player player;

public PacketSender(Player player) {
    this.player = player;
}
```

Every PacketSender instance is tied to a specific player, ensuring packets are sent to the correct client.

## Core Methods

### Login and Initialization

#### `loginPlayer()`
Handles the complete player login process:

```java
public PacketSender loginPlayer() {
    // Add to GUI if enabled
    if (Constants.GUI_ENABLED) {
        ControlPanel.addEntity(player.playerName);
    }
    
    // Initialize login screen
    player.getPlayerAssistant().loginScreen();
    
    // Check for bans
    if (Connection.isNamedBanned(player.playerName)) {
        player.logout();
        return this;
    }
    
    // Send initial login packet
    if (player.getOutStream() != null) {
        player.outStream.createFrame(249);
        player.outStream.writeByteA(1);
        player.outStream.writeWordBigEndianA(player.playerId);
    }
    
    // Initialize player state
    initializePlayerState();
    
    // Send welcome messages
    sendWelcomeMessages();
    
    // Setup equipment and skills
    initializeEquipmentAndSkills();
    
    // Final synchronization
    player.handler.updatePlayer(player, player.outStream);
    player.handler.updateNPC(player, player.outStream);
    player.flushOutStream();
    
    return this;
}
```

**Login Process:**
1. GUI registration
2. Ban checking
3. Duplicate login prevention
4. Quest state initialization
5. Pet summoning
6. Skill validation
7. Tutorial handling
8. Equipment setup
9. Interface initialization
10. World synchronization

### Communication

#### `sendMessage(String message)`
Sends a chat message to the player:

```java
public PacketSender sendMessage(String message) {
    if (player.getOutStream() != null) {
        player.getOutStream().createFrameVarSize(253);
        player.getOutStream().writeString(message);
        player.getOutStream().endFrameVarSize();
    }
    return this;
}
```

**Usage Examples:**
```java
player.getPacketSender().sendMessage("Welcome to the server!");
player.getPacketSender().sendMessage("@red@Warning: @bla@You are in danger!");
player.getPacketSender().sendMessage("@blu@Information: @bla@Quest completed!");
```

#### `sendClan(String name, String message, String clan, int rights)`
Sends clan chat messages:

```java
public PacketSender sendClan(String name, String message, String clan, int rights) {
    if (player.getOutStream() == null) return this;
    
    player.outStream.createFrameVarSizeWord(217);
    player.outStream.writeString(name);
    player.outStream.writeString(message);
    player.outStream.writeString(clan);
    player.outStream.writeWord(rights);
    player.outStream.endFrameVarSize();
    
    return this;
}
```

### Interface Management

#### `showInterface(int interfaceId)`
Displays an interface to the player:

```java
public PacketSender showInterface(int interfaceId) {
    if (player.getOutStream() != null) {
        player.getOutStream().createFrame(97);
        player.getOutStream().writeWord(interfaceId);
    }
    return this;
}
```

#### `sendString(String text, int componentId)`
Updates text on interface components:

```java
public PacketSender sendString(String text, int componentId) {
    return sendString(text, componentId, false);
}

public PacketSender sendString(String text, int componentId, boolean forceSend) {
    if (player.getOutStream() != null) {
        if (forceSend || player.checkPacket126Update(text, componentId)) {
            player.getOutStream().createFrameVarSizeWord(126);
            player.getOutStream().writeString(text);
            player.getOutStream().writeWordA(componentId);
            player.getOutStream().endFrameVarSizeWord();
        }
    }
    return this;
}
```

#### `closeAllWindows()`
Closes all open interfaces:

```java
public PacketSender closeAllWindows() {
    if (player.getOutStream() != null) {
        player.getOutStream().createFrame(219);
        player.flushOutStream();
    }
    return this;
}
```

### Inventory and Item Management

#### `sendUpdateItems(int frame, Item[] items)`
Updates item containers (inventory, bank, shop, etc.):

```java
public PacketSender sendUpdateItems(int frame, Item[] items) {
    if (player.getOutStream() != null) {
        player.getOutStream().createFrameVarSizeWord(53);
        player.getOutStream().writeWord(frame);
        player.getOutStream().writeWord(items.length);
        
        for (Item item : items) {
            // Handle item count encoding
            if (item.getCount() > 254) {
                player.getOutStream().writeByte(255);
                player.getOutStream().writeDWord_v2(item.getCount());
            } else {
                player.getOutStream().writeByte(item.getCount());
            }
            
            // Handle item ID
            int id = item.getId() + 1;
            if (item.getCount() < 1) id = 0;
            if (id > Constants.ITEM_LIMIT || id < 0) id = Constants.ITEM_LIMIT;
            
            player.getOutStream().writeWordBigEndianA(id);
        }
        
        player.getOutStream().endFrameVarSizeWord();
        player.flushOutStream();
    }
    return this;
}
```

#### `sendItemOnInterface(int itemId, int amount, int childId)`
Displays a specific item on an interface:

```java
public PacketSender sendItemOnInterface(int itemId, int amount, int childId) {
    if (player.getOutStream() != null) {
        player.getOutStream().createFrame(34);
        player.getOutStream().writeWord(childId);
        player.getOutStream().writeByte(0);
        player.getOutStream().writeDWord_v2(amount);
        player.getOutStream().writeWordBigEndianA(itemId);
    }
    return this;
}
```

### Ground Items

#### `createGroundItem(int itemId, int x, int y, int amount)`
Shows a ground item to the player:

```java
public PacketSender createGroundItem(int itemId, int x, int y, int amount) {
    return createGroundItem(itemId, x, y, amount, player.heightLevel);
}

public PacketSender createGroundItem(int itemId, int x, int y, int amount, int height) {
    if (player.getOutStream() != null) {
        player.getOutStream().createFrame(85);
        player.getOutStream().writeByteC(y - (player.mapRegionY * 8));
        player.getOutStream().writeByteC(x - (player.mapRegionX * 8));
        player.getOutStream().createFrame(44);
        player.getOutStream().writeWordBigEndianA(itemId);
        player.getOutStream().writeWord(amount);
        player.getOutStream().writeByte(0);
    }
    return this;
}
```

#### `removeGroundItem(int itemId, int x, int y, int amount)`
Removes a ground item from the player's view:

```java
public PacketSender removeGroundItem(int itemId, int x, int y, int amount) {
    if (player.getOutStream() != null) {
        player.getOutStream().createFrame(85);
        player.getOutStream().writeByteC(y - (player.mapRegionY * 8));
        player.getOutStream().writeByteC(x - (player.mapRegionX * 8));
        player.getOutStream().createFrame(156);
        player.getOutStream().writeByteS(0);
        player.getOutStream().writeWord(itemId);
    }
    return this;
}
```

### Visual Effects

#### `shakeScreen(int verticalAmount, int verticalSpeed, int horizontalAmount, int horizontalSpeed)`
Creates screen shake effects:

```java
public PacketSender shakeScreen(int verticalAmount, int verticalSpeed,
                               int horizontalAmount, int horizontalSpeed) {
    if (player.getOutStream() == null) return this;
    
    player.getOutStream().createFrame(35);
    player.getOutStream().writeByte(verticalAmount);
    player.getOutStream().writeByte(verticalSpeed);
    player.getOutStream().writeByte(horizontalAmount);
    player.getOutStream().writeByte(horizontalSpeed);
    
    return this;
}
```

#### `stillGfx(int id, int x, int y, int height, int time)`
Creates static graphics at a location:

```java
public PacketSender stillGfx(int id, int x, int y, int height, int time) {
    if (player.getOutStream() != null) {
        player.getOutStream().createFrame(85);
        player.getOutStream().writeByteC(y - (player.mapRegionY * 8));
        player.getOutStream().writeByteC(x - (player.mapRegionX * 8));
        player.getOutStream().createFrame(4);
        player.getOutStream().writeByte(0);
        player.getOutStream().writeWord(id);
        player.getOutStream().writeByte(height);
        player.getOutStream().writeWord(time);
    }
    return this;
}
```

### Audio

#### `sendSound(int id, int volume, int delay)`
Plays sound effects:

```java
public PacketSender sendSound(int id, int volume, int delay) {
    if (player.getOutStream() != null) {
        player.getOutStream().createFrame(174);
        player.getOutStream().writeWord(id);
        player.getOutStream().writeByte(volume);
        player.getOutStream().writeWord(delay);
    }
    return this;
}
```

#### `sendSong(int id)`
Changes background music:

```java
public PacketSender sendSong(int id) {
    if (player.getOutStream() != null) {
        player.getOutStream().createFrame(74);
        player.getOutStream().writeWordBigEndian(id);
    }
    return this;
}
```

### Configuration

#### `sendConfig(int id, int state)`
Updates client configuration values:

```java
public PacketSender sendConfig(int id, int state) {
    if (player.getOutStream() != null) {
        player.getOutStream().createFrame(36);
        player.getOutStream().writeWordBigEndian(id);
        player.getOutStream().writeByte(state);
    }
    return this;
}
```

**Common Configurations:**
- `173` - Run/walk toggle
- `504` - Run energy display
- `502` - Split chat mode
- `108` - Autocast spell

### Skills and Stats

#### `setSkillLevel(int skillId, int currentLevel, int experience)`
Updates skill information:

```java
public PacketSender setSkillLevel(int skillId, int currentLevel, int experience) {
    if (player.getOutStream() != null) {
        player.getOutStream().createFrame(134);
        player.getOutStream().writeByte(skillId);
        player.getOutStream().writeDWord_v1(experience);
        player.getOutStream().writeByte(currentLevel);
    }
    return this;
}
```

### World Objects

#### `object(int objectId, int x, int y, int face, int type)`
Creates or updates world objects:

```java
public PacketSender object(int objectId, int x, int y, int face, int type) {
    return object(objectId, x, y, player.heightLevel, face, type);
}

public PacketSender object(int objectId, int x, int y, int height, int face, int type) {
    if (player.getOutStream() != null) {
        player.getOutStream().createFrame(85);
        player.getOutStream().writeByteC(y - (player.mapRegionY * 8));
        player.getOutStream().writeByteC(x - (player.mapRegionX * 8));
        player.getOutStream().createFrame(151);
        player.getOutStream().writeByteA(0);
        player.getOutStream().writeWordBigEndian(objectId);
        player.getOutStream().writeByteS((type << 2) + (face & 3));
    }
    return this;
}
```

## Usage Examples

### Basic Communication
```java
// Send messages with color codes
player.getPacketSender()
    .sendMessage("@red@Warning: @bla@Low health!")
    .sendMessage("@gre@Success: @bla@Quest completed!");

// Chain multiple operations
player.getPacketSender()
    .closeAllWindows()
    .sendMessage("Interface closed")
    .showInterface(3559);
```

### Interface Management
```java
// Show bank interface
player.getPacketSender()
    .showInterface(5292)
    .sendString("Bank of " + Constants.SERVER_NAME, 5383)
    .sendUpdateItems(5064, bankItems);

// Update shop interface
player.getPacketSender()
    .sendString("Shop: " + shopName, 3901)
    .sendUpdateItems(3900, shopItems)
    .sendString("Coins: " + playerCoins, 3902);
```

### Visual Effects
```java
// Combat effects
player.getPacketSender()
    .shakeScreen(2, 2, 0, 0)  // Screen shake
    .sendSound(315, 100, 0)   // Hit sound
    .stillGfx(85, x, y, 0, 10); // Blood splat

// Teleport effects
player.getPacketSender()
    .stillGfx(308, player.absX, player.absY, 0, 0)
    .sendSound(200, 100, 0);
```

### Skill Updates
```java
// Update multiple skills
for (int i = 0; i < 23; i++) {
    player.getPacketSender().setSkillLevel(i, player.playerLevel[i], player.playerXP[i]);
}

// Update specific skill with message
player.getPacketSender()
    .setSkillLevel(0, newLevel, newXP)
    .sendMessage("Congratulations! You have reached level " + newLevel + " Attack!");
```

## Performance Considerations

### Packet Batching
- Use method chaining to reduce individual packet sends
- Flush output stream only when necessary
- Group related updates together

### Null Checking
```java
if (player.getOutStream() != null) {
    // Send packets
}
```

Always check for null output streams to prevent crashes.

### Memory Management
- Avoid creating unnecessary string objects
- Reuse packet data when possible
- Clean up resources properly

## Best Practices

1. **Always check for null streams** before sending packets
2. **Use method chaining** for efficiency and readability
3. **Batch related updates** to reduce network overhead
4. **Handle disconnected players** gracefully
5. **Use appropriate packet types** for different data
6. **Validate input parameters** to prevent client crashes
7. **Log important packet operations** for debugging

## Related Classes

- [`Player`](Player.md) - Contains PacketSender instance
- [`Client`](Client.md) - Concrete player with network session
- [`Stream`](Stream.md) - Handles packet data serialization
- [`PlayerAssistant`](PlayerAssistant.md) - Uses PacketSender for player operations
- [`ItemAssistant`](ItemAssistant.md) - Uses PacketSender for inventory updates
