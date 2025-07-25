package core.handlers;

import core.engine.Game;
import game.entities.Entity;
import game.entities.NPC;
import game.entities.Player;
import game.animation.Animation;
import game.animation.SpotAnim;

/**
 * Handles entity facing and animation logic extracted from {@link Game}.
 */
public final class EntityAnimationHandler {
    private final Game game;

    public EntityAnimationHandler(Game game) {
        this.game = game;
    }

    void updateEntityFacing(Entity entity) {
        if (entity.turnSpeed == 0) {
            return;
        }
        if (entity.interactingEntity != -1 && entity.interactingEntity < 32768) {
            NPC npc = game.npcArray[entity.interactingEntity];
            if (npc != null) {
                int i1 = entity.x - npc.x;
                int k1 = entity.y - npc.y;
                if (i1 != 0 || k1 != 0) {
                    entity.turnDirection = (int) (Math.atan2(i1, k1) * 325.94900000000001D) & 0x7ff;
                }
            }
        }
        if (entity.interactingEntity >= 32768) {
            int j = entity.interactingEntity - 32768;
            if (j == game.localPlayerIndex) {
                j = game.myPlayerIndex;
            }
            Player player = game.playerArray[j];
            if (player != null) {
                int l1 = entity.x - player.x;
                int i2 = entity.y - player.y;
                if (l1 != 0 || i2 != 0) {
                    entity.turnDirection = (int) (Math.atan2(l1, i2) * 325.94900000000001D) & 0x7ff;
                }
            }
        }
        if ((entity.focusX != 0 || entity.focusY != 0) && (entity.smallXYIndex == 0 || entity.movementDelay > 0)) {
            int k = entity.x - (entity.focusX - game.baseX - game.baseX) * 64;
            int j1 = entity.y - (entity.focusY - game.baseY - game.baseY) * 64;
            if (k != 0 || j1 != 0) {
                entity.turnDirection = (int) (Math.atan2(k, j1) * 325.94900000000001D) & 0x7ff;
            }
            entity.focusX = 0;
            entity.focusY = 0;
        }
        int l = entity.turnDirection - entity.currentHeading & 0x7ff;
        if (l != 0) {
            if (l < entity.turnSpeed || l > 2048 - entity.turnSpeed) {
                entity.currentHeading = entity.turnDirection;
            } else if (l > 1024) {
                entity.currentHeading -= entity.turnSpeed;
            } else {
                entity.currentHeading += entity.turnSpeed;
            }
            entity.currentHeading &= 0x7ff;
            if (entity.currentAnimation == entity.standAnimation && entity.currentHeading != entity.turnDirection) {
                if (entity.turnAnimation != -1) {
                    entity.currentAnimation = entity.turnAnimation;
                    return;
                }
                entity.currentAnimation = entity.walkAnimation;
            }
        }
    }

    void updateEntityAnimation(Entity entity) {
        entity.forcedAnimation = false;
        if (entity.currentAnimation != -1) {
            Animation animation = Animation.anims[entity.currentAnimation];
            entity.animationFrameCycle++;
            if (entity.animationFrame < animation.frameCount && entity.animationFrameCycle > animation.getFrameDelay(entity.animationFrame)) {
                entity.animationFrameCycle = 0;
                entity.animationFrame++;
            }
            if (entity.animationFrame >= animation.frameCount) {
                entity.animationFrameCycle = 0;
                entity.animationFrame = 0;
            }
        }
        if (entity.spotAnimId != -1 && game.loopCycle >= entity.spotAnimStartTick) {
            if (entity.spotAnimFrame < 0) {
                entity.spotAnimFrame = 0;
            }
            Animation animation_1 = SpotAnim.cache[entity.spotAnimId].animation;
            for (entity.spotAnimFrameCycle++; entity.spotAnimFrame < animation_1.frameCount && entity.spotAnimFrameCycle > animation_1.getFrameDelay(entity.spotAnimFrame); entity.spotAnimFrame++) {
                entity.spotAnimFrameCycle -= animation_1.getFrameDelay(entity.spotAnimFrame);
            }
            if (entity.spotAnimFrame >= animation_1.frameCount && (entity.spotAnimFrame < 0 || entity.spotAnimFrame >= animation_1.frameCount)) {
                entity.spotAnimId = -1;
            }
        }
        if (entity.anim != -1 && entity.graphicDelay <= 1) {
            Animation animation_2 = Animation.anims[entity.anim];
            if (animation_2.precedenceAnimating == 1 && entity.animationDelay > 0 && entity.forceMoveStartCycle <= game.loopCycle && entity.forceMoveEndCycle < game.loopCycle) {
                entity.graphicDelay = 1;
                return;
            }
        }
        if (entity.anim != -1 && entity.graphicDelay == 0) {
            Animation animation_3 = Animation.anims[entity.anim];
            for (entity.graphicFrameCycle++; entity.graphicFrame < animation_3.frameCount && entity.graphicFrameCycle > animation_3.getFrameDelay(entity.graphicFrame); entity.graphicFrame++) {
                entity.graphicFrameCycle -= animation_3.getFrameDelay(entity.graphicFrame);
            }
            if (entity.graphicFrame >= animation_3.frameCount) {
                entity.graphicFrame -= animation_3.frameStep;
                entity.graphicCycle++;
                if (entity.graphicCycle >= animation_3.maxLoops) {
                    entity.anim = -1;
                }
                if (entity.graphicFrame < 0 || entity.graphicFrame >= animation_3.frameCount) {
                    entity.anim = -1;
                }
            }
            entity.forcedAnimation = animation_3.stretches;
        }
        if (entity.graphicDelay > 0) {
            entity.graphicDelay--;
        }
    }
}
