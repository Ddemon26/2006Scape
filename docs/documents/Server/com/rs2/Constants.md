# Constants

**Package:** `com.rs2`  
**Source:** [`2006Scape Server/src/main/java/com/rs2/Constants.java`](2006Scape Server/src/main/java/com/rs2/Constants.java)

## Overview

The `Constants` class serves as the central configuration hub for the 2006Scape server, containing all essential server settings, game constants, and configuration values. This class defines everything from server identity and network settings to game mechanics, skill IDs, and game.entities.NPC classifications. Many of these values can be modified at runtime using the ConfigLoader system, making it easy to customize server behavior without code changes.

## Key Responsibilities

- **Server Configuration**: Core server settings like name, world ID, and network ports
- **core.engine.Game Mechanics**: XP rates, respawn locations, and gameplay toggles
- **Network Settings**: Connection limits, timeouts, and security parameters
- **Skill System**: Skill IDs and related constants
- **game.entities.NPC Classification**: Boss and slayer monster categorization
- **Security**: RSA encryption keys and authentication settings
- **Performance**: Buffer sizes, cycle timing, and optimization parameters

## Core Configuration Categories

### Server Identity and Basic Settings

#### Server Information
```java
public static String SERVER_NAME = "2006Scape";
public static String WEBSITE_LINK = "https://2006Scape.org";
public static int WORLD = 1;
public static double TEST_VERSION = 2.3;
```

#### Network Configuration
```java
public static int HTTP_PORT = 8080;           // Web server port
public static int JAGGRAB_PORT = 43595;       // File server port
public static int MAX_PLAYERS = 200;          // Maximum concurrent players
public static int TIMEOUT = 60;               // Connection timeout in seconds
public static int IPS_ALLOWED = 250;          // IPs allowed per connection
public static int CONNECTION_DELAY = 100;     // Delay between connections
```

#### Server Features
```java
public static boolean GUI_ENABLED = false;           // Control panel GUI
public static boolean FILE_SERVER = true;           // Enable file server
public static boolean SERVER_DEBUG = false;         // Debug mode
public static boolean MEMBERS_ONLY = false;         // Members-only world
public static boolean TUTORIAL_ISLAND = false;      // Tutorial Island enabled
public static boolean WEBSITE_INTEGRATION = false;  // Website features
```

### core.engine.Game Mechanics and Gameplay

#### Experience and Progression
```java
public static double XP_RATE = 1.0;                    // Global XP multiplier
public static boolean VARIABLE_XP_RATE = false;        // Allow player choice
public static int[] VARIABLE_XP_RATES = {1, 2, 5, 10}; // Available XP rates
```

#### game.entities.Player Management
```java
public static int SAVE_TIMER = 120;           // Auto-save interval (seconds)
public static int RESPAWN_X = 3222;           // Death respawn X coordinate
public static int RESPAWN_Y = 3218;           // Death respawn Y coordinate
public static int DUELING_RESPAWN_X = 3362;   // Duel death respawn X
public static int DUELING_RESPAWN_Y = 3263;   // Duel death respawn Y
```

#### Content Toggles
```java
public static boolean PARTY_ROOM_DISABLED = false;  // Disable party room
public static boolean CLUES_ENABLED = true;         // Enable clue scrolls
public static boolean ITEM_REQUIREMENTS = true;     // Enforce item requirements
public static boolean SOUND = true;                 // Enable sound effects
public static boolean GUILDS = true;                // Enable guild features
```

#### Administrative Controls
```java
public static boolean ADMIN_CAN_TRADE = false;      // Allow admin trading
public static boolean ADMIN_DROP_ITEMS = false;     // Allow admin item drops
public static boolean ADMIN_CAN_SELL_ITEMS = false; // Allow admin shop sales
```

### Technical and Performance Settings

#### System Performance
```java
public static final int CYCLE_TIME = 600;           // core.engine.Game tick interval (ms)
public static final int BUFFER_SIZE = 10000;        // Network buffer size
public static int CYCLE_LOGGING_TICK = 10;          // Logging frequency
public static boolean CYCLE_LOGGING = true;         // Enable cycle logging
```

#### core.engine.Game Limits
```java
public static final int ITEM_LIMIT = 17000;         // Maximum item ID
public static final int MAXITEM_AMOUNT = Integer.MAX_VALUE; // Max item stack
public static final int NO_TELEPORT_WILD_LEVEL = 20; // Teleport restriction level
```

#### game.entities.NPC Behavior
```java
public static final int NPC_RANDOM_WALK_DISTANCE = 5;  // Random walk radius
public static final int NPC_FOLLOW_DISTANCE = 10;      // Follow distance
```

### File System and Directories

#### Directory Paths
```java
public static final String FILE_SYSTEM_DIR = "./data/cache/";  // Cache directory
public static final String SERVER_LOG_DIR = "./data/logs/";   // Log directory
```

### Security and Encryption

#### RSA Encryption Keys
```java
public static final BigInteger RSA_EXPONENT = new BigInteger("...");
public static final BigInteger RSA_MODULUS = new BigInteger("...");
```

These keys are used for secure client-server communication during the login process.

## Skill System Constants

### Skill IDs
```java
public static final int ATTACK = 0;
public static final int DEFENCE = 1;
public static final int STRENGTH = 2;
public static final int HITPOINTS = 3;
public static final int RANGED = 4;
public static final int PRAYER = 5;
public static final int MAGIC = 6;
public static final int COOKING = 7;
public static final int WOODCUTTING = 8;
public static final int FLETCHING = 9;
public static final int FISHING = 10;
public static final int FIREMAKING = 11;
public static final int CRAFTING = 12;
public static final int SMITHING = 13;
public static final int MINING = 14;
public static final int HERBLORE = 15;
public static final int AGILITY = 16;
public static final int THIEVING = 17;
public static final int SLAYER = 18;
public static final int FARMING = 19;
public static final int RUNECRAFTING = 20;
```

These constants are used throughout the server to reference specific skills in arrays and calculations.

## Interface and UI Constants

### Sidebar Interfaces
```java
public static int[] SIDEBARS = { 
    2423,  // Attack tab
    3917,  // game.mechanics.Skills tab
    638,   // Quest tab
    3213,  // Inventory tab
    1644,  // Equipment tab
    5608,  // Prayer tab
    1151,  // Magic tab
    18128, // Clan tab
    5065,  // Friends tab
    5715,  // Ignore tab
    2449,  // Logout tab
    904,   // Settings tab
    147,   // Emotes tab
    962    // Music tab
};
```

## game.entities.NPC Classification Systems

### Boss NPCs
```java
public static final LinkedHashSet<Integer> BOSS_NPC_IDS = new LinkedHashSet<>(Arrays.asList(
    StaticNpcList.CHAOS_ELEMENTAL,
    StaticNpcList.DAGANNOTH_REX,
    StaticNpcList.DAGANNOTH_PRIME,
    StaticNpcList.DAGANNOTH_SUPREME,
    StaticNpcList.GIANT_MOLE,
    StaticNpcList.KING_BLACK_DRAGON,
    StaticNpcList.KALPHITE_QUEEN,
    StaticNpcList.TZTOKJAD
));
```

### Slayer Monsters
```java
public static final LinkedHashSet<Integer> SLAYER_NPC_IDS = new LinkedHashSet<>(Arrays.asList(
    StaticNpcList.CRAWLING_HAND,
    StaticNpcList.CAVE_BUG,
    StaticNpcList.CAVE_CRAWLER,
    StaticNpcList.BANSHEE,
    StaticNpcList.ROCKSLUG,
    StaticNpcList.COCKATRICE,
    StaticNpcList.PYREFIEND,
    StaticNpcList.BASILISK,
    StaticNpcList.JELLY,
    StaticNpcList.TUROTH,
    StaticNpcList.ABERRANT_SPECTER,
    StaticNpcList.DUST_DEVIL,
    StaticNpcList.KURASK,
    StaticNpcList.SKELETAL_WYVERN,
    StaticNpcList.GARGOYLE,
    StaticNpcList.NECHRYAEL,
    StaticNpcList.ABYSSAL_DEMON,
    StaticNpcList.DARK_BEAST
    // ... and many more
));
```

### Undead Creatures
```java
public static final String[] UNDEAD = {
    "armoured zombie", "ankous", "banshee", "crawling hand", 
    "dried zombie", "ghost", "ghostly warrior", "ghast",
    "mummy", "mighty banshee", "shade", "skeleton", 
    "zombie", "zombie rat", "zogre"
    // ... complete list for undead detection
};
```

## Special game.items.Item Arrays

### Fun Weapons (Dueling)
```java
public static final int[] FUN_WEAPONS = { 
    2460, 2461, 2462, 2463, 2464, 2465, 2466, 2467, 
    2468, 2469, 2470, 2471, 2471, 2473, 2474, 2475, 
    2476, 2477 
};
```

These are special weapons allowed in certain dueling scenarios.

## Usage Examples

### Accessing Server Configuration
```java
// Check server settings
if (Constants.SERVER_DEBUG) {
    System.out.println("Debug mode enabled");
}

// Get server information
String serverName = Constants.SERVER_NAME;
int maxPlayers = Constants.MAX_PLAYERS;
double xpRate = Constants.XP_RATE;
```

### Using Skill Constants
```java
// Award experience to a skill
player.getPlayerAssistant().addSkillXP(1000, Constants.ATTACK);

// Check skill level
int attackLevel = player.playerLevel[Constants.ATTACK];
int defenceXP = player.playerXP[Constants.DEFENCE];

// Refresh skill interface
player.getPlayerAssistant().refreshSkill(Constants.HITPOINTS);
```

### game.entities.NPC Classification Checks
```java
// Check if game.entities.NPC is a boss
if (Constants.BOSS_NPC_IDS.contains(npcType)) {
    // Handle boss mechanics
    player.bossKillCount++;
}

// Check if game.entities.NPC is a slayer monster
if (Constants.SLAYER_NPC_IDS.contains(npcType)) {
    // Award slayer experience
    player.getPlayerAssistant().addSkillXP(damage * 4, Constants.SLAYER);
}

// Check if game.entities.NPC is undead
boolean isUndead = false;
String npcName = NpcHandler.getNpcListName(npcType).toLowerCase();
for (String undeadName : Constants.UNDEAD) {
    if (npcName.contains(undeadName)) {
        isUndead = true;
        break;
    }
}
```

### Network and Performance Settings
```java
// Check connection limits
if (connectionCount > Constants.IPS_ALLOWED) {
    // Reject connection
}

// Use buffer size for streams
byte[] buffer = new byte[Constants.BUFFER_SIZE];

// Check cycle timing
if (cycleTime > Constants.CYCLE_TIME) {
    System.out.println("Cycle running slow!");
}
```

### Feature Toggles
```java
// Check if features are enabled
if (Constants.CLUES_ENABLED) {
    // Process clue scroll logic
}

if (Constants.TUTORIAL_ISLAND) {
    // Show tutorial for new players
}

if (Constants.ITEM_REQUIREMENTS) {
    // Check item requirements before equipping
}
```

## Configuration Management

### Runtime Configuration
Many constants can be modified at server startup using the ConfigLoader:

```java
// These values can be overridden by config files
Constants.SERVER_NAME = "My Custom Server";
Constants.XP_RATE = 5.0;
Constants.MAX_PLAYERS = 500;
```

### Environment-Specific Settings
```java
// Development vs Production settings
if (Constants.SERVER_DEBUG) {
    // Enable debug features
    Constants.CYCLE_LOGGING = true;
    Constants.GUI_ENABLED = true;
} else {
    // Production optimizations
    Constants.CYCLE_LOGGING = false;
    Constants.GUI_ENABLED = false;
}
```

## Best Practices

### Using Constants
1. **Always use constants** instead of magic numbers
2. **Group related constants** logically
3. **Use descriptive names** that explain the purpose
4. **Document complex constants** with comments
5. **Consider configurability** for server operators

### Modifying Constants
1. **Test thoroughly** after changing values
2. **Consider dependencies** between constants
3. **Update documentation** when adding new constants
4. **Use appropriate data types** for the values
5. **Validate ranges** for numeric constants

### Performance Considerations
1. **Use final for true constants** to enable compiler optimizations
2. **Avoid expensive operations** in constant initialization
3. **Consider memory usage** for large arrays
4. **Use appropriate collection types** for lookups

## Integration Points

### ConfigLoader Integration
```java
// Constants can be loaded from external configuration
ConfigLoader.loadSettings("server.json");
// This may modify Constants values at runtime
```

### Server Startup
```java
// Constants are used throughout server initialization
GameEngine engine = new GameEngine();
engine.setMaxPlayers(Constants.MAX_PLAYERS);
engine.setCycleTime(Constants.CYCLE_TIME);
```

### core.engine.Game Systems
```java
// game.mechanics.Skills system uses skill constants
for (int skill = 0; skill <= Constants.RUNECRAFTING; skill++) {
    player.getPlayerAssistant().refreshSkill(skill);
}

// Combat system uses game.entities.NPC classifications
if (Constants.BOSS_NPC_IDS.contains(npc.npcType)) {
    // Apply boss-specific mechanics
}
```

## Related Classes

- [`ConfigLoader`](ConfigLoader.md) - Loads external configuration to override constants
- [`GameEngine`](GameEngine.md) - Uses constants for server initialization
- [`game.entities.Player`](game.entities.Player.md) - Uses skill constants and game settings
- [`NpcHandler`](NpcHandler.md) - Uses game.entities.NPC classification constants
- All server classes - Reference constants for configuration values
