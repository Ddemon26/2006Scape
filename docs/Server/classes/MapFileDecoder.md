# MapFileDecoder

Package `org.apollo.cache.map`.

Defined in [`2006Scape Server/src/main/java/org/apollo/cache/map/MapFileDecoder.java`](2006Scape Server/src/main/java/org/apollo/cache/map/MapFileDecoder.java).

A decoder for the terrain data stored in {@link MapFile}s.  @author Major

```java
public class MapFileDecoder {
public static MapFileDecoder create(IndexedFileSystem fs, MapIndex index) throws IOException
public MapFileDecoder(ByteBuffer buffer)
public MapFile decode()
private MapPlane decodePlane(MapPlane[] planes, int level)
private Tile decodeTile(MapPlane[] planes, int level, int x, int z)
```
