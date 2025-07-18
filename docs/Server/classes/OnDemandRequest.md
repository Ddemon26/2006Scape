# OnDemandRequest

Package `org.apollo.net.codec.update`.

Defined in [`2006Scape Server/src/main/java/org/apollo/net/codec/update/OnDemandRequest.java`](2006Scape Server/src/main/java/org/apollo/net/codec/update/OnDemandRequest.java).

Represents a single 'on-demand' request.  @author Graham

```java
public final class OnDemandRequest implements Comparable<OnDemandRequest> {
public static Priority valueOf(int value)
private Priority(int value)
public int compareWith(Priority other)
public int toInteger()
public OnDemandRequest(FileDescriptor descriptor, Priority priority)
```
