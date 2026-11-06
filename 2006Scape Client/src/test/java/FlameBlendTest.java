import static org.junit.Assert.*;
import org.junit.Test;

public class FlameBlendTest {
    private final Game game = new Game();

    // Independent scalar-channel specification, including the returned sign extension.
    public static int expected(int a, int b, int n) {
        int value = 0;
        for (int shift : new int[]{0,8,16}) {
            long x = (a >>> shift) & 255, y = (b >>> shift) & 255;
            int channel = (int) ((x * (256L-n) + y * n) / 256);
            value |= channel << shift;
        }
        return (value << 8) >> 8;
    }

    @Test public void endpointsSelectDifferentOperandsAndIgnoreTheirUpperBytes() {
        assertEquals(0x123456,game.blendRgb256(0x123456,0xabcdef,0));
        assertEquals(0xffabcdef,game.blendRgb256(0x123456,0xabcdef,256));
        assertEquals(0x123456,game.blendRgb256(0xfe123456,0x01abcdef,0));
        assertEquals(0xffabcdef,game.blendRgb256(0xfe123456,0x01abcdef,256));
        assertEquals(0x123456,game.blendRgb256(0xff123456,0x12123456,127));
    }

    @Test public void quarterAndMidpointVectorsSeparateWeightDirectionAndDenominator() {
        assertEquals(0x3f3f3f,game.blendRgb256(0,0xffffff,64));
        assertEquals(0x7f7f7f,game.blendRgb256(0,0xffffff,128));
        assertEquals(0xffbfbfbf,game.blendRgb256(0xffffff,0,64));
        assertEquals(0x7e0080,game.blendRgb256(0xff0000,0x0000ff,128+1));
    }

    @Test public void everyWeightUsesIndependentChannelsAndTruncatesBeforePacking() {
        int[][] pairs={{0x010203,0xfedcba},{0x090307,0x020805},{0xffffff,0},{0,0xffffff},{0x800001,0x01ff80}};
        for(int[] pair:pairs) for(int n=0;n<=256;n++)
            assertEquals("n="+n,expected(pair[0],pair[1],n),game.blendRgb256(pair[0],pair[1],n));
        assertEquals(0,game.blendRgb256(0,0x010101,255));
        assertEquals(0,game.blendRgb256(0x010101,0,1));
    }

    @Test public void allBytePairsAtCriticalWeightsDoNotCarryBetweenChannels() {
        for(int a=0;a<256;a++) for(int b=0;b<256;b++) for(int n:new int[]{0,1,127,128,129,255,256}) {
            int scalar=(a*(256-n)+b*n)/256;
            assertEquals(scalar,game.blendRgb256(a,b,n));
            assertEquals(scalar<<8,game.blendRgb256(a<<8,b<<8,n));
            assertEquals((scalar<<24)>>8,game.blendRgb256(a<<16,b<<16,n));
        }
    }

    @Test public void returnIsSigned24BitRgbRatherThanArgbOrUnsignedRgb() {
        assertEquals(0x007fffff,game.blendRgb256(0x7fffff,0,0));
        assertEquals(0xff800000,game.blendRgb256(0x800000,0,0));
        assertEquals(-1,game.blendRgb256(0xffffff,0xffffff,128));
        assertEquals(0,game.blendRgb256(0xff000000,0x80000000,128));
    }

    @Test public void outOfRangeWeightsAreNotClampedOrRejected() {
        assertEquals(0x00020202,game.blendRgb256(0,0x010101,512));
        assertEquals(0x00030303,game.blendRgb256(0x020202,0x010101,-256));
        assertEquals(0,game.blendRgb256(0,0,Integer.MIN_VALUE));
        assertEquals(0,game.blendRgb256(0,0,Integer.MAX_VALUE));
    }
}
