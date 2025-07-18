# GlobalDropsHandler

Package `com.rs2.world`.

Defined in [`2006Scape Server/src/main/java/com/rs2/world/GlobalDropsHandler.java`](2006Scape Server/src/main/java/com/rs2/world/GlobalDropsHandler.java).

Handles global drops which respawn after set amount of time when taken  @author Stuart <RogueX>

```java
public class GlobalDropsHandler {
private static final List<GlobalDrop> globalDrops = new ArrayList<>();
private static final Set<GlobalDrop> spawnedDrops = new HashSet<>();
public static void initialize()
public void execute(CycleEventContainer container)
public void stop()
```
