# PlayerCredentials

Package `org.apollo.util.security`.

Defined in [`2006Scape Server/src/main/java/org/apollo/util/security/PlayerCredentials.java`](2006Scape Server/src/main/java/org/apollo/util/security/PlayerCredentials.java).

Holds the credentials for a player.  @author Graham

```java
public final class PlayerCredentials {
public PlayerCredentials(String username, String password, int usernameHash, int uid, String hostAddress)
public long getEncodedUsername()
public void setPassword(String password)
public String getPassword()
public int getUid()
public String getUsername()
public int getUsernameHash()
public String getHostAddress()
public int hashCode()
public boolean equals(Object obj)
```
