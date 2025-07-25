package core.handlers;

import core.engine.Game;
import core.managers.ObjectManager;
import core.network.Signlink;
import core.network.Stream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;

/** Handles game loading stages and map loading extracted from {@link Game}. */
public final class LoadingHandler {
  private final Game game;

  public LoadingHandler(Game game) {
    this.game = game;
  }

  public void loadingStages() {
    if (game.lowMem && game.loadingStage == 2 && ObjectManager.currentPlane != game.plane) {
      game.drawTextOnScreen(null, "Loading - please wait.");
      game.loadingStage = 1;
      game.loadingStartTime = System.currentTimeMillis();
    }
    if (game.loadingStage == 1) {
      int j = checkMapLoadStatus();
      if (j != 0 && System.currentTimeMillis() - game.loadingStartTime > 0x57e40L) {
        Signlink.reporterror(
            game.myUsername
                + " glcfb "
                + game.serverSessionKey
                + ","
                + j
                + ","
                + game.lowMem
                + ","
                + game.decompressors[0]
                + ","
                + game.onDemandFetcher.getNodeCount()
                + ","
                + game.plane
                + ","
                + game.currentRegionX
                + ","
                + game.currentRegionY);
        game.loadingStartTime = System.currentTimeMillis();
      }
    }
    if (game.loadingStage == 2 && game.plane != game.lastPlane) {
      game.lastPlane = game.plane;
      game.generateMinimap(game.plane);
    }
  }

  public int checkMapLoadStatus() {
    for (int i = 0; i < game.terrainData.length; i++) {
      if (game.terrainData[i] == null && game.terrainArchiveIds[i] != -1) {
        return -1;
      }
      if (game.objectMapData[i] == null && game.objectArchiveIds[i] != -1) {
        return -2;
      }
    }

    boolean flag = true;
    for (int j = 0; j < game.terrainData.length; j++) {
      byte abyte0[] = game.objectMapData[j];
      if (abyte0 != null) {
        int k = (game.regionBaseIds[j] >> 8) * 64 - game.baseX;
        int l = (game.regionBaseIds[j] & 0xff) * 64 - game.baseY;
        if (game.isDynamicRegion) {
          k = 10;
          l = 10;
        }
        flag &= ObjectManager.areObjectsReady(k, abyte0, l);
      }
    }

    if (!flag) {
      return -3;
    }
    if (game.regionLoading) {
      return -4;
    } else {
      game.loadingStage = 2;
      ObjectManager.currentPlane = game.plane;
      game.constructMapRegion();
      game.stream.createFrame(121);
      return 0;
    }
  }

  /** Retrieve update CRCs from the web server. */
  public void connectServer() {
    int j = 5;
    game.expectedCRCs[8] = 0;
    int k = 0;
    while (game.expectedCRCs[8] == 0) {
      String s = "Unknown problem";
      game.drawLoadingText(20, "Connecting to web server");
      try {
        DataInputStream datainputstream =
            game.openJagGrabInputStream("crc" + (int) (Math.random() * 99999999D) + "-" + 317);
        Stream crcStream = new Stream(new byte[40]);
        datainputstream.readFully(crcStream.buffer, 0, 40);
        datainputstream.close();
        for (int i1 = 0; i1 < 9; i1++) game.expectedCRCs[i1] = crcStream.readDWord();

        int j1 = crcStream.readDWord();
        int k1 = 1234;
        for (int l1 = 0; l1 < 9; l1++) k1 = (k1 << 1) + game.expectedCRCs[l1];

        if (j1 != k1) {
          s = "checksum problem";
          game.expectedCRCs[8] = 0;
        }
      } catch (EOFException _ex) {
        s = "EOF problem";
        game.expectedCRCs[8] = 0;
      } catch (IOException _ex) {
        s = "FileServer Connection problem";
        String cacheDir = Signlink.findcachedir();
        game.expectedCRCs[8] = new File(cacheDir + "main_file_cache.dat").length() > 0 ? 1 : 0;
      } catch (Exception _ex) {
        s = "logic problem";
        game.expectedCRCs[8] = 0;
        if (!Signlink.reporterror) return;
      }
      if (game.expectedCRCs[8] == 0) {
        k++;
        for (int l = j; l > 0; l--) {
          if (k >= 10) {
            game.drawLoadingText(10, "Game updated - please reload page");
            l = 10;
          } else {
            game.drawLoadingText(10, s + " - retry in " + l + " secs.");
          }
          try {
            Thread.sleep(1000L);
          } catch (Exception _ex) {
          }
        }

        j *= 2;
        if (j > 60) j = 60;
        game.useJaggrab = !game.useJaggrab;
      }
    }
  }
}
