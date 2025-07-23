package core;

import net.Signlink;
import net.Stream;

import game.NPC;
/** Updates NPCs each game tick, extracted from {@link Game}. */
final class NpcUpdater {
    private final Game game;

    NpcUpdater(Game game) {
        this.game = game;
    }

    void updateNPCs(Stream stream, int size) {
        game.entityRemovalCount = 0;
        game.playerUpdateCount = 0;
        updateNpcList(stream);
        game.addLocalNPCs(size, stream);
        game.processNpcUpdateMasks(stream);
        for (int i = 0; i < game.entityRemovalCount; i++) {
            int l = game.removedEntityIndices[i];
            if (game.npcArray[l].lastUpdateCycle != game.loopCycle) {
                game.npcArray[l].definition = null;
                game.npcArray[l] = null;
            }
        }
        if (stream.currentOffset != size) {
            Signlink.reporterror(game.myUsername + " size mismatch in getnpcpos - pos:" + stream.currentOffset + " psize:" + size);
            throw new RuntimeException("eek");
        }
        for (int i1 = 0; i1 < game.npcCount; i1++) {
            if (game.npcArray[game.npcIndices[i1]] == null) {
                Signlink.reporterror(game.myUsername + " null entry in npc list - pos:" + i1 + " size:" + game.npcCount);
                throw new RuntimeException("eek");
            }
        }
    }
    private void updateNpcList(Stream stream) {
        stream.initBitAccess();
        int k = stream.readBits(8);
        if (k < game.npcCount) {
            for (int l = k; l < game.npcCount; l++) {
                game.removedEntityIndices[game.entityRemovalCount++] = game.npcIndices[l];
            }
        }
        if (k > game.npcCount) {
            Signlink.reporterror(game.myUsername + " Too many npcs");
            throw new RuntimeException("eek");
        }
        game.npcCount = 0;
        for (int i1 = 0; i1 < k; i1++) {
            int j1 = game.npcIndices[i1];
            NPC npc = game.npcArray[j1];
            int k1 = stream.readBits(1);
            if (k1 == 0) {
                game.npcIndices[game.npcCount++] = j1;
                npc.lastUpdateCycle = game.loopCycle;
            } else {
                int l1 = stream.readBits(2);
                if (l1 == 0) {
                    game.npcIndices[game.npcCount++] = j1;
                    npc.lastUpdateCycle = game.loopCycle;
                    game.playerUpdateIndices[game.playerUpdateCount++] = j1;
                } else if (l1 == 1) {
                    game.npcIndices[game.npcCount++] = j1;
                    npc.lastUpdateCycle = game.loopCycle;
                    int i2 = stream.readBits(3);
                    npc.moveInDir(false, i2);
                    int k2 = stream.readBits(1);
                    if (k2 == 1) {
                        game.playerUpdateIndices[game.playerUpdateCount++] = j1;
                    }
                } else if (l1 == 2) {
                    game.npcIndices[game.npcCount++] = j1;
                    npc.lastUpdateCycle = game.loopCycle;
                    int j2 = stream.readBits(3);
                    npc.moveInDir(true, j2);
                    int l2 = stream.readBits(3);
                    npc.moveInDir(true, l2);
                    int i3 = stream.readBits(1);
                    if (i3 == 1) {
                        game.playerUpdateIndices[game.playerUpdateCount++] = j1;
                    }
                } else if (l1 == 3) {
                    game.removedEntityIndices[game.entityRemovalCount++] = j1;
                }
            }
        }
    }
}
