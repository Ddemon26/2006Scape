/* MidiEventSink - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

public interface MidiEventSink extends Runnable
{
    public void writeEvents(int[] events, int count);
    
    public void method11(boolean bool);
    
    public void method12(boolean bool);
    
    public void close();
    
    public int getPlaybackPosition(int i);
    
    public void method15(byte i);
}
