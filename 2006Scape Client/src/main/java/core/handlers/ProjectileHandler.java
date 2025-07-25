package core.handlers;

import core.engine.Game;
import game.entities.NPC;
import game.entities.Player;
import game.entities.Projectile;

/**
 * Handles projectile processing extracted from {@link Game}.
 */
public final class ProjectileHandler {
    private final Game game;

    public ProjectileHandler(Game game) {
        this.game = game;
    }

    public void processProjectiles() {
        for (Projectile projectile = (Projectile) game.projectileList.reverseGetFirst(); projectile != null; projectile = (Projectile) game.projectileList.reverseGetNext()) {
            if (projectile.plane != game.plane || game.loopCycle > projectile.endCycle) {
                projectile.unlink();
            } else if (game.loopCycle >= projectile.startCycle) {
                if (projectile.targetIndex > 0) {
                    NPC npc = game.npcArray[projectile.targetIndex - 1];
                    if (npc != null && npc.x >= 0 && npc.x < 13312 && npc.y >= 0 && npc.y < 13312) {
                        projectile.track(game.loopCycle, npc.y, game.getTileHeight(projectile.plane, npc.y, npc.x) - projectile.heightOffset, npc.x);
                    }
                }
                if (projectile.targetIndex < 0) {
                    int j = -projectile.targetIndex - 1;
                    Player player;
                    if (j == game.localPlayerIndex) {
                        player = game.myPlayer;
                    } else {
                        player = game.playerArray[j];
                    }
                    if (player != null && player.x >= 0 && player.x < 13312 && player.y >= 0 && player.y < 13312) {
                        projectile.track(game.loopCycle, player.y, game.getTileHeight(projectile.plane, player.y, player.x) - projectile.heightOffset, player.x);
                    }
                }
                projectile.update(game.animationCycle);
                game.worldController.addAnimableObject(game.plane, projectile.yaw, (int) projectile.currentHeight, -1, (int) projectile.currentY, 60, (int) projectile.currentX, projectile, false);
            }
        }
    }
}