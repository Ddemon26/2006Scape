# RS2ProtocolDecoder

Package `com.rs2.net`.

Defined in [`2006Scape Server/src/main/java/com/rs2/net/RS2ProtocolDecoder.java`](2006Scape Server/src/main/java/com/rs2/net/RS2ProtocolDecoder.java).

RS2Protocol Decoder helper class.

```java
public class RS2ProtocolDecoder extends StatefulFrameDecoder<GameDecoderState> {
public RS2ProtocolDecoder(IsaacRandom isaac)
protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out, GameDecoderState state)
private void decodeLength(ByteBuf buffer)
private void decodeOpcode(ByteBuf buffer, List<Object> out)
private void decodePayload(ByteBuf buffer, List<Object> out)
```
