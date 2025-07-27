# core.world.CollisionMap

Defined in [`2006Scape Client/src/main/java/core.world.CollisionMap.java`](2006Scape Client/src/main/java/core.world.CollisionMap.java).

Collision Map helper class.

```java
final class core.world.CollisionMap {
public core.world.CollisionMap()
public void reset()
public void addWall(int i, int j, int k, int l, boolean flag)
public void addObject(boolean flag, int j, int k, int l, int i1, int j1)
public void blockTile(int i, int k)
public void removeWall(int i, int j, boolean flag, int k, int l)
public void removeObject(int i, int j, int k, int l, int i1, boolean flag)
public void unblockTile(int j, int k)
public boolean canReachWall(int i, int j, int k, int i1, int j1, int k1)
public boolean canReachObject(int i, int j, int k, int l, int i1, int j1)
public boolean canReachArea(int i, int j, int k, int l, int i1, int j1, int k1)
```
