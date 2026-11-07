import static org.junit.Assert.*;
import org.junit.Test;

public class EntityTurningTest {
    private Game game() {
        Game game = new Game();
        game.npcArray = new NPC[8];
        game.playerArray = new Player[8];
        return game;
    }

    private Entity entity() {
        Entity entity = new Entity();
        entity.x = 1024;
        entity.y = 1024;
        entity.turnSpeed = 32;
        entity.interactingEntity = -1;
        entity.idleAnimationId = 10;
        entity.turnInPlaceAnimationId = 11;
        entity.walkAnimationId = 12;
        entity.movementAnimationId = 10;
        return entity;
    }

    @Test public void zeroTurnRateLeavesTargetAndAnimationUntouched() {
        Game game = game(); Entity entity = entity();
        entity.turnSpeed = 0; entity.turnDirection = 512;
        entity.interactingEntity = 2;
        NPC target = new NPC(); target.x = 1024; target.y = 896;
        game.npcArray[2] = target;
        game.updateEntityTurning(entity);
        assertEquals(512, entity.turnDirection);
        assertEquals(0, entity.facingDirection);
        assertEquals(10, entity.movementAnimationId);
    }

    @Test public void npcAxesSetFacingAndTurnAnimation() {
        Game game = game(); Entity entity = entity();
        NPC target = new NPC(); game.npcArray[2] = target;
        entity.interactingEntity = 2;
        target.x = 1024; target.y = 896;
        game.updateEntityTurning(entity);
        assertEquals(0, entity.turnDirection);
        assertEquals(10, entity.movementAnimationId);
        target.x = 896; target.y = 1024;
        game.updateEntityTurning(entity);
        assertEquals(511, entity.turnDirection);
        assertEquals(32, entity.facingDirection);
        assertEquals(11, entity.movementAnimationId);
        entity.turnDirection = 777; target.x = entity.x; target.y = entity.y;
        game.updateEntityTurning(entity);
        assertEquals(777, entity.turnDirection);
    }

    @Test public void playerSlotOffsetSelectsOnlyRequestedPlayer() {
        Game game = game(); Entity entity = entity();
        Player east = new Player(); east.x = 896; east.y = 1024;
        Player north = new Player(); north.x = 1024; north.y = 1152;
        game.playerArray[2] = east; game.playerArray[3] = north;
        entity.interactingEntity = 32770;
        game.updateEntityTurning(entity);
        assertEquals(511, entity.turnDirection);
        entity.interactingEntity = 32771;
        game.updateEntityTurning(entity);
        assertEquals(1023, entity.turnDirection);
        entity.interactingEntity = 32772;
        game.updateEntityTurning(entity);
        assertEquals(1023, entity.turnDirection);
    }

    @Test public void faceCoordinateWaitsForWalkingQueueThenClears() {
        Game game = game(); Entity entity = entity();
        entity.faceHalfTileX = 14; entity.faceHalfTileY = 16;
        entity.smallXYIndex = 1; entity.deferredMovementTicks = 0;
        game.updateEntityTurning(entity);
        assertEquals(14, entity.faceHalfTileX);
        assertEquals(16, entity.faceHalfTileY);
        entity.deferredMovementTicks = 1;
        game.updateEntityTurning(entity);
        assertEquals(511, entity.turnDirection);
        assertEquals(0, entity.faceHalfTileX);
        assertEquals(0, entity.faceHalfTileY);
        entity.faceHalfTileX = 16; entity.faceHalfTileY = 18;
        entity.smallXYIndex = 0;
        game.updateEntityTurning(entity);
        assertEquals(1023, entity.turnDirection);
        assertEquals(0, entity.faceHalfTileX);
        assertEquals(0, entity.faceHalfTileY);
    }

    @Test public void modularTurnTakesShortestStepAndSnapsAtBoundary() {
        Game game = game(); Entity entity = entity();
        entity.turnDirection = 8; entity.facingDirection = 2040;
        game.updateEntityTurning(entity);
        assertEquals(8, entity.facingDirection);
        entity.turnDirection = 1536; entity.facingDirection = 0;
        entity.turnInPlaceAnimationId = -1; entity.movementAnimationId = entity.idleAnimationId;
        game.updateEntityTurning(entity);
        assertEquals(2016, entity.facingDirection);
        assertEquals(12, entity.movementAnimationId);
        entity.turnDirection = 1024; entity.facingDirection = 0;
        game.updateEntityTurning(entity);
        assertEquals(32, entity.facingDirection);
    }
}
