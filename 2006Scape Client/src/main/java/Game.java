/**
 * NOTICE: IF YOU CHANGE ANYTHING IN GAME.JAVA, PLEASE COPY-PASTE THE WHOLE CLASS OVER TO LOCALGAME.JAVA
 * THIS IS TO ALLOW LOCAL PARABOT TO CONTINUE TO WORK
 */

import javax.imageio.ImageIO;
import javax.swing.*;
import java.applet.AppletContext;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
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
    

	public static String intToKOrMilLongName(int i) {
		String s = String.valueOf(i);
		for (int k = s.length() - 3; k > 0; k -= 3) {
			s = s.substring(0, k) + "," + s.substring(k);
		}
		if (s.length() > 8) {
			s = "@gre@" + s.substring(0, s.length() - 8) + " million @whi@(" + s + ")";
		} else if (s.length() > 4) {
			s = "@cya@" + s.substring(0, s.length() - 4) + "K @whi@(" + s + ")";
		}
		return " " + s;
	}
	
	static final boolean musicIsntNull() {
            if (midiPlayer == null)
			return false;
		return true;
	}
	
       static final void closeMidiSystem() {
            if (midiPlayer != null) {
                       stopMidiPlayback(false);
                        if (midiFadeCycles > 0) {
                                midiPlayer.setVolume(256);
                                midiFadeCycles = 0;
			}
                       midiPlayer.shutdown();
                 midiPlayer = null;
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
                                        decompressors[cacheIndex].writeEntry(data.length, data, fileIndex);
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
                                        decompressors[3].writeEntry(abyte0.length, abyte0, MusicIndex);
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
                               setMidiVolume(i);
		}
	}
	
       static final void setMidiVolume(int i) {
            if (midiPlayer != null) {
			if (midiFadeCycles == 0) {
				if (currentMidiVolume >= 0) {
					currentMidiVolume = i;
                                    midiPlayer.adjustVolume(i, 0);
				}
			} else if (queuedMidiData != null)
				queuedMidiVolume = i;
		}
	}
	
       static final synchronized void stopMusic(boolean bool) {
		if (musicIsntNull()) {
               stopMidiPlayback(bool);
			fetchMusic = false;
		}
	}
	
       static final void stopMidiPlayback(boolean bool) {
               playMidiTrack(0, null, bool);
	}
	
	static final boolean constructMusic() {
		midiFadeCycles = 20;
		try {
                midiPlayer = (MidiPlayer) Class.forName("SystemMidiPlayer").newInstance();
		} catch (Throwable throwable) {
		    return false;
		}
		return true;
	}
	
       final synchronized void queueSong(int i_30_, int volume,
                    boolean bool, int music) {
		if (musicIsntNull()) {
			nextSong = music;
                        onDemandFetcher.queueRequest(2, nextSong);
			musicVolume2 = volume;
			queuedSongId = -1;
			autoPlaySong = true;
			nextSongDelay = i_30_;
		}
	}
	
       final synchronized void playSong(int i, boolean bool, int music) {
		if (musicIsntNull()) {
			nextSong = music;
                        onDemandFetcher.queueRequest(2, nextSong);
			musicVolume2 = i;
			queuedSongId = -1;
			autoPlaySong = true;
		    nextSongDelay = -1;
		}
	}
	
	public void sendFrame126(String str,int i) {
		RSInterface.interfaceCache[i].disabledText = str;
		if(RSInterface.interfaceCache[i].parentID == tabInterfaceIDs[tabID])
			needDrawTabArea = true;
	}
	
	public static byte[] musicData;
	
       static final synchronized void processMusicQueue() {
		if (musicIsntNull()) {
			if (fetchMusic) {
				byte[] is = musicData;
				if (is != null) {
                                       if (nextSongDelay >= 0)
                                               initiateMidiFade(autoPlaySong, nextSongDelay, musicVolume2, is);
                                       else if (queuedSongId >= 0)
                                               queueMidiTrack(queuedSongId, -1, autoPlaySong, is, musicVolume2);
					else
                                               playMidiTrack(musicVolume2, is, autoPlaySong);
					fetchMusic = false;
				}
			}
                   updateMidiFade(0);
		}
	}
	
       static final int calculateLogVolume(int i) {
               return (int) (Math.log((double) i * 0.00390625) * 868.5889638065036 + 0.5);
	}
	
       static final void playMidiTrack(int i_2_, byte[] is, boolean bool) {
            if (midiPlayer != null) {
			if (currentMidiVolume >= 0) {
                    midiPlayer.stopMidi();
				currentMidiVolume = -1;
				queuedMidiData = null;
				midiFadeCycles = 20;
				fadeVolume = 0;
			}
		    if (is != null) {
		    	if (midiFadeCycles > 0) {
                            midiPlayer.setVolume(i_2_);
		    		midiFadeCycles = 0;
		    	}
		    	currentMidiVolume = i_2_;
                    midiPlayer.playMidi(i_2_, is, 0, bool);
		    }
		}
	}
	
       static final void queueMidiTrack(int i, int i_29_, boolean bool, byte[] is, int i_30_) {
            if (midiPlayer != null) {
			if (i_29_ >= (currentMidiVolume ^ 0xffffffff)) {
				i -= 20;
				if (i < 1)
					i = 1;
				midiFadeCycles = i;
				if (currentMidiVolume == 0)
					fadeStep = 0;
				else {
                                       int i_31_ = calculateLogVolume(currentMidiVolume);
					i_31_ -= fadeVolume;
					fadeStep = ((fadeStep - 1 + (i_31_ + 3600)) / fadeStep);
				}
				midiLooping = bool;
				queuedMidiData = is;
				queuedMidiVolume = i_30_;
			} else if (midiFadeCycles != 0) {
				midiLooping = bool;
				queuedMidiData = is;
				queuedMidiVolume = i_30_;
                       } else
                               playMidiTrack(i_30_, is, bool);
		}
	}
	
       static final void initiateMidiFade(boolean bool, int i, int i_2_, byte[] is) {
            if (midiPlayer != null) {
			if (currentMidiVolume >= 0) {
				fadeStep = i;
				if (currentMidiVolume != 0) {
                                       int i_4_ = calculateLogVolume(currentMidiVolume);
					i_4_ -= fadeVolume;
					midiFadeCycles = (i_4_ + 3600) / i;
					if (midiFadeCycles < 1)
						midiFadeCycles = 1;
				} else
					midiFadeCycles = 1;
				queuedMidiData = is;
				queuedMidiVolume = i_2_;
				midiLooping = bool;
                       } else if (midiFadeCycles == 0)
                               playMidiTrack(i_2_, is, bool);
			else {
				queuedMidiVolume = i_2_;
				midiLooping = bool;
				queuedMidiData = is;
			}
		}
	}
	
       static final void updateMidiFade(int i) {
            if (midiPlayer != null) {
			if (currentMidiVolume < i) {
				if (midiFadeCycles > 0) {
					midiFadeCycles--;
					if (midiFadeCycles == 0) {
                                                if (queuedMidiData == null)
                                                        midiPlayer.setVolume(256);
                                                else {
                                                        midiPlayer.setVolume(queuedMidiVolume);
                                                        currentMidiVolume = queuedMidiVolume;
                                                        midiPlayer.playMidi(queuedMidiVolume, queuedMidiData, 0, midiLooping);
							queuedMidiData = null;
						}
						fadeVolume = 0;
					}
				}
			} else if (midiFadeCycles > 0) {
				fadeVolume += fadeStep;
                                midiPlayer.adjustVolume(currentMidiVolume, fadeVolume);
				midiFadeCycles--;
				if (midiFadeCycles == 0) {
                                        midiPlayer.stopMidi();
					midiFadeCycles = 20;
					currentMidiVolume = -1;
				}
			}
                        midiPlayer.poll(i - 122);
		}
	}

	private void stopMidi() {
		if (Signlink.music != null) {
			if (Signlink.music.isRunning()) {
				Signlink.fadeOut();
				Signlink.music.stop();
				Signlink.midi = "stop";
			}
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
		fullScreenBackground.initDrawingArea();
		Texture.lineOffsets = chatAreaOffsets;
		chatBack.draw(0, 0);
		if (messagePromptRaised) {
			chatTextDrawingArea.textCenter(0, inputPrompt, 40, 239);
			chatTextDrawingArea.textCenter(128, promptInput + "*", 60, 239);
		} else if (inputDialogState == 1) {
			chatTextDrawingArea.textCenter(0, "Enter amount:", 40, 239);
			chatTextDrawingArea.textCenter(128, amountOrNameInput + "*", 60, 239);
		} else if (inputDialogState == 2) {
			chatTextDrawingArea.textCenter(0, "Enter name:", 40, 239);
			chatTextDrawingArea.textCenter(128, amountOrNameInput + "*", 60, 239);
		} else if (messagePrompt != null) {
			chatTextDrawingArea.textCenter(0, messagePrompt, 40, 239);
			chatTextDrawingArea.textCenter(128, "Click to continue", 60, 239);
		} else if (backDialogID != -1) {
			drawInterface(0, 0, RSInterface.interfaceCache[backDialogID], 0);//CHANGED THIS - andrew was 0, 0
		} else if (dialogID != -1) {
			drawInterface(0, 0, RSInterface.interfaceCache[dialogID], 0);//CHANGED THIS - andrew was 0, 0
		} else {
			TextDrawingArea textDrawingArea = boldFont;
			int j = 0;
			DrawingArea.setDrawingArea(77, 0, 463, 0);
			for (int k = 0; k < 100; k++) {
				if (chatMessages[k] != null) {
					int l = chatTypes[k];
					int i1 = 70 - j * 14 + chatScrollPosition;
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
							textDrawingArea.textLeftShadow(false, 4, 0, chatMessages[k], i1);
							} catch (Exception e) {
								
							}
						}
						j++;
					}
					if ((l == 1 || l == 2) && (l == 1 || publicChatMode == 0 || publicChatMode == 1 && isFriendOrSelf(s1))) {
						if (i1 > 0 && i1 < 110) {
							int j1 = 4;
							if (byte0 == 1) {
								modIcons[0].draw(j1, i1 - 12);
								j1 += 14;
							}
							if (byte0 == 2) {
								modIcons[1].draw(j1, i1 - 12);
								j1 += 14;
							}
							textDrawingArea.textLeft(0, s1 + ":", i1, j1);
							j1 += textDrawingArea.getTextWidth(s1) + 8;
							textDrawingArea.textLeft(255, chatMessages[k], i1, j1);
						}
						j++;
					}
					if ((l == 3 || l == 7) && splitpublicChat == 0 && (l == 7 || privateChatMode == 0 || privateChatMode == 1 && isFriendOrSelf(s1))) {
						if (i1 > 0 && i1 < 110) {
							int k1 = 4;
							textDrawingArea.textLeft(0, "From", i1, k1);
							k1 += textDrawingArea.getTextWidth("From ");
							if (byte0 == 1) {
								modIcons[0].draw(k1, i1 - 12);
								k1 += 14;
							}
							if (byte0 == 2) {
								modIcons[1].draw(k1, i1 - 12);
								k1 += 14;
							}
							textDrawingArea.textLeft(0, s1 + ":", i1, k1);
							k1 += textDrawingArea.getTextWidth(s1) + 8;
							textDrawingArea.textLeft(0x800000, chatMessages[k], i1, k1);
						}
						j++;
					}
					if (l == 4 && (tradeMode == 0 || tradeMode == 1 && isFriendOrSelf(s1))) {
						if (i1 > 0 && i1 < 110) {
							textDrawingArea.textLeft(0x800080, s1 + " " + chatMessages[k], i1, 4);
						}
						j++;
					}
					if (l == 5 && splitpublicChat == 0 && privateChatMode < 2) {
						if (i1 > 0 && i1 < 110) {
							textDrawingArea.textLeft(0x800000, chatMessages[k], i1, 4);
						}
						j++;
					}
					if (l == 6 && splitpublicChat == 0 && privateChatMode < 2) {
						if (i1 > 0 && i1 < 110) {
							textDrawingArea.textLeft(0, "To " + s1 + ":", i1, 4);
							textDrawingArea.textLeft(0x800000, chatMessages[k], i1, 12 + textDrawingArea.getTextWidth("To " + s1));
						}
						j++;
					}
					if (l == 8 && (tradeMode == 0 || tradeMode == 1 && isFriendOrSelf(s1))) {
						if (i1 > 0 && i1 < 110) {
							textDrawingArea.textLeft(0x7e3200, s1 + " " + chatMessages[k], i1, 4);
						}
						j++;
					}
				}
			}

			DrawingArea.defaultDrawingAreaSize();
			chatScrollHeight = j * 14 + 7;
			if (chatScrollHeight < 78) {
				chatScrollHeight = 78;
			}
			drawScrollThumb(77, chatScrollHeight - chatScrollPosition - 77, 0, 463, chatScrollHeight);
			String s;
			if (myPlayer != null && myPlayer.name != null) {
				s = myPlayer.name;
			} else {
				s = TextClass.fixName(myUsername);
			}
			textDrawingArea.textLeft(0, s + ":", 90, 4);
			textDrawingArea.textLeft(255, inputString + "*", 90, 6 + textDrawingArea.getTextWidth(s + ": "));
			DrawingArea.drawHorizontalLine(77, 0, 479, 0);
		}
		if (menuOpen && menuScreenArea == 2) {
			drawMenu();
		}
		fullScreenBackground.drawGraphics(357, super.graphics, 17);
		tabAreaBuffer.initDrawingArea();
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
				if (k < menuOffsetX - 10 || k > menuOffsetX + menuWidth + 10 || j1 < menuOffsetY - 10 || j1 > menuOffsetY + menuHeight + 10) {
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
						itemBeingDragged = false;
						dragCounter = 0;
						dragInterfaceId = j2;
						draggedSlot = l1;
						activeInterfaceType = 2;
						dragStartX = super.saveClickX;
						dragStartY = super.saveClickY;
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
			if (j == 1 && (oneMouseButtonMode == 1 || menuHasAddFriend(menuActionRow - 1)) && menuActionRow > 2) {
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

       public void constructMapRegion() {
		try {
			lastPlane = -1;
			graphicsObjectList.removeAll();
			projectileList.removeAll();
			Texture.clearCache();
			unlinkMRUNodes();
			worldController.initToNull();
			System.gc();
			for (int i = 0; i < 4; i++) {
				collisionMaps[i].reset();
			}

			for (int l = 0; l < 4; l++) {
				for (int k1 = 0; k1 < 104; k1++) {
					for (int j2 = 0; j2 < 104; j2++) {
						tileFlags[l][k1][j2] = 0;
					}

				}

			}

			ObjectManager objectManager = new ObjectManager(tileFlags, tileHeights);
			int k2 = terrainData.length;
			stream.createFrame(0);
			if (!isDynamicRegion) {
				for (int i3 = 0; i3 < k2; i3++) {
					int i4 = (regionBaseIds[i3] >> 8) * 64 - baseX;
					int k5 = (regionBaseIds[i3] & 0xff) * 64 - baseY;
					byte abyte0[] = terrainData[i3];
					if (abyte0 != null) {
						objectManager.loadRegion(abyte0, k5, i4, (currentRegionX - 6) * 8, (currentRegionY - 6) * 8, collisionMaps);
					}
				}

				for (int j4 = 0; j4 < k2; j4++) {
					int l5 = (regionBaseIds[j4] >> 8) * 64 - baseX;
					int k7 = (regionBaseIds[j4] & 0xff) * 64 - baseY;
					byte abyte2[] = terrainData[j4];
					if (abyte2 == null && currentRegionY < 800) {
						objectManager.clearRegion(k7, 64, 64, l5);
					}
				}

				mapLoadPacketCounter++;
				if (mapLoadPacketCounter > 160) {
					mapLoadPacketCounter = 0;
					stream.createFrame(238);
					stream.writeWordBigEndian(96);
				}
				stream.createFrame(0);
				for (int i6 = 0; i6 < k2; i6++) {
					byte abyte1[] = objectMapData[i6];
					if (abyte1 != null) {
						int l8 = (regionBaseIds[i6] >> 8) * 64 - baseX;
						int k9 = (regionBaseIds[i6] & 0xff) * 64 - baseY;
						objectManager.loadObjects(l8, collisionMaps, k9, worldController, abyte1);
					}
				}

			}
			if (isDynamicRegion) {
				for (int j3 = 0; j3 < 4; j3++) {
					for (int k4 = 0; k4 < 13; k4++) {
						for (int j6 = 0; j6 < 13; j6++) {
							int l7 = dynamicRegionData[j3][k4][j6];
							if (l7 != -1) {
								int i9 = l7 >> 24 & 3;
								int l9 = l7 >> 1 & 3;
								int j10 = l7 >> 14 & 0x3ff;
								int l10 = l7 >> 3 & 0x7ff;
								int j11 = (j10 / 8 << 8) + l10 / 8;
								for (int l11 = 0; l11 < regionBaseIds.length; l11++) {
									if (regionBaseIds[l11] != j11 || terrainData[l11] == null) {
										continue;
									}
									objectManager.loadChunk(i9, l9, collisionMaps, k4 * 8, (j10 & 7) * 8, terrainData[l11], (l10 & 7) * 8, j3, j6 * 8);
									break;
								}

							}
						}

					}

				}

				for (int l4 = 0; l4 < 13; l4++) {
					for (int k6 = 0; k6 < 13; k6++) {
						int i8 = dynamicRegionData[0][l4][k6];
						if (i8 == -1) {
							objectManager.clearRegion(k6 * 8, 8, 8, l4 * 8);
						}
					}

				}

				stream.createFrame(0);
				for (int l6 = 0; l6 < 4; l6++) {
					for (int j8 = 0; j8 < 13; j8++) {
						for (int j9 = 0; j9 < 13; j9++) {
							int i10 = dynamicRegionData[l6][j8][j9];
							if (i10 != -1) {
								int k10 = i10 >> 24 & 3;
								int i11 = i10 >> 1 & 3;
								int k11 = i10 >> 14 & 0x3ff;
								int i12 = i10 >> 3 & 0x7ff;
								int j12 = (k11 / 8 << 8) + i12 / 8;
								for (int k12 = 0; k12 < regionBaseIds.length; k12++) {
									if (regionBaseIds[k12] != j12 || objectMapData[k12] == null) {
										continue;
									}
									objectManager.loadObjectChunk(collisionMaps, worldController, k10, j8 * 8, (i12 & 7) * 8, l6, objectMapData[k12], (k11 & 7) * 8, i11, j9 * 8);
									break;
								}

							}
						}

					}

				}

			}
			stream.createFrame(0);
                        objectManager.buildLandscape(collisionMaps, worldController);
			if(tabAreaBuffer != null) {
				tabAreaBuffer.initDrawingArea();
				Texture.lineOffsets = chatBoxAreaOffsets;
			}
			stream.createFrame(0);
			int k3 = ObjectManager.lowestPlane;
			if (k3 > plane) {
				k3 = plane;
			}
			if (k3 < plane - 1) {
				k3 = plane - 1;
			}
			if (lowMem) {
                                worldController.setActivePlane(ObjectManager.lowestPlane);
			} else {
                                worldController.setActivePlane(0);
			}
			for (int i5 = 0; i5 < 104; i5++) {
				for (int i7 = 0; i7 < 104; i7++) {
					spawnGroundItem(i5, i7);
				}

			}

			terrainLoadCycle++;
			if (terrainLoadCycle > 98) {
				terrainLoadCycle = 0;
				stream.createFrame(150);
			}
                       locatePendingSpawns();
		} catch (Exception exception) {
		}
		ObjectDef.mruNodes1.unlinkAll();
		if (super.gameFrame != null) {
			stream.createFrame(210);
			stream.writeDWord(0x3f008edd);
		}
		if (lowMem && Signlink.cache_dat != null) {
			int j = onDemandFetcher.getVersionCount(0);
			for (int i1 = 0; i1 < j; i1++) {
				int l1 = onDemandFetcher.getModelIndex(i1);
				if ((l1 & 0x79) == 0) {
					Model.unload(i1);
				}
			}

		}
		System.gc();
                Texture.initCache();
                onDemandFetcher.clearPriorityQueue();
		int k = (currentRegionX - 6) / 8 - 1;
		int j1 = (currentRegionX + 6) / 8 + 1;
		int i2 = (currentRegionY - 6) / 8 - 1;
		int l2 = (currentRegionY + 6) / 8 + 1;
		if (forceMapReload) {
			k = 49;
			j1 = 50;
			i2 = 49;
			l2 = 50;
		}
		for (int l3 = k; l3 <= j1; l3++) {
			for (int j5 = i2; j5 <= l2; j5++) {
				if (l3 == k || l3 == j1 || j5 == i2 || j5 == l2) {
                                        int j7 = onDemandFetcher.getRegionArchiveId(0, j5, l3);
                                        if (j7 != -1) {
                                                onDemandFetcher.requestFileNow(j7, 3);
                                        }
                                        int k8 = onDemandFetcher.getRegionArchiveId(1, j5, l3);
                                        if (k8 != -1) {
                                                onDemandFetcher.requestFileNow(k8, 3);
                                        }
				}
			}

		}

	}

	public void unlinkMRUNodes() {
		ObjectDef.mruNodes1.unlinkAll();
		ObjectDef.mruNodes2.unlinkAll();
		EntityDef.mruNodes.unlinkAll();
            ItemDef.modelCache.unlinkAll();
            ItemDef.spriteCache.unlinkAll();
		Player.mruNodes.unlinkAll();
            SpotAnim.modelCache.unlinkAll();
	}

       public void generateMinimap(int i) {
               int ai[] = minimapImage.pixels;
		int j = ai.length;
		for (int k = 0; k < j; k++) {
			ai[k] = 0;
		}

		for (int l = 1; l < 103; l++) {
			int i1 = 24628 + (103 - l) * 512 * 4;
			for (int k1 = 1; k1 < 103; k1++) {
                                if ((tileFlags[i][k1][l] & 0x18) == 0) {
                                        worldController.renderMinimapTile(ai, i1, i, k1, l);
                                }
                                if (i < 3 && (tileFlags[i + 1][k1][l] & 8) != 0) {
                                        worldController.renderMinimapTile(ai, i1, i + 1, k1, l);
                                }
				i1 += 4;
			}

		}

		int j1 = (238 + (int) (Math.random() * 20D) - 10 << 16) + (238 + (int) (Math.random() * 20D) - 10 << 8) + 238 + (int) (Math.random() * 20D) - 10;
		int l1 = 238 + (int) (Math.random() * 20D) - 10 << 16;
               minimapImage.initializeDrawingArea();
		for (int i2 = 1; i2 < 103; i2++) {
			for (int j2 = 1; j2 < 103; j2++) {
				if ((tileFlags[i][j2][i2] & 0x18) == 0) {
                                       drawMinimapLoc(i2, j1, j2, l1, i);
				}
				if (i < 3 && (tileFlags[i + 1][j2][i2] & 8) != 0) {
                                       drawMinimapLoc(i2, j1, j2, l1, i + 1);
				}
			}

		}
		if(tabAreaBuffer != null) {
			tabAreaBuffer.initDrawingArea();
			Texture.lineOffsets = chatBoxAreaOffsets;
		}
		minimapIconCount = 0;
		for (int k2 = 0; k2 < 104; k2++) {
			for (int l2 = 0; l2 < 104; l2++) {
				int i3 = worldController.getTileDecorationUid(plane, k2, l2);
				if (i3 != 0) {
					i3 = i3 >> 14 & 0x7fff;
					int j3 = ObjectDef.forID(i3).anInt746;
					if (j3 >= 0) {
						int k3 = k2;
						int l3 = l2;
						if (j3 != 22 && j3 != 29 && j3 != 34 && j3 != 36 && j3 != 46 && j3 != 47 && j3 != 48) {
							byte byte0 = 104;
							byte byte1 = 104;
							int ai1[][] = collisionMaps[plane].clippingFlags;
							for (int i4 = 0; i4 < 10; i4++) {
								int j4 = (int) (Math.random() * 4D);
								if (j4 == 0 && k3 > 0 && k3 > k2 - 3 && (ai1[k3 - 1][l3] & 0x1280108) == 0) {
									k3--;
								}
								if (j4 == 1 && k3 < byte0 - 1 && k3 < k2 + 3 && (ai1[k3 + 1][l3] & 0x1280180) == 0) {
									k3++;
								}
								if (j4 == 2 && l3 > 0 && l3 > l2 - 3 && (ai1[k3][l3 - 1] & 0x1280102) == 0) {
									l3--;
								}
								if (j4 == 3 && l3 < byte1 - 1 && l3 < l2 + 3 && (ai1[k3][l3 + 1] & 0x1280120) == 0) {
									l3++;
								}
							}

						}
						minimapIconSprites[minimapIconCount] = mapFunctions[j3];
						minimapIconX[minimapIconCount] = k3;
						minimapIconY[minimapIconCount] = l3;
						minimapIconCount++;
					}
				}
			}

		}

	}

	public void spawnGroundItem(int i, int j) {
		NodeList class19 = groundArray[plane][i][j];
		if (class19 == null) {
                        worldController.clearItemPile(plane, i, j);
			return;
		}
		long k = Long.MIN_VALUE;
		Object obj = null;
		for (Item item = (Item) class19.reverseGetFirst(); item != null; item = (Item) class19.reverseGetNext()) {
			ItemDef itemDef = ItemDef.lookup(item.ID);
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
		for (Item class30_sub2_sub4_sub2_1 = (Item) class19.reverseGetFirst(); class30_sub2_sub4_sub2_1 != null; class30_sub2_sub4_sub2_1 = (Item) class19.reverseGetNext()) {
			if (class30_sub2_sub4_sub2_1.ID != ((Item) obj).ID && obj1 == null) {
				obj1 = class30_sub2_sub4_sub2_1;
			}
			if (class30_sub2_sub4_sub2_1.ID != ((Item) obj).ID && class30_sub2_sub4_sub2_1.ID != ((Item) obj1).ID && obj2 == null) {
				obj2 = class30_sub2_sub4_sub2_1;
			}
		}

		int i1 = i + (j << 7) + 0x60000000;
                worldController.addItemPile(i, i1, ((Animable) obj1), getTileHeight(plane, j * 128 + 64, i * 128 + 64), ((Animable) obj2), ((Animable) obj), plane, j);
	}

       public void addNpcsToScene(boolean flag) {
		for (int j = 0; j < npcCount; j++) {
			NPC npc = npcArray[npcIndices[j]];
			int k = 0x20000000 + (npcIndices[j] << 14);
                       if (npc == null || !npc.isVisible() || npc.definition.priorityRender != flag) {
				continue;
			}
			int l = npc.x >> 7;
			int i1 = npc.y >> 7;
			if (l < 0 || l >= 104 || i1 < 0 || i1 >= 104) {
				continue;
			}
			if (npc.size == 1 && (npc.x & 0x7f) == 64 && (npc.y & 0x7f) == 64) {
				if (occupiedTiles[l][i1] == waveCycle) {
					continue;
				}
				occupiedTiles[l][i1] = waveCycle;
			}
                       if (!npc.definition.clickable) {
				k += 0x80000000;
			}
                   worldController.addAnimableObject(plane, npc.currentHeading, getTileHeight(plane, npc.y, npc.x), k, npc.y, (npc.size - 1) * 64 + 60, npc.x, npc, npc.aBoolean1541);
		}
	}

	public boolean replayWave() {
		return Signlink.wavereplay();
	}

	public void loadError() {
		String s = "ondemand";// was a constant parameter
		try {
			getAppletContext().showDocument(new URL(getCodeBase(), "loaderror_" + s + ".html"));
		} catch (Exception exception) {
			exception.printStackTrace();
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
		if (class9.type != 0 || class9.children == null || class9.aBoolean266) {
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
			if ((class9_1.hoverTarget >= 0 || class9_1.hoverTextColor != 0) && k >= i2 && i1 >= j2 && k < i2 + class9_1.width && i1 < j2 + class9_1.height) {
				if (class9_1.hoverTarget >= 0) {
					hoveredWidgetId = class9_1.hoverTarget;
				} else {
					hoveredWidgetId = class9_1.id;
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
				if (class9_1.atActionType == 6 && !actionPending && k >= i2 && i1 >= j2 && k < i2 + class9_1.width && i1 < j2 + class9_1.height) {
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
								if (class9_1.inv[k2] > 0) {
									ItemDef itemDef = ItemDef.lookup(class9_1.inv[k2] - 1);
									if (itemSelected == 1 && class9_1.isInventoryInterface) {
										if (class9_1.id != selectedItemInterfaceId || k2 != selectedItemSlot) {
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
										menuActionName[menuActionRow] = "Examine @lre@" + itemDef.name + (showInfo ? " @gre@(@whi@" + (class9_1.inv[k2] - 1) + "@gre@)" : "");
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

	public void drawScrollThumb(int j, int k, int l, int i1, int j1) {
		scrollBar1.draw(i1, l);
		scrollBar2.draw(i1, l + j - 16);
		DrawingArea.fillArea(j - 32, l + 16, scrollBarColor, 16, i1);
		int k1 = (j - 32) * j / j1;
		if (k1 < 8) {
			k1 = 8;
		}
		int l1 = (j - 32 - k1) * k / (j1 - j);
		DrawingArea.fillArea(k1, l + 16 + l1, scrollBarHandleColor, 16, i1);
		DrawingArea.drawVerticalLine(l + 16 + l1, scrollBarLightColor, k1, i1);
		DrawingArea.drawVerticalLine(l + 16 + l1, scrollBarLightColor, k1, i1 + 1);
		DrawingArea.drawHorizontalLine(l + 16 + l1, scrollBarLightColor, 16, i1);
		DrawingArea.drawHorizontalLine(l + 17 + l1, scrollBarLightColor, 16, i1);
		DrawingArea.drawVerticalLine(l + 16 + l1, scrollBarDarkColor, k1, i1 + 15);
		DrawingArea.drawVerticalLine(l + 17 + l1, scrollBarDarkColor, k1 - 1, i1 + 14);
		DrawingArea.drawHorizontalLine(l + 15 + l1 + k1, scrollBarDarkColor, 16, i1);
		DrawingArea.drawHorizontalLine(l + 14 + l1 + k1, scrollBarDarkColor, 15, i1 + 1);
	}

	public void updateNPCs(Stream stream, int i) {
		entityRemovalCount = 0;
		playerUpdateCount = 0;
               updateNpcList(stream);
               addLocalNPCs(i, stream);
               processNpcUpdateMasks(stream);
		for (int k = 0; k < entityRemovalCount; k++) {
			int l = removedEntityIndices[k];
			if (npcArray[l].lastUpdateCycle != loopCycle) {
                                npcArray[l].definition = null;
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
				chatSettingsUpdateNeeded = true;
				inputTaken = true;
				stream.createFrame(95);
				stream.writeWordBigEndian(publicChatMode);
				stream.writeWordBigEndian(privateChatMode);
				stream.writeWordBigEndian(tradeMode);
			}
			if (super.saveClickX >= 135 && super.saveClickX <= 235 && super.saveClickY >= 467 && super.saveClickY <= 499) {
				privateChatMode = (privateChatMode + 1) % 3;
				chatSettingsUpdateNeeded = true;
				inputTaken = true;
				stream.createFrame(95);
				stream.writeWordBigEndian(publicChatMode);
				stream.writeWordBigEndian(privateChatMode);
				stream.writeWordBigEndian(tradeMode);
			}
			if (super.saveClickX >= 273 && super.saveClickX <= 373 && super.saveClickY >= 467 && super.saveClickY <= 499) {
				tradeMode = (tradeMode + 1) % 3;
				chatSettingsUpdateNeeded = true;
				inputTaken = true;
				stream.createFrame(95);
				stream.writeWordBigEndian(publicChatMode);
				stream.writeWordBigEndian(privateChatMode);
				stream.writeWordBigEndian(tradeMode);
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
			abuseReportCounter++;
			if (abuseReportCounter > 1386) {
				abuseReportCounter = 0;
				stream.createFrame(165);
				stream.writeWordBigEndian(0);
				int j = stream.currentOffset;
				stream.writeWordBigEndian(139);
				stream.writeWordBigEndian(150);
				stream.writeWord(32131);
				stream.writeWordBigEndian((int) (Math.random() * 256D));
				stream.writeWord(3250);
				stream.writeWordBigEndian(177);
				stream.writeWord(24859);
				stream.writeWordBigEndian(119);
				if ((int) (Math.random() * 2D) == 0) {
					stream.writeWord(47234);
				}
				if ((int) (Math.random() * 2D) == 0) {
					stream.writeWordBigEndian(21);
				}
				stream.writeBytes(stream.currentOffset - j);
			}
		}
	}

       public void applyVarp(int i) {
                int action = Varp.cache[i].actionType;
		if (action == 0) {
			return;
		}
		int config = variousSettings[i];
		if (action == 1) {
			if (config == 1) {
				Texture.setBrightness(0.90000000000000002D);
			}
			if (config == 2) {
				Texture.setBrightness(0.80000000000000004D);
			}
			if (config == 3) {
				Texture.setBrightness(0.69999999999999996D);
			}
			if (config == 4) {
				Texture.setBrightness(0.59999999999999998D);
			}
            ItemDef.spriteCache.unlinkAll();
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
                                       playSong(volume, false, currentSong);
					previousSong = 0;//TODO temp music
				}
				musicVolume = volume;
			}
		}
		if (action == 4) {
			SoundPlayer.setVolume(config);
			if (config == 0) {
				soundEffectEnabled = true;
				setWaveVolume(0);
			}
			if (config == 1) {
				soundEffectEnabled = true;
				setWaveVolume(-400);
			}
			if (config == 2) {
				soundEffectEnabled = true;
				setWaveVolume(-800);
			}
			if (config == 3) {
				soundEffectEnabled = true;
				setWaveVolume(-1200);
			}
			if (config == 4) {
				soundEffectEnabled = false;
			}
		}
		if (action == 5) {
			oneMouseButtonMode = config;
		}
		if (action == 6) {
			chatEffectsState = config;
		}
		if (action == 8) {
			splitpublicChat = config;
			inputTaken = true;
		}
		if (action == 9) {
			configActionId = config;
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
						for (Item item = (Item) class19.reverseGetFirst(); item != null; item = (Item) class19.reverseGetNext()) {
							ItemDef itemDef = ItemDef.lookup(item.ID);
							long totalValue = Math.max(1, item.amount) * Math.max(1, itemDef.value);
							totalValue = totalValue > 0 ? totalValue : Integer.MAX_VALUE;
							calcEntityScreenPos(k5 * 128 + 64, 20, l5 * 128 + 64);
							// only show ground items names if worth more than x (1k default)
							if (totalValue >= customSettingMinItemValue) {
								int color = Color.WHITE.hashCode();
								if (totalValue >= 1e5) {
									color = Color.GREEN.hashCode();
								} else if (totalValue >= 1e4) {
									color = Color.CYAN.hashCode();
								} else if (totalValue >= 1e3) {
									color = Color.YELLOW.hashCode();
								}
								String text = "";
								if (item.amount > 1) {
									DecimalFormatSymbols separator = new DecimalFormatSymbols();
									separator.setGroupingSeparator(',');
									DecimalFormat formatter = new DecimalFormat("#,###,###,###", separator);
									text += formatter.format(item.amount) + " x ";
								}
								text += itemDef.name + " (" +  intToShortLetter(totalValue) + " gp)";
								plainFont.textLeft(color, text, spriteDrawY - offset, spriteDrawX - (plainFont.getTextWidth(text) / 2));
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
                                EntityDef entityDef = ((NPC) obj).definition;
					if (entityDef.childrenIDs != null) {
                                        entityDef = entityDef.transform();
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
							npcScreenPos(((Entity) obj), ((Entity) obj).height + 15);
							// ItemDef.getSprite(995, 1000, 0xffff00).drawTransparentSprite(spriteDrawX - 16, spriteDrawY - l);
							plainFont.textCenter(0x00ffff, "[SHOP]", spriteDrawY - 5, spriteDrawX);
						}
					} else if (customSettingVisiblePlayerNames) {
						// Show player names
						npcScreenPos(((Entity) obj), ((Entity) obj).height + 15);
						plainFont.textCenter(0xffffff, player.name, spriteDrawY - 5, spriteDrawX);
						if (player.privelage >= 1) {
							npcScreenPos(((Entity) obj), ((Entity) obj).height + 15);
							int icon = Math.max(0, Math.min(1, player.privelage - 1));
							modIcons[icon].draw( spriteDrawX - player.name.length() * 3 - 16, spriteDrawY - 7);
						}
					}
					if (player.headIcon >= 0) {
						npcScreenPos(((Entity) obj), ((Entity) obj).height + 15);
						if (spriteDrawX > -1) {
							if (player.skullIcon < 2) {
								skullIcons[player.skullIcon].drawTransparentSprite(spriteDrawX - 12, spriteDrawY - l);
								l += 25;
							}
							if (player.headIcon < 7) {
								headIcons[player.headIcon].drawTransparentSprite(spriteDrawX - 12, spriteDrawY - l);
								l += 18;
							}
						}
					}
					if (j >= 0 && hintIconState == 10 && selectedPlayerId == playerIndices[j]) {
						npcScreenPos(((Entity) obj), ((Entity) obj).height + 15);
						if (spriteDrawX > -1) {
							headIconsHint[1].drawTransparentSprite(spriteDrawX - 12, spriteDrawY - l);
						}
					}
				} else {
                                EntityDef entityDef_1 = ((NPC) obj).definition;
                                        if (entityDef_1.headIcon >= 0 && entityDef_1.headIcon < headIcons.length) {
						npcScreenPos(((Entity) obj), ((Entity) obj).height + 15);
						if (spriteDrawX > -1) {
                                                        headIcons[entityDef_1.headIcon].drawTransparentSprite(spriteDrawX - 12, spriteDrawY - 30);
						}
					}
					if (hintIconState == 1 && hintNpcIndex == npcIndices[j - playerCount] && loopCycle % 20 < 10) {
						npcScreenPos(((Entity) obj), ((Entity) obj).height + 15);
						if (spriteDrawX > -1) {
							headIconsHint[0].drawTransparentSprite(spriteDrawX - 12, spriteDrawY - 28);
						}
					}
				}
				// Chat messages sent
				if (((Entity) obj).textSpoken != null && (j >= playerCount || publicChatMode == 0 || publicChatMode == 3 || publicChatMode == 1 && isFriendOrSelf(((Player) obj).name))) {
					npcScreenPos(((Entity) obj), ((Entity) obj).height);
					if (spriteDrawX > -1 && anInt974 < maxDisplayedText) {
                                                textWidth[anInt974] = chatTextDrawingArea.measurePlainTextWidth(((Entity) obj).textSpoken) / 2;
                                                textHeight[anInt974] = chatTextDrawingArea.fontHeight;
						textX[anInt974] = spriteDrawX;
						textY[anInt974] = spriteDrawY;
						textColors[anInt974] = ((Entity) obj).chatColor;
						textEffects[anInt974] = ((Entity) obj).chatEffect;
						textCycles[anInt974] = ((Entity) obj).textCycle;
						overheadTexts[anInt974++] = ((Entity) obj).textSpoken;
						if (chatEffectsState == 0 && ((Entity) obj).chatEffect >= 1 && ((Entity) obj).chatEffect <= 3) {
							textHeight[anInt974] += 10;
							textY[anInt974] += 5;
						}
						if (chatEffectsState == 0 && ((Entity) obj).chatEffect == 4) {
							textWidth[anInt974] = 60;
						}
						if (chatEffectsState == 0 && ((Entity) obj).chatEffect == 5) {
							textHeight[anInt974] += 5;
						}
					}
				}
				// HP markers for player?
				if (((Entity) obj).loopCycleStatus > loopCycle) {
					try {
						npcScreenPos(((Entity) obj), ((Entity) obj).height + 15);
						if (spriteDrawX > -1) {
							int i1 = ((Entity) obj).currentHealth * 30 / ((Entity) obj).maxHealth;
							if (i1 > 30) {
								i1 = 30;
							}
							DrawingArea.fillArea(5, spriteDrawY - 3, 0x00ff00, i1, spriteDrawX - 15);
							DrawingArea.fillArea(5, spriteDrawY - 3, 0xff0000, 30 - i1, spriteDrawX - 15 + i1);
						}
					} catch (Exception e) {
					}
				}
				// Hit markers
				for (int j1 = 0; j1 < 4; j1++) {
					if (((Entity) obj).hitsLoopCycle[j1] > loopCycle) {
						npcScreenPos(((Entity) obj), ((Entity) obj).height / 2);
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
							hitMarks[((Entity) obj).hitMarkTypes[j1]].drawTransparentSprite(spriteDrawX - 12, spriteDrawY - 12);
							plainFont.textCenter(0, String.valueOf(((Entity) obj).hitArray[j1]), spriteDrawY + 4, spriteDrawX);
							plainFont.textCenter(0xffffff, String.valueOf(((Entity) obj).hitArray[j1]), spriteDrawY + 3, spriteDrawX - 1);
						}
					}
				}
			}
			// Hit markers
			for (int k = 0; k < anInt974; k++) {
				int k1 = textX[k];
				int l1 = textY[k];
				int j2 = textWidth[k];
				int k2 = textHeight[k];
				boolean flag = true;
				while (flag) {
					flag = false;
					for (int l2 = 0; l2 < k; l2++) {
						if (l1 + 2 > textY[l2] - textHeight[l2] && l1 - k2 < textY[l2] + 2 && k1 - j2 < textX[l2] + textWidth[l2] && k1 + j2 > textX[l2] - textWidth[l2] && textY[l2] - textHeight[l2] < l1) {
							l1 = textY[l2] - textHeight[l2];
							flag = true;
						}
					}

				}
				spriteDrawX = textX[k];
				spriteDrawY = textY[k] = l1;
				String s = overheadTexts[k];
				if (chatEffectsState == 0) {
					int i3 = 0xffff00;
					if (textColors[k] < 6) {
						i3 = hitmarkColors[textColors[k]];
					}
					if (textColors[k] == 6) {
						i3 = waveCycle % 20 >= 10 ? 0xffff00 : 0xff0000;
					}
					if (textColors[k] == 7) {
						i3 = waveCycle % 20 >= 10 ? 0x00ffff : 255;
					}
					if (textColors[k] == 8) {
						i3 = waveCycle % 20 >= 10 ? 0x80ff80 : 45056;
					}
					if (textColors[k] == 9) {
						int j3 = 150 - textCycles[k];
						if (j3 < 50) {
							i3 = 0xff0000 + 1280 * j3;
						} else if (j3 < 100) {
							i3 = 0xffff00 - 0x50000 * (j3 - 50);
						} else if (j3 < 150) {
							i3 = 0x00ff00 + 5 * (j3 - 100);
						}
					}
					if (textColors[k] == 10) {
						int k3 = 150 - textCycles[k];
						if (k3 < 50) {
							i3 = 0xff0000 + 5 * k3;
						} else if (k3 < 100) {
							i3 = 0xff00ff - 0x50000 * (k3 - 50);
						} else if (k3 < 150) {
							i3 = 255 + 0x50000 * (k3 - 100) - 5 * (k3 - 100);
						}
					}
					if (textColors[k] == 11) {
						int l3 = 150 - textCycles[k];
						if (l3 < 50) {
							i3 = 0xffffff - 0x50005 * l3;
						} else if (l3 < 100) {
							i3 = 0x00ff00 + 0x50005 * (l3 - 50);
						} else if (l3 < 150) {
							i3 = 0xffffff - 0x50000 * (l3 - 100);
						}
					}
					if (textEffects[k] == 0) {
						chatTextDrawingArea.textCenter(0, s, spriteDrawY + 1, spriteDrawX);
						chatTextDrawingArea.textCenter(i3, s, spriteDrawY, spriteDrawX);
					}
					if (textEffects[k] == 1) {
                                                chatTextDrawingArea.drawWavyCenteredText(0, s, spriteDrawX, waveCycle, spriteDrawY + 1);
                                                chatTextDrawingArea.drawWavyCenteredText(i3, s, spriteDrawX, waveCycle, spriteDrawY);
					}
					if (textEffects[k] == 2) {
                                                chatTextDrawingArea.drawWavyText(spriteDrawX, s, waveCycle, spriteDrawY + 1, 0);
                                                chatTextDrawingArea.drawWavyText(spriteDrawX, s, waveCycle, spriteDrawY, i3);
					}
					if (textEffects[k] == 3) {
                                                chatTextDrawingArea.drawShakeText(150 - textCycles[k], s, waveCycle, spriteDrawY + 1, spriteDrawX, 0);
                                                chatTextDrawingArea.drawShakeText(150 - textCycles[k], s, waveCycle, spriteDrawY, spriteDrawX, i3);
					}
					if (textEffects[k] == 4) {
                                                int i4 = chatTextDrawingArea.measurePlainTextWidth(s);
						int k4 = (150 - textCycles[k]) * (i4 + 100) / 150;
						DrawingArea.setDrawingArea(334, spriteDrawX - 50, spriteDrawX + 50, 0);
						chatTextDrawingArea.textLeft(0, s, spriteDrawY + 1, spriteDrawX + 50 - k4);
						chatTextDrawingArea.textLeft(i3, s, spriteDrawY, spriteDrawX + 50 - k4);
						DrawingArea.defaultDrawingAreaSize();
					}
					if (textEffects[k] == 5) {
						int j4 = 150 - textCycles[k];
						int l4 = 0;
						if (j4 < 25) {
							l4 = j4 - 25;
						} else if (j4 > 125) {
							l4 = j4 - 125;
						}
                                                DrawingArea.setDrawingArea(spriteDrawY + 5, 0, 512, spriteDrawY - chatTextDrawingArea.fontHeight - 1);
						chatTextDrawingArea.textCenter(0, s, spriteDrawY + 1 + l4, spriteDrawX);
						chatTextDrawingArea.textCenter(i3, s, spriteDrawY + l4, spriteDrawX);
						DrawingArea.defaultDrawingAreaSize();
					}
				} else {
					chatTextDrawingArea.textCenter(0, s, spriteDrawY + 1, spriteDrawX);
					chatTextDrawingArea.textCenter(0xffff00, s, spriteDrawY, spriteDrawX);
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

				stream.createFrame(215);
				stream.writeQWord(l);
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
		buttonLeft.drawTransparentSprite(x, y);
		while ((curWidth + 26) < width) {
			buttonRight.drawTransparentSprite(x + curWidth, y);
			curWidth += 26;
		}
		buttonRight.drawTransparentSprite(x + width - 30, y);
	}

	public void drawCheckbox(boolean enabled, int x, int y) {
		StreamLoader streamLoader_2 = streamLoaderForName(4, "2d graphics", "media", expectedCRCs[4], 40);
		Sprite checkboxUnchecked = new Sprite(streamLoader_2, "miscgraphics", 10);
		Sprite checkboxChecked = new Sprite(streamLoader_2, "miscgraphics", 11);
	}

	public void drawTabArea() {
		textBackground.initDrawingArea();
		Texture.lineOffsets = tabAreaOffsets;
		invBack.draw(0, 0);
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
						// buttonLeftDisabled.drawTransparentSprite(centerX - 73, currentY);
						boldFont.textCenterShadow(Color.YELLOW.hashCode(), centerX, "always visible", currentY + textTop, true);
						boldFont.textCenterShadow(Color.YELLOW.hashCode(), centerX, "player names", currentY + textBottom, true);
						
						drawButton(true, centerX - 73, currentY += 40, 146);
						boldFont.textCenterShadow(Color.YELLOW.hashCode(), centerX, "item drops visible", currentY + textTop, true);
						boldFont.textCenterShadow(Color.WHITE.hashCode(), centerX, intToKOrMil(customSettingMinItemValue) + " gp", currentY + textBottom, true);
						
						drawButton(true, centerX - 73, currentY += 40, 146);
						boldFont.textCenterShadow(Color.YELLOW.hashCode(), centerX, "draw distance", currentY + textTop, true);
						boldFont.textCenterShadow(Color.WHITE.hashCode(), centerX, WorldController.drawDistance + " tiles", currentY + textBottom, true);

						drawButton(customSettingShowExperiencePerHour, centerX - 73, currentY += 40, 146);
						boldFont.textCenterShadow(Color.YELLOW.hashCode(), centerX, "show exp info", currentY + textMiddle, true);
						
						drawButton(showInfo, centerX - 73, currentY += 40, 146);
						boldFont.textCenterShadow(Color.YELLOW.hashCode(), centerX, "show debug info", currentY + textMiddle, true);

						drawButton(customSettingVisualFixes, centerX - 73, currentY += 40, 146);
						boldFont.textCenterShadow(Color.YELLOW.hashCode(), centerX, "visual fixes", currentY + textMiddle, true);
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
		textBackground.drawGraphics(205, super.graphics, 553);
		tabAreaBuffer.initDrawingArea();
		Texture.lineOffsets = chatBoxAreaOffsets;
	}

       public void animateTextures(int j) {
		if (!lowMem) {
			if (Texture.textureLastUsed[17] >= j) {
				Background background = Texture.textures[17];
				int k = background.width * background.height - 1;
				int j1 = background.width * animationCycle * 2;
				byte abyte0[] = background.pixels;
				byte abyte3[] = soundPayload;
				for (int i2 = 0; i2 <= k; i2++) {
					abyte3[i2] = abyte0[i2 - j1 & k];
				}

				background.pixels = abyte3;
				soundPayload = abyte0;
				Texture.unloadTexture(17);
			}
			if (Texture.textureLastUsed[24] >= j) {
				Background background_1 = Texture.textures[24];
				int l = background_1.width * background_1.height - 1;
				int k1 = background_1.width * animationCycle * 2;
				byte abyte1[] = background_1.pixels;
				byte abyte4[] = soundPayload;
				for (int j2 = 0; j2 <= l; j2++) {
					abyte4[j2] = abyte1[j2 - k1 & l];
				}

				background_1.pixels = abyte4;
				soundPayload = abyte1;
				Texture.unloadTexture(24);
			}
			if (Texture.textureLastUsed[34] >= j) {
				Background background_2 = Texture.textures[34];
				int i1 = background_2.width * background_2.height - 1;
				int l1 = background_2.width * animationCycle * 2;
				byte abyte2[] = background_2.pixels;
				byte abyte5[] = soundPayload;
				for (int k2 = 0; k2 <= i1; k2++) {
					abyte5[k2] = abyte2[k2 - l1 & i1];
				}

				background_2.pixels = abyte5;
				soundPayload = abyte2;
				Texture.unloadTexture(34);
			}
			if (Texture.textureLastUsed[40] >= j) {
				Background background_2 = Texture.textures[40];
				int i1 = background_2.width * background_2.height - 1;
				int l1 = background_2.width * animationCycle * 2;
				byte abyte2[] = background_2.pixels;
				byte abyte5[] = soundPayload;
				for (int k2 = 0; k2 <= i1; k2++) {
					abyte5[k2] = abyte2[k2 - l1 & i1];
				}

				background_2.pixels = abyte5;
				soundPayload = abyte2;
				Texture.unloadTexture(40);
			}
		}
	}

       public void updateEntityText() {
		for (int i = -1; i < playerCount; i++) {
			int j;
			if (i == -1) {
				j = myPlayerIndex;
			} else {
				j = playerIndices[i];
			}
			Player player = playerArray[j];
			if (player != null && player.textCycle > 0) {
				player.textCycle--;
				if (player.textCycle == 0) {
					player.textSpoken = null;
				}
			}
		}

		for (int k = 0; k < npcCount; k++) {
			int l = npcIndices[k];
			NPC npc = npcArray[l];
			if (npc != null && npc.textCycle > 0) {
				npc.textCycle--;
				if (npc.textCycle == 0) {
					npc.textSpoken = null;
				}
			}
		}

	}

	public void calcCameraPos() {
		int i = cameraTargetX * 128 + 64;
		int j = cameraTargetY * 128 + 64;
		int k = getTileHeight(plane, j, i) - cameraTargetZ;
		if (xCameraPos < i) {
			xCameraPos += cameraMoveSpeed + (i - xCameraPos) * cameraMoveAcceleration / 1000;
			if (xCameraPos > i) {
				xCameraPos = i;
			}
		}
		if (xCameraPos > i) {
			xCameraPos -= cameraMoveSpeed + (xCameraPos - i) * cameraMoveAcceleration / 1000;
			if (xCameraPos < i) {
				xCameraPos = i;
			}
		}
		if (zCameraPos < k) {
			zCameraPos += cameraMoveSpeed + (k - zCameraPos) * cameraMoveAcceleration / 1000;
			if (zCameraPos > k) {
				zCameraPos = k;
			}
		}
		if (zCameraPos > k) {
			zCameraPos -= cameraMoveSpeed + (zCameraPos - k) * cameraMoveAcceleration / 1000;
			if (zCameraPos < k) {
				zCameraPos = k;
			}
		}
		if (yCameraPos < j) {
			yCameraPos += cameraMoveSpeed + (j - yCameraPos) * cameraMoveAcceleration / 1000;
			if (yCameraPos > j) {
				yCameraPos = j;
			}
		}
		if (yCameraPos > j) {
			yCameraPos -= cameraMoveSpeed + (yCameraPos - j) * cameraMoveAcceleration / 1000;
			if (yCameraPos < j) {
				yCameraPos = j;
			}
		}
		i = cameraFocusX * 128 + 64;
		j = cameraFocusY * 128 + 64;
		k = getTileHeight(plane, j, i) - cameraFocusHeight;
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
			yCameraCurve += cameraAdjustSpeed + (l1 - yCameraCurve) * cameraAdjustAcceleration / 1000;
			if (yCameraCurve > l1) {
				yCameraCurve = l1;
			}
		}
		if (yCameraCurve > l1) {
			yCameraCurve -= cameraAdjustSpeed + (yCameraCurve - l1) * cameraAdjustAcceleration / 1000;
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
			xCameraCurve += cameraAdjustSpeed + j2 * cameraAdjustAcceleration / 1000;
			xCameraCurve &= 0x7ff;
		}
		if (j2 < 0) {
			xCameraCurve -= cameraAdjustSpeed + -j2 * cameraAdjustAcceleration / 1000;
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
		int menuH = menuHeight;
		int fill = 0x5d5447;

		DrawingArea.fillArea(menuH, yPos, fill, menuW, xPos);
		DrawingArea.fillArea(16, yPos + 1, 0, menuW - 2, xPos + 1);
		DrawingArea.fillPixels(yPos + 18, menuH - 19, 0, xPos + 1, menuW - 2);
		chatTextDrawingArea.textLeft(fill, "Choose Option", yPos + 14, xPos + 3);

		// //Border
		// DrawingArea.drawFrameRounded(yPos + 2, menuH - 4, 250, 0x706a5e, menuW, xPos);
		// DrawingArea.drawFrameRounded(yPos + 1, menuH - 2, 250, 0x706a5e, menuW - 2, xPos + 1);
		// DrawingArea.drawFrameRounded(yPos, menuH, 250, 0x706a5e, menuW - 4, xPos + 2);
		// //Border
		// DrawingArea.drawFrameRounded(yPos + 1, menuH - 2, 250, 0x2d2822, menuW - 6, xPos + 3);
		// DrawingArea.drawFrameRounded(yPos + 2, menuH - 4, 250, 0x2d2822, menuW - 4, xPos + 2);
		// DrawingArea.drawFrameRounded(yPos + 3, menuH - 6, 250, 0x2d2822, menuW - 2, xPos + 1);
		// //Border
		// DrawingArea.drawFrameRounded(yPos + 19, menuH - 22, 250, 0x524a3d, menuW - 4, xPos + 2);
		// DrawingArea.drawFrameRounded(yPos + 20, menuH - 22, 250, 0x524a3d, menuW - 6, xPos + 3);
		// //Menu Fill
		// DrawingArea.fillArea(fill, yPos + 20, menuW - 6, menuH - 23, 170, xPos + 3);
		// //Menu Header
		// DrawingArea.fillArea(0x2a251e, yPos + 2, menuW - 6, 17, 170, xPos + 3);
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
			chatTextDrawingArea.textLeftShadow(true, xPos + 3, colorItem, menuActionName[rowItem], yPosItem);
		}

	}

	public void addFriend(long l) {
		try {
			if (l == 0L) {
				return;
			}
			if (friendsCount >= 100 && friendsListStatus != 1) {
				pushMessage("Your friendlist is full. Max of 100 for free users, and 200 for members", 0, "");
				return;
			}
			if (friendsCount >= 200) {
				pushMessage("Your friendlist is full. Max of 100 for free users, and 200 for members", 0, "");
				return;
			}
			String s = TextClass.fixName(TextClass.nameForLong(l));
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
				stream.createFrame(188);
				stream.writeQWord(l);
				return;
			}
		} catch (RuntimeException runtimeexception) {
			Signlink.reporterror("15283, " + (byte) 68 + ", " + l + ", " + runtimeexception.toString());
		}
		throw new RuntimeException();
	}

        public int getTileHeight(int plane, int worldY, int worldX) {
                int l = worldX >> 7;
                int i1 = worldY >> 7;
                if (l < 0 || i1 < 0 || l > 103 || i1 > 103) {
                        return 0;
                }
                int j1 = plane;
                if (j1 < 3 && (tileFlags[1][l][i1] & 2) == 2) {
                        j1++;
                }
                int k1 = worldX & 0x7f;
                int l1 = worldY & 0x7f;
                int i2 = tileHeights[j1][l][i1] * (128 - k1) + tileHeights[j1][l + 1][i1] * k1 >> 7;
                int j2 = tileHeights[j1][l][i1 + 1] * (128 - k1) + tileHeights[j1][l + 1][i1 + 1] * k1 >> 7;
                return i2 * (128 - l1) + j2 * l1 >> 7;
        }

	public static String intToKOrMil(int j) {
		if (j < 0x186a0) {
			return String.valueOf(j);
		}
		if (j < 0x989680) {
			return j / 1000 + "K";
		} else {
			return j / 0xf4240 + "M";
		}
	}

	public static String intToShortLetter(long number) {
		DecimalFormat nf = new DecimalFormat("0.0");
		double i = number;
		if (i >= 1e9) { // 1B
				return nf.format((i / 1e9)) + "B";
		}
		if (i >= 1e7) { // 1K
				return (int) (i / 1e6) + "M";
		}
		if (i >= 1e6) { // 1M
				return nf.format((i / 1e6)) + "M";
		}
		if (i >= 1e4) { // 1K
				return (int) (i / 1e3) + "K";
		}
		if (i >= 1e3) { // 1K
				return nf.format((i / 1e3)) + "K";
		}
		return "" + number;
	}

	public void resetLogout() {
		try {
			if (socketStream != null) {
				socketStream.close();
			}
		} catch (Exception _ex) {
		}
		socketStream = null;
		loggedIn = false;
		loginScreenState = 0;
		// myUsername = "";
		// myPassword = "";
		unlinkMRUNodes();
		worldController.initToNull();
		for (int i = 0; i < 4; i++) {
			collisionMaps[i].reset();
		}

		System.gc();
		stopMidi();
		currentSong = -1;
		nextSong = -1;
		previousSong = 0;
               queueSong(10, musicVolume, false, 0);
	}

       public void resetCharacterOptions() {
		characterDesignChanged = true;
		for (int j = 0; j < 7; j++) {
			characterStyle[j] = -1;
			for (int k = 0; k < IDK.length; k++) {
                                if (IDK.cache[k].nonSelectable || IDK.cache[k].bodyPartId != j + (isMaleCharacter ? 0 : 7)) {
                                        continue;
                                }
				characterStyle[j] = k;
				break;
			}

		}

	}

       public void addLocalNPCs(int i, Stream stream) {
		while (stream.bitPosition + 21 < i * 8) {
			int k = stream.readBits(14);
			if (k == 16383) {
				break;
			}
			if (npcArray[k] == null) {
				npcArray[k] = new NPC();
			}
			NPC npc = npcArray[k];
			npcIndices[npcCount++] = k;
			npc.lastUpdateCycle = loopCycle;
			int l = stream.readBits(5);
			if (l > 15) {
				l -= 32;
			}
			int i1 = stream.readBits(5);
			if (i1 > 15) {
				i1 -= 32;
			}
			int j1 = stream.readBits(1);
                        npc.definition = EntityDef.forID(stream.readBits(ClientSettings.NPC_BITS));
			int k1 = stream.readBits(1);
			if (k1 == 1) {
				playerUpdateIndices[playerUpdateCount++] = k;
			}
                        npc.size = npc.definition.size;
                       npc.turnSpeed = npc.definition.turnSpeed;
                        npc.walkAnimation = npc.definition.walkAnimation;
                        npc.turn180Animation = npc.definition.turn180Animation;
                        npc.turn90CWAnimation = npc.definition.turn90CWAnimation;
                        npc.turn90CCWAnimation = npc.definition.turn90CCWAnimation;
                        npc.standAnimation = npc.definition.standAnimation;
			npc.setPos(myPlayer.smallX[0] + i1, myPlayer.smallY[0] + l, j1 == 1);
		}
		stream.finishBitAccess();
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
               processMusicQueue();
	}

       public void addPlayersToScene(boolean flag) {
		if (myPlayer.x >> 7 == destX && myPlayer.y >> 7 == destY) {
			destX = 0;
		}
		int j = playerCount;
		if (flag) {
			j = 1;
		}
		for (int l = 0; l < j; l++) {
			Player player;
			int i1;
			if (flag) {
				player = myPlayer;
				i1 = myPlayerIndex << 14;
			} else {
				player = playerArray[playerIndices[l]];
				i1 = playerIndices[l] << 14;
			}
			if (player == null || !player.isVisible()) {
				continue;
			}
			player.aBoolean1699 = (lowMem && playerCount > 50 || playerCount > 200) && !flag && player.currentAnimation == player.standAnimation;
			int j1 = player.x >> 7;
			int k1 = player.y >> 7;
			if (j1 < 0 || j1 >= 104 || k1 < 0 || k1 >= 104) {
				continue;
			}
                        if (player.aModel_1714 != null && loopCycle >= player.animationStartCycle && loopCycle < player.animationEndCycle) {
				player.aBoolean1699 = false;
                                player.animationBaseY = getTileHeight(plane, player.y, player.x);
                               worldController.addAnimatingObject(plane, player.y, player, player.currentHeading, player.anInt1722, player.x, player.animationBaseY, player.anInt1719, player.anInt1721, i1, player.anInt1720);
				continue;
			}
			if ((player.x & 0x7f) == 64 && (player.y & 0x7f) == 64) {
				if (occupiedTiles[j1][k1] == waveCycle) {
					continue;
				}
				occupiedTiles[j1][k1] = waveCycle;
			}
                        player.animationBaseY = getTileHeight(plane, player.y, player.x);
                      worldController.addAnimableObject(plane, player.currentHeading, player.animationBaseY, i1, player.y, 60, player.x, player, player.aBoolean1541);
		}

	}

	public boolean promptUserForInput(RSInterface class9) {
		int j = class9.contentType;
		if (interfaceMode == 2) {
			if (j == 201) {
				inputTaken = true;
				inputDialogState = 0;
				messagePromptRaised = true;
				promptInput = "";
				friendsListAction = 1;
				inputPrompt = "Enter name of friend to add to list";
			}
			if (j == 202) {
				inputTaken = true;
				inputDialogState = 0;
				messagePromptRaised = true;
				promptInput = "";
				friendsListAction = 2;
				inputPrompt = "Enter name of friend to delete from list";
			}
		}
		if (j == 205) {
			reconnectDelay = 250;
			return true;
		}
		if (j == 501) {
			inputTaken = true;
			inputDialogState = 0;
			messagePromptRaised = true;
			promptInput = "";
			friendsListAction = 4;
			inputPrompt = "Enter name of player to add to list";
		}
		if (j == 502) {
			inputTaken = true;
			inputDialogState = 0;
			messagePromptRaised = true;
			promptInput = "";
			friendsListAction = 5;
			inputPrompt = "Enter name of player to delete from list";
		}
		if (j >= 300 && j <= 313) {
			int k = (j - 300) / 2;
			int j1 = j & 1;
			int i2 = characterStyle[k];
			if (i2 != -1) {
				do {
					if (j1 == 0 && --i2 < 0) {
						i2 = IDK.length - 1;
					}
					if (j1 == 1 && ++i2 >= IDK.length) {
						i2 = 0;
					}
                                } while (IDK.cache[i2].nonSelectable || IDK.cache[i2].bodyPartId != k + (isMaleCharacter ? 0 : 7));
				characterStyle[k] = i2;
				characterDesignChanged = true;
			}
		}
		if (j >= 314 && j <= 323) {
			int l = (j - 314) / 2;
			int k1 = j & 1;
			int j2 = characterColorIndices[l];
			if (k1 == 0 && --j2 < 0) {
				j2 = appearanceColorOptions[l].length - 1;
			}
			if (k1 == 1 && ++j2 >= appearanceColorOptions[l].length) {
				j2 = 0;
			}
			characterColorIndices[l] = j2;
			characterDesignChanged = true;
		}
		if (j == 324 && !isMaleCharacter) {
			isMaleCharacter = true;
                   resetCharacterOptions();
		}
		if (j == 325 && isMaleCharacter) {
			isMaleCharacter = false;
                   resetCharacterOptions();
		}
		if (j == 326) {
			stream.createFrame(101);
			stream.writeWordBigEndian(isMaleCharacter ? 0 : 1);
			for (int i1 = 0; i1 < 7; i1++) {
				stream.writeWordBigEndian(characterStyle[i1]);
			}

			for (int l1 = 0; l1 < 5; l1++) {
				stream.writeWordBigEndian(characterColorIndices[l1]);
			}

			return true;
		}
		if (j == 613) {
			canMute = !canMute;
		}
		if (j >= 601 && j <= 612) {
			closeOpenInterfaces();
			if (reportAbuseInput.length() > 0) {
				stream.createFrame(218);
				stream.writeQWord(TextClass.longForName(reportAbuseInput));
				stream.writeWordBigEndian(j - 601);
				stream.writeWordBigEndian(canMute ? 1 : 0);
			}
		}
		return false;
	}

       public void processPlayerUpdateMasks(Stream stream) {
		for (int j = 0; j < playerUpdateCount; j++) {
			int k = playerUpdateIndices[j];
			Player player = playerArray[k];
			int l = stream.readUnsignedByte();
			if ((l & 0x40) != 0) {
				l += stream.readUnsignedByte() << 8;
			}
                        decodePlayerUpdateMask(l, k, stream, player);
		}

	}

       public void drawMinimapLoc(int i, int k, int l, int i1, int j1) {
		int k1 = worldController.getBoundaryObjectUid(j1, l, i);
		if (k1 != 0) {
			int l1 = worldController.getObjectConfig(j1, l, i, k1);
			int k2 = l1 >> 6 & 3;
			int i3 = l1 & 0x1f;
			int k3 = k;
			if (k1 > 0) {
				k3 = i1;
			}
                       int ai[] = minimapImage.pixels;
			int k4 = 24624 + l * 4 + (103 - i) * 512 * 4;
			int i5 = k1 >> 14 & 0x7fff;
			ObjectDef class46_2 = ObjectDef.forID(i5);
                        if (class46_2.mapSceneId != -1) {
                                Background background_2 = mapScenes[class46_2.mapSceneId];
				if (background_2 != null) {
					int i6 = (class46_2.sizeX * 4 - background_2.width) / 2;
					int j6 = (class46_2.sizeY * 4 - background_2.height) / 2;
					background_2.draw(48 + l * 4 + i6, 48 + (104 - i - class46_2.sizeY) * 4 + j6);
				}
			} else {
				if (i3 == 0 || i3 == 2) {
					if (k2 == 0) {
						ai[k4] = k3;
						ai[k4 + 512] = k3;
						ai[k4 + 1024] = k3;
						ai[k4 + 1536] = k3;
					} else if (k2 == 1) {
						ai[k4] = k3;
						ai[k4 + 1] = k3;
						ai[k4 + 2] = k3;
						ai[k4 + 3] = k3;
					} else if (k2 == 2) {
						ai[k4 + 3] = k3;
						ai[k4 + 3 + 512] = k3;
						ai[k4 + 3 + 1024] = k3;
						ai[k4 + 3 + 1536] = k3;
					} else if (k2 == 3) {
						ai[k4 + 1536] = k3;
						ai[k4 + 1536 + 1] = k3;
						ai[k4 + 1536 + 2] = k3;
						ai[k4 + 1536 + 3] = k3;
					}
				}
				if (i3 == 3) {
					if (k2 == 0) {
						ai[k4] = k3;
					} else if (k2 == 1) {
						ai[k4 + 3] = k3;
					} else if (k2 == 2) {
						ai[k4 + 3 + 1536] = k3;
					} else if (k2 == 3) {
						ai[k4 + 1536] = k3;
					}
				}
				if (i3 == 2) {
					if (k2 == 3) {
						ai[k4] = k3;
						ai[k4 + 512] = k3;
						ai[k4 + 1024] = k3;
						ai[k4 + 1536] = k3;
					} else if (k2 == 0) {
						ai[k4] = k3;
						ai[k4 + 1] = k3;
						ai[k4 + 2] = k3;
						ai[k4 + 3] = k3;
					} else if (k2 == 1) {
						ai[k4 + 3] = k3;
						ai[k4 + 3 + 512] = k3;
						ai[k4 + 3 + 1024] = k3;
						ai[k4 + 3 + 1536] = k3;
					} else if (k2 == 2) {
						ai[k4 + 1536] = k3;
						ai[k4 + 1536 + 1] = k3;
						ai[k4 + 1536 + 2] = k3;
						ai[k4 + 1536 + 3] = k3;
					}
				}
			}
		}
		k1 = worldController.getSceneObjectUid(j1, l, i);
		if (k1 != 0) {
			int i2 = worldController.getObjectConfig(j1, l, i, k1);
			int l2 = i2 >> 6 & 3;
			int j3 = i2 & 0x1f;
			int l3 = k1 >> 14 & 0x7fff;
			ObjectDef class46_1 = ObjectDef.forID(l3);
                        if (class46_1.mapSceneId != -1) {
                                Background background_1 = mapScenes[class46_1.mapSceneId];
				if (background_1 != null) {
					int j5 = (class46_1.sizeX * 4 - background_1.width) / 2;
					int k5 = (class46_1.sizeY * 4 - background_1.height) / 2;
					background_1.draw(48 + l * 4 + j5, 48 + (104 - i - class46_1.sizeY) * 4 + k5);
				}
			} else if (j3 == 9) {
				int l4 = 0xeeeeee;
				if (k1 > 0) {
					l4 = 0xee0000;
				}
                           int ai1[] = minimapImage.pixels;
				int l5 = 24624 + l * 4 + (103 - i) * 512 * 4;
				if (l2 == 0 || l2 == 2) {
					ai1[l5 + 1536] = l4;
					ai1[l5 + 1024 + 1] = l4;
					ai1[l5 + 512 + 2] = l4;
					ai1[l5 + 3] = l4;
				} else {
					ai1[l5] = l4;
					ai1[l5 + 512 + 1] = l4;
					ai1[l5 + 1024 + 2] = l4;
					ai1[l5 + 1536 + 3] = l4;
				}
			}
		}
		k1 = worldController.getTileDecorationUid(j1, l, i);
		if (k1 != 0) {
			int j2 = k1 >> 14 & 0x7fff;
			ObjectDef class46 = ObjectDef.forID(j2);
                        if (class46.mapSceneId != -1) {
                                Background background = mapScenes[class46.mapSceneId];
				if (background != null) {
					int i4 = (class46.sizeX * 4 - background.width) / 2;
					int j4 = (class46.sizeY * 4 - background.height) / 2;
					background.draw(48 + l * 4 + i4, 48 + (104 - i - class46.sizeY) * 4 + j4);
				}
			}
		}
	}

	public void loadTitleScreen() {
		loginBoxBackground = new Background(titleStreamLoader, "titlebox", 0);
		loginButtonBackground = new Background(titleStreamLoader, "titlebutton", 0);
		runeBackgrounds = new Background[12];
		int j = 0;
		try {
			j = Integer.parseInt(getParameter("fl_icon"));
		} catch (Exception _ex) {
		}
		if (j == 0) {
			for (int k = 0; k < 12; k++) {
				runeBackgrounds[k] = new Background(titleStreamLoader, "runes", k);
			}

		} else {
			for (int l = 0; l < 12; l++) {
				runeBackgrounds[l] = new Background(titleStreamLoader, "runes", 12 + (l & 3));
			}

		}
		titleBackgroundLeft = new Sprite(128, 265);
		titleBackgroundRight = new Sprite(128, 265);
               System.arraycopy(titleLeftProducer.pixels, 0, titleBackgroundLeft.pixels, 0, 33920);

               System.arraycopy(titleRightProducer.pixels, 0, titleBackgroundRight.pixels, 0, 33920);

		flamePaletteRed = new int[256];
		for (int k1 = 0; k1 < 64; k1++) {
			flamePaletteRed[k1] = k1 * 0x40000;
		}

		for (int l1 = 0; l1 < 64; l1++) {
			flamePaletteRed[l1 + 64] = 0xff0000 + 1024 * l1;
		}

		for (int i2 = 0; i2 < 64; i2++) {
			flamePaletteRed[i2 + 128] = 0xffff00 + 4 * i2;
		}

		for (int j2 = 0; j2 < 64; j2++) {
			flamePaletteRed[j2 + 192] = 0xffffff;
		}

		flamePaletteGreen = new int[256];
		for (int k2 = 0; k2 < 64; k2++) {
			flamePaletteGreen[k2] = k2 * 1024;
		}

		for (int l2 = 0; l2 < 64; l2++) {
			flamePaletteGreen[l2 + 64] = 0x00ff00 + 4 * l2;
		}

		for (int i3 = 0; i3 < 64; i3++) {
			flamePaletteGreen[i3 + 128] = 0x00ffff + 0x40000 * i3;
		}

		for (int j3 = 0; j3 < 64; j3++) {
			flamePaletteGreen[j3 + 192] = 0xffffff;
		}

		flamePaletteBlue = new int[256];
		for (int k3 = 0; k3 < 64; k3++) {
			flamePaletteBlue[k3] = k3 * 4;
		}

		for (int l3 = 0; l3 < 64; l3++) {
			flamePaletteBlue[l3 + 64] = 255 + 0x40000 * l3;
		}

		for (int i4 = 0; i4 < 64; i4++) {
			flamePaletteBlue[i4 + 128] = 0xff00ff + 1024 * i4;
		}

		for (int j4 = 0; j4 < 64; j4++) {
			flamePaletteBlue[j4 + 192] = 0xffffff;
		}

		flameBuffer = new int[256];
		flameGradient1 = new int[32768];
		flameGradient2 = new int[32768];
		randomizeBackground(null);
		flameBuffer1 = new int[32768];
		flameBuffer2 = new int[32768];
		drawLoadingText(10, "Connecting to fileserver");
		if (!flameThreadActive) {
			drawFlames = true;
			flameThreadActive = true;
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
		if (lowMem && loadingStage == 2 && ObjectManager.currentPlane != plane) {
			drawTextOnScreen(null, "Loading - please wait.");
			loadingStage = 1;
			loadingStartTime = System.currentTimeMillis();
		}
		if (loadingStage == 1) {
			int j = checkMapLoadStatus();
			if (j != 0 && System.currentTimeMillis() - loadingStartTime > 0x57e40L) {
				Signlink.reporterror(myUsername + " glcfb " + serverSessionKey + "," + j + "," + lowMem + "," + decompressors[0] + "," + onDemandFetcher.getNodeCount() + "," + plane + "," + currentRegionX + "," + currentRegionY);
				loadingStartTime = System.currentTimeMillis();
			}
		}
		if (loadingStage == 2 && plane != lastPlane) {
			lastPlane = plane;
                       generateMinimap(plane);
		}
	}

	public int checkMapLoadStatus() {
		for (int i = 0; i < terrainData.length; i++) {
			if (terrainData[i] == null && terrainArchiveIds[i] != -1) {
				return -1;
			}
			if (objectMapData[i] == null && objectArchiveIds[i] != -1) {
				return -2;
			}
		}

		boolean flag = true;
		for (int j = 0; j < terrainData.length; j++) {
			byte abyte0[] = objectMapData[j];
			if (abyte0 != null) {
				int k = (regionBaseIds[j] >> 8) * 64 - baseX;
				int l = (regionBaseIds[j] & 0xff) * 64 - baseY;
				if (isDynamicRegion) {
					k = 10;
					l = 10;
				}
				flag &= ObjectManager.areObjectsReady(k, abyte0, l);
			}
		}

		if (!flag) {
			return -3;
		}
		if (regionLoading) {
			return -4;
		} else {
			loadingStage = 2;
			ObjectManager.currentPlane = plane;
                       constructMapRegion();
			stream.createFrame(121);
			return 0;
		}
	}

       public void processProjectiles() {
       for (Projectile class30_sub2_sub4_sub4 = (Projectile) projectileList.reverseGetFirst(); class30_sub2_sub4_sub4 != null; class30_sub2_sub4_sub4 = (Projectile) projectileList.reverseGetNext()) {
                        if (class30_sub2_sub4_sub4.plane != plane || loopCycle > class30_sub2_sub4_sub4.endCycle) {
                                class30_sub2_sub4_sub4.unlink();
                        } else if (loopCycle >= class30_sub2_sub4_sub4.startCycle) {
                                if (class30_sub2_sub4_sub4.targetIndex > 0) {
                                        NPC npc = npcArray[class30_sub2_sub4_sub4.targetIndex - 1];
                                        if (npc != null && npc.x >= 0 && npc.x < 13312 && npc.y >= 0 && npc.y < 13312) {
                                                class30_sub2_sub4_sub4.track(loopCycle, npc.y, getTileHeight(class30_sub2_sub4_sub4.plane, npc.y, npc.x) - class30_sub2_sub4_sub4.heightOffset, npc.x);
                                        }
                                }
                                if (class30_sub2_sub4_sub4.targetIndex < 0) {
                                        int j = -class30_sub2_sub4_sub4.targetIndex - 1;
                                        Player player;
                                        if (j == localPlayerIndex) {
                                                player = myPlayer;
                                        } else {
                                                player = playerArray[j];
                                        }
                                        if (player != null && player.x >= 0 && player.x < 13312 && player.y >= 0 && player.y < 13312) {
                                                class30_sub2_sub4_sub4.track(loopCycle, player.y, getTileHeight(class30_sub2_sub4_sub4.plane, player.y, player.x) - class30_sub2_sub4_sub4.heightOffset, player.x);
                                        }
                                }
                                class30_sub2_sub4_sub4.update(animationCycle);
                               worldController.addAnimableObject(plane, class30_sub2_sub4_sub4.yaw, (int) class30_sub2_sub4_sub4.currentHeight, -1, (int) class30_sub2_sub4_sub4.currentY, 60, (int) class30_sub2_sub4_sub4.currentX, class30_sub2_sub4_sub4, false);
                        }
                }

	}

	public AppletContext getAppletContext() {
		if (Signlink.mainapp != null) {
			return Signlink.mainapp.getAppletContext();
		} else {
			return super.getAppletContext();
		}
	}

	public void drawLogo() {
		byte abyte0[] = titleStreamLoader.getFileData("title.dat");
		Sprite sprite = new Sprite(abyte0, this);
		titleLeftProducer.initDrawingArea();
		sprite.drawSprite(0, 0);
		titleRightProducer.initDrawingArea();
		sprite.drawSprite(-637, 0);
		titleImageProducer.initDrawingArea();
		sprite.drawSprite(-128, 0);
		loginLeftProducer.initDrawingArea();
		sprite.drawSprite(-202, -371);
		loginRightProducer.initDrawingArea();
		sprite.drawSprite(-202, -171);
		titleTopLeftProducer.initDrawingArea();
		sprite.drawSprite(0, -265);
		titleTopRightProducer.initDrawingArea();
		sprite.drawSprite(-562, -265);
		titleBottomLeftProducer.initDrawingArea();
		sprite.drawSprite(-128, -171);
		titleBottomRightProducer.initDrawingArea();
		sprite.drawSprite(-562, -171);
		int ai[] = new int[sprite.width];
		for (int j = 0; j < sprite.height; j++) {
			for (int k = 0; k < sprite.width; k++) {
				ai[k] = sprite.pixels[sprite.width - k - 1 + sprite.width * j];
			}

			System.arraycopy(ai, 0, sprite.pixels, sprite.width * j, sprite.width);

		}

		titleLeftProducer.initDrawingArea();
		sprite.drawSprite(382, 0);
		titleRightProducer.initDrawingArea();
		sprite.drawSprite(-255, 0);
		titleImageProducer.initDrawingArea();
		sprite.drawSprite(254, 0);
		loginLeftProducer.initDrawingArea();
		sprite.drawSprite(180, -371);
		loginRightProducer.initDrawingArea();
		sprite.drawSprite(180, -171);
		titleTopLeftProducer.initDrawingArea();
		sprite.drawSprite(382, -265);
		titleTopRightProducer.initDrawingArea();
		sprite.drawSprite(-180, -265);
		titleBottomLeftProducer.initDrawingArea();
		sprite.drawSprite(254, -171);
		titleBottomRightProducer.initDrawingArea();
		sprite.drawSprite(-180, -171);
		sprite = new Sprite(titleStreamLoader, "logo", 0);
		titleImageProducer.initDrawingArea();
		sprite.drawTransparentSprite(382 - sprite.width / 2 - 128, 18);
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
                               if (onDemandData.type == 0) {
                                       Model.loadModelData(onDemandData.data, onDemandData.id);
                                       if ((onDemandFetcher.getModelIndex(onDemandData.id) & 0x62) != 0) {
                                               needDrawTabArea = true;
                                               if (backDialogID != -1) {
                                                       inputTaken = true;
                                               }
                                       }
                               }
                               if (onDemandData.type == 1 && onDemandData.data != null) {
                                       AnimFrame.load(onDemandData.data);
                               }
                               if (onDemandData.type == 2 && onDemandData.id == nextSong && onDemandData.data != null) {
                                       musicData = new byte[onDemandData.data.length];
                                       System.arraycopy(onDemandData.data, 0, musicData, 0, musicData.length);
                                       fetchMusic = true;
                               }
                               if (onDemandData.type == 3 && loadingStage == 1) {
                                       for (int i = 0; i < terrainData.length; i++) {
                                               if (terrainArchiveIds[i] == onDemandData.id) {
                                                       terrainData[i] = onDemandData.data;
                                                       if (onDemandData.data == null) {
                                                               terrainArchiveIds[i] = -1;
                                                       }
                                                       break;
                                               }
                                               if (objectArchiveIds[i] != onDemandData.id) {
                                                       continue;
                                               }
                                               objectMapData[i] = onDemandData.data;
                                               if (onDemandData.data == null) {
                                                       objectArchiveIds[i] = -1;
                                               }
                                               break;
                                       }

                               }
                       } while (onDemandData.type != 93 || !onDemandFetcher.hasLandscape(onDemandData.id));
                       ObjectManager.loadObjectModels(new Stream(onDemandData.data), onDemandFetcher);
		} while (true);
	}

	public void calcFlamesPosition() {
		char c = '\u0100';
		for (int j = 10; j < 117; j++) {
			int k = (int) (Math.random() * 100D);
			if (k < 50) {
				flameBuffer1[j + (c - 2 << 7)] = 255;
			}
		}
		for (int l = 0; l < 100; l++) {
			int i1 = (int) (Math.random() * 124D) + 2;
			int k1 = (int) (Math.random() * 128D) + 128;
			int k2 = i1 + (k1 << 7);
			flameBuffer1[k2] = 192;
		}

		for (int j1 = 1; j1 < c - 1; j1++) {
			for (int l1 = 1; l1 < 127; l1++) {
				int l2 = l1 + (j1 << 7);
				flameBuffer2[l2] = (flameBuffer1[l2 - 1] + flameBuffer1[l2 + 1] + flameBuffer1[l2 - 128] + flameBuffer1[l2 + 128]) / 4;
			}

		}

		flameOffset += 128;
		if (flameOffset > flameGradient1.length) {
			flameOffset -= flameGradient1.length;
			int i2 = (int) (Math.random() * 12D);
			randomizeBackground(runeBackgrounds[i2]);
		}
		for (int j2 = 1; j2 < c - 1; j2++) {
			for (int i3 = 1; i3 < 127; i3++) {
				int k3 = i3 + (j2 << 7);
				int i4 = flameBuffer2[k3 + 128] - flameGradient1[k3 + flameOffset & flameGradient1.length - 1] / 5;
				if (i4 < 0) {
					i4 = 0;
				}
				flameBuffer1[k3] = i4;
			}

		}

		System.arraycopy(flameLineOffsets, 1, flameLineOffsets, 0, c - 1);

		flameLineOffsets[c - 1] = (int) (Math.sin((double) loopCycle / 14D) * 16D + Math.sin((double) loopCycle / 15D) * 14D + Math.sin((double) loopCycle / 16D) * 12D);
		if (flameMainColor > 0) {
			flameMainColor -= 4;
		}
		if (flameSecondaryColor > 0) {
			flameSecondaryColor -= 4;
		}
		if (flameMainColor == 0 && flameSecondaryColor == 0) {
			int l3 = (int) (Math.random() * 2000D);
			if (l3 == 0) {
				flameMainColor = 1024;
			}
			if (l3 == 1) {
				flameSecondaryColor = 1024;
			}
		}
	}

	public boolean saveWave(byte abyte0[], int i) {
		return abyte0 == null || Signlink.wavesave(abyte0, i);
	}

       public void resetInterfaceAnimation(int i) {
		RSInterface class9 = RSInterface.interfaceCache[i];
		if (class9 == null || class9.children == null)
			return;
		for (int element : class9.children) {
			if (element == -1) {
				break;
			}
			RSInterface class9_1 = RSInterface.interfaceCache[element];
			if (class9_1.type == 1) {
                               resetInterfaceAnimation(class9_1.id);
			}
                        class9_1.animationFrame = 0;
                        class9_1.animationCycle = 0;
		}
	}

	public void drawHeadIcon() {
		if (hintIconState != 2) {
			return;
		}
		calcEntityScreenPos((selectedNpcId - baseX << 7) + lastMouseX, destinationY * 2, (destinationX - baseY << 7) + lastMouseY);
		if (spriteDrawX > -1 && loopCycle % 20 < 10) {
			headIconsHint[0].drawTransparentSprite(spriteDrawX - 12, spriteDrawY - 28);
		}
	}

	public void mainGameProcessor() {
		if (systemUpdateTimer > 1) {
			systemUpdateTimer--;
		}
		if (reconnectDelay > 0) {
			reconnectDelay--;
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
		synchronized (mouseDetection.syncObject) {
			if (flagged) {
				if (super.clickMode3 != 0 || mouseDetection.coordsIndex >= 40) {
					stream.createFrame(45);
					stream.writeWordBigEndian(0);
					int j2 = stream.currentOffset;
					int j3 = 0;
					for (int j4 = 0; j4 < mouseDetection.coordsIndex; j4++) {
						if (j2 - stream.currentOffset >= 240) {
							break;
						}
						j3++;
						int l4 = mouseDetection.coordsY[j4];
						if (l4 < 0) {
							l4 = 0;
						} else if (l4 > 502) {
							l4 = 502;
						}
						int k5 = mouseDetection.coordsX[j4];
						if (k5 < 0) {
							k5 = 0;
						} else if (k5 > 764) {
							k5 = 764;
						}
						int i6 = l4 * 765 + k5;
						if (mouseDetection.coordsY[j4] == -1 && mouseDetection.coordsX[j4] == -1) {
							k5 = -1;
							l4 = -1;
							i6 = 0x7ffff;
						}
						if (k5 == lastMousePacketX && l4 == lastMousePacketY) {
							if (mouseIdleTicks < 2047) {
								mouseIdleTicks++;
							}
						} else {
							int j6 = k5 - lastMousePacketX;
							lastMousePacketX = k5;
							int k6 = l4 - lastMousePacketY;
							lastMousePacketY = l4;
							if (mouseIdleTicks < 8 && j6 >= -32 && j6 <= 31 && k6 >= -32 && k6 <= 31) {
								j6 += 32;
								k6 += 32;
								stream.writeWord((mouseIdleTicks << 12) + (j6 << 6) + k6);
								mouseIdleTicks = 0;
							} else if (mouseIdleTicks < 8) {
								stream.writeDWordBigEndian(0x800000 + (mouseIdleTicks << 19) + i6);
								mouseIdleTicks = 0;
							} else {
								stream.writeDWord(0xc0000000 + (mouseIdleTicks << 19) + i6);
								mouseIdleTicks = 0;
							}
						}
					}

					stream.writeBytes(stream.currentOffset - j2);
					if (j3 >= mouseDetection.coordsIndex) {
						mouseDetection.coordsIndex = 0;
					} else {
						mouseDetection.coordsIndex -= j3;
						for (int i5 = 0; i5 < mouseDetection.coordsIndex; i5++) {
							mouseDetection.coordsX[i5] = mouseDetection.coordsX[i5 + j3];
							mouseDetection.coordsY[i5] = mouseDetection.coordsY[i5 + j3];
						}

					}
				}
			} else {
				mouseDetection.coordsIndex = 0;
			}
		}
		if (super.clickMode3 != 0) {
                        long l = (super.lastClickTime - lastMouseClickTime) / 50L;
			if (l > 4095L) {
				l = 4095L;
			}
                        lastMouseClickTime = super.lastClickTime;
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
			stream.createFrame(241);
			stream.writeDWord((l5 << 20) + (j5 << 19) + k4);
		}
		if (cameraUpdateDelay > 0) {
			cameraUpdateDelay--;
		}
		if (super.keyArray[1] == 1 || super.keyArray[2] == 1 || super.keyArray[3] == 1 || super.keyArray[4] == 1) {
			cameraUpdatePending = true;
		}
		if (cameraUpdatePending && cameraUpdateDelay <= 0) {
			cameraUpdateDelay = 20;
			cameraUpdatePending = false;
			stream.createFrame(86);
			stream.writeWord(cameraPitch);
			stream.writeShortA(cameraYaw);
		}
		if (super.awtFocus && !hasFocus) {
			hasFocus = true;
			stream.createFrame(3);
			stream.writeWordBigEndian(1);
		}
		if (!super.awtFocus && hasFocus) {
			hasFocus = false;
			stream.createFrame(3);
			stream.writeWordBigEndian(0);
		}
		loadingStages();
               processPendingSpawns();
               processSoundQueue();
		connectionTimeoutCounter++;
		if (connectionTimeoutCounter > 750) {
			dropClient();
		}
               animatePlayers();
               animateNpcs();
               updateEntityText();
		animationCycle++;
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
			dragCounter++;
			if (super.mouseX > dragStartX + 5 || super.mouseX < dragStartX - 5 || super.mouseY > dragStartY + 5 || super.mouseY < dragStartY - 5) {
				itemBeingDragged = true;
			}
			if (super.clickMode2 == 0) {
				if (activeInterfaceType == 2) {
					needDrawTabArea = true;
				}
				if (activeInterfaceType == 3) {
					inputTaken = true;
				}
				activeInterfaceType = 0;
				if (itemBeingDragged && dragCounter >= 5) {
					lastActiveInvInterface = -1;
					processRightClick();
					if (lastActiveInvInterface == dragInterfaceId && mouseInvInterfaceIndex != draggedSlot) {
						RSInterface class9 = RSInterface.interfaceCache[dragInterfaceId];
						int j1 = 0;
						if (configActionId == 1 && class9.contentType == 206) {
							j1 = 1;
						}
						if (class9.inv[mouseInvInterfaceIndex] <= 0) {
							j1 = 0;
						}
						if (class9.aBoolean235) {
							int l2 = draggedSlot;
							int l3 = mouseInvInterfaceIndex;
							class9.inv[l3] = class9.inv[l2];
							class9.invStackSizes[l3] = class9.invStackSizes[l2];
							class9.inv[l2] = -1;
							class9.invStackSizes[l2] = 0;
						} else if (j1 == 1) {
							int i3 = draggedSlot;
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
							class9.swapInventoryItems(draggedSlot, mouseInvInterfaceIndex);
						}
						stream.createFrame(214);
						stream.writeShortLEA(dragInterfaceId);
                                                stream.writeByteNeg(j1);
						stream.writeShortLEA(draggedSlot);
						stream.writeShortLEDup(mouseInvInterfaceIndex);
					}
				} else if ((oneMouseButtonMode == 1 || menuHasAddFriend(menuActionRow - 1)) && menuActionRow > 2) {
					determineMenuSize();
				} else if (menuActionRow > 0) {
					doAction(menuActionRow - 1);
				}
				atInventoryLoopCycle = 10;
				super.clickMode3 = 0;
			}
		}
                if (WorldController.clickedTileX != -1) {
                        int k = WorldController.clickedTileX;
                        int k1 = WorldController.clickedTileY;
			boolean flag = doWalkTo(0, 0, 0, 0, myPlayer.smallY[0], 0, 0, k1, myPlayer.smallX[0], true, k);
                        WorldController.clickedTileX = -1;
                        WorldController.clickedTileY = -1;
			if (flag) {
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 1;
				crossIndex = 0;
			}
		}
		if (super.clickMode3 == 1 && messagePrompt != null) {
			messagePrompt = null;
			inputTaken = true;
			super.clickMode3 = 0;
		}
		if (!processMenuClick()) {
			processMainScreenClick();
			processTabClick();
			processChatModeClick();
		}
		if (super.clickMode2 == 1 || super.clickMode3 == 1) {
			clickCycle++;
		}
               if (loadingStage == 2) {
                        updateCameraPosition();
               }
                if (loadingStage == 2 && isCameraLocked) {
                        calcCameraPos();
                }
		for (int i1 = 0; i1 < 5; i1++) {
			cameraShakeCycle[i1]++;
		}

               processInput();
		super.idleTime++; //Idle timer nope!
		/*if (super.idleTime > 12000) {
			reconnectDelay = 250;
			super.idleTime -= 500;
			stream.createFrame(202);
		}*/
		idleCycleCounter++;
		if (idleCycleCounter > 500) {
			idleCycleCounter = 0;
			int l1 = (int) (Math.random() * 8D);
			if ((l1 & 1) == 1) {
				cameraXOffset += cameraXOffsetSpeed;
			}
			if ((l1 & 2) == 2) {
				cameraYOffset += cameraYOffsetSpeed;
			}
			if ((l1 & 4) == 4) {
				cameraYawOffset += cameraYawOffsetSpeed;
			}
		}
		if (cameraXOffset < -50) {
			cameraXOffsetSpeed = 2;
		}
		if (cameraXOffset > 50) {
			cameraXOffsetSpeed = -2;
		}
		if (cameraYOffset < -55) {
			cameraYOffsetSpeed = 2;
		}
		if (cameraYOffset > 55) {
			cameraYOffsetSpeed = -2;
		}
		if (cameraYawOffset < -40) {
			cameraYawOffsetSpeed = 1;
		}
		if (cameraYawOffset > 40) {
			cameraYawOffsetSpeed = -1;
		}
		minimapRandomTimer++;
		if (minimapRandomTimer > 500) {
			minimapRandomTimer = 0;
			int i2 = (int) (Math.random() * 8D);
			if ((i2 & 1) == 1) {
				minimapRotationOffset += minimapHorizontalSpeed;
			}
			if ((i2 & 2) == 2) {
				minimapZoom += minimapVerticalSpeed;
			}
		}
		if (minimapRotationOffset < -60) {
			minimapHorizontalSpeed = 2;
		}
		if (minimapRotationOffset > 60) {
			minimapHorizontalSpeed = -2;
		}
		if (minimapZoom < -20) {
			minimapVerticalSpeed = 1;
		}
		if (minimapZoom > 10) {
			minimapVerticalSpeed = -1;
		}
		keepAliveCounter++;
		if (keepAliveCounter > 50) {
			stream.createFrame(0);
		}
		try {
			if (socketStream != null && stream.currentOffset > 0) {
				socketStream.queueBytes(stream.currentOffset, stream.buffer);
				stream.currentOffset = 0;
				keepAliveCounter = 0;
			}
		} catch (IOException _ex) {
			dropClient();
		} catch (Exception exception) {
			resetLogout();
		}
	}

       public void locatePendingSpawns() {
               PendingSpawn class30_sub1 = (PendingSpawn) pendingSpawns.reverseGetFirst();
               for (; class30_sub1 != null; class30_sub1 = (PendingSpawn) pendingSpawns.reverseGetNext()) {
                       if (class30_sub1.delay == -1) {
                               class30_sub1.spawnDelay = 0;
                               locateSceneObject(class30_sub1);
                       } else {
                               class30_sub1.unlink();
                       }
               }

       }

	void drawLoadingText(int i, String s) {
		loadingPercent = i;
		errorMessage = s;
		resetImageProducers();
		if (titleStreamLoader == null) {
			super.drawLoadingText(i, s);
			return;
		}
		loginRightProducer.initDrawingArea();
		char c = '\u0168';
		char c1 = '\310';
		byte byte1 = 20;
		chatTextDrawingArea.textCenter(0xffffff, "" + ClientSettings.SERVER_NAME + " is loading - please wait...", c1 / 2 - 26 - byte1, c / 2);
		int j = c1 / 2 - 18 - byte1;
		DrawingArea.fillPixels(j, 34, 0x8c1111, c / 2 - 152, 304);
		DrawingArea.fillPixels(j + 1, 32, 0, c / 2 - 151, 302);
		DrawingArea.fillArea(30, j + 2, 0x8c1111, i * 3, c / 2 - 150);
		DrawingArea.fillArea(30, j + 2, 0, 300 - i * 3, c / 2 - 150 + i * 3);
		chatTextDrawingArea.textCenter(0xffffff, s, c1 / 2 + 5 - byte1, c / 2);
		loginRightProducer.drawGraphics(171, super.graphics, 202);
		if (welcomeScreenRaised) {
			welcomeScreenRaised = false;
			if (!flameThreadActive) {
				titleLeftProducer.drawGraphics(0, super.graphics, 0);
				titleRightProducer.drawGraphics(0, super.graphics, 637);
			}
			titleImageProducer.drawGraphics(0, super.graphics, 128);
			loginLeftProducer.drawGraphics(371, super.graphics, 202);
			titleTopLeftProducer.drawGraphics(265, super.graphics, 0);
			titleTopRightProducer.drawGraphics(265, super.graphics, 562);
			titleBottomLeftProducer.drawGraphics(171, super.graphics, 128);
			titleBottomRightProducer.drawGraphics(171, super.graphics, 562);
		}
	}

       public void handleScrollbarInput(int i, int j, int k, int l, RSInterface class9, int i1, boolean flag, int j1) {
		int scrollPadding;
		if (scrollBarDragging) {
			scrollPadding = 32;
		} else {
			scrollPadding = 0;
		}
		scrollBarDragging = false;
		if (k >= i && k < i + 16 && l >= i1 && l < i1 + 16) {
			class9.scrollPosition -= clickCycle * 4;
			if (flag) {
				needDrawTabArea = true;
			}
		} else if (k >= i && k < i + 16 && l >= i1 + j - 16 && l < i1 + j) {
			class9.scrollPosition += clickCycle * 4;
			if (flag) {
				needDrawTabArea = true;
			}
		} else if (k >= i - scrollPadding && k < i + 16 + scrollPadding && l >= i1 + 16 && l < i1 + j - 16 && clickCycle > 0) {
			int l1 = (j - 32) * j / j1;
			if (l1 < 8) {
				l1 = 8;
			}
			int i2 = l - i1 - 16 - l1 / 2;
			int j2 = j - 32 - l1;
			class9.scrollPosition = (j1 - j) * i2 / j2;
			if (flag) {
				needDrawTabArea = true;
			}
			scrollBarDragging = true;
		}
	}

	public boolean walkToObject(int i, int j, int k) {
		int i1 = i >> 14 & 0x7fff;
		int j1 = worldController.getObjectConfig(plane, k, j, i);
		if (j1 == -1) {
			return false;
		}
		int k1 = j1 & 0x1f;
		int l1 = j1 >> 6 & 3;
		if (k1 == 10 || k1 == 11 || k1 == 22) {
			ObjectDef class46 = ObjectDef.forID(i1);
			int i2;
			int j2;
			if (l1 == 0 || l1 == 2) {
				i2 = class46.sizeX;
				j2 = class46.sizeY;
			} else {
				i2 = class46.sizeY;
				j2 = class46.sizeX;
			}
			int k2 = class46.anInt768;
			if (l1 != 0) {
				k2 = (k2 << l1 & 0xf) + (k2 >> 4 - l1);
			}
			doWalkTo(2, 0, j2, 0, myPlayer.smallY[0], i2, k2, j, myPlayer.smallX[0], false, k);
		} else {
			doWalkTo(2, l1, 0, k1 + 1, myPlayer.smallY[0], 0, 0, j, myPlayer.smallX[0], false, k);
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
			if (decompressors[0] != null) {
				abyte0 = decompressors[0].decompress(i);
			}
		} catch (Exception _ex) {
		}
		if(abyte0 != null && ClientSettings.CHECK_CRC) {
			fileCRC.reset();
			fileCRC.update(abyte0);
			int i1 = (int)fileCRC.getValue();
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
				int i2 = stream.read3Bytes() + 6;
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
					if (decompressors[0] != null) {
                                                decompressors[0].writeEntry(abyte0.length, abyte0, i);
					}
				} catch (Exception _ex) {
					decompressors[0] = null;
				}

				if(abyte0 != null && ClientSettings.CHECK_CRC)
				{
					fileCRC.reset();
					fileCRC.update(abyte0);
					int i3 = (int)fileCRC.getValue();
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
				useJaggrab = !useJaggrab;
			}

		}

		StreamLoader streamLoader_1 = new StreamLoader(abyte0);
		return streamLoader_1;
	}

	public void dropClient() {
		if (reconnectDelay > 0) {
			resetLogout();
			return;
		}
		drawTextOnScreen("Please wait - attempting to reestablish", "Connection lost");
		minimapState = 0;
		destX = 0;
		RSSocket rsSocket = socketStream;
		loggedIn = false;
		loginFailures = 0;
		login(myUsername, myPassword, true);
		if (!loggedIn) {
			resetLogout();
		}
		try {
			rsSocket.close();
		} catch (Exception _ex) {
		}
	}
	
	public void drawTextOnScreen(String s, String s1) {
		if (tabAreaBuffer != null) {
			tabAreaBuffer.initDrawingArea();
			Texture.lineOffsets = chatBoxAreaOffsets;
			int j = 151;
			if (s != null)
				j -= 7;
			boldFont.textCenter(0, s1, j, 257);
			boldFont.textCenter(0xffffff, s1, j - 1, 256);
			j += 15;
			if (s != null) {
				boldFont.textCenter(0, s, j, 257);
				boldFont.textCenter(0xffffff, s, j - 1, 256);
			}
			tabAreaBuffer.drawGraphics(4, super.graphics, 4);
			return;
		}
		if (super.fullGameScreen != null) {
			super.fullGameScreen.initDrawingArea();
			Texture.lineOffsets = gameScreenOffsets;
			int k = 251;
			char c = '\u012C';
			byte byte0 = 50;
			DrawingArea.fillArea(byte0, k - 5 - byte0 / 2, 0, c, 383 - c / 2);
			DrawingArea.fillPixels(k - 5 - byte0 / 2, byte0, 0xffffff, 383 - c / 2, c);
			if (s != null)
				k -= 7;
			boldFont.textCenter(0, s1, k, 383);
			boldFont.textCenter(0xffffff, s1, k - 1, 382);
			k += 15;
			if (s != null) {
				boldFont.textCenter(0, s, k, 383);
				boldFont.textCenter(0xffffff, s, k - 1, 382);
			}
			super.fullGameScreen.drawGraphics(0, super.graphics, 0);
		}
	}

	public void doAction(int i) {
		if (i < 0) {
			return;
		}
		if (inputDialogState != 0) {
			inputDialogState = 0;
			inputTaken = true;
		}
		int j = menuActionCmd2[i];
		int k = menuActionCmd3[i];
		int l = menuActionID[i];
		int i1 = menuActionCmd1[i];
		if (l >= 2000) {
			l -= 2000;
		}
		if (l == 696) {
			cameraYaw = 0;
            cameraPitch = 120;
        }
		if (l == 582) {
			NPC npc = npcArray[i1];
			if (npc != null) {
				doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, npc.smallY[0], myPlayer.smallX[0], false, npc.smallX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				stream.createFrame(57);
				stream.writeShortA(selectedItemId);
				stream.writeShortA(i1);
				stream.writeShortLEDup(selectedItemSlot);
				stream.writeShortA(selectedItemInterfaceId);
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
			stream.createFrame(236);
			stream.writeShortLEDup(k + baseY);
			stream.writeWord(i1);
			stream.writeShortLEDup(j + baseX);
		}
		if (l == 62 && walkToObject(i1, k, j)) {
			stream.createFrame(192);
			stream.writeWord(selectedItemInterfaceId);
			stream.writeShortLEDup(i1 >> 14 & 0x7fff);
			stream.writeShortLEA(k + baseY);
			stream.writeShortLEDup(selectedItemSlot);
			stream.writeShortLEA(j + baseX);
			stream.writeWord(selectedItemId);
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
			stream.createFrame(25);
			stream.writeShortLEDup(selectedItemInterfaceId);
			stream.writeShortA(selectedItemId);
			stream.writeWord(i1);
			stream.writeShortA(k + baseY);
			stream.writeShortLEA(selectedItemSlot);
			stream.writeWord(j + baseX);
		}
		if (l == 74) {
			stream.createFrame(122);
			stream.writeShortLEA(k);
			stream.writeShortA(j);
			stream.writeShortLEDup(i1);
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
				stream.createFrame(185);
				stream.writeWord(k);
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
				actionCounter += i1;
				if (actionCounter >= 90) {
					stream.createFrame(136);
					actionCounter = 0;
				}
				stream.createFrame(128);
				stream.writeWord(i1);
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
				stream.createFrame(155);
				stream.writeShortLEDup(i1);
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
				stream.createFrame(153);
				stream.writeShortLEDup(i1);
			}
		}
		if (l == 516) {
			if (!menuOpen) {
                                worldController.queueClick(super.saveClickY - 4, super.saveClickX - 4);
			} else {
                                worldController.queueClick(k - 4, j - 4);
			}
		}
		if (l == 1062) { //Fifth click
			objectClickCounter += baseX;
			if (objectClickCounter >= 113) {
				stream.createFrame(183);
				stream.writeDWordBigEndian(0xe63271);
				objectClickCounter = 0;
			}
			walkToObject(i1, k, j);
			stream.createFrame(228);
			stream.writeShortA(i1 >> 14 & 0x7fff);
			stream.writeShortA(k + baseY);
			stream.writeWord(j + baseX);
		}
		if (l == 679 && !actionPending) {
			stream.createFrame(40);
			stream.writeWord(k);
			actionPending = true;
		}
		if (l == 431) {
			stream.createFrame(129);
			stream.writeShortA(j);
			stream.writeWord(k);
			stream.writeShortA(i1);
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
				long l3 = TextClass.longForName(s.substring(k1 + 5).trim());
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
			stream.createFrame(135);
			stream.writeShortLEDup(j);
			stream.writeShortA(k);
			stream.writeShortLEDup(i1);
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
			stream.createFrame(16);
			stream.writeShortA(i1);
			stream.writeShortLEA(j);
			stream.writeShortLEA(k);
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
				String s7 = TextClass.fixName(TextClass.nameForLong(TextClass.longForName(s1)));
				boolean flag9 = false;
				for (int j3 = 0; j3 < playerCount; j3++) {
					Player class30_sub2_sub4_sub1_sub2_7 = playerArray[playerIndices[j3]];
					if (class30_sub2_sub4_sub1_sub2_7 == null || class30_sub2_sub4_sub1_sub2_7.name == null || !class30_sub2_sub4_sub1_sub2_7.name.equalsIgnoreCase(s7)) {
						continue;
					}
					doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, class30_sub2_sub4_sub1_sub2_7.smallY[0], myPlayer.smallX[0], false, class30_sub2_sub4_sub1_sub2_7.smallX[0]);
					if (l == 484) {
						stream.createFrame(139);
						stream.writeShortLEDup(playerIndices[j3]);
					}
					if (l == 6) {
						actionCounter += i1;
						if (actionCounter >= 90) {
							stream.createFrame(136);
							actionCounter = 0;
						}
						stream.createFrame(128);
						stream.writeWord(playerIndices[j3]);
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
			stream.createFrame(53);
			stream.writeWord(j);
			stream.writeShortA(selectedItemSlot);
			stream.writeShortLEA(i1);
			stream.writeWord(selectedItemInterfaceId);
			stream.writeShortLEDup(selectedItemId);
			stream.writeWord(k);
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
			stream.createFrame(87);
			stream.writeShortA(i1);
			stream.writeWord(k);
			stream.writeShortA(j);
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
			selectedSpellId = k;
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
			stream.createFrame(117);
			stream.writeShortLEA(k);
			stream.writeShortLEA(i1);
			stream.writeShortLEDup(j);
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
				playerOptionCounter += i1;
				if (playerOptionCounter >= 54) {
					stream.createFrame(189);
					stream.writeWordBigEndian(234);
					playerOptionCounter = 0;
				}
				stream.createFrame(73);
				stream.writeShortLEDup(i1);
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
			stream.createFrame(79);
			stream.writeShortLEDup(k + baseY);
			stream.writeWord(i1);
			stream.writeShortA(j + baseX);
		}
		if (l == 632) {
			stream.createFrame(145);
			stream.writeShortA(k);
			stream.writeShortA(j);
			stream.writeShortA(i1);
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
			stream.createFrame(75);
			stream.writeShortLEA(k);
			stream.writeShortLEDup(j);
			stream.writeShortA(i1);
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
			stream.createFrame(156);
			stream.writeShortA(j + baseX);
			stream.writeShortLEDup(k + baseY);
			stream.writeShortLEA(i1);
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
			stream.createFrame(181);
			stream.writeShortLEDup(k + baseY);
			stream.writeWord(i1);
			stream.writeShortLEDup(j + baseX);
			stream.writeShortA(selectedSpellId);
		}
		if (l == 646) {
			stream.createFrame(185);
			stream.writeWord(k);
			RSInterface class9_2 = RSInterface.interfaceCache[k];
			if (class9_2.valueIndexArray != null && class9_2.valueIndexArray[0][0] == 5) {
				int i2 = class9_2.valueIndexArray[0][1];
				if (variousSettings[i2] != class9_2.requiredValues[0]) {
					variousSettings[i2] = class9_2.requiredValues[0];
                                   applyVarp(i2);
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
				npcClickCounter += i1;
				if (npcClickCounter >= 85) {
					stream.createFrame(230);
					stream.writeWordBigEndian(239);
					npcClickCounter = 0;
				}
				stream.createFrame(17);
				stream.writeShortLEA(i1);
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
				npcAttackCounter++;
				if (npcAttackCounter >= 96) {
					stream.createFrame(152);
					stream.writeWordBigEndian(88);
					npcAttackCounter = 0;
				}
				stream.createFrame(21);
				stream.writeWord(i1);
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
				stream.createFrame(131);
				stream.writeShortLEA(i1);
				stream.writeShortA(selectedSpellId);
			}
		}
		if (l == 200) {
			closeOpenInterfaces();
		}
		if (l == 1025) {
			NPC class30_sub2_sub4_sub1_sub1_5 = npcArray[i1];
			if (class30_sub2_sub4_sub1_sub1_5 != null) {
                                EntityDef entityDef = class30_sub2_sub4_sub1_sub1_5.definition;
                                if (entityDef.childrenIDs != null) {
                                        entityDef = entityDef.transform();
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
			walkToObject(i1, k, j);
			stream.createFrame(252);
			stream.writeShortLEA(i1 >> 14 & 0x7fff);
			stream.writeShortLEDup(k + baseY);
			stream.writeShortA(j + baseX);
		}
		if (l == 412) {
			NPC class30_sub2_sub4_sub1_sub1_6 = npcArray[i1];
			if (class30_sub2_sub4_sub1_sub1_6 != null) {
				doWalkTo(2, 0, 1, 0, myPlayer.smallY[0], 1, 0, class30_sub2_sub4_sub1_sub1_6.smallY[0], myPlayer.smallX[0], false, class30_sub2_sub4_sub1_sub1_6.smallX[0]);
				crossX = super.saveClickX;
				crossY = super.saveClickY;
				crossType = 2;
				crossIndex = 0;
				stream.createFrame(72);
				stream.writeShortA(i1);
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
				stream.createFrame(249);
				stream.writeShortA(i1);
				stream.writeShortLEDup(selectedSpellId);
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
				stream.createFrame(39);
				stream.writeShortLEDup(i1);
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
				stream.createFrame(139);
				stream.writeShortLEDup(i1);
			}
		}
		if (l == 956 && walkToObject(i1, k, j)) {
			stream.createFrame(35);
			stream.writeShortLEDup(j + baseX);
			stream.writeShortA(selectedSpellId);
			stream.writeShortA(k + baseY);
			stream.writeShortLEDup(i1 >> 14 & 0x7fff);
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
			stream.createFrame(23);
			stream.writeShortLEDup(k + baseY);
			stream.writeShortLEDup(i1);
			stream.writeShortLEDup(j + baseX);
		}
		if (l == 867) {
			if ((i1 & 3) == 0) {
				itemUseCounter++;
			}
			if (itemUseCounter >= 59) {
				stream.createFrame(200);
				stream.writeWord(25501);
				itemUseCounter = 0;
			}
			stream.createFrame(43);
			stream.writeShortLEDup(k);
			stream.writeShortA(i1);
			stream.writeShortA(j);
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
			stream.createFrame(237);
			stream.writeWord(j);
			stream.writeShortA(i1);
			stream.writeWord(k);
			stream.writeShortA(selectedSpellId);
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
				stream.createFrame(14);
				// stream.writeShortA(selectedItemInterfaceId);
				stream.writeWord(i1);
				// stream.writeWord(selectedItemId);
				stream.writeShortLEDup(selectedItemSlot);
			}
		}
		if (l == 639) {
			String s3 = menuActionName[i];
			int k2 = s3.indexOf("@whi@");
			if (k2 != -1) {
				long l4 = TextClass.longForName(s3.substring(k2 + 5).trim());
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
					privateMessageRecipient = friendsListAsLongs[k3];
					inputPrompt = "Enter message to send to " + friendsList[k3];
				} else {
					pushMessage("That player is currently offline.", 0, "");
				}
			}
		}
		if (l == 454) {
			stream.createFrame(41);
			stream.writeWord(i1);
			stream.writeShortA(j);
			stream.writeShortA(k);
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
					npcInteractionCounter++;
				}
				if (npcInteractionCounter >= 53) {
					stream.createFrame(85);
					stream.writeWordBigEndian(66);
					npcInteractionCounter = 0;
				}
				stream.createFrame(18);
				stream.writeShortLEDup(i1);
			}
		}
		if (l == 113) {//third
			walkToObject(i1, k, j);
			stream.createFrame(70);
			stream.writeShortLEDup(j + baseX);
			stream.writeWord(k + baseY);
			stream.writeShortLEA(i1 >> 14 & 0x7fff);
		}
		if (l == 872) {//fourth
			walkToObject(i1, k, j);
			stream.createFrame(234);
			stream.writeShortLEA(j + baseX);
			stream.writeShortA(i1 >> 14 & 0x7fff);
			stream.writeShortLEA(k + baseY);
		}
		if (l == 502) {//first
			walkToObject(i1, k, j);
			stream.createFrame(132);
			stream.writeShortLEA(j + baseX);
			stream.writeWord(i1 >> 14 & 0x7fff);
			stream.writeShortA(k + baseY);
		}
		if (l == 1125) {
			ItemDef itemDef = ItemDef.lookup(i1);
			RSInterface class9_4 = RSInterface.interfaceCache[k];
			if (class9_4 != null && class9_4.invStackSizes[j] >= 1e5) {
				DecimalFormatSymbols separator = new DecimalFormatSymbols();
				separator.setGroupingSeparator(',');
				DecimalFormat formatter = new DecimalFormat("#,###,###,###", separator);
				pushMessage(formatter.format(class9_4.invStackSizes[j]) + " x " + itemDef.name, 0, "");
			}
			if (itemDef.description != null) {
				pushMessage(new String(itemDef.description) + " (" + intToKOrMil(itemDef.value) + "gp ea)", 0, "");
			} else {
				pushMessage("It's a " + itemDef.name + ".", 0, "");
			}
		}
		if (l == 169) {
			stream.createFrame(185);
			stream.writeWord(k);
			RSInterface class9_3 = RSInterface.interfaceCache[k];
			if (class9_3.valueIndexArray != null && class9_3.valueIndexArray[0][0] == 5) {
				int l2 = class9_3.valueIndexArray[0][1];
				variousSettings[l2] = 1 - variousSettings[l2];
                           applyVarp(l2);
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
			selectedItemSlot = j;
			selectedItemInterfaceId = k;
			selectedItemId = i1;
			selectedItemName = ItemDef.lookup(i1).name;
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
			stream.createFrame(253);
			stream.writeShortLEDup(j + baseX);
			stream.writeShortLEA(k + baseY);
			stream.writeShortA(i1);
		}
		if (l == 1448) {
			ItemDef itemDef_1 = ItemDef.lookup(i1);
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

       public void updateRestrictedArea() {
               restrictedArea = 0;
               int j = (myPlayer.x >> 7) + baseX;
               int k = (myPlayer.y >> 7) + baseY;
               if (j >= 3053 && j <= 3156 && k >= 3056 && k <= 3136) {
                       restrictedArea = 1;
               }
               if (j >= 3072 && j <= 3118 && k >= 9492 && k <= 9535) {
                       restrictedArea = 1;
               }
               if (restrictedArea == 1 && j >= 3139 && j <= 3199 && k >= 3008 && k <= 3062) {
                       restrictedArea = 0;
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
		for (int k = 0; k < Model.queueLength; k++) {
			int l = Model.faceQueue[k];
			int i1 = l & 0x7f;
			int j1 = l >> 7 & 0x7f;
			int k1 = l >> 29 & 3;
			int l1 = l >> 14 & 0x7fff;
			if (l == j) {
				continue;
			}
			j = l;
			if (k1 == 2 && worldController.getObjectConfig(plane, i1, j1, l) >= 0) {
				ObjectDef class46 = ObjectDef.forID(l1);
				if (class46.childrenIDs != null) {
					class46 = class46.getChildDefinition();
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
                                if (npc.definition.size == 1 && (npc.x & 0x7f) == 64 && (npc.y & 0x7f) == 64) {
					for (int j2 = 0; j2 < npcCount; j2++) {
						NPC npc2 = npcArray[npcIndices[j2]];
                                                if (npc2 != null && npc2 != npc && npc2.definition.size == 1 && npc2.x == npc.x && npc2.y == npc.y) {
                                                        buildAtNPCMenu(npc2.definition, npcIndices[j2], j1, i1);
						}
					}

					for (int l2 = 0; l2 < playerCount; l2++) {
						Player player = playerArray[playerIndices[l2]];
						if (player != null && player.x == npc.x && player.y == npc.y) {
							buildAtPlayerMenu(i1, playerIndices[l2], player, j1);
						}
					}

				}
                                buildAtNPCMenu(npc.definition, l1, j1, i1);
			}
			if (k1 == 0) {
				Player player = playerArray[l1];
				if ((player.x & 0x7f) == 64 && (player.y & 0x7f) == 64) {
					for (int k2 = 0; k2 < npcCount; k2++) {
						NPC class30_sub2_sub4_sub1_sub1_2 = npcArray[npcIndices[k2]];
                                                if (class30_sub2_sub4_sub1_sub1_2 != null && class30_sub2_sub4_sub1_sub1_2.definition.size == 1 && class30_sub2_sub4_sub1_sub1_2.x == player.x && class30_sub2_sub4_sub1_sub1_2.y == player.y) {
                                                        buildAtNPCMenu(class30_sub2_sub4_sub1_sub1_2.definition, npcIndices[k2], j1, i1);
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
					for (Item item = (Item) class19.getFirst(); item != null; item = (Item) class19.getNext()) {
						ItemDef itemDef = ItemDef.lookup(item.ID);
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
		chatBuffer = null;
		stream = null;
		updateBuffer = null;
		inStream = null;
		regionBaseIds = null;
		terrainData = null;
		objectMapData = null;
		terrainArchiveIds = null;
		objectArchiveIds = null;
		tileHeights = null;
		tileFlags = null;
		worldController = null;
		collisionMaps = null;
		pathDirections = null;
		pathDistances = null;
		pathTileX = null;
		pathTileY = null;
		soundPayload = null;
		textBackground = null;
		chatBackground = null;
		tabAreaBuffer = null;
		fullScreenBackground = null;
		tabAreaIconBuffer = null;
		tabAreaBackgroundBuffer = null;
		mapEdgeBuffer = null;
		backLeftIP1 = null;
		backLeftIP2 = null;
		backRightIP1 = null;
		backRightIP2 = null;
		backTopIP1 = null;
		backVmidIP1 = null;
		backVmidIP2 = null;
		backVmidIP3 = null;
		midSubscreenBuffer = null;
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
		occupiedTiles = null;
		playerArray = null;
		playerIndices = null;
		playerUpdateIndices = null;
		playerBuffers = null;
		removedEntityIndices = null;
		npcArray = null;
		npcIndices = null;
		groundArray = null;
		pendingSpawns = null;
		projectileList = null;
		graphicsObjectList = null;
		menuActionCmd2 = null;
		menuActionCmd3 = null;
		menuActionID = null;
		menuActionCmd1 = null;
		menuActionName = null;
		variousSettings = null;
		minimapIconX = null;
		minimapIconY = null;
		minimapIconSprites = null;
           minimapImage = null;
		friendsList = null;
		friendsListAsLongs = null;
		friendsNodeIDs = null;
		titleLeftProducer = null;
		titleRightProducer = null;
		titleImageProducer = null;
		loginLeftProducer = null;
		loginRightProducer = null;
		titleTopLeftProducer = null;
		titleTopRightProducer = null;
		titleBottomLeftProducer = null;
		titleBottomRightProducer = null;
		multiOverlay = null;
		nullLoader();
		ObjectDef.nullLoader();
		EntityDef.nullLoader();
        ItemDef.resetCache();
		FloorOverlay.cache = null;
		IDK.cache = null;
		RSInterface.interfaceCache = null;
		CachePlaceholder.cache = null;
		Animation.anims = null;
		SpotAnim.cache = null;
           SpotAnim.modelCache = null;
		Varp.cache = null;
		super.fullGameScreen = null;
		Player.mruNodes = null;
		Texture.reset();
		WorldController.nullLoader();
                Model.clearCache();
                AnimFrame.clear();
		System.gc();
	}

	public void printDebug() {
		System.out.println("============");
		System.out.println("flame-cycle:" + flameDrawingCounter);
		if (onDemandFetcher != null) {
			System.out.println("Od-cycle:" + onDemandFetcher.onDemandCycle);
		}
		System.out.println("loop-cycle:" + loopCycle);
		System.out.println("draw-cycle:" + drawCycle);
		System.out.println("ptype:" + pktType);
		System.out.println("psize:" + pktSize);
		if (socketStream != null) {
			socketStream.printDebug();
		}
		super.shouldDebug = true;
	}

	Component getGameComponent() {
		if (Signlink.mainapp != null) {
			return Signlink.mainapp;
		} else {
			return this;
		}
	}

       public void processInput() {
		do {
			int j = readChar(-796);
			if (j == -1) {
				break;
			}
			if (customTabAction == 1 || customTabAction == 2) {
				if (j >= 48 && j <= 57 && promptInput.length() < 10 && !promptInput.toLowerCase().contains("k") && !promptInput.toLowerCase().contains("m") && !promptInput.toLowerCase().contains("b")) {
					promptInput += (char) j;
					inputTaken = true;
				}
				if ((!promptInput.toLowerCase().contains("k") && !promptInput.toLowerCase().contains("m") && !promptInput.toLowerCase().contains("b")) && (j == 107 || j == 109) || j == 98) {
					promptInput += (char) j;
					inputTaken = true;
				}
				if (j == 8 && promptInput.length() > 0) {
					promptInput = promptInput.substring(0, promptInput.length() - 1);
					inputTaken = true;
				}
				try {
					if (j == 13 || j == 10) {
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
				if (j == 8 && reportAbuseInput.length() > 0) {
					reportAbuseInput = reportAbuseInput.substring(0, reportAbuseInput.length() - 1);
				}
				if ((j >= 97 && j <= 122 || j >= 65 && j <= 90 || j >= 48 && j <= 57 || j == 32) && reportAbuseInput.length() < 12) {
					reportAbuseInput += (char) j;
				}
			} else if (messagePromptRaised) {
				if (j >= 32 && j <= 122 && promptInput.length() < 80) {
					promptInput += (char) j;
					inputTaken = true;
				}
				if (j == 8 && promptInput.length() > 0) {
					promptInput = promptInput.substring(0, promptInput.length() - 1);
					inputTaken = true;
				}
				if (j == 13 || j == 10) {
					messagePromptRaised = false;
					inputTaken = true;
					if (friendsListAction == 1) {
						long l = TextClass.longForName(promptInput);
						addFriend(l);
					}
					if (friendsListAction == 2 && friendsCount > 0) {
						long l1 = TextClass.longForName(promptInput);
						delFriend(l1);
					}
					if (friendsListAction == 3 && promptInput.length() > 0) {
						stream.createFrame(126);
						stream.writeWordBigEndian(0);
						int k = stream.currentOffset;
						stream.writeQWord(privateMessageRecipient);
                                                TextInput.encodeChatMessage(promptInput, stream);
						stream.writeBytes(stream.currentOffset - k);
						promptInput = TextInput.processText(promptInput);
						promptInput = Censor.doCensor(promptInput);
						pushMessage(promptInput, 6, TextClass.fixName(TextClass.nameForLong(privateMessageRecipient)));
						if (privateChatMode == 2) {
							privateChatMode = 1;
							chatSettingsUpdateNeeded = true;
							stream.createFrame(95);
							stream.writeWordBigEndian(publicChatMode);
							stream.writeWordBigEndian(privateChatMode);
							stream.writeWordBigEndian(tradeMode);
						}
					}
					if (friendsListAction == 4 && ignoreCount < 100) {
						long l2 = TextClass.longForName(promptInput);
						addIgnore(l2);
					}
					if (friendsListAction == 5 && ignoreCount > 0) {
						long l3 = TextClass.longForName(promptInput);
						delIgnore(l3);
					}
				}
			} else if (inputDialogState == 1) {
				if (j >= 48 && j <= 57 && amountOrNameInput.length() < 10 && !amountOrNameInput.toLowerCase().contains("k") && !amountOrNameInput.toLowerCase().contains("m") && !amountOrNameInput.toLowerCase().contains("b")) {
					amountOrNameInput += (char) j;
					inputTaken = true;
				}
				if ((!amountOrNameInput.toLowerCase().contains("k") && !amountOrNameInput.toLowerCase().contains("m") && !amountOrNameInput.toLowerCase().contains("b")) && (j == 107 || j == 109) || j == 98) {
					amountOrNameInput += (char) j;
					inputTaken = true;
				}
				if (j == 8 && amountOrNameInput.length() > 0) {
					amountOrNameInput = amountOrNameInput.substring(0, amountOrNameInput.length() - 1);
					inputTaken = true;
				}
				try {
					if (j == 13 || j == 10) {
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
							stream.createFrame(208);
							stream.writeDWord(amount);
						}
						inputDialogState = 0;
						inputTaken = true;
					}
				} catch (NumberFormatException nfe) {
					inputDialogState = 0;
					inputTaken = true;
					pushMessage("Please enter a lower amount.", 0, "");
				}
			} else if (inputDialogState == 2) {
				if (j >= 32 && j <= 122 && amountOrNameInput.length() < 12) {
					amountOrNameInput += (char) j;
					inputTaken = true;
				}
				if (j == 8 && amountOrNameInput.length() > 0) {
					amountOrNameInput = amountOrNameInput.substring(0, amountOrNameInput.length() - 1);
					inputTaken = true;
				}
				if (j == 13 || j == 10) {
					if (amountOrNameInput.length() > 0) {
						stream.createFrame(60);
						stream.writeQWord(TextClass.longForName(amountOrNameInput));
					}
					inputDialogState = 0;
					inputTaken = true;
				}
			} else {
				// typing characters
				if (j >= 32 && j <= 122 && inputString.length() < 80) {
					inputString += (char) j;
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
				if (j == 8 && inputString.length() > 0) {
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
				if ((j == 13 || j == 10) && inputString.length() > 0) {
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
										collisionMaps[k1].clippingFlags[i2][k2] = 0;
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
                                                                onDemandFetcher.validateOrQueue((byte) 1, 2, j1);
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
						stream.createFrame(103);
						stream.writeWordBigEndian(inputString.length() - 1);
						stream.writeString(inputString.substring(2));
					} else {
						String s = inputString.toLowerCase();
						int j2 = 0;
						if (s.startsWith("yellow:")) {
							j2 = 0;
							inputString = inputString.substring(7);
						} else if (s.startsWith("red:")) {
							j2 = 1;
							inputString = inputString.substring(4);
						} else if (s.startsWith("green:")) {
							j2 = 2;
							inputString = inputString.substring(6);
						} else if (s.startsWith("cyan:")) {
							j2 = 3;
							inputString = inputString.substring(5);
						} else if (s.startsWith("purple:")) {
							j2 = 4;
							inputString = inputString.substring(7);
						} else if (s.startsWith("white:")) {
							j2 = 5;
							inputString = inputString.substring(6);
						} else if (s.startsWith("flash1:")) {
							j2 = 6;
							inputString = inputString.substring(7);
						} else if (s.startsWith("flash2:")) {
							j2 = 7;
							inputString = inputString.substring(7);
						} else if (s.startsWith("flash3:")) {
							j2 = 8;
							inputString = inputString.substring(7);
						} else if (s.startsWith("glow1:")) {
							j2 = 9;
							inputString = inputString.substring(6);
						} else if (s.startsWith("glow2:")) {
							j2 = 10;
							inputString = inputString.substring(6);
						} else if (s.startsWith("glow3:")) {
							j2 = 11;
							inputString = inputString.substring(6);
						}
						s = inputString.toLowerCase();
						int i3 = 0;
						if (s.startsWith("wave:")) {
							i3 = 1;
							inputString = inputString.substring(5);
						} else if (s.startsWith("wave2:")) {
							i3 = 2;
							inputString = inputString.substring(6);
						} else if (s.startsWith("shake:")) {
							i3 = 3;
							inputString = inputString.substring(6);
						} else if (s.startsWith("scroll:")) {
							i3 = 4;
							inputString = inputString.substring(7);
						} else if (s.startsWith("slide:")) {
							i3 = 5;
							inputString = inputString.substring(6);
						}
						stream.createFrame(4);
						stream.writeWordBigEndian(0);
						int j3 = stream.currentOffset;
                                                stream.writeByteSub(i3);
                                                stream.writeByteSub(j2);
						chatBuffer.currentOffset = 0;
                                                TextInput.encodeChatMessage(inputString, chatBuffer);
						stream.writeBytesReverseAdd(0, chatBuffer.buffer, chatBuffer.currentOffset);
						stream.writeBytes(stream.currentOffset - j3);
						inputString = TextInput.processText(inputString);
						inputString = Censor.doCensor(inputString);
						myPlayer.textSpoken = inputString;
						myPlayer.chatColor = j2;
						myPlayer.chatEffect = i3;
						myPlayer.textCycle = 150;
						if (myPrivilege >= 1) {
							pushMessage(myPlayer.textSpoken, 2, "@cr" + Math.min(2, myPrivilege) + "@" + myPlayer.name);
						} else {
							pushMessage(myPlayer.textSpoken, 2, myPlayer.name);
						}
						if (publicChatMode == 2) {
							publicChatMode = 3;
							chatSettingsUpdateNeeded = true;
							stream.createFrame(95);
							stream.writeWordBigEndian(publicChatMode);
							stream.writeWordBigEndian(privateChatMode);
							stream.writeWordBigEndian(tradeMode);
						}
					}
					inputString = "";
					inputTaken = true;
				}
			}
		} while (true);
	}

	public void buildChatAreaMenu(int j) {
		int l = 0;
		for (int i1 = 0; i1 < 100; i1++) {
			if (chatMessages[i1] == null) {
				continue;
			}
			int j1 = chatTypes[i1];
			int k1 = 70 - l * 14 + chatScrollPosition + 4;
			if (k1 < -20) {
				break;
			}
			String s = chatNames[i1];
			if (s != null && s.startsWith("@cr1@")) {
				s = s.substring(5);
			}
			if (s != null && s.startsWith("@cr2@")) {
				s = s.substring(5);
			}
			if (j1 == 0) {
				l++;
			}
			if ((j1 == 1 || j1 == 2) && (j1 == 1 || publicChatMode == 0 || publicChatMode == 1 && isFriendOrSelf(s))) {
				if (j > k1 - 14 && j <= k1 && !s.equals(myPlayer.name)) {
					if (myPrivilege >= 1 && myPrivilege <= 3) {
						menuActionName[menuActionRow] = "Report abuse @whi@" + s;
						menuActionID[menuActionRow] = 606;
						menuActionRow++;
					}
					menuActionName[menuActionRow] = "Add ignore @whi@" + s;
					menuActionID[menuActionRow] = 42;
					menuActionRow++;
					menuActionName[menuActionRow] = "Reply to @whi@" + s;
					menuActionID[menuActionRow] = 639;
					menuActionRow++;
					menuActionName[menuActionRow] = "Add friend @whi@" + s;
					menuActionID[menuActionRow] = 337;
					menuActionRow++;
				}
				l++;
			}
			if ((j1 == 3 || j1 == 7) && splitpublicChat == 0 && (j1 == 7 || privateChatMode == 0 || publicChatMode == 1 && isFriendOrSelf(s))) {
				if (j > k1 - 14 && j <= k1) {
					if (myPrivilege >= 1 && myPrivilege <= 3) {
						menuActionName[menuActionRow] = "Report abuse @whi@" + s;
						menuActionID[menuActionRow] = 606;
						menuActionRow++;
					}
					menuActionName[menuActionRow] = "Add ignore @whi@" + s;
					menuActionID[menuActionRow] = 42;
					menuActionRow++;
					menuActionName[menuActionRow] = "Reply to @whi@" + s;
					menuActionID[menuActionRow] = 639;
					menuActionRow++;
					menuActionName[menuActionRow] = "Add friend @whi@" + s;
					menuActionID[menuActionRow] = 337;
					menuActionRow++;
				}
				l++;
			}
			if (j1 == 4 && (tradeMode == 0 || tradeMode == 1 && isFriendOrSelf(s))) {
				if (j > k1 - 14 && j <= k1) {
					menuActionName[menuActionRow] = "Accept trade @whi@" + s;
					menuActionID[menuActionRow] = 484;
					menuActionRow++;
				}
				l++;
			}
			if ((j1 == 5 || j1 == 6) && splitpublicChat == 0 && privateChatMode < 2) {
				l++;
			}
			if (j1 == 8 && (tradeMode == 0 || tradeMode == 1 && isFriendOrSelf(s))) {
				if (j > k1 - 14 && j <= k1) {
					menuActionName[menuActionRow] = "Accept challenge @whi@" + s;
					menuActionID[menuActionRow] = 6;
					menuActionRow++;
				}
				l++;
			}
		}

	}

	public void drawFriendsListOrWelcomeScreen(RSInterface class9) {
		int j = class9.contentType;
		if (j >= 1 && j <= 100 || j >= 701 && j <= 800) {
			if (j == 1 && interfaceMode == 0) {
				class9.disabledText = "Loading friend list";
				class9.atActionType = 0;
				return;
			}
			if (j == 1 && interfaceMode == 1) {
				class9.disabledText = "Connecting to friendserver";
				class9.atActionType = 0;
				return;
			}
			if (j == 2 && interfaceMode != 2) {
				class9.disabledText = "Please wait...";
				class9.atActionType = 0;
				return;
			}
			int k = friendsCount;
			if (interfaceMode != 2) {
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
			if (interfaceMode != 2) {
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
			if (interfaceMode != 2) {
				i1 = 0;
			}
			class9.scrollMax = i1 * 15 + 20;
			if (class9.scrollMax <= class9.height) {
				class9.scrollMax = class9.height + 1;
			}
			return;
		}
		if (j >= 401 && j <= 500) {
			if ((j -= 401) == 0 && interfaceMode == 0) {
				class9.disabledText = "Loading ignore list";
				class9.atActionType = 0;
				return;
			}
			if (j == 1 && interfaceMode == 0) {
				class9.disabledText = "Please wait...";
				class9.atActionType = 0;
				return;
			}
			int j1 = ignoreCount;
			if (interfaceMode == 0) {
				j1 = 0;
			}
			if (j >= j1) {
				class9.disabledText = "";
				class9.atActionType = 0;
				return;
			} else {
				class9.disabledText = TextClass.fixName(TextClass.nameForLong(ignoreListAsLongs[j]));
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
                        class9.modelRotation1 = 150;
                        class9.modelRotation2 = (int) (Math.sin((double) loopCycle / 40D) * 256D) & 0x7ff;
			if (characterDesignChanged) {
				for (int k1 = 0; k1 < 7; k1++) {
					int l1 = characterStyle[k1];
                                        if (l1 >= 0 && !IDK.cache[l1].ready()) {
						return;
					}
				}

				characterDesignChanged = false;
				Model aclass30_sub2_sub4_sub6s[] = new Model[7];
				int i2 = 0;
				for (int j2 = 0; j2 < 7; j2++) {
					int k2 = characterStyle[j2];
					if (k2 >= 0) {
                                                aclass30_sub2_sub4_sub6s[i2++] = IDK.cache[k2].getBodyModel();
					}
				}

				Model model = new Model(i2, aclass30_sub2_sub4_sub6s);
				for (int l2 = 0; l2 < 5; l2++) {
					if (characterColorIndices[l2] != 0) {
						model.recolor(appearanceColorOptions[l2][0], appearanceColorOptions[l2][characterColorIndices[l2]]);
						if (l2 == 1) {
							model.recolor(additionalColorCodes[0], additionalColorCodes[characterColorIndices[l2]]);
						}
					}
				}

				model.buildVertexGroups();
				model.applyFrame(Animation.anims[myPlayer.standAnimation].frameIds[0]);
				model.applyLighting(64, 850, -30, -50, -30, true);
                                class9.mediaType = 5;
                                class9.mediaId = 0;
                                RSInterface.clearModelCache(model, 0, 5);
			}
			return;
		}
		if (j == 324) {
			if (maleIconSprite == null) {
				maleIconSprite = class9.sprite1;
				femaleIconSprite = class9.sprite2;
			}
			if (isMaleCharacter) {
				class9.sprite1 = femaleIconSprite;
				return;
			} else {
				class9.sprite1 = maleIconSprite;
				return;
			}
		}
		if (j == 325) {
			if (maleIconSprite == null) {
				maleIconSprite = class9.sprite1;
				femaleIconSprite = class9.sprite2;
			}
			if (isMaleCharacter) {
				class9.sprite1 = maleIconSprite;
				return;
			} else {
				class9.sprite1 = femaleIconSprite;
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
			if (recoveryQuestionChangeDate == 0)
				class9.disabledText = "\\nYou have not yet set any recovery questions.\\nIt is @lre@strongly@yel@ recommended that you do so.\\n\\nIf you don't you will be @lre@unable to recover your\\n@lre@password@yel@ if you forget it, or it is stolen.";
			else if (recoveryQuestionChangeDate <= currentDateOffset) {
				class9.disabledText = "\\n\\nRecovery Questions Last Set:\\n@gre@"
						+ formatDate(recoveryQuestionChangeDate);
			} else {
				int l1 = (currentDateOffset + 14) - recoveryQuestionChangeDate;
				String s2;
				if (l1 <= 0)
					s2 = "Earlier today";
				else if (l1 == 1)
					s2 = "Yesterday";
				else
					s2 = l1 + " days ago";
				class9.disabledText = s2
						+ " you requested@lre@ new recovery\\n@lre@questions.@yel@ The requested change will occur\\non: @lre@"
						+ formatDate(recoveryQuestionChangeDate)
						+ "\\n\\nIf you do not remember making this request\\ncancel it immediately, and change your password.";
			}
		if (j == 663)
			if (lastPasswordChange <= 0 || lastPasswordChange > currentDateOffset + 10)
				class9.disabledText = "Last password change:\\n@gre@Never changed";
			else
				class9.disabledText = "Last password change:\\n@gre@"
						+ formatDate(lastPasswordChange);
		if (j == 668) {
			if (recoveryQuestionChangeDate > currentDateOffset) {
				class9.disabledText = "To cancel this request:\\n1) Logout and return to the frontpage of this website.\\n2) Choose 'Cancel recovery questions'.";
				return;
			}
			class9.disabledText = "To change your recovery questions:\\n1) Logout and return to the frontpage of this website.\\n2) Choose 'Set new recovery questions'.";
		}
	}
	
        public String formatDate(int i) {
		if (i > currentDateOffset + 10) {
			return "Unknown";
		} else {
			long l = ((long) i + 11745L) * 0x5265c00L;
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date(l));
			int j = calendar.get(5);
			int k = calendar.get(2);
			int i1 = calendar.get(1);
			String as[] = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul",
					"Aug", "Sep", "Oct", "Nov", "Dec" };
			return j + "-" + as[k] + "-" + i1;
		}
	}

	public void drawSplitpublicChat() {
		if (splitpublicChat == 0) {
			return;
		}
		TextDrawingArea textDrawingArea = boldFont;
		int i = 0;
		if (systemUpdateTimer != 0) {
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
					textDrawingArea.textLeft(0, "From", l, k1);
					textDrawingArea.textLeft(0x00ffff, "From", l - 1, k1);
					k1 += textDrawingArea.getTextWidth("From ");
					if (byte1 == 1) {
						modIcons[0].draw(k1, l - 12);
						k1 += 14;
					}
					if (byte1 == 2) {
						modIcons[1].draw(k1, l - 12);
						k1 += 14;
					}
					textDrawingArea.textLeft(0, s + ": " + chatMessages[j], l, k1);
					textDrawingArea.textLeft(0x00ffff, s + ": " + chatMessages[j], l - 1, k1);
					if (++i >= 5) {
						return;
					}
				}
				if (k == 5 && privateChatMode < 2) {
					int i1 = 329 - i * 13;
					textDrawingArea.textLeft(0, chatMessages[j], i1, 4);
					textDrawingArea.textLeft(0x00ffff, chatMessages[j], i1 - 1, 4);
					if (++i >= 5) {
						return;
					}
				}
				if (k == 6 && privateChatMode < 2) {
					int j1 = 329 - i * 13;
					textDrawingArea.textLeft(0, "To " + s + ": " + chatMessages[j], j1, 4);
					textDrawingArea.textLeft(0x00ffff, "To " + s + ": " + chatMessages[j], j1 - 1, 4);
					if (++i >= 5) {
						return;
					}
				}
			}
		}

	}
	public void screenshot(boolean sendMessage, String... subfolders) {
		try {
			Window window = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusedWindow();
			if (window == null) {
				return;
			}
			Point point = window.getLocationOnScreen();
			int x = (int) point.getX();
			int y = (int) point.getY();
			int w = window.getWidth();
			int h = window.getHeight();
			Robot robot = new Robot(window.getGraphicsConfiguration().getDevice());
			Rectangle captureSize = new Rectangle(x, y, w, h);
			BufferedImage bufferedimage = robot.createScreenCapture(captureSize);
	
			// Format the current date and time
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd-HH_mm_ss");
			String dateTime = dateFormat.format(new Date());
	
			// Update the file path and naming
			String fileExtension = myUsername != null && !myUsername.isEmpty() ? myUsername : ClientSettings.SERVER_NAME;
			
			String subfolderPath = String.join(File.separator, subfolders);
			if (!subfolderPath.isEmpty()) {
				subfolderPath += File.separator;
			}
			
			String screenshotDir = System.getProperty("user.home") + File.separatorChar + ClientSettings.SERVER_NAME + File.separatorChar + "screenshots" + File.separatorChar + subfolderPath;
			File dir = new File(screenshotDir);
			if (!dir.exists()) {
				dir.mkdirs(); // Create the directory if it doesn't exist
			}
	
			File file = new File(screenshotDir, fileExtension + "_" + dateTime + ".png");
	
			if (!file.exists()) {
				ImageIO.write(bufferedimage, "png", file);
				if (sendMessage) {
					pushMessage("A picture has been saved in your screenshots folder.", 0, "");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void pushMessage(String s, int i, String s1) {
		if (i == 0 && dialogID != -1) {
			messagePrompt = s;
			super.clickMode3 = 0;
		}
		if (backDialogID == -1) {
			inputTaken = true;
		}
		for (int j = 99; j > 0; j--) {
			chatTypes[j] = chatTypes[j - 1];
			chatNames[j] = chatNames[j - 1];
			chatMessages[j] = chatMessages[j - 1];
		}

		chatTypes[0] = i;
		chatNames[0] = s1;
		chatMessages[0] = s;
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
							inputPrompt = "Enter minimum item value";
							customTabAction = 1;
						}
						startY += 40;
						if (super.saveClickY >= startY && super.saveClickY <= (startY + 30)) {
							inputTaken = true;
							inputDialogState = 0;
							messagePromptRaised = true;
							promptInput = "";
							inputPrompt = "Enter new draw distance";
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
			if (flashingTabId == tabID) {
				stream.createFrame(152);
				stream.writeWordBigEndian(tabID);
			}
		}
	}
	
	public void resetImageProducers() {
		if (titleImageProducer != null) {
			return;
		}
		super.fullGameScreen = null;
		fullScreenBackground = null;
		chatBackground = null;
		textBackground = null;
		tabAreaBuffer = null;
		tabAreaIconBuffer = null;
		tabAreaBackgroundBuffer = null;
		mapEdgeBuffer = null;
		titleLeftProducer = new RSImageProducer(128, 265, getGameComponent());
		DrawingArea.setAllPixelsToZero();
		titleRightProducer = new RSImageProducer(128, 265, getGameComponent());
		DrawingArea.setAllPixelsToZero();
		titleImageProducer = new RSImageProducer(509, 171, getGameComponent());
		DrawingArea.setAllPixelsToZero();
		loginLeftProducer = new RSImageProducer(360, 132, getGameComponent());
		DrawingArea.setAllPixelsToZero();
		loginRightProducer = new RSImageProducer(360, 200, getGameComponent());
		DrawingArea.setAllPixelsToZero();
		titleTopLeftProducer = new RSImageProducer(202, 238, getGameComponent());
		DrawingArea.setAllPixelsToZero();
		titleTopRightProducer = new RSImageProducer(203, 238, getGameComponent());
		DrawingArea.setAllPixelsToZero();
		titleBottomLeftProducer = new RSImageProducer(74, 94, getGameComponent());
		DrawingArea.setAllPixelsToZero();
		titleBottomRightProducer = new RSImageProducer(75, 94, getGameComponent());
		DrawingArea.setAllPixelsToZero();
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
		titleImageProducer = null;
		loginLeftProducer = null;
		loginRightProducer = null;
		titleTopLeftProducer = null;
		titleTopRightProducer = null;
		titleBottomLeftProducer = null;
		titleBottomRightProducer = null;
		fullScreenBackground = null;
		chatBackground = null;
		textBackground = null;
		tabAreaBuffer = null;
		tabAreaIconBuffer = null;
		mapEdgeBuffer = null;
		tabAreaBackgroundBuffer = null;
		super.fullGameScreen = new RSImageProducer(765, 503, getGameComponent());
		welcomeScreenRaised = true;
	}

	public void resetImageProducers2() {
		if (fullScreenBackground != null) {
			return;
		}
		nullLoader();
		super.fullGameScreen = null;
		titleImageProducer = null;
		loginLeftProducer = null;
		loginRightProducer = null;
		titleLeftProducer = null;
		titleRightProducer = null;
		titleTopLeftProducer = null;
		titleTopRightProducer = null;
		titleBottomLeftProducer = null;
		titleBottomRightProducer = null;
		fullScreenBackground = new RSImageProducer(479, 96, getGameComponent());
		chatBackground = new RSImageProducer(172, 156, getGameComponent());
		DrawingArea.setAllPixelsToZero();
		mapBack.draw(0, 0);
		textBackground = new RSImageProducer(190, 261, getGameComponent());
		tabAreaBuffer = new RSImageProducer(512, 334, getGameComponent());
		DrawingArea.setAllPixelsToZero();
		tabAreaIconBuffer = new RSImageProducer(496, 50, getGameComponent());
		tabAreaBackgroundBuffer = new RSImageProducer(269, 37, getGameComponent());
		mapEdgeBuffer = new RSImageProducer(249, 45, getGameComponent());
		welcomeScreenRaised = true;
		tabAreaBuffer.initDrawingArea();
		Texture.lineOffsets = chatBoxAreaOffsets;
		// SignLink.midii.fadeOut();
	}

       public void drawMinimapHint(Sprite sprite, int y, int x) {
		int l = x * x + y * y;
		if (l > 4225 && l < 90000) {
			int i1 = cameraYaw + minimapRotationOffset & 0x7ff;
			int j1 = Model.sineTable[i1];
			int k1 = Model.cosineTable[i1];
			j1 = (j1 * 256) / (minimapZoom + 256);
			k1 = (k1 * 256) / (minimapZoom + 256);
			int l1 = y * j1 + x * k1 >> 16;
			int i2 = y * k1 - x * j1 >> 16;
			double d = Math.atan2((double) l1, (double) i2);
			int j2 = (int) (Math.sin(d) * 63D);
			int k2 = (int) (Math.cos(d) * 57D);
			mapEdge.drawRotated(83 - k2 - 20, d, 94 + j2 + 4 - 10);
		} else {
			markMinimap(sprite, x, y);
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
			hoveredWidgetId = 0;
			buildInterfaceMenu(0, RSInterface.interfaceCache[fullScreenInterfaceId], super.mouseX, 0, super.mouseY, 0);
			if (hoveredWidgetId != lastHoveredWidgetId) {
				lastHoveredWidgetId = hoveredWidgetId;
			}
			return;
		}
		buildSplitPrivateChatMenu();
		hoveredWidgetId = 0;
		if (super.mouseX > 4 && super.mouseY > 4 && super.mouseX < 516 && super.mouseY < 338) {
			if (openInterfaceID != -1) {
				buildInterfaceMenu(4, RSInterface.interfaceCache[openInterfaceID], super.mouseX, 4, super.mouseY, 0);
			} else {
				build3dScreenMenu();
			}
		}
		if (hoveredWidgetId != lastHoveredWidgetId) {
			lastHoveredWidgetId = hoveredWidgetId;
		}
		hoveredWidgetId = 0;
		if (super.mouseX > 553 && super.mouseY > 205 && super.mouseX < 743 && super.mouseY < 466) {
			if (invOverlayInterfaceID != -1) {
				buildInterfaceMenu(553, RSInterface.interfaceCache[invOverlayInterfaceID], super.mouseX, 205, super.mouseY, 0);
			} else if (tabInterfaceIDs[tabID] != -1) {
				buildInterfaceMenu(553, RSInterface.interfaceCache[tabInterfaceIDs[tabID]], super.mouseX, 205, super.mouseY, 0);
			}
		}
		if (hoveredWidgetId != hoveredTabId) {
			needDrawTabArea = true;
			hoveredTabId = hoveredWidgetId;
		}
		hoveredWidgetId = 0;
		if (super.mouseX > 17 && super.mouseY > 357 && super.mouseX < 496 && super.mouseY < 453) {
			if (backDialogID != -1) {
				buildInterfaceMenu(17, RSInterface.interfaceCache[backDialogID], super.mouseX, 357, super.mouseY, 0);
			} else if (dialogID != -1) {
				buildInterfaceMenu(17, RSInterface.interfaceCache[dialogID], super.mouseX, 357, super.mouseY, 0);
			} else if (super.mouseY < 434 && super.mouseX < 426) {
				buildChatAreaMenu(super.mouseY - 357);
			}
		}
		if ((backDialogID != -1 || dialogID != -1) && hoveredWidgetId != lastInteractionId) {//TODO remove if any issues
			inputTaken = true;
			lastInteractionId = hoveredWidgetId;
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

	public int blendColors(int i, int j, int k) {
		int l = 256 - k;
		return ((i & 0xff00ff) * l + (j & 0xff00ff) * k & 0xff00ff00) + ((i & 0xff00) * l + (j & 0xff00) * k & 0xff0000) >> 8;
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
			long l = TextClass.longForName(s);
			int i = (int) (l >> 16 & 31L);
			stream.currentOffset = 0;
			stream.writeWordBigEndian(14);
			stream.writeWordBigEndian(i);
			socketStream.queueBytes(2, stream.buffer);
			for (int j = 0; j < 8; j++) {
				socketStream.read();
			}

			int k = socketStream.read();
			int i1 = k;
			if (k == 0) {
				socketStream.flushInputStream(inStream.buffer, 8);
				inStream.currentOffset = 0;
				serverSessionKey = inStream.readQWord();
				int ai[] = new int[4];
				ai[0] = (int) (Math.random() * 99999999D);
				ai[1] = (int) (Math.random() * 99999999D);
				ai[2] = (int) (serverSessionKey >> 32);
				ai[3] = (int) serverSessionKey;
				stream.currentOffset = 0;
				stream.writeWordBigEndian(10);
				stream.writeDWord(ai[0]);
				stream.writeDWord(ai[1]);
				stream.writeDWord(ai[2]);
				stream.writeDWord(ai[3]);
				stream.writeDWord(ClientSettings.UID);
				stream.writeString(s);
				stream.writeString(s1);
				stream.rsaEncrypt();
				updateBuffer.currentOffset = 0;
				if (flag) {
					updateBuffer.writeWordBigEndian(18);
				} else {
					updateBuffer.writeWordBigEndian(16);
				}
				updateBuffer.writeWordBigEndian(stream.currentOffset + 36 + 1 + 1 + 2);
				updateBuffer.writeWordBigEndian(255);
				updateBuffer.writeWord(1);
				updateBuffer.writeWordBigEndian(lowMem ? 1 : 0);
				for (int l1 = 0; l1 < 9; l1++) {
					updateBuffer.writeDWord(expectedCRCs[l1]);
				}

				updateBuffer.writeBytes(stream.buffer, stream.currentOffset, 0);
				stream.encryption = new ISAACRandomGen(ai);
				for (int j2 = 0; j2 < 4; j2++) {
					ai[j2] += 50;
				}

				encryption = new ISAACRandomGen(ai);
				socketStream.queueBytes(updateBuffer.currentOffset, updateBuffer.buffer);
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
				lastMouseClickTime = 0L;
				mouseIdleTicks = 0;
				mouseDetection.coordsIndex = 0;
				super.awtFocus = true;
				hasFocus = true;
				loggedIn = true;
				stream.currentOffset = 0;
				inStream.currentOffset = 0;
				pktType = -1;
				lastPacketType = -1;
				prevPktType = -1;
				prevPktType2 = -1;
				pktSize = 0;
				connectionTimeoutCounter = 0;
				systemUpdateTimer = 0;
				reconnectDelay = 0;
				hintIconState = 0;
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
				cameraXOffset = (int) (Math.random() * 100D) - 50;
				cameraYOffset = (int) (Math.random() * 110D) - 55;
				cameraYawOffset = (int) (Math.random() * 80D) - 40;
				minimapRotationOffset = (int) (Math.random() * 120D) - 60;
				minimapZoom = (int) (Math.random() * 30D) - 20;
				cameraYaw = (int) (Math.random() * 20D) - 10 & 0x7ff;
				minimapState = 0;
				lastPlane = -1;
				destX = 0;
				destY = 0;
				playerCount = 0;
				npcCount = 0;
				for (int i2 = 0; i2 < maxPlayers; i2++) {
					playerArray[i2] = null;
					playerBuffers[i2] = null;
				}

				for (int k2 = 0; k2 < 16384; k2++) {
					npcArray[k2] = null;
				}

				myPlayer = playerArray[myPlayerIndex] = new Player();
				projectileList.removeAll();
				graphicsObjectList.removeAll();
				for (int l2 = 0; l2 < 4; l2++) {
					for (int i3 = 0; i3 < 104; i3++) {
						for (int k3 = 0; k3 < 104; k3++) {
							groundArray[l2][i3][k3] = null;
						}
					}
				}

				pendingSpawns = new NodeList();
				interfaceMode = 0;
				friendsCount = 0;
				dialogID = -1;
				backDialogID = -1;
				openInterfaceID = -1;
				invOverlayInterfaceID = -1;
				fullScreenInterfaceId = -1;
				overlayInterfaceId = -1;
				actionPending = false;
				tabID = 3;
				inputDialogState = 0;
				menuOpen = false;
				messagePromptRaised = false;
				messagePrompt = null;
				multiCombatZone = 0;
				flashingTabId = -1;
				isMaleCharacter = true;
                           resetCharacterOptions();
				for (int j3 = 0; j3 < 5; j3++) {
					characterColorIndices[j3] = 0;
				}

				for (int l3 = 0; l3 < 5; l3++) {
					atPlayerActions[l3] = null;
					atPlayerArray[l3] = false;
				}

				itemUseCounter = 0;
				npcAttackCounter = 0;
				playerOptionCounter = 0;
                               walkPacketCounter = 0;
				objectClickCounter = 0;
				actionCounter = 0;
				npcInteractionCounter = 0;
				npcClickCounter = 0;
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
				lastPacketType = -1;
				prevPktType = -1;
				prevPktType2 = -1;
				pktSize = 0;
				connectionTimeoutCounter = 0;
				systemUpdateTimer = 0;
				menuActionRow = 0;
				menuOpen = false;
				loadingStartTime = System.currentTimeMillis();
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

	public boolean doWalkTo(int i, int j, int k, int i1, int j1, int k1, int l1, int i2, int j2, boolean flag, int k2) {
		byte byte0 = 104;
		byte byte1 = 104;
		for (int l2 = 0; l2 < byte0; l2++) {
			for (int i3 = 0; i3 < byte1; i3++) {
				pathDirections[l2][i3] = 0;
				pathDistances[l2][i3] = 0x5f5e0ff;
			}

		}
		int j3 = j2;
		int k3 = j1;
		pathDirections[j2][j1] = 99;
		pathDistances[j2][j1] = 0;
		int l3 = 0;
		int i4 = 0;
		pathTileX[l3] = j2;
		pathTileY[l3++] = j1;
		boolean flag1 = false;
		int j4 = pathTileX.length;
		int ai[][] = collisionMaps[plane].clippingFlags;
		while (i4 != l3) {
			j3 = pathTileX[i4];
			k3 = pathTileY[i4];
			i4 = (i4 + 1) % j4;
			if (j3 == k2 && k3 == i2) {
				flag1 = true;
				break;
			}
			if (i1 != 0) {
				if ((i1 < 5 || i1 == 10) && collisionMaps[plane].canReachWall(k2, j3, k3, j, i1 - 1, i2)) {
					flag1 = true;
					break;
				}
				if (i1 < 10 && collisionMaps[plane].canReachObject(k2, i2, k3, i1 - 1, j, j3)) {
					flag1 = true;
					break;
				}
			}
			if (k1 != 0 && k != 0 && collisionMaps[plane].canReachArea(i2, k2, j3, k, l1, k1, k3)) {
				flag1 = true;
				break;
			}
			int l4 = pathDistances[j3][k3] + 1;
			if (j3 > 0 && pathDirections[j3 - 1][k3] == 0 && (ai[j3 - 1][k3] & 0x1280108) == 0) {
				pathTileX[l3] = j3 - 1;
				pathTileY[l3] = k3;
				l3 = (l3 + 1) % j4;
				pathDirections[j3 - 1][k3] = 2;
				pathDistances[j3 - 1][k3] = l4;
			}
			if (j3 < byte0 - 1 && pathDirections[j3 + 1][k3] == 0 && (ai[j3 + 1][k3] & 0x1280180) == 0) {
				pathTileX[l3] = j3 + 1;
				pathTileY[l3] = k3;
				l3 = (l3 + 1) % j4;
				pathDirections[j3 + 1][k3] = 8;
				pathDistances[j3 + 1][k3] = l4;
			}
			if (k3 > 0 && pathDirections[j3][k3 - 1] == 0 && (ai[j3][k3 - 1] & 0x1280102) == 0) {
				pathTileX[l3] = j3;
				pathTileY[l3] = k3 - 1;
				l3 = (l3 + 1) % j4;
				pathDirections[j3][k3 - 1] = 1;
				pathDistances[j3][k3 - 1] = l4;
			}
			if (k3 < byte1 - 1 && pathDirections[j3][k3 + 1] == 0 && (ai[j3][k3 + 1] & 0x1280120) == 0) {
				pathTileX[l3] = j3;
				pathTileY[l3] = k3 + 1;
				l3 = (l3 + 1) % j4;
				pathDirections[j3][k3 + 1] = 4;
				pathDistances[j3][k3 + 1] = l4;
			}
			if (j3 > 0 && k3 > 0 && pathDirections[j3 - 1][k3 - 1] == 0 && (ai[j3 - 1][k3 - 1] & 0x128010e) == 0 && (ai[j3 - 1][k3] & 0x1280108) == 0 && (ai[j3][k3 - 1] & 0x1280102) == 0) {
				pathTileX[l3] = j3 - 1;
				pathTileY[l3] = k3 - 1;
				l3 = (l3 + 1) % j4;
				pathDirections[j3 - 1][k3 - 1] = 3;
				pathDistances[j3 - 1][k3 - 1] = l4;
			}
			if (j3 < byte0 - 1 && k3 > 0 && pathDirections[j3 + 1][k3 - 1] == 0 && (ai[j3 + 1][k3 - 1] & 0x1280183) == 0 && (ai[j3 + 1][k3] & 0x1280180) == 0 && (ai[j3][k3 - 1] & 0x1280102) == 0) {
				pathTileX[l3] = j3 + 1;
				pathTileY[l3] = k3 - 1;
				l3 = (l3 + 1) % j4;
				pathDirections[j3 + 1][k3 - 1] = 9;
				pathDistances[j3 + 1][k3 - 1] = l4;
			}
			if (j3 > 0 && k3 < byte1 - 1 && pathDirections[j3 - 1][k3 + 1] == 0 && (ai[j3 - 1][k3 + 1] & 0x1280138) == 0 && (ai[j3 - 1][k3] & 0x1280108) == 0 && (ai[j3][k3 + 1] & 0x1280120) == 0) {
				pathTileX[l3] = j3 - 1;
				pathTileY[l3] = k3 + 1;
				l3 = (l3 + 1) % j4;
				pathDirections[j3 - 1][k3 + 1] = 6;
				pathDistances[j3 - 1][k3 + 1] = l4;
			}
			if (j3 < byte0 - 1 && k3 < byte1 - 1 && pathDirections[j3 + 1][k3 + 1] == 0 && (ai[j3 + 1][k3 + 1] & 0x12801e0) == 0 && (ai[j3 + 1][k3] & 0x1280180) == 0 && (ai[j3][k3 + 1] & 0x1280120) == 0) {
				pathTileX[l3] = j3 + 1;
				pathTileY[l3] = k3 + 1;
				l3 = (l3 + 1) % j4;
				pathDirections[j3 + 1][k3 + 1] = 12;
				pathDistances[j3 + 1][k3 + 1] = l4;
			}
		}
		alternatePathFound = 0;
		if (!flag1) {
			if (flag) {
				int i5 = 100;
				for (int k5 = 1; k5 < 2; k5++) {
					for (int i6 = k2 - k5; i6 <= k2 + k5; i6++) {
						for (int l6 = i2 - k5; l6 <= i2 + k5; l6++) {
							if (i6 >= 0 && l6 >= 0 && i6 < 104 && l6 < 104 && pathDistances[i6][l6] < i5) {
								i5 = pathDistances[i6][l6];
								j3 = i6;
								k3 = l6;
								alternatePathFound = 1;
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
		pathTileX[i4] = j3;
		pathTileY[i4++] = k3;
		int l5;
		for (int j5 = l5 = pathDirections[j3][k3]; j3 != j2 || k3 != j1; j5 = pathDirections[j3][k3]) {
			if (j5 != l5) {
				l5 = j5;
				pathTileX[i4] = j3;
				pathTileY[i4++] = k3;
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
		// if(cancelWalk) { return i4 > 0; }

		if (i4 > 0) {
			int k4 = i4;
			if (k4 > 25) {
				k4 = 25;
			}
			i4--;
			int k6 = pathTileX[i4];
			int i7 = pathTileY[i4];
                   walkPacketCounter += k4;
                   if (walkPacketCounter >= 92) {
				stream.createFrame(36);
				stream.writeDWord(0);
                               walkPacketCounter = 0;
			}
			if (i == 0) {
				stream.createFrame(164);
				stream.writeWordBigEndian(k4 + k4 + 3);
			}
			if (i == 1) {
				stream.createFrame(248);
				stream.writeWordBigEndian(k4 + k4 + 3 + 14);
			}
			if (i == 2) {
				stream.createFrame(98);
				stream.writeWordBigEndian(k4 + k4 + 3);
			}
			stream.writeShortLEA(k6 + baseX);
			destX = pathTileX[0];
			destY = pathTileY[0];
			for (int j7 = 1; j7 < k4; j7++) {
				i4--;
				stream.writeWordBigEndian(pathTileX[i4] - k6);
				stream.writeWordBigEndian(pathTileY[i4] - i7);
			}

			stream.writeShortLEDup(i7 + baseY);
                        stream.writeByteNeg(super.keyArray[5] != 1 ? 0 : 1);
			return true;
		}
		return i != 1;
	}
	
       static final int adjustColorBrightness(int i, int i_1_) {
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

       public void processNpcUpdateMasks(Stream stream) {
		for (int j = 0; j < playerUpdateCount; j++) {
			int k = playerUpdateIndices[j];
			NPC npc = npcArray[k];
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
				npc.updateHitData(j2, j1, loopCycle);
				npc.loopCycleStatus = loopCycle + 300;
				npc.currentHealth = stream.readUnsignedByteA();
				npc.maxHealth = stream.readUnsignedByte();
			}
			if ((l & 0x80) != 0) {
                               npc.spotAnimId = stream.readUnsignedWord();
				int k1 = stream.readDWord();
                               npc.spotAnimHeight = k1 >> 16;
                               npc.spotAnimStartTick = loopCycle + (k1 & 0xffff);
                               npc.spotAnimFrame = 0;
                               npc.spotAnimFrameCycle = 0;
                               if (npc.spotAnimStartTick > loopCycle) {
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
				npc.updateHitData(k2, l1, loopCycle);
				npc.loopCycleStatus = loopCycle + 300;
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

	public void buildAtNPCMenu(EntityDef entityDef, int i, int j, int k) {
		if (menuActionRow >= 400) {
			return;
		}
                if (entityDef.childrenIDs != null) {
                        entityDef = entityDef.transform();
                }
		if (entityDef == null) {
			return;
		}
                if (!entityDef.clickable) {
			return;
		}
		String s = entityDef.name;
		if (entityDef.combatLevel != 0) {
			s = s + combatDiffColor(myPlayer.combatLevel, entityDef.combatLevel) + " (level-" + entityDef.combatLevel + ")";
		}
		if (itemSelected == 1) {
			menuActionName[menuActionRow] = "Use " + selectedItemName + " with @yel@" + s;
			menuActionID[menuActionRow] = 582;
			menuActionCmd1[menuActionRow] = i;
			menuActionCmd2[menuActionRow] = k;
			menuActionCmd3[menuActionRow] = j;
			menuActionRow++;
			return;
		}
		if (spellSelected == 1) {
			if ((spellUsableOn & 2) == 2) {
				menuActionName[menuActionRow] = spellTooltip + " @yel@" + s;
				menuActionID[menuActionRow] = 413;
				menuActionCmd1[menuActionRow] = i;
				menuActionCmd2[menuActionRow] = k;
				menuActionCmd3[menuActionRow] = j;
				menuActionRow++;
			}
		} else {
			if (entityDef.actions != null) {
				for (int l = 4; l >= 0; l--) {
					if (entityDef.actions[l] != null && !entityDef.actions[l].equalsIgnoreCase("attack")) {
						menuActionName[menuActionRow] = entityDef.actions[l] + " @yel@" + s;
						if (l == 0) {
							menuActionID[menuActionRow] = 20;
						}
						if (l == 1) {
							menuActionID[menuActionRow] = 412;
						}
						if (l == 2) {
							menuActionID[menuActionRow] = 225;
						}
						if (l == 3) {
							menuActionID[menuActionRow] = 965;
						}
						if (l == 4) {
							menuActionID[menuActionRow] = 478;
						}
						menuActionCmd1[menuActionRow] = i;
						menuActionCmd2[menuActionRow] = k;
						menuActionCmd3[menuActionRow] = j;
						menuActionRow++;
					}
				}

			}
			if (entityDef.actions != null) {
				for (int i1 = 4; i1 >= 0; i1--) {
					if (entityDef.actions[i1] != null && entityDef.actions[i1].equalsIgnoreCase("attack")) {
						char c = '\0';
						if (entityDef.combatLevel > myPlayer.combatLevel) {
							c = '\u07D0';
						}
						menuActionName[menuActionRow] = entityDef.actions[i1] + " @yel@" + s;
						if (i1 == 0) {
							menuActionID[menuActionRow] = 20 + c;
						}
						if (i1 == 1) {
							menuActionID[menuActionRow] = 412 + c;
						}
						if (i1 == 2) {
							menuActionID[menuActionRow] = 225 + c;
						}
						if (i1 == 3) {
							menuActionID[menuActionRow] = 965 + c;
						}
						if (i1 == 4) {
							menuActionID[menuActionRow] = 478 + c;
						}
						menuActionCmd1[menuActionRow] = i;
						menuActionCmd2[menuActionRow] = k;
						menuActionCmd3[menuActionRow] = j;
						menuActionRow++;
					}
				}

			}
			menuActionName[menuActionRow] = "Examine @yel@" + s + (showInfo ? " @gre@(@whi@" + entityDef.type + "@gre@)" : "");
			menuActionID[menuActionRow] = 1025;
			menuActionCmd1[menuActionRow] = i;
			menuActionCmd2[menuActionRow] = k;
			menuActionCmd3[menuActionRow] = j;
			menuActionRow++;
		}
	}

	public void buildAtPlayerMenu(int i, int j, Player player, int k) {
		if (player == myPlayer) {
			return;
		}
		if (menuActionRow >= 400) {
			return;
		}
		String s;
		if (player.skill == 0) {
			if (player.combatLevel > 0) {
				s = player.name + combatDiffColor(myPlayer.combatLevel, player.combatLevel) + " (level-" + player.combatLevel + ")";
			} else {
				s = player.name + " @cya@(store)";
			}
		} else {
			s = player.name + " (skill-" + player.skill + ")";
		}
		if (itemSelected == 1) {
			menuActionName[menuActionRow] = "Use " + selectedItemName + " with @whi@" + s;
			menuActionID[menuActionRow] = 491;
			menuActionCmd1[menuActionRow] = j;
			menuActionCmd2[menuActionRow] = i;
			menuActionCmd3[menuActionRow] = k;
			menuActionRow++;
		} else if (spellSelected == 1) {
			if ((spellUsableOn & 8) == 8) {
				menuActionName[menuActionRow] = spellTooltip + " @whi@" + s;
				menuActionID[menuActionRow] = 365;
				menuActionCmd1[menuActionRow] = j;
				menuActionCmd2[menuActionRow] = i;
				menuActionCmd3[menuActionRow] = k;
				menuActionRow++;
			}
		} else {
			for (int l = 4; l >= 0; l--) {
				if (atPlayerActions[l] != null) {
					menuActionName[menuActionRow] = atPlayerActions[l] + " @whi@" + s;
					char c = '\0';
					if (atPlayerActions[l].equalsIgnoreCase("attack")) {
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
					} else if (atPlayerArray[l]) {
						c = '\u07D0';
					}
					if (l == 0) {
						menuActionID[menuActionRow] = 561 + c;
					}
					if (l == 1) {
						menuActionID[menuActionRow] = 779 + c;
					}
					if (l == 2) {
						menuActionID[menuActionRow] = 27 + c;
					}
					if (l == 3) {
						menuActionID[menuActionRow] = 577 + c;
					}
					if (l == 4) {
						menuActionID[menuActionRow] = 729 + c;
					}
					menuActionCmd1[menuActionRow] = j;
					menuActionCmd2[menuActionRow] = i;
					menuActionCmd3[menuActionRow] = k;
					menuActionRow++;
				}
			}

		}
		for (int i1 = 0; i1 < menuActionRow; i1++) {
			if (menuActionID[i1] == 516) {
				menuActionName[i1] = "Walk here @whi@" + s;
				return;
			}
		}

	}

       public void locateSceneObject(PendingSpawn class30_sub1) {
		int i = 0;
		int j = -1;
		int k = 0;
		int l = 0;
		if (class30_sub1.category == 0) {
			i = worldController.getBoundaryObjectUid(class30_sub1.plane, class30_sub1.x, class30_sub1.y);
		}
		if (class30_sub1.category == 1) {
			i = worldController.getWallDecorationUid(class30_sub1.plane, class30_sub1.x, class30_sub1.y);
		}
		if (class30_sub1.category == 2) {
			i = worldController.getSceneObjectUid(class30_sub1.plane, class30_sub1.x, class30_sub1.y);
		}
		if (class30_sub1.category == 3) {
			i = worldController.getTileDecorationUid(class30_sub1.plane, class30_sub1.x, class30_sub1.y);
		}
		if (i != 0) {
			int i1 = worldController.getObjectConfig(class30_sub1.plane, class30_sub1.x, class30_sub1.y, i);
			j = i >> 14 & 0x7fff;
			k = i1 & 0x1f;
			l = i1 >> 6;
		}
		class30_sub1.oldId = j;
		class30_sub1.oldOrientation = k;
		class30_sub1.oldType = l;
	}

       public final void processSoundQueue() {
		for (int index = 0; index < currentSound; index++) {
			//if (soundDelay[index] <= 0) {
				boolean flag1 = false;
				try {
                                Stream stream = Sounds.createSoundStream(soundType[index], sound[index]);
					new SoundPlayer((InputStream) new ByteArrayInputStream(stream.buffer, 0, stream.currentOffset), soundVolume[index], soundDelay[index]);
					if (System.currentTimeMillis() + (long) (stream.currentOffset / 22) > lastSoundUpdate + (long) (soundBufferOffset / 22)) {
						soundBufferOffset = stream.currentOffset;
						lastSoundUpdate = System.currentTimeMillis();
					}
				} catch (Exception exception) {
					exception.printStackTrace();
				}
				if (!flag1 || soundDelay[index] == -5) {
					currentSound--;
					for (int j = index; j < currentSound; j++) {
						sound[j] = sound[j + 1];
						soundType[j] = soundType[j + 1];
						soundDelay[j] = soundDelay[j + 1];
						soundVolume[j] = soundVolume[j + 1];
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
                               playSong(musicVolume, false, currentSong);
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
					expectedCRCs[i1] = class30_sub2_sub2.readDWord();

				int j1 = class30_sub2_sub2.readDWord();
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
				useJaggrab = !useJaggrab;
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
		initialLoadComplete = true;
		boolean flag = true;
		if (!flag) {
			genericLoadingError = true;
			return;
		}
		if (Signlink.cache_dat != null) {
			for (int i = 0; i < 5; i++) {
				decompressors[i] = new Decompressor(Signlink.cache_dat, Signlink.cache_idx[i], i + 1);
			}

		}
		try {
			connectServer();
			titleStreamLoader = streamLoaderForName(1, "title screen", "title", expectedCRCs[1], 25);
			plainFont = new TextDrawingArea(false, "p11_full", titleStreamLoader);
			boldFont = new TextDrawingArea(false, "p12_full", titleStreamLoader);
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
			tileFlags = new byte[4][104][104];
			tileHeights = new int[4][105][105];
			worldController = new WorldController(tileHeights);
			for (int j = 0; j < 4; j++) {
				collisionMaps[j] = new CollisionMap();
			}

                   minimapImage = new Sprite(512, 512);
			StreamLoader streamLoader_6 = streamLoaderForName(5, "update list", "versionlist", expectedCRCs[5], 60);
			drawLoadingText(60, "Connecting to update server");
			onDemandFetcher = new OnDemandFetcher();
			onDemandFetcher.start(streamLoader_6, this);
//			musics();
                        AnimFrame.init(onDemandFetcher.getAnimCount());
			Model.init(onDemandFetcher.getVersionCount(0), onDemandFetcher);
			if (!lowMem) {
                               queueSong(10, musicVolume, false, 0);
				while (onDemandFetcher.getNodeCount() > 0) {
					processOnDemandQueue();
					try {
						Thread.sleep(100L);
					} catch (Exception _ex) {
					}
                                        if (onDemandFetcher.socketErrorCount > 3) {
						loadError();
						return;
					}
				}
			}
			drawLoadingText(65, "Requesting animations");
			int k = onDemandFetcher.getVersionCount(1);
			for (int i1 = 0; i1 < k; i1++) {
                                onDemandFetcher.queueRequest(1, i1);
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
                                if (onDemandFetcher.socketErrorCount > 3) {
					loadError();
					return;
				}
			}
			drawLoadingText(70, "Requesting models");
			k = onDemandFetcher.getVersionCount(0);
			for (int k1 = 0; k1 < k; k1++) {
				int l1 = onDemandFetcher.getModelIndex(k1);
				if ((l1 & 1) != 0) {
                                        onDemandFetcher.queueRequest(0, k1);
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
			if (decompressors[0] != null) {
				drawLoadingText(75, "Requesting maps");
                                onDemandFetcher.queueRequest(3, onDemandFetcher.getRegionArchiveId(0, 48, 47));
                                onDemandFetcher.queueRequest(3, onDemandFetcher.getRegionArchiveId(1, 48, 47));
                                onDemandFetcher.queueRequest(3, onDemandFetcher.getRegionArchiveId(0, 48, 48));
                                onDemandFetcher.queueRequest(3, onDemandFetcher.getRegionArchiveId(1, 48, 48));
                                onDemandFetcher.queueRequest(3, onDemandFetcher.getRegionArchiveId(0, 48, 49));
                                onDemandFetcher.queueRequest(3, onDemandFetcher.getRegionArchiveId(1, 48, 49));
                                onDemandFetcher.queueRequest(3, onDemandFetcher.getRegionArchiveId(0, 47, 47));
                                onDemandFetcher.queueRequest(3, onDemandFetcher.getRegionArchiveId(1, 47, 47));
                                onDemandFetcher.queueRequest(3, onDemandFetcher.getRegionArchiveId(0, 47, 48));
                                onDemandFetcher.queueRequest(3, onDemandFetcher.getRegionArchiveId(1, 47, 48));
                                onDemandFetcher.queueRequest(3, onDemandFetcher.getRegionArchiveId(0, 148, 48));
                                onDemandFetcher.queueRequest(3, onDemandFetcher.getRegionArchiveId(1, 148, 48));
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
                                        onDemandFetcher.validateOrQueue(byte0, 0, k2);
                                }
                        }

                        onDemandFetcher.requestMapFiles(isMembers);
                        if (!lowMem) {
                                int l = onDemandFetcher.getVersionCount(2);
                                for (int i3 = 1; i3 < l; i3++) {
                                        if (onDemandFetcher.isMidiRequired(i3)) {
                                                onDemandFetcher.validateOrQueue((byte) 1, 2, i3);
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
			mapEdge = new Sprite(streamLoader_2, "mapedge", 0);
			mapEdge.crop();

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
			scrollBar1 = new Background(streamLoader_2, "scrollbar", 0);
			scrollBar2 = new Background(streamLoader_2, "scrollbar", 1);
			redStone1 = new Background(streamLoader_2, "redstone1", 0);
			redStone2 = new Background(streamLoader_2, "redstone2", 0);
			redStone3 = new Background(streamLoader_2, "redstone3", 0);
			redStone1_2 = new Background(streamLoader_2, "redstone1", 0);
                  redStone1_2.flipHorizontal();
			redStone2_2 = new Background(streamLoader_2, "redstone2", 0);
                  redStone2_2.flipHorizontal();
			redStone1_3 = new Background(streamLoader_2, "redstone1", 0);
                  redStone1_3.flipVertical();
			redStone2_3 = new Background(streamLoader_2, "redstone2", 0);
                  redStone2_3.flipVertical();
			redStone3_2 = new Background(streamLoader_2, "redstone3", 0);
                  redStone3_2.flipVertical();
			redStone1_4 = new Background(streamLoader_2, "redstone1", 0);
                  redStone1_4.flipHorizontal();
                  redStone1_4.flipVertical();
			redStone2_4 = new Background(streamLoader_2, "redstone2", 0);
                  redStone2_4.flipHorizontal();
                  redStone2_4.flipVertical();
			for (int l4 = 0; l4 < 2; l4++) {
				modIcons[l4] = new Background(streamLoader_2, "mod_icons", l4);
			}

			Sprite sprite = new Sprite(streamLoader_2, "backleft1", 0);
			backLeftIP1 = new RSImageProducer(sprite.width, sprite.height, getGameComponent());
			sprite.drawSprite(0, 0);
			sprite = new Sprite(streamLoader_2, "backleft2", 0);
			backLeftIP2 = new RSImageProducer(sprite.width, sprite.height, getGameComponent());
			sprite.drawSprite(0, 0);
			sprite = new Sprite(streamLoader_2, "backright1", 0);
			backRightIP1 = new RSImageProducer(sprite.width, sprite.height, getGameComponent());
			sprite.drawSprite(0, 0);
			sprite = new Sprite(streamLoader_2, "backright2", 0);
			backRightIP2 = new RSImageProducer(sprite.width, sprite.height, getGameComponent());
			sprite.drawSprite(0, 0);
			sprite = new Sprite(streamLoader_2, "backtop1", 0);
			backTopIP1 = new RSImageProducer(sprite.width, sprite.height, getGameComponent());
			sprite.drawSprite(0, 0);
			sprite = new Sprite(streamLoader_2, "backvmid1", 0);
			backVmidIP1 = new RSImageProducer(sprite.width, sprite.height, getGameComponent());
			sprite.drawSprite(0, 0);
			sprite = new Sprite(streamLoader_2, "backvmid2", 0);
			backVmidIP2 = new RSImageProducer(sprite.width, sprite.height, getGameComponent());
			sprite.drawSprite(0, 0);
			sprite = new Sprite(streamLoader_2, "backvmid3", 0);
			backVmidIP3 = new RSImageProducer(sprite.width, sprite.height, getGameComponent());
			sprite.drawSprite(0, 0);
			sprite = new Sprite(streamLoader_2, "backhmid2", 0);
			midSubscreenBuffer = new RSImageProducer(sprite.width, sprite.height, getGameComponent());
			sprite.drawSprite(0, 0);
			int i5 = (int) (Math.random() * 21D) - 10;
			int j5 = (int) (Math.random() * 21D) - 10;
			int k5 = (int) (Math.random() * 21D) - 10;
			int l5 = (int) (Math.random() * 41D) - 20;
			for (int i6 = 0; i6 < 100; i6++) {
				if (mapFunctions[i6] != null) {
					mapFunctions[i6].adjustRgb(i5 + l5, j5 + l5, k5 + l5);
				}
				if (mapScenes[i6] != null) {
                                        mapScenes[i6].adjustPalette(i5 + l5, j5 + l5, k5 + l5);
				}
			}

			drawLoadingText(83, "Unpacking textures");
			Texture.loadTextures(streamLoader_3);
			Texture.setBrightness(0.80000000000000004D);
			Texture.initCache();
			drawLoadingText(86, "Unpacking config");
			Animation.unpackConfig(streamLoader);
			ObjectDef.unpackConfig(streamLoader);
			FloorOverlay.unpackConfig(streamLoader);
			ItemDef.unpackConfig(streamLoader);
			EntityDef.unpackConfig(streamLoader);
			IDK.unpackConfig(streamLoader);
			SpotAnim.unpackConfig(streamLoader);
			Varp.unpackConfig(streamLoader);
			VarBit.unpackConfig(streamLoader);
			ItemDef.isMembers = isMembers;
			// if (!lowMem) {
			drawLoadingText(90, "Unpacking sounds");
			byte abyte0[] = streamLoader_5.getFileData("sounds.dat");
			Stream stream = new Stream(abyte0);
			Sounds.unpack(stream);
			// }
			drawLoadingText(95, "Unpacking interfaces");
			TextDrawingArea aclass30_sub2_sub1_sub4s[] = {plainFont, boldFont, chatTextDrawingArea, aTextDrawingArea_1273};
			RSInterface.unpack(streamLoader_1, aclass30_sub2_sub1_sub4s, streamLoader_2);
			drawLoadingText(100, "Preparing game engine");
			for (int j6 = 0; j6 < 33; j6++) {
				int k6 = 999;
				int i7 = 0;
				for (int k7 = 0; k7 < 34; k7++) {
					if (mapBack.pixels[k7 + j6 * mapBack.width] == 0) {
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

				mapBackLeft[j6] = k6;
				mapBackWidths[j6] = i7 - k6;
			}

			for (int l6 = 5; l6 < 156; l6++) {
				int j7 = 999;
				int l7 = 0;
				for (int j8 = 25; j8 < 172; j8++) {
					if (mapBack.pixels[j8 + l6 * mapBack.width] == 0 && (j8 > 34 || l6 > 34)) {
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

				minimapLineOffset[l6 - 5] = j7 - 25;
				minimapLineLengths[l6 - 5] = l7 - j7;
			}
			Texture.resize(765, 503);
			gameScreenOffsets = Texture.lineOffsets;
			Texture.resize(479, 96);
			chatAreaOffsets = Texture.lineOffsets;
			Texture.resize(190, 261);
			tabAreaOffsets = Texture.lineOffsets;
			Texture.resize(512, 334);
			chatBoxAreaOffsets = Texture.lineOffsets;
			int ai[] = new int[9];
			for (int i8 = 0; i8 < 9; i8++) {
				int k8 = 128 + i8 * 32 + 15;
				int l8 = 600 + k8 * 3;
				int i9 = Texture.sineTable[k8];
				ai[i8] = l8 * i9 >> 16;
			}

                        WorldController.buildVisibilityMap(500, 800, 512, 334, ai);
			Censor.loadConfig(streamLoader_4);
			mouseDetection = new MouseDetection(this);
			startRunnable(mouseDetection, 10);
			DynamicObject.client = this;
			ObjectDef.clientInstance = this;
			EntityDef.clientInstance = this;
			
			if (myUsername != "" && myPassword != "")
				login(myUsername, myPassword, false);
			return;
		} catch (Exception exception) {
			Signlink.reporterror("loaderror " + errorMessage + " " + loadingPercent);
		}
		loadingError = true;
	}

       public void addLocalPlayers(Stream stream, int i) {
		while (stream.bitPosition + 10 < i * 8) {
			int j = stream.readBits(11);
			if (j == 2047) {
				break;
			}
			if (playerArray[j] == null) {
				playerArray[j] = new Player();
				if (playerBuffers[j] != null) {
					playerArray[j].updatePlayer(playerBuffers[j]);
				}
			}
			playerIndices[playerCount++] = j;
			Player player = playerArray[j];
			player.lastUpdateCycle = loopCycle;
			int k = stream.readBits(1);
			if (k == 1) {
				playerUpdateIndices[playerUpdateCount++] = j;
			}
			int l = stream.readBits(1);
			int i1 = stream.readBits(5);
			if (i1 > 15) {
				i1 -= 32;
			}
			int j1 = stream.readBits(5);
			if (j1 > 15) {
				j1 -= 32;
			}
			player.setPos(myPlayer.smallX[0] + j1, myPlayer.smallY[0] + i1, l == 1);
		}
		stream.finishBitAccess();
	}

	public void processMainScreenClick() {
		if (minimapState != 0) {
			return;
		}
		if (super.clickMode3 == 1) {
			int i = super.saveClickX - 25 - 550;
			int j = super.saveClickY - 5 - 4;
			if (i >= 0 && j >= 0 && i < 146 && j < 151) {
				i -= 73;
				j -= 75;
				int k = cameraYaw + minimapRotationOffset & 0x7ff;
				int i1 = Texture.sineTable[k];
				int j1 = Texture.cosineTable[k];
				i1 = i1 * (minimapZoom + 256) >> 8;
				j1 = j1 * (minimapZoom + 256) >> 8;
				int k1 = j * i1 + i * j1 >> 11;
				int l1 = j * j1 - i * i1 >> 11;
				int i2 = myPlayer.x + k1 >> 7;
				int j2 = myPlayer.y - l1 >> 7;
				boolean flag1 = doWalkTo(1, 0, 0, 0, myPlayer.smallY[0], 0, 0, j2, myPlayer.smallX[0], true, i2);
				if (flag1) {
					stream.writeWordBigEndian(i);
					stream.writeWordBigEndian(j);
					stream.writeWord(cameraYaw);
					stream.writeWordBigEndian(57);
					stream.writeWordBigEndian(minimapRotationOffset);
					stream.writeWordBigEndian(minimapZoom);
					stream.writeWordBigEndian(89);
					stream.writeWord(myPlayer.x);
					stream.writeWord(myPlayer.y);
					stream.writeWordBigEndian(alternatePathFound);
					stream.writeWordBigEndian(63);
				}
			}
			antiCheatPacketCounter++;
			if (antiCheatPacketCounter > 1151) {
				antiCheatPacketCounter = 0;
				stream.createFrame(246);
				stream.writeWordBigEndian(0);
				int l = stream.currentOffset;
				if ((int) (Math.random() * 2D) == 0) {
					stream.writeWordBigEndian(101);
				}
				stream.writeWordBigEndian(197);
				stream.writeWord((int) (Math.random() * 65536D));
				stream.writeWordBigEndian((int) (Math.random() * 256D));
				stream.writeWordBigEndian(67);
				stream.writeWord(14214);
				if ((int) (Math.random() * 2D) == 0) {
					stream.writeWord(29487);
				}
				stream.writeWord((int) (Math.random() * 65536D));
				if ((int) (Math.random() * 2D) == 0) {
					stream.writeWordBigEndian(220);
				}
				stream.writeWordBigEndian(180);
				stream.writeBytes(stream.currentOffset - l);
			}
		}
	}

	public String interfaceIntToString(int j) {
		if (j < 0x3b9ac9ff) {
			return String.valueOf(j);
		} else {
			return "*";
		}
	}

	public void showErrorScreen() {
		Graphics g = getGameComponent().getGraphics();
		g.setColor(Color.black);
		g.fillRect(0, 0, 765, 503);
                setFrameRate(1);
		if (loadingError) {
			flameThreadActive = false;
			g.setFont(new Font("Helvetica", 1, 16));
			g.setColor(Color.yellow);
			int k = 35;
			g.drawString("Sorry, an error has occured whilst loading " + ClientSettings.SERVER_NAME + "", 30, k);
			k += 50;
			g.setColor(Color.white);
			g.drawString("To fix this try the following (in order):", 30, k);
			k += 50;
			g.setColor(Color.white);
			g.setFont(new Font("Helvetica", 1, 12));
			g.drawString("1: Try closing ALL open web-browser windows, and reloading", 30, k);
			k += 30;
			g.drawString("2: Try clearing your web-browsers cache from tools->internet options", 30, k);
			k += 30;
			g.drawString("3: Try using a different game-world", 30, k);
			k += 30;
			g.drawString("4: Try rebooting your computer", 30, k);
			k += 30;
			g.drawString("5: Try selecting a different version of Java from the play-game menu", 30, k);
		}
		if (genericLoadingError) {
			flameThreadActive = false;
			g.setFont(new Font("Helvetica", 1, 20));
			g.setColor(Color.white);
			g.drawString("Error - unable to load game!", 50, 50);
			g.drawString("To play " + ClientSettings.SERVER_NAME + " make sure you play from", 50, 100);
			g.drawString("" + ClientSettings.SERVER_WEBSITE + "", 50, 150);
		}
		if (rsAlreadyLoaded) {
			flameThreadActive = false;
			g.setColor(Color.yellow);
			int l = 35;
			g.drawString("Error a copy of " + ClientSettings.SERVER_NAME + " already appears to be loaded", 30, l);
			l += 50;
			g.setColor(Color.white);
			g.drawString("To fix this try the following (in order):", 30, l);
			l += 50;
			g.setColor(Color.white);
			g.setFont(new Font("Helvetica", 1, 12));
			g.drawString("1: Try closing ALL open web-browser windows, and reloading", 30, l);
			l += 30;
			g.drawString("2: Try rebooting your computer, and reloading", 30, l);
			l += 30;
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

       public void animateNpcs() {
		for (int j = 0; j < npcCount; j++) {
			int k = npcIndices[j];
			NPC npc = npcArray[k];
			if (npc != null) {
                                updateEntityMovement(npc);
			}
		}
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
		if (entity == myPlayer && (entity.x < 1536 || entity.y < 1536 || entity.x >= 11776 || entity.y >= 11776)) {
			entity.anim = -1;
                       entity.spotAnimId = -1;
			entity.forceMoveStartCycle = 0;
			entity.forceMoveEndCycle = 0;
			entity.x = entity.smallX[0] * 128 + entity.size * 64;
			entity.y = entity.smallY[0] * 128 + entity.size * 64;
                       entity.clearMovement();
		}
               if (entity.forceMoveStartCycle > loopCycle) {
                        updateForcedMovement(entity);
               } else if (entity.forceMoveEndCycle >= loopCycle) {
                       updateInterpolatedMovement(entity);
               } else {
                       updateWalkingStep(entity);
		}
           updateEntityFacing(entity);
           updateEntityAnimation(entity);
	}

       public void updateForcedMovement(Entity entity) {
		int i = entity.forceMoveStartCycle - loopCycle;
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

        public void updateInterpolatedMovement(Entity entity) {
        if (entity.forceMoveEndCycle == loopCycle || entity.anim == -1 || entity.graphicDelay != 0 || entity.graphicFrameCycle + 1 > Animation.anims[entity.anim].getFrameDelay(entity.graphicFrame)) {
			int i = entity.forceMoveEndCycle - entity.forceMoveStartCycle;
			int j = loopCycle - entity.forceMoveStartCycle;
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

        public void updateWalkingStep(Entity entity) {
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
		if (entity.aBooleanArray1553[entity.smallXYIndex - 1]) {
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

       public void updateEntityFacing(Entity entity) {
               if (entity.turnSpeed == 0) {
                       return;
               }
		if (entity.interactingEntity != -1 && entity.interactingEntity < 32768) {
			NPC npc = npcArray[entity.interactingEntity];
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
			if (j == localPlayerIndex) {
				j = myPlayerIndex;
			}
			Player player = playerArray[j];
			if (player != null) {
				int l1 = entity.x - player.x;
				int i2 = entity.y - player.y;
				if (l1 != 0 || i2 != 0) {
					entity.turnDirection = (int) (Math.atan2(l1, i2) * 325.94900000000001D) & 0x7ff;
				}
			}
		}
		if ((entity.focusX != 0 || entity.focusY != 0) && (entity.smallXYIndex == 0 || entity.movementDelay > 0)) {
			int k = entity.x - (entity.focusX - baseX - baseX) * 64;
			int j1 = entity.y - (entity.focusY - baseY - baseY) * 64;
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

       public void updateEntityAnimation(Entity entity) {
		entity.aBoolean1541 = false;
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
               if (entity.spotAnimId != -1 && loopCycle >= entity.spotAnimStartTick) {
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
			if (animation_2.precedenceAnimating == 1 && entity.animationDelay > 0 && entity.forceMoveStartCycle <= loopCycle && entity.forceMoveEndCycle < loopCycle) {
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
			entity.aBoolean1541 = animation_3.stretches;
		}
		if (entity.graphicDelay > 0) {
			entity.graphicDelay--;
		}
	}

	public void drawGameScreen() {
		if (fullScreenInterfaceId != -1 && (loadingStage == 2 || super.fullGameScreen != null)) {
			if (loadingStage == 2) {
				updateInterfaceAnimations(animationCycle, fullScreenInterfaceId);
				if (fullScreenInterfaceId != -1)
					updateInterfaceAnimations(animationCycle, fullScreenInterfaceId);
				animationCycle = 0;
				resetAllImageProducers();
				super.fullGameScreen.initDrawingArea();
				Texture.lineOffsets = gameScreenOffsets;
				DrawingArea.setAllPixelsToZero();
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
			midSubscreenBuffer.drawGraphics(338, super.graphics, 0);
			needDrawTabArea = true;
			inputTaken = true;
			tabAreaAltered = true;
			chatSettingsUpdateNeeded = true;
			if (loadingStage != 2) {
				tabAreaBuffer.drawGraphics(4, super.graphics, 4);
				chatBackground.drawGraphics(4, super.graphics, 550);
			}
		}
                if (loadingStage == 2) {
                        renderGameView();
                }
		if (menuOpen && menuScreenArea == 1) {
			needDrawTabArea = true;
		}
		if (invOverlayInterfaceID != -1) {
			boolean flag1 = updateInterfaceAnimations(animationCycle, invOverlayInterfaceID);
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
			chatScrollComponent.scrollPosition = chatScrollHeight - chatScrollPosition - 77;
			if (super.mouseX > 448 && super.mouseX < 560 && super.mouseY > 332) {
                               handleScrollbarInput(463, 77, super.mouseX - 17, super.mouseY - 357, chatScrollComponent, 0, false, chatScrollHeight);
			}
			int i = chatScrollHeight - 77 - chatScrollComponent.scrollPosition;
			if (i < 0) {
				i = 0;
			}
			if (i > chatScrollHeight - 77) {
				i = chatScrollHeight - 77;
			}
			if (chatScrollPosition != i) {
				chatScrollPosition = i;
				inputTaken = true;
			}
		}
		if (backDialogID != -1) {
			boolean flag2 = updateInterfaceAnimations(animationCycle, backDialogID);
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
		if (messagePrompt != null) {
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
			chatBackground.drawGraphics(4, super.graphics, 550);
		}
		if (flashingTabId != -1) {
			tabAreaAltered = true;
		}
		if (tabAreaAltered) {
			if (flashingTabId != -1 && flashingTabId == tabID) {
				flashingTabId = -1;
				stream.createFrame(120);
				stream.writeWordBigEndian(tabID);
			}
			tabAreaAltered = false;
			mapEdgeBuffer.initDrawingArea();
			backHmid1.draw(0, 0);
			if (invOverlayInterfaceID == -1) {
				if (tabInterfaceIDs[tabID] != -1) {
					if (tabID == 0) {
						redStone1.draw(22, 10);
					}
					if (tabID == 1) {
						redStone2.draw(54, 8);
					}
					if (tabID == 2) {
						redStone2.draw(82, 8);
					}
					if (tabID == 3) {
						redStone3.draw(110, 8);
					}
					if (tabID == 4) {
						redStone2_2.draw(153, 8);
					}
					if (tabID == 5) {
						redStone2_2.draw(181, 8);
					}
					if (tabID == 6) {
						redStone1_2.draw(209, 9);
					}
				}
				if (tabInterfaceIDs[0] != -1 && (flashingTabId != 0 || loopCycle % 20 < 10)) {
					sideIcons[0].draw(29, 13);
				}
				if (tabInterfaceIDs[1] != -1 && (flashingTabId != 1 || loopCycle % 20 < 10)) {
					sideIcons[1].draw(53, 11);
				}
				if (tabInterfaceIDs[2] != -1 && (flashingTabId != 2 || loopCycle % 20 < 10)) {
					sideIcons[2].draw(82, 11);
				}
				if (tabInterfaceIDs[3] != -1 && (flashingTabId != 3 || loopCycle % 20 < 10)) {
					sideIcons[3].draw(115, 12);
				}
				if (tabInterfaceIDs[4] != -1 && (flashingTabId != 4 || loopCycle % 20 < 10)) {
					sideIcons[4].draw(153, 13);
				}
				if (tabInterfaceIDs[5] != -1 && (flashingTabId != 5 || loopCycle % 20 < 10)) {
					sideIcons[5].draw(180, 11);
				}
				if (tabInterfaceIDs[6] != -1 && (flashingTabId != 6 || loopCycle % 20 < 10)) {
					sideIcons[6].draw(208, 13);
				}
			}
			mapEdgeBuffer.drawGraphics(160, super.graphics, 516);
			tabAreaBackgroundBuffer.initDrawingArea();
			backBase2.draw(0, 0);
			if (invOverlayInterfaceID == -1) {
				if (tabInterfaceIDs[tabID] != -1) {
					if (tabID == 7 && ClientSettings.CUSTOM_SETTINGS_TAB) {
						redStone1_3.draw(42, 0);
					}
					if (tabID == 8) {
						redStone2_3.draw(74, 0);
					}
					if (tabID == 9) {
						redStone2_3.draw(102, 0);
					}
					if (tabID == 10) {
						redStone3_2.draw(130, 1);
					}
					if (tabID == 11) {
						redStone2_4.draw(173, 0);
					}
					if (tabID == 12) {
						redStone2_4.draw(201, 0);
					}
					if (tabID == 13) {
						redStone1_4.draw(229, 0);
					}
				}
				if (tabInterfaceIDs[7] != -1 && (flashingTabId != 7 || loopCycle % 20 < 10) && ClientSettings.CUSTOM_SETTINGS_TAB) {
					sideIcons[10].draw(47, 2);
				}
				if (tabInterfaceIDs[8] != -1 && (flashingTabId != 8 || loopCycle % 20 < 10)) {
					sideIcons[7].draw(74, 2);
				}
				if (tabInterfaceIDs[9] != -1 && (flashingTabId != 9 || loopCycle % 20 < 10)) {
					sideIcons[8].draw(102, 3);
				}
				if (tabInterfaceIDs[10] != -1 && (flashingTabId != 10 || loopCycle % 20 < 10)) {
					sideIcons[9].draw(137, 4);
				}
				if (tabInterfaceIDs[11] != -1 && (flashingTabId != 11 || loopCycle % 20 < 10)) {
					sideIcons[10].draw(174, 2);
				}
				if (tabInterfaceIDs[12] != -1 && (flashingTabId != 12 || loopCycle % 20 < 10)) {
					sideIcons[11].draw(201, 2);
				}
				if (tabInterfaceIDs[13] != -1 && (flashingTabId != 13 || loopCycle % 20 < 10)) {
					sideIcons[12].draw(226, 2);
				}
			}
			tabAreaBackgroundBuffer.drawGraphics(466, super.graphics, 496);
			tabAreaBuffer.initDrawingArea();
			Texture.lineOffsets = chatBoxAreaOffsets;
		}
		if (chatSettingsUpdateNeeded) {
			chatSettingsUpdateNeeded = false;
			tabAreaIconBuffer.initDrawingArea();
			backBase1.draw(0, 0);
			boldFont.textCenterShadow(0xffffff, 55, "Public chat", 28, true);
			if (publicChatMode == 0) {
				boldFont.textCenterShadow(0x00ff00, 55, "On", 41, true);
			}
			if (publicChatMode == 1) {
				boldFont.textCenterShadow(0xffff00, 55, "Friends", 41, true);
			}
			if (publicChatMode == 2) {
				boldFont.textCenterShadow(0xff0000, 55, "Off", 41, true);
			}
			if (publicChatMode == 3) {
				boldFont.textCenterShadow(0x00ffff, 55, "Hide", 41, true);
			}
			boldFont.textCenterShadow(0xffffff, 184, "Private chat", 28, true);
			if (privateChatMode == 0) {
				boldFont.textCenterShadow(0x00ff00, 184, "On", 41, true);
			}
			if (privateChatMode == 1) {
				boldFont.textCenterShadow(0xffff00, 184, "Friends", 41, true);
			}
			if (privateChatMode == 2) {
				boldFont.textCenterShadow(0xff0000, 184, "Off", 41, true);
			}
			boldFont.textCenterShadow(0xffffff, 324, "Trade/compete", 28, true);
			if (tradeMode == 0) {
				boldFont.textCenterShadow(0x00ff00, 324, "On", 41, true);
			}
			if (tradeMode == 1) {
				boldFont.textCenterShadow(0xffff00, 324, "Friends", 41, true);
			}
			if (tradeMode == 2) {
				boldFont.textCenterShadow(0xff0000, 324, "Off", 41, true);
			}
			boldFont.textCenterShadow(0xffffff, 458, "Report abuse", 33, true);
			tabAreaIconBuffer.drawGraphics(453, super.graphics, 0);
			tabAreaBuffer.initDrawingArea();
			Texture.lineOffsets = chatBoxAreaOffsets;
		}
		animationCycle = 0;
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

       public void processGraphicsObjects() {
                GraphicsObject graphicsObject = (GraphicsObject) graphicsObjectList.reverseGetFirst();
                for (; graphicsObject != null; graphicsObject = (GraphicsObject) graphicsObjectList.reverseGetNext()) {
                        if (graphicsObject.plane != plane || graphicsObject.finished) {
                                graphicsObject.unlink();
                        } else if (loopCycle >= graphicsObject.endCycle) {
                                graphicsObject.update(animationCycle);
                                if (graphicsObject.finished) {
                                        graphicsObject.unlink();
                                } else {
                                       worldController.addAnimableObject(graphicsObject.plane, 0, graphicsObject.height, -1, graphicsObject.y, 60, graphicsObject.x, graphicsObject, false);
                                }
                        }
                }

	}

	public void drawInterface(int scrollPos, int k, RSInterface class9, int l) {
		if (class9.type != 0 || class9.children == null) {
			return;
		}
		if (class9.aBoolean266 && lastHoveredWidgetId != class9.id && hoveredTabId != class9.id && lastInteractionId != class9.id) {
			return;
		}
		int i1 = DrawingArea.topX;
		int j1 = DrawingArea.topY;
		int k1 = DrawingArea.bottomX;
		int l1 = DrawingArea.bottomY;
		DrawingArea.setDrawingArea(l + class9.height, k, k + class9.width, l);
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
					drawScrollThumb(component.height, component.scrollPosition, l2, k2 + component.width, component.scrollMax);
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
							if (component.inv[i3] > 0) {
								int k6 = 0;
								int j7 = 0;
								int j9 = component.inv[i3] - 1;
								if (k5 > DrawingArea.topX - 32 && k5 < DrawingArea.bottomX && j6 > DrawingArea.topY - 32 && j6 < DrawingArea.bottomY || activeInterfaceType != 0 && draggedSlot == i3) {
									int l9 = 0;
									if (itemSelected == 1 && selectedItemSlot == i3 && selectedItemInterfaceId == component.id) {
										l9 = 0xffffff;
									}
									Sprite class30_sub2_sub1_sub1_2 = ItemDef.getSprite(j9, component.invStackSizes[i3], l9);
									if (class30_sub2_sub1_sub1_2 != null) {
										if (activeInterfaceType != 0 && draggedSlot == i3 && dragInterfaceId == component.id) {
											k6 = super.mouseX - dragStartX;
											j7 = super.mouseY - dragStartY;
											if (k6 < 5 && k6 > -5) {
												k6 = 0;
											}
											if (j7 < 5 && j7 > -5) {
												j7 = 0;
											}
											if (dragCounter < 5) {
												k6 = 0;
												j7 = 0;
											}
											// Draw item being moved
											class30_sub2_sub1_sub1_2.drawSprite1(k5 + k6, j6 + j7);
											if (j6 + j7 < DrawingArea.topY && class9.scrollPosition > 0) {
												int i10 = animationCycle * (DrawingArea.topY - j6 - j7) / 3;
												if (i10 > animationCycle * 10) {
													i10 = animationCycle * 10;
												}
												if (i10 > class9.scrollPosition) {
													i10 = class9.scrollPosition;
												}
												class9.scrollPosition -= i10;
												dragStartY += i10;
											}
											if (j6 + j7 + 32 > DrawingArea.bottomY && class9.scrollPosition < class9.scrollMax - class9.height) {
												int j10 = animationCycle * (j6 + j7 + 32 - DrawingArea.bottomY) / 3;
												if (j10 > animationCycle * 10) {
													j10 = animationCycle * 10;
												}
												if (j10 > class9.scrollMax - class9.height - class9.scrollPosition) {
													j10 = class9.scrollMax - class9.height - class9.scrollPosition;
												}
												class9.scrollPosition += j10;
												dragStartY -= j10;
											}
										} else if (atInventoryInterfaceType != 0 && atInventoryIndex == i3 && atInventoryInterface == component.id) {
											// Using item? wear/unequip etc
											class30_sub2_sub1_sub1_2.drawSprite1(k5, j6);
										} else {
											// Draw item in inventory/equipment etc
											class30_sub2_sub1_sub1_2.drawTransparentSprite(k5, j6);
										}
										if (class30_sub2_sub1_sub1_2.trimWidth == 33 || component.invStackSizes[i3] != 1) {
											// Draw item amounts
											int k10 = component.invStackSizes[i3];
											plainFont.textLeft(0, intToKOrMil(k10), j6 + 10 + j7, k5 + 1 + k6); // shadow
											plainFont.textLeft(0xffff00, intToKOrMil(k10), j6 + 9 + j7, k5 + k6); // top layer
										}
									}
								}
							} else if (component.sprites != null && i3 < 20) {
								Sprite class30_sub2_sub1_sub1_1 = component.sprites[i3];
								if (class30_sub2_sub1_sub1_1 != null) {
									// Empty slots in equipment
									class30_sub2_sub1_sub1_1.drawTransparentSprite(k5, j6);
								}
							}
							i3++;
						}

					}

				} else if (component.type == 3) {
					boolean flag = false;
					if (lastInteractionId == component.id || hoveredTabId == component.id || lastHoveredWidgetId == component.id) {
						flag = true;
					}
					int color;
					if (interfaceIsSelected(component)) {
						color = component.activeTextColor;
						if (flag && component.activeHoverTextColor != 0) {
							color = component.activeHoverTextColor;
						}
					} else {
						color = component.textColor;
						if (flag && component.hoverTextColor != 0) {
							color = component.hoverTextColor;
						}
					}
					if (component.aByte254 == 0) {
						if (component.aBoolean227) {
							DrawingArea.fillArea(component.height, l2, 0x2a251e, component.width, k2);
						} else {
							DrawingArea.fillPixels(l2, component.height, color, k2, component.width);
						}
					} else if (component.aBoolean227) {
						DrawingArea.fillArea(color, l2, component.width, component.height, 256 - (component.aByte254 & 0xff), k2);
					} else {
						DrawingArea.drawFrameRounded(l2, component.height, 256 - (component.aByte254 & 0xff), color, component.width, k2);
					}
				} else if (component.type == 4) {
					TextDrawingArea textDrawingArea = component.textDrawingAreas;
					String s = component.disabledText;
					boolean flag1 = false;
					if (lastInteractionId == component.id || hoveredTabId == component.id || lastHoveredWidgetId == component.id) {
						flag1 = true;
					}
					int i4;
					if (interfaceIsSelected(component)) {
						i4 = component.activeTextColor;
						if (flag1 && component.activeHoverTextColor != 0) {
							i4 = component.activeHoverTextColor;
						}
						if (component.enabledText.length() > 0) {
							s = component.enabledText;
						}
					} else {
						i4 = component.textColor;
						if (flag1 && component.hoverTextColor != 0) {
							i4 = component.hoverTextColor;
						}
					}
					if (component.atActionType == 6 && actionPending) {
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
                                        for (int l6 = l2 + textDrawingArea.fontHeight; s.length() > 0; l6 += textDrawingArea.fontHeight) {
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
						if (component.aBoolean223) {
							textDrawingArea.textCenterShadow(i4, k2 + component.width / 2, s1, l6, component.aBoolean268);
						} else {
							textDrawingArea.textLeftShadow(component.aBoolean268, k2, i4, s1, l6);
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
						sprite.drawTransparentSprite(k2, l2);
					}
				} else if (component.type == 6) {
					int k3 = Texture.textureInt1;
					int j4 = Texture.textureInt2;
					Texture.textureInt1 = k2 + component.width / 2;
					Texture.textureInt2 = l2 + component.height / 2;
                                        int i5 = Texture.sineTable[component.modelRotation1] * component.modelZoom >> 16;
                                        int l5 = Texture.cosineTable[component.modelRotation1] * component.modelZoom >> 16;
					boolean flag2 = interfaceIsSelected(component);
                                        int i7;
                                        if (flag2) {
                                                i7 = component.enabledAnimation;
                                        } else {
                                                i7 = component.disabledAnimation;
                                        }
					Model model;
					if (i7 == -1) {
                                                model = component.prepareModel(-1, -1, flag2);
					} else {
						Animation animation = Animation.anims[i7];
                                                model = component.prepareModel(animation.secondaryFrameIds[component.animationFrame], animation.frameIds[component.animationFrame], flag2);
					}
                                        if (model != null) {
                                                model.transformVertices(component.modelRotation2, 0, component.modelRotation1, 0, i5, l5);
                                        }
					Texture.textureInt1 = k3;
					Texture.textureInt2 = j4;
				} else if (component.type == 7) {
					TextDrawingArea textDrawingArea_1 = component.textDrawingAreas;
					int k4 = 0;
					for (int j5 = 0; j5 < component.height; j5++) {
						for (int i6 = 0; i6 < component.width; i6++) {
							if (component.inv[k4] > 0) {
								ItemDef itemDef = ItemDef.lookup(component.inv[k4] - 1);
								String s2 = itemDef.name;
								if (itemDef.stackable || component.invStackSizes[k4] != 1) {
									s2 = s2 + " x" + intToKOrMilLongName(component.invStackSizes[k4]);
								}
								int i9 = k2 + i6 * (115 + component.invSpritePadX);
								int k9 = l2 + j5 * (12 + component.invSpritePadY);
								if (component.aBoolean223) {
									textDrawingArea_1.textCenterShadow(component.textColor, i9 + component.width / 2, s2, k9, component.aBoolean268);
								} else {
									textDrawingArea_1.textLeftShadow(component.aBoolean268, i9, component.textColor, s2, k9);
								}
							}
							k4++;
						}

					}

				} else if(component.type == 8) {
					
				}
			}
		}

		DrawingArea.setDrawingArea(l1, i1, k1, j1);
	}

	public void randomizeBackground(Background background) {
		int j = 256;
		for (int k = 0; k < flameGradient1.length; k++) {
			flameGradient1[k] = 0;
		}

		for (int l = 0; l < 5000; l++) {
			int i1 = (int) (Math.random() * 128D * (double) j);
			flameGradient1[i1] = (int) (Math.random() * 256D);
		}

		for (int j1 = 0; j1 < 20; j1++) {
			for (int k1 = 1; k1 < j - 1; k1++) {
				for (int i2 = 1; i2 < 127; i2++) {
					int k2 = i2 + (k1 << 7);
					flameGradient2[k2] = (flameGradient1[k2 - 1] + flameGradient1[k2 + 1] + flameGradient1[k2 - 128] + flameGradient1[k2 + 128]) / 4;
				}

			}

			int ai[] = flameGradient1;
			flameGradient1 = flameGradient2;
			flameGradient2 = ai;
		}

		if (background != null) {
			int l1 = 0;
			for (int j2 = 0; j2 < background.height; j2++) {
				for (int l2 = 0; l2 < background.width; l2++) {
					if (background.pixels[l1++] != 0) {
						int i3 = l2 + 16 + background.offsetX;
						int j3 = j2 + 16 + background.offsetY;
						int k3 = i3 + (j3 << 7);
						flameGradient1[k3] = 0;
					}
				}

			}

		}
	}

        public void decodePlayerUpdateMask(int i, int j, Stream stream, Player player) {
		if ((i & 0x400) != 0) {
			player.forceMoveStartX = stream.readUnsignedByteSub();
			player.forceMoveStartY = stream.readUnsignedByteSub();
			player.forceMoveEndX = stream.readUnsignedByteSub();
			player.forceMoveEndY = stream.readUnsignedByteSub();
			player.forceMoveStartCycle = stream.readShortLEAdd() + loopCycle;
			player.forceMoveEndCycle = stream.readShortAdd() + loopCycle;
			player.forceMoveDirection = stream.readUnsignedByteSub();
                       player.clearMovement();
		}
		if ((i & 0x100) != 0) {
                       player.spotAnimId = stream.readShortLE();
                       int k = stream.readDWord();
                       player.spotAnimHeight = k >> 16;
                       player.spotAnimStartTick = loopCycle + (k & 0xffff);
                       player.spotAnimFrame = 0;
                       player.spotAnimFrameCycle = 0;
                       if (player.spotAnimStartTick > loopCycle) {
                               player.spotAnimFrame = -1;
                       }
                       if (player.spotAnimId == 0x00ffff) {
                               player.spotAnimId = -1;
                       }
			//processSound(player.anInt1520, 0, player, null);
		}
		if ((i & 8) != 0) {
			int l = stream.readShortLE();
			if (l == 0x00ffff) {
				l = -1;
			}
			//processSound(l, 1, player, null);
			int i2 = stream.readUnsignedByteNeg();
			if (l == player.anim && l != -1) {
				int i3 = Animation.anims[l].replayMode;
				if (i3 == 1) {
					player.graphicFrame = 0;
					player.graphicFrameCycle = 0;
					player.graphicDelay = i2;
					player.graphicCycle = 0;
				}
				if (i3 == 2) {
					player.graphicCycle = 0;
				}
			} else if (l == -1 || player.anim == -1 || Animation.anims[l].priority >= Animation.anims[player.anim].priority) {
				player.anim = l;
				player.graphicFrame = 0;
				player.graphicFrameCycle = 0;
				player.graphicDelay = i2;
				player.graphicCycle = 0;
				player.animationDelay = player.smallXYIndex;
			}
		}
		if ((i & 4) != 0) {
			player.textSpoken = stream.readString();
			if (player.textSpoken.charAt(0) == '~') {
				player.textSpoken = player.textSpoken.substring(1);
				pushMessage(player.textSpoken, 2, player.name);
			} else if (player == myPlayer) {
				pushMessage(player.textSpoken, 2, player.name);
			}
			player.chatColor = 0;
			player.chatEffect = 0;
			player.textCycle = 150;
		}
		if ((i & 0x80) != 0) {
			int i1 = stream.readShortLE();
			int j2 = stream.readUnsignedByte();
			int j3 = stream.readUnsignedByteNeg();
			int k3 = stream.currentOffset;
			if (player.name != null && player.visible) {
				long l3 = TextClass.longForName(player.name);
				boolean flag = false;
				if (j2 <= 1) {
					for (int i4 = 0; i4 < ignoreCount; i4++) {
						if (ignoreListAsLongs[i4] != l3) {
							continue;
						}
						flag = true;
						break;
					}

				}
				if (!flag && restrictedArea == 0) {
					try {
						chatBuffer.currentOffset = 0;
						stream.readBytesReverse(j3, 0, chatBuffer.buffer);
						chatBuffer.currentOffset = 0;
                                                String s = TextInput.decodeChatMessage(j3, chatBuffer);
						s = Censor.doCensor(s);
						player.textSpoken = s;
						player.chatColor = i1 >> 8;
						player.privelage = j2;

						// entityMessage(player);

						player.chatEffect = i1 & 0xff;
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
			stream.currentOffset = k3 + j3;
		}
		if ((i & 1) != 0) {
			player.interactingEntity = stream.readShortLE();
			if (player.interactingEntity == 0x00ffff) {
				player.interactingEntity = -1;
			}
		}
		if ((i & 0x10) != 0) {
			int j1 = stream.readUnsignedByteNeg();
			byte abyte0[] = new byte[j1];
			Stream stream_1 = new Stream(abyte0);
			stream.readBytes(j1, 0, abyte0);
			playerBuffers[j] = stream_1;
			player.updatePlayer(stream_1);
		}
		if ((i & 2) != 0) {
			player.focusX = stream.readShortLEAdd();
			player.focusY = stream.readShortLE();
		}
		if ((i & 0x20) != 0) {
			int k1 = stream.readUnsignedByte();
			int k2 = stream.readUnsignedByteA();
			player.updateHitData(k2, k1, loopCycle);
			player.loopCycleStatus = loopCycle + 300;
			player.currentHealth = stream.readUnsignedByteNeg();
			player.maxHealth = stream.readUnsignedByte();
		}
		if ((i & 0x200) != 0) {
			int l1 = stream.readUnsignedByte();
			int l2 = stream.readUnsignedByteSub();
			player.updateHitData(l2, l1, loopCycle);
			player.loopCycleStatus = loopCycle + 300;
			player.currentHealth = stream.readUnsignedByte();
			player.maxHealth = stream.readUnsignedByteNeg();
		}
	}

       public void updateCameraPosition() {
		try {
			int j = myPlayer.x + cameraXOffset;
			int k = myPlayer.y + cameraYOffset;
			if (cameraX - j < -500 || cameraX - j > 500 || cameraY - k < -500 || cameraY - k > 500) {
				cameraX = j;
				cameraY = k;
			}
			if (cameraX != j) {
				cameraX += (j - cameraX) / 16;
			}
			if (cameraY != k) {
				cameraY += (k - cameraY) / 16;
			}
                        if (super.keyArray[1] == 1) {
                                cameraYawAccel += (-24 - cameraYawAccel) / 2;
                        } else if (super.keyArray[2] == 1) {
                                cameraYawAccel += (24 - cameraYawAccel) / 2;
                        } else {
                                cameraYawAccel /= 2;
                        }
                        if (super.keyArray[3] == 1) {
                                cameraPitchAccel += (12 - cameraPitchAccel) / 2;
                        } else if (super.keyArray[4] == 1) {
                                cameraPitchAccel += (-12 - cameraPitchAccel) / 2;
                        } else {
                                cameraPitchAccel /= 2;
                        }
                        cameraYaw = cameraYaw + cameraYawAccel / 2 & 0x7ff;
                        cameraPitch += cameraPitchAccel / 2;
			if (cameraPitch < 128) {
				cameraPitch = 128;
			}
			if (cameraPitch > 383) {
				cameraPitch = 383;
			}
			int l = cameraX >> 7;
			int i1 = cameraY >> 7;
			int j1 = getTileHeight(plane, cameraY, cameraX);
			int k1 = 0;
			if (l > 3 && i1 > 3 && l < 100 && i1 < 100) {
				for (int l1 = l - 4; l1 <= l + 4; l1++) {
					for (int k2 = i1 - 4; k2 <= i1 + 4; k2++) {
						int l2 = plane;
						if (l2 < 3 && (tileFlags[1][l1][k2] & 2) == 2) {
							l2++;
						}
						int i3 = j1 - tileHeights[l2][l1][k2];
						if (i3 > k1) {
							k1 = i3;
						}
					}

				}

			}
			cameraMoveCycle++;
			if (cameraMoveCycle > 1512) {
				cameraMoveCycle = 0;
				stream.createFrame(77);
				stream.writeWordBigEndian(0);
				int i2 = stream.currentOffset;
				stream.writeWordBigEndian((int) (Math.random() * 256D));
				stream.writeWordBigEndian(101);
				stream.writeWordBigEndian(233);
				stream.writeWord(45092);
				if ((int) (Math.random() * 2D) == 0) {
					stream.writeWord(35784);
				}
				stream.writeWordBigEndian((int) (Math.random() * 256D));
				stream.writeWordBigEndian(64);
				stream.writeWordBigEndian(38);
				stream.writeWord((int) (Math.random() * 65536D));
				stream.writeWord((int) (Math.random() * 65536D));
				stream.writeBytes(stream.currentOffset - i2);
			}
			int j2 = k1 * 192;
			if (j2 > 0x17f00) {
				j2 = 0x17f00;
			}
			if (j2 < 32768) {
				j2 = 32768;
			}
			if (j2 > cameraZoom) {
				cameraZoom += (j2 - cameraZoom) / 24;
				return;
			}
			if (j2 < cameraZoom) {
				cameraZoom += (j2 - cameraZoom) / 80;
			}
		} catch (Exception _ex) {
			Signlink.reporterror("glfc_ex " + myPlayer.x + "," + myPlayer.y + "," + cameraX + "," + cameraY + "," + currentRegionX + "," + currentRegionY + "," + baseX + "," + baseY);
			throw new RuntimeException("eek");
		}
	}

	public void processDrawing() {
		if (rsAlreadyLoaded || loadingError || genericLoadingError) {
			showErrorScreen();
			return;
		}
		drawCycle++;
		if (!loggedIn) {
			drawLoginScreen(false);
		} else {
			drawGameScreen();
		}
		clickCycle = 0;
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

	public static String combatDiffColor(int i, int j) {
		int k = i - j;
		if (k < -9) {
			return "@red@";
		}
		if (k < -6) {
			return "@or3@";
		}
		if (k < -3) {
			return "@or2@";
		}
		if (k < 0) {
			return "@or1@";
		}
		if (k > 9) {
			return "@gre@";
		}
		if (k > 6) {
			return "@gr3@";
		}
		if (k > 3) {
			return "@gr2@";
		}
		if (k > 0) {
			return "@gr1@";
		} else {
			return "@yel@";
		}
	}

	public void setWaveVolume(int i) {
		Signlink.wavevol = i;
	}

	public void draw3dScreen() {
		drawSplitpublicChat();
		if (crossType == 1) {
			crosses[crossIndex / 100].drawTransparentSprite(crossX - 8 - 4, crossY - 8 - 4);
			clickPacketCounter++;
			if (clickPacketCounter > 67) {
				clickPacketCounter = 0;
				stream.createFrame(78);
			}
		}
		if (crossType == 2) {
			crosses[4 + crossIndex / 100].drawTransparentSprite(crossX - 8 - 4, crossY - 8 - 4);
		}
		if (overlayInterfaceId != -1) {
			updateInterfaceAnimations(animationCycle, overlayInterfaceId);
			drawInterface(0, 0, RSInterface.interfaceCache[overlayInterfaceId], 0);
		}
		if (openInterfaceID != -1) {
			updateInterfaceAnimations(animationCycle, openInterfaceID);
			drawInterface(0, 0, RSInterface.interfaceCache[openInterfaceID], 0);
		}
               updateRestrictedArea();
		if (!menuOpen) {
			processRightClick();
			drawTooltip();
		} else if (menuScreenArea == 0) {
			drawMenu();
		}
		if (multiCombatZone == 1) {
			multiOverlay.drawSprite(472, 296);
		}
		if (systemUpdateTimer != 0) {
			String message = "";
			int seconds = systemUpdateTimer / 50;
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
			int debugWidth = chatTextDrawingArea.getTextWidth("System update in: 00:00") + 10; //140;
			int debugHeight = 25 + (debugItems * 15);
			int debugX = 511 - debugWidth;
			int debugY = 334 - debugHeight;
			int fill = 0x5d5447;
			int fill2 = Color.BLACK.hashCode();
			int opacity = 140;
	
			DrawingArea.fillArea(fill, debugY, debugWidth, debugHeight, opacity, debugX);
			DrawingArea.fillArea(fill2, debugY + 1, debugWidth - 2, 16, opacity, debugX + 1);
			DrawingArea.fillPixels(debugY + 18, debugHeight - 19, fill2, debugX + 1, debugWidth - 2);
			chatTextDrawingArea.textLeft(Color.WHITE.darker().hashCode(), "Notification", debugY += 14, debugX + 3);

			// Bump Y value
			debugY += 3;
			
			// Draw items
			chatTextDrawingArea.textLeftShadow(true, debugX + 4, Color.WHITE.hashCode(), "System update in:", debugY += 15);
			chatTextDrawingArea.textRightShadow(true, debugX + debugWidth - 4, Color.YELLOW.hashCode(), message, debugY);

			// Not sure what this is for
			systemUpdateCounter++;
			if (systemUpdateCounter > 75) {
				systemUpdateCounter = 0;
				stream.createFrame(148);
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
			String s = TextClass.fixName(TextClass.nameForLong(l));
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
			stream.createFrame(133);
			stream.writeQWord(l);
			return;
		} catch (RuntimeException runtimeexception) {
			Signlink.reporterror("45688, " + l + ", " + 4 + ", " + runtimeexception.toString());
		}
		throw new RuntimeException();
	}

       public void animatePlayers() {
		for (int i = -1; i < playerCount; i++) {
			int j;
			if (i == -1) {
				j = myPlayerIndex;
			} else {
				j = playerIndices[i];
			}
			Player player = playerArray[j];
			if (player != null) {
                           updateEntityMovement(player);
			}
		}

	}

	public void processPendingSpawns() {
		if (loadingStage == 2) {
			for (PendingSpawn class30_sub1 = (PendingSpawn) pendingSpawns.reverseGetFirst(); class30_sub1 != null; class30_sub1 = (PendingSpawn) pendingSpawns.reverseGetNext()) {
				if (class30_sub1.delay > 0) {
					class30_sub1.delay--;
				}
				if (class30_sub1.delay == 0) {
					if (class30_sub1.oldId < 0 || ObjectManager.isObjectVisible(class30_sub1.oldId, class30_sub1.oldOrientation)) {
                                                updateSceneObjects(class30_sub1.y, class30_sub1.plane, class30_sub1.oldType, class30_sub1.oldOrientation, class30_sub1.x, class30_sub1.category, class30_sub1.oldId);
						class30_sub1.unlink();
					}
				} else {
					if (class30_sub1.spawnDelay > 0) {
						class30_sub1.spawnDelay--;
					}
					if (class30_sub1.spawnDelay == 0 && class30_sub1.x >= 1 && class30_sub1.y >= 1 && class30_sub1.x <= 102 && class30_sub1.y <= 102 && (class30_sub1.id < 0 || ObjectManager.isObjectVisible(class30_sub1.id, class30_sub1.type))) {
                                                updateSceneObjects(class30_sub1.y, class30_sub1.plane, class30_sub1.orientation, class30_sub1.type, class30_sub1.x, class30_sub1.category, class30_sub1.id);
						class30_sub1.spawnDelay = -1;
						if (class30_sub1.id == class30_sub1.oldId && class30_sub1.oldId == -1) {
							class30_sub1.unlink();
						} else if (class30_sub1.id == class30_sub1.oldId && class30_sub1.orientation == class30_sub1.oldType && class30_sub1.type == class30_sub1.oldOrientation) {
							class30_sub1.unlink();
						}
					}
				}
			}

		}
	}

	public void determineMenuSize() {
		int i = chatTextDrawingArea.getTextWidth("Choose Option");
		for (int j = 0; j < menuActionRow; j++) {
			int k = chatTextDrawingArea.getTextWidth(menuActionName[j]);
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
			menuHeight = 15 * menuActionRow + 22;
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
			menuHeight = 15 * menuActionRow + 22;
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
			menuHeight = 15 * menuActionRow + 22;
		}
	}

       public void updateSelfMovement(Stream stream) {
		stream.initBitAccess();
		int j = stream.readBits(1);
		if (j == 0) {
			return;
		}
		int k = stream.readBits(2);
		if (k == 0) {
			playerUpdateIndices[playerUpdateCount++] = myPlayerIndex;
			return;
		}
		if (k == 1) {
			int l = stream.readBits(3);
			myPlayer.moveInDir(false, l);
			int k1 = stream.readBits(1);
			if (k1 == 1) {
				playerUpdateIndices[playerUpdateCount++] = myPlayerIndex;
			}
			return;
		}
		if (k == 2) {
			int i1 = stream.readBits(3);
			myPlayer.moveInDir(true, i1);
			int l1 = stream.readBits(3);
			myPlayer.moveInDir(true, l1);
			int j2 = stream.readBits(1);
			if (j2 == 1) {
				playerUpdateIndices[playerUpdateCount++] = myPlayerIndex;
			}
			return;
		}
		if (k == 3) {
			boolean debugPlanes = false;
			if (debugPlanes) {
				System.out.println("plane is " + plane);
			}
			plane = stream.readBits(2);
			if (debugPlanes) {
				System.out.println("plane is now " + plane);
			}
			int j1 = stream.readBits(1);
			int i2 = stream.readBits(1);
			if (i2 == 1) {
				playerUpdateIndices[playerUpdateCount++] = myPlayerIndex;
			}
			int k2 = stream.readBits(7);
			int l2 = stream.readBits(7);
			myPlayer.setPos(l2, k2, j1 == 1);
                       //Handle plane changes: higher planes disable roof checks and we must update
                       //ObjectManager's plane field then call constructMapRegion so it rebuilds the scene.
                       //Test coords: lumbridge 3207, 3227; varrock 3209, 3392; gnome agility 2474, 3427
			ObjectManager.hideRoofs = ClientSettings.HIDE_ROOFS;
			ObjectManager.hideBuggyVarrockSwordShopSnow = ClientSettings.HIDE_BUGGY_VARROCK_SWORD_SHOP_SNOW;
			if (plane > 0) {
				ObjectManager.hideRoofs = false;
				ObjectManager.hideBuggyVarrockSwordShopSnow = false;
			}
			ObjectManager.currentPlane = plane;
                       constructMapRegion();
		}
	}

	public void nullLoader() {
		flameThreadActive = false;
		while (drawingFlames) {
			flameThreadActive = false;
			try {
				Thread.sleep(50L);
			} catch (Exception _ex) {
			}
		}
		loginBoxBackground = null;
		loginButtonBackground = null;
		runeBackgrounds = null;
		flameBuffer = null;
		flamePaletteRed = null;
		flamePaletteGreen = null;
		flamePaletteBlue = null;
		flameGradient1 = null;
		flameGradient2 = null;
		flameBuffer1 = null;
		flameBuffer2 = null;
		titleBackgroundLeft = null;
		titleBackgroundRight = null;
	}

       public boolean updateInterfaceAnimations(int i, int j) {
		boolean flag1 = false;
		RSInterface class9 = RSInterface.interfaceCache[j];
		if (class9 == null || class9.children == null)
			return flag1;
		for (int element : class9.children) {
			if (element == -1) {
				break;
			}
			RSInterface class9_1 = RSInterface.interfaceCache[element];
			if (class9_1.type == 1) {
				flag1 |= updateInterfaceAnimations(i, class9_1.id);
			}
                        if (class9_1.type == 6 && (class9_1.disabledAnimation != -1 || class9_1.enabledAnimation != -1)) {
                               boolean flag2 = interfaceIsSelected(class9_1);
                               int l;
                                if (flag2) {
                                        l = class9_1.enabledAnimation;
                                } else {
                                        l = class9_1.disabledAnimation;
                                }
                                if (l != -1) {
                                        Animation animation = Animation.anims[l];
                                        for (class9_1.animationCycle += i; class9_1.animationCycle > animation.getFrameDelay(class9_1.animationFrame);) {
                                                class9_1.animationCycle -= animation.getFrameDelay(class9_1.animationFrame) + 1;
                                                class9_1.animationFrame++;
                                                if (class9_1.animationFrame >= animation.frameCount) {
                                                        class9_1.animationFrame -= animation.frameStep;
                                                        if (class9_1.animationFrame < 0 || class9_1.animationFrame >= animation.frameCount) {
                                                                class9_1.animationFrame = 0;
                                                        }
                                                }
                                                flag1 = true;
                                        }

				}
			}
		}

		return flag1;
	}

       public int determineCameraPlane() {
		int j = 3;
		if (yCameraCurve < 310) {
			int k = Math.max(0, Math.min(103, xCameraPos >> 7));
			int l = Math.max(0, Math.min(103, yCameraPos >> 7));
			int i1 = myPlayer.x >> 7;
			int j1 = myPlayer.y >> 7;
			if ((tileFlags[plane][k][l] & 4) != 0) {
				j = plane;
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
					if ((tileFlags[plane][k][l] & 4) != 0) {
						j = plane;
					}
					k2 += i2;
					if (k2 >= 0x10000) {
						k2 -= 0x10000;
						if (l < j1) {
							l++;
						} else if (l > j1) {
							l--;
						}
						if ((tileFlags[plane][k][l] & 4) != 0) {
							j = plane;
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
					if ((tileFlags[plane][k][l] & 4) != 0) {
						j = plane;
					}
					l2 += j2;
					if (l2 >= 0x10000) {
						l2 -= 0x10000;
						if (k < i1) {
							k++;
						} else if (k > i1) {
							k--;
						}
						if ((tileFlags[plane][k][l] & 4) != 0) {
							j = plane;
						}
					}
				}
			}
		}
		if ((tileFlags[plane][myPlayer.x >> 7][myPlayer.y >> 7] & 4) != 0) {
			j = plane;
		}
		return j;
	}

       public int getCurrentPlane() {
		// Hide other planes when using fixed camera
		return plane;
		// int j = getTileHeight(plane, yCameraPos, xCameraPos);
		// if (j - zCameraPos < 800 && (tileFlags[plane][xCameraPos >> 7][yCameraPos >> 7] & 4) != 0) {
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

					stream.createFrame(74);
					stream.writeQWord(l);
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
		if (Signlink.mainapp != null) {
			return Signlink.mainapp.getParameter(s);
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
					if (k2 >= 0 && k2 < ItemDef.totalItems && (!ItemDef.lookup(k2).membersObject || isMembers)) {
						for (int j3 = 0; j3 < class9_1.inv.length; j3++) {
							if (class9_1.inv[j3] == k2 + 1) {
								k1 += class9_1.invStackSizes[j3];
							}
						}

					}
				}
				if (j1 == 5) {
					k1 = variousSettings[ai[l++]];
				}
				if (j1 == 6) {
					k1 = levelExperience[maxStats[ai[l++]] - 1];
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
					if (l2 >= 0 && l2 < ItemDef.totalItems && (!ItemDef.lookup(l2).membersObject || isMembers)) {
						for (int element : class9_2.inv) {
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
                                        int l3 = varBit.configId;
                                        int i4 = varBit.leastSignificantBit;
                                        int j4 = varBit.mostSignificantBit;
					int k4 = bitMasks[j4 - i4];
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
                chatTextDrawingArea.drawRandomColorText(4, 0xffffff, s, loopCycle / 1000, 15);
	}

	public void drawMinimap() {
		chatBackground.initDrawingArea();
		if (minimapState == 2) {
			byte abyte0[] = mapBack.pixels;
			int ai[] = DrawingArea.pixels;
			int k2 = abyte0.length;
			for (int i5 = 0; i5 < k2; i5++) {
				if (abyte0[i5] == 0) {
					ai[i5] = 0;
				}
			}

			compass.drawTransformed(33, cameraYaw, mapBackWidths, 256, mapBackLeft, 25, 0, 0, 33, 25);
			tabAreaBuffer.initDrawingArea();
			Texture.lineOffsets = chatBoxAreaOffsets;
			return;
		}
		int i = cameraYaw + minimapRotationOffset & 0x7ff;
		int j = 48 + myPlayer.x / 32;
		int l2 = 464 - myPlayer.y / 32;
           minimapImage.drawTransformed(151, i, minimapLineLengths, 256 + minimapZoom, minimapLineOffset, l2, 5, 25, 146, j);
		compass.drawTransformed(33, cameraYaw, mapBackWidths, 256, mapBackLeft, 25, 0, 0, 33, 25);
		// Minimap icons (shops, quest etc)
		for (int j5 = 0; j5 < minimapIconCount; j5++) {
			int k = minimapIconX[j5] * 4 + 2 - myPlayer.x / 32;
			int i3 = minimapIconY[j5] * 4 + 2 - myPlayer.y / 32;
			markMinimap(minimapIconSprites[j5], k, i3);
		}

		for (int k5 = 0; k5 < 104; k5++) {
			for (int l5 = 0; l5 < 104; l5++) {
				NodeList class19 = groundArray[plane][k5][l5];
				if (class19 != null) {
					int l = k5 * 4 + 2 - myPlayer.x / 32;
					int j3 = l5 * 4 + 2 - myPlayer.y / 32;
					markMinimap(mapDotItem, l, j3);
				}
			}
		}

		for (int i6 = 0; i6 < npcCount; i6++) {
			NPC npc = npcArray[npcIndices[i6]];
			if (npc != null && npc.isVisible()) {
                                EntityDef entityDef = npc.definition;
                                if (entityDef.childrenIDs != null) {
                                        entityDef = entityDef.transform();
                                }
                                if (entityDef != null && entityDef.minimapVisible && entityDef.clickable) {
					int i1 = npc.x / 32 - myPlayer.x / 32;
					int k3 = npc.y / 32 - myPlayer.y / 32;
					markMinimap(mapDotNPC, i1, k3);
				}
			}
		}

		for (int j6 = 0; j6 < playerCount; j6++) {
			Player player = playerArray[playerIndices[j6]];
			if (player != null && player.isVisible()) {
				int j1 = player.x / 32 - myPlayer.x / 32;
				int l3 = player.y / 32 - myPlayer.y / 32;
				boolean flag1 = false;
				boolean flag2 = false;
				long l6 = TextClass.longForName(player.name);

				if (myPlayer.team != 0 && player.team != 0 && myPlayer.team == player.team || player.combatLevel == 0) {
					flag1 = true;
				}

				for (int k6 = 0; k6 < friendsCount; k6++) {
					if (l6 != friendsListAsLongs[k6] || friendsNodeIDs[k6] == 0) {
						continue;
					}
					flag2 = true;
					break;
				}

				if (flag1) {
					markMinimap(mapDotTeam, j1, l3);
				} else if (flag2) {
					markMinimap(mapDotFriend, j1, l3);
				} else {
					markMinimap(mapDotPlayer, j1, l3);
				}
			}
		}

		if (hintIconState != 0 && loopCycle % 20 < 10) {
			if (hintIconState == 1 && hintNpcIndex >= 0 && hintNpcIndex < npcArray.length) {
				NPC class30_sub2_sub4_sub1_sub1_1 = npcArray[hintNpcIndex];
				if (class30_sub2_sub4_sub1_sub1_1 != null) {
					int k1 = class30_sub2_sub4_sub1_sub1_1.x / 32 - myPlayer.x / 32;
					int i4 = class30_sub2_sub4_sub1_sub1_1.y / 32 - myPlayer.y / 32;
					drawMinimapHint(mapMarker, i4, k1);
				}
			}
			if (hintIconState == 2) {
				int l1 = (selectedNpcId - baseX) * 4 + 2 - myPlayer.x / 32;
				int j4 = (destinationX - baseY) * 4 + 2 - myPlayer.y / 32;
				drawMinimapHint(mapMarker, j4, l1);
			}
			if (hintIconState == 10 && selectedPlayerId >= 0 && selectedPlayerId < playerArray.length) {
				Player class30_sub2_sub4_sub1_sub2_1 = playerArray[selectedPlayerId];
				if (class30_sub2_sub4_sub1_sub2_1 != null) {
					int i2 = class30_sub2_sub4_sub1_sub2_1.x / 32 - myPlayer.x / 32;
					int k4 = class30_sub2_sub4_sub1_sub2_1.y / 32 - myPlayer.y / 32;
					drawMinimapHint(mapMarker, k4, i2);
				}
			}
		}
		if (destX != 0) {
			int j2 = destX * 4 + 2 - myPlayer.x / 32;
			int l4 = destY * 4 + 2 - myPlayer.y / 32;
			markMinimap(mapFlag, j2, l4);
		}
		// Draw player square on mini map
		DrawingArea.fillArea(3, 78, 0xffffff, 3, 97);
		tabAreaBuffer.initDrawingArea();
		Texture.lineOffsets = chatBoxAreaOffsets;
	}

	public void npcScreenPos(Entity entity, int i) {
		calcEntityScreenPos(entity.x, i, entity.y);

		// aryan entity.entScreenX = spriteDrawX; entity.entScreenY =
		// spriteDrawY;
	}

	public void calcEntityScreenPos(int i, int j, int l) {
		if (i < 128 || l < 128 || i > 13056 || l > 13056) {
			spriteDrawX = -1;
			spriteDrawY = -1;
			return;
		}
		int i1 = getTileHeight(plane, l, i) - j;
		i -= xCameraPos;
		i1 -= zCameraPos;
		l -= yCameraPos;
		int j1 = Model.sineTable[yCameraCurve];
		int k1 = Model.cosineTable[yCameraCurve];
		int l1 = Model.sineTable[xCameraCurve];
		int i2 = Model.cosineTable[xCameraCurve];
		int j2 = l * l1 + i * i2 >> 16;
		l = l * i2 - i * l1 >> 16;
		i = j2;
		j2 = i1 * k1 - l * j1 >> 16;
		l = i1 * j1 + l * k1 >> 16;
		i1 = j2;
		if (l >= 50) {
			spriteDrawX = Texture.textureInt1 + (i << 9) / l;
			spriteDrawY = Texture.textureInt2 + (i1 << 9) / l;
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
		if (systemUpdateTimer != 0) {
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
						int i1 = boldFont.getTextWidth("From:  " + s + chatMessages[j]) + 25;
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

       public void queuePendingSpawn(int j, int k, int l, int i1, int j1, int k1, int l1, int i2, int j2) {
		PendingSpawn class30_sub1 = null;
		for (PendingSpawn class30_sub1_1 = (PendingSpawn) pendingSpawns.reverseGetFirst(); class30_sub1_1 != null; class30_sub1_1 = (PendingSpawn) pendingSpawns.reverseGetNext()) {
			if (class30_sub1_1.plane != l1 || class30_sub1_1.x != i2 || class30_sub1_1.y != j1 || class30_sub1_1.category != i1) {
				continue;
			}
			class30_sub1 = class30_sub1_1;
			break;
		}

		if (class30_sub1 == null) {
			class30_sub1 = new PendingSpawn();
			class30_sub1.plane = l1;
			class30_sub1.category = i1;
			class30_sub1.x = i2;
			class30_sub1.y = j1;
			locateSceneObject(class30_sub1);
			pendingSpawns.insertHead(class30_sub1);
		}
		class30_sub1.id = k;
		class30_sub1.type = k1;
		class30_sub1.orientation = l;
		class30_sub1.spawnDelay = j2;
		class30_sub1.delay = j;
	}

	public boolean interfaceIsSelected(RSInterface class9) {
		if (class9.valueCompareType == null) {
			return false;
		}
		for (int i = 0; i < class9.valueCompareType.length; i++) {
			int j = extractInterfaceValues(class9, i);
			int k = class9.requiredValues[i];
			if (class9.valueCompareType[i] == 2) {
				if (j >= k) {
					return false;
				}
			} else if (class9.valueCompareType[i] == 3) {
				if (j <= k) {
					return false;
				}
			} else if (class9.valueCompareType[i] == 4) {
				if (j == k) {
					return false;
				}
			} else if (j != k) {
				return false;
			}
		}

		return true;
	}

	public DataInputStream openJagGrabInputStream(String s) throws IOException {
		// if(!useJaggrab)
		// if(signlink.mainapp != null)
		// return signlink.openurl(s);
		// else
		// return new DataInputStream((new URL(getCodeBase(), s)).openStream());
		if (jaggrabSocket != null) {
			try {
				jaggrabSocket.close();
			} catch (Exception _ex) {
			}
			jaggrabSocket = null;
		}
		jaggrabSocket = openSocket(43595);
		jaggrabSocket.setSoTimeout(10000);
		java.io.InputStream inputstream = jaggrabSocket.getInputStream();
		OutputStream outputstream = jaggrabSocket.getOutputStream();
		outputstream.write(("JAGGRAB /" + s + "\n\n").getBytes());
		return new DataInputStream(inputstream);
	}
	
	public void doFlamesDrawing() {
		char c = '\u0100';
		if (flameMainColor > 0) {
			for (int i = 0; i < 256; i++) {
				if (flameMainColor > 768) {
					flameBuffer[i] = blendColors(flamePaletteRed[i], flamePaletteGreen[i], 1024 - flameMainColor);
				} else if (flameMainColor > 256) {
					flameBuffer[i] = flamePaletteGreen[i];
				} else {
					flameBuffer[i] = blendColors(flamePaletteGreen[i], flamePaletteRed[i], 256 - flameMainColor);
				}
			}

		} else if (flameSecondaryColor > 0) {
			for (int j = 0; j < 256; j++) {
				if (flameSecondaryColor > 768) {
					flameBuffer[j] = blendColors(flamePaletteRed[j], flamePaletteBlue[j], 1024 - flameSecondaryColor);
				} else if (flameSecondaryColor > 256) {
					flameBuffer[j] = flamePaletteBlue[j];
				} else {
					flameBuffer[j] = blendColors(flamePaletteBlue[j], flamePaletteRed[j], 256 - flameSecondaryColor);
				}
			}

		} else {
			System.arraycopy(flamePaletteRed, 0, flameBuffer, 0, 256);

		}
               System.arraycopy(titleBackgroundLeft.pixels, 0, titleLeftProducer.pixels, 0, 33920);

		int i1 = 0;
		int j1 = 1152;
		for (int k1 = 1; k1 < c - 1; k1++) {
			int l1 = flameLineOffsets[k1] * (c - k1) / c;
			int j2 = 22 + l1;
			if (j2 < 0) {
				j2 = 0;
			}
			i1 += j2;
			for (int l2 = j2; l2 < 128; l2++) {
				int j3 = flameBuffer1[i1++];
				if (j3 != 0) {
					int l3 = j3;
					int j4 = 256 - j3;
					j3 = flameBuffer[j3];
                                   int l4 = titleLeftProducer.pixels[j1];
                                   titleLeftProducer.pixels[j1++] = ((j3 & 0xff00ff) * l3 + (l4 & 0xff00ff) * j4 & 0xff00ff00) + ((j3 & 0xff00) * l3 + (l4 & 0xff00) * j4 & 0xff0000) >> 8;
				} else {
					j1++;
				}
			}

			j1 += j2;
		}

		titleLeftProducer.drawGraphics(0, super.graphics, 0);
               System.arraycopy(titleBackgroundRight.pixels, 0, titleRightProducer.pixels, 0, 33920);

		i1 = 0;
		j1 = 1176;
		for (int k2 = 1; k2 < c - 1; k2++) {
			int i3 = flameLineOffsets[k2] * (c - k2) / c;
			int k3 = 103 - i3;
			j1 += i3;
			for (int i4 = 0; i4 < k3; i4++) {
				int k4 = flameBuffer1[i1++];
				if (k4 != 0) {
					int i5 = k4;
					int j5 = 256 - k4;
					k4 = flameBuffer[k4];
                                   int k5 = titleRightProducer.pixels[j1];
                                   titleRightProducer.pixels[j1++] = ((k4 & 0xff00ff) * i5 + (k5 & 0xff00ff) * j5 & 0xff00ff00) + ((k4 & 0xff00) * i5 + (k5 & 0xff00) * j5 & 0xff0000) >> 8;
				} else {
					j1++;
				}
			}

			i1 += 128 - k3;
			j1 += 128 - k3 - i3;
		}

		titleRightProducer.drawGraphics(0, super.graphics, 637);
	}

       public void updateOtherPlayers(Stream stream) {
		int j = stream.readBits(8);
		if (j < playerCount) {
			for (int k = j; k < playerCount; k++) {
				removedEntityIndices[entityRemovalCount++] = playerIndices[k];
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
			int j1 = stream.readBits(1);
			if (j1 == 0) {
				playerIndices[playerCount++] = i1;
				player.lastUpdateCycle = loopCycle;
			} else {
				int k1 = stream.readBits(2);
				if (k1 == 0) {
					playerIndices[playerCount++] = i1;
					player.lastUpdateCycle = loopCycle;
					playerUpdateIndices[playerUpdateCount++] = i1;
				} else if (k1 == 1) {
					playerIndices[playerCount++] = i1;
					player.lastUpdateCycle = loopCycle;
					int l1 = stream.readBits(3);
					player.moveInDir(false, l1);
					int j2 = stream.readBits(1);
					if (j2 == 1) {
						playerUpdateIndices[playerUpdateCount++] = i1;
					}
				} else if (k1 == 2) {
					playerIndices[playerCount++] = i1;
					player.lastUpdateCycle = loopCycle;
					int i2 = stream.readBits(3);
					player.moveInDir(true, i2);
					int k2 = stream.readBits(3);
					player.moveInDir(true, k2);
					int l2 = stream.readBits(1);
					if (l2 == 1) {
						playerUpdateIndices[playerUpdateCount++] = i1;
					}
				} else if (k1 == 3) {
					removedEntityIndices[entityRemovalCount++] = i1;
				}
			}
		}
	}

	public void drawLoginScreen(boolean flag) {
		resetImageProducers();
		loginRightProducer.initDrawingArea();
		loginBoxBackground.draw(0, 0);
		char c = '\u0168';
		char c1 = '\310';
		if (loginScreenState == 0) {
			int i = c1 / 2 + 80;
			plainFont.textCenterShadow(0x75a9a9, c / 2, onDemandFetcher.statusString, i, true);
			i = c1 / 2 - 20;
			chatTextDrawingArea.textCenterShadow(0xffff00, c / 2, "Welcome to " + ClientSettings.SERVER_NAME + "", i, true);
			i += 30;
			int l = c / 2 - 80;
			int k1 = c1 / 2 + 20;
			loginButtonBackground.draw(l - 73, k1 - 20);
			chatTextDrawingArea.textCenterShadow(0xffffff, l, "New User", k1 + 5, true);
			l = c / 2 + 80;
			loginButtonBackground.draw(l - 73, k1 - 20);
			chatTextDrawingArea.textCenterShadow(0xffffff, l, "Existing User", k1 + 5, true);
		}
		if (loginScreenState == 2) {
			int j = c1 / 2 - 40;
			if (loginMessage1.length() > 0) {
				chatTextDrawingArea.textCenterShadow(0xffff00, c / 2, loginMessage1, j - 15, true);
				chatTextDrawingArea.textCenterShadow(0xffff00, c / 2, loginMessage2, j, true);
				j += 30;
			} else {
				chatTextDrawingArea.textCenterShadow(0xffff00, c / 2, loginMessage2, j - 7, true);
				j += 30;
			}
			chatTextDrawingArea.textLeftShadow(true, c / 2 - 90, 0xffffff, "Username: " + myUsername + (loginScreenCursorPos == 0 & loopCycle % 40 < 20 ? "@yel@|" : ""), j);
			j += 15;
			chatTextDrawingArea.textLeftShadow(true, c / 2 - 88, 0xffffff, "Password: " + TextClass.passwordAsterisks(myPassword) + (loginScreenCursorPos == 1 & loopCycle % 40 < 20 ? "@yel@|" : ""), j);
			j += 15;
			if (!flag) {
				int i1 = c / 2 - 80;
				int l1 = c1 / 2 + 50;
				loginButtonBackground.draw(i1 - 73, l1 - 20);
				chatTextDrawingArea.textCenterShadow(0xffffff, i1, "Login", l1 + 5, true);
				i1 = c / 2 + 80;
				loginButtonBackground.draw(i1 - 73, l1 - 20);
				chatTextDrawingArea.textCenterShadow(0xffffff, i1, "Cancel", l1 + 5, true);
			}
		}
		if (loginScreenState == 3) {
			chatTextDrawingArea.textCenterShadow(0xffff00, c / 2, "Create a free account", c1 / 2 - 60, true);
			int k = c1 / 2 - 35;
			chatTextDrawingArea.textCenterShadow(0xffffff, c / 2, "To create a new account just click", k, true);
			k += 15;
			chatTextDrawingArea.textCenterShadow(0xffffff, c / 2, "\"Cancel\" below, and click \"Existing User\".", k, true);
			k += 15;
			chatTextDrawingArea.textCenterShadow(0xffffff, c / 2, "Log in with any credentials you want and an", k, true);
			k += 15;
			chatTextDrawingArea.textCenterShadow(0xffffff, c / 2, "account will automatically be created for you.", k, true);
			k += 15;
			int j1 = c / 2;
			int i2 = c1 / 2 + 50;
			loginButtonBackground.draw(j1 - 73, i2 - 20);
			chatTextDrawingArea.textCenterShadow(0xffffff, j1, "Cancel", i2 + 5, true);
		}
		loginRightProducer.drawGraphics(171, super.graphics, 202);
		if (welcomeScreenRaised) {
			welcomeScreenRaised = false;
			titleImageProducer.drawGraphics(0, super.graphics, 128);
			loginLeftProducer.drawGraphics(371, super.graphics, 202);
			titleTopLeftProducer.drawGraphics(265, super.graphics, 0);
			titleTopRightProducer.drawGraphics(265, super.graphics, 562);
			titleBottomLeftProducer.drawGraphics(171, super.graphics, 128);
			titleBottomRightProducer.drawGraphics(171, super.graphics, 562);
		}
	}

	public void drawFlames() {
		drawingFlames = true;
		try {
			long l = System.currentTimeMillis();
			int i = 0;
			int j = 20;
			while (flameThreadActive) {
				flameDrawingCounter++;
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

       public void handleMapPackets(Stream stream, int j) {
		if (j == 84) {
			int k = stream.readUnsignedByte();
			int j3 = mapEventX + (k >> 4 & 7);
			int i6 = mapEventY + (k & 7);
			int l8 = stream.readUnsignedWord();
			int k11 = stream.readUnsignedWord();
			int l13 = stream.readUnsignedWord();
			if (j3 >= 0 && i6 >= 0 && j3 < 104 && i6 < 104) {
				NodeList class19_1 = groundArray[plane][j3][i6];
				if (class19_1 != null) {
					for (Item class30_sub2_sub4_sub2_3 = (Item) class19_1.reverseGetFirst(); class30_sub2_sub4_sub2_3 != null; class30_sub2_sub4_sub2_3 = (Item) class19_1.reverseGetNext()) {
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
			int k3 = mapEventX + (l >> 4 & 7);
			int j6 = mapEventY + (l & 7);
			int i9 = stream.readUnsignedWord();
			int l11 = stream.readUnsignedByte();
			int i14 = l11 >> 4 & 0xf;
			int i16 = l11 & 7;
			if (myPlayer.smallX[0] >= k3 - i14 && myPlayer.smallX[0] <= k3 + i14 && myPlayer.smallY[0] >= j6 - i14 && myPlayer.smallY[0] <= j6 + i14 && soundEffectEnabled && !lowMem && currentSound < 50) {
				sound[currentSound] = i9;
				soundType[currentSound] = i16;
                                soundDelay[currentSound] = Sounds.delays[i9];
				currentSound++;
			}
		}
		if (j == 215) {
			int i1 = stream.readShortAdd();
			int l3 = stream.readUnsignedByteSub();
			int k6 = mapEventX + (l3 >> 4 & 7);
			int j9 = mapEventY + (l3 & 7);
			int i12 = stream.readShortAdd();
			int j14 = stream.readUnsignedWord();
			if (k6 >= 0 && j9 >= 0 && k6 < 104 && j9 < 104 && i12 != localPlayerIndex) {
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
			int j1 = stream.readUnsignedByteA();
			int i4 = mapEventX + (j1 >> 4 & 7);
			int l6 = mapEventY + (j1 & 7);
			int k9 = stream.readUnsignedWord();
			if (i4 >= 0 && l6 >= 0 && i4 < 104 && l6 < 104) {
				NodeList class19 = groundArray[plane][i4][l6];
				if (class19 != null) {
					for (Item item = (Item) class19.reverseGetFirst(); item != null; item = (Item) class19.reverseGetNext()) {
						if (item.ID != (k9 & 0x7fff)) {
							continue;
						}
						item.unlink();
						break;
					}

					if (class19.reverseGetFirst() == null) {
						groundArray[plane][i4][l6] = null;
					}
					spawnGroundItem(i4, l6);
				}
			}
			return;
		}
		if (j == 160) {
			int k1 = stream.readUnsignedByteSub();
			int j4 = mapEventX + (k1 >> 4 & 7);
			int i7 = mapEventY + (k1 & 7);
			int l9 = stream.readUnsignedByteSub();
			int j12 = l9 >> 2;
			int k14 = l9 & 3;
			int j16 = objectData[j12];
			int j17 = stream.readShortAdd();
			if (j4 >= 0 && i7 >= 0 && j4 < 103 && i7 < 103) {
				int j18 = tileHeights[plane][j4][i7];
				int i19 = tileHeights[plane][j4 + 1][i7];
				int l19 = tileHeights[plane][j4 + 1][i7 + 1];
				int k20 = tileHeights[plane][j4][i7 + 1];
				if (j16 == 0) {
                                       BoundaryObject class10 = worldController.getBoundaryObject(plane, j4, i7);
					if (class10 != null) {
						int k21 = class10.uid >> 14 & 0x7fff;
						if (j12 == 2) {
							class10.primary = new DynamicObject(k21, 4 + k14, 2, i19, l19, j18, k20, j17, false);
							class10.secondary = new DynamicObject(k21, k14 + 1 & 3, 2, i19, l19, j18, k20, j17, false);
						} else {
							class10.primary = new DynamicObject(k21, k14, j12, i19, l19, j18, k20, j17, false);
						}
					}
				}
                                if (j16 == 1) {
                                       WallDecoration decoration = worldController.getWallDecoration(j4, i7, plane);
                                        if (decoration != null) {
                                                decoration.renderable = new DynamicObject(decoration.uid >> 14 & 0x7fff, 0, 4, i19, l19, j18, k20, j17, false);
                                        }
                                }
                                if (j16 == 2) {
                                       SceneObject sceneObject = worldController.getSceneObject(j4, i7, plane);
                                        if (j12 == 11) {
                                                j12 = 10;
                                        }
                                        if (sceneObject != null) {
                                                sceneObject.renderable = new DynamicObject(sceneObject.uid >> 14 & 0x7fff, k14, j12, i19, l19, j18, k20, j17, false);
                                        }
				}
				if (j16 == 3) {
                                       TileDecoration class49 = worldController.getTileDecoration(i7, j4, plane);
                                        if (class49 != null) {
                                                class49.renderable = new DynamicObject(class49.uid >> 14 & 0x7fff, k14, 22, i19, l19, j18, k20, j17, false);
                                        }
				}
			}
			return;
		}
		if (j == 147) {
			int l1 = stream.readUnsignedByteSub();
			int k4 = mapEventX + (l1 >> 4 & 7);
			int j7 = mapEventY + (l1 & 7);
			int i10 = stream.readUnsignedWord();
			byte byte0 = stream.readByteSub();
			int l14 = stream.readShortLE();
			byte byte1 = stream.readByteNeg();
			int k17 = stream.readUnsignedWord();
			int k18 = stream.readUnsignedByteSub();
			int j19 = k18 >> 2;
			int i20 = k18 & 3;
			int l20 = objectData[j19];
			byte byte2 = stream.readSignedByte();
			int l21 = stream.readUnsignedWord();
			byte byte3 = stream.readByteNeg();
			Player player;
			if (i10 == localPlayerIndex) {
				player = myPlayer;
			} else {
				player = playerArray[i10];
			}
			if (player != null) {
				ObjectDef class46 = ObjectDef.forID(l21);
				int i22 = tileHeights[plane][k4][j7];
				int j22 = tileHeights[plane][k4 + 1][j7];
				int k22 = tileHeights[plane][k4 + 1][j7 + 1];
				int l22 = tileHeights[plane][k4][j7 + 1];
				Model model = class46.getModel(j19, i20, i22, j22, k22, l22, -1);
				if (model != null) {
					queuePendingSpawn(k17 + 1, -1, 0, l20, j7, 0, plane, k4, l14 + 1);
                                player.animationStartCycle = l14 + loopCycle;
                                player.animationEndCycle = k17 + loopCycle;
					player.aModel_1714 = model;
					int i23 = class46.sizeX;
					int j23 = class46.sizeY;
					if (i20 == 1 || i20 == 3) {
						i23 = class46.sizeY;
						j23 = class46.sizeX;
					}
                                player.animationBaseX = k4 * 128 + i23 * 64;
                                player.animationBaseZ = j7 * 128 + j23 * 64;
                                player.animationBaseHeight = getTileHeight(plane, player.animationBaseZ, player.animationBaseX);
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
					player.anInt1719 = k4 + byte2;
					player.anInt1721 = k4 + byte0;
					player.anInt1720 = j7 + byte3;
					player.anInt1722 = j7 + byte1;
				}
			}
		}
		if (j == 101) {
			int l2 = stream.readUnsignedByteNeg();
			int k5 = l2 >> 2;
			int j8 = l2 & 3;
			int i11 = objectData[k5];
			int j13 = stream.readUnsignedByte();
			int k15 = mapEventX + (j13 >> 4 & 7);
			int l16 = mapEventY + (j13 & 7);

			if (k15 >= 0 && l16 >= 0 && k15 < 104 && l16 < 104) {
				queuePendingSpawn(-1, -1, j8, i11, l16, k5, plane, k15, 0);
			}
			return;
		}
		if (j == 151) {
                        int i2 = stream.readUnsignedByteAdd();
			int l4 = mapEventX + (i2 >> 4 & 7);
			int k7 = mapEventY + (i2 & 7);
			int j10 = stream.readShortLE();
			int k12 = stream.readUnsignedByteSub();
			int i15 = k12 >> 2;
			int k16 = k12 & 3;
			int l17 = objectData[i15];
			//System.out.println("id: " + j10 + " x:" + (this.baseX + mapEventX) + " y:" + (this.baseY + mapEventY));
			if (l4 >= 0 && k7 >= 0 && l4 < 104 && k7 < 104)
				queuePendingSpawn(-1, j10, k16, l17, k7, i15, plane, l4, 0);
			return;
		}

		if (j == 4) {
			int j2 = stream.readUnsignedByte();
			int i5 = mapEventX + (j2 >> 4 & 7);
			int l7 = mapEventY + (j2 & 7);
			int k10 = stream.readUnsignedWord();
			int l12 = stream.readUnsignedByte();
			int j15 = stream.readUnsignedWord();
			if (i5 >= 0 && l7 >= 0 && i5 < 104 && l7 < 104) {
				i5 = i5 * 128 + 64;
				l7 = l7 * 128 + 64;
                                GraphicsObject graphicsObject = new GraphicsObject(plane, loopCycle, j15, k10, getTileHeight(plane, l7, i5) - l12, l7, i5);
                                graphicsObjectList.insertHead(graphicsObject);
			}
			return;
		}
		if (j == 44) {
			int itemID = stream.readShortLEAdd();
			int itemAmount = stream.readUnsignedWord();
			int i8 = stream.readUnsignedByte();
			int l10 = mapEventX + (i8 >> 4 & 7);
			int i13 = mapEventY + (i8 & 7);
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
			int l5 = mapEventX + (i3 >> 4 & 7);
			int k8 = mapEventY + (i3 & 7);
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
                                Projectile class30_sub2_sub4_sub4 = new Projectile(i21, l18, k19 + loopCycle, j20 + loopCycle, j21, plane, getTileHeight(plane, k8, l5) - i18, k8, l5, l15, i17);
                                class30_sub2_sub4_sub4.track(k19 + loopCycle, k13, getTileHeight(plane, k13, j11) - l18, j11);
                                projectileList.insertHead(class30_sub2_sub4_sub4);
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

       public void updateNpcList(Stream stream) {
		stream.initBitAccess();
		int k = stream.readBits(8);
		if (k < npcCount) {
			for (int l = k; l < npcCount; l++) {
				removedEntityIndices[entityRemovalCount++] = npcIndices[l];
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
			int k1 = stream.readBits(1);
			if (k1 == 0) {
				npcIndices[npcCount++] = j1;
				npc.lastUpdateCycle = loopCycle;
			} else {
				int l1 = stream.readBits(2);
				if (l1 == 0) {
					npcIndices[npcCount++] = j1;
					npc.lastUpdateCycle = loopCycle;
					playerUpdateIndices[playerUpdateCount++] = j1;
				} else if (l1 == 1) {
					npcIndices[npcCount++] = j1;
					npc.lastUpdateCycle = loopCycle;
					int i2 = stream.readBits(3);
					npc.moveInDir(false, i2);
					int k2 = stream.readBits(1);
					if (k2 == 1) {
						playerUpdateIndices[playerUpdateCount++] = j1;
					}
				} else if (l1 == 2) {
					npcIndices[npcCount++] = j1;
					npc.lastUpdateCycle = loopCycle;
					int j2 = stream.readBits(3);
					npc.moveInDir(true, j2);
					int l2 = stream.readBits(3);
					npc.moveInDir(true, l2);
					int i3 = stream.readBits(1);
					if (i3 == 1) {
						playerUpdateIndices[playerUpdateCount++] = j1;
					}
				} else if (l1 == 3) {
					removedEntityIndices[entityRemovalCount++] = j1;
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

	public void markMinimap(Sprite sprite, int i, int j) {
		int k = cameraYaw + minimapRotationOffset & 0x7ff;
		int l = i * i + j * j;
		if (l > 6400) {
			return;
		}
		int i1 = Model.sineTable[k];
		int j1 = Model.cosineTable[k];
		i1 = i1 * 256 / (minimapZoom + 256);
		j1 = j1 * 256 / (minimapZoom + 256);
		int k1 = j * i1 + i * j1 >> 16;
		int l1 = j * j1 - i * i1 >> 16;
		if (l > 2500) {
			sprite.drawWithMask(mapBack, 83 - l1 - sprite.trimHeight / 2 - 4, 94 + k1 - sprite.trimWidth / 2 + 4);
		} else {
			sprite.drawTransparentSprite(94 + k1 - sprite.trimWidth / 2 + 4, 83 - l1 - sprite.trimHeight / 2 - 4);
		}
	}

       public void updateSceneObjects(int i, int j, int k, int l, int i1, int j1, int k1) {
		if (i1 >= 1 && i >= 1 && i1 <= 102 && i <= 102) {
			if (lowMem && j != plane) {
				return;
			}
			int i2 = 0;
			if (j1 == 0) {
				i2 = worldController.getBoundaryObjectUid(j, i1, i);
			}
			if (j1 == 1) {
				i2 = worldController.getWallDecorationUid(j, i1, i);
			}
			if (j1 == 2) {
				i2 = worldController.getSceneObjectUid(j, i1, i);
			}
			if (j1 == 3) {
				i2 = worldController.getTileDecorationUid(j, i1, i);
			}
			if (i2 != 0) {
				int i3 = worldController.getObjectConfig(j, i1, i, i2);
				int j2 = i2 >> 14 & 0x7fff;
				int k2 = i3 & 0x1f;
				int l2 = i3 >> 6;
				if (j1 == 0) {
                                        worldController.clearBoundaryObject(i1, j, i, (byte) -119);
					ObjectDef class46 = ObjectDef.forID(j2);
					if (class46.isSolid) {
						collisionMaps[j].removeWall(l2, k2, class46.impenetrable, i1, i);
					}
				}
				if (j1 == 1) {
                                        worldController.clearWallDecoration(i, j, i1);
				}
				if (j1 == 2) {
                                        worldController.removeSceneObject(j, i1, i);
					ObjectDef class46_1 = ObjectDef.forID(j2);
					if (i1 + class46_1.sizeX > 103 || i + class46_1.sizeX > 103 || i1 + class46_1.sizeY > 103 || i + class46_1.sizeY > 103) {
						return;
					}
					if (class46_1.isSolid) {
						collisionMaps[j].removeObject(l2, class46_1.sizeX, i1, i, class46_1.sizeY, class46_1.impenetrable);
					}
				}
				if (j1 == 3) {
                                        worldController.clearTileDecoration(j, i, i1);
					ObjectDef class46_2 = ObjectDef.forID(j2);
					if (class46_2.isSolid && class46_2.interactive) {
						collisionMaps[j].unblockTile(i, i1);
					}
				}
			}
			if (k1 >= 0) {
				int j3 = j;
				if (j3 < 3 && (tileFlags[1][i1][i] & 2) == 2) {
					j3++;
				}
				ObjectManager.addObject(worldController, k, i, l, j3, collisionMaps[j], tileHeights, i1, k1, j);
			}
		}
	}

	public void updatePlayers(int i, Stream stream) {
		entityRemovalCount = 0;
		playerUpdateCount = 0;
               updateSelfMovement(stream);
               updateOtherPlayers(stream);
               addLocalPlayers(stream, i);
               processPlayerUpdateMasks(stream);
		for (int k = 0; k < entityRemovalCount; k++) {
			int l = removedEntityIndices[k];
			if (playerArray[l].lastUpdateCycle != loopCycle) {
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
			int i3 = Model.sineTable[l1];
			int k3 = Model.cosineTable[l1];
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
			int j3 = Model.sineTable[i2];
			int l3 = Model.cosineTable[i2];
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
				socketStream.flushInputStream(inStream.buffer, 1);
				pktType = inStream.buffer[0] & 0xff;
				if (encryption != null) {
					pktType = pktType - encryption.getNextKey() & 0xff;
				}
				pktSize = SizeConstants.packetSizes[pktType];
				i--;
			}
			if (pktSize == -1) {
				if (i > 0) {
					socketStream.flushInputStream(inStream.buffer, 1);
					pktSize = inStream.buffer[0] & 0xff;
					i--;
				} else {
					return false;
				}
			}
			if (pktSize == -2) {
				if (i > 1) {
					socketStream.flushInputStream(inStream.buffer, 2);
					inStream.currentOffset = 0;
					pktSize = inStream.readUnsignedWord();
					i -= 2;
				} else {
					return false;
				}
			}
			if (i < pktSize) {
				return false;
			}
			inStream.currentOffset = 0;
			socketStream.flushInputStream(inStream.buffer, pktSize);
			connectionTimeoutCounter = 0;
			prevPktType2 = prevPktType;
			prevPktType = lastPacketType;
			lastPacketType = pktType;
			if (pktType == 81) {
				updatePlayers(pktSize, inStream);
				regionLoading = false;
				pktType = -1;
				return true;
			}
			if (pktType == 176) {
				daysSinceRecovChange = inStream.readUnsignedByteNeg();
				unreadMessages = inStream.readShortAdd();
				membersInt = inStream.readUnsignedByte();
				lastLoginIp = inStream.readIntV2();
				daysSinceLastLogin = inStream.readUnsignedWord();
                               if (lastLoginIp != 0 && openInterfaceID == -1) {
                                       Signlink.dnslookup(TextClass.intToIpString(lastLoginIp));
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
				mapEventX = inStream.readUnsignedByteNeg();
				mapEventY = inStream.readUnsignedByteSub();
				for (int j = mapEventX; j < mapEventX + 8; j++) {
					for (int l9 = mapEventY; l9 < mapEventY + 8; l9++) {
						if (groundArray[plane][j][l9] != null) {
							groundArray[plane][j][l9] = null;
							spawnGroundItem(j, l9);
						}
					}

				}

				for (PendingSpawn class30_sub1 = (PendingSpawn) pendingSpawns.reverseGetFirst(); class30_sub1 != null; class30_sub1 = (PendingSpawn) pendingSpawns.reverseGetNext()) {
					if (class30_sub1.x >= mapEventX && class30_sub1.x < mapEventX + 8 && class30_sub1.y >= mapEventY && class30_sub1.y < mapEventY + 8 && class30_sub1.plane == plane) {
						class30_sub1.delay = 0;
					}
				}

				pktType = -1;
				return true;
			}
			if (pktType == 185) {
				int k = inStream.readShortLEAdd();
                                RSInterface.interfaceCache[k].mediaType = 3;
				if (myPlayer.desc == null) {
                                        RSInterface.interfaceCache[k].mediaId = (myPlayer.bodyColors[0] << 25) + (myPlayer.bodyColors[4] << 20) + (myPlayer.equipment[0] << 15) + (myPlayer.equipment[8] << 10) + (myPlayer.equipment[11] << 5) + myPlayer.equipment[1];
				} else {
                                        RSInterface.interfaceCache[k].mediaId = (int) (0x12345678L + myPlayer.desc.type);
				}
				pktType = -1;
				return true;
			}
			if (pktType == 107) {
				isCameraLocked = false;
				for (int l = 0; l < 5; l++) {
					tabFlashing[l] = false;
				}

				pktType = -1;
				return true;
			}
			if (pktType == 72) {
				int i1 = inStream.readShortLE();
				RSInterface class9 = RSInterface.interfaceCache[i1];
				for (int k15 = 0; k15 < class9.inv.length; k15++) {
					class9.inv[k15] = -1;
					class9.inv[k15] = 0;
				}

				pktType = -1;
				return true;
			}
			if (pktType == 214) {
				ignoreCount = pktSize / 8;
				for (int j1 = 0; j1 < ignoreCount; j1++) {
					ignoreListAsLongs[j1] = inStream.readQWord();
				}

				pktType = -1;
				return true;
			}
			if (pktType == 166) {
				isCameraLocked = true;
				cameraTargetX = inStream.readUnsignedByte();
				cameraTargetY = inStream.readUnsignedByte();
				cameraTargetZ = inStream.readUnsignedWord();
				cameraMoveSpeed = inStream.readUnsignedByte();
				cameraMoveAcceleration = inStream.readUnsignedByte();
				if (cameraMoveAcceleration >= 100) {
					xCameraPos = cameraTargetX * 128 + 64;
					yCameraPos = cameraTargetY * 128 + 64;
					zCameraPos = getTileHeight(plane, yCameraPos, xCameraPos) - cameraTargetZ;
				}
				pktType = -1;
				return true;
			}
			if (pktType == 134) {
				needDrawTabArea = true;
				int skillID = inStream.readUnsignedByte();
				int experience = inStream.readIntV1();
				int level = inStream.readUnsignedByte();
				currentExp[skillID] = experience;
				currentStats[skillID] = level;
				maxStats[skillID] = 1;
				// Fix for current health not updating
				if (skillID == 3) {
					myPlayer.currentHealth = level;
				}
				for (int k20 = 0; k20 < 98; k20++) {
					if (experience >= levelExperience[k20]) {
						maxStats[skillID] = k20 + 2;
					}
				}

				pktType = -1;
				return true;
			}
			if (pktType == 71) {
				int l1 = inStream.readUnsignedWord();
				int j10 = inStream.readUnsignedByteA();
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
				int i2 = inStream.readShortLE();
				if (i2 == 0x00ffff) {
					i2 = -1;
				}
				if (i2 != -1 || previousSong != 0) {
					if (i2 != -1 && currentSong != i2 && musicVolume != 0 && previousSong == 0)
                                               queueSong(10, musicVolume, false, i2);
				} else
                                       stopMusic(false);
				currentSong = i2;
				pktType = -1;
				return true;
			}
			if (pktType == 121) {
				int i_60_ = inStream.readShortLEAdd();
				int i_61_ = inStream.readShortAdd();
				if (i_61_ == 0x00ffff)
					i_61_ = -1;
				if (musicVolume != 0 && i_61_ != -1) {
                                       playSong(musicVolume, false, i_60_);
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
				int k2 = inStream.readSignedWord();
				int l10 = inStream.readShortLESigned();
				int i16 = inStream.readShortLE();
                                RSInterface class9_5 = RSInterface.interfaceCache[i16];
                                class9_5.offsetX = k2;
				class9_5.offsetY = l10;
				pktType = -1;
				return true;
			}
			if (pktType == 73 || pktType == 241) {

				// mapReset();
				int l2 = currentRegionX;
				int i11 = currentRegionY;
				if (pktType == 73) {
					l2 = inStream.readShortAdd();
					i11 = inStream.readUnsignedWord();
					isDynamicRegion = false;
				}
				if (pktType == 241) {
					i11 = inStream.readShortAdd();
					inStream.initBitAccess();
					for (int j16 = 0; j16 < 4; j16++) {
						for (int l20 = 0; l20 < 13; l20++) {
							for (int j23 = 0; j23 < 13; j23++) {
								int i26 = inStream.readBits(1);
								if (i26 == 1) {
									dynamicRegionData[j16][l20][j23] = inStream.readBits(26);
								} else {
									dynamicRegionData[j16][l20][j23] = -1;
								}
							}

						}

					}

					inStream.finishBitAccess();
					l2 = inStream.readUnsignedWord();
					isDynamicRegion = true;
				}
				if (currentRegionX == l2 && currentRegionY == i11 && loadingStage == 2) {
					pktType = -1;
					return true;
				}
				currentRegionX = l2;
				currentRegionY = i11;
				baseX = (currentRegionX - 6) * 8;
				baseY = (currentRegionY - 6) * 8;
				forceMapReload = (currentRegionX / 8 == 48 || currentRegionX / 8 == 49) && currentRegionY / 8 == 48;
				if (currentRegionX / 8 == 48 && currentRegionY / 8 == 148) {
					forceMapReload = true;
				}
				loadingStage = 1;
				loadingStartTime = System.currentTimeMillis();
				drawTextOnScreen(null, "Loading - please wait.");
				if (pktType == 73) {
					int k16 = 0;
					for (int i21 = (currentRegionX - 6) / 8; i21 <= (currentRegionX + 6) / 8; i21++) {
						for (int k23 = (currentRegionY - 6) / 8; k23 <= (currentRegionY + 6) / 8; k23++) {
							k16++;
						}

					}

					terrainData = new byte[k16][];
					objectMapData = new byte[k16][];
					regionBaseIds = new int[k16];
					terrainArchiveIds = new int[k16];
					objectArchiveIds = new int[k16];
					k16 = 0;
					for (int l23 = (currentRegionX - 6) / 8; l23 <= (currentRegionX + 6) / 8; l23++) {
						for (int j26 = (currentRegionY - 6) / 8; j26 <= (currentRegionY + 6) / 8; j26++) {
							regionBaseIds[k16] = (l23 << 8) + j26;
							if (forceMapReload && (j26 == 49 || j26 == 149 || j26 == 147 || l23 == 50 || l23 == 49 && j26 == 47)) {
								terrainArchiveIds[k16] = -1;
								objectArchiveIds[k16] = -1;
								k16++;
							} else {
                                                                int k28 = terrainArchiveIds[k16] = onDemandFetcher.getRegionArchiveId(0, j26, l23);
                                                                if (k28 != -1) {
                                                                        onDemandFetcher.queueRequest(3, k28);
                                                                }
                                                                int j30 = objectArchiveIds[k16] = onDemandFetcher.getRegionArchiveId(1, j26, l23);
                                                                if (j30 != -1) {
                                                                        onDemandFetcher.queueRequest(3, j30);
                                                                }
								k16++;
							}
						}

					}

				}
				if (pktType == 241) {
					int l16 = 0;
					int ai[] = new int[676];
					for (int i24 = 0; i24 < 4; i24++) {
						for (int k26 = 0; k26 < 13; k26++) {
							for (int l28 = 0; l28 < 13; l28++) {
								int k30 = dynamicRegionData[i24][k26][l28];
								if (k30 != -1) {
									int k31 = k30 >> 14 & 0x3ff;
									int i32 = k30 >> 3 & 0x7ff;
									int k32 = (k31 / 8 << 8) + i32 / 8;
									for (int j33 = 0; j33 < l16; j33++) {
										if (ai[j33] != k32) {
											continue;
										}
										k32 = -1;
										break;
									}

									if (k32 != -1) {
										ai[l16++] = k32;
									}
								}
							}

						}

					}

					terrainData = new byte[l16][];
					objectMapData = new byte[l16][];
					regionBaseIds = new int[l16];
					terrainArchiveIds = new int[l16];
					objectArchiveIds = new int[l16];
					for (int l26 = 0; l26 < l16; l26++) {
						int i29 = regionBaseIds[l26] = ai[l26];
						int l30 = i29 >> 8 & 0xff;
						int l31 = i29 & 0xff;
                                                int j32 = terrainArchiveIds[l26] = onDemandFetcher.getRegionArchiveId(0, l31, l30);
                                                if (j32 != -1) {
                                                        onDemandFetcher.queueRequest(3, j32);
                                                }
                                                int i33 = objectArchiveIds[l26] = onDemandFetcher.getRegionArchiveId(1, l31, l30);
                                                if (i33 != -1) {
                                                        onDemandFetcher.queueRequest(3, i33);
                                                }
					}

				}
				int i17 = baseX - prevBaseX;
				int j21 = baseY - prevBaseY;
				prevBaseX = baseX;
				prevBaseY = baseY;
				for (int j24 = 0; j24 < 16384; j24++) {
					NPC npc = npcArray[j24];
					if (npc != null) {
						for (int j29 = 0; j29 < 10; j29++) {
							npc.smallX[j29] -= i17;
							npc.smallY[j29] -= j21;
						}

						npc.x -= i17 * 128;
						npc.y -= j21 * 128;
					}
				}

				for (int i27 = 0; i27 < maxPlayers; i27++) {
					Player player = playerArray[i27];
					if (player != null) {
						for (int i31 = 0; i31 < 10; i31++) {
							player.smallX[i31] -= i17;
							player.smallY[i31] -= j21;
						}

						player.x -= i17 * 128;
						player.y -= j21 * 128;
					}
				}

				regionLoading = true;
				byte byte1 = 0;
				byte byte2 = 104;
				byte byte3 = 1;
				if (i17 < 0) {
					byte1 = 103;
					byte2 = -1;
					byte3 = -1;
				}
				byte byte4 = 0;
				byte byte5 = 104;
				byte byte6 = 1;
				if (j21 < 0) {
					byte4 = 103;
					byte5 = -1;
					byte6 = -1;
				}
				for (int k33 = byte1; k33 != byte2; k33 += byte3) {
					for (int l33 = byte4; l33 != byte5; l33 += byte6) {
						int i34 = k33 + i17;
						int j34 = l33 + j21;
						for (int k34 = 0; k34 < 4; k34++) {
							if (i34 >= 0 && j34 >= 0 && i34 < 104 && j34 < 104) {
								groundArray[k34][k33][l33] = groundArray[k34][i34][j34];
							} else {
								groundArray[k34][k33][l33] = null;
							}
						}

					}

				}

				for (PendingSpawn class30_sub1_1 = (PendingSpawn) pendingSpawns.reverseGetFirst(); class30_sub1_1 != null; class30_sub1_1 = (PendingSpawn) pendingSpawns.reverseGetNext()) {
					class30_sub1_1.x -= i17;
					class30_sub1_1.y -= j21;
					if (class30_sub1_1.x < 0 || class30_sub1_1.y < 0 || class30_sub1_1.x >= 104 || class30_sub1_1.y >= 104) {
						class30_sub1_1.unlink();
					}
				}

				if (destX != 0) {
					destX -= i17;
					destY -= j21;
				}
				isCameraLocked = false;
				pktType = -1;
				return true;
			}
			if (pktType == 208) {
				int i3 = inStream.readShortLESigned();
				if (i3 >= 0) {
                                       resetInterfaceAnimation(i3);
				}
				overlayInterfaceId = i3;
				pktType = -1;
				return true;
			}
			if (pktType == 99) {
				minimapState = inStream.readUnsignedByte();
				pktType = -1;
				return true;
			}
			if (pktType == 75) {
				int j3 = inStream.readShortLEAdd();
				int j11 = inStream.readShortLEAdd();
                                RSInterface.interfaceCache[j11].mediaType = 2;
                                RSInterface.interfaceCache[j11].mediaId = j3;
				pktType = -1;
				return true;
			}
			if (pktType == 114) {
				systemUpdateTimer = inStream.readShortLE() * 30;
				pktType = -1;
				return true;
			}
			if (pktType == 60) {
				mapEventY = inStream.readUnsignedByte();
				mapEventX = inStream.readUnsignedByteNeg();
				while (inStream.currentOffset < pktSize) {
					int k3 = inStream.readUnsignedByte();
                                        handleMapPackets(inStream, k3);
				}
				pktType = -1;
				return true;
			}
			if (pktType == 35) {
				int l3 = inStream.readUnsignedByte();
				int k11 = inStream.readUnsignedByte();
				int j17 = inStream.readUnsignedByte();
				int k21 = inStream.readUnsignedByte();
				tabFlashing[l3] = true;
				cameraShakeAmplitude[l3] = k11;
				cameraShakeFrequency[l3] = j17;
				cameraShakeSpeed[l3] = k21;
				cameraShakeCycle[l3] = 0;
				pktType = -1;
				return true;
			}
			if (pktType == 174) {
				int id = inStream.readUnsignedWord();
				int type = /*inStream.readUnsignedByte()*/1;
				int delay = inStream.readUnsignedWord();
				int volume = inStream.readUnsignedByte();
				sound[currentSound] = id;
				soundType[currentSound] = type;
                                soundDelay[currentSound] = delay + Sounds.delays[id];
				soundVolume[currentSound] = volume;
				currentSound++;
				pktType = -1;
				return true;
			}
			if (pktType == 104) {
				int j4 = inStream.readUnsignedByteNeg();
				int i12 = inStream.readUnsignedByteA();
				String s6 = inStream.readString();
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
				String s = inStream.readString();
				if (s.endsWith(":tradereq:")) {
					String s3 = s.substring(0, s.indexOf(":"));
					long l17 = TextClass.longForName(s3);
					boolean flag2 = false;
					for (int j27 = 0; j27 < ignoreCount; j27++) {
						if (ignoreListAsLongs[j27] != l17) {
							continue;
						}
						flag2 = true;
						break;
					}

					if (!flag2 && restrictedArea == 0) {
						pushMessage("wishes to trade with you.", 4, s3);
					}
				} else if (s.endsWith(":duelreq:")) {
					String s4 = s.substring(0, s.indexOf(":"));
					long l18 = TextClass.longForName(s4);
					boolean flag3 = false;
					for (int k27 = 0; k27 < ignoreCount; k27++) {
						if (ignoreListAsLongs[k27] != l18) {
							continue;
						}
						flag3 = true;
						break;
					}

					if (!flag3 && restrictedArea == 0) {
						pushMessage("wishes to duel with you.", 8, s4);
					}
				} else if (s.endsWith(":chalreq:")) {
					String s5 = s.substring(0, s.indexOf(":"));
					long l19 = TextClass.longForName(s5);
					boolean flag4 = false;
					for (int l27 = 0; l27 < ignoreCount; l27++) {
						if (ignoreListAsLongs[l27] != l19) {
							continue;
						}
						flag4 = true;
						break;
					}

					if (!flag4 && restrictedArea == 0) {
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
				long l4 = inStream.readQWord();
				int i18 = inStream.readUnsignedByte();
				String s7 = TextClass.fixName(TextClass.nameForLong(l4));
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
				hintIconState = inStream.readUnsignedByte();
				if (hintIconState == 1) {
					hintNpcIndex = inStream.readUnsignedWord();
				}
				if (hintIconState >= 2 && hintIconState <= 6) {
					if (hintIconState == 2) {
						lastMouseX = 64;
						lastMouseY = 64;
					}
					if (hintIconState == 3) {
						lastMouseX = 0;
						lastMouseY = 64;
					}
					if (hintIconState == 4) {
						lastMouseX = 128;
						lastMouseY = 64;
					}
					if (hintIconState == 5) {
						lastMouseX = 64;
						lastMouseY = 0;
					}
					if (hintIconState == 6) {
						lastMouseX = 64;
						lastMouseY = 128;
					}
					hintIconState = 2;
					selectedNpcId = inStream.readUnsignedWord();
					destinationX = inStream.readUnsignedWord();
					destinationY = inStream.readUnsignedByte();
				}
				if (hintIconState == 10) {
					selectedPlayerId = inStream.readUnsignedWord();
				}
				pktType = -1;
				return true;
			}
			if (pktType == 248) {
				int i5 = inStream.readShortAdd();
				int k12 = inStream.readUnsignedWord();
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
				actionPending = false;
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
				int j5 = inStream.readShortLE();
				int l12 = inStream.readShortAdd();
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
					if (variousSettings[k5] != varpArray[k5]) {
						variousSettings[k5] = varpArray[k5];
                                          applyVarp(k5);
						needDrawTabArea = true;
					}
				}

				pktType = -1;
				return true;
			}
			if (pktType == 196) {
				long l5 = inStream.readQWord();
				@SuppressWarnings("unused")
				int j18 = inStream.readDWord();
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
				if (!flag5 && restrictedArea == 0) {
					try {
						// Direct message
                                                String s9 = TextInput.decodeChatMessage(pktSize - 13, inStream);
						if (l21 == 2 || l21 == 3) {
							pushMessage(s9, 7, "@cr2@" + TextClass.fixName(TextClass.nameForLong(l5)));
						} else if (l21 == 1) {
							pushMessage(s9, 7, "@cr1@" + TextClass.fixName(TextClass.nameForLong(l5)));
						} else {
							pushMessage(s9, 3, TextClass.fixName(TextClass.nameForLong(l5)));
						}
					} catch (Exception exception1) {
						Signlink.reporterror("cde1");
					}
				}
				pktType = -1;
				return true;
			}
			if (pktType == 85) {
				mapEventY = inStream.readUnsignedByteNeg();
				mapEventX = inStream.readUnsignedByteNeg();
				pktType = -1;
				return true;
			}
			if (pktType == 24) {
				flashingTabId = inStream.readUnsignedByteSub();
				if (flashingTabId == tabID) {
					if (flashingTabId == 3) {
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
				int i6 = inStream.readShortLE();
				int i13 = inStream.readUnsignedWord();
				int k18 = inStream.readUnsignedWord();
                                if (k18 == 0x00ffff) {
                                        RSInterface.interfaceCache[i6].mediaType = 0;
					pktType = -1;
					return true;
				} else {
					ItemDef itemDef = ItemDef.lookup(k18);
                                        RSInterface.interfaceCache[i6].mediaType = 4;
                                        RSInterface.interfaceCache[i6].mediaId = k18;
                                        RSInterface.interfaceCache[i6].modelRotation1 = itemDef.modelRotation1;
                                        RSInterface.interfaceCache[i6].modelRotation2 = itemDef.modelRotation2;
                                        RSInterface.interfaceCache[i6].modelZoom = itemDef.modelZoom * 100 / i13;
					pktType = -1;
					return true;
				}
			}
			if (pktType == 171) {
				boolean flag1 = inStream.readUnsignedByte() == 1;
				int j13 = inStream.readUnsignedWord();
				RSInterface.interfaceCache[j13].aBoolean266 = flag1;
				pktType = -1;
				return true;
			}
			if (pktType == 142) {
				int j6 = inStream.readShortLE();
                               resetInterfaceAnimation(j6);
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
				actionPending = false;
				pktType = -1;
				return true;
			}
			if (pktType == 126) {
				String message = inStream.readString();
				int interfaceID = inStream.readShortAdd();
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
				chatSettingsUpdateNeeded = true;
				inputTaken = true;
				pktType = -1;
				return true;
			}
			if (pktType == 240) {
				if (tabID == 12) {
					needDrawTabArea = true;
				}
				weight = inStream.readSignedWord();
				pktType = -1;
				return true;
			}
			if (pktType == 69) {
				int k9 = inStream.readUnsignedWord();
				int k15 = inStream.readUnsignedWord();
                               resetInterfaceAnimation(k15);
				if (k9 != -1)
                                       resetInterfaceAnimation(k9);
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
				actionPending = false;
				pktType = -1;
				return true;
			}
			if (pktType == 8) {
				int k6 = inStream.readShortLEAdd();
				int l13 = inStream.readUnsignedWord();
                                RSInterface.interfaceCache[k6].mediaType = 1;
                                RSInterface.interfaceCache[k6].mediaId = l13;
				pktType = -1;
				return true;
			}
			if (pktType == 122) {
				int l6 = inStream.readShortLEAdd();
				int i14 = inStream.readShortLEAdd();
				int i19 = i14 >> 10 & 0x1f;
				int i22 = i14 >> 5 & 0x1f;
				int l24 = i14 & 0x1f;
				RSInterface.interfaceCache[l6].textColor = (i19 << 19) + (i22 << 11) + (l24 << 3);
				pktType = -1;
				return true;
			}
			if (pktType == 53) {
				needDrawTabArea = true;
				int i7 = inStream.readUnsignedWord();
				RSInterface class9_1 = RSInterface.interfaceCache[i7];
				int j19 = inStream.readUnsignedWord();
				for (int j22 = 0; j22 < j19; j22++) {
					int i25 = inStream.readUnsignedByte();
					if (i25 == 255) {
						i25 = inStream.readIntV2();
					}
					class9_1.inv[j22] = inStream.readShortLEAdd();
					class9_1.invStackSizes[j22] = i25;
				}

				for (int j25 = j19; j25 < class9_1.inv.length; j25++) {
					class9_1.inv[j25] = 0;
					class9_1.invStackSizes[j25] = 0;
				}

				pktType = -1;
				return true;
			}
			if (pktType == 230) {
				int j7 = inStream.readShortAdd();
				int j14 = inStream.readUnsignedWord();
				int k19 = inStream.readUnsignedWord();
				int k22 = inStream.readShortLEAdd();
                                RSInterface.interfaceCache[j14].modelRotation1 = k19;
                                RSInterface.interfaceCache[j14].modelRotation2 = k22;
                                RSInterface.interfaceCache[j14].modelZoom = j7;
				pktType = -1;
				return true;
			}
			if (pktType == 221) {
				interfaceMode = inStream.readUnsignedByte();
				needDrawTabArea = true;
				pktType = -1;
				return true;
			}
			if (pktType == 177) {
				isCameraLocked = true;
				cameraFocusX = inStream.readUnsignedByte();
				cameraFocusY = inStream.readUnsignedByte();
				cameraFocusHeight = inStream.readUnsignedWord();
				cameraAdjustSpeed = inStream.readUnsignedByte();
				cameraAdjustAcceleration = inStream.readUnsignedByte();
				if (cameraAdjustAcceleration >= 100) {
					int k7 = cameraFocusX * 128 + 64;
					int k14 = cameraFocusY * 128 + 64;
					int i20 = getTileHeight(plane, k14, k7) - cameraFocusHeight;
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
				friendsListStatus = inStream.readUnsignedByteA();
				localPlayerIndex = inStream.readShortLEAdd();
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
				pktType = -1;
				return true;
			}
			if (pktType == 187) {
				messagePromptRaised = false;
				inputDialogState = 2;
				amountOrNameInput = "";
				inputTaken = true;
				pktType = -1;
				return true;
			}
			if (pktType == 97) {
				int l7 = inStream.readUnsignedWord();
                               resetInterfaceAnimation(l7);
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
					if (ClientSettings.SNOW_OVERLAY_FORCE_ENABLED || (ClientSettings.SNOW_OVERLAY_ENABLED && FloorOverlay.getTodaysDate().contains(ClientSettings.SNOW_MONTH))) {
						openInterfaceID = 15819;
					} else {
						openInterfaceID = 15801;
					}
					fullScreenInterfaceId = 15244;
				} else {
					openInterfaceID = l7;
				}
				actionPending = false;
				pktType = -1;
				return true;
			}
			if (pktType == 218) {
				int i8 = inStream.readShortLEAddSigned();
				dialogID = i8;
				inputTaken = true;
				pktType = -1;
				return true;
			}
			if (pktType == 87) {
				int j8 = inStream.readShortLE();
				int l14 = inStream.readIntV1();
				varpArray[j8] = l14;
				if (variousSettings[j8] != l14) {
					variousSettings[j8] = l14;
                                  applyVarp(j8);
					needDrawTabArea = true;
					if (dialogID != -1) {
						inputTaken = true;
					}
				}
				pktType = -1;
				return true;
			}
			if (pktType == 36) {
				int k8 = inStream.readShortLE();
				byte byte0 = inStream.readSignedByte();
				varpArray[k8] = byte0;
				if (variousSettings[k8] != byte0) {
					variousSettings[k8] = byte0;
                                  applyVarp(k8);
					needDrawTabArea = true;
					if (dialogID != -1) {
						inputTaken = true;
					}
				}
				pktType = -1;
				return true;
			}
			if (pktType == 61) {
				multiCombatZone = inStream.readUnsignedByte();
				pktType = -1;
				return true;
			}
			if (pktType == 200) {
				int l8 = inStream.readUnsignedWord();
				int i15 = inStream.readSignedWord();
                                RSInterface class9_4 = RSInterface.interfaceCache[l8];
                                class9_4.disabledAnimation = i15;
                                if (i15 == -1) {
                                        class9_4.animationFrame = 0;
                                        class9_4.animationCycle = 0;
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
				actionPending = false;
				pktType = -1;
				return true;
			}
			if (pktType == 34) {
				needDrawTabArea = true;
				int i9 = inStream.readUnsignedWord();
				RSInterface class9_2 = RSInterface.interfaceCache[i9];
				while (inStream.currentOffset < pktSize) {
					int j20 = inStream.readUnsignedSmart();
					int i23 = inStream.readUnsignedWord();
					int l25 = inStream.readUnsignedByte();
					if (l25 == 255) {
						l25 = inStream.readDWord();
					}
					if (j20 >= 0 && j20 < class9_2.inv.length) {
						class9_2.inv[j20] = i23;
						class9_2.invStackSizes[j20] = l25;
					}
				}
				pktType = -1;
				return true;
			}
			if (pktType == 105 || pktType == 84 || pktType == 147 || pktType == 215 || pktType == 4 || pktType == 117 || pktType == 156 || pktType == 44 || pktType == 160 || pktType == 101 || pktType == 151) {
                                handleMapPackets(inStream, pktType);
				pktType = -1;
				return true;
			}
			if (pktType == 106) {
				tabID = inStream.readUnsignedByteNeg();
				needDrawTabArea = true;
				tabAreaAltered = true;
				pktType = -1;
				return true;
			}
			if (pktType == 164) {
				int j9 = inStream.readShortLE();
                               resetInterfaceAnimation(j9);
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
				actionPending = false;
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

        public void renderGameView() {
		waveCycle++;
          addPlayersToScene(true);
               addNpcsToScene(true);
          addPlayersToScene(false);
               addNpcsToScene(false);
               processProjectiles();
                processGraphicsObjects();
		if (!isCameraLocked) {
			int i = cameraPitch;
			if (cameraZoom / 256 > i) {
				i = cameraZoom / 256;
			}
			if (tabFlashing[4] && cameraShakeFrequency[4] + 128 > i) {
				i = cameraShakeFrequency[4] + 128;
			}
			int k = cameraYaw + cameraYawOffset & 0x7ff;
			// Camera zoom control
			setCameraPos(600 + i * zoom, i, cameraX, getTileHeight(plane, myPlayer.y, myPlayer.x) - 70, k, cameraY);
		}
		int j;
		if (!isCameraLocked) {
               j = determineCameraPlane();
		} else {
               j = getCurrentPlane();
		}
		int l = xCameraPos;
		int i1 = zCameraPos;
		int j1 = yCameraPos;
		int k1 = yCameraCurve;
		int l1 = xCameraCurve;
		for (int i2 = 0; i2 < 5; i2++) {
			if (tabFlashing[i2]) {
				int j2 = (int) (Math.random() * (double) (cameraShakeAmplitude[i2] * 2 + 1) - (double) cameraShakeAmplitude[i2] + Math.sin((double) cameraShakeCycle[i2] * (double) cameraShakeSpeed[i2] / 100D) * (double) cameraShakeFrequency[i2]);
				if (i2 == 0) {
					xCameraPos += j2;
				}
				if (i2 == 1) {
					zCameraPos += j2;
				}
				if (i2 == 2) {
					yCameraPos += j2;
				}
				if (i2 == 3) {
					xCameraCurve = xCameraCurve + j2 & 0x7ff;
				}
				if (i2 == 4) {
					yCameraCurve += j2;
					if (yCameraCurve < 128) {
						yCameraCurve = 128;
					}
					if (yCameraCurve > 383) {
						yCameraCurve = 383;
					}
				}
			}
		}

		int k2 = Texture.cycle;
		Model.withinViewport = true;
		Model.queueLength = 0;
		Model.viewportCenterX = super.mouseX - 4;
		Model.viewportCenterY = super.mouseY - 4;
		DrawingArea.setAllPixelsToZero();
		if(graphicsEnabled){
                        worldController.renderScene(xCameraPos, yCameraPos, xCameraCurve, zCameraPos, j, yCameraCurve);
			worldController.clearObj5Cache();
			updateEntities();
			drawHeadIcon();
		}
		// Allow tabs to work
          animateTextures(k2);
		// Allow stuff inside the tabs to work
		draw3dScreen();
		if (showInfo) {
			int debugX = 0;
			int debugY = 234;
			int debugItems = 5;
			int debugWidth = 140;
			int debugHeight = 25 + (debugItems * 15);
			int fill = 0x5d5447;
			int fill2 = Color.BLACK.hashCode();
			int opacity = 140;
	
			DrawingArea.fillArea(fill, debugY, debugWidth, debugHeight, opacity, debugX);
			DrawingArea.fillArea(fill2, debugY + 1, debugWidth - 2, 16, opacity, debugX + 1);
			DrawingArea.fillPixels(debugY + 18, debugHeight - 19, fill2, debugX + 1, debugWidth - 2);
			chatTextDrawingArea.textLeft(Color.WHITE.darker().hashCode(), "Debug Info", debugY += 14, debugX + 3);
			chatTextDrawingArea.textLeft(super.fps > 40 ? Color.YELLOW.hashCode() : super.fps > 25 ? Color.ORANGE.hashCode() : Color.RED.hashCode(), super.fps + "fps", debugY, debugX + debugWidth - chatTextDrawingArea.getTextWidth(super.fps + "fps") - 3);

			// Bump Y value
			debugY += 3;
			
			// Draw items
			Runtime runtime = Runtime.getRuntime();
			int memKB = (int) ((runtime.totalMemory() - runtime.freeMemory()) / 1024L);
			chatTextDrawingArea.textLeftShadow(true, debugX + 4, Color.WHITE.hashCode(), "Memory:", debugY += 15);
			chatTextDrawingArea.textRightShadow(true, debugX + debugWidth - 4, Color.YELLOW.hashCode(), (memKB / 1024) + "mb", debugY);
			chatTextDrawingArea.textLeftShadow(true, debugX + 4, Color.WHITE.hashCode(), "Mouse:", debugY += 15);
			chatTextDrawingArea.textRightShadow(true, debugX + debugWidth - 4, Color.YELLOW.hashCode(), super.mouseX + ", " + super.mouseY, debugY);
			chatTextDrawingArea.textLeftShadow(true, debugX + 4, Color.WHITE.hashCode(), "Coords:", debugY += 15);
			chatTextDrawingArea.textRightShadow(true, debugX + debugWidth - 4, Color.YELLOW.hashCode(), (myPlayer.smallX[0] + baseX) + ", " + (myPlayer.smallY[0] + baseY), debugY);
			chatTextDrawingArea.textLeftShadow(true, debugX + 4, Color.WHITE.hashCode(), "Interface:", debugY += 15);
			chatTextDrawingArea.textRightShadow(true, debugX + debugWidth - 4, Color.YELLOW.hashCode(), "" + openInterfaceID, debugY);
			chatTextDrawingArea.textLeftShadow(true, debugX + 4, Color.WHITE.hashCode(), "Zoom level:", debugY += 15);
			chatTextDrawingArea.textRightShadow(true, debugX + debugWidth - 4, Color.YELLOW.hashCode(), "" + zoom, debugY);
		}
		
		if (customSettingShowExperiencePerHour) {
			int debugItems = 2;
			int debugWidth = 140;
			int debugHeight = 25 + (debugItems * 15);
			int debugX = 511 - debugWidth;
			int debugY = 0;
			int fill = 0x5d5447;
			int fill2 = Color.BLACK.hashCode();
			int opacity = 140;
	
			DrawingArea.fillArea(fill, debugY, debugWidth, debugHeight, opacity, debugX);
			DrawingArea.fillArea(fill2, debugY + 1, debugWidth - 2, 16, opacity, debugX + 1);
			DrawingArea.fillPixels(debugY + 18, debugHeight - 19, fill2, debugX + 1, debugWidth - 2);
			chatTextDrawingArea.textLeft(Color.WHITE.darker().hashCode(), "Experience Info", debugY += 14, debugX + 3);

			// Bump Y value
			debugY += 3;
			
			// Calculate exp/h
			long currentExpGained = calculateTotalExp();
			long expGained = currentExpGained - customSettingShowExperiencePerHourStartExp;
			long expPerHour = (long) ((expGained * 3600000D) / (System.currentTimeMillis() - customSettingShowExperiencePerHourStart));

			// Draw items
			chatTextDrawingArea.textLeftShadow(true, debugX + 4, Color.WHITE.hashCode(), "Exp per hour:", debugY += 15);
			chatTextDrawingArea.textRightShadow(true, debugX + debugWidth - 4, Color.YELLOW.hashCode(), intToShortLetter((int) expPerHour), debugY);
			chatTextDrawingArea.textLeftShadow(true, debugX + 4, Color.WHITE.hashCode(), "Levels gained:", debugY += 15);
			chatTextDrawingArea.textRightShadow(true, debugX + debugWidth - 4, Color.YELLOW.hashCode(), "" + (calculateTotalLevels() - customSettingShowExperiencePerHourStartLevels), debugY);
		}

		// Draw main screen
		tabAreaBuffer.drawGraphics(4, super.graphics, 4);

		if(graphicsEnabled) {
			xCameraPos = l;
			zCameraPos = i1;
			yCameraPos = j1;
			yCameraCurve = k1;
			xCameraCurve = l1;
		}
	}

	public void closeOpenInterfaces() {
		stream.createFrame(130);
		if (invOverlayInterfaceID != -1) {
			invOverlayInterfaceID = -1;
			needDrawTabArea = true;
			actionPending = false;
			tabAreaAltered = true;
		}
		if (backDialogID != -1) {
			backDialogID = -1;
			inputTaken = true;
			actionPending = false;
		}
		if (fullScreenInterfaceId != -1) {
			fullScreenInterfaceId = -1;
		}
		if (openInterfaceID != -1)
			openInterfaceID = -1;
	}

	public Game() {
	    //Test if they're on 32-bit, warn them if they are
		if (!System.getProperty("sun.arch.data.model").contains("64"))
		{
			JOptionPane.showMessageDialog(null, "You're running 32-bit java. This will definitely cause problems.\nYou can get the right Java 8 at AdoptOpenJDK.net", "You're running 32-bit Java!", JOptionPane.INFORMATION_MESSAGE);
			System.out.println("Please upgrade to 64-bit java to avoid problems! (AdoptOpenJDK.net)");
		}
		if (Double.parseDouble(System.getProperty("java.specification.version")) >= 1.9) {
			JOptionPane.showMessageDialog(null, "You're not running Java 8. If you're using Parabot, this will cause problems!\nYou can get Java 8 from AdoptOpenJDK.net", "You're not running Java 8!", JOptionPane.INFORMATION_MESSAGE);
			System.out.println("Please downgrade to Java 8 to avoid problems! (AdoptOpenJDK.net)");
		}
		server = ClientSettings.SERVER_IP;
		pathDistances = new int[104][104];
		friendsNodeIDs = new int[200];
		groundArray = new NodeList[4][104][104];
		flameThreadActive = false;
		chatBuffer = new Stream(new byte[5000]);
		npcArray = new NPC[16384];
		npcIndices = new int[16384];
		removedEntityIndices = new int[1000];
		updateBuffer = Stream.create();
		soundEffectEnabled = true;
		openInterfaceID = -1;
		currentExp = new int[Skills.skillsCount];
		useJaggrab = false;
		cameraShakeAmplitude = new int[5];
		selectedTargetId = -1;
		tabFlashing = new boolean[5];
		drawFlames = false;
		reportAbuseInput = "";
		localPlayerIndex = -1;
		menuOpen = false;
		inputString = "";
		maxPlayers = 2048;
		myPlayerIndex = 2047;
		playerArray = new Player[maxPlayers];
		playerIndices = new int[maxPlayers];
		playerUpdateIndices = new int[maxPlayers];
		playerBuffers = new Stream[maxPlayers];
		cameraYawOffsetSpeed = 1;
		pathDirections = new int[104][104];
		scrollBarLightColor = 0x766654;
		soundPayload = new byte[16384];
		currentStats = new int[Skills.skillsCount];
		ignoreListAsLongs = new long[100];
		loadingError = false;
		scrollBarDarkColor = 0x332d25;
		cameraShakeSpeed = new int[5];
		occupiedTiles = new int[104][104];
		chatTypes = new int[100];
		chatNames = new String[100];
		chatMessages = new String[100];
		sideIcons = new Background[13];
		hasFocus = true;
		friendsListAsLongs = new long[200];
		currentSong = -1;
		drawingFlames = false;
		spriteDrawX = -1;
		spriteDrawY = -1;
		mapBackLeft = new int[33];
		flameLineOffsets = new int[256];
		decompressors = new Decompressor[5];
		variousSettings = new int[2000];
		scrollBarDragging = false;
		maxDisplayedText = 50;
		textX = new int[maxDisplayedText];
		textY = new int[maxDisplayedText];
		textHeight = new int[maxDisplayedText];
		textWidth = new int[maxDisplayedText];
		textColors = new int[maxDisplayedText];
		textEffects = new int[maxDisplayedText];
		textCycles = new int[maxDisplayedText];
		overheadTexts = new String[maxDisplayedText];
		lastPlane = -1;
		hitMarks = new Sprite[20];
		characterColorIndices = new int[5];
		scrollBarColor = 0x23201b;
		amountOrNameInput = "";
		projectileList = new NodeList();
		cameraUpdatePending = false;
		overlayInterfaceId = -1;
		cameraShakeCycle = new int[5];
		characterDesignChanged = false;
		mapFunctions = new Sprite[100];
		dialogID = -1;
		maxStats = new int[Skills.skillsCount];
		varpArray = new int[2000];
		isMaleCharacter = true;
		minimapLineOffset = new int[151];
		flashingTabId = -1;
		graphicsObjectList = new NodeList();
		mapBackWidths = new int[33];
		chatScrollComponent = new RSInterface();
		mapScenes = new Background[100];
		scrollBarHandleColor = 0x4d4233;
		characterStyle = new int[7];
		minimapIconX = new int[1000];
		minimapIconY = new int[1000];
		regionLoading = false;
		friendsList = new String[200];
		inStream = Stream.create();
		expectedCRCs = new int[9];
		menuActionCmd2 = new int[500];
		menuActionCmd3 = new int[500];
		menuActionID = new int[500];
		menuActionCmd1 = new int[500];
		headIcons = new Sprite[20];
		headIconsHint = new Sprite[20];
		skullIcons = new Sprite[20];
		tabAreaAltered = false;
		inputPrompt = "";
		atPlayerActions = new String[5];
		atPlayerArray = new boolean[5];
		dynamicRegionData = new int[4][13][13];
		cameraYOffsetSpeed = 2;
		minimapIconSprites = new Sprite[1000];
		forceMapReload = false;
		actionPending = false;
		crosses = new Sprite[8];
		musicEnabled = true;
		needDrawTabArea = false;
		loggedIn = false;
		canMute = false;
		isDynamicRegion = false;
		isCameraLocked = false;
		minimapVerticalSpeed = 1;
		myUsername = "";
		myPassword = "";
		genericLoadingError = false;
		reportAbuseInterfaceID = -1;
		pendingSpawns = new NodeList();
		cameraPitch = 128;
		invOverlayInterfaceID = -1;
		stream = Stream.create();
		menuActionName = new String[500];
		cameraShakeFrequency = new int[5];
		sound = new int[50];
		minimapHorizontalSpeed = 2;
		chatScrollHeight = 78;
		promptInput = "";
		modIcons = new Background[2];
		tabID = 3;
		inputTaken = false;
		songChanging = true;
		minimapLineLengths = new int[151];
		collisionMaps = new CollisionMap[4];
		chatSettingsUpdateNeeded = false;
		soundType = new int[50];
		itemBeingDragged = false;
		soundDelay = new int[50];
		soundVolume = new int[50];
		rsAlreadyLoaded = false;
		welcomeScreenRaised = false;
		messagePromptRaised = false;
		loginMessage1 = "";
		loginMessage2 = "";
		backDialogID = -1;
		cameraXOffsetSpeed = 2;
		pathTileX = new int[4000];
		pathTileY = new int[4000];
		unusedSlotIndex = -1;
		fileCRC = new CRC32();
	}
	public CRC32 fileCRC;
	public static String server;
	public int ignoreCount;
	public long loadingStartTime;
        public int[][] pathDistances;
	public int[] friendsNodeIDs;
	public NodeList[][][] groundArray;
        public int[] flameBuffer1;
        public int[] flameBuffer2;
	public volatile boolean flameThreadActive;
	public Socket jaggrabSocket;
	public int loginScreenState;
	public Stream chatBuffer;
	public NPC[] npcArray;
	public int npcCount;
	public int[] npcIndices;
	public int entityRemovalCount;
	public int[] removedEntityIndices;
	public int lastPacketType;
	public int prevPktType;
	public int prevPktType2;
	public String messagePrompt;
	public int publicChatMode;
	public int privateChatMode;
	public Stream updateBuffer;
	public boolean soundEffectEnabled;
	public static int systemUpdateCounter;
	public int[] flameBuffer;
	public int[] flamePaletteRed;
	public int[] flamePaletteGreen;
	public int[] flamePaletteBlue;
	public static int unusedCounter;
	public int hintIconState;
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
	public boolean useJaggrab;
        public final int[] cameraShakeAmplitude;
	public int selectedTargetId;
	public final boolean[] tabFlashing;
	public int weight;
	public MouseDetection mouseDetection;
	public volatile boolean drawFlames;
	public String reportAbuseInput;
	public int localPlayerIndex;
	public boolean menuOpen;
	public int hoveredWidgetId;
	public String inputString;
	public final int maxPlayers;
	public final int myPlayerIndex;
	public Player[] playerArray;
	public int playerCount;
	public int[] playerIndices;
	public int playerUpdateCount;
	public int[] playerUpdateIndices;
	public Stream[] playerBuffers;
	public int cameraYawOffset;
	public int cameraYawOffsetSpeed;
	public int friendsCount;
	public int interfaceMode;
        public int[][] pathDirections;
        public final int scrollBarLightColor;
	public RSImageProducer backLeftIP1;
	public RSImageProducer backLeftIP2;
	public RSImageProducer backRightIP1;
	public RSImageProducer backRightIP2;
	public RSImageProducer backTopIP1;
	public RSImageProducer backVmidIP1;
	public RSImageProducer backVmidIP2;
	public RSImageProducer backVmidIP3;
	public RSImageProducer midSubscreenBuffer;
	public byte[] soundPayload;
	public int configActionId;
	public int crossX;
	public int crossY;
	public int crossIndex;
	public int crossType;
	public int plane;
	public final int[] currentStats;
        public static int objectClickCounter;
	public final long[] ignoreListAsLongs;
	public boolean loadingError;
        public final int scrollBarDarkColor;
        public final int[] cameraShakeSpeed;
        public int[][] occupiedTiles;
	public Sprite maleIconSprite;
	public Sprite femaleIconSprite;
	public int selectedPlayerId;
	public int selectedNpcId;
	public int destinationX;
	public int destinationY;
	public int lastMouseX;
	public int lastMouseY;
	public static int abuseReportCounter;
	public final int[] chatTypes;
	public final String[] chatNames;
	public final String[] chatMessages;
	public int animationCycle;
	public WorldController worldController;
	public Background[] sideIcons;
	public int menuScreenArea;
	public int menuOffsetX;
	public int menuOffsetY;
	public int menuWidth;
	public int menuHeight;
	public long privateMessageRecipient;
	public boolean hasFocus;
	public long[] friendsListAsLongs;
	public int currentSong;
	public static int nodeID = 10;
	static int portOff;
	public static boolean isMembers = true;
	public static boolean lowMem;
	public volatile boolean drawingFlames;
	public int spriteDrawX;
	public int spriteDrawY;
        public final int[] hitmarkColors = {0xffff00, 0xff0000, 0x00ff00, 0x00ffff, 0xff00ff, 0xffffff};
	public Background loginBoxBackground;
	public Background loginButtonBackground;
        public final int[] mapBackLeft;
        public final int[] flameLineOffsets;
	final Decompressor[] decompressors;
	public int variousSettings[];
	public boolean scrollBarDragging;
        public final int maxDisplayedText;
        public final int[] textX;
        public final int[] textY;
        public final int[] textHeight;
        public final int[] textWidth;
        public final int[] textColors;
        public final int[] textEffects;
        public final int[] textCycles;
        public final String[] overheadTexts;
        public int cameraZoom;
        public int lastPlane;
        public static int playerOptionCounter;
	public Sprite[] hitMarks;
	public int idleCycleCounter;
	public int dragCounter;
        public final int[] characterColorIndices;
	public static boolean initialLoadComplete;
	public int cameraFocusX;
	public int cameraFocusY;
	public int cameraFocusHeight;
	public int cameraAdjustSpeed;
	public int cameraAdjustAcceleration;
	public ISAACRandomGen encryption;
	public Sprite mapEdge;
	public final int scrollBarColor;
	static final int[][] appearanceColorOptions = {{6798, 107, 10283, 16, 4797, 7744, 5799, 4634, 33697, 22433, 2983, 54193}, {8741, 12, 64030, 43162, 7735, 8404, 1701, 38430, 24094, 10153, 56621, 4783, 1341, 16578, 35003, 25239}, {25238, 8742, 12, 64030, 43162, 7735, 8404, 1701, 38430, 24094, 10153, 56621, 4783, 1341, 16578, 35003}, {4626, 11146, 6439, 12, 4758, 10270}, {4550, 4537, 5681, 5673, 5790, 6806, 8076, 4574}};
	public String amountOrNameInput;
        public static int cameraMoveCycle;
	public int daysSinceLastLogin;
	public int pktSize;
	public int pktType;
	public int connectionTimeoutCounter;
	public int keepAliveCounter;
	public int reconnectDelay;
	public NodeList projectileList;
	public int cameraX;
	public int cameraY;
	public int cameraUpdateDelay;
	public boolean cameraUpdatePending;
	public int overlayInterfaceId;
        public static final int[] levelExperience;
	public int minimapState;
	public int mouseIdleTicks;
	public int loadingStage;
	public Background scrollBar1;
	public Background scrollBar2;
	public int lastHoveredWidgetId;
	public Background backBase1;
	public Background backBase2;
	public Background backHmid1;
        public final int[] cameraShakeCycle;
	public boolean characterDesignChanged;
	public Sprite[] mapFunctions;
	public int baseX;
	public int baseY;
	public int prevBaseX;
	public int prevBaseY;
	public int loginFailures;
	public int lastInteractionId;
	public int flameMainColor;
	public int flameSecondaryColor;
	public int dialogID;
	public final int[] maxStats;
        public final int[] varpArray;
	public int friendsListStatus;
	public boolean isMaleCharacter;
	public int hoveredTabId;
	public String errorMessage;
	public static int terrainLoadCycle;
	public final int[] minimapLineOffset;
	public StreamLoader titleStreamLoader;
	public int flashingTabId;
	public int multiCombatZone;
	public NodeList graphicsObjectList;
        public final int[] mapBackWidths;
	public final RSInterface chatScrollComponent;
	public Background[] mapScenes;
	public static int drawCycle;
	public int currentSound;
	public final int scrollBarHandleColor;
	public int friendsListAction;
        public final int[] characterStyle;
	public int mouseInvInterfaceIndex;
	public int lastActiveInvInterface;
	public OnDemandFetcher onDemandFetcher;
	public int currentRegionX;
	public int currentRegionY;
	public int minimapIconCount;
	public int[] minimapIconX;
	public int[] minimapIconY;
	public Sprite mapDotItem;
	public Sprite mapDotNPC;
	public Sprite mapDotPlayer;
	public Sprite mapDotFriend;
	public Sprite mapDotTeam;
	public int loadingPercent;
	public boolean regionLoading;
	public String[] friendsList;
	public Stream inStream;
	public int dragInterfaceId;
	public int draggedSlot;
	public int activeInterfaceType;
        public int dragStartX;
        public int dragStartY;
        public int chatScrollPosition;
	public final int[] expectedCRCs;
	public int[] menuActionCmd2;
	public int[] menuActionCmd3;
	public int[] menuActionID;
	public int[] menuActionCmd1;
	public Sprite[] headIcons;
	public Sprite[] skullIcons;
	public Sprite[] headIconsHint;
	public static int mapLoadPacketCounter;
	public int cameraTargetX;
	public int cameraTargetY;
	public int cameraTargetZ;
	public int cameraMoveSpeed;
	public int cameraMoveAcceleration;
	public boolean tabAreaAltered;
	public int systemUpdateTimer;
	public RSImageProducer titleImageProducer;
	public RSImageProducer loginLeftProducer;
	public RSImageProducer loginRightProducer;
	public RSImageProducer titleLeftProducer;
	public RSImageProducer titleRightProducer;
	public RSImageProducer titleTopLeftProducer;
	public RSImageProducer titleTopRightProducer;
	public RSImageProducer titleBottomLeftProducer;
	public RSImageProducer titleBottomRightProducer;
	public static int antiCheatPacketCounter;
	public int membersInt;
	public String inputPrompt;
	public Sprite compass;
	public RSImageProducer tabAreaIconBuffer;
	public RSImageProducer tabAreaBackgroundBuffer;
	public RSImageProducer mapEdgeBuffer;
	public static Player myPlayer;
	public final String[] atPlayerActions;
	public final boolean[] atPlayerArray;
        public final int[][][] dynamicRegionData;
	public final int[] tabInterfaceIDs = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
        public int cameraYOffset;
        public int cameraYOffsetSpeed;
	public int menuActionRow;
	public static int npcAttackCounter;
	public int spellSelected;
	public int selectedSpellId;
	public int spellUsableOn;
	public String spellTooltip;
	public Sprite[] minimapIconSprites;
        public boolean forceMapReload;
        public static int clickPacketCounter;
	public Background redStone1;
	public Background redStone2;
	public Background redStone3;
	public Background redStone1_2;
	public Background redStone2_2;
	public int energy;
	public boolean actionPending;
	public Sprite[] crosses;
	public boolean musicEnabled;
	public Background[] runeBackgrounds;
	public boolean needDrawTabArea;
	public int unreadMessages;
	public static int npcInteractionCounter;
	public boolean loggedIn;
	public boolean canMute;
	public boolean isDynamicRegion;
	public boolean isCameraLocked;
	static int loopCycle;
	public static final String validUserPassChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!\"\243$%^&*()-_=+[{]};:'@#~,<.>/?\\| ";
	public RSImageProducer textBackground;
	public RSImageProducer chatBackground;
	public RSImageProducer tabAreaBuffer;
	public RSImageProducer fullScreenBackground;
	public int daysSinceRecovChange;
	public RSSocket socketStream;
	public int minimapZoom;
	public int minimapVerticalSpeed;
	public static long lastSoundUpdate;
	public String myUsername;
	public String myPassword;
	public static int itemUseCounter;
	public boolean genericLoadingError;
	public final int[] objectData = {0, 0, 0, 0, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3};
	public int reportAbuseInterfaceID;
	public NodeList pendingSpawns;
	public int[] chatAreaOffsets;
	public int[] tabAreaOffsets;
	public int[] chatBoxAreaOffsets;
	public byte[][] terrainData;
	public int cameraPitch;
	public int cameraYaw;
       public int cameraYawAccel;
       public int cameraPitchAccel;
	public static int actionCounter;
	public int invOverlayInterfaceID;
        public int[] flameGradient1;
        public int[] flameGradient2;
	public Stream stream;
	public int lastLoginIp;
	public int splitpublicChat;
	public Background invBack;
	public Background mapBack;
	public Background chatBack;
	public String[] menuActionName;
	public Sprite titleBackgroundLeft;
	public Sprite titleBackgroundRight;
        public final int[] cameraShakeFrequency;
	static final int[] additionalColorCodes = {9104, 10275, 7595, 3610, 7975, 8526, 918, 38802, 24466, 10145, 58654, 5027, 1457, 16565, 34991, 25486};
	public static boolean flagged;
	public final int[] sound;
	public int flameDrawingCounter;
	public int minimapRotationOffset;
        public int minimapHorizontalSpeed;
        public int chatScrollHeight;
	public String promptInput;
	public int clickCycle;
        public int[][][] tileHeights;
	public long serverSessionKey;
	public int loginScreenCursorPos;
	public final Background[] modIcons;
	public long lastMouseClickTime;
	public int tabID;
	public int hintNpcIndex;
	public boolean inputTaken;
	public int inputDialogState;
	public static int npcClickCounter;
	public int nextSong;
	public boolean songChanging;
        public final int[] minimapLineLengths;
	public CollisionMap[] collisionMaps;
        public static int bitMasks[];
	public boolean chatSettingsUpdateNeeded;
        public int[] regionBaseIds;
        public int[] terrainArchiveIds;
        public int[] objectArchiveIds;
	public int lastMousePacketX;
	public int lastMousePacketY;
        public final int pathSearchMax = 100;
	public final int[] soundType;
	public boolean itemBeingDragged;
	public int atInventoryLoopCycle;
	public int atInventoryInterface;
	public int atInventoryIndex;
	public int atInventoryInterfaceType;
	public byte[][] objectMapData;
	public int tradeMode;
	public int chatEffectsState;
	public final int[] soundDelay;
	public final int[] soundVolume;
       public int restrictedArea;
	public final boolean rsAlreadyLoaded;
	public int oneMouseButtonMode;
	public int minimapRandomTimer;
	public boolean welcomeScreenRaised;
	public boolean messagePromptRaised;
	public static int soundBufferOffset;
        public byte[][][] tileFlags;
	public int previousSong;
	public int destX;
	public int destY;
        public Sprite minimapImage;
        public int alternatePathFound;
	public int waveCycle;
	public String loginMessage1;
	public String loginMessage2;
	public int mapEventX;
	public int mapEventY;
	public TextDrawingArea plainFont;
	public TextDrawingArea boldFont;
	public TextDrawingArea chatTextDrawingArea;
	public int flameOffset;
	public int backDialogID;
	public int cameraXOffset;
	public int cameraXOffsetSpeed;
	public int[] pathTileX;
	public int[] pathTileY;
	public int itemSelected;
        public int selectedItemSlot;
        public int selectedItemInterfaceId;
        public int selectedItemId;
	public String selectedItemName;
        public static int walkPacketCounter;
	public int unusedSlotIndex;
	public static int tiara;
	public static int unusedSettingValue;
	public boolean showInfo = false;
        public static int midiVolume = 256;
	public static int[] midiChannels = new int[] { 12800, 12800, 12800, 12800, 12800, 12800,
	12800, 12800, 12800, 12800, 12800, 12800,
	12800, 12800, 12800, 12800 };
	public static int midiFadeCycles = 0;
        public static MidiPlayer midiPlayer;
	public static boolean fetchMusic = false;
	public static int musicVolume2;
	public static int currentMidiVolume = -1;
	public static byte[] queuedMidiData;
	public static int fadeVolume = 0;
	public static int fadeStep = 0;
	public static int queuedMidiVolume;
	public static boolean midiLooping;
	public static int nextSongDelay;
	public static boolean autoPlaySong;
	public static int queuedSongId;
	public static int musicVolume = 0;
	public int[] gameScreenOffsets;
	public int currentDateOffset;
	public int loginScreenDelay;
	public int lastPasswordChange;
        public int scrollPadding;
        public int unusedRecoveryDate;
	public int recoveryQuestionChangeDate;

	static {
		levelExperience = new int[99];
		int i = 0;
		for (int j = 0; j < 99; j++) {
			int l = j + 1;
			int i1 = (int) ((double) l + 300D * Math.pow(2D, (double) l / 7D));
			i += i1;
			levelExperience[j] = i / 4;
		}

		bitMasks = new int[32];
		i = 2;
		for (int k = 0; k < 32; k++) {
			bitMasks[k] = i - 1;
			i += i;
		}

	}

	void mouseWheelDragged(int i, int j) {
		if (!mouseWheelDown)
			return;
               this.cameraYawAccel += i * 3;
               this.cameraPitchAccel += (j << 1);
       }

	public void keyPressed(KeyEvent keyevent)
	{
		super.keyPressed(keyevent);
		switch (keyevent.getKeyCode())
		{
			case KeyEvent.VK_ESCAPE:
				closeOpenInterfaces();
				break;
			case KeyEvent.VK_F1:
				needDrawTabArea = true;
				tabID = 3;
				tabAreaAltered = true;
				break;
			case KeyEvent.VK_F2:
				needDrawTabArea = true;
				tabID = 4;
				tabAreaAltered = true;
				break;
			case KeyEvent.VK_F3:
				needDrawTabArea = true;
				tabID = 5;
				tabAreaAltered = true;
				break;
			case KeyEvent.VK_F4:
				needDrawTabArea = true;
				tabID = 6;
				tabAreaAltered = true;
				break;
			case KeyEvent.VK_F5:
				needDrawTabArea = true;
				tabID = 0;
				tabAreaAltered = true;
				break;
			case KeyEvent.VK_F6:
				needDrawTabArea = true;
				tabID = 1;
				tabAreaAltered = true;
				break;
			case KeyEvent.VK_F7:
				needDrawTabArea = true;
				tabID = 2;
				tabAreaAltered = true;
				break;
			case KeyEvent.VK_F8:
				needDrawTabArea = true;
				tabID = 8;
				tabAreaAltered = true;
				break;
			case KeyEvent.VK_F9:
				needDrawTabArea = true;
				tabID = 11;
				tabAreaAltered = true;
				break;
			case KeyEvent.VK_F10:
				needDrawTabArea = true;
				tabID = 12;
				tabAreaAltered = true;
				break;
			case KeyEvent.VK_F11:
				needDrawTabArea = true;
				tabID = 13;
				tabAreaAltered = true;
				break;
			case KeyEvent.VK_F12:
				needDrawTabArea = true;
				tabID = 10;
				tabAreaAltered = true;
				break;
			case KeyEvent.VK_PAGE_UP:
				if (zoom > -1) {
					zoom--;
					if (ClientSettings.SHOW_ZOOM_LEVEL_MESSAGES) {
						pushMessage("Your zoom level is now: " + zoom, 0, "");
					}
				}
				break;
			case KeyEvent.VK_PAGE_DOWN:
				if (zoom < (WorldController.drawDistance / 3)) {
					zoom++;
					if (ClientSettings.SHOW_ZOOM_LEVEL_MESSAGES) {
						pushMessage("Your zoom level is now: " + zoom, 0, "");
					}
				}
				break;
			case KeyEvent.VK_V:
				if (keyevent.isControlDown()) {
					inputString += getClipBoard();
					if (inputString.length() > 80) {
						inputString = inputString.substring(0, 80);
					}
					inputTaken = true;
				}

		}
		  if (ClientSettings.SCREENSHOTS_ENABLED && keyevent.getKeyCode() == KeyEvent.VK_PRINTSCREEN && keyevent.isControlDown()) {
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
				ItemDef item = ItemDef.lookup(definition);
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
				if (ID > 0 && itemCount < class9_1.inv.length) {
					class9_1.inv[itemCount] = ID + 1; // Sets item ID;
					class9_1.invStackSizes[itemCount++] = 1; // Sets item amoounts
				}
			}
			while (itemCount < class9_1.inv.length) {
				class9_1.inv[itemCount] = 0;
				class9_1.invStackSizes[itemCount++] = 0;
			}
		} else {
			pushMessage("@blu@" + sType + " @bla@search results for @blu@" + name + "@bla@ displayed above (@blu@" + definitionResultsTotal + "@bla@ results).", 0, "");
		}
    }

	public void openInterface(int interfaceID) {
               resetInterfaceAnimation(interfaceID);
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
			if (ClientSettings.SNOW_OVERLAY_FORCE_ENABLED || (ClientSettings.SNOW_OVERLAY_ENABLED && FloorOverlay.getTodaysDate().contains(ClientSettings.SNOW_MONTH))) {
				openInterfaceID = 15819;
			} else {
				openInterfaceID = 15801;
			}
			fullScreenInterfaceId = 15244;
		} else {
			openInterfaceID = interfaceID;
		}
		actionPending = false;
	}

	public void openSideInterface(int tab, int interfaceID) {
		if (interfaceID == 0x00ffff) {
			interfaceID = -1;
		}
		tabInterfaceIDs[tab] = interfaceID;
		needDrawTabArea = true;
		tabAreaAltered = true;
	}

	public final void mouseWheelMoved(MouseWheelEvent e) {
		int notches = e.getWheelRotation();
		if (ClientSettings.CONTROL_KEY_ZOOMING && !e.isControlDown()) {
			return;
		}
		// If mouse over main game screen, without anything else opened
		if (openInterfaceID == -1 && mouseX < 515 && mouseY < 340) {
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

	public String getClipBoard(){
		String myString = "";
		try {
			myString = (String)Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
		} catch (HeadlessException e) {
			e.printStackTrace();            
		} catch (UnsupportedFlavorException e) {
			e.printStackTrace();            
		} catch (IOException e) {
			e.printStackTrace();
		}

		String output = "";
		for(int i = 0; i < myString.length(); i++) {
			int j = (int) myString.charAt(i);
			if (j >= 32 && j <= 122) {
				output += (char) j;
			}
		}

		return output;
	}
}
