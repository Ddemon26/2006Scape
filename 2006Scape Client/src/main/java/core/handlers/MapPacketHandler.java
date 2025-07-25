package core.handlers;

import core.engine.Game;
import game.items.Item;
import game.definitions.ObjectDef;
import game.entities.Player;
import game.entities.GraphicsObject;
import game.entities.Projectile;
import audio.Sounds;
import core.network.Stream;
import render.geometry.Model;
import render.objects.*;
import util.collections.NodeList;

/**
 * Processes map-related packets extracted from {@link Game}.
 */
public final class MapPacketHandler {
    private final Game game;

    public MapPacketHandler(Game game) {
        this.game = game;
    }

    public void handleMapPackets(Stream stream, int j) {
		if (j == 84) {
			int k = stream.readUnsignedByte();
			int j3 = game.mapEventX + (k >> 4 & 7);
			int i6 = game.mapEventY + (k & 7);
			int l8 = stream.readUnsignedWord();
			int k11 = stream.readUnsignedWord();
			int l13 = stream.readUnsignedWord();
			if (j3 >= 0 && i6 >= 0 && j3 < 104 && i6 < 104) {
                                NodeList itemList1 = game.groundArray[game.plane][j3][i6];
                                if (itemList1 != null) {
                                        for (Item itemToUpdate = (Item) itemList1.reverseGetFirst(); itemToUpdate != null; itemToUpdate = (Item) itemList1.reverseGetNext()) {
						if (itemToUpdate.ID != (l8 & 0x7fff) || itemToUpdate.amount != k11) {
							continue;
						}
						itemToUpdate.amount = l13;
						break;
					}

                                        game.spawnGroundItem(j3, i6);
                                }
			}
			return;
		}
		if (j == 105) {
			int l = stream.readUnsignedByte();
			int k3 = game.mapEventX + (l >> 4 & 7);
			int j6 = game.mapEventY + (l & 7);
			int i9 = stream.readUnsignedWord();
			int l11 = stream.readUnsignedByte();
			int i14 = l11 >> 4 & 0xf;
			int i16 = l11 & 7;
			if (game.myPlayer.smallX[0] >= k3 - i14 && game.myPlayer.smallX[0] <= k3 + i14 && game.myPlayer.smallY[0] >= j6 - i14 && game.myPlayer.smallY[0] <= j6 + i14 && game.soundEffectEnabled && !game.lowMem && game.currentSound < 50) {
				game.sound[game.currentSound] = i9;
				game.soundType[game.currentSound] = i16;
                                game.soundDelay[game.currentSound] = Sounds.delays[i9];
				game.currentSound++;
			}
		}
		if (j == 215) {
			int i1 = stream.readShortAdd();
			int l3 = stream.readUnsignedByteSub();
			int k6 = game.mapEventX + (l3 >> 4 & 7);
			int j9 = game.mapEventY + (l3 & 7);
			int i12 = stream.readShortAdd();
			int j14 = stream.readUnsignedWord();
			if (k6 >= 0 && j9 >= 0 && k6 < 104 && j9 < 104 && i12 != game.localPlayerIndex) {
				Item newItem = new Item();
				newItem.ID = i1;
				newItem.amount = j14;
				if (game.groundArray[game.plane][k6][j9] == null) {
					game.groundArray[game.plane][k6][j9] = new NodeList();
				}
				game.groundArray[game.plane][k6][j9].insertHead(newItem);
				game.spawnGroundItem(k6, j9);
			}
			return;
		}
		if (j == 156) {
			int j1 = stream.readUnsignedByteA();
			int i4 = game.mapEventX + (j1 >> 4 & 7);
			int l6 = game.mapEventY + (j1 & 7);
			int k9 = stream.readUnsignedWord();
			if (i4 >= 0 && l6 >= 0 && i4 < 104 && l6 < 104) {
                                NodeList itemList = game.groundArray[game.plane][i4][l6];
                                if (itemList != null) {
                                        for (Item item = (Item) itemList.reverseGetFirst(); item != null; item = (Item) itemList.reverseGetNext()) {
						if (item.ID != (k9 & 0x7fff)) {
							continue;
						}
						item.unlink();
						break;
					}

                                        if (itemList.reverseGetFirst() == null) {
                                                game.groundArray[game.plane][i4][l6] = null;
                                        }
					game.spawnGroundItem(i4, l6);
				}
			}
			return;
		}
		if (j == 160) {
			int k1 = stream.readUnsignedByteSub();
			int j4 = game.mapEventX + (k1 >> 4 & 7);
			int i7 = game.mapEventY + (k1 & 7);
			int l9 = stream.readUnsignedByteSub();
			int j12 = l9 >> 2;
			int k14 = l9 & 3;
			int j16 = game.objectData[j12];
			int j17 = stream.readShortAdd();
			if (j4 >= 0 && i7 >= 0 && j4 < 103 && i7 < 103) {
				int j18 = game.tileHeights[game.plane][j4][i7];
				int i19 = game.tileHeights[game.plane][j4 + 1][i7];
				int l19 = game.tileHeights[game.plane][j4 + 1][i7 + 1];
				int k20 = game.tileHeights[game.plane][j4][i7 + 1];
				if (j16 == 0) {
                                       BoundaryObject boundaryObject = game.worldController.getBoundaryObject(game.plane, j4, i7);
                                        if (boundaryObject != null) {
                                                int k21 = boundaryObject.uid >> 14 & 0x7fff;
                                                if (j12 == 2) {
                                                        boundaryObject.primary = new DynamicObject(k21, 4 + k14, 2, i19, l19, j18, k20, j17, false);
                                                        boundaryObject.secondary = new DynamicObject(k21, k14 + 1 & 3, 2, i19, l19, j18, k20, j17, false);
                                                } else {
                                                        boundaryObject.primary = new DynamicObject(k21, k14, j12, i19, l19, j18, k20, j17, false);
                                                }
                                        }
				}
                                if (j16 == 1) {
                                       WallDecoration decoration = game.worldController.getWallDecoration(j4, i7, game.plane);
                                        if (decoration != null) {
                                                decoration.renderable = new DynamicObject(decoration.uid >> 14 & 0x7fff, 0, 4, i19, l19, j18, k20, j17, false);
                                        }
                                }
                                if (j16 == 2) {
                                       SceneObject sceneObject = game.worldController.getSceneObject(j4, i7, game.plane);
                                        if (j12 == 11) {
                                                j12 = 10;
                                        }
                                        if (sceneObject != null) {
                                                sceneObject.renderable = new DynamicObject(sceneObject.uid >> 14 & 0x7fff, k14, j12, i19, l19, j18, k20, j17, false);
                                        }
				}
				if (j16 == 3) {
                                       TileDecoration tileDecoration = game.worldController.getTileDecoration(i7, j4, game.plane);
                                        if (tileDecoration != null) {
                                                tileDecoration.renderable = new DynamicObject(tileDecoration.uid >> 14 & 0x7fff, k14, 22, i19, l19, j18, k20, j17, false);
                                        }
				}
			}
			return;
		}
		if (j == 147) {
			int l1 = stream.readUnsignedByteSub();
			int k4 = game.mapEventX + (l1 >> 4 & 7);
			int j7 = game.mapEventY + (l1 & 7);
			int i10 = stream.readUnsignedWord();
			byte byte0 = stream.readByteSub();
			int l14 = stream.readShortLE();
			byte byte1 = stream.readByteNeg();
			int k17 = stream.readUnsignedWord();
			int k18 = stream.readUnsignedByteSub();
			int j19 = k18 >> 2;
			int i20 = k18 & 3;
			int l20 = game.objectData[j19];
			byte byte2 = stream.readSignedByte();
			int l21 = stream.readUnsignedWord();
			byte byte3 = stream.readByteNeg();
			Player player;
			if (i10 == game.localPlayerIndex) {
				player = game.myPlayer;
			} else {
				player = game.playerArray[i10];
			}
			if (player != null) {
				ObjectDef objectDef = ObjectDef.forID(l21);
				int i22 = game.tileHeights[game.plane][k4][j7];
				int j22 = game.tileHeights[game.plane][k4 + 1][j7];
				int k22 = game.tileHeights[game.plane][k4 + 1][j7 + 1];
				int l22 = game.tileHeights[game.plane][k4][j7 + 1];
				Model model = objectDef.getModel(j19, i20, i22, j22, k22, l22, -1);
				if (model != null) {
					game.queuePendingSpawn(k17 + 1, -1, 0, l20, j7, 0, game.plane, k4, l14 + 1);
                                player.animationStartCycle = l14 + game.loopCycle;
                                player.animationEndCycle = k17 + game.loopCycle;
                                        player.overlayModel = model;
					int i23 = objectDef.sizeX;
					int j23 = objectDef.sizeY;
					if (i20 == 1 || i20 == 3) {
						i23 = objectDef.sizeY;
						j23 = objectDef.sizeX;
					}
                                player.animationBaseX = k4 * 128 + i23 * 64;
                                player.animationBaseZ = j7 * 128 + j23 * 64;
                                player.animationBaseHeight = game.getTileHeight(game.plane, player.animationBaseZ, player.animationBaseX);
					if (byte2 > byte0) {
						byte byte4 = byte2;
						byte2 = byte0;
						byte0 = byte4;
					}
					if (byte3 > byte1) {
						byte byte5 = byte3;
						byte3 = byte1;
						byte1 = byte5;
					}
					player.boundingBoxMinX = k4 + byte2;
					player.boundingBoxMaxX = k4 + byte0;
					player.boundingBoxMinY = j7 + byte3;
					player.boundingBoxMaxY = j7 + byte1;
				}
			}
		}
		if (j == 101) {
			int l2 = stream.readUnsignedByteNeg();
			int k5 = l2 >> 2;
			int j8 = l2 & 3;
			int i11 = game.objectData[k5];
			int j13 = stream.readUnsignedByte();
			int k15 = game.mapEventX + (j13 >> 4 & 7);
			int l16 = game.mapEventY + (j13 & 7);

			if (k15 >= 0 && l16 >= 0 && k15 < 104 && l16 < 104) {
				game.queuePendingSpawn(-1, -1, j8, i11, l16, k5, game.plane, k15, 0);
			}
			return;
		}
		if (j == 151) {
                        int i2 = stream.readUnsignedByteAdd();
			int l4 = game.mapEventX + (i2 >> 4 & 7);
			int k7 = game.mapEventY + (i2 & 7);
			int j10 = stream.readShortLE();
			int k12 = stream.readUnsignedByteSub();
			int i15 = k12 >> 2;
			int k16 = k12 & 3;
			int l17 = game.objectData[i15];
			//System.out.println("id: " + j10 + " x:" + (this.baseX + game.mapEventX) + " y:" + (this.baseY + game.mapEventY));
			if (l4 >= 0 && k7 >= 0 && l4 < 104 && k7 < 104)
				game.queuePendingSpawn(-1, j10, k16, l17, k7, i15, game.plane, l4, 0);
			return;
		}

		if (j == 4) {
			int j2 = stream.readUnsignedByte();
			int i5 = game.mapEventX + (j2 >> 4 & 7);
			int l7 = game.mapEventY + (j2 & 7);
			int k10 = stream.readUnsignedWord();
			int l12 = stream.readUnsignedByte();
			int j15 = stream.readUnsignedWord();
			if (i5 >= 0 && l7 >= 0 && i5 < 104 && l7 < 104) {
				i5 = i5 * 128 + 64;
				l7 = l7 * 128 + 64;
                                GraphicsObject graphicsObject = new GraphicsObject(game.plane, game.loopCycle, j15, k10, game.getTileHeight(game.plane, l7, i5) - l12, l7, i5);
                                game.graphicsObjectList.insertHead(graphicsObject);
			}
			return;
		}
		if (j == 44) {
			int itemID = stream.readShortLEAdd();
			int itemAmount = stream.readUnsignedWord();
			int i8 = stream.readUnsignedByte();
			int l10 = game.mapEventX + (i8 >> 4 & 7);
			int i13 = game.mapEventY + (i8 & 7);
			if (l10 >= 0 && i13 >= 0 && l10 < 104 && i13 < 104) {
				Item gItem = new Item();
				gItem.ID = itemID;
				gItem.amount = itemAmount;
				if (game.groundArray[game.plane][l10][i13] == null) {
					game.groundArray[game.plane][l10][i13] = new NodeList();
				}
				game.groundArray[game.plane][l10][i13].insertHead(gItem);
				game.spawnGroundItem(l10, i13);
			}
			return;
		}
		if (j == 117) {
			int i3 = stream.readUnsignedByte();
			int l5 = game.mapEventX + (i3 >> 4 & 7);
			int k8 = game.mapEventY + (i3 & 7);
			int j11 = l5 + stream.readSignedByte();
			int k13 = k8 + stream.readSignedByte();
			int l15 = stream.readSignedWord();
			int i17 = stream.readUnsignedWord();
			int i18 = stream.readUnsignedByte() * 4;
			int l18 = stream.readUnsignedByte() * 4;
			int k19 = stream.readUnsignedWord();
			int j20 = stream.readUnsignedWord();
			int i21 = stream.readUnsignedByte();
			int j21 = stream.readUnsignedByte();
			if (l5 >= 0 && k8 >= 0 && l5 < 104 && k8 < 104 && j11 >= 0 && k13 >= 0 && j11 < 104 && k13 < 104 && i17 != 0x00ffff) {
				l5 = l5 * 128 + 64;
				k8 = k8 * 128 + 64;
				j11 = j11 * 128 + 64;
				k13 = k13 * 128 + 64;
                                Projectile projectile = new Projectile(i21, l18, k19 + game.loopCycle, j20 + game.loopCycle, j21, game.plane, game.getTileHeight(game.plane, k8, l5) - i18, k8, l5, l15, i17);
                                projectile.track(k19 + game.loopCycle, k13, game.getTileHeight(game.plane, k13, j11) - l18, j11);
                                game.projectileList.insertHead(projectile);
			}
		}
	}
}
