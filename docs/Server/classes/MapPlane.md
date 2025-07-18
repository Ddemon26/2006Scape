# MapPlane

Package `org.apollo.cache.map`.

Defined in [`2006Scape Server/src/main/java/org/apollo/cache/map/MapPlane.java`](2006Scape Server/src/main/java/org/apollo/cache/map/MapPlane.java).

A plane of a map, which is a distinct height level.  @author Major

```java
public final class MapPlane {
public MapPlane(int level, Tile[][] tiles)
public int getLevel()
public int getSize()
public Tile getTile(int x, int z)
public Stream<Tile> getTiles()
```
