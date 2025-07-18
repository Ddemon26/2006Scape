# LoginSession

Package `org.apollo.game.session`.

Defined in [`2006Scape Server/src/main/java/org/apollo/game/session/LoginSession.java`](2006Scape Server/src/main/java/org/apollo/game/session/LoginSession.java).

Temporary quick and tear integration with apollo netcode. This needs redone when the Apollo Service system is added. @author Advocatus

```java
public final class LoginSession extends Session {
public LoginSession(Channel channel)
public void destroy()
public void messageReceived(Object message) throws Exception
private void handleLoginRequest(LoginRequest request) throws IOException
```
