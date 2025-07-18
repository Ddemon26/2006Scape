# CombinedResourceProvider

Package `org.apollo.net.update.resource`.

Defined in [`2006Scape Server/src/main/java/org/apollo/net/update/resource/CombinedResourceProvider.java`](2006Scape Server/src/main/java/org/apollo/net/update/resource/CombinedResourceProvider.java).

A resource provider composed of multiple resource providers.  @author Graham

```java
public final class CombinedResourceProvider implements ResourceProvider {
public CombinedResourceProvider(ResourceProvider... providers)
public boolean accept(String path) throws IOException
public Optional<ByteBuffer> get(String path) throws IOException
```
