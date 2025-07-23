package core;

import audio.SoundPlayer;
import render.Texture;
import game.ItemDef;
import util.Varp;

/** Applies configuration changes originally handled in {@link Game}. */
final class SettingApplier {
    private final Game game;

    SettingApplier(Game game) {
        this.game = game;
    }

    void applyVarp(int i) {
        int action = Varp.cache[i].actionType;
        if (action == 0) {
            return;
        }
        int config = game.variousSettings[i];
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
            game.welcomeScreenRaised = true;
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
            if (volume != Game.musicVolume) {
                if (Game.musicVolume != 0 || game.currentSong == -1) {
                    if (volume != 0)
                        GameMusicController.setVolume(volume);
                    else {
                        GameMusicController.stopMusic(false);
                        game.previousSong = 0;
                    }
                } else {
                    game.musicController.playSong(volume, false, game.currentSong);
                    game.previousSong = 0;//TODO temp music
                }
                Game.musicVolume = volume;
            }
        }
        if (action == 4) {
            SoundPlayer.setVolume(config);
            if (config == 0) {
                game.soundEffectEnabled = true;
                game.setWaveVolume(0);
            }
            if (config == 1) {
                game.soundEffectEnabled = true;
                game.setWaveVolume(-400);
            }
            if (config == 2) {
                game.soundEffectEnabled = true;
                game.setWaveVolume(-800);
            }
            if (config == 3) {
                game.soundEffectEnabled = true;
                game.setWaveVolume(-1200);
            }
            if (config == 4) {
                game.soundEffectEnabled = false;
            }
        }
        if (action == 5) {
            game.oneMouseButtonMode = config;
        }
        if (action == 6) {
            game.chatEffectsState = config;
        }
        if (action == 8) {
            game.splitpublicChat = config;
            game.inputTaken = true;
        }
        if (action == 9) {
            game.configActionId = config;
        }
    }
}
