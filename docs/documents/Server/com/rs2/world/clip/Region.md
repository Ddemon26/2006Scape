# Region

Package `com.rs2.world.clip`.

Defined in [`2006Scape Server/src/main/java/com/rs2/world/clip/Region.java`](2006Scape Server/src/main/java/com/rs2/world/clip/Region.java).

Region helper class.

```java
public class Region {
public Region(int id, boolean members)
public int id()
public boolean members()
public static boolean isMembers(int x, int y)
public static Region getRegion(int x, int y)
public static int getRegionId(int x, int y)
public static Objects getObject(int id, int x, int y, int z)
public static boolean objectExists(int id, int x, int y, int z)
public void removeClipping(int x, int y, int height)
public static boolean canMove(int x, int y, int z, int direction)
public static boolean canShoot(int x, int y, int z, int direction)
public static boolean projectileBlockedNorth(int x, int y, int z)
public static boolean projectileBlockedEast(int x, int y, int z)
public static boolean projectileBlockedSouth(int x, int y, int z)
public static boolean projectileBlockedWest(int x, int y, int z)
public static boolean projectileBlockedNorthEast(int x, int y, int z)
public static boolean projectileBlockedNorthWest(int x, int y, int z)
public static boolean projectileBlockedSouthEast(int x, int y, int z)
public static boolean projectileBlockedSouthWest(int x, int y, int z)
public static boolean canMove(int startX, int startY, int endX, int endY, int height, int xLength, int yLength)
public static void addClipping(int x, int y, int height, int shift)
public static void addObject(int objectId, int x, int y, int height, int type, int direction, boolean startUp)
public static int getClipping(int x, int y, int height)
public static int getProjectileClipping(int x, int y, int height)
```
