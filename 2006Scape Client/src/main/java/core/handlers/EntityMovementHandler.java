package core.handlers;

import core.engine.Game;
import game.Entity;
import game.NPC;
import game.Player;
import game.Animation;
import game.SpotAnim;

/**
 * Updates entity movement logic extracted from {@link Game}.
 */
public final class EntityMovementHandler {
    private final Game game;

    public EntityMovementHandler(Game game) {
        this.game = game;
    }

    public void updateEntityMovement(Entity entity) {
        if (entity.x < 128 || entity.y < 128 || entity.x >= 13184 || entity.y >= 13184) {
            entity.anim = -1;
            entity.spotAnimId = -1;
            entity.forceMoveStartCycle = 0;
            entity.forceMoveEndCycle = 0;
            entity.x = entity.smallX[0] * 128 + entity.size * 64;
            entity.y = entity.smallY[0] * 128 + entity.size * 64;
            entity.clearMovement();
        }
        if (entity == game.myPlayer && (entity.x < 1536 || entity.y < 1536 || entity.x >= 11776 || entity.y >= 11776)) {
            entity.anim = -1;
            entity.spotAnimId = -1;
            entity.forceMoveStartCycle = 0;
            entity.forceMoveEndCycle = 0;
            entity.x = entity.smallX[0] * 128 + entity.size * 64;
            entity.y = entity.smallY[0] * 128 + entity.size * 64;
            entity.clearMovement();
        }
        if (entity.forceMoveStartCycle > game.loopCycle) {
            updateForcedMovement(entity);
        } else if (entity.forceMoveEndCycle >= game.loopCycle) {
            updateInterpolatedMovement(entity);
        } else {
            updateWalkingStep(entity);
        }
        game.entityAnimationHandler.updateEntityFacing(entity);
        game.entityAnimationHandler.updateEntityAnimation(entity);
    }

    void updateForcedMovement(Entity entity) {
        int i = entity.forceMoveStartCycle - game.loopCycle;
        int j = entity.forceMoveStartX * 128 + entity.size * 64;
        int k = entity.forceMoveStartY * 128 + entity.size * 64;
        entity.x += (j - entity.x) / i;
        entity.y += (k - entity.y) / i;
        entity.movementDelay = 0;
        if (entity.forceMoveDirection == 0) {
            entity.turnDirection = 1024;
        }
        if (entity.forceMoveDirection == 1) {
            entity.turnDirection = 1536;
        }
        if (entity.forceMoveDirection == 2) {
            entity.turnDirection = 0;
        }
        if (entity.forceMoveDirection == 3) {
            entity.turnDirection = 512;
        }
    }

    void updateInterpolatedMovement(Entity entity) {
        if (entity.forceMoveEndCycle == game.loopCycle || entity.anim == -1 || entity.graphicDelay != 0 ||
                entity.graphicFrameCycle + 1 > Animation.anims[entity.anim].getFrameDelay(entity.graphicFrame)) {
            int i = entity.forceMoveEndCycle - entity.forceMoveStartCycle;
            int j = game.loopCycle - entity.forceMoveStartCycle;
            int k = entity.forceMoveStartX * 128 + entity.size * 64;
            int l = entity.forceMoveStartY * 128 + entity.size * 64;
            int i1 = entity.forceMoveEndX * 128 + entity.size * 64;
            int j1 = entity.forceMoveEndY * 128 + entity.size * 64;
            entity.x = (k * (i - j) + i1 * j) / i;
            entity.y = (l * (i - j) + j1 * j) / i;
        }
        entity.movementDelay = 0;
        if (entity.forceMoveDirection == 0) {
            entity.turnDirection = 1024;
        }
        if (entity.forceMoveDirection == 1) {
            entity.turnDirection = 1536;
        }
        if (entity.forceMoveDirection == 2) {
            entity.turnDirection = 0;
        }
        if (entity.forceMoveDirection == 3) {
            entity.turnDirection = 512;
        }
        entity.currentHeading = entity.turnDirection;
    }

    void updateWalkingStep(Entity entity) {
        entity.currentAnimation = entity.standAnimation;
        if (entity.smallXYIndex == 0) {
            entity.movementDelay = 0;
            return;
        }
        if (entity.anim != -1 && entity.graphicDelay == 0) {
            Animation animation = Animation.anims[entity.anim];
            if (entity.animationDelay > 0 && animation.precedenceAnimating == 0) {
                entity.movementDelay++;
                return;
            }
            if (entity.animationDelay <= 0 && animation.precedenceWalking == 0) {
                entity.movementDelay++;
                return;
            }
        }
        int i = entity.x;
        int j = entity.y;
        int k = entity.smallX[entity.smallXYIndex - 1] * 128 + entity.size * 64;
        int l = entity.smallY[entity.smallXYIndex - 1] * 128 + entity.size * 64;
        if (k - i > 256 || k - i < -256 || l - j > 256 || l - j < -256) {
            entity.x = k;
            entity.y = l;
            return;
        }
        if (i < k) {
            if (j < l) {
                entity.turnDirection = 1280;
            } else if (j > l) {
                entity.turnDirection = 1792;
            } else {
                entity.turnDirection = 1536;
            }
        } else if (i > k) {
            if (j < l) {
                entity.turnDirection = 768;
            } else if (j > l) {
                entity.turnDirection = 256;
            } else {
                entity.turnDirection = 512;
            }
        } else if (j < l) {
            entity.turnDirection = 1024;
        } else {
            entity.turnDirection = 0;
        }
        int i1 = entity.turnDirection - entity.currentHeading & 0x7ff;
        if (i1 > 1024) {
            i1 -= 2048;
        }
        int j1 = entity.turn180Animation;
        if (i1 >= -256 && i1 <= 256) {
            j1 = entity.walkAnimation;
        } else if (i1 >= 256 && i1 < 768) {
            j1 = entity.turn90CCWAnimation;
        } else if (i1 >= -768 && i1 <= -256) {
            j1 = entity.turn90CWAnimation;
        }
        if (j1 == -1) {
            j1 = entity.walkAnimation;
        }
        entity.currentAnimation = j1;
        int k1 = 4;
        if (entity.currentHeading != entity.turnDirection && entity.interactingEntity == -1 && entity.turnSpeed != 0) {
            k1 = 2;
        }
        if (entity.smallXYIndex > 2) {
            k1 = 6;
        }
        if (entity.smallXYIndex > 3) {
            k1 = 8;
        }
        if (entity.movementDelay > 0 && entity.smallXYIndex > 1) {
            k1 = 8;
            entity.movementDelay--;
        }
        if (entity.movementQueueFlags[entity.smallXYIndex - 1]) {
            k1 <<= 1;
        }
        if (k1 >= 8 && entity.currentAnimation == entity.walkAnimation && entity.runAnimation != -1) {
            entity.currentAnimation = entity.runAnimation;
        }
        if (i < k) {
            entity.x += k1;
            if (entity.x > k) {
                entity.x = k;
            }
        } else if (i > k) {
            entity.x -= k1;
            if (entity.x < k) {
                entity.x = k;
            }
        }
        if (j < l) {
            entity.y += k1;
            if (entity.y > l) {
                entity.y = l;
            }
        } else if (j > l) {
            entity.y -= k1;
            if (entity.y < l) {
                entity.y = l;
            }
        }
        if (entity.x == k && entity.y == l) {
            entity.smallXYIndex--;
            if (entity.animationDelay > 0) {
                entity.animationDelay--;
            }
        }
    }
}
