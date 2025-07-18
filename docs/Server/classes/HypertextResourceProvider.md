# HypertextResourceProvider

Package `org.apollo.net.update.resource`.

Defined in [`2006Scape Server/src/main/java/org/apollo/net/update/resource/HypertextResourceProvider.java`](2006Scape Server/src/main/java/org/apollo/net/update/resource/HypertextResourceProvider.java).

A {@link ResourceProvider} which provides additional hypertext resources.  @author Graham

```java
public final class HypertextResourceProvider implements ResourceProvider {
public HypertextResourceProvider(Path base)
public boolean accept(String path) throws IOException
public Optional<ByteBuffer> get(String path) throws IOException
```
