# Tile

Package `org.apollo.cache.map`.

Defined in [`2006Scape Server/src/main/java/org/apollo/cache/map/Tile.java`](2006Scape Server/src/main/java/org/apollo/cache/map/Tile.java).

A single tile on the map.  @author Major

```java
public final class Tile {
public Builder(int x, int y, int height)
public Tile build()
public void setAttributes(int attributes)
public void setHeight(int height)
public void setOverlay(int overlay)
public void setOverlayOrientation(int orientation)
public void setOverlayType(int type)
public void setPosition(int x, int y, int height)
public void setUnderlay(int underlay)
public static Builder builder(int x, int y, int height)
public int getAttributes()
public int getHeight()
public int getOverlay()
public int getOverlayOrientation()
public int getOverlayType()
public int getUnderlay()
public int getX()
public int getY()
```
