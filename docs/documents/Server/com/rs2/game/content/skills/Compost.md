# Compost

Package `com.rs2.game.content.skills.farming`.

Defined in [`2006Scape Server/src/main/java/com/rs2/game/content/skills/farming/Compost.java`](2006Scape Server/src/main/java/com/rs2/game/content/skills/farming/Compost.java).

Created by IntelliJ IDEA. User: vayken Date: 22/02/12 Time: 15:43 To change this template use File | Settings | File Templates.

```java
public class Compost {
public Compost(game.entities.Player player)
public static CompostBinLocations forId(int index)
public static CompostBinLocations forPosition(int x, int y)
public int getCompostIndex()
public int getBinObjectId()
public int getObjectFace()
public static CompostBinStages forId(int binId)
public int getBinEmpty()
public int getClosedBin()
public int getBinWithCompostable()
public int getBinFullOfCompostable()
public int getBinWithSuperCompostable()
public int getBinFullOFSuperCompostable()
public int getBinWithCompost()
public int getBinFullOfCompost()
public int getBinWithSuperCompost()
public int getBinFullOfSuperCompost()
public int getBinWithTomatoes()
public int getBinFullOfTomatoes()
public int getBinWithRottenTomatoes()
public int getBinFullOfRottenTomatoes()
public void closeCompostBin(final int index)
public void execute(CycleEventContainer container)
public void stop()
public void openCompostBin(final int index)
public void execute(CycleEventContainer container)
public void stop()
public void fillCompostBin(int x, int y, final int organicItemUsed)
public void execute(CycleEventContainer container)
public void stop()
public void retrieveCompost(final int index)
public void execute(CycleEventContainer container)
public void stop()
public boolean handleObjectClick(int objectId, int objectX, int objectY)
public void resetVariables(int index)
```
