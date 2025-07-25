# ObjectManager

Package `com.rs2.world`.

Defined in [`2006Scape Server/src/main/java/com/rs2/world/ObjectManager.java`](2006Scape Server/src/main/java/com/rs2/world/ObjectManager.java).

@author Sanity

```java
public class ObjectManager {
public ArrayList<Object> objects = new ArrayList<Object>();
public static void objectTicks(final Player player, final int objectId, final int objectX, final int objectY, final int objectH, final int face, final int objectType, int ticks)
public void execute(CycleEventContainer container)
public void stop()
public static void singleGateTicks(final Player player, final int objectId, final int newObjectX, final int newObjectY, final int oldObjectX, final int oldObjectY, final int objectH, final int face, int ticks)
public void execute(CycleEventContainer container)
public void stop()
public static void doubleGateTicks(final Player player, final int objectId, final int newObjectX, final int newObjectY, final int oldObjectX, final int oldObjectY, final int oldObjectX2, final int oldObjectY2, final int objectH, final int face, int ticks)
public void execute(CycleEventContainer container)
public void stop()
public boolean objectExists(final int x, final int y)
public void process()
public void removeObject(int x, int y)
public void updateObject(Object o)
public void placeObject(Object o)
public Object getObject(int x, int y, int height)
public void loadObjects(Player c)
public void loadCustomSpawns(Player c)
public boolean isObelisk(int id)
public void startObelisk(int obeliskId)
public int getObeliskIndex(int id)
public void teleportObelisk(int port)
public boolean loadForPlayer(Object o, Player c)
public void addObject(Object o)
```
