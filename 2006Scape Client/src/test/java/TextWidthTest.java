import static org.junit.Assert.*;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Base64;
import org.junit.Test;

public class TextWidthTest {
    // Genuine constructor fixture: solid rectangular glyphs, known unequal widths.
    private static final String ARCHIVE = "ABV4AABsMUFZJlNZgggZLwACfsP0/6EAAgIBAAEAAgABAQAQAAAIIABwUyYmQZGBSlQYTQGmtVVkqrjKA1xKrrigd4b6dCqbYTfBb42229effXlWcSmKLjBRqyAfsnx3/c+PWnOiv4u5IpwoSEEEDJeA";
    public static TextDrawingArea font(boolean wideSpace) {
        return new TextDrawingArea(wideSpace, "fixture", new StreamLoader(Base64.getDecoder().decode(ARCHIVE)));
    }
    private static int[] advances(TextDrawingArea font) throws Exception {
        Field field = TextDrawingArea.class.getDeclaredField("glyphAdvancePixels");
        field.setAccessible(true); return (int[])field.get(font);
    }
    @Test public void constructorAdvancesArePixelsBetweenOriginsRatherThanInkWidths() throws Exception {
        TextDrawingArea font = font(false);
        assertEquals(5, font.measureLiteralTextWidth("A")); assertEquals(8, font.measureLiteralTextWidth("B"));
        assertEquals(11, font.measureLiteralTextWidth("W")); assertEquals(3, font.measureLiteralTextWidth("i"));
        assertEquals(13, font.measureLiteralTextWidth("AB")); assertEquals(18, font.measureLiteralTextWidth("ABA"));
        assertEquals(3, font.measureLiteralTextWidth(" ")); assertEquals(6, font(true).measureLiteralTextWidth(" "));
        assertEquals(256, advances(font).length);
        int[] saved = DrawingArea.pixels;
        int[] state = {DrawingArea.width, DrawingArea.height, DrawingArea.topX, DrawingArea.topY,
            DrawingArea.bottomX, DrawingArea.bottomY, DrawingArea.scanlineRightLimit,
            DrawingArea.clipRightHalf, DrawingArea.clipBottomHalf};
        try {
            int[] pixels = new int[40*20]; DrawingArea.bindRaster(20, 40, pixels);
            font.textLeft(0x123456, "AA", 10, 2);
            // Each A has ink columns 3..5 then 8..10: origins differ by 5px.
            for (int x=0; x<40; x++) assertEquals("x="+x,
                (x>=3 && x<=5 || x>=8 && x<=10) ? 0x123456 : 0, pixels[3*40+x]);
            assertEquals(10, font.measureLiteralTextWidth("AA")); // Ink span is only 8px.
        } finally {
            DrawingArea.pixels=saved; DrawingArea.width=state[0]; DrawingArea.height=state[1];
            DrawingArea.topX=state[2]; DrawingArea.topY=state[3]; DrawingArea.bottomX=state[4];
            DrawingArea.bottomY=state[5]; DrawingArea.scanlineRightLimit=state[6];
            DrawingArea.clipRightHalf=state[7]; DrawingArea.clipBottomHalf=state[8];
        }
    }
    @Test public void literalMeasurementCountsEveryLatin1CodeUnitIncludingTagDelimiters() throws Exception {
        TextDrawingArea font=font(false); int[] values=advances(font);
        for(int c=0;c<256;c++) assertEquals("code unit "+c, c==32 ? 3 :
            c==65 ? 5 : c==66 ? 8 : c==73 ? 6 : c==105 ? 3 : c==87 ? 11 : 4,
            font.measureLiteralTextWidth(String.valueOf((char)c)));
        assertEquals(33, font.measureLiteralTextWidth("A@red@B"));
        assertEquals(5+3+8, font.measureLiteralTextWidth("A B"));
        assertEquals(8, font.measureLiteralTextWidth("\u0000\u00ff"));
        int[] before=values.clone(); font.measureLiteralTextWidth("ABA"); assertArrayEquals(before,values);
    }
    @Test public void taggedMeasurementSkipsAnyFiveUnitDelimitedSequenceWithoutColorValidation() {
        TextDrawingArea font=font(false);
        for(String tag:new String[]{"@red@","@xyz@","@str@","@end@","@@@@@","@\u0100xy@"}) {
            assertEquals(tag,13,font.measureTaggedTextWidth("A"+tag+"B"));
            assertEquals(tag,0,font.measureTaggedTextWidth(tag));
        }
        assertEquals(13,font.measureTaggedTextWidth("@red@A@xyz@@end@B@str@"));
        assertEquals(18,font.measureTaggedTextWidth("A@red@BA"));
    }
    @Test public void malformedAndAdjacentDelimitersHaveExactPositionalBoundaries() {
        TextDrawingArea font=font(false);
        String[] literal={"@", "@red", "red@", "@re@", "@reds@", "A@red", "A@re@B"};
        int[] widths={4,16,16,16,24,21,29};
        for(int i=0;i<literal.length;i++) assertEquals(literal[i],widths[i],font.measureTaggedTextWidth(literal[i]));
        assertEquals(4,font.measureTaggedTextWidth("@@@@@@"));
        assertEquals(13,font.measureTaggedTextWidth("A@red@@blu@B"));
        assertEquals(17,font.measureTaggedTextWidth("A@@red@B"));
    }
    @Test public void nullEmptyAndUnsupportedUtf16KeepTheirContracts() {
        TextDrawingArea font=font(false);
        assertEquals(0,font.measureLiteralTextWidth(null)); assertEquals(0,font.measureTaggedTextWidth(null));
        assertEquals(0,font.measureLiteralTextWidth("")); assertEquals(0,font.measureTaggedTextWidth(""));
        for(String text:new String[]{"\u0100","\ud83d\ude00","A\u0100"}) {
            try {font.measureLiteralTextWidth(text); fail(text);} catch(ArrayIndexOutOfBoundsException expected) {}
            try {font.measureTaggedTextWidth(text); fail(text);} catch(ArrayIndexOutOfBoundsException expected) {}
        }
        assertEquals(13,font.measureTaggedTextWidth("A@\ud83d\ude00x@B"));
    }
    @Test public void measurementsReadTheLiveAdvanceTableAndPreserveSignedIntArithmetic() throws Exception {
        TextDrawingArea font=font(false); int[] values=advances(font);
        values['A']=17; assertEquals(25,font.measureLiteralTextWidth("AB")); assertEquals(25,font.measureTaggedTextWidth("A@red@B"));
        values['A']=Integer.MAX_VALUE; values['B']=1;
        assertEquals(Integer.MIN_VALUE,font.measureLiteralTextWidth("AB"));
        assertEquals(Integer.MIN_VALUE,font.measureTaggedTextWidth("A@xyz@B"));
        values['A']=-5; assertEquals(-4,font.measureLiteralTextWidth("AB"));
    }
    @Test public void measuringTagsDoesNotToggleRendererStrikeState() throws Exception {
        TextDrawingArea font=font(false);
        Field field=TextDrawingArea.class.getDeclaredField("strikeThroughEnabled"); field.setAccessible(true);
        font.measureTaggedTextWidth("@str@A"); assertFalse(field.getBoolean(font));
        field.setBoolean(font,true); font.measureTaggedTextWidth("@end@A"); assertTrue(field.getBoolean(font));
        assertEquals(5,font.measureTaggedTextWidth("A"));
    }
}
