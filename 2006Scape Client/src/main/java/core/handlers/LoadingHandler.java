package core.handlers;

import core.engine.Game;
import core.managers.ObjectManager;
import core.network.Signlink;

/**
 * Handles game loading stages and map loading extracted from {@link Game}.
 */
public final class LoadingHandler {
    private final Game game;

    public LoadingHandler(Game game) {
        this.game = game;
    }

    public void loadingStages() {
        if (game.lowMem && game.loadingStage == 2 && ObjectManager.currentPlane != game.plane) {
            game.drawTextOnScreen(null, "Loading - please wait.");
            game.loadingStage = 1;
            game.loadingStartTime = System.currentTimeMillis();
        }
        if (game.loadingStage == 1) {
            int j = checkMapLoadStatus();
            if (j != 0 && System.currentTimeMillis() - game.loadingStartTime > 0x57e40L) {
                Signlink.reporterror(game.myUsername + " glcfb " + game.serverSessionKey + "," + j + "," + game.lowMem + "," + game.decompressors[0] + "," + game.onDemandFetcher.getNodeCount() + "," + game.plane + "," + game.currentRegionX + "," + game.currentRegionY);
                game.loadingStartTime = System.currentTimeMillis();
            }
        }
        if (game.loadingStage == 2 && game.plane != game.lastPlane) {
            game.lastPlane = game.plane;
            game.generateMinimap(game.plane);
        }
    }

    public int checkMapLoadStatus() {
        for (int i = 0; i < game.terrainData.length; i++) {
            if (game.terrainData[i] == null && game.terrainArchiveIds[i] != -1) {
                return -1;
            }
            if (game.objectMapData[i] == null && game.objectArchiveIds[i] != -1) {
                return -2;
            }
        }

        boolean flag = true;
        for (int j = 0; j < game.terrainData.length; j++) {
            byte abyte0[] = game.objectMapData[j];
            if (abyte0 != null) {
                int k = (game.regionBaseIds[j] >> 8) * 64 - game.baseX;
                int l = (game.regionBaseIds[j] & 0xff) * 64 - game.baseY;
                if (game.isDynamicRegion) {
                    k = 10;
                    l = 10;
                }
                flag &= ObjectManager.areObjectsReady(k, abyte0, l);
            }
        }

        if (!flag) {
            return -3;
        }
        if (game.regionLoading) {
            return -4;
        } else {
            game.loadingStage = 2;
            ObjectManager.currentPlane = game.plane;
            game.constructMapRegion();
            game.stream.createFrame(121);
            return 0;
        }
    }
}