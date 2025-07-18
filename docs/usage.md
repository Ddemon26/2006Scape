# Getting Started with 2006Scape

This guide covers everything you need to know to get 2006Scape running, whether you want to play the game or contribute to development.

## For Players

### Quick Play
The fastest way to start playing is to download the official client:

1. **Download**: Get the latest client from [2006Scape.org](https://2006Scape.org/)
2. **Install**: Run the installer and follow the setup wizard
3. **Play**: Launch the client and create your account
4. **Join**: Connect with the community on [Discord](https://discord.gg/hZ6VfWG)

### System Requirements
- **Java**: Java 8 or higher
- **OS**: Windows, macOS, or Linux
- **Memory**: 2GB RAM minimum
- **Storage**: 1GB free space

## For Developers

### Prerequisites
Before you begin, ensure you have:
- **Java Development Kit (JDK) 8+**: [Download here](https://adoptopenjdk.net/?variant=openjdk8)
- **Maven**: For building the project
- **Git**: For cloning the repository
- **IDE**: IntelliJ IDEA (recommended) or Eclipse

### Quick Setup

#### 1. Clone the Repository
```bash
git clone https://github.com/2006-Scape/2006rebotted.git
cd 2006rebotted
```

#### 2. Build the Project
```bash
mvn clean install
```

#### 3. Import into IDE
**IntelliJ IDEA:**
1. Open IntelliJ IDEA
2. File → Open → Select the `2006rebotted` folder
3. Wait for Maven to import dependencies
4. File → Project Structure → Set Project SDK to Java 8

**Eclipse:**
1. File → Import → Existing Maven Projects
2. Browse to the `2006rebotted` folder
3. Import all projects

### Running the Server

#### Method 1: IDE (Recommended)
1. Navigate to `2006Scape Server/src/main/java/com/rs2`
2. Right-click `GameEngine.java`
3. Select "Run GameEngine.main()"
4. Server starts on port `43594`

#### Method 2: Command Line
```bash
cd "2006Scape Server"
mvn exec:java -Dexec.mainClass="com.rs2.GameEngine"
```

#### Server Configuration
Create a `ServerConfig.json` file based on `ServerConfig.Sample.json`:
```json
{
  "server_name": "Local Dev Server",
  "server_port": 43594,
  "mysql_enabled": false,
  "discord_enabled": false
}
```

### Running the Client

#### Method 1: IDE (Recommended)
1. **Start the server first** (see above)
2. Navigate to `2006Scape Client/src/main/java`
3. Right-click `Client.java`
4. Select "Run Client.main()"
5. Client connects to `localhost:43594`

#### Method 2: Command Line
```bash
cd "2006Scape Client"
mvn exec:java -Dexec.mainClass="Client"
```

#### Local Development Mode
When running locally:
- Use any username/password to login
- Server accepts all credentials in development mode
- No account registration required

### Project Structure

```
2006rebotted/
├── 2006Scape Server/          # Game server code
│   ├── src/main/java/com/rs2/ # Server source code
│   ├── data/                  # Game data files
│   └── pom.xml               # Maven configuration
├── 2006Scape Client/          # Game client code
│   ├── src/main/java/        # Client source code
│   └── pom.xml               # Maven configuration
├── docs/                      # Documentation
└── README.md                 # Project overview
```

## Development Workflow

### Making Changes

#### Server Development
1. **Core Logic**: Edit files in `2006Scape Server/src/main/java/com/rs2/`
2. **Game Data**: Modify files in `2006Scape Server/data/`
3. **Testing**: Restart server and test with local client

#### Client Development
1. **UI/Rendering**: Edit files in `2006Scape Client/src/main/java/`
2. **Testing**: Restart client (server can stay running)
3. **Assets**: Client downloads assets from server automatically

### Building and Testing

#### Full Build
```bash
# Build everything
mvn clean install

# Build specific module
cd "2006Scape Server"
mvn clean install
```

#### Running Tests
```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=YourTestClass
```

### Common Development Tasks

#### Adding New Features
1. **Server-side**: Add logic to appropriate handler classes
2. **Client-side**: Add UI elements and networking code
3. **Protocol**: Update packet handlers for client-server communication
4. **Testing**: Test with local server and client

#### Debugging
- **Server Logs**: Check console output for errors
- **Client Debug**: Enable debug mode in client settings
- **Network**: Monitor packet flow between client and server

## Using Parabot for Testing

[Parabot](Parabot/Parabot-intro.md) is useful for automated testing:

### Setup Parabot
1. Download from [Parabot releases](https://github.com/2006-Scape/Parabot/releases)
2. Start your local server
3. Run Parabot with local flag:
```bash
java -jar Parabot.jar -local
```

### Scripting
- Create automation scripts for testing game mechanics
- Test repetitive actions without manual input
- Validate server behavior under load

## Troubleshooting

### Common Issues

#### Server Won't Start
- **Check Java Version**: Ensure Java 8+ is installed
- **Port Conflicts**: Make sure port 43594 is available
- **Dependencies**: Run `mvn clean install` to resolve dependencies

#### Client Can't Connect
- **Server Running**: Ensure server is started first
- **Firewall**: Check if firewall is blocking connections
- **Port**: Verify server is running on port 43594

#### Build Failures
- **Clean Build**: Try `mvn clean install`
- **Java Version**: Ensure consistent Java version across tools
- **Dependencies**: Check internet connection for Maven downloads

### Getting Help
- **Discord**: Join our [Discord community](https://discord.gg/hZ6VfWG)
- **GitHub Issues**: Report bugs on [GitHub](https://github.com/2006-Scape/2006rebotted/issues)
- **Documentation**: Check our [comprehensive docs](index.md)

## Next Steps

### For New Developers
1. **Explore the Code**: Browse [Server](Server/Server-intro.md) and [Client](Client/client-intro.md) architecture
2. **Read Documentation**: Check out our [API references](Server/classes/index.md)
3. **Join Community**: Connect with other developers on [Discord](https://discord.gg/hZ6VfWG)
4. **Contribute**: See our [Contributing Guide](contributing.md)

### For Players
1. **Learn the Game**: Check the [Community Wiki](https://wiki.2006scape.org)
2. **Join Events**: Participate in community events
3. **Report Issues**: Help improve the game by reporting bugs
4. **Share Feedback**: Let us know what you think!

## External Resources

- **Official Website**: [2006Scape.org](https://2006scape.org)
- **Community Discord**: [Join here](https://discord.gg/hZ6VfWG)
- **GitHub Repository**: [Source code](https://github.com/2006-Scape/2006rebotted)
- **Community Wiki**: [Game guides](https://wiki.2006scape.org)
- **Rune-Server Forum**: [Development discussions](https://www.rune-server.ee/forums/2006scape.318/)