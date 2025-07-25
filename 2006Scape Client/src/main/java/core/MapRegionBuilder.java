package core;

import core.engine.Game;
import game.ObjectDef;
import game.ObjectManager;
import net.Signlink;
import render.Model;
import render.Texture;

/** Builds map regions and requests region files, extracted from {@link Game}. */
public final class MapRegionBuilder {
    private final Game game;

    public MapRegionBuilder(Game game) {
        this.game = game;
    }

    public void constructMapRegion() {
        try {
            game.lastPlane = -1;
            game.graphicsObjectList.removeAll();
            game.projectileList.removeAll();
            Texture.clearCache();
            game.unlinkMRUNodes();
            game.worldController.initToNull();
            System.gc();
            for (int i = 0; i < 4; i++) {
                game.collisionMaps[i].reset();
            }
            for (int l = 0; l < 4; l++) {
                for (int k1 = 0; k1 < 104; k1++) {
                    for (int j2 = 0; j2 < 104; j2++) {
                        game.tileFlags[l][k1][j2] = 0;
                    }
                }
            }
            ObjectManager objectManager = new ObjectManager(game.tileFlags, game.tileHeights);
            int k2 = game.terrainData.length;
            game.stream.createFrame(0);
            if (!game.isDynamicRegion) {
                for (int i3 = 0; i3 < k2; i3++) {
                    int i4 = (game.regionBaseIds[i3] >> 8) * 64 - game.baseX;
                    int k5 = (game.regionBaseIds[i3] & 0xff) * 64 - game.baseY;
                    byte[] abyte0 = game.terrainData[i3];
                    if (abyte0 != null) {
                        objectManager.loadRegion(abyte0, k5, i4,
                                (game.currentRegionX - 6) * 8, (game.currentRegionY - 6) * 8,
                                game.collisionMaps);
                    }
                }
                for (int j4 = 0; j4 < k2; j4++) {
                    int l5 = (game.regionBaseIds[j4] >> 8) * 64 - game.baseX;
                    int k7 = (game.regionBaseIds[j4] & 0xff) * 64 - game.baseY;
                    byte[] abyte2 = game.terrainData[j4];
                    if (abyte2 == null && game.currentRegionY < 800) {
                        objectManager.clearRegion(k7, 64, 64, l5);
                    }
                }
                game.mapLoadPacketCounter++;
                if (game.mapLoadPacketCounter > 160) {
                    game.mapLoadPacketCounter = 0;
                    game.stream.createFrame(238);
                    game.stream.writeWordBigEndian(96);
                }
                game.stream.createFrame(0);
                for (int i6 = 0; i6 < k2; i6++) {
                    byte[] abyte1 = game.objectMapData[i6];
                    if (abyte1 != null) {
                        int l8 = (game.regionBaseIds[i6] >> 8) * 64 - game.baseX;
                        int k9 = (game.regionBaseIds[i6] & 0xff) * 64 - game.baseY;
                        objectManager.loadObjects(l8, game.collisionMaps, k9, game.worldController, abyte1);
                    }
                }
            }
            if (game.isDynamicRegion) {
                for (int j3 = 0; j3 < 4; j3++) {
                    for (int k4 = 0; k4 < 13; k4++) {
                        for (int j6 = 0; j6 < 13; j6++) {
                            int l7 = game.dynamicRegionData[j3][k4][j6];
                            if (l7 != -1) {
                                int i9 = l7 >> 24 & 3;
                                int l9 = l7 >> 1 & 3;
                                int j10 = l7 >> 14 & 0x3ff;
                                int l10 = l7 >> 3 & 0x7ff;
                                int j11 = (j10 / 8 << 8) + l10 / 8;
                                for (int l11 = 0; l11 < game.regionBaseIds.length; l11++) {
                                    if (game.regionBaseIds[l11] != j11 || game.terrainData[l11] == null) {
                                        continue;
                                    }
                                    objectManager.loadChunk(i9, l9, game.collisionMaps, k4 * 8,
                                            (j10 & 7) * 8, game.terrainData[l11],
                                            (l10 & 7) * 8, j3, j6 * 8);
                                    break;
                                }
                            }
                        }
                    }
                }
                for (int l4 = 0; l4 < 13; l4++) {
                    for (int k6 = 0; k6 < 13; k6++) {
                        int i8 = game.dynamicRegionData[0][l4][k6];
                        if (i8 == -1) {
                            objectManager.clearRegion(k6 * 8, 8, 8, l4 * 8);
                        }
                    }
                }
                game.stream.createFrame(0);
                for (int l6 = 0; l6 < 4; l6++) {
                    for (int j8 = 0; j8 < 13; j8++) {
                        for (int j9 = 0; j9 < 13; j9++) {
                            int i10 = game.dynamicRegionData[l6][j8][j9];
                            if (i10 != -1) {
                                int k10 = i10 >> 24 & 3;
                                int i11 = i10 >> 1 & 3;
                                int k11 = i10 >> 14 & 0x3ff;
                                int i12 = i10 >> 3 & 0x7ff;
                                int j12 = (k11 / 8 << 8) + i12 / 8;
                                for (int k12 = 0; k12 < game.regionBaseIds.length; k12++) {
                                    if (game.regionBaseIds[k12] != j12 || game.objectMapData[k12] == null) {
                                        continue;
                                    }
                                    objectManager.loadObjectChunk(game.collisionMaps, game.worldController,
                                            k10, j8 * 8, (i12 & 7) * 8, l6,
                                            game.objectMapData[k12], (k11 & 7) * 8, i11, j9 * 8);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            game.stream.createFrame(0);
            objectManager.buildLandscape(game.collisionMaps, game.worldController);
            if (game.tabAreaBuffer != null) {
                game.tabAreaBuffer.initDrawingArea();
                Texture.lineOffsets = game.chatBoxAreaOffsets;
            }
            game.stream.createFrame(0);
            int k3 = ObjectManager.lowestPlane;
            if (k3 > game.plane) {
                k3 = game.plane;
            }
            if (k3 < game.plane - 1) {
                k3 = game.plane - 1;
            }
            if (game.lowMem) {
                game.worldController.setActivePlane(ObjectManager.lowestPlane);
            } else {
                game.worldController.setActivePlane(0);
            }
            for (int i5 = 0; i5 < 104; i5++) {
                for (int i7 = 0; i7 < 104; i7++) {
                    game.spawnGroundItem(i5, i7);
                }
            }
            game.terrainLoadCycle++;
            if (game.terrainLoadCycle > 98) {
                game.terrainLoadCycle = 0;
                game.stream.createFrame(150);
            }
            game.locatePendingSpawns();
        } catch (Exception ignored) {
        }
        ObjectDef.mruNodes1.unlinkAll();
        if (game.gameFrame != null) {
            game.stream.createFrame(210);
            game.stream.writeDWord(0x3f008edd);
        }
        if (game.lowMem && Signlink.cache_dat != null) {
            int j = game.onDemandFetcher.getVersionCount(0);
            for (int i1 = 0; i1 < j; i1++) {
                int l1 = game.onDemandFetcher.getModelIndex(i1);
                if ((l1 & 0x79) == 0) {
                    Model.unload(i1);
                }
            }
        }
        System.gc();
        Texture.initCache();
        game.onDemandFetcher.clearPriorityQueue();
        int k = (game.currentRegionX - 6) / 8 - 1;
        int j1 = (game.currentRegionX + 6) / 8 + 1;
        int i2 = (game.currentRegionY - 6) / 8 - 1;
        int l2 = (game.currentRegionY + 6) / 8 + 1;
        if (game.forceMapReload) {
            k = 49;
            j1 = 50;
            i2 = 49;
            l2 = 50;
        }
        for (int l3 = k; l3 <= j1; l3++) {
            for (int j5 = i2; j5 <= l2; j5++) {
                if (l3 == k || l3 == j1 || j5 == i2 || j5 == l2) {
                    int j7 = game.onDemandFetcher.getRegionArchiveId(0, j5, l3);
                    if (j7 != -1) {
                        game.onDemandFetcher.requestFileNow(j7, 3);
                    }
                    int k8 = game.onDemandFetcher.getRegionArchiveId(1, j5, l3);
                    if (k8 != -1) {
                        game.onDemandFetcher.requestFileNow(k8, 3);
                    }
                }
            }
        }
    }
}
