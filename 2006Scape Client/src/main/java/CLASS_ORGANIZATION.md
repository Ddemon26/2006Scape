# Class Organization Guide

This guide shows where to move each class file based on program flow and functionality.

## Keep in Root (`java/`)
- `Client.java` - Main client entry point
- `Main.java` - Application main entry point  
- `LocalGame.java` - Local testing entry point

## Core Engine (`core/`)

### `core/engine/`
- `core.engine.Game.java` - Main game loop and core engine
- `core.engine.RSApplet.java` - Applet framework
- `core.engine.RSFrame.java` - Main application frame
- `core.engine.ClientSettings.java` - core.engine.Game settings and configuration
- `core.engine.SizeConstants.java` - Screen and UI size constants

### `core/managers/`
- `core.managers.ObjectManager.java` - High-level object management
- `core.managers.OnDemandFetcher.java` - Cache and data fetching manager
- `core.managers.OnDemandFetcherParent.java` - Cache fetcher parent interface
- `core.managers.OnDemandData.java` - On-demand data management

### `core/handlers/`
- `core.handlers.MouseDetection.java` - Mouse input handling
- `core.handlers.TextInput.java` - Text input processing
- `core.handlers.Signlink.java` - Native system interface

### `core/renderers/`
- `core.renderers.RSImageProducer.java` - Image rendering producer
- `core.renderers.TextClass.java` - Text rendering utilities
- `core.renderers.TextDrawingArea.java` - Text drawing operations
- `core.renderers.RSInterface.java` - UI interface rendering

### `core/network/`
- `core.network.Stream.java` - Network data streaming
- `core.network.StreamLoader.java` - core.network.Stream loading utilities
- `core.network.RSSocket.java` - Network socket management

### `core/world/`
- `core.world.WorldController.java` - World state management
- `core.world.CollisionMap.java` - Collision detection and pathfinding
- `core.world.TileRotation.java` - Tile rotation utilities
- `core.world.CullingCluster.java` - Rendering culling optimization

## core.engine.Game Systems (`game/`)

### `game/entities/`
- `game.entities.Animable.java` - Base animatable entity
- `game.entities.Entity.java` - Base game entity
- `game.entities.Player.java` - game.entities.Player character
- `game.entities.NPC.java` - Non-player character
- `game.entities.Projectile.java` - game.entities.Projectile entities
- `game.entities.GraphicsObject.java` - Graphic effect objects
- `game.entities.PendingSpawn.java` - game.entities.Entity spawning queue

### `game/definitions/`
- `game.definitions.EntityDef.java` - game.entities.Entity definition loader
- `game.definitions.ItemDef.java` - game.items.Item definition loader
- `game.definitions.ObjectDef.java` - Object definition loader
- `game.definitions.IDK.java` - Identity kit definitions
- `game.definitions.SpotAnim.java` - Spot animation definitions
- `game.definitions.VarBit.java` - Variable bit definitions
- `game.definitions.Varp.java` - Variable parameter definitions

### `game/animation/`
- `game.animation.Animation.java` - game.animation.Animation system
- `game.animation.AnimFrame.java` - game.animation.Animation frame data
- `game.animation.FrameBase.java` - game.animation.Animation frame base

### `game/items/`
- `game.items.Item.java` - game.items.Item representation
- `game.items.ItemPile.java` - render.objects.Ground item piles

### `game/world/`
- `game.world.SceneObject.java` - Scene object management
- `game.world.BoundaryObject.java` - Boundary objects
- `game.world.WallDecoration.java` - Wall decoration objects
- `game.world.TileDecoration.java` - Tile decoration objects
- `game.world.DynamicObject.java` - Dynamic world objects

### `game/mechanics/`
- `game.mechanics.Skills.java` - game.mechanics.Skills system

## Rendering (`render/`)

### `render/core/`
- `render.core.DrawingArea.java` - 2D drawing primitives
- `render.core.Sprite.java` - render.core.Sprite rendering
- `render.core.Background.java` - render.core.Background rendering
- `render.core.Texture.java` - render.core.Texture management

### `render/geometry/`
- `render.geometry.Model.java` - 3D model rendering
- `render.geometry.ModelHeader.java` - render.geometry.Model header data
- `render.geometry.VertexNormal.java` - Vertex normal calculations

### `render/objects/`
- `render.objects.Ground.java` - render.objects.Ground rendering

### `render/tiles/`
- `render.tiles.PlainTile.java` - Plain tile rendering
- `render.tiles.ShapedTile.java` - Shaped tile rendering
- `render.tiles.FloorOverlay.java` - Floor overlay rendering

### `render/effects/`
- Currently no specific effect classes identified

## Utilities (`util/`)

### `util/collections/`
- `util.collections.Node.java` - Base node for collections
- `util.collections.NodeSub.java` - Sub-node implementation
- `util.collections.NodeList.java` - util.collections.Node-based list
- `util.collections.NodeSubList.java` - Sub-node list
- `util.collections.NodeHashTable.java` - util.collections.Node-based hash table
- `util.collections.MRUCache.java` - Most Recently Used cache

### `util/compression/`
- `util.compression.BZip2Decompressor.java` - BZip2 decompression
- `util.compression.BZip2State.java` - BZip2 decompression state
- `util.compression.Decompressor.java` - Generic decompression interface
- `util.compression.CachePlaceholder.java` - Cache placeholder utilities

### `util/cryptography/`
- `util.cryptography.ISAACRandomGen.java` - ISAAC random number generation

### `util/helpers/`
- `util.helpers.Censor.java` - Text censoring utilities

### `util/audio/`
- `audio.AbstractMidiController.java` - Abstract MIDI controller
- `audio.Midi.java` - MIDI file handling
- `audio.MidiFile.java` - MIDI file representation
- `audio.MidiHandler.java` - MIDI event handling
- `audio.MidiPlayer.java` - MIDI playback
- `audio.QueuedMidiPlayer.java` - Queued MIDI player
- `audio.SystemMidiPlayer.java` - System MIDI player
- `audio.Instrument.java` - Musical instrument data
- `audio.SoundEnvelope.java` - Sound envelope processing
- `audio.SoundFilter.java` - Sound filtering
- `audio.SoundPlayer.java` - Sound playback
- `audio.Sounds.java` - Sound management
- `audio.MuLawInputStream.java` - Mu-law audio decoding

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