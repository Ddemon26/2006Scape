# Packet

Package `com.rs2.net`.

Defined in [`2006Scape Server/src/main/java/com/rs2/net/Packet.java`](2006Scape Server/src/main/java/com/rs2/net/Packet.java).

Represents a single packet.  @author Graham Edgecombe

```java
public class Packet {
public Packet(final int opcode, final Type type, final ByteBuf payload)
public boolean isRaw()
public int getOpcode()
public Type getType()
public ByteBuf getPayload()
```
