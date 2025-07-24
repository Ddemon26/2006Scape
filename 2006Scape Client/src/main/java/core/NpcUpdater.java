package core;

import net.Signlink;
import net.Stream;

import game.EntityDef;
import game.Animation;
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
        processNpcUpdateMasks(stream);
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

    private void processNpcUpdateMasks(Stream stream) {
        for (int j = 0; j < game.playerUpdateCount; j++) {
            int k = game.playerUpdateIndices[j];
            NPC npc = game.npcArray[k];
            int l = stream.readUnsignedByte();
            if ((l & 0x10) != 0) {
                int i1 = stream.readShortLE();
                if (i1 == 0x00ffff) {
                    i1 = -1;
                }
                int i2 = stream.readUnsignedByte();
                if (i1 == npc.anim && i1 != -1) {
                    int l2 = Animation.anims[i1].replayMode;
                    if (l2 == 1) {
                        npc.graphicFrame = 0;
                        npc.graphicFrameCycle = 0;
                        npc.graphicDelay = i2;
                        npc.graphicCycle = 0;
                    }
                    if (l2 == 2) {
                        npc.graphicCycle = 0;
                    }
                } else if (i1 == -1 || npc.anim == -1 || Animation.anims[i1].priority >= Animation.anims[npc.anim].priority) {
                    npc.anim = i1;
                    npc.graphicFrame = 0;
                    npc.graphicFrameCycle = 0;
                    npc.graphicDelay = i2;
                    npc.graphicCycle = 0;
                    npc.animationDelay = npc.smallXYIndex;
                }
            }
            if ((l & 8) != 0) {
                int j1 = stream.readUnsignedByteA();
                int j2 = stream.readUnsignedByteNeg();
                npc.updateHitData(j2, j1, game.loopCycle);
                npc.loopCycleStatus = game.loopCycle + 300;
                npc.currentHealth = stream.readUnsignedByteA();
                npc.maxHealth = stream.readUnsignedByte();
            }
            if ((l & 0x80) != 0) {
                npc.spotAnimId = stream.readUnsignedWord();
                int k1 = stream.readDWord();
                npc.spotAnimHeight = k1 >> 16;
                npc.spotAnimStartTick = game.loopCycle + (k1 & 0xffff);
                npc.spotAnimFrame = 0;
                npc.spotAnimFrameCycle = 0;
                if (npc.spotAnimStartTick > game.loopCycle) {
                    npc.spotAnimFrame = -1;
                }
                if (npc.spotAnimId == 0x00ffff) {
                    npc.spotAnimId = -1;
                }
            }
            if ((l & 0x20) != 0) {
                npc.interactingEntity = stream.readUnsignedWord();
                if (npc.interactingEntity == 0x00ffff) {
                    npc.interactingEntity = -1;
                }
            }
            if ((l & 1) != 0) {
                npc.textSpoken = stream.readString();
                npc.textCycle = 100;
            }
            if ((l & 0x40) != 0) {
                int l1 = stream.readUnsignedByteNeg();
                int k2 = stream.readUnsignedByteSub();
                npc.updateHitData(k2, l1, game.loopCycle);
                npc.loopCycleStatus = game.loopCycle + 300;
                npc.currentHealth = stream.readUnsignedByteSub();
                npc.maxHealth = stream.readUnsignedByteNeg();
            }
            if ((l & 2) != 0) {
                npc.definition = EntityDef.forID(stream.readShortLEAdd());
                npc.size = npc.definition.size;
                npc.turnSpeed = npc.definition.turnSpeed;
                npc.walkAnimation = npc.definition.walkAnimation;
                npc.turn180Animation = npc.definition.turn180Animation;
                npc.turn90CWAnimation = npc.definition.turn90CWAnimation;
                npc.turn90CCWAnimation = npc.definition.turn90CCWAnimation;
                npc.standAnimation = npc.definition.standAnimation;
            }
            if ((l & 4) != 0) {
                npc.focusX = stream.readShortLE();
                npc.focusY = stream.readShortLE();
            }
        }
    }
}
