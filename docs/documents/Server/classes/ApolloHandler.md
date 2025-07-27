# ApolloHandler

Package `org.apollo.game.session`.

Defined in [`2006Scape Server/src/main/java/org/apollo/game/session/ApolloHandler.java`](2006Scape Server/src/main/java/org/apollo/game/session/ApolloHandler.java).

An implementation of {@link ChannelInboundHandlerAdapter} which handles incoming upstream events from Netty.  @author Graham

```java
public final class ApolloHandler extends ChannelInboundHandlerAdapter {
public static final AttributeKey<Session> SESSION_KEY = AttributeKey.valueOf("session");
public ApolloHandler()
public void channelInactive(ChannelHandlerContext ctx)
public void exceptionCaught(ChannelHandlerContext ctx, Throwable e)
public void channelRead(ChannelHandlerContext ctx, Object message) throws Exception
public void channelReadComplete(ChannelHandlerContext ctx)
```
