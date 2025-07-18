# MapIndexDecoder

Package `org.apollo.cache.map`.

Defined in [`2006Scape Server/src/main/java/org/apollo/cache/map/MapIndexDecoder.java`](2006Scape Server/src/main/java/org/apollo/cache/map/MapIndexDecoder.java).

Decodes {@link MapIndex}s from the {@link IndexedFileSystem}.  @author Ryley @author Major

```java
public final class MapIndexDecoder implements Runnable {
public MapIndexDecoder(IndexedFileSystem fs)
public Map<Integer, MapIndex> decode() throws IOException
public void run()
```
