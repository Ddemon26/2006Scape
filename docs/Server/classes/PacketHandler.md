# PacketHandler

Central dispatcher for all incoming packets. Each opcode is associated with a
`PacketType` implementation in a static array. When the server receives a packet
from a player session, `processPacket` invokes the mapped handler.

Source: [PacketHandler.java](../../2006Scape%20Server/src/main/java/com/rs2/net/packets/PacketHandler.java)

```java
// inside a Netty pipeline
PacketHandler.processPacket(player, packet);
```
