# HttpRequestWorker

Package `org.apollo.net.update`.

Defined in [`2006Scape Server/src/main/java/org/apollo/net/update/HttpRequestWorker.java`](2006Scape Server/src/main/java/org/apollo/net/update/HttpRequestWorker.java).

A worker which services HTTP requests.  @author Graham

```java
public final class HttpRequestWorker extends RequestWorker<HttpRequest, ResourceProvider> {
private static final Path WWW_DIRECTORY = Paths.get("data/www");
public HttpRequestWorker(UpdateDispatcher dispatcher, IndexedFileSystem fs)
private static ByteBuf createErrorPage(HttpResponseStatus status, String description)
private static String getMimeType(String name)
protected ChannelRequest<HttpRequest> nextRequest(UpdateDispatcher dispatcher) throws InterruptedException
```
