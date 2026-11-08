import java.lang.reflect.Field;
import org.junit.Test;
import static org.junit.Assert.*;

public class PlayerChatRightsTest {
    private static final String[] CENSOR_FIELDS = {"allowedFragmentCodes", "badWords", "badWordExemptionPairs", "domains", "topLevelDomains", "topLevelDomainStrictness"};
    private final Object[] savedCensor = new Object[6];
    @org.junit.Before public void saveCensor() throws Exception {
        for (int i = 0; i < CENSOR_FIELDS.length; i++) {
            Field f = Censor.class.getDeclaredField(CENSOR_FIELDS[i]); f.setAccessible(true); savedCensor[i] = f.get(null);
        }
    }
    @org.junit.After public void restoreCensor() throws Exception {
        for (int i = 0; i < CENSOR_FIELDS.length; i++) emptyCensor(CENSOR_FIELDS[i], savedCensor[i]);
    }
    private static final class ScreenProbe extends Game {
        @Override public void projectEntityToScreen(Entity entity, int heightOffset) {
            spriteDrawX = 200; spriteDrawY = 100;
        }
    }
    private static TextDrawingArea font() throws Exception {
        TextDrawingArea f = (TextDrawingArea) unsafe().allocateInstance(TextDrawingArea.class);
        byte[][] masks = new byte[256][];
        int[] widths = new int[256], heights = new int[256], advances = new int[256];
        for (int i = 0; i < 256; i++) {
            masks[i] = new byte[15]; java.util.Arrays.fill(masks[i], (byte) 1);
            widths[i] = 3; heights[i] = 5; advances[i] = 6;
        }
        String[] names = {"glyphMasks", "glyphWidthPixels", "glyphHeightPixels", "glyphXOffsetPixels", "glyphYOffsetPixels", "glyphAdvancePixels"};
        Object[] values = {masks, widths, heights, new int[256], new int[256], advances};
        for (int i = 0; i < names.length; i++) {
            Field field = TextDrawingArea.class.getDeclaredField(names[i]); field.setAccessible(true); field.set(f, values[i]);
        }
        f.maxAsciiGlyphHeightPixels = 8; return f;
    }
    private static boolean hasColor(int color) {
        for (int pixel : DrawingArea.pixels) if (pixel == color) return true;
        return false;
    }
    private static sun.misc.Unsafe unsafe() throws Exception {
        Field f = sun.misc.Unsafe.class.getDeclaredField("theUnsafe"); f.setAccessible(true);
        return (sun.misc.Unsafe) f.get(null);
    }
    @Test public void independentBytesAndReversedPayloadReachSpeakerAndHistory() throws Exception {
        for (int[] pair : new int[][] {{2, 4}, {4, 2}, {0, 255}, {255, 0}}) {
            for (String text : new String[] {"hello", "testing 123"}) {
                Game g = game(); Player p = speaker();
                Stream encoded = new Stream(new byte[100]); TextInput.encodeChatText(text, encoded);
                byte[] wire = new byte[4 + encoded.currentOffset];
                wire[0] = (byte) pair[1]; wire[1] = (byte) pair[0]; wire[2] = 3;
                wire[3] = (byte) -encoded.currentOffset;
                for (int i = 0; i < encoded.currentOffset; i++) wire[4 + i] = encoded.buffer[encoded.currentOffset - 1 - i];
                Stream packet = new Stream(wire); g.decodePlayerUpdateMask(0x80, 0, packet, p);
                assertEquals(pair[0], p.overheadChatColorCode); assertEquals(pair[1], p.overheadChatEffectCode);
                assertEquals(3, p.rights); assertEquals(text.substring(0, 1).toUpperCase() + text.substring(1), p.textSpoken.trim());
                assertEquals(p.textSpoken, g.chatMessages[0]); assertEquals(wire.length, packet.currentOffset);
                g.chatSuppressionZone = 1; p.textSpoken = "unchanged";
                g.decodePlayerUpdateMask(0x80, 0, new Stream(wire), p); assertEquals("unchanged", p.textSpoken);
            }
        }
    }
    @Test public void styleFieldsSelectIndependentColorAndDrawingPrimitive() throws Exception {
        Player saved = Game.myPlayer; boolean android = ClientSettings.ANDROID;
        int[] raster = DrawingArea.pixels; int width = DrawingArea.width, height = DrawingArea.height;
        int[] clip = {DrawingArea.topX, DrawingArea.topY, DrawingArea.bottomX, DrawingArea.bottomY};
        try {
            ClientSettings.ANDROID = false; ScreenProbe g = new ScreenProbe();
            g.chatTextDrawingArea = font(); g.sceneDrawCycle = 7;
            DrawingArea.bindRaster(334, 512, new int[512 * 334]);
            Player p = speaker(); p.headIcon = -1; p.loopCycleStatus = -1; p.textSpoken = "hello"; p.textCycle = 127;
            Game.myPlayer = p;
            java.util.Set<Integer> shapes = new java.util.HashSet<Integer>();
            for (int effect = 0; effect <= 5; effect++) {
                p.overheadChatEffectCode = effect;
                for (int color = 0; color < 6; color++) {
                    p.overheadChatColorCode = color; java.util.Arrays.fill(DrawingArea.pixels, 0); g.updateEntities();
                    assertTrue("effect=" + effect + " color=" + color, hasColor(g.anIntArray965[color]));
                }
                shapes.add(java.util.Arrays.hashCode(DrawingArea.pixels));
            }
            assertEquals("six distinct rendered effects", 6, shapes.size());
            p.overheadChatEffectCode = 5; p.overheadChatColorCode = 1; p.textCycle = 140;
            java.util.Arrays.fill(DrawingArea.pixels, 0); g.updateEntities();
            assertFalse("slide starts outside its clip", hasColor(0xff0000));
            p.textCycle = 127;
            p.overheadChatEffectCode = 0; p.overheadChatColorCode = 6;
            g.sceneDrawCycle = 9; java.util.Arrays.fill(DrawingArea.pixels, 0); g.updateEntities(); assertTrue(hasColor(0xff0000)); assertFalse(hasColor(0xffff00));
            g.sceneDrawCycle = 10; java.util.Arrays.fill(DrawingArea.pixels, 0); g.updateEntities(); assertTrue(hasColor(0xffff00)); assertFalse(hasColor(0xff0000));
            p.overheadChatEffectCode = 2; p.overheadChatColorCode = 1; g.chatEffectsMode = 1;
            java.util.Arrays.fill(DrawingArea.pixels, 0); g.updateEntities(); assertTrue(hasColor(0xffff00)); assertFalse(hasColor(0xff0000));
            int[] disabled = DrawingArea.pixels.clone(); p.overheadChatEffectCode = 0; g.chatEffectsMode = 0; p.overheadChatColorCode = 0;
            java.util.Arrays.fill(DrawingArea.pixels, 0); g.updateEntities(); assertArrayEquals(disabled, DrawingArea.pixels);
        } finally {
            Game.myPlayer = saved; ClientSettings.ANDROID = android;
            DrawingArea.bindRaster(height, width, raster);
            DrawingArea.setClip(clip[3], clip[0], clip[2], clip[1]);
        }
    }
    private static Game game() throws Exception {
        Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        Game game = (Game) ((sun.misc.Unsafe) field.get(null)).allocateInstance(Game.class);
        set(game, "chatTypes", new int[100]); set(game, "chatNames", new String[100]); set(game, "chatMessages", new String[100]);
        game.aStream_834 = new Stream(new byte[5000]);
        emptyCensor("allowedFragmentCodes", new int[0]); emptyCensor("badWords", new char[0][]); emptyCensor("badWordExemptionPairs", new byte[0][][]);
        emptyCensor("domains", new char[0][]); emptyCensor("topLevelDomains", new char[0][]); emptyCensor("topLevelDomainStrictness", new int[0]);
        return game;
    }
    private static void emptyCensor(String name, Object value) throws Exception {
        Field field = Censor.class.getDeclaredField(name); field.setAccessible(true); field.set(null, value);
    }
    private static void set(Game game, String name, Object value) throws Exception {
        Field field = Game.class.getDeclaredField(name); field.setAccessible(true); field.set(game, value);
    }
    private static Stream chat(int rights) {
        return new Stream(new byte[] {0x12, 0x34, (byte) rights, (byte) 253, 0, 0, 0});
    }
    private static Player speaker() { Player p = new Player(); p.name = "Speaker"; p.visible = true; return p; }

    @Test public void privelageHoldsTheRightsByteOfTheChatMask() throws Exception {
        for (int rights = 0; rights <= 3; rights++) {
            Game game = game(); game.ignoreCount = 0; Player p = speaker();
            Stream s = chat(rights);
            game.decodePlayerUpdateMask(0x80, 0, s, p);
            assertEquals(rights, p.rights);
            assertEquals(7, s.currentOffset);
        }
    }

    @Test public void rightsOfTwoOrMoreBypassIgnoreListWhileZeroAndOneAreIgnored() throws Exception {
        for (int rights = 0; rights <= 3; rights++) {
            Game game = game(); Player p = speaker();
            set(game, "ignoreListAsLongs", new long[] {TextClass.encodeBase37Name("Speaker")});
            game.ignoreCount = 1;
            game.decodePlayerUpdateMask(0x80, 0, chat(rights), p);
            assertEquals(rights <= 1 ? 0 : rights, p.rights);
        }
    }

    @Test public void rightsSelectCrownPrefixOfTheChatLineName() throws Exception {
        String[] expected = {"Speaker", "@cr1@Speaker", "@cr2@Speaker", "@cr2@Speaker"};
        for (int rights = 0; rights <= 3; rights++) {
            Game game = game(); game.ignoreCount = 0; Player p = speaker();
            game.decodePlayerUpdateMask(0x80, 0, chat(rights), p);
            assertEquals(expected[rights], game.chatNames[0]);
        }
    }
}
