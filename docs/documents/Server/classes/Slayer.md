# Slayer

Package `com.rs2.game.content.skills.slayer`.

Defined in [`2006Scape Server/src/main/java/com/rs2/game/content/skills/slayer/Slayer.java`](2006Scape Server/src/main/java/com/rs2/game/content/skills/slayer/Slayer.java).

Slayer helper class.

```java
public class Slayer {
public static ArrayList<Integer> veryEasyTask = new ArrayList<Integer>();
public static ArrayList<Integer> easyTask = new ArrayList<Integer>();
public static ArrayList<Integer> mediumTask = new ArrayList<Integer>();
public static ArrayList<Integer> hardTask = new ArrayList<Integer>();
public static ArrayList<Integer> veryHardTask = new ArrayList<Integer>();
public Slayer(Player player)
public int getId()
public int getCombatRequirement()
public String getLocation()
public String getMaster()
public int getDifficulty()
public int getNpcId()
public int getExp()
public int getLevelReq()
public int getDifficulty()
public String getLocation()
public static int r(int random)
public boolean canAttackNpc(int i)
public void resizeTable(int difficulty)
public static boolean getMasterRequirment(Player player, int id)
public int getTaskExp(int npcId)
public int getRequiredLevel(int npcId)
public String getLocation(int npcId)
public String getMasterLocation(int npcId)
public boolean isSlayerNpc(int npcId)
public boolean isSlayerTask(int npcId)
public int getDifficulty(int npcId)
public String getSlayerMaster(int npcId)
public String getTaskName(int npcId)
public int getTaskId(String name)
public boolean hasTask()
public void generateTask()
public int getTaskAmount(int task_id)
public int getSlayerDifficulty(Player c2)
public int getRandomTask(int diff)
public void cancelTask()
public void removeTask()
public void updatePoints()
public void updateCurrentlyRemoved()
public void buySlayerExperience()
public void buySlayerDart()
public void buyBroadArrows()
public void buyRespite()
```
