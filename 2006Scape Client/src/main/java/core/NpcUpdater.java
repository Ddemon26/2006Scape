package core;

import net.Signlink;
import net.Stream;

/** Updates NPCs each game tick, extracted from {@link Game}. */
final class NpcUpdater {
    private final Game game;

    NpcUpdater(Game game) {
        this.game = game;
    }

    void updateNPCs(Stream stream, int size) {
        game.entityRemovalCount = 0;
        game.playerUpdateCount = 0;
        game.updateNpcList(stream);
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
}
