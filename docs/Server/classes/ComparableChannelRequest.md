# ComparableChannelRequest

Package `org.apollo.net.update`.

Defined in [`2006Scape Server/src/main/java/org/apollo/net/update/ComparableChannelRequest.java`](2006Scape Server/src/main/java/org/apollo/net/update/ComparableChannelRequest.java).

A {@link ChannelRequest} with a {@link Comparable} request type.  @author Major  @param <T> The type of request.

```java
public final class ComparableChannelRequest<T extends Comparable<T>> extends ChannelRequest<T> implements Comparable<ComparableChannelRequest<T>> {
public ComparableChannelRequest(Channel channel, T request)
public int compareTo(ComparableChannelRequest<T> o)
```
