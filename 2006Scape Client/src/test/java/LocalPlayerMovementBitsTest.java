import static org.junit.Assert.*;
import org.junit.*;

public class LocalPlayerMovementBitsTest {
    private static class Probe extends Game {
        int refreshes;
        @Override public void rebuildScene() { refreshes++; }
    }
    private Probe game;
    private Player saved;
    private int oldPlane;
    private boolean oldRoofs, oldSnow;

    @Before public void setUp() {
        saved = Game.myPlayer;
        oldPlane = ObjectManager.scenePlaneFilter;
        oldRoofs = ObjectManager.hideRoofs;
        oldSnow = ObjectManager.hideBuggyVarrockSwordShopSnow;
        game = new Probe();
        Game.myPlayer = new Player();
        Game.myPlayer.setPos(50, 50, true);
        game.anIntArray894 = new int[8];
        game.anInt893 = 0;
        game.plane = 0;
    }
    @After public void tearDown() {
        Game.myPlayer = saved;
        ObjectManager.scenePlaneFilter = oldPlane;
        ObjectManager.hideRoofs = oldRoofs;
        ObjectManager.hideBuggyVarrockSwordShopSnow = oldSnow;
    }
    private Stream bits(int... fields) {
        int length = 0;
        for (int i = 0; i < fields.length; i += 2) length += fields[i];
        byte[] bytes = new byte[(length + 7) / 8];
        int pos = 0;
        for (int i = 0; i < fields.length; i += 2) {
            int width = fields[i], value = fields[i + 1];
            for (int bit = width - 1; bit >= 0; bit--, pos++)
                if (((value >>> bit) & 1) != 0) bytes[pos / 8] |= 1 << (7 - pos % 8);
        }
        return new Stream(bytes);
    }
    private Stream apply(int... fields) {
        Stream stream = bits(fields);
        game.decodeLocalPlayerMovement(stream);
        return stream;
    }
    @Test public void presenceAndKindSelectDistinctOutcomes() {
        Stream absent = apply(1, 0, 2, 3);
        assertEquals(1, absent.bitOffset);
        assertEquals(0, game.anInt893);
        assertEquals(50, Game.myPlayer.smallX[0]);
        Stream maskOnly = apply(1, 1, 2, 0);
        assertEquals(3, maskOnly.bitOffset);
        assertEquals(1, game.anInt893);
        assertEquals(game.myPlayerIndex, game.anIntArray894[0]);
        assertEquals(50, Game.myPlayer.smallY[0]);
    }
    @Test public void walkingDirectionAndMaskBitAreIndependent() {
        Stream west = apply(1, 1, 2, 1, 3, 3, 1, 0);
        assertEquals(7, west.bitOffset);
        assertEquals(49, Game.myPlayer.smallX[0]);
        assertEquals(50, Game.myPlayer.smallY[0]);
        assertFalse(Game.myPlayer.aBooleanArray1553[0]);
        assertEquals(0, game.anInt893);
        Game.myPlayer.setPos(50, 50, true);
        apply(1, 1, 2, 1, 3, 4, 1, 1);
        assertEquals(51, Game.myPlayer.smallX[0]);
        assertEquals(50, Game.myPlayer.smallY[0]);
        assertEquals(1, game.anInt893);
    }
    @Test public void bothRunDirectionsAreOrderedAndMaskIsSeparate() {
        Stream run = apply(1, 1, 2, 2, 3, 1, 3, 4, 1, 1);
        assertEquals(10, run.bitOffset);
        assertEquals(51, Game.myPlayer.smallX[0]);
        assertEquals(51, Game.myPlayer.smallY[0]);
        assertEquals(50, Game.myPlayer.smallX[1]);
        assertEquals(51, Game.myPlayer.smallY[1]);
        assertTrue(Game.myPlayer.aBooleanArray1553[0]);
        assertTrue(Game.myPlayer.aBooleanArray1553[1]);
        assertEquals(1, game.anInt893);
        Game.myPlayer.setPos(50, 50, true);
        game.anInt893 = 0;
        apply(1, 1, 2, 2, 3, 4, 3, 1, 1, 0);
        assertEquals(51, Game.myPlayer.smallX[0]);
        assertEquals(51, Game.myPlayer.smallY[0]);
        assertEquals(0, game.anInt893);
    }
    @Test public void relocationSeparatesPlaneResetMaskAndXY() {
        Stream near = apply(1, 1, 2, 3, 2, 0, 1, 0, 1, 0, 7, 52, 7, 51);
        assertEquals(21, near.bitOffset);
        assertEquals(51, Game.myPlayer.smallX[0]);
        assertEquals(52, Game.myPlayer.smallY[0]);
        assertEquals(1, Game.myPlayer.smallXYIndex);
        assertEquals(0, game.anInt893);
        assertEquals(1, game.refreshes);
        Game.myPlayer.setPos(50, 50, true);
        apply(1, 1, 2, 3, 2, 0, 1, 1, 1, 0, 7, 52, 7, 51);
        assertEquals(0, Game.myPlayer.smallXYIndex);
        assertEquals(0, game.anInt893);
        Game.myPlayer.setPos(50, 50, true);
        apply(1, 1, 2, 3, 2, 0, 1, 0, 1, 1, 7, 52, 7, 51);
        assertEquals(1, Game.myPlayer.smallXYIndex);
        assertEquals(1, game.anInt893);
        Game.myPlayer.setPos(50, 50, true);
        game.anInt893 = 0;
        apply(1, 1, 2, 3, 2, 2, 1, 0, 1, 0, 7, 52, 7, 51);
        assertEquals(2, game.plane);
        assertEquals(51, Game.myPlayer.smallX[0]);
        assertEquals(52, Game.myPlayer.smallY[0]);
        Game.myPlayer.setPos(50, 50, true);
        apply(1, 1, 2, 3, 2, 0, 1, 0, 1, 0, 7, 53, 7, 51);
        assertEquals(51, Game.myPlayer.smallX[0]);
        assertEquals(53, Game.myPlayer.smallY[0]);
        Game.myPlayer.setPos(50, 50, true);
        apply(1, 1, 2, 3, 2, 0, 1, 0, 1, 0, 7, 52, 7, 52);
        assertEquals(52, Game.myPlayer.smallX[0]);
        assertEquals(52, Game.myPlayer.smallY[0]);
        Game.myPlayer.setPos(50, 50, true);
        game.anInt893 = 0;
        Stream far = apply(1, 1, 2, 3, 2, 2, 1, 1, 1, 1, 7, 77, 7, 20);
        assertEquals(21, far.bitOffset);
        assertEquals(20, Game.myPlayer.smallX[0]);
        assertEquals(77, Game.myPlayer.smallY[0]);
        assertEquals(0, Game.myPlayer.smallXYIndex);
        assertEquals(2, game.plane);
        assertEquals(2, ObjectManager.scenePlaneFilter);
        assertEquals(1, game.anInt893);
        assertFalse(ObjectManager.hideRoofs);
        assertEquals(7, game.refreshes);
    }
}
