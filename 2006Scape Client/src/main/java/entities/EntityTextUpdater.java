package entities;

import core.engine.Game;
import game.entities.NPC;
import game.entities.Player;

/** Clears expired overhead text for players and NPCs, extracted from {@link Game}. */
public final class EntityTextUpdater {
  private final Game game;

  public EntityTextUpdater(Game game) {
    this.game = game;
  }

  public void updateEntityText() {
    for (int i = -1; i < game.playerCount; i++) {
      int j;
      if (i == -1) {
        j = game.myPlayerIndex;
      } else {
        j = game.playerIndices[i];
      }
      Player player = game.playerArray[j];
      if (player != null && player.textCycle > 0) {
        player.textCycle--;
        if (player.textCycle == 0) {
          player.textSpoken = null;
        }
      }
    }

    for (int k = 0; k < game.npcCount; k++) {
      int l = game.npcIndices[k];
      NPC npc = game.npcArray[l];
      if (npc != null && npc.textCycle > 0) {
        npc.textCycle--;
        if (npc.textCycle == 0) {
          npc.textSpoken = null;
        }
      }
    }
  }
}
