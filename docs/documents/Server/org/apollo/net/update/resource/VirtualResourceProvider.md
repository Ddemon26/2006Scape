# VirtualResourceProvider

Package `org.apollo.net.update.resource`.

Defined in [`2006Scape Server/src/main/java/org/apollo/net/update/resource/VirtualResourceProvider.java`](2006Scape Server/src/main/java/org/apollo/net/update/resource/VirtualResourceProvider.java).

A {@link ResourceProvider} which maps virtual resources (such as {@code /media}) to files in an {@link IndexedFileSystem}.  @author Graham

```java
public final class VirtualResourceProvider implements ResourceProvider {
public VirtualResourceProvider(IndexedFileSystem fs)
public boolean accept(String path) throws IOException
public Optional<ByteBuffer> get(String path) throws IOException
```
