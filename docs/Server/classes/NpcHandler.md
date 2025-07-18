# NpcHandler

Package `com.rs2.game.npcs`.

Defined in [`2006Scape Server/src/main/java/com/rs2/game/npcs/NpcHandler.java`](2006Scape Server/src/main/java/com/rs2/game/npcs/NpcHandler.java).

Handles npc related functionality.

```java
public class NpcHandler {
public void spawnSecondForm(Player c, final int i)
public void execute(CycleEventContainer container)
public void stop()
public void spawnFirstForm(Player c, final int i)
public void execute(CycleEventContainer container)
public void stop()
public void catchRat(final int npcIndex)
public void execute(CycleEventContainer container)
public void stop()
public NpcHandler()
public static boolean isUndead(int index)
public boolean switchesAttackers(int i) { // This seems unused, that's probably not good
public int getClosePlayer(Player c, int i)
public void spawnNpc2(int npcType, int x, int y, int heightLevel, int WalkingType, int HP, int maxHit, int attack, int defence, boolean attackPlayer)
public void newNPCList(int npcType, String npcName, int combat, int HP)
public int getKillerId(int playerId)
public void process()
public boolean getsPulled(Player c, int i)
public static boolean multiAttacks(int i)
public static void handleClipping(int i)
public void dropItems(int i)
public void appendSlayerExperience(int i)
public void resetEvent(int i)
public void resetPlayersInCombat(int i)
public static int GetMove(int Place1, int Place2)
public static boolean followPlayer(int i)
public static void followPlayer(int i, Player player)
public static int distanceRequired(int i)
public static int followDistance(int i)
public static int getProjectileSpeed(int i)
public static int offset(int i)
public boolean specialCase(Player c, int i) { // responsible for npcs that
public boolean retaliates(int npcType)
public static void handleSpecialEffects(Player c, int i, int damage)
public static int getMaxHit(int i)
public static int getNpcListCombat(int npcId)
public void loadSpawnList()
public boolean writeAutoSpawn(String FileName)
public static int getNpcListHP(int npcId)
public static String getNpcListName(int npcId)
public void loadNPCList()
public boolean writeNpcListJson(String FileName)
public static boolean checkSpawn(Client player, int i)
public boolean getNpcListAggressive(int npcId)
public int getNpcSize(int npcId)
```
