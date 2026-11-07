abstract class MusicPlayer {
    
    abstract void play(int volume, byte[] midiData, int fadeAttenuation, boolean loop);
    
    abstract void shutdown();
    
  	public MusicPlayer() {
	/* empty */
    }
    
    abstract void fade(int volume, int fadeAttenuation);
    
    abstract void setVolume(int volume);
    
    abstract void tick(int guard);
    
    abstract void stop();
}