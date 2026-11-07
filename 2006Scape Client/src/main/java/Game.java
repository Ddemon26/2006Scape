/**
 * NOTICE: IF YOU CHANGE ANYTHING IN GAME.JAVA, PLEASE COPY-PASTE THE WHOLE CLASS OVER TO LOCALGAME.JAVA
 * THIS IS TO ALLOW LOCAL PARABOT TO CONTINUE TO WORK
 */


import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.zip.CRC32;

/**
 * NOTICE: IF YOU CHANGE ANYTHING IN GAME.JAVA, PLEASE COPY-PASTE THE WHOLE CLASS OVER TO LOCALGAME.JAVA
 * THIS IS TO ALLOW LOCAL PARABOT TO CONTINUE TO WORK
 */
@SuppressWarnings("serial")
public class Game extends RSApplet {
	
	private boolean graphicsEnabled = true;
	
	public static int random(final float range) {
		return (int) (java.lang.Math.random() * (range + 1));
	}
    

	public static String intToKOrMilLongName(int amount) {
		String formattedAmount = String.valueOf(amount);
		for (int commaIndex = formattedAmount.length() - 3; commaIndex > 0; commaIndex -= 3) {
			formattedAmount = formattedAmount.substring(0, commaIndex) + "," + formattedAmount.substring(commaIndex);
		}
		if (formattedAmount.length() > 8) {
			formattedAmount = "@gre@" + formattedAmount.substring(0, formattedAmount.length() - 8) + " million @whi@(" + formattedAmount + ")";
		} else if (formattedAmount.length() > 4) {
			formattedAmount = "@cya@" + formattedAmount.substring(0, formattedAmount.length() - 4) + "K @whi@(" + formattedAmount + ")";
		}
		return " " + formattedAmount;
	}
	
	static final boolean musicIsntNull() {
		if (musicPlayer == null)
			return false;
		return true;
	}
	
	static final void shutdownMusicPlayer() {
		if (musicPlayer != null) {
			stopMusicPlayback(false);
			if (songTransitionTicks > 0) {
				musicPlayer.setVolume(256);
				songTransitionTicks = 0;
			}
		    musicPlayer.shutdown();
		    musicPlayer = null;
		}
	}
	
	public static String getFileNameWithoutExtension(String fileName) {
		File tmpFile = new File(fileName);
		tmpFile.getName();
		int whereDot = tmpFile.getName().lastIndexOf('.');
		if (0 < whereDot && whereDot <= tmpFile.getName().length() - 2) {
			return tmpFile.getName().substring(0, whereDot);
		}
		return "";
	}
	
	public String indexLocation(int cacheIndex, int index) {
		return Signlink.findcachedir() + "index" + cacheIndex + "/" + (index != -1 ? index + ".gz" : "");
	}

	public void repackCacheIndex(int cacheIndex) {
		System.out.println("Started repacking index " + cacheIndex + ".");
		int indexLength = new File(indexLocation(cacheIndex, -1)).listFiles().length;
		File[] file = new File(indexLocation(cacheIndex, -1)).listFiles();
		try {
			for (int index = 0; index < indexLength; index++) {
				int fileIndex = Integer.parseInt(getFileNameWithoutExtension(file[index].toString()));
				byte[] data = fileToByteArray(cacheIndex, fileIndex);
				if(data != null && data.length > 0) {
					cacheStores[cacheIndex].writeFile(data.length, data, fileIndex);
					System.out.println("Repacked " + fileIndex + ".");
				} else {
					System.out.println("Unable to locate index " + fileIndex + ".");
				}
			}
		} catch(Exception e) {
			System.out.println("Error packing cache index " + cacheIndex + ".");
		}
		System.out.println("Finished repacking " + cacheIndex + ".");
	}

	public byte[] fileToByteArray(int cacheIndex, int index) {
		try {
			if (indexLocation(cacheIndex, index).length() <= 0 || indexLocation(cacheIndex, index) == null) {
				return null;
			}
			File file = new File(indexLocation(cacheIndex, index));
			byte[] fileData = new byte[(int)file.length()];
			FileInputStream fis = new FileInputStream(file);
			fis.read(fileData);
			fis.close();
			return fileData;
		} catch(Exception e) {
			return null;
		}
	}
	
	public void musics() {
		for(int MusicIndex = 0; MusicIndex < 3536; MusicIndex++) {
			byte[] abyte0 = GetMusic(MusicIndex);
				if (abyte0 != null && abyte0.length > 0) {
					cacheStores[3].writeFile(abyte0.length, abyte0, MusicIndex);
				}
			}
		}
	
	public byte[] GetMusic(int Index) {
		try {
		File Music = new File(Signlink.findcachedir() + "./sounds/"+Index+".gz");
		byte[] aByte = new byte[(int)Music.length()];
		FileInputStream Fis = new FileInputStream(Music);
		Fis.read(aByte);
		Fis.close();
		return aByte;
		} catch(Exception e) {
		return null;
		}
	}
	
	static final void setVolume(int i) {
		if (musicIsntNull()) {
			if (fetchMusic)
				musicVolume2 = i;
			else
				applySongVolume(i);
		}
	}
	
	static final void applySongVolume(int i) {
		if (musicPlayer != null) {
			if (songTransitionTicks == 0) {
				if (currentSongVolume >= 0) {
					currentSongVolume = i;
					musicPlayer.fade(i, 0);
				}
			} else if (queuedSongData != null)
				queuedSongVolume = i;
		}
	}
	
	static final synchronized void stopMusic(boolean bool) {
		if (musicIsntNull()) {
			stopMusicPlayback(bool);
			fetchMusic = false;
		}
	}
	
	static final void stopMusicPlayback(boolean bool) {
		setMusicData(0, null, bool);
	}
	
	static final boolean constructMusic() {
		songTransitionTicks = 20;
		try {
			if (ClientSettings.ANDROID) {
				musicPlayer = (MusicPlayer) AndroidHooks.getMusicPlayer();
				return musicPlayer != null;
			}
		    musicPlayer = (MusicPlayer) Class.forName("JavaMidiMusicPlayer").newInstance();
		} catch (Throwable throwable) {
		    return false;
		}
		return true;
	}
	
	final synchronized void requestSongWithFade(int fadeStepPerTick, int volume,
		    boolean bool, int songId) {
		if (musicIsntNull()) {
			nextSong = songId;
			onDemandFetcher.requestFile(2, nextSong);
			musicVolume2 = volume;
			pendingSongFadeTicks = -1;
			aBoolean995 = true;
			pendingSongFadeStep = fadeStepPerTick;
		}
	}
	
	final synchronized void requestSongWithoutFade(int volume, boolean bool, int songId) {
		if (musicIsntNull()) {
			nextSong = songId;
			onDemandFetcher.requestFile(2, nextSong);
			musicVolume2 = volume;
			pendingSongFadeTicks = -1;
			aBoolean995 = true;
		    pendingSongFadeStep = -1;
		}
	}
	
	public void sendFrame126(String str,int i) {
		RSInterface.interfaceCache[i].disabledText = str;
		if(RSInterface.interfaceCache[i].parentID == tabInterfaceIDs[tabID])
			needDrawTabArea = true;
	}
	
	public static byte[] musicData;
	
	static final synchronized void updateMusic() {
		if (musicIsntNull()) {
			if (fetchMusic) {
				byte[] fetchedSongData = musicData;
				if (fetchedSongData != null) {
					if (ClientSettings.ANDROID)
						setMusicData(musicVolume2, fetchedSongData, aBoolean995);
					else if (pendingSongFadeStep >= 0)
						fadeOutAndQueueSong(aBoolean995, pendingSongFadeStep, musicVolume2, fetchedSongData);
					else if (pendingSongFadeTicks >= 0)
						queueSongAfterFadeTicks(pendingSongFadeTicks, -1, aBoolean995, fetchedSongData, musicVolume2);
					else
						setMusicData(musicVolume2, fetchedSongData, aBoolean995);
					fetchMusic = false;
				}
			}
		    tickSongTransition(0);
		}
	}
	
	static final int volumeToFadeOffset(int volume) {
		return (int) (Math.log((double) volume * 0.00390625) * 868.5889638065036 + 0.5);
	}
	
	static final void setMusicData(int volume, byte[] midiData, boolean loopMusic) {
		if (musicPlayer != null) {
			if (currentSongVolume >= 0) {
				musicPlayer.stop();
				currentSongVolume = -1;
				queuedSongData = null;
				songTransitionTicks = 20;
				fadeProgress = 0;
			}
			if (midiData != null) {
				if (songTransitionTicks > 0) {
					musicPlayer.setVolume(volume);
					songTransitionTicks = 0;
				}
					currentSongVolume = volume;
					musicPlayer.play(volume, midiData, 0, loopMusic);
			}
		}
	}
	
	static final void queueSongAfterFadeTicks(int fadeTicks, int i_29_, boolean loopMusic, byte[] midiData, int volume) {
		if (musicPlayer != null) {
			if (i_29_ >= (currentSongVolume ^ 0xffffffff)) {
				fadeTicks -= 20;
				if (fadeTicks < 1)
					fadeTicks = 1;
				songTransitionTicks = fadeTicks;
				if (currentSongVolume == 0)
					fadeStepPerTick = 0;
				else {
					int remainingFadeOffset = volumeToFadeOffset(currentSongVolume);
					remainingFadeOffset -= fadeProgress;
					fadeStepPerTick = ((fadeStepPerTick - 1 + (remainingFadeOffset + 3600)) / fadeStepPerTick);
				}
				queuedSongLoop = loopMusic;
				queuedSongData = midiData;
				queuedSongVolume = volume;
			} else if (songTransitionTicks != 0) {
				queuedSongLoop = loopMusic;
				queuedSongData = midiData;
				queuedSongVolume = volume;
			} else
				setMusicData(volume, midiData, loopMusic);
		}
	}
	
	static final void fadeOutAndQueueSong(boolean loopMusic, int stepPerTick, int volume, byte[] midiData) {
		if (musicPlayer != null) {
			if (currentSongVolume >= 0) {
				fadeStepPerTick = stepPerTick;
				if (currentSongVolume != 0) {
					int remainingFadeOffset = volumeToFadeOffset(currentSongVolume);
					remainingFadeOffset -= fadeProgress;
					songTransitionTicks = (remainingFadeOffset + 3600) / stepPerTick;
					if (songTransitionTicks < 1)
						songTransitionTicks = 1;
				} else
					songTransitionTicks = 1;
				queuedSongData = midiData;
				queuedSongVolume = volume;
				queuedSongLoop = loopMusic;
			} else if (songTransitionTicks == 0)
				setMusicData(volume, midiData, loopMusic);
			else {
				queuedSongVolume = volume;
				queuedSongLoop = loopMusic;
				queuedSongData = midiData;
			}
		}
	}
	
	static final void tickSongTransition(int i) {
		if (musicPlayer != null) {
			if (currentSongVolume < i) {
				if (songTransitionTicks > 0) {
					songTransitionTicks--;
					if (songTransitionTicks == 0) {
						if (queuedSongData == null)
							musicPlayer.setVolume(256);
						else {
							musicPlayer.setVolume(queuedSongVolume);
							currentSongVolume = queuedSongVolume;
							musicPlayer.play(queuedSongVolume, queuedSongData, 0, queuedSongLoop);
							queuedSongData = null;
						}
						fadeProgress = 0;
					}
				}
			} else if (songTransitionTicks > 0) {
				fadeProgress += fadeStepPerTick;
				musicPlayer.fade(currentSongVolume, fadeProgress);
				songTransitionTicks--;
				if (songTransitionTicks == 0) {
					musicPlayer.stop();
					songTransitionTicks = 20;
					currentSongVolume = -1;
				}
			}
			musicPlayer.tick(i - 122);
		}
	}

	private void stopMidi() {
		if (ClientSettings.ANDROID) {
			AndroidHooks.stopMidi();
			Signlink.midi = "stop";
		} else {
			DesktopHooks.stopSignlinkMidi();
			Signlink.midi = "stop";
		}
	}

	public boolean menuHasAddFriend(int j) {
		if (j < 0) {
			return false;
		}
		int k = menuActionID[j];
		if (k >= 2000) {
			k -= 2000;
		}
		return k == 337;
	}

	public void drawChatArea() {
		aRSImageProducer_1166.setAsDrawingArea();
		Texture.lineOffsets = chatAreaOffsets;
		chatBack.drawNonzeroIndexed(0, 0);
		if (messagePromptRaised) {
			chatTextDrawingArea.drawCenteredLiteralText(0, aString1121, 40, 239);
			chatTextDrawingArea.drawCenteredLiteralText(128, promptInput + "*", 60, 239);
		} else if (inputDialogState == 1) {
			chatTextDrawingArea.drawCenteredLiteralText(0, "Enter amount:", 40, 239);
			chatTextDrawingArea.drawCenteredLiteralText(128, amountOrNameInput + "*", 60, 239);
		} else if (inputDialogState == 2) {
			chatTextDrawingArea.drawCenteredLiteralText(0, "Enter name:", 40, 239);
			chatTextDrawingArea.drawCenteredLiteralText(128, amountOrNameInput + "*", 60, 239);
		} else if (aString844 != null) {
			chatTextDrawingArea.drawCenteredLiteralText(0, aString844, 40, 239);
			chatTextDrawingArea.drawCenteredLiteralText(128, "Click to continue", 60, 239);
		} else if (backDialogID != -1) {
			drawInterface(0, 0, RSInterface.interfaceCache[backDialogID], 0);//CHANGED THIS - andrew was 0, 0
		} else if (dialogID != -1) {
			drawInterface(0, 0, RSInterface.interfaceCache[dialogID], 0);//CHANGED THIS - andrew was 0, 0
		} else {
			TextDrawingArea textDrawingArea = aTextDrawingArea_1271;
			int j = 0;
			DrawingArea.setClip(77, 0, 463, 0);
			for (int k = 0; k < 100; k++) {
				if (chatMessages[k] != null) {
					int l = chatTypes[k];
					int i1 = 70 - j * 14 + anInt1089;
					String s1 = chatNames[k];
					byte byte0 = 0;
					if (s1 != null && s1.startsWith("@cr1@")) {
						s1 = s1.substring(5);
						byte0 = 1;
					}
					if (s1 != null && s1.startsWith("@cr2@")) {
						s1 = s1.substring(5);
						byte0 = 2;
					}
					if (l == 0) {
						if (i1 > 0 && i1 < 110) {
							try {
							textDrawingArea.drawTaggedText(false, 4, 0, chatMessages[k], i1);
							} catch (Exception e) {
								
							}
						}
						j++;
					}
					if ((l == 1 || l == 2) && (l == 1 || publicChatMode == 0 || publicChatMode == 1 && isFriendOrSelf(s1))) {
						if (i1 > 0 && i1 < 110) {
							int j1 = 4;
							if (byte0 == 1) {
								modIcons[0].drawNonzeroIndexed(j1, i1 - 12);
								j1 += 14;
							}
							if (byte0 == 2) {
								modIcons[1].drawNonzeroIndexed(j1, i1 - 12);
								j1 += 14;
							}
							textDrawingArea.drawLiteralText(0, s1 + ":", i1, j1);
							j1 += textDrawingArea.measureTaggedTextWidth(s1) + 8;
							textDrawingArea.drawLiteralText(255, chatMessages[k], i1, j1);
						}
						j++;
					}
					if ((l == 3 || l == 7) && splitpublicChat == 0 && (l == 7 || privateChatMode == 0 || privateChatMode == 1 && isFriendOrSelf(s1))) {
						if (i1 > 0 && i1 < 110) {
							int k1 = 4;
							textDrawingArea.drawLiteralText(0, "From", i1, k1);
							k1 += textDrawingArea.measureTaggedTextWidth("From ");
							if (byte0 == 1) {
								modIcons[0].drawNonzeroIndexed(k1, i1 - 12);
								k1 += 14;
							}
							if (byte0 == 2) {
								modIcons[1].drawNonzeroIndexed(k1, i1 - 12);
								k1 += 14;
							}
							textDrawingArea.drawLiteralText(0, s1 + ":", i1, k1);
							k1 += textDrawingArea.measureTaggedTextWidth(s1) + 8;
							textDrawingArea.drawLiteralText(0x800000, chatMessages[k], i1, k1);
						}
						j++;
					}
					if (l == 4 && (tradeMode == 0 || tradeMode == 1 && isFriendOrSelf(s1))) {
						if (i1 > 0 && i1 < 110) {
							textDrawingArea.drawLiteralText(0x800080, s1 + " " + chatMessages[k], i1, 4);
						}
						j++;
					}
					if (l == 5 && splitpublicChat == 0 && privateChatMode < 2) {
						if (i1 > 0 && i1 < 110) {
							textDrawingArea.drawLiteralText(0x800000, chatMessages[k], i1, 4);
						}
						j++;
					}
					if (l == 6 && splitpublicChat == 0 && privateChatMode < 2) {
						if (i1 > 0 && i1 < 110) {
							textDrawingArea.drawLiteralText(0, "To " + s1 + ":", i1, 4);
							textDrawingArea.drawLiteralText(0x800000, chatMessages[k], i1, 12 + textDrawingArea.measureTaggedTextWidth("To " + s1));
						}
						j++;
					}
					if (l == 8 && (tradeMode == 0 || tradeMode == 1 && isFriendOrSelf(s1))) {
						if (i1 > 0 && i1 < 110) {
							textDrawingArea.drawLiteralText(0x7e3200, s1 + " " + chatMessages[k], i1, 4);
						}
						j++;
					}
				}
			}

			DrawingArea.resetClip();
			anInt1211 = j * 14 + 7;
			if (anInt1211 < 78) {
				anInt1211 = 78;
			}
			drawScrollbar(77, anInt1211 - anInt1089 - 77, 0, 463, anInt1211);
			String s;
			if (myPlayer != null && myPlayer.name != null) {
				s = myPlayer.name;
			} else {
				s = TextClass.formatDisplayName(myUsername);
			}
			textDrawingArea.drawLiteralText(0, s + ":", 90, 4);
			textDrawingArea.drawLiteralText(255, inputString + "*", 90, 6 + textDrawingArea.measureTaggedTextWidth(s + ": "));
			DrawingArea.drawHorizontalLine(77, 0, 479, 0);
		}
		if (menuOpen && menuScreenArea == 2) {
			drawMenu();
		}
		aRSImageProducer_1166.drawGraphics(357, super.graphics, 17);
		aRSImageProducer_1165.setAsDrawingArea();
		Texture.lineOffsets = chatBoxAreaOffsets;
	}

	public void init() {
		try {
			portOff = 0;
			setHighMem();
			isMembers = true;
			Signlink.storeid = 32;
			Signlink.startpriv(InetAddress.getLocalHost());
			initClientFrame(503, 765);
		} catch (Exception exception) {
			return;
		}
	}

	public void startRunnable(Runnable runnable, int i) {
		if (i > 10) {
			i = 10;
		}
		if (Signlink.mainapp != null) {
			Signlink.startthread(runnable, i);
		} else {
			super.startRunnable(runnable, i);
		}
	}

	public Socket openSocket(int i) throws IOException {
		if (Signlink.mainapp != null) {
			return Signlink.opensocket(i);
		} else {
			return new Socket(InetAddress.getByName(getCodeBase().getHost()), i);
		}
	}

	public boolean processMenuClick() {
		if (activeInterfaceType != 0) {
			return false;
		}
		int j = super.clickMode3;
		if (spellSelected == 1 && super.saveClickX >= 516 && super.saveClickY >= 160 && super.saveClickX <= 765 && super.saveClickY <= 205) {
			j = 0;
		}
		if (menuOpen) {
			if (j != 1) {
				int k = super.mouseX;
				int j1 = super.mouseY;
				if (menuScreenArea == 0) {
					k -= 4;
					j1 -= 4;
				}
				if (menuScreenArea == 1) {
					k -= 553;
					j1 -= 205;
				}
				if (menuScreenArea == 2) {
					k -= 17;
					j1 -= 357;
				}
				if (k < menuOffsetX - 10 || k > menuOffsetX + menuWidth + 10 || j1 < menuOffsetY - 10 || j1 > menuOffsetY + anInt952 + 10) {
					menuOpen = false;
					if (menuScreenArea == 1) {
						needDrawTabArea = true;
					}
					if (menuScreenArea == 2) {
						inputTaken = true;
					}
				}
			}
			if (j == 1) {
				int l = menuOffsetX;
				int k1 = menuOffsetY;
				int i2 = menuWidth;
				int k2 = super.saveClickX;
				int l2 = super.saveClickY;
				if (menuScreenArea == 0) {
					k2 -= 4;
					l2 -= 4;
				}
				if (menuScreenArea == 1) {
					k2 -= 553;
					l2 -= 205;
				}
				if (menuScreenArea == 2) {
					k2 -= 17;
					l2 -= 357;
				}
				int i3 = -1;
				for (int j3 = 0; j3 < menuActionRow; j3++) {
					int k3 = k1 + 31 + (menuActionRow - 1 - j3) * 15;
					if (k2 > l && k2 < l + i2 && l2 > k3 - 13 && l2 < k3 + 3) {
						i3 = j3;
					}
				}

				if (i3 != -1) {
					doAction(i3);
				}
				menuOpen = false;
				if (menuScreenArea == 1) {
					needDrawTabArea = true;
				}
				if (menuScreenArea == 2) {
					inputTaken = true;
				}
			}
		} else {
			if (j == 1 && menuActionRow > 0) {
				int i1 = menuActionID[menuActionRow - 1];
				if (i1 == 632 || i1 == 78 || i1 == 867 || i1 == 431 || i1 == 53 || i1 == 74 || i1 == 454 || i1 == 539 || i1 == 493 || i1 == 847 || i1 == 447 || i1 == 1125) {
					int l1 = menuActionCmd2[menuActionRow - 1];
					int j2 = menuActionCmd3[menuActionRow - 1];
					RSInterface class9 = RSInterface.interfaceCache[j2];
					if (class9.aBoolean259 || class9.aBoolean235) {
						aBoolean1242 = false;
						anInt989 = 0;
						anInt1084 = j2;
						anInt1085 = l1;
						activeInterfaceType = 2;
						anInt1087 = super.saveClickX;
						anInt1088 = super.saveClickY;
						if (RSInterface.interfaceCache[j2].parentID == openInterfaceID) {
							activeInterfaceType = 1;
						}
						if (RSInterface.interfaceCache[j2].parentID == backDialogID) {
							activeInterfaceType = 3;
						}
						return true;
					}
				}
			}
			if (j == 1 && (anInt1253 == 1 || menuHasAddFriend(menuActionRow - 1)) && menuActionRow > 2) {
				j = 2;
			}
			if (j == 1 && menuActionRow > 0) {
				doAction(menuActionRow - 1);
			}
			if (j == 2 && menuActionRow > 0) {
				determineMenuSize();
			}
		}
		return false;
	}

	public void saveMidi(boolean flag, byte abyte0[]) {
		Signlink.midifade = flag ? 1 : 0;
		Signlink.saveMidi(abyte0, abyte0.length);
	}

	public void rebuildScene() {
		try {
			minimapBuiltPlane = -1;
			transientGraphics.removeAll();
			projectiles.removeAll();
			Texture.releaseAllTextureBuffers();
			unlinkMRUNodes();
			worldController.initToNull();
			System.gc();
			for (int collisionMapIndex = 0; collisionMapIndex < 4; collisionMapIndex++) {
				aClass11Array1230[collisionMapIndex].reset();
			}

			for (int tilePlane = 0; tilePlane < 4; tilePlane++) {
				for (int tileX = 0; tileX < 104; tileX++) {
					for (int tileY = 0; tileY < 104; tileY++) {
						byteGroundArray[tilePlane][tileX][tileY] = 0;
					}

				}

			}

			ObjectManager objectManager = new ObjectManager(byteGroundArray, intGroundArray);
			int regionCount = regionTerrainData.length;
			stream.writeEncryptedOpcode(0);
			if (!aBoolean1159) {
				for (int regionIndex = 0; regionIndex < regionCount; regionIndex++) {
					int regionTileX = (regionKeys[regionIndex] >> 8) * 64 - baseX;
					int regionTileY = (regionKeys[regionIndex] & 0xff) * 64 - baseY;
					byte terrainData[] = regionTerrainData[regionIndex];
					if (terrainData != null) {
						objectManager.loadTerrainRegion(terrainData, regionTileY, regionTileX, (anInt1069 - 6) * 8, (anInt1070 - 6) * 8, aClass11Array1230);
					}
				}

				for (int regionIndex = 0; regionIndex < regionCount; regionIndex++) {
					int regionTileX = (regionKeys[regionIndex] >> 8) * 64 - baseX;
					int regionTileY = (regionKeys[regionIndex] & 0xff) * 64 - baseY;
					byte terrainData[] = regionTerrainData[regionIndex];
					if (terrainData == null && anInt1070 < 800) {
						objectManager.blankRegionArea(regionTileY, 64, 64, regionTileX);
					}
				}

				anInt1097++;
				if (anInt1097 > 160) {
					anInt1097 = 0;
					stream.writeEncryptedOpcode(238);
					stream.writeByte(96);
				}
				stream.writeEncryptedOpcode(0);
				for (int regionIndex = 0; regionIndex < regionCount; regionIndex++) {
					byte objectData[] = regionObjectData[regionIndex];
					if (objectData != null) {
						int regionTileX = (regionKeys[regionIndex] >> 8) * 64 - baseX;
						int regionTileY = (regionKeys[regionIndex] & 0xff) * 64 - baseY;
						objectManager.loadObjects(regionTileX, aClass11Array1230, regionTileY, worldController, objectData);
					}
				}

			}
			if (aBoolean1159) {
				for (int destPlane = 0; destPlane < 4; destPlane++) {
					for (int destChunkX = 0; destChunkX < 13; destChunkX++) {
						for (int destChunkY = 0; destChunkY < 13; destChunkY++) {
							int chunkTemplate = anIntArrayArrayArray1129[destPlane][destChunkX][destChunkY];
							if (chunkTemplate != -1) {
								int sourcePlane = chunkTemplate >> 24 & 3;
								int rotation = chunkTemplate >> 1 & 3;
								int sourceChunkX = chunkTemplate >> 14 & 0x3ff;
								int sourceChunkY = chunkTemplate >> 3 & 0x7ff;
								int sourceRegionKey = (sourceChunkX / 8 << 8) + sourceChunkY / 8;
								for (int regionIndex = 0; regionIndex < regionKeys.length; regionIndex++) {
									if (regionKeys[regionIndex] != sourceRegionKey || regionTerrainData[regionIndex] == null) {
										continue;
									}
									objectManager.loadTerrainChunk(sourcePlane, rotation, aClass11Array1230, destChunkX * 8, (sourceChunkX & 7) * 8, regionTerrainData[regionIndex], (sourceChunkY & 7) * 8, destPlane, destChunkY * 8);
									break;
								}

							}
						}

					}

				}

				for (int chunkX = 0; chunkX < 13; chunkX++) {
					for (int chunkY = 0; chunkY < 13; chunkY++) {
						int chunkTemplate = anIntArrayArrayArray1129[0][chunkX][chunkY];
						if (chunkTemplate == -1) {
							objectManager.blankRegionArea(chunkY * 8, 8, 8, chunkX * 8);
						}
					}

				}

				stream.writeEncryptedOpcode(0);
				for (int destPlane = 0; destPlane < 4; destPlane++) {
					for (int chunkX = 0; chunkX < 13; chunkX++) {
						for (int chunkY = 0; chunkY < 13; chunkY++) {
							int chunkTemplate = anIntArrayArrayArray1129[destPlane][chunkX][chunkY];
							if (chunkTemplate != -1) {
								int sourcePlane = chunkTemplate >> 24 & 3;
								int rotation = chunkTemplate >> 1 & 3;
								int sourceChunkX = chunkTemplate >> 14 & 0x3ff;
								int sourceChunkY = chunkTemplate >> 3 & 0x7ff;
								int sourceRegionKey = (sourceChunkX / 8 << 8) + sourceChunkY / 8;
								for (int regionIndex = 0; regionIndex < regionKeys.length; regionIndex++) {
									if (regionKeys[regionIndex] != sourceRegionKey || regionObjectData[regionIndex] == null) {
										continue;
									}
									objectManager.loadObjectChunk(aClass11Array1230, worldController, sourcePlane, chunkX * 8, (sourceChunkY & 7) * 8, destPlane, regionObjectData[regionIndex], (sourceChunkX & 7) * 8, rotation, chunkY * 8);
									break;
								}

							}
						}

					}

				}

			}
			stream.writeEncryptedOpcode(0);
			objectManager.buildTerrainScene(aClass11Array1230, worldController);
			if(aRSImageProducer_1165 != null) {
				aRSImageProducer_1165.setAsDrawingArea();
				Texture.lineOffsets = chatBoxAreaOffsets;
			}
			stream.writeEncryptedOpcode(0);
			int k3 = ObjectManager.lowestBuiltPlane;
			if (k3 > plane) {
				k3 = plane;
			}
			if (k3 < plane - 1) {
				k3 = plane - 1;
			}
			if (lowMem) {
				worldController.createPlaneTiles(ObjectManager.lowestBuiltPlane);
			} else {
				worldController.createPlaneTiles(0);
			}
			for (int tileX = 0; tileX < 104; tileX++) {
				for (int tileY = 0; tileY < 104; tileY++) {
					spawnGroundItem(tileX, tileY);
				}

			}

			anInt1051++;
			if (anInt1051 > 98) {
				anInt1051 = 0;
				stream.writeEncryptedOpcode(150);
			}
			resetPendingSceneChangesAfterRegionLoad();
		} catch (Exception exception) {
		}
		ObjectDef.mruNodes1.clear();
		if (super.gameFrame != null) {
			stream.writeEncryptedOpcode(210);
			stream.writeIntBigEndian(0x3f008edd);
		}
		if (lowMem && Signlink.cache_dat != null) {
			int modelCount = onDemandFetcher.getVersionCount(0);
			for (int modelId = 0; modelId < modelCount; modelId++) {
				int modelIndexByte = onDemandFetcher.getModelIndex(modelId);
				if ((modelIndexByte & 0x79) == 0) {
					Model.unloadModelHeader(modelId);
				}
			}

		}
		System.gc();
		Texture.allocateBufferPool();
		onDemandFetcher.clearExtraFileQueue();
		int minRegionX = (anInt1069 - 6) / 8 - 1;
		int maxRegionX = (anInt1069 + 6) / 8 + 1;
		int minRegionY = (anInt1070 - 6) / 8 - 1;
		int maxRegionY = (anInt1070 + 6) / 8 + 1;
		if (aBoolean1141) {
			minRegionX = 49;
			maxRegionX = 50;
			minRegionY = 49;
			maxRegionY = 50;
		}
		for (int regionX = minRegionX; regionX <= maxRegionX; regionX++) {
			for (int regionY = minRegionY; regionY <= maxRegionY; regionY++) {
				if (regionX == minRegionX || regionX == maxRegionX || regionY == minRegionY || regionY == maxRegionY) {
					int terrainFileId = onDemandFetcher.getMapFileId(0, regionY, regionX);
					if (terrainFileId != -1) {
						onDemandFetcher.requestExtraFile(terrainFileId, 3);
					}
					int objectFileId = onDemandFetcher.getMapFileId(1, regionY, regionX);
					if (objectFileId != -1) {
						onDemandFetcher.requestExtraFile(objectFileId, 3);
					}
				}
			}

		}

	}

	public void unlinkMRUNodes() {
		ObjectDef.mruNodes1.clear();
		ObjectDef.mruNodes2.clear();
		EntityDef.mruNodes.clear();
		ItemDef.mruNodes2.clear();
		ItemDef.mruNodes1.clear();
		Player.mruNodes.clear();
		SpotAnim.modelCache.clear();
	}

	public void buildMinimapImage(int tilePlane) {
		int pixels[] = minimapSprite.pixels;
		int pixelCount = pixels.length;
		for (int pixelIndex = 0; pixelIndex < pixelCount; pixelIndex++) {
			pixels[pixelIndex] = 0;
		}

		for (int tileY = 1; tileY < 103; tileY++) {
			int tilePixelIndex = 24628 + (103 - tileY) * 512 * 4;
			for (int tileX = 1; tileX < 103; tileX++) {
				if ((byteGroundArray[tilePlane][tileX][tileY] & 0x18) == 0) {
					worldController.drawMinimapTile(pixels, tilePixelIndex, tilePlane, tileX, tileY);
				}
				if (tilePlane < 3 && (byteGroundArray[tilePlane + 1][tileX][tileY] & 8) != 0) {
					worldController.drawMinimapTile(pixels, tilePixelIndex, tilePlane + 1, tileX, tileY);
				}
				tilePixelIndex += 4;
			}

		}

		int wallColour = (238 + (int) (Math.random() * 20D) - 10 << 16) + (238 + (int) (Math.random() * 20D) - 10 << 8) + 238 + (int) (Math.random() * 20D) - 10;
		int interactiveWallColour = 238 + (int) (Math.random() * 20D) - 10 << 16;
		minimapSprite.setAsDrawingArea();
		for (int tileY = 1; tileY < 103; tileY++) {
			for (int tileX = 1; tileX < 103; tileX++) {
				if ((byteGroundArray[tilePlane][tileX][tileY] & 0x18) == 0) {
					drawMinimapTileObjects(tileY, wallColour, tileX, interactiveWallColour, tilePlane);
				}
				if (tilePlane < 3 && (byteGroundArray[tilePlane + 1][tileX][tileY] & 8) != 0) {
					drawMinimapTileObjects(tileY, wallColour, tileX, interactiveWallColour, tilePlane + 1);
				}
			}

		}
		if(aRSImageProducer_1165 != null) {
			aRSImageProducer_1165.setAsDrawingArea();
			Texture.lineOffsets = chatBoxAreaOffsets;
		}
		mapFunctionIconCount = 0;
		for (int tileX = 0; tileX < 104; tileX++) {
			for (int tileY = 0; tileY < 104; tileY++) {
				int decorationUid = worldController.getGroundDecorationUid(plane, tileX, tileY);
				if (decorationUid != 0) {
					int objectId = decorationUid >> 14 & 0x7fff;
					int mapFunctionId = ObjectDef.forID(objectId).mapFunctionId;
					if (mapFunctionId >= 0) {
						int iconTileX = tileX;
						int iconTileY = tileY;
						if (mapFunctionId != 22 && mapFunctionId != 29 && mapFunctionId != 34 && mapFunctionId != 36 && mapFunctionId != 46 && mapFunctionId != 47 && mapFunctionId != 48) {
							byte sceneWidth = 104;
							byte sceneHeight = 104;
							int collisionFlags[][] = aClass11Array1230[plane].tileFlags;
							for (int attempt = 0; attempt < 10; attempt++) {
								int direction = (int) (Math.random() * 4D);
								if (direction == 0 && iconTileX > 0 && iconTileX > tileX - 3 && (collisionFlags[iconTileX - 1][iconTileY] & 0x1280108) == 0) {
									iconTileX--;
								}
								if (direction == 1 && iconTileX < sceneWidth - 1 && iconTileX < tileX + 3 && (collisionFlags[iconTileX + 1][iconTileY] & 0x1280180) == 0) {
									iconTileX++;
								}
								if (direction == 2 && iconTileY > 0 && iconTileY > tileY - 3 && (collisionFlags[iconTileX][iconTileY - 1] & 0x1280102) == 0) {
									iconTileY--;
								}
								if (direction == 3 && iconTileY < sceneHeight - 1 && iconTileY < tileY + 3 && (collisionFlags[iconTileX][iconTileY + 1] & 0x1280120) == 0) {
									iconTileY++;
								}
							}

						}
						mapFunctionIconSprites[mapFunctionIconCount] = mapFunctions[mapFunctionId];
						mapFunctionIconTileX[mapFunctionIconCount] = iconTileX;
						mapFunctionIconTileY[mapFunctionIconCount] = iconTileY;
						mapFunctionIconCount++;
					}
				}
			}

		}

	}

	public void spawnGroundItem(int i, int j) {
		NodeList class19 = groundArray[plane][i][j];
		if (class19 == null) {
			worldController.removeGroundItemStack(plane, i, j);
			return;
		}
		long k = Long.MIN_VALUE;
		Object obj = null;
		for (Item item = (Item) class19.firstFromTail(); item != null; item = (Item) class19.nextTowardHead()) {
			ItemDef itemDef = ItemDef.forID(item.ID);
			long l = itemDef.value;
			if (itemDef.stackable) {
				l *= item.amount + 1;
				// notifyItemSpawn(item, i + baseX, j + baseY);
			}

			if (l > k) {
				k = l;
				obj = item;
			}
		}

		class19.insertTail(((Node) obj));
		Object obj1 = null;
		Object obj2 = null;
		for (Item class30_sub2_sub4_sub2_1 = (Item) class19.firstFromTail(); class30_sub2_sub4_sub2_1 != null; class30_sub2_sub4_sub2_1 = (Item) class19.nextTowardHead()) {
			if (class30_sub2_sub4_sub2_1.ID != ((Item) obj).ID && obj1 == null) {
				obj1 = class30_sub2_sub4_sub2_1;
			}
			if (class30_sub2_sub4_sub2_1.ID != ((Item) obj).ID && class30_sub2_sub4_sub2_1.ID != ((Item) obj1).ID && obj2 == null) {
				obj2 = class30_sub2_sub4_sub2_1;
			}
		}

		int i1 = i + (j << 7) + 0x60000000;
		worldController.addItemStack(i, i1, ((Animable) obj1), interpolatedGroundHeight(plane, j * 128 + 64, i * 128 + 64), ((Animable) obj2), ((Animable) obj), plane, j);
	}

	public void addNpcsToScene(boolean firstPass) {
		for (int npcSlot = 0; npcSlot < npcCount; npcSlot++) {
			NPC npc = npcArray[npcIndices[npcSlot]];
			int uid = 0x20000000 + (npcIndices[npcSlot] << 14);
			if (npc == null || !npc.isVisible() || npc.desc.placedInFirstPass != firstPass) {
				continue;
			}
			int tileX = npc.x >> 7;
			int tileY = npc.y >> 7;
			if (tileX < 0 || tileX >= 104 || tileY < 0 || tileY >= 104) {
				continue;
			}
			if (npc.tileSize == 1 && (npc.x & 0x7f) == 64 && (npc.y & 0x7f) == 64) {
				if (anIntArrayArray929[tileX][tileY] == anInt1265) {
					continue;
				}
				anIntArrayArray929[tileX][tileY] = anInt1265;
			}
			if (!npc.desc.interactable) {
				uid += 0x80000000;
			}
			worldController.addEntity(plane, npc.facingDirection, interpolatedGroundHeight(plane, npc.y, npc.x), uid, npc.y, (npc.tileSize - 1) * 64 + 60, npc.x, npc, npc.aBoolean1541);
		}
	}

	public boolean replayWave() {
		return Signlink.wavereplay();
	}

	public void loadError() {
		String s = "ondemand";// was a constant parameter
		if (!ClientSettings.ANDROID) {
			DesktopHooks.showDocument(this, s);
		}
		do {
			try {
				Thread.sleep(1000L);
			} catch (Exception _ex) {
			}
		} while (true);
	}
	
	static final void sleep(long time) {
		if (time > 0L) {
			if (time % 10L != 0L)
				threadSleep(time);
			else {
				threadSleep(time - 1L);
				threadSleep(1L);
			}
		}
	}
	
	static final void threadSleep(long time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException interruptedexception) {
			/* empty */
		}
	}

	public void buildInterfaceMenu(int i, RSInterface class9, int k, int l, int i1, int j1) {
		if (class9.type != 0 || class9.children == null || class9.hidden) {
			return;
		}
		if (k < i || i1 < l || k > i + class9.width || i1 > l + class9.height) {
			return;
		}
		int k1 = class9.children.length;
		for (int l1 = 0; l1 < k1; l1++) {
			int i2 = class9.childX[l1] + i;
			int j2 = class9.childY[l1] + l - j1;
			RSInterface class9_1 = RSInterface.interfaceCache[class9.children[l1]];
			i2 += class9_1.offsetX;
			j2 += class9_1.offsetY;
			if ((class9_1.hoverRedirectId >= 0 || class9_1.disabledHoverColor != 0) && k >= i2 && i1 >= j2 && k < i2 + class9_1.width && i1 < j2 + class9_1.height) {
				if (class9_1.hoverRedirectId >= 0) {
					anInt886 = class9_1.hoverRedirectId;
				} else {
					anInt886 = class9_1.id;
				}
			}
			if (class9_1.type == 0) {
				buildInterfaceMenu(i2, class9_1, k, j2, i1, class9_1.scrollPosition);
				if (class9_1.scrollMax > class9_1.height) {
					handleScrollbarInput(i2 + class9_1.width, class9_1.height, k, i1, class9_1, j2, true, class9_1.scrollMax);
				}
			} else {
				if (class9_1.atActionType == 1 && k >= i2 && i1 >= j2 && k < i2 + class9_1.width && i1 < j2 + class9_1.height) {
					boolean flag = false;
					if (class9_1.contentType != 0) {
						flag = buildFriendsListMenu(class9_1);
					}
					if (!flag) {
						menuActionName[menuActionRow] = showInfo ? class9_1.tooltip + ", " + class9_1.id : class9_1.tooltip;
						menuActionID[menuActionRow] = 315;
						menuActionCmd3[menuActionRow] = class9_1.id;
						menuActionRow++;
					}
				}
				if (class9_1.atActionType == 2 && spellSelected == 0 && k >= i2 && i1 >= j2 && k < i2 + class9_1.width && i1 < j2 + class9_1.height) {
					String s = class9_1.selectedActionName;
					if (s.indexOf(" ") != -1) {
						s = s.substring(0, s.indexOf(" "));
					}
					menuActionName[menuActionRow] = s + " @gre@" + class9_1.spellName;
					menuActionID[menuActionRow] = 626;
					menuActionCmd3[menuActionRow] = class9_1.id;
					menuActionRow++;
				}
				if (class9_1.atActionType == 3 && k >= i2 && i1 >= j2 && k < i2 + class9_1.width && i1 < j2 + class9_1.height) {
					menuActionName[menuActionRow] = "Close";
					menuActionID[menuActionRow] = 200;
					menuActionCmd3[menuActionRow] = class9_1.id;
					menuActionRow++;
				}
				if (class9_1.atActionType == 4 && k >= i2 && i1 >= j2 && k < i2 + class9_1.width && i1 < j2 + class9_1.height) {
					menuActionName[menuActionRow] = showInfo ? class9_1.tooltip + ", " + class9_1.id : class9_1.tooltip;
					menuActionID[menuActionRow] = 169;
					menuActionCmd3[menuActionRow] = class9_1.id;
					menuActionRow++;
				}
				if (class9_1.atActionType == 5 && k >= i2 && i1 >= j2 && k < i2 + class9_1.width && i1 < j2 + class9_1.height) {
					menuActionName[menuActionRow] = showInfo ? class9_1.tooltip + ", " + class9_1.id : class9_1.tooltip;
					menuActionID[menuActionRow] = 646;
					menuActionCmd3[menuActionRow] = class9_1.id;
					menuActionRow++;
				}
				if (class9_1.atActionType == 6 && !aBoolean1149 && k >= i2 && i1 >= j2 && k < i2 + class9_1.width && i1 < j2 + class9_1.height) {
					menuActionName[menuActionRow] = showInfo ? class9_1.tooltip + ", " + class9_1.id : class9_1.tooltip;
					menuActionID[menuActionRow] = 679;
					menuActionCmd3[menuActionRow] = class9_1.id;
					menuActionRow++;
				}
				if (class9_1.type == 2) {
					int k2 = 0;
					for (int l2 = 0; l2 < class9_1.height; l2++) {
						for (int i3 = 0; i3 < class9_1.width; i3++) {
							int j3 = i2 + i3 * (32 + class9_1.invSpritePadX);
							int k3 = j2 + l2 * (32 + class9_1.invSpritePadY);
							if (k2 < 20) {
								j3 += class9_1.spritesX[k2];
								k3 += class9_1.spritesY[k2];
							}
							if (k >= j3 && i1 >= k3 && k < j3 + 32 && i1 < k3 + 32) {
								mouseInvInterfaceIndex = k2;
								lastActiveInvInterface = class9_1.id;
								if (class9_1.itemIdsPlusOne[k2] > 0) {
									ItemDef itemDef = ItemDef.forID(class9_1.itemIdsPlusOne[k2] - 1);
									if (itemSelected == 1 && class9_1.isInventoryInterface) {
										if (class9_1.id != anInt1284 || k2 != anInt1283) {
											menuActionName[menuActionRow] = "Use " + selectedItemName + " with @lre@" + itemDef.name;
											menuActionID[menuActionRow] = 870;
											menuActionCmd1[menuActionRow] = itemDef.id;
											menuActionCmd2[menuActionRow] = k2;
											menuActionCmd3[menuActionRow] = class9_1.id;
											menuActionRow++;
										}
									} else if (spellSelected == 1 && class9_1.isInventoryInterface) {
										if ((spellUsableOn & 0x10) == 16) {
											menuActionName[menuActionRow] = spellTooltip + " @lre@" + itemDef.name;
											menuActionID[menuActionRow] = 543;
											menuActionCmd1[menuActionRow] = itemDef.id;
											menuActionCmd2[menuActionRow] = k2;
											menuActionCmd3[menuActionRow] = class9_1.id;
											menuActionRow++;
										}
									} else {
										if (class9_1.isInventoryInterface) {
											for (int l3 = 4; l3 >= 3; l3--) {
												if (itemDef.actions != null && itemDef.actions[l3] != null) {
													menuActionName[menuActionRow] = itemDef.actions[l3] + " @lre@" + itemDef.name;
													if (l3 == 3) {
															menuActionID[menuActionRow] = 493;
													}
													if (l3 == 4) {
															menuActionID[menuActionRow] = 847;
													}
													menuActionCmd1[menuActionRow] = itemDef.id;
													menuActionCmd2[menuActionRow] = k2;
													menuActionCmd3[menuActionRow] = class9_1.id;
													menuActionRow++;
												} else if (l3 == 4) {
													menuActionName[menuActionRow] = "Drop @lre@" + itemDef.name;
													menuActionID[menuActionRow] = 847;
													menuActionCmd1[menuActionRow] = itemDef.id;
													menuActionCmd2[menuActionRow] = k2;
													menuActionCmd3[menuActionRow] = class9_1.id;
													menuActionRow++;
												}
											}

										}
										if (class9_1.usableItemInterface) {
										    if (shiftDown)
											{
												menuActionName[menuActionRow] = "Drop @lre@" + itemDef.name;
											} else {
												menuActionName[menuActionRow] = "Use @lre@" + itemDef.name;
											}
											menuActionID[menuActionRow] = 447;
											menuActionCmd1[menuActionRow] = itemDef.id;
											menuActionCmd2[menuActionRow] = k2;
											menuActionCmd3[menuActionRow] = class9_1.id;
											menuActionRow++;
										}
										if (class9_1.isInventoryInterface && itemDef.actions != null) {
											for (int i4 = 2; i4 >= 0; i4--) {
												if (itemDef.actions[i4] != null) {
													if (shiftDown)
													{
														menuActionName[menuActionRow] = "Drop @lre@" + itemDef.name;
														menuActionCmd1[menuActionRow] = itemDef.id;
														menuActionCmd2[menuActionRow] = k2;
														menuActionCmd3[menuActionRow] = class9_1.id;
													}
													else
													{
														menuActionName[menuActionRow] = itemDef.actions[i4] + " @lre@" + itemDef.name; //Seems like it's Wear + SPACE + ItemName.
														if (i4 == 0) {
															menuActionID[menuActionRow] = 74;
														}
														if (i4 == 1) {
															menuActionID[menuActionRow] = 454;
														}
														if (i4 == 2) {
															menuActionID[menuActionRow] = 539;
														}
														menuActionCmd1[menuActionRow] = itemDef.id;
														menuActionCmd2[menuActionRow] = k2;
														menuActionCmd3[menuActionRow] = class9_1.id;
														menuActionRow++;
													}
												}
											}

										}
										if (class9_1.actions != null && !(RSInterface.interfaceCache[5383].disabledText.startsWith("Search") && class9_1.parentID == 5292)) {
											for (int j4 = 4; j4 >= 0; j4--) {
												if (class9_1.actions[j4] != null) {
													menuActionName[menuActionRow] = class9_1.actions[j4] + " @lre@" + itemDef.name;
													if (j4 == 0) {
														menuActionID[menuActionRow] = 632;
													}
													if (j4 == 1) {
														menuActionID[menuActionRow] = 78;
													}
													if (j4 == 2) {
														menuActionID[menuActionRow] = 867;
													}
													if (j4 == 3) {
														menuActionID[menuActionRow] = 431;
													}
													if (j4 == 4) {
														menuActionID[menuActionRow] = 53;
													}
													menuActionCmd1[menuActionRow] = itemDef.id;
													menuActionCmd2[menuActionRow] = k2;
													menuActionCmd3[menuActionRow] = class9_1.id;
													menuActionRow++;
												}
											}
										}
										menuActionName[menuActionRow] = "Examine @lre@" + itemDef.name + (showInfo ? " @gre@(@whi@" + (class9_1.itemIdsPlusOne[k2] - 1) + "@gre@)" : "");
										menuActionID[menuActionRow] = 1125;
										menuActionCmd1[menuActionRow] = itemDef.id;
										menuActionCmd2[menuActionRow] = k2;
										menuActionCmd3[menuActionRow] = class9_1.id;
										menuActionRow++;
									}
								}
							}
							k2++;
						}

					}

				}
			}
		}
	}

	public void drawScrollbar(int viewportHeight, int scrollOffset, int topY, int leftX, int contentHeight) {
		scrollUpArrow.drawNonzeroIndexed(leftX, topY);
		scrollDownArrow.drawNonzeroIndexed(leftX, topY + viewportHeight - 16);
		DrawingArea.fillRectangle(viewportHeight - 32, topY + 16, scrollTrackColor, 16, leftX);
		int thumbHeight = (viewportHeight - 32) * viewportHeight / contentHeight;
		if (thumbHeight < 8) {
			thumbHeight = 8;
		}
		int thumbOffset = (viewportHeight - 32 - thumbHeight) * scrollOffset / (contentHeight - viewportHeight);
		DrawingArea.fillRectangle(thumbHeight, topY + 16 + thumbOffset, scrollThumbColor, 16, leftX);
		DrawingArea.drawVerticalLine(topY + 16 + thumbOffset, scrollHighlightColor, thumbHeight, leftX);
		DrawingArea.drawVerticalLine(topY + 16 + thumbOffset, scrollHighlightColor, thumbHeight, leftX + 1);
		DrawingArea.drawHorizontalLine(topY + 16 + thumbOffset, scrollHighlightColor, 16, leftX);
		DrawingArea.drawHorizontalLine(topY + 17 + thumbOffset, scrollHighlightColor, 16, leftX);
		DrawingArea.drawVerticalLine(topY + 16 + thumbOffset, scrollShadowColor, thumbHeight, leftX + 15);
		DrawingArea.drawVerticalLine(topY + 17 + thumbOffset, scrollShadowColor, thumbHeight - 1, leftX + 14);
		DrawingArea.drawHorizontalLine(topY + 15 + thumbOffset + thumbHeight, scrollShadowColor, 16, leftX);
		DrawingArea.drawHorizontalLine(topY + 14 + thumbOffset + thumbHeight, scrollShadowColor, 15, leftX + 1);
	}

	public void updateNPCs(Stream stream, int i) {
		anInt839 = 0;
		maskUpdateCount = 0;
		method139(stream);
		addNewNpcs(i, stream);
		decodeNpcUpdateMasks(stream);
		for (int k = 0; k < anInt839; k++) {
			int l = anIntArray840[k];
			if (npcArray[l].anInt1537 != loopCycle) {
				npcArray[l].desc = null;
				npcArray[l] = null;
			}
		}

		if (stream.currentOffset != i) {
			Signlink.reporterror(myUsername + " size mismatch in getnpcpos - pos:" + stream.currentOffset + " psize:" + i);
			throw new RuntimeException("eek");
		}
		for (int i1 = 0; i1 < npcCount; i1++) {
			if (npcArray[npcIndices[i1]] == null) {
				Signlink.reporterror(myUsername + " null entry in npc list - pos:" + i1 + " size:" + npcCount);
				throw new RuntimeException("eek");
			}
		}

	}

	public void processChatModeClick() {
		if (super.clickMode3 == 1) {
			if (super.saveClickX >= 6 && super.saveClickX <= 106 && super.saveClickY >= 467 && super.saveClickY <= 499) {
				publicChatMode = (publicChatMode + 1) % 4;
				aBoolean1233 = true;
				inputTaken = true;
				stream.writeEncryptedOpcode(95);
				stream.writeByte(publicChatMode);
				stream.writeByte(privateChatMode);
				stream.writeByte(tradeMode);
			}
			if (super.saveClickX >= 135 && super.saveClickX <= 235 && super.saveClickY >= 467 && super.saveClickY <= 499) {
				privateChatMode = (privateChatMode + 1) % 3;
				aBoolean1233 = true;
				inputTaken = true;
				stream.writeEncryptedOpcode(95);
				stream.writeByte(publicChatMode);
				stream.writeByte(privateChatMode);
				stream.writeByte(tradeMode);
			}
			if (super.saveClickX >= 273 && super.saveClickX <= 373 && super.saveClickY >= 467 && super.saveClickY <= 499) {
				tradeMode = (tradeMode + 1) % 3;
				aBoolean1233 = true;
				inputTaken = true;
				stream.writeEncryptedOpcode(95);
				stream.writeByte(publicChatMode);
				stream.writeByte(privateChatMode);
				stream.writeByte(tradeMode);
			}
			if (super.saveClickX >= 412 && super.saveClickX <= 512 && super.saveClickY >= 467 && super.saveClickY <= 499) {
				if (openInterfaceID == -1) {
					closeOpenInterfaces();
					reportAbuseInput = "";
					canMute = false;
					for (RSInterface element : RSInterface.interfaceCache) {
						if (element == null || element.contentType != 600) {
							continue;
						}
						reportAbuseInterfaceID = openInterfaceID = element.parentID;
						break;
					}

				} else {
					pushMessage("Please close the interface you have open before using 'report abuse'", 0, "");
				}
			}
			anInt940++;
			if (anInt940 > 1386) {
				anInt940 = 0;
				stream.writeEncryptedOpcode(165);
				stream.writeByte(0);
				int j = stream.currentOffset;
				stream.writeByte(139);
				stream.writeByte(150);
				stream.writeShortBigEndian(32131);
				stream.writeByte((int) (Math.random() * 256D));
				stream.writeShortBigEndian(3250);
				stream.writeByte(177);
				stream.writeShortBigEndian(24859);
				stream.writeByte(119);
				if ((int) (Math.random() * 2D) == 0) {
					stream.writeShortBigEndian(47234);
				}
				if ((int) (Math.random() * 2D) == 0) {
					stream.writeByte(21);
				}
				stream.backpatchBytePayloadLength(stream.currentOffset - j);
			}
		}
	}

	public void method33(int i) {
		int action = Varp.cache[i].settingAction;
		if (action == 0) {
			return;
		}
		int config = variousSettings[i];
		if (action == 1) {
			if (config == 1) {
				Texture.buildPalettes(0.90000000000000002D);
			}
			if (config == 2) {
				Texture.buildPalettes(0.80000000000000004D);
			}
			if (config == 3) {
				Texture.buildPalettes(0.69999999999999996D);
			}
			if (config == 4) {
				Texture.buildPalettes(0.59999999999999998D);
			}
			ItemDef.mruNodes1.clear();
			welcomeScreenRaised = true;
		}
		if (action == 3) {
			int volume = 0;
			if (config == 0)
				volume = 255;
			if (config == 1)
				volume = 192;
			if (config == 2)
				volume = 128;
			if (config == 3)
				volume = 64;
			if (config == 4)
				volume = 0;
			if (volume != musicVolume) {
				if (musicVolume != 0 || currentSong == -1) {
					if (volume != 0)
						setVolume(volume);
					else {
						stopMusic(false);
						previousSong = 0;
					}
				} else {
					requestSongWithoutFade(volume, false, currentSong);
					previousSong = 0;//TODO temp music
				}
				musicVolume = volume;
			}
		}
		if (action == 4) {
			if (ClientSettings.ANDROID) {
				AndroidHooks.setSoundVolume(config);
			} else {
				DesktopHooks.setSoundVolume(config);
			}
			if (config == 0) {
				aBoolean848 = true;
				setWaveVolume(0);
			}
			if (config == 1) {
				aBoolean848 = true;
				setWaveVolume(-400);
			}
			if (config == 2) {
				aBoolean848 = true;
				setWaveVolume(-800);
			}
			if (config == 3) {
				aBoolean848 = true;
				setWaveVolume(-1200);
			}
			if (config == 4) {
				aBoolean848 = false;
			}
		}
		if (action == 5) {
			anInt1253 = config;
		}
		if (action == 6) {
			anInt1249 = config;
		}
		if (action == 8) {
			splitpublicChat = config;
			inputTaken = true;
		}
		if (action == 9) {
			anInt913 = config;
		}
	}

	public void updateEntities() {
		try {
			// Draw item info
			for (int k5 = 0; k5 < 104; k5++) {
				for (int l5 = 0; l5 < 104; l5++) {
					NodeList class19 = groundArray[plane][k5][l5];
					if (class19 != null) {
						int offset = 5;
						for (Item item = (Item) class19.firstFromTail(); item != null; item = (Item) class19.nextTowardHead()) {
							ItemDef itemDef = ItemDef.forID(item.ID);
							long totalValue = Math.max(1, item.amount) * Math.max(1, itemDef.value);
							totalValue = totalValue > 0 ? totalValue : Integer.MAX_VALUE;
							projectWorldToScreen(k5 * 128 + 64, 20, l5 * 128 + 64);
							// only show ground items names if worth more than x (1k default)
							if (totalValue >= customSettingMinItemValue) {
								int color = RSColors.WHITE;
								if (totalValue >= 1e5) {
									color = RSColors.GREEN;
								} else if (totalValue >= 1e4) {
									color = RSColors.CYAN;
								} else if (totalValue >= 1e3) {
									color = RSColors.YELLOW;
								}
								String text = "";
								if (item.amount > 1) {
									DecimalFormatSymbols separator = new DecimalFormatSymbols();
									separator.setGroupingSeparator(',');
									DecimalFormat formatter = new DecimalFormat("#,###,###,###", separator);
									text += formatter.format(item.amount) + " x ";
								}
								text += itemDef.name + " (" +  intToShortLetter(totalValue) + " gp)";
								aTextDrawingArea_1270.drawLiteralText(color, text, spriteDrawY - offset, spriteDrawX - (aTextDrawingArea_1270.measureTaggedTextWidth(text) / 2));
								offset += 10;
							}
						}
					}
				}
			}
			// Entity stuff
			int anInt974 = 0;
			for (int j = -1; j < playerCount + npcCount; j++) {
				Object obj;
				if (j == -1) {
					obj = myPlayer;
				} else if (j < playerCount) {
					obj = playerArray[playerIndices[j]];
				} else {
					obj = npcArray[npcIndices[j - playerCount]];
				}
				if (obj == null || !((Entity) obj).isVisible()) {
					continue;
				}
				if (obj instanceof NPC) {
					EntityDef entityDef = ((NPC) obj).desc;
					if (entityDef.childrenIDs != null) {
						entityDef = entityDef.getMorphedDefinition();
					}
					if (entityDef == null) {
						continue;
					}
				}
				if (j < playerCount) {
					int l = 30;
					Player player = (Player) obj;
					if (player.combatLevel == 0) {
						if (customSettingVisiblePlayerNames) {
							// Show shops
							projectEntityToScreen(((Entity) obj), ((Entity) obj).height + 15);
							// ItemDef.getSprite(995, 1000, 0xffff00).drawSprite(spriteDrawX - 16, spriteDrawY - l);
							aTextDrawingArea_1270.drawCenteredLiteralText(0x00ffff, "[SHOP]", spriteDrawY - 5, spriteDrawX);
						}
					} else if (customSettingVisiblePlayerNames) {
						// Show player names
						projectEntityToScreen(((Entity) obj), ((Entity) obj).height + 15);
						aTextDrawingArea_1270.drawCenteredLiteralText(0xffffff, player.name, spriteDrawY - 5, spriteDrawX);
						if (player.privelage >= 1) {
							projectEntityToScreen(((Entity) obj), ((Entity) obj).height + 15);
							int icon = Math.max(0, Math.min(1, player.privelage - 1));
							modIcons[icon].drawNonzeroIndexed( spriteDrawX - player.name.length() * 3 - 16, spriteDrawY - 7);
						}
					}
					if (player.headIcon >= 0) {
						projectEntityToScreen(((Entity) obj), ((Entity) obj).height + 15);
						if (spriteDrawX > -1) {
							if (player.skullIcon < 2) {
								skullIcons[player.skullIcon].drawSprite(spriteDrawX - 12, spriteDrawY - l);
								l += 25;
							}
							if (player.headIcon < 7) {
								headIcons[player.headIcon].drawSprite(spriteDrawX - 12, spriteDrawY - l);
								l += 18;
							}
						}
					}
					if (j >= 0 && anInt855 == 10 && anInt933 == playerIndices[j]) {
						projectEntityToScreen(((Entity) obj), ((Entity) obj).height + 15);
						if (spriteDrawX > -1) {
							headIconsHint[1].drawSprite(spriteDrawX - 12, spriteDrawY - l);
						}
					}
				} else {
					EntityDef entityDef_1 = ((NPC) obj).desc;
					if (entityDef_1.headIconId >= 0 && entityDef_1.headIconId < headIcons.length) {
						projectEntityToScreen(((Entity) obj), ((Entity) obj).height + 15);
						if (spriteDrawX > -1) {
							headIcons[entityDef_1.headIconId].drawSprite(spriteDrawX - 12, spriteDrawY - 30);
						}
					}
					if (anInt855 == 1 && anInt1222 == npcIndices[j - playerCount] && loopCycle % 20 < 10) {
						projectEntityToScreen(((Entity) obj), ((Entity) obj).height + 15);
						if (spriteDrawX > -1) {
							headIconsHint[0].drawSprite(spriteDrawX - 12, spriteDrawY - 28);
						}
					}
				}
				// Chat messages sent
				if (((Entity) obj).textSpoken != null && (j >= playerCount || publicChatMode == 0 || publicChatMode == 3 || publicChatMode == 1 && isFriendOrSelf(((Player) obj).name))) {
					projectEntityToScreen(((Entity) obj), ((Entity) obj).height);
					if (spriteDrawX > -1 && anInt974 < anInt975) {
						anIntArray979[anInt974] = chatTextDrawingArea.measureLiteralTextWidth(((Entity) obj).textSpoken) / 2;
						anIntArray978[anInt974] = chatTextDrawingArea.maxAsciiGlyphHeightPixels;
						anIntArray976[anInt974] = spriteDrawX;
						anIntArray977[anInt974] = spriteDrawY;
						anIntArray980[anInt974] = ((Entity) obj).anInt1513;
						anIntArray981[anInt974] = ((Entity) obj).anInt1531;
						anIntArray982[anInt974] = ((Entity) obj).textCycle;
						aStringArray983[anInt974++] = ((Entity) obj).textSpoken;
						if (anInt1249 == 0 && ((Entity) obj).anInt1531 >= 1 && ((Entity) obj).anInt1531 <= 3) {
							anIntArray978[anInt974] += 10;
							anIntArray977[anInt974] += 5;
						}
						if (anInt1249 == 0 && ((Entity) obj).anInt1531 == 4) {
							anIntArray979[anInt974] = 60;
						}
						if (anInt1249 == 0 && ((Entity) obj).anInt1531 == 5) {
							anIntArray978[anInt974] += 5;
						}
					}
				}
				// HP markers for player?
				if (((Entity) obj).loopCycleStatus > loopCycle) {
					try {
						projectEntityToScreen(((Entity) obj), ((Entity) obj).height + 15);
						if (spriteDrawX > -1) {
							int i1 = ((Entity) obj).currentHealth * 30 / ((Entity) obj).maxHealth;
							if (i1 > 30) {
								i1 = 30;
							}
							DrawingArea.fillRectangle(5, spriteDrawY - 3, 0x00ff00, i1, spriteDrawX - 15);
							DrawingArea.fillRectangle(5, spriteDrawY - 3, 0xff0000, 30 - i1, spriteDrawX - 15 + i1);
						}
					} catch (Exception e) {
					}
				}
				// Hit markers
				for (int j1 = 0; j1 < 4; j1++) {
					if (((Entity) obj).hitsLoopCycle[j1] > loopCycle) {
						projectEntityToScreen(((Entity) obj), ((Entity) obj).height / 2);
						if (spriteDrawX > -1) {
							if (j1 == 1) {
								spriteDrawY -= 20;
							}
							if (j1 == 2) {
								spriteDrawX -= 15;
								spriteDrawY -= 10;
							}
							if (j1 == 3) {
								spriteDrawX += 15;
								spriteDrawY -= 10;
							}
							hitMarks[((Entity) obj).hitMarkTypes[j1]].drawSprite(spriteDrawX - 12, spriteDrawY - 12);
							aTextDrawingArea_1270.drawCenteredLiteralText(0, String.valueOf(((Entity) obj).hitArray[j1]), spriteDrawY + 4, spriteDrawX);
							aTextDrawingArea_1270.drawCenteredLiteralText(0xffffff, String.valueOf(((Entity) obj).hitArray[j1]), spriteDrawY + 3, spriteDrawX - 1);
						}
					}
				}
			}
			// Hit markers
			for (int k = 0; k < anInt974; k++) {
				int k1 = anIntArray976[k];
				int l1 = anIntArray977[k];
				int j2 = anIntArray979[k];
				int k2 = anIntArray978[k];
				boolean flag = true;
				while (flag) {
					flag = false;
					for (int l2 = 0; l2 < k; l2++) {
						if (l1 + 2 > anIntArray977[l2] - anIntArray978[l2] && l1 - k2 < anIntArray977[l2] + 2 && k1 - j2 < anIntArray976[l2] + anIntArray979[l2] && k1 + j2 > anIntArray976[l2] - anIntArray979[l2] && anIntArray977[l2] - anIntArray978[l2] < l1) {
							l1 = anIntArray977[l2] - anIntArray978[l2];
							flag = true;
						}
					}

				}
				spriteDrawX = anIntArray976[k];
				spriteDrawY = anIntArray977[k] = l1;
				String s = aStringArray983[k];
				if (anInt1249 == 0) {
					int i3 = 0xffff00;
					if (anIntArray980[k] < 6) {
						i3 = anIntArray965[anIntArray980[k]];
					}
					if (anIntArray980[k] == 6) {
						i3 = anInt1265 % 20 >= 10 ? 0xffff00 : 0xff0000;
					}
					if (anIntArray980[k] == 7) {
						i3 = anInt1265 % 20 >= 10 ? 0x00ffff : 255;
					}
					if (anIntArray980[k] == 8) {
						i3 = anInt1265 % 20 >= 10 ? 0x80ff80 : 45056;
					}
					if (anIntArray980[k] == 9) {
						int j3 = 150 - anIntArray982[k];
						if (j3 < 50) {
							i3 = 0xff0000 + 1280 * j3;
						} else if (j3 < 100) {
							i3 = 0xffff00 - 0x50000 * (j3 - 50);
						} else if (j3 < 150) {
							i3 = 0x00ff00 + 5 * (j3 - 100);
						}
					}
					if (anIntArray980[k] == 10) {
						int k3 = 150 - anIntArray982[k];
						if (k3 < 50) {
							i3 = 0xff0000 + 5 * k3;
						} else if (k3 < 100) {
							i3 = 0xff00ff - 0x50000 * (k3 - 50);
						} else if (k3 < 150) {
							i3 = 255 + 0x50000 * (k3 - 100) - 5 * (k3 - 100);
						}
					}
					if (anIntArray980[k] == 11) {
						int l3 = 150 - anIntArray982[k];
						if (l3 < 50) {
							i3 = 0xffffff - 0x50005 * l3;
						} else if (l3 < 100) {
							i3 = 0x00ff00 + 0x50005 * (l3 - 50);
						} else if (l3 < 150) {
							i3 = 0xffffff - 0x50000 * (l3 - 100);
						}
					}
					if (anIntArray981[k] == 0) {
						chatTextDrawingArea.drawCenteredLiteralText(0, s, spriteDrawY + 1, spriteDrawX);
						chatTextDrawingArea.drawCenteredLiteralText(i3, s, spriteDrawY, spriteDrawX);
					}
					if (anIntArray981[k] == 1) {
						chatTextDrawingArea.drawCenteredVerticalWave(0, s, spriteDrawX, anInt1265, spriteDrawY + 1);
						chatTextDrawingArea.drawCenteredVerticalWave(i3, s, spriteDrawX, anInt1265, spriteDrawY);
					}
					if (anIntArray981[k] == 2) {
						chatTextDrawingArea.drawCenteredWave(spriteDrawX, s, anInt1265, spriteDrawY + 1, 0);
						chatTextDrawingArea.drawCenteredWave(spriteDrawX, s, anInt1265, spriteDrawY, i3);
					}
					if (anIntArray981[k] == 3) {
						chatTextDrawingArea.drawCenteredShake(150 - anIntArray982[k], s, anInt1265, spriteDrawY + 1, spriteDrawX, 0);
						chatTextDrawingArea.drawCenteredShake(150 - anIntArray982[k], s, anInt1265, spriteDrawY, spriteDrawX, i3);
					}
					if (anIntArray981[k] == 4) {
						int i4 = chatTextDrawingArea.measureLiteralTextWidth(s);
						int k4 = (150 - anIntArray982[k]) * (i4 + 100) / 150;
						DrawingArea.setClip(334, spriteDrawX - 50, spriteDrawX + 50, 0);
						chatTextDrawingArea.drawLiteralText(0, s, spriteDrawY + 1, spriteDrawX + 50 - k4);
						chatTextDrawingArea.drawLiteralText(i3, s, spriteDrawY, spriteDrawX + 50 - k4);
						DrawingArea.resetClip();
					}
					if (anIntArray981[k] == 5) {
						int j4 = 150 - anIntArray982[k];
						int l4 = 0;
						if (j4 < 25) {
							l4 = j4 - 25;
						} else if (j4 > 125) {
							l4 = j4 - 125;
						}
						DrawingArea.setClip(spriteDrawY + 5, 0, 512, spriteDrawY - chatTextDrawingArea.maxAsciiGlyphHeightPixels - 1);
						chatTextDrawingArea.drawCenteredLiteralText(0, s, spriteDrawY + 1 + l4, spriteDrawX);
						chatTextDrawingArea.drawCenteredLiteralText(i3, s, spriteDrawY + l4, spriteDrawX);
						DrawingArea.resetClip();
					}
				} else {
					chatTextDrawingArea.drawCenteredLiteralText(0, s, spriteDrawY + 1, spriteDrawX);
					chatTextDrawingArea.drawCenteredLiteralText(0xffff00, s, spriteDrawY, spriteDrawX);
				}
			}
		} catch (Exception e) {
		}
	}

	public void delFriend(long l) {
		try {
			if (l == 0L) {
				return;
			}
			for (int i = 0; i < friendsCount; i++) {
				if (friendsListAsLongs[i] != l) {
					continue;
				}
				friendsCount--;
				needDrawTabArea = true;
				for (int j = i; j < friendsCount; j++) {
					friendsList[j] = friendsList[j + 1];
					friendsNodeIDs[j] = friendsNodeIDs[j + 1];
					friendsListAsLongs[j] = friendsListAsLongs[j + 1];
				}

				stream.writeEncryptedOpcode(215);
				stream.writeLongBigEndian(l);
				break;
			}
		} catch (RuntimeException runtimeexception) {
			Signlink.reporterror("18622, " + false + ", " + l + ", " + runtimeexception.toString());
			throw new RuntimeException();
		}
	}

	public void drawButton(boolean enabled, int x, int y, int width) {
		StreamLoader streamLoader_2 = streamLoaderForName(4, "2d graphics", "media", expectedCRCs[4], 40);
		// All these sprites are 30x30
		Sprite buttonLeft = new Sprite(streamLoader_2, "miscgraphics", enabled ? 7 : 4);
		Sprite buttonRight = new Sprite(streamLoader_2, "miscgraphics", enabled ? 8 : 6);
		int curWidth = 30;
		buttonLeft.drawSprite(x, y);
		while ((curWidth + 26) < width) {
			buttonRight.drawSprite(x + curWidth, y);
			curWidth += 26;
		}
		buttonRight.drawSprite(x + width - 30, y);
	}

	public void drawCheckbox(boolean enabled, int x, int y) {
		StreamLoader streamLoader_2 = streamLoaderForName(4, "2d graphics", "media", expectedCRCs[4], 40);
		Sprite checkboxUnchecked = new Sprite(streamLoader_2, "miscgraphics", 10);
		Sprite checkboxChecked = new Sprite(streamLoader_2, "miscgraphics", 11);
	}

	public void drawTabArea() {
		aRSImageProducer_1163.setAsDrawingArea();
		Texture.lineOffsets = tabAreaOffsets;
		invBack.drawNonzeroIndexed(0, 0);
		if (invOverlayInterfaceID == -1) {
			if (tabInterfaceIDs[tabID] != -1) {
				if (tabID == 7 && ClientSettings.CUSTOM_SETTINGS_TAB) {
					try {
						int centerX = 95;
						int currentY = 10;
						int textMiddle = 22;
						int textTop = 14;
						int textBottom = 29;

						drawButton(customSettingVisiblePlayerNames, centerX - 73, currentY, 146);
						// buttonLeftDisabled.drawSprite(centerX - 73, currentY);
						aTextDrawingArea_1271.drawCenteredTaggedText(RSColors.YELLOW, centerX, "always visible", currentY + textTop, true);
						aTextDrawingArea_1271.drawCenteredTaggedText(RSColors.YELLOW, centerX, "player names", currentY + textBottom, true);
						
						drawButton(true, centerX - 73, currentY += 40, 146);
						aTextDrawingArea_1271.drawCenteredTaggedText(RSColors.YELLOW, centerX, "item drops visible", currentY + textTop, true);
						aTextDrawingArea_1271.drawCenteredTaggedText(RSColors.WHITE, centerX, intToKOrMil(customSettingMinItemValue) + " gp", currentY + textBottom, true);
						
						drawButton(true, centerX - 73, currentY += 40, 146);
						aTextDrawingArea_1271.drawCenteredTaggedText(RSColors.YELLOW, centerX, "draw distance", currentY + textTop, true);
						aTextDrawingArea_1271.drawCenteredTaggedText(RSColors.WHITE, centerX, WorldController.drawDistance + " tiles", currentY + textBottom, true);

						drawButton(customSettingShowExperiencePerHour, centerX - 73, currentY += 40, 146);
						aTextDrawingArea_1271.drawCenteredTaggedText(RSColors.YELLOW, centerX, "show exp info", currentY + textMiddle, true);
						
						drawButton(showInfo, centerX - 73, currentY += 40, 146);
						aTextDrawingArea_1271.drawCenteredTaggedText(RSColors.YELLOW, centerX, "show debug info", currentY + textMiddle, true);

						drawButton(customSettingVisualFixes, centerX - 73, currentY += 40, 146);
						aTextDrawingArea_1271.drawCenteredTaggedText(RSColors.YELLOW, centerX, "visual fixes", currentY + textMiddle, true);
					} catch (Exception e) { }
				}
			}
		}
		if (invOverlayInterfaceID != -1) {
			drawInterface(0, 0, RSInterface.interfaceCache[invOverlayInterfaceID], 0);
		} else if (tabInterfaceIDs[tabID] != -1) {
			drawInterface(0, 0, RSInterface.interfaceCache[tabInterfaceIDs[tabID]], 0);
		}
		if (menuOpen && menuScreenArea == 1) {
			drawMenu();
		}
		aRSImageProducer_1163.drawGraphics(205, super.graphics, 553);
		aRSImageProducer_1165.setAsDrawingArea();
		Texture.lineOffsets = chatBoxAreaOffsets;
	}

	public void scrollAnimatedTextures(int frameStartStamp) {
		if (!lowMem) {
			if (Texture.lastUsedStamps[17] >= frameStartStamp) {
				Background texture = Texture.textureBackgrounds[17];
				int pixelMask = texture.imageWidth * texture.imageHeight - 1;
				int scrollOffset = texture.imageWidth * cyclesSinceDraw * 2;
				byte sourcePixels[] = texture.paletteIndices;
				byte scratchPixels[] = textureScrollScratch;
				for (int pixelIndex = 0; pixelIndex <= pixelMask; pixelIndex++) {
					scratchPixels[pixelIndex] = sourcePixels[pixelIndex - scrollOffset & pixelMask];
				}

				texture.paletteIndices = scratchPixels;
				textureScrollScratch = sourcePixels;
				Texture.releaseTextureBuffer(17);
			}
			if (Texture.lastUsedStamps[24] >= frameStartStamp) {
				Background texture = Texture.textureBackgrounds[24];
				int pixelMask = texture.imageWidth * texture.imageHeight - 1;
				int scrollOffset = texture.imageWidth * cyclesSinceDraw * 2;
				byte sourcePixels[] = texture.paletteIndices;
				byte scratchPixels[] = textureScrollScratch;
				for (int pixelIndex = 0; pixelIndex <= pixelMask; pixelIndex++) {
					scratchPixels[pixelIndex] = sourcePixels[pixelIndex - scrollOffset & pixelMask];
				}

				texture.paletteIndices = scratchPixels;
				textureScrollScratch = sourcePixels;
				Texture.releaseTextureBuffer(24);
			}
			if (Texture.lastUsedStamps[34] >= frameStartStamp) {
				Background texture = Texture.textureBackgrounds[34];
				int pixelMask = texture.imageWidth * texture.imageHeight - 1;
				int scrollOffset = texture.imageWidth * cyclesSinceDraw * 2;
				byte sourcePixels[] = texture.paletteIndices;
				byte scratchPixels[] = textureScrollScratch;
				for (int pixelIndex = 0; pixelIndex <= pixelMask; pixelIndex++) {
					scratchPixels[pixelIndex] = sourcePixels[pixelIndex - scrollOffset & pixelMask];
				}

				texture.paletteIndices = scratchPixels;
				textureScrollScratch = sourcePixels;
				Texture.releaseTextureBuffer(34);
			}
			if (Texture.lastUsedStamps[40] >= frameStartStamp) {
				Background texture = Texture.textureBackgrounds[40];
				int pixelMask = texture.imageWidth * texture.imageHeight - 1;
				int scrollOffset = texture.imageWidth * cyclesSinceDraw * 2;
				byte sourcePixels[] = texture.paletteIndices;
				byte scratchPixels[] = textureScrollScratch;
				for (int pixelIndex = 0; pixelIndex <= pixelMask; pixelIndex++) {
					scratchPixels[pixelIndex] = sourcePixels[pixelIndex - scrollOffset & pixelMask];
				}

				texture.paletteIndices = scratchPixels;
				textureScrollScratch = sourcePixels;
				Texture.releaseTextureBuffer(40);
			}
		}
	}

	public void decrementOverheadTextTimers() {
		for (int playerSlot = -1; playerSlot < playerCount; playerSlot++) {
			int playerIndex;
			if (playerSlot == -1) {
				playerIndex = myPlayerIndex;
			} else {
				playerIndex = playerIndices[playerSlot];
			}
			Player player = playerArray[playerIndex];
			if (player != null && player.textCycle > 0) {
				player.textCycle--;
				if (player.textCycle == 0) {
					player.textSpoken = null;
				}
			}
		}

		for (int npcSlot = 0; npcSlot < npcCount; npcSlot++) {
			int npcIndex = npcIndices[npcSlot];
			NPC npc = npcArray[npcIndex];
			if (npc != null && npc.textCycle > 0) {
				npc.textCycle--;
				if (npc.textCycle == 0) {
					npc.textSpoken = null;
				}
			}
		}

	}

	public void calcCameraPos() {
		int i = anInt1098 * 128 + 64;
		int j = anInt1099 * 128 + 64;
		int k = interpolatedGroundHeight(plane, j, i) - anInt1100;
		if (xCameraPos < i) {
			xCameraPos += anInt1101 + (i - xCameraPos) * anInt1102 / 1000;
			if (xCameraPos > i) {
				xCameraPos = i;
			}
		}
		if (xCameraPos > i) {
			xCameraPos -= anInt1101 + (xCameraPos - i) * anInt1102 / 1000;
			if (xCameraPos < i) {
				xCameraPos = i;
			}
		}
		if (zCameraPos < k) {
			zCameraPos += anInt1101 + (k - zCameraPos) * anInt1102 / 1000;
			if (zCameraPos > k) {
				zCameraPos = k;
			}
		}
		if (zCameraPos > k) {
			zCameraPos -= anInt1101 + (zCameraPos - k) * anInt1102 / 1000;
			if (zCameraPos < k) {
				zCameraPos = k;
			}
		}
		if (yCameraPos < j) {
			yCameraPos += anInt1101 + (j - yCameraPos) * anInt1102 / 1000;
			if (yCameraPos > j) {
				yCameraPos = j;
			}
		}
		if (yCameraPos > j) {
			yCameraPos -= anInt1101 + (yCameraPos - j) * anInt1102 / 1000;
			if (yCameraPos < j) {
				yCameraPos = j;
			}
		}
		i = anInt995 * 128 + 64;
		j = anInt996 * 128 + 64;
		k = interpolatedGroundHeight(plane, j, i) - anInt997;
		int l = i - xCameraPos;
		int i1 = k - zCameraPos;
		int j1 = j - yCameraPos;
		int k1 = (int) Math.sqrt(l * l + j1 * j1);
		int l1 = (int) (Math.atan2(i1, k1) * 325.94900000000001D) & 0x7ff;
		int i2 = (int) (Math.atan2(l, j1) * -325.94900000000001D) & 0x7ff;
		if (l1 < 128) {
			l1 = 128;
		}
		if (l1 > 383) {
			l1 = 383;
		}
		if (yCameraCurve < l1) {
			yCameraCurve += anInt998 + (l1 - yCameraCurve) * anInt999 / 1000;
			if (yCameraCurve > l1) {
				yCameraCurve = l1;
			}
		}
		if (yCameraCurve > l1) {
			yCameraCurve -= anInt998 + (yCameraCurve - l1) * anInt999 / 1000;
			if (yCameraCurve < l1) {
				yCameraCurve = l1;
			}
		}
		int j2 = i2 - xCameraCurve;
		if (j2 > 1024) {
			j2 -= 2048;
		}
		if (j2 < -1024) {
			j2 += 2048;
		}
		if (j2 > 0) {
			xCameraCurve += anInt998 + j2 * anInt999 / 1000;
			xCameraCurve &= 0x7ff;
		}
		if (j2 < 0) {
			xCameraCurve -= anInt998 + -j2 * anInt999 / 1000;
			xCameraCurve &= 0x7ff;
		}
		int k2 = i2 - xCameraCurve;
		if (k2 > 1024) {
			k2 -= 2048;
		}
		if (k2 < -1024) {
			k2 += 2048;
		}
		if (k2 < 0 && j2 > 0 || k2 > 0 && j2 < 0) {
			xCameraCurve = i2;
		}
	}

	public void drawMenu() {
		int xPos = menuOffsetX;
		int yPos = menuOffsetY;
		int menuW = menuWidth;
		int menuH = anInt952;
		int fill = 0x5d5447;

		DrawingArea.fillRectangle(menuH, yPos, fill, menuW, xPos);
		DrawingArea.fillRectangle(16, yPos + 1, 0, menuW - 2, xPos + 1);
		DrawingArea.drawRectangleOutline(yPos + 18, menuH - 19, 0, xPos + 1, menuW - 2);
		chatTextDrawingArea.drawLiteralText(fill, "Choose Option", yPos + 14, xPos + 3);

		// //Border
		// DrawingArea.drawBlendedRectangleOutline(yPos + 2, menuH - 4, 250, 0x706a5e, menuW, xPos);
		// DrawingArea.drawBlendedRectangleOutline(yPos + 1, menuH - 2, 250, 0x706a5e, menuW - 2, xPos + 1);
		// DrawingArea.drawBlendedRectangleOutline(yPos, menuH, 250, 0x706a5e, menuW - 4, xPos + 2);
		// //Border
		// DrawingArea.drawBlendedRectangleOutline(yPos + 1, menuH - 2, 250, 0x2d2822, menuW - 6, xPos + 3);
		// DrawingArea.drawBlendedRectangleOutline(yPos + 2, menuH - 4, 250, 0x2d2822, menuW - 4, xPos + 2);
		// DrawingArea.drawBlendedRectangleOutline(yPos + 3, menuH - 6, 250, 0x2d2822, menuW - 2, xPos + 1);
		// //Border
		// DrawingArea.drawBlendedRectangleOutline(yPos + 19, menuH - 22, 250, 0x524a3d, menuW - 4, xPos + 2);
		// DrawingArea.drawBlendedRectangleOutline(yPos + 20, menuH - 22, 250, 0x524a3d, menuW - 6, xPos + 3);
		// //Menu Fill
		// DrawingArea.fillBlendedRectangle(fill, yPos + 20, menuW - 6, menuH - 23, 170, xPos + 3);
		// //Menu Header
		// DrawingArea.fillBlendedRectangle(0x2a251e, yPos + 2, menuW - 6, 17, 170, xPos + 3);
		// chatTextDrawingArea.method385(0xc6b895, "Choose Option", yPos + 14, xPos + 3);

		int mX = super.mouseX;
		int mY = super.mouseY;
		if (menuScreenArea == 0) {
			mX -= 4;
			mY -= 4;
		}
		if (menuScreenArea == 1) {
			mX -= 553;
			mY -= 205;
		}
		if (menuScreenArea == 2) {
			mX -= 17;
			mY -= 357;
		}
		for (int rowItem = 0; rowItem < menuActionRow; rowItem++) {
			int yPosItem = yPos + 31 + (menuActionRow - 1 - rowItem) * 15;
			int colorItem = 0xffffff;
			if (mX > xPos && mX < xPos + menuW && mY > yPosItem - 13 && mY < yPosItem + 3) {
				colorItem = 0xffff00;
			}
			chatTextDrawingArea.drawTaggedText(true, xPos + 3, colorItem, menuActionName[rowItem], yPosItem);
		}

	}

	public void addFriend(long l) {
		try {
			if (l == 0L) {
				return;
			}
			if (friendsCount >= 100 && anInt1046 != 1) {
				pushMessage("Your friendlist is full. Max of 100 for free users, and 200 for members", 0, "");
				return;
			}
			if (friendsCount >= 200) {
				pushMessage("Your friendlist is full. Max of 100 for free users, and 200 for members", 0, "");
				return;
			}
			String s = TextClass.formatDisplayName(TextClass.decodeBase37Name(l));
			for (int i = 0; i < friendsCount; i++) {
				if (friendsListAsLongs[i] == l) {
					pushMessage(s + " is already on your friend list", 0, "");
					return;
				}
			}
			for (int j = 0; j < ignoreCount; j++) {
				if (ignoreListAsLongs[j] == l) {
					pushMessage("Please remove " + s + " from your ignore list first", 0, "");
					return;
				}
			}

			if (s.equals(myPlayer.name)) {
				return;
			} else {
				friendsList[friendsCount] = s;
				friendsListAsLongs[friendsCount] = l;
				friendsNodeIDs[friendsCount] = 0;
				friendsCount++;
				needDrawTabArea = true;
				stream.writeEncryptedOpcode(188);
				stream.writeLongBigEndian(l);
				return;
			}
		} catch (RuntimeException runtimeexception) {
			Signlink.reporterror("15283, " + (byte) 68 + ", " + l + ", " + runtimeexception.toString());
		}
		throw new RuntimeException();
	}

	public int interpolatedGroundHeight(int requestedPlane, int worldY, int worldX) {
		int tileX = worldX >> 7;
		int tileY = worldY >> 7;
		if (tileX < 0 || tileY < 0 || tileX > 103 || tileY > 103) {
			return 0;
		}
		int heightPlane = requestedPlane;
		if (heightPlane < 3 && (byteGroundArray[1][tileX][tileY] & 2) == 2) {
			heightPlane++;
		}
		int tileOffsetX = worldX & 0x7f;
		int tileOffsetY = worldY & 0x7f;
		int lowerYHeight = intGroundArray[heightPlane][tileX][tileY] * (128 - tileOffsetX) + intGroundArray[heightPlane][tileX + 1][tileY] * tileOffsetX >> 7;
		int upperYHeight = intGroundArray[heightPlane][tileX][tileY + 1] * (128 - tileOffsetX) + intGroundArray[heightPlane][tileX + 1][tileY + 1] * tileOffsetX >> 7;
		return lowerYHeight * (128 - tileOffsetY) + upperYHeight * tileOffsetY >> 7;
	}

	public static String intToKOrMil(int amount) {
		if (amount < 0x186a0) {
			return String.valueOf(amount);
		}
		if (amount < 0x989680) {
			return amount / 1000 + "K";
		} else {
			return amount / 0xf4240 + "M";
		}
	}

	public static String intToShortLetter(long number) {
		DecimalFormat oneDecimalFormat = new DecimalFormat("0.0");
		double amountAsDouble = number;
		if (amountAsDouble >= 1e9) { // 1B
				return oneDecimalFormat.format((amountAsDouble / 1e9)) + "B";
		}
		if (amountAsDouble >= 1e7) { // 1K
				return (int) (amountAsDouble / 1e6) + "M";
		}
		if (amountAsDouble >= 1e6) { // 1M
				return oneDecimalFormat.format((amountAsDouble / 1e6)) + "M";
		}
		if (amountAsDouble >= 1e4) { // 1K
				return (int) (amountAsDouble / 1e3) + "K";
		}
		if (amountAsDouble >= 1e3) { // 1K
				return oneDecimalFormat.format((amountAsDouble / 1e3)) + "K";
		}
		return "" + number;
	}

	public void resetLogout() {
		boolean wasDisconnected = loggedIn && (pktType != 109) && (anInt1011 <= 0);
		try {
			if (socketStream != null) {
				socketStream.close();
			}
		} catch (Exception _ex) {
		}
		socketStream = null;
		loggedIn = false;
		if (wasDisconnected && (ClientSettings.ANDROID || (myUsername != null && myUsername.length() > 0))) {
			loginMessage1 = "You were disconnected from the server.";
			loginMessage2 = "Press Login to reconnect.";
			loginScreenState = 2;
			loginScreenCursorPos = (myPassword != null && myPassword.length() > 0) ? 1 : 0;
		} else {
			loginScreenState = 0;
		}
		// myUsername = "";
		// myPassword = "";
		unlinkMRUNodes();
		worldController.initToNull();
		for (int i = 0; i < 4; i++) {
			aClass11Array1230[i].reset();
		}

		System.gc();
		stopMidi();
		currentSong = -1;
		nextSong = -1;
		previousSong = 0;
		requestSongWithFade(10, musicVolume, false, 0);
	}

	public void selectDefaultDesignerKits() {
		aBoolean1031 = true;
		for (int bodyPart = 0; bodyPart < 7; bodyPart++) {
			designerKitIds[bodyPart] = -1;
			for (int kitIndex = 0; kitIndex < IDK.length; kitIndex++) {
				if (IDK.cache[kitIndex].notSelectable || IDK.cache[kitIndex].bodyPartIndex != bodyPart + (designerMale ? 0 : 7)) {
					continue;
				}
				designerKitIds[bodyPart] = kitIndex;
				break;
			}

		}

	}

	public void addNewNpcs(int packetLength, Stream stream) {
		while (stream.bitOffset + 21 < packetLength * 8) {
			int npcIndex = stream.readBitsMsbFirst(14);
			if (npcIndex == 16383) {
				break;
			}
			if (npcArray[npcIndex] == null) {
				npcArray[npcIndex] = new NPC();
			}
			NPC npc = npcArray[npcIndex];
			npcIndices[npcCount++] = npcIndex;
			npc.anInt1537 = loopCycle;
			int yOffset = stream.readBitsMsbFirst(5);
			if (yOffset > 15) {
				yOffset -= 32;
			}
			int xOffset = stream.readBitsMsbFirst(5);
			if (xOffset > 15) {
				xOffset -= 32;
			}
			int snapToTile = stream.readBitsMsbFirst(1);
			npc.desc = EntityDef.forID(stream.readBitsMsbFirst(ClientSettings.NPC_BITS));
			int hasUpdateMask = stream.readBitsMsbFirst(1);
			if (hasUpdateMask == 1) {
				maskUpdateIndices[maskUpdateCount++] = npcIndex;
			}
			npc.tileSize = npc.desc.tileSize;
			npc.turnSpeed = npc.desc.turnSpeed;
			npc.walkAnimationId = npc.desc.walkAnimationId;
			npc.reverseWalkAnimationId = npc.desc.reverseWalkAnimationId;
			npc.sidestepNegativeAnimationId = npc.desc.sidestepNegativeAnimationId;
			npc.sidestepPositiveAnimationId = npc.desc.sidestepPositiveAnimationId;
			npc.idleAnimationId = npc.desc.idleAnimationId;
			npc.setPos(myPlayer.smallX[0] + xOffset, myPlayer.smallY[0] + yOffset, snapToTile == 1);
		}
		stream.finishBitReading();
	}

	public void processGameLoop() {
		if (rsAlreadyLoaded || loadingError || genericLoadingError) {
			return;
		}
		loopCycle++;
		if (!loggedIn) {
			processLoginScreenInput();
		} else {
			mainGameProcessor();
		}
		processOnDemandQueue();
		updateMusic();
	}

	public void addPlayersToScene(boolean localPlayerOnly) {
		if (myPlayer.x >> 7 == destX && myPlayer.y >> 7 == destY) {
			destX = 0;
		}
		int playerLimit = playerCount;
		if (localPlayerOnly) {
			playerLimit = 1;
		}
		for (int playerSlot = 0; playerSlot < playerLimit; playerSlot++) {
			Player player;
			int uid;
			if (localPlayerOnly) {
				player = myPlayer;
				uid = myPlayerIndex << 14;
			} else {
				player = playerArray[playerIndices[playerSlot]];
				uid = playerIndices[playerSlot] << 14;
			}
			if (player == null || !player.isVisible()) {
				continue;
			}
			player.staticModelOnly = (lowMem && playerCount > 50 || playerCount > 200) && !localPlayerOnly && player.movementAnimationId == player.idleAnimationId;
			int tileX = player.x >> 7;
			int tileY = player.y >> 7;
			if (tileX < 0 || tileX >= 104 || tileY < 0 || tileY >= 104) {
				continue;
			}
			if (player.attachedModel != null && loopCycle >= player.attachedModelStartCycle && loopCycle < player.attachedModelEndCycle) {
				player.staticModelOnly = false;
				player.groundHeight = interpolatedGroundHeight(plane, player.y, player.x);
				worldController.addEntityInTileBounds(plane, player.y, player, player.facingDirection, player.attachedMaxTileY, player.x, player.groundHeight, player.attachedMinTileX, player.attachedMaxTileX, uid, player.attachedMinTileY);
				continue;
			}
			if ((player.x & 0x7f) == 64 && (player.y & 0x7f) == 64) {
				if (anIntArrayArray929[tileX][tileY] == anInt1265) {
					continue;
				}
				anIntArrayArray929[tileX][tileY] = anInt1265;
			}
			player.groundHeight = interpolatedGroundHeight(plane, player.y, player.x);
			worldController.addEntity(plane, player.facingDirection, player.groundHeight, uid, player.y, 60, player.x, player, player.aBoolean1541);
		}

	}

	public boolean promptUserForInput(RSInterface class9) {
		int j = class9.contentType;
		if (anInt900 == 2) {
			if (j == 201) {
				inputTaken = true;
				inputDialogState = 0;
				messagePromptRaised = true;
				promptInput = "";
				friendsListAction = 1;
				aString1121 = "Enter name of friend to add to list";
				if (ClientSettings.ANDROID) {
					AndroidHooks.showKeyboard();
				}
			}
			if (j == 202) {
				inputTaken = true;
				inputDialogState = 0;
				messagePromptRaised = true;
				promptInput = "";
				friendsListAction = 2;
				aString1121 = "Enter name of friend to delete from list";
				if (ClientSettings.ANDROID) {
					AndroidHooks.showKeyboard();
				}
			}
		}
		if (j == 205) {
			anInt1011 = 250;
			return true;
		}
		if (j == 501) {
			inputTaken = true;
			inputDialogState = 0;
			messagePromptRaised = true;
			promptInput = "";
			friendsListAction = 4;
			aString1121 = "Enter name of player to add to list";
			if (ClientSettings.ANDROID) {
				AndroidHooks.showKeyboard();
			}
		}
		if (j == 502) {
			inputTaken = true;
			inputDialogState = 0;
			messagePromptRaised = true;
			promptInput = "";
			friendsListAction = 5;
			aString1121 = "Enter name of player to delete from list";
			if (ClientSettings.ANDROID) {
				AndroidHooks.showKeyboard();
			}
		}
		if (j >= 300 && j <= 313) {
			int k = (j - 300) / 2;
			int j1 = j & 1;
			int i2 = designerKitIds[k];
			if (i2 != -1) {
				do {
					if (j1 == 0 && --i2 < 0) {
						i2 = IDK.length - 1;
					}
					if (j1 == 1 && ++i2 >= IDK.length) {
						i2 = 0;
					}
				} while (IDK.cache[i2].notSelectable || IDK.cache[i2].bodyPartIndex != k + (designerMale ? 0 : 7));
				designerKitIds[k] = i2;
				aBoolean1031 = true;
			}
		}
		if (j >= 314 && j <= 323) {
			int l = (j - 314) / 2;
			int k1 = j & 1;
			int j2 = anIntArray990[l];
			if (k1 == 0 && --j2 < 0) {
				j2 = anIntArrayArray1003[l].length - 1;
			}
			if (k1 == 1 && ++j2 >= anIntArrayArray1003[l].length) {
				j2 = 0;
			}
			anIntArray990[l] = j2;
			aBoolean1031 = true;
		}
		if (j == 324 && !designerMale) {
			designerMale = true;
			selectDefaultDesignerKits();
		}
		if (j == 325 && designerMale) {
			designerMale = false;
			selectDefaultDesignerKits();
		}
		if (j == 326) {
			stream.writeEncryptedOpcode(101);
			stream.writeByte(designerMale ? 0 : 1);
			for (int i1 = 0; i1 < 7; i1++) {
				stream.writeByte(designerKitIds[i1]);
			}

			for (int l1 = 0; l1 < 5; l1++) {
				stream.writeByte(anIntArray990[l1]);
			}

			return true;
		}
		if (j == 613) {
			canMute = !canMute;
		}
		if (j >= 601 && j <= 612) {
			closeOpenInterfaces();
			if (reportAbuseInput.length() > 0) {
				stream.writeEncryptedOpcode(218);
				stream.writeLongBigEndian(TextClass.encodeBase37Name(reportAbuseInput));
				stream.writeByte(j - 601);
				stream.writeByte(canMute ? 1 : 0);
			}
		}
		return false;
	}

	public void decodePlayerUpdateMasks(Stream stream) {
		for (int slot = 0; slot < maskUpdateCount; slot++) {
			int playerIndex = maskUpdateIndices[slot];
			Player player = playerArray[playerIndex];
			int updateMask = stream.readUnsignedByte();
			if ((updateMask & 0x40) != 0) {
				updateMask += stream.readUnsignedByte() << 8;
			}
			decodePlayerUpdateMask(updateMask, playerIndex, stream, player);
		}

	}

	public void drawMinimapTileObjects(int tileY, int wallColour, int tileX, int interactiveWallColour, int tilePlane) {
		int uid = worldController.getWallUid(tilePlane, tileX, tileY);
		if (uid != 0) {
			int wallInfo = worldController.getObjectInfo(tilePlane, tileX, tileY, uid);
			int rotation = wallInfo >> 6 & 3;
			int shape = wallInfo & 0x1f;
			int colour = wallColour;
			if (uid > 0) {
				colour = interactiveWallColour;
			}
			int pixels[] = minimapSprite.pixels;
			int pixelIndex = 24624 + tileX * 4 + (103 - tileY) * 512 * 4;
			int objectId = uid >> 14 & 0x7fff;
			ObjectDef objectDef = ObjectDef.forID(objectId);
			if (objectDef.mapSceneId != -1) {
				Background mapScene = mapScenes[objectDef.mapSceneId];
				if (mapScene != null) {
					int sceneOffsetX = (objectDef.sizeX * 4 - mapScene.imageWidth) / 2;
					int sceneOffsetY = (objectDef.sizeY * 4 - mapScene.imageHeight) / 2;
					mapScene.drawNonzeroIndexed(48 + tileX * 4 + sceneOffsetX, 48 + (104 - tileY - objectDef.sizeY) * 4 + sceneOffsetY);
				}
			} else {
				if (shape == 0 || shape == 2) {
					if (rotation == 0) {
						pixels[pixelIndex] = colour;
						pixels[pixelIndex + 512] = colour;
						pixels[pixelIndex + 1024] = colour;
						pixels[pixelIndex + 1536] = colour;
					} else if (rotation == 1) {
						pixels[pixelIndex] = colour;
						pixels[pixelIndex + 1] = colour;
						pixels[pixelIndex + 2] = colour;
						pixels[pixelIndex + 3] = colour;
					} else if (rotation == 2) {
						pixels[pixelIndex + 3] = colour;
						pixels[pixelIndex + 3 + 512] = colour;
						pixels[pixelIndex + 3 + 1024] = colour;
						pixels[pixelIndex + 3 + 1536] = colour;
					} else if (rotation == 3) {
						pixels[pixelIndex + 1536] = colour;
						pixels[pixelIndex + 1536 + 1] = colour;
						pixels[pixelIndex + 1536 + 2] = colour;
						pixels[pixelIndex + 1536 + 3] = colour;
					}
				}
				if (shape == 3) {
					if (rotation == 0) {
						pixels[pixelIndex] = colour;
					} else if (rotation == 1) {
						pixels[pixelIndex + 3] = colour;
					} else if (rotation == 2) {
						pixels[pixelIndex + 3 + 1536] = colour;
					} else if (rotation == 3) {
						pixels[pixelIndex + 1536] = colour;
					}
				}
				if (shape == 2) {
					if (rotation == 3) {
						pixels[pixelIndex] = colour;
						pixels[pixelIndex + 512] = colour;
						pixels[pixelIndex + 1024] = colour;
						pixels[pixelIndex + 1536] = colour;
					} else if (rotation == 0) {
						pixels[pixelIndex] = colour;
						pixels[pixelIndex + 1] = colour;
						pixels[pixelIndex + 2] = colour;
						pixels[pixelIndex + 3] = colour;
					} else if (rotation == 1) {
						pixels[pixelIndex + 3] = colour;
						pixels[pixelIndex + 3 + 512] = colour;
						pixels[pixelIndex + 3 + 1024] = colour;
						pixels[pixelIndex + 3 + 1536] = colour;
					} else if (rotation == 2) {
						pixels[pixelIndex + 1536] = colour;
						pixels[pixelIndex + 1536 + 1] = colour;
						pixels[pixelIndex + 1536 + 2] = colour;
						pixels[pixelIndex + 1536 + 3] = colour;
					}
				}
			}
		}
		uid = worldController.getSceneObjectUid(tilePlane, tileX, tileY);
		if (uid != 0) {
			int info = worldController.getObjectInfo(tilePlane, tileX, tileY, uid);
			int rotation = info >> 6 & 3;
			int shape = info & 0x1f;
			int objectId = uid >> 14 & 0x7fff;
			ObjectDef objectDef = ObjectDef.forID(objectId);
			if (objectDef.mapSceneId != -1) {
				Background mapScene = mapScenes[objectDef.mapSceneId];
				if (mapScene != null) {
					int sceneOffsetX = (objectDef.sizeX * 4 - mapScene.imageWidth) / 2;
					int sceneOffsetY = (objectDef.sizeY * 4 - mapScene.imageHeight) / 2;
					mapScene.drawNonzeroIndexed(48 + tileX * 4 + sceneOffsetX, 48 + (104 - tileY - objectDef.sizeY) * 4 + sceneOffsetY);
				}
			} else if (shape == 9) {
				int colour = 0xeeeeee;
				if (uid > 0) {
					colour = 0xee0000;
				}
				int pixels[] = minimapSprite.pixels;
				int pixelIndex = 24624 + tileX * 4 + (103 - tileY) * 512 * 4;
				if (rotation == 0 || rotation == 2) {
					pixels[pixelIndex + 1536] = colour;
					pixels[pixelIndex + 1024 + 1] = colour;
					pixels[pixelIndex + 512 + 2] = colour;
					pixels[pixelIndex + 3] = colour;
				} else {
					pixels[pixelIndex] = colour;
					pixels[pixelIndex + 512 + 1] = colour;
					pixels[pixelIndex + 1024 + 2] = colour;
					pixels[pixelIndex + 1536 + 3] = colour;
				}
			}
		}
		uid = worldController.getGroundDecorationUid(tilePlane, tileX, tileY);
		if (uid != 0) {
			int objectId = uid >> 14 & 0x7fff;
			ObjectDef objectDef = ObjectDef.forID(objectId);
			if (objectDef.mapSceneId != -1) {
				Background mapScene = mapScenes[objectDef.mapSceneId];
				if (mapScene != null) {
					int sceneOffsetX = (objectDef.sizeX * 4 - mapScene.imageWidth) / 2;
					int sceneOffsetY = (objectDef.sizeY * 4 - mapScene.imageHeight) / 2;
					mapScene.drawNonzeroIndexed(48 + tileX * 4 + sceneOffsetX, 48 + (104 - tileY - objectDef.sizeY) * 4 + sceneOffsetY);
				}
			}
		}
	}

	public void loadTitleScreen() {
		aBackground_966 = new Background(titleStreamLoader, "titlebox", 0);
		aBackground_967 = new Background(titleStreamLoader, "titlebutton", 0);
		aBackgroundArray1152s = new Background[12];
		int j = 0;
		try {
			j = Integer.parseInt(getParameter("fl_icon"));
		} catch (Exception _ex) {
		}
		if (j == 0) {
			for (int k = 0; k < 12; k++) {
				aBackgroundArray1152s[k] = new Background(titleStreamLoader, "runes", k);
			}

		} else {
			for (int l = 0; l < 12; l++) {
				aBackgroundArray1152s[l] = new Background(titleStreamLoader, "runes", 12 + (l & 3));
			}

		}
		aClass30_Sub2_Sub1_Sub1_1201 = new Sprite(128, 265);
		aClass30_Sub2_Sub1_Sub1_1202 = new Sprite(128, 265);
		System.arraycopy(aRSImageProducer_1110.pixels, 0, aClass30_Sub2_Sub1_Sub1_1201.pixels, 0, 33920);

		System.arraycopy(aRSImageProducer_1111.pixels, 0, aClass30_Sub2_Sub1_Sub1_1202.pixels, 0, 33920);

		anIntArray851 = new int[256];
		for (int k1 = 0; k1 < 64; k1++) {
			anIntArray851[k1] = k1 * 0x40000;
		}

		for (int l1 = 0; l1 < 64; l1++) {
			anIntArray851[l1 + 64] = 0xff0000 + 1024 * l1;
		}

		for (int i2 = 0; i2 < 64; i2++) {
			anIntArray851[i2 + 128] = 0xffff00 + 4 * i2;
		}

		for (int j2 = 0; j2 < 64; j2++) {
			anIntArray851[j2 + 192] = 0xffffff;
		}

		anIntArray852 = new int[256];
		for (int k2 = 0; k2 < 64; k2++) {
			anIntArray852[k2] = k2 * 1024;
		}

		for (int l2 = 0; l2 < 64; l2++) {
			anIntArray852[l2 + 64] = 0x00ff00 + 4 * l2;
		}

		for (int i3 = 0; i3 < 64; i3++) {
			anIntArray852[i3 + 128] = 0x00ffff + 0x40000 * i3;
		}

		for (int j3 = 0; j3 < 64; j3++) {
			anIntArray852[j3 + 192] = 0xffffff;
		}

		anIntArray853 = new int[256];
		for (int k3 = 0; k3 < 64; k3++) {
			anIntArray853[k3] = k3 * 4;
		}

		for (int l3 = 0; l3 < 64; l3++) {
			anIntArray853[l3 + 64] = 255 + 0x40000 * l3;
		}

		for (int i4 = 0; i4 < 64; i4++) {
			anIntArray853[i4 + 128] = 0xff00ff + 1024 * i4;
		}

		for (int j4 = 0; j4 < 64; j4++) {
			anIntArray853[j4 + 192] = 0xffffff;
		}

		anIntArray850 = new int[256];
		anIntArray1190 = new int[32768];
		anIntArray1191 = new int[32768];
		randomizeBackground(null);
		anIntArray828 = new int[32768];
		anIntArray829 = new int[32768];
		drawLoadingText(10, "Connecting to fileserver");
		if (!aBoolean831) {
			drawFlames = true;
			aBoolean831 = true;
			startRunnable(this, 2);
		}
	}

	public static void setHighMem() {
		WorldController.lowMem = false;
		Texture.lowMem = false;
		lowMem = false;
		ObjectManager.lowMem = false;
		ObjectDef.lowMem = false;
	}

	public void loadingStages() {
		if (lowMem && loadingStage == 2 && ObjectManager.scenePlaneFilter != plane) {
			drawTextOnScreen(null, "Loading - please wait.");
			loadingStage = 1;
			aLong824 = System.currentTimeMillis();
		}
		if (loadingStage == 1) {
			int j = tryFinishRegionLoad();
			if (j != 0 && System.currentTimeMillis() - aLong824 > 0x57e40L) {
				Signlink.reporterror(myUsername + " glcfb " + aLong1215 + "," + j + "," + lowMem + "," + cacheStores[0] + "," + onDemandFetcher.getNodeCount() + "," + plane + "," + anInt1069 + "," + anInt1070);
				aLong824 = System.currentTimeMillis();
			}
		}
		if (loadingStage == 2 && plane != minimapBuiltPlane) {
			minimapBuiltPlane = plane;
			buildMinimapImage(plane);
		}
	}

	public int tryFinishRegionLoad() {
		for (int regionIndex = 0; regionIndex < regionTerrainData.length; regionIndex++) {
			if (regionTerrainData[regionIndex] == null && regionTerrainFileIds[regionIndex] != -1) {
				return -1;
			}
			if (regionObjectData[regionIndex] == null && regionObjectFileIds[regionIndex] != -1) {
				return -2;
			}
		}

		boolean allObjectModelsReady = true;
		for (int regionIndex = 0; regionIndex < regionTerrainData.length; regionIndex++) {
			byte objectData[] = regionObjectData[regionIndex];
			if (objectData != null) {
				int regionOffsetX = (regionKeys[regionIndex] >> 8) * 64 - baseX;
				int regionOffsetY = (regionKeys[regionIndex] & 0xff) * 64 - baseY;
				if (aBoolean1159) {
					regionOffsetX = 10;
					regionOffsetY = 10;
				}
				allObjectModelsReady &= ObjectManager.method189(regionOffsetX, objectData, regionOffsetY);
			}
		}

		if (!allObjectModelsReady) {
			return -3;
		}
		if (aBoolean1080) {
			return -4;
		} else {
			loadingStage = 2;
			ObjectManager.scenePlaneFilter = plane;
			rebuildScene();
			stream.writeEncryptedOpcode(121);
			return 0;
		}
	}

	public void updateProjectiles() {
		for (Projectile projectile = (Projectile) projectiles.firstFromTail(); projectile != null; projectile = (Projectile) projectiles.nextTowardHead()) {
			if (projectile.plane != plane || loopCycle > projectile.endCycle) {
				projectile.unlink();
			} else if (loopCycle >= projectile.startCycle) {
				if (projectile.target > 0) {
					NPC npc = npcArray[projectile.target - 1];
					if (npc != null && npc.x >= 0 && npc.x < 13312 && npc.y >= 0 && npc.y < 13312) {
						projectile.retarget(loopCycle, npc.y, interpolatedGroundHeight(projectile.plane, npc.y, npc.x) - projectile.targetHeightOffset, npc.x);
					}
				}
				if (projectile.target < 0) {
					int playerIndex = -projectile.target - 1;
					Player player;
					if (playerIndex == unknownInt10) {
						player = myPlayer;
					} else {
						player = playerArray[playerIndex];
					}
					if (player != null && player.x >= 0 && player.x < 13312 && player.y >= 0 && player.y < 13312) {
						projectile.retarget(loopCycle, player.y, interpolatedGroundHeight(projectile.plane, player.y, player.x) - projectile.targetHeightOffset, player.x);
					}
				}
				projectile.advance(cyclesSinceDraw);
				worldController.addEntity(plane, projectile.yaw, (int) projectile.positionHeight, -1, (int) projectile.positionY, 60, (int) projectile.positionX, projectile, false);
			}
		}

	}


	public void drawLogo() {
		byte abyte0[] = titleStreamLoader.getDataForName("title.dat");
		Sprite sprite = new Sprite(abyte0, this);
		aRSImageProducer_1110.setAsDrawingArea();
		sprite.drawOpaque(0, 0);
		aRSImageProducer_1111.setAsDrawingArea();
		sprite.drawOpaque(-637, 0);
		aRSImageProducer_1107.setAsDrawingArea();
		sprite.drawOpaque(-128, 0);
		aRSImageProducer_1108.setAsDrawingArea();
		sprite.drawOpaque(-202, -371);
		aRSImageProducer_1109.setAsDrawingArea();
		sprite.drawOpaque(-202, -171);
		aRSImageProducer_1112.setAsDrawingArea();
		sprite.drawOpaque(0, -265);
		aRSImageProducer_1113.setAsDrawingArea();
		sprite.drawOpaque(-562, -265);
		aRSImageProducer_1114.setAsDrawingArea();
		sprite.drawOpaque(-128, -171);
		aRSImageProducer_1115.setAsDrawingArea();
		sprite.drawOpaque(-562, -171);
		int ai[] = new int[sprite.width];
		for (int j = 0; j < sprite.height; j++) {
			for (int k = 0; k < sprite.width; k++) {
				ai[k] = sprite.pixels[sprite.width - k - 1 + sprite.width * j];
			}

			System.arraycopy(ai, 0, sprite.pixels, sprite.width * j, sprite.width);

		}

		aRSImageProducer_1110.setAsDrawingArea();
		sprite.drawOpaque(382, 0);
		aRSImageProducer_1111.setAsDrawingArea();
		sprite.drawOpaque(-255, 0);
		aRSImageProducer_1107.setAsDrawingArea();
		sprite.drawOpaque(254, 0);
		aRSImageProducer_1108.setAsDrawingArea();
		sprite.drawOpaque(180, -371);
		aRSImageProducer_1109.setAsDrawingArea();
		sprite.drawOpaque(180, -171);
		aRSImageProducer_1112.setAsDrawingArea();
		sprite.drawOpaque(382, -265);
		aRSImageProducer_1113.setAsDrawingArea();
		sprite.drawOpaque(-180, -265);
		aRSImageProducer_1114.setAsDrawingArea();
		sprite.drawOpaque(254, -171);
		aRSImageProducer_1115.setAsDrawingArea();
		sprite.drawOpaque(-180, -171);
		sprite = new Sprite(titleStreamLoader, "logo", 0);
		aRSImageProducer_1107.setAsDrawingArea();
		sprite.drawSprite(382 - sprite.width / 2 - 128, 18);
		sprite = null;
		System.gc();

	}

	public void processOnDemandQueue() {
		do {
			OnDemandData onDemandData;
			do {
				onDemandData = onDemandFetcher.getNextNode();
				if (onDemandData == null) {
					return;
				}
				if (onDemandData.dataType == 0) {
					Model.loadModelHeader(onDemandData.buffer, onDemandData.ID);
					if ((onDemandFetcher.getModelIndex(onDemandData.ID) & 0x62) != 0) {
						needDrawTabArea = true;
						if (backDialogID != -1) {
							inputTaken = true;
						}
					}
				}
				if (onDemandData.dataType == 1 && onDemandData.buffer != null) {
					AnimationFrame.decodeFile(onDemandData.buffer);
				}
				if (onDemandData.dataType == 2 && onDemandData.ID == nextSong && onDemandData.buffer != null) {
					musicData = new byte[onDemandData.buffer.length];
					System.arraycopy(onDemandData.buffer, 0, musicData, 0, musicData.length);
					fetchMusic = true;
				}
				if (onDemandData.dataType == 3 && loadingStage == 1) {
					for (int regionIndex = 0; regionIndex < regionTerrainData.length; regionIndex++) {
						if (regionTerrainFileIds[regionIndex] == onDemandData.ID) {
							regionTerrainData[regionIndex] = onDemandData.buffer;
							if (onDemandData.buffer == null) {
								regionTerrainFileIds[regionIndex] = -1;
							}
							break;
						}
						if (regionObjectFileIds[regionIndex] != onDemandData.ID) {
							continue;
						}
						regionObjectData[regionIndex] = onDemandData.buffer;
						if (onDemandData.buffer == null) {
							regionObjectFileIds[regionIndex] = -1;
						}
						break;
					}

				}
			} while (onDemandData.dataType != 93 || !onDemandFetcher.isMapObjectFileId(onDemandData.ID));
			ObjectManager.requestObjectModels(new Stream(onDemandData.buffer), onDemandFetcher);
		} while (true);
	}

	public void calcFlamesPosition() {
		char c = '\u0100';
		for (int j = 10; j < 117; j++) {
			int k = (int) (Math.random() * 100D);
			if (k < 50) {
				anIntArray828[j + (c - 2 << 7)] = 255;
			}
		}
		for (int l = 0; l < 100; l++) {
			int i1 = (int) (Math.random() * 124D) + 2;
			int k1 = (int) (Math.random() * 128D) + 128;
			int k2 = i1 + (k1 << 7);
			anIntArray828[k2] = 192;
		}

		for (int j1 = 1; j1 < c - 1; j1++) {
			for (int l1 = 1; l1 < 127; l1++) {
				int l2 = l1 + (j1 << 7);
				anIntArray829[l2] = (anIntArray828[l2 - 1] + anIntArray828[l2 + 1] + anIntArray828[l2 - 128] + anIntArray828[l2 + 128]) / 4;
			}

		}

		anInt1275 += 128;
		if (anInt1275 > anIntArray1190.length) {
			anInt1275 -= anIntArray1190.length;
			int i2 = (int) (Math.random() * 12D);
			randomizeBackground(aBackgroundArray1152s[i2]);
		}
		for (int j2 = 1; j2 < c - 1; j2++) {
			for (int i3 = 1; i3 < 127; i3++) {
				int k3 = i3 + (j2 << 7);
				int i4 = anIntArray829[k3 + 128] - anIntArray1190[k3 + anInt1275 & anIntArray1190.length - 1] / 5;
				if (i4 < 0) {
					i4 = 0;
				}
				anIntArray828[k3] = i4;
			}

		}

		System.arraycopy(anIntArray969, 1, anIntArray969, 0, c - 1);

		anIntArray969[c - 1] = (int) (Math.sin((double) loopCycle / 14D) * 16D + Math.sin((double) loopCycle / 15D) * 14D + Math.sin((double) loopCycle / 16D) * 12D);
		if (anInt1040 > 0) {
			anInt1040 -= 4;
		}
		if (anInt1041 > 0) {
			anInt1041 -= 4;
		}
		if (anInt1040 == 0 && anInt1041 == 0) {
			int l3 = (int) (Math.random() * 2000D);
			if (l3 == 0) {
				anInt1040 = 1024;
			}
			if (l3 == 1) {
				anInt1041 = 1024;
			}
		}
	}

	public boolean saveWave(byte abyte0[], int i) {
		return abyte0 == null || Signlink.wavesave(abyte0, i);
	}

	public void resetInterfaceModelAnimations(int interfaceId) {
		RSInterface parentInterface = RSInterface.interfaceCache[interfaceId];
		if (parentInterface == null || parentInterface.children == null)
			return;
		for (int childInterfaceId : parentInterface.children) {
			if (childInterfaceId == -1) {
				break;
			}
			RSInterface childInterface = RSInterface.interfaceCache[childInterfaceId];
			if (childInterface.type == 1) {
				resetInterfaceModelAnimations(childInterface.id);
			}
			childInterface.modelAnimationFrame = 0;
			childInterface.modelAnimationTicks = 0;
		}
	}

	public void drawHeadIcon() {
		if (anInt855 != 2) {
			return;
		}
		projectWorldToScreen((anInt934 - baseX << 7) + anInt937, anInt936 * 2, (anInt935 - baseY << 7) + anInt938);
		if (spriteDrawX > -1 && loopCycle % 20 < 10) {
			headIconsHint[0].drawSprite(spriteDrawX - 12, spriteDrawY - 28);
		}
	}

	public void mainGameProcessor() {
		if (anInt1104 > 1) {
			anInt1104--;
		}
		if (anInt1011 > 0) {
			anInt1011--;
		}
		//TODO: Technically, this loop should be < 5 for authenticity, but until we reduce server inefficiencies
		//(for example killing a bunch of cows results in client lag, 
		//likely from all the items on the ground for example), < 100 is fine. 
		//OSRS uses < 100 and there are no drawbacks from having this < 100.
		for (int j = 0; j < 100; j++) {
			if (!parsePacket()) {
				break;
			}
		}

		if (!loggedIn) {
			return;
		}
		synchronized (mouseDetection.sampleLock) {
			if (flagged) {
				if (super.clickMode3 != 0 || mouseDetection.sampleCount >= 40) {
					stream.writeEncryptedOpcode(45);
					stream.writeByte(0);
					int j2 = stream.currentOffset;
					int j3 = 0;
					for (int j4 = 0; j4 < mouseDetection.sampleCount; j4++) {
						if (j2 - stream.currentOffset >= 240) {
							break;
						}
						j3++;
						int l4 = mouseDetection.sampledMouseYs[j4];
						if (l4 < 0) {
							l4 = 0;
						} else if (l4 > 502) {
							l4 = 502;
						}
						int k5 = mouseDetection.sampledMouseXs[j4];
						if (k5 < 0) {
							k5 = 0;
						} else if (k5 > 764) {
							k5 = 764;
						}
						int i6 = l4 * 765 + k5;
						if (mouseDetection.sampledMouseYs[j4] == -1 && mouseDetection.sampledMouseXs[j4] == -1) {
							k5 = -1;
							l4 = -1;
							i6 = 0x7ffff;
						}
						if (k5 == anInt1237 && l4 == anInt1238) {
							if (anInt1022 < 2047) {
								anInt1022++;
							}
						} else {
							int j6 = k5 - anInt1237;
							anInt1237 = k5;
							int k6 = l4 - anInt1238;
							anInt1238 = l4;
							if (anInt1022 < 8 && j6 >= -32 && j6 <= 31 && k6 >= -32 && k6 <= 31) {
								j6 += 32;
								k6 += 32;
								stream.writeShortBigEndian((anInt1022 << 12) + (j6 << 6) + k6);
								anInt1022 = 0;
							} else if (anInt1022 < 8) {
								stream.writeMediumBigEndian(0x800000 + (anInt1022 << 19) + i6);
								anInt1022 = 0;
							} else {
								stream.writeIntBigEndian(0xc0000000 + (anInt1022 << 19) + i6);
								anInt1022 = 0;
							}
						}
					}

					stream.backpatchBytePayloadLength(stream.currentOffset - j2);
					if (j3 >= mouseDetection.sampleCount) {
						mouseDetection.sampleCount = 0;
					} else {
						mouseDetection.sampleCount -= j3;
						for (int i5 = 0; i5 < mouseDetection.sampleCount; i5++) {
							mouseDetection.sampledMouseXs[i5] = mouseDetection.sampledMouseXs[i5 + j3];
							mouseDetection.sampledMouseYs[i5] = mouseDetection.sampledMouseYs[i5 + j3];
						}

					}
				}
			} else {
				mouseDetection.sampleCount = 0;
			}
		}
		if (super.clickMode3 != 0) {
			long l = (super.saveClickTime - aLong1220) / 50L;
			if (l > 4095L) {
				l = 4095L;
			}
			aLong1220 = super.saveClickTime;
			int k2 = super.saveClickY;
			if (k2 < 0) {
				k2 = 0;
			} else if (k2 > 502) {
				k2 = 502;
			}
			int k3 = super.saveClickX;
			if (k3 < 0) {
				k3 = 0;
			} else if (k3 > 764) {
				k3 = 764;
			}
			int k4 = k2 * 765 + k3;
			int j5 = 0;
			if (super.clickMode3 == 2) {
				j5 = 1;
			}
			int l5 = (int) l;
			stream.writeEncryptedOpcode(241);
			stream.writeIntBigEndian((l5 << 20) + (j5 << 19) + k4);
		}
		if (anInt1016 > 0) {
			anInt1016--;
		}
		if (super.keyArray[1] == 1 || super.keyArray[2] == 1 || super.keyArray[3] == 1 || super.keyArray[4] == 1) {
			aBoolean1017 = true;
		}
		if (aBoolean1017 && anInt1016 <= 0) {
			anInt1016 = 20;
			aBoolean1017 = false;
			stream.writeEncryptedOpcode(86);
			stream.writeShortBigEndian(anInt1184);
			stream.writeShortBigEndianAdd128(minimapInt1);
		}
		if (super.awtFocus && !aBoolean954) {
			aBoolean954 = true;
			stream.writeEncryptedOpcode(3);
			stream.writeByte(1);
		}
		if (!super.awtFocus && aBoolean954) {
			aBoolean954 = false;
			stream.writeEncryptedOpcode(3);
			stream.writeByte(0);
		}
		loadingStages();
		advancePendingSceneChanges();
		drainQueuedSoundsAndAdvanceSongCountdown();
		anInt1009++;
		if (anInt1009 > 750) {
			dropClient();
		}
		advancePlayerEntities();
		advanceNpcEntities();
		decrementOverheadTextTimers();
		cyclesSinceDraw++;
		if (crossType != 0) {
			crossIndex += 20;
			if (crossIndex >= 400) {
				crossType = 0;
			}
		}
		if (atInventoryInterfaceType != 0) {
			atInventoryLoopCycle++;
			if (atInventoryLoopCycle >= 15) {
				if (atInventoryInterfaceType == 2) {
					needDrawTabArea = true;
				}
				if (atInventoryInterfaceType == 3) {
					inputTaken = true;
				}
				atInventoryInterfaceType = 0;
			}
		}
		if (activeInterfaceType != 0) {
			anInt989++;
			if (super.mouseX > anInt1087 + 5 || super.mouseX < anInt1087 - 5 || super.mouseY > anInt1088 + 5 || super.mouseY < anInt1088 - 5) {
				aBoolean1242 = true;
			}
			if (super.clickMode2 == 0) {
				if (activeInterfaceType == 2) {
					needDrawTabArea = true;
				}
				if (activeInterfaceType == 3) {
					inputTaken = true;
				}
				activeInterfaceType = 0;
				if (aBoolean1242 && anInt989 >= 5) {
					lastActiveInvInterface = -1;
					processRightClick();
					if (lastActiveInvInterface == anInt1084 && mouseInvInterfaceIndex != anInt1085) {
						RSInterface class9 = RSInterface.interfaceCache[anInt1084];
						int j1 = 0;
						if (anInt913 == 1 && class9.contentType == 206) {
							j1 = 1;
						}
						if (class9.itemIdsPlusOne[mouseInvInterfaceIndex] <= 0) {
							j1 = 0;
						}
						if (class9.aBoolean235) {
							int l2 = anInt1085;
							int l3 = mouseInvInterfaceIndex;
							class9.itemIdsPlusOne[l3] = class9.itemIdsPlusOne[l2];
							class9.itemQuantities[l3] = class9.itemQuantities[l2];
							class9.itemIdsPlusOne[l2] = -1;
							class9.itemQuantities[l2] = 0;
						} else if (j1 == 1) {
							int i3 = anInt1085;
							for (int i4 = mouseInvInterfaceIndex; i3 != i4;) {
								if (i3 > i4) {
									class9.swapInventoryItems(i3, i3 - 1);
									i3--;
								} else if (i3 < i4) {
									class9.swapInventoryItems(i3, i3 + 1);
									i3++;
								}
							}

						} else {
							class9.swapInventoryItems(anInt1085, mouseInvInterfaceIndex);
						}
						stream.writeEncryptedOpcode(214);
						stream.writeShortLittleEndianAdd128(anInt1084);
						stream.writeNegatedByte(j1);
						stream.writeShortLittleEndianAdd128(anInt1085);
						stream.writeShortLittleEndian(mouseInvInterfaceIndex);
					}
				} else if ((anInt1253 == 1 || menuHasAddFriend(menuActionRow - 1)) && menuActionRow > 2) {
					determineMenuSize();
				} else if (menuActionRow > 0) {
					doAction(menuActionRow - 1);
				}
				atInventoryLoopCycle = 10;
				super.clickMode3 = 0;
			}
		}
		if (WorldController.pickedTileX != -1) {
			int k = WorldController.pickedTileX;
			int k1 = WorldController.pickedTileY;
			boolean flag = doWalkTo(0, 0, 0, 0, myPlayer.smallY[0], 0, 0, k1, myPlayer.smallX[0], true, k);
			WorldController.pickedTileX = -1;
			if (flag) {
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 1;
				crossIndex = 0;
			}
		}
		if (super.clickMode3 == 1 && aString844 != null) {
			aString844 = null;
			inputTaken = true;
			super.clickMode3 = 0;
		}
		if (!processMenuClick()) {
			processMainScreenClick();
			processTabClick();
			processChatModeClick();
		}
		if (super.clickMode2 == 1 || super.clickMode3 == 1) {
			anInt1213++;
		}
		if (loadingStage == 2) {
			updateCameraFollow();
		}
		if (loadingStage == 2 && aBoolean1160) {
			calcCameraPos();
		}
		for (int i1 = 0; i1 < 5; i1++) {
			anIntArray1030[i1]++;
		}

		processKeyboardInput();
		super.idleTime++; //Idle timer nope!
		/*if (super.idleTime > 12000) {
			anInt1011 = 250;
			super.idleTime -= 500;
			stream.writeEncryptedOpcode(202);
		}*/
		anInt988++;
		if (anInt988 > 500) {
			anInt988 = 0;
			int l1 = (int) (Math.random() * 8D);
			if ((l1 & 1) == 1) {
				anInt1278 += anInt1279;
			}
			if ((l1 & 2) == 2) {
				anInt1131 += anInt1132;
			}
			if ((l1 & 4) == 4) {
				anInt896 += anInt897;
			}
		}
		if (anInt1278 < -50) {
			anInt1279 = 2;
		}
		if (anInt1278 > 50) {
			anInt1279 = -2;
		}
		if (anInt1131 < -55) {
			anInt1132 = 2;
		}
		if (anInt1131 > 55) {
			anInt1132 = -2;
		}
		if (anInt896 < -40) {
			anInt897 = 1;
		}
		if (anInt896 > 40) {
			anInt897 = -1;
		}
		anInt1254++;
		if (anInt1254 > 500) {
			anInt1254 = 0;
			int i2 = (int) (Math.random() * 8D);
			if ((i2 & 1) == 1) {
				minimapInt2 += anInt1210;
			}
			if ((i2 & 2) == 2) {
				minimapInt3 += anInt1171;
			}
		}
		if (minimapInt2 < -60) {
			anInt1210 = 2;
		}
		if (minimapInt2 > 60) {
			anInt1210 = -2;
		}
		if (minimapInt3 < -20) {
			anInt1171 = 1;
		}
		if (minimapInt3 > 10) {
			anInt1171 = -1;
		}
		anInt1010++;
		if (anInt1010 > 50) {
			stream.writeEncryptedOpcode(0);
		}
		try {
			if (socketStream != null && stream.currentOffset > 0) {
				socketStream.queueBytes(stream.currentOffset, stream.buffer);
				stream.currentOffset = 0;
				anInt1010 = 0;
			}
		} catch (IOException _ex) {
			dropClient();
		} catch (Exception exception) {
			resetLogout();
		}
	}

	public void resetPendingSceneChangesAfterRegionLoad() {
		Class30_Sub1 pendingSceneChange = (Class30_Sub1) pendingSceneChanges.firstFromTail();
		for (; pendingSceneChange != null; pendingSceneChange = (Class30_Sub1) pendingSceneChanges.nextTowardHead()) {
			if (pendingSceneChange.restoreDelayTicks == -1) {
				pendingSceneChange.placementDelayTicks = 0;
				snapshotOriginalSceneObject(pendingSceneChange);
			} else {
				pendingSceneChange.unlink();
			}
		}

	}

	void drawLoadingText(int i, String s) {
		anInt1079 = i;
		aString1049 = s;
		resetImageProducers();
		if (titleStreamLoader == null) {
			super.drawLoadingText(i, s);
			return;
		}
		aRSImageProducer_1109.setAsDrawingArea();
		char c = '\u0168';
		char c1 = '\310';
		byte byte1 = 20;
		chatTextDrawingArea.drawCenteredLiteralText(0xffffff, "" + ClientSettings.SERVER_NAME + " is loading - please wait...", c1 / 2 - 26 - byte1, c / 2);
		int j = c1 / 2 - 18 - byte1;
		DrawingArea.drawRectangleOutline(j, 34, 0x8c1111, c / 2 - 152, 304);
		DrawingArea.drawRectangleOutline(j + 1, 32, 0, c / 2 - 151, 302);
		DrawingArea.fillRectangle(30, j + 2, 0x8c1111, i * 3, c / 2 - 150);
		DrawingArea.fillRectangle(30, j + 2, 0, 300 - i * 3, c / 2 - 150 + i * 3);
		chatTextDrawingArea.drawCenteredLiteralText(0xffffff, s, c1 / 2 + 5 - byte1, c / 2);
		aRSImageProducer_1109.drawGraphics(171, super.graphics, 202);
		if (welcomeScreenRaised) {
			welcomeScreenRaised = false;
			if (!aBoolean831) {
				aRSImageProducer_1110.drawGraphics(0, super.graphics, 0);
				aRSImageProducer_1111.drawGraphics(0, super.graphics, 637);
			}
			aRSImageProducer_1107.drawGraphics(0, super.graphics, 128);
			aRSImageProducer_1108.drawGraphics(371, super.graphics, 202);
			aRSImageProducer_1112.drawGraphics(265, super.graphics, 0);
			aRSImageProducer_1113.drawGraphics(265, super.graphics, 562);
			aRSImageProducer_1114.drawGraphics(171, super.graphics, 128);
			aRSImageProducer_1115.drawGraphics(171, super.graphics, 562);
		}
	}

	public void handleScrollbarInput(int leftX, int viewportHeight, int pointerX, int pointerY, RSInterface component, int topY, boolean redrawTabArea, int contentHeight) {
		int dragMargin;
		if (scrollbarDragging) {
			dragMargin = 32;
		} else {
			dragMargin = 0;
		}
		scrollbarDragging = false;
		if (pointerX >= leftX && pointerX < leftX + 16 && pointerY >= topY && pointerY < topY + 16) {
			component.scrollPosition -= anInt1213 * 4;
			if (redrawTabArea) {
				needDrawTabArea = true;
			}
		} else if (pointerX >= leftX && pointerX < leftX + 16 && pointerY >= topY + viewportHeight - 16 && pointerY < topY + viewportHeight) {
			component.scrollPosition += anInt1213 * 4;
			if (redrawTabArea) {
				needDrawTabArea = true;
			}
		} else if (pointerX >= leftX - dragMargin && pointerX < leftX + 16 + dragMargin && pointerY >= topY + 16 && pointerY < topY + viewportHeight - 16 && anInt1213 > 0) {
			int thumbHeight = (viewportHeight - 32) * viewportHeight / contentHeight;
			if (thumbHeight < 8) {
				thumbHeight = 8;
			}
			int thumbTopOffset = pointerY - topY - 16 - thumbHeight / 2;
			int thumbTravel = viewportHeight - 32 - thumbHeight;
			component.scrollPosition = (contentHeight - viewportHeight) * thumbTopOffset / thumbTravel;
			if (redrawTabArea) {
				needDrawTabArea = true;
			}
			scrollbarDragging = true;
		}
	}

	public boolean walkToSceneObject(int sceneUid, int targetTileY, int targetTileX) {
		int objectId = sceneUid >> 14 & 0x7fff;
		int sceneConfig = worldController.getObjectInfo(plane, targetTileX, targetTileY, sceneUid);
		if (sceneConfig == -1) {
			return false;
		}
		int objectShape = sceneConfig & 0x1f;
		int objectOrientation = sceneConfig >> 6 & 3;
		if (objectShape == 10 || objectShape == 11 || objectShape == 22) {
			ObjectDef definition = ObjectDef.forID(objectId);
			int orientedSizeX;
			int orientedSizeY;
			if (objectOrientation == 0 || objectOrientation == 2) {
				orientedSizeX = definition.sizeX;
				orientedSizeY = definition.sizeY;
			} else {
				orientedSizeX = definition.sizeY;
				orientedSizeY = definition.sizeX;
			}
			int rotatedAccessMask = definition.blockedSidesMask;
			if (objectOrientation != 0) {
				rotatedAccessMask = (rotatedAccessMask << objectOrientation & 0xf) + (rotatedAccessMask >> 4 - objectOrientation);
			}
			doWalkTo(2, 0, orientedSizeY, 0, myPlayer.smallY[0], orientedSizeX, rotatedAccessMask,
					targetTileY, myPlayer.smallX[0], false, targetTileX);
		} else {
			doWalkTo(2, objectOrientation, 0, objectShape + 1, myPlayer.smallY[0], 0, 0,
					targetTileY, myPlayer.smallX[0], false, targetTileX);
		}
		crossX = super.saveClickX;
		crossY = super.saveClickY;
		crossType = 2;
		crossIndex = 0;
		return true;
	}

	public StreamLoader streamLoaderForName(int i, String s, String s1, int j, int k) {
		byte abyte0[] = null;
		int l = 5;
		try {
			if (cacheStores[0] != null) {
				abyte0 = cacheStores[0].readFile(i);
			}
		} catch (Exception _ex) {
		}
		if(abyte0 != null && ClientSettings.CHECK_CRC) {
			aCRC32_930.reset();
			aCRC32_930.update(abyte0);
			int i1 = (int)aCRC32_930.getValue();
			if(i1 != j)
				abyte0 = null;
		}
		if (abyte0 != null) {
			StreamLoader streamLoader = new StreamLoader(abyte0);
			return streamLoader;
		}
		int j1 = 0;
		while (abyte0 == null) {
			String s2 = "Unknown error";
			drawLoadingText(k, "Requesting " + s);
			try {
				int k1 = 0;
				DataInputStream datainputstream = openJagGrabInputStream(s1 + j);
				byte abyte1[] = new byte[6];
				datainputstream.readFully(abyte1, 0, 6);
				Stream stream = new Stream(abyte1);
				stream.currentOffset = 3;
				int i2 = stream.readUnsignedMediumBigEndian() + 6;
				int j2 = 6;
				abyte0 = new byte[i2];
				System.arraycopy(abyte1, 0, abyte0, 0, 6);

				while (j2 < i2) {
					int l2 = i2 - j2;
					if (l2 > 1000) {
						l2 = 1000;
					}
					int j3 = datainputstream.read(abyte0, j2, l2);
					if (j3 < 0) {
						s2 = "Length error: " + j2 + "/" + i2;
						throw new IOException("EOF");
					}
					j2 += j3;
					int k3 = j2 * 100 / i2;
					if (k3 != k1) {
						drawLoadingText(k, "Loading " + s + " - " + k3 + "%");
					}
					k1 = k3;
				}
				datainputstream.close();
				try {
					if (cacheStores[0] != null) {
						cacheStores[0].writeFile(abyte0.length, abyte0, i);
					}
				} catch (Exception _ex) {
					cacheStores[0] = null;
				}

				if(abyte0 != null && ClientSettings.CHECK_CRC)
				{
					aCRC32_930.reset();
					aCRC32_930.update(abyte0);
					int i3 = (int)aCRC32_930.getValue();
					if(i3 != j)
					{
						abyte0 = null;
						j1++;
						s2 = "Checksum error: " + i3;
					}
				}

			} catch (IOException ioexception) {
				if (s2.equals("Unknown error")) {
					s2 = "Connection error";
				}
				abyte0 = null;
			} catch (NullPointerException _ex) {
				s2 = "Null error";
				abyte0 = null;
				if (!Signlink.reporterror) {
					return null;
				}
			} catch (ArrayIndexOutOfBoundsException _ex) {
				s2 = "Bounds error";
				abyte0 = null;
				if (!Signlink.reporterror) {
					return null;
				}
			} catch (Exception _ex) {
				s2 = "Unexpected error";
				abyte0 = null;
				if (!Signlink.reporterror) {
					return null;
				}
			}
			if (abyte0 == null) {
				for (int l1 = l; l1 > 0; l1--) {
					if (j1 >= 3) {
						drawLoadingText(k, "Game updated - please reload page");
						l1 = 10;
					} else {
						drawLoadingText(k, s2 + " - Retrying in " + l1);
					}
					try {
						Thread.sleep(1000L);
					} catch (Exception _ex) {
					}
				}

				l *= 2;
				if (l > 60) {
					l = 60;
				}
				aBoolean872 = !aBoolean872;
			}

		}

		StreamLoader streamLoader_1 = new StreamLoader(abyte0);
		return streamLoader_1;
	}

	public void dropClient() {
		if (anInt1011 > 0) {
			resetLogout();
			return;
		}
		drawTextOnScreen("Please wait - attempting to reestablish", "Connection lost");
		anInt1021 = 0;
		destX = 0;
		RSSocket rsSocket = socketStream;
		loggedIn = false;
		loginFailures = 0;
		login(myUsername, myPassword, true);
		if (!loggedIn) {
			String existingMsg1 = loginMessage1;
			String existingMsg2 = loginMessage2;
			resetLogout();
			if (existingMsg1 != null && !existingMsg1.isEmpty()) {
				loginMessage1 = existingMsg1;
				loginMessage2 = existingMsg2;
			} else {
				loginMessage1 = "You were disconnected from the server.";
				loginMessage2 = "Press Login to reconnect.";
			}
			if (ClientSettings.ANDROID || (myUsername != null && myUsername.length() > 0)) {
				loginScreenState = 2;
				loginScreenCursorPos = (myPassword != null && myPassword.length() > 0) ? 1 : 0;
			}
		}
		try {
			if (rsSocket != null) {
				rsSocket.close();
			}
		} catch (Exception _ex) {
		}
	}
	
	public void drawTextOnScreen(String s, String s1) {
		if (aRSImageProducer_1165 != null) {
			aRSImageProducer_1165.setAsDrawingArea();
			Texture.lineOffsets = chatBoxAreaOffsets;
			int j = 151;
			if (s != null)
				j -= 7;
			aTextDrawingArea_1271.drawCenteredLiteralText(0, s1, j, 257);
			aTextDrawingArea_1271.drawCenteredLiteralText(0xffffff, s1, j - 1, 256);
			j += 15;
			if (s != null) {
				aTextDrawingArea_1271.drawCenteredLiteralText(0, s, j, 257);
				aTextDrawingArea_1271.drawCenteredLiteralText(0xffffff, s, j - 1, 256);
			}
			aRSImageProducer_1165.drawGraphics(4, super.graphics, 4);
			return;
		}
		if (super.fullGameScreen != null) {
			super.fullGameScreen.setAsDrawingArea();
			Texture.lineOffsets = gameScreenOffsets;
			int k = 251;
			char c = '\u012C';
			byte byte0 = 50;
			DrawingArea.fillRectangle(byte0, k - 5 - byte0 / 2, 0, c, 383 - c / 2);
			DrawingArea.drawRectangleOutline(k - 5 - byte0 / 2, byte0, 0xffffff, 383 - c / 2, c);
			if (s != null)
				k -= 7;
			aTextDrawingArea_1271.drawCenteredLiteralText(0, s1, k, 383);
			aTextDrawingArea_1271.drawCenteredLiteralText(0xffffff, s1, k - 1, 382);
			k += 15;
			if (s != null) {
				aTextDrawingArea_1271.drawCenteredLiteralText(0, s, k, 383);
				aTextDrawingArea_1271.drawCenteredLiteralText(0xffffff, s, k - 1, 382);
			}
			super.fullGameScreen.drawGraphics(0, super.graphics, 0);
		}
	}

	public void doAction(int i) {
		if (i < 0) {
			return;
		}
		if (inputDialogState != 0) {
			if (!ClientSettings.ANDROID || (super.saveClickX > 516 || super.saveClickY < 357 || super.saveClickY > 467)) {
				inputDialogState = 0;
				inputTaken = true;
				if (ClientSettings.ANDROID) {
					AndroidHooks.hideKeyboard();
				}
			}
		}
		if (messagePromptRaised) {
			if (ClientSettings.ANDROID && (super.saveClickX > 516 || super.saveClickY < 357 || super.saveClickY > 467)) {
				messagePromptRaised = false;
				inputTaken = true;
				AndroidHooks.hideKeyboard();
			}
		}
		int j = menuActionCmd2[i];
		int k = menuActionCmd3[i];
		int l = menuActionID[i];
		int i1 = menuActionCmd1[i];
		if (l >= 2000) {
			l -= 2000;
		}
		if (l == 696) {
			minimapInt1 = 0;
            anInt1184 = 120;
        }
		if (l == 582) {
			NPC npc = npcArray[i1];
			if (npc != null) {
				doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, npc.smallY[0], myPlayer.smallX[0], false, npc.smallX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				stream.writeEncryptedOpcode(57);
				stream.writeShortBigEndianAdd128(anInt1285);
				stream.writeShortBigEndianAdd128(i1);
				stream.writeShortLittleEndian(anInt1283);
				stream.writeShortBigEndianAdd128(anInt1284);
			}
		}
		if (l == 234) {
			boolean flag1 = doWalkTo(2, 0, 0, 0, myPlayer.smallY[0], 0, 0, k, myPlayer.smallX[0], false, j);
			if (!flag1) {
				flag1 = doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, k, myPlayer.smallX[0], false, j);
			}
			crossX = super.saveClickX;
			crossY = super.saveClickY;
			crossType = 2;
			crossIndex = 0;
			stream.writeEncryptedOpcode(236);
			stream.writeShortLittleEndian(k + baseY);
			stream.writeShortBigEndian(i1);
			stream.writeShortLittleEndian(j + baseX);
		}
		if (l == 62 && walkToSceneObject(i1, k, j)) {
			stream.writeEncryptedOpcode(192);
			stream.writeShortBigEndian(anInt1284);
			stream.writeShortLittleEndian(i1 >> 14 & 0x7fff);
			stream.writeShortLittleEndianAdd128(k + baseY);
			stream.writeShortLittleEndian(anInt1283);
			stream.writeShortLittleEndianAdd128(j + baseX);
			stream.writeShortBigEndian(anInt1285);
		}
		if (l == 511) {
			boolean flag2 = doWalkTo(2, 0, 0, 0, myPlayer.smallY[0], 0, 0, k, myPlayer.smallX[0], false, j);
			if (!flag2) {
				flag2 = doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, k, myPlayer.smallX[0], false, j);
			}
			crossX = super.saveClickX;
			crossY = super.saveClickY;
			crossType = 2;
			crossIndex = 0;
			stream.writeEncryptedOpcode(25);
			stream.writeShortLittleEndian(anInt1284);
			stream.writeShortBigEndianAdd128(anInt1285);
			stream.writeShortBigEndian(i1);
			stream.writeShortBigEndianAdd128(k + baseY);
			stream.writeShortLittleEndianAdd128(anInt1283);
			stream.writeShortBigEndian(j + baseX);
		}
		if (l == 74) {
			stream.writeEncryptedOpcode(122);
			stream.writeShortLittleEndianAdd128(k);
			stream.writeShortBigEndianAdd128(j);
			stream.writeShortLittleEndian(i1);
			atInventoryLoopCycle = 0;
			atInventoryInterface = k;
			atInventoryIndex = j;
			atInventoryInterfaceType = 2;
			if (RSInterface.interfaceCache[k].parentID == openInterfaceID) {
				atInventoryInterfaceType = 1;
			}
			if (RSInterface.interfaceCache[k].parentID == backDialogID) {
				atInventoryInterfaceType = 3;
			}
		}
		if (l == 315) {
			RSInterface class9 = RSInterface.interfaceCache[k];
			boolean flag8 = true;
			if (class9.contentType > 0) {
				flag8 = promptUserForInput(class9);
			}
			if (flag8) {
				stream.writeEncryptedOpcode(185);
				stream.writeShortBigEndian(k);
			}
		}
		if (l == 561) {
			Player player = playerArray[i1];
			if (player != null) {
				doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, player.smallY[0], myPlayer.smallX[0], false, player.smallX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				anInt1188 += i1;
				if (anInt1188 >= 90) {
					stream.writeEncryptedOpcode(136);
					anInt1188 = 0;
				}
				stream.writeEncryptedOpcode(128);
				stream.writeShortBigEndian(i1);
			}
		}
		if (l == 20) {
			NPC class30_sub2_sub4_sub1_sub1_1 = npcArray[i1];
			if (class30_sub2_sub4_sub1_sub1_1 != null) {
				doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, class30_sub2_sub4_sub1_sub1_1.smallY[0], myPlayer.smallX[0], false, class30_sub2_sub4_sub1_sub1_1.smallX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				stream.writeEncryptedOpcode(155);
				stream.writeShortLittleEndian(i1);
			}
		}
		if (l == 779) {
			Player class30_sub2_sub4_sub1_sub2_1 = playerArray[i1];
			if (class30_sub2_sub4_sub1_sub2_1 != null) {
				doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, class30_sub2_sub4_sub1_sub2_1.smallY[0], myPlayer.smallX[0], false, class30_sub2_sub4_sub1_sub2_1.smallX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				stream.writeEncryptedOpcode(153);
				stream.writeShortLittleEndian(i1);
			}
		}
		if (l == 516) {
			if (!menuOpen) {
				worldController.requestTilePick(super.saveClickY - 4, super.saveClickX - 4);
			} else {
				worldController.requestTilePick(k - 4, j - 4);
			}
		}
		if (l == 1062) { //Fifth click
			anInt924 += baseX;
			if (anInt924 >= 113) {
				stream.writeEncryptedOpcode(183);
				stream.writeMediumBigEndian(0xe63271);
				anInt924 = 0;
			}
			walkToSceneObject(i1, k, j);
			stream.writeEncryptedOpcode(228);
			stream.writeShortBigEndianAdd128(i1 >> 14 & 0x7fff);
			stream.writeShortBigEndianAdd128(k + baseY);
			stream.writeShortBigEndian(j + baseX);
		}
		if (l == 679 && !aBoolean1149) {
			stream.writeEncryptedOpcode(40);
			stream.writeShortBigEndian(k);
			aBoolean1149 = true;
		}
		if (l == 431) {
			stream.writeEncryptedOpcode(129);
			stream.writeShortBigEndianAdd128(j);
			stream.writeShortBigEndian(k);
			stream.writeShortBigEndianAdd128(i1);
			atInventoryLoopCycle = 0;
			atInventoryInterface = k;
			atInventoryIndex = j;
			atInventoryInterfaceType = 2;
			if (RSInterface.interfaceCache[k].parentID == openInterfaceID) {
				atInventoryInterfaceType = 1;
			}
			if (RSInterface.interfaceCache[k].parentID == backDialogID) {
				atInventoryInterfaceType = 3;
			}
		}
		if (l == 337 || l == 42 || l == 792 || l == 322) {
			String s = menuActionName[i];
			int k1 = s.indexOf("@whi@");
			if (k1 != -1) {
				long l3 = TextClass.encodeBase37Name(s.substring(k1 + 5).trim());
				if (l == 337) {
					addFriend(l3);
				}
				if (l == 42) {
					addIgnore(l3);
				}
				if (l == 792) {
					delFriend(l3);
				}
				if (l == 322) {
					delIgnore(l3);
				}
			}
		}
		if (l == 53) {
			stream.writeEncryptedOpcode(135);
			stream.writeShortLittleEndian(j);
			stream.writeShortBigEndianAdd128(k);
			stream.writeShortLittleEndian(i1);
			atInventoryLoopCycle = 0;
			atInventoryInterface = k;
			atInventoryIndex = j;
			atInventoryInterfaceType = 2;
			if (RSInterface.interfaceCache[k].parentID == openInterfaceID) {
				atInventoryInterfaceType = 1;
			}
			if (RSInterface.interfaceCache[k].parentID == backDialogID) {
				atInventoryInterfaceType = 3;
			}
		}
		if (l == 539) {
			stream.writeEncryptedOpcode(16);
			stream.writeShortBigEndianAdd128(i1);
			stream.writeShortLittleEndianAdd128(j);
			stream.writeShortLittleEndianAdd128(k);
			atInventoryLoopCycle = 0;
			atInventoryInterface = k;
			atInventoryIndex = j;
			atInventoryInterfaceType = 2;
			if (RSInterface.interfaceCache[k].parentID == openInterfaceID) {
				atInventoryInterfaceType = 1;
			}
			if (RSInterface.interfaceCache[k].parentID == backDialogID) {
				atInventoryInterfaceType = 3;
			}
		}
		if (l == 484 || l == 6) {
			String s1 = menuActionName[i];
			int l1 = s1.indexOf("@whi@");
			if (l1 != -1) {
				// Accept trade/duel from player
				s1 = s1.substring(l1 + 5).trim();
				String s7 = TextClass.formatDisplayName(TextClass.decodeBase37Name(TextClass.encodeBase37Name(s1)));
				boolean flag9 = false;
				for (int j3 = 0; j3 < playerCount; j3++) {
					Player class30_sub2_sub4_sub1_sub2_7 = playerArray[playerIndices[j3]];
					if (class30_sub2_sub4_sub1_sub2_7 == null || class30_sub2_sub4_sub1_sub2_7.name == null || !class30_sub2_sub4_sub1_sub2_7.name.equalsIgnoreCase(s7)) {
						continue;
					}
					doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, class30_sub2_sub4_sub1_sub2_7.smallY[0], myPlayer.smallX[0], false, class30_sub2_sub4_sub1_sub2_7.smallX[0]);
					if (l == 484) {
						stream.writeEncryptedOpcode(139);
						stream.writeShortLittleEndian(playerIndices[j3]);
					}
					if (l == 6) {
						anInt1188 += i1;
						if (anInt1188 >= 90) {
							stream.writeEncryptedOpcode(136);
							anInt1188 = 0;
						}
						stream.writeEncryptedOpcode(128);
						stream.writeShortBigEndian(playerIndices[j3]);
					}
					flag9 = true;
					break;
				}

				if (!flag9) {
					pushMessage("Unable to find " + s7, 0, "");
				}
			}
		}
		if (l == 870) {
			stream.writeEncryptedOpcode(53);
			stream.writeShortBigEndian(j);
			stream.writeShortBigEndianAdd128(anInt1283);
			stream.writeShortLittleEndianAdd128(i1);
			stream.writeShortBigEndian(anInt1284);
			stream.writeShortLittleEndian(anInt1285);
			stream.writeShortBigEndian(k);
			atInventoryLoopCycle = 0;
			atInventoryInterface = k;
			atInventoryIndex = j;
			atInventoryInterfaceType = 2;
			if (RSInterface.interfaceCache[k].parentID == openInterfaceID) {
				atInventoryInterfaceType = 1;
			}
			if (RSInterface.interfaceCache[k].parentID == backDialogID) {
				atInventoryInterfaceType = 3;
			}
		}
		if (l == 847) {
			stream.writeEncryptedOpcode(87);
			stream.writeShortBigEndianAdd128(i1);
			stream.writeShortBigEndian(k);
			stream.writeShortBigEndianAdd128(j);
			atInventoryLoopCycle = 0;
			atInventoryInterface = k;
			atInventoryIndex = j;
			atInventoryInterfaceType = 2;
			if (RSInterface.interfaceCache[k].parentID == openInterfaceID) {
				atInventoryInterfaceType = 1;
			}
			if (RSInterface.interfaceCache[k].parentID == backDialogID) {
				atInventoryInterfaceType = 3;
			}
		}
		if (l == 626) {
			RSInterface class9_1 = RSInterface.interfaceCache[k];
			spellSelected = 1;
			anInt1137 = k;
			spellUsableOn = class9_1.spellUsableOn;
			itemSelected = 0;
			needDrawTabArea = true;
			String s4 = class9_1.selectedActionName;
			if (s4.indexOf(" ") != -1) {
				s4 = s4.substring(0, s4.indexOf(" "));
			}
			String s8 = class9_1.selectedActionName;
			if (s8.indexOf(" ") != -1) {
				s8 = s8.substring(s8.indexOf(" ") + 1);
			}
			spellTooltip = s4 + " " + class9_1.spellName + " " + s8;
			if (spellUsableOn == 16) {
				needDrawTabArea = true;
				tabID = 3;
				tabAreaAltered = true;
			}
			return;
		}
		if (l == 78) {
			stream.writeEncryptedOpcode(117);
			stream.writeShortLittleEndianAdd128(k);
			stream.writeShortLittleEndianAdd128(i1);
			stream.writeShortLittleEndian(j);
			atInventoryLoopCycle = 0;
			atInventoryInterface = k;
			atInventoryIndex = j;
			atInventoryInterfaceType = 2;
			if (RSInterface.interfaceCache[k].parentID == openInterfaceID) {
				atInventoryInterfaceType = 1;
			}
			if (RSInterface.interfaceCache[k].parentID == backDialogID) {
				atInventoryInterfaceType = 3;
			}
		}
		if (l == 27) {
			Player class30_sub2_sub4_sub1_sub2_2 = playerArray[i1];
			if (class30_sub2_sub4_sub1_sub2_2 != null) {
				doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, class30_sub2_sub4_sub1_sub2_2.smallY[0], myPlayer.smallX[0], false, class30_sub2_sub4_sub1_sub2_2.smallX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				anInt986 += i1;
				if (anInt986 >= 54) {
					stream.writeEncryptedOpcode(189);
					stream.writeByte(234);
					anInt986 = 0;
				}
				stream.writeEncryptedOpcode(73);
				stream.writeShortLittleEndian(i1);
			}
		}
		if (l == 213) {
			boolean flag3 = doWalkTo(2, 0, 0, 0, myPlayer.smallY[0], 0, 0, k, myPlayer.smallX[0], false, j);
			if (!flag3) {
				flag3 = doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, k, myPlayer.smallX[0], false, j);
			}
			crossX = super.saveClickX;
			crossY = super.saveClickY;
			crossType = 2;
			crossIndex = 0;
			stream.writeEncryptedOpcode(79);
			stream.writeShortLittleEndian(k + baseY);
			stream.writeShortBigEndian(i1);
			stream.writeShortBigEndianAdd128(j + baseX);
		}
		if (l == 632) {
			stream.writeEncryptedOpcode(145);
			stream.writeShortBigEndianAdd128(k);
			stream.writeShortBigEndianAdd128(j);
			stream.writeShortBigEndianAdd128(i1);
			atInventoryLoopCycle = 0;
			atInventoryInterface = k;
			atInventoryIndex = j;
			atInventoryInterfaceType = 2;
			if (RSInterface.interfaceCache[k].parentID == openInterfaceID) {
				atInventoryInterfaceType = 1;
			}
			if (RSInterface.interfaceCache[k].parentID == backDialogID) {
				atInventoryInterfaceType = 3;
			}
		}
		if (l == 493) {
			stream.writeEncryptedOpcode(75);
			stream.writeShortLittleEndianAdd128(k);
			stream.writeShortLittleEndian(j);
			stream.writeShortBigEndianAdd128(i1);
			atInventoryLoopCycle = 0;
			atInventoryInterface = k;
			atInventoryIndex = j;
			atInventoryInterfaceType = 2;
			if (RSInterface.interfaceCache[k].parentID == openInterfaceID) {
				atInventoryInterfaceType = 1;
			}
			if (RSInterface.interfaceCache[k].parentID == backDialogID) {
				atInventoryInterfaceType = 3;
			}
		}
		if (l == 652) {
			boolean flag4 = doWalkTo(2, 0, 0, 0, myPlayer.smallY[0], 0, 0, k, myPlayer.smallX[0], false, j);
			if (!flag4) {
				flag4 = doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, k, myPlayer.smallX[0], false, j);
			}
			crossX = super.saveClickX;
			crossY = super.saveClickY;
			crossType = 2;
			crossIndex = 0;
			stream.writeEncryptedOpcode(156);
			stream.writeShortBigEndianAdd128(j + baseX);
			stream.writeShortLittleEndian(k + baseY);
			stream.writeShortLittleEndianAdd128(i1);
		}
		if (l == 94) {
			boolean flag5 = doWalkTo(2, 0, 0, 0, myPlayer.smallY[0], 0, 0, k, myPlayer.smallX[0], false, j);
			if (!flag5) {
				flag5 = doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, k, myPlayer.smallX[0], false, j);
			}
			crossX = super.saveClickX;
			crossY = super.saveClickY;
			crossType = 2;
			crossIndex = 0;
			stream.writeEncryptedOpcode(181);
			stream.writeShortLittleEndian(k + baseY);
			stream.writeShortBigEndian(i1);
			stream.writeShortLittleEndian(j + baseX);
			stream.writeShortBigEndianAdd128(anInt1137);
		}
		if (l == 646) {
			stream.writeEncryptedOpcode(185);
			stream.writeShortBigEndian(k);
			RSInterface class9_2 = RSInterface.interfaceCache[k];
			if (class9_2.valueIndexArray != null && class9_2.valueIndexArray[0][0] == 5) {
				int i2 = class9_2.valueIndexArray[0][1];
				if (variousSettings[i2] != class9_2.conditionValues[0]) {
					variousSettings[i2] = class9_2.conditionValues[0];
					method33(i2);
					needDrawTabArea = true;
				}
			}
		}
		if (l == 225) {
			NPC class30_sub2_sub4_sub1_sub1_2 = npcArray[i1];
			if (class30_sub2_sub4_sub1_sub1_2 != null) {
				doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, class30_sub2_sub4_sub1_sub1_2.smallY[0], myPlayer.smallX[0], false, class30_sub2_sub4_sub1_sub1_2.smallX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				anInt1226 += i1;
				if (anInt1226 >= 85) {
					stream.writeEncryptedOpcode(230);
					stream.writeByte(239);
					anInt1226 = 0;
				}
				stream.writeEncryptedOpcode(17);
				stream.writeShortLittleEndianAdd128(i1);
			}
		}
		if (l == 965) {
			NPC class30_sub2_sub4_sub1_sub1_3 = npcArray[i1];
			if (class30_sub2_sub4_sub1_sub1_3 != null) {
				doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, class30_sub2_sub4_sub1_sub1_3.smallY[0], myPlayer.smallX[0], false, class30_sub2_sub4_sub1_sub1_3.smallX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				anInt1134++;
				if (anInt1134 >= 96) {
					stream.writeEncryptedOpcode(152);
					stream.writeByte(88);
					anInt1134 = 0;
				}
				stream.writeEncryptedOpcode(21);
				stream.writeShortBigEndian(i1);
			}
		}
		if (l == 413) {
			NPC class30_sub2_sub4_sub1_sub1_4 = npcArray[i1];
			if (class30_sub2_sub4_sub1_sub1_4 != null) {
				doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, class30_sub2_sub4_sub1_sub1_4.smallY[0], myPlayer.smallX[0], false, class30_sub2_sub4_sub1_sub1_4.smallX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				stream.writeEncryptedOpcode(131);
				stream.writeShortLittleEndianAdd128(i1);
				stream.writeShortBigEndianAdd128(anInt1137);
			}
		}
		if (l == 200) {
			closeOpenInterfaces();
		}
		if (l == 1025) {
			NPC class30_sub2_sub4_sub1_sub1_5 = npcArray[i1];
			if (class30_sub2_sub4_sub1_sub1_5 != null) {
				EntityDef entityDef = class30_sub2_sub4_sub1_sub1_5.desc;
				if (entityDef.childrenIDs != null) {
					entityDef = entityDef.getMorphedDefinition();
				}
				if (entityDef != null) {
					String s9;
					if (entityDef.description != null) {
						s9 = new String(entityDef.description);
					} else {
						s9 = "It's a " + entityDef.name + ".";
					}
					pushMessage(s9, 0, "");
				}
			}
		}
		if (l == 900) {
			walkToSceneObject(i1, k, j);
			stream.writeEncryptedOpcode(252);
			stream.writeShortLittleEndianAdd128(i1 >> 14 & 0x7fff);
			stream.writeShortLittleEndian(k + baseY);
			stream.writeShortBigEndianAdd128(j + baseX);
		}
		if (l == 412) {
			NPC class30_sub2_sub4_sub1_sub1_6 = npcArray[i1];
			if (class30_sub2_sub4_sub1_sub1_6 != null) {
				doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, class30_sub2_sub4_sub1_sub1_6.smallY[0], myPlayer.smallX[0], false, class30_sub2_sub4_sub1_sub1_6.smallX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				stream.writeEncryptedOpcode(72);
				stream.writeShortBigEndianAdd128(i1);
			}
		}
		if (l == 365) {
			Player class30_sub2_sub4_sub1_sub2_3 = playerArray[i1];
			if (class30_sub2_sub4_sub1_sub2_3 != null) {
				doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, class30_sub2_sub4_sub1_sub2_3.smallY[0], myPlayer.smallX[0], false, class30_sub2_sub4_sub1_sub2_3.smallX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				stream.writeEncryptedOpcode(249);
				stream.writeShortBigEndianAdd128(i1);
				stream.writeShortLittleEndian(anInt1137);
			}
		}
		if (l == 729) {
			Player class30_sub2_sub4_sub1_sub2_4 = playerArray[i1];
			if (class30_sub2_sub4_sub1_sub2_4 != null) {
				doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, class30_sub2_sub4_sub1_sub2_4.smallY[0], myPlayer.smallX[0], false, class30_sub2_sub4_sub1_sub2_4.smallX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				stream.writeEncryptedOpcode(39);
				stream.writeShortLittleEndian(i1);
			}
		}
		if (l == 577) {
			Player class30_sub2_sub4_sub1_sub2_5 = playerArray[i1];
			if (class30_sub2_sub4_sub1_sub2_5 != null) {
				doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, class30_sub2_sub4_sub1_sub2_5.smallY[0], myPlayer.smallX[0], false, class30_sub2_sub4_sub1_sub2_5.smallX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				stream.writeEncryptedOpcode(139);
				stream.writeShortLittleEndian(i1);
			}
		}
		if (l == 956 && walkToSceneObject(i1, k, j)) {
			stream.writeEncryptedOpcode(35);
			stream.writeShortLittleEndian(j + baseX);
			stream.writeShortBigEndianAdd128(anInt1137);
			stream.writeShortBigEndianAdd128(k + baseY);
			stream.writeShortLittleEndian(i1 >> 14 & 0x7fff);
		}
		if (l == 567) {
			boolean flag6 = doWalkTo(2, 0, 0, 0, myPlayer.smallY[0], 0, 0, k, myPlayer.smallX[0], false, j);
			if (!flag6) {
				flag6 = doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, k, myPlayer.smallX[0], false, j);
			}
			crossX = super.saveClickX;
			crossY = super.saveClickY;
			crossType = 2;
			crossIndex = 0;
			stream.writeEncryptedOpcode(23);
			stream.writeShortLittleEndian(k + baseY);
			stream.writeShortLittleEndian(i1);
			stream.writeShortLittleEndian(j + baseX);
		}
		if (l == 867) {
			if ((i1 & 3) == 0) {
				anInt1175++;
			}
			if (anInt1175 >= 59) {
				stream.writeEncryptedOpcode(200);
				stream.writeShortBigEndian(25501);
				anInt1175 = 0;
			}
			stream.writeEncryptedOpcode(43);
			stream.writeShortLittleEndian(k);
			stream.writeShortBigEndianAdd128(i1);
			stream.writeShortBigEndianAdd128(j);
			atInventoryLoopCycle = 0;
			atInventoryInterface = k;
			atInventoryIndex = j;
			atInventoryInterfaceType = 2;
			if (RSInterface.interfaceCache[k].parentID == openInterfaceID) {
				atInventoryInterfaceType = 1;
			}
			if (RSInterface.interfaceCache[k].parentID == backDialogID) {
				atInventoryInterfaceType = 3;
			}
		}
		if (l == 543) {
			stream.writeEncryptedOpcode(237);
			stream.writeShortBigEndian(j);
			stream.writeShortBigEndianAdd128(i1);
			stream.writeShortBigEndian(k);
			stream.writeShortBigEndianAdd128(anInt1137);
			atInventoryLoopCycle = 0;
			atInventoryInterface = k;
			atInventoryIndex = j;
			atInventoryInterfaceType = 2;
			if (RSInterface.interfaceCache[k].parentID == openInterfaceID) {
				atInventoryInterfaceType = 1;
			}
			if (RSInterface.interfaceCache[k].parentID == backDialogID) {
				atInventoryInterfaceType = 3;
			}
		}
		if (l == 606) {
			String s2 = menuActionName[i];
			int j2 = s2.indexOf("@whi@");
			if (j2 != -1) {
				if (openInterfaceID == -1) {
					closeOpenInterfaces();
					reportAbuseInput = s2.substring(j2 + 5).trim();
					canMute = false;
					for (RSInterface element : RSInterface.interfaceCache) {
						if (element == null || element.contentType != 600) {
							continue;
						}
						reportAbuseInterfaceID = openInterfaceID = element.parentID;
						break;
					}

				} else {
					pushMessage("Please close the interface you have open before using 'report abuse'", 0, "");
				}
			}
		}
		if (l == 491) {
			Player class30_sub2_sub4_sub1_sub2_6 = playerArray[i1];
			if (class30_sub2_sub4_sub1_sub2_6 != null) {
				doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, class30_sub2_sub4_sub1_sub2_6.smallY[0], myPlayer.smallX[0], false, class30_sub2_sub4_sub1_sub2_6.smallX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				stream.writeEncryptedOpcode(14);
				// stream.writeShortBigEndianAdd128(anInt1284);
				stream.writeShortBigEndian(i1);
				// stream.writeShortBigEndian(anInt1285);
				stream.writeShortLittleEndian(anInt1283);
			}
		}
		if (l == 639) {
			String s3 = menuActionName[i];
			int k2 = s3.indexOf("@whi@");
			if (k2 != -1) {
				long l4 = TextClass.encodeBase37Name(s3.substring(k2 + 5).trim());
				int k3 = -1;
				for (int i4 = 0; i4 < friendsCount; i4++) {
					if (friendsListAsLongs[i4] != l4) {
						continue;
					}
					k3 = i4;
					break;
				}

				if (k3 != -1 && friendsNodeIDs[k3] > 9) {
					inputTaken = true;
					inputDialogState = 0;
					messagePromptRaised = true;
					promptInput = "";
					friendsListAction = 3;
					aLong953 = friendsListAsLongs[k3];
					aString1121 = "Enter message to send to " + friendsList[k3];
					if (ClientSettings.ANDROID) {
						AndroidHooks.showKeyboard();
					}
				} else {
					pushMessage("That player is currently offline.", 0, "");
				}
			}
		}
		if (l == 454) {
			stream.writeEncryptedOpcode(41);
			stream.writeShortBigEndian(i1);
			stream.writeShortBigEndianAdd128(j);
			stream.writeShortBigEndianAdd128(k);
			atInventoryLoopCycle = 0;
			atInventoryInterface = k;
			atInventoryIndex = j;
			atInventoryInterfaceType = 2;
			if (RSInterface.interfaceCache[k].parentID == openInterfaceID) {
				atInventoryInterfaceType = 1;
			}
			if (RSInterface.interfaceCache[k].parentID == backDialogID) {
				atInventoryInterfaceType = 3;
			}
		}
		if (l == 478) {
			NPC class30_sub2_sub4_sub1_sub1_7 = npcArray[i1];
			if (class30_sub2_sub4_sub1_sub1_7 != null) {
				doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, class30_sub2_sub4_sub1_sub1_7.smallY[0], myPlayer.smallX[0], false, class30_sub2_sub4_sub1_sub1_7.smallX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				if ((i1 & 3) == 0) {
					anInt1155++;
				}
				if (anInt1155 >= 53) {
					stream.writeEncryptedOpcode(85);
					stream.writeByte(66);
					anInt1155 = 0;
				}
				stream.writeEncryptedOpcode(18);
				stream.writeShortLittleEndian(i1);
			}
		}
		if (l == 113) {//third
			walkToSceneObject(i1, k, j);
			stream.writeEncryptedOpcode(70);
			stream.writeShortLittleEndian(j + baseX);
			stream.writeShortBigEndian(k + baseY);
			stream.writeShortLittleEndianAdd128(i1 >> 14 & 0x7fff);
		}
		if (l == 872) {//fourth
			walkToSceneObject(i1, k, j);
			stream.writeEncryptedOpcode(234);
			stream.writeShortLittleEndianAdd128(j + baseX);
			stream.writeShortBigEndianAdd128(i1 >> 14 & 0x7fff);
			stream.writeShortLittleEndianAdd128(k + baseY);
		}
		if (l == 502) {//first
			walkToSceneObject(i1, k, j);
			stream.writeEncryptedOpcode(132);
			stream.writeShortLittleEndianAdd128(j + baseX);
			stream.writeShortBigEndian(i1 >> 14 & 0x7fff);
			stream.writeShortBigEndianAdd128(k + baseY);
		}
		if (l == 1125) {
			ItemDef itemDef = ItemDef.forID(i1);
			RSInterface class9_4 = RSInterface.interfaceCache[k];
			if (class9_4 != null && class9_4.itemQuantities[j] >= 1e5) {
				DecimalFormatSymbols separator = new DecimalFormatSymbols();
				separator.setGroupingSeparator(',');
				DecimalFormat formatter = new DecimalFormat("#,###,###,###", separator);
				pushMessage(formatter.format(class9_4.itemQuantities[j]) + " x " + itemDef.name, 0, "");
			}
			if (itemDef.description != null) {
				pushMessage(new String(itemDef.description) + " (" + intToKOrMil(itemDef.value) + "gp ea)", 0, "");
			} else {
				pushMessage("It's a " + itemDef.name + ".", 0, "");
			}
		}
		if (l == 169) {
			stream.writeEncryptedOpcode(185);
			stream.writeShortBigEndian(k);
			RSInterface class9_3 = RSInterface.interfaceCache[k];
			if (class9_3.valueIndexArray != null && class9_3.valueIndexArray[0][0] == 5) {
				int l2 = class9_3.valueIndexArray[0][1];
				variousSettings[l2] = 1 - variousSettings[l2];
				method33(l2);
				needDrawTabArea = true;
			}
		}
		if (l == 447) {
		    if (shiftDown)
			{
				doAction(2); //1=Examine, 2=Drop, 3=wear?, 4= does some really wack shit in the inventory
				needDrawTabArea = true;
				return;
			}
			itemSelected = 1;
			anInt1283 = j;
			anInt1284 = k;
			anInt1285 = i1;
			selectedItemName = ItemDef.forID(i1).name;
			spellSelected = 0;
			needDrawTabArea = true;
			return;
		}

		if (l == 1226) {
			int j1 = i1 >> 14 & 0x7fff;
			ObjectDef class46 = ObjectDef.forID(j1);
			String s10;
			if (class46.description != null) {
				s10 = new String(class46.description);
			} else {
				s10 = "It's a " + class46.name + ".";
			}
			pushMessage(s10, 0, "");
		}
		if (l == 244) {
			boolean flag7 = doWalkTo(2, 0, 0, 0, myPlayer.smallY[0], 0, 0, k, myPlayer.smallX[0], false, j);
			if (!flag7) {
				flag7 = doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, k, myPlayer.smallX[0], false, j);
			}
			crossX = super.saveClickX;
			crossY = super.saveClickY;
			crossType = 2;
			crossIndex = 0;
			stream.writeEncryptedOpcode(253);
			stream.writeShortLittleEndian(j + baseX);
			stream.writeShortLittleEndianAdd128(k + baseY);
			stream.writeShortBigEndianAdd128(i1);
		}
		if (l == 1448) {
			ItemDef itemDef_1 = ItemDef.forID(i1);
			String s6;
			if (itemDef_1.description != null) {
				s6 = new String(itemDef_1.description) + " (" + intToKOrMil(itemDef_1.value) + "gp ea)";
			} else {
				s6 = "It's a " + itemDef_1.name + ".";
			}
			pushMessage(s6, 0, "");
		}
		itemSelected = 0;
		spellSelected = 0;
		needDrawTabArea = true;

	}

	public void updateChatSuppressionZone() {
		chatSuppressionZone = 0;
		int worldTileX = (myPlayer.x >> 7) + baseX;
		int worldTileY = (myPlayer.y >> 7) + baseY;
		if (worldTileX >= 3053 && worldTileX <= 3156 && worldTileY >= 3056 && worldTileY <= 3136) {
			chatSuppressionZone = 1;
		}
		if (worldTileX >= 3072 && worldTileX <= 3118 && worldTileY >= 9492 && worldTileY <= 9535) {
			chatSuppressionZone = 1;
		}
		if (chatSuppressionZone == 1 && worldTileX >= 3139 && worldTileX <= 3199 && worldTileY >= 3008 && worldTileY <= 3062) {
			chatSuppressionZone = 0;
		}
	}

	public void run() {
		if (drawFlames) {
			drawFlames();
		} else {
			super.run();
		}
	}

	public void build3dScreenMenu() {
		if (itemSelected == 0 && spellSelected == 0) {
			menuActionName[menuActionRow] = "Walk here";
			menuActionID[menuActionRow] = 516;
			menuActionCmd2[menuActionRow] = super.mouseX;
			menuActionCmd3[menuActionRow] = super.mouseY;
			menuActionRow++;
		}
		int j = -1;
		for (int k = 0; k < Model.pickedCount; k++) {
			int l = Model.pickedIds[k];
			int i1 = l & 0x7f;
			int j1 = l >> 7 & 0x7f;
			int k1 = l >> 29 & 3;
			int l1 = l >> 14 & 0x7fff;
			if (l == j) {
				continue;
			}
			j = l;
			if (k1 == 2 && worldController.getObjectInfo(plane, i1, j1, l) >= 0) {
				ObjectDef class46 = ObjectDef.forID(l1);
				if (class46.childrenIDs != null) {
					class46 = class46.resolveMorph();
				}
				if (class46 == null) {
					continue;
				}
				if (itemSelected == 1) {
					menuActionName[menuActionRow] = "Use " + selectedItemName + " with @cya@" + class46.name;
					menuActionID[menuActionRow] = 62;
					menuActionCmd1[menuActionRow] = l;
					menuActionCmd2[menuActionRow] = i1;
					menuActionCmd3[menuActionRow] = j1;
					menuActionRow++;
				} else if (spellSelected == 1) {
					if ((spellUsableOn & 4) == 4) {
						menuActionName[menuActionRow] = spellTooltip + " @cya@" + class46.name;
						menuActionID[menuActionRow] = 956;
						menuActionCmd1[menuActionRow] = l;
						menuActionCmd2[menuActionRow] = i1;
						menuActionCmd3[menuActionRow] = j1;
						menuActionRow++;
					}
				} else {
					if (class46.actions != null) {
						for (int i2 = 4; i2 >= 0; i2--) {
							if (class46.actions[i2] != null) {
								menuActionName[menuActionRow] = class46.actions[i2] + " @cya@" + class46.name;
								if (i2 == 0) {
									menuActionID[menuActionRow] = 502;
								}
								if (i2 == 1) {
									menuActionID[menuActionRow] = 900;
								}
								if (i2 == 2) {
									menuActionID[menuActionRow] = 113;
								}
								if (i2 == 3) {
									menuActionID[menuActionRow] = 872;
								}
								if (i2 == 4) {
									menuActionID[menuActionRow] = 1062;
								}
								menuActionCmd1[menuActionRow] = l;
								menuActionCmd2[menuActionRow] = i1;
								menuActionCmd3[menuActionRow] = j1;
								menuActionRow++;
							}
						}

					}
					menuActionName[menuActionRow] = "Examine @cya@" + class46.name + (showInfo ? " @gre@(@whi@" + l1 + "@gre@) (@whi@" + (i1 + baseX) + "," + (j1 + baseY) + "@gre@)" : "");
					menuActionID[menuActionRow] = 1226;
					menuActionCmd1[menuActionRow] = class46.type << 14;
					menuActionCmd2[menuActionRow] = i1;
					menuActionCmd3[menuActionRow] = j1;
					menuActionRow++;
				}
			}
			if (k1 == 1) {
				NPC npc = npcArray[l1];
				if (npc.desc.tileSize == 1 && (npc.x & 0x7f) == 64 && (npc.y & 0x7f) == 64) {
					for (int j2 = 0; j2 < npcCount; j2++) {
						NPC npc2 = npcArray[npcIndices[j2]];
						if (npc2 != null && npc2 != npc && npc2.desc.tileSize == 1 && npc2.x == npc.x && npc2.y == npc.y) {
							buildAtNPCMenu(npc2.desc, npcIndices[j2], j1, i1);
						}
					}

					for (int l2 = 0; l2 < playerCount; l2++) {
						Player player = playerArray[playerIndices[l2]];
						if (player != null && player.x == npc.x && player.y == npc.y) {
							buildAtPlayerMenu(i1, playerIndices[l2], player, j1);
						}
					}

				}
				buildAtNPCMenu(npc.desc, l1, j1, i1);
			}
			if (k1 == 0) {
				Player player = playerArray[l1];
				if ((player.x & 0x7f) == 64 && (player.y & 0x7f) == 64) {
					for (int k2 = 0; k2 < npcCount; k2++) {
						NPC class30_sub2_sub4_sub1_sub1_2 = npcArray[npcIndices[k2]];
						if (class30_sub2_sub4_sub1_sub1_2 != null && class30_sub2_sub4_sub1_sub1_2.desc.tileSize == 1 && class30_sub2_sub4_sub1_sub1_2.x == player.x && class30_sub2_sub4_sub1_sub1_2.y == player.y) {
							buildAtNPCMenu(class30_sub2_sub4_sub1_sub1_2.desc, npcIndices[k2], j1, i1);
						}
					}

					for (int i3 = 0; i3 < playerCount; i3++) {
						Player class30_sub2_sub4_sub1_sub2_2 = playerArray[playerIndices[i3]];
						if (class30_sub2_sub4_sub1_sub2_2 != null && class30_sub2_sub4_sub1_sub2_2 != player && class30_sub2_sub4_sub1_sub2_2.x == player.x && class30_sub2_sub4_sub1_sub2_2.y == player.y) {
							buildAtPlayerMenu(i1, playerIndices[i3], class30_sub2_sub4_sub1_sub2_2, j1);
						}
					}

				}
				buildAtPlayerMenu(i1, l1, player, j1);
			}
			if (k1 == 3) {
				NodeList class19 = groundArray[plane][i1][j1];
				if (class19 != null) {
					for (Item item = (Item) class19.firstFromHead(); item != null; item = (Item) class19.nextTowardTail()) {
						ItemDef itemDef = ItemDef.forID(item.ID);
						if (itemSelected == 1) {
							menuActionName[menuActionRow] = "Use " + selectedItemName + " with @lre@" + itemDef.name;
							menuActionID[menuActionRow] = 511;
							menuActionCmd1[menuActionRow] = item.ID;
							menuActionCmd2[menuActionRow] = i1;
							menuActionCmd3[menuActionRow] = j1;
							menuActionRow++;
						} else if (spellSelected == 1) {
							if ((spellUsableOn & 1) == 1) {
								menuActionName[menuActionRow] = spellTooltip + " @lre@" + itemDef.name;
								menuActionID[menuActionRow] = 94;
								menuActionCmd1[menuActionRow] = item.ID;
								menuActionCmd2[menuActionRow] = i1;
								menuActionCmd3[menuActionRow] = j1;
								menuActionRow++;
							}
						} else {
							for (int j3 = 4; j3 >= 0; j3--) {
								if (itemDef.groundActions != null && itemDef.groundActions[j3] != null) {
									menuActionName[menuActionRow] = itemDef.groundActions[j3] + " @lre@" + itemDef.name;
									if (j3 == 0) {
										menuActionID[menuActionRow] = 652;
									}
									if (j3 == 1) {
										menuActionID[menuActionRow] = 567;
									}
									if (j3 == 2) {
										menuActionID[menuActionRow] = 234;
									}
									if (j3 == 3) {
										menuActionID[menuActionRow] = 244;
									}
									if (j3 == 4) {
										menuActionID[menuActionRow] = 213;
									}
									menuActionCmd1[menuActionRow] = item.ID;
									menuActionCmd2[menuActionRow] = i1;
									menuActionCmd3[menuActionRow] = j1;
									menuActionRow++;
								} else if (j3 == 2) {
									menuActionName[menuActionRow] = "Take @lre@" + itemDef.name;
									menuActionID[menuActionRow] = 234;
									menuActionCmd1[menuActionRow] = item.ID;
									menuActionCmd2[menuActionRow] = i1;
									menuActionCmd3[menuActionRow] = j1;
									menuActionRow++;
								}
							}

							menuActionName[menuActionRow] = "Examine @lre@" + itemDef.name + (showInfo ? " @gre@(@whi@" + item.ID + "@gre@)" : "");
							menuActionID[menuActionRow] = 1448;
							menuActionCmd1[menuActionRow] = item.ID;
							menuActionCmd2[menuActionRow] = i1;
							menuActionCmd3[menuActionRow] = j1;
							menuActionRow++;
						}
					}

				}
			}
		}
	}

	public void cleanUpForQuit() {
		Signlink.reporterror = false;
		try {
			if (socketStream != null) {
				socketStream.close();
			}
		} catch (Exception _ex) {
		}
		socketStream = null;
		stopMidi();
		if (mouseDetection != null) {
			mouseDetection.running = false;
		}
		mouseDetection = null;
		if (onDemandFetcher != null) {
			// prevent an exception if the client hasn't connected but tries to close the client, so it will actually close
			onDemandFetcher.disable();
		}
		onDemandFetcher = null;
		aStream_834 = null;
		stream = null;
		aStream_847 = null;
		inStream = null;
		regionKeys = null;
		regionTerrainData = null;
		regionObjectData = null;
		regionTerrainFileIds = null;
		regionObjectFileIds = null;
		intGroundArray = null;
		byteGroundArray = null;
		worldController = null;
		aClass11Array1230 = null;
		pathBackDirection = null;
		pathDistance = null;
		bigX = null;
		bigY = null;
		textureScrollScratch = null;
		aRSImageProducer_1163 = null;
		aRSImageProducer_1164 = null;
		aRSImageProducer_1165 = null;
		aRSImageProducer_1166 = null;
		aRSImageProducer_1123 = null;
		aRSImageProducer_1124 = null;
		aRSImageProducer_1125 = null;
		backLeftIP1 = null;
		backLeftIP2 = null;
		backRightIP1 = null;
		backRightIP2 = null;
		backTopIP1 = null;
		backVmidIP1 = null;
		backVmidIP2 = null;
		backVmidIP3 = null;
		backVmidIP2_2 = null;
		invBack = null;
		mapBack = null;
		chatBack = null;
		backBase1 = null;
		backBase2 = null;
		backHmid1 = null;
		sideIcons = null;
		redStone1 = null;
		redStone2 = null;
		redStone3 = null;
		redStone1_2 = null;
		redStone2_2 = null;
		redStone1_3 = null;
		redStone2_3 = null;
		redStone3_2 = null;
		redStone1_4 = null;
		redStone2_4 = null;
		compass = null;
		hitMarks = null;
		headIcons = null;
		skullIcons = null;
		headIconsHint = null;
		crosses = null;
		mapDotItem = null;
		mapDotNPC = null;
		mapDotPlayer = null;
		mapDotFriend = null;
		mapDotTeam = null;
		mapScenes = null;
		mapFunctions = null;
		anIntArrayArray929 = null;
		playerArray = null;
		playerIndices = null;
		maskUpdateIndices = null;
		aStreamArray895s = null;
		anIntArray840 = null;
		npcArray = null;
		npcIndices = null;
		groundArray = null;
		pendingSceneChanges = null;
		projectiles = null;
		transientGraphics = null;
		menuActionCmd2 = null;
		menuActionCmd3 = null;
		menuActionID = null;
		menuActionCmd1 = null;
		menuActionName = null;
		variousSettings = null;
		mapFunctionIconTileX = null;
		mapFunctionIconTileY = null;
		mapFunctionIconSprites = null;
		minimapSprite = null;
		friendsList = null;
		friendsListAsLongs = null;
		friendsNodeIDs = null;
		aRSImageProducer_1110 = null;
		aRSImageProducer_1111 = null;
		aRSImageProducer_1107 = null;
		aRSImageProducer_1108 = null;
		aRSImageProducer_1109 = null;
		aRSImageProducer_1112 = null;
		aRSImageProducer_1113 = null;
		aRSImageProducer_1114 = null;
		aRSImageProducer_1115 = null;
		multiOverlay = null;
		nullLoader();
		ObjectDef.nullLoader();
		EntityDef.nullLoader();
		ItemDef.nullLoader();
		Flo.cache = null;
		IDK.cache = null;
		RSInterface.interfaceCache = null;
		DummyClass.cache = null;
		Animation.anims = null;
		SpotAnim.cache = null;
		SpotAnim.modelCache = null;
		Varp.cache = null;
		super.fullGameScreen = null;
		Player.mruNodes = null;
		Texture.nullLoader();
		WorldController.nullLoader();
		Model.nullLoader();
		AnimationFrame.nullLoader();
		System.gc();
	}

	public void printDebug() {
		System.out.println("============");
		System.out.println("flame-cycle:" + anInt1208);
		if (onDemandFetcher != null) {
			System.out.println("Od-cycle:" + onDemandFetcher.onDemandCycle);
		}
		System.out.println("loop-cycle:" + loopCycle);
		System.out.println("draw-cycle:" + anInt1061);
		System.out.println("ptype:" + pktType);
		System.out.println("psize:" + pktSize);
		if (socketStream != null) {
			socketStream.printDebug();
		}
		super.shouldDebug = true;
	}


	public void processKeyboardInput() {
		do {
			int characterCode = readChar(-796);
			if (characterCode == -1) {
				break;
			}
			if (customTabAction == 1 || customTabAction == 2) {
				if (characterCode >= 48 && characterCode <= 57 && promptInput.length() < 10 && !promptInput.toLowerCase().contains("k") && !promptInput.toLowerCase().contains("m") && !promptInput.toLowerCase().contains("b")) {
					promptInput += (char) characterCode;
					inputTaken = true;
				}
				if ((!promptInput.toLowerCase().contains("k") && !promptInput.toLowerCase().contains("m") && !promptInput.toLowerCase().contains("b")) && (characterCode == 107 || characterCode == 109) || characterCode == 98) {
					promptInput += (char) characterCode;
					inputTaken = true;
				}
				if (characterCode == 8 && promptInput.length() > 0) {
					promptInput = promptInput.substring(0, promptInput.length() - 1);
					inputTaken = true;
				}
				try {
					if (characterCode == 13 || characterCode == 10) {
						if (promptInput.length() > 0) {
							if (promptInput.toLowerCase().contains("k")) {
								promptInput = promptInput.replaceAll("k", "000");
							} else if (promptInput.toLowerCase().contains("m")) {
								promptInput = promptInput.replaceAll("m", "000000");
							} else if (promptInput.toLowerCase().contains("b")) {
								promptInput = promptInput.replaceAll("b", "000000000");
							}
							if (customTabAction == 1) {
								customSettingMinItemValue = Integer.parseInt(promptInput);
							}
							if (customTabAction == 2) {
								WorldController.drawDistance = Math.max(10, Math.min(100, Integer.parseInt(promptInput)));
								zoom = Math.min(zoom, WorldController.drawDistance / 3);
							}
						}
						customTabAction = 0;
						inputTaken = true;
						messagePromptRaised = false;
						drawTabArea();
					}
				} catch (NumberFormatException nfe) {
					customTabAction = 0;
					inputTaken = true;
					messagePromptRaised = false;
					pushMessage("Please enter a lower amount.", 0, "");
				}
			} else if (openInterfaceID != -1 && openInterfaceID == reportAbuseInterfaceID) {
				if (characterCode == 8 && reportAbuseInput.length() > 0) {
					reportAbuseInput = reportAbuseInput.substring(0, reportAbuseInput.length() - 1);
				}
				if ((characterCode >= 97 && characterCode <= 122 || characterCode >= 65 && characterCode <= 90 || characterCode >= 48 && characterCode <= 57 || characterCode == 32) && reportAbuseInput.length() < 12) {
					reportAbuseInput += (char) characterCode;
				}
			} else if (messagePromptRaised) {
				if (characterCode >= 32 && characterCode <= 122 && promptInput.length() < 80) {
					promptInput += (char) characterCode;
					inputTaken = true;
				}
				if (characterCode == 8 && promptInput.length() > 0) {
					promptInput = promptInput.substring(0, promptInput.length() - 1);
					inputTaken = true;
				}
				if (characterCode == 13 || characterCode == 10) {
					messagePromptRaised = false;
					inputTaken = true;
					if (ClientSettings.ANDROID) {
						AndroidHooks.hideKeyboard();
					}
					if (friendsListAction == 1) {
						long friendName = TextClass.encodeBase37Name(promptInput);
						addFriend(friendName);
					}
					if (friendsListAction == 2 && friendsCount > 0) {
						long friendNameToRemove = TextClass.encodeBase37Name(promptInput);
						delFriend(friendNameToRemove);
					}
					if (friendsListAction == 3 && promptInput.length() > 0) {
						stream.writeEncryptedOpcode(126);
						stream.writeByte(0);
						int privatePayloadStart = stream.currentOffset;
						stream.writeLongBigEndian(aLong953);
						TextInput.encodeChatText(promptInput, stream);
						stream.backpatchBytePayloadLength(stream.currentOffset - privatePayloadStart);
						promptInput = TextInput.normalizeChatText(promptInput);
						promptInput = Censor.doCensor(promptInput);
						pushMessage(promptInput, 6, TextClass.formatDisplayName(TextClass.decodeBase37Name(aLong953)));
						if (privateChatMode == 2) {
							privateChatMode = 1;
							aBoolean1233 = true;
							stream.writeEncryptedOpcode(95);
							stream.writeByte(publicChatMode);
							stream.writeByte(privateChatMode);
							stream.writeByte(tradeMode);
						}
					}
					if (friendsListAction == 4 && ignoreCount < 100) {
						long ignoreName = TextClass.encodeBase37Name(promptInput);
						addIgnore(ignoreName);
					}
					if (friendsListAction == 5 && ignoreCount > 0) {
						long ignoreNameToRemove = TextClass.encodeBase37Name(promptInput);
						delIgnore(ignoreNameToRemove);
					}
				}
			} else if (inputDialogState == 1) {
				if (characterCode >= 48 && characterCode <= 57 && amountOrNameInput.length() < 10 && !amountOrNameInput.toLowerCase().contains("k") && !amountOrNameInput.toLowerCase().contains("m") && !amountOrNameInput.toLowerCase().contains("b")) {
					amountOrNameInput += (char) characterCode;
					inputTaken = true;
				}
				if ((!amountOrNameInput.toLowerCase().contains("k") && !amountOrNameInput.toLowerCase().contains("m") && !amountOrNameInput.toLowerCase().contains("b")) && (characterCode == 107 || characterCode == 109) || characterCode == 98) {
					amountOrNameInput += (char) characterCode;
					inputTaken = true;
				}
				if (characterCode == 8 && amountOrNameInput.length() > 0) {
					amountOrNameInput = amountOrNameInput.substring(0, amountOrNameInput.length() - 1);
					inputTaken = true;
				}
				try {
					if (characterCode == 13 || characterCode == 10) {
						if (amountOrNameInput.length() > 0) {
							if (amountOrNameInput.toLowerCase().contains("k")) {
								amountOrNameInput = amountOrNameInput.replaceAll("k", "000");
							} else if (amountOrNameInput.toLowerCase().contains("m")) {
								amountOrNameInput = amountOrNameInput.replaceAll("m", "000000");
							} else if (amountOrNameInput.toLowerCase().contains("b")) {
								amountOrNameInput = amountOrNameInput.replaceAll("b", "000000000");
							}
							int amount = 0;
							amount = Integer.parseInt(amountOrNameInput);
							stream.writeEncryptedOpcode(208);
							stream.writeIntBigEndian(amount);
						}
						inputDialogState = 0;
						inputTaken = true;
						if (ClientSettings.ANDROID) {
							AndroidHooks.hideKeyboard();
						}
					}
				} catch (NumberFormatException nfe) {
					inputDialogState = 0;
					inputTaken = true;
					if (ClientSettings.ANDROID) {
						AndroidHooks.hideKeyboard();
					}
					pushMessage("Please enter a lower amount.", 0, "");
				}
			} else if (inputDialogState == 2) {
				if (characterCode >= 32 && characterCode <= 122 && amountOrNameInput.length() < 12) {
					amountOrNameInput += (char) characterCode;
					inputTaken = true;
				}
				if (characterCode == 8 && amountOrNameInput.length() > 0) {
					amountOrNameInput = amountOrNameInput.substring(0, amountOrNameInput.length() - 1);
					inputTaken = true;
				}
				if (characterCode == 13 || characterCode == 10) {
					if (amountOrNameInput.length() > 0) {
						stream.writeEncryptedOpcode(60);
						stream.writeLongBigEndian(TextClass.encodeBase37Name(amountOrNameInput));
					}
					inputDialogState = 0;
					inputTaken = true;
					if (ClientSettings.ANDROID) {
						AndroidHooks.hideKeyboard();
					}
				}
			} else {
				// typing characters
				if (characterCode >= 32 && characterCode <= 122 && inputString.length() < 80) {
					inputString += (char) characterCode;
					inputTaken = true;
					if (inputString.startsWith("::search")) {
						String[] args = inputString.split(" ");
						inputDialogState = 3;
						int searchType = 1;
						String searchString = "";
						if (args.length < 2) {
							return;
						}
						try {
							searchType = Integer.parseInt(args[1]);
							if (args.length >= 3) {
								searchString = inputString.substring(inputString.indexOf(args[1]) + args[1].length() + 1);
							}
						} catch (Exception e) {
							searchType = 1;
							searchString = inputString.substring(args[0].length() + 1);
						}
						definitionSearch(searchString, searchType);
					}
				}
				// deleting characters
				if (characterCode == 8 && inputString.length() > 0) {
					inputString = inputString.substring(0, inputString.length() - 1);
					inputTaken = true;
					if (inputString.startsWith("::search")) {
						String[] args = inputString.split(" ");
						inputDialogState = 3;
						int searchType = 1;
						String searchString = "";
						if (args.length < 2) {
							return;
						}
						try {
							searchType = Integer.parseInt(args[1]);
							if (args.length >= 3) {
								searchString = inputString.substring(inputString.indexOf(args[1]) + args[1].length() + 1);
							}
						} catch (Exception e) {
							searchType = 1;
							searchString = inputString.substring(args[0].length() + 1);
						}
						definitionSearch(searchString, searchType);
					}
				}
				// submitted string
				if ((characterCode == 13 || characterCode == 10) && inputString.length() > 0) {
					if (inputString.equals("::gfxtgl") || inputString.equals("::tglgfx") || inputString.equals("::togglerender") || inputString.equals("::togglegfx")) {
						graphicsEnabled = !graphicsEnabled;
					}
					if (inputString.equals("::crtlkeyzoom") || inputString.equals("::controlkeyzoom")) {
						ClientSettings.CONTROL_KEY_ZOOMING = !ClientSettings.CONTROL_KEY_ZOOMING;
						pushMessage("Your control key zooming is now: " + (ClientSettings.CONTROL_KEY_ZOOMING ? "enabled" : "disabled"), 0, "");
					}
					if (myPrivilege >= 2) {
						if (inputString.equals("::noclip"))
							for (int k1 = 0; k1 < 4; k1++)
								for (int i2 = 1; i2 < 103; i2++)
									for (int k2 = 1; k2 < 103; k2++)
										aClass11Array1230[k1].tileFlags[i2][k2] = 0;
						if (inputString.equals("::clientdrop")) {
							dropClient();
						}
						if (inputString.equals("::lag")) {
							printDebug();
						}
						if (inputString.startsWith("::int")) {
							String[] args = inputString.split(" ");
							int interfaceID = 1;
							try {
								interfaceID = Integer.parseInt(args[1]);
							} catch (Exception e) {
								interfaceID = 1;
							}
							openInterface(interfaceID);
							inputString = "";
							inputTaken = true;
							return;
						}
						if (inputString.equals("::mg")) {
							if (tabInterfaceIDs[6] == 12855)
								openSideInterface(6, 1151);
							else
								openSideInterface(6, 12855);
							inputString = "";
							inputTaken = true;
							return;
						}
						if (inputString.equals("::prefetchmusic")) {
							for (int j1 = 0; j1 < onDemandFetcher.getVersionCount(2); j1++) {
								onDemandFetcher.markExtraFileForDownload((byte) 1, 2, j1);
							}
						}
					}
						if (inputString.startsWith("::dd")) {
							String[] args = inputString.split(" ");
							int  distance = 25;
							try {
								distance = Math.max(10, Math.min(100, Integer.parseInt(args[1])));
							} catch (Exception e) {
								distance = 25;
							}
							WorldController.drawDistance = distance;
							if (zoom > (WorldController.drawDistance / 3))
								zoom = WorldController.drawDistance / 3;
							inputString = "";
							inputTaken = true;
							return;
						}
						if (inputString.equals("::dataon")) {
							showInfo = !showInfo;
						}
					if (inputString.startsWith("::")) {
						stream.writeEncryptedOpcode(103);
						stream.writeByte(inputString.length() - 1);
						stream.writeLfTerminatedString(inputString.substring(2));
					} else {
						String lowercaseChatText = inputString.toLowerCase();
						int chatColorCode = 0;
						if (lowercaseChatText.startsWith("yellow:")) {
							chatColorCode = 0;
							inputString = inputString.substring(7);
						} else if (lowercaseChatText.startsWith("red:")) {
							chatColorCode = 1;
							inputString = inputString.substring(4);
						} else if (lowercaseChatText.startsWith("green:")) {
							chatColorCode = 2;
							inputString = inputString.substring(6);
						} else if (lowercaseChatText.startsWith("cyan:")) {
							chatColorCode = 3;
							inputString = inputString.substring(5);
						} else if (lowercaseChatText.startsWith("purple:")) {
							chatColorCode = 4;
							inputString = inputString.substring(7);
						} else if (lowercaseChatText.startsWith("white:")) {
							chatColorCode = 5;
							inputString = inputString.substring(6);
						} else if (lowercaseChatText.startsWith("flash1:")) {
							chatColorCode = 6;
							inputString = inputString.substring(7);
						} else if (lowercaseChatText.startsWith("flash2:")) {
							chatColorCode = 7;
							inputString = inputString.substring(7);
						} else if (lowercaseChatText.startsWith("flash3:")) {
							chatColorCode = 8;
							inputString = inputString.substring(7);
						} else if (lowercaseChatText.startsWith("glow1:")) {
							chatColorCode = 9;
							inputString = inputString.substring(6);
						} else if (lowercaseChatText.startsWith("glow2:")) {
							chatColorCode = 10;
							inputString = inputString.substring(6);
						} else if (lowercaseChatText.startsWith("glow3:")) {
							chatColorCode = 11;
							inputString = inputString.substring(6);
						}
						lowercaseChatText = inputString.toLowerCase();
						int chatEffectCode = 0;
						if (lowercaseChatText.startsWith("wave:")) {
							chatEffectCode = 1;
							inputString = inputString.substring(5);
						} else if (lowercaseChatText.startsWith("wave2:")) {
							chatEffectCode = 2;
							inputString = inputString.substring(6);
						} else if (lowercaseChatText.startsWith("shake:")) {
							chatEffectCode = 3;
							inputString = inputString.substring(6);
						} else if (lowercaseChatText.startsWith("scroll:")) {
							chatEffectCode = 4;
							inputString = inputString.substring(7);
						} else if (lowercaseChatText.startsWith("slide:")) {
							chatEffectCode = 5;
							inputString = inputString.substring(6);
						}
						stream.writeEncryptedOpcode(4);
						stream.writeByte(0);
						int publicPayloadStart = stream.currentOffset;
						stream.writeByte128MinusValue(chatEffectCode);
						stream.writeByte128MinusValue(chatColorCode);
						aStream_834.currentOffset = 0;
						TextInput.encodeChatText(inputString, aStream_834);
						stream.writeReversedBytesAdd128(0, aStream_834.buffer, aStream_834.currentOffset);
						stream.backpatchBytePayloadLength(stream.currentOffset - publicPayloadStart);
						inputString = TextInput.normalizeChatText(inputString);
						inputString = Censor.doCensor(inputString);
						myPlayer.textSpoken = inputString;
						myPlayer.anInt1513 = chatColorCode;
						myPlayer.anInt1531 = chatEffectCode;
						myPlayer.textCycle = 150;
						if (myPrivilege >= 1) {
							pushMessage(myPlayer.textSpoken, 2, "@cr" + Math.min(2, myPrivilege) + "@" + myPlayer.name);
						} else {
							pushMessage(myPlayer.textSpoken, 2, myPlayer.name);
						}
						if (publicChatMode == 2) {
							publicChatMode = 3;
							aBoolean1233 = true;
							stream.writeEncryptedOpcode(95);
							stream.writeByte(publicChatMode);
							stream.writeByte(privateChatMode);
							stream.writeByte(tradeMode);
						}
					}
					inputString = "";
					inputTaken = true;
				}
			}
		} while (true);
	}

	public void buildChatAreaMenu(int hoverY) {
		int visibleLineCount = 0;
		for (int messageIndex = 0; messageIndex < 100; messageIndex++) {
			if (chatMessages[messageIndex] == null) {
				continue;
			}
			int chatType = chatTypes[messageIndex];
			int lineBottomY = 70 - visibleLineCount * 14 + anInt1089 + 4;
			if (lineBottomY < -20) {
				break;
			}
			String senderName = chatNames[messageIndex];
			if (senderName != null && senderName.startsWith("@cr1@")) {
				senderName = senderName.substring(5);
			}
			if (senderName != null && senderName.startsWith("@cr2@")) {
				senderName = senderName.substring(5);
			}
			if (chatType == 0) {
				visibleLineCount++;
			}
			if ((chatType == 1 || chatType == 2) && (chatType == 1 || publicChatMode == 0 || publicChatMode == 1 && isFriendOrSelf(senderName))) {
				if (hoverY > lineBottomY - 14 && hoverY <= lineBottomY && !senderName.equals(myPlayer.name)) {
					if (myPrivilege >= 1 && myPrivilege <= 3) {
						menuActionName[menuActionRow] = "Report abuse @whi@" + senderName;
						menuActionID[menuActionRow] = 606;
						menuActionRow++;
					}
					menuActionName[menuActionRow] = "Add ignore @whi@" + senderName;
					menuActionID[menuActionRow] = 42;
					menuActionRow++;
					menuActionName[menuActionRow] = "Reply to @whi@" + senderName;
					menuActionID[menuActionRow] = 639;
					menuActionRow++;
					menuActionName[menuActionRow] = "Add friend @whi@" + senderName;
					menuActionID[menuActionRow] = 337;
					menuActionRow++;
				}
				visibleLineCount++;
			}
			if ((chatType == 3 || chatType == 7) && splitpublicChat == 0 && (chatType == 7 || privateChatMode == 0 || publicChatMode == 1 && isFriendOrSelf(senderName))) {
				if (hoverY > lineBottomY - 14 && hoverY <= lineBottomY) {
					if (myPrivilege >= 1 && myPrivilege <= 3) {
						menuActionName[menuActionRow] = "Report abuse @whi@" + senderName;
						menuActionID[menuActionRow] = 606;
						menuActionRow++;
					}
					menuActionName[menuActionRow] = "Add ignore @whi@" + senderName;
					menuActionID[menuActionRow] = 42;
					menuActionRow++;
					menuActionName[menuActionRow] = "Reply to @whi@" + senderName;
					menuActionID[menuActionRow] = 639;
					menuActionRow++;
					menuActionName[menuActionRow] = "Add friend @whi@" + senderName;
					menuActionID[menuActionRow] = 337;
					menuActionRow++;
				}
				visibleLineCount++;
			}
			if (chatType == 4 && (tradeMode == 0 || tradeMode == 1 && isFriendOrSelf(senderName))) {
				if (hoverY > lineBottomY - 14 && hoverY <= lineBottomY) {
					menuActionName[menuActionRow] = "Accept trade @whi@" + senderName;
					menuActionID[menuActionRow] = 484;
					menuActionRow++;
				}
				visibleLineCount++;
			}
			if ((chatType == 5 || chatType == 6) && splitpublicChat == 0 && privateChatMode < 2) {
				visibleLineCount++;
			}
			if (chatType == 8 && (tradeMode == 0 || tradeMode == 1 && isFriendOrSelf(senderName))) {
				if (hoverY > lineBottomY - 14 && hoverY <= lineBottomY) {
					menuActionName[menuActionRow] = "Accept challenge @whi@" + senderName;
					menuActionID[menuActionRow] = 6;
					menuActionRow++;
				}
				visibleLineCount++;
			}
		}

	}

	public void drawFriendsListOrWelcomeScreen(RSInterface class9) {
		int j = class9.contentType;
		if (j >= 1 && j <= 100 || j >= 701 && j <= 800) {
			if (j == 1 && anInt900 == 0) {
				class9.disabledText = "Loading friend list";
				class9.atActionType = 0;
				return;
			}
			if (j == 1 && anInt900 == 1) {
				class9.disabledText = "Connecting to friendserver";
				class9.atActionType = 0;
				return;
			}
			if (j == 2 && anInt900 != 2) {
				class9.disabledText = "Please wait...";
				class9.atActionType = 0;
				return;
			}
			int k = friendsCount;
			if (anInt900 != 2) {
				k = 0;
			}
			if (j > 700) {
				j -= 601;
			} else {
				j--;
			}
			if (j >= k) {
				class9.disabledText = "";
				class9.atActionType = 0;
				return;
			} else {
				class9.disabledText = friendsList[j];
				class9.atActionType = 1;
				return;
			}
		}
		if (j >= 101 && j <= 200 || j >= 801 && j <= 900) {
			int l = friendsCount;
			if (anInt900 != 2) {
				l = 0;
			}
			if (j > 800) {
				j -= 701;
			} else {
				j -= 101;
			}
			if (j >= l) {
				class9.disabledText = "";
				class9.atActionType = 0;
				return;
			}
			if (friendsNodeIDs[j] - 9 <= 0) {
				class9.disabledText = "@red@Offline";
			} else if (friendsNodeIDs[j] == nodeID) {
				class9.disabledText = "@gre@World-" + (friendsNodeIDs[j] - 9);
			} else {
				class9.disabledText = "@yel@World-" + (friendsNodeIDs[j] - 9);
			}
			class9.atActionType = 1;
			return;
		}
		if (j == 203) {
			int i1 = friendsCount;
			if (anInt900 != 2) {
				i1 = 0;
			}
			class9.scrollMax = i1 * 15 + 20;
			if (class9.scrollMax <= class9.height) {
				class9.scrollMax = class9.height + 1;
			}
			return;
		}
		if (j >= 401 && j <= 500) {
			if ((j -= 401) == 0 && anInt900 == 0) {
				class9.disabledText = "Loading ignore list";
				class9.atActionType = 0;
				return;
			}
			if (j == 1 && anInt900 == 0) {
				class9.disabledText = "Please wait...";
				class9.atActionType = 0;
				return;
			}
			int j1 = ignoreCount;
			if (anInt900 == 0) {
				j1 = 0;
			}
			if (j >= j1) {
				class9.disabledText = "";
				class9.atActionType = 0;
				return;
			} else {
				class9.disabledText = TextClass.formatDisplayName(TextClass.decodeBase37Name(ignoreListAsLongs[j]));
				class9.atActionType = 1;
				return;
			}
		}
		if (j == 503) {
			class9.scrollMax = ignoreCount * 15 + 20;
			if (class9.scrollMax <= class9.height) {
				class9.scrollMax = class9.height + 1;
			}
			return;
		}
		if (j == 327) {
			class9.modelPitch = 150;
			class9.modelYaw = (int) (Math.sin((double) loopCycle / 40D) * 256D) & 0x7ff;
			if (aBoolean1031) {
				for (int k1 = 0; k1 < 7; k1++) {
					int l1 = designerKitIds[k1];
					if (l1 >= 0 && !IDK.cache[l1].areBodyModelsLoaded()) {
						return;
					}
				}

				aBoolean1031 = false;
				Model aclass30_sub2_sub4_sub6s[] = new Model[7];
				int i2 = 0;
				for (int j2 = 0; j2 < 7; j2++) {
					int k2 = designerKitIds[j2];
					if (k2 >= 0) {
						aclass30_sub2_sub4_sub6s[i2++] = IDK.cache[k2].buildBodyModel();
					}
				}

				Model model = new Model(i2, aclass30_sub2_sub4_sub6s);
				for (int l2 = 0; l2 < 5; l2++) {
					if (anIntArray990[l2] != 0) {
						model.recolor(anIntArrayArray1003[l2][0], anIntArrayArray1003[l2][anIntArray990[l2]]);
						if (l2 == 1) {
							model.recolor(anIntArray1204[0], anIntArray1204[anIntArray990[l2]]);
						}
					}
				}

				model.buildAnimationGroups();
				model.applyAnimationFrame(Animation.anims[myPlayer.idleAnimationId].frameIds[0]);
				model.applyLighting(64, 850, -30, -50, -30, true);
				class9.mediaType = 5;
				class9.mediaID = 0;
				RSInterface.replaceCachedModel(model, 0, 5);
			}
			return;
		}
		if (j == 324) {
			if (aClass30_Sub2_Sub1_Sub1_931 == null) {
				aClass30_Sub2_Sub1_Sub1_931 = class9.sprite1;
				aClass30_Sub2_Sub1_Sub1_932 = class9.sprite2;
			}
			if (designerMale) {
				class9.sprite1 = aClass30_Sub2_Sub1_Sub1_932;
				return;
			} else {
				class9.sprite1 = aClass30_Sub2_Sub1_Sub1_931;
				return;
			}
		}
		if (j == 325) {
			if (aClass30_Sub2_Sub1_Sub1_931 == null) {
				aClass30_Sub2_Sub1_Sub1_931 = class9.sprite1;
				aClass30_Sub2_Sub1_Sub1_932 = class9.sprite2;
			}
			if (designerMale) {
				class9.sprite1 = aClass30_Sub2_Sub1_Sub1_931;
				return;
			} else {
				class9.sprite1 = aClass30_Sub2_Sub1_Sub1_932;
				return;
			}
		}
		if (j == 600) {
			class9.disabledText = reportAbuseInput;
			if (loopCycle % 20 < 10) {
				class9.disabledText += "|";
				return;
			} else {
				class9.disabledText += " ";
				return;
			}
		}
		if (j == 613) {
			if (myPrivilege >= 1 && myPrivilege <= 3) {
				if (canMute) {
					class9.textColor = 0xff0000;
					class9.disabledText = "Moderator option: Mute player for 48 hours: <ON>";
				} else {
					class9.textColor = 0xffffff;
					class9.disabledText = "Moderator option: Mute player for 48 hours: <OFF>";
				}
			} else {
				class9.disabledText = "";
			}
		}
		if (j == 661)
			if (anInt1034 == 0)
				class9.disabledText = "\\nYou have not yet set any recovery questions.\\nIt is @lre@strongly@yel@ recommended that you do so.\\n\\nIf you don't you will be @lre@unable to recover your\\n@lre@password@yel@ if you forget it, or it is stolen.";
			else if (anInt1034 <= anInt1170) {
				class9.disabledText = "\\n\\nRecovery Questions Last Set:\\n@gre@"
						+ formatAccountDay(anInt1034);
			} else {
				int l1 = (anInt1170 + 14) - anInt1034;
				String s2;
				if (l1 <= 0)
					s2 = "Earlier today";
				else if (l1 == 1)
					s2 = "Yesterday";
				else
					s2 = l1 + " days ago";
				class9.disabledText = s2
						+ " you requested@lre@ new recovery\\n@lre@questions.@yel@ The requested change will occur\\non: @lre@"
						+ formatAccountDay(anInt1034)
						+ "\\n\\nIf you do not remember making this request\\ncancel it immediately, and change your password.";
			}
		if (j == 663)
			if (anInt1083 <= 0 || anInt1083 > anInt1170 + 10)
				class9.disabledText = "Last password change:\\n@gre@Never changed";
			else
				class9.disabledText = "Last password change:\\n@gre@"
						+ formatAccountDay(anInt1083);
		if (j == 668) {
			if (anInt1034 > anInt1170) {
				class9.disabledText = "To cancel this request:\\n1) Logout and return to the frontpage of this website.\\n2) Choose 'Cancel recovery questions'.";
				return;
			}
			class9.disabledText = "To change your recovery questions:\\n1) Logout and return to the frontpage of this website.\\n2) Choose 'Set new recovery questions'.";
		}
	}
	
	public String formatAccountDay(int dayCount) {
		if (dayCount > anInt1170 + 10) {
			return "Unknown";
		} else {
			long timestampMillis = ((long) dayCount + 11745L) * 0x5265c00L;
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date(timestampMillis));
			int dayOfMonth = calendar.get(5);
			int monthIndex = calendar.get(2);
			int year = calendar.get(1);
			String monthAbbreviations[] = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul",
					"Aug", "Sep", "Oct", "Nov", "Dec" };
			return dayOfMonth + "-" + monthAbbreviations[monthIndex] + "-" + year;
		}
	}

	public void drawSplitpublicChat() {
		if (splitpublicChat == 0) {
			return;
		}
		TextDrawingArea textDrawingArea = aTextDrawingArea_1271;
		int i = 0;
		if (anInt1104 != 0) {
			i = 1;
		}
		for (int j = 0; j < 100; j++) {
			if (chatMessages[j] != null) {
				int k = chatTypes[j];
				String s = chatNames[j];
				byte byte1 = 0;
				if (s != null && s.startsWith("@cr1@")) {
					s = s.substring(5);
					byte1 = 1;
				}
				if (s != null && s.startsWith("@cr2@")) {
					s = s.substring(5);
					byte1 = 2;
				}
				if ((k == 3 || k == 7) && (k == 7 || privateChatMode == 0 || privateChatMode == 1 && isFriendOrSelf(s))) {
					int l = 329 - i * 13;
					int k1 = 4;
					textDrawingArea.drawLiteralText(0, "From", l, k1);
					textDrawingArea.drawLiteralText(0x00ffff, "From", l - 1, k1);
					k1 += textDrawingArea.measureTaggedTextWidth("From ");
					if (byte1 == 1) {
						modIcons[0].drawNonzeroIndexed(k1, l - 12);
						k1 += 14;
					}
					if (byte1 == 2) {
						modIcons[1].drawNonzeroIndexed(k1, l - 12);
						k1 += 14;
					}
					textDrawingArea.drawLiteralText(0, s + ": " + chatMessages[j], l, k1);
					textDrawingArea.drawLiteralText(0x00ffff, s + ": " + chatMessages[j], l - 1, k1);
					if (++i >= 5) {
						return;
					}
				}
				if (k == 5 && privateChatMode < 2) {
					int i1 = 329 - i * 13;
					textDrawingArea.drawLiteralText(0, chatMessages[j], i1, 4);
					textDrawingArea.drawLiteralText(0x00ffff, chatMessages[j], i1 - 1, 4);
					if (++i >= 5) {
						return;
					}
				}
				if (k == 6 && privateChatMode < 2) {
					int j1 = 329 - i * 13;
					textDrawingArea.drawLiteralText(0, "To " + s + ": " + chatMessages[j], j1, 4);
					textDrawingArea.drawLiteralText(0x00ffff, "To " + s + ": " + chatMessages[j], j1 - 1, 4);
					if (++i >= 5) {
						return;
					}
				}
			}
		}

	}
	public void screenshot(boolean sendMessage, String... subfolders) {
		if (ClientSettings.ANDROID) {
			AndroidHooks.takeScreenshot(this, sendMessage, subfolders);
		} else {
			DesktopHooks.takeScreenshot(this, sendMessage, subfolders);
		}
	}
	public void pushMessage(String message, int chatType, String senderName) {
		if (chatType == 0 && dialogID != -1) {
			aString844 = message;
			super.clickMode3 = 0;
		}
		if (backDialogID == -1) {
			inputTaken = true;
		}
		for (int slot = 99; slot > 0; slot--) {
			chatTypes[slot] = chatTypes[slot - 1];
			chatNames[slot] = chatNames[slot - 1];
			chatMessages[slot] = chatMessages[slot - 1];
		}

		chatTypes[0] = chatType;
		chatNames[0] = senderName;
		chatMessages[0] = message;
	}
	
	public void processMinimapActions() {
        int x = super.mouseX;
        int y = super.mouseY;
        if (x >= 551 && x <= 577 && y >= 7 && y <= 40) {
            menuActionName[1] = "Face North";
            menuActionID[1] = 696;
            menuActionRow = 2;
        }
	}

	int customTabAction = 0;
	static boolean customSettingVisiblePlayerNames = false;
	static int customSettingMinItemValue = Integer.MAX_VALUE;
	boolean customSettingShowExperiencePerHour = false;
	long customSettingShowExperiencePerHourStartExp = 0;
	long customSettingShowExperiencePerHourStart = System.currentTimeMillis();
	int customSettingShowExperiencePerHourStartLevels = 0;
	boolean customSettingVisualFixes = true;

	public void processTabClick() {
		if (super.clickMode3 == 1) {
			if (super.saveClickX >= 539 && super.saveClickX <= 573 && super.saveClickY >= 169 && super.saveClickY < 205 && tabInterfaceIDs[0] != -1) {
				needDrawTabArea = true;
				tabID = 0;
				tabAreaAltered = true;
			}
			if (super.saveClickX >= 569 && super.saveClickX <= 599 && super.saveClickY >= 168 && super.saveClickY < 205 && tabInterfaceIDs[1] != -1) {
				needDrawTabArea = true;
				tabID = 1;
				tabAreaAltered = true;
				if(ClientSettings.SCREENSHOTS_ENABLED && ClientSettings.AUTOMATIC_SCREENSHOTS_ENABLED) {
					java.util.Timer timer = new java.util.Timer();
					java.util.TimerTask delayedScreenshot = new java.util.TimerTask() {
						@Override
						public void run() {
							screenshot(false, "stats");
						}
					};
					timer.schedule(delayedScreenshot, 300);
				}
			}
			if (super.saveClickX >= 597 && super.saveClickX <= 627 && super.saveClickY >= 168 && super.saveClickY < 205 && tabInterfaceIDs[2] != -1) {
				needDrawTabArea = true;
				tabID = 2;
				tabAreaAltered = true;
			}
			if (super.saveClickX >= 625 && super.saveClickX <= 669 && super.saveClickY >= 168 && super.saveClickY < 203 && tabInterfaceIDs[3] != -1) {
				needDrawTabArea = true;
				tabID = 3;
				tabAreaAltered = true;
			}
			if (super.saveClickX >= 666 && super.saveClickX <= 696 && super.saveClickY >= 168 && super.saveClickY < 205 && tabInterfaceIDs[4] != -1) {
				needDrawTabArea = true;
				tabID = 4;
				tabAreaAltered = true;
			}
			if (super.saveClickX >= 694 && super.saveClickX <= 724 && super.saveClickY >= 168 && super.saveClickY < 205 && tabInterfaceIDs[5] != -1) {
				needDrawTabArea = true;
				tabID = 5;
				tabAreaAltered = true;
			}
			if (super.saveClickX >= 722 && super.saveClickX <= 756 && super.saveClickY >= 169 && super.saveClickY < 205 && tabInterfaceIDs[6] != -1) {
				needDrawTabArea = true;
				tabID = 6;
				tabAreaAltered = true;
			}
			if (super.saveClickX >= 540 && super.saveClickX <= 574 && super.saveClickY >= 466 && super.saveClickY < 502 && tabInterfaceIDs[7] != -1 && ClientSettings.CUSTOM_SETTINGS_TAB) {
				/* Unused tab bottom left */
				needDrawTabArea = true;
				tabID = 7;
				tabAreaAltered = true;
			}
			if (super.saveClickX >= 572 && super.saveClickX <= 602 && super.saveClickY >= 466 && super.saveClickY < 503 && tabInterfaceIDs[8] != -1) {
				needDrawTabArea = true;
				tabID = 8;
				tabAreaAltered = true;
			}
			if (super.saveClickX >= 599 && super.saveClickX <= 629 && super.saveClickY >= 466 && super.saveClickY < 503 && tabInterfaceIDs[9] != -1) {
				needDrawTabArea = true;
				tabID = 9;
				tabAreaAltered = true;
			}
			if (super.saveClickX >= 627 && super.saveClickX <= 671 && super.saveClickY >= 467 && super.saveClickY < 502 && tabInterfaceIDs[10] != -1) {
				needDrawTabArea = true;
				tabID = 10;
				tabAreaAltered = true;
			}
			if (super.saveClickX >= 669 && super.saveClickX <= 699 && super.saveClickY >= 466 && super.saveClickY < 503 && tabInterfaceIDs[11] != -1) {
				needDrawTabArea = true;
				tabID = 11;
				tabAreaAltered = true;
			}
			if (super.saveClickX >= 696 && super.saveClickX <= 726 && super.saveClickY >= 466 && super.saveClickY < 503 && tabInterfaceIDs[12] != -1) {
				needDrawTabArea = true;
				tabID = 12;
				tabAreaAltered = true;
			}
			if (super.saveClickX >= 724 && super.saveClickX <= 758 && super.saveClickY >= 466 && super.saveClickY < 502 && tabInterfaceIDs[13] != -1) {
				needDrawTabArea = true;
				tabID = 13;
				tabAreaAltered = true;
			}
			if (invOverlayInterfaceID == -1) {
				if (tabInterfaceIDs[tabID] != -1) {
					// Handle our custom tab
					if (tabID == 7 && ClientSettings.CUSTOM_SETTINGS_TAB && super.saveClickX >= 575 && super.saveClickX <= 720 && super.saveClickY >= 210 && super.saveClickY <= 465) {
						int startY = 217 + 3;
						if (super.saveClickY >= startY && super.saveClickY <= (startY + 30)) {
							customSettingVisiblePlayerNames = !customSettingVisiblePlayerNames;
						}
						startY += 40;
						if (super.saveClickY >= startY && super.saveClickY <= (startY + 30)) {
							inputTaken = true;
							inputDialogState = 0;
							messagePromptRaised = true;
							promptInput = "";
							aString1121 = "Enter minimum item value";
							customTabAction = 1;
						}
						startY += 40;
						if (super.saveClickY >= startY && super.saveClickY <= (startY + 30)) {
							inputTaken = true;
							inputDialogState = 0;
							messagePromptRaised = true;
							promptInput = "";
							aString1121 = "Enter new draw distance";
							customTabAction = 2;
						}
						startY += 40;
						if (super.saveClickY >= startY && super.saveClickY <= (startY + 30)) {
							customSettingShowExperiencePerHour= !customSettingShowExperiencePerHour;
							customSettingShowExperiencePerHourStart = System.currentTimeMillis();
							customSettingShowExperiencePerHourStartExp = calculateTotalExp();
							customSettingShowExperiencePerHourStartLevels = calculateTotalLevels();
						}
						startY += 40;
						if (super.saveClickY >= startY && super.saveClickY <= (startY + 30)) {
							showInfo= !showInfo;
						}
						startY += 40;
						if (super.saveClickY >= startY && super.saveClickY <= (startY + 30)) {
							customSettingVisualFixes = !customSettingVisualFixes;
							ClientSettings.BILINEAR_MINIMAP_FILTERING = !ClientSettings.BILINEAR_MINIMAP_FILTERING;
							ClientSettings.FIX_TRANSPARENCY_OVERFLOW = !ClientSettings.FIX_TRANSPARENCY_OVERFLOW;
							ClientSettings.FULL_512PX_VIEWPORT = !ClientSettings.FULL_512PX_VIEWPORT;
						}
					}
				}
			}
			if (anInt1054 == tabID) {
				stream.writeEncryptedOpcode(152);
				stream.writeByte(tabID);
			}
		}
	}
	
	public void resetImageProducers() {
		if (aRSImageProducer_1107 != null) {
			return;
		}
		super.fullGameScreen = null;
		aRSImageProducer_1166 = null;
		aRSImageProducer_1164 = null;
		aRSImageProducer_1163 = null;
		aRSImageProducer_1165 = null;
		aRSImageProducer_1123 = null;
		aRSImageProducer_1124 = null;
		aRSImageProducer_1125 = null;
		aRSImageProducer_1110 = new RSImageProducer(128, 265, getGameComponent());
		DrawingArea.clearRaster();
		aRSImageProducer_1111 = new RSImageProducer(128, 265, getGameComponent());
		DrawingArea.clearRaster();
		aRSImageProducer_1107 = new RSImageProducer(509, 171, getGameComponent());
		DrawingArea.clearRaster();
		aRSImageProducer_1108 = new RSImageProducer(360, 132, getGameComponent());
		DrawingArea.clearRaster();
		aRSImageProducer_1109 = new RSImageProducer(360, 200, getGameComponent());
		DrawingArea.clearRaster();
		aRSImageProducer_1112 = new RSImageProducer(202, 238, getGameComponent());
		DrawingArea.clearRaster();
		aRSImageProducer_1113 = new RSImageProducer(203, 238, getGameComponent());
		DrawingArea.clearRaster();
		aRSImageProducer_1114 = new RSImageProducer(74, 94, getGameComponent());
		DrawingArea.clearRaster();
		aRSImageProducer_1115 = new RSImageProducer(75, 94, getGameComponent());
		DrawingArea.clearRaster();
		if (titleStreamLoader != null) {
			drawLogo();
			loadTitleScreen();
		}
		welcomeScreenRaised = true;
	}
	
	public void resetAllImageProducers() {
		if (super.fullGameScreen != null)
			return;
		nullLoader();
		aRSImageProducer_1107 = null;
		aRSImageProducer_1108 = null;
		aRSImageProducer_1109 = null;
		aRSImageProducer_1112 = null;
		aRSImageProducer_1113 = null;
		aRSImageProducer_1114 = null;
		aRSImageProducer_1115 = null;
		aRSImageProducer_1166 = null;
		aRSImageProducer_1164 = null;
		aRSImageProducer_1163 = null;
		aRSImageProducer_1165 = null;
		aRSImageProducer_1123 = null;
		aRSImageProducer_1125 = null;
		aRSImageProducer_1124 = null;
		super.fullGameScreen = new RSImageProducer(765, 503, getGameComponent());
		welcomeScreenRaised = true;
	}

	public void resetImageProducers2() {
		if (aRSImageProducer_1166 != null) {
			return;
		}
		nullLoader();
		super.fullGameScreen = null;
		aRSImageProducer_1107 = null;
		aRSImageProducer_1108 = null;
		aRSImageProducer_1109 = null;
		aRSImageProducer_1110 = null;
		aRSImageProducer_1111 = null;
		aRSImageProducer_1112 = null;
		aRSImageProducer_1113 = null;
		aRSImageProducer_1114 = null;
		aRSImageProducer_1115 = null;
		aRSImageProducer_1166 = new RSImageProducer(479, 96, getGameComponent());
		aRSImageProducer_1164 = new RSImageProducer(172, 156, getGameComponent());
		DrawingArea.clearRaster();
		mapBack.drawNonzeroIndexed(0, 0);
		aRSImageProducer_1163 = new RSImageProducer(190, 261, getGameComponent());
		aRSImageProducer_1165 = new RSImageProducer(512, 334, getGameComponent());
		DrawingArea.clearRaster();
		aRSImageProducer_1123 = new RSImageProducer(496, 50, getGameComponent());
		aRSImageProducer_1124 = new RSImageProducer(269, 37, getGameComponent());
		aRSImageProducer_1125 = new RSImageProducer(249, 45, getGameComponent());
		welcomeScreenRaised = true;
		aRSImageProducer_1165.setAsDrawingArea();
		Texture.lineOffsets = chatBoxAreaOffsets;
		// SignLink.midii.fadeOut();
	}

	public void drawMinimapHint(Sprite sprite, int offsetY, int offsetX) {
		int distanceSquared = offsetX * offsetX + offsetY * offsetY;
		if (distanceSquared > 4225 && distanceSquared < 90000) {
			int rotation2048 = minimapInt1 + minimapInt2 & 0x7ff;
			int scaledSine16 = Model.sineTable16[rotation2048];
			int scaledCosine16 = Model.cosineTable16[rotation2048];
			scaledSine16 = (scaledSine16 * 256) / (minimapInt3 + 256);
			scaledCosine16 = (scaledCosine16 * 256) / (minimapInt3 + 256);
			int rotatedX = offsetY * scaledSine16 + offsetX * scaledCosine16 >> 16;
			int rotatedY = offsetY * scaledCosine16 - offsetX * scaledSine16 >> 16;
			double arrowAngleRadians = Math.atan2((double) rotatedX, (double) rotatedY);
			int ellipseOffsetX = (int) (Math.sin(arrowAngleRadians) * 63D);
			int ellipseOffsetY = (int) (Math.cos(arrowAngleRadians) * 57D);
			minimapEdgeArrow.drawRotatedNonzero20x20(83 - ellipseOffsetY - 20, arrowAngleRadians, 94 + ellipseOffsetX + 4 - 10);
		} else {
			drawMinimapMarker(sprite, offsetX, offsetY);
		}
	}

	public void processRightClick() {
		if (activeInterfaceType != 0) {
			return;
		}
		menuActionName[0] = "Cancel";
		menuActionID[0] = 1107;
		menuActionRow = 1;
		 if (fullScreenInterfaceId != -1) {
			anInt886 = 0;
			buildInterfaceMenu(0, RSInterface.interfaceCache[fullScreenInterfaceId], super.mouseX, 0, super.mouseY, 0);
			if (anInt886 != anInt1026) {
				anInt1026 = anInt886;
			}
			return;
		}
		buildSplitPrivateChatMenu();
		anInt886 = 0;
		if (super.mouseX > 4 && super.mouseY > 4 && super.mouseX < 516 && super.mouseY < 338) {
			if (openInterfaceID != -1) {
				buildInterfaceMenu(4, RSInterface.interfaceCache[openInterfaceID], super.mouseX, 4, super.mouseY, 0);
			} else {
				build3dScreenMenu();
			}
		}
		if (anInt886 != anInt1026) {
			anInt1026 = anInt886;
		}
		anInt886 = 0;
		if (super.mouseX > 553 && super.mouseY > 205 && super.mouseX < 743 && super.mouseY < 466) {
			if (invOverlayInterfaceID != -1) {
				buildInterfaceMenu(553, RSInterface.interfaceCache[invOverlayInterfaceID], super.mouseX, 205, super.mouseY, 0);
			} else if (tabInterfaceIDs[tabID] != -1) {
				buildInterfaceMenu(553, RSInterface.interfaceCache[tabInterfaceIDs[tabID]], super.mouseX, 205, super.mouseY, 0);
			}
		}
		if (anInt886 != anInt1048) {
			needDrawTabArea = true;
			anInt1048 = anInt886;
		}
		anInt886 = 0;
		if (super.mouseX > 17 && super.mouseY > 357 && super.mouseX < 496 && super.mouseY < 453) {
			if (backDialogID != -1) {
				buildInterfaceMenu(17, RSInterface.interfaceCache[backDialogID], super.mouseX, 357, super.mouseY, 0);
			} else if (dialogID != -1) {
				buildInterfaceMenu(17, RSInterface.interfaceCache[dialogID], super.mouseX, 357, super.mouseY, 0);
			} else if (super.mouseY < 434 && super.mouseX < 426) {
				buildChatAreaMenu(super.mouseY - 357);
			}
		}
		if ((backDialogID != -1 || dialogID != -1) && anInt886 != anInt1039) {//TODO remove if any issues
			inputTaken = true;
			anInt1039 = anInt886;
		}
		processMinimapActions();
		boolean flag = false;
		while (!flag) {
			flag = true;
			for (int j = 0; j < menuActionRow - 1; j++) {
				if (menuActionID[j] < 1000 && menuActionID[j + 1] > 1000) {
					String s = menuActionName[j];
					menuActionName[j] = menuActionName[j + 1];
					menuActionName[j + 1] = s;
					int k = menuActionID[j];
					menuActionID[j] = menuActionID[j + 1];
					menuActionID[j + 1] = k;
					k = menuActionCmd2[j];
					menuActionCmd2[j] = menuActionCmd2[j + 1];
					menuActionCmd2[j + 1] = k;
					k = menuActionCmd3[j];
					menuActionCmd3[j] = menuActionCmd3[j + 1];
					menuActionCmd3[j + 1] = k;
					k = menuActionCmd1[j];
					menuActionCmd1[j] = menuActionCmd1[j + 1];
					menuActionCmd1[j + 1] = k;
					flag = false;
				}
			}

		}
	}

	public int blendRgb256(int firstRgb, int secondRgb, int secondWeight) {
		int firstWeight = 256 - secondWeight;
		return ((firstRgb & 0xff00ff) * firstWeight + (secondRgb & 0xff00ff) * secondWeight & 0xff00ff00) + ((firstRgb & 0xff00) * firstWeight + (secondRgb & 0xff00) * secondWeight & 0xff0000) >> 8;
	}

	public void login(String s, String s1, boolean flag) {
		Signlink.errorname = s;
		try {
			if (!flag) {
				loginMessage1 = "";
				loginMessage2 = "Connecting to server...";
				drawLoginScreen(true);
			}
			socketStream = new RSSocket(this, openSocket((ClientSettings.SERVER_WORLD == 1) ? 43594 : 43596 + ClientSettings.SERVER_WORLD + portOff));
			long l = TextClass.encodeBase37Name(s);
			int i = (int) (l >> 16 & 31L);
			stream.currentOffset = 0;
			stream.writeByte(14);
			stream.writeByte(i);
			socketStream.queueBytes(2, stream.buffer);
			for (int j = 0; j < 8; j++) {
				socketStream.read();
			}

			int k = socketStream.read();
			int i1 = k;
			if (k == 0) {
				socketStream.readFully(inStream.buffer, 8);
				inStream.currentOffset = 0;
				aLong1215 = inStream.readLongBigEndian();
				int ai[] = new int[4];
				ai[0] = (int) (Math.random() * 99999999D);
				ai[1] = (int) (Math.random() * 99999999D);
				ai[2] = (int) (aLong1215 >> 32);
				ai[3] = (int) aLong1215;
				stream.currentOffset = 0;
				stream.writeByte(10);
				stream.writeIntBigEndian(ai[0]);
				stream.writeIntBigEndian(ai[1]);
				stream.writeIntBigEndian(ai[2]);
				stream.writeIntBigEndian(ai[3]);
				stream.writeIntBigEndian(ClientSettings.UID);
				stream.writeLfTerminatedString(s);
				stream.writeLfTerminatedString(s1);
				stream.encryptRsaBlock();
				aStream_847.currentOffset = 0;
				if (flag) {
					aStream_847.writeByte(18);
				} else {
					aStream_847.writeByte(16);
				}
				aStream_847.writeByte(stream.currentOffset + 36 + 1 + 1 + 2);
				aStream_847.writeByte(255);
				aStream_847.writeShortBigEndian(1);
				aStream_847.writeByte(lowMem ? 1 : 0);
				for (int l1 = 0; l1 < 9; l1++) {
					aStream_847.writeIntBigEndian(expectedCRCs[l1]);
				}

				aStream_847.writeBytes(stream.buffer, stream.currentOffset, 0);
				stream.encryption = new ISAACRandomGen(ai);
				for (int j2 = 0; j2 < 4; j2++) {
					ai[j2] += 50;
				}

				encryption = new ISAACRandomGen(ai);
				socketStream.queueBytes(aStream_847.currentOffset, aStream_847.buffer);
				k = socketStream.read();
			}
			if (k == 1) {
				try {
					Thread.sleep(2000L);
				} catch (Exception _ex) {
				}
				login(s, s1, flag);
				return;
			}
			if (k == 2) {
				myPrivilege = socketStream.read();
				flagged = socketStream.read() == 1;
				aLong1220 = 0L;
				anInt1022 = 0;
				mouseDetection.sampleCount = 0;
				super.awtFocus = true;
				aBoolean954 = true;
				loggedIn = true;
				stream.currentOffset = 0;
				inStream.currentOffset = 0;
				pktType = -1;
				anInt841 = -1;
				prevPktType = -1;
				prevPktType2 = -1;
				pktSize = 0;
				anInt1009 = 0;
				anInt1104 = 0;
				anInt1011 = 0;
				anInt855 = 0;
				menuActionRow = 0;
				menuOpen = false;
				super.idleTime = 0;
				for (int j1 = 0; j1 < 100; j1++) {
					chatMessages[j1] = null;
				}

				itemSelected = 0;
				spellSelected = 0;
				loadingStage = 0;
				currentSound = 0;
				anInt1278 = (int) (Math.random() * 100D) - 50;
				anInt1131 = (int) (Math.random() * 110D) - 55;
				anInt896 = (int) (Math.random() * 80D) - 40;
				minimapInt2 = (int) (Math.random() * 120D) - 60;
				minimapInt3 = (int) (Math.random() * 30D) - 20;
				minimapInt1 = (int) (Math.random() * 20D) - 10 & 0x7ff;
				anInt1021 = 0;
				minimapBuiltPlane = -1;
				destX = 0;
				destY = 0;
				playerCount = 0;
				npcCount = 0;
				for (int i2 = 0; i2 < maxPlayers; i2++) {
					playerArray[i2] = null;
					aStreamArray895s[i2] = null;
				}

				for (int k2 = 0; k2 < 16384; k2++) {
					npcArray[k2] = null;
				}

				myPlayer = playerArray[myPlayerIndex] = new Player();
				projectiles.removeAll();
				transientGraphics.removeAll();
				for (int l2 = 0; l2 < 4; l2++) {
					for (int i3 = 0; i3 < 104; i3++) {
						for (int k3 = 0; k3 < 104; k3++) {
							groundArray[l2][i3][k3] = null;
						}
					}
				}

				pendingSceneChanges = new NodeList();
				anInt900 = 0;
				friendsCount = 0;
				dialogID = -1;
				backDialogID = -1;
				openInterfaceID = -1;
				invOverlayInterfaceID = -1;
				fullScreenInterfaceId = -1;
				anInt1018 = -1;
				aBoolean1149 = false;
				tabID = 3;
				inputDialogState = 0;
				menuOpen = false;
				messagePromptRaised = false;
				aString844 = null;
				anInt1055 = 0;
				anInt1054 = -1;
				designerMale = true;
				selectDefaultDesignerKits();
				for (int j3 = 0; j3 < 5; j3++) {
					anIntArray990[j3] = 0;
				}

				for (int l3 = 0; l3 < 5; l3++) {
					atPlayerActions[l3] = null;
					atPlayerArray[l3] = false;
				}

				anInt1175 = 0;
				anInt1134 = 0;
				anInt986 = 0;
				waypointsSinceKeepAlive = 0;
				anInt924 = 0;
				anInt1188 = 0;
				anInt1155 = 0;
				anInt1226 = 0;
				resetImageProducers2();
				return;
			}
			if (k == 3) {
				loginMessage1 = "";
				loginMessage2 = "Invalid username or password.";
				return;
			}
			if (k == 4) {
				loginMessage1 = "Your account has been disabled.";
				loginMessage2 = "Or you entered an invalid character for user/pass";
				return;
			}
			if (k == 5) {
				loginMessage1 = "Your account is already logged in.";
				loginMessage2 = "Try again in 60 secs...";
				return;
			}
			if (k == 6) {
				loginMessage1 = "" + ClientSettings.SERVER_NAME + " has been updated!";
				loginMessage2 = "Please reload this page.";
				return;
			}
			if (k == 7) {
				loginMessage1 = "This world is full.";
				loginMessage2 = "Please use a different world.";
				return;
			}
			if (k == 8) {
				loginMessage1 = "Unable to connect.";
				loginMessage2 = "Login server offline.";
				return;
			}
			if (k == 9) {
				loginMessage1 = "Login limit exceeded.";
				loginMessage2 = "Too many connections from your address.";
				return;
			}
			if (k == 10) {
				loginMessage1 = "Unable to connect.";
				loginMessage2 = "Bad session id.";
				return;
			}
			if (k == 11) {
				loginMessage1 = "Login server rejected session.";
				loginMessage2 = "Please try again.";
				return;
			}
			if (k == 12) {
				loginMessage1 = "Only staff are allowed to play right now.";
				loginMessage2 = "Please login using a staff account.";
				//loginMessage1 = "You need a members account to login to this world.";
				//loginMessage2 = "Please subscribe, or use a different world.";
				return;
			}
			if (k == 13) {
				loginMessage1 = "Could not complete login.";
				loginMessage2 = "Please try using a different world.";
				return;
			}
			if (k == 14) {
				loginMessage1 = "The server is being updated.";
				loginMessage2 = "Please wait 1 minute and try again.";
				return;
			}
			if (k == 15) {
				loggedIn = true;
				stream.currentOffset = 0;
				inStream.currentOffset = 0;
				pktType = -1;
				anInt841 = -1;
				prevPktType = -1;
				prevPktType2 = -1;
				pktSize = 0;
				anInt1009 = 0;
				anInt1104 = 0;
				menuActionRow = 0;
				menuOpen = false;
				aLong824 = System.currentTimeMillis();
				return;
			}
			if (k == 16) {
				loginMessage1 = "Login attempts exceeded.";
				loginMessage2 = "Please wait 1 minute and try again.";
				return;
			}
			if (k == 17) {
				loginMessage1 = "You are standing in a members-only area.";
				loginMessage2 = "To play on this world move to a free area first";
				return;
			}
			if (k == 20) {
				loginMessage1 = "Invalid loginserver requested";
				loginMessage2 = "Please try using a different world.";
				return;
			}
			if (k == 30) {
				loginMessage1 = "You need a forum account to play.";
				loginMessage2 = "Go to " + ClientSettings.SERVER_WEBSITE + " to register.";
				return;
			}
			if (k == 31) {
				loginMessage1 = "You're using an invalid or outdated client.";
				loginMessage2 = "Get the latest version at " + ClientSettings.SERVER_WEBSITE + "";
				return;
			}
			if (k == 32) {
				loginMessage1 = "You need to recover your account first,";
				loginMessage2 = "go to " + ClientSettings.SERVER_WEBSITE + "/help for a tutorial!";
				return;
			}
			if (k == 33) {
				loginMessage1 = "You need a members account to login to this world.";
				loginMessage2 = "Please subscribe, or use a different world.";
				return;
			}
			if (k == 34) {
				loginMessage1 = "You need to activate your forum account first.";
				loginMessage2 = "Click on confirm in the email we have send you!";
				return;
			}
			if (k == 21) {
				for (int k1 = socketStream.read(); k1 >= 0; k1--) {
					loginMessage1 = "You have only just left another world";
					loginMessage2 = "Your profile will be transferred in: " + k1 + " seconds";
					drawLoginScreen(true);
					try {
						Thread.sleep(1000L);
					} catch (Exception _ex) {
					}
				}

				login(s, s1, flag);
				return;
			}
			if (k == -1) {
				if (i1 == 0) {
					if (loginFailures < 2) {
						try {
							Thread.sleep(2000L);
						} catch (Exception _ex) {
						}
						loginFailures++;
						login(s, s1, flag);
						return;
					} else {
						loginMessage1 = "Error connecting to server.";
						loginMessage2 = "Please try again in a little while.";
						return;
					}
				} else {
					loginMessage1 = "No response from server";
					loginMessage2 = "Please try closing and opening your client again.";
					return;
				}
			} else {
				System.out.println("response:" + k);
				loginMessage1 = "Unexpected server response";
				loginMessage2 = "Please try using a different world.";
				return;
			}
		} catch (IOException _ex) {
			loginMessage1 = "";
		}
		loginMessage2 = "Error connecting to server.";
	}

	public boolean doWalkTo(int walkType, int orientation, int sizeY, int shapePlusOne, int startY, int sizeX, int blockedSidesMask, int targetY, int startX, boolean approximate, int targetX) {
		byte gridWidth = 104;
		byte gridHeight = 104;
		for (int clearX = 0; clearX < gridWidth; clearX++) {
			for (int clearY = 0; clearY < gridHeight; clearY++) {
				pathBackDirection[clearX][clearY] = 0;
				pathDistance[clearX][clearY] = 0x5f5e0ff;
			}

		}
		int currentX = startX;
		int currentY = startY;
		pathBackDirection[startX][startY] = 99;
		pathDistance[startX][startY] = 0;
		int queueTail = 0;
		int queueHead = 0;
		bigX[queueTail] = startX;
		bigY[queueTail++] = startY;
		boolean reachedTarget = false;
		int queueCapacity = bigX.length;
		int tileFlags[][] = aClass11Array1230[plane].tileFlags;
		while (queueHead != queueTail) {
			currentX = bigX[queueHead];
			currentY = bigY[queueHead];
			queueHead = (queueHead + 1) % queueCapacity;
			if (currentX == targetX && currentY == targetY) {
				reachedTarget = true;
				break;
			}
			if (shapePlusOne != 0) {
				if ((shapePlusOne < 5 || shapePlusOne == 10) && aClass11Array1230[plane].reachedWallObject(targetX, currentX, currentY, orientation, shapePlusOne - 1, targetY)) {
					reachedTarget = true;
					break;
				}
				if (shapePlusOne < 10 && aClass11Array1230[plane].reachedWallDecoration(targetX, targetY, currentY, shapePlusOne - 1, orientation, currentX)) {
					reachedTarget = true;
					break;
				}
			}
			if (sizeX != 0 && sizeY != 0 && aClass11Array1230[plane].reachedObjectFootprint(targetY, targetX, currentX, sizeY, blockedSidesMask, sizeX, currentY)) {
				reachedTarget = true;
				break;
			}
			int nextDistance = pathDistance[currentX][currentY] + 1;
			if (currentX > 0 && pathBackDirection[currentX - 1][currentY] == 0 && (tileFlags[currentX - 1][currentY] & 0x1280108) == 0) {
				bigX[queueTail] = currentX - 1;
				bigY[queueTail] = currentY;
				queueTail = (queueTail + 1) % queueCapacity;
				pathBackDirection[currentX - 1][currentY] = 2;
				pathDistance[currentX - 1][currentY] = nextDistance;
			}
			if (currentX < gridWidth - 1 && pathBackDirection[currentX + 1][currentY] == 0 && (tileFlags[currentX + 1][currentY] & 0x1280180) == 0) {
				bigX[queueTail] = currentX + 1;
				bigY[queueTail] = currentY;
				queueTail = (queueTail + 1) % queueCapacity;
				pathBackDirection[currentX + 1][currentY] = 8;
				pathDistance[currentX + 1][currentY] = nextDistance;
			}
			if (currentY > 0 && pathBackDirection[currentX][currentY - 1] == 0 && (tileFlags[currentX][currentY - 1] & 0x1280102) == 0) {
				bigX[queueTail] = currentX;
				bigY[queueTail] = currentY - 1;
				queueTail = (queueTail + 1) % queueCapacity;
				pathBackDirection[currentX][currentY - 1] = 1;
				pathDistance[currentX][currentY - 1] = nextDistance;
			}
			if (currentY < gridHeight - 1 && pathBackDirection[currentX][currentY + 1] == 0 && (tileFlags[currentX][currentY + 1] & 0x1280120) == 0) {
				bigX[queueTail] = currentX;
				bigY[queueTail] = currentY + 1;
				queueTail = (queueTail + 1) % queueCapacity;
				pathBackDirection[currentX][currentY + 1] = 4;
				pathDistance[currentX][currentY + 1] = nextDistance;
			}
			if (currentX > 0 && currentY > 0 && pathBackDirection[currentX - 1][currentY - 1] == 0 && (tileFlags[currentX - 1][currentY - 1] & 0x128010e) == 0 && (tileFlags[currentX - 1][currentY] & 0x1280108) == 0 && (tileFlags[currentX][currentY - 1] & 0x1280102) == 0) {
				bigX[queueTail] = currentX - 1;
				bigY[queueTail] = currentY - 1;
				queueTail = (queueTail + 1) % queueCapacity;
				pathBackDirection[currentX - 1][currentY - 1] = 3;
				pathDistance[currentX - 1][currentY - 1] = nextDistance;
			}
			if (currentX < gridWidth - 1 && currentY > 0 && pathBackDirection[currentX + 1][currentY - 1] == 0 && (tileFlags[currentX + 1][currentY - 1] & 0x1280183) == 0 && (tileFlags[currentX + 1][currentY] & 0x1280180) == 0 && (tileFlags[currentX][currentY - 1] & 0x1280102) == 0) {
				bigX[queueTail] = currentX + 1;
				bigY[queueTail] = currentY - 1;
				queueTail = (queueTail + 1) % queueCapacity;
				pathBackDirection[currentX + 1][currentY - 1] = 9;
				pathDistance[currentX + 1][currentY - 1] = nextDistance;
			}
			if (currentX > 0 && currentY < gridHeight - 1 && pathBackDirection[currentX - 1][currentY + 1] == 0 && (tileFlags[currentX - 1][currentY + 1] & 0x1280138) == 0 && (tileFlags[currentX - 1][currentY] & 0x1280108) == 0 && (tileFlags[currentX][currentY + 1] & 0x1280120) == 0) {
				bigX[queueTail] = currentX - 1;
				bigY[queueTail] = currentY + 1;
				queueTail = (queueTail + 1) % queueCapacity;
				pathBackDirection[currentX - 1][currentY + 1] = 6;
				pathDistance[currentX - 1][currentY + 1] = nextDistance;
			}
			if (currentX < gridWidth - 1 && currentY < gridHeight - 1 && pathBackDirection[currentX + 1][currentY + 1] == 0 && (tileFlags[currentX + 1][currentY + 1] & 0x12801e0) == 0 && (tileFlags[currentX + 1][currentY] & 0x1280180) == 0 && (tileFlags[currentX][currentY + 1] & 0x1280120) == 0) {
				bigX[queueTail] = currentX + 1;
				bigY[queueTail] = currentY + 1;
				queueTail = (queueTail + 1) % queueCapacity;
				pathBackDirection[currentX + 1][currentY + 1] = 12;
				pathDistance[currentX + 1][currentY + 1] = nextDistance;
			}
		}
		walkDestinationAdjusted = 0;
		if (!reachedTarget) {
			if (approximate) {
				int bestDistance = 100;
				for (int radius = 1; radius < 2; radius++) {
					for (int searchX = targetX - radius; searchX <= targetX + radius; searchX++) {
						for (int searchY = targetY - radius; searchY <= targetY + radius; searchY++) {
							if (searchX >= 0 && searchY >= 0 && searchX < 104 && searchY < 104 && pathDistance[searchX][searchY] < bestDistance) {
								bestDistance = pathDistance[searchX][searchY];
								currentX = searchX;
								currentY = searchY;
								walkDestinationAdjusted = 1;
								reachedTarget = true;
							}
						}

					}

					if (reachedTarget) {
						break;
					}
				}

			}
			if (!reachedTarget) {
				return false;
			}
		}
		// queueHead is reused from here on as the number of stored waypoints
		queueHead = 0;
		bigX[queueHead] = currentX;
		bigY[queueHead++] = currentY;
		int previousStepDirection;
		for (int stepDirection = previousStepDirection = pathBackDirection[currentX][currentY]; currentX != startX || currentY != startY; stepDirection = pathBackDirection[currentX][currentY]) {
			if (stepDirection != previousStepDirection) {
				previousStepDirection = stepDirection;
				bigX[queueHead] = currentX;
				bigY[queueHead++] = currentY;
			}
			if ((stepDirection & 2) != 0) {
				currentX++;
			} else if ((stepDirection & 8) != 0) {
				currentX--;
			}
			if ((stepDirection & 1) != 0) {
				currentY++;
			} else if ((stepDirection & 4) != 0) {
				currentY--;
			}
		}
		// if(cancelWalk) { return queueHead > 0; }

		if (queueHead > 0) {
			int waypointCount = queueHead;
			if (waypointCount > 25) {
				waypointCount = 25;
			}
			queueHead--;
			int firstWaypointX = bigX[queueHead];
			int firstWaypointY = bigY[queueHead];
			waypointsSinceKeepAlive += waypointCount;
			if (waypointsSinceKeepAlive >= 92) {
				stream.writeEncryptedOpcode(36);
				stream.writeIntBigEndian(0);
				waypointsSinceKeepAlive = 0;
			}
			if (walkType == 0) {
				stream.writeEncryptedOpcode(164);
				stream.writeByte(waypointCount + waypointCount + 3);
			}
			if (walkType == 1) {
				stream.writeEncryptedOpcode(248);
				stream.writeByte(waypointCount + waypointCount + 3 + 14);
			}
			if (walkType == 2) {
				stream.writeEncryptedOpcode(98);
				stream.writeByte(waypointCount + waypointCount + 3);
			}
			stream.writeShortLittleEndianAdd128(firstWaypointX + baseX);
			destX = bigX[0];
			destY = bigY[0];
			for (int waypointIndex = 1; waypointIndex < waypointCount; waypointIndex++) {
				queueHead--;
				stream.writeByte(bigX[queueHead] - firstWaypointX);
				stream.writeByte(bigY[queueHead] - firstWaypointY);
			}

			stream.writeShortLittleEndian(firstWaypointY + baseY);
			stream.writeNegatedByte(super.keyArray[5] != 1 ? 0 : 1);
			return true;
		}
		return walkType != 1;
	}
	
	static final int method77(int i, int i_1_) {
		if (i_1_ == -2)
	   		return 12345678;
		if (i_1_ == -1) {
			if (i < 0)
				i = 0;
			else if (i > 127)
				i = 127;
			i = 127 - i;
			return i;
		}
		i = i * (i_1_ & 0x7f) / 128;
		if (i < 2)
			i = 2;
		else if (i > 126)
			i = 126;
		return (i_1_ & 0xff80) + i;
	}

	public void decodeNpcUpdateMasks(Stream packet) {
		for (int updateSlot = 0; updateSlot < maskUpdateCount; updateSlot++) {
			int npcIndex = maskUpdateIndices[updateSlot];
			NPC npc = npcArray[npcIndex];
			int mask = packet.readUnsignedByte();
			if ((mask & 0x10) != 0) {
				int animationId = packet.readUnsignedShortLittleEndian();
				if (animationId == 0x00ffff) {
					animationId = -1;
				}
				int animationDelay = packet.readUnsignedByte();
				if (animationId == npc.anim && animationId != -1) {
					int replayMode = Animation.anims[animationId].replayMode;
					if (replayMode == 1) {
						npc.anInt1527 = 0;
						npc.anInt1528 = 0;
						npc.anInt1529 = animationDelay;
						npc.anInt1530 = 0;
					}
					if (replayMode == 2) {
						npc.anInt1530 = 0;
					}
				} else if (animationId == -1 || npc.anim == -1 || Animation.anims[animationId].priority >= Animation.anims[npc.anim].priority) {
					npc.anim = animationId;
					npc.anInt1527 = 0;
					npc.anInt1528 = 0;
					npc.anInt1529 = animationDelay;
					npc.anInt1530 = 0;
					npc.anInt1542 = npc.smallXYIndex;
				}
			}
			if ((mask & 8) != 0) {
				int hitDamage = packet.readUnsignedByteSubtract128();
				int hitType = packet.readUnsignedNegatedByte();
				npc.updateHitData(hitType, hitDamage, loopCycle);
				npc.loopCycleStatus = loopCycle + 300;
				npc.currentHealth = packet.readUnsignedByteSubtract128();
				npc.maxHealth = packet.readUnsignedByte();
			}
			if ((mask & 0x80) != 0) {
				npc.anInt1520 = packet.readUnsignedShortBigEndian();
				int graphicTiming = packet.readIntBigEndian();
				npc.anInt1524 = graphicTiming >> 16;
				npc.anInt1523 = loopCycle + (graphicTiming & 0xffff);
				npc.anInt1521 = 0;
				npc.anInt1522 = 0;
				if (npc.anInt1523 > loopCycle) {
					npc.anInt1521 = -1;
				}
				if (npc.anInt1520 == 0x00ffff) {
					npc.anInt1520 = -1;
				}
			}
			if ((mask & 0x20) != 0) {
				npc.interactingEntity = packet.readUnsignedShortBigEndian();
				if (npc.interactingEntity == 0x00ffff) {
					npc.interactingEntity = -1;
				}
			}
			if ((mask & 1) != 0) {
				npc.textSpoken = packet.readLfTerminatedString();
				npc.textCycle = 100;
			}
			if ((mask & 0x40) != 0) {
				int secondHitDamage = packet.readUnsignedNegatedByte();
				int secondHitType = packet.readUnsignedByte128MinusValue();
				npc.updateHitData(secondHitType, secondHitDamage, loopCycle);
				npc.loopCycleStatus = loopCycle + 300;
				npc.currentHealth = packet.readUnsignedByte128MinusValue();
				npc.maxHealth = packet.readUnsignedNegatedByte();
			}
			if ((mask & 2) != 0) {
				npc.desc = EntityDef.forID(packet.readUnsignedShortLittleEndianSubtract128());
				npc.tileSize = npc.desc.tileSize;
				npc.turnSpeed = npc.desc.turnSpeed;
				npc.walkAnimationId = npc.desc.walkAnimationId;
				npc.reverseWalkAnimationId = npc.desc.reverseWalkAnimationId;
				npc.sidestepNegativeAnimationId = npc.desc.sidestepNegativeAnimationId;
				npc.sidestepPositiveAnimationId = npc.desc.sidestepPositiveAnimationId;
				npc.idleAnimationId = npc.desc.idleAnimationId;
			}
			if ((mask & 4) != 0) {
				npc.anInt1538 = packet.readUnsignedShortLittleEndian();
				npc.anInt1539 = packet.readUnsignedShortLittleEndian();
			}
		}
	}

	public void buildAtNPCMenu(EntityDef entityDef, int npcIndex, int tileY, int tileX) {
		if (menuActionRow >= 400) {
			return;
		}
		if (entityDef.childrenIDs != null) {
			entityDef = entityDef.getMorphedDefinition();
		}
		if (entityDef == null) {
			return;
		}
		if (!entityDef.interactable) {
			return;
		}
		String label = entityDef.name;
		if (entityDef.combatLevel != 0) {
			label = label + combatLevelColorTag(myPlayer.combatLevel, entityDef.combatLevel) + " (level-" + entityDef.combatLevel + ")";
		}
		if (itemSelected == 1) {
			menuActionName[menuActionRow] = "Use " + selectedItemName + " with @yel@" + label;
			menuActionID[menuActionRow] = 582;
			menuActionCmd1[menuActionRow] = npcIndex;
			menuActionCmd2[menuActionRow] = tileX;
			menuActionCmd3[menuActionRow] = tileY;
			menuActionRow++;
			return;
		}
		if (spellSelected == 1) {
			if ((spellUsableOn & 2) == 2) {
				menuActionName[menuActionRow] = spellTooltip + " @yel@" + label;
				menuActionID[menuActionRow] = 413;
				menuActionCmd1[menuActionRow] = npcIndex;
				menuActionCmd2[menuActionRow] = tileX;
				menuActionCmd3[menuActionRow] = tileY;
				menuActionRow++;
			}
		} else {
			if (entityDef.actions != null) {
				for (int actionSlot = 4; actionSlot >= 0; actionSlot--) {
					if (entityDef.actions[actionSlot] != null && !entityDef.actions[actionSlot].equalsIgnoreCase("attack")) {
						menuActionName[menuActionRow] = entityDef.actions[actionSlot] + " @yel@" + label;
						if (actionSlot == 0) {
							menuActionID[menuActionRow] = 20;
						}
						if (actionSlot == 1) {
							menuActionID[menuActionRow] = 412;
						}
						if (actionSlot == 2) {
							menuActionID[menuActionRow] = 225;
						}
						if (actionSlot == 3) {
							menuActionID[menuActionRow] = 965;
						}
						if (actionSlot == 4) {
							menuActionID[menuActionRow] = 478;
						}
						menuActionCmd1[menuActionRow] = npcIndex;
						menuActionCmd2[menuActionRow] = tileX;
						menuActionCmd3[menuActionRow] = tileY;
						menuActionRow++;
					}
				}

			}
			if (entityDef.actions != null) {
				for (int actionSlot = 4; actionSlot >= 0; actionSlot--) {
					if (entityDef.actions[actionSlot] != null && entityDef.actions[actionSlot].equalsIgnoreCase("attack")) {
						char c = '\0';
						if (entityDef.combatLevel > myPlayer.combatLevel) {
							c = '\u07D0';
						}
						menuActionName[menuActionRow] = entityDef.actions[actionSlot] + " @yel@" + label;
						if (actionSlot == 0) {
							menuActionID[menuActionRow] = 20 + c;
						}
						if (actionSlot == 1) {
							menuActionID[menuActionRow] = 412 + c;
						}
						if (actionSlot == 2) {
							menuActionID[menuActionRow] = 225 + c;
						}
						if (actionSlot == 3) {
							menuActionID[menuActionRow] = 965 + c;
						}
						if (actionSlot == 4) {
							menuActionID[menuActionRow] = 478 + c;
						}
						menuActionCmd1[menuActionRow] = npcIndex;
						menuActionCmd2[menuActionRow] = tileX;
						menuActionCmd3[menuActionRow] = tileY;
						menuActionRow++;
					}
				}

			}
			menuActionName[menuActionRow] = "Examine @yel@" + label + (showInfo ? " @gre@(@whi@" + entityDef.type + "@gre@)" : "");
			menuActionID[menuActionRow] = 1025;
			menuActionCmd1[menuActionRow] = npcIndex;
			menuActionCmd2[menuActionRow] = tileX;
			menuActionCmd3[menuActionRow] = tileY;
			menuActionRow++;
		}
	}

	public void buildAtPlayerMenu(int tileX, int playerIndex, Player player, int tileY) {
		if (player == myPlayer) {
			return;
		}
		if (menuActionRow >= 400) {
			return;
		}
		String label;
		if (player.skill == 0) {
			if (player.combatLevel > 0) {
				label = player.name + combatLevelColorTag(myPlayer.combatLevel, player.combatLevel) + " (level-" + player.combatLevel + ")";
			} else {
				label = player.name + " @cya@(store)";
			}
		} else {
			label = player.name + " (skill-" + player.skill + ")";
		}
		if (itemSelected == 1) {
			menuActionName[menuActionRow] = "Use " + selectedItemName + " with @whi@" + label;
			menuActionID[menuActionRow] = 491;
			menuActionCmd1[menuActionRow] = playerIndex;
			menuActionCmd2[menuActionRow] = tileX;
			menuActionCmd3[menuActionRow] = tileY;
			menuActionRow++;
		} else if (spellSelected == 1) {
			if ((spellUsableOn & 8) == 8) {
				menuActionName[menuActionRow] = spellTooltip + " @whi@" + label;
				menuActionID[menuActionRow] = 365;
				menuActionCmd1[menuActionRow] = playerIndex;
				menuActionCmd2[menuActionRow] = tileX;
				menuActionCmd3[menuActionRow] = tileY;
				menuActionRow++;
			}
		} else {
			for (int actionSlot = 4; actionSlot >= 0; actionSlot--) {
				if (atPlayerActions[actionSlot] != null) {
					menuActionName[menuActionRow] = atPlayerActions[actionSlot] + " @whi@" + label;
					char c = '\0';
					if (atPlayerActions[actionSlot].equalsIgnoreCase("attack")) {
						if (player.combatLevel > myPlayer.combatLevel) {
							c = '\u07D0';
						}
						if (myPlayer.team != 0 && player.team != 0) {
							if (myPlayer.team == player.team) {
								c = '\u07D0';
							} else {
								c = '\0';
							}
						}
					} else if (atPlayerArray[actionSlot]) {
						c = '\u07D0';
					}
					if (actionSlot == 0) {
						menuActionID[menuActionRow] = 561 + c;
					}
					if (actionSlot == 1) {
						menuActionID[menuActionRow] = 779 + c;
					}
					if (actionSlot == 2) {
						menuActionID[menuActionRow] = 27 + c;
					}
					if (actionSlot == 3) {
						menuActionID[menuActionRow] = 577 + c;
					}
					if (actionSlot == 4) {
						menuActionID[menuActionRow] = 729 + c;
					}
					menuActionCmd1[menuActionRow] = playerIndex;
					menuActionCmd2[menuActionRow] = tileX;
					menuActionCmd3[menuActionRow] = tileY;
					menuActionRow++;
				}
			}

		}
		for (int rowIndex = 0; rowIndex < menuActionRow; rowIndex++) {
			if (menuActionID[rowIndex] == 516) {
				menuActionName[rowIndex] = "Walk here @whi@" + label;
				return;
			}
		}

	}

	public void snapshotOriginalSceneObject(Class30_Sub1 pendingSceneChange) {
		int sceneUid = 0;
		int originalObjectId = -1;
		int originalShape = 0;
		int originalOrientation = 0;
		if (pendingSceneChange.objectCategory == 0) {
			sceneUid = worldController.getWallUid(pendingSceneChange.scenePlane, pendingSceneChange.tileX, pendingSceneChange.tileY);
		}
		if (pendingSceneChange.objectCategory == 1) {
			sceneUid = worldController.getWallDecorationUid(pendingSceneChange.scenePlane, pendingSceneChange.tileX, pendingSceneChange.tileY);
		}
		if (pendingSceneChange.objectCategory == 2) {
			sceneUid = worldController.getSceneObjectUid(pendingSceneChange.scenePlane, pendingSceneChange.tileX, pendingSceneChange.tileY);
		}
		if (pendingSceneChange.objectCategory == 3) {
			sceneUid = worldController.getGroundDecorationUid(pendingSceneChange.scenePlane, pendingSceneChange.tileX, pendingSceneChange.tileY);
		}
		if (sceneUid != 0) {
			int sceneMetadata = worldController.getObjectInfo(pendingSceneChange.scenePlane, pendingSceneChange.tileX, pendingSceneChange.tileY, sceneUid);
			originalObjectId = sceneUid >> 14 & 0x7fff;
			originalShape = sceneMetadata & 0x1f;
			originalOrientation = sceneMetadata >> 6;
		}
		pendingSceneChange.originalObjectId = originalObjectId;
		pendingSceneChange.originalShape = originalShape;
		pendingSceneChange.originalOrientation = originalOrientation;
	}

	public final void drainQueuedSoundsAndAdvanceSongCountdown() {
		for (int index = 0; index < currentSound; index++) {
			//if (soundDelay[index] <= 0) {
				boolean flag1 = false;
				try {
					Stream soundStream = Sounds.buildWave(soundType[index], sound[index]);
					if (soundStream != null && soundStream.currentOffset > 0) {
						if (ClientSettings.ANDROID) {
							AndroidHooks.playSound(soundStream.buffer, 0, soundStream.currentOffset, soundVolume[index], soundDelay[index]);
						} else {
							DesktopHooks.playSound(soundStream.buffer, 0, soundStream.currentOffset, soundVolume[index], soundDelay[index]);
						}
						if (System.currentTimeMillis() + (long) (soundStream.currentOffset / 22) > soundEndReferenceStartMillis + (long) (soundEndReferenceByteLength / 22)) {
							soundEndReferenceByteLength = soundStream.currentOffset;
							soundEndReferenceStartMillis = System.currentTimeMillis();
						}
					}
				} catch (Exception exception) {
					exception.printStackTrace();
				}
				if (!flag1 || soundDelay[index] == -5) {
					currentSound--;
					for (int shiftIndex = index; shiftIndex < currentSound; shiftIndex++) {
						sound[shiftIndex] = sound[shiftIndex + 1];
						soundType[shiftIndex] = soundType[shiftIndex + 1];
						soundDelay[shiftIndex] = soundDelay[shiftIndex + 1];
						soundVolume[shiftIndex] = soundVolume[shiftIndex + 1];
					}
					index--;
				} else {
					soundDelay[index] = -5;
				}
			/*} else {
				soundDelay[index]--;
			}*/
		}
		if (previousSong > 0) {
			previousSong -= 20;
			if (previousSong < 0)
				previousSong = 0;
			if (previousSong == 0 && musicVolume != 0 && currentSong != -1) {
				requestSongWithoutFade(musicVolume, false, currentSong);
			}
		}
	}
	
	private void connectServer()
	{
	  int j = 5;
		expectedCRCs[8] = 0;
		int k = 0;
		while(expectedCRCs[8] == 0)
		{
			String s = "Unknown problem";
			drawLoadingText(20, "Connecting to web server");
			try
			{
				DataInputStream datainputstream = openJagGrabInputStream("crc" + (int)(Math.random() * 99999999D) + "-" + 317);
				Stream class30_sub2_sub2 = new Stream(new byte[40]);
				datainputstream.readFully(class30_sub2_sub2.buffer, 0, 40);
				datainputstream.close();
				for(int i1 = 0; i1 < 9; i1++)
					expectedCRCs[i1] = class30_sub2_sub2.readIntBigEndian();

				int j1 = class30_sub2_sub2.readIntBigEndian();
				int k1 = 1234;
				for(int l1 = 0; l1 < 9; l1++)
					k1 = (k1 << 1) + expectedCRCs[l1];

				if(j1 != k1)
				{
					s = "checksum problem";
					expectedCRCs[8] = 0;
				}
			}
			catch(EOFException _ex)
			{
				s = "EOF problem";
				expectedCRCs[8] = 0;
			}
			catch(IOException _ex)
			{
				s = "FileServer Connection problem";
				// Check if we already have cache files, if so then allow the client to load anyway
				String cacheDir = Signlink.findcachedir();
				expectedCRCs[8] = new File(cacheDir + "main_file_cache.dat").length() > 0 ? 1 : 0;
			}
			catch(Exception _ex)
			{
				s = "logic problem";
				expectedCRCs[8] = 0;
				if(!Signlink.reporterror)
					return;
			}
			if(expectedCRCs[8] == 0)
			{
				k++;
				for(int l = j; l > 0; l--)
				{
					if(k >= 10)
					{
						drawLoadingText(10, "Game updated - please reload page");
						l = 10;
					} else
					{
						drawLoadingText(10, s + " - retry in " + l + " secs.");
					}
					try
					{
						Thread.sleep(1000L);
					}
					catch(Exception _ex) { }
				}

				j *= 2;
				if(j > 60)
					j = 60;
				aBoolean872 = !aBoolean872;
			}
		}
	}

	void startUp() {
		drawLoadingText(20, "Starting up");
//		try {
//			CacheDownloader.download(this, new URL("https://dl.dropboxusercontent.com/u/19852069/assets.zip"));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		if (Signlink.sunjava) {
			super.minDelay = 5;
		}
		aBoolean993 = true;
		boolean flag = true;
		if (!flag) {
			genericLoadingError = true;
			return;
		}
		if (Signlink.cache_dat != null) {
			for (int i = 0; i < 5; i++) {
				cacheStores[i] = new CacheStore(Signlink.cache_dat, Signlink.cache_idx[i], i + 1);
			}

		}
		try {
			connectServer();
			titleStreamLoader = streamLoaderForName(1, "title screen", "title", expectedCRCs[1], 25);
			aTextDrawingArea_1270 = new TextDrawingArea(false, "p11_full", titleStreamLoader);
			aTextDrawingArea_1271 = new TextDrawingArea(false, "p12_full", titleStreamLoader);
			chatTextDrawingArea = new TextDrawingArea(false, "b12_full", titleStreamLoader);
			TextDrawingArea aTextDrawingArea_1273 = new TextDrawingArea(true, "q8_full", titleStreamLoader);
			drawLogo();
			loadTitleScreen();
			//repackCacheIndex(1);
			constructMusic();
			StreamLoader streamLoader = streamLoaderForName(2, "config", "config", expectedCRCs[2], 30);
			StreamLoader streamLoader_1 = streamLoaderForName(3, "interface", "interface", expectedCRCs[3], 35);
			StreamLoader streamLoader_2 = streamLoaderForName(4, "2d graphics", "media", expectedCRCs[4], 40);
			StreamLoader streamLoader_3 = streamLoaderForName(6, "textures", "textures", expectedCRCs[6], 45);
			StreamLoader streamLoader_4 = streamLoaderForName(7, "chat system", "wordenc", expectedCRCs[7], 50);
			StreamLoader streamLoader_5 = streamLoaderForName(8, "sound effects", "sounds", expectedCRCs[8], 55);
			byteGroundArray = new byte[4][104][104];
			intGroundArray = new int[4][105][105];
			worldController = new WorldController(intGroundArray);
			for (int j = 0; j < 4; j++) {
				aClass11Array1230[j] = new CollisionMap();
			}

			minimapSprite = new Sprite(512, 512);
			StreamLoader streamLoader_6 = streamLoaderForName(5, "update list", "versionlist", expectedCRCs[5], 60);
			drawLoadingText(60, "Connecting to update server");
			onDemandFetcher = new OnDemandFetcher();
			onDemandFetcher.start(streamLoader_6, this);
//			musics();
			AnimationFrame.initialize(onDemandFetcher.getAnimCount());
			Model.initModelHeaders(onDemandFetcher.getVersionCount(0), onDemandFetcher);
			if (!lowMem) {
				requestSongWithFade(10, musicVolume, false, 0);
				while (onDemandFetcher.getNodeCount() > 0) {
					processOnDemandQueue();
					if (fetchMusic) {
						updateMusic();
					}
					try {
						Thread.sleep(100L);
					} catch (Exception _ex) {
					}
					if (onDemandFetcher.connectionFailureCount > 3) {
						loadError();
						return;
					}
				}
			}
			drawLoadingText(65, "Requesting animations");
			int k = onDemandFetcher.getVersionCount(1);
			for (int i1 = 0; i1 < k; i1++) {
				onDemandFetcher.requestFile(1, i1);
			}

			while (onDemandFetcher.getNodeCount() > 0) {
				int j1 = k - onDemandFetcher.getNodeCount();
				if (j1 > 0) {
					drawLoadingText(65, "Loading animations - " + j1 * 100 / k + "%");
				}
				processOnDemandQueue();
				try {
					Thread.sleep(100L);
				} catch (Exception _ex) {
				}
				if (onDemandFetcher.connectionFailureCount > 3) {
					loadError();
					return;
				}
			}
			drawLoadingText(70, "Requesting models");
			k = onDemandFetcher.getVersionCount(0);
			for (int k1 = 0; k1 < k; k1++) {
				int l1 = onDemandFetcher.getModelIndex(k1);
				if ((l1 & 1) != 0) {
					onDemandFetcher.requestFile(0, k1);
				}
			}

			k = onDemandFetcher.getNodeCount();
			while (onDemandFetcher.getNodeCount() > 0) {
				int i2 = k - onDemandFetcher.getNodeCount();
				if (i2 > 0) {
					drawLoadingText(70, "Loading models - " + i2 * 100 / k + "%");
				}
				processOnDemandQueue();
				try {
					Thread.sleep(100L);
				} catch (Exception _ex) {
				}
			}
			if (cacheStores[0] != null) {
				drawLoadingText(75, "Requesting maps");
				onDemandFetcher.requestFile(3, onDemandFetcher.getMapFileId(0, 48, 47));
				onDemandFetcher.requestFile(3, onDemandFetcher.getMapFileId(1, 48, 47));
				onDemandFetcher.requestFile(3, onDemandFetcher.getMapFileId(0, 48, 48));
				onDemandFetcher.requestFile(3, onDemandFetcher.getMapFileId(1, 48, 48));
				onDemandFetcher.requestFile(3, onDemandFetcher.getMapFileId(0, 48, 49));
				onDemandFetcher.requestFile(3, onDemandFetcher.getMapFileId(1, 48, 49));
				onDemandFetcher.requestFile(3, onDemandFetcher.getMapFileId(0, 47, 47));
				onDemandFetcher.requestFile(3, onDemandFetcher.getMapFileId(1, 47, 47));
				onDemandFetcher.requestFile(3, onDemandFetcher.getMapFileId(0, 47, 48));
				onDemandFetcher.requestFile(3, onDemandFetcher.getMapFileId(1, 47, 48));
				onDemandFetcher.requestFile(3, onDemandFetcher.getMapFileId(0, 148, 48));
				onDemandFetcher.requestFile(3, onDemandFetcher.getMapFileId(1, 148, 48));
				k = onDemandFetcher.getNodeCount();
				while (onDemandFetcher.getNodeCount() > 0) {
					int j2 = k - onDemandFetcher.getNodeCount();
					if (j2 > 0) {
						drawLoadingText(75, "Loading maps - " + j2 * 100 / k + "%");
					}
					processOnDemandQueue();
					try {
						Thread.sleep(100L);
					} catch (Exception _ex) {
					}
				}
			}
			k = onDemandFetcher.getVersionCount(0);
			for (int k2 = 0; k2 < k; k2++) {
				int l2 = onDemandFetcher.getModelIndex(k2);
				byte byte0 = 0;
				if ((l2 & 8) != 0) {
					byte0 = 10;
				} else if ((l2 & 0x20) != 0) {
					byte0 = 9;
				} else if ((l2 & 0x10) != 0) {
					byte0 = 8;
				} else if ((l2 & 0x40) != 0) {
					byte0 = 7;
				} else if ((l2 & 0x80) != 0) {
					byte0 = 6;
				} else if ((l2 & 2) != 0) {
					byte0 = 5;
				} else if ((l2 & 4) != 0) {
					byte0 = 4;
				}
				if ((l2 & 1) != 0) {
					byte0 = 3;
				}
				if (byte0 != 0) {
					onDemandFetcher.markExtraFileForDownload(byte0, 0, k2);
				}
			}

			onDemandFetcher.markMapFilesForDownload(isMembers);
			if (!lowMem) {
				int l = onDemandFetcher.getVersionCount(2);
				for (int i3 = 1; i3 < l; i3++) {
					if (onDemandFetcher.isSongPreloadFlagged(i3)) {
						onDemandFetcher.markExtraFileForDownload((byte) 1, 2, i3);
					}
				}

			}
			drawLoadingText(80, "Unpacking media");
			invBack = new Background(streamLoader_2, "invback", 0);
			chatBack = new Background(streamLoader_2, "chatback", 0);
			mapBack = new Background(streamLoader_2, "mapback", 0);
			backBase1 = new Background(streamLoader_2, "backbase1", 0);
			backBase2 = new Background(streamLoader_2, "backbase2", 0);
			backHmid1 = new Background(streamLoader_2, "backhmid1", 0);
			for (int j3 = 0; j3 < 13; j3++) {
				sideIcons[j3] = new Background(streamLoader_2, "sideicons", j3);
			}

			compass = new Sprite(streamLoader_2, "compass", 0);
			minimapEdgeArrow = new Sprite(streamLoader_2, "mapedge", 0);
			minimapEdgeArrow.expandToCanvas();

			multiOverlay = new Sprite(streamLoader_2, "overlay_multiway", 0);
			try {
				for (int k3 = 0; k3 < 100; k3++) {
					mapScenes[k3] = new Background(streamLoader_2, "mapscene", k3);
				}

			} catch (Exception _ex) {
			}
			try {
				for (int l3 = 0; l3 < 100; l3++) {
					mapFunctions[l3] = new Sprite(streamLoader_2, "mapfunction", l3);
				}

			} catch (Exception _ex) {
			}
			try {
				for (int i4 = 0; i4 < 20; i4++) {
					hitMarks[i4] = new Sprite(streamLoader_2, "hitmarks", i4);
				}

			} catch (Exception _ex) {
			}
			try {
				for (int h1 = 0; h1 < 6; h1++) {
					headIconsHint[h1] = new Sprite(streamLoader_2, "headicons_hint", h1);
				}
				for (int j4 = 0; j4 < 8; j4++) {
					headIcons[j4] = new Sprite(streamLoader_2, "headicons_prayer", j4);
				}
				for (int j45 = 0; j45 < 3; j45++) {
					skullIcons[j45] = new Sprite(streamLoader_2, "headicons_pk", j45);
				}
			} catch (Exception _ex) {
			}
			mapFlag = new Sprite(streamLoader_2, "mapmarker", 0);
			mapMarker = new Sprite(streamLoader_2, "mapmarker", 1);
			for (int k4 = 0; k4 < 8; k4++) {
				crosses[k4] = new Sprite(streamLoader_2, "cross", k4);
			}

			mapDotItem = new Sprite(streamLoader_2, "mapdots", 0);
			mapDotNPC = new Sprite(streamLoader_2, "mapdots", 1);
			mapDotPlayer = new Sprite(streamLoader_2, "mapdots", 2);
			mapDotFriend = new Sprite(streamLoader_2, "mapdots", 3);
			mapDotTeam = new Sprite(streamLoader_2, "mapdots", 4);
			scrollUpArrow = new Background(streamLoader_2, "scrollbar", 0);
			scrollDownArrow = new Background(streamLoader_2, "scrollbar", 1);
			redStone1 = new Background(streamLoader_2, "redstone1", 0);
			redStone2 = new Background(streamLoader_2, "redstone2", 0);
			redStone3 = new Background(streamLoader_2, "redstone3", 0);
			redStone1_2 = new Background(streamLoader_2, "redstone1", 0);
			redStone1_2.flipHorizontally();
			redStone2_2 = new Background(streamLoader_2, "redstone2", 0);
			redStone2_2.flipHorizontally();
			redStone1_3 = new Background(streamLoader_2, "redstone1", 0);
			redStone1_3.flipVertically();
			redStone2_3 = new Background(streamLoader_2, "redstone2", 0);
			redStone2_3.flipVertically();
			redStone3_2 = new Background(streamLoader_2, "redstone3", 0);
			redStone3_2.flipVertically();
			redStone1_4 = new Background(streamLoader_2, "redstone1", 0);
			redStone1_4.flipHorizontally();
			redStone1_4.flipVertically();
			redStone2_4 = new Background(streamLoader_2, "redstone2", 0);
			redStone2_4.flipHorizontally();
			redStone2_4.flipVertically();
			for (int l4 = 0; l4 < 2; l4++) {
				modIcons[l4] = new Background(streamLoader_2, "mod_icons", l4);
			}

			Sprite sprite = new Sprite(streamLoader_2, "backleft1", 0);
			backLeftIP1 = new RSImageProducer(sprite.width, sprite.height, getGameComponent());
			sprite.drawOpaque(0, 0);
			sprite = new Sprite(streamLoader_2, "backleft2", 0);
			backLeftIP2 = new RSImageProducer(sprite.width, sprite.height, getGameComponent());
			sprite.drawOpaque(0, 0);
			sprite = new Sprite(streamLoader_2, "backright1", 0);
			backRightIP1 = new RSImageProducer(sprite.width, sprite.height, getGameComponent());
			sprite.drawOpaque(0, 0);
			sprite = new Sprite(streamLoader_2, "backright2", 0);
			backRightIP2 = new RSImageProducer(sprite.width, sprite.height, getGameComponent());
			sprite.drawOpaque(0, 0);
			sprite = new Sprite(streamLoader_2, "backtop1", 0);
			backTopIP1 = new RSImageProducer(sprite.width, sprite.height, getGameComponent());
			sprite.drawOpaque(0, 0);
			sprite = new Sprite(streamLoader_2, "backvmid1", 0);
			backVmidIP1 = new RSImageProducer(sprite.width, sprite.height, getGameComponent());
			sprite.drawOpaque(0, 0);
			sprite = new Sprite(streamLoader_2, "backvmid2", 0);
			backVmidIP2 = new RSImageProducer(sprite.width, sprite.height, getGameComponent());
			sprite.drawOpaque(0, 0);
			sprite = new Sprite(streamLoader_2, "backvmid3", 0);
			backVmidIP3 = new RSImageProducer(sprite.width, sprite.height, getGameComponent());
			sprite.drawOpaque(0, 0);
			sprite = new Sprite(streamLoader_2, "backhmid2", 0);
			backVmidIP2_2 = new RSImageProducer(sprite.width, sprite.height, getGameComponent());
			sprite.drawOpaque(0, 0);
			int i5 = (int) (Math.random() * 21D) - 10;
			int j5 = (int) (Math.random() * 21D) - 10;
			int k5 = (int) (Math.random() * 21D) - 10;
			int l5 = (int) (Math.random() * 41D) - 20;
			for (int i6 = 0; i6 < 100; i6++) {
				if (mapFunctions[i6] != null) {
					mapFunctions[i6].adjustRgb(i5 + l5, j5 + l5, k5 + l5);
				}
				if (mapScenes[i6] != null) {
					mapScenes[i6].adjustPaletteRgb(i5 + l5, j5 + l5, k5 + l5);
				}
			}

			drawLoadingText(83, "Unpacking textures");
			Texture.loadTextures(streamLoader_3);
			Texture.buildPalettes(0.80000000000000004D);
			Texture.allocateBufferPool();
			drawLoadingText(86, "Unpacking config");
			Animation.unpackConfig(streamLoader);
			ObjectDef.unpackConfig(streamLoader);
			Flo.unpackConfig(streamLoader);
			ItemDef.unpackConfig(streamLoader);
			EntityDef.unpackConfig(streamLoader);
			IDK.unpackConfig(streamLoader);
			SpotAnim.unpackConfig(streamLoader);
			Varp.unpackConfig(streamLoader);
			VarBit.unpackConfig(streamLoader);
			ItemDef.isMembers = isMembers;
			// if (!lowMem) {
			drawLoadingText(90, "Unpacking sounds");
			byte abyte0[] = streamLoader_5.getDataForName("sounds.dat");
			Stream stream = new Stream(abyte0);
			Sounds.unpack(stream);
			// }
			drawLoadingText(95, "Unpacking interfaces");
			TextDrawingArea aclass30_sub2_sub1_sub4s[] = {aTextDrawingArea_1270, aTextDrawingArea_1271, chatTextDrawingArea, aTextDrawingArea_1273};
			RSInterface.unpack(streamLoader_1, aclass30_sub2_sub1_sub4s, streamLoader_2);
			drawLoadingText(100, "Preparing game engine");
			for (int j6 = 0; j6 < 33; j6++) {
				int k6 = 999;
				int i7 = 0;
				for (int k7 = 0; k7 < 34; k7++) {
					if (mapBack.paletteIndices[k7 + j6 * mapBack.imageWidth] == 0) {
						if (k6 == 999) {
							k6 = k7;
						}
						continue;
					}
					if (k6 == 999) {
						continue;
					}
					i7 = k7;
					break;
				}

				anIntArray968[j6] = k6;
				anIntArray1057[j6] = i7 - k6;
			}

			for (int l6 = 5; l6 < 156; l6++) {
				int j7 = 999;
				int l7 = 0;
				for (int j8 = 25; j8 < 172; j8++) {
					if (mapBack.paletteIndices[j8 + l6 * mapBack.imageWidth] == 0 && (j8 > 34 || l6 > 34)) {
						if (j7 == 999) {
							j7 = j8;
						}
						continue;
					}
					if (j7 == 999) {
						continue;
					}
					l7 = j8;
					break;
				}

				anIntArray1052[l6 - 5] = j7 - 25;
				anIntArray1229[l6 - 5] = l7 - j7;
			}
			Texture.setScreenSize(765, 503);
			gameScreenOffsets = Texture.lineOffsets;
			Texture.setScreenSize(479, 96);
			chatAreaOffsets = Texture.lineOffsets;
			Texture.setScreenSize(190, 261);
			tabAreaOffsets = Texture.lineOffsets;
			Texture.setScreenSize(512, 334);
			chatBoxAreaOffsets = Texture.lineOffsets;
			int ai[] = new int[9];
			for (int i8 = 0; i8 < 9; i8++) {
				int k8 = 128 + i8 * 32 + 15;
				int l8 = 600 + k8 * 3;
				int i9 = Texture.sineTable[k8];
				ai[i8] = l8 * i9 >> 16;
			}

			WorldController.buildTileVisibilityTable(500, 800, 512, 334, ai);
			Censor.loadConfig(streamLoader_4);
			mouseDetection = new MouseDetection(this);
			startRunnable(mouseDetection, 10);
			AnimatedObject.clientInstance = this;
			ObjectDef.clientInstance = this;
			EntityDef.clientInstance = this;
			
			if (myUsername != null && !myUsername.isEmpty()) {
				loginScreenState = 2;
				loginScreenCursorPos = (myPassword != null && !myPassword.isEmpty()) ? 1 : 0;
				if (myPassword != null && !myPassword.isEmpty()) {
					login(myUsername, myPassword, false);
				}
			}
			return;
		} catch (Exception exception) {
			Signlink.reporterror("loaderror " + aString1049 + " " + anInt1079);
		}
		loadingError = true;
	}

	public void addNewLocalPlayers(Stream stream, int packetSize) {
		while (stream.bitOffset + 10 < packetSize * 8) {
			int playerIndex = stream.readBitsMsbFirst(11);
			if (playerIndex == 2047) {
				break;
			}
			if (playerArray[playerIndex] == null) {
				playerArray[playerIndex] = new Player();
				if (aStreamArray895s[playerIndex] != null) {
					playerArray[playerIndex].updatePlayer(aStreamArray895s[playerIndex]);
				}
			}
			playerIndices[playerCount++] = playerIndex;
			Player player = playerArray[playerIndex];
			player.anInt1537 = loopCycle;
			int needsUpdate = stream.readBitsMsbFirst(1);
			if (needsUpdate == 1) {
				maskUpdateIndices[maskUpdateCount++] = playerIndex;
			}
			int snapToPosition = stream.readBitsMsbFirst(1);
			int offsetY = stream.readBitsMsbFirst(5);
			if (offsetY > 15) {
				offsetY -= 32;
			}
			int offsetX = stream.readBitsMsbFirst(5);
			if (offsetX > 15) {
				offsetX -= 32;
			}
			player.setPos(myPlayer.smallX[0] + offsetX, myPlayer.smallY[0] + offsetY, snapToPosition == 1);
		}
		stream.finishBitReading();
	}

	public void processMainScreenClick() {
		if (anInt1021 != 0) {
			return;
		}
		if (super.clickMode3 == 1) {
			int i = super.saveClickX - 25 - 550;
			int j = super.saveClickY - 5 - 4;
			if (i >= 0 && j >= 0 && i < 146 && j < 151) {
				i -= 73;
				j -= 75;
				int k = minimapInt1 + minimapInt2 & 0x7ff;
				int i1 = Texture.sineTable[k];
				int j1 = Texture.cosineTable[k];
				i1 = i1 * (minimapInt3 + 256) >> 8;
				j1 = j1 * (minimapInt3 + 256) >> 8;
				int k1 = j * i1 + i * j1 >> 11;
				int l1 = j * j1 - i * i1 >> 11;
				int i2 = myPlayer.x + k1 >> 7;
				int j2 = myPlayer.y - l1 >> 7;
				boolean flag1 = doWalkTo(1, 0, 0, 0, myPlayer.smallY[0], 0, 0, j2, myPlayer.smallX[0], true, i2);
				if (flag1) {
					stream.writeByte(i);
					stream.writeByte(j);
					stream.writeShortBigEndian(minimapInt1);
					stream.writeByte(57);
					stream.writeByte(minimapInt2);
					stream.writeByte(minimapInt3);
					stream.writeByte(89);
					stream.writeShortBigEndian(myPlayer.x);
					stream.writeShortBigEndian(myPlayer.y);
					stream.writeByte(walkDestinationAdjusted);
					stream.writeByte(63);
				}
			}
			anInt1117++;
			if (anInt1117 > 1151) {
				anInt1117 = 0;
				stream.writeEncryptedOpcode(246);
				stream.writeByte(0);
				int l = stream.currentOffset;
				if ((int) (Math.random() * 2D) == 0) {
					stream.writeByte(101);
				}
				stream.writeByte(197);
				stream.writeShortBigEndian((int) (Math.random() * 65536D));
				stream.writeByte((int) (Math.random() * 256D));
				stream.writeByte(67);
				stream.writeShortBigEndian(14214);
				if ((int) (Math.random() * 2D) == 0) {
					stream.writeShortBigEndian(29487);
				}
				stream.writeShortBigEndian((int) (Math.random() * 65536D));
				if ((int) (Math.random() * 2D) == 0) {
					stream.writeByte(220);
				}
				stream.writeByte(180);
				stream.backpatchBytePayloadLength(stream.currentOffset - l);
			}
		}
	}

	public String interfaceIntToString(int value) {
		if (value < 0x3b9ac9ff) {
			return String.valueOf(value);
		} else {
			return "*";
		}
	}

	public void showErrorScreen() {
		if (ClientSettings.ANDROID) {
			AndroidHooks.showErrorScreen(this, loadingError, genericLoadingError, rsAlreadyLoaded);
		} else {
			DesktopHooks.showErrorScreen(this, loadingError, genericLoadingError, rsAlreadyLoaded);
		}
	}

	public URL getCodeBase() {
		// if (SignLink.mainapp != null) {
		// return SignLink.mainapp.getCodeBase();
		// }
		try {
			// if (super.gameFrame != null) {
			return new URL("http://" + server + ":" + (8080 + portOff));
			// }
		} catch (Exception _ex) {
		}
		return null;
		// return super.getCodeBase();
	}

	public void advanceNpcEntities() {
		for (int npcListIndex = 0; npcListIndex < npcCount; npcListIndex++) {
			int npcIndex = npcIndices[npcListIndex];
			NPC npc = npcArray[npcIndex];
			if (npc != null) {
				advanceEntityMovementAndAnimation(npc);
			}
		}
	}

	public void advanceEntityMovementAndAnimation(Entity entity) {
		if (entity.x < 128 || entity.y < 128 || entity.x >= 13184 || entity.y >= 13184) {
			entity.anim = -1;
			entity.anInt1520 = -1;
			entity.anInt1547 = 0;
			entity.anInt1548 = 0;
			entity.x = entity.smallX[0] * 128 + entity.tileSize * 64;
			entity.y = entity.smallY[0] * 128 + entity.tileSize * 64;
			entity.method446();
		}
		if (entity == myPlayer && (entity.x < 1536 || entity.y < 1536 || entity.x >= 11776 || entity.y >= 11776)) {
			entity.anim = -1;
			entity.anInt1520 = -1;
			entity.anInt1547 = 0;
			entity.anInt1548 = 0;
			entity.x = entity.smallX[0] * 128 + entity.tileSize * 64;
			entity.y = entity.smallY[0] * 128 + entity.tileSize * 64;
			entity.method446();
		}
		if (entity.anInt1547 > loopCycle) {
			advanceEntityToForcedMovementStart(entity);
		} else if (entity.anInt1548 >= loopCycle) {
			advanceEntityThroughForcedMovement(entity);
		} else {
			advanceEntityAlongWalkingPath(entity);
		}
		updateEntityTurning(entity);
		updateEntityAnimations(entity);
	}

	public void advanceEntityToForcedMovementStart(Entity entity) {
		int ticksUntilMovementStart = entity.anInt1547 - loopCycle;
		int movementStartX = entity.anInt1543 * 128 + entity.tileSize * 64;
		int movementStartY = entity.anInt1545 * 128 + entity.tileSize * 64;
		entity.x += (movementStartX - entity.x) / ticksUntilMovementStart;
		entity.y += (movementStartY - entity.y) / ticksUntilMovementStart;
		entity.deferredMovementTicks = 0;
		if (entity.anInt1549 == 0) {
			entity.turnDirection = 1024;
		}
		if (entity.anInt1549 == 1) {
			entity.turnDirection = 1536;
		}
		if (entity.anInt1549 == 2) {
			entity.turnDirection = 0;
		}
		if (entity.anInt1549 == 3) {
			entity.turnDirection = 512;
		}
	}

	public void advanceEntityThroughForcedMovement(Entity entity) {
		if (entity.anInt1548 == loopCycle || entity.anim == -1 || entity.anInt1529 != 0 || entity.anInt1528 + 1 > Animation.anims[entity.anim].getFrameDelay(entity.anInt1527)) {
			int movementDurationTicks = entity.anInt1548 - entity.anInt1547;
			int elapsedMovementTicks = loopCycle - entity.anInt1547;
			int movementStartX = entity.anInt1543 * 128 + entity.tileSize * 64;
			int movementStartY = entity.anInt1545 * 128 + entity.tileSize * 64;
			int movementEndX = entity.anInt1544 * 128 + entity.tileSize * 64;
			int movementEndY = entity.anInt1546 * 128 + entity.tileSize * 64;
			entity.x = (movementStartX * (movementDurationTicks - elapsedMovementTicks) + movementEndX * elapsedMovementTicks) / movementDurationTicks;
			entity.y = (movementStartY * (movementDurationTicks - elapsedMovementTicks) + movementEndY * elapsedMovementTicks) / movementDurationTicks;
		}
		entity.deferredMovementTicks = 0;
		if (entity.anInt1549 == 0) {
			entity.turnDirection = 1024;
		}
		if (entity.anInt1549 == 1) {
			entity.turnDirection = 1536;
		}
		if (entity.anInt1549 == 2) {
			entity.turnDirection = 0;
		}
		if (entity.anInt1549 == 3) {
			entity.turnDirection = 512;
		}
		entity.facingDirection = entity.turnDirection;
	}

	public void advanceEntityAlongWalkingPath(Entity entity) {
		entity.movementAnimationId = entity.idleAnimationId;
		if (entity.smallXYIndex == 0) {
			entity.deferredMovementTicks = 0;
			return;
		}
		if (entity.anim != -1 && entity.anInt1529 == 0) {
			Animation animation = Animation.anims[entity.anim];
			if (entity.anInt1542 > 0 && animation.movingPrecedence == 0) {
				entity.deferredMovementTicks++;
				return;
			}
			if (entity.anInt1542 <= 0 && animation.idlePrecedence == 0) {
				entity.deferredMovementTicks++;
				return;
			}
		}
		int previousX = entity.x;
		int previousY = entity.y;
		int targetX = entity.smallX[entity.smallXYIndex - 1] * 128 + entity.tileSize * 64;
		int targetY = entity.smallY[entity.smallXYIndex - 1] * 128 + entity.tileSize * 64;
		if (targetX - previousX > 256 || targetX - previousX < -256 || targetY - previousY > 256 || targetY - previousY < -256) {
			entity.x = targetX;
			entity.y = targetY;
			return;
		}
		if (previousX < targetX) {
			if (previousY < targetY) {
				entity.turnDirection = 1280;
			} else if (previousY > targetY) {
				entity.turnDirection = 1792;
			} else {
				entity.turnDirection = 1536;
			}
		} else if (previousX > targetX) {
			if (previousY < targetY) {
				entity.turnDirection = 768;
			} else if (previousY > targetY) {
				entity.turnDirection = 256;
			} else {
				entity.turnDirection = 512;
			}
		} else if (previousY < targetY) {
			entity.turnDirection = 1024;
		} else {
			entity.turnDirection = 0;
		}
		int relativeTurnAngle = entity.turnDirection - entity.facingDirection & 0x7ff;
		if (relativeTurnAngle > 1024) {
			relativeTurnAngle -= 2048;
		}
		int movementAnimation = entity.reverseWalkAnimationId;
		if (relativeTurnAngle >= -256 && relativeTurnAngle <= 256) {
			movementAnimation = entity.walkAnimationId;
		} else if (relativeTurnAngle >= 256 && relativeTurnAngle < 768) {
			movementAnimation = entity.sidestepPositiveAnimationId;
		} else if (relativeTurnAngle >= -768 && relativeTurnAngle <= -256) {
			movementAnimation = entity.sidestepNegativeAnimationId;
		}
		if (movementAnimation == -1) {
			movementAnimation = entity.walkAnimationId;
		}
		entity.movementAnimationId = movementAnimation;
		int movementStepSize = 4;
		if (entity.facingDirection != entity.turnDirection && entity.interactingEntity == -1 && entity.turnSpeed != 0) {
			movementStepSize = 2;
		}
		if (entity.smallXYIndex > 2) {
			movementStepSize = 6;
		}
		if (entity.smallXYIndex > 3) {
			movementStepSize = 8;
		}
		if (entity.deferredMovementTicks > 0 && entity.smallXYIndex > 1) {
			movementStepSize = 8;
			entity.deferredMovementTicks--;
		}
		if (entity.aBooleanArray1553[entity.smallXYIndex - 1]) {
			movementStepSize <<= 1;
		}
		if (movementStepSize >= 8 && entity.movementAnimationId == entity.walkAnimationId && entity.runAnimationId != -1) {
			entity.movementAnimationId = entity.runAnimationId;
		}
		if (previousX < targetX) {
			entity.x += movementStepSize;
			if (entity.x > targetX) {
				entity.x = targetX;
			}
		} else if (previousX > targetX) {
			entity.x -= movementStepSize;
			if (entity.x < targetX) {
				entity.x = targetX;
			}
		}
		if (previousY < targetY) {
			entity.y += movementStepSize;
			if (entity.y > targetY) {
				entity.y = targetY;
			}
		} else if (previousY > targetY) {
			entity.y -= movementStepSize;
			if (entity.y < targetY) {
				entity.y = targetY;
			}
		}
		if (entity.x == targetX && entity.y == targetY) {
			entity.smallXYIndex--;
			if (entity.anInt1542 > 0) {
				entity.anInt1542--;
			}
		}
	}

	public void updateEntityTurning(Entity entity) {
		if (entity.turnSpeed == 0) {
			return;
		}
		if (entity.interactingEntity != -1 && entity.interactingEntity < 32768) {
			NPC npc = npcArray[entity.interactingEntity];
			if (npc != null) {
				int npcDeltaX = entity.x - npc.x;
				int npcDeltaY = entity.y - npc.y;
				if (npcDeltaX != 0 || npcDeltaY != 0) {
					entity.turnDirection = (int) (Math.atan2(npcDeltaX, npcDeltaY) * 325.94900000000001D) & 0x7ff;
				}
			}
		}
		if (entity.interactingEntity >= 32768) {
			int playerIndex = entity.interactingEntity - 32768;
			if (playerIndex == unknownInt10) {
				playerIndex = myPlayerIndex;
			}
			Player player = playerArray[playerIndex];
			if (player != null) {
				int playerDeltaX = entity.x - player.x;
				int playerDeltaY = entity.y - player.y;
				if (playerDeltaX != 0 || playerDeltaY != 0) {
					entity.turnDirection = (int) (Math.atan2(playerDeltaX, playerDeltaY) * 325.94900000000001D) & 0x7ff;
				}
			}
		}
		if ((entity.anInt1538 != 0 || entity.anInt1539 != 0) && (entity.smallXYIndex == 0 || entity.deferredMovementTicks > 0)) {
			int faceTileDeltaX = entity.x - (entity.anInt1538 - baseX - baseX) * 64;
			int faceTileDeltaY = entity.y - (entity.anInt1539 - baseY - baseY) * 64;
			if (faceTileDeltaX != 0 || faceTileDeltaY != 0) {
				entity.turnDirection = (int) (Math.atan2(faceTileDeltaX, faceTileDeltaY) * 325.94900000000001D) & 0x7ff;
			}
			entity.anInt1538 = 0;
			entity.anInt1539 = 0;
		}
		int turnDelta = entity.turnDirection - entity.facingDirection & 0x7ff;
		if (turnDelta != 0) {
			if (turnDelta < entity.turnSpeed || turnDelta > 2048 - entity.turnSpeed) {
				entity.facingDirection = entity.turnDirection;
			} else if (turnDelta > 1024) {
				entity.facingDirection -= entity.turnSpeed;
			} else {
				entity.facingDirection += entity.turnSpeed;
			}
			entity.facingDirection &= 0x7ff;
			if (entity.movementAnimationId == entity.idleAnimationId && entity.facingDirection != entity.turnDirection) {
				if (entity.turnInPlaceAnimationId != -1) {
					entity.movementAnimationId = entity.turnInPlaceAnimationId;
					return;
				}
				entity.movementAnimationId = entity.walkAnimationId;
			}
		}
	}

	public void updateEntityAnimations(Entity entity) {
		entity.aBoolean1541 = false;
		if (entity.movementAnimationId != -1) {
			Animation movementAnimation = Animation.anims[entity.movementAnimationId];
			entity.anInt1519++;
			if (entity.anInt1518 < movementAnimation.frameCount && entity.anInt1519 > movementAnimation.getFrameDelay(entity.anInt1518)) {
				entity.anInt1519 = 0;
				entity.anInt1518++;
			}
			if (entity.anInt1518 >= movementAnimation.frameCount) {
				entity.anInt1519 = 0;
				entity.anInt1518 = 0;
			}
		}
		if (entity.anInt1520 != -1 && loopCycle >= entity.anInt1523) {
			if (entity.anInt1521 < 0) {
				entity.anInt1521 = 0;
			}
			Animation spotAnimation = SpotAnim.cache[entity.anInt1520].animation;
			for (entity.anInt1522++; entity.anInt1521 < spotAnimation.frameCount && entity.anInt1522 > spotAnimation.getFrameDelay(entity.anInt1521); entity.anInt1521++) {
				entity.anInt1522 -= spotAnimation.getFrameDelay(entity.anInt1521);
			}

			if (entity.anInt1521 >= spotAnimation.frameCount && (entity.anInt1521 < 0 || entity.anInt1521 >= spotAnimation.frameCount)) {
				entity.anInt1520 = -1;
			}
		}
		if (entity.anim != -1 && entity.anInt1529 <= 1) {
			Animation actionDelayAnimation = Animation.anims[entity.anim];
			if (actionDelayAnimation.movingPrecedence == 1 && entity.anInt1542 > 0 && entity.anInt1547 <= loopCycle && entity.anInt1548 < loopCycle) {
				entity.anInt1529 = 1;
				return;
			}
		}
		if (entity.anim != -1 && entity.anInt1529 == 0) {
			Animation actionAnimation = Animation.anims[entity.anim];
			for (entity.anInt1528++; entity.anInt1527 < actionAnimation.frameCount && entity.anInt1528 > actionAnimation.getFrameDelay(entity.anInt1527); entity.anInt1527++) {
				entity.anInt1528 -= actionAnimation.getFrameDelay(entity.anInt1527);
			}

			if (entity.anInt1527 >= actionAnimation.frameCount) {
				entity.anInt1527 -= actionAnimation.loopBackFrames;
				entity.anInt1530++;
				if (entity.anInt1530 >= actionAnimation.maxLoops) {
					entity.anim = -1;
				}
				if (entity.anInt1527 < 0 || entity.anInt1527 >= actionAnimation.frameCount) {
					entity.anim = -1;
				}
			}
			entity.aBoolean1541 = actionAnimation.extendByYaw;
		}
		if (entity.anInt1529 > 0) {
			entity.anInt1529--;
		}
	}

	public void drawGameScreen() {
		if (fullScreenInterfaceId != -1 && (loadingStage == 2 || super.fullGameScreen != null)) {
			if (loadingStage == 2) {
				advanceInterfaceModelAnimations(cyclesSinceDraw, fullScreenInterfaceId);
				if (fullScreenInterfaceId != -1)
					advanceInterfaceModelAnimations(cyclesSinceDraw, fullScreenInterfaceId);
				cyclesSinceDraw = 0;
				resetAllImageProducers();
				super.fullGameScreen.setAsDrawingArea();
				Texture.lineOffsets = gameScreenOffsets;
				DrawingArea.clearRaster();
				welcomeScreenRaised = true;
				if (openInterfaceID != -1) {
					RSInterface class13_1 = RSInterface.interfaceCache[openInterfaceID];
					if (class13_1.width == 512 && class13_1.height == 334
							&& class13_1.type == 0) {
						class13_1.width = 765;
						class13_1.height = 503;
					}
					drawInterface(0, 0, class13_1, 0);
				}
				RSInterface rSInterface = RSInterface.interfaceCache[fullScreenInterfaceId];
				if (rSInterface.width == 512 && rSInterface.height == 334
						&& rSInterface.type == 0) {
					rSInterface.width = 765;
					rSInterface.height = 503;
				}
				drawInterface(0, 0, rSInterface, 0);
				if (!menuOpen) {
					processRightClick();
					drawTooltip();
				} else {
					drawMenu();
				}
			}
			super.fullGameScreen.drawGraphics(0, super.graphics, 0);
			return;
		}
		if (welcomeScreenRaised) {
			resetImageProducers2();
			welcomeScreenRaised = false;
			backLeftIP1.drawGraphics(4, super.graphics, 0);
			backLeftIP2.drawGraphics(357, super.graphics, 0);
			backRightIP1.drawGraphics(4, super.graphics, 722);
			backRightIP2.drawGraphics(205, super.graphics, 743);
			backTopIP1.drawGraphics(0, super.graphics, 0);
			backVmidIP1.drawGraphics(4, super.graphics, 516);
			backVmidIP2.drawGraphics(205, super.graphics, 516);
			backVmidIP3.drawGraphics(357, super.graphics, 496);
			backVmidIP2_2.drawGraphics(338, super.graphics, 0);
			needDrawTabArea = true;
			inputTaken = true;
			tabAreaAltered = true;
			aBoolean1233 = true;
			if (loadingStage != 2) {
				aRSImageProducer_1165.drawGraphics(4, super.graphics, 4);
				aRSImageProducer_1164.drawGraphics(4, super.graphics, 550);
			}
		}
		if (loadingStage == 2) {
			drawWorldView();
		}
		if (menuOpen && menuScreenArea == 1) {
			needDrawTabArea = true;
		}
		if (invOverlayInterfaceID != -1) {
			boolean flag1 = advanceInterfaceModelAnimations(cyclesSinceDraw, invOverlayInterfaceID);
			if (flag1) {
				needDrawTabArea = true;
			}
		}
		if (atInventoryInterfaceType == 2) {
			needDrawTabArea = true;
		}
		if (activeInterfaceType == 2) {
			needDrawTabArea = true;
		}
		if (needDrawTabArea) {
			drawTabArea();
			needDrawTabArea = false;
		}
		if (backDialogID == -1) {
			aClass9_1059.scrollPosition = anInt1211 - anInt1089 - 77;
			if (super.mouseX > 448 && super.mouseX < 560 && super.mouseY > 332) {
				handleScrollbarInput(463, 77, super.mouseX - 17, super.mouseY - 357, aClass9_1059, 0, false, anInt1211);
			}
			int i = anInt1211 - 77 - aClass9_1059.scrollPosition;
			if (i < 0) {
				i = 0;
			}
			if (i > anInt1211 - 77) {
				i = anInt1211 - 77;
			}
			if (anInt1089 != i) {
				anInt1089 = i;
				inputTaken = true;
			}
		}
		if (backDialogID != -1) {
			boolean flag2 = advanceInterfaceModelAnimations(cyclesSinceDraw, backDialogID);
			if (flag2) {
				inputTaken = true;
			}
		}
		if (dialogID != -1) {
			inputTaken = true;
		}
		if (atInventoryInterfaceType == 3) {
			inputTaken = true;
		}
		if (activeInterfaceType == 3) {
			inputTaken = true;
		}
		if (aString844 != null) {
			inputTaken = true;
		}
		if (menuOpen && menuScreenArea == 2) {
			inputTaken = true;
		}
		if (inputTaken) {
			drawChatArea();
			inputTaken = false;
		}
		if (loadingStage == 2) {
			drawMinimap();
			aRSImageProducer_1164.drawGraphics(4, super.graphics, 550);
		}
		if (anInt1054 != -1) {
			tabAreaAltered = true;
		}
		if (tabAreaAltered) {
			if (anInt1054 != -1 && anInt1054 == tabID) {
				anInt1054 = -1;
				stream.writeEncryptedOpcode(120);
				stream.writeByte(tabID);
			}
			tabAreaAltered = false;
			aRSImageProducer_1125.setAsDrawingArea();
			backHmid1.drawNonzeroIndexed(0, 0);
			if (invOverlayInterfaceID == -1) {
				if (tabInterfaceIDs[tabID] != -1) {
					if (tabID == 0) {
						redStone1.drawNonzeroIndexed(22, 10);
					}
					if (tabID == 1) {
						redStone2.drawNonzeroIndexed(54, 8);
					}
					if (tabID == 2) {
						redStone2.drawNonzeroIndexed(82, 8);
					}
					if (tabID == 3) {
						redStone3.drawNonzeroIndexed(110, 8);
					}
					if (tabID == 4) {
						redStone2_2.drawNonzeroIndexed(153, 8);
					}
					if (tabID == 5) {
						redStone2_2.drawNonzeroIndexed(181, 8);
					}
					if (tabID == 6) {
						redStone1_2.drawNonzeroIndexed(209, 9);
					}
				}
				if (tabInterfaceIDs[0] != -1 && (anInt1054 != 0 || loopCycle % 20 < 10)) {
					sideIcons[0].drawNonzeroIndexed(29, 13);
				}
				if (tabInterfaceIDs[1] != -1 && (anInt1054 != 1 || loopCycle % 20 < 10)) {
					sideIcons[1].drawNonzeroIndexed(53, 11);
				}
				if (tabInterfaceIDs[2] != -1 && (anInt1054 != 2 || loopCycle % 20 < 10)) {
					sideIcons[2].drawNonzeroIndexed(82, 11);
				}
				if (tabInterfaceIDs[3] != -1 && (anInt1054 != 3 || loopCycle % 20 < 10)) {
					sideIcons[3].drawNonzeroIndexed(115, 12);
				}
				if (tabInterfaceIDs[4] != -1 && (anInt1054 != 4 || loopCycle % 20 < 10)) {
					sideIcons[4].drawNonzeroIndexed(153, 13);
				}
				if (tabInterfaceIDs[5] != -1 && (anInt1054 != 5 || loopCycle % 20 < 10)) {
					sideIcons[5].drawNonzeroIndexed(180, 11);
				}
				if (tabInterfaceIDs[6] != -1 && (anInt1054 != 6 || loopCycle % 20 < 10)) {
					sideIcons[6].drawNonzeroIndexed(208, 13);
				}
			}
			aRSImageProducer_1125.drawGraphics(160, super.graphics, 516);
			aRSImageProducer_1124.setAsDrawingArea();
			backBase2.drawNonzeroIndexed(0, 0);
			if (invOverlayInterfaceID == -1) {
				if (tabInterfaceIDs[tabID] != -1) {
					if (tabID == 7 && ClientSettings.CUSTOM_SETTINGS_TAB) {
						redStone1_3.drawNonzeroIndexed(42, 0);
					}
					if (tabID == 8) {
						redStone2_3.drawNonzeroIndexed(74, 0);
					}
					if (tabID == 9) {
						redStone2_3.drawNonzeroIndexed(102, 0);
					}
					if (tabID == 10) {
						redStone3_2.drawNonzeroIndexed(130, 1);
					}
					if (tabID == 11) {
						redStone2_4.drawNonzeroIndexed(173, 0);
					}
					if (tabID == 12) {
						redStone2_4.drawNonzeroIndexed(201, 0);
					}
					if (tabID == 13) {
						redStone1_4.drawNonzeroIndexed(229, 0);
					}
				}
				if (tabInterfaceIDs[7] != -1 && (anInt1054 != 7 || loopCycle % 20 < 10) && ClientSettings.CUSTOM_SETTINGS_TAB) {
					sideIcons[10].drawNonzeroIndexed(47, 2);
				}
				if (tabInterfaceIDs[8] != -1 && (anInt1054 != 8 || loopCycle % 20 < 10)) {
					sideIcons[7].drawNonzeroIndexed(74, 2);
				}
				if (tabInterfaceIDs[9] != -1 && (anInt1054 != 9 || loopCycle % 20 < 10)) {
					sideIcons[8].drawNonzeroIndexed(102, 3);
				}
				if (tabInterfaceIDs[10] != -1 && (anInt1054 != 10 || loopCycle % 20 < 10)) {
					sideIcons[9].drawNonzeroIndexed(137, 4);
				}
				if (tabInterfaceIDs[11] != -1 && (anInt1054 != 11 || loopCycle % 20 < 10)) {
					sideIcons[10].drawNonzeroIndexed(174, 2);
				}
				if (tabInterfaceIDs[12] != -1 && (anInt1054 != 12 || loopCycle % 20 < 10)) {
					sideIcons[11].drawNonzeroIndexed(201, 2);
				}
				if (tabInterfaceIDs[13] != -1 && (anInt1054 != 13 || loopCycle % 20 < 10)) {
					sideIcons[12].drawNonzeroIndexed(226, 2);
				}
			}
			aRSImageProducer_1124.drawGraphics(466, super.graphics, 496);
			aRSImageProducer_1165.setAsDrawingArea();
			Texture.lineOffsets = chatBoxAreaOffsets;
		}
		if (aBoolean1233) {
			aBoolean1233 = false;
			aRSImageProducer_1123.setAsDrawingArea();
			backBase1.drawNonzeroIndexed(0, 0);
			aTextDrawingArea_1271.drawCenteredTaggedText(0xffffff, 55, "Public chat", 28, true);
			if (publicChatMode == 0) {
				aTextDrawingArea_1271.drawCenteredTaggedText(0x00ff00, 55, "On", 41, true);
			}
			if (publicChatMode == 1) {
				aTextDrawingArea_1271.drawCenteredTaggedText(0xffff00, 55, "Friends", 41, true);
			}
			if (publicChatMode == 2) {
				aTextDrawingArea_1271.drawCenteredTaggedText(0xff0000, 55, "Off", 41, true);
			}
			if (publicChatMode == 3) {
				aTextDrawingArea_1271.drawCenteredTaggedText(0x00ffff, 55, "Hide", 41, true);
			}
			aTextDrawingArea_1271.drawCenteredTaggedText(0xffffff, 184, "Private chat", 28, true);
			if (privateChatMode == 0) {
				aTextDrawingArea_1271.drawCenteredTaggedText(0x00ff00, 184, "On", 41, true);
			}
			if (privateChatMode == 1) {
				aTextDrawingArea_1271.drawCenteredTaggedText(0xffff00, 184, "Friends", 41, true);
			}
			if (privateChatMode == 2) {
				aTextDrawingArea_1271.drawCenteredTaggedText(0xff0000, 184, "Off", 41, true);
			}
			aTextDrawingArea_1271.drawCenteredTaggedText(0xffffff, 324, "Trade/compete", 28, true);
			if (tradeMode == 0) {
				aTextDrawingArea_1271.drawCenteredTaggedText(0x00ff00, 324, "On", 41, true);
			}
			if (tradeMode == 1) {
				aTextDrawingArea_1271.drawCenteredTaggedText(0xffff00, 324, "Friends", 41, true);
			}
			if (tradeMode == 2) {
				aTextDrawingArea_1271.drawCenteredTaggedText(0xff0000, 324, "Off", 41, true);
			}
			aTextDrawingArea_1271.drawCenteredTaggedText(0xffffff, 458, "Report abuse", 33, true);
			aRSImageProducer_1123.drawGraphics(453, super.graphics, 0);
			aRSImageProducer_1165.setAsDrawingArea();
			Texture.lineOffsets = chatBoxAreaOffsets;
		}
		cyclesSinceDraw = 0;
	}

	public boolean buildFriendsListMenu(RSInterface class9) {
		int i = class9.contentType;
		if (i >= 1 && i <= 200 || i >= 701 && i <= 900) {
			if (i >= 801) {
				i -= 701;
			} else if (i >= 701) {
				i -= 601;
			} else if (i >= 101) {
				i -= 101;
			} else {
				i--;
			}
			menuActionName[menuActionRow] = "Remove @whi@" + friendsList[i];
			menuActionID[menuActionRow] = 792;
			menuActionRow++;
			menuActionName[menuActionRow] = "Message @whi@" + friendsList[i];
			menuActionID[menuActionRow] = 639;
			menuActionRow++;
			return true;
		}
		if (i >= 401 && i <= 500) {
			menuActionName[menuActionRow] = "Remove @whi@" + class9.disabledText;
			menuActionID[menuActionRow] = 322;
			menuActionRow++;
			return true;
		} else {
			return false;
		}
	}

	public void updateTransientGraphics() {
		StationaryGraphic graphic = (StationaryGraphic) transientGraphics.firstFromTail();
		for (; graphic != null; graphic = (StationaryGraphic) transientGraphics.nextTowardHead()) {
			if (graphic.plane != plane || graphic.finished) {
				graphic.unlink();
			} else if (loopCycle >= graphic.startCycle) {
				graphic.advanceAnimation(cyclesSinceDraw);
				if (graphic.finished) {
					graphic.unlink();
				} else {
					worldController.addEntity(graphic.plane, 0, graphic.height, -1, graphic.worldX, 60, graphic.worldY, graphic, false);
				}
			}
		}

	}

	public void drawInterface(int scrollPos, int k, RSInterface class9, int l) {
		if (class9.type != 0 || class9.children == null) {
			return;
		}
		if (class9.hidden && anInt1026 != class9.id && anInt1048 != class9.id && anInt1039 != class9.id) {
			return;
		}
		int i1 = DrawingArea.topX;
		int j1 = DrawingArea.topY;
		int k1 = DrawingArea.bottomX;
		int l1 = DrawingArea.bottomY;
		DrawingArea.setClip(l + class9.height, k, k + class9.width, l);
		int i2 = class9.children.length;
		for (int j2 = 0; j2 < i2; j2++) {
			int k2 = class9.childX[j2] + k;
			int l2 = class9.childY[j2] + l - scrollPos;
			RSInterface component = RSInterface.interfaceCache[class9.children[j2]];
			k2 += component.offsetX;
			l2 += component.offsetY;
			if (component.contentType > 0) {
				drawFriendsListOrWelcomeScreen(component);
			}
			if (component.type == 0) {
				if (component.scrollPosition > component.scrollMax - component.height) {
					component.scrollPosition = component.scrollMax - component.height;
				}
				if (component.scrollPosition < 0) {
					component.scrollPosition = 0;
				}
				drawInterface(component.scrollPosition, k2, component, l2);
				if (component.scrollMax > component.height) {
					drawScrollbar(component.height, component.scrollPosition, l2, k2 + component.width, component.scrollMax);
				}
			} else if (component.type != 1) {
				if (component.type == 2) {
					int i3 = 0;
					for (int l3 = 0; l3 < component.height; l3++) {
						for (int l4 = 0; l4 < component.width; l4++) {
							int k5 = k2 + l4 * (32 + component.invSpritePadX);
							int j6 = l2 + l3 * (32 + component.invSpritePadY);
							if (i3 < 20) {
								k5 += component.spritesX[i3];
								j6 += component.spritesY[i3];
							}
							if (component.itemIdsPlusOne[i3] > 0) {
								int k6 = 0;
								int j7 = 0;
								int j9 = component.itemIdsPlusOne[i3] - 1;
								if (k5 > DrawingArea.topX - 32 && k5 < DrawingArea.bottomX && j6 > DrawingArea.topY - 32 && j6 < DrawingArea.bottomY || activeInterfaceType != 0 && anInt1085 == i3) {
									int l9 = 0;
									if (itemSelected == 1 && anInt1283 == i3 && anInt1284 == component.id) {
										l9 = 0xffffff;
									}
									Sprite class30_sub2_sub1_sub1_2 = ItemDef.getSprite(j9, component.itemQuantities[i3], l9);
									if (class30_sub2_sub1_sub1_2 != null) {
										if (activeInterfaceType != 0 && anInt1085 == i3 && anInt1084 == component.id) {
											k6 = super.mouseX - anInt1087;
											j7 = super.mouseY - anInt1088;
											if (k6 < 5 && k6 > -5) {
												k6 = 0;
											}
											if (j7 < 5 && j7 > -5) {
												j7 = 0;
											}
											if (anInt989 < 5) {
												k6 = 0;
												j7 = 0;
											}
											// Draw item being moved
											class30_sub2_sub1_sub1_2.drawHalfBlended(k5 + k6, j6 + j7);
											if (j6 + j7 < DrawingArea.topY && class9.scrollPosition > 0) {
												int i10 = cyclesSinceDraw * (DrawingArea.topY - j6 - j7) / 3;
												if (i10 > cyclesSinceDraw * 10) {
													i10 = cyclesSinceDraw * 10;
												}
												if (i10 > class9.scrollPosition) {
													i10 = class9.scrollPosition;
												}
												class9.scrollPosition -= i10;
												anInt1088 += i10;
											}
											if (j6 + j7 + 32 > DrawingArea.bottomY && class9.scrollPosition < class9.scrollMax - class9.height) {
												int j10 = cyclesSinceDraw * (j6 + j7 + 32 - DrawingArea.bottomY) / 3;
												if (j10 > cyclesSinceDraw * 10) {
													j10 = cyclesSinceDraw * 10;
												}
												if (j10 > class9.scrollMax - class9.height - class9.scrollPosition) {
													j10 = class9.scrollMax - class9.height - class9.scrollPosition;
												}
												class9.scrollPosition += j10;
												anInt1088 -= j10;
											}
										} else if (atInventoryInterfaceType != 0 && atInventoryIndex == i3 && atInventoryInterface == component.id) {
											// Using item? wear/unequip etc
											class30_sub2_sub1_sub1_2.drawHalfBlended(k5, j6);
										} else {
											// Draw item in inventory/equipment etc
											class30_sub2_sub1_sub1_2.drawSprite(k5, j6);
										}
										if (class30_sub2_sub1_sub1_2.trimWidth == 33 || component.itemQuantities[i3] != 1) {
											// Draw item amounts
											int k10 = component.itemQuantities[i3];
											aTextDrawingArea_1270.drawLiteralText(0, intToKOrMil(k10), j6 + 10 + j7, k5 + 1 + k6); // shadow
											aTextDrawingArea_1270.drawLiteralText(0xffff00, intToKOrMil(k10), j6 + 9 + j7, k5 + k6); // top layer
										}
									}
								}
							} else if (component.sprites != null && i3 < 20) {
								Sprite class30_sub2_sub1_sub1_1 = component.sprites[i3];
								if (class30_sub2_sub1_sub1_1 != null) {
									// Empty slots in equipment
									class30_sub2_sub1_sub1_1.drawSprite(k5, j6);
								}
							}
							i3++;
						}

					}

				} else if (component.type == 3) {
					boolean flag = false;
					if (anInt1039 == component.id || anInt1048 == component.id || anInt1026 == component.id) {
						flag = true;
					}
					int color;
					if (interfaceIsSelected(component)) {
						color = component.enabledColor;
						if (flag && component.enabledHoverColor != 0) {
							color = component.enabledHoverColor;
						}
					} else {
						color = component.textColor;
						if (flag && component.disabledHoverColor != 0) {
							color = component.disabledHoverColor;
						}
					}
					if (component.transparency == 0) {
						if (component.filled) {
							DrawingArea.fillRectangle(component.height, l2, 0x2a251e, component.width, k2);
						} else {
							DrawingArea.drawRectangleOutline(l2, component.height, color, k2, component.width);
						}
					} else if (component.filled) {
						DrawingArea.fillBlendedRectangle(color, l2, component.width, component.height, 256 - (component.transparency & 0xff), k2);
					} else {
						DrawingArea.drawBlendedRectangleOutline(l2, component.height, 256 - (component.transparency & 0xff), color, component.width, k2);
					}
				} else if (component.type == 4) {
					TextDrawingArea textDrawingArea = component.textDrawingAreas;
					String s = component.disabledText;
					boolean flag1 = false;
					if (anInt1039 == component.id || anInt1048 == component.id || anInt1026 == component.id) {
						flag1 = true;
					}
					int i4;
					if (interfaceIsSelected(component)) {
						i4 = component.enabledColor;
						if (flag1 && component.enabledHoverColor != 0) {
							i4 = component.enabledHoverColor;
						}
						if (component.enabledText.length() > 0) {
							s = component.enabledText;
						}
					} else {
						i4 = component.textColor;
						if (flag1 && component.disabledHoverColor != 0) {
							i4 = component.disabledHoverColor;
						}
					}
					if (component.atActionType == 6 && aBoolean1149) {
						s = "Please wait...";
						i4 = component.textColor;
					}
					if (DrawingArea.width == 479) {
						if (i4 == 0xffff00) {
							i4 = 255;
						}
						if (i4 == 49152) {
							i4 = 0xffffff;
						}
					}
					for (int l6 = l2 + textDrawingArea.maxAsciiGlyphHeightPixels; s.length() > 0; l6 += textDrawingArea.maxAsciiGlyphHeightPixels) {
						if (s.indexOf("%") != -1) {
							do {
								int k7 = s.indexOf("%1");
								if (k7 == -1) {
									break;
								}
								s = s.substring(0, k7) + interfaceIntToString(extractInterfaceValues(component, 0)) + s.substring(k7 + 2);
							} while (true);
							do {
								int l7 = s.indexOf("%2");
								if (l7 == -1) {
									break;
								}
								s = s.substring(0, l7) + interfaceIntToString(extractInterfaceValues(component, 1)) + s.substring(l7 + 2);
							} while (true);
							do {
								int i8 = s.indexOf("%3");
								if (i8 == -1) {
									break;
								}
								s = s.substring(0, i8) + interfaceIntToString(extractInterfaceValues(component, 2)) + s.substring(i8 + 2);
							} while (true);
							do {
								int j8 = s.indexOf("%4");
								if (j8 == -1) {
									break;
								}
								s = s.substring(0, j8) + interfaceIntToString(extractInterfaceValues(component, 3)) + s.substring(j8 + 2);
							} while (true);
							do {
								int k8 = s.indexOf("%5");
								if (k8 == -1) {
									break;
								}
								s = s.substring(0, k8) + interfaceIntToString(extractInterfaceValues(component, 4)) + s.substring(k8 + 2);
							} while (true);
						}
						int l8 = s.indexOf("\\n");
						String s1;
						if (l8 != -1) {
							s1 = s.substring(0, l8);
							s = s.substring(l8 + 2);
						} else {
							s1 = s;
							s = "";
						}
						if (component.centeredText) {
							textDrawingArea.drawCenteredTaggedText(i4, k2 + component.width / 2, s1, l6, component.shadowedText);
						} else {
							textDrawingArea.drawTaggedText(component.shadowedText, k2, i4, s1, l6);
						}
					}

				} else if (component.type == 5) {
					Sprite sprite;
					if (interfaceIsSelected(component)) {
						sprite = component.sprite2;
					} else {
						sprite = component.sprite1;
					}
					if (sprite != null) {
						sprite.drawSprite(k2, l2);
					}
				} else if (component.type == 6) {
					int k3 = Texture.textureInt1;
					int j4 = Texture.textureInt2;
					Texture.textureInt1 = k2 + component.width / 2;
					Texture.textureInt2 = l2 + component.height / 2;
					int i5 = Texture.sineTable[component.modelPitch] * component.modelZoom >> 16;
					int l5 = Texture.cosineTable[component.modelPitch] * component.modelZoom >> 16;
					boolean flag2 = interfaceIsSelected(component);
					int i7;
					if (flag2) {
						i7 = component.enabledAnimationId;
					} else {
						i7 = component.disabledAnimationId;
					}
					Model model;
					if (i7 == -1) {
						model = component.getMediaModel(-1, -1, flag2);
					} else {
						Animation animation = Animation.anims[i7];
						model = component.getMediaModel(animation.secondFrameIds[component.modelAnimationFrame], animation.frameIds[component.modelAnimationFrame], flag2);
					}
					if (model != null) {
						model.projectRotated(component.modelYaw, 0, component.modelPitch, 0, i5, l5);
					}
					Texture.textureInt1 = k3;
					Texture.textureInt2 = j4;
				} else if (component.type == 7) {
					TextDrawingArea textDrawingArea_1 = component.textDrawingAreas;
					int k4 = 0;
					for (int j5 = 0; j5 < component.height; j5++) {
						for (int i6 = 0; i6 < component.width; i6++) {
							if (component.itemIdsPlusOne[k4] > 0) {
								ItemDef itemDef = ItemDef.forID(component.itemIdsPlusOne[k4] - 1);
								String s2 = itemDef.name;
								if (itemDef.stackable || component.itemQuantities[k4] != 1) {
									s2 = s2 + " x" + intToKOrMilLongName(component.itemQuantities[k4]);
								}
								int i9 = k2 + i6 * (115 + component.invSpritePadX);
								int k9 = l2 + j5 * (12 + component.invSpritePadY);
								if (component.centeredText) {
									textDrawingArea_1.drawCenteredTaggedText(component.textColor, i9 + component.width / 2, s2, k9, component.shadowedText);
								} else {
									textDrawingArea_1.drawTaggedText(component.shadowedText, i9, component.textColor, s2, k9);
								}
							}
							k4++;
						}

					}

				} else if(component.type == 8) {
					
				}
			}
		}

		DrawingArea.setClip(l1, i1, k1, j1);
	}

	public void randomizeBackground(Background background) {
		int j = 256;
		for (int k = 0; k < anIntArray1190.length; k++) {
			anIntArray1190[k] = 0;
		}

		for (int l = 0; l < 5000; l++) {
			int i1 = (int) (Math.random() * 128D * (double) j);
			anIntArray1190[i1] = (int) (Math.random() * 256D);
		}

		for (int j1 = 0; j1 < 20; j1++) {
			for (int k1 = 1; k1 < j - 1; k1++) {
				for (int i2 = 1; i2 < 127; i2++) {
					int k2 = i2 + (k1 << 7);
					anIntArray1191[k2] = (anIntArray1190[k2 - 1] + anIntArray1190[k2 + 1] + anIntArray1190[k2 - 128] + anIntArray1190[k2 + 128]) / 4;
				}

			}

			int ai[] = anIntArray1190;
			anIntArray1190 = anIntArray1191;
			anIntArray1191 = ai;
		}

		if (background != null) {
			int l1 = 0;
			for (int j2 = 0; j2 < background.imageHeight; j2++) {
				for (int l2 = 0; l2 < background.imageWidth; l2++) {
					if (background.paletteIndices[l1++] != 0) {
						int i3 = l2 + 16 + background.offsetX;
						int j3 = j2 + 16 + background.offsetY;
						int k3 = i3 + (j3 << 7);
						anIntArray1190[k3] = 0;
					}
				}

			}

		}
	}

	public void decodePlayerUpdateMask(int updateMask, int playerIndex, Stream stream, Player player) {
		if ((updateMask & 0x400) != 0) {
			player.anInt1543 = stream.readUnsignedByte128MinusValue();
			player.anInt1545 = stream.readUnsignedByte128MinusValue();
			player.anInt1544 = stream.readUnsignedByte128MinusValue();
			player.anInt1546 = stream.readUnsignedByte128MinusValue();
			player.anInt1547 = stream.readUnsignedShortLittleEndianSubtract128() + loopCycle;
			player.anInt1548 = stream.readUnsignedShortBigEndianSubtract128() + loopCycle;
			player.anInt1549 = stream.readUnsignedByte128MinusValue();
			player.method446();
		}
		if ((updateMask & 0x100) != 0) {
			player.anInt1520 = stream.readUnsignedShortLittleEndian();
			int packedSpotData = stream.readIntBigEndian();
			player.anInt1524 = packedSpotData >> 16;
			player.anInt1523 = loopCycle + (packedSpotData & 0xffff);
			player.anInt1521 = 0;
			player.anInt1522 = 0;
			if (player.anInt1523 > loopCycle) {
				player.anInt1521 = -1;
			}
			if (player.anInt1520 == 0x00ffff) {
				player.anInt1520 = -1;
			}
			//processSound(player.anInt1520, 0, player, null);
		}
		if ((updateMask & 8) != 0) {
			int animationId = stream.readUnsignedShortLittleEndian();
			if (animationId == 0x00ffff) {
				animationId = -1;
			}
			//processSound(animationId, 1, player, null);
			int animationDelay = stream.readUnsignedNegatedByte();
			if (animationId == player.anim && animationId != -1) {
				int replayMode = Animation.anims[animationId].replayMode;
				if (replayMode == 1) {
					player.anInt1527 = 0;
					player.anInt1528 = 0;
					player.anInt1529 = animationDelay;
					player.anInt1530 = 0;
				}
				if (replayMode == 2) {
					player.anInt1530 = 0;
				}
			} else if (animationId == -1 || player.anim == -1 || Animation.anims[animationId].priority >= Animation.anims[player.anim].priority) {
				player.anim = animationId;
				player.anInt1527 = 0;
				player.anInt1528 = 0;
				player.anInt1529 = animationDelay;
				player.anInt1530 = 0;
				player.anInt1542 = player.smallXYIndex;
			}
		}
		if ((updateMask & 4) != 0) {
			player.textSpoken = stream.readLfTerminatedString();
			if (player.textSpoken.charAt(0) == '~') {
				player.textSpoken = player.textSpoken.substring(1);
				pushMessage(player.textSpoken, 2, player.name);
			} else if (player == myPlayer) {
				pushMessage(player.textSpoken, 2, player.name);
			}
			player.anInt1513 = 0;
			player.anInt1531 = 0;
			player.textCycle = 150;
		}
		if ((updateMask & 0x80) != 0) {
			int i1 = stream.readUnsignedShortLittleEndian();
			int j2 = stream.readUnsignedByte();
			int compressedChatLength = stream.readUnsignedNegatedByte();
			int chatPayloadStart = stream.currentOffset;
			if (player.name != null && player.visible) {
				long encodedSpeakerName = TextClass.encodeBase37Name(player.name);
				boolean ignoredSpeaker = false;
				if (j2 <= 1) {
					for (int ignoreIndex = 0; ignoreIndex < ignoreCount; ignoreIndex++) {
						if (ignoreListAsLongs[ignoreIndex] != encodedSpeakerName) {
							continue;
						}
						ignoredSpeaker = true;
						break;
					}

				}
				if (!ignoredSpeaker && chatSuppressionZone == 0) {
					try {
						aStream_834.currentOffset = 0;
						stream.readReversedBytes(compressedChatLength, 0, aStream_834.buffer);
						aStream_834.currentOffset = 0;
						String s = TextInput.decodeChatText(compressedChatLength, aStream_834);
						s = Censor.doCensor(s);
						player.textSpoken = s;
						player.anInt1513 = i1 >> 8;
						player.privelage = j2;

						// entityMessage(player);

						player.anInt1531 = i1 & 0xff;
						player.textCycle = 150;
						if (j2 == 2 || j2 == 3) {
							pushMessage(s, 1, "@cr2@" + player.name);
						} else if (j2 == 1) {
							pushMessage(s, 1, "@cr1@" + player.name);
						} else {
							pushMessage(s, 2, player.name);
						}
					} catch (Exception exception) {
						Signlink.reporterror("cde2");
					}
				}
			}
			stream.currentOffset = chatPayloadStart + compressedChatLength;
		}
		if ((updateMask & 1) != 0) {
			player.interactingEntity = stream.readUnsignedShortLittleEndian();
			if (player.interactingEntity == 0x00ffff) {
				player.interactingEntity = -1;
			}
		}
		if ((updateMask & 0x10) != 0) {
			int appearanceLength = stream.readUnsignedNegatedByte();
			byte appearanceBytes[] = new byte[appearanceLength];
			Stream appearanceStream = new Stream(appearanceBytes);
			stream.readBytes(appearanceLength, 0, appearanceBytes);
			aStreamArray895s[playerIndex] = appearanceStream;
			player.updatePlayer(appearanceStream);
		}
		if ((updateMask & 2) != 0) {
			player.anInt1538 = stream.readUnsignedShortLittleEndianSubtract128();
			player.anInt1539 = stream.readUnsignedShortLittleEndian();
		}
		if ((updateMask & 0x20) != 0) {
			int hitDamage = stream.readUnsignedByte();
			int hitType = stream.readUnsignedByteSubtract128();
			player.updateHitData(hitType, hitDamage, loopCycle);
			player.loopCycleStatus = loopCycle + 300;
			player.currentHealth = stream.readUnsignedNegatedByte();
			player.maxHealth = stream.readUnsignedByte();
		}
		if ((updateMask & 0x200) != 0) {
			int hitDamage = stream.readUnsignedByte();
			int hitType = stream.readUnsignedByte128MinusValue();
			player.updateHitData(hitType, hitDamage, loopCycle);
			player.loopCycleStatus = loopCycle + 300;
			player.currentHealth = stream.readUnsignedByte();
			player.maxHealth = stream.readUnsignedNegatedByte();
		}
	}

	public void updateCameraFollow() {
		try {
			int targetWorldX = myPlayer.x + anInt1278;
			int targetWorldY = myPlayer.y + anInt1131;
			if (anInt1014 - targetWorldX < -500 || anInt1014 - targetWorldX > 500 || anInt1015 - targetWorldY < -500 || anInt1015 - targetWorldY > 500) {
				anInt1014 = targetWorldX;
				anInt1015 = targetWorldY;
			}
			if (anInt1014 != targetWorldX) {
				anInt1014 += (targetWorldX - anInt1014) / 16;
			}
			if (anInt1015 != targetWorldY) {
				anInt1015 += (targetWorldY - anInt1015) / 16;
			}
			if (super.keyArray[1] == 1) {
				anInt1186 += (-24 - anInt1186) / 2;
			} else if (super.keyArray[2] == 1) {
				anInt1186 += (24 - anInt1186) / 2;
			} else {
				anInt1186 /= 2;
			}
			if (super.keyArray[3] == 1) {
				anInt1187 += (12 - anInt1187) / 2;
			} else if (super.keyArray[4] == 1) {
				anInt1187 += (-12 - anInt1187) / 2;
			} else {
				anInt1187 /= 2;
			}
			minimapInt1 = minimapInt1 + anInt1186 / 2 & 0x7ff;
			anInt1184 += anInt1187 / 2;
			if (anInt1184 < 128) {
				anInt1184 = 128;
			}
			if (anInt1184 > 383) {
				anInt1184 = 383;
			}
			int cameraTileX = anInt1014 >> 7;
			int cameraTileY = anInt1015 >> 7;
			int centerGroundHeight = interpolatedGroundHeight(plane, anInt1015, anInt1014);
			int maximumNearbyClearance = 0;
			if (cameraTileX > 3 && cameraTileY > 3 && cameraTileX < 100 && cameraTileY < 100) {
				for (int scanTileX = cameraTileX - 4; scanTileX <= cameraTileX + 4; scanTileX++) {
					for (int scanTileY = cameraTileY - 4; scanTileY <= cameraTileY + 4; scanTileY++) {
						int heightPlane = plane;
						if (heightPlane < 3 && (byteGroundArray[1][scanTileX][scanTileY] & 2) == 2) {
							heightPlane++;
						}
						int sampleClearance = centerGroundHeight - intGroundArray[heightPlane][scanTileX][scanTileY];
						if (sampleClearance > maximumNearbyClearance) {
							maximumNearbyClearance = sampleClearance;
						}
					}

				}

			}
			anInt1005++;
			if (anInt1005 > 1512) {
				anInt1005 = 0;
				stream.writeEncryptedOpcode(77);
				stream.writeByte(0);
				int i2 = stream.currentOffset;
				stream.writeByte((int) (Math.random() * 256D));
				stream.writeByte(101);
				stream.writeByte(233);
				stream.writeShortBigEndian(45092);
				if ((int) (Math.random() * 2D) == 0) {
					stream.writeShortBigEndian(35784);
				}
				stream.writeByte((int) (Math.random() * 256D));
				stream.writeByte(64);
				stream.writeByte(38);
				stream.writeShortBigEndian((int) (Math.random() * 65536D));
				stream.writeShortBigEndian((int) (Math.random() * 65536D));
				stream.backpatchBytePayloadLength(stream.currentOffset - i2);
			}
			int targetCameraDistance = maximumNearbyClearance * 192;
			if (targetCameraDistance > 0x17f00) {
				targetCameraDistance = 0x17f00;
			}
			if (targetCameraDistance < 32768) {
				targetCameraDistance = 32768;
			}
			if (targetCameraDistance > anInt984) {
				anInt984 += (targetCameraDistance - anInt984) / 24;
				return;
			}
			if (targetCameraDistance < anInt984) {
				anInt984 += (targetCameraDistance - anInt984) / 80;
			}
		} catch (Exception _ex) {
			Signlink.reporterror("glfc_ex " + myPlayer.x + "," + myPlayer.y + "," + anInt1014 + "," + anInt1015 + "," + anInt1069 + "," + anInt1070 + "," + baseX + "," + baseY);
			throw new RuntimeException("eek");
		}
	}

	public void processDrawing() {
		if (rsAlreadyLoaded || loadingError || genericLoadingError) {
			showErrorScreen();
			return;
		}
		anInt1061++;
		if (ClientSettings.ANDROID && AndroidHooks.isAppInBackground()) {
			anInt1213 = 0;
			return;
		}
		if (!loggedIn) {
			drawLoginScreen(false);
		} else {
			drawGameScreen();
		}
		anInt1213 = 0;
	}

	public boolean isFriendOrSelf(String s) {
		if (s == null) {
			return false;
		}
		for (int i = 0; i < friendsCount; i++) {
			if (s.equalsIgnoreCase(friendsList[i])) {
				return true;
			}
		}
		return s.equalsIgnoreCase(myPlayer.name);
	}

	public static String combatLevelColorTag(int localCombatLevel, int targetCombatLevel) {
		int levelDifference = localCombatLevel - targetCombatLevel;
		if (levelDifference < -9) {
			return "@red@";
		}
		if (levelDifference < -6) {
			return "@or3@";
		}
		if (levelDifference < -3) {
			return "@or2@";
		}
		if (levelDifference < 0) {
			return "@or1@";
		}
		if (levelDifference > 9) {
			return "@gre@";
		}
		if (levelDifference > 6) {
			return "@gr3@";
		}
		if (levelDifference > 3) {
			return "@gr2@";
		}
		if (levelDifference > 0) {
			return "@gr1@";
		} else {
			return "@yel@";
		}
	}

	public void setWaveVolume(int volume) {
		Signlink.wavevol = volume;
	}

	public void draw3dScreen() {
		drawSplitpublicChat();
		if (crossType == 1) {
			crosses[crossIndex / 100].drawSprite(crossX - 8 - 4, crossY - 8 - 4);
			anInt1142++;
			if (anInt1142 > 67) {
				anInt1142 = 0;
				stream.writeEncryptedOpcode(78);
			}
		}
		if (crossType == 2) {
			crosses[4 + crossIndex / 100].drawSprite(crossX - 8 - 4, crossY - 8 - 4);
		}
		if (anInt1018 != -1) {
			advanceInterfaceModelAnimations(cyclesSinceDraw, anInt1018);
			drawInterface(0, 0, RSInterface.interfaceCache[anInt1018], 0);
		}
		if (openInterfaceID != -1) {
			advanceInterfaceModelAnimations(cyclesSinceDraw, openInterfaceID);
			drawInterface(0, 0, RSInterface.interfaceCache[openInterfaceID], 0);
		}
		updateChatSuppressionZone();
		if (!menuOpen) {
			processRightClick();
			drawTooltip();
		} else if (menuScreenArea == 0) {
			drawMenu();
		}
		if (anInt1055 == 1) {
			multiOverlay.drawSprite(472, 296);
		}
		if (anInt1104 != 0) {
			String message = "";
			int seconds = anInt1104 / 50;
			int minutes = seconds / 60;
			int hours = minutes / 60;
			seconds %= 60;
			minutes %= 60;
			if (hours > 0) {
				if (hours < 10)
					message += "0";
				message += hours + ":";
			}
			if (minutes > 0) {
				if (minutes < 10)
					message += "0";
				message += minutes + ":";
			}
			if (seconds < 10)
				message += "0";
			message += seconds + "";

			// Draw the menu
			int debugItems = 1;
			int debugWidth = chatTextDrawingArea.measureTaggedTextWidth("System update in: 00:00") + 10; //140;
			int debugHeight = 25 + (debugItems * 15);
			int debugX = 511 - debugWidth;
			int debugY = 334 - debugHeight;
			int fill = 0x5d5447;
			int fill2 = RSColors.BLACK;
			int opacity = 140;
	
			DrawingArea.fillBlendedRectangle(fill, debugY, debugWidth, debugHeight, opacity, debugX);
			DrawingArea.fillBlendedRectangle(fill2, debugY + 1, debugWidth - 2, 16, opacity, debugX + 1);
			DrawingArea.drawRectangleOutline(debugY + 18, debugHeight - 19, fill2, debugX + 1, debugWidth - 2);
			chatTextDrawingArea.drawLiteralText(RSColors.WHITE_DARKER, "Notification", debugY += 14, debugX + 3);

			// Bump Y value
			debugY += 3;
			
			// Draw items
			chatTextDrawingArea.drawTaggedText(true, debugX + 4, RSColors.WHITE, "System update in:", debugY += 15);
			chatTextDrawingArea.drawRightAlignedTaggedText(true, debugX + debugWidth - 4, RSColors.YELLOW, message, debugY);

			// Not sure what this is for
			anInt849++;
			if (anInt849 > 75) {
				anInt849 = 0;
				stream.writeEncryptedOpcode(148);
			}
		}
	}

	public void addIgnore(long l) {
		try {
			if (l == 0L) {
				return;
			}
			if (ignoreCount >= 100) {
				pushMessage("Your ignore list is full. Max of 100 hit", 0, "");
				return;
			}
			String s = TextClass.formatDisplayName(TextClass.decodeBase37Name(l));
			for (int j = 0; j < ignoreCount; j++) {
				if (ignoreListAsLongs[j] == l) {
					pushMessage(s + " is already on your ignore list", 0, "");
					return;
				}
			}
			for (int k = 0; k < friendsCount; k++) {
				if (friendsListAsLongs[k] == l) {
					pushMessage("Please remove " + s + " from your friend list first", 0, "");
					return;
				}
			}

			ignoreListAsLongs[ignoreCount++] = l;
			needDrawTabArea = true;
			stream.writeEncryptedOpcode(133);
			stream.writeLongBigEndian(l);
			return;
		} catch (RuntimeException runtimeexception) {
			Signlink.reporterror("45688, " + l + ", " + 4 + ", " + runtimeexception.toString());
		}
		throw new RuntimeException();
	}

	public void advancePlayerEntities() {
		for (int playerListIndex = -1; playerListIndex < playerCount; playerListIndex++) {
			int playerIndex;
			if (playerListIndex == -1) {
				playerIndex = myPlayerIndex;
			} else {
				playerIndex = playerIndices[playerListIndex];
			}
			Player player = playerArray[playerIndex];
			if (player != null) {
				advanceEntityMovementAndAnimation(player);
			}
		}

	}

	public void advancePendingSceneChanges() {
		if (loadingStage == 2) {
			for (Class30_Sub1 pendingSceneChange = (Class30_Sub1) pendingSceneChanges.firstFromTail(); pendingSceneChange != null; pendingSceneChange = (Class30_Sub1) pendingSceneChanges.nextTowardHead()) {
				if (pendingSceneChange.restoreDelayTicks > 0) {
					pendingSceneChange.restoreDelayTicks--;
				}
				if (pendingSceneChange.restoreDelayTicks == 0) {
					if (pendingSceneChange.originalObjectId < 0 || ObjectManager.areObjectModelsLoaded(pendingSceneChange.originalObjectId, pendingSceneChange.originalShape)) {
						applySceneObjectChange(pendingSceneChange.tileY, pendingSceneChange.scenePlane, pendingSceneChange.originalOrientation, pendingSceneChange.originalShape, pendingSceneChange.tileX, pendingSceneChange.objectCategory, pendingSceneChange.originalObjectId);
						pendingSceneChange.unlink();
					}
				} else {
					if (pendingSceneChange.placementDelayTicks > 0) {
						pendingSceneChange.placementDelayTicks--;
					}
					if (pendingSceneChange.placementDelayTicks == 0 && pendingSceneChange.tileX >= 1 && pendingSceneChange.tileY >= 1 && pendingSceneChange.tileX <= 102 && pendingSceneChange.tileY <= 102 && (pendingSceneChange.requestedObjectId < 0 || ObjectManager.areObjectModelsLoaded(pendingSceneChange.requestedObjectId, pendingSceneChange.requestedShape))) {
						applySceneObjectChange(pendingSceneChange.tileY, pendingSceneChange.scenePlane, pendingSceneChange.requestedOrientation, pendingSceneChange.requestedShape, pendingSceneChange.tileX, pendingSceneChange.objectCategory, pendingSceneChange.requestedObjectId);
						pendingSceneChange.placementDelayTicks = -1;
						if (pendingSceneChange.requestedObjectId == pendingSceneChange.originalObjectId && pendingSceneChange.originalObjectId == -1) {
							pendingSceneChange.unlink();
						} else if (pendingSceneChange.requestedObjectId == pendingSceneChange.originalObjectId && pendingSceneChange.requestedOrientation == pendingSceneChange.originalOrientation && pendingSceneChange.requestedShape == pendingSceneChange.originalShape) {
							pendingSceneChange.unlink();
						}
					}
				}
			}

		}
	}

	public void determineMenuSize() {
		if (isScrollBarAt(super.saveClickX, super.saveClickY)) {
			return;
		}
		if (ClientSettings.ANDROID && (menuActionRow == 0 || (menuActionRow == 1 && menuActionID[0] == 1107))) {
			return;
		}
		int i = chatTextDrawingArea.measureTaggedTextWidth("Choose Option");
		for (int j = 0; j < menuActionRow; j++) {
			int k = chatTextDrawingArea.measureTaggedTextWidth(menuActionName[j]);
			if (k > i) {
				i = k;
			}
		}

		i += 8;
		int l = 15 * menuActionRow + 21;
		if (super.saveClickX > 4 && super.saveClickY > 4 && super.saveClickX < 516 && super.saveClickY < 338) {
			int i1 = super.saveClickX - 4 - i / 2;
			if (i1 + i > 512) {
				i1 = 512 - i;
			}
			if (i1 < 0) {
				i1 = 0;
			}
			int l1 = super.saveClickY - 4;
			if (l1 + l > 334) {
				l1 = 334 - l;
			}
			if (l1 < 0) {
				l1 = 0;
			}
			menuOpen = true;
			menuScreenArea = 0;
			menuOffsetX = i1;
			menuOffsetY = l1;
			menuWidth = i;
			anInt952 = 15 * menuActionRow + 22;
		}
		if (super.saveClickX > 553 && super.saveClickY > 205 && super.saveClickX < 743 && super.saveClickY < 466) {
			int j1 = super.saveClickX - 553 - i / 2;
			if (j1 < 0) {
				j1 = 0;
			} else if (j1 + i > 190) {
				j1 = 190 - i;
			}
			int i2 = super.saveClickY - 205;
			if (i2 < 0) {
				i2 = 0;
			} else if (i2 + l > 261) {
				i2 = 261 - l;
			}
			menuOpen = true;
			menuScreenArea = 1;
			menuOffsetX = j1;
			menuOffsetY = i2;
			menuWidth = i;
			anInt952 = 15 * menuActionRow + 22;
		}
		if (super.saveClickX > 17 && super.saveClickY > 357 && super.saveClickX < 496 && super.saveClickY < 453) {
			int k1 = super.saveClickX - 17 - i / 2;
			if (k1 < 0) {
				k1 = 0;
			} else if (k1 + i > 479) {
				k1 = 479 - i;
			}
			int j2 = super.saveClickY - 357;
			if (j2 < 0) {
				j2 = 0;
			} else if (j2 + l > 96) {
				j2 = 96 - l;
			}
			menuOpen = true;
			menuScreenArea = 2;
			menuOffsetX = k1;
			menuOffsetY = j2;
			menuWidth = i;
			anInt952 = 15 * menuActionRow + 22;
		}
	}

	public void decodeLocalPlayerMovement(Stream stream) {
		stream.beginBitReading();
		int hasMovementUpdate = stream.readBitsMsbFirst(1);
		if (hasMovementUpdate == 0) {
			return;
		}
		int movementType = stream.readBitsMsbFirst(2);
		if (movementType == 0) {
			maskUpdateIndices[maskUpdateCount++] = myPlayerIndex;
			return;
		}
		if (movementType == 1) {
			int walkDirection = stream.readBitsMsbFirst(3);
			myPlayer.moveInDir(false, walkDirection);
			int walkHasMask = stream.readBitsMsbFirst(1);
			if (walkHasMask == 1) {
				maskUpdateIndices[maskUpdateCount++] = myPlayerIndex;
			}
			return;
		}
		if (movementType == 2) {
			int firstRunDirection = stream.readBitsMsbFirst(3);
			myPlayer.moveInDir(true, firstRunDirection);
			int secondRunDirection = stream.readBitsMsbFirst(3);
			myPlayer.moveInDir(true, secondRunDirection);
			int runHasMask = stream.readBitsMsbFirst(1);
			if (runHasMask == 1) {
				maskUpdateIndices[maskUpdateCount++] = myPlayerIndex;
			}
			return;
		}
		if (movementType == 3) {
			boolean debugPlanes = false;
			if (debugPlanes) {
				System.out.println("plane is " + plane);
			}
			plane = stream.readBitsMsbFirst(2);
			if (debugPlanes) {
				System.out.println("plane is now " + plane);
			}
			int teleport = stream.readBitsMsbFirst(1);
			int relocationHasMask = stream.readBitsMsbFirst(1);
			if (relocationHasMask == 1) {
				maskUpdateIndices[maskUpdateCount++] = myPlayerIndex;
			}
			int localTileY = stream.readBitsMsbFirst(7);
			int localTileX = stream.readBitsMsbFirst(7);
			myPlayer.setPos(localTileX, localTileY, teleport == 1);
			//Handle plane changes, higher planes need the roof drawing checks disabled, and we need to manually update ObjectManager's scenePlaneFilter (plane) then manually call rebuildScene to let the ObjectManaegr know we're on a different plane. This makes plane changes draw properly. Coords for testing this: lumb: 3207 3227 and varrock: 3209, 3392 and gnome agil: 2474, 3427
			ObjectManager.hideRoofs = ClientSettings.HIDE_ROOFS;
			ObjectManager.hideBuggyVarrockSwordShopSnow = ClientSettings.HIDE_BUGGY_VARROCK_SWORD_SHOP_SNOW;
			if (plane > 0) {
				ObjectManager.hideRoofs = false;
				ObjectManager.hideBuggyVarrockSwordShopSnow = false;
			}
			ObjectManager.scenePlaneFilter = plane;
			rebuildScene();
		}
	}

	public void nullLoader() {
		aBoolean831 = false;
		while (drawingFlames) {
			aBoolean831 = false;
			try {
				Thread.sleep(50L);
			} catch (Exception _ex) {
			}
		}
		aBackground_966 = null;
		aBackground_967 = null;
		aBackgroundArray1152s = null;
		anIntArray850 = null;
		anIntArray851 = null;
		anIntArray852 = null;
		anIntArray853 = null;
		anIntArray1190 = null;
		anIntArray1191 = null;
		anIntArray828 = null;
		anIntArray829 = null;
		aClass30_Sub2_Sub1_Sub1_1201 = null;
		aClass30_Sub2_Sub1_Sub1_1202 = null;
	}

	public boolean advanceInterfaceModelAnimations(int elapsedTicks, int interfaceId) {
		boolean frameAdvanced = false;
		RSInterface parentInterface = RSInterface.interfaceCache[interfaceId];
		if (parentInterface == null || parentInterface.children == null)
			return frameAdvanced;
		for (int childInterfaceId : parentInterface.children) {
			if (childInterfaceId == -1) {
				break;
			}
			RSInterface childInterface = RSInterface.interfaceCache[childInterfaceId];
			if (childInterface.type == 1) {
				frameAdvanced |= advanceInterfaceModelAnimations(elapsedTicks, childInterface.id);
			}
			if (childInterface.type == 6 && (childInterface.disabledAnimationId != -1 || childInterface.enabledAnimationId != -1)) {
				boolean selected = interfaceIsSelected(childInterface);
				int animationId;
				if (selected) {
					animationId = childInterface.enabledAnimationId;
				} else {
					animationId = childInterface.disabledAnimationId;
				}
				if (animationId != -1) {
					Animation animation = Animation.anims[animationId];
					for (childInterface.modelAnimationTicks += elapsedTicks; childInterface.modelAnimationTicks > animation.getFrameDelay(childInterface.modelAnimationFrame);) {
						childInterface.modelAnimationTicks -= animation.getFrameDelay(childInterface.modelAnimationFrame) + 1;
						childInterface.modelAnimationFrame++;
						if (childInterface.modelAnimationFrame >= animation.frameCount) {
							childInterface.modelAnimationFrame -= animation.loopBackFrames;
							if (childInterface.modelAnimationFrame < 0 || childInterface.modelAnimationFrame >= animation.frameCount) {
								childInterface.modelAnimationFrame = 0;
							}
						}
						frameAdvanced = true;
					}

				}
			}
		}

		return frameAdvanced;
	}

	public int selectRoofVisibilityPlane() {
		int renderPlane = 3;
		if (yCameraCurve < 310) {
			int rayTileX = Math.max(0, Math.min(103, xCameraPos >> 7));
			int rayTileY = Math.max(0, Math.min(103, yCameraPos >> 7));
			int playerTileX = myPlayer.x >> 7;
			int playerTileY = myPlayer.y >> 7;
			if ((byteGroundArray[plane][rayTileX][rayTileY] & 4) != 0) {
				renderPlane = plane;
			}
			int tileDistanceX;
			if (playerTileX > rayTileX) {
				tileDistanceX = playerTileX - rayTileX;
			} else {
				tileDistanceX = rayTileX - playerTileX;
			}
			int tileDistanceY;
			if (playerTileY > rayTileY) {
				tileDistanceY = playerTileY - rayTileY;
			} else {
				tileDistanceY = rayTileY - playerTileY;
			}
			if (tileDistanceX > tileDistanceY) {
				int yStep16 = tileDistanceY * 0x10000 / tileDistanceX;
				int yAccumulator16 = 32768;
				while (rayTileX != playerTileX) {
					if (rayTileX < playerTileX) {
						rayTileX++;
					} else if (rayTileX > playerTileX) {
						rayTileX--;
					}
					if ((byteGroundArray[plane][rayTileX][rayTileY] & 4) != 0) {
						renderPlane = plane;
					}
					yAccumulator16 += yStep16;
					if (yAccumulator16 >= 0x10000) {
						yAccumulator16 -= 0x10000;
						if (rayTileY < playerTileY) {
							rayTileY++;
						} else if (rayTileY > playerTileY) {
							rayTileY--;
						}
						if ((byteGroundArray[plane][rayTileX][rayTileY] & 4) != 0) {
							renderPlane = plane;
						}
					}
				}
			} else {
				int xStep16 = tileDistanceX * 0x10000 / tileDistanceY;
				int xAccumulator16 = 32768;
				while (rayTileY != playerTileY) {
					if (rayTileY < playerTileY) {
						rayTileY++;
					} else if (rayTileY > playerTileY) {
						rayTileY--;
					}
					if ((byteGroundArray[plane][rayTileX][rayTileY] & 4) != 0) {
						renderPlane = plane;
					}
					xAccumulator16 += xStep16;
					if (xAccumulator16 >= 0x10000) {
						xAccumulator16 -= 0x10000;
						if (rayTileX < playerTileX) {
							rayTileX++;
						} else if (rayTileX > playerTileX) {
							rayTileX--;
						}
						if ((byteGroundArray[plane][rayTileX][rayTileY] & 4) != 0) {
							renderPlane = plane;
						}
					}
				}
			}
		}
		if ((byteGroundArray[plane][myPlayer.x >> 7][myPlayer.y >> 7] & 4) != 0) {
			renderPlane = plane;
		}
		return renderPlane;
	}

	public int selectFixedCameraPlane() {
		// Hide other planes when using fixed camera
		return plane;
		// int j = interpolatedGroundHeight(plane, yCameraPos, xCameraPos);
		// if (j - zCameraPos < 800 && (byteGroundArray[plane][xCameraPos >> 7][yCameraPos >> 7] & 4) != 0) {
		// 	return plane;
		// } else {
		// 	return 3;
		// }
	}

	public void delIgnore(long l) {
		try {
			if (l == 0L) {
				return;
			}
			for (int j = 0; j < ignoreCount; j++) {
				if (ignoreListAsLongs[j] == l) {
					ignoreCount--;
					needDrawTabArea = true;
					System.arraycopy(ignoreListAsLongs, j + 1, ignoreListAsLongs, j, ignoreCount - j);

					stream.writeEncryptedOpcode(74);
					stream.writeLongBigEndian(l);
					return;
				}
			}

			return;
		} catch (RuntimeException runtimeexception) {
			Signlink.reporterror("47229, " + 3 + ", " + l + ", " + runtimeexception.toString());
		}
		throw new RuntimeException();
	}

	public String getParameter(String s) {
		if (ClientSettings.ANDROID) {
			return "";
		}
		if (Signlink.mainapp != null) {
			return DesktopHooks.getParameter(s);
		} else {
			return super.getParameter(s);
		}
	}

	public int extractInterfaceValues(RSInterface class9, int j) {
		if (class9.valueIndexArray == null || j >= class9.valueIndexArray.length) {
			return -2;
		}
		try {
			int ai[] = class9.valueIndexArray[j];
			int k = 0;
			int l = 0;
			int i1 = 0;
			do {
				int j1 = ai[l++];
				int k1 = 0;
				byte byte0 = 0;
				if (j1 == 0) {
					return k;
				}
				if (j1 == 1) {
					k1 = currentStats[ai[l++]];
				}
				if (j1 == 2) {
					k1 = maxStats[ai[l++]];
				}
				if (j1 == 3) {
					k1 = currentExp[ai[l++]];
				}
				if (j1 == 4) {
					RSInterface class9_1 = RSInterface.interfaceCache[ai[l++]];
					int k2 = ai[l++];
					if (k2 >= 0 && k2 < ItemDef.totalItems && (!ItemDef.forID(k2).membersObject || isMembers)) {
						for (int j3 = 0; j3 < class9_1.itemIdsPlusOne.length; j3++) {
							if (class9_1.itemIdsPlusOne[j3] == k2 + 1) {
								k1 += class9_1.itemQuantities[j3];
							}
						}

					}
				}
				if (j1 == 5) {
					k1 = variousSettings[ai[l++]];
				}
				if (j1 == 6) {
					k1 = anIntArray1019[maxStats[ai[l++]] - 1];
				}
				if (j1 == 7) {
					k1 = variousSettings[ai[l++]] * 100 / 46875;
				}
				if (j1 == 8) {
					k1 = myPlayer.combatLevel;
				}
				if (j1 == 9) {
					for (int l1 = 0; l1 < Skills.skillsCount; l1++) {
						if (Skills.skillEnabled[l1]) {
							k1 += maxStats[l1];
						}
					}

				}
				if (j1 == 10) {
					RSInterface class9_2 = RSInterface.interfaceCache[ai[l++]];
					int l2 = ai[l++] + 1;
					if (l2 >= 0 && l2 < ItemDef.totalItems && (!ItemDef.forID(l2).membersObject || isMembers)) {
						for (int element : class9_2.itemIdsPlusOne) {
							if (element != l2) {
								continue;
							}
							k1 = 0x3b9ac9ff;
							break;
						}

					}
				}
				if (j1 == 11) {
					k1 = energy;
				}
				if (j1 == 12) {
					k1 = weight;
				}
				if (j1 == 13) {
					int i2 = variousSettings[ai[l++]];
					int i3 = ai[l++];
					k1 = (i2 & 1 << i3) == 0 ? 0 : 1;
				}
				if (j1 == 14) {
					int j2 = ai[l++];
					VarBit varBit = VarBit.cache[j2];
					int l3 = varBit.varpIndex;
					int i4 = varBit.lowBit;
					int j4 = varBit.highBit;
					int k4 = anIntArray1232[j4 - i4];
					k1 = variousSettings[l3] >> i4 & k4;
				}
				if (j1 == 15) {
					byte0 = 1;
				}
				if (j1 == 16) {
					byte0 = 2;
				}
				if (j1 == 17) {
					byte0 = 3;
				}
				if (j1 == 18) {
					k1 = (myPlayer.x >> 7) + baseX;
				}
				if (j1 == 19) {
					k1 = (myPlayer.y >> 7) + baseY;
				}
				if (j1 == 20) {
					k1 = ai[l++];
				}
				if (byte0 == 0) {
					if (i1 == 0) {
						k += k1;
					}
					if (i1 == 1) {
						k -= k1;
					}
					if (i1 == 2 && k1 != 0) {
						k /= k1;
					}
					if (i1 == 3) {
						k *= k1;
					}
					i1 = 0;
				} else {
					i1 = byte0;
				}
			} while (true);
		} catch (Exception _ex) {
			return -1;
		}
	}

	public void drawTooltip() {
		if (menuActionRow < 2 && itemSelected == 0 && spellSelected == 0) {
			return;
		}

		String s;
		if (itemSelected == 1 && menuActionRow < 2) {
			s = "Use " + selectedItemName + " with...";
		} else if (spellSelected == 1 && menuActionRow < 2) {
			s = spellTooltip + "...";
		} else {
			s = menuActionName[menuActionRow - 1];
		}
		if (menuActionRow > 2) {
			s = s + "@whi@ / " + (menuActionRow - 2) + " more options";
		}
		chatTextDrawingArea.drawSeededJitterText(4, 0xffffff, s, loopCycle / 1000, 15);
	}

	public void drawMinimap() {
		aRSImageProducer_1164.setAsDrawingArea();
		if (anInt1021 == 2) {
			byte backMaskIndices[] = mapBack.paletteIndices;
			int raster[] = DrawingArea.pixels;
			int maskLength = backMaskIndices.length;
			for (int pixelIndex = 0; pixelIndex < maskLength; pixelIndex++) {
				if (backMaskIndices[pixelIndex] == 0) {
					raster[pixelIndex] = 0;
				}
			}

			compass.drawRotatedScanlines(33, minimapInt1, anIntArray1057, 256, anIntArray968, 25, 0, 0, 33, 25);
			aRSImageProducer_1165.setAsDrawingArea();
			Texture.lineOffsets = chatBoxAreaOffsets;
			return;
		}
		int minimapRotation = minimapInt1 + minimapInt2 & 0x7ff;
		int sourceCenterX = 48 + myPlayer.x / 32;
		int sourceCenterY = 464 - myPlayer.y / 32;
		minimapSprite.drawRotatedScanlines(151, minimapRotation, anIntArray1229, 256 + minimapInt3, anIntArray1052, sourceCenterY, 5, 25, 146, sourceCenterX);
		compass.drawRotatedScanlines(33, minimapInt1, anIntArray1057, 256, anIntArray968, 25, 0, 0, 33, 25);
		// Minimap icons (shops, quest etc)
		for (int iconIndex = 0; iconIndex < mapFunctionIconCount; iconIndex++) {
			int iconOffsetX = mapFunctionIconTileX[iconIndex] * 4 + 2 - myPlayer.x / 32;
			int iconOffsetY = mapFunctionIconTileY[iconIndex] * 4 + 2 - myPlayer.y / 32;
			drawMinimapMarker(mapFunctionIconSprites[iconIndex], iconOffsetX, iconOffsetY);
		}

		for (int tileX = 0; tileX < 104; tileX++) {
			for (int tileY = 0; tileY < 104; tileY++) {
				NodeList groundItems = groundArray[plane][tileX][tileY];
				if (groundItems != null) {
					int itemOffsetX = tileX * 4 + 2 - myPlayer.x / 32;
					int itemOffsetY = tileY * 4 + 2 - myPlayer.y / 32;
					drawMinimapMarker(mapDotItem, itemOffsetX, itemOffsetY);
				}
			}
		}

		for (int npcSlot = 0; npcSlot < npcCount; npcSlot++) {
			NPC npc = npcArray[npcIndices[npcSlot]];
			if (npc != null && npc.isVisible()) {
				EntityDef entityDef = npc.desc;
				if (entityDef.childrenIDs != null) {
					entityDef = entityDef.getMorphedDefinition();
				}
				if (entityDef != null && entityDef.showsMinimapDot && entityDef.interactable) {
					int npcOffsetX = npc.x / 32 - myPlayer.x / 32;
					int npcOffsetY = npc.y / 32 - myPlayer.y / 32;
					drawMinimapMarker(mapDotNPC, npcOffsetX, npcOffsetY);
				}
			}
		}

		for (int playerSlot = 0; playerSlot < playerCount; playerSlot++) {
			Player player = playerArray[playerIndices[playerSlot]];
			if (player != null && player.isVisible()) {
				int playerOffsetX = player.x / 32 - myPlayer.x / 32;
				int playerOffsetY = player.y / 32 - myPlayer.y / 32;
				boolean teamDot = false;
				boolean onlineFriend = false;
				long nameHash = TextClass.encodeBase37Name(player.name);

				if (myPlayer.team != 0 && player.team != 0 && myPlayer.team == player.team || player.combatLevel == 0) {
					teamDot = true;
				}

				for (int friendSlot = 0; friendSlot < friendsCount; friendSlot++) {
					if (nameHash != friendsListAsLongs[friendSlot] || friendsNodeIDs[friendSlot] == 0) {
						continue;
					}
					onlineFriend = true;
					break;
				}

				if (teamDot) {
					drawMinimapMarker(mapDotTeam, playerOffsetX, playerOffsetY);
				} else if (onlineFriend) {
					drawMinimapMarker(mapDotFriend, playerOffsetX, playerOffsetY);
				} else {
					drawMinimapMarker(mapDotPlayer, playerOffsetX, playerOffsetY);
				}
			}
		}

		if (anInt855 != 0 && loopCycle % 20 < 10) {
			if (anInt855 == 1 && anInt1222 >= 0 && anInt1222 < npcArray.length) {
				NPC hintNpc = npcArray[anInt1222];
				if (hintNpc != null) {
					int hintNpcOffsetX = hintNpc.x / 32 - myPlayer.x / 32;
					int hintNpcOffsetY = hintNpc.y / 32 - myPlayer.y / 32;
					drawMinimapHint(mapMarker, hintNpcOffsetY, hintNpcOffsetX);
				}
			}
			if (anInt855 == 2) {
				int hintLocationOffsetX = (anInt934 - baseX) * 4 + 2 - myPlayer.x / 32;
				int hintLocationOffsetY = (anInt935 - baseY) * 4 + 2 - myPlayer.y / 32;
				drawMinimapHint(mapMarker, hintLocationOffsetY, hintLocationOffsetX);
			}
			if (anInt855 == 10 && anInt933 >= 0 && anInt933 < playerArray.length) {
				Player hintPlayer = playerArray[anInt933];
				if (hintPlayer != null) {
					int hintPlayerOffsetX = hintPlayer.x / 32 - myPlayer.x / 32;
					int hintPlayerOffsetY = hintPlayer.y / 32 - myPlayer.y / 32;
					drawMinimapHint(mapMarker, hintPlayerOffsetY, hintPlayerOffsetX);
				}
			}
		}
		if (destX != 0) {
			int destOffsetX = destX * 4 + 2 - myPlayer.x / 32;
			int destOffsetY = destY * 4 + 2 - myPlayer.y / 32;
			drawMinimapMarker(mapFlag, destOffsetX, destOffsetY);
		}
		// Draw player square on mini map
		DrawingArea.fillRectangle(3, 78, 0xffffff, 3, 97);
		aRSImageProducer_1165.setAsDrawingArea();
		Texture.lineOffsets = chatBoxAreaOffsets;
	}

	public void projectEntityToScreen(Entity entity, int heightOffset) {
		projectWorldToScreen(entity.x, heightOffset, entity.y);

		// aryan entity.entScreenX = spriteDrawX; entity.entScreenY =
		// spriteDrawY;
	}

	public void projectWorldToScreen(int worldX, int heightOffset, int worldY) {
		if (worldX < 128 || worldY < 128 || worldX > 13056 || worldY > 13056) {
			spriteDrawX = -1;
			spriteDrawY = -1;
			return;
		}
		int verticalPosition = interpolatedGroundHeight(plane, worldY, worldX) - heightOffset;
		worldX -= xCameraPos;
		verticalPosition -= zCameraPos;
		worldY -= yCameraPos;
		int pitchSine16 = Model.sineTable16[yCameraCurve];
		int pitchCosine16 = Model.cosineTable16[yCameraCurve];
		int yawSine16 = Model.sineTable16[xCameraCurve];
		int yawCosine16 = Model.cosineTable16[xCameraCurve];
		int rotatedComponent = worldY * yawSine16 + worldX * yawCosine16 >> 16;
		worldY = worldY * yawCosine16 - worldX * yawSine16 >> 16;
		worldX = rotatedComponent;
		rotatedComponent = verticalPosition * pitchCosine16 - worldY * pitchSine16 >> 16;
		worldY = verticalPosition * pitchSine16 + worldY * pitchCosine16 >> 16;
		verticalPosition = rotatedComponent;
		if (worldY >= 50) {
			spriteDrawX = Texture.textureInt1 + (worldX << 9) / worldY;
			spriteDrawY = Texture.textureInt2 + (verticalPosition << 9) / worldY;
		} else {
			spriteDrawX = -1;
			spriteDrawY = -1;
		}
	}

	public void buildSplitPrivateChatMenu() {
		if (splitpublicChat == 0) {
			return;
		}
		int i = 0;
		if (anInt1104 != 0) {
			i = 1;
		}
		for (int j = 0; j < 100; j++) {
			if (chatMessages[j] != null) {
				int k = chatTypes[j];
				String s = chatNames[j];
				if (s != null && s.startsWith("@cr1@")) {
					s = s.substring(5);
				}
				if (s != null && s.startsWith("@cr2@")) {
					s = s.substring(5);
				}
				if ((k == 3 || k == 7) && (k == 7 || privateChatMode == 0 || privateChatMode == 1 && isFriendOrSelf(s))) {
					int l = 329 - i * 13;
					if (super.mouseX > 4 && super.mouseY - 4 > l - 10 && super.mouseY - 4 <= l + 3) {
						int i1 = aTextDrawingArea_1271.measureTaggedTextWidth("From:  " + s + chatMessages[j]) + 25;
						if (i1 > 450) {
							i1 = 450;
						}
						if (super.mouseX < 4 + i1) {
							if (myPrivilege >= 1 && myPrivilege <= 3) {
								menuActionName[menuActionRow] = "Report abuse @whi@" + s;
								menuActionID[menuActionRow] = 2606;
								menuActionRow++;
							}
							menuActionName[menuActionRow] = "Add ignore @whi@" + s;
							menuActionID[menuActionRow] = 2042;
							menuActionRow++;
							menuActionName[menuActionRow] = "Reply to @whi@" + s;
							menuActionID[menuActionRow] = 2639;
							menuActionRow++;
							menuActionName[menuActionRow] = "Add friend @whi@" + s;
							menuActionID[menuActionRow] = 2337;
							menuActionRow++;
						}
					}
					if (++i >= 5) {
						return;
					}
				}
				if ((k == 5 || k == 6) && privateChatMode < 2 && ++i >= 5) {
					return;
				}
			}
		}

	}

	public void queueSceneObjectChange(int restoreDelayTicks, int requestedObjectId, int requestedOrientation, int objectCategory, int tileY, int requestedShape, int scenePlane, int tileX, int placementDelayTicks) {
		Class30_Sub1 pendingChange = null;
		for (Class30_Sub1 candidate = (Class30_Sub1) pendingSceneChanges.firstFromTail(); candidate != null; candidate = (Class30_Sub1) pendingSceneChanges.nextTowardHead()) {
			if (candidate.scenePlane != scenePlane || candidate.tileX != tileX || candidate.tileY != tileY || candidate.objectCategory != objectCategory) {
				continue;
			}
			pendingChange = candidate;
			break;
		}

		if (pendingChange == null) {
			pendingChange = new Class30_Sub1();
			pendingChange.scenePlane = scenePlane;
			pendingChange.objectCategory = objectCategory;
			pendingChange.tileX = tileX;
			pendingChange.tileY = tileY;
			snapshotOriginalSceneObject(pendingChange);
			pendingSceneChanges.insertHead(pendingChange);
		}
		pendingChange.requestedObjectId = requestedObjectId;
		pendingChange.requestedShape = requestedShape;
		pendingChange.requestedOrientation = requestedOrientation;
		pendingChange.placementDelayTicks = placementDelayTicks;
		pendingChange.restoreDelayTicks = restoreDelayTicks;
	}

	public boolean interfaceIsSelected(RSInterface component) {
		if (component.conditionOperators == null) {
			return false;
		}
		for (int conditionIndex = 0; conditionIndex < component.conditionOperators.length; conditionIndex++) {
			int actualValue = extractInterfaceValues(component, conditionIndex);
			int requiredValue = component.conditionValues[conditionIndex];
			if (component.conditionOperators[conditionIndex] == 2) {
				if (actualValue >= requiredValue) {
					return false;
				}
			} else if (component.conditionOperators[conditionIndex] == 3) {
				if (actualValue <= requiredValue) {
					return false;
				}
			} else if (component.conditionOperators[conditionIndex] == 4) {
				if (actualValue == requiredValue) {
					return false;
				}
			} else if (actualValue != requiredValue) {
				return false;
			}
		}

		return true;
	}

	public DataInputStream openJagGrabInputStream(String s) throws IOException {
		// if(!aBoolean872)
		// if(signlink.mainapp != null)
		// return signlink.openurl(s);
		// else
		// return new DataInputStream((new URL(getCodeBase(), s)).openStream());
		if (aSocket832 != null) {
			try {
				aSocket832.close();
			} catch (Exception _ex) {
			}
			aSocket832 = null;
		}
		aSocket832 = openSocket(43595);
		aSocket832.setSoTimeout(10000);
		java.io.InputStream inputstream = aSocket832.getInputStream();
		OutputStream outputstream = aSocket832.getOutputStream();
		outputstream.write(("JAGGRAB /" + s + "\n\n").getBytes());
		return new DataInputStream(inputstream);
	}
	
	public void doFlamesDrawing() {
		char c = '\u0100';
		if (anInt1040 > 0) {
			for (int i = 0; i < 256; i++) {
				if (anInt1040 > 768) {
					anIntArray850[i] = blendRgb256(anIntArray851[i], anIntArray852[i], 1024 - anInt1040);
				} else if (anInt1040 > 256) {
					anIntArray850[i] = anIntArray852[i];
				} else {
					anIntArray850[i] = blendRgb256(anIntArray852[i], anIntArray851[i], 256 - anInt1040);
				}
			}

		} else if (anInt1041 > 0) {
			for (int j = 0; j < 256; j++) {
				if (anInt1041 > 768) {
					anIntArray850[j] = blendRgb256(anIntArray851[j], anIntArray853[j], 1024 - anInt1041);
				} else if (anInt1041 > 256) {
					anIntArray850[j] = anIntArray853[j];
				} else {
					anIntArray850[j] = blendRgb256(anIntArray853[j], anIntArray851[j], 256 - anInt1041);
				}
			}

		} else {
			System.arraycopy(anIntArray851, 0, anIntArray850, 0, 256);

		}
		System.arraycopy(aClass30_Sub2_Sub1_Sub1_1201.pixels, 0, aRSImageProducer_1110.pixels, 0, 33920);

		int i1 = 0;
		int j1 = 1152;
		for (int k1 = 1; k1 < c - 1; k1++) {
			int l1 = anIntArray969[k1] * (c - k1) / c;
			int j2 = 22 + l1;
			if (j2 < 0) {
				j2 = 0;
			}
			i1 += j2;
			for (int l2 = j2; l2 < 128; l2++) {
				int j3 = anIntArray828[i1++];
				if (j3 != 0) {
					int l3 = j3;
					int j4 = 256 - j3;
					j3 = anIntArray850[j3];
					int l4 = aRSImageProducer_1110.pixels[j1];
					aRSImageProducer_1110.pixels[j1++] = ((j3 & 0xff00ff) * l3 + (l4 & 0xff00ff) * j4 & 0xff00ff00) + ((j3 & 0xff00) * l3 + (l4 & 0xff00) * j4 & 0xff0000) >> 8;
				} else {
					j1++;
				}
			}

			j1 += j2;
		}

		aRSImageProducer_1110.drawGraphics(0, super.graphics, 0);
		System.arraycopy(aClass30_Sub2_Sub1_Sub1_1202.pixels, 0, aRSImageProducer_1111.pixels, 0, 33920);

		i1 = 0;
		j1 = 1176;
		for (int k2 = 1; k2 < c - 1; k2++) {
			int i3 = anIntArray969[k2] * (c - k2) / c;
			int k3 = 103 - i3;
			j1 += i3;
			for (int i4 = 0; i4 < k3; i4++) {
				int k4 = anIntArray828[i1++];
				if (k4 != 0) {
					int i5 = k4;
					int j5 = 256 - k4;
					k4 = anIntArray850[k4];
					int k5 = aRSImageProducer_1111.pixels[j1];
					aRSImageProducer_1111.pixels[j1++] = ((k4 & 0xff00ff) * i5 + (k5 & 0xff00ff) * j5 & 0xff00ff00) + ((k4 & 0xff00) * i5 + (k5 & 0xff00) * j5 & 0xff0000) >> 8;
				} else {
					j1++;
				}
			}

			i1 += 128 - k3;
			j1 += 128 - k3 - i3;
		}

		aRSImageProducer_1111.drawGraphics(0, super.graphics, 637);
	}

	public void method134(Stream stream) {
		int j = stream.readBitsMsbFirst(8);
		if (j < playerCount) {
			for (int k = j; k < playerCount; k++) {
				anIntArray840[anInt839++] = playerIndices[k];
			}

		}
		if (j > playerCount) {
			Signlink.reporterror(myUsername + " Too many players");
			throw new RuntimeException("eek");
		}
		playerCount = 0;
		for (int l = 0; l < j; l++) {
			int i1 = playerIndices[l];
			Player player = playerArray[i1];
			int j1 = stream.readBitsMsbFirst(1);
			if (j1 == 0) {
				playerIndices[playerCount++] = i1;
				player.anInt1537 = loopCycle;
			} else {
				int k1 = stream.readBitsMsbFirst(2);
				if (k1 == 0) {
					playerIndices[playerCount++] = i1;
					player.anInt1537 = loopCycle;
					maskUpdateIndices[maskUpdateCount++] = i1;
				} else if (k1 == 1) {
					playerIndices[playerCount++] = i1;
					player.anInt1537 = loopCycle;
					int l1 = stream.readBitsMsbFirst(3);
					player.moveInDir(false, l1);
					int j2 = stream.readBitsMsbFirst(1);
					if (j2 == 1) {
						maskUpdateIndices[maskUpdateCount++] = i1;
					}
				} else if (k1 == 2) {
					playerIndices[playerCount++] = i1;
					player.anInt1537 = loopCycle;
					int i2 = stream.readBitsMsbFirst(3);
					player.moveInDir(true, i2);
					int k2 = stream.readBitsMsbFirst(3);
					player.moveInDir(true, k2);
					int l2 = stream.readBitsMsbFirst(1);
					if (l2 == 1) {
						maskUpdateIndices[maskUpdateCount++] = i1;
					}
				} else if (k1 == 3) {
					anIntArray840[anInt839++] = i1;
				}
			}
		}
	}

	public void drawLoginScreen(boolean flag) {
		resetImageProducers();
		aRSImageProducer_1109.setAsDrawingArea();
		aBackground_966.drawNonzeroIndexed(0, 0);
		char c = '\u0168';
		char c1 = '\310';
		if (loginScreenState == 0) {
			int i = c1 / 2 + 80;
			aTextDrawingArea_1270.drawCenteredTaggedText(0x75a9a9, c / 2, onDemandFetcher.statusString, i, true);
			i = c1 / 2 - 20;
			chatTextDrawingArea.drawCenteredTaggedText(0xffff00, c / 2, "Welcome to " + ClientSettings.SERVER_NAME + "", i, true);
			i += 30;
			int l = c / 2 - 80;
			int k1 = c1 / 2 + 20;
			aBackground_967.drawNonzeroIndexed(l - 73, k1 - 20);
			chatTextDrawingArea.drawCenteredTaggedText(0xffffff, l, "New User", k1 + 5, true);
			l = c / 2 + 80;
			aBackground_967.drawNonzeroIndexed(l - 73, k1 - 20);
			chatTextDrawingArea.drawCenteredTaggedText(0xffffff, l, "Existing User", k1 + 5, true);
		}
		if (loginScreenState == 2) {
			int j = c1 / 2 - 40;
			if (loginMessage1.length() > 0) {
				chatTextDrawingArea.drawCenteredTaggedText(0xffff00, c / 2, loginMessage1, j - 15, true);
				chatTextDrawingArea.drawCenteredTaggedText(0xffff00, c / 2, loginMessage2, j, true);
				j += 30;
			} else {
				chatTextDrawingArea.drawCenteredTaggedText(0xffff00, c / 2, loginMessage2, j - 7, true);
				j += 30;
			}
			chatTextDrawingArea.drawTaggedText(true, c / 2 - 90, 0xffffff, "Username: " + myUsername + (loginScreenCursorPos == 0 & loopCycle % 40 < 20 ? "@yel@|" : ""), j);
			j += 15;
			chatTextDrawingArea.drawTaggedText(true, c / 2 - 88, 0xffffff, "Password: " + TextClass.passwordAsterisks(myPassword) + (loginScreenCursorPos == 1 & loopCycle % 40 < 20 ? "@yel@|" : ""), j);
			j += 15;
			if (!flag) {
				int i1 = c / 2 - 80;
				int l1 = c1 / 2 + 50;
				aBackground_967.drawNonzeroIndexed(i1 - 73, l1 - 20);
				chatTextDrawingArea.drawCenteredTaggedText(0xffffff, i1, "Login", l1 + 5, true);
				i1 = c / 2 + 80;
				aBackground_967.drawNonzeroIndexed(i1 - 73, l1 - 20);
				chatTextDrawingArea.drawCenteredTaggedText(0xffffff, i1, "Cancel", l1 + 5, true);
			}
		}
		if (loginScreenState == 3) {
			chatTextDrawingArea.drawCenteredTaggedText(0xffff00, c / 2, "Create a free account", c1 / 2 - 60, true);
			int k = c1 / 2 - 35;
			chatTextDrawingArea.drawCenteredTaggedText(0xffffff, c / 2, "To create a new account just click", k, true);
			k += 15;
			chatTextDrawingArea.drawCenteredTaggedText(0xffffff, c / 2, "\"Cancel\" below, and click \"Existing User\".", k, true);
			k += 15;
			chatTextDrawingArea.drawCenteredTaggedText(0xffffff, c / 2, "Log in with any credentials you want and an", k, true);
			k += 15;
			chatTextDrawingArea.drawCenteredTaggedText(0xffffff, c / 2, "account will automatically be created for you.", k, true);
			k += 15;
			int j1 = c / 2;
			int i2 = c1 / 2 + 50;
			aBackground_967.drawNonzeroIndexed(j1 - 73, i2 - 20);
			chatTextDrawingArea.drawCenteredTaggedText(0xffffff, j1, "Cancel", i2 + 5, true);
		}
		aRSImageProducer_1109.drawGraphics(171, super.graphics, 202);
		if (welcomeScreenRaised) {
			welcomeScreenRaised = false;
			aRSImageProducer_1107.drawGraphics(0, super.graphics, 128);
			aRSImageProducer_1108.drawGraphics(371, super.graphics, 202);
			aRSImageProducer_1112.drawGraphics(265, super.graphics, 0);
			aRSImageProducer_1113.drawGraphics(265, super.graphics, 562);
			aRSImageProducer_1114.drawGraphics(171, super.graphics, 128);
			aRSImageProducer_1115.drawGraphics(171, super.graphics, 562);
		}
	}

	public void drawFlames() {
		drawingFlames = true;
		try {
			long l = System.currentTimeMillis();
			int i = 0;
			int j = 20;
			while (aBoolean831) {
				anInt1208++;
				calcFlamesPosition();
				calcFlamesPosition();
				doFlamesDrawing();
				if (++i > 10) {
					long l1 = System.currentTimeMillis();
					int k = (int) (l1 - l) / 10 - j;
					j = 40 - k;
					if (j < 5) {
						j = 5;
					}
					i = 0;
					l = l1;
				}
				try {
					Thread.sleep(j);
				} catch (Exception _ex) {
				}
			}
		} catch (Exception _ex) {
		}
		drawingFlames = false;
	}

	public void raiseWelcomeScreen() {
		welcomeScreenRaised = true;
	}

	public void method137(Stream stream, int j) {
		if (j == 84) {
			int k = stream.readUnsignedByte();
			int j3 = anInt1268 + (k >> 4 & 7);
			int i6 = anInt1269 + (k & 7);
			int l8 = stream.readUnsignedShortBigEndian();
			int k11 = stream.readUnsignedShortBigEndian();
			int l13 = stream.readUnsignedShortBigEndian();
			if (j3 >= 0 && i6 >= 0 && j3 < 104 && i6 < 104) {
				NodeList class19_1 = groundArray[plane][j3][i6];
				if (class19_1 != null) {
					for (Item class30_sub2_sub4_sub2_3 = (Item) class19_1.firstFromTail(); class30_sub2_sub4_sub2_3 != null; class30_sub2_sub4_sub2_3 = (Item) class19_1.nextTowardHead()) {
						if (class30_sub2_sub4_sub2_3.ID != (l8 & 0x7fff) || class30_sub2_sub4_sub2_3.amount != k11) {
							continue;
						}
						class30_sub2_sub4_sub2_3.amount = l13;
						break;
					}

					spawnGroundItem(j3, i6);
				}
			}
			return;
		}
		if (j == 105) {
			int l = stream.readUnsignedByte();
			int k3 = anInt1268 + (l >> 4 & 7);
			int j6 = anInt1269 + (l & 7);
			int i9 = stream.readUnsignedShortBigEndian();
			int l11 = stream.readUnsignedByte();
			int i14 = l11 >> 4 & 0xf;
			int i16 = l11 & 7;
			if (myPlayer.smallX[0] >= k3 - i14 && myPlayer.smallX[0] <= k3 + i14 && myPlayer.smallY[0] >= j6 - i14 && myPlayer.smallY[0] <= j6 + i14 && aBoolean848 && !lowMem && currentSound < 50) {
				sound[currentSound] = i9;
				soundType[currentSound] = i16;
				soundDelay[currentSound] = Sounds.leadingSilence20msUnits[i9];
				currentSound++;
			}
		}
		if (j == 215) {
			int i1 = stream.readUnsignedShortBigEndianSubtract128();
			int l3 = stream.readUnsignedByte128MinusValue();
			int k6 = anInt1268 + (l3 >> 4 & 7);
			int j9 = anInt1269 + (l3 & 7);
			int i12 = stream.readUnsignedShortBigEndianSubtract128();
			int j14 = stream.readUnsignedShortBigEndian();
			if (k6 >= 0 && j9 >= 0 && k6 < 104 && j9 < 104 && i12 != unknownInt10) {
				Item class30_sub2_sub4_sub2_2 = new Item();
				class30_sub2_sub4_sub2_2.ID = i1;
				class30_sub2_sub4_sub2_2.amount = j14;
				if (groundArray[plane][k6][j9] == null) {
					groundArray[plane][k6][j9] = new NodeList();
				}
				groundArray[plane][k6][j9].insertHead(class30_sub2_sub4_sub2_2);
				spawnGroundItem(k6, j9);
			}
			return;
		}
		if (j == 156) {
			int j1 = stream.readUnsignedByteSubtract128();
			int i4 = anInt1268 + (j1 >> 4 & 7);
			int l6 = anInt1269 + (j1 & 7);
			int k9 = stream.readUnsignedShortBigEndian();
			if (i4 >= 0 && l6 >= 0 && i4 < 104 && l6 < 104) {
				NodeList class19 = groundArray[plane][i4][l6];
				if (class19 != null) {
					for (Item item = (Item) class19.firstFromTail(); item != null; item = (Item) class19.nextTowardHead()) {
						if (item.ID != (k9 & 0x7fff)) {
							continue;
						}
						item.unlink();
						break;
					}

					if (class19.firstFromTail() == null) {
						groundArray[plane][i4][l6] = null;
					}
					spawnGroundItem(i4, l6);
				}
			}
			return;
		}
		if (j == 160) {
			int k1 = stream.readUnsignedByte128MinusValue();
			int j4 = anInt1268 + (k1 >> 4 & 7);
			int i7 = anInt1269 + (k1 & 7);
			int l9 = stream.readUnsignedByte128MinusValue();
			int j12 = l9 >> 2;
			int k14 = l9 & 3;
			int j16 = anIntArray1177[j12];
			int j17 = stream.readUnsignedShortBigEndianSubtract128();
			if (j4 >= 0 && i7 >= 0 && j4 < 103 && i7 < 103) {
				int j18 = intGroundArray[plane][j4][i7];
				int i19 = intGroundArray[plane][j4 + 1][i7];
				int l19 = intGroundArray[plane][j4 + 1][i7 + 1];
				int k20 = intGroundArray[plane][j4][i7 + 1];
				if (j16 == 0) {
					Object1 class10 = worldController.getWall(plane, j4, i7);
					if (class10 != null) {
						int k21 = class10.uid >> 14 & 0x7fff;
						if (j12 == 2) {
							class10.model = new AnimatedObject(k21, 4 + k14, 2, i19, l19, j18, k20, j17, false);
							class10.secondaryModel = new AnimatedObject(k21, k14 + 1 & 3, 2, i19, l19, j18, k20, j17, false);
						} else {
							class10.model = new AnimatedObject(k21, k14, j12, i19, l19, j18, k20, j17, false);
						}
					}
				}
				if (j16 == 1) {
					Object2 class26 = worldController.getWallDecoration(j4, i7, plane);
					if (class26 != null) {
						class26.model = new AnimatedObject(class26.uid >> 14 & 0x7fff, 0, 4, i19, l19, j18, k20, j17, false);
					}
				}
				if (j16 == 2) {
					Object5 class28 = worldController.getSceneObject(j4, i7, plane);
					if (j12 == 11) {
						j12 = 10;
					}
					if (class28 != null) {
						class28.model = new AnimatedObject(class28.uid >> 14 & 0x7fff, k14, j12, i19, l19, j18, k20, j17, false);
					}
				}
				if (j16 == 3) {
					Object3 class49 = worldController.getGroundDecoration(i7, j4, plane);
					if (class49 != null) {
						class49.model = new AnimatedObject(class49.uid >> 14 & 0x7fff, k14, 22, i19, l19, j18, k20, j17, false);
					}
				}
			}
			return;
		}
		if (j == 147) {
			int l1 = stream.readUnsignedByte128MinusValue();
			int k4 = anInt1268 + (l1 >> 4 & 7);
			int j7 = anInt1269 + (l1 & 7);
			int i10 = stream.readUnsignedShortBigEndian();
			byte byte0 = stream.readSignedByte128MinusValue();
			int l14 = stream.readUnsignedShortLittleEndian();
			byte byte1 = stream.readSignedNegatedByte();
			int k17 = stream.readUnsignedShortBigEndian();
			int k18 = stream.readUnsignedByte128MinusValue();
			int j19 = k18 >> 2;
			int i20 = k18 & 3;
			int l20 = anIntArray1177[j19];
			byte byte2 = stream.readSignedByte();
			int l21 = stream.readUnsignedShortBigEndian();
			byte byte3 = stream.readSignedNegatedByte();
			Player player;
			if (i10 == unknownInt10) {
				player = myPlayer;
			} else {
				player = playerArray[i10];
			}
			if (player != null) {
				ObjectDef class46 = ObjectDef.forID(l21);
				int i22 = intGroundArray[plane][k4][j7];
				int j22 = intGroundArray[plane][k4 + 1][j7];
				int k22 = intGroundArray[plane][k4 + 1][j7 + 1];
				int l22 = intGroundArray[plane][k4][j7 + 1];
				Model model = class46.getModel(j19, i20, i22, j22, k22, l22, -1);
				if (model != null) {
					queueSceneObjectChange(k17 + 1, -1, 0, l20, j7, 0, plane, k4, l14 + 1);
					player.attachedModelStartCycle = l14 + loopCycle;
					player.attachedModelEndCycle = k17 + loopCycle;
					player.attachedModel = model;
					int i23 = class46.sizeX;
					int j23 = class46.sizeY;
					if (i20 == 1 || i20 == 3) {
						i23 = class46.sizeY;
						j23 = class46.sizeX;
					}
					player.attachedModelX = k4 * 128 + i23 * 64;
					player.attachedModelY = j7 * 128 + j23 * 64;
					player.attachedModelHeight = interpolatedGroundHeight(plane, player.attachedModelY, player.attachedModelX);
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
					player.attachedMinTileX = k4 + byte2;
					player.attachedMaxTileX = k4 + byte0;
					player.attachedMinTileY = j7 + byte3;
					player.attachedMaxTileY = j7 + byte1;
				}
			}
		}
		if (j == 101) {
			int l2 = stream.readUnsignedNegatedByte();
			int k5 = l2 >> 2;
			int j8 = l2 & 3;
			int i11 = anIntArray1177[k5];
			int j13 = stream.readUnsignedByte();
			int k15 = anInt1268 + (j13 >> 4 & 7);
			int l16 = anInt1269 + (j13 & 7);

			if (k15 >= 0 && l16 >= 0 && k15 < 104 && l16 < 104) {
				queueSceneObjectChange(-1, -1, j8, i11, l16, k5, plane, k15, 0);
			}
			return;
		}
		if (j == 151) {
			int i2 = stream.readUnsignedByteSubtract128Duplicate();
			int l4 = anInt1268 + (i2 >> 4 & 7);
			int k7 = anInt1269 + (i2 & 7);
			int j10 = stream.readUnsignedShortLittleEndian();
			int k12 = stream.readUnsignedByte128MinusValue();
			int i15 = k12 >> 2;
			int k16 = k12 & 3;
			int l17 = anIntArray1177[i15];
			//System.out.println("id: " + j10 + " x:" + (this.baseX + anInt1268) + " y:" + (this.baseY + anInt1269));
			if (l4 >= 0 && k7 >= 0 && l4 < 104 && k7 < 104)
				queueSceneObjectChange(-1, j10, k16, l17, k7, i15, plane, l4, 0);
			return;
		}

		if (j == 4) {
			int j2 = stream.readUnsignedByte();
			int i5 = anInt1268 + (j2 >> 4 & 7);
			int l7 = anInt1269 + (j2 & 7);
			int k10 = stream.readUnsignedShortBigEndian();
			int l12 = stream.readUnsignedByte();
			int j15 = stream.readUnsignedShortBigEndian();
			if (i5 >= 0 && l7 >= 0 && i5 < 104 && l7 < 104) {
				i5 = i5 * 128 + 64;
				l7 = l7 * 128 + 64;
				StationaryGraphic graphic = new StationaryGraphic(plane, loopCycle, j15, k10, interpolatedGroundHeight(plane, l7, i5) - l12, l7, i5);
				transientGraphics.insertHead(graphic);
			}
			return;
		}
		if (j == 44) {
			int itemID = stream.readUnsignedShortLittleEndianSubtract128();
			int itemAmount = stream.readUnsignedShortBigEndian();
			int i8 = stream.readUnsignedByte();
			int l10 = anInt1268 + (i8 >> 4 & 7);
			int i13 = anInt1269 + (i8 & 7);
			if (l10 >= 0 && i13 >= 0 && l10 < 104 && i13 < 104) {
				Item gItem = new Item();
				gItem.ID = itemID;
				gItem.amount = itemAmount;
				if (groundArray[plane][l10][i13] == null) {
					groundArray[plane][l10][i13] = new NodeList();
				}
				groundArray[plane][l10][i13].insertHead(gItem);
				spawnGroundItem(l10, i13);
			}
			return;
		}
		if (j == 117) {
			int i3 = stream.readUnsignedByte();
			int l5 = anInt1268 + (i3 >> 4 & 7);
			int k8 = anInt1269 + (i3 & 7);
			int j11 = l5 + stream.readSignedByte();
			int k13 = k8 + stream.readSignedByte();
			int l15 = stream.readSignedShortBigEndian();
			int i17 = stream.readUnsignedShortBigEndian();
			int i18 = stream.readUnsignedByte() * 4;
			int l18 = stream.readUnsignedByte() * 4;
			int k19 = stream.readUnsignedShortBigEndian();
			int j20 = stream.readUnsignedShortBigEndian();
			int i21 = stream.readUnsignedByte();
			int j21 = stream.readUnsignedByte();
			if (l5 >= 0 && k8 >= 0 && l5 < 104 && k8 < 104 && j11 >= 0 && k13 >= 0 && j11 < 104 && k13 < 104 && i17 != 0x00ffff) {
				l5 = l5 * 128 + 64;
				k8 = k8 * 128 + 64;
				j11 = j11 * 128 + 64;
				k13 = k13 * 128 + 64;
				Projectile class30_sub2_sub4_sub4 = new Projectile(i21, l18, k19 + loopCycle, j20 + loopCycle, j21, plane, interpolatedGroundHeight(plane, k8, l5) - i18, k8, l5, l15, i17);
				class30_sub2_sub4_sub4.retarget(k19 + loopCycle, k13, interpolatedGroundHeight(plane, k13, j11) - l18, j11);
				projectiles.insertHead(class30_sub2_sub4_sub4);
			}
		}
	}

	public static void setLowMem() {
		WorldController.lowMem = true;
		Texture.lowMem = true;
		lowMem = true;
		ObjectManager.lowMem = true;
		ObjectDef.lowMem = true;
	}

	public void method139(Stream stream) {
		stream.beginBitReading();
		int k = stream.readBitsMsbFirst(8);
		if (k < npcCount) {
			for (int l = k; l < npcCount; l++) {
				anIntArray840[anInt839++] = npcIndices[l];
			}

		}
		if (k > npcCount) {
			Signlink.reporterror(myUsername + " Too many npcs");
			throw new RuntimeException("eek");
		}
		npcCount = 0;
		for (int i1 = 0; i1 < k; i1++) {
			int j1 = npcIndices[i1];
			NPC npc = npcArray[j1];
			int k1 = stream.readBitsMsbFirst(1);
			if (k1 == 0) {
				npcIndices[npcCount++] = j1;
				npc.anInt1537 = loopCycle;
			} else {
				int l1 = stream.readBitsMsbFirst(2);
				if (l1 == 0) {
					npcIndices[npcCount++] = j1;
					npc.anInt1537 = loopCycle;
					maskUpdateIndices[maskUpdateCount++] = j1;
				} else if (l1 == 1) {
					npcIndices[npcCount++] = j1;
					npc.anInt1537 = loopCycle;
					int i2 = stream.readBitsMsbFirst(3);
					npc.moveInDir(false, i2);
					int k2 = stream.readBitsMsbFirst(1);
					if (k2 == 1) {
						maskUpdateIndices[maskUpdateCount++] = j1;
					}
				} else if (l1 == 2) {
					npcIndices[npcCount++] = j1;
					npc.anInt1537 = loopCycle;
					int j2 = stream.readBitsMsbFirst(3);
					npc.moveInDir(true, j2);
					int l2 = stream.readBitsMsbFirst(3);
					npc.moveInDir(true, l2);
					int i3 = stream.readBitsMsbFirst(1);
					if (i3 == 1) {
						maskUpdateIndices[maskUpdateCount++] = j1;
					}
				} else if (l1 == 3) {
					anIntArray840[anInt839++] = j1;
				}
			}
		}

	}

	public void processLoginScreenInput() {
		if (loginScreenState == 0) {
			int i = super.myWidth / 2 - 80;
			int l = super.myHeight / 2 + 20;
			l += 20;
			if (super.clickMode3 == 1 && super.saveClickX >= i - 75 && super.saveClickX <= i + 75 && super.saveClickY >= l - 20 && super.saveClickY <= l + 20) {
				loginScreenState = 3;
				loginScreenCursorPos = 0;
			}
			i = super.myWidth / 2 + 80;
			if (super.clickMode3 == 1 && super.saveClickX >= i - 75 && super.saveClickX <= i + 75 && super.saveClickY >= l - 20 && super.saveClickY <= l + 20) {
				loginMessage1 = "";
				loginMessage2 = "Enter your username & password.";
				loginScreenState = 2;
				loginScreenCursorPos = 0;
			}
		} else {
			if (loginScreenState == 2) {
				int j = super.myHeight / 2 - 40;
				j += 30;
				j += 25;
				if (super.clickMode3 == 1 && super.saveClickY >= j - 15 && super.saveClickY < j) {
					loginScreenCursorPos = 0;
				}
				j += 15;
				if (super.clickMode3 == 1 && super.saveClickY >= j - 15 && super.saveClickY < j) {
					loginScreenCursorPos = 1;
				}
				j += 15;
				int i1 = super.myWidth / 2 - 80;
				int k1 = super.myHeight / 2 + 50;
				k1 += 20;
				if (super.clickMode3 == 1 && super.saveClickX >= i1 - 75 && super.saveClickX <= i1 + 75 && super.saveClickY >= k1 - 20 && super.saveClickY <= k1 + 20) {
					loginFailures = 0;
					login(myUsername, myPassword, false);
					if (loggedIn) {
						return;
					}
				}
				i1 = super.myWidth / 2 + 80;
				if (super.clickMode3 == 1 && super.saveClickX >= i1 - 75 && super.saveClickX <= i1 + 75 && super.saveClickY >= k1 - 20 && super.saveClickY <= k1 + 20) {
					loginScreenState = 0;
					// myUsername = "";
					// myPassword = "";
				}
				do {
					int l1 = readChar(-796);
					if (l1 == -1) {
						break;
					}
					boolean flag1 = false;
					for (int i2 = 0; i2 < validUserPassChars.length(); i2++) {
						if (l1 != validUserPassChars.charAt(i2)) {
							continue;
						}
						flag1 = true;
						break;
					}

					if (loginScreenCursorPos == 0) {
						if (l1 == 8 && myUsername.length() > 0) {
							myUsername = myUsername.substring(0, myUsername.length() - 1);
						}
						if (l1 == 9 || l1 == 10 || l1 == 13) {
							loginScreenCursorPos = 1;
						}
						if (flag1) {
							myUsername += (char) l1;
						}
						if (myUsername.length() > 12) {
							myUsername = myUsername.substring(0, 12);
						}
					} else if (loginScreenCursorPos == 1) {
						if (l1 == 8 && myPassword.length() > 0) {
							myPassword = myPassword.substring(0, myPassword.length() - 1);
						}
						if (l1 == 9 || l1 == 10 || l1 == 13) {
							login(myUsername, myPassword, false);
							loginScreenCursorPos = 0;
						}
						if (flag1) {
							myPassword += (char) l1;
						}
						if (myPassword.length() > 20) {
							myPassword = myPassword.substring(0, 20);
						}
					}
				} while (true);
				return;
			}
			if (loginScreenState == 3) {
				int k = super.myWidth / 2;
				int j1 = super.myHeight / 2 + 50;
				j1 += 20;
				if (super.clickMode3 == 1 && super.saveClickX >= k - 75 && super.saveClickX <= k + 75 && super.saveClickY >= j1 - 20 && super.saveClickY <= j1 + 20) {
					loginScreenState = 0;
				}
			}
		}
	}

	public void drawMinimapMarker(Sprite sprite, int offsetX, int offsetY) {
		int rotation2048 = minimapInt1 + minimapInt2 & 0x7ff;
		int distanceSquared = offsetX * offsetX + offsetY * offsetY;
		if (distanceSquared > 6400) {
			return;
		}
		int scaledSine16 = Model.sineTable16[rotation2048];
		int scaledCosine16 = Model.cosineTable16[rotation2048];
		scaledSine16 = scaledSine16 * 256 / (minimapInt3 + 256);
		scaledCosine16 = scaledCosine16 * 256 / (minimapInt3 + 256);
		int rotatedX = offsetY * scaledSine16 + offsetX * scaledCosine16 >> 16;
		int rotatedY = offsetY * scaledCosine16 - offsetX * scaledSine16 >> 16;
		if (distanceSquared > 2500) {
			sprite.drawNonzeroThroughZeroMask(mapBack, 83 - rotatedY - sprite.trimHeight / 2 - 4, 94 + rotatedX - sprite.trimWidth / 2 + 4);
		} else {
			sprite.drawSprite(94 + rotatedX - sprite.trimWidth / 2 + 4, 83 - rotatedY - sprite.trimHeight / 2 - 4);
		}
	}

	public void applySceneObjectChange(int tileY, int scenePlane, int orientation, int shape, int tileX, int objectCategory, int objectId) {
		if (tileX >= 1 && tileY >= 1 && tileX <= 102 && tileY <= 102) {
			if (lowMem && scenePlane != plane) {
				return;
			}
			int existingPackedUid = 0;
			if (objectCategory == 0) {
				existingPackedUid = worldController.getWallUid(scenePlane, tileX, tileY);
			}
			if (objectCategory == 1) {
				existingPackedUid = worldController.getWallDecorationUid(scenePlane, tileX, tileY);
			}
			if (objectCategory == 2) {
				existingPackedUid = worldController.getSceneObjectUid(scenePlane, tileX, tileY);
			}
			if (objectCategory == 3) {
				existingPackedUid = worldController.getGroundDecorationUid(scenePlane, tileX, tileY);
			}
			if (existingPackedUid != 0) {
				int existingObjectInfo = worldController.getObjectInfo(scenePlane, tileX, tileY, existingPackedUid);
				int existingObjectId = existingPackedUid >> 14 & 0x7fff;
				int existingShape = existingObjectInfo & 0x1f;
				int existingOrientation = existingObjectInfo >> 6;
				if (objectCategory == 0) {
					worldController.removeWall(tileX, scenePlane, tileY, (byte) -119);
					ObjectDef existingWallDefinition = ObjectDef.forID(existingObjectId);
					if (existingWallDefinition.blocksMovement) {
						aClass11Array1230[scenePlane].removeWall(existingOrientation, existingShape, existingWallDefinition.blocksProjectiles, tileX, tileY);
					}
				}
				if (objectCategory == 1) {
					worldController.removeWallDecoration(tileY, scenePlane, tileX);
				}
				if (objectCategory == 2) {
					worldController.removeSceneObject(scenePlane, tileX, tileY);
					ObjectDef class46_1 = ObjectDef.forID(existingObjectId);
					if (tileX + class46_1.sizeX > 103 || tileY + class46_1.sizeX > 103 || tileX + class46_1.sizeY > 103 || tileY + class46_1.sizeY > 103) {
						return;
					}
					if (class46_1.blocksMovement) {
						aClass11Array1230[scenePlane].removeObject(existingOrientation, class46_1.sizeX, tileX, tileY, class46_1.sizeY, class46_1.blocksProjectiles);
					}
				}
				if (objectCategory == 3) {
					worldController.removeGroundDecoration(scenePlane, tileY, tileX);
					ObjectDef class46_2 = ObjectDef.forID(existingObjectId);
					if (class46_2.blocksMovement && class46_2.hasActions) {
						aClass11Array1230[scenePlane].unblockTile(tileY, tileX);
					}
				}
			}
			if (objectId >= 0) {
				int heightPlane = scenePlane;
				if (heightPlane < 3 && (byteGroundArray[1][tileX][tileY] & 2) == 2) {
					heightPlane++;
				}
				ObjectManager.placeObjectWithoutShading(worldController, orientation, tileY, shape, heightPlane, aClass11Array1230[scenePlane], intGroundArray, tileX, objectId, scenePlane);
			}
		}
	}

	public void updatePlayers(int i, Stream stream) {
		anInt839 = 0;
		maskUpdateCount = 0;
		decodeLocalPlayerMovement(stream);
		method134(stream);
		addNewLocalPlayers(stream, i);
		decodePlayerUpdateMasks(stream);
		for (int k = 0; k < anInt839; k++) {
			int l = anIntArray840[k];
			if (playerArray[l].anInt1537 != loopCycle) {
				playerArray[l] = null;
			}
		}

		if (stream.currentOffset != i) {
			Signlink.reporterror("Error packet size mismatch in getplayer pos:" + stream.currentOffset + " psize:" + i);
			throw new RuntimeException("eek");
		}
		for (int i1 = 0; i1 < playerCount; i1++) {
			if (playerArray[playerIndices[i1]] == null) {
				Signlink.reporterror(myUsername + " null entry in pl list - pos:" + i1 + " size:" + playerCount);
				throw new RuntimeException("eek");
			}
		}

	}

	public void setCameraPos(int j, int k, int l, int i1, int j1, int k1) {
		int l1 = 2048 - k & 0x7ff;
		int i2 = 2048 - j1 & 0x7ff;
		int j2 = 0;
		int k2 = 0;
		int l2 = j;
		if (l1 != 0) {
			int i3 = Model.sineTable16[l1];
			int k3 = Model.cosineTable16[l1];
			int i4 = k2 * k3 - l2 * i3 >> 16;
			l2 = k2 * i3 + l2 * k3 >> 16;
			k2 = i4;
		}
		if (i2 != 0) {
			/*
			 * xxx if(cameratoggle){ if(zoom == 0) zoom = k2; if(lftrit == 0)
			 * lftrit = j2; if(fwdbwd == 0) fwdbwd = l2; k2 = zoom; j2 = lftrit;
			 * l2 = fwdbwd; }
			 */
			int j3 = Model.sineTable16[i2];
			int l3 = Model.cosineTable16[i2];
			int j4 = l2 * j3 + j2 * l3 >> 16;
			l2 = l2 * l3 - j2 * j3 >> 16;
			j2 = j4;
		}
		xCameraPos = l - j2;
		zCameraPos = i1 - k2;
		yCameraPos = k1 - l2;
		yCameraCurve = k;
		xCameraCurve = j1;
	}

	public boolean parsePacket() {
		if (socketStream == null) {
			return false;
		}
		try {
			int i = socketStream.available();
			if (i == 0) {
				return false;
			}
			if (pktType == -1) {
				socketStream.readFully(inStream.buffer, 1);
				pktType = inStream.buffer[0] & 0xff;
				if (encryption != null) {
					pktType = pktType - encryption.getNextKey() & 0xff;
				}
				pktSize = SizeConstants.packetSizes[pktType];
				i--;
			}
			if (pktSize == -1) {
				if (i > 0) {
					socketStream.readFully(inStream.buffer, 1);
					pktSize = inStream.buffer[0] & 0xff;
					i--;
				} else {
					return false;
				}
			}
			if (pktSize == -2) {
				if (i > 1) {
					socketStream.readFully(inStream.buffer, 2);
					inStream.currentOffset = 0;
					pktSize = inStream.readUnsignedShortBigEndian();
					i -= 2;
				} else {
					return false;
				}
			}
			if (i < pktSize) {
				return false;
			}
			inStream.currentOffset = 0;
			socketStream.readFully(inStream.buffer, pktSize);
			anInt1009 = 0;
			prevPktType2 = prevPktType;
			prevPktType = anInt841;
			anInt841 = pktType;
			if (pktType == 81) {
				updatePlayers(pktSize, inStream);
				aBoolean1080 = false;
				pktType = -1;
				return true;
			}
			if (pktType == 176) {
				daysSinceRecovChange = inStream.readUnsignedNegatedByte();
				unreadMessages = inStream.readUnsignedShortBigEndianSubtract128();
				membersInt = inStream.readUnsignedByte();
				anInt1193 = inStream.readIntOrder2143();
				daysSinceLastLogin = inStream.readUnsignedShortBigEndian();
				if (anInt1193 != 0 && openInterfaceID == -1) {
					Signlink.dnslookup(TextClass.formatIpv4Address(anInt1193));
					closeOpenInterfaces();
					char c = '\u028A';
					if (daysSinceRecovChange != 201 || membersInt == 1) {
						c = '\u028F';
					}
					reportAbuseInput = "";
					canMute = false;
					for (RSInterface element : RSInterface.interfaceCache) {
						if (element == null || element.contentType != c) {
							continue;
						}
						openInterfaceID = element.parentID;
						break;
					}

				}
				pktType = -1;
				return true;
			}
			if (pktType == 64) {
				anInt1268 = inStream.readUnsignedNegatedByte();
				anInt1269 = inStream.readUnsignedByte128MinusValue();
				for (int j = anInt1268; j < anInt1268 + 8; j++) {
					for (int l9 = anInt1269; l9 < anInt1269 + 8; l9++) {
						if (groundArray[plane][j][l9] != null) {
							groundArray[plane][j][l9] = null;
							spawnGroundItem(j, l9);
						}
					}

				}

				for (Class30_Sub1 class30_sub1 = (Class30_Sub1) pendingSceneChanges.firstFromTail(); class30_sub1 != null; class30_sub1 = (Class30_Sub1) pendingSceneChanges.nextTowardHead()) {
					if (class30_sub1.tileX >= anInt1268 && class30_sub1.tileX < anInt1268 + 8 && class30_sub1.tileY >= anInt1269 && class30_sub1.tileY < anInt1269 + 8 && class30_sub1.scenePlane == plane) {
						class30_sub1.restoreDelayTicks = 0;
					}
				}

				pktType = -1;
				return true;
			}
			if (pktType == 185) {
				int k = inStream.readUnsignedShortLittleEndianSubtract128();
				RSInterface.interfaceCache[k].mediaType = 3;
				if (myPlayer.desc == null) {
					RSInterface.interfaceCache[k].mediaID = (myPlayer.appearanceColours[0] << 25) + (myPlayer.appearanceColours[4] << 20) + (myPlayer.equipment[0] << 15) + (myPlayer.equipment[8] << 10) + (myPlayer.equipment[11] << 5) + myPlayer.equipment[1];
				} else {
					RSInterface.interfaceCache[k].mediaID = (int) (0x12345678L + myPlayer.desc.type);
				}
				pktType = -1;
				return true;
			}
			if (pktType == 107) {
				aBoolean1160 = false;
				for (int l = 0; l < 5; l++) {
					aBooleanArray876[l] = false;
				}

				pktType = -1;
				return true;
			}
			if (pktType == 72) {
				int i1 = inStream.readUnsignedShortLittleEndian();
				RSInterface class9 = RSInterface.interfaceCache[i1];
				for (int k15 = 0; k15 < class9.itemIdsPlusOne.length; k15++) {
					class9.itemIdsPlusOne[k15] = -1;
					class9.itemIdsPlusOne[k15] = 0;
				}

				pktType = -1;
				return true;
			}
			if (pktType == 214) {
				ignoreCount = pktSize / 8;
				for (int j1 = 0; j1 < ignoreCount; j1++) {
					ignoreListAsLongs[j1] = inStream.readLongBigEndian();
				}

				pktType = -1;
				return true;
			}
			if (pktType == 166) {
				aBoolean1160 = true;
				anInt1098 = inStream.readUnsignedByte();
				anInt1099 = inStream.readUnsignedByte();
				anInt1100 = inStream.readUnsignedShortBigEndian();
				anInt1101 = inStream.readUnsignedByte();
				anInt1102 = inStream.readUnsignedByte();
				if (anInt1102 >= 100) {
					xCameraPos = anInt1098 * 128 + 64;
					yCameraPos = anInt1099 * 128 + 64;
					zCameraPos = interpolatedGroundHeight(plane, yCameraPos, xCameraPos) - anInt1100;
				}
				pktType = -1;
				return true;
			}
			if (pktType == 134) {
				needDrawTabArea = true;
				int skillID = inStream.readUnsignedByte();
				int experience = inStream.readIntOrder3412();
				int level = inStream.readUnsignedByte();
				currentExp[skillID] = experience;
				currentStats[skillID] = level;
				maxStats[skillID] = 1;
				// Fix for current health not updating
				if (skillID == 3) {
					myPlayer.currentHealth = level;
				}
				for (int k20 = 0; k20 < 98; k20++) {
					if (experience >= anIntArray1019[k20]) {
						maxStats[skillID] = k20 + 2;
					}
				}

				pktType = -1;
				return true;
			}
			if (pktType == 71) {
				int l1 = inStream.readUnsignedShortBigEndian();
				int j10 = inStream.readUnsignedByteSubtract128();
				if (l1 == 0x00ffff) {
					l1 = -1;
				}
				tabInterfaceIDs[j10] = l1;
				needDrawTabArea = true;
				tabAreaAltered = true;
				pktType = -1;
				return true;
			}
			if (pktType == 74) {
				int i2 = inStream.readUnsignedShortLittleEndian();
				if (i2 == 0x00ffff) {
					i2 = -1;
				}
				if (i2 != -1 || previousSong != 0) {
					if (i2 != -1 && currentSong != i2 && musicVolume != 0 && previousSong == 0)
						requestSongWithFade(10, musicVolume, false, i2);
				} else
					stopMusic(false);
				currentSong = i2;
				pktType = -1;
				return true;
			}
			if (pktType == 121) {
				int i_60_ = inStream.readUnsignedShortLittleEndianSubtract128();
				int i_61_ = inStream.readUnsignedShortBigEndianSubtract128();
				if (i_61_ == 0x00ffff)
					i_61_ = -1;
				if (musicVolume != 0 && i_61_ != -1) {
					requestSongWithoutFade(musicVolume, false, i_60_);
					previousSong = i_61_*20;
				}
				pktType = -1;
				return true;
			}
			if (pktType == 109) {
				resetLogout();
				pktType = -1;
				return false;
			}
			if (pktType == 70) {
				int k2 = inStream.readSignedShortBigEndian();
				int l10 = inStream.readSignedShortLittleEndian();
				int i16 = inStream.readUnsignedShortLittleEndian();
				RSInterface class9_5 = RSInterface.interfaceCache[i16];
				class9_5.offsetX = k2;
				class9_5.offsetY = l10;
				pktType = -1;
				return true;
			}
			if (pktType == 73 || pktType == 241) {

				// mapReset();
				int centerChunkX = anInt1069;
				int centerChunkY = anInt1070;
				if (pktType == 73) {
					centerChunkX = inStream.readUnsignedShortBigEndianSubtract128();
					centerChunkY = inStream.readUnsignedShortBigEndian();
					aBoolean1159 = false;
				}
				if (pktType == 241) {
					centerChunkY = inStream.readUnsignedShortBigEndianSubtract128();
					inStream.beginBitReading();
					for (int destPlane = 0; destPlane < 4; destPlane++) {
						for (int destChunkX = 0; destChunkX < 13; destChunkX++) {
							for (int destChunkY = 0; destChunkY < 13; destChunkY++) {
								int hasTemplate = inStream.readBitsMsbFirst(1);
								if (hasTemplate == 1) {
									anIntArrayArrayArray1129[destPlane][destChunkX][destChunkY] = inStream.readBitsMsbFirst(26);
								} else {
									anIntArrayArrayArray1129[destPlane][destChunkX][destChunkY] = -1;
								}
							}

						}

					}

					inStream.finishBitReading();
					centerChunkX = inStream.readUnsignedShortBigEndian();
					aBoolean1159 = true;
				}
				if (anInt1069 == centerChunkX && anInt1070 == centerChunkY && loadingStage == 2) {
					pktType = -1;
					return true;
				}
				anInt1069 = centerChunkX;
				anInt1070 = centerChunkY;
				baseX = (anInt1069 - 6) * 8;
				baseY = (anInt1070 - 6) * 8;
				aBoolean1141 = (anInt1069 / 8 == 48 || anInt1069 / 8 == 49) && anInt1070 / 8 == 48;
				if (anInt1069 / 8 == 48 && anInt1070 / 8 == 148) {
					aBoolean1141 = true;
				}
				loadingStage = 1;
				aLong824 = System.currentTimeMillis();
				drawTextOnScreen(null, "Loading - please wait.");
				if (pktType == 73) {
					int regionCount = 0;
					for (int regionX = (anInt1069 - 6) / 8; regionX <= (anInt1069 + 6) / 8; regionX++) {
						for (int regionY = (anInt1070 - 6) / 8; regionY <= (anInt1070 + 6) / 8; regionY++) {
							regionCount++;
						}

					}

					regionTerrainData = new byte[regionCount][];
					regionObjectData = new byte[regionCount][];
					regionKeys = new int[regionCount];
					regionTerrainFileIds = new int[regionCount];
					regionObjectFileIds = new int[regionCount];
					regionCount = 0;
					for (int regionX = (anInt1069 - 6) / 8; regionX <= (anInt1069 + 6) / 8; regionX++) {
						for (int regionY = (anInt1070 - 6) / 8; regionY <= (anInt1070 + 6) / 8; regionY++) {
							regionKeys[regionCount] = (regionX << 8) + regionY;
							if (aBoolean1141 && (regionY == 49 || regionY == 149 || regionY == 147 || regionX == 50 || regionX == 49 && regionY == 47)) {
								regionTerrainFileIds[regionCount] = -1;
								regionObjectFileIds[regionCount] = -1;
								regionCount++;
							} else {
								int terrainFileId = regionTerrainFileIds[regionCount] = onDemandFetcher.getMapFileId(0, regionY, regionX);
								if (terrainFileId != -1) {
									onDemandFetcher.requestFile(3, terrainFileId);
								}
								int objectFileId = regionObjectFileIds[regionCount] = onDemandFetcher.getMapFileId(1, regionY, regionX);
								if (objectFileId != -1) {
									onDemandFetcher.requestFile(3, objectFileId);
								}
								regionCount++;
							}
						}

					}

				}
				if (pktType == 241) {
					int regionCount = 0;
					int uniqueRegionKeys[] = new int[676];
					for (int destPlane = 0; destPlane < 4; destPlane++) {
						for (int destChunkX = 0; destChunkX < 13; destChunkX++) {
							for (int destChunkY = 0; destChunkY < 13; destChunkY++) {
								int chunkTemplate = anIntArrayArrayArray1129[destPlane][destChunkX][destChunkY];
								if (chunkTemplate != -1) {
									int sourceChunkX = chunkTemplate >> 14 & 0x3ff;
									int sourceChunkY = chunkTemplate >> 3 & 0x7ff;
									int regionKey = (sourceChunkX / 8 << 8) + sourceChunkY / 8;
									for (int seenIndex = 0; seenIndex < regionCount; seenIndex++) {
										if (uniqueRegionKeys[seenIndex] != regionKey) {
											continue;
										}
										regionKey = -1;
										break;
									}

									if (regionKey != -1) {
										uniqueRegionKeys[regionCount++] = regionKey;
									}
								}
							}

						}

					}

					regionTerrainData = new byte[regionCount][];
					regionObjectData = new byte[regionCount][];
					regionKeys = new int[regionCount];
					regionTerrainFileIds = new int[regionCount];
					regionObjectFileIds = new int[regionCount];
					for (int regionIndex = 0; regionIndex < regionCount; regionIndex++) {
						int regionKey = regionKeys[regionIndex] = uniqueRegionKeys[regionIndex];
						int regionX = regionKey >> 8 & 0xff;
						int regionY = regionKey & 0xff;
						int terrainFileId = regionTerrainFileIds[regionIndex] = onDemandFetcher.getMapFileId(0, regionY, regionX);
						if (terrainFileId != -1) {
							onDemandFetcher.requestFile(3, terrainFileId);
						}
						int objectFileId = regionObjectFileIds[regionIndex] = onDemandFetcher.getMapFileId(1, regionY, regionX);
						if (objectFileId != -1) {
							onDemandFetcher.requestFile(3, objectFileId);
						}
					}

				}
				int deltaX = baseX - anInt1036;
				int deltaY = baseY - anInt1037;
				anInt1036 = baseX;
				anInt1037 = baseY;
				for (int npcIndex = 0; npcIndex < 16384; npcIndex++) {
					NPC npc = npcArray[npcIndex];
					if (npc != null) {
						for (int pathIndex = 0; pathIndex < 10; pathIndex++) {
							npc.smallX[pathIndex] -= deltaX;
							npc.smallY[pathIndex] -= deltaY;
						}

						npc.x -= deltaX * 128;
						npc.y -= deltaY * 128;
					}
				}

				for (int playerIndex = 0; playerIndex < maxPlayers; playerIndex++) {
					Player player = playerArray[playerIndex];
					if (player != null) {
						for (int pathIndex = 0; pathIndex < 10; pathIndex++) {
							player.smallX[pathIndex] -= deltaX;
							player.smallY[pathIndex] -= deltaY;
						}

						player.x -= deltaX * 128;
						player.y -= deltaY * 128;
					}
				}

				aBoolean1080 = true;
				byte startX = 0;
				byte endX = 104;
				byte stepX = 1;
				if (deltaX < 0) {
					startX = 103;
					endX = -1;
					stepX = -1;
				}
				byte startY = 0;
				byte endY = 104;
				byte stepY = 1;
				if (deltaY < 0) {
					startY = 103;
					endY = -1;
					stepY = -1;
				}
				for (int newTileX = startX; newTileX != endX; newTileX += stepX) {
					for (int newTileY = startY; newTileY != endY; newTileY += stepY) {
						int oldTileX = newTileX + deltaX;
						int oldTileY = newTileY + deltaY;
						for (int tilePlane = 0; tilePlane < 4; tilePlane++) {
							if (oldTileX >= 0 && oldTileY >= 0 && oldTileX < 104 && oldTileY < 104) {
								groundArray[tilePlane][newTileX][newTileY] = groundArray[tilePlane][oldTileX][oldTileY];
							} else {
								groundArray[tilePlane][newTileX][newTileY] = null;
							}
						}

					}

				}

				for (Class30_Sub1 pendingSceneChange = (Class30_Sub1) pendingSceneChanges.firstFromTail(); pendingSceneChange != null; pendingSceneChange = (Class30_Sub1) pendingSceneChanges.nextTowardHead()) {
					pendingSceneChange.tileX -= deltaX;
					pendingSceneChange.tileY -= deltaY;
					if (pendingSceneChange.tileX < 0 || pendingSceneChange.tileY < 0 || pendingSceneChange.tileX >= 104 || pendingSceneChange.tileY >= 104) {
						pendingSceneChange.unlink();
					}
				}

				if (destX != 0) {
					destX -= deltaX;
					destY -= deltaY;
				}
				aBoolean1160 = false;
				pktType = -1;
				return true;
			}
			if (pktType == 208) {
				int i3 = inStream.readSignedShortLittleEndian();
				if (i3 >= 0) {
					resetInterfaceModelAnimations(i3);
				}
				anInt1018 = i3;
				pktType = -1;
				return true;
			}
			if (pktType == 99) {
				anInt1021 = inStream.readUnsignedByte();
				pktType = -1;
				return true;
			}
			if (pktType == 75) {
				int j3 = inStream.readUnsignedShortLittleEndianSubtract128();
				int j11 = inStream.readUnsignedShortLittleEndianSubtract128();
				RSInterface.interfaceCache[j11].mediaType = 2;
				RSInterface.interfaceCache[j11].mediaID = j3;
				pktType = -1;
				return true;
			}
			if (pktType == 114) {
				anInt1104 = inStream.readUnsignedShortLittleEndian() * 30;
				pktType = -1;
				return true;
			}
			if (pktType == 60) {
				anInt1269 = inStream.readUnsignedByte();
				anInt1268 = inStream.readUnsignedNegatedByte();
				while (inStream.currentOffset < pktSize) {
					int k3 = inStream.readUnsignedByte();
					method137(inStream, k3);
				}
				pktType = -1;
				return true;
			}
			if (pktType == 35) {
				int l3 = inStream.readUnsignedByte();
				int k11 = inStream.readUnsignedByte();
				int j17 = inStream.readUnsignedByte();
				int k21 = inStream.readUnsignedByte();
				aBooleanArray876[l3] = true;
				anIntArray873[l3] = k11;
				anIntArray1203[l3] = j17;
				anIntArray928[l3] = k21;
				anIntArray1030[l3] = 0;
				pktType = -1;
				return true;
			}
			if (pktType == 174) {
				int id = inStream.readUnsignedShortBigEndian();
				int type = /*inStream.readUnsignedByte()*/1;
				int delay = inStream.readUnsignedShortBigEndian();
				int volume = inStream.readUnsignedByte();
				sound[currentSound] = id;
				soundType[currentSound] = type;
				soundDelay[currentSound] = delay + Sounds.leadingSilence20msUnits[id];
				soundVolume[currentSound] = volume;
				currentSound++;
				pktType = -1;
				return true;
			}
			if (pktType == 104) {
				int j4 = inStream.readUnsignedNegatedByte();
				int i12 = inStream.readUnsignedByteSubtract128();
				String s6 = inStream.readLfTerminatedString();
				if (j4 >= 1 && j4 <= 5) {
					if (s6.equalsIgnoreCase("null")) {
						s6 = null;
					}
					atPlayerActions[j4 - 1] = s6;
					atPlayerArray[j4 - 1] = i12 == 0;
				}
				pktType = -1;
				return true;
			}
			if (pktType == 78) {
				destX = 0;
				pktType = -1;
				return true;
			}
			if (pktType == 253) {
				String s = inStream.readLfTerminatedString();
				if (s.endsWith(":tradereq:")) {
					String s3 = s.substring(0, s.indexOf(":"));
					long l17 = TextClass.encodeBase37Name(s3);
					boolean flag2 = false;
					for (int j27 = 0; j27 < ignoreCount; j27++) {
						if (ignoreListAsLongs[j27] != l17) {
							continue;
						}
						flag2 = true;
						break;
					}

					if (!flag2 && chatSuppressionZone == 0) {
						pushMessage("wishes to trade with you.", 4, s3);
					}
				} else if (s.endsWith(":duelreq:")) {
					String s4 = s.substring(0, s.indexOf(":"));
					long l18 = TextClass.encodeBase37Name(s4);
					boolean flag3 = false;
					for (int k27 = 0; k27 < ignoreCount; k27++) {
						if (ignoreListAsLongs[k27] != l18) {
							continue;
						}
						flag3 = true;
						break;
					}

					if (!flag3 && chatSuppressionZone == 0) {
						pushMessage("wishes to duel with you.", 8, s4);
					}
				} else if (s.endsWith(":chalreq:")) {
					String s5 = s.substring(0, s.indexOf(":"));
					long l19 = TextClass.encodeBase37Name(s5);
					boolean flag4 = false;
					for (int l27 = 0; l27 < ignoreCount; l27++) {
						if (ignoreListAsLongs[l27] != l19) {
							continue;
						}
						flag4 = true;
						break;
					}

					if (!flag4 && chatSuppressionZone == 0) {
						String s8 = s.substring(s.indexOf(":") + 1, s.length() - 9);
						pushMessage(s8, 8, s5);
					}
				} else if (s.endsWith(":airtiara:")) {
					tiara = 1;
				} else if (s.endsWith(":watertiara:")) {
					tiara = 2;
				} else if (s.endsWith(":earthtiara:")) {
					tiara = 3;
				} else if (s.endsWith(":firetiara:")) {
					tiara = 4;
				} else if (s.endsWith(":mindtiara:")) {
					tiara = 5;
				} else if (s.endsWith(":bodytiara:")) {
					tiara = 6;
				} else if (s.endsWith(":cosmictiara:")) {
					tiara = 7;
				} else if (s.endsWith(":chaostiara:")) {
					tiara = 8;
				} else if (s.endsWith(":naturetiara:")) {
					tiara = 9;
				} else if (s.endsWith(":lawtiara:")) {
					tiara = 10;
				} else if (s.endsWith(":deathtiara:")) {
					tiara = 11;
				} else if (s.endsWith(":notiara:")) {
					tiara = 0;
				} else {
					pushMessage(s, 0, "");
				}
				pktType = -1;
				// serverMessage(s);

				return true;
			}
			if (pktType == 1) {
				for (int k4 = 0; k4 < playerArray.length; k4++) {
					if (playerArray[k4] != null) {
						playerArray[k4].anim = -1;
					}
				}

				for (int j12 = 0; j12 < npcArray.length; j12++) {
					if (npcArray[j12] != null) {
						npcArray[j12].anim = -1;
					}
				}

				pktType = -1;
				return true;
			}
			if (pktType == 50) {
				long l4 = inStream.readLongBigEndian();
				int i18 = inStream.readUnsignedByte();
				String s7 = TextClass.formatDisplayName(TextClass.decodeBase37Name(l4));
				for (int k24 = 0; k24 < friendsCount; k24++) {
					if (l4 != friendsListAsLongs[k24]) {
						continue;
					}
					if (friendsNodeIDs[k24] != i18) {
						friendsNodeIDs[k24] = i18;
						needDrawTabArea = true;
						if (i18 >= 2) {
							pushMessage(s7 + " has logged in.", 5, "");
						}
						if (i18 <= 1) {
							pushMessage(s7 + " has logged out.", 5, "");
						}
					}
					s7 = null;
					break;
				}

				if (s7 != null && friendsCount < 200) {
					friendsListAsLongs[friendsCount] = l4;
					friendsList[friendsCount] = s7;
					friendsNodeIDs[friendsCount] = i18;
					friendsCount++;
					needDrawTabArea = true;
				}
				for (boolean flag6 = false; !flag6;) {
					flag6 = true;
					for (int k29 = 0; k29 < friendsCount - 1; k29++) {
						if (friendsNodeIDs[k29] != nodeID && friendsNodeIDs[k29 + 1] == nodeID || friendsNodeIDs[k29] == 0 && friendsNodeIDs[k29 + 1] != 0) {
							int j31 = friendsNodeIDs[k29];
							friendsNodeIDs[k29] = friendsNodeIDs[k29 + 1];
							friendsNodeIDs[k29 + 1] = j31;
							String s10 = friendsList[k29];
							friendsList[k29] = friendsList[k29 + 1];
							friendsList[k29 + 1] = s10;
							long l32 = friendsListAsLongs[k29];
							friendsListAsLongs[k29] = friendsListAsLongs[k29 + 1];
							friendsListAsLongs[k29 + 1] = l32;
							needDrawTabArea = true;
							flag6 = false;
						}
					}

				}

				pktType = -1;
				return true;
			}
			if (pktType == 110) {
				if (tabID == 12) {
					needDrawTabArea = true;
				}
				energy = inStream.readUnsignedByte();
				pktType = -1;
				return true;
			}
			if (pktType == 254) {
				anInt855 = inStream.readUnsignedByte();
				if (anInt855 == 1) {
					anInt1222 = inStream.readUnsignedShortBigEndian();
				}
				if (anInt855 >= 2 && anInt855 <= 6) {
					if (anInt855 == 2) {
						anInt937 = 64;
						anInt938 = 64;
					}
					if (anInt855 == 3) {
						anInt937 = 0;
						anInt938 = 64;
					}
					if (anInt855 == 4) {
						anInt937 = 128;
						anInt938 = 64;
					}
					if (anInt855 == 5) {
						anInt937 = 64;
						anInt938 = 0;
					}
					if (anInt855 == 6) {
						anInt937 = 64;
						anInt938 = 128;
					}
					anInt855 = 2;
					anInt934 = inStream.readUnsignedShortBigEndian();
					anInt935 = inStream.readUnsignedShortBigEndian();
					anInt936 = inStream.readUnsignedByte();
				}
				if (anInt855 == 10) {
					anInt933 = inStream.readUnsignedShortBigEndian();
				}
				pktType = -1;
				return true;
			}
			if (pktType == 248) {
				int i5 = inStream.readUnsignedShortBigEndianSubtract128();
				int k12 = inStream.readUnsignedShortBigEndian();
				if (backDialogID != -1) {
					backDialogID = -1;
					inputTaken = true;
				}
				if (fullScreenInterfaceId != -1) {
					fullScreenInterfaceId = -1;
				}
				if (openInterfaceID != i5) {
					openInterfaceID = i5;
				}
				if (invOverlayInterfaceID != k12) {
					invOverlayInterfaceID = k12;
				}
				if (inputDialogState != 0) {
					inputDialogState = 0;
					inputTaken = true;
				}
				needDrawTabArea = true;
				tabAreaAltered = true;
				aBoolean1149 = false;
				pktType = -1;
				if (ClientSettings.SCREENSHOTS_ENABLED && ClientSettings.AUTOMATIC_SCREENSHOTS_ENABLED && i5 == 5292) {
					java.util.Timer timer = new java.util.Timer();
					java.util.TimerTask delayedScreenshot = new java.util.TimerTask() {
						@Override
						public void run() {
							screenshot(false, "bank");
						}
					};
					timer.schedule(delayedScreenshot, 600);
				}
				return true;
			}
			if (pktType == 79) {
				int j5 = inStream.readUnsignedShortLittleEndian();
				int l12 = inStream.readUnsignedShortBigEndianSubtract128();
				RSInterface class9_3 = RSInterface.interfaceCache[j5];
				if (class9_3 != null && class9_3.type == 0) {
					if (l12 < 0) {
						l12 = 0;
					}
					if (l12 > class9_3.scrollMax - class9_3.height) {
						l12 = class9_3.scrollMax - class9_3.height;
					}
					class9_3.scrollPosition = l12;
				}
				pktType = -1;
				return true;
			}
			if (pktType == 68) {
				for (int k5 = 0; k5 < variousSettings.length; k5++) {
					if (variousSettings[k5] != anIntArray1045[k5]) {
						variousSettings[k5] = anIntArray1045[k5];
						method33(k5);
						needDrawTabArea = true;
					}
				}

				pktType = -1;
				return true;
			}
			if (pktType == 196) {
				long l5 = inStream.readLongBigEndian();
				@SuppressWarnings("unused")
				int j18 = inStream.readIntBigEndian();
				int l21 = inStream.readUnsignedByte();
				boolean flag5 = false;
				if (l21 <= 1) {
					for (int l29 = 0; l29 < ignoreCount; l29++) {
						if (ignoreListAsLongs[l29] != l5) {
							continue;
						}
						flag5 = true;

					}
				}
				if (!flag5 && chatSuppressionZone == 0) {
					try {
						// Direct message
						String s9 = TextInput.decodeChatText(pktSize - 13, inStream);
						if (l21 == 2 || l21 == 3) {
							pushMessage(s9, 7, "@cr2@" + TextClass.formatDisplayName(TextClass.decodeBase37Name(l5)));
						} else if (l21 == 1) {
							pushMessage(s9, 7, "@cr1@" + TextClass.formatDisplayName(TextClass.decodeBase37Name(l5)));
						} else {
							pushMessage(s9, 3, TextClass.formatDisplayName(TextClass.decodeBase37Name(l5)));
						}
					} catch (Exception exception1) {
						Signlink.reporterror("cde1");
					}
				}
				pktType = -1;
				return true;
			}
			if (pktType == 85) {
				anInt1269 = inStream.readUnsignedNegatedByte();
				anInt1268 = inStream.readUnsignedNegatedByte();
				pktType = -1;
				return true;
			}
			if (pktType == 24) {
				anInt1054 = inStream.readUnsignedByte128MinusValue();
				if (anInt1054 == tabID) {
					if (anInt1054 == 3) {
						tabID = 1;
					} else {
						tabID = 3;
					}
					needDrawTabArea = true;
				}
				pktType = -1;
				return true;
			}
			if (pktType == 246) {
				int i6 = inStream.readUnsignedShortLittleEndian();
				int i13 = inStream.readUnsignedShortBigEndian();
				int k18 = inStream.readUnsignedShortBigEndian();
				if (k18 == 0x00ffff) {
					RSInterface.interfaceCache[i6].mediaType = 0;
					pktType = -1;
					return true;
				} else {
					ItemDef itemDef = ItemDef.forID(k18);
					RSInterface.interfaceCache[i6].mediaType = 4;
					RSInterface.interfaceCache[i6].mediaID = k18;
					RSInterface.interfaceCache[i6].modelPitch = itemDef.modelRotation1;
					RSInterface.interfaceCache[i6].modelYaw = itemDef.modelRotation2;
					RSInterface.interfaceCache[i6].modelZoom = itemDef.modelZoom * 100 / i13;
					pktType = -1;
					return true;
				}
			}
			if (pktType == 171) {
				boolean flag1 = inStream.readUnsignedByte() == 1;
				int j13 = inStream.readUnsignedShortBigEndian();
				RSInterface.interfaceCache[j13].hidden = flag1;
				pktType = -1;
				return true;
			}
			if (pktType == 142) {
				int j6 = inStream.readUnsignedShortLittleEndian();
				resetInterfaceModelAnimations(j6);
				if (backDialogID != -1) {
					backDialogID = -1;
					inputTaken = true;
				}
				if (fullScreenInterfaceId != -1) {
					fullScreenInterfaceId = -1;
				}
				if (openInterfaceID != -1) {
					openInterfaceID = -1;
				}
				if (invOverlayInterfaceID != j6) {
					invOverlayInterfaceID = j6;
				}
				if (inputDialogState != 0) {
					inputDialogState = 0;
					inputTaken = true;
				}
				needDrawTabArea = true;
				tabAreaAltered = true;
				aBoolean1149 = false;
				pktType = -1;
				return true;
			}
			if (pktType == 126) {
				String message = inStream.readLfTerminatedString();
				int interfaceID = inStream.readUnsignedShortBigEndianSubtract128();
				// Update current player health (fix for refresh skill not including this)
				if (interfaceID == 4016) {
					myPlayer.currentHealth = Integer.parseInt(message);
					currentStats[3] = Integer.parseInt(message);
				}
				RSInterface.interfaceCache[interfaceID].disabledText = message;
				if (RSInterface.interfaceCache[interfaceID].parentID == tabInterfaceIDs[tabID]) {
					needDrawTabArea = true;
				}
				pktType = -1;
				return true;
			}
			if (pktType == 206) {
				publicChatMode = inStream.readUnsignedByte();
				privateChatMode = inStream.readUnsignedByte();
				tradeMode = inStream.readUnsignedByte();
				aBoolean1233 = true;
				inputTaken = true;
				pktType = -1;
				return true;
			}
			if (pktType == 240) {
				if (tabID == 12) {
					needDrawTabArea = true;
				}
				weight = inStream.readSignedShortBigEndian();
				pktType = -1;
				return true;
			}
			if (pktType == 69) {
				int k9 = inStream.readUnsignedShortBigEndian();
				int k15 = inStream.readUnsignedShortBigEndian();
				resetInterfaceModelAnimations(k15);
				if (k9 != -1)
					resetInterfaceModelAnimations(k9);
				if (openInterfaceID != -1) {
					openInterfaceID = -1;
				}
				if (invOverlayInterfaceID != -1) {
					invOverlayInterfaceID = -1;
				}
				if (backDialogID != -1) {
					backDialogID = -1;
				}
				if (fullScreenInterfaceId != k15) {
					fullScreenInterfaceId = k9;
				}
				inputDialogState = 0;
				aBoolean1149 = false;
				pktType = -1;
				return true;
			}
			if (pktType == 8) {
				int k6 = inStream.readUnsignedShortLittleEndianSubtract128();
				int l13 = inStream.readUnsignedShortBigEndian();
				RSInterface.interfaceCache[k6].mediaType = 1;
				RSInterface.interfaceCache[k6].mediaID = l13;
				pktType = -1;
				return true;
			}
			if (pktType == 122) {
				int l6 = inStream.readUnsignedShortLittleEndianSubtract128();
				int i14 = inStream.readUnsignedShortLittleEndianSubtract128();
				int i19 = i14 >> 10 & 0x1f;
				int i22 = i14 >> 5 & 0x1f;
				int l24 = i14 & 0x1f;
				RSInterface.interfaceCache[l6].textColor = (i19 << 19) + (i22 << 11) + (l24 << 3);
				pktType = -1;
				return true;
			}
			if (pktType == 53) {
				needDrawTabArea = true;
				int i7 = inStream.readUnsignedShortBigEndian();
				RSInterface class9_1 = RSInterface.interfaceCache[i7];
				int j19 = inStream.readUnsignedShortBigEndian();
				for (int j22 = 0; j22 < j19; j22++) {
					int i25 = inStream.readUnsignedByte();
					if (i25 == 255) {
						i25 = inStream.readIntOrder2143();
					}
					class9_1.itemIdsPlusOne[j22] = inStream.readUnsignedShortLittleEndianSubtract128();
					class9_1.itemQuantities[j22] = i25;
				}

				for (int j25 = j19; j25 < class9_1.itemIdsPlusOne.length; j25++) {
					class9_1.itemIdsPlusOne[j25] = 0;
					class9_1.itemQuantities[j25] = 0;
				}

				pktType = -1;
				return true;
			}
			if (pktType == 230) {
				int j7 = inStream.readUnsignedShortBigEndianSubtract128();
				int j14 = inStream.readUnsignedShortBigEndian();
				int k19 = inStream.readUnsignedShortBigEndian();
				int k22 = inStream.readUnsignedShortLittleEndianSubtract128();
				RSInterface.interfaceCache[j14].modelPitch = k19;
				RSInterface.interfaceCache[j14].modelYaw = k22;
				RSInterface.interfaceCache[j14].modelZoom = j7;
				pktType = -1;
				return true;
			}
			if (pktType == 221) {
				anInt900 = inStream.readUnsignedByte();
				needDrawTabArea = true;
				pktType = -1;
				return true;
			}
			if (pktType == 177) {
				aBoolean1160 = true;
				anInt995 = inStream.readUnsignedByte();
				anInt996 = inStream.readUnsignedByte();
				anInt997 = inStream.readUnsignedShortBigEndian();
				anInt998 = inStream.readUnsignedByte();
				anInt999 = inStream.readUnsignedByte();
				if (anInt999 >= 100) {
					int k7 = anInt995 * 128 + 64;
					int k14 = anInt996 * 128 + 64;
					int i20 = interpolatedGroundHeight(plane, k14, k7) - anInt997;
					int l22 = k7 - xCameraPos;
					int k25 = i20 - zCameraPos;
					int j28 = k14 - yCameraPos;
					int i30 = (int) Math.sqrt(l22 * l22 + j28 * j28);
					yCameraCurve = (int) (Math.atan2(k25, i30) * 325.94900000000001D) & 0x7ff;
					xCameraCurve = (int) (Math.atan2(l22, j28) * -325.94900000000001D) & 0x7ff;
					if (yCameraCurve < 128) {
						yCameraCurve = 128;
					}
					if (yCameraCurve > 383) {
						yCameraCurve = 383;
					}
				}
				pktType = -1;
				return true;
			}
			if (pktType == 249) {
				anInt1046 = inStream.readUnsignedByteSubtract128();
				unknownInt10 = inStream.readUnsignedShortLittleEndianSubtract128();
				pktType = -1;
				return true;
			}
			if (pktType == 65) {
				updateNPCs(inStream, pktSize);
				pktType = -1;
				return true;
			}
			if (pktType == 27) {
				messagePromptRaised = false;
				inputDialogState = 1;
				amountOrNameInput = "";
				inputTaken = true;
				if (ClientSettings.ANDROID) {
					AndroidHooks.showKeyboard();
				}
				pktType = -1;
				return true;
			}
			if (pktType == 187) {
				messagePromptRaised = false;
				inputDialogState = 2;
				amountOrNameInput = "";
				inputTaken = true;
				if (ClientSettings.ANDROID) {
					AndroidHooks.showKeyboard();
				}
				pktType = -1;
				return true;
			}
			if (pktType == 97) {
				int l7 = inStream.readUnsignedShortBigEndian();
				resetInterfaceModelAnimations(l7);
				if (invOverlayInterfaceID != -1) {
					invOverlayInterfaceID = -1;
					needDrawTabArea = true;
					tabAreaAltered = true;
				}
				if (backDialogID != -1) {
					backDialogID = -1;
					inputTaken = true;
				}
				if (inputDialogState != 0) {
					inputDialogState = 0;
					inputTaken = true;
				}
				// 17511 = Question Type
				// 15819 = Christmas Type
				// 15812 = Security Type
				// 15801 = Item Scam Type
				// 15791 = Password Safety 
				// 15774 = Good/Bad Password
				// 15767 = Drama Type 
				if (l7 == 15244) {
					if (ClientSettings.SNOW_OVERLAY_FORCE_ENABLED || (ClientSettings.SNOW_OVERLAY_ENABLED && Flo.getTodaysDate().contains(ClientSettings.SNOW_MONTH))) {
						openInterfaceID = 15819;
					} else {
						openInterfaceID = 15801;
					}
					fullScreenInterfaceId = 15244;
				} else {
					openInterfaceID = l7;
				}
				aBoolean1149 = false;
				pktType = -1;
				return true;
			}
			if (pktType == 218) {
				int i8 = inStream.readSignedShortLittleEndianSubtract128();
				dialogID = i8;
				inputTaken = true;
				pktType = -1;
				return true;
			}
			if (pktType == 87) {
				int j8 = inStream.readUnsignedShortLittleEndian();
				int l14 = inStream.readIntOrder3412();
				anIntArray1045[j8] = l14;
				if (variousSettings[j8] != l14) {
					variousSettings[j8] = l14;
					method33(j8);
					needDrawTabArea = true;
					if (dialogID != -1) {
						inputTaken = true;
					}
				}
				pktType = -1;
				return true;
			}
			if (pktType == 36) {
				int k8 = inStream.readUnsignedShortLittleEndian();
				byte byte0 = inStream.readSignedByte();
				anIntArray1045[k8] = byte0;
				if (variousSettings[k8] != byte0) {
					variousSettings[k8] = byte0;
					method33(k8);
					needDrawTabArea = true;
					if (dialogID != -1) {
						inputTaken = true;
					}
				}
				pktType = -1;
				return true;
			}
			if (pktType == 61) {
				anInt1055 = inStream.readUnsignedByte();
				pktType = -1;
				return true;
			}
			if (pktType == 200) {
				int l8 = inStream.readUnsignedShortBigEndian();
				int i15 = inStream.readSignedShortBigEndian();
				RSInterface class9_4 = RSInterface.interfaceCache[l8];
				class9_4.disabledAnimationId = i15;
				if (i15 == -1) {
					class9_4.modelAnimationFrame = 0;
					class9_4.modelAnimationTicks = 0;
				}
				pktType = -1;
				return true;
			}
			if (pktType == 219) {
				if (invOverlayInterfaceID != -1) {
					invOverlayInterfaceID = -1;
					needDrawTabArea = true;
					tabAreaAltered = true;
				}
				if (backDialogID != -1) {
					backDialogID = -1;
					inputTaken = true;
				}
				if (fullScreenInterfaceId != -1) {
					fullScreenInterfaceId = -1;
				}
				if (openInterfaceID != -1) {
					openInterfaceID = -1;
				}
				if (inputDialogState != 0) {
					inputDialogState = 0;
					inputTaken = true;
				}
				aBoolean1149 = false;
				pktType = -1;
				return true;
			}
			if (pktType == 34) {
				needDrawTabArea = true;
				int i9 = inStream.readUnsignedShortBigEndian();
				RSInterface class9_2 = RSInterface.interfaceCache[i9];
				while (inStream.currentOffset < pktSize) {
					int j20 = inStream.readUnsignedSmart();
					int i23 = inStream.readUnsignedShortBigEndian();
					int l25 = inStream.readUnsignedByte();
					if (l25 == 255) {
						l25 = inStream.readIntBigEndian();
					}
					if (j20 >= 0 && j20 < class9_2.itemIdsPlusOne.length) {
						class9_2.itemIdsPlusOne[j20] = i23;
						class9_2.itemQuantities[j20] = l25;
					}
				}
				pktType = -1;
				return true;
			}
			if (pktType == 105 || pktType == 84 || pktType == 147 || pktType == 215 || pktType == 4 || pktType == 117 || pktType == 156 || pktType == 44 || pktType == 160 || pktType == 101 || pktType == 151) {
				method137(inStream, pktType);
				pktType = -1;
				return true;
			}
			if (pktType == 106) {
				tabID = inStream.readUnsignedNegatedByte();
				needDrawTabArea = true;
				tabAreaAltered = true;
				pktType = -1;
				return true;
			}
			if (pktType == 164) {
				int j9 = inStream.readUnsignedShortLittleEndian();
				resetInterfaceModelAnimations(j9);
				if (invOverlayInterfaceID != -1) {
					invOverlayInterfaceID = -1;
					needDrawTabArea = true;
					tabAreaAltered = true;
				}
				if (fullScreenInterfaceId != -1) {
					fullScreenInterfaceId = -1;
				}
				if (openInterfaceID != -1) {
					openInterfaceID = -1;
				}
				if (backDialogID != j9) {
					backDialogID = j9;
				}
				inputTaken = true;
				aBoolean1149 = false;
				pktType = -1;
				return true;
			}
			String s2 = "T1 - " + pktType + "," + pktSize + " - " + prevPktType + "," + prevPktType2 + " - ";
			for (int j15 = 0; j15 < pktSize && j15 < 50; j15++) {
				s2 = s2 + inStream.buffer[j15] + ",";
			}
			Signlink.reporterror(s2);
			resetLogout();
		} catch (IOException _ex) {
			dropClient();
		} catch (Exception exception) {
			String s2 = "T2 - " + pktType + "," + prevPktType + "," + prevPktType2 + " - " + pktSize + "," + (baseX + myPlayer.smallX[0]) + "," + (baseY + myPlayer.smallY[0]) + " - ";
			for (int j15 = 0; j15 < pktSize && j15 < 50; j15++) {
				s2 = s2 + inStream.buffer[j15] + ",";
			}

			Signlink.reporterror(s2);
			resetLogout();
			exception.printStackTrace();
		}
		return true;
	}

	public static int zoom = 3;

	public void drawWorldView() {
		anInt1265++;
		addPlayersToScene(true);
		addNpcsToScene(true);
		addPlayersToScene(false);
		addNpcsToScene(false);
		updateProjectiles();
		updateTransientGraphics();
		if (!aBoolean1160) {
			int cameraPitch = anInt1184;
			if (anInt984 / 256 > cameraPitch) {
				cameraPitch = anInt984 / 256;
			}
			if (aBooleanArray876[4] && anIntArray1203[4] + 128 > cameraPitch) {
				cameraPitch = anIntArray1203[4] + 128;
			}
			int cameraYaw = minimapInt1 + anInt896 & 0x7ff;
			// Camera zoom control
			setCameraPos(600 + cameraPitch * zoom, cameraPitch, anInt1014, interpolatedGroundHeight(plane, myPlayer.y, myPlayer.x) - 70, cameraYaw, anInt1015);
		}
		int scenePlane;
		if (!aBoolean1160) {
			scenePlane = selectRoofVisibilityPlane();
		} else {
			scenePlane = selectFixedCameraPlane();
		}
		int savedXCameraPos = xCameraPos;
		int savedZCameraPos = zCameraPos;
		int savedYCameraPos = yCameraPos;
		int savedYCameraCurve = yCameraCurve;
		int savedXCameraCurve = xCameraCurve;
		for (int shakeChannel = 0; shakeChannel < 5; shakeChannel++) {
			if (aBooleanArray876[shakeChannel]) {
				int shakeOffset = (int) (Math.random() * (double) (anIntArray873[shakeChannel] * 2 + 1) - (double) anIntArray873[shakeChannel] + Math.sin((double) anIntArray1030[shakeChannel] * (double) anIntArray928[shakeChannel] / 100D) * (double) anIntArray1203[shakeChannel]);
				if (shakeChannel == 0) {
					xCameraPos += shakeOffset;
				}
				if (shakeChannel == 1) {
					zCameraPos += shakeOffset;
				}
				if (shakeChannel == 2) {
					yCameraPos += shakeOffset;
				}
				if (shakeChannel == 3) {
					xCameraCurve = xCameraCurve + shakeOffset & 0x7ff;
				}
				if (shakeChannel == 4) {
					yCameraCurve += shakeOffset;
					if (yCameraCurve < 128) {
						yCameraCurve = 128;
					}
					if (yCameraCurve > 383) {
						yCameraCurve = 383;
					}
				}
			}
		}

		int textureUseCounter = Texture.useCounter;
		Model.pickingEnabled = true;
		Model.pickedCount = 0;
		Model.pickMouseX = super.mouseX - 4;
		Model.pickMouseY = super.mouseY - 4;
		DrawingArea.clearRaster();
		if(graphicsEnabled){
			worldController.drawScene(xCameraPos, yCameraPos, xCameraCurve, zCameraPos, scenePlane, yCameraCurve);
			worldController.clearObj5Cache();
			updateEntities();
			drawHeadIcon();
		}
		// Allow tabs to work
		scrollAnimatedTextures(textureUseCounter);
		// Allow stuff inside the tabs to work
		draw3dScreen();
		if (showInfo) {
			int debugX = 0;
			int debugY = 234;
			int debugItems = 5;
			int debugWidth = 140;
			int debugHeight = 25 + (debugItems * 15);
			int fill = 0x5d5447;
			int fill2 = RSColors.BLACK;
			int opacity = 140;
	
			DrawingArea.fillBlendedRectangle(fill, debugY, debugWidth, debugHeight, opacity, debugX);
			DrawingArea.fillBlendedRectangle(fill2, debugY + 1, debugWidth - 2, 16, opacity, debugX + 1);
			DrawingArea.drawRectangleOutline(debugY + 18, debugHeight - 19, fill2, debugX + 1, debugWidth - 2);
			chatTextDrawingArea.drawLiteralText(RSColors.WHITE_DARKER, "Debug Info", debugY += 14, debugX + 3);
			chatTextDrawingArea.drawLiteralText(super.fps > 40 ? RSColors.YELLOW : super.fps > 25 ? RSColors.ORANGE : RSColors.RED, super.fps + "fps", debugY, debugX + debugWidth - chatTextDrawingArea.measureTaggedTextWidth(super.fps + "fps") - 3);

			// Bump Y value
			debugY += 3;
			
			// Draw items
			Runtime runtime = Runtime.getRuntime();
			int memKB = (int) ((runtime.totalMemory() - runtime.freeMemory()) / 1024L);
			chatTextDrawingArea.drawTaggedText(true, debugX + 4, RSColors.WHITE, "Memory:", debugY += 15);
			chatTextDrawingArea.drawRightAlignedTaggedText(true, debugX + debugWidth - 4, RSColors.YELLOW, (memKB / 1024) + "mb", debugY);
			chatTextDrawingArea.drawTaggedText(true, debugX + 4, RSColors.WHITE, "Mouse:", debugY += 15);
			chatTextDrawingArea.drawRightAlignedTaggedText(true, debugX + debugWidth - 4, RSColors.YELLOW, super.mouseX + ", " + super.mouseY, debugY);
			chatTextDrawingArea.drawTaggedText(true, debugX + 4, RSColors.WHITE, "Coords:", debugY += 15);
			chatTextDrawingArea.drawRightAlignedTaggedText(true, debugX + debugWidth - 4, RSColors.YELLOW, (myPlayer.smallX[0] + baseX) + ", " + (myPlayer.smallY[0] + baseY), debugY);
			chatTextDrawingArea.drawTaggedText(true, debugX + 4, RSColors.WHITE, "Interface:", debugY += 15);
			chatTextDrawingArea.drawRightAlignedTaggedText(true, debugX + debugWidth - 4, RSColors.YELLOW, "" + openInterfaceID, debugY);
			chatTextDrawingArea.drawTaggedText(true, debugX + 4, RSColors.WHITE, "Zoom level:", debugY += 15);
			chatTextDrawingArea.drawRightAlignedTaggedText(true, debugX + debugWidth - 4, RSColors.YELLOW, "" + zoom, debugY);
		}
		
		if (customSettingShowExperiencePerHour) {
			int debugItems = 2;
			int debugWidth = 140;
			int debugHeight = 25 + (debugItems * 15);
			int debugX = 511 - debugWidth;
			int debugY = 0;
			int fill = 0x5d5447;
			int fill2 = RSColors.BLACK;
			int opacity = 140;
	
			DrawingArea.fillBlendedRectangle(fill, debugY, debugWidth, debugHeight, opacity, debugX);
			DrawingArea.fillBlendedRectangle(fill2, debugY + 1, debugWidth - 2, 16, opacity, debugX + 1);
			DrawingArea.drawRectangleOutline(debugY + 18, debugHeight - 19, fill2, debugX + 1, debugWidth - 2);
			chatTextDrawingArea.drawLiteralText(RSColors.WHITE_DARKER, "Experience Info", debugY += 14, debugX + 3);

			// Bump Y value
			debugY += 3;
			
			// Calculate exp/h
			long currentExpGained = calculateTotalExp();
			long expGained = currentExpGained - customSettingShowExperiencePerHourStartExp;
			long expPerHour = (long) ((expGained * 3600000D) / (System.currentTimeMillis() - customSettingShowExperiencePerHourStart));

			// Draw items
			chatTextDrawingArea.drawTaggedText(true, debugX + 4, RSColors.WHITE, "Exp per hour:", debugY += 15);
			chatTextDrawingArea.drawRightAlignedTaggedText(true, debugX + debugWidth - 4, RSColors.YELLOW, intToShortLetter((int) expPerHour), debugY);
			chatTextDrawingArea.drawTaggedText(true, debugX + 4, RSColors.WHITE, "Levels gained:", debugY += 15);
			chatTextDrawingArea.drawRightAlignedTaggedText(true, debugX + debugWidth - 4, RSColors.YELLOW, "" + (calculateTotalLevels() - customSettingShowExperiencePerHourStartLevels), debugY);
		}

		// Draw main screen
		aRSImageProducer_1165.drawGraphics(4, super.graphics, 4);

		if(graphicsEnabled) {
			xCameraPos = savedXCameraPos;
			zCameraPos = savedZCameraPos;
			yCameraPos = savedYCameraPos;
			yCameraCurve = savedYCameraCurve;
			xCameraCurve = savedXCameraCurve;
		}
	}

	public void closeOpenInterfaces() {
		stream.writeEncryptedOpcode(130);
		if (invOverlayInterfaceID != -1) {
			invOverlayInterfaceID = -1;
			needDrawTabArea = true;
			aBoolean1149 = false;
			tabAreaAltered = true;
		}
		if (backDialogID != -1) {
			backDialogID = -1;
			inputTaken = true;
			aBoolean1149 = false;
		}
		if (fullScreenInterfaceId != -1) {
			fullScreenInterfaceId = -1;
		}
		if (openInterfaceID != -1)
			openInterfaceID = -1;
	}

	public Game() {
		if (!ClientSettings.ANDROID) {
			DesktopHooks.checkJavaVersion();
		}
		server = ClientSettings.SERVER_IP;
		pathDistance = new int[104][104];
		friendsNodeIDs = new int[200];
		groundArray = new NodeList[4][104][104];
		aBoolean831 = false;
		aStream_834 = new Stream(new byte[5000]);
		npcArray = new NPC[16384];
		npcIndices = new int[16384];
		anIntArray840 = new int[1000];
		aStream_847 = Stream.acquire();
		aBoolean848 = true;
		openInterfaceID = -1;
		currentExp = new int[Skills.skillsCount];
		aBoolean872 = false;
		anIntArray873 = new int[5];
		anInt874 = -1;
		aBooleanArray876 = new boolean[5];
		drawFlames = false;
		reportAbuseInput = "";
		unknownInt10 = -1;
		menuOpen = false;
		inputString = "";
		maxPlayers = 2048;
		myPlayerIndex = 2047;
		playerArray = new Player[maxPlayers];
		playerIndices = new int[maxPlayers];
		maskUpdateIndices = new int[maxPlayers];
		aStreamArray895s = new Stream[maxPlayers];
		anInt897 = 1;
		pathBackDirection = new int[104][104];
		scrollHighlightColor = 0x766654;
		textureScrollScratch = new byte[16384];
		currentStats = new int[Skills.skillsCount];
		ignoreListAsLongs = new long[100];
		loadingError = false;
		scrollShadowColor = 0x332d25;
		anIntArray928 = new int[5];
		anIntArrayArray929 = new int[104][104];
		chatTypes = new int[100];
		chatNames = new String[100];
		chatMessages = new String[100];
		sideIcons = new Background[13];
		aBoolean954 = true;
		friendsListAsLongs = new long[200];
		currentSong = -1;
		drawingFlames = false;
		spriteDrawX = -1;
		spriteDrawY = -1;
		anIntArray968 = new int[33];
		anIntArray969 = new int[256];
		cacheStores = new CacheStore[5];
		variousSettings = new int[2000];
		scrollbarDragging = false;
		anInt975 = 50;
		anIntArray976 = new int[anInt975];
		anIntArray977 = new int[anInt975];
		anIntArray978 = new int[anInt975];
		anIntArray979 = new int[anInt975];
		anIntArray980 = new int[anInt975];
		anIntArray981 = new int[anInt975];
		anIntArray982 = new int[anInt975];
		aStringArray983 = new String[anInt975];
		minimapBuiltPlane = -1;
		hitMarks = new Sprite[20];
		anIntArray990 = new int[5];
		scrollTrackColor = 0x23201b;
		amountOrNameInput = "";
		projectiles = new NodeList();
		aBoolean1017 = false;
		anInt1018 = -1;
		anIntArray1030 = new int[5];
		aBoolean1031 = false;
		mapFunctions = new Sprite[100];
		dialogID = -1;
		maxStats = new int[Skills.skillsCount];
		anIntArray1045 = new int[2000];
		designerMale = true;
		anIntArray1052 = new int[151];
		anInt1054 = -1;
		transientGraphics = new NodeList();
		anIntArray1057 = new int[33];
		aClass9_1059 = new RSInterface();
		mapScenes = new Background[100];
		scrollThumbColor = 0x4d4233;
		designerKitIds = new int[7];
		mapFunctionIconTileX = new int[1000];
		mapFunctionIconTileY = new int[1000];
		aBoolean1080 = false;
		friendsList = new String[200];
		inStream = Stream.acquire();
		expectedCRCs = new int[9];
		menuActionCmd2 = new int[500];
		menuActionCmd3 = new int[500];
		menuActionID = new int[500];
		menuActionCmd1 = new int[500];
		headIcons = new Sprite[20];
		headIconsHint = new Sprite[20];
		skullIcons = new Sprite[20];
		tabAreaAltered = false;
		aString1121 = "";
		atPlayerActions = new String[5];
		atPlayerArray = new boolean[5];
		anIntArrayArrayArray1129 = new int[4][13][13];
		anInt1132 = 2;
		mapFunctionIconSprites = new Sprite[1000];
		aBoolean1141 = false;
		aBoolean1149 = false;
		crosses = new Sprite[8];
		musicEnabled = true;
		needDrawTabArea = false;
		loggedIn = false;
		canMute = false;
		aBoolean1159 = false;
		aBoolean1160 = false;
		anInt1171 = 1;
		myUsername = "";
		myPassword = "";
		genericLoadingError = false;
		reportAbuseInterfaceID = -1;
		pendingSceneChanges = new NodeList();
		anInt1184 = 128;
		invOverlayInterfaceID = -1;
		stream = Stream.acquire();
		menuActionName = new String[500];
		anIntArray1203 = new int[5];
		sound = new int[50];
		anInt1210 = 2;
		anInt1211 = 78;
		promptInput = "";
		modIcons = new Background[2];
		tabID = 3;
		inputTaken = false;
		songChanging = true;
		anIntArray1229 = new int[151];
		aClass11Array1230 = new CollisionMap[4];
		aBoolean1233 = false;
		soundType = new int[50];
		aBoolean1242 = false;
		soundDelay = new int[50];
		soundVolume = new int[50];
		rsAlreadyLoaded = false;
		welcomeScreenRaised = false;
		messagePromptRaised = false;
		loginMessage1 = "";
		loginMessage2 = "";
		backDialogID = -1;
		anInt1279 = 2;
		bigX = new int[4000];
		bigY = new int[4000];
		anInt1289 = -1;
		aCRC32_930 = new CRC32();
	}
	public CRC32 aCRC32_930;
	public static String server;
	public int ignoreCount;
	public long aLong824;
	public int[][] pathDistance;
	public int[] friendsNodeIDs;
	public NodeList[][][] groundArray;
	public int[] anIntArray828;
	public int[] anIntArray829;
	public volatile boolean aBoolean831;
	public Socket aSocket832;
	public int loginScreenState;
	public Stream aStream_834;
	public NPC[] npcArray;
	public int npcCount;
	public int[] npcIndices;
	public int anInt839;
	public int[] anIntArray840;
	public int anInt841;
	public int prevPktType;
	public int prevPktType2;
	public String aString844;
	public int publicChatMode;
	public int privateChatMode;
	public Stream aStream_847;
	public boolean aBoolean848;
	public static int anInt849;
	public int[] anIntArray850;
	public int[] anIntArray851;
	public int[] anIntArray852;
	public int[] anIntArray853;
	public static int anInt854;
	public int anInt855;
	public int openInterfaceID;
	public int fullScreenInterfaceId = -1;
	public int xCameraPos;
	public int zCameraPos;
	public int yCameraPos;
	public int yCameraCurve;
	public int xCameraCurve;
	public int myPrivilege;
	public final int[] currentExp;
	public static int musicId;
	public static int soundId;
	public Background redStone1_3;
	public Background redStone2_3;
	public Background redStone3_2;
	public Background redStone1_4;
	public Background redStone2_4;
	public Sprite multiOverlay;
	public Sprite mapFlag;
	public Sprite mapMarker;
	public boolean aBoolean872;
	public final int[] anIntArray873;
	public int anInt874;
	public final boolean[] aBooleanArray876;
	public int weight;
	public MouseDetection mouseDetection;
	public volatile boolean drawFlames;
	public String reportAbuseInput;
	public int unknownInt10;
	public boolean menuOpen;
	public int anInt886;
	public String inputString;
	public final int maxPlayers;
	public final int myPlayerIndex;
	public Player[] playerArray;
	public int playerCount;
	public int[] playerIndices;
	public int maskUpdateCount;
	public int[] maskUpdateIndices;
	public Stream[] aStreamArray895s;
	public int anInt896;
	public int anInt897;
	public int friendsCount;
	public int anInt900;
	public int[][] pathBackDirection;
	public final int scrollHighlightColor;
	public RSImageProducer backLeftIP1;
	public RSImageProducer backLeftIP2;
	public RSImageProducer backRightIP1;
	public RSImageProducer backRightIP2;
	public RSImageProducer backTopIP1;
	public RSImageProducer backVmidIP1;
	public RSImageProducer backVmidIP2;
	public RSImageProducer backVmidIP3;
	public RSImageProducer backVmidIP2_2;
	public byte[] textureScrollScratch;
	public int anInt913;
	public int crossX;
	public int crossY;
	public int crossIndex;
	public int crossType;
	public int plane;
	public final int[] currentStats;
	public static int anInt924;
	public final long[] ignoreListAsLongs;
	public boolean loadingError;
	public final int scrollShadowColor;
	public final int[] anIntArray928;
	public int[][] anIntArrayArray929;
	public Sprite aClass30_Sub2_Sub1_Sub1_931;
	public Sprite aClass30_Sub2_Sub1_Sub1_932;
	public int anInt933;
	public int anInt934;
	public int anInt935;
	public int anInt936;
	public int anInt937;
	public int anInt938;
	public static int anInt940;
	public final int[] chatTypes;
	public final String[] chatNames;
	public final String[] chatMessages;
	public int cyclesSinceDraw;
	public WorldController worldController;
	public Background[] sideIcons;
	public int menuScreenArea;
	public int menuOffsetX;
	public int menuOffsetY;
	public int menuWidth;
	public int anInt952;
	public long aLong953;
	public boolean aBoolean954;
	public long[] friendsListAsLongs;
	public int currentSong;
	public static int nodeID = 10;
	static int portOff;
	public static boolean isMembers = true;
	public static boolean lowMem;
	public volatile boolean drawingFlames;
	public int spriteDrawX;
	public int spriteDrawY;
	public final int[] anIntArray965 = {0xffff00, 0xff0000, 0x00ff00, 0x00ffff, 0xff00ff, 0xffffff};
	public Background aBackground_966;
	public Background aBackground_967;
	public final int[] anIntArray968;
	public final int[] anIntArray969;
	final CacheStore[] cacheStores;
	public int variousSettings[];
	public boolean scrollbarDragging;
	public final int anInt975;
	public final int[] anIntArray976;
	public final int[] anIntArray977;
	public final int[] anIntArray978;
	public final int[] anIntArray979;
	public final int[] anIntArray980;
	public final int[] anIntArray981;
	public final int[] anIntArray982;
	public final String[] aStringArray983;
	public int anInt984;
	public int minimapBuiltPlane;
	public static int anInt986;
	public Sprite[] hitMarks;
	public int anInt988;
	public int anInt989;
	public final int[] anIntArray990;
	public static boolean aBoolean993;
	public int anInt995;
	public int anInt996;
	public int anInt997;
	public int anInt998;
	public int anInt999;
	public ISAACRandomGen encryption;
	public Sprite minimapEdgeArrow;
	public final int scrollTrackColor;
	static final int[][] anIntArrayArray1003 = {{6798, 107, 10283, 16, 4797, 7744, 5799, 4634, 33697, 22433, 2983, 54193}, {8741, 12, 64030, 43162, 7735, 8404, 1701, 38430, 24094, 10153, 56621, 4783, 1341, 16578, 35003, 25239}, {25238, 8742, 12, 64030, 43162, 7735, 8404, 1701, 38430, 24094, 10153, 56621, 4783, 1341, 16578, 35003}, {4626, 11146, 6439, 12, 4758, 10270}, {4550, 4537, 5681, 5673, 5790, 6806, 8076, 4574}};
	public String amountOrNameInput;
	public static int anInt1005;
	public int daysSinceLastLogin;
	public int pktSize;
	public int pktType;
	public int anInt1009;
	public int anInt1010;
	public int anInt1011;
	public NodeList projectiles;
	public int anInt1014;
	public int anInt1015;
	public int anInt1016;
	public boolean aBoolean1017;
	public int anInt1018;
	public static final int[] anIntArray1019;
	public int anInt1021;
	public int anInt1022;
	public int loadingStage;
	public Background scrollUpArrow;
	public Background scrollDownArrow;
	public int anInt1026;
	public Background backBase1;
	public Background backBase2;
	public Background backHmid1;
	public final int[] anIntArray1030;
	public boolean aBoolean1031;
	public Sprite[] mapFunctions;
	public int baseX;
	public int baseY;
	public int anInt1036;
	public int anInt1037;
	public int loginFailures;
	public int anInt1039;
	public int anInt1040;
	public int anInt1041;
	public int dialogID;
	public final int[] maxStats;
	public final int[] anIntArray1045;
	public int anInt1046;
	public boolean designerMale;
	public int anInt1048;
	public String aString1049;
	public static int anInt1051;
	public final int[] anIntArray1052;
	public StreamLoader titleStreamLoader;
	public int anInt1054;
	public int anInt1055;
	public NodeList transientGraphics;
	public final int[] anIntArray1057;
	public final RSInterface aClass9_1059;
	public Background[] mapScenes;
	public static int anInt1061;
	public int currentSound;
	public final int scrollThumbColor;
	public int friendsListAction;
	public final int[] designerKitIds;
	public int mouseInvInterfaceIndex;
	public int lastActiveInvInterface;
	public OnDemandFetcher onDemandFetcher;
	public int anInt1069;
	public int anInt1070;
	public int mapFunctionIconCount;
	public int[] mapFunctionIconTileX;
	public int[] mapFunctionIconTileY;
	public Sprite mapDotItem;
	public Sprite mapDotNPC;
	public Sprite mapDotPlayer;
	public Sprite mapDotFriend;
	public Sprite mapDotTeam;
	public int anInt1079;
	public boolean aBoolean1080;
	public String[] friendsList;
	public Stream inStream;
	public int anInt1084;
	public int anInt1085;
	public int activeInterfaceType;
	public int anInt1087;
	public int anInt1088;
	public int anInt1089;
	public final int[] expectedCRCs;
	public int[] menuActionCmd2;
	public int[] menuActionCmd3;
	public int[] menuActionID;
	public int[] menuActionCmd1;
	public Sprite[] headIcons;
	public Sprite[] skullIcons;
	public Sprite[] headIconsHint;
	public static int anInt1097;
	public int anInt1098;
	public int anInt1099;
	public int anInt1100;
	public int anInt1101;
	public int anInt1102;
	public boolean tabAreaAltered;
	public int anInt1104;
	public RSImageProducer aRSImageProducer_1107;
	public RSImageProducer aRSImageProducer_1108;
	public RSImageProducer aRSImageProducer_1109;
	public RSImageProducer aRSImageProducer_1110;
	public RSImageProducer aRSImageProducer_1111;
	public RSImageProducer aRSImageProducer_1112;
	public RSImageProducer aRSImageProducer_1113;
	public RSImageProducer aRSImageProducer_1114;
	public RSImageProducer aRSImageProducer_1115;
	public static int anInt1117;
	public int membersInt;
	public String aString1121;
	public Sprite compass;
	public RSImageProducer aRSImageProducer_1123;
	public RSImageProducer aRSImageProducer_1124;
	public RSImageProducer aRSImageProducer_1125;
	public static Player myPlayer;
	public final String[] atPlayerActions;
	public final boolean[] atPlayerArray;
	public final int[][][] anIntArrayArrayArray1129;
	public final int[] tabInterfaceIDs = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
	public int anInt1131;
	public int anInt1132;
	public int menuActionRow;
	public static int anInt1134;
	public int spellSelected;
	public int anInt1137;
	public int spellUsableOn;
	public String spellTooltip;
	public Sprite[] mapFunctionIconSprites;
	public boolean aBoolean1141;
	public static int anInt1142;
	public Background redStone1;
	public Background redStone2;
	public Background redStone3;
	public Background redStone1_2;
	public Background redStone2_2;
	public int energy;
	public boolean aBoolean1149;
	public Sprite[] crosses;
	public boolean musicEnabled;
	public Background[] aBackgroundArray1152s;
	public boolean needDrawTabArea;
	public int unreadMessages;
	public static int anInt1155;
	public boolean loggedIn;
	public boolean canMute;
	public boolean aBoolean1159;
	public boolean aBoolean1160;
	static int loopCycle;
	public static final String validUserPassChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!\"\243$%^&*()-_=+[{]};:'@#~,<.>/?\\| ";
	public RSImageProducer aRSImageProducer_1163;
	public RSImageProducer aRSImageProducer_1164;
	public RSImageProducer aRSImageProducer_1165;
	public RSImageProducer aRSImageProducer_1166;
	public int daysSinceRecovChange;
	public RSSocket socketStream;
	public int minimapInt3;
	public int anInt1171;
	public static long soundEndReferenceStartMillis;
	public String myUsername;
	public String myPassword;
	public static int anInt1175;
	public boolean genericLoadingError;
	public final int[] anIntArray1177 = {0, 0, 0, 0, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3};
	public int reportAbuseInterfaceID;
	public NodeList pendingSceneChanges;
	public int[] chatAreaOffsets;
	public int[] tabAreaOffsets;
	public int[] chatBoxAreaOffsets;
	public byte[][] regionTerrainData;
	public int anInt1184;
	public int minimapInt1;
	public int anInt1186;
	public int anInt1187;
	public static int anInt1188;
	public int invOverlayInterfaceID;
	public int[] anIntArray1190;
	public int[] anIntArray1191;
	public Stream stream;
	public int anInt1193;
	public int splitpublicChat;
	public Background invBack;
	public Background mapBack;
	public Background chatBack;
	public String[] menuActionName;
	public Sprite aClass30_Sub2_Sub1_Sub1_1201;
	public Sprite aClass30_Sub2_Sub1_Sub1_1202;
	public final int[] anIntArray1203;
	static final int[] anIntArray1204 = {9104, 10275, 7595, 3610, 7975, 8526, 918, 38802, 24466, 10145, 58654, 5027, 1457, 16565, 34991, 25486};
	public static boolean flagged;
	public final int[] sound;
	public int anInt1208;
	public int minimapInt2;
	public int anInt1210;
	public int anInt1211;
	public String promptInput;
	public int anInt1213;
	public int[][][] intGroundArray;
	public long aLong1215;
	public int loginScreenCursorPos;
	public final Background[] modIcons;
	public long aLong1220;
	public int tabID;
	public int anInt1222;
	public boolean inputTaken;
	public int inputDialogState;
	public static int anInt1226;
	public int nextSong;
	public boolean songChanging;
	public final int[] anIntArray1229;
	public CollisionMap[] aClass11Array1230;
	public static int anIntArray1232[];
	public boolean aBoolean1233;
	public int[] regionKeys;
	public int[] regionTerrainFileIds;
	public int[] regionObjectFileIds;
	public int anInt1237;
	public int anInt1238;
	public final int anInt1239 = 100;
	public final int[] soundType;
	public boolean aBoolean1242;
	public int atInventoryLoopCycle;
	public int atInventoryInterface;
	public int atInventoryIndex;
	public int atInventoryInterfaceType;
	public byte[][] regionObjectData;
	public int tradeMode;
	public int anInt1249;
	public final int[] soundDelay;
	public final int[] soundVolume;
	public int chatSuppressionZone;
	public final boolean rsAlreadyLoaded;
	public int anInt1253;
	public int anInt1254;
	public boolean welcomeScreenRaised;
	public boolean messagePromptRaised;
	public static int soundEndReferenceByteLength;
	public byte[][][] byteGroundArray;
	public int previousSong;
	public int destX;
	public int destY;
	public Sprite minimapSprite;
	public int walkDestinationAdjusted;
	public int anInt1265;
	public String loginMessage1;
	public String loginMessage2;
	public int anInt1268;
	public int anInt1269;
	public TextDrawingArea aTextDrawingArea_1270;
	public TextDrawingArea aTextDrawingArea_1271;
	public TextDrawingArea chatTextDrawingArea;
	public int anInt1275;
	public int backDialogID;
	public int anInt1278;
	public int anInt1279;
	public int[] bigX;
	public int[] bigY;
	public int itemSelected;
	public int anInt1283;
	public int anInt1284;
	public int anInt1285;
	public String selectedItemName;
	public static int waypointsSinceKeepAlive;
	public int anInt1289;
	public static int tiara;
	public static int anInt1290;
	public boolean showInfo = false;
	public static int musicOutputVolume = 256;
	public static int[] channelVolumes = new int[] { 12800, 12800, 12800, 12800, 12800, 12800,
	12800, 12800, 12800, 12800, 12800, 12800,
	12800, 12800, 12800, 12800 };
	public static int songTransitionTicks = 0;
	public static MusicPlayer musicPlayer;
	public static boolean fetchMusic = false;
	public static int musicVolume2;
	public static int currentSongVolume = -1;
	public static byte[] queuedSongData;
	public static int fadeProgress = 0;
	public static int fadeStepPerTick = 0;
	public static int queuedSongVolume;
	public static boolean queuedSongLoop;
	public static int pendingSongFadeStep;
	public static boolean aBoolean995;
	public static int pendingSongFadeTicks;
	public static int musicVolume = 255;
	public int[] gameScreenOffsets;
	public int anInt1170;
	public int anInt1215;
	public int anInt1083;
	public int anInt992;
	public int anInt1273;
	public int anInt1034;

	static {
		anIntArray1019 = new int[99];
		int i = 0;
		for (int j = 0; j < 99; j++) {
			int l = j + 1;
			int i1 = (int) ((double) l + 300D * Math.pow(2D, (double) l / 7D));
			i += i1;
			anIntArray1019[j] = i / 4;
		}

		anIntArray1232 = new int[32];
		i = 2;
		for (int k = 0; k < 32; k++) {
			anIntArray1232[k] = i - 1;
			i += i;
		}

	}

	void mouseWheelDragged(int i, int j) {
		if (!mouseWheelDown)
			return;
		this.anInt1186 += i * 3;
		this.anInt1187 += (j << 1);
	}

	public void handleKeyPressed(int keyCode, char keyChar, boolean isControlDown, boolean isShiftDown)
	{
		super.handleKeyPressed(keyCode, keyChar, isControlDown, isShiftDown);
		switch (keyCode)
		{
			case 27: // VK_ESCAPE
				closeOpenInterfaces();
				break;
			case 112: // VK_F1
				needDrawTabArea = true;
				tabID = 3;
				tabAreaAltered = true;
				break;
			case 113: // VK_F2
				needDrawTabArea = true;
				tabID = 4;
				tabAreaAltered = true;
				break;
			case 114: // VK_F3
				needDrawTabArea = true;
				tabID = 5;
				tabAreaAltered = true;
				break;
			case 115: // VK_F4
				needDrawTabArea = true;
				tabID = 6;
				tabAreaAltered = true;
				break;
			case 116: // VK_F5
				needDrawTabArea = true;
				tabID = 0;
				tabAreaAltered = true;
				break;
			case 117: // VK_F6
				needDrawTabArea = true;
				tabID = 1;
				tabAreaAltered = true;
				break;
			case 118: // VK_F7
				needDrawTabArea = true;
				tabID = 2;
				tabAreaAltered = true;
				break;
			case 119: // VK_F8
				needDrawTabArea = true;
				tabID = 8;
				tabAreaAltered = true;
				break;
			case 120: // VK_F9
				needDrawTabArea = true;
				tabID = 11;
				tabAreaAltered = true;
				break;
			case 121: // VK_F10
				needDrawTabArea = true;
				tabID = 12;
				tabAreaAltered = true;
				break;
			case 122: // VK_F11
				needDrawTabArea = true;
				tabID = 13;
				tabAreaAltered = true;
				break;
			case 123: // VK_F12
				needDrawTabArea = true;
				tabID = 10;
				tabAreaAltered = true;
				break;
			case 33: // VK_PAGE_UP
				if (zoom > -1) {
					zoom--;
					if (ClientSettings.SHOW_ZOOM_LEVEL_MESSAGES) {
						pushMessage("Your zoom level is now: " + zoom, 0, "");
					}
				}
				break;
			case 34: // VK_PAGE_DOWN
				if (zoom < (WorldController.drawDistance / 3)) {
					zoom++;
					if (ClientSettings.SHOW_ZOOM_LEVEL_MESSAGES) {
						pushMessage("Your zoom level is now: " + zoom, 0, "");
					}
				}
				break;
			case 86: // VK_V
				if (isControlDown) {
					inputString += getClipBoard();
					if (inputString.length() > 80) {
						inputString = inputString.substring(0, 80);
					}
					inputTaken = true;
				}
				break;
		}
		if (ClientSettings.SCREENSHOTS_ENABLED && keyCode == 154 && isControlDown) {
			screenshot(true);
		}
	}

	public long calculateTotalExp() {
		long exp = 0;
		for (int i = 0; i < currentExp.length; i++) {
			exp += currentExp[i];
		}
		return exp;
	}

	public int calculateTotalLevels() {
		int levels = 0;
		for (int i = 0; i < maxStats.length; i++) {
			levels += maxStats[i];
		}
		// need to remove 4 for some reason
		return levels - 4;
	}

	
	public void definitionSearch(String name, int type) {
		int amount = 0;
		int definitionResultsTotal = 0;
		int definitionResultIDs[] = new int[352];
		String definitionResults[] = new String[352];
		String sType = "";
		if (type == 1) {
			amount = ItemDef.totalItems;
			sType = "Item";
		} else if (type == 2) {
			amount = EntityDef.totalNPCs;
			sType = "NPC";
		} else if (type == 3) {
			amount = ObjectDef.totalObjects;
			sType = "Object";
		} else {
			type = 1;
			amount = ItemDef.totalItems;
			sType = "Item";
		}	
		if (type != 1) {
			for (int line = 0; line < 100; line++) {
				pushMessage("", 0, "");
			}
		}
        if (name == null || name.length() == 0) {
            definitionResultsTotal = 0;
            return;
        }

        String search = name;
        String parts[] = new String[100];
        int found = 0;
        do {
            int regex = search.indexOf(" ");
            if (regex == -1) {
                break;
            }
            String part = search.substring(0, regex).trim();
            if (part.length() > 0) {
                parts[found++] = part.toLowerCase();
            }
            search = search.substring(regex + 1);
        } while (true);
		search = search.trim();
		if (search.length() > 0) {
			parts[found++] = search.toLowerCase();
        }
        definitionResultsTotal = 0;
        label0: for (int definition = 0; definition < amount; definition++) {
			String result = "";
			if (type == 1) {
				ItemDef item = ItemDef.forID(definition);
				if (item.certTemplateID != -1 || item.name == null) {
					continue;
				}
				result = item.name + "@bla@ - " + new String(item.description, StandardCharsets.UTF_8);
			} else if (type == 2) {
				EntityDef npc = EntityDef.forID(definition);
				if (npc.name == null) {
					continue;
				}
				result = npc.name;
			} else if (type == 3) {
				ObjectDef object = ObjectDef.forID(definition);
				if (object.name == null) {
					continue;
				}
				result = object.name;
			}
            for (int index = 0; index < found; index++) {
                if (!result.toLowerCase().contains(parts[index])) {
                    continue label0;
                }
            }
			
			if (type != 1) {
				pushMessage("@whi@[" + definition + "] @blu@" + result + "", 0, "");
			}
            definitionResults[definitionResultsTotal] = result;
            definitionResultIDs[definitionResultsTotal] = definition;
            definitionResultsTotal++;
            if (definitionResultsTotal >= definitionResults.length) {
                break;
            }
        }

		if (type == 1) {
			// Open bank interface
			needDrawTabArea = true;
			int interfaceID = 5382;
			RSInterface class9_1 = RSInterface.interfaceCache[interfaceID];
			openInterface(5292); // Bank interface
			RSInterface.interfaceCache[5383].disabledText = "Search results for @yel@" + name; // The Bank of Text

			int itemCount = 0;
			for (int ID : definitionResultIDs) {
				if (ID > 0 && itemCount < class9_1.itemIdsPlusOne.length) {
					class9_1.itemIdsPlusOne[itemCount] = ID + 1; // Sets item ID;
					class9_1.itemQuantities[itemCount++] = 1; // Sets item amoounts
				}
			}
			while (itemCount < class9_1.itemIdsPlusOne.length) {
				class9_1.itemIdsPlusOne[itemCount] = 0;
				class9_1.itemQuantities[itemCount++] = 0;
			}
		} else {
			pushMessage("@blu@" + sType + " @bla@search results for @blu@" + name + "@bla@ displayed above (@blu@" + definitionResultsTotal + "@bla@ results).", 0, "");
		}
    }

	public void openInterface(int interfaceID) {
		resetInterfaceModelAnimations(interfaceID);
		if (invOverlayInterfaceID != -1) {
			invOverlayInterfaceID = -1;
			needDrawTabArea = true;
			tabAreaAltered = true;
		}
		if (backDialogID != -1) {
			backDialogID = -1;
			inputTaken = true;
		}
		if (inputDialogState != 0) {
			inputDialogState = 0;
			inputTaken = true;
		}
		if (interfaceID == 15244) {
			if (ClientSettings.SNOW_OVERLAY_FORCE_ENABLED || (ClientSettings.SNOW_OVERLAY_ENABLED && Flo.getTodaysDate().contains(ClientSettings.SNOW_MONTH))) {
				openInterfaceID = 15819;
			} else {
				openInterfaceID = 15801;
			}
			fullScreenInterfaceId = 15244;
		} else {
			openInterfaceID = interfaceID;
		}
		aBoolean1149 = false;
	}

	public void openSideInterface(int tab, int interfaceID) {
		if (interfaceID == 0x00ffff) {
			interfaceID = -1;
		}
		tabInterfaceIDs[tab] = interfaceID;
		needDrawTabArea = true;
		tabAreaAltered = true;
	}

	public void adjustZoom(int notches) {
		if (openInterfaceID == -1) {
			if (notches < 0) {
				if (zoom > -1) {
					zoom--;
					if (ClientSettings.SHOW_ZOOM_LEVEL_MESSAGES) {
						pushMessage("Your zoom level is now: " + zoom, 0, "");
					}
				}
			} else {
				if (zoom < (WorldController.drawDistance / 3)) {
					zoom++;
					if (ClientSettings.SHOW_ZOOM_LEVEL_MESSAGES) {
						pushMessage("Your zoom level is now: " + zoom, 0, "");
					}
				}
			}
		}
	}

	public void handleMouseWheel(int notches, boolean isControlDown) {
		if (!ClientSettings.ANDROID && ClientSettings.CONTROL_KEY_ZOOMING && !isControlDown) {
			return;
		}
		// If mouse over main game screen, without anything else opened
		if (openInterfaceID == -1 && mouseX < 515 && mouseY < 340) {
			adjustZoom(notches);
		}
	}

	public String getClipBoard() {
		if (ClientSettings.ANDROID) {
			return AndroidHooks.getClipBoard();
		} else {
			return DesktopHooks.getClipBoard();
		}
	}

	public boolean canDragItemAt(int x, int y) {
		if (!loggedIn || menuOpen || itemSelected != 0 || spellSelected != 0) {
			return false;
		}
		int prevMouseX = super.mouseX;
		int prevMouseY = super.mouseY;
		super.mouseX = x;
		super.mouseY = y;
		lastActiveInvInterface = -1;
		mouseInvInterfaceIndex = -1;
		processRightClick();
		boolean canDrag = false;
		if (lastActiveInvInterface != -1 && mouseInvInterfaceIndex != -1) {
			RSInterface interf = RSInterface.interfaceCache[lastActiveInvInterface];
			if (interf != null && (interf.aBoolean259 || interf.aBoolean235)
					&& mouseInvInterfaceIndex >= 0 && mouseInvInterfaceIndex < interf.itemIdsPlusOne.length
					&& interf.itemIdsPlusOne[mouseInvInterfaceIndex] > 0) {
				canDrag = true;
			}
		}
		super.mouseX = prevMouseX;
		super.mouseY = prevMouseY;
		return canDrag;
	}

	public boolean startItemDrag(int x, int y) {
		if (!loggedIn || menuOpen || itemSelected != 0 || spellSelected != 0) {
			return false;
		}
		super.mouseX = x;
		super.mouseY = y;
		lastActiveInvInterface = -1;
		mouseInvInterfaceIndex = -1;
		processRightClick();
		if (lastActiveInvInterface != -1 && mouseInvInterfaceIndex != -1) {
			RSInterface interf = RSInterface.interfaceCache[lastActiveInvInterface];
			if (interf != null && (interf.aBoolean259 || interf.aBoolean235)
					&& mouseInvInterfaceIndex >= 0 && mouseInvInterfaceIndex < interf.itemIdsPlusOne.length
					&& interf.itemIdsPlusOne[mouseInvInterfaceIndex] > 0) {
				aBoolean1242 = true;
				anInt989 = 10;
				anInt1084 = lastActiveInvInterface;
				anInt1085 = mouseInvInterfaceIndex;
				activeInterfaceType = 2;
				anInt1087 = x;
				anInt1088 = y;
				if (openInterfaceID != -1 && interf.parentID == openInterfaceID) {
					activeInterfaceType = 1;
				}
				if (backDialogID != -1 && interf.parentID == backDialogID) {
					activeInterfaceType = 3;
				}
				super.clickMode2 = 1;
				if (activeInterfaceType == 2) {
					needDrawTabArea = true;
				}
				return true;
			}
		}
		return false;
	}

	public boolean isScrollBarAt(int x, int y) {
		if (!loggedIn) {
			return false;
		}
		// 1. Chatbox scrollbar:
		// Chat area is drawn at full screen x=17, y=357. Scrollbar thumb is at relative x=463, y=0, width 16, height 77.
		// Screen coordinates: x in [480..496], y in [357..434].
		if (backDialogID == -1) {
			if (x >= 465 && x <= 512 && y >= 345 && y <= 445) {
				return true;
			}
		}

		// 2. Full screen interface (e.g. cutscene / welcome screen)
		if (fullScreenInterfaceId != -1) {
			if (checkInterfaceScrollBar(fullScreenInterfaceId, 0, 0, x, y)) {
				return true;
			}
		}

		// 3. Open main interface (bank, shop, quest journal, etc.)
		if (openInterfaceID != -1) {
			if (checkInterfaceScrollBar(openInterfaceID, 4, 4, x, y)) {
				return true;
			}
		}

		// 4. Side tab / inventory overlay interface
		if (invOverlayInterfaceID != -1) {
			if (checkInterfaceScrollBar(invOverlayInterfaceID, 553, 205, x, y)) {
				return true;
			}
		} else if (tabID >= 0 && tabID < tabInterfaceIDs.length && tabInterfaceIDs[tabID] != -1) {
			if (checkInterfaceScrollBar(tabInterfaceIDs[tabID], 553, 205, x, y)) {
				return true;
			}
		}

		// 5. Chat dialogue interfaces (NPC chat, options, etc.)
		if (backDialogID != -1) {
			if (checkInterfaceScrollBar(backDialogID, 17, 357, x, y)) {
				return true;
			}
		} else if (dialogID != -1) {
			if (checkInterfaceScrollBar(dialogID, 17, 357, x, y)) {
				return true;
			}
		}

		return false;
	}

	private boolean checkInterfaceScrollBar(int interfaceId, int xOffset, int yOffset, int clickX, int clickY) {
		if (interfaceId < 0 || interfaceId >= RSInterface.interfaceCache.length) {
			return false;
		}
		RSInterface rsi = RSInterface.interfaceCache[interfaceId];
		if (rsi == null) {
			return false;
		}
		return checkInterfaceScrollBar(rsi, xOffset, yOffset, 0, clickX, clickY);
	}

	private boolean checkInterfaceScrollBar(RSInterface rsi, int i, int l, int scrollPos, int clickX, int clickY) {
		if (rsi == null || rsi.children == null || rsi.hidden) {
			return false;
		}
		int count = rsi.children.length;
		for (int idx = 0; idx < count; idx++) {
			int childId = rsi.children[idx];
			if (childId < 0 || childId >= RSInterface.interfaceCache.length) {
				continue;
			}
			RSInterface child = RSInterface.interfaceCache[childId];
			if (child == null || child.hidden) {
				continue;
			}
			int i2 = rsi.childX[idx] + i + child.offsetX;
			int j2 = rsi.childY[idx] + l - scrollPos + child.offsetY;
			if (child.type == 0) {
				if (child.scrollMax > child.height) {
					int sbX = i2 + child.width;
					int sbY = j2;
					int sbH = child.height;
					// Standard scrollbar width is 16. Allow a touch-friendly margin around it
					if (clickX >= sbX - 10 && clickX <= sbX + 26 && clickY >= sbY - 5 && clickY <= sbY + sbH + 5) {
						return true;
					}
				}
				if (checkInterfaceScrollBar(child, i2, j2, child.scrollPosition, clickX, clickY)) {
					return true;
				}
			}
		}
		return false;
	}
}
