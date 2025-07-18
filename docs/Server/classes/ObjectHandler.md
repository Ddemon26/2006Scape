# ObjectHandler

Package `com.rs2.world`.

Defined in [`2006Scape Server/src/main/java/com/rs2/world/ObjectHandler.java`](2006Scape Server/src/main/java/com/rs2/world/ObjectHandler.java).

@author Sanity

```java
public class ObjectHandler {
public List<Objects> globalObjects = new ArrayList<Objects>();
public static List<Objects> mapObjects = new ArrayList<Objects>();
public static List<Objects> removedObjects = new ArrayList<Objects>();
public ObjectHandler()
public Objects getObjectByPosition(int x, int y)
public void createAnObject(int id, int x, int y, int face)
public void createAnObject(Player c, int id, int x, int y)
public void createAnObject(Player player, int id, int x, int y, int h, int face)
public void createAnObject(Player player, int id, int x, int y, int h)
public void createAnObject(int id, int x, int y, int h, int face, int type)
public void createAnObject(int id, int x, int y)
public void addObject(Objects object)
public void removeObject(Objects object)
public Objects objectExists(int objectX, int objectY, int objectHeight)
public void updateObjects(Player c)
public void placeObject(Objects o)
public void removeAllObjects(Objects o)
public void process()
public boolean isObelisk(int id)
public void startObelisk(int obeliskId)
public int getObeliskIndex(int id)
public void teleportObelisk(int port)
```
