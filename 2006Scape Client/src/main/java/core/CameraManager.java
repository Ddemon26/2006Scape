package core;

import net.Signlink;
import render.Model;

/**
 * Handles camera movement and orientation calculations.
 */
public class CameraManager {
    private final Game game;

    public CameraManager(Game game) {
        this.game = game;
    }

    public void calcCameraPos() {
        int i = game.cameraTargetX * 128 + 64;
        int j = game.cameraTargetY * 128 + 64;
        int k = game.getTileHeight(game.plane, j, i) - game.cameraTargetZ;
        if (game.xCameraPos < i) {
            game.xCameraPos += game.cameraMoveSpeed + (i - game.xCameraPos) * game.cameraMoveAcceleration / 1000;
            if (game.xCameraPos > i) {
                game.xCameraPos = i;
            }
        }
        if (game.xCameraPos > i) {
            game.xCameraPos -= game.cameraMoveSpeed + (game.xCameraPos - i) * game.cameraMoveAcceleration / 1000;
            if (game.xCameraPos < i) {
                game.xCameraPos = i;
            }
        }
        if (game.zCameraPos < k) {
            game.zCameraPos += game.cameraMoveSpeed + (k - game.zCameraPos) * game.cameraMoveAcceleration / 1000;
            if (game.zCameraPos > k) {
                game.zCameraPos = k;
            }
        }
        if (game.zCameraPos > k) {
            game.zCameraPos -= game.cameraMoveSpeed + (game.zCameraPos - k) * game.cameraMoveAcceleration / 1000;
            if (game.zCameraPos < k) {
                game.zCameraPos = k;
            }
        }
        if (game.yCameraPos < j) {
            game.yCameraPos += game.cameraMoveSpeed + (j - game.yCameraPos) * game.cameraMoveAcceleration / 1000;
            if (game.yCameraPos > j) {
                game.yCameraPos = j;
            }
        }
        if (game.yCameraPos > j) {
            game.yCameraPos -= game.cameraMoveSpeed + (game.yCameraPos - j) * game.cameraMoveAcceleration / 1000;
            if (game.yCameraPos < j) {
                game.yCameraPos = j;
            }
        }
        i = game.cameraFocusX * 128 + 64;
        j = game.cameraFocusY * 128 + 64;
        k = game.getTileHeight(game.plane, j, i) - game.cameraFocusHeight;
        int l = i - game.xCameraPos;
        int i1 = k - game.zCameraPos;
        int j1 = j - game.yCameraPos;
        int k1 = (int) Math.sqrt(l * l + j1 * j1);
        int l1 = (int) (Math.atan2(i1, k1) * 325.94900000000001D) & 0x7ff;
        int i2 = (int) (Math.atan2(l, j1) * -325.94900000000001D) & 0x7ff;
        if (l1 < 128) {
            l1 = 128;
        }
        if (l1 > 383) {
            l1 = 383;
        }
        if (game.yCameraCurve < l1) {
            game.yCameraCurve += game.cameraAdjustSpeed + (l1 - game.yCameraCurve) * game.cameraAdjustAcceleration / 1000;
            if (game.yCameraCurve > l1) {
                game.yCameraCurve = l1;
            }
        }
        if (game.yCameraCurve > l1) {
            game.yCameraCurve -= game.cameraAdjustSpeed + (game.yCameraCurve - l1) * game.cameraAdjustAcceleration / 1000;
            if (game.yCameraCurve < l1) {
                game.yCameraCurve = l1;
            }
        }
        int j2 = i2 - game.xCameraCurve;
        if (j2 > 1024) {
            j2 -= 2048;
        }
        if (j2 < -1024) {
            j2 += 2048;
        }
        if (j2 > 0) {
            game.xCameraCurve += game.cameraAdjustSpeed + j2 * game.cameraAdjustAcceleration / 1000;
            game.xCameraCurve &= 0x7ff;
        }
        if (j2 < 0) {
            game.xCameraCurve -= game.cameraAdjustSpeed + -j2 * game.cameraAdjustAcceleration / 1000;
            game.xCameraCurve &= 0x7ff;
        }
        int k2 = i2 - game.xCameraCurve;
        if (k2 > 1024) {
            k2 -= 2048;
        }
        if (k2 < -1024) {
            k2 += 2048;
        }
        if (k2 < 0 && j2 > 0 || k2 > 0 && j2 < 0) {
            game.xCameraCurve = i2;
        }
    }

    public void updateCameraPosition() {
        try {
            int j = game.myPlayer.x + game.cameraXOffset;
            int k = game.myPlayer.y + game.cameraYOffset;
            if (game.cameraX - j < -500 || game.cameraX - j > 500 || game.cameraY - k < -500 || game.cameraY - k > 500) {
                game.cameraX = j;
                game.cameraY = k;
            }
            if (game.cameraX != j) {
                game.cameraX += (j - game.cameraX) / 16;
            }
            if (game.cameraY != k) {
                game.cameraY += (k - game.cameraY) / 16;
            }
            if (game.keyArray[1] == 1) {
                game.cameraYawAccel += (-24 - game.cameraYawAccel) / 2;
            } else if (game.keyArray[2] == 1) {
                game.cameraYawAccel += (24 - game.cameraYawAccel) / 2;
            } else {
                game.cameraYawAccel /= 2;
            }
            if (game.keyArray[3] == 1) {
                game.cameraPitchAccel += (12 - game.cameraPitchAccel) / 2;
            } else if (game.keyArray[4] == 1) {
                game.cameraPitchAccel += (-12 - game.cameraPitchAccel) / 2;
            } else {
                game.cameraPitchAccel /= 2;
            }
            game.cameraYaw = game.cameraYaw + game.cameraYawAccel / 2 & 0x7ff;
            game.cameraPitch += game.cameraPitchAccel / 2;
            if (game.cameraPitch < 128) {
                game.cameraPitch = 128;
            }
            if (game.cameraPitch > 383) {
                game.cameraPitch = 383;
            }
            int l = game.cameraX >> 7;
            int i1 = game.cameraY >> 7;
            int j1 = game.getTileHeight(game.plane, game.cameraY, game.cameraX);
            int k1 = 0;
            if (l > 3 && i1 > 3 && l < 100 && i1 < 100) {
                for (int l1 = l - 4; l1 <= l + 4; l1++) {
                    for (int k2 = i1 - 4; k2 <= i1 + 4; k2++) {
                        int l2 = game.plane;
                        if (l2 < 3 && (game.tileFlags[1][l1][k2] & 2) == 2) {
                            l2++;
                        }
                        int i3 = j1 - game.tileHeights[l2][l1][k2];
                        if (i3 > k1) {
                            k1 = i3;
                        }
                    }

                }

            }
            game.cameraMoveCycle++;
            if (game.cameraMoveCycle > 1512) {
                game.cameraMoveCycle = 0;
                game.stream.createFrame(77);
                game.stream.writeWordBigEndian(0);
                int i2 = game.stream.currentOffset;
                game.stream.writeWordBigEndian((int) (Math.random() * 256D));
                game.stream.writeWordBigEndian(101);
                game.stream.writeWordBigEndian(233);
                game.stream.writeWord(45092);
                if ((int) (Math.random() * 2D) == 0) {
                    game.stream.writeWord(35784);
                }
                game.stream.writeWordBigEndian((int) (Math.random() * 256D));
                game.stream.writeWordBigEndian(64);
                game.stream.writeWordBigEndian(38);
                game.stream.writeWord((int) (Math.random() * 65536D));
                game.stream.writeWord((int) (Math.random() * 65536D));
                game.stream.writeBytes(game.stream.currentOffset - i2);
            }
            int j2 = k1 * 192;
            if (j2 > 0x17f00) {
                j2 = 0x17f00;
            }
            if (j2 < 32768) {
                j2 = 32768;
            }
            if (j2 > game.cameraZoom) {
                game.cameraZoom += (j2 - game.cameraZoom) / 24;
                return;
            }
            if (j2 < game.cameraZoom) {
                game.cameraZoom += (j2 - game.cameraZoom) / 80;
            }
        } catch (Exception _ex) {
            Signlink.reporterror("glfc_ex " + game.myPlayer.x + "," + game.myPlayer.y + "," + game.cameraX + "," + game.cameraY + "," + game.currentRegionX + "," + game.currentRegionY + "," + game.baseX + "," + game.baseY);
            throw new RuntimeException("eek");
        }
    }

    public int determineCameraPlane() {
        int j = 3;
        if (game.yCameraCurve < 310) {
            int k = Math.max(0, Math.min(103, game.xCameraPos >> 7));
            int l = Math.max(0, Math.min(103, game.yCameraPos >> 7));
            int i1 = game.myPlayer.x >> 7;
            int j1 = game.myPlayer.y >> 7;
            if ((game.tileFlags[game.plane][k][l] & 4) != 0) {
                j = game.plane;
            }
            int k1;
            if (i1 > k) {
                k1 = i1 - k;
            } else {
                k1 = k - i1;
            }
            int l1;
            if (j1 > l) {
                l1 = j1 - l;
            } else {
                l1 = l - j1;
            }
            if (k1 > l1) {
                int i2 = l1 * 0x10000 / k1;
                int k2 = 32768;
                while (k != i1) {
                    if (k < i1) {
                        k++;
                    } else if (k > i1) {
                        k--;
                    }
                    if ((game.tileFlags[game.plane][k][l] & 4) != 0) {
                        j = game.plane;
                    }
                    k2 += i2;
                    if (k2 >= 0x10000) {
                        k2 -= 0x10000;
                        if (l < j1) {
                            l++;
                        } else if (l > j1) {
                            l--;
                        }
                        if ((game.tileFlags[game.plane][k][l] & 4) != 0) {
                            j = game.plane;
                        }
                    }
                }
            } else {
                int j2 = k1 * 0x10000 / l1;
                int l2 = 32768;
                while (l != j1) {
                    if (l < j1) {
                        l++;
                    } else if (l > j1) {
                        l--;
                    }
                    if ((game.tileFlags[game.plane][k][l] & 4) != 0) {
                        j = game.plane;
                    }
                    l2 += j2;
                    if (l2 >= 0x10000) {
                        l2 -= 0x10000;
                        if (k < i1) {
                            k++;
                        } else if (k > i1) {
                            k--;
                        }
                        if ((game.tileFlags[game.plane][k][l] & 4) != 0) {
                            j = game.plane;
                        }
                    }
                }
            }
        }
        if ((game.tileFlags[game.plane][game.myPlayer.x >> 7][game.myPlayer.y >> 7] & 4) != 0) {
            j = game.plane;
        }
        return j;
    }

    public void setCameraPos(int j, int k, int l, int i1, int j1, int k1) {
        int l1 = 2048 - k & 0x7ff;
        int i2 = 2048 - j1 & 0x7ff;
        int j2 = 0;
        int k2 = 0;
        int l2 = j;
        if (l1 != 0) {
            int i3 = Model.sineTable[l1];
            int k3 = Model.cosineTable[l1];
            int i4 = k2 * k3 - l2 * i3 >> 16;
            l2 = k2 * i3 + l2 * k3 >> 16;
            k2 = i4;
        }
        if (i2 != 0) {
            int j3 = Model.sineTable[i2];
            int l3 = Model.cosineTable[i2];
            int j4 = l2 * j3 + j2 * l3 >> 16;
            l2 = l2 * l3 - j2 * j3 >> 16;
            j2 = j4;
        }
        game.xCameraPos = l - j2;
        game.zCameraPos = i1 - k2;
        game.yCameraPos = k1 - l2;
        game.yCameraCurve = k;
        game.xCameraCurve = j1;
    }
}
