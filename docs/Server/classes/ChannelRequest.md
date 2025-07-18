# ChannelRequest

Package `org.apollo.net.update`.

Defined in [`2006Scape Server/src/main/java/org/apollo/net/update/ChannelRequest.java`](2006Scape Server/src/main/java/org/apollo/net/update/ChannelRequest.java).

A specialised request which contains a channel as well as the request object itself.  @author Graham @param <T> The type of request.

```java
public class ChannelRequest<T> {
public ChannelRequest(Channel channel, T request)
public Channel getChannel()
public T getRequest()
```
