package core.world;

import core.engine.Game;
import game.definitions.ObjectDef;

/** Performs A* style walking path calculations extracted from {@link Game}. */
public final class Pathfinder {
  private final Game game;

  public Pathfinder(Game game) {
    this.game = game;
  }

  public boolean doWalkTo(
      int i, int j, int k, int i1, int j1, int k1, int l1, int i2, int j2, boolean flag, int k2) {
    byte byte0 = 104;
    byte byte1 = 104;
    for (int l2 = 0; l2 < byte0; l2++) {
      for (int i3 = 0; i3 < byte1; i3++) {
        game.pathDirections[l2][i3] = 0;
        game.pathDistances[l2][i3] = 0x5f5e0ff;
      }
    }
    int j3 = j2;
    int k3 = j1;
    game.pathDirections[j2][j1] = 99;
    game.pathDistances[j2][j1] = 0;
    int l3 = 0;
    int i4 = 0;
    game.pathTileX[l3] = j2;
    game.pathTileY[l3++] = j1;
    boolean flag1 = false;
    int j4 = game.pathTileX.length;
    int[][] ai = game.collisionMaps[game.plane].clippingFlags;
    while (i4 != l3) {
      j3 = game.pathTileX[i4];
      k3 = game.pathTileY[i4];
      i4 = (i4 + 1) % j4;
      if (j3 == k2 && k3 == i2) {
        flag1 = true;
        break;
      }
      if (i1 != 0) {
        if ((i1 < 5 || i1 == 10)
            && game.collisionMaps[game.plane].canReachWall(k2, j3, k3, j, i1 - 1, i2)) {
          flag1 = true;
          break;
        }
        if (i1 < 10 && game.collisionMaps[game.plane].canReachObject(k2, i2, k3, i1 - 1, j, j3)) {
          flag1 = true;
          break;
        }
      }
      if (k1 != 0
          && k != 0
          && game.collisionMaps[game.plane].canReachArea(i2, k2, j3, k, l1, k1, k3)) {
        flag1 = true;
        break;
      }
      int l4 = game.pathDistances[j3][k3] + 1;
      if (j3 > 0 && game.pathDirections[j3 - 1][k3] == 0 && (ai[j3 - 1][k3] & 0x1280108) == 0) {
        game.pathTileX[l3] = j3 - 1;
        game.pathTileY[l3] = k3;
        l3 = (l3 + 1) % j4;
        game.pathDirections[j3 - 1][k3] = 2;
        game.pathDistances[j3 - 1][k3] = l4;
      }
      if (j3 < byte0 - 1
          && game.pathDirections[j3 + 1][k3] == 0
          && (ai[j3 + 1][k3] & 0x1280180) == 0) {
        game.pathTileX[l3] = j3 + 1;
        game.pathTileY[l3] = k3;
        l3 = (l3 + 1) % j4;
        game.pathDirections[j3 + 1][k3] = 8;
        game.pathDistances[j3 + 1][k3] = l4;
      }
      if (k3 > 0 && game.pathDirections[j3][k3 - 1] == 0 && (ai[j3][k3 - 1] & 0x1280102) == 0) {
        game.pathTileX[l3] = j3;
        game.pathTileY[l3] = k3 - 1;
        l3 = (l3 + 1) % j4;
        game.pathDirections[j3][k3 - 1] = 1;
        game.pathDistances[j3][k3 - 1] = l4;
      }
      if (k3 < byte1 - 1
          && game.pathDirections[j3][k3 + 1] == 0
          && (ai[j3][k3 + 1] & 0x1280120) == 0) {
        game.pathTileX[l3] = j3;
        game.pathTileY[l3] = k3 + 1;
        l3 = (l3 + 1) % j4;
        game.pathDirections[j3][k3 + 1] = 4;
        game.pathDistances[j3][k3 + 1] = l4;
      }
      if (j3 > 0
          && k3 > 0
          && game.pathDirections[j3 - 1][k3 - 1] == 0
          && (ai[j3 - 1][k3 - 1] & 0x128010e) == 0
          && (ai[j3 - 1][k3] & 0x1280108) == 0
          && (ai[j3][k3 - 1] & 0x1280102) == 0) {
        game.pathTileX[l3] = j3 - 1;
        game.pathTileY[l3] = k3 - 1;
        l3 = (l3 + 1) % j4;
        game.pathDirections[j3 - 1][k3 - 1] = 3;
        game.pathDistances[j3 - 1][k3 - 1] = l4;
      }
      if (j3 < byte0 - 1
          && k3 > 0
          && game.pathDirections[j3 + 1][k3 - 1] == 0
          && (ai[j3 + 1][k3 - 1] & 0x1280183) == 0
          && (ai[j3 + 1][k3] & 0x1280180) == 0
          && (ai[j3][k3 - 1] & 0x1280102) == 0) {
        game.pathTileX[l3] = j3 + 1;
        game.pathTileY[l3] = k3 - 1;
        l3 = (l3 + 1) % j4;
        game.pathDirections[j3 + 1][k3 - 1] = 9;
        game.pathDistances[j3 + 1][k3 - 1] = l4;
      }
      if (j3 > 0
          && k3 < byte1 - 1
          && game.pathDirections[j3 - 1][k3 + 1] == 0
          && (ai[j3 - 1][k3 + 1] & 0x1280138) == 0
          && (ai[j3 - 1][k3] & 0x1280108) == 0
          && (ai[j3][k3 + 1] & 0x1280120) == 0) {
        game.pathTileX[l3] = j3 - 1;
        game.pathTileY[l3] = k3 + 1;
        l3 = (l3 + 1) % j4;
        game.pathDirections[j3 - 1][k3 + 1] = 6;
        game.pathDistances[j3 - 1][k3 + 1] = l4;
      }
      if (j3 < byte0 - 1
          && k3 < byte1 - 1
          && game.pathDirections[j3 + 1][k3 + 1] == 0
          && (ai[j3 + 1][k3 + 1] & 0x12801e0) == 0
          && (ai[j3 + 1][k3] & 0x1280180) == 0
          && (ai[j3][k3 + 1] & 0x1280120) == 0) {
        game.pathTileX[l3] = j3 + 1;
        game.pathTileY[l3] = k3 + 1;
        l3 = (l3 + 1) % j4;
        game.pathDirections[j3 + 1][k3 + 1] = 12;
        game.pathDistances[j3 + 1][k3 + 1] = l4;
      }
    }
    game.alternatePathFound = 0;
    if (!flag1) {
      if (flag) {
        int i5 = 100;
        for (int k5 = 1; k5 < 2; k5++) {
          for (int i6 = k2 - k5; i6 <= k2 + k5; i6++) {
            for (int l6 = i2 - k5; l6 <= i2 + k5; l6++) {
              if (i6 >= 0 && l6 >= 0 && i6 < 104 && l6 < 104 && game.pathDistances[i6][l6] < i5) {
                i5 = game.pathDistances[i6][l6];
                j3 = i6;
                k3 = l6;
                game.alternatePathFound = 1;
                flag1 = true;
              }
            }
          }

          if (flag1) {
            break;
          }
        }
      }
      if (!flag1) {
        return false;
      }
    }
    i4 = 0;
    game.pathTileX[i4] = j3;
    game.pathTileY[i4++] = k3;
    int l5;
    for (int j5 = l5 = game.pathDirections[j3][k3];
        j3 != j2 || k3 != j1;
        j5 = game.pathDirections[j3][k3]) {
      if (j5 != l5) {
        l5 = j5;
        game.pathTileX[i4] = j3;
        game.pathTileY[i4++] = k3;
      }
      if ((j5 & 2) != 0) {
        j3++;
      } else if ((j5 & 8) != 0) {
        j3--;
      }
      if ((j5 & 1) != 0) {
        k3++;
      } else if ((j5 & 4) != 0) {
        k3--;
      }
    }
    if (i4 > 0) {
      int k4 = i4;
      if (k4 > 25) {
        k4 = 25;
      }
      i4--;
      int k6 = game.pathTileX[i4];
      int i7 = game.pathTileY[i4];
      Game.walkPacketCounter += k4;
      if (Game.walkPacketCounter >= 92) {
        game.stream.createFrame(36);
        game.stream.writeDWord(0);
        Game.walkPacketCounter = 0;
      }
      if (i == 0) {
        game.stream.createFrame(164);
        game.stream.writeWordBigEndian(k4 + k4 + 3);
      }
      if (i == 1) {
        game.stream.createFrame(248);
        game.stream.writeWordBigEndian(k4 + k4 + 3 + 14);
      }
      if (i == 2) {
        game.stream.createFrame(98);
        game.stream.writeWordBigEndian(k4 + k4 + 3);
      }
      game.stream.writeShortLEA(k6 + game.baseX);
      game.destX = game.pathTileX[0];
      game.destY = game.pathTileY[0];
      for (int j7 = 1; j7 < k4; j7++) {
        i4--;
        game.stream.writeWordBigEndian(game.pathTileX[i4] - k6);
        game.stream.writeWordBigEndian(game.pathTileY[i4] - i7);
      }

      game.stream.writeShortLEDup(i7 + game.baseY);
      game.stream.writeByteNeg(game.keyArray[5] != 1 ? 0 : 1);
      return true;
    }
    return i != 1;
  }

  public boolean walkToObject(int i, int j, int k) {
    int i1 = i >> 14 & 0x7fff;
    int j1 = game.worldController.getObjectConfig(game.plane, k, j, i);
    if (j1 == -1) {
      return false;
    }
    int k1 = j1 & 0x1f;
    int l1 = j1 >> 6 & 3;
    if (k1 == 10 || k1 == 11 || k1 == 22) {
      ObjectDef objectDef = ObjectDef.forID(i1);
      int i2;
      int j2;
      if (l1 == 0 || l1 == 2) {
        i2 = objectDef.sizeX;
        j2 = objectDef.sizeY;
      } else {
        i2 = objectDef.sizeY;
        j2 = objectDef.sizeX;
      }
      int k2 = objectDef.defaultOrientation;
      if (l1 != 0) {
        k2 = (k2 << l1 & 0xf) + (k2 >> 4 - l1);
      }
      doWalkTo(2, 0, j2, 0, game.myPlayer.smallY[0], i2, k2, j, game.myPlayer.smallX[0], false, k);
    } else {
      doWalkTo(2, l1, 0, k1 + 1, game.myPlayer.smallY[0], 0, 0, j, game.myPlayer.smallX[0], false, k);
    }
    game.crossX = game.saveClickX;
    game.crossY = game.saveClickY;
    game.crossType = 2;
    game.crossIndex = 0;
    return true;
  }
}
