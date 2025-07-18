# HandshakeDecoder

Package `org.apollo.net.codec.handshake`.

Defined in [`2006Scape Server/src/main/java/org/apollo/net/codec/handshake/HandshakeDecoder.java`](2006Scape Server/src/main/java/org/apollo/net/codec/handshake/HandshakeDecoder.java).

A {@link ByteToMessageDecoder} which decodes the handshake and makes changes to the pipeline as appropriate for the selected service.  @author Graham

```java
public final class HandshakeDecoder extends ByteToMessageDecoder {
private static final Logger logger = Logger.getLogger(HandshakeDecoder.class.getName());
protected void decode(ChannelHandlerContext ctx, ByteBuf buffer, List<Object> out)
```
