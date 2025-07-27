# Class Organization Guide

This guide shows where to move each class file based on program flow and functionality.

## Keep in Root (`java/`)
- `Client.java` - Main client entry point
- `Main.java` - Application main entry point  
- `LocalGame.java` - Local testing entry point

## Core Engine (`core/`)

### `core/engine/`
- `Game.java` - Main game loop and core engine
- `RSApplet.java` - Applet framework
- `RSFrame.java` - Main application frame
- `ClientSettings.java` - Game settings and configuration
- `SizeConstants.java` - Screen and UI size constants

### `core/managers/`
- `ObjectManager.java` - High-level object management
- `OnDemandFetcher.java` - Cache and data fetching manager
- `OnDemandFetcherParent.java` - Cache fetcher parent interface
- `OnDemandData.java` - On-demand data management

### `core/handlers/`
- `MouseDetection.java` - Mouse input handling
- `TextInput.java` - Text input processing
- `Signlink.java` - Native system interface

### `core/renderers/`
- `RSImageProducer.java` - Image rendering producer
- `TextClass.java` - Text rendering utilities
- `TextDrawingArea.java` - Text drawing operations
- `RSInterface.java` - UI interface rendering

### `core/network/`
- `Stream.java` - Network data streaming
- `StreamLoader.java` - Stream loading utilities
- `RSSocket.java` - Network socket management

### `core/world/`
- `WorldController.java` - World state management
- `CollisionMap.java` - Collision detection and pathfinding
- `TileRotation.java` - Tile rotation utilities
- `CullingCluster.java` - Rendering culling optimization

## Game Systems (`game/`)

### `game/entities/`
- `Animable.java` - Base animatable entity
- `Entity.java` - Base game entity
- `Player.java` - Player character
- `NPC.java` - Non-player character
- `Projectile.java` - Projectile entities
- `GraphicsObject.java` - Graphic effect objects
- `PendingSpawn.java` - Entity spawning queue

### `game/definitions/`
- `EntityDef.java` - Entity definition loader
- `ItemDef.java` - Item definition loader
- `ObjectDef.java` - Object definition loader
- `IDK.java` - Identity kit definitions
- `SpotAnim.java` - Spot animation definitions
- `VarBit.java` - Variable bit definitions
- `Varp.java` - Variable parameter definitions

### `game/animation/`
- `Animation.java` - Animation system
- `AnimFrame.java` - Animation frame data
- `FrameBase.java` - Animation frame base

### `game/items/`
- `Item.java` - Item representation
- `ItemPile.java` - Ground item piles

### `game/world/`
- `SceneObject.java` - Scene object management
- `BoundaryObject.java` - Boundary objects
- `WallDecoration.java` - Wall decoration objects
- `TileDecoration.java` - Tile decoration objects
- `DynamicObject.java` - Dynamic world objects

### `game/mechanics/`
- `Skills.java` - Skills system

## Rendering (`render/`)

### `render/core/`
- `DrawingArea.java` - 2D drawing primitives
- `Sprite.java` - Sprite rendering
- `Background.java` - Background rendering
- `Texture.java` - Texture management

### `render/geometry/`
- `Model.java` - 3D model rendering
- `ModelHeader.java` - Model header data
- `VertexNormal.java` - Vertex normal calculations

### `render/objects/`
- `Ground.java` - Ground rendering

### `render/tiles/`
- `PlainTile.java` - Plain tile rendering
- `ShapedTile.java` - Shaped tile rendering
- `FloorOverlay.java` - Floor overlay rendering

### `render/effects/`
- Currently no specific effect classes identified

## Utilities (`util/`)

### `util/collections/`
- `Node.java` - Base node for collections
- `NodeSub.java` - Sub-node implementation
- `NodeList.java` - Node-based list
- `NodeSubList.java` - Sub-node list
- `NodeHashTable.java` - Node-based hash table
- `MRUCache.java` - Most Recently Used cache

### `util/compression/`
- `BZip2Decompressor.java` - BZip2 decompression
- `BZip2State.java` - BZip2 decompression state
- `Decompressor.java` - Generic decompression interface
- `CachePlaceholder.java` - Cache placeholder utilities

### `util/cryptography/`
- `ISAACRandomGen.java` - ISAAC random number generation

### `util/helpers/`
- `Censor.java` - Text censoring utilities

### `util/audio/`
- `AbstractMidiController.java` - Abstract MIDI controller
- `Midi.java` - MIDI file handling
- `MidiFile.java` - MIDI file representation
- `MidiHandler.java` - MIDI event handling
- `MidiPlayer.java` - MIDI playback
- `QueuedMidiPlayer.java` - Queued MIDI player
- `SystemMidiPlayer.java` - System MIDI player
- `Instrument.java` - Musical instrument data
- `SoundEnvelope.java` - Sound envelope processing
- `SoundFilter.java` - Sound filtering
- `SoundPlayer.java` - Sound playback
- `Sounds.java` - Sound management
- `MuLawInputStream.java` - Mu-law audio decoding

## Directory Structure Created

```
java/
├── core/
│   ├── engine/
│   ├── managers/
│   ├── handlers/
│   ├── renderers/
│   ├── network/
│   └── world/
├── game/
│   ├── entities/
│   ├── definitions/
│   ├── animation/
│   ├── items/
│   ├── world/
│   └── mechanics/
├── render/
│   ├── core/
│   ├── geometry/
│   ├── objects/
│   ├── tiles/
│   └── effects/
└── util/
    ├── collections/
    ├── compression/
    ├── cryptography/
    ├── helpers/
    └── audio/
```

This organization follows the architectural patterns described in CLAUDE.md and groups classes by their functional responsibilities in the game's execution flow.