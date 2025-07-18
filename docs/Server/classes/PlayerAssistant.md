# PlayerAssistant

**Package:** `com.rs2.game.players`  
**Source:** [`2006Scape Server/src/main/java/com/rs2/game/players/PlayerAssistant.java`](2006Scape Server/src/main/java/com/rs2/game/players/PlayerAssistant.java)

## Overview

The `PlayerAssistant` class is a comprehensive utility class that provides essential player-related functionality and services. It serves as the primary helper for player operations including movement, teleportation, skill management, interface handling, combat utilities, and various game mechanics. This class is one of the most important assistant classes, containing core functionality that supports nearly every aspect of player interaction with the game world.

## Key Responsibilities

- **Movement and Teleportation**: Player positioning, pathfinding, and teleport mechanics
- **Skill Management**: Experience calculation, level-ups, and skill-related utilities
- **Interface Management**: Login screens, sidebars, and UI components
- **Combat Utilities**: Path blocking, following, and combat-related calculations
- **Game Mechanics**: Energy management, special item handling, and game state management
- **Administrative Functions**: Player validation, debugging, and server utilities
- **Visual Effects**: Animations, graphics, and camera controls

## Core Architecture

### Player Association
```java
private Player player;

public PlayerAssistant(Player player) {
    this.player = player;
}
```

Each PlayerAssistant instance is tied to a specific player, providing personalized functionality and state management.

## Core Methods

### Movement and Teleportation

#### `movePlayer(int x, int y, int height)`
Instantly moves a player to specified coordinates:

```java
public void movePlayer(int x, int y, int height) {
    player.resetWalkingQueue();
    player.absX = x;
    player.absY = y;
    player.heightLevel = height;
    player.didTeleport = true;
    requestUpdates();
}
```

#### `startTeleport(int x, int y, int height, String teleportType)`
Initiates a teleportation sequence with animations:

```java
public void startTeleport(int x, int y, int height, String teleportType) {
    if (player.teleTimer > 0) return;
    
    player.newLocation = 1;
    player.teleX = x;
    player.teleY = y;
    player.teleHeight = height;
    
    switch (teleportType.toLowerCase()) {
        case "modern":
            player.startAnimation(8939);
            player.teleGfx = 1576;
            player.teleTimer = 9;
            break;
        case "ancient":
            player.startAnimation(9599);
            player.teleGfx = 1681;
            player.teleTimer = 9;
            break;
        case "lunar":
            player.startAnimation(9606);
            player.teleGfx = 1685;
            player.teleTimer = 9;
            break;
    }
}
```

#### `processTeleport()`
Handles the teleportation animation sequence:

```java
public void processTeleport() {
    player.resetWalkingQueue();
    player.teleTimer = 8;
    player.newLocation = 0;
    requestUpdates();
}
```

### Skill Management

#### `addSkillXP(double amount, int skill)`
Adds experience to a specific skill:

```java
public boolean addSkillXP(double amount, int skill) {
    if (skill < 0 || skill >= player.playerXP.length) {
        return false;
    }
    
    // Apply XP rate multiplier
    amount *= player.getXPRate();
    
    // Check for maximum XP
    if (player.playerXP[skill] + amount > 200000000) {
        amount = 200000000 - player.playerXP[skill];
    }
    
    int oldLevel = getLevelForXP(player.playerXP[skill]);
    player.playerXP[skill] += (int) amount;
    int newLevel = getLevelForXP(player.playerXP[skill]);
    
    // Check for level up
    if (newLevel > oldLevel) {
        player.playerLevel[skill] = newLevel;
        levelUp(skill);
    }
    
    refreshSkill(skill);
    return true;
}
```

#### `levelUp(int skill)`
Handles level up notifications and effects:

```java
public void levelUp(int skill) {
    String skillName = SkillData.SKILL_NAMES[skill];
    int newLevel = getLevelForXP(player.playerXP[skill]);
    
    player.getPacketSender().sendMessage("Congratulations! You have reached level " + 
                                        newLevel + " " + skillName + "!");
    
    // Send level up interface
    player.getPacketSender().showInterface(6243);
    player.getPacketSender().sendString("Congratulations!", 6243);
    player.getPacketSender().sendString("You have just advanced a " + skillName + " level!", 6244);
    player.getPacketSender().sendString("You are now level " + newLevel + ".", 6245);
    
    // Play level up sound
    player.getPacketSender().sendSound(31, 100, 0);
    
    // Flash skill tab
    player.getPacketSender().flashSideBarIcon(skill == 3 ? 1 : 0);
}
```

#### Experience Calculation Utilities

```java
public static int getLevelForXP(int experience) {
    int points = 0;
    int output = 0;
    
    for (int level = 1; level <= 99; level++) {
        points += Math.floor(level + 300.0 * Math.pow(2.0, level / 7.0));
        output = (int) Math.floor(points / 4);
        
        if (output >= experience) {
            return level;
        }
    }
    return 99;
}

public static int getXPForLevel(int level) {
    int points = 0;
    int output = 0;
    
    for (int lvl = 1; lvl <= level; lvl++) {
        points += Math.floor(lvl + 300.0 * Math.pow(2.0, lvl / 7.0));
        if (lvl >= level) {
            return output;
        }
        output = (int) Math.floor(points / 4);
    }
    return 0;
}
```

### Interface Management

#### `loginScreen()`
Displays the login welcome screen:

```java
public void loginScreen() {
    player.getPacketSender().showInterface(15244);
    player.getPacketSender().sendString("Welcome to " + Constants.SERVER_NAME + 
                                       "             World: " + Constants.WORLD + "\\n", 15257);
    
    // Calculate days since last login
    int currentDay = player.getLastLogin() - player.lastLoginDate;
    
    // Set minimum herblore level
    if (player.playerLevel[Constants.HERBLORE] < 3) {
        player.playerLevel[Constants.HERBLORE] = 3;
        player.playerXP[Constants.HERBLORE] = 175;
        refreshSkill(Constants.HERBLORE);
    }
    
    // Display login message
    if (player.lastLoginDate <= 0) {
        player.getPacketSender().sendString("This is your first time logging in!", 15258);
    } else if (player.lastLoginDate == 1) {
        player.getPacketSender().sendString("You last logged in @red@yesterday @bla@ from: @red@" + 
                                           player.lastConnectedFrom, 15258);
    } else {
        String timeAgo = currentDay > 1 ? (currentDay + " @bla@days ago") : ("earlier today");
        player.getPacketSender().sendString("You last logged in @red@" + timeAgo + 
                                           " @bla@ from: @red@" + player.lastConnectedFrom, 15258);
    }
    
    // Display server information
    displayWelcomeMessages();
}
```

#### `sendSidebars()`
Initializes the player's interface sidebars:

```java
public void sendSidebars() {
    for (int i = 0; i < Constants.SIDEBARS.length; i++) {
        player.getPacketSender().setSidebarInterface(i, Constants.SIDEBARS[i]);
    }
    
    // Set magic book interface based on player's spellbook
    player.getPacketSender().setSidebarInterface(6, 
        player.playerMagicBook == 0 ? 1151 : 12855);
}
```

### Combat and Movement Utilities

#### `pathBlocked(Client attacker, Client victim)`
Determines if the path between two players is blocked:

```java
public static boolean pathBlocked(Client attacker, Client victim) {
    double offsetX = Math.abs(attacker.absX - victim.absX);
    double offsetY = Math.abs(attacker.absY - victim.absY);
    
    int distance = TileControl.calculateDistance(attacker, victim);
    
    if (distance == 0) return true;
    
    // Calculate movement ratios
    offsetX = offsetX > 0 ? offsetX / distance : 0;
    offsetY = offsetY > 0 ? offsetY / distance : 0;
    
    // Build path array
    int[][] path = new int[distance][5];
    int curX = attacker.absX;
    int curY = attacker.absY;
    
    // Calculate each step in the path
    for (int step = 0; step < distance; step++) {
        // Calculate next movement
        int nextMoveX = calculateNextMove(curX, victim.absX, offsetX);
        int nextMoveY = calculateNextMove(curY, victim.absY, offsetY);
        
        path[step] = new int[]{curX, curY, attacker.heightLevel, nextMoveX, nextMoveY};
        
        curX += nextMoveX;
        curY += nextMoveY;
    }
    
    // Check if any step in the path is blocked
    for (int[] step : path) {
        if (!Region.getClipping(step[0], step[1], step[2], step[3], step[4])) {
            return true;
        }
    }
    
    return false;
}
```

#### `followPlayer()` / `followNpc()`
Handles player following mechanics:

```java
public void followPlayer() {
    if (player.followPlayerId <= 0) return;
    
    Player target = PlayerHandler.players[player.followPlayerId];
    if (target == null || target.disconnected) {
        resetFollow();
        return;
    }
    
    // Calculate follow position
    int[] followPos = getFollowLocation(target.absX, target.absY);
    
    if (player.goodDistance(followPos[0], followPos[1], player.absX, player.absY, 1)) {
        return; // Already close enough
    }
    
    // Move towards target
    walkTo(followPos[0], followPos[1]);
}
```

### Energy and Status Management

#### `writeEnergy()`
Updates the player's run energy display:

```java
public void writeEnergy() {
    String energyText;
    
    if (player.playerEnergy >= 100) {
        energyText = "100%";
    } else if (player.playerEnergy > 0) {
        energyText = (int) Math.ceil(player.playerEnergy) + "%";
    } else {
        energyText = "0%";
    }
    
    player.getPacketSender().sendString(energyText, 149);
}
```

#### `raiseTimer()`
Calculates energy regeneration rate based on agility level:

```java
public int raiseTimer() {
    // Calculations from OSRS wiki: Energy regeneration
    double seconds = 60 / (8 + Math.floor(player.playerLevel[Constants.AGILITY] / 6));
    return (int) Math.floor(seconds * 1000);
}
```

### Special Mechanics

#### `handleROL()` - Ring of Life
Handles the Ring of Life teleportation mechanic:

```java
public void handleROL() {
    if (!savePlayer()) return;
    
    // Remove ring and teleport to safety
    player.getItemAssistant().deleteEquipment(2570, ItemConstants.RING);
    startTeleport(3222, 3218, 0, "modern");
    player.getPacketSender().sendMessage("Your ring of life saves you.");
}

public boolean savePlayer() {
    return (player.wildLevel < 20 && 
            player.playerEquipment[ItemConstants.RING] == 2570 && 
            player.playerLevel[Constants.HITPOINTS] > 0 && 
            player.playerLevel[Constants.HITPOINTS] <= getLevelForXP(player.playerXP[Constants.HITPOINTS]) / 10 && 
            player.underAttackBy > 0);
}
```

#### `startFading(int occurrence, int x, int y, int height)`
Creates a fade-to-black teleportation effect:

```java
public void startFading(final int occurrence, final int x, final int y, final int h) {
    if (!player.allowFading) return;
    
    player.allowFading = false;
    player.getPacketSender().showInterface(13583); // Black screen
    player.getPacketSender().sendMapState(2);
    
    CycleEventHandler.getSingleton().addEvent(this, new CycleEvent() {
        @Override
        public void execute(CycleEventContainer container) {
            movePlayer(x, y, h);
            resetAnimation();
            requestUpdates();
            container.stop();
        }
        
        @Override
        public void stop() {
            player.allowFading = true;
            player.getPacketSender().sendMapState(0);
            
            // Show arrival message after fade
            CycleEventHandler.getSingleton().addEvent(player, new CycleEvent() {
                @Override
                public void execute(CycleEventContainer container) {
                    container.stop();
                }
                
                @Override
                public void stop() {
                    player.getPacketSender().closeAllWindows();
                    showArrivalMessage(occurrence);
                }
            }, 2);
        }
    }, 4);
}
```

### Administrative and Utility Functions

#### `requestUpdates()`
Flags the player for appearance and position updates:

```java
public void requestUpdates() {
    player.updateRequired = true;
    player.appearanceUpdateRequired = true;
}
```

#### `isPlayer()`
Determines if the player is a regular player (not staff):

```java
public boolean isPlayer() {
    return player.playerRights < 2 || player.playerRights > 3;
}
```

#### `checkForFlags()`
Checks if the player's account has any suspicious flags:

```java
public boolean checkForFlags() {
    // Check for impossible stats, items, or other anomalies
    // Returns true if account appears to be modified/cheated
    
    // Check skill levels vs experience
    for (int i = 0; i < player.playerLevel.length; i++) {
        int maxLevel = getLevelForXP(player.playerXP[i]);
        if (player.playerLevel[i] > maxLevel + 20) { // Allow some boost margin
            return true;
        }
    }
    
    // Check for impossible items or quantities
    // Additional validation logic...
    
    return false;
}
```

## Usage Examples

### Basic Movement and Teleportation
```java
// Instant movement
player.getPlayerAssistant().movePlayer(3200, 3200, 0);

// Teleportation with animation
player.getPlayerAssistant().startTeleport(2605, 3093, 0, "modern");

// Fade teleportation
player.getPlayerAssistant().startFading(0, 2674, 3712, 0);
```

### Skill Management
```java
// Add experience
player.getPlayerAssistant().addSkillXP(1000, Constants.ATTACK);

// Check level requirements
int requiredLevel = 60;
if (player.playerLevel[Constants.WOODCUTTING] >= requiredLevel) {
    // Allow action
}

// Get total level
int totalLevel = player.getPlayerAssistant().getTotalLevel();
```

### Interface and UI Management
```java
// Show login screen
player.getPlayerAssistant().loginScreen();

// Setup sidebars
player.getPlayerAssistant().sendSidebars();

// Hide all sidebars
player.getPlayerAssistant().hideAllSideBars();

// Update energy display
player.getPlayerAssistant().writeEnergy();
```

### Combat Utilities
```java
// Check if path is blocked
if (PlayerAssistant.pathBlocked(attacker, victim)) {
    // Cannot attack - path blocked
}

// Start following another player
player.followPlayerId = targetPlayer.playerId;
player.getPlayerAssistant().followPlayer();

// Reset following
player.getPlayerAssistant().resetFollow();
```

### Administrative Functions
```java
// Check if player account is flagged
if (player.getPlayerAssistant().checkForFlags()) {
    // Handle suspicious account
}

// Check if player is regular player
if (player.getPlayerAssistant().isPlayer()) {
    // Apply normal player restrictions
}
```

## Performance Considerations

### Optimization Strategies
- **Efficient Pathfinding**: Use optimized algorithms for movement calculations
- **Batch Updates**: Group related interface updates together
- **Caching**: Cache frequently calculated values like total levels
- **Event Management**: Properly manage CycleEvents to prevent memory leaks

### Common Pitfalls
- **Teleport Spam**: Implement cooldowns to prevent teleport abuse
- **Path Calculation**: Expensive operations should be cached when possible
- **Interface Updates**: Avoid unnecessary interface refreshes
- **Memory Leaks**: Always stop CycleEvents properly

## Best Practices

1. **Always validate coordinates** before teleporting players
2. **Check player state** before performing actions
3. **Use appropriate teleport types** for different situations
4. **Handle edge cases** in pathfinding and movement
5. **Implement proper cooldowns** for special abilities
6. **Log important actions** for debugging and monitoring
7. **Respect game boundaries** and area restrictions

## Related Classes

- [`Player`](Player.md) - Contains PlayerAssistant instance
- [`Client`](Client.md) - Concrete player implementation
- [`PacketSender`](PacketSender.md) - Interface updates and communication
- [`CycleEventHandler`](CycleEventHandler.md) - Scheduled events and delays
- [`Region`](Region.md) - World clipping and pathfinding
- [`SkillData`](SkillData.md) - Skill-related constants and calculations
