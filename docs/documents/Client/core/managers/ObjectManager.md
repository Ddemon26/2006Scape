# core.managers.ObjectManager

Defined in [`2006Scape Client/src/main/java/core.managers.ObjectManager.java`](2006Scape Client/src/main/java/core.managers.ObjectManager.java).

Object Manager helper class.

```java
final class core.managers.ObjectManager {
public core.managers.ObjectManager(byte abyte0[][][], int ai[][][])
public final void buildLandscape(core.world.CollisionMap collisionMaps[], core.world.WorldController worldController)
public static void loadObjectModels(core.network.Stream stream, core.managers.OnDemandFetcher onDemandFetcher)
public final void clearRegion(int i, int j, int l, int i1)
public static boolean isObjectVisible(int i, int j)
public final void loadChunk(int i, int j, core.world.CollisionMap collisionMaps[], int l, int i1, byte abyte0[], int j1, int k1, int l1)
public final void loadRegion(byte abyte0[], int i, int j, int k, int l, core.world.CollisionMap collisionMaps[])
public final void loadObjectChunk(core.world.CollisionMap collisionMaps[], core.world.WorldController worldController, int i, int j, int k, int l, byte abyte0[], int i1, int j1, int k1)
public static void addObject(core.world.WorldController worldController, int i, int j, int k, int l, core.world.CollisionMap collisionMap, int ai[][][], int i1, int j1, int k1)
public static boolean areObjectsReady(int i, byte[] is, int i_250_) // xxx bad
public final void loadObjects(int i, core.world.CollisionMap collisionMaps[], int j, core.world.WorldController worldController, byte abyte0[])
```
