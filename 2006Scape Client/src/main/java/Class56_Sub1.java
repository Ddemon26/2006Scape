/* Class56_Sub1 - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

abstract class Class56_Sub1 extends Class56
{
    final void applyFade(int fadeAttenuation, int volume, long timestamp) {
    	volume = (int) ((double) volume * Math.pow(0.1, (double) fadeAttenuation * 5.0E-4) + 0.5);
    	if (volume != Game.musicOutputVolume) {
    		Game.musicOutputVolume = volume;
    		for (int channel = 0; channel < 16; channel++) {
    			int outputVolume = channelOutputVolume(channel);
    			sendMessage(channel + 176, 7, outputVolume >> 7, timestamp);
    			sendMessage(channel + 176, 39, outputVolume & 0x7f, timestamp);
    		}
    	}
    }

    abstract void sendMessage(int status, int data1, int data2, long timestamp);

    final boolean filterVolumeController(int status, int controller, int value, long timestamp) {
    	if ((status & 0xf0) == 176) {
    		if (controller == 121) {
    			sendMessage(status, controller, value, timestamp);
    			int channel = status & 0xf;
    			Game.channelVolumes[channel] = 12800;
    			int outputVolume = channelOutputVolume(channel);
    			sendMessage(status, 7, outputVolume >> 7, timestamp);
    			sendMessage(status, 39, outputVolume & 0x7f, timestamp);
    			return true;
    		}
    		if (controller == 7 || controller == 39) {
    			int channel = status & 0xf;
    			if (controller == 7)
    				Game.channelVolumes[channel] = (Game.channelVolumes[channel] & 0x7f) + (value << 7);
    			else
    				Game.channelVolumes[channel] = (Game.channelVolumes[channel] & 0x3f80) + value;
    			int outputVolume = channelOutputVolume(channel);
    			sendMessage(status, 7, outputVolume >> 7, timestamp);
    			sendMessage(status, 39, outputVolume & 0x7f, timestamp);
    			return true;
    		}
    	}
    	return false;
    }

    final void resetAllChannels(long timestamp) {
    	for (int channel = 0; channel < 16; channel++)
    		sendMessage(channel + 176, 123, 0, timestamp);
    	for (int channel = 0; channel < 16; channel++)
    		sendMessage(channel + 176, 120, 0, timestamp);
    	for (int channel = 0; channel < 16; channel++)
    		sendMessage(channel + 176, 121, 0, timestamp);
    	for (int channel = 0; channel < 16; channel++)
    		sendMessage(channel + 176, 0, 0, timestamp);
    	for (int channel = 0; channel < 16; channel++)
    		sendMessage(channel + 176, 32, 0, timestamp);
    	for (int channel = 0; channel < 16; channel++)
    		sendMessage(channel + 192, 0, 0, timestamp);
    }

    final void setMasterVolume(int volume, long timestamp) {
    	Game.musicOutputVolume = volume;
    	for (int channel = 0; channel < 16; channel++)
    		Game.channelVolumes[channel] = 12800;
    	for (int channel = 0; channel < 16; channel++) {
    		int outputVolume = channelOutputVolume(channel);
    		sendMessage(channel + 176, 7, outputVolume >> 7, timestamp);
    		sendMessage(channel + 176, 39, outputVolume & 0x7f, timestamp);
    	}
    }

    private static final int channelOutputVolume(int channel) {
    	int scaled = Game.channelVolumes[channel];
    	scaled = (scaled * Game.musicOutputVolume >> 8) * scaled;
    	return (int) (Math.sqrt((double) scaled) + 0.5);
    }
}
