# GlobalDropsHandler

Package `com.rs2.world`.

Defined in [`2006Scape Server/src/main/java/com/rs2/world/GlobalDropsHandler.java`](2006Scape Server/src/main/java/com/rs2/world/GlobalDropsHandler.java).

Handles global drops which respawn after set amount of time when taken  @author Stuart <RogueX>

```java
public class GlobalDropsHandler {
public static void initialize()
public void execute(CycleEventContainer container)
public void stop()
public static void writeGlobalDropsDump()
public static boolean itemExists(int itemID, int itemX, int itemY, boolean yes)
public static void pickup(Player player, int itemID, int itemX, int itemY)
public static void load(Client player)
public static void reset(Player c)
public GlobalDrop(int id, int amount, int itemX, int itemY)
public GlobalDrop(int id, int amount, int itemX, int itemY, int height)
public int getX()
public int getY()
public int getId()
public int getAmount()
public boolean isTaken()
public void setTaken(boolean a)
public void setTakenAt(long a)
public long getTakenAt()
public boolean isSpawned()
public void setSpawned(boolean spawned)
public int getHeight()
public void setHeight(int height)
```
