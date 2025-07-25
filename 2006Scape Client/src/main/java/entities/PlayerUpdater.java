package entities;

import core.engine.Game;
import game.entities.Player;
import core.network.Signlink;
import core.network.Stream;

/**
 * Updates the local and other players each tick, extracted from {@link Game}.
 */
public final class PlayerUpdater {
    private final Game game;

    public PlayerUpdater(Game game) {
        this.game = game;
    }

    public void updatePlayers(int size, Stream stream) {
        game.entityRemovalCount = 0;
        game.playerUpdateCount = 0;
        game.updateSelfMovement(stream);
        updateOtherPlayers(stream);
        game.addLocalPlayers(stream, size);
        game.processPlayerUpdateMasks(stream);
        for (int k = 0; k < game.entityRemovalCount; k++) {
            int l = game.removedEntityIndices[k];
            if (game.playerArray[l].lastUpdateCycle != game.loopCycle) {
                game.playerArray[l] = null;
            }
        }
        if (stream.currentOffset != size) {
            Signlink.reporterror("Error packet size mismatch in getplayer pos:" + stream.currentOffset + " psize:" + size);
            throw new RuntimeException("eek");
        }
        for (int i1 = 0; i1 < game.playerCount; i1++) {
            if (game.playerArray[game.playerIndices[i1]] == null) {
                Signlink.reporterror(game.myUsername + " null entry in pl list - pos:" + i1 + " size:" + game.playerCount);
                throw new RuntimeException("eek");
            }
        }
    }

       public void updateOtherPlayers(Stream stream) {
		int j = stream.readBits(8);
		if (j < game.playerCount) {
			for (int k = j; k < game.playerCount; k++) {
				game.removedEntityIndices[game.entityRemovalCount++] = game.playerIndices[k];
			}

		}
		if (j > game.playerCount) {
			Signlink.reporterror(game.myUsername + " Too many players");
			throw new RuntimeException("eek");
		}
		game.playerCount = 0;
		for (int l = 0; l < j; l++) {
			int i1 = game.playerIndices[l];
			Player player = game.playerArray[i1];
			int j1 = stream.readBits(1);
			if (j1 == 0) {
				game.playerIndices[game.playerCount++] = i1;
				player.lastUpdateCycle = game.loopCycle;
			} else {
				int k1 = stream.readBits(2);
				if (k1 == 0) {
					game.playerIndices[game.playerCount++] = i1;
					player.lastUpdateCycle = game.loopCycle;
					game.playerUpdateIndices[game.playerUpdateCount++] = i1;
				} else if (k1 == 1) {
					game.playerIndices[game.playerCount++] = i1;
					player.lastUpdateCycle = game.loopCycle;
					int l1 = stream.readBits(3);
					player.moveInDir(false, l1);
					int j2 = stream.readBits(1);
					if (j2 == 1) {
						game.playerUpdateIndices[game.playerUpdateCount++] = i1;
					}
				} else if (k1 == 2) {
					game.playerIndices[game.playerCount++] = i1;
					player.lastUpdateCycle = game.loopCycle;
					int i2 = stream.readBits(3);
					player.moveInDir(true, i2);
					int k2 = stream.readBits(3);
					player.moveInDir(true, k2);
					int l2 = stream.readBits(1);
					if (l2 == 1) {
						game.playerUpdateIndices[game.playerUpdateCount++] = i1;
					}
                                } else if (k1 == 3) {
                                        game.removedEntityIndices[game.entityRemovalCount++] = i1;
                                }
                        }
                }
        }
}
