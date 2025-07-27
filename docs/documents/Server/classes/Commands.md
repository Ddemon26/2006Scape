# Commands

Package `com.rs2.net.packets.impl`.

Defined in [`2006Scape Server/src/main/java/com/rs2/net/packets/impl/Commands.java`](2006Scape Server/src/main/java/com/rs2/net/packets/impl/Commands.java).

Discord commands for .

```java
public class Commands implements PacketType {
public void processPacket(game.entities.Player player, Packet packet)
public static void playerCommands(game.entities.Player player, String playerCommand, String[] arguments)
public void run()
public static void moderatorCommands(game.entities.Player player, String playerCommand, String[] arguments)
public static void adminCommands(game.entities.Player player, String playerCommand, String[] arguments)
public static void developerCommands(game.entities.Player player, String playerCommand, String[] arguments)
```
