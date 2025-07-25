package core.renderers;

import core.engine.Game;
import game.entities.NPC;
import game.entities.Player;

/** Handles scene rendering logic for players and NPCs extracted from {@link Game}. */
public final class SceneRenderer {
  private final Game game;

  public SceneRenderer(Game game) {
    this.game = game;
  }

  public void addPlayersToScene(boolean flag) {
    if (game.myPlayer.x >> 7 == game.destX && game.myPlayer.y >> 7 == game.destY) {
      game.destX = 0;
    }
    int j = game.playerCount;
    if (flag) {
      j = 1;
    }
    for (int l = 0; l < j; l++) {
      Player player;
      int i1;
      if (flag) {
        player = game.myPlayer;
        i1 = game.myPlayerIndex << 14;
      } else {
        player = game.playerArray[game.playerIndices[l]];
        i1 = game.playerIndices[l] << 14;
      }
      if (player == null || !player.isVisible()) {
        continue;
      }
      player.skipAnimations =
          (game.lowMem && game.playerCount > 50 || game.playerCount > 200)
              && !flag
              && player.currentAnimation == player.standAnimation;
      int j1 = player.x >> 7;
      int k1 = player.y >> 7;
      if (j1 < 0 || j1 >= 104 || k1 < 0 || k1 >= 104) {
        continue;
      }
      if (player.overlayModel != null
          && game.loopCycle >= player.animationStartCycle
          && game.loopCycle < player.animationEndCycle) {
        player.skipAnimations = false;
        player.animationBaseY = game.getTileHeight(game.plane, player.y, player.x);
        game.worldController.addAnimatingObject(
            game.plane,
            player.y,
            player,
            player.currentHeading,
            player.boundingBoxMaxY,
            player.x,
            player.animationBaseY,
            player.boundingBoxMinX,
            player.boundingBoxMaxX,
            i1,
            player.boundingBoxMinY);
        continue;
      }
      if ((player.x & 0x7f) == 64 && (player.y & 0x7f) == 64) {
        if (game.occupiedTiles[j1][k1] == game.waveCycle) {
          continue;
        }
        game.occupiedTiles[j1][k1] = game.waveCycle;
      }
      player.animationBaseY = game.getTileHeight(game.plane, player.y, player.x);
      game.worldController.addAnimableObject(
          game.plane,
          player.currentHeading,
          player.animationBaseY,
          i1,
          player.y,
          60,
          player.x,
          player,
          player.forcedAnimation);
    }
  }

  public void addNpcsToScene(boolean flag) {
    for (int j = 0; j < game.npcCount; j++) {
      NPC npc = game.npcArray[game.npcIndices[j]];
      int k = 0x20000000 + (game.npcIndices[j] << 14);
      if (npc == null || !npc.isVisible() || npc.definition.priorityRender != flag) {
        continue;
      }
      int l = npc.x >> 7;
      int i1 = npc.y >> 7;
      if (l < 0 || l >= 104 || i1 < 0 || i1 >= 104) {
        continue;
      }
      if (npc.size == 1 && (npc.x & 0x7f) == 64 && (npc.y & 0x7f) == 64) {
        if (game.occupiedTiles[l][i1] == game.waveCycle) {
          continue;
        }
        game.occupiedTiles[l][i1] = game.waveCycle;
      }
      if (!npc.definition.clickable) {
        k += 0x80000000;
      }
      game.worldController.addAnimableObject(
          game.plane,
          npc.currentHeading,
          game.getTileHeight(game.plane, npc.y, npc.x),
          k,
          npc.y,
          (npc.size - 1) * 64 + 60,
          npc.x,
          npc,
          npc.forcedAnimation);
    }
  }
}
