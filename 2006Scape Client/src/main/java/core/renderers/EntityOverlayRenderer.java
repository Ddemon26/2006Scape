package core.renderers;
import core.engine.Game;
import game.entities.Entity;
import game.definitions.EntityDef;
import game.entities.NPC;
import game.entities.Player;
import render.core.DrawingArea;

/** Renders entity overlays like names and hit markers extracted from {@link Game}. */
public final class EntityOverlayRenderer {
    private final Game game;

    public EntityOverlayRenderer(Game game) {
        this.game = game;
    }

    public void renderEntityOverlays() {
                                int overheadTextCount = 0;
                                for (int j = -1; j < game.playerCount + game.npcCount; j++) {
                                        Entity entity;
                                        if (j == -1) {
                                                entity = game.myPlayer;
                                        } else if (j < game.playerCount) {
                                                entity = game.playerArray[game.playerIndices[j]];
                                        } else {
                                                entity = game.npcArray[game.npcIndices[j - game.playerCount]];
                                        }
                                        if (entity == null || !entity.isVisible()) {
                                                continue;
                                        }
                                        if (entity instanceof NPC) {
                                        EntityDef entityDef = ((NPC) entity).definition;
        					if (entityDef.childrenIDs != null) {
                                                entityDef = entityDef.transform();
        					}
        					if (entityDef == null) {
        						continue;
        					}
        				}
        				if (j < game.playerCount) {
        					int l = 30;
        					Player player = (Player) entity;
        					if (player.combatLevel == 0) {
        						if (game.customSettingVisiblePlayerNames) {
        							// Show shops
        							game.npcScreenPos(entity, entity.height + 15);
        							// ItemDef.getSprite(995, 1000, 0xffff00).drawTransparentSprite(game.spriteDrawX - 16, game.spriteDrawY - l);
        							game.plainFont.textCenter(0x00ffff, "[SHOP]", game.spriteDrawY - 5, game.spriteDrawX);
        						}
        					} else if (game.customSettingVisiblePlayerNames) {
        						// Show player names
        						game.npcScreenPos(entity, entity.height + 15);
        						game.plainFont.textCenter(0xffffff, player.name, game.spriteDrawY - 5, game.spriteDrawX);
        						if (player.privelage >= 1) {
        							game.npcScreenPos(entity, entity.height + 15);
        							int icon = Math.max(0, Math.min(1, player.privelage - 1));
        							game.modIcons[icon].draw( game.spriteDrawX - player.name.length() * 3 - 16, game.spriteDrawY - 7);
        						}
        					}
        					if (player.headIcon >= 0) {
        						game.npcScreenPos(entity, entity.height + 15);
        						if (game.spriteDrawX > -1) {
        							if (player.skullIcon < 2) {
        								game.skullIcons[player.skullIcon].drawTransparentSprite(game.spriteDrawX - 12, game.spriteDrawY - l);
        								l += 25;
        							}
        							if (player.headIcon < 7) {
        								game.headIcons[player.headIcon].drawTransparentSprite(game.spriteDrawX - 12, game.spriteDrawY - l);
        								l += 18;
        							}
        						}
        					}
        					if (j >= 0 && game.hintIconState == 10 && game.selectedPlayerId == game.playerIndices[j]) {
        						game.npcScreenPos(entity, entity.height + 15);
        						if (game.spriteDrawX > -1) {
        							game.headIconsHint[1].drawTransparentSprite(game.spriteDrawX - 12, game.spriteDrawY - l);
        						}
        					}
        				} else {
                                        EntityDef entityDef_1 = ((NPC) entity).definition;
                                                if (entityDef_1.headIcon >= 0 && entityDef_1.headIcon < game.headIcons.length) {
        						game.npcScreenPos(entity, entity.height + 15);
        						if (game.spriteDrawX > -1) {
                                                                game.headIcons[entityDef_1.headIcon].drawTransparentSprite(game.spriteDrawX - 12, game.spriteDrawY - 30);
        						}
        					}
        					if (game.hintIconState == 1 && game.hintNpcIndex == game.npcIndices[j - game.playerCount] && game.loopCycle % 20 < 10) {
        						game.npcScreenPos(entity, entity.height + 15);
        						if (game.spriteDrawX > -1) {
        							game.headIconsHint[0].drawTransparentSprite(game.spriteDrawX - 12, game.spriteDrawY - 28);
        						}
        					}
        				}
        				// Chat messages sent
        				if (entity.textSpoken != null && (j >= game.playerCount || game.publicChatMode == 0 || game.publicChatMode == 3 || game.publicChatMode == 1 && game.isFriendOrSelf(((Player) entity).name))) {
        					game.npcScreenPos(entity, entity.height);
                                                if (game.spriteDrawX > -1 && overheadTextCount < game.maxDisplayedText) {
                                                        game.textWidth[overheadTextCount] = game.chatTextDrawingArea.measurePlainTextWidth(entity.textSpoken) / 2;
                                                        game.textHeight[overheadTextCount] = game.chatTextDrawingArea.fontHeight;
                                                        game.textX[overheadTextCount] = game.spriteDrawX;
                                                        game.textY[overheadTextCount] = game.spriteDrawY;
                                                        game.textColors[overheadTextCount] = entity.chatColor;
                                                        game.textEffects[overheadTextCount] = entity.chatEffect;
                                                        game.textCycles[overheadTextCount] = entity.textCycle;
                                                        game.overheadTexts[overheadTextCount++] = entity.textSpoken;
        						if (game.chatEffectsState == 0 && entity.chatEffect >= 1 && entity.chatEffect <= 3) {
                                                                game.textHeight[overheadTextCount] += 10;
                                                                game.textY[overheadTextCount] += 5;
        						}
        						if (game.chatEffectsState == 0 && entity.chatEffect == 4) {
                                                                game.textWidth[overheadTextCount] = 60;
        						}
        						if (game.chatEffectsState == 0 && entity.chatEffect == 5) {
                                                                game.textHeight[overheadTextCount] += 5;
        						}
        					}
        				}
        				// HP markers for player?
        				if (entity.loopCycleStatus > game.loopCycle) {
        					try {
        						game.npcScreenPos(entity, entity.height + 15);
        						if (game.spriteDrawX > -1) {
        							int i1 = entity.currentHealth * 30 / entity.maxHealth;
        							if (i1 > 30) {
        								i1 = 30;
        							}
        							DrawingArea.fillArea(5, game.spriteDrawY - 3, 0x00ff00, i1, game.spriteDrawX - 15);
        							DrawingArea.fillArea(5, game.spriteDrawY - 3, 0xff0000, 30 - i1, game.spriteDrawX - 15 + i1);
        						}
        					} catch (Exception e) {
        					}
        				}
        				// Hit markers
        				for (int j1 = 0; j1 < 4; j1++) {
        					if (entity.hitsLoopCycle[j1] > game.loopCycle) {
        						game.npcScreenPos(entity, entity.height / 2);
        						if (game.spriteDrawX > -1) {
        							if (j1 == 1) {
        								game.spriteDrawY -= 20;
        							}
        							if (j1 == 2) {
        								game.spriteDrawX -= 15;
        								game.spriteDrawY -= 10;
        							}
        							if (j1 == 3) {
        								game.spriteDrawX += 15;
        								game.spriteDrawY -= 10;
        							}
        							game.hitMarks[entity.hitMarkTypes[j1]].drawTransparentSprite(game.spriteDrawX - 12, game.spriteDrawY - 12);
        							game.plainFont.textCenter(0, String.valueOf(entity.hitArray[j1]), game.spriteDrawY + 4, game.spriteDrawX);
        							game.plainFont.textCenter(0xffffff, String.valueOf(entity.hitArray[j1]), game.spriteDrawY + 3, game.spriteDrawX - 1);
        						}
        					}
        				}
        			}
        			// Hit markers
                                for (int k = 0; k < overheadTextCount; k++) {
        				int k1 = game.textX[k];
        				int l1 = game.textY[k];
        				int j2 = game.textWidth[k];
        				int k2 = game.textHeight[k];
        				boolean flag = true;
        				while (flag) {
        					flag = false;
        					for (int l2 = 0; l2 < k; l2++) {
        						if (l1 + 2 > game.textY[l2] - game.textHeight[l2] && l1 - k2 < game.textY[l2] + 2 && k1 - j2 < game.textX[l2] + game.textWidth[l2] && k1 + j2 > game.textX[l2] - game.textWidth[l2] && game.textY[l2] - game.textHeight[l2] < l1) {
        							l1 = game.textY[l2] - game.textHeight[l2];
        							flag = true;
        						}
        					}
        
        				}
        				game.spriteDrawX = game.textX[k];
        				game.spriteDrawY = game.textY[k] = l1;
        				String s = game.overheadTexts[k];
        				if (game.chatEffectsState == 0) {
        					int i3 = 0xffff00;
        					if (game.textColors[k] < 6) {
        						i3 = game.hitmarkColors[game.textColors[k]];
        					}
        					if (game.textColors[k] == 6) {
        						i3 = game.waveCycle % 20 >= 10 ? 0xffff00 : 0xff0000;
        					}
        					if (game.textColors[k] == 7) {
        						i3 = game.waveCycle % 20 >= 10 ? 0x00ffff : 255;
        					}
        					if (game.textColors[k] == 8) {
        						i3 = game.waveCycle % 20 >= 10 ? 0x80ff80 : 45056;
        					}
        					if (game.textColors[k] == 9) {
        						int j3 = 150 - game.textCycles[k];
        						if (j3 < 50) {
        							i3 = 0xff0000 + 1280 * j3;
        						} else if (j3 < 100) {
        							i3 = 0xffff00 - 0x50000 * (j3 - 50);
        						} else if (j3 < 150) {
        							i3 = 0x00ff00 + 5 * (j3 - 100);
        						}
        					}
        					if (game.textColors[k] == 10) {
        						int k3 = 150 - game.textCycles[k];
        						if (k3 < 50) {
        							i3 = 0xff0000 + 5 * k3;
        						} else if (k3 < 100) {
        							i3 = 0xff00ff - 0x50000 * (k3 - 50);
        						} else if (k3 < 150) {
        							i3 = 255 + 0x50000 * (k3 - 100) - 5 * (k3 - 100);
        						}
        					}
        					if (game.textColors[k] == 11) {
        						int l3 = 150 - game.textCycles[k];
        						if (l3 < 50) {
        							i3 = 0xffffff - 0x50005 * l3;
        						} else if (l3 < 100) {
        							i3 = 0x00ff00 + 0x50005 * (l3 - 50);
        						} else if (l3 < 150) {
        							i3 = 0xffffff - 0x50000 * (l3 - 100);
        						}
        					}
        					if (game.textEffects[k] == 0) {
        						game.chatTextDrawingArea.textCenter(0, s, game.spriteDrawY + 1, game.spriteDrawX);
        						game.chatTextDrawingArea.textCenter(i3, s, game.spriteDrawY, game.spriteDrawX);
        					}
        					if (game.textEffects[k] == 1) {
                                                        game.chatTextDrawingArea.drawWavyCenteredText(0, s, game.spriteDrawX, game.waveCycle, game.spriteDrawY + 1);
                                                        game.chatTextDrawingArea.drawWavyCenteredText(i3, s, game.spriteDrawX, game.waveCycle, game.spriteDrawY);
        					}
        					if (game.textEffects[k] == 2) {
                                                        game.chatTextDrawingArea.drawWavyText(game.spriteDrawX, s, game.waveCycle, game.spriteDrawY + 1, 0);
                                                        game.chatTextDrawingArea.drawWavyText(game.spriteDrawX, s, game.waveCycle, game.spriteDrawY, i3);
        					}
        					if (game.textEffects[k] == 3) {
                                                        game.chatTextDrawingArea.drawShakeText(150 - game.textCycles[k], s, game.waveCycle, game.spriteDrawY + 1, game.spriteDrawX, 0);
                                                        game.chatTextDrawingArea.drawShakeText(150 - game.textCycles[k], s, game.waveCycle, game.spriteDrawY, game.spriteDrawX, i3);
        					}
        					if (game.textEffects[k] == 4) {
                                                        int i4 = game.chatTextDrawingArea.measurePlainTextWidth(s);
        						int k4 = (150 - game.textCycles[k]) * (i4 + 100) / 150;
        						DrawingArea.setDrawingArea(334, game.spriteDrawX - 50, game.spriteDrawX + 50, 0);
        						game.chatTextDrawingArea.textLeft(0, s, game.spriteDrawY + 1, game.spriteDrawX + 50 - k4);
        						game.chatTextDrawingArea.textLeft(i3, s, game.spriteDrawY, game.spriteDrawX + 50 - k4);
        						DrawingArea.defaultDrawingAreaSize();
        					}
        					if (game.textEffects[k] == 5) {
        						int j4 = 150 - game.textCycles[k];
        						int l4 = 0;
        						if (j4 < 25) {
        							l4 = j4 - 25;
        						} else if (j4 > 125) {
        							l4 = j4 - 125;
        						}
                                                        DrawingArea.setDrawingArea(game.spriteDrawY + 5, 0, 512, game.spriteDrawY - game.chatTextDrawingArea.fontHeight - 1);
        						game.chatTextDrawingArea.textCenter(0, s, game.spriteDrawY + 1 + l4, game.spriteDrawX);
        						game.chatTextDrawingArea.textCenter(i3, s, game.spriteDrawY + l4, game.spriteDrawX);
        						DrawingArea.defaultDrawingAreaSize();
        					}
        				} else {
        					game.chatTextDrawingArea.textCenter(0, s, game.spriteDrawY + 1, game.spriteDrawX);
        					game.chatTextDrawingArea.textCenter(0xffff00, s, game.spriteDrawY, game.spriteDrawX);
        				}
        			}
    }}

