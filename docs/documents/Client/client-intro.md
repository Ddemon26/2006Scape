# Client Architecture

The 2006Scape client provides the complete user interface and 3D world rendering for players. Based on the original RuneScape revision 289 client, it's been enhanced with modern improvements while maintaining authentic 2006-era gameplay experience.

## Core Architecture

### Main Client System
The [`Client`](classes/Client.md) class serves as the central hub, coordinating all client subsystems including rendering, networking, input handling, and game state management.

**Key responsibilities:**
- Game loop and frame rendering
- Server communication
- User input processing
- Interface management
- Audio playback

### Rendering Engine

#### 3D World Rendering
The client features a complete 3D rendering system for the game world:
- [`WorldController`](classes/WorldController.md) - Manages 3D world rendering and scene graph
- [`Model`](classes/Model.md) - 3D model representation and rendering
- [`Ground`](classes/Ground.md) - Terrain and floor rendering
- [`ObjectManager`](classes/ObjectManager.md) - World object placement and rendering

#### Graphics and UI
- [`DrawingArea`](classes/DrawingArea.md) - 2D graphics rendering utilities
- [`RSImageProducer`](classes/RSImageProducer.md) - Image rendering and manipulation
- [`Sprite`](classes/Sprite.md) - 2D sprite handling
- [`TextDrawingArea`](classes/TextDrawingArea.md) - Text rendering system

### Game Entities

#### Players and NPCs
- [`Player`](classes/Player.md) - Represents other players in the game world
- [`NPC`](classes/NPC.md) - Non-player character representation
- [`Entity`](classes/Entity.md) - Base class for all game entities

#### Items and Objects
- [`Item`](classes/Item.md) - Individual item representation
- [`ItemDef`](classes/ItemDef.md) - Item definitions and properties
- [`ObjectDef`](classes/ObjectDef.md) - World object definitions

### User Interface System

#### Interface Management
- [`RSInterface`](classes/RSInterface.md) - Game interface components and widgets
- [`TextInput`](classes/TextInput.md) - Text input handling
- [`MouseDetection`](classes/MouseDetection.md) - Mouse input processing

#### Game Screens
The client handles various game screens and interfaces:
- Login screen
- Game world view
- Inventory management
- Chat system
- Settings panels

## Network Architecture

### Server Communication
The client communicates with the [game server](../Server/Server-intro.md) using a binary protocol:

- [`RSSocket`](classes/RSSocket.md) - Low-level network socket handling
- [`Stream`](classes/Stream.md) - Data serialization and packet handling
- [`OnDemandFetcher`](classes/OnDemandFetcher.md) - Asset downloading from server

### Protocol Integration
- **Login Process**: Authenticates with server and receives initial game state
- **Game Updates**: Receives world state changes and entity updates
- **User Actions**: Sends player input and actions to server

## Audio System

### Music and Sound
Complete audio system supporting MIDI music and sound effects:
- [`MidiPlayer`](classes/MidiPlayer.md) - MIDI music playback
- [`SoundPlayer`](classes/SoundPlayer.md) - Sound effect management
- [`Sounds`](classes/Sounds.md) - Audio asset management

### Audio Components
- [`Instrument`](classes/Instrument.md) - MIDI instrument handling
- [`SoundFilter`](classes/SoundFilter.md) - Audio processing and effects

## Data Management

### Asset Loading
- [`OnDemandData`](classes/OnDemandData.md) - Asset data management
- [`StreamLoader`](classes/StreamLoader.md) - Resource loading utilities
- [`MRUCache`](classes/MRUCache.md) - Most Recently Used cache for assets

### Game Data
- [`ItemDef`](classes/ItemDef.md) - Item definitions and stats
- [`ObjectDef`](classes/ObjectDef.md) - World object properties
- [`EntityDef`](classes/EntityDef.md) - Entity definitions

## Input and Controls

### User Input
- [`MouseDetection`](classes/MouseDetection.md) - Mouse click and movement handling
- [`TextInput`](classes/TextInput.md) - Keyboard input for chat and commands
- Game controls for movement, interaction, and interface navigation

### Game Interaction
- Point-and-click movement
- Object interaction
- Combat targeting
- Interface manipulation

## Getting Started

### Running the Client
1. Navigate to `2006Scape Client/src/main/java`
2. Run the [`Client`](classes/Client.md) class
3. Client connects to localhost:43594 by default

### Development Setup
- **Source Location**: `2006Scape Client/src/main/java`
- **Build Tool**: Maven (`mvn clean install`)
- **Java Version**: Java 8+
- **Dependencies**: Requires active [game server](../Server/Server-intro.md)

### Local Development
When running locally:
1. Start the [game server](../Server/Server-intro.md) first
2. Ensure server is running on `localhost:43594`
3. Launch client - it will automatically connect to local server
4. Use any credentials to login (local server accepts any login)

## Client Modes

### Production Mode
- Connects to official 2006Scape servers
- Downloads from [2006Scape.org](https://2006scape.org)
- Full authentication required

### Development Mode
- Connects to localhost server
- Accepts any login credentials
- Useful for testing and development

### Parabot Integration
The client can be used with [Parabot](../Parabot/Parabot-intro.md) for automation:
- Run Parabot with `-local` flag
- Connects to local development server
- Enables script testing and development

## API Reference

Complete documentation for all 80+ client classes is available in the [Client Classes](classes/index.md) reference.

### Most Important Classes
- [`Client`](classes/Client.md) - Main client controller
- [`WorldController`](classes/WorldController.md) - 3D world rendering
- [`RSInterface`](classes/RSInterface.md) - User interface system
- [`Player`](classes/Player.md) - Player representation
- [`Model`](classes/Model.md) - 3D model system

## Integration with Server

The client works closely with the [game server](../Server/Server-intro.md):

- **Authentication**: Client sends login credentials to server
- **World State**: Server sends game world updates to client
- **User Actions**: Client sends player input to server for processing
- **Asset Loading**: Client downloads game assets from server

## External Resources

- [Client Setup Guide](../../usage.md)
- [Server Architecture](../Server/Server-intro.md)
- [Parabot Integration](../Parabot/Parabot-intro.md)
- [Contributing Guidelines](../community/contributing.md)
- [Project Repository](https://github.com/2006-Scape/2006rebotted)
