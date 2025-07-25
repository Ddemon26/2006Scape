package core.managers;

import core.engine.Game;
import core.engine.ClientSettings;
import java.io.IOException;
import net.RSSocket;
import util.NodeList;
import game.Player;
import net.Signlink;
import ui.TextClass;
import util.ISAACRandomGen;

/**
 * Handles network login handshake extracted from {@link Game}.
 */
public final class LoginManager {
    private final Game game;

    public LoginManager(Game game) {
        this.game = game;
    }

    public void login(String username, String password, boolean flag) {
        Signlink.errorname = username;
        try {
            if (!flag) {
                game.loginMessage1 = "";
                game.loginMessage2 = "Connecting to server...";
                game.loginScreen.drawLoginScreen(true);
            }
            game.socketStream = new RSSocket(game, game.openSocket((ClientSettings.SERVER_WORLD == 1) ? 43594 : 43596 + ClientSettings.SERVER_WORLD + game.portOff));
            long l = TextClass.longForName(username);
            int i = (int) (l >> 16 & 31L);
            game.stream.currentOffset = 0;
            game.stream.writeWordBigEndian(14);
            game.stream.writeWordBigEndian(i);
            game.socketStream.queueBytes(2, game.stream.buffer);
            for (int j = 0; j < 8; j++) {
                game.socketStream.read();
            }

            int k = game.socketStream.read();
            int i1 = k;
            if (k == 0) {
                game.socketStream.flushInputStream(game.inStream.buffer, 8);
                game.inStream.currentOffset = 0;
                game.serverSessionKey = game.inStream.readQWord();
                int[] ai = new int[4];
                ai[0] = (int) (Math.random() * 99999999D);
                ai[1] = (int) (Math.random() * 99999999D);
                ai[2] = (int) (game.serverSessionKey >> 32);
                ai[3] = (int) game.serverSessionKey;
                game.stream.currentOffset = 0;
                game.stream.writeWordBigEndian(10);
                game.stream.writeDWord(ai[0]);
                game.stream.writeDWord(ai[1]);
                game.stream.writeDWord(ai[2]);
                game.stream.writeDWord(ai[3]);
                game.stream.writeDWord(ClientSettings.UID);
                game.stream.writeString(username);
                game.stream.writeString(password);
                game.stream.rsaEncrypt();
                game.updateBuffer.currentOffset = 0;
                if (flag) {
                    game.updateBuffer.writeWordBigEndian(18);
                } else {
                    game.updateBuffer.writeWordBigEndian(16);
                }
                game.updateBuffer.writeWordBigEndian(game.stream.currentOffset + 36 + 1 + 1 + 2);
                game.updateBuffer.writeWordBigEndian(255);
                game.updateBuffer.writeWord(1);
                game.updateBuffer.writeWordBigEndian(game.lowMem ? 1 : 0);
                for (int l1 = 0; l1 < 9; l1++) {
                    game.updateBuffer.writeDWord(game.expectedCRCs[l1]);
                }

                game.updateBuffer.writeBytes(game.stream.buffer, game.stream.currentOffset, 0);
                game.stream.encryption = new ISAACRandomGen(ai);
                for (int j2 = 0; j2 < 4; j2++) {
                    ai[j2] += 50;
                }

                game.encryption = new ISAACRandomGen(ai);
                game.socketStream.queueBytes(game.updateBuffer.currentOffset, game.updateBuffer.buffer);
                k = game.socketStream.read();
            }
            if (k == 1) {
                try {
                    Thread.sleep(2000L);
                } catch (Exception _ex) {
                }
                login(username, password, flag);
                return;
            }
            if (k == 2) {
                game.myPrivilege = game.socketStream.read();
                game.flagged = game.socketStream.read() == 1;
                game.lastMouseClickTime = 0L;
                game.mouseIdleTicks = 0;
                game.mouseDetection.coordsIndex = 0;
                game.awtFocus = true;
                game.hasFocus = true;
                game.loggedIn = true;
                game.stream.currentOffset = 0;
                game.inStream.currentOffset = 0;
                game.pktType = -1;
                game.lastPacketType = -1;
                game.prevPktType = -1;
                game.prevPktType2 = -1;
                game.pktSize = 0;
                game.connectionTimeoutCounter = 0;
                game.systemUpdateTimer = 0;
                game.reconnectDelay = 0;
                game.hintIconState = 0;
                game.menuActionRow = 0;
                game.menuOpen = false;
                game.idleTime = 0;
                for (int j1 = 0; j1 < 100; j1++) {
                    game.chatMessages[j1] = null;
                }

                game.itemSelected = 0;
                game.spellSelected = 0;
                game.loadingStage = 0;
                game.currentSound = 0;
                game.cameraXOffset = (int) (Math.random() * 100D) - 50;
                game.cameraYOffset = (int) (Math.random() * 110D) - 55;
                game.cameraYawOffset = (int) (Math.random() * 80D) - 40;
                game.minimapRotationOffset = (int) (Math.random() * 120D) - 60;
                game.minimapZoom = (int) (Math.random() * 30D) - 20;
                game.cameraYaw = (int) (Math.random() * 20D) - 10 & 0x7ff;
                game.minimapState = 0;
                game.lastPlane = -1;
                game.destX = 0;
                game.destY = 0;
                game.playerCount = 0;
                game.npcCount = 0;
                for (int i2 = 0; i2 < game.maxPlayers; i2++) {
                    game.playerArray[i2] = null;
                    game.playerBuffers[i2] = null;
                }

                for (int k2 = 0; k2 < 16384; k2++) {
                    game.npcArray[k2] = null;
                }

                game.myPlayer = game.playerArray[game.myPlayerIndex] = new Player();
                game.projectileList.removeAll();
                game.graphicsObjectList.removeAll();
                for (int l2 = 0; l2 < 4; l2++) {
                    for (int i3 = 0; i3 < 104; i3++) {
                        for (int k3 = 0; k3 < 104; k3++) {
                            game.groundArray[l2][i3][k3] = null;
                        }
                    }
                }

                game.pendingSpawns = new NodeList();
                game.interfaceMode = 0;
                game.friendsCount = 0;
                game.dialogID = -1;
                game.backDialogID = -1;
                game.openInterfaceID = -1;
                game.invOverlayInterfaceID = -1;
                game.fullScreenInterfaceId = -1;
                game.overlayInterfaceId = -1;
                game.actionPending = false;
                game.tabID = 3;
                game.inputDialogState = 0;
                game.menuOpen = false;
                game.messagePromptRaised = false;
                game.messagePrompt = null;
                game.multiCombatZone = 0;
                game.flashingTabId = -1;
                game.isMaleCharacter = true;
                game.resetCharacterOptions();
                for (int j3 = 0; j3 < 5; j3++) {
                    game.characterColorIndices[j3] = 0;
                }

                for (int l3 = 0; l3 < 5; l3++) {
                    game.atPlayerActions[l3] = null;
                    game.atPlayerArray[l3] = false;
                }

                game.itemUseCounter = 0;
                game.npcAttackCounter = 0;
                game.playerOptionCounter = 0;
                Game.walkPacketCounter = 0;
                game.objectClickCounter = 0;
                Game.actionCounter = 0;
                game.npcInteractionCounter = 0;
                Game.npcClickCounter = 0;
                game.resetImageProducers2();
                return;
            }
            if (k == 3) {
                game.loginMessage1 = "";
                game.loginMessage2 = "Invalid username or password.";
                return;
            }
            if (k == 4) {
                game.loginMessage1 = "Your account has been disabled.";
                game.loginMessage2 = "Or you entered an invalid character for user/pass";
                return;
            }
            if (k == 5) {
                game.loginMessage1 = "Your account is already logged in.";
                game.loginMessage2 = "Try again in 60 secs...";
                return;
            }
            if (k == 6) {
                game.loginMessage1 = "" + ClientSettings.SERVER_NAME + " has been updated!";
                game.loginMessage2 = "Please reload this page.";
                return;
            }
            if (k == 7) {
                game.loginMessage1 = "This world is full.";
                game.loginMessage2 = "Please use a different world.";
                return;
            }
            if (k == 8) {
                game.loginMessage1 = "Unable to connect.";
                game.loginMessage2 = "Login server offline.";
                return;
            }
            if (k == 9) {
                game.loginMessage1 = "Login limit exceeded.";
                game.loginMessage2 = "Too many connections from your address.";
                return;
            }
            if (k == 10) {
                game.loginMessage1 = "Unable to connect.";
                game.loginMessage2 = "Bad session id.";
                return;
            }
            if (k == 11) {
                game.loginMessage1 = "Login server rejected session.";
                game.loginMessage2 = "Please try again.";
                return;
            }
            if (k == 12) {
                game.loginMessage1 = "Only staff are allowed to play right now.";
                game.loginMessage2 = "Please login using a staff account.";
                return;
            }
            if (k == 13) {
                game.loginMessage1 = "Could not complete login.";
                game.loginMessage2 = "Please try using a different world.";
                return;
            }
            if (k == 14) {
                game.loginMessage1 = "The server is being updated.";
                game.loginMessage2 = "Please wait 1 minute and try again.";
                return;
            }
            if (k == 15) {
                game.loggedIn = true;
                game.stream.currentOffset = 0;
                game.inStream.currentOffset = 0;
                game.pktType = -1;
                game.lastPacketType = -1;
                game.prevPktType = -1;
                game.prevPktType2 = -1;
                game.pktSize = 0;
                game.connectionTimeoutCounter = 0;
                game.systemUpdateTimer = 0;
                game.menuActionRow = 0;
                game.menuOpen = false;
                game.loadingStartTime = System.currentTimeMillis();
                return;
            }
            if (k == 16) {
                game.loginMessage1 = "Login attempts exceeded.";
                game.loginMessage2 = "Please wait 1 minute and try again.";
                return;
            }
            if (k == 17) {
                game.loginMessage1 = "You are standing in a members-only area.";
                game.loginMessage2 = "To play on this world move to a free area first";
                return;
            }
            if (k == 20) {
                game.loginMessage1 = "Invalid loginserver requested";
                game.loginMessage2 = "Please try using a different world.";
                return;
            }
            if (k == 30) {
                game.loginMessage1 = "You need a forum account to play.";
                game.loginMessage2 = "Go to " + ClientSettings.SERVER_WEBSITE + " to register.";
                return;
            }
            if (k == 31) {
                game.loginMessage1 = "You're using an invalid or outdated client.";
                game.loginMessage2 = "Get the latest version at " + ClientSettings.SERVER_WEBSITE + "";
                return;
            }
            if (k == 32) {
                game.loginMessage1 = "You need to recover your account first,";
                game.loginMessage2 = "go to " + ClientSettings.SERVER_WEBSITE + "/help for a tutorial!";
                return;
            }
            if (k == 33) {
                game.loginMessage1 = "You need a members account to login to this world.";
                game.loginMessage2 = "Please subscribe, or use a different world.";
                return;
            }
            if (k == 34) {
                game.loginMessage1 = "You need to activate your forum account first.";
                game.loginMessage2 = "Click on confirm in the email we have send you!";
                return;
            }
            if (k == 21) {
                for (int k1 = game.socketStream.read(); k1 >= 0; k1--) {
                    game.loginMessage1 = "You have only just left another world";
                    game.loginMessage2 = "Your profile will be transferred in: " + k1 + " seconds";
                    game.loginScreen.drawLoginScreen(true);
                    try {
                        Thread.sleep(1000L);
                    } catch (Exception _ex) {
                    }
                }

                login(username, password, flag);
                return;
            }
            if (k == -1) {
                if (i1 == 0) {
                    if (game.loginFailures < 2) {
                        try {
                            Thread.sleep(2000L);
                        } catch (Exception _ex) {
                        }
                        game.loginFailures++;
                        login(username, password, flag);
                        return;
                    } else {
                        game.loginMessage1 = "Error connecting to server.";
                        game.loginMessage2 = "Please try again in a little while.";
                        return;
                    }
                } else {
                    game.loginMessage1 = "No response from server";
                    game.loginMessage2 = "Please try closing and opening your client again.";
                    return;
                }
            } else {
                System.out.println("response:" + k);
                game.loginMessage1 = "Unexpected server response";
                game.loginMessage2 = "Please try using a different world.";
                return;
            }
        } catch (IOException _ex) {
            game.loginMessage1 = "";
        }
        game.loginMessage2 = "Error connecting to server.";
    }
}
