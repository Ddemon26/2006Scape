package com.rs2.domain;

/* AbstractMidiController - Decompiled by JODE
 * Visit http://jode.sourceforge.net/
 */

abstract class AbstractMidiController extends MidiPlayer
{
    final void applyVolumeFade(int step, int volume, long timestamp) {
        volume = (int) ((double) volume * Math.pow(0.1, (double) step * 5.0E-4) + 0.5);
        if (volume != Game.anInt1401) {
            Game.anInt1401 = volume;
            for (int channel = 0; channel < 16; channel++) {
                int scaled = calculateChannelVolume(channel);
                sendShortMessage(channel + 176, 7, scaled >> 7, timestamp);
                sendShortMessage(channel + 176, 39, scaled & 0x7f, timestamp);
            }
        }
    }
    
    abstract void sendShortMessage(int status, int data1, int data2, long timestamp);
    
    final boolean handleControlChange(int status, int controller, int value, long timestamp) {
        if ((status & 0xf0) == 176) {
            if (controller == 121) {
                sendShortMessage(status, controller, value, timestamp);
                int channel = status & 0xf;
                Game.anIntArray385[channel] = 12800;
                int scaled = calculateChannelVolume(channel);
                sendShortMessage(status, 7, scaled >> 7, timestamp);
                sendShortMessage(status, 39, scaled & 0x7f, timestamp);
                return true;
            }
            if (controller == 7 || controller == 39) {
                int channel = status & 0xf;
                if (controller == 7)
                    Game.anIntArray385[channel] = (Game.anIntArray385[channel] & 0x7f) + (value << 7);
                else
                    Game.anIntArray385[channel] = (Game.anIntArray385[channel] & 0x3f80) + value;
                int scaled = calculateChannelVolume(channel);
                sendShortMessage(status, 7, scaled >> 7, timestamp);
                sendShortMessage(status, 39, scaled & 0x7f, timestamp);
                return true;
            }
        }
        return false;
    }
    
    final void resetAllControllers(long timestamp) {
        for (int channel = 0; channel < 16; channel++)
            sendShortMessage(channel + 176, 123, 0, timestamp);
        for (int channel = 0; channel < 16; channel++)
            sendShortMessage(channel + 176, 120, 0, timestamp);
        for (int channel = 0; channel < 16; channel++)
            sendShortMessage(channel + 176, 121, 0, timestamp);
        for (int channel = 0; channel < 16; channel++)
            sendShortMessage(channel + 176, 0, 0, timestamp);
        for (int channel = 0; channel < 16; channel++)
            sendShortMessage(channel + 176, 32, 0, timestamp);
        for (int channel = 0; channel < 16; channel++)
            sendShortMessage(channel + 192, 0, 0, timestamp);
    }
    
    final void setMasterVolume(int volume, long timestamp) {
        Game.anInt1401 = volume;
        for (int channel = 0; channel < 16; channel++)
            Game.anIntArray385[channel] = 12800;
        for (int channel = 0; channel < 16; channel++) {
            int scaled = calculateChannelVolume(channel);
            sendShortMessage(channel + 176, 7, scaled >> 7, timestamp);
            sendShortMessage(channel + 176, 39, scaled & 0x7f, timestamp);
        }
    }
    
    private static final int calculateChannelVolume(int channel) {
        int value = Game.anIntArray385[channel];
        value = (value * Game.anInt1401 >> 8) * value;
        return (int) (Math.sqrt((double) value) + 0.5);
    }
}
