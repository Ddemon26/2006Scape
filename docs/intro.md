# Introduction to 2006Scape

2006Scape is a comprehensive open-source recreation of RuneScape as it existed in 2006, widely considered the golden age of the game. Our mission is to faithfully preserve the authentic gameplay experience while leveraging modern development practices and tools.

## Project Vision

### Authenticity First
We strive to recreate the exact mechanics, content, and feel of 2006-era RuneScape:
- **Combat System**: Authentic melee, ranged, and magic combat with original formulas
- **game.mechanics.Skills**: All 23 skills with period-accurate training methods and rates
- **Quests**: Complete quest implementations with original dialogue and rewards
- **Economy**: Balanced item values and trade mechanics from the era

### Modern Development
While preserving the classic gameplay, we embrace modern development standards:
- **Open Source**: Full transparency with community-driven development
- **Clean Code**: Well-documented, maintainable codebase
- **Active Development**: Regular updates and community contributions
- **Cross-Platform**: Runs on Windows, macOS, and Linux

## Project Architecture

### Three-Component System

#### 🖥️ [core.engine.Game Server](Server/Server-intro.md)
The heart of 2006Scape, handling all game logic and world simulation:
- **Technology**: Java-based server with custom networking
- **Features**: Complete game world, combat, skills, quests, and player management
- **Scale**: Supports hundreds of concurrent players
- **Extensibility**: Plugin system for custom content

#### 🎮 [core.engine.Game Client](Client/client-intro.md)
The player-facing application providing the complete game experience:
- **Technology**: Java client with 3D rendering engine
- **Features**: Full 3D world, user interface, audio system, and networking
- **Compatibility**: Based on original RuneScape client architecture
- **Performance**: Optimized for smooth gameplay on modern systems

#### 🤖 [Parabot Integration](Parabot/Parabot-intro.md)
Scripting environment for automation and testing:
- **Purpose**: Automated testing and bot development
- **Integration**: Seamless connection to local development servers
- **Scripting**: Full API access for custom automation scripts
- **Testing**: Essential tool for validating game mechanics

## What Makes 2006Scape Special

### Community-Driven
- **Open Development**: All code is publicly available on GitHub
- **Community Input**: Features and fixes driven by player feedback
- **Collaborative**: Welcoming environment for new contributors
- **Transparent**: Development discussions happen in public Discord channels

### Technical Excellence
- **Clean Architecture**: Well-structured codebase with clear separation of concerns
- **Comprehensive Documentation**: Detailed docs for all 400+ server and 80+ client classes
- **Modern Tooling**: Maven builds, automated testing, and CI/CD pipelines
- **Performance**: Optimized for stability and low latency

### Authentic Experience
- **Period Accuracy**: Extensive research to match 2006 mechanics exactly
- **Content Complete**: All major content from the 2006 era implemented
- **Bug Compatibility**: Even preserves some original quirks and behaviors
- **Visual Fidelity**: Authentic graphics, sounds, and user interface

## Development Philosophy

### Quality Over Speed
We prioritize correctness and maintainability over rapid feature addition:
- **Code Review**: All changes undergo peer review
- **Testing**: Comprehensive testing before release
- **Documentation**: Every feature is properly documented
- **Stability**: Focus on reliable, bug-free gameplay

### Community First
The project exists to serve the RuneScape community:
- **game.entities.Player Feedback**: Regular surveys and feedback collection
- **Open Communication**: Transparent development process
- **Accessibility**: Easy setup for both players and developers
- **Education**: Detailed guides and documentation for contributors

## Getting Involved

### For Players
- **[Download and Play](https://2006scape.org)**: Join thousands of players online
- **[Community Discord](https://discord.gg/hZ6VfWG)**: Connect with other players and developers
- **[Report Issues](https://github.com/2006-Scape/2006rebotted/issues)**: Help improve the game
- **[Community Wiki](https://wiki.2006scape.org)**: Learn game mechanics and strategies

### For Developers
- **[Setup Guide](usage.md)**: Get the development environment running
- **[Architecture Docs](Server/Server-intro.md)**: Understand the codebase structure
- **[Contributing Guide](contributing.md)**: Learn our development workflow
- **[API Reference](Server/classes/index.md)**: Explore the complete class documentation

### For Content Creators
- **[Parabot Scripting](Parabot/Parabot-intro.md)**: Create automation tools
- **Custom Content**: Develop plugins and modifications
- **Documentation**: Help improve guides and tutorials
- **Community Events**: Organize and participate in community activities

## Project History

### Origins
2006Scape began as a passion project to preserve the beloved 2006 era of RuneScape. The original game from this period is no longer officially available, making preservation efforts like ours crucial for gaming history.

### Evolution
What started as a small recreation project has grown into a comprehensive game server with:
- **Active game.entities.Player Base**: Thousands of regular players
- **Rich Content**: Hundreds of quests, skills, and activities
- **Stable Infrastructure**: Reliable servers and regular updates
- **Growing Community**: Active development and player communities

### Future Vision
We continue to evolve while staying true to our core mission:
- **Content Completion**: Implementing remaining 2006-era content
- **Quality Improvements**: Ongoing bug fixes and optimizations
- **Community Growth**: Expanding our player and developer base
- **Preservation**: Ensuring this piece of gaming history remains accessible

## Technical Highlights

### Server Capabilities
- **Concurrent Players**: Supports 500+ simultaneous players
- **core.engine.Game Mechanics**: All 23 skills, 100+ quests, complete combat system
- **World Simulation**: Full game world with NPCs, objects, and interactions
- **Data Persistence**: game.entities.Player progress saved across sessions

### Client Features
- **3D Rendering**: Complete 3D world with authentic graphics
- **Audio System**: MIDI music and sound effects
- **User Interface**: All original interfaces and menus
- **Network Protocol**: Efficient client-server communication

### Development Tools
- **Build System**: Maven-based builds with dependency management
- **Documentation**: Comprehensive API docs and guides
- **Testing**: Automated testing with Parabot integration
- **Version Control**: Git-based development with GitHub hosting

## Join the Adventure

Whether you're a nostalgic player wanting to relive the golden age of RuneScape, a developer interested in game server architecture, or someone passionate about preserving gaming history, 2006Scape welcomes you.

**Ready to start?** Check out our [Getting Started Guide](usage.md) or jump straight into the [Server](Server/Server-intro.md) and [Client](Client/client-intro.md) documentation.

**Questions?** Join our [Discord community](https://discord.gg/hZ6VfWG) where thousands of players and developers are ready to help.

Welcome to 2006Scape – where the adventure never ends! 🏰⚔️

