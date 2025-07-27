# 2006Scape Documentation

Welcome to the comprehensive documentation for 2006Scape, an open source RuneScape emulation server targeting the 2006 era. This documentation covers everything from getting started to advanced development topics.

## Quick Start

New to 2006Scape? Start here:

1. **[Getting Started](usage.md)** - Download and run the game
2. **[Project Overview](intro.md)** - Learn about the project structure
3. **[Contributing Guide](contributing.md)** - Join the development community

## Architecture Overview

2006Scape consists of three main components:

### 🖥️ [Server](documents/Server/Server-intro.md)
The game server handles all game logic, player interactions, and world simulation. Built in Java with a focus on 2006-era mechanics.

- **[Server Architecture](documents/Server/Server-intro.md)** - Core concepts and design
- **[Server Classes](documents/Server/classes/index.md)** - Complete API reference
- **Key Components:**
  - core.engine.Game Engine - Core server loop and event handling
  - game.entities.Player Management - Authentication, sessions, and data persistence  
  - Combat System - Melee, ranged, and magic combat mechanics
  - game.mechanics.Skills System - All 23 skills with authentic mechanics
  - Quest System - Story-driven content and progression

### 🎮 [Client](documents/Client/client-intro.md)
The game client provides the user interface and renders the 3D world. Based on the original RuneScape client with modern improvements.

- **[Client Architecture](documents/Client/client-intro.md)** - Rendering and networking
- **[Client Classes](documents/Client/classes/index.md)** - Complete API reference
- **Key Components:**
  - Rendering Engine - 3D world and UI rendering
  - Network Protocol - Server communication
  - Input Handling - Mouse and keyboard interaction
  - Audio System - Music and sound effects

### 🤖 [Parabot](documents/Parabot/Parabot-intro.md)
Scripting client for automation and testing. Allows developers to create bots for testing game mechanics.

- **[Parabot Setup](documents/Parabot/Parabot-intro.md)** - Installation and usage
- **Scripting API** - Create custom automation scripts

## Development Resources

### For Players
- **[Download & Play](https://2006scape.org)** - Official client download
- **[Community Wiki](https://wiki.2006scape.org)** - core.engine.Game guides and information

### For Developers
- **[Development Setup](usage.md)** - Build from source
- **[Contributing Guidelines](contributing.md)** - Code standards and workflow
- **[Server Classes](documents/Server/classes/index.md)** - 400+ documented server classes
- **[Client Classes](documents/Client/classes/index.md)** - 80+ documented client classes

### Community & Support
- **[Discord Community](https://discord.gg/hZ6VfWG)** - Real-time chat and support
- **[GitHub Repository](https://github.com/2006-Scape/2006rebotted)** - Source code and issues
- **[Rune-Server Thread](https://www.rune-server.ee/forums/2006scape.318/)** - Development discussions

## Legal Notice

Please see our [legal disclaimer](disclaimer.md) regarding project affiliation and intellectual property.
