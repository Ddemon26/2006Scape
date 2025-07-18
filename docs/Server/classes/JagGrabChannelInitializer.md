# JagGrabChannelInitializer

Package `org.apollo.net`.

Defined in [`2006Scape Server/src/main/java/org/apollo/net/JagGrabChannelInitializer.java`](2006Scape Server/src/main/java/org/apollo/net/JagGrabChannelInitializer.java).

A {@link ChannelInitializer} for the JAGGRAB protocol.  @author Graham

```java
public final class JagGrabChannelInitializer extends ChannelInitializer<SocketChannel> {
private static final ByteBuf DOUBLE_LINE_FEED_DELIMITER = Unpooled.buffer(2);
public JagGrabChannelInitializer(ChannelInboundHandlerAdapter handler)
public void initChannel(SocketChannel ch) throws Exception
```
