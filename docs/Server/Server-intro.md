# Server Architecture

The 2006Scape server is the heart of the game world, handling all game logic, player interactions, and world simulation. Built in Java with a focus on authenticity to the 2006 RuneScape era while maintaining modern code standards.

## Core Architecture

### Game Engine
The [`GameEngine`](classes/GameEngine.md) class serves as the main server loop, processing game ticks at 600ms intervals (matching the original game). It coordinates all server subsystems and manages the game world state.

**Key responsibilities:**
- Player session management
- NPC AI and movement
- Combat calculations
- Skill processing
- Quest progression
- World events

### Player Management
The [`Player`](classes/Player.md) class represents each connected player, containing their stats, inventory, location, and game state. The [`PlayerHandler`](classes/PlayerHandler.md) manages all active players and their interactions.

**Player systems:**
- Authentication and login ([`LoginDecoder`](classes/LoginDecoder.md))
- Character data persistence ([`PlayerSave`](classes/PlayerSave.md))
- Combat mechanics ([`CombatAssistant`](classes/CombatAssistant.md))
- Skill progression ([`SkillHandler`](classes/SkillHandler.md))

### World Systems

#### NPCs and Combat
- [`NpcHandler`](classes/NpcHandler.md) - Manages all non-player characters
- [`NpcCombat`](classes/NpcCombat.md) - NPC combat AI and mechanics
- [`CombatAssistant`](classes/CombatAssistant.md) - Player vs Player/NPC combat

#### Skills System
All 23 original skills are implemented with authentic mechanics:
- **Combat Skills**: [`MeleeData`](classes/MeleeData.md), [`RangeData`](classes/RangeData.md), [`MagicData`](classes/MagicData.md)
- **Gathering**: [`Mining`](classes/Mining.md), [`Fishing`](classes/Fishing.md), [`Woodcutting`](classes/Woodcutting.md)
- **Production**: [`Smithing`](classes/Smithing.md), [`Cooking`](classes/Cooking.md), [`Crafting`](classes/CraftingData.md)
- **Support**: [`Prayer`](classes/Prayer.md), [`Runecrafting`](classes/Runecrafting.md)

#### Quest System
Complete quest implementations with authentic dialogue and progression:
- [`QuestAssistant`](classes/QuestAssistant.md) - Quest management framework
- Individual quest classes: [`CooksAssistant`](classes/CooksAssistant.md), [`RestlessGhost`](classes/RestlessGhost.md), etc.

## Network Architecture

### Protocol Handling
The server uses a custom protocol based on the original RuneScape networking:
- [`RS2ProtocolDecoder`](classes/RS2ProtocolDecoder.md) - Incoming packet processing
- [`RS2ProtocolEncoder`](classes/RS2ProtocolEncoder.md) - Outgoing packet creation
- [`PacketHandler`](classes/PacketHandler.md) - Packet routing and validation

### Client Communication
- [`Client`](classes/Client.md) - Represents a connected client session
- [`PacketSender`](classes/PacketSender.md) - Sends data to clients
- [`Connection`](classes/Connection.md) - Low-level network connection management

## Data Management

### Game Data
- [`ItemDefinition`](classes/ItemDefinition.md) - Item properties and metadata
- [`NpcDefinition`](classes/NpcDefinition.md) - NPC stats and behavior
- [`ObjectDefinition`](classes/ObjectDefinition.md) - World object properties

### World State
- [`Region`](classes/Region.md) - Map region management
- [`GameObject`](classes/GameObject.md) - Interactive world objects
- [`GroundItem`](classes/GroundItem.md) - Items on the ground

## Getting Started

### Running the Server
1. Navigate to `2006Scape Server/src/main/java/com/rs2`
2. Run the [`GameEngine`](classes/GameEngine.md) class
3. Server starts on default port 43594

### Configuration
- [`ServerConfig.Sample.json`](../../2006Scape%20Server/ServerConfig.Sample.json) - Server configuration template
- [`Constants`](classes/Constants.md) - Game constants and settings

### Development
- **Source Location**: `2006Scape Server/src/main/java`
- **Build Tool**: Maven (`mvn clean install`)
- **Java Version**: Java 8+

## Integration with Client

The server communicates with the [game client](../Client/client-intro.md) using a binary protocol. Key integration points:

- **Login Process**: Server validates credentials and sends player data
- **Game Updates**: Server sends world state changes to client
- **User Input**: Client sends player actions to server for processing

## API Reference

Complete documentation for all 400+ server classes is available in the [Server Classes](classes/index.md) reference.

### Most Important Classes
- [`GameEngine`](classes/GameEngine.md) - Main server loop
- [`Player`](classes/Player.md) - Player representation
- [`Client`](classes/Client.md) - Client session
- [`CombatAssistant`](classes/CombatAssistant.md) - Combat system
- [`SkillHandler`](classes/SkillHandler.md) - Skill processing

## External Resources

- [Server Setup Guide](../usage.md)
- [Contributing Guidelines](../contributing.md)
- [Client Architecture](../Client/client-intro.md)
- [Project Repository](https://github.com/2006-Scape/2006rebotted)
