# LoginRequest

Package `org.apollo.net.codec.login`.

Defined in [`2006Scape Server/src/main/java/org/apollo/net/codec/login/LoginRequest.java`](2006Scape Server/src/main/java/org/apollo/net/codec/login/LoginRequest.java).

Represents a login request.  @author Graham

```java
public final class LoginRequest {
public LoginRequest(PlayerCredentials credentials, IsaacRandomPair randomPair, boolean lowMemory, boolean reconnecting, int releaseNumber, int[] archiveCrcs, int clientVersion)
public int[] getArchiveCrcs()
public int getClientVersion()
public PlayerCredentials getCredentials()
public IsaacRandomPair getRandomPair()
```
