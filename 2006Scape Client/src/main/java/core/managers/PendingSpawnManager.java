package core.managers;

import core.engine.Game;
import core.world.PendingSpawn;

/** Manages pending scene spawns extracted from {@link Game}. */
public final class PendingSpawnManager {
    private final Game game;

    public PendingSpawnManager(Game game) {
        this.game = game;
    }

    public void locatePendingSpawns() {
        PendingSpawn pendingSpawn = (PendingSpawn) game.pendingSpawns.reverseGetFirst();
        for (; pendingSpawn != null; pendingSpawn = (PendingSpawn) game.pendingSpawns.reverseGetNext()) {
            if (pendingSpawn.delay == -1) {
                pendingSpawn.spawnDelay = 0;
                locateSceneObject(pendingSpawn);
            } else {
                pendingSpawn.unlink();
            }
        }
    }

    public void locateSceneObject(PendingSpawn pendingSpawn) {
        int i = 0;
        int j = -1;
        int k = 0;
        int l = 0;
        if (pendingSpawn.category == 0) {
            i = game.worldController.getBoundaryObjectUid(pendingSpawn.plane, pendingSpawn.x, pendingSpawn.y);
        }
        if (pendingSpawn.category == 1) {
            i = game.worldController.getWallDecorationUid(pendingSpawn.plane, pendingSpawn.x, pendingSpawn.y);
        }
        if (pendingSpawn.category == 2) {
            i = game.worldController.getSceneObjectUid(pendingSpawn.plane, pendingSpawn.x, pendingSpawn.y);
        }
        if (pendingSpawn.category == 3) {
            i = game.worldController.getTileDecorationUid(pendingSpawn.plane, pendingSpawn.x, pendingSpawn.y);
        }
        if (i != 0) {
            int i1 = game.worldController.getObjectConfig(pendingSpawn.plane, pendingSpawn.x, pendingSpawn.y, i);
            j = i >> 14 & 0x7fff;
            k = i1 & 0x1f;
            l = i1 >> 6;
        }
        pendingSpawn.oldId = j;
        pendingSpawn.oldOrientation = k;
        pendingSpawn.oldType = l;
    }

    public void processPendingSpawns() {
        if (game.loadingStage == 2) {
            for (PendingSpawn pendingSpawn = (PendingSpawn) game.pendingSpawns.reverseGetFirst();
                 pendingSpawn != null; pendingSpawn = (PendingSpawn) game.pendingSpawns.reverseGetNext()) {
                if (pendingSpawn.delay > 0) {
                    pendingSpawn.delay--;
                }
                if (pendingSpawn.delay == 0) {
                    if (pendingSpawn.oldId < 0 || ObjectManager.isObjectVisible(pendingSpawn.oldId, pendingSpawn.oldOrientation)) {
                        game.updateSceneObjects(pendingSpawn.y, pendingSpawn.plane, pendingSpawn.oldType,
                                pendingSpawn.oldOrientation, pendingSpawn.x, pendingSpawn.category, pendingSpawn.oldId);
                        pendingSpawn.unlink();
                    }
                } else {
                    if (pendingSpawn.spawnDelay > 0) {
                        pendingSpawn.spawnDelay--;
                    }
                    if (pendingSpawn.spawnDelay == 0 && pendingSpawn.x >= 1 && pendingSpawn.y >= 1
                            && pendingSpawn.x <= 102 && pendingSpawn.y <= 102
                            && (pendingSpawn.id < 0 || ObjectManager.isObjectVisible(pendingSpawn.id, pendingSpawn.type))) {
                        game.updateSceneObjects(pendingSpawn.y, pendingSpawn.plane, pendingSpawn.orientation,
                                pendingSpawn.type, pendingSpawn.x, pendingSpawn.category, pendingSpawn.id);
                        pendingSpawn.spawnDelay = -1;
                        if (pendingSpawn.id == pendingSpawn.oldId && pendingSpawn.oldId == -1) {
                            pendingSpawn.unlink();
                        } else if (pendingSpawn.id == pendingSpawn.oldId && pendingSpawn.orientation == pendingSpawn.oldType
                                && pendingSpawn.type == pendingSpawn.oldOrientation) {
                            pendingSpawn.unlink();
                        }
                    }
                }
            }
        }
    }

    public void queuePendingSpawn(int j, int k, int l, int i1, int j1, int k1, int l1, int i2, int j2) {
        PendingSpawn pendingSpawn = null;
        for (PendingSpawn pendingSpawnIter = (PendingSpawn) game.pendingSpawns.reverseGetFirst();
             pendingSpawnIter != null; pendingSpawnIter = (PendingSpawn) game.pendingSpawns.reverseGetNext()) {
            if (pendingSpawnIter.plane != l1 || pendingSpawnIter.x != i2 || pendingSpawnIter.y != j1
                    || pendingSpawnIter.category != i1) {
                continue;
            }
            pendingSpawn = pendingSpawnIter;
            break;
        }

        if (pendingSpawn == null) {
            pendingSpawn = new PendingSpawn();
            pendingSpawn.plane = l1;
            pendingSpawn.category = i1;
            pendingSpawn.x = i2;
            pendingSpawn.y = j1;
            locateSceneObject(pendingSpawn);
            game.pendingSpawns.insertHead(pendingSpawn);
        }
        pendingSpawn.id = k;
        pendingSpawn.type = k1;
        pendingSpawn.orientation = l;
        pendingSpawn.spawnDelay = j2;
        pendingSpawn.delay = j;
    }
}
