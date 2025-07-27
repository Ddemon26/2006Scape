# SpecialPlantOne

Package `com.rs2.game.content.skills.farming`.

Defined in [`2006Scape Server/src/main/java/com/rs2/game/content/skills/farming/SpecialPlantOne.java`](2006Scape Server/src/main/java/com/rs2/game/content/skills/farming/SpecialPlantOne.java).

Created by IntelliJ IDEA. User: vayken Date: 24/02/12 Time: 20:34 To change this template use File | Settings | File Templates.

```java
public class SpecialPlantOne {
public SpecialPlantOne(game.entities.Player player)
public static SpecialPlantData forId(int saplingId)
public int getSapplingId()
public int getHarvestId()
public int getSeedAmount()
public int getLevelRequired()
public int getGrowthTime()
public double getDiseaseChance()
public double getPlantingXp()
public double getHarvestXp()
public int getStartingState()
public int getEndingState()
public int getCheckHealthState()
public double getCheckHealthXp()
public int getDiseaseDiffValue()
public int getDeathDiffValue()
public static SpecialPlantFieldsData forIdPosition(int x, int y)
public int getSpecialPlantsIndex()
public Point[] getSpecialPlantPosition()
public int getSaplingd()
public static InspectData forId(int saplingId)
public int getSeedId()
public String[][] getMessages()
public void updateSpecialPlants()
public void doCalculations()
public void modifyStage(int i)
public void doStateCalculation(int index)
public boolean clearPatch(int objectX, int objectY, int itemId)
public void execute(CycleEventContainer container)
public void stop()
public boolean plantSapling(int objectX, int objectY, final int saplingId)
public void execute(CycleEventContainer container)
public void stop()
public boolean harvestOrCheckHealth(int objectX, int objectY)
public void execute(CycleEventContainer container)
public void stop()
public void lowerStage(int index, int timer)
public boolean putCompost(int objectX, int objectY, final int itemId)
public void execute(CycleEventContainer container)
public void stop()
public boolean inspect(int objectX, int objectY)
public void execute(CycleEventContainer container)
public void stop()
public boolean guide(int objectX, int objectY)
public boolean curePlant(int objectX, int objectY, int itemId)
public void execute(CycleEventContainer container)
public void stop()
public boolean checkIfRaked(int objectX, int objectY)
```
