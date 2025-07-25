# PendingSpawnManager

Defined in [`2006Scape Client/src/main/java/core/PendingSpawnManager.java`](../../2006Scape%20Client/src/main/java/core/PendingSpawnManager.java).

Manages queued scene spawns extracted from [`Game`](Game.md).

```java
final class PendingSpawnManager {
    PendingSpawnManager(Game game)
    void locatePendingSpawns()
    void locateSceneObject(PendingSpawn pendingSpawn)
    void processPendingSpawns()
    void queuePendingSpawn(int delay, int id, int orientation, int category, int y, int type, int plane, int x, int spawnDelay)
}
```
