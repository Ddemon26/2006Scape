# CombatAssistant

Package `com.rs2.game.content.combat`.

Defined in [`2006Scape Server/src/main/java/com/rs2/game/content/combat/CombatAssistant.java`](2006Scape Server/src/main/java/com/rs2/game/content/combat/CombatAssistant.java).

@author whoever contributed @author Andrew (Mr Extremez)

```java
public class CombatAssistant {
public CombatAssistant(Player player2)
public boolean inCombat()
public void delayedHit(int i) { // npc hit delay
public void applyNpcMeleeDamage(int i, int damageMask)
public void fireProjectileNpc()
public void attackingNpcTick()
public void attackingPlayerTick()
public void attackNpc(int i)
public void attackPlayer(int i)
public void playerDelayedHit(int i)
public void applyPlayerMeleeDamage(int i, int damageMask)
public void applySmite(int index, int damage)
public void fireProjectilePlayer()
public void resetPlayerAttack()
public int getCombatDifference(int combat1, int combat2)
public boolean checkReqs()
public int getRequiredDistance()
public void applyRecoilNPC(Player c, int damage, int i)
public void applyRecoil(Player c2, int damage, int i)
public void removeRecoil(Player c2)
public int getBonusAttack(int i)
public boolean checkSpecAmount(int weapon)
public int meleeMaxHit()
public int calcDef()
public int calcAtt()
public void getPlayerAnimIndex()
public int getHitDelay()
public int getAttackDelay()
public int getWepAnim()
public int getBlockEmote()
public int rangeMaxHit()
public boolean checkMagicReqs(int spell)
public int calculateRangeDefence()
public int calculateRangeAttack()
public boolean usingBolts()
public boolean properBolts()
public int mageDef()
public int mageAtk()
```
