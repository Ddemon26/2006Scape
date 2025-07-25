# Commands

Package `com.rs2.net.packets.impl`.

Defined in [`2006Scape Server/src/main/java/com/rs2/net/packets/impl/Commands.java`](2006Scape Server/src/main/java/com/rs2/net/packets/impl/Commands.java).

Discord commands for .

```java
public class Commands implements PacketType {
public void processPacket(Player player, Packet packet)
public static void playerCommands(Player player, String playerCommand, String[] arguments)
public void run()
public static void moderatorCommands(Player player, String playerCommand, String[] arguments)
public static void adminCommands(Player player, String playerCommand, String[] arguments)
public static void developerCommands(Player player, String playerCommand, String[] arguments)
```
