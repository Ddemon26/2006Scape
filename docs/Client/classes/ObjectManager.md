# ObjectManager

Defined in [`2006Scape Client/src/main/java/game/ObjectManager.java`](2006Scape Client/src/main/java/game/ObjectManager.java).

Object Manager helper class.

```java
final class ObjectManager {
public ObjectManager(byte abyte0[][][], int ai[][][])
public final void buildLandscape(CollisionMap collisionMaps[], WorldController worldController)
public static void loadObjectModels(Stream stream, OnDemandFetcher onDemandFetcher)
public final void clearRegion(int i, int j, int l, int i1)
public static boolean isObjectVisible(int i, int j)
public final void loadChunk(int i, int j, CollisionMap collisionMaps[], int l, int i1, byte abyte0[], int j1, int k1, int l1)
public final void loadRegion(byte abyte0[], int i, int j, int k, int l, CollisionMap collisionMaps[])
public final void loadObjectChunk(CollisionMap collisionMaps[], WorldController worldController, int i, int j, int k, int l, byte abyte0[], int i1, int j1, int k1)
public static void addObject(WorldController worldController, int i, int j, int k, int l, CollisionMap collisionMap, int ai[][][], int i1, int j1, int k1)
public static boolean areObjectsReady(int i, byte[] is, int i_250_) // xxx bad
public final void loadObjects(int i, CollisionMap collisionMaps[], int j, WorldController worldController, byte abyte0[])
```
