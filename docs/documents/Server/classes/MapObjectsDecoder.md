# MapObjectsDecoder

Package `org.apollo.cache.map`.

Defined in [`2006Scape Server/src/main/java/org/apollo/cache/map/MapObjectsDecoder.java`](2006Scape Server/src/main/java/org/apollo/cache/map/MapObjectsDecoder.java).

A decoder for reading the map objects for a given map.  @author Major

```java
public final class MapObjectsDecoder {
public static MapObjectsDecoder create(IndexedFileSystem fs, MapIndex index) throws IOException
public MapObjectsDecoder(ByteBuffer buffer)
public List<MapObject> decode()
```
