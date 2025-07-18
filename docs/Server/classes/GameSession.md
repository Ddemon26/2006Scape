# GameSession

Package `org.apollo.game.session`.

Defined in [`2006Scape Server/src/main/java/org/apollo/game/session/GameSession.java`](2006Scape Server/src/main/java/org/apollo/game/session/GameSession.java).

Temporary quick and tear integration with apollo netcode. This needs redone when the the packets are fully redone. @author Advocatus

```java
public final class GameSession extends Session {
public void setPlayer(Player player)
public GameSession(Channel channel, Player player, boolean reconnecting)
public void destroy()
public boolean isReconnecting()
public void messageReceived(Object message)
```
