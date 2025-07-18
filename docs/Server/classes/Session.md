# Session

Package `org.apollo.game.session`.

Defined in [`2006Scape Server/src/main/java/org/apollo/game/session/Session.java`](2006Scape Server/src/main/java/org/apollo/game/session/Session.java).

A session which is used as an attribute of a {@link ChannelHandlerContext} in Netty.  @author Graham

```java
public abstract class Session {
public Session(Channel channel)
public abstract void destroy();
public abstract void messageReceived(Object message) throws Exception;
protected final Channel getChannel()
```
