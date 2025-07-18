# ObjectManager

Package `com.rs2.world`.

Defined in [`2006Scape Server/src/main/java/com/rs2/world/ObjectManager.java`](2006Scape Server/src/main/java/com/rs2/world/ObjectManager.java).

@author Sanity

```java
public class ObjectManager {
public ArrayList<Object> objects = new ArrayList<Object>();
private final ArrayList<Object> toRemove = new ArrayList<Object>();
public static void objectTicks(final Player player, final int objectId, final int objectX, final int objectY, final int objectH, final int face, final int objectType, int ticks)
public void execute(CycleEventContainer container)
public void stop()
```
