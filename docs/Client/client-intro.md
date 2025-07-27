# Client Architecture

The 2006Scape client provides the complete user interface and 3D world rendering for players. Based on the original RuneScape revision 289 client, it's been enhanced with modern improvements while maintaining authentic 2006-era gameplay experience.

## Core Architecture

### Main Client System
The [`Client`](classes/Client.md) class serves as the central hub, coordinating all client subsystems including rendering, networking, input handling, and game state management.

**Key responsibilities:**
- core.engine.Game loop and frame rendering
- Server communication
- User input processing
- Interface management
- Audio playback

### Rendering Engine

#### 3D World Rendering
The client features a complete 3D rendering system for the game world:
- [`core.world.WorldController`](classes/core.world.WorldController.md) - Manages 3D world rendering and scene graph
- [`render.geometry.Model`](classes/render.geometry.Model.md) - 3D model representation and rendering
- [`render.objects.Ground`](classes/render.objects.Ground.md) - Terrain and floor rendering
- [`core.managers.ObjectManager`](classes/core.managers.ObjectManager.md) - World object placement and rendering

#### Graphics and UI
- [`render.core.DrawingArea`](classes/render.core.DrawingArea.md) - 2D graphics rendering utilities
- [`core.renderers.RSImageProducer`](classes/core.renderers.RSImageProducer.md) - Image rendering and manipulation
- [`render.core.Sprite`](classes/render.core.Sprite.md) - 2D sprite handling
- [`core.renderers.TextDrawingArea`](classes/core.renderers.TextDrawingArea.md) - Text rendering system

### core.engine.Game Entities

#### Players and NPCs
- [`game.entities.Player`](classes/game.entities.Player.md) - Represents other players in the game world
- [`game.entities.NPC`](classes/game.entities.NPC.md) - Non-player character representation
- [`game.entities.Entity`](classes/game.entities.Entity.md) - Base class for all game entities

#### Items and Objects
- [`game.items.Item`](classes/game.items.Item.md) - Individual item representation
- [`game.definitions.ItemDef`](classes/game.definitions.ItemDef.md) - game.items.Item definitions and properties
- [`game.definitions.ObjectDef`](classes/game.definitions.ObjectDef.md) - World object definitions

### User Interface System

#### Interface Management
- [`core.renderers.RSInterface`](classes/core.renderers.RSInterface.md) - core.engine.Game interface components and widgets
- [`core.handlers.TextInput`](classes/core.handlers.TextInput.md) - Text input handling
- [`core.handlers.MouseDetection`](classes/core.handlers.MouseDetection.md) - Mouse input processing

#### core.engine.Game Screens
The client handles various game screens and interfaces:
- Login screen
- core.engine.Game world view
- Inventory management
- Chat system
- Settings panels

## Network Architecture

### Server Communication
The client communicates with the [game server](../Server/Server-intro.md) using a binary protocol:

- [`core.network.RSSocket`](classes/core.network.RSSocket.md) - Low-level network socket handling
- [`core.network.Stream`](classes/core.network.Stream.md) - Data serialization and packet handling
- [`core.managers.OnDemandFetcher`](classes/core.managers.OnDemandFetcher.md) - Asset downloading from server

### Protocol Integration
- **Login Process**: Authenticates with server and receives initial game state
- **core.engine.Game Updates**: Receives world state changes and entity updates
- **User Actions**: Sends player input and actions to server

## Audio System

### Music and Sound
Complete audio system supporting MIDI music and sound effects:
- [`audio.MidiPlayer`](classes/audio.MidiPlayer.md) - MIDI music playback
- [`audio.SoundPlayer`](classes/audio.SoundPlayer.md) - Sound effect management
- [`audio.Sounds`](classes/audio.Sounds.md) - Audio asset management

### Audio Components
- [`audio.Instrument`](classes/audio.Instrument.md) - MIDI instrument handling
- [`audio.SoundFilter`](classes/audio.SoundFilter.md) - Audio processing and effects

## Data Management

### Asset Loading
- [`core.managers.OnDemandData`](classes/core.managers.OnDemandData.md) - Asset data management
- [`core.network.StreamLoader`](classes/core.network.StreamLoader.md) - Resource loading utilities
- [`util.collections.MRUCache`](classes/util.collections.MRUCache.md) - Most Recently Used cache for assets

### core.engine.Game Data
- [`game.definitions.ItemDef`](classes/game.definitions.ItemDef.md) - game.items.Item definitions and stats
- [`game.definitions.ObjectDef`](classes/game.definitions.ObjectDef.md) - World object properties
- [`game.definitions.EntityDef`](classes/game.definitions.EntityDef.md) - game.entities.Entity definitions

## Input and Controls

### User Input
- [`core.handlers.MouseDetection`](classes/core.handlers.MouseDetection.md) - Mouse click and movement handling
- [`core.handlers.TextInput`](classes/core.handlers.TextInput.md) - Keyboard input for chat and commands
- core.engine.Game controls for movement, interaction, and interface navigation

### core.engine.Game Interaction
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
- [`core.world.WorldController`](classes/core.world.WorldController.md) - 3D world rendering
- [`core.renderers.RSInterface`](classes/core.renderers.RSInterface.md) - User interface system
- [`game.entities.Player`](classes/game.entities.Player.md) - game.entities.Player representation
- [`render.geometry.Model`](classes/render.geometry.Model.md) - 3D model system

## Integration with Server

The client works closely with the [game server](../Server/Server-intro.md):

- **Authentication**: Client sends login credentials to server
- **World State**: Server sends game world updates to client
- **User Actions**: Client sends player input to server for processing
- **Asset Loading**: Client downloads game assets from server

## External Resources

- [Client Setup Guide](../usage.md)
- [Server Architecture](../Server/Server-intro.md)
- [Parabot Integration](../Parabot/Parabot-intro.md)
- [Contributing Guidelines](../contributing.md)
- [Project Repository](https://github.com/2006-Scape/2006rebotted)
