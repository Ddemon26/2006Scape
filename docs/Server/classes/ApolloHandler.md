# ApolloHandler

Package `org.apollo.game.session`.

Defined in [`2006Scape Server/src/main/java/org/apollo/game/session/ApolloHandler.java`](2006Scape Server/src/main/java/org/apollo/game/session/ApolloHandler.java).

An implementation of {@link ChannelInboundHandlerAdapter} which handles incoming upstream events from Netty.  @author Graham

```java
public final class ApolloHandler extends ChannelInboundHandlerAdapter {
private static final Logger logger = Logger.getLogger(ApolloHandler.class.getName());
public static final AttributeKey<Session> SESSION_KEY = AttributeKey.valueOf("session");
public ApolloHandler()
public void channelInactive(ChannelHandlerContext ctx)
public void exceptionCaught(ChannelHandlerContext ctx, Throwable e)
```
