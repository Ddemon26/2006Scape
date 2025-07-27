# UpdateDispatcher

Package `org.apollo.net.update`.

Defined in [`2006Scape Server/src/main/java/org/apollo/net/update/UpdateDispatcher.java`](2006Scape Server/src/main/java/org/apollo/net/update/UpdateDispatcher.java).

Dispatches update requests to worker threads.  @author Graham

```java
public final class UpdateDispatcher {
public void dispatch(Channel channel, HttpRequest request)
public void dispatch(Channel channel, JagGrabRequest request)
public void dispatch(Channel channel, OnDemandRequest request)
```
