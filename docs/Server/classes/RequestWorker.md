# RequestWorker

Package `org.apollo.net.update`.

Defined in [`2006Scape Server/src/main/java/org/apollo/net/update/RequestWorker.java`](2006Scape Server/src/main/java/org/apollo/net/update/RequestWorker.java).

The base class for request workers.  @author Graham @param <T> The type of request. @param <P> The type of provider.

```java
* The base class for request workers.
public RequestWorker(UpdateDispatcher dispatcher, P provider)
protected abstract ChannelRequest<T> nextRequest(UpdateDispatcher dispatcher) throws InterruptedException;
public final void run()
protected abstract void service(P provider, Channel channel, T request) throws IOException;
public final void stop()
```
